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

package org.apache.shardingsphere.api.sharding.standard;

import org.apache.shardingsphere.api.sharding.ShardingAlgorithm;

import java.util.Collection;

/**
 * Precise sharding algorithm.
 * 
 * @author zhangliang
 * 
 * @param <T> class type of sharding value
 *
 *           用于处理 =，in 的分片
 */
public interface PreciseShardingAlgorithm<T extends Comparable<?>> extends ShardingAlgorithm {
    
    /**
     * Sharding.
     * 
     * @param availableTargetNames available data sources or tables's names
     * @param shardingValue sharding value
     * @return sharding result for data source or table's name
     */
    String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<T> shardingValue);
}
