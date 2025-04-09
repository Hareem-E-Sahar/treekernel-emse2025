package org.starobjects.tested.integ.junit4;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.jmock.Mockery;
import org.junit.internal.runners.InitializationError;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.internal.runners.MethodRoadie;
import org.junit.internal.runners.TestMethod;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.nakedobjects.applib.fixtures.LogonFixture;
import org.nakedobjects.authentication.standard.file.PasswordFileAuthenticationManagerInstaller;
import org.nakedobjects.commons.factory.InstanceFactory;
import org.nakedobjects.config.ConfigurationConstants;
import org.nakedobjects.config.NakedObjectConfiguration;
import org.nakedobjects.config.loader.ConfigurationLoader;
import org.nakedobjects.context.NakedObjectsContext;
import org.nakedobjects.installers.InstallerLookupImpl;
import org.nakedobjects.metamodel.authentication.AuthenticationSession;
import org.nakedobjects.persistence.PersistenceSession;
import org.nakedobjects.services.ServicesInjector;
import org.nakedobjects.system.DeploymentType;
import org.nakedobjects.system.internal.InitialisationSession;
import org.nakedobjects.transaction.NakedObjectTransactionManager;
import org.nakedobjects.viewer.headless.applib.HeadlessViewer;
import org.starobjects.tested.doclib.DocumentUsing;
import org.starobjects.tested.doclib.Documentor;
import org.starobjects.tested.documentor.Constants;
import org.starobjects.tested.documentor.DocumentProxyInteractionsListener;
import org.starobjects.tested.documentor.DocumentorFactory;
import org.starobjects.tested.documentor.memory.InMemoryDocumentor;
import org.starobjects.tested.framework.config.TestedConfigurationLoader;
import org.starobjects.tested.integ.junit4.internal.NakedObjectsSystemUsingInstallersWithinJunit;

/**
 * Copied from JMock, and with the same support.
 * 
 */
public class NakedObjectsTestRunner extends JUnit4ClassRunner {

    private final Field mockeryField;

    private Documentor documentor;

    private final DocumentProxyInteractionsListener documentProxyInteractionsListener;

    /**
     * Only used during object construction.
     */
    public NakedObjectsTestRunner(final Class<?> testClass) throws InitializationError {
        super(testClass);
        ConfigurationLoader initConfigurationLoader = new TestedConfigurationLoader();
        NakedObjectConfiguration initConfiguration = initConfigurationLoader.load();
        final Documentor requested = getDocumentor(testClass, initConfiguration);
        documentProxyInteractionsListener = new DocumentProxyInteractionsListener(requested);
        documentor = documentProxyInteractionsListener;
        mockeryField = findFieldAndMakeAccessible(testClass, Mockery.class);
    }

    @Override
    protected void invokeTestMethod(final Method method, final RunNotifier notifier) {
        NakedObjectsSystemUsingInstallersWithinJunit system = null;
        AuthenticationSession session = null;
        final Description description = methodDescription(method);
        try {
            final DeploymentType deploymentType = DeploymentType.PROTOTYPE;
            ConfigurationLoader configurationLoader = new TestedConfigurationLoader();
            InstallerLookupImpl installerLookup = new InstallerLookupImpl(getClass());
            installerLookup.setConfigurationLoader(configurationLoader);
            system = new NakedObjectsSystemUsingInstallersWithinJunit(deploymentType, installerLookup, getTestClass(), documentor);
            system.setHideSplash(!false);
            system.setAuthenticatorInstaller(installerLookup.configure(new PasswordFileAuthenticationManagerInstaller()));
            system.init();
            runTestMethod(method, notifier, description, system);
        } catch (final InvocationTargetException e) {
            notifier.testAborted(description, e.getCause());
            getTransactionManager().abortTransaction();
            return;
        } catch (final Exception e) {
            notifier.testAborted(description, e);
            return;
        } finally {
            if (system != null) {
                if (session != null) {
                    NakedObjectsContext.closeSession();
                }
                system.shutdown();
            }
        }
    }

