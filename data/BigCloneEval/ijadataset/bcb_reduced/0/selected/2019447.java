package com.ibm.icu.util;

import java.util.Date;
import java.util.Locale;

/**
 * <code>JapaneseCalendar</code> is a subclass of <code>GregorianCalendar</code>
 * that numbers years and eras based on the reigns of the Japanese emperors.
 * The Japanese calendar is identical to the Gregorian calendar in all respects
 * except for the year and era.  The ascension of each  emperor to the throne
 * begins a new era, and the years of that era are numbered starting with the
 * year of ascension as year 1.
 * <p>
 * Note that in the year of an imperial ascension, there are two possible sets
 * of year and era values: that for the old era and for the new.  For example, a
 * new era began on January 7, 1989 AD.  Strictly speaking, the first six days
 * of that year were in the Showa era, e.g. "January 6, 64 Showa", while the rest
 * of the year was in the Heisei era, e.g. "January 7, 1 Heisei".  This class
 * handles this distinction correctly when computing dates.  However, in lenient
 * mode either form of date is acceptable as input. 
 * <p>
 * In modern times, eras have started on January 8, 1868 AD, Gregorian (Meiji),
 * July 30, 1912 (Taisho), December 25, 1926 (Showa), and January 7, 1989 (Heisei).  Constants
 * for these eras, suitable for use in the <code>ERA</code> field, are provided
 * in this class.  Note that the <em>number</em> used for each era is more or
 * less arbitrary.  Currently, the era starting in 1053 AD is era #0; however this
 * may change in the future as we add more historical data.  Use the predefined
 * constants rather than using actual, absolute numbers.
 * <p>
 *
 * @see com.ibm.icu.util.GregorianCalendar
 *
 * @author Laura Werner
 * @author Alan Liu
 * @draft ICU 2.4
 */
public class JapaneseCalendar extends GregorianCalendar {

    private static String copyright = "Copyright © 1998 IBM Corp. All Rights Reserved.";

    /**
     * Constructs a default <code>JapaneseCalendar</code> using the current time
     * in the default time zone with the default locale.
     * @draft ICU 2.4
     */
    public JapaneseCalendar() {
        super();
    }

    /**
     * Constructs a <code>JapaneseCalendar</code> based on the current time
     * in the given time zone with the default locale.
     * @param zone the given time zone.
     * @draft ICU 2.4
     */
    public JapaneseCalendar(TimeZone zone) {
        super(zone);
    }

    /**
     * Constructs a <code>JapaneseCalendar</code> based on the current time
     * in the default time zone with the given locale.
     * @param aLocale the given locale.
     * @draft ICU 2.4
     */
    public JapaneseCalendar(Locale aLocale) {
        super(aLocale);
    }

    /**
     * Constructs a <code>JapaneseCalendar</code> based on the current time
     * in the given time zone with the given locale.
     *
     * @param zone the given time zone.
     *
     * @param aLocale the given locale.
     * @draft ICU 2.4
     */
    public JapaneseCalendar(TimeZone zone, Locale aLocale) {
        super(zone, aLocale);
    }

    /**
     * Constructs a <code>JapaneseCalendar</code> with the given date set
     * in the default time zone with the default locale.
     *
     * @param date      The date to which the new calendar is set.
     * @draft ICU 2.4
     */
    public JapaneseCalendar(Date date) {
        this();
        setTime(date);
    }

    /**
     * Constructs a <code>JapaneseCalendar</code> with the given date set
     * in the default time zone with the default locale.
     *
     * @param era       The imperial era used to set the calendar's {@link #ERA ERA} field.
     *                  Eras are numbered starting with the Tenki era, which
     *                  began in 1053 AD Gregorian, as era zero.  Recent
     *                  eras can be specified using the constants
     *                  {@link #MEIJI} (which started in 1868 AD),
     *                  {@link #TAISHO} (1912 AD),
     *                  {@link #SHOWA} (1926 AD), and
     *                  {@link #HEISEI} (1989 AD).
     *
     * @param year      The value used to set the calendar's {@link #YEAR YEAR} field,
     *                  in terms of the era.
     *
     * @param month     The value used to set the calendar's {@link #MONTH MONTH} field.
     *                  The value is 0-based. e.g., 0 for January.
     *
     * @param date      The value used to set the calendar's DATE field.
     * @draft ICU 2.4
     */
    public JapaneseCalendar(int era, int year, int month, int date) {
        super(year, month, date);
        set(ERA, era);
    }

