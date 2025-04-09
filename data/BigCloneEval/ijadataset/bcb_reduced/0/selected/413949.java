package org.cipotato.climate.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.cipotato.science.util.CalendarUtils;
import org.cipotato.science.util.UnitConverter;
import com.vividsolutions.jts.geom.Coordinate;

public class NOAAReader {

    private Vector<String> vector = new Vector<String>(3000000);

    private LinkedHashSet<String> deadlist = new LinkedHashSet<String>();

    private HashSet<String> uniqueStations = new HashSet<String>();

    private HashSet<String> selectStations = new HashSet<String>();

    private TreeMap<String, String> stations = new TreeMap<String, String>();

    private Vector<String> newdata = new Vector<String>();

    int recordSize = 44;

    public void clear() {
        vector.clear();
    }

    public String[] getStations() {
        String[] s = new String[stations.size()];
        Iterator i = stations.keySet().iterator();
        int k = 0;
        while (i.hasNext()) {
            s[k] = i.next().toString();
            k++;
        }
        ;
        return s;
    }

    public void readNOAATable(String fileName) {
        String str = "";
        String s1;
        String out = "";
        int i = 0;
        try {
            BufferedReader in = new BufferedReader(new FileReader(fileName));
            while ((str = in.readLine()) != null) {
                i++;
                if (i > 1) {
                    s1 = str.trim();
                    if (s1.length() < 2) break;
                    out = getStation(str) + getDate(str) + "\t" + getMin(str) + "\t" + getAvg(str) + "\t" + getMax(str) + "\t" + getPrecip(str) + "\n";
                    vector.addElement(out);
                }
            }
            in.close();
        } catch (IOException e) {
            System.out.println("Error in reading temperature file " + fileName);
            e.printStackTrace();
        }
    }

    public void findValidStations(int minDays) {
        String s, t = "";
        int count = 0;
        int valid = 0;
        int n = vector.size();
        Collections.sort(vector);
        for (int i = 0; i < n; i++) {
            s = vector.elementAt(i).toString().substring(0, 6);
            if (i < (n - 1)) {
                t = vector.elementAt(i + 1).toString().substring(0, 6);
                if (s.equals(t)) {
                    count++;
                } else {
                    if (count > minDays) {
                        valid++;
                    } else {
                    }
                    count = 0;
                }
            }
        }
        System.out.println("Valid:\t" + valid + "\tfinal size: " + vector.size());
    }

    public void showStations() {
        int n = vector.size();
        for (int i = 0; i < n; i++) {
            System.out.println(vector.get(i));
        }
        System.out.println("new size: " + n);
    }

