package sample.mybatis.config;

import io.shardingsphere.core.api.ShardingDataSourceFactory;
import io.shardingsphere.core.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.api.config.TableRuleConfiguration;
import io.shardingsphere.core.api.config.strategy.InlineShardingStrategyConfiguration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author wusi
 * @version 2018/9/12.
 */
@Configuration
@EnableConfigurationProperties(MybatisProperties.class)
public class DataSourceConfig implements ResourceLoaderAware, EnvironmentAware {

    // 配置真实数据源
    private Map<String, DataSource> dataSourceMap = new HashMap<String, DataSource>();
    // mybatis配置类
    private MybatisProperties properties;
    private ResourceLoader resourceLoader;
    private Environment environment;

    private static final String DS_0 = "ds0";
    private static final String DS_1 = "ds1";
    private static final String DS_2 = "ds2";

    // 配置mybatis sqlSessionFactory
//    @Bean
//    @Primary
//    @Qualifier("shardDataSource")
//    public SqlSessionFactory sqlSessionFactory() throws Exception {
//        // 项目启动的时候初始化 SqlSessionFactoryBean，设置成员变量，这个成员变量是从yml等配置文件读来的，也就是spring boot
//        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
//        factory.setDataSource(shardingDataSourceInit());
//        factory.setVfs(SpringBootVFS.class);
//        factory.setConfigLocation(this.resourceLoader.getResource(environment.getProperty("mybatis.config-location")));
//        // springBoot配置完成后解析config.xml
//        return factory.getObject();
//    }

    @Bean
    @Primary
    @Qualifier("shardDataSource")
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        DataSource dataSource0 = DataSourceBuilder.create()
                .driverClassName("com.mysql.jdbc.Driver")
                .username("root")
                .password("0109QWe")
                .url("jdbc:mysql://localhost:3306/mybatis1?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull")
                .build();
        // 项目启动的时候初始化 SqlSessionFactoryBean，设置成员变量，这个成员变量是从yml等配置文件读来的，也就是spring boot
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource0);
        factory.setVfs(SpringBootVFS.class);
        factory.setConfigLocation(this.resourceLoader.getResource(environment.getProperty("mybatis.config-location")));
        // springBoot配置完成后解析config.xml
        return factory.getObject();
    }


    @Bean("shardDataSource")
    @Primary
    public DataSource shardingDataSourceInit() throws Exception {
        // 配置数据源
        DataSource dataSource2 = DataSourceBuilder.create()
                .driverClassName("com.mysql.jdbc.Driver")
                .username("root")
                .password("0109QWe")
                .url("jdbc:mysql://localhost:3306/mybatis3?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull")
                .build();
        dataSourceMap.put(DS_2, dataSource2);

        DataSource dataSource1 = DataSourceBuilder.create()
                .driverClassName("com.mysql.jdbc.Driver")
                .username("root")
                .password("0109QWe")
                .url("jdbc:mysql://localhost:3306/mybatis2?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull")
                .build();
        dataSourceMap.put(DS_1, dataSource1);

        DataSource dataSource0 = DataSourceBuilder.create()
                .driverClassName("com.mysql.jdbc.Driver")
                .username("root")
                .password("0109QWe")
                .url("jdbc:mysql://localhost:3306/mybatis1?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull")
                .build();
        dataSourceMap.put(DS_0, dataSource0);
        // 添加数据源
        // 配置分表策略
        TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
        orderTableRuleConfig.setLogicTable("t_city");
        orderTableRuleConfig.setActualDataNodes("ds${0..2}.t_city_${0..1}");
        // 分库策略
        orderTableRuleConfig.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration(
                "id",
                "ds${id % 3}"));
        orderTableRuleConfig.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration(
                "sub_id",
                "t_city_${sub_id % 2}"));

        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);

        return ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, new HashMap<String, Object>(), new Properties());
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public MybatisProperties getProperties() {
        return properties;
    }

    public void setProperties(MybatisProperties properties) {
        this.properties = properties;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
