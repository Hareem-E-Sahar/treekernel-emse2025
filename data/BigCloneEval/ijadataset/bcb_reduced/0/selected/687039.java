package tcl.lang;

import tcl.lang.reflect.PkgInvoker;
import java.util.*;
import java.beans.*;
import java.lang.reflect.*;

/**
 * This class implements the built-in "java::info" command in Tcl.
 */
class JavaInfoCmd implements Command {

    private static final String validCmds[] = { "class", "baseclass", "dimensions", "events", "fields", "methods", "constructors", "properties", "superclass" };

    private static final int CLASS = 0;

    private static final int BASECLASS = 1;

    private static final int DIMENSIONS = 2;

    private static final int EVENTS = 3;

    private static final int FIELDS = 4;

    private static final int METHODS = 5;

    private static final int CONSTRUCTORS = 6;

    private static final int PROPERTIES = 7;

    private static final int SUPERCLASS = 8;

    private static final String propOpts[] = { "-type" };

    private static final String methOpts[] = { "-type", "-static" };

    static final int TYPE_OPT = 0;

    static final int STATIC_OPT = 1;

    public void cmdProc(Interp interp, TclObject argv[]) throws TclException {
        int lastArg = argv.length - 1;
        boolean statOpt = false;
        boolean typeOpt = false;
        TclObject resultListObj;
        Class c;
        if (argv.length < 2) {
            throw new TclNumArgsException(interp, 1, argv, "option ?arg arg ...?");
        }
        int opt = TclIndex.get(interp, argv[1], validCmds, "option", 0);
        switch(opt) {
            case BASECLASS:
                if (argv.length != 3) {
                    throw new TclNumArgsException(interp, 2, argv, "objOrClass");
                }
                c = getClassFromObj(interp, argv[2]);
                if (c != null) {
                    interp.setResult(getBaseNameFromClass(c));
                }
                return;
            case CLASS:
                if (argv.length != 3) {
                    throw new TclNumArgsException(interp, 2, argv, "javaObj");
                }
                c = ReflectObject.getClass(interp, argv[2]);
                if (c != null) {
                    interp.setResult(getNameFromClass(c));
                }
                return;
            case DIMENSIONS:
                if (argv.length != 3) {
                    throw new TclNumArgsException(interp, 2, argv, "objOrClass");
                }
                c = getClassFromObj(interp, argv[2]);
                if (c == null) {
                    interp.setResult(0);
                } else {
                    interp.setResult(getNumDimsFromClass(c));
                }
                return;
            case EVENTS:
                if (argv.length != 3) {
                    throw new TclNumArgsException(interp, 2, argv, "javaObj");
                }
                c = getClassFromObj(interp, argv[2]);
                if (c == null) {
                    interp.resetResult();
                    return;
                }
                if (!PkgInvoker.isAccessible(c)) {
                    JavaInvoke.notAccessibleError(interp, c);
                }
                lookup: {
                    BeanInfo beanInfo;
                    try {
                        beanInfo = Introspector.getBeanInfo(c);
                    } catch (IntrospectionException e) {
                        break lookup;
                    }
                    EventSetDescriptor events[] = beanInfo.getEventSetDescriptors();
                    if (events == null) {
                        break lookup;
                    }
                    TclObject list = TclList.newInstance();
                    for (int i = 0; i < events.length; i++) {
                        TclList.append(interp, list, TclString.newInstance(getNameFromClass(events[i].getListenerType())));
                    }
                    interp.setResult(list);
                    return;
                }
                interp.resetResult();
                return;
            case FIELDS:
                if ((lastArg < 2) || (lastArg > 4)) {
                    throw new TclNumArgsException(interp, 2, argv, "?-type? ?-static? objOrClass");
                }
                for (int i = 2; i < lastArg; i++) {
                    opt = TclIndex.get(interp, argv[i], methOpts, "option", 0);
                    switch(opt) {
                        case STATIC_OPT:
                            statOpt = true;
                            break;
                        case TYPE_OPT:
                            typeOpt = true;
                            break;
                    }
                }
                c = getClassFromObj(interp, argv[lastArg]);
                if (c != null) {
                    if (!PkgInvoker.isAccessible(c)) {
                        JavaInvoke.notAccessibleError(interp, c);
                    }
                    resultListObj = getFieldInfoList(interp, c, statOpt, typeOpt);
                    interp.setResult(resultListObj);
                }
                return;
            case METHODS:
                if ((lastArg < 2) || (lastArg > 4)) {
                    throw new TclNumArgsException(interp, 2, argv, "?-type? ?-static? objOrClass");
                }
                for (int i = 2; i < lastArg; i++) {
                    opt = TclIndex.get(interp, argv[i], methOpts, "option", 0);
                    switch(opt) {
                        case STATIC_OPT:
                            statOpt = true;
                            break;
                        case TYPE_OPT:
                            typeOpt = true;
                            break;
                    }
                }
                c = getClassFromObj(interp, argv[lastArg]);
                if (c != null) {
                    if (!PkgInvoker.isAccessible(c)) {
                        JavaInvoke.notAccessibleError(interp, c);
                    }
                    resultListObj = getMethodInfoList(interp, c, statOpt, typeOpt);
                    interp.setResult(resultListObj);
                }
                return;
            case CONSTRUCTORS:
                if (argv.length != 3) {
                    throw new TclNumArgsException(interp, 2, argv, "objOrClass");
                }
                c = getClassFromObj(interp, argv[lastArg]);
                if (c != null) {
                    if (!PkgInvoker.isAccessible(c)) {
                        JavaInvoke.notAccessibleError(interp, c);
                    }
                    resultListObj = getConstructorInfoList(interp, c);
                    interp.setResult(resultListObj);
                }
                return;
            case PROPERTIES:
                if ((lastArg < 2) || (lastArg > 3)) {
                    throw new TclNumArgsException(interp, 2, argv, "?-type? objOrClass");
                }
                if (lastArg == 3) {
                    opt = TclIndex.get(interp, argv[2], propOpts, "option", 0);
                    typeOpt = true;
                }
                c = getClassFromObj(interp, argv[lastArg]);
                if (c != null) {
                    if (!PkgInvoker.isAccessible(c)) {
                        JavaInvoke.notAccessibleError(interp, c);
                    }
                    resultListObj = getPropInfoList(interp, c, typeOpt);
                    interp.setResult(resultListObj);
                }
                return;
            case SUPERCLASS:
                if (argv.length != 3) {
                    throw new TclNumArgsException(interp, 2, argv, "objOrClass");
                }
                c = getClassFromObj(interp, argv[2]);
                interp.resetResult();
                if (c != null) {
                    c = c.getSuperclass();
                    if (c != null) {
                        interp.setResult(getNameFromClass(c));
                    }
                }
                return;
        }
    }

