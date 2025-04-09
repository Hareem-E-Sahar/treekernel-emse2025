package net.sf.collections15.comparators;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Test class for FixedOrderComparator.
 *
 * @author David Leppik
 * @author Stephen Colebourne
 * @version $Revision: 1.2 $ $Date: 2004/10/17 01:12:17 $
 */
public class TestFixedOrderComparator extends TestCase {

    /**
     * Top cities of the world, by population including metro areas.
     */
    public static final String topCities[] = new String[] { "Tokyo", "Mexico City", "Mumbai", "Sao Paulo", "New York", "Shanghai", "Lagos", "Los Angeles", "Calcutta", "Buenos Aires" };

    public TestFixedOrderComparator(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(TestFixedOrderComparator.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Tests that the constructor plus add method compares items properly.
     */
    public void testConstructorPlusAdd() {
        FixedOrderComparator<String> comparator = FixedOrderComparator.getInstance();
        for (int i = 0; i < topCities.length; i++) {
            comparator.add(topCities[i]);
        }
        String[] keys = (String[]) topCities.clone();
        assertComparatorYieldsOrder(keys, comparator);
    }

    /**
     * Tests that the array constructor compares items properly.
     */
    public void testArrayConstructor() {
        String[] keys = (String[]) topCities.clone();
        String[] topCitiesForTest = (String[]) topCities.clone();
        FixedOrderComparator<String> comparator = FixedOrderComparator.getInstance(topCitiesForTest);
        assertComparatorYieldsOrder(keys, comparator);
        topCitiesForTest[0] = "Brighton";
        assertComparatorYieldsOrder(keys, comparator);
    }

    /**
     * Tests the list constructor.
     */
    public void testListConstructor() {
        String[] keys = (String[]) topCities.clone();
        List<String> topCitiesForTest = new LinkedList<String>(Arrays.asList(topCities));
        FixedOrderComparator<String> comparator = FixedOrderComparator.getInstance(topCitiesForTest);
        assertComparatorYieldsOrder(keys, comparator);
        topCitiesForTest.set(0, "Brighton");
        assertComparatorYieldsOrder(keys, comparator);
    }

    /**
     * Tests addAsEqual method.
     */
    public void testAddAsEqual() {
        FixedOrderComparator<String> comparator = FixedOrderComparator.getInstance(topCities);
        comparator.addAsEqual("New York", "Minneapolis");
        assertEquals(0, comparator.compare("New York", "Minneapolis"));
        assertEquals(-1, comparator.compare("Tokyo", "Minneapolis"));
        assertEquals(1, comparator.compare("Shanghai", "Minneapolis"));
    }

    /**
     * Tests whether or not updates are disabled after a comparison is made.
     */
    public void testLock() {
        FixedOrderComparator<String> comparator = FixedOrderComparator.getInstance(topCities);
        assertEquals(false, comparator.isLocked());
        comparator.compare("New York", "Tokyo");
        assertEquals(true, comparator.isLocked());
        try {
            comparator.add("Minneapolis");
            fail("Should have thrown an UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }
        try {
            comparator.addAsEqual("New York", "Minneapolis");
            fail("Should have thrown an UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }
    }

    public void testUnknownObjectBehavior() {
        FixedOrderComparator<String> comparator = FixedOrderComparator.getInstance(topCities);
        try {
            comparator.compare("New York", "Minneapolis");
            fail("Should have thrown an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            comparator.compare("Minneapolis", "New York");
            fail("Should have thrown a IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        assertEquals(FixedOrderComparator.UnknownObjectBehaviour.UNKNOWN_THROW_EXCEPTION, comparator.getUnknownObjectBehavior());
        comparator = FixedOrderComparator.getInstance(topCities);
        comparator.setUnknownObjectBehavior(FixedOrderComparator.UnknownObjectBehaviour.UNKNOWN_BEFORE);
        assertEquals(FixedOrderComparator.UnknownObjectBehaviour.UNKNOWN_BEFORE, comparator.getUnknownObjectBehavior());
        LinkedList<String> keys = new LinkedList<String>(Arrays.asList(topCities));
        keys.addFirst("Minneapolis");
        assertComparatorYieldsOrder(keys.toArray(new String[0]), comparator);
        assertEquals(-1, comparator.compare("Minneapolis", "New York"));
        assertEquals(1, comparator.compare("New York", "Minneapolis"));
        assertEquals(0, comparator.compare("Minneapolis", "St Paul"));
        comparator = FixedOrderComparator.getInstance(topCities);
        comparator.setUnknownObjectBehavior(FixedOrderComparator.UnknownObjectBehaviour.UNKNOWN_AFTER);
        keys = new LinkedList<String>(Arrays.asList(topCities));
        keys.add("Minneapolis");
        assertComparatorYieldsOrder(keys.toArray(new String[0]), comparator);
        assertEquals(1, comparator.compare("Minneapolis", "New York"));
        assertEquals(-1, comparator.compare("New York", "Minneapolis"));
        assertEquals(0, comparator.compare("Minneapolis", "St Paul"));
    }

    /**
     * Shuffles the keys and asserts that the comparator sorts them back to
     * their original order.
     */
    private void assertComparatorYieldsOrder(String[] orderedObjects, Comparator<String> comparator) {
        String[] keys = (String[]) orderedObjects.clone();
        boolean isInNewOrder = false;
        while (keys.length > 1 && isInNewOrder == false) {
            shuffle: {
                Random rand = new Random();
                for (int i = keys.length - 1; i > 0; i--) {
                    String swap = keys[i];
                    int j = rand.nextInt(i + 1);
                    keys[i] = keys[j];
                    keys[j] = swap;
                }
            }
            testShuffle: {
                for (int i = 0; i < keys.length && !isInNewOrder; i++) {
                    if (!orderedObjects[i].equals(keys[i])) {
                        isInNewOrder = true;
                    }
                }
            }
        }
        Arrays.sort(keys, comparator);
        for (int i = 0; i < orderedObjects.length; i++) {
            assertEquals(orderedObjects[i], keys[i]);
        }
    }
}
