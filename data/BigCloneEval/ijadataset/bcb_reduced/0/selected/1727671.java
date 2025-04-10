package org.databene.contiperf.junit;

import java.lang.reflect.Constructor;
import org.junit.Before;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;

/**
 * Parent class for tests that verify ContiPerf functionality.<br/><br/>
 * Created: 24.05.2010 06:18:21
 * @since 1.05
 * @author Volker Bergmann
 */
public abstract class AbstractContiPerfTest {

    protected boolean finished;

    protected boolean failed;

    protected boolean assumptionFailed;

    protected boolean ignored;

    @Before
    public void setUp() {
        finished = false;
        failed = false;
        assumptionFailed = false;
        ignored = false;
    }

    protected void runTest(Class<?> testClass) throws Exception {
        RunWith runWith = testClass.getAnnotation(RunWith.class);
        if (runWith != null) runAnnotatedTestClass(testClass, runWith); else runPlainTestClass(testClass);
    }

    private void runPlainTestClass(Class<?> testClass) throws Exception {
        BlockJUnit4ClassRunner runner = new BlockJUnit4ClassRunner(testClass);
        RunNotifier notifier = new RunNotifier();
        notifier.addListener(new MyListener());
        runner.run(notifier);
    }

    private void runAnnotatedTestClass(Class<?> testClass, RunWith runWith) throws Exception {
        Class<? extends Runner> runnerClass = runWith.value();
        Constructor<? extends Runner> constructor = runnerClass.getConstructor(Class.class);
        Runner runner = constructor.newInstance(testClass);
        RunNotifier notifier = new RunNotifier();
        notifier.addListener(new MyListener());
        runner.run(notifier);
    }

    protected class MyListener extends RunListener {

        @Override
        public void testFinished(Description description) throws Exception {
            finished = true;
        }

        @Override
        public void testFailure(Failure failure) throws Exception {
            failed = true;
        }

        @Override
        public void testAssumptionFailure(Failure failure) {
            assumptionFailed = true;
        }

        @Override
        public void testIgnored(Description description) throws Exception {
            ignored = true;
        }
    }
}
