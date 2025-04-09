package de.bugbusters.binpacking.algo;

import de.bugbusters.binpacking.exceptions.BinCreationException;
import de.bugbusters.binpacking.model.Bin;
import de.bugbusters.binpacking.model.Element;
import de.bugbusters.binpacking.model.Size;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * An abtract impl of {@link de.bugbusters.binpacking.algo.BinPacking} providing common methods used in each concrete impl
 *
 * @author Sven Kiesewetter
 */
public abstract class AbstractBinPacking implements BinPacking {

    public static final Class[] CONSTRUCTOR_PARAM_CLASSES = new Class[] { String.class, Size.class };

    private static final BySizeComparator BY_SIZE_COMPARATOR = new BySizeComparator();

    private String name;

    /**
     * Create a new instance of AbstractBinPacking
     *
     * @param name the name of the algorithm
     */
    protected AbstractBinPacking(String name) {
        this.name = name;
    }

    /**
     * Instantiates <code>binImpl</code> using <code>id</code> and <code>capacity</code> as the parameters
     *
     * @param binImpl  the {@link de.bugbusters.binpacking.model.Bin} impl to instantiate
     * @param id       the id to use
     * @param capacity the capacity to use
     * @return a new instance of <code>binImpl</code>
     * @throws BinCreationException if <code>binImpl</code> could not be instantiated
     */
    protected Bin createNewBin(Class binImpl, String id, Size capacity) throws BinCreationException {
        Constructor constructor;
        try {
            constructor = binImpl.getConstructor(CONSTRUCTOR_PARAM_CLASSES);
        } catch (NoSuchMethodException e) {
            throw new BinCreationException("Bin impl '" + binImpl.getName() + "' does not provide a constructor with the following parameters " + Arrays.asList(CONSTRUCTOR_PARAM_CLASSES), e);
        }
        try {
            return (Bin) constructor.newInstance(new Object[] { id, capacity });
        } catch (InstantiationException e) {
            throw new BinCreationException("Could not instantiate bin impl '" + binImpl.getName() + "'", e);
        } catch (IllegalAccessException e) {
            throw new BinCreationException("Could not instantiate bin impl '" + binImpl.getName() + "'", e);
        } catch (InvocationTargetException e) {
            throw new BinCreationException("Could not instantiate bin impl '" + binImpl.getName() + "'", e);
        }
    }

    /**
     * Sorts <code>elements</code> by size
     *
     * @param elements the {@link de.bugbusters.binpacking.model.Element}s to sort
     * @return a list of sorted {@link de.bugbusters.binpacking.model.Element}s
     */
    protected List sortElements(Set elements) {
        List sortedElements = new ArrayList(elements);
        Collections.sort(sortedElements, BY_SIZE_COMPARATOR);
        Collections.reverse(sortedElements);
        return sortedElements;
    }

    /**
     * Generates a new bin identifier using <code>number</code>
     *
     * @param number the number to use
     * @return a new bin identifier
     */
    protected String createNumberedBinId(int number) {
        return String.valueOf(number);
    }

    /**
     * @see de.bugbusters.binpacking.algo.BinPacking#getAlgorithmName()
     */
    public String getAlgorithmName() {
        return name;
    }

    /**
     * A comparator that is used by {@link AbstractBinPacking#sortElements(java.util.Set)}. It compares 2
     * {@link de.bugbusters.binpacking.model.Element}s by their sizes
     */
    private static class BySizeComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            Element e1 = (Element) o1;
            Element e2 = (Element) o2;
            return e1.getSize().compareTo(e2.getSize());
        }
    }
}
