package com.metasolutions.jfcml.helpers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import com.metasolutions.jfcml.extend.ScriptHandler;
import com.metasolutions.util.PackageRegistry;
import com.metasolutions.util.SimpleStack;

/**
 * A DocumentHandler which provides a collection of utilities for Reflection
 * oriented tasks.
 * 
 * @author Shawn Curry 
 * @version 0.9 May 28, 2005
 */
public class ReflectionHandler extends DocumentHandler {

    private static final boolean REFLECT_DEBUG;

    static {
        REFLECT_DEBUG = ((Boolean) PackageRegistry.getService("REFLECT_DEBUG")).booleanValue();
    }

    private ScriptHandler scriptHandler;

    private MethodParser methodParser;

    public ReflectionHandler(WindowContext context) {
        super(context);
        scriptHandler = null;
        methodParser = new MethodParser();
    }

    /**
     * @return Returns the scriptHandler.
     */
    public ScriptHandler getScriptHandler() {
        return scriptHandler;
    }

    /**
     * @param scriptHandler The scriptHandler to set.
     */
    public void setScriptHandler(ScriptHandler scriptHandler) {
        this.scriptHandler = scriptHandler;
    }

    public Class[] getClasses(Object[] arr) {
        if (arr == null) throw new NullPointerException("Parameter must not be null");
        int len = arr.length;
        Class[] cls = new Class[len];
        for (int i = 0; i < len; ++i) cls[i] = arr[i].getClass();
        return cls;
    }

    public Class[] getNormalizedClasses(Object[] arr) {
        if (arr == null) throw new NullPointerException("Parameter must not be null");
        int len = arr.length;
        Class[] cls = new Class[len];
        for (int i = 0; i < len; ++i) {
            if (arr[i] instanceof Primative) cls[i] = ((Primative) arr[i]).TYPE; else cls[i] = arr[i].getClass();
        }
        return cls;
    }

    public Object resolveCast(Class c, Object o) {
        if (o == null) {
            if (REFLECT_DEBUG) System.out.println("null cannot be cast to " + c);
            return null;
        }
        if (c.isInstance(o)) return o;
        if (c.isPrimitive()) {
            if (o instanceof Primative) return Primative.castToType(c, (Primative) o).getValue(); else throw new RuntimeException("" + o.getClass() + " cannot be cast to " + c);
        } else if (c == String.class && o instanceof Primative) return ((Character) Primative.castToType(char.class, (Primative) o).getValue()).toString(); else throw new RuntimeException("" + o.getClass() + " cannot be cast to " + c);
    }

    public Field getField(Class cls, String field) {
        if (REFLECT_DEBUG) System.out.println("getField: " + field + "; " + cls);
        try {
            return cls.getField(field);
        } catch (SecurityException e) {
        } catch (NoSuchFieldException e) {
        }
        if (REFLECT_DEBUG) System.out.println("Failed to find field: " + field + " in class: " + cls);
        return null;
    }

    public Object getField(Class cls, Object obj, String field) {
        if (cls == null) throw new NullPointerException("Class must not be null");
        if (field == null) throw new NullPointerException("Field name must not be null");
        Field f = getField(cls, field);
        if (f == null) {
            if (REFLECT_DEBUG) System.out.println("getField Failed: class: " + cls + " target: " + (obj == null ? 0 : obj.hashCode()) + " field: " + field);
            return null;
        }
        try {
            return internalize(f.getType(), f.get(obj));
        } catch (IllegalArgumentException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        }
        throw new RuntimeException("Error calling Default Constructor For Class: ");
    }

    public Object getField(Object obj, String name) {
        if (REFLECT_DEBUG) System.out.println("getField: " + name + "; " + (obj != null ? obj.getClass().getName() : "null") + ": " + (obj == null ? 0 : obj.hashCode()));
        return getField(obj.getClass(), obj, name);
    }

