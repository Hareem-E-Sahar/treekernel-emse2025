package org.apache.harmony.luni.tests.java.net;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.DatagramSocketImpl;
import java.net.DatagramSocketImplFactory;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.PortUnreachableException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import org.apache.harmony.luni.net.PlainDatagramSocketImpl;
import org.junit.Test;
import tests.support.Support_Configuration;
import tests.support.Support_PortManager;

public class DatagramSocketTest extends SocketTestCase {

    java.net.DatagramSocket ds;

    java.net.DatagramPacket dp;

    DatagramSocket sds = null;

    String retval;

    String testString = "Test String";

    boolean interrupted;

    class DatagramServer extends Thread {

        public DatagramSocket ms;

        boolean running = true;

        public volatile byte[] rbuf = new byte[512];

        volatile DatagramPacket rdp = null;

        public void run() {
            try {
                while (running) {
                    try {
                        ms.receive(rdp);
                        ms.send(rdp);
                    } catch (java.io.InterruptedIOException e) {
                        Thread.yield();
                    }
                    ;
                }
                ;
            } catch (java.io.IOException e) {
                System.out.println("DatagramServer server failed: " + e);
            } finally {
                ms.close();
            }
        }

        public void stopServer() {
            running = false;
        }

        public DatagramServer(int aPort, InetAddress address) throws IOException {
            rbuf = new byte[512];
            rbuf[0] = -1;
            rdp = new DatagramPacket(rbuf, rbuf.length);
            ms = new DatagramSocket(aPort, address);
            ms.setSoTimeout(2000);
        }
    }

    /**
     * @tests java.net.DatagramSocket#DatagramSocket()
     */
    public void test_Constructor() throws SocketException {
        new DatagramSocket();
    }

    /**
     * @tests java.net.DatagramSocket#DatagramSocket(int)
     */
    public void test_ConstructorI() throws SocketException {
        DatagramSocket ds = new DatagramSocket(0);
        ds.close();
    }

    /**
     * @tests java.net.DatagramSocket#DatagramSocket(int, java.net.InetAddress)
     */
    public void test_ConstructorILjava_net_InetAddress() throws IOException {
        DatagramSocket ds = new DatagramSocket(0, InetAddress.getLocalHost());
        assertTrue("Created socket with incorrect port", ds.getLocalPort() != 0);
        assertEquals("Created socket with incorrect address", InetAddress.getLocalHost(), ds.getLocalAddress());
    }

    /**
     * @tests java.net.DatagramSocket#close()
     */
    public void test_close() throws UnknownHostException, SocketException {
        DatagramSocket ds = new DatagramSocket(0);
        DatagramPacket dp = new DatagramPacket("Test String".getBytes(), 11, InetAddress.getLocalHost(), 0);
        ds.close();
        try {
            ds.send(dp);
            fail("Data sent after close");
        } catch (IOException e) {
        }
    }

