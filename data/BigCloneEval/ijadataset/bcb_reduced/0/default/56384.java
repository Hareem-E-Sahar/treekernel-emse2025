import java.util.*;
import instructionFormats.*;

/**
 * Transformations that use escape analysis.
 * <ul> 
 *  <li> 1. synchronization removal
 *  <li> 2. scalar replacement of aggregates and short arrays
 * </ul>
 *
 * @author Stephen Fink
 *
 */
class OPT_EscapeTransformations extends OPT_CompilerPhase implements OPT_Operators {

    final boolean shouldPerform(OPT_Options options) {
        return options.MONITOR_REMOVAL || options.SCALAR_REPLACE_AGGREGATES;
    }

    final String getName() {
        return "Escape Transformations";
    }

    final boolean printingEnabled(OPT_Options options, boolean before) {
        return false;
    }

    /** 
   * Perform the transformations
   *
   * @param ir IR for the target method
   */
    public void perform(OPT_IR ir) {
        OPT_DefUse.computeDU(ir);
        OPT_DefUse.recomputeSSA(ir);
        OPT_SimpleEscape analyzer = new OPT_SimpleEscape();
        OPT_FI_EscapeSummary summary = analyzer.simpleEscapeAnalysis(ir);
        for (OPT_Register reg = ir.regpool.getFirstRegister(); reg != null; reg = reg.getNext()) {
            if (!reg.isSSA()) continue;
            if (reg.defList == null) continue;
            OPT_Instruction def = reg.defList.instruction;
            if (ir.options.SCALAR_REPLACE_AGGREGATES && summary.isMethodLocal(reg)) {
                OPT_AggregateReplacer s = null;
                if ((def.getOpcode() == NEW_opcode) || (def.getOpcode() == NEWARRAY_opcode)) {
                    s = getAggregateReplacer(def, ir);
                }
                if (s != null) {
                    s.transform();
                }
            }
            if (ir.options.MONITOR_REMOVAL && summary.isThreadLocal(reg)) {
                OPT_UnsyncReplacer unsync = null;
                if ((def.getOpcode() == NEW_opcode) || (def.getOpcode() == NEWARRAY_opcode)) {
                    unsync = getUnsyncReplacer(reg, def, ir);
                }
                if (unsync != null) {
                    unsync.transform();
                }
            }
        }
    }

    /** 
   * Generate an object which transforms defs & uses of "synchronized"
   * objects to defs & uses of "unsynchronized" objects
   *
   * <p> PRECONDITION: objects pointed to by reg do NOT escape
   *
   * @param reg the pointer whose defs and uses should be transformed
   * @param inst the allocation site
   * @param ir controlling ir
   * @return an OPT_UnsyncReplacer specialized to the allocation site,
   *		null if no legal transformation found
   */
    private OPT_UnsyncReplacer getUnsyncReplacer(OPT_Register reg, OPT_Instruction inst, OPT_IR ir) {
        if (!synchronizesOn(ir, reg)) return null;
        return OPT_UnsyncReplacer.getReplacer(inst, ir);
    }

    /**
   * Is there an instruction in this IR which causes synchronization
   * on an object pointed to by a particular register?
   * PRECONDITION: register lists computed and valid
   */
    private static boolean synchronizesOn(OPT_IR ir, OPT_Register r) {
        for (OPT_RegisterOperand use = r.useList; use != null; use = (OPT_RegisterOperand) use.getNext()) {
            OPT_Instruction s = use.instruction;
            if (s.operator == MONITORENTER) return true;
            if (s.operator == MONITOREXIT) return true;
            if (Call.conforms(s)) {
                VM_Method m = Call.getMethod(s).method;
                if (!m.isStatic()) {
                    OPT_RegisterOperand invokee = Call.getParam(s, 0).asRegister();
                    if (invokee == use) {
                        if (m.isSynchronized()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /** 
   * Generate an object which will perform scalar replacement of
   * an aggregate allocated by a given instruction
   *
   * <p> PRECONDITION: objects returned by this allocation site do NOT escape
   * 		     the current method
   * 
   * @param inst the allocation site
   * @param ir controlling ir
   * @return an OPT_AggregateReplacer specialized to the allocation site,
   *		null if no legal transformation found
   */
    private OPT_AggregateReplacer getAggregateReplacer(OPT_Instruction inst, OPT_IR ir) {
        OPT_Options options = ir.options;
        VM_Type t = null;
        if (inst.getOpcode() == NEW_opcode) t = New.getType(inst).type; else if (inst.getOpcode() == NEWARRAY_opcode) {
            t = NewArray.getType(inst).type;
        } else throw new OPT_OptimizingCompilerException("Logic Error in OPT_EscapeTransformations", true);
        OPT_AggregateReplacer s = null;
        if (t.isClassType() && options.SCALAR_REPLACE_AGGREGATES) {
            return OPT_ObjectReplacer.getReplacer(inst, ir);
        }
        if (t.isArrayType() && options.SCALAR_REPLACE_AGGREGATES) {
            return OPT_ShortArrayReplacer.getReplacer(inst, ir);
        }
        return null;
    }
}
