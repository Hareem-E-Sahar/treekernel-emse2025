package part2.chapter06;

import java.io.FileOutputStream;
import java.io.IOException;
import part1.chapter03.MovieTemplates;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

public class ImportingPages2 {

    /** The resulting PDF file. */
    public static final String RESULT = "results/part2/chapter06/time_table_imported2.pdf";

    /**
     * Main method.
     * @param args no arguments needed
     * @throws DocumentException 
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, DocumentException {
        new MovieTemplates().createPdf(MovieTemplates.RESULT);
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(RESULT));
        document.open();
        PdfReader reader = new PdfReader(MovieTemplates.RESULT);
        int n = reader.getNumberOfPages();
        PdfImportedPage page;
        PdfPTable table = new PdfPTable(2);
        for (int i = 1; i <= n; i++) {
            page = writer.getImportedPage(reader, i);
            table.getDefaultCell().setRotation(-reader.getPageRotation(i));
            table.addCell(Image.getInstance(page));
        }
        document.add(table);
        document.close();
    }
}
