import java.lang.reflect.*;

/**
 *   This class implements the JNI environment, it includes:
 * -The array of JNI function pointers accessible from C
 * -Implementation of all the JNI functions
 *
 * @author Ton Ngo
 * @author Steve Smith 
 */
public class VM_JNIEnvironment implements VM_JNILinuxConstants, VM_RegisterConstants {

    private static boolean initialized = false;

    private static String[] names;

    static VM_CompilerInfo javaToCCompilerInfo;

    /**
   * This is the JNI function table, the address of this array will be
   * passed to the native code
   */
    private static INSTRUCTION[][] JNIFunctions;

    static int[] JNIFunctionPointers;

    /**
   * These are thread specific information, such as:
   *  -the list of references passed to native code, for GC purpose
   *  -saved RVM system registers
   */
    int JNIEnvAddress;

    int savedTIreg;

    VM_Processor savedPRreg;

    boolean alwaysHasNativeFrame;

    int[] JNIRefs;

    int JNIRefsTop;

    int JNIRefsMax;

    int JNIRefsSavedFP;

    int JNITopJavaFP;

    Throwable pendingException = null;

    VM_Registers savedContextForTermination;

    static final int JNIREFS_ARRAY_LENGTH = 100;

    public static void init() {
        JNIFunctions = new byte[FUNCTIONCOUNT + 1][];
        JNIFunctionPointers = new int[VM_Scheduler.MAX_THREADS * 2];
        JNIFunctionPointers = new int[VM_Scheduler.MAX_THREADS * 2];
    }

    /**
   *  Initialize the array of JNI functions
   *  To be called from VM_DynamicLibrary.java when a library is loaded,
   *  expecting native calls to be made
   *
   */
    public static void boot() {
        if (initialized) return;
        setNames();
        try {
            VM_Class cls = VM_Class.forName("VM_JNIFunctions");
            VM_Method[] mths = cls.getDeclaredMethods();
            for (int i = 0; i < mths.length; i++) {
                String methodName = mths[i].getName().toString();
                int jniIndex = indexOf(methodName);
                if (jniIndex != -1) {
                    JNIFunctions[jniIndex] = mths[i].getMostRecentlyGeneratedInstructions();
                }
            }
        } catch (VM_ResolutionException e) {
            throw new InternalError("VM_JNIEnvironment fails to initialize, has the class been renamed\n");
        }
        VM_Magic.setMemoryWord(VM_Magic.objectAsAddress(JNIFunctions) + JNIFUNCTIONS_JTOC_OFFSET, VM_Magic.getTocPointer());
        initialized = true;
    }

    public VM_JNIEnvironment(int threadSlot) {
        JNIFunctionPointers[threadSlot * 2] = VM_Magic.objectAsAddress(JNIFunctions);
        JNIFunctionPointers[(threadSlot * 2) + 1] = VM_Magic.objectAsAddress(VM_Statics.getSlots());
        JNIEnvAddress = VM_Magic.objectAsAddress(JNIFunctionPointers) + threadSlot * 8;
        JNIFunctionPointers[threadSlot * 2] = VM_Magic.objectAsAddress(JNIFunctions);
        JNIFunctionPointers[(threadSlot * 2) + 1] = 0;
        JNIEnvAddress = VM_Magic.objectAsAddress(JNIFunctionPointers) + threadSlot * 8;
        JNIRefs = new int[JNIREFS_ARRAY_LENGTH];
        JNIRefs[0] = 0;
        JNIRefsTop = 0;
        JNIRefsSavedFP = 0;
        JNIRefsMax = (JNIRefs.length - 1) * 4;
        alwaysHasNativeFrame = false;
    }

    public int pushJNIRef(Object ref) {
        JNIRefsTop += 4;
        JNIRefs[JNIRefsTop >> 2] = VM_Magic.objectAsAddress(ref);
        return JNIRefsTop;
    }

    public Object getJNIRef(int offset) {
        if (offset > JNIRefsTop) {
            VM.sysWrite("JNI ERROR: getJNIRef for illegal offset > TOP, ");
            VM.sysWrite(offset);
            VM.sysWrite("\n");
            return null;
        }
        return VM_Magic.addressAsObject(JNIRefs[offset >> 2]);
    }

    public void recordException(Throwable e) {
        if (pendingException == null || e == null) pendingException = e;
    }

    public Throwable getException() {
        return pendingException;
    }

    public int getJNIenvAddress() {
        return JNIEnvAddress;
    }

    public INSTRUCTION[] getInstructions(int id) {
        return JNIFunctions[id];
    }

    private static int indexOf(String functionName) {
        for (int i = 0; i < FUNCTIONCOUNT; i++) {
            if (names[i].equals(functionName)) return i;
        }
        return -1;
    }

