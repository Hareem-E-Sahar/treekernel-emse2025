package au.gov.naa.digipres.xena.plugin.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.ExportResult;

/**
 * The original archive file has been decompressed and normalised to a collection of xena files, plus an index file to link the collection together.
 * This denormaliser iterates through each xena file listed in the index file, and exports each of these xena files to a temporary file.
 * Each of these temporary files are then compressed in a zip file.
 * 
 * created 03/04/2007
 * archive
 * Short desc of class:
 */
public class ArchiveDeNormaliser extends AbstractDeNormaliser {

    private int entryCounter = 0;

    private ZipOutputStream zipOS;

    @Override
    public String getName() {
        return "Archive Denormaliser";
    }

    @Override
    public String getOutputFileExtension(XenaInputSource xis) {
        return "zip";
    }

    @Override
    public void startDocument() {
        zipOS = new ZipOutputStream(streamResult.getOutputStream());
    }

    @Override
    public void endDocument() throws SAXException {
        if (entryCounter == 0) {
            throw new SAXException("No valid archive entries found - check that all normalised files are present and in the same directory.");
        }
        try {
            zipOS.flush();
            zipOS.close();
        } catch (IOException e) {
            throw new SAXException("Problem closing Zip output stream", e);
        }
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        if (qName.equals(ArchiveNormaliser.ARCHIVE_PREFIX + ":" + ArchiveNormaliser.ENTRY_TAG)) {
            entryCounter++;
            String xenaFilename = atts.getValue(ArchiveNormaliser.ARCHIVE_PREFIX + ":" + ArchiveNormaliser.ENTRY_OUTPUT_FILENAME);
            if (xenaFilename == null) {
                xenaFilename = atts.getValue(ArchiveNormaliser.ENTRY_OUTPUT_FILENAME);
            }
            if (xenaFilename == null) {
                throw new SAXException("Archive entry has a null xena filename.");
            }
            File entryFile = new File(sourceDirectory, xenaFilename);
            if (entryFile.exists() && entryFile.isFile()) {
                File entryExportFile = null;
                try {
                    ExportResult exportResult = normaliserManager.export(new XenaInputSource(entryFile), outputDirectory, true);
                    entryExportFile = exportResult.getOutputFile();
                    String originalPath = atts.getValue(ArchiveNormaliser.ARCHIVE_PREFIX + ":" + ArchiveNormaliser.ENTRY_ORIGINAL_PATH_ATTRIBUTE);
                    if (originalPath == null) {
                        originalPath = atts.getValue(ArchiveNormaliser.ENTRY_ORIGINAL_PATH_ATTRIBUTE);
                    }
                    if (originalPath == null) {
                        throw new SAXException("Archive entry has a null original path.");
                    }
                    int extensionIndex = entryExportFile.getName().lastIndexOf(".");
                    String entryExportFileExtension = extensionIndex >= 0 ? entryExportFile.getName().substring(extensionIndex + 1) : "";
                    String entryName = normaliserManager.adjustOutputFileExtension(originalPath, entryExportFileExtension);
                    ZipEntry currentEntry = new ZipEntry(entryName);
                    String originalDate = atts.getValue(ArchiveNormaliser.ARCHIVE_PREFIX + ":" + ArchiveNormaliser.ENTRY_ORIGINAL_FILE_DATE_ATTRIBUTE);
                    if (originalDate == null) {
                        originalDate = atts.getValue(ArchiveNormaliser.ENTRY_ORIGINAL_FILE_DATE_ATTRIBUTE);
                    }
                    if (originalDate == null) {
                        throw new SAXException("Archive entry has a null original date.");
                    }
                    SimpleDateFormat formatter = new SimpleDateFormat(ArchiveNormaliser.DATE_FORMAT_STRING);
                    currentEntry.setTime(formatter.parse(originalDate).getTime());
                    zipOS.putNextEntry(currentEntry);
                    FileInputStream entryExportIS = new FileInputStream(entryExportFile);
                    byte[] buffer = new byte[10 * 1024];
                    int bytesRead = entryExportIS.read(buffer);
                    while (bytesRead > 0) {
                        zipOS.write(buffer, 0, bytesRead);
                        bytesRead = entryExportIS.read(buffer);
                    }
                    entryExportIS.close();
                    zipOS.closeEntry();
                } catch (Exception ex) {
                    throw new SAXException("Problem exporting archive entry " + atts.getValue(ArchiveNormaliser.ARCHIVE_PREFIX + ":" + ArchiveNormaliser.ENTRY_ORIGINAL_PATH_ATTRIBUTE), ex);
                } finally {
                    if (entryExportFile != null) {
                        entryExportFile.delete();
                    }
                }
            }
        }
    }
}
