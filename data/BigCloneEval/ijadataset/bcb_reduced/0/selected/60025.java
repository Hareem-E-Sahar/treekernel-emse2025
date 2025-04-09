package uk.org.ogsadai.tools;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.org.ogsadai.config.Key;
import uk.org.ogsadai.config.KeyValueProperties;
import uk.org.ogsadai.config.KeyValueUnknownException;
import uk.org.ogsadai.exception.ErrorID;

/**
 * Editor utilities.
 *
 * @author The OGSA-DAI Project Team
 */
public class EditorUtils {

    /** Copyright statement. */
    private static final String COPYRIGHT_NOTICE = "Copyright (c) The University of Edinburgh, 2007-2010.";

    /**
     * Invoke by reflection a method that takes 0 or more string arguments.
     * 
     * @param object
     *     Object.
     * @param method
     *     Method name.
     * @param arguments
     *     Arguments.
     * @throws ConfigEditorException
     *     If there is a problem in the invocation.
     */
    public static void invokeStringMethod(Object object, String method, String[] arguments) throws ConfigEditorException {
        Class<String>[] argTypes = new Class[arguments.length];
        for (int i = 0; i < argTypes.length; i++) {
            argTypes[i] = String.class;
        }
        try {
            Method methodProxy = object.getClass().getMethod(method, argTypes);
            methodProxy.invoke(object, (Object[]) arguments);
        } catch (NoSuchMethodException e) {
            throw new ConfigEditorException(ErrorID.CONFIG_EDITOR_SYNTAX_ERROR, new String[] { method });
        } catch (InvocationTargetException e) {
            throw new ConfigEditorException(ErrorID.CONFIG_EDITOR_EXECUTION_ERROR, new String[] { method }, e.getCause());
        } catch (Exception e) {
            throw new ConfigEditorException(ErrorID.CONFIG_EDITOR_EXECUTION_ERROR, new String[] { method }, e);
        }
    }

    /**
     * Print an exception chain, displaying the localized messages.
     * 
     * @param e
     *            Exception.
     */
    public static void printException(Throwable e) {
        Throwable ex = e;
        while (ex != null) {
            String msg = ex.getLocalizedMessage();
            if (msg == null) {
                msg = ex.getMessage();
            }
            if (msg == null) {
                msg = ex.toString();
            }
            System.out.println(ex.getLocalizedMessage());
            ex = ex.getCause();
        }
    }

    /**
     * Tokenize the given string into a list of strings. Space is used as
     * the delimiter. If there are quoted sub-strings then these are returned
     * as single tokens.
     * 
     * @param str
     *     String.
     * @return tokens.
     */
    public static List<String> tokenizeRegexp(String str) {
        Pattern pattern = Pattern.compile("\".*?\"");
        Matcher matcher = pattern.matcher(str);
        List<String> tokens = new ArrayList<String>();
        int start = 0;
        while (matcher.find()) {
            tokens.addAll(Arrays.asList(str.substring(start, matcher.start()).trim().split(" ")));
            tokens.add(matcher.group().trim());
            start = matcher.end();
        }
        tokens.addAll(Arrays.asList(str.substring(start).trim().split(" ")));
        return tokens;
    }

