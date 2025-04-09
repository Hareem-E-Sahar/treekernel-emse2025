package org.nakedobjects.nof.reflect.java.reflect.util;

import org.nakedobjects.noa.NakedObjectRuntimeException;
import org.nakedobjects.noa.adapter.NakedObject;
import org.nakedobjects.noa.spec.NakedObjectSpecification;
import org.nakedobjects.nof.core.context.NakedObjectsContext;
import org.nakedobjects.nof.core.persist.PersistorUtil;
import org.nakedobjects.nof.core.util.UnknownTypeException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public final class WrapperUtils {

    private static Map wrapperClasses = new HashMap();

    static {
        wrapperClasses.put(boolean.class, Boolean.class);
        wrapperClasses.put(byte.class, Byte.class);
        wrapperClasses.put(short.class, Short.class);
        wrapperClasses.put(int.class, Integer.class);
        wrapperClasses.put(long.class, Long.class);
        wrapperClasses.put(float.class, Float.class);
        wrapperClasses.put(double.class, Double.class);
    }

    private WrapperUtils() {
    }

    public static Object[] convertPrimitiveToObjectArray(final Class arrayType, final Object originalArray) {
        Object[] convertedArray;
        try {
            final Class wrapperClass = (Class) wrapperClasses.get(arrayType);
            final Constructor constructor = wrapperClass.getConstructor(new Class[] { String.class });
            final int len = Array.getLength(originalArray);
            convertedArray = (Object[]) Array.newInstance(wrapperClass, len);
            for (int i = 0; i < len; i++) {
                convertedArray[i] = constructor.newInstance(new Object[] { Array.get(originalArray, i).toString() });
            }
        } catch (final NoSuchMethodException e) {
            throw new NakedObjectRuntimeException(e);
        } catch (final ArrayIndexOutOfBoundsException e) {
            throw new NakedObjectRuntimeException(e);
        } catch (final IllegalArgumentException e) {
            throw new NakedObjectRuntimeException(e);
        } catch (final InstantiationException e) {
            throw new NakedObjectRuntimeException(e);
        } catch (final IllegalAccessException e) {
            throw new NakedObjectRuntimeException(e);
        } catch (final InvocationTargetException e) {
            throw new NakedObjectRuntimeException(e);
        }
        return convertedArray;
    }

    public static NakedObject createAdapter(final Class type, final Object object) {
        final NakedObjectSpecification specification = NakedObjectsContext.getReflector().loadSpecification(type);
        if (specification.isObject()) {
            return PersistorUtil.createAdapter(object);
        } else {
            throw new UnknownTypeException("not an object, is this a collection?");
        }
    }
}
