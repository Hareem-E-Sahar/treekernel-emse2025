package kawa.standard;

import gnu.lists.*;
import gnu.mapping.*;
import gnu.expr.*;
import gnu.kawa.functions.IsEq;
import gnu.kawa.functions.ApplyToArgs;
import gnu.kawa.reflect.Invoke;
import gnu.kawa.reflect.SlotGet;

/** Implement the Scheme standard functions "map" and "for-each".
 * @author Per Bothner
 */
public class map extends gnu.mapping.ProcedureN implements CanInline {

    /** True if we should collect the result into a list. */
    boolean collect;

    final ApplyToArgs applyToArgs;

    final Declaration applyFieldDecl;

    public map(boolean collect, ApplyToArgs applyToArgs, Declaration applyFieldDecl) {
        super(collect ? "map" : "for-each");
        this.collect = collect;
        this.applyToArgs = applyToArgs;
        this.applyFieldDecl = applyFieldDecl;
    }

    /** An optimized single-list version of map. */
    public static Object map1(Procedure proc, Object list) throws Throwable {
        Object result = LList.Empty;
        Pair last = null;
        while (list != LList.Empty) {
            Pair pair = (Pair) list;
            Pair new_pair = new Pair(proc.apply1(pair.getCar()), LList.Empty);
            if (last == null) result = new_pair; else last.setCdr(new_pair);
            last = new_pair;
            list = pair.getCdr();
        }
        return result;
    }

    /** An optimized single-list version of for-each. */
    public static void forEach1(Procedure proc, Object list) throws Throwable {
        while (list != LList.Empty) {
            Pair pair = (Pair) list;
            proc.apply1(pair.getCar());
            list = pair.getCdr();
        }
    }

    public Object apply2(Object arg1, Object arg2) throws Throwable {
        if (arg1 instanceof Procedure) {
            Procedure proc = (Procedure) arg1;
            if (collect) return map1(proc, arg2);
            forEach1(proc, arg2);
            return Values.empty;
        }
        return applyN(new Object[] { arg1, arg2 });
    }

    public Object applyN(Object[] args) throws Throwable {
        int arity = args.length - 1;
        if (arity == 1 && args[0] instanceof Procedure) {
            Procedure proc = (Procedure) (args[0]);
            if (collect) return map1(proc, args[1]);
            forEach1(proc, args[1]);
            return Values.empty;
        }
        Object result;
        Pair last = null;
        if (collect) result = LList.Empty; else result = Values.empty;
        ;
        Object[] rest = new Object[arity];
        System.arraycopy(args, 1, rest, 0, arity);
        Procedure proc;
        int need_apply;
        Object[] each_args;
        if (args[0] instanceof Procedure) {
            need_apply = 0;
            each_args = new Object[arity];
            proc = (Procedure) args[0];
        } else {
            need_apply = 1;
            each_args = new Object[arity + 1];
            each_args[0] = args[0];
            proc = applyToArgs;
        }
        for (; ; ) {
            for (int i = 0; i < arity; i++) {
                Object list = rest[i];
                if (list == LList.Empty) return result;
                Pair pair = (Pair) list;
                each_args[need_apply + i] = pair.getCar();
                rest[i] = pair.getCdr();
            }
            Object value = proc.applyN(each_args);
            if (collect) {
                Pair new_pair = new Pair(value, LList.Empty);
                if (last == null) result = new_pair; else last.setCdr(new_pair);
                last = new_pair;
            }
        }
    }

    public Expression inline(ApplyExp exp, InlineCalls walker, boolean argsInlined) {
        exp.walkArgs(walker, argsInlined);
        Expression[] args = exp.getArgs();
        int nargs = args.length;
        if (nargs < 2) return exp;
        nargs--;
        Expression proc = args[0];
        boolean procSafeForMultipleEvaluation = !proc.side_effects();
        Expression[] inits1 = new Expression[1];
        inits1[0] = proc;
        LetExp let1 = new LetExp(inits1);
        Declaration procDecl = let1.addDeclaration("%proc", Compilation.typeProcedure);
        procDecl.noteValue(args[0]);
        Expression[] inits2 = new Expression[1];
        LetExp let2 = new LetExp(inits2);
        let1.setBody(let2);
        LambdaExp lexp = new LambdaExp(collect ? nargs + 1 : nargs);
        inits2[0] = lexp;
        Declaration loopDecl = let2.addDeclaration("%loop");
        loopDecl.noteValue(lexp);
        Expression[] inits3 = new Expression[nargs];
        LetExp let3 = new LetExp(inits3);
        Declaration[] largs = new Declaration[nargs];
        Declaration[] pargs = new Declaration[nargs];
        IsEq isEq = Scheme.isEq;
        for (int i = 0; i < nargs; i++) {
            String argName = "arg" + i;
            largs[i] = lexp.addDeclaration(argName);
            pargs[i] = let3.addDeclaration(argName, Compilation.typePair);
            inits3[i] = new ReferenceExp(largs[i]);
            pargs[i].noteValue(inits3[i]);
        }
        Declaration resultDecl = collect ? lexp.addDeclaration("result") : null;
        Expression[] doArgs = new Expression[1 + nargs];
        Expression[] recArgs = new Expression[collect ? nargs + 1 : nargs];
        for (int i = 0; i < nargs; i++) {
            doArgs[i + 1] = walker.walkApplyOnly(SlotGet.makeGetField(new ReferenceExp(pargs[i]), "car"));
            recArgs[i] = walker.walkApplyOnly(SlotGet.makeGetField(new ReferenceExp(pargs[i]), "cdr"));
        }
        if (!procSafeForMultipleEvaluation) proc = new ReferenceExp(procDecl);
        doArgs[0] = proc;
        Expression doit = walker.walkApplyOnly(new ApplyExp(new ReferenceExp(applyFieldDecl), doArgs));
        Expression rec = walker.walkApplyOnly(new ApplyExp(new ReferenceExp(loopDecl), recArgs));
        if (collect) {
            Expression[] consArgs = new Expression[2];
            consArgs[0] = doit;
            consArgs[1] = new ReferenceExp(resultDecl);
            recArgs[nargs] = Invoke.makeInvokeStatic(Compilation.typePair, "make", consArgs);
            lexp.body = rec;
        } else {
            lexp.body = new BeginExp(doit, rec);
        }
        let3.setBody(lexp.body);
        lexp.body = let3;
        Expression[] initArgs = new Expression[collect ? nargs + 1 : nargs];
        QuoteExp empty = new QuoteExp(LList.Empty);
        for (int i = nargs; --i >= 0; ) {
            Expression[] compArgs = new Expression[2];
            compArgs[0] = new ReferenceExp(largs[i]);
            compArgs[1] = empty;
            Expression result = collect ? (Expression) new ReferenceExp(resultDecl) : (Expression) QuoteExp.voidExp;
            lexp.body = new IfExp(walker.walkApplyOnly(new ApplyExp(isEq, compArgs)), result, lexp.body);
            initArgs[i] = args[i + 1];
        }
        if (collect) initArgs[nargs] = empty;
        Expression body = walker.walkApplyOnly(new ApplyExp(new ReferenceExp(loopDecl), initArgs));
        if (collect) {
            Expression[] reverseArgs = new Expression[1];
            reverseArgs[0] = body;
            body = Invoke.makeInvokeStatic(Compilation.scmListType, "reverseInPlace", reverseArgs);
        }
        let2.setBody(body);
        if (procSafeForMultipleEvaluation) return let2; else return let1;
    }
}
