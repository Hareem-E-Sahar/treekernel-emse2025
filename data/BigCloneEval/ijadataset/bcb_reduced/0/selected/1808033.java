package org.openxml4j.opc.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.dom4j.Document;
import org.openxml4j.exceptions.InvalidFormatException;
import org.openxml4j.opc.Package;
import org.openxml4j.opc.StreamHelper;

/**
 * Zip implementation of the ContentTypeManager.
 * 
 * @author Julien Chable
 * @version 1.0
 * @see ContentTypeManager
 */
public class ZipContentTypeManager extends ContentTypeManager {

    /**
	 * Delegate constructor to the super constructor.
	 * 
	 * @param in
	 *            The input stream to parse to fill internal content type
	 *            collections.
	 * @throws InvalidFormatException
	 *             If the content types part content is not valid.
	 */
    public ZipContentTypeManager(InputStream in, Package pkg) throws InvalidFormatException {
        super(in, pkg);
    }

    @Override
    public boolean saveImpl(Document content, OutputStream out) {
        ZipOutputStream zos = null;
        if (out instanceof ZipOutputStream) zos = (ZipOutputStream) out; else zos = new ZipOutputStream(out);
        ZipEntry partEntry = new ZipEntry(CONTENT_TYPES_PART_NAME);
        try {
            zos.putNextEntry(partEntry);
            ByteArrayOutputStream outTemp = new ByteArrayOutputStream();
            StreamHelper.saveXmlInStream(content, out);
            InputStream ins = new ByteArrayInputStream(outTemp.toByteArray());
            byte[] buff = new byte[ZipHelper.READ_WRITE_FILE_BUFFER_SIZE];
            while (ins.available() > 0) {
                int resultRead = ins.read(buff);
                if (resultRead == -1) {
                    break;
                } else {
                    zos.write(buff, 0, resultRead);
                }
            }
            zos.closeEntry();
        } catch (IOException ioe) {
            logger.error("Cannot write: " + CONTENT_TYPES_PART_NAME + " in Zip !", ioe);
            return false;
        }
        return true;
    }
}
