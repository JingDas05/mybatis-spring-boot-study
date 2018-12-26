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

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import sample.mybatis.annotation.domain.CityExample;
import sample.mybatis.annotation.mapper.CityMapper;

import javax.annotation.Resource;

@SpringBootApplication
@MapperScan("sample.mybatis.annotation.mapper")
public class SampleMapperApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(SampleMapperApplication.class, args);
    }

    @Resource
    private CityMapper cityMapper;

    @Override
    public void run(String... args) throws Exception {
//        PageHelper.startPage(1, 10);
        Page<Object> objects = PageHelper.offsetPage(1, 10, false);
        CityExample cityExample = new CityExample();
        CityExample.Criteria criteria = cityExample.createCriteria();
        criteria.andCountryEqualTo("中国");
        System.out.println(this.cityMapper.selectByExample(cityExample));
    }
}
