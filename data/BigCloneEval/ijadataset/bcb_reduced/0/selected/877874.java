package gnu.expr;

import gnu.bytecode.*;
import gnu.mapping.*;
import gnu.lists.LList;
import java.util.Vector;
import gnu.kawa.functions.Convert;

/**
 * Class used to implement Scheme lambda expressions.
 * @author	Per Bothner
 */
public class LambdaExp extends ScopeExp {

    public Expression body;

    /** Minumnum number of parameters.
   * Does not count implicit isThisParameter(). */
    public int min_args;

    /** Maximum number of actual arguments;  -1 if variable. */
    public int max_args;

    /** Set of visible top-level LambdaExps that need apply methods. */
    Vector applyMethods;

    Variable argsArray;

    private Declaration firstArgsArrayArg;

    public Keyword[] keywords;

    public Expression[] defaultArgs;

    /** A list of Declarations, chained using Declaration's nextCapturedVar.
    * All the Declarations are allocated in the current heapFrame. */
    Declaration capturedVars;

    public void capture(Declaration decl) {
        if (decl.isSimple()) {
            if (capturedVars == null && !decl.isStatic() && !(this instanceof ModuleExp || this instanceof ClassExp)) {
                heapFrame = new gnu.bytecode.Variable("heapFrame");
            }
            decl.setSimple(false);
            if (!decl.isPublic()) {
                decl.nextCapturedVar = capturedVars;
                capturedVars = decl;
            }
        }
    }

    /** A local variable that points to the heap-allocated part of the frame.
   * Each captured variable is a field in the heapFrame.  A procedure has
   * a heapFrame iff if has a parameter or local variable that is
   * referenced ("captured") by a non-inline inferior procedure.
   * (I.e there is a least one non-inline procedure that encloses the
   * reference but not the definition.)  Note that an inline procedure may
   * have a heapFrame if it encloses a non-inline procedure.  This is
   * necessary because we represent loops as tail-recursive inline procedures.
   */
    Variable heapFrame;

    public LambdaExp firstChild;

    public LambdaExp nextSibling;

    /** A magic value to indicate there is no unique return continuation. */
    static final ApplyExp unknownContinuation = new ApplyExp((Expression) null, null);

    /** The unique call site that calls this lambda.
   * The value is null if no callers have been seen.
   * A value of unknownContinuation means there are multiple call sites.
   * Tail-recursive calls do not count as multiple call sites.
   * This is used to see if we can inline the function at its unique call site.
   * Usually this is an ApplyExp, but it can also be the "tail position"
   * for some outer expression, such as an IfExp.  This allows inlining f
   * in the call 'if (cond) f(x) else f(y)' since both calls have the same
   * return point.
   */
    public Expression returnContinuation;

    /** If non-null, set of functions that tail-call this function. */
    java.util.Set<LambdaExp> tailCallers;

    /** If this lambda gets inlined this is the containing lambda.
      Otherwise this is null. */
    public LambdaExp inlineHome;

    /** Expressions that name classes that may be thrown. */
    ReferenceExp[] throwsSpecification;

    public void setExceptions(ReferenceExp[] exceptions) {
        throwsSpecification = exceptions;
    }

    /** If non-null, a Declaration whose value is (only) this LambdaExp. */
    public Declaration nameDecl;

    /** If non-null, this is a Field that is used for implementing lexical closures.
   * If getName() is "closureEnv", it is our parent's heapFrame,
   * which is an instance of one of our siblings.
   * (Otherwise, we use "this" as the implicit "closureEnv" field.) */
    public Field closureEnvField;

    /** Field in heapFrame.getType() that contains the static link.
   * It is used by child functions to get to outer environments.
   * Its value is this function's closureEnv value. */
    public Field staticLinkField;

    /** A variable that points to the closure environment passed in.
   * It can be any one of:
   * null, if no closure environment is needed;
   * this, if this object is its parent's heapFrame;
   * a local variable initialized from this.closureEnv;
   * a parameter (only if !getCanRead()); or
   * a copy of our caller's closureEnv or heapFrame (only if getInlineOnly()).
   * See declareClosureEnv and closureEnvField. */
    Variable closureEnv;

    static final int INLINE_ONLY = 1;

    static final int CAN_READ = 2;

    static final int CAN_CALL = 4;

    static final int IMPORTS_LEX_VARS = 8;

    static final int NEEDS_STATIC_LINK = 16;

    static final int CANNOT_INLINE = 32;

    static final int CLASS_METHOD = 64;

    static final int METHODS_COMPILED = 128;

    public static final int NO_FIELD = 256;

    /** True if any parameter default expression captures a parameter. */
    static final int DEFAULT_CAPTURES_ARG = 512;

    public static final int SEQUENCE_RESULT = 1024;

    public static final int OVERLOADABLE_FIELD = 2048;

    public static final int ATTEMPT_INLINE = 4096;

    protected static final int NEXT_AVAIL_FLAG = 8192;

    /** True iff this lambda is only "called" inline. */
    public final boolean getInlineOnly() {
        return (flags & INLINE_ONLY) != 0;
    }

    public final void setInlineOnly(boolean inlineOnly) {
        setFlag(inlineOnly, INLINE_ONLY);
    }

    public final boolean getNeedsClosureEnv() {
        return (flags & (NEEDS_STATIC_LINK | IMPORTS_LEX_VARS)) != 0;
    }

    /** True if a child lambda uses lexical variables from outside.
      Hence, a child heapFrame needs a staticLink to outer frames. */
    public final boolean getNeedsStaticLink() {
        return (flags & NEEDS_STATIC_LINK) != 0;
    }

    public final void setNeedsStaticLink(boolean needsStaticLink) {
        if (needsStaticLink) flags |= NEEDS_STATIC_LINK; else flags &= ~NEEDS_STATIC_LINK;
    }

    /** True iff this lambda "captures" (uses) lexical variables from outside. */
    public final boolean getImportsLexVars() {
        return (flags & IMPORTS_LEX_VARS) != 0;
    }

    public final void setImportsLexVars(boolean importsLexVars) {
        if (importsLexVars) flags |= IMPORTS_LEX_VARS; else flags &= ~IMPORTS_LEX_VARS;
    }

    public final void setImportsLexVars() {
        int old = flags;
        flags |= IMPORTS_LEX_VARS;
        if ((old & IMPORTS_LEX_VARS) == 0 && nameDecl != null) setCallersNeedStaticLink();
    }

    public final void setNeedsStaticLink() {
        int old = flags;
        flags |= NEEDS_STATIC_LINK;
        if ((old & NEEDS_STATIC_LINK) == 0 && nameDecl != null) setCallersNeedStaticLink();
    }

    void setCallersNeedStaticLink() {
        LambdaExp outer = outerLambda();
        for (ApplyExp app = nameDecl.firstCall; app != null; app = app.nextCall) {
            LambdaExp caller = app.context;
            for (; caller != outer && !(caller instanceof ModuleExp); caller = caller.outerLambda()) caller.setNeedsStaticLink();
        }
    }

    public final boolean getCanRead() {
        return (flags & CAN_READ) != 0;
    }

