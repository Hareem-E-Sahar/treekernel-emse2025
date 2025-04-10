public class Test {    public void createPdf(String filename) throws IOException, DocumentException {
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
        document.open();
        Type3Font t3 = new Type3Font(writer, true);
        PdfContentByte d = t3.defineGlyph('D', 600, 0, 0, 600, 700);
        d.setColorStroke(new BaseColor(0xFF, 0x00, 0x00));
        d.setColorFill(new GrayColor(0.7f));
        d.setLineWidth(100);
        d.moveTo(5, 5);
        d.lineTo(300, 695);
        d.lineTo(595, 5);
        d.closePathFillStroke();
        PdfContentByte s = t3.defineGlyph('S', 600, 0, 0, 600, 700);
        s.setColorStroke(new BaseColor(0x00, 0x80, 0x80));
        s.setLineWidth(100);
        s.moveTo(595, 5);
        s.lineTo(5, 5);
        s.lineTo(300, 350);
        s.lineTo(5, 695);
        s.lineTo(595, 695);
        s.stroke();
        Font f = new Font(t3, 12);
        Paragraph p = new Paragraph();
        p.add("This is a String with a Type3 font that contains a fancy Delta (");
        p.add(new Chunk("D", f));
        p.add(") and a custom Sigma (");
        p.add(new Chunk("S", f));
        p.add(").");
        document.add(p);
        document.close();
    }
}