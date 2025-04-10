package com.sun.beans.finder;

import java.util.HashMap;
import java.util.Map;

/**
 * This abstract class provides functionality
 * to find a public method or constructor
 * with specified parameter types.
 * It supports a variable number of parameters.
 *
 * @since 1.7
 *
 * @author Sergey A. Malenkov
 */
abstract class AbstractFinder<T> {

    private final Class<?>[] args;

    /**
     * Creates finder for array of classes of arguments.
     * If a particular element of array equals {@code null},
     * than the appropriate pair of classes
     * does not take into consideration.
     *
     * @param args  array of classes of arguments
     */
    protected AbstractFinder(Class<?>[] args) {
        this.args = args;
    }

    /**
     * Returns an array of {@code Class} objects
     * that represent the formal parameter types of the method
     * Returns an empty array if the method takes no parameters.
     *
     * @param method  the object that represents method
     * @return the parameter types of the method
     */
    protected abstract Class<?>[] getParameters(T method);

    /**
     * Returns {@code true} if and only if the method
     * was declared to take a variable number of arguments.
     *
     * @param method  the object that represents method
     * @return {@code true} if the method was declared
     *         to take a variable number of arguments;
     *         {@code false} otherwise
     */
    protected abstract boolean isVarArgs(T method);

    /**
     * Checks validness of the method.
     * At least the valid method should be public.
     *
     * @param method  the object that represents method
     * @return {@code true} if the method is valid,
     *         {@code false} otherwise
     */
    protected abstract boolean isValid(T method);

    /**
     * Performs a search in the {@code methods} array.
     * The one method is selected from the array of the valid methods.
     * The list of parameters of the selected method shows
     * the best correlation with the list of arguments
     * specified at class initialization.
     * If more than one method is both accessible and applicable
     * to a method invocation, it is necessary to choose one
     * to provide the descriptor for the run-time method dispatch.
     * The most specific method should be chosen.
     *
     * @param methods  the array of methods to search within
     * @return the object that represents found method
     * @throws NoSuchMethodException if no method was found or several
     *                               methods meet the search criteria
     * @see #isAssignable
     */
    final T find(T[] methods) throws NoSuchMethodException {
        Map<T, Class<?>[]> map = new HashMap<T, Class<?>[]>();
        T oldMethod = null;
        Class<?>[] oldParams = null;
        boolean ambiguous = false;
        for (T newMethod : methods) {
            if (isValid(newMethod)) {
                Class<?>[] newParams = getParameters(newMethod);
                if (newParams.length == this.args.length) {
                    PrimitiveWrapperMap.replacePrimitivesWithWrappers(newParams);
                    if (isAssignable(newParams, this.args)) {
                        if (oldMethod == null) {
                            oldMethod = newMethod;
                            oldParams = newParams;
                        } else {
                            boolean useNew = isAssignable(oldParams, newParams);
                            boolean useOld = isAssignable(newParams, oldParams);
                            if (useOld == useNew) {
                                ambiguous = true;
                            } else if (useNew) {
                                oldMethod = newMethod;
                                oldParams = newParams;
                                ambiguous = false;
                            }
                        }
                    }
                }
                if (isVarArgs(newMethod)) {
                    int length = newParams.length - 1;
                    if (length <= this.args.length) {
                        Class<?>[] array = new Class<?>[this.args.length];
                        System.arraycopy(newParams, 0, array, 0, length);
                        if (length < this.args.length) {
                            Class<?> type = newParams[length].getComponentType();
                            if (type.isPrimitive()) {
                                type = PrimitiveWrapperMap.getType(type.getName());
                            }
                            for (int i = length; i < this.args.length; i++) {
                                array[i] = type;
                            }
                        }
                        map.put(newMethod, array);
                    }
                }
            }
        }
        for (T newMethod : methods) {
            Class<?>[] newParams = map.get(newMethod);
            if (newParams != null) {
                if (isAssignable(newParams, this.args)) {
                    if (oldMethod == null) {
                        oldMethod = newMethod;
                        oldParams = newParams;
                    } else {
                        boolean useNew = isAssignable(oldParams, newParams);
                        boolean useOld = isAssignable(newParams, oldParams);
                        if (useOld == useNew) {
                            if (oldParams == map.get(oldMethod)) {
                                ambiguous = true;
                            }
                        } else if (useNew) {
                            oldMethod = newMethod;
                            oldParams = newParams;
                            ambiguous = false;
                        }
                    }
                }
            }
        }
        if (ambiguous) {
            throw new NoSuchMethodException("Ambiguous methods are found");
        }
        if (oldMethod == null) {
            throw new NoSuchMethodException("Method is not found");
        }
        return oldMethod;
    }

    /**
     * Determines if every class in {@code min} array is either the same as,
     * or is a superclass of, the corresponding class in {@code max} array.
     * The length of every array must equal the number of arguments.
     * This comparison is performed in the {@link #find} method
     * before the first call of the isAssignable method.
     * If an argument equals {@code null}
     * the appropriate pair of classes does not take into consideration.
     *
     * @param min  the array of classes to be checked
     * @param max  the array of classes that is used to check
     * @return {@code true} if all classes in {@code min} array
     *         are assignable from corresponding classes in {@code max} array,
     *         {@code false} otherwise
     *
     * @see Class#isAssignableFrom
     */
    private boolean isAssignable(Class<?>[] min, Class<?>[] max) {
        for (int i = 0; i < this.args.length; i++) {
            if (null != this.args[i]) {
                if (!min[i].isAssignableFrom(max[i])) {
                    return false;
                }
            }
        }
        return true;
    }
}
