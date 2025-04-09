package app;

import gui.macros.SimpleMacro;
import java.awt.Color;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import util.Version;
import math.geom2d.*;
import math.geom2d.conic.*;
import math.geom2d.curve.Curve2D;
import math.geom2d.domain.Domain2D;
import math.geom2d.line.LineSegment2D;
import math.geom2d.line.LinearShape2D;
import math.geom2d.line.Ray2D;
import math.geom2d.line.StraightLine2D;
import math.geom2d.point.PointSet2D;
import math.geom2d.point.PointShape2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polyline2D;
import math.geom2d.transform.CircleInversion2D;
import math.geom2d.transform.Transform2D;
import model.EuclideDoc;
import model.EuclideLayer;
import model.EuclideFigure;
import model.EuclideSheet;
import model.event.EuclideDocEvent;
import model.event.EuclideDocListener;
import model.style.DefaultDrawStyle;
import model.style.DrawStyle;
import model.style.Marker;
import model.style.DrawStyle.EndCap;
import model.style.DrawStyle.FillType;
import model.style.DrawStyle.LineJoin;
import model.style.DrawStyle.LineWidthUnit;
import model.style.DrawStyle.MarkerSizeUnit;
import app.event.EuclideAppEvent;
import app.event.EuclideAppListener;
import dynamic.*;

/**
 * EuclideApp manages changes made to documents. It also distributes 
 * DocumentChangeEvent to DocumentListener.
 * 
 * @author dlegland
 */
public class EuclideApp implements EuclideDocListener {

    public static final String defaultBaseTitle = "Euclide";

    public static final String defaultNoNameLabel = "NoName";

    public static final Version version = new Version(0, 6, 6);

    /** 
	 * The base string for creating names for transforms, measures, or
	 * predicates. 
	 */
    private static final String baseNameFormat = "%1$s%2$02d";

    public enum AppMarker {

        NO_CHANGE(null, "No Change"), CIRCLE(Marker.CIRCLE, "Circle"), SQUARE(Marker.SQUARE, "Square"), PLUS(Marker.PLUS, "Plus"), CROSS(Marker.CROSS, "Cross"), DIAMOND(Marker.DIAMOND, "Diamond");

        private Marker marker;

        private String name;

        AppMarker(Marker marker, String name) {
            this.marker = marker;
            this.name = name;
        }

        public Marker getMarker() {
            return marker;
        }

        ;

        public String getName() {
            return name;
        }

        ;

        @Override
        public String toString() {
            return name;
        }

        ;
    }

    public enum AppMarkerSizeUnit {

        NO_CHANGE(null, "No Change"), USER(MarkerSizeUnit.USER, "User unit"), PIXEL(MarkerSizeUnit.PIXEL, "Pixel");

        private MarkerSizeUnit unit;

        private String name;

        AppMarkerSizeUnit(MarkerSizeUnit unit, String name) {
            this.unit = unit;
            this.name = name;
        }

        public MarkerSizeUnit getUnit() {
            return unit;
        }

        ;

        public String getName() {
            return name;
        }

        ;

        @Override
        public String toString() {
            return name;
        }

        ;
    }

    public enum AppColor {

        NO_CHANGE(null, "No Change"), RED(Color.RED, "Red"), GREEN(Color.GREEN, "Green"), BLUE(Color.BLUE, "Blue"), CYAN(Color.CYAN, "Cyan"), MAGENTA(Color.MAGENTA, "Magenta"), YELLOW(Color.YELLOW, "Yellow"), DARK_RED(new Color(127, 0, 0), "Dark red"), DARK_GREEN(new Color(0, 127, 0), "Dark green"), DARK_BLUE(new Color(0, 0, 127), "Dark blue"), BLACK(Color.BLACK, "Black"), DARK_GRAY(new Color(63, 63, 63), "Dark gray"), GRAY(Color.GRAY, "Gray"), LIGHT_GRAY(new Color(191, 191, 191), "Light gray"), WHITE(Color.WHITE, "White");

