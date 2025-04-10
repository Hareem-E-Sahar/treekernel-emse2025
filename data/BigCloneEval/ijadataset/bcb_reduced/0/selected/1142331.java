package gnu.kawa.reflect;

import gnu.mapping.*;
import gnu.expr.*;
import gnu.bytecode.*;
import gnu.lists.FString;
import java.lang.reflect.Array;
import gnu.kawa.lispexpr.ClassNamespace;

public class Invoke extends ProcedureN implements CanInline {

    /** The kind on invoke operation.
   *  'N' - make (new).
   *  'P' - invoke-special.
   *  'S' - invoke-static (static or non-static):
   *        The first operand is a Class or Type, the second is the name,
   *        and if the is non-static the 3rd is the receiver.
   *  's' - Like 'S' but only allow static methods. [not used]
   *  'V' - non-static invoke, only allow non-static methods. [not used]
   *  '*' - non-static invoke, can match static methods also.
   *        This is Java's 'Primary.MethodName(args)' - if the selected method
   *        is static, we only use Primary's type for method select,
   *        but ignore its value.
   */
    char kind;

    Language language;

    public static final Invoke invoke = new Invoke("invoke", '*');

    public static final Invoke invokeStatic = new Invoke("invoke-static", 'S');

    public static final Invoke invokeSpecial = new Invoke("invoke-special", 'P');

    public static final Invoke make = new Invoke("make", 'N');

    public Invoke(String name, char kind) {
        super(name);
        this.kind = kind;
        this.language = Language.getDefaultLanguage();
    }

    public Invoke(String name, char kind, Language language) {
        super(name);
        this.kind = kind;
        this.language = language;
    }

    public static Object invoke$V(Object[] args) throws Throwable {
        return invoke.applyN(args);
    }

    public static Object invokeStatic$V(Object[] args) throws Throwable {
        return invokeStatic.applyN(args);
    }

    public static Object make$V(Object[] args) throws Throwable {
        return make.applyN(args);
    }

    private static ObjectType typeFrom(Object arg, Invoke thisProc) {
        if (arg instanceof Class) arg = Type.make((Class) arg);
        if (arg instanceof ObjectType) return (ObjectType) arg;
        if (arg instanceof String || arg instanceof FString) return ClassType.make(arg.toString());
        if (arg instanceof Symbol) return ClassType.make(((Symbol) arg).getName());
        if (arg instanceof ClassNamespace) return ((ClassNamespace) arg).getClassType();
        throw new WrongType(thisProc, 0, arg, "class-specifier");
    }

    public void apply(CallContext ctx) throws Throwable {
        Object[] args = ctx.getArgs();
        if (kind == 'S' || kind == 'V' || kind == 's' || kind == '*') {
            int nargs = args.length;
            Procedure.checkArgCount(this, nargs);
            Object arg0 = args[0];
            ObjectType dtype = (ObjectType) ((kind == 'S' || kind == 's') ? typeFrom(arg0, this) : Type.make(arg0.getClass()));
            Procedure proc = lookupMethods(dtype, args[1]);
            Object[] margs = new Object[nargs - (kind == 'S' ? 2 : 1)];
            int i = 0;
            if (kind == 'V' || kind == '*') margs[i++] = args[0];
            System.arraycopy(args, 2, margs, i, nargs - 2);
            proc.checkN(margs, ctx);
        } else ctx.writeValue(this.applyN(args));
    }

