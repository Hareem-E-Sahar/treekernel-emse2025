import junit.framework.*;

public class AlgorithmsTest extends TestCase {

    public AlgorithmsTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of isPalindrome method, of class Algorithms.
     */
    public void testIsPalindrome() {
        System.out.println("isPalindrome");
        assertEquals(Algorithms.isPalindrome("A"), true);
        assertEquals(Algorithms.isPalindrome("BA"), false);
        assertEquals(Algorithms.isPalindrome("BB"), true);
        assertEquals(Algorithms.isPalindrome("BAB"), true);
        assertEquals(Algorithms.isPalindrome("BAA"), false);
        assertEquals(Algorithms.isPalindrome("ABB"), false);
    }

    /**
     * Test of reverseString method, of class Algorithms.
     */
    public void testReverseString() {
        System.out.println("reverseString");
        assertEquals(Algorithms.reverseString("REVIVER"), "REVIVER");
    }
}
