package de.grogra.xl.compiler.scope;

import java.util.HashMap;
import antlr.collections.AST;
import de.grogra.reflect.ClassAdapter;
import de.grogra.reflect.Member;
import de.grogra.reflect.Reflection;
import de.grogra.reflect.Type;
import de.grogra.reflect.TypeId;
import de.grogra.util.Utils;
import de.grogra.xl.compiler.Compiler;
import de.grogra.xl.compiler.XMethod;
import de.grogra.xl.expr.AssignLocal;
import de.grogra.xl.expr.Block;
import de.grogra.xl.expr.Break;
import de.grogra.xl.expr.BreakTarget;
import de.grogra.xl.expr.Completable;
import de.grogra.xl.expr.ControlTransfer;
import de.grogra.xl.expr.EnterFrame;
import de.grogra.xl.expr.Expression;
import de.grogra.xl.expr.Finally;
import de.grogra.xl.expr.InvokeStatic;
import de.grogra.xl.expr.LeaveFrame;
import de.grogra.xl.expr.LocalAccess;
import de.grogra.xl.expr.NonlocalGenerator;
import de.grogra.xl.expr.Return;
import de.grogra.xl.expr.TryFinally;
import de.grogra.xl.util.IntHashMap;
import de.grogra.xl.util.IntList;
import de.grogra.xl.util.ObjectList;
import de.grogra.xl.vmx.VMXState;

public class MethodScope extends BlockScope {

    public Local consumer;

    public Local enclosingInstance;

    public AST ast;

    private final ObjectList allLocals = new ObjectList(10, false);

    private final ObjectList exceptionTypes = new ObjectList(5, false);

    private XMethod method;

    private int jsize = 0, xsize = 0;

    private final long modifiers;

    private final ObjectList labelStack = new ObjectList(10);

    private final IntList labelIdStack = new IntList(10);

    private static final int CONTINUE_LABEL = 1;

    private static final int USED = 2;

    private final IntList labelFlags = new IntList(10);

    private final IntHashMap labelToTarget = new IntHashMap(10);

    private int nextLabelId = 1;

    private ObjectList paramsForLocals;

    private Local result;

    private Local localForVMX;

    MethodScope(TypeScope enclosing, long modifiers) {
        super(enclosing);
        this.modifiers = modifiers;
        if ((modifiers & Member.STATIC) == 0) {
            declareParameter("this", Member.FINAL | Compiler.MOD_THIS_PARAMETER, enclosing.getDeclaredType());
        }
        if (((modifiers & Compiler.MOD_CONSTRUCTOR) != 0) && !enclosing.isStatic()) {
            enclosingInstance = declareParameter("this$0", Member.FINAL, enclosing.getDeclaredType().getDeclaringType());
        }
    }

    public MethodScope(MethodScope enclosing) {
        super(enclosing);
        this.modifiers = Compiler.ROUTINE_MODIFIERS;
        declareLocalForVMX();
        enclosing.createVMXFrame();
    }

    @Override
    public Member getDeclaredEntity() {
        return method;
    }

    public void createVMXFrame() {
        if (localForVMX != null) {
            return;
        }
        declareLocalForVMX();
        Block b = new Block();
        Compiler.setBlockScope(b, this);
        b.add(new EnterFrame(localForVMX));
        TryFinally tf = new TryFinally();
        tf.setBranch(method.getBranch());
        tf.add(new Finally(null, null).add(new LeaveFrame(localForVMX)));
        b.add(tf);
        method.setBranch(b);
    }

    public void createLocalForVMX() {
        if (localForVMX == null) {
            declareLocalForVMX();
            InvokeStatic i = new InvokeStatic(Reflection.findMethodInClasses(localForVMX.getType(), "current"));
            i.complete(this);
            AssignLocal a = localForVMX.createSet();
            a.complete(this);
            getBlock().insertBranchNode(0, a.add(i));
        }
    }

    public Local getLocalForVMX() {
        return localForVMX;
    }

    private void declareLocalForVMX() {
        localForVMX = declareLocal("vmx.", Member.FINAL, ClassAdapter.wrap(VMXState.class), null);
        allocateFixed(localForVMX);
    }

