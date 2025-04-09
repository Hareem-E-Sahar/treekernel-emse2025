package org.jtools.tmplc;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.jtools.tmpl.compiler.api.Function;
import org.jtools.tmpl.compiler.api.Functions;
import org.jtools.tmpl.compiler.api.Macro;
import org.jtools.tmpl.compiler.api.MacroContext;
import org.jtools.tmpl.compiler.api.Mandatory;
import org.jtools.tmpl.compiler.api.Output;
import org.jtools.tmpl.compiler.api.StatementStyle;
import org.jtools.util.StringUtils;

public final class MacroWrapper {

    private final Class<?> clazz;

    private boolean analysed = false;

    private Collection<MacroArgument> arguments;

    private Collection<String> outputs;

    private Method textsetter;

    private Method exec;

    private Collection<Signature> signatures;

    public MacroWrapper(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Object init(MacroContext resources) {
        analyse();
        try {
            Macro meta = clazz.getAnnotation(Macro.class);
            String init = meta == null ? "init" : meta.init();
            if (init != null) {
                if ("<init>".equals(init)) return clazz.getConstructor(MacroContext.class).newInstance(resources);
                Object result = clazz.newInstance();
                if (init.length() > 0) clazz.getMethod(init, MacroContext.class).invoke(result, resources);
                return result;
            }
            return clazz.newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * executes this macro.
     * <p>
     * @param stmt declaration of the statement that calls this macro.
     * @param args list of macro's arguments may be null or empty if the macro was called without arguments.
     * @param lines list of stmt lines that are not interpreted as arguments. may be null or empty if the macro was
     *            called without additional lines.
     * @param callback the interface to add this macro's replacements - created by calls to <code>stmt</code> - to.
     */
    private Object invoke(Method m, Object instance, Object... args) {
        try {
            return m.invoke(instance, args);
        } catch (InvocationTargetException e) {
            Throwable x = e.getCause();
            if (x instanceof RuntimeException) throw (RuntimeException) x;
            if (x instanceof Error) throw (Error) x;
            throw new RuntimeException(x);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void execute(Object instance, Map<MacroArgument, Object> args, String[] lines) {
        analyse();
        for (MacroArgument arg : arguments) {
            Object val = args.get(arg);
            if (val != null) invoke(arg.getSetter(), instance, val);
        }
        if (textsetter != null && lines != null) invoke(textsetter, instance, (Object) lines);
        invoke(exec, instance);
    }

    private String toArgName(String methodName) {
        if (methodName.length() < 4) return null;
        if (!methodName.startsWith("set")) return null;
        String name = methodName.substring(3);
        if (!name.substring(0, 1).equals(name.substring(0, 1).toUpperCase())) return null;
        return StringUtils.firstLowerCase(name);
    }

    private static final Class<?> stringArrayClass = new String[0].getClass();

    private MacroArgument toArgument(Method m) {
        String result = toArgName(m.getName());
        if (result == null) return null;
        if (Modifier.isStatic(m.getModifiers())) return null;
        if (m.getParameterTypes().length != 1) return null;
        if (String.class.equals(m.getParameterTypes()[0]) || stringArrayClass.equals(m.getParameterTypes()[0])) return new MacroArgument(m, result, m.isAnnotationPresent(Mandatory.class));
        return null;
    }

    private Method findTextMethod(String methodName) {
        try {
            return clazz.getMethod(methodName, Array.newInstance(String.class, 0).getClass());
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private void analyse() {
        if (analysed) return;
        Macro meta = clazz.getAnnotation(Macro.class);
        String txt = null;
        String ex = null;
        if (meta != null) {
            String[] langs = meta.output();
            if (langs.length > 0) outputs = Arrays.asList(langs);
            txt = meta.text();
            ex = meta.execute();
        }
        if (txt != null) {
            if (txt.length() > 0) {
                textsetter = findTextMethod(txt);
                if (textsetter == null) throw new RuntimeException("method " + txt + "(String[]) not found");
            }
        } else textsetter = findTextMethod("setText");
        if (ex == null) ex = "execute";
        try {
            exec = clazz.getMethod(ex);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        signatures = new HashSet<Signature>();
        Map<String, MacroArgument> argMap = getArgumentMap();
        arguments = argMap.values();
        signatures.add(getNamedSignature(argMap));
        Signature signature = getDefaultFunction(argMap);
        if (signature != null) signatures.add(signature);
        signature = getFunction(argMap, clazz.getAnnotation(Function.class));
        if (signature != null) signatures.add(signature);
        Functions functions = clazz.getAnnotation(Functions.class);
        if (functions != null) for (Function f : functions.value()) {
            signature = getFunction(argMap, f);
            if (signature != null) signatures.add(signature);
        }
        analysed = true;
    }

    private Signature getNamedSignature(Map<String, MacroArgument> argMap) {
        return new Signature(StatementStyle.XML, argMap.values());
    }

    private Signature getDefaultFunction(Map<String, MacroArgument> argMap) {
        if (argMap.size() <= 1) return new Signature(StatementStyle.FUNCTION, argMap.values());
        MacroArgument value = argMap.get("value");
        if (value != null) return new Signature(StatementStyle.FUNCTION, Collections.singleton(value));
        return null;
    }

    private Signature getFunction(Map<String, MacroArgument> argMap, Function meta) {
        if (meta == null) return null;
        MacroArgument ellipse = null;
        ArrayList<MacroArgument> args = new ArrayList<MacroArgument>();
        Set<String> names = new HashSet<String>();
        for (String name : meta.value()) {
            if (!names.add(name)) throw new RuntimeException("duplicate argument " + name);
            if (ellipse != null) throw new RuntimeException("argument " + ellipse.getName() + " is an ellipse, no more arguments are allowed after ellipse");
            MacroArgument arg = argMap.get(name);
            if (arg == null) throw new RuntimeException("argument " + name + " not found");
            if (arg.isEllipse()) ellipse = arg;
            args.add(arg);
        }
        return new Signature(StatementStyle.FUNCTION, args);
    }

    private Map<String, MacroArgument> getArgumentMap() {
        Map<String, MacroArgument> args = new TreeMap<String, MacroArgument>();
        for (Method m : clazz.getMethods()) {
            if (!m.equals(textsetter)) {
                MacroArgument arg = toArgument(m);
                if (arg != null) args.put(arg.getName(), arg);
            }
        }
        return args;
    }

    public Collection<MacroArgument> getArguments() {
        analyse();
        return arguments;
    }

    /**
     * gets a list of valid dest language names for this macro.
     * <p>
     * a macro may either be 'dest language dependend' or not. <br>
     * if a macro is dest language dependend it can only be used if the dest language's name of the template to be
     * compiled is in this list. <br>
     * if a macro is not dest language dependend it can be used by any dest language.
     * @return list of valid dest language names for this dest language dependend macro or null if this macro is not
     *         dest language dependend.
     * @see Output#getName Output.getName
     */
    public Collection<String> getDestLanguages() {
        analyse();
        return outputs;
    }

    public Collection<Signature> getSignatures() {
        analyse();
        return signatures;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj instanceof MacroWrapper) return clazz.equals(((MacroWrapper) obj).clazz);
        return false;
    }

    @Override
    public int hashCode() {
        return clazz.hashCode();
    }
}
