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
final class VM_BaselineGCMapIterator extends VM_GCMapIterator implements VM_BaselineConstants, VM_Uninterruptible {

    private static final boolean TRACE_ALL = false;

    private static final boolean TRACE_DL = false;

    VM_BaselineGCMapIterator(int registerLocations[]) {
        this.registerLocations = registerLocations;
        dynamicLink = new VM_DynamicLink();
    }

    void setupIterator(VM_CompiledMethod compiledMethod, int instructionOffset, VM_Address fp) {
        currentMethod = compiledMethod.getMethod();
        framePtr = fp;
        maps = ((VM_BaselineCompiledMethod) compiledMethod).referenceMaps;
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
        bridgeRegisterLocation = VM_Address.zero();
        bridgeSpilledParamLocation = VM_Address.zero();
        if (currentMethod.getDeclaringClass().isDynamicBridge()) {
            VM_Address ip = VM_Magic.getReturnAddress(fp);
            fp = VM_Magic.getCallerFramePointer(fp);
            int callingCompiledMethodId = VM_Magic.getCompiledMethodID(fp);
            VM_CompiledMethod callingCompiledMethod = VM_CompiledMethods.getCompiledMethod(callingCompiledMethodId);
            int callingInstructionOffset = ip.diff(VM_Magic.objectAsAddress(callingCompiledMethod.getInstructions()));
            callingCompiledMethod.getDynamicLink(dynamicLink, callingInstructionOffset);
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
            if (callingCompiledMethod.getCompilerType() == VM_CompiledMethod.BASELINE) {
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
            bridgeRegisterLocation = framePtr.add(STACKFRAME_FIRST_PARAMETER_OFFSET);
            bridgeSpilledParamLocation = framePtr.add(bridgeSpilledParamInitialOffset);
        }
    }

    VM_Address getNextReferenceAddress() {
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
            VM.sysWriteHex(VM_Magic.getMemoryWord(framePtr.add(mapOffset)));
            VM.sysWrite(".\n");
            if (mapId < 0) VM.sysWrite("Offset is a JSR return address ie internal pointer.\n");
        }
        if (mapOffset != 0) {
            if (bridgeParameterMappingRequired) return (framePtr.add(mapOffset - BRIDGE_FRAME_EXTRA_SIZE)); else return (framePtr.add(mapOffset));
        } else if (bridgeParameterMappingRequired) {
            if (VM.TraceStkMaps || TRACE_ALL || TRACE_DL) {
                VM.sysWrite("getNextReferenceAddress: bridgeTarget=");
                VM.sysWrite(bridgeTarget);
                VM.sysWrite("\n");
            }
            if (!bridgeRegistersLocationUpdated) {
                registerLocations[JTOC] = framePtr.add(JTOC_SAVE_OFFSET).toInt();
                registerLocations[T0] = framePtr.add(T0_SAVE_OFFSET).toInt();
                registerLocations[T1] = framePtr.add(T1_SAVE_OFFSET).toInt();
                registerLocations[EBX] = framePtr.add(EBX_SAVE_OFFSET).toInt();
                bridgeRegistersLocationUpdated = true;
            }
            if (bridgeParameterIndex == -1) {
                bridgeParameterIndex += 1;
                bridgeRegisterIndex += 1;
                bridgeRegisterLocation = bridgeRegisterLocation.sub(4);
                bridgeSpilledParamLocation = bridgeSpilledParamLocation.sub(4);
                if (VM.TraceStkMaps || TRACE_ALL || TRACE_DL) {
                    VM.sysWrite("VM_BaselineGCMapIterator getNextReferenceOffset = dynamic link GPR this ");
                    VM.sysWrite(bridgeRegisterLocation.add(4));
                    VM.sysWrite(".\n");
                }
                return bridgeRegisterLocation.add(4);
            }
            while (bridgeParameterIndex < bridgeParameterTypes.length) {
                VM_Type bridgeParameterType = bridgeParameterTypes[bridgeParameterIndex++];
                if (bridgeParameterType.isReferenceType()) {
                    bridgeRegisterIndex += 1;
                    bridgeRegisterLocation = bridgeRegisterLocation.sub(4);
                    bridgeSpilledParamLocation = bridgeSpilledParamLocation.sub(4);
                    if (bridgeRegisterIndex <= NUM_PARAMETER_GPRS) {
                        if (VM.TraceStkMaps || TRACE_ALL || TRACE_DL) {
                            VM.sysWrite("VM_BaselineGCMapIterator getNextReferenceOffset = dynamic link GPR parameter ");
                            VM.sysWrite(bridgeRegisterLocation.add(4));
                            VM.sysWrite(".\n");
                        }
                        return bridgeRegisterLocation.add(4);
                    } else {
                        if (bridgeSpilledParameterMappingRequired) {
                            if (VM.TraceStkMaps || TRACE_ALL || TRACE_DL) {
                                VM.sysWrite("VM_BaselineGCMapIterator getNextReferenceOffset = dynamic link spilled parameter ");
                                VM.sysWrite(bridgeSpilledParamLocation.add(4));
                                VM.sysWrite(".\n");
                            }
                            return bridgeSpilledParamLocation.add(4);
                        } else {
                            break;
                        }
                    }
                } else if (bridgeParameterType.isLongType()) {
                    bridgeRegisterIndex += 2;
                    bridgeRegisterLocation = bridgeRegisterLocation.sub(8);
                    bridgeSpilledParamLocation = bridgeSpilledParamLocation.sub(8);
                } else if (bridgeParameterType.isDoubleType()) {
                    bridgeSpilledParamLocation = bridgeSpilledParamLocation.sub(8);
                } else if (bridgeParameterType.isFloatType()) {
                    bridgeSpilledParamLocation = bridgeSpilledParamLocation.sub(4);
                } else {
                    bridgeRegisterIndex += 1;
                    bridgeRegisterLocation = bridgeRegisterLocation.sub(4);
                    bridgeSpilledParamLocation = bridgeSpilledParamLocation.sub(4);
                }
            }
        } else {
            registerLocations[JTOC] = framePtr.add(JTOC_SAVE_OFFSET).toInt();
        }
        return VM_Address.zero();
    }

    VM_Address getNextReturnAddressAddress() {
        if (mapId >= 0) {
            if (VM.TraceStkMaps || TRACE_ALL) {
                VM.sysWrite("VM_BaselineGCMapIterator getNextReturnAddressOffset mapId = ");
                VM.sysWrite(mapId);
                VM.sysWrite(".\n");
            }
            return VM_Address.zero();
        }
        mapOffset = maps.getNextJSRReturnAddr(mapOffset);
        if (VM.TraceStkMaps || TRACE_ALL) {
            VM.sysWrite("VM_BaselineGCMapIterator getNextReturnAddressOffset = ");
            VM.sysWrite(mapOffset);
            VM.sysWrite(".\n");
        }
        return (mapOffset == 0) ? VM_Address.zero() : framePtr.add(mapOffset);
    }

    void cleanupPointers() {
        maps.cleanupPointers();
        maps = null;
        if (mapId < 0) VM_ReferenceMaps.jsrLock.unlock();
        bridgeTarget = null;
        bridgeParameterTypes = null;
    }

    int getType() {
        return VM_CompiledMethod.BASELINE;
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

    private VM_Address bridgeRegisterLocation;

    private VM_Address bridgeSpilledParamLocation;

    private int bridgeSpilledParamInitialOffset;
}
