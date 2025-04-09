package com.llq.util;

import java.util.ArrayList;
import java.util.List;

public class LlqUtil {

    public static String removeLast(List<String> list) {
        if (list.isEmpty()) {
            return "";
        }
        return list.remove(list.size() - 1);
    }

    public static List<String> split(String string) {
        List<String> result = new ArrayList<String>();
        StringBuilder word = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char ch = string.charAt(i);
            if (ch != ' ') {
                word.append(ch);
            } else {
                if (word.length() > 0) {
                    result.add(word.toString());
                    word = new StringBuilder();
                }
            }
        }
        if (word.length() > 0) result.add(word.toString());
        return result;
    }

    public static int countChars(String string, char ch) {
        int count;
        int i = 0;
        for (i = 0, count = 0; i < string.length(); i++) {
            if (string.charAt(i) == ch) {
                count++;
            }
        }
        return count;
    }

    public static boolean isPalindrome(String string) {
        int limit = string.length() / 2;
        if (limit == 0) {
            return true;
        }
        for (int forward = 0, backward = string.length() - 1; forward < limit; forward++, backward--) {
            if (string.charAt(forward) != string.charAt(backward)) {
                return false;
            }
        }
        return true;
    }

    public static String endTrim(String string) {
        int i = string.length() - 1;
        while (i >= 0) {
            if (string.charAt(i) != ' ' && string.charAt(i) != '	') {
                break;
            }
            i--;
        }
        return string.substring(0, i + 1);
    }
}
