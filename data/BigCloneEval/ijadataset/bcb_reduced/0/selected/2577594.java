package net.sf.stump.api.runner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import net.sf.stump.api.junit.StumpTestCase;
import net.sf.stump.api.junit4.PreparePreview;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

/**
 * @author Joni Freeman
 * @author Paul Mogren
 */
public class Launcher {

    @SuppressWarnings("unchecked")
    public Throwable launch(String fqn) {
        StumpTestCase.setServersEnabled(false);
        try {
            Class<?> testClass = getClass().getClassLoader().loadClass(fqn);
            TestResult results = new TestResult();
            Class<? extends Annotation> annotRunWith;
            try {
                annotRunWith = (Class<? extends Annotation>) getClass().getClassLoader().loadClass("org.junit.runner.RunWith");
            } catch (ClassNotFoundException e) {
                annotRunWith = null;
            }
            boolean suiteMethodPresent = false;
            try {
                Method suiteMethod = testClass.getMethod("suite");
                suiteMethodPresent = suiteMethod.getReturnType().isAssignableFrom(Test.class) && Modifier.isPublic(suiteMethod.getModifiers()) && Modifier.isStatic(suiteMethod.getModifiers());
            } catch (Exception noSuitableSuiteMethod) {
            }
            if ((annotRunWith == null || !testClass.isAnnotationPresent(annotRunWith)) && (testClass.isAssignableFrom(TestCase.class) || suiteMethodPresent)) {
                Method[] launchMethods = findLaunchMethods(testClass);
                TestSuite suite = new TestSuite();
                for (Method method : launchMethods) {
                    suite.addTest(createTest(testClass, method));
                }
                suite.run(results);
            } else {
                JUnit4TestAdapter adapter = new JUnit4TestAdapter(testClass);
                Filter filter = new Filter() {

                    @Override
                    public String describe() {
                        return "Wicket Stump launch filter";
                    }

                    @Override
                    public boolean shouldRun(Description description) {
                        return null != description.getAnnotation(PreparePreview.class);
                    }
                };
                adapter.filter(filter);
                adapter.run(results);
            }
            if (results.errorCount() > 0) {
                return (results.errors().nextElement()).thrownException();
            }
            return null;
        } catch (Throwable t) {
            return t;
        }
    }

    private TestCase createTest(Class<?> testClass, Method method) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Constructor<?> constructor = testClass.getConstructor();
        TestCase test = (TestCase) constructor.newInstance();
        test.setName(method.getName());
        return test;
    }

    protected Method[] findLaunchMethods(Class<?> testClass) {
        List<Method> launchMethods = new ArrayList<Method>();
        for (Method method : testClass.getMethods()) {
            if (method.getName().startsWith("launch")) {
                launchMethods.add(method);
            }
        }
        return launchMethods.toArray(new Method[launchMethods.size()]);
    }
}
