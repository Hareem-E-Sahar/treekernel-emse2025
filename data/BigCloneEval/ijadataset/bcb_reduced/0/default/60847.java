import java.lang.reflect.Method;

class InvokeReflect {

    static String methodToRun = "run";

    static String signatureToPrintOut = "()Z";

    static Class[] noparams = {};

    public static void main(String argv[]) throws Exception {
        if (argv.length == 0) {
            printUsage();
            return;
        }
        for (int iArg = 0; iArg < argv.length; iArg++) {
            String arg = argv[iArg];
            if (arg.startsWith("-h")) {
                printUsage();
                return;
            }
            System.out.println("**** START OF EXECUTION of " + arg + "." + methodToRun + " " + signatureToPrintOut + " ****.");
            Class klass = Class.forName(arg);
            Method method = klass.getDeclaredMethod(methodToRun, noparams);
            Object result = method.invoke(null, noparams);
            System.out.println("**** RESULT: " + result);
        }
    }

    static boolean run() throws Exception {
        methodToRun = "sampleRun";
        String[] sampleArgv = { "InvokeReflect", "-h" };
        System.out.println("Running `InvokeReflect InvokeReflect -h'");
        main(sampleArgv);
        return true;
    }

    static boolean sampleRun() {
        System.out.println("This is InvokeReflect.sampleRun()");
        return true;
    }

    static void printUsage() {
        System.out.println("\nUsage:  InvokeReflect className ...\n");
    }
}
