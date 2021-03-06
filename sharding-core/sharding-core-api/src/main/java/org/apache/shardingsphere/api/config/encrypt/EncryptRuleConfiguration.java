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

package org.apache.shardingsphere.api.config.encrypt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.api.config.RuleConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Encrypt rule configuration.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
public final class EncryptRuleConfiguration implements RuleConfiguration {

    // 加解密器配置列表
    private final Map<String, EncryptorRuleConfiguration> encryptors;

    // 加密表配置列表
    private final Map<String, EncryptTableRuleConfiguration> tables;
    
    public EncryptRuleConfiguration() {
        this(new LinkedHashMap<String, EncryptorRuleConfiguration>(), new LinkedHashMap<String, EncryptTableRuleConfiguration>());
    }
}
