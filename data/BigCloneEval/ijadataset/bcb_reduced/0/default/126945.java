/**
 * This class compiles the prolog and epilog for all code that makes
 * the transition between Java and Native C
 * 2 cases:
 *  -from Java to C:  all user-defined native methods
 *  -C to Java:  all JNI functions in VM_JNIFunctions.java
 *
 * @author Ton Ngo
 * @author Steve Smith
 */
public class VM_JNICompiler implements VM_JNILinuxConstants, VM_BaselineConstants {

    private static final int SAVED_GPRS = 5;

    static final int EDI_SAVE_OFFSET = STACKFRAME_BODY_OFFSET;

    static final int EBX_SAVE_OFFSET = STACKFRAME_BODY_OFFSET - WORDSIZE;

    static final int EBP_SAVE_OFFSET = EBX_SAVE_OFFSET - WORDSIZE;

    static final int JNI_RETURN_ADDRESS_OFFSET = EBP_SAVE_OFFSET - WORDSIZE;

    static final int JNI_PR_OFFSET = JNI_RETURN_ADDRESS_OFFSET - WORDSIZE;

    static final int SAVED_JAVA_FP_OFFSET = STACKFRAME_BODY_OFFSET;

    static final int SAVED_GPRS_FOR_JNI = 5;

    /*****************************************************************
   * Handle the Java to C transition:  native methods
   *
   */
    static VM_MachineCode generateGlueCodeForNative(VM_CompiledMethod cm) {
        int compiledMethodId = cm.getId();
        VM_Method method = cm.getMethod();
        VM_Assembler asm = new VM_Assembler(100);
        int nativeIP = method.getNativeIP();
        int parameterWords = method.getParameterWords();
        prepareStackHeader(asm, method, compiledMethodId);
        storeParametersForLintel(asm, method);
        asm.emitMOV_Reg_Imm(S0, nativeIP);
        asm.emitCALL_RegDisp(JTOC, VM_Entrypoints.invokeNativeFunctionInstructionsField.getOffset());
        VM_ProcessorLocalState.emitMoveFieldToReg(asm, S0, VM_Entrypoints.activeThreadField.getOffset());
        asm.emitMOV_Reg_RegDisp(S0, S0, VM_Entrypoints.jniEnvField.getOffset());
        if (method.getReturnType().isReferenceType()) {
            asm.emitADD_Reg_RegDisp(T0, S0, VM_Entrypoints.JNIRefsField.getOffset());
            asm.emitMOV_Reg_RegInd(T0, T0);
        } else if (method.getReturnType().isLongType()) {
            asm.emitPUSH_Reg(T1);
        }
        VM_ProcessorLocalState.emitMoveFieldToReg(asm, S0, VM_Entrypoints.activeThreadField.getOffset());
        asm.emitMOV_Reg_RegDisp(S0, S0, VM_Entrypoints.jniEnvField.getOffset());
        popJNIrefForEpilog(asm);
        if (method.getReturnType().isLongType()) {
            asm.emitMOV_Reg_Reg(T1, T0);
            asm.emitPOP_Reg(T0);
        }
        VM_ProcessorLocalState.emitMoveFieldToReg(asm, S0, VM_Entrypoints.activeThreadField.getOffset());
        asm.emitMOV_Reg_RegDisp(S0, S0, VM_Entrypoints.jniEnvField.getOffset());
        asm.emitMOV_Reg_RegDisp(EBX, S0, VM_Entrypoints.JNIPendingExceptionField.getOffset());
        asm.emitMOV_RegDisp_Imm(S0, VM_Entrypoints.JNIPendingExceptionField.getOffset(), 0);
        asm.emitCMP_Reg_Imm(EBX, 0);
        VM_ForwardReference fr = asm.forwardJcc(asm.EQ);
        asm.emitMOV_Reg_Reg(T0, EBX);
        asm.emitMOV_Reg_RegDisp(T1, JTOC, VM_Entrypoints.athrowMethod.getOffset());
        asm.emitMOV_Reg_Reg(SP, EBP);
        asm.emitMOV_Reg_RegDisp(JTOC, SP, EDI_SAVE_OFFSET);
        asm.emitMOV_Reg_RegDisp(EBX, SP, EBX_SAVE_OFFSET);
        asm.emitMOV_Reg_RegDisp(EBP, SP, EBP_SAVE_OFFSET);
        asm.emitPOP_RegDisp(PR, VM_Entrypoints.framePointerField.getOffset());
        asm.emitJMP_Reg(T1);
        fr.resolve(asm);
        asm.emitMOV_Reg_Reg(SP, EBP);
        asm.emitMOV_Reg_RegDisp(JTOC, SP, EDI_SAVE_OFFSET);
        asm.emitMOV_Reg_RegDisp(EBX, SP, EBX_SAVE_OFFSET);
        asm.emitMOV_Reg_RegDisp(EBP, SP, EBP_SAVE_OFFSET);
        asm.emitPOP_RegDisp(PR, VM_Entrypoints.framePointerField.getOffset());
        if (method.isStatic()) asm.emitRET_Imm(parameterWords << LG_WORDSIZE); else asm.emitRET_Imm((parameterWords + 1) << LG_WORDSIZE);
        return new VM_MachineCode(asm.getMachineCodes(), null);
    }

