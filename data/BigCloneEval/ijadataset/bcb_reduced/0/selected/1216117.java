package com2.sun.tools.javac.jvm;

import java.util.*;
import com2.sun.tools.javac.util.*;
import com2.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com2.sun.tools.javac.util.List;
import com2.sun.tools.javac.code.*;
import com2.sun.tools.javac.comp.*;
import com2.sun.tools.javac.tree.*;
import com2.sun.tools.javac.code.Symbol.*;
import com2.sun.tools.javac.code.Type.*;
import com2.sun.tools.javac.jvm.Code.*;
import com2.sun.tools.javac.jvm.Items.*;
import com2.sun.tools.javac.tree.JCTree.*;
import static com2.sun.tools.javac.code.Flags.*;
import static com2.sun.tools.javac.code.Kinds.*;
import static com2.sun.tools.javac.code.TypeTags.*;
import static com2.sun.tools.javac.jvm.ByteCodes.*;
import static com2.sun.tools.javac.jvm.CRTFlags.*;

/** This pass maps flat Java (i.e. without inner classes) to bytecodes.
 *
 *  <p><b>This is NOT part of any API supported by Sun Microsystems.  If
 *  you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
@Version("@(#)Gen.java	1.154 07/06/14")
public class Gen extends JCTree.Visitor {

    protected static final Context.Key<Gen> genKey = new Context.Key<Gen>();

    private final Log log;

    private final Symtab syms;

    private final Check chk;

    private final Resolve rs;

    private final TreeMaker make;

    private final Name.Table names;

    private final Target target;

    private final Type stringBufferType;

    private final Map<Type, Symbol> stringBufferAppend;

    private Name accessDollar;

    private final Types types;

    /** Switch: GJ mode?
     */
    private final boolean allowGenerics;

    /** Set when Miranda method stubs are to be generated. */
    private final boolean generateIproxies;

    /** Format of stackmap tables to be generated. */
    private final Code.StackMapFormat stackMap;

    /** A type that serves as the expected type for all method expressions.
     */
    private final Type methodType;

    public static Gen instance(Context context) {
        Gen instance = context.get(genKey);
        if (instance == null) instance = new Gen(context);
        return instance;
    }

    protected Gen(Context context) {
        context.put(genKey, this);
        names = Name.Table.instance(context);
        log = Log.instance(context);
        syms = Symtab.instance(context);
        chk = Check.instance(context);
        rs = Resolve.instance(context);
        make = TreeMaker.instance(context);
        target = Target.instance(context);
        types = Types.instance(context);
        methodType = new MethodType(null, null, null, syms.methodClass);
        allowGenerics = Source.instance(context).allowGenerics();
        stringBufferType = target.useStringBuilder() ? syms.stringBuilderType : syms.stringBufferType;
        stringBufferAppend = new HashMap<Type, Symbol>();
        accessDollar = names.fromString("access" + target.syntheticNameChar());
        Options options = Options.instance(context);
        lineDebugInfo = options.get("-g:") == null || options.get("-g:lines") != null;
        varDebugInfo = options.get("-g:") == null ? options.get("-g") != null : options.get("-g:vars") != null;
        genCrt = options.get("-Xjcov") != null;
        debugCode = options.get("debugcode") != null;
        generateIproxies = target.requiresIproxy() || options.get("miranda") != null;
        if (target.generateStackMapTable()) {
            this.stackMap = StackMapFormat.JSR202;
        } else {
            if (target.generateCLDCStackmap()) {
                this.stackMap = StackMapFormat.CLDC;
            } else {
                this.stackMap = StackMapFormat.NONE;
            }
        }
        int setjsrlimit = 50;
        String jsrlimitString = options.get("jsrlimit");
        if (jsrlimitString != null) {
            try {
                setjsrlimit = Integer.parseInt(jsrlimitString);
            } catch (NumberFormatException ex) {
            }
        }
        this.jsrlimit = setjsrlimit;
        this.useJsrLocally = false;
    }

    /** Switches
     */
    private final boolean lineDebugInfo;

    private final boolean varDebugInfo;

    private final boolean genCrt;

    private final boolean debugCode;

    /** Default limit of (approximate) size of finalizer to inline.
     *  Zero means always use jsr.  100 or greater means never use
     *  jsr.
     */
    private final int jsrlimit;

    /** True if jsr is used.
     */
    private boolean useJsrLocally;

    private Pool pool = new Pool();

    /** Code buffer, set by genMethod.
     */
    private Code code;

    /** Items structure, set by genMethod.
     */
    private Items items;

    /** Environment for symbol lookup, set by genClass
     */
    private Env<AttrContext> attrEnv;

    /** The top level tree.
     */
    private JCCompilationUnit toplevel;

    /** The number of code-gen errors in this class.
     */
    private int nerrs = 0;

    /** A hash table mapping syntax trees to their ending source positions.
     */
    private Map<JCTree, Integer> endPositions;

    /** Generate code to load an integer constant.
     *  @param n     The integer to be loaded.
     */
    void loadIntConst(int n) {
        items.makeImmediateItem(syms.intType, n).load();
    }

    /** The opcode that loads a zero constant of a given type code.
     *  @param tc   The given type code (@see ByteCode).
     */
    public static int zero(int tc) {
        switch(tc) {
            case INTcode:
            case BYTEcode:
            case SHORTcode:
            case CHARcode:
                return iconst_0;
            case LONGcode:
                return lconst_0;
            case FLOATcode:
                return fconst_0;
            case DOUBLEcode:
                return dconst_0;
            default:
                throw new AssertionError("zero");
        }
    }

    /** The opcode that loads a one constant of a given type code.
     *  @param tc   The given type code (@see ByteCode).
     */
    public static int one(int tc) {
        return zero(tc) + 1;
    }

    /** Generate code to load -1 of the given type code (either int or long).
     *  @param tc   The given type code (@see ByteCode).
     */
    void emitMinusOne(int tc) {
        if (tc == LONGcode) {
            items.makeImmediateItem(syms.longType, new Long(-1)).load();
        } else {
            code.emitop0(iconst_m1);
        }
    }

    /** Construct a symbol to reflect the qualifying type that should
     *  appear in the byte code as per JLS 13.1.
     *
     *  For target >= 1.2: Clone a method with the qualifier as owner (except
     *  for those cases where we need to work around VM bugs).
     *
     *  For target <= 1.1: If qualified variable or method is defined in a
     *  non-accessible class, clone it with the qualifier class as owner.
     *
     *  @param sym    The accessed symbol
     *  @param site   The qualifier's type.
     */
    Symbol binaryQualifier(Symbol sym, Type site) {
        if (site.tag == ARRAY) {
            if (sym == syms.lengthVar || sym.owner != syms.arrayClass) return sym;
            Symbol qualifier = target.arrayBinaryCompatibility() ? new ClassSymbol(Flags.PUBLIC, site.tsym.name, site, syms.noSymbol) : syms.objectType.tsym;
            return sym.clone(qualifier);
        }
        if (sym.owner == site.tsym || (sym.flags() & (STATIC | SYNTHETIC)) == (STATIC | SYNTHETIC)) {
            return sym;
        }
        if (!target.obeyBinaryCompatibility()) return rs.isAccessible(attrEnv, (TypeSymbol) sym.owner) ? sym : sym.clone(site.tsym);
        if (!target.interfaceFieldsBinaryCompatibility()) {
            if ((sym.owner.flags() & INTERFACE) != 0 && sym.kind == VAR) return sym;
        }
        if (sym.owner == syms.objectType.tsym) return sym;
        if (!target.interfaceObjectOverridesBinaryCompatibility()) {
            if ((sym.owner.flags() & INTERFACE) != 0 && syms.objectType.tsym.members().lookup(sym.name).scope != null) return sym;
        }
        return sym.clone(site.tsym);
    }

    /** Insert a reference to given type in the constant pool,
     *  checking for an array with too many dimensions;
     *  return the reference's index.
     *  @param type   The type for which a reference is inserted.
     */
    int makeRef(DiagnosticPosition pos, Type type) {
        checkDimension(pos, type);
        return pool.put(type.tag == CLASS ? (Object) type.tsym : (Object) type);
    }

    /** Check if the given type is an array with too many dimensions.
     */
    private void checkDimension(DiagnosticPosition pos, Type t) {
        switch(t.tag) {
            case METHOD:
                checkDimension(pos, t.getReturnType());
                for (List<Type> args = t.getParameterTypes(); args.nonEmpty(); args = args.tail) checkDimension(pos, args.head);
                break;
            case ARRAY:
                if (types.dimensions(t) > ClassFile.MAX_DIMENSIONS) {
                    log.error(pos, "limit.dimensions");
                    nerrs++;
                }
                break;
            default:
                break;
        }
    }

    /** Create a tempory variable.
     *  @param type   The variable's type.
     */
    LocalItem makeTemp(Type type) {
        VarSymbol v = new VarSymbol(Flags.SYNTHETIC, names.empty, type, env.enclMethod.sym);
        code.newLocal(v);
        return items.makeLocalItem(v);
    }

    /** Generate code to call a non-private method or constructor.
     *  @param pos         Position to be used for error reporting.
     *  @param site        The type of which the method is a member.
     *  @param name        The method's name.
     *  @param argtypes    The method's argument types.
     *  @param isStatic    A flag that indicates whether we call a
     *                     static or instance method.
     */
    void callMethod(DiagnosticPosition pos, Type site, Name name, List<Type> argtypes, boolean isStatic) {
        Symbol msym = rs.resolveInternalMethod(pos, attrEnv, site, name, argtypes, null);
        if (isStatic) items.makeStaticItem(msym).invoke(); else items.makeMemberItem(msym, name == names.init).invoke();
    }

    /** Is the given method definition an access method
     *  resulting from a qualified super? This is signified by an odd
     *  access code.
     */
    private boolean isAccessSuper(JCMethodDecl enclMethod) {
        return (enclMethod.mods.flags & SYNTHETIC) != 0 && isOddAccessName(enclMethod.name);
    }

    /** Does given name start with "access$" and end in an odd digit?
     */
    private boolean isOddAccessName(Name name) {
        return name.startsWith(accessDollar) && (name.byteAt(name.len - 1) & 1) == 1;
    }

    /** Generate code to invoke the finalizer associated with given
     *  environment.
     *  Any calls to finalizers are appended to the environments `cont' chain.
     *  Mark beginning of gap in catch all range for finalizer.
     */
    void genFinalizer(Env<GenContext> env) {
        if (code.isAlive() && env.info.finalize != null) env.info.finalize.gen();
    }

    /** Generate code to call all finalizers of structures aborted by
     *  a non-local
     *  exit.  Return target environment of the non-local exit.
     *  @param target      The tree representing the structure that's aborted
     *  @param env         The environment current at the non-local exit.
     */
    Env<GenContext> unwind(JCTree target, Env<GenContext> env) {
        Env<GenContext> env1 = env;
        while (true) {
            genFinalizer(env1);
            if (env1.tree == target) break;
            env1 = env1.next;
        }
        return env1;
    }

    /** Mark end of gap in catch-all range for finalizer.
     *  @param env   the environment which might contain the finalizer
     *               (if it does, env.info.gaps != null).
     */
    void endFinalizerGap(Env<GenContext> env) {
        if (env.info.gaps != null && env.info.gaps.length() % 2 == 1) env.info.gaps.append(code.curPc());
    }

    /** Mark end of all gaps in catch-all ranges for finalizers of environments
     *  lying between, and including to two environments.
     *  @param from    the most deeply nested environment to mark
     *  @param to      the least deeply nested environment to mark
     */
    void endFinalizerGaps(Env<GenContext> from, Env<GenContext> to) {
        Env<GenContext> last = null;
        while (last != to) {
            endFinalizerGap(from);
            last = from;
            from = from.next;
        }
    }

    /** Do any of the structures aborted by a non-local exit have
     *  finalizers that require an empty stack?
     *  @param target      The tree representing the structure that's aborted
     *  @param env         The environment current at the non-local exit.
     */
    boolean hasFinally(JCTree target, Env<GenContext> env) {
        while (env.tree != target) {
            if (env.tree.getTag() == JCTree.TRY && env.info.finalize.hasFinalizer()) return true;
            env = env.next;
        }
        return false;
    }

    /** Distribute member initializer code into constructors and <clinit>
     *  method.
     *  @param defs         The list of class member declarations.
     *  @param c            The enclosing class.
     */
    List<JCTree> normalizeDefs(List<JCTree> defs, ClassSymbol c) {
        ListBuffer<JCStatement> initCode = new ListBuffer<JCStatement>();
        ListBuffer<JCStatement> clinitCode = new ListBuffer<JCStatement>();
        ListBuffer<JCTree> methodDefs = new ListBuffer<JCTree>();
        for (List<JCTree> l = defs; l.nonEmpty(); l = l.tail) {
            JCTree def = l.head;
            switch(def.getTag()) {
                case JCTree.BLOCK:
                    JCBlock block = (JCBlock) def;
                    if ((block.flags & STATIC) != 0) clinitCode.append(block); else initCode.append(block);
                    break;
                case JCTree.METHODDEF:
                    methodDefs.append(def);
                    break;
                case JCTree.VARDEF:
                    JCVariableDecl vdef = (JCVariableDecl) def;
                    VarSymbol sym = vdef.sym;
                    checkDimension(vdef.pos(), sym.type);
                    if (vdef.init != null) {
                        if ((sym.flags() & STATIC) == 0) {
                            JCStatement init = make.at(vdef.pos()).Assignment(sym, vdef.init);
                            initCode.append(init);
                            if (endPositions != null) {
                                Integer endPos = endPositions.remove(vdef);
                                if (endPos != null) endPositions.put(init, endPos);
                            }
                        } else if (sym.getConstValue() == null) {
                            JCStatement init = make.at(vdef.pos).Assignment(sym, vdef.init);
                            clinitCode.append(init);
                            if (endPositions != null) {
                                Integer endPos = endPositions.remove(vdef);
                                if (endPos != null) endPositions.put(init, endPos);
                            }
                        } else {
                            checkStringConstant(vdef.init.pos(), sym.getConstValue());
                        }
                    }
                    break;
                default:
                    assert false;
            }
        }
        if (initCode.length() != 0) {
            List<JCStatement> inits = initCode.toList();
            for (JCTree t : methodDefs) {
                normalizeMethod((JCMethodDecl) t, inits);
            }
        }
        if (clinitCode.length() != 0) {
            MethodSymbol clinit = new MethodSymbol(STATIC, names.clinit, new MethodType(List.<Type>nil(), syms.voidType, List.<Type>nil(), syms.methodClass), c);
            c.members().enter(clinit);
            List<JCStatement> clinitStats = clinitCode.toList();
            JCBlock block = make.at(clinitStats.head.pos()).Block(0, clinitStats);
            block.endpos = TreeInfo.endPos(clinitStats.last());
            methodDefs.append(make.MethodDef(clinit, block));
        }
        return methodDefs.toList();
    }

    /** Check a constant value and report if it is a string that is
     *  too large.
     */
    private void checkStringConstant(DiagnosticPosition pos, Object constValue) {
        if (nerrs != 0 || constValue == null || !(constValue instanceof String) || ((String) constValue).length() < Pool.MAX_STRING_LENGTH) return;
        log.error(pos, "limit.string");
        nerrs++;
    }

    /** Insert instance initializer code into initial constructor.
     *  @param md        The tree potentially representing a
     *                   constructor's definition.
     *  @param initCode  The list of instance initializer statements.
     */
    void normalizeMethod(JCMethodDecl md, List<JCStatement> initCode) {
        if (md.name == names.init && TreeInfo.isInitialConstructor(md)) {
            List<JCStatement> stats = md.body.stats;
            ListBuffer<JCStatement> newstats = new ListBuffer<JCStatement>();
            if (stats.nonEmpty()) {
                while (TreeInfo.isSyntheticInit(stats.head)) {
                    newstats.append(stats.head);
                    stats = stats.tail;
                }
                newstats.append(stats.head);
                stats = stats.tail;
                while (stats.nonEmpty() && TreeInfo.isSyntheticInit(stats.head)) {
                    newstats.append(stats.head);
                    stats = stats.tail;
                }
                newstats.appendList(initCode);
                while (stats.nonEmpty()) {
                    newstats.append(stats.head);
                    stats = stats.tail;
                }
            }
            md.body.stats = newstats.toList();
            if (md.body.endpos == Position.NOPOS) md.body.endpos = TreeInfo.endPos(md.body.stats.last());
        }
    }

    /** Add abstract methods for all methods defined in one of
     *  the interfaces of a given class,
     *  provided they are not already implemented in the class.
     *
     *  @param c      The class whose interfaces are searched for methods
     *                for which Miranda methods should be added.
     */
    void implementInterfaceMethods(ClassSymbol c) {
        implementInterfaceMethods(c, c);
    }

    /** Add abstract methods for all methods defined in one of
     *  the interfaces of a given class,
     *  provided they are not already implemented in the class.
     *
     *  @param c      The class whose interfaces are searched for methods
     *                for which Miranda methods should be added.
     *  @param site   The class in which a definition may be needed.
     */
    void implementInterfaceMethods(ClassSymbol c, ClassSymbol site) {
        for (List<Type> l = types.interfaces(c.type); l.nonEmpty(); l = l.tail) {
            ClassSymbol i = (ClassSymbol) l.head.tsym;
            for (Scope.Entry e = i.members().elems; e != null; e = e.sibling) {
                if (e.sym.kind == MTH && (e.sym.flags() & STATIC) == 0) {
                    MethodSymbol absMeth = (MethodSymbol) e.sym;
                    MethodSymbol implMeth = absMeth.binaryImplementation(site, types);
                    if (implMeth == null) addAbstractMethod(site, absMeth); else if ((implMeth.flags() & IPROXY) != 0) adjustAbstractMethod(site, implMeth, absMeth);
                }
            }
            implementInterfaceMethods(i, site);
        }
    }

    /** Add an abstract methods to a class
     *  which implicitly implements a method defined in some interface
     *  implemented by the class. These methods are called "Miranda methods".
     *  Enter the newly created method into its enclosing class scope.
     *  Note that it is not entered into the class tree, as the emitter
     *  doesn't need to see it there to emit an abstract method.
     *
     *  @param c      The class to which the Miranda method is added.
     *  @param m      The interface method symbol for which a Miranda method
     *                is added.
     */
    private void addAbstractMethod(ClassSymbol c, MethodSymbol m) {
        MethodSymbol absMeth = new MethodSymbol(m.flags() | IPROXY | SYNTHETIC, m.name, m.type, c);
        c.members().enter(absMeth);
    }

    private void adjustAbstractMethod(ClassSymbol c, MethodSymbol pm, MethodSymbol im) {
        MethodType pmt = (MethodType) pm.type;
        Type imt = types.memberType(c.type, im);
        pmt.thrown = chk.intersect(pmt.getThrownTypes(), imt.getThrownTypes());
    }

    /** Visitor argument: The current environment.
     */
    Env<GenContext> env;

    /** Visitor argument: The expected type (prototype).
     */
    Type pt;

    /** Visitor result: The item representing the computed value.
     */
    Item result;

    /** Visitor method: generate code for a definition, catching and reporting
     *  any completion failures.
     *  @param tree    The definition to be visited.
     *  @param env     The environment current at the definition.
     */
    public void genDef(JCTree tree, Env<GenContext> env) {
        Env<GenContext> prevEnv = this.env;
        try {
            this.env = env;
            tree.accept(this);
        } catch (CompletionFailure ex) {
            chk.completionError(tree.pos(), ex);
        } finally {
            this.env = prevEnv;
        }
    }

    /** Derived visitor method: check whether CharacterRangeTable
     *  should be emitted, if so, put a new entry into CRTable
     *  and call method to generate bytecode.
     *  If not, just call method to generate bytecode.
     *  @see    #genStat(Tree, Env)
     *
     *  @param  tree     The tree to be visited.
     *  @param  env      The environment to use.
     *  @param  crtFlags The CharacterRangeTable flags
     *                   indicating type of the entry.
     */
    public void genStat(JCTree tree, Env<GenContext> env, int crtFlags) {
        if (!genCrt) {
            genStat(tree, env);
            return;
        }
        int startpc = code.curPc();
        genStat(tree, env);
        if (tree.getTag() == JCTree.BLOCK) crtFlags |= CRT_BLOCK;
        code.crt.put(tree, crtFlags, startpc, code.curPc());
    }

    /** Derived visitor method: generate code for a statement.
     */
    public void genStat(JCTree tree, Env<GenContext> env) {
        if (code.isAlive()) {
            code.statBegin(tree.pos);
            genDef(tree, env);
        } else if (env.info.isSwitch && tree.getTag() == JCTree.VARDEF) {
            code.newLocal(((JCVariableDecl) tree).sym);
        }
    }

    /** Derived visitor method: check whether CharacterRangeTable
     *  should be emitted, if so, put a new entry into CRTable
     *  and call method to generate bytecode.
     *  If not, just call method to generate bytecode.
     *  @see    #genStats(List, Env)
     *
     *  @param  trees    The list of trees to be visited.
     *  @param  env      The environment to use.
     *  @param  crtFlags The CharacterRangeTable flags
     *                   indicating type of the entry.
     */
    public void genStats(List<JCStatement> trees, Env<GenContext> env, int crtFlags) {
        if (!genCrt) {
            genStats(trees, env);
            return;
        }
        if (trees.length() == 1) {
            genStat(trees.head, env, crtFlags | CRT_STATEMENT);
        } else {
            int startpc = code.curPc();
            genStats(trees, env);
            code.crt.put(trees, crtFlags, startpc, code.curPc());
        }
    }

    /** Derived visitor method: generate code for a list of statements.
     */
    public void genStats(List<? extends JCTree> trees, Env<GenContext> env) {
        for (List<? extends JCTree> l = trees; l.nonEmpty(); l = l.tail) genStat(l.head, env, CRT_STATEMENT);
    }

    /** Derived visitor method: check whether CharacterRangeTable
     *  should be emitted, if so, put a new entry into CRTable
     *  and call method to generate bytecode.
     *  If not, just call method to generate bytecode.
     *  @see    #genCond(Tree,boolean)
     *
     *  @param  tree     The tree to be visited.
     *  @param  crtFlags The CharacterRangeTable flags
     *                   indicating type of the entry.
     */
    public CondItem genCond(JCTree tree, int crtFlags) {
        if (!genCrt) return genCond(tree, false);
        int startpc = code.curPc();
        CondItem item = genCond(tree, (crtFlags & CRT_FLOW_CONTROLLER) != 0);
        code.crt.put(tree, crtFlags, startpc, code.curPc());
        return item;
    }

    /** Derived visitor method: generate code for a boolean
     *  expression in a control-flow context.
     *  @param _tree         The expression to be visited.
     *  @param markBranches The flag to indicate that the condition is
     *                      a flow controller so produced conditions
     *                      should contain a proper tree to generate
     *                      CharacterRangeTable branches for them.
     */
    public CondItem genCond(JCTree _tree, boolean markBranches) {
        JCTree inner_tree = TreeInfo.skipParens(_tree);
        if (inner_tree.getTag() == JCTree.CONDEXPR) {
            JCConditional tree = (JCConditional) inner_tree;
            CondItem cond = genCond(tree.cond, CRT_FLOW_CONTROLLER);
            if (cond.isTrue()) {
                code.resolve(cond.trueJumps);
                CondItem result = genCond(tree.truepart, CRT_FLOW_TARGET);
                if (markBranches) result.tree = tree.truepart;
                return result;
            }
            if (cond.isFalse()) {
                code.resolve(cond.falseJumps);
                CondItem result = genCond(tree.falsepart, CRT_FLOW_TARGET);
                if (markBranches) result.tree = tree.falsepart;
                return result;
            }
            Chain secondJumps = cond.jumpFalse();
            code.resolve(cond.trueJumps);
            CondItem first = genCond(tree.truepart, CRT_FLOW_TARGET);
            if (markBranches) first.tree = tree.truepart;
            Chain falseJumps = first.jumpFalse();
            code.resolve(first.trueJumps);
            Chain trueJumps = code.branch(goto_);
            code.resolve(secondJumps);
            CondItem second = genCond(tree.falsepart, CRT_FLOW_TARGET);
            CondItem result = items.makeCondItem(second.opcode, code.mergeChains(trueJumps, second.trueJumps), code.mergeChains(falseJumps, second.falseJumps));
            if (markBranches) result.tree = tree.falsepart;
            return result;
        } else {
            CondItem result = genExpr(_tree, syms.booleanType).mkCond();
            if (markBranches) result.tree = _tree;
            return result;
        }
    }

    /** Visitor method: generate code for an expression, catching and reporting
     *  any completion failures.
     *  @param tree    The expression to be visited.
     *  @param pt      The expression's expected type (proto-type).
     */
    public Item genExpr(JCTree tree, Type pt) {
        Type prevPt = this.pt;
        try {
            if (tree.type.constValue() != null) {
                checkStringConstant(tree.pos(), tree.type.constValue());
                result = items.makeImmediateItem(tree.type, tree.type.constValue());
            } else {
                this.pt = pt;
                tree.accept(this);
            }
            return result.coerce(pt);
        } catch (CompletionFailure ex) {
            chk.completionError(tree.pos(), ex);
            code.state.stacksize = 1;
            return items.makeStackItem(pt);
        } finally {
            this.pt = prevPt;
        }
    }

    /** Derived visitor method: generate code for a list of method arguments.
     *  @param trees    The argument expressions to be visited.
     *  @param pts      The expression's expected types (i.e. the formal parameter
     *                  types of the invoked method).
     */
    public void genArgs(List<JCExpression> trees, List<Type> pts) {
        for (List<JCExpression> l = trees; l.nonEmpty(); l = l.tail) {
            genExpr(l.head, pts.head).load();
            pts = pts.tail;
        }
        assert pts.isEmpty();
    }

    /** Thrown when the byte code size exceeds limit.
     */
    public static class CodeSizeOverflow extends RuntimeException {

        private static final long serialVersionUID = 0;

        public CodeSizeOverflow() {
        }
    }

    public void visitMethodDef(JCMethodDecl tree) {
        Env<GenContext> localEnv = env.dup(tree);
        localEnv.enclMethod = tree;
        this.pt = tree.sym.erasure(types).getReturnType();
        checkDimension(tree.pos(), tree.sym.erasure(types));
        genMethod(tree, localEnv, false);
    }

    /** Generate code for a method.
	 *  @param tree     The tree representing the method definition.
	 *  @param env      The environment current for the method body.
	 *  @param fatcode  A flag that indicates whether all jumps are
	 *		    within 32K.  We first invoke this method under
	 *		    the assumption that fatcode == false, i.e. all
	 *		    jumps are within 32K.  If this fails, fatcode
	 *		    is set to true and we try again.
	 */
    void genMethod(JCMethodDecl tree, Env<GenContext> env, boolean fatcode) {
        MethodSymbol meth = tree.sym;
        if (Code.width(types.erasure(env.enclMethod.sym.type).getParameterTypes()) + (((tree.mods.flags & STATIC) == 0 || meth.isConstructor()) ? 1 : 0) > ClassFile.MAX_PARAMETERS) {
            log.error(tree.pos(), "limit.parameters");
            nerrs++;
        } else if (tree.body != null) {
            int startpcCrt = initCode(tree, env, fatcode);
            try {
                genStat(tree.body, env);
            } catch (CodeSizeOverflow e) {
                startpcCrt = initCode(tree, env, fatcode);
                genStat(tree.body, env);
            }
            if (code.state.stacksize != 0) {
                log.error(tree.body.pos(), "stack.sim.error", tree);
                throw new AssertionError();
            }
            if (code.isAlive()) {
                code.statBegin(TreeInfo.endPos(tree.body));
                if (env.enclMethod == null || env.enclMethod.sym.type.getReturnType().tag == VOID) {
                    code.emitop0(return_);
                } else {
                    int startpc = code.entryPoint();
                    CondItem c = items.makeCondItem(goto_);
                    code.resolve(c.jumpTrue(), startpc);
                }
            }
            if (genCrt) code.crt.put(tree.body, CRT_BLOCK, startpcCrt, code.curPc());
            code.endScopes(0);
            if (code.checkLimits(tree.pos(), log)) {
                nerrs++;
                return;
            }
            if (!fatcode && code.fatcode) genMethod(tree, env, true);
            if (stackMap == StackMapFormat.JSR202) {
                code.lastFrame = null;
                code.frameBeforeLast = null;
            }
        }
    }

    private int initCode(JCMethodDecl tree, Env<GenContext> env, boolean fatcode) {
        MethodSymbol meth = tree.sym;
        meth.code = code = new Code(meth, fatcode, lineDebugInfo ? toplevel.lineMap : null, varDebugInfo, stackMap, debugCode, genCrt ? new CRTable(tree, env.toplevel.endPositions) : null, syms, types, pool);
        items = new Items(pool, code, syms, types);
        if (code.debugCode) System.err.println(meth + " for body " + tree);
        if ((tree.mods.flags & STATIC) == 0) {
            Type selfType = meth.owner.type;
            if (meth.isConstructor() && selfType != syms.objectType) selfType = UninitializedType.uninitializedThis(selfType);
            code.setDefined(code.newLocal(new VarSymbol(FINAL, names._this, selfType, meth.owner)));
        }
        for (List<JCVariableDecl> l = tree.params; l.nonEmpty(); l = l.tail) {
            checkDimension(l.head.pos(), l.head.sym.type);
            code.setDefined(code.newLocal(l.head.sym));
        }
        int startpcCrt = genCrt ? code.curPc() : 0;
        code.entryPoint();
        code.pendingStackMap = false;
        return startpcCrt;
    }

    public void visitVarDef(JCVariableDecl tree) {
        VarSymbol v = tree.sym;
        code.newLocal(v);
        if (tree.init != null) {
            checkStringConstant(tree.init.pos(), v.getConstValue());
            if (v.getConstValue() == null || varDebugInfo) {
                genExpr(tree.init, v.erasure(types)).load();
                items.makeLocalItem(v).store();
            }
        }
        checkDimension(tree.pos(), v.type);
    }

    public void visitSkip(JCSkip tree) {
    }

    public void visitBlock(JCBlock tree) {
        int limit = code.nextreg;
        Env<GenContext> localEnv = env.dup(tree, new GenContext());
        genStats(tree.stats, localEnv);
        if (env.tree.getTag() != JCTree.METHODDEF) {
            code.statBegin(tree.endpos);
            code.endScopes(limit);
            code.pendingStatPos = Position.NOPOS;
        }
    }

    public void visitDoLoop(JCDoWhileLoop tree) {
        genLoop(tree, tree.body, tree.cond, List.<JCExpressionStatement>nil(), false);
    }

    public void visitWhileLoop(JCWhileLoop tree) {
        genLoop(tree, tree.body, tree.cond, List.<JCExpressionStatement>nil(), true);
    }

    public void visitForLoop(JCForLoop tree) {
        int limit = code.nextreg;
        genStats(tree.init, env);
        genLoop(tree, tree.body, tree.cond, tree.step, true);
        code.endScopes(limit);
    }

    /** Generate code for a loop.
	 *  @param loop       The tree representing the loop.
	 *  @param body       The loop's body.
	 *  @param cond       The loop's controling condition.
	 *  @param step       "Step" statements to be inserted at end of
	 *                    each iteration.
	 *  @param testFirst  True if the loop test belongs before the body.
	 */
    private void genLoop(JCStatement loop, JCStatement body, JCExpression cond, List<JCExpressionStatement> step, boolean testFirst) {
        Env<GenContext> loopEnv = env.dup(loop, new GenContext());
        int startpc = code.entryPoint();
        if (testFirst) {
            CondItem c;
            if (cond != null) {
                code.statBegin(cond.pos);
                c = genCond(TreeInfo.skipParens(cond), CRT_FLOW_CONTROLLER);
            } else {
                c = items.makeCondItem(goto_);
            }
            Chain loopDone = c.jumpFalse();
            code.resolve(c.trueJumps);
            genStat(body, loopEnv, CRT_STATEMENT | CRT_FLOW_TARGET);
            code.resolve(loopEnv.info.cont);
            genStats(step, loopEnv);
            code.resolve(code.branch(goto_), startpc);
            code.resolve(loopDone);
        } else {
            genStat(body, loopEnv, CRT_STATEMENT | CRT_FLOW_TARGET);
            code.resolve(loopEnv.info.cont);
            genStats(step, loopEnv);
            CondItem c;
            if (cond != null) {
                code.statBegin(cond.pos);
                c = genCond(TreeInfo.skipParens(cond), CRT_FLOW_CONTROLLER);
            } else {
                c = items.makeCondItem(goto_);
            }
            code.resolve(c.jumpTrue(), startpc);
            code.resolve(c.falseJumps);
        }
        code.resolve(loopEnv.info.exit);
    }

    public void visitForeachLoop(JCEnhancedForLoop tree) {
        throw new AssertionError();
    }

    public void visitLabelled(JCLabeledStatement tree) {
        Env<GenContext> localEnv = env.dup(tree, new GenContext());
        genStat(tree.body, localEnv, CRT_STATEMENT);
        code.resolve(localEnv.info.exit);
    }

    public void visitSwitch(JCSwitch tree) {
        int limit = code.nextreg;
        assert tree.selector.type.tag != CLASS;
        int startpcCrt = genCrt ? code.curPc() : 0;
        Item sel = genExpr(tree.selector, syms.intType);
        List<JCCase> cases = tree.cases;
        if (cases.isEmpty()) {
            sel.load().drop();
            if (genCrt) code.crt.put(TreeInfo.skipParens(tree.selector), CRT_FLOW_CONTROLLER, startpcCrt, code.curPc());
        } else {
            sel.load();
            if (genCrt) code.crt.put(TreeInfo.skipParens(tree.selector), CRT_FLOW_CONTROLLER, startpcCrt, code.curPc());
            Env<GenContext> switchEnv = env.dup(tree, new GenContext());
            switchEnv.info.isSwitch = true;
            int lo = Integer.MAX_VALUE;
            int hi = Integer.MIN_VALUE;
            int nlabels = 0;
            int[] labels = new int[cases.length()];
            int defaultIndex = -1;
            List<JCCase> l = cases;
            for (int i = 0; i < labels.length; i++) {
                if (l.head.pat != null) {
                    int val = ((Number) l.head.pat.type.constValue()).intValue();
                    labels[i] = val;
                    if (val < lo) lo = val;
                    if (hi < val) hi = val;
                    nlabels++;
                } else {
                    assert defaultIndex == -1;
                    defaultIndex = i;
                }
                l = l.tail;
            }
            long table_space_cost = 4 + ((long) hi - lo + 1);
            long table_time_cost = 3;
            long lookup_space_cost = 3 + 2 * (long) nlabels;
            long lookup_time_cost = nlabels;
            int opcode = nlabels > 0 && table_space_cost + 3 * table_time_cost <= lookup_space_cost + 3 * lookup_time_cost ? tableswitch : lookupswitch;
            int startpc = code.curPc();
            code.emitop0(opcode);
            code.align(4);
            int tableBase = code.curPc();
            int[] offsets = null;
            code.emit4(-1);
            if (opcode == tableswitch) {
                code.emit4(lo);
                code.emit4(hi);
                for (long i = lo; i <= hi; i++) {
                    code.emit4(-1);
                }
            } else {
                code.emit4(nlabels);
                for (int i = 0; i < nlabels; i++) {
                    code.emit4(-1);
                    code.emit4(-1);
                }
                offsets = new int[labels.length];
            }
            Code.State stateSwitch = code.state.dup();
            code.markDead();
            l = cases;
            for (int i = 0; i < labels.length; i++) {
                JCCase c = l.head;
                l = l.tail;
                int pc = code.entryPoint(stateSwitch);
                if (i != defaultIndex) {
                    if (opcode == tableswitch) {
                        code.put4(tableBase + 4 * (labels[i] - lo + 3), pc - startpc);
                    } else {
                        offsets[i] = pc - startpc;
                    }
                } else {
                    code.put4(tableBase, pc - startpc);
                }
                genStats(c.stats, switchEnv, CRT_FLOW_TARGET);
            }
            code.resolve(switchEnv.info.exit);
            if (code.get4(tableBase) == -1) {
                code.put4(tableBase, code.entryPoint(stateSwitch) - startpc);
            }
            if (opcode == tableswitch) {
                int defaultOffset = code.get4(tableBase);
                for (long i = lo; i <= hi; i++) {
                    int t = (int) (tableBase + 4 * (i - lo + 3));
                    if (code.get4(t) == -1) code.put4(t, defaultOffset);
                }
            } else {
                if (defaultIndex >= 0) for (int i = defaultIndex; i < labels.length - 1; i++) {
                    labels[i] = labels[i + 1];
                    offsets[i] = offsets[i + 1];
                }
                if (nlabels > 0) qsort2(labels, offsets, 0, nlabels - 1);
                for (int i = 0; i < nlabels; i++) {
                    int caseidx = tableBase + 8 * (i + 1);
                    code.put4(caseidx, labels[i]);
                    code.put4(caseidx + 4, offsets[i]);
                }
            }
        }
        code.endScopes(limit);
    }

    /** Sort (int) arrays of keys and values
	 */
    static void qsort2(int[] keys, int[] values, int lo, int hi) {
        int i = lo;
        int j = hi;
        int pivot = keys[(i + j) / 2];
        do {
            while (keys[i] < pivot) i++;
            while (pivot < keys[j]) j--;
            if (i <= j) {
                int temp1 = keys[i];
                keys[i] = keys[j];
                keys[j] = temp1;
                int temp2 = values[i];
                values[i] = values[j];
                values[j] = temp2;
                i++;
                j--;
            }
        } while (i <= j);
        if (lo < j) qsort2(keys, values, lo, j);
        if (i < hi) qsort2(keys, values, i, hi);
    }

    public void visitSynchronized(JCSynchronized tree) {
        int limit = code.nextreg;
        final LocalItem lockVar = makeTemp(syms.objectType);
        genExpr(tree.lock, tree.lock.type).load().duplicate();
        lockVar.store();
        code.emitop0(monitorenter);
        code.state.lock(lockVar.reg);
        final Env<GenContext> syncEnv = env.dup(tree, new GenContext());
        syncEnv.info.finalize = new GenFinalizer() {

            void gen() {
                genLast();
                assert syncEnv.info.gaps.length() % 2 == 0;
                syncEnv.info.gaps.append(code.curPc());
            }

            void genLast() {
                if (code.isAlive()) {
                    lockVar.load();
                    code.emitop0(monitorexit);
                    code.state.unlock(lockVar.reg);
                }
            }
        };
        syncEnv.info.gaps = new ListBuffer<Integer>();
        genTry(tree.body, List.<JCCatch>nil(), syncEnv);
        code.endScopes(limit);
    }

    public void visitTry(final JCTry tree) {
        final Env<GenContext> tryEnv = env.dup(tree, new GenContext());
        final Env<GenContext> oldEnv = env;
        if (!useJsrLocally) {
            useJsrLocally = (stackMap == StackMapFormat.NONE) && (jsrlimit <= 0 || jsrlimit < 100 && estimateCodeComplexity(tree.finalizer) > jsrlimit);
        }
        tryEnv.info.finalize = new GenFinalizer() {

            void gen() {
                if (useJsrLocally) {
                    if (tree.finalizer != null) {
                        Code.State jsrState = code.state.dup();
                        jsrState.push(code.jsrReturnValue);
                        tryEnv.info.cont = new Chain(code.emitJump(jsr), tryEnv.info.cont, jsrState);
                    }
                    assert tryEnv.info.gaps.length() % 2 == 0;
                    tryEnv.info.gaps.append(code.curPc());
                } else {
                    assert tryEnv.info.gaps.length() % 2 == 0;
                    tryEnv.info.gaps.append(code.curPc());
                    genLast();
                }
            }

            void genLast() {
                if (tree.finalizer != null) genStat(tree.finalizer, oldEnv, CRT_BLOCK);
            }

            boolean hasFinalizer() {
                return tree.finalizer != null;
            }
        };
        tryEnv.info.gaps = new ListBuffer<Integer>();
        genTry(tree.body, tree.catchers, tryEnv);
    }

    /** Generate code for a try or synchronized statement
	 *  @param body      The body of the try or synchronized statement.
	 *  @param catchers  The lis of catch clauses.
	 *  @param env       the environment current for the body.
	 */
    void genTry(JCTree body, List<JCCatch> catchers, Env<GenContext> env) {
        int limit = code.nextreg;
        int startpc = code.curPc();
        Code.State stateTry = code.state.dup();
        genStat(body, env, CRT_BLOCK);
        int endpc = code.curPc();
        boolean hasFinalizer = env.info.finalize != null && env.info.finalize.hasFinalizer();
        List<Integer> gaps = env.info.gaps.toList();
        code.statBegin(TreeInfo.endPos(body));
        genFinalizer(env);
        code.statBegin(TreeInfo.endPos(env.tree));
        Chain exitChain = code.branch(goto_);
        endFinalizerGap(env);
        if (startpc != endpc) for (List<JCCatch> l = catchers; l.nonEmpty(); l = l.tail) {
            code.entryPoint(stateTry, l.head.param.sym.type);
            genCatch(l.head, env, startpc, endpc, gaps);
            genFinalizer(env);
            if (hasFinalizer || l.tail.nonEmpty()) {
                code.statBegin(TreeInfo.endPos(env.tree));
                exitChain = code.mergeChains(exitChain, code.branch(goto_));
            }
            endFinalizerGap(env);
        }
        if (hasFinalizer) {
            code.newRegSegment();
            int catchallpc = code.entryPoint(stateTry, syms.throwableType);
            int startseg = startpc;
            while (env.info.gaps.nonEmpty()) {
                int endseg = env.info.gaps.next().intValue();
                registerCatch(body.pos(), startseg, endseg, catchallpc, 0);
                startseg = env.info.gaps.next().intValue();
            }
            code.statBegin(TreeInfo.finalizerPos(env.tree));
            code.markStatBegin();
            Item excVar = makeTemp(syms.throwableType);
            excVar.store();
            genFinalizer(env);
            excVar.load();
            registerCatch(body.pos(), startseg, env.info.gaps.next().intValue(), catchallpc, 0);
            code.emitop0(athrow);
            code.markDead();
            if (env.info.cont != null) {
                code.resolve(env.info.cont);
                code.statBegin(TreeInfo.finalizerPos(env.tree));
                code.markStatBegin();
                LocalItem retVar = makeTemp(syms.throwableType);
                retVar.store();
                env.info.finalize.genLast();
                code.emitop1w(ret, retVar.reg);
                code.markDead();
            }
        }
        code.resolve(exitChain);
        code.endScopes(limit);
    }

    /** Generate code for a catch clause.
	 *  @param tree     The catch clause.
	 *  @param env      The environment current in the enclosing try.
	 *  @param startpc  Start pc of try-block.
	 *  @param endpc    End pc of try-block.
	 */
    void genCatch(JCCatch tree, Env<GenContext> env, int startpc, int endpc, List<Integer> gaps) {
        if (startpc != endpc) {
            int catchType = makeRef(tree.pos(), tree.param.type);
            while (gaps.nonEmpty()) {
                int end = gaps.head.intValue();
                registerCatch(tree.pos(), startpc, end, code.curPc(), catchType);
                gaps = gaps.tail;
                startpc = gaps.head.intValue();
                gaps = gaps.tail;
            }
            if (startpc < endpc) registerCatch(tree.pos(), startpc, endpc, code.curPc(), catchType);
            VarSymbol exparam = tree.param.sym;
            code.statBegin(tree.pos);
            code.markStatBegin();
            int limit = code.nextreg;
            int exlocal = code.newLocal(exparam);
            items.makeLocalItem(exparam).store();
            code.statBegin(TreeInfo.firstStatPos(tree.body));
            genStat(tree.body, env, CRT_BLOCK);
            code.endScopes(limit);
            code.statBegin(TreeInfo.endPos(tree.body));
        }
    }

    /** Register a catch clause in the "Exceptions" code-attribute.
	 */
    void registerCatch(DiagnosticPosition pos, int startpc, int endpc, int handler_pc, int catch_type) {
        if (startpc != endpc) {
            char startpc1 = (char) startpc;
            char endpc1 = (char) endpc;
            char handler_pc1 = (char) handler_pc;
            if (startpc1 == startpc && endpc1 == endpc && handler_pc1 == handler_pc) {
                code.addCatch(startpc1, endpc1, handler_pc1, (char) catch_type);
            } else {
                if (!useJsrLocally && !target.generateStackMapTable()) {
                    useJsrLocally = true;
                    throw new CodeSizeOverflow();
                } else {
                    log.error(pos, "limit.code.too.large.for.try.stmt");
                    nerrs++;
                }
            }
        }
    }

    /** Very roughly estimate the number of instructions needed for
     *  the given tree.
     */
    int estimateCodeComplexity(JCTree tree) {
        if (tree == null) return 0;
        class ComplexityScanner extends TreeScanner {

            int complexity = 0;

            public void scan(JCTree tree) {
                if (complexity > jsrlimit) return;
                super.scan(tree);
            }

            public void visitClassDef(JCClassDecl tree) {
            }

            public void visitDoLoop(JCDoWhileLoop tree) {
                super.visitDoLoop(tree);
                complexity++;
            }

            public void visitWhileLoop(JCWhileLoop tree) {
                super.visitWhileLoop(tree);
                complexity++;
            }

            public void visitForLoop(JCForLoop tree) {
                super.visitForLoop(tree);
                complexity++;
            }

            public void visitSwitch(JCSwitch tree) {
                super.visitSwitch(tree);
                complexity += 5;
            }

            public void visitCase(JCCase tree) {
                super.visitCase(tree);
                complexity++;
            }

            public void visitSynchronized(JCSynchronized tree) {
                super.visitSynchronized(tree);
                complexity += 6;
            }

            public void visitTry(JCTry tree) {
                super.visitTry(tree);
                if (tree.finalizer != null) complexity += 6;
            }

            public void visitCatch(JCCatch tree) {
                super.visitCatch(tree);
                complexity += 2;
            }

            public void visitConditional(JCConditional tree) {
                super.visitConditional(tree);
                complexity += 2;
            }

            public void visitIf(JCIf tree) {
                super.visitIf(tree);
                complexity += 2;
            }

            public void visitBreak(JCBreak tree) {
                super.visitBreak(tree);
                complexity += 1;
            }

            public void visitContinue(JCContinue tree) {
                super.visitContinue(tree);
                complexity += 1;
            }

            public void visitReturn(JCReturn tree) {
                super.visitReturn(tree);
                complexity += 1;
            }

            public void visitThrow(JCThrow tree) {
                super.visitThrow(tree);
                complexity += 1;
            }

            public void visitAssert(JCAssert tree) {
                super.visitAssert(tree);
                complexity += 5;
            }

            public void visitApply(JCMethodInvocation tree) {
                super.visitApply(tree);
                complexity += 2;
            }

            public void visitNewClass(JCNewClass tree) {
                scan(tree.encl);
                scan(tree.args);
                complexity += 2;
            }

            public void visitNewArray(JCNewArray tree) {
                super.visitNewArray(tree);
                complexity += 5;
            }

            public void visitAssign(JCAssign tree) {
                super.visitAssign(tree);
                complexity += 1;
            }

            public void visitAssignop(JCAssignOp tree) {
                super.visitAssignop(tree);
                complexity += 2;
            }

            public void visitUnary(JCUnary tree) {
                complexity += 1;
                if (tree.type.constValue() == null) super.visitUnary(tree);
            }

            public void visitBinary(JCBinary tree) {
                complexity += 1;
                if (tree.type.constValue() == null) super.visitBinary(tree);
            }

            public void visitTypeTest(JCInstanceOf tree) {
                super.visitTypeTest(tree);
                complexity += 1;
            }

            public void visitIndexed(JCArrayAccess tree) {
                super.visitIndexed(tree);
                complexity += 1;
            }

            public void visitSelect(JCFieldAccess tree) {
                super.visitSelect(tree);
                if (tree.sym.kind == VAR) complexity += 1;
            }

            public void visitIdent(JCIdent tree) {
                if (tree.sym.kind == VAR) {
                    complexity += 1;
                    if (tree.type.constValue() == null && tree.sym.owner.kind == TYP) complexity += 1;
                }
            }

            public void visitLiteral(JCLiteral tree) {
                complexity += 1;
            }

            public void visitTree(JCTree tree) {
            }

            public void visitWildcard(JCWildcard tree) {
                throw new AssertionError(this.getClass().getName());
            }
        }
        ComplexityScanner scanner = new ComplexityScanner();
        tree.accept(scanner);
        return scanner.complexity;
    }

    public void visitIf(JCIf tree) {
        int limit = code.nextreg;
        Chain thenExit = null;
        CondItem c = genCond(TreeInfo.skipParens(tree.cond), CRT_FLOW_CONTROLLER);
        Chain elseChain = c.jumpFalse();
        if (!c.isFalse()) {
            code.resolve(c.trueJumps);
            genStat(tree.thenpart, env, CRT_STATEMENT | CRT_FLOW_TARGET);
            thenExit = code.branch(goto_);
        }
        if (elseChain != null) {
            code.resolve(elseChain);
            if (tree.elsepart != null) genStat(tree.elsepart, env, CRT_STATEMENT | CRT_FLOW_TARGET);
        }
        code.resolve(thenExit);
        code.endScopes(limit);
    }

    public void visitExec(JCExpressionStatement tree) {
        JCExpression e = tree.expr;
        switch(e.getTag()) {
            case JCTree.POSTINC:
                ((JCUnary) e).setTag(JCTree.PREINC);
                break;
            case JCTree.POSTDEC:
                ((JCUnary) e).setTag(JCTree.PREDEC);
                break;
        }
        genExpr(tree.expr, tree.expr.type).drop();
    }

    public void visitBreak(JCBreak tree) {
        Env<GenContext> targetEnv = unwind(tree.target, env);
        assert code.state.stacksize == 0;
        targetEnv.info.addExit(code.branch(goto_));
        endFinalizerGaps(env, targetEnv);
    }

    public void visitContinue(JCContinue tree) {
        Env<GenContext> targetEnv = unwind(tree.target, env);
        assert code.state.stacksize == 0;
        targetEnv.info.addCont(code.branch(goto_));
        endFinalizerGaps(env, targetEnv);
    }

    public void visitReturn(JCReturn tree) {
        int limit = code.nextreg;
        final Env<GenContext> targetEnv;
        if (tree.expr != null) {
            Item r = genExpr(tree.expr, pt).load();
            if (hasFinally(env.enclMethod, env)) {
                r = makeTemp(pt);
                r.store();
            }
            targetEnv = unwind(env.enclMethod, env);
            r.load();
            code.emitop0(ireturn + Code.truncate(Code.typecode(pt)));
        } else {
            targetEnv = unwind(env.enclMethod, env);
            code.emitop0(return_);
        }
        endFinalizerGaps(env, targetEnv);
        code.endScopes(limit);
    }

    public void visitThrow(JCThrow tree) {
        genExpr(tree.expr, tree.expr.type).load();
        code.emitop0(athrow);
    }

    public void visitApply(JCMethodInvocation tree) {
        Item m = genExpr(tree.meth, methodType);
        genArgs(tree.args, TreeInfo.symbol(tree.meth).externalType(types).getParameterTypes());
        result = m.invoke();
    }

    public void visitConditional(JCConditional tree) {
        Chain thenExit = null;
        CondItem c = genCond(tree.cond, CRT_FLOW_CONTROLLER);
        Chain elseChain = c.jumpFalse();
        if (!c.isFalse()) {
            code.resolve(c.trueJumps);
            int startpc = genCrt ? code.curPc() : 0;
            genExpr(tree.truepart, pt).load();
            code.state.forceStackTop(tree.type);
            if (genCrt) code.crt.put(tree.truepart, CRT_FLOW_TARGET, startpc, code.curPc());
            thenExit = code.branch(goto_);
        }
        if (elseChain != null) {
            code.resolve(elseChain);
            int startpc = genCrt ? code.curPc() : 0;
            genExpr(tree.falsepart, pt).load();
            code.state.forceStackTop(tree.type);
            if (genCrt) code.crt.put(tree.falsepart, CRT_FLOW_TARGET, startpc, code.curPc());
        }
        code.resolve(thenExit);
        result = items.makeStackItem(pt);
    }

    public void visitNewClass(JCNewClass tree) {
        assert tree.encl == null && tree.def == null;
        code.emitop2(new_, makeRef(tree.pos(), tree.type));
        code.emitop0(dup);
        genArgs(tree.args, tree.constructor.externalType(types).getParameterTypes());
        items.makeMemberItem(tree.constructor, true).invoke();
        result = items.makeStackItem(tree.type);
    }

    public void visitNewArray(JCNewArray tree) {
        if (tree.elems != null) {
            Type elemtype = types.elemtype(tree.type);
            loadIntConst(tree.elems.length());
            Item arr = makeNewArray(tree.pos(), tree.type, 1);
            int i = 0;
            for (List<JCExpression> l = tree.elems; l.nonEmpty(); l = l.tail) {
                arr.duplicate();
                loadIntConst(i);
                i++;
                genExpr(l.head, elemtype).load();
                items.makeIndexedItem(elemtype).store();
            }
            result = arr;
        } else {
            for (List<JCExpression> l = tree.dims; l.nonEmpty(); l = l.tail) {
                genExpr(l.head, syms.intType).load();
            }
            result = makeNewArray(tree.pos(), tree.type, tree.dims.length());
        }
    }

    /** Generate code to create an array with given element type and number
	 *  of dimensions.
	 */
    Item makeNewArray(DiagnosticPosition pos, Type type, int ndims) {
        Type elemtype = types.elemtype(type);
        if (types.dimensions(elemtype) + ndims > ClassFile.MAX_DIMENSIONS) {
            log.error(pos, "limit.dimensions");
            nerrs++;
        }
        int elemcode = Code.arraycode(elemtype);
        if (elemcode == 0 || (elemcode == 1 && ndims == 1)) {
            code.emitAnewarray(makeRef(pos, elemtype), type);
        } else if (elemcode == 1) {
            code.emitMultianewarray(ndims, makeRef(pos, type), type);
        } else {
            code.emitNewarray(elemcode, type);
        }
        return items.makeStackItem(type);
    }

    public void visitParens(JCParens tree) {
        result = genExpr(tree.expr, tree.expr.type);
    }

    public void visitAssign(JCAssign tree) {
        Item l = genExpr(tree.lhs, tree.lhs.type);
        genExpr(tree.rhs, tree.lhs.type).load();
        result = items.makeAssignItem(l);
    }

    public void visitAssignop(JCAssignOp tree) {
        OperatorSymbol operator = (OperatorSymbol) tree.operator;
        Item l;
        if (operator.opcode == string_add) {
            makeStringBuffer(tree.pos());
            l = genExpr(tree.lhs, tree.lhs.type);
            if (l.width() > 0) {
                code.emitop0(dup_x1 + 3 * (l.width() - 1));
            }
            l.load();
            appendString(tree.lhs);
            appendStrings(tree.rhs);
            bufferToString(tree.pos());
        } else {
            l = genExpr(tree.lhs, tree.lhs.type);
            if ((tree.getTag() == JCTree.PLUS_ASG || tree.getTag() == JCTree.MINUS_ASG) && l instanceof LocalItem && tree.lhs.type.tag <= INT && tree.rhs.type.tag <= INT && tree.rhs.type.constValue() != null) {
                int ival = ((Number) tree.rhs.type.constValue()).intValue();
                if (tree.getTag() == JCTree.MINUS_ASG) ival = -ival;
                ((LocalItem) l).incr(ival);
                result = l;
                return;
            }
            l.duplicate();
            l.coerce(operator.type.getParameterTypes().head).load();
            completeBinop(tree.lhs, tree.rhs, operator).coerce(tree.lhs.type);
        }
        result = items.makeAssignItem(l);
    }

    public void visitUnary(JCUnary tree) {
        OperatorSymbol operator = (OperatorSymbol) tree.operator;
        if (tree.getTag() == JCTree.NOT) {
            CondItem od = genCond(tree.arg, false);
            result = od.negate();
        } else {
            Item od = genExpr(tree.arg, operator.type.getParameterTypes().head);
            switch(tree.getTag()) {
                case JCTree.POS:
                    result = od.load();
                    break;
                case JCTree.NEG:
                    result = od.load();
                    code.emitop0(operator.opcode);
                    break;
                case JCTree.COMPL:
                    result = od.load();
                    emitMinusOne(od.typecode);
                    code.emitop0(operator.opcode);
                    break;
                case JCTree.PREINC:
                case JCTree.PREDEC:
                    od.duplicate();
                    if (od instanceof LocalItem && (operator.opcode == iadd || operator.opcode == isub)) {
                        ((LocalItem) od).incr(tree.getTag() == JCTree.PREINC ? 1 : -1);
                        result = od;
                    } else {
                        od.load();
                        code.emitop0(one(od.typecode));
                        code.emitop0(operator.opcode);
                        if (od.typecode != INTcode && Code.truncate(od.typecode) == INTcode) code.emitop0(int2byte + od.typecode - BYTEcode);
                        result = items.makeAssignItem(od);
                    }
                    break;
                case JCTree.POSTINC:
                case JCTree.POSTDEC:
                    od.duplicate();
                    if (od instanceof LocalItem && (operator.opcode == iadd || operator.opcode == isub)) {
                        Item res = od.load();
                        ((LocalItem) od).incr(tree.getTag() == JCTree.POSTINC ? 1 : -1);
                        result = res;
                    } else {
                        Item res = od.load();
                        od.stash(od.typecode);
                        code.emitop0(one(od.typecode));
                        code.emitop0(operator.opcode);
                        if (od.typecode != INTcode && Code.truncate(od.typecode) == INTcode) code.emitop0(int2byte + od.typecode - BYTEcode);
                        od.store();
                        result = res;
                    }
                    break;
                case JCTree.NULLCHK:
                    result = od.load();
                    code.emitop0(dup);
                    genNullCheck(tree.pos());
                    break;
                default:
                    assert false;
            }
        }
    }

    /** Generate a null check from the object value at stack top. */
    private void genNullCheck(DiagnosticPosition pos) {
        callMethod(pos, syms.objectType, names.getClass, List.<Type>nil(), false);
        code.emitop0(pop);
    }

    public void visitBinary(JCBinary tree) {
        OperatorSymbol operator = (OperatorSymbol) tree.operator;
        if (operator.opcode == string_add) {
            makeStringBuffer(tree.pos());
            appendStrings(tree);
            bufferToString(tree.pos());
            result = items.makeStackItem(syms.stringType);
        } else if (tree.getTag() == JCTree.AND) {
            CondItem lcond = genCond(tree.lhs, CRT_FLOW_CONTROLLER);
            if (!lcond.isFalse()) {
                Chain falseJumps = lcond.jumpFalse();
                code.resolve(lcond.trueJumps);
                CondItem rcond = genCond(tree.rhs, CRT_FLOW_TARGET);
                result = items.makeCondItem(rcond.opcode, rcond.trueJumps, code.mergeChains(falseJumps, rcond.falseJumps));
            } else {
                result = lcond;
            }
        } else if (tree.getTag() == JCTree.OR) {
            CondItem lcond = genCond(tree.lhs, CRT_FLOW_CONTROLLER);
            if (!lcond.isTrue()) {
                Chain trueJumps = lcond.jumpTrue();
                code.resolve(lcond.falseJumps);
                CondItem rcond = genCond(tree.rhs, CRT_FLOW_TARGET);
                result = items.makeCondItem(rcond.opcode, code.mergeChains(trueJumps, rcond.trueJumps), rcond.falseJumps);
            } else {
                result = lcond;
            }
        } else {
            Item od = genExpr(tree.lhs, operator.type.getParameterTypes().head);
            od.load();
            result = completeBinop(tree.lhs, tree.rhs, operator);
        }
    }

    /** Make a new string buffer.
	 */
    void makeStringBuffer(DiagnosticPosition pos) {
        code.emitop2(new_, makeRef(pos, stringBufferType));
        code.emitop0(dup);
        callMethod(pos, stringBufferType, names.init, List.<Type>nil(), false);
    }

    /** Append value (on tos) to string buffer (on tos - 1).
	 */
    void appendString(JCTree tree) {
        Type t = tree.type.baseType();
        if (t.tag > lastBaseTag && t.tsym != syms.stringType.tsym) {
            t = syms.objectType;
        }
        items.makeMemberItem(getStringBufferAppend(tree, t), false).invoke();
    }

    Symbol getStringBufferAppend(JCTree tree, Type t) {
        assert t.constValue() == null;
        Symbol method = stringBufferAppend.get(t);
        if (method == null) {
            method = rs.resolveInternalMethod(tree.pos(), attrEnv, stringBufferType, names.append, List.of(t), null);
            stringBufferAppend.put(t, method);
        }
        return method;
    }

    /** Add all strings in tree to string buffer.
	 */
    void appendStrings(JCTree tree) {
        tree = TreeInfo.skipParens(tree);
        if (tree.getTag() == JCTree.PLUS && tree.type.constValue() == null) {
            JCBinary op = (JCBinary) tree;
            if (op.operator.kind == MTH && ((OperatorSymbol) op.operator).opcode == string_add) {
                appendStrings(op.lhs);
                appendStrings(op.rhs);
                return;
            }
        }
        genExpr(tree, tree.type).load();
        appendString(tree);
    }

    /** Convert string buffer on tos to string.
	 */
    void bufferToString(DiagnosticPosition pos) {
        callMethod(pos, stringBufferType, names.toString, List.<Type>nil(), false);
    }

    /** Complete generating code for operation, with left operand
	 *  already on stack.
	 *  @param lhs       The tree representing the left operand.
	 *  @param rhs       The tree representing the right operand.
	 *  @param operator  The operator symbol.
	 */
    Item completeBinop(JCTree lhs, JCTree rhs, OperatorSymbol operator) {
        MethodType optype = (MethodType) operator.type;
        int opcode = operator.opcode;
        if (opcode >= if_icmpeq && opcode <= if_icmple && rhs.type.constValue() instanceof Number && ((Number) rhs.type.constValue()).intValue() == 0) {
            opcode = opcode + (ifeq - if_icmpeq);
        } else if (opcode >= if_acmpeq && opcode <= if_acmpne && TreeInfo.isNull(rhs)) {
            opcode = opcode + (if_acmp_null - if_acmpeq);
        } else {
            Type rtype = operator.erasure(types).getParameterTypes().tail.head;
            if (opcode >= ishll && opcode <= lushrl) {
                opcode = opcode + (ishl - ishll);
                rtype = syms.intType;
            }
            genExpr(rhs, rtype).load();
            if (opcode >= (1 << preShift)) {
                code.emitop0(opcode >> preShift);
                opcode = opcode & 0xFF;
            }
        }
        if (opcode >= ifeq && opcode <= if_acmpne || opcode == if_acmp_null || opcode == if_acmp_nonnull) {
            return items.makeCondItem(opcode);
        } else {
            code.emitop0(opcode);
            return items.makeStackItem(optype.restype);
        }
    }

    public void visitTypeCast(JCTypeCast tree) {
        result = genExpr(tree.expr, tree.clazz.type).load();
        if (tree.clazz.type.tag > lastBaseTag && types.asSuper(tree.expr.type, tree.clazz.type.tsym) == null) {
            code.emitop2(checkcast, makeRef(tree.pos(), tree.clazz.type));
        }
    }

    public void visitWildcard(JCWildcard tree) {
        throw new AssertionError(this.getClass().getName());
    }

    public void visitTypeTest(JCInstanceOf tree) {
        genExpr(tree.expr, tree.expr.type).load();
        code.emitop2(instanceof_, makeRef(tree.pos(), tree.clazz.type));
        result = items.makeStackItem(syms.booleanType);
    }

    public void visitIndexed(JCArrayAccess tree) {
        genExpr(tree.indexed, tree.indexed.type).load();
        genExpr(tree.index, syms.intType).load();
        result = items.makeIndexedItem(tree.type);
    }

    public void visitIdent(JCIdent tree) {
        Symbol sym = tree.sym;
        if (tree.name == names._this || tree.name == names._super) {
            Item res = tree.name == names._this ? items.makeThisItem() : items.makeSuperItem();
            if (sym.kind == MTH) {
                res.load();
                res = items.makeMemberItem(sym, true);
            }
            result = res;
        } else if (sym.kind == VAR && sym.owner.kind == MTH) {
            result = items.makeLocalItem((VarSymbol) sym);
        } else if ((sym.flags() & STATIC) != 0) {
            if (!isAccessSuper(env.enclMethod)) sym = binaryQualifier(sym, env.enclClass.type);
            result = items.makeStaticItem(sym);
        } else {
            items.makeThisItem().load();
            sym = binaryQualifier(sym, env.enclClass.type);
            result = items.makeMemberItem(sym, (sym.flags() & PRIVATE) != 0);
        }
    }

    public void visitSelect(JCFieldAccess tree) {
        Symbol sym = tree.sym;
        if (tree.name == names._class) {
            assert target.hasClassLiterals();
            code.emitop2(ldc2, makeRef(tree.pos(), tree.selected.type));
            result = items.makeStackItem(pt);
            return;
        }
        Symbol ssym = TreeInfo.symbol(tree.selected);
        boolean selectSuper = ssym != null && (ssym.kind == TYP || ssym.name == names._super);
        boolean accessSuper = isAccessSuper(env.enclMethod);
        Item base = (selectSuper) ? items.makeSuperItem() : genExpr(tree.selected, tree.selected.type);
        if (sym.kind == VAR && ((VarSymbol) sym).getConstValue() != null) {
            if ((sym.flags() & STATIC) != 0) {
                if (!selectSuper && (ssym == null || ssym.kind != TYP)) base = base.load();
                base.drop();
            } else {
                base.load();
                genNullCheck(tree.selected.pos());
            }
            result = items.makeImmediateItem(sym.type, ((VarSymbol) sym).getConstValue());
        } else {
            if (!accessSuper) sym = binaryQualifier(sym, tree.selected.type);
            if ((sym.flags() & STATIC) != 0) {
                if (!selectSuper && (ssym == null || ssym.kind != TYP)) base = base.load();
                base.drop();
                result = items.makeStaticItem(sym);
            } else {
                base.load();
                if (sym == syms.lengthVar) {
                    code.emitop0(arraylength);
                    result = items.makeStackItem(syms.intType);
                } else {
                    result = items.makeMemberItem(sym, (sym.flags() & PRIVATE) != 0 || selectSuper || accessSuper);
                }
            }
        }
    }

    public void visitLiteral(JCLiteral tree) {
        if (tree.type.tag == TypeTags.BOT) {
            code.emitop0(aconst_null);
            if (types.dimensions(pt) > 1) {
                code.emitop2(checkcast, makeRef(tree.pos(), pt));
                result = items.makeStackItem(pt);
            } else {
                result = items.makeStackItem(tree.type);
            }
        } else result = items.makeImmediateItem(tree.type, tree.value);
    }

    public void visitLetExpr(LetExpr tree) {
        int limit = code.nextreg;
        genStats(tree.defs, env);
        result = genExpr(tree.expr, tree.expr.type).load();
        code.endScopes(limit);
    }

    /** Generate code for a class definition.
     *  @param env   The attribution environment that belongs to the
     *               outermost class containing this class definition.
     *               We need this for resolving some additional symbols.
     *  @param cdef  The tree representing the class definition.
     *  @return      True if code is generated with no errors.
     */
    public boolean genClass(Env<AttrContext> env, JCClassDecl cdef) {
        try {
            attrEnv = env;
            ClassSymbol c = cdef.sym;
            this.toplevel = env.toplevel;
            this.endPositions = toplevel.endPositions;
            if (generateIproxies && (c.flags() & (INTERFACE | ABSTRACT)) == ABSTRACT && !allowGenerics) implementInterfaceMethods(c);
            cdef.defs = normalizeDefs(cdef.defs, c);
            c.pool = pool;
            pool.reset();
            Env<GenContext> localEnv = new Env<GenContext>(cdef, new GenContext());
            localEnv.toplevel = env.toplevel;
            localEnv.enclClass = cdef;
            for (List<JCTree> l = cdef.defs; l.nonEmpty(); l = l.tail) {
                genDef(l.head, localEnv);
            }
            if (pool.numEntries() > Pool.MAX_ENTRIES) {
                log.error(cdef.pos(), "limit.pool");
                nerrs++;
            }
            if (nerrs != 0) {
                for (List<JCTree> l = cdef.defs; l.nonEmpty(); l = l.tail) {
                    if (l.head.getTag() == JCTree.METHODDEF) ((JCMethodDecl) l.head).sym.code = null;
                }
            }
            cdef.defs = List.nil();
            return nerrs == 0;
        } finally {
            attrEnv = null;
            this.env = null;
            toplevel = null;
            endPositions = null;
            nerrs = 0;
        }
    }

    /** An abstract class for finalizer generation.
     */
    abstract class GenFinalizer {

        /** Generate code to clean up when unwinding. */
        abstract void gen();

        /** Generate code to clean up at last. */
        abstract void genLast();

        /** Does this finalizer have some nontrivial cleanup to perform? */
        boolean hasFinalizer() {
            return true;
        }
    }

    /** code generation contexts,
     *  to be used as type parameter for environments.
     */
    static class GenContext {

        /** A chain for all unresolved jumps that exit the current environment.
	 */
        Chain exit = null;

        /** A chain for all unresolved jumps that continue in the
	 *  current environment.
	 */
        Chain cont = null;

        /** A closure that generates the finalizer of the current environment.
	 *  Only set for Synchronized and Try contexts.
	 */
        GenFinalizer finalize = null;

        /** Is this a switch statement?  If so, allocate registers
	 * even when the variable declaration is unreachable.
	 */
        boolean isSwitch = false;

        /** A list buffer containing all gaps in the finalizer range,
	 *  where a catch all exception should not apply.
	 */
        ListBuffer<Integer> gaps = null;

        /** Add given chain to exit chain.
	 */
        void addExit(Chain c) {
            exit = Code.mergeChains(c, exit);
        }

        /** Add given chain to cont chain.
	 */
        void addCont(Chain c) {
            cont = Code.mergeChains(c, cont);
        }
    }
}
