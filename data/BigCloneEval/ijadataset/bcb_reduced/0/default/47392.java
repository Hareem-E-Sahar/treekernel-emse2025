/**
 * Common code for inline oracles.
 * This class's shouldInline method factors out the basic logic
 * and then delegates to the subclass method to make all non-trivial decisions.
 *
 * @author Stephen Fink
 * @author Dave Grove
 */
abstract class OPT_GenericInlineOracle extends OPT_InlineTools implements OPT_InlineOracle {

    /**
   * Should we inline a particular call site?
   *
   * @param state information needed to make the inlining decision
   * @eturns an OPT_InlineDecision with the result
   *
   */
    public OPT_InlineDecision shouldInline(OPT_CompilationState state) {
        if (!state.getOptions().INLINE) {
            return OPT_InlineDecision.NO("inlining not enabled");
        }
        VM_Method caller = state.getMethod();
        VM_Method callee = state.obtainTarget();
        int inlinedSizeEstimate = 0;
        if (state.isInvokeInterface()) {
            if (!callee.getDeclaringClass().isLoaded()) {
                return OPT_InlineDecision.NO("Cannot inline interface before it is loaded");
            }
            return shouldInlineInterfaceInternal(caller, callee, state);
        } else {
            if (!legalToInline(caller, callee)) return OPT_InlineDecision.NO("illegal inlining");
            boolean guardless = state.getComputedTarget() != null || !needsGuard(callee);
            if (guardless && OPT_InlineTools.hasInlinePragma(callee, state)) return OPT_InlineDecision.YES(callee, "pragmaInline");
            if (OPT_InlineTools.hasNoInlinePragma(callee, state)) return OPT_InlineDecision.NO("pragmaNoInline");
            inlinedSizeEstimate = OPT_InlineTools.inlinedSizeEstimate(callee, state);
            if (inlinedSizeEstimate < state.getOptions().IC_MAX_ALWAYS_INLINE_TARGET_SIZE && guardless && !state.getSequence().containsMethod(callee)) {
                return OPT_InlineDecision.YES(callee, "trivial inline");
            }
        }
        return shouldInlineInternal(caller, callee, state, inlinedSizeEstimate);
    }

    /**
   * Children must implement this method.
   * It contains the non-generic decision making portion of the oracle for invokevirtual,
   * invokespecial, and invokestatic.
   */
    protected abstract OPT_InlineDecision shouldInlineInternal(VM_Method caller, VM_Method callee, OPT_CompilationState state, int inlinedSizeEstimate);

    /**
   * Children must implement this method.
   * It contains the non-generic decision making portion of the oracle for invokeinterface.
   */
    protected abstract OPT_InlineDecision shouldInlineInterfaceInternal(VM_Method caller, VM_Method callee, OPT_CompilationState state);

    /**
   * Logic to select the appropriate guarding mechanism for the edge
   * from caller to callee according to the controlling OPT_Options.
   * If we are using IG_CODE_PATCH, then these method also records 
   * the required dependency.
   * 
   * @param caller the caller method
   * @param callee the callee method
   * @param opts the opt options
   * @param codePatchSupported can we use code patching at this call site?
   */
    protected byte chooseGuard(VM_Method caller, VM_Method callee, OPT_CompilationState state, boolean codePatchSupported) {
        byte guard = state.getOptions().INLINING_GUARD;
        if (codePatchSupported) {
            if (guard == OPT_Options.IG_CODE_PATCH) {
                if (OPT_ClassLoadingDependencyManager.TRACE || OPT_ClassLoadingDependencyManager.DEBUG) {
                    VM_Class.OptCLDepManager.report("CODE PATCH: Inlined " + callee + " into " + caller + "\n");
                }
                VM_Class.OptCLDepManager.addNotOverriddenDependency(callee, state.getCompiledMethodId());
            }
        } else if (guard == OPT_Options.IG_CODE_PATCH) {
            guard = OPT_Options.IG_METHOD_TEST;
        }
        if (guard == OPT_Options.IG_METHOD_TEST && callee.getDeclaringClass().isFinal()) {
            guard = OPT_Options.IG_CLASS_TEST;
        }
        return guard;
    }

    /**
   * Estimate the expected cost of the inlining action
   * (inclues both the inline body and the guard/off-branch code).
   *
   * @param inlinedBodyEstimate size estimate for inlined body
   * @param needsGuard is it going to be a guarded inline?
   * @param preEx      can preEx inlining be used to avoid the guard?
   * @param opts       controlling options object
   * @return the estimated cost of the inlining action
   */
    protected int inliningActionCost(int inlinedBodyEstimate, boolean needsGuard, boolean preEx, OPT_Options opts) {
        int guardCost = 0;
        if (needsGuard & !preEx) {
            guardCost += VM_OptMethodSummary.CALL_COST;
            if (opts.guardWithMethodTest()) {
                guardCost += 3 * VM_OptMethodSummary.SIMPLE_OPERATION_COST;
            } else if (opts.guardWithCodePatch()) {
                guardCost += 1 * VM_OptMethodSummary.SIMPLE_OPERATION_COST;
            } else {
                guardCost += 2 * VM_OptMethodSummary.SIMPLE_OPERATION_COST;
            }
        }
        return guardCost + inlinedBodyEstimate;
    }
}
