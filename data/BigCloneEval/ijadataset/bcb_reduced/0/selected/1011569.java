package questions.compression;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

public class ZipPdfFiles {

    public static final String RESULT = "results/questions/compression/pdf_files.zip";

    public static void main(String args[]) throws IOException, DocumentException {
        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(RESULT));
        for (int i = 1; i <= 3; i++) {
            createPdf(zip, i);
        }
        zip.close();
    }

    public static void createPdf(ZipOutputStream zip, int counter) throws IOException, DocumentException {
        ZipEntry entry = new ZipEntry("document" + counter + ".pdf");
        zip.putNextEntry(entry);
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, zip);
        writer.setCloseStream(false);
        document.open();
        document.add(new Paragraph("Document " + counter));
        document.close();
        zip.closeEntry();
    }
}