    /**
     * Tokenize the given string into a list of strings. Space is used as
     * the delimiter. If there are quoted sub-strings then these are returned
     * as single tokens.
     * 
     * @param str
     *     String.
     * @return tokens.
     * @throws IOException
     *     If any problems arise.
     */
    public static List<String> tokenize(String str) throws IOException {
        StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(str));
        List<String> tokens = new ArrayList<String>();
        int tokenType;
        tokenizer.resetSyntax();
        tokenizer.wordChars('_', '_');
        tokenizer.wordChars(',', ',');
        tokenizer.wordChars('~', '~');
        tokenizer.wordChars('@', '@');
        tokenizer.wordChars(':', ':');
        tokenizer.wordChars(';', ';');
        tokenizer.wordChars('.', '.');
        tokenizer.wordChars('/', '/');
        tokenizer.wordChars('?', '?');
        tokenizer.wordChars('!', '!');
        tokenizer.wordChars('+', '+');
        tokenizer.wordChars('-', '-');
        tokenizer.wordChars('=', '=');
        tokenizer.wordChars('0', '9');
        tokenizer.wordChars('a', 'z');
        tokenizer.wordChars('A', 'Z');
        tokenizer.quoteChar('"');
        tokenizer.quoteChar('\'');
        while ((tokenType = tokenizer.nextToken()) != StreamTokenizer.TT_EOF) {
            if (tokenType == StreamTokenizer.TT_NUMBER) {
                tokens.add(Double.toString(tokenizer.nval));
            } else if (tokenType == StreamTokenizer.TT_WORD) {
                tokens.add(tokenizer.sval);
            } else if (tokenType == '\"') {
                tokens.add(tokenizer.sval);
            }
        }
        return tokens;
    }

    /**
     * List key-value properties properties.
     * 
     * @param properties
     *     Key-value properties.
     */
    public static void listKeyValues(KeyValueProperties properties) {
        Key[] keys = properties.getKeys();
        for (int i = 0; i < keys.length; i++) {
            try {
                Object value = properties.get(keys[i]);
                System.out.println(keys[i] + "=" + value);
            } catch (KeyValueUnknownException e) {
            }
        }
    }

    /**
     * List configuration property keys only.
     * 
     * @param properties
     *     Key-value properties.
     */
    public static void listKeys(KeyValueProperties properties) {
        Key[] keys = properties.getKeys();
        for (int i = 0; i < keys.length; i++) {
            System.out.println(keys[i]);
        }
    }

    /**
     * Get a configuration property.
     * 
     * @param properties
     *     Key-value properties.
     * @param keyString
     *     Key as a string.
     * @throws KeyValueUnknownException
     *     If the key is not known to the configuration properties.
     */
    public static void getKeyValue(KeyValueProperties properties, String keyString) throws KeyValueUnknownException {
        Key key = new Key(keyString);
        System.out.println(properties.get(key));
    }

    /**
     * Delete a configuration property.
     *
     * @param properties
     *     Key-value properties.
     * @param keyString
     *     Key as a string.
     * @throws KeyValueUnknownException
     *     If the key is not known to the configuration properties.
     */
    public static void deleteKeyValue(KeyValueProperties properties, String keyString) throws KeyValueUnknownException {
        Key key = new Key(keyString);
        properties.clear(key);
    }

    /**
     * Add a configuration property.
     * 
     * @param properties
     *     Key-value properties.
     * @param keyString
     *     Key as a string.
     * @param value
     *     Value.
     */
    public static void addKeyValue(KeyValueProperties properties, String keyString, String value) {
        Key key = new Key(keyString);
        properties.put(key, value);
    }

    /**
     * Purge configuration properties.
     * 
     * @param properties
     *     Key-value properties.
     */
    public static void purgeKeyValues(KeyValueProperties properties) {
        Key[] keys = properties.getKeys();
        for (int i = 0; i < keys.length; i++) {
            try {
                properties.clear(keys[i]);
            } catch (KeyValueUnknownException e) {
            }
        }
    }

    /**
     * Get the ith argument from an array. If it is not there
     * (i.e. i is outwith the bounds of the array) then throw
     * an exception with the given error message.
     *
     * @param arguments
     *     Array of arguments.
     * @param index
     *     Index of requested argument.
     * @param error
     *     Error message.
     * @return argument.
     * @throws IllegalArgumentException
     *     If the index is out of bounds. The exception will
     *     contain the given error message.
     */
    public static String getArgument(String[] arguments, int index, String error) {
        try {
            return arguments[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(error);
        }
    }
}
