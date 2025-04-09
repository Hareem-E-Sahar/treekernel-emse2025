package br.usp.iterador.jxpath;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import br.usp.iterador.logic.Controller;

public class ReflectionResolver {

    private final Controller controller;

    public ReflectionResolver(Controller controller) {
        this.controller = controller;
    }

    public Member getGenericMember(Class<? extends Object> type, String fieldOrProperty) throws NoSuchFieldException {
        try {
            Field field = type.getDeclaredField(fieldOrProperty);
            return new FieldMember(field);
        } catch (Exception e) {
        }
        try {
            String methodName = "get" + Character.toUpperCase(fieldOrProperty.charAt(0)) + fieldOrProperty.substring(1);
            Method method = type.getDeclaredMethod(methodName);
            return new MethodMember(method);
        } catch (Exception e) {
        }
        throw new NoSuchFieldException("Member not found: " + fieldOrProperty);
    }

    public Field getField(Class<? extends Object> type, String field) {
        try {
            return type.getDeclaredField(field);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T instantiate(Class<T> type, Object... args) {
        Class[] paramType = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            paramType[i] = args[i].getClass();
        }
        try {
            return type.getConstructor(paramType).newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException("Impossible to instantiate object of type " + type.getName(), e);
        }
    }

    public <T> T instantiateIOC(Class<T> type) {
        return controller.newInstance(type);
    }
}
