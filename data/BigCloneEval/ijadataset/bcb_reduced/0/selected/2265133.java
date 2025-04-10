package org.drftpd.tests;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;

/**
 * @author mog
 * @version $Id: DummySocket.java 1764 2007-08-04 02:01:21Z tdsoul $
 */
public class DummySocket extends Socket {

    private ByteArrayOutputStream _out = new ByteArrayOutputStream();

    public DummySocket() {
    }

    public DummySocket(SocketImpl impl) throws SocketException {
        throw new UnsupportedOperationException();
    }

    public DummySocket(String host, int port) throws UnknownHostException, IOException {
        throw new UnsupportedOperationException();
    }

    public DummySocket(InetAddress address, int port) throws IOException {
        throw new UnsupportedOperationException();
    }

    public DummySocket(String host, int port, InetAddress localAddr, int localPort) throws IOException {
        throw new UnsupportedOperationException();
    }

    public DummySocket(InetAddress address, int port, InetAddress localAddr, int localPort) throws IOException {
        throw new UnsupportedOperationException();
    }

    public DummySocket(String host, int port, boolean stream) throws IOException {
        throw new UnsupportedOperationException();
    }

    public DummySocket(InetAddress host, int port, boolean stream) throws IOException {
        throw new UnsupportedOperationException();
    }

    public ByteArrayOutputStream getByteArrayOutputStream() {
        return _out;
    }

    /**
     *
     */
    public void bind(SocketAddress bindpoint) throws IOException {
        throw new UnsupportedOperationException();
    }

    public synchronized void close() throws IOException {
    }

    /**
     *
     */
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void connect(SocketAddress endpoint) throws IOException {
    }

    /**
     *
     */
    public SocketChannel getChannel() {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public InetAddress getInetAddress() {
        return getLocalAddress();
    }

    /**
     *
     */
    public InputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public boolean getKeepAlive() throws SocketException {
        throw new UnsupportedOperationException();
    }

    public InetAddress getLocalAddress() {
        try {
            return InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     */
    public int getLocalPort() {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public SocketAddress getLocalSocketAddress() {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public boolean getOOBInline() throws SocketException {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public OutputStream getOutputStream() throws IOException {
        return _out;
    }

    /**
     *
     */
    public int getPort() {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public synchronized int getReceiveBufferSize() throws SocketException {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public SocketAddress getRemoteSocketAddress() {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public boolean getReuseAddress() throws SocketException {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public synchronized int getSendBufferSize() throws SocketException {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public int getSoLinger() throws SocketException {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public synchronized int getSoTimeout() throws SocketException {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public boolean getTcpNoDelay() throws SocketException {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public int getTrafficClass() throws SocketException {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public boolean isBound() {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public boolean isClosed() {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public boolean isConnected() {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public boolean isInputShutdown() {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public boolean isOutputShutdown() {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public void sendUrgentData(int data) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public void setKeepAlive(boolean on) throws SocketException {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public void setOOBInline(boolean on) throws SocketException {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public synchronized void setReceiveBufferSize(int size) throws SocketException {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public void setReuseAddress(boolean on) throws SocketException {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public synchronized void setSendBufferSize(int size) throws SocketException {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public void setSoLinger(boolean on, int linger) throws SocketException {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public synchronized void setSoTimeout(int timeout) throws SocketException {
    }

    /**
     *
     */
    public void setTcpNoDelay(boolean on) throws SocketException {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public void setTrafficClass(int tc) throws SocketException {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public void shutdownInput() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public void shutdownOutput() throws IOException {
        throw new UnsupportedOperationException();
    }
}
