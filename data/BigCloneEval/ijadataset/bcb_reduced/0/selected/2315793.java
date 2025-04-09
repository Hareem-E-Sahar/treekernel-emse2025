package dryven.model.binding.fieldset;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import javax.persistence.Column;
import dryven.annotations.Validator;

public class MethodField extends AnnotatedField {

    private String _name;

    private Method _getter;

    private Method _setter;

    public MethodField(Method[] getterAndSetter) {
        this(getterAndSetter[0], getterAndSetter[1]);
    }

    public MethodField(Method getter, Method setter) {
        super(getter);
        _getter = getter;
        _setter = setter;
        String getterName = propertyNameFromGetter(getter);
        _name = getterName;
    }

    protected static String propertyNameFromGetter(Method getter) {
        String getterName = getter.getName();
        if (getterName.startsWith("get")) {
            getterName = getterName.substring(3);
        } else if (getterName.startsWith("is")) {
            getterName = getterName.substring(2);
        }
        getterName = getterName.substring(0, 1).toLowerCase() + getterName.substring(1);
        return getterName;
    }

    protected static Method getSetterForGetter(Method getter) {
        String propName = MethodField.propertyNameFromGetter(getter);
        String setterName = "set" + propName.substring(0, 1).toUpperCase() + propName.substring(1);
        Method setter = null;
        try {
            setter = getter.getDeclaringClass().getMethod(setterName, new Class<?>[] { getter.getReturnType() });
        } catch (Exception e) {
        }
        return setter;
    }

    protected static boolean isGetterMethod(Method getter) {
        String getterName = getter.getName();
        return (getterName.startsWith("get") || getterName.startsWith("is")) && getter.getParameterTypes().length == 0;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public Object getValue(Object model) {
        try {
            return _getter.invoke(model, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setValue(Object model, Object value) {
        try {
            _setter.invoke(model, new Object[] { value });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Access getAccess() {
        Access a = super.getAccess();
        if (a != Access.Read && _setter == null) {
            return Access.Read;
        }
        return a;
    }

    public Method getGetter() {
        return _getter;
    }

    public Method getSetter() {
        return _setter;
    }

    @Override
    public Class<?> getModelType() {
        return _getter.getDeclaringClass();
    }

    @Override
    public Class<?> getValueType() {
        return _getter.getReturnType();
    }

    @Override
    public int getMaxLength() {
        Validator v = _getter.getAnnotation(Validator.class);
        if (v != null && v.maxLength() != 0) {
            return v.maxLength();
        }
        Column c = _getter.getAnnotation(Column.class);
        if (c != null) {
            return c.length();
        }
        return 0;
    }

    public Object newModelInstance() {
        Class<?> clazz = _getter.getDeclaringClass();
        Object model = null;
        try {
            Constructor<?> ctor = clazz.getConstructor(new Class<?>[] {});
            boolean accessible = ctor.isAccessible();
            if (!accessible) {
                ctor.setAccessible(true);
            }
            model = ctor.newInstance(new Object[] {});
            if (!accessible) {
                ctor.setAccessible(false);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not get model default constructor: " + clazz.getName());
        }
        return model;
    }
}
