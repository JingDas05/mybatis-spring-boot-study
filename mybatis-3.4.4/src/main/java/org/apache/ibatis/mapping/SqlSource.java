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

/**
 * Represents the content of a mapped statement read from an XML file or an annotation. 
 * It creates the SQL that will be passed to the database out of the input parameter received from the user.
 *
 * SqlSource Sql源接口，代表从xml文件或注解映射的sql内容，主要就是用于创建BoundSql，
 * 有实现类DynamicSqlSource(动态Sql源)，StaticSqlSource(静态Sql源)等
 *
 * @author Clinton Begin
 */
public interface SqlSource {

  // sqlSource存储了一条sql语句的所有信息，通用的，没有具体的值，这个地方入参，返回数据库
  // 需要的带有参数的sql语句
  BoundSql getBoundSql(Object parameterObject);

}
