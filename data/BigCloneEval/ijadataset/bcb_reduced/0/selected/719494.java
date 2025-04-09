package net.jadoth.collections;

import static java.lang.Math.min;
import static net.jadoth.lang.reflection.JaReflect.componentType;
import java.lang.reflect.Array;
import java.util.Collection;
import net.jadoth.Jadoth;
import net.jadoth.collections.interfaces.Collector;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.lang.Equalator;
import net.jadoth.lang.functional.Predicate;
import net.jadoth.math.FastRandom;

/**
 * @author Thomas M�nz
 *
 */
public abstract class JaArrays {

    private static String exceptionRange(final int size, final int startIndex, final int length) {
        return "Range [" + (length < 0 ? startIndex + length + 1 + ";" + startIndex : startIndex + ";" + (startIndex + length - 1)) + "] not in [0;" + (size - 1) + "]";
    }

    private static String exceptionIndexOutOfBounds(final int size, final int index) {
        return "Index: " + index + ", Size: " + size;
    }

    public static final int validIndex(final int index, final Object[] array) throws ArrayIndexOutOfBoundsException {
        if (index < 0 || array != null && index >= array.length) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return index;
    }

    public static final int validateArrayRange(final Object[] array, final int offset, final int length) {
        if (length >= 0) {
            if (offset < 0 || offset + length > array.length) {
                throw new IndexOutOfBoundsException(exceptionRange(array.length, offset, length));
            }
            if (length == 0) {
                return 0;
            }
            return +1;
        } else if (length < 0) {
            if (offset + length < -1 || offset >= array.length) {
                throw new IndexOutOfBoundsException(exceptionRange(array.length, offset, length));
            }
            return -1;
        } else if (offset < 0 || offset >= array.length) {
            throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(array.length, offset));
        } else {
            return 0;
        }
    }

    public static final void checkBounds(final Object[] array, final int start, final int bound) {
        if (bound < 0 || bound > array.length) {
            throw new ArrayIndexOutOfBoundsException(bound - 1);
        } else if (start < 0 || start >= bound) {
            throw new ArrayIndexOutOfBoundsException(start);
        }
    }

    public static boolean hasNoContent(final Object[] array) {
        return array == null || array.length == 0;
    }

    public static final <T> T[] fill(final T[] array, final T fillElement, int fromIndex, final int toIndex) {
        if (fromIndex < 0 || fromIndex >= array.length) throw new ArrayIndexOutOfBoundsException(fromIndex);
        if (toIndex < 0 || toIndex >= array.length) throw new ArrayIndexOutOfBoundsException(toIndex);
        if (fromIndex < toIndex) {
            while (fromIndex <= toIndex) {
                array[fromIndex++] = fillElement;
            }
        } else {
            while (fromIndex >= toIndex) {
                array[fromIndex--] = fillElement;
            }
        }
        return array;
    }

    public static final int[] fill(final int[] array, final int fillElement) {
        final int length = array.length;
        for (int i = 0; i < length; i++) {
            array[i] = fillElement;
        }
        return array;
    }

    public static final byte[] fill(final byte[] array, final byte fillElement) {
        final int length = array.length;
        for (int i = 0; i < length; i++) {
            array[i] = fillElement;
        }
        return array;
    }

    public static final int[] fill(final int[] array, final int fillElement, int fromIndex, final int toIndex) {
        if (fromIndex < 0 || fromIndex >= array.length) throw new ArrayIndexOutOfBoundsException(fromIndex);
        if (toIndex < 0 || toIndex >= array.length) throw new ArrayIndexOutOfBoundsException(toIndex);
        if (fromIndex < toIndex) {
            while (fromIndex <= toIndex) {
                array[fromIndex++] = fillElement;
            }
        } else {
            while (fromIndex >= toIndex) {
                array[fromIndex--] = fillElement;
            }
        }
        return array;
    }

    public static final <T> T[] clear(final T[] array) {
        final int length = array.length;
        for (int i = 0; i < length; i++) {
            array[i] = null;
        }
        return array;
    }

    @SuppressWarnings("unchecked")
    public static final <T> T[] subArray(final T[] array, final int offset, final int length) {
        final T[] newArray;
        System.arraycopy(array, offset, newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), length), 0, length);
        return newArray;
    }

    public static final boolean equals(final Object[] array1, final int startIndex1, final Object[] array2, final int startIndex2, final int length, final Equalator<Object> comparator) {
        int a = startIndex1, b = startIndex2;
        for (final int aBound = startIndex1 + length; a < aBound; a++, b++) {
            if (comparator.equal(array1[a], array2[b])) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public static final <T> T[] add(final T[] a1, final T... a2) {
        if (a1 == null) return a2 == null ? null : a2.clone();
        if (a2 == null) return a1.clone();
        final T[] a = (T[]) Array.newInstance(a1.getClass().getComponentType(), a1.length + a2.length);
        System.arraycopy(a1, 0, a, 0, a1.length);
        System.arraycopy(a2, 0, a, a1.length, a2.length);
        return a;
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static final <T> T[] combine(final T[]... arrays) {
        if (arrays == null || arrays.length == 0) return null;
        return combine((Class<T>) arrays[0].getClass().getComponentType(), arrays);
    }

    @SuppressWarnings("unchecked")
    public static final <T, S extends T> T[] combine(final Class<T> componentType, final S[]... arrays) {
        if (arrays == null || arrays.length == 0) return null;
        if (arrays.length == 1) return arrays[0].clone();
        long totalLength = 0;
        for (final S[] array : arrays) {
            totalLength += array.length;
        }
        if (totalLength > Integer.MAX_VALUE) {
            throw new ArrayIndexOutOfBoundsException(Long.toString(totalLength));
        }
        final T[] combined = (T[]) Array.newInstance(componentType, (int) totalLength);
        for (int c = 0, i = 0; c < arrays.length; c++) {
            System.arraycopy(arrays[c], 0, combined, i, arrays[c].length);
            i += arrays[c].length;
        }
        return combined;
    }

    public static final int[] _intAdd(final int[] a1, final int... a2) {
        if (a1 == null) return a2 == null ? null : a2.clone();
        if (a2 == null) return a1.clone();
        final int[] a = new int[a1.length + a2.length];
        System.arraycopy(a1, 0, a, 0, a1.length);
        System.arraycopy(a2, 0, a, a1.length, a2.length);
        return a;
    }

    /**
	 * Merges the both passed arrays by taking all elements from <code>a1</code> (even duplicates) and adds all
	 * elements of <code>a2</code> (also duplicates as well) that are not already contained in <code>a1</code>.
	 *
	 * @param <T>
	 * @param a1
	 * @param a2
	 * @return
	 */
    @SuppressWarnings("unchecked")
    public static final <T> T[] merge(final T[] a1, final T... a2) {
        if (a1 == null) return a2 == null ? null : a2.clone();
        if (a2 == null) return a1.clone();
        final int a1Len = a1.length;
        final BulkList<T> buffer = new BulkList<T>(a1);
        a2: for (final T e : a2) {
            for (int i = 0; i < a1Len; i++) {
                if (e == a1[i]) continue a2;
            }
            buffer.add(e);
        }
        return buffer.toArray((Class<T>) a1.getClass().getComponentType());
    }

    /**
	 * This method checks if <code>array</code> contains <code>element</code> by object identity
	 *
	 * @param <E> any type
	 * @param array the array to be searched in
	 * @param element the element to be searched (by identity)
	 * @return {@code true} if <code>array</code> contains <code>element</code> by object identity, else <tt>false</tt>
	 */
    public static final <E> boolean contains(final E[] array, final E element) {
        for (final E e : array) {
            if (e == element) return true;
        }
        return false;
    }

    public static final <E> boolean eqContains(final E[] array, final E element) {
        if (element == null) {
            for (final E e : array) {
                if (e == null) return true;
            }
        } else {
            for (final E e : array) {
                if (element.equals(e)) return true;
            }
        }
        return false;
    }

    public static final <T, S extends T> boolean contains(final T[] array, final S element, final Equalator<? super T> cmp) {
        for (final T t : array) {
            if (cmp.equal(element, t)) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static final <E> boolean containsId(final Collection<E> c, final E element) {
        if (c instanceof XGettingCollection<?>) {
            return ((XGettingCollection<E>) c).containsId(element);
        }
        for (final E t : c) {
            if (t == element) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static final <E> boolean containS(final Collection<E> c, final E element) {
        if (c instanceof XGettingCollection<?>) {
            return ((XGettingCollection<E>) c).contains(element);
        }
        return c.contains(element);
    }

    @SuppressWarnings("unchecked")
    public static final <E> boolean contains(final Collection<? super E> c, final E sample, final Equalator<? super E> equalator) {
        if (c instanceof XGettingCollection<?>) {
            return ((XGettingCollection<E>) c).contains(Jadoth.predicate(sample, equalator));
        }
        for (final Object t : c) {
            if (equalator.equal((E) t, sample)) return true;
        }
        return false;
    }

    /**
	 * Removed all occurances of <code>elementToRemove</code> from array <code>src</code>.<br>
	 * If <code>dest</code> is the same as <code>src</code>, the retained elements will be compressed in place.
	 * Otherwise all retained elements are copied to dest.<br>
	 * As the number of to be removed objects is not known beforehand, <code>dest</code> has to have enough length to
	 * receive all retained elements, otherwise an {@link ArrayIndexOutOfBoundsException} will be thrown.
	 * <p>
	 * Note that removing {@code null} will still set the trailing array positions to {@code null}.<br>
	 *
	 * @param elementToRemove
	 * @param src the source array containing all elements.
	 * @param srcStart
	 * @param srcBound
	 * @param dest the destination array to receive the retained objects. Can be <code>src</code> again.
	 * @param destStart
	 * @param destBound
	 * @return the number of removed elements
	 */
    public static <E> int removeAllFromArray(final E elementToRemove, final E[] src, final int srcStart, final int srcBound, final E[] dest, final int destStart, final int destBound) throws ArrayIndexOutOfBoundsException {
        int currentMoveTargetIndex;
        if (src == dest) {
            currentMoveTargetIndex = srcStart;
            while (currentMoveTargetIndex < destBound && src[currentMoveTargetIndex] != elementToRemove) {
                currentMoveTargetIndex++;
            }
        } else {
            currentMoveTargetIndex = destStart;
        }
        int currentMoveSourceIndex = 0;
        int currentMoveLength = 0;
        int seekIndex = currentMoveTargetIndex;
        while (seekIndex < srcBound) {
            while (seekIndex < srcBound && src[seekIndex] == elementToRemove) {
                seekIndex++;
            }
            currentMoveSourceIndex = seekIndex;
            while (seekIndex < srcBound && src[seekIndex] != elementToRemove) {
                seekIndex++;
            }
            currentMoveLength = seekIndex - currentMoveSourceIndex;
            switch(currentMoveLength) {
                case 4:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                    dest[currentMoveTargetIndex + 1] = src[currentMoveSourceIndex + 1];
                    dest[currentMoveTargetIndex + 2] = src[currentMoveSourceIndex + 2];
                    dest[currentMoveTargetIndex + 3] = src[currentMoveSourceIndex + 3];
                    break;
                case 3:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                    dest[currentMoveTargetIndex + 1] = src[currentMoveSourceIndex + 1];
                    dest[currentMoveTargetIndex + 2] = src[currentMoveSourceIndex + 2];
                    break;
                case 2:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                    dest[currentMoveTargetIndex + 1] = src[currentMoveSourceIndex + 1];
                    break;
                case 1:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                case 0:
                    break;
                default:
                    System.arraycopy(src, currentMoveSourceIndex, dest, currentMoveTargetIndex, currentMoveLength);
            }
            currentMoveTargetIndex += currentMoveLength;
        }
        if (src == dest) {
            for (int i = currentMoveTargetIndex; i < srcBound; i++) {
                dest[i] = null;
            }
        }
        return srcBound - currentMoveTargetIndex;
    }

    public static int removeAllFromArray(final int elementToRemove, final int[] src, final int srcStart, final int srcBound, final int[] dest, final int destStart, final int destBound) throws ArrayIndexOutOfBoundsException {
        int currentMoveTargetIndex;
        if (src == dest) {
            currentMoveTargetIndex = srcStart;
            while (currentMoveTargetIndex < destBound && src[currentMoveTargetIndex] != elementToRemove) {
                currentMoveTargetIndex++;
            }
        } else {
            currentMoveTargetIndex = destStart;
        }
        int currentMoveSourceIndex = 0;
        int currentMoveLength = 0;
        int seekIndex = currentMoveTargetIndex;
        while (seekIndex < srcBound) {
            while (seekIndex < srcBound && src[seekIndex] == elementToRemove) {
                seekIndex++;
            }
            currentMoveSourceIndex = seekIndex;
            while (seekIndex < srcBound && src[seekIndex] != elementToRemove) {
                seekIndex++;
            }
            currentMoveLength = seekIndex - currentMoveSourceIndex;
            switch(currentMoveLength) {
                case 4:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                    dest[currentMoveTargetIndex + 1] = src[currentMoveSourceIndex + 1];
                    dest[currentMoveTargetIndex + 2] = src[currentMoveSourceIndex + 2];
                    dest[currentMoveTargetIndex + 3] = src[currentMoveSourceIndex + 3];
                    break;
                case 3:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                    dest[currentMoveTargetIndex + 1] = src[currentMoveSourceIndex + 1];
                    dest[currentMoveTargetIndex + 2] = src[currentMoveSourceIndex + 2];
                    break;
                case 2:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                    dest[currentMoveTargetIndex + 1] = src[currentMoveSourceIndex + 1];
                    break;
                case 1:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                case 0:
                    break;
                default:
                    System.arraycopy(src, currentMoveSourceIndex, dest, currentMoveTargetIndex, currentMoveLength);
            }
            currentMoveTargetIndex += currentMoveLength;
        }
        if (src == dest) {
            for (int i = currentMoveTargetIndex; i < srcBound; i++) {
                dest[i] = Integer.MIN_VALUE;
            }
        }
        return srcBound - currentMoveTargetIndex;
    }

    public static <E> int removeAllFromArray(final Collection<? extends E> elementsToRemove, final E[] src, final int srcStart, final int srcBound, final E[] dest, final int destStart, final int destBound, final boolean ignoreNulls) throws ArrayIndexOutOfBoundsException {
        int currentMoveTargetIndex;
        Object e = null;
        if (src == dest) {
            currentMoveTargetIndex = srcStart;
            while (currentMoveTargetIndex < destBound && (e = src[currentMoveTargetIndex]) == null && ignoreNulls || !elementsToRemove.contains(e)) {
                currentMoveTargetIndex++;
            }
        } else {
            currentMoveTargetIndex = destStart;
        }
        int currentMoveSourceIndex = 0;
        int currentMoveLength = 0;
        int seekIndex = currentMoveTargetIndex;
        while (seekIndex < srcBound) {
            while (seekIndex < srcBound && ((e = src[seekIndex]) != null || !ignoreNulls) && elementsToRemove.contains(e)) {
                seekIndex++;
            }
            currentMoveSourceIndex = seekIndex;
            while (seekIndex < srcBound && ((e = src[seekIndex]) == null && ignoreNulls || !elementsToRemove.contains(e))) {
                seekIndex++;
            }
            currentMoveLength = seekIndex - currentMoveSourceIndex;
            switch(currentMoveLength) {
                case 4:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                    dest[currentMoveTargetIndex + 1] = src[currentMoveSourceIndex + 1];
                    dest[currentMoveTargetIndex + 2] = src[currentMoveSourceIndex + 2];
                    dest[currentMoveTargetIndex + 3] = src[currentMoveSourceIndex + 3];
                    break;
                case 3:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                    dest[currentMoveTargetIndex + 1] = src[currentMoveSourceIndex + 1];
                    dest[currentMoveTargetIndex + 2] = src[currentMoveSourceIndex + 2];
                    break;
                case 2:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                    dest[currentMoveTargetIndex + 1] = src[currentMoveSourceIndex + 1];
                    break;
                case 1:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                case 0:
                    break;
                default:
                    System.arraycopy(src, currentMoveSourceIndex, dest, currentMoveTargetIndex, currentMoveLength);
            }
            currentMoveTargetIndex += currentMoveLength;
        }
        if (src == dest) {
            for (int i = currentMoveTargetIndex; i < srcBound; i++) {
                dest[i] = null;
            }
        }
        return srcBound - currentMoveTargetIndex;
    }

    public static <E> int removeAllFromArray(final XGettingCollection<E> elementsToRemove, final E[] src, final int srcStart, final int srcBound, final E[] dest, final int destStart, final int destBound, final boolean ignoreNulls) throws ArrayIndexOutOfBoundsException {
        int currentMoveTargetIndex;
        E e = null;
        if (src == dest) {
            currentMoveTargetIndex = srcStart;
            while (currentMoveTargetIndex < destBound && (e = src[currentMoveTargetIndex]) == null && ignoreNulls || elementsToRemove.containsId(e)) {
                currentMoveTargetIndex++;
            }
        } else {
            currentMoveTargetIndex = destStart;
        }
        int currentMoveSourceIndex = 0;
        int currentMoveLength = 0;
        int seekIndex = currentMoveTargetIndex;
        while (seekIndex < srcBound) {
            while (seekIndex < srcBound && ((e = src[seekIndex]) != null || !ignoreNulls) && elementsToRemove.containsId(e)) {
                seekIndex++;
            }
            currentMoveSourceIndex = seekIndex;
            while (seekIndex < srcBound && ((e = src[seekIndex]) == null && ignoreNulls || !elementsToRemove.containsId(e))) {
                seekIndex++;
            }
            currentMoveLength = seekIndex - currentMoveSourceIndex;
            switch(currentMoveLength) {
                case 4:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                    dest[currentMoveTargetIndex + 1] = src[currentMoveSourceIndex + 1];
                    dest[currentMoveTargetIndex + 2] = src[currentMoveSourceIndex + 2];
                    dest[currentMoveTargetIndex + 3] = src[currentMoveSourceIndex + 3];
                    break;
                case 3:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                    dest[currentMoveTargetIndex + 1] = src[currentMoveSourceIndex + 1];
                    dest[currentMoveTargetIndex + 2] = src[currentMoveSourceIndex + 2];
                    break;
                case 2:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                    dest[currentMoveTargetIndex + 1] = src[currentMoveSourceIndex + 1];
                    break;
                case 1:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                case 0:
                    break;
                default:
                    System.arraycopy(src, currentMoveSourceIndex, dest, currentMoveTargetIndex, currentMoveLength);
            }
            currentMoveTargetIndex += currentMoveLength;
        }
        if (src == dest) {
            for (int i = currentMoveTargetIndex; i < srcBound; i++) {
                dest[i] = null;
            }
        }
        return srcBound - currentMoveTargetIndex;
    }

    public static <E> int removeAllFromArray(final E[] elementsToRemove, final E[] src, final int srcStart, final int srcBound, final E[] dest, final int destStart, final int destBound, final boolean ignoreNulls) throws ArrayIndexOutOfBoundsException {
        int currentMoveTargetIndex;
        E e = null;
        if (src == dest) {
            currentMoveTargetIndex = srcStart;
            while (currentMoveTargetIndex < destBound && (e = src[currentMoveTargetIndex]) == null && ignoreNulls || !contains(elementsToRemove, e)) {
                currentMoveTargetIndex++;
            }
        } else {
            currentMoveTargetIndex = destStart;
        }
        int currentMoveSourceIndex = 0;
        int currentMoveLength = 0;
        int seekIndex = currentMoveTargetIndex;
        while (seekIndex < srcBound) {
            while (seekIndex < srcBound && ((e = src[seekIndex]) != null || !ignoreNulls) && contains(elementsToRemove, e)) {
                seekIndex++;
            }
            currentMoveSourceIndex = seekIndex;
            while (seekIndex < srcBound && ((e = src[seekIndex]) == null && ignoreNulls || !contains(elementsToRemove, e))) {
                seekIndex++;
            }
            currentMoveLength = seekIndex - currentMoveSourceIndex;
            switch(currentMoveLength) {
                case 4:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                    dest[currentMoveTargetIndex + 1] = src[currentMoveSourceIndex + 1];
                    dest[currentMoveTargetIndex + 2] = src[currentMoveSourceIndex + 2];
                    dest[currentMoveTargetIndex + 3] = src[currentMoveSourceIndex + 3];
                    break;
                case 3:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                    dest[currentMoveTargetIndex + 1] = src[currentMoveSourceIndex + 1];
                    dest[currentMoveTargetIndex + 2] = src[currentMoveSourceIndex + 2];
                    break;
                case 2:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                    dest[currentMoveTargetIndex + 1] = src[currentMoveSourceIndex + 1];
                    break;
                case 1:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                case 0:
                    break;
                default:
                    System.arraycopy(src, currentMoveSourceIndex, dest, currentMoveTargetIndex, currentMoveLength);
            }
            currentMoveTargetIndex += currentMoveLength;
        }
        if (src == dest) {
            for (int i = currentMoveTargetIndex; i < srcBound; i++) {
                dest[i] = null;
            }
        }
        return srcBound - currentMoveTargetIndex;
    }

    public static int removeAllFromArray(final Object elementToRemove, final Object[] src, final int srcStart, final int srcBound, final Object[] dest, final int destStart, final int destBound, final Equalator<Object> equalator) throws ArrayIndexOutOfBoundsException {
        if (elementToRemove == null) {
            return removeAllFromArray(elementToRemove, src, srcStart, srcBound, dest, destStart, destBound);
        }
        int currentMoveTargetIndex;
        if (src == dest) {
            currentMoveTargetIndex = srcStart;
            while (currentMoveTargetIndex < destBound && !equalator.equal(elementToRemove, src[currentMoveTargetIndex])) {
                currentMoveTargetIndex++;
            }
        } else {
            currentMoveTargetIndex = destStart;
        }
        int currentMoveSourceIndex = 0;
        int currentMoveLength = 0;
        int seekIndex = currentMoveTargetIndex;
        while (seekIndex < srcBound) {
            while (seekIndex < srcBound && equalator.equal(elementToRemove, src[seekIndex])) {
                seekIndex++;
            }
            currentMoveSourceIndex = seekIndex;
            while (seekIndex < srcBound && !equalator.equal(elementToRemove, src[seekIndex])) {
                seekIndex++;
            }
            currentMoveLength = seekIndex - currentMoveSourceIndex;
            switch(currentMoveLength) {
                case 4:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                    dest[currentMoveTargetIndex + 1] = src[currentMoveSourceIndex + 1];
                    dest[currentMoveTargetIndex + 2] = src[currentMoveSourceIndex + 2];
                    dest[currentMoveTargetIndex + 3] = src[currentMoveSourceIndex + 3];
                    break;
                case 3:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                    dest[currentMoveTargetIndex + 1] = src[currentMoveSourceIndex + 1];
                    dest[currentMoveTargetIndex + 2] = src[currentMoveSourceIndex + 2];
                    break;
                case 2:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                    dest[currentMoveTargetIndex + 1] = src[currentMoveSourceIndex + 1];
                    break;
                case 1:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                case 0:
                    break;
                default:
                    System.arraycopy(src, currentMoveSourceIndex, dest, currentMoveTargetIndex, currentMoveLength);
            }
            currentMoveTargetIndex += currentMoveLength;
        }
        if (src == dest) {
            for (int i = currentMoveTargetIndex; i < srcBound; i++) {
                dest[i] = null;
            }
        }
        return srcBound - currentMoveTargetIndex;
    }

    public static <E> int removeAllFromArray(final XGettingCollection<E> elementsToRemove, final E[] src, final int srcStart, final int srcBound, final E[] dest, final int destStart, final int destBound, final boolean ignoreNulls, final Equalator<? super E> equalator) throws ArrayIndexOutOfBoundsException {
        E e = null;
        int currentMoveTargetIndex;
        if (src == dest) {
            currentMoveTargetIndex = srcStart;
            while (currentMoveTargetIndex < destBound && (e = src[currentMoveTargetIndex]) == null && ignoreNulls || !elementsToRemove.contains(Jadoth.predicate(e, equalator))) {
                currentMoveTargetIndex++;
            }
        } else {
            currentMoveTargetIndex = destStart;
        }
        int currentMoveSourceIndex = 0;
        int currentMoveLength = 0;
        int seekIndex = currentMoveTargetIndex;
        while (seekIndex < srcBound) {
            while (seekIndex < srcBound && ((e = src[seekIndex]) != null || !ignoreNulls) && elementsToRemove.contains(Jadoth.predicate(e, equalator))) {
                seekIndex++;
            }
            currentMoveSourceIndex = seekIndex;
            while (seekIndex < srcBound && ((e = src[seekIndex]) == null && ignoreNulls || !elementsToRemove.contains(Jadoth.predicate(e, equalator)))) {
                seekIndex++;
            }
            currentMoveLength = seekIndex - currentMoveSourceIndex;
            switch(currentMoveLength) {
                case 4:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                    dest[currentMoveTargetIndex + 1] = src[currentMoveSourceIndex + 1];
                    dest[currentMoveTargetIndex + 2] = src[currentMoveSourceIndex + 2];
                    dest[currentMoveTargetIndex + 3] = src[currentMoveSourceIndex + 3];
                    break;
                case 3:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                    dest[currentMoveTargetIndex + 1] = src[currentMoveSourceIndex + 1];
                    dest[currentMoveTargetIndex + 2] = src[currentMoveSourceIndex + 2];
                    break;
                case 2:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                    dest[currentMoveTargetIndex + 1] = src[currentMoveSourceIndex + 1];
                    break;
                case 1:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                case 0:
                    break;
                default:
                    System.arraycopy(src, currentMoveSourceIndex, dest, currentMoveTargetIndex, currentMoveLength);
            }
            currentMoveTargetIndex += currentMoveLength;
        }
        if (src == dest) {
            for (int i = currentMoveTargetIndex; i < srcBound; i++) {
                dest[i] = null;
            }
        }
        return srcBound - currentMoveTargetIndex;
    }

    public static <E> int removeAllFromArray(final E[] elementsToRemove, final E[] src, final int srcStart, final int srcBound, final E[] dest, final int destStart, final int destBound, final boolean ignoreNulls, final Equalator<? super E> eq) throws ArrayIndexOutOfBoundsException {
        int currentMoveTargetIndex;
        E e = null;
        if (src == dest) {
            currentMoveTargetIndex = srcStart;
            while (currentMoveTargetIndex < destBound && (e = src[currentMoveTargetIndex]) == null && ignoreNulls || !contains(elementsToRemove, e, eq)) {
                currentMoveTargetIndex++;
            }
        } else {
            currentMoveTargetIndex = destStart;
        }
        int currentMoveSourceIndex = 0;
        int currentMoveLength = 0;
        int seekIndex = currentMoveTargetIndex;
        while (seekIndex < srcBound) {
            while (seekIndex < srcBound && ((e = src[seekIndex]) != null || !ignoreNulls) && contains(elementsToRemove, e, eq)) {
                seekIndex++;
            }
            currentMoveSourceIndex = seekIndex;
            while (seekIndex < srcBound && ((e = src[seekIndex]) == null && ignoreNulls || !contains(elementsToRemove, e, eq))) {
                seekIndex++;
            }
            currentMoveLength = seekIndex - currentMoveSourceIndex;
            switch(currentMoveLength) {
                case 4:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                    dest[currentMoveTargetIndex + 1] = src[currentMoveSourceIndex + 1];
                    dest[currentMoveTargetIndex + 2] = src[currentMoveSourceIndex + 2];
                    dest[currentMoveTargetIndex + 3] = src[currentMoveSourceIndex + 3];
                    break;
                case 3:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                    dest[currentMoveTargetIndex + 1] = src[currentMoveSourceIndex + 1];
                    dest[currentMoveTargetIndex + 2] = src[currentMoveSourceIndex + 2];
                    break;
                case 2:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                    dest[currentMoveTargetIndex + 1] = src[currentMoveSourceIndex + 1];
                    break;
                case 1:
                    dest[currentMoveTargetIndex] = src[currentMoveSourceIndex];
                case 0:
                    break;
                default:
                    System.arraycopy(src, currentMoveSourceIndex, dest, currentMoveTargetIndex, currentMoveLength);
            }
            currentMoveTargetIndex += currentMoveLength;
        }
        if (src == dest) {
            for (int i = currentMoveTargetIndex; i < srcBound; i++) {
                dest[i] = null;
            }
        }
        return srcBound - currentMoveTargetIndex;
    }

    public static final <T> T[] reverse(final T[] array) {
        final int halfSize = array.length >> 1;
        for (int i = 0, j = array.length - 1; i < halfSize; i++, j--) {
            final T e = array[i];
            array[i] = array[j];
            array[j] = e;
        }
        return array;
    }

    @SuppressWarnings("unchecked")
    public static final <T> T[] toReversed(final T[] array) {
        final int len;
        final T[] rArray = (T[]) Array.newInstance(array.getClass().getComponentType(), len = array.length);
        for (int i = 0, r = len; i < len; i++) {
            rArray[--r] = array[i];
        }
        return rArray;
    }

    @SuppressWarnings("unchecked")
    public static final <T> T[] toReversed(final T[] array, final int offset, final int length) {
        return length < 0 ? Jadoth.reverseArraycopy(array, offset, (T[]) Array.newInstance(array.getClass().getComponentType(), -length), 0, -length) : Jadoth.reverseArraycopy(array, offset + length - 1, (T[]) Array.newInstance(array.getClass().getComponentType(), length), 0, length);
    }

    @SuppressWarnings("unchecked")
    public static final <T> T[] copy(final T[] array) {
        final T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length);
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    @SafeVarargs
    public static final <T> T[] shuffle(final T... data) {
        final FastRandom random = new FastRandom();
        for (int i = data.length, j; i > 1; i--) {
            final T t = data[i - 1];
            data[i - 1] = data[j = random.nextInt(i)];
            data[j] = t;
        }
        return data;
    }

    public static final int[] shuffle(final int... data) {
        return shuffle(new FastRandom(), data);
    }

    public static final int[] shuffle(final FastRandom random, final int... data) {
        for (int i = data.length, j; i > 1; i--) {
            final int t = data[i - 1];
            data[i - 1] = data[j = random.nextInt(i)];
            data[j] = t;
        }
        return data;
    }

    /**
	 * Convenience method, calling either {@link System#arraycopy(Object, int, Object, int, int)} for
	 * {@code length >= 0} or {@link Jadoth#reverseArraycopy(Object[], int, Object[], int, int)} for {@code length < 0}
	 * and returns {@code dest}.<br>
	 * If length is known to be positive and performance badly matters or negative length shall be treated as an error,
	 * use {@link System#arraycopy(Object, int, Object, int, int)} directly. Otherwise, this method is a convenient
	 * alternative to handle more flexible bi-directional array copying.
	 *
	 * @param <T>
	 * @param <U>
	 * @param src
	 * @param srcPos
	 * @param dest
	 * @param destPos
	 * @param length
	 * @return
	 */
    public static <T, U extends T> T[] arraycopy(final U[] src, final int srcPos, final T[] dest, final int destPos, final int length) {
        if (length < 0) {
            return Jadoth.reverseArraycopy(src, srcPos, dest, destPos, -length);
        }
        System.arraycopy(src, srcPos, dest, destPos, length);
        return dest;
    }

    public static final boolean containsNull(final Object[] data, int offset, final int length) {
        final int endIndex, d;
        if (length >= 0) {
            if (offset < 0 || (endIndex = offset + length - 1) >= data.length) {
                throw new IndexOutOfBoundsException(exceptionRange(data.length, offset, length));
            }
            if (length == 0) {
                return false;
            }
            d = +1;
        } else if (length < 0) {
            if ((endIndex = offset + length + 1) < 0 || offset >= data.length) {
                throw new IndexOutOfBoundsException(exceptionRange(data.length, offset, length));
            }
            d = -1;
        } else if (offset < 0 || offset >= data.length) {
            throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(data.length, offset));
        } else {
            return false;
        }
        for (offset -= d; offset != endIndex; ) {
            if (data[offset += d] == null) return true;
        }
        return false;
    }

    public static <E> E[] filter(final E[] elements, final Predicate<? super E> predicate) {
        return filterTo(elements, new BulkList<E>(), predicate).toArray(componentType(elements));
    }

    public static <E, C extends Collector<? super E>> C filterTo(final E[] elements, final C target, final Predicate<? super E> predicate) {
        for (final E e : elements) if (predicate.apply(e)) target.collect(e);
        return target;
    }

    public static <E> int replaceAllInArray(final E[] data, final int startLow, final int boundHigh, final E oldElement, final E newElement) {
        int replaceCount = 0;
        for (int i = startLow; i < boundHigh; i++) {
            if (data[i] == oldElement) {
                data[i] = newElement;
                replaceCount++;
            }
        }
        return replaceCount;
    }

    public static <E> int replaceAllInArray(final E[] data, int startLow, final int boundHigh, final E sample, final Equalator<? super E> equalator, final E newElement) {
        int replaceCount = 0;
        for (; startLow < boundHigh; startLow++) {
            if (equalator.equal(data[startLow], sample)) {
                data[startLow] = newElement;
                replaceCount++;
            }
        }
        return replaceCount;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] and(final T[] a1, final T[] a2) {
        final int length;
        final T[] target = (T[]) Array.newInstance(a1.getClass().getComponentType(), length = min(a1.length, a2.length));
        for (int i = 0; i < length; i++) {
            target[i] = a1[i] != null && a2[i] != null ? a1[i] : null;
        }
        return target;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] or(final T[] a1, final T[] a2) {
        final int length;
        final T[] target = (T[]) Array.newInstance(a1.getClass().getComponentType(), length = min(a1.length, a2.length));
        for (int i = 0; i < length; i++) {
            target[i] = a1[i] != null ? a1[i] : a2[i] != null ? a2[i] : null;
        }
        return target;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] not(final T[] a1, final T[] a2) {
        final int length;
        final T[] target = (T[]) Array.newInstance(a1.getClass().getComponentType(), length = min(a1.length, a2.length));
        for (int i = 0; i < length; i++) {
            target[i] = a2[i] == null ? a1[i] : null;
        }
        return target;
    }

    /**
	 * Orders the passed elements by the passed indices.
	 *
	 * @param elements the elements to be sorted according to the passed indices.
	 * @param indices the indices defining the order in which the passed elements shall be rearranged.
	 * @param target the target array to receive the sorted elements.
	 */
    public static <S, T extends S> S[] orderByIndices(final T[] elements, final int[] indices, final int indicesOffset, final S[] target) throws IllegalArgumentException {
        if (indicesOffset < 0) {
            throw new ArrayIndexOutOfBoundsException(indicesOffset);
        }
        final int targetLength = target.length;
        if (elements.length + indicesOffset > indices.length) {
            throw new ArrayIndexOutOfBoundsException(elements.length + indicesOffset);
        }
        final int indicesBound = indicesOffset + elements.length;
        for (int i = indicesOffset; i < indicesBound; i++) {
            if (indices[i] >= targetLength) {
                throw new ArrayIndexOutOfBoundsException(indices[i]);
            }
        }
        for (int i = indicesOffset; i < indicesBound; i++) {
            if (indices[i] < 0) {
                continue;
            }
            target[indices[i]] = elements[i - indicesOffset];
        }
        return target;
    }
}
