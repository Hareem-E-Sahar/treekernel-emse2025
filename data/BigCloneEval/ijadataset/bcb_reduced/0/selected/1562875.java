package org.jugile.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ""
 * 
 * @author jukka.rahkonen@iki.fi
 */
public class Proxy extends Jugile {

    private Object o;

    public Object getObject() {
        return o;
    }

    public Proxy(Object o) {
        this.o = o;
    }

    public Proxy() {
    }

    private Method cmethod;

    public void setMethod(Method m) {
        cmethod = m;
    }

    public Proxy o(Object o) {
        this.o = o;
        return this;
    }

    private String mname;

    public Proxy m(String methodname) {
        cmethod = null;
        mname = methodname;
        params = new ArrayList<Param>();
        return this;
    }

    private List<Param> params = new ArrayList<Param>();

    public Proxy p(Object p) {
        params.add(new Param(p));
        return this;
    }

    public Proxy p(Object p, Class<?> c) {
        params.add(new Param(p, c));
        return this;
    }

    public Proxy p(Class<?> c) {
        params.add(new Param(c));
        return this;
    }

    public Proxy attr(int idx, Object value) {
        Param p = params.get(idx);
        p.o = value;
        return this;
    }

    public Proxy attr(int idx, String value) {
        Param p = params.get(idx);
        p.instantiate(value);
        return this;
    }

    class Param {

        private Class<?> c;

        private Object o;

        public Class<?> getClazz() {
            return c;
        }

        public Object getObject() {
            return o;
        }

        public Param(Object p) {
            o = p;
            if (p.getClass() == Integer.class) c = int.class; else if (p.getClass() == Long.class) c = long.class; else if (p.getClass() == Float.class) c = float.class; else if (p.getClass() == Double.class) c = double.class; else if (p.getClass() == Boolean.class) c = boolean.class; else if (p.getClass() == Byte.class) c = byte.class; else if (p.getClass() == Character.class) c = char.class; else c = p.getClass();
        }

        public Param(Object p, Class<?> c) {
            o = p;
            this.c = c;
        }

        public Param(Class<?> c) {
            this.c = c;
        }

        public void instantiate(String str) {
            if ("null".equals(str)) {
                o = null;
                return;
            }
            try {
                if (c == int.class) {
                    o = Integer.parseInt(str);
                } else if (c == Integer.class) {
                    o = new Integer(str);
                } else {
                    Constructor con = c.getConstructor(String.class);
                    o = con.newInstance(str);
                }
            } catch (Exception e) {
                fail(e);
            }
        }
    }

    public Method getMethod(String name, Class[] classes) {
        if (classes == null) classes = new Class[0];
        try {
            return o.getClass().getMethod(name, classes);
        } catch (Exception e) {
            return null;
        }
    }

