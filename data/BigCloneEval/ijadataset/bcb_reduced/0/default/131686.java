import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.xerces.parsers.SAXParser;
import org.doomdark.uuid.UUIDGenerator;
import org.xaware.shared.util.logging.XAwareLogger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XAFunctoids {

    /** Logger instance for the class. */
    private static XAwareLogger lf = XAwareLogger.getXAwareLogger("XAFunctoids");

    /** Constant indicating the class name. */
    private static final String className = "XAFunctoids";

    /** variable for providing a unique Id. */
    private static long uniqueId_Count = 1001;

    /** variable for providing a sequence number. */
    private static long sequence_Number = 0;

    private static final int SIZE = 95;

    private static final int ASCII[] = { 0x0020, 0x0021, 0x0022, 0x0023, 0x0024, 0x0025, 0x0026, 0x0027, 0x0028, 0x0029, 0x002a, 0x002b, 0x002c, 0x002d, 0x002e, 0x002f, 0x0030, 0x0031, 0x0032, 0x0033, 0x0034, 0x0035, 0x0036, 0x0037, 0x0038, 0x0039, 0x003a, 0x003b, 0x003c, 0x003d, 0x003e, 0x003f, 0x0040, 0x0041, 0x0042, 0x0043, 0x0044, 0x0045, 0x0046, 0x0047, 0x0048, 0x0049, 0x004a, 0x004b, 0x004c, 0x004d, 0x004e, 0x004f, 0x0050, 0x0051, 0x0052, 0x0053, 0x0054, 0x0055, 0x0056, 0x0057, 0x0058, 0x0059, 0x005a, 0x005b, 0x005c, 0x005d, 0x005e, 0x005f, 0x0060, 0x0061, 0x0062, 0x0063, 0x0064, 0x0065, 0x0066, 0x0067, 0x0068, 0x0069, 0x006a, 0x006b, 0x006c, 0x006d, 0x006e, 0x006f, 0x0070, 0x0071, 0x0072, 0x0073, 0x0074, 0x0075, 0x0076, 0x0077, 0x0078, 0x0079, 0x007a, 0x007b, 0x007c, 0x007d, 0x007e };

    private static final int EBCDIC[] = { 0x0040, 0x005a, 0x007f, 0x007b, 0x005b, 0x006c, 0x0050, 0x007d, 0x004d, 0x005d, 0x005c, 0x004e, 0x006b, 0x0060, 0x004b, 0x0061, 0x00f0, 0x00f1, 0x00f2, 0x00f3, 0x00f4, 0x00f5, 0x00f6, 0x00f7, 0x00f8, 0x00f9, 0x007a, 0x005e, 0x004c, 0x007e, 0x006e, 0x006f, 0x007c, 0x00c1, 0x00c2, 0x00c3, 0x00c4, 0x00c5, 0x00c6, 0x00c7, 0x00c8, 0x00c9, 0x00d1, 0x00d2, 0x00d3, 0x00d4, 0x00d5, 0x00d6, 0x00d7, 0x00d8, 0x00d9, 0x00e2, 0x00e3, 0x00e4, 0x00e5, 0x00e6, 0x00e7, 0x00e8, 0x00e9, 0x00ad, 0x00e0, 0x00bd, 0x005f, 0x006d, 0x0079, 0x0081, 0x0082, 0x0083, 0x0084, 0x0085, 0x0086, 0x0087, 0x0088, 0x0089, 0x0091, 0x0092, 0x0093, 0x0094, 0x0095, 0x0096, 0x0097, 0x0098, 0x0099, 0x00a2, 0x00a3, 0x00a4, 0x00a5, 0x00a6, 0x00a7, 0x00a8, 0x00a9, 0x00c0, 0x006a, 0x00d0, 0x00a1 };

    public XAFunctoids() {
    }

    public static String appendStrings(String s, String s1) {
        return s + s1;
    }

    public static String substring(String s, String beginIndex) {
        return s.substring(new Integer(beginIndex).intValue());
    }

    public static String substring(String s, String beginIndex, String endIndex) {
        return s.substring(new Integer(beginIndex).intValue(), new Integer(endIndex).intValue());
    }

    public static String insertChar(String inputStr, String index, String insertChar) {
        int position = (new Integer(index)).intValue();
        int inputLen = inputStr.length();
        if (position < 0 || position > inputLen - 1) return (index + " not within input string's length !");
        if (insertChar.length() > 1) return ("Only 1 char may be inserted " + insertChar);
        String returnStr = inputStr.substring(0, position) + insertChar + inputStr.substring(position);
        return returnStr;
    }

    public static String upper(String s) {
        return (((s == null)) ? "" : s.toUpperCase());
    }

    public static String lower(String s) {
        return (((s == null)) ? "" : s.toLowerCase());
    }

    public static String length(String s) {
        return (((s == null)) ? "0" : new Integer(s.length()).toString());
    }

    public static String trim(String s) {
        return (((s == null)) ? "" : s.trim());
    }

    public static String split(String sSrc, String matchStr, String itemN) {
        int itemNum = 0;
        try {
            itemNum = Integer.parseInt(itemN);
        } catch (Exception e) {
            return "";
        }
        StringTokenizer tokens = new StringTokenizer(sSrc, matchStr, false);
        int cnt = 0;
        while (tokens.hasMoreTokens()) {
            String sToken = tokens.nextToken();
            cnt++;
            if (cnt == itemNum) return sToken;
        }
        return "";
    }

    /**
     * Replace character values specified by the first character of the first two params
     * @param oldChar - String
     * @param newChar - String
     * @param src - String
     * @return String
     */
    public static String replaceAll(final String oldChar, final String newChar, final String src) {
        String result = src;
        if (oldChar.length() > 0) {
            char oldInput = oldChar.charAt(0);
            if (newChar.length() > 0) {
                char newInput = newChar.charAt(0);
                result = src.replace(oldInput, newInput);
            } else {
                result = src.replaceAll(oldChar, newChar);
            }
        }
        return result;
    }

    public static String stringToHex(String s) {
        if ((s == null) || !(s.length() > 0)) return "";
        byte[] b = s.getBytes();
        StringBuffer sb = new StringBuffer(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(hexChar[(b[i] & 0xf0) >>> 4]);
            sb.append(hexChar[b[i] & 0x0f]);
        }
        return sb.toString();
    }

    static char[] hexChar = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    /**
     * Convert a hex string to a string.Permits upper or lower case hex.
     * 
     * @param s
     *            String must have even number of characters. and be formed only of digits 0-9 A-F or a-f. No spaces,
     *            minus or plus signs.
     * @return corresponding string. String s = java.net.URLDecoder.decode( string, "UTF-8" ) ;
     */
    public static String hexToString(String s) {
        int stringLength = s.length();
        lf.finest("stringLength: " + new Integer(stringLength).toString(), "XAFunctoids", "hexToString");
        if ((stringLength & 0x1) != 0) {
            throw new IllegalArgumentException("fromHexString requires an even number of hex characters");
        }
        byte[] b = new byte[stringLength / 2];
        for (int i = 0, j = 0; i < stringLength; i += 2, j++) {
            int high = charToNibble(s.charAt(i));
            int low = charToNibble(s.charAt(i + 1));
            b[j] = (byte) ((high << 4) | low);
            lf.finest("i: " + i + " j: " + j + " s2[i]:" + s.charAt(i) + " s2[i + 1]: " + s.charAt(i + 1) + " b[j]: " + b[j], "XAFunctoids", "hexToString");
        }
        System.out.println("byte []: " + b);
        return b.toString().toCharArray().toString();
    }

    /**
     * convert a single char to corresponding nibble.
     * 
     * @param c
     *            char to convert. must be 0-9 a-f A-F, no spaces, plus or minus signs
     * @return corresponding integer
     */
    private static int charToNibble(char c) {
        if ('0' <= c && c <= '9') {
            return c - '0';
        } else if ('a' <= c && c <= 'f') {
            return c - 'a' + 0xa;
        } else if ('A' <= c && c <= 'F') {
            return c - 'A' + 0xa;
        } else {
            throw new IllegalArgumentException("Invalid hex character: " + c);
        }
    }

    public static String date() {
        java.sql.Timestamp today = new java.sql.Timestamp(System.currentTimeMillis());
        return (today.toString().substring(0, 10));
    }

    public static String time() {
        java.sql.Timestamp today = new java.sql.Timestamp(System.currentTimeMillis());
        return (today.toString().substring(11, 19));
    }

    public static String year(String isoDate) {
        return (((isoDate == null) || !(isoDate.length() > 0)) ? "" : isoDate.substring(0, 4));
    }

    public static String month(String isoDate) {
        return (((isoDate == null) || !(isoDate.length() > 0)) ? "" : isoDate.substring(4, 6));
    }

    public static String day(String isoDate) {
        return (((isoDate == null) || !(isoDate.length() > 0)) ? "" : isoDate.substring(6, 8));
    }

    public static String xawareDatesToIso(String xawareDate) {
        return (((xawareDate == null) || !(xawareDate.length() > 0) || xawareDate.startsWith("%")) ? "" : xawareDate.substring(0, 4) + xawareDate.substring(5, 7) + xawareDate.substring(8, 10) + "000000");
    }

    public static String xawareDateTimesToIsoTime(String xawareDateTime) {
        if ((xawareDateTime == null) || !(xawareDateTime.length() > 0) || xawareDateTime.startsWith("%")) return "";
        String HH = "00";
        String MM = "00";
        String ss = "00";
        if ((xawareDateTime.indexOf(":")) == 2) {
            HH = xawareDateTime.substring(0, 2);
            MM = xawareDateTime.substring(3, 5);
            ss = xawareDateTime.substring(6, 8);
            return HH + MM + ss;
        } else {
            int i2 = xawareDateTime.length();
            if (i2 >= 19) {
                HH = xawareDateTime.substring(11, 13);
                MM = xawareDateTime.substring(14, 16);
                ss = xawareDateTime.substring(17, 19);
            }
            return HH + MM + ss;
        }
    }

    public static String xawareDateTimesToIso(String xawareDateTime) {
        if ((xawareDateTime == null) || !(xawareDateTime.length() > 0) || xawareDateTime.startsWith("%")) return "";
        String yy = "00";
        String mm = "00";
        String dd = "00";
        String HH = "00";
        String MM = "00";
        String ss = "00";
        if ((xawareDateTime.indexOf(":")) == 2) {
            HH = xawareDateTime.substring(0, 2);
            MM = xawareDateTime.substring(3, 5);
            ss = xawareDateTime.substring(6, 8);
            return HH + MM + ss;
        } else {
            yy = xawareDateTime.substring(0, 4);
            mm = xawareDateTime.substring(5, 7);
            dd = xawareDateTime.substring(8, 10);
            int i2 = xawareDateTime.length();
            if (i2 >= 19) {
                HH = xawareDateTime.substring(11, 13);
                MM = xawareDateTime.substring(14, 16);
                ss = xawareDateTime.substring(17, 19);
            }
            return yy + mm + dd + HH + MM + ss;
        }
    }

    public static String isoToOracleTimeStamp(String isoDate) {
        if ((isoDate == null) || !(isoDate.length() > 0) || isoDate.startsWith("%")) return "";
        int monthNo = new Integer(isoDate.substring(4, 6)).intValue();
        String monthName = monthNoToName(monthNo);
        String amPm = "AM";
        String HH = isoDate.substring(8, 10);
        int hour = new Integer(HH).intValue();
        if (hour == 0) {
            HH = "12";
        }
        if (hour > 12) {
            hour -= 12;
            amPm = "PM";
            HH = new Integer(hour).toString();
        }
        return isoDate.substring(6, 8) + "-" + monthName + "-" + isoDate.substring(0, 4) + " " + HH + ":" + isoDate.substring(10, 12) + ":" + isoDate.substring(12) + ".0" + amPm;
    }

    public static String isoToOracleDate(String isoDate) {
        if ((isoDate == null) || !(isoDate.length() > 0) || isoDate.startsWith("%")) return "";
        int monthNo = new Integer(isoDate.substring(4, 6)).intValue();
        String monthName = monthNoToName(monthNo);
        return isoDate.substring(6, 8) + "-" + monthName + "-" + isoDate.substring(0, 4);
    }

    public static String oracleToIso(String oraDate) {
        if ((oraDate == null) || !(oraDate.length() > 0) || oraDate.startsWith("%")) return "";
        String monthName = oraDate.substring(3, 6);
        String monthNo = monthNameToNo(monthName);
        return oraDate.substring(7) + monthNo + oraDate.substring(0, 2) + "000000";
    }

    public static String isoToDb2(String isoDate) {
        return (((isoDate == null) || !(isoDate.length() > 0) || isoDate.startsWith("%")) ? "" : isoDate.substring(4, 6) + "/" + isoDate.substring(6, 8) + "/" + isoDate.substring(0, 4));
    }

    public static String isoToDb2Time(String isoDate) {
        return (((isoDate == null) || !(isoDate.length() > 0) || isoDate.startsWith("%")) ? "" : isoDate.substring(8, 10) + ":" + isoDate.substring(10, 12) + ":" + isoDate.substring(12));
    }

    public static String db2ToIso(String db2Date) {
        return (((db2Date == null) || !(db2Date.length() > 0) || db2Date.startsWith("%")) ? "" : db2Date.substring(6) + db2Date.substring(0, 2) + db2Date.substring(3, 5) + "000000");
    }

    public static String isoToSqlServer(String isoDate) {
        return (((isoDate == null) || !(isoDate.length() > 0) || isoDate.startsWith("%")) ? "" : isoDate.substring(0, 4) + "-" + isoDate.substring(4, 6) + "-" + isoDate.substring(6, 8) + " " + isoDate.substring(8, 10) + ":" + isoDate.substring(10, 12) + ":" + isoDate.substring(12, 14));
    }

    public static String sqlServerToIso(String sqlServerDate) {
        return (((sqlServerDate == null) || !(sqlServerDate.length() > 0) || sqlServerDate.startsWith("%")) ? "" : sqlServerDate.substring(1, 5) + sqlServerDate.substring(6, 8) + sqlServerDate.substring(9, 11) + "000000");
    }

    public static String formatGivenDate(String inputFormat, String inputDate, String outputFormat) {
        if ((inputDate == null) || !(inputDate.length() > 0) || inputDate.startsWith("%")) return "";
        SimpleDateFormat formatter = new SimpleDateFormat(inputFormat);
        ParsePosition pos = new ParsePosition(0);
        Date currentTime_2 = null;
        try {
            currentTime_2 = formatter.parse(inputDate, pos);
        } catch (Exception ex) {
            lf.warning(inputDate + " format does not match " + inputFormat, "XAFunctoids", "formatGivenDate");
            return (inputDate + " format does not match " + inputFormat);
        }
        SimpleDateFormat formatter2 = null;
        try {
            formatter2 = new SimpleDateFormat(outputFormat);
        } catch (Exception ex1) {
            lf.warning(outputFormat + " does not confirm to date specifications ", "XAFunctoids", "formatGivenDate");
            return (outputFormat + " does not confirm to date specifications ");
        }
        String dateString = null;
        try {
            dateString = formatter2.format(currentTime_2);
        } catch (Exception ex2) {
            lf.severe(ex2.getMessage() + " XAFunctoids" + " formatGivenDate");
        }
        return dateString;
    }

    public static String DateTimePerGivenFormat(String DateAndOrTime, String outputFormat) {
        if ((DateAndOrTime == null) || !(DateAndOrTime.length() > 0) || DateAndOrTime.startsWith("%")) return "";
        String desiredDate = null;
        String inputFormat = null;
        java.sql.Timestamp today = new java.sql.Timestamp(System.currentTimeMillis());
        if (DateAndOrTime.equals("Date")) {
            desiredDate = today.toString().substring(0, 10);
            inputFormat = "yyyy'-'MM'-'dd";
        } else if (DateAndOrTime.equals("Time")) {
            desiredDate = today.toString().substring(11, 19);
            inputFormat = "H:mm:ss";
        } else if (DateAndOrTime.equals("Timestamp")) {
            desiredDate = today.toString();
            inputFormat = "yyyy'-'MM'-'dd' 'H:mm:ss.SSS";
        }
        SimpleDateFormat formatter = new SimpleDateFormat(inputFormat);
        ParsePosition pos = new ParsePosition(0);
        Date currentTime_2 = null;
        try {
            currentTime_2 = formatter.parse(desiredDate, pos);
        } catch (Exception ex) {
            lf.warning(desiredDate + " format does not match " + inputFormat, "XAFunctoids", "DateTimePerGivenFormat");
            return (desiredDate + " format does not match " + inputFormat);
        }
        SimpleDateFormat formatter2 = null;
        try {
            formatter2 = new SimpleDateFormat(outputFormat);
        } catch (Exception ex1) {
            lf.warning(outputFormat + " does not confirm to date specifications ", "XAFunctoids", "DateTimePerGivenFormat");
            return (outputFormat + " does not confirm to date specifications ");
        }
        String dateString = null;
        try {
            dateString = formatter2.format(currentTime_2);
        } catch (Exception ex2) {
            lf.warning(ex2.getMessage() + " XAFunctoids" + " DateTimePerGivenFormat");
        }
        return dateString;
    }

    public static String dateAdd2(String inputFormat, String inputDate, String durationFormat, String duration, String outputFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(inputFormat);
        ParsePosition pos = new ParsePosition(0);
        Date currentTime_1 = null;
        try {
            currentTime_1 = formatter.parse(inputDate, pos);
        } catch (Exception ex) {
            lf.warning(inputDate + " format does not match " + inputFormat, "XAFunctoids", "dateAdd2");
            return (inputDate + " format does not match " + inputFormat);
        }
        long inputTime = currentTime_1.getTime();
        SimpleDateFormat formatter2 = new SimpleDateFormat(durationFormat);
        ParsePosition pos2 = new ParsePosition(0);
        Date currentTime_2 = null;
        try {
            currentTime_2 = formatter2.parse(duration, pos2);
        } catch (Exception ex) {
            lf.warning(inputDate + " format does not match " + inputFormat, "XAFunctoids", "dateAdd2");
            return (inputDate + " format does not match " + inputFormat);
        }
        long duration2 = currentTime_2.getTime();
        long resultantTime = inputTime + duration2;
        Date resultantDate = new Date(resultantTime);
        SimpleDateFormat formatter3 = null;
        try {
            formatter3 = new SimpleDateFormat(outputFormat);
        } catch (Exception ex1) {
            lf.warning(outputFormat + " does not confirm to date specifications ", "XAFunctoids", "dateAdd2");
            return (outputFormat + " does not confirm to date specifications ");
        }
        String dateString = formatter3.format(resultantDate);
        return dateString;
    }

    public static String dateAdd3(String inputFormat, String inputDate, String durationFormat, String duration, String outputFormat) {
        if ((inputDate == null) || !(inputDate.length() > 0) || inputDate.startsWith("%")) return "";
        SimpleDateFormat formatter = new SimpleDateFormat(inputFormat);
        formatter.setLenient(false);
        ParsePosition pos = new ParsePosition(0);
        Date inputTime_1 = null;
        try {
            inputTime_1 = formatter.parse(inputDate, pos);
        } catch (Exception ex) {
            lf.warning(inputDate + " format does not match " + inputFormat, "XAFunctoids", "addDayToDate");
            return (inputDate + " format does not match " + inputFormat);
        }
        try {
            String inputDate2 = inputTime_1.toString();
        } catch (Exception ex2) {
            lf.warning(inputDate + " is not a valid date ", "XAFunctoids", "addDayToDate");
            return (inputDate + " is not a valid date ");
        }
        SimpleDateFormat formatter2 = new SimpleDateFormat(durationFormat);
        formatter2.setLenient(false);
        ParsePosition pos2 = new ParsePosition(0);
        Date duration_1 = null;
        try {
            duration_1 = formatter2.parse(duration, pos2);
        } catch (Exception ex) {
            lf.warning(duration + " format does not match " + durationFormat, "XAFunctoids", "addDayToDate");
            return (duration + " format does not match " + durationFormat);
        }
        try {
            String duration_Date2 = duration_1.toString();
        } catch (Exception ex2) {
            lf.warning(duration + " is not a valid date ", "XAFunctoids", "addDayToDate");
            return (duration + " is not a valid date ");
        }
        SimpleDateFormat formatter3 = null;
        try {
            formatter3 = new SimpleDateFormat(outputFormat);
        } catch (Exception ex1) {
            lf.warning(outputFormat + " does not confirm to date specifications ", "XAFunctoids", "addDayToDate");
            return (outputFormat + " does not confirm to date specifications ");
        }
        return formatter3.format(new Date(inputTime_1.getTime() + duration_1.getTime()));
    }

    public static String dateAdd(String isoDate, String duration) {
        int startYearInt = new Integer(isoDate.substring(0, 4)).intValue();
        int startMonthInt = new Integer(isoDate.substring(4, 6)).intValue();
        int startDayInt = new Integer(isoDate.substring(6, 8)).intValue();
        int startHourInt = new Integer(isoDate.substring(8, 10)).intValue();
        int startMinInt = new Integer(isoDate.substring(10, 12)).intValue();
        int startSecInt = new Integer(isoDate.substring(12)).intValue();
        lf.finest("isoDate:" + isoDate + " duration:" + duration + " startYearInt:" + startYearInt + " startMonthInt:" + startMonthInt + " startDayInt:" + startDayInt + " startHourInt:" + startHourInt + " startMinInt:" + startMinInt + " startSecInt:" + startSecInt, "XAFunctoids", "dateAdd");
        String operation = null;
        if (duration.startsWith("-")) {
            operation = "subtract";
            duration = duration.substring(1);
        }
        int durYearInt = 0;
        int durMonthInt = 0;
        int durDayInt = 0;
        int durHourInt = 0;
        int durMinInt = 0;
        int durSecInt = 0;
        lf.finest("duration:1 " + duration, "XAFunctoids", "dateAdd");
        if (duration.indexOf("Y") > 0) {
            durYearInt = new Integer(duration.substring(1, duration.indexOf("Y"))).intValue();
            lf.finest("durYearInt: " + durYearInt, "XAFunctoids", "dateAdd");
            duration = duration.substring(duration.indexOf("Y") + 1);
        }
        lf.finest("duration:2 " + duration, "XAFunctoids", "dateAdd");
        if (duration.indexOf("M") > 0) {
            durMonthInt = new Integer(duration.substring(0, duration.indexOf("M"))).intValue();
            lf.finest("durMonthInt: " + durMonthInt, "XAFunctoids", "dateAdd");
            duration = duration.substring(duration.indexOf("M") + 1);
        }
        lf.finest("duration:3 " + duration, "XAFunctoids", "dateAdd");
        if (duration.indexOf("D") > 0) {
            durDayInt = new Integer(duration.substring(0, duration.indexOf("D"))).intValue();
            lf.finest("durDayInt: " + durDayInt, "XAFunctoids", "dateAdd");
            duration = duration.substring(duration.indexOf("D") + 1);
        }
        lf.finest("duration:4 " + duration, "XAFunctoids", "dateAdd");
        if (duration.indexOf("T") != -1) {
            if (duration.indexOf("H") > 0) {
                durHourInt = new Integer(duration.substring(1, duration.indexOf("H"))).intValue();
                lf.finest("durHourInt: " + durHourInt, "XAFunctoids", "dateAdd");
                duration = duration.substring(duration.indexOf("H") + 1);
            }
            lf.finest("duration:5 " + duration, "XAFunctoids", "dateAdd");
            if (duration.indexOf("M") > 0) {
                durMinInt = new Integer(duration.substring(0, duration.indexOf("M"))).intValue();
                lf.finest("durMinInt: " + durMinInt, "XAFunctoids", "dateAdd");
                duration = duration.substring(duration.indexOf("M") + 1);
            }
            lf.finest("duration:6 " + duration, "XAFunctoids", "dateAdd");
            if (duration.indexOf("S") > 0) {
                durSecInt = new Integer(duration.substring(0, duration.indexOf("S"))).intValue();
                lf.finest("durSecInt: " + durSecInt, "XAFunctoids", "dateAdd");
                duration = duration.substring(0, duration.indexOf("S"));
            }
        }
        lf.finest("isoDate:" + isoDate + " duration:" + duration + " startYearInt:" + startYearInt + " startMonthInt:" + startMonthInt + " startDayInt:" + startDayInt + " startHourInt:" + startHourInt + " startMinInt:" + startMinInt + " startSecInt:" + startSecInt, "XAFunctoids", "dateAdd");
        lf.finest("durYearInt:" + durYearInt + " durMonthInt:" + durMonthInt + " durDayInt:" + durDayInt + " durHourInt:" + durHourInt + " durMinInt:" + durMinInt + " durSecInt:" + durSecInt + " duration: " + duration, "XAFunctoids", "dateAdd");
        int endYearInt = 0;
        int endMonthInt = 0;
        int endDayInt = 0;
        int endHourInt = 0;
        int endMinInt = 0;
        int endSecInt = 0;
        int carry = 0;
        if (operation != "subtract") {
            endSecInt = startSecInt + durSecInt;
            if (endSecInt > 59) {
                carry = endSecInt / 60;
                endSecInt = endSecInt % 60;
                lf.finest(" carry:" + carry + " endSecInt:" + endSecInt, "XAFunctoids", "dateAdd");
            }
            endMinInt = startMinInt + durMinInt + carry;
            lf.finest(" carry:" + carry + " endMinInt:" + endMinInt, "XAFunctoids", "dateAdd");
            if (endMinInt > 59) {
                carry = endMinInt / 60;
                endMinInt = endMinInt % 60;
                lf.finest(" carry:" + carry + " endMinInt:" + endMinInt, "XAFunctoids", "dateAdd");
            }
            endHourInt = startHourInt + durHourInt + carry;
            lf.finest(" carry:" + carry + " endHourInt:" + endHourInt, "XAFunctoids", "dateAdd");
            if (endHourInt > 23) {
                carry = endHourInt / 24;
                endHourInt = endHourInt % 24;
                lf.finest(" carry:" + carry + " endHourInt:" + endHourInt, "XAFunctoids", "dateAdd");
            }
            endDayInt = startDayInt + durDayInt + carry;
            endMonthInt = startMonthInt + durMonthInt;
            endYearInt = startYearInt + durYearInt;
            lf.finest(" carry:" + carry + " endDayInt:" + endDayInt, "XAFunctoids", "dateAdd");
            while (endDayInt > maxDaysInMonth(endYearInt, endMonthInt)) {
                carry = endDayInt / maxDaysInMonth(endYearInt, endMonthInt);
                endDayInt = endDayInt % maxDaysInMonth(endYearInt, endMonthInt);
                lf.finest(" carry:" + carry + " endDayInt:" + endDayInt, "XAFunctoids", "dateAdd");
                endMonthInt += carry;
                lf.finest(" carry:" + carry + " endMonthInt:" + endMonthInt, "XAFunctoids", "dateAdd");
                if (endMonthInt > 12) {
                    carry = endMonthInt / 12;
                    endMonthInt = endMonthInt % 12;
                    lf.finest(" carry:" + carry + " endMonthInt:" + endMonthInt, "XAFunctoids", "dateAdd");
                }
                endYearInt += carry;
                lf.finest(" carry:" + carry + " endYearInt:" + endYearInt, "XAFunctoids", "dateAdd");
            }
        }
        return "result:Y" + endYearInt + " M: " + endMonthInt + " D: " + endDayInt + " H: " + endHourInt + " M: " + endMinInt + " S: " + endSecInt;
    }

    private static int maxDaysInMonth(int endYearInt, int endMonthInt) {
        int maxDaysInMonth = 0;
        if (endMonthInt > 12) {
            return 28;
        } else {
            if ((endMonthInt == 1) || (endMonthInt == 3) || (endMonthInt == 5) || (endMonthInt == 7) || (endMonthInt == 8) || (endMonthInt == 10) || (endMonthInt == 12)) {
                maxDaysInMonth = 31;
            }
            if ((endMonthInt == 4) || (endMonthInt == 6) || (endMonthInt == 9) || (endMonthInt == 11)) {
                maxDaysInMonth = 30;
            }
            if (endMonthInt == 2) {
                if (((endYearInt % 4 == 0) && !(endYearInt % 100 == 0)) || (endYearInt % 400 == 0)) {
                    maxDaysInMonth = 29;
                } else {
                    maxDaysInMonth = 28;
                }
            }
        }
        return maxDaysInMonth;
    }

    private static String monthNoToName(int monthNo) {
        String monthName = null;
        switch(monthNo) {
            case 1:
                monthName = "JAN";
                break;
            case 2:
                monthName = "FEB";
                break;
            case 3:
                monthName = "MAR";
                break;
            case 4:
                monthName = "APR";
                break;
            case 5:
                monthName = "MAY";
                break;
            case 6:
                monthName = "JUN";
                break;
            case 7:
                monthName = "JUL";
                break;
            case 8:
                monthName = "AUG";
                break;
            case 9:
                monthName = "SEP";
                break;
            case 10:
                monthName = "OCT";
                break;
            case 11:
                monthName = "NOV";
                break;
            case 12:
                monthName = "DEC";
                break;
        }
        return monthName;
    }

    private static String monthNameToNo(String monthName) {
        if (monthName.equalsIgnoreCase("JAN")) {
            return "01";
        }
        if (monthName.equalsIgnoreCase("FEB")) {
            return "02";
        }
        if (monthName.equalsIgnoreCase("MAR")) {
            return "03";
        }
        if (monthName.equalsIgnoreCase("APR")) {
            return "04";
        }
        if (monthName.equalsIgnoreCase("MAY")) {
            return "05";
        }
        if (monthName.equalsIgnoreCase("JUN")) {
            return "06";
        }
        if (monthName.equalsIgnoreCase("JUL")) {
            return "07";
        }
        if (monthName.equalsIgnoreCase("AUG")) {
            return "08";
        }
        if (monthName.equalsIgnoreCase("SEP")) {
            return "09";
        }
        if (monthName.equalsIgnoreCase("OCT")) {
            return "10";
        }
        if (monthName.equalsIgnoreCase("NOV")) {
            return "11";
        }
        if (monthName.equalsIgnoreCase("DEC")) {
            return "12";
        } else {
            return "01";
        }
    }

    /**
     * Add a day to the input date and return it in the format specified
     * 
     * @param inputFormat
     *            String
     * @param inputDate
     *            String
     * @param outputFormat
     *            String
     * @return String
     */
    public static String addDayToDate(String inputFormat, String inputDate, String outputFormat) {
        if ((inputDate == null) || !(inputDate.length() > 0) || inputDate.startsWith("%")) return "";
        SimpleDateFormat formatter = new SimpleDateFormat(inputFormat);
        formatter.setLenient(false);
        ParsePosition pos = new ParsePosition(0);
        Date currentTime_2 = null;
        try {
            currentTime_2 = formatter.parse(inputDate, pos);
        } catch (Exception ex) {
            lf.warning(inputDate + " format does not match " + inputFormat, "XAFunctoids", "addDayToDate");
            return (inputDate + " format does not match " + inputFormat);
        }
        try {
            String inputDate2 = currentTime_2.toString();
        } catch (Exception ex2) {
            lf.warning(inputDate + " is not a valid date ", "XAFunctoids", "addDayToDate");
            return (inputDate + " is not a valid date ");
        }
        SimpleDateFormat formatter2 = null;
        try {
            formatter2 = new SimpleDateFormat(outputFormat);
        } catch (Exception ex1) {
            lf.warning(outputFormat + " does not confirm to date specifications ", "XAFunctoids", "addDayToDate");
            return (outputFormat + " does not confirm to date specifications ");
        }
        return formatter2.format(new Date(currentTime_2.getTime() + 24 * 60 * 60 * 1000));
    }

    public static String add(String s1, String s2) {
        int periodIndex1 = -1;
        if (s1 != null) {
            periodIndex1 = s1.indexOf('.');
        }
        int periodIndex2 = -1;
        if (s2 != null) {
            periodIndex1 = s2.indexOf('.');
        }
        if (periodIndex1 >= 0 || periodIndex2 >= 0) {
            return new Double((new Double(s1).doubleValue()) + (new Double(s2).doubleValue())).toString();
        }
        return "" + (Integer.parseInt(s1) + Integer.parseInt(s2));
    }

    public static String subtract(String s1, String s2) {
        int periodIndex1 = -1;
        if (s1 != null) {
            periodIndex1 = s1.indexOf('.');
        }
        int periodIndex2 = -1;
        if (s2 != null) {
            periodIndex1 = s2.indexOf('.');
        }
        if (periodIndex1 >= 0 || periodIndex2 >= 0) {
            return new Double((new Double(s1).doubleValue()) - (new Double(s2).doubleValue())).toString();
        }
        return "" + (Integer.parseInt(s1) - Integer.parseInt(s2));
    }

    public static String multiply(String s1, String s2) {
        int periodIndex1 = -1;
        if (s1 != null) {
            periodIndex1 = s1.indexOf('.');
        }
        int periodIndex2 = -1;
        if (s2 != null) {
            periodIndex1 = s2.indexOf('.');
        }
        if (periodIndex1 >= 0 || periodIndex2 >= 0) {
            return new Double((new Double(s1).doubleValue()) * (new Double(s2).doubleValue())).toString();
        }
        return "" + (Integer.parseInt(s1) * Integer.parseInt(s2));
    }

    public static String divide(String s, String s1) {
        return new Double((new Double(s).doubleValue()) / (new Double(s1).doubleValue())).toString();
    }

    public static String min(String[] s) {
        double dMin = new Double(s[0]).doubleValue();
        int len = s.length;
        for (int i = 0; i <= len + 1; i++) {
            double dArray = new Double(s[i]).doubleValue();
            lf.finest("dMin: " + dMin + " i: " + i + " dArray: " + dArray, "XAFunctoids", "min");
            if (dArray < dMin) {
                dMin = dArray;
            }
            if (i == len - 1) {
                break;
            }
        }
        lf.finest("dMin: " + new Double(dMin).toString(), "XAFunctoids", "min");
        return new Double(dMin).toString();
    }

    public static String max(String[] s) {
        double dMax = new Double(s[0]).doubleValue();
        int len = s.length;
        for (int i = 0; i <= len; i++) {
            double dArray = new Double(s[i]).doubleValue();
            lf.finest("dMax: " + dMax + " i: " + i + " dArray: " + dArray, "XAFunctoids", "max");
            if (dArray > dMax) {
                dMax = dArray;
            }
            if (i == len - 1) {
                break;
            }
        }
        return new Double(dMax).toString();
    }

    private static String averageStr(String[] s4) {
        double dSum = 0;
        int len = s4.length;
        for (int i = 0; i < len; i++) {
            double dArray = new Double(s4[i]).doubleValue();
            dSum += dArray;
            lf.finest("dSum: " + dSum + " i: " + i + " dArray: " + dArray, "XAFunctoids", "averageStr");
        }
        String s2 = new Double(dSum / len).toString();
        int i3 = s2.substring(s2.indexOf(".")).length();
        System.out.println("average of: " + len + " nos is: " + s2 + " i3: " + i3);
        if (i3 > 4) {
            return s2.substring(0, s2.indexOf(".") + 5);
        } else {
            return s2;
        }
    }

    public static String round(String s) {
        if ((s == null) || !(s.length() > 0) || s.startsWith("%")) return "";
        if (s.indexOf(".") > 0) {
            Float f = new Float(s);
            int roundedNum = Math.round(f.floatValue());
            return new Integer(roundedNum).toString();
        } else {
            return s;
        }
    }

    public static String roundUp(String s) {
        if ((s == null) || !(s.length() > 0) || s.startsWith("%")) return "";
        if (s.indexOf(".") > 0) {
            int intNum = new Integer(s.substring(0, s.indexOf("."))).intValue();
            int decimalNum = new Integer(s.substring(s.indexOf(".") + 1, s.length())).intValue();
            if (decimalNum > 0) {
                intNum++;
            }
            String sRet = "" + intNum;
            return sRet;
        } else {
            return s;
        }
    }

    public static String roundDown(String s) {
        if ((s == null) || !(s.length() > 0) || s.startsWith("%")) return "";
        if (s.indexOf(".") > 0) {
            return s.substring(0, s.indexOf("."));
        } else {
            return s;
        }
    }

    public static String squareRoot(String s) {
        if ((s == null) || !(s.length() > 0) || s.startsWith("%")) return "";
        double d2 = new Double(s).doubleValue();
        double d3 = java.lang.Math.sqrt(d2);
        String s2 = new Double(d3).toString();
        int i3 = s2.substring(s2.indexOf(".")).length();
        System.out.println("squareRoot of: " + s + " is: " + d3 + " s2: " + s2 + " i3: " + i3);
        if (i3 > 4) {
            return s2.substring(0, s2.indexOf(".") + 5);
        } else {
            return s2;
        }
    }

    /**
     * Increments and returns a static long sequence number for each call
     * 
     * @return String sequence number returned for each call.
     */
    public static synchronized String getSequenceNumber() {
        sequence_Number++;
        return new Long(sequence_Number).toString();
    }

    /**
     * Resets sequence number.
     * 
     * @param numIn
     *            number to which the sequence number should be reset to.
     * @return String sequence number reset.
     * 
     */
    public static synchronized String resetSequenceNumber(String numIn) {
        if ((numIn == null) || numIn.startsWith("%")) return "";
        try {
            sequence_Number = new Integer(numIn).intValue();
        } catch (NumberFormatException ex) {
            return "Input value " + numIn + " is not integer. Sequence number not reset.";
        }
        return new Long(sequence_Number).toString();
    }

    /**
     * Returns a random number like main509869938511001
     * 
     * @return String a random number.
     */
    public static synchronized String getUniqueId() {
        Long today = new Long(System.currentTimeMillis());
        String transId2 = today + new Long(uniqueId_Count).toString();
        uniqueId_Count++;
        return transId2;
    }

    public static String sin(String s) {
        if ((s == null) || !(s.length() > 0) || s.startsWith("%")) return "";
        double d2 = new Double(s).doubleValue();
        double d3 = java.lang.Math.sin(java.lang.Math.toRadians(d2));
        String s2 = new Double(d3).toString();
        int i3 = s2.substring(s2.indexOf(".")).length();
        System.out.println("sin of: " + s + " is: " + d3 + " s2: " + s2 + " i3: " + i3);
        if (i3 > 4) {
            return s2.substring(0, s2.indexOf(".") + 5);
        } else {
            return s2;
        }
    }

    public static String cos(String s) {
        if ((s == null) || !(s.length() > 0) || s.startsWith("%")) return "";
        double d2 = new Double(s).doubleValue();
        double d3 = java.lang.Math.cos(java.lang.Math.toRadians(d2));
        String s2 = new Double(d3).toString();
        int i3 = s2.substring(s2.indexOf(".")).length();
        System.out.println("cos of: " + s + " is: " + d3 + " s2: " + s2 + " i3: " + i3);
        if (i3 > 4) {
            return s2.substring(0, s2.indexOf(".") + 5);
        } else {
            return s2;
        }
    }

    public static String tan(String s) {
        if ((s == null) || !(s.length() > 0) || s.startsWith("%")) return "";
        double d2 = new Double(s).doubleValue();
        double d3 = java.lang.Math.tan(java.lang.Math.toRadians(d2));
        String s2 = new Double(d3).toString();
        int i3 = s2.substring(s2.indexOf(".")).length();
        System.out.println("tan of: " + s + " is: " + d3 + " s2: " + s2 + " i3: " + i3);
        if (i3 > 4) {
            return s2.substring(0, s2.indexOf(".") + 5);
        } else {
            return s2;
        }
    }

    public static String arcSin(String s) {
        if ((s == null) || !(s.length() > 0) || s.startsWith("%")) return "";
        double d2 = new Double(s).doubleValue();
        double d3 = java.lang.Math.asin(d2);
        String s2 = new Double(d3).toString();
        int i3 = s2.substring(s2.indexOf(".")).length();
        System.out.println("arcSin of: " + s + " is: " + d3 + " s2: " + s2 + " i3: " + i3);
        if (i3 > 4) {
            return s2.substring(0, s2.indexOf(".") + 5);
        } else {
            return s2;
        }
    }

    public static String arcCos(String s) {
        if ((s == null) || !(s.length() > 0) || s.startsWith("%")) return "";
        double d2 = new Double(s).doubleValue();
        double d3 = java.lang.Math.acos(d2);
        String s2 = new Double(d3).toString();
        int i3 = s2.substring(s2.indexOf(".")).length();
        System.out.println("arcCos of: " + s + " is: " + d3 + " s2: " + s2 + " i3: " + i3);
        if (i3 > 4) {
            return s2.substring(0, s2.indexOf(".") + 5);
        } else {
            return s2;
        }
    }

    public static String arcTan(String s) {
        if ((s == null) || !(s.length() > 0) || s.startsWith("%")) return "";
        double d2 = new Double(s).doubleValue();
        double d3 = java.lang.Math.atan(d2);
        String s2 = new Double(d3).toString();
        int i3 = s2.substring(s2.indexOf(".")).length();
        System.out.println("arcTan of: " + s + " is: " + d3 + " s2: " + s2 + " i3: " + i3);
        if (i3 > 4) {
            return s2.substring(0, s2.indexOf(".") + 5);
        } else {
            return s2;
        }
    }

    public static String user() {
        return System.getProperty("user", "user");
    }

    public static String role() {
        return System.getProperty("role", "role");
    }

    public static String currentDirectory() {
        return System.getProperty("currentDirectory", "currentDirectory");
    }

    public static String log() {
        return System.getProperty("user", "user");
    }

    public static String logLevel() {
        return System.getProperty("user", "user");
    }

    public static String[] testArrayReturn(String s, String s1) {
        String as[] = new String[2];
        as[0] = s;
        as[1] = s1;
        return as;
    }

    public static String[] testArrays(String as[]) {
        return as;
    }

    public static String formatAddress(String s, String s1, String s2) {
        return s + ", " + s1 + " " + s2;
    }

    public static String executeProgram(String programName) {
        Runtime rt = Runtime.getRuntime();
        long l1 = System.currentTimeMillis();
        Process p = null;
        int retStatus = 0;
        long millis = 0;
        long secs = 0;
        long min = 0;
        long hours = 0;
        LinkedList output = new LinkedList();
        LinkedList error = new LinkedList();
        String ls = System.getProperty("line.separator");
        try {
            p = rt.exec(programName);
        } catch (IOException e) {
            return "Caught IOException while running " + programName + ls + " Message = " + e.getMessage() + ls;
        }
        InputStream is = p.getInputStream();
        BufferedReader bf = new BufferedReader(new InputStreamReader(is));
        String line = null;
        try {
            while ((line = bf.readLine()) != null) {
                output.addLast(line);
            }
        } catch (IOException e) {
            return "Caught IOException. " + ls + "Message = " + e.getMessage() + ls;
        }
        InputStream es = p.getErrorStream();
        BufferedReader ebf = new BufferedReader(new InputStreamReader(es));
        try {
            while ((line = ebf.readLine()) != null) {
                error.addLast(line);
            }
        } catch (IOException e) {
            return "Caught IOException. " + ls + "Message = " + e.getMessage() + ls;
        }
        try {
            retStatus = p.waitFor();
        } catch (InterruptedException e) {
        }
        StringBuffer message = new StringBuffer();
        if (output.size() != 0) {
            message.append(ls + "OUTPUT:" + ls);
            for (Iterator iter = output.iterator(); iter.hasNext(); ) {
                message.append((String) iter.next() + ls);
            }
        }
        if (error.size() != 0) {
            message.append("ERRORS:" + ls);
            for (Iterator iter = error.iterator(); iter.hasNext(); ) {
                message.append((String) iter.next() + ls);
            }
        }
        try {
            is.close();
            bf.close();
            es.close();
            ebf.close();
        } catch (IOException e) {
            return "Caught IOException while closing resources...." + ls + "Message: " + e.getMessage() + ls;
        }
        long l2 = System.currentTimeMillis();
        millis = l2 - l1;
        hours = millis / 3600000;
        millis = millis % 3600000;
        min = millis / 60000;
        millis = millis % 60000;
        secs = millis / 1000;
        millis = millis % 1000;
        message.append(ls + "TIME TO EXECUTE: ");
        if (hours < 10) {
            message.append("0");
        }
        message.append(hours + ":");
        if (min < 10) {
            message.append("0");
        }
        message.append(min + ":");
        if (secs < 10) {
            message.append("0");
        }
        message.append(secs + ":");
        if (millis < 10) {
            message.append("0");
        }
        if (millis < 100) {
            message.append("0");
        }
        message.append(millis + "(HH:MM:SS:MS)" + ls);
        return message.toString();
    }

    public static String executeBulkLoad(String command, String logLines, String timeout) {
        String ls = System.getProperty("line.separator");
        long ptimeout = 0;
        int numLines = 0;
        try {
            ptimeout = new Long(timeout).longValue();
            numLines = new Integer(logLines).intValue();
        } catch (NumberFormatException e) {
            return "The second and third parameters( number of lines and timeout ) need to be numbers. " + ls + e.getMessage();
        }
        if (ptimeout > 0) return executeBulkLoadWithTimeout(command, logLines, ptimeout);
        Runtime rt = Runtime.getRuntime();
        long l1 = System.currentTimeMillis();
        Process p = null;
        int retStatus = 0;
        long millis = 0;
        long secs = 0;
        long min = 0;
        long hours = 0;
        LinkedList output = new LinkedList();
        LinkedList error = new LinkedList();
        try {
            p = rt.exec(command);
        } catch (IOException e) {
            return "Caught IOException while executing the command. Message = " + ls + e.getMessage() + ls;
        }
        InputStream is = p.getInputStream();
        BufferedReader bf = new BufferedReader(new InputStreamReader(is));
        InputStream is2 = p.getErrorStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is2));
        try {
            output = getOutput(bf, numLines);
            error = getError(br, numLines);
        } catch (IOException e) {
            return "Caught IOException. Message = " + ls + e.getMessage() + ls;
        }
        StringBuffer message = new StringBuffer();
        if (output.size() > 0) {
            message.append(ls + "OUTPUT:" + ls);
            for (Iterator iter = output.iterator(); iter.hasNext(); ) {
                message.append((String) iter.next() + ls);
            }
        }
        if (error.size() > 0) {
            message.append("ERRORS:" + ls);
            for (Iterator iter = error.iterator(); iter.hasNext(); ) {
                message.append((String) iter.next() + ls);
            }
        }
        try {
            retStatus = p.waitFor();
        } catch (InterruptedException e) {
        }
        try {
            is.close();
            is2.close();
            bf.close();
            br.close();
        } catch (IOException e) {
        }
        long l2 = System.currentTimeMillis();
        millis = l2 - l1;
        System.out.println("millis = " + millis);
        String time = new org.xaware.shared.util.ElapsedTimeConverter().convertElapsedTime(millis);
        message.append(ls + "TIME TO EXECUTE: " + time + " (HH:MM:SS:MS) " + ls);
        return message.toString();
    }

    private static String executeBulkLoadWithTimeout(String command, String logLines, long timeout) {
        Process loadProcess = null;
        boolean programExitedWithoutErrors = false;
        boolean programExitedWithErrors = false;
        String ls = System.getProperty("line.separator");
        LinkedList output = new LinkedList();
        LinkedList error = new LinkedList();
        int numLines = new Integer(logLines).intValue();
        InputStream is = null;
        BufferedReader bf = null;
        InputStream is2 = null;
        BufferedReader br = null;
        long l1 = System.currentTimeMillis();
        long millis = 0;
        long secs = 0;
        long min = 0;
        long hours = 0;
        Runtime rt = Runtime.getRuntime();
        try {
            loadProcess = rt.exec(command);
        } catch (IOException e) {
            return "IOException thrown while executing the program " + command + ls + " Message = " + e.getMessage() + ls;
        }
        while (timeout > 1000 && loadProcess != null) {
            try {
                Thread.sleep(1000);
                long t1 = System.currentTimeMillis();
                int ret = Integer.MIN_VALUE;
                try {
                    ret = loadProcess.exitValue();
                    if (ret == 0) {
                        programExitedWithoutErrors = true;
                    } else programExitedWithErrors = true;
                    break;
                } catch (IllegalThreadStateException e) {
                    is = loadProcess.getInputStream();
                    bf = new BufferedReader(new InputStreamReader(is));
                    is2 = loadProcess.getErrorStream();
                    br = new BufferedReader(new InputStreamReader(is2));
                    try {
                        output = getOutput(bf, numLines);
                        error = getError(br, numLines);
                    } catch (IOException e1) {
                        return "Caught IOException. Message = " + ls + e1.getMessage() + ls;
                    }
                    timeout -= 1000 + (System.currentTimeMillis() - t1);
                    continue;
                }
            } catch (InterruptedException e) {
            }
        }
        if (programExitedWithoutErrors || programExitedWithErrors) {
            if (output.isEmpty()) {
                is = loadProcess.getInputStream();
                bf = new BufferedReader(new InputStreamReader(is));
                is2 = loadProcess.getErrorStream();
                br = new BufferedReader(new InputStreamReader(is2));
                try {
                    output = getOutput(bf, numLines);
                    System.out.println("output = " + output.toString());
                    error = getError(br, numLines);
                } catch (IOException e1) {
                    return "Caught IOException. Message = " + ls + e1.getMessage() + ls;
                }
            }
            StringBuffer message = new StringBuffer();
            if (output.size() > 0) {
                message.append(ls + "OUTPUT:" + ls);
                for (Iterator iter = output.iterator(); iter.hasNext(); ) {
                    message.append((String) iter.next() + ls);
                }
            }
            if (error.size() > 0) {
                message.append("ERRORS:" + ls);
                for (Iterator iter = error.iterator(); iter.hasNext(); ) {
                    message.append((String) iter.next() + ls);
                }
            }
            try {
                if (is != null) {
                    is.close();
                }
                if (is2 != null) {
                    is2.close();
                }
                if (bf != null) {
                    bf.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
            }
            long l2 = System.currentTimeMillis();
            millis = l2 - l1;
            System.out.println("millis = " + millis);
            String time = new org.xaware.shared.util.ElapsedTimeConverter().convertElapsedTime(millis);
            message.append(ls + "TIME TO EXECUTE: " + time + " (HH:MM:SS:MS) " + ls);
            return message.toString();
        }
        return "Timed out... Program " + command + " may still be running";
    }

    private static LinkedList getOutput(BufferedReader bf, int numLines) throws IOException {
        final int ALLOUTPUT = -1;
        final int NOOUTPUT = 0;
        LinkedList output = new LinkedList();
        String line = null;
        if (numLines > 0) {
            while ((line = bf.readLine()) != null) {
                if (output.size() == numLines) {
                    output.addLast(line);
                    output.removeFirst();
                } else output.addLast(line);
            }
        } else if (numLines == ALLOUTPUT) {
            while ((line = bf.readLine()) != null) {
                output.addLast(line);
            }
        } else if (numLines == NOOUTPUT) {
        }
        return output;
    }

    private static LinkedList getError(BufferedReader br, int numLines) throws IOException {
        final int ALLOUTPUT = -1;
        final int NOOUTPUT = 0;
        LinkedList error = new LinkedList();
        String eline = null;
        if (numLines > 0) {
            while ((eline = br.readLine()) != null) {
                if (error.size() == numLines) {
                    error.addLast(eline);
                    error.removeFirst();
                } else error.addLast(eline);
            }
        } else if (numLines == ALLOUTPUT) {
            while ((eline = br.readLine()) != null) {
                error.addLast(eline);
            }
        } else if (numLines == NOOUTPUT) {
        }
        return error;
    }

    /**
     * Use the SAX parser to validate the XML against a specified schema either internal or external. The following SAX
     * features will be set to "true" for the validation: o Validation - XML document should specify an XML schema or a
     * DTD. o Validation/schema - report validation errors against a schema. o Validation/schema-full-checking - full
     * schema, grammar-constraint checking.
     * 
     * The xmlDocument will be converted to a StringReader and then fed to a SAX parser. The text will be validated by
     * the external schema if the schemaUrl parameter is not null or by the internal schema if the schemaUrl parameter
     * is null. The string "VALID" will be returned if the schema is validated, "INVALID" if the validation returns an
     * error or "ERROR" if an exception is caught or some other error occurs that prevents validation. The number of
     * errors returned can be limited by setting error limit. A negative error limit means return all errors. Calling
     * the functoid that does not have an error limit value will also return all errors.
     * 
     * @param SchemaUrl
     * @param XmlDocument
     * @return
     */
    public static String stringValidateSchema(String xmlDocument, String schemaUrl) {
        return stringValidateSchema(xmlDocument, schemaUrl, "-1");
    }

    public static String stringValidateSchema(String xmlDocument, String schemaUrl, String errLimit) {
        final String methodName = "stringValidateSchema";
        String validResult = "ERROR";
        SAXParser parser = new SAXParser();
        try {
            parser.setFeature("http://xml.org/sax/features/validation", true);
            parser.setFeature("http://apache.org/xml/features/validation/schema", true);
            parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
            if (schemaUrl.length() > 0) {
                if (schemaUrl.indexOf(" ") > 0) {
                    parser.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation", schemaUrl);
                } else {
                    parser.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation", schemaUrl);
                }
            }
            XercesChecker handler = new XercesChecker();
            handler.setErrLimit(Integer.parseInt(errLimit));
            parser.setErrorHandler(handler);
            StringReader reader = new StringReader(xmlDocument);
            InputSource inputSource = new InputSource(reader);
            parser.parse(inputSource);
            if (handler.isValid()) {
                validResult = "VALID";
                lf.fine("XML Document is valid", className, methodName);
            } else {
                validResult = "INVALID: \n" + handler.getErrMsg();
                lf.warning(handler.getErrMsg(), className, methodName);
            }
        } catch (SAXParseException e) {
            System.out.println(": The XML String is not well-formed at ");
            validResult += ": \nLine " + e.getLineNumber() + ", column " + e.getColumnNumber() + " in file " + e.getSystemId();
        } catch (SAXException e) {
            validResult += ": \nCould not check document because " + e.getMessage();
        } catch (IOException e) {
            validResult += ": \nDue to an IOException, the parser could not check ";
        }
        return validResult;
    }

    /**
     * Use the SAX parser to validate the XML against a specified schema either internal or external. The following SAX
     * features will be set to "true" for the validation: o Validation - XML document should specify an XML schema or a
     * DTD. o Validation/schema - report validation errors against a schema. o Validation/schema-full-checking - full
     * schema, grammar-constraint checking.
     * 
     * The fileUri will be opened with a FileReader and then fed to a SAX parser. The text will be validated by the
     * external schema if the schemaUrl parameter is not null or by the internal schema if the schemaUrl parameter is
     * null. The string "VALID" will be returned if the schema is validated, "INVALID" if the validation returns an
     * error or "ERROR" if an exception is caught or some other error occurs that prevents validation. The number of
     * errors returned can be limited by setting error limit. A negative error limit means return all errors. Calling
     * the functoid that does not have an error limit value will also return all errors.
     * 
     * @param SchemaUrl
     * @param XmlDocument
     * @return
     */
    public static String fileValidateSchema(String filename, String schemaUrl) {
        return fileValidateSchema(filename, schemaUrl, "-1");
    }

    public static String fileValidateSchema(String filename, String schemaUrl, String errLimit) {
        final String methodName = "fileValidateSchema";
        String validResult = "ERROR";
        SAXParser parser = new SAXParser();
        try {
            parser.setFeature("http://xml.org/sax/features/validation", true);
            parser.setFeature("http://apache.org/xml/features/validation/schema", true);
            parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
            if (schemaUrl.length() > 0) {
                if (schemaUrl.indexOf(" ") > 0) {
                    parser.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation", schemaUrl);
                } else {
                    parser.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation", schemaUrl);
                }
            }
            XercesChecker handler = new XercesChecker();
            handler.setErrLimit(Integer.parseInt(errLimit));
            parser.setErrorHandler(handler);
            parser.parse(filename);
            if (handler.isValid()) {
                validResult = "VALID";
                lf.fine("XML Document is valid", className, methodName);
            } else {
                validResult = "INVALID: \n" + handler.getErrMsg();
                lf.warning(handler.getErrMsg(), className, methodName);
            }
        } catch (SAXParseException e) {
            System.out.println(filename + " is not well-formed at ");
            validResult += ": \nLine " + e.getLineNumber() + ", column " + e.getColumnNumber() + " in file " + e.getSystemId();
        } catch (SAXException e) {
            validResult += ": \nCould not check document because " + e.getMessage();
        } catch (IOException e) {
            validResult += ": \nDue to an IOException, the parser could not check " + filename;
        }
        return validResult;
    }

    /**
     * Compare the two supplied strings to determine if they are equal.
     * 
     * @param s1
     *            String - First string to compare with second
     * @param s2
     *            String - Second string to compare with first
     * @return Returns string "true" if strings are equal, otherwise returns string "false"
     */
    public static String equalStrings(String s1, String s2) {
        String ret = "false";
        if ((s1 == null) && (s2 == null)) ret = "true"; else if ((s1 == null) && (s2 != null)) return ret; else if ((s1 != null) && (s2 == null)) return ret; else if (s1.equals(s2)) ret = "true";
        return ret;
    }

    /**
     * Test the supplied string to see if it has a null value
     * 
     * @param s -
     *            String that will be tested for null values
     * @return Returns string "true" if string has null values, otherwise it returns the string "false"
     */
    public static String isNull(String s) {
        boolean nullValue = false;
        if (s == null) return "true";
        if (s.length() >= 4) {
            String str = s;
            do {
                boolean nullTest1 = str.indexOf("&#0;") == 0;
                if (nullTest1) {
                    if (str.length() > 4) str = str.substring(4); else str = new String();
                }
                boolean nullTest2 = str.indexOf("&#x0;") == 0;
                if (nullTest2) {
                    if (str.length() > 5) str = str.substring(5); else str = new String();
                }
                nullValue = nullTest1 || nullTest2;
            } while ((str.length() > 0) && nullValue);
        } else {
            byte v[] = s.getBytes();
            if (v.length > 0) {
                nullValue = true;
                for (int i = 0; i < v.length; i++) nullValue = nullValue && (v[i] == 0);
            }
        }
        return (nullValue ? "true" : "false");
    }

    /**
     * Determine if the supplied string is comprised of a numeric expression. The number can have a leading sign and one
     * embeded decimal point. All other characters must be numeric.
     * 
     * @param s -
     *            String supplied for evaluation
     * @return Returns string "true" if it is numeric, "false" otherwise.
     */
    public static String isNumeric(String s) {
        int detectedDecimal = 0;
        boolean ret = true;
        if (s == null) return "false";
        String numbEval = s.trim();
        if (numbEval.startsWith("+") || numbEval.startsWith("-")) {
            numbEval = numbEval.substring(1);
        }
        byte numbChars[] = numbEval.getBytes();
        for (int i = 0; i < numbChars.length && ret; i++) {
            switch(numbChars[i]) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    break;
                case '.':
                    detectedDecimal++;
                    ret = ret && (detectedDecimal < 2);
                    break;
                default:
                    ret = false;
            }
        }
        return (ret ? "true" : "false");
    }

    /**
     * Determine if the supplied string is a number and that its value is zero
     * 
     * @param s
     * @return Returns string "true" if string is a zero value, otherwise it returns the string "false"
     */
    public static String isZero(String s) {
        String ret = isNumeric(s);
        if ("true".equals(ret)) {
            if (s.length() > 0) {
                StringBuffer localVal = new StringBuffer(s);
                if (s.startsWith("+") || s.startsWith("-")) {
                    localVal.deleteCharAt(0);
                }
                int decimalPoint = localVal.toString().indexOf(".");
                if (decimalPoint >= 0) {
                    localVal.deleteCharAt(decimalPoint);
                }
                try {
                    int numb = Integer.parseInt(localVal.toString());
                    if (numb != 0) ret = "false";
                } catch (NumberFormatException e) {
                    ret = "false";
                }
            }
        }
        return ret;
    }

    public static void main(String[] args) {
        String ret = null;
        String command = "C:\\cygwin\\bin\\ssh.exe mmarshall@sybase-test02 'bcp test02.dbo.QAEmp in datafile200.txt -c -t\"**\" -r\"\\r\\n\" -Usa -P -b1000 -SSYBASETEST02 -eC:\\load.err'";
        String command2 = "bcp test.dbo.emp in C:\\datafile2.txt -c -t\"***\" -r\"\\r\\n\" -Usa -P -b1000 -SMMARSHALLT42P -eC:\\load.err";
        ret = XAFunctoids.executeBulkLoad(command, "-1", "-1");
        XAFunctoids f = new XAFunctoids();
        System.out.println(f.DateTimePerGivenFormat("Date", "dd.MM.yy"));
        System.out.println(f.DateTimePerGivenFormat("Time", "h:mm a"));
        System.out.println(f.DateTimePerGivenFormat("Timestamp", "yyyy.MMMMM.dd GGG hh:mm aaa"));
        System.out.println(ret);
    }

    /**
     * Generate a universally unique ID
     * 
     * @return String
     */
    public static String getUUID() {
        return UUIDGenerator.getInstance().generateTimeBasedUUID().toString();
    }

    /**
     * Validate a Universally unique id as created by getUUID
     * 
     * @param sGUID -
     *            String
     * @return String - "true" or "false"
     */
    public static String isValidUUID(String sGUID) {
        String result = "true";
        String pattern = "[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(sGUID);
        if (!m.matches()) {
            result = "false";
        }
        return result;
    }
}
