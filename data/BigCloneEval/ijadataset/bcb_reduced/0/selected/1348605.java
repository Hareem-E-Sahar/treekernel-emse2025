package net.euler.project.problems.four;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Solution {

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        final List<Integer> temp = new ArrayList<Integer>();
        for (int i = 999; i > 1; i--) for (int j = 999; j > 1; j--) {
            final int k = i * j;
            if (isPalindrome(String.valueOf(k))) temp.add(k);
        }
        Collections.sort(temp);
        System.out.println(temp.get(temp.size() - 1));
    }

    private static final boolean isPalindrome(final String s) {
        String opposite = "";
        for (int i = s.length() - 1; i >= 0; i--) opposite = opposite + s.charAt(i);
        return s.equals(opposite);
    }
}
