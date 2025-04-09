package hld.coins.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUnit {

    public static final String LONG_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String SHORT_FORMAT = "yyyy-MM-dd";

    public static final String TIME_FORMAT = "HH:mm:ss";

    /**
	 * ��ȡ����ʱ��
	 * 
	 * @return ����ʱ������ yyyy-MM-dd HH:mm:ss
	 */
    public static Date getNowDate() {
        return getNowDate(LONG_FORMAT);
    }

    /**
	 * ��ȡ����ʱ��
	 * 
	 * @return ���ض�ʱ���ַ��ʽyyyy-MM-dd
	 */
    public static Date getNowDateShort() {
        return getNowDate(SHORT_FORMAT);
    }

    /**
	 * ��ȡʱ�� Сʱ:��;�� HH:mm:ss
	 * 
	 * @return
	 */
    public static Date getNowTimeShort() {
        return getNowDate(TIME_FORMAT);
    }

    /**
	 * ��ȡ����ʱ��
	 * 
	 * @param timeFormat
	 *            ����ʱ���ʽ
	 */
    public static Date getNowDate(String timeFormat) {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat(timeFormat);
        String dateString = formatter.format(currentTime);
        ParsePosition pos = new ParsePosition(8);
        Date currentTime_2 = formatter.parse(dateString, pos);
        return currentTime_2;
    }

    /**
	 * ��ȡ����ʱ��
	 * 
	 * @return �����ַ��ʽ yyyy-MM-dd HH:mm:ss
	 */
    public static String getStringDate() {
        return getStringDate(LONG_FORMAT);
    }

    /**
	 * ��ȡ����ʱ��
	 * 
	 * @return ���ض�ʱ���ַ��ʽyyyy-MM-dd
	 */
    public static String getStringDateShort() {
        return getStringDate(SHORT_FORMAT);
    }

    /**
	 * ��ȡʱ�� Сʱ:��;�� HH:mm:ss
	 * 
	 * @return
	 */
    public static String getTimeShort() {
        return getStringDate(TIME_FORMAT);
    }

    /**
	 * ��ȡ����ʱ��
	 * 
	 * @param �����ַ��ʽ
	 */
    public static String getStringDate(String timeFormat) {
        java.util.Date currentTime = new java.util.Date();
        SimpleDateFormat formatter = new SimpleDateFormat(timeFormat);
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    /**
	 * ����ʱ���ʽ�ַ�ת��Ϊʱ�� yyyy-MM-dd HH:mm:ss
	 * 
	 * @param strDate
	 * @return
	 */
    public static Date strToLongDate(String strDate) {
        return strToDate(strDate, LONG_FORMAT);
    }

    /**
	 * ����ʱ���ʽ�ַ�ת��Ϊʱ�� yyyy-MM-dd
	 * 
	 * @param strDate
	 * @return
	 */
    public static Date strToShortDate(String strDate) {
        return strToDate(strDate, SHORT_FORMAT);
    }

    /**
	 * ��ʱ���ʽ�ַ�ת��Ϊʱ�� HH:mm:ss
	 * 
	 * @param strDate
	 * @return
	 */
    public static Date strToTimeDate(String strDate) {
        return strToDate(strDate, TIME_FORMAT);
    }

    /**
	 * ��ָ����ʱ���ʽ�ַ�ת��Ϊʱ��
	 * 
	 * @param strDate
	 * @param timeFormat
	 * @return
	 */
    public static Date strToDate(String strDate, String timeFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(timeFormat);
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        return strtodate;
    }

    /**
	 * ����ʱ���ʽʱ��ת��Ϊ�ַ� yyyy-MM-dd HH:mm:ss
	 * 
	 * @param dateDate
	 * @return
	 */
    public static String dateToLongStr(Date dateDate) {
        return dateToStr(dateDate, LONG_FORMAT);
    }

    /**
	 * ����ʱ���ʽ�ַ�ת��Ϊʱ�� yyyy-MM-dd
	 * 
	 * @param strDate
	 * @return
	 */
    public static String dateToShortStr(Date dateDate) {
        return dateToStr(dateDate, SHORT_FORMAT);
    }

    /**
	 * ��ʱ���ʽ�ַ�ת��Ϊʱ�� HH:mm:ss
	 * 
	 * @param strDate
	 * @return
	 */
    public static String dateToTimeStr(Date dateDate) {
        return dateToStr(dateDate, TIME_FORMAT);
    }

    /**
	 * ��ָ����ʱ���ʽʱ��ת��Ϊ�ַ�
	 * 
	 * @param dateDate
	 * @param timeFormat
	 * @return
	 */
    public static String dateToStr(Date dateDate, String timeFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(timeFormat);
        String dateString = formatter.format(dateDate);
        return dateString;
    }

    public static String LongToStr(long m, String timeFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(timeFormat);
        String dateString = formatter.format(new Date(m));
        return dateString;
    }
}
