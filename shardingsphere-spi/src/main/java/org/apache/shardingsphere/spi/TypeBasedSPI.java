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

package org.apache.shardingsphere.spi;

import java.util.Properties;

/**
 * Base algorithm SPI.
 *
 * @author zhangliang
 */
public interface TypeBasedSPI {
    
    /**
     * Get algorithm type.
     * 获取SPI对应的类型
     *
     * @return type
     */
    String getType();

    /**
     * Get properties.
     * 获取属性
     *
     * @return properties of algorithm
     */
    Properties getProperties();

    /**
     * Set properties.
     * 设置属性
     *
     * @param properties properties of algorithm
     */
    void setProperties(Properties properties);
}
