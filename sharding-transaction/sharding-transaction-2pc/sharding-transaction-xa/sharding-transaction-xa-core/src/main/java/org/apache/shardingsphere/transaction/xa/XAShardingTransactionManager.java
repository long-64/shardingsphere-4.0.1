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

package org.apache.shardingsphere.transaction.xa;

import lombok.SneakyThrows;
import org.apache.shardingsphere.spi.database.DatabaseType;
import org.apache.shardingsphere.transaction.core.ResourceDataSource;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.spi.ShardingTransactionManager;
import org.apache.shardingsphere.transaction.xa.atomikos.manager.AtomikosTransactionManager;
import org.apache.shardingsphere.transaction.xa.jta.datasource.XATransactionDataSource;
import org.apache.shardingsphere.transaction.xa.manager.XATransactionManagerLoader;
import org.apache.shardingsphere.transaction.xa.spi.XATransactionManager;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Sharding transaction manager for XA.
 *
 * @author zhaojun
 */
public final class XAShardingTransactionManager implements ShardingTransactionManager {
    
    private final Map<String, XATransactionDataSource> cachedDataSources = new HashMap<>();

    /**
     *  使用单例 + SPI 机制 {@link XATransactionManagerLoader#getInstance()}
     */
    private final XATransactionManager xaTransactionManager = XATransactionManagerLoader.getInstance().getTransactionManager();
    
    @Override
    public void init(final DatabaseType databaseType, final Collection<ResourceDataSource> resourceDataSources) {
        for (ResourceDataSource each : resourceDataSources) {

            // 创建XATransactionDataSource并进行缓存
            cachedDataSources.put(each.getOriginalName(), new XATransactionDataSource(databaseType, each.getUniqueResourceName(), each.getDataSource(), xaTransactionManager));
        }

        /**
         * 初始化 XATransactionManager
         *   {@link AtomikosTransactionManager#init()}
         *   {@link org.apache.shardingsphere.transaction.xa.bitronix.manager.BitronixXATransactionManager#init()
         */
        xaTransactionManager.init();
    }
    
    @Override
    public TransactionType getTransactionType() {
        return TransactionType.XA;
    }
    
    @SneakyThrows
    @Override
    public boolean isInTransaction() {
        return Status.STATUS_NO_TRANSACTION != xaTransactionManager.getTransactionManager().getStatus();
    }
    
    @Override
    public Connection getConnection(final String dataSourceName) throws SQLException {
        try {
            return cachedDataSources.get(dataSourceName).getConnection();
        } catch (final SystemException | RollbackException ex) {
            throw new SQLException(ex);
        }
    }
    
    @SneakyThrows
    @Override
    public void begin() {
        xaTransactionManager.getTransactionManager().begin();
    }
    
    @SneakyThrows
    @Override
    public void commit() {
        xaTransactionManager.getTransactionManager().commit();
    }
    
    @SneakyThrows
    @Override
    public void rollback() {
        xaTransactionManager.getTransactionManager().rollback();
    }
    
    @Override
    public void close() throws Exception {
        for (XATransactionDataSource each : cachedDataSources.values()) {
            each.close();
        }
        cachedDataSources.clear();
        xaTransactionManager.close();
    }
}
