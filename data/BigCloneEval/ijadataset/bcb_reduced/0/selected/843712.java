package writer2latex.epub;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import writer2latex.api.ConverterResult;
import writer2latex.api.OutputFile;
import writer2latex.util.Misc;
import writer2latex.xhtml.XhtmlConfig;

/** This class repackages an XHTML document into EPUB format.
 *  Some filenames are hard wired in this implementation: The main directory is OEBPS and
 *  the OPF and NCX files are book.opf and book.ncx respectively 
 */
public class EPUBWriter implements OutputFile {

    private static final byte[] mimeBytes = { 'a', 'p', 'p', 'l', 'i', 'c', 'a', 't', 'i', 'o', 'n', '/', 'e', 'p', 'u', 'b', '+', 'z', 'i', 'p' };

    private ConverterResult xhtmlResult;

    private String sFileName;

    private XhtmlConfig config;

    public EPUBWriter(ConverterResult xhtmlResult, String sFileName, XhtmlConfig config) {
        this.xhtmlResult = xhtmlResult;
        this.sFileName = Misc.removeExtension(sFileName);
        this.config = config;
    }

    public String getFileName() {
        return sFileName + ".epub";
    }

    public String getMIMEType() {
        return "application/epub+zip";
    }

    public boolean isMasterDocument() {
        return true;
    }

    public void write(OutputStream os) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(os);
        ZipEntry mimeEntry = new ZipEntry("mimetype");
        mimeEntry.setMethod(ZipEntry.STORED);
        mimeEntry.setCrc(0x2CAB616F);
        mimeEntry.setSize(mimeBytes.length);
        zos.putNextEntry(mimeEntry);
        zos.write(mimeBytes, 0, mimeBytes.length);
        zos.closeEntry();
        OutputFile containerWriter = new ContainerWriter();
        ZipEntry containerEntry = new ZipEntry("META-INF/container.xml");
        zos.putNextEntry(containerEntry);
        writeZipEntry(containerWriter, zos);
        zos.closeEntry();
        OPFWriter manifest = new OPFWriter(xhtmlResult);
        ZipEntry manifestEntry = new ZipEntry("OEBPS/book.opf");
        zos.putNextEntry(manifestEntry);
        writeZipEntry(manifest, zos);
        zos.closeEntry();
        OutputFile ncx = new NCXWriter(xhtmlResult, manifest.getUid());
        ZipEntry ncxEntry = new ZipEntry("OEBPS/book.ncx");
        zos.putNextEntry(ncxEntry);
        writeZipEntry(ncx, zos);
        zos.closeEntry();
        Iterator<OutputFile> iter = xhtmlResult.iterator();
        while (iter.hasNext()) {
            OutputFile file = iter.next();
            ZipEntry entry = new ZipEntry("OEBPS/" + file.getFileName());
            zos.putNextEntry(entry);
            writeZipEntry(file, zos);
            zos.closeEntry();
        }
        zos.close();
    }

    private void writeZipEntry(OutputFile file, ZipOutputStream zos) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        file.write(baos);
        byte[] content = baos.toByteArray();
        zos.write(content, 0, content.length);
    }
}
