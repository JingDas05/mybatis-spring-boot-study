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
package org.apache.ibatis.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.parsing.TokenHandler;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;

/**
 *
 * 主要是定义了一个ParameterMappingTokenHandler，解析 #{}的实际值
 *
 * @author Clinton Begin
 */
public class SqlSourceBuilder extends BaseBuilder {

  private static final String parameterProperties = "javaType,jdbcType,mode,numericScale,resultMap,typeHandler,jdbcTypeName";

  public SqlSourceBuilder(Configuration configuration) {
    super(configuration);
  }

  // 将#{} 解析成？ 例如select * from city where id = ?
  // 第一个参数是经过sqlNode.apply()方法处理后的语句
  // 第二个参数是用户传入的实参类型
  // 第三个参数记录了形参和实参的对应关系，实际上就是经过SqlNode.apply()方法处理后的 DynamicContext.bindings集合
  public SqlSource parse(String originalSql, Class<?> parameterType, Map<String, Object> additionalParameters) {
    // 它是解析#{}占位符中的参数属性以及替换占位符的核心
    ParameterMappingTokenHandler handler = new ParameterMappingTokenHandler(configuration, parameterType, additionalParameters);
    // 使用GenericTokenParser ParameterMappingTokenHandler 解析入参 #{}
    GenericTokenParser parser = new GenericTokenParser("#{", "}", handler);
    // parse后将 #{} 换成 ？
    String sql = parser.parse(originalSql);
    // 创建 StaticSqlSource 其中封装了占位符被替换成 “?”的SQL语句以及参数对应的 parameterMappings 集合
    return new StaticSqlSource(configuration, sql, handler.getParameterMappings());
  }

  private static class ParameterMappingTokenHandler extends BaseBuilder implements TokenHandler {

    // 参数映射列表
    private List<ParameterMapping> parameterMappings = new ArrayList<ParameterMapping>();
    // 参数类型
    private Class<?> parameterType;
    // DynamicContext.bindings集合对应的 metaObject对象
    private MetaObject metaParameters;

    public ParameterMappingTokenHandler(Configuration configuration, Class<?> parameterType, Map<String, Object> additionalParameters) {
      super(configuration);
      this.parameterType = parameterType;
      this.metaParameters = configuration.newMetaObject(additionalParameters);
    }

    public List<ParameterMapping> getParameterMappings() {
      return parameterMappings;
    }

    @Override
    public String handleToken(String content) {
      // 构建参数映射，并将解析得到的 ParameterMapping 对象添加到 parameterMappings集合中保存
      parameterMappings.add(buildParameterMapping(content));
      // parse后将 #{} 换成 ？
      return "?";
    }

    private ParameterMapping buildParameterMapping(String content) {
      // 解析参数属性，并形成Map eg: #{__frc_item_0, javaType=int, jdbcType=numeric, typeHandler=MyTypeHandler}
      // 它就会被解析如下Map {"property"："__frc_item_0". "jdbcType"："numeric"...}
      Map<String, String> propertiesMap = parseParameterMapping(content);
      // 获取参数名称
      String property = propertiesMap.get("property");
      // 确定参数的javaType属性
      Class<?> propertyType;
      if (metaParameters.hasGetter(property)) { // issue #448 get type from additional params
        propertyType = metaParameters.getGetterType(property);
      } else if (typeHandlerRegistry.hasTypeHandler(parameterType)) {
        propertyType = parameterType;
      } else if (JdbcType.CURSOR.name().equals(propertiesMap.get("jdbcType"))) {
        // 如果是游标的话 propertyType是ResultSet
        propertyType = java.sql.ResultSet.class;
      } else if (property != null) {
        MetaClass metaClass = MetaClass.forClass(parameterType, configuration.getReflectorFactory());
        if (metaClass.hasGetter(property)) {
          propertyType = metaClass.getGetterType(property);
        } else {
          propertyType = Object.class;
        }
      } else {
        propertyType = Object.class;
      }
      // 创建 ParameterMapping建造者，并设置 ParameterMapping 相关配置
      ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, property, propertyType);
      Class<?> javaType = propertyType;
      String typeHandlerAlias = null;
      for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
        String name = entry.getKey();
        String value = entry.getValue();
        if ("javaType".equals(name)) {
          javaType = resolveClass(value);
          builder.javaType(javaType);
        } else if ("jdbcType".equals(name)) {
          builder.jdbcType(resolveJdbcType(value));
        } else if ("mode".equals(name)) {
          builder.mode(resolveParameterMode(value));
        } else if ("numericScale".equals(name)) {
          builder.numericScale(Integer.valueOf(value));
        } else if ("resultMap".equals(name)) {
          builder.resultMapId(value);
        } else if ("typeHandler".equals(name)) {
          typeHandlerAlias = value;
        } else if ("jdbcTypeName".equals(name)) {
          builder.jdbcTypeName(value);
        } else if ("property".equals(name)) {
          // Do Nothing
        } else if ("expression".equals(name)) {
          // 现在还不支持 expression属性
          throw new BuilderException("Expression based parameters are not supported yet");
        } else {
          throw new BuilderException("An invalid property '" + name + "' was found in mapping #{" + content + "}.  Valid properties are " + parameterProperties);
        }
      }
      if (typeHandlerAlias != null) {
        builder.typeHandler(resolveTypeHandler(javaType, typeHandlerAlias));
      }
      // 创建 ParameterMapping对象，没有指定 TypeHandler则会在这里的build()方法中，根据javaType和jdbcType从TypeHandlerRegistry中获取对应的
      // TypeHandler对象
      return builder.build();
    }

    private Map<String, String> parseParameterMapping(String content) {
      try {
        return new ParameterExpression(content);
      } catch (BuilderException ex) {
        throw ex;
      } catch (Exception ex) {
        throw new BuilderException("Parsing error was found in mapping #{" + content + "}.  Check syntax #{property|(expression), var1=value1, var2=value2, ...} ", ex);
      }
    }
  }
}