    /**
     * Constructs a <code>JapaneseCalendar</code> with the given date set
     * in the default time zone with the default locale.
     *
     * @param year      The value used to set the calendar's {@link #YEAR YEAR} field,
     *                  in the era Heisei, the most current at the time this
     *                  class was last updated.
     *
     * @param month     The value used to set the calendar's {@link #MONTH MONTH} field.
     *                  The value is 0-based. e.g., 0 for January.
     *
     * @param date      The value used to set the calendar's {@link #DATE DATE} field.
     * @draft ICU 2.4
     */
    public JapaneseCalendar(int year, int month, int date) {
        super(year, month, date);
        set(ERA, CURRENT_ERA);
    }

    /**
     * Constructs a <code>JapaneseCalendar</code> with the given date
     * and time set for the default time zone with the default locale.
     *
     * @param year      The value used to set the calendar's {@link #YEAR YEAR} time field,
     *                  in the era Heisei, the most current at the time of this
     *                  writing.
     *
     * @param month     The value used to set the calendar's {@link #MONTH MONTH} time field.
     *                  The value is 0-based. e.g., 0 for January.
     *
     * @param date      The value used to set the calendar's {@link #DATE DATE} time field.
     *
     * @param hour      The value used to set the calendar's {@link #HOUR_OF_DAY HOUR_OF_DAY} time field.
     *
     * @param minute    The value used to set the calendar's {@link #MINUTE MINUTE} time field.
     *
     * @param second    The value used to set the calendar's {@link #SECOND SECOND} time field.
     * @draft ICU 2.4
     */
    public JapaneseCalendar(int year, int month, int date, int hour, int minute, int second) {
        super(year, month, date, hour, minute, second);
        set(ERA, CURRENT_ERA);
    }

    /**
     * @draft ICU 2.4
     */
    protected int handleGetExtendedYear() {
        int year;
        if (newerField(EXTENDED_YEAR, YEAR) == EXTENDED_YEAR && newerField(EXTENDED_YEAR, ERA) == EXTENDED_YEAR) {
            year = internalGet(EXTENDED_YEAR, 1);
        } else {
            year = internalGet(YEAR) + ERAS[internalGet(ERA) * 3] - 1;
        }
        return year;
    }

    /**
     * @draft ICU 2.4
     */
    protected void handleComputeFields(int julianDay) {
        super.handleComputeFields(julianDay);
        int year = internalGet(EXTENDED_YEAR);
        int low = 0;
        if (year > ERAS[ERAS.length - 3]) {
            low = CURRENT_ERA;
        } else {
            int high = ERAS.length / 3;
            while (low < high - 1) {
                int i = (low + high) / 2;
                int diff = year - ERAS[i * 3];
                if (diff == 0) {
                    diff = internalGet(MONTH) - (ERAS[i * 3 + 1] - 1);
                    if (diff == 0) {
                        diff = internalGet(DAY_OF_MONTH) - ERAS[i * 3 + 2];
                    }
                }
                if (diff >= 0) {
                    low = i;
                } else {
                    high = i;
                }
            }
        }
        internalSet(ERA, low);
        internalSet(YEAR, year - ERAS[low * 3] + 1);
    }

