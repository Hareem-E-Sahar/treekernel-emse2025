package framework.unitTest.base;

import framework.log.Log;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.Date;

public abstract class UnitTestBase implements Runnable {

    public static enum Status {

        UNKNOWN, CLEAN, DIRTY, UNSTABLE, ERROR
    }

    private TestProfile testProfileInstance;

    private Object targetInstance;

    private String targetClassName;

    private String testProfileName;

    private String unitTestName = getClass().getName();

    private int testCounter = 0;

    private int testsPassed = 0;

    private int exceptions = 0;

    private int testsFailed = 0;

    private Status status = Status.UNKNOWN;

    private Date startTime;

    private Date stopTime;

    protected abstract void runTests();

    public UnitTestBase(Object target, TestProfile profile) {
        if (profile == null) {
            error("null testProfile.");
            return;
        }
        if (target == null) {
            error("null target.");
            return;
        }
        testProfileInstance = profile;
        testProfileName = profile.getClass().getName();
        targetInstance = target;
        targetClassName = target.getClass().getName();
    }

    @SuppressWarnings("unchecked")
    public UnitTestBase(String targetClassName, String testProfileName) {
        String className = null;
        try {
            className = targetClassName;
            Class targetClass = Class.forName(targetClassName);
            className = testProfileName;
            Class testProfile = Class.forName(testProfileName);
            getInstances(targetClass, testProfile);
        } catch (ClassNotFoundException exception) {
            error("Class not found: " + className);
        }
    }

    public UnitTestBase(Class targetClass, Class<TestProfile> testProfile) {
        getInstances(targetClass, testProfile);
    }

    public UnitTestBase() {
        Class targetClass = null;
        Class<? extends TestProfile> testProfile = null;
        Annotation[] annotations = this.getClass().getAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Class<? extends Annotation> type = annotations[i].annotationType();
            if (type.equals(framework.unitTest.base.annotations.TargetObject.class)) targetClass = ((framework.unitTest.base.annotations.TargetObject) annotations[i]).value();
            if (type.equals(framework.unitTest.base.annotations.TestProfile.class)) testProfile = ((framework.unitTest.base.annotations.TestProfile) annotations[i]).value();
        }
        getInstances(targetClass, testProfile);
    }

    private void getInstances(Class<?> targetClass, Class<? extends TestProfile> testProfile) {
        if (testProfile == null) {
            error("You must specify a testProfile. Look at another unit test to see how to do this.");
            return;
        }
        if (targetClass == null) {
            error("You must specify a targetClass. Look at another unit test to see how to do this.");
            return;
        }
        testProfileName = testProfile.getName();
        targetClassName = targetClass.getName();
        if (!isTestProfile(testProfile)) {
            error(testProfileName + " must implement the 'TestProfile' interface.");
            return;
        }
        String clazz = null;
        try {
            clazz = testProfileName;
            Constructor<? extends TestProfile> profileConstructor = testProfile.getConstructor();
            testProfileInstance = profileConstructor.newInstance();
            clazz = targetClassName;
            Constructor<?> targetConstructor = targetClass.getConstructor();
            targetInstance = targetConstructor.newInstance();
        } catch (NoSuchMethodException exception) {
            error("Could not instantiate " + clazz + ". It must have a no-argument constructor.");
        } catch (InstantiationException exception) {
            error("Could not instantiate " + clazz + ". Exception during target object initialization.");
            Log.out(exception);
        } catch (IllegalAccessException exception) {
            error("Could not instantiate " + clazz + ". Make sure its constructor is public.");
        } catch (InvocationTargetException exception) {
            error("Could not instantiate " + clazz + ".");
        }
    }

    private boolean isTestProfile(Class toCheck) {
        if (toCheck == null) return false;
        Class[] interfaces = toCheck.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) if (interfaces[i].equals(TestProfile.class)) return true;
        return false;
    }

    public void main(String[] args) {
        this.run();
    }

    public void printResults() {
        System.out.println("Unit test:\t\t" + unitTestName);
        System.out.println("Test Profile:\t\t" + testProfileName);
        System.out.println("Target Class:\t\t" + targetClassName);
        System.out.println("Test cases run:\t\t" + testCounter);
        System.out.println("Start Time:\t\t" + startTime);
        System.out.println("Stop Time:\t\t" + stopTime);
        System.out.println("Tests passed:\t\t" + testsPassed);
        System.out.println("Tests failed:\t\t" + testsFailed);
        System.out.println("Tests excepting:\t" + exceptions);
        System.out.println("Status:\t\t\t" + status);
    }

    private void error(String message) {
        error(message, null);
    }

    private void error(String message, Exception exception) {
        this.status = Status.ERROR;
        System.out.println("UNIT TEST ERROR: " + message);
        if (exception != null) Log.out(exception);
    }

    public void run() {
        try {
            startTime = Calendar.getInstance().getTime();
            this.runTests();
        } catch (Exception exception) {
            error("Exception during execution. There is probably a problem within your unit test code.");
            Log.out(exception);
        }
        stopTime = Calendar.getInstance().getTime();
        printResults();
    }

    public Status status() {
        return status;
    }

    /**
      * Use this method to test your classes. Specify the name of the method
      * you wish to test, followed by any parameters that your method requires, 
      * followed by the expected output. 
      * 
      * If this is at all unclear, look at the example unit test for A1: 
      * cs307.unitTests.A1UnitTest
      * 
      * @param methodName the name of the method you wish to test
      * @param args   the arguments you wish to pass to your method, followed by
      *               the expected result. Acceptable arguments will vary
      *               based on the methodName supplied and the TestProfile that 
      *               your unit test is based on. 
      * @return true if test passed     
      */
    protected boolean test(String methodName, Object... args) {
        testCounter++;
        if (status == Status.ERROR) {
            System.out.println("Test " + testCounter + " not run. Unit test is in an error state.");
            return false;
        }
        try {
            System.out.println("Test Case:" + testCounter);
            TestResult result = testProfileInstance.testMethod(targetInstance, methodName, args);
            System.out.println("Input: " + result.input());
            System.out.println("Expected output: " + result.expected());
            System.out.println("Actual output: " + result.actual());
            if (result.passed()) {
                System.out.println("PASSED.\n");
                testsPassed++;
                if (status == Status.UNKNOWN) status = Status.CLEAN;
            } else {
                System.out.println("FAILED.\n");
                testsFailed++;
                if ((status == Status.CLEAN) || (status == Status.UNKNOWN)) status = Status.DIRTY;
            }
            return result.passed();
        } catch (UnitTestException exception) {
            error("Exception running test: " + exception.getMessage());
        } catch (ClassCastException exception) {
            error("Invalid parameter to method " + methodName);
        } catch (NoSuchMethodException exception) {
            error("Could not find method '" + methodName + "'");
        } catch (IllegalAccessException exception) {
            error("Illegal access exception running '" + methodName + "'. Is it public???");
        } catch (InvocationTargetException exception) {
            status = Status.UNSTABLE;
            System.out.println("***EXCEPTION***");
            exceptions++;
            Log.out(new Exception(exception.getCause()));
        }
        return false;
    }
}
