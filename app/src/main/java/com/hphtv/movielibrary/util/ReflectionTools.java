package com.hphtv.movielibrary.util;

import java.lang.reflect.Method;

/**
 * @author lxp
 * @date 20-9-19
 */
public class ReflectionTools {

    /**
     * 利用递归找一个类的指定方法，如果找不到，去父亲里面找直到最上层Object对象为止。
     *
     * @param clazz      目标类
     * @param methodName 方法名
     * @param classes    方法参数类型数组
     * @return 方法对象
     */
    public static Method getMethod(Class clazz, String methodName, Class<?>... classes) {
        Method method = null;
        try {
            method = clazz.getDeclaredMethod(methodName, classes);
        } catch (NoSuchMethodException e) {
            try {
                method = clazz.getMethod(methodName, classes);
            } catch (NoSuchMethodException ex) {
                if (clazz.getSuperclass() == null) {
                    return method;
                } else {
                    method = getMethod(clazz.getSuperclass(), methodName, classes);
                }
            }
        }
        return method;
    }

    /**
     *
     * @param obj 调用对象
     * @param method 方法
     * @param objects 参数
     * @return
     */
    public static Object invoke(final Object obj,Method method,Object... objects) {
        try {
            method.setAccessible(true);// 调用private方法的关键一句话
            return method.invoke(obj, objects);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
