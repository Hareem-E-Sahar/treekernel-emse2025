package com.vsimtone.pub.utils;

import java.util.*;
import java.text.*;

public class DateUtil {

    private static char seperator = '-';

    private static long adjustTime = 0;

    private DateUtil() {
    }

    /**
   * 把格式为 yyyy*mm*dd hh:mm:ss或yyyy*mm*dd hh:mm或yyyy*mm*dd hh的日期时间转换成毫秒数。
   * (*)可以是任意字符
   * @param strDate String 要被转换的时间字符串
   * @return long 转换之后的时间毫秒数
   */
    public static long getDateMillis(String strDate) {
        if (strDate == null || (strDate != null && strDate.trim().equals(""))) return -1;
        String sDate = strDate;
        String sTime = null;
        String sep = " ";
        StringTokenizer st = null;
        long millis = 0;
        st = new StringTokenizer(strDate, sep);
        if (st.hasMoreTokens()) {
            sDate = st.nextToken();
        }
        if (st.hasMoreTokens()) {
            sTime = st.nextToken();
        }
        int year = Integer.parseInt(sDate.substring(0, 4));
        int month = Integer.parseInt(sDate.substring(5, 7));
        int day = Integer.parseInt(sDate.substring(8));
        int hour = 0;
        int min = 0;
        int sec = 0;
        if (sTime != null) {
            st = new StringTokenizer(sTime, ":");
            if (st.hasMoreTokens()) {
                hour = Integer.parseInt(st.nextToken());
            }
            if (st.hasMoreTokens()) {
                min = Integer.parseInt(st.nextToken());
            }
            if (st.hasMoreTokens()) {
                sec = Integer.parseInt(st.nextToken());
            }
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, hour, min, sec);
        return calendar.getTimeInMillis();
    }

    /**
   * 把格式为 yyyy/mm/dd 或 yyyy-mm-dd 的日期转换成毫秒数。
   * 或者把格式为 yyyy/mm/dd hh:mm:ss 或 yyyy-mm-dd hh:mm:ss 的日期时间转换成毫秒数。
   * @param strDate 表示一个日期字符串，格式为 yyyy/mm/dd 或 yyyy-mm-dd 。
   * @return 返回距离1970年1月1日午夜0点的毫秒数。
   */
    public static long getMillis(String strDate) {
        if (strDate == null || (strDate != null && strDate.trim().equals(""))) return -1;
        String sDate = strDate;
        String sTime = null;
        String sep = " ";
        StringTokenizer st = null;
        long millis = 0;
        if (strDate.indexOf(":") > 0) {
            st = new StringTokenizer(strDate, sep);
            sDate = st.nextToken();
            sTime = st.nextToken();
        }
        sDate = getFormatDate(sDate);
        sDate = sDate.substring(0, 4) + sDate.substring(5, 7) + sDate.substring(8);
        if (sTime != null) sTime = sTime.substring(0, 2) + sTime.substring(3, 5) + sTime.substring(6); else sTime = "000000";
        String strDateTime = sDate + sTime;
        return Long.parseLong(strDateTime);
    }

    /**
   * 把数字日期转换成格式为 yyyy-mm-dd的日期。
   * @param millis 距离1970年1月1日午夜0点的毫秒数
   * @return 返回格式为 yyyy-mm-dd的字符日期和时间。
   */
    public static String getDate(long millis) {
        if (millis <= 0) return "";
        Calendar rightNow = getCalendar();
        rightNow.setTime(new java.util.Date(millis));
        int year = rightNow.get(Calendar.YEAR);
        int month = rightNow.get(Calendar.MONTH) + 1;
        int day = rightNow.get(Calendar.DAY_OF_MONTH);
        int hour = rightNow.get(Calendar.HOUR_OF_DAY);
        int minute = rightNow.get(Calendar.MINUTE);
        int second = rightNow.get(Calendar.SECOND);
        String strDateTime = year + "-" + (month < 10 ? "0" + month + "-" : month + "-") + (day < 10 ? "0" + day : day + "");
        return strDateTime;
    }

