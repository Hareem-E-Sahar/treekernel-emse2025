package org.ms150hams.trackem.util.configdb;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;

public final class ConfigDB {

    private static final Logger logger = Logger.getLogger("org.ms150hams.trackem.util.configdb");

    public static final String framework = "embedded";

    public static final String driver = "org.apache.derby.jdbc.EmbeddedDriver";

    public static final String protocol = "jdbc:derby:";

    private static Properties props = new Properties();

    private static Connection conn = null;

    private static final int currentDBVersion = 3;

    public static void main(String[] args) {
        if (args.length >= 1 && args[0].equals("listDBs")) {
            File[] dbs = listConfigDBs();
            System.out.println("Found the following DBs:");
            for (int i = 0; i < dbs.length; i++) {
                try {
                    System.out.println("  " + dbs[i].getName() + " (" + dbs[i].getCanonicalPath() + ")");
                } catch (IOException e) {
                }
            }
        } else if (args.length >= 1 && args[0].equals("createDB")) {
            File f = null;
            if (args.length > 1) {
                f = new File(args[1]);
            } else {
                f = new File("./TrackemEvents/ConfigDB");
            }
            initConfigDB(f);
        } else if (args.length >= 1 && args[0].equals("listStations")) {
            File f = null;
            if (args.length > 1) {
                f = new File(args[1]);
            } else {
                f = new File("./TrackemEvents/ConfigDB");
            }
            String[][] stations = listStations(f);
            for (int i = 0; i < stations.length; i++) System.out.println(stations[i][0] + ": " + stations[i][1]);
        } else {
            System.err.println("Unknown argument " + args[0]);
        }
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            logger.info("Derby shutdown.");
        }
    }

    public static String[][] listStations(File f) {
        try {
            Class.forName(driver).newInstance();
            logger.fine("Loaded the appropriate driver.");
            String path = null;
            path = f.getCanonicalPath();
            path = path.replaceAll("/jar:", "jar:");
            Connection conn = DriverManager.getConnection(protocol + path, props);
            conn.setAutoCommit(false);
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("VALUES (SELECT COUNT(ID) FROM SAG)" + "+(SELECT COUNT(ID) FROM RestStop)+(SELECT COUNT(ID) FROM CommandCenter)" + "+(SELECT COUNT(ID) FROM StaffVehicle)+(SELECT COUNT(ID) FROM SupplyVehicle)" + "+(SELECT COUNT(ID) FROM Other)");
            rs.next();
            int n = rs.getInt(1);
            String[][] ret = new String[n][];
            int pos = 0;
            rs.close();
            rs = s.executeQuery("SELECT 'SAG' || CHAR(id) as uid, 'SAG ' || CHAR(id) as name FROM SAG");
            while (rs.next()) {
                ret[pos++] = new String[] { rs.getString("uid").trim(), rs.getString("name").trim() };
            }
            rs.close();
            rs = s.executeQuery("SELECT 'RST' || CHAR(id) as uid, 'Rest Stop ' || cityname as name FROM RestStop");
            while (rs.next()) {
                ret[pos++] = new String[] { rs.getString("uid").trim(), rs.getString("name").trim() };
            }
            rs.close();
            rs = s.executeQuery("SELECT 'CMD' || CHAR(id) as uid, displayname as name FROM CommandCenter");
            while (rs.next()) {
                ret[pos++] = new String[] { rs.getString("uid").trim(), rs.getString("name").trim() };
            }
            rs.close();
            rs = s.executeQuery("SELECT 'STV' || CHAR(id) as uid, vehName as name FROM StaffVehicle");
            while (rs.next()) {
                ret[pos++] = new String[] { rs.getString("uid").trim(), rs.getString("name").trim() };
            }
            rs.close();
            rs = s.executeQuery("SELECT 'SUP' || CHAR(id) as uid, vehName as name FROM SupplyVehicle");
            while (rs.next()) {
                ret[pos++] = new String[] { rs.getString("uid").trim(), rs.getString("name").trim() };
            }
            rs.close();
            rs = s.executeQuery("SELECT 'OTH' || CHAR(id) as uid, displayname as name FROM Other");
            while (rs.next()) {
                ret[pos++] = new String[] { rs.getString("uid").trim(), rs.getString("name").trim() };
            }
            try {
                DriverManager.getConnection("jdbc:derby:" + f.getCanonicalPath() + ";shutdown=true");
            } catch (SQLException e) {
                logger.fine("Derby shutdown.");
            }
            conn.close();
            return ret;
        } catch (Exception e) {
            logger.log(Level.WARNING, "listStations error: ", e);
            e.printStackTrace();
            if (e instanceof SQLException && ((SQLException) e).getNextException() != null) logger.log(Level.WARNING, "Chained Exception:", ((SQLException) e).getNextException());
        }
        return null;
    }

    public static File[] listConfigDBs() {
        File[] tmpRoots = File.listRoots();
        ArrayList tmp = new ArrayList();
        File[] roots = new File[tmpRoots.length + 1];
        System.arraycopy(tmpRoots, 0, roots, 0, tmpRoots.length);
        roots[roots.length - 1] = new File(".");
        for (int i = 0; i < roots.length; i++) {
            try {
                File root = roots[i];
                File subdir = new File(root, "TrackemEvents");
                if (!subdir.exists()) continue;
                if (!subdir.isDirectory()) continue;
                logger.finer("Looking for Event files in " + subdir.getCanonicalPath());
                File[] possibilities = subdir.listFiles();
                for (int j = 0; j < possibilities.length; j++) {
                    if (!possibilities[j].isDirectory()) continue;
                    File prop = new File(possibilities[j], "service.properties");
                    if (!prop.exists()) {
                        logger.fine("Folder " + possibilities[j] + " exists, but is not recognized as a Derby database.)");
                        continue;
                    }
                    logger.finer("Found event config database " + possibilities[j]);
                    tmp.add(possibilities[j]);
                }
            } catch (IOException e) {
                logger.fine("Error enumerating event files: \n" + e);
                continue;
            }
        }
        logger.finer("Returning possible events " + tmp);
        return (File[]) tmp.toArray(new File[tmp.size()]);
    }

    /**
     * Initializes and connects to the specified Configuration Database, and then,
     * if the schema does not exist, creates it and inserts temporary development
     * data.
     * @param dbName - The filesystem name of the desired configuration database.
     */
    public static void initConfigDB(File db) {
        if (conn != null) return;
        logger.info("Starting ConfigDB in " + framework + " mode.");
        try {
            Class.forName(driver).newInstance();
            logger.fine("Loaded the appropriate driver.");
            String path = null;
            path = db.getCanonicalPath();
            path = path.replaceAll("/jar:", "jar:");
            conn = DriverManager.getConnection(protocol + path + ";create=true", props);
            conn.setAutoCommit(false);
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM SYS.SYSTABLES WHERE TABLETYPE='T'");
            if (!rs.next()) {
                logger.warning("No user tables found, creating development schema and data.");
                createTables();
                insertTempData();
            } else {
                rs = s.executeQuery("SELECT INTEGER (Value) as version FROM MetaData WHERE Name='DBVERSION'");
                rs.next();
                if (rs.getInt(1) != currentDBVersion) {
                    logger.severe("This configDB version (" + rs.getInt(1) + ") is not equal to software version " + currentDBVersion + ".  \n" + "You probably need to delete your existing DB (" + db.getPath() + ") and allow a new one to be generated.");
                }
            }
            s.close();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error connecting ConfigDB: ", e);
            e.printStackTrace();
        }
    }

    public static void createTables() {
        logger.info("Creating Config Database Tables");
        try {
            Statement s = conn.createStatement();
            s.execute("CREATE TABLE MetaData(Name VARCHAR(10), Value VARCHAR(50))");
            s.execute("INSERT INTO MetaData (name, value) VALUES ('DBVERSION', '" + currentDBVersion + "')");
            s.execute("CREATE TABLE SAG(id int PRIMARY KEY, callsign VARCHAR(10), opname VARCHAR(30), note VARCHAR(100))");
            s.execute("CREATE TABLE RestStop(id int PRIMARY KEY, callsign VARCHAR(10), opname VARCHAR(40), cityname VARCHAR(30), lat DOUBLE, lon DOUBLE, note VARCHAR(100))");
            s.execute("CREATE TABLE StationGroup(id int PRIMARY KEY, name VARCHAR(30), note VARCHAR(100))");
            s.execute("CREATE TABLE StationGroupMember(groupid int, memberid VARCHAR(10))");
            s.execute("CREATE TABLE StaffVehicle(id int PRIMARY KEY, callsign VARCHAR(10), opName VARCHAR(30), vehName VARCHAR(30), opnote VARCHAR(100))");
            s.execute("CREATE TABLE SupplyVehicle(id int PRIMARY KEY, callsign VARCHAR(10), opname VARCHAR(30), vehName VARCHAR(30), note VARCHAR(100))");
            s.execute("CREATE TABLE Other(id int PRIMARY KEY, callsign VARCHAR(10), displayname VARCHAR(30), note VARCHAR(100))");
            s.execute("CREATE TABLE CommandCenter(id int PRIMARY KEY, callsign VARCHAR(10), opname VARCHAR(30), displayname VARCHAR(30), note VARCHAR(100))");
            s.execute("CREATE TABLE NetworkResponsibilities (stationID VARCHAR(5), topLeft DOUBLE, topRight DOUBLE, bottomLeft DOUBLE, bottomRight DOUBLE)");
            s.execute("CREATE TABLE NetworkTopology (stationID VARCHAR(5), neighbor VARCHAR(5), neighborCallsign VARCHAR(7))");
            s.execute("CREATE TABLE Route (id int PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY, name VARCHAR(30))");
            s.execute("CREATE TABLE RouteWaypoint (id int PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY, routeID INT, waypointOrder INT, latitude DOUBLE, longitude DOUBLE)");
            s.execute("CREATE TABLE RoutePOI (id int PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,  name VARCHAR(100), latitude DOUBLE, longitude DOUBLE, description VARCHAR(255))");
            s.close();
            conn.commit();
        } catch (Exception e) {
            logger.severe("Error creating tables: " + e);
        }
    }

    public static void insertTempData() {
        logger.info("Inserting Temp Data");
        try {
            Statement s = conn.createStatement();
            s.executeUpdate("INSERT INTO RestStop (ID, CALLSIGN, OPNAME, CITYNAME, LAT, LON) VALUES " + "(1, 'N0RPM', 'Jason Godfrey', 'Carlton', 46.66099628576818, -92.42778396593793)," + "(2, 'K0RTB', 'Bob Bohrer', 'Mahtowa', 46.57477931460706, -92.63051708615714)," + "(3, 'N0BFG', 'Ryan Bloch', 'Moose Lake', 46.44105810339763, -92.77403920455779)," + "(4, 'N0QPM', 'Al Jones', 'Willow River', 46.30633722290082, -92.82839852980236), " + "(5, 'KC0QEL', 'Neil Dolan', 'Finlayson', 0, 0), " + "(6, 'KC0MGK', 'Nicole Hanson', 'Hinckley', 46.00849130361021, -92.9001464332689)");
            s.executeUpdate("INSERT INTO SAG (ID, CALLSIGN, OPNAME) VALUES " + "(1, '', ''), (2, 'W0VJ', 'Vron Jones'), (3, '', '')," + "(4, 'KC0TFB', 'Peter Gamache'), (5, 'KB0RZU', 'Lou Behrens'), (6, 'N0YAM', 'Brian Peterson')," + "(7, 'KC0TQY', 'Don Remington'), (8, 'KC0WAK', 'Danielle Tetrault'), (9, 'AB0XE', 'Steve Howard')," + "(10, 'KC0SAN', 'Jean Arimond'), (11, 'BUS1', ''), (12, 'BUS2', '')");
            s.executeUpdate("INSERT INTO CommandCenter (ID, CALLSIGN, OPNAME, DISPLAYNAME, NOTE) VALUES \n" + "(1, 'KC0PZN', 'John Laxson', 'Hinckley Command Center', ''),\n" + "(2, 'N0AWN', 'Ralph Bierbaum', 'Hinckley Net Control 1', ''),\n" + "(3, 'N0HOY', 'Dean Blosberg', 'Communications Supervisor', ''),\n" + "(4, 'KB0MVW', 'Dave Healy', 'Hinckley Net Control 3', ''),\n" + "(5, 'N0ZRD', 'Gordy Hanson', 'Net Control North', ''),\n" + "(6, '', 'Tim Arimond', 'Backup Net Control', '')");
            s.executeUpdate("INSERT INTO Other (id, callsign, displayname) values (1, 'N0CALL', 'Other 1')," + "(2, 'N0CALL', 'Other 2')");
            s.executeUpdate("INSERT INTO StaffVehicle (id, callsign, opname, vehName) VALUES " + "(1, 'N0HOY', 'Dean Blosberg', 'Command Center Rover')," + "(2, 'KF6WQH', 'Mike Jensen', 'Data Support Rover')," + "(3, 'N0VUY', 'Scott Ahlgren', 'SAG Leader')," + "(4, 'N0CALL', 'Driver1', 'Hospital Runner 1')," + "(5, 'N0CALL', 'Driver2', 'Hospital Runner 2')," + "(6, 'N0CALL', 'Driver3', 'Hospital Runner 3')");
            s.executeUpdate("INSERT INTO SupplyVehicle (id, callsign, opname, vehName) VALUES " + "(1, 'N0RUG', 'Pete Winters', 'Communications Setup 1')," + "(2, 'KC0NPA', 'Rich Bopp', 'Communications Setup 2')");
            s.executeUpdate("INSERT INTO StationGroup (id, name) VALUES " + "(1, 'All Stations'), (2, 'All Rest Stops')," + "(3, 'Command Centers'), (4, 'Technical Support')");
            s.executeUpdate("INSERT INTO StationGroupMember (groupid, memberid) VALUES " + "(2, 'RST1'), (2, 'RST2'), (2, 'RST3'), (2, 'RST4'), (2, 'RST5'), (2, 'RST6'), " + "(3, 'CMD1'), (3, 'CMD2'), (3, 'CMD3'), " + "(4, 'STV2'), (4, 'RST1'), (4, 'CMD1')");
            s.executeUpdate("INSERT INTO NetworkTopology (stationID, neighbor, neighborCallsign) VALUES" + "('RST1', 'RST2', 'REST2')," + "('RST2', 'RST1', 'REST1'), ('RST2', 'RST3', 'REST3')," + "('RST3', 'RST2', 'REST2'), ('RST3', 'RST4', 'REST4')," + "('RST4', 'RST3', 'REST3'), ('RST4', 'RST5', 'REST5')," + "('RST5', 'RST4', 'REST4'), ('RST5', 'RST6', 'REST6')," + "('RST6', 'RST5', 'REST5')");
            s.executeUpdate("INSERT INTO Route (id, name) VALUES (1, 'MS150 Saturday Route')");
            s.executeUpdate("INSERT INTO RouteWaypoint (routeID, waypointOrder, latitude, longitude) VALUES " + "(1, 100, 46.74364072798709, -92.23688863749955)," + "(1, 200, 46.66099628576818, -92.42778396593793)," + "(1, 300, 46.57477931460706, -92.63051708615714)," + "(1, 400, 46.44105810339763, -92.77403920455779)," + "(1, 500, 46.31999586356624, -92.83526499328424)," + "(1, 600, 46.12786946957677, -92.8667544973399)," + "(1, 700, 46.00849130361021, -92.9001464332689)");
            s.executeUpdate("INSERT INTO RoutePOI (name, latitude, longitude, description) VALUES " + "('POI1', 45.0029022167013, -93.0276062392375, 'Community Center')," + "('POI3', 45.00642414297052, -93.00836674507866, 'Trail Crossing 1st St')," + "('POI4', 45.02675575985891, -92.95873573948273, 'Trail Crossing 694')," + "('POI5', 45.03588038837339, -92.94540380574975, 'Trail Crossing 36')," + "('POI6', 45.07966702300454, -92.89702005173483, 'Rest Stop A')," + "('POI7', 45.10455799160599, -92.86491409632716, 'Trail Crossing Manning Tr')," + "('POI8', 45.1210086499513, -92.83780995910567, 'Rest Stop B')," + "('POI9', 45.10459354490848, -92.8333531812055, 'Intersection of Myeron and County 55')," + "('POI10', 45.07796274976818, -92.82993369290534, 'Intersection of County 55 and 96')," + "('POI11', 45.13846254665923, -92.84232949853598, 'Turn at 55 and 4')," + "('POI12', 45.13322392870241, -92.87645817215643, 'Turn onto Manning (MS60)')," + "('POI13', 45.19513419729164, -92.88136754061966, 'Rest Stop C')," + "('POI14', 45.23662284351055, -92.88546586216798, 'Turn on Mayberry Tr')," + "('POI15', 45.24939239721147, -92.81163463331697, 'Rest Stop D')," + "('POI16', 45.19467813114959, -92.82681748356127, 'Turn from 3 to 4')");
            s.close();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getNextException() != null) e.getNextException().printStackTrace();
        }
    }

    public static Statement getStatement() {
        if (conn == null) {
            logger.severe("ConfigDB must be initialized, first");
            return null;
        }
        try {
            return conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Statement getScrollStatement() {
        if (conn == null) {
            logger.severe("ConfigDB must be Initialized first");
            return null;
        }
        try {
            return conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
