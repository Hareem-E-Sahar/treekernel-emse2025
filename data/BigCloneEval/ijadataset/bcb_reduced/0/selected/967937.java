package org.dctmutils.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dctmutils.common.exception.MissingParameterException;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;

/**
 * Utility class for working with the content in Documentum objects.
 * 
 * @author <a href="mailto:luther@dctmutils.org">Luther E. Birdzell</a>
 */
public class ContentHelper {

    private static Log log = LogFactory.getLog(ContentHelper.class);

    /**
     * Converts a document to a <code>String</code>. Warning this method does
     * not validate that the requested document is a text document before
     * converting it to a <code>String</code>.
     * 
     * @param document
     * @return a <code>String</code> value or null if the document is null or
     *         does not have content.
     * @exception DfException
     * @exception IOException
     */
    public static String getContentAsString(IDfSysObject document) throws DfException, IOException {
        String documentText = new String(getContentAsBytes(document));
        return documentText;
    }

    /**
     * Converts a document to a byte[].
     * 
     * @param document
     * @return a <code>String</code> value or null if the document is null or
     *         does not have content.
     * @exception DfException
     * @exception IOException
     */
    public static byte[] getContentAsBytes(IDfSysObject document) throws DfException, IOException {
        if (document == null) {
            throw new MissingParameterException("document");
        }
        byte[] content = null;
        ByteArrayInputStream contentStream = document.getContent();
        if (contentStream != null) {
            content = new byte[(int) document.getContentSize()];
            contentStream.read(content, 0, content.length);
        } else {
            log.warn("the document has no content");
        }
        return content;
    }

    /**
     * Get the specified document's  rendition as a byte[].
     * 
     * @param document
     * @param format 
     * @return a <code>String</code> value or null if the document is null or
     *         does not have content.
     * @exception DfException
     * @exception IOException
     */
    public static byte[] getRenditionContentAsBytes(IDfSysObject document, String format) throws DfException, IOException {
        if (document == null) {
            throw new MissingParameterException("document");
        }
        byte[] content = null;
        ByteArrayInputStream contentStream = document.getContentEx(format, 0);
        if (contentStream != null) {
            content = new byte[(int) document.getContentSize()];
            contentStream.read(content, 0, content.length);
        } else {
            log.warn("the rendition has no content");
        }
        return content;
    }

    /**
     * Converts a document to a Base64 encoded byte[].
     * 
     * @param document
     * @return a <code>byte[]</code> value
     * @exception DfException
     * @exception IOException
     */
    public static byte[] getContentAsBase64EncodedBytes(IDfSysObject document) throws DfException, IOException {
        byte[] encodedFileBytes = Base64.encodeBase64(getContentAsBytes(document), false);
        return encodedFileBytes;
    }

    /**
     * Converts a document to a Base64 encoded <code>String</code>.
     * 
     * @param document
     * @return a <code>String</code> value
     * @exception DfException
     * @exception IOException
     */
    public static String getContentAsBase64EncodedString(IDfSysObject document) throws DfException, IOException {
        String base64EncodedString = new String(getContentAsBase64EncodedBytes(document));
        return base64EncodedString;
    }

    /**
     * Write to content of the Document to the specified path. This method will
     * use the object_name of the Document and append the file extension if the
     * object_name does not already contain it.
     * 
     * @param document
     * @param path
     * @throws DfException
     * @throws IOException
     */
    public static void writeContentToFile(IDfSysObject document, String path) throws DfException, IOException {
        String fileName = document.getObjectName();
        String fileExtension = null;
        if (fileName.indexOf(".") < 0) {
            FormatHelper formatHelper = FormatHelper.getInstance(document.getSession());
            fileExtension = formatHelper.getExtensionForFormat(document.getContentType());
            fileName = fileName + "." + fileExtension;
        }
        writeContentToFile(document, path, fileName);
    }

