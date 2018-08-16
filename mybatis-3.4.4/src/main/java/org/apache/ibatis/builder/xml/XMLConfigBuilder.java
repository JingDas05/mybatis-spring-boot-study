/**
 * Copyright 2009-2016 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.builder.xml;

import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;
import javax.sql.DataSource;

import org.apache.ibatis.builder.BaseBuilder;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.datasource.DataSourceFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.loader.ProxyFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.io.VFS;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.AutoMappingUnknownColumnBehavior;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.LocalCacheScope;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.type.JdbcType;

/**
 * @author Clinton Begin
 * @author Kazuki Shimizu
 */
public class XMLConfigBuilder extends BaseBuilder {

    // 表示是否已经解析过 mybatis-config.xml
    private boolean parsed;
    // 用于解析配置文件的XPathParser对象
    private XPathParser parser;
    // 标识<environment>配置的名称，默认读取<environment>标签的default属性
    private String environment;
    // 负责创建和缓存Reflector对象
    private ReflectorFactory localReflectorFactory = new DefaultReflectorFactory();

    public XMLConfigBuilder(Reader reader) {
        this(reader, null, null);
    }

    public XMLConfigBuilder(Reader reader, String environment) {
        this(reader, environment, null);
    }

    public XMLConfigBuilder(Reader reader, String environment, Properties props) {
        this(new XPathParser(reader, true, props, new XMLMapperEntityResolver()), environment, props);
    }

    public XMLConfigBuilder(InputStream inputStream) {
        this(inputStream, null, null);
    }

    public XMLConfigBuilder(InputStream inputStream, String environment) {
        this(inputStream, environment, null);
    }

    public XMLConfigBuilder(InputStream inputStream, String environment, Properties props) {
        this(new XPathParser(inputStream, true, props, new XMLMapperEntityResolver()), environment, props);
    }

    // xml

    // 解析的工作都是XPathParser做的
    // 私有方法，实际调用的方法

    // 这个地方实际需要的是XPathParser对象，XPathParser的初始化需要
    // InputStream inputStream, boolean validation, Properties variables, EntityResolver entityResolver
    private XMLConfigBuilder(XPathParser parser, String environment, Properties props) {
        // 初始化configuration, 因为这个时候是读取配置文件，所以 new Configuration()
        super(new Configuration());
        // 和 dubbo中的 RpcContext的用法差不多，很值得借鉴
        ErrorContext.instance().resource("SQL Mapper Configuration");
        this.configuration.setVariables(props);
        this.parsed = false;
        this.environment = environment;
        this.parser = parser;
    }

    public Configuration parse() {
        if (parsed) {
            throw new BuilderException("Each XMLConfigBuilder can only be used once.");
        }
        parsed = true;
        // 这个地方用的是 XPathParser的 api,解析根节点<configuration>,并开始解析
        parseConfiguration(parser.evalNode("/configuration"));
        return configuration;
    }

    private void parseConfiguration(XNode root) {
        try {
            //issue #117 先读取properties
            // 解析<properties>节点
            propertiesElement(root.evalNode("properties"));
            // 解析<settings>节点
            Properties settings = settingsAsProperties(root.evalNode("settings"));
            // 设置vfsImpl字段
            loadCustomVfs(settings);
            // 解析<typeAliases>节点
            typeAliasesElement(root.evalNode("typeAliases"));
            // 解析<plugins>节点
            pluginElement(root.evalNode("plugins"));
            // 解析<objectFactory>节点
            objectFactoryElement(root.evalNode("objectFactory"));
            // 解析<objectWrapperFactory>节点
            objectWrapperFactoryElement(root.evalNode("objectWrapperFactory"));
            // 解析<reflectorFactory>节点
            reflectorFactoryElement(root.evalNode("reflectorFactory"));
            // 将settings值设置到configuration中
            settingsElement(settings);
            // read it after objectFactory and objectWrapperFactory issue #631
            // 解析<environments>节点
            environmentsElement(root.evalNode("environments"));
            // 解析<databaseIdProvider>节点
            databaseIdProviderElement(root.evalNode("databaseIdProvider"));
            // 解析<typeHandlers>节点
            typeHandlerElement(root.evalNode("typeHandlers"));
            // 解析<mappers>节点
            mapperElement(root.evalNode("mappers"));
        } catch (Exception e) {
            throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
        }
    }

