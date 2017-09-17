/**
 *    Copyright 2009-2015 the original author or authors.
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
package org.apache.ibatis.session;

import java.sql.Connection;

/**
 * @author Clinton Begin
 */
public enum TransactionIsolationLevel {
  // 没有事务
  NONE(Connection.TRANSACTION_NONE),
  // 当前会话只能读取到其他事务提交的数据，未提交的数据读不到。同一个事务中，读取到两次不同的结果。这就造成了不可重复读
  READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
  // 即便是事务没有commit，但是我们仍然能读到未提交的数据，这是所有隔离级别中最低的一种。脏读
  READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
  // 当前会话可以重复读，就是每次读取的结果集都相同，而不管其他事务有没有提交。幻读,
  // 其他事务提交的数据，不能再提交
  REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
  // 隔离级别设置为serializable的时候，其他会话对该表的写操作将被挂起。可以看到，这是隔离级别中最严格的，
  // 但是这样做势必对性能造成影响。
  SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

  private final int level;

  private TransactionIsolationLevel(int level) {
    this.level = level;
  }

  public int getLevel() {
    return level;
  }
}
