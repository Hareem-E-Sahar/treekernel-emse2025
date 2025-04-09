package sis.report;

import java.util.*;
import sis.studentinfo.*;

public class Loop {

    public static int countChars(String input, char ch) {
        int count;
        int i;
        for (i = 0, count = 0; i < input.length(); i++) if (input.charAt(i) == ch) count++;
        return count;
    }

    public static boolean isPalindrome(String string) {
        if (string.length() == 0) return true;
        int limit = string.length() / 2;
        for (int forward = 0, backward = string.length() - 1; forward < limit; forward++) if (string.charAt(forward) != string.charAt(backward)) return false;
        return true;
    }

    public int fib(int x) {
        if (x == 0) return 0;
        if (x == 1) return 1;
        return fib(x - 1) + fib(x - 2);
    }

    String sequenceUsindDo(int start, int stop) {
        StringBuilder builder = new StringBuilder();
        int i = start;
        do {
            if (i > start) builder.append(',');
            builder.append(i);
        } while (++i <= stop);
        return builder.toString();
    }

    String sequenceUsindFor(int start, int stop) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i <= stop; i++) {
            if (i > start) builder.append(',');
            builder.append(i);
        }
        return builder.toString();
    }

    String sequenceUsindWhile(int start, int stop) {
        StringBuilder builder = new StringBuilder();
        int i = start;
        while (i <= stop) {
            if (i > start) builder.append(',');
            builder.append(i);
            i++;
        }
        return builder.toString();
    }

    public String endTrim(String source) {
        int i = source.length();
        if (i != 0) {
            while (i >= 0) {
                if (source.charAt(i - 1) == ' ') break;
            }
            return source.substring(0, i - 1);
        } else {
            return " ";
        }
    }
}