    public XMethod createAndDeclareMethod(String name, Type returnType) {
        if (method != null) {
            throw new IllegalStateException("method != null");
        }
        if ((modifiers & (Compiler.MOD_CONSTRUCTOR | Member.SYNTHETIC)) == Member.SYNTHETIC) {
            name = name.replace('<', '$').replace('>', '$');
            name = Reflection.getSyntheticName(Reflection.getDeclaredMethods(getDeclaredType()), name);
        }
        if (((modifiers & Compiler.MOD_CONSTRUCTOR) != 0) && !("<init>".equals(name) && (returnType.getTypeId() == TypeId.VOID))) {
            throw new IllegalArgumentException(name + " " + returnType);
        }
        method = new XMethod(name, modifiers, this, returnType, (Type[]) exceptionTypes.toArray(new Type[exceptionTypes.size]));
        Compiler.setBlockScope(method, this);
        method.add(getBlock());
        return method;
    }

    private int uniqueInt = 0;

    final int nextUniqueInt() {
        return getOutmost().uniqueInt++;
    }

    @Override
    public Expression setBlock(Expression block) {
        if ((method == null) && (getBlock() == null)) {
            return super.setBlock(block);
        }
        throw new AssertionError();
    }

    public XMethod getMethod() {
        return method;
    }

    @Override
    public boolean isStatic() {
        return (modifiers & Member.STATIC) != 0;
    }

    public boolean isInitializer() {
        return (modifiers & Compiler.MOD_INITIALIZER) != 0;
    }

    public boolean isConstructor() {
        return (modifiers & Compiler.MOD_CONSTRUCTOR) != 0;
    }

    public boolean isIllegalUseBeforeDeclaration(de.grogra.reflect.Field field) {
        TypeScope ts = TypeScope.get(this);
        if (((modifiers & Compiler.MOD_INITIALIZER) == 0) || (Reflection.isStatic(field) != isStatic()) || (ts.getDeclaredType() != field.getDeclaringType())) {
            return false;
        }
        AST d = ts.getASTOfDeclaration(field);
        if (d == null) {
            if ((field.getModifiers() & Member.SYNTHETIC) != 0) {
                return false;
            }
            throw new AssertionError();
        }
        for (AST a = ast; a != null; a = a.getNextSibling()) {
            if (a == d) {
                return true;
            }
        }
        for (AST a = d; a != null; a = a.getNextSibling()) {
            if (a == ast) {
                return false;
            }
        }
        throw new AssertionError();
    }

    public boolean isLocal() {
        return false;
    }

    public static MethodScope get(Expression e) {
        while (!(e instanceof XMethod)) {
            e = (Expression) e.getAxisParent();
        }
        return (MethodScope) Compiler.getBlockScope(e);
    }

    public static MethodScope get(Scope scope) {
        while (!(scope instanceof MethodScope)) {
            if (scope == null) {
                return null;
            }
            scope = scope.getEnclosingScope();
        }
        return (MethodScope) scope;
    }

    public MethodScope getOutmost() {
        MethodScope s = this;
        while (true) {
            MethodScope s2 = MethodScope.get(s.getEnclosingScope());
            if (s2 == null) {
                return s;
            }
            s = s2;
        }
    }

    public void declareException(Type type) {
        exceptionTypes.add(type);
    }

    public Local declareParameter(String name, long mods, Class cls) {
        return declareParameter(name, mods, ClassAdapter.wrap(cls));
    }

    public Local declareParameter(String name, long mods, Type type) {
        Local l = declareLocal(name, mods | Compiler.MOD_PARAMETER, type, null);
        if ((modifiers & Compiler.MOD_ROUTINE) != 0) {
            l.nesting = 0;
        }
        allocateFixed(l);
        return l;
    }

    public Local getParameterForEnclosingLocal(Local enclosing) {
        if (!isConstructor()) {
            return null;
        }
        ObjectList enc = TypeScope.get(this).getEnclosingLocals();
        int idx = enc.indexOf(enclosing);
        if (idx < 0) {
            return null;
        }
        if (paramsForLocals == null) {
            paramsForLocals = new ObjectList();
        }
        for (int i = paramsForLocals.size(); i <= idx; i++) {
            Local e = (Local) enc.get(i);
            Local loc = declareParameter("arg$" + e.getName() + '.', 0, e.getType());
            paramsForLocals.add(loc);
        }
        return (Local) paramsForLocals.get(idx);
    }

    void declareResultLocal() {
        if (result == null) {
            result = declareLocal("result.", 0, method.getReturnType(), null);
        }
    }

    public VMXState.Local getResultLocal() {
        return (result != null) ? result.createVMXLocal() : null;
    }

    public boolean enterLabel(String name) {
        boolean exists = getTargetId(name, false) >= 0;
        labelStack.push(name);
        labelFlags.push(0);
        labelIdStack.push(nextLabelId++);
        return exists;
    }

    public void enterBreakTarget(String label) {
        labelStack.push(null);
        labelFlags.push(0);
        labelIdStack.push((label != null) ? labelIdStack.peek(1) : nextLabelId++);
    }

