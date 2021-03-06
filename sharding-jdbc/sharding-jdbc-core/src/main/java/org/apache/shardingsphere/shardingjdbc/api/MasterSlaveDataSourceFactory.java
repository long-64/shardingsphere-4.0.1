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

package org.apache.shardingsphere.shardingjdbc.api;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.MasterSlaveDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * Master-slave data source factory.
 * 
 * @author zhangliang
 * @author panjuan
 *
 *  配置读写分离
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MasterSlaveDataSourceFactory {
    
    /**
     * Create master-slave data source.
     *
     * @param dataSourceMap data source map
     * @param masterSlaveRuleConfig master-slave rule configuration
     * @param props props
     * @return master-slave data source
     * @throws SQLException SQL exception
     *
     *  读写分离信息 {@link MasterSlaveRuleConfiguration}
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final MasterSlaveRuleConfiguration masterSlaveRuleConfig, final Properties props) throws SQLException {
        return new MasterSlaveDataSource(dataSourceMap, new MasterSlaveRule(masterSlaveRuleConfig), props);
    }
}
