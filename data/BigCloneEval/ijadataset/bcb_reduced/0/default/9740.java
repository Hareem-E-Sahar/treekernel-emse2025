import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

public class Importer {

    public static void main(String[] args) {
        System.out.println("Importer Start!!");
        String filename = System.getProperty("i", "unknown");
        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfDataset.openFile(filename, null);
            processNetcdfFile(ncfile);
        } catch (IOException ioe) {
            log("trying to open " + filename, ioe);
        } finally {
            if (null != ncfile) try {
                ncfile.close();
            } catch (IOException ioe) {
                log("trying to close " + filename, ioe);
            }
        }
    }

    private static void processNetcdfFile(NetcdfFile ncfile) {
        System.out.println("Processing dataset...");
        ucar.ma2.Array net_us = readVariable(ncfile, System.getProperty("u"));
        ucar.ma2.Array net_vs = readVariable(ncfile, System.getProperty("v"));
        ucar.ma2.Array net_lats = readVariable(ncfile, System.getProperty("lat"));
        ucar.ma2.Array net_lons = readVariable(ncfile, System.getProperty("lon"));
        int[] lat_shape = net_lats.getShape();
        int[] lon_shape = net_lons.getShape();
        ucar.ma2.Index lat_index = net_lats.getIndex();
        ucar.ma2.Index lon_index = net_lons.getIndex();
        ucar.ma2.Index us_index = net_us.getIndex();
        ucar.ma2.Index vs_index = net_vs.getIndex();
        if (lat_shape.length == 2) {
            System.out.println("linear");
            for (int i = 0; i < lat_shape[0]; i++) {
                for (int j = 0; j < lon_shape[1]; j++) {
                    float lats = net_lats.getFloat(lat_index.set(i, j));
                    float lons = net_lons.getFloat(lon_index.set(i, j));
                    int gridid = getGridID(lats, lons);
                    double u = net_us.getDouble(us_index.set(i, j));
                    double v = net_vs.getDouble(vs_index.set(i, j));
                    double[] values = getValues(u, v);
                    float speed = (float) values[0];
                    float direction = (float) values[1];
                    int rotation = (int) values[2];
                    System.out.print("Speed: " + speed + " Dir: " + direction + " Rot: " + rotation);
                }
            }
        } else if (lat_shape.length == 1) {
            System.out.println("nested");
            for (int i = 0; i < lat_shape[0]; i++) {
                for (int j = 0; j < lon_shape[0]; j++) {
                    float lats = net_lats.getFloat(lat_index.set(i));
                    float lons = net_lons.getFloat(lon_index.set(j));
                    int gridid = getGridID(lats, lons);
                    double u = net_us.getDouble(us_index.set(i, j));
                    double v = net_vs.getDouble(vs_index.set(i, j));
                    double[] values = getValues(u, v);
                    float speed = (float) values[0];
                    float direction = (float) values[1];
                    int rotation = (int) values[2];
                    System.out.println("Speed: " + speed + " Dir: " + direction + " Rot: " + rotation);
                }
            }
        }
    }

    private static int getGridID(double lat, double lon) {
        BigDecimal big_lat = new BigDecimal(lat);
        BigDecimal big_lon = new BigDecimal(lon);
        String zLat = big_lat.setScale(6, 6).toString();
        String zLon = big_lon.setScale(6, 6).toString();
        int grid_fallback = -999;
        try {
            String url = "jdbc:postgresql://155.206.19.246/ODM-Gamma";
            Class.forName("org.postgresql.Driver");
            Connection db = DriverManager.getConnection(url, "postgis", "");
            Statement st = db.createStatement();
            ResultSet rs = st.executeQuery("SELECT gid from mdm.grid WHERE point_geom=GeomFromText('POINT(" + zLon + " " + zLat + ")',4326)");
            while (rs.next()) {
                int gridid = rs.getInt(1);
                rs.close();
                st.close();
                db.close();
                return gridid;
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Caught ClassNotFoundException: " + e.getMessage());
            return grid_fallback;
        } catch (SQLException e) {
            System.out.println("Caught SQLException: " + e.getMessage());
            return grid_fallback;
        }
        return grid_fallback;
    }

    private static double[] getValues(double u, double v) {
        double speed = Math.sqrt(Math.pow(v, 2) + Math.pow(u, 2));
        double degrees = Math.toDegrees(Math.atan2(v, u));
        double rotation = Math.round(((270 - degrees) + 360) % 360);
        double direction = Math.round((((90 - degrees) + 360) % 360) * Math.pow(10, (double) 2)) / Math.pow(10, (double) 2);
        double[] values = { speed, direction, rotation };
        return values;
    }

    public static boolean storeInformation(int gridid, float value, int variableid, int rotation) {
        return storeInformation(gridid, value, variableid, rotation, -1);
    }

    public static boolean storeInformation(int gridid, float value, int variableid, int rotation, int assist) {
        try {
            String url = "jdbc:postgresql://155.206.19.246/ODM-Gamma";
            Class.forName("org.postgresql.Driver");
            Connection db = DriverManager.getConnection(url, "postgis", "");
            Statement st = db.createStatement();
            st.close();
            db.close();
        } catch (ClassNotFoundException e) {
            System.out.println("Caught ClassNotFoundException: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Caught SQLException: " + e.getMessage());
        }
        return true;
    }

    private static void log(String string, IOException ioe) {
        System.out.println(string);
    }

    private static ucar.ma2.Array readVariable(NetcdfFile ncfile, String varName) {
        Variable v = ncfile.findVariable(varName);
        if (null == v) {
            ucar.ma2.Array none = null;
            return none;
        }
        try {
            int[] varShape = v.getShape();
            int[] origin = new int[varShape.length];
            int[] size = null;
            if (varShape.length == 2) {
                size = new int[] { varShape[0], varShape[1] };
            } else {
                size = new int[] { varShape[0] };
            }
            try {
                ucar.ma2.Array data = v.read(origin, size);
                return data;
            } catch (InvalidRangeException ioe) {
                System.out.println("Processing dataset...");
            }
        } catch (IOException ioe) {
            log("trying to read " + varName, ioe);
        }
        ucar.ma2.Array none = null;
        return none;
    }
}
