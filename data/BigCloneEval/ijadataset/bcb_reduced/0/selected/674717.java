package DE.FhG.IGD.util;

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

/**
 * Supports various forms of command line argument parsing.
 * This class provides powerful tools for parsing command line
 * input as is required e.g. by a shell. Basically, two types
 * of support are unified in this class
 * <ul>
 * <li> static methods for pre-processing a single input
 *   line, and post-processing of an array of shell tokens
 *   with regard to aliasing and pipeline support.
 * <li> instance methods for declaring typed options, and
 *   for parsing and retrieving values assigned to options.
 * </ul>
 * Options are recognized on the command line by a leading dash.
 * Each option that shall be recognized must be declared upon
 * creation of a parser instance. An option declaration consists
 * of an option name and the short name of the option type,
 * separated by a colon. The short name refers to a basic option
 * type. The parser also supports array types of the basic types.
 * Array types are declared by appending a &quot;[&quot; to the
 * short name of the basic option type.<p>
 *
 * Short names of option option types are defined in a file that
 * must be in the <code>CLASSPATH</code>. This file has the name
 * defined by {@link #ARGS_MAP ARGS_MAP}. The type mapping can
 * be extended easily with additional type mappings. The only
 * requirement for classes that implement types is that they
 * have a constructor that takes a single string as its
 * parameter.<p>
 *
 * When adding type mappings, bear in mind that the short names
 * of types in general will be compiled into classes that use
 * this parser. Modification of existing mappings will likely
 * disrupt the functioning of existing classes. The type map
 * thus should be regarded as if it was hard-coded into this
 * class. Future versions of this class might support setting
 * custom type mappings for instances that override the map in
 * <code>ARGS_MAP</code>.<p>
 *
 * Two flavours of resolving are also supported. Firstly, the
 * pre-processing step of &quot;raw&quot; input lines can
 * contain <i>variables</i>. A variable is defined by a leading
 * dollar sign (&quot;$&quot;) followed by the variable name in
 * curly braces, e.g. &quot;${home}&quot;. A variable name that
 * starts with the string &quot;WhatIs:&quot; is treated as a
 * special key which is resolved against {@link WhatIs WhatIs}.
 * Variables that have no mapping in the <code>Map</code> that
 * is passed to the parsing method are resolved against the
 * system properties.<p>
 *
 * Aliases are defined by means of a pre-processed array of
 * strings. Aliases commands are replaced by a matching alias
 * definition. Please note that variable substitution occurs
 * before alias resolving. Any variable definitions in alias
 * definitions are thus mapped literally.
 *
 * @author Volker Roth
 * @version "$Id: ArgsParser.java 474 2001-08-24 17:55:05Z vroth $"
 */
public class ArgsParser extends Object {

    /**
     * The name of the file that contains the mapping from
     * short type names to the names of Java classes that
     * represent values of that option type.
     */
    public static final String ARGS_MAP = "DE/FhG/IGD/util/args.map";

    /**
     * The identifier of flag types.
     */
    public static final String TYPE_FLAG = "!";

    /**
     * Maps classes to type identifier characters.
     */
    private static Map t2cl_;

    /**
     * Maps type identifier characters to classes.
     */
    private static Map cl2t_;

    /**
     * The <code>Map</code> to map classes to primitive classes.
     */
    private static Map prim_;

    /**
     * Busy eating up characters. This is a final state.
     */
    public static final int STATE_EAT = 0;

    /**
     * Skipping white spaces between args. All non-final states
     * must be greater than this state.
     */
    public static final int STATE_SKIP = 1;

    /**
     * Found beginning of a variable '$'.
     */
    public static final int STATE_VAR = 2;

    /**
     * Reading of variable name &quot;${&quot;.
     */
    public static final int STATE_VARQUOTE = 3;

    /**
     * The <code>Map</code> of option definitions.
     */
    protected Map options_ = new HashMap();

    /**
     * A list which is used to collect the options that
     * are actually defined, in the order in which they
     * were defined.
     */
    protected List active_ = new ArrayList(8);