    private static Class getClassFromObj(Interp interp, TclObject objOrClass) throws TclException {
        Class c;
        try {
            c = ReflectObject.getClass(interp, objOrClass);
        } catch (TclException e) {
            try {
                c = ClassRep.get(interp, objOrClass);
            } catch (TclException e2) {
                throw new TclException(interp, "unknown java class or object \"" + objOrClass + "\"");
            }
        }
        return c;
    }

    private static TclObject getPropInfoList(Interp interp, Class c, boolean typeOpt) throws TclException {
        BeanInfo beaninfo;
        try {
            beaninfo = Introspector.getBeanInfo(c);
        } catch (IntrospectionException e) {
            throw new TclException(interp, e.toString());
        }
        PropertyDescriptor propDesc[] = null;
        propDesc = beaninfo.getPropertyDescriptors();
        TclObject resultListObj = TclList.newInstance();
        TclObject elementObj, pairObj;
        for (int i = 0; i < propDesc.length; i++) {
            pairObj = TclList.newInstance();
            if (typeOpt) {
                elementObj = TclString.newInstance(getNameFromClass(propDesc[i].getPropertyType()));
                if (elementObj != null) {
                    TclList.append(interp, pairObj, elementObj);
                }
            }
            elementObj = TclString.newInstance(propDesc[i].getName());
            TclList.append(interp, pairObj, elementObj);
            TclList.append(interp, resultListObj, pairObj);
        }
        return resultListObj;
    }

