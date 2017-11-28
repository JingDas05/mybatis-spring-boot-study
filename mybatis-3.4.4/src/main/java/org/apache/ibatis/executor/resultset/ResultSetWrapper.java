/**
 *    Copyright 2009-2016 the original author or authors.
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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.ObjectTypeHandler;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.apache.ibatis.type.UnknownTypeHandler;

/**
 * @author Iwao AVE!
 */
public class ResultSetWrapper {

  // 底层封装的ResultSet对象
  private final ResultSet resultSet;
  private final TypeHandlerRegistry typeHandlerRegistry;
  // 记录了ResultSet中每列的列名
  private final List<String> columnNames = new ArrayList<String>();
  // 记录了ResultSet中每列对应的java类型
  private final List<String> classNames = new ArrayList<String>();
  // 记录了ResultSet中每列对应的JdbcType
  private final List<JdbcType> jdbcTypes = new ArrayList<JdbcType>();
  // 记录了ResultSet中每列对应的TypeHandler对象，key是列名，value是TypeHandler集合
  private final Map<String, Map<Class<?>, TypeHandler<?>>> typeHandlerMap = new HashMap<String, Map<Class<?>, TypeHandler<?>>>();
  // 记录了被映射的列名，其中key是 ResultMap 对象的id,value是该ResultMap对象映射的列名集合
  private Map<String, List<String>> mappedColumnNamesMap = new HashMap<String, List<String>>();
  // 记录了未映射的列名，其中key是 ResultMap 对象的id,value是该ResultMap对象映射的列名集合
  private Map<String, List<String>> unMappedColumnNamesMap = new HashMap<String, List<String>>();

  public ResultSetWrapper(ResultSet rs, Configuration configuration) throws SQLException {
    super();
    this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
    this.resultSet = rs;
    final ResultSetMetaData metaData = rs.getMetaData();
    final int columnCount = metaData.getColumnCount();
    // 初始化 columnNames， classNames， jdbcTypes
    for (int i = 1; i <= columnCount; i++) {
//      记录了ResultSet中每列的列名，jdbcTypes以及所属class名字
      columnNames.add(configuration.isUseColumnLabel() ? metaData.getColumnLabel(i) : metaData.getColumnName(i));
      jdbcTypes.add(JdbcType.forCode(metaData.getColumnType(i)));
      classNames.add(metaData.getColumnClassName(i));
    }
  }

  public ResultSet getResultSet() {
    return resultSet;
  }

  public List<String> getColumnNames() {
    return this.columnNames;
  }

  // 返回的是不可修改的List<String>
  public List<String> getClassNames() {
    return Collections.unmodifiableList(classNames);
  }

  public JdbcType getJdbcType(String columnName) {
    for (int i = 0 ; i < columnNames.size(); i++) {
      // 循环遍历列名数组，根据列名获取jdbcType
      if (columnNames.get(i).equalsIgnoreCase(columnName)) {
        return jdbcTypes.get(i);
      }
    }
    return null;
  }

  /**
   *
   * 获取读取结果集时的类型处理器
   * 首先从尝试从TypeHandlerRegistry中获取，如果没有发现，获取列的jdbcType然后获取对应的处理器
   *
   * Gets the type handler to use when reading the result set.
   * Tries to get from the TypeHandlerRegistry by searching for the property type.
   * If not found it gets the column JDBC type and tries to get a handler for it.
   * 
   * @param propertyType
   * @param columnName
   * @return
   */
  public TypeHandler<?> getTypeHandler(Class<?> propertyType, String columnName) {
    TypeHandler<?> handler = null;
    Map<Class<?>, TypeHandler<?>> columnHandlers = typeHandlerMap.get(columnName);
    if (columnHandlers == null) {
      // 如果不存在初始化Map<Class<?>, TypeHandler<?>>
      // 注意这个地方，先将空的引用columnHandlers，放置到typeHandlerMap中，之后再进行赋值，可以的！
      columnHandlers = new HashMap<Class<?>, TypeHandler<?>>();
      typeHandlerMap.put(columnName, columnHandlers);
    } else {
      // 如果存在的话，就从columnHandlers中获取
      handler = columnHandlers.get(propertyType);
    }
    // 如果上面没有获取到handler，进行进一步处理
    if (handler == null) {
      JdbcType jdbcType = getJdbcType(columnName);
      handler = typeHandlerRegistry.getTypeHandler(propertyType, jdbcType);
      // Replicate logic of UnknownTypeHandler#resolveTypeHandler
      // See issue #59 comment 10
      if (handler == null || handler instanceof UnknownTypeHandler) {
        final int index = columnNames.indexOf(columnName);
        // 获取列对应的Class类型
        final Class<?> javaType = resolveClass(classNames.get(index));
        // 根据 javaType jdbcType继续获取
        if (javaType != null && jdbcType != null) {
          handler = typeHandlerRegistry.getTypeHandler(javaType, jdbcType);
        } else if (javaType != null) {
          handler = typeHandlerRegistry.getTypeHandler(javaType);
        } else if (jdbcType != null) {
          handler = typeHandlerRegistry.getTypeHandler(jdbcType);
        }
      }
      // 如果还没有获取到，就用 ObjectTypeHandler
      if (handler == null || handler instanceof UnknownTypeHandler) {
        handler = new ObjectTypeHandler();
      }
      columnHandlers.put(propertyType, handler);
    }
    return handler;
  }

