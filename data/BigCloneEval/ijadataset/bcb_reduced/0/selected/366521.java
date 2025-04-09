package abbot.swt.utilities;

import java.lang.reflect.Array;

public class JavaUtil {

    private static final String[] Prefixes = { "abbot.swt", "org.eclipse.swt", "java." };

    /**
	 * Gets the sort-of-concise name of a {@link Class}.
	 * 
	 * @param aClass
	 *            a {@link Class}
	 * @return the {@link Class}' sort-of-concise name
	 */
    public static String getClassName(Class<?> aClass) {
        Class<?> componentType = aClass.getComponentType();
        if (componentType != null) {
            return getClassName(componentType) + "[]";
        }
        Class<?> enclosingClass = aClass.getEnclosingClass();
        if (enclosingClass != null) return getClassName(enclosingClass) + "$" + aClass.getSimpleName();
        String name = aClass.getName();
        if (!aClass.isAnonymousClass() && !aClass.isSynthetic() && !aClass.isLocalClass()) {
            for (String prefix : Prefixes) {
                if (name.startsWith(prefix)) {
                    String simpleName = aClass.getSimpleName();
                    if (simpleName.length() > 0) return simpleName;
                }
            }
        }
        return name;
    }

    /**
	 * Gets a copy of an array with a new element appended.
	 * 
	 * @param array
	 *            an array
	 * @param element
	 *            the element to be appended
	 * @return a new array that is a copy of <code>array</code> with <code>element</code> appended
	 */
    @SuppressWarnings("unchecked")
    public static <T> T[] append(T[] array, T element) {
        T[] array2 = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length + 1);
        System.arraycopy(array, 0, array2, 0, array.length);
        array2[array.length] = element;
        return array2;
    }

    /**
	 * Gets a copy of an array with the elements of another array appended.
	 * 
	 * @param array0
	 *            an array
	 * @param element
	 *            the element to be appended
	 * @return a new array that is a copy of <code>array</code> with <code>element</code> appended
	 */
    @SuppressWarnings("unchecked")
    public static <T> T[] append(T[] array0, T[] array1) {
        T[] array2 = (T[]) Array.newInstance(array0.getClass().getComponentType(), array0.length + array1.length);
        System.arraycopy(array0, 0, array2, 0, array0.length);
        System.arraycopy(array1, 0, array2, array0.length, array1.length);
        return array2;
    }
}