    private static String[] setNames() {
        names = new String[FUNCTIONCOUNT];
        names[0] = new String("undefined");
        names[RESERVED0] = new String("reserved0");
        names[RESERVED1] = new String("reserved1");
        names[RESERVED2] = new String("reserved2");
        names[RESERVED3] = new String("reserved3");
        names[GETVERSION] = new String("GetVersion");
        names[DEFINECLASS] = new String("DefineClass");
        names[FINDCLASS] = new String("FindClass");
        names[FROMREFLECTEDMETHOD] = new String("FromReflectedMethod");
        names[FROMREFLECTEDFIELD] = new String("FromReflectedField");
        names[TOREFLECTEDMETHOD] = new String("ToReflectedMethod");
        names[GETSUPERCLASS] = new String("GetSuperclass");
        names[ISASSIGNABLEFROM] = new String("IsAssignableFrom");
        names[TOREFLECTEDFIELD] = new String("ToReflectedField");
        names[THROW] = new String("Throw");
        names[THROWNEW] = new String("ThrowNew");
        names[EXCEPTIONOCCURRED] = new String("ExceptionOccurred");
        names[EXCEPTIONDESCRIBE] = new String("ExceptionDescribe");
        names[EXCEPTIONCLEAR] = new String("ExceptionClear");
        names[FATALERROR] = new String("FatalError");
        names[PUSHLOCALFRAME] = new String("PushLocalFrame");
        names[POPLOCALFRAME] = new String("PopLocalFrame");
        names[NEWGLOBALREF] = new String("NewGlobalRef");
        names[DELETEGLOBALREF] = new String("DeleteGlobalRef");
        names[DELETELOCALREF] = new String("DeleteLocalRef");
        names[ISSAMEOBJECT] = new String("IsSameObject");
        names[NEWLOCALREF] = new String("NewLocalRef");
        names[ENSURELOCALCAPACITY] = new String("EnsureLocalCapacity");
        names[ALLOCOBJECT] = new String("AllocObject");
        names[NEWOBJECT] = new String("NewObject");
        names[NEWOBJECTV] = new String("NewObjectV");
        names[NEWOBJECTA] = new String("NewObjectA");
        names[GETOBJECTCLASS] = new String("GetObjectClass");
        names[ISINSTANCEOF] = new String("IsInstanceOf");
        names[GETMETHODID] = new String("GetMethodID");
        names[CALLOBJECTMETHOD] = new String("CallObjectMethod");
        names[CALLOBJECTMETHODV] = new String("CallObjectMethodV");
        names[CALLOBJECTMETHODA] = new String("CallObjectMethodA");
        names[CALLBOOLEANMETHOD] = new String("CallBooleanMethod");
        names[CALLBOOLEANMETHODV] = new String("CallBooleanMethodV");
        names[CALLBOOLEANMETHODA] = new String("CallBooleanMethodA");
        names[CALLBYTEMETHOD] = new String("CallByteMethod");
        names[CALLBYTEMETHODV] = new String("CallByteMethodV");
        names[CALLBYTEMETHODA] = new String("CallByteMethodA");
        names[CALLCHARMETHOD] = new String("CallCharMethod");
        names[CALLCHARMETHODV] = new String("CallCharMethodV");
        names[CALLCHARMETHODA] = new String("CallCharMethodA");
        names[CALLSHORTMETHOD] = new String("CallShortMethod");
        names[CALLSHORTMETHODV] = new String("CallShortMethodV");
        names[CALLSHORTMETHODA] = new String("CallShortMethodA");
        names[CALLINTMETHOD] = new String("CallIntMethod");
        names[CALLINTMETHODV] = new String("CallIntMethodV");
        names[CALLINTMETHODA] = new String("CallIntMethodA");
        names[CALLLONGMETHOD] = new String("CallLongMethod");
        names[CALLLONGMETHODV] = new String("CallLongMethodV");
        names[CALLLONGMETHODA] = new String("CallLongMethodA");
        names[CALLFLOATMETHOD] = new String("CallFloatMethod");
        names[CALLFLOATMETHODV] = new String("CallFloatMethodV");
        names[CALLFLOATMETHODA] = new String("CallFloatMethodA");
        names[CALLDOUBLEMETHOD] = new String("CallDoubleMethod");
        names[CALLDOUBLEMETHODV] = new String("CallDoubleMethodV");
        names[CALLDOUBLEMETHODA] = new String("CallDoubleMethodA");
        names[CALLVOIDMETHOD] = new String("CallVoidMethod");
        names[CALLVOIDMETHODV] = new String("CallVoidMethodV");
        names[CALLVOIDMETHODA] = new String("CallVoidMethodA");
        names[CALLNONVIRTUALOBJECTMETHOD] = new String("CallNonvirtualObjectMethod");
        names[CALLNONVIRTUALOBJECTMETHODV] = new String("CallNonvirtualObjectMethodV");
        names[CALLNONVIRTUALOBJECTMETHODA] = new String("CallNonvirtualObjectMethodA");
        names[CALLNONVIRTUALBOOLEANMETHOD] = new String("CallNonvirtualBooleanMethod");
        names[CALLNONVIRTUALBOOLEANMETHODV] = new String("CallNonvirtualBooleanMethodV");
        names[CALLNONVIRTUALBOOLEANMETHODA] = new String("CallNonvirtualBooleanMethodA");
        names[CALLNONVIRTUALBYTEMETHOD] = new String("CallNonvirtualByteMethod");
        names[CALLNONVIRTUALBYTEMETHODV] = new String("CallNonvirtualByteMethodV");
        names[CALLNONVIRTUALBYTEMETHODA] = new String("CallNonvirtualByteMethodA");
        names[CALLNONVIRTUALCHARMETHOD] = new String("CallNonvirtualCharMethod");
        names[CALLNONVIRTUALCHARMETHODV] = new String("CallNonvirtualCharMethodV");
        names[CALLNONVIRTUALCHARMETHODA] = new String("CallNonvirtualCharMethodA");
        names[CALLNONVIRTUALSHORTMETHOD] = new String("CallNonvirtualShortMethod");
        names[CALLNONVIRTUALSHORTMETHODV] = new String("CallNonvirtualShortMethodV");
        names[CALLNONVIRTUALSHORTMETHODA] = new String("CallNonvirtualShortMethodA");
        names[CALLNONVIRTUALINTMETHOD] = new String("CallNonvirtualIntMethod");
        names[CALLNONVIRTUALINTMETHODV] = new String("CallNonvirtualIntMethodV");
        names[CALLNONVIRTUALINTMETHODA] = new String("CallNonvirtualIntMethodA");
        names[CALLNONVIRTUALLONGMETHOD] = new String("CallNonvirtualLongMethod");
        names[CALLNONVIRTUALLONGMETHODV] = new String("CallNonvirtualLongMethodV");
        names[CALLNONVIRTUALLONGMETHODA] = new String("CallNonvirtualLongMethodA");
        names[CALLNONVIRTUALFLOATMETHOD] = new String("CallNonvirtualFloatMethod");
        names[CALLNONVIRTUALFLOATMETHODV] = new String("CallNonvirtualFloatMethodV");
        names[CALLNONVIRTUALFLOATMETHODA] = new String("CallNonvirtualFloatMethodA");
        names[CALLNONVIRTUALDOUBLEMETHOD] = new String("CallNonvirtualDoubleMethod");
        names[CALLNONVIRTUALDOUBLEMETHODV] = new String("CallNonvirtualDoubleMethodV");
        names[CALLNONVIRTUALDOUBLEMETHODA] = new String("CallNonvirtualDoubleMethodA");
        names[CALLNONVIRTUALVOIDMETHOD] = new String("CallNonvirtualVoidMethod");
        names[CALLNONVIRTUALVOIDMETHODV] = new String("CallNonvirtualVoidMethodV");
        names[CALLNONVIRTUALVOIDMETHODA] = new String("CallNonvirtualVoidMethodA");
        names[GETFIELDID] = new String("GetFieldID");
        names[GETOBJECTFIELD] = new String("GetObjectField");
        names[GETBOOLEANFIELD] = new String("GetBooleanField");
        names[GETBYTEFIELD] = new String("GetByteField");
        names[GETCHARFIELD] = new String("GetCharField");
        names[GETSHORTFIELD] = new String("GetShortField");
        names[GETINTFIELD] = new String("GetIntField");
        names[GETLONGFIELD] = new String("GetLongField");
        names[GETFLOATFIELD] = new String("GetFloatField");
        names[GETDOUBLEFIELD] = new String("GetDoubleField");
        names[SETOBJECTFIELD] = new String("SetObjectField");
        names[SETBOOLEANFIELD] = new String("SetBooleanField");
        names[SETBYTEFIELD] = new String("SetByteField");
        names[SETCHARFIELD] = new String("SetCharField");
        names[SETSHORTFIELD] = new String("SetShortField");
        names[SETINTFIELD] = new String("SetIntField");
        names[SETLONGFIELD] = new String("SetLongField");
        names[SETFLOATFIELD] = new String("SetFloatField");
        names[SETDOUBLEFIELD] = new String("SetDoubleField");
        names[GETSTATICMETHODID] = new String("GetStaticMethodID");
        names[CALLSTATICOBJECTMETHOD] = new String("CallStaticObjectMethod");
        names[CALLSTATICOBJECTMETHODV] = new String("CallStaticObjectMethodV");
        names[CALLSTATICOBJECTMETHODA] = new String("CallStaticObjectMethodA");
        names[CALLSTATICBOOLEANMETHOD] = new String("CallStaticBooleanMethod");
        names[CALLSTATICBOOLEANMETHODV] = new String("CallStaticBooleanMethodV");
        names[CALLSTATICBOOLEANMETHODA] = new String("CallStaticBooleanMethodA");
        names[CALLSTATICBYTEMETHOD] = new String("CallStaticByteMethod");
        names[CALLSTATICBYTEMETHODV] = new String("CallStaticByteMethodV");
        names[CALLSTATICBYTEMETHODA] = new String("CallStaticByteMethodA");
        names[CALLSTATICCHARMETHOD] = new String("CallStaticCharMethod");
        names[CALLSTATICCHARMETHODV] = new String("CallStaticCharMethodV");
        names[CALLSTATICCHARMETHODA] = new String("CallStaticCharMethodA");
        names[CALLSTATICSHORTMETHOD] = new String("CallStaticShortMethod");
        names[CALLSTATICSHORTMETHODV] = new String("CallStaticShortMethodV");
        names[CALLSTATICSHORTMETHODA] = new String("CallStaticShortMethodA");
        names[CALLSTATICINTMETHOD] = new String("CallStaticIntMethod");
        names[CALLSTATICINTMETHODV] = new String("CallStaticIntMethodV");
        names[CALLSTATICINTMETHODA] = new String("CallStaticIntMethodA");
        names[CALLSTATICLONGMETHOD] = new String("CallStaticLongMethod");
        names[CALLSTATICLONGMETHODV] = new String("CallStaticLongMethodV");
        names[CALLSTATICLONGMETHODA] = new String("CallStaticLongMethodA");
        names[CALLSTATICFLOATMETHOD] = new String("CallStaticFloatMethod");
        names[CALLSTATICFLOATMETHODV] = new String("CallStaticFloatMethodV");
        names[CALLSTATICFLOATMETHODA] = new String("CallStaticFloatMethodA");
        names[CALLSTATICDOUBLEMETHOD] = new String("CallStaticDoubleMethod");
        names[CALLSTATICDOUBLEMETHODV] = new String("CallStaticDoubleMethodV");
        names[CALLSTATICDOUBLEMETHODA] = new String("CallStaticDoubleMethodA");
        names[CALLSTATICVOIDMETHOD] = new String("CallStaticVoidMethod");
        names[CALLSTATICVOIDMETHODV] = new String("CallStaticVoidMethodV");
        names[CALLSTATICVOIDMETHODA] = new String("CallStaticVoidMethodA");
        names[GETSTATICFIELDID] = new String("GetStaticFieldID");
        names[GETSTATICOBJECTFIELD] = new String("GetStaticObjectField");
        names[GETSTATICBOOLEANFIELD] = new String("GetStaticBooleanField");
        names[GETSTATICBYTEFIELD] = new String("GetStaticByteField");
        names[GETSTATICCHARFIELD] = new String("GetStaticCharField");
        names[GETSTATICSHORTFIELD] = new String("GetStaticShortField");
        names[GETSTATICINTFIELD] = new String("GetStaticIntField");
        names[GETSTATICLONGFIELD] = new String("GetStaticLongField");
        names[GETSTATICFLOATFIELD] = new String("GetStaticFloatField");
        names[GETSTATICDOUBLEFIELD] = new String("GetStaticDoubleField");
        names[SETSTATICOBJECTFIELD] = new String("SetStaticObjectField");
        names[SETSTATICBOOLEANFIELD] = new String("SetStaticBooleanField");
        names[SETSTATICBYTEFIELD] = new String("SetStaticByteField");
        names[SETSTATICCHARFIELD] = new String("SetStaticCharField");
        names[SETSTATICSHORTFIELD] = new String("SetStaticShortField");
        names[SETSTATICINTFIELD] = new String("SetStaticIntField");
        names[SETSTATICLONGFIELD] = new String("SetStaticLongField");
        names[SETSTATICFLOATFIELD] = new String("SetStaticFloatField");
        names[SETSTATICDOUBLEFIELD] = new String("SetStaticDoubleField");
        names[NEWSTRING] = new String("NewString");
        names[GETSTRINGLENGTH] = new String("GetStringLength");
        names[GETSTRINGCHARS] = new String("GetStringChars");
        names[RELEASESTRINGCHARS] = new String("ReleaseStringChars");
        names[NEWSTRINGUTF] = new String("NewStringUTF");
        names[GETSTRINGUTFLENGTH] = new String("GetStringUTFLength");
        names[GETSTRINGUTFCHARS] = new String("GetStringUTFChars");
        names[RELEASESTRINGUTFCHARS] = new String("ReleaseStringUTFChars");
        names[GETARRAYLENGTH] = new String("GetArrayLength");
        names[NEWOBJECTARRAY] = new String("NewObjectArray");
        names[GETOBJECTARRAYELEMENT] = new String("GetObjectArrayElement");
        names[SETOBJECTARRAYELEMENT] = new String("SetObjectArrayElement");
        names[NEWBOOLEANARRAY] = new String("NewBooleanArray");
        names[NEWBYTEARRAY] = new String("NewByteArray");
        names[NEWCHARARRAY] = new String("NewCharArray");
        names[NEWSHORTARRAY] = new String("NewShortArray");
        names[NEWINTARRAY] = new String("NewIntArray");
        names[NEWLONGARRAY] = new String("NewLongArray");
        names[NEWFLOATARRAY] = new String("NewFloatArray");
        names[NEWDOUBLEARRAY] = new String("NewDoubleArray");
        names[GETBOOLEANARRAYELEMENTS] = new String("GetBooleanArrayElements");
        names[GETBYTEARRAYELEMENTS] = new String("GetByteArrayElements");
        names[GETCHARARRAYELEMENTS] = new String("GetCharArrayElements");
        names[GETSHORTARRAYELEMENTS] = new String("GetShortArrayElements");
        names[GETINTARRAYELEMENTS] = new String("GetIntArrayElements");
        names[GETLONGARRAYELEMENTS] = new String("GetLongArrayElements");
        names[GETFLOATARRAYELEMENTS] = new String("GetFloatArrayElements");
        names[GETDOUBLEARRAYELEMENTS] = new String("GetDoubleArrayElements");
        names[RELEASEBOOLEANARRAYELEMENTS] = new String("ReleaseBooleanArrayElements");
        names[RELEASEBYTEARRAYELEMENTS] = new String("ReleaseByteArrayElements");
        names[RELEASECHARARRAYELEMENTS] = new String("ReleaseCharArrayElements");
        names[RELEASESHORTARRAYELEMENTS] = new String("ReleaseShortArrayElements");
        names[RELEASEINTARRAYELEMENTS] = new String("ReleaseIntArrayElements");
        names[RELEASELONGARRAYELEMENTS] = new String("ReleaseLongArrayElements");
        names[RELEASEFLOATARRAYELEMENTS] = new String("ReleaseFloatArrayElements");
        names[RELEASEDOUBLEARRAYELEMENTS] = new String("ReleaseDoubleArrayElements");
        names[GETBOOLEANARRAYREGION] = new String("GetBooleanArrayRegion");
        names[GETBYTEARRAYREGION] = new String("GetByteArrayRegion");
        names[GETCHARARRAYREGION] = new String("GetCharArrayRegion");
        names[GETSHORTARRAYREGION] = new String("GetShortArrayRegion");
        names[GETINTARRAYREGION] = new String("GetIntArrayRegion");
        names[GETLONGARRAYREGION] = new String("GetLongArrayRegion");
        names[GETFLOATARRAYREGION] = new String("GetFloatArrayRegion");
        names[GETDOUBLEARRAYREGION] = new String("GetDoubleArrayRegion");
        names[SETBOOLEANARRAYREGION] = new String("SetBooleanArrayRegion");
        names[SETBYTEARRAYREGION] = new String("SetByteArrayRegion");
        names[SETCHARARRAYREGION] = new String("SetCharArrayRegion");
        names[SETSHORTARRAYREGION] = new String("SetShortArrayRegion");
        names[SETINTARRAYREGION] = new String("SetIntArrayRegion");
        names[SETLONGARRAYREGION] = new String("SetLongArrayRegion");
        names[SETFLOATARRAYREGION] = new String("SetFloatArrayRegion");
        names[SETDOUBLEARRAYREGION] = new String("SetDoubleArrayRegion");
        names[REGISTERNATIVES] = new String("RegisterNatives");
        names[UNREGISTERNATIVES] = new String("UnregisterNatives");
        names[MONITORENTER] = new String("MonitorEnter");
        names[MONITOREXIT] = new String("MonitorExit");
        names[GETJAVAVM] = new String("GetJavaVM");
        names[GETSTRINGREGION] = new String("GetStringRegion");
        names[GETSTRINGUTFREGION] = new String("GetStringUTFRegion");
        names[GETPRIMITIVEARRAYCRITICAL] = new String("GetPrimitiveArrayCritical");
        names[RELEASEPRIMITIVEARRAYCRITICAL] = new String("ReleasePrimitiveArrayCritical");
        names[GETSTRINGCRITICAL] = new String("GetStringCritical");
        names[RELEASESTRINGCRITICAL] = new String("ReleaseStringCritical");
        names[NEWWEAKGLOBALREF] = new String("NewWeakGlobalRef");
        names[DELETEWEAKGLOBALREF] = new String("DeleteWeakGlobalRef");
        names[EXCEPTIONCHECK] = new String("ExceptionCheck");
        return names;
    }