    public void enterContinueTarget(String label) {
        labelStack.push(label);
        labelFlags.push(CONTINUE_LABEL);
        labelIdStack.push(nextLabelId++);
    }

    public void leave(BreakTarget e) {
        int id = labelIdStack.pop();
        if ((labelFlags.pop() & USED) != 0) {
            e.initialize(id);
            labelToTarget.put(id, e);
        }
        labelStack.pop();
    }

    public int getTargetId(String name, boolean forContinue) {
        for (int i = labelStack.size() - 1; i >= 0; i--) {
            Object label = labelStack.get(i);
            int flags = labelFlags.get(i);
            if (forContinue) {
                if ((flags & CONTINUE_LABEL) != 0) {
                    if ((name == null) || name.equals(label)) {
                        labelFlags.set(i, flags | USED);
                        return labelIdStack.get(i);
                    }
                } else if ((name != null) && name.equals(label)) {
                    return -2;
                }
            } else if (((flags & CONTINUE_LABEL) == 0) && Utils.equal(name, label)) {
                labelFlags.set(i, flags | USED);
                return labelIdStack.get(i);
            }
        }
        return -1;
    }

    public BreakTarget getTargetFor(Break e) {
        return (BreakTarget) labelToTarget.get(e.getLabel(), null);
    }

    @Override
    public Expression createThis() {
        assert (modifiers & Member.STATIC) == 0;
        Expression e = findLocal("this", false).createGet();
        e.lval |= Compiler.EXPR_FINAL | Compiler.EXPR_THIS;
        return e;
    }

    public final Local makeVMXLocal(Local local) {
        assert local.isJavaLocal();
        receiveLocal(local);
        Local param = null;
        if (local.isParameter()) {
            if (Reflection.isFinal(local)) {
                param = local;
                local = declareLocal(local.getName() + '.', Member.FINAL, local.getType(), local.getAST());
            } else {
                param = declareLocal(local.getName() + '.', Compiler.MOD_PARAMETER | Member.FINAL, local.getType(), local.getAST());
                param.index = local.index;
                local.unsetParameter();
            }
        } else if (local.index >= 0) {
            throw new AssertionError(local);
        }
        local.nesting = 0;
        if (param != null) {
            getBlock().insertBranchNode(0, local.createSet().add(param.createGet()));
        }
        return local;
    }

    public Type[] getParameterTypes() {
        ObjectList list = new ObjectList();
        for (int i = 0; i < allLocals.size; i++) {
            Local l = (Local) allLocals.get(i);
            if (l.isParameter() && ((l.getModifiersEx() & Compiler.MOD_THIS_PARAMETER) == 0)) {
                list.add(l.getType());
            }
        }
        if (list.size == 0) {
            return Type.TYPE_0;
        }
        Type[] t = new Type[list.size];
        list.toArray(t);
        return t;
    }

    public Local getParameter(int index) {
        for (int i = 0; i < allLocals.size; i++) {
            Local l = (Local) allLocals.get(i);
            if (l.isParameter() && ((l.getModifiersEx() & Compiler.MOD_THIS_PARAMETER) == 0)) {
                if (--index < 0) {
                    return l;
                }
            }
        }
        return null;
    }

    private int allocateFixed(Local local) {
        if ((local.ref == null) && (local.index < 0)) {
            int s = (local.nesting == VMXState.Local.JAVA) ? jsize : xsize;
            local.index = s;
            s += Reflection.getJVMStackSize(local.getType());
            if (local.nesting == VMXState.Local.JAVA) {
                jsize = s;
            } else {
                xsize = s;
            }
        }
        return local.index;
    }

    public void removeLocal(Local l) {
        allLocals.remove(l);
    }

    void receiveLocal(Local l) {
        if (l.methodScope != this) {
            if (l.methodScope != null) {
                l.methodScope.allLocals.remove(l);
            }
            allLocals.add(l);
            l.methodScope = this;
        }
    }

    private static int computeAccesses(Expression e, int seqId) {
        seqId++;
        for (Expression f = e.getFirstExpression(); f != null; f = f.getNextExpression()) {
            seqId = computeAccesses(f, seqId);
        }
        seqId++;
        if (e instanceof LocalAccess) {
            LocalAccess a = (LocalAccess) e;
            for (int i = a.getLocalCount() - 1; i >= 0; i--) {
                Local l = a.getLocal(i);
                if (l != null) {
                    if (l.firstAccess == null) {
                        l.firstAccess = e;
                        l.accessRoot = e;
                        l.lastAccess = e;
                    } else {
                        l.lastAccess = e;
                        l.accessRoot = (Expression) l.accessRoot.getCommonAncestor(e);
                    }
                }
            }
        }
        if (e instanceof XMethod) {
            MethodScope ms = MethodScope.get(e);
            for (int i = ms.allLocals.size - 1; i >= 0; i--) {
                Local l = (Local) ms.allLocals.elements[i];
                if ((l.accessRoot != null) && !l.isParameter()) {
                    MethodScope s = get(l.accessRoot);
                    if (s != ms) {
                        s.receiveLocal(l);
                    }
                }
            }
        }
        return seqId;
    }