    public Object call() {
        int len = params.size();
        Class<?> classes[] = new Class[len];
        Object objects[] = new Object[len];
        int i = 0;
        for (Param p : params) {
            classes[i] = p.getClazz();
            objects[i] = p.getObject();
            i++;
        }
        try {
            if (cmethod != null) return cmethod.invoke(o, objects);
            Method m = o.getClass().getMethod(mname, classes);
            return m.invoke(o, objects);
        } catch (InvocationTargetException ie) {
            ie.printStackTrace();
            fail(ie.getCause());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e);
        }
        return null;
    }

    /**
	 * Find method from object and its superclass.
	 * @param obj
	 * @param name
	 * @param paramclasses
	 * @return
	 */
    public Method findMethod(Object obj, String name, Class<?>[] paramclasses) {
        if (paramclasses == null) paramclasses = new Class<?>[] {};
        Method m = null;
        try {
            m = obj.getClass().getMethod(name, paramclasses);
        } catch (NoSuchMethodException nsme) {
            try {
                m = obj.getClass().getSuperclass().getMethod(name, paramclasses);
            } catch (NoSuchMethodException nsme2) {
                for (Method m2 : obj.getClass().getSuperclass().getDeclaredMethods()) {
                    print("method: " + m2.getName() + " " + m2.getParameterTypes());
                }
            }
        }
        if (m == null) fail("method not found: " + obj.getClass().getName() + "." + name + "(" + classesToString(paramclasses) + ")");
        return m;
    }

    private String classesToString(Class[] pms) {
        Buffer buf = new Buffer();
        for (Class cl : pms) {
            buf.add(cl.getName());
            buf.add(",");
        }
        return buf.toString();
    }

    public List<Method> findMethods(Class retType, String nameStartsWith, Class[] param) {
        List<Method> res = new ArrayList<Method>();
        for (Method m : o.getClass().getMethods()) {
            if (!Modifier.isPublic(m.getModifiers())) continue;
            if (!m.getReturnType().equals(retType)) continue;
            if (!empty(nameStartsWith)) {
                if (!m.getName().startsWith(nameStartsWith)) continue;
            }
            if (param != null && param.length > 0) {
                if (!compareParams(m.getParameterTypes(), param)) continue;
            }
            res.add(m);
        }
        return res;
    }

    private Method method;

    public Method setMethod(String name) {
        cmethod = null;
        method = null;
        if (!empty(name)) {
            Class sup = o.getClass().getSuperclass();
            if (sup != null) {
                for (Method m : sup.getDeclaredMethods()) {
                    if (m.getName().equals(name)) {
                        method = m;
                        method.setAccessible(true);
                        return method;
                    }
                }
            }
            for (Method m : o.getClass().getDeclaredMethods()) {
                if (m.getName().equals(name)) {
                    method = m;
                    method.setAccessible(true);
                    return method;
                }
            }
        }
        fail("method not found: " + name);
        return method;
    }

    public Object callMethod() {
        Object[] objs = {};
        return callWithParams(objs);
    }

    public Object callWithParams(Object o1) {
        Object[] objs = { o1 };
        return callWithParams(objs);
    }

    public Object callWithParams(Object o1, Object o2) {
        Object[] objs = { o1, o2 };
        return callWithParams(objs);
    }

    public Object callWithParams(Object[] objects) {
        try {
            return method.invoke(o, objects);
        } catch (InvocationTargetException ie) {
            fail(ie.getCause() + " " + method.getName());
        } catch (Exception e) {
            fail(e + " " + method.getName());
        }
        return null;
    }

    private boolean compareParams(Class p1[], Class p2[]) {
        if (p1.length != p2.length) return false;
        for (int i = 0; i < p1.length; i++) {
            if (!p1[i].equals(p2[i])) return false;
        }
        return true;
    }

    public Object get(String key) {
        return get(key, null);
    }

    public Object get(String key, String nullResult) {
        return get(key.split("\\."), 0, nullResult);
    }

    private Object get(String keys[], int idx, String nullResult) {
        String key = keys[idx];
        Object res = null;
        if (o instanceof Map) {
            res = ((Map) o).get(key);
        } else if (o instanceof Vo) {
            res = ((Vo) o).get(key);
        } else {
            if (hasGetter("get" + up(key))) res = call("get" + up(key)); else if (!hasGetter("get")) fail("no property getter found: " + key); else res = m("get").p(key).call();
        }
        if (idx == keys.length - 1) {
            if (res == null) return nullResult;
            return res;
        }
        if (res == null) return nullResult;
        Proxy p = new Proxy(res);
        return p.get(keys, idx + 1, nullResult);
    }

    private boolean isBoCollection = false;

    private Map<String, Object> getters = null;

    public Map<String, Object> getGetters() {
        if (getters != null) return getters;
        getters = new HashMap<String, Object>();
        Method[] allMethods = o.getClass().getMethods();
        for (Method m : allMethods) {
            String mname = m.getName();
            if (mname.startsWith("get")) {
                Class<?> p[] = m.getParameterTypes();
                if (p.length == 0 && !"get".equals(mname)) {
                    getters.put(mname, m);
                }
                if (p.length == 1 && "get".equals(mname) && p[0] == String.class) {
                    getters.put(mname, m);
                }
            }
            if (mname.equals("list") && m.getParameterTypes().length == 0) {
                isBoCollection = true;
                getters.put(mname, m);
            }
        }
        return getters;
    }

    public boolean hasGetter(String key) {
        return (getGetters().get(key) != null);
    }

    public Object call(String mname) {
        return m(mname).call();
    }

    public Object call(String mname, Object p1) {
        return m(mname).p(p1).call();
    }

    public Object call(String mname, Object p1, Object p2) {
        return m(mname).p(p1).p(p2).call();
    }

    public Object call(String mname, Object p1, Object p2, Object p3) {
        return m(mname).p(p1).p(p2).p(p3).call();
    }

    public Object call(String mname, Object p1, Object p2, Object p3, Object p4) {
        return m(mname).p(p1).p(p2).p(p3).p(p4).call();
    }

    public Object call(List args) {
        if (params.size() == 0) {
            for (Object a : args) p(a.getClass());
        }
        for (int i = 0; i < args.size(); i++) {
            Param p = params.get(i);
            p.o = args.get(i);
        }
        return call();
    }

    public boolean isList() {
        getGetters();
        if (o instanceof List) return true;
        if (isBoCollection) return true;
        return false;
    }

    public List<?> getList() {
        getGetters();
        if (o instanceof List) return (List<?>) o;
        if (isBoCollection) return (List<?>) call("list");
        return null;
    }

    public static Object staticCall(String classname, String mname) {
        try {
            Class class1 = Class.forName(classname);
            Method method = class1.getMethod(mname);
            return method.invoke(null);
        } catch (Exception e) {
            fail(e);
            return null;
        }
    }

    public static Object staticCall(Class cl, String mname, Object o1) {
        try {
            Class[] types = { o1.getClass() };
            Object[] pms = { o1 };
            Method method = cl.getMethod(mname, types);
            return method.invoke(null, pms);
        } catch (Exception e) {
            fail(e);
            return null;
        }
    }

    public static Object staticCallPrivate(String classname, String mname) {
        try {
            Class class1 = Class.forName(classname);
            return staticCallPrivate(class1, mname);
        } catch (Exception e) {
            fail(e);
            return null;
        }
    }

    public static Object staticCallPrivate(Class clazz, String mname) {
        try {
            Method method = null;
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getName().equals(mname)) {
                    method = m;
                    method.setAccessible(true);
                }
            }
            if (method == null) fail("no static method found: " + clazz.getName() + " " + mname);
            return method.invoke(null);
        } catch (Exception e) {
            fail(e);
            return null;
        }
    }
}
