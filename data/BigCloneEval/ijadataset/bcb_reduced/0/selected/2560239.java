package cesynch.crystal.synchronizer;

import java.security.SecureRandom;
import java.util.Random;

/**
 * <p>Randomly generated password.</p>
 * <p>The length of the generated password will be determined at
 *  random. It will be no shorter than the minimum default and
 * no longer than maximum default.
 * 
 * @author <a href="http://www.obviex.com/Samples/Password.aspx">http://www.obviex.com/Samples/Password.aspx</a>
 */
public class PasswordGenerator {

    private static int DEFAULT_MIN_PASSWORD_LENGTH = 8;

    private static int DEFAULT_MAX_PASSWORD_LENGTH = 8;

    private static final String PASSWORD_CHARS_LCASE = "abcdefgijkmnopqrstwxyz";

    private static final String PASSWORD_CHARS_UCASE = "ABCDEFGHJKLMNPQRSTWXYZ";

    private static final String PASSWORD_CHARS_NUMERIC = "23456789";

    private static final String PASSWORD_CHARS_SPECIAL = "@#$";

    private static byte[] randomBytes = new byte[4];

    private static int seed;

    private static Random random = new Random(seed);

    private static boolean isInit;

    private static void init() throws Exception {
        if (!isInit) {
            SecureRandom.getInstance("SHA1PRNG").nextBytes(randomBytes);
            seed = (randomBytes[0] & 0x7f) << 24 | randomBytes[1] << 16 | randomBytes[2] << 8 | randomBytes[3];
            isInit = true;
        }
    }

    /**
     * 
     * @return
     */
    public static String generate() throws Exception {
        return generate(DEFAULT_MIN_PASSWORD_LENGTH, DEFAULT_MAX_PASSWORD_LENGTH);
    }

    public static String generate(int length) throws Exception {
        return generate(length, length);
    }

    public static String generate(int minLength, int maxLength) throws Exception {
        PasswordGenerator.init();
        if (minLength <= 0 || maxLength <= 0 || minLength > maxLength) return null;
        char[][] charGroups = new char[][] { PASSWORD_CHARS_LCASE.toCharArray(), PASSWORD_CHARS_UCASE.toCharArray(), PASSWORD_CHARS_NUMERIC.toCharArray(), PASSWORD_CHARS_SPECIAL.toCharArray() };
        int[] charsLeftInGroup = new int[charGroups.length];
        for (int i = 0; i < charsLeftInGroup.length; i++) charsLeftInGroup[i] = charGroups[i].length;
        int[] leftGroupsOrder = new int[charGroups.length];
        for (int i = 0; i < leftGroupsOrder.length; i++) leftGroupsOrder[i] = i;
        char[] password = null;
        if (minLength < maxLength) password = new char[random.nextInt(maxLength - minLength) + minLength]; else password = new char[minLength];
        int nextCharIdx;
        int nextGroupIdx;
        int nextLeftGroupsOrderIdx;
        int lastCharIdx;
        int lastLeftGroupsOrderIdx = leftGroupsOrder.length - 1;
        for (int i = 0; i < password.length; i++) {
            if (lastLeftGroupsOrderIdx == 0) nextLeftGroupsOrderIdx = 0; else nextLeftGroupsOrderIdx = random.nextInt(lastLeftGroupsOrderIdx);
            nextGroupIdx = leftGroupsOrder[nextLeftGroupsOrderIdx];
            lastCharIdx = charsLeftInGroup[nextGroupIdx] - 1;
            if (lastCharIdx == 0) nextCharIdx = 0; else nextCharIdx = random.nextInt(lastCharIdx + 1);
            password[i] = charGroups[nextGroupIdx][nextCharIdx];
            if (lastCharIdx == 0) charsLeftInGroup[nextGroupIdx] = charGroups[nextGroupIdx].length; else {
                if (lastCharIdx != nextCharIdx) {
                    char temp = charGroups[nextGroupIdx][lastCharIdx];
                    charGroups[nextGroupIdx][lastCharIdx] = charGroups[nextGroupIdx][nextCharIdx];
                    charGroups[nextGroupIdx][nextCharIdx] = temp;
                }
                charsLeftInGroup[nextGroupIdx]--;
            }
            if (lastLeftGroupsOrderIdx == 0) lastLeftGroupsOrderIdx = leftGroupsOrder.length - 1; else {
                if (lastLeftGroupsOrderIdx != nextLeftGroupsOrderIdx) {
                    int temp = leftGroupsOrder[lastLeftGroupsOrderIdx];
                    leftGroupsOrder[lastLeftGroupsOrderIdx] = leftGroupsOrder[nextLeftGroupsOrderIdx];
                    leftGroupsOrder[nextLeftGroupsOrderIdx] = temp;
                }
                lastLeftGroupsOrderIdx--;
            }
        }
        return new String(password);
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 1000; i++) {
            System.out.println(PasswordGenerator.generate());
        }
    }
}
