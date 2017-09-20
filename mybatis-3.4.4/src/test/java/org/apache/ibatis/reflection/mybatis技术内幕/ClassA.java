package org.apache.ibatis.reflection.mybatis技术内幕;

import java.util.Map;

/**
 * @author wusi
 * @version 2017/9/20 21:37.
 */
public class ClassA <K, V> {
    protected Map<K, V> map;

    public Map<K, V> getMap() {
        return map;
    }

    public void setMap(Map<K, V> map) {
        this.map = map;
    }
}