    /**
   * 把数字日期转换成格式为 yyyymmdd 的日期。
   * @param millis 距离1970年1月1日午夜0点的毫秒数
   * @return 返回格式为 yyyymmdd的字符日期和时间。
   */
    public static String getDate1(long millis) {
        if (millis <= 0) return "";
        Calendar rightNow = getCalendar();
        rightNow.setTime(new java.util.Date(millis));
        int year = rightNow.get(Calendar.YEAR);
        int month = rightNow.get(Calendar.MONTH) + 1;
        int day = rightNow.get(Calendar.DAY_OF_MONTH);
        int hour = rightNow.get(Calendar.HOUR_OF_DAY);
        int minute = rightNow.get(Calendar.MINUTE);
        int second = rightNow.get(Calendar.SECOND);
        String strDateTime = year + (month < 10 ? "0" + month : month + "") + (day < 10 ? "0" + day : day + "");
        return strDateTime;
    }

    /**
   * 把毫秒数转换成一个日期和时间的字符串，格式为：yyyy-mm-dd hh:mm:ss。
   * @param millis 距离1970年1月1日午夜0点的毫秒数
   * @return 返回格式为 yyyy-mm-dd hh:mm:ss 的字符日期和时间。
   */
    public static String getDateTime(long millis) {
        if (millis <= 0) return "";
        Calendar rightNow = getCalendar();
        rightNow.setTime(new java.util.Date(millis));
        int year = rightNow.get(Calendar.YEAR);
        int month = rightNow.get(Calendar.MONTH) + 1;
        int day = rightNow.get(Calendar.DAY_OF_MONTH);
        int hour = rightNow.get(Calendar.HOUR_OF_DAY);
        int minute = rightNow.get(Calendar.MINUTE);
        int second = rightNow.get(Calendar.SECOND);
        String strDateTime = year + "-" + (month < 10 ? "0" + month + "-" : month + "-") + (day < 10 ? "0" + day + " " : day + " ") + (hour < 10 ? "0" + hour + ":" : hour + ":") + (minute < 10 ? "0" + minute + ":" : minute + ":") + (second < 10 ? "0" + second : second + "");
        return strDateTime;
    }

    /**
 * 缩短时间字符串 在短信中节省字数  add by hehy
 * 把毫秒数转换成一个较短的日期和时间的字符串，格式为：mm-dd hh:mm。
 * @param millis 距离1970年1月1日午夜0点的毫秒数
 * @return 返回格式为mm-dd hh:mm 的字符日期和时间。
 */
    public static String getSimpleDateTime(long millis) {
        if (millis <= 0) return "";
        Calendar rightNow = getCalendar();
        rightNow.setTime(new java.util.Date(millis));
        int month = rightNow.get(Calendar.MONTH) + 1;
        int day = rightNow.get(Calendar.DAY_OF_MONTH);
        int hour = rightNow.get(Calendar.HOUR_OF_DAY);
        int minute = rightNow.get(Calendar.MINUTE);
        String strDateTime = (month < 10 ? "0" + month + "-" : month + "-") + (day < 10 ? "0" + day + " " : day + " ") + (hour < 10 ? "0" + hour + ":" : hour + ":") + (minute < 10 ? "0" + minute : minute + "");
        return strDateTime;
    }

    /**
   * 返回当前日期的字符形式，格式为：yyyy-mm-dd
   * @return 当天的yyyy-mm-dd格式
   */
    public static String getToday() {
        Calendar rightNow = getCalendar();
        int year = rightNow.get(Calendar.YEAR);
        int month = rightNow.get(Calendar.MONTH) + 1;
        int day = rightNow.get(Calendar.DAY_OF_MONTH);
        return year + String.valueOf(seperator) + (month < 10 ? "0" + month : month + "") + String.valueOf(seperator) + (day < 10 ? "0" + day : day + "");
    }

    /**
   * 返回MMDDYY格式的当前日期。例如:假设当天是2004年8月11日，则返回的是08112004
   * @return String 当前日期的MMDDYY格式
   */
    public static String getMMDDYY() {
        String strYY = ("" + getYear()).substring(2);
        int intMM = getMonth();
        String strMM = null;
        if (intMM < 10) {
            strMM = "0" + intMM;
        } else {
            strMM = "" + intMM;
        }
        String strDD = null;
        int intDD = getDay();
        if (intDD < 10) {
            strDD = "0" + intDD;
        } else {
            strDD = "" + intDD;
        }
        return strMM + strDD + strYY;
    }

