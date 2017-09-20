package org.apache.ibatis.reflection.mybatis技术内幕;

import org.apache.ibatis.reflection.TypeParameterResolver;
import org.apache.ibatis.submitted.ognl_enum.Person;
import org.junit.Test;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author wusi
 * @version 2017/9/20 21:39.
 */
public class TestType {

    SubClassA<Long> sa = new SubClassA<>();

    @Test
    public void testType() throws Exception{
        Field map =  ClassA.class.getDeclaredField("map");
        print(map.getGenericType() instanceof ParameterizedType);
        print(map);

        Type type = TypeParameterResolver.resolveFieldType(map,
                ParameterizedTypeImpl.make(
                        SubClassA.class,
                        new Type[]{Long.class},
                        TestType.class));
        // 等同于以下语句
        type = TypeParameterResolver.resolveFieldType(
                map,
                TestType.class.getDeclaredField("sa").getGenericType());
        print(type);
        // ParameterizedTypeImpl 是 ParameterizedType的实现
        ParameterizedType p = (ParameterizedType) type;
        print(p.getRawType());
        print(p.getOwnerType());
        for (Type t : p.getActualTypeArguments()) {
            print(t);
        }
    }

    private void print(Object o) {
        System.out.println(o);
    }
}
