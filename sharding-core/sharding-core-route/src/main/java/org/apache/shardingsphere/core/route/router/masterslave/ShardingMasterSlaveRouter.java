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

package org.apache.shardingsphere.core.route.router.masterslave;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.api.hint.HintManager;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Sharding with master-slave router interface.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ShardingMasterSlaveRouter {
    
    private final Collection<MasterSlaveRule> masterSlaveRules;
    
    /**
     * Route Master slave after sharding.
     * 
     * @param sqlRouteResult SQL route result
     * @return route result
     */
    public SQLRouteResult route(final SQLRouteResult sqlRouteResult) {
        for (MasterSlaveRule each : masterSlaveRules) {
            /**
             *  根据每条 MasterSlaveRule 执行路由方法 {@link #route(MasterSlaveRule, SQLRouteResult)}
             */
            route(each, sqlRouteResult);
        }
        return sqlRouteResult;
    }
    
    private void route(final MasterSlaveRule masterSlaveRule, final SQLRouteResult sqlRouteResult) {
        Collection<RoutingUnit> toBeRemoved = new LinkedList<>();
        Collection<RoutingUnit> toBeAdded = new LinkedList<>();
        for (RoutingUnit each : sqlRouteResult.getRoutingResult().getRoutingUnits()) {
            if (!masterSlaveRule.getName().equalsIgnoreCase(each.getDataSourceName())) {
                continue;
            }
            toBeRemoved.add(each);
            String actualDataSourceName;

            // 判断是否走主库
            if (isMasterRoute(sqlRouteResult.getSqlStatementContext().getSqlStatement())) {
                MasterVisitedManager.setMasterVisited();
                actualDataSourceName = masterSlaveRule.getMasterDataSourceName();
            } else {

                /**
                 * 如果从库有多个，默认采用 '轮询策略'，也可以选择 '随机访问' 策略
                 *
                 *   随机算法 {@link org.apache.shardingsphere.core.strategy.masterslave.RandomMasterSlaveLoadBalanceAlgorithm#getDataSource(String, String, List)}
                 *   轮询算法 {@link org.apache.shardingsphere.core.strategy.masterslave.RoundRobinMasterSlaveLoadBalanceAlgorithm#getDataSource(String, String, List)}
                 */
                actualDataSourceName = masterSlaveRule.getLoadBalanceAlgorithm().getDataSource(
                        masterSlaveRule.getName(), masterSlaveRule.getMasterDataSourceName(), new ArrayList<>(masterSlaveRule.getSlaveDataSourceNames()));
            }
            toBeAdded.add(createNewRoutingUnit(actualDataSourceName, each));
        }
        sqlRouteResult.getRoutingResult().getRoutingUnits().removeAll(toBeRemoved);
        sqlRouteResult.getRoutingResult().getRoutingUnits().addAll(toBeAdded);
    }

    // 判断是否走，主库
    private boolean isMasterRoute(final SQLStatement sqlStatement) {
        return containsLockSegment(sqlStatement) || !(sqlStatement instanceof SelectStatement) || MasterVisitedManager.isMasterVisited() || HintManager.isMasterRouteOnly();
    }

    private boolean containsLockSegment(final SQLStatement sqlStatement) {
        return sqlStatement instanceof SelectStatement && ((SelectStatement) sqlStatement).getLock().isPresent();
    }
    
    private RoutingUnit createNewRoutingUnit(final String actualDataSourceName, final RoutingUnit originalTableUnit) {
        RoutingUnit result = new RoutingUnit(actualDataSourceName, originalTableUnit.getDataSourceName());
        result.getTableUnits().addAll(originalTableUnit.getTableUnits());
        return result;
    }
}