    /**
   * 返回当前年份
   * @return 当前的年份
   */
    public static int getYear() {
        Calendar rightNow = getCalendar();
        return rightNow.get(Calendar.YEAR);
    }

    /**
   * 返回当前月份
   * @return int 当前的月份
   */
    public static int getMonth() {
        Calendar rightNow = getCalendar();
        return rightNow.get(Calendar.MONTH) + 1;
    }

    /**
   * 返回当前在一月中的天数
   * @return int 当天的日期数
   */
    public static int getDay() {
        Calendar rightNow = getCalendar();
        return rightNow.get(Calendar.DAY_OF_MONTH);
    }

    /**
   * 返回当前星期几
   * @return int 当前星期几
   */
    public static int getWeekDay() {
        Calendar rightNow = getCalendar();
        return rightNow.get(Calendar.DAY_OF_WEEK) - 1;
    }

    /**
   * 返回指定日期的年份
   * @param millis 数字日期
   * @return int 年份
   */
    public static int getYear(long millis) {
        String strDate = String.valueOf(millis);
        String year = strDate.substring(0, 4);
        return Integer.parseInt(year);
    }

    /**
   * 返回指定日期的月份
   * @param millis 数字日期
   * @return int 月份
   */
    public static int getMonth(long millis) {
        String strDate = String.valueOf(millis);
        String month = strDate.substring(4, 6);
        return Integer.parseInt(month);
    }

    /**
   * 返回指定日期所在月份中的天数
   * @param millis 数字日期
   * @return int 天数
   */
    public static int getDay(long millis) {
        if (millis <= 0) return -1;
        String strDate = String.valueOf(millis);
        String day = strDate.substring(6, 8);
        return Integer.parseInt(day);
    }

    /**
   * 判断两个毫秒之间相差多少天
   * @param millis1 毫秒数表示的第一个时间。
   * @param millis2 毫秒数表示的第二个时间。
   * @return 返回两个时间相差的天数。
   * @return int 相差的天数
   */
    public static int getIntervalDays(long millis1, long millis2) {
        long beginMillis1 = DateUtil.getOneDayBeginMillis(millis1);
        long beginMillis2 = DateUtil.getOneDayBeginMillis(millis2);
        long realMillis1 = getDateMillis(beginMillis1);
        long realMillis2 = getDateMillis(beginMillis2);
        return Math.round((realMillis1 - realMillis2) / 86400000);
    }

    /**
   * 给出一个基准时间和一个与基准时间相差的天数，
   * 得到一个提前或落后的时间
   * @param baseMillis 数字日期型的基准时间
   * @param days 与基准时间相差的天数
   * @return 提前或落后的时间毫秒数
   */
    public static long getAdvancedMillis(long baseMillis, int days) {
        long realMillis = getDateMillis(baseMillis);
        realMillis += days * 86400000l;
        return getLongDateTime(realMillis);
    }

    /**
   * 给出一个基准时间和一个与基准时间相差的天数，
   * 得到一个提前或落后的时间
   * @param baseDate 字符串日期型的基准时间
   * @param days 与基准时间相差的天数
   * @return 提前或落后的时间毫秒数
   */
    public static long getAdvancedMillis(String baseDate, int days) {
        long realMillis = dateMillis(baseDate);
        realMillis += days * 86400000l;
        return getLongDateTime(realMillis);
    }

    /**
   * 取得一天的起始数字日期
   * @param millis 数字日期
   * @return long 起始时间的毫秒数
   */
    public static long getOneDayBeginMillis(long millis) {
        if (millis <= 0) return millis;
        String strDate = String.valueOf(millis);
        if (strDate.length() < 8) return -1;
        strDate = strDate.substring(0, 8) + "000000";
        return Long.parseLong(strDate);
    }

    /**
   * 取得一天的终止数字日期
   * @param millis 数字日期
   * @return long 终止时间的毫秒数
   */
    public static long getOneDayEndMillis(long millis) {
        if (millis <= 0) return millis;
        String strDate = String.valueOf(millis);
        if (strDate.length() < 8) return -1;
        strDate = strDate.substring(0, 8) + "235959";
        return Long.parseLong(strDate);
    }

