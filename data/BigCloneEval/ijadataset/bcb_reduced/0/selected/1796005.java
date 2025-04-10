package org.springframework.beans.factory.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import javax.mail.Session;
import javax.servlet.ServletException;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.xml.sax.InputSource;
import org.springframework.beans.DerivedTestBean;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.ITestBean;
import org.springframework.beans.IndexedTestBean;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.ResourceTestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanIsAbstractException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.DummyFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.MethodReplacer;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.SerializationTestUtils;
import org.springframework.util.StopWatch;

/**
 * Miscellaneous tests for XML bean definitions.
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 */
public class XmlBeanFactoryTests extends TestCase {

    public void testRefToSeparatePrototypeInstances() throws Exception {
        DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
        reader.setValidating(false);
        reader.loadBeanDefinitions(new ClassPathResource("reftypes.xml", getClass()));
        assertTrue("7 beans in reftypes, not " + xbf.getBeanDefinitionCount(), xbf.getBeanDefinitionCount() == 7);
        TestBean emma = (TestBean) xbf.getBean("emma");
        TestBean georgia = (TestBean) xbf.getBean("georgia");
        ITestBean emmasJenks = emma.getSpouse();
        ITestBean georgiasJenks = georgia.getSpouse();
        assertTrue("Emma and georgia think they have a different boyfriend", emmasJenks != georgiasJenks);
        assertTrue("Emmas jenks has right name", emmasJenks.getName().equals("Andrew"));
        assertTrue("Emmas doesn't equal new ref", emmasJenks != xbf.getBean("jenks"));
        assertTrue("Georgias jenks has right name", emmasJenks.getName().equals("Andrew"));
        assertTrue("They are object equal", emmasJenks.equals(georgiasJenks));
        assertTrue("They object equal direct ref", emmasJenks.equals(xbf.getBean("jenks")));
    }

    public void testRefToSingleton() throws Exception {
        DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
        reader.setValidating(false);
        Resource resource = new ClassPathResource("reftypes.xml", getClass());
        reader.loadBeanDefinitions(new EncodedResource(resource, "ISO-8859-1"));
        assertTrue("7 beans in reftypes, not " + xbf.getBeanDefinitionCount(), xbf.getBeanDefinitionCount() == 7);
        TestBean jen = (TestBean) xbf.getBean("jenny");
        TestBean dave = (TestBean) xbf.getBean("david");
        TestBean jenks = (TestBean) xbf.getBean("jenks");
        ITestBean davesJen = dave.getSpouse();
        ITestBean jenksJen = jenks.getSpouse();
        assertTrue("1 jen instance", davesJen == jenksJen);
        assertTrue("1 jen instance", davesJen == jen);
    }

    public void testInnerBeans() throws IOException {
        DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
        reader.setValidating(false);
        InputStream inputStream = getClass().getResourceAsStream("reftypes.xml");
        try {
            reader.loadBeanDefinitions(new InputSource(inputStream));
        } finally {
            inputStream.close();
        }
        TestBean hasInnerBeans = (TestBean) xbf.getBean("hasInnerBeans");
        assertEquals(5, hasInnerBeans.getAge());
        TestBean inner1 = (TestBean) hasInnerBeans.getSpouse();
        assertNotNull(inner1);
        assertEquals("innerBean", inner1.getBeanName());
        assertEquals("inner1", inner1.getName());
        assertEquals(6, inner1.getAge());
        assertNotNull(hasInnerBeans.getFriends());
        Object[] friends = hasInnerBeans.getFriends().toArray();
        assertEquals(3, friends.length);
        DerivedTestBean inner2 = (DerivedTestBean) friends[0];
        assertEquals("inner2", inner2.getName());
        assertTrue(inner2.getBeanName().startsWith(DerivedTestBean.class.getName()));
        assertFalse(xbf.containsBean("innerBean"));
        assertNotNull(inner2);
        assertEquals(7, inner2.getAge());
        TestBean innerFactory = (TestBean) friends[1];
        assertEquals(DummyFactory.SINGLETON_NAME, innerFactory.getName());
        TestBean inner5 = (TestBean) friends[2];
        assertEquals("innerBean", inner5.getBeanName());
        assertNotNull(hasInnerBeans.getSomeMap());
        assertEquals(2, hasInnerBeans.getSomeMap().size());
        TestBean inner3 = (TestBean) hasInnerBeans.getSomeMap().get("someKey");
        assertEquals("Jenny", inner3.getName());
        assertEquals(30, inner3.getAge());
        TestBean inner4 = (TestBean) hasInnerBeans.getSomeMap().get("someOtherKey");
        assertEquals("inner4", inner4.getName());
        assertEquals(9, inner4.getAge());
        xbf.destroySingletons();
        assertTrue(inner1.wasDestroyed());
        assertTrue(inner2.wasDestroyed());
        assertTrue(innerFactory.getName() == null);
        assertTrue(inner5.wasDestroyed());
    }

    public void testSingletonInheritanceFromParentFactorySingleton() throws Exception {
        XmlBeanFactory parent = new XmlBeanFactory(new ClassPathResource("parent.xml", getClass()));
        XmlBeanFactory child = new XmlBeanFactory(new ClassPathResource("child.xml", getClass()), parent);
        assertEquals(TestBean.class, child.getType("inheritsFromParentFactory"));
        TestBean inherits = (TestBean) child.getBean("inheritsFromParentFactory");
        assertTrue(inherits.getName().equals("override"));
        assertTrue(inherits.getAge() == 1);
        TestBean inherits2 = (TestBean) child.getBean("inheritsFromParentFactory");
        assertTrue(inherits2 == inherits);
    }

    public void testInheritanceWithDifferentClass() throws Exception {
        XmlBeanFactory parent = new XmlBeanFactory(new ClassPathResource("parent.xml", getClass()));
        XmlBeanFactory child = new XmlBeanFactory(new ClassPathResource("child.xml", getClass()), parent);
        assertEquals(DerivedTestBean.class, child.getType("inheritsWithClass"));
        DerivedTestBean inherits = (DerivedTestBean) child.getBean("inheritsWithDifferentClass");
        assertTrue(inherits.getName().equals("override"));
        assertTrue(inherits.getAge() == 1);
        assertTrue(inherits.wasInitialized());
    }

    public void testInheritanceWithClass() throws Exception {
        XmlBeanFactory parent = new XmlBeanFactory(new ClassPathResource("parent.xml", getClass()));
        XmlBeanFactory child = new XmlBeanFactory(new ClassPathResource("child.xml", getClass()), parent);
        assertEquals(DerivedTestBean.class, child.getType("inheritsWithClass"));
        DerivedTestBean inherits = (DerivedTestBean) child.getBean("inheritsWithClass");
        assertTrue(inherits.getName().equals("override"));
        assertTrue(inherits.getAge() == 1);
        assertTrue(inherits.wasInitialized());
    }