    public void save(String zipFileName, int y) {
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
            out.setLevel(Deflater.DEFAULT_COMPRESSION);
            out.putNextEntry(new ZipEntry(Integer.toString(y) + ".txt"));
            int n = vector.size();
            String s = "";
            byte[] buf;
            for (int i = 0; i < n; i++) {
                s = vector.elementAt(i).toString() + "\n";
                recordSize = s.length();
                buf = s.getBytes();
                out.write(buf, 0, recordSize);
            }
            out.closeEntry();
            out.close();
        } catch (Exception e) {
            System.out.println("Error while zipping:/t" + zipFileName);
            e.printStackTrace();
        }
    }

    public void loadFromZip(String zipFileName) {
        uniqueStations.clear();
        vector.clear();
        String s = "";
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(zipFileName);
            Enumeration e = zipFile.entries();
            ZipEntry ze = (ZipEntry) e.nextElement();
            BufferedReader zipReader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(ze)));
            int i = 0;
            while (zipReader.ready()) {
                i++;
                s = zipReader.readLine();
                s.trim();
                vector.addElement(s);
                uniqueStations.add(getStation(s));
            }
            zipReader.close();
        } catch (Exception e) {
            System.out.println("Error while zipping:\t" + zipFileName);
            e.printStackTrace();
        }
    }

    public String[] getUniqueStations() {
        Object[] o = uniqueStations.toArray();
        String[] s = new String[o.length];
        Set<String> sorted = new TreeSet<String>(uniqueStations);
        Iterator i = sorted.iterator();
        int k = 0;
        while (i.hasNext()) {
            s[k] = i.next().toString();
            k++;
        }
        ;
        return s;
    }

    public void writeClimateData(String station, String file) {
        int size = vector.size();
        String s = "";
        String st = "";
        String[] t;
        String tmin, tmax;
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < size; i++) {
                s = vector.get(i).toString().trim();
                st = getStation(s);
                if (station.equalsIgnoreCase(st)) {
                    t = s.split("\t");
                    tmin = t[1];
                    tmax = t[3];
                    out.write(tmin + "\t" + tmax + "\n");
                }
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeIncompleteStations() {
        int size = vector.size();
        String s1, s2;
        String s3 = "";
        String s4 = "";
        int dd, d1, d2, mon1, mon2, day1, day2;
        Vector<String> r_this;
        Vector<String> r_next;
        Vector<String> rm_this;
        Vector<String> rm_next;
        int start_pos = 0;
        int end_pos = 0;
        boolean newStation = false;
        boolean newStation2 = false;
        System.out.println("initial size: " + size);
        for (int i = 0; i < size; i++) {
            end_pos++;
            s1 = vector.elementAt(i);
            r_this = getData(s1);
            if (i < (size - 1)) {
                s2 = vector.elementAt(i + 1);
            } else {
                s2 = vector.elementAt(start_pos);
            }
            r_next = getData(s2);
            mon1 = getMonth(r_this.elementAt(1));
            day1 = getDay(r_this.elementAt(1));
            newStation = !r_this.get(0).toString().equals(r_next.get(0).toString());
            if (newStation && mon1 == 12 && (day1 > 15)) {
                r_next = r_this;
                start_pos = i + 1;
                if (mon1 > 1 && day1 > 1) {
                    mon1 = 1;
                    day1 = 1;
                    r_next = getData(s2);
                }
            }
            mon2 = getMonth(r_next.elementAt(1));
            day2 = getDay(r_next.elementAt(1));
            d1 = CalendarUtils.calcJulianDays(mon1, day1);
            d2 = CalendarUtils.calcJulianDays(mon2, day2);
            dd = d2 - d1;
            if (dd > 14) {
                for (int r = start_pos; r < (size - 1); r++) {
                    s3 = vector.elementAt(r);
                    rm_this = getData(s3);
                    s4 = vector.elementAt(r + 1);
                    rm_next = getData(s4);
                    newStation2 = !rm_this.get(0).toString().equals(rm_next.get(0).toString());
                    if (newStation2) {
                        end_pos = r;
                        break;
                    }
                }
                System.out.println("Removing: " + start_pos + "\t" + end_pos);
                System.out.println("\t" + s1 + "\t" + s3);
                int r = 0;
                try {
                    for (r = vector.size(); r >= start_pos; r--) {
                        vector.remove(r);
                    }
                } catch (Exception e) {
                    System.out.println("Error while removing ... end_pos: " + end_pos + "\tstart_pos\t" + start_pos + "\tr\t" + r);
                    e.printStackTrace();
                }
                size = vector.size();
                i = start_pos;
                System.out.println("new size: " + size);
            }
        }
    }

    public void removeIncompleteStationData() {
        int size = vector.size();
        String s1, s2;
        Vector<String> r_this;
        Vector<String> r_next;
        Vector<String> t = new Vector<String>();
        Vector<String> completeStations = new Vector<String>();
        int start_pos = 0;
        int end_pos = 0;
        boolean newStation = false;
        System.out.println("initial size: " + size);
        for (int i = 0; i < size; i++) {
            s1 = vector.elementAt(i);
            r_this = getData(s1);
            if (i < (size - 1)) {
                s2 = vector.elementAt(i + 1);
            } else {
                s2 = vector.elementAt(start_pos);
            }
            r_next = getData(s2);
            newStation = !r_this.get(0).toString().equals(r_next.get(0).toString());
            if (!newStation) {
                t.add(s1);
                end_pos++;
            } else {
                if (t.size() < 365) {
                    for (int k = start_pos; k < end_pos; k++) {
                        System.out.println("Adding: \t" + k + "\t" + start_pos + "\t" + end_pos + "\t" + vector.get(k));
                        completeStations.add(vector.get(k));
                    }
                    System.out.println("Incomplete at:\t" + t.size() + "\tstart_pos\t" + start_pos + "\t" + vector.get(start_pos));
                    System.out.println("\t\t\tend_pos\t" + end_pos + "\t" + vector.get(end_pos));
                    start_pos++;
                    end_pos = start_pos;
                } else {
                    start_pos += t.size();
                }
                t.clear();
            }
        }
        vector.clear();
        vector.addAll(completeStations);
        completeStations.clear();
    }

    public void convertData() {
        int size = vector.size();
        String newrec = "";
        Vector r_this;
        String s;
        for (int i = 0; i < size; i++) {
            s = vector.elementAt(i);
            r_this = scanData(s);
            newrec = r_this.get(0) + "" + r_this.get(1) + "\t" + r_this.get(2) + "\t" + r_this.get(3) + "\t" + r_this.get(4) + "\t" + r_this.get(5);
            vector.setElementAt(newrec, i);
        }
    }

    public void interpolateMissingData() {
        String s;
        Vector<String> r_this = new Vector<String>();
        Vector<String> r_prev = new Vector<String>();
        Vector<String> r_next = new Vector<String>();
        int missing = 999;
        int idxTmin = 2;
        int idxRain = 5;
        double v_this, v_prev, v_next;
        int size = vector.size();
        String newrec = "";
        boolean newStation = true;
        int j = 0;
        for (int k = idxTmin; k <= idxRain; k++) {
            for (int i = 0; i < size; i++) {
                s = vector.elementAt(i);
                r_this = getData(s);
                v_this = Double.valueOf(r_this.get(k).toString());
                if (v_this > missing) {
                    r_prev = r_this;
                    r_next = r_this;
                    if (i > 0 && i < (size - 1)) {
                        r_next = getData(vector.elementAt(i + 1));
                        r_prev = getData(vector.elementAt(i - 1));
                    }
                    newStation = !r_this.get(0).toString().equals(r_next.get(0).toString());
                    v_prev = Double.valueOf(r_prev.get(k).toString());
                    v_next = Double.valueOf(r_next.get(k).toString());
                    int m = 0;
                    int lst = i;
                    for (j = i; j < (size - 1); j++) {
                        m++;
                        s = vector.elementAt(j);
                        r_next = getData(s);
                        v_next = Double.valueOf(r_next.get(k).toString());
                        if (v_next < missing | newStation) {
                            lst = j;
                            break;
                        }
                    }
                    double d = (v_next - v_prev) / m;
                    double newVal = v_prev;
                    Vector<String> r_trans;
                    m = 1;
                    for (j = i; i < size; j++) {
                        if (j == lst) break;
                        newVal = newVal + (m) * d;
                        s = String.format("%.1f", Double.valueOf(newVal));
                        r_trans = getData(vector.elementAt(j));
                        r_trans.set(k, s);
                        newrec = r_trans.get(0) + "" + r_trans.get(1) + "\t" + r_trans.get(2) + "\t" + r_trans.get(3) + "\t" + r_trans.get(4) + "\t" + r_trans.get(5);
                        vector.set(j, newrec);
                        m++;
                    }
                }
            }
            System.out.println();
        }
    }

    public void interpolateMissingDays() {
        int size = vector.size();
        String s, s1, s2;
        int dd, d1, d2, mon1, mon2, day1, day2;
        double[] tmin = new double[3];
        double[] tavg = new double[3];
        double[] tmax = new double[3];
        double[] rain = new double[3];
        Vector<String> r_this;
        Vector<String> r_next;
        boolean newStation = false;
        for (int i = 0; i < (size - 1); i++) {
            s1 = vector.elementAt(i);
            r_this = getData(s1);
            s2 = vector.elementAt(i + 1);
            r_next = getData(s2);
            newStation = !r_this.get(0).toString().equals(r_next.get(0).toString());
            if (newStation) {
                r_next = r_this;
            }
            mon1 = getMonth(r_this.elementAt(1));
            mon2 = getMonth(r_next.elementAt(1));
            day1 = getDay(r_this.elementAt(1));
            day2 = getDay(r_next.elementAt(1));
            d1 = CalendarUtils.calcJulianDays(mon1, day1);
            d2 = CalendarUtils.calcJulianDays(mon2, day2);
            dd = d2 - d1;
            if (dd > 1 & dd < 15) {
                tmin[0] = Double.valueOf(r_this.elementAt(2));
                tmin[1] = Double.valueOf(r_next.elementAt(2));
                tavg[0] = Double.valueOf(r_this.elementAt(3));
                tavg[1] = Double.valueOf(r_next.elementAt(3));
                tmax[0] = Double.valueOf(r_this.elementAt(4));
                tmax[1] = Double.valueOf(r_next.elementAt(4));
                rain[0] = Double.valueOf(r_this.elementAt(5));
                rain[1] = Double.valueOf(r_next.elementAt(5));
                tmin[2] = (tmin[1] - tmin[0]) / dd;
                tavg[2] = (tavg[1] - tavg[0]) / dd;
                tmax[2] = (tmax[1] - tmax[0]) / dd;
                rain[2] = (rain[1] - rain[0]) / dd;
                System.out.println(s1 + "\t" + r_this);
                System.out.println(dd - 1);
                double dsum = 0;
                String ssum = "";
                for (int m = 1; m < dd; m++) {
                    s = r_this.get(0) + Integer.toString(getYear(r_this.get(1))) + CalendarUtils.nextDate(d1 + m);
                    dsum = tmin[0] + m * tmin[2];
                    ssum = String.format("%.1f", Double.valueOf(dsum));
                    s += "\t" + ssum;
                    dsum = tavg[0] + m * tavg[2];
                    ssum = String.format("%.1f", Double.valueOf(dsum));
                    s += "\t" + ssum;
                    dsum = tmax[0] + m * tmax[2];
                    ssum = String.format("%.1f", Double.valueOf(dsum));
                    s += "\t" + ssum;
                    dsum = rain[0] + m * rain[2];
                    ssum = String.format("%.1f", Double.valueOf(dsum));
                    s += "\t" + ssum;
                    System.out.println(s);
                    newdata.add(s);
                }
                System.out.println(s2);
            } else if (dd > 14) {
                deadlist.add(r_this.elementAt(0));
            }
        }
        eliminateIncompleteStations();
        addNewData();
    }

    private void addNewData() {
        System.out.println("Eliminating ...");
        vector.addAll(newdata);
        System.out.println("Sorting ...");
        Collections.sort(vector);
    }

    private void eliminateIncompleteStations() {
        int size = vector.size();
        String station = "";
        Vector<String> r_this;
        for (int i = 0; i < size; i++) {
            r_this = getData(vector.elementAt(i));
            Iterator itr = deadlist.iterator();
            while (itr.hasNext()) {
                station = itr.next().toString();
                if (station.equals(r_this.get(0).trim())) {
                    vector.remove(i);
                    size = vector.size();
                }
            }
        }
    }

    public void showDeadlist() {
        System.out.println(deadlist + "\t" + deadlist.size());
        Iterator itr = deadlist.iterator();
        while (itr.hasNext()) {
            System.out.println(itr.next());
        }
        ;
    }

    public void showNewData() {
        int n = newdata.size();
        System.out.println(n);
    }

    private Vector<String> scanData(String str) {
        Vector<String> results = new Vector<String>();
        String[] raw = str.split("\t");
        double value = 0;
        String s = "";
        for (int i = 0; i < raw.length; i++) {
            if (i > 0 & i < 4) {
                value = UnitConverter.FtoC(Double.valueOf(raw[i]).doubleValue());
                s = String.format("%.1f", Double.valueOf(value));
            } else if (i == 4) {
                value = UnitConverter.ItoMm(Double.valueOf(raw[i]).doubleValue());
                s = String.format("%.1f", Double.valueOf(value));
            } else {
                s = raw[i];
            }
            if (i == 0) {
                s = raw[i].substring(0, 6);
                results.add(s);
                s = raw[i].substring(6, 14);
                results.add(s);
            } else {
                results.add(s);
            }
        }
        return results;
    }

    private Vector<String> getData(String str) {
        Vector<String> results = new Vector<String>();
        String[] raw = str.split("\t");
        String s = "";
        for (int i = 0; i < raw.length; i++) {
            s = raw[i].trim();
            if (i == 0) {
                s = raw[i].substring(0, 6);
                results.add(s);
                s = raw[i].substring(6, 14);
                results.add(s);
            } else {
                results.add(s);
            }
        }
        return results;
    }

    private String getAvg(String str) {
        return str.substring(16, 24);
    }

    private String getPrecip(String str) {
        return str.substring(112, 117);
    }

    private String getMin(String str) {
        return str.substring(104, 110);
    }

    private String getMax(String str) {
        return str.substring(96, 102);
    }

    private String getDate(String str) {
        return str.substring(8, 16);
    }

    private int getDay(String str) {
        return Integer.valueOf(str.substring(6, 8));
    }

    private int getMonth(String str) {
        return Integer.valueOf(str.substring(4, 6));
    }

    private int getYear(String str) {
        return Integer.valueOf(str.substring(0, 4));
    }

    private String getStation(String str) {
        String s = str;
        try {
            s = str.substring(0, 6);
        } catch (Exception e) {
            e.printStackTrace();
            s = "";
        }
        return s;
    }

    public void readStationData(String fileName) {
        String str = "";
        int i = 0;
        try {
            BufferedReader in = new BufferedReader(new FileReader(fileName));
            while ((str = in.readLine()) != null) {
                i++;
                str.trim();
                stations.put(getStationId(str), str);
            }
            in.close();
        } catch (IOException e) {
            System.out.println("Error in reading station file " + fileName);
            e.printStackTrace();
        }
    }

    public String getCountryId(String str) {
        String n = stations.get(str);
        String s = n.substring(34, 36);
        return s;
    }

    public String getName(String str) {
        String n = stations.get(str);
        String s = n.substring(14, 34);
        s.replace("['/&\\]", " ");
        s = "";
        return s;
    }

    public double getLon(String str) {
        String n = stations.get(str);
        String s = n.substring(43, 49);
        double d;
        try {
            d = Double.valueOf(s) / 100;
        } catch (Exception e) {
            d = -99999;
        }
        if (s.equals("-99999")) d = -99999;
        return d;
    }

    public int getElev(String str) {
        String n = stations.get(str);
        String s = n.substring(50, 55);
        int d = 0;
        try {
            d = Double.valueOf(s).intValue();
        } catch (Exception e) {
            d = 999999;
        }
        if (s.equals("-9999")) d = 999999;
        return d;
    }

    public double getLat(String str) {
        String n = stations.get(str);
        String s = "-9999";
        double d;
        try {
            s = n.substring(37, 42);
            d = Double.valueOf(s) / 100;
        } catch (Exception e) {
            d = -9999;
        }
        if (s.equals("-9999")) d = -99999;
        return d;
    }

    private String getStationId(String str) {
        String s = str.substring(0, 6);
        return s;
    }

    public void readSelectedStationData(String fileName) {
        String str = "";
        int i = 0;
        selectStations.clear();
        try {
            BufferedReader in = new BufferedReader(new FileReader(fileName));
            while ((str = in.readLine()) != null) {
                i++;
                str = str.trim().split("\t")[0];
                selectStations.add(getStationId(str));
            }
            in.close();
        } catch (IOException e) {
            System.out.println("Error in reading temperature file " + fileName);
            e.printStackTrace();
        }
    }

    public void showSelectedStation() {
        System.out.println(selectStations.size());
        System.out.println(selectStations);
    }

    public void filterSelectedStations() {
        System.out.println("initial size of dabase: " + vector.size());
        Vector<String> data = new Vector<String>();
        data.clear();
        if (vector.size() > 0) {
            int i = 0;
            String s;
            Vector r_this;
            for (i = 0; i < vector.size(); i++) {
                s = vector.elementAt(i);
                r_this = scanData(s);
                if (selectStations.contains(r_this.get(0))) {
                    System.out.println(i + "\tSelecting ..." + s);
                    data.add(s);
                } else {
                    System.out.println(i);
                }
            }
        }
        vector.clear();
        vector.addAll(data);
        System.out.println(data.size() + "\tfinal size of dabase: " + vector.size());
    }

    public String getStationIdByCoord(Coordinate coord) {
        String id = null;
        String lat;
        String lon;
        String lat_search;
        String lon_search;
        String[] s = new String[stations.size()];
        Iterator i = stations.keySet().iterator();
        int k = 0;
        while (i.hasNext()) {
            s[k] = i.next().toString();
            lat = String.format("%.0f", getLat(s[k]));
            ;
            lon = String.format("%.0f", getLon(s[k]));
            ;
            lat_search = String.format("%.0f", coord.y);
            lon_search = String.format("%.0f", coord.x);
            System.out.println(lat + "\t" + lon + "\t" + lat_search + "\t" + lon_search);
            id = s[k];
            if (lat.equals(lat_search) && (lon.equals(lon_search))) return id;
            k++;
        }
        ;
        return id;
    }

    public Vector filterDataByStation(String sid) {
        Vector<String> v = new Vector<String>();
        int n = vector.size();
        String dataline = "";
        for (int i = 0; i < n; i++) {
            dataline = vector.get(i);
            if (dataline.contains(sid)) {
                v.add(dataline);
            }
        }
        return v;
    }
}