        private Color color;

        private String name;

        AppColor(Color color, String name) {
            this.color = color;
            this.name = name;
        }

        public Color getColor() {
            return color;
        }

        ;

        public String getName() {
            return name;
        }

        ;

        @Override
        public String toString() {
            return name;
        }

        ;
    }

    public enum AppLineWidthUnit {

        NO_CHANGE(null, "No Change"), USER(LineWidthUnit.USER, "User unit"), PIXEL(LineWidthUnit.PIXEL, "Pixel");

        private LineWidthUnit unit;

        private String name;

        AppLineWidthUnit(LineWidthUnit unit, String name) {
            this.unit = unit;
            this.name = name;
        }

        public LineWidthUnit getUnit() {
            return unit;
        }

        ;

        public String getName() {
            return name;
        }

        ;

        @Override
        public String toString() {
            return name;
        }

        ;
    }

    public enum AppDash {

        NO_CHANGE(null, "No Change"), CONTINUOUS(new float[0], "Continuous"), DOTTED(new float[] { 1, 1 }, "Dotted"), DASH2(new float[] { 2, 2 }, "Small dash"), DASH4(new float[] { 4, 4 }, "Normal dash"), DASH6(new float[] { 6, 6 }, "Large dash"), ALT1_2_5_2(new float[] { 1, 2, 5, 2 }, "Alternate (small)"), ALT2_4_10_4(new float[] { 2, 4, 10, 4 }, "Alternate (large)");

        private float[] dash;

        private String name;

        AppDash(float[] dash, String name) {
            this.dash = dash;
            this.name = name;
        }

        public float[] getDash() {
            return dash;
        }

        ;

        public String getName() {
            return name;
        }

        ;

        @Override
        public String toString() {
            return name;
        }

        ;
    }

    public enum AppLineJoin {

        NO_CHANGE(null, "No Change"), BEVEL(LineJoin.BEVEL, "Bevel"), MITER(LineJoin.MITER, "Miter"), ROUND(LineJoin.ROUND, "Round");

        private LineJoin lineJoin;

        private String name;

        AppLineJoin(LineJoin lineJoin, String name) {
            this.name = name;
            this.lineJoin = lineJoin;
        }

        public LineJoin getLineJoin() {
            return lineJoin;
        }

        ;

        public String getName() {
            return name;
        }

        ;

        @Override
        public String toString() {
            return name;
        }

        ;
    }

    public enum AppEndCap {

        NO_CHANGE(null, "No Change"), BUTT(EndCap.BUTT, "Butt"), ROUND(EndCap.ROUND, "Round"), SQUARE(EndCap.SQUARE, "Square");

        private EndCap endCap;

        private String name;

        AppEndCap(EndCap endCap, String name) {
            this.name = name;
            this.endCap = endCap;
        }

        public EndCap getEndCap() {
            return endCap;
        }

        ;

        public String getName() {
            return name;
        }

        ;

        @Override
        public String toString() {
            return name;
        }

        ;
    }

    public enum AppFillType {

        NO_CHANGE(null, "No Change"), NONE(FillType.NONE, "None"), COLOR(FillType.COLOR, "Color");

        private FillType fillType;

        private String name;

        AppFillType(FillType fillType, String name) {
            this.fillType = fillType;
            this.name = name;
        }

        public FillType getFillType() {
            return fillType;
        }

        ;

        public String getName() {
            return name;
        }

        ;

        @Override
        public String toString() {
            return name;
        }

        ;
    }

    ArrayList<EuclideAppListener> listeners = new ArrayList<EuclideAppListener>();

    String baseTitle = defaultBaseTitle;

    String noNameLabel = defaultNoNameLabel;

    /**
	 * Specific style for point sets
	 */
    DrawStyle pointSetStyle;

    ArrayList<EuclideDoc> docs = new ArrayList<EuclideDoc>();

    EuclideDoc currentDoc = null;

    /**
	 * Macro stored by the application.
	 */
    private ArrayList<SimpleMacro> macros = new ArrayList<SimpleMacro>();

