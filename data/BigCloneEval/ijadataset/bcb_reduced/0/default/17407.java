import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static boolean isPalindrome(String word) {
        int left = 0;
        int right = word.length() - 1;
        while (left < right) {
            if (word.charAt(left) != word.charAt(right)) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }

    public static boolean isMirrorString(String word, Hashtable table) {
        int left = 0;
        int right = word.length() - 1;
        while (left <= right) {
            if (word.charAt(left) != table.get(word.charAt(right))) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }

    public static void main(String[] args) {
        Hashtable nueva = new Hashtable();
        nueva.put('A', 'A');
        nueva.put('E', '3');
        nueva.put('H', 'H');
        nueva.put('I', 'I');
        nueva.put('J', 'L');
        nueva.put('L', 'J');
        nueva.put('M', 'M');
        nueva.put('O', 'O');
        nueva.put('S', '2');
        nueva.put('T', 'T');
        nueva.put('U', 'U');
        nueva.put('V', 'V');
        nueva.put('W', 'W');
        nueva.put('X', 'X');
        nueva.put('Y', 'Y');
        nueva.put('Z', '5');
        nueva.put('1', '1');
        nueva.put('2', 'S');
        nueva.put('3', 'E');
        nueva.put('5', 'Z');
        nueva.put('8', '8');
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line = "";
        boolean palin;
        boolean mirror;
        try {
            while ((line = br.readLine()) != null) {
                palin = isPalindrome(line);
                mirror = isMirrorString(line, nueva);
                if (!palin && !mirror) {
                    System.out.println(line + " -- is not a palindrome.\n");
                }
                if (!palin && mirror) {
                    System.out.println(line + " -- is a mirrored string.\n");
                }
                if (palin && !mirror) {
                    System.out.println(line + " -- is a regular palindrome.\n");
                }
                if (palin && mirror) {
                    System.out.println(line + " -- is a mirrored palindrome.\n");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
