package ru.spb.leonidv.lvcontrols.beansutils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameUtils {

    /**
     * Возвращает имя свойства в нотации JavaBeans для переданного метода
     * установки или возвращения метода.
     * 
     * @param methodName
     * @return
     */
    public static String getPropertyName(String methodName) {
        int prefixLength;
        if (methodName.startsWith("set") || methodName.startsWith("get") || methodName.startsWith("has")) {
            prefixLength = 3;
        } else if (methodName.startsWith("is")) {
            prefixLength = 2;
        } else {
            throw new IllegalArgumentException("Not correct JavaBeans method name");
        }
        final String propertyName = methodName.substring(prefixLength + 1);
        final String firstChar = methodName.substring(prefixLength, prefixLength + 1).toLowerCase();
        return firstChar + propertyName;
    }

    /**
     * Проверяет, является ли переданный метод методом доступа к свойству
     * 
     * @param methodName
     *          имя проверяемого метода
     * @return истина, если метод является методом доступа к свойству
     */
    public static boolean isPropertyMethod(String methodName) {
        return isSetter(methodName) || isGetter(methodName);
    }

    /**
     * Проверяет, является ли данный метод методом установки свойства
     * 
     * @param methodName
     *          имя проверяемого метода
     * @return истина, если метод является методом установки значения свойства
     */
    public static boolean isSetter(String methodName) {
        return methodName.startsWith("set");
    }

    /**
     * Проверяет, является ли данный метод методом чтения свойства
     * 
     * @param methodName
     *          имя проверяемого метода
     * @return истина, если метод является методом чтения значения свойства
     */
    public static boolean isGetter(String methodName) {
        if (methodName.equals("hashCode")) {
            return false;
        }
        return methodName.startsWith("get") || methodName.startsWith("is") || methodName.startsWith("has");
    }

    /**
     * Осуществляет перевод имени в JavaBeans натации в более приятный для
     * человека вид. По сути, каждая заглавная буква
     * 
     * @param propertyName
     * @return
     */
    public static String getHumanPresentation(String propertyName) {
        Pattern pattern = Pattern.compile("\\p{Upper}+");
        Matcher matcher = pattern.matcher(propertyName);
        StringBuffer result = new StringBuffer(propertyName);
        int offset = 0;
        while (matcher.find()) {
            String capitalsLetters = propertyName.substring(matcher.start(), matcher.end());
            if (capitalsLetters.length() == 1) {
                capitalsLetters = capitalsLetters.toLowerCase();
            }
            result.replace(matcher.start() + offset, matcher.end() + offset, " " + capitalsLetters);
            offset++;
        }
        if (result.substring(0, 1).equals(" ")) {
            result.delete(0, 1);
        }
        String firstLetter = result.substring(0, 1).toUpperCase();
        result.replace(0, 1, firstLetter);
        return result.toString();
    }

    private static String getMethodSuffix(String propertyName) {
        StringBuffer buffer = new StringBuffer(propertyName);
        String firstChar = buffer.substring(0, 1).toUpperCase();
        buffer.replace(0, 1, firstChar);
        return buffer.toString();
    }

    /**
     * Возвращает имя метода установка значения для указанного свойства
     * 
     * @param propertyName
     *          имя свойства
     * @return имя метода установки значения свойства
     */
    public static String getSetterName(String propertyName) {
        return "set" + getMethodSuffix(propertyName);
    }

    /**
     * Возвращает массив имен получения значения для указанного свойства
     * 
     * @param propertyName
     *          имя свойства
     * @return все возможные имя методов доступа к значению свойства
     */
    public static String[] getGetterNames(String propertyName) {
        String[] result = new String[3];
        final String suffix = getMethodSuffix(propertyName);
        result[0] = "get" + suffix;
        result[1] = "is" + suffix;
        result[2] = "has" + suffix;
        return result;
    }
}
