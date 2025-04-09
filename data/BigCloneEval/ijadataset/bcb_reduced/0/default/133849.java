import java.io.FileOutputStream;
import java.io.IOException;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;

public class Chap0101 {

    public static void main(String[] args) {
        System.out.println("Chapter 1 example 1: Hello World");
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream("Chap0101.pdf"));
            document.open();
            document.add(new Paragraph("Hello World"));
        } catch (DocumentException de) {
            System.err.println(de.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        document.close();
    }
}