    /**
   * Get a VM_Field of an object given the index for this field
   * @param obj an Object
   * @param fieldIndex an index into the VM_Field array that describes the fields of this object
   * @return the VM_Field pointed to by the index, or null if the index or the object is invalid
   *
   */
    public static VM_Field getFieldAtIndex(Object obj, int fieldIndex) {
        VM_Type objType = VM_Magic.getObjectType(obj);
        if (objType.isClassType()) {
            VM_Field[] fields = objType.asClass().getInstanceFields();
            if (fieldIndex >= fields.length) {
                return null;
            } else {
                return fields[fieldIndex];
            }
        } else {
            return null;
        }
    }

    /**
   * Common code shared by the JNI functions NewObjectA, NewObjectV, NewObject
   * (object creation)
   * @param methodID the method ID for a constructor
   * @return a new object created by the specified constructor
   * @see VM_JNIFunctions.CallStaticIntMethod, VM_JNIFunctions.CallStaticLongMethod, etc.
   */
    public static Object invokeInitializer(Class cls, int methodID, int argAddress, boolean isJvalue, boolean isDotDotStyle) throws Exception {
        VM_Method mth = VM_MethodDictionary.getValue(methodID);
        VM_Type[] argTypes = mth.getParameterTypes();
        Class[] argClasses = new Class[argTypes.length];
        for (int i = 0; i < argClasses.length; i++) {
            argClasses[i] = argTypes[i].getClassForType();
        }
        Constructor constMethod = cls.getConstructor(argClasses);
        if (constMethod == null) throw new Exception("Constructor not found");
        int varargAddress;
        if (isDotDotStyle) varargAddress = getVarArgAddress(false); else varargAddress = argAddress;
        Object argObjs[];
        if (isJvalue) argObjs = packageParameterFromJValue(mth, argAddress); else argObjs = packageParameterFromVarArg(mth, varargAddress);
        Object newobj = constMethod.newInstance(argObjs);
        return newobj;
    }