    public Object applyN(Object[] args) throws Throwable {
        if (kind == 'P') throw new RuntimeException(getName() + ": invoke-special not allowed at run time");
        int nargs = args.length;
        Procedure.checkArgCount(this, nargs);
        Object arg0 = args[0];
        ObjectType dtype = (kind != 'V' && kind != '*' ? typeFrom(arg0, this) : (ObjectType) Type.make(arg0.getClass()));
        Object mname;
        if (kind == 'N') {
            mname = null;
            if (dtype instanceof TypeValue) {
                Procedure constructor = ((TypeValue) dtype).getConstructor();
                if (constructor != null) {
                    nargs--;
                    Object[] xargs = new Object[nargs];
                    System.arraycopy(args, 1, xargs, 0, nargs);
                    return constructor.applyN(xargs);
                }
            }
            if (dtype instanceof PairClassType) {
                PairClassType ptype = (PairClassType) dtype;
                dtype = ptype.instanceType;
            }
            if (dtype instanceof ArrayType) {
                Type elementType = ((ArrayType) dtype).getComponentType();
                int len;
                len = args.length - 1;
                String name;
                int length;
                int i;
                boolean lengthSpecified;
                if (len >= 2 && args[1] instanceof Keyword && ("length".equals(name = ((Keyword) args[1]).getName()) || "size".equals(name))) {
                    length = ((Number) args[2]).intValue();
                    i = 3;
                    lengthSpecified = true;
                } else {
                    length = len;
                    i = 1;
                    lengthSpecified = false;
                }
                Object arr = Array.newInstance(elementType.getReflectClass(), length);
                int index = 0;
                for (; i <= len; i++) {
                    Object arg = args[i];
                    if (lengthSpecified && arg instanceof Keyword && i < len) {
                        String kname = ((Keyword) arg).getName();
                        try {
                            index = Integer.parseInt(kname);
                        } catch (Throwable ex) {
                            throw new RuntimeException("non-integer keyword '" + kname + "' in array constructor");
                        }
                        arg = args[++i];
                    }
                    Array.set(arr, index, elementType.coerceFromObject(arg));
                    index++;
                }
                return arr;
            }
        } else {
            mname = args[1];
        }
        MethodProc proc = lookupMethods((ObjectType) dtype, mname);
        if (kind != 'N') {
            Object[] margs = new Object[nargs - (kind == 'S' || kind == 's' ? 2 : 1)];
            int i = 0;
            if (kind == 'V' || kind == '*') margs[i++] = args[0];
            System.arraycopy(args, 2, margs, i, nargs - 2);
            return proc.applyN(margs);
        } else {
            CallContext vars = CallContext.getInstance();
            int err = proc.matchN(args, vars);
            if (err == 0) return vars.runUntilValue();
            if ((nargs & 1) == 1) {
                for (int i = 1; ; i += 2) {
                    if (i == nargs) {
                        Object result;
                        result = proc.apply1(args[0]);
                        for (i = 1; i < nargs; i += 2) {
                            Keyword key = (Keyword) args[i];
                            Object arg = args[i + 1];
                            SlotSet.apply(false, result, key.getName(), arg);
                        }
                        return result;
                    }
                    if (!(args[i] instanceof Keyword)) break;
                }
            }
            MethodProc vproc = ClassMethods.apply((ClassType) dtype, "valueOf", '\0', language);
            if (vproc != null) {
                Object[] margs = new Object[nargs - 1];
                System.arraycopy(args, 1, margs, 0, nargs - 1);
                err = vproc.matchN(margs, vars);
                if (err == 0) return vars.runUntilValue();
            }
            throw MethodProc.matchFailAsException(err, proc, args);
        }
    }

    public int numArgs() {
        return (-1 << 12) | (kind == 'N' ? 1 : 2);
    }

    protected MethodProc lookupMethods(ObjectType dtype, Object name) {
        String mname;
        if (kind == 'N') mname = "<init>"; else {
            if (name instanceof String || name instanceof FString) mname = name.toString(); else if (name instanceof Symbol) mname = ((Symbol) name).getName(); else throw new WrongType(this, 1, null);
            mname = Compilation.mangleName(mname);
        }
        MethodProc proc = ClassMethods.apply(dtype, mname, kind == 'P' ? 'P' : kind == '*' || kind == 'V' ? 'V' : '\0', language);
        if (proc == null) throw new RuntimeException(getName() + ": no method named `" + mname + "' in class " + dtype.getName());
        return proc;
    }

    protected PrimProcedure[] getMethods(ObjectType ctype, String mname, ClassType caller) {
        return ClassMethods.getMethods(ctype, mname, kind == 'P' ? 'P' : kind == '*' || kind == 'V' ? 'V' : '\0', caller, language);
    }

