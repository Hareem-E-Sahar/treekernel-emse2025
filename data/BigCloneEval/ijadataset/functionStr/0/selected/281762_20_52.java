public class Test {    protected BufferedImage getImage() throws Exception {
        InputStream in = params.connection;
        PDF pdf = (PDF) Behavior.getInstance("AdobePDF", "AdobePDF", null, null, null);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[TEMP_COPY_BUFFER_SIZE];
        int len = 0;
        while ((len = in.read(buffer)) > 0) outputStream.write(buffer, 0, len);
        in.close();
        outputStream.close();
        pdf.setInput(new InputUniByteArray(outputStream.toByteArray()));
        multivalent.Document doc = new multivalent.Document("doc", null, null);
        doc.clear();
        doc.putAttr(multivalent.Document.ATTR_PAGE, Integer.toString(PDF_RENDER_PAGE));
        pdf.parse(doc);
        Node top = doc.childAt(0);
        doc.formatBeforeAfter(200, 200, null);
        int pdfWidth = top.bbox.width;
        int pdfHeight = top.bbox.height;
        BufferedImage bimage = new BufferedImage(pdfWidth, pdfHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bimage.createGraphics();
        g.setClip(0, 0, pdfWidth, pdfHeight);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        Context cx = doc.getStyleSheet().getContext(g, null);
        top.paintBeforeAfter(g.getClipBounds(), cx);
        bimage = scaleImage(bimage);
        doc.removeAllChildren();
        cx.reset();
        g.dispose();
        pdf.getReader().close();
        return bimage;
    }
}