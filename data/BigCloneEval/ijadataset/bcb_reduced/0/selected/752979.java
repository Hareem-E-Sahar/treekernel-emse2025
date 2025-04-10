package ggc.mobile.util;

import ggc.mobile.db.GGCDbMobile;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Image;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.Collator;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.swing.ImageIcon;

public class DataAccessMobile {

    public String current_db_version = "7";

    /**
     * At later time this will be determined by user management part
     */
    public long current_user_id = 1;

    public static final String pathPrefix = ".";

    private static DataAccessMobile s_da = null;

    public GGCDbMobile m_db = null;

    public Font fonts[] = null;

    private GregorianCalendar m_date = null, m_dateStart = null;

    public static DecimalFormat MmolDecimalFormat = new DecimalFormat("#0.0");

    public static DecimalFormat Decimal0Format = new DecimalFormat("#0");

    public static DecimalFormat Decimal2Format = new DecimalFormat("#0.00");

    public static DecimalFormat Decimal3Format = new DecimalFormat("#0.000");

    /**
     * Which BG unit is used: BG_MGDL = mg/dl, BG_MMOL = mmol/l
     */
    public int m_BG_unit = BG_MGDL;

    public String[] availableLanguages = { "English", "Deutsch", "Slovenski", "Français" };

    public String[] avLangPostfix = { "en", "de", "si", "fr" };

    public String[] bg_units = { "mg/dl", "mmol/l" };

    public Hashtable<String, String> timeZones;

    public ArrayList<Container> parents_list;

    public ImageIcon config_icons[] = null;

    protected Collator m_collator = null;

    public String days[] = new String[7];

    public String months[] = new String[12];

    protected I18nControl m_i18n = null;

    public String[] options_yes_no = null;

    /**
     * 
     * This is DataAccess constructor; Since classes use Singleton Pattern,
     * constructor is protected and can be accessed only with getInstance()
     * method.<br>
     * <br>
     * 
     */
    private DataAccessMobile() {
        this.m_i18n = I18nControl.getInstance();
        loadArraysTranslation();
        this.m_collator = this.m_i18n.getCollationDefintion();
        this.m_db = new GGCDbMobile(this);
    }

    /**
     * 
     * This method returns reference to OmniI18nControl object created, or if no
     * object was created yet, it creates one.<br>
     * <br>
     * 
     * @return Reference to OmniI18nControl object
     * 
     */
    public static DataAccessMobile getInstance() {
        if (s_da == null) s_da = new DataAccessMobile();
        return s_da;
    }

    /**
     * This method sets handle to DataAccess to null and deletes the instance. <br>
     * <br>
     */
    public static void deleteInstance() {
        DataAccessMobile.s_da = null;
    }

    public GGCDbMobile getDb() {
        return m_db;
    }

    public void setDb(GGCDbMobile db) {
        this.m_db = db;
    }

    public void loadArraysTranslation() {
        months[0] = m_i18n.getMessage("JANUARY");
        months[1] = m_i18n.getMessage("FEBRUARY");
        months[2] = m_i18n.getMessage("MARCH");
        months[3] = m_i18n.getMessage("APRIL");
        months[4] = m_i18n.getMessage("MAY");
        months[5] = m_i18n.getMessage("JUNE");
        months[6] = m_i18n.getMessage("JULY");
        months[7] = m_i18n.getMessage("AUGUST");
        months[8] = m_i18n.getMessage("SEPTEMBER");
        months[9] = m_i18n.getMessage("OCTOBER");
        months[10] = m_i18n.getMessage("NOVEMBER");
        months[11] = m_i18n.getMessage("DECEMBER");
        days[0] = m_i18n.getMessage("MONDAY");
        days[1] = m_i18n.getMessage("TUESDAY");
        days[2] = m_i18n.getMessage("WEDNESDAY");
        days[3] = m_i18n.getMessage("THURSDAY");
        days[4] = m_i18n.getMessage("FRIDAY");
        days[5] = m_i18n.getMessage("SATURDAY");
        days[6] = m_i18n.getMessage("SUNDAY");
    }

    public static String getFloatAsString(float f, String decimal_places) {
        return DataAccessMobile.getFloatAsString(f, Integer.parseInt(decimal_places));
    }

