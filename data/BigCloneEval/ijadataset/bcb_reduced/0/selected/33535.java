package org.jikesrvm.ia32;

import org.jikesrvm.ArchitectureSpecific;
import org.jikesrvm.VM;
import org.jikesrvm.classloader.VM_Method;
import org.jikesrvm.compilers.common.assembler.ia32.VM_Assembler;
import org.jikesrvm.objectmodel.VM_ObjectModel;
import org.jikesrvm.runtime.VM_ArchEntrypoints;
import org.vmmagic.unboxed.Offset;

/**
 * An interface conflict resolution stub uses a hidden parameter to
 * distinguish among multiple interface methods of a class that map to
 * the same slot in the class's IMT. </p>
 *
 * <p><STRONG>Assumption:</STRONG>
 * Register EAX contains the "this" parameter of the
 * method being called invoked.
 *
 * <p><STRONG>Assumption:</STRONG>
 * Register ECX is available as a scratch register (we need one!)
 */
public abstract class VM_InterfaceMethodConflictResolver implements VM_RegisterConstants {

    public static ArchitectureSpecific.VM_CodeArray createStub(int[] sigIds, VM_Method[] targets) {
        int numEntries = sigIds.length;
        VM_Assembler asm = new ArchitectureSpecific.VM_Assembler(numEntries);
        if (VM.VerifyAssertions) {
            for (int i = 1; i < sigIds.length; i++) {
                VM._assert(sigIds[i - 1] < sigIds[i]);
            }
        }
        int[] bcIndices = new int[numEntries];
        assignBytecodeIndices(0, bcIndices, 0, numEntries - 1);
        insertStubPrologue(asm);
        insertStubCase(asm, sigIds, targets, bcIndices, 0, numEntries - 1);
        return asm.getMachineCodes();
    }

    private static int assignBytecodeIndices(int bcIndex, int[] bcIndices, int low, int high) {
        int middle = (high + low) / 2;
        bcIndices[middle] = bcIndex++;
        if (low == middle && middle == high) {
            return bcIndex;
        } else {
            if (low < middle) {
                bcIndex = assignBytecodeIndices(bcIndex, bcIndices, low, middle - 1);
            }
            if (middle < high) {
                bcIndex = assignBytecodeIndices(bcIndex, bcIndices, middle + 1, high);
            }
            return bcIndex;
        }
    }

    private static void insertStubPrologue(VM_Assembler asm) {
        VM_ObjectModel.baselineEmitLoadTIB((ArchitectureSpecific.VM_Assembler) asm, ECX.value(), EAX.value());
    }

    private static void insertStubCase(VM_Assembler asm, int[] sigIds, VM_Method[] targets, int[] bcIndices, int low, int high) {
        int middle = (high + low) / 2;
        asm.resolveForwardReferences(bcIndices[middle]);
        if (low == middle && middle == high) {
            VM_Method target = targets[middle];
            if (target.isStatic()) {
                VM_ProcessorLocalState.emitMoveFieldToReg(asm, ECX, VM_ArchEntrypoints.jtocField.getOffset());
            }
            asm.emitJMP_RegDisp(ECX, target.getOffset());
        } else {
            Offset disp = VM_ArchEntrypoints.hiddenSignatureIdField.getOffset();
            VM_ProcessorLocalState.emitCompareFieldWithImm(asm, disp, sigIds[middle]);
            if (low < middle) {
                asm.emitJCC_Cond_Label(VM_Assembler.LT, bcIndices[(low + middle - 1) / 2]);
            }
            if (middle < high) {
                asm.emitJCC_Cond_Label(VM_Assembler.GT, bcIndices[(middle + 1 + high) / 2]);
            }
            VM_Method target = targets[middle];
            if (target.isStatic()) {
                VM_ProcessorLocalState.emitMoveFieldToReg(asm, ECX, VM_ArchEntrypoints.jtocField.getOffset());
            }
            asm.emitJMP_RegDisp(ECX, target.getOffset());
            if (low < middle) {
                insertStubCase(asm, sigIds, targets, bcIndices, low, middle - 1);
            }
            if (middle < high) {
                insertStubCase(asm, sigIds, targets, bcIndices, middle + 1, high);
            }
        }
    }
}
