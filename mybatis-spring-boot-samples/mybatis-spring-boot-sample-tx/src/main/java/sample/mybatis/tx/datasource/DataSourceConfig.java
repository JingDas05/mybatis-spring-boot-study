package sample.mybatis.tx.datasource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @author wusi
 * @version 2018/11/23.
 */
@Configuration
public class DataSourceConfig {

    @Resource
    private Environment env;
    private DataSource dataSource = null;

    @Bean(name = "txDatasource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.txdb")
    public DataSource initDataSource() {
        if (dataSource == null) {
            dataSource = DataSourceBuilder.create().build();
        }
        return dataSource;
    }

    // 可以多个事务管理器
    @Bean(name = "txDatasourceManager")
    public DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(initDataSource());
    }

    @Bean(name = "txSqlSessionFactory")
    @Primary
    public SqlSessionFactory initSqlSessionFactory() throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(initDataSource());
        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources(env.getProperty("mybatis.rightsdb.mapper-locations")));
        return sessionFactory.getObject();
    }

    @Bean(name = "txJdbcTemplate")
    public JdbcTemplate initJdbcTemplate() throws SQLException {
        return new JdbcTemplate(initDataSource());
    }

}