    private static Collection<AngleUnit> angleUnits;

    private static Collection<LengthUnit> lengthUnits;

    boolean allowingNewFreePoints = true;

    public static final Class<? extends DynamicObject2D> getDynamicClass(Class<?> aClass) {
        Class<? extends DynamicObject2D> dynClass = DynamicObject2D.class;
        if (Shape2D.class.isAssignableFrom(aClass)) dynClass = DynamicShape2D.class; else if (Transform2D.class.isAssignableFrom(aClass)) dynClass = DynamicTransform2D.class; else if (Measure2D.class.isAssignableFrom(aClass)) dynClass = DynamicMeasure2D.class; else if (DynamicPredicate2D.class.isAssignableFrom(aClass)) dynClass = DynamicPredicate2D.class; else if (Vector2D.class.isAssignableFrom(aClass)) dynClass = DynamicVector2D.class;
        return dynClass;
    }

    public static final Class<?> getPrimitiveClass(Class<?> baseClass) {
        if (Integer.class.equals(baseClass)) return int.class; else if (Boolean.class.equals(baseClass)) return boolean.class; else if (Double.class.equals(baseClass)) return double.class; else if (String.class.equals(baseClass)) return String.class; else return baseClass;
    }

    /**
	 * @throws java.awt.HeadlessException
	 */
    public EuclideApp() {
        initDrawStyles();
        initUnits();
        EuclideDoc doc = EuclideDoc.createDefaultDoc();
        docs.add(doc);
        setCurrentDoc(doc);
        doc.addDocumentListener(this);
    }

    private void initDrawStyles() {
        DefaultDrawStyle style = new DefaultDrawStyle();
        style = new DefaultDrawStyle();
        style.setMarkerColor(Color.BLUE);
        style.setMarkerFillColor(Color.CYAN);
        style.setMarkerSize(3);
        style.setMarkerLineWidth(1);
        style.setLineColor(Color.BLUE);
        style.setFillType(DrawStyle.FillType.COLOR);
        style.setFillColor(Color.CYAN);
        style.setFillTransparency(.5);
        this.pointSetStyle = style;
    }

    private void initUnits() {
        angleUnits = new ArrayList<AngleUnit>(3);
        angleUnits.add(AngleUnit.DEGREE);
        angleUnits.add(AngleUnit.RADIAN);
        angleUnits.add(AngleUnit.TURN);
        lengthUnits = new ArrayList<LengthUnit>(4);
        lengthUnits.add(LengthUnit.MICROMETER);
        lengthUnits.add(LengthUnit.METER);
        lengthUnits.add(LengthUnit.CENTIMETER);
        lengthUnits.add(LengthUnit.MILLIMETER);
    }

    public EuclideDoc getCurrentDoc() {
        return currentDoc;
    }

    /**
	 * Set up the current document. The document is setup only if it is
	 * contained in the list of documents.
	 * @param aDoc
	 */
    public void setCurrentDoc(EuclideDoc doc) {
        if (!docs.contains(doc) && doc != null) return;
        currentDoc = doc;
        fireAppModifiedEvent(new AppModifiedEvent(this, EuclideAppEvent.CURRENTDOC_CHANGED));
    }

    /**
	 * Adds the given document to the list of opened docs. If this is the only
	 * opened document, also set it as current document.
	 * @param aDoc
	 */
    public void addDoc(EuclideDoc doc) {
        docs.add(doc);
        if (docs.size() == 1) currentDoc = doc;
        fireDocAddedEvent(new AppDocsModifiedEvent(this, EuclideAppEvent.DOCUMENT_ADDED, doc));
    }

    public void removeDoc(EuclideDoc doc) {
        int i = docs.indexOf(doc);
        docs.remove(doc);
        if (i > 0) i--; else {
            if (docs.size() == 0) i = -1;
        }
        fireDocRemovedEvent(new AppDocsModifiedEvent(this, EuclideAppEvent.DOCUMENT_REMOVED, doc));
        if (i != -1) this.setCurrentDoc(docs.get(i)); else this.setCurrentDoc(null);
    }