    /**************************************************************
   * Prepare the stack header for Java to C transition
   *         before               after
   *	   high address		high address
   *	   |          |		|          | Caller frame
   *	   |          |		|          |
   *  +    |arg 0     |		|arg 0     |    
   *  +    |arg 1     |		|arg 1     |
   *  +    |...       |		|...       |
   *  +8   |arg n-1   |		|arg n-1   |    
   *  +4   |returnAddr|		|returnAddr|
   *   0   +	      +		+saved FP  + <---- FP for glue frame
   *  -4   |	      |		|methodID  |
   *  -8   |	      |		|saved EDI |  (EDI == JTOC - for baseline methods)  
   *  -C   |	      |		|saved EBX |    
   *  -10  |	      |	        |	   |	
   *  
   *  
   *  
   */
    static void prepareStackHeader(VM_Assembler asm, VM_Method method, int compiledMethodId) {
        asm.emitPUSH_RegDisp(PR, VM_Entrypoints.framePointerField.getOffset());
        VM_ProcessorLocalState.emitMoveRegToField(asm, VM_Entrypoints.framePointerField.getOffset(), SP);
        asm.emitMOV_RegDisp_Imm(SP, STACKFRAME_METHOD_ID_OFFSET, compiledMethodId);
        asm.emitMOV_RegDisp_Reg(SP, EDI_SAVE_OFFSET, JTOC);
        asm.emitMOV_RegDisp_Reg(SP, EBX_SAVE_OFFSET, EBX);
        asm.emitMOV_RegDisp_Reg(SP, EBP_SAVE_OFFSET, EBP);
        asm.emitMOV_Reg_Reg(EBP, SP);
        VM_ProcessorLocalState.emitMoveFieldToReg(asm, JTOC, VM_Entrypoints.jtocField.getOffset());
    }

