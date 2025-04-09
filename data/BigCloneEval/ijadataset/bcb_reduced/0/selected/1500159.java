package it.freax.fpm.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * @author kLeZ-hAcK
 */
public class Generics<T> {

    Class<T> clazz;

    public Generics(Class<T> clazz) {
        this.clazz = clazz;
    }

    public static <T> Generics<T> getOne() {
        return new Generics<T>(null);
    }

    public static <T> Generics<T> getOne(Class<T> clazz) {
        return new Generics<T>(clazz);
    }

    @SuppressWarnings("unchecked")
    public T getInstance() {
        T t = null;
        if (clazz != null) {
            try {
                t = clazz.newInstance();
            } catch (InstantiationException e) {
                ErrorHandler.getOne(getClass()).handle(e);
            } catch (IllegalAccessException e) {
                ErrorHandler.getOne(getClass()).handle(e);
            }
        } else {
            t = (T) new Object();
        }
        return t;
    }

    public T getChildInstance(String childSideName, Object... args) throws InstantiationException {
        StringBuffer className = new StringBuffer(clazz.getPackage().getName());
        className.append('.');
        className.append(childSideName).append(clazz.getSimpleName());
        return getInstance(className.toString(), args);
    }

    @SuppressWarnings("unchecked")
    public T getInstance(String className, Object... args) throws InstantiationException {
        T ret = null;
        FpmCollections<Class<?>> colls = FpmCollections.getOne(new ArrayList<Class<?>>());
        try {
            Class<?> dad = Class.forName(className);
            for (Object o : args) {
                colls.add(o.getClass());
            }
            Class<?>[] app = new Class<?>[] {};
            Constructor<?> c = dad.getConstructor(colls.toArray(app));
            ret = (T) c.newInstance(args);
        } catch (ClassNotFoundException e) {
            throw ErrorHandler.getOne(getClass()).<InstantiationException>rethrow(e);
        } catch (SecurityException e) {
            throw ErrorHandler.getOne(getClass()).<InstantiationException>rethrow(e);
        } catch (NoSuchMethodException e) {
            throw ErrorHandler.getOne(getClass()).<InstantiationException>rethrow(e);
        } catch (IllegalArgumentException e) {
            throw ErrorHandler.getOne(getClass()).<InstantiationException>rethrow(e);
        } catch (IllegalAccessException e) {
            throw ErrorHandler.getOne(getClass()).<InstantiationException>rethrow(e);
        } catch (InvocationTargetException e) {
            throw ErrorHandler.getOne(getClass()).<InstantiationException>rethrow(e);
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    public <C> C cast(T t) {
        return (C) t;
    }
}