    private static TclObject getFieldInfoList(Interp interp, Class c, boolean statOpt, boolean typeOpt) throws TclException {
        Field[] fieldArray = FieldSig.getAccessibleFields(c);
        TclObject resultListObj = TclList.newInstance();
        TclObject elementObj, sigObj, pairObj;
        Class declClass;
        for (int f = 0; f < fieldArray.length; ++f) {
            boolean isStatic = ((fieldArray[f].getModifiers() & Modifier.STATIC) > 0);
            if (isStatic == statOpt) {
                sigObj = TclList.newInstance();
                String fieldName = fieldArray[f].getName();
                elementObj = TclString.newInstance(fieldName);
                TclList.append(interp, sigObj, elementObj);
                declClass = fieldArray[f].getDeclaringClass();
                if (!declClass.equals(c)) {
                    for (int i = 0; i < fieldArray.length; ++i) {
                        if (i == f) {
                            continue;
                        }
                        if (!fieldName.equals(fieldArray[i].getName())) {
                            continue;
                        }
                        Class tmpClass = fieldArray[i].getDeclaringClass();
                        if (declClass.isAssignableFrom(tmpClass)) {
                            elementObj = TclString.newInstance(getNameFromClass(declClass));
                            TclList.append(interp, sigObj, elementObj);
                            break;
                        }
                    }
                }
                if (typeOpt) {
                    pairObj = TclList.newInstance();
                    elementObj = TclString.newInstance(getNameFromClass(fieldArray[f].getType()));
                    TclList.append(interp, pairObj, elementObj);
                    TclList.append(interp, pairObj, sigObj);
                    TclList.append(interp, resultListObj, pairObj);
                } else {
                    TclList.append(interp, resultListObj, sigObj);
                }
            }
        }
        return resultListObj;
    }

    private static TclObject getMethodInfoList(Interp interp, Class c, boolean statOpt, boolean typeOpt) throws TclException {
        Method[] methodArray;
        if (statOpt) {
            methodArray = FuncSig.getAccessibleStaticMethods(c);
        } else {
            methodArray = FuncSig.getAccessibleInstanceMethods(c);
        }
        TclObject resultListObj = TclList.newInstance();
        TclObject elementObj, sigObj;
        for (int m = 0; m < methodArray.length; ++m) {
            if (true) {
                sigObj = TclList.newInstance();
                elementObj = TclString.newInstance(methodArray[m].getName());
                TclList.append(interp, sigObj, elementObj);
                Class[] paramArray = methodArray[m].getParameterTypes();
                for (int p = 0; p < paramArray.length; ++p) {
                    elementObj = TclString.newInstance(getNameFromClass(paramArray[p]));
                    TclList.append(interp, sigObj, elementObj);
                }
                if (typeOpt) {
                    TclObject sublist = TclList.newInstance();
                    TclObject exceptions = TclList.newInstance();
                    Class ex[] = methodArray[m].getExceptionTypes();
                    for (int i = 0; i < ex.length; i++) {
                        TclList.append(interp, exceptions, TclString.newInstance(getNameFromClass(ex[i])));
                    }
                    TclList.append(interp, sublist, TclString.newInstance(getNameFromClass(methodArray[m].getReturnType())));
                    TclList.append(interp, sublist, sigObj);
                    TclList.append(interp, sublist, exceptions);
                    TclList.append(interp, resultListObj, sublist);
                } else {
                    TclList.append(interp, resultListObj, sigObj);
                }
            }
        }
        return resultListObj;
    }

    private static TclObject getConstructorInfoList(Interp interp, Class c) throws TclException {
        Constructor[] constructorArray = FuncSig.getAccessibleConstructors(c);
        TclObject resultListObj = TclList.newInstance();
        TclObject elementObj, sigObj;
        for (int m = 0; m < constructorArray.length; ++m) {
            sigObj = TclList.newInstance();
            elementObj = TclString.newInstance(constructorArray[m].getName());
            TclList.append(interp, sigObj, elementObj);
            Class[] paramArray = constructorArray[m].getParameterTypes();
            for (int p = 0; p < paramArray.length; ++p) {
                elementObj = TclString.newInstance(getNameFromClass(paramArray[p]));
                TclList.append(interp, sigObj, elementObj);
            }
            TclList.append(interp, resultListObj, sigObj);
        }
        return resultListObj;
    }

    static int getNumDimsFromClass(Class type) {
        int dim;
        for (dim = 0; type.isArray(); dim++) {
            type = type.getComponentType();
        }
        return dim;
    }

    static String getNameFromClass(Class type) {
        StringBuffer name = new StringBuffer();
        while (type.isArray()) {
            name.append("[]");
            type = type.getComponentType();
        }
        String className = type.getName().replace('$', '.');
        name.insert(0, className);
        return name.toString();
    }

    private static String getBaseNameFromClass(Class type) {
        while (type.isArray()) {
            type = type.getComponentType();
        }
        return type.getName().toString().replace('$', '.');
    }
}
