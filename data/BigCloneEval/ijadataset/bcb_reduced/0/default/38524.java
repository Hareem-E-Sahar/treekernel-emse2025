import package1.*;
import package2.*;
import package1.package3.*;
import java.io.*;
import java.util.Random;

public class Assert {

    private static Class1 testClass1;

    private static Class2 testClass2;

    private static Class3 testClass3;

    private static final boolean debug = true;

    private static Random generator = new Random();

    /**
     * The first invocation of this test starts a loop which exhaustively tests
     * the object tree with all of its different settings.
     * There are 7 test objects in a tree that has 7 different places to set
     * assertions untouched/on/off so this tests 3^7 or 2187 different
     * configurations.
     *
     * This test spawns a new VM for each run because assertions are set on or
     * off at class load time. Once the class is loaded its assertion status
     * does not change.
     */
    public static void main(String[] args) throws Exception {
        int[] switches = new int[7];
        int switchSource = 0;
        if (args.length == 0) {
            for (int x = 0; x < 100; x++) {
                int temp = generator.nextInt(2187);
                for (int i = 0; i < 7; i++) {
                    switches[i] = temp % 3;
                    temp = temp / 3;
                }
                String command = System.getProperty("java.home") + File.separator + "bin" + File.separator + "cvm Assert";
                System.out.println("Command = " + command);
                StringBuffer commandString = new StringBuffer(command);
                for (int j = 0; j < 7; j++) commandString.append(" " + switches[j]);
                Process p = null;
                p = Runtime.getRuntime().exec(commandString.toString());
                if (debug) {
                    BufferedReader blah = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String outString = blah.readLine();
                    while (outString != null) {
                        System.out.println("from slave:" + outString);
                        outString = blah.readLine();
                    }
                }
                p.waitFor();
                int result = p.exitValue();
                if (debug) {
                    if (result == 0) {
                        for (int k = 6; k >= 0; k--) System.out.print(switches[k]);
                        System.out.println();
                    } else {
                        System.out.print("Nonzero Exit: ");
                        for (int k = 6; k >= 0; k--) System.out.print(switches[k]);
                        System.out.println();
                    }
                } else {
                    if (result != 0) {
                        System.err.print("Nonzero Exit: ");
                        for (int k = 6; k >= 0; k--) System.err.print(switches[k]);
                        System.err.println();
                        throw new RuntimeException("Assertion test failure.");
                    }
                }
            }
        } else {
            for (int i = 0; i < 7; i++) switches[i] = Integer.parseInt(args[i]);
            SetAssertionSwitches(switches);
            ConstructClassTree();
            TestClassTree(switches);
        }
    }

    private static void SetAssertionSwitches(int[] switches) {
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        if (switches[0] != 0) loader.setDefaultAssertionStatus(switches[0] == 2);
        if (switches[1] != 0) loader.setPackageAssertionStatus("package1", switches[1] == 2);
        if (switches[2] != 0) loader.setPackageAssertionStatus("package2", switches[2] == 2);
        if (switches[3] != 0) loader.setPackageAssertionStatus("package1.package3", switches[3] == 2);
        if (switches[4] != 0) loader.setClassAssertionStatus("package1.Class1", switches[4] == 2);
        if (switches[5] != 0) loader.setClassAssertionStatus("package2.Class2", switches[5] == 2);
        if (switches[6] != 0) loader.setClassAssertionStatus("package1.package3.Class3", switches[6] == 2);
    }

    private static void TestClassTree(int[] switches) {
        boolean assertsOn = (switches[4] == 2) ? true : (switches[4] == 1) ? false : (switches[1] == 2) ? true : (switches[1] == 1) ? false : (switches[0] == 2) ? true : false;
        testClass1.testAssert(assertsOn);
        assertsOn = (switches[4] == 2) ? true : (switches[4] == 1) ? false : (switches[1] == 2) ? true : (switches[1] == 1) ? false : (switches[0] == 2) ? true : false;
        Class1.Class11.testAssert(assertsOn);
        assertsOn = (switches[5] == 2) ? true : (switches[5] == 1) ? false : (switches[2] == 2) ? true : (switches[2] == 1) ? false : (switches[0] == 2) ? true : false;
        testClass2.testAssert(assertsOn);
        assertsOn = (switches[6] == 2) ? true : (switches[6] == 1) ? false : (switches[3] == 2) ? true : (switches[3] == 1) ? false : (switches[1] == 2) ? true : (switches[1] == 1) ? false : (switches[0] == 2) ? true : false;
        testClass3.testAssert(assertsOn);
        assertsOn = (switches[6] == 2) ? true : (switches[6] == 1) ? false : (switches[3] == 2) ? true : (switches[3] == 1) ? false : (switches[1] == 2) ? true : (switches[1] == 1) ? false : (switches[0] == 2) ? true : false;
        Class3.Class31.testAssert(assertsOn);
    }

    private static void ConstructClassTree() {
        testClass1 = new Class1();
        testClass2 = new Class2();
        testClass3 = new Class3();
    }
}
