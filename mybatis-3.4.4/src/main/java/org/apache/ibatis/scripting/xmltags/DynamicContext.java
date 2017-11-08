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

import java.util.HashMap;
import java.util.Map;

import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.OgnlRuntime;
import ognl.PropertyAccessor;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;

/**
 *
 * 记录解析动态SQL语句之后产生的SQL语句片段，用于记录动态SQL语句解析结果的容器
 *
 * @author Clinton Begin
 */
public class DynamicContext {

  public static final String PARAMETER_OBJECT_KEY = "_parameter";
  public static final String DATABASE_ID_KEY = "_databaseId";

  static {
    OgnlRuntime.setPropertyAccessor(ContextMap.class, new ContextAccessor());
  }

  // 参数上下文
  private final ContextMap bindings;
  // sqlNode解析动态SQL时，会将解析后的SQL语句片段添加到该属性中保存，最终拼凑出一条完整的SQL语句
  private final StringBuilder sqlBuilder = new StringBuilder();
  private int uniqueNumber = 0;

  // parameterObject 运行时用户传入的参数，其中包含了后续用于替换#{}占位符的实参
  public DynamicContext(Configuration configuration, Object parameterObject) {
    // 对于非Map类型的参数，会创建对应的 MetaObject 对象，并封装成 ContextMap对象
    if (parameterObject != null && !(parameterObject instanceof Map)) {
      MetaObject metaObject = configuration.newMetaObject(parameterObject);
      bindings = new ContextMap(metaObject);
    } else {
      bindings = new ContextMap(null);
    }
    // bindings赋值，_parameter 的值为parameterObject， _databaseId的值为configuration.getDatabaseId()
    // _parameter 在有的SqlNode实现中直接使用了该字面量
    bindings.put(PARAMETER_OBJECT_KEY, parameterObject);
    bindings.put(DATABASE_ID_KEY, configuration.getDatabaseId());
  }

  public Map<String, Object> getBindings() {
    return bindings;
  }

  public void bind(String name, Object value) {
    bindings.put(name, value);
  }

  // 追加 SQL 片段
  public void appendSql(String sql) {
    sqlBuilder.append(sql);
    sqlBuilder.append(" ");
  }

  //获取完整sql语句，去掉前后空格
  public String getSql() {
    return sqlBuilder.toString().trim();
  }

  // 获取自增number
  public int getUniqueNumber() {
    return uniqueNumber++;
  }


  static class ContextMap extends HashMap<String, Object> {
    private static final long serialVersionUID = 2977601501966151582L;

    // 成员变量的作用，如果contextMap找不到，那么从这个变量里找
    // 将用户传入的参数封装成MetaObject对象
    private MetaObject parameterMetaObject;
    public ContextMap(MetaObject parameterMetaObject) {
      this.parameterMetaObject = parameterMetaObject;
    }

    // 重写 get()方法
    @Override
    public Object get(Object key) {
      String strKey = (String) key;
      // 如果 ContextMap中包含了该key,则直接返回
      if (super.containsKey(strKey)) {
        return super.get(strKey);
      }

      // 从运行时参数中查找对应属性
      if (parameterMetaObject != null) {
        // issue #61 do not modify the context when reading
        return parameterMetaObject.getValue(strKey);
      }

      return null;
    }
  }

  // OgnlRuntime需要
  static class ContextAccessor implements PropertyAccessor {

    @Override
    public Object getProperty(Map context, Object target, Object name)
        throws OgnlException {
      Map map = (Map) target;

      // 从target map 中获取key为name的值
      Object result = map.get(name);
      if (map.containsKey(name) || result != null) {
        return result;
      }
      // 如果没获取到， 从target map 中获取 key 为_parameter的值，强转map, 再根据name获取
      Object parameterObject = map.get(PARAMETER_OBJECT_KEY);
      if (parameterObject instanceof Map) {
        return ((Map)parameterObject).get(name);
      }
      // 没找到返回空
      return null;
    }

    @Override
    public void setProperty(Map context, Object target, Object name, Object value)
        throws OgnlException {
      // 设置属性，key:name value:value to target
      Map<Object, Object> map = (Map<Object, Object>) target;
      map.put(name, value);
    }

    @Override
    public String getSourceAccessor(OgnlContext arg0, Object arg1, Object arg2) {
      return null;
    }

    @Override
    public String getSourceSetter(OgnlContext arg0, Object arg1, Object arg2) {
      return null;
    }
  }
}