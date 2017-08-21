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
package org.apache.ibatis.builder.xml.dynamic;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.scripting.xmltags.ChooseSqlNode;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.scripting.xmltags.IfSqlNode;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SetSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.TextSqlNode;
import org.apache.ibatis.scripting.xmltags.WhereSqlNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.Test;

public class DynamicSqlSourceTest extends BaseDataTest {

  // 验证简单的没有循环和条件的sql
  @Test
  public void shouldDemonstrateSimpleExpectedTextWithNoLoopsOrConditionals() throws Exception {
    final String expected = "SELECT * FROM BLOG";
    final MixedSqlNode sqlNode = mixedContents(new TextSqlNode(expected));
    DynamicSqlSource source = createDynamicSqlSource(sqlNode);
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  // 验证多部件的没有循环和条件的sql
  @Test
  public void shouldDemonstrateMultipartExpectedTextWithNoLoopsOrConditionals() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new TextSqlNode("WHERE ID = ?"));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldConditionallyIncludeWhere() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new IfSqlNode(mixedContents(new TextSqlNode("WHERE ID = ?")), "true"
        ));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldConditionallyExcludeWhere() throws Exception {
    final String expected = "SELECT * FROM BLOG";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new IfSqlNode(mixedContents(new TextSqlNode("WHERE ID = ?")), "false"
        ));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldConditionallyDefault() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE CATEGORY = 'DEFAULT'";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new ChooseSqlNode(new ArrayList<SqlNode>() {{
          add(new IfSqlNode(mixedContents(new TextSqlNode("WHERE CATEGORY = ?")), "false"
          ));
          add(new IfSqlNode(mixedContents(new TextSqlNode("WHERE CATEGORY = 'NONE'")), "false"
          ));
        }}, mixedContents(new TextSqlNode("WHERE CATEGORY = 'DEFAULT'"))));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldConditionallyChooseFirst() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE CATEGORY = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new ChooseSqlNode(new ArrayList<SqlNode>() {{
          add(new IfSqlNode(mixedContents(new TextSqlNode("WHERE CATEGORY = ?")), "true"
          ));
          add(new IfSqlNode(mixedContents(new TextSqlNode("WHERE CATEGORY = 'NONE'")), "false"
          ));
        }}, mixedContents(new TextSqlNode("WHERE CATEGORY = 'DEFAULT'"))));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldConditionallyChooseSecond() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE CATEGORY = 'NONE'";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new ChooseSqlNode(new ArrayList<SqlNode>() {{
          add(new IfSqlNode(mixedContents(new TextSqlNode("WHERE CATEGORY = ?")), "false"
          ));
          add(new IfSqlNode(mixedContents(new TextSqlNode("WHERE CATEGORY = 'NONE'")), "true"
          ));
        }}, mixedContents(new TextSqlNode("WHERE CATEGORY = 'DEFAULT'"))));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  // 去掉前缀是where 后面出现的 and or
  @Test
  public void shouldTrimWHEREInsteadOfANDForFirstCondition() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE  ID = ?";
    // 这个地方构建了DynamicSqlSource，核心是构建了 一个 MixedSqlNode，里面是嵌套的结构包含了
    // TextSqlNode 和 WhereSqlNode ，WhereSqlNode 包含了
        // IfSqlNode， IfSqlNode 包含了
            // TextSqlNode
            // 最开始会先调用外层sqlNode的apply()，也就是 MixedSqlNode 的 apply方法（）
            // MixedSqlNode 会循环调用包含sqlNode的方法，当调用 WhereSqlNode 的 apply 方法，会调用父类 TrimSqlNode的方法
            // 父类 TrimSqlNode的方法 会先执行自己的apply方法(装饰 context )，之后执行context装饰者 FilteredDynamicContext
            // 的applyAll方法去除前后缀等操作

    // 总结来说就是层层调用apply方法，添加到context中
    // 重点就是 TrimSqlNode中的 FilteredDynamicContext对象，加强 客户端 DynamicSqlSource 的 DynamicContext 的类，处理数据再
    // 保存到DynamicContext中
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new WhereSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode("   and ID = ?  ")), "true"
            ),
            new IfSqlNode(mixedContents(new TextSqlNode("   or NAME = ?  ")), "false"
            )
        )));
    // getBoundSql() 核心方法，调用 apply() 获取boundSql
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldTrimWHEREANDWithLFForFirstCondition() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE \n ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new WhereSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode("   and\n ID = ?  ")), "true"
                )
            )));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldTrimWHEREANDWithCRLFForFirstCondition() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE \r\n ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new WhereSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode("   and\r\n ID = ?  ")), "true"
                )
            )));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldTrimWHEREANDWithTABForFirstCondition() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE \t ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new WhereSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode("   and\t ID = ?  ")), "true"
                )
            )));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldTrimWHEREORWithLFForFirstCondition() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE \n ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new WhereSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode("   or\n ID = ?  ")), "true"
                )
            )));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldTrimWHEREORWithCRLFForFirstCondition() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE \r\n ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new WhereSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode("   or\r\n ID = ?  ")), "true"
                )
            )));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldTrimWHEREORWithTABForFirstCondition() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE \t ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new WhereSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode("   or\t ID = ?  ")), "true"
                )
            )));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldTrimWHEREInsteadOfORForSecondCondition() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE  NAME = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new WhereSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode("   and ID = ?  ")), "false"
            ),
            new IfSqlNode(mixedContents(new TextSqlNode("   or NAME = ?  ")), "true"
            )
        )));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldTrimWHEREInsteadOfANDForBothConditions() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE  ID = ?   OR NAME = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new WhereSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode("   and ID = ?   ")), "true"
            ),
            new IfSqlNode(mixedContents(new TextSqlNode("OR NAME = ?  ")), "true"
            )
        )));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldTrimNoWhereClause() throws Exception {
    final String expected = "SELECT * FROM BLOG";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new WhereSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode("   and ID = ?   ")), "false"
            ),
            new IfSqlNode(mixedContents(new TextSqlNode("OR NAME = ?  ")), "false"
            )
        )));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldTrimSETInsteadOfCOMMAForBothConditions() throws Exception {
    final String expected = "UPDATE BLOG SET ID = ?,  NAME = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("UPDATE BLOG"),
        new SetSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode(" ID = ?, ")), "true"
            ),
            new IfSqlNode(mixedContents(new TextSqlNode(" NAME = ?, ")), "true"
            )
        )));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldTrimNoSetClause() throws Exception {
    final String expected = "UPDATE BLOG";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("UPDATE BLOG"),
        new SetSqlNode(new Configuration(),mixedContents(
            new IfSqlNode(mixedContents(new TextSqlNode("   , ID = ?   ")), "false"
            ),
            new IfSqlNode(mixedContents(new TextSqlNode(", NAME = ?  ")), "false"
            )
        )));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldIterateOnceForEachItemInCollection() throws Exception {
    final HashMap<String, String[]> parameterObject = new HashMap<String, String[]>() {{
      put("array", new String[]{"one", "two", "three"});
    }};
    final String expected = "SELECT * FROM BLOG WHERE ID in (  one = ? AND two = ? AND three = ? )";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG WHERE ID in"),
        new ForEachSqlNode(new Configuration(),mixedContents(new TextSqlNode("${item} = #{item}")), "array", "index", "item", "(", ")", "AND"));
    BoundSql boundSql = source.getBoundSql(parameterObject);
    assertEquals(expected, boundSql.getSql());
    assertEquals(3, boundSql.getParameterMappings().size());
    assertEquals("__frch_item_0", boundSql.getParameterMappings().get(0).getProperty());
    assertEquals("__frch_item_1", boundSql.getParameterMappings().get(1).getProperty());
    assertEquals("__frch_item_2", boundSql.getParameterMappings().get(2).getProperty());
  }

  @Test
  public void shouldHandleOgnlExpression() throws Exception {
    final HashMap<String, String> parameterObject = new HashMap<String, String>() {{
      put("name", "Steve");
    }};
    final String expected = "Expression test: 3 / yes.";
    DynamicSqlSource source = createDynamicSqlSource(new TextSqlNode("Expression test: ${name.indexOf('v')} / ${name in {'Bob', 'Steve'\\} ? 'yes' : 'no'}."));
    BoundSql boundSql = source.getBoundSql(parameterObject);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldSkipForEachWhenCollectionIsEmpty() throws Exception {
    final HashMap<String, Integer[]> parameterObject = new HashMap<String, Integer[]>() {{
        put("array", new Integer[] {});
    }};
    final String expected = "SELECT * FROM BLOG";
    DynamicSqlSource source = createDynamicSqlSource(new TextSqlNode("SELECT * FROM BLOG"),
        new ForEachSqlNode(new Configuration(), mixedContents(
            new TextSqlNode("#{item}")), "array", null, "item", "WHERE id in (", ")", ","));
    BoundSql boundSql = source.getBoundSql(parameterObject);
    assertEquals(expected, boundSql.getSql());
    assertEquals(0, boundSql.getParameterMappings().size());
  }

  @Test
  public void shouldPerformStrictMatchOnForEachVariableSubstitution() throws Exception {
    final Map<String, Object> param = new HashMap<String, Object>();
    final Map<String, String> uuu = new HashMap<String, String>();
    uuu.put("u", "xyz");
    List<Bean> uuuu = new ArrayList<Bean>();
    uuuu.add(new Bean("bean id"));
    param.put("uuu", uuu);
    param.put("uuuu", uuuu);
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("INSERT INTO BLOG (ID, NAME, NOTE, COMMENT) VALUES"),
        new ForEachSqlNode(new Configuration(),mixedContents(
            new TextSqlNode("#{uuu.u}, #{u.id}, #{ u,typeHandler=org.apache.ibatis.type.StringTypeHandler},"
                + " #{u:VARCHAR,typeHandler=org.apache.ibatis.type.StringTypeHandler}")), "uuuu", "uu", "u", "(", ")", ","));
    BoundSql boundSql = source.getBoundSql(param);
    assertEquals(4, boundSql.getParameterMappings().size());
    assertEquals("uuu.u", boundSql.getParameterMappings().get(0).getProperty());
    assertEquals("__frch_u_0.id", boundSql.getParameterMappings().get(1).getProperty());
    assertEquals("__frch_u_0", boundSql.getParameterMappings().get(2).getProperty());
    assertEquals("__frch_u_0", boundSql.getParameterMappings().get(3).getProperty());
  }

  // 公用方法，创建动态sqlSource
  private DynamicSqlSource createDynamicSqlSource(SqlNode... contents) throws IOException, SQLException {
    createBlogDataSource();
    final String resource = "org/apache/ibatis/builder/MapperConfig.xml";
    final Reader reader = Resources.getResourceAsReader(resource);
    // SqlSessionFactory 这个是工厂类，用于生产 SqlSession
    SqlSessionFactory sqlMapper = new SqlSessionFactoryBuilder().build(reader);
    Configuration configuration = sqlMapper.getConfiguration();
    MixedSqlNode sqlNode = mixedContents(contents);
    // 创建动态sqlSource需要configuration对象, 以及嵌套的sqlNode
    return new DynamicSqlSource(configuration, sqlNode);
  }

  private MixedSqlNode mixedContents(SqlNode... contents) {
    return new MixedSqlNode(Arrays.asList(contents));
  }

  @Test
  public void shouldMapNullStringsToEmptyStrings() {
    final String expected = "id=${id}";
    final MixedSqlNode sqlNode = mixedContents(new TextSqlNode(expected));
    final DynamicSqlSource source = new DynamicSqlSource(new Configuration(), sqlNode);
    String sql = source.getBoundSql(new Bean(null)).getSql();
    Assert.assertEquals("id=", sql);
  }

  public static class Bean {
    public String id;
    public Bean(String property) {
      this.id = property;
    }
    public String getId() {
      return id;
    }
    public void setId(String property) {
      this.id = property;
    }
  }

}