    /**************************************************************
   * Process the arguments:
   *   -insert the 2 JNI args
   *   -replace pointers
   *   -reverse the order of the args from Java to fit the C convention
   *   -
   *
   *         before               after
   *
   *	   high address		high address
   *	   |          | 	|          | Caller frame
   *	   |          |		|          | 
   *  +    |arg 0     | 	|arg 0     | 	-> firstParameterOffset
   *  +    |arg 1     |		|arg 1     | 
   *  +    |...       |		|...       | 
   *  +8   |arg n-1   | 	|arg n-1   | 	
   *  +4   |returnAddr|		|returnAddr| 
   *   0   +saved FP  + 	+saved FP  + <---- FP for glue frame
   *  -4   |methodID  |		|methodID  | 
   *  -8   |saved EDI | 	|saved EDI | 	-> STACKFRAME_BODY_OFFSET = -8
   *  -C   |saved EBX | 	|saved EBX | 	
   *  -10  |	      | 	|returnAddr|  (return from OutOfLine to generated epilog)    
   *  -14  |	      |	        |saved PR  |
   *  -18  |	      |	        |arg n-1   |  reordered args to native method (firstLocalOffset
   *  -1C  |	      |	        | ...      |  ...
   *  -20  |	      |  	|arg 1     |  ...
   *  -24  |	      |	        |arg 0     |  ...
   *  -28  |	      |	        |class/obj |  required second arg 
   *  -2C  |	      |   SP -> |jniEnv    |  required first arg  (emptyStackOffset)
   *  -30  |	      |	        |          |    
   *	   |          |  	|          | 	
   *	    low address		 low address
   */
    static void storeParametersForLintel(VM_Assembler asm, VM_Method method) {
        VM_Class klass = method.getDeclaringClass();
        int parameterWords = method.getParameterWords();
        int savedRegistersSize = SAVED_GPRS << LG_WORDSIZE;
        int firstLocalOffset = STACKFRAME_BODY_OFFSET - savedRegistersSize;
        int emptyStackOffset = firstLocalOffset - ((parameterWords + 2) << LG_WORDSIZE) + WORDSIZE;
        int firstParameterOffset = STACKFRAME_BODY_OFFSET + STACKFRAME_HEADER_SIZE + (parameterWords << LG_WORDSIZE);
        int firstActualParameter;
        VM_Type[] types = method.getParameterTypes();
        int numArguments = types.length;
        int numRefArguments = 1;
        int numFloats = 0;
        for (int i = 0; i < numArguments; i++) {
            if (types[i].isReferenceType()) numRefArguments++;
            if (types[i].isFloatType() || types[i].isDoubleType()) numFloats++;
        }
        int gpr = 0;
        int parameterOffset = firstParameterOffset;
        if (!method.isStatic()) {
            asm.emitMOV_RegDisp_Reg(EBP, firstParameterOffset + WORDSIZE, VOLATILE_GPRS[gpr]);
            gpr++;
        }
        for (int i = 0; i < numArguments && gpr < NUM_PARAMETER_GPRS; i++) {
            if (types[i].isDoubleType()) {
                parameterOffset -= 2 * WORDSIZE;
                continue;
            } else if (types[i].isFloatType()) {
                parameterOffset -= WORDSIZE;
                continue;
            } else if (types[i].isLongType()) {
                if (gpr < NUM_PARAMETER_GPRS) {
                    asm.emitMOV_RegDisp_Reg(EBP, parameterOffset, VOLATILE_GPRS[gpr]);
                    gpr++;
                    parameterOffset -= WORDSIZE;
                }
                if (gpr < NUM_PARAMETER_GPRS) {
                    asm.emitMOV_RegDisp_Reg(EBP, parameterOffset, VOLATILE_GPRS[gpr]);
                    gpr++;
                    parameterOffset -= WORDSIZE;
                }
            } else {
                if (gpr < NUM_PARAMETER_GPRS) {
                    asm.emitMOV_RegDisp_Reg(EBP, parameterOffset, VOLATILE_GPRS[gpr]);
                    gpr++;
                    parameterOffset -= WORDSIZE;
                }
            }
        }
        asm.emitADD_Reg_Imm(SP, emptyStackOffset);
        VM_ProcessorLocalState.emitMoveFieldToReg(asm, S0, VM_Entrypoints.activeThreadField.getOffset());
        asm.emitMOV_Reg_RegDisp(S0, S0, VM_Entrypoints.jniEnvField.getOffset());
        VM_ProcessorLocalState.emitStoreProcessor(asm, S0, VM_Entrypoints.JNIEnvSavedPRField.getOffset());
        asm.emitMOV_RegDisp_Reg(S0, VM_Entrypoints.JNITopJavaFPField.getOffset(), EBP);
        startJNIrefForProlog(asm, numRefArguments);
        asm.emitMOV_Reg_RegDisp(EBX, S0, VM_Entrypoints.JNIEnvAddressField.getOffset());
        asm.emitMOV_RegDisp_Reg(EBP, emptyStackOffset, EBX);
        VM_ProcessorLocalState.emitMoveFieldToReg(asm, T1, VM_Entrypoints.vpStatusAddressField.getOffset());
        asm.emitMOV_RegDisp_Reg(EBX, WORDSIZE, T1);
        VM_ProcessorLocalState.emitMoveFieldToReg(asm, JTOC, VM_Entrypoints.jtocField.getOffset());
        if (method.isStatic()) {
            klass.getClassForType();
            int tibOffset = klass.getTibOffset();
            asm.emitMOV_Reg_RegDisp(EBX, JTOC, tibOffset);
            asm.emitMOV_Reg_RegInd(EBX, EBX);
            asm.emitMOV_Reg_RegDisp(EBX, EBX, VM_Entrypoints.classForTypeField.getOffset());
            firstActualParameter = 0;
        } else {
            asm.emitMOV_Reg_RegDisp(EBX, EBP, firstParameterOffset + WORDSIZE);
            firstActualParameter = 1;
        }
        pushJNIref(asm);
        asm.emitMOV_RegDisp_Reg(EBP, emptyStackOffset + WORDSIZE, EBX);
        int i = parameterWords - 1;
        int fpr = numFloats - 1;
        for (int argIndex = numArguments - 1; argIndex >= 0; argIndex--) {
            if (types[argIndex].isReferenceType()) {
                asm.emitMOV_Reg_RegDisp(EBX, EBP, firstParameterOffset - (i * WORDSIZE));
                asm.emitCMP_Reg_Imm(EBX, 0);
                VM_ForwardReference beq = asm.forwardJcc(asm.EQ);
                pushJNIref(asm);
                beq.resolve(asm);
                asm.emitMOV_RegDisp_Reg(EBP, emptyStackOffset + (WORDSIZE * (2 + i)), EBX);
                i--;
            } else if (types[argIndex].isDoubleType()) {
                if (fpr < NUM_PARAMETER_FPRS) {
                    asm.emitFSTP_RegDisp_Reg_Quad(EBP, emptyStackOffset + (WORDSIZE * (2 + i - 1)), FP0);
                } else {
                    asm.emitMOV_Reg_RegDisp(EBX, EBP, firstParameterOffset - (i * WORDSIZE));
                    asm.emitMOV_RegDisp_Reg(EBP, emptyStackOffset + (WORDSIZE * (2 + i - 1)), EBX);
                    asm.emitMOV_Reg_RegDisp(EBX, EBP, firstParameterOffset - ((i - 1) * WORDSIZE));
                    asm.emitMOV_RegDisp_Reg(EBP, emptyStackOffset + (WORDSIZE * (2 + i)), EBX);
                }
                i -= 2;
                fpr--;
            } else if (types[argIndex].isFloatType()) {
                if (fpr < NUM_PARAMETER_FPRS) {
                    asm.emitFSTP_RegDisp_Reg(EBP, emptyStackOffset + (WORDSIZE * (2 + i)), FP0);
                } else {
                    asm.emitMOV_Reg_RegDisp(EBX, EBP, firstParameterOffset - (i * WORDSIZE));
                    asm.emitMOV_RegDisp_Reg(EBP, emptyStackOffset + (WORDSIZE * (2 + i)), EBX);
                }
                i--;
                fpr--;
            } else if (types[argIndex].isLongType()) {
                asm.emitMOV_Reg_RegDisp(EBX, EBP, firstParameterOffset - (i * WORDSIZE));
                asm.emitMOV_RegDisp_Reg(EBP, emptyStackOffset + (WORDSIZE * (2 + i - 1)), EBX);
                asm.emitMOV_Reg_RegDisp(EBX, EBP, firstParameterOffset - ((i - 1) * WORDSIZE));
                asm.emitMOV_RegDisp_Reg(EBP, emptyStackOffset + (WORDSIZE * (2 + i)), EBX);
                i -= 2;
            } else {
                asm.emitMOV_Reg_RegDisp(EBX, EBP, firstParameterOffset - (i * WORDSIZE));
                asm.emitMOV_RegDisp_Reg(EBP, emptyStackOffset + (WORDSIZE * (2 + i)), EBX);
                i--;
            }
        }
    }

