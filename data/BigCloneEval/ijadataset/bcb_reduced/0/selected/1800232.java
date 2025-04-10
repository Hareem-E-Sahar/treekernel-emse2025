package com.inepex.ineForm.server.util;

import java.security.MessageDigest;
import java.util.Random;
import java.util.StringTokenizer;

public class StringUtil {

    public static String getPaddedIntWithZeros(int padToDigits, int num) {
        String result = "" + num;
        while (result.length() < padToDigits) result = "0" + result;
        return result;
    }

    public static String join(String separator, String[] elements) {
        String result = "";
        int pos = 0;
        for (String e : elements) {
            if (pos > 0) result += separator + e; else result += e;
            pos++;
        }
        return result;
    }

    public static String getPart(String string, String separator, int partnr) throws Exception {
        StringTokenizer st = new StringTokenizer(string, separator);
        int nr = 0;
        while (st.hasMoreTokens() && nr < partnr) {
            st.nextToken();
            nr++;
        }
        if (!st.hasMoreTokens()) throw new Exception("Not enough token in this string.");
        return st.nextToken();
    }

    public static String getRandomNumString(int lenght) {
        String random = "";
        Random rnd = new Random(System.currentTimeMillis());
        for (int i = 0; i < lenght; i++) random += Integer.toString(rnd.nextInt(10));
        return random;
    }

    static String[] accentedAll = { "Ă€", "Ă?", "Ă‚", "Ă?", "Ă„", "Ă…", "Ă†", "Ä‚", "Ä„", "Ă‡", "Ä†", "ÄŚ", "Ĺ’", "ÄŽ", "Ä?", "Ă ", "Ăˇ", "Ă˘", "ĂŁ", "Ă¤", "ĂĄ", "Ă¦", "Ä?", "Ä…", "Ă§", "Ä‡", "ÄŤ", "Ĺ“", "ÄŹ", "Ä‘", "Ă?", "Ă‰", "ĂŠ", "Ă‹", "Ä?", "Äš", "ĆŹ", "Äž", "ĂŚ", "ĂŤ", "ĂŽ", "ĂŹ", "Ä°", "Äą", "Ä˝", "Ĺ?", "Ă¨", "Ă©", "ĂŞ", "Ă«", "Ä™", "Ä›", "É™", "Äź", "Ă¬", "Ă­", "Ă®", "ĂŻ", "Ä±", "Äş", "Äľ", "Ĺ‚", "Ă‘", "Ĺ?", "Ĺ‡", "Ă’", "Ă“", "Ă”", "Ă•", "Ă–", "Ă?", "Ĺ?", "Ĺ”", "Ĺ?", "Ĺš", "Ĺž", "Ĺ ", "Ă±", "Ĺ„", "Ĺ?", "Ă˛", "Ăł", "Ă´", "Ă¶", "Ă¸", "Ĺ‘", "Ĺ•", "Ĺ™", "Ĺ›", "Ĺź", "Ĺˇ", "Ĺ˘", "Ĺ¤", "Ă™", "Ăš", "Ă›", "Ĺ˛", "Ăś", "Ĺ®", "Ĺ°", "Ăť", "Ăź", "Ĺą", "Ĺ»", "Ĺ˝", "ĹŁ", "ĹĄ", "Ăą", "Ăş", "Ă»", "Ĺł", "ĂĽ", "ĹŻ", "Ĺ±", "Ă˝", "Ăż", "Ĺş", "ĹĽ", "Ĺľ", "Đ?", "Đ‘", "Đ’", "Đ“", "Đ”", "Đ•", "Đ?", "Đ–", "Đ—", "Đ?", "Đ™", "Đš", "Đ›", "Đś", "Đť", "Đž", "Đź", "Đ ", "Đ°", "Đ±", "Đ˛", "Đł", "Đ´", "Đµ", "Ń‘", "Đ¶", "Đ·", "Đ¸", "Đą", "Đş", "Đ»", "ĐĽ", "Đ˝", "Đľ", "Ń€", "Đˇ", "Đ˘", "ĐŁ", "Đ¤", "ĐĄ", "Đ¦", "Đ§", "Đ¨", "Đ©", "ĐŞ", "Đ«", "Đ¬", "Đ­", "Đ®", "ĐŻ", "Ń?", "Ń‚", "Ń?", "Ń„", "Ń…", "Ń†", "Ń‡", "Ń?", "Ń‰", "ŃŠ", "Ń‹", "ŃŚ", "ŃŤ", "ŃŽ", "ŃŹ" };