    /**
     * @tests java.net.DatagramSocket#connect(java.net.InetAddress, int)
     */
    public void test_connectLjava_net_InetAddressI() throws Exception {
        DatagramSocket ds = new DatagramSocket();
        InetAddress inetAddress = InetAddress.getLocalHost();
        ds.connect(inetAddress, 0);
        assertEquals("Incorrect InetAddress", inetAddress, ds.getInetAddress());
        assertEquals("Incorrect Port", 0, ds.getPort());
        ds.disconnect();
        int portNumber;
        if ("true".equals(System.getProperty("run.ipv6tests"))) {
            System.out.println("Running test_connectLjava_net_InetAddressI(DatagramSocketTest) with IPv6GlobalAddressJcl4: " + Support_Configuration.IPv6GlobalAddressJcl4);
            ds = new java.net.DatagramSocket();
            inetAddress = InetAddress.getByName(Support_Configuration.IPv6GlobalAddressJcl4);
            portNumber = Support_PortManager.getNextPortForUDP();
            ds.connect(inetAddress, portNumber);
            assertTrue("Incorrect InetAddress", ds.getInetAddress().equals(inetAddress));
            assertTrue("Incorrect Port", ds.getPort() == portNumber);
            ds.disconnect();
        }
        InetAddress localHost = InetAddress.getLocalHost();
        ds = new DatagramSocket();
        int port = ds.getLocalPort();
        ds.connect(localHost, port);
        DatagramPacket send = new DatagramPacket(new byte[10], 10, localHost, port);
        ds.send(send);
        DatagramPacket receive = new DatagramPacket(new byte[20], 20);
        ds.setSoTimeout(2000);
        ds.receive(receive);
        ds.close();
        assertTrue("Wrong size: " + receive.getLength(), receive.getLength() == 10);
        assertTrue("Wrong receiver", receive.getAddress().equals(localHost));
        class DatagramServer extends Thread {

            public DatagramSocket ms;

            boolean running = true;

            public byte[] rbuf = new byte[512];

            DatagramPacket rdp = null;

            public void run() {
                try {
                    while (running) {
                        try {
                            ms.receive(rdp);
                            ms.send(rdp);
                        } catch (java.io.InterruptedIOException e) {
                            Thread.yield();
                        }
                        ;
                    }
                    ;
                } catch (java.io.IOException e) {
                    System.out.println("Multicast server failed: " + e);
                } finally {
                    ms.close();
                }
            }

            public void stopServer() {
                running = false;
            }

            public DatagramServer(int aPort, InetAddress address) throws java.io.IOException {
                rbuf = new byte[512];
                rbuf[0] = -1;
                rdp = new DatagramPacket(rbuf, rbuf.length);
                ms = new DatagramSocket(aPort, address);
                ms.setSoTimeout(2000);
            }
        }
        try {
            ds = new java.net.DatagramSocket();
            inetAddress = InetAddress.getLocalHost();
            portNumber = Support_PortManager.getNextPortForUDP();
            ds.connect(inetAddress, portNumber);
            send = new DatagramPacket(new byte[10], 10);
            ds.send(send);
            receive = new DatagramPacket(new byte[20], 20);
            ds.setSoTimeout(10000);
            ds.receive(receive);
            ds.close();
            fail("No PortUnreachableException when connected at native level on recv ");
        } catch (PortUnreachableException e) {
        }
        DatagramServer server = null;
        int[] ports = Support_PortManager.getNextPortsForUDP(3);
        int serverPortNumber = ports[0];
        localHost = InetAddress.getLocalHost();
        ds = new DatagramSocket(ports[1]);
        DatagramSocket ds2 = new DatagramSocket(ports[2]);
        server = new DatagramServer(serverPortNumber, localHost);
        server.start();
        Thread.sleep(1000);
        port = ds.getLocalPort();
        ds.connect(localHost, serverPortNumber);
        final byte[] sendBytes = { 'T', 'e', 's', 't', 0 };
        send = new DatagramPacket(sendBytes, sendBytes.length);
        ds.send(send);
        receive = new DatagramPacket(new byte[20], 20);
        ds.setSoTimeout(2000);
        ds.receive(receive);
        ds.close();
        assertTrue("Wrong size data received: " + receive.getLength(), receive.getLength() == sendBytes.length);
        assertTrue("Wrong data received" + new String(receive.getData(), 0, receive.getLength()) + ":" + new String(sendBytes), new String(receive.getData(), 0, receive.getLength()).equals(new String(sendBytes)));
        assertTrue("Wrong receiver:" + receive.getAddress() + ":" + localHost, receive.getAddress().equals(localHost));
        if (server != null) {
            server.stopServer();
        }
        try {
            ds = new java.net.DatagramSocket();
            inetAddress = InetAddress.getLocalHost();
            portNumber = Support_PortManager.getNextPortForUDP();
            ds.connect(inetAddress, portNumber);
            ds.disconnect();
            ds.close();
        } catch (PortUnreachableException e) {
        }
        try {
            ds = new java.net.DatagramSocket();
            inetAddress = InetAddress.getLocalHost();
            portNumber = Support_PortManager.getNextPortForUDP();
            ds.connect(inetAddress, portNumber);
            send = new DatagramPacket(new byte[10], 10, inetAddress, portNumber + 1);
            ds.send(send);
            ds.close();
            fail("No Exception when trying to send to a different address on a connected socket ");
        } catch (IllegalArgumentException e) {
        }
        server = null;
        ports = Support_PortManager.getNextPortsForUDP(3);
        serverPortNumber = ports[0];
        localHost = InetAddress.getLocalHost();
        ds = new DatagramSocket(ports[1]);
        ds2 = new DatagramSocket(ports[2]);
        server = new DatagramServer(serverPortNumber, localHost);
        server.start();
        Thread.sleep(1000);
        port = ds.getLocalPort();
        ds.connect(localHost, serverPortNumber + 1);
        ds.disconnect();
        ds.connect(localHost, serverPortNumber);
        send = new DatagramPacket(sendBytes, sendBytes.length);
        ds.send(send);
        receive = new DatagramPacket(new byte[20], 20);
        ds.setSoTimeout(2000);
        ds.receive(receive);
        ds.close();
        assertTrue("connect/disconnect/connect - Wrong size data received: " + receive.getLength(), receive.getLength() == sendBytes.length);
        assertTrue("connect/disconnect/connect - Wrong data received" + new String(receive.getData(), 0, receive.getLength()) + ":" + new String(sendBytes), new String(receive.getData(), 0, receive.getLength()).equals(new String(sendBytes)));
        assertTrue("connect/disconnect/connect - Wrong receiver:" + receive.getAddress() + ":" + localHost, receive.getAddress().equals(localHost));
        if (server != null) {
            server.stopServer();
        }
        server = null;
        ports = Support_PortManager.getNextPortsForUDP(3);
        serverPortNumber = ports[0];
        localHost = InetAddress.getLocalHost();
        ds = new DatagramSocket(ports[1]);
        ds2 = new DatagramSocket(ports[2]);
        server = new DatagramServer(serverPortNumber, localHost);
        server.start();
        Thread.sleep(1000);
        port = ds.getLocalPort();
        ds.connect(localHost, serverPortNumber + 1);
        ds.disconnect();
        send = new DatagramPacket(sendBytes, sendBytes.length, localHost, serverPortNumber);
        ds.send(send);
        receive = new DatagramPacket(new byte[20], 20);
        ds.setSoTimeout(2000);
        ds.receive(receive);
        ds.close();
        assertTrue("connect/disconnect - Wrong size data received: " + receive.getLength(), receive.getLength() == sendBytes.length);
        assertTrue("connect/disconnect - Wrong data received" + new String(receive.getData(), 0, receive.getLength()) + ":" + new String(sendBytes), new String(receive.getData(), 0, receive.getLength()).equals(new String(sendBytes)));
        assertTrue("connect/disconnect - Wrong receiver:" + receive.getAddress() + ":" + localHost, receive.getAddress().equals(localHost));
        if (server != null) {
            server.stopServer();
        }
        server = null;
        ports = Support_PortManager.getNextPortsForUDP(3);
        serverPortNumber = ports[0];
        localHost = InetAddress.getLocalHost();
        ds = new DatagramSocket(ports[1]);
        ds2 = new DatagramSocket(ports[2]);
        server = new DatagramServer(serverPortNumber, localHost);
        server.start();
        Thread.sleep(1000);
        port = ds.getLocalPort();
        ds.connect(localHost, serverPortNumber + 1);
        ds.connect(localHost, serverPortNumber);
        send = new DatagramPacket(sendBytes, sendBytes.length);
        ds.send(send);
        receive = new DatagramPacket(new byte[20], 20);
        ds.setSoTimeout(2000);
        ds.receive(receive);
        ds.close();
        assertTrue("connect/connect - Wrong size data received: " + receive.getLength(), receive.getLength() == sendBytes.length);
        assertTrue("connect/connect - Wrong data received" + new String(receive.getData(), 0, receive.getLength()) + ":" + new String(sendBytes), new String(receive.getData(), 0, receive.getLength()).equals(new String(sendBytes)));
        assertTrue("connect/connect - Wrong receiver:" + receive.getAddress() + ":" + localHost, receive.getAddress().equals(localHost));
        if (server != null) {
            server.stopServer();
        }
        ds = new java.net.DatagramSocket();
        byte[] addressBytes = { 0, 0, 0, 0 };
        inetAddress = InetAddress.getByAddress(addressBytes);
        portNumber = Support_PortManager.getNextPortForUDP();
        ds.connect(inetAddress, portNumber);
        if ("true".equals(System.getProperty("run.ipv6tests"))) {
            System.out.println("Running test_connectLjava_net_InetAddressI(DatagramSocketTest) with IPv6 address");
            ds = new java.net.DatagramSocket();
            byte[] addressTestBytes = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            inetAddress = InetAddress.getByAddress(addressTestBytes);
            portNumber = Support_PortManager.getNextPortForUDP();
            ds.connect(inetAddress, portNumber);
        }
    }

    /**
     * @tests java.net.DatagramSocket#disconnect()
     */
    public void test_disconnect() throws Exception {
        DatagramSocket ds = new DatagramSocket();
        InetAddress inetAddress = InetAddress.getLocalHost();
        ds.connect(inetAddress, 0);
        ds.disconnect();
        assertNull("Incorrect InetAddress", ds.getInetAddress());
        assertEquals("Incorrect Port", -1, ds.getPort());
        if ("true".equals(System.getProperty("run.ipv6tests"))) {
            System.out.println("Running test_disconnect(DatagramSocketTest) with IPv6GlobalAddressJcl4: " + Support_Configuration.IPv6GlobalAddressJcl4);
            ds = new DatagramSocket();
            inetAddress = InetAddress.getByName(Support_Configuration.IPv6GlobalAddressJcl4);
            ds.connect(inetAddress, 0);
            ds.disconnect();
            assertNull("Incorrect InetAddress", ds.getInetAddress());
            assertEquals("Incorrect Port", -1, ds.getPort());
        }
    }

    /**
     * @tests java.net.DatagramSocket#getInetAddress()
     */
    public void test_getInetAddress() {
        assertTrue("Used to test", true);
    }

