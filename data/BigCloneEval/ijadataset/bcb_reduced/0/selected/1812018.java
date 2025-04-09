package spidr.swr;

import java.sql.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import wdc.dbaccess.*;
import wdc.settings.*;
import wdc.utils.*;

public class AmieApi {

    /**   * Gets one grid of data for specific element, time stamp and hemisphere   *   * @param stmt The database statement   * @param element The element (field) to be consider   * @param timeStamp Java long formatted as yyyymmddhhmm   * @return Amie data on grid [0..23]x[0..23] (missing value as Float.NaN)   *   with index order [lat][mlt]   * @throws Exception   */
    public static float[][] getData(String element, long timeStamp, String hemisphere) throws Exception {
        int year = (int) (timeStamp / 10000000000L);
        Connection con = ConnectionPool.getConnection("amie" + year);
        Statement stmt = con.createStatement();
        float[][] data = getData(stmt, element, timeStamp, hemisphere);
        ConnectionPool.releaseConnection(con);
        return data;
    }

    /**   * Gets one grid of data for specific element, time stamp and northern hemisphere   *   * @param element The element (field) to be consider   * @param timeStamp Java long formatted as yyyymmddhhmm   * @return Amie data on grid [0..23]x[0..23] (missing value as Float.NaN)   *   with index order [lat][mlt]   * @throws Exception   */
    public static float[][] getData(String element, long timeStamp) throws Exception {
        return getData(element, timeStamp, "north");
    }

    static float[][] getData(Statement stmt, String element, long timeStamp) throws Exception {
        return getData(stmt, element, timeStamp, "north");
    }

    static float[][] getData(Statement stmt, String element, long timeStamp, String hemisphere) throws Exception {
        String[] TABLES = { "grids" + (int) (timeStamp / 100000000L) + "n_" + element.toLowerCase(), "grids" + (int) (timeStamp / 100000000L) + "s_" + element.toLowerCase() };
        boolean haveData = false;
        try {
            float[][] res = new float[24][24];
            String table = TABLES[0];
            if ("south".equalsIgnoreCase(hemisphere)) {
                table = TABLES[1];
            }
            try {
                stmt.executeQuery("DESCRIBE " + table);
            } catch (SQLException e) {
                System.out.println("Table not found: " + table);
                return null;
            }
            String sqlQuery = "SELECT data FROM " + table + " WHERE obsTime='" + Utilities.formatTime(timeStamp) + "'";
            ResultSet rs = stmt.executeQuery(sqlQuery);
            while (rs.next()) {
                float[] data = null;
                InputStream istr = rs.getBinaryStream(1);
                if (istr != null && istr.available() > 0) {
                    DataInputStream distr = new DataInputStream(istr);
                    for (int i = 0; i < 24; i++) {
                        for (int j = 0; j < 24; j++) {
                            res[i][j] = distr.readFloat();
                        }
                    }
                    distr.close();
                    haveData = true;
                }
            }
            rs.close();
            if (haveData) {
                return res;
            }
            System.out.println("Data not found: " + sqlQuery);
            return null;
        } catch (Exception e) {
            throw new Exception("Amie data selection error: " + e.toString());
        }
    }

    /**   * Gets one day of Amie index data for specific element   * @param element The element (index) to get data for   * @param dayId The day identifier yyyymmdd   * @return array of index data (missing value as Float.NaN)   * @throws Exception   */
    public static float[] getIndex(String element, int dayId) throws Exception {
        return getIndex(element, dayId, dayId);
    }

    /**   * Gets several days of Amie index data for specific element   * @param element The element (index) to get data for   * @param dayIdFrom The day identifier yyyymmdd   * @param dayIdTo The day identifier yyyymmdd   * @return array of dayly data (missing value as Float.NaN)   * @throws Exception   */
    public static float[] getIndex(String element, int dayIdFrom, int dayIdTo) throws Exception {
        DateInterval dateInterval = new DateInterval(new WDCDay(dayIdFrom), new WDCDay(dayIdTo));
        return getIndex(element, dateInterval);
    }

    /**   * Gets several days of data for specific element and location   * @param element The element (field) to get data for   * @param dateInterval The dateInterval   * @return array of dayly data (missing value as Float.NaN)   * @throws Exception   */
    public static float[] getIndex(String element, DateInterval dateInterval) throws Exception {
        Connection con = null;
        Statement stmt = null;
        try {
            con = ConnectionPool.getConnection("Amie");
            stmt = con.createStatement();
            float[] data = getIndex(stmt, element, dateInterval.getDateFrom().getDayId(), dateInterval.getDateTo().getDayId());
            return data;
        } catch (Exception e) {
            throw new Exception("Data are not available: " + e.toString());
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception e) {
            }
            ConnectionPool.releaseConnection(con);
        }
    }

    public static float[] getIndex(Statement stmt, String element, int dayIdFrom, int dayIdTo) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        GregorianCalendar clndr = new GregorianCalendar(TimeZone.getTimeZone("GMT"), Locale.US);
        DateInterval dateInterval = new DateInterval(new WDCDay(dayIdFrom), new WDCDay(dayIdTo));
        if (!dateInterval.isValid()) {
            return null;
        }
        String timeFrom = df.format(new java.util.Date(dateInterval.getDateFrom().epochTime()));
        String timeTo = df.format(new java.util.Date(dateInterval.getDateTo().epochTime() + 1440 * 60000));
        try {
            String sqlStr = "SELECT obsTime, " + element + " FROM amie_indexes " + "WHERE obsTime>=\"" + timeFrom + "\" AND obsTime<\"" + timeTo + "\"";
            ResultSet rs = stmt.executeQuery(sqlStr);
            float[] data = new float[dateInterval.numDays() * 1440];
            long startEtime = dateInterval.getDateFrom().epochTime();
            while (rs.next()) {
                clndr.setTime(df.parse(rs.getString(1)));
                long etime = clndr.getTime().getTime();
                int num = (int) ((etime - startEtime) / 60000);
                data[num] = rs.getFloat(2);
            }
            rs.close();
            return data;
        } catch (Exception e) {
            throw new Exception("Amie data selection error: " + e.toString());
        }
    }

    /**   * Used to test this class   * @param args command line arguments   */
    public static void main(String args[]) {
        System.out.println("Activate settings");
        try {
            if (args.length == 0) {
                System.out.println("No parameters: base conf-file required.");
                return;
            }
            Settings.getInstance().load(args[0]);
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
        System.out.println("Get index");
        long curTime = (new java.util.Date()).getTime();
        try {
            float[] data = AmieApi.getIndex("ind_bz", 19970203, 19971203);
            curTime = (new java.util.Date()).getTime() - curTime;
            System.out.println("Finish (" + (float) curTime / 1000 + " sec)");
            System.out.println("Num samples: " + ((data != null) ? data.length : 0));
        } catch (Exception e) {
            System.out.println("Index data are not available: " + e.toString());
        }
        System.out.println("Get grid");
        curTime = (new java.util.Date()).getTime();
        try {
            float[][] res = getData("aurenergyflux", 19970504235500L);
            curTime = (new java.util.Date()).getTime() - curTime;
            int counter = 0;
            if (res != null) {
                for (int i = 0; i < 24; i++) {
                    System.out.print("lat " + i + ":");
                    for (int j = 0; j < 24; j++) {
                        System.out.print(" mlon " + j + " = " + res[i][j]);
                    }
                    System.out.println();
                }
            }
            System.out.println("Finish (" + (float) curTime / 1000 + " sec)");
        } catch (Exception e) {
            System.out.println("Grid data are not available: " + e.toString());
        }
    }
}