    /**
   * Start a new frame for this Java to C transition:
   * Expect: 
   *    -S0 contains a pointer to the VM_Thread.jniEnv
   * Perform these steps:
   *    -push current SavedFP index 
   *    -set SaveFP index <- current TOP
   * Leave registers ready for more push onto the jniEnv.JNIRefs array
   *    -S0 holds jniEnv so we can update jniEnv.JNIRefsTop and 
   *    -T0 holds address of top =  starting address of jniEnv.JNIRefs array + jniEnv.JNIRefsTop
   *     T0 is to be incremented before each push
   *    S0              ebx                         T0
   *  jniEnv        
   *    .         jniEnv.JNIRefs             jniEnv.JNIRefsTop
   *    .               .                    jniEnv.JNIRefsTop + 4
   *    .         jniEnv.JNIRefsSavedFP            .
   *    .               .                    jniEnv.JNIRefsTop
   *    .               .                    address(JNIRefsTop)             
   *    .
   */
    static void startJNIrefForProlog(VM_Assembler asm, int numRefsExpected) {
        asm.emitMOV_Reg_RegDisp(EBX, S0, VM_Entrypoints.JNIRefsField.getOffset());
        asm.emitMOV_Reg_RegDisp(T0, S0, VM_Entrypoints.JNIRefsTopField.getOffset());
        asm.emitADD_Reg_Imm(T0, numRefsExpected * WORDSIZE);
        asm.emitCMP_Reg_RegDisp(T0, S0, VM_Entrypoints.JNIRefsMaxField.getOffset());
        asm.emitADD_RegDisp_Imm(S0, VM_Entrypoints.JNIRefsTopField.getOffset(), WORDSIZE);
        asm.emitMOV_Reg_RegDisp(T0, S0, VM_Entrypoints.JNIRefsTopField.getOffset());
        asm.emitADD_Reg_Reg(T0, EBX);
        asm.emitMOV_Reg_RegDisp(EBX, S0, VM_Entrypoints.JNIRefsSavedFPField.getOffset());
        asm.emitMOV_RegInd_Reg(T0, EBX);
        asm.emitMOV_Reg_RegDisp(T0, S0, VM_Entrypoints.JNIRefsTopField.getOffset());
        asm.emitMOV_RegDisp_Reg(S0, VM_Entrypoints.JNIRefsSavedFPField.getOffset(), T0);
        asm.emitADD_Reg_RegDisp(T0, S0, VM_Entrypoints.JNIRefsField.getOffset());
        asm.emitADD_RegDisp_Imm(S0, VM_Entrypoints.JNIRefsTopField.getOffset(), numRefsExpected * WORDSIZE);
    }

