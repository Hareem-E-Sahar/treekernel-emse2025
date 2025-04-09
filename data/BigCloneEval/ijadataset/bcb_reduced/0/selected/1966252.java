package uebung03.ml.aufgabe02;

import java.util.Scanner;

public class BinarySearch {

    public static int search(String[] array, String token, int start, int end) {
        if (start >= end) return 0;
        int midPos = (start + end) / 2;
        int compRes = token.compareTo(array[midPos]);
        if (compRes == 0) {
            return midPos + 1;
        } else if (compRes < 0) {
            return search(array, token, start, midPos);
        } else {
            return search(array, token, midPos + 1, end);
        }
    }

    public static void main(String[] args) {
        String[] array = new String[] { "Alge", "Ding", "Lang", "Politik", "Spiel", "Text", "Welt", "Zimmer" };
        System.out.println("Lang: " + search(array, "Lang", 0, array.length));
        System.out.println("Welt: " + search(array, "Welt", 0, array.length));
        System.out.println("Politik: " + search(array, "Politik", 0, array.length));
        System.out.print("Suchstring : ");
        String string = new Scanner(System.in).nextLine();
        System.out.println(search(array, string, 0, array.length));
        try {
            for (int i = 0; i < array.length; ++i) {
                if (search(array, array[i], 0, array.length) != i + 1) {
                    throw new AssertionError("not working: " + search(array, array[i], 0, array.length) + "<>" + (i + 1));
                }
            }
        } catch (Exception ex) {
            throw new AssertionError();
        }
    }
}
