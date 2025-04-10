package org.apache.harmony.luni.tests.java.util;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.PropertyPermission;
import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

public class PropertyPermissionTest extends junit.framework.TestCase {

    static PropertyPermission javaPP = new PropertyPermission("java.*", "read");

    static PropertyPermission userPP = new PropertyPermission("user.name", "read,write");

    /**
	 * @tests java.util.PropertyPermission#PropertyPermission(java.lang.String,
	 *        java.lang.String)
	 */
    public void test_ConstructorLjava_lang_StringLjava_lang_String() {
        assertTrue("Used to test", true);
    }

    /**
	 * @tests java.util.PropertyPermission#equals(java.lang.Object)
	 */
    public void test_equalsLjava_lang_Object() {
        PropertyPermission equalToJavaPP = new PropertyPermission("java.*", "read");
        PropertyPermission notEqualToJavaPP = new PropertyPermission("java.*", "read, write");
        PropertyPermission alsoNotEqualToJavaPP = new PropertyPermission("java.home", "read");
        assertTrue("Equal returned false for equal objects", javaPP.equals(equalToJavaPP));
        assertTrue("Equal returned true for objects with different names", !javaPP.equals(notEqualToJavaPP));
        assertTrue("Equal returned true for objects with different permissions", !javaPP.equals(alsoNotEqualToJavaPP));
    }

    /**
	 * @tests java.util.PropertyPermission#getActions()
	 */
    public void test_getActions() {
        assertEquals("getActions did not return proper action", "read", javaPP.getActions());
        assertEquals("getActions did not return proper canonical representation of actions", "read,write", userPP.getActions());
    }

    /**
	 * @tests java.util.PropertyPermission#hashCode()
	 */
    public void test_hashCode() {
        assertTrue("javaPP returned wrong hashCode", javaPP.hashCode() == javaPP.getName().hashCode());
        assertTrue("userPP returned wrong hashCode", userPP.hashCode() == userPP.getName().hashCode());
    }

    /**
	 * @tests java.util.PropertyPermission#implies(java.security.Permission)
	 */
    public void test_impliesLjava_security_Permission() {
        PropertyPermission impliedByJavaPP = new PropertyPermission("java.home", "read");
        PropertyPermission notImpliedByJavaPP = new PropertyPermission("java.home", "read,write");
        PropertyPermission impliedByUserPP = new PropertyPermission("user.name", "read,write");
        PropertyPermission alsoImpliedByUserPP = new PropertyPermission("user.name", "write");
        assertTrue("Returned false for implied permission (subset of .*)", javaPP.implies(impliedByJavaPP));
        assertTrue("Returned true for unimplied permission", !javaPP.implies(notImpliedByJavaPP));
        assertTrue("Returned false for implied permission (equal)", userPP.implies(impliedByUserPP));
        assertTrue("Returned false for implied permission (subset of actions)", userPP.implies(alsoImpliedByUserPP));
    }

    /**
	 * @tests java.util.PropertyPermission#newPermissionCollection()
	 */
    public void test_newPermissionCollection() {
        java.security.PermissionCollection pc = javaPP.newPermissionCollection();
        pc.add(javaPP);
        Enumeration elementEnum = pc.elements();
        assertTrue("Invalid PermissionCollection returned", elementEnum.nextElement().equals(javaPP));
    }

    /**
     * @tests java.util.PropertyPermission#readObject(ObjectInputStream)
     * @tests java.util.PropertyPermission#writeObject(ObjectOutputStream)
     */
    public void test_serialization() throws Exception {
        PropertyPermission pp = new PropertyPermission("test", "read");
        SerializationTest.verifySelf(pp, comparator);
        SerializationTest.verifyGolden(this, pp, comparator);
    }

    private static final SerializableAssert comparator = new SerializableAssert() {

        public void assertDeserialized(Serializable initial, Serializable deserialized) {
            PropertyPermission initialPP = (PropertyPermission) initial;
            PropertyPermission deseriaPP = (PropertyPermission) deserialized;
            assertEquals("should be equal", initialPP, deseriaPP);
        }
    };
}
