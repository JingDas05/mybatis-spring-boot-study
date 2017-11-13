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
package org.apache.ibatis.scripting.xmltags;

import java.util.Map;

import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

/**
 * @author Clinton Begin
 */
public class DynamicSqlSource implements SqlSource {

  private Configuration configuration;
  private SqlNode rootSqlNode;

  public DynamicSqlSource(Configuration configuration, SqlNode rootSqlNode) {
    this.configuration = configuration;
    this.rootSqlNode = rootSqlNode;
  }

  // 通过传入的具体参数 parameterObject，获取DynamicContext
  @Override
  public BoundSql getBoundSql(Object parameterObject) {
    // 初始化context对象，用于存放参数，以及中间值，parameterObject 是用户传入的实参
    DynamicContext context = new DynamicContext(configuration, parameterObject);
    // 解析，将sqlNode的apply()方法解析得到的SQL语句片段追加到context中，
    // 调用rootSqlNode.apply()方法调用整个树状结构中全部的sqlNode.apply()方法，最终通过context.getSql()得到完整SQL语句
    rootSqlNode.apply(context);
    // 建立 sqlSourceBuilder
    SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
    Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
    // 通过 sqlSourceBuilder 建立 sqlSource，并将SQL语句中的 “#{}” 占位符替换成 “？”占位符
    SqlSource sqlSource = sqlSourceParser.parse(context.getSql(), parameterType, context.getBindings());
    // 创建BoundSql对象，并将 DynamicContext.bindings中的参数信息复制到其AdditionalParameter保存
    BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
    for (Map.Entry<String, Object> entry : context.getBindings().entrySet()) {
      boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
    }
    return boundSql;
  }

}