    /**
   * Push a pointer value onto the JNIRefs array, 
   * Expect:
   *   -T0 pointing to the address of the valid top 
   *   -the pointer value in register ebx
   *   -the space in the JNIRefs array has checked for overflow 
   *   by startJNIrefForProlog()
   * Perform these steps:
   *   -increment the JNIRefsTop index in ebx by 4
   *   -push a pointer value in ebx onto the top of the JNIRefs array
   *   -put the JNIRefsTop index into the sourceReg as the replacement for the pointer
   * Note:  jniEnv.JNIRefsTop is not updated yet
   *
   */
    static void pushJNIref(VM_Assembler asm) {
        asm.emitADD_Reg_Imm(T0, WORDSIZE);
        asm.emitMOV_RegInd_Reg(T0, EBX);
        asm.emitMOV_Reg_Reg(EBX, T0);
        asm.emitSUB_Reg_RegDisp(EBX, S0, VM_Entrypoints.JNIRefsField.getOffset());
    }

    /**
   * Generate the code to pop the frame in JNIRefs array for this Java to C transition
   * Expect:
   *  -JTOC, PR registers are valid
   *  -S0 contains a pointer to the VM_Thread.jniEnv
   *  -EBX and T1 are available as scratch registers
   * Perform these steps:
   *  -jniEnv.JNIRefsTop <- jniEnv.JNIRefsSavedFP - 4
   *  -jniEnv.JNIRefsSavedFP <- (jniEnv.JNIRefs + jniEnv.JNIRefsSavedFP)
   *
   */
    static void popJNIrefForEpilog(VM_Assembler asm) {
        asm.emitMOV_Reg_RegDisp(T1, S0, VM_Entrypoints.JNIRefsSavedFPField.getOffset());
        asm.emitMOV_RegDisp_Reg(S0, VM_Entrypoints.JNIRefsTopField.getOffset(), T1);
        asm.emitSUB_RegDisp_Imm(S0, VM_Entrypoints.JNIRefsTopField.getOffset(), WORDSIZE);
        asm.emitMOV_Reg_RegDisp(EBX, S0, VM_Entrypoints.JNIRefsField.getOffset());
        asm.emitMOV_Reg_RegIdx(EBX, EBX, T1, asm.BYTE, 0);
        asm.emitMOV_RegDisp_Reg(S0, VM_Entrypoints.JNIRefsSavedFPField.getOffset(), EBX);
    }

