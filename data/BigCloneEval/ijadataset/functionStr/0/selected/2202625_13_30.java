public class Test {    public static String getMD5(String str) {
        try {
            java.security.MessageDigest md5 = java.security.MessageDigest.getInstance("MD5");
            char[] charArray = str.toCharArray();
            byte[] byteArray = new byte[charArray.length];
            for (int i = 0; i < charArray.length; i++) byteArray[i] = (byte) charArray[i];
            byte[] md5Bytes = md5.digest(byteArray);
            StringBuffer hexValue = new StringBuffer();
            for (byte md5Byte : md5Bytes) {
                int val = ((int) md5Byte) & 0xff;
                if (val < 16) hexValue.append("0");
                hexValue.append(Integer.toHexString(val));
            }
            return hexValue.toString();
        } catch (NoSuchAlgorithmException nsae) {
        }
        return "";
    }
}