    /**
     * @tests java.net.DatagramSocket#getLocalAddress()
     */
    public void test_getLocalAddress() throws Exception {
        InetAddress local = null;
        int portNumber = Support_PortManager.getNextPortForUDP();
        local = InetAddress.getLocalHost();
        ds = new java.net.DatagramSocket(portNumber, local);
        assertTrue("Returned incorrect address. Got:" + ds.getLocalAddress() + " wanted: " + InetAddress.getByName(InetAddress.getLocalHost().getHostName()), InetAddress.getByName(InetAddress.getLocalHost().getHostName()).equals(ds.getLocalAddress()));
        String preferIPv4StackValue = System.getProperty("java.net.preferIPv4Stack");
        String preferIPv6AddressesValue = System.getProperty("java.net.preferIPv6Addresses");
        DatagramSocket s = new DatagramSocket(0);
        if (((preferIPv4StackValue == null) || preferIPv4StackValue.equalsIgnoreCase("false")) && (preferIPv6AddressesValue != null) && (preferIPv6AddressesValue.equals("true"))) {
            assertTrue("ANY address not returned correctly (getLocalAddress) with preferIPv6Addresses=true, preferIPv4Stack=false " + s.getLocalSocketAddress(), s.getLocalAddress() instanceof Inet6Address);
        } else {
            assertTrue("ANY address not returned correctly (getLocalAddress) with preferIPv6Addresses=true, preferIPv4Stack=true " + s.getLocalSocketAddress(), s.getLocalAddress() instanceof Inet4Address);
        }
        s.close();
    }

    /**
     * @tests java.net.DatagramSocket#getLocalPort()
     */
    public void test_getLocalPort() throws SocketException {
        DatagramSocket ds = new DatagramSocket();
        assertTrue("Returned incorrect port", ds.getLocalPort() != 0);
    }

    /**
     * @tests java.net.DatagramSocket#getPort()
     */
    public void test_getPort() throws IOException {
        DatagramSocket theSocket = new DatagramSocket();
        assertEquals("Expected -1 for remote port as not connected", -1, theSocket.getPort());
        int portNumber = 49152;
        theSocket.connect(InetAddress.getLocalHost(), portNumber);
        assertEquals("getPort returned wrong value", portNumber, theSocket.getPort());
    }

    /**
     * @tests java.net.DatagramSocket#getReceiveBufferSize()
     */
    public void test_getReceiveBufferSize() throws SocketException {
        DatagramSocket ds = new DatagramSocket();
        try {
            ds.setReceiveBufferSize(130);
            assertTrue("Incorrect buffer size", ds.getReceiveBufferSize() >= 130);
            ensureExceptionThrownIfOptionIsUnsupportedOnOS(SO_RCVBUF);
        } catch (Exception e) {
            handleException(e, SO_RCVBUF);
        }
    }

    /**
     * @tests java.net.DatagramSocket#getSendBufferSize()
     */
    public void test_getSendBufferSize() {
        try {
            int portNumber = Support_PortManager.getNextPortForUDP();
            ds = new java.net.DatagramSocket(portNumber);
            ds.setSendBufferSize(134);
            assertTrue("Incorrect buffer size", ds.getSendBufferSize() >= 134);
            ensureExceptionThrownIfOptionIsUnsupportedOnOS(SO_SNDBUF);
        } catch (Exception e) {
            handleException(e, SO_SNDBUF);
        }
    }

    /**
     * @tests java.net.DatagramSocket#getSoTimeout()
     */
    public void test_getSoTimeout() throws SocketException {
        DatagramSocket ds = new DatagramSocket();
        try {
            ds.setSoTimeout(100);
            assertEquals("Returned incorrect timeout", 100, ds.getSoTimeout());
            ensureExceptionThrownIfOptionIsUnsupportedOnOS(SO_TIMEOUT);
        } catch (Exception e) {
            handleException(e, SO_TIMEOUT);
        }
    }

    /**
     * @tests java.net.DatagramSocket#receive(java.net.DatagramPacket)
     */
    public void test_receiveLjava_net_DatagramPacket() throws IOException {
        receive_oversize_java_net_DatagramPacket();
        final int[] ports = Support_PortManager.getNextPortsForUDP(2);
        final int portNumber = ports[0];
        class TestDGRcv implements Runnable {

            public void run() {
                InetAddress localHost = null;
                try {
                    localHost = InetAddress.getLocalHost();
                    Thread.sleep(1000);
                    DatagramSocket sds = new DatagramSocket(ports[1]);
                    DatagramPacket rdp = new DatagramPacket("Test String".getBytes(), 11, localHost, portNumber);
                    sds.send(rdp);
                    sds.close();
                } catch (Exception e) {
                    System.err.println("host " + localHost + " port " + portNumber + " failed to send data: " + e);
                    e.printStackTrace();
                }
            }
        }
        try {
            new Thread(new TestDGRcv(), "DGSender").start();
            ds = new java.net.DatagramSocket(portNumber);
            ds.setSoTimeout(6000);
            byte rbuf[] = new byte[1000];
            DatagramPacket rdp = new DatagramPacket(rbuf, rbuf.length);
            ds.receive(rdp);
            ds.close();
            assertTrue("Send/Receive failed to return correct data: " + new String(rbuf, 0, 11), new String(rbuf, 0, 11).equals("Test String"));
        } finally {
            ds.close();
        }
        try {
            interrupted = false;
            final DatagramSocket ds = new DatagramSocket();
            ds.setSoTimeout(12000);
            Runnable runnable = new Runnable() {

                public void run() {
                    try {
                        ds.receive(new DatagramPacket(new byte[1], 1));
                    } catch (InterruptedIOException e) {
                        interrupted = true;
                    } catch (IOException e) {
                    }
                }
            };
            Thread thread = new Thread(runnable, "DatagramSocket.receive1");
            thread.start();
            try {
                do {
                    Thread.sleep(500);
                } while (!thread.isAlive());
            } catch (InterruptedException e) {
            }
            ds.close();
            int c = 0;
            do {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
                if (interrupted) {
                    fail("received interrupt");
                }
                if (++c > 4) {
                    fail("read call did not exit");
                }
            } while (thread.isAlive());
            interrupted = false;
            int[] ports1 = Support_PortManager.getNextPortsForUDP(2);
            final int portNum = ports[0];
            final DatagramSocket ds2 = new DatagramSocket(ports[1]);
            ds2.setSoTimeout(12000);
            Runnable runnable2 = new Runnable() {

                public void run() {
                    try {
                        ds2.receive(new DatagramPacket(new byte[1], 1, InetAddress.getLocalHost(), portNum));
                    } catch (InterruptedIOException e) {
                        interrupted = true;
                    } catch (IOException e) {
                    }
                }
            };
            Thread thread2 = new Thread(runnable2, "DatagramSocket.receive2");
            thread2.start();
            try {
                do {
                    Thread.sleep(500);
                } while (!thread2.isAlive());
            } catch (InterruptedException e) {
            }
            ds2.close();
            int c2 = 0;
            do {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
                if (interrupted) {
                    fail("receive2 was interrupted");
                }
                if (++c2 > 4) {
                    fail("read2 call did not exit");
                }
            } while (thread2.isAlive());
            interrupted = false;
            DatagramSocket ds3 = new DatagramSocket();
            ds3.setSoTimeout(500);
            Date start = new Date();
            try {
                ds3.receive(new DatagramPacket(new byte[1], 1));
            } catch (InterruptedIOException e) {
                interrupted = true;
            }
            ds3.close();
            assertTrue("receive not interrupted", interrupted);
            int delay = (int) (new Date().getTime() - start.getTime());
            assertTrue("timeout too soon: " + delay, delay >= 490);
        } catch (IOException e) {
            fail("Unexpected IOException : " + e.getMessage());
        }
    }

