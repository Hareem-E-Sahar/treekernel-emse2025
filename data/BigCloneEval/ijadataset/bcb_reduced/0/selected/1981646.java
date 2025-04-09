package org.fpdev.core;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.fpdev.core.basenet.BLink;
import org.fpdev.core.basenet.BNode;
import org.fpdev.core.basenet.BaseNetFeatureIO;
import org.fpdev.core.data.LegacyDB;
import org.fpdev.core.data.SpatialDataConnection;
import org.fpdev.core.data.LegacyDerbyDB;
import org.fpdev.core.data.LegacyMysqlDB;
import org.fpdev.core.data.PostgisConnection;
import org.fpdev.apps.rtemaster.GUIOutputAcceptor;
import org.fpdev.core.data.CoreDB;
import org.fpdev.core.data.DummyCoreDB;
import org.fpdev.core.data.MysqlCoreDB;
import org.fpdev.core.data.ShapefileDataConnection;
import org.fpdev.util.FPUtil;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.postgis.PostgisDataStoreFactory;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A DataPackage contains all of the spatial information for a geographically
 * defined area. This includes the main base geospatial network (accessed via
 * the SpatialDataConnection class) and the Scenario hierarchy.
 *
 * <p>Currently, the software only works with one DataPackage at a time. This
 * will need to change, however, if multi-package features are to be supported
 * in the future (for instance, inter-regional trip planning).
 *
 * <p>TODO: Transit-specific items, such as the collections of routes, stations,
 * etc., are still stored in the FPEngine class. These should migrate to the
 * DataPackage class eventually.
 *
 *
 * @author demory
 */
public class DataPackage {

    private FPEngine engine_;

    private String name_;

    private Properties props_;

    private Scenarios scenarios_;

    private CoreDB coreDB_;

    private LegacyDB legDB_;

    private BaseNetFeatureIO featureIO_;

    private SpatialDataConnection spatialData_;

    /** Creates a new instance of DataPackage */
    public DataPackage(String name, FPEngine engine) {
        engine_ = engine;
        featureIO_ = new BaseNetFeatureIO(engine);
        name_ = name;
        props_ = new Properties();
        scenarios_ = new Scenarios();
        readXMLFile();
        if (props_.getProperty("dataSource").equals("postgis")) spatialData_ = new PostgisConnection(engine, this); else if (props_.getProperty("dataSource").equals("shp")) spatialData_ = new ShapefileDataConnection(engine, this); else spatialData_ = new DummyDataConnection();
        if (props_.getProperty("dbUsername").length() == 0) coreDB_ = new DummyCoreDB(); else coreDB_ = new MysqlCoreDB(engine_, this, props_.getProperty("dbUsername"), props_.getProperty("dbPassword"));
        legDB_ = null;
        switch(LegacyDB.getRDBMSCode(props_.getProperty("dataSource"))) {
            case LegacyDB.DB_DERBY:
                legDB_ = new LegacyDerbyDB(engine_, this);
                break;
            case LegacyDB.DB_MYSQL:
                System.out.println("sending user: " + props_.getProperty("dbUsername"));
                legDB_ = new LegacyMysqlDB(engine_, this, props_.getProperty("dbUsername"), props_.getProperty("dbPassword"));
                break;
        }
    }

    public String getName() {
        return name_;
    }

    public SpatialDataConnection getSpatialDataConn() {
        return spatialData_;
    }

    public BaseNetFeatureIO getFeatureIO() {
        return featureIO_;
    }

    public Scenarios getScenarios() {
        return scenarios_;
    }

    public CoreDB getCoreDB() {
        return coreDB_;
    }

    public LegacyDB getLegacyDB() {
        return legDB_;
    }

    public String getPath() {
        return engine_.getProperty("5pHome") + "data" + File.separator + name_ + File.separator;
    }

    public void readXMLFile() {
        readXMLFile(true);
    }

