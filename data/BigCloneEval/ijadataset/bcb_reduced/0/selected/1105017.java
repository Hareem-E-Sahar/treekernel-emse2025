package org.das2.datum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.das2.datum.TimeUtil.TimeStruct;

/**
 * Orbits are a map of string identifiers to DatumRanges, typically used to enumerate the orbits a spacecraft makes.
 * For example, Cassini orbit "C" was from 2004-366T07:03 to 2005-032T03:27 and "33" was from  2006-318T23:34 to 2006-330T22:23.
 * There are two types of orbits: canonical, which have an identifier like "cassini" and can be used by the community, and user
 * which have identifiers like  http://das2.org/wiki/index.php/testorbits .  In either case, these refer to a file.
 * The canonical ones are stored on the das2 wiki at  http://das2.org/wiki/index.php/Orbits/&lt;id&gt;.  This file is a
 * three-column ascii file with the orbit id in either the first or last column.  Note any line not meeting this spec is ignored,
 * so that orbit files can contain additional documentation (and can sit within a wiki).
 *
 * @author jbf
 */
public class Orbits {

    private String sc;

    private LinkedHashMap<String, DatumRange> orbits;

    /**
     * the last orbit
     */
    private String last;

    private static HashMap<String, Orbits> missions = new HashMap();