    /**
   * Common code shared by the JNI functions CallStatic<type>Method
   * (static method invocation)
   * @param methodID the method ID
   * @param expectReturnType the return type of the method to be invoked
   * @return an object that may be the return object or a wrapper for the primitive return value 
   * @see VM_JNIFunctions.CallStaticIntMethod, VM_JNIFunctions.CallStaticLongMethod, etc.
   */
    public static Object invokeWithDotDotVarArg(int methodID, VM_Type expectReturnType) throws Exception {
        VM_Magic.pragmaNoOptCompile();
        VM_Magic.pragmaNoInline();
        int varargAddress = getVarArgAddress(false);
        return packageAndInvoke(null, methodID, varargAddress, expectReturnType, false, true);
    }

    /**
   * Common code shared by the JNI functions Call<type>Method
   * (virtual method invocation)
   * @param obj the object instance 
   * @param methodID the method ID
   * @param expectReturnType the return type for checking purpose
   * @param skip4Args  true if the calling JNI Function takes 4 args before the vararg
   *                   false if the calling JNI Function takes 3 args before the vararg
   * @return an object that may be the return object or a wrapper for the primitive return value 
   * @see VM_JNIFunctions.CallIntMethod, VM_JNIFunctions.CallLongMethod, etc.
   */
    public static Object invokeWithDotDotVarArg(Object obj, int methodID, VM_Type expectReturnType, boolean skip4Args) throws Exception {
        VM_Magic.pragmaNoOptCompile();
        VM_Magic.pragmaNoInline();
        int varargAddress = getVarArgAddress(skip4Args);
        return packageAndInvoke(obj, methodID, varargAddress, expectReturnType, skip4Args, true);
    }

