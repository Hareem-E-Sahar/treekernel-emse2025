package net.sourceforge.javabits.util;

import java.lang.reflect.Array;
import java.util.Comparator;
import net.sourceforge.javabits.function.Function;
import net.sourceforge.javabits.lang.Strings;
import net.sourceforge.javabits.predicate.Predicate;

/**
 * @author Jochen Kuhnle
 */
public abstract class JArrays {

    public static final <E> String join(E[] array, CharSequence separator, int start, int end) {
        StringBuilder buf = new StringBuilder();
        for (int i = start; i < end; ++i) {
            buf.append(array[i]);
            buf.append(separator);
        }
        Strings.cutoff(buf, separator.length());
        return buf.toString();
    }

    public static final <E> String join(E[] array, CharSequence separator) {
        return join(array, separator, 0, array.length);
    }

    public static final <E> void transform(E[] array, Function<? super E, ? extends E> function, int start, int end) {
        for (int i = start; i < end; ++i) {
            array[i] = function.evaluate(array[i]);
        }
    }

    public static final <E> void transform(E[] array, Function<? super E, ? extends E> function) {
        transform(array, function, 0, array.length);
    }

    public static final <E> int index(E[] array, Predicate<? super E> predicate, int start, int end) {
        int result = -1;
        for (int i = start; i < end; ++i) {
            if (predicate.evaluate(array[i])) {
                result = i;
                break;
            }
        }
        return result;
    }

    public static final <E> int index(E[] array, Predicate<E> predicate) {
        return index(array, predicate, 0, array.length);
    }

    public static final <E> int lastIndex(E[] array, Predicate<? super E> predicate, int start, int end) {
        int result = -1;
        for (int i = end - 1; i >= start; --i) {
            if (predicate.evaluate(array[i])) {
                result = i;
                break;
            }
        }
        return result;
    }

    public static final <E> int lastIndex(E[] array, Predicate<E> predicate) {
        return lastIndex(array, predicate, 0, array.length);
    }

    public static final <E> int compareElement(E elem1, E elem2, Comparator<? super E> comparator, int index) {
        int comparison = comparator.compare(elem1, elem2);
        int result = 0;
        if (comparison < 0) {
            result = -index - 1;
        } else if (comparison > 0) {
            result = index + 1;
        }
        return result;
    }

    public static final <E> int compare(E[] array1, E pad1, E[] array2, E pad2, Comparator<? super E> comparator) {
        int result = 0;
        int min = Math.min(array1.length, array2.length);
        check: {
            for (int i = 0; i < min; ++i) {
                result = compareElement(array1[i], array2[i], comparator, i);
                if (result != 0) {
                    break check;
                }
            }
            for (int i = min; i < array1.length; ++i) {
                result = compareElement(array1[i], pad2, comparator, i);
                if (result != 0) {
                    break check;
                }
            }
            for (int i = min; i < array2.length; ++i) {
                result = compareElement(pad1, array2[i], comparator, i);
                if (result != 0) {
                    break check;
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static final <E> E[] resized(E[] array, int length) {
        E[] result = array;
        if (array.length != length) {
            result = (E[]) Array.newInstance(array.getClass().getComponentType(), length);
            int minLength = Math.min(array.length, length);
            System.arraycopy(array, 0, result, 0, minLength);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static final <E> E[] subArray(E[] array, int start, int end) {
        E[] result = (E[]) Array.newInstance(array.getClass().getComponentType(), end - start);
        System.arraycopy(array, start, result, 0, end - start);
        return result;
    }

    public static final float[] resized(float[] array, int length) {
        float[] result = array;
        if (array.length != length) {
            result = new float[length];
            int minLength = Math.min(array.length, length);
            System.arraycopy(array, 0, result, 0, minLength);
        }
        return result;
    }

    public static final int min(int[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array is empty.");
        }
        int result = Integer.MAX_VALUE;
        for (int i : array) {
            result = Math.min(result, i);
        }
        return result;
    }

    public static final float min(float[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array is empty.");
        }
        float result = Float.POSITIVE_INFINITY;
        for (float f : array) {
            result = Math.min(result, f);
        }
        return result;
    }

    public static final float max(float[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array is empty.");
        }
        float result = Float.NEGATIVE_INFINITY;
        for (float f : array) {
            result = Math.max(result, f);
        }
        return result;
    }

    public static final float average(float[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array is empty.");
        }
        float sum = 0;
        for (float f : array) {
            sum += f;
        }
        float result = sum / array.length;
        return result;
    }

    public static final float positiveAverage(float[] array, float emptyValue) {
        float sum = 0;
        int counter = 0;
        for (float f : array) {
            if (f >= 0) {
                sum += f;
                ++counter;
            }
        }
        float result = emptyValue;
        if (counter > 0) {
            result = sum / counter;
        }
        return result;
    }

    public static final <E> int indexOf(E[] array, E searchValue, int defaultValue) {
        int result = defaultValue;
        for (int i = 0; i < array.length; ++i) {
            if (array[i].equals(searchValue)) {
                result = i;
                break;
            }
        }
        return result;
    }
}