    public final void setCanRead(boolean read) {
        if (read) flags |= CAN_READ; else flags &= ~CAN_READ;
    }

    public final boolean getCanCall() {
        return (flags & CAN_CALL) != 0;
    }

    public final void setCanCall(boolean called) {
        if (called) flags |= CAN_CALL; else flags &= ~CAN_CALL;
    }

    /** True if this is a method in an ClassExp. */
    public final boolean isClassMethod() {
        return (flags & CLASS_METHOD) != 0;
    }

    public final void setClassMethod(boolean isMethod) {
        if (isMethod) flags |= CLASS_METHOD; else flags &= ~CLASS_METHOD;
    }

    /** True iff this is the dummy top-level function of a module body. */
    public final boolean isModuleBody() {
        return this instanceof ModuleExp;
    }

    /** True if a class is generated for this procedure.  */
    public final boolean isClassGenerated() {
        return isModuleBody() || this instanceof ClassExp;
    }

    public boolean isAbstract() {
        return body == QuoteExp.abstractExp;
    }

    /** Specify the calling convention used for this function.
   * @return One of the CALL_WITH_xxx values in Compilation. */
    public int getCallConvention() {
        if (isModuleBody()) return ((Compilation.defaultCallConvention >= Compilation.CALL_WITH_CONSUMER) ? Compilation.defaultCallConvention : Compilation.CALL_WITH_CONSUMER);
        if (isClassMethod()) return Compilation.CALL_WITH_RETURN;
        return ((Compilation.defaultCallConvention != Compilation.CALL_WITH_UNSPECIFIED) ? Compilation.defaultCallConvention : Compilation.CALL_WITH_RETURN);
    }

    public final boolean isHandlingTailCalls() {
        return isModuleBody() || (Compilation.defaultCallConvention >= Compilation.CALL_WITH_TAILCALLS && !isClassMethod());
    }

    public final boolean variable_args() {
        return max_args < 0;
    }

    ClassType type = Compilation.typeProcedure;

    /** Return the ClassType of the Procedure this is being compiled into. */
    protected ClassType getCompiledClassType(Compilation comp) {
        if (type == Compilation.typeProcedure) throw new Error("internal error: getCompiledClassType");
        return type;
    }

    public Type getType() {
        return type;
    }

    public void setType(ClassType type) {
        this.type = type;
    }

    /** Number of argument variable actually passed by the caller.
   * For functions that accept more than 4 argument, or take a variable number,
   * this is 1, since in that all arguments are passed in a single array. */
    public int incomingArgs() {
        return min_args == max_args && max_args <= 4 && max_args > 0 ? max_args : 1;
    }

    /** If non-zero, the selector field of the ModuleMethod for this. */
    int selectorValue;

    int getSelectorValue(Compilation comp) {
        int s = selectorValue;
        if (s == 0) {
            s = comp.maxSelectorValue;
            comp.maxSelectorValue = s + primMethods.length;
            selectorValue = ++s;
        }
        return s;
    }

    /** Methods used to implement this functions.
   * primMethods[0] is used if the argument count is min_args;
   * primMethods[1] is used if the argument count is min_args+1;
   * primMethods[primMethods.length-1] is used otherwise.
   */
    Method[] primMethods;

    /** If in a ClassExp which isMakingClassPair, the static body methods.
   * Otherwise, same as primMethods. */
    Method[] primBodyMethods;

    /** Select the method used given an argument count. */
    public final Method getMethod(int argCount) {
        if (primMethods == null || (max_args >= 0 && argCount > max_args)) return null;
        int index = argCount - min_args;
        if (index < 0) return null;
        int length = primMethods.length;
        return primMethods[index < length ? index : length - 1];
    }

    /** Get the method that contains the actual body of the procedure.
   * (The other methods are just stubs that call that method.) */
    public final Method getMainMethod() {
        Method[] methods = primBodyMethods;
        return methods == null ? null : methods[methods.length - 1];
    }

    /** Return the parameter type of the "keyword/rest" parameters. */
    public final Type restArgType() {
        if (min_args == max_args) return null;
        if (primMethods == null) throw new Error("internal error - restArgType");
        Method[] methods = primMethods;
        if (max_args >= 0 && methods.length > max_args - min_args) return null;
        Method method = methods[methods.length - 1];
        Type[] types = method.getParameterTypes();
        int ilast = types.length - 1;
        if (method.getName().endsWith("$X")) ilast--;
        return types[ilast];
    }

    public LambdaExp outerLambda() {
        return outer == null ? null : outer.currentLambda();
    }

    /** Return the closest outer non-inlined LambdaExp. */
    public LambdaExp outerLambdaNotInline() {
        for (ScopeExp exp = this; (exp = exp.outer) != null; ) {
            if (exp instanceof LambdaExp) {
                LambdaExp result = (LambdaExp) exp;
                if (!result.getInlineOnly()) return result;
            }
        }
        return null;
    }

    /** True if given LambdaExp is inlined in this function, perhaps indirectly.
   * Is false if this is not inline-only or if getCaller() is not inlined is
   * outer.  Usually the same as (this.outerLambdaNotInline()==outer),
   * except in the case that outer.getInlineOnly(). */
    boolean inlinedIn(LambdaExp outer) {
        for (LambdaExp exp = this; exp.getInlineOnly(); exp = exp.getCaller()) {
            if (exp == outer) return true;
        }
        return false;
    }

    /** For an INLINE_ONLY function, return the function it gets inlined in. */
    public LambdaExp getCaller() {
        return inlineHome;
    }

    Variable thisVariable;

    public Variable declareThis(ClassType clas) {
        if (thisVariable == null) {
            thisVariable = new Variable("this");
            getVarScope().addVariableAfter(null, thisVariable);
            thisVariable.setParameter(true);
        }
        if (thisVariable.getType() == null) thisVariable.setType(clas);
        if (decls != null && decls.isThisParameter()) decls.var = thisVariable;
        return thisVariable;
    }

    public Variable declareClosureEnv() {
        if (closureEnv == null && getNeedsClosureEnv()) {
            LambdaExp parent = outerLambda();
            if (parent instanceof ClassExp) parent = parent.outerLambda();
            Variable parentFrame = parent.heapFrame != null ? parent.heapFrame : parent.closureEnv;
            if (isClassMethod() && !"*init*".equals(getName())) closureEnv = declareThis(type); else if (parent.heapFrame == null && !parent.getNeedsStaticLink() && !(parent instanceof ModuleExp)) closureEnv = null; else if (!isClassGenerated() && !getInlineOnly()) {
                Method primMethod = getMainMethod();
                boolean isInit = "*init*".equals(getName());
                if (!primMethod.getStaticFlag() && !isInit) closureEnv = declareThis(primMethod.getDeclaringClass()); else {
                    Type envType = primMethod.getParameterTypes()[0];
                    closureEnv = new Variable("closureEnv", envType);
                    Variable prev;
                    if (isInit) prev = declareThis(primMethod.getDeclaringClass()); else prev = null;
                    getVarScope().addVariableAfter(prev, closureEnv);
                    closureEnv.setParameter(true);
                }
            } else if (inlinedIn(parent)) closureEnv = parentFrame; else {
                closureEnv = new Variable("closureEnv", parentFrame.getType());
                getVarScope().addVariable(closureEnv);
            }
        }
        return closureEnv;
    }

