package org.powermock.modules.testng.internal;

import javassist.util.proxy.ProxyFactory;
import org.powermock.core.MockRepository;
import org.powermock.core.classloader.MockClassLoader;
import org.powermock.core.transformers.MockTransformer;
import org.powermock.core.transformers.impl.MainMockTransformer;
import org.powermock.modules.testng.PowerMockTestCase;
import org.powermock.reflect.Whitebox;
import org.powermock.reflect.proxyframework.RegisterProxyFramework;
import org.powermock.tests.utils.IgnorePackagesExtractor;
import org.powermock.tests.utils.TestClassesExtractor;
import org.powermock.tests.utils.impl.MockPolicyInitializerImpl;
import org.powermock.tests.utils.impl.PowerMockIgnorePackagesExtractorImpl;
import org.powermock.tests.utils.impl.PrepareForTestExtractorImpl;
import org.testng.IObjectFactory;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class PowerMockClassloaderObjectFactory implements IObjectFactory {

    private final MockClassLoader mockLoader;

    private final TestClassesExtractor testClassesExtractor;

    private final IgnorePackagesExtractor ignorePackagesExtractor;

    public PowerMockClassloaderObjectFactory() {
        List<MockTransformer> mockTransformerChain = new ArrayList<MockTransformer>();
        final MainMockTransformer mainMockTransformer = new MainMockTransformer();
        mockTransformerChain.add(mainMockTransformer);
        String[] classesToLoadByMockClassloader = new String[0];
        String[] packagesToIgnore = new String[0];
        mockLoader = new MockClassLoader(classesToLoadByMockClassloader, packagesToIgnore);
        mockLoader.setMockTransformerChain(mockTransformerChain);
        testClassesExtractor = new PrepareForTestExtractorImpl();
        ignorePackagesExtractor = new PowerMockIgnorePackagesExtractorImpl();
    }

    public Object newInstance(@SuppressWarnings("rawtypes") Constructor constructor, Object... params) {
        MockRepository.clear();
        Class<?> testClass = constructor.getDeclaringClass();
        mockLoader.addIgnorePackage(ignorePackagesExtractor.getPackagesToIgnore(testClass));
        mockLoader.addClassesToModify(testClassesExtractor.getTestClasses(testClass));
        try {
            registerProxyframework(mockLoader);
            new MockPolicyInitializerImpl(testClass).initialize(mockLoader);
            final Class<?> testClassLoadedByMockedClassLoader = createTestClass(testClass);
            Constructor<?> con = testClassLoadedByMockedClassLoader.getConstructor(constructor.getParameterTypes());
            final Object testInstance = con.newInstance(params);
            if (!extendsPowerMockTestCase(testClass)) {
                setInvocationHandler(testInstance);
            }
            return testInstance;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setInvocationHandler(Object testInstance) throws Exception {
        Class<?> powerMockTestNGMethodHandlerClass = Class.forName(PowerMockTestNGMethodHandler.class.getName(), false, mockLoader);
        Object powerMockTestNGMethodHandlerInstance = powerMockTestNGMethodHandlerClass.getConstructor(Class.class).newInstance(testInstance.getClass());
        Whitebox.invokeMethod(testInstance, "setHandler", powerMockTestNGMethodHandlerInstance);
    }

    /**
	 * We proxy the test class in order to be able to clear state after each
	 * test method invocation. It would be much better to be able to register a
	 * testng listener programmtically but I cannot find a way to do so.
	 */
    private Class<?> createTestClass(Class<?> actualTestClass) throws Exception {
        final Class<?> testClassLoadedByMockedClassLoader = Class.forName(actualTestClass.getName(), false, mockLoader);
        if (extendsPowerMockTestCase(actualTestClass)) {
            return testClassLoadedByMockedClassLoader;
        } else {
            Class<?> proxyFactoryClass = Class.forName(ProxyFactory.class.getName(), false, mockLoader);
            final Class<?> testNGMethodFilterByMockedClassLoader = Class.forName(TestNGMethodFilter.class.getName(), false, mockLoader);
            Object f = proxyFactoryClass.newInstance();
            Object filter = testNGMethodFilterByMockedClassLoader.newInstance();
            Whitebox.invokeMethod(f, "setFilter", filter);
            Whitebox.invokeMethod(f, "setSuperclass", testClassLoadedByMockedClassLoader);
            Class<?> c = Whitebox.invokeMethod(f, "createClass");
            return c;
        }
    }

    private boolean extendsPowerMockTestCase(Class<?> actualTestClass) {
        return PowerMockTestCase.class.isAssignableFrom(actualTestClass);
    }

    private void registerProxyframework(ClassLoader classLoader) {
        Class<?> proxyFrameworkClass = null;
        try {
            proxyFrameworkClass = Class.forName("org.powermock.api.extension.proxyframework.ProxyFrameworkImpl", false, classLoader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Extension API internal error: org.powermock.api.extension.proxyframework.ProxyFrameworkImpl could not be located in classpath.");
        }
        Class<?> proxyFrameworkRegistrar = null;
        try {
            proxyFrameworkRegistrar = Class.forName(RegisterProxyFramework.class.getName(), false, classLoader);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            Whitebox.invokeMethod(proxyFrameworkRegistrar, "registerProxyFramework", Whitebox.newInstance(proxyFrameworkClass));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