    private Properties settingsAsProperties(XNode context) {
        if (context == null) {
            return new Properties();
        }
        Properties props = context.getChildrenAsProperties();
        // Check that all settings are known to the configuration class
        // 创建 Configuration 对应的 MetaClass 对象
        MetaClass metaConfig = MetaClass.forClass(Configuration.class, localReflectorFactory);
        // 检测 Configuration是否包含了定义key指定属性相应的setter方法
        for (Object key : props.keySet()) {
            if (!metaConfig.hasSetter(String.valueOf(key))) {
                throw new BuilderException("The setting " + key + " is not known.  Make sure you spelled it correctly (case sensitive).");
            }
        }
        return props;
    }

    private void loadCustomVfs(Properties props) throws ClassNotFoundException {
        String value = props.getProperty("vfsImpl");
        if (value != null) {
            String[] clazzes = value.split(",");
            for (String clazz : clazzes) {
                if (!clazz.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Class<? extends VFS> vfsImpl = (Class<? extends VFS>) Resources.classForName(clazz);
                    // 全局配置中添加自定义VFS实现
                    configuration.setVfsImpl(vfsImpl);
                }
            }
        }
    }

    private void typeAliasesElement(XNode parent) {
        if (parent != null) {
            for (XNode child : parent.getChildren()) {
                //处理package节点
                if ("package".equals(child.getName())) {
                    String typeAliasPackage = child.getStringAttribute("name");
                    // 扫描指定包中所有类，并解析@Alias注解，完成别名注册
                    configuration.getTypeAliasRegistry().registerAliases(typeAliasPackage);
                } else {
                    // 获取指定别名
                    String alias = child.getStringAttribute("alias");
                    // 获取别名对应的类型
                    String type = child.getStringAttribute("type");
                    try {
                        Class<?> clazz = Resources.classForName(type);
                        if (alias == null) {
                            typeAliasRegistry.registerAlias(clazz);
                        } else {
                            typeAliasRegistry.registerAlias(alias, clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        throw new BuilderException("Error registering typeAlias for '" + alias + "'. Cause: " + e, e);
                    }
                }
            }
        }
    }

    private void pluginElement(XNode parent) throws Exception {
        if (parent != null) {
            for (XNode child : parent.getChildren()) {
                String interceptor = child.getStringAttribute("interceptor");
                Properties properties = child.getChildrenAsProperties();
                // 通过前面介绍的TypeAliasRegistry解析别名之后，实例化 Interceptor 对象
                Interceptor interceptorInstance = (Interceptor) resolveClass(interceptor).newInstance();
                interceptorInstance.setProperties(properties);
                // 记录 Interceptor
                configuration.addInterceptor(interceptorInstance);
            }
        }
    }

    private void objectFactoryElement(XNode context) throws Exception {
        if (context != null) {
            String type = context.getStringAttribute("type");
            Properties properties = context.getChildrenAsProperties();
            ObjectFactory factory = (ObjectFactory) resolveClass(type).newInstance();
            factory.setProperties(properties);
            //将自定义的objectFactory对象记录到 configuration 对象的 ObjectFactory字段中
            configuration.setObjectFactory(factory);
        }
    }

    private void objectWrapperFactoryElement(XNode context) throws Exception {
        if (context != null) {
            String type = context.getStringAttribute("type");
            ObjectWrapperFactory factory = (ObjectWrapperFactory) resolveClass(type).newInstance();
            configuration.setObjectWrapperFactory(factory);
        }
    }

    private void reflectorFactoryElement(XNode context) throws Exception {
        if (context != null) {
            String type = context.getStringAttribute("type");
            ReflectorFactory factory = (ReflectorFactory) resolveClass(type).newInstance();
            configuration.setReflectorFactory(factory);
        }
    }

    private void propertiesElement(XNode context) throws Exception {
        // context 就是root节点
        if (context != null) {
            Properties defaults = context.getChildrenAsProperties();
            //如果url 和 resource存在那么就引入url 或者 resource指向的properties，url和resource只能存在一个
            String resource = context.getStringAttribute("resource");
            String url = context.getStringAttribute("url");
            if (resource != null && url != null) {
                throw new BuilderException("The properties element cannot specify both a URL and a resource based property file reference.  Please specify one or the other.");
            }
            if (resource != null) {
                defaults.putAll(Resources.getResourceAsProperties(resource));
            } else if (url != null) {
                defaults.putAll(Resources.getUrlAsProperties(url));
            }
            // 与configuration中的Variables合并
            Properties vars = configuration.getVariables();
            if (vars != null) {
                defaults.putAll(vars);
            }
            //全局变量和读取配置文件的变量汇总，放回到全局变量，并且更新 XPathParser的variables
            parser.setVariables(defaults);
            configuration.setVariables(defaults);
        }
    }

    private void settingsElement(Properties props) throws Exception {
        // 指定 MyBatis 应如何自动映射列到字段或属性。 NONE 表示取消自动映射；
        // PARTIAL 只会自动映射没有定义嵌套结果集映射的结果集。 FULL 会自动映射任意复杂的结果集（无论是否嵌套）。default PARTIAL
        configuration.setAutoMappingBehavior(AutoMappingBehavior.valueOf(props.getProperty("autoMappingBehavior", "PARTIAL")));
        // 指定发现自动映射目标未知列（或者未知属性类型）的行为。
        // NONE: 不做任何反应
        // WARNING: 输出提醒日志 ('org.apache.ibatis.session.AutoMappingUnknownColumnBehavior' 的日志等级必须设置为 WARN)
        // FAILING: 映射失败 (抛出 SqlSessionException)
        // default NONE
        configuration.setAutoMappingUnknownColumnBehavior(AutoMappingUnknownColumnBehavior.valueOf(props.getProperty("autoMappingUnknownColumnBehavior", "NONE")));
        // 该配置影响的所有映射器中配置的缓存的全局开关。
        configuration.setCacheEnabled(booleanValueOf(props.getProperty("cacheEnabled"), true));
        // 指定 Mybatis 创建具有延迟加载能力的对象所用到的代理工具。CGLIB | JAVASSIST	JAVASSIST (MyBatis 3.3 or above)
        configuration.setProxyFactory((ProxyFactory) createInstance(props.getProperty("proxyFactory")));
        configuration.setLazyLoadingEnabled(booleanValueOf(props.getProperty("lazyLoadingEnabled"), false));
        // 当开启时，任何方法的调用都会加载该对象的所有属性。否则，每个属性会按需加载，default false (true in ≤3.4.1)
        configuration.setAggressiveLazyLoading(booleanValueOf(props.getProperty("aggressiveLazyLoading"), false));
        // 是否允许单一语句返回多结果集
        configuration.setMultipleResultSetsEnabled(booleanValueOf(props.getProperty("multipleResultSetsEnabled"), true));
        // 使用列标签代替列名。不同的驱动在这方面会有不同的表现， 具体可参考相关驱动文档或通过测试这两种不同的模式来观察所用驱动的结果。default true
        configuration.setUseColumnLabel(booleanValueOf(props.getProperty("useColumnLabel"), true));
        // 允许 JDBC 支持自动生成主键，需要驱动兼容。 如果设置为 true 则这个设置强制使用自动生成主键，尽管一些驱动
        // 不能兼容但仍可正常工作（比如 Derby）。default false
        configuration.setUseGeneratedKeys(booleanValueOf(props.getProperty("useGeneratedKeys"), false));
        // 配置默认的执行器。SIMPLE 就是普通的执行器；REUSE 执行器会重用预处理语句（prepared statements）； BATCH 执行器将重用语句并执行批量更新。default SIMPLE
        configuration.setDefaultExecutorType(ExecutorType.valueOf(props.getProperty("defaultExecutorType", "SIMPLE")));
        // 设置超时时间，它决定驱动等待数据库响应的秒数
        configuration.setDefaultStatementTimeout(integerValueOf(props.getProperty("defaultStatementTimeout"), null));
        // 	为驱动的结果集获取数量（fetchSize）设置一个提示值。此参数只可以在查询设置中被覆盖。default not set
        configuration.setDefaultFetchSize(integerValueOf(props.getProperty("defaultFetchSize"), null));
        // 是否开启自动驼峰命名规则（camel case）映射，即从经典数据库列名 A_COLUMN 到经典 Java 属性名 aColumn 的类似映射。default false
        configuration.setMapUnderscoreToCamelCase(booleanValueOf(props.getProperty("mapUnderscoreToCamelCase"), false));
        // 允许在嵌套语句中使用分页（RowBounds）。 If allow, set the false.
        configuration.setSafeRowBoundsEnabled(booleanValueOf(props.getProperty("safeRowBoundsEnabled"), false));
        // MyBatis 利用本地缓存机制（Local Cache）防止循环引用（circular references）和加速重复嵌套查询。 默认值为 SESSION，
        // 这种情况下会缓存一个会话中执行的所有查询。 若设置值为 STATEMENT，本地会话仅用在语句执行上，对相同 SqlSession 的不同调用将不会共享数据。
        configuration.setLocalCacheScope(LocalCacheScope.valueOf(props.getProperty("localCacheScope", "SESSION")));
        // 当没有为参数提供特定的 JDBC 类型时，为空值指定 JDBC 类型。 某些驱动需要指定列的 JDBC 类型，多数情况直接用一般类型即可，比如 NULL、VARCHAR 或 OTHER。
        configuration.setJdbcTypeForNull(JdbcType.valueOf(props.getProperty("jdbcTypeForNull", "OTHER")));
        // 	指定哪个对象的方法触发一次延迟加载。
        configuration.setLazyLoadTriggerMethods(stringSetValueOf(props.getProperty("lazyLoadTriggerMethods"), "equals,clone,hashCode,toString"));
        //	允许在嵌套语句中使用分页（ResultHandler）。 If allow, set the false.
        configuration.setSafeResultHandlerEnabled(booleanValueOf(props.getProperty("safeResultHandlerEnabled"), true));
        // 指定动态 SQL 生成的默认语言。 default org.apache.ibatis.scripting.xmltags.XMLLanguageDriver
        configuration.setDefaultScriptingLanguage(resolveClass(props.getProperty("defaultScriptingLanguage")));
        // 指定当结果集中值为 null 的时候是否调用映射对象的 setter（map 对象时为 put）方法，这对于有 Map.keySet()
        // 依赖或 null 值初始化的时候是有用的。注意基本类型（int、boolean等）是不能设置成 null 的。
        configuration.setCallSettersOnNulls(booleanValueOf(props.getProperty("callSettersOnNulls"), false));
        // 允许使用方法签名中的名称作为语句参数名称。 为了使用该特性，你的工程必须采用Java 8编译，并且加上-parameters选项。（从3.4.1开始）
        configuration.setUseActualParamName(booleanValueOf(props.getProperty("useActualParamName"), true));
        // 当返回行的所有列都是空时，MyBatis默认返回null。 当开启这个设置时，MyBatis会返回一个空实例。
        // 请注意，它也适用于嵌套的结果集 (i.e. collectioin and association)。（从3.4.2开始）
        configuration.setReturnInstanceForEmptyRow(booleanValueOf(props.getProperty("returnInstanceForEmptyRow"), false));
        // 指定 MyBatis 增加到日志名称的前缀。
        configuration.setLogPrefix(props.getProperty("logPrefix"));
        @SuppressWarnings("unchecked")
        Class<? extends Log> logImpl = (Class<? extends Log>) resolveClass(props.getProperty("logImpl"));
        configuration.setLogImpl(logImpl);
        configuration.setConfigurationFactory(resolveClass(props.getProperty("configurationFactory")));
    }

    private void environmentsElement(XNode context) throws Exception {
        if (context != null) {
            if (environment == null) {
                // 获取root节点要读取的默认环境名字
                environment = context.getStringAttribute("default");
            }
            for (XNode child : context.getChildren()) {
                String id = child.getStringAttribute("id");
                //寻找指定的环境id
                if (isSpecifiedEnvironment(id)) {
                    // 解析transactionManager节点获取实例TransactionFactory
                    TransactionFactory txFactory = transactionManagerElement(child.evalNode("transactionManager"));
                    // 解析dataSource节点获取实例DataSourceFactory
                    DataSourceFactory dsFactory = dataSourceElement(child.evalNode("dataSource"));
                    DataSource dataSource = dsFactory.getDataSource();
                    // 建造者模式创建Environment，用上面的 id txFactory dataSource
                    Environment.Builder environmentBuilder = new Environment.Builder(id)
                            .transactionFactory(txFactory)
                            .dataSource(dataSource);
                    configuration.setEnvironment(environmentBuilder.build());
                }
            }
        }
    }

    private void databaseIdProviderElement(XNode context) throws Exception {
        DatabaseIdProvider databaseIdProvider = null;
        if (context != null) {
            String type = context.getStringAttribute("type");
            // awful patch to keep backward compatibility
            if ("VENDOR".equals(type)) {
                type = "DB_VENDOR";
            }
            Properties properties = context.getChildrenAsProperties();
            // 创建 DatabaseIdProvider 对象
            databaseIdProvider = (DatabaseIdProvider) resolveClass(type).newInstance();
            // 配置 DatabaseIdProvider， 完成初始化
            databaseIdProvider.setProperties(properties);
        }
        // 获取environment的作用就是获得dataSource
        Environment environment = configuration.getEnvironment();
        if (environment != null && databaseIdProvider != null) {
            String databaseId = databaseIdProvider.getDatabaseId(environment.getDataSource());
            configuration.setDatabaseId(databaseId);
        }
    }

    private TransactionFactory transactionManagerElement(XNode context) throws Exception {
        if (context != null) {
            String type = context.getStringAttribute("type");
            // 把标签中的 <properties></properties> 转换成Properties
            Properties props = context.getChildrenAsProperties();
            TransactionFactory factory = (TransactionFactory) resolveClass(type).newInstance();
            factory.setProperties(props);
            return factory;
        }
        // 如果context为空，抛出异常
        throw new BuilderException("Environment declaration requires a TransactionFactory.");
    }

    private DataSourceFactory dataSourceElement(XNode context) throws Exception {
        if (context != null) {
            String type = context.getStringAttribute("type");
            Properties props = context.getChildrenAsProperties();
            DataSourceFactory factory = (DataSourceFactory) resolveClass(type).newInstance();
            factory.setProperties(props);
            return factory;
        }
        throw new BuilderException("Environment declaration requires a DataSourceFactory.");
    }

    //读取typeHandlers节点，并且注册类型处理器
    private void typeHandlerElement(XNode parent) throws Exception {
        if (parent != null) {
            for (XNode child : parent.getChildren()) {
                //处理入参是包名的情况
                if ("package".equals(child.getName())) {
                    String typeHandlerPackage = child.getStringAttribute("name");
                    typeHandlerRegistry.register(typeHandlerPackage);
                } else {
                    String javaTypeName = child.getStringAttribute("javaType");
                    String jdbcTypeName = child.getStringAttribute("jdbcType");
                    String handlerTypeName = child.getStringAttribute("handler");
                    // resolveClass(), resolveJdbcType(), resolveClass()为父类baseBuilder中的方法，子类可以共用
                    Class<?> javaTypeClass = resolveClass(javaTypeName);
                    JdbcType jdbcType = resolveJdbcType(jdbcTypeName);
                    Class<?> typeHandlerClass = resolveClass(handlerTypeName);
                    if (javaTypeClass != null) {
                        if (jdbcType == null) {
                            typeHandlerRegistry.register(javaTypeClass, typeHandlerClass);
                        } else {
                            typeHandlerRegistry.register(javaTypeClass, jdbcType, typeHandlerClass);
                        }
                    } else {
                        typeHandlerRegistry.register(typeHandlerClass);
                    }
                }
            }
        }
    }

    //核心方法，注册mapper,果然重要的都放在最后
    private void mapperElement(XNode parent) throws Exception {
        if (parent != null) {
            // 处理 <mappers>的子节点
            for (XNode child : parent.getChildren()) {
                //处理路径为包名的逻辑，这个是注册包下所有的接口，不是解析xml文件
                if ("package".equals(child.getName())) {
                    String mapperPackage = child.getStringAttribute("name");
                    // 扫描指定的包，并向 MapperRegistry注册Mapper接口
                    configuration.addMappers(mapperPackage);
                } else {
                    // 获取<mapper>节点的 resource url mapperClass 属性，这三个属性互斥
                    String resource = child.getStringAttribute("resource");
                    String url = child.getStringAttribute("url");
                    String mapperClass = child.getStringAttribute("class");
                    if (resource != null && url == null && mapperClass == null) {
                        ErrorContext.instance().resource(resource);
                        InputStream inputStream = Resources.getResourceAsStream(resource);
                        XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
                        mapperParser.parse();
                    } else if (resource == null && url != null && mapperClass == null) {
                        ErrorContext.instance().resource(url);
                        InputStream inputStream = Resources.getUrlAsStream(url);
                        XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, url, configuration.getSqlFragments());
                        mapperParser.parse();
                    } else if (resource == null && url == null && mapperClass != null) {
                        Class<?> mapperInterface = Resources.classForName(mapperClass);
                        configuration.addMapper(mapperInterface);
                    } else {
                        throw new BuilderException("A mapper element may only specify a url, resource or class, but not more than one.");
                    }
                }
            }
        }
    }

    private boolean isSpecifiedEnvironment(String id) {
        if (environment == null) {
            throw new BuilderException("No environment specified.");
        } else if (id == null) {
            throw new BuilderException("Environment requires an id attribute.");
        } else if (environment.equals(id)) {
            return true;
        }
        return false;
    }

}
