package ggc.cgm.util;

import ggc.cgm.manager.CGMManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import com.atech.db.hibernate.HibernateDb;
import com.atech.utils.ATDataAccessAbstract;

public class DataAccessCGM extends ATDataAccessAbstract {

    public static final String pathPrefix = ".";

    public I18nControl m_i18n = null;

    private static DataAccessCGM s_da = null;

    public Font fonts[] = null;

    private CGMManager m_meterManager = null;

    public static DecimalFormat MmolDecimalFormat = new DecimalFormat("#0.0");

    /**
     * Which BG unit is used: BG_MGDL = mg/dl, BG_MMOL = mmol/l
     */
    public int m_BG_unit = BG_MGDL;

    public String[] availableLanguages = { "English", "Deutsch", "Slovenski" };

    public String[] avLangPostfix = { "en", "de", "si" };

    public String[] bg_units = { "mg/dl", "mmol/l" };

    public Hashtable<String, String> timeZones;

    public ImageIcon config_icons[] = null;

    public String[] options_yes_no = null;

    public Hashtable<String, String> metersUrl;

    public ArrayList<String> metersNames;

    /**
     *
     *  This is DataAccess constructor; Since classes use Singleton Pattern,
     *  constructor is protected and can be accessed only with getInstance() 
     *  method.<br><br>
     *
     */
    private DataAccessCGM() {
        super(I18nControl.getInstance());
    }

    public void initSpecial() {
        this.loadFonts();
        this.loadTimeZones();
        loadPumpsTable();
        checkPrerequisites();
    }

    /**
     *
     *  This method returns reference to OmniI18nControl object created, or if no 
     *  object was created yet, it creates one.<br><br>
     *
     *  @return Reference to OmniI18nControl object
     * 
     */
    public static DataAccessCGM getInstance() {
        if (s_da == null) s_da = new DataAccessCGM();
        return s_da;
    }

    public static DataAccessCGM createInstance(JFrame main) {
        if (s_da == null) {
            s_da = new DataAccessCGM();
        }
        return s_da;
    }

    /**
     *  This method sets handle to DataAccess to null and deletes the instance. <br><br>
     */
    public void deleteInstance() {
        m_i18n = null;
    }

    public void loadPumpsTable() {
        metersUrl = new Hashtable<String, String>();
        metersNames = new ArrayList<String>();
        metersNames.add("Abbott Diabetes Care");
        metersUrl.put("Abbott Diabetes Care", "abbott.html");
        metersNames.add("Bayer Diagnostics");
        metersUrl.put("Bayer Diagnostics", "bayer.html");
        metersNames.add("Diabetic Supply of Suncoast");
        metersUrl.put("Diabetic Supply of Suncoast", "suncoast.html");
        metersNames.add("Diagnostic Devices");
        metersUrl.put("Diagnostic Devices", "prodigy.html");
        metersNames.add("Arkray USA (formerly Hypoguard)");
        metersUrl.put("Arkray USA (formerly Hypoguard)", "arkray.html");
        metersNames.add("HealthPia America");
        metersUrl.put("HealthPia America", "healthpia.html");
        metersNames.add("Home Diagnostics");
        metersUrl.put("Home Diagnostics", "home_diganostics.html");
        metersNames.add("Lifescan");
        metersUrl.put("Lifescan", "lifescan.html");
        metersNames.add("Nova Biomedical");
        metersUrl.put("Nova Biomedical", "nova_biomedical.html");
        metersNames.add("Roche Diagnostics");
        metersUrl.put("Roche Diagnostics", "roche.html");
        metersNames.add("Sanvita");
        metersUrl.put("Sanvita", "sanvita.html");
        metersNames.add("U.S. Diagnostics");
        metersUrl.put("U.S. Diagnostics", "us_diagnostics.html");
        metersNames.add("WaveSense");
        metersUrl.put("WaveSense", "wavesense.html");
    }

    public String getApplicationName() {
        return "GGC_PumpTool";
    }

    public void checkPrerequisites() {
    }

    public String getImagesRoot() {
        return "/icons/";
    }

    public void loadBackupRestoreCollection() {
    }

    public CGMManager getCGMManager() {
        return this.m_meterManager;
    }

