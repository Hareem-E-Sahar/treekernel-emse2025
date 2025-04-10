package org.das2.datum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * TT2000 converter that takes leap seconds into account from us2000.
 * @author jbf
 */
public class LeapSecondsConverter extends UnitsConverter {

    public static final int T1972_LEAP = 10;

    private static List<Long> leapSeconds;

    private static List<Double> withoutLeapSeconds;

    private static long lastUpdateMillis = 0;

    private static double us2000_st = -1;

    private static double us2000_en = -1;

    private static int us2000_c = -1;

    private static long tt2000_st = -1;

    private static long tt2000_en = -1;

    private static int tt2000_c = -1;

    private static void updateLeapSeconds() throws IOException, MalformedURLException, NumberFormatException {
        URL url = new URL("http://cdf.gsfc.nasa.gov/html/CDFLeapSeconds.txt");
        InputStream in;
        try {
            in = url.openStream();
        } catch (IOException ex) {
            url = LeapSecondsConverter.class.getResource("CDFLeapSeconds.txt");
            in = url.openStream();
            System.err.println("Using local copy of leap seconds!!!");
        }
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        String s = "";
        leapSeconds = new ArrayList(50);
        withoutLeapSeconds = new ArrayList(50);
        String lastLine = s;
        while (s != null) {
            s = r.readLine();
            if (s == null) {
                System.err.println("Last leap second read from " + url + " " + lastLine);
                continue;
            }
            if (s.startsWith(";")) {
                continue;
            }
            String[] ss = s.trim().split("\\s+", -2);
            if (ss[0].compareTo("1972") < 0) {
                continue;
            }
            int iyear = Integer.parseInt(ss[0]);
            int imonth = Integer.parseInt(ss[1]);
            int iday = Integer.parseInt(ss[2]);
            int ileap = (int) (Double.parseDouble(ss[3]));
            double us2000 = TimeUtil.createTimeDatum(iyear, imonth, iday, 0, 0, 0, 0).doubleValue(Units.us2000);
            leapSeconds.add(Long.valueOf(((long) us2000) * 1000L - 43200000000000L + (long) (ileap - 32) * 1000000000));
            withoutLeapSeconds.add(us2000);
        }
        leapSeconds.add(Long.MAX_VALUE);
        withoutLeapSeconds.add(Double.MAX_VALUE);
        lastUpdateMillis = System.currentTimeMillis();
    }

    boolean us2000ToTT2000;

    public LeapSecondsConverter(boolean us2000ToTT2000) {
        this.us2000ToTT2000 = us2000ToTT2000;
        if (us2000ToTT2000) {
            inverse = new LeapSecondsConverter(!us2000ToTT2000);
            inverse.inverse = this;
        }
    }

    /**
     * calculate the number of leap seconds in the tt2000, since 2000.
     * This is intended to replicate the table http://cdf.gsfc.nasa.gov/html/CDFLeapSeconds.txt
     * @param tt2000 the time in tt2000, which include the leap seconds.
     * @return
     * @throws Exception
     */
    public static synchronized int getLeapSecondCountForUs2000(double us2000) throws IOException {
        if (System.currentTimeMillis() - lastUpdateMillis > 86400000) {
            updateLeapSeconds();
        }
        if (us2000 < withoutLeapSeconds.get(0)) {
            return 0;
        }
        for (int i = 0; i < withoutLeapSeconds.size() - 1; i++) {
            if (withoutLeapSeconds.get(i) <= us2000 && (i == withoutLeapSeconds.size() - 1 || us2000 < withoutLeapSeconds.get(i + 1))) {
                us2000_st = withoutLeapSeconds.get(i);
                us2000_en = withoutLeapSeconds.get(i + 1);
                us2000_c = i + T1972_LEAP;
                return i + 10;
            }
        }
        throw new RuntimeException("code shouldn't get to this point: implementation error...");
    }

    /**
     * calculate the number of leap seconds in the tt2000, since 2000.
     * This is intended to replicate the table http://cdf.gsfc.nasa.gov/html/CDFLeapSeconds.txt
     * @param tt2000 the time in tt2000, which include the leap seconds.
     * @return
     * @throws Exception
     */
    public static synchronized int getLeapSecondCountForTT2000(long tt2000) throws IOException {
        if (System.currentTimeMillis() - lastUpdateMillis > 86400000) {
            updateLeapSeconds();
        }
        if (tt2000 < leapSeconds.get(0)) {
            return 0;
        }
        int i = 0;
        for (i = 0; i < leapSeconds.size() - 1; i++) {
            if (leapSeconds.get(i) <= tt2000 && (i == leapSeconds.size() - 1 || tt2000 < leapSeconds.get(i + 1))) {
                tt2000_st = leapSeconds.get(i);
                tt2000_en = leapSeconds.get(i + 1);
                tt2000_c = i + 10;
                return i + 10;
            }
        }
        return i + 9;
    }

    @Override
    public UnitsConverter getInverse() {
        return inverse;
    }

    @Override
    public synchronized double convert(double value) {
        try {
            int leapSeconds;
            if (this.us2000ToTT2000) {
                if (us2000_st <= value && value < us2000_en) {
                    leapSeconds = us2000_c;
                } else {
                    leapSeconds = getLeapSecondCountForUs2000(value);
                }
                return (value * 1000 - 43200000000000L) + (leapSeconds - 32 + 64.184) * 1000000000L;
            } else {
                if (tt2000_st <= value && value < tt2000_en) {
                    leapSeconds = tt2000_c;
                } else {
                    leapSeconds = getLeapSecondCountForTT2000((long) value);
                }
                return ((value - (leapSeconds - 32 + 64.184) * 1000000000L) + 43200000000000L) / 1000.;
            }
        } catch (IOException ex) {
            throw new RuntimeException("LeapSeconds file not available.  This should never happen since there is a leapSeconds file within code.", ex);
        }
    }
}
