package org.junit.internal.runners;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;

/**
 * @deprecated Included for backwards compatibility with JUnit 4.4. Will be
 *             removed in the next release. Please use
 *             {@link BlockJUnit4ClassRunner} in place of {@link JUnit4ClassRunner}.
 *             
 *             This may disappear as soon as 1 April 2009
 */
@Deprecated
public class JUnit4ClassRunner extends Runner implements Filterable, Sortable {

    private final List<Method> fTestMethods;

    private TestClass fTestClass;

    public JUnit4ClassRunner(Class<?> klass) throws InitializationError {
        fTestClass = new TestClass(klass);
        fTestMethods = getTestMethods();
        validate();
    }

    protected List<Method> getTestMethods() {
        return fTestClass.getTestMethods();
    }

    protected void validate() throws InitializationError {
        MethodValidator methodValidator = new MethodValidator(fTestClass);
        methodValidator.validateMethodsForDefaultRunner();
        methodValidator.assertValid();
    }

    @Override
    public void run(final RunNotifier notifier) {
        new ClassRoadie(notifier, fTestClass, getDescription(), new Runnable() {

            public void run() {
                runMethods(notifier);
            }
        }).runProtected();
    }

    protected void runMethods(final RunNotifier notifier) {
        for (Method method : fTestMethods) invokeTestMethod(method, notifier);
    }

    @Override
    public Description getDescription() {
        Description spec = Description.createSuiteDescription(getName(), classAnnotations());
        List<Method> testMethods = fTestMethods;
        for (Method method : testMethods) spec.addChild(methodDescription(method));
        return spec;
    }

    protected Annotation[] classAnnotations() {
        return fTestClass.getJavaClass().getAnnotations();
    }

    protected String getName() {
        return getTestClass().getName();
    }

    protected Object createTest() throws Exception {
        return getTestClass().getConstructor().newInstance();
    }

    protected void invokeTestMethod(Method method, RunNotifier notifier) {
        Description description = methodDescription(method);
        Object test;
        try {
            test = createTest();
        } catch (InvocationTargetException e) {
            testAborted(notifier, description, e.getCause());
            return;
        } catch (Exception e) {
            testAborted(notifier, description, e);
            return;
        }
        TestMethod testMethod = wrapMethod(method);
        new MethodRoadie(test, testMethod, notifier, description).run();
    }

    private void testAborted(RunNotifier notifier, Description description, Throwable e) {
        notifier.fireTestStarted(description);
        notifier.fireTestFailure(new Failure(description, e));
        notifier.fireTestFinished(description);
    }

    protected TestMethod wrapMethod(Method method) {
        return new TestMethod(method, fTestClass);
    }

    protected String testName(Method method) {
        return method.getName();
    }

    protected Description methodDescription(Method method) {
        return Description.createTestDescription(getTestClass().getJavaClass(), testName(method), testAnnotations(method));
    }

    protected Annotation[] testAnnotations(Method method) {
        return method.getAnnotations();
    }

    public void filter(Filter filter) throws NoTestsRemainException {
        for (Iterator<Method> iter = fTestMethods.iterator(); iter.hasNext(); ) {
            Method method = iter.next();
            if (!filter.shouldRun(methodDescription(method))) iter.remove();
        }
        if (fTestMethods.isEmpty()) throw new NoTestsRemainException();
    }

    public void sort(final Sorter sorter) {
        Collections.sort(fTestMethods, new Comparator<Method>() {

            public int compare(Method o1, Method o2) {
                return sorter.compare(methodDescription(o1), methodDescription(o2));
            }
        });
    }

    protected TestClass getTestClass() {
        return fTestClass;
    }
}
