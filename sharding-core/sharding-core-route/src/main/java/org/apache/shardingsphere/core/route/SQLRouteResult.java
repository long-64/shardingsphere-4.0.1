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

package org.apache.shardingsphere.core.route;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingConditions;
import org.apache.shardingsphere.core.route.router.sharding.keygen.GeneratedKey;
import org.apache.shardingsphere.core.route.type.RoutingResult;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * SQL route result.
 * 
 * @author gaohongtao
 * @author zhangliang
 * @author zhaojun
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class SQLRouteResult {

    // SQLStatement 上下文
    private final SQLStatementContext sqlStatementContext;

    // 分片条件
    private final ShardingConditions shardingConditions;

    // 自动生成分片键
    private final GeneratedKey generatedKey;

    // 一组路由单元
    private final Collection<RouteUnit> routeUnits = new LinkedHashSet<>();

    // 由 RoutingEngine 生成的 RoutingResult
    private RoutingResult routingResult;
    
    public SQLRouteResult(final SQLStatementContext sqlStatementContext, final ShardingConditions shardingConditions) {
        this(sqlStatementContext, shardingConditions, null);
    }
    
    /**
     * Get generated key.
     * 
     * @return generated key
     */
    public Optional<GeneratedKey> getGeneratedKey() {
        return Optional.fromNullable(generatedKey);
    }
}
