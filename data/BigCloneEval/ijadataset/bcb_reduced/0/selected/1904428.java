package org.jikesrvm.ia32;

import org.jikesrvm.ArchitectureSpecific;
import org.jikesrvm.VM;
import org.jikesrvm.classloader.RVMMethod;
import org.jikesrvm.compilers.common.assembler.ia32.Assembler;
import org.jikesrvm.objectmodel.ObjectModel;
import org.jikesrvm.runtime.ArchEntrypoints;
import org.jikesrvm.runtime.Magic;
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
public abstract class InterfaceMethodConflictResolver implements RegisterConstants {

    public static ArchitectureSpecific.CodeArray createStub(int[] sigIds, RVMMethod[] targets) {
        int numEntries = sigIds.length;
        Assembler asm = new ArchitectureSpecific.Assembler(numEntries);
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

    private static void insertStubPrologue(Assembler asm) {
        ObjectModel.baselineEmitLoadTIB((ArchitectureSpecific.Assembler) asm, ECX.value(), EAX.value());
    }

    private static void insertStubCase(Assembler asm, int[] sigIds, RVMMethod[] targets, int[] bcIndices, int low, int high) {
        int middle = (high + low) / 2;
        asm.resolveForwardReferences(bcIndices[middle]);
        if (low == middle && middle == high) {
            RVMMethod target = targets[middle];
            if (target.isStatic()) {
                asm.emitJMP_Abs(Magic.getTocPointer().plus(target.getOffset()));
            } else {
                asm.emitJMP_RegDisp(ECX, target.getOffset());
            }
        } else {
            Offset disp = ArchEntrypoints.hiddenSignatureIdField.getOffset();
            ThreadLocalState.emitCompareFieldWithImm(asm, disp, sigIds[middle]);
            if (low < middle) {
                asm.emitJCC_Cond_Label(Assembler.LT, bcIndices[(low + middle - 1) / 2]);
            }
            if (middle < high) {
                asm.emitJCC_Cond_Label(Assembler.GT, bcIndices[(middle + 1 + high) / 2]);
            }
            RVMMethod target = targets[middle];
            if (target.isStatic()) {
                asm.emitJMP_Abs(Magic.getTocPointer().plus(target.getOffset()));
            } else {
                asm.emitJMP_RegDisp(ECX, target.getOffset());
            }
            if (low < middle) {
                insertStubCase(asm, sigIds, targets, bcIndices, low, middle - 1);
            }
            if (middle < high) {
                insertStubCase(asm, sigIds, targets, bcIndices, middle + 1, high);
            }
        }
    }
}
