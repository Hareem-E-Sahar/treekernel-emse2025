package org.in4ama.documentengine.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.in4ama.documentautomator.exception.DocumentException;
import org.in4ama.documentengine.exception.EvaluationException;

/** Stores the binary data of an open office document */
public final class OdtResource {

    private Hashtable<String, byte[]> entries = new Hashtable<String, byte[]>();

    /** Creates a new empty instnace of the OdtResource class */
    public OdtResource() {
    }

    /** Creates a new instance of the OdtResource class 
 * @throws DocumentException 
 * @throws EvaluationException */
    public OdtResource(String fileName) throws DocumentException, EvaluationException {
        try {
            File file = new File(fileName);
            InputStream in = new BufferedInputStream(new FileInputStream(file));
            init(in);
        } catch (FileNotFoundException ex) {
            String msg = "Unable to read the ODT file '" + fileName + "'.";
            throw new DocumentException(msg, ex);
        }
    }

    /** Returns a copy of this object */
    public OdtResource getEvalInstance() {
        OdtResource copy = new OdtResource();
        Set<String> entryNames = entries.keySet();
        for (String entryName : entryNames) {
            byte[] content = entries.get(entryName);
            byte[] contentCopy = new byte[content.length];
            System.arraycopy(content, 0, contentCopy, 0, content.length);
            copy.entries.put(entryName, contentCopy);
        }
        return copy;
    }

    /** Creates a new instance of the OdtResource class 
 * @throws EvaluationException */
    public OdtResource(InputStream in) throws EvaluationException {
        init(in);
    }

    public byte[] getResource(String name) {
        return entries.get(name);
    }

    public void setResource(String name, byte[] content) {
        entries.put(name, content);
    }

    private void init(InputStream in) throws EvaluationException {
        byte[] buf = new byte[1024];
        BufferedInputStream bis = null;
        ZipInputStream zis = null;
        ZipEntry ze = null;
        try {
            bis = new BufferedInputStream(in);
            zis = new ZipInputStream(bis);
            while ((ze = zis.getNextEntry()) != null) {
                String name = ze.getName();
                if (ze.isDirectory()) {
                    entries.put(name, new byte[0]);
                } else {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    int len;
                    while ((len = zis.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.flush();
                    entries.put(name, out.toByteArray());
                }
            }
        } catch (Exception ex) {
            String msg = "Unable to read the ODT file.";
            throw new EvaluationException(msg, ex);
        } finally {
            try {
                if (bis != null) bis.close();
                if (zis != null) zis.close();
            } catch (Exception ex) {
                String msg = "Unable to read the ODT file.";
                throw new EvaluationException(msg, ex);
            }
        }
    }

    public InputStream getInputStream() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream out = new ZipOutputStream(bos);
        for (Object oEntryName : entries.keySet()) {
            String entryName = (String) oEntryName;
            byte[] content = (byte[]) entries.get(entryName);
            out.putNextEntry(new ZipEntry(entryName));
            if (content.length > 0) {
                out.write(content);
            }
            out.closeEntry();
        }
        out.flush();
        out.close();
        bos.flush();
        InputStream retIn = new ByteArrayInputStream(bos.toByteArray());
        bos.close();
        return retIn;
    }
}
