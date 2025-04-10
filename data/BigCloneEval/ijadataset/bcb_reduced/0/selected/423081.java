package org.freehep.graphicsio.pdf;

import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Delay <tt>Paint</tt> objects (gradient/texture, not color) for writing
 * pattern/shading/function dictionaries to the pdf file when the pageStream is
 * complete.<br>
 * TODO: - reuse pattern dictionaries if possible - cyclic function not working
 * yet (ps calculation)
 * 
 * @author Simon Fischer
 * @version $Id: PDFPaintDelayQueue.java 10270 2007-01-09 18:18:57Z duns $
 */
public class PDFPaintDelayQueue {

    private static int currentNumber = 0;

    private class Entry {

        private Paint paint;

        private String name;

        private AffineTransform trafo;

        private String writeAs;

        private boolean written;

        private Entry(Paint paint, AffineTransform trafo, String writeAs) {
            this.paint = paint;
            this.trafo = trafo;
            this.writeAs = writeAs;
            this.name = "Paint" + (currentNumber++);
            this.written = false;
        }
    }

    private List paintList;

    private PDFWriter pdf;

    private AffineTransform pageMatrix;

    /** Don't forget to call <tt>setPageMatrix()</tt>. */
    public PDFPaintDelayQueue(PDFWriter pdf, PDFImageDelayQueue imageDelayQueue) {
        this.pdf = pdf;
        this.paintList = new LinkedList();
        this.pageMatrix = new AffineTransform();
    }

    /**
     * Call this method in order to inform this class about the transformation
     * that is necessary to map the pattern's coordinate space to the default
     * coordinate system of the parent's content stream (in our case the
     * flipping of the page).
     */
    public void setPageMatrix(AffineTransform t) {
        pageMatrix = new AffineTransform(t);
    }

    public PDFName delayPaint(Paint paint, AffineTransform transform, String writeAs) {
        Entry e = new Entry(paint, transform, writeAs);
        paintList.add(e);
        return pdf.name(e.name);
    }

    /** Creates a stream for every delayed image. */
    public void processAll() throws IOException {
        ListIterator i = paintList.listIterator();
        while (i.hasNext()) {
            Entry e = (Entry) i.next();
            if (!e.written) {
                e.written = true;
                if (e.paint instanceof GradientPaint) {
                    addGradientPaint(e);
                } else if (e.paint instanceof TexturePaint) {
                    addTexturePaint(e);
                } else {
                    System.err.println("PDFWriter: Paint of class '" + e.paint.getClass() + "' not supported.");
                }
            }
        }
    }

    /**
     * Adds all names to the dictionary which should be the value of the
     * resources dicionrary's /Pattern entry.
     */
    public int addPatterns() throws IOException {
        if (paintList.size() > 0) {
            PDFDictionary patterns = pdf.openDictionary("Pattern");
            ListIterator i = paintList.listIterator();
            while (i.hasNext()) {
                Entry e = (Entry) i.next();
                patterns.entry(e.name, pdf.ref(e.name));
            }
            pdf.close(patterns);
        }
        return paintList.size();
    }

    private void addGradientPaint(Entry e) throws IOException {
        GradientPaint gp = (GradientPaint) e.paint;
        PDFDictionary pattern = pdf.openDictionary(e.name);
        pattern.entry("Type", pdf.name("Pattern"));
        pattern.entry("PatternType", 2);
        setMatrix(pattern, e, 0, 0);
        PDFDictionary shading = pattern.openDictionary("Shading");
        shading.entry("ShadingType", 2);
        shading.entry("ColorSpace", pdf.name("DeviceRGB"));
        Point2D p1 = gp.getPoint1();
        Point2D p2 = gp.getPoint2();
        shading.entry("Coords", new double[] { p1.getX(), p1.getY(), p2.getX(), p2.getY() });
        double[] domain = new double[] { 0, 1 };
        shading.entry("Domain", domain);
        String functionRef = e.name + "Function";
        shading.entry("Function", pdf.ref(functionRef));
        shading.entry("Extend", new boolean[] { true, true });
        pattern.close(shading);
        pdf.close(pattern);
        float[] col0 = new float[3];
        gp.getColor1().getRGBColorComponents(col0);
        double c0[] = new double[] { col0[0], col0[1], col0[2] };
        float[] col1 = new float[3];
        gp.getColor2().getRGBColorComponents(col1);
        double c1[] = new double[] { col1[0], col1[1], col1[2] };
        if (gp.isCyclic()) {
            addLinearFunction(functionRef, c0, c1, domain);
        } else {
            addLinearFunction(functionRef, c0, c1, domain);
        }
    }

    /** Writes a type 2 (exponential) function (exponent N=1) to the pdf file. */
    private void addLinearFunction(String functionRef, double[] c0, double[] c1, double[] dom) throws IOException {
        PDFDictionary function = pdf.openDictionary(functionRef);
        function.entry("FunctionType", 2);
        function.entry("Domain", dom);
        function.entry("Range", new double[] { 0., 1., 0., 1., 0., 1. });
        function.entry("C0", c0);
        function.entry("C1", c1);
        function.entry("N", 1);
        pdf.close(function);
    }

    protected void addCyclicFunction(String functionRef, double[] c0, double[] c1, double[] dom) throws IOException {
        PDFStream function = pdf.openStream(functionRef);
        function.entry("FunctionType", 4);
        function.entry("Domain", dom);
        function.entry("Range", new double[] { 0., 1., 0., 1., 0., 1. });
        function.println("{");
        for (int i = 0; i < 3; i++) {
            if (i < 2) function.println("dup");
            function.println((c1[i] - c0[i]) + " mul");
            function.println(c0[i] + " add");
            if (i < 2) function.println("exch");
        }
        function.println("}");
        pdf.close(function);
    }

    private void addTexturePaint(Entry e) throws IOException {
        TexturePaint tp = (TexturePaint) e.paint;
        PDFStream pattern = pdf.openStream(e.name, null);
        pattern.entry("Type", pdf.name("Pattern"));
        pattern.entry("PatternType", 1);
        pattern.entry("PaintType", 1);
        BufferedImage image = tp.getImage();
        pattern.entry("TilingType", 1);
        double width = tp.getAnchorRect().getWidth();
        double height = tp.getAnchorRect().getHeight();
        double offsX = tp.getAnchorRect().getX();
        double offsY = tp.getAnchorRect().getY();
        pattern.entry("BBox", new double[] { 0, 0, width, height });
        pattern.entry("XStep", width);
        pattern.entry("YStep", height);
        PDFDictionary resources = pattern.openDictionary("Resources");
        resources.entry("ProcSet", new Object[] { pdf.name("PDF"), pdf.name("ImageC") });
        pattern.close(resources);
        setMatrix(pattern, e, offsX, offsY);
        pattern.matrix(width, 0, 0, -height, 0, height);
        pattern.inlineImage(image, null, e.writeAs);
        pdf.close(pattern);
    }

    /**
     * Sets both the page (flip) matrix and the actual transformation matrix
     * plus a translation offset (which may of course be be 0).
     */
    private void setMatrix(PDFDictionary dict, Entry e, double translX, double translY) throws IOException {
        AffineTransform trafo = new AffineTransform(pageMatrix);
        trafo.concatenate(e.trafo);
        trafo.translate(translX, translY);
        double[] matrix = new double[6];
        trafo.getMatrix(matrix);
        dict.entry("Matrix", new double[] { matrix[0], matrix[1], matrix[2], matrix[3], matrix[4], matrix[5] });
    }
}
