package org.amlfilter.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.CRC32;

/**
 *
 * The purpose of this class is to provide
 * some general purpose utility functions
 *
 * @author Harish Seshadri
 * @version $Id: GeneralUtils.java,v 1.1 2007/01/28 07:13:38 hseshadr Exp $
 */
public class GeneralUtils implements GeneralConstants {

    /**
     * This method generates a fully qualified
     * class name given a class package name and a class name.
     * @param pClassPackageName The class package name
     * @param pClassName The class name
     * @return The fully qualified class name
     * @throws IllegalArgumentException
     */
    public static String generateClassName(String pClassPackageName, String pClassName) {
        if (null == pClassPackageName) {
            throw new IllegalArgumentException(NO_CLASS_PACKAGE_NAME);
        }
        if (null == pClassName || 0 == pClassName.length()) {
            throw new IllegalArgumentException(NO_CLASS_NAME);
        }
        StringBuilder fullClassName = new StringBuilder();
        fullClassName.append(pClassPackageName);
        if (!pClassPackageName.endsWith(".")) {
            fullClassName.append(".");
        }
        fullClassName.append(pClassName);
        return fullClassName.toString();
    }

    /**
     * Creates the class instance
     * @param pClassName The fully qualified class name
     * @return The object instance of the class
     * @throws IllegalArgumentException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static Object createClassInstance(String pClassName) throws IllegalArgumentException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (null == pClassName || 0 == pClassName.length()) {
            throw new IllegalArgumentException(NO_CLASS_NAME);
        }
        Class classToInstantiate;
        Object object;
        classToInstantiate = Class.forName(pClassName);
        object = classToInstantiate.newInstance();
        return object;
    }

    /**
     * Format a given message from the bundle
     * @param pFormattingPattern The formatting pattern
     * @param pFormattingArguments The formatting arguments to substitute
     * @return The formatted output
     * @throws IllegalArgumentException
     */
    public static String formatMessage(String pFormattingPattern, Object[] pFormattingArguments) throws IllegalArgumentException {
        if (null == pFormattingPattern) {
            throw new IllegalArgumentException(NO_FORMATTING_PATTERN);
        }
        if (null == pFormattingArguments) {
            throw new IllegalArgumentException(NO_FORMATTING_ARGUMENTS);
        }
        return MessageFormat.format(pFormattingPattern, pFormattingArguments);
    }

    /**
     * This method generates an absolute path by
     * essentially pre-pending the root path to the
     * relative path. It abides by some rules during
     * the generation:
     * If the root path ends with the path delimiter
     *   (i.e "/", "\", ...) and the relative path begins
     *   with the path delimiter then strip of the path
     *   delimiter from the root path and then prepend
     *   the new root path to the relative path.
     * If the root path does not end with a path delimiter
     *   and the relative path does not begin with a path
     *   delimiter, then append a path delimiter to the
     *   root path. The new root path is then pre-pended to
     *   the relative path
     * The other cases are simply appended
     * @param pRootPath The root path
     * @param pRelativePath The relative path
     * @param pPathDelimiter The path delimiter
     * @return The absolute path
     * @throws IllegalArgumentException
     */
    public static String generateAbsolutePath(String pRootPath, String pRelativePath, String pPathDelimiter) throws IllegalArgumentException {
        if (null == pRootPath) {
            throw new IllegalArgumentException(NO_ROOT_PATH);
        }
        if (null == pRelativePath) {
            throw new IllegalArgumentException(NO_RELATIVE_PATH);
        }
        if (null == pPathDelimiter) {
            throw new IllegalArgumentException(NO_DELIMITER);
        }
        StringBuilder absolutePath = new StringBuilder();
        if (pRootPath.endsWith(pPathDelimiter) && pRelativePath.startsWith(pPathDelimiter)) {
            absolutePath.append(pRootPath.substring(0, pRootPath.length() - 1));
            absolutePath.append(pRelativePath.substring(0, pRelativePath.length()));
        } else if (!(pRootPath.endsWith(pPathDelimiter)) && !(pRelativePath.startsWith(pPathDelimiter))) {
            absolutePath.append(pRootPath);
            absolutePath.append(pPathDelimiter);
            absolutePath.append(pRelativePath);
        } else {
            absolutePath.append(pRootPath);
            absolutePath.append(pRelativePath);
        }
        return absolutePath.toString();
    }

