package part2.chapter06;

import java.io.FileOutputStream;
import java.io.IOException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

public class TilingHero {

    /** The original PDF file. */
    public static final String RESOURCE = "resources/pdfs/hero.pdf";

    /** The resulting PDF file. */
    public static final String RESULT = "results/part2/chapter06/superman.pdf";

    /**
     * Manipulates a PDF file src with the file dest as result
     * @param src the original PDF
     * @param dest the resulting PDF
     * @throws IOException
     * @throws DocumentException
     */
    public void manipulatePdf(String src, String dest) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(src);
        Rectangle pagesize = reader.getPageSizeWithRotation(1);
        Document document = new Document(pagesize);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(dest));
        document.open();
        PdfContentByte content = writer.getDirectContent();
        PdfImportedPage page = writer.getImportedPage(reader, 1);
        float x, y;
        for (int i = 0; i < 16; i++) {
            x = -pagesize.getWidth() * (i % 4);
            y = pagesize.getHeight() * (i / 4 - 3);
            content.addTemplate(page, 4, 0, 0, 4, x, y);
            document.newPage();
        }
        document.close();
    }

    /**
     * Main method.
     * @param    args    no arguments needed
     * @throws DocumentException 
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, DocumentException {
        new TilingHero().manipulatePdf(RESOURCE, RESULT);
    }
}
