package com.mepping.snmpjaag.nrpe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

/**
 * CheckNrpe
 * 
 * @author roberto
 *
 */
public class CheckNrpe {

    public static final int MAX_PACKETBUFFER_LENGTH = 1024;

    public static final int C_STRUCT_PADDING = 2;

    public static final int DEFAULT_PORT = 5666;

    public static final String DEFAULT_COMMAND = "_NRPE_CHECK";

    public static final int DEFAULT_TIMEOUT = 30000;

    public static final int NRPE_PACKET_VERSION_1 = 1;

    public static final int NRPE_PACKET_VERSION_2 = 2;

    public static final int NRPE_PACKET_VERSION_3 = 3;

    public static final int QUERY_PACKET = 1;

    public static final int RESPONSE_PACKET = 2;

    public static final int RESPONSE_PACKET_WITH_MORE = 3;

    public PluginResult check(boolean ssl, String hostname) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        return check(ssl, hostname, DEFAULT_PORT, DEFAULT_TIMEOUT, DEFAULT_COMMAND, null);
    }

    public PluginResult check(boolean ssl, String hostname, int port) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        return check(ssl, hostname, port, DEFAULT_TIMEOUT, DEFAULT_COMMAND, null);
    }

    public PluginResult check(boolean ssl, String hostname, int port, String command) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        return check(ssl, hostname, port, DEFAULT_TIMEOUT, command, null);
    }

    public PluginResult check(boolean ssl, String hostname, int port, String command, String[] args) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        return check(ssl, hostname, port, DEFAULT_TIMEOUT, command, args);
    }

    public PluginResult check(boolean ssl, String hostname, int port, int timeout, String command, String[] argv) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        StringBuffer cmdline = new StringBuffer();
        cmdline.append(command.trim());
        if (null != argv) {
            for (int i = 1; i < argv.length; i++) {
                cmdline.append(' ').append(argv[i].trim());
            }
        }
        return check(ssl, hostname, port, timeout, cmdline.toString());
    }

    public PluginResult check(boolean ssl, String hostname, int port, int timeout, String cmdline) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        byte[] packet = new byte[2 + 2 + 4 + 2 + MAX_PACKETBUFFER_LENGTH + C_STRUCT_PADDING];
        htons(NRPE_PACKET_VERSION_2, packet, 0);
        htons(QUERY_PACKET, packet, 2);
        htonl(0, packet, 4);
        htons(PluginResult.STATE_OK, packet, 8);
        System.arraycopy(cmdline.getBytes(), 0, packet, 10, cmdline.length());
        packet[10 + MAX_PACKETBUFFER_LENGTH - 1] = 0;
        CRC32 crc32 = new CRC32();
        crc32.update(packet);
        htonl(crc32.getValue(), packet, 4);
        hexdump(packet, 0, packet.length, System.out);
        Socket socket = new Socket(hostname, port);
        socket.setSoTimeout(timeout);
        if (ssl) {
            socket = wrap(socket);
        }
        OutputStream out = socket.getOutputStream();
        out.write(packet);
        InputStream in = socket.getInputStream();
        int length = in.read(packet);
        System.out.println("length=" + length);
        hexdump(packet, 0, length, System.out);
        long packetCrc32 = ntohl(packet, 4);
        htonl(0, packet, 4);
        crc32.reset();
        crc32.update(packet);
        if (packetCrc32 != crc32.getValue()) {
            throw new IOException("Bad CRC32 " + packetCrc32 + " (expected " + crc32.getValue() + ")");
        }
        PluginResult result = new PluginResult();
        result.setState(ntohs(packet, 8));
        StringBuffer pluginOutput = new StringBuffer();
        for (int i = 10; i < packet.length && 0 != packet[i]; i++) {
            pluginOutput.append((char) packet[i]);
        }
        result.setPluginOutput(pluginOutput.toString());
        return result;
    }

    private Socket wrap(Socket socket) {
        return socket;
    }

    public static void htons(int val, byte[] buf, int off) {
        buf[1 + off] = (byte) ((val) & 0xff);
        buf[off] = (byte) ((val >> 8) & 0xff);
    }

    public static int ntohs(byte[] buf, int off) {
        return ((int) (buf[off] & 0xff) << 8) + ((int) (buf[1 + off] & 0xff));
    }

    public static void htonl(long val, byte[] buf, int off) {
        buf[3 + off] = (byte) ((val) & 0xff);
        buf[2 + off] = (byte) ((val >> 8) & 0xff);
        buf[1 + off] = (byte) ((val >> 16) & 0xff);
        buf[off] = (byte) ((val >> 24) & 0xff);
    }

    public static long ntohl(byte[] buf, int off) {
        return ((long) (buf[off] & 0xff) << 24) + ((long) (buf[1 + off] & 0xff) << 16) + ((long) (buf[2 + off] & 0xff) << 8) + ((long) (buf[3 + off] & 0xff));
    }

    public static void hexdump(byte[] buf, int off, int len, PrintStream out) {
        while (len > 0) {
            String text = " ";
            int j = 0;
            for (; j < 16 && len > 0; j++) {
                out.print("0123456789abcdef".charAt((buf[off] >> 4) & 0x0f));
                out.print("0123456789abcdef".charAt((buf[off]) & 0x0f));
                out.print(7 == j ? "  " : " ");
                if (buf[off] >= 32 && buf[off] <= 126) {
                    text += (char) buf[off];
                } else {
                    text += '.';
                }
                off++;
                len--;
            }
            for (; j < 16; j++) {
                out.print("   ");
            }
            out.print(text);
            out.println();
        }
    }

    public static void main(String[] args) throws Exception {
        String hostname = "192.168.56.101";
        int port = 5666;
        String command = "check_disks";
        PluginResult result = new CheckNrpe().check(false, hostname, port, command);
        System.out.println(result.getState());
        System.out.println(result.getPluginOutput());
        System.out.println("$SERVICEOUTPUT$=\"" + result.getServiceOutput() + "\"");
        if (null != result.getServicePerfData()) {
            System.out.println("$SERVICEPERFDATA$=\"" + result.getServicePerfData() + "\"");
            PerfData[] array = result.getServicePerfDataArray();
            for (int i = 0; i < array.length; i++) {
                System.out.println(array[i]);
            }
        }
        if (null != result.getLongServiceOutput()) {
            System.out.println("$LONGSERVICEOUTPUT$=\"" + result.getLongServiceOutput() + "\"");
        }
        System.exit(result.getState());
    }
}
