public class Test {    public void createPdf(String filename) throws IOException, DocumentException {
        Rectangle rect = new Rectangle(-595, -842, 595, 842);
        Document document = new Document(rect);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
        document.open();
        PdfContentByte canvas = writer.getDirectContent();
        canvas.moveTo(-595, 0);
        canvas.lineTo(595, 0);
        canvas.moveTo(0, -842);
        canvas.lineTo(0, 842);
        canvas.stroke();
        PdfReader reader = new PdfReader(RESOURCE);
        PdfTemplate template = writer.getImportedPage(reader, 1);
        canvas.saveState();
        canvas.addTemplate(template, 0, 0);
        canvas.concatCTM(0.5f, 0, 0, 0.5f, -595, 0);
        canvas.addTemplate(template, 0, 0);
        canvas.concatCTM(1, 0, 0, 1, 595, 595);
        canvas.addTemplate(template, 0, 0);
        canvas.restoreState();
        canvas.saveState();
        canvas.concatCTM(1, 0, 0.4f, 1, -750, -650);
        canvas.addTemplate(template, 0, 0);
        canvas.restoreState();
        canvas.saveState();
        canvas.concatCTM(0, -1, -1, 0, 650, 0);
        canvas.addTemplate(template, 0, 0);
        canvas.concatCTM(0.2f, 0, 0, 0.5f, 0, 300);
        canvas.addTemplate(template, 0, 0);
        canvas.restoreState();
        document.close();
    }
}