    /*****************************************************************
   * Handle the C to Java transition:  JNI methods in VM_JNIFunctions.java
   * NOTE:
   *   -We need PR to access Java environment, but not certain whether
   *    Linux C treats it as nonvolatile and restores it before calling, 
   *    so for now it is saved in the JNIenv and restored from there.
   *   -Unlike the powerPC scheme which has a special prolog preceding
   *    the normal Java prolog, the Intel scheme replaces the Java prolog
   *    completely with the special prolog
   *
   *            Stack on entry            Stack at end of prolog after call
   *             high memory 			   high memory
   *            |            |                   |            |
   *	EBP ->	|saved FP    | 			 |saved FP    |
   *            |  ...       |                   |  ...       |
   *            |            |                   |            |
   *		|arg n-1     | 			 |arg n-1     |
   * native    	|  ...       | 			 |  ...       |       
   * caller    	|arg 0       | 			 |arg 0       |
   *	ESP -> 	|return addr |        		 |return addr |
   *            |            |           EBP ->  |saved FP    |
   *            |            |                   |methodID    | normal MethodID for JNI function
   *            |            |                   |saved JavaFP| offset to preceeding java frame
   *            |            |                   |saved edi   |	to be used for JTOC
   *            |            |                   |  "   ebx   |	to be used for nonvolatile
   *            |            |                   |  "   ecx   |	to be used for scrach
   *            |            |                   |  "   esi   |	to be used for PR
   *            |            |                   |arg 0       | copied in reverse order
   *            |            |                   |  ...       |
   *            |            |           ESP ->  |arg n-1     |
   *            |            |                   |            | normally compiled Java code continue
   *            |            |                   |            |
   *            |            |                   |            |
   *            |            |                   |            |
   *             low memory                        low memory
   *
   */
    static void generateGlueCodeForJNIMethod(VM_Assembler asm, VM_Method method, int methodID) {
        VM_Address bootRecordAddress = VM_Magic.objectAsAddress(VM_BootRecord.the_boot_record);
        asm.emitPUSH_Reg(EBP);
        asm.emitMOV_Reg_Reg(EBP, SP);
        asm.emitPUSH_Imm(methodID);
        asm.emitSUB_Reg_Imm(SP, WORDSIZE);
        asm.emitPUSH_Reg(JTOC);
        asm.emitPUSH_Reg(EBX);
        asm.emitPUSH_Reg(S0);
        VM_ProcessorLocalState.emitPushProcessor(asm);
        VM_Type[] types = method.getParameterTypes();
        int numArguments = types.length;
        int argOffset = 2;
        for (int i = 0; i < numArguments; i++) {
            if (types[i].isLongType() || types[i].isDoubleType()) {
                asm.emitMOV_Reg_RegDisp(EBX, EBP, ((argOffset + 1) * WORDSIZE));
                asm.emitPUSH_Reg(EBX);
                asm.emitMOV_Reg_RegDisp(EBX, EBP, (argOffset * WORDSIZE));
                asm.emitPUSH_Reg(EBX);
                argOffset += 2;
            } else {
                asm.emitMOV_Reg_RegDisp(EBX, EBP, (argOffset * WORDSIZE));
                asm.emitPUSH_Reg(EBX);
                argOffset++;
            }
        }
        int retryLabel = asm.getMachineCodeIndex();
        asm.emitMOV_Reg_RegDisp(EBX, EBP, (2 * WORDSIZE));
        asm.emitMOV_Reg_RegDisp(JTOC, EBX, 0);
        asm.emitMOV_Reg_RegDisp(JTOC, JTOC, JNIFUNCTIONS_JTOC_OFFSET);
        asm.emitMOV_Reg_RegDisp(S0, EBX, WORDSIZE);
        asm.emitMOV_Reg_RegInd(T0, S0);
        asm.emitCMP_Reg_Imm(T0, VM_Processor.IN_NATIVE);
        VM_ForwardReference fr = asm.forwardJcc(asm.EQ);
        asm.emitMOV_Reg_RegDisp(T0, JTOC, VM_Entrypoints.the_boot_recordField.getOffset());
        asm.emitCALL_RegDisp(T0, VM_Entrypoints.sysVirtualProcessorYieldIPField.getOffset());
        asm.emitJMP_Imm(retryLabel);
        fr.resolve(asm);
        asm.emitMOV_Reg_Imm(T1, VM_Processor.IN_JAVA);
        asm.emitCMPXCHG_RegInd_Reg(S0, T1);
        asm.emitJCC_Cond_Imm(asm.NE, retryLabel);
        int numLocalVariables = method.getLocalWords() - method.getParameterWords();
        asm.emitSUB_Reg_Imm(SP, (numLocalVariables << LG_WORDSIZE));
        asm.emitMOV_Reg_RegDisp(S0, JTOC, VM_Entrypoints.JNIFunctionPointersField.getOffset());
        asm.emitSUB_Reg_Reg(EBX, S0);
        asm.emitSHR_Reg_Imm(EBX, 1);
        asm.emitMOV_Reg_RegDisp(S0, JTOC, VM_Entrypoints.threadsField.getOffset());
        asm.emitMOV_Reg_RegIdx(S0, S0, EBX, asm.BYTE, 0);
        asm.emitMOV_Reg_RegDisp(EBX, S0, VM_Entrypoints.jniEnvField.getOffset());
        asm.emitMOV_Reg_RegDisp(ESI, EBX, VM_Entrypoints.JNITopJavaFPField.getOffset());
        asm.emitSUB_Reg_Reg(ESI, EBP);
        asm.emitMOV_RegDisp_Reg(EBP, SAVED_JAVA_FP_OFFSET, ESI);
        VM_ProcessorLocalState.emitSetProcessor(asm, EBX, VM_Entrypoints.JNIEnvSavedPRField.getOffset());
        VM_ProcessorLocalState.emitMoveRegToField(asm, VM_Entrypoints.framePointerField.getOffset(), EBP);
        VM_ProcessorLocalState.emitMoveFieldToReg(asm, T0, VM_Entrypoints.processorModeField.getOffset());
        asm.emitCMP_Reg_Imm(T0, VM_Processor.RVM);
        VM_ForwardReference fr1 = asm.forwardJcc(asm.EQ);
        asm.emitCALL_RegDisp(JTOC, VM_Entrypoints.becomeRVMThreadMethod.getOffset());
        fr1.resolve(asm);
        asm.emitNOP();
    }

