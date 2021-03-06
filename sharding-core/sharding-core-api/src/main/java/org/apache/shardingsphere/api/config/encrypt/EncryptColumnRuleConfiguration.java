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

/**
 * Encrypt column rule configuration.
 *
 * @author panjuan
 *
 *  通过配置文件，解析
 */
@RequiredArgsConstructor
@Getter
public final class EncryptColumnRuleConfiguration {

    // 存储明文字段
    private final String plainColumn;

    // 存储密文字段
    private final String cipherColumn;

    // 辅助查询字段
    private final String assistedQueryColumn;

    // 加密器名称
    private final String encryptor;
}
