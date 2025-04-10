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
        AffineTransform af = new AffineTransform();
        af.translate(-595, 0);
        af.scale(0.5, 0.5);
        canvas.transform(af);
        canvas.addTemplate(template, 0, 0);
        canvas.concatCTM(AffineTransform.getTranslateInstance(595, 595));
        canvas.addTemplate(template, 0, 0);
        canvas.restoreState();
        canvas.saveState();
        af = new AffineTransform(1, 0, 0.4, 1, -750, -650);
        canvas.addTemplate(template, af);
        canvas.restoreState();
        canvas.saveState();
        af = new AffineTransform(0, -1, -1, 0, 650, 0);
        canvas.addTemplate(template, af);
        af = new AffineTransform(0, -0.2f, -0.5f, 0, 350, 0);
        canvas.addTemplate(template, af);
        canvas.restoreState();
        document.close();
    }
}