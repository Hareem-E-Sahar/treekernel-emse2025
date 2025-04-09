package com.golden.gamedev.util;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Random;

/**
 * Utility class provides general functions, such as array
 * enlargement/shrinkment operation, array mixed, randomize, and other functions
 * that categorized as common functions.
 */
public class Utility {

    private static final Random rnd = new Random();

    private Utility() {
    }

    /**
   * Expands an array of object by specified size, <code>src</code> can not be
   * <code>null</code>.
   * <p>
   * 
   * The original array is not changed, this method creates and returns a new
   * expanded array.
   * 
   * @param src
   *        the array to be expanded, could be an array of primitive or an array
   *        of Object
   * @param increase
   *        array size increment
   * @param bottom
   *        true, the expanded array is at the bottom
   * @return The expanded array.
   */
    public static Object expand(Object src, int increase, boolean bottom) {
        int size = Array.getLength(src);
        Object dest = Array.newInstance(src.getClass().getComponentType(), size + increase);
        System.arraycopy(src, 0, dest, (bottom) ? 0 : increase, size);
        return dest;
    }

    /**
   * Expands an array of object by specified size, <code>src</code> can not be
   * <code>null</code>.
   * <p>
   * 
   * The new expanded object will be at the bottom of the returned array (<b>last
   * index</b>).
   * 
   * @param src
   *        the array to be expanded, could be an array of primitive or an array
   *        of Object
   * @param increase
   *        array size increment
   * @return The expanded array.
   */
    public static Object expand(Object src, int increase) {
        return Utility.expand(src, increase, true);
    }

    /**
   * Expands an array of <code>Class type</code> object by specified size,
   * <code>src</code> can be <code>null</code>.
   * 
   * @param src
   *        the array to be expanded, could be an array of primitive or an array
   *        of Object
   * @param increase
   *        array size increment
   * @param bottom
   *        true, the expanded array is at the bottom
   * @param type
   *        array class
   * @return The expanded array.
   */
    public static Object expand(Object src, int increase, boolean bottom, Class type) {
        if (src == null) {
            return Array.newInstance(type, 1);
        }
        return Utility.expand(src, increase, bottom);
    }

    /**
   * Cuts an array of object from specified position.
   * <p>
   * 
   * The original array is not changed, this method creates and returns a new
   * shrinked array.
   * 
   * @param src
   *        the array to be cut, could be an array of primitive or an array of
   *        Object
   * @param position
   *        index position to be cut
   * @return The shrinked array.
   */
    public static Object cut(Object src, int position) {
        int size = Array.getLength(src);
        if (size == 1) {
            return Array.newInstance(src.getClass().getComponentType(), 0);
        }
        int numMoved = size - position - 1;
        if (numMoved > 0) {
            System.arraycopy(src, position + 1, src, position, numMoved);
        }
        size--;
        Object dest = Array.newInstance(src.getClass().getComponentType(), size);
        System.arraycopy(src, 0, dest, 0, size);
        return dest;
    }

    /**
   * Shuffles elements in an array.
   * 
   * @param src
   *        the array to be mixed, could be an array of primitive or an array of
   *        Object
   */
    public static void mixElements(Object src) {
        int size = Array.getLength(src);
        Object tempVal;
        int tempPos;
        for (int i = 0; i < size; i++) {
            tempPos = Utility.getRandom(i, size - 1);
            tempVal = Array.get(src, tempPos);
            Array.set(src, tempPos, Array.get(src, i));
            Array.set(src, i, tempVal);
        }
    }

    /**
   * Returns pre-defined Random object.
   */
    public static Random getRandomObject() {
        return Utility.rnd;
    }

    /**
   * Returns a random number, range from lowerbound (inclusive) to upperbound
   * (inclusive).
   * <p>
   * 
   * For example :
   * 
   * <pre>
   * // to return random number from 0 to 10
   * int rand = Utility.getRandom(0, 10);
   * </pre>
   * 
   * 
   * @param lowerBound
   *        the lowest random number
   * @param upperBound
   *        the highest random number
   * @return Random number range from lowerbound to upperbound.
   */
    public static int getRandom(int lowerBound, int upperBound) {
        return lowerBound + Utility.rnd.nextInt(upperBound - lowerBound + 1);
    }

    /**
   * Compacting String <code>s</code> to occupy less memory. Use this with a
   * big array of String to save up memory.
   * <p>
   * 
   * For example {@link FileUtil#fileRead(File)} method is using this method to
   * returned a compact string.
   * 
   * @param s
   *        an array of String to be compacted.
   * @return Compacted String.
   */
    public static String[] compactStrings(String[] s) {
        String[] result = new String[s.length];
        int offset = 0;
        for (int i = 0; i < s.length; ++i) {
            offset += s[i].length();
        }
        char[] allchars = new char[offset];
        offset = 0;
        for (int i = 0; i < s.length; ++i) {
            s[i].getChars(0, s[i].length(), allchars, offset);
            offset += s[i].length();
        }
        String allstrings = new String(allchars);
        offset = 0;
        for (int i = 0; i < s.length; ++i) {
            result[i] = allstrings.substring(offset, offset += s[i].length());
        }
        return result;
    }
}