    public LambdaExp() {
    }

    public LambdaExp(int args) {
        min_args = args;
        max_args = args;
    }

    public LambdaExp(Expression body) {
        this.body = body;
    }

    /** Generate code to load heapFrame on the JVM stack. */
    public void loadHeapFrame(Compilation comp) {
        LambdaExp curLambda = comp.curLambda;
        while (curLambda != this && curLambda.getInlineOnly()) curLambda = curLambda.getCaller();
        gnu.bytecode.CodeAttr code = comp.getCode();
        if (curLambda.heapFrame != null && this == curLambda) {
            code.emitLoad(curLambda.heapFrame);
            return;
        }
        ClassType curType;
        if (curLambda.closureEnv != null) {
            code.emitLoad(curLambda.closureEnv);
            curType = (ClassType) curLambda.closureEnv.getType();
        } else {
            code.emitPushThis();
            curType = comp.curClass;
        }
        while (curLambda != this) {
            Field link = curLambda.staticLinkField;
            if (link != null && link.getDeclaringClass() == curType) {
                code.emitGetField(link);
                curType = (ClassType) link.getType();
            }
            curLambda = curLambda.outerLambda();
        }
    }

    /** Get the i'the formal parameter. */
    Declaration getArg(int i) {
        for (Declaration var = firstDecl(); ; var = var.nextDecl()) {
            if (var == null) throw new Error("internal error - getArg");
            if (i == 0) return var;
            --i;
        }
    }

    public void compileEnd(Compilation comp) {
        gnu.bytecode.CodeAttr code = comp.getCode();
        if (!getInlineOnly()) {
            if (comp.method.reachableHere() && (Compilation.defaultCallConvention < Compilation.CALL_WITH_TAILCALLS || isModuleBody() || isClassMethod() || isHandlingTailCalls())) code.emitReturn();
            popScope(code);
            if (!Compilation.fewerClasses) code.popScope();
        }
        for (LambdaExp child = firstChild; child != null; ) {
            if (!child.getCanRead() && !child.getInlineOnly()) {
                child.compileAsMethod(comp);
            }
            child = child.nextSibling;
        }
        if (heapFrame != null) comp.generateConstructor(this);
    }

    public void generateApplyMethods(Compilation comp) {
        comp.generateMatchMethods(this);
        if (Compilation.defaultCallConvention >= Compilation.CALL_WITH_CONSUMER) comp.generateApplyMethodsWithContext(this); else comp.generateApplyMethodsWithoutContext(this);
    }

    Field allocFieldFor(Compilation comp) {
        if (nameDecl != null && nameDecl.field != null) return nameDecl.field;
        boolean needsClosure = getNeedsClosureEnv();
        ClassType frameType = needsClosure ? getOwningLambda().getHeapFrameType() : comp.mainClass;
        String name = getName();
        String fname = name == null ? "lambda" : Compilation.mangleNameIfNeeded(name);
        int fflags = Access.FINAL;
        if (nameDecl != null && nameDecl.context instanceof ModuleExp) {
            boolean external_access = nameDecl.needsExternalAccess();
            if (external_access) fname = Declaration.PRIVATE_PREFIX + fname;
            if (nameDecl.getFlag(Declaration.STATIC_SPECIFIED)) {
                fflags |= Access.STATIC;
                if (!((ModuleExp) nameDecl.context).isStatic()) fflags &= ~Access.FINAL;
            }
            if (!nameDecl.isPrivate() || external_access || comp.immediate) fflags |= Access.PUBLIC;
            if ((flags & OVERLOADABLE_FIELD) != 0) {
                String fname0 = fname;
                int suffix = min_args == max_args ? min_args : 1;
                do {
                    fname = fname0 + '$' + suffix++;
                } while (frameType.getDeclaredField(fname) != null);
            }
        } else {
            fname = fname + "$Fn" + ++comp.localFieldIndex;
            if (!needsClosure) fflags |= Access.STATIC;
        }
        Type rtype = Compilation.typeModuleMethod;
        Field field = frameType.addField(fname, rtype, fflags);
        if (nameDecl != null) nameDecl.field = field;
        return field;
    }

    final void addApplyMethod(Compilation comp, Field field) {
        LambdaExp owner = this;
        if (field != null && field.getStaticFlag()) owner = comp.getModule(); else {
            for (; ; ) {
                owner = owner.outerLambda();
                if (owner instanceof ModuleExp || owner.heapFrame != null) break;
            }
            ClassType frameType = owner.getHeapFrameType();
            if (!(frameType.getSuperclass().isSubtype(Compilation.typeModuleBody))) owner = comp.getModule();
        }
        if (owner.applyMethods == null) owner.applyMethods = new Vector();
        owner.applyMethods.addElement(this);
    }

    public Field compileSetField(Compilation comp) {
        Field field = allocFieldFor(comp);
        if (comp.usingCPStyle()) compile(comp, Type.objectType); else {
            compileAsMethod(comp);
            addApplyMethod(comp, field);
        }
        return (new ProcInitializer(this, comp, field)).field;
    }

    public void compile(Compilation comp, Target target) {
        if (target instanceof IgnoreTarget && (getInlineOnly() || !getCanRead())) return;
        Type rtype;
        CodeAttr code = comp.getCode();
        {
            LambdaExp outer = outerLambda();
            rtype = Compilation.typeModuleMethod;
            if ((flags & NO_FIELD) != 0 || (comp.immediate && outer instanceof ModuleExp)) {
                compileAsMethod(comp);
                addApplyMethod(comp, null);
                ProcInitializer.emitLoadModuleMethod(this, comp);
            } else {
                Field field = compileSetField(comp);
                if (field.getStaticFlag()) code.emitGetStatic(field); else {
                    LambdaExp parent = comp.curLambda;
                    Variable frame = parent.heapFrame != null ? parent.heapFrame : parent.closureEnv;
                    code.emitLoad(frame);
                    code.emitGetField(field);
                }
            }
        }
        target.compileFromStack(comp, rtype);
    }

    public ClassType getHeapFrameType() {
        if (this instanceof ModuleExp || this instanceof ClassExp) return (ClassType) getType(); else return (ClassType) heapFrame.getType();
    }

    public LambdaExp getOwningLambda() {
        ScopeExp exp = outer;
        for (; ; exp = exp.outer) {
            if (exp == null) return null;
            if (exp instanceof ModuleExp || (exp instanceof ClassExp && getNeedsClosureEnv()) || (exp instanceof LambdaExp && ((LambdaExp) exp).heapFrame != null)) return (LambdaExp) exp;
        }
    }

    void addMethodFor(Compilation comp, ObjectType closureEnvType) {
        ScopeExp sc = this;
        while (sc != null && !(sc instanceof ClassExp)) sc = sc.outer;
        ClassType ctype;
        if (sc != null) ctype = ((ClassExp) sc).instanceType; else ctype = getOwningLambda().getHeapFrameType();
        addMethodFor(ctype, comp, closureEnvType);
    }

