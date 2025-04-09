package name.huliqing.qblog.backup;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import name.huliqing.common.XmlUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author huliqing
 */
public class ZipHelper {

    public static final void export(String filename, byte[] bytes, String charset) throws IOException {
        HttpServletResponse response = findResponse();
        if (charset != null) {
            response.setCharacterEncoding(charset);
        }
        response.setContentType("application/octet-stream");
        response.setHeader("content-disposition", "attachment;filename=" + filename + ".zip");
        OutputStream os = response.getOutputStream();
        ZipOutputStream zos = new ZipOutputStream(os);
        ZipEntry zipEntry = new ZipEntry(filename + ".xml");
        zos.putNextEntry(zipEntry);
        zos.write(bytes);
        zos.close();
    }

    public static final void export(String filename, Document doc) throws TransformerException, UnsupportedEncodingException, IOException {
        export(filename, doc, "UTF-8");
    }

    public static final void export(String filename, Document doc, String charset) throws TransformerException, UnsupportedEncodingException, IOException {
        byte[] bytes = XmlUtils.toXmlString(doc).getBytes(charset);
        export(filename, bytes, charset);
    }

    public static final byte[] importAsByte(byte[] zipBytes) throws IOException {
        ZipInputStream zis = new ZipInputStream(new InputStreamHelper(zipBytes));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipEntry ze = null;
        int len;
        byte[] buff = new byte[2048];
        while ((ze = zis.getNextEntry()) != null) {
            while ((len = zis.read(buff, 0, buff.length)) != -1) {
                baos.write(buff, 0, len);
            }
        }
        return baos.toByteArray();
    }

    public static final Document importAsDoc(byte[] zipBytes) throws IOException, SAXException, ParserConfigurationException {
        return importAsDoc(zipBytes, "UTF-8");
    }

    public static final Document importAsDoc(byte[] zipBytes, String charset) throws IOException, SAXException, ParserConfigurationException {
        byte[] bytes = importAsByte(zipBytes);
        String strValue = new String(bytes, 0, bytes.length, charset);
        return XmlUtils.newDocument(strValue);
    }

    private static final HttpServletResponse findResponse() {
        FacesContext fc = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();
        return response;
    }

    private static final class InputStreamHelper extends InputStream {

        private byte[] bytes;

        private int pos;

        public InputStreamHelper(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public int read() throws IOException {
            if (bytes.length <= 0 || pos >= bytes.length) {
                return -1;
            }
            return bytes[pos++] & 0xff;
        }
    }
}