    /**
     * Write to content of the Document to the specified path. This method
     * allows the caller to specify the file name.
     * 
     * @param document
     * @param path
     * @param fileName
     * @throws DfException
     * @throws IOException
     */
    public static void writeContentToFile(IDfSysObject document, String path, String fileName) throws DfException, IOException {
        byte[] bytes = getContentAsBytes(document);
        FileOutputStream outputStream = null;
        try {
            path = path + "/" + fileName;
            outputStream = new FileOutputStream(path, false);
            outputStream.write(bytes);
            outputStream.flush();
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    /**
     * Set the content of an <code>IDfSysObject</code> using plain text.
     * 
     * @param document
     * @param plainText
     * @exception DfException
     * @exception IOException
     */
    public static void setContent(IDfSysObject document, String plainText) throws DfException, IOException {
        setContent(document, plainText.getBytes());
    }

    /**
     * Set the content of an <code>IDfSysObject</code> using a Base64 encoded
     * <code>String</code>.
     * 
     * @param document
     * @param base64EncodedContent
     * @exception DfException
     * @exception IOException
     */
    public static void setBase64EncodedContent(IDfSysObject document, String base64EncodedContent) throws DfException, IOException {
        setBase64EncodedContent(document, base64EncodedContent.getBytes());
    }

    /**
     * Set the content of an <code>IDfSysObject</code> using a Base64 encoded
     * byte[].
     * 
     * @param document
     * @param base64EncodedContent
     * @exception DfException
     * @exception IOException
     */
    public static void setBase64EncodedContent(IDfSysObject document, byte[] base64EncodedContent) throws DfException, IOException {
        byte[] contentBytes = Base64.decodeBase64(base64EncodedContent);
        setContent(document, contentBytes);
    }

    /**
     * Set the content of an <code>IDfSysObject</code> using a
     * <code>File</code> that is located in the ClassPath.
     * 
     * @param document
     * @param fileName
     * @exception DfException
     * @exception IOException
     */
    public static void setContentFromFile(IDfSysObject document, String fileName) throws DfException, IOException {
        InputStream fileStream = FileHelper.getFileAsStreamFromClassPath(fileName);
        byte[] content = IOUtils.toByteArray(fileStream);
        setContent(document, content);
    }

    /**
     * Set the content of an <code>IDfSysObject</code> using a
     * <code>File</code>.
     * 
     * @param document
     * @param file
     * @exception DfException
     * @exception IOException
     */
    public static void setContentFromFile(IDfSysObject document, File file) throws DfException, IOException {
        FileInputStream fileStream = new FileInputStream(file);
        byte[] content = IOUtils.toByteArray(fileStream);
        setContent(document, content);
    }

    /**
     * Set the content of an <code>IDfSysObject</code> using a byte[].
     * 
     * @param document
     * @param content
     * @exception DfException
     * @exception IOException
     */
    public static void setContent(IDfSysObject document, byte[] content) throws DfException, IOException {
        if (document == null || content == null) {
            throw new MissingParameterException("document and content");
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(content, 0, content.length);
        document.setContent(baos);
    }

    /**
     * Swap the primary content for the first rendition.
     *
     * @param dfSession
     * @param documentObjectId
     * @throws DfException
     * @throws IOException
     */
    public static void swapContentForFirstRendition(IDfSession dfSession, String documentObjectId) throws DfException, IOException {
        IDfDocument document = (IDfDocument) dfSession.getObject(new DfId(documentObjectId));
        String format = document.getContentType();
        String renditionFormat = null;
        IDfCollection coll = null;
        try {
            coll = document.getRenditions("r_object_id, full_format");
            while (coll.next()) {
                renditionFormat = coll.getString("full_format");
                if (!StringUtils.equals(format, renditionFormat)) {
                    break;
                }
            }
            if (StringUtils.equals(format, renditionFormat)) {
                log.warn("No alternate rendition exists for the document with r_object_id = " + DqlHelper.escape(document.getObjectId().getId()));
                return;
            }
        } finally {
            DqlHelper.cleanup(coll);
        }
        swapContentForRendition(dfSession, documentObjectId, renditionFormat);
    }

    /**
     * Swap the document's content for the specified rendition.
     * 
     * <br>
     * 
     * NOTE: use the following method after calling document.save() 
     * when adding renditions programmatically:
     * <code>document.addRenditionEx(absoluteFilePath, renditionFormat, 0, null, true)</code>
     *
     * @param dfSession
     * @param documentObjectId
     * @param renditionFormat
     * @throws DfException
     * @throws IOException
     */
    public static void swapContentForRendition(IDfSession dfSession, String documentObjectId, String renditionFormat) throws DfException, IOException {
        if (dfSession == null || DmObjectHelper.isIDfId(documentObjectId) || StringUtils.isBlank(renditionFormat)) {
            throw new MissingParameterException("dfSession, documentObjectId and renditionFormat");
        }
        IDfSysObject document = (IDfSysObject) dfSession.getObject(new DfId(documentObjectId));
        if (document == null) {
            log.warn("Unable to locate document with r_object_id = " + DqlHelper.escape(documentObjectId));
            return;
        }
        String primaryFormat = document.getContentType();
        if (StringUtils.equals(renditionFormat, primaryFormat)) {
            log.warn("The primary format is the same as the requested rendition format - no work to do for " + "document with r_object_id = " + DqlHelper.escape(documentObjectId));
            return;
        }
        byte[] renditionContent = getRenditionContentAsBytes(document, renditionFormat);
        if (renditionContent != null) {
            byte[] primaryContent = ContentHelper.getContentAsBytes(document);
            document.removeContent(0);
            document.save();
            document.setContentType(renditionFormat);
            ContentHelper.setContent(document, renditionContent);
            document.save();
            File tempFile = new File(documentObjectId);
            IOUtils.write(primaryContent, new FileOutputStream(tempFile));
            document.addRenditionEx(tempFile.getAbsolutePath(), primaryFormat, 0, null, true);
            tempFile.delete();
        }
    }
}
