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

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import sample.mybatis.tx.service.TxService;

import javax.annotation.Resource;

@MapperScan("sample.mybatis.tx.mapper")
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class TxApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(TxApplication.class, args);
    }

    @Resource
    private TxService txService;

    @Override
    public void run(String... args) throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
//                txService.txWithCode();
//        txService.txWithAnnotation();
            }
        }).start();
    }

}
