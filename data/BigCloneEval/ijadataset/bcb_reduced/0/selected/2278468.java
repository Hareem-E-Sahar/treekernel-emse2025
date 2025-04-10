package org.xsocket.connection;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import org.junit.Assert;
import org.junit.Test;
import org.xsocket.MaxReadSizeExceededException;
import org.xsocket.QAUtil;

/**
*
* @author grro@xsocket.org
*/
public final class CompatibilityFileChannelTest {

    @Test
    public void testNonBlockingTransferFrom() throws Exception {
        ServerHandler srvHdl = new ServerHandler();
        IServer server = new Server(srvHdl);
        server.start();
        INonBlockingConnection clientCon = new NonBlockingConnection("localhost", server.getLocalPort());
        QAUtil.sleep(1000);
        INonBlockingConnection serverCon = srvHdl.getConection();
        serverCon.setAutoflush(false);
        File file = QAUtil.createTempfile();
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel fc = raf.getChannel();
        String txt = "Hello my client\r\n";
        serverCon.write(txt);
        serverCon.flush();
        QAUtil.sleep(200);
        long transfered = fc.transferFrom(clientCon, 0, 9000000);
        fc.close();
        raf.close();
        Assert.assertEquals(txt.length(), transfered);
        Assert.assertTrue(QAUtil.isEquals(file, txt));
        file.delete();
        clientCon.close();
        server.close();
    }

    @Test
    public void testBlockingTransferFromServerClosed() throws Exception {
        ServerHandler srvHdl = new ServerHandler();
        IServer server = new Server(srvHdl);
        server.start();
        IBlockingConnection clientCon = new BlockingConnection("localhost", server.getLocalPort());
        QAUtil.sleep(1000);
        final INonBlockingConnection serverCon = srvHdl.getConection();
        serverCon.setAutoflush(false);
        File file = QAUtil.createTempfile();
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel fc = raf.getChannel();
        String txt = "Hello my client\r\n";
        serverCon.write(txt);
        serverCon.flush();
        Thread t = new Thread() {

            @Override
            public void run() {
                QAUtil.sleep(300);
                try {
                    serverCon.close();
                } catch (IOException ioe) {
                    ioe.toString();
                }
                ;
            }
        };
        t.start();
        long transfered = fc.transferFrom(clientCon, 0, 9000000);
        fc.close();
        raf.close();
        Assert.assertEquals(txt.length(), transfered);
        Assert.assertTrue(QAUtil.isEquals(file, txt));
        Assert.assertFalse(clientCon.isOpen());
        file.delete();
        server.close();
    }

    @Test
    public void testNonBlockingTransferFromServerClosed() throws Exception {
        ServerHandler srvHdl = new ServerHandler();
        IServer server = new Server(srvHdl);
        server.start();
        INonBlockingConnection clientCon = new NonBlockingConnection("localhost", server.getLocalPort());
        QAUtil.sleep(1000);
        INonBlockingConnection serverCon = srvHdl.getConection();
        serverCon.setAutoflush(false);
        File file = QAUtil.createTempfile();
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel fc = raf.getChannel();
        String txt = "Hello my client\r\n";
        serverCon.write(txt);
        serverCon.flush();
        serverCon.close();
        QAUtil.sleep(1000);
        long transfered = fc.transferFrom(clientCon, 0, 9000000);
        fc.close();
        raf.close();
        Assert.assertEquals(txt.length(), transfered);
        Assert.assertTrue(QAUtil.isEquals(file, txt));
        file.delete();
        clientCon.close();
    }

    @Test
    public void testNonBlockingTransferFromClientClosed() throws Exception {
        ServerHandler srvHdl = new ServerHandler();
        IServer server = new Server(srvHdl);
        server.start();
        INonBlockingConnection clientCon = new NonBlockingConnection("localhost", server.getLocalPort());
        QAUtil.sleep(1000);
        INonBlockingConnection serverCon = srvHdl.getConection();
        serverCon.setAutoflush(false);
        File file = QAUtil.createTempfile();
        System.out.println(file.getAbsolutePath());
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel fc = raf.getChannel();
        String txt = "Hello my client\r\n";
        serverCon.write(txt);
        serverCon.flush();
        QAUtil.sleep(1000);
        clientCon.close();
        try {
            fc.transferFrom(clientCon, 0, 9000000);
            Assert.fail("ClosedChannelException expected");
        } catch (ClosedChannelException expected) {
        }
        file.delete();
        server.close();
        clientCon.close();
    }

    @Test
    public void testBlockingTransferFromClientClosed() throws Exception {
        ServerHandler srvHdl = new ServerHandler();
        IServer server = new Server(srvHdl);
        server.start();
        IBlockingConnection clientCon = new BlockingConnection("localhost", server.getLocalPort());
        QAUtil.sleep(1000);
        INonBlockingConnection serverCon = srvHdl.getConection();
        serverCon.setAutoflush(false);
        File file = QAUtil.createTempfile();
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel fc = raf.getChannel();
        String txt = "Hello my client\r\n";
        serverCon.write(txt);
        serverCon.flush();
        QAUtil.sleep(200);
        clientCon.close();
        try {
            fc.transferFrom(clientCon, 0, 9000000);
            Assert.fail("ClosedChannelException expected");
        } catch (ClosedChannelException expected) {
        }
        file.delete();
        server.close();
        clientCon.close();
    }

    @Test
    public void testTransferTo() throws Exception {
        ServerHandler srvHdl = new ServerHandler();
        IServer server = new Server(srvHdl);
        server.start();
        INonBlockingConnection clientCon = new NonBlockingConnection("localhost", server.getLocalPort());
        QAUtil.sleep(1000);
        INonBlockingConnection serverCon = srvHdl.getConection();
        File file = QAUtil.createTestfile_40k();
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        FileChannel fc = raf.getChannel();
        fc.transferTo(0, fc.size(), clientCon);
        QAUtil.sleep(200);
        ByteBuffer[] buffer = serverCon.readByteBufferByLength(serverCon.available());
        Assert.assertTrue(QAUtil.isEquals(file, buffer));
        raf.close();
        file.delete();
        clientCon.close();
        server.close();
    }

    @Test
    public void testTransferToSourceClosed() throws Exception {
        ServerHandler srvHdl = new ServerHandler();
        IServer server = new Server(srvHdl);
        server.start();
        INonBlockingConnection clientCon = new NonBlockingConnection("localhost", server.getLocalPort());
        QAUtil.sleep(1000);
        INonBlockingConnection serverCon = srvHdl.getConection();
        File file = QAUtil.createTestfile_40k();
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        FileChannel fc = raf.getChannel();
        fc.transferTo(0, fc.size(), clientCon);
        clientCon.close();
        QAUtil.sleep(1000);
        ByteBuffer[] buffer = serverCon.readByteBufferByLength(serverCon.available());
        Assert.assertTrue(QAUtil.isEquals(file, buffer));
        raf.close();
        file.delete();
        clientCon.close();
        server.close();
    }

    private static final class ServerHandler implements IConnectHandler {

        private INonBlockingConnection connection = null;

        public boolean onConnect(INonBlockingConnection connection) throws IOException, BufferUnderflowException, MaxReadSizeExceededException {
            this.connection = connection;
            return true;
        }

        INonBlockingConnection getConection() {
            return connection;
        }
    }
}
