package org.geotools.caching.util;

import java.net.URI;
import java.util.Random;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.geotools.feature.AttributeType;
import org.geotools.feature.DefaultAttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.feature.type.GeometricAttributeType;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.referencing.crs.DefaultEngineeringCRS;

public class Generator {

    public static final FeatureType type;

    private static final GeometryFactory gfact;

    private static final Random rand;

    private static final FilterFactory filterFactory;

    static {
        gfact = new GeometryFactory();
        rand = new Random();
        filterFactory = new FilterFactoryImpl();
        FeatureTypeBuilder builder = FeatureTypeBuilder.newInstance("test");
        GeometricAttributeType geom = new GeometricAttributeType("geom", Geometry.class, true, null, DefaultEngineeringCRS.GENERIC_2D, Filter.INCLUDE);
        AttributeType dummydata = DefaultAttributeTypeFactory.newAttributeType("dummydata", String.class);
        builder.addType(geom);
        builder.addType(dummydata);
        builder.setDefaultGeometry(geom);
        builder.setNamespace(URI.create("testStore"));
        try {
            type = builder.getFeatureType();
        } catch (SchemaException e) {
            throw (RuntimeException) new RuntimeException().initCause(e);
        }
    }

    private final double xrange;

    private final double yrange;

    double meansize;

    int max_vertices;

    public Generator(double xrange, double yrange) {
        this.xrange = xrange;
        this.yrange = yrange;
        this.max_vertices = 10;
        this.meansize = Math.min(xrange, yrange) / 20;
    }

    static LineString createRectangle(double x1, double y1, double x2, double y2) {
        double x_min = (x1 < x2) ? x1 : x2;
        double y_min = (y1 < y2) ? y1 : y2;
        double x_max = (x1 < x2) ? x2 : x1;
        double y_max = (y1 < y2) ? y2 : y1;
        Coordinate[] coords = new Coordinate[5];
        coords[0] = new Coordinate(x_min, y_min);
        coords[1] = new Coordinate(x_max, y_min);
        coords[2] = new Coordinate(x_max, y_max);
        coords[3] = new Coordinate(x_min, y_max);
        coords[4] = coords[0];
        CoordinateSequence cs = new CoordinateArraySequence(coords);
        return new LineString(cs, gfact);
    }

    public void sort(double[] array) {
        double tmp;
        for (int i = 0; i < array.length; i++) {
            tmp = array[i];
            for (int j = i + 1; j < array.length; j++) {
                if (array[j] < tmp) {
                    array[i] = array[j];
                    array[j] = tmp;
                    tmp = array[i];
                }
            }
        }
    }

    Geometry createGeometry() {
        double center_x = meansize + (rand.nextDouble() * (xrange - (2 * meansize)));
        double center_y = meansize + (rand.nextDouble() * (yrange - (2 * meansize)));
        double size = rand.nextDouble() * 2 * meansize;
        int n_vertices = 3 + rand.nextInt(max_vertices - 3);
        double[] angles = new double[n_vertices];
        double[] distances = new double[n_vertices];
        for (int k = 0; k < n_vertices; k++) {
            angles[k] = rand.nextDouble() * 2 * Math.PI;
            distances[k] = rand.nextDouble() * size;
        }
        sort(angles);
        Coordinate[] coords = new Coordinate[n_vertices + 1];
        for (int k = 0; k < n_vertices; k++) {
            double x = center_x + (distances[k] * Math.cos(angles[k]));
            double y = center_y + (distances[k] * Math.sin(angles[k]));
            coords[k] = new Coordinate(x, y);
        }
        coords[n_vertices] = coords[0];
        CoordinateSequence cs = new CoordinateArraySequence(coords);
        return new LineString(cs, gfact);
    }

    public Feature createFeature(int i) {
        Geometry g = createGeometry();
        String dummydata = "Id: " + i;
        Feature f = null;
        try {
            f = type.create(new Object[] { g, dummydata });
            return f;
        } catch (IllegalAttributeException e) {
            throw (RuntimeException) new RuntimeException().initCause(e);
        }
    }

    public static Coordinate pickRandomPoint(Coordinate center, double xrange, double yrange) {
        double x = (center.x - (xrange / 2)) + (xrange * rand.nextDouble());
        double y = (center.y - (yrange / 2)) + (yrange * rand.nextDouble());
        return new Coordinate(x, y);
    }

    public static Query createBboxQuery(Coordinate center, double xrange, double yrange) {
        double x_min = center.x - (xrange / 2);
        double x_max = center.x + (xrange / 2);
        double y_min = center.y - (yrange / 2);
        double y_max = center.y + (yrange / 2);
        Filter bb = filterFactory.bbox(type.getPrimaryGeometry().getLocalName(), x_min, y_min, x_max, y_max, type.getPrimaryGeometry().getCoordinateSystem().toString());
        return new DefaultQuery(type.getTypeName(), bb);
    }

    public static Filter createBboxFilter(Coordinate center, double xrange, double yrange) {
        double x_min = center.x - (xrange / 2);
        double x_max = center.x + (xrange / 2);
        double y_min = center.y - (yrange / 2);
        double y_max = center.y + (yrange / 2);
        Filter bb = filterFactory.bbox(type.getPrimaryGeometry().getLocalName(), x_min, y_min, x_max, y_max, type.getPrimaryGeometry().getCoordinateSystem().toString());
        return bb;
    }

    public FeatureType getFeatureType() {
        return type;
    }
}
