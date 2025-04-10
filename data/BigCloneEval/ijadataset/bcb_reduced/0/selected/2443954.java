package edu.byu.ece.edif.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * A test suite for testing the EDIF package. This class runs tests defined in
 * classes that extend AbstractTestClass. Any methods annotated with
 * 
 * @Test in such classes will be run as tests.
 * @author Jonathan Johnson
 * @version $Id: TestSuite.java 4 2008-04-16 22:31:52Z mrspud $
 */
public class TestSuite {

    /**
     * Construct a new TestSuite for running tests.
     */
    public TestSuite() {
        _testClasses = new ArrayList();
        _results = new ArrayList();
        _exceptionMap = new TreeMap();
    }

    /**
     * Add a new TestClass to the TestSuite.
     * 
     * @Test methods in testClass will be run by the TestSuite.
     * @param testClass the class to add to the TestSuite
     */
    public void addTest(Class testClass) {
        _testClasses.add(testClass);
    }

    /**
     * @return a Map containing any exceptions thrown by the test methods or
     * <code>null</code> if no exceptions were thrown. The key is a String
     * representing the name of the Method that threw the exception. The value
     * is a String describing the exception (stack trace and type).
     */
    public Map getExceptions() {
        if (_exceptionMap.size() == 0) return null; else return _exceptionMap;
    }

    /**
     * Run tests in all the test classes that have been added to the TestSuite.
     * 
     * @return a Collection of TestResult objects containing the results of the
     * tests.
     */
    public Collection run() {
        for (Object currentClassObj : _testClasses) {
            boolean hasSetup = false;
            int setupIndex = 0;
            Class currentClass = (Class) currentClassObj;
            Method[] methods = currentClass.getMethods();
            Arrays.sort((Object[]) methods, new methodComparator());
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("setup")) {
                    hasSetup = true;
                    setupIndex = i;
                }
            }
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].isAnnotationPresent(Test.class)) {
                    _className = currentClass.getName();
                    if (_className.contains(".")) _className = _className.substring(_className.lastIndexOf(".") + 1, _className.length());
                    _methodName = methods[i].getName();
                    _currentResult = TestStatus.UNTESTED;
                    try {
                        Constructor constructor = currentClass.getConstructor(new Class[] { this.getClass() });
                        Object newTestObject = constructor.newInstance(new Object[] { this });
                        if (hasSetup) methods[setupIndex].invoke(newTestObject, (Object[]) null);
                        methods[i].invoke(newTestObject, (Object[]) null);
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        _currentResult = TestStatus.EXCEPTION;
                        StackTraceElement[] stackTrace = e.getCause().getStackTrace();
                        StringBuilder sb = new StringBuilder();
                        for (int j = 0; j < stackTrace.length; j++) {
                            sb.append(stackTrace[stackTrace.length - 1 - j].toString());
                            sb.append("\n");
                        }
                        sb.append(e.getCause().toString());
                        _exceptionMap.put(methods[i].getName(), sb.toString());
                    }
                    _results.add(new TestResult(_className, _methodName, _currentResult));
                }
            }
        }
        return _results;
    }

    /**
     * Add an error if the passed in objects are not equal by the
     * <code>==</code> operator.
     * 
     * @param obj1 first object to compare
     * @param obj2 second object to compare
     */
    protected void assertEqual(Object obj1, Object obj2) {
        TestStatus result;
        if (obj1 == obj2) result = TestStatus.PASSED; else result = TestStatus.FAILED;
        if (result == TestStatus.FAILED) _currentResult = TestStatus.FAILED; else if (_currentResult == TestStatus.UNTESTED && result == TestStatus.PASSED) _currentResult = TestStatus.PASSED;
    }

    /**
     * Add an error if the passed in boolean is not <code>false</code>.
     * 
     * @param condition the boolean to test
     */
    public void assertFalse(boolean condition) {
        if (condition == true) _currentResult = TestStatus.FAILED; else if (_currentResult == TestStatus.UNTESTED) _currentResult = TestStatus.PASSED;
    }

    /**
     * Add an error if the passed in boolean is not <code>true</code>.
     * 
     * @param condition the boolean to test
     */
    public void assertTrue(boolean condition) {
        if (condition == false) _currentResult = TestStatus.FAILED; else if (_currentResult == TestStatus.UNTESTED) _currentResult = TestStatus.PASSED;
    }

    /**
     * Add an error if the passed in objects are equal by the <code>==</code>
     * operator.
     * 
     * @param obj1 first object to compare
     * @param obj2 second object to compare
     */
    public void assertUnequal(Object obj1, Object obj2) {
        TestStatus result;
        if (obj1 == obj2) result = TestStatus.FAILED; else result = TestStatus.PASSED;
        if (result == TestStatus.FAILED) _currentResult = TestStatus.FAILED; else if (_currentResult == TestStatus.UNTESTED && result == TestStatus.PASSED) _currentResult = TestStatus.PASSED;
    }

    /**
     * The name of the TestClass currently being processed
     */
    private String _className;

    /**
     * A Map containing the exceptions thrown by test methods. The key is a
     * String representing the name of the Method that threw the exception. The
     * value is a String describing the exception (stack trace and type).
     */
    private Map _exceptionMap;

    /**
     * The name of the test method currently being run
     */
    private String _methodName;

    /**
     * A Collection of TestResult objects describing all test results
     */
    private Collection _results;

    /**
     * Contains the current result of the current test method running. This will
     * be set to <code>true</code> at the beginning of each method run and
     * only set to false if one the the assert methods of the test method fails.
     */
    private TestStatus _currentResult;

    /**
     * A Collection of TestClass objects which define tests to be run
     */
    private Collection _testClasses;

    /**
     * A Comparator implementation to compare methods by their name so they can
     * be sorted in alphabetical order.
     */
    private class methodComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            return ((Method) o1).getName().compareTo(((Method) o2).getName());
        }
    }
}
