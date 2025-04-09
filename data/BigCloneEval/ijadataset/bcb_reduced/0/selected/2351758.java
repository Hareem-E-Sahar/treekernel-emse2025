package org.jikesrvm.ppc;

import org.jikesrvm.ArchitectureSpecific;
import org.jikesrvm.VM;
import org.jikesrvm.classloader.VM_Method;
import org.jikesrvm.compilers.common.assembler.ppc.VM_Assembler;
import org.jikesrvm.compilers.common.assembler.ppc.VM_AssemblerConstants;
import org.jikesrvm.objectmodel.VM_ObjectModel;
import org.jikesrvm.runtime.VM_Magic;
import org.jikesrvm.runtime.VM_Memory;

/**
 * Generates a custom IMT-conflict resolution stub.
 * We create a binary search tree.
 */
public abstract class VM_InterfaceMethodConflictResolver implements VM_BaselineConstants, VM_AssemblerConstants {

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
        ArchitectureSpecific.VM_CodeArray stub = asm.makeMachineCode().getInstructions();
        if (VM.runningVM) VM_Memory.sync(VM_Magic.objectAsAddress(stub), stub.length() << LG_INSTRUCTION_WIDTH);
        return stub;
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
        VM_ObjectModel.baselineEmitLoadTIB((ArchitectureSpecific.VM_Assembler) asm, S0, T0);
    }

    private static void insertStubCase(VM_Assembler asm, int[] sigIds, VM_Method[] targets, int[] bcIndices, int low, int high) {
        int middle = (high + low) / 2;
        asm.resolveForwardReferences(bcIndices[middle]);
        if (low == middle && middle == high) {
            VM_Method target = targets[middle];
            if (target.isStatic()) {
                asm.emitLAddrToc(S0, target.getOffset());
            } else {
                asm.emitLAddrOffset(S0, S0, target.getOffset());
            }
            asm.emitMTCTR(S0);
            asm.emitBCCTR();
        } else {
            asm.emitCMPI(S1, sigIds[middle]);
            if (low < middle) {
                asm.emitShortBC(LT, 0, bcIndices[(low + middle - 1) / 2]);
            }
            if (middle < high) {
                asm.emitShortBC(GT, 0, bcIndices[(middle + 1 + high) / 2]);
            }
            VM_Method target = targets[middle];
            if (target.isStatic()) {
                asm.emitLAddrToc(S0, target.getOffset());
            } else {
                asm.emitLAddrOffset(S0, S0, target.getOffset());
            }
            asm.emitMTCTR(S0);
            asm.emitBCCTR();
            if (low < middle) {
                insertStubCase(asm, sigIds, targets, bcIndices, low, middle - 1);
            }
            if (middle < high) {
                insertStubCase(asm, sigIds, targets, bcIndices, middle + 1, high);
            }
        }
    }
}
