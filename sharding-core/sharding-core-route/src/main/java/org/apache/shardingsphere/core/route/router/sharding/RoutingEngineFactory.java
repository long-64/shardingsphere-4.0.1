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

package org.apache.shardingsphere.core.route.router.sharding;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.UseStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.postgresql.ResetParameterStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.postgresql.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.TCLStatement;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingConditions;
import org.apache.shardingsphere.core.route.type.RoutingEngine;
import org.apache.shardingsphere.core.route.type.broadcast.DataSourceGroupBroadcastRoutingEngine;
import org.apache.shardingsphere.core.route.type.broadcast.DatabaseBroadcastRoutingEngine;
import org.apache.shardingsphere.core.route.type.broadcast.MasterInstanceBroadcastRoutingEngine;
import org.apache.shardingsphere.core.route.type.broadcast.TableBroadcastRoutingEngine;
import org.apache.shardingsphere.core.route.type.complex.ComplexRoutingEngine;
import org.apache.shardingsphere.core.route.type.defaultdb.DefaultDatabaseRoutingEngine;
import org.apache.shardingsphere.core.route.type.ignore.IgnoreRoutingEngine;
import org.apache.shardingsphere.core.route.type.standard.StandardRoutingEngine;
import org.apache.shardingsphere.core.route.type.unicast.UnicastRoutingEngine;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;

/**
 * Routing engine factory.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RoutingEngineFactory {
    
    /**
     * Create new instance of routing engine.
     * 
     * @param shardingRule sharding rule
     * @param metaData meta data of ShardingSphere
     * @param sqlStatementContext SQL statement context
     * @param shardingConditions shardingConditions
     * @return new instance of routing engine
     *
     *  ??????????????????????????????,??????????????? RoutingEngine
     */
    public static RoutingEngine newInstance(final ShardingRule shardingRule,
                                            final ShardingSphereMetaData metaData, final SQLStatementContext sqlStatementContext, final ShardingConditions shardingConditions) {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();

        //?????????????????? ????????????????????????????????????????????????
        if (sqlStatement instanceof TCLStatement) {

            /* ??????????????? DataSourceName ???????????? RoutingUnit  */
            return new DatabaseBroadcastRoutingEngine(shardingRule);
        }

        //???????????????
        if (sqlStatement instanceof DDLStatement) {

            /**
             *  ??????????????? {@link TableBroadcastRoutingEngine
             */
            return new TableBroadcastRoutingEngine(shardingRule, metaData.getTables(), sqlStatementContext);
        }

        //????????????,
        if (sqlStatement instanceof DALStatement) {

            /**
             * ???????????? {@link #getDALRoutingEngine(ShardingRule, SQLStatement, Collection)}
             */
            return getDALRoutingEngine(shardingRule, sqlStatement, tableNames);
        }
        //???????????????
        if (sqlStatement instanceof DCLStatement) {

            /**
             * ???????????????????????? DCLStatement ???????????? {@link #getDCLRoutingEngine(ShardingRule, SQLStatementContext, ShardingSphereMetaData)}
             */
            return getDCLRoutingEngine(shardingRule, sqlStatementContext, metaData);
        }

        //???????????????
        if (shardingRule.isAllInDefaultDataSource(tableNames)) {
            return new DefaultDatabaseRoutingEngine(shardingRule, tableNames);
        }

        //????????????
        if (shardingRule.isAllBroadcastTables(tableNames)) {
            return sqlStatement instanceof SelectStatement ? new UnicastRoutingEngine(shardingRule, tableNames) : new DatabaseBroadcastRoutingEngine(shardingRule);
        }
        if (sqlStatementContext.getSqlStatement() instanceof DMLStatement && tableNames.isEmpty() && shardingRule.hasDefaultDataSourceName()) {
            return new DefaultDatabaseRoutingEngine(shardingRule, tableNames);
        }

        //????????????
        if (sqlStatementContext.getSqlStatement() instanceof DMLStatement && shardingConditions.isAlwaysFalse() || tableNames.isEmpty() || !shardingRule.tableRuleExists(tableNames)) {

            /**
             * ????????? {@link UnicastRoutingEngine#route()}
             */
            return new UnicastRoutingEngine(shardingRule, tableNames);
        }

        /**
         *  ?????????????????? {@link #getShardingRoutingEngine(ShardingRule, SQLStatementContext, ShardingConditions, Collection)}
         */
        return getShardingRoutingEngine(shardingRule, sqlStatementContext, shardingConditions, tableNames);
    }
    
    private static RoutingEngine getDALRoutingEngine(final ShardingRule shardingRule, final SQLStatement sqlStatement, final Collection<String> tableNames) {

        //?????????Use???????????????????????????
        if (sqlStatement instanceof UseStatement) {
            return new IgnoreRoutingEngine();
        }

        //?????????Set???ResetParameter????????????????????????????????????
        if (sqlStatement instanceof SetStatement || sqlStatement instanceof ResetParameterStatement || sqlStatement instanceof ShowDatabasesStatement) {
            return new DatabaseBroadcastRoutingEngine(shardingRule);
        }

        // ????????????????????????????????????????????????????????????
        if (!tableNames.isEmpty() && !shardingRule.tableRuleExists(tableNames) && shardingRule.hasDefaultDataSourceName()) {
            return new DefaultDatabaseRoutingEngine(shardingRule, tableNames);
        }

        // ????????????????????????????????????????????????
        if (!tableNames.isEmpty()) {

            // d
            return new UnicastRoutingEngine(shardingRule, tableNames);
        }
        return new DataSourceGroupBroadcastRoutingEngine(shardingRule);
    }
    
    private static RoutingEngine getDCLRoutingEngine(final ShardingRule shardingRule, final SQLStatementContext sqlStatementContext, final ShardingSphereMetaData metaData) {

        /**
         *  ??????????????? {@link MasterInstanceBroadcastRoutingEngine
         */
        return isGrantForSingleTable(sqlStatementContext)
                ? new TableBroadcastRoutingEngine(shardingRule, metaData.getTables(), sqlStatementContext) : new MasterInstanceBroadcastRoutingEngine(shardingRule, metaData.getDataSources());
    }
    
    private static boolean isGrantForSingleTable(final SQLStatementContext sqlStatementContext) {
        return !sqlStatementContext.getTablesContext().isEmpty() && !"*".equals(sqlStatementContext.getTablesContext().getSingleTableName());
    }


    /**
     * ????????????
     */
    private static RoutingEngine getShardingRoutingEngine(final ShardingRule shardingRule, final SQLStatementContext sqlStatementContext,
                                                          final ShardingConditions shardingConditions, final Collection<String> tableNames) {

        // ?????????????????????????????????
        Collection<String> shardingTableNames = shardingRule.getShardingLogicTableNames(tableNames);

        /**
         * ?????????????????????????????????????????????????????? '???????????????'???????????? StandardRoutingEngine
         *
         *   ???????????????????????? {@link ShardingRule#isAllBindingTables(Collection)}
         */
        if (1 == shardingTableNames.size() || shardingRule.isAllBindingTables(shardingTableNames)) {
            return new StandardRoutingEngine(shardingRule, shardingTableNames.iterator().next(), sqlStatementContext, shardingConditions);
        }
        // TODO config for cartesian set

        // ???????????? ComplexRoutingEngine
        return new ComplexRoutingEngine(shardingRule, tableNames, sqlStatementContext, shardingConditions);
    }
}
