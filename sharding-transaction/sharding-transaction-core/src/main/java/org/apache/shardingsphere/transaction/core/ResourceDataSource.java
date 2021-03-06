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

package org.apache.shardingsphere.transaction.core;

import lombok.Getter;

import javax.sql.DataSource;

/**
 * Unique resource data source.
 *
 * @author zhaojun
 */
@Getter
public final class ResourceDataSource {
    
    private final String originalName;
    
    private String uniqueResourceName;
    
    private final DataSource dataSource;
    
    public ResourceDataSource(final String originalName, final DataSource dataSource) {
        this.originalName = originalName;
        this.dataSource = dataSource;

        /**
         * 构建一个唯一资源名称,单例模式 {@link ResourceIDGenerator#nextId()}
         */
        this.uniqueResourceName = ResourceIDGenerator.getInstance().nextId() + originalName;
    }
}