    /**
	 * Returns all the documents
	 * @return all documents opened by application
	 */
    public Collection<EuclideDoc> getDocuments() {
        return docs;
    }

    /**
	 * Returns the number of open documents.
	 */
    public int getDocumentNumber() {
        return docs.size();
    }

    /**
	 * Adds a new document, and set it as the current document.
	 * @param aDoc the document to add
	 */
    public void addNewDoc() {
        EuclideDoc doc = EuclideDoc.createDefaultDoc();
        doc.setName("NoName" + docs.size());
        docs.add(doc);
        currentDoc = doc;
        fireDocAddedEvent(new AppDocsModifiedEvent(this, EuclideAppEvent.DOCUMENT_ADDED, doc));
    }

    /**
	 * Creates a new sheet, adds it to the document, and return the created
	 * sheet.
	 */
    public EuclideSheet addNewSheet(EuclideDoc doc) {
        int i = 1;
        String name = "Sheet " + i;
        boolean ok = false;
        while (!ok) {
            ok = true;
            for (EuclideSheet sheet : doc.getSheets()) if (sheet.getName().equals(name)) {
                ok = false;
                name = "Sheet " + (++i);
                break;
            }
        }
        EuclideSheet sheet = new EuclideSheet(doc, name);
        sheet.addLayer(new EuclideLayer("layer 0"));
        doc.addSheet(sheet);
        return sheet;
    }

    /**
	 * Closes the current document (remove it from the list of docs).
	 */
    public void closeCurrentDoc() {
        EuclideDoc doc = getCurrentDoc();
        removeDoc(doc);
    }

    public boolean isAllowingNewFreePoints() {
        return allowingNewFreePoints;
    }

    /**
	 * Creates a shape with the given geometry, by setting up the 
	 * appropriate style for point, line and fill, and by creating a new
	 * name.
	 * @param geometry the geometry of the shape
	 * @return an EuclideShape with appropriate geometry and style
	 */
    public EuclideFigure createEuclideShape(DynamicShape2D dynamic) {
        EuclideFigure shape = new EuclideFigure(dynamic);
        setupFigureStyle(shape);
        String shapeName = createShapeName(dynamic.getShape());
        shape.setName(shapeName);
        dynamic.setName(shapeName);
        return shape;
    }

    /**
	 * Modifies the base style of the figure according to the class of its
	 * geometry.
	 */
    private void setupFigureStyle(EuclideFigure shape) {
        DynamicShape2D dynamic = shape.getGeometry();
        Shape2D geometry = dynamic.getShape();
        DrawStyle style;
        if (geometry instanceof PointSet2D) style = new DefaultDrawStyle(pointSetStyle); else style = new DefaultDrawStyle(getCurrentDoc().getDrawStyle());
        shape.setDrawStyle(style);
    }

