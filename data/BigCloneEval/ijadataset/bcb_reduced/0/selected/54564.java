package org.jgentleframework.utils.network.sockets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * The Class ZipSocket.
 * 
 * @author Quoc Chung - mailto: <a
 *         href="mailto:skydunkpro@yahoo.com">skydunkpro@yahoo.com</a>
 * @date Feb 19, 2009
 */
public class ZipSocket extends Socket {

    /** The in. */
    private InputStream in;

    /** The out. */
    private OutputStream out;

    /**
	 * Instantiates a new zip socket.
	 */
    public ZipSocket() {
        super();
    }

    /**
	 * Instantiates a new zip socket.
	 * 
	 * @param host
	 *            the host
	 * @param port
	 *            the port
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
    public ZipSocket(String host, int port) throws IOException {
        super(host, port);
    }

    public InputStream getInputStream() throws IOException {
        if (in == null) {
            in = new ZipInputStream(super.getInputStream());
            ((ZipInputStream) in).getNextEntry();
        }
        return in;
    }

    public OutputStream getOutputStream() throws IOException {
        if (out == null) {
            out = new ZipOutputStream(super.getOutputStream());
            ((ZipOutputStream) out).putNextEntry(new ZipEntry("dummy"));
        }
        return out;
    }

    public void close() throws IOException {
        OutputStream o = getOutputStream();
        o.flush();
        ((ZipOutputStream) o).closeEntry();
        ((ZipOutputStream) o).finish();
        super.close();
    }
}
