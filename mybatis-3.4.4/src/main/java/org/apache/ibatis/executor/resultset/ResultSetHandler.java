/**
 *    Copyright 2009-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.executor.resultset;

import org.apache.ibatis.cursor.Cursor;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author Clinton Begin
 */
public interface ResultSetHandler {

  // 处理除了下面两种情况的结果映射,处理结果集，生成相应的结果对象集合
  <E> List<E> handleResultSets(Statement stmt) throws SQLException;

  // 处理返回值类型为Cursor<T>
  <E> Cursor<E> handleCursorResultSets(Statement stmt) throws SQLException;

  // 存储过程处理出参
  void handleOutputParameters(CallableStatement cs) throws SQLException;

}
