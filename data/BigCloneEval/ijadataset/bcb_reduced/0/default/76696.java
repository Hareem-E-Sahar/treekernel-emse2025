import java.awt.Color;
import java.io.FileOutputStream;
import java.io.File;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.cyberneko.html.parsers.DOMParser;
import com.lowagie.text.*;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.List;
import com.lowagie.text.pdf.*;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.*;
import ml.options.*;
import java.util.*;

class TocGenerator extends PdfPageEventHelper {

    List _toc;

    public TocGenerator() {
        _toc = new List();
    }

    @Override
    public void onChapter(PdfWriter arg0, com.lowagie.text.Document doc, float arg2, Paragraph title) {
        if (!title.getContent().endsWith("Index")) {
            _toc.add(new ListItem(new Paragraph(title.getContent() + " .... " + doc.getPageNumber())));
        }
    }

    @Override
    public void onSection(PdfWriter arg0, com.lowagie.text.Document doc, float arg2, int arg3, Paragraph title) {
        _toc.add(new ListItem(new Paragraph(title.getContent() + " .... " + doc.getPageNumber())));
    }

    public List getToc() {
        return _toc;
    }
}

@SuppressWarnings("unchecked")
public class RecoverSpr {

    private static final String THE_URL = "http://www.rssd.esa.int/herschel_webapps/servletsuite/ProblemReportServlet?area=hcss&mode=displaypr&id=";

    private static Hashtable fieldsDef;

    static {
        fieldsDef = new Hashtable();
        fieldsDef.put("Id", new Object[] { 8, 1 });
        fieldsDef.put("Title", new Object[] { 9, 2 });
        fieldsDef.put("Submitter", new Object[] { 14, 3 });
        fieldsDef.put("Module", new Object[] { 21, 4 });
        fieldsDef.put("Priority", new Object[] { 23, 5 });
        fieldsDef.put("SubmissionDate", new Object[] { 29, 6 });
        fieldsDef.put("Description", new Object[] { 37, 7 });
        fieldsDef.put("Analysis", new Object[] { 39, 8 });
        fieldsDef.put("Status", new Object[] { 27, 8 });
    }

    ;

    private Document _dom = null;

    private Hashtable _data;

