package org.log5j.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.log5j.Format;
import org.log5j.Logger;

/**
 * The <code>PatternFormat</code> class allows a developer to customise the
 * output of a logger.
 * <p>
 * The conversion pattern for a <code>PatternFormat</code> is specified by the
 * configuration property <code>&lt;logger-name&gt;.format.pattern</code>, or
 * <code>&lt;logger-name&gt;.writer.format.pattern</code> for writer-specific
 * formats.
 * <p>
 * Configuration is very similar to the <a
 * href="http://logging.apache.org/log4j/">log4j</a> <code>PatternLayout</code>
 * class although a couple of options are missing. Log5j does not yet provide
 * mapped diagnostic contexts or nested diagnostic contexts.
 * <p>
 * Each conversion specifier starts with a percent sign (%) and is followed by
 * optional format modifiers and a conversion character. Without a preceding
 * percent sign, any character will be printed literally.
 * <p>
 * <table border="1">
 * <tr>
 * <th align="center">Conversion Character</th>
 * <th>Effect</th>
 * </tr>
 * <tr>
 * <td align="center"><b>%</b></td>
 * <td>Print a literal percent sign (%) by using two percent signs (%%) as an
 * escape sequence.</td>
 * </tr>
 * <tr>
 * <td align="center"><b>c</b></td>
 * <td>Print the name of the logger. This conversion character can be followed
 * by an optional precision specifier. The precision specifier is a decimal
 * constant inside curly braces. For example,
 * 
 * <pre>
 *    %c{3}
 * </pre>
 * 
 * Where the name of a logger is made up of a words separated by periods (.)
 * this conversion specifier will print the three rightmost words. That is, if
 * the logger name is, "a.b.c.d", this conversion specifier will print, "b.c.d".
 * </td>
 * </tr>
 * <tr>
 * <td align="center"><b>d</b></td>
 * <td>Print the current date and/or time. This conversion character can be
 * followed by an optional date format pattern enclosed in curly braces. Log5j
 * uses Joda's {@link DateTimeFormat} class to format the date, and the format
 * pattern should match the specification for that class.</td>
 * </tr>
 * <tr>
 * <td align="center"><b>n</b></td>
 * <td>Print (an) end-of-line character(s). The character(s) printed are those
 * returned by System.getProperty("line.separator").</td>
 * </tr>
 * <tr>
 * <td align="center"><b>p</b></td>
 * <td>Print the name of the logging level.</td>
 * </tr>
 * <tr>
 * <td align="center"><b>t</b></td>
 * <td>Print the name of the current thread.</td>
 * </tr>
 * <tr>
 * <td align="center"><b>m</b></td>
 * <td>Print the log message.</td>
 * </tr>
 * <tr>
 * <td align="center"><b>C</b></td>
 * <td>Print the fully qualified class name of the object issuing the logging
 * request. This conversion character can be followed by an optional precision
 * specifier. The precision specifier is a decimal constant inside curly braces.
 * For example,
 * 
 * <pre>
 *    %C{3}
 * </pre>
 * 
 * This conversion specifier will print the two rightmost package names and the
 * class name. That is, if the object's class name is, "a.b.c.MyClass", this
 * conversion specifier will print, "b.c.MyClass".
 * <p>
 * Generating this information is very slow, do not use this feature where
 * performance is an issue.
 * </td>
 * </tr>
 * <tr>
 * <td align="center"><b>F</b></td>
 * <td>Print out the filename of the class where the logging call was issued.
 * <p>
 * Generating this information is extremely slow, do not use this feature where
 * performance is an issue.
 * </td>
 * </tr>
 * <tr>
 * <td align="center"><b>L</b></td>
 * <td>Print out the line number in the filename of the class where the logging
 * call was issued.
 * <p>
 * Generating this information is extremely slow, do not use this feature where
 * performance is an issue.
 * </td>
 * </tr>
 * <tr>
 * <td align="center"><b>M</b></td>
 * <td>Print out the name of the method where the logging call was issued.
 * <p>
 * Generating this information is very slow, do not use this feature where
 * performance is an issue.
 * </td>
 * </tr>
 * <tr>
 * <td align="center"><b>l</b></td>
 * <td>Print out all location information from the stack trace where the logging
 * call was issued.
 * <p>
 * Generating this information is extremely slow, do not use this feature where
 * performance is an issue.
 * </td>
 * </tr>
 * </table>
 * <p>
 * <b>Format modifiers</b> affect the minimum and maximum width, and
 * justification of each field in the pattern. Without format modifiers, each
 * field is printed as is.
 * <p>
 * A minus character (-) at the beginning of the format modifier causes the the
 * field to be left-justified. By default fields are right-justified.
 * <p>
 * A decimal number immediately after the minus character (or after the percent
 * sign if there is no minus character) defines the minimum field width.
 * <p>
 * The maximum field width is also defined by a decimal number. It must be
 * preceded by a period (.) which must follow the left justification flag and
 * minimum field width if these exist. If a field must be truncated, the
 * leftmost end will be truncated and the rightmost end preserved.
 * <p>
 * Some examples follow;
 * <p>
 * <table border="1">
 * <tr>
 * <td><b>%10m</b></td>
 * <td>The log message will fill at least 10 spaces, and be right-justified if
 * necessary.</td>
 * </tr>
 * <tr>
 * <td><b>%-15m</b></td>
 * <td>The log message will fill at least 15 spaces, and be left-justified if
 * necessary.</td>
 * </tr>
 * <tr>
 * <td><b>%.20m</b></td>
 * <td>The log message will fill no more than 20 spaces.</td>
 * </tr>
 * <tr>
 * <td><b>%-10.20m</b></td>
 * <td>The log message will fill at least 10 spaces but no more than 20, It will
 * be left-justified if necessary.</td>
 * </tr>
 * </table>
 * <p>
 * <code>PatternFormat</code> also recognises the configuration properties
 * <ul>
 * <li><code>arrayDepth</code>. An integer value that specifies the number of
 * levels of nesting of arrays to recurse through when printing the contents of
 * arrays.</li>
 * <li><code>traceThrowable</code>. if set to true, any {@link Throwable} object
 * will have its full stack trace logged. The rules for setting the value of
 * <code>traceThrowable</code> are those of the {@link Boolean#valueOf(String)}
 * method.</li>
 * </ul>
 * 
 * @author Bruce Ashton
 * @date 2007-07-16
 */
