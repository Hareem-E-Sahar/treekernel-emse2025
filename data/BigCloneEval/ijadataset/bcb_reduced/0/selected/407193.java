package com.gm.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * 常用小工具
 */
public class ToolsUtils {

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+08:00"));
    }

    /**
	 * 四舍五入 n 位小数,0取到小数第一位-1取个位
	 */
    public static double myRound(double rNums, int n) {
        int j = (int) Math.round(rNums * Math.pow(10, n));
        rNums = j / Math.pow(10, n);
        return rNums;
    }

    /**
	 * 计算时间差(时间单位,开始时时结束时间) 示例方法
	 * howLong("h","2007-08-09 10:22:26","2007-08-09 20:21:30") ///9小时56?返回9小时
	 * 
	 * @param unit
	 *            :s:秒,m:分钟,h:小时,d:天数
	 * @param time1
	 * @param time2
	 * @return
	 * @throws ParseException
	 */
    public static long howLong(String unit, String time1, String time2) throws ParseException {
        Date date1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time1);
        Date date2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time2);
        long ltime = date1.getTime() - date2.getTime() < 0 ? date2.getTime() - date1.getTime() : date1.getTime() - date2.getTime();
        if (unit.equals("s")) {
            return ltime / 1000;
        } else if (unit.equals("m")) {
            return ltime / 60000;
        } else if (unit.equals("h")) {
            return ltime / 3600000;
        } else if (unit.equals("d")) {
            return ltime / 86400000;
        } else {
            return 0;
        }
    }

    /**
	 * 得到几天后的星期 或者几天前的日期
	 */
    public static String getDateFrom(String dateFrom, int amount) {
        SimpleDateFormat formatYMD = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = formatYMD.parse(dateFrom);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, amount);
        return formatYMD.format(cal.getTime());
    }

    /**
	 * 如果数据的记录值是1或"true"，则返回 "checked=checked"
	 */
    public static String isChecked(String val) {
        if (val == null) {
            return "";
        } else {
            if (val.equals("1") || val.equals("true")) {
                return "checked=\"checked\"";
            } else {
                return "";
            }
        }
    }

    /**
	 * 如果数据的记录值是true，则返回 "checked=checked"
	 */
    public static String isChecked(boolean val) {
        if (val) {
            return "checked=\"checked\"";
        } else {
            return "";
        }
    }

    /**
	 * 对字符串数组进行排序
	 * 
	 * @param str
	 *            开始字符串数组
	 * @param flag
	 *            flag=0:顺序排序 flag=1:序排序
	 * @return 排序后的字符串数组
	 */
    public static String[] arrSort(String[] str, int flag) {
        if (str == null || str.length == 0) {
            return null;
        }
        String temp = str[0];
        if (flag == 0) {
            for (int i = 0; i < str.length - 1; i++) {
                for (int j = i + 1; j < str.length; j++) {
                    if (str[i].compareTo(str[j]) > 0) {
                        temp = str[i];
                        str[i] = str[j];
                        str[j] = temp;
                    }
                }
            }
        } else if (flag == 1) {
            for (int i = 0; i < str.length - 1; i++) {
                for (int j = i + 1; j < str.length; j++) {
                    if (str[i].compareTo(str[j]) < 0) {
                        temp = str[i];
                        str[i] = str[j];
                        str[j] = temp;
                    }
                }
            }
        }
        return str;
    }

    public static void main(String[] args) {
        System.out.println(myRound(25.56, -1));
    }
}
