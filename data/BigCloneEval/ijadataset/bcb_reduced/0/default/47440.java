/**
 * Iterator for stack frame  built by the Baseline compiler
 * An Instance of this class will iterate through a particular 
 * reference map of a method returning the offsets of any refereces
 * that are part of the input parameters, local variables, and 
 * java stack for the stack frame.
 *
 * @author Bowen Alpern
 * @author Maria Butrico
 * @author Anthony Cocchi
 */
final class VM_BaselineGCMapIterator extends VM_GCMapIterator implements VM_BaselineConstants {

    private static final boolean TRACE_ALL = false;

    private static final boolean TRACE_DL = false;

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
        if (VM.TraceStkMaps || TRACE_ALL) {
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
        bridgeSpilledParamLocation = 0;
        if (currentMethod.getDeclaringClass().isDynamicBridge()) {
            int ip = VM_Magic.getReturnAddress(fp);
            fp = VM_Magic.getCallerFramePointer(fp);
            int callingCompiledMethodId = VM_Magic.getCompiledMethodID(fp);
            VM_CompiledMethod callingCompiledMethod = VM_ClassLoader.getCompiledMethod(callingCompiledMethodId);
            VM_CompilerInfo callingCompilerInfo = callingCompiledMethod.getCompilerInfo();
            int callingInstructionOffset = ip - VM_Magic.objectAsAddress(callingCompiledMethod.getInstructions());
            callingCompilerInfo.getDynamicLink(dynamicLink, callingInstructionOffset);
            bridgeTarget = dynamicLink.methodRef();
            bridgeParameterTypes = bridgeTarget.getParameterTypes();
            if (dynamicLink.isInvokedWithImplicitThisParameter()) {
                bridgeParameterInitialIndex = -1;
                bridgeSpilledParamInitialOffset = 8;
            } else {
                bridgeParameterInitialIndex = 0;
                bridgeSpilledParamInitialOffset = 4;
            }
            bridgeSpilledParamInitialOffset += (4 * bridgeTarget.getParameterWords());
            if (callingCompilerInfo.getCompilerType() == VM_CompilerInfo.BASELINE) {
                bridgeSpilledParameterMappingRequired = false;
            } else {
                bridgeSpilledParameterMappingRequired = true;
            }
        }
        reset();
    }

    void reset() {
        mapOffset = 0;
        if (bridgeTarget != null) {
            bridgeParameterMappingRequired = true;
            bridgeParameterIndex = bridgeParameterInitialIndex;
            bridgeRegisterIndex = 0;
            bridgeRegisterLocation = framePtr + STACKFRAME_FIRST_PARAMETER_OFFSET;
            bridgeSpilledParamLocation = framePtr + bridgeSpilledParamInitialOffset;
        }
    }

