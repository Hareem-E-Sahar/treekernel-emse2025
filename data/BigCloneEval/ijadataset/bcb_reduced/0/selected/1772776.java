package org.grandtestauto.test;

import org.grandtestauto.*;
import org.grandtestauto.test.dataconstants.org.grandtestauto.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * Unit test for <code>CoverageUnitTester</code>.
 *
 * @author Tim Lavers
 */
public class CoverageUnitTesterTest {

    private static Set<String> methodsCalled = new HashSet<String>();

    private static Set<String> testsCreated = new HashSet<String>();

    public static void ping(String methodCalled) {
        methodsCalled.add(methodCalled);
    }

    public static void recordTestCreated(Class testClass) {
        testsCreated.add(testClass.getName());
    }

    public boolean autoCleanupTest() throws Exception {
        methodsCalled = new HashSet<String>();
        CoverageUnitTester cut = cutForConfiguredPackage(Grandtestauto.test102_zip, "a102.test", null, null, null, null, null);
        System.out.println(">>>>>> EXPECTED STACK TRACE START <<<<<<");
        cut.runTests();
        System.out.println(">>>>>> EXPECTED STACK TRACE END <<<<<<");
        Set<String> expected = new HashSet<String>();
        expected.add("a102.test.ATest.cleanup");
        Helpers.assertEqual(methodsCalled, expected);
        return true;
    }

    public boolean autoCleanupThrowsExceptionTest() throws Exception {
        methodsCalled = new HashSet<String>();
        CoverageUnitTester cut = cutForConfiguredPackage(Grandtestauto.test103_zip, "a103.test", null, null, null, null, null);
        System.out.println(">>>>>> EXPECTED STACK TRACE START <<<<<<");
        cut.runTests();
        Set<String> expected = new HashSet<String>();
        expected.add("a103.test.ATest.cleanup");
        expected.add("a103.test.ATest.extraTest");
        System.out.println(">>>>>> EXPECTED STACK TRACE END <<<<<<");
        Helpers.assertEqual(methodsCalled, expected);
        String logFileContents = Helpers.logFileContents();
        String msgForError = Messages.message(Messages.OPK_CLEANUP_THREW_THROWABLE, "Throwable deliberately thrown in test.");
        assert logFileContents.contains(msgForError);
        return true;
    }

    public boolean autoCleanupMethodNotPublicTest() throws Exception {
        methodsCalled = new HashSet<String>();
        CoverageUnitTester cut = cutForConfiguredPackage(Grandtestauto.test104_zip, "a104.test", null, null, null, null, null);
        System.out.println(">>>>>> EXPECTED STACK TRACE START <<<<<<");
        cut.runTests();
        System.out.println(">>>>>> EXPECTED STACK TRACE END <<<<<<");
        Helpers.assertEqual(methodsCalled.size(), 0);
        String logFileContents = Helpers.logFileContents();
        String msgForError = Messages.message(Messages.SK_CLEANUP_NOT_PUBLIC);
        assert !logFileContents.contains(msgForError);
        return true;
    }

    public boolean autoCleanupMethodNotNoArgsTest() throws Exception {
        methodsCalled = new HashSet<String>();
        CoverageUnitTester cut = cutForConfiguredPackage(Grandtestauto.test105_zip, "a105.test", null, null, null, null, null);
        System.out.println(">>>>>> EXPECTED STACK TRACE START <<<<<<");
        cut.runTests();
        System.out.println(">>>>>> EXPECTED STACK TRACE END <<<<<<");
        Helpers.assertEqual(methodsCalled.size(), 0);
        String logFileContents = Helpers.logFileContents();
        String msgForError = Messages.message(Messages.SK_CLEANUP_NOT_NO_ARGS);
        assert logFileContents.contains(msgForError);
        return true;
    }

