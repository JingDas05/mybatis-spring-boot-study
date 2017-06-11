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
package org.apache.ibatis.mapping;

import java.sql.ResultSet;

/**
 *
 * resultSet处理方式，详细介绍见 http://treegrowth.top/#/pages/detail/409815c996664e63a0e9b30428470171
 *
 * @author Clinton Begin
 */
public enum ResultSetType {
  //默认的cursor 类型，仅仅支持结果集forward ，不支持back， forward ，random ，last ，first 等操作。
  FORWARD_ONLY(ResultSet.TYPE_FORWARD_ONLY),
  //支持结果集backforward ，random ，last ，first 等操作，对其它session 对数据库中数据做出的更改是不敏感的。
  SCROLL_INSENSITIVE(ResultSet.TYPE_SCROLL_INSENSITIVE),
  //支持结果集backforward ，random ，last ，first 等操作，对其它session 对数据库中数据做出的更改是不敏感的。
  SCROLL_SENSITIVE(ResultSet.TYPE_SCROLL_SENSITIVE);

  private int value;

  ResultSetType(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