    public static String getFloatAsString(float f, int decimal_places) {
        switch(decimal_places) {
            case 1:
                return DataAccessMobile.MmolDecimalFormat.format(f);
            case 2:
                return DataAccessMobile.Decimal2Format.format(f);
            case 3:
                return DataAccessMobile.Decimal3Format.format(f);
            default:
                return DataAccessMobile.Decimal0Format.format(f);
        }
    }

    public String getImagesRoot() {
        return "/icons/";
    }

    private void loadIcons() {
    }

    public String[] getAvailableLanguages() {
        return this.availableLanguages;
    }

    public int getSelectedLanguageIndex() {
        return 0;
    }

    public int getLanguageIndex(String postfix) {
        for (int i = 0; i < this.avLangPostfix.length; i++) {
            if (this.avLangPostfix[i].equals(postfix)) return i;
        }
        return 0;
    }

    public int getLanguageIndexByName(String name) {
        for (int i = 0; i < this.availableLanguages.length; i++) {
            if (this.availableLanguages[i].equals(name)) return i;
        }
        return 0;
    }

    public static final int BG_MGDL = 1;

    public static final int BG_MMOL = 2;

    public int getBGMeasurmentType() {
        return this.m_BG_unit;
    }

    public void setBGMeasurmentType(int type) {
        this.m_BG_unit = type;
    }

    public static final float MGDL_TO_MMOL_FACTOR = 0.0555f;

    public static final float MMOL_TO_MGDL_FACTOR = 18.016f;

    /**
     * Depending on the return value of <code>getBGMeasurmentType()</code>,
     * either return the mg/dl or the mmol/l value of the database's value.
     * Default is mg/dl.
     * 
     * @param dbValue
     *            - The database's value (in float)
     * @return the BG in either mg/dl or mmol/l
     */
    public float getDisplayedBG(float dbValue) {
        switch(this.m_BG_unit) {
            case BG_MMOL:
                return (new BigDecimal(dbValue * MGDL_TO_MMOL_FACTOR, new MathContext(3, RoundingMode.HALF_UP)).floatValue());
            case BG_MGDL:
            default:
                return dbValue;
        }
    }

    public float getBGValue(float bg_value) {
        switch(this.m_BG_unit) {
            case BG_MMOL:
                return (bg_value * MGDL_TO_MMOL_FACTOR);
            case BG_MGDL:
            default:
                return bg_value;
        }
    }

    public float getBGValueByType(int type, float bg_value) {
        switch(type) {
            case BG_MMOL:
                return (bg_value * MGDL_TO_MMOL_FACTOR);
            case BG_MGDL:
            default:
                return bg_value;
        }
    }

    public float getBGValueByType(int input_type, int output_type, float bg_value) {
        if (input_type == output_type) return bg_value; else {
            if (output_type == DataAccessMobile.BG_MGDL) {
                return bg_value * DataAccessMobile.MGDL_TO_MMOL_FACTOR;
            } else {
                return bg_value * DataAccessMobile.MMOL_TO_MGDL_FACTOR;
            }
        }
    }

    public float getBGValueDifferent(int type, float bg_value) {
        if (type == DataAccessMobile.BG_MGDL) {
            return bg_value * DataAccessMobile.MGDL_TO_MMOL_FACTOR;
        } else {
            return bg_value * DataAccessMobile.MMOL_TO_MGDL_FACTOR;
        }
    }

    public I18nControl getI18nControlInstance() {
        return this.m_i18n;
    }

    /**
     * Utils
     */
    public Image getImage(String filename, Component cmp) {
        Image img;
        InputStream is = this.getClass().getResourceAsStream(filename);
        if (is == null) System.out.println("Error reading image: " + filename);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            int c;
            while ((c = is.read()) >= 0) baos.write(c);
            img = cmp.getToolkit().createImage(baos.toByteArray());
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return img;
    }

    private int current_person_id = 1;

    public int getCurrentPersonId() {
        return this.current_person_id;
    }

    public static String[] getLFData() {
        String out[] = new String[2];
        try {
            Properties props = new Properties();
            FileInputStream in = new FileInputStream("../data/GGC_Config.properties");
            props.load(in);
            out[0] = (String) props.get("LF_CLASS");
            out[1] = (String) props.get("SKINLF_SELECTED");
            return out;
        } catch (Exception ex) {
            System.out.println("DataAccess::getLFData::Exception> " + ex);
            return null;
        }
    }

    private long component_id_last;