    private static final int[] ERAS = { 645, 6, 19, 650, 2, 15, 672, 1, 1, 686, 7, 20, 701, 3, 21, 704, 5, 10, 708, 1, 11, 715, 9, 2, 717, 11, 17, 724, 2, 4, 729, 8, 5, 749, 4, 14, 749, 7, 2, 757, 8, 18, 765, 1, 7, 767, 8, 16, 770, 10, 1, 781, 1, 1, 782, 8, 19, 806, 5, 18, 810, 9, 19, 824, 1, 5, 834, 1, 3, 848, 6, 13, 851, 4, 28, 854, 11, 30, 857, 2, 21, 859, 4, 15, 877, 4, 16, 885, 2, 21, 889, 4, 27, 898, 4, 26, 901, 7, 15, 923, 4, 11, 931, 4, 26, 938, 5, 22, 947, 4, 22, 957, 10, 27, 961, 2, 16, 964, 7, 10, 968, 8, 13, 970, 3, 25, 973, 12, 20, 976, 7, 13, 978, 11, 29, 983, 4, 15, 985, 4, 27, 987, 4, 5, 989, 8, 8, 990, 11, 7, 995, 2, 22, 999, 1, 13, 1004, 7, 20, 1012, 12, 25, 1017, 4, 23, 1021, 2, 2, 1024, 7, 13, 1028, 7, 25, 1037, 4, 21, 1040, 11, 10, 1044, 11, 24, 1046, 4, 14, 1053, 1, 11, 1058, 8, 29, 1065, 8, 2, 1069, 4, 13, 1074, 8, 23, 1077, 11, 17, 1081, 2, 10, 1084, 2, 7, 1087, 4, 7, 1094, 12, 15, 1096, 12, 17, 1097, 11, 21, 1099, 8, 28, 1104, 2, 10, 1106, 4, 9, 1108, 8, 3, 1110, 7, 13, 1113, 7, 13, 1118, 4, 3, 1120, 4, 10, 1124, 4, 3, 1126, 1, 22, 1131, 1, 29, 1132, 8, 11, 1135, 4, 27, 1141, 7, 10, 1142, 4, 28, 1144, 2, 23, 1145, 7, 22, 1151, 1, 26, 1154, 10, 28, 1156, 4, 27, 1159, 4, 20, 1160, 1, 10, 1161, 9, 4, 1163, 3, 29, 1165, 6, 5, 1166, 8, 27, 1169, 4, 8, 1171, 4, 21, 1175, 7, 28, 1177, 8, 4, 1181, 7, 14, 1182, 5, 27, 1184, 4, 16, 1185, 8, 14, 1190, 4, 11, 1199, 4, 27, 1201, 2, 13, 1204, 2, 20, 1206, 4, 27, 1207, 10, 25, 1211, 3, 9, 1213, 12, 6, 1219, 4, 12, 1222, 4, 13, 1224, 11, 20, 1225, 4, 20, 1227, 12, 10, 1229, 3, 5, 1232, 4, 2, 1233, 4, 15, 1234, 11, 5, 1235, 9, 19, 1238, 11, 23, 1239, 2, 7, 1240, 7, 16, 1243, 2, 26, 1247, 2, 28, 1249, 3, 18, 1256, 10, 5, 1257, 3, 14, 1259, 3, 26, 1260, 4, 13, 1261, 2, 20, 1264, 2, 28, 1275, 4, 25, 1278, 2, 29, 1288, 4, 28, 1293, 8, 55, 1299, 4, 25, 1302, 11, 21, 1303, 8, 5, 1306, 12, 14, 1308, 10, 9, 1311, 4, 28, 1312, 3, 20, 1317, 2, 3, 1319, 4, 28, 1321, 2, 23, 1324, 12, 9, 1326, 4, 26, 1329, 8, 29, 1331, 8, 9, 1334, 1, 29, 1336, 2, 29, 1340, 4, 28, 1346, 12, 8, 1370, 7, 24, 1372, 4, 1, 1375, 5, 27, 1381, 2, 10, 1384, 4, 28, 1384, 2, 27, 1379, 3, 22, 1387, 8, 23, 1389, 2, 9, 1390, 3, 26, 1394, 7, 5, 1428, 4, 27, 1429, 9, 5, 1441, 2, 17, 1444, 2, 5, 1449, 7, 28, 1452, 7, 25, 1455, 7, 25, 1457, 9, 28, 1460, 12, 21, 1466, 2, 28, 1467, 3, 3, 1469, 4, 28, 1487, 7, 29, 1489, 8, 21, 1492, 7, 19, 1501, 2, 29, 1504, 2, 30, 1521, 8, 23, 1528, 8, 20, 1532, 7, 29, 1555, 10, 23, 1558, 2, 28, 1570, 4, 23, 1573, 7, 28, 1592, 12, 8, 1596, 10, 27, 1615, 7, 13, 1624, 2, 30, 1644, 12, 16, 1648, 2, 15, 1652, 9, 18, 1655, 4, 13, 1658, 7, 23, 1661, 4, 25, 1673, 9, 21, 1681, 9, 29, 1684, 2, 21, 1688, 9, 30, 1704, 3, 13, 1711, 4, 25, 1716, 6, 22, 1736, 4, 28, 1741, 2, 27, 1744, 2, 21, 1748, 7, 12, 1751, 10, 27, 1764, 6, 2, 1772, 11, 16, 1781, 4, 2, 1789, 1, 25, 1801, 2, 5, 1804, 2, 11, 1818, 4, 22, 1830, 12, 10, 1844, 12, 2, 1848, 2, 28, 1854, 11, 27, 1860, 3, 18, 1861, 2, 19, 1864, 2, 20, 1865, 4, 7, 1868, 9, 8, 1912, 7, 30, 1926, 12, 25, 1989, 1, 8 };

