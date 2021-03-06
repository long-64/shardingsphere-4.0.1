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

package org.apache.shardingsphere.underlying.rewrite.sql.token.generator;

import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.underlying.rewrite.sql.token.pojo.SQLToken;

import java.util.Collection;

/**
 * SQL token generator for collection.
 *
 * @author zhangliang
 * @author panjuan
 */
public interface CollectionSQLTokenGenerator extends SQLTokenGenerator {
    
    /**
     * Generate SQL tokens.
     * 
     * @param sqlStatementContext SQL statement context
     * @return SQL tokens
     *
     *  生成批量 SQLToken
     */
    Collection<? extends SQLToken> generateSQLTokens(SQLStatementContext sqlStatementContext);
}