    /**
   * 取得一天的起始数字日期
   * @param strDate 日期字符串，形式为：yyyy-mm-dd 或 yyyy/mm/dd
   * @return long 起止时间的毫秒数
   */
    public static long getOneDayBeginMillis(String strDate) {
        if (strDate == null || (strDate != null && strDate.trim().equals(""))) return -1;
        int len = strDate.length();
        if (len < 4) return -1; else if (len == 4) strDate = strDate + "0101000000"; else if (len <= 7) strDate = strDate.substring(0, 4) + strDate.substring(5, 7) + "01000000"; else strDate = strDate.substring(0, 4) + strDate.substring(5, 7) + strDate.substring(8, 10) + "000000";
        return Long.parseLong(strDate);
    }

    /**
   * 取得一天的终止数字日期
   * @param strDate 日期字符串，形式为：yyyy-mm-dd 或 yyyy/mm/dd
   * @return long 终止时间的毫秒数
   */
    public static long getOneDayEndMillis(String strDate) {
        if (strDate == null || (strDate != null && strDate.trim().equals(""))) return -1;
        int len = strDate.length();
        if (len < 4) return -1; else if (len == 4) strDate = strDate + "1231235959"; else strDate = strDate.substring(0, 4) + strDate.substring(5, 7) + strDate.substring(8, 10) + "235959";
        return Long.parseLong(strDate);
    }

    /**
   * 将非标准日期格式转化为标准日期格式（标准日期格式：yyyy-mm-dd）
   * @param strDate String 非标准日期格式日期字符串
   * @return String 标准日期格式日期字符串
   */
    public static String getFormatDate(String strDate) {
        String sep = null;
        if (strDate.indexOf("-") > 0) sep = "-"; else if (strDate.indexOf("/") > 0) sep = "/";
        StringTokenizer st = new StringTokenizer(strDate, sep);
        String year = st.nextToken();
        String month = st.nextToken();
        String day = st.nextToken();
        if (year == null || month == null || day == null) return "";
        if (year.length() == 2) {
            if (year.compareTo("70") >= 0) year = "19" + year; else year = "20" + year;
        }
        if (month.length() == 1) month = "0" + month;
        if (day.length() == 1) day = "0" + day;
        return year + "-" + month + "-" + day;
    }

    /**
   * 判断某年是否为润年
   *
   * @param  year 年（整形）
   * @return ture，表示未润年；false表示不是润年
   */
    public static boolean isLeapYear(int year) {
        if ((year % 4 == 0) && (year % 100 != 0) || (year % 400 == 0)) {
            return true;
        } else {
            return false;
        }
    }

    /**
   * 判断某年是否为润年
   *
   * @param  year 年（整形）
   * @return ture，表示未润年；false表示不是润年
   */
    public static boolean isRunNian(int year) {
        if ((year % 4 == 00) && (year % 100 != 0) || (year % 400 == 0)) {
            return true;
        } else {
            return false;
        }
    }

    /**
   * 返回某年某个月的天数
   *
   * @param year 年份
   * @param month 月份
   */
    public static int getDaySumOfMonth(int year, int month) {
        int days = 0;
        switch(month) {
            case 1:
                days = 31;
                break;
            case 2:
                if (DateUtil.isLeapYear(year)) days = 29; else days = 28;
                break;
            case 3:
                days = 31;
                break;
            case 4:
                days = 30;
                break;
            case 5:
                days = 31;
                break;
            case 6:
                days = 30;
                break;
            case 7:
                days = 31;
                break;
            case 8:
                days = 31;
                break;
            case 9:
                days = 30;
                break;
            case 10:
                days = 31;
                break;
            case 11:
                days = 30;
                break;
            case 12:
                days = 31;
                break;
        }
        return days;
    }

    /**
   * 获取本的的当前时间的毫秒数
   * @return long 当前时间的毫秒数
   */
    public static long currentSystemTimeMillis() {
        long currentSystemTime = getLongDateTime(System.currentTimeMillis() + 8 * 60 * 60 * 1000 + adjustTime);
        return currentSystemTime;
    }

    /**
   * 获取本的的当前时间的毫秒数
   * @return long 当前时间的毫秒数
   */
    public static long getLocalCurrentTimeMillis() {
        return getLongDateTime(System.currentTimeMillis() + 8 * 60 * 60 * 1000);
    }

