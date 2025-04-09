package br.uerj.eng.geomatica.interoperability.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.SchemaException;
import br.uerj.eng.geomatica.interoperability.model.DataSource;
import br.uerj.eng.geomatica.interoperability.model.GeographicMap;
import br.uerj.eng.geomatica.interoperability.model.SchemaCorrelation;
import br.uerj.eng.geomatica.interoperability.model.SchemaElement;
import br.uerj.eng.geomatica.interoperability.model.WFSDataSource;
import com.vividsolutions.jts.geom.Geometry;
import edu.stanford.smi.protegex.owl.model.OWLModel;

/**
 * Service that exports data from integrated maps to a new file (shp,owl,etc.).
 * 
 * @author Victor Azevedo (reference Azevedo, V. H. M.)
 */
public class ExportService {

    public static void exportToOWL(GeographicMap map) {
        if (!map.getDataSources().isEmpty()) {
            OWLModel model = OntologyService.getOWLModel(map.getDomain());
            String schemaName = map.getDomain().getName();
            FeatureType ft = createNewSchema(map.getDataSources(), schemaName, map.getFileName());
            FeatureCollection features = FeatureCollections.newCollection();
            features = getFeatures(map.getDataSources(), ft);
            FeatureIterator fit = features.features();
            while (fit.hasNext()) {
                Feature feature = fit.next();
                OntologyService.addFeatureIndividual(model, feature);
            }
            OntologyService.saveOWLModel(model, map.getFileName());
        }
    }

