package org.w3c.domts.level2.core;

import java.lang.reflect.Constructor;
import junit.framework.TestSuite;
import org.w3c.domts.DOMTestDocumentBuilderFactory;
import org.w3c.domts.DOMTestSuite;
import org.w3c.domts.DocumentBuilderSetting;
import org.w3c.domts.JTidyDocumentBuilderFactory;
import org.w3c.domts.JUnitTestSuiteAdapter;

public class TestJTidy extends TestSuite {

    public static TestSuite suite() throws Exception {
        Class testClass = ClassLoader.getSystemClassLoader().loadClass("org.w3c.domts.level2.core.alltests");
        Constructor testConstructor = testClass.getConstructor(new Class[] { DOMTestDocumentBuilderFactory.class });
        DOMTestDocumentBuilderFactory factory = new JTidyDocumentBuilderFactory(new DocumentBuilderSetting[0]);
        Object test = testConstructor.newInstance(new Object[] { factory });
        return new JUnitTestSuiteAdapter((DOMTestSuite) test);
    }
}
