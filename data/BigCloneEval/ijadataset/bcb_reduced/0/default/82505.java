final class VM_BaselineGCMapIterator extends VM_GCMapIterator implements VM_BaselineConstants {

    VM_BaselineGCMapIterator(int registerLocations[]) {
        this.registerLocations = registerLocations;
        dynamicLink = new VM_DynamicLink();
    }

    void setupIterator(VM_CompiledMethod compiledMethod, int instructionOffset, int fp) {
        currentMethod = compiledMethod.getMethod();
        framePtr = fp;
        maps = ((VM_BaselineCompilerInfo) compiledMethod.getCompilerInfo()).referenceMaps;
        mapId = maps.locateGCPoint(instructionOffset, currentMethod);
        mapOffset = 0;
        if (mapId < 0) {
            VM_ReferenceMaps.jsrLock.lock();
            maps.setupJSRSubroutineMap(framePtr, mapId, compiledMethod);
        }
        if (VM.TraceStkMaps) {
            VM.sysWrite("VM_BaselineGCMapIterator setupIterator mapId = ");
            VM.sysWrite(mapId);
            VM.sysWrite(".\n");
        }
        bridgeTarget = null;
        bridgeParameterTypes = null;
        bridgeParameterMappingRequired = false;
        bridgeRegistersLocationUpdated = false;
        bridgeParameterIndex = 0;
        bridgeRegisterIndex = 0;
        bridgeRegisterLocation = 0;
        if (currentMethod.getDeclaringClass().isDynamicBridge()) {
            fp = VM_Magic.getCallerFramePointer(fp);
            int ip = VM_Magic.getNextInstructionAddress(fp);
            int callingCompiledMethodId = VM_Magic.getCompiledMethodID(fp);
            VM_CompiledMethod callingCompiledMethod = VM_ClassLoader.getCompiledMethod(callingCompiledMethodId);
            VM_CompilerInfo callingCompilerInfo = callingCompiledMethod.getCompilerInfo();
            int callingInstructionOffset = ip - VM_Magic.objectAsAddress(callingCompiledMethod.getInstructions());
            callingCompilerInfo.getDynamicLink(dynamicLink, callingInstructionOffset);
            bridgeTarget = dynamicLink.methodRef();
            bridgeParameterInitialIndex = dynamicLink.isInvokedWithImplicitThisParameter() ? -1 : 0;
            bridgeParameterTypes = bridgeTarget.getParameterTypes();
        }
        reset();
    }

    void reset() {
        mapOffset = 0;
        if (bridgeTarget != null) {
            bridgeParameterMappingRequired = true;
            bridgeParameterIndex = bridgeParameterInitialIndex;
            bridgeRegisterIndex = FIRST_VOLATILE_GPR;
            bridgeRegisterLocation = VM_Magic.getMemoryWord(framePtr) - (LAST_NONVOLATILE_FPR - FIRST_VOLATILE_FPR + 1) * 8 - (LAST_NONVOLATILE_GPR - FIRST_VOLATILE_GPR + 1) * 4;
        }
    }

    int getNextReferenceAddress() {
        if (mapId < 0) mapOffset = maps.getNextJSRRef(mapOffset); else mapOffset = maps.getNextRef(mapOffset, mapId);
        if (VM.TraceStkMaps) {
            VM.sysWrite("VM_BaselineGCMapIterator getNextReferenceOffset = ");
            VM.sysWrite(mapOffset);
            VM.sysWrite(".\n");
            if (mapId < 0) VM.sysWrite("Offset is a JSR return address ie internal pointer.\n");
        }
        if (mapOffset != 0) return (framePtr + mapOffset); else if (bridgeParameterMappingRequired) {
            if (!bridgeRegistersLocationUpdated) {
                int location = framePtr + VM_Compiler.getFrameSize(currentMethod);
                location -= (LAST_NONVOLATILE_FPR - FIRST_VOLATILE_FPR + 1) * 8;
                for (int i = LAST_NONVOLATILE_GPR; i >= FIRST_VOLATILE_GPR; --i) registerLocations[i] = location -= 4;
                bridgeRegistersLocationUpdated = true;
            }
            if (bridgeParameterIndex == -1) {
                bridgeParameterIndex += 1;
                bridgeRegisterIndex += 1;
                bridgeRegisterLocation += 4;
                return bridgeRegisterLocation - 4;
            }
            for (; ; ) {
                if (bridgeParameterIndex == bridgeParameterTypes.length || bridgeRegisterIndex > LAST_VOLATILE_GPR) {
                    bridgeParameterMappingRequired = false;
                    break;
                }
                VM_Type bridgeParameterType = bridgeParameterTypes[bridgeParameterIndex++];
                if (bridgeParameterType.isReferenceType()) {
                    bridgeRegisterIndex += 1;
                    bridgeRegisterLocation += 4;
                    return bridgeRegisterLocation - 4;
                } else if (bridgeParameterType.isLongType()) {
                    bridgeRegisterIndex += 2;
                    bridgeRegisterLocation += 8;
                } else if (bridgeParameterType.isDoubleType() || bridgeParameterType.isFloatType()) {
                } else {
                    bridgeRegisterIndex += 1;
                    bridgeRegisterLocation += 4;
                }
            }
        }
        return 0;
    }

    int getNextReturnAddressAddress() {
        if (mapId >= 0) {
            if (VM.TraceStkMaps) {
                VM.sysWrite("VM_BaselineGCMapIterator getNextReturnAddressOffset mapId = ");
                VM.sysWrite(mapId);
                VM.sysWrite(".\n");
            }
            return 0;
        }
        mapOffset = maps.getNextJSRReturnAddr(mapOffset);
        if (VM.TraceStkMaps) {
            VM.sysWrite("VM_BaselineGCMapIterator getNextReturnAddressOffset = ");
            VM.sysWrite(mapOffset);
            VM.sysWrite(".\n");
        }
        return (mapOffset == 0) ? 0 : (framePtr + mapOffset);
    }

    void cleanupPointers() {
        maps.cleanupPointers();
        maps = null;
        if (mapId < 0) VM_ReferenceMaps.jsrLock.unlock();
        bridgeTarget = null;
        bridgeParameterTypes = null;
    }

    int getType() {
        return VM_GCMapIterator.BASELINE;
    }

    int getStackDepth() {
        return maps.getStackDepth(mapId);
    }

    private int mapOffset;

    private int mapId;

    private VM_ReferenceMaps maps;

    private VM_DynamicLink dynamicLink;

    private VM_Method bridgeTarget;

    private VM_Method currentMethod;

    private VM_Type[] bridgeParameterTypes;

    private boolean bridgeParameterMappingRequired;

    private boolean bridgeRegistersLocationUpdated;

    private int bridgeParameterInitialIndex;

    private int bridgeParameterIndex;

    private int bridgeRegisterIndex;

    private int bridgeRegisterLocation;
}
