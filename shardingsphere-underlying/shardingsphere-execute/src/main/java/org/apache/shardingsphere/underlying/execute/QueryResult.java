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

package org.apache.shardingsphere.underlying.execute;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Calendar;

/**
 * Query result form SQL.
 *
 * @author zhangliang
 * @author yangyi
 *
 *  代表查询结果的接口
 */
public interface QueryResult {
    
    /**
     * iterate next data.
     *
     * @return has next data
     * @throws SQLException SQL Exception
     */
    boolean next() throws SQLException;
    
    /**
     * Get data value.
     *
     * @param columnIndex column index
     * @param type class type of data value
     * @return data value
     * @throws SQLException SQL Exception
     */
    Object getValue(int columnIndex, Class<?> type) throws SQLException;
    
    /**
     * Get calendar value.
     *
     * @param columnIndex column index
     * @param type class type of data value
     * @param calendar calendar
     * @return calendar value
     * @throws SQLException SQL Exception
     */
    Object getCalendarValue(int columnIndex, Class<?> type, Calendar calendar) throws SQLException;
    
    /**
     * Get InputStream.
     *
     * @param columnIndex column index
     * @param type class type of data value
     * @return InputStream
     * @throws SQLException SQL Exception
     */
    InputStream getInputStream(int columnIndex, String type) throws SQLException;
    
    /**
     * Judge ResultSet is null or not.
     *
     * @return ResultSet is null or not
     * @throws SQLException SQL Exception
     */
    boolean wasNull() throws SQLException;
    
    /**
     * Get column count.
     *
     * @return column count
     * @throws SQLException SQL Exception
     */
    int getColumnCount() throws SQLException;
    
    /**
     * Get column label.
     *
     * @param columnIndex column index
     * @return column label
     * @throws SQLException SQL Exception
     */
    String getColumnLabel(int columnIndex) throws SQLException;
    
    /**
     * Whether value is case sensitive or not.
     *
     * @param columnIndex column index
     * @return true if column is case sensitive, otherwise false
     * @throws SQLException SQL Exception
     */
    boolean isCaseSensitive(int columnIndex) throws SQLException;
}