    /**
     * read the orbits file.  The file may be on the wiki page http://das2.org/wiki/index.php/Orbits.&lt;SC%gt;,
     * or on the classpath in /orbits/&lt;SC%gt;.dat  The orbits file will be read by ignoring any line
     * that does not contain three non-space blobs, and either the first two or last two should parse as an
     * ISO8601 string.  The ISO8601 strings must start with 4-digit years, either
     * Note the input can then be html, with a pre section containing the orbits.
     *
     * @param sc
     * @return
     */
    private static LinkedHashMap<String, DatumRange> readOrbits(String sc) throws IOException {
        URL url;
        try {
            if (sc.contains(":")) {
                url = new URL(sc);
            } else {
                url = new URL("http://das2.org/wiki/index.php/Orbits/" + sc);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
        InputStream in;
        try {
            in = url.openConnection().getInputStream();
        } catch (IOException ex) {
            if (sc.contains(":")) {
                throw new IllegalArgumentException("I/O Exception prevents reading orbits from \"" + sc + "\"", ex);
            }
            url = Orbits.class.getResource("/orbits/" + sc + ".dat");
            if (url == null) {
                throw new IllegalArgumentException("unable to find orbits file for \"" + sc + "\"");
            }
            try {
                in = url.openConnection().getInputStream();
            } catch (IOException ex2) {
                throw new IllegalArgumentException("I/O Exception prevents reading orbits for \"" + sc + "\"", ex2);
            }
        }
        LinkedHashMap<String, DatumRange> result = new LinkedHashMap();
        BufferedReader rin = new BufferedReader(new InputStreamReader(in));
        String s = rin.readLine();
        int col = -1;
        while (s != null) {
            String[] ss = s.trim().split("\\s+");
            if (ss.length == 3 && (ss[1].startsWith("1") || ss[1].startsWith("2"))) {
                Datum d1;
                Datum d2;
                String s0;
                try {
                    if (col > -1) {
                        try {
                            d1 = TimeUtil.create(ss[col]);
                            d2 = TimeUtil.create(ss[col + 1]);
                            s0 = ss[col == 0 ? 2 : 0];
                        } catch (ParseException ex) {
                            s = rin.readLine();
                            continue;
                        }
                    } else {
                        if (ss[0].length() > 4) {
                            d1 = TimeUtil.create(ss[0]);
                            d2 = TimeUtil.create(ss[1]);
                        } else {
                            throw new ParseException("time is too short", 0);
                        }
                        s0 = ss[2];
                        col = 0;
                    }
                } catch (ParseException ex) {
                    try {
                        d1 = TimeUtil.create(ss[1]);
                        d2 = TimeUtil.create(ss[2]);
                        s0 = ss[0];
                        col = 1;
                    } catch (ParseException ex1) {
                        s = rin.readLine();
                        continue;
                    }
                }
                if (d1.gt(d2)) {
                    System.err.println("dropping invalid orbit: " + s);
                } else {
                    try {
                        result.put(s0, new DatumRange(d1, d2));
                    } catch (IllegalArgumentException ex) {
                        System.err.println(ex.getMessage() + ": " + s);
                    }
                }
            }
            s = rin.readLine();
        }
        rin.close();
        return result;
    }

    private Orbits(String sc, LinkedHashMap<String, DatumRange> orbits) {
        this.sc = sc;
        this.orbits = orbits;
    }

    /**
     * return the DatumRange for this orbit number.  Note this IS NOT an OrbitDatumRange.
     * @param orbit
     * @return
     * @throws ParseException
     */
    public DatumRange getDatumRange(String orbit) throws ParseException {
        DatumRange s = orbits.get(orbit);
        if (s == null) {
            throw new ParseException("unable to find orbit: " + orbit + " for " + sc, 0);
        }
        return s;
    }

    public String next(String orbit) {
        boolean next = false;
        for (String s : orbits.keySet()) {
            if (next) return s;
            if (s.equals(orbit)) {
                next = true;
            }
        }
        return null;
    }

    public String prev(String orbit) {
        String prev = null;
        for (String s : orbits.keySet()) {
            if (s.equals(orbit)) {
                return prev;
            }
            prev = s;
        }
        return null;
    }

    /**
     * return -1 if a is before b, 0 if they are equal, and 1 if a is after b.
     * @param a
     * @param b
     * @return
     */
    public int compare(String a, String b) {
        if (a.equals(b)) return 0;
        int ia = -1;
        int ib = -1;
        int i = 0;
        for (String s : orbits.keySet()) {
            if (s.equals(a)) ia = i;
            if (s.equals(b)) ib = i;
            i++;
        }
        if (ia == -1) throw new IllegalArgumentException("not an orbit id: " + a);
        if (ib == -1) throw new IllegalArgumentException("not an orbit id: " + b);
        return ia < ib ? -1 : (ia == ib ? 0 : 1);
    }

    /**
     * return the first orbit id, so that we can iterate through all
     * @return
     */
    public String first() {
        return orbits.keySet().iterator().next();
    }

    public String last() {
        return last;
    }

    public String getSpacecraft() {
        return sc;
    }

    public static synchronized Orbits getOrbitsFor(String sc) {
        Orbits orbits = missions.get(sc);
        if (orbits != null && orbits.orbits.size() > 0) {
            return orbits;
        } else {
            try {
                LinkedHashMap<String, DatumRange> lorbits = readOrbits(sc);
                orbits = new Orbits(sc, lorbits);
                Iterator<String> o = lorbits.keySet().iterator();
                String last = o.hasNext() ? o.next() : null;
                while (o.hasNext()) last = o.next();
                orbits.last = last;
                missions.put(sc, orbits);
            } catch (IOException ex) {
                throw new IllegalArgumentException("Unable to read orbits file for " + sc, ex);
            }
            return orbits;
        }
    }

    /**
     * allow orbits to be used in file names
     */
    public static class OrbitFieldHandler implements TimeParser.FieldHandler {

        Orbits o;

        OrbitDatumRange orbitDatumRange;

        char pad = '_';

        public String configure(Map<String, String> args) {
            String id = args.get("id");
            if (id == null) {
                id = "rbspa-pp";
            }
            o = getOrbitsFor(id);
            if (args.containsKey("pad")) {
                pad = args.get("pad").charAt(0);
            }
            return null;
        }

        public String getRegex() {
            return ".*";
        }

        public void parse(String fieldContent, TimeStruct startTime, TimeStruct timeWidth, Map<String, String> extra) throws ParseException {
            int i = 0;
            while (i < fieldContent.length() && fieldContent.charAt(i) == pad) i++;
            DatumRange dr = o.getDatumRange(fieldContent.substring(i).trim());
            TimeStruct tsmin = TimeUtil.toTimeStruct(dr.min());
            TimeStruct tsmax = TimeUtil.toTimeStruct(dr.max());
            startTime.year = tsmin.year;
            startTime.month = tsmin.month;
            startTime.day = tsmin.day;
            startTime.doy = tsmin.doy;
            startTime.hour = tsmin.hour;
            startTime.minute = tsmin.hour;
            startTime.seconds = tsmin.seconds;
            timeWidth.year = tsmax.year - tsmin.year;
            timeWidth.month = tsmax.month - tsmin.month;
            timeWidth.day = tsmax.day - tsmin.day;
            timeWidth.doy = tsmax.doy - tsmin.doy;
            timeWidth.hour = tsmax.hour - tsmin.hour;
            timeWidth.minute = tsmax.hour - tsmin.hour;
            timeWidth.seconds = tsmax.seconds - tsmin.seconds;
            timeWidth.micros = tsmax.micros - tsmin.micros;
            orbitDatumRange = new OrbitDatumRange(o.sc, fieldContent.substring(i).trim());
        }

        public OrbitDatumRange getOrbitRange() {
            return orbitDatumRange;
        }

        public String format(TimeStruct startTime, TimeStruct timeWidth, int length, Map<String, String> extra) throws IllegalArgumentException {
            DatumRange seek = new DatumRange(TimeUtil.toDatum(startTime), TimeUtil.toDatum(TimeUtil.add(startTime, timeWidth)));
            String result = null;
            for (String s : o.orbits.keySet()) {
                DatumRange dr;
                try {
                    dr = o.getDatumRange(s);
                } catch (ParseException ex) {
                    throw new RuntimeException(ex);
                }
                double nmin = DatumRangeUtil.normalize(dr, seek.min());
                if (seek.width().value() == 0) {
                    if (nmin >= -0.001 && nmin < 1.001) {
                        result = s;
                        break;
                    }
                } else {
                    double nmax = DatumRangeUtil.normalize(dr, seek.max());
                    if (nmin > -0.8 && nmin < 0.2 && (nmax - nmin < 0.01 || (nmax > 0.8 && nmax < 1.2))) {
                        result = s;
                        break;
                    }
                }
            }
            if (result == null) {
                throw new IllegalArgumentException("unable to find orbit for timerange");
            }
            if (length < 0) length = 5;
            int n = length - result.length();
            String ppad = "";
            for (int i = 0; i < n; i++) {
                ppad = ppad + pad;
            }
            return ppad + result;
        }
    }

    public static void main(String[] args) throws ParseException {
        Orbits o = getOrbitsFor("cassini");
        System.err.println(o.getDatumRange("120"));
        {
            TimeParser tp = TimeParser.create("$5(o,id=cassini)", "o", new OrbitFieldHandler());
            DatumRange dr = tp.parse("____C").getTimeRange();
            System.err.println(dr);
            System.err.println(tp.format(dr));
        }
        {
            TimeParser tp = TimeParser.create("$5(o,id=crres)", "o", new OrbitFieldHandler());
            DatumRange dr = tp.parse("__132").getTimeRange();
            System.err.println(dr);
            System.err.println(tp.format(dr));
        }
        {
            DatumRange dr = TimeParser.create("$(o,id=crres)").parse("599").getTimeRange();
            System.err.println(dr);
            DatumRange o600 = dr.next();
            System.err.println(o600);
            System.err.println("-generate a list--");
            List<DatumRange> drs = DatumRangeUtil.generateList(DatumRangeUtil.parseTimeRangeValid("1991-03-27 through 1991-03-29"), o600);
            for (DatumRange dr1 : drs) {
                System.err.println(dr1);
            }
        }
    }
}