    /**
   * This method supports var args passed from C
   *
   * In the Linux Intel C convention, the caller places the args immediately above the
   * saved return address, starting with the first arg
   *
   *
   *
   *
   * For the JNI functions that takes var args, their prolog code will save the
   * var arg in the glue frame because the values in the register may be lost by 
   * subsequent calls.
   *
   * This method copies the var arg values that were saved earlier in glue frame into
   * the spill area of the original caller, thereby doing the work that the callee
   * normally performs in the AIX C convention.
   *
   * NOTE: This method contains internal stack pointer.
   * For now we assume that the stack will not be relocatable while native code is running
   * because native code can hold an address into the stack, so this code is OK,
   * but this is an issue to be resolved later
   *
   * NOTE:  this method assumes that it is immediately above the 
   * invokeWithDotDotVarArg frame, the JNI frame, the glue frame and 
   * the C caller frame in the respective order.  
   * Therefore, this method will not work if called from anywhere else
   *
   *  low address
   *
   *   |  fp  | <- VM_JNIEnvironment.getVarArgAddress
   *   | mid  |
   *   |      |
   *   |      |
   *   |------|   
   *   |  fp  | <- VM_JNIEnvironment.invokeWithDotDotVarArg frame
   *   | mid  |
   *   | ...  |
   *   |      |
   *   |      |
   *   |------|   
   *   |  fp  | <- JNI method frame
   *   | mid  |
   *   | ...  |
   *   | arg 0|    args copied by JNI prolog (3 for static, nonvirtual, 
   *   | arg 1|    or 4 for virtual)
   *   | arg 2|
   *   |      |
   *   |      |
   *   |------|
   *   | fp   | <- Native C caller frame
   *   |return|
   *   | arg 0|    
   *   | arg 1|    
   *   | arg 2|
   *   | arg 3|
   *   | arg 4|
   *   | arg 5|
   *   | arg 6|
   *   | arg 7|
   *   | arg 8|    
   *   | arg 9|
   *   |      |
   *   |      |
   *   |      |
   *
   *
   *   high address
   *
   *
   * @param skip4Args if true, the calling JNI function has 4 args before the vararg
   *                  if false, the calling JNI function has 3 args before the vararg
   * @return the starting address of the vararg in the caller stack frame
   */
    private static int getVarArgAddress(boolean skip4Args) {
        int fp = VM_Magic.getMemoryWord(VM_Magic.getFramePointer());
        fp = VM_Magic.getMemoryWord(fp);
        return (fp + 2 * 4 + (skip4Args ? 4 * 4 : 3 * 4));
    }

