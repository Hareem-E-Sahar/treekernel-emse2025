package net.sourceforge.contactmanager.data;

import java.lang.reflect.Constructor;
import java.security.PrivilegedAction;

public class ConstructorCaller<T> implements PrivilegedAction<T> {

    Class<T> sourceClass;

    Class<? extends Object>[] parmTypes;

    Object[] args;

    @SuppressWarnings("unchecked")
    public ConstructorCaller(Class<T> sourceClass, Object... args) {
        this.sourceClass = sourceClass;
        this.args = args;
        parmTypes = new Class[args.length];
        for (int x = 0; x < args.length; x++) {
            parmTypes[x] = args[x].getClass();
        }
    }

    @Override
    public T run() {
        try {
            Constructor<T> constructor = sourceClass.getConstructor(parmTypes);
            return constructor.newInstance(args);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