    void addMethodFor(ClassType ctype, Compilation comp, ObjectType closureEnvType) {
        String name = getName();
        LambdaExp outer = outerLambda();
        int key_args = keywords == null ? 0 : keywords.length;
        int opt_args = defaultArgs == null ? 0 : defaultArgs.length - key_args;
        int numStubs = ((flags & DEFAULT_CAPTURES_ARG) != 0) ? 0 : opt_args;
        boolean varArgs = max_args < 0 || min_args + numStubs < max_args;
        Method[] methods = new Method[numStubs + 1];
        primBodyMethods = methods;
        if (primMethods == null) primMethods = methods;
        boolean isStatic;
        char isInitMethod = '\0';
        if (nameDecl != null && nameDecl.getFlag(Declaration.NONSTATIC_SPECIFIED)) isStatic = false; else if (nameDecl != null && nameDecl.getFlag(Declaration.STATIC_SPECIFIED)) isStatic = true; else if (isClassMethod()) {
            if (outer instanceof ClassExp) {
                ClassExp cl = (ClassExp) outer;
                isStatic = cl.isMakingClassPair() && closureEnvType != null;
                if (this == cl.initMethod) isInitMethod = 'I'; else if (this == cl.clinitMethod) {
                    isInitMethod = 'C';
                    isStatic = true;
                }
            } else isStatic = false;
        } else if (thisVariable != null || closureEnvType == ctype) isStatic = false; else if (nameDecl != null && nameDecl.context instanceof ModuleExp) {
            ModuleExp mexp = (ModuleExp) nameDecl.context;
            isStatic = mexp.getSuperType() == null && mexp.getInterfaces() == null;
        } else isStatic = true;
        StringBuffer nameBuf = new StringBuffer(60);
        int mflags = isStatic ? Access.STATIC : 0;
        if (nameDecl != null) {
            if (nameDecl.needsExternalAccess()) mflags |= Access.PUBLIC; else {
                short defaultFlag = nameDecl.isPrivate() ? 0 : Access.PUBLIC;
                if (isClassMethod()) defaultFlag = nameDecl.getAccessFlags(defaultFlag);
                mflags |= defaultFlag;
            }
        }
        if (!(outer.isModuleBody() || outer instanceof ClassExp) || name == null) {
            nameBuf.append("lambda");
            nameBuf.append(+(++comp.method_counter));
        }
        if (isInitMethod == 'C') nameBuf.append("<clinit>"); else if (getSymbol() != null) nameBuf.append(Compilation.mangleName(name));
        if (getFlag(SEQUENCE_RESULT)) nameBuf.append("$C");
        boolean withContext = (getCallConvention() >= Compilation.CALL_WITH_CONSUMER && isInitMethod == '\0');
        if (isInitMethod != '\0') {
            if (isStatic) {
                mflags = (mflags & ~Access.PROTECTED + Access.PRIVATE) + Access.PUBLIC;
            } else {
                mflags = (mflags & ~Access.PUBLIC + Access.PROTECTED) + Access.PRIVATE;
            }
        }
        if (ctype.isInterface() || isAbstract()) mflags |= Access.ABSTRACT;
        if (isClassMethod() && outer instanceof ClassExp && min_args == max_args) {
            Method[] inherited = null;
            int iarg = 0;
            param_loop: for (Declaration param = firstDecl(); ; param = param.nextDecl(), iarg++) {
                if (param == null) {
                    if (returnType != null) break;
                } else if (param.isThisParameter()) {
                    iarg--;
                    continue;
                } else if (param.getFlag(Declaration.TYPE_SPECIFIED)) continue;
                if (inherited == null) {
                    final String mangled = nameBuf.toString();
                    gnu.bytecode.Filter filter = new gnu.bytecode.Filter() {

                        public boolean select(Object value) {
                            gnu.bytecode.Method method = (gnu.bytecode.Method) value;
                            if (!method.getName().equals(mangled)) return false;
                            Type[] ptypes = method.getParameterTypes();
                            return ptypes.length == min_args;
                        }
                    };
                    inherited = ctype.getMethods(filter, 2);
                }
                Type type = null;
                for (int i = inherited.length; --i >= 0; ) {
                    Method method = inherited[i];
                    Type ptype = param == null ? method.getReturnType() : method.getParameterTypes()[iarg];
                    if (type == null) type = ptype; else if (ptype != type) {
                        if (param == null) break param_loop; else continue param_loop;
                    }
                }
                if (type != null) {
                    if (param != null) param.setType(type); else setCoercedReturnType(type);
                }
                if (param == null) break param_loop;
            }
        }
        Type rtype = (getFlag(SEQUENCE_RESULT) || getCallConvention() >= Compilation.CALL_WITH_CONSUMER) ? Type.voidType : getReturnType().getImplementationType();
        int extraArg = (closureEnvType != null && closureEnvType != ctype) ? 1 : 0;
        int ctxArg = 0;
        if (getCallConvention() >= Compilation.CALL_WITH_CONSUMER && isInitMethod == '\0') ctxArg = 1;
        int nameBaseLength = nameBuf.length();
        for (int i = 0; i <= numStubs; i++) {
            nameBuf.setLength(nameBaseLength);
            int plainArgs = min_args + i;
            int numArgs = plainArgs;
            if (i == numStubs && varArgs) numArgs++;
            Type[] atypes = new Type[extraArg + numArgs + ctxArg];
            if (extraArg > 0) atypes[0] = closureEnvType;
            Declaration var = firstDecl();
            if (var != null && var.isThisParameter()) var = var.nextDecl();
            for (int itype = 0; itype < plainArgs; var = var.nextDecl()) atypes[extraArg + itype++] = var.getType().getImplementationType();
            if (ctxArg != 0) atypes[atypes.length - 1] = Compilation.typeCallContext;
            if (plainArgs < numArgs) {
                Type lastType = var.getType();
                String lastTypeName = lastType.getName();
                if (ctype.getClassfileVersion() >= ClassType.JDK_1_5_VERSION && "java.lang.Object[]".equals(lastTypeName)) mflags |= Access.VARARGS; else nameBuf.append("$V");
                if (key_args > 0 || numStubs < opt_args || !("gnu.lists.LList".equals(lastTypeName) || "java.lang.Object[]".equals(lastTypeName))) {
                    lastType = Compilation.objArrayType;
                    argsArray = new Variable("argsArray", Compilation.objArrayType);
                    argsArray.setParameter(true);
                }
                firstArgsArrayArg = var;
                atypes[atypes.length - (withContext ? 2 : 1)] = lastType;
            }
            if (withContext) nameBuf.append("$X");
            boolean classSpecified = (outer instanceof ClassExp || (outer instanceof ModuleExp && (((ModuleExp) outer).getFlag(ModuleExp.SUPERTYPE_SPECIFIED))));
            name = nameBuf.toString();
            {
                int renameCount = 0;
                int len = nameBuf.length();
                retry: for (; ; ) {
                    for (ClassType t = ctype; t != null; t = t.getSuperclass()) {
                        if (t.getDeclaredMethod(name, atypes) != null) {
                            nameBuf.setLength(len);
                            nameBuf.append('$');
                            nameBuf.append(++renameCount);
                            name = nameBuf.toString();
                            continue retry;
                        }
                        if (classSpecified) break;
                    }
                    break;
                }
            }
            Method method = ctype.addMethod(name, atypes, rtype, mflags);
            methods[i] = method;
            if (throwsSpecification != null && throwsSpecification.length > 0) {
                int n = throwsSpecification.length;
                ClassType[] exceptions = new ClassType[n];
                for (int j = 0; j < n; j++) {
                    ClassType exception = null;
                    Declaration decl = throwsSpecification[j].getBinding();
                    if (decl != null) {
                        Expression declValue = decl.getValue();
                        if (declValue instanceof ClassExp) exception = ((ClassExp) declValue).getCompiledClassType(comp); else comp.error('e', "throws specification " + decl.getName() + " has non-class lexical binding");
                    }
                    if (exception == null) {
                        String exName = throwsSpecification[j].getName();
                        int nlen = exName.length();
                        if (nlen > 2 && exName.charAt(0) == '<' && exName.charAt(nlen - 1) == '>') exName = exName.substring(1, nlen - 1);
                        exception = ClassType.make(exName);
                    }
                    exceptions[j] = exception;
                }
                ExceptionsAttr attr = new ExceptionsAttr(method);
                attr.setExceptions(exceptions);
            }
        }
    }

