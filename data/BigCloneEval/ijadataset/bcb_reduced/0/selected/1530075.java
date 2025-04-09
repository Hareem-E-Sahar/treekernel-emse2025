package com.headissue.asterisk.jtapi.gjtapi.junit;

import java.lang.reflect.Constructor;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests extends TestSuite {

    public AllTests() {
        addTestSuite(DialplanParserCommandTest.class);
        addTestSuite(DialplanParserLineTest.class);
        addTestSuite(DialplanParserLocalTargetTest.class);
    }

    /**
	 * Run code with text interface. Empty arguments run all tests. 
	 * An argument of the form "packge.class.methodname" runs a specific test.
	 */
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            junit.textui.TestRunner.run(new AllTests());
        } else {
            String s = args[0];
            s = s.replace('/', '.');
            Class _suiteClass = null;
            try {
                _suiteClass = Class.forName(s);
            } catch (ClassNotFoundException ex) {
            }
            Test t;
            if (_suiteClass != null) {
                if (TestSuite.class.isAssignableFrom(_suiteClass)) {
                    t = (TestSuite) _suiteClass.newInstance();
                } else {
                    t = new TestSuite(_suiteClass);
                }
            } else {
                int i = s.lastIndexOf('.');
                String _className = s.substring(0, i);
                String _methodName = s.substring(i + 1);
                Class c = Class.forName(_className);
                Class[] ca = { String.class };
                Constructor con = c.getConstructor(ca);
                Object[] oa = { _methodName };
                t = (Test) con.newInstance(oa);
            }
            junit.textui.TestRunner.run(t);
        }
    }
}