    /**
   * 把毫秒数转换成数字日期和时间。
   * @param millis 距离1970年1月1日午夜0点的毫秒数。
   * @return 返回格式为 yyyymmddhhmmss 的字符日期和时间。
   */
    public static long getLongDateTime(long millis) {
        if (millis <= 0) return -1;
        Calendar rightNow = getCalendar();
        rightNow.setTime(new java.util.Date(millis));
        int year = rightNow.get(Calendar.YEAR);
        int month = rightNow.get(Calendar.MONTH) + 1;
        int day = rightNow.get(Calendar.DAY_OF_MONTH);
        int hour = rightNow.get(Calendar.HOUR_OF_DAY);
        int minute = rightNow.get(Calendar.MINUTE);
        int second = rightNow.get(Calendar.SECOND);
        String strDateTime = year + (month < 10 ? "0" + month : month + "") + (day < 10 ? "0" + day : day + "") + (hour < 10 ? "0" + hour : hour + "") + (minute < 10 ? "0" + minute : minute + "") + (second < 10 ? "0" + second : second + "");
        return Long.parseLong(strDateTime);
    }

    /**
   * 返回本月第一天的日期。
   * @param strDate 字符型日期（格式：yyyy-mm-dd）
   * @return 返回格式：yyyy-mm-dd
   */
    public static String getFirstDateOfMonth(String strDate) {
        return strDate.substring(0, 8) + "01";
    }

    /**
   * 返回本月最后一天的日期，返回格式（yyyy-mm-dd）
   * @param strDate 字符型日期（格式：yyyy-mm-dd）
   * @return 返回格式：yyyy-mm-dd
   */
    public static String getLastDateOfMonth(String strDate) {
        long millis = getMillis(strDate);
        int year = getYear(millis);
        int month = getMonth(millis);
        int day = getDaySumOfMonth(year, month);
        return strDate.substring(0, 8) + day;
    }

    /**
   * 返回上月第一天的日期。
   * @param strDate 字符型日期（格式：yyyy-mm-dd）
   * @return 返回格式：yyyy-mm-dd
   */
    public static String getFirstDateOfLastMonth(String strDate) {
        long millis = getMillis(strDate);
        int year = getYear(millis);
        int month = getMonth(millis);
        int lastMonth = month - 1;
        if (lastMonth == 0) {
            lastMonth = 12;
            year = year - 1;
        }
        return String.valueOf(year) + "-" + (lastMonth > 9 ? String.valueOf(lastMonth) : "0" + lastMonth) + "-01";
    }

    /**
   * 返回上月最后一天的日期。
   * @param strDate 字符型日期（格式：yyyy-mm-dd）
   * @return 返回格式：yyyy-mm-dd
   */
    public static String getLastDateOfLastMonth(String strDate) {
        long millis = getMillis(strDate);
        int year = getYear(millis);
        int month = getMonth(millis);
        month = month - 1;
        if (month == 0) {
            month = 12;
            year = year - 1;
        }
        int day = getDaySumOfMonth(year, month);
        return "" + year + "-" + ((month > 9 ? String.valueOf(month) : "0" + month)) + "-" + String.valueOf(day);
    }

    /**
   * 给出一个基准时间和一个与基准时间相差的天数，
   * 得到一个提前或落后的时间
   * @param baseDate 字符串日期型的基准时间
   * @param days 与基准时间相差的天数（正/负）
   * @param 返回字符型日期（格式为：yyyy-mm-dd）
   */
    public static String getAdvancedDate(String baseDate, int days) {
        long realMillis = dateMillis(baseDate);
        realMillis += days * 86400000l;
        return getDate(getLongDateTime(realMillis));
    }

    /**
   * 将一定格式的日期字符串恢复成日期类型。
   * @param date String 表示日期数据的字符串
   * @param format String 日期字符串的格式
   * @return Date 返回的日期类型数据
   */
    public static java.util.Date parseDate(String date, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        ParsePosition pos = new ParsePosition(0);
        java.util.Date d = formatter.parse(date, pos);
        return d;
    }

