package com.llq.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import com.llq.util.LlqUtil;

public class LlqUtilTest {

    @Test
    public void removeLastTest() {
        List<String> list = new ArrayList<String>();
        list.add("s1");
        list.add("s2");
        list.add("s3");
        assertEquals("s3", LlqUtil.removeLast(list));
        assertEquals("s2", LlqUtil.removeLast(list));
        assertEquals("s1", LlqUtil.removeLast(list));
        assertEquals("", LlqUtil.removeLast(list));
        assertEquals("", LlqUtil.removeLast(list));
    }

    @Test
    public void splitTest() {
        String string2 = "  luo   lin   qiang  ";
        assertEquals("luo", LlqUtil.split(string2).get(0));
        assertEquals("lin", LlqUtil.split(string2).get(1));
        assertEquals("qiang", LlqUtil.split(string2).get(2));
    }

    @Test
    public void countCharsTest() {
        assertEquals(4, LlqUtil.countChars("luo lin   qiang hello !", 'l'));
        assertEquals(1, LlqUtil.countChars("luo lin   qiang hello !", 'u'));
        assertEquals(2, LlqUtil.countChars("luo lin   qiang hello !", 'i'));
        assertEquals(6, LlqUtil.countChars("luo lin   qiang hello !", ' '));
        assertEquals(1, LlqUtil.countChars("luo lin   qiang hello !", '!'));
    }

    @Test
    public void palindromeTest() {
        assertEquals(0, 1 / 2);
        assertTrue(LlqUtil.isPalindrome(""));
        assertTrue(LlqUtil.isPalindrome("a"));
        assertTrue(LlqUtil.isPalindrome("aa"));
        assertTrue(LlqUtil.isPalindrome("aaa"));
        assertTrue(LlqUtil.isPalindrome("aaaa"));
        assertTrue(LlqUtil.isPalindrome("aba"));
        assertTrue(LlqUtil.isPalindrome("abba"));
        assertTrue(LlqUtil.isPalindrome("abcba"));
        assertTrue(LlqUtil.isPalindrome("abccba"));
        assertFalse(LlqUtil.isPalindrome("ab"));
        assertFalse(LlqUtil.isPalindrome("abc"));
        assertFalse(LlqUtil.isPalindrome("abca"));
        assertFalse(LlqUtil.isPalindrome("abab"));
        assertFalse(LlqUtil.isPalindrome("abb"));
        assertFalse(LlqUtil.isPalindrome("abbaa"));
        assertFalse(LlqUtil.isPalindrome("abcdef"));
    }

    @Test
    public void endTrimTest() {
        assertEquals("", LlqUtil.endTrim(""));
        assertEquals("", LlqUtil.endTrim("  "));
        assertEquals("  x", LlqUtil.endTrim("  x  "));
        assertEquals("y", LlqUtil.endTrim("y  "));
        assertEquals("y", LlqUtil.endTrim("y"));
        assertEquals("xxy", LlqUtil.endTrim("xxy"));
        assertEquals("xx y", LlqUtil.endTrim("xx y"));
        assertEquals(" x xy", LlqUtil.endTrim(" x xy	    "));
    }
}
