import java.io.FileOutputStream;
import java.io.IOException;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;

public class Chap0203 {

    public static void main(String[] args) {
        System.out.println("Chapter 2 example 3: Greek Characters");
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream("Chap0203.pdf"));
            document.open();
            document.add(new Phrase("What is the " + (char) 945 + "-coefficient of the " + (char) 946 + "-factor in the " + (char) 947 + "-equation?\n"));
            for (int i = 913; i < 970; i++) {
                document.add(new Phrase(" " + String.valueOf(i) + ": " + (char) i));
            }
        } catch (DocumentException de) {
            System.err.println(de.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        document.close();
    }
}