    /**
     * @tests java.net.DatagramSocket#send(java.net.DatagramPacket)
     */
    public void test_sendLjava_net_DatagramPacket() throws Exception {
        int[] ports = Support_PortManager.getNextPortsForUDP(2);
        final int portNumber = ports[0];
        class TestDGSend implements Runnable {

            Thread pThread;

            public TestDGSend(Thread t) {
                pThread = t;
            }

            public void run() {
                try {
                    byte[] rbuf = new byte[1000];
                    sds = new DatagramSocket(portNumber);
                    DatagramPacket sdp = new DatagramPacket(rbuf, rbuf.length);
                    sds.setSoTimeout(6000);
                    sds.receive(sdp);
                    retval = new String(rbuf, 0, testString.length());
                    pThread.interrupt();
                } catch (java.io.InterruptedIOException e) {
                    System.out.println("Recv operation timed out");
                    pThread.interrupt();
                    ds.close();
                    return;
                } catch (Exception e) {
                    System.out.println("Failed to establish Dgram server: " + e);
                }
            }
        }
        try {
            new Thread(new TestDGSend(Thread.currentThread()), "DGServer").start();
            ds = new java.net.DatagramSocket(ports[1]);
            dp = new DatagramPacket(testString.getBytes(), testString.length(), InetAddress.getLocalHost(), portNumber);
            try {
                Thread.sleep(500);
                ds.send(dp);
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                ds.close();
                assertTrue("Incorrect data sent: " + retval, retval.equals(testString));
            }
        } finally {
            ds.close();
        }
        class testDatagramSocket extends DatagramSocket {

            public testDatagramSocket(DatagramSocketImpl impl) {
                super(impl);
            }
        }
        class testDatagramSocketImpl extends DatagramSocketImpl {

            protected void create() throws SocketException {
            }

            protected void bind(int arg0, InetAddress arg1) throws SocketException {
            }

            protected void send(DatagramPacket arg0) throws IOException {
            }

            protected int peek(InetAddress arg0) throws IOException {
                return 0;
            }

            protected int peekData(DatagramPacket arg0) throws IOException {
                return 0;
            }

            protected void receive(DatagramPacket arg0) throws IOException {
            }

            protected void setTTL(byte arg0) throws IOException {
            }

            protected byte getTTL() throws IOException {
                return 0;
            }

            protected void setTimeToLive(int arg0) throws IOException {
            }

            protected int getTimeToLive() throws IOException {
                return 0;
            }

            protected void join(InetAddress arg0) throws IOException {
            }

            protected void leave(InetAddress arg0) throws IOException {
            }

            protected void joinGroup(SocketAddress arg0, NetworkInterface arg1) throws IOException {
            }

            protected void leaveGroup(SocketAddress arg0, NetworkInterface arg1) throws IOException {
            }

            protected void close() {
            }

            public void setOption(int arg0, Object arg1) throws SocketException {
            }

            public Object getOption(int arg0) throws SocketException {
                return null;
            }
        }
        InetAddress i = InetAddress.getByName("127.0.0.1");
        DatagramSocket d = new DatagramSocket(0, i);
        try {
            d.send(new DatagramPacket(new byte[] { 1 }, 1));
            fail("should throw NPE.");
        } catch (NullPointerException e) {
        } finally {
            d.close();
        }
        InetSocketAddress addr = InetSocketAddress.createUnresolved("localhost", 0);
        try {
            DatagramPacket dp = new DatagramPacket(new byte[272], 3, addr);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * If the InetAddress of DatagramPacket is null, DatagramSocket.send(DatagramPacket)
     * should throw NullPointer Exception.
     * @tests java.net.DatagramSocket#send(java.net.DatagramPacket)
     */
    @Test
    public void test_sendLjava_net_DatagramPacket2() throws IOException {
        int udp_port = 20000;
        int send_port = 23000;
        DatagramSocket udpSocket = new DatagramSocket(udp_port);
        byte[] data = { 65 };
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, null, send_port);
        try {
            udpSocket.send(sendPacket);
            fail("Should throw SocketException");
        } catch (NullPointerException e) {
        } finally {
            udpSocket.close();
        }
    }

    /**
     * @tests {@link java.net.DatagramSocket#setDatagramSocketImplFactory(DatagramSocketImplFactory)}
     */
    public void test_set_Datagram_SocketImpl_Factory() throws IOException {
        DatagramSocketImplFactory factory = new MockDatagramSocketImplFactory();
        DatagramSocket.setDatagramSocketImplFactory(factory);
        try {
            DatagramSocket.setDatagramSocketImplFactory(null);
            fail("Should throw SocketException");
        } catch (SocketException e) {
        }
        try {
            DatagramSocket.setDatagramSocketImplFactory(factory);
            fail("Should throw SocketException");
        } catch (SocketException e) {
        }
    }

    private class MockDatagramSocketImplFactory implements DatagramSocketImplFactory {

        public DatagramSocketImpl createDatagramSocketImpl() {
            return new PlainDatagramSocketImpl();
        }
    }

    /**
     * @tests java.net.DatagramSocket#setSendBufferSize(int)
     */
    public void test_setSendBufferSizeI() {
        try {
            int portNumber = Support_PortManager.getNextPortForUDP();
            ds = new java.net.DatagramSocket(portNumber);
            ds.setSendBufferSize(134);
            assertTrue("Incorrect buffer size", ds.getSendBufferSize() >= 134);
            ensureExceptionThrownIfOptionIsUnsupportedOnOS(SO_SNDBUF);
        } catch (Exception e) {
            handleException(e, SO_SNDBUF);
        }
    }

    /**
     * @tests java.net.DatagramSocket#setReceiveBufferSize(int)
     */
    public void test_setReceiveBufferSizeI() {
        try {
            int portNumber = Support_PortManager.getNextPortForUDP();
            ds = new java.net.DatagramSocket(portNumber);
            ds.setReceiveBufferSize(130);
            assertTrue("Incorrect buffer size", ds.getReceiveBufferSize() >= 130);
            ensureExceptionThrownIfOptionIsUnsupportedOnOS(SO_RCVBUF);
        } catch (Exception e) {
            handleException(e, SO_RCVBUF);
        }
    }

    /**
     * @tests java.net.DatagramSocket#setSoTimeout(int)
     */
    public void test_setSoTimeoutI() throws SocketException {
        DatagramSocket ds = new DatagramSocket();
        try {
            ds.setSoTimeout(100);
            assertTrue("Set incorrect timeout", ds.getSoTimeout() >= 100);
            ensureExceptionThrownIfOptionIsUnsupportedOnOS(SO_TIMEOUT);
        } catch (Exception e) {
            handleException(e, SO_TIMEOUT);
        }
    }

    /**
     * @tests java.net.DatagramSocket#DatagramSocket(java.net.DatagramSocketImpl)
     */
    public void test_ConstructorLjava_net_DatagramSocketImpl() {
        class SimpleTestDatagramSocket extends DatagramSocket {

            public SimpleTestDatagramSocket(DatagramSocketImpl impl) {
                super(impl);
            }
        }
        try {
            new SimpleTestDatagramSocket((DatagramSocketImpl) null);
            fail("exception expected");
        } catch (NullPointerException ex) {
        }
    }

    /**
     * @tests java.net.DatagramSocket#DatagramSocket(java.net.SocketAddress)
     */
    public void test_ConstructorLjava_net_SocketAddress() throws Exception {
        class UnsupportedSocketAddress extends SocketAddress {

            public UnsupportedSocketAddress() {
            }
        }
        DatagramSocket ds = new DatagramSocket(new InetSocketAddress(InetAddress.getLocalHost(), 0));
        assertTrue(ds.getBroadcast());
        assertTrue("Created socket with incorrect port", ds.getLocalPort() != 0);
        assertEquals("Created socket with incorrect address", InetAddress.getLocalHost(), ds.getLocalAddress());
        try {
            ds = new java.net.DatagramSocket(new UnsupportedSocketAddress());
            fail("No exception when constructing datagramSocket with unsupported SocketAddress type");
        } catch (IllegalArgumentException e) {
        }
        ds = new DatagramSocket((SocketAddress) null);
        assertTrue(ds.getBroadcast());
    }

    /**
     * @tests java.net.DatagramSocket#bind(java.net.SocketAddress)
     */
    public void test_bindLjava_net_SocketAddress() throws Exception {
        class mySocketAddress extends SocketAddress {

            public mySocketAddress() {
            }
        }
        DatagramServer server = null;
        int[] ports = Support_PortManager.getNextPortsForUDP(3);
        int portNumber = ports[0];
        int serverPortNumber = ports[1];
        DatagramSocket theSocket = new DatagramSocket(new InetSocketAddress(InetAddress.getLocalHost(), portNumber));
        assertTrue("Local address not correct after bind:" + theSocket.getLocalSocketAddress().toString() + "Expected: " + (new InetSocketAddress(InetAddress.getLocalHost(), portNumber)).toString(), theSocket.getLocalSocketAddress().equals(new InetSocketAddress(InetAddress.getLocalHost(), portNumber)));
        InetAddress localHost = InetAddress.getLocalHost();
        portNumber = ports[2];
        DatagramSocket ds = new DatagramSocket(null);
        ds.bind(new InetSocketAddress(localHost, portNumber));
        server = new DatagramServer(serverPortNumber, localHost);
        server.start();
        Thread.sleep(1000);
        ds.connect(new InetSocketAddress(localHost, serverPortNumber));
        byte[] sendBytes = { 'T', 'e', 's', 't', 0 };
        DatagramPacket send = new DatagramPacket(sendBytes, sendBytes.length);
        ds.send(send);
        Thread.sleep(1000);
        ds.close();
        assertTrue("Address in packet sent does not match address bound to:" + server.rdp.getAddress() + ":" + server.rdp.getPort() + ":" + localHost + ":" + portNumber, (server.rdp.getAddress().equals(localHost)) && (server.rdp.getPort() == portNumber));
        theSocket = new DatagramSocket(null);
        theSocket.bind(null);
        assertNotNull("Bind with null did not work", theSocket.getLocalSocketAddress());
        theSocket.close();
        theSocket = new DatagramSocket(null);
        try {
            theSocket.bind(new InetSocketAddress(InetAddress.getByAddress(Support_Configuration.nonLocalAddressBytes), Support_PortManager.getNextPortForUDP()));
            fail("No exception when binding to bad address");
        } catch (SocketException ex) {
        }
        theSocket.close();
        ports = Support_PortManager.getNextPortsForUDP(2);
        theSocket = new DatagramSocket(null);
        DatagramSocket theSocket2 = new DatagramSocket(ports[0]);
        try {
            InetSocketAddress theAddress = new InetSocketAddress(InetAddress.getLocalHost(), ports[1]);
            theSocket.bind(theAddress);
            theSocket2.bind(theAddress);
            fail("No exception binding to address that is not available");
        } catch (SocketException ex) {
        }
        theSocket.close();
        theSocket2.close();
        theSocket = new DatagramSocket(null);
        try {
            theSocket.bind(new mySocketAddress());
            fail("No exception when binding using unsupported SocketAddress subclass");
        } catch (IllegalArgumentException ex) {
        }
        theSocket.close();
        if (server != null) {
            server.stopServer();
        }
    }

    /**
     * @tests java.net.DatagramSocket#connect(java.net.SocketAddress)
     */
    public void test_connectLjava_net_SocketAddress() throws Exception {
        try {
            ds = new java.net.DatagramSocket();
            InetAddress inetAddress = InetAddress.getLocalHost();
            int portNumber = Support_PortManager.getNextPortForUDP();
            ds.connect(new InetSocketAddress(inetAddress, portNumber));
            DatagramPacket send = new DatagramPacket(new byte[10], 10);
            ds.send(send);
            DatagramPacket receive = new DatagramPacket(new byte[20], 20);
            ds.setSoTimeout(10000);
            ds.receive(receive);
            ds.close();
            fail("No PortUnreachableException when connected at native level on recv ");
        } catch (PortUnreachableException e) {
        }
        DatagramServer server = null;
        int[] ports = Support_PortManager.getNextPortsForUDP(3);
        int serverPortNumber = ports[0];
        InetAddress localHost = InetAddress.getLocalHost();
        DatagramSocket ds = new DatagramSocket(ports[1]);
        DatagramSocket ds2 = new DatagramSocket(ports[2]);
        server = new DatagramServer(serverPortNumber, localHost);
        server.start();
        Thread.sleep(1000);
        int port = ds.getLocalPort();
        ds.connect(new InetSocketAddress(localHost, serverPortNumber));
        final byte[] sendBytes = { 'T', 'e', 's', 't', 0 };
        DatagramPacket send = new DatagramPacket(sendBytes, sendBytes.length);
        ds.send(send);
        DatagramPacket receive = new DatagramPacket(new byte[20], 20);
        ds.setSoTimeout(2000);
        ds.receive(receive);
        ds.close();
        assertTrue("Wrong size data received: " + receive.getLength(), receive.getLength() == sendBytes.length);
        assertTrue("Wrong data received" + new String(receive.getData(), 0, receive.getLength()) + ":" + new String(sendBytes), new String(receive.getData(), 0, receive.getLength()).equals(new String(sendBytes)));
        assertTrue("Wrong receiver:" + receive.getAddress() + ":" + localHost, receive.getAddress().equals(localHost));
        if (server != null) {
            server.stopServer();
        }
        try {
            ds = new java.net.DatagramSocket();
            InetAddress inetAddress = InetAddress.getLocalHost();
            int portNumber = Support_PortManager.getNextPortForUDP();
            ds.connect(new InetSocketAddress(inetAddress, portNumber));
            ds.disconnect();
            ds.close();
        } catch (PortUnreachableException e) {
        }
        try {
            ds = new java.net.DatagramSocket();
            InetAddress inetAddress = InetAddress.getLocalHost();
            int portNumber = Support_PortManager.getNextPortForUDP();
            ds.connect(new InetSocketAddress(inetAddress, portNumber));
            DatagramPacket senddp = new DatagramPacket(new byte[10], 10, inetAddress, portNumber + 1);
            ds.send(senddp);
            ds.close();
            fail("No Exception when trying to send to a different address on a connected socket ");
        } catch (IllegalArgumentException e) {
        }
        server = null;
        ports = Support_PortManager.getNextPortsForUDP(3);
        serverPortNumber = ports[0];
        localHost = InetAddress.getLocalHost();
        ds = new DatagramSocket(ports[1]);
        ds2 = new DatagramSocket(ports[2]);
        server = new DatagramServer(serverPortNumber, localHost);
        server.start();
        Thread.sleep(1000);
        port = ds.getLocalPort();
        ds.connect(new InetSocketAddress(localHost, serverPortNumber + 1));
        ds.disconnect();
        ds.connect(new InetSocketAddress(localHost, serverPortNumber));
        send = new DatagramPacket(sendBytes, sendBytes.length);
        ds.send(send);
        receive = new DatagramPacket(new byte[20], 20);
        ds.setSoTimeout(2000);
        ds.receive(receive);
        ds.close();
        assertTrue("connect/disconnect/connect - Wrong size data received: " + receive.getLength(), receive.getLength() == sendBytes.length);
        assertTrue("connect/disconnect/connect - Wrong data received" + new String(receive.getData(), 0, receive.getLength()) + ":" + new String(sendBytes), new String(receive.getData(), 0, receive.getLength()).equals(new String(sendBytes)));
        assertTrue("connect/disconnect/connect - Wrong receiver:" + receive.getAddress() + ":" + localHost, receive.getAddress().equals(localHost));
        if (server != null) {
            server.stopServer();
        }
        server = null;
        ports = Support_PortManager.getNextPortsForUDP(3);
        serverPortNumber = ports[0];
        localHost = InetAddress.getLocalHost();
        ds = new DatagramSocket(ports[1]);
        ds2 = new DatagramSocket(ports[2]);
        server = new DatagramServer(serverPortNumber, localHost);
        server.start();
        Thread.sleep(1000);
        port = ds.getLocalPort();
        ds.connect(new InetSocketAddress(localHost, serverPortNumber + 1));
        ds.disconnect();
        send = new DatagramPacket(sendBytes, sendBytes.length, localHost, serverPortNumber);
        ds.send(send);
        receive = new DatagramPacket(new byte[20], 20);
        ds.setSoTimeout(2000);
        ds.receive(receive);
        ds.close();
        assertTrue("connect/disconnect - Wrong size data received: " + receive.getLength(), receive.getLength() == sendBytes.length);
        assertTrue("connect/disconnect - Wrong data received" + new String(receive.getData(), 0, receive.getLength()) + ":" + new String(sendBytes), new String(receive.getData(), 0, receive.getLength()).equals(new String(sendBytes)));
        assertTrue("connect/disconnect - Wrong receiver:" + receive.getAddress() + ":" + localHost, receive.getAddress().equals(localHost));
        if (server != null) {
            server.stopServer();
        }
        server = null;
        ports = Support_PortManager.getNextPortsForUDP(3);
        serverPortNumber = ports[0];
        localHost = InetAddress.getLocalHost();
        ds = new DatagramSocket(ports[1]);
        ds2 = new DatagramSocket(ports[2]);
        server = new DatagramServer(serverPortNumber, localHost);
        server.start();
        Thread.sleep(1000);
        port = ds.getLocalPort();
        ds.connect(new InetSocketAddress(localHost, serverPortNumber + 1));
        ds.connect(new InetSocketAddress(localHost, serverPortNumber));
        byte[] sendTestBytes = { 'T', 'e', 's', 't', 0 };
        send = new DatagramPacket(sendTestBytes, sendTestBytes.length);
        ds.send(send);
        DatagramPacket receivedp = new DatagramPacket(new byte[20], 20);
        ds.setSoTimeout(2000);
        ds.receive(receivedp);
        ds.close();
        assertTrue("connect/connect - Wrong size data received: " + receivedp.getLength(), receivedp.getLength() == sendTestBytes.length);
        assertTrue("connect/connect - Wrong data received" + new String(receivedp.getData(), 0, receivedp.getLength()) + ":" + new String(sendTestBytes), new String(receivedp.getData(), 0, receivedp.getLength()).equals(new String(sendTestBytes)));
        assertTrue("connect/connect - Wrong receiver:" + receivedp.getAddress() + ":" + localHost, receivedp.getAddress().equals(localHost));
        if (server != null) {
            server.stopServer();
        }
        try {
            ds = new java.net.DatagramSocket();
            byte[] addressBytes = { 0, 0, 0, 0 };
            InetAddress inetAddress = InetAddress.getByAddress(addressBytes);
            int portNumber = Support_PortManager.getNextPortForUDP();
            InetAddress localHostIA = InetAddress.getLocalHost();
            ds.connect(new InetSocketAddress(inetAddress, portNumber));
            assertTrue("Is not connected after connect to inaddr any", ds.isConnected());
            byte[] sendBytesArray = { 'T', 'e', 's', 't', 0 };
            DatagramPacket senddp = new DatagramPacket(sendBytesArray, sendBytesArray.length, localHostIA, portNumber);
            ds.send(senddp);
            fail("No exception when trying to connect at native level with bad address (exception from send)  ");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * @tests java.net.DatagramSocket#isBound()
     */
    public void test_isBound() throws Exception {
        InetAddress addr = InetAddress.getLocalHost();
        int[] ports = Support_PortManager.getNextPortsForUDP(3);
        int port = ports[0];
        DatagramSocket theSocket = new DatagramSocket(ports[1]);
        assertTrue("Socket indicated  not bound when it should be (1)", theSocket.isBound());
        theSocket.close();
        theSocket = new DatagramSocket(new InetSocketAddress(addr, port));
        assertTrue("Socket indicated  not bound when it should be (2)", theSocket.isBound());
        theSocket.close();
        theSocket = new DatagramSocket(null);
        assertFalse("Socket indicated  bound when it should not be (1)", theSocket.isBound());
        theSocket.close();
        theSocket = new DatagramSocket(null);
        theSocket.connect(new InetSocketAddress(addr, port));
        assertTrue("Socket indicated not bound when it should be (3)", theSocket.isBound());
        theSocket.close();
        InetSocketAddress theLocalAddress = new InetSocketAddress(InetAddress.getLocalHost(), ports[2]);
        theSocket = new DatagramSocket(null);
        assertFalse("Socket indicated bound when it should not be (2)", theSocket.isBound());
        theSocket.bind(theLocalAddress);
        assertTrue("Socket indicated not bound when it should be (4)", theSocket.isBound());
        theSocket.close();
        assertTrue("Socket indicated not bound when it should be (5)", theSocket.isBound());
    }

    /**
     * @tests java.net.DatagramSocket#isConnected()
     */
    public void test_isConnected() throws Exception {
        InetAddress addr = InetAddress.getLocalHost();
        int[] ports = Support_PortManager.getNextPortsForUDP(4);
        int port = ports[0];
        DatagramSocket theSocket = new DatagramSocket(ports[1]);
        assertFalse("Socket indicated connected when it should not be", theSocket.isConnected());
        theSocket.connect(new InetSocketAddress(addr, port));
        assertTrue("Socket indicated  not connected when it should be", theSocket.isConnected());
        theSocket.connect(new InetSocketAddress(addr, ports[2]));
        assertTrue("Socket indicated  not connected when it should be", theSocket.isConnected());
        theSocket.disconnect();
        assertFalse("Socket indicated connected when it should not be", theSocket.isConnected());
        theSocket.close();
        theSocket = new DatagramSocket(ports[3]);
        theSocket.connect(new InetSocketAddress(addr, port));
        theSocket.close();
        assertTrue("Socket indicated  not connected when it should be", theSocket.isConnected());
    }

    /**
     * @tests java.net.DatagramSocket#getRemoteSocketAddress()
     */
    public void test_getRemoteSocketAddress() throws Exception {
        int[] ports = Support_PortManager.getNextPortsForUDP(3);
        int sport = ports[0];
        int portNumber = ports[1];
        DatagramSocket s = new DatagramSocket(new InetSocketAddress(InetAddress.getLocalHost(), portNumber));
        s.connect(new InetSocketAddress(InetAddress.getLocalHost(), sport));
        assertTrue("Returned incorrect InetSocketAddress(1):" + s.getLocalSocketAddress().toString(), s.getRemoteSocketAddress().equals(new InetSocketAddress(InetAddress.getLocalHost(), sport)));
        s.close();
        DatagramSocket theSocket = new DatagramSocket(null);
        portNumber = ports[2];
        theSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), portNumber));
        assertNull("Returned incorrect InetSocketAddress -unconnected socket:" + "Expected: NULL", theSocket.getRemoteSocketAddress());
        theSocket.connect(new InetSocketAddress(InetAddress.getLocalHost(), sport));
        assertTrue("Returned incorrect InetSocketAddress(2):" + theSocket.getRemoteSocketAddress().toString(), theSocket.getRemoteSocketAddress().equals(new InetSocketAddress(InetAddress.getLocalHost(), sport)));
        theSocket.close();
    }

