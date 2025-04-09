package it.conte.tesi.snmp.utils;

import java.net.*;
import java.util.*;
import static java.lang.System.out;

public class ListNetsEx {

    public static void main(String args[]) throws SocketException {
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets)) {
            displayInterfaceInformation(netint);
        }
    }

    static void displayInterfaceInformation(NetworkInterface netint) throws SocketException {
        out.printf("Display name: %s\n", netint.getDisplayName());
        out.printf("Name: %s\n", netint.getName());
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            out.printf("InetAddress: %s\n", inetAddress);
        }
        out.printf("Up? %s\n", netint.isUp());
        out.printf("Loopback? %s\n", netint.isLoopback());
        out.printf("PointToPoint? %s\n", netint.isPointToPoint());
        out.printf("Supports multicast? %s\n", netint.supportsMulticast());
        out.printf("Virtual? %s\n", netint.isVirtual());
        byte[] mac = netint.getHardwareAddress();
        String formattata = "";
        if (mac != null) {
            for (int i = 0; i < mac.length; i++) {
                formattata = formattata.concat(String.format("%02x%s", mac[i], (i < mac.length - 1) ? ":" : ""));
            }
            System.out.println("Mac address: " + formattata);
            out.printf("\nMTU: %s\n", netint.getMTU());
            out.printf("\n");
        }
    }
}
