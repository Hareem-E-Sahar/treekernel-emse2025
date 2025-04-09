package edu.ucsd.ncmir.WIBAnnotation;

import edu.ucsd.ccdb.slash.service.Annotation;
import edu.ucsd.ccdb.slash.service.Exception_Exception;
import edu.ucsd.ccdb.slash.service.Geometry;
import edu.ucsd.ccdb.slash.service.SLASHAnnotationService;
import edu.ucsd.ccdb.slash.service.SlashDATASET;
import edu.ucsd.ncmir.SLASHAnnotation.SLASHBadParentException;
import edu.ucsd.ncmir.SLASHAnnotation.SLASHException;
import edu.ucsd.ncmir.SLASHAnnotation.SLASHGeometry;
import edu.ucsd.ncmir.SLASHAnnotation.SLASHObject;
import edu.ucsd.ncmir.SLASHAnnotation.SLASHGeometryType;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import net.java.dev.jaxb.array.DoubleArray;

/**
 *
 * @author spl
 */
final class WIBObject implements SLASHObject {

    private Annotation _annotation;

    private SLASHAnnotationService _port;

    private SlashDATASET _dataset;

    private String _application;

    WIBObject(SLASHAnnotationService port, SlashDATASET dataset, String name, String ontology, SLASHObject parent, SLASHGeometryType geometry_type) throws SLASHException {
        this._port = port;
        this._dataset = dataset;
        String onto_url = this.getOntologyURI(name, ontology);
        try {
            this._annotation = port.createAnnotation(dataset, name, onto_url, geometry_type.toString());
        } catch (Exception_Exception exception) {
            throw new WIBException(exception);
        }
        this.setOrientation(1.0, 0.0, 0.0, 0.0);
        this.reparent(parent);
    }

    WIBObject(SLASHAnnotationService port, SlashDATASET dataset, Annotation annotation) throws SLASHException {
        this._port = port;
        this._dataset = dataset;
        this._annotation = annotation;
    }

    private String getOntologyURI(String name, String ontology) {
        String onto_url;
        try {
            ClassLoader cl = this.getClass().getClassLoader();
            String class_name = this.getClass().getPackage().getName() + "." + ontology + "Handler";
            Class<?> ontology_handler_class = (Class<?>) cl.loadClass(class_name);
            Constructor<?> constructor = ontology_handler_class.getConstructor(new Class[0]);
            OntologyHandler handler = (OntologyHandler) constructor.newInstance();
            onto_url = handler.getURL(name);
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            onto_url = "unknown";
        }
        return onto_url;
    }

    @Override
    public long getUniqueID() {
        return this._annotation.getAnnotationID();
    }

    @Override
    public double[][] getBoundingBox() {
        double[][] bbox = new double[][] { { Double.MAX_VALUE, Double.MAX_VALUE }, { -Double.MAX_VALUE, -Double.MAX_VALUE } };
        for (DoubleArray da : this._annotation.getBoundingBox()) {
            List<Double> coord = da.getItem();
            double x = coord.get(0);
            double y = coord.get(1);
            if (bbox[0][0] > x) bbox[0][0] = x;
            if (bbox[0][1] > y) bbox[0][1] = y;
            if (bbox[1][0] < x) bbox[1][0] = x;
            if (bbox[1][0] < y) bbox[1][0] = y;
        }
        return bbox;
    }

    @Override
    public SLASHGeometryType getSLASHGeometryType() {
        String annotation_type_string = this._annotation.getGeometryType();
        SLASHGeometryType slash_geometry_type = null;
        for (SLASHGeometryType st : SLASHGeometryType.values()) if (st.toString().equals(annotation_type_string)) {
            slash_geometry_type = st;
            break;
        }
        return slash_geometry_type;
    }

    @Override
    public String getObjectName() {
        return this._annotation.getObjectName();
    }

    @Override
    public String getObjectOntology() {
        return this._annotation.getOntologyName();
    }