    public static void exportToShapefile(GeographicMap map) {
        if (!map.getDataSources().isEmpty()) {
            String schemaName = map.getDomain().getName();
            FeatureType ft = createNewSchema(map.getDataSources(), schemaName, map.getFileName());
            FeatureCollection features = FeatureCollections.newCollection();
            features = getFeatures(map.getDataSources(), ft);
            try {
                File file = new File(map.getFileName());
                URL url = file.toURI().toURL();
                ShapefileDataStore datastore = new ShapefileDataStore(url);
                datastore.createSchema(ft);
                FeatureStore src = (FeatureStore) datastore.getFeatureSource(ft.getTypeName());
                Transaction t = src.getTransaction();
                FeatureWriter writer = datastore.getFeatureWriter(ft.getTypeName(), t);
                FeatureIterator fit = features.features();
                while (fit.hasNext()) {
                    Feature shpFeature = writer.next();
                    Feature feature = fit.next();
                    for (int i = 0; i < feature.getNumberOfAttributes(); i++) {
                        shpFeature.setAttribute(i, feature.getAttribute(i));
                        if (feature.getAttribute(i) instanceof Geometry) {
                            shpFeature.setDefaultGeometry((Geometry) feature.getAttribute(i));
                        }
                    }
                    writer.write();
                }
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static FeatureType createNewSchema(Collection dataSources, String schemaName, String path) {
        schemaName = schemaName.trim().toLowerCase();
        FeatureType ft = null;
        Iterator it = dataSources.iterator();
        ArrayList<SchemaElement> attributes = new ArrayList<SchemaElement>();
        try {
            while (it.hasNext()) {
                WFSDataSource ds = (WFSDataSource) it.next();
                Iterator it2 = ds.getCorrelationTable().iterator();
                while (it2.hasNext()) {
                    SchemaCorrelation correlation = (SchemaCorrelation) it2.next();
                    if (!attributes.contains(correlation.getDomainProperty())) {
                        attributes.add(correlation.getDomainProperty());
                    }
                }
            }
            FeatureTypeBuilder build = FeatureTypeBuilder.newInstance(schemaName);
            build.setName(schemaName);
            build.setNamespace(new URI(path));
            AttributeType at = null;
            for (int i = 0; i < attributes.size(); i++) {
                SchemaElement attribute = (SchemaElement) attributes.get(i);
                at = AttributeTypeFactory.newAttributeType(attribute.getName(), attribute.getType());
                build.addType(at);
            }
            at = AttributeTypeFactory.newAttributeType("origem", String.class);
            build.addType(at);
            ft = build.getFeatureType();
        } catch (SchemaException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ft;
    }

    private static FeatureCollection getFeatures(Collection sources, FeatureType ft) {
        FeatureCollection features = FeatureCollections.newCollection();
        Iterator it = sources.iterator();
        while (it.hasNext()) {
            WFSDataSource wfsDS = (WFSDataSource) it.next();
            FeatureReader fr = null;
            try {
                URL url = new URL(wfsDS.getURLCapabilities());
                Map<Object, Object> m = new HashMap<Object, Object>();
                m.put(WFSDataStoreFactory.URL.key, url);
                m.put(WFSDataStoreFactory.TIMEOUT.key, new Integer(10000));
                m.put(WFSDataStoreFactory.PROTOCOL.key, Boolean.TRUE);
                DataStore wfs = (new WFSDataStoreFactory()).createNewDataStore(m);
                Query query = new DefaultQuery(wfsDS.getTypeName());
                fr = wfs.getFeatureReader(query, Transaction.AUTO_COMMIT);
                while (fr.hasNext()) {
                    Feature featureSource = (Feature) fr.next();
                    AttributeType[] schema = ft.getAttributeTypes();
                    Object[] attributes = new Object[schema.length];
                    Geometry defaultGeometry = null;
                    for (int i = 0; i < schema.length - 1; i++) {
                        String sourceAttributeName = getCorrelatedAttribute(wfsDS, schema[i].getName());
                        attributes[i] = featureSource.getAttribute(sourceAttributeName);
                        if (attributes[i] instanceof String) {
                            attributes[i] = getCorrelatedValue(wfsDS, sourceAttributeName, (String) attributes[i]);
                        }
                        if (attributes[i] instanceof Geometry) {
                            defaultGeometry = (Geometry) attributes[i];
                        }
                    }
                    attributes[schema.length - 1] = wfsDS.getName();
                    Feature feature = ft.create(attributes);
                    feature.setDefaultGeometry(defaultGeometry);
                    features.add(feature);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    fr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return features;
    }

    private static String getCorrelatedAttribute(DataSource source, String attribute) {
        SchemaElement correlatedAttribute = null;
        Iterator<SchemaCorrelation> it = source.getCorrelationTable().iterator();
        while (it.hasNext()) {
            SchemaCorrelation correlation = it.next();
            if (correlation.getDomainProperty().getName().equals(attribute)) {
                correlatedAttribute = correlation.getSchemaElement();
                return correlatedAttribute.getName();
            }
            if (correlation.getSchemaElement().getName().equals(attribute)) {
                correlatedAttribute = correlation.getDomainProperty();
                return correlatedAttribute.getName();
            }
        }
        return null;
    }

    private static String getCorrelatedValue(DataSource source, String attributeName, String attributeValue) {
        SchemaElement correlatedAttribute = null;
        Iterator<SchemaCorrelation> it = source.getCorrelationTable().iterator();
        while (it.hasNext()) {
            SchemaCorrelation correlation = it.next();
            if (correlation.getDomainProperty().getName().equals(attributeName) || correlation.getSchemaElement().getName().equals(attributeName)) {
                if (correlation.getQualifiedCorrelations().containsKey(attributeValue)) return (String) correlation.getQualifiedCorrelations().get(attributeValue);
            }
        }
        return attributeValue;
    }

    private void zipGeneratedFiles(String path, String nomeArquivo) {
        try {
            int BUFFER = 2048;
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(path + File.separatorChar + "mapas" + File.separatorChar + nomeArquivo + ".zip");
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            out.setLevel(ZipOutputStream.DEFLATED);
            byte data[] = new byte[BUFFER];
            String rootPath = File.listRoots()[0].getAbsolutePath();
            String files[] = new String[3];
            files[0] = rootPath + "tmp" + File.separatorChar + nomeArquivo + ".shp";
            files[1] = rootPath + "tmp" + File.separatorChar + nomeArquivo + ".shx";
            files[2] = rootPath + "tmp" + File.separatorChar + nomeArquivo + ".dbf";
            for (int i = 0; i < files.length; i++) {
                System.out.println("Adding: " + files[i]);
                FileInputStream fi = new FileInputStream(files[i]);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(files[i]);
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
                File file = new File(files[i]);
                file.delete();
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