    int getNextReferenceAddress() {
        if (mapId < 0) {
            mapOffset = maps.getNextJSRRef(mapOffset);
        } else {
            mapOffset = maps.getNextRef(mapOffset, mapId);
        }
        if (VM.TraceStkMaps || TRACE_ALL) {
            VM.sysWrite("VM_BaselineGCMapIterator getNextReferenceOffset = ");
            VM.sysWriteHex(mapOffset);
            VM.sysWrite(".\n");
            VM.sysWrite("Reference is ");
            VM.sysWriteHex(VM_Magic.getMemoryWord(framePtr + mapOffset));
            VM.sysWrite(".\n");
            if (mapId < 0) VM.sysWrite("Offset is a JSR return address ie internal pointer.\n");
        }
        if (mapOffset != 0) {
            if (bridgeParameterMappingRequired) return (framePtr + mapOffset - BRIDGE_FRAME_EXTRA_SIZE); else return (framePtr + mapOffset);
        } else if (bridgeParameterMappingRequired) {
            if (VM.TraceStkMaps || TRACE_ALL || TRACE_DL) {
                VM.sysWrite("getNextReferenceAddress: bridgeTarget=");
                VM.sysWrite(bridgeTarget);
                VM.sysWrite("\n");
            }
            if (!bridgeRegistersLocationUpdated) {
                registerLocations[JTOC] = framePtr + JTOC_SAVE_OFFSET;
                registerLocations[T0] = framePtr + T0_SAVE_OFFSET;
                registerLocations[T1] = framePtr + T1_SAVE_OFFSET;
                registerLocations[EBX] = framePtr + EBX_SAVE_OFFSET;
                bridgeRegistersLocationUpdated = true;
            }
            if (bridgeParameterIndex == -1) {
                bridgeParameterIndex += 1;
                bridgeRegisterIndex += 1;
                bridgeRegisterLocation -= 4;
                bridgeSpilledParamLocation -= 4;
                if (VM.TraceStkMaps || TRACE_ALL || TRACE_DL) {
                    VM.sysWrite("VM_BaselineGCMapIterator getNextReferenceOffset = dynamic link GPR this ");
                    VM.sysWriteHex(bridgeRegisterLocation + 4);
                    VM.sysWrite(".\n");
                }
                return bridgeRegisterLocation + 4;
            }
            while (bridgeParameterIndex < bridgeParameterTypes.length) {
                VM_Type bridgeParameterType = bridgeParameterTypes[bridgeParameterIndex++];
                if (bridgeParameterType.isReferenceType()) {
                    bridgeRegisterIndex += 1;
                    bridgeRegisterLocation -= 4;
                    bridgeSpilledParamLocation -= 4;
                    if (bridgeRegisterIndex <= NUM_PARAMETER_GPRS) {
                        if (VM.TraceStkMaps || TRACE_ALL || TRACE_DL) {
                            VM.sysWrite("VM_BaselineGCMapIterator getNextReferenceOffset = dynamic link GPR parameter ");
                            VM.sysWriteHex(bridgeRegisterLocation + 4);
                            VM.sysWrite(".\n");
                        }
                        return bridgeRegisterLocation + 4;
                    } else {
                        if (bridgeSpilledParameterMappingRequired) {
                            if (VM.TraceStkMaps || TRACE_ALL || TRACE_DL) {
                                VM.sysWrite("VM_BaselineGCMapIterator getNextReferenceOffset = dynamic link spilled parameter ");
                                VM.sysWriteHex(bridgeSpilledParamLocation + 4);
                                VM.sysWrite(".\n");
                            }
                            return bridgeSpilledParamLocation + 4;
                        } else {
                            break;
                        }
                    }
                } else if (bridgeParameterType.isLongType()) {
                    bridgeRegisterIndex += 2;
                    bridgeRegisterLocation -= 8;
                    bridgeSpilledParamLocation -= 8;
                } else if (bridgeParameterType.isDoubleType()) {
                    bridgeSpilledParamLocation -= 8;
                } else if (bridgeParameterType.isFloatType()) {
                    bridgeSpilledParamLocation -= 4;
                } else {
                    bridgeRegisterIndex += 1;
                    bridgeRegisterLocation -= 4;
                    bridgeSpilledParamLocation -= 4;
                }
            }
        } else {
            registerLocations[JTOC] = framePtr + JTOC_SAVE_OFFSET;
        }
        return 0;
    }

    int getNextReturnAddressAddress() {
        if (mapId >= 0) {
            if (VM.TraceStkMaps || TRACE_ALL) {
                VM.sysWrite("VM_BaselineGCMapIterator getNextReturnAddressOffset mapId = ");
                VM.sysWrite(mapId);
                VM.sysWrite(".\n");
            }
            return 0;
        }
        mapOffset = maps.getNextJSRReturnAddr(mapOffset);
        if (VM.TraceStkMaps || TRACE_ALL) {
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

    private boolean bridgeSpilledParameterMappingRequired;

    private boolean bridgeRegistersLocationUpdated;

    private int bridgeParameterInitialIndex;

    private int bridgeParameterIndex;

    private int bridgeRegisterIndex;

    private int bridgeRegisterLocation;

    private int bridgeSpilledParamLocation;

    private int bridgeSpilledParamInitialOffset;
}
