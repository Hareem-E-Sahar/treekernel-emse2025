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
        if (!state.isInvokeInterface()) {
            if (!legalToInline(caller, callee)) return OPT_InlineDecision.NO("illegal inlining");
            if (OPT_InlineTools.hasInlinePragma(callee, state)) return OPT_InlineDecision.YES(callee, "pragmaInline");
            if (OPT_InlineTools.hasNoInlinePragma(callee, state)) return OPT_InlineDecision.NO("pragmaNoInline");
            inlinedSizeEstimate = OPT_InlineTools.inlinedSizeEstimate(callee, state);
            if (inlinedSizeEstimate < state.getOptions().IC_MAX_ALWAYS_INLINE_TARGET_SIZE && (!needsGuard(callee) || state.getComputedTarget() != null) && !state.getSequence().containsMethod(callee)) {
                return OPT_InlineDecision.YES(callee, "trivial inline");
            }
        }
        return shouldInlineInternal(caller, callee, state, inlinedSizeEstimate);
    }

    /**
   * Children must implement this method.
   * It contains the non-generic decision making portion of the oracle.
   */
    protected abstract OPT_InlineDecision shouldInlineInternal(VM_Method caller, VM_Method callee, OPT_CompilationState state, int inlinedSizeEstimate);
}