    /**
   * Common code shared by the JNI functions CallStatic<type>MethodV
   * @param methodID the method ID
   * @param argAddress a raw address for the variable argument list
   * @return an object that may be the return object or a wrapper for the primitive return value 
   * @see VM_JNIFunctions.CallStaticIntMethodV, VM_JNIFunctions.CallStaticLongMethodV, etc.
   */
    public static Object invokeWithVarArg(int methodID, int argAddress, VM_Type expectReturnType) throws Exception {
        return packageAndInvoke(null, methodID, argAddress, expectReturnType, false, true);
    }

    /**
   * Common code shared by the JNI functions Call<type>MethodV
   * @param obj the object instance 
   * @param methodID the method ID
   * @param argAddress a raw address for the variable argument list
   * @param expectReturnType the return type for checking purpose
   * @param skip4Args received from the JNI function, passed on to VM_Reflection.invoke()
   * @return an object that may be the return object or a wrapper for the primitive return value 
   * @see VM_JNIFunctions.CallStaticIntMethodV, VM_JNIFunctions.CallStaticLongMethodV, etc.
   */
    public static Object invokeWithVarArg(Object obj, int methodID, int argAddress, VM_Type expectReturnType, boolean skip4Args) throws Exception {
        return packageAndInvoke(obj, methodID, argAddress, expectReturnType, skip4Args, true);
    }

    /**
   * Common code shared by the JNI functions CallStatic<type>MethodA
   * @param methodID an index into the VM_MethodDictionary
   * @param argAddress a raw address for the argument array
   * @return an object that may be the return object or a wrapper for the primitive return value 
   * @see VM_JNIFunctions.CallStaticIntMethodA, VM_JNIFunctions.CallStaticLongMethodA, etc.
   */
    public static Object invokeWithJValue(int methodID, int argAddress, VM_Type expectReturnType) throws Exception {
        return packageAndInvoke(null, methodID, argAddress, expectReturnType, false, false);
    }

