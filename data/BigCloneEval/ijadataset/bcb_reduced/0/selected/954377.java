package org.exist.debuggee.dbgp.packets;

import org.apache.mina.core.session.IoSession;
import org.exist.Database;
import org.exist.EXistException;
import org.exist.debuggee.dbgp.Errors;
import org.exist.dom.BinaryDocument;
import org.exist.dom.DocumentImpl;
import org.exist.security.PermissionDeniedException;
import org.exist.storage.DBBroker;
import org.exist.storage.lock.Lock;
import org.exist.util.Base64Encoder;
import org.exist.xmldb.XmldbURI;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author <a href="mailto:wolfgang@exist-db.org">Wolfgang Meier</a>
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class Source extends Command {

    /**
	 * file URI 
	 */
    private String fileURI;

    /**
	 * begin line
	 */
    private Integer lineBegin = null;

    /**
	 * end line
	 */
    private Integer lineEnd = null;

    private boolean success = false;

    private Exception exception = null;

    private byte[] source;

    private byte[] response = null;

    public Source(IoSession session, String args) {
        super(session, args);
    }

    @Override
    protected void setArgument(String arg, String val) {
        if (arg.equals("f")) fileURI = val; else if (arg.equals("b")) lineBegin = Integer.valueOf(val); else if (arg.equals("e")) lineEnd = Integer.valueOf(val); else super.setArgument(arg, val);
    }

    @Override
    public void exec() {
        if (fileURI == null) return;
        InputStream is = null;
        try {
            if (fileURI.toLowerCase().startsWith("dbgp://")) {
                String uri = fileURI.substring(7);
                if (uri.toLowerCase().startsWith("file/")) {
                    uri = fileURI.substring(5);
                    is = new FileInputStream(new File(uri));
                } else {
                    XmldbURI pathUri = XmldbURI.create(URLDecoder.decode(fileURI.substring(15), "UTF-8"));
                    Database db = getJoint().getContext().getDatabase();
                    DBBroker broker = null;
                    try {
                        broker = db.getBroker();
                        DocumentImpl resource = broker.getXMLResource(pathUri, Lock.READ_LOCK);
                        if (resource.getResourceType() == DocumentImpl.BINARY_FILE) {
                            is = broker.getBinaryResource((BinaryDocument) resource);
                        } else {
                            return;
                        }
                    } catch (EXistException e) {
                        exception = e;
                    } finally {
                        db.release(broker);
                    }
                }
            } else {
                URL url = new URL(fileURI);
                URLConnection conn = url.openConnection();
                is = conn.getInputStream();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[256];
            int c;
            while ((c = is.read(buf)) > -1) {
                baos.write(buf, 0, c);
            }
            source = baos.toByteArray();
            success = true;
        } catch (MalformedURLException e) {
            exception = e;
        } catch (IOException e) {
            exception = e;
        } catch (PermissionDeniedException e) {
            exception = e;
        } finally {
            if (is != null) try {
                is.close();
            } catch (IOException e) {
                if (exception == null) exception = e;
            }
        }
    }

    public byte[] responseBytes() {
        if (exception != null) {
            String url = "NULL";
            if (fileURI != null) url = fileURI;
            response = errorBytes("source", Errors.ERR_100, exception.getMessage() + " (URL:" + url + ")");
        } else if (response == null) {
            if (source != null) {
                try {
                    String head = xml_declaration + "<response " + namespaces + "command=\"source\" " + "success=\"" + getSuccessString() + "\" " + "encoding=\"base64\" " + "transaction_id=\"" + transactionID + "\"><![CDATA[";
                    String tail = "]]></response>";
                    Base64Encoder enc = new Base64Encoder();
                    enc.translate(source);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    baos.write(head.getBytes());
                    baos.write(new String(enc.getCharArray()).getBytes());
                    baos.write(tail.getBytes());
                    response = baos.toByteArray();
                } catch (IOException e) {
                    response = errorBytes("source");
                }
            } else {
                response = errorBytes("source", Errors.ERR_100, Errors.ERR_100_STR);
            }
        }
        return response;
    }

    private String getSuccessString() {
        if (success) return "1";
        return "0";
    }

    public byte[] commandBytes() {
        String command = "source" + " -i " + transactionID + " -f " + fileURI;
        if (lineBegin != null) command += " -b " + String.valueOf(lineBegin);
        if (lineEnd != null) command += " -e " + String.valueOf(lineEnd);
        return command.getBytes();
    }

    public void setFileURI(String fileURI) {
        this.fileURI = fileURI;
    }

    public String toString() {
        StringBuilder response = new StringBuilder();
        response.append("source ");
        if (fileURI != null) {
            response.append("fileURI = '");
            response.append(fileURI);
            response.append("' ");
        }
        response.append("[" + transactionID + "]");
        return response.toString();
    }
}