    public boolean unitTestIdentificationTest() throws Exception {
        init();
        CoverageUnitTester cut = cutForConfiguredPackage(Grandtestauto.test46_zip, "a46.test", null, null, null, null, null);
        cut.runTests();
        assert testsCreated.contains("a46.test.XTest");
        assert testsCreated.contains("a46.test.ATest");
        String logFileContents = Helpers.logFileContents();
        String msgForYTest = Messages.message(Messages.OPK_COULD_NOT_CREATE_TEST_CLASS, "a46.test.YTest");
        assert logFileContents.contains(msgForYTest);
        String msgForZTest = Messages.message(Messages.OPK_COULD_NOT_CREATE_TEST_CLASS, "a46.test.ZTest");
        assert logFileContents.contains(msgForZTest);
        return true;
    }

    public boolean runTestsTest() throws Exception {
        init();
        CoverageUnitTester cut = cutForConfiguredPackage(Grandtestauto.test46_zip, "a46.test", null, null, null, null, null);
        cut.runTests();
        assert testsCreated.contains("a46.test.XTest");
        assert testsCreated.contains("a46.test.ATest");
        String logFileContents = Helpers.logFileContents();
        String msgForYTest = Messages.message(Messages.OPK_COULD_NOT_CREATE_TEST_CLASS, "a46.test.YTest");
        assert logFileContents.contains(msgForYTest);
        String msgForZTest = Messages.message(Messages.OPK_COULD_NOT_CREATE_TEST_CLASS, "a46.test.ZTest");
        assert logFileContents.contains(msgForZTest);
        methodsCalled = new HashSet<String>();
        cut = cutForConfiguredPackage(Grandtestauto.test17_zip, "a17.test", null, null, null, null, null);
        cut.runTests();
        Set<String> expected = new HashSet<String>();
        expected.add("mTest");
        expected.add("nTest");
        Helpers.assertEqual(methodsCalled, expected);
        cut = cutForConfiguredPackage(Grandtestauto.test14_zip, "a14.test", null, null, null, null, null);
        assert !cut.runTests();
        System.out.println(">>>>>> EXPECTED STACK TRACE START <<<<<<");
        cut = cutForConfiguredPackage(Grandtestauto.test23_zip, "a23.test", null, null, null, null, null);
        assert !cut.runTests();
        System.out.println(">>>>>> EXPECTED STACK TRACE END <<<<<<");
        cut = cutForConfiguredPackage(Grandtestauto.test5_zip, "a5.test", null, null, null, null, null);
        assert !cut.runTests();
        logFileContents = Helpers.logFileContents();
        String msgForMissingTest = "In a5 the following method is not unit-tested:" + Helpers.NL + "public void a5.X.m()";
        assert logFileContents.contains(msgForMissingTest) : logFileContents;
        cut = cutForConfiguredPackage(Grandtestauto.test26_zip, "a26.test", null, null, null, null, null);
        assert !cut.runTests();
        logFileContents = Helpers.logFileContents();
        msgForMissingTest = "In a26 the following method is not unit-tested:" + Helpers.NL + "protected void a26.A.a()";
        assert logFileContents.contains(msgForMissingTest) : logFileContents;
        cut = cutForConfiguredPackage(Grandtestauto.test27_zip, "a27.test", null, null, null, null, null);
        assert !cut.runTests();
        logFileContents = Helpers.logFileContents();
        msgForMissingTest = "In a27 the following method is not unit-tested:" + Helpers.NL + "protected void a27.A.a()";
        assert logFileContents.contains(msgForMissingTest) : logFileContents;
        cut = cutForConfiguredPackage(Grandtestauto.test28_zip, "a28.test", null, null, null, null, null);
        assert !cut.runTests();
        logFileContents = Helpers.logFileContents();
        msgForMissingTest = "In a28 the following method is not unit-tested:" + Helpers.NL + "protected void a28.A.a()";
        assert logFileContents.contains(msgForMissingTest) : logFileContents;
        cut = cutForConfiguredPackage(Grandtestauto.test32_zip, "a32.test", null, null, null, null, null);
        assert !cut.runTests();
        logFileContents = Helpers.logFileContents();
        msgForMissingTest = "In a32 the following method is not unit-tested:" + Helpers.NL + "protected void a32.A.a()";
        assert logFileContents.contains(msgForMissingTest) : logFileContents;
        cut = cutForConfiguredPackage(Grandtestauto.test29_zip, "a29.test", null, null, null, null, null);
        assert cut.runTests();
        cut = cutForConfiguredPackage(Grandtestauto.test30_zip, "a30.test", null, null, null, null, null);
        assert cut.runTests();
        cut = cutForConfiguredPackage(Grandtestauto.test31_zip, "a31.test", null, null, null, null, null);
        assert cut.runTests();
        cut = cutForConfiguredPackage(Grandtestauto.test18_zip, "a18.test", null, null, null, null, null);
        assert cut.runTests();
        cut = cutForConfiguredPackage(Grandtestauto.test19_zip, "a19.test", null, null, null, null, null);
        assert cut.runTests();
        cut = cutForConfiguredPackage(Grandtestauto.test19_zip, "a19.test", null, null, null, null, null);
        assert cut.runTests();
        cut = cutForConfiguredPackage(Grandtestauto.test47_zip, "a47.test", null, null, null, null, null);
        cut.runTests();
        logFileContents = Helpers.logFileContents();
        String listOfUntestedMethods = "In a47 the following methods are not unit-tested:" + Helpers.NL + "public void a47.Y.c(java.lang.String[],int,int[])" + Helpers.NL + "public void a47.X.b(int,int)" + Helpers.NL + "public void a47.W.a(java.lang.String).";
        assert logFileContents.contains(listOfUntestedMethods) : "Got: '" + logFileContents;
        cut = cutForConfiguredPackage(Grandtestauto.test16_zip, "a16.test", null, null, null, null, null);
        methodsCalled = new HashSet<String>();
        assert cut.runTests();
        Helpers.assertEqual(methodsCalled.size(), 4);
        Set<String> expectedMethodsCalled = new HashSet<String>();
        expectedMethodsCalled.add("a_String_Test");
        expectedMethodsCalled.add("a_StringArray_Test");
        expectedMethodsCalled.add("a_StringArray_StringArray_Test");
        expectedMethodsCalled.add("a_String_StringArray_Test");
        Helpers.assertEqual(methodsCalled, expectedMethodsCalled);
        cut = cutForConfiguredPackage(Grandtestauto.test48_zip, "a48.test", null, null, null, null, null);
        assert cut.runTests();
        cut = cutForConfiguredPackage(Grandtestauto.test4_zip, "a4.test", null, null, null, null, null);
        assert !cut.runTests();
        logFileContents = Helpers.logFileContents();
        msgForMissingTest = "In a4 the following class is not unit-tested:" + Helpers.NL + "X";
        assert logFileContents.contains(msgForMissingTest) : logFileContents;
        cut = cutForConfiguredPackage(Grandtestauto.test24_zip, "a24.test", null, null, null, null, null);
        assert cut.runTests();
        cut = cutForConfiguredPackage(Grandtestauto.test22_zip, "a22.test", null, null, null, null, null);
        assert cut.runTests();
        cut = cutForConfiguredPackage(Grandtestauto.test86_zip, "a86.test", null, null, null, null, null);
        assert cut.runTests();
        cut = cutForConfiguredPackage(Grandtestauto.test87_zip, "a87.test", null, null, null, null, null);
        assert cut.runTests();
        cut = cutForConfiguredPackage(Grandtestauto.test12_zip, "a12.test", null, null, null, null, null);
        assert !cut.runTests();
        logFileContents = Helpers.logFileContents();
        msgForMissingTest = "In a12 the following constructor was not tested:" + Helpers.NL + "protected a12.X()";
        assert logFileContents.contains(msgForMissingTest) : logFileContents;
        cut = cutForConfiguredPackage(Grandtestauto.test49_zip, "a49.test", null, null, null, null, null);
        assert !cut.runTests();
        logFileContents = Helpers.logFileContents();
        System.out.println("resultsFileContents = " + logFileContents);
        String listOfUntestedConstructors = "In a49 the following constructors were not tested:" + Helpers.NL + "public a49.W(java.lang.String)" + Helpers.NL + "public a49.X(int,int)" + Helpers.NL + "public a49.Y(java.lang.String[],int,int[])" + Helpers.NL + "public a49.Z(java.lang.String).";
        assert logFileContents.contains(listOfUntestedConstructors);
        cut = cutForConfiguredPackage(Grandtestauto.test50_zip, "a50.test", null, null, null, null, null);
        assert !cut.runTests();
        cut = cutForConfiguredPackage(Grandtestauto.test11_zip, "a11.test", null, null, null, null, null);
        assert !cut.runTests();
        cut = cutForConfiguredPackage(Grandtestauto.test13_zip, "a13.test", null, null, null, null, null);
        assert !cut.runTests();
        cut = cutForConfiguredPackage(Grandtestauto.test33_zip, "a33.test", null, null, null, null, null);
        assert cut.runTests();
        cut = cutForConfiguredPackage(Grandtestauto.test95_zip, "a95.test", null, null, null, null, null);
        assert !cut.runTests();
        System.out.println(">>>>>> EXPECTED STACK TRACE START <<<<<<");
        cut = cutForConfiguredPackage(Grandtestauto.test96_zip, "a96.test", null, null, null, null, null);
        boolean runResult = cut.runTests();
        System.out.println(">>>>>> EXPECTED STACK END START <<<<<<");
        assert !runResult;
        cut = cutForConfiguredPackage(Grandtestauto.test98_zip, "a98.test", null, null, null, null, null);
        assert !cut.runTests();
        System.out.println(">>>>>> EXPECTED STACK TRACE START <<<<<<");
        cut = cutForConfiguredPackage(Grandtestauto.test97_zip, "a97.test", null, null, null, null, null);
        runResult = cut.runTests();
        System.out.println(">>>>>> EXPECTED STACK END START <<<<<<");
        assert !runResult;
        cut = cutForConfiguredPackage(Grandtestauto.test101_zip, "a101.test", null, null, null, null, null);
        assert !cut.runTests();
        return true;
    }

