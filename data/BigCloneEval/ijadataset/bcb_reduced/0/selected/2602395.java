package com.android.zweibo.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

    /** 
     * 16进制字符集 
     */
    private static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    private static MessageDigest messageDigest = null;

    /** 
     * 初始化messageDigest的加密算法为MD5 
     */
    static {
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /** 
     * MD5加密字符串 
     * @param str 目标字符串 
     * @return MD5加密后的字符串 
     */
    public static String getMD5String(String str) {
        return getMD5String(str.getBytes());
    }

    /** 
     * MD5加密以byte数组表示的字符串 
     * @param bytes 目标byte数组 
     * @return MD5加密后的字符串 
     */
    public static String getMD5String(byte[] bytes) {
        messageDigest.update(bytes);
        return bytesToHex(messageDigest.digest());
    }

    /** 
     * 将字节数组转换成16进制字符串 
     * @param bytes 目标字节数组 
     * @return 转换结果 
     */
    public static String bytesToHex(byte bytes[]) {
        return bytesToHex(bytes, 0, bytes.length);
    }

    /** 
     * 将字节数组中指定区间的子数组转换成16进制字符串 
     * @param bytes 目标字节数组 
     * @param start 起始位置（包括该位置） 
     * @param end 结束位置（不包括该位置） 
     * @return 转换结果 
     */
    public static String bytesToHex(byte bytes[], int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < start + end; i++) {
            sb.append(byteToHex(bytes[i]));
        }
        return sb.toString();
    }

    /** 
     * 将单个字节码转换成16进制字符串 
     * @param bt 目标字节 
     * @return 转换结果 
     */
    public static String byteToHex(byte bt) {
        return HEX_DIGITS[(bt & 0xf0) >> 4] + "" + HEX_DIGITS[bt & 0xf];
    }
}
