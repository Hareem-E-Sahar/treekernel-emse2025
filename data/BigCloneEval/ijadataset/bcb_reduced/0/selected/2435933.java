package net.sf.joafip.store.service;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import net.sf.joafip.AssertNotNull;
import net.sf.joafip.Fortest;
import net.sf.joafip.NotStorableClass;
import org.apache.log4j.Logger;
import sun.reflect.ReflectionFactory;

/**
 * helper for reflection
 * 
 * @author luc peuvrier
 * 
 */
@NotStorableClass
public class HelperReflect {

    private static final String FAILED_CREATE_INSTANCE_WITHOUT_CONSTRUCTOR_INVOCATION = "failed create instance without constructor invocation";

    private static final String FAILED_SET_ARRAY_ELEMENT = "failed set array element";

    private static final String FAILED_GET_ARRAY_ELEMENT = "failed get array element";

    private static final String FAILED_INVOKE_STATIC_METHOD = "failed invoke static method";

    private static final Logger _log = Logger.getLogger(HelperReflect.class);

    private static final String FAILED_SET_FIELD_VALUE = "failed set field value";

    private static final String FAILED_GET_FIELD_VALUE = "failed get field value";

    private static final HelperReflect INSTANCE = new HelperReflect();

    private static final ReflectionFactory reflectionFactory = ReflectionFactory.getReflectionFactory();

    public static HelperReflect getInstance() {
        return INSTANCE;
    }

    private HelperReflect() {
        super();
    }

    /**
	 * assert that field annotated {@link AssertNotNull} are not null<br>
	 * used for debug<br>
	 * 
	 * @param object
	 * @throws ReflectException
	 */
    @Fortest
    public void assertNotNullField(final Object object) throws ReflectException {
        if (object != null) {
            final List<Field> list = allDeclaredFieldsByReflection(object.getClass());
            for (Field field : list) {
                getFieldValue(object, field);
            }
        }
    }