    /**
     * @tests java.net.DatagramSocket#getLocalSocketAddress()
     */
    public void test_getLocalSocketAddress() throws Exception {
        int portNumber = Support_PortManager.getNextPortForUDP();
        DatagramSocket s = new DatagramSocket(new InetSocketAddress(InetAddress.getLocalHost(), portNumber));
        assertTrue("Returned incorrect InetSocketAddress(1):" + s.getLocalSocketAddress().toString() + "Expected: " + (new InetSocketAddress(InetAddress.getLocalHost(), portNumber)).toString(), s.getLocalSocketAddress().equals(new InetSocketAddress(InetAddress.getLocalHost(), portNumber)));
        s.close();
        InetSocketAddress remoteAddress = (InetSocketAddress) s.getRemoteSocketAddress();
        DatagramSocket theSocket = new DatagramSocket(null);
        assertNull("Returned incorrect InetSocketAddress -unbound socket- Expected null", theSocket.getLocalSocketAddress());
        portNumber = Support_PortManager.getNextPortForUDP();
        theSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), portNumber));
        assertTrue("Returned incorrect InetSocketAddress(2):" + theSocket.getLocalSocketAddress().toString() + "Expected: " + (new InetSocketAddress(InetAddress.getLocalHost(), portNumber)).toString(), theSocket.getLocalSocketAddress().equals(new InetSocketAddress(InetAddress.getLocalHost(), portNumber)));
        theSocket.close();
        s = new DatagramSocket(0);
        String preferIPv4StackValue = System.getProperty("java.net.preferIPv4Stack");
        String preferIPv6AddressesValue = System.getProperty("java.net.preferIPv6Addresses");
        if (((preferIPv4StackValue == null) || preferIPv4StackValue.equalsIgnoreCase("false")) && (preferIPv6AddressesValue != null) && (preferIPv6AddressesValue.equals("true"))) {
            assertTrue("ANY address not returned correctly with preferIPv6Addresses=true, preferIPv4Stack=false " + s.getLocalSocketAddress(), ((InetSocketAddress) s.getLocalSocketAddress()).getAddress() instanceof Inet6Address);
        } else {
            assertTrue("ANY address not returned correctly with preferIPv6Addresses=true, preferIPv4Stack=true " + s.getLocalSocketAddress(), ((InetSocketAddress) s.getLocalSocketAddress()).getAddress() instanceof Inet4Address);
        }
        s.close();
    }

    /**
     * @tests java.net.DatagramSocket#setReuseAddress(boolean)
     */
    public void test_setReuseAddressZ() throws Exception {
        try {
            DatagramSocket theSocket1 = null;
            DatagramSocket theSocket2 = null;
            try {
                InetSocketAddress theAddress = new InetSocketAddress(InetAddress.getLocalHost(), Support_PortManager.getNextPortForUDP());
                theSocket1 = new DatagramSocket(null);
                theSocket2 = new DatagramSocket(null);
                theSocket1.setReuseAddress(false);
                theSocket2.setReuseAddress(false);
                theSocket1.bind(theAddress);
                theSocket2.bind(theAddress);
                fail("No exception when trying to connect to do duplicate socket bind with re-useaddr set to false");
            } catch (BindException e) {
            }
            if (theSocket1 != null) theSocket1.close();
            if (theSocket2 != null) theSocket2.close();
            InetSocketAddress theAddress = new InetSocketAddress(InetAddress.getLocalHost(), Support_PortManager.getNextPortForUDP());
            theSocket1 = new DatagramSocket(null);
            theSocket2 = new DatagramSocket(null);
            theSocket1.setReuseAddress(true);
            theSocket2.setReuseAddress(true);
            theSocket1.bind(theAddress);
            theSocket2.bind(theAddress);
            if (theSocket1 != null) theSocket1.close();
            if (theSocket2 != null) theSocket2.close();
            try {
                theAddress = new InetSocketAddress(InetAddress.getLocalHost(), Support_PortManager.getNextPortForUDP());
                theSocket1 = new DatagramSocket(null);
                theSocket2 = new DatagramSocket(null);
                theSocket1.bind(theAddress);
                theSocket2.bind(theAddress);
                fail("No exception when trying to connect to do duplicate socket bind with re-useaddr left as default");
            } catch (BindException e) {
            }
            if (theSocket1 != null) theSocket1.close();
            if (theSocket2 != null) theSocket2.close();
            ensureExceptionThrownIfOptionIsUnsupportedOnOS(SO_REUSEADDR);
        } catch (Exception e) {
            handleException(e, SO_REUSEADDR);
        }
    }

    /**
     * @tests java.net.DatagramSocket#getReuseAddress()
     */
    public void test_getReuseAddress() {
        try {
            DatagramSocket theSocket = new DatagramSocket();
            theSocket.setReuseAddress(true);
            assertTrue("getReuseAddress false when it should be true", theSocket.getReuseAddress());
            theSocket.setReuseAddress(false);
            assertFalse("getReuseAddress true when it should be False", theSocket.getReuseAddress());
            ensureExceptionThrownIfOptionIsUnsupportedOnOS(SO_REUSEADDR);
        } catch (Exception e) {
            handleException(e, SO_REUSEADDR);
        }
    }

    /**
     * @tests java.net.DatagramSocket#setBroadcast(boolean)
     */
    public void test_setBroadcastZ() {
        try {
            int[] ports = Support_PortManager.getNextPortsForUDP(3);
            DatagramSocket theSocket = new DatagramSocket(ports[0]);
            theSocket.setBroadcast(false);
            byte theBytes[] = { -1, -1, -1, -1 };
            try {
                theSocket.connect(new InetSocketAddress(InetAddress.getByAddress(theBytes), ports[1]));
                assertFalse("No exception when connecting to broadcast address with setBroadcast(false)", theSocket.getBroadcast());
            } catch (Exception ex) {
            }
            theSocket.setBroadcast(true);
            theSocket.connect(new InetSocketAddress(InetAddress.getByAddress(theBytes), ports[2]));
            ensureExceptionThrownIfOptionIsUnsupportedOnOS(SO_BROADCAST);
        } catch (Exception e) {
            handleException(e, SO_BROADCAST);
        }
    }

    /**
     * @tests java.net.DatagramSocket#getBroadcast()
     */
    public void test_getBroadcast() {
        try {
            DatagramSocket theSocket = new DatagramSocket();
            theSocket.setBroadcast(true);
            assertTrue("getBroadcast false when it should be true", theSocket.getBroadcast());
            theSocket.setBroadcast(false);
            assertFalse("getBroadcast true when it should be False", theSocket.getBroadcast());
            ensureExceptionThrownIfOptionIsUnsupportedOnOS(SO_BROADCAST);
        } catch (Exception e) {
            handleException(e, SO_BROADCAST);
        }
    }

    /**
     * @tests java.net.DatagramSocket#setTrafficClass(int)
     */
    public void test_setTrafficClassI() {
        try {
            int IPTOS_LOWCOST = 0x2;
            int IPTOS_RELIABILTY = 0x4;
            int IPTOS_THROUGHPUT = 0x8;
            int IPTOS_LOWDELAY = 0x10;
            int[] ports = Support_PortManager.getNextPortsForUDP(2);
            new InetSocketAddress(InetAddress.getLocalHost(), ports[0]);
            DatagramSocket theSocket = new DatagramSocket(ports[1]);
            try {
                theSocket.setTrafficClass(256);
                fail("No exception when traffic class set to 256");
            } catch (IllegalArgumentException e) {
            }
            try {
                theSocket.setTrafficClass(-1);
                fail("No exception when traffic class set to -1");
            } catch (IllegalArgumentException e) {
            }
            theSocket.setTrafficClass(IPTOS_LOWCOST);
            theSocket.setTrafficClass(IPTOS_THROUGHPUT);
            ensureExceptionThrownIfOptionIsUnsupportedOnOS(IP_TOS);
        } catch (Exception e) {
            handleException(e, IP_TOS);
        }
    }

    /**
     * @tests java.net.DatagramSocket#getTrafficClass()
     */
    public void test_getTrafficClass() {
        try {
            int IPTOS_LOWCOST = 0x2;
            int IPTOS_RELIABILTY = 0x4;
            int IPTOS_THROUGHPUT = 0x8;
            int IPTOS_LOWDELAY = 0x10;
            int[] ports = Support_PortManager.getNextPortsForUDP(2);
            new InetSocketAddress(InetAddress.getLocalHost(), ports[0]);
            DatagramSocket theSocket = new DatagramSocket(ports[1]);
            int trafficClass = theSocket.getTrafficClass();
        } catch (Exception e) {
            handleException(e, IP_TOS);
        }
    }

    /**
     * @tests java.net.DatagramSocket#isClosed()
     */
    public void test_isClosed() throws Exception {
        DatagramSocket theSocket = new DatagramSocket();
        assertFalse("Socket should indicate it is not closed(1):", theSocket.isClosed());
        theSocket.close();
        assertTrue("Socket should indicate it is not closed(1):", theSocket.isClosed());
        InetSocketAddress theAddress = new InetSocketAddress(InetAddress.getLocalHost(), Support_PortManager.getNextPortForUDP());
        theSocket = new DatagramSocket(theAddress);
        assertFalse("Socket should indicate it is not closed(2):", theSocket.isClosed());
        theSocket.close();
        assertTrue("Socket should indicate it is not closed(2):", theSocket.isClosed());
    }

    /**
     * @tests java.net.DatagramSocket#getChannel()
     */
    public void test_getChannel() throws SocketException {
        assertNull(new DatagramSocket().getChannel());
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
        retval = "Bogus retval";
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
        try {
            ds.close();
            sds.close();
        } catch (Exception e) {
        }
    }

    protected void receive_oversize_java_net_DatagramPacket() {
        final int[] ports = Support_PortManager.getNextPortsForUDP(2);
        final int portNumber = ports[0];
        class TestDGRcvOver implements Runnable {

            public void run() {
                InetAddress localHost = null;
                try {
                    localHost = InetAddress.getLocalHost();
                    Thread.sleep(1000);
                    DatagramSocket sds = new DatagramSocket(ports[1]);
                    DatagramPacket rdp = new DatagramPacket("0123456789".getBytes(), 10, localHost, portNumber);
                    sds.send(rdp);
                    sds.close();
                } catch (Exception e) {
                    System.err.println("host " + localHost + " port " + portNumber + " failed to send oversize data: " + e);
                    e.printStackTrace();
                }
            }
        }
        try {
            new Thread(new TestDGRcvOver(), "DGSenderOver").start();
            ds = new java.net.DatagramSocket(portNumber);
            ds.setSoTimeout(6000);
            byte rbuf[] = new byte[5];
            DatagramPacket rdp = new DatagramPacket(rbuf, rbuf.length);
            ;
            ds.receive(rdp);
            ds.close();
            assertTrue("Send/Receive oversize failed to return correct data: " + new String(rbuf, 0, 5), new String(rbuf, 0, 5).equals("01234"));
        } catch (Exception e) {
            System.err.println("Exception during send test: " + e);
            e.printStackTrace();
            fail("port " + portNumber + " Exception: " + e + " during oversize send test");
        } finally {
            ds.close();
        }
    }
}
