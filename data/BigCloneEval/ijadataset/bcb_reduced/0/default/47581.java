import java.util.Arrays;

public class BinaryConvert {

    public static boolean[] getBinaryArrayFromToken(String hexChars) {
        int value = Integer.parseInt(hexChars, 16);
        int numOfBits = hexChars.length() * 4;
        boolean[] booleans = new boolean[numOfBits];
        for (int i = 0; i < numOfBits; i++) {
            booleans[i] = (value & 1 << i) != 0;
        }
        Log.level3("Binary to Array:" + Arrays.toString(booleans));
        return booleans;
    }

    public static boolean[] getBinaryArrayFromBinaryString(String BinaryString) {
        boolean[] booleans = new boolean[BinaryString.length()];
        for (int i = 0; i < BinaryString.length(); i++) {
            if (String.valueOf(BinaryString.charAt(i)).endsWith("1")) {
                booleans[i] = true;
            } else {
                booleans[i] = false;
            }
        }
        Log.level3("Input Binary String:" + BinaryString);
        Log.level3("Output Array:" + Arrays.toString(booleans));
        return booleans;
    }

    public static int checkNumberOfTokensLeft() {
        int X = Status.LineToken.countTokens();
        return X;
    }

    public static String getBinaryString(String hexChars) {
        int binaryToken = Integer.parseInt(hexChars, 16);
        String binaryValue = Integer.toBinaryString(binaryToken);
        int leading0s = 8 - binaryValue.length();
        while (leading0s != 0) {
            binaryValue = "0" + binaryValue;
            leading0s = leading0s - 1;
        }
        Log.level3("Binary To String: " + binaryValue);
        return binaryValue;
    }
}
