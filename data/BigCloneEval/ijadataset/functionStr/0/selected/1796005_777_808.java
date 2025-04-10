public class Test {    public void testReplaceMethodOverrideWithSetterInjection() {
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
}