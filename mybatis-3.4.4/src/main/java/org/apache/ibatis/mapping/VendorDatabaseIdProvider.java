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
package org.apache.ibatis.mapping;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

/**
 * Vendor DatabaseId provider
 * 
 * It returns database product name as a databaseId
 * If the user provides a properties it uses it to translate database product name
 * key="Microsoft SQL Server", value="ms" will return "ms" 
 * It can return null, if no database product name or 
 * a properties was specified and no translation was found 
 * 
 * @author Eduardo Macarron
 */
public class VendorDatabaseIdProvider implements DatabaseIdProvider {
  
  private static final Log log = LogFactory.getLog(VendorDatabaseIdProvider.class);

  private Properties properties;

  @Override
  public String getDatabaseId(DataSource dataSource) {
    if (dataSource == null) {
      throw new NullPointerException("dataSource cannot be null");
    }
    try {
      return getDatabaseName(dataSource);
    } catch (Exception e) {
      log.error("Could not get a databaseId from dataSource", e);
    }
    return null;
  }

  @Override
  public void setProperties(Properties p) {
    //注入Properties的引用，这个是配置文件里配置的数据库类型
    this.properties = p;
  }

  private String getDatabaseName(DataSource dataSource) throws SQLException {
    // 从dataBase MetaData中获取product名字
    String productName = getDatabaseProductName(dataSource);
    if (this.properties != null) {
      for (Map.Entry<Object, Object> property : properties.entrySet()) {
        //如果productName包含全局的properties里面已存在的key,那么就用全局properties里面的名字，否则返回null
        if (productName.contains((String) property.getKey())) {
          return (String) property.getValue();
        }
      }
      // 没有符合，返回null
      return null;
    }
    return productName;
  }

  //获取dataSource名字，新建Connection，从连接中获取MetaData
  private String getDatabaseProductName(DataSource dataSource) throws SQLException {
    Connection con = null;
    try {
      con = dataSource.getConnection();
      DatabaseMetaData metaData = con.getMetaData();
      return metaData.getDatabaseProductName();
    } finally {
      if (con != null) {
        try {
          con.close();
        } catch (SQLException e) {
          // ignored
        }
      }
    }
  }
  
}