    public void readXMLFile(boolean readScenarios) {
        try {
            String filename = getPath() + "datapackage.xml";
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = docBuilder.parse(filename);
            NodeList docNodes = doc.getChildNodes();
            Node thisNode = docNodes.item(0);
            if (docNodes.getLength() != 1 && !thisNode.getNodeName().equals("datapackage")) {
                System.out.println("Not a valid datapackage properties file");
                return;
            }
            NodeList nodes = thisNode.getChildNodes();
            for (int n = 0; n < nodes.getLength(); n++) {
                Node node = nodes.item(n);
                if (node.getNodeName().compareTo("properties") == 0) {
                    System.out.println("found properties block");
                    NodeList propNodes = node.getChildNodes();
                    for (int i = 0; i < propNodes.getLength(); i++) {
                        Node propNode = propNodes.item(i);
                        if (propNode.getNodeName().compareTo("property") == 0) {
                            String name = propNode.getAttributes().getNamedItem("name").getNodeValue();
                            System.out.println("prop name=" + name + " val=" + propNode.getTextContent());
                            props_.setProperty(name, propNode.getTextContent());
                        }
                    }
                }
                if (!readScenarios) return;
                if (node.getNodeName().compareTo("basescenario") == 0) {
                    readChildScenarios(node, scenarios_.getBase());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (props_.getProperty("dataSource").equals("postgis")) spatialData_.close();
    }

    private void readChildScenarios(Node xmlNode, Scenario parent) {
        NodeList childNodes = xmlNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeName().compareTo("scenario") == 0) {
                short id = new Short(childNode.getAttributes().getNamedItem("id").getNodeValue()).shortValue();
                String name = childNode.getAttributes().getNamedItem("name").getNodeValue();
                Scenario childScen = new Scenario(id, name, parent);
                scenarios_.add(childScen);
                if (childNode.hasChildNodes()) this.readChildScenarios(childNode, childScen);
            }
        }
    }

    public String getProperty(String name) {
        return props_.getProperty(name);
    }

    public void writeArchive(File archive) {
        String dpPath = getPath();
        String[] filenames = new String[] { "datapackage.xml", "routes.xml", "stations.xml", "streetalias.xml" };
        byte[] buf = new byte[1024];
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(archive));
            zos.setLevel(Deflater.DEFAULT_COMPRESSION);
            for (int i = 0; i < filenames.length; i++) {
                FileInputStream in = new FileInputStream(dpPath + filenames[i]);
                zos.putNextEntry(new ZipEntry(filenames[i]));
                int len;
                while ((len = in.read(buf)) > 0) {
                    zos.write(buf, 0, len);
                }
                zos.closeEntry();
                in.close();
                System.out.println("wrote " + filenames[i]);
            }
            FPUtil.zipDir(dpPath + "route", zos, dpPath.length());
            legDB_.dumpToZip(zos);
            zos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void deleteFromDisk() {
        if (name_.length() > 0) {
            FPUtil.deleteDirectory(engine_.getProperty("5pHome") + "data" + File.separator + name_);
            legDB_.delete();
            legDB_ = null;
        }
    }

    public boolean restoreFromArchive(File archive) {
        deleteFromDisk();
        String dir = engine_.getProperty("5pHome") + "data" + File.separator + name_;
        new File(dir).mkdir();
        Enumeration entries;
        ZipFile zipFile;
        try {
            zipFile = new ZipFile(archive);
            entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String eName = entry.getName();
                if (eName.startsWith("mysql") || eName.startsWith("derby")) {
                    continue;
                }
                if (entry.isDirectory()) {
                    System.err.println("Extracting directory: " + entry.getName());
                    (new File(dir + File.separator + eName)).mkdir();
                    continue;
                }
                eName = FPUtil.fixFilename(eName);
                (new File(dir + File.separator + eName).getParentFile()).mkdirs();
                FPUtil.copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(dir + File.separator + eName)));
            }
            zipFile.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        this.readXMLFile(false);
        try {
            legDB_ = null;
            zipFile = new ZipFile(archive);
            switch(LegacyDB.getRDBMSCode(props_.getProperty("rdbms"))) {
                case LegacyDB.DB_DERBY:
                    legDB_ = new LegacyDerbyDB(engine_, this, zipFile);
                    break;
                case LegacyDB.DB_MYSQL:
                    legDB_ = new LegacyMysqlDB(engine_, this, engine_.getProperty("dbUsername"), engine_.getProperty("dbPassword"), zipFile);
                    break;
            }
            if (legDB_ != null) {
                return true;
            } else {
                System.out.println("Invalid rdbms: " + props_.getProperty("rdbms"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static void initDP(String name, String desc, double initX, double initY, Properties sysProps) {
        String dataDir = sysProps.getProperty(SysProps.FP_HOME) + "data" + File.separator + name + File.separator;
        new File(dataDir).mkdir();
        try {
            String filename = dataDir + "datapackage.xml";
            FileWriter writer = new FileWriter(filename);
            writer.write("<?xml version=\"1.0\"?>\n");
            writer.write("<datapackage name=\"" + name + "\" desc=\"" + desc + "\">\n");
            writer.write("  <properties>\n");
            writer.write("    <property name=\"dataSource\"></property>\n");
            writer.write("    <property name=\"logQueries\">false</property>\n");
            writer.write("    <property name=\"loadNetworkCache\">false</property>\n");
            writer.write("    <property name=\"loadTransitCache\">false</property>\n");
            writer.write("    <property name=\"dbUsername\"></property>\n");
            writer.write("    <property name=\"dbPassword\"></property>\n");
            writer.write("    <property name=\"initX\">" + initX + "</property>\n");
            writer.write("    <property name=\"initY\">" + initY + "</property>\n");
            writer.write("    <property name=\"initRes\">50</property>\n");
            writer.write("  </properties>\n");
            writer.write("\n");
            writer.write("  <basescenario>\n");
            writer.write("  </basescenario>\n");
            writer.write("\n");
            writer.write("</datapackage>\n");
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        createDataFile(dataDir + "routes.xml", new String[] { "providers", "routes" });
        createDataFile(dataDir + "stations.xml", new String[] { "stations" });
    }

    private static void createDataFile(String filename, String[] elements) {
        try {
            FileWriter writer = new FileWriter(filename);
            writer.write("<?xml version=\"1.0\"?>\n");
            writer.write("<fivepoints>\n");
            for (int i = 0; i < elements.length; i++) {
                String element = elements[i];
                writer.write("<" + element + ">\n");
                ;
                writer.write("</" + element + ">\n");
            }
            writer.write("</fivepoints>\n");
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void readEntireNetwork() {
        System.out.println("Reading all nodes..");
        spatialData_.allNodes();
        System.out.println("Reading all links..");
        spatialData_.allLinks();
    }

    public void readNetworkFromSHP() {
        String home = engine_.getProperty("5pHome"), fs = File.separator;
        String dpName = engine_.getDataPackage().getName();
        try {
            File file = new File(home + "data" + fs + dpName + fs + "shp" + fs + "nodes.shp");
            if (!file.exists()) {
                System.out.println("Fatal error: nodes shapefile does not exist");
                System.exit(1);
            }
            URL shapeURL = file.toURI().toURL();
            ShapefileDataStore store = new ShapefileDataStore(shapeURL);
            readNetworkNodes(store.getFeatureSource(store.getTypeNames()[0]));
        } catch (Exception e) {
            System.out.println("Error reading nodes shapefile:");
            e.printStackTrace();
        }
        try {
            File file = new File(home + "data" + fs + dpName + fs + "shp" + fs + "links.shp");
            if (!file.exists()) {
                System.out.println("Fatal error: links shapefile does not exist");
                System.exit(1);
            }
            URL shapeURL = file.toURI().toURL();
            ShapefileDataStore store = new ShapefileDataStore(shapeURL);
            readNetworkLinks(store.getFeatureSource(store.getTypeNames()[0]));
        } catch (Exception e) {
            System.out.println("Error reading nodes shapefile:");
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void readNetworkFromPostgis() {
        try {
            readNetworkNodes(((PostgisConnection) spatialData_).getNodeSource());
            readNetworkLinks(((PostgisConnection) spatialData_).getLinkSource());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readNetworkNodes(FeatureSource source) {
        try {
            FeatureCollection fColl = source.getFeatures();
            System.out.println("Feature collection type: " + fColl.getSchema().getName());
            System.out.println("Num features: " + fColl.size());
            FeatureIterator fIter = fColl.features();
            int c = 0;
            while (fIter.hasNext()) {
                c++;
                if (c % 10000 == 0) {
                    System.out.println("loaded " + c + " features");
                }
                SimpleFeature f = (SimpleFeature) fIter.next();
                BNode node = featureIO_.readNode(f);
                if (node != null) engine_.getBaseNet().addNode(node);
            }
            System.out.println("loaded " + c + " features total");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void readNetworkLinks(FeatureSource source) {
        try {
            FeatureCollection fColl = source.getFeatures();
            System.out.println("Feature collection type: " + fColl.getSchema().getName());
            System.out.println("Num features: " + fColl.size());
            FeatureIterator fIter = fColl.features();
            int c = 0;
            while (fIter.hasNext()) {
                c++;
                if (c % 10000 == 0) {
                    System.out.println("loaded " + c + " features");
                }
                SimpleFeature f = (SimpleFeature) fIter.next();
                BLink link = featureIO_.readLink(f);
                if (link != null) engine_.getBaseNet().addLink(link);
            }
            System.out.println("loaded " + c + " features total");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void writeNetworkToSHP(GUIOutputAcceptor gui) {
        String home = engine_.getProperty("5pHome"), fs = File.separator;
        String dpName = engine_.getDataPackage().getName();
        CoordinateReferenceSystem crs = null;
        try {
            crs = CRS.decode(getProperty("crsCode"));
        } catch (Exception ex) {
            Logger.getLogger(DataPackage.class.getName()).log(Level.SEVERE, null, ex);
        }
        File shpDir = new File(home + "data" + fs + dpName + fs + "shp");
        if (!shpDir.exists()) shpDir.mkdir();
        gui.statusText("Writing nodes...");
        try {
            File file = new File(home + "data" + fs + dpName + fs + "shp" + fs + "nodes.shp");
            if (!file.exists()) file.createNewFile();
            ShapefileDataStore outStore = new ShapefileDataStore(file.toURI().toURL());
            outStore.forceSchemaCRS(crs);
            SimpleFeatureType featureType = featureIO_.getNodeFeatureType();
            outStore.createSchema(featureType);
            featureIO_.writeNodes(engine_.getBaseNet().getNodes(), (FeatureStore) outStore.getFeatureSource(featureType.getName()));
            gui.msg("Nodes shapefile written");
        } catch (Exception e) {
            e.printStackTrace();
        }
        gui.statusText("Writing links...");
        try {
            File file = new File(home + "data" + fs + dpName + fs + "shp" + fs + "links.shp");
            if (!file.exists()) file.createNewFile();
            ShapefileDataStore outStore = new ShapefileDataStore(file.toURI().toURL());
            outStore.forceSchemaCRS(crs);
            SimpleFeatureType featureType = featureIO_.getLinkFeatureType();
            outStore.createSchema(featureType);
            featureIO_.writeLinks(engine_.getBaseNet().getLinks(), (FeatureStore) outStore.getFeatureSource(featureType.getName()));
            gui.msg("Links shapefile written");
        } catch (Exception e) {
            e.printStackTrace();
        }
        gui.statusText("");
    }

    public void writeNetworkToPostgis(GUIOutputAcceptor gui) {
        try {
            java.util.Map params = new java.util.HashMap();
            params.put(PostgisDataStoreFactory.DBTYPE.key, "postgis");
            params.put(PostgisDataStoreFactory.HOST.key, "localhost");
            params.put(PostgisDataStoreFactory.PORT.key, new Integer(5432));
            params.put(PostgisDataStoreFactory.SCHEMA.key, "public");
            String dbName = "fp_" + engine_.getDataPackage().getName();
            String username = getProperty("dbUsername");
            String password = getProperty("dbPassword");
            params.put(PostgisDataStoreFactory.DATABASE.key, dbName);
            params.put(PostgisDataStoreFactory.USER.key, username);
            params.put(PostgisDataStoreFactory.PASSWD.key, password);
            DataStore outStore = DataStoreFinder.getDataStore(params);
            System.out.println("datastore=" + outStore);
            gui.statusText("Writing nodes...");
            SimpleFeatureType nodeFT = featureIO_.getNodeFeatureTypePostgis();
            outStore.createSchema(nodeFT);
            featureIO_.writeNodes(engine_.getBaseNet().getNodes(), (FeatureStore) outStore.getFeatureSource(nodeFT.getName()));
            gui.msg("Nodes table written");
            gui.statusText("Writing links...");
            SimpleFeatureType linkFT = featureIO_.getLinkFeatureType();
            outStore.createSchema(linkFT);
            featureIO_.writeLinks(engine_.getBaseNet().getLinks(), (FeatureStore) outStore.getFeatureSource(linkFT.getName()));
            gui.msg("Links table written");
            gui.statusText("");
            outStore.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class DummyDataConnection implements SpatialDataConnection {

        public void close() {
        }

        public void createNode(BNode node) {
        }

        public void deleteNode(BNode node) {
        }

        public void createLink(BLink link) {
        }

        public void deleteLink(BLink link) {
        }

        public void modifyNode(BNode node, NodeAttr[] attrs) {
        }

        public void modifyLink(BLink link, LinkAttr[] attrs) {
        }

        public void modifyNodes(Collection<BNode> nodes, NodeAttr[] attrs) {
        }

        public void modifyLinks(Collection<BLink> links, LinkAttr[] attrs) {
        }

        public BNode nodeFromID(int id) {
            return null;
        }

        public Collection<BNode> allNodes() {
            return null;
        }

        public Collection<BNode> nearbyNodes(double x, double y, double r) {
            return null;
        }

        public Collection<BNode> nodesInRange(double x1, double y1, double x2, double y2) {
            return null;
        }

        public Collection<BLink> allLinks() {
            return null;
        }

        public Collection<BLink> linksInRange(double x1, double y1, double x2, double y2) {
            return null;
        }

        public Collection<BLink> linksInSet(Collection<Integer> ids) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Collection<BNode> nodesInSet(Collection<Integer> ids) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