    private int nesting;

    private HashMap<Local, Local> nestedLocals;

    private void computeNesting(Expression e, MethodScope root) {
        for (Expression f = e.getFirstExpression(); f != null; f = f.getNextExpression()) {
            if (f instanceof XMethod) {
                MethodScope ms = MethodScope.get(f);
                ms.nesting = nesting + 1;
                ms.computeNesting(f, root);
            } else {
                computeNesting(f, root);
            }
        }
        if (e instanceof ControlTransfer) {
            ControlTransfer ct = (ControlTransfer) e;
            MethodScope targetScope;
            if (ct instanceof Break) {
                targetScope = MethodScope.get(root.getTargetFor((Break) ct));
            } else if (ct instanceof Return) {
                targetScope = ((Return) ct).getScope();
            } else {
                throw new AssertionError(ct);
            }
            ct.setNesting(nesting - targetScope.nesting);
            MethodScope m = this;
            if (m != targetScope) {
                while (m != targetScope) {
                    e = (Expression) m.getMethod().getAxisParent();
                    m = MethodScope.get(m.getEnclosingScope());
                }
                ((NonlocalGenerator) e).addTransfer(ct);
            }
            if (ct instanceof Return) {
                while (e != m.getBlock()) {
                    if (e.needsEmptyOperandStackForFinally()) {
                        m.declareResultLocal();
                        break;
                    }
                    e = (Expression) e.getAxisParent();
                }
            }
        } else if (e instanceof LocalAccess) {
            LocalAccess a = (LocalAccess) e;
            for (int i = a.getLocalCount() - 1; i >= 0; i--) {
                Local l = a.getLocal(i);
                if ((l != null) && (l.methodScope != this)) {
                    if (l.isJavaLocal()) {
                        l = l.methodScope.makeVMXLocal(l);
                    }
                    Local n;
                    if (nestedLocals == null) {
                        nestedLocals = new HashMap<Local, Local>();
                        n = null;
                    } else {
                        n = nestedLocals.get(l);
                    }
                    if (n == null) {
                        n = declareLocal(l.getName(), 0, l.getType(), l.getAST());
                        n.ref = l;
                        n.nesting = nesting - l.methodScope.nesting;
                        nestedLocals.put(l, n);
                    }
                    a.setLocal(i, n);
                }
            }
        }
    }

    public void complete() {
        assert (method != null) : this;
        if (method == null) {
            throw new NullPointerException();
        }
        computeAccesses(method, 0);
        nesting = 0;
        computeNesting(getBlock(), this);
        complete(method, null);
    }

    private static void complete(Expression e, MethodScope ms) {
        MethodScope newMs;
        if (e instanceof XMethod) {
            ms = MethodScope.get(e);
            ms.allocateLocals();
            e.removeFromChain();
            newMs = ms;
        } else {
            newMs = null;
        }
        if (e instanceof Completable) {
            ((Completable) e).complete(ms);
        }
        Expression n;
        for (e = e.getFirstExpression(); e != null; e = n) {
            n = e.getNextExpression();
            complete(e, ms);
        }
        if (newMs != null) {
            newMs.method.setJFrameSize(newMs.jsize);
            newMs.method.setXFrameSize(newMs.xsize);
        }
    }

    private void allocateLocals() {
        for (int i = 0; i < allLocals.size; i++) {
            allocateFixed((Local) allLocals.elements[i]);
        }
    }

    public void dumpLocals() {
        for (int i = 0; i < allLocals.size; i++) {
            System.err.println(allLocals.get(i));
        }
    }

    @Override
    public String toString() {
        return "MethodScope[" + TypeScope.get(this).getDeclaredType() + ',' + method + ',' + Reflection.modifiersToString((int) modifiers) + ']';
    }

    public void dispose() {
        super.dispose();
        allLocals.clear();
        labelToTarget.clear();
        nestedLocals = null;
        paramsForLocals = null;
        result = null;
        localForVMX = null;
        consumer = null;
        enclosingInstance = null;
    }
}
