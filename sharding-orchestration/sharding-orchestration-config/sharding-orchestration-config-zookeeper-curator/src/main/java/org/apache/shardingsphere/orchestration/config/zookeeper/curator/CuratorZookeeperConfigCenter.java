/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.orchestration.config.zookeeper.curator;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.shardingsphere.orchestration.config.api.ConfigCenter;
import org.apache.shardingsphere.orchestration.config.api.ConfigCenterConfiguration;
import org.apache.shardingsphere.orchestration.config.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.config.listener.DataChangedEventListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.OperationTimeoutException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Config center for zookeeper with curator.
 * 
 * @author wangguangyuan
 */
public final class CuratorZookeeperConfigCenter implements ConfigCenter {
    
    private final Map<String, TreeCache> caches = new HashMap<>();
    
    private CuratorFramework client;
    
    @Getter
    @Setter
    private Properties properties = new Properties();
    
    @Override
    public void init(final ConfigCenterConfiguration config) {
        client = buildCuratorClient(config);
        initCuratorClient(config);
    }
    
    private CuratorFramework buildCuratorClient(final ConfigCenterConfiguration config) {
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(config.getServerLists())
                .retryPolicy(new ExponentialBackoffRetry(config.getRetryIntervalMilliseconds(), config.getMaxRetries(), config.getRetryIntervalMilliseconds() * config.getMaxRetries()))
                .namespace(config.getNamespace());
        if (0 != config.getTimeToLiveSeconds()) {
            builder.sessionTimeoutMs(config.getTimeToLiveSeconds() * 1000);
        }
        if (0 != config.getOperationTimeoutMilliseconds()) {
            builder.connectionTimeoutMs(config.getOperationTimeoutMilliseconds());
        }
        if (!Strings.isNullOrEmpty(config.getDigest())) {
            builder.authorization("digest", config.getDigest().getBytes(Charsets.UTF_8))
                    .aclProvider(new ACLProvider() {
                        
                        @Override
                        public List<ACL> getDefaultAcl() {
                            return ZooDefs.Ids.CREATOR_ALL_ACL;
                        }
                        
                        @Override
                        public List<ACL> getAclForPath(final String path) {
                            return ZooDefs.Ids.CREATOR_ALL_ACL;
                        }
                    });
        }
        return builder.build();
    }
    
    private void initCuratorClient(final ConfigCenterConfiguration config) {
        client.start();
        try {
            if (!client.blockUntilConnected(config.getRetryIntervalMilliseconds() * config.getMaxRetries(), TimeUnit.MILLISECONDS)) {
                client.close();
                throw new OperationTimeoutException();
            }
        } catch (final InterruptedException | OperationTimeoutException ex) {
            CuratorZookeeperExceptionHandler.handleException(ex);
        }
    }
    
    @Override
    public String get(final String key) {
        TreeCache cache = findTreeCache(key);
        if (null == cache) {
            return getDirectly(key);
        }
        ChildData resultInCache = cache.getCurrentData(key);
        if (null != resultInCache) {
            return null == resultInCache.getData() ? null : new String(resultInCache.getData(), Charsets.UTF_8);
        }
        return getDirectly(key);
    }
    
    private TreeCache findTreeCache(final String key) {
        for (Entry<String, TreeCache> entry : caches.entrySet()) {
            if (key.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
    
    @Override
    public String getDirectly(final String key) {
        try {
            return new String(client.getData().forPath(key), Charsets.UTF_8);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            CuratorZookeeperExceptionHandler.handleException(ex);
            return null;
        }
    }
    
    @Override
    public boolean isExisted(final String key) {
        try {
            return null != client.checkExists().forPath(key);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            CuratorZookeeperExceptionHandler.handleException(ex);
            return false;
        }
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        try {
            List<String> result = client.getChildren().forPath(key);
            Collections.sort(result, new Comparator<String>() {
                
                @Override
                public int compare(final String o1, final String o2) {
                    return o2.compareTo(o1);
                }
            });
            return result;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            CuratorZookeeperExceptionHandler.handleException(ex);
            return Collections.emptyList();
        }
    }
    
    @Override
    public void persist(final String key, final String value) {
        try {
            if (!isExisted(key)) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(key, value.getBytes(Charsets.UTF_8));
            } else {
                update(key, value);
            }
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            CuratorZookeeperExceptionHandler.handleException(ex);
        }
    }
    
    @Override
    public void update(final String key, final String value) {
        try {
            client.inTransaction().check().forPath(key).and().setData().forPath(key, value.getBytes(Charsets.UTF_8)).and().commit();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            CuratorZookeeperExceptionHandler.handleException(ex);
        }
    }
    
    @Override
    public void persistEphemeral(final String key, final String value) {
        try {
            if (isExisted(key)) {
                client.delete().deletingChildrenIfNeeded().forPath(key);
            }
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(key, value.getBytes(Charsets.UTF_8));
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            CuratorZookeeperExceptionHandler.handleException(ex);
        }
    }
    
    @Override
    public void watch(final String key, final DataChangedEventListener dataChangedEventListener) {
        final String path = key + "/";
        if (!caches.containsKey(path)) {
            addCacheData(key);
        }
        TreeCache cache = caches.get(path);
        cache.getListenable().addListener(new TreeCacheListener() {
            
            @Override
            public void childEvent(final CuratorFramework client, final TreeCacheEvent event) throws UnsupportedEncodingException {
                ChildData data = event.getData();
                if (null == data || null == data.getPath()) {
                    return;
                }
                DataChangedEvent.ChangedType changedType = getChangedType(event);
                if (DataChangedEvent.ChangedType.IGNORED != changedType) {
                    dataChangedEventListener.onChange(new DataChangedEvent(data.getPath(), null == data.getData() ? null : new String(data.getData(), "UTF-8"), changedType));
                }
            }
        });
    }
    
    private DataChangedEvent.ChangedType getChangedType(final TreeCacheEvent event) {
        switch (event.getType()) {
            case NODE_UPDATED:
                return DataChangedEvent.ChangedType.UPDATED;
            case NODE_REMOVED:
                return DataChangedEvent.ChangedType.DELETED;
            default:
                return DataChangedEvent.ChangedType.IGNORED;
        }
    }
    
    private void addCacheData(final String cachePath) {
        TreeCache cache = new TreeCache(client, cachePath);
        try {
            cache.start();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            CuratorZookeeperExceptionHandler.handleException(ex);
        }
        caches.put(cachePath + "/", cache);
    }
    
    @Override
    public void close() {
        for (Entry<String, TreeCache> each : caches.entrySet()) {
            each.getValue().close();
        }
        waitForCacheClose();
        CloseableUtils.closeQuietly(client);
    }
    
    /* TODO ??????500ms, cache??????????????????client, ??????????????????
     * ??????????????????, ???????????????client????????????cache??????????????????.
     * ??????Curator?????????????????????bug.
     * BUG?????????https://issues.apache.org/jira/browse/CURATOR-157
     */
    private void waitForCacheClose() {
        try {
            Thread.sleep(500L);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public String getType() {
        return "zookeeper";
    }
}