    /**
     * This test is to help find a problem with one of the function tests.
     */
    public boolean a1Test() {
        CoverageUnitTester cut = cutForConfiguredPackage(Grandtestauto.test1_zip, "a1.test", null, null, null, null, null);
        assert !cut.runTests();
        String log = Helpers.logFileContents();
        String errMsg = "In a1 the following classes are not unit-tested:" + Helpers.NL + "Y" + Helpers.NL + "X.";
        assert log.contains(errMsg) : "Got: '" + log + "'";
        return true;
    }

    public boolean overriddentParametrisedMethodTest() {
        CoverageUnitTester cut = cutForConfiguredPackage(Grandtestauto.test84_zip, "a84.test", null, null, null, null, null);
        assert cut.runTests() : "log was: " + Helpers.logFileContents();
        return true;
    }

    public boolean restrictionOfMethodsRunTest() {
        CoverageUnitTester cut = cutForConfiguredPackage(Grandtestauto.test83_zip, "a83.test", null, null, null, null, null);
        methodsCalled.clear();
        cut.runTests();
        Set<String> expected = new HashSet<String>();
        expected.add("constructor_A_Test");
        expected.add("aTest");
        expected.add("bTest");
        expected.add("cTest");
        expected.add("dTest");
        expected.add("eTest");
        expected.add("constructor_B_Test");
        expected.add("xTest");
        expected.add("x_int_Test");
        expected.add("x_String_Test");
        expected.add("yTest");
        expected.add("y_intArray_Test");
        assert methodsCalled.equals(expected) : "Got: " + methodsCalled;
        cut = cutForConfiguredPackage(Grandtestauto.test83_zip, "a83.test", "a83", "A", "b", "dTest", null);
        methodsCalled.clear();
        cut.runTests();
        expected = new HashSet<String>();
        expected.add("bTest");
        expected.add("cTest");
        expected.add("constructor_A_Test");
        expected.add("dTest");
        assert methodsCalled.equals(expected) : "Got: " + methodsCalled;
        cut = cutForConfiguredPackage(Grandtestauto.test83_zip, "a83.test", "a83", "A", null, "d", null);
        methodsCalled.clear();
        cut.runTests();
        expected = new HashSet<String>();
        expected.add("aTest");
        expected.add("bTest");
        expected.add("cTest");
        expected.add("constructor_A_Test");
        assert methodsCalled.equals(expected) : "Got: " + methodsCalled;
        cut = cutForConfiguredPackage(Grandtestauto.test83_zip, "a83.test", "a83", "A", "c", null, null);
        methodsCalled.clear();
        cut.runTests();
        expected = new HashSet<String>();
        expected.add("cTest");
        expected.add("constructor_A_Test");
        expected.add("dTest");
        expected.add("eTest");
        assert methodsCalled.equals(expected) : "Got: " + methodsCalled;
        cut = cutForConfiguredPackage(Grandtestauto.test83_zip, "a83.test", "a83", "A", null, null, "cTest");
        methodsCalled.clear();
        cut.runTests();
        expected = new HashSet<String>();
        expected.add("cTest");
        assert methodsCalled.equals(expected) : "Got: " + methodsCalled;
        cut = cutForConfiguredPackage(Grandtestauto.test83_zip, "a83.test", "a83", "A", "a", "d", "cTest");
        methodsCalled.clear();
        cut.runTests();
        expected = new HashSet<String>();
        expected.add("cTest");
        assert methodsCalled.equals(expected) : "Got: " + methodsCalled;
        return true;
    }