    public boolean setField(Class cls, Object obj, Object value, String field) {
        if (cls == null) throw new NullPointerException("Class must not be null");
        if (field == null) throw new NullPointerException("Field name must not be null");
        if (REFLECT_DEBUG) System.out.println("setField: " + field + "; " + cls + ": " + (obj == null ? 0 : obj.hashCode()) + "=" + value);
        Field f = getField(cls, field);
        if (f == null) {
            if (REFLECT_DEBUG) System.out.println("setField Failed: class: " + cls + " target: " + obj + " value: " + value + " field: " + field);
            return false;
        }
        try {
            f.set(obj, normalize(value));
            return true;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Object getConstant(Class cls, String name) {
        if (REFLECT_DEBUG) System.out.println("getConstant: " + name + "; " + cls);
        return getField(cls, null, name);
    }

    public Object normalize(Object o) {
        if (o == null) return null;
        if (o instanceof Primative) return ((Primative) o).getValue();
        return o;
    }

    public Object internalize(Class c, Object o) {
        if (c == null || o == null) return null;
        if (c.isPrimitive()) return new Primative(c, o);
        return o;
    }

    /**
     * 
     * @param toCastTo
     * @param toCast
     * @return
     */
    public MethodContext normalizeArgumentList(Class[] toCastTo, Object[] toCast) {
        int len = toCast.length;
        Object[] casted = new Object[len];
        for (int i = len; i-- > 0; ) casted[i] = (toCast[i] == null ? null : resolveCast(toCastTo[i], toCast[i]));
        return new MethodContext(toCastTo, casted);
    }

    public Object[] internalizeArgumentList(Class[] cls, Object[] list) {
        int len = list.length;
        Object[] intern = new Object[len];
        for (int i = len; i-- > 0; ) intern[i] = internalize(cls[i], list[i]);
        return intern;
    }

    public Object callConstructor(Class c, Object[] args) {
        if (REFLECT_DEBUG) System.out.println("callConstructor: " + c + "; args:" + args.length);
        MethodContext context = getMethodContext(getParameters(c.getConstructors()), args);
        if (context == null) throw new RuntimeException("Error calling constructor: " + c.getName() + "; (check package imports?)");
        return callConstructor(c, context.getParameterClasses(), context.getParameters());
    }

    public Object callDefaultConstructor(Class c) {
        if (REFLECT_DEBUG) System.out.println("callDefaultConstructor: " + c);
        try {
            return c.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Error calling Default Constructor For Class: " + c);
    }

    public Object callConstructor(Class c, Class[] classes, Object[] args) {
        Constructor con = null;
        try {
            con = c.getConstructor(classes);
        } catch (Exception e) {
            throw new RuntimeException("Error locating constructor: " + c);
        }
        try {
            return con.newInstance(args);
        } catch (Exception e1) {
            throw new RuntimeException("Error calling constructor: " + c);
        }
    }

    public Method getMethod(Class cls, String methodname, Class[] classes) {
        if (REFLECT_DEBUG) System.out.println("getMethod: " + methodname + "; " + cls + ": args:" + classes.length);
        try {
            return cls.getMethod(methodname, classes);
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        }
        if (REFLECT_DEBUG) System.out.println("getMethod failed: class: " + cls + " method: " + methodname + " classes: " + classes);
        return null;
    }

    public Object callMethod(Class c, Object target, String methodname, Class[] classes, Object[] arguments) {
        Method method = getMethod(c, methodname, classes);
        if (method == null) return null;
        try {
            return internalize(method.getReturnType(), method.invoke(target, arguments));
        } catch (IllegalArgumentException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        } catch (InvocationTargetException e1) {
            e1.printStackTrace();
        }
        throw new RuntimeException("Unknown error calling method");
    }

    /**
     * 
     * @param target
     * @param methodname
     * @param value
     */
    public Object callMethod(Class c, Object target, String methodname, String value) {
        if (REFLECT_DEBUG) System.out.println("callMethod: " + methodname + "; " + c + ": " + (target == null ? 0 : target.hashCode()) + "." + value);
        Method[] methods = filterMethods(methodname, c.getMethods());
        MethodContext context = getMethodContext(getParameters(methods), scriptHandler.parseStatement(value));
        if (context == null) throw new RuntimeException("Error calling method: " + methodname + "; (check package imports?)");
        return callMethod(c, target, methodname, context.getParameterClasses(), context.getParameters());
    }

    public Object callMethod(Class c, Object target, String methodname, Object[] arguments) {
        if (REFLECT_DEBUG) System.out.println("callMethod: " + methodname + "; " + c + ": " + (target == null ? 0 : target.hashCode()) + ": args:" + arguments.length);
        Method[] methods = filterMethods(methodname, c.getMethods());
        MethodContext context = getMethodContext(getParameters(methods), arguments);
        if (context == null) throw new RuntimeException("Error calling method: " + methodname + "; (check package imports?)");
        return callMethod(c, target, methodname, context.getParameterClasses(), context.getParameters());
    }

    private Method[] filterMethods(String methodName, Method[] array) {
        int hash = methodName.hashCode();
        SimpleStack vector = new SimpleStack();
        for (int i = array.length; i-- > 0; ) if (array[i].getName().hashCode() == hash) vector.push(array[i]);
        int count = vector.size();
        Method[] out = new Method[count];
        for (int i = count; i-- > 0; ) out[i] = (Method) vector.pop();
        return out;
    }

    private Class[][] getParameters(Object[] methods) {
        int sz = methods.length;
        Class[][] out = new Class[sz][];
        if (methods instanceof Method[]) {
            Method[] arr = (Method[]) methods;
            for (int i = sz; i-- > 0; ) out[i] = arr[i].getParameterTypes();
            return out;
        }
        if (methods instanceof Constructor[]) {
            Constructor[] arr = (Constructor[]) methods;
            for (int i = sz; i-- > 0; ) out[i] = arr[i].getParameterTypes();
            return out;
        }
        throw new IllegalArgumentException("Parameter must match one of: <Method[],Constructor[]>");
    }

    public Method getMethod(Class cls, String methodname, Object[] arguments) {
        Method[] methods = filterMethods(methodname, cls.getMethods());
        MethodContext context = getMethodContext(getParameters(methods), arguments);
        if (context == null) {
            if (REFLECT_DEBUG) System.out.println("getMethod failed: no MethodContext ");
            return null;
        }
        try {
            return cls.getMethod(methodname, context.getParameterClasses());
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        }
        if (REFLECT_DEBUG) System.out.println("getMethod failed: class: " + cls + " method: " + methodname);
        return null;
    }

    /**
     * 
     * @param choices
     * @param list
     * @return
     */
    private MethodContext getMethodContext(Class[][] choices, Object[] list) {
        int numchoice = choices.length;
        int listsize = list.length;
        SimpleStack vector = new SimpleStack();
        for (int i = numchoice; i-- > 0; ) if (choices[i].length == listsize) vector.push(choices[i]);
        int vectorsize = vector.size();
        if (vectorsize == 0) {
            if (REFLECT_DEBUG) System.out.println("getMethodContext failed: none of the choices had correct number of parameters");
            return null;
        }
        if (vectorsize == 1) return normalizeArgumentList((Class[]) vector.pop(), list);
        int maxvalue = 0;
        int maxindex = -1;
        for (int i = vectorsize; i-- > 0; ) {
            int current = scoreArguments((Class[]) vector.elementAt(i), list);
            if (current > maxvalue) {
                maxvalue = current;
                maxindex = i;
            }
        }
        if (maxindex == -1) {
            if (REFLECT_DEBUG) System.out.println("getMethodContext failed: argument list did not match any parameter list");
            return null;
        }
        return normalizeArgumentList((Class[]) vector.elementAt(maxindex), list);
    }

    /**
     * There is more than one choice.  Some arguments in 'list' may not yet have an exact type.
     * For example, the user may have provided a whole number where a double was expected:
     * new AffineTransform( 1, 0, 0, 0, 1, 0, 0, 0, 1 );
     * There are many cases where the argument list could feasibly apply to more than one parameter list.
     * Therefore, a bit of "fuzzy logic" will be employed:  the parameter list will be "scored":
     *
     * Numbers:
     * 
     * Exact Match
     * 	Definition: (expected <primative number class x>, found x)
     * 	Score: (list.length * 2) per match
     * 
     * Good Match
     * 	Definition: (expected float, found double)(expected (short|byte), found int)
     * 	Score: (list.length) per match
     * 
     * Feasible Match
     * 	Definition: (expected (double|float), found int)
     * 	Score: 1 per match
     * 
     * Mismatch
     * 	Definition: (expected (short|byte|int), found double)
     * 	Score: a total score of -1 (ruling the list out as a possibility)
     * 
     *
     * (Class|Interface):
     * 
     * Match
     * 	Definition: (expected <T extends Object>, found T)
     * 		(expected <? extends T>, found <? extends T>)
     * 		(expected <? implements I>, found <? implements I>)
     * 	Score: (list.length * 3) per match
     * 
     * Mismatch
     * 	Definition: (expected <? extends T>, found <? extends U)
     *  Score: a total score of -1 (ruling the list out as a possibility)
     * 
     * @param classes
     * @param list
     * @return
     */
    private int scoreArguments(Class[] classes, Object[] list) {
        int score = 0;
        int args = list.length;
        int INSTANCE_EXACT_MATCH = args * 3;
        int PRIMATIVE_EXACT_MATCH = args * 2;
        int PRIMATIVE_GOOD_MATCH = args;
        int PRIMATIVE_FEASIBLE_MATCH = 1;
        int MISMATCH = -1;
        for (int i = args; i-- > 0; ) {
            Class cls = classes[i];
            if (cls.isPrimitive()) {
                if (!(list[i] instanceof Primative)) return MISMATCH;
                Primative prim = (Primative) list[i];
                Object value = prim.getValue();
                if (cls == prim.TYPE) score += PRIMATIVE_EXACT_MATCH; else if (cls == boolean.class || prim.TYPE == boolean.class) return MISMATCH; else if (value instanceof Number) {
                    if ((prim.TYPE == double.class || prim.TYPE == float.class) && (cls == float.class || cls == double.class)) score += PRIMATIVE_GOOD_MATCH; else if ((prim.TYPE == byte.class || prim.TYPE == int.class || prim.TYPE == long.class || prim.TYPE == short.class) && (cls == byte.class || cls == int.class || cls == long.class || cls == short.class)) score += PRIMATIVE_GOOD_MATCH; else score += PRIMATIVE_FEASIBLE_MATCH;
                } else score += PRIMATIVE_FEASIBLE_MATCH;
            } else if (cls == String.class && list[i] instanceof Primative && ((Primative) list[i]).TYPE == char.class) score += INSTANCE_EXACT_MATCH; else if (cls.isInstance(list[i])) score += INSTANCE_EXACT_MATCH; else return MISMATCH;
        }
        return score;
    }
}