    static void generateEpilogForJNIMethod(VM_Assembler asm, VM_Method method) {
        if (method.getReturnType().isLongType()) {
            asm.emitPUSH_Reg(T1);
            asm.emitMOV_Reg_Reg(T1, T0);
            asm.emitPOP_Reg(T0);
        }
        VM_ProcessorLocalState.emitMoveFieldToReg(asm, S0, VM_Entrypoints.activeThreadField.getOffset());
        asm.emitMOV_Reg_RegDisp(S0, S0, VM_Entrypoints.jniEnvField.getOffset());
        asm.emitMOV_Reg_RegDisp(JTOC, EBP, SAVED_JAVA_FP_OFFSET);
        asm.emitADD_Reg_Reg(JTOC, EBP);
        asm.emitMOV_RegDisp_Reg(S0, VM_Entrypoints.JNITopJavaFPField.getOffset(), JTOC);
        VM_ProcessorLocalState.emitStoreProcessor(asm, S0, VM_Entrypoints.JNIEnvSavedPRField.getOffset());
        VM_ProcessorLocalState.emitStoreProcessor(asm, JTOC, JNI_PR_OFFSET);
        VM_ProcessorLocalState.emitMoveFieldToReg(asm, EBX, VM_Entrypoints.vpStatusAddressField.getOffset());
        asm.emitMOV_Reg_RegDisp(JTOC, S0, VM_Entrypoints.JNIEnvAddressField.getOffset());
        asm.emitMOV_RegDisp_Reg(JTOC, WORDSIZE, EBX);
        asm.emitMOV_RegInd_Imm(EBX, VM_Processor.IN_NATIVE);
        VM_ProcessorLocalState.emitPopProcessor(asm);
        asm.emitPOP_Reg(S0);
        asm.emitPOP_Reg(EBX);
        asm.emitPOP_Reg(JTOC);
        asm.emitMOV_Reg_Reg(SP, EBP);
        asm.emitPOP_Reg(EBP);
        asm.emitRET();
    }
}
