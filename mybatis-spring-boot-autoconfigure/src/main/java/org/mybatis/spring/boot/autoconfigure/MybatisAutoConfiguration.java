/**
 * Copyright 2015-2016 the original author or authors.
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
package org.mybatis.spring.boot.autoconfigure;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * {@link EnableAutoConfiguration Auto-Configuration} for Mybatis. Contributes a
 * {@link SqlSessionFactory} and a {@link SqlSessionTemplate}.
 * <p>
 * If {@link org.mybatis.spring.annotation.MapperScan} is used, or a
 * configuration file is specified as a property, those will be considered,
 * otherwise this auto-configuration will attempt to register mappers based on
 * the interface definitions in or under the root auto-configuration package.
 *
 * @author Eddú Meléndez
 * @author Josh Long
 * @author Kazuki Shimizu
 * @author Eduardo Macarrón
 */
@Configuration
// 依赖于 SqlSessionFactory.class, SqlSessionFactoryBean.class
@ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class})
@ConditionalOnBean(DataSource.class)
@EnableConfigurationProperties(MybatisProperties.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class MybatisAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MybatisAutoConfiguration.class);

    private final MybatisProperties properties;

    private final Interceptor[] interceptors;

    private final ResourceLoader resourceLoader;

    private final DatabaseIdProvider databaseIdProvider;

    // 项目启动的时候先读取yml等项目配置文件，封装成properties
    // 注意ObjectProvider接口的使用
    public MybatisAutoConfiguration(MybatisProperties properties,
                                    ObjectProvider<Interceptor[]> interceptorsProvider,
                                    ResourceLoader resourceLoader,
                                    ObjectProvider<DatabaseIdProvider> databaseIdProvider) {
        this.properties = properties;
        this.interceptors = interceptorsProvider.getIfAvailable();
        this.resourceLoader = resourceLoader;
        this.databaseIdProvider = databaseIdProvider.getIfAvailable();
    }

    @PostConstruct
    public void checkConfigFileExists() {
        if (this.properties.isCheckConfigLocation() && StringUtils.hasText(this.properties.getConfigLocation())) {
            // 获取配置文件资源
            Resource resource = this.resourceLoader.getResource(this.properties.getConfigLocation());
            Assert.state(resource.exists(), "Cannot find config location: " + resource
                    + " (please add config file or check your Mybatis configuration)");
        }
    }

    // 很重要的方法，读取yml初始化SqlSessionFactory, 采用的是默认实现类 DefaultSqlSessionFactory，
    // 在SqlSessionFactoryBuilder 中的 build(Configuration)
    @Bean
    @ConditionalOnMissingBean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        // 项目启动的时候初始化 SqlSessionFactoryBean，设置成员变量，这个成员变量是从yml等配置文件读来的，也就是spring boot
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setVfs(SpringBootVFS.class);
        //设置springBoot解析的配置,可以设置configLocation,也可以设置configuration,只能设置其一
        if (StringUtils.hasText(this.properties.getConfigLocation())) {
            // 通过资源加载器，加载配置文件
            factory.setConfigLocation(this.resourceLoader.getResource(this.properties.getConfigLocation()));
        }
        factory.setConfiguration(properties.getConfiguration());
        if (this.properties.getConfigurationProperties() != null) {
            factory.setConfigurationProperties(this.properties.getConfigurationProperties());
        }
        if (!ObjectUtils.isEmpty(this.interceptors)) {
            factory.setPlugins(this.interceptors);
        }
        if (this.databaseIdProvider != null) {
            factory.setDatabaseIdProvider(this.databaseIdProvider);
        }
        if (StringUtils.hasLength(this.properties.getTypeAliasesPackage())) {
            factory.setTypeAliasesPackage(this.properties.getTypeAliasesPackage());
        }
        if (StringUtils.hasLength(this.properties.getTypeHandlersPackage())) {
            factory.setTypeHandlersPackage(this.properties.getTypeHandlersPackage());
        }
        if (!ObjectUtils.isEmpty(this.properties.resolveMapperLocations())) {
            factory.setMapperLocations(this.properties.resolveMapperLocations());
        }
        // springBoot配置完成后解析config.xml
        return factory.getObject();
    }

    // 初始化 sqlSessionTemplate,自动注入sqlSessionFactory
    @Bean
    @ConditionalOnMissingBean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        ExecutorType executorType = this.properties.getExecutorType();
        if (executorType != null) {
            return new SqlSessionTemplate(sqlSessionFactory, executorType);
        } else {
            return new SqlSessionTemplate(sqlSessionFactory);
        }
    }

    /**
     * 如果没有@MapperScan注解，那么就扫描spring boot 启动入口的包的所有标注有@Mapper注解的类
     * <p>
     * This will just scan the same base package as Spring Boot does. If you want
     * more power, you can explicitly use
     * {@link org.mybatis.spring.annotation.MapperScan} but this will get typed
     * mappers working correctly, out-of-the-box, similar to using Spring Data JPA
     * repositories.
     */
    public static class AutoConfiguredMapperScannerRegistrar
            implements BeanFactoryAware, ImportBeanDefinitionRegistrar, ResourceLoaderAware {

        private BeanFactory beanFactory;

        private ResourceLoader resourceLoader;

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

            logger.debug("Searching for mappers annotated with @Mapper");

            ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);

            try {
                if (this.resourceLoader != null) {
                    scanner.setResourceLoader(this.resourceLoader);
                }

                // 获取包路径
                List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
                if (logger.isDebugEnabled()) {
                    for (String pkg : packages) {
                        logger.debug("Using auto-configuration base package '{}'", pkg);
                    }
                }

                scanner.setAnnotationClass(Mapper.class);
                scanner.registerFilters();
                scanner.doScan(StringUtils.toStringArray(packages));
            } catch (IllegalStateException ex) {
                logger.debug("Could not determine auto-configuration package, automatic mapper scanning disabled.", ex);
            }
        }

        @Override
        public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
            this.beanFactory = beanFactory;
        }

        @Override
        public void setResourceLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
        }
    }

    /**
     * 这个地方写的很不错
     * 如果标注了@MapperScan 的注解，将会生成 MapperFactoryBean，
     * 如果没有标注@MapperScan 也就是没有MapperFactoryBean的实例，就走下面的配置
     * <p>
     * {@link org.mybatis.spring.annotation.MapperScan} ultimately ends up
     * creating instances of {@link MapperFactoryBean}. If
     * {@link org.mybatis.spring.annotation.MapperScan} is used then this
     * auto-configuration is not needed. If it is _not_ used, however, then this
     * will bring in a bean registrar and automatically register components based
     * on the same component-scanning path as Spring Boot itself.
     */
    @Configuration
    // Import 引入配置，引入的配置在 spring的管理范围内
    @Import({AutoConfiguredMapperScannerRegistrar.class})
    @ConditionalOnMissingBean(MapperFactoryBean.class)
    public static class MapperScannerRegistrarNotFoundConfiguration {

        @PostConstruct
        public void afterPropertiesSet() {
            logger.debug("No {} found.", MapperFactoryBean.class.getName());
        }
    }

}