    public void allocChildClasses(Compilation comp) {
        Method main = getMainMethod();
        if (main != null && !main.getStaticFlag()) declareThis(main.getDeclaringClass());
        Declaration decl = firstDecl();
        for (; ; ) {
            if (decl == firstArgsArrayArg && argsArray != null) {
                getVarScope().addVariable(argsArray);
            }
            if (!getInlineOnly() && getCallConvention() >= Compilation.CALL_WITH_CONSUMER && (firstArgsArrayArg == null ? decl == null : argsArray != null ? decl == firstArgsArrayArg : decl == firstArgsArrayArg.nextDecl())) {
                Variable var = getVarScope().addVariable(null, Compilation.typeCallContext, "$ctx");
                var.setParameter(true);
            }
            if (decl == null) break;
            Variable var = decl.var;
            if (var != null || (getInlineOnly() && decl.ignorable())) ; else if (decl.isSimple() && !decl.isIndirectBinding()) {
                var = decl.allocateVariable(null);
            } else {
                String vname = Compilation.mangleName(decl.getName()).intern();
                Type vtype = decl.getType().getImplementationType();
                var = decl.var = getVarScope().addVariable(null, vtype, vname);
                var.setParameter(true);
            }
            decl = decl.nextDecl();
        }
        declareClosureEnv();
        allocFrame(comp);
        allocChildMethods(comp);
    }

    void allocChildMethods(Compilation comp) {
        for (LambdaExp child = firstChild; child != null; child = child.nextSibling) {
            if (!child.isClassGenerated() && !child.getInlineOnly()) {
                ObjectType closureEnvType;
                if (!child.getNeedsClosureEnv()) closureEnvType = null; else if (this instanceof ClassExp || this instanceof ModuleExp) closureEnvType = getCompiledClassType(comp); else {
                    LambdaExp owner = this;
                    while (owner.heapFrame == null) owner = owner.outerLambda();
                    closureEnvType = (ClassType) owner.heapFrame.getType();
                }
                child.addMethodFor(comp, closureEnvType);
            }
            if (child instanceof ClassExp) {
                ClassExp cl = (ClassExp) child;
                if (cl.getNeedsClosureEnv()) {
                    ClassType parentFrameType;
                    if (this instanceof ModuleExp || this instanceof ClassExp) parentFrameType = (ClassType) getType(); else {
                        Variable parentFrame = this.heapFrame != null ? this.heapFrame : this.closureEnv;
                        parentFrameType = (ClassType) parentFrame.getType();
                    }
                    cl.closureEnvField = cl.staticLinkField = cl.instanceType.setOuterLink(parentFrameType);
                }
            }
        }
    }

    public void allocFrame(Compilation comp) {
        if (heapFrame != null) {
            ClassType frameType;
            if (this instanceof ModuleExp || this instanceof ClassExp) frameType = getCompiledClassType(comp); else {
                frameType = new ClassType(comp.generateClassName("frame"));
                frameType.setSuper(comp.getModuleType());
                comp.addClass(frameType);
            }
            heapFrame.setType(frameType);
        }
    }

    void allocParameters(Compilation comp) {
        CodeAttr code = comp.getCode();
        int i = 0;
        int j = 0;
        code.locals.enterScope(getVarScope());
        int line = getLineNumber();
        if (line > 0) code.putLineNumber(getFileName(), line);
        for (Declaration decl = firstDecl(); decl != null; ) {
            if (argsArray != null && min_args == max_args && primMethods == null && getCallConvention() < Compilation.CALL_WITH_CONSUMER) {
                code.emitLoad(argsArray);
                code.emitPushInt(j);
                code.emitArrayLoad(Type.objectType);
                decl.getType().emitCoerceFromObject(code);
                code.emitStore(decl.getVariable());
            }
            j++;
            i++;
            decl = decl.nextDecl();
        }
        if (heapFrame != null) heapFrame.allocateLocal(code);
    }

    static Method searchForKeywordMethod3;

    static Method searchForKeywordMethod4;

    /** Rembembers stuff to do in <init> of this class. */
    Initializer initChain;

