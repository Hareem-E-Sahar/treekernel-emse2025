package net.updesa.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Útilidades para el manejo de fechas y calendarios.
 *
 * @author jsolorzano
 */
public class DateUtils {

    private static final DateFormat utilDateFormatter = new SimpleDateFormat("dd-MM-yyyy");

    private static final DateFormat sqlDateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    public static String getActualYear() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy");
        return df.format(new Date());
    }

    public static String getActualMonth() {
        SimpleDateFormat df = new SimpleDateFormat("MM");
        return df.format(new Date());
    }

    public static String getActualDay() {
        SimpleDateFormat df = new SimpleDateFormat("dd");
        return df.format(new Date());
    }

    public static String getActualIsoDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(new Date());
    }

    public static String getActualIsoMonth() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM");
        return df.format(new Date());
    }

    public static int getLastDayOfMonth(int year, int month) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static java.sql.Date utilDateToSqlDate(java.util.Date uDate) throws ParseException {
        return java.sql.Date.valueOf(sqlDateFormatter.format(uDate));
    }

    public static java.util.Date sqlDateToutilDate(java.sql.Date sDate) throws ParseException {
        return (java.util.Date) utilDateFormatter.parse(utilDateFormatter.format(sDate));
    }
}