    /**
   * Common code shared by the JNI functions Call<type>MethodA
   * @param obj the object instance 
   * @param methodID an index into the VM_MethodDictionary
   * @param argAddress a raw address for the argument array
   * @param expectReturnType the return type for checking purpose
   * @param skip4Args received from the JNI function, passed on to VM_Reflection.invoke()
   * @return an object that may be the return object or a wrapper for the primitive return value 
   * @see VM_JNIFunctions.CallStaticIntMethodA, VM_JNIFunctions.CallStaticLongMethodA, etc.
   */
    public static Object invokeWithJValue(Object obj, int methodID, int argAddress, VM_Type expectReturnType, boolean skip4Args) throws Exception {
        return packageAndInvoke(obj, methodID, argAddress, expectReturnType, skip4Args, false);
    }

    /**
   * Common code shared by invokeWithJValue, invokeWithVarArg and invokeWithDotDotVarArg
   * @param obj the object instance 
   * @param methodID an index into the VM_MethodDictionary
   * @param argAddress a raw address for the argument array
   * @param expectReturnType the return type for checking purpose
   * @param skip4Args This flag is received from the JNI function and passed directly to 
   *                     VM_Reflection.invoke().  
   *                     It is true if the actual method is to be invoked, which could be
   *                     from the superclass.
   *                     It is false if the method from the real class of the object 
   *                     is to be invoked, which may not be the actual method specified by methodID
   * @param isVarArg  This flag describes whether the array of parameters is in var arg format or
   *                  jvalue format
   * @return an object that may be the return object or a wrapper for the primitive return value 
   * @see invokeWithJValue, invokeWithVarArg, invokeWithDotDotVarArg
   */
    public static Object packageAndInvoke(Object obj, int methodID, int argAddress, VM_Type expectReturnType, boolean skip4Args, boolean isVarArg) throws Exception {
        VM_Magic.pragmaNoOptCompile();
        VM_Magic.pragmaNoInline();
        VM_Method targetMethod;
        int returnValue;
        targetMethod = VM_MethodDictionary.getValue(methodID);
        VM_Type returnType = targetMethod.getReturnType();
        if (expectReturnType == null) {
            if (!returnType.isReferenceType()) throw new Exception("Wrong return type for method: expect reference type instead of " + returnType);
        } else {
            if (returnType != expectReturnType) throw new Exception("Wrong return type for method: expect " + expectReturnType + " instead of " + returnType);
        }
        Object[] argObjectArray;
        if (isVarArg) {
            argObjectArray = packageParameterFromVarArg(targetMethod, argAddress);
        } else {
            argObjectArray = packageParameterFromJValue(targetMethod, argAddress);
        }
        Object returnObj = VM_Reflection.invoke(targetMethod, obj, argObjectArray, skip4Args);
        return returnObj;
    }

    /**
   * Repackage the arguments passed as a variable argument list into an array of Object,
   * used by the JNI functions CallStatic<type>MethodV
   * @param mth the target VM_Method
   * @param argAddress an address into the C space for the array of jvalue unions;  
   *                   each element is 2-word and holds the argument of the appropriate type
   * @return an Object array holding the arguments wrapped at Objects
   * @see packageAndInvoke()
   */
    static Object[] packageParameterFromVarArg(VM_Method targetMethod, int argAddress) {
        VM_Type[] argTypes = targetMethod.getParameterTypes();
        int argCount = argTypes.length;
        Object[] argObjectArray = new Object[argCount];
        VM_JNIEnvironment env = VM_Thread.getCurrentThread().getJNIEnv();
        for (int i = 0, addr = argAddress; i < argCount; i++) {
            int loword, hiword;
            loword = VM_Magic.getMemoryWord(addr);
            addr += 4;
            if (argTypes[i].isFloatType()) {
                hiword = VM_Magic.getMemoryWord(addr);
                addr += 4;
                long doubleBits = (((long) hiword) << 32) | (loword & 0xFFFFFFFFL);
                argObjectArray[i] = VM_Reflection.wrapFloat((float) (Double.longBitsToDouble(doubleBits)));
            } else if (argTypes[i].isDoubleType()) {
                hiword = VM_Magic.getMemoryWord(addr);
                addr += 4;
                long doubleBits = (((long) hiword) << 32) | (loword & 0xFFFFFFFFL);
                argObjectArray[i] = VM_Reflection.wrapDouble(Double.longBitsToDouble(doubleBits));
            } else if (argTypes[i].isLongType()) {
                hiword = VM_Magic.getMemoryWord(addr);
                addr += 4;
                long longValue = (((long) hiword) << 32) | (loword & 0xFFFFFFFFL);
                argObjectArray[i] = VM_Reflection.wrapLong(longValue);
            } else if (argTypes[i].isBooleanType()) {
                argObjectArray[i] = VM_Reflection.wrapBoolean(loword);
            } else if (argTypes[i].isByteType()) {
                argObjectArray[i] = VM_Reflection.wrapByte((byte) loword);
            } else if (argTypes[i].isCharType()) {
                argObjectArray[i] = VM_Reflection.wrapChar((char) loword);
            } else if (argTypes[i].isShortType()) {
                argObjectArray[i] = VM_Reflection.wrapShort((short) loword);
            } else if (argTypes[i].isReferenceType()) {
                argObjectArray[i] = env.getJNIRef(loword);
            } else if (argTypes[i].isIntType()) {
                argObjectArray[i] = VM_Reflection.wrapInt(loword);
            } else {
                return null;
            }
        }
        return argObjectArray;
    }