    private static long selectApplicable(PrimProcedure[] methods, ObjectType ctype, Expression[] args, int margsLength, int argsStartIndex, int objIndex) {
        Type[] atypes = new Type[margsLength];
        int dst = 0;
        if (objIndex >= 0) atypes[dst++] = ctype;
        for (int src = argsStartIndex; src < args.length && dst < atypes.length; src++, dst++) atypes[dst] = args[src].getType();
        return ClassMethods.selectApplicable(methods, atypes);
    }

    /** Return an array if args (starting with start) is a set of
   * (keyword, value)-value pairs. */
    static Object[] checkKeywords(Type type, Expression[] args, int start, ClassType caller) {
        int len = args.length;
        if (((len - start) & 1) != 0) return null;
        Object[] fields = new Object[(len - start) >> 1];
        for (int i = fields.length; --i >= 0; ) {
            Expression arg = args[start + 2 * i];
            if (!(arg instanceof QuoteExp)) return null;
            Object value = ((QuoteExp) arg).getValue();
            if (!(value instanceof Keyword)) return null;
            String name = ((Keyword) value).getName();
            Member slot = SlotSet.lookupMember((ObjectType) type, name, caller);
            fields[i] = slot != null ? (Object) slot : (Object) name;
        }
        return fields;
    }

    /** Check if class exists.
   * @return 1 if class actually exists;
   * -1 is class should exist, but doesn't;
   * and 0 otherwise.
   */
    public static int checkKnownClass(Type type, Compilation comp) {
        if (type instanceof ClassType && ((ClassType) type).isExisting()) {
            try {
                type.getReflectClass();
                return 1;
            } catch (Exception ex) {
                comp.error('e', "unknown class: " + type.getName());
                return -1;
            }
        }
        return 0;
    }

    /** Resolve class specifier to ClassType at inline time.
   * This is an optimization to avoid having a module-level binding
   * created for the class name. */
    public static ApplyExp inlineClassName(ApplyExp exp, int carg, InlineCalls walker) {
        Compilation comp = walker.getCompilation();
        Language language = comp.getLanguage();
        Expression[] args = exp.getArgs();
        if (args.length > carg) {
            Type type = language.getTypeFor(args[carg]);
            if (!(type instanceof Type)) return exp;
            checkKnownClass(type, comp);
            Expression[] nargs = new Expression[args.length];
            System.arraycopy(args, 0, nargs, 0, args.length);
            nargs[carg] = new QuoteExp(type);
            ApplyExp nexp = new ApplyExp(exp.getFunction(), nargs);
            nexp.setLine(exp);
            return nexp;
        }
        return exp;
    }