    /**
     * This method strips the file name extension
     * out of the file name
     * @param pFileName The file name
     * @return The extension-free file name
     * @throws IllegalArgumentException
     */
    public static String stripFileExtension(String pFileNameWithExtension) throws IllegalArgumentException {
        if (null == pFileNameWithExtension) {
            throw new IllegalArgumentException(NO_FILE_NAME_WITH_EXT);
        }
        int pos = pFileNameWithExtension.lastIndexOf(".");
        if (-1 == pos) {
            return pFileNameWithExtension;
        }
        return pFileNameWithExtension.substring(0, pos);
    }

    /**
     * Escape any characters to html that is higher than 7-bit (2^7=128)
     * @param pStringToEscape The string to escape
     * @return The escaped html string
     * @throws IllegalArgumentException
     */
    public static String htmlEscape(String pStringToEscape) throws IllegalArgumentException {
        if (null == pStringToEscape) {
            throw new IllegalArgumentException(NO_STRING_TO_ESCAPE);
        }
        StringBuilder htmlEscapeBuffer = new StringBuilder();
        for (int i = 0; i < pStringToEscape.length(); i++) {
            char ch = pStringToEscape.charAt(i);
            if (((int) ch) > 127) {
                htmlEscapeBuffer.append(HTML_ESCAPE_PREFIX_WITH_NUMERIC_CODE);
                htmlEscapeBuffer.append((int) ch);
                htmlEscapeBuffer.append(HTML_ESCAPE_SUFFIX);
            } else {
                htmlEscapeBuffer.append(ch);
            }
        }
        return htmlEscapeBuffer.toString();
    }

    /**
     * Escape any characters to html that is higher than 7-bit (2^7=128)
     * @param pStringToEscape The string to escape
     * @return The escaped html string
     * @throws IllegalArgumentException
     */
    public static String htmlEscapeAll(String pStringToEscape) throws IllegalArgumentException {
        if (null == pStringToEscape) {
            throw new IllegalArgumentException(NO_STRING_TO_ESCAPE);
        }
        StringBuilder htmlEscapeBuffer = new StringBuilder();
        for (int i = 0; i < pStringToEscape.length(); i++) {
            char ch = pStringToEscape.charAt(i);
            if (((int) ch) > 127) {
                htmlEscapeBuffer.append(HTML_ESCAPE_PREFIX_WITH_NUMERIC_CODE);
                htmlEscapeBuffer.append((int) ch);
                htmlEscapeBuffer.append(HTML_ESCAPE_SUFFIX);
            } else {
                if ('&' == ch) {
                    htmlEscapeBuffer.append("&amp;");
                } else if ('>' == ch) {
                    htmlEscapeBuffer.append("&gt;");
                } else if ('<' == ch) {
                    htmlEscapeBuffer.append("&lt;");
                } else {
                    htmlEscapeBuffer.append(ch);
                }
            }
        }
        return htmlEscapeBuffer.toString();
    }

    /**
     * This method creates the tab string
     * for the indentation based on the level
     * @param pLevel The current level
     */
    public static String createTabString(int pLevel) {
        StringBuilder sb = new StringBuilder("\t");
        for (int i = 0; i < pLevel; i++) {
            sb.append("\t");
        }
        return sb.toString();
    }

