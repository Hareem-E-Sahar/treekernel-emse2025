package com.googlecode.ascrblr.api.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

    /**
     * Creates a MD5 hash from a <code>String</code>
     * @param value Value to build a MD5 hash
     * @return Hashed value
     * @throws ServiceException
     */
    public static String getMD5(String value) throws ServiceException {
        try {
            byte[] intext = value.getBytes();
            StringBuffer sb = new StringBuffer();
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] md5rslt = md5.digest(intext);
            for (int i = 0; i < md5rslt.length; i++) {
                String tmpStr = "0" + Integer.toHexString((0xff & md5rslt[i]));
                sb.append(tmpStr.substring(tmpStr.length() - 2));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new ServiceException("Authentication token can not be created", e);
        }
    }
}
