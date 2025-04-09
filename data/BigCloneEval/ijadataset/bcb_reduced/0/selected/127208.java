package it.conte.tesi.snmp.utils;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author conte
 */
public class NetworkInterfaceInfo {

    private static Set<String> macs = new HashSet<String>();

    public static Set<String> getMyInterfacesMacAddress() {
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netint : Collections.list(nets)) {
                putMacAddress(netint);
            }
        } catch (SocketException ex) {
            Logger.getLogger(NetworkInterfaceInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        return macs;
    }

    private static void putMacAddress(NetworkInterface netint) throws SocketException {
        byte[] mac = netint.getHardwareAddress();
        String formattata = "";
        if (mac != null) {
            for (int i = 0; i < mac.length; i++) {
                formattata = formattata.concat(String.format("%02x%s", mac[i], (i < mac.length - 1) ? ":" : ""));
            }
            macs.add(formattata);
        }
    }
}
