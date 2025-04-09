package org.fest.assertions;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Understands access to constructors using Java reflection.
 *
 * @author Yvonne Wang
 * @author Alex Ruiz
 */
class ConstructorInvoker {

    Object newInstance(String className, Class<?>[] parameterTypes, Object[] parameterValues) throws Exception {
        Class<?> targetType = Class.forName(className);
        Constructor<?> constructor = targetType.getConstructor(parameterTypes);
        boolean accessible = constructor.isAccessible();
        try {
            setAccessible(constructor, true);
            return constructor.newInstance(parameterValues);
        } finally {
            try {
                setAccessible(constructor, accessible);
            } catch (RuntimeException e) {
            }
        }
    }

    private void setAccessible(AccessibleObject accessible, boolean value) {
        AccessController.doPrivileged(new SetAccessibleValueAction(accessible, value));
    }

    private static class SetAccessibleValueAction implements PrivilegedAction<Void> {

        private final AccessibleObject accessible;

        private final boolean value;

        private SetAccessibleValueAction(AccessibleObject accessible, boolean value) {
            this.accessible = accessible;
            this.value = value;
        }

        public Void run() {
            accessible.setAccessible(value);
            return null;
        }
    }
}
