package example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import sis.student.Student;
import junit.framework.TestCase;

public class VariedTest extends TestCase {

    public void testForSkip() {
        StringBuilder builder = new StringBuilder();
        String string = "123456";
        for (int i = 0; i < string.length() - 1; i += 2) {
            builder.append(string.charAt(i));
        }
        assertEquals("135", builder.toString());
    }

    public void testFibonacci() {
        assertEquals(0, fib(0));
        assertEquals(1, fib(1));
        assertEquals(1, fib(2));
        assertEquals(2, fib(3));
        assertEquals(3, fib(4));
        assertEquals(5, fib(5));
        assertEquals(8, fib(6));
        assertEquals(13, fib(7));
        assertEquals(21, fib(8));
        assertEquals(34, fib(9));
        assertEquals(55, fib(10));
    }

    private int fib(int x) {
        if (x == 0) return 0;
        if (x == 1) return 1;
        return fib(x - 1) + fib(x - 2);
    }

    public void testCommas() {
        String sequence = "1,2,3,4,5";
        assertEquals(sequence, sequenceUsingDo(1, 5));
        assertEquals(sequence, sequenceUsingFor(1, 5));
        assertEquals(sequence, sequenceUsingWhile(1, 5));
        sequence = "8";
        assertEquals(sequence, sequenceUsingDo(8, 8));
        assertEquals(sequence, sequenceUsingFor(8, 8));
        assertEquals(sequence, sequenceUsingWhile(8, 8));
    }

    private String sequenceUsingWhile(int start, int stop) {
        StringBuilder builder = new StringBuilder();
        int i = start;
        while (i <= stop) {
            if (i > start) builder.append(',');
            builder.append(i);
            i++;
        }
        return builder.toString();
    }

    private String sequenceUsingFor(int start, int stop) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i <= stop; i++) {
            if (i > start) builder.append(',');
            builder.append(i);
        }
        return builder.toString();
    }

    private String sequenceUsingDo(int start, int stop) {
        StringBuilder builder = new StringBuilder();
        int i = start;
        do {
            if (i > start) builder.append(',');
            builder.append(i);
        } while (++i <= stop);
        return builder.toString();
    }

    public void testLabelBreak() {
        List<List<String>> table = new ArrayList<List<String>>();
        List<String> row1 = new ArrayList<String>();
        row1.add("5");
        row1.add("2");
        List<String> row2 = new ArrayList<String>();
        row2.add("3");
        row2.add("4");
        table.add(row1);
        table.add(row2);
        assertTrue(found(table, "3"));
        assertFalse(found(table, "8"));
    }

    private boolean found(List<List<String>> table, String target) {
        boolean found = false;
        search: for (List<String> row : table) {
            for (String value : row) {
                if (value.equals(target)) {
                    found = true;
                    break search;
                }
            }
        }
        return found;
    }

    public void testCasting() {
        List students = new ArrayList();
        students.add(new Student("a"));
        students.add(new Student("b"));
        List names = new ArrayList();
        Iterator it = students.iterator();
        while (it.hasNext()) {
            Student student = (Student) it.next();
            names.add(student.getLastName());
        }
        assertEquals("a", names.get(0));
        assertEquals("b", names.get(1));
    }

    public void testTwoDimensionalArrays() {
        final int rows = 3;
        final int cols = 4;
        int count = 0;
        int[][] matrix = new int[rows][cols];
        for (int x = 0; x < rows; x++) for (int y = 0; y < cols; y++) matrix[x][y] = count++;
        assertEquals(11, matrix[2][3]);
        assertEquals(6, matrix[1][2]);
    }

    public void testPartialDidmensions() {
        final int rows = 3;
        int[][] matrix = new int[rows][];
        matrix[0] = new int[] { 0 };
        matrix[1] = new int[] { 1, 2 };
        matrix[2] = new int[] { 3, 4, 5 };
        assertEquals(1, matrix[1][0]);
        assertEquals(5, matrix[2][2]);
    }

    /**
	 * Reference ���� Not Equality...
	 */
    public void testArrayEquality() {
        int[] a = { 1, 2, 3 };
        int[] b = { 1, 2, 3 };
        assertFalse(a == b);
    }

    /**
	 * Reference ���� Not Equals...
	 */
    public void testArrayEquals() {
        int[] a = { 1, 2, 3 };
        int[] b = { 1, 2, 3 };
        assertFalse(a.equals(b));
        assertTrue(Arrays.equals(a, b));
    }
}
