package org.in4ama.documentengine.compile;

import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.in4ama.documentautomator.documents.Document;
import org.in4ama.documentautomator.util.XmlHelper;
import org.w3c.dom.Node;

/** Skeleton implementation of the document compiler. */
public abstract class AbstractDocumentCompiler implements DocumentCompiler {

    /** Adds a list of documents to the specified input stream. */
    public void addDocuments(List<Document> documents, ZipOutputStream zipOutputStream) throws CompileException {
        for (Document document : documents) {
            addDocument(document, zipOutputStream);
        }
    }

    /** Adds the XML node to the specified ZIP output stream. */
    protected void addDocument(String filePath, Node node, ZipOutputStream out) throws CompileException {
        InputStream content = null;
        try {
            content = XmlHelper.convertToInputStream(node);
        } catch (Exception ex) {
            String msg = "Unable to convert XML node to input stream.";
            throw new CompileException(msg, ex);
        }
        addFile(filePath, content, out);
    }

    /** Adds the file to the given output stream. */
    protected void addFile(String filePath, InputStream content, ZipOutputStream out) throws CompileException {
        try {
            byte[] buf = new byte[1024];
            out.putNextEntry(new ZipEntry(filePath));
            for (int len = 0; (len = content.read(buf)) > 0; ) {
                out.write(buf, 0, len);
            }
        } catch (Exception ex) {
            String msg = "Unable to add an entry to the ZIP file.";
            throw new CompileException(msg, ex);
        }
    }

    /** Creates and returns a path to the folder as required by ZIP format. */
    protected String composeFolderPath(String... parts) {
        StringBuffer path = new StringBuffer();
        for (String part : parts) {
            path.append(part).append("/");
        }
        return path.toString();
    }

    /** Creates and returns a path to the file as required by ZIP format.
	 * the last argument is the file extension. */
    protected String composeFilePath(String... parts) {
        StringBuffer path = new StringBuffer();
        for (int i = 0; i < parts.length - 2; i++) {
            path.append(parts[i]).append("/");
        }
        if (parts.length > 1) {
            path.append(parts[parts.length - 2]);
        }
        if (parts.length > 0) {
            path.append(".").append(parts[parts.length - 1]);
        }
        return path.toString();
    }

    /** Returns the name of the document name created 
	  * from the given file name. */
    protected String getDocumentName(String fileName, String fileExtension) {
        String documentName = null;
        if (fileName.endsWith("." + fileExtension)) {
            documentName = fileName.substring(0, fileName.length() - fileExtension.length() - 1);
        }
        return documentName;
    }
}