    /**
	 * get all declared fields to persist for a class using recursive reflection
	 * on class and mother class
	 * 
	 * @param classForField
	 *            class definition where get all declared fields to persist
	 * @return all declared fields for the class
	 */
    public List<Field> allDeclaredFieldsByReflection(final Class classForField) {
        Field[] fields;
        Class currentClass = classForField;
        final List<Field> list = new LinkedList<Field>();
        while (currentClass != null && !Enum.class.equals(currentClass)) {
            fields = currentClass.getDeclaredFields();
            for (Field field : fields) {
                if (toPersist(field)) {
                    list.add(field);
                } else if (_log.isDebugEnabled()) {
                    _log.debug("not persisted => not writed " + field.getName());
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return list;
    }

    /**
	 * 
	 * @param field
	 * @return true if field is persistent
	 */
    private boolean toPersist(final Field field) {
        final int modifiers = field.getModifiers();
        return !Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers);
    }

    /**
	 * @param object
	 * @param field
	 * @return
	 * @throws ReflectException
	 */
    public Object getFieldValue(final Object object, final Field field) throws ReflectException {
        final Object fieldValue;
        try {
            field.setAccessible(true);
            fieldValue = field.get(object);
            field.setAccessible(false);
            if (fieldValue == null && field.isAnnotationPresent(AssertNotNull.class)) {
                nullFieldException(field, object);
            }
        } catch (IllegalArgumentException exception) {
            _log.fatal("\n" + FAILED_GET_FIELD_VALUE, exception);
            throw new ReflectException(FAILED_GET_FIELD_VALUE, exception);
        } catch (IllegalAccessException exception) {
            _log.fatal("\n" + FAILED_GET_FIELD_VALUE, exception);
            throw new ReflectException(FAILED_GET_FIELD_VALUE, exception);
        }
        return fieldValue;
    }

    /**
	 * @param object
	 * @param field
	 * @param type
	 * @param fieldValue
	 * @throws ReflectException
	 */
    public void setFieldValue(final Object object, final Field field, final Class<?> type, final Object fieldValue) throws ReflectException {
        try {
            if (fieldValue == null && field.isAnnotationPresent(AssertNotNull.class)) {
                nullFieldException(field, object);
            }
            field.setAccessible(true);
            field.set(object, fieldValue);
            field.setAccessible(false);
        } catch (IllegalArgumentException exception) {
            _log.fatal(fatalMessage(type, field, fieldValue), exception);
            throw new ReflectException(FAILED_SET_FIELD_VALUE, exception);
        } catch (IllegalAccessException exception) {
            _log.fatal(fatalMessage(type, field, fieldValue), exception);
            throw new ReflectException(FAILED_SET_FIELD_VALUE, exception);
        }
    }

    /**
	 * throws a field value can not be null exception
	 * 
	 * @param field
	 * @param object
	 * @throws ReflectException
	 *             always throws
	 */
    private void nullFieldException(final Field field, final Object object) throws ReflectException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("field ");
        stringBuilder.append(field.getName());
        stringBuilder.append(" of ");
        stringBuilder.append(field.getDeclaringClass());
        stringBuilder.append(" ( ");
        stringBuilder.append(object.getClass());
        stringBuilder.append(" ) is null and annotated @AssertNotNull");
        throw new ReflectException(stringBuilder.toString());
    }

    /**
	 * @param type
	 * @param fieldValue
	 * @return
	 */
    private String fatalMessage(final Class<?> type, final Field field, final Object fieldValue) {
        return "\n" + FAILED_SET_FIELD_VALUE + "\nfield " + field + "\nfield type " + type + "\nfield value type " + fieldValue.getClass();
    }

    /**
	 * @param array
	 * @param index
	 * @param elementValue
	 * @throws ReflectException
	 */
    public void setArrayElement(final Object array, final int index, final Object elementValue) throws ReflectException {
        try {
            Array.set(array, index, elementValue);
        } catch (Exception exception) {
            throw new ReflectException(FAILED_SET_ARRAY_ELEMENT, exception);
        }
    }

    /**
	 * @param array
	 * @param index
	 * @return
	 * @throws ReflectException
	 */
    public Object getArrayElement(final Object array, final int index) throws ReflectException {
        final Object elementObject;
        try {
            elementObject = Array.get(array, index);
        } catch (Exception exception) {
            throw new ReflectException(FAILED_GET_ARRAY_ELEMENT, exception);
        }
        return elementObject;
    }

    public Object invokeStaticMethod(final Class<?> objectClass, final String methodName, final Class<?>[] parameterTypes, final Object[] args) throws ReflectException {
        final Object object;
        try {
            final Method method = objectClass.getMethod(methodName, parameterTypes);
            object = method.invoke(null, args);
        } catch (SecurityException exception) {
            throw new ReflectException(FAILED_INVOKE_STATIC_METHOD, exception);
        } catch (NoSuchMethodException exception) {
            throw new ReflectException(FAILED_INVOKE_STATIC_METHOD, exception);
        } catch (IllegalArgumentException exception) {
            throw new ReflectException(FAILED_INVOKE_STATIC_METHOD, exception);
        } catch (IllegalAccessException exception) {
            throw new ReflectException(FAILED_INVOKE_STATIC_METHOD, exception);
        } catch (InvocationTargetException exception) {
            throw new ReflectException(FAILED_INVOKE_STATIC_METHOD, exception);
        }
        return object;
    }

    public Object invokeMethod(final Class<?> objectClass, final String methodName, final Object objectForInvoke, final Class<?>[] parameterTypes, final Object[] args) throws ReflectException {
        final Object object;
        try {
            final Method method = objectClass.getMethod(methodName, parameterTypes);
            object = method.invoke(objectForInvoke, args);
        } catch (SecurityException exception) {
            throw new ReflectException(FAILED_INVOKE_STATIC_METHOD, exception);
        } catch (NoSuchMethodException exception) {
            throw new ReflectException(FAILED_INVOKE_STATIC_METHOD, exception);
        } catch (IllegalArgumentException exception) {
            throw new ReflectException(FAILED_INVOKE_STATIC_METHOD, exception);
        } catch (IllegalAccessException exception) {
            throw new ReflectException(FAILED_INVOKE_STATIC_METHOD, exception);
        } catch (InvocationTargetException exception) {
            throw new ReflectException(FAILED_INVOKE_STATIC_METHOD, exception);
        }
        return object;
    }

    /**
	 * create new instance whitout invoke constructor
	 * 
	 * @param objectClass
	 *            the instance to create class
	 * @return the new instance
	 * @throws ReflectException
	 *             creation failure
	 */
    public Object newInstanceNoConstruction(final Class objectClass) throws ReflectException {
        Constructor constr;
        try {
            constr = reflectionFactory.newConstructorForSerialization(objectClass, Object.class.getConstructor(new Class[0]));
            return constr.newInstance();
        } catch (SecurityException exception) {
            throw new ReflectException(FAILED_CREATE_INSTANCE_WITHOUT_CONSTRUCTOR_INVOCATION, exception);
        } catch (NoSuchMethodException exception) {
            throw new ReflectException(FAILED_CREATE_INSTANCE_WITHOUT_CONSTRUCTOR_INVOCATION, exception);
        } catch (IllegalArgumentException exception) {
            throw new ReflectException(FAILED_CREATE_INSTANCE_WITHOUT_CONSTRUCTOR_INVOCATION, exception);
        } catch (InstantiationException exception) {
            throw new ReflectException(FAILED_CREATE_INSTANCE_WITHOUT_CONSTRUCTOR_INVOCATION, exception);
        } catch (IllegalAccessException exception) {
            throw new ReflectException(FAILED_CREATE_INSTANCE_WITHOUT_CONSTRUCTOR_INVOCATION, exception);
        } catch (InvocationTargetException exception) {
            throw new ReflectException(FAILED_CREATE_INSTANCE_WITHOUT_CONSTRUCTOR_INVOCATION, exception);
        }
    }
}
