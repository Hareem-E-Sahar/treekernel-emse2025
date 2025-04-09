package com.michael.common;

import java.util.HashMap;

/**
 * 单例对象工厂
 * 
 * @author zhanghongze
 * 
 */
public class Factory {

    private static HashMap<String, Object> objMap = new HashMap<String, Object>();

    /**
	 * 获取对象的工厂
	 * 
	 * @param clazz
	 * @return
	 */
    public static Object getInstance(Class<?> clazz) {
        Object obj = objMap.get(clazz.toString());
        if (obj == null) {
            try {
                obj = clazz.newInstance();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
            objMap.put(clazz.toString(), obj);
        }
        return obj;
    }

    /**
	 * 获取对象的工厂
	 * @param clazz
	 * @param params
	 * @return
	 */
    public static Object getInstance(Class<?> clazz, Object[] params) {
        Object obj = objMap.get(clazz.toString() + params.toString());
        if (obj == null) {
            Class<?>[] parameterTypes = new Class[params.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                parameterTypes[i] = params[i].getClass();
            }
            try {
                obj = clazz.getConstructor(parameterTypes).newInstance(params);
            } catch (Exception e) {
                e.printStackTrace();
            }
            objMap.put(clazz.toString() + params.toString(), obj);
        }
        return obj;
    }
}