    /**
   * Repackage the arguments passed as an array of jvalue into an array of Object,
   * used by the JNI functions CallStatic<type>MethodA
   * @param mth the target VM_Method
   * @param argAddress an address into the C space for the array of jvalue unions;  
   *                   each element is 2-word and holds the argument of the appropriate type
   * @return an Object array holding the arguments wrapped at Objects
   */
    static Object[] packageParameterFromJValue(VM_Method targetMethod, int argAddress) {
        VM_Type[] argTypes = targetMethod.getParameterTypes();
        int argCount = argTypes.length;
        Object[] argObjectArray = new Object[argCount];
        VM_JNIEnvironment env = VM_Thread.getCurrentThread().getJNIEnv();
        for (int i = 0, addr = argAddress; i < argCount; i++, addr += 8) {
            int loword, hiword;
            loword = VM_Magic.getMemoryWord(addr);
            if (argTypes[i].isFloatType()) {
                argObjectArray[i] = VM_Reflection.wrapFloat(Float.intBitsToFloat(loword));
            } else if (argTypes[i].isDoubleType()) {
                hiword = VM_Magic.getMemoryWord(addr + 4);
                long doubleBits = (((long) hiword) << 32) | (loword & 0xFFFFFFFFL);
                argObjectArray[i] = VM_Reflection.wrapDouble(Double.longBitsToDouble(doubleBits));
            } else if (argTypes[i].isLongType()) {
                hiword = VM_Magic.getMemoryWord(addr + 4);
                long longValue = (((long) hiword) << 32) | (loword & 0xFFFFFFFFL);
                argObjectArray[i] = VM_Reflection.wrapLong(longValue);
            } else if (argTypes[i].isBooleanType()) {
                argObjectArray[i] = VM_Reflection.wrapBoolean(loword & 0x000000FF);
            } else if (argTypes[i].isByteType()) {
                argObjectArray[i] = VM_Reflection.wrapByte((byte) (loword & 0x000000FF));
            } else if (argTypes[i].isCharType()) {
                argObjectArray[i] = VM_Reflection.wrapChar((char) (loword & 0x0000FFFF));
            } else if (argTypes[i].isShortType()) {
                argObjectArray[i] = VM_Reflection.wrapShort((short) (loword & 0x0000FFFF));
            } else if (argTypes[i].isReferenceType()) {
                argObjectArray[i] = env.getJNIRef(loword);
            } else if (argTypes[i].isIntType()) {
                argObjectArray[i] = VM_Reflection.wrapInt(loword);
            } else {
                return null;
            }
        }
        return argObjectArray;
    }

    /**
   * Given an address in C that points to a null-terminated string,
   * create a new Java byte[] with a copy of the string
   * @param stringAddress an address in C space for a string
   * @return a new Java byte[]
   */
    static byte[] createByteArrayFromC(int stringAddress) {
        int word;
        int length = 0;
        int addr = stringAddress;
        while (true) {
            word = VM_Magic.getMemoryWord(addr);
            int byte3 = ((word >> 24) & 0xFF);
            int byte2 = ((word >> 16) & 0xFF);
            int byte1 = ((word >> 8) & 0xFF);
            int byte0 = (word & 0xFF);
            if (byte0 == 0) break;
            length++;
            if (byte1 == 0) break;
            length++;
            if (byte2 == 0) break;
            length++;
            if (byte3 == 0) break;
            length++;
            addr += 4;
        }
        byte[] contents = new byte[length];
        VM_Memory.memcopy(VM_Magic.objectAsAddress(contents), stringAddress, length);
        return contents;
    }

    /**
   * Given an address in C that points to a null-terminated string,
   * create a new Java String with a copy of the string
   * @param stringAddress an address in C space for a string
   * @return a new Java String
   */
    static String createStringFromC(int stringAddress) {
        byte[] contents = createByteArrayFromC(stringAddress);
        return new String(contents);
    }

    public void dumpJniRefsStack() {
        int jniRefOffset = JNIRefsTop;
        VM.sysWrite("\n* * dump of JNIEnvironment JniRefs Stack * *\n");
        VM.sysWrite("* JNIRefs = ");
        VM.sysWriteHex(VM_Magic.objectAsAddress(JNIRefs));
        VM.sysWrite(" * JNIRefsTop = ");
        VM.sysWrite(JNIRefsTop, false);
        VM.sysWrite(" * JNIRefsSavedFP = ");
        VM.sysWrite(JNIRefsSavedFP, false);
        VM.sysWrite(".\n*\n");
        while (jniRefOffset >= 0) {
            VM.sysWrite(jniRefOffset, false);
            VM.sysWrite(" ");
            VM.sysWriteHex(VM_Magic.objectAsAddress(JNIRefs) + jniRefOffset);
            VM.sysWrite(" ");
            VM_GCUtil.dumpRef(JNIRefs[jniRefOffset >> 2]);
            jniRefOffset -= 4;
        }
        VM.sysWrite("\n* * end of dump * *\n");
    }
}