    /**
	 * Creates a new construction/Dynamic object.
	 * @param elementClass the class of the construction, inherits
	 * 		DynamicObject2D
	 * @param paramClasses the array of classes for each argument of
	 * 		constructor
	 * @param param the array of arguments for the constructor, should be the
	 * 		same size as paramClass
	 * @return a new construction (DynamicObject2D) initialized with given
	 * 		parameters
	 */
    public DynamicObject2D createObject(Class<?> elementClass, Class<?>[] paramClasses, Object[] param) {
        Constructor<?> cons;
        DynamicObject2D newObject;
        Class<?>[] buildClasses = new Class[paramClasses.length];
        for (int i = 0; i < paramClasses.length; i++) {
            buildClasses[i] = paramClasses[i];
            if (paramClasses[i].isArray()) {
                Class<?> arrayClass = paramClasses[i].getComponentType();
                if (Shape2D.class.isAssignableFrom(arrayClass)) buildClasses[i] = Array.newInstance(DynamicShape2D.class, 0).getClass();
                if (Transform2D.class.isAssignableFrom(arrayClass)) buildClasses[i] = Array.newInstance(DynamicTransform2D.class, 0).getClass();
                if (Measure2D.class.isAssignableFrom(arrayClass)) buildClasses[i] = Array.newInstance(DynamicMeasure2D.class, 0).getClass();
                if (Vector2D.class.isAssignableFrom(arrayClass)) buildClasses[i] = Array.newInstance(DynamicVector2D.class, 0).getClass();
                if (DynamicPredicate2D.class.isAssignableFrom(arrayClass)) buildClasses[i] = Array.newInstance(DynamicPredicate2D.class, 0).getClass();
            } else {
                if (DynamicShape2D.class.isAssignableFrom(paramClasses[i])) buildClasses[i] = DynamicShape2D.class;
                if (DynamicTransform2D.class.isAssignableFrom(paramClasses[i])) buildClasses[i] = DynamicTransform2D.class;
                if (DynamicMeasure2D.class.isAssignableFrom(paramClasses[i])) buildClasses[i] = DynamicMeasure2D.class;
                if (DynamicVector2D.class.isAssignableFrom(paramClasses[i])) buildClasses[i] = DynamicVector2D.class;
                if (DynamicPredicate2D.class.isAssignableFrom(paramClasses[i])) buildClasses[i] = DynamicPredicate2D.class;
                if (Shape2D.class.isAssignableFrom(paramClasses[i])) buildClasses[i] = DynamicShape2D.class;
                if (Transform2D.class.isAssignableFrom(paramClasses[i])) buildClasses[i] = DynamicTransform2D.class;
                if (Measure2D.class.isAssignableFrom(paramClasses[i])) buildClasses[i] = DynamicMeasure2D.class;
                if (Vector2D.class.isAssignableFrom(paramClasses[i])) buildClasses[i] = DynamicVector2D.class;
                if (DynamicPredicate2D.class.isAssignableFrom(paramClasses[i])) buildClasses[i] = DynamicPredicate2D.class;
            }
        }
        try {
            cons = elementClass.getConstructor(buildClasses);
        } catch (NoSuchMethodException ex) {
            System.out.println("couldn't create constructor for " + elementClass.getName());
            return null;
        }
        try {
            newObject = (DynamicObject2D) cons.newInstance(param);
        } catch (InvocationTargetException ex) {
            System.out.println(param);
            System.out.println(param[0]);
            System.out.println(cons);
            System.out.println(ex);
            System.out.println(ex.getTargetException());
            return null;
        } catch (Exception ex) {
            System.out.println("Couldn't instanciate new class : " + elementClass.getName());
            System.out.println(ex);
            return null;
        }
        return newObject;
    }

    /**
	 * Returns the name of the shape, from its class and possibly depending
	 * on the Locale. For example, Point2D class will return string "point",
	 * StraightLine2D will return "straight line"....
	 * If the shape is not recognized, return the string "shape".
	 * 
	 */
    public String getShapeName(Shape2D shape) {
        return this.getShapeName(shape.getClass());
    }

    /**
	 * Returns a string associated with an EuclideShape. The string is
	 * composed of a shape identifier ("line", "ellipse", "point set"...) and
	 * eventually the name of the shape between parens. 
	 */
    public String getShapeString(DynamicShape2D dynamic) {
        String baseName = "shape";
        if (dynamic.isDefined()) {
            Shape2D shape = dynamic.getShape();
            if (shape != null) baseName = getShapeName(shape.getClass());
        }
        String name = dynamic.getName();
        if (name != null) if (!name.isEmpty()) {
            baseName = baseName + " (" + name + ")";
        }
        return baseName;
    }

