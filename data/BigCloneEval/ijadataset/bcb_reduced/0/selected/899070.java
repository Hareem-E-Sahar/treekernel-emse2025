package neembuu.common;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.BitSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * RangeArray is like a {@link java.util.Vector } (Vector can be thought of as an expandable array)
 * who's maximum capacity is 9223372036854775798 elements (If only positive indices are used then  almost half of {@link Long#MAX_VALUE } ).<br/>
 * Capacity of a normal integer array on a 32-bit system, is less than 4294967296 (4294967296 is lesser than 9223372036854775798 ).<br/>
 * Also an integer array of such size would require around 16GB of RAM.<br/>
 * RangeArray can store such large number of values by grouping indices with same value.
 * (Which means RangeArrray will also not work if same value doesn't repeat itself sufficient number of times) <br/>
 * <br/>
 * Example :<br/>
 * <pre>
 * array index ::: 0 1 2 3 4 5 ...<br/>
 * value stored :: a a a b b c ...<br/>
 * Would be saved as :<br/>
 * <br/>
 * Index            :::   0     1     2   ...<br/>
 * Absolute index   ::: 0-->2 3-->4 5-->5 ...<br/> 
 * Value stored     :::   a     b     c   ...<br/>
 * </pre><br/>
 * <u>Note</u> :<ul>
 * <li> RangeArray indices are always inclusive, both starting and ending.
 * Example 5-->5 means only single 5, 6-->8 means 6,7&8
 * <li> In the above example, indices of the sort 0-->2 are called <b>absolute index</b>
 * <li> Range Values are stored in an array/Vector, indices of that array are simply called <b>index</b>
 * </ul>
 * <br/>
 * RangeArray carries only {@link RangeArrayElement }s.
 * An {@link RangeArrayElement } does not carry any property.
 * It must be extended in order to store property in it. <br/>
 * An example implementation of {@link RangeArrayElement } may be found here {@link ObjectRangeArrayElement } <br/>
 * <br/>
 * <br/>
 * <i>Not Important : </i><br/>
 * Bitmaping is not a good option always as it reduces resolution.<br/>
 * EXAMPLE : File systems often use 1-bit to store information about region of the disk (of size let's say 512kilobytes).<br/>
 * So if that 1-bit is 0 it could mean the region being represented is FREE for use,
 * if that 1-bit is 1 it would mean that the region being represented is NOT FREE .
 * But this method cannot tell us upto what extend that region of disk is USED UP or FREE.
 * It could mean that only 1byte is used up, or, the entire region of 512KB has been used up entirely. <br/>
 * This way resolution is reduced by bitmap approach.<br/>
 * Even so, bitmap consumes very little space and resolution high is not required is most cases.
 * Use of RangeArray in such case is not a good choice.<br/>
 * However, during a <a href=http://neembuu.sf.net/wiki/index.php/File_abstraction>File abstraction operation </a>
 * the download order and pattern can be very random. The progress information in such a case
 * would be more efficiently and losslessly stored in a RangeArray.
 * It will also be quickly accessible and changes can be much more easily observed.
 * This fact can be best appreciated by seeing an example (follow the File abstraction link given above)
 * <br/>
 * <br/>
 * @author Shashank Tulsyan
 * @param <C> The Type of RangeArrayElement that is to be stored in this RangeArray
 */
public class RangeArray<C extends RangeArrayElement> implements RangeArrayElementFactory, Collection<C>, RangeArrayElementFilter<C> {

    private static final boolean DEBUG = false;

    public static final long MIN_VALUE_SUPPORTED = (Long.MIN_VALUE / 2) + 4;

    public static final long MAX_VALUE_SUPPORTED = (Long.MAX_VALUE / 2) - 4;

    private volatile long fileSize;

    public static final long DEFAULT_FILE_SIZE = MAX_VALUE_SUPPORTED;

    public static final int DEFAULT_CAPACITY_INCREMENT = 10;

    protected final UnsynchronizedVector<C> store;

    private RangeArrayElementFactory<C> factory = null;

    private Class<? super C> elementClass = null;

    private final Object modLock = new Object();

    public final Object getModLock() {
        return modLock;
    }

    /**
     * The number of times this list has been <i>structurally modified</i>.
     * Structural modifications are those that change the size of the
     * list, or otherwise perturb it in such a fashion that iterations in
     * progress may yield incorrect results.
     *
     * <p>This field is used by the iterator and list iterator implementation
     * returned by the {@code iterator} and {@code listIterator} methods.
     * If the value of this field changes unexpectedly, the iterator (or list
     * iterator) will throw a {@code ConcurrentModificationException} in
     * response to the {@code next}, {@code remove}, {@code previous},
     * {@code set} or {@code add} operations.  This provides
     * <i>fail-fast</i> behavior, rather than non-deterministic behavior in
     * the face of concurrent modification during iteration.
     *
     * <p><b>Use of this field by subclasses is optional.</b> If a subclass
     * wishes to provide fail-fast iterators (and list iterators), then it
     * merely has to increment this field in its {@code add(int, E)} and
     * {@code remove(int)} methods (and any other methods that it overrides
     * that result in structural modifications to the list).  A single call to
     * {@code add(int, E)} or {@code remove(int)} must add no more than
     * one to this field, or the iterators (and list iterators) will throw
     * bogus {@code ConcurrentModificationExceptions}.  If an implementation
     * does not wish to provide fail-fast iterators, this field may be
     * ignored.
     */
    protected transient int modCount = 0;

    /**
     * @see RangeArray#RangeArray(int, neembuu.common.RangeArrayElementFactory) 
     */
    public RangeArray() {
        this(DEFAULT_CAPACITY_INCREMENT);
    }

    /**
     * @see RangeArray#RangeArray(int, neembuu.common.RangeArrayElementFactory)
     */
    public RangeArray(int capacityIncrement) {
        this(capacityIncrement, (RangeArrayElementFactory<C>) null);
        this.factory = this;
    }

    public RangeArray(UnsynchronizedVector<C> s) {
        this.store = s;
        if (s.size() < 2) throw new IllegalArgumentException("Store does not contain fake entry " + (RangeArray.MIN_VALUE_SUPPORTED - 3) + "-->" + (RangeArray.MIN_VALUE_SUPPORTED - 3) + "and " + (RangeArray.MAX_VALUE_SUPPORTED + 3) + "-->" + (RangeArray.MAX_VALUE_SUPPORTED + 3));
        if (s.get(0).starting() == s.get(0).ending() && s.get(0).ending() == RangeArray.MIN_VALUE_SUPPORTED - 3) {
        } else throw new IllegalArgumentException("Store does not contain fake entry " + (RangeArray.MIN_VALUE_SUPPORTED - 3) + "-->" + (RangeArray.MIN_VALUE_SUPPORTED - 3));
        if (s.get(s.size() - 1).starting() == s.get(s.size() - 1).ending() && s.get(s.size() - 1).ending() == RangeArray.MAX_VALUE_SUPPORTED + 3) {
        } else throw new IllegalArgumentException("Store does not contain fake entry " + (RangeArray.MAX_VALUE_SUPPORTED + 3) + "-->" + (RangeArray.MAX_VALUE_SUPPORTED + 3));
        long lastEndOffset = MIN_VALUE_SUPPORTED - 3;
        for (int i = 1; i < s.size() - 1; i++) {
            C element = s.get(i);
            element.checkRange(element.starting(), element.ending());
            if (lastEndOffset > element.starting()) {
                throw new IllegalArgumentException("Elements in store are over laping");
            }
        }
    }

    private RangeArray(int capacityIncrement, UnsynchronizedVector uv) {
        if (capacityIncrement < 0) throw new IllegalArgumentException("Illegal Capacity increment=" + capacityIncrement);
        store = uv;
        this.factory = this;
        C x = (C) this.factory.newInstance(MIN_VALUE_SUPPORTED - 3, MIN_VALUE_SUPPORTED - 3), y = (C) this.factory.newInstance(MAX_VALUE_SUPPORTED + 3, MAX_VALUE_SUPPORTED + 3);
        this.fileSize = DEFAULT_FILE_SIZE;
    }

    /**
     *
     * @see RangeArray#RangeArray(int, java.lang.Class, long)
     */
    public RangeArray(int capacityIncrement, Class<? super C> elementClass) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        this(capacityIncrement, elementClass, DEFAULT_FILE_SIZE);
    }

    /**
     * Instead of making a separate factory class, implementors
     * can also pass an object of the class of the elements being added.</p>
     * But in such a case they must ensure that the RangeArrayElement implementation
     * that they create, constain constructors as in the following example as :
     * <pre>
     * class CustomRangeArrayElement extends RangeArrayElement {
     *      public CustomRangeArrayElement(){
     *          super();
     *          //.... anything more if required
     *      }
     *      public CustomRangeArrayElement(long s,long e){
     *          super(s,e);
     *          //.... anything more if required
     *      }
     *      //other constructors if any
     *
     * //....
     * }
     * </pre>
     * And we would make a RangeArray Object as :
     * <pre>
     * RangeArray<CustomRangeArrayElement> rangeArray = new RangeArray<CustomRangeArrayElement>(
     *          RangeArray.DEFAULT_CAPACITY_INCREMENT,
     *          CustomRangeArrayElement.class
     *      );
     * </pre>
     * In the above example fileSize is set to, {@link RangeArray#DEFAULT_FILE_SIZE }
     * @param capacityIncrement the amount by which the capacity is
     *                              increased when the vector(if this implementation uses one) overflows
     * @param elementClass The object of the class of the element that will be added to this
     * @param fileSize The size of the abstract file (if any) being represented by this
     * @throws  NoSuchMethodException These exception are thrown only during failed invocation
     *      of {@link Constructor#newInstance(java.lang.Object[]) } by this constructor.
     * @throws  InstantiationException
     * @throws  IllegalAccessException
     * @throws  InvocationTargetException
     */
    public RangeArray(int capacityIncrement, Class<? super C> elementClass, long fileSize) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if (capacityIncrement < 0) throw new IllegalArgumentException("Illegal Capacity increment=" + capacityIncrement);
        store = new UnsynchronizedVector<C>(0, capacityIncrement);
        if (elementClass == null) {
            throw new IllegalArgumentException("RangeArrayElement\'s class should not be null.For default functionality use new RangeArray() instead.");
        } else {
            this.elementClass = elementClass;
        }
        C x = (C) (elementClass.getConstructor(boolean.class).newInstance(true)), y = (C) (elementClass.getConstructor(boolean.class).newInstance(false));
        store.add(x);
        store.add(y);
        this.fileSize = fileSize;
    }

    /**
     * @see RangeArray#RangeArray(int, neembuu.common.RangeArrayElementFactory, long)
     * @param initialCapacity
     * @param capacityIncrement
     * @param factory 
     * @throws ClassCastException If appropriate factory is not provided.
     */
    public RangeArray(int capacityIncrement, RangeArrayElementFactory<C> factory) throws ClassCastException {
        this(capacityIncrement, factory, DEFAULT_FILE_SIZE);
    }

    /**
     * Making a separate factory for custom RangeArrayElement is a safer option
     * than using {@link RangeArray#RangeArray(int, java.lang.Class, long) }
     * @see {@link ObjectRangeArrayElement}
     * @param capacityIncrement the amount by which the capacity is
     *                              increased when the vector(if this implementation uses one) overflows
     * @param factory Used for creating instances of C (C extends RangeArrayElement )
     * @param fileSize The size of the abstract file (if any) being represented by this
     * @throws ClassCastException
     */
    public RangeArray(int capacityIncrement, RangeArrayElementFactory<C> factory, long fileSize) throws ClassCastException {
        if (capacityIncrement < 0) throw new IllegalArgumentException("Illegal Capacity increment=" + capacityIncrement);
        store = new UnsynchronizedVector<C>(0, capacityIncrement);
        if (factory != null) this.factory = factory; else {
            this.factory = this;
        }
        C x = (C) this.factory.newInstance(MIN_VALUE_SUPPORTED - 3, MIN_VALUE_SUPPORTED - 3), y = (C) this.factory.newInstance(MAX_VALUE_SUPPORTED + 3, MAX_VALUE_SUPPORTED + 3);
        store.add(x);
        store.add(y);
        this.fileSize = fileSize;
    }

    private final int length() {
        return store.size();
    }

    private int[] indexOf(C sc) {
        int[] ret = { 0, 0 };
        long[] x = { sc.starting(), sc.ending() };
        int mid, lb, ub;
        boolean[] done_x = { false, false };
        boolean mide, pullupperbound, pulllowerbound;
        if (DEBUG) System.out.println("                  inside indexOf");
        boolean cs1, cs2;
        for (int i = 0; i < 2; i++) {
            lb = 0;
            ub = length() * 2;
            do {
                mid = (lb + ub) / 2;
                mide = mid % 2 == 0;
                if (DEBUG) System.out.println();
                if (DEBUG) System.out.println("               mid=" + mid + " " + getOff(mid) + " " + getOff(mid + 1) + " i=" + i + "done i=" + done_x[i]);
                cs1 = cs2 = pulllowerbound = pullupperbound = false;
                if (i == 0) {
                    if (mide) {
                        if (getOff(mid) + 1 <= x[0]) cs1 = true;
                        if (x[0] <= getOff(mid + 1) + 1) cs2 = true;
                        if (cs1 && cs2) {
                            ret[0] = mid;
                            done_x[0] = true;
                        } else if (!cs1 && cs2) {
                            pulllowerbound = false;
                            pullupperbound = true;
                        } else if (cs1 && !cs2) {
                            pulllowerbound = true;
                            pullupperbound = false;
                        }
                    } else {
                        if (getOff(mid) + 2 <= x[0]) cs1 = true;
                        if (x[0] <= getOff(mid + 1)) cs2 = true;
                        if (cs1 && cs2) {
                            ret[0] = mid;
                            done_x[0] = true;
                        } else if (!cs1 && cs2) {
                            pulllowerbound = false;
                            pullupperbound = true;
                        } else if (cs1 && !cs2) {
                            pulllowerbound = true;
                            pullupperbound = false;
                        }
                    }
                } else {
                    if (mide) {
                        if (getOff(mid) - 1 <= x[1]) cs1 = true;
                        if (x[1] <= getOff(mid + 1) - 1) cs2 = true;
                        if (cs1 && cs2) {
                            ret[1] = mid;
                            done_x[1] = true;
                        } else if (!cs1 && cs2) {
                            pulllowerbound = false;
                            pullupperbound = true;
                        } else if (cs1 && !cs2) {
                            pulllowerbound = true;
                            pullupperbound = false;
                        }
                    } else {
                        if (getOff(mid) <= x[1]) cs1 = true;
                        if (x[1] <= getOff(mid + 1) - 2) cs2 = true;
                        if (cs1 && cs2) {
                            ret[1] = mid;
                            done_x[1] = true;
                        } else if (!cs1 && cs2) {
                            pulllowerbound = false;
                            pullupperbound = true;
                        } else if (cs1 && !cs2) {
                            pulllowerbound = true;
                            pullupperbound = false;
                        }
                    }
                }
                if (DEBUG) System.out.println("              " + cs1 + " " + cs2 + " " + pullupperbound + " " + pulllowerbound);
                if (pullupperbound) ub = mid - 1;
                if (pulllowerbound) lb = mid + 1;
            } while (lb <= ub && !done_x[i]);
        }
        if (DEBUG) System.out.println("                done indexOf " + x[0] + " " + x[1]);
        if (DEBUG) System.out.println("ret=" + ret[0] + " " + ret[1]);
        return (ret);
    }

    private void twoNewElements(int i) {
        insertXnewElements(i, 2);
    }

    /**
     * index i is wrt to getOff(]; so to get equivalent storage point on other arrays, we use i/2
     * same is case with all other operations on data arrays
     */
    private final void newElement(int i) {
        insertXnewElements(i, 1);
    }

    /**
     * 
     * @param i
     * @param x
     */
    private final void insertXnewElements(int i, int x) {
        modCount++;
        if (DEBUG) System.out.println("i=" + i);
        if (DEBUG) System.out.println("x=" + x);
        for (int j = 0; j < x; j++) {
            store.insertElementAt(makeNewInstance(), i / 2 + 1);
        }
    }

    private C makeNewInstance(long s, long e) {
        try {
            if (factory != null) return (C) (factory.newInstance(s, e)); else return (C) (elementClass.getConstructor(long.class, long.class).newInstance(s, e));
        } catch (Exception any) {
            return null;
        }
    }

    private C makeNewInstance() {
        try {
            if (factory != null) return factory.newInstance(); else return (C) (elementClass.getConstructor().newInstance());
        } catch (Exception any) {
            any.printStackTrace();
            return null;
        }
    }

    /**
     * Remove elements from <b>startIndex</b> to <b>endIndex</b> (these indices are wrt getOff(]) inclusive of start and start
     * @param startIndex
     * @param endIndex
     */
    private final void removeElements(int startIndex, int endIndex) {
        modCount++;
        store.removeRange(startIndex / 2, (endIndex / 2) + 1);
    }

    /**
     * Makes and returns a new copy of this RangeArray
     * @return A new copy of this RangeArray 
     */
    public final RangeArray<C> copy() {
        UnsynchronizedVector<C> cs;
        synchronized (modLock) {
            cs = (UnsynchronizedVector) store.clone();
        }
        RangeArray<C> ret = new RangeArray(this.store.capacityIncrement, cs);
        if (this.factory != null) ret.factory = this.factory;
        if (this.elementClass != null) ret.elementClass = this.elementClass;
        return ret;
    }

    @Override
    public String toString() {
        return toString_();
    }

    /**
     * Similar to {@link Object#toString() } with a custom name String
     * used for representing this RangeArray, instead of
     * less informative super.toString() .
     * @param rangeArrayDisplayName The display name to be used to represent this RangeArray
     * @return  a string representation of the object with given display name
     */
    private final String toString_() {
        StringBuilder br = new StringBuilder(100);
        br.append("{\n");
        for (int j = 1; j < length() - 1; j++) {
            br.append(" index=");
            br.append(j - 1);
            br.append(" ");
            br.append(store.elementAt(j));
            br.append(" ");
            br.append((store.elementAt(j).ending() - store.elementAt(j).starting() + 1));
            br.append("\n");
        }
        br.append("}");
        return br.toString();
    }

    /**
     * Adds an entry in this RangeArray.
     * Removes an entry if it is an antagonist entry ( {@link RangeArrayElement#isAnAntagonistEntry() } )
     * @param newEntry The new entry which is to be added to this RangeArray
     * @see #removeElement(neembuu.common.RangeArrayElement)
     * @see RangeArrayElement#isAnAntagonistEntry()
     * @throws RangeArrayElementRejectedByFilterException If file size was set to some value which is lesser than
     * {@link #MAX_VALUE_SUPPORTED } and the element being added is greater than this value
     */
    public final void addElement(C newEntry) {
        if (!canBeAnElementOfThis(newEntry)) throw new RangeArrayElementRejectedByFilterException();
        if (newEntry.isAnAntagonistEntry()) {
            removeElement(newEntry.starting(), newEntry.ending());
            return;
        }
        long start = newEntry.starting(), end = newEntry.ending();
        int[] ind;
        ind = indexOf(newEntry);
        int x1 = ind[0], x2 = ind[1];
        boolean x1e = x1 % 2 == 0, x2e = x2 % 2 == 0;
        boolean eq = x1 == x2;
        boolean dx1 = x1e ? newEntry.dissolves(store.elementAt(x1 / 2)) : false, dx2 = x2e ? newEntry.dissolves(store.elementAt(x2 / 2)) : false;
        if (DEBUG) {
            System.out.println("Adding entry");
            System.out.print("x1=" + x1);
            System.out.print(" x2=" + x2);
            System.out.print(" x1e=" + x1e);
            System.out.print(" x2e=" + x2e);
            System.out.print(" eq=" + eq);
            System.out.print(" dx1=" + dx1);
            System.out.println(" dx2=" + dx2);
        }
        findCaseAndAdd(start, end, x1, x2, dx1, dx2, newEntry);
    }

    /**
     * Removes an entry, whatever it'start value maybe, (even if it exists fully, partially or not all)
     * from this RangeArray, specified by given value of <b>start</b> and <b>end</b>.
     * If an entry with a given property has to be removed, it should be done by ADDING
     * an entry with the correct property using {@link #addElement(neembuu.common.RangeArrayElement) } , this
     * new entry will replace the old entry.<br/>
     * <br/>
     * Throws exceptions, details can be read here {@link RangeArrayElement#checkRange(long, long) }
     * <br/>
     * This function is equivalent to adding a value in this RangeArray
     * whoes {@link RangeArrayElement#isAnAntagonistEntry() } returns true;
     * @param start The starting value of entry which is to be removed from this RangeArray
     * @param end The ending value of entry which is to be removed from this RangeArray
     * @see #addElement(neembuu.common.RangeArrayElement)
     */
    public final void removeElement(long start, long end) {
        synchronized (modLock) {
            C entryToRemove = makeNewInstance(start, end);
            int[] ind;
            ind = indexOf(entryToRemove);
            int x1 = ind[0], x2 = ind[1];
            boolean x1e = x1 % 2 == 0, x2e = x2 % 2 == 0;
            boolean eq = x1 == x2;
            boolean dx1 = false, dx2 = false;
            findCaseAndAdd(start, end, x1, x2, dx1, dx2, entryToRemove);
            for (int j = 0; j < store.size(); j++) {
                if (store.get(j).equalsIgnoreProperty(entryToRemove)) {
                    store.remove(j);
                    return;
                }
            }
        }
    }

    private final void findCaseAndAdd(long s, long e, int x1, int x2, boolean dx1, boolean dx2, C newEntry) {
        boolean x1e = x1 % 2 == 0, x2e = x2 % 2 == 0;
        boolean eq = x1 == x2;
        if (eq) {
            if (x1e) {
                if (dx1) {
                    return;
                } else {
                    twoNewElements(x1);
                    modifyEntry(x1 + 4, e + 1, getOff(x1 + 1), store.elementAt(x1 / 2));
                    modifyEntry(x1, getOff(x1), s - 1, store.elementAt(x1 / 2));
                    modifyEntry(x1 + 2, s, e, newEntry);
                    modifiedAround(getOff(x1), getOff(x1 + 1));
                    return;
                }
            } else {
                newElement(x1 - 1);
                modifyEntry(x1 + 1, s, e, newEntry, OPERATION.CS2);
                modifiedAround(s, e);
                return;
            }
        }
        if (!x1e && !x2e) {
            modifyEntry(x1 + 1, s, e, newEntry);
            if (x2 - x1 >= 4) {
                removeElements(x1 + 3, x2 - 1);
            }
            modifiedAround(s, e);
            return;
        }
        if (x1e && x2e && !dx1 && !dx2) {
            modifyEntry(x1, getOff(x1), s - 1, store.elementAt(x1 / 2));
            modifyEntry(x2, e + 1, getOff(x2 + 1), store.elementAt(x2 / 2));
            if (x2 - x1 == 2) {
                newElement(x1);
                modifyEntry(x1 + 2, s, e, newEntry);
                modifiedAround(s, e);
                return;
            }
            if (x2 - x1 == 4) {
                modifyEntry(x1 + 2, s, e, newEntry);
                modifiedAround(s, e);
                return;
            }
            if (x2 - x1 >= 6) {
                modifyEntry(x1 + 2, s, e, newEntry);
                removeElements(x1 + 4, x2 - 2);
                modifiedAround(s, e);
                return;
            }
            modifiedAround(s, e);
            return;
        }
        if (!x2e) {
            if (dx1) {
                if (x2 - x1 == 1) {
                    modifyEntry(x1, getOff(x1), e, store.elementAt(x1 / 2));
                    modifiedAround(getOff(x1), e);
                    return;
                }
                modifyEntry(x1, getOff(x1), e, store.elementAt(x1 / 2));
                removeElements(x1 + 2, x2 - 1);
                modifiedAround(getOff(x1), e);
                return;
            }
            modifyEntry(x1, getOff(x1), s - 1, store.elementAt(x1 / 2));
            if (x2 - x1 == 1) {
                newElement(x1);
                modifyEntry(x1 + 2, s, e, newEntry);
                modifiedAround(s, e);
                return;
            }
            modifyEntry(x1 + 2, s, e, newEntry);
            if (x2 - x1 >= 5) {
                removeElements(x1 + 4, x2 - 1);
            }
            modifiedAround(s, e);
            return;
        }
        if (!x1e) {
            if (dx2) {
                if (x2 - x1 == 1) {
                    modifyEntry(x2, s, getOff(x2 + 1), newEntry);
                    modifiedAround(s, getOff(x2 + 1));
                    return;
                }
                modifyEntry(x2, s, getOff(x2 + 1), newEntry);
                long offset_x2p1 = getOff(x2 + 1);
                removeElements(x1 + 1, x2 - 2);
                modifiedAround(s, offset_x2p1);
                return;
            }
            modifyEntry(x2, e + 1, getOff(x2 + 1), store.elementAt(x2 / 2));
            if (x2 - x1 == 1) {
                newElement(x1 - 1);
                modifyEntry(x2, s, e, newEntry);
                modifiedAround(s, e);
                return;
            }
            modifyEntry(x2 - 2, s, e, newEntry);
            if (x2 - x1 >= 5) {
                removeElements(x1 + 1, x2 - 4);
            }
            modifiedAround(s, e);
            return;
        }
        if (x1e && x2e) {
            if (dx1) {
                if (dx2) {
                    modifyEntry(x1, getOff(x1), getOff(x2 + 1), null);
                    long offset_c = getOff(x1), offset_j = getOff(x2 + 1);
                    removeElements(x1 + 2, x2);
                    modifiedAround(offset_c, offset_j);
                    return;
                }
                long offset_c = getOff(x1), offset_f = getOff(x2 + 1);
                modifyEntry(x1, getOff(x1), e, null);
                modifyEntry(x2, e + 1, getOff(x2 + 1), store.elementAt(x2 / 2));
                removeElements(x1 + 2, x2 - 2);
                modifiedAround(offset_c, offset_f);
                return;
            }
            if (dx2) {
                long offset_c = getOff(x1), offset_f = getOff(x2 + 1);
                modifyEntry(x1, getOff(x1), s - 1, store.elementAt(x1 / 2));
                modifyEntry(x2, s, getOff(x2 + 1), null);
                removeElements(x1 + 2, x2 - 2);
                modifiedAround(offset_c, offset_f);
                return;
            }
        }
    }

    /**
     * Modify entry at i th index (wrt getOff(]) and place following values
     */
    private final void modifyEntry(int i, long s, long e, C copyPropertiesFrom) {
        try {
            if (copyPropertiesFrom != null) {
                store.elementAt(i / 2).copyPropertiesFrom(copyPropertiesFrom);
            }
            store.elementAt(i / 2).setStarting(s);
            store.elementAt(i / 2).setEnding(e);
        } catch (ArrayIndexOutOfBoundsException a) {
            a.printStackTrace();
        }
    }

    private void modifyEntry(int i, long s, long e, C copyPropertiesFrom, OPERATION hint) {
        try {
            if (copyPropertiesFrom != null) {
                if (hint == OPERATION.CS2) {
                    store.setElementAt(copyPropertiesFrom, i / 2);
                } else if (factory != null) {
                    if (factory.entriesNeverDissolve()) {
                        store.setElementAt(copyPropertiesFrom, i / 2);
                    }
                } else if (elementClass != null) {
                    if (copyPropertiesFrom.entriesNeverDissolve()) {
                        store.setElementAt(copyPropertiesFrom, i / 2);
                    }
                } else {
                    store.elementAt(i / 2).copyPropertiesFrom(copyPropertiesFrom);
                }
            }
            store.elementAt(i / 2).setStarting(s);
            store.elementAt(i / 2).setEnding(e);
        } catch (ArrayIndexOutOfBoundsException a) {
            a.printStackTrace();
        }
    }

    private long getOff(int i) {
        if (i % 2 == 0) {
            return store.elementAt(i / 2).starting();
        }
        return store.elementAt(i / 2).ending();
    }

    private void setOff(int i, long v) {
        if (i % 2 == 0) {
            store.elementAt(i / 2).setStarting(v);
            return;
        }
        store.elementAt(i / 2).setEnding(v);
    }

    @Override
    public RangeArrayElement newInstance(long start, long end) {
        return new RangeArrayElement(start, end, true);
    }

    @Override
    public RangeArrayElement newInstance() {
        return new RangeArrayElement(0, 0, true);
    }

    @Override
    public boolean entriesNeverDissolve() {
        return false;
    }

    public final RangeArray subRange(RangeArrayElement bounds) {
        synchronized (modLock) {
            int lower_index = this.indexOf(bounds.starting());
            if (lower_index < 0) lower_index = 0;
            if (lower_index >= this.size() * 2 - 1) {
                return null;
            }
            if (lower_index % 2 != 0) lower_index++;
            lower_index /= 2;
            int higher_index = this.indexOf(bounds.ending());
            if (higher_index < 0) higher_index = 0;
            if (higher_index >= this.size() * 2 - 1) {
                return null;
            }
            if (higher_index % 2 != 0) higher_index++;
            higher_index /= 2;
            RangeArray rangeArray = new RangeArray();
            rangeArray.add(this.get(lower_index));
            if (lower_index == higher_index) {
                return rangeArray;
            }
            System.arraycopy(store.elementData, lower_index + 2, rangeArray.store.elementData, 2, higher_index - lower_index + 1);
            return rangeArray;
        }
    }

    public final BitSet getNewBitSetView(int blockSize) {
        RangeArrayElement bounds = new RangeArrayElement(this.get(0).starting(), this.getFileSize());
        return getNewBitSetView(bounds, blockSize);
    }

    public final BitSet getNewBitSetView(RangeArrayElement bounds, int blockSize) {
        if (bounds.getSize() % blockSize != 0) throw new IllegalArgumentException("bounds.getSize()/blockSize should be and integer");
        BitSet bitSet = new BitSet((int) (bounds.getSize() / blockSize));
        for (int j = 0; j < bitSet.length(); j++) {
            bitSet.set(j, containsCompletely(j, blockSize));
        }
        return bitSet;
    }

    /**
     * Used for making BitSet view out of a RangeArray
     * @param n the number of the piece, couting starts from 0
     * @elementSize Size of the bittorrent piece or block, whichever is required
     * @return true if and only if the region n*pieceSize--->(n+1)*pieceSize-1 exists fully
     */
    public final boolean containsCompletely(int n, long elementSize) {
        return containsCompletely(n, elementSize, true);
    }

    /**
     * Same as {@link #containsCompletely(int, long) }
     * @param dissovabilityComplex Is true implies that entries which have the same value
     * might also not dissolve because they are intrinsically undissolvable. In such a case
     * size of each entry has to be added to check is the given region exists as sum of more than
     * one entry
     * @return true if and only if the region n*pieceSize--->(n+1)*pieceSize-1 exists fully in
     * a single entry or multiple entries (dissovabilityComplex set to true)
     */
    public final boolean containsCompletely(int n, long elementSize, boolean dissovabilityComplex) {
        synchronized (modLock) {
            int lower_index = this.indexOf(n * elementSize);
            if (lower_index < 0) {
                return false;
            }
            if (lower_index >= this.size() * 2 - 1) {
                return false;
            }
            if (lower_index % 2 != 0) {
                return false;
            }
            lower_index /= 2;
            int higher_index = this.indexOf((n + 1) * elementSize - 1);
            if (higher_index < 0) return false;
            if (higher_index >= this.size() * 2 - 1) return false;
            if (higher_index % 2 != 0) return false;
            higher_index /= 2;
            if (lower_index == higher_index) return true;
            if (dissovabilityComplex) return false;
            long totalsize = 0;
            totalsize += this.get(lower_index).ending() - n * elementSize + 1;
            totalsize += (n + 1) * elementSize - this.get(higher_index).starting();
            for (int idx = lower_index + 1; idx < this.size() && idx < higher_index; idx++) {
                totalsize += this.get(idx).getSize();
            }
            if ((n + 1) * elementSize > getFileSize()) {
                elementSize = getFileSize() - n * elementSize;
            }
            return (totalsize == elementSize);
        }
    }

    /**
     * Returns -1 or size()*2-1 if not found.
     * If found between two different entries returns an odd value.<br/>
     * If found inside a entry returns an even value, which is twice the index at which it was found.<br/>
     * Example :<br/>
     * <br/>
     * <pre>
     * neembuu.common.RangeArray@c20e24{
     *  index=0 -100->-91 10
     *  index=1 0->1 2
     *  index=2 100->109 10
     * }
     * indexOf(-1000)=-1   // not found
     * indexOf(-1)=1       // this means not found, and exists between index [1/2] = 0 and ]1/2[ = 1
     * indexOf(-100)=0     // found at index 0/2 = 0
     * indexOf(0)=2        // found at index 2/2 = 1
     * indexOf(100)=4      // found at index 4/2 = 2
     * indexOf(109)=4      // found at index 4/2 = 2
     * indexOf(111)=5      // this means not found, and exists between index [5/2] = 2  and ]5/2[ = 3 (if 3 existed)
     * </pre>
     * <br/>
     * <br/>
     *
     * @see #contains(java.lang.Object) Refer to the source code of RangeArray#contains(java.lang.Object)
     * @param absoluteIndex
     * @return twice the actual index of given absoluteIndex
     */
    public final int indexOf(long absoluteIndex) {
        int index = getIndexOf(absoluteIndex);
        index = index - 2;
        return index;
    }

    /**
     * this is same as indexOf but indexing system counts fake entries.
     * @param absoluteIndex
     * @return 
     */
    private final int getIndexOf(long absoluteIndex) {
        int mid, lb, ub;
        int index = -1;
        boolean mide, pullupperbound, pulllowerbound;
        boolean cs1, cs2;
        int numberOfItr = 0;
        int length = length() * 2;
        lb = 0;
        ub = length() * 2;
        do {
            mid = (lb + ub) / 2;
            mide = mid % 2 == 0;
            cs1 = cs2 = pulllowerbound = pullupperbound = false;
            if (numberOfItr > length) {
                if (numberOfItr > 2 * length) {
                    throw new RuntimeException("Stuck in an infinite loop in getIndexOf(long ) due to malformed RangeArray");
                }
                System.err.println("Stuck in an infinite loop in getIndexOf(long ). getOff[mid=" + mid + "]=" + getOff(mid));
            }
            numberOfItr++;
            if (mide) {
                if (getOff(mid) <= absoluteIndex) cs1 = true;
                if (absoluteIndex <= getOff(mid + 1)) cs2 = true;
                if (cs1 && cs2) {
                    index = mid;
                } else if (!cs1 && cs2) {
                    pulllowerbound = false;
                    pullupperbound = true;
                } else if (cs1 && !cs2) {
                    pulllowerbound = true;
                    pullupperbound = false;
                }
            } else {
                if (getOff(mid) < absoluteIndex) cs1 = true;
                if (absoluteIndex < getOff(mid + 1)) cs2 = true;
                if (cs1 && cs2) {
                    index = mid;
                } else if (!cs1 && cs2) {
                    pulllowerbound = false;
                    pullupperbound = true;
                } else if (cs1 && !cs2) {
                    pulllowerbound = true;
                    pullupperbound = false;
                }
            }
            if (pullupperbound) ub = mid - 1;
            if (pulllowerbound) lb = mid + 1;
        } while (lb <= ub && index == -1);
        return index;
    }

    /**
     * Returns the RangeArrayElement at the specified position in this.
     *
     * <pre>
     *     0           1
     * 123--->212 4212--->11343
     *  propertyA  propertyB
     * </pre>
     *
     * Value at index 0 is propertyA and at 1 is propertyB <br/>
     * Value at absoluteIndex 123,124,125..... and 212 is propertyA. <br/>
     * Value at absoluteIndex 4212,4213.... and 11343  is propertyB. <br/>
     * @param absoluteIndex The absolute RangeArray index
     * @return the RangeArrayElement at the specified position in this list.
     * @see RangeArray#RangeArray(int, neembuu.common.RangeArrayElementFactory)
     */
    public C get(long absoluteIndex) {
        synchronized (modLock) {
            int index = getIndexOf(absoluteIndex);
            if (index < 2 || index >= store.size() * 2 - 1) return null;
            if (index % 2 == 1) return null;
            return store.get(index / 2);
        }
    }

    /**
     * Same as RangeArray#get(int) but allow access to fake entries.
     * The index system in both is same. <br/>
     * In RangeArray#get_checkFakeAsWell(int index) , at index=-1 and index=RangeArray#size()+1
     * return values instead of throwing ArrayIndexOutOfBoundsException.<br/>
     * @see #get(int)
     * @param index
     * @return  
     */
    public C get_checkFakeAsWell(int index) {
        if (index < 1 || index >= store.size() + 1) throw new ArrayIndexOutOfBoundsException(index);
        return store.get(index + 1);
    }

    /**
     * Returns the RangeArrayElement at the specified position in this.
     *
     * <pre>
     *     0           1
     * 123--->212 4212--->11343
     *  propertyA  propertyB
     * </pre>
     *
     * Value at index 0 is propertyA and at 1 is propertyB <br/>
     * Value at absoluteIndex 123,124,125..... and 212 is propertyA. <br/>
     * Value at absoluteIndex 4212,4213.... and 11343  is propertyB. <br/>
     * @param index The index
     * @return the RangeArrayElement at the specified position in this list.
     * @see RangeArray#RangeArray(int, neembuu.common.RangeArrayElementFactory)
     * @throws ArrayIndexOutOfBoundsException when array index is negative or greater than size
     */
    public C get(int index) {
        if (index < 0 || index >= size()) throw new ArrayIndexOutOfBoundsException(index);
        return store.get(index + 1);
    }

    /**
     * Get's the size of file this RangeArray is being used to represent.
     * This value is the maximum allowed accessible absoluteindex (for absoulte index see {@link #get(long) } )
     * of this RangeArray.
     * </p>
     * This operation is used for files generally, but may be used to limit the capacity
     * of a RangeArray. Default value is {@link RangeArray#MAX_VALUE_SUPPORTED }
     * </p>
     * Negative values are allowed since the domain of range array exists on either side of zero.
     * @return file size limit
     */
    public final long getFileSize() {
        return fileSize;
    }

    /**
     * Set the file size value
     * @param fileSize The size of this file at the moment ( this value can be changed anytime )
     * @throws ArrayIndexOutOfBoundsException If the new value is lesser than the biggest value already present in this
     */
    public void setFileSize(long fileSize) throws ArrayIndexOutOfBoundsException {
        if (store.get(store.size() - 2).ending() > fileSize) throw new ArrayIndexOutOfBoundsException("New file size is lesser than already present values. Current file size=" + this.fileSize + " attempting to set file size =" + fileSize);
        this.fileSize = fileSize;
        if (this instanceof ListenableRangeArray) {
            ListenableRangeArray self = (ListenableRangeArray) this;
            self.announceToListeners(new RangeArrayElement(0, fileSize));
        }
    }

    @Override
    public final int size() {
        return (length() - 2);
    }

    public long absoluteSize() {
        long ret;
        ret = store.get(store.size() - 1).ending() + 1;
        if (store.get(1).starting() < 0) {
            ret += store.get(1).starting();
        }
        return ret;
    }

    @Override
    public final boolean isEmpty() {
        return length() <= 2;
    }

    @Override
    public final void clear() {
        store.removeRange(1, length() - 1);
    }

    @Override
    public final boolean add(C newEntry) {
        if (contains(newEntry)) return false;
        addElement(newEntry);
        return true;
    }

    @Override
    public final boolean addAll(Collection<? extends C> collection) {
        boolean ret = false;
        for (C entry : collection) {
            if (add(entry)) ret = true;
        }
        return ret;
    }

    /**
     * Merges this RangeArray with another<br/>.
     * Uses System.arraycopy if possible (hence faster), otherwise uses addAll.
     * @see #addAll(java.util.Collection) 
     * @param mergeWith
     */
    public final void merge(RangeArray<C> mergeWith) {
        if (mergeWith.size() == 0) return;
        boolean simplyArrayCopy = false;
        if (this.size() == 0) simplyArrayCopy = true;
        int startIndex = this.getIndexOf(mergeWith.get(0).starting());
        int lastIndex = this.getIndexOf(mergeWith.getLastElement().ending());
        if (startIndex == lastIndex && lastIndex < 0) simplyArrayCopy = true;
        if (simplyArrayCopy) {
            if (startIndex == Integer.MIN_VALUE) {
                startIndex = this.store.elementCount;
            } else startIndex *= -1;
            synchronized (modLock) {
                store.ensureCapacity(store.size() + mergeWith.size());
                if (store.size() - startIndex - 1 > 0) System.arraycopy(store.elementData, startIndex + 1, store.elementData, startIndex + 1 + mergeWith.size(), store.size() - startIndex - 1);
                System.arraycopy(mergeWith.store.elementData, 1, store.elementData, startIndex + 1, mergeWith.size());
                this.store.elementCount += mergeWith.size();
            }
        } else {
            addAll(mergeWith);
        }
    }

    @Override
    public final boolean contains(Object o) {
        if (!(o instanceof RangeArrayElement)) return false;
        C entryToCheck = (C) o;
        int startIndex = indexOf(entryToCheck.starting());
        int endIndex = indexOf(entryToCheck.ending());
        return ((startIndex >= 0 && endIndex >= 0) && (startIndex < this.size() * 2 - 1 && endIndex < this.size() * 2 - 1) && (startIndex % 2 == 0 && endIndex % 2 == 0));
    }

    public final int containsAt(Object o) {
        C newEntry = (C) o;
        int[] ind = indexOf(newEntry);
        if (ind[0] % 2 == 0) {
            if (store.get(ind[0] / 2).contains(newEntry)) return (ind[0] / 2 - 1);
        }
        return -1;
    }

    public final boolean containsPartially(C toCheck) {
        int[] ind = indexOf(toCheck);
        if (ind[0] % 2 == 0) return true; else {
            if (ind[0] % 2 == 0) return true;
            if (ind[0] > ind[1]) return true;
        }
        return false;
    }

    @Override
    public final boolean containsAll(Collection<?> collection) {
        for (int j = 1; j < store.size() - 1; j++) {
            C entry = (C) store.get(j);
            if (!contains(entry)) return false;
        }
        return true;
    }

    @Override
    public final Iterator<C> iterator() {
        class RA_Iterator implements Iterator<C> {

            int index;

            int expectedModCount = modCount;

            public RA_Iterator() {
                index = 1;
            }

            @Override
            public boolean hasNext() {
                return (index < store.size() - 1);
            }

            @Override
            public C next() {
                checkForComodification();
                if (!hasNext()) throw new NoSuchElementException();
                C ret = store.get(index);
                index++;
                return ret;
            }

            @Override
            public void remove() {
                store.remove(index);
            }

            final void checkForComodification() {
                if (modCount != expectedModCount) throw new ConcurrentModificationException();
            }
        }
        return new RA_Iterator();
    }

    /**
     * Returns an iterator which has it 's first and last element
     * shifted (using RangeArrayElement#getShiftedTo(long)) such that 
     * this can be used to iterate exactly over the bounds.
     * This can be used for reading, such as in PartialFileFS.
     * The region for which iterator has to be provided is <b>copied</b> in
     * a thread safe manner, and then an iterator is constructed around it.
     * Changes at individual elements are reflected in the copies,
     * but changes such as removal of an element from this rangearray cannot be observed
     * in the copy.
     * @see RangeArrayElement#getShiftedTo(long) 
     * @param bounds The region for which the iterator is required
     * @throws UnsupportedOperationException It is possible that the implementing class does not chose to properly implement this method
     */
    public final Iterator<C> iteratorOver(final RangeArrayElement bounds) {
        int[] x = getIndexPair(bounds);
        if (x == null) return null;
        int lastElementIndex = x[1] + 1, firstElementIndex = x[0] + 1;
        RangeArrayElement[] regionData = new RangeArrayElement[lastElementIndex - firstElementIndex + 1];
        System.arraycopy(this.store.elementData, firstElementIndex, regionData, 0, regionData.length);
        if (regionData[0].starting() < bounds.starting()) {
            regionData[0] = regionData[0];
        }
        if (regionData[regionData.length - 1].ending() < bounds.ending()) {
            regionData[regionData.length - 1] = regionData[regionData.length - 1];
        }
        return new ShiftedIterator<C>(regionData);
    }

    public final int[] getIndexPair(long boundsStart, long boundsEnd) {
        return getIndexPair(new RangeArrayElement(boundsStart, boundsEnd));
    }

    /**
     * For a system as shown below,
     * and <code>bounds = new RangeArrayElement(x1,x2) </code>
     * <pre>
     * Index                     0               1               2               3               4               5
     * Offset value          A------>B       C--x1-->D       E------>F       G------>H       I---x2->J       K------>L
     * </pre>
     * Where x1 lies between C and D, and similarly x2 lies between I and J,
     * returns <code>new int[]{1,4} </code>.
     * <br/>
     * Pay attention to the fact that both indices are inclusive.
     * So in  for loop for this would be something like :
     * <pre>
     * int[]indices=rangeArray.getIndexPair(bounds);
     * for(int j=indices[0]; j&#60;=indices[1] ; j++){
     *      RangeArrayElement nextRAE = rangeArray.get(j);
     * }
     * </pre>
     * <br/><br/>
     * <u>Special case</u>  :
     * When the element who's index pair is being searched lies prior to all existing entries: <br/>
     * <small>(If the request lies beyond all existing entries, the second value in the
     * index pair array is equal to length() -1 to avoid array index out of bounds exception.
     * This is for convenience at the cost of loss of symmetry in api )</small>
     * <pre>
     * Index                        0                     1
     * Offset value          1000------>1200      5000---->6000
     * </pre>
     * And index pair of <code>new RangeArrayElement(500,600)</code> is required,
     * then the array would be 
     * a negative value will be returned by as if there was a fake entry before zero at minus one.
     * <pre>
     * Index                       -1                      0                    1
     * Offset value   -infinity----> -infinity       1000------>1200      5000---->6000
     * </pre>
     * That is returned value would be </code>new int[]{-1,-1}<code> for <ul>
     * <li>Empty array <</li>>
     * <li><code>new RangeArrayElement(500,600)</code> region prior and not touching or overlapping</li>
     * </ul>
     * The  returned value would be </code>new int[]{-1,0}<code> for <ul>
     * <li><code>new RangeArrayElement(500,1100)</code> region starting before ending inside</li>
     * <li><code>new RangeArrayElement(500,1600)</code> region starting before extending beyond</li>
     * </ul>
     * The returned value would be </code>new int[]{0,0}<code> for <ul>
     * <li><code>new RangeArrayElement(1100,1600)</code> region inside and extending </li>
     * <li><code>new RangeArrayElement(1100,1110)</code> region inside</li>
     * <li><code>new RangeArrayElement(1600,2000)</code> region outside</li>
     * </ul>
     * <br/><br/>
     * 
     * <small> <u>Not important : </u>
     * {@link #getIndexPair(neembuu.common.RangeArrayElement) }
     * is different from {@link #indexOf(neembuu.common.RangeArrayElement) }
     * The latter does not use exact index of logic, it is
     * designed specially for insertion algorithms. It has special boundary
     * conditions which do not match the boundary conditions and constraints
     * of {@link #getIndexPair(neembuu.common.RangeArrayElement) }</small>
     * @param bounds
     * @return the indices of elements closest to the requested bounds.
     */
    public final int[] getIndexPair(RangeArrayElement bounds) {
        if (this.isEmpty()) return new int[] { -1, -1 };
        synchronized (modLock) {
            if (size() == 1) {
                if (bounds.starting() < this.get(0).starting()) {
                    if (bounds.ending() < this.get(0).starting()) {
                        return new int[] { -1, -1 };
                    }
                    return new int[] { -1, 0 };
                }
                return new int[] { 0, 0 };
            }
            int firstElementIndex = this.getIndexOf(bounds.starting());
            if (firstElementIndex <= 1) {
            } else if (firstElementIndex > length() * 2 - 2 - 1) {
                return null;
            }
            if (firstElementIndex % 2 == -1) {
                firstElementIndex = -2;
            } else if (firstElementIndex % 2 == 1) {
                firstElementIndex--;
            }
            firstElementIndex /= 2;
            int lastElementIndex = this.getIndexOf(bounds.ending());
            if (lastElementIndex <= 1) {
                lastElementIndex = 2;
            } else if (lastElementIndex >= length() * 2 - 2 - 1) {
                lastElementIndex = length() * 2 - 4;
            }
            if (lastElementIndex % 2 == 1) {
            }
            lastElementIndex /= 2;
            return new int[] { firstElementIndex - 1, lastElementIndex - 1 };
        }
    }

    static final class ShiftedIterator<C extends RangeArrayElement> implements Iterator<C> {

        private final RangeArrayElement[] regionData;

        private int index = 0;

        public ShiftedIterator(RangeArrayElement[] regionData) {
            this.regionData = regionData;
        }

        @Override
        public boolean hasNext() {
            return index < regionData.length;
        }

        @Override
        public C next() {
            return (C) regionData[index++];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported as this is a copy.");
        }
    }

    public final Iterator<C> absoluteIterator() {
        return new AbsoluteIterator<C>(this);
    }

    protected static final class AbsoluteIterator<C extends RangeArrayElement> implements Iterator<C> {

        long absoluteindex;

        RangeArray<C> rangeArray;

        public AbsoluteIterator(RangeArray<C> rangeArray) {
            absoluteindex = rangeArray.store.get(1).starting();
            this.rangeArray = rangeArray;
        }

        @Override
        public boolean hasNext() {
            return (absoluteindex >= rangeArray.store.get(1).starting() && absoluteindex <= rangeArray.store.get(rangeArray.store.size() - 2).ending());
        }

        @Override
        public final C next() {
            checkForComodification();
            if (!hasNext()) throw new NoSuchElementException();
            C ret = rangeArray.get(absoluteindex);
            absoluteindex++;
            return ret;
        }

        public final long getPosition() {
            return absoluteindex - 1;
        }

        @Override
        public final void remove() {
            rangeArray.removeElement(absoluteindex - 1, absoluteindex - 1);
        }

        final void checkForComodification() {
        }
    }

    @Override
    public final boolean remove(Object o) {
        C toRemove = (C) o;
        boolean ret = containsPartially(toRemove);
        removeElement(toRemove.starting(), toRemove.ending());
        return ret;
    }

    @Override
    public final boolean removeAll(Collection<?> c) {
        Iterator<?> i = c.iterator();
        boolean ret = false;
        while (i.hasNext()) {
            if (remove(i.next())) ret = true;
        }
        return ret;
    }

    @Override
    public final boolean retainAll(Collection<?> c) {
        Iterator i = c.iterator();
        boolean ret = false;
        RangeArray<C> newArray = new RangeArray<C>(4);
        if (this.factory != null) newArray.factory = this.factory;
        if (this.elementClass != null) newArray.elementClass = this.elementClass;
        while (i.hasNext()) {
            C toRetain = (C) i.next();
            int loc = containsAt(toRetain);
            if (loc > 0 && loc < store.size() - 1) {
                C elementWithProperties = store.get(loc);
                newArray.addElement(elementWithProperties);
            }
        }
        synchronized (modLock) {
            if (true) throw new UnsupportedOperationException("unsupported for sake of immutable unsyc array reference");
        }
        return ret;
    }

    @Override
    public Object[] toArray() {
        Object[] a = new Object[this.size()];
        System.arraycopy(this.store.elementData, 1, a, 0, this.size());
        return a;
    }

    /**
     * Use this is painting graphical components and other things,
     * so that unnecessary locking on the rangearray is not required.
     */
    public final UnsynchronizedAccess getUnSynchronizedArrayAccess() {
        return this.store;
    }

    public static interface UnsynchronizedAccess {

        /**
         * Use this is painting graphical components and other things,
         * so that unnecessary locking on the rangearray is not required.
         * @return 
         * @throws NullPointerException if and only if arraycopy with lock fails and 
         * array passed is null
         */
        public RangeArrayElement[] tryToGetUnsynchronizedCopy(RangeArray array);

        /**
         * For mouse event listener
         */
        public RangeArrayElement getUnsynchronized(long index, RangeArray array);
    }

    @Override
    public <T> T[] toArray(T[] a) {
        synchronized (modLock) {
            if (a.length < this.size()) a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), this.size());
            System.arraycopy(this.store.elementData, 1, a, 0, this.size());
            if (a.length > this.size()) a[this.size()] = null;
            return a;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RangeArray)) return false;
        RangeArray toCompare = (RangeArray) obj;
        return notEqualsGetReason(toCompare) == null;
    }

    public String notEqualsGetReason(RangeArray toCompare) {
        if (toCompare.size() != this.size()) {
            return "size not equal";
        }
        for (int i = 0; i < this.size(); i++) {
            if (!this.get(i).equals(toCompare.get(i))) {
                return "failed at=" + toCompare.get(i) + " " + this.get(i);
            }
        }
        return null;
    }

    @Override
    public final boolean canBeAnElementOfThis(C element) {
        if (this instanceof FilteredRangeArray) {
            return ((FilteredRangeArray) this).canBeAnElementOfThisFilter(element);
        }
        if (fileSize == MAX_VALUE_SUPPORTED) return true;
        return true;
    }

    /**
     * We can also perform parallel summing ... but that is a java7
     * thing
     * @param bounds null means sum over entire region
     * @return
     */
    public long getElementsSum(RangeArrayElement bounds) {
        synchronized (modLock) {
            int lower_index, higher_index;
            if (bounds == null) {
                lower_index = 0;
                higher_index = this.size() - 1;
            } else {
                lower_index = this.indexOf(bounds.starting());
                if (lower_index < 0) {
                    return 0;
                }
                if (lower_index >= this.size() * 2 - 1) {
                    return 0;
                }
                if (lower_index % 2 != 0) {
                    lower_index++;
                }
                lower_index /= 2;
                higher_index = this.indexOf(bounds.ending());
                if (higher_index < 0) return 0;
                if (higher_index >= this.size() * 2 - 1) {
                    higher_index = this.size() * 2 - 2;
                }
                if (higher_index % 2 != 0) {
                    higher_index--;
                }
                higher_index /= 2;
            }
            long sum = 0;
            for (int i = lower_index; i <= higher_index; i++) {
                sum += this.get(i).getSize();
            }
            return sum;
        }
    }

    public static enum OPERATION {

        CS1, CS2, CS3, CS4, CS5, CS6, CS7, CS8, CS9
    }

    public void add(long offset, int size) {
        C entry = makeNewInstance(offset, offset + size - 1);
        add(entry);
    }

    /**
     *
     * @param toFindIndexOfEntry The entry whoes index is to be found
     * @return -1 is not found, else the index of the entry
     */
    public int getIndexOf(C toFindIndexOfEntry) {
        int index = -1;
        int lowerBound = 0, upperBound = size();
        do {
            int mid = (lowerBound + upperBound) / 2;
            int compareValue = get(mid).compareTo(toFindIndexOfEntry);
            if (compareValue == 0) {
                index = mid;
                break;
            }
            if (compareValue < 0) {
                lowerBound = mid + 1;
            } else {
                upperBound = mid - 1;
            }
        } while (index == -1 && upperBound >= lowerBound);
        return index;
    }

    public long totalBytesInEntries() {
        long total = 0;
        for (RangeArrayElement ele : this) {
            total += ele.getSize();
        }
        return total;
    }

    /**
     * Reduces the size of the array (if an array is used) used to store elements.
     * See {@link Vector#trimToSize() }
     */
    public void trimToSize() {
        store.trimToSize();
    }

    public C getLastElement() {
        return store.get(store.size() - 2);
    }

    private final void modifiedAround(long s, long e) {
        if (this instanceof ListenableRangeArray) {
            ((ListenableRangeArray) this).announceToListeners(new RangeArrayElement(s, e));
        }
    }

    public static final class Builder<C extends RangeArrayElement> {

        private final LinkedList<C> elements = new LinkedList<C>();

        public Builder() {
        }

        public Builder add(long start, long end) {
            elements.add((C) (new RangeArrayElement(start, end)));
            return this;
        }

        public Builder add(C element) {
            elements.add(element);
            return this;
        }

        public RangeArray<C> build() {
            RangeArray<C> toret = new RangeArray<C>();
            for (C ele : elements) {
                toret.add(ele);
            }
            return toret;
        }
    }
}