    static String[] replaceAll = { "A", "A", "A", "A", "A", "A", "AE", "A", "A", "C", "C", "C", "CE", "D", "D", "a", "a", "a", "a", "a", "a", "ae", "a", "a", "c", "c", "c", "ce", "d", "d", "E", "E", "E", "E", "E", "E", "E", "G", "I", "I", "I", "I", "I", "L", "L", "L", "e", "e", "e", "e", "e", "e", "e", "g", "i", "i", "i", "i", "i", "l", "l", "l", "N", "N", "N", "O", "O", "O", "O", "O", "O", "O", "R", "R", "S", "S", "S", "n", "n", "n", "o", "o", "o", "o", "o", "o", "r", "r", "s", "s", "s", "T", "T", "U", "U", "U", "U", "U", "U", "U", "Y", "Y", "Z", "Z", "Z", "t", "t", "u", "u", "u", "u", "u", "u", "u", "y", "y", "z", "z", "z", "A", "B", "B", "r", "A", "E", "E", "X", "3", "N", "N", "K", "N", "M", "H", "O", "N", "P", "a", "b", "b", "r", "a", "e", "e", "x", "3", "n", "n", "k", "n", "m", "h", "o", "p", "C", "T", "Y", "O", "X", "U", "u", "W", "W", "b", "b", "b", "E", "O", "R", "c", "t", "y", "o", "x", "u", "u", "w", "w", "b", "b", "b", "e", "o", "r" };

    static String[] accented = { "Ă€", "Ă?", "Ă‚", "Ă?", "Ă„", "Ă…", "Ă†", "Ä‚", "Ä„", "Ă‡", "Ä†", "ÄŚ", "Ĺ’", "ÄŽ", "Ä?", "Ă?", "Ă‰", "ĂŠ", "Ă‹", "Ä?", "Äš", "ĆŹ", "Äž", "ĂŚ", "ĂŤ", "ĂŽ", "ĂŹ", "Ä°", "Äą", "Ä˝", "Ĺ?", "Ă‘", "Ĺ?", "Ĺ‡", "Ă’", "Ă“", "Ă”", "Ă•", "Ă–", "Ă?", "Ĺ?", "Ĺ”", "Ĺ?", "Ĺš", "Ĺž", "Ĺ ", "Ĺ˘", "Ĺ¤", "Ă™", "Ăš", "Ă›", "Ĺ˛", "Ăś", "Ĺ®", "Ĺ°", "Ăť", "Ăź", "Ĺą", "Ĺ»", "Ĺ˝", "Đ?", "Đ‘", "Đ’", "Đ“", "Đ”", "Đ•", "Đ?", "Đ–", "Đ—", "Đ?", "Đ™", "Đš", "Đ›", "Đś", "Đť", "Đž", "Đź", "Đ ", "Đˇ", "Đ˘", "ĐŁ", "Đ¤", "ĐĄ", "Đ¦", "Đ§", "Đ¨", "Đ©", "ĐŞ", "Đ«", "Đ¬", "Đ­", "Đ®", "ĐŻ" };

    static String[] replace = { "A", "A", "A", "A", "A", "A", "AE", "A", "A", "C", "C", "C", "CE", "D", "D", "E", "E", "E", "E", "E", "E", "E", "G", "I", "I", "I", "I", "I", "L", "L", "L", "N", "N", "N", "O", "O", "O", "O", "O", "O", "O", "R", "R", "S", "S", "S", "T", "T", "U", "U", "U", "U", "U", "U", "U", "Y", "Y", "Z", "Z", "Z", "A", "B", "B", "r", "A", "E", "E", "X", "3", "N", "N", "K", "N", "M", "H", "O", "N", "P", "C", "T", "Y", "O", "X", "U", "u", "W", "W", "b", "b", "b", "E", "O", "R" };

    public static String unaccentUpper(String s) {
        for (int i = 0; i < accented.length; i++) {
            s = s.replace(accented[i], replace[i]);
        }
        return s;
    }

    public static String unaccent(String s) {
        for (int i = 0; i < accentedAll.length; i++) {
            s = s.replace(accentedAll[i], replaceAll[i]);
        }
        return s;
    }

    public static String removeSpecChars(String s) {
        return s.replace(" ", "-").replaceAll("[^a-zA-Z0-9_-]", "");
    }

    public static String subString(String s, int maxLength) {
        if (s.length() > maxLength) s = s.substring(0, maxLength);
        return s;
    }

    public static String hash(String text) throws Exception {
        StringBuffer hexString;
        MessageDigest mdAlgorithm = MessageDigest.getInstance("MD5");
        mdAlgorithm.update(text.getBytes());
        byte[] digest = mdAlgorithm.digest();
        hexString = new StringBuffer();
        for (int i = 0; i < digest.length; i++) {
            text = Integer.toHexString(0xFF & digest[i]);
            if (text.length() < 2) {
                text = "0" + text;
            }
            hexString.append(text);
        }
        return hexString.toString();
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || "".equals(str);
    }

    /**
	 * example: 
	 * splitToCharBlocks("0123456789", 3, "-") returns "012-345-678-9"
	 * 
	 * @param string
	 * @param blockSize
	 * @param separator
	 * @return
	 */
    public static String splitToCharBlocks(String string, int blockSize, String separator) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < string.length(); i++) {
            sb.append(string.charAt(i));
            if ((i + 1) % blockSize == 0 && i != string.length() - 1) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }
}
