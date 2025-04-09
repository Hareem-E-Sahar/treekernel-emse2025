/**
  * This class implements the OPT_InlineOracle interface with a
  * profile-directed inlining strategy.
  *
  * @author Stephen Fink
  * @author Dave Grove
  * @modified Michael Hind
  */
class OPT_ProfileDirectedInlineOracle extends OPT_GenericInlineOracle {

    public OPT_InlineDecision shouldInline(OPT_CompilationState state) {
        OPT_Options opts = state.getOptions();
        if (!opts.INLINE) return OPT_InlineDecision.NO("inlining not enabled");
        OPT_InlineDecision d = staticOracle.shouldInline(state);
        if (!d.isNO()) return d;
        VM_Method caller = state.getMethod();
        int bcX = state.getBytecodeIndex();
        VM_Method[] targets = plan.getTargets(caller, bcX);
        if (targets == null) {
            return d;
        } else if (targets.length == 1) {
            VM_Method callee = targets[0];
            VM_Method computedTarget = state.getComputedTarget();
            if (computedTarget != null && callee != computedTarget) {
                recordRefusalToInlineHotEdge(state.getCompiledMethodId(), caller, bcX, callee);
                return OPT_InlineDecision.NO("AI: mismatch between computed target and profile data");
            }
            if (!viableCandidate(caller, callee, state)) {
                recordRefusalToInlineHotEdge(state.getCompiledMethodId(), caller, bcX, callee);
                return OPT_InlineDecision.NO("AI: candidate judged to be nonviable");
            }
            if (computedTarget != null) {
                return OPT_InlineDecision.YES(computedTarget, "AI: hot edge matches computed target");
            }
            VM_Method staticCallee = state.obtainTarget();
            if (candidateNeedsGuard(caller, staticCallee, state)) {
                if (opts.GUARDED_INLINE) {
                    boolean codePatch = opts.guardWithCodePatch() && !state.isInvokeInterface() && isCurrentlyFinal(staticCallee, true);
                    byte guard = chooseGuard(caller, staticCallee, state, codePatch);
                    if (guard == OPT_Options.IG_METHOD_TEST) {
                        guard = chooseGuard(caller, callee, state, false);
                    }
                    return OPT_InlineDecision.guardedYES(callee, guard, "AI: guarded inline of hot edge");
                } else {
                    recordRefusalToInlineHotEdge(state.getCompiledMethodId(), caller, bcX, callee);
                    return OPT_InlineDecision.NO("AI: guarded inlining disabled");
                }
            } else {
                return OPT_InlineDecision.YES(callee, "AI: hot edge");
            }
        } else {
            VM_Method computedTarget = state.getComputedTarget();
            if (computedTarget != null) {
                for (int i = 0; i < targets.length; i++) {
                    if (targets[i] == computedTarget) {
                        if (viableCandidate(caller, targets[i], state)) {
                            return OPT_InlineDecision.YES(computedTarget, "AI: hot edge matches computed target");
                        }
                    }
                }
                for (int i = 0; i < targets.length; i++) {
                    recordRefusalToInlineHotEdge(state.getCompiledMethodId(), caller, bcX, targets[i]);
                }
                return OPT_InlineDecision.NO("AI: multiple hot edges, but none match computed target");
            } else {
                if (!opts.GUARDED_INLINE) return OPT_InlineDecision.NO("AI: guarded inlining disabled");
                int viable = 0;
                for (int i = 0; i < targets.length; i++) {
                    if (viableCandidate(caller, targets[i], state)) {
                        viable++;
                    } else {
                        recordRefusalToInlineHotEdge(state.getCompiledMethodId(), caller, bcX, targets[i]);
                        targets[i] = null;
                    }
                }
                if (viable > 0) {
                    VM_Method[] viableTargets = new VM_Method[viable];
                    byte[] guards = new byte[viable];
                    viable = 0;
                    for (int i = 0; i < targets.length; i++) {
                        if (targets[i] != null) {
                            viableTargets[viable] = targets[i];
                            guards[viable++] = chooseGuard(caller, targets[i], state, false);
                        }
                    }
                    return OPT_InlineDecision.guardedYES(viableTargets, guards, "AI: viable hot edge(s) found");
                } else {
                    return OPT_InlineDecision.NO("AI: all candidates judged to be nonviable");
                }
            }
        }
    }

    protected boolean viableCandidate(VM_Method caller, VM_Method callee, OPT_CompilationState state) {
        if (!legalToInline(caller, callee)) return false;
        OPT_InlineSequence seq = state.getSequence();
        if (seq.containsMethod(callee)) return false;
        if (hasInlinePragma(callee, state)) return true;
        if (hasNoInlinePragma(callee, state)) return false;
        int inlinedSizeEstimate = inlinedSizeEstimate(callee, state);
        if (inlinedSizeEstimate > state.getOptions().AI_MAX_TARGET_SIZE) return false;
        int totalMCGenerated = state.getMCSizeEstimate();
        if (totalMCGenerated + inlinedSizeEstimate - VM_OptMethodSummary.CALL_COST > getMaxRootSize(state)) return false;
        return true;
    }

    protected boolean candidateNeedsGuard(VM_Method caller, VM_Method callee, OPT_CompilationState state) {
        if (state.isInvokeInterface()) return true;
        if (needsGuard(callee)) {
            if (state.getIsExtant() && state.getOptions().PREEX_INLINE) {
                if (isCurrentlyFinal(callee, true)) {
                    if (OPT_ClassLoadingDependencyManager.TRACE || OPT_ClassLoadingDependencyManager.DEBUG) {
                        VM_Class.OptCLDepManager.report("PREEX_INLINE: Inlined " + callee + " into " + caller + "\n");
                    }
                    VM_Class.OptCLDepManager.addNotOverriddenDependency(callee, state.getCompiledMethodId());
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    protected OPT_InlineDecision shouldInlineInternal(VM_Method caller, VM_Method callee, OPT_CompilationState state, int inlinedSizeEstimate) {
        OPT_OptimizingCompilerException.UNREACHABLE();
        return null;
    }

    protected OPT_InlineDecision shouldInlineInterfaceInternal(VM_Method caller, VM_Method callee, OPT_CompilationState state) {
        OPT_OptimizingCompilerException.UNREACHABLE();
        return null;
    }

    protected void recordRefusalToInlineHotEdge(int cmid, VM_Method caller, int bcX, VM_Method callee) {
    }

    protected OPT_InlinePlan plan;

    protected OPT_StaticInlineOracle staticOracle = new OPT_StaticInlineOracle();

    /** 
   * construct an oracle that interfaces to a plan 
   */
    OPT_ProfileDirectedInlineOracle(OPT_InlinePlan plan) {
        this.plan = plan;
    }

    /**
   * Return the upper limit on the machine code instructions for the 
   * root method.
   * @param state compilation state
   */
    protected int getMaxRootSize(OPT_CompilationState state) {
        OPT_Options opts = state.getOptions();
        int rootSize = VM_OptMethodSummary.inlinedSizeEstimate(state.getRootMethod());
        return Math.min(opts.AI_MAX_INLINE_EXPANSION_FACTOR * rootSize, opts.AI_MAX_METHOD_SIZE + rootSize);
    }
}