    public void testPrototypeInheritanceFromParentFactoryPrototype() throws Exception {
        XmlBeanFactory parent = new XmlBeanFactory(new ClassPathResource("parent.xml", getClass()));
        XmlBeanFactory child = new XmlBeanFactory(new ClassPathResource("child.xml", getClass()), parent);
        assertEquals(TestBean.class, child.getType("prototypeInheritsFromParentFactoryPrototype"));
        TestBean inherits = (TestBean) child.getBean("prototypeInheritsFromParentFactoryPrototype");
        assertTrue(inherits.getName().equals("prototype-override"));
        assertTrue(inherits.getAge() == 2);
        TestBean inherits2 = (TestBean) child.getBean("prototypeInheritsFromParentFactoryPrototype");
        assertFalse(inherits2 == inherits);
        inherits2.setAge(13);
        assertTrue(inherits2.getAge() == 13);
        assertTrue(inherits.getAge() == 2);
    }

    public void testPrototypeInheritanceFromParentFactorySingleton() throws Exception {
        XmlBeanFactory parent = new XmlBeanFactory(new ClassPathResource("parent.xml", getClass()));
        XmlBeanFactory child = new XmlBeanFactory(new ClassPathResource("child.xml", getClass()), parent);
        TestBean inherits = (TestBean) child.getBean("protoypeInheritsFromParentFactorySingleton");
        assertTrue(inherits.getName().equals("prototypeOverridesInheritedSingleton"));
        assertTrue(inherits.getAge() == 1);
        TestBean inherits2 = (TestBean) child.getBean("protoypeInheritsFromParentFactorySingleton");
        assertFalse(inherits2 == inherits);
        inherits2.setAge(13);
        assertTrue(inherits2.getAge() == 13);
        assertTrue(inherits.getAge() == 1);
    }

    public void testAbstractParentBeans() {
        XmlBeanFactory parent = new XmlBeanFactory(new ClassPathResource("parent.xml", getClass()));
        parent.preInstantiateSingletons();
        assertTrue(parent.isSingleton("inheritedTestBeanWithoutClass"));
        Map tbs = parent.getBeansOfType(TestBean.class);
        assertEquals(2, tbs.size());
        assertTrue(tbs.containsKey("inheritedTestBeanPrototype"));
        assertTrue(tbs.containsKey("inheritedTestBeanSingleton"));
        try {
            parent.getBean("inheritedTestBeanWithoutClass");
            fail("Should have thrown BeanIsAbstractException");
        } catch (BeanIsAbstractException ex) {
        }
        assertTrue(parent.getBean("inheritedTestBeanPrototype") instanceof TestBean);
    }

