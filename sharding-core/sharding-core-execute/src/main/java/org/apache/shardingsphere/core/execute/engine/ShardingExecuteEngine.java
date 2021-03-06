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

package org.apache.shardingsphere.core.execute.engine;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import org.apache.shardingsphere.core.exception.ShardingException;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Sharding execute engine.
 *
 * @author zhangliang
 *
 *  分片执行引擎入口类
 */
public final class ShardingExecuteEngine implements AutoCloseable {
    
    private final ShardingExecutorService shardingExecutorService;
    
    private ListeningExecutorService executorService;
    
    public ShardingExecuteEngine(final int executorSize) {
        shardingExecutorService = new ShardingExecutorService(executorSize);
        executorService = shardingExecutorService.getExecutorService();
    }
    
    /**
     * Execute for group.
     *
     * @param inputGroups input groups
     * @param callback sharding execute callback
     * @param <I> type of input value
     * @param <O> type of return value
     * @return execute result
     * @throws SQLException throw if execute failure
     */
    public <I, O> List<O> groupExecute(final Collection<ShardingExecuteGroup<I>> inputGroups, final ShardingGroupExecuteCallback<I, O> callback) throws SQLException {
        return groupExecute(inputGroups, null, callback, false);
    }
    
    /**
     * Execute for group.
     *
     * @param inputGroups input groups
     * @param firstCallback first sharding execute callback
     * @param callback sharding execute callback
     * @param serial whether using multi thread execute or not
     * @param <I> type of input value
     * @param <O> type of return value
     * @return execute result
     * @throws SQLException throw if execute failure
     */
    public <I, O> List<O> groupExecute(
        final Collection<ShardingExecuteGroup<I>> inputGroups, final ShardingGroupExecuteCallback<I, O> firstCallback, final ShardingGroupExecuteCallback<I, O> callback, final boolean serial)
        throws SQLException {
        if (inputGroups.isEmpty()) {
            return Collections.emptyList();
        }

        /**
         *  串行、还是并行 serial
         *     串行执行 {@link #serialExecute(Collection, ShardingGroupExecuteCallback, ShardingGroupExecuteCallback)}
         *     并行执行 {@link #parallelExecute(Collection, ShardingGroupExecuteCallback, ShardingGroupExecuteCallback)}
         */
        return serial ? serialExecute(inputGroups, firstCallback, callback) : parallelExecute(inputGroups, firstCallback, callback);
    }

    // 串行执行
    private <I, O> List<O> serialExecute(final Collection<ShardingExecuteGroup<I>> inputGroups, final ShardingGroupExecuteCallback<I, O> firstCallback,
                                         final ShardingGroupExecuteCallback<I, O> callback) throws SQLException {
        Iterator<ShardingExecuteGroup<I>> inputGroupsIterator = inputGroups.iterator();

        //获取第一个输入的 ShardingExecuteGroup
        ShardingExecuteGroup<I> firstInputs = inputGroupsIterator.next();

        /**
         * 通过第一个回调 firstCallback 完成同步执行的 {@link #syncGroupExecute(ShardingExecuteGroup, ShardingGroupExecuteCallback)}  
         */
        List<O> result = new LinkedList<>(syncGroupExecute(firstInputs, null == firstCallback ? callback : firstCallback));

        //对剩下的 ShardingExecuteGroup，通过回调 callback 逐个同步执行 syncGroupExecute
        for (ShardingExecuteGroup<I> each : Lists.newArrayList(inputGroupsIterator)) {
            result.addAll(syncGroupExecute(each, callback));
        }
        return result;
    }

    /*
     * 无论是  serialExecute、还是 parallelExecute
     *  1、都会从 ShardingExecuteGroup 中获取第一个 firstInputs 元素并进行执行。
     *     然后剩下的再进行同步、或异步执行。
     *  2、ShardingSphere 这样使用线程，考虑到当前线程同样也是一个可用资源。
     *     2.1、让第一个任务由当前线程进行执行，可用充分利用当前线程。从而最大化线程利用率。
     */

    /**
     * 并行执行
     */
    private <I, O> List<O> parallelExecute(final Collection<ShardingExecuteGroup<I>> inputGroups, final ShardingGroupExecuteCallback<I, O> firstCallback,
                                           final ShardingGroupExecuteCallback<I, O> callback) throws SQLException {
        Iterator<ShardingExecuteGroup<I>> inputGroupsIterator = inputGroups.iterator();

        // 获取第一个输入的 ShardingExecuteGroup
        ShardingExecuteGroup<I> firstInputs = inputGroupsIterator.next();

        /**
         * 通过 {@link #asyncGroupExecute(ShardingExecuteGroup, ShardingGroupExecuteCallback)}  执行异步回调
         */
        Collection<ListenableFuture<Collection<O>>> restResultFutures = asyncGroupExecute(Lists.newArrayList(inputGroupsIterator), callback);

        /**
         * 获取执行结果并组装返回
         */
        return getGroupResults(syncGroupExecute(firstInputs, null == firstCallback ? callback : firstCallback), restResultFutures);
    }
    
    private <I, O> Collection<ListenableFuture<Collection<O>>> asyncGroupExecute(final List<ShardingExecuteGroup<I>> inputGroups, final ShardingGroupExecuteCallback<I, O> callback) {
        Collection<ListenableFuture<Collection<O>>> result = new LinkedList<>();
        for (ShardingExecuteGroup<I> each : inputGroups) {
            result.add(asyncGroupExecute(each, callback));
        }
        return result;
    }
    
    private <I, O> ListenableFuture<Collection<O>> asyncGroupExecute(final ShardingExecuteGroup<I> inputGroup, final ShardingGroupExecuteCallback<I, O> callback) {
        final Map<String, Object> dataMap = ShardingExecuteDataMap.getDataMap();

        // 使用 Guava 的 ListeningExecutorService， 提交一个异步执行的任务，并返回一个 ListenableFuture
        return executorService.submit(new Callable<Collection<O>>() {
            
            @Override
            public Collection<O> call() throws SQLException {
                /**
                 * [execute] {@link org.apache.shardingsphere.core.execute.sql.execute.SQLExecuteCallback#execute(Collection, boolean, Map)}
                 */
                return callback.execute(inputGroup.getInputs(), false, dataMap);
            }
        });
    }
    
    private <I, O> Collection<O> syncGroupExecute(final ShardingExecuteGroup<I> executeGroup, final ShardingGroupExecuteCallback<I, O> callback) throws SQLException {
        /**
         *  [execute] {@link org.apache.shardingsphere.core.execute.sql.execute.SQLExecuteCallback#execute(Collection, boolean, Map)}
         */
        return callback.execute(executeGroup.getInputs(), true, ShardingExecuteDataMap.getDataMap());
    }
    
    private <O> List<O> getGroupResults(final Collection<O> firstResults, final Collection<ListenableFuture<Collection<O>>> restFutures) throws SQLException {
        List<O> result = new LinkedList<>(firstResults);
        for (ListenableFuture<Collection<O>> each : restFutures) {
            try {
                result.addAll(each.get());
            } catch (final InterruptedException | ExecutionException ex) {
                return throwException(ex);
            }
        }
        return result;
    }
    
    private <O> List<O> throwException(final Exception exception) throws SQLException {
        if (exception.getCause() instanceof SQLException) {
            throw (SQLException) exception.getCause();
        }
        throw new ShardingException(exception);
    }
    
    @Override
    public void close() {
        shardingExecutorService.close();
    }
}