    public RecoverSpr(DOMParser parser, String id) throws Exception {
        parser.parse(THE_URL + id);
        _dom = parser.getDocument();
        NodeList nl = _dom.getElementsByTagName("td");
        _data = new Hashtable();
        Enumeration enumeration = fieldsDef.keys();
        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            Object[] element = (Object[]) fieldsDef.get(key);
            _data.put(key, nl.item((Integer) element[0]).getTextContent());
        }
    }

    public void addPdfTable(com.lowagie.text.Document pdfDoc, Image headerImage, Vector fields) throws Exception {
        PdfPTable table = new PdfPTable(2);
        PdfPCell cell = new PdfPCell(headerImage);
        cell.setColspan(2);
        table.addCell(cell);
        Enumeration enumeration = fields.elements();
        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            cell = new PdfPCell(new Paragraph((String) _data.get(key)));
            cell.setColspan(2);
            table.addCell(cell);
        }
        table.setExtendLastRow(true);
        pdfDoc.add(table);
        pdfDoc.newPage();
    }

    public void generateXlsRow(WritableSheet s, int rowIndx, Vector fields) throws Exception {
        Enumeration enumeration = fields.elements();
        int colIndx = 0;
        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            s.addCell(new Label(colIndx, rowIndx, (String) _data.get(key)));
            colIndx++;
        }
    }

    public String toString(Vector fields) {
        StringBuffer sb = new StringBuffer();
        Enumeration enumeration = fields.elements();
        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            sb.append(key + " " + _data.get(key) + "\n");
        }
        return sb.toString();
    }

    public String toString() {
        return toString(new Vector(fieldsDef.keySet()));
    }

    public static com.lowagie.text.Document openPdfDocument(String filename, TocGenerator tocG) throws Exception {
        com.lowagie.text.Document pdfDoc = null;
        pdfDoc = new com.lowagie.text.Document(PageSize.A4, 25, 25, 25, 25);
        PdfWriter writer = PdfWriter.getInstance(pdfDoc, new FileOutputStream(filename));
        writer.setPageEvent(tocG);
        pdfDoc.addTitle("PHS SxR resume " + new java.util.Date());
        pdfDoc.addAuthor("Rafael Andres");
        pdfDoc.addSubject("SxR resume");
        prepareCover(pdfDoc, writer);
        PdfPTable table = new PdfPTable(1);
        Phrase p = new Phrase("");
        p.add(table);
        HeaderFooter footer = new HeaderFooter(new Phrase(p), true);
        footer.setBorder(HeaderFooter.NO_BORDER);
        footer.setAlignment(HeaderFooter.ALIGN_CENTER);
        pdfDoc.setFooter(footer);
        pdfDoc.open();
        return pdfDoc;
    }

    public static void prepareCover(com.lowagie.text.Document pdfDoc, PdfWriter writer) {
        try {
            BaseFont coverFont = BaseFont.createFont(BaseFont.HELVETICA_BOLDOBLIQUE, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            BaseFont auxCoverFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            Image im = Image.getInstance(RecoverSpr.class.getResource("/image/herschel_logo.jpg"));
            PdfContentByte cb = writer.getDirectContent();
            cb.setColorFill(new Color(247, 230, 80));
            cb.rectangle(0, 0, 160, pdfDoc.getPageSize().getHeight());
            cb.fill();
            cb.beginText();
            cb.setColorFill(new Color(75, 123, 138));
            cb.setColorStroke(new Color(75, 123, 138));
            cb.setTextMatrix(180, (pdfDoc.getPageSize().getHeight() / 3) * 2.5f);
            cb.setTextRenderingMode(cb.TEXT_RENDER_MODE_STROKE);
            cb.setFontAndSize(coverFont, 64);
            cb.moveTextWithLeading(0, -36);
            cb.showText("HERSCHEL");
            cb.newlineText();
            cb.setFontAndSize(auxCoverFont, 16);
            cb.setTextRenderingMode(cb.TEXT_RENDER_MODE_FILL);
            cb.showText("Space Observatory");
            float imagePos = cb.getYTLM();
            cb.setTextMatrix(180, cb.getYTLM() - im.getHeight() - 60);
            cb.setFontAndSize(auxCoverFont, 16);
            cb.setColorFill(Color.BLACK);
            cb.showText("Report");
            cb.newlineText();
            cb.setFontAndSize(auxCoverFont, 10);
            cb.showText(new Date().toString());
            cb.endText();
            im.setAbsolutePosition(180, imagePos - im.getHeight() - 20);
            cb.addImage(im);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getData(String key) {
        return (String) _data.get(key);
    }

    public static void main(String[] args) {
        String filename = "Resume";
        ArrayList<String> ids = null;
        String[] fieldStr = new String[] { "Id", "Title", "Status", "Description", "Analysis" };
        boolean generatePdf = true;
        boolean generateXls = true;
        Options opts = new Options(args, Options.Multiplicity.ZERO_OR_ONE, 1, 100);
        opts.getSet().addOption("pdf").addOption("xls").addOption("file", Options.Separator.EQUALS).addOption("fields", Options.Separator.EQUALS);
        if (!opts.check()) {
            System.out.println("Usage:\n\t recoverSpr [-pdf] [-xls] [-file=<file>]  [-fields=<field1>:<..>:<fieldn>]<id> ... <id>");
            System.exit(1);
        }
        generatePdf = opts.getSet().isSet("pdf");
        generateXls = opts.getSet().isSet("xls");
        if (opts.getSet().isSet("file")) {
            filename = opts.getSet().getOption("file").getResultValue(0);
        }
        if (opts.getSet().isSet("fields")) {
            String fieldsOption = opts.getSet().getOption("fields").getResultValue(0);
            fieldStr = fieldsOption.split(":");
        }
        ids = opts.getSet().getData();
        if (ids != null && ids.size() > 0) {
            com.lowagie.text.Document pdfDoc = null;
            TocGenerator tocG = null;
            int chapIndx = 1;
            WritableWorkbook w = null;
            WritableSheet s = null;
            try {
                if (generatePdf) {
                    tocG = new TocGenerator();
                    pdfDoc = RecoverSpr.openPdfDocument(filename + ".pdf", tocG);
                }
                if (generateXls) {
                    w = Workbook.createWorkbook(new File(filename + ".xls"));
                    s = w.createSheet("PHS", 0);
                }
                Image headerImage = Image.getInstance(RecoverSpr.class.getResource("/image/header_50.jpg"));
                headerImage.scalePercent(60);
                DOMParser parser = new DOMParser();
                Vector fields = new Vector(Arrays.asList(fieldStr));
                Collections.sort(ids);
                for (int i = 0; i < ids.size(); i++) {
                    RecoverSpr rs = new RecoverSpr(parser, ids.get(i));
                    if (generatePdf) {
                        Paragraph chapTitle = new Paragraph(rs.getData("Id"), FontFactory.getFont(FontFactory.HELVETICA, 16));
                        chapTitle.setSpacingAfter(12);
                        pdfDoc.add(new Chapter(chapTitle, chapIndx));
                        chapIndx++;
                        rs.addPdfTable(pdfDoc, headerImage, fields);
                    }
                    if (generateXls) {
                        rs.generateXlsRow(s, i, fields);
                    }
                    System.out.println(rs.toString(fields));
                }
                if (generatePdf) {
                    pdfDoc.add(new Chapter("Index", chapIndx));
                    pdfDoc.add(tocG.getToc());
                    pdfDoc.close();
                }
                if (generateXls) {
                    w.write();
                    w.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
