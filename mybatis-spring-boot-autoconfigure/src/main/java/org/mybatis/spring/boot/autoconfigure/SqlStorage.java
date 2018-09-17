package org.mybatis.spring.boot.autoconfigure;

import org.apache.ibatis.mapping.MappedStatement;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;


/**
 * 打印sql的工具，待完成
 *
 * @author wusi
 * @version 2018/8/28.
 */
@Configuration
@ConditionalOnBean(SqlSessionTemplate.class)
public class SqlStorage {

    @Resource
    private SqlSessionTemplate sessionTemplate;

    @PostConstruct
    public void writeSql() {
//        String FILE_NAME = "sql.txt";
//        Collection<MappedStatement> mappedStatements = sessionTemplate.getConfiguration().getMappedStatements();
//        Set<MappedStatement> mappedStatementsSet = new HashSet<>(mappedStatements);
//        try {
//            BufferedWriter out = new BufferedWriter(new FileWriter(FILE_NAME));
//            if (sessionTemplate != null && !CollectionUtils.isEmpty(mappedStatementsSet)) {
//                for (MappedStatement mappedStatement : mappedStatementsSet) {
//                    out.write(mappedStatement.getSqlSource().getBoundSql(null).getSql());
//                    out.write("\r\n");
//                }
//                out.close();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