    void enterFunction(Compilation comp) {
        CodeAttr code = comp.getCode();
        getVarScope().noteStartFunction(code);
        if (closureEnv != null && !closureEnv.isParameter() && !comp.usingCPStyle()) {
            if (!getInlineOnly()) {
                code.emitPushThis();
                Field field = closureEnvField;
                if (field == null) field = outerLambda().closureEnvField;
                code.emitGetField(field);
                code.emitStore(closureEnv);
            } else if (!inlinedIn(outerLambda())) {
                outerLambda().loadHeapFrame(comp);
                code.emitStore(closureEnv);
            }
        }
        if (!comp.usingCPStyle()) {
            ClassType frameType = heapFrame == null ? currentModule().getCompiledClassType(comp) : (ClassType) heapFrame.getType();
            for (Declaration decl = capturedVars; decl != null; decl = decl.nextCapturedVar) {
                if (decl.field != null) continue;
                decl.makeField(frameType, comp, null);
            }
        }
        if (heapFrame != null && !comp.usingCPStyle()) {
            ClassType frameType = (ClassType) heapFrame.getType();
            if (closureEnv != null && !(this instanceof ModuleExp)) staticLinkField = frameType.addField("staticLink", closureEnv.getType());
            if (!(this instanceof ModuleExp) && !(this instanceof ClassExp)) {
                frameType.setEnclosingMember(comp.method);
                code.emitNew(frameType);
                code.emitDup(frameType);
                Method constructor = Compilation.getConstructor(frameType, this);
                code.emitInvokeSpecial(constructor);
                if (staticLinkField != null) {
                    code.emitDup(frameType);
                    code.emitLoad(closureEnv);
                    code.emitPutField(staticLinkField);
                }
                code.emitStore(heapFrame);
            }
        }
        Variable argsArray = this.argsArray;
        if (min_args == max_args && !Compilation.fewerClasses && primMethods == null && getCallConvention() < Compilation.CALL_WITH_CONSUMER) argsArray = null;
        int i = 0;
        int opt_i = 0;
        int key_i = 0;
        int key_args = keywords == null ? 0 : keywords.length;
        int opt_args = defaultArgs == null ? 0 : defaultArgs.length - key_args;
        if (this instanceof ModuleExp) return;
        int plainArgs = -1;
        int defaultStart = 0;
        Method mainMethod = getMainMethod();
        Variable callContextSave = comp.callContextVar;
        for (Declaration param = firstDecl(); param != null; param = param.nextDecl()) {
            comp.callContextVar = (getCallConvention() < Compilation.CALL_WITH_CONSUMER ? null : getVarScope().lookup("$ctx"));
            if (param == firstArgsArrayArg && argsArray != null) {
                if (primMethods != null) {
                    plainArgs = i;
                    defaultStart = plainArgs - min_args;
                } else {
                    plainArgs = 0;
                    defaultStart = 0;
                }
            }
            if (plainArgs >= 0 || !param.isSimple() || param.isIndirectBinding()) {
                Type paramType = param.getType();
                Type stackType = (mainMethod == null || plainArgs >= 0 ? Type.objectType : paramType);
                if (!param.isSimple()) param.loadOwningObject(null, comp);
                if (plainArgs < 0) {
                    code.emitLoad(param.getVariable());
                } else if (i < min_args) {
                    code.emitLoad(argsArray);
                    code.emitPushInt(i);
                    code.emitArrayLoad(Type.objectType);
                } else if (i < min_args + opt_args) {
                    code.emitPushInt(i - plainArgs);
                    code.emitLoad(argsArray);
                    code.emitArrayLength();
                    code.emitIfIntLt();
                    code.emitLoad(argsArray);
                    code.emitPushInt(i - plainArgs);
                    code.emitArrayLoad();
                    code.emitElse();
                    defaultArgs[defaultStart + opt_i++].compile(comp, paramType);
                    code.emitFi();
                } else if (max_args < 0 && i == min_args + opt_args) {
                    code.emitLoad(argsArray);
                    code.emitPushInt(i - plainArgs);
                    code.emitInvokeStatic(Compilation.makeListMethod);
                    stackType = Compilation.scmListType;
                } else {
                    code.emitLoad(argsArray);
                    code.emitPushInt(min_args + opt_args - plainArgs);
                    comp.compileConstant(keywords[key_i++]);
                    Expression defaultArg = defaultArgs[defaultStart + opt_i++];
                    if (defaultArg instanceof QuoteExp) {
                        if (searchForKeywordMethod4 == null) {
                            Type[] argts = new Type[4];
                            argts[0] = Compilation.objArrayType;
                            argts[1] = Type.intType;
                            argts[2] = Type.objectType;
                            argts[3] = Type.objectType;
                            searchForKeywordMethod4 = Compilation.scmKeywordType.addMethod("searchForKeyword", argts, Type.objectType, Access.PUBLIC | Access.STATIC);
                        }
                        defaultArg.compile(comp, paramType);
                        code.emitInvokeStatic(searchForKeywordMethod4);
                    } else {
                        if (searchForKeywordMethod3 == null) {
                            Type[] argts = new Type[3];
                            argts[0] = Compilation.objArrayType;
                            argts[1] = Type.intType;
                            argts[2] = Type.objectType;
                            searchForKeywordMethod3 = Compilation.scmKeywordType.addMethod("searchForKeyword", argts, Type.objectType, Access.PUBLIC | Access.STATIC);
                        }
                        code.emitInvokeStatic(searchForKeywordMethod3);
                        code.emitDup(1);
                        comp.compileConstant(Special.dfault);
                        code.emitIfEq();
                        code.emitPop(1);
                        defaultArg.compile(comp, paramType);
                        code.emitFi();
                    }
                }
                if (paramType != stackType) CheckedTarget.emitCheckedCoerce(comp, this, i + 1, paramType);
                if (param.isIndirectBinding()) param.pushIndirectBinding(comp);
                if (param.isSimple()) code.emitStore(param.getVariable()); else code.emitPutField(param.field);
            }
            i++;
        }
        comp.callContextVar = callContextSave;
    }

    void compileAsMethod(Compilation comp) {
        if ((flags & METHODS_COMPILED) != 0 || isAbstract()) return;
        flags |= METHODS_COMPILED;
        if (primMethods == null) return;
        Method save_method = comp.method;
        LambdaExp save_lambda = comp.curLambda;
        comp.curLambda = this;
        Method method = primMethods[0];
        boolean isStatic = method.getStaticFlag();
        int numStubs = primMethods.length - 1;
        Type restArgType = restArgType();
        int[] saveDeclFlags = null;
        if (numStubs > 0) {
            saveDeclFlags = new int[min_args + numStubs];
            int k = 0;
            for (Declaration decl = firstDecl(); k < min_args + numStubs; decl = decl.nextDecl()) saveDeclFlags[k++] = decl.flags;
        }
        boolean ctxArg = getCallConvention() >= Compilation.CALL_WITH_CONSUMER;
        for (int i = 0; i <= numStubs; i++) {
            comp.method = primMethods[i];
            if (i < numStubs) {
                CodeAttr code = comp.method.startCode();
                int toCall = i + 1;
                while (toCall < numStubs && defaultArgs[toCall] instanceof QuoteExp) toCall++;
                boolean varArgs = toCall == numStubs && restArgType != null;
                Declaration decl;
                Variable callContextSave = comp.callContextVar;
                Variable var = code.getArg(0);
                if (!isStatic) {
                    code.emitPushThis();
                    if (getNeedsClosureEnv()) closureEnv = var;
                    var = code.getArg(1);
                }
                decl = firstDecl();
                for (int j = 0; j < min_args + i; j++, decl = decl.nextDecl()) {
                    decl.flags |= Declaration.IS_SIMPLE;
                    decl.var = var;
                    code.emitLoad(var);
                    var = var.nextVar();
                }
                comp.callContextVar = ctxArg ? var : null;
                for (int j = i; j < toCall; j++, decl = decl.nextDecl()) {
                    Target paramTarget = StackTarget.getInstance(decl.getType());
                    defaultArgs[j].compile(comp, paramTarget);
                }
                if (varArgs) {
                    Expression arg;
                    String lastTypeName = restArgType.getName();
                    if ("gnu.lists.LList".equals(lastTypeName)) arg = new QuoteExp(gnu.lists.LList.Empty); else if ("java.lang.Object[]".equals(lastTypeName)) arg = new QuoteExp(Values.noArgs); else throw new Error("unimplemented #!rest type " + lastTypeName);
                    arg.compile(comp, restArgType);
                }
                if (ctxArg) code.emitLoad(var);
                if (isStatic) code.emitInvokeStatic(primMethods[toCall]); else code.emitInvokeVirtual(primMethods[toCall]);
                code.emitReturn();
                closureEnv = null;
                comp.callContextVar = callContextSave;
            } else {
                if (saveDeclFlags != null) {
                    int k = 0;
                    for (Declaration decl = firstDecl(); k < min_args + numStubs; decl = decl.nextDecl()) {
                        decl.flags = saveDeclFlags[k++];
                        decl.var = null;
                    }
                }
                comp.method.initCode();
                allocChildClasses(comp);
                allocParameters(comp);
                enterFunction(comp);
                compileBody(comp);
                compileEnd(comp);
                generateApplyMethods(comp);
            }
        }
        comp.method = save_method;
        comp.curLambda = save_lambda;
    }

