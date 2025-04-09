package com.googlecode.fizzle;

import junit.framework.TestCase;

/**
 * @author Ray Krueger
 */
public abstract class ExceptionTestCase extends TestCase {

    protected abstract Class<? extends Exception> getExceptionClass();

    public void testNoArgConstructor() throws Exception {
        try {
            getExceptionClass().getConstructor();
            fail("NoSuchMethodException expected, no-arg exception constructors are evil");
        } catch (NoSuchMethodException e) {
        }
    }

    public void testStringConstructor() throws Exception {
        Exception e = getExceptionClass().getConstructor(String.class).newInstance("HI MOM");
        assertEquals("HI MOM", e.getMessage());
        assertNull(e.getCause());
    }

    public void testStringAndCauseConstructor() throws Exception {
        final Throwable EXPECTED = new Throwable();
        Exception e = getExceptionClass().getConstructor(String.class, Throwable.class).newInstance("HI MOM", EXPECTED);
        assertEquals("HI MOM", e.getMessage());
        assertEquals(EXPECTED, e.getCause());
    }

    public void testCauseConstructor() throws Exception {
        final Throwable EXPECTED = new Throwable("MESSAGE FROM CAUSE");
        Exception e = getExceptionClass().getConstructor(Throwable.class).newInstance(EXPECTED);
        assertEquals("java.lang.Throwable: MESSAGE FROM CAUSE", e.getMessage());
        assertEquals(EXPECTED, e.getCause());
    }
}
