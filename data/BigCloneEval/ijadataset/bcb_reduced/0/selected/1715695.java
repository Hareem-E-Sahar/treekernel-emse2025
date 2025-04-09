package SystemInfo;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 *
 * @author sahaqiel
 *
 */
public class NetworkSystem {

    private enum NETWORK_VALUES {

        NetworkHostname, NetworkInterfaceDisplayNames, NetworkInterfaceHardwareAddresses, NetworkInterfaceLoopbacks, NetworkInterfaceMtus, NetworkInterfaceNames, NetworkInterfacePointToPoints, NetworkInterfaceSupportsMulticasts, NetworkInterfaceUps, NetworkInterfaceVirtuals;

        @Override
        public String toString() {
            switch(this) {
                case NetworkHostname:
                    return "network.hostname";
                case NetworkInterfaceDisplayNames:
                    return "network.interface.display.names";
                case NetworkInterfaceHardwareAddresses:
                    return "network.interface.hardware.addresses";
                case NetworkInterfaceLoopbacks:
                    return "network.interface.loopbacks";
                case NetworkInterfaceMtus:
                    return "network.interface.mtus";
                case NetworkInterfaceNames:
                    return "network.interface.names";
                case NetworkInterfacePointToPoints:
                    return "network.interface.point.to.points";
                case NetworkInterfaceSupportsMulticasts:
                    return "network.interface.supports.multicasts";
                case NetworkInterfaceUps:
                    return "network.interface.supports.ups";
                case NetworkInterfaceVirtuals:
                    return "network.interface.virtuals";
            }
            return "";
        }
    }

    ;

    public String getProperty(String keyStr) {
        NETWORK_VALUES value = null;
        for (NETWORK_VALUES cur : NETWORK_VALUES.values()) {
            if (cur.toString().equals(keyStr)) {
                value = cur;
            }
        }
        if (value != null) {
            switch(value) {
                case NetworkHostname:
                    return getHostname();
                case NetworkInterfaceDisplayNames:
                    return getDisplayNames();
                case NetworkInterfaceHardwareAddresses:
                    return getHardwareAddresses();
                case NetworkInterfaceLoopbacks:
                    return getLoopbacks();
                case NetworkInterfaceMtus:
                    return getMtus();
                case NetworkInterfaceNames:
                    return getNames();
                case NetworkInterfacePointToPoints:
                    return getPointToPoints();
                case NetworkInterfaceSupportsMulticasts:
                    return getSupportsMulticasts();
                case NetworkInterfaceUps:
                    return getUps();
                case NetworkInterfaceVirtuals:
                    return getVirtuals();
            }
        }
        return null;
    }

    private String getHostname() {
        StringBuilder bldr = new StringBuilder();
        try {
            InetAddress local = InetAddress.getLocalHost();
            bldr.append(local.getHostName());
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
        return bldr.toString();
    }

    private String getDisplayNames() {
        StringBuilder bldr = new StringBuilder();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                bldr.append(interfaces.nextElement().getDisplayName());
                bldr.append(", ");
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return rmLastTwo(bldr.toString());
    }

    private String getHardwareAddresses() {
        StringBuilder bldr = new StringBuilder();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface intr = interfaces.nextElement();
                bldr.append(intr.getDisplayName());
                bldr.append(" ");
                bldr.append(toHexString(intr.getHardwareAddress()));
                bldr.append(", ");
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return rmLastTwo(bldr.toString());
    }

    private String toHexString(byte[] bytes) {
        String result = "";
        if (bytes != null) {
            char[] HEX_ARRAY = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
            char[] hexChars = new char[bytes.length * 2];
            int v;
            for (int j = 0; j < bytes.length; j++) {
                v = bytes[j] & 0xFF;
                hexChars[j * 2] = HEX_ARRAY[v / 16];
                hexChars[j * 2 + 1] = HEX_ARRAY[v % 16];
            }
            result = new String(hexChars);
        }
        return insertColons(result, 1);
    }

    private String insertColons(String str, int period) {
        if (str == null) {
            return "";
        }
        StringBuilder bldr = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            bldr.append(str.charAt(i));
            if (i % 2 == 1) {
                bldr.append(':');
            }
        }
        String result = bldr.toString();
        if (result.endsWith(":")) {
            result = result.substring(0, result.lastIndexOf(':'));
        }
        return result;
    }

    private String getLoopbacks() {
        StringBuilder bldr = new StringBuilder();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface intr = interfaces.nextElement();
                bldr.append(intr.getDisplayName());
                if (intr.isLoopback()) {
                    bldr.append(" is a loopback");
                } else {
                    bldr.append(" is not a loopback");
                }
                bldr.append(", ");
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return rmLastTwo(bldr.toString());
    }

    private String getMtus() {
        StringBuilder bldr = new StringBuilder();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface intr = interfaces.nextElement();
                bldr.append(intr.getDisplayName());
                bldr.append(" ");
                bldr.append(intr.getMTU());
                bldr.append(", ");
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return rmLastTwo(bldr.toString());
    }

    private String getNames() {
        StringBuilder bldr = new StringBuilder();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                bldr.append(interfaces.nextElement().getName());
                bldr.append(", ");
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return rmLastTwo(bldr.toString());
    }

    private String getPointToPoints() {
        StringBuilder bldr = new StringBuilder();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface intr = interfaces.nextElement();
                bldr.append(intr.getDisplayName());
                if (intr.isPointToPoint()) {
                    bldr.append(" is Point to Point");
                } else {
                    bldr.append(" is not Point to Point");
                }
                bldr.append(", ");
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return rmLastTwo(bldr.toString());
    }

    private String getSupportsMulticasts() {
        StringBuilder bldr = new StringBuilder();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface intr = interfaces.nextElement();
                bldr.append(intr.getDisplayName());
                if (intr.supportsMulticast()) {
                    bldr.append(" supports multicast");
                } else {
                    bldr.append(" does not support multicast");
                }
                bldr.append(", ");
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return rmLastTwo(bldr.toString());
    }

    private String getUps() {
        StringBuilder bldr = new StringBuilder();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface intr = interfaces.nextElement();
                bldr.append(intr.getDisplayName());
                if (intr.isUp()) {
                    bldr.append(" is enabled");
                } else {
                    bldr.append(" is disabled");
                }
                bldr.append(", ");
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return rmLastTwo(bldr.toString());
    }

    private String getVirtuals() {
        StringBuilder bldr = new StringBuilder();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface intr = interfaces.nextElement();
                bldr.append(intr.getDisplayName());
                if (intr.isVirtual()) {
                    bldr.append(" is virtual (sub-interface)");
                } else {
                    bldr.append(" is not virtual");
                }
                bldr.append(", ");
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return rmLastTwo(bldr.toString());
    }

    private String rmLastTwo(final String str) {
        if (str.length() > 2) {
            int idx = str.lastIndexOf(", ");
            if (idx != -1) {
                return str.substring(0, idx);
            }
        }
        return "";
    }
}