    public void compileBody(Compilation comp) {
        Target target;
        Variable callContextSave = comp.callContextVar;
        comp.callContextVar = null;
        if (getCallConvention() >= Compilation.CALL_WITH_CONSUMER) {
            Variable var = getVarScope().lookup("$ctx");
            if (var != null && var.getType() == Compilation.typeCallContext) comp.callContextVar = var;
            target = ConsumerTarget.makeContextTarget(comp);
        } else target = Target.pushValue(getReturnType());
        body.compileWithPosition(comp, target, body.getLineNumber() > 0 ? body : this);
        comp.callContextVar = callContextSave;
    }

    /** A cache if this has already been evaluated. */
    Procedure thisValue;

    protected Expression walk(ExpWalker walker) {
        return walker.walkLambdaExp(this);
    }

    protected void walkChildren(ExpWalker walker) {
        walkChildrenOnly(walker);
        walkProperties(walker);
    }

    protected final void walkChildrenOnly(ExpWalker walker) {
        LambdaExp save = walker.currentLambda;
        walker.currentLambda = this;
        try {
            walker.walkDefaultArgs(this);
            if (walker.exitValue == null && body != null) body = walker.walk(body);
        } finally {
            walker.currentLambda = save;
        }
    }

    protected final void walkProperties(ExpWalker walker) {
        if (properties != null) {
            int len = properties.length;
            for (int i = 1; i < len; i += 2) {
                Object val = properties[i];
                if (val instanceof Expression) {
                    properties[i] = walker.walk((Expression) properties[i]);
                }
            }
        }
    }

    protected boolean mustCompile() {
        if (keywords != null && keywords.length > 0) return true;
        if (defaultArgs != null) {
            for (int i = defaultArgs.length; --i >= 0; ) {
                Expression def = defaultArgs[i];
                if (def != null && !(def instanceof QuoteExp)) return true;
            }
        }
        return false;
    }

    public void apply(CallContext ctx) throws Throwable {
        setIndexes();
        ctx.writeValue(new Closure(this, ctx));
    }

    Object evalDefaultArg(int index, CallContext ctx) {
        try {
            return defaultArgs[index].eval(ctx);
        } catch (Throwable ex) {
            throw new WrappedException("error evaluating default argument", ex);
        }
    }

    public Expression inline(ApplyExp exp, InlineCalls walker, Declaration decl, boolean argsInlined) {
        Expression[] args = exp.getArgs();
        if (!argsInlined) {
            if ((flags & ATTEMPT_INLINE) != 0) {
                Expression inlined = InlineCalls.inlineCall(this, args, true);
                if (inlined != null) return walker.walk(inlined);
            }
            exp.args = walker.walkExps(exp.args, exp.args.length);
        }
        int args_length = exp.args.length;
        String msg = WrongArguments.checkArgCount(getName(), min_args, max_args, args_length);
        if (msg != null) return walker.noteError(msg);
        int conv = getCallConvention();
        Compilation comp = walker.getCompilation();
        Method method;
        if (comp.inlineOk(this) && isClassMethod() && (conv <= Compilation.CALL_WITH_CONSUMER || (conv == Compilation.CALL_WITH_TAILCALLS)) && (method = getMethod(args_length)) != null) {
            boolean isStatic = nameDecl.isStatic();
            if (!isStatic && outer instanceof ClassExp) {
                ClassExp cl = (ClassExp) outer;
                if (cl.isMakingClassPair()) {
                }
            }
            PrimProcedure mproc = new PrimProcedure(method, this);
            Expression[] margs;
            if (isStatic) margs = exp.args; else {
                LambdaExp curLambda = walker.getCurrentLambda();
                for (; ; ) {
                    if (curLambda == null) return walker.noteError("internal error: missing " + this);
                    if (curLambda.outer == outer) break;
                    curLambda = curLambda.outerLambda();
                }
                Declaration d = curLambda.firstDecl();
                if (d == null || !d.isThisParameter()) return walker.noteError("calling non-static method " + getName() + " from static method " + curLambda.getName());
                int nargs = exp.getArgCount();
                margs = new Expression[1 + nargs];
                System.arraycopy(exp.getArgs(), 0, margs, 1, nargs);
                margs[0] = new ThisExp(d);
            }
            ApplyExp nexp = new ApplyExp(mproc, margs);
            return nexp.setLine(exp);
        }
        return exp;
    }

    public void print(OutPort out) {
        out.startLogicalBlock("(Lambda/", ")", 2);
        Object sym = getSymbol();
        if (sym != null) {
            out.print(sym);
            out.print('/');
        }
        out.print(id);
        out.print('/');
        out.print("fl:");
        out.print(Integer.toHexString(flags));
        out.writeSpaceFill();
        printLineColumn(out);
        out.startLogicalBlock("(", false, ")");
        Special prevMode = null;
        int i = 0;
        int opt_i = 0;
        int key_args = keywords == null ? 0 : keywords.length;
        int opt_args = defaultArgs == null ? 0 : defaultArgs.length - key_args;
        Declaration decl = firstDecl();
        if (decl != null && decl.isThisParameter()) i = -1;
        for (; decl != null; decl = decl.nextDecl()) {
            Special mode;
            if (i < min_args) mode = null; else if (i < min_args + opt_args) mode = Special.optional; else if (max_args < 0 && i == min_args + opt_args) mode = Special.rest; else mode = Special.key;
            if (decl != firstDecl()) out.writeSpaceFill();
            if (mode != prevMode) {
                out.print(mode);
                out.writeSpaceFill();
            }
            Expression defaultArg = null;
            if (mode == Special.optional || mode == Special.key) defaultArg = defaultArgs[opt_i++];
            if (defaultArg != null) out.print('(');
            decl.printInfo(out);
            if (defaultArg != null && defaultArg != QuoteExp.falseExp) {
                out.print(' ');
                defaultArg.print(out);
                out.print(')');
            }
            i++;
            prevMode = mode;
        }
        out.endLogicalBlock(")");
        out.writeSpaceLinear();
        if (body == null) out.print("<null body>"); else body.print(out);
        out.endLogicalBlock(")");
    }

    protected final String getExpClassName() {
        String cname = getClass().getName();
        int index = cname.lastIndexOf('.');
        if (index >= 0) cname = cname.substring(index + 1);
        return cname;
    }