    public String getNewComponentId() {
        component_id_last++;
        return "" + this.component_id_last;
    }

    public void loadOptions() {
        this.options_yes_no = new String[2];
        this.options_yes_no[0] = m_i18n.getMessage("YES");
        this.options_yes_no[1] = m_i18n.getMessage("NO");
    }

    public String getCurrentDateString() {
        GregorianCalendar gc = new GregorianCalendar();
        return gc.get(Calendar.DAY_OF_MONTH) + "." + (gc.get(Calendar.MONTH) + 1) + "." + gc.get(Calendar.YEAR);
    }

    public String[] getMonthsArray() {
        String arr[] = new String[12];
        arr[0] = m_i18n.getMessage("JANUARY");
        arr[1] = m_i18n.getMessage("FEBRUARY");
        arr[2] = m_i18n.getMessage("MARCH");
        arr[3] = m_i18n.getMessage("APRIL");
        arr[4] = m_i18n.getMessage("MAY");
        arr[5] = m_i18n.getMessage("JUNE");
        arr[6] = m_i18n.getMessage("JULY");
        arr[7] = m_i18n.getMessage("AUGUST");
        arr[8] = m_i18n.getMessage("SEPTEMBER");
        arr[9] = m_i18n.getMessage("OCTOBER");
        arr[10] = m_i18n.getMessage("NOVEMBER");
        arr[11] = m_i18n.getMessage("DECEMBER");
        return arr;
    }

    public String getDateString(int date) {
        int year = date / 10000;
        int months = date - (year * 10000);
        months = months / 100;
        int days = date - (year * 10000) - (months * 100);
        if (year == 0) return getLeadingZero(days, 2) + "/" + getLeadingZero(months, 2); else return getLeadingZero(days, 2) + "/" + getLeadingZero(months, 2) + "/" + year;
    }

    public String getTimeString(int time) {
        int hours = time / 100;
        int min = time - hours * 100;
        return getLeadingZero(hours, 2) + ":" + getLeadingZero(min, 2);
    }

    public String getDateTimeString(long date) {
        return getDateTimeString(date, 1);
    }

    public String getDateTimeAsDateString(long date) {
        return getDateTimeString(date, 2);
    }

    public String getDateTimeAsTimeString(long date) {
        return getDateTimeString(date, 3);
    }

    public static final int DT_DATETIME = 1;

    public static final int DT_DATE = 2;

    public static final int DT_TIME = 3;

    public String getDateTimeString(long dt, int ret_type) {
        int y = (int) (dt / 100000000L);
        dt -= y * 100000000L;
        int m = (int) (dt / 1000000L);
        dt -= m * 1000000L;
        int d = (int) (dt / 10000L);
        dt -= d * 10000L;
        int h = (int) (dt / 100L);
        dt -= h * 100L;
        int min = (int) dt;
        if (ret_type == DT_DATETIME) return getLeadingZero(d, 2) + "." + getLeadingZero(m, 2) + "." + y + "  " + getLeadingZero(h, 2) + ":" + getLeadingZero(min, 2); else if (ret_type == DT_DATE) return getLeadingZero(d, 2) + "." + getLeadingZero(m, 2) + "." + y; else return getLeadingZero(h, 2) + ":" + getLeadingZero(min, 2);
    }

    public Date getDateTimeAsDateObject(long dt) {
        GregorianCalendar gc = new GregorianCalendar();
        int y = (int) (dt / 100000000L);
        dt -= y * 100000000L;
        int m = (int) (dt / 1000000L);
        dt -= m * 1000000L;
        int d = (int) (dt / 10000L);
        dt -= d * 10000L;
        int h = (int) (dt / 100L);
        dt -= h * 100L;
        int min = (int) dt;
        gc.set(Calendar.DATE, d);
        gc.set(Calendar.MONTH, m - 1);
        gc.set(Calendar.YEAR, y);
        gc.set(Calendar.HOUR_OF_DAY, h);
        gc.set(Calendar.MINUTE, min);
        return gc.getTime();
    }