  private Class<?> resolveClass(String className) {
    try {
      // #699 className could be null
      if (className != null) {
        return Resources.classForName(className);
      }
    } catch (ClassNotFoundException e) {
      // ignore
    }
    return null;
  }

  private void loadMappedAndUnmappedColumnNames(ResultMap resultMap, String columnPrefix) throws SQLException {
    List<String> mappedColumnNames = new ArrayList<String>();
    List<String> unmappedColumnNames = new ArrayList<String>();
    // 大写化列前缀，如果为null,就是null
    final String upperColumnPrefix = columnPrefix == null ? null : columnPrefix.toUpperCase(Locale.ENGLISH);
    // 将columnNames中的所有列名加上前缀，之后返回，得到实际映射的列名
    // 如果columnNames或者 prefix为空的话就原封不动返回columnNames
    final Set<String> mappedColumns = prependPrefixes(resultMap.getMappedColumns(), upperColumnPrefix);
    // 遍历resultSet中的所有列名,将数据库和 <resultMap>中的列进行比较，<resultMap>有的记录到mappedColumnNames
    // <resultMap> 没有的 记录到 unmappedColumnNames
    for (String columnName : columnNames) {
      final String upperColumnName = columnName.toUpperCase(Locale.ENGLISH);
      if (mappedColumns.contains(upperColumnName)) {
        // 记录映射的列名
        mappedColumnNames.add(upperColumnName);
      } else {
        // 记录未映射的列名
        unmappedColumnNames.add(columnName);
      }
    }
    // 将ResultMap的Id和列前缀组成的key,将ResultMap映射的列名以及未映射的列名保存到 mappedColumnNamesMap unMappedColumnNamesMap中
    // mappedColumnNamesMap unMappedColumnNamesMap会存储各个 <resultMap>id的映射和未映射的列名集合
    mappedColumnNamesMap.put(getMapKey(resultMap, columnPrefix), mappedColumnNames);
    unMappedColumnNamesMap.put(getMapKey(resultMap, columnPrefix), unmappedColumnNames);
  }

  // 获取已映射的列名集合，设计模式 亨元模式
  // 从mappedColumnNamesMap中获取mappedColumnNames，如果不存在调用loadMappedAndUnmappedColumnNames进行加载，之后再获取
  public List<String> getMappedColumnNames(ResultMap resultMap, String columnPrefix) throws SQLException {
    // 在mappedColumnNamesMap集合中查找被映射的列名，其中key是ResultMap的id与前缀组成
    List<String> mappedColumnNames = mappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
    if (mappedColumnNames == null) {
      // 如果未找到，则加载后存入到 mappedColumnNamesMap 集合中
      loadMappedAndUnmappedColumnNames(resultMap, columnPrefix);
      mappedColumnNames = mappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
    }
    return mappedColumnNames;
  }

  // 此处逻辑与上面方法类似
  public List<String> getUnmappedColumnNames(ResultMap resultMap, String columnPrefix) throws SQLException {
    List<String> unMappedColumnNames = unMappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
    if (unMappedColumnNames == null) {
      loadMappedAndUnmappedColumnNames(resultMap, columnPrefix);
      unMappedColumnNames = unMappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
    }
    return unMappedColumnNames;
  }

  // 构建MapKey， resultMapId:columnPrefix
  private String getMapKey(ResultMap resultMap, String columnPrefix) {
    return resultMap.getId() + ":" + columnPrefix;
  }

  // 将columnNames中的所有列名加上前缀，之后返回
  // 如果columnNames或者 prefix为空的话就原封不动返回columnNames
  private Set<String> prependPrefixes(Set<String> columnNames, String prefix) {
    if (columnNames == null || columnNames.isEmpty() || prefix == null || prefix.length() == 0) {
      return columnNames;
    }
    final Set<String> prefixed = new HashSet<String>();
    for (String columnName : columnNames) {
      prefixed.add(prefix + columnName);
    }
    return prefixed;
  }
  
}