    /**
	 * Returns the name of the shape, from its class and possibly depending
	 * on the Locale. For example, Point2D class will return string "point",
	 * StraightLine2D will return "straight line"....
	 * If the shape is not recognized, return the string "shape".
	 * 
	 */
    public String getShapeName(Class<?> shapeClass) {
        if (PointShape2D.class.isAssignableFrom(shapeClass)) {
            if (Point2D.class.isAssignableFrom(shapeClass)) return "point";
            if (PointSet2D.class.isAssignableFrom(shapeClass)) return "point set";
            return "point shape";
        }
        if (Curve2D.class.isAssignableFrom(shapeClass)) {
            if (StraightLine2D.class.isAssignableFrom(shapeClass)) return "straight line";
            if (LineSegment2D.class.isAssignableFrom(shapeClass)) return "line segment";
            if (Ray2D.class.isAssignableFrom(shapeClass)) return "ray";
            if (Circle2D.class.isAssignableFrom(shapeClass)) return "circle";
            if (Ellipse2D.class.isAssignableFrom(shapeClass)) return "ellipse";
            if (CircleArc2D.class.isAssignableFrom(shapeClass)) return "circle arc";
            if (EllipseArc2D.class.isAssignableFrom(shapeClass)) return "ellipse arc";
            if (Parabola2D.class.isAssignableFrom(shapeClass)) return "parabola";
            if (ParabolaArc2D.class.isAssignableFrom(shapeClass)) return "parabola arc";
            if (Hyperbola2D.class.isAssignableFrom(shapeClass)) return "hyperbola";
            if (Polyline2D.class.isAssignableFrom(shapeClass)) return "polyline";
            return "curve";
        }
        if (Domain2D.class.isAssignableFrom(shapeClass)) {
            if (Polygon2D.class.isAssignableFrom(shapeClass)) return "polygon";
            return "domain";
        }
        return "shape";
    }

    /**
	 * Returns a default name for a shape, according to shape class, 
	 * and to already stored shapes.
	 */
    public String createShapeName(Shape2D geometry) {
        String baseName = "S";
        if (geometry instanceof Point2D) baseName = "P"; else if (geometry instanceof LinearShape2D) baseName = "L"; else if (geometry instanceof CircularShape2D) baseName = "C"; else if (geometry instanceof Ellipse2D) baseName = "E"; else if (geometry instanceof EllipseArc2D) baseName = "E"; else if (geometry instanceof Polygon2D) baseName = "Poly"; else if (geometry instanceof Hyperbola2D) baseName = "H";
        int num = 1;
        boolean ok = false;
        String name = null;
        while (!ok) {
            ok = true;
            name = String.format("%1$s%2$d", baseName, num);
            for (EuclideFigure shape : currentDoc.getFigures()) {
                if (name.equals(shape.getName())) {
                    ok = false;
                    num++;
                    break;
                }
            }
        }
        return name;
    }

    /**
	 * Returns a default name for a transform, according to transform type,
	 * and to already stored transforms.
	 */
    public String createTransformName(Transform2D transform) {
        String baseName = "Trans";
        if (transform instanceof AffineTransform2D) {
            AffineTransform2D affine = (AffineTransform2D) transform;
            if (AffineTransform2D.isMotion(affine)) baseName = "Mov";
        }
        if (transform instanceof CircleInversion2D) baseName = "Inv";
        int num = 1;
        boolean ok = false;
        String name = String.format(baseNameFormat, baseName, num);
        while (!ok) {
            ok = true;
            name = String.format(baseNameFormat, baseName, num);
            for (DynamicTransform2D trans : currentDoc.getTransforms()) {
                if (trans.getName().startsWith(name)) {
                    num++;
                    ok = false;
                    break;
                }
            }
        }
        return name;
    }

    /**
	 * Returns a default name for a measure, according to measure type, 
	 * and to already stored measures.
	 */
    public String createMeasureName(Measure2D measure) {
        String baseName = "Meas";
        if (measure instanceof LengthMeasure2D) baseName = "Dist";
        if (measure instanceof AngleMeasure2D) baseName = "Angle";
        int num = 1;
        boolean ok = false;
        String name = null;
        while (!ok) {
            ok = true;
            name = String.format(baseNameFormat, baseName, num);
            for (DynamicMeasure2D meas : currentDoc.getMeasures()) {
                if (meas.getName().startsWith(name)) {
                    num++;
                    ok = false;
                    break;
                }
            }
        }
        return name;
    }