    @Override
    public double[] getOrientation() {
        return new double[] { this._annotation.getOrientationW(), this._annotation.getOrientationX(), this._annotation.getOrientationY(), this._annotation.getOrientationZ() };
    }

    @Override
    public void setOrientation(double w, double x, double y, double z) throws SLASHException {
        this._annotation.setOrientationW(w);
        this._annotation.setOrientationX(x);
        this._annotation.setOrientationY(y);
        this._annotation.setOrientationZ(z);
        this.updateAnnotation();
    }

    @Override
    public void setObjectName(String name, String ontology) throws SLASHException {
        this._annotation.setObjectName(name);
        this._annotation.setOntologyName(ontology);
        this._annotation.setOntologyURI(this.getOntologyURI(name, ontology));
        this.updateAnnotation();
    }

    @Override
    public void setOntologicalProperty(String property_name, String ontology, String value) throws SLASHException {
        this.updateAnnotation();
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getOntologicalProperty(String property_name, String ontology) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SLASHObject getParent() throws SLASHException {
        try {
            Annotation p = this._port.getParent(this._annotation);
            return p == null ? null : new WIBObject(this._port, this._dataset, p);
        } catch (Exception_Exception exception) {
            throw new WIBException(exception);
        }
    }

    @Override
    public void reparent(SLASHObject parent) throws SLASHException {
        Annotation parent_annotation = this.getAnnotationFromSLASHObject(parent);
        try {
            if (this.isChild(parent_annotation, this._annotation)) throw new SLASHBadParentException("Reparent would " + "create cycle");
            this._port.reparent(parent_annotation, this._annotation);
        } catch (Exception_Exception exception) {
            throw new WIBException(exception);
        }
    }

    private Annotation getAnnotationFromSLASHObject(SLASHObject object) throws SLASHException {
        Annotation annotation = null;
        if (object != null) {
            long dataset_id = this._dataset.getDatasetId();
            long object_id = object.getUniqueID();
            try {
                for (Annotation a : this._port.getAnnotationsByDataset(dataset_id)) if (object_id == a.getAnnotationID()) {
                    annotation = a;
                    break;
                }
            } catch (Exception_Exception exception) {
                throw new WIBException(exception);
            }
        }
        return annotation;
    }

    private boolean isChild(Annotation parent, Annotation annotation) throws Exception_Exception {
        boolean is_child = annotation.equals(parent);
        if (!is_child) for (Annotation child : this._port.getChildren(annotation)) if (is_child = this.isChild(parent, child)) break;
        return is_child;
    }

    @Override
    public ArrayList<SLASHObject> getChildren() throws SLASHException {
        ArrayList<SLASHObject> list;
        try {
            list = new ArrayList<SLASHObject>();
            for (Annotation child : this._port.getChildren(this._annotation)) list.add(new WIBObject(this._port, this._dataset, child));
        } catch (Exception_Exception exception) {
            throw new WIBException(exception);
        }
        return list;
    }

    @Override
    public void setOrientation(double[] wxyz) throws SLASHException {
        this._annotation.setOrientationW(wxyz[0]);
        this._annotation.setOrientationX(wxyz[1]);
        this._annotation.setOrientationY(wxyz[2]);
        this._annotation.setOrientationZ(wxyz[3]);
        this.updateAnnotation();
    }

    @Override
    public SLASHGeometry createSLASHGeometry(String user_name, double offset, double[][] trace) throws SLASHException {
        ArrayList<DoubleArray> trace_array = new ArrayList<DoubleArray>();
        for (double[] point : trace) {
            DoubleArray da = new DoubleArray();
            List<Double> dai = da.getItem();
            dai.add(new Double(point[0]));
            dai.add(new Double(point[1]));
            trace_array.add(da);
        }
        SLASHGeometry slash_geometry;
        try {
            Geometry geometry = this._port.createPolyline(user_name, offset, trace_array);
            this._port.setObject(this._annotation, geometry);
            slash_geometry = new WIBGeometry(this._port, geometry);
        } catch (Exception_Exception exception) {
            throw new WIBException(exception);
        }
        return slash_geometry;
    }

    @Override
    public void deleteSLASHGeometry(SLASHGeometry polyline) throws SLASHException {
        polyline.delete();
    }

    @Override
    public ArrayList<SLASHGeometry> getSLASHGeometriesByOffset(double offset) throws SLASHException {
        ArrayList<SLASHGeometry> geometries = new ArrayList<SLASHGeometry>();
        for (SLASHGeometry g : this.getSLASHGeometries()) if (g.getOffset() == offset) geometries.add(g);
        return geometries;
    }

    @Override
    public ArrayList<SLASHGeometry> getSLASHGeometries() throws SLASHException {
        ArrayList<SLASHGeometry> geometries = new ArrayList<SLASHGeometry>();
        for (Geometry g : this._annotation.getGeom()) geometries.add(new WIBGeometry(this._port, g));
        return geometries;
    }

    @Override
    public SLASHGeometry getSLASHGeometryByUniqueID(long unique_id) throws SLASHException {
        SLASHGeometry found = null;
        for (SLASHGeometry g : this.getSLASHGeometries()) if (g.getUniqueID() == unique_id) {
            found = g;
            break;
        }
        return found;
    }

    @Override
    public void setApplicationData(String app_name, String app_data) throws SLASHException {
        try {
            this._port.setAnnotationAppData(this._annotation, app_name, app_data);
            this.updateAnnotation();
        } catch (Exception_Exception exception) {
            throw new WIBException(exception);
        }
    }

    @Override
    public String getApplicationData(String app_name) throws SLASHException {
        try {
            String s = this._port.getAnnotationAppValue(this._annotation, app_name);
            return s == null ? "" : s;
        } catch (Exception_Exception exception) {
            throw new WIBException(exception);
        }
    }

    @Override
    public void delete() throws SLASHException {
        try {
            for (SLASHObject so : this.getChildren()) so.delete();
            this._port.reparent(null, this._annotation);
            for (Geometry g : this._annotation.getGeom()) this._port.deletePolyline(g);
            this._port.delete(this._annotation);
        } catch (Exception_Exception exception) {
            new WIBException(exception);
        }
    }

    private void updateAnnotation() throws SLASHException {
        try {
            this._port.updateAnnotation(this._annotation);
        } catch (Exception_Exception exception) {
            new WIBException(exception);
        }
    }

    @Override
    public String toString() {
        String s = "";
        try {
            s += "Object UniqueID: " + this.getUniqueID() + "\n";
            s += "ApplicationData: " + this.getApplicationData(this._application).trim() + "\n";
            SLASHObject parent = this.getParent();
            s += "Parent: " + (parent != null ? parent.getUniqueID() : "none") + "\n";
            double[][] bounding_box = this.getBoundingBox();
            s += "BoundingBox: { " + "{ " + bounding_box[0][0] + "," + bounding_box[0][1] + " }, " + "{ " + bounding_box[1][0] + "," + bounding_box[1][1] + " } " + "}" + "\n";
            s += "ObjectName: " + this.getObjectName() + "\n";
            s += "ObjectOntology: " + this.getObjectOntology() + "\n";
            double[] orientation = this.getOrientation();
            s += "Orientation: " + orientation[0] + " " + orientation[1] + " " + orientation[2] + " " + orientation[3] + "\n";
            s += "SLASHGeometryType: " + this.getSLASHGeometryType() + "\n";
            for (SLASHGeometry sg : this.getSLASHGeometries()) s += sg.toString();
            for (SLASHObject so : this.getChildren()) s += " " + so.toString().replaceAll("\n", "\n ").trim() + "\n";
        } catch (SLASHException se) {
            se.printStackTrace(System.err);
        }
        return s.replaceAll("\n", "\n ").trim();
    }
}
