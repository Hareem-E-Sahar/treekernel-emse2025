package org.mozilla.javascript.tools.shell;

import java.io.*;
import java.net.*;
import java.lang.reflect.*;
import java.util.regex.*;
import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.ToolErrorReporter;
import org.mozilla.javascript.serialize.*;

/**
 * This class provides for sharing functions across multiple threads.
 * This is of particular interest to server applications.
 *
 * @author Norris Boyd
 */
public class Global extends ImporterTopLevel {

    static final long serialVersionUID = 4029130780977538005L;

    static final String envAskariHome = "ASKARI_HOME";

    NativeArray history;

    private InputStream inStream;

    private PrintStream outStream;

    private PrintStream errStream;

    private boolean sealedStdLib = false;

    boolean initialized;

    private QuitAction quitAction;

    public Global() {
    }

    public Global(Context cx) {
        init(cx);
    }

    /**
     * Set the action to call from quit().
     */
    public void initQuitAction(QuitAction quitAction) {
        if (quitAction == null) throw new IllegalArgumentException("quitAction is null");
        if (this.quitAction != null) throw new IllegalArgumentException("The method is once-call.");
        this.quitAction = quitAction;
    }

    public void init(ContextFactory factory) {
        factory.call(new ContextAction() {

            public Object run(Context cx) {
                init(cx);
                return null;
            }
        });
    }

    public void init(Context cx) {
        initStandardObjects(cx, sealedStdLib);
        String[] names = { "defineClass", "loadClass", "deserialize", "serialize", "include", "use", "load", "print", "readFile", "readUrl", "writeFile", "writeUrl", "runCommand", "seal", "spawn", "sync", "toint32", "version", "help", "quit" };
        defineFunctionProperties(names, Global.class, ScriptableObject.DONTENUM);
        Environment.defineClass(this);
        Environment environment = new Environment(this);
        defineProperty("environment", environment, ScriptableObject.DONTENUM);
        history = (NativeArray) cx.newArray(this, 0);
        defineProperty("history", history, ScriptableObject.DONTENUM);
        initialized = true;
    }

    /**
     * Print a help message.
     *
     * This method is defined as a JavaScript function.
     */
    public static void help(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        PrintStream out = getInstance(funObj).getOut();
        if (args.length > 0) {
            try {
                out.println(ToolErrorReporter.getMessage("msg.help." + args[0]));
            } catch (RuntimeException e) {
                out.println("Sorry, no help available for '" + args[0] + "'.");
            }
        } else {
            out.println(ToolErrorReporter.getMessage("msg.help"));
        }
    }

