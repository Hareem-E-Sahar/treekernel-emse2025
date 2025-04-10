public class Test {    public static void main(String[] args) throws IOException, DocumentException {
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