    public void testDependenciesMaterializeThis() throws Exception {
        XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("dependenciesMaterializeThis.xml", getClass()));
        DummyBoImpl bos = (DummyBoImpl) xbf.getBean("boSingleton");
        DummyBoImpl bop = (DummyBoImpl) xbf.getBean("boPrototype");
        assertNotSame(bos, bop);
        assertTrue(bos.dao == bop.dao);
        assertEquals(2, xbf.getBeansOfType(DummyBoImpl.class, true, true).size());
        assertEquals(1, xbf.getBeansOfType(DummyBoImpl.class, false, true).size());
        assertEquals(2, xbf.getBeansOfType(DummyBoImpl.class).size());
        assertEquals(3, xbf.getBeansOfType(DummyBo.class, true, true).size());
        assertEquals(2, xbf.getBeansOfType(DummyBo.class, true, false).size());
        assertEquals(3, xbf.getBeansOfType(DummyBo.class).size());
    }

    public void testChildOverridesParentBean() throws Exception {
        XmlBeanFactory parent = new XmlBeanFactory(new ClassPathResource("parent.xml", getClass()));
        XmlBeanFactory child = new XmlBeanFactory(new ClassPathResource("child.xml", getClass()), parent);
        TestBean inherits = (TestBean) child.getBean("inheritedTestBean");
        assertTrue(inherits.getName().equals("overrideParentBean"));
        assertTrue(inherits.getAge() == 1);
        TestBean inherits2 = (TestBean) child.getBean("inheritedTestBean");
        assertTrue(inherits2 == inherits);
    }

    /**
	 * Check that a prototype can't inherit from a bogus parent.
	 * If a singleton does this the factory will fail to load.
	 */
    public void testBogusParentageFromParentFactory() throws Exception {
        XmlBeanFactory parent = new XmlBeanFactory(new ClassPathResource("parent.xml", getClass()));
        XmlBeanFactory child = new XmlBeanFactory(new ClassPathResource("child.xml", getClass()), parent);
        try {
            TestBean inherits = (TestBean) child.getBean("bogusParent");
            fail();
        } catch (BeanDefinitionStoreException ex) {
            assertTrue(ex.getMessage().indexOf("bogusParent") != -1);
            assertTrue(ex.getCause() instanceof NoSuchBeanDefinitionException);
        }
    }

    /**
	 * Note that prototype/singleton distinction is <b>not</b> inherited.
	 * It's possible for a subclass singleton not to return independent
	 * instances even if derived from a prototype
	 */
    public void testSingletonInheritsFromParentFactoryPrototype() throws Exception {
        XmlBeanFactory parent = new XmlBeanFactory(new ClassPathResource("parent.xml", getClass()));
        XmlBeanFactory child = new XmlBeanFactory(new ClassPathResource("child.xml", getClass()), parent);
        TestBean inherits = (TestBean) child.getBean("singletonInheritsFromParentFactoryPrototype");
        assertTrue(inherits.getName().equals("prototype-override"));
        assertTrue(inherits.getAge() == 2);
        TestBean inherits2 = (TestBean) child.getBean("singletonInheritsFromParentFactoryPrototype");
        assertTrue(inherits2 == inherits);
    }

    public void testSingletonFromParent() {
        XmlBeanFactory parent = new XmlBeanFactory(new ClassPathResource("parent.xml", getClass()));
        TestBean beanFromParent = (TestBean) parent.getBean("inheritedTestBeanSingleton");
        XmlBeanFactory child = new XmlBeanFactory(new ClassPathResource("child.xml", getClass()), parent);
        TestBean beanFromChild = (TestBean) child.getBean("inheritedTestBeanSingleton");
        assertTrue("singleton from parent and child is the same", beanFromParent == beanFromChild);
    }

    public void testNestedPropertyValue() {
        XmlBeanFactory parent = new XmlBeanFactory(new ClassPathResource("parent.xml", getClass()));
        XmlBeanFactory child = new XmlBeanFactory(new ClassPathResource("child.xml", getClass()), parent);
        IndexedTestBean bean = (IndexedTestBean) child.getBean("indexedTestBean");
        assertEquals("name applied correctly", "myname", bean.getArray()[0].getName());
    }

    public void testCircularReferences() {
        DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
        reader.setValidating(false);
        reader.loadBeanDefinitions(new ClassPathResource("reftypes.xml", getClass()));
        TestBean jenny = (TestBean) xbf.getBean("jenny");
        TestBean david = (TestBean) xbf.getBean("david");
        TestBean ego = (TestBean) xbf.getBean("ego");
        assertTrue("Correct circular reference", jenny.getSpouse() == david);
        assertTrue("Correct circular reference", david.getSpouse() == jenny);
        assertTrue("Correct circular reference", ego.getSpouse() == ego);
    }

    public void testFactoryReferenceCircle() {
        XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("factoryCircle.xml", getClass()));
        TestBean tb = (TestBean) xbf.getBean("singletonFactory");
        DummyFactory db = (DummyFactory) xbf.getBean("&singletonFactory");
        assertTrue(tb == db.getOtherTestBean());
    }

    public void testComplexFactoryReferenceCircle() {
        XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("complexFactoryCircle.xml", getClass()));
        xbf.getBean("proxy1");
        assertEquals(5, xbf.getSingletonCount());
        xbf.getBean("proxy2");
        assertEquals(7, xbf.getSingletonCount());
    }

    public void testInitMethodIsInvoked() throws Exception {
        XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("initializers.xml", getClass()));
        DoubleInitializer in = (DoubleInitializer) xbf.getBean("init-method1");
        assertEquals(14, in.getNum());
    }

    /**
	 * Test that if a custom initializer throws an exception, it's handled correctly
	 */
    public void testInitMethodThrowsException() {
        XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("initializers.xml", getClass()));
        try {
            xbf.getBean("init-method2");
            fail();
        } catch (BeanCreationException ex) {
            assertTrue(ex.getResourceDescription().indexOf("initializers.xml") != -1);
            assertEquals("init-method2", ex.getBeanName());
            assertTrue(ex.getCause() instanceof ServletException);
        }
    }

    public void testNoSuchInitMethod() throws Exception {
        XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("initializers.xml", getClass()));
        try {
            xbf.getBean("init-method3");
            fail();
        } catch (FatalBeanException ex) {
            assertTrue(ex.getMessage().indexOf("initializers.xml") != -1);
            assertTrue(ex.getMessage().indexOf("init-method3") != -1);
            assertTrue(ex.getMessage().indexOf("init") != -1);
        }
    }

    /**
	 * Check that InitializingBean method is called first.
	 */
    public void testInitializingBeanAndInitMethod() throws Exception {
        InitAndIB.constructed = false;
        XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("initializers.xml", getClass()));
        assertFalse(InitAndIB.constructed);
        xbf.preInstantiateSingletons();
        assertFalse(InitAndIB.constructed);
        InitAndIB iib = (InitAndIB) xbf.getBean("init-and-ib");
        assertTrue(InitAndIB.constructed);
        assertTrue(iib.afterPropertiesSetInvoked && iib.initMethodInvoked);
        assertTrue(!iib.destroyed && !iib.customDestroyed);
        xbf.destroySingletons();
        assertTrue(iib.destroyed && iib.customDestroyed);
        xbf.destroySingletons();
        assertTrue(iib.destroyed && iib.customDestroyed);
    }

    /**
	 * Check that InitializingBean method is called first.
	 */
    public void testDefaultLazyInit() throws Exception {
        InitAndIB.constructed = false;
        XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("default-lazy-init.xml", getClass()));
        assertFalse(InitAndIB.constructed);
        xbf.preInstantiateSingletons();
        assertTrue(InitAndIB.constructed);
        try {
            xbf.getBean("lazy-and-bad");
        } catch (BeanCreationException ex) {
            assertTrue(ex.getCause() instanceof ServletException);
        }
    }

    public void testNoSuchXmlFile() throws Exception {
        try {
            XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("missing.xml", getClass()));
            fail("Shouldn't create factory from missing XML");
        } catch (BeanDefinitionStoreException ex) {
        }
    }

    public void testInvalidXmlFile() throws Exception {
        try {
            XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("invalid.xml", getClass()));
            fail("Shouldn't create factory from invalid XML");
        } catch (BeanDefinitionStoreException ex) {
        }
    }

    public void testUnsatisfiedObjectDependencyCheck() throws Exception {
        try {
            XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("unsatisfiedObjectDependencyCheck.xml", getClass()));
            DependenciesBean a = (DependenciesBean) xbf.getBean("a");
            fail();
        } catch (UnsatisfiedDependencyException ex) {
        }
    }

    public void testUnsatisfiedSimpleDependencyCheck() throws Exception {
        try {
            XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("unsatisfiedSimpleDependencyCheck.xml", getClass()));
            DependenciesBean a = (DependenciesBean) xbf.getBean("a");
            fail();
        } catch (UnsatisfiedDependencyException ex) {
        }
    }

    public void testSatisfiedObjectDependencyCheck() throws Exception {
        XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("satisfiedObjectDependencyCheck.xml", getClass()));
        DependenciesBean a = (DependenciesBean) xbf.getBean("a");
        assertNotNull(a.getSpouse());
        assertEquals(xbf, a.getBeanFactory());
    }

    public void testSatisfiedSimpleDependencyCheck() throws Exception {
        XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("satisfiedSimpleDependencyCheck.xml", getClass()));
        DependenciesBean a = (DependenciesBean) xbf.getBean("a");
        assertEquals(a.getAge(), 33);
    }

    public void testUnsatisfiedAllDependencyCheck() throws Exception {
        try {
            XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("unsatisfiedAllDependencyCheckMissingObjects.xml", getClass()));
            DependenciesBean a = (DependenciesBean) xbf.getBean("a");
            fail();
        } catch (UnsatisfiedDependencyException ex) {
        }
    }

    public void testSatisfiedAllDependencyCheck() throws Exception {
        XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("satisfiedAllDependencyCheck.xml", getClass()));
        DependenciesBean a = (DependenciesBean) xbf.getBean("a");
        assertEquals(a.getAge(), 33);
        assertNotNull(a.getName());
        assertNotNull(a.getSpouse());
    }

    public void testAutowire() throws Exception {
        XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("autowire.xml", getClass()));
        TestBean spouse = new TestBean("kerry", 0);
        xbf.registerSingleton("spouse", spouse);
        doTestAutowire(xbf);
    }

    public void testAutowireWithParent() throws Exception {
        XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("autowire.xml", getClass()));
        DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
        MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.addPropertyValue("name", "kerry");
        lbf.registerBeanDefinition("spouse", new RootBeanDefinition(TestBean.class, pvs));
        xbf.setParentBeanFactory(lbf);
        doTestAutowire(xbf);
    }

    private void doTestAutowire(XmlBeanFactory xbf) throws Exception {
        DependenciesBean rod1 = (DependenciesBean) xbf.getBean("rod1");
        TestBean kerry = (TestBean) xbf.getBean("spouse");
        assertEquals(kerry, rod1.getSpouse());
        DependenciesBean rod1a = (DependenciesBean) xbf.getBean("rod1a");
        assertEquals(kerry, rod1a.getSpouse());
        DependenciesBean rod2 = (DependenciesBean) xbf.getBean("rod2");
        assertEquals(kerry, rod2.getSpouse());
        ConstructorDependenciesBean rod3 = (ConstructorDependenciesBean) xbf.getBean("rod3");
        IndexedTestBean other = (IndexedTestBean) xbf.getBean("other");
        assertEquals(kerry, rod3.getSpouse1());
        assertEquals(kerry, rod3.getSpouse2());
        assertEquals(other, rod3.getOther());
        ConstructorDependenciesBean rod3a = (ConstructorDependenciesBean) xbf.getBean("rod3a");
        assertEquals(kerry, rod3a.getSpouse1());
        assertEquals(kerry, rod3a.getSpouse2());
        assertEquals(other, rod3a.getOther());
        try {
            ConstructorDependenciesBean rod4 = (ConstructorDependenciesBean) xbf.getBean("rod4");
            fail("Should have thrown FatalBeanException");
        } catch (FatalBeanException ex) {
        }
        DependenciesBean rod5 = (DependenciesBean) xbf.getBean("rod5");
        assertNull(rod5.getSpouse());
        BeanFactory appCtx = (BeanFactory) xbf.getBean("childAppCtx");
        assertTrue(appCtx.containsBean("rod1"));
        assertTrue(appCtx.containsBean("jenny"));
    }

    public void testAutowireWithDefault() throws Exception {
        XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("default-autowire.xml", getClass()));
        DependenciesBean rod1 = (DependenciesBean) xbf.getBean("rod1");
        assertNotNull(rod1.getSpouse());
        assertTrue(rod1.getSpouse().getName().equals("Kerry"));
        DependenciesBean rod2 = (DependenciesBean) xbf.getBean("rod2");
        assertNotNull(rod2.getSpouse());
        assertTrue(rod2.getSpouse().getName().equals("Kerry"));
        try {
            DependenciesBean rod3 = (DependenciesBean) xbf.getBean("rod3");
            fail("Should have thrown UnsatisfiedDependencyException");
        } catch (UnsatisfiedDependencyException ex) {
        }
    }

    public void testAutowireByConstructor() throws Exception {
        XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("constructor-arg.xml", getClass()));
        ConstructorDependenciesBean rod1 = (ConstructorDependenciesBean) xbf.getBean("rod1");
        TestBean kerry = (TestBean) xbf.getBean("kerry2");
        assertEquals(kerry, rod1.getSpouse1());
        assertEquals(0, rod1.getAge());
        assertEquals(null, rod1.getName());
        ConstructorDependenciesBean rod2 = (ConstructorDependenciesBean) xbf.getBean("rod2");
        TestBean kerry1 = (TestBean) xbf.getBean("kerry1");
        TestBean kerry2 = (TestBean) xbf.getBean("kerry2");
        assertEquals(kerry2, rod2.getSpouse1());
        assertEquals(kerry1, rod2.getSpouse2());
        assertEquals(0, rod2.getAge());
        assertEquals(null, rod2.getName());
        ConstructorDependenciesBean rod = (ConstructorDependenciesBean) xbf.getBean("rod3");
        IndexedTestBean other = (IndexedTestBean) xbf.getBean("other");
        assertEquals(kerry, rod.getSpouse1());
        assertEquals(kerry, rod.getSpouse2());
        assertEquals(other, rod.getOther());
        assertEquals(0, rod.getAge());
        assertEquals(null, rod.getName());
        ConstructorDependenciesBean rod4 = (ConstructorDependenciesBean) xbf.getBean("rod4");
        assertEquals(kerry, rod.getSpouse1());
        assertEquals(kerry, rod.getSpouse2());
        assertEquals(other, rod.getOther());
        assertEquals(0, rod.getAge());
        assertEquals(null, rod.getName());
    }

    public void testAutowireByConstructorWithSimpleValues() throws Exception {
        XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("constructor-arg.xml", getClass()));
        ConstructorDependenciesBean rod5 = (ConstructorDependenciesBean) xbf.getBean("rod5");
        TestBean kerry1 = (TestBean) xbf.getBean("kerry1");
        TestBean kerry2 = (TestBean) xbf.getBean("kerry2");
        IndexedTestBean other = (IndexedTestBean) xbf.getBean("other");
        assertEquals(kerry2, rod5.getSpouse1());
        assertEquals(kerry1, rod5.getSpouse2());
        assertEquals(other, rod5.getOther());
        assertEquals(99, rod5.getAge());
        assertEquals("myname", rod5.getName());
        DerivedConstructorDependenciesBean rod6 = (DerivedConstructorDependenciesBean) xbf.getBean("rod6");
        assertTrue(rod6.initialized);
        assertTrue(!rod6.destroyed);
        assertEquals(kerry2, rod6.getSpouse1());
        assertEquals(kerry1, rod6.getSpouse2());
        assertEquals(other, rod6.getOther());
        assertEquals(0, rod6.getAge());
        assertEquals(null, rod6.getName());
        xbf.destroySingletons();
        assertTrue(rod6.destroyed);
    }

    public void testConstructorArgResolution() {
        XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("constructor-arg.xml", getClass()));
        TestBean kerry1 = (TestBean) xbf.getBean("kerry1");
        TestBean kerry2 = (TestBean) xbf.getBean("kerry2");
        ConstructorDependenciesBean rod10 = (ConstructorDependenciesBean) xbf.getBean("rod10");
        assertEquals(null, rod10.getName());
        ConstructorDependenciesBean rod11 = (ConstructorDependenciesBean) xbf.getBean("rod11");
        assertEquals(kerry2, rod11.getSpouse1());
        ConstructorDependenciesBean rod12 = (ConstructorDependenciesBean) xbf.getBean("rod12");
        assertEquals(kerry1, rod12.getSpouse1());
        assertNull(rod12.getSpouse2());
        ConstructorDependenciesBean rod13 = (ConstructorDependenciesBean) xbf.getBean("rod13");
        assertEquals(kerry1, rod13.getSpouse1());
        assertEquals(kerry2, rod13.getSpouse2());
        ConstructorDependenciesBean rod14 = (ConstructorDependenciesBean) xbf.getBean("rod14");
        assertEquals(kerry1, rod14.getSpouse1());
        assertEquals(kerry2, rod14.getSpouse2());
        ConstructorDependenciesBean rod15 = (ConstructorDependenciesBean) xbf.getBean("rod15");
        assertEquals(kerry2, rod15.getSpouse1());
        assertEquals(kerry1, rod15.getSpouse2());
        ConstructorDependenciesBean rod16 = (ConstructorDependenciesBean) xbf.getBean("rod16");
        assertEquals(kerry2, rod16.getSpouse1());
        assertEquals(kerry1, rod16.getSpouse2());
        assertEquals(29, rod16.getAge());
    }

    public void testConstructorArgWithSingleMatch() {
        XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("constructor-arg.xml", getClass()));
        File file = (File) xbf.getBean("file");
        assertEquals(File.separator + "test", file.getPath());
    }

    public void testThrowsExceptionOnTooManyArguments() throws Exception {
        XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("constructor-arg.xml", getClass()));
        try {
            ConstructorDependenciesBean rod = (ConstructorDependenciesBean) xbf.getBean("rod7");
            fail("Should have thrown BeanDefinitionStoreException");
        } catch (BeanCreationException ex) {
        }
    }

    public void testThrowsExceptionOnAmbiguousResolution() throws Exception {
        XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("constructor-arg.xml", getClass()));
        try {
            ConstructorDependenciesBean rod = (ConstructorDependenciesBean) xbf.getBean("rod8");
            fail("Should have thrown UnsatisfiedDependencyException");
        } catch (UnsatisfiedDependencyException ex) {
        }
    }

    public void testFactoryBeanDefinedAsPrototype() {
        try {
            XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("invalid-factory.xml", getClass()));
            fail("Should have thrown BeanDefinitionStoreException");
        } catch (BeanDefinitionStoreException ex) {
        }
    }

    public void testDependsOn() {
        doTestDependencies("dependencies-dependsOn.xml");
    }

    public void testDependsOnInInnerBean() {
        doTestDependencies("dependencies-dependsOn-inner.xml");
    }

    public void testDependenciesThroughConstructorArguments() {
        doTestDependencies("dependencies-carg.xml");
    }

    public void testDependenciesThroughConstructorArgumentAutowiring() {
        doTestDependencies("dependencies-carg-autowire.xml");
    }

    public void testDependenciesThroughConstructorArgumentsInInnerBean() {
        doTestDependencies("dependencies-carg-inner.xml");
    }

    public void testDependenciesThroughProperties() {
        doTestDependencies("dependencies-prop.xml");
    }

    public void testDependenciesThroughPropertiesWithInTheMiddle() {
        doTestDependencies("dependencies-prop-inTheMiddle.xml");
    }

    public void testDependenciesThroughPropertyAutowiringByName() {
        doTestDependencies("dependencies-prop-autowireByName.xml");
    }

    public void testDependenciesThroughPropertyAutowiringByType() {
        doTestDependencies("dependencies-prop-autowireByType.xml");
    }

    public void testDependenciesThroughPropertiesInInnerBean() {
        doTestDependencies("dependencies-prop-inner.xml");
    }

    private void doTestDependencies(String filename) {
        PreparingBean1.prepared = false;
        PreparingBean1.destroyed = false;
        PreparingBean2.prepared = false;
        PreparingBean2.destroyed = false;
        DependingBean.destroyed = false;
        HoldingBean.destroyed = false;
        XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource(filename, getClass()));
        xbf.preInstantiateSingletons();
        xbf.destroySingletons();
        assertTrue(PreparingBean1.prepared);
        assertTrue(PreparingBean1.destroyed);
        assertTrue(PreparingBean2.prepared);
        assertTrue(PreparingBean2.destroyed);
        assertTrue(DependingBean.destroyed);
        if (xbf.containsBean("holdingBean")) {
            assertTrue(HoldingBean.destroyed);
        }
    }

    public void testClassNotFoundWithDefault() {
        try {
            XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("classNotFound.xml", getClass()));
        } catch (BeanDefinitionStoreException ex) {
            assertTrue(ex.getResourceDescription().indexOf("classNotFound.xml") != -1);
            assertEquals("classNotFound", ex.getBeanName());
            assertTrue(ex.getCause() instanceof ClassNotFoundException);
        }
    }

    public void testClassNotFoundWithNoBeanClassLoader() {
        try {
            DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
            XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(bf);
            reader.setBeanClassLoader(null);
            reader.loadBeanDefinitions(new ClassPathResource("classNotFound.xml", getClass()));
            assertTrue(bf.getBeanDefinition("classNotFound") instanceof RootBeanDefinition);
            assertEquals(((RootBeanDefinition) bf.getBeanDefinition("classNotFound")).getBeanClassName(), "org.springframework.beans.TestBeana");
        } catch (BeanDefinitionStoreException ex) {
            fail("Should not have thrown BeanDefinitionStoreException");
        }
    }

    public void testResourceAndInputStream() throws IOException {
        XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("resource.xml", getClass()));
        ResourceTestBean resource1 = (ResourceTestBean) xbf.getBean("resource1");
        ResourceTestBean resource2 = (ResourceTestBean) xbf.getBean("resource2");
        assertTrue(resource1.getResource() instanceof ClassPathResource);
        StringWriter writer = new StringWriter();
        FileCopyUtils.copy(new InputStreamReader(resource1.getResource().getInputStream()), writer);
        assertEquals("test", writer.toString());
        writer = new StringWriter();
        FileCopyUtils.copy(new InputStreamReader(resource1.getInputStream()), writer);
        assertEquals("test", writer.toString());
        writer = new StringWriter();
        FileCopyUtils.copy(new InputStreamReader(resource2.getResource().getInputStream()), writer);
        assertEquals("test", writer.toString());
        writer = new StringWriter();
        FileCopyUtils.copy(new InputStreamReader(resource2.getInputStream()), writer);
        assertEquals("test", writer.toString());
    }

    public void testClassPathResourceWithImport() {
        XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("org/springframework/beans/factory/xml/resource.xml"));
        ResourceTestBean resource1 = (ResourceTestBean) xbf.getBean("resource1");
        ResourceTestBean resource2 = (ResourceTestBean) xbf.getBean("resource2");
    }

    public void testUrlResourceWithImport() {
        URL url = getClass().getResource("resource.xml");
        XmlBeanFactory xbf = new XmlBeanFactory(new UrlResource(url));
        ResourceTestBean resource1 = (ResourceTestBean) xbf.getBean("resource1");
        ResourceTestBean resource2 = (ResourceTestBean) xbf.getBean("resource2");
    }

    public void testFileSystemResourceWithImport() {
        String file = getClass().getResource("resource.xml").getFile();
        XmlBeanFactory xbf = new XmlBeanFactory(new FileSystemResource(file));
        ResourceTestBean resource1 = (ResourceTestBean) xbf.getBean("resource1");
        ResourceTestBean resource2 = (ResourceTestBean) xbf.getBean("resource2");
    }

    public void testLookupOverrideMethodsWithSetterInjection() {
        DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
        reader.setValidating(true);
        reader.loadBeanDefinitions(new ClassPathResource("overrides.xml", getClass()));
        testLookupOverrideMethodsWithSetterInjection(xbf, "overrideOneMethod", true);
        testLookupOverrideMethodsWithSetterInjection(xbf, "overrideInheritedMethod", true);
        int howmany = 100;
        StopWatch sw = new StopWatch();
        sw.start("Look up " + howmany + " prototype bean instances with method overrides");
        for (int i = 0; i < howmany; i++) {
            testLookupOverrideMethodsWithSetterInjection(xbf, "overrideOnPrototype", false);
        }
        sw.stop();
        System.out.println(sw);
        assertTrue(sw.getTotalTimeMillis() < 1000);
        OverrideOneMethod swappedOom = (OverrideOneMethod) xbf.getBean("overrideOneMethodSwappedReturnValues");
        TestBean tb = swappedOom.getPrototypeDependency();
        assertEquals("David", tb.getName());
        tb = swappedOom.protectedOverrideSingleton();
        assertEquals("Jenny", tb.getName());
    }

    private void testLookupOverrideMethodsWithSetterInjection(BeanFactory xbf, String beanName, boolean singleton) {
        OverrideOneMethod oom = (OverrideOneMethod) xbf.getBean(beanName);
        if (singleton) {
            assertSame(oom, xbf.getBean(beanName));
        } else {
            assertNotSame(oom, xbf.getBean(beanName));
        }
        TestBean jenny1 = oom.getPrototypeDependency();
        assertEquals("Jenny", jenny1.getName());
        TestBean jenny2 = oom.getPrototypeDependency();
        assertEquals("Jenny", jenny2.getName());
        assertNotSame(jenny1, jenny2);
        TestBean jenny3 = oom.invokesOverridenMethodOnSelf();
        assertEquals("Jenny", jenny3.getName());
        assertNotSame(jenny1, jenny3);
        TestBean dave1 = oom.protectedOverrideSingleton();
        assertEquals("David", dave1.getName());
        TestBean dave2 = oom.protectedOverrideSingleton();
        assertEquals("David", dave2.getName());
        assertSame(dave1, dave2);
    }

    public void testReplaceMethodOverrideWithSetterInjection() {
        DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
        reader.setValidating(true);
        reader.loadBeanDefinitions(new ClassPathResource("delegationOverrides.xml", getClass()));
        OverrideOneMethod oom = (OverrideOneMethod) xbf.getBean("overrideOneMethod");
        TestBean jenny1 = oom.getPrototypeDependency();
        assertEquals("Jenny", jenny1.getName());
        TestBean jenny2 = oom.getPrototypeDependency();
        assertEquals("Jenny", jenny2.getName());
        assertNotSame(jenny1, jenny2);
        TestBean dave1 = oom.protectedOverrideSingleton();
        assertEquals("David", dave1.getName());
        TestBean dave2 = oom.protectedOverrideSingleton();
        assertEquals("David", dave2.getName());
        assertSame(dave1, dave2);
        String str = "woierowijeiowiej";
        assertEquals(str, oom.echo(str));
        String s = "this is not a palindrome";
        String reverse = new StringBuffer(s).reverse().toString();
        assertEquals("Should have overridden to reverse, not echo", reverse, oom.replaceMe(s));
        assertEquals("Should have overridden no-arg overloaded replaceMe method to return fixed value", FixedMethodReplacer.VALUE, oom.replaceMe());
        OverrideOneMethodSubclass ooms = (OverrideOneMethodSubclass) xbf.getBean("replaceVoidMethod");
        DoSomethingReplacer dos = (DoSomethingReplacer) xbf.getBean("doSomethingReplacer");
        assertEquals(null, dos.lastArg);
        String s1 = "";
        String s2 = "foo bar black sheep";
        ooms.doSomething(s1);
        assertEquals(s1, dos.lastArg);
        ooms.doSomething(s2);
        assertEquals(s2, dos.lastArg);
    }

    public void testLookupOverrideOneMethodWithConstructorInjection() {
        DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
        reader.setValidating(true);
        reader.loadBeanDefinitions(new ClassPathResource("constructorOverrides.xml", getClass()));
        ConstructorInjectedOverrides cio = (ConstructorInjectedOverrides) xbf.getBean("constructorOverrides");
        assertEquals("Setter string was set", "from property element", cio.getSetterString());
        TestBean jenny = (TestBean) xbf.getBean("jenny");
        assertSame(jenny, cio.getTestBean());
        assertSame(jenny, cio.getTestBean());
        FactoryMethods fm1 = cio.createFactoryMethods();
        FactoryMethods fm2 = cio.createFactoryMethods();
        assertNotSame("FactoryMethods reference is to a prototype", fm1, fm2);
        assertSame("The two prototypes hold the same singleton reference", fm1.getTestBean(), fm2.getTestBean());
    }

    public void testRejectsOverrideOfBogusMethodName() {
        DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
        reader.setValidating(true);
        try {
            reader.loadBeanDefinitions(new ClassPathResource("invalidOverridesNoSuchMethod.xml", getClass()));
            fail("Shouldn't allow override of bogus method");
        } catch (BeanDefinitionStoreException ex) {
            assertTrue("Bogus method name correctly reported", ex.getMessage().indexOf("bogusMethod") != -1);
        }
    }

    /**
	 * Assert the presence of this bug until we resolve it.
	 */
    public void testSerializabilityOfMethodReplacer() throws Exception {
        try {
            BUGtestSerializableMethodReplacerAndSuperclass();
            fail();
        } catch (AssertionFailedError ex) {
            System.err.println("****** SPR-356: Objects with MethodReplace overrides are not serializable");
        }
    }

    public void BUGtestSerializableMethodReplacerAndSuperclass() throws IOException, ClassNotFoundException {
        DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
        reader.setValidating(true);
        reader.loadBeanDefinitions(new ClassPathResource("delegationOverrides.xml", getClass()));
        SerializableMethodReplacerCandidate s = (SerializableMethodReplacerCandidate) xbf.getBean("serializableReplacer");
        String forwards = "this is forwards";
        String backwards = new StringBuffer(forwards).reverse().toString();
        assertEquals(backwards, s.replaceMe(forwards));
        assertTrue(SerializationTestUtils.isSerializable(s));
        s = (SerializableMethodReplacerCandidate) SerializationTestUtils.serializeAndDeserialize(s);
        assertEquals("Method replace still works after serialization and deserialization", backwards, s.replaceMe(forwards));
    }

    public void testFactoryMethodsSingletonOnTargetClass() {
        DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
        reader.setValidating(true);
        reader.loadBeanDefinitions(new ClassPathResource("factory-methods.xml", getClass()));
        TestBean tb = (TestBean) xbf.getBean("defaultTestBean");
        assertEquals("defaultInstance", tb.getName());
        assertEquals(1, tb.getAge());
        FactoryMethods fm = (FactoryMethods) xbf.getBean("default");
        assertEquals(0, fm.getNum());
        assertEquals("default", fm.getName());
        assertEquals("defaultInstance", fm.getTestBean().getName());
        assertEquals("setterString", fm.getStringValue());
        fm = (FactoryMethods) xbf.getBean("testBeanOnly");
        assertEquals(0, fm.getNum());
        assertEquals("default", fm.getName());
        assertEquals("Juergen", fm.getTestBean().getName());
        fm = (FactoryMethods) xbf.getBean("full");
        assertEquals(27, fm.getNum());
        assertEquals("gotcha", fm.getName());
        assertEquals("Juergen", fm.getTestBean().getName());
        FactoryMethods fm2 = (FactoryMethods) xbf.getBean("full");
        assertSame(fm, fm2);
        xbf.destroySingletons();
        assertTrue(tb.wasDestroyed());
    }

    public void testFactoryMethodsWithNull() {
        DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
        reader.setValidating(true);
        reader.loadBeanDefinitions(new ClassPathResource("factory-methods.xml", getClass()));
        FactoryMethods fm = (FactoryMethods) xbf.getBean("fullWithNull");
        assertEquals(27, fm.getNum());
        assertEquals(null, fm.getName());
        assertEquals("Juergen", fm.getTestBean().getName());
        fm = (FactoryMethods) xbf.getBean("fullWithGenericNull");
        assertEquals(27, fm.getNum());
        assertEquals(null, fm.getName());
        assertEquals("Juergen", fm.getTestBean().getName());
    }

    public void testFactoryMethodsWithAutowire() {
        DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
        reader.setValidating(true);
        reader.loadBeanDefinitions(new ClassPathResource("factory-methods.xml", getClass()));
        FactoryMethods fm = (FactoryMethods) xbf.getBean("fullWithAutowire");
        assertEquals(27, fm.getNum());
        assertEquals("gotchaAutowired", fm.getName());
        assertEquals("Juergen", fm.getTestBean().getName());
    }

    public void testFactoryMethodsPrototypeOnTargetClass() {
        DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
        reader.setValidating(true);
        reader.loadBeanDefinitions(new ClassPathResource("factory-methods.xml", getClass()));
        FactoryMethods fm = (FactoryMethods) xbf.getBean("defaultPrototype");
        FactoryMethods fm2 = (FactoryMethods) xbf.getBean("defaultPrototype");
        assertEquals(0, fm.getNum());
        assertEquals("default", fm.getName());
        assertEquals("defaultInstance", fm.getTestBean().getName());
        assertEquals("setterString", fm.getStringValue());
        assertEquals(fm.getNum(), fm2.getNum());
        assertEquals(fm.getStringValue(), fm2.getStringValue());
        assertNotSame(fm.getTestBean(), fm2.getTestBean());
        assertNotSame(fm, fm2);
        fm = (FactoryMethods) xbf.getBean("testBeanOnlyPrototype");
        fm2 = (FactoryMethods) xbf.getBean("testBeanOnlyPrototype");
        assertEquals(0, fm.getNum());
        assertEquals("default", fm.getName());
        assertEquals("Juergen", fm.getTestBean().getName());
        assertEquals(fm.getNum(), fm2.getNum());
        assertEquals(fm.getStringValue(), fm2.getStringValue());
        assertSame(fm.getTestBean(), fm2.getTestBean());
        assertNotSame(fm, fm2);
        fm = (FactoryMethods) xbf.getBean("fullPrototype");
        fm2 = (FactoryMethods) xbf.getBean("fullPrototype");
        assertEquals(27, fm.getNum());
        assertEquals("gotcha", fm.getName());
        assertEquals("Juergen", fm.getTestBean().getName());
        assertEquals(fm.getNum(), fm2.getNum());
        assertEquals(fm.getStringValue(), fm2.getStringValue());
        assertSame(fm.getTestBean(), fm2.getTestBean());
        assertNotSame(fm, fm2);
    }

    /**
	 * Tests where the static factory method is on a different class.
	 */
    public void testFactoryMethodsOnExternalClass() {
        DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
        reader.setValidating(true);
        reader.loadBeanDefinitions(new ClassPathResource("factory-methods.xml", getClass()));
        assertEquals(TestBean.class, xbf.getType("externalFactoryMethodWithoutArgs"));
        assertEquals(TestBean.class, xbf.getType("externalFactoryMethodWithArgs"));
        String[] names = xbf.getBeanNamesForType(TestBean.class);
        assertTrue(Arrays.asList(names).contains("externalFactoryMethodWithoutArgs"));
        assertTrue(Arrays.asList(names).contains("externalFactoryMethodWithArgs"));
        TestBean tb = (TestBean) xbf.getBean("externalFactoryMethodWithoutArgs");
        assertEquals(2, tb.getAge());
        assertEquals("Tristan", tb.getName());
        tb = (TestBean) xbf.getBean("externalFactoryMethodWithArgs");
        assertEquals(33, tb.getAge());
        assertEquals("Rod", tb.getName());
        assertEquals(TestBean.class, xbf.getType("externalFactoryMethodWithoutArgs"));
        assertEquals(TestBean.class, xbf.getType("externalFactoryMethodWithArgs"));
        names = xbf.getBeanNamesForType(TestBean.class);
        assertTrue(Arrays.asList(names).contains("externalFactoryMethodWithoutArgs"));
        assertTrue(Arrays.asList(names).contains("externalFactoryMethodWithArgs"));
    }

    public void testInstanceFactoryMethodWithoutArgs() {
        DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
        reader.setValidating(true);
        reader.loadBeanDefinitions(new ClassPathResource("factory-methods.xml", getClass()));
        InstanceFactory.count = 0;
        xbf.preInstantiateSingletons();
        assertEquals(1, InstanceFactory.count);
        FactoryMethods fm = (FactoryMethods) xbf.getBean("instanceFactoryMethodWithoutArgs");
        assertEquals("instanceFactory", fm.getTestBean().getName());
        assertEquals(1, InstanceFactory.count);
        FactoryMethods fm2 = (FactoryMethods) xbf.getBean("instanceFactoryMethodWithoutArgs");
        assertEquals("instanceFactory", fm2.getTestBean().getName());
        assertSame(fm2, fm);
        assertEquals(1, InstanceFactory.count);
    }

    public void testFactoryMethodNoMatchingStaticMethod() {
        DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
        reader.setValidating(true);
        reader.loadBeanDefinitions(new ClassPathResource("factory-methods.xml", getClass()));
        try {
            xbf.getBean("noMatchPrototype");
            fail("No static method matched");
        } catch (BeanCreationException ex) {
        }
    }

    public void testCanSpecifyFactoryMethodArgumentsOnFactoryMethodPrototype() {
        DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
        reader.setValidating(true);
        reader.loadBeanDefinitions(new ClassPathResource("factory-methods.xml", getClass()));
        TestBean tbArg = new TestBean();
        tbArg.setName("arg1");
        TestBean tbArg2 = new TestBean();
        tbArg2.setName("arg2");
        FactoryMethods fm1 = (FactoryMethods) xbf.getBean("testBeanOnlyPrototype", new Object[] { tbArg });
        FactoryMethods fm2 = (FactoryMethods) xbf.getBean("testBeanOnlyPrototype", new Object[] { tbArg2 });
        assertEquals(0, fm1.getNum());
        assertEquals("default", fm1.getName());
        assertEquals("arg1", fm1.getTestBean().getName());
        assertEquals("arg2", fm2.getTestBean().getName());
        assertEquals(fm1.getNum(), fm2.getNum());
        assertEquals(fm2.getStringValue(), "testBeanOnlyPrototypeDISetterString");
        assertEquals(fm2.getStringValue(), fm2.getStringValue());
        assertSame(fm2.getTestBean(), fm2.getTestBean());
        assertNotSame(fm1, fm2);
    }

    public void testCannotSpecifyFactoryMethodArgumentsOnSingleton() {
        DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
        reader.setValidating(true);
        reader.loadBeanDefinitions(new ClassPathResource("factory-methods.xml", getClass()));
        try {
            xbf.getBean("testBeanOnly", new Object[] { new TestBean() });
            fail("Shouldn't allow args to be passed to a singleton");
        } catch (BeanDefinitionStoreException ex) {
        }
    }

    public void testFactoryMethodWithDifferentReturnType() {
        DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
        reader.setValidating(true);
        reader.loadBeanDefinitions(new ClassPathResource("factory-methods.xml", getClass()));
        assertTrue(List.class.isAssignableFrom(xbf.getType("listInstance")));
        String[] names = xbf.getBeanNamesForType(FactoryMethods.class);
        assertTrue(!Arrays.asList(names).contains("listInstance"));
        names = xbf.getBeanNamesForType(List.class);
        assertTrue(Arrays.asList(names).contains("listInstance"));
        xbf.preInstantiateSingletons();
        assertTrue(List.class.isAssignableFrom(xbf.getType("listInstance")));
        names = xbf.getBeanNamesForType(FactoryMethods.class);
        assertTrue(!Arrays.asList(names).contains("listInstance"));
        names = xbf.getBeanNamesForType(List.class);
        assertTrue(Arrays.asList(names).contains("listInstance"));
        List list = (List) xbf.getBean("listInstance");
        assertEquals(Collections.EMPTY_LIST, list);
    }

    public void testFactoryMethodForJavaMailSession() {
        DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
        reader.setValidating(true);
        reader.loadBeanDefinitions(new ClassPathResource("factory-methods.xml", getClass()));
        Session session = (Session) xbf.getBean("javaMailSession");
        assertEquals("someuser", session.getProperty("mail.smtp.user"));
        assertEquals("somepw", session.getProperty("mail.smtp.password"));
    }

    public void testCannotSpecifyFactoryMethodArgumentsExceptWithFactoryMethod() {
        DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
        reader.setValidating(true);
        reader.loadBeanDefinitions(new ClassPathResource("overrides.xml", getClass()));
        try {
            xbf.getBean("overrideOnPrototype", new Object[] { new TestBean() });
            fail("Shouldn't allow args to be passed to a Setter-Injected object");
        } catch (BeanDefinitionStoreException ex) {
        }
    }

    public void testConstructorArgWithSingleSimpleTypeMatch() {
        XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("constructor-arg.xml", getClass()));
        SingleSimpleTypeConstructorBean bean = (SingleSimpleTypeConstructorBean) xbf.getBean("beanWithBoolean");
        assertTrue(bean.isSingleBoolean());
        SingleSimpleTypeConstructorBean bean2 = (SingleSimpleTypeConstructorBean) xbf.getBean("beanWithBoolean2");
        assertTrue(bean2.isSingleBoolean());
    }

    public void testConstructorArgWithDoubleSimpleTypeMatch() {
        XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("constructor-arg.xml", getClass()));
        SingleSimpleTypeConstructorBean bean = (SingleSimpleTypeConstructorBean) xbf.getBean("beanWithBooleanAndString");
        assertTrue(bean.isSecondBoolean());
        assertEquals("A String", bean.getTestString());
        SingleSimpleTypeConstructorBean bean2 = (SingleSimpleTypeConstructorBean) xbf.getBean("beanWithBooleanAndString2");
        assertTrue(bean2.isSecondBoolean());
        assertEquals("A String", bean2.getTestString());
    }

    public void testDoubleBooleanAutowire() {
        XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("constructor-arg.xml", getClass()));
        DoubleBooleanConstructorBean bean = (DoubleBooleanConstructorBean) xbf.getBean("beanWithDoubleBoolean");
        assertEquals(Boolean.TRUE, bean.boolean1);
        assertEquals(Boolean.FALSE, bean.boolean2);
    }

    public void testDoubleBooleanAutowireWithIndex() {
        XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("constructor-arg.xml", getClass()));
        DoubleBooleanConstructorBean bean = (DoubleBooleanConstructorBean) xbf.getBean("beanWithDoubleBooleanAndIndex");
        assertEquals(Boolean.FALSE, bean.boolean1);
        assertEquals(Boolean.TRUE, bean.boolean2);
    }

    public static class DoSomethingReplacer implements MethodReplacer {

        public Object lastArg;

        public Object reimplement(Object obj, Method method, Object[] args) throws Throwable {
            assertEquals(1, args.length);
            assertEquals("doSomething", method.getName());
            lastArg = args[0];
            return null;
        }
    }

    public static class BadInitializer {

        /** Init method */
        public void init2() throws ServletException {
            throw new ServletException();
        }
    }

    public static class DoubleInitializer {

        private int num;

        public int getNum() {
            return num;
        }

        public void setNum(int i) {
            num = i;
        }

        /** Init method */
        public void init() {
            this.num *= 2;
        }
    }

    public static class InitAndIB implements InitializingBean, DisposableBean {

        public static boolean constructed;

        public boolean afterPropertiesSetInvoked, initMethodInvoked, destroyed, customDestroyed;

        public InitAndIB() {
            constructed = true;
        }

        public void afterPropertiesSet() {
            if (this.initMethodInvoked) {
                fail();
            }
            this.afterPropertiesSetInvoked = true;
        }

        /** Init method */
        public void customInit() throws ServletException {
            if (!this.afterPropertiesSetInvoked) {
                fail();
            }
            this.initMethodInvoked = true;
        }

        public void destroy() {
            if (this.customDestroyed) {
                fail();
            }
            if (this.destroyed) {
                throw new IllegalStateException("Already destroyed");
            }
            this.destroyed = true;
        }

        public void customDestroy() {
            if (!this.destroyed) {
                fail();
            }
            if (this.customDestroyed) {
                throw new IllegalStateException("Already customDestroyed");
            }
            this.customDestroyed = true;
        }
    }

    public static class PreparingBean1 implements DisposableBean {

        public static boolean prepared = false;

        public static boolean destroyed = false;

        public PreparingBean1() {
            prepared = true;
        }

        public void destroy() {
            destroyed = true;
        }
    }

    public static class PreparingBean2 implements DisposableBean {

        public static boolean prepared = false;

        public static boolean destroyed = false;

        public PreparingBean2() {
            prepared = true;
        }

        public void destroy() {
            destroyed = true;
        }
    }

    public static class DependingBean implements InitializingBean, DisposableBean {

        public static boolean destroyed = false;

        public DependingBean() {
        }

        public DependingBean(PreparingBean1 bean1, PreparingBean2 bean2) {
        }

        public void setBean1(PreparingBean1 bean1) {
        }

        public void setBean2(PreparingBean2 bean2) {
        }

        public void setInTheMiddleBean(InTheMiddleBean bean) {
        }

        public void afterPropertiesSet() {
            if (!(PreparingBean1.prepared && PreparingBean2.prepared)) {
                throw new IllegalStateException("Need prepared PreparedBeans!");
            }
        }

        public void destroy() {
            if (PreparingBean1.destroyed || PreparingBean2.destroyed) {
                throw new IllegalStateException("Should not be destroyed before PreparedBeans");
            }
            destroyed = true;
        }
    }

    public static class InTheMiddleBean {

        public void setBean1(PreparingBean1 bean1) {
        }

        public void setBean2(PreparingBean2 bean2) {
        }
    }

    public static class HoldingBean implements DisposableBean {

        public static boolean destroyed = false;

        public void setDependingBean(DependingBean dependingBean) {
        }

        public void destroy() {
            if (DependingBean.destroyed) {
                throw new IllegalStateException("Should not be destroyed before DependingBean");
            }
            destroyed = true;
        }
    }

    public static class DoubleBooleanConstructorBean {

        private Boolean boolean1;

        private Boolean boolean2;

        public DoubleBooleanConstructorBean(Boolean b1, Boolean b2) {
            this.boolean1 = b1;
            this.boolean2 = b2;
        }
    }
}