    public void loadTimeZones() {
        this.timeZones = new Hashtable<String, String>();
        this.timeZones.put("(GMT+13:00) Nuku'alofa", "Pacific/Tongatapu");
        this.timeZones.put("(GMT+12:00) Fiji, Kamchatka, Marshall Is.", "Pacific/Fiji");
        this.timeZones.put("(GMT+12:00) Auckland, Wellington", "Pacific/Auckland");
        this.timeZones.put("(GMT+11:00) Magadan, Solomon Is., New Caledonia", "Asia/Magadan");
        this.timeZones.put("(GMT+10:00) Vladivostok", "Asia/Vladivostok");
        this.timeZones.put("(GMT+10:00) Hobart", "Australia/Hobart");
        this.timeZones.put("(GMT+10:00) Guam, Port Moresby", "Pacific/Guam");
        this.timeZones.put("(GMT+10:00) Canberra, Melbourne, Sydney", "Australia/Sydney");
        this.timeZones.put("(GMT+10:00) Brisbane", "Australia/Brisbane");
        this.timeZones.put("(GMT+09:30) Adelaide", "Australia/Adelaide");
        this.timeZones.put("(GMT+09:00) Yakutsk", "Asia/Yakutsk");
        this.timeZones.put("(GMT+09:00) Seoul", "Asia/Seoul");
        this.timeZones.put("(GMT+09:00) Osaka, Sapporo, Tokyo", "Asia/Tokyo");
        this.timeZones.put("(GMT+08:00) Taipei", "Asia/Taipei");
        this.timeZones.put("(GMT+08:00) Perth", "Australia/Perth");
        this.timeZones.put("(GMT+08:00) Kuala Lumpur, Singapore", "Asia/Kuala_Lumpur");
        this.timeZones.put("(GMT+08:00) Irkutsk, Ulaan Bataar", "Asia/Irkutsk");
        this.timeZones.put("(GMT+08:00) Beijing, Chongqing, Hong Kong, Urumqi", "Asia/Hong_Kong");
        this.timeZones.put("(GMT+07:00) Krasnoyarsk", "Asia/Krasnoyarsk");
        this.timeZones.put("(GMT+07:00) Bangkok, Hanoi, Jakarta", "Asia/Bangkok");
        this.timeZones.put("(GMT+06:30) Rangoon", "Asia/Rangoon");
        this.timeZones.put("(GMT+06:00) Sri Jayawardenepura", "Asia/Colombo");
        this.timeZones.put("(GMT+06:00) Astana, Dhaka", "Asia/Dhaka");
        this.timeZones.put("(GMT+06:00) Almaty, Novosibirsk", "Asia/Almaty");
        this.timeZones.put("(GMT+05:45) Kathmandu", "Asia/Katmandu");
        this.timeZones.put("(GMT+05:30) Chennai, Kolkata, Mumbai, New Delhi", "Asia/Calcutta");
        this.timeZones.put("(GMT+05:00) Islamabad, Karachi, Tashkent", "Asia/Karachi");
        this.timeZones.put("(GMT+05:00) Ekaterinburg", "Asia/Yekaterinburg");
        this.timeZones.put("(GMT+04:30) Kabul", "Asia/Kabul");
        this.timeZones.put("(GMT+04:00) Baku, Tbilisi, Yerevan", "Asia/Baku");
        this.timeZones.put("(GMT+04:00) Abu Dhabi, Muscat", "Asia/Dubai");
        this.timeZones.put("(GMT+03:30) Tehran", "Asia/Tehran");
        this.timeZones.put("(GMT+03:00) Nairobi", "Africa/Nairobi");
        this.timeZones.put("(GMT+03:00) Moscow, St. Petersburg, Volgograd", "Europe/Moscow");
        this.timeZones.put("(GMT+03:00) Kuwait, Riyadh", "Asia/Kuwait");
        this.timeZones.put("(GMT+03:00) Baghdad", "Asia/Baghdad");
        this.timeZones.put("(GMT+02:00) Jerusalem", "Asia/Jerusalem");
        this.timeZones.put("(GMT+02:00) Helsinki, Kyiv, Riga, Sofia, Tallinn, Vilnius", "Europe/Helsinki");
        this.timeZones.put("(GMT+02:00) Harare, Pretoria", "Africa/Harare");
        this.timeZones.put("(GMT+02:00) Cairo", "Africa/Cairo");
        this.timeZones.put("(GMT+02:00) Bucharest", "Europe/Bucharest");
        this.timeZones.put("(GMT+02:00) Athens, Istanbul, Minsk", "Europe/Athens");
        this.timeZones.put("(GMT+01:00) West Central Africa", "Africa/Lagos");
        this.timeZones.put("(GMT+01:00) Sarajevo, Skopje, Warsaw, Zagreb", "Europe/Warsaw");
        this.timeZones.put("(GMT+01:00) Brussels, Copenhagen, Madrid, Paris", "Europe/Brussels");
        this.timeZones.put("(GMT+01:00) Belgrade, Bratislava, Budapest, Ljubljana, Prague", "Europe/Prague,Europe/Belgrade");
        this.timeZones.put("(GMT+01:00) Amsterdam, Berlin, Bern, Rome, Stockholm, Vienna", "Europe/Amsterdam");
        this.timeZones.put("(GMT) Casablanca, Monrovia", "Africa/Casablanca");
        this.timeZones.put("(GMT) Greenwich Mean Time : Dublin, Edinburgh, Lisbon, London", "Europe/Dublin");
        this.timeZones.put("(GMT-01:00) Azores", "Atlantic/Azores");
        this.timeZones.put("(GMT-01:00) Cape Verde Is.", "Atlantic/Cape_Verde");
        this.timeZones.put("(GMT-02:00) Mid-Atlantic", "Atlantic/South_Georgia");
        this.timeZones.put("(GMT-03:00) Brasilia", "America/Sao_Paulo");
        this.timeZones.put("(GMT-03:00) Buenos Aires, Georgetown", "America/Buenos_Aires");
        this.timeZones.put("(GMT-03:00) Greenland", "America/Thule");
        this.timeZones.put("(GMT-03:30) Newfoundland", "America/St_Johns");
        this.timeZones.put("(GMT-04:00) Atlantic Time (Canada)", "America/Halifax");
        this.timeZones.put("(GMT-04:00) Caracas, La Paz", "America/Caracas");
        this.timeZones.put("(GMT-04:00) Santiago", "America/Santiago");
        this.timeZones.put("(GMT-05:00) Bogota, Lima, Quito", "America/Bogota");
        this.timeZones.put("(GMT-05:00) Eastern Time (US & Canada)", " America/New_York");
        this.timeZones.put("(GMT-05:00) Indiana (East)", "America/Indianapolis");
        this.timeZones.put("(GMT-06:00) Central America", "America/Costa_Rica");
        this.timeZones.put("(GMT-06:00) Central Time (US & Canada)", "America/Chicago");
        this.timeZones.put("(GMT-06:00) Guadalajara, Mexico City, Monterrey", "America/Mexico_City");
        this.timeZones.put("(GMT-06:00) Saskatchewan", "America/Winnipeg");
        this.timeZones.put("(GMT-07:00) Arizona", "America/Phoenix");
        this.timeZones.put("(GMT-07:00) Chihuahua, La Paz, Mazatlan", "America/Tegucigalpa");
        this.timeZones.put("(GMT-07:00) Mountain Time (US & Canada)", "America/Denver");
        this.timeZones.put("(GMT-08:00) Pacific Time (US & Canada); Tijuana", "America/Los_Angeles");
        this.timeZones.put("(GMT-09:00) Alaska", "America/Anchorage");
        this.timeZones.put("(GMT-10:00) Hawaii", "Pacific/Honolulu");
        this.timeZones.put("(GMT-11:00) Midway Island, Samoa", "Pacific/Apia");
        this.timeZones.put("(GMT-12:00) International Date Line West", "MIT");
    }

