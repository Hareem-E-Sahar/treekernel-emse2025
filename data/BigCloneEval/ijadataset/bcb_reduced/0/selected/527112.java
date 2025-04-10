package org.snova.c4.client.connection.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import org.arch.util.RandomHelper;

/**
 * @author qiyingwang
 * 
 */
public class ConnectionHelper {

    private static String userToken = null;

    public static String getUserToken() {
        if (null != userToken) {
            return userToken;
        }
        try {
            byte[] mac = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < mac.length; i++) {
                if (i != 0) {
                    sb.append("-");
                }
                String s = Integer.toHexString(mac[i] & 0xFF);
                sb.append(s.length() == 1 ? 0 + s : s);
            }
            userToken = sb.toString();
        } catch (Exception e) {
            userToken = RandomHelper.generateRandomString(8);
        }
        return userToken;
    }
}
