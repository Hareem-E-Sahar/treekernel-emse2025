package com.neoworks.util;

import org.apache.log4j.Category;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.List;
import java.util.Vector;
import java.lang.Object;
import com.neoworks.util.OneBlock;
import com.neoworks.util.TwoBlock;
import java.lang.Class;
import java.lang.String;
import java.util.AbstractSet;
import java.util.AbstractCollection;
import java.util.SortedSet;
import java.lang.Boolean;
import java.lang.IllegalAccessException;
import java.lang.Throwable;
import java.lang.Exception;
import java.lang.InstantiationException;
import java.lang.Integer;
import java.lang.Number;
import java.lang.Error;
import java.lang.RuntimeException;
import java.lang.ClassNotFoundException;
import java.lang.NoClassDefFoundError;
import java.lang.LinkageError;
import java.util.Enumeration;
import java.util.ArrayList;

/**
 * Static methods for working with Collections and function blocks.
 * 
 * @author  Nigel Atkinson <a href="mailto:nigel@neoworks.com">nigel@neoworks.com</a>
 * @author  Nicko Cadell <a href="mailto:nicko@neoworks.com">nicko@neoworks.com</a>
 * @version $Revision: 1.1.1.1 $
 */
public class CollectionUtils {

    private static final Category log = Category.getInstance(CollectionUtils.class.getName());

    private static final boolean logDebugEnabled = log.isDebugEnabled();

    private static final boolean logInfoEnabled = log.isInfoEnabled();

    /**
	 * Private constructor to prevent construction
	 */
    private CollectionUtils() {
    }

    /**
	 * Put an Enumeration into something useful (a Collection, implemented as an ArrayList)
	 *
	 * @param e The Enumeration
	 * @return A Collection containing the contents of the Enumeration
	 */
    public static Collection Enumeration2Collection(Enumeration e) {
        return Enumeration2Collection(e, new ArrayList());
    }

    /**
	 * Put an Enumeration into a Collection of your choice
	 *
	 * @param e The Enumeration
	 * @param c The Collection to put the contents of the Enumeration into
	 * @return A Collection containing the contents of the Enumeration
	 */
    public static Collection Enumeration2Collection(Enumeration e, Collection c) {
        while (e.hasMoreElements()) {
            c.add(e.nextElement());
        }
        return c;
    }

    /**
	 * Get the intersection of two sets of homogeneous objects
	 *
	 * @param a The first Collection
	 * @param b The second Collection
	 * @return <code>Set</code> A set containing the intersection of the Collections
	 */
    public static Set getIntersection(Collection a, Collection b) {
        Set retSet = new TreeSet();
        Iterator i = (a.size() < b.size()) ? a.iterator() : b.iterator();
        Collection t = (a.size() < b.size()) ? b : a;
        Object o;
        while (i.hasNext()) {
            o = i.next();
            if (t.contains(o)) {
                retSet.add(o);
            }
        }
        return retSet;
    }

    /**
	 * return a collection containing only the elements from
	 * the collection passed which generated the result true
	 * when passed to the block.
	 * The one argument block must take an element from the collection
	 * as an arguemnt and must return a Boolean type.
	 * @param collection The input data collection
	 * @param block The predicate block
	 * @return A Collection containg only the input values accepted by the predicate block
	 */
    public static Collection collect(Collection collection, OneBlock block) {
        try {
            Collection list = (Collection) (collection.getClass().newInstance());
            Iterator iter = collection.iterator();
            while (iter.hasNext()) {
                Object data = iter.next();
                if (((Boolean) block.value(data)).booleanValue() == true) {
                    list.add(data);
                }
            }
            return list;
        } catch (java.lang.IllegalAccessException iaEx) {
        } catch (java.lang.InstantiationException iEx) {
        }
        return null;
    }

    /**
	 * This provides a fold function for collections.
	 * It requires a two argument block which takes an
	 * Object as its first argument and an element
	 * from the data collection as the second. It must
	 * return an Object which is passed to the next
	 * block with the next element.
	 * @param collection The input data collection
	 * @param defaultValue The initial value to pass to the first call to the block
	 * @param block The two argument block that folds the input data
	 * @return the Collection containing the result of the fold
	 */
    public static Object fold(Collection collection, Object defaultValue, TwoBlock block) {
        Object result = defaultValue;
        Iterator iter = collection.iterator();
        while (iter.hasNext()) {
            result = block.value(result, iter.next());
        }
        return result;
    }

    /**
	 * Detect if a collection meets some test. Each element in
	 * the collection is passed to the block. If the block returns
	 * true for any element then this function returns true.
	 * This will return as soon a positive result is found.
	 * Otherwise a result of false is returned.
	 * The one argument block must take an element from the collection
	 * as an arguemnt and must return a Boolean type.
	 * @param collection The input data collection
	 * @param block The predicate block
	 * @return true if any item in the input collection passes the predicate block test
	 */
    public static boolean detect(Collection collection, OneBlock block) {
        Iterator iter = collection.iterator();
        while (iter.hasNext()) {
            if (((Boolean) block.value(iter.next())).booleanValue() == true) {
                return true;
            }
        }
        return false;
    }

    /**
	 * Detect if a collection fails some test. Each element in 
	 * the collection is passed to the block. If the block returns
	 * false for any element then this function returns false.
	 * This will return as soon a negative result is found.
	 * Otherwise a result of true is returned.
	 * The one argument block must take an element from the collection
	 * as an arguemnt and must return a Boolean type.
	 * @param collection The input data collection
	 * @param block The predicate block
	 * @return false if any item in the input collection fails the predicate bolck test
	 */
    public static boolean reject(Collection collection, OneBlock block) {
        Iterator iter = collection.iterator();
        while (iter.hasNext()) {
            if (((Boolean) block.value(iter.next())).booleanValue() == false) {
                return false;
            }
        }
        return true;
    }