    /**
     * @draft ICU 2.4
     */
    public static final int CURRENT_ERA = (ERAS.length / 3) - 1;

    /** 
     * Constant for the era starting on Sept. 8, 1868 AD.
     * @draft ICU 2.4 
     */
    public static final int MEIJI = CURRENT_ERA - 3;

    /** 
     * Constant for the era starting on July 30, 1912 AD. 
     * @draft ICU 2.4 
     */
    public static final int TAISHO = CURRENT_ERA - 2;

    /** 
     * Constant for the era starting on Dec. 25, 1926 AD. 
     * @draft ICU 2.4 
     */
    public static final int SHOWA = CURRENT_ERA - 1;

    /** 
     * Constant for the era starting on Jan. 7, 1989 AD. 
     * @draft ICU 2.4 
     */
    public static final int HEISEI = CURRENT_ERA;

    /**
     * Partial limits table for limits that differ from GregorianCalendar's.
     * The YEAR max limits are filled in the first time they are needed.
     */
    private static int LIMITS[][] = { { 0, 0, CURRENT_ERA, CURRENT_ERA }, { 1, 1, 0, 0 } };

    private static boolean YEAR_LIMIT_KNOWN = false;

    /**
     * Override GregorianCalendar.  We should really handle YEAR_WOY and
     * EXTENDED_YEAR here too to implement the 1..5000000 range, but it's
     * not critical.
     * @draft ICU 2.4
     */
    protected int handleGetLimit(int field, int limitType) {
        switch(field) {
            case ERA:
                return LIMITS[field][limitType];
            case YEAR:
                if (!YEAR_LIMIT_KNOWN) {
                    int min = ERAS[3] - ERAS[0];
                    int max = min;
                    for (int i = 6; i < ERAS.length; i += 3) {
                        int d = ERAS[i] - ERAS[i - 3];
                        if (d < min) {
                            min = d;
                        } else if (d > max) {
                            max = d;
                        }
                    }
                    LIMITS[field][LEAST_MAXIMUM] = min;
                    LIMITS[field][MAXIMUM] = max;
                }
                return LIMITS[field][limitType];
            default:
                return super.handleGetLimit(field, limitType);
        }
    }
}