    private void runTestMethod(final Method method, final RunNotifier notifier, final Description description, final NakedObjectsSystemUsingInstallersWithinJunit system) throws Exception {
        HeadlessViewer embeddedViewer = system.getHeadlessViewer();
        embeddedViewer.addInteractionListener(documentProxyInteractionsListener);
        NakedObjectsContext.openSession(new InitialisationSession());
        final LogonFixture sessionFixture = system.getAnnotatedClassFixturesInstaller().getLogonFixture();
        if (sessionFixture != null) {
            sessionFixture.install();
        }
        getTransactionManager().startTransaction();
        Object test = createTest();
        getServicesManager().injectDependencies(test);
        documentor.beginTest(method.getName());
        try {
            final TestMethod testMethod = wrapMethod(method);
            new MethodRoadie(test, testMethod, notifier, description).run();
            getTransactionManager().endTransaction();
        } finally {
            documentor.endTest();
        }
    }

    /**
     * Taken from JMock's runner.
     */
    @Override
    protected TestMethod wrapMethod(final Method method) {
        return new TestMethod(method, getTestClass()) {

            @Override
            public void invoke(final Object testFixture) throws IllegalAccessException, InvocationTargetException {
                super.invoke(testFixture);
                if (mockeryField != null) {
                    mockeryOf(testFixture).assertIsSatisfied();
                }
            }
        };
    }

    private Documentor getDocumentor(final Class<?> testClass, NakedObjectConfiguration configuration) throws InitializationError {
        Documentor documentor = getDocumentorFromConfiguration(testClass.getCanonicalName(), configuration);
        if (documentor == null) {
            getDocumentorFromAnnotation(testClass, configuration);
        }
        if (documentor == null) {
            documentor = new InMemoryDocumentor(configuration, testClass.getCanonicalName());
        }
        return documentor;
    }

    public Documentor getDocumentorFromConfiguration(String classUnderTest, NakedObjectConfiguration configuration) {
        final String factoryName = configuration.getString(ConfigurationConstants.ROOT + Constants.DOCUMENTOR_FACTORY);
        if (factoryName != null) {
            final DocumentorFactory documentorFactory = InstanceFactory.createInstance(factoryName, DocumentorFactory.class);
            configuration.injectInto(documentorFactory);
            return documentorFactory.newDocumentor(classUnderTest);
        }
        return null;
    }

    private Documentor getDocumentorFromAnnotation(final Class<?> testClass, NakedObjectConfiguration configuration) throws InitializationError {
        final DocumentUsing documentUsingAnnotation = testClass.getAnnotation(DocumentUsing.class);
        if (documentUsingAnnotation == null) {
            return null;
        }
        final Class<? extends Documentor> documentorClass = documentUsingAnnotation.value();
        try {
            Constructor<? extends Documentor> constructor = documentorClass.getConstructor(new Class[] { NakedObjectConfiguration.class, String.class });
            return constructor.newInstance(configuration, testClass.getCanonicalName());
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
        }
        try {
            return documentorClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * JMock code.
     * 
     * @param test
     * @return
     */
    protected Mockery mockeryOf(final Object test) {
        if (mockeryField == null) {
            return null;
        }
        try {
            final Mockery mockery = (Mockery) mockeryField.get(test);
            if (mockery == null) {
                throw new IllegalStateException(String.format("Mockery named '%s' is null", mockeryField.getName()));
            }
            return mockery;
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException(String.format("cannot get value of field %s", mockeryField.getName()), e);
        }
    }

    /**
     * Adapted from JMock code.
     */
    static Field findFieldAndMakeAccessible(final Class<?> testClass, final Class<?> clazz) throws InitializationError {
        for (Class<?> c = testClass; c != Object.class; c = c.getSuperclass()) {
            for (final Field field : c.getDeclaredFields()) {
                if (clazz.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    return field;
                }
            }
        }
        return null;
    }

    @Override
    protected void finalize() throws Throwable {
        documentor.close();
        super.finalize();
    }

    private static PersistenceSession getPersistenceSession() {
        return NakedObjectsContext.getPersistenceSession();
    }

    private static ServicesInjector getServicesManager() {
        return getPersistenceSession().getServicesInjector();
    }

    private static NakedObjectTransactionManager getTransactionManager() {
        return getPersistenceSession().getTransactionManager();
    }
}
