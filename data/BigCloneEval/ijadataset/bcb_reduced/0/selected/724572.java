package com.hy.enterprise.framework.util.lang;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.joda.time.DateTime;
import org.joda.time.Days;
import com.vsoft.libra.enterprise.framework.util.logger.SeamLoggerUtil;

/**
 * <ul>
 * <li>设计作者：刘川</li>
 * <li>设计日期：2009-8-9</li>
 * <li>设计时间：下午04:13:27</li>
 * <li>设计目的：时间处理助手类</li>
 * </ul>
 * <ul>
 * <b>修订历史</b>
 * <li>1、</li>
 * </ul>
 */
public class DateUtil {

    /**
	 * 日志记录器
	 */
    private static LogProvider logger = Logging.getLogProvider(DateUtil.class);

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String TIME_FORMAT = "HH:mm:ss";

    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.S";

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-9</li>
	 * <li>设计时间：下午04:26:12</li>
	 * <li>设计目的：为当前日期加上指定数量的季度</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param calendar
	 * @param quarterDelta
	 * @return
	 */
    public static Calendar addQuarter(Calendar calendar, int quarterDelta) {
        int month = calendar.get(Calendar.MONTH);
        int quarter = month / 3;
        Calendar startMonth = new GregorianCalendar(calendar.get(Calendar.YEAR), quarter * 3, 1);
        startMonth.add(Calendar.MONTH, quarterDelta * 3);
        return startMonth;
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-12</li>
	 * <li>设计时间：下午09:53:15</li>
	 * <li>设计目的：以默认格式将指定日期转换为字符串</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param date
	 * @return
	 */
    public static String format(Date date) {
        return DateUtil.format(date, "yyyy-MM-dd");
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-11</li>
	 * <li>设计时间：下午04:05:23</li>
	 * <li>设计目的：将给定日期对象按照指定日期格式化方式格式化为字符串</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param date
	 * @param dateFormat
	 * @return
	 */
    public static String format(Date date, String dateFormat) {
        if (date == null) {
            return null;
        }
        DateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        return simpleDateFormat.format(date);
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-12</li>
	 * <li>设计时间：下午09:54:01</li>
	 * <li>设计目的：以默认格式将时间戳格式化为字符串</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param date
	 * @return
	 */
    public static String formatTimestamp(Date date) {
        return DateUtil.formatTimestamp(date, "yyyy-MM-dd H:m:s");
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-12</li>
	 * <li>设计时间：下午09:56:34</li>
	 * <li>设计目的：以指定格式将时间戳格式化为字符串</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param date
	 * @param timestampFormat
	 * @return
	 */
    public static String formatTimestamp(Date date, String timestampFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(timestampFormat);
        return simpleDateFormat.format(date);
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-9</li>
	 * <li>设计时间：下午04:21:35</li>
	 * <li>设计目的：转换{@link java.util.Date}为{@link java.util.Calendar}类型</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param date
	 * @return
	 */
    public static Calendar getCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-9</li>
	 * <li>设计时间：下午04:22:47</li>
	 * <li>设计目的：</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param date
	 * @return
	 */
    public static Date getDateEndForDisplay(Date date) {
        Calendar calendar;
        if (date != null) {
            calendar = new GregorianCalendar();
            calendar.setTime(date);
            calendar.add(Calendar.HOUR, -12);
            return calendar.getTime();
        } else {
            return null;
        }
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-9</li>
	 * <li>设计时间：下午04:16:17</li>
	 * <li>设计目的：获取指定日期所在月的天数</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param calendar
	 * @return
	 */
    public static int getDaysInMonth(Calendar calendar) {
        Calendar cloneCendar;
        cloneCendar = (Calendar) calendar.clone();
        cloneCendar.set(Calendar.DAY_OF_MONTH, 1);
        cloneCendar.add(Calendar.MONTH, 1);
        cloneCendar.add(Calendar.DAY_OF_MONTH, -1);
        return cloneCendar.get(Calendar.DATE);
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-9</li>
	 * <li>设计时间：下午04:17:38</li>
	 * <li>设计目的：获取指定日期所在月的天数</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param calendar
	 * @return
	 */
    public static int getDaysInMonth(DateTime calendar) {
        DateTime start = new DateTime(calendar.getYear(), calendar.getMonthOfYear(), 1, 0, 0, 0, 0);
        DateTime end = start.plusMonths(1);
        Days days = Days.daysBetween(start, end);
        return days.getDays();
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-10</li>
	 * <li>设计时间：上午01:18:54</li>
	 * <li>设计目的：将当前日期转换为显示用的字符串</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @return
	 */
    public static String getDisplayDate() {
        return DateUtil.getDisplayDate(new Date());
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-10</li>
	 * <li>设计时间：上午01:17:44</li>
	 * <li>设计目的：将指定日期转换为显示用的字符串</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param calendar
	 * @return
	 */
    public static String getDisplayDate(Calendar calendar) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if (calendar != null) {
            return simpleDateFormat.format(calendar.getTime());
        } else {
            return "";
        }
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-10</li>
	 * <li>设计时间：上午01:16:23</li>
	 * <li>设计目的：将指定日期转换为显示用的字符串</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param calendar
	 * @param dateFormat
	 * @return
	 */
    public static String getDisplayDate(Calendar calendar, String dateFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        if (calendar != null) {
            return simpleDateFormat.format(calendar.getTime());
        } else {
            return "";
        }
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-10</li>
	 * <li>设计时间：上午01:18:39</li>
	 * <li>设计目的：将指定日期转换为显示用的字符串</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param date
	 * @return
	 */
    public static String getDisplayDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return DateUtil.getDisplayDate(calendar, "yyyy-MM-dd");
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-10</li>
	 * <li>设计时间：上午01:20:07</li>
	 * <li>设计目的：将当前日期时间转换为显示用的字符串</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @return
	 */
    public static String getDisplayDateTime() {
        return DateUtil.getDisplayDateTime(new Date());
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-10</li>
	 * <li>设计时间：上午01:19:51</li>
	 * <li>设计目的：将指定日期时间转换为显示用的字符串</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param date
	 * @return
	 */
    public static String getDisplayDateTime(java.util.Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return DateUtil.getDisplayDate(calendar, "yyyy-MM-dd hh:mm:ss");
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-11</li>
	 * <li>设计时间：下午01:21:01</li>
	 * <li>设计目的：获取相隔天数</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param beforedate
	 * @param afterdate
	 * @return
	 * @throws ParseException
	 */
    public static long getDistinceDay(Date beforedate, Date afterdate) throws ParseException {
        long dayCount = 0;
        try {
            dayCount = (afterdate.getTime() - beforedate.getTime()) / (24 * 60 * 60 * 1000);
        } catch (Exception exception) {
            SeamLoggerUtil.error(DateUtil.logger, "日期解析错误", exception);
        }
        return dayCount;
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-11</li>
	 * <li>设计时间：下午01:19:21</li>
	 * <li>设计目的：获取相隔天数</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param beforedate
	 * @return
	 * @throws ParseException
	 */
    public static long getDistinceDay(String beforedate) throws ParseException {
        return DateUtil.getDistinceDay(beforedate, DateUtil.getDisplayDate());
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-11</li>
	 * <li>设计时间：下午01:23:39</li>
	 * <li>设计目的：获取相隔天数</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param beforedate
	 * @param afterdate
	 * @return
	 * @throws ParseException
	 */
    public static long getDistinceDay(String beforedate, String afterdate) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        long dayCount = 0;
        try {
            Date date1 = simpleDateFormat.parse(beforedate);
            Date date2 = simpleDateFormat.parse(afterdate);
            dayCount = (date2.getTime() - date1.getTime()) / (24 * 60 * 60 * 1000);
        } catch (ParseException parseException) {
            SeamLoggerUtil.error(DateUtil.logger, "日期解析错误", parseException);
        }
        return dayCount;
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-11</li>
	 * <li>设计时间：下午01:17:16</li>
	 * <li>设计目的：获取相隔时间数</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param beforeDateTime
	 * @param afterDateTime
	 * @return
	 * @throws ParseException
	 */
    public static long getDistinceTime(String beforeDateTime, String afterDateTime) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        long timeCount = 0;
        try {
            Date d1 = simpleDateFormat.parse(beforeDateTime);
            Date d2 = simpleDateFormat.parse(afterDateTime);
            timeCount = (d2.getTime() - d1.getTime()) / (60 * 60 * 1000);
        } catch (ParseException parseException) {
            SeamLoggerUtil.error(DateUtil.logger, "日期解析错误", parseException);
            throw parseException;
        }
        return timeCount;
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-9</li>
	 * <li>设计时间：下午04:19:25</li>
	 * <li>设计目的：获取指定地区日期格式</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param dateLocale
	 * @return
	 */
    public static String getPatternForDateLocale(Locale dateLocale) {
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.MONTH, 4);
        calendar.set(Calendar.DAY_OF_MONTH, 30);
        calendar.set(Calendar.YEAR, 2007);
        String formatted = DateFormat.getDateInstance(DateFormat.SHORT, dateLocale).format(calendar.getTime());
        String format = formatted.replaceAll("\\d", "");
        char separator = format.charAt(0);
        String[] parts = formatted.split("\\" + separator);
        StringBuilder pattern = new StringBuilder();
        for (String part : parts) {
            int index = Integer.parseInt(part);
            if (index == calendar.get(Calendar.DAY_OF_MONTH)) {
                pattern.append("dd");
            } else if (index == 1 + calendar.get(Calendar.MONTH)) {
                pattern.append("MM");
            } else {
                pattern.append("yyyy");
            }
            pattern.append(separator);
        }
        pattern.deleteCharAt(pattern.length() - 1);
        return pattern.toString();
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-9</li>
	 * <li>设计时间：下午04:14:41</li>
	 * <li>设计目的：判断指定日期是否为周末（星期六或者星期日）</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param calendar
	 * @return
	 */
    public static boolean isWeekend(Calendar calendar) {
        int dayInWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return (dayInWeek == Calendar.SATURDAY) || (dayInWeek == Calendar.SUNDAY);
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-9</li>
	 * <li>设计时间：下午04:24:16</li>
	 * <li>设计目的：将日期的时间部分设置为23:59:59.999</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param calendar
	 */
    public static void maximizeTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-9</li>
	 * <li>设计时间：下午04:23:30</li>
	 * <li>设计目的：将日期的时间部分设置为23:59:59.999</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param date
	 * @return
	 */
    public static Date maximizeTime(Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        DateUtil.maximizeTime(calendar);
        return calendar.getTime();
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-9</li>
	 * <li>设计时间：下午04:24:53</li>
	 * <li>设计目的：将日期的时间部分设置00:00.00</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param calendar
	 */
    public static void nullifyTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-9</li>
	 * <li>设计时间：下午04:25:49</li>
	 * <li>设计目的：将日期的时间部分设置00:00.00</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param date
	 * @return
	 */
    public static Date nullifyTime(Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        DateUtil.nullifyTime(calendar);
        return calendar.getTime();
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-11</li>
	 * <li>设计时间：下午04:08:35</li>
	 * <li>设计目的：按照给定格式解析指定日期字符串</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param dateString
	 * @param dateFormat
	 * @return
	 */
    public static Date parse(String dateString, String dateFormat) {
        return DateUtil.parse(dateString, dateFormat, java.util.Date.class);
    }

    /**
	 * <ul>
	 * <li>设计作者：刘川</li>
	 * <li>设计日期：2009-8-11</li>
	 * <li>设计时间：下午04:08:01</li>
	 * <li>设计目的：按照给定格式解析指定日期字符串</li>
	 * </ul>
	 * <ul>
	 * <b>修订历史</b>
	 * <li>1、</li>
	 * </ul>
	 * 
	 * @param <T>
	 * @param dateString
	 * @param dateFormat
	 * @param targetResultType
	 * @return
	 */
    @SuppressWarnings("unchecked")
    public static <T extends Date> T parse(String dateString, String dateFormat, Class<T> targetResultType) {
        if (StringUtil.isEmpty(dateString)) {
            return null;
        }
        DateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        try {
            long time = simpleDateFormat.parse(dateString).getTime();
            Date tempDate = targetResultType.getConstructor(long.class).newInstance(time);
            return (T) tempDate;
        } catch (ParseException parseException) {
            String errorInfo = "无法使用格式：[" + dateFormat + "]解析日期字符串：[" + dateString + "]";
            throw new IllegalArgumentException(errorInfo, parseException);
        } catch (Exception exception) {
            throw new IllegalArgumentException("目标结果类型：[" + targetResultType.getName() + "]错误", exception);
        }
    }

    /**
	 * 构造函数
	 */
    private DateUtil() {
        super();
    }
}
