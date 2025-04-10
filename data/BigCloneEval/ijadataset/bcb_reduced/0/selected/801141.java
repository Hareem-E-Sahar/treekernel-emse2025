package org.geotools.renderer.style;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.feature.Feature;
import org.geotools.filter.Expression;
import org.geotools.renderer.lite.CustomGlyphRenderer;
import org.geotools.renderer.lite.GlyphRenderer;
import org.geotools.renderer.lite.SVGGlyphRenderer;
import org.geotools.styling.*;
import org.geotools.styling.Font;
import javax.imageio.ImageIO;
import javax.media.jai.util.Range;
import java.awt.*;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SLDStyleFactory {

    /** The logger for the rendering module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.rendering");

    /** Holds a lookup bewteen SLD names and java constants. */
    private static final java.util.Map joinLookup = new java.util.HashMap();

    /** Holds a lookup bewteen SLD names and java constants. */
    private static final java.util.Map capLookup = new java.util.HashMap();

    /** Holds a lookup bewteen SLD names and java constants. */
    private static final java.util.Map fontStyleLookup = new java.util.HashMap();

    /** Set containing the font families known of this machine */
    private static Set fontFamilies = null;

    /** Fonts already loaded */
    private static Map loadedFonts = new HashMap();

    /** Holds the set of well-known marks. */
    static Set wellKnownMarks = new java.util.HashSet();

    /** Holds the of graphic formats supported by the current jdk */
    static Set supportedGraphicFormats = null;

    /** Current way to load images */
    static ImageLoader imageLoader = new ImageLoader();

    /** This one is used as the observer object in image tracks */
    private static final Canvas obs = new Canvas();

    /**
	 * This one holds the list of glyphRenderers that can convert glyphs into an
	 * image
	 */
    private static List glyphRenderers = new ArrayList();

    static {
        joinLookup.put("miter", new Integer(BasicStroke.JOIN_MITER));
        joinLookup.put("bevel", new Integer(BasicStroke.JOIN_BEVEL));
        joinLookup.put("round", new Integer(BasicStroke.JOIN_ROUND));
        capLookup.put("butt", new Integer(BasicStroke.CAP_BUTT));
        capLookup.put("round", new Integer(BasicStroke.CAP_ROUND));
        capLookup.put("square", new Integer(BasicStroke.CAP_SQUARE));
        fontStyleLookup.put("normal", new Integer(java.awt.Font.PLAIN));
        fontStyleLookup.put("italic", new Integer(java.awt.Font.ITALIC));
        fontStyleLookup.put("oblique", new Integer(java.awt.Font.ITALIC));
        fontStyleLookup.put("bold", new Integer(java.awt.Font.BOLD));
        wellKnownMarks.add("Square");
        wellKnownMarks.add("Triangle");
        wellKnownMarks.add("Cross");
        wellKnownMarks.add("Circle");
        wellKnownMarks.add("Star");
        wellKnownMarks.add("X");
        wellKnownMarks.add("Arrow");
        wellKnownMarks.add("Hatch");
        wellKnownMarks.add("square");
        wellKnownMarks.add("triangle");
        wellKnownMarks.add("cross");
        wellKnownMarks.add("circle");
        wellKnownMarks.add("star");
        wellKnownMarks.add("x");
        wellKnownMarks.add("arrow");
        wellKnownMarks.add("hatch");
        glyphRenderers.add(new CustomGlyphRenderer());
        try {
            glyphRenderers.add(new SVGGlyphRenderer());
        } catch (Exception e) {
            LOGGER.warning("Will not support SVG External Graphics " + e);
        }
    }

    /** Parsed SVG glyphs */
    WeakHashMap svgGlyphs = new WeakHashMap();

    /** Symbolizers that depend on attributes */
    WeakHashMap dynamicSymbolizers = new WeakHashMap();

    /** Symbolizers that do not depend on attributes */
    WeakHashMap staticSymbolizers = new WeakHashMap();

    private static Set getSupportedGraphicFormats() {
        if (supportedGraphicFormats == null) {
            supportedGraphicFormats = new java.util.HashSet();
            String[] types = ImageIO.getReaderMIMETypes();
            for (int i = 0; i < types.length; i++) {
                supportedGraphicFormats.add(types[i]);
            }
        }
        return supportedGraphicFormats;
    }

    private long hits;

    private long requests;

    /**
	 * Holds value of property mapScaleDenominator.
	 */
    private double mapScaleDenominator = Double.NaN;

    ;

    public double getHitRatio() {
        return (double) hits / (double) requests;
    }

    public long getHits() {
        return hits;
    }

    public long getRequests() {
        return requests;
    }

    /**
	 * <p>
	 * Creates a rendered style
	 * </p>
	 * 
	 * <p>
	 * Makes use of a symbolizer cache based on identity to avoid recomputing
	 * over and over the same style object and to reduce memory usage. The same
	 * Style2D object will be returned by subsequent calls using the same
	 * feature independent symbolizer with the same scaleRange.
	 * </p>
	 * 
	 * @param f
	 *            The feature
	 * @param symbolizer
	 *            The SLD symbolizer
	 * @param scaleRange
	 *            The scale range in which the feature should be painted
	 *            according to the symbolizer
	 * 
	 * @return A rendered style equivalent to the symbolizer
	 */
    public Style2D createStyle(Feature f, Symbolizer symbolizer, Range scaleRange) {
        Style2D style = null;
        SymbolizerKey key = new SymbolizerKey(symbolizer, scaleRange);
        style = (Style2D) staticSymbolizers.get(key);
        requests++;
        if (style != null) {
            hits++;
        } else {
            style = createStyleInternal(f, symbolizer, scaleRange);
            if (dynamicSymbolizers.containsKey(key)) {
                return style;
            } else {
                StyleAttributeExtractorTruncated sae = new StyleAttributeExtractorTruncated();
                sae.visit(symbolizer);
                Set nameSet = sae.getAttributeNameSet();
                if ((nameSet == null) || (nameSet.size() == 0)) {
                    staticSymbolizers.put(key, style);
                } else {
                    dynamicSymbolizers.put(key, null);
                }
            }
        }
        if (staticSymbolizers.size() > 10000) staticSymbolizers = new WeakHashMap();
        if (dynamicSymbolizers.size() > 10000) dynamicSymbolizers = new WeakHashMap();
        return style;
    }

    /**
	 * Really creates the symbolizer
	 * 
	 * @param f
	 *            DOCUMENT ME!
	 * @param symbolizer
	 *            DOCUMENT ME!
	 * @param scaleRange
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
    private Style2D createStyleInternal(Feature f, Symbolizer symbolizer, Range scaleRange) {
        Style2D style = null;
        if (symbolizer instanceof PolygonSymbolizer) {
            style = createPolygonStyle(f, (PolygonSymbolizer) symbolizer, scaleRange);
        } else if (symbolizer instanceof LineSymbolizer) {
            style = createLineStyle(f, (LineSymbolizer) symbolizer, scaleRange);
        } else if (symbolizer instanceof PointSymbolizer) {
            style = createPointStyle(f, (PointSymbolizer) symbolizer, scaleRange);
        } else if (symbolizer instanceof TextSymbolizer) {
            style = createTextStyle(f, (TextSymbolizer) symbolizer, scaleRange);
        }
        return style;
    }

    /**
	 * Creates a rendered style
	 * 
	 * @param f
	 *            The feature
	 * @param symbolizer
	 *            The SLD symbolizer
	 * @param scaleRange
	 *            The scale range in which the feature should be painted
	 *            according to the symbolizer
	 * 
	 * @return A rendered style equivalent to the symbolizer
	 * 
	 * @throws UnsupportedOperationException
	 *             if an unknown symbolizer is passed to this method
	 */
    public Style2D createDynamicStyle(Feature f, Symbolizer symbolizer, Range scaleRange) {
        Style2D style = null;
        if (symbolizer instanceof PolygonSymbolizer) {
            style = createDynamicPolygonStyle(f, (PolygonSymbolizer) symbolizer, scaleRange);
        } else if (symbolizer instanceof LineSymbolizer) {
            style = createDynamicLineStyle(f, (LineSymbolizer) symbolizer, scaleRange);
        } else {
            throw new UnsupportedOperationException("This kind of symbolizer is not yet supported");
        }
        return style;
    }

    Style2D createPolygonStyle(Feature feature, PolygonSymbolizer symbolizer, Range scaleRange) {
        PolygonStyle2D style = new PolygonStyle2D();
        setScaleRange(style, scaleRange);
        style.setStroke(getStroke(symbolizer.getStroke(), feature));
        style.setGraphicStroke(getGraphicStroke(symbolizer.getStroke(), feature));
        style.setContour(getStrokePaint(symbolizer.getStroke(), feature));
        style.setContourComposite(getStrokeComposite(symbolizer.getStroke(), feature));
        style.setFill(getPaint(symbolizer.getFill(), feature));
        style.setFillComposite(getComposite(symbolizer.getFill(), feature));
        return style;
    }

    Style2D createDynamicPolygonStyle(Feature feature, PolygonSymbolizer symbolizer, Range scaleRange) {
        PolygonStyle2D style = new DynamicPolygonStyle2D(feature, symbolizer);
        setScaleRange(style, scaleRange);
        return style;
    }

    Style2D createLineStyle(Feature feature, LineSymbolizer symbolizer, Range scaleRange) {
        LineStyle2D style = new LineStyle2D();
        setScaleRange(style, scaleRange);
        style.setStroke(getStroke(symbolizer.getStroke(), feature));
        style.setGraphicStroke(getGraphicStroke(symbolizer.getStroke(), feature));
        style.setContour(getStrokePaint(symbolizer.getStroke(), feature));
        style.setContourComposite(getStrokeComposite(symbolizer.getStroke(), feature));
        return style;
    }

    Style2D createDynamicLineStyle(Feature feature, LineSymbolizer symbolizer, Range scaleRange) {
        LineStyle2D style = new DynamicLineStyle2D(feature, symbolizer);
        setScaleRange(style, scaleRange);
        return style;
    }

    Style2D createPointStyle(Feature feature, PointSymbolizer symbolizer, Range scaleRange) {
        Style2D retval = null;
        Symbol[] symbols = symbolizer.getGraphic().getMarks();
        float opacity = evalOpacity(symbolizer.getGraphic().getOpacity(), feature);
        int size;
        try {
            size = (int) evalToDouble(symbolizer.getGraphic().getSize(), feature, 10);
            if (size <= 0) {
                size = 10;
            }
        } catch (NumberFormatException nfe) {
            size = 10;
        }
        float rotation = 0;
        rotation = (float) ((evalToFloat(symbolizer.getGraphic().getRotation(), feature, 0) * Math.PI) / 180);
        final int length = symbols.length;
        ExternalGraphic eg;
        GlyphRenderer r;
        BufferedImage img = null;
        double dsize;
        AffineTransform scaleTx;
        AffineTransformOp ato;
        BufferedImage scaledImage;
        Mark mark;
        Shape shape;
        MarkStyle2D ms2d;
        for (int i = 0; i < length; i++) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("trying to render symbol " + i);
            }
            if (symbols[i] instanceof ExternalGraphic) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("rendering External graphic");
                }
                eg = (ExternalGraphic) symbols[i];
                img = null;
                for (Iterator it = glyphRenderers.iterator(); it.hasNext() && (img == null); ) {
                    r = (GlyphRenderer) it.next();
                    if (r.canRender(eg.getFormat())) {
                        img = r.render(symbolizer.getGraphic(), eg, feature, 10);
                        break;
                    }
                }
                if (img == null) {
                    img = getImage(eg, size);
                }
                if (img == null) {
                    continue;
                }
                if (img.getHeight() != size) {
                    dsize = (double) size;
                    double scaleY = dsize / img.getHeight();
                    double scaleX = scaleY;
                    scaleTx = AffineTransform.getScaleInstance(scaleX, scaleY);
                    ato = new AffineTransformOp(scaleTx, AffineTransformOp.TYPE_BILINEAR);
                    scaledImage = ato.createCompatibleDestImage(img, img.getColorModel());
                    img = ato.filter(img, scaledImage);
                }
                if (img != null) {
                    retval = new GraphicStyle2D(img, 0, opacity);
                    break;
                }
            }
            if (symbols[i] instanceof Mark) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("rendering mark @ PointRenderer " + symbols[i].toString());
                }
                mark = (Mark) symbols[i];
                String markName = mark.getWellKnownName().evaluate((Object) feature).toString();
                shape = Java2DMark.getWellKnownMark(markName);
                ms2d = new MarkStyle2D();
                ms2d.setShape(shape);
                ms2d.setFill(getPaint(mark.getFill(), feature));
                ms2d.setFillComposite(getComposite(mark.getFill(), feature));
                ms2d.setStroke(getStroke(mark.getStroke(), feature));
                ms2d.setContour(getStrokePaint(mark.getStroke(), feature));
                ms2d.setContourComposite(getStrokeComposite(mark.getStroke(), feature));
                ms2d.setSize(size);
                if (rotation != 0.0) ms2d.setRotation(rotation);
                retval = ms2d;
                break;
            }
            if (symbols[i] instanceof TextMark) {
                continue;
            }
        }
        if (retval != null) {
            setScaleRange(retval, scaleRange);
        }
        return retval;
    }

    Style2D createTextStyle(Feature feature, TextSymbolizer symbolizer, Range scaleRange) {
        TextStyle2D ts2d = new TextStyle2D();
        setScaleRange(ts2d, scaleRange);
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("creating text style");
        }
        String geomName = symbolizer.getGeometryPropertyName();
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("geomName = " + geomName);
        }
        Geometry geom = findGeometry(feature, geomName);
        if ((geom == null) || geom.isEmpty()) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("empty geometry");
            }
            return null;
        }
        Object obj = symbolizer.getLabel().getValue(feature);
        if (obj == null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("Null label in render text");
            }
            return null;
        }
        String label = obj.toString();
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("label is " + label);
        }
        if (label == null) {
            return null;
        }
        ts2d.setLabel(label);
        Font[] fonts = symbolizer.getFonts();
        java.awt.Font javaFont = getFont(feature, fonts);
        ts2d.setFont(javaFont);
        LabelPlacement placement = symbolizer.getLabelPlacement();
        double anchorX = 0;
        double anchorY = 0;
        double rotation = 0;
        double dispX = 0;
        double dispY = 0;
        if (placement instanceof PointPlacement) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("setting pointPlacement");
            }
            PointPlacement p = (PointPlacement) placement;
            anchorX = ((Number) p.getAnchorPoint().getAnchorPointX().getValue(feature)).doubleValue();
            anchorY = ((Number) p.getAnchorPoint().getAnchorPointY().getValue(feature)).doubleValue();
            dispX = ((Number) p.getDisplacement().getDisplacementX().getValue(feature)).doubleValue();
            dispY = ((Number) p.getDisplacement().getDisplacementY().getValue(feature)).doubleValue();
            if ((symbolizer instanceof TextSymbolizer2) && (((TextSymbolizer2) symbolizer).getGraphic() != null)) {
                rotation = 0.0;
            } else {
                rotation = ((Number) p.getRotation().getValue(feature)).doubleValue();
                rotation *= (Math.PI / 180.0);
            }
            ts2d.setPointPlacement(true);
        } else if (placement instanceof LinePlacement) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("setting pointPlacement");
            }
            ts2d.setPointPlacement(false);
            LinePlacement p = (LinePlacement) placement;
            int displace = ((Number) p.getPerpendicularOffset().getValue(feature)).intValue();
            ts2d.setPerpendicularOffset(displace);
        }
        ts2d.setAnchorX(anchorX);
        ts2d.setAnchorY(anchorY);
        ts2d.setRotation((float) rotation);
        ts2d.setDisplacementX(dispX);
        ts2d.setDisplacementY(dispY);
        ts2d.setFill(getPaint(symbolizer.getFill(), feature));
        ts2d.setComposite(getComposite(symbolizer.getFill(), feature));
        Halo halo = symbolizer.getHalo();
        if (halo != null) {
            ts2d.setHaloFill(getPaint(halo.getFill(), feature));
            ts2d.setHaloComposite(getComposite(halo.getFill(), feature));
            ts2d.setHaloRadius(((Number) halo.getRadius().getValue(feature)).floatValue());
        }
        Graphic graphicShield = null;
        if (symbolizer instanceof TextSymbolizer2) {
            graphicShield = ((TextSymbolizer2) symbolizer).getGraphic();
            if (graphicShield != null) {
                PointSymbolizer p = StyleFactoryFinder.createStyleFactory().createPointSymbolizer();
                p.setGraphic(graphicShield);
                Style2D shieldStyle = createPointStyle(feature, p, scaleRange);
                ts2d.setGraphic(shieldStyle);
            }
        }
        return ts2d;
    }

    /**
	 * Extracts the named geometry from feature. If geomName is null then the
	 * feature's default geometry is used. If geomName cannot be found in
	 * feature then null is returned.
	 * 
	 * @param feature
	 *            The feature to find the geometry in
	 * @param geomName
	 *            The name of the geometry to find: null if the default geometry
	 *            should be used.
	 * 
	 * @return The geometry extracted from feature or null if this proved
	 *         impossible.
	 */
    private Geometry findGeometry(final Feature feature, final String geomName) {
        Geometry geom = null;
        if (geomName == null) {
            geom = feature.getDefaultGeometry();
        } else {
            geom = (Geometry) feature.getAttribute(geomName);
        }
        return geom;
    }

    /**
	 * Returns the first font associated to the feature that can be found on the
	 * current machine
	 * 
	 * @param feature
	 *            The feature whose font is to be found
	 * @param fonts
	 *            An array of fonts dependent of the feature, the first that is
	 *            found on the current machine is returned
	 * 
	 * @return The first of the specified fonts found on this machine or null if
	 *         none found
	 */
    private java.awt.Font getFont(Feature feature, Font[] fonts) {
        if (fontFamilies == null) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            fontFamilies = new HashSet();
            List f = Arrays.asList(ge.getAvailableFontFamilyNames());
            fontFamilies.addAll(f);
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("there are " + fontFamilies.size() + " fonts available");
            }
        }
        java.awt.Font javaFont = null;
        int styleCode = 0;
        int size = 6;
        String requestedFont = "";
        for (int k = 0; k < fonts.length; k++) {
            requestedFont = fonts[k].getFontFamily().getValue(feature).toString();
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("trying to load " + requestedFont);
            }
            if (loadedFonts.containsKey(requestedFont)) {
                javaFont = (java.awt.Font) loadedFonts.get(requestedFont);
                String reqStyle = (String) fonts[k].getFontStyle().getValue(feature);
                if (fontStyleLookup.containsKey(reqStyle)) {
                    styleCode = ((Integer) fontStyleLookup.get(reqStyle)).intValue();
                } else {
                    styleCode = java.awt.Font.PLAIN;
                }
                String reqWeight = (String) fonts[k].getFontWeight().getValue(feature);
                if (reqWeight.equalsIgnoreCase("Bold")) {
                    styleCode = styleCode | java.awt.Font.BOLD;
                }
                size = ((Number) fonts[k].getFontSize().getValue(feature)).intValue();
                return javaFont.deriveFont(styleCode, size);
            }
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("not already loaded");
            }
            if (fontFamilies.contains(requestedFont)) {
                String reqStyle = (String) fonts[k].getFontStyle().getValue(feature);
                if (fontStyleLookup.containsKey(reqStyle)) {
                    styleCode = ((Integer) fontStyleLookup.get(reqStyle)).intValue();
                } else {
                    styleCode = java.awt.Font.PLAIN;
                }
                String reqWeight = (String) fonts[k].getFontWeight().getValue(feature);
                if (reqWeight.equalsIgnoreCase("Bold")) {
                    styleCode = styleCode | java.awt.Font.BOLD;
                }
                size = ((Number) fonts[k].getFontSize().getValue(feature)).intValue();
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("requesting " + requestedFont + " " + styleCode + " " + size);
                }
                javaFont = new java.awt.Font(requestedFont, styleCode, size);
                loadedFonts.put(requestedFont, javaFont);
                return javaFont;
            }
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("not a system font");
            }
            InputStream is = null;
            if (requestedFont.startsWith("http") || requestedFont.startsWith("file:")) {
                try {
                    URL url = new URL(requestedFont);
                    is = url.openStream();
                } catch (MalformedURLException mue) {
                    if (LOGGER.isLoggable(Level.INFO)) {
                        LOGGER.info("Bad url in SLDStyleFactory " + requestedFont + "\n" + mue);
                    }
                } catch (IOException ioe) {
                    if (LOGGER.isLoggable(Level.INFO)) {
                        LOGGER.info("IO error in SLDStyleFactory " + requestedFont + "\n" + ioe);
                    }
                }
            } else {
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("not a URL");
                }
                File file = new File(requestedFont);
                try {
                    is = new FileInputStream(file);
                } catch (FileNotFoundException fne) {
                    if (LOGGER.isLoggable(Level.INFO)) {
                        LOGGER.info("Bad file name in SLDStyleFactory" + requestedFont + "\n" + fne);
                    }
                }
            }
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("about to load");
            }
            if (is == null) {
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info("null input stream");
                }
                continue;
            }
            try {
                javaFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, is);
            } catch (FontFormatException ffe) {
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info("Font format error in SLDStyleFactory " + requestedFont + "\n" + ffe);
                }
                continue;
            } catch (IOException ioe) {
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info("IO error in SLDStyleFactory " + requestedFont + "\n" + ioe);
                }
                continue;
            }
            loadedFonts.put(requestedFont, javaFont);
            return javaFont;
        }
        return new java.awt.Font("Serif", java.awt.Font.PLAIN, 12);
    }

    void setScaleRange(Style style, Range scaleRange) {
        double min = ((Number) scaleRange.getMinValue()).doubleValue();
        double max = ((Number) scaleRange.getMaxValue()).doubleValue();
        style.setMinMaxScale(min, max);
    }

    private BufferedImage getGraphicStroke(org.geotools.styling.Stroke stroke, Feature feature) {
        if ((stroke == null) || (stroke.getGraphicStroke() == null)) {
            return null;
        }
        Graphic graphicStroke = stroke.getGraphicStroke();
        BufferedImage image = getExternalGraphic(graphicStroke);
        double size = ((Number) graphicStroke.getSize().getValue(feature)).doubleValue();
        if (image != null) {
            int trueImageWidth = image.getWidth();
            int trueImageHeight = image.getHeight();
            double scalex = size / trueImageWidth;
            double scaley = size / trueImageWidth;
            AffineTransform at = AffineTransform.getScaleInstance(scalex, scaley);
            AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
            BufferedImage scaledImage = ato.createCompatibleDestImage(image, image.getColorModel());
            ato.filter(image, scaledImage);
            image = scaledImage;
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("got an image in graphic fill");
            }
        } else {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("going for the mark from graphic fill");
            }
            Mark mark = getMark(graphicStroke, feature);
            image = new BufferedImage((int) size, (int) size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D ig2d = image.createGraphics();
            double rotation = 0.0;
            rotation = ((Number) graphicStroke.getRotation().getValue(feature)).doubleValue();
            rotation *= (Math.PI / 180.0);
            fillDrawMark(ig2d, size / 2, size / 2, mark, (int) size, rotation, feature);
            MediaTracker track = new MediaTracker(obs);
            track.addImage(image, 1);
            try {
                track.waitForID(1);
            } catch (InterruptedException e) {
                LOGGER.warning(e.toString());
            }
        }
        return image;
    }

    private Stroke getStroke(org.geotools.styling.Stroke stroke, Feature feature) {
        if (stroke == null) {
            return null;
        }
        String joinType;
        int joinCode;
        joinType = evaluateExpression(stroke.getLineJoin(), feature, "miter");
        if (joinLookup.containsKey(joinType)) {
            joinCode = ((Integer) joinLookup.get(joinType)).intValue();
        } else {
            joinCode = java.awt.BasicStroke.JOIN_MITER;
        }
        String capType;
        int capCode;
        capType = evaluateExpression(stroke.getLineCap(), feature, "square");
        if (capLookup.containsKey(capType)) {
            capCode = ((Integer) capLookup.get(capType)).intValue();
        } else {
            capCode = java.awt.BasicStroke.CAP_SQUARE;
        }
        float[] dashes = stroke.getDashArray();
        float width = evalToFloat(stroke.getWidth(), feature, 1);
        float dashOffset = evalToFloat(stroke.getDashOffset(), feature, 0);
        if (width < 1.5) {
            width = 0;
        }
        BasicStroke stroke2d;
        if ((dashes != null) && (dashes.length > 0)) {
            stroke2d = new BasicStroke(width, capCode, joinCode, 1, dashes, dashOffset);
        } else {
            stroke2d = new BasicStroke(width, capCode, joinCode, 1);
        }
        return stroke2d;
    }

    private Paint getStrokePaint(org.geotools.styling.Stroke stroke, Feature feature) {
        if (stroke == null) {
            return null;
        }
        Paint contourPaint = evalToColor(stroke.getColor(), feature, Color.BLACK);
        org.geotools.styling.Graphic gr = stroke.getGraphicFill();
        if (gr != null) {
            contourPaint = getTexturePaint(gr, feature);
        }
        return contourPaint;
    }

    private Composite getStrokeComposite(org.geotools.styling.Stroke stroke, Feature feature) {
        if (stroke == null) {
            return null;
        }
        float opacity = evalOpacity(stroke.getOpacity(), feature);
        Composite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
        return composite;
    }

    protected Paint getPaint(Fill fill, Feature feature) {
        if (fill == null) {
            return null;
        }
        Paint fillPaint = null;
        if (fill.getColor() != null) {
            fillPaint = Color.decode((String) fill.getColor().getValue(feature));
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("Setting fill: " + fillPaint.toString());
            }
        }
        org.geotools.styling.Graphic gr = fill.getGraphicFill();
        if (gr != null) {
            fillPaint = getTexturePaint(gr, feature);
        }
        return fillPaint;
    }

    /**
	 * Computes the Composite equivalent to the opacity in the SLD Fill
	 * 
	 * @param fill
	 * @param feature
	 * 
	 */
    protected Composite getComposite(Fill fill, Feature feature) {
        if (fill == null) {
            return null;
        }
        float opacity = evalOpacity(fill.getOpacity(), feature);
        Composite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
        return composite;
    }

    /**
	 * DOCUMENT ME!
	 * 
	 * @param gr
	 *            DOCUMENT ME!
	 * @param feature
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
    public TexturePaint getTexturePaint(org.geotools.styling.Graphic gr, Feature feature) {
        BufferedImage image = getExternalGraphic(gr);
        if (image != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("got an image in graphic fill");
            }
        } else {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("going for the mark from graphic fill");
            }
            org.geotools.styling.Mark mark = getMark(gr, feature);
            if (mark == null) {
                return null;
            }
            int size = 200;
            image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            double rotation = 0.0;
            rotation = ((Number) gr.getRotation().getValue(feature)).doubleValue();
            rotation *= (Math.PI / 180.0);
            fillDrawMark(g2d, 100, 100, mark, (int) (size * .9), rotation, feature);
            java.awt.MediaTracker track = new java.awt.MediaTracker(obs);
            track.addImage(image, 1);
            try {
                track.waitForID(1);
            } catch (InterruptedException e) {
                LOGGER.warning("An unterupptedException occurred while drawing a local image..." + e);
            }
        }
        int size = ((Number) gr.getSize().getValue(feature)).intValue();
        double width = image.getWidth();
        double height = image.getHeight();
        double unitSize = Math.max(width, height);
        double drawSize = (double) size / unitSize;
        width *= drawSize;
        height *= -drawSize;
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("size = " + size + " unitsize " + unitSize + " drawSize " + drawSize);
        }
        Rectangle2D.Double rect = new Rectangle2D.Double(0.0, 0.0, width, height);
        TexturePaint imagePaint = new TexturePaint(image, rect);
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("applied TexturePaint " + imagePaint);
        }
        return imagePaint;
    }

    private BufferedImage getExternalGraphic(Graphic graphic) {
        ExternalGraphic[] extgraphics = graphic.getExternalGraphics();
        if (extgraphics != null) {
            for (int i = 0; i < extgraphics.length; i++) {
                ExternalGraphic eg = extgraphics[i];
                BufferedImage img = getImage(eg, -1);
                if (img != null) {
                    return img;
                }
            }
        }
        return null;
    }

    private BufferedImage getImage(ExternalGraphic eg, int sizeHint) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("got a " + eg.getFormat());
        }
        BufferedImage img;
        if (getSupportedGraphicFormats().contains(eg.getFormat().toLowerCase())) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("a java supported format");
            }
            try {
                img = imageLoader.get(eg.getLocation(), false);
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("Image return = " + img);
                }
                return img;
            } catch (java.net.MalformedURLException e) {
                LOGGER.warning("ExternalGraphic has a malformed url: " + e);
            }
        }
        return null;
    }

    private Mark getMark(Graphic graphic, Feature feature) {
        Mark[] marks = graphic.getMarks();
        Mark mark;
        for (int i = 0; i < marks.length; i++) {
            String name = marks[i].getWellKnownName().evaluate(feature).toString();
            if (wellKnownMarks.contains(name)) {
                mark = marks[i];
                return mark;
            }
        }
        mark = null;
        return mark;
    }

    private void fillDrawMark(Graphics2D graphic, double tx, double ty, Mark mark, int size, double rotation, Feature feature) {
        AffineTransform temp = graphic.getTransform();
        AffineTransform markAT = new AffineTransform();
        String markName = mark.getWellKnownName().evaluate((Object) feature).toString();
        Shape shape = Java2DMark.getWellKnownMark(markName);
        Point2D mapCentre = new Point2D.Double(tx, ty);
        Point2D graphicCentre = new Point2D.Double();
        temp.transform(mapCentre, graphicCentre);
        markAT.translate(graphicCentre.getX(), graphicCentre.getY());
        double shearY = temp.getShearY();
        double scaleY = temp.getScaleY();
        double originalRotation = Math.atan(shearY / scaleY);
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("originalRotation " + originalRotation);
        }
        markAT.rotate(rotation - originalRotation);
        double unitSize = 1.0;
        double drawSize = (double) size / unitSize;
        markAT.scale(drawSize, -drawSize);
        graphic.setTransform(markAT);
        if (mark.getFill() != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("applying fill to mark");
            }
            graphic.setPaint(getPaint(mark.getFill(), null));
            graphic.setComposite(getComposite(mark.getFill(), null));
            graphic.fill(shape);
        }
        if (mark.getStroke() != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("applying stroke to mark");
            }
            graphic.setPaint(getStrokePaint(mark.getStroke(), null));
            graphic.setComposite(getStrokeComposite(mark.getStroke(), null));
            graphic.setStroke(getStroke(mark.getStroke(), null));
            graphic.draw(shape);
        }
        graphic.setTransform(temp);
        if (mark.getFill() != null) {
            graphic.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        return;
    }

    /**
	 * Evaluates an expression over the passed feature, if the expression or the
	 * result is null, the default value will be returned
	 * 
	 * @param e
	 * @param feature
	 * @param defaultValue
	 * 
	 */
    private String evaluateExpression(Expression e, Feature feature, String defaultValue) {
        String result = defaultValue;
        if (e != null) {
            result = (String) e.getValue(feature);
            if (result == null) {
                result = defaultValue;
            }
        }
        return result;
    }

    /**
	 * DOCUMENT ME!
	 * 
	 * @param joinType
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
    public static int lookUpJoin(String joinType) {
        if (SLDStyleFactory.joinLookup.containsKey(joinType)) {
            return ((Integer) joinLookup.get(joinType)).intValue();
        } else {
            return java.awt.BasicStroke.JOIN_MITER;
        }
    }

    /**
	 * DOCUMENT ME!
	 * 
	 * @param capType
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
    public static int lookUpCap(String capType) {
        if (SLDStyleFactory.capLookup.containsKey(capType)) {
            return ((Integer) capLookup.get(capType)).intValue();
        } else {
            return java.awt.BasicStroke.CAP_SQUARE;
        }
    }

    /**
	 * Getter for property mapScaleDenominator.
	 * 
	 * @return Value of property mapScaleDenominator.
	 */
    public double getMapScaleDenominator() {
        return this.mapScaleDenominator;
    }

    /**
	 * Setter for property mapScaleDenominator.
	 * 
	 * @param mapScaleDenominator
	 *            New value of property mapScaleDenominator.
	 */
    public void setMapScaleDenominator(double mapScaleDenominator) {
        this.mapScaleDenominator = mapScaleDenominator;
    }

    /**
	 * Simple key used to cache Style2D objects based on the originating
	 * symbolizer and scale range. Will compare symbolizers by identity,
	 * avoiding a possibly very long comparison
	 * 
	 * @author aaime
	 */
    private static class SymbolizerKey {

        private Symbolizer symbolizer;

        private double minScale;

        private double maxScale;

        public SymbolizerKey(Symbolizer symbolizer, Range scaleRange) {
            this.symbolizer = symbolizer;
            minScale = ((Number) scaleRange.getMinValue()).doubleValue();
            maxScale = ((Number) scaleRange.getMaxValue()).doubleValue();
        }

        /**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
        public boolean equals(Object obj) {
            if (!(obj instanceof SymbolizerKey)) {
                return false;
            }
            SymbolizerKey other = (SymbolizerKey) obj;
            return (other.symbolizer == symbolizer) && (other.minScale == minScale) && (other.maxScale == maxScale);
        }

        /**
		 * @see java.lang.Object#hashCode()
		 */
        public int hashCode() {
            return ((((17 + symbolizer.hashCode()) * 37) + doubleHash(minScale)) * 37) + doubleHash(maxScale);
        }

        private int doubleHash(double value) {
            long bits = Double.doubleToLongBits(value);
            return (int) (bits ^ (bits >>> 32));
        }
    }

    private float evalToFloat(Expression exp, Feature f, float fallback) {
        try {
            if (exp == null || exp.evaluate(f) == null) {
                return 10;
            }
            String value = exp.evaluate(f).toString();
            return Float.parseFloat(value);
        } catch (NumberFormatException nfe) {
            return 10;
        }
    }

    private double evalToDouble(Expression exp, Feature f, double fallback) {
        try {
            if (exp == null || exp.evaluate(f) == null) {
                return 10;
            }
            String value = exp.evaluate(f).toString();
            return Double.parseDouble(value);
        } catch (NumberFormatException nfe) {
            return 10.0;
        }
    }

    private Color evalToColor(Expression exp, Feature f, Color fallback) {
        if (exp == null) {
            return fallback;
        }
        try {
            return Color.decode((String) exp.getValue(f));
        } catch (NumberFormatException nfe) {
            return fallback;
        }
    }

    private float evalOpacity(Expression e, Feature f) {
        return evalToFloat(e, f, 1);
    }
}
