package org.delisy.utils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author LvWei
 * @Date 2024/7/30 8:48
 */
public class LocalDataCache {
    private static final ConcurrentHashMap<String ,String> cache = new ConcurrentHashMap<>();

    public static void set(String key, String value){
        cache.put(key, value);
    }

    public static String get(String key){
        return cache.get(key);
    }

    public static void remove(String key){
        cache.remove(key);
    }
}
