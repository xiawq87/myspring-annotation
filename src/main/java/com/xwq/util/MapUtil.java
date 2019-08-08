package com.xwq.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapUtil {
    public static void addToMap(Map<String, List<String>> map, String key, String value) {
        if(!map.containsKey(key)) {
            List list = new ArrayList();
            list.add(value);
            map.put(key, list);
        } else {
            map.get(key).add(value);
        }
    }

    public static void addToMap(Map<Class, List<String>> map, Class key, String value) {
        if(!map.containsKey(key)) {
            List list = new ArrayList();
            list.add(value);
            map.put(key, list);
        } else {
            map.get(key).add(value);
        }
    }
}