    public Color getColor(int color) {
        return new Color(color);
    }

    public static final int BG_MGDL = 1;

    public static final int BG_MMOL = 2;

    public int getBGMeasurmentType() {
        return this.m_BG_unit;
    }

    public void setBGMeasurmentType(int type) {
        this.m_BG_unit = type;
    }

    private static final float MGDL_TO_MMOL_FACTOR = 0.0555f;

    private static final float MMOL_TO_MGDL_FACTOR = 18.016f;

    /**
     * Depending on the return value of <code>getBGMeasurmentType()</code>, either
     * return the mg/dl or the mmol/l value of the database's value. Default is mg/dl.
     * @param dbValue - The database's value (in float)
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
            if (output_type == DataAccessCGM.BG_MGDL) {
                return bg_value * DataAccessCGM.MGDL_TO_MMOL_FACTOR;
            } else {
                return bg_value * DataAccessCGM.MMOL_TO_MGDL_FACTOR;
            }
        }
    }

    public float getBGValueDifferent(int type, float bg_value) {
        if (type == DataAccessCGM.BG_MGDL) {
            return bg_value * DataAccessCGM.MGDL_TO_MMOL_FACTOR;
        } else {
            return bg_value * DataAccessCGM.MMOL_TO_MGDL_FACTOR;
        }
    }

    public I18nControl getI18nInstance() {
        return m_i18n;
    }

    /**
     *  Utils
     */
    public Image getImage(String filename, Component cmp) {
        Image img;
        InputStream is = this.getClass().getResourceAsStream(filename);
        if (is == null) System.out.println("Error reading image: " + filename);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            int c;
            while ((c = is.read()) >= 0) baos.write(c);
            if (cmp == null) cmp = new JLabel();
            img = cmp.getToolkit().createImage(baos.toByteArray());
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return img;
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

    public int getStartYear() {
        return 1800;
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

    public GregorianCalendar getGregorianCalendar(Date date) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        return gc;
    }

    public static void notImplemented(String source) {
        System.out.println("Not Implemented: " + source);
    }

    @Override
    public HibernateDb getHibernateDb() {
        return null;
    }
}
