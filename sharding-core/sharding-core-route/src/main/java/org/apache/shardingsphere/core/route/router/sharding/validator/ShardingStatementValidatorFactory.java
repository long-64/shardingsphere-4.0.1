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

package org.apache.shardingsphere.core.route.router.sharding.validator;

import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.core.route.router.sharding.validator.impl.ShardingInsertStatementValidator;
import org.apache.shardingsphere.core.route.router.sharding.validator.impl.ShardingUpdateStatementValidator;

/**
 * Sharding statement validator factory.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingStatementValidatorFactory {
    
    /**
     * New instance of sharding statement validator.
     * 
     * @param sqlStatement SQL statement
     * @return instance of sharding statement validator
     *
     *  典型的工厂模型
     *
     *   针对，Insert、Update 进行语法验证
     */
    public static Optional<ShardingStatementValidator> newInstance(final SQLStatement sqlStatement) {

        // 根据不同的类，提供不同的验证方式
        if (sqlStatement instanceof InsertStatement) {

            // InsertStatement 类型
            return Optional.<ShardingStatementValidator>of(new ShardingInsertStatementValidator());
        }
        if (sqlStatement instanceof UpdateStatement) {

            // UpdateStatement 类型
            return Optional.<ShardingStatementValidator>of(new ShardingUpdateStatementValidator());
        }
        return Optional.absent();
    }
}
