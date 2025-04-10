package net.sourceforge.processdash.tool.export.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.sourceforge.processdash.util.StringUtils;
import net.sourceforge.processdash.util.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

public class ExternalResourceManifestXMLv1 implements ExternalResourceMappingLoader, ExternalResourceXmlConstantsv1 {

    private List<MappingEntry> mappingEntries;

    public ExternalResourceManifestXMLv1() {
        this.mappingEntries = new ArrayList<MappingEntry>();
    }

    public boolean isEmpty() {
        return mappingEntries.isEmpty();
    }

    public void addMapping(String origPath, String origUrl, String newPath) {
        mappingEntries.add(new MappingEntry(origPath, origUrl, newPath));
    }

    public void write(ZipOutputStream out) throws IOException {
        if (isEmpty()) return;
        ZipEntry entry = new ZipEntry(ARCHIVE_PATH + "/" + MANIFEST_FILE_NAME);
        out.putNextEntry(entry);
        write((OutputStream) out);
        out.closeEntry();
    }

    public void write(OutputStream out) throws IOException {
        XmlSerializer ser = XMLUtils.getXmlSerializer(true);
        ser.setOutput(out, ENCODING);
        ser.startDocument(ENCODING, null);
        ser.startTag(null, DOC_ROOT_ELEM);
        for (MappingEntry e : mappingEntries) {
            String origURL = e.getOriginalURL();
            String origPath = e.getOriginalPath();
            String newPath = e.getNewPath();
            ser.startTag(null, IMPORT_DIR_ELEM);
            if (StringUtils.hasValue(origURL)) ser.attribute(null, ORIGINAL_URL, origURL);
            if (StringUtils.hasValue(origPath)) ser.attribute(null, ORIGINAL_PATH, origPath);
            ser.attribute(null, NEW_PATH, newPath);
            ser.endTag(null, IMPORT_DIR_ELEM);
        }
        ser.endTag(null, DOC_ROOT_ELEM);
        ser.endDocument();
    }

    public Map load(File fileOrDirectory) {
        File archiveDir;
        File manifestFile;
        if (fileOrDirectory.getName().toLowerCase().endsWith(".xml")) {
            manifestFile = fileOrDirectory;
            archiveDir = manifestFile.getParentFile();
        } else {
            archiveDir = new File(fileOrDirectory, ARCHIVE_PATH);
            manifestFile = new File(archiveDir, MANIFEST_FILE_NAME);
        }
        if (!manifestFile.isFile()) return Collections.EMPTY_MAP;
        Document doc;
        try {
            doc = XMLUtils.parse(new FileInputStream(manifestFile));
        } catch (Exception e) {
            return Collections.EMPTY_MAP;
        }
        Map result = new HashMap();
        NodeList directories = doc.getElementsByTagName(IMPORT_DIR_ELEM);
        for (int i = 0; i < directories.getLength(); i++) {
            Element e = (Element) directories.item(i);
            String origPath = e.getAttribute(ORIGINAL_PATH);
            String origURL = e.getAttribute(ORIGINAL_URL);
            String newPath = e.getAttribute(NEW_PATH);
            File newDir = new File(archiveDir, newPath);
            if (XMLUtils.hasValue(origPath)) result.put(origPath, newDir.getAbsolutePath());
            if (XMLUtils.hasValue(origURL)) result.put(origURL, newDir.getAbsolutePath());
            mappingEntries.add(new MappingEntry(origPath, origURL, newPath));
        }
        return result;
    }

    private class MappingEntry {

        private String originalPath;

        private String originalURL;

        private String newPath;

        protected MappingEntry(String originalPath, String originalURL, String newPath) {
            this.originalPath = nvl(originalPath);
            this.originalURL = nvl(originalURL);
            this.newPath = nvl(newPath);
        }

        private String nvl(String s) {
            return (StringUtils.hasValue(s) ? s : null);
        }

        public String getOriginalPath() {
            return originalPath;
        }

        public String getOriginalURL() {
            return originalURL;
        }

        public String getNewPath() {
            return newPath;
        }
    }
}