    public long getDateTimeLong(long dt, int ret_type) {
        int y = (int) (dt / 100000000L);
        dt -= y * 100000000L;
        int m = (int) (dt / 1000000L);
        dt -= m * 1000000L;
        int d = (int) (dt / 10000L);
        dt -= d * 10000L;
        int h = (int) (dt / 100L);
        dt -= h * 100L;
        int min = (int) dt;
        if (ret_type == DT_DATETIME) {
            return Integer.parseInt(y + getLeadingZero(m, 2) + getLeadingZero(d, 2) + getLeadingZero(h, 2) + getLeadingZero(min, 2));
        } else if (ret_type == DT_DATE) {
            return Integer.parseInt(getLeadingZero(d, 2) + getLeadingZero(m, 2) + y);
        } else return Integer.parseInt(getLeadingZero(h, 2) + getLeadingZero(min, 2));
    }

    public long getDateTimeFromDateObject(Date dt) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(dt);
        String dx = "";
        dx += "" + gc.get(Calendar.YEAR);
        dx += "" + getLeadingZero(gc.get(Calendar.MONTH + 1), 2);
        dx += "" + getLeadingZero(gc.get(Calendar.DAY_OF_MONTH), 2);
        dx += "" + getLeadingZero(gc.get(Calendar.HOUR_OF_DAY), 2);
        dx += "" + getLeadingZero(gc.get(Calendar.MINUTE), 2);
        return Long.parseLong(dx);
    }

    public String getDateTimeStringFromGregorianCalendar(GregorianCalendar gc, int type) {
        String st = "";
        if (gc.get(Calendar.YEAR) < 1000) {
            st += gc.get(Calendar.YEAR) + 1900;
        } else {
            st += gc.get(Calendar.YEAR);
        }
        st += getLeadingZero(gc.get(Calendar.MONTH) + 1, 2);
        st += getLeadingZero(gc.get(Calendar.DAY_OF_MONTH), 2);
        if (type == 2) {
            st += getLeadingZero(gc.get(Calendar.HOUR_OF_DAY), 2);
            st += getLeadingZero(gc.get(Calendar.MINUTE), 2);
        }
        return st;
    }

    public String getDateTimeString(int date, int time) {
        return getDateString(date) + " " + getTimeString(time);
    }

    public int getStartYear() {
        return 1970;
    }

    public boolean isSameDay(GregorianCalendar day) {
        return isSameDay(m_date, day);
    }

    public boolean isSameDay(GregorianCalendar gc1, GregorianCalendar gc2) {
        if ((gc1 == null) || (gc2 == null)) {
            return false;
        } else {
            if ((gc1.get(Calendar.DAY_OF_MONTH) == gc2.get(Calendar.DAY_OF_MONTH)) && (gc1.get(Calendar.MONTH) == gc2.get(Calendar.MONTH)) && (gc1.get(Calendar.YEAR) == gc2.get(Calendar.YEAR))) {
                return true;
            } else {
                return false;
            }
        }
    }

    public GregorianCalendar getGregorianCalendar(Date date) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        return gc;
    }

    public static void notImplemented(String source) {
        System.out.println("Not Implemented: " + source);
    }

    public String getLeadingZero(int number, int places) {
        String nn = "" + number;
        while (nn.length() < places) {
            nn = "0" + nn;
        }
        return nn;
    }

    public String getLeadingZero(String number, int places) {
        number = number.trim();
        while (number.length() < places) {
            number = "0" + number;
        }
        return number;
    }

    /**
     * For replacing strings.<br>
     * 
     * @param input   Input String
     * @param replace What to seatch for.
     * @param replacement  What to replace with.
     * 
     * @return Parsed string.
     */
    public String replaceExpression(String input, String replace, String replacement) {
        int idx;
        if ((idx = input.indexOf(replace)) == -1) {
            return input;
        }
        boolean finished = false;
        while (!finished) {
            StringBuffer returning = new StringBuffer();
            while (idx != -1) {
                returning.append(input.substring(0, idx));
                returning.append(replacement);
                input = input.substring(idx + replace.length());
                idx = input.indexOf(replace);
            }
            returning.append(input);
            input = returning.toString();
            if ((idx = returning.indexOf(replace)) == -1) {
                finished = true;
            }
        }
        return input;
    }

    public String parseExpression(String in, String expression, String replace) {
        StringBuffer buffer;
        int idx = in.indexOf(expression);
        if (replace == null) replace = "";
        if (idx == -1) return in;
        buffer = new StringBuffer();
        while (idx != -1) {
            buffer.append(in.substring(0, idx));
            buffer.append(replace);
            in = in.substring(idx + expression.length());
            idx = in.indexOf(expression);
        }
        buffer.append(in);
        return buffer.toString();
    }

    public String parseExpressionFull(String in, String expression, String replace) {
        String buffer;
        int idx = in.indexOf(expression);
        if (replace == null) replace = "";
        if (idx == -1) return in;
        buffer = "";
        if (idx != -1) {
            buffer = in.substring(0, idx) + replace + in.substring(idx + expression.length());
            idx = in.indexOf(expression);
            if (idx != -1) buffer = parseExpressionFull(buffer, expression, replace);
        }
        return buffer;
    }

    public boolean isEmptyOrUnset(String val) {
        if ((val == null) || (val.trim().length() == 0)) {
            return true;
        } else return false;
    }

    public static boolean isFound(String text, String search_str) {
        if ((search_str.trim().length() == 0) || (text.trim().length() == 0)) return true;
        return text.trim().indexOf(search_str.trim()) != -1;
    }

    public String[] splitString(String input, String delimiter) {
        String res[] = null;
        if (!input.contains(delimiter)) {
            res = new String[1];
            res[0] = input;
        } else {
            StringTokenizer strtok = new StringTokenizer(input, delimiter);
            res = new String[strtok.countTokens()];
            int i = 0;
            while (strtok.hasMoreTokens()) {
                res[i] = strtok.nextToken().trim();
                i++;
            }
        }
        return res;
    }

    public float getFloatValue(Object aValue) {
        float out = 0.0f;
        if (aValue == null) return out;
        if (aValue instanceof Float) {
            try {
                Float f = (Float) aValue;
                out = f.floatValue();
            } catch (Exception ex) {
            }
        } else if (aValue instanceof Double) {
            try {
                Double f = (Double) aValue;
                out = f.floatValue();
            } catch (Exception ex) {
            }
        } else if (aValue instanceof Integer) {
            try {
                Integer f = (Integer) aValue;
                out = f.floatValue();
            } catch (Exception ex) {
            }
        } else if (aValue instanceof String) {
            String s = (String) aValue;
            if (s.length() > 0) {
                try {
                    out = Float.parseFloat(s);
                } catch (Exception ex) {
                }
            }
        }
        return out;
    }

    public int getIntValue(Object aValue) {
        int out = 0;
        if (aValue == null) return out;
        if (aValue instanceof Integer) {
            try {
                Integer i = (Integer) aValue;
                out = i.intValue();
            } catch (Exception ex) {
            }
        } else if (aValue instanceof String) {
            String s = (String) aValue;
            if (s.length() > 0) {
                try {
                    out = Integer.parseInt(s);
                } catch (Exception ex) {
                }
            }
        }
        return out;
    }

    public long getLongValue(Object aValue) {
        long out = 0L;
        if (aValue == null) return out;
        if (aValue instanceof Long) {
            try {
                Long i = (Long) aValue;
                out = i.longValue();
            } catch (Exception ex) {
            }
        } else if (aValue instanceof String) {
            String s = (String) aValue;
            if (s.length() > 0) {
                try {
                    out = Long.parseLong(s);
                } catch (Exception ex) {
                }
            }
        }
        return out;
    }

    public float getFloatValueFromString(String aValue) {
        return this.getFloatValueFromString(aValue, 0.0f);
    }

    public float getFloatValueFromString(String aValue, float def_value) {
        float out = def_value;
        try {
            out = Float.parseFloat(aValue);
        } catch (Exception ex) {
            System.out.println("Error on parsing string to get float [" + aValue + "]:" + ex);
        }
        return out;
    }

    public int getIntValueFromString(String aValue) {
        return this.getIntValueFromString(aValue, 0);
    }

    public int getIntValueFromString(String aValue, int def_value) {
        int out = def_value;
        try {
            out = Integer.parseInt(aValue);
        } catch (Exception ex) {
            System.out.println("Error on parsing string to get int [" + aValue + "]:" + ex);
        }
        return out;
    }

    public long getLongValueFromString(String aValue) {
        return this.getLongValueFromString(aValue, 0L);
    }

    public long getLongValueFromString(String aValue, long def_value) {
        long out = def_value;
        try {
            out = Long.parseLong(aValue);
        } catch (Exception ex) {
            System.out.println("Error on parsing string to get long [" + aValue + "]:" + ex);
        }
        return out;
    }
}