    /**
     * This method converts an exception stack trace to a string.
     * It uses a string writer object which is wrapped by a
     * print writer object to fit the printStackTrace signature.
     * The string writer is a character stream that collects its
     * output in a string buffer, which can be used to construct
     * a string.
     * @param pThrowable A throwable (exception)
     * @return The exception stack as a string
     * @throws IllegalArgumentException
     */
    public static String getStackTraceAsString(Throwable pThrowable) throws IllegalArgumentException {
        if (null == pThrowable) {
            throw new IllegalArgumentException(NO_THROWABLE_OBJECT);
        }
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        pThrowable.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    /**
     * This method converts an exception stack trace to a string.
     * It uses a string writer object which is wrapped by a
     * print writer object to fit the printStackTrace signature.
     * The string writer is a character stream that collects its
     * output in a string buffer, which can be used to construct
     * a string.
     * @param pThrowable A throwable (exception)
     * @param pNumLines The number of lines to display
     * @return The exception stack as a string
     * @throws IllegalArgumentException
     */
    public static String getStackTraceAsString(Throwable pThrowable, int pNumLines) throws IllegalArgumentException {
        if (null == pThrowable) {
            throw new IllegalArgumentException(NO_THROWABLE_OBJECT);
        }
        String stackString = getStackTraceAsString(pThrowable);
        StringTokenizer tokenizer = new StringTokenizer(stackString, getSystemLineSeparator());
        if (tokenizer.countTokens() < pNumLines) {
            return stackString;
        }
        StringBuilder stackStringBuilder = new StringBuilder();
        for (int i = 0; i < pNumLines; i++) {
            stackStringBuilder.append(tokenizer.nextToken());
            stackStringBuilder.append(getSystemLineSeparator());
        }
        return stackStringBuilder.toString();
    }

    /**
     * Get the system file separator. It gets this by getting
     * the system property value of the file separator.
     * There are different representations for file separation
     * on different platforms (i.e UNIX=/, NT=\, ...)
     * @return A string representing the file separator
     */
    public static String getSystemFileSeparator() {
        return System.getProperty(FILE_SEPARATOR_PROPERTY_NAME);
    }

    /**
     * Get the system line separator. It gets this by getting
     * the system property value of the line separator.
     * There are different representations for line separation
     * on different platforms (i.e UNIX=\n, NT=\r\n, ...)
     * @return A string representing the line separator
     */
    public static String getSystemLineSeparator() {
        return System.getProperty(LINE_SEPARATOR_PROPERTY_NAME);
    }

    /**
     * Computes a CRC 32 for the given buffer
     * @param pBytes The bytes array to compute the checksum
     * @return The checksum
     * @throws IllegalArgumentException
     */
    public static long computeCRC(byte[] pBytes) throws IllegalArgumentException {
        if (null == pBytes) {
            throw new IllegalArgumentException(NO_BYTE_ARRAY);
        }
        CRC32 checksumObj = new CRC32();
        checksumObj.update(pBytes);
        return checksumObj.getValue();
    }

    public static Object getRandomElementFromList(List pElements) {
        int numberOfElements = pElements.size();
        int randomPos = (int) Math.round(Math.random() * (float) numberOfElements);
        if (randomPos == numberOfElements) {
            randomPos--;
        }
        return pElements.get(randomPos);
    }

    /**
     * Deletes the trailing zeroes from a string. I does not check if the string is a number. 
     * @param pText A string without trailing zeros
     */
    public static String cleanTrailingZeros(String pText) {
        pText = pText.trim();
        StringBuilder retVal = new StringBuilder();
        boolean trimmingZeros = true;
        char[] charArrayFromText = pText.toCharArray();
        for (int i = 0; i < charArrayFromText.length; i++) {
            if (trimmingZeros) {
                if (!(charArrayFromText[i] == '0')) {
                    retVal.append(charArrayFromText[i]);
                    trimmingZeros = false;
                }
            } else {
                retVal.append(charArrayFromText[i]);
            }
        }
        return retVal.toString();
    }

    /**
     * Replaces in each one of the elements of the list a string for another one.
     * 
     * @param pList The list to work on
     * @param 
     * @param 
     * @return The cleaned list
     * @throws Exception
     */
    public static List<String> replaceFromListElements(List<String> pList, String pToFind, String pToReplace) throws Exception {
        if (null == pList || pList.size() == 0) {
            return pList;
        }
        List<String> itemList = new ArrayList<String>();
        for (int i = 0; i < pList.size(); i++) {
            String item = pList.get(i).replaceAll(pToFind, pToReplace);
            if (null != item && !item.trim().isEmpty()) {
                itemList.add(item);
            }
        }
        return itemList;
    }

    /**
     * Cleans each one of the elements of the list
     * 
     * @param pList The list to clean
     * @return The cleaned list
     * @throws Exception
     */
    public static List<String> cleanListElements(List<String> pList) throws Exception {
        if (null == pList || pList.size() == 0) {
            return pList;
        }
        List<String> itemList = new ArrayList<String>();
        for (int i = 0; i < pList.size(); i++) {
            String item = AlgorithmUtils.cleanString(pList.get(i));
            if (null != item && !item.trim().isEmpty()) {
                itemList.add(item);
            }
        }
        return itemList;
    }

    /**
     * Deduplicates a list of strings, cleans and sorts it.
     * 
     * @param pList The original list
     * @return The processed list
     */
    public static List<String> deduplicateAndSortList(List<String> pList) {
        List<String> countryList = new ArrayList<String>();
        HashSet<String> countrySet = new HashSet<String>();
        for (int i = 0; i < pList.size(); i++) {
            if (null != pList.get(i) && !pList.get(i).isEmpty()) {
                countrySet.add(pList.get(i));
            }
        }
        countryList.addAll(countrySet);
        Collections.sort(countryList);
        return countryList;
    }
}
