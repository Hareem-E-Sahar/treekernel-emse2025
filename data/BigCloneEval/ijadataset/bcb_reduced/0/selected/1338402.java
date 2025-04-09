package de.matthiasmann.twl.utils;

import de.matthiasmann.twl.CallbackWithReason;
import java.lang.reflect.Array;

/**
 * Callback list management functions
 *
 * @author Matthias Mann
 */
public class CallbackSupport {

    private CallbackSupport() {
    }

    private static void checkNotNull(Object callback) {
        if (callback == null) {
            throw new NullPointerException("callback");
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] addCallbackToList(T[] curList, T callback, Class<T> clazz) {
        checkNotNull(callback);
        final int curLength = (curList == null) ? 0 : curList.length;
        T[] newList = (T[]) Array.newInstance(clazz, curLength + 1);
        if (curLength > 0) {
            System.arraycopy(curList, 0, newList, 0, curLength);
        }
        newList[curLength] = callback;
        return newList;
    }

    public static <T> int findCallbackPosition(T[] list, T callback) {
        checkNotNull(callback);
        if (list != null) {
            for (int i = 0, n = list.length; i < n; i++) {
                if (list[i] == callback) {
                    return i;
                }
            }
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] removeCallbackFromList(T[] curList, int index) {
        final int curLength = curList.length;
        assert (index >= 0 && index < curLength);
        if (curLength == 1) {
            return null;
        }
        final int newLength = curLength - 1;
        T[] newList = (T[]) Array.newInstance(curList.getClass().getComponentType(), newLength);
        System.arraycopy(curList, 0, newList, 0, index);
        System.arraycopy(curList, index + 1, newList, index, newLength - index);
        return newList;
    }

    public static <T> T[] removeCallbackFromList(T[] curList, T callback) {
        int idx = findCallbackPosition(curList, callback);
        if (idx >= 0) {
            curList = removeCallbackFromList(curList, idx);
        }
        return curList;
    }

    public static void fireCallbacks(Runnable[] callbacks) {
        if (callbacks != null) {
            for (Runnable cb : callbacks) {
                cb.run();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> void fireCallbacks(CallbackWithReason<?>[] callbacks, T reason) {
        if (callbacks != null) {
            for (CallbackWithReason<?> cb : callbacks) {
                ((CallbackWithReason<T>) cb).callback(reason);
            }
        }
    }
}