    /**
	 * Returns a default name for a predicate, according to predicate type, 
	 * and to already stored measures.
	 */
    public String createVectorName(DynamicVector2D vector) {
        String baseName = "Vect";
        int num = 1;
        boolean ok = false;
        String name = null;
        while (!ok) {
            ok = true;
            name = String.format(baseNameFormat, baseName, num);
            for (DynamicVector2D vect : currentDoc.getVectors()) {
                if (vect.getName().startsWith(name)) {
                    num++;
                    ok = false;
                    break;
                }
            }
        }
        return name;
    }

    /**
	 * Returns a default name for a predicate, according to predicate type, 
	 * and to already stored predicates.
	 */
    public String createPredicateName(DynamicPredicate2D predicate) {
        String baseName = "Pred";
        int num = 1;
        boolean ok = false;
        String name = null;
        while (!ok) {
            ok = true;
            name = String.format(baseNameFormat, baseName, num);
            for (DynamicPredicate2D pred : currentDoc.getPredicates()) {
                if (pred.getName().startsWith(name)) {
                    num++;
                    ok = false;
                    break;
                }
            }
        }
        return name;
    }

    public Collection<AngleUnit> getAngleUnits() {
        return Collections.unmodifiableCollection(angleUnits);
    }

    public Collection<LengthUnit> getLengthUnits() {
        return Collections.unmodifiableCollection(lengthUnits);
    }

    public void documentModified(EuclideDocEvent evt) {
    }

    public boolean addMacro(SimpleMacro macro) {
        if (macros.contains(macro)) return false;
        macros.add(macro);
        return true;
    }

    public boolean removeMacro(SimpleMacro macro) {
        if (!macros.contains(macro)) return false;
        macros.remove(macro);
        return true;
    }

    public Collection<SimpleMacro> getMacros() {
        return macros;
    }

    public String createMacroName() {
        String baseName = "Macro";
        int num = 1;
        boolean ok = false;
        String name = String.format(baseNameFormat, baseName, num);
        while (!ok) {
            ok = true;
            name = String.format(baseNameFormat, baseName, num);
            for (SimpleMacro macro : macros) {
                if (macro.getName().equals(name)) {
                    num++;
                    ok = false;
                    break;
                }
            }
        }
        return name;
    }

    public void addApplicationListener(EuclideAppListener listener) {
        listeners.add(listener);
    }

    public void removeApplicationListener(EuclideAppListener listener) {
        listeners.remove(listener);
    }

    public void removeAllListener() {
        listeners.clear();
    }

    private void fireAppModifiedEvent(EuclideAppEvent evt) {
        for (EuclideAppListener listener : listeners) listener.appliModified(evt);
    }

    private void fireDocAddedEvent(EuclideAppEvent evt) {
        for (EuclideAppListener listener : listeners) listener.appliDocAdded(evt);
    }

    private void fireDocRemovedEvent(EuclideAppEvent evt) {
        for (EuclideAppListener listener : listeners) listener.appliDocRemoved(evt);
    }

    public class AppModifiedEvent extends EuclideAppEvent {

        int state = 0;

        EuclideApp app;

        public AppModifiedEvent(EuclideApp app) {
            this.app = app;
        }

        public AppModifiedEvent(EuclideApp app, int state) {
            this.app = app;
            this.state = state;
        }

        @Override
        public int getState() {
            return state;
        }

        @Override
        public EuclideApp getAppli() {
            return app;
        }

        public EuclideDoc getDoc() {
            return null;
        }
    }

    public class AppDocsModifiedEvent extends AppModifiedEvent {

        EuclideDoc doc = null;

        public AppDocsModifiedEvent(EuclideApp app, int state, EuclideDoc doc) {
            super(app, state);
            this.doc = doc;
        }

        public EuclideDoc getDoc() {
            return doc;
        }
    }
}