    public Expression inline(ApplyExp exp, InlineCalls walker, boolean argsInlined) {
        exp.walkArgs(walker, argsInlined);
        Compilation comp = walker.getCompilation();
        Expression[] args = exp.getArgs();
        int nargs = args.length;
        if (!comp.mustCompile || nargs == 0 || ((kind == 'V' || kind == '*') && nargs == 1)) return exp;
        ObjectType type;
        Expression arg0 = args[0];
        Type type0 = (kind == 'V' || kind == '*' ? arg0.getType() : language.getTypeFor(arg0));
        if (type0 instanceof PairClassType) type = ((PairClassType) type0).instanceType; else if (type0 instanceof ObjectType) type = (ObjectType) type0; else type = null;
        String name = getMethodName(args);
        int margsLength, argsStartIndex, objIndex;
        if (kind == 'V' || kind == '*') {
            margsLength = nargs - 1;
            argsStartIndex = 2;
            objIndex = 0;
        } else if (kind == 'N') {
            margsLength = nargs;
            argsStartIndex = 0;
            objIndex = -1;
        } else if (kind == 'S' || kind == 's') {
            margsLength = nargs - 2;
            argsStartIndex = 2;
            objIndex = -1;
        } else if (kind == 'P') {
            margsLength = nargs - 2;
            argsStartIndex = 3;
            objIndex = 1;
        } else return exp;
        if (kind == 'N' && type instanceof ArrayType) {
            ArrayType atype = (ArrayType) type;
            Type elementType = atype.getComponentType();
            Expression sizeArg = null;
            boolean lengthSpecified = false;
            if (args.length >= 3 && args[1] instanceof QuoteExp) {
                Object arg1 = ((QuoteExp) args[1]).getValue();
                if (arg1 instanceof Keyword && ("length".equals(name = ((Keyword) arg1).getName()) || "size".equals(name))) {
                    sizeArg = args[2];
                    lengthSpecified = true;
                }
            }
            if (sizeArg == null) sizeArg = QuoteExp.getInstance(new Integer(args.length - 1));
            Expression alloc = new ApplyExp(new ArrayNew(elementType), new Expression[] { sizeArg });
            if (lengthSpecified && args.length == 3) return alloc;
            LetExp let = new LetExp(new Expression[] { alloc });
            Declaration adecl = let.addDeclaration((String) null, atype);
            adecl.noteValue(alloc);
            BeginExp begin = new BeginExp();
            int index = 0;
            for (int i = lengthSpecified ? 3 : 1; i < args.length; i++) {
                Expression arg = args[i];
                if (lengthSpecified && i + 1 < args.length && arg instanceof QuoteExp) {
                    Object key = ((QuoteExp) arg).getValue();
                    if (key instanceof Keyword) {
                        String kname = ((Keyword) key).getName();
                        try {
                            index = Integer.parseInt(kname);
                            arg = args[++i];
                        } catch (Throwable ex) {
                            comp.error('e', "non-integer keyword '" + kname + "' in array constructor");
                            return exp;
                        }
                    }
                }
                begin.add(new ApplyExp(new ArraySet(elementType), new Expression[] { new ReferenceExp(adecl), QuoteExp.getInstance(new Integer(index)), arg }));
                index++;
            }
            begin.add(new ReferenceExp(adecl));
            let.body = begin;
            return let;
        } else if (type != null && name != null) {
            if (type instanceof TypeValue && kind == 'N') {
                Procedure constructor = ((TypeValue) type).getConstructor();
                if (constructor != null) {
                    Expression[] xargs = new Expression[nargs - 1];
                    System.arraycopy(args, 1, xargs, 0, nargs - 1);
                    return ((InlineCalls) walker).walkApplyOnly(new ApplyExp(constructor, xargs));
                }
            }
            PrimProcedure[] methods;
            int okCount, maybeCount;
            ClassType caller = comp == null ? null : comp.curClass != null ? comp.curClass : comp.mainClass;
            ObjectType ctype = (ObjectType) type;
            try {
                methods = getMethods(ctype, name, caller);
                long num = selectApplicable(methods, ctype, args, margsLength, argsStartIndex, objIndex);
                okCount = (int) (num >> 32);
                maybeCount = (int) num;
            } catch (Exception ex) {
                comp.error('w', "unknown class: " + type.getName());
                return exp;
            }
            int index = -1;
            Object[] slots;
            if (okCount + maybeCount == 0 && kind == 'N' && (ClassMethods.selectApplicable(methods, new Type[] { Compilation.typeClassType }) >> 32) == 1 && (slots = checkKeywords(type, args, 1, caller)) != null) {
                StringBuffer errbuf = null;
                for (int i = 0; i < slots.length; i++) {
                    if (slots[i] instanceof String) {
                        if (errbuf == null) {
                            errbuf = new StringBuffer();
                            errbuf.append("no field or setter ");
                        } else errbuf.append(", ");
                        errbuf.append('`');
                        errbuf.append(slots[i]);
                        errbuf.append('\'');
                    }
                }
                if (errbuf != null) {
                    errbuf.append(" in class ");
                    errbuf.append(type.getName());
                    comp.error('w', errbuf.toString());
                    return exp;
                } else {
                    ApplyExp e = new ApplyExp(methods[0], new Expression[] { arg0 });
                    for (int i = 0; i < slots.length; i++) {
                        Expression[] sargs = { e, new QuoteExp(slots[i]), args[2 * i + 2] };
                        e = new ApplyExp(SlotSet.setFieldReturnObject, sargs);
                    }
                    return e.setLine(exp);
                }
            }
            int nmethods = methods.length;
            if (okCount + maybeCount == 0 && kind == 'N') {
                methods = invokeStatic.getMethods(ctype, "valueOf", caller);
                argsStartIndex = 1;
                margsLength = nargs - 1;
                long num = selectApplicable(methods, ctype, args, margsLength, argsStartIndex, -1);
                okCount = (int) (num >> 32);
                maybeCount = (int) num;
            }
            if (okCount + maybeCount == 0) {
                if (kind == 'P' || comp.getBooleanOption("warn-invoke-unknown-method", true)) {
                    if (kind == 'N') name = name + "/valueOf";
                    String message = (nmethods + methods.length == 0 ? "no accessible method '" + name + "' in " + type.getName() : ("no possibly applicable method '" + name + "' in " + type.getName()));
                    comp.error(kind == 'P' ? 'e' : 'w', message);
                }
            } else if (okCount == 1 || (okCount == 0 && maybeCount == 1)) index = 0; else if (okCount > 0) {
                index = MethodProc.mostSpecific(methods, okCount);
                if (index < 0) {
                    if (kind == 'S') {
                        for (int i = 0; i < okCount; i++) {
                            if (methods[i].getStaticFlag()) {
                                if (index >= 0) {
                                    index = -1;
                                    break;
                                } else index = i;
                            }
                        }
                    }
                }
                if (index < 0 && (kind == 'P' || comp.getBooleanOption("warn-invoke-unknown-method", true))) {
                    StringBuffer sbuf = new StringBuffer();
                    sbuf.append("more than one definitely applicable method `");
                    sbuf.append(name);
                    sbuf.append("' in ");
                    sbuf.append(type.getName());
                    append(methods, okCount, sbuf);
                    comp.error(kind == 'P' ? 'e' : 'w', sbuf.toString());
                }
            } else if (kind == 'P' || comp.getBooleanOption("warn-invoke-unknown-method", true)) {
                StringBuffer sbuf = new StringBuffer();
                sbuf.append("more than one possibly applicable method '");
                sbuf.append(name);
                sbuf.append("' in ");
                sbuf.append(type.getName());
                append(methods, maybeCount, sbuf);
                comp.error(kind == 'P' ? 'e' : 'w', sbuf.toString());
            }
            if (index >= 0) {
                Expression[] margs = new Expression[margsLength];
                int dst = 0;
                if (objIndex >= 0) margs[dst++] = args[objIndex];
                for (int src = argsStartIndex; src < args.length && dst < margs.length; src++, dst++) margs[dst] = args[src];
                return new ApplyExp(methods[index], margs).setLine(exp);
            }
        }
        return exp;
    }

