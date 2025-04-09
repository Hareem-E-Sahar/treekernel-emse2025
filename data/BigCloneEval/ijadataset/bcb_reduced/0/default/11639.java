import de.tudarmstadt.ito.xmldbms.Map;
import de.tudarmstadt.ito.xmldbms.mapfactories.MapFactory_DTD;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Hashtable;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.apache.xerces.parsers.SAXParser;

public class GenerateMap {

    static final byte[] RETURN = System.getProperty("line.separator").getBytes();

    public static void main(String[] argv) {
        try {
            if (argv.length != 1) {
                System.out.println("\nUsage: java GenerateMap <input-file>\n");
                return;
            }
            generateMap(argv[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void generateMap(String filename) throws Exception {
        Map map;
        String basename;
        InputSource src;
        src = new InputSource(getFileURL(filename));
        basename = getBasename(filename);
        map = createMap(src, filename);
        serializeMap(map, basename);
        createStatements(map, basename);
    }

    static Map createMap(InputSource src, String filename) throws Exception {
        MapFactory_DTD factory = new MapFactory_DTD();
        String ext = getExtension(filename);
        Hashtable namespaceURIs = null;
        if (ext.equals("DDM")) {
            return factory.createMapFromSchema(src, MapFactory_DTD.SCHEMA_DDML, true, getSAXParser());
        } else if (ext.equals("DTD")) {
            return factory.createMapFromDTD(src, MapFactory_DTD.DTD_EXTERNAL, true, namespaceURIs);
        } else {
            return factory.createMapFromDTD(src, MapFactory_DTD.DTD_XMLDOCUMENT, true, namespaceURIs);
        }
    }

    static void serializeMap(Map map, String basename) throws Exception {
        FileOutputStream mapFile;
        mapFile = new FileOutputStream(basename + ".map");
        map.serialize(mapFile, true, 3);
        mapFile.close();
    }

    static void createStatements(Map map, String basename) throws Exception {
        FileOutputStream sqlFile;
        String[] createStrings;
        String url = "jdbc:postgresql:sports?user=nobody&password=PASSWORD", driver = "org.postgresql.Driver";
        Connection conn;
        Class.forName(driver);
        conn = DriverManager.getConnection(url);
        map.setConnection(conn);
        sqlFile = new FileOutputStream(basename + ".sql");
        createStrings = map.getCreateTableStrings();
        for (int i = 0; i < createStrings.length; i++) {
            sqlFile.write(createStrings[i].getBytes());
            sqlFile.write(RETURN);
        }
        sqlFile.close();
    }

    static String getFileURL(String fileName) {
        File file;
        file = new File(fileName);
        return "file:///" + file.getAbsolutePath();
    }

    static String getBasename(String filename) {
        int period;
        period = filename.lastIndexOf('.', filename.length());
        if (period == -1) {
            return filename;
        } else {
            return filename.substring(0, period);
        }
    }

    static String getExtension(String filename) {
        int period;
        period = filename.lastIndexOf('.', filename.length());
        if (period == -1) {
            return "";
        } else {
            return filename.substring(period + 1, filename.length()).toUpperCase();
        }
    }

    static Parser getSAXParser() {
        return new SAXParser();
    }
}