    public String toString() {
        String str = getExpClassName() + ':' + getSymbol() + '/' + id + '/';
        int l = getLineNumber();
        if (l <= 0 && body != null) l = body.getLineNumber();
        if (l > 0) str = str + "l:" + l;
        return str;
    }

    /** If non-null, a sequence of (key, value)-pairs.
   * These will be used to call setProperty at run-time. */
    Object[] properties;

    public Object getProperty(Object key, Object defaultValue) {
        if (properties != null) {
            for (int i = properties.length; (i -= 2) >= 0; ) {
                if (properties[i] == key) return properties[i + 1];
            }
        }
        return defaultValue;
    }

    public synchronized void setProperty(Object key, Object value) {
        properties = PropertySet.setProperty(properties, key, value);
    }

    /** If non-null, the type of values returned by this function.
   * If null, the return type has not been set or calculated yet. */
    public Type returnType;

    /** The return type of this function, i.e the type of its returned values. */
    public final Type getReturnType() {
        if (returnType == null) {
            returnType = Type.objectType;
            if (body != null && !isAbstract()) returnType = body.getType();
        }
        return returnType;
    }

    public final void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    public final void setCoercedReturnType(Type returnType) {
        this.returnType = returnType;
        if (returnType != null && returnType != Type.objectType && returnType != Type.voidType && body != QuoteExp.abstractExp) {
            Expression value = body;
            body = Convert.makeCoercion(value, returnType);
            body.setLine(value);
        }
    }
}

class Closure extends MethodProc {

    Object[][] evalFrames;

    LambdaExp lambda;

    public int numArgs() {
        return lambda.min_args | (lambda.max_args << 12);
    }

    public Closure(LambdaExp lexp, CallContext ctx) {
        this.lambda = lexp;
        Object[][] oldFrames = ctx.evalFrames;
        if (oldFrames != null) {
            int n = oldFrames.length;
            while (n > 0 && oldFrames[n - 1] == null) n--;
            evalFrames = new Object[n][];
            System.arraycopy(oldFrames, 0, evalFrames, 0, n);
        }
        setSymbol(lambda.getSymbol());
    }

    public int match0(CallContext ctx) {
        return matchN(new Object[] {}, ctx);
    }

    public int match1(Object arg1, CallContext ctx) {
        return matchN(new Object[] { arg1 }, ctx);
    }

    public int match2(Object arg1, Object arg2, CallContext ctx) {
        return matchN(new Object[] { arg1, arg2 }, ctx);
    }

    public int match3(Object arg1, Object arg2, Object arg3, CallContext ctx) {
        return matchN(new Object[] { arg1, arg2, arg3 }, ctx);
    }

    public int match4(Object arg1, Object arg2, Object arg3, Object arg4, CallContext ctx) {
        return matchN(new Object[] { arg1, arg2, arg3, arg4 }, ctx);
    }

    public int matchN(Object[] args, CallContext ctx) {
        int num = numArgs();
        int nargs = args.length;
        int min = num & 0xFFF;
        if (nargs < min) return MethodProc.NO_MATCH_TOO_FEW_ARGS | min;
        int max = num >> 12;
        if (nargs > max && max >= 0) return MethodProc.NO_MATCH_TOO_MANY_ARGS | max;
        Object[] evalFrame = new Object[lambda.frameSize];
        int key_args = lambda.keywords == null ? 0 : lambda.keywords.length;
        int opt_args = lambda.defaultArgs == null ? 0 : lambda.defaultArgs.length - key_args;
        int i = 0;
        int opt_i = 0;
        int key_i = 0;
        int min_args = lambda.min_args;
        for (Declaration decl = lambda.firstDecl(); decl != null; decl = decl.nextDecl()) {
            Object value;
            if (i < min_args) value = args[i++]; else if (i < min_args + opt_args) {
                if (i < nargs) value = args[i++]; else value = lambda.evalDefaultArg(opt_i, ctx);
                opt_i++;
            } else if (lambda.max_args < 0 && i == min_args + opt_args) {
                if (decl.type instanceof ArrayType) {
                    int rem = nargs - i;
                    Type elementType = ((ArrayType) decl.type).getComponentType();
                    if (elementType == Type.objectType) {
                        Object[] rest = new Object[rem];
                        System.arraycopy(args, i, rest, 0, rem);
                        value = rest;
                    } else {
                        Class elementClass = elementType.getReflectClass();
                        value = java.lang.reflect.Array.newInstance(elementClass, rem);
                        for (int j = 0; j < rem; j++) {
                            Object el;
                            try {
                                el = elementType.coerceFromObject(args[i + j]);
                            } catch (ClassCastException ex) {
                                return NO_MATCH_BAD_TYPE | (i + j);
                            }
                            java.lang.reflect.Array.set(value, j, el);
                        }
                    }
                } else value = LList.makeList(args, i);
            } else {
                Keyword keyword = lambda.keywords[key_i++];
                int key_offset = min_args + opt_args;
                value = Keyword.searchForKeyword(args, key_offset, keyword);
                if (value == Special.dfault) value = lambda.evalDefaultArg(opt_i, ctx);
                opt_i++;
            }
            if (decl.type != null) {
                try {
                    value = decl.type.coerceFromObject(value);
                } catch (ClassCastException ex) {
                    return NO_MATCH_BAD_TYPE | i;
                }
            }
            if (decl.isIndirectBinding()) {
                gnu.mapping.Location loc = decl.makeIndirectLocationFor();
                loc.set(value);
                value = loc;
            }
            evalFrame[decl.evalIndex] = value;
        }
        ctx.values = evalFrame;
        ctx.where = 0;
        ctx.next = 0;
        ctx.proc = this;
        return 0;
    }

    public void apply(CallContext ctx) throws Throwable {
        int level = ScopeExp.nesting(lambda);
        Object[] evalFrame = ctx.values;
        Object[][] saveFrames = ctx.evalFrames;
        int numFrames = evalFrames == null ? 0 : evalFrames.length;
        if (level >= numFrames) numFrames = level;
        numFrames += 10;
        Object[][] newFrames = new Object[numFrames][];
        if (evalFrames != null) System.arraycopy(evalFrames, 0, newFrames, 0, evalFrames.length);
        newFrames[level] = evalFrame;
        ctx.evalFrames = newFrames;
        try {
            if (lambda.body == null) {
                StringBuffer sbuf = new StringBuffer("procedure ");
                String name = lambda.getName();
                if (name == null) name = "<anonymous>";
                sbuf.append(name);
                int line = lambda.getLineNumber();
                if (line > 0) {
                    sbuf.append(" at line ");
                    sbuf.append(line);
                }
                sbuf.append(" was called before it was expanded");
                throw new RuntimeException(sbuf.toString());
            }
            lambda.body.apply(ctx);
        } finally {
            ctx.evalFrames = saveFrames;
        }
    }

    public Object getProperty(Object key, Object defaultValue) {
        Object value = super.getProperty(key, defaultValue);
        if (value == null) value = lambda.getProperty(key, defaultValue);
        return value;
    }
}