    private void append(PrimProcedure[] methods, int mcount, StringBuffer sbuf) {
        for (int i = 0; i < mcount; i++) {
            sbuf.append("\n  candidate: ");
            sbuf.append(methods[i]);
        }
    }

    private String getMethodName(Expression[] args) {
        if (kind == 'N') return "<init>";
        int nameIndex = (kind == 'P' ? 2 : 1);
        if (args.length >= nameIndex + 1) return ClassMethods.checkName(args[nameIndex], false);
        return null;
    }

    /** Return an ApplyExp that will call a method with given arguments.
   * @param type the class containing the method we want to call.
   * @param name the name of the method we want to call
   * @param args the arguments to the call
   * @return an ApplyExp representing the call
   */
    public static synchronized ApplyExp makeInvokeStatic(ClassType type, String name, Expression[] args) {
        PrimProcedure method = getStaticMethod(type, name, args);
        if (method == null) throw new RuntimeException("missing or ambiguous method `" + name + "' in " + type.getName());
        return new ApplyExp(method, args);
    }

    public static synchronized PrimProcedure getStaticMethod(ClassType type, String name, Expression[] args) {
        PrimProcedure[] methods = invokeStatic.getMethods(type, name, null);
        long num = selectApplicable(methods, type, args, args.length, 0, -1);
        int okCount = (int) (num >> 32);
        int maybeCount = (int) num;
        int index;
        if (methods == null) index = -1; else if (okCount > 0) index = MethodProc.mostSpecific(methods, okCount); else if (maybeCount == 1) index = 0; else index = -1;
        return index < 0 ? null : methods[index];
    }
}
