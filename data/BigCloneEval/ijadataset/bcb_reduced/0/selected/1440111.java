package org.oauth4j.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.oauth4j.jaxb.OAuth4JError;
import com.sun.jersey.api.client.ClientResponse;

public class OAuth4JUtil {

    public static boolean hasErrorResponse(ClientResponse response) {
        int statusCode = response.getStatus();
        switch(statusCode / 100) {
            case 1:
                return false;
            case 2:
                return false;
            case 3:
                return false;
            case 4:
                return true;
            case 5:
                return true;
            default:
                return false;
        }
    }

    public static OAuth4JError getError(ClientResponse response) {
        return response.getEntity(OAuth4JError.class);
    }

    public static String[] stringSplitter(String string, String regex) {
        String[] info = new String[] {};
        if (string != null && string.indexOf(regex) > -1) {
            info = string.split(regex);
        }
        return info;
    }

    public static boolean hasText(String string) {
        return string != null && string.trim().length() > 0;
    }

    public static <T> T createInstance(Class<? extends T> clazz) throws OAuth4JException {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            throw new OAuth4JException("Unable to instantiate the class... " + clazz + ", does this class have a public default constructor?");
        } catch (IllegalAccessException e) {
            throw new OAuth4JException("Unable to instantiate the class... " + clazz + ", does this class public a default constructor?");
        }
    }

    public static Map<String, String> convertToKeyValue(String... strings) {
        Map<String, String> replacements = new HashMap<String, String>();
        if (strings == null) {
            return replacements;
        }
        for (int count = 0; count < strings.length; count++) {
            replacements.put(String.valueOf(count), strings[count]);
        }
        return replacements;
    }

    public static String replaceTokens(String text, Map<String, String> replacements) {
        Pattern pattern = Pattern.compile("\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(text);
        StringBuilder builder = new StringBuilder();
        int i = 0;
        while (matcher.find()) {
            String replacement = replacements.get(matcher.group(1));
            builder.append(text.substring(i, matcher.start()));
            if (replacement == null) builder.append(matcher.group(0)); else builder.append(replacement);
            i = matcher.end();
        }
        builder.append(text.substring(i, text.length()));
        return builder.toString();
    }
}
