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
package org.apache.ibatis.datasource.jndi;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.datasource.DataSourceException;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

public class JndiDataSourceFactoryTest extends BaseDataTest {

  private static final String TEST_INITIAL_CONTEXT_FACTORY = MockContextFactory.class.getName();
  private static final String TEST_INITIAL_CONTEXT = "/mypath/path/";
  private static final String TEST_DATA_SOURCE = "myDataSource";
  private UnpooledDataSource expectedDataSource;

  @Before
  public void setup() throws Exception {
    // 调用 BaseDataTest 的方法createUnpooledDataSource(String resource)创建不是连接池的数据源
    expectedDataSource = createUnpooledDataSource(BLOG_PROPERTIES);
  }

  @Test
  public void shouldRetrieveDataSourceFromJNDI() throws Exception {
    createJndiDataSource();
    JndiDataSourceFactory factory = new JndiDataSourceFactory();
    factory.setProperties(new Properties() {
      {
        // key 为 env.java.naming.factory.initial, value 为org.apache.ibatis.datasource.jndi.JndiDataSourceFactoryTest$MockContextFactory
        // 注意这个地方是内部类
        setProperty(JndiDataSourceFactory.ENV_PREFIX + Context.INITIAL_CONTEXT_FACTORY, TEST_INITIAL_CONTEXT_FACTORY);
        // key 为 initial_context， value 为 /mypath/path/
        setProperty(JndiDataSourceFactory.INITIAL_CONTEXT, TEST_INITIAL_CONTEXT);
        // key 为 data_source， value 为 myDataSource
        setProperty(JndiDataSourceFactory.DATA_SOURCE, TEST_DATA_SOURCE);
      }
    });
    DataSource actualDataSource = factory.getDataSource();
    assertEquals(expectedDataSource, actualDataSource);
  }

  private void createJndiDataSource() throws Exception {
    try {
      // 环境配置，key 为 java.naming.factory.initial， value
      // 为org.apache.ibatis.datasource.jndi.JndiDataSourceFactoryTest$MockContextFactory
      Hashtable<String, String> env = new Hashtable<String, String>();
      env.put(Context.INITIAL_CONTEXT_FACTORY, TEST_INITIAL_CONTEXT_FACTORY);

      // 新建模拟环境，并且绑定expectedDataSource数据源，key是myDataSource
      MockContext ctx = new MockContext(false);
      ctx.bind(TEST_DATA_SOURCE, expectedDataSource);

      // 初始化环境，绑定上面的模拟环境，key为/mypath/path/
      InitialContext initCtx = new InitialContext(env);
      initCtx.bind(TEST_INITIAL_CONTEXT, ctx);
    } catch (NamingException e) {
      throw new DataSourceException("There was an error configuring JndiDataSourceTransactionPool. Cause: " + e, e);
    }
  }

  // 定义了一个模拟的MockContextFactory，实现getInitialContext(Hashtable<?, ?> environment)方法
  public static class MockContextFactory implements InitialContextFactory {
    @Override
    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
      return new MockContext(false);
    }
  }

  //  定义了一个模拟的 MockContext
  public static class MockContext extends InitialContext {
    private static Map<String,Object> bindings = new HashMap<String,Object>();

    public MockContext(boolean lazy) throws NamingException {
      super(lazy);
    }

    @Override
    public Object lookup(String name) throws NamingException {
      return bindings.get(name);
    }

    @Override
    public void bind(String name, Object obj) throws NamingException {
      bindings.put(name, obj);
    }
  }


}
