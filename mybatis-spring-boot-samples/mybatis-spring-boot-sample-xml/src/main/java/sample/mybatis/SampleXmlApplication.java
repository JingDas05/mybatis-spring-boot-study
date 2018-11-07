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
package sample.mybatis;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import sample.mybatis.domain.City;
import sample.mybatis.domain.CityExample;
import sample.mybatis.domain.HotelExample;
import sample.mybatis.mapper.CityMapper;
import sample.mybatis.mapper.HotelMapper;

import javax.annotation.Resource;
import javax.sql.DataSource;

@SpringBootApplication
@MapperScan("sample.mybatis.mapper")
public class SampleXmlApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(SampleXmlApplication.class, args);
    }

    @Resource
    private CityMapper cityMapper;

    @Resource
    private HotelMapper hotelMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public void run(String... args) throws Exception {
//        for (int i = 0; i < 50; i++) {
//            City city = new City();
//            city.setId(i);
//            city.setSubId(i);
//            city.setName("城市" + i);
//            city.setCountry("中国");
//            city.setState("省份" + i);
//            cityMapper.insert(city);
//        }
//        System.out.println(this.cityMapper.selectByPrimaryKey(1));
//        System.out.println(this.hotelMapper.selectByExample(new HotelExample()));
        System.out.println(this.cityMapper.selectByExample(new CityExample()).size());

        Environment environment = sqlSessionFactory.getConfiguration().getEnvironment();

        // 更换数据源进行查询
        DataSource dataSource1 = DataSourceBuilder.create()
                .driverClassName("com.mysql.jdbc.Driver")
                .username("root")
                .password("0109QWe")
                .url("jdbc:mysql://localhost:3306/mybatis2?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull")
                .build();
        Environment nextEnvironment = new Environment(environment.getId(), environment.getTransactionFactory(), dataSource1);
        sqlSessionFactory.getConfiguration().setEnvironment(nextEnvironment);
        System.out.println(this.cityMapper.selectByExample(new CityExample()).size());

    }
}