public final class PatternFormat implements Format {

    private abstract static class FormatElement {

        protected static final String justify(final String message, int maxLength, final int minLength, final boolean leftJustify) {
            final int length = message.length();
            if (maxLength > length) {
                maxLength = length;
            }
            if (maxLength < minLength) {
                maxLength = minLength;
            }
            if (length == maxLength) {
                return message;
            }
            final char[] tempArr = new char[maxLength];
            if (length > maxLength) {
                message.getChars(length - maxLength, length, tempArr, 0);
            } else if (leftJustify) {
                message.getChars(0, length, tempArr, 0);
                Arrays.fill(tempArr, length, maxLength, ' ');
            } else {
                int idx = maxLength - length;
                message.getChars(0, length, tempArr, idx);
                Arrays.fill(tempArr, 0, idx, ' ');
            }
            return new String(tempArr);
        }

        abstract void formatElement(StringBuilder message, String logName, String levelName, Object... object);
    }

    private static final int DEFAULT_ARRAY_DEPTH = 4;

    private static final char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

    private static final String EOL = System.getProperty("line.separator");

    private static final String LOGGER_CLASS_NAME = Logger.class.getName();

    private final int arrayDepth;

    private final boolean traceThrowable;

    private final FormatElement[] pattern;

    /**
     * The standard constructor takes as arguments the properties that may
     * affect how <code>PatternFormat</code> formats log lines.
     * <p>
     * <code>PatternFormat</code> recognises the following properties;
     * <ul>
     * <li><code>arrayDepth</code>: The number of levels of nesting of arrays to
     * recurse through when printing the contents of arrays</li>
     * <li><code>traceThrowable</code>: Print out the entire stack trace of any
     * <code>Throwable</code> if true</li>
     * <li><code>pattern</code>: The format pattern for this
     * <code>PatternFormat</code></li>
     * </ul>
     * </p>
     * 
     * @param properties the properties of this format object.
     */
    public PatternFormat(final Properties properties) {
        final List<FormatElement> patternList = new ArrayList<FormatElement>();
        char[] patternChars = new char[0];
        int tmpArrayDepth = DEFAULT_ARRAY_DEPTH;
        String patternStr = properties.getProperty("pattern");
        if (patternStr != null) {
            patternChars = patternStr.toCharArray();
        }
        try {
            tmpArrayDepth = Integer.valueOf(properties.getProperty("arrayDepth"));
        } catch (NumberFormatException e) {
        }
        this.arrayDepth = tmpArrayDepth;
        traceThrowable = Boolean.valueOf(properties.getProperty("traceThrowable"));
        final AtomicBoolean leftJustifyRef = new AtomicBoolean(false);
        final AtomicInteger maxLengthRef = new AtomicInteger(Integer.MAX_VALUE);
        final AtomicInteger minLengthRef = new AtomicInteger(0);
        final AtomicInteger precisionRef = new AtomicInteger(0);
        final StringBuilder builder = new StringBuilder();
        boolean literal = true;
        int i = 0;
        while (i < patternChars.length) {
            if (literal) {
                builder.setLength(0);
                while (patternChars[i] != '%') {
                    builder.append(patternChars[i]);
                    i++;
                }
                if (builder.length() > 0) {
                    patternList.add(new FormatElement() {

                        private final String string = builder.toString();

                        public final void formatElement(final StringBuilder message, final String logName, final String levelName, final Object... object) {
                            message.append(string);
                        }
                    });
                }
                literal = false;
            } else {
                char[] tempArr;
                int startIndex = i;
                leftJustifyRef.set(false);
                minLengthRef.set(0);
                maxLengthRef.set(Integer.MAX_VALUE);
                if (patternChars[i] == '-') {
                    leftJustifyRef.set(true);
                    startIndex++;
                    i++;
                }
                while (Arrays.binarySearch(DIGITS, patternChars[i]) >= 0) {
                    i++;
                }
                if (i > startIndex) {
                    int length = i - startIndex;
                    tempArr = new char[length];
                    System.arraycopy(patternChars, startIndex, tempArr, 0, length);
                    minLengthRef.set(Integer.parseInt(new String(tempArr)));
                }
                if (patternChars[i] == '.' && Arrays.binarySearch(DIGITS, patternChars[++i]) >= 0) {
                    startIndex = i;
                    while (Arrays.binarySearch(DIGITS, patternChars[i]) >= 0) {
                        i++;
                    }
                    int length = i - startIndex;
                    tempArr = new char[length];
                    System.arraycopy(patternChars, startIndex, tempArr, 0, length);
                    maxLengthRef.set(Integer.parseInt(new String(tempArr)));
                }
                switch(patternChars[i]) {
                    case '%':
                        patternList.add(new FormatElement() {

                            public final void formatElement(final StringBuilder message, final String logName, final String levelName, final Object... object) {
                                message.append('%');
                            }
                        });
                        break;
                    case 'c':
                        builder.setLength(0);
                        if (patternChars[i + 1] == '{') {
                            i = i + 2;
                            while (patternChars[i] != '}') {
                                builder.append(patternChars[i]);
                                i++;
                            }
                        }
                        precisionRef.set(0);
                        try {
                            precisionRef.set(Integer.parseInt(builder.toString()));
                        } catch (NumberFormatException e) {
                        }
                        if (precisionRef.get() < 1) {
                            patternList.add(new FormatElement() {

                                private final int maxLength = maxLengthRef.get();

                                private final int minLength = minLengthRef.get();

                                private final boolean leftJustify = leftJustifyRef.get();

                                public final void formatElement(final StringBuilder message, final String logName, final String levelName, final Object... object) {
                                    message.append(justify(logName, maxLength, minLength, leftJustify));
                                }
                            });
                        } else {
                            patternList.add(new FormatElement() {

                                private final int maxLength = maxLengthRef.get();

                                private final int minLength = minLengthRef.get();

                                private final int precision = precisionRef.get();

                                private final boolean leftJustify = leftJustifyRef.get();

                                public final void formatElement(final StringBuilder message, final String logName, final String levelName, final Object... object) {
                                    int count = 0;
                                    int idx = 0;
                                    for (int i = logName.length() - 1; i > 0; i--) {
                                        if ('.' == logName.charAt(i)) {
                                            count++;
                                            if (count == precision) {
                                                idx = i + 1;
                                                break;
                                            }
                                        }
                                    }
                                    message.append(justify(logName.substring(idx), maxLength, minLength, leftJustify));
                                }
                            });
                        }
                        break;
                    case 'd':
                        builder.setLength(0);
                        if (patternChars[i + 1] == '{') {
                            i = i + 2;
                            while (patternChars[i] != '}') {
                                builder.append(patternChars[i]);
                                i++;
                            }
                        } else {
                            builder.append("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                        }
                        patternList.add(new FormatElement() {

                            private final DateTimeFormatter format = DateTimeFormat.forPattern(builder.toString());

                            private final int maxLength = maxLengthRef.get();

                            private final int minLength = minLengthRef.get();

                            private final boolean leftJustify = leftJustifyRef.get();

                            public final void formatElement(final StringBuilder message, final String logName, final String levelName, final Object... object) {
                                message.append(justify(format.print(System.currentTimeMillis()), maxLength, minLength, leftJustify));
                            }
                        });
                        break;
                    case 'n':
                        patternList.add(new FormatElement() {

                            public final void formatElement(final StringBuilder message, final String logName, final String levelName, final Object... object) {
                                message.append(EOL);
                            }
                        });
                        break;
                    case 'p':
                        patternList.add(new FormatElement() {

                            private final int maxLength = maxLengthRef.get();

                            private final int minLength = minLengthRef.get();

                            private final boolean leftJustify = leftJustifyRef.get();

                            public final void formatElement(final StringBuilder message, final String logName, final String levelName, final Object... object) {
                                message.append(justify(levelName, maxLength, minLength, leftJustify));
                            }
                        });
                        break;
                    case 't':
                        patternList.add(new FormatElement() {

                            private final int maxLength = maxLengthRef.get();

                            private final int minLength = minLengthRef.get();

                            private final boolean leftJustify = leftJustifyRef.get();

                            public final void formatElement(final StringBuilder message, final String logName, final String levelName, final Object... object) {
                                message.append(justify(Thread.currentThread().getName(), maxLength, minLength, leftJustify));
                            }
                        });
                        break;
                    case 'm':
                        patternList.add(new FormatElement() {

                            private final int maxLength = maxLengthRef.get();

                            private final int minLength = minLengthRef.get();

                            private final boolean leftJustify = leftJustifyRef.get();

                            private void append(final StringBuilder message, final int arrayDepth, final Object object) {
                                if (object == null) {
                                    message.append("null");
                                } else if (traceThrowable && object instanceof Throwable) {
                                    final Throwable throwable = (Throwable) object;
                                    message.append(throwable.getMessage());
                                    message.append(EOL);
                                    for (final StackTraceElement element : throwable.getStackTrace()) {
                                        message.append('\t');
                                        message.append(element.toString());
                                        message.append(EOL);
                                    }
                                } else if (arrayDepth > 0 && object.getClass().isArray()) {
                                    final Class componentType = object.getClass().getComponentType();
                                    message.append("{ ");
                                    if (Object.class.isAssignableFrom(componentType)) {
                                        for (final Object value : (Object[]) object) {
                                            append(message, arrayDepth - 1, value);
                                            message.append(", ");
                                        }
                                    } else {
                                        if (boolean.class == componentType) {
                                            for (final boolean value : (boolean[]) object) {
                                                append(message, 0, value);
                                                message.append(", ");
                                            }
                                        } else if (byte.class == componentType) {
                                            for (final byte value : (byte[]) object) {
                                                append(message, 0, value);
                                                message.append(", ");
                                            }
                                        } else if (char.class == componentType) {
                                            for (final char value : (char[]) object) {
                                                append(message, 0, value);
                                                message.append(", ");
                                            }
                                        } else if (short.class == componentType) {
                                            for (final short value : (short[]) object) {
                                                append(message, 0, value);
                                                message.append(", ");
                                            }
                                        } else if (int.class == componentType) {
                                            for (final int value : (int[]) object) {
                                                append(message, 0, value);
                                                message.append(", ");
                                            }
                                        } else if (long.class == componentType) {
                                            for (final long value : (long[]) object) {
                                                append(message, 0, value);
                                                message.append(", ");
                                            }
                                        } else if (float.class == componentType) {
                                            for (final float value : (float[]) object) {
                                                append(message, 0, value);
                                                message.append(", ");
                                            }
                                        } else if (double.class == componentType) {
                                            for (final double value : (double[]) object) {
                                                append(message, 0, value);
                                                message.append(", ");
                                            }
                                        }
                                    }
                                    message.setCharAt(message.length() - 2, ' ');
                                    message.setCharAt(message.length() - 1, '}');
                                } else {
                                    message.append(object.toString());
                                }
                            }

                            public final void formatElement(final StringBuilder message, final String logName, final String levelName, final Object... objects) {
                                if (objects == null) {
                                    message.append(justify("null", maxLength, minLength, leftJustify));
                                    return;
                                }
                                final StringBuilder builder = new StringBuilder();
                                int lastIndex = objects.length - 1;
                                for (int i = 0; i < objects.length; i++) {
                                    append(builder, arrayDepth, objects[i]);
                                    if (i < lastIndex) {
                                        builder.append(' ');
                                    }
                                }
                                message.append(justify(builder.toString(), maxLength, minLength, leftJustify));
                            }
                        });
                        break;
                    case 'C':
                        builder.setLength(0);
                        if (patternChars[i + 1] == '{') {
                            i = i + 2;
                            while (patternChars[i] != '}') {
                                builder.append(patternChars[i]);
                                i++;
                            }
                        }
                        precisionRef.set(0);
                        try {
                            precisionRef.set(Integer.parseInt(builder.toString()));
                        } catch (NumberFormatException e) {
                        }
                        if (precisionRef.get() < 1) {
                            patternList.add(new FormatElement() {

                                private final int maxLength = maxLengthRef.get();

                                private final int minLength = minLengthRef.get();

                                private final boolean leftJustify = leftJustifyRef.get();

                                public final void formatElement(final StringBuilder message, final String logName, final String levelName, final Object... object) {
                                    final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                                    int caller = 3;
                                    for (int i = 3; i < stackTrace.length; i++) {
                                        if (LOGGER_CLASS_NAME.equals(stackTrace[i].getClassName())) {
                                            caller = i + 1;
                                        }
                                    }
                                    if (caller >= stackTrace.length) {
                                        caller = stackTrace.length - 1;
                                    }
                                    message.append(justify(stackTrace[caller].getClassName(), maxLength, minLength, leftJustify));
                                }
                            });
                        } else {
                            patternList.add(new FormatElement() {

                                private final int maxLength = maxLengthRef.get();

                                private final int minLength = minLengthRef.get();

                                private final int precision = precisionRef.get();

                                private final boolean leftJustify = leftJustifyRef.get();

                                public final void formatElement(final StringBuilder message, final String logName, final String levelName, final Object... object) {
                                    final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                                    int caller = 3;
                                    for (int i = 3; i < stackTrace.length; i++) {
                                        if (LOGGER_CLASS_NAME.equals(stackTrace[i].getClassName())) {
                                            caller = i + 1;
                                        }
                                    }
                                    if (caller >= stackTrace.length) {
                                        caller = stackTrace.length - 1;
                                    }
                                    final String className = stackTrace[caller].getClassName();
                                    int count = 0;
                                    int idx = 0;
                                    for (int i = className.length() - 1; i > 0; i--) {
                                        if ('.' == className.charAt(i)) {
                                            count++;
                                            if (count == precision) {
                                                idx = i + 1;
                                                break;
                                            }
                                        }
                                    }
                                    message.append(justify(className.substring(idx), maxLength, minLength, leftJustify));
                                }
                            });
                        }
                        break;
                    case 'F':
                        patternList.add(new FormatElement() {

                            private final int maxLength = maxLengthRef.get();

                            private final int minLength = minLengthRef.get();

                            private final boolean leftJustify = leftJustifyRef.get();

                            public final void formatElement(final StringBuilder message, final String logName, final String levelName, final Object... object) {
                                final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                                int caller = 3;
                                for (int i = 3; i < stackTrace.length; i++) {
                                    if (LOGGER_CLASS_NAME.equals(stackTrace[i].getClassName())) {
                                        caller = i + 1;
                                    }
                                }
                                if (caller < stackTrace.length) {
                                    message.append(justify(stackTrace[caller].getFileName(), maxLength, minLength, leftJustify));
                                }
                            }
                        });
                        break;
                    case 'L':
                        patternList.add(new FormatElement() {

                            private final int maxLength = maxLengthRef.get();

                            private final int minLength = minLengthRef.get();

                            private final boolean leftJustify = leftJustifyRef.get();

                            public final void formatElement(final StringBuilder message, final String logName, final String levelName, final Object... object) {
                                final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                                int caller = 3;
                                for (int i = 3; i < stackTrace.length; i++) {
                                    if (LOGGER_CLASS_NAME.equals(stackTrace[i].getClassName())) {
                                        caller = i + 1;
                                    }
                                }
                                if (caller < stackTrace.length) {
                                    message.append(justify(Integer.toString(stackTrace[caller].getLineNumber()), maxLength, minLength, leftJustify));
                                }
                            }
                        });
                        break;
                    case 'M':
                        patternList.add(new FormatElement() {

                            private final int maxLength = maxLengthRef.get();

                            private final int minLength = minLengthRef.get();

                            private final boolean leftJustify = leftJustifyRef.get();

                            public final void formatElement(final StringBuilder message, final String logName, final String levelName, final Object... object) {
                                final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                                int caller = 3;
                                for (int i = 3; i < stackTrace.length; i++) {
                                    if (LOGGER_CLASS_NAME.equals(stackTrace[i].getClassName())) {
                                        caller = i + 1;
                                    }
                                }
                                if (caller < stackTrace.length) {
                                    message.append(justify(stackTrace[caller].getMethodName(), maxLength, minLength, leftJustify));
                                }
                            }
                        });
                        break;
                    case 'l':
                        patternList.add(new FormatElement() {

                            private final int maxLength = maxLengthRef.get();

                            private final int minLength = minLengthRef.get();

                            private final boolean leftJustify = leftJustifyRef.get();

                            public final void formatElement(final StringBuilder message, final String logName, final String levelName, final Object... object) {
                                final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                                int caller = 3;
                                for (int i = 3; i < stackTrace.length; i++) {
                                    if (LOGGER_CLASS_NAME.equals(stackTrace[i].getClassName())) {
                                        caller = i + 1;
                                    }
                                }
                                if (caller < stackTrace.length) {
                                    message.append(justify(stackTrace[caller].toString(), maxLength, minLength, leftJustify));
                                }
                            }
                        });
                }
                literal = true;
            }
            i++;
        }
        pattern = patternList.toArray(new FormatElement[patternList.size()]);
    }

    public String format(final String logName, final String levelName, final Object... objects) {
        final StringBuilder message = new StringBuilder();
        for (final FormatElement element : pattern) {
            element.formatElement(message, logName, levelName, objects);
        }
        return message.toString();
    }
}
