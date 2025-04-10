package xbird.util.xfer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import xbird.util.datetime.StopWatch;
import xbird.util.io.FastByteArrayOutputStream;
import xbird.util.io.IOUtils;
import xbird.util.lang.PrintUtils;
import xbird.util.net.NetUtils;

/**
 * 
 * <DIV lang="en"></DIV>
 * <DIV lang="ja"></DIV>
 * 
 * @author Makoto YUI (yuin405+xbird@gmail.com)
 */
public final class TransferUtils {

    private static final Log LOG = LogFactory.getLog(TransferUtils.class);

    private TransferUtils() {
    }

    public static void sendfile(@Nonnull final File file, @Nonnull final InetAddress dstAddr, final int dstPort, final boolean append, final boolean sync) throws IOException {
        sendfile(file, null, dstAddr, dstPort, append, sync);
    }

    public static void sendfile(@Nonnull final File file, @Nullable final String writeDirPath, @Nonnull final InetAddress dstAddr, final int dstPort, final boolean append, final boolean sync) throws IOException {
        sendfile(file, writeDirPath, dstAddr, dstPort, append, sync, null);
    }

    public static void sendfile(@Nonnull final File file, @Nullable final String writeDirPath, @Nonnull final InetAddress dstAddr, final int dstPort, final boolean append, final boolean sync, @Nonnull final TransferClientHandler handler) throws IOException {
        sendfile(file, 0L, -1L, writeDirPath, dstAddr, dstPort, append, sync, handler);
    }

    public static void sendfile(@Nonnull final File file, final long fromPos, final long count, @Nullable final String writeDirPath, @Nonnull final InetAddress dstAddr, final int dstPort, final boolean append, final boolean sync, @Nonnull final TransferClientHandler handler) throws IOException {
        if (!file.exists()) {
            throw new IllegalArgumentException(file.getAbsolutePath() + " does not exist");
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException(file.getAbsolutePath() + " is not file");
        }
        if (!file.canRead()) {
            throw new IllegalArgumentException(file.getAbsolutePath() + " cannot read");
        }
        final SocketAddress dstSockAddr = new InetSocketAddress(dstAddr, dstPort);
        SocketChannel channel = null;
        Socket socket = null;
        final OutputStream out;
        try {
            channel = SocketChannel.open();
            socket = channel.socket();
            socket.connect(dstSockAddr);
            out = socket.getOutputStream();
        } catch (IOException e) {
            LOG.error("failed to connect: " + dstSockAddr, e);
            IOUtils.closeQuietly(channel);
            NetUtils.closeQuietly(socket);
            throw e;
        }
        DataInputStream din = null;
        if (sync) {
            InputStream in = socket.getInputStream();
            din = new DataInputStream(in);
        }
        final DataOutputStream dos = new DataOutputStream(out);
        final StopWatch sw = new StopWatch();
        FileInputStream src = null;
        final long nbytes;
        try {
            src = new FileInputStream(file);
            FileChannel fc = src.getChannel();
            String fileName = file.getName();
            IOUtils.writeString(fileName, dos);
            IOUtils.writeString(writeDirPath, dos);
            long xferBytes = (count == -1L) ? fc.size() : count;
            dos.writeLong(xferBytes);
            dos.writeBoolean(append);
            dos.writeBoolean(sync);
            if (handler == null) {
                dos.writeBoolean(false);
            } else {
                dos.writeBoolean(true);
                handler.writeAdditionalHeader(dos);
            }
            nbytes = fc.transferTo(fromPos, xferBytes, channel);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Sent a file '" + file.getAbsolutePath() + "' of " + nbytes + " bytes to " + dstSockAddr.toString() + " in " + sw.toString());
            }
            if (sync) {
                long remoteRecieved = din.readLong();
                if (remoteRecieved != xferBytes) {
                    throw new IllegalStateException("Sent " + xferBytes + " bytes, but remote node received " + remoteRecieved + " bytes");
                }
            }
        } catch (FileNotFoundException e) {
            LOG.error(PrintUtils.prettyPrintStackTrace(e, -1));
            throw e;
        } catch (IOException e) {
            LOG.error(PrintUtils.prettyPrintStackTrace(e, -1));
            throw e;
        } finally {
            IOUtils.closeQuietly(src);
            IOUtils.closeQuietly(din, dos);
            IOUtils.closeQuietly(channel);
            NetUtils.closeQuietly(socket);
        }
    }

    public static void send(@Nonnull final FastByteArrayOutputStream data, @Nonnull final String fileName, @Nonnull final InetAddress dstAddr, final int dstPort, final boolean append, final boolean sync) throws IOException {
        send(data, fileName, null, dstAddr, dstPort, append, sync);
    }

    public static void send(@Nonnull final FastByteArrayOutputStream data, @Nonnull final String fileName, @Nullable final String writeDirPath, @Nonnull final InetAddress dstAddr, final int dstPort, final boolean append, final boolean sync) throws IOException {
        send(data, fileName, writeDirPath, dstAddr, dstPort, append, sync, null);
    }

    public static void send(@Nonnull final FastByteArrayOutputStream data, @Nonnull final String fileName, @Nullable final String writeDirPath, @Nonnull final InetAddress dstAddr, final int dstPort, final boolean append, final boolean sync, @Nullable final TransferClientHandler handler) throws IOException {
        final SocketAddress dstSockAddr = new InetSocketAddress(dstAddr, dstPort);
        SocketChannel channel = null;
        Socket socket = null;
        final OutputStream out;
        try {
            channel = SocketChannel.open();
            socket = channel.socket();
            socket.connect(dstSockAddr);
            out = socket.getOutputStream();
        } catch (IOException e) {
            LOG.error("failed to connect: " + dstSockAddr, e);
            IOUtils.closeQuietly(channel);
            NetUtils.closeQuietly(socket);
            throw e;
        }
        DataInputStream din = null;
        if (sync) {
            InputStream in = socket.getInputStream();
            din = new DataInputStream(in);
        }
        final DataOutputStream dos = new DataOutputStream(out);
        final StopWatch sw = new StopWatch();
        try {
            IOUtils.writeString(fileName, dos);
            IOUtils.writeString(writeDirPath, dos);
            long nbytes = data.size();
            dos.writeLong(nbytes);
            dos.writeBoolean(append);
            dos.writeBoolean(sync);
            if (handler == null) {
                dos.writeBoolean(false);
            } else {
                dos.writeBoolean(true);
                handler.writeAdditionalHeader(dos);
            }
            data.writeTo(out);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Sent a file data '" + fileName + "' of " + nbytes + " bytes to " + dstSockAddr.toString() + " in " + sw.toString());
            }
            if (sync) {
                long remoteRecieved = din.readLong();
                if (remoteRecieved != nbytes) {
                    throw new IllegalStateException("Sent " + nbytes + " bytes, but remote node received " + remoteRecieved + " bytes");
                }
            }
        } catch (FileNotFoundException e) {
            LOG.error(PrintUtils.prettyPrintStackTrace(e, -1));
            throw e;
        } catch (IOException e) {
            LOG.error(PrintUtils.prettyPrintStackTrace(e, -1));
            throw e;
        } finally {
            IOUtils.closeQuietly(din, dos);
            IOUtils.closeQuietly(channel);
            NetUtils.closeQuietly(socket);
        }
    }
}
