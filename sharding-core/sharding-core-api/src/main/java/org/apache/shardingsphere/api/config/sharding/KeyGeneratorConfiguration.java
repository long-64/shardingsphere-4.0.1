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

package org.apache.shardingsphere.api.config.sharding;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.api.config.TypeBasedSPIConfiguration;

import java.util.Properties;

/**
 * Key generator configuration.
 *
 * @author panjuan
 */
@Getter
public final class KeyGeneratorConfiguration extends TypeBasedSPIConfiguration {

    /**
     * 首先，指定一个列名。
     *
     *  利用 Properties 配置项，指定自增值生成过程中所需要属性。
     */
    private final String column;
    
    public KeyGeneratorConfiguration(final String type, final String column) {
        super(type);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(column), "Column is required.");
        this.column = column;
    }

    public KeyGeneratorConfiguration(final String type, final String column, final Properties properties) {
        super(type, properties);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(column), "Column is required.");
        this.column = column;
    }
}