    public boolean constructorTest() throws Exception {
        GrandTestAuto gta = Helpers.setupForZip(Grandtestauto.test4_zip);
        Class<?> ut = Class.forName("a4.test.UnitTester");
        Field flag = ut.getDeclaredField("flag");
        assert !flag.getBoolean(null) : "Flag not initially false";
        Constructor constructor = ut.getConstructor(GrandTestAuto.class);
        CoverageUnitTester cut = (CoverageUnitTester) constructor.newInstance(gta);
        assert !cut.runTests();
        return true;
    }

    public boolean noPrintoutOfMissingTestsWhenTestsAreRestrictedTest() {
        CoverageUnitTester cut = cutForConfiguredPackage(Grandtestauto.test83_zip, "a83.test", "a83", "A", "b", "dTest", null);
        cut.runTests();
        String log = Helpers.logFileContents();
        System.out.println("log = " + log);
        return true;
    }

    private CoverageUnitTester cutForConfiguredPackage(String zipName, String packageName, String singlePackage, String singleTest, String initialTestMethod, String finalTestMethod, String singleTestMethod) {
        GrandTestAuto gta = Helpers.setupForZip(new File(zipName), true, true, true, null, null, singlePackage, false, true, Helpers.defaultLogFile().getPath(), null, null, null, singleTest, initialTestMethod, finalTestMethod, singleTestMethod);
        try {
            Class<?> ut = Class.forName(packageName + ".UnitTester");
            Constructor ctr = ut.getConstructor(GrandTestAuto.class);
            return (CoverageUnitTester) ctr.newInstance(gta);
        } catch (Exception e) {
            e.printStackTrace();
            assert false : "See above stack trace";
        }
        return null;
    }

    private void init() {
        methodsCalled = new HashSet<String>();
        testsCreated = new HashSet<String>();
    }
}
