package org.bug4j.server.processor;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The StackPathHashCalculator is used to de-duplicate bugs based on the method calls but without line numbers.
 * This allows to match two hits reported by two slightly different versions of the application where one of the
 * method calls might be off by a few lines.
 */
public class StackPathHashCalculator {

    private static final Pattern STACK_PATTERN = Pattern.compile("\tat ([^()]*)\\((.*)\\)");

    private StackPathHashCalculator() {
    }

    public static String analyze(List<String> stackLines) {
        final MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        final Iterator<String> iterator = stackLines.iterator();
        if (!iterator.hasNext()) {
            return null;
        }
        try {
            final String messageLine = iterator.next();
            final String exceptionClass = getExceptionClass(messageLine);
            messageDigest.update(exceptionClass.getBytes("UTF-8"));
            analyze(exceptionClass, iterator, messageDigest);
            final byte[] bytes = messageDigest.digest();
            final BigInteger bigInt = new BigInteger(1, bytes);
            final String ret = bigInt.toString(36);
            return ret;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    protected static void analyze(String exceptionClass, Iterator<String> iterator, MessageDigest messageDigest) throws UnsupportedEncodingException {
        while (iterator.hasNext()) {
            final String stackLine = iterator.next();
            if (stackLine.startsWith("Caused by: ")) {
                final String substring = stackLine.substring("Caused by: ".length());
                exceptionClass = getExceptionClass(substring);
                messageDigest.update(exceptionClass.getBytes("UTF-8"));
            } else {
                final Matcher matcher = STACK_PATTERN.matcher(stackLine);
                if (matcher.matches()) {
                    final String methodCall = matcher.group(1);
                    if (!isSyntheticProxyMethod(methodCall)) {
                        final String cleaned = methodCall.replaceAll("[0-9]", "");
                        messageDigest.update(cleaned.getBytes("UTF-8"));
                    }
                }
            }
        }
    }

    private static boolean isSyntheticProxyMethod(String methodCall) {
        final String[] prefixes = { "sun.reflect.", "$Proxy", "org.jboss.aop.advice.org.jboss.ejb3.interceptors.aop.InvocationContextInterceptor" };
        for (String prefix : prefixes) {
            if (methodCall.startsWith(prefix)) {
                return true;
            }
        }
        if (methodCall.contains("_$$_javassist_")) {
            return true;
        }
        return false;
    }

    static String getExceptionClass(String messageLine) {
        final String ret;
        final int pos = messageLine.indexOf(':');
        if (pos < 0) {
            ret = messageLine;
        } else {
            ret = messageLine.substring(0, pos);
        }
        return ret;
    }
}
