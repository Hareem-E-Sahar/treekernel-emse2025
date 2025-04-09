package com.ourlinc.conference;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期处理工具类，主要用于处理日期的转换（字符串转日期、日期转字符串、还有日期的格式转换）
 * @author pengchengji
 *
 */
public class DateUtil {

    /**
	 * 把一个Date对象转换成yyyy-MM-dd的格式
	 * @param date #输入参数# 
	 * @return 转换后的Date对象
	 */
    public static final Date formatDate(Date date) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String strToday = format.format(date);
        try {
            date = format.parse(strToday);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
	 * 把一个Date对象转换成yyyy-MM-dd格式的字符串
	 * @param date
	 * @return 转换后的Date对象
	 */
    public static final String dateFormatToStr(Date date) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

    /**
	 * 把一个String对象转换成yyyy-MM-dd格式的Date对象
	 * @param strOneDate
	 * @return 转换后的Date对象
	 */
    public static final Date strFormatDate(String strOneDate) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = format.parse(strOneDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}
