package org.dynalang.mop.beans;

import java.lang.reflect.Array;
import java.lang.reflect.Member;
import java.lang.reflect.UndeclaredThrowableException;
import org.dynalang.mop.CallProtocol;
import org.dynalang.mop.BaseMetaobjectProtocol.Result;

/**
 * A dynamic method bound to exactly one Java method. Used to represent both
 * non-overloaded methods as well as results of overloaded method resolution. 
 * @author Attila Szegedi
 * @version $Id: $
 * @param <T>
 */
public class SimpleDynamicMethod<T extends Member> extends DynamicMethod<T> {

    private final T member;

    private final Class<?>[] paramTypes;

    SimpleDynamicMethod(T method) {
        this.member = method;
        this.paramTypes = getParameterTypes(method);
    }

    /**
     * Returns the single method that this dynamic method is bound to. Note 
     * that if you invoke the member directly (instead of going through 
     * {@link #call(Object, CallProtocol, Object...)}, you will need to do any
     * argument marshaling and vararg packing yourself), while if you use this
     * object's call method, it will do these for you.
     * @return this dynamic method's associated Java method.
     */
    public T getMember() {
        return member;
    }

    @Override
    public Object call(Object target, CallProtocol callProtocol, Object... args) {
        if (args == null) {
            args = NULL_ARGS;
        }
        int argsLen = args.length;
        int paramTypesLen = paramTypes.length;
        int paramTypesLenM1 = paramTypesLen - 1;
        boolean varArg = isVarArgs(member);
        int fixArgsLen = varArg ? paramTypesLenM1 : paramTypesLen;
        int min = Math.min(argsLen, fixArgsLen);
        boolean argsCloned = false;
        for (int i = 0; i < min; ++i) {
            Object src = args[i];
            Object dst = marshal(args[i], paramTypes[i], callProtocol);
            if (dst != src) {
                if (!argsCloned) {
                    args = args.clone();
                }
                args[i] = dst;
            }
        }
        if (varArg) {
            Class<?> varArgType = paramTypes[paramTypesLenM1];
            Class<?> componentType = varArgType.getComponentType();
            if (argsLen != paramTypesLen) {
                Object[] newargs = new Object[paramTypesLen];
                System.arraycopy(args, 0, newargs, 0, paramTypesLenM1);
                int varArgLen = argsLen - paramTypesLenM1;
                Object varArgArray = Array.newInstance(componentType, varArgLen);
                newargs[paramTypesLenM1] = varArgArray;
                for (int i = 0; i < varArgLen; ++i) {
                    Array.set(varArgArray, i, marshal(args[paramTypesLenM1 + i], componentType, callProtocol));
                }
                args = newargs;
            } else {
                Object lastArg = args[paramTypesLenM1];
                Object varArgArray = callProtocol.representAs(lastArg, varArgType);
                if (varArgArray != null && !varArgType.isInstance(varArgArray)) {
                    varArgArray = Array.newInstance(componentType, 1);
                    Array.set(varArgArray, 0, marshal(lastArg, componentType, callProtocol));
                }
                if (lastArg != varArgArray) {
                    if (!argsCloned) {
                        args = args.clone();
                    }
                    args[paramTypesLenM1] = varArgArray;
                }
            }
        }
        try {
            return invoke(member, target, args);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    private Object marshal(Object src, Class<?> targetClass, CallProtocol callProtocol) {
        Object dst = callProtocol.representAs(src, targetClass);
        if (dst == Result.noAuthority || dst == Result.noRepresentation) {
            String className = src == null ? "null" : src.getClass().getName();
            throw new IllegalArgumentException("Can't represent " + className + " as " + targetClass.getName() + " for invoking " + member);
        }
        return dst;
    }
}