    /**
     * Print the string values of its arguments.
     *
     * This method is defined as a JavaScript function.
     * Note that its arguments are of the "varargs" form, which
     * allows it to handle an arbitrary number of arguments
     * supplied to the JavaScript function.
     *
     */
    public static Object print(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        PrintStream out = getInstance(funObj).getOut();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) out.print(" ");
            String s = Context.toString(args[i]);
            out.print(s);
        }
        out.println();
        return Context.getUndefinedValue();
    }

    /**
     * Call embedding-specific quit action passing its argument as
     * int32 exit code.
     *
     * This method is defined as a JavaScript function.
     */
    public static void quit(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        Global global = getInstance(funObj);
        if (global.quitAction != null) {
            int exitCode = (args.length == 0 ? 0 : ScriptRuntime.toInt32(args[0]));
            global.quitAction.quit(cx, exitCode);
        }
    }

    /**
     * Get and set the language version.
     *
     * This method is defined as a JavaScript function.
     */
    public static double version(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        double result = (double) cx.getLanguageVersion();
        if (args.length > 0) {
            double d = Context.toNumber(args[0]);
            cx.setLanguageVersion((int) d);
        }
        return result;
    }

    /**
     * Load and execute a set of JavaScript source files.
     *
     * This method is defined as a JavaScript function.
     *
     */
    public static void load(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        for (int i = 0; i < args.length; i++) {
            Main.processFile(cx, thisObj, Context.toString(args[i]));
        }
    }

    /**
     * Include standard Askari library source file.
     * Requires the ASKARI_HOME environment variable to be set.
     *
     * This method is defined as a JavaScript function.
     *
     */
    public static void include(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        if (args.length < 1) throw Context.reportRuntimeError("Must supply a class to load.");
        String useFile;
        Scriptable objProto;
        Scriptable newObj;
        useFile = Context.toString(args[0]);
        if (args.length > 1) objProto = ScriptRuntime.toObject(cx, thisObj, args[1]); else objProto = ScriptableObject.getObjectPrototype(thisObj);
        Scriptable currentObj = thisObj;
        Matcher m = Pattern.compile("([^\\.]+)\\.").matcher(useFile);
        while (m.find()) {
            Object result = currentObj.get(m.group(1), currentObj);
            try {
                if (result == NOT_FOUND) {
                    newObj = (Scriptable) new NativeObject();
                    newObj.setPrototype(objProto);
                    currentObj.put(m.group(1), currentObj, newObj);
                } else {
                    newObj = (Scriptable) result;
                }
                currentObj = newObj;
            } catch (ClassCastException e) {
                Context.reportRuntimeError("attempting to cast non-Object as Object.");
            }
        }
        Pattern p = Pattern.compile("[\\w-+@$&]+(\\.[\\w-+@$&]+)*");
        if (p.matcher(useFile).matches()) {
            useFile = useFile.replaceAll("\\.", "/");
            useFile = System.getenv(envAskariHome) + "/include/" + useFile + ".js";
        } else {
            throw Context.reportRuntimeError(args[0] + " is not a valid library name.");
        }
        try {
            Main.processFile(cx, thisObj, useFile);
        } catch (Exception e) {
            throw Context.reportRuntimeError("Could not load class " + args[0] + " from " + useFile + ".");
        }
    }

    /**
     * Defined specified variable if it is not defined in current scope.
     * Requires the ASKARI_HOME environment variable to be set.
     *
     * This method is defined as a JavaScript function.
     *
     */
    public static Object use(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        if (args.length < 1) throw Context.reportRuntimeError("Must supply a class to load.");
        String useFile;
        Scriptable objProto;
        Scriptable newObj;
        useFile = Context.toString(args[0]);
        if (args.length > 1) objProto = ScriptRuntime.toObject(cx, thisObj, args[1]); else objProto = ScriptableObject.getObjectPrototype(thisObj);
        Scriptable currentObj = thisObj;
        Matcher m = Pattern.compile("([^\\.]+)").matcher(useFile);
        while (m.find()) {
            Object result = currentObj.get(m.group(1), currentObj);
            try {
                if (result == NOT_FOUND) {
                    newObj = (Scriptable) new NativeObject();
                    newObj.setPrototype(objProto);
                    currentObj.put(m.group(1), currentObj, newObj);
                } else {
                    newObj = (Scriptable) result;
                }
                currentObj = newObj;
            } catch (ClassCastException e) {
                Context.reportRuntimeError("attempting to cast non-Object as Object.");
            }
        }
        return currentObj;
    }

    /**
     * Load a Java class that defines a JavaScript object using the
     * conventions outlined in ScriptableObject.defineClass.
     * <p>
     * This method is defined as a JavaScript function.
     * @exception IllegalAccessException if access is not available
     *            to a reflected class member
     * @exception InstantiationException if unable to instantiate
     *            the named class
     * @exception InvocationTargetException if an exception is thrown
     *            during execution of methods of the named class
     * @exception ClassDefinitionException if the format of the
     *            class causes this exception in ScriptableObject.defineClass
     * @see org.mozilla.javascript.ScriptableObject#defineClass
     */
    public static void defineClass(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        Class clazz = getClass(args);
        ScriptableObject.defineClass(thisObj, clazz);
    }

    /**
     * Load and execute a script compiled to a class file.
     * <p>
     * This method is defined as a JavaScript function.
     * When called as a JavaScript function, a single argument is
     * expected. This argument should be the name of a class that
     * implements the Script interface, as will any script
     * compiled by jsc.
     *
     * @exception IllegalAccessException if access is not available
     *            to the class
     * @exception InstantiationException if unable to instantiate
     *            the named class
     * @exception InvocationTargetException if an exception is thrown
     *            during execution of methods of the named class
     * @see org.mozilla.javascript.ScriptableObject#defineClass
     */
    public static void loadClass(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        Class clazz = getClass(args);
        if (!Script.class.isAssignableFrom(clazz)) {
            throw reportRuntimeError("msg.must.implement.Script");
        }
        Script script = (Script) clazz.newInstance();
        script.exec(cx, thisObj);
    }

    private static Class getClass(Object[] args) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        if (args.length == 0) {
            throw reportRuntimeError("msg.expected.string.arg");
        }
        Object arg0 = args[0];
        if (arg0 instanceof Wrapper) {
            Object wrapped = ((Wrapper) arg0).unwrap();
            if (wrapped instanceof Class) return (Class) wrapped;
        }
        String className = Context.toString(args[0]);
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException cnfe) {
            throw reportRuntimeError("msg.class.not.found", className);
        }
    }

    public static void serialize(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws IOException {
        if (args.length < 2) {
            throw Context.reportRuntimeError("Expected an object to serialize and a filename to write " + "the serialization to");
        }
        Object obj = args[0];
        String filename = Context.toString(args[1]);
        FileOutputStream fos = new FileOutputStream(filename);
        Scriptable scope = ScriptableObject.getTopLevelScope(thisObj);
        ScriptableOutputStream out = new ScriptableOutputStream(fos, scope);
        out.writeObject(obj);
        out.close();
    }

    public static Object deserialize(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws IOException, ClassNotFoundException {
        if (args.length < 1) {
            throw Context.reportRuntimeError("Expected a filename to read the serialization from");
        }
        String filename = Context.toString(args[0]);
        FileInputStream fis = new FileInputStream(filename);
        Scriptable scope = ScriptableObject.getTopLevelScope(thisObj);
        ObjectInputStream in = new ScriptableInputStream(fis, scope);
        Object deserialized = in.readObject();
        in.close();
        return Context.toObject(deserialized, scope);
    }

    /**
     * The spawn function runs a given function or script in a different
     * thread.
     *
     * js> function g() { a = 7; }
     * js> a = 3;
     * 3
     * js> spawn(g)
     * Thread[Thread-1,5,main]
     * js> a
     * 3
     */
    public static Object spawn(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        Scriptable scope = funObj.getParentScope();
        Runner runner;
        if (args.length != 0 && args[0] instanceof Function) {
            Object[] newArgs = null;
            if (args.length > 1 && args[1] instanceof Scriptable) {
                newArgs = cx.getElements((Scriptable) args[1]);
            }
            if (newArgs == null) {
                newArgs = ScriptRuntime.emptyArgs;
            }
            runner = new Runner(scope, (Function) args[0], newArgs);
        } else if (args.length != 0 && args[0] instanceof Script) {
            runner = new Runner(scope, (Script) args[0]);
        } else {
            throw reportRuntimeError("msg.spawn.args");
        }
        runner.factory = cx.getFactory();
        Thread thread = new Thread(runner);
        thread.start();
        return thread;
    }

    /**
     * The sync function creates a synchronized function (in the sense
     * of a Java synchronized method) from an existing function. The
     * new function synchronizes on the <code>this</code> object of
     * its invocation.
     * js> var o = { f : sync(function(x) {
     *       print("entry");
     *       Packages.java.lang.Thread.sleep(x*1000);
     *       print("exit");
     *     })};
     * js> spawn(function() {o.f(5);});
     * Thread[Thread-0,5,main]
     * entry
     * js> spawn(function() {o.f(5);});
     * Thread[Thread-1,5,main]
     * js>
     * exit
     * entry
     * exit
     */
    public static Object sync(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        if (args.length == 1 && args[0] instanceof Function) {
            return new Synchronizer((Function) args[0]);
        } else {
            throw reportRuntimeError("msg.sync.args");
        }
    }

    /**
     * Execute the specified command with the given argument and options
     * as a separate process and return the exit status of the process.
     * <p>
     * Usage:
     * <pre>
     * runCommand(command)
     * runCommand(command, arg1, ..., argN)
     * runCommand(command, arg1, ..., argN, options)
     * </pre>
     * All except the last arguments to runCommand are converted to strings
     * and denote command name and its arguments. If the last argument is a
     * JavaScript object, it is an option object. Otherwise it is converted to
     * string denoting the last argument and options objects assumed to be
     * empty.
     * Te following properties of the option object are processed:
     * <ul>
     * <li><tt>args</tt> - provides an array of additional command arguments
     * <li><tt>env</tt> - explicit environment object. All its enumeratable
     *   properties define the corresponding environment variable names.
     * <li><tt>input</tt> - the process input. If it is not
     *   java.io.InputStream, it is converted to string and sent to the process
     *   as its input. If not specified, no input is provided to the process.
     * <li><tt>output</tt> - the process output instead of
     *   java.lang.System.out. If it is not instance of java.io.OutputStream,
     *   the process output is read, converted to a string, appended to the
     *   output property value converted to string and put as the new value of
     *   the output property.
     * <li><tt>err</tt> - the process error output instead of
     *   java.lang.System.err. If it is not instance of java.io.OutputStream,
     *   the process error output is read, converted to a string, appended to
     *   the err property value converted to string and put as the new
     *   value of the err property.
     * </ul>
     */
    public static Object runCommand(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws IOException {
        int L = args.length;
        if (L == 0 || (L == 1 && args[0] instanceof Scriptable)) {
            throw reportRuntimeError("msg.runCommand.bad.args");
        }
        InputStream in = null;
        OutputStream out = null, err = null;
        ByteArrayOutputStream outBytes = null, errBytes = null;
        Object outObj = null, errObj = null;
        String[] environment = null;
        Scriptable params = null;
        Object[] addArgs = null;
        if (args[L - 1] instanceof Scriptable) {
            params = (Scriptable) args[L - 1];
            --L;
            Object envObj = ScriptableObject.getProperty(params, "env");
            if (envObj != Scriptable.NOT_FOUND) {
                if (envObj == null) {
                    environment = new String[0];
                } else {
                    if (!(envObj instanceof Scriptable)) {
                        throw reportRuntimeError("msg.runCommand.bad.env");
                    }
                    Scriptable envHash = (Scriptable) envObj;
                    Object[] ids = ScriptableObject.getPropertyIds(envHash);
                    environment = new String[ids.length];
                    for (int i = 0; i != ids.length; ++i) {
                        Object keyObj = ids[i], val;
                        String key;
                        if (keyObj instanceof String) {
                            key = (String) keyObj;
                            val = ScriptableObject.getProperty(envHash, key);
                        } else {
                            int ikey = ((Number) keyObj).intValue();
                            key = Integer.toString(ikey);
                            val = ScriptableObject.getProperty(envHash, ikey);
                        }
                        if (val == ScriptableObject.NOT_FOUND) {
                            val = Undefined.instance;
                        }
                        environment[i] = key + '=' + ScriptRuntime.toString(val);
                    }
                }
            }
            Object inObj = ScriptableObject.getProperty(params, "input");
            if (inObj != Scriptable.NOT_FOUND) {
                in = toInputStream(inObj);
            }
            outObj = ScriptableObject.getProperty(params, "output");
            if (outObj != Scriptable.NOT_FOUND) {
                out = toOutputStream(outObj);
                if (out == null) {
                    outBytes = new ByteArrayOutputStream();
                    out = outBytes;
                }
            }
            errObj = ScriptableObject.getProperty(params, "err");
            if (errObj != Scriptable.NOT_FOUND) {
                err = toOutputStream(errObj);
                if (err == null) {
                    errBytes = new ByteArrayOutputStream();
                    err = errBytes;
                }
            }
            Object addArgsObj = ScriptableObject.getProperty(params, "args");
            if (addArgsObj != Scriptable.NOT_FOUND) {
                Scriptable s = Context.toObject(addArgsObj, getTopLevelScope(thisObj));
                addArgs = cx.getElements(s);
            }
        }
        Global global = getInstance(funObj);
        if (out == null) {
            out = (global != null) ? global.getOut() : System.out;
        }
        if (err == null) {
            err = (global != null) ? global.getErr() : System.err;
        }
        String[] cmd = new String[(addArgs == null) ? L : L + addArgs.length];
        for (int i = 0; i != L; ++i) {
            cmd[i] = ScriptRuntime.toString(args[i]);
        }
        if (addArgs != null) {
            for (int i = 0; i != addArgs.length; ++i) {
                cmd[L + i] = ScriptRuntime.toString(addArgs[i]);
            }
        }
        int exitCode = runProcess(cmd, environment, in, out, err);
        if (outBytes != null) {
            String s = ScriptRuntime.toString(outObj) + outBytes.toString();
            ScriptableObject.putProperty(params, "output", s);
        }
        if (errBytes != null) {
            String s = ScriptRuntime.toString(errObj) + errBytes.toString();
            ScriptableObject.putProperty(params, "err", s);
        }
        return new Integer(exitCode);
    }

    /**
     * The seal function seals all supplied arguments.
     */
    public static void seal(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        for (int i = 0; i != args.length; ++i) {
            Object arg = args[i];
            if (!(arg instanceof ScriptableObject) || arg == Undefined.instance) {
                if (!(arg instanceof Scriptable) || arg == Undefined.instance) {
                    throw reportRuntimeError("msg.shell.seal.not.object");
                } else {
                    throw reportRuntimeError("msg.shell.seal.not.scriptable");
                }
            }
        }
        for (int i = 0; i != args.length; ++i) {
            Object arg = args[i];
            ((ScriptableObject) arg).sealObject();
        }
    }

    /**
     * The readFile reads the given file context and convert it to a string
     * using the specified character coding or default character coding if
     * explicit coding argument is not given.
     * <p>
     * Usage:
     * <pre>
     * readFile(filePath)
     * readFile(filePath, charCoding)
     * </pre>
     * The first form converts file's context to string using the default
     * character coding.
     */
    public static Object readFile(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws IOException {
        if (args.length == 0) {
            throw reportRuntimeError("msg.shell.readFile.bad.args");
        }
        String path = ScriptRuntime.toString(args[0]);
        String charCoding = null;
        if (args.length >= 2) {
            charCoding = ScriptRuntime.toString(args[1]);
        }
        return readUrl(path, charCoding, true);
    }

    /**
     * The readUrl opens connection to the given URL, read all its data
     * and converts them to a string
     * using the specified character coding or default character coding if
     * explicit coding argument is not given.
     * <p>
     * Usage:
     * <pre>
     * readUrl(url)
     * readUrl(url, charCoding)
     * </pre>
     * The first form converts file's context to string using the default
     * charCoding.
     */
    public static Object readUrl(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws IOException {
        if (args.length == 0) {
            throw reportRuntimeError("msg.shell.readUrl.bad.args");
        }
        String url = ScriptRuntime.toString(args[0]);
        String charCoding = null;
        if (args.length >= 2) {
            charCoding = ScriptRuntime.toString(args[1]);
        }
        return readUrl(url, charCoding, false);
    }

    /**
     * writeFile writes the supplied string to the given file context 
     * using the specified character coding or default character coding if
     * explicit coding argument is not given.
     * <p>
     * Usage:
     * <pre>
     * writeFile(filePath, data)
     * writeFile(filePath, data, charCoding)
     * </pre>
     * The first form converts string using the default charCoding
     * before writing to file.
     */
    public static void writeFile(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws IOException {
        if (args.length < 2) {
            throw reportRuntimeError("msg.shell.writeFile.bad.args");
        }
        String path = ScriptRuntime.toString(args[0]);
        String data = ScriptRuntime.toString(args[1]);
        String charCoding = null;
        if (args.length >= 3) {
            charCoding = ScriptRuntime.toString(args[2]);
        }
        writeUrl(path, data, charCoding, true);
    }

    /**
     * writeUrl writes the supplied string to the given URL
     * using the specified character coding or default character coding if
     * explicit coding argument is not given.
     * <p>
     * Usage:
     * <pre>
     * writeUrl(url, data)
     * writeUrl(url, data, charCoding)
     * </pre>
     * The first form converts string using the default charCoding
     * before writing to file.
     */
    public static void writeUrl(Context cx, Scriptable thisObj, Object[] args, Function funObj) throws IOException {
        if (args.length < 2) {
            throw reportRuntimeError("msg.shell.writeUrl.bad.args");
        }
        String url = ScriptRuntime.toString(args[0]);
        String data = ScriptRuntime.toString(args[1]);
        String charCoding = null;
        if (args.length >= 3) {
            charCoding = ScriptRuntime.toString(args[2]);
        }
        writeUrl(url, data, charCoding, false);
    }

    /**
     * Convert the argument to int32 number.
     */
    public static Object toint32(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        Object arg = (args.length != 0 ? args[0] : Undefined.instance);
        if (arg instanceof Integer) return arg;
        return ScriptRuntime.wrapInt(ScriptRuntime.toInt32(arg));
    }

    public InputStream getIn() {
        return inStream == null ? System.in : inStream;
    }

    public void setIn(InputStream in) {
        inStream = in;
    }

    public PrintStream getOut() {
        return outStream == null ? System.out : outStream;
    }

    public void setOut(PrintStream out) {
        outStream = out;
    }

    public PrintStream getErr() {
        return errStream == null ? System.err : errStream;
    }

    public void setErr(PrintStream err) {
        errStream = err;
    }

    public void setSealedStdLib(boolean value) {
        sealedStdLib = value;
    }

    private static Global getInstance(Function function) {
        Scriptable scope = function.getParentScope();
        if (!(scope instanceof Global)) throw reportRuntimeError("msg.bad.shell.function.scope", String.valueOf(scope));
        return (Global) scope;
    }

    /**
     * If any of in, out, err is null, the corresponding process stream will
     * be closed immediately, otherwise it will be closed as soon as
     * all data will be read from/written to process
     */
    private static int runProcess(String[] cmd, String[] environment, InputStream in, OutputStream out, OutputStream err) throws IOException {
        Process p;
        if (environment == null) {
            p = Runtime.getRuntime().exec(cmd);
        } else {
            p = Runtime.getRuntime().exec(cmd, environment);
        }
        PipeThread inThread = null, errThread = null;
        try {
            InputStream errProcess = null;
            try {
                if (err != null) {
                    errProcess = p.getErrorStream();
                } else {
                    p.getErrorStream().close();
                }
                InputStream outProcess = null;
                try {
                    if (out != null) {
                        outProcess = p.getInputStream();
                    } else {
                        p.getInputStream().close();
                    }
                    OutputStream inProcess = null;
                    try {
                        if (in != null) {
                            inProcess = p.getOutputStream();
                        } else {
                            p.getOutputStream().close();
                        }
                        if (out != null) {
                            if (err != null) {
                                errThread = new PipeThread(true, errProcess, err);
                                errThread.start();
                            }
                            if (in != null) {
                                inThread = new PipeThread(false, in, inProcess);
                                inThread.start();
                            }
                            pipe(true, outProcess, out);
                        } else if (in != null) {
                            if (err != null) {
                                errThread = new PipeThread(true, errProcess, err);
                                errThread.start();
                            }
                            pipe(false, in, inProcess);
                            in.close();
                        } else if (err != null) {
                            pipe(true, errProcess, err);
                            errProcess.close();
                            errProcess = null;
                        }
                        for (; ; ) {
                            try {
                                p.waitFor();
                                break;
                            } catch (InterruptedException ex) {
                            }
                        }
                        return p.exitValue();
                    } finally {
                        if (inProcess != null) {
                            inProcess.close();
                        }
                    }
                } finally {
                    if (outProcess != null) {
                        outProcess.close();
                    }
                }
            } finally {
                if (errProcess != null) {
                    errProcess.close();
                }
            }
        } finally {
            p.destroy();
            if (inThread != null) {
                for (; ; ) {
                    try {
                        inThread.join();
                        break;
                    } catch (InterruptedException ex) {
                    }
                }
            }
            if (errThread != null) {
                for (; ; ) {
                    try {
                        errThread.join();
                        break;
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }
    }

    static void pipe(boolean fromProcess, InputStream from, OutputStream to) throws IOException {
        try {
            final int SIZE = 4096;
            byte[] buffer = new byte[SIZE];
            for (; ; ) {
                int n;
                if (!fromProcess) {
                    n = from.read(buffer, 0, SIZE);
                } else {
                    try {
                        n = from.read(buffer, 0, SIZE);
                    } catch (IOException ex) {
                        break;
                    }
                }
                if (n < 0) {
                    break;
                }
                if (fromProcess) {
                    to.write(buffer, 0, n);
                    to.flush();
                } else {
                    try {
                        to.write(buffer, 0, n);
                        to.flush();
                    } catch (IOException ex) {
                        break;
                    }
                }
            }
        } finally {
            try {
                if (fromProcess) {
                    from.close();
                } else {
                    to.close();
                }
            } catch (IOException ex) {
            }
        }
    }

    private static InputStream toInputStream(Object value) throws IOException {
        InputStream is = null;
        String s = null;
        if (value instanceof Wrapper) {
            Object unwrapped = ((Wrapper) value).unwrap();
            if (unwrapped instanceof InputStream) {
                is = (InputStream) unwrapped;
            } else if (unwrapped instanceof byte[]) {
                is = new ByteArrayInputStream((byte[]) unwrapped);
            } else if (unwrapped instanceof Reader) {
                s = readReader((Reader) unwrapped);
            } else if (unwrapped instanceof char[]) {
                s = new String((char[]) unwrapped);
            }
        }
        if (is == null) {
            if (s == null) {
                s = ScriptRuntime.toString(value);
            }
            is = new ByteArrayInputStream(s.getBytes());
        }
        return is;
    }

    private static OutputStream toOutputStream(Object value) {
        OutputStream os = null;
        if (value instanceof Wrapper) {
            Object unwrapped = ((Wrapper) value).unwrap();
            if (unwrapped instanceof OutputStream) {
                os = (OutputStream) unwrapped;
            }
        }
        return os;
    }

    private static String readUrl(String filePath, String charCoding, boolean urlIsFile) throws IOException {
        int chunkLength;
        InputStream is = null;
        try {
            if (!urlIsFile) {
                URL urlObj = new URL(filePath);
                URLConnection uc = urlObj.openConnection();
                is = uc.getInputStream();
                chunkLength = uc.getContentLength();
                if (chunkLength <= 0) chunkLength = 1024;
                if (charCoding == null) {
                    String type = uc.getContentType();
                    if (type != null) {
                        charCoding = getCharCodingFromType(type);
                    }
                }
            } else {
                File f = new File(filePath);
                long length = f.length();
                chunkLength = (int) length;
                if (chunkLength != length) throw new IOException("Too big file size: " + length);
                if (chunkLength == 0) {
                    return "";
                }
                is = new FileInputStream(f);
            }
            Reader r;
            if (charCoding == null) {
                r = new InputStreamReader(is);
            } else {
                r = new InputStreamReader(is, charCoding);
            }
            return readReader(r, chunkLength);
        } finally {
            if (is != null) is.close();
        }
    }

    private static void writeUrl(String filePath, String data, String charCoding, boolean urlIsFile) throws IOException {
        int chunkLength;
        OutputStream os = null;
        try {
            if (!urlIsFile) {
                URL urlObj = new URL(filePath);
                URLConnection uc = urlObj.openConnection();
                os = uc.getOutputStream();
                if (charCoding == null) {
                    String type = uc.getContentType();
                    if (type != null) {
                        charCoding = getCharCodingFromType(type);
                    }
                }
            } else {
                File f = new File(filePath);
                os = new FileOutputStream(f);
            }
            Writer w;
            if (charCoding == null) {
                w = new OutputStreamWriter(os);
            } else {
                w = new OutputStreamWriter(os, charCoding);
            }
            w.write(data);
            w.flush();
        } finally {
            if (os != null) os.close();
        }
    }

    private static String getCharCodingFromType(String type) {
        int i = type.indexOf(';');
        if (i >= 0) {
            int end = type.length();
            ++i;
            while (i != end && type.charAt(i) <= ' ') {
                ++i;
            }
            String charset = "charset";
            if (charset.regionMatches(true, 0, type, i, charset.length())) {
                i += charset.length();
                while (i != end && type.charAt(i) <= ' ') {
                    ++i;
                }
                if (i != end && type.charAt(i) == '=') {
                    ++i;
                    while (i != end && type.charAt(i) <= ' ') {
                        ++i;
                    }
                    if (i != end) {
                        while (type.charAt(end - 1) <= ' ') {
                            --end;
                        }
                        return type.substring(i, end);
                    }
                }
            }
        }
        return null;
    }

    private static String readReader(Reader reader) throws IOException {
        return readReader(reader, 4096);
    }

    private static String readReader(Reader reader, int initialBufferSize) throws IOException {
        char[] buffer = new char[initialBufferSize];
        int offset = 0;
        for (; ; ) {
            int n = reader.read(buffer, offset, buffer.length - offset);
            if (n < 0) {
                break;
            }
            offset += n;
            if (offset == buffer.length) {
                char[] tmp = new char[buffer.length * 2];
                System.arraycopy(buffer, 0, tmp, 0, offset);
                buffer = tmp;
            }
        }
        return new String(buffer, 0, offset);
    }

    static RuntimeException reportRuntimeError(String msgId) {
        String message = ToolErrorReporter.getMessage(msgId);
        return Context.reportRuntimeError(message);
    }

    static RuntimeException reportRuntimeError(String msgId, String msgArg) {
        String message = ToolErrorReporter.getMessage(msgId, msgArg);
        return Context.reportRuntimeError(message);
    }
}

class Runner implements Runnable, ContextAction {

    Runner(Scriptable scope, Function func, Object[] args) {
        this.scope = scope;
        f = func;
        this.args = args;
    }

    Runner(Scriptable scope, Script script) {
        this.scope = scope;
        s = script;
    }

    public void run() {
        factory.call(this);
    }

    public Object run(Context cx) {
        if (f != null) return f.call(cx, scope, scope, args); else return s.exec(cx, scope);
    }

    ContextFactory factory;

    private Scriptable scope;

    private Function f;

    private Script s;

    private Object[] args;
}

class PipeThread extends Thread {

    PipeThread(boolean fromProcess, InputStream from, OutputStream to) {
        setDaemon(true);
        this.fromProcess = fromProcess;
        this.from = from;
        this.to = to;
    }

    public void run() {
        try {
            Global.pipe(fromProcess, from, to);
        } catch (IOException ex) {
            throw Context.throwAsScriptRuntimeEx(ex);
        }
    }

    private boolean fromProcess;

    private InputStream from;

    private OutputStream to;
}
