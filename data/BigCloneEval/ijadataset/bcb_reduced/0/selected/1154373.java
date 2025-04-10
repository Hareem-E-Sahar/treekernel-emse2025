package org.jsecurity.authz.permission;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * @author Jeremy Haile
 * @since 0.9
 */
public class WildcardPermissionTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNull() {
        new WildcardPermission(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmpty() {
        new WildcardPermission("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBlank() {
        new WildcardPermission("   ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnlyDelimiters() {
        new WildcardPermission("::,,::,:");
    }

    @Test
    public void testNamed() {
        WildcardPermission p1, p2;
        p1 = new WildcardPermission("something");
        p2 = new WildcardPermission("something");
        assertTrue(p1.implies(p2));
        assertTrue(p2.implies(p1));
        p1 = new WildcardPermission("something");
        p2 = new WildcardPermission("SOMETHING");
        assertTrue(p1.implies(p2));
        assertTrue(p2.implies(p1));
        p1 = new WildcardPermission("something");
        p2 = new WildcardPermission("else");
        assertFalse(p1.implies(p2));
        assertFalse(p2.implies(p1));
        p1 = new WildcardPermission("BLAHBLAH", false);
        p2 = new WildcardPermission("BLAHBLAH", false);
        assertTrue(p1.implies(p2));
        assertTrue(p2.implies(p1));
        p1 = new WildcardPermission("BLAHBLAH", false);
        p2 = new WildcardPermission("bLAHBLAH", false);
        assertTrue(p1.implies(p2));
        assertTrue(p2.implies(p1));
        p1 = new WildcardPermission("BLAHBLAH", false);
        p2 = new WildcardPermission("whatwhat", false);
        assertFalse(p1.implies(p2));
        assertFalse(p2.implies(p1));
    }

    @Test
    public void testLists() {
        WildcardPermission p1, p2, p3;
        p1 = new WildcardPermission("one,two");
        p2 = new WildcardPermission("one");
        assertTrue(p1.implies(p2));
        assertFalse(p2.implies(p1));
        p1 = new WildcardPermission("one,two,three");
        p2 = new WildcardPermission("one,three");
        assertTrue(p1.implies(p2));
        assertFalse(p2.implies(p1));
        p1 = new WildcardPermission("one,two:one,two,three");
        p2 = new WildcardPermission("one:three");
        p3 = new WildcardPermission("one:two,three");
        assertTrue(p1.implies(p2));
        assertFalse(p2.implies(p1));
        assertTrue(p1.implies(p3));
        assertFalse(p2.implies(p3));
        assertTrue(p3.implies(p2));
        p1 = new WildcardPermission("one,two,three:one,two,three:one,two");
        p2 = new WildcardPermission("one:three:two");
        assertTrue(p1.implies(p2));
        assertFalse(p2.implies(p1));
        p1 = new WildcardPermission("one");
        p2 = new WildcardPermission("one:two,three,four");
        p3 = new WildcardPermission("one:two,three,four:five:six:seven");
        assertTrue(p1.implies(p2));
        assertTrue(p1.implies(p3));
        assertFalse(p2.implies(p1));
        assertFalse(p3.implies(p1));
        assertTrue(p2.implies(p3));
    }

    @Test
    public void testWildcards() {
        WildcardPermission p1, p2, p3, p4, p5, p6, p7, p8;
        p1 = new WildcardPermission("*");
        p2 = new WildcardPermission("one");
        p3 = new WildcardPermission("one:two");
        p4 = new WildcardPermission("one,two:three,four");
        p5 = new WildcardPermission("one,two:three,four,five:six:seven,eight");
        assertTrue(p1.implies(p2));
        assertTrue(p1.implies(p3));
        assertTrue(p1.implies(p4));
        assertTrue(p1.implies(p5));
        p1 = new WildcardPermission("newsletter:*");
        p2 = new WildcardPermission("newsletter:read");
        p3 = new WildcardPermission("newsletter:read,write");
        p4 = new WildcardPermission("newsletter:*");
        p5 = new WildcardPermission("newsletter:*:*");
        p6 = new WildcardPermission("newsletter:*:read");
        p7 = new WildcardPermission("newsletter:write:*");
        p8 = new WildcardPermission("newsletter:read,write:*");
        assertTrue(p1.implies(p2));
        assertTrue(p1.implies(p3));
        assertTrue(p1.implies(p4));
        assertTrue(p1.implies(p5));
        assertTrue(p1.implies(p6));
        assertTrue(p1.implies(p7));
        assertTrue(p1.implies(p8));
        p1 = new WildcardPermission("newsletter:*:*");
        assertTrue(p1.implies(p2));
        assertTrue(p1.implies(p3));
        assertTrue(p1.implies(p4));
        assertTrue(p1.implies(p5));
        assertTrue(p1.implies(p6));
        assertTrue(p1.implies(p7));
        assertTrue(p1.implies(p8));
        p1 = new WildcardPermission("newsletter:*:*:*");
        assertTrue(p1.implies(p2));
        assertTrue(p1.implies(p3));
        assertTrue(p1.implies(p4));
        assertTrue(p1.implies(p5));
        assertTrue(p1.implies(p6));
        assertTrue(p1.implies(p7));
        assertTrue(p1.implies(p8));
        p1 = new WildcardPermission("newsletter:*:read");
        p2 = new WildcardPermission("newsletter:123:read");
        p3 = new WildcardPermission("newsletter:123,456:read,write");
        p4 = new WildcardPermission("newsletter:read");
        p5 = new WildcardPermission("newsletter:read,write");
        p6 = new WildcardPermission("newsletter:123:read:write");
        assertTrue(p1.implies(p2));
        assertFalse(p1.implies(p3));
        assertFalse(p1.implies(p4));
        assertFalse(p1.implies(p5));
        assertTrue(p1.implies(p6));
        p1 = new WildcardPermission("newsletter:*:read:*");
        assertTrue(p1.implies(p2));
        assertTrue(p1.implies(p6));
    }
}
