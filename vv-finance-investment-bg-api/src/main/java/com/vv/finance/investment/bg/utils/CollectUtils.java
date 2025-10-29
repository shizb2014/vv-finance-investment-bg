package com.vv.finance.investment.bg.utils;

import java.util.*;

/**
 * @author chenyu
 * @date 2020/12/21 14:53
 */
public  class CollectUtils {
    /**
     * 描述：分割Map
     */
    public static <K, V> List<Map<K, V>> splitMap(Map<K, V> map, int pageSize) {
        if (map == null || map.isEmpty()) {
            return Collections.emptyList();
        }
        pageSize = pageSize == 0 ? 10000 : pageSize;
        List<Map<K, V>> newList = new ArrayList<>();
        int j = 0;
        for (K k : map.keySet()) {
            if (j % pageSize == 0) {
                newList.add(new HashMap<>());
            }
            newList.get(newList.size() - 1).put(k, map.get(k));
            j++;
        }
        return newList;
    }
}