    /**
   * 将日期转换成指定格式的字符串.
   * @param d Date 要被转换的日期数据
   * @param format String 指定的转换格式
   * @return String 表示转换之后的日期字符串
   */
    public static String format(Date d, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        String s = formatter.format(d);
        return s;
    }

    /**
   * 将日期转换成字符串格式，默认格式为：yyyyMMddHHmmssS
   * @param d Date 要被转换的日期数据
   * @return String 表示转换之后的日期字符串
   */
    public static String format(Date d) {
        return format(d, "yyyyMMddHHmmssS");
    }

    /**
   * 分解时间字符串，转换成秒
   * @param timestr String 形如22-11:06:23 或00:08。
   * @return long
   */
    public static long parseTimeToSecond(String timestr) {
        long time = 0L;
        try {
            if (timestr.indexOf(":") != -1) {
                String[] times = StringUtil.splitStr(timestr, ":");
                if (times != null && times.length == 3) {
                    if (!(times[0].indexOf("-") == -1)) {
                        String[] days = times[0].split("-");
                        time = Long.parseLong(days[0]) * 24 * 3600 + Long.parseLong(days[1]) * 3600;
                    } else {
                        time = Long.parseLong(times[0]) * 3600;
                    }
                    time += Long.parseLong(times[1]) * 60 + Long.parseLong(times[2]);
                } else if (times.length == 2) {
                    time = Long.parseLong(times[0]) * 60 + Long.parseLong(times[1]);
                }
            } else {
                return Long.parseLong(timestr);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return Long.MIN_VALUE;
        }
        return time;
    }

    /**
   * 把秒转换成时间字符串
   * @param second long
   * @return String
   */
    public static String parseSecondToTime(long second) {
        String strReturn = "";
        long DAYSEC = 3600 * 24;
        long HOURSEC = 3600;
        long MINSEC = 60;
        int day = 0;
        int hour = 0;
        int min = 0;
        int sec = 0;
        {
            day = (int) (second / DAYSEC);
            long sectemp = second % DAYSEC;
            hour = (int) (sectemp / HOURSEC);
            sectemp = sectemp % HOURSEC;
            min = (int) (sectemp / MINSEC);
            sec = (int) (sectemp % MINSEC);
        }
        if (day > 0) {
            strReturn += day + "-";
        }
        if (hour > 9) {
            strReturn += hour;
        } else {
            strReturn += "0" + hour;
        }
        if (min > 9) {
            strReturn += ":" + min;
        } else {
            strReturn += ":0" + min;
        }
        if (sec > 9) {
            strReturn += ":" + sec;
        } else {
            strReturn += ":0" + sec;
        }
        return strReturn;
    }

    /**************************** private methods ****************************/
    private static Calendar getCalendar() {
        return Calendar.getInstance();
    }

    /**
   * 把数字日期（yyyymmddhhmmss）转换成毫秒数。
   */
    public static long getDateMillis(long millis) {
        return dateMillis(getDateTime(millis));
    }

    /**
   * 把格式为 yyyy/mm/dd 或 yyyy-mm-dd 的日期转换成毫秒数。
   * 或者把格式为 yyyy/mm/dd hh:mm:ss 或 yyyy-mm-dd hh:mm:ss 的日期时间转换成毫秒数。
   * @param strDate 表示一个日期字符串，格式为 yyyy/mm/dd 或 yyyy-mm-dd 。
   * @return 返回距离1970年1月1日午夜0点的毫秒数。
   */
    private static long dateMillis(String strDate) {
        if (strDate == null || (strDate != null && strDate.trim().equals(""))) return -1;
        String sDate = strDate;
        String sTime = null;
        String sep = " ";
        StringTokenizer st = null;
        long millis = 0;
        if (strDate.indexOf(":") > 0) {
            st = new StringTokenizer(strDate, sep);
            sDate = st.nextToken();
            sTime = st.nextToken();
        }
        if (sTime != null) {
            st = new StringTokenizer(sTime, ":");
            int hour = Integer.parseInt(st.nextToken());
            int minute = Integer.parseInt(st.nextToken());
            int second = Integer.parseInt(st.nextToken());
            millis = hour * 60 * 60 * 1000 + minute * 60 * 1000 + second * 1000;
        }
        try {
            sDate = getFormatDate(sDate);
            sDate = sDate.toString().replace('/', seperator);
            if (sDate.equals("1970-01-01")) millis += 0; else millis = java.sql.Date.valueOf(sDate).getTime() + 28800000 + millis;
            return millis;
        } catch (Throwable e) {
        }
        return -1;
    }

    /**
   * add by wangcp
   * 适应bs的初始化查询条件日期时间的方法
   * para dataFormat: yyyy-MM-dd HH:mm、 yyyy-MM-dd等格式
   * 返回当时系统时间
   */
    public static String getNowDate(String dataFormat) {
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat(dataFormat);
        Date odate = new Date();
        Date sdate = new Date(odate.getTime());
        return dateFormat.format(sdate);
    }

    /**
   * 获取离现在N天前的方法
   * @param preDay
   * @param dataFormat
   * @return
   */
    public static String getPreDate(int preDay, String dataFormat) {
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat(dataFormat);
        long currentTime = System.currentTimeMillis();
        long preTime = currentTime - 86400000;
        Date odate = new Date(preTime);
        return dateFormat.format(odate);
    }

    public static String getFirstDayOfMonth(String dataFormat) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return format(cal.getTime(), dataFormat);
    }