    static {
        LineNumberReader reader;
        InputStream in;
        String line;
        String tmp;
        Object key;
        Object val;
        Map map;
        int n;
        t2cl_ = new HashMap();
        cl2t_ = new HashMap();
        prim_ = new HashMap();
        prim_.put(Integer.class, int.class);
        prim_.put(Long.class, long.class);
        prim_.put(Float.class, float.class);
        prim_.put(Double.class, double.class);
        prim_.put(Byte.class, byte.class);
        prim_.put(Boolean.class, boolean.class);
        in = ClassLoader.getSystemResourceAsStream(ARGS_MAP);
        reader = new LineNumberReader(new InputStreamReader(in));
        map = new HashMap();
        try {
            while ((line = reader.readLine()) != null) {
                n = line.indexOf('=');
                if (n > 0) {
                    tmp = line.substring(0, n).trim();
                    key = (Object) Class.forName(tmp);
                    val = line.substring(n + 1).trim();
                    cl2t_.put(key, val);
                    t2cl_.put(val, key);
                    key = prim_.get(key);
                    if (key != null) {
                        cl2t_.put(key, val);
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Creates an instance that parses the given option definitions.
     * The <code>options</code> string consists of a comma separated
     * list of option declarations. Each option declaration consists
     * of an option name and a type abbreviation (a short name, e.g.
     * a one-letter name) separated by a colon. For instance, the
     * string &quot;host:s,port:n,file:f&quot; declares three options,
     * a string (host), an integer (port), and a file (file). When
     * these options are parsed, the result values can be retrieved
     * as a <code>String</code>, an <code>int</code>, and a <code>
     * File</code> respectively.<p>
     *
     * The parser supports array types of all the basic types that
     * it understands. An array option is defined by appending
     * &quot;[&quot; to the short name of the desired type. For
     * instance, an option that takes an array of integers is
     * declared as &quot;option:n[&quot;.<p>
     *
     * The value of an option can be requested by means of a
     * variety of getter methods of the form
     * <i>type</i><code>Value(name)</code> where <i>type</i>
     * is the type of the option. Array types are returned as
     * an array object. The component type of the returned array
     * is the <i>primitive</i> type whenever possible and not a
     * wrapper type. For instance, the return type of the array
     * option declared above is <code>int[]</code> rather than
     * <code>Integer[]</code>. This little magic is achieved by
     * means of Java Reflection.
     *
     * @param options The string of option declarations as
     *   described above.
     * @exception IllegalArgumentException if the option
     *   declarations has a bad syntax.
     */
    public ArgsParser(String options) {
        String type;
        String def;
        int n;
        if (options == null) {
            throw new NullPointerException("Options");
        }
        while (options.length() > 0) {
            n = options.indexOf(',');
            if (n < 0) {
                def = options;
                options = "";
            } else {
                def = options.substring(0, n);
                options = options.substring(n + 1);
            }
            if (def.length() == 0) {
                continue;
            }
            n = def.indexOf(':');
            if (n < 1) {
                throw new IllegalArgumentException("Missing option identifier in \"" + def + "\"!");
            }
            type = def.substring(n + 1);
            def = def.substring(0, n);
            if (type.length() == 0) {
                throw new IllegalArgumentException("Missing type identifier in \"" + def + "\"!");
            }
            options_.put(def, new Option(def, type));
        }
    }

    /**
     * @param c The <code>Collection</code> of objects that
     *   shall be converted into an array.
     * @return An array with the elements of the given <code>
     *   Collection</code>. The first element in <code>c</code>
     *   determines the type of the result array. If the first
     *   element is of a type that wraps a primitive Java type
     *   than the resulting array will have the primitive
     *   type as the component type. If the given <code>
     *   Collection</code> has no elements then <code>null
     *   </code> is returned.
     */
    public static Object convert(Collection c) {
        Iterator i;
        Object obj;
        Object res;
        Class type;
        int n;
        if (c.size() == 0) {
            return null;
        }
        obj = c.iterator().next();
        type = (Class) prim_.get(obj.getClass());
        if (type == null) {
            type = obj.getClass();
        }
        res = Array.newInstance(type, c.size());
        for (n = 0, i = c.iterator(); i.hasNext(); n++) {
            Array.set(res, n, i.next());
        }
        return res;
    }

    /**
     * Returns the last elements of array <code>argv</code> starting
     * with the lement at index <code>offset</code>. If the given
     * offset is greater than the length of the array then an
     * exception is thrown.
     *
     * @param argv The array of arguments.
     * @param offset The start index.
     */
    public static String[] tail(String[] argv, int offset) {
        if (offset < 0 || offset > argv.length) {
            throw new ArrayIndexOutOfBoundsException("Invalid offset: " + offset);
        }
        String[] res;
        res = new String[argv.length - offset];
        System.arraycopy(argv, offset, res, 0, res.length);
        return res;
    }

    /**
     * Parses the pre-parsed array of input tokens into pipeline
     * stages. A single &quot;|&quot; serves as the pipeline
     * symbol and separates the stages of the pipeline.
     *
     * @param argv The array of input tokens as returned for
     *   instance by <code>parse(String, Map)</code>.
     * @param aliases The <code>Map</code> of alias definitions.
     *   The key type of <code>aliases</code> must be <code>
     *   String</code>, and the value type must be <code>
     *   String[]</code>.
     * @return The list of <code>String[]</code> where each
     *   element of the list contains the arguments to each
     *   pipeline stage. The pipeline stages are already
     *   resolved against the given alias definitions.
     * @exception BadArgsException if pipeline symbols are
     *   at illegal places such as the end or the beginning
     *   of <code>argv</code>.
     */
    public static List postprocess(String[] argv, Map aliases) throws BadArgsException {
        ArrayList pipe;
        String[] next;
        String[] tmp;
        boolean eating;
        int n;
        int m;
        if (argv == null || argv.length == 0) {
            return new ArrayList(0);
        }
        eating = false;
        pipe = new ArrayList(4);
        tmp = new String[argv.length];
        for (m = 0, n = 0; n < argv.length; n++) {
            if (argv[n].equals("|")) {
                if (!eating) {
                    throw new BadArgsException("Isolated pipe symbol at index " + n);
                }
                if (m == 0) {
                    throw new BadArgsException("Empty pipe at index " + n);
                }
                next = new String[m];
                System.arraycopy(tmp, 0, next, 0, m);
                pipe.add(resolveAlias(next, aliases));
                m = 0;
                eating = false;
            } else {
                tmp[m++] = argv[n];
                eating = true;
            }
        }
        if (!eating) {
            throw new BadArgsException("Terminal argument is a pipe symbol!");
        }
        next = new String[m];
        System.arraycopy(tmp, 0, next, 0, m);
        pipe.add(resolveAlias(next, aliases));
        return pipe;
    }

    /**
     * Parses the given string into a <code>String[]</code> as
     * a shell would. Unless quoted, spaces and tabs in <code>
     * line</code> denote separators. The &quot;|&quot; character
     * denotes a pipe symbol. A pipe symbol may preceed other
     * tokens without a separating whitespace, but is returned
     * as a separate string in the result. Variables start with
     * the dollar sign and the variable name must be enclosed
     * in angle brackets. Variable names are resolved against
     * the given variable definitions. A variable name that
     * starts with &quot;WhatIs:&quot; is interpreted as a
     * {@link WhatIs WhatIs} key. A variable name that is not
     * a <code>WhatIs</code> key and is not defined in <code>
     * vars</code> is resolved against the system properties.
     *
     * @param line The input string that shall be parsed.
     * @param vars The <code>Map</code> of variable definitions
     *   where the key and value type must be <code>String</code>.
     * @return The array of elements into which the input line
     *   was parsed.
     * @exception BadArgsException if the format of <code>
     *   line</code> is bad.
     */
    public static String[] preprocess(String line, Map vars) throws BadArgsException {
        StringBuffer buf;
        boolean escape;
        boolean quote;
        String key;
        String val;
        char[] c;
        List args;
        int state;
        int i;
        int l;
        if (line == null) {
            return new String[0];
        }
        c = line.toCharArray();
        buf = new StringBuffer(32);
        args = new ArrayList(8);
        state = STATE_SKIP;
        quote = false;
        escape = false;
        for (l = 0, i = 0; i < c.length; i++) {
            if (state == STATE_SKIP) {
                if (c[i] == ' ' || c[i] == '\t') {
                    continue;
                }
                if (c[i] == '|') {
                    args.add("|");
                    continue;
                }
                state = STATE_EAT;
            }
            if (state == STATE_VAR) {
                if (c[i] != '{') {
                    throw new BadArgsException("Variable name must be in curly braces!");
                }
                state = STATE_VARQUOTE;
                l = i + 1;
                continue;
            }
            if (state == STATE_VARQUOTE) {
                if (Character.isLetterOrDigit(c[i]) || c[i] == '.' || c[i] == ':' || c[i] == '_') {
                    continue;
                }
                if (c[i] != '}') {
                    throw new BadArgsException("Bad char in variable name!");
                }
                state = STATE_EAT;
                key = new String(c, l, i - l);
                val = resolveVariable(key, vars);
                if (val == null) {
                    throw new BadArgsException("Undefined variable: " + key);
                }
                buf.append(val);
                continue;
            }
            if (escape) {
                switch(c[i]) {
                    case ' ':
                        buf.append(' ');
                        break;
                    case 'n':
                        buf.append('\n');
                        break;
                    case 't':
                        buf.append('\t');
                        break;
                    case '$':
                        buf.append('$');
                        break;
                    case '\\':
                        buf.append('\\');
                        break;
                    case '"':
                        buf.append('"');
                        break;
                    case '|':
                        buf.append('|');
                        break;
                    default:
                        throw new BadArgsException("Illegal escape '\\" + c[i] + "'");
                }
                escape = false;
                continue;
            }
            if (c[i] == '$') {
                state = STATE_VAR;
                continue;
            }
            if (c[i] == '"') {
                quote = !quote;
                continue;
            }
            if (c[i] == '\\') {
                escape = true;
                continue;
            }
            if (!quote && (c[i] == ' ' || c[i] == '\t')) {
                args.add(buf.toString());
                buf.setLength(0);
                state = STATE_SKIP;
                continue;
            }
            buf.append(c[i]);
        }
        if (quote || escape) {
            throw new BadArgsException("Unresolved QUOTE or ESCAPE!");
        }
        if (state > STATE_SKIP) {
            throw new BadArgsException("Syntax error in variable expression!");
        }
        if (state == STATE_EAT) {
            args.add(buf.toString());
        }
        return (String[]) args.toArray(new String[0]);
    }

    /**
     * Resolves any aliases that may apply to <code>argv</code>.
     * If no fitting alias is found then the input is returned
     * unmodified.<p>
     *
     * @param argv The parsed arguments as returned by {@link
     *   #parse(java.lang.String[]) parse(String[])}.
     * @param aliases The <code>Map</code> of aliases. The key
     *   type of the <code>Map</code> must be <code>String</code>,
     *   and the value type must be <code>String[]</code>.
     * @return The new array of strings where the first element
     *   of the input string is replaced by the array of strings
     *   of the matching alias definition. If no matching alias
     *   is found then the input array is returned unmodified.
     */
    public static String[] resolveAlias(String[] argv, Map aliases) {
        String[] result;
        String[] alias;
        boolean done;
        int i;
        if (argv == null || argv.length == 0) {
            return argv;
        }
        if (aliases == null || aliases.size() == 0) {
            return argv;
        }
        alias = (String[]) aliases.get(argv[0]);
        if (alias == null) {
            return argv;
        }
        result = new String[alias.length + argv.length - 1];
        System.arraycopy(alias, 0, result, 0, alias.length);
        System.arraycopy(argv, 1, result, alias.length, argv.length - 1);
        return result;
    }

    /**
     * Returns the value of the variable with name <code>var</code>.
     * The shell maintains its own set of variables. However, if no
     * variable with name <code>var</code> is defined in the shell
     * but a system property with the same name exists then the
     * system property is returned.
     *
     * @param name The variable name. If <code>name</code> starts
     *   with &quot;WhatIs:&quot; then the remainder of <code>
     *   name</code> is interpreted as a {@link WhatIs WhatIs}
     *   key, and the value of the variable is set to the string
     *   value of that key. If no such key is defined in <code>
     *   WhatIs</code> then an exception is thrown. If <code>
     *   name</code> is not a <code>WhatIs</code> key and is not
     *   defined in <code>variables</code> then the system
     *   properties are used to resolve <code>name</code>.
     * @param variables The <code>Map</code> of variable definitions.
     *   Both the key and the value type of the given <code>Map</code>
     *   must be <code>String</code>.
     * @return The value of the variable or <code>null</code> if
     *   no variable with the given name is defined.
     * @exception IllegalStateException if the given name denotes
     *   a <code>WhatIs</code> key, but <code>WhatIs</code> does
     *   not contain a mapping for that key.
     */
    public static String resolveVariable(String name, Map variables) {
        String res;
        if (name == null) {
            return null;
        }
        if (name.startsWith("WhatIs:")) {
            try {
                return WhatIs.stringValue(name.substring(7));
            } catch (Exception e) {
                return null;
            }
        }
        if (variables != null) {
            res = (String) variables.get(name);
            if (res != null) {
                return res;
            }
        }
        try {
            res = System.getProperty(name);
            return res;
        } catch (SecurityException e) {
            return null;
        }
    }

    public static Class getPrimitiveClass(Class aClass) {
        if (aClass == null) {
            throw new NullPointerException("aClass");
        }
        return (Class) prim_.get(aClass);
    }

    public static String getShortName(Class aClass) {
        if (aClass == null) {
            throw new NullPointerException("aClass");
        }
        return (String) cl2t_.get(aClass);
    }

    /**
     * @param shortName The short name that stands for a parser
     *   option type.
     * @return The class object that is represented by the
     *   given type identifier, e.g. <code>java.lang.String.class
     *   </code> for &quot;s&quot;.
     */
    public static Class getClass(String shortName) {
        if (shortName == null) {
            throw new NullPointerException("shortName");
        }
        return (Class) t2cl_.get(shortName);
    }

    /**
     * Parses the given input and assigns the parsed values to
     * the internal set of options.
     */
    public String[] parse(String[] argv) throws ArgsParserException {
        Option opt;
        String arg;
        List res;
        List ol;
        int n;
        res = new ArrayList();
        opt = null;
        for (n = 0; n < argv.length; n++) {
            arg = argv[n];
            if (arg == null) {
                continue;
            }
            if (arg.startsWith("-")) {
                arg = arg.substring(1);
                opt = getOption(arg, false);
                if (opt.type_ == null) {
                    if (!opt.defined_) {
                        active_.add(opt.name_);
                    }
                    opt.defined_ = true;
                    opt = null;
                }
                continue;
            }
            if (opt == null) {
                res.add(arg);
                continue;
            }
            if (!opt.defined_) {
                active_.add(opt.name_);
            }
            if (opt.array_) {
                if (opt.value_ == null) {
                    opt.value_ = new ArrayList();
                }
                ol = (List) opt.value_;
                opt.defined_ = true;
                ol.add(parseOption(opt, arg));
                continue;
            }
            if (opt.defined_) {
                throw new BadArgsException("Option \"" + opt.name_ + "\" is double defined!");
            }
            opt.value_ = parseOption(opt, arg);
            opt.defined_ = true;
            opt = null;
        }
        return (String[]) res.toArray(argv);
    }

    /**
     * Parses the given option argument into the appropriate
     * Java type and returns the resulting object. The parsed
     * value is <b>not</b> automatically assigned to <code>
     * opt</code>. This must be done by the caller.
     *
     * @param opt The option whose type shall be parsed.
     * @param arg The string representation of that type.
     * @return The Java object that represents <code>arg
     *   </code>.
     * @exception BadArgsException if the given <code>arg
     *   </code> cannot be parsed into the desired type.
     */
    protected Object parseOption(Option opt, String arg) throws BadArgsException {
        Constructor init;
        Object[] param;
        Class[] sig;
        try {
            sig = new Class[] { arg.getClass() };
            param = new Object[] { arg };
            init = opt.type_.getConstructor(sig);
            return init.newInstance(param);
        } catch (Exception e) {
            opt.help(System.err);
            throw new BadArgsException("Can't assign \"" + arg + "\" to option \"" + opt.name_ + "\"");
        }
    }

    /**
     * Returns the option with the given name. If <code>
     * defined</code> is <code>true</code> then this
     * method throws an exception if the option is known
     * but not assigned any value.<p>
     *
     * This method recognizes options in constant time if
     * the correct name is given (see disclaimers on <code>
     * HashMap</code>). Options are also recognized by a
     * unique prefix in time proportional to the number of
     * options.
     *
     * @param name The name of the option to be returned.
     * @param defined <code>true</code> if an exception
     *   shall be thrown if the option with the given
     *   name is not assigned a value yet.
     * @return The option with the given name.
     * @exception UnknownOptionException if no option with
     *   the given name was declared upon creting this
     *   parser.
     * @exception UndefinedOptionException if the option
     *   with the given name is not assigned a value yet
     *   and <code>defined</code> is <code>true</code>.
     */
    protected Option getOption(String name, boolean defined) throws ArgsParserException {
        Option opt;
        if (name == null) {
            throw new NullPointerException("name");
        }
        opt = (Option) options_.get(name);
        if (opt == null) {
            Iterator i;
            List list;
            list = new ArrayList(8);
            for (i = options_.values().iterator(); i.hasNext(); ) {
                opt = (Option) i.next();
                if (opt.name_.startsWith(name)) {
                    list.add(opt);
                }
            }
            if (list.size() != 1) {
                throw new UnknownOptionException(name);
            }
            opt = (Option) list.get(0);
        }
        if (defined && !opt.defined_) {
            throw new UndefinedOptionException(name);
        }
        return opt;
    }

    /**
     * @param name The name of the option instance that shall be
     *   returned.
     * @return The result of the call <code>getOption(name, true)
     *   </code>. In other words, a check is made whether the
     *   option with the given name is already defined (assigned
     *   at least one value).
     * @exception UnknownOptionException if no option with the
     *   given name was declared upon creating this instance.
     * @exception UndefinedOptionException if the option with
     *   the given name does not yet have a value assigned to
     *   it.
     */
    protected Option getOption(String name) throws ArgsParserException {
        return getOption(name, true);
    }

    public int intValue(String name) throws ArgsParserException {
        Option opt;
        opt = getOption(name);
        return ((Number) opt.value_).intValue();
    }

    public long longValue(String name) throws ArgsParserException {
        Option opt;
        opt = getOption(name);
        return ((Number) opt.value_).longValue();
    }

    public float floatValue(String name) throws ArgsParserException {
        Option opt;
        opt = getOption(name);
        return ((Number) opt.value_).floatValue();
    }

    public double doubleValue(String name) throws ArgsParserException {
        Option opt;
        opt = getOption(name);
        return ((Number) opt.value_).doubleValue();
    }

    public String stringValue(String name) throws ArgsParserException {
        Option opt;
        opt = getOption(name);
        return opt.value_.toString();
    }

    public char charValue(String name) throws ArgsParserException {
        Option opt;
        opt = getOption(name);
        return ((Character) opt.value_).charValue();
    }

    public boolean booleanValue(String name) throws ArgsParserException {
        Option opt;
        opt = getOption(name);
        return ((Boolean) opt.value_).booleanValue();
    }

    public File fileValue(String name) throws ArgsParserException {
        Option opt;
        opt = getOption(name);
        return (File) opt.value_;
    }

    public Object value(String name) throws ArgsParserException {
        Option opt;
        opt = getOption(name);
        return opt.value_;
    }

    public int intValue(String name, int alt) {
        try {
            return intValue(name);
        } catch (ArgsParserException e) {
            return alt;
        }
    }

    public long longValue(String name, long alt) {
        try {
            return longValue(name);
        } catch (ArgsParserException e) {
            return alt;
        }
    }

    public float floatValue(String name, float alt) {
        try {
            return floatValue(name);
        } catch (ArgsParserException e) {
            return alt;
        }
    }

    public double doubleValue(String name, double alt) {
        try {
            return doubleValue(name);
        } catch (ArgsParserException e) {
            return alt;
        }
    }

    public String stringValue(String name, String alt) {
        try {
            return stringValue(name);
        } catch (ArgsParserException e) {
            return alt;
        }
    }

    public char charValue(String name, char alt) {
        try {
            return charValue(name);
        } catch (ArgsParserException e) {
            return alt;
        }
    }

    public boolean booleanValue(String name, boolean alt) {
        try {
            return booleanValue(name);
        } catch (ArgsParserException e) {
            return alt;
        }
    }

    public File fileValue(String name, File alt) {
        try {
            return fileValue(name);
        } catch (ArgsParserException e) {
            return alt;
        }
    }

    /**
     * Retrieves the values assigned to an option. Depending
     * on <code>unwrap</code> the value(s) are unwrapped to
     * their primitive types whenever applicable, or they
     * are returned as <code>Object[]</code> with wrapped
     * types rather than primitive types.
     *
     * @param name The name of the option whose values shall
     *   be returned.
     * @param unwrap If <code>true</code> then primitive types
     *   will be unwrapped and the returned type will be an
     *   array of the primitive type whenever applicable. If
     *   <code>false</code> then the returned type will be of
     *   type <code>Object[]</code> and the elements will be
     *   wrapped types rather than primitive types.
     */
    public Object values(String name, boolean unwrap) throws ArgsParserException {
        if (!unwrap) {
            Collection val;
            Iterator i;
            Option opt;
            Object res;
            int n;
            opt = getOption(name);
            if (!opt.array_) {
                res = Array.newInstance(opt.type_, 1);
                Array.set(res, 0, opt.value_);
            } else {
                val = (Collection) opt.value_;
                res = Array.newInstance(opt.type_, val.size());
                for (n = 0, i = val.iterator(); i.hasNext(); n++) {
                    Array.set(res, n, i.next());
                }
            }
            return res;
        }
        return values(name);
    }

    /**
     * Retrieves the values assigned to an option. The return
     * type is an array type. The array's component type will
     * be primitive whenever applicable.
     *
     * @param name The name of the option whose values shall
     *   be returned. The values are automatically unwrapped
     *   to their primitive types whenever applicable. The
     *   return type is an array type whose component type
     *   is the (primitive) type of the option type.
     * @return The value(s) of the option with the given name
     *   as an array. The array's component type is <i>primitive
     *   </i> whenever applicable. For instance, the return type
     *   for integer options is <code>int[]</code> rather than
     *   <code>Integer[]</code>. If the option's type is not an
     *   array type then the returned array contains the value
     *   of the option as a single element of an array of length
     *   1. The array's component type is the type of the option.
     * @exception UnknownOptionException if no option with the
     *   given name was declared in this instance.
     * @exception UndefinedOptionException if no values are
     *   assigned to the option with the given name.
     */
    public Object values(String name) throws ArgsParserException {
        Object res;
        Option opt;
        Class type;
        opt = getOption(name);
        if (opt.array_) {
            return convert((Collection) opt.value_);
        }
        type = (Class) prim_.get(opt.value_.getClass());
        if (type == null) {
            type = opt.value_.getClass();
        }
        res = Array.newInstance(type, 1);
        Array.set(res, 0, opt.value_);
        return res;
    }

    /**
     * @return <code>true</code> if the option with the given
     *   name is declared and is a <i>flag</i> (was defined
     *   with tye &quot;!&quot; in the options descriptor).
     */
    public boolean isFlag(String name) throws ArgsParserException {
        return (getOption(name, false).type_ == null);
    }

    /**
     * @param name The name of the option that shall be tested.
     * @return <code>true</code> if and only if the option
     *   with the given name is declared and is assigned at
     *   least one value.
     * @exception UnknownOptionException if no option with
     *   the given name exists.
     */
    public boolean isDefined(String name) throws ArgsParserException {
        return getOption(name, false).defined_;
    }

    /**
     * @param name The name of the option that shall be tested.
     * @return <code>true</code> if the option with the given
     *   nname is declared and is an array option.
     * @exception UnknownOptionException if no option with
     *   the given name exists.
     */
    public boolean isArray(String name) throws ArgsParserException {
        return getOption(name, false).array_;
    }

    /**
     * Clears all options. The parser is ready to be used again
     * with the same set of options.
     */
    public void reset() {
        Iterator i;
        Option opt;
        for (i = options_.values().iterator(); i.hasNext(); ) {
            opt = (Option) i.next();
            opt.clear();
        }
        active_.clear();
    }

    /**
     * Prints the list of declared options, their types, and
     * information on the assigned values for those options
     * that are already defined.
     *
     * @param out The output is written to the given <code>
     *   PrintStream</code>.
     */
    public void help(PrintStream out) {
        Iterator i;
        Option opt;
        if (out == null) {
            throw new NullPointerException("out");
        }
        for (i = options_.values().iterator(); i.hasNext(); ) {
            opt = (Option) i.next();
            opt.help(out);
        }
    }

    /**
     * @return The array of names of those options that are
     *   defined. The order of elements in the array is the
     *   order in which the options where parsed (from left
     *   to right on the parsed input array).
     */
    public String[] getDefinedNames() {
        return (String[]) active_.toArray(new String[0]);
    }

    /**
     * Represents a single option and the values for that option.
     *
     */
    protected class Option extends Object {

        protected Object value_;

        protected String name_;

        protected boolean defined_;

        protected boolean array_;

        protected Class type_;

        protected Option(String name, String type) {
            int n;
            if (name == null || type == null) {
                throw new NullPointerException("name or type!");
            }
            name_ = name;
            defined_ = false;
            if (type.equals(TYPE_FLAG)) {
                array_ = false;
                return;
            }
            if (type.endsWith("[")) {
                array_ = true;
                type = type.substring(0, type.length() - 1);
            } else {
                array_ = false;
            }
            type_ = (Class) t2cl_.get(type);
            if (type_ == null) {
                throw new IllegalArgumentException("Unknown option type: \"" + type + "\"");
            }
        }

        protected void clear() {
            value_ = null;
            defined_ = false;
        }

        protected void help(PrintStream out) {
            String value;
            String name;
            if (type_ == null) {
                name = "<flag>";
                value = defined_ ? "" : "= <undefined>";
            } else {
                name = type_.getName();
                name = name.substring(name.lastIndexOf('.') + 1);
                if (defined_) {
                    value = array_ ? "= {...}" : ("= " + value_.toString());
                } else {
                    value = "= <undefined>";
                }
            }
            out.println("(" + name + (array_ ? "[]" : "") + ") " + name_ + " " + value);
        }

        public boolean equals(Object o) {
            Option opt;
            if (o instanceof Object) {
                opt = (Option) o;
                return name_.equals(opt.name_) && (type_ == opt.type_) && (array_ == opt.array_);
            }
            return false;
        }

        public int hashCode() {
            int n;
            n = (type_ == null) ? 0 : type_.hashCode();
            return name_.hashCode() + (array_ ? -1 : 1) * n;
        }
    }
}
