package org.jgentleframework.utils.network.sockets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * The Class ZipCompressingSocket.
 * 
 * @author Quoc Chung - mailto: <a
 *         href="mailto:skydunkpro@yahoo.com">skydunkpro@yahoo.com</a>
 * @date Feb 13, 2009
 * @see AbstractCompressingSocket
 */
public class ZipCompressingSocket extends AbstractCompressingSocket {

    /**
	 * Instantiates a new compressing socket.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
    public ZipCompressingSocket() throws IOException {
    }

    /**
	 * Instantiates a new compressing socket.
	 * 
	 * @param host
	 *            the host
	 * @param port
	 *            the port
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
    public ZipCompressingSocket(String host, int port) throws IOException {
        super(host, port);
    }

    public InputStream getInputStream() throws IOException {
        if (null == compressingInputStream) {
            InputStream originalInputStream = super.getInputStream();
            compressingInputStream = new ZipInputStream(originalInputStream);
            ((ZipInputStream) compressingInputStream).getNextEntry();
        }
        return compressingInputStream;
    }

    public OutputStream getOutputStream() throws IOException {
        if (null == compressingOutputStream) {
            OutputStream originalOutputStream = super.getOutputStream();
            compressingOutputStream = new ZipOutputStream(originalOutputStream);
            ((ZipOutputStream) compressingOutputStream).putNextEntry(new ZipEntry("dummy"));
        }
        return compressingOutputStream;
    }
}