    /**
   * 将形如20080808200808 字符串转换成 2008-08-08 20:08:08
   * 将形如YYYYMMddHHmmss 字符串转换成 YYYY-MM-dd HH:mm:ss
   * by liulb 2008-08-21 
   * @param timeStr
   * @return
   */
    public static String formatTimeString(String timeStr) {
        StringBuffer sb = new StringBuffer();
        if (timeStr.length() == 14) {
            String YYYY = timeStr.substring(0, 4);
            String MM = timeStr.substring(4, 6);
            String dd = timeStr.substring(6, 8);
            String HH = timeStr.substring(8, 10);
            String mm = timeStr.substring(10, 12);
            String ss = timeStr.substring(12, 14);
            sb.append(YYYY + "-" + MM + "-" + dd + " " + HH + ":" + mm + ":" + ss);
        } else {
            sb.append("非法时间串");
        }
        return sb.toString();
    }

    /**
   * 将形如2008080820 字符串转换成 2008-08-08 20时
   * 将形如YYYYMMddHHmmss 字符串转换成 YYYY-MM-dd HH时
   * by liulb 2008-08-21 
   * @param timeStr
   * @return
   */
    public static String formatTimeStringOfHour(String timeStr) {
        StringBuffer sb = new StringBuffer();
        if (timeStr.length() == 10) {
            String YYYY = timeStr.substring(0, 4);
            String MM = timeStr.substring(4, 6);
            String dd = timeStr.substring(6, 8);
            String HH = timeStr.substring(8, 10);
            sb.append(YYYY + "-" + MM + "-" + dd + " " + HH + "时");
        } else {
            sb.append("非法时间串");
        }
        return sb.toString();
    }

    /**
   * 将形如2008080820 字符串转换成 2008-08-08 
   * 将形如YYYYMMddHHmmss 字符串转换成 YYYY-MM-dd 
   * by liulb 2008-08-21 
   * @param timeStr
   * @return
   */
    public static String formatTimeStringOfDay(String timeStr) {
        StringBuffer sb = new StringBuffer();
        if (timeStr.length() == 8) {
            String YYYY = timeStr.substring(0, 4);
            String MM = timeStr.substring(4, 6);
            String dd = timeStr.substring(6, 8);
            sb.append(YYYY + "-" + MM + "-" + dd);
        } else {
            sb.append("非法时间串");
        }
        return sb.toString();
    }

    /**
	 * 将日期utc格式字符串转换为指定格式的字符串
	 * @param utcDateString 日其UTC字符串 比如 "Tue Apr 12 00:00:00 UTC+0800 2011"
	 * @param format 要转换为的格式  比如 "yyyy-MM-dd HH:mm:ss"
	 * @return
	 */
    public static String parseUTCDate(String utcDateString, String format) {
        if (utcDateString.indexOf(":") <= -1) {
            utcDateString += " 00:00:00";
        }
        DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'UTC+0800' yyyy", Locale.ENGLISH);
        Date date = null;
        try {
            date = (Date) df.parse(utcDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new SimpleDateFormat(format).format(date);
    }
}
