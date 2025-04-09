package sis.report;

import sis.studentinfo.*;
import junit.framework.TestCase;

public class LoopTests extends TestCase {

    Loop loop = new Loop();

    private Session session;

    public void testPalindrome() {
        assertFalse(Loop.isPalindrome("abcdef"));
        assertFalse(Loop.isPalindrome("abccda"));
        assertFalse(Loop.isPalindrome("abccda"));
        assertFalse(Loop.isPalindrome("abcxba"));
        assertTrue(Loop.isPalindrome("a"));
        assertTrue(Loop.isPalindrome("aa"));
        assertFalse(Loop.isPalindrome("ab"));
        assertTrue(Loop.isPalindrome(""));
        assertTrue(Loop.isPalindrome("aaa"));
        assertTrue(Loop.isPalindrome("aba"));
        assertFalse(Loop.isPalindrome("abbba"));
        assertFalse(Loop.isPalindrome("abba"));
        assertFalse(Loop.isPalindrome("abbas"));
    }

    public void testForSkip() {
        StringBuilder builder = new StringBuilder();
        String string = "123456";
        for (int i = 0; i < string.length(); i += 2) builder.append(string.charAt(i));
        assertEquals("135", builder.toString());
    }

    public void testFibonacci() {
        assertEquals(0, loop.fib(0));
        assertEquals(1, loop.fib(1));
        assertEquals(1, loop.fib(2));
        assertEquals(2, loop.fib(3));
        assertEquals(3, loop.fib(4));
        assertEquals(5, loop.fib(5));
        assertEquals(8, loop.fib(6));
        assertEquals(13, loop.fib(7));
        assertEquals(21, loop.fib(8));
        assertEquals(34, loop.fib(9));
        assertEquals(55, loop.fib(10));
    }

    public void testCommas() {
        String sequence = "1,2,3,4,5";
        assertEquals(sequence, loop.sequenceUsindDo(1, 5));
        assertEquals(sequence, loop.sequenceUsindFor(1, 5));
        assertEquals(sequence, loop.sequenceUsindWhile(1, 5));
    }

    public void testEndTrim() {
        assertEquals("", loop.endTrim(" "));
        assertEquals(" x", loop.endTrim(" x "));
        assertEquals("xaxa", loop.endTrim("xaxa "));
        assertEquals("xxx", loop.endTrim("xxx "));
    }
}
