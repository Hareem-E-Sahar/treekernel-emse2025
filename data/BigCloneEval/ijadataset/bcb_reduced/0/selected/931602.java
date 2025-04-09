package com.hy.enterprise.framework.util.lang;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import com.vsoft.libra.enterprise.framework.lang.LibraRuntimeException;

/**
 * <ul>
 * <li>设计作者：刘川</li>
 * <li>设计日期：2009-8-4</li>
 * <li>设计时间：下午01:44:32</li>
 * <li>设计目的：Libra平台对象操作助手类</li>
 * </ul>
 * <ul>
 * <b>修订历史</b>
 * <li>1、</li>
 * </ul>
 */
public class ObjectUtil {

    /**
	 * 
	 */
    private static final String GET = "get";

    /**
	 * 
	 */
    private static final String IS = "is";

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-10</li>
	 * <li>设计时间：上午01:32:02</li>
	 * <li>设计目的：将指定对象加入到指定数组中</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param array
	 * @param object
	 * @return
	 */
    public static Object[] addObjectToArray(Object[] array, Object object) {
        Class<?> compType = Object.class;
        if (array != null) {
            compType = array.getClass().getComponentType();
        } else if (object != null) {
            compType = object.getClass();
        }
        int newArrLength = (array != null ? array.length + 1 : 1);
        Object[] newArr = (Object[]) Array.newInstance(compType, newArrLength);
        if (array != null) {
            System.arraycopy(array, 0, newArr, 0, array.length);
        }
        newArr[newArr.length - 1] = object;
        return newArr;
    }

    public static String decapitalize(String substring) {
        if ((substring.length() >= 2) && Character.isUpperCase(substring.charAt(0)) && Character.isUpperCase(substring.charAt(1))) {
            return substring;
        } else {
            StringBuilder sb = new StringBuilder(substring.length());
            sb.append(Character.toLowerCase(substring.charAt(0))).append(substring.substring(1));
            return sb.toString();
        }
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-9</li>
	 * <li>设计时间：下午05:25:07</li>
	 * <li>设计目的：将对象转换为从二进制字节数组</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param object
	 * @return
	 */
    public static byte[] getBytes(Object object) {
        ByteArrayOutputStream byteArrayOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        byte[] data = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            data = byteArrayOutputStream.toByteArray();
        } catch (IOException ioException) {
            throw new LibraRuntimeException("在将对象转换为从二进制字节数组时发生错误", ioException);
        } finally {
            try {
                if (objectOutputStream != null) {
                    objectOutputStream.flush();
                    objectOutputStream.close();
                }
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
            } catch (IOException ioException) {
                throw new LibraRuntimeException("在将对象转换为从二进制字节数组时发生错误", ioException);
            }
        }
        return data;
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-9</li>
	 * <li>设计时间：下午05:26:16</li>
	 * <li>设计目的：将二进制字节数组转换对象</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param bytes
	 * @return
	 */
    public static Object getObject(byte[] bytes) {
        ByteArrayInputStream byteArrayInputStream = null;
        ObjectInputStream objectInputStream = null;
        Object object = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(bytes);
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            object = objectInputStream.readObject();
        } catch (ClassNotFoundException classNotFoundException) {
            throw new LibraRuntimeException("在将从二进制字节数组转换对象时发生错误", classNotFoundException);
        } catch (IOException ioException) {
            throw new LibraRuntimeException("在将从二进制字节数组转换对象时发生错误", ioException);
        } finally {
            try {
                if (objectInputStream != null) {
                    objectInputStream.close();
                }
                if (byteArrayInputStream != null) {
                    byteArrayInputStream.close();
                }
            } catch (IOException ioException) {
                throw new LibraRuntimeException("在将从二进制字节数组转换对象时发生错误", ioException);
            }
        }
        return object;
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-10</li>
	 * <li>设计时间：下午02:02:31</li>
	 * <li>设计目的：判断给定方法是否为获取器方法</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param method
	 * @return
	 */
    public static boolean isGetter(Method method) {
        if (method.getParameterTypes().length > 0) {
            return false;
        }
        String name = method.getName();
        Class<?> returnType = method.getReturnType();
        boolean b = returnType.equals(Boolean.class) || returnType.equals(Boolean.TYPE);
        return !returnType.equals(Void.class) && (name.startsWith(ObjectUtil.GET) || (b && name.startsWith(ObjectUtil.IS)));
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-10</li>
	 * <li>设计时间：下午01:58:45</li>
	 * <li>设计目的：将多个同类型对象合并成一个数组</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param <T>
	 * @param clazz
	 * @param first
	 * @param more
	 * @return
	 */
    @SuppressWarnings("unchecked")
    public static <T> T[] merge(Class<T> clazz, T first, T... more) {
        int morel = 0;
        if (more != null) {
            morel = more.length;
        }
        T[] merged = (T[]) Array.newInstance(clazz, 1 + morel);
        merged[0] = first;
        System.arraycopy(more, 0, merged, 1, morel);
        return merged;
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-10</li>
	 * <li>设计时间：下午02:02:48</li>
	 * <li>设计目的：通过给定获取器方法获取其所获取属性的名称</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param getter
	 * @return
	 */
    public static String propertyNameForGetter(Method getter) {
        if (getter.getParameterTypes().length > 0) {
            throw new IllegalArgumentException("方法：[" + getter + "]不是获取器方法，这是由于其带有参数");
        }
        String methodName = getter.getName();
        if (methodName.startsWith(ObjectUtil.IS) && (getter.getReturnType().equals(Boolean.class) || getter.getReturnType().equals(Boolean.TYPE))) {
            return ObjectUtil.decapitalize(methodName.substring(ObjectUtil.IS.length()));
        } else if (methodName.startsWith(ObjectUtil.GET)) {
            return ObjectUtil.decapitalize(methodName.substring(ObjectUtil.GET.length()));
        } else {
            throw new IllegalStateException("方法：[" + getter + "]没有遵循Java Bean命名规范");
        }
    }

    /**
	 * 构造函数
	 */
    private ObjectUtil() {
        super();
    }
}
