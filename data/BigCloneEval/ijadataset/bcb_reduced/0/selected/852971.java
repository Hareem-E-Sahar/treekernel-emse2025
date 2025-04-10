package org.plazmaforge.framework.datawarehouse.convert.dataexport.odt.zip;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public abstract class OasisZip {

    private List oasisZipEntries = null;

    private OasisZipEntry contentEntry = null;

    private OasisZipEntry stylesEntry = null;

    public OasisZip() throws IOException {
        oasisZipEntries = new ArrayList();
        contentEntry = createEntry("content.xml");
        oasisZipEntries.add(contentEntry);
        oasisZipEntries.add(new EmptyOasisZipEntry("meta.xml"));
        oasisZipEntries.add(new EmptyOasisZipEntry("settings.xml"));
        stylesEntry = createEntry("styles.xml");
        oasisZipEntries.add(stylesEntry);
        OasisZipEntry mimeEntry = createEntry("mimetype");
        Writer mimeWriter = null;
        try {
            mimeWriter = mimeEntry.getWriter();
            mimeWriter.write("application/vnd.oasis.opendocument.text");
            mimeWriter.flush();
            oasisZipEntries.add(mimeEntry);
        } finally {
            if (mimeWriter != null) {
                try {
                    mimeWriter.close();
                } catch (IOException e) {
                }
            }
        }
        OasisZipEntry manifestEntry = createEntry("META-INF/manifest.xml");
        Writer manifestWriter = null;
        try {
            manifestWriter = manifestEntry.getWriter();
            manifestWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?> \r\n");
            manifestWriter.write("<!DOCTYPE manifest:manifest PUBLIC \"-//OpenOffice.org//DTD Manifest 1.0//EN\" \"Manifest.dtd\"> \r\n");
            manifestWriter.write("<manifest:manifest xmlns:manifest=\"urn:oasis:names:tc:opendocument:xmlns:manifest:1.0\"> \r\n");
            manifestWriter.write("  <manifest:file-entry manifest:media-type=\"application/vnd.oasis.opendocument.text\" manifest:full-path=\"/\"/> \r\n");
            manifestWriter.write("  <manifest:file-entry manifest:media-type=\"application/vnd.sun.xml.ui.configuration\" manifest:full-path=\"Configurations2/\"/> \r\n");
            manifestWriter.write("  <manifest:file-entry manifest:media-type=\"\" manifest:full-path=\"Pictures/\"/> \r\n");
            manifestWriter.write("  <manifest:file-entry manifest:media-type=\"text/xml\" manifest:full-path=\"content.xml\"/> \r\n");
            manifestWriter.write("  <manifest:file-entry manifest:media-type=\"text/xml\" manifest:full-path=\"styles.xml\"/> \r\n");
            manifestWriter.write("  <manifest:file-entry manifest:media-type=\"text/xml\" manifest:full-path=\"meta.xml\"/> \r\n");
            manifestWriter.write("  <manifest:file-entry manifest:media-type=\"\" manifest:full-path=\"Thumbnails/thumbnail.png\"/> \r\n");
            manifestWriter.write("  <manifest:file-entry manifest:media-type=\"\" manifest:full-path=\"Thumbnails/\"/> \r\n");
            manifestWriter.write("  <manifest:file-entry manifest:media-type=\"text/xml\" manifest:full-path=\"settings.xml\"/> \r\n");
            manifestWriter.write("</manifest:manifest> \r\n");
            manifestWriter.flush();
            oasisZipEntries.add(manifestEntry);
        } finally {
            if (mimeWriter != null) {
                try {
                    mimeWriter.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public abstract OasisZipEntry createEntry(String name);

    public OasisZipEntry getContentEntry() {
        return contentEntry;
    }

    public OasisZipEntry getStylesEntry() {
        return stylesEntry;
    }

    public void addEntry(OasisZipEntry entry) {
        oasisZipEntries.add(entry);
    }

    public void zipEntries(OutputStream os) throws IOException {
        ZipOutputStream zipos = new ZipOutputStream(os);
        zipos.setMethod(ZipOutputStream.DEFLATED);
        for (int i = 0; i < oasisZipEntries.size(); i++) {
            OasisZipEntry oasisZipEntry = (OasisZipEntry) oasisZipEntries.get(i);
            ZipEntry zipEntry = new ZipEntry(oasisZipEntry.getName());
            zipos.putNextEntry(zipEntry);
            oasisZipEntry.writeData(zipos);
        }
        zipos.flush();
        zipos.finish();
    }

    public void dispose() {
        for (int i = 0; i < oasisZipEntries.size(); i++) {
            OasisZipEntry oasisZipEntry = (OasisZipEntry) oasisZipEntries.get(i);
            oasisZipEntry.dispose();
        }
    }
}