    /**
	 * Find the first value in a collection that meets some test. 
	 * Each element in the collection is passed to the block. 
	 * If the block returns true for the element then this function 
	 * returns the element. This will return as soon a positive result 
	 * is found. Otherwise a result of null is returned. The one argument 
	 * block must take an element from the collection as an arguemnt 
	 * and must return a Boolean type.
	 * @param collection The input data collection
	 * @param block The predicate block
	 * @return The first item from the input collection that passes the predicate block test
	 */
    public static Object findFirst(Collection collection, OneBlock block) {
        Iterator iter = collection.iterator();
        while (iter.hasNext()) {
            Object data = iter.next();
            if (((Boolean) block.value(data)).booleanValue() == true) {
                return data;
            }
        }
        return null;
    }

    /**
	 * Return a collection which is each element in the original
	 * collection mapped through the one argument function in the block
	 * The one argument block must take an element from the collection
	 * as an argument and must return an Object type.
	 * @param collection The input data collection
	 * @param block The remapping block
	 * @return A collection of the results of remapping the input data
	 */
    public static Collection map(Collection collection, OneBlock block) {
        try {
            Collection output = (Collection) (collection.getClass().newInstance());
            Iterator iter = collection.iterator();
            while (iter.hasNext()) {
                output.add(block.value(iter.next()));
            }
            return output;
        } catch (java.lang.IllegalAccessException iaEx) {
        } catch (java.lang.InstantiationException iEx) {
        }
        return null;
    }

    /**
	 * Return a Collection from an array
	 * @param array The input data array
	 * @param collectionType The type of Collection sub class to create
	 * @return A Collection object holding the data from the input array
	 */
    public static Collection arrayCollection(Object[] array, Class collectionType) {
        try {
            Collection coll = (Collection) collectionType.newInstance();
            for (int i = 0; i < array.length; i++) {
                coll.add(array[i]);
            }
            return coll;
        } catch (java.lang.IllegalAccessException iaEx) {
        } catch (java.lang.InstantiationException iEx) {
        }
        return null;
    }

    /**
	 * An implementation of Quicksort using medians of 3 for partitions.
	 * It is public and static so it can be used to sort any plain arrays.
	 * Time complexity: O(n log n).
	 *
	 * Example: to sort a string into lexical order (uses inner class)
	 * <pre>
	 * Collection data = ...  ;
	 * Collection sorted = Utils.quickSort(data ,new TwoBlock() {
	 *    public Object value(Object fst, Object snd)
	 *    {return new Integer(((String)fst).compareTo((String)snd));}});
	 * </pre>
	 * @return The sorted collection
	 * @see TwoBlock
	 * @param block A two argument block returning an Integer object that compares the two arguments. 
	 * It should have the same symantics as the java.lang.Comparable.compareTo() method.
	 * @param collection the collection to sort
	 */
    public static Collection quickSort(Collection collection, TwoBlock block) {
        Object[] arrayData = collection.toArray();
        quickSort(arrayData, 0, arrayData.length - 1, block);
        return arrayCollection(arrayData, collection.getClass());
    }

    /**
	 * An implementation of Quicksort using medians of 3 for partitions.
	 * It is public and static so it can be used to sort any plain arrays.
	 * Time complexity: O(n log n).
	 *
	 * Example: to sort a string into lexical order (uses inner class)
	 * <pre>
	 * String s[] = {"hello","sports","fans"};
	 * Utils.quickSort(s,0,s.length-1,new TwoBlock() {
	 *     public Object value(Object fst, Object snd)
	 *     {return new Integer(((String)fst).compareTo((String)snd));}});
	 * </pre>
	 * @param s   the array to sort
	 * @param lo  the least index to sort from
	 * @param hi  the greatest index
	 * @param block A two argument block returning an Integer object that compares the two arguments. 
	 * It should have the same symantics as the java.lang.Comparable.compareTo() method.
	 * @see TwoBlock
	 */
    public static void quickSort(Object s[], int lo, int hi, TwoBlock block) {
        if (lo >= hi) {
            return;
        }
        int mid = (lo + hi) / 2;
        if (((Integer) block.value(s[lo], s[mid])).intValue() > 0) {
            Object tmp = s[lo];
            s[lo] = s[mid];
            s[mid] = tmp;
        }
        if (((Integer) block.value(s[mid], s[hi])).intValue() > 0) {
            Object tmp = s[mid];
            s[mid] = s[hi];
            s[hi] = tmp;
            if (((Integer) block.value(s[lo], s[mid])).intValue() > 0) {
                Object tmp2 = s[lo];
                s[lo] = s[mid];
                s[mid] = tmp2;
            }
        }
        int left = lo + 1;
        int right = hi - 1;
        if (left >= right) {
            return;
        }
        Object partition = s[mid];
        for (; ; ) {
            while (((Integer) block.value(s[right], partition)).intValue() > 0) {
                --right;
            }
            while (left < right && ((Integer) block.value(s[left], partition)).intValue() <= 0) {
                ++left;
            }
            if (left < right) {
                Object tmp = s[left];
                s[left] = s[right];
                s[right] = tmp;
                --right;
            } else {
                break;
            }
        }
        quickSort(s, lo, left, block);
        quickSort(s, left + 1, hi, block);
    }
}
