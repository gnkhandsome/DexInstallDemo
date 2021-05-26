package com.qknode.myapplication;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectUtil {

    /**
     * 通过反射获取Object对象中的某个字段
     *
     * @param instance 对象实例
     * @param name     字段名称
     * @return
     */
    public static Field findField(Object instance, String name) throws NoSuchFieldException {
        Class clazz = instance.getClass();
        while (null != clazz) {
            try {
                Field field = clazz.getDeclaredField(name);
                // 字段是否可访问
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                return field;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException("No such field: " + name);
    }


    /**
     * 通过反射获取Object对象中的某个方法
     *
     * @param instance       对象实例
     * @param name           方法名称
     * @param parameterTypes 方法参数类型列表
     * @return
     */
    public static Method findMethod(Object instance, String name,
                                    Class<?>... parameterTypes) throws NoSuchMethodException {
        Class aClass = instance.getClass();

        while (null != aClass) {
            try {
                Method method = aClass.getDeclaredMethod(name, parameterTypes);
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                return method;
            } catch (NoSuchMethodException e) {
                aClass = aClass.getSuperclass();
            }
        }
        throw new NoSuchMethodException("No such method: " + name);
    }

}
