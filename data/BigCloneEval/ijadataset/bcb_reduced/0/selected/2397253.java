package org.caisi.utility.PMmodule;

import org.apache.log4j.Category;
import java.text.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtils {

    static Category cat = Category.getInstance(DateUtils.class.getName());

    private static SimpleDateFormat sdf;

    private static String formatDate = "dd/MM/yyyy";

    public static SimpleDateFormat getDateFormatter() {
        if (sdf == null) {
            sdf = new SimpleDateFormat(formatDate);
        }
        return sdf;
    }

    public static void setDateFormatter(String pattern) {
        sdf = new SimpleDateFormat(pattern);
    }

    public static String getDate() {
        Date date = new Date();
        return DateFormat.getDateInstance().format(date);
    }

    public static String getDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat();
        return sdf.format(date);
    }

    public static String getCurrentDateOnlyStr(String delimiter) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        String monthStr = String.valueOf(month);
        String dayStr = String.valueOf(day);
        if (monthStr.length() <= 1) {
            monthStr = "0" + monthStr;
        }
        if (dayStr.length() <= 1) {
            dayStr = "0" + dayStr;
        }
        String dateStr = year + delimiter + monthStr + delimiter + dayStr;
        return dateStr;
    }

    public static String getDateTime() {
        Date date = new Date();
        return DateFormat.getDateTimeInstance().format(date);
    }

    /** Compara uma data com a data atual.
   * @param pDate Data que ser� comparada com a data atual.

   * @param format Formato da data. Ex: dd/MM/yyyy, yyyy-MM-dd

   * @return  1 - se a data for maior que a data atual.

   * -1 - se a data for menor que a data atual.

   * 0 - se a data for igual que a data atual.

   * -2 - se ocorrer algum erro.

   */
    public static String compareDate(String pDate, String format) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
        try {
            Date date = df.parse(pDate);
            cat.debug("[DateUtils] - compareDate: date = " + date.toString());
            String sNow = DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date());
            Date now = df.parse(sNow);
            cat.debug("[DateUtils] - compareDate: now = " + now.toString());
            if (date.after(now)) {
                cat.debug("[DateUtils] - compareDate: 1");
                return "1";
            } else if (date.before(now)) {
                cat.debug("[DateUtils] - compareDate: -1");
                return "-1";
            } else {
                cat.debug("[DateUtils] - compareDate: 0");
                return "0";
            }
        } catch (ParseException e) {
            cat.error("[DateUtils] - compareDate: -2", e);
            return "-2";
        }
    }

    /** Compara uma data com outra.
   * @param pDate Data que ser� comparada com pDate2.

   * * @param pDate2 Data que ser� comparada com pDate.

   * @param format Formato da data. Ex: dd/MM/yyyy, yyyy-MM-dd

   * @return  1 - se a data for maior que a data atual.

   * -1 - se a data for menor que a data atual.

   * 0 - se a data for igual que a data atual.

   * -2 - se ocorrer algum erro.

   */
    public static String compareDate(String pDate, String pDate2, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        try {
            Date date = df.parse(pDate);
            cat.debug("[DateUtils] - compareDate: date = " + date.toString());
            Date now = df.parse(pDate2);
            cat.debug("[DateUtils] - compareDate: now = " + now.toString());
            if (date.after(now)) {
                cat.debug("[DateUtils] - compareDate: 1");
                return "1";
            } else if (date.before(now)) {
                cat.debug("[DateUtils] - compareDate: -1");
                return "-1";
            } else {
                cat.debug("[DateUtils] - compareDate: 0");
                return "0";
            }
        } catch (ParseException e) {
            cat.error("[DateUtils] - compareDate: -2", e);
            return "-2";
        }
    }

    public static String formatDate(String date, String format, String formatAtual) {
        try {
            setDateFormatter(formatAtual);
            Date data = getDateFormatter().parse(date);
            cat.debug("[DateUtils] - formatDate: data formatada: " + getDateFormatter().format(data));
            setDateFormatter(format);
            return getDateFormatter().format(data);
        } catch (ParseException e) {
            cat.error("[DateUtils] - formatDate: ", e);
        }
        return "";
    }

    public static String formatDate(String date, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat();
            Date data = sdf.parse(date);
            cat.debug("[DateUtils] - formatDate: data formatada: " + sdf.format(data));
            setDateFormatter(format);
            return getDateFormatter().format(data);
        } catch (ParseException e) {
            cat.error("[DateUtils] - formatDate: ", e);
        }
        return "";
    }

    public static String sumDate(String format, String pSum) {
        int iSum = new Integer(pSum).intValue();
        cat.debug("[DateUtils] - sumDate: iSum = " + iSum);
        Calendar calendar = new GregorianCalendar();
        Date now = new Date();
        calendar.setTime(now);
        calendar.add(Calendar.DATE, iSum);
        Date data = calendar.getTime();
        setDateFormatter(format);
        return getDateFormatter().format(data);
    }

    public String NextDay(int day, int month, int year) {
        boolean leapyear;
        System.out.println("Entered Date: " + year + "-" + month + "-" + day);
        switch(month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
                if (day < 31) {
                    day++;
                } else {
                    day = 1;
                    month++;
                }
                break;
            case 12:
                if (day < 31) {
                    day++;
                } else {
                    day = 1;
                    month = 1;
                    year++;
                }
                break;
            case 2:
                if (day < 28) {
                    day++;
                } else {
                    if (((year % 4 == 0) && !(year % 100 == 0)) || (year % 400 == 0)) {
                        leapyear = true;
                    } else {
                        leapyear = false;
                    }
                    if (leapyear == true) {
                        if (day == 28) {
                            day++;
                        } else {
                            day = 1;
                            month++;
                        }
                    } else {
                        day = 1;
                        month++;
                    }
                }
                break;
            default:
                if (day < 30) {
                    day++;
                } else {
                    day = 1;
                    month++;
                }
        }
        String nextDay = year + "-" + month + "-" + day;
        System.out.println("next day: " + nextDay);
        return nextDay;
    }

    public String NextDay(int day, int month, int year, int numDays) {
        boolean leapyear;
        int modValue = 28;
        System.out.println("Entered Date: " + year + "-" + month + "-" + day);
        while (numDays > 0) {
            int curNumDays = numDays % modValue;
            if (curNumDays == 0) {
                curNumDays = modValue;
            }
            switch(month) {
                case 1:
                case 3:
                case 5:
                case 7:
                case 8:
                case 10:
                    if (day + curNumDays < 31) {
                        day = day + curNumDays;
                    } else if (((day + curNumDays) % 31) == 0) {
                        day = 31;
                    } else {
                        day = ((day + curNumDays) % 31);
                        month++;
                    }
                    break;
                case 12:
                    if (day + curNumDays < 31) {
                        day = day + curNumDays;
                    } else if (((day + curNumDays) % 31) == 0) {
                        day = 31;
                    } else {
                        day = ((day + curNumDays) % 31);
                        month = 1;
                        year++;
                    }
                    break;
                case 2:
                    if (((year % 4 == 0) && !(year % 100 == 0)) || (year % 400 == 0)) {
                        if (day + curNumDays < 29) {
                            day = day + curNumDays;
                        } else if (((day + curNumDays) % 29) == 0) {
                            day = 29;
                        } else {
                            day = ((day + curNumDays) % 29);
                            month++;
                        }
                    } else {
                        if (day + curNumDays < 28) {
                            day = day + curNumDays;
                        } else if (((day + curNumDays) % 28) == 0) {
                            day = 28;
                        } else {
                            day = ((day + curNumDays) % 28);
                            month++;
                        }
                    }
                    break;
                default:
                    if (day + curNumDays < 30) {
                        day = day + curNumDays;
                    } else if (((day + curNumDays) % 30) == 0) {
                        day = 30;
                    } else {
                        day = ((day + curNumDays) % 30);
                        month++;
                    }
            }
            numDays = numDays - curNumDays;
            System.out.println("curNumDays: " + curNumDays + " ; numDays: " + numDays);
        }
        String nextDay = year + "-" + month + "-" + day;
        System.out.println("next few day: " + nextDay);
        return nextDay;
    }

    /**
   *Gets the difference between two dates, in days.
   *Takes two dates represented in milliseconds and returns the difference in days
   */
    public int getDifDays(long greater, long lesser) {
        double x = (greater - lesser) / 86400000;
        int ret = (int) x;
        return ret;
    }
}
