package com.hs.framework.common.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

/**
 *  <p>
 *
 *  Title: eBeiwaiӢ����ֲ�</p> <p>
 *
 *  Description: </p> <p>
 *
 *  Copyright: Copyright (c) 2003</p> <p>
 *
 *  Company: zxt\</p>
 *
 *@author     ����
 *@created    2003��12��31��
 *@version    1.0
 */
public final class DateUtil {

    static Calendar cld;

    /**
     *  ȡ��ʱ�������Calendar
     *
     *@return
     */
    public static Calendar getCalendar() {
        return getCalendar(null);
    }

    /**
     *  ���java.util.Date ����ȡ�� Calendar����
     *
     *@param  date  ʱ�����java.util.Date
     *@return       ʱ����� Calendar
     */
    public static Calendar getCalendar(java.util.Date date) {
        if (cld == null) {
            cld = new GregorianCalendar();
        }
        if (date != null) {
            cld.setTime(date);
        }
        return cld;
    }

    /**
     *  ȡ�ñ��µ�һ�������
     *
     *@param  DQRQ
     *@return
     */
    public static String getCurMonthFirstDay(String DQRQ) {
        String Year = DQRQ.substring(0, 4);
        String Month = DQRQ.substring(5, 7);
        String Day = "01";
        String strReturn = Year + "-" + Month + "-" + Day;
        return strReturn;
    }

    /**
     *  ȡ�õ�ǰ�µ��������
     *
     *@return
     */
    public static int getDaysOfMonth() {
        return getDaysOfMonth(getRightYear(), getRightMonth());
    }

    /**
     *  ȡ��һ���µ��������
     *
     *@param  year
     *@param  month
     *@return
     */
    public static int getDaysOfMonth(int year, int month) {
        return (int) ((toLongTime(month == 12 ? (year + 1) : year, month == 12 ? 1 : (month + 1), 1) - toLongTime(year, month, 1)) / (24 * 60 * 60 * 1000));
    }

    /**
     *  ȡ����һ���µ��������
     *
     *@param  year
     *@param  month
     *@return
     */
    public static int getDaysOfNextMonth(int year, int month) {
        year = month == 12 ? year + 1 : year;
        month = month == 12 ? 1 : month + 1;
        return getDaysOfMonth(year, month);
    }

    /**
     *  ȡ���ϸ��µ��������
     *
     *@param  year
     *@param  month
     *@return
     */
    public static int getDaysOfProMonth(int year, int month) {
        year = month == 1 ? year - 1 : year;
        month = month == 1 ? 12 : month - 1;
        return getDaysOfMonth(year, month);
    }

    /**
     *  ȡ���µ�һ��
     *
     *@param  dqrq  Description of Parameter
     *@return       String
     *@since        2003-0416
     */
    public static String getFirstDayOfPreMonth(String dqrq) {
        String strDQRQ = dqrq;
        String strReturn = "";
        String strYear = "";
        String strMonth = "";
        String strDay = "";
        if (strDQRQ == null) {
            return "";
        }
        if (strDQRQ.length() == 8) {
            strYear = strDQRQ.substring(0, 4);
            strMonth = strDQRQ.substring(4, 6);
            strDay = strDQRQ.substring(6, 8);
        }
        if (strDQRQ.length() == 10) {
            strYear = strDQRQ.substring(0, 4);
            strMonth = strDQRQ.substring(5, 7);
            strDay = strDQRQ.substring(8, 10);
        }
        int iMonth = DataTypeUtil.toInt(strMonth);
        int iYear = DataTypeUtil.toInt(strYear);
        if (iMonth == 1) {
            iYear = iYear - 1;
            iMonth = 12;
        } else if (iMonth > 1) {
            iMonth = iMonth - 1;
        } else {
            return "";
        }
        if (iMonth < 10) {
            strMonth = "0" + iMonth;
        } else {
            strMonth = "" + iMonth;
        }
        strDay = "01";
        if (strDQRQ.length() == 8) {
            return iYear + strMonth + strDay;
        } else if (strDQRQ.length() == 10) {
            return iYear + "-" + strMonth + "-" + strDay;
        } else {
            return "";
        }
    }

    /**
     *  Gets the LastDate attribute of the DateUtil class
     *
     *@param  day  Description of Parameter
     *@return      The LastDate value
     */
    public static Date getLastDate(long day) {
        Date date = new Date();
        long date_3_hm = date.getTime() - 3600000 * 34 * day;
        Date date_3_hm_date = new Date(date_3_hm);
        return date_3_hm_date;
    }

    /**
     *  Gets the LongStringDate attribute of the DateUtil class
     *
     *@return    The LongStringDate value
     */
    public static String getLongStringDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    /**
     *  ȡ���¸��µ�һ�������
     *
     *@param  DQRQ
     *@return
     */
    public static String getNextMonthFirstDay(String DQRQ) {
        String Year = DQRQ.substring(0, 4);
        int intYear = Integer.valueOf(Year).intValue();
        String Month = DQRQ.substring(5, 7);
        int intMonth = Integer.valueOf(Month).intValue();
        String Day = "01";
        if (intMonth == 12) {
            intYear = intYear + 1;
            intMonth = 1;
        } else {
            intMonth = intMonth + 1;
        }
        Year = String.valueOf(intYear);
        Month = String.valueOf(intMonth);
        if (Month.length() == 1) {
            Month = "0" + Month;
        }
        String strReturn = Year + "-" + Month + "-" + Day;
        return strReturn;
    }

    /**
     *  ��ݵ�ǰ����ȡ�� ��һ���±�������
     *
     *@param  date  ��ǰ����
     *@return
     */
    public static Date[] getNextMonths(Date date) {
        int iYear = getRightYear(date);
        int iMonth = getRightMonth(date);
        Date[] dates = new Date[2];
        Calendar cld = GregorianCalendar.getInstance();
        cld.set(iYear, iMonth - 1, 1);
        dates[0] = cld.getTime();
        cld.set(iYear, iMonth - 1, getDaysOfMonth(iYear, iMonth));
        dates[1] = cld.getTime();
        return dates;
    }

    /**
     *  ��ݵ�ǰ���� ȡ����һ��������� ������
     *
     *@param  date
     *@return
     */
    public static Date[] getNextQuarters(Date date) {
        int iYear = getRightYear(date);
        int iMonth = getRightMonth(date);
        Calendar cld = GregorianCalendar.getInstance();
        Date[] dates = new Date[2];
        if (iMonth >= 1 && iMonth < 4) {
            cld.set(iYear, 0, 1);
            dates[0] = cld.getTime();
            cld.set(iYear, 2, 31);
            dates[1] = cld.getTime();
        } else if (iMonth >= 4 && iMonth < 7) {
            cld.set(iYear, 3, 1);
            dates[0] = cld.getTime();
            cld.set(iYear, 5, 30);
            dates[1] = cld.getTime();
        } else if (iMonth >= 7 && iMonth < 10) {
            cld.set(iYear, 6, 1);
            dates[0] = cld.getTime();
            cld.set(iYear, 8, 30);
            dates[1] = cld.getTime();
        } else {
            cld.set(iYear, 9, 1);
            dates[0] = cld.getTime();
            cld.set(iYear, 11, 31);
            dates[1] = cld.getTime();
        }
        return dates;
    }

    /**
     *  Gets the nextSomeDay attribute of the DateUtil class
     *
     *@param  strDate  Description of the Parameter
     *@param  next     Description of the Parameter
     *@return          The nextSomeDay value
     */
    public static String getNextSomeDay(String strDate, int next) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date strtodate = formatter.parse(strDate);
            long tomorrowLong = strtodate.getTime() + next * 24 * 60 * 60 * 1000;
            System.err.println(tomorrowLong);
            Date tomorrow_date = new Date(tomorrowLong);
            return formatter.format(tomorrow_date);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *  ��ݵ�ǰ����ȡ�� ��һ���걨������
     *
     *@param  date
     *@return
     */
    public static Date[] getNextYears(Date date) {
        Date[] dates = new Date[2];
        int iYear = getRightYear(date);
        Calendar cld = GregorianCalendar.getInstance();
        cld.set(iYear, 0, 1);
        dates[0] = cld.getTime();
        cld.set(iYear, 11, 31);
        dates[1] = cld.getTime();
        return dates;
    }

    /**
     *  Gets the Now attribute of the DateUtil class
     *
     *@return    The Now value
     */
    public static Date getNow() {
        Date currentTime = new Date();
        return currentTime;
    }

    /**
     *  Gets the NowDate attribute of the DateUtil class
     *
     *@return    The NowDate value
     */
    public static Date getNowDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        ParsePosition pos = new ParsePosition(8);
        Date currentTime_2 = formatter.parse(dateString, pos);
        return currentTime_2;
    }

    /**
     *  Gets the NowDateShort attribute of the DateUtil class
     *
     *@return    The NowDateShort value
     */
    public static Date getNowDateShort() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = formatter.format(currentTime);
        ParsePosition pos = new ParsePosition(8);
        Date currentTime_2 = formatter.parse(dateString, pos);
        return currentTime_2;
    }

    /**
     *  ���ַ�ת��ΪORACLE��Ҫ��������ַ��ʽ <P>
     *
     *  ADD BY WJZ 031229<P>
     *
     *
     *
     *@param  date  �����ַ�
     *@return       ORACLE��Ҫ��������ַ��ʽ
     */
    public static String getOracleDateFormat(String date) {
        if (date == null) {
            return null;
        }
        if (date.length() == 10) {
            if (date.indexOf("-") > 0) {
                return "to_date('" + date + "','YYYY-MM-DD')";
            } else if (date.indexOf("/") > 0) {
                return "to_date('" + date + "','YYYY/MM/DD')";
            } else {
                return date;
            }
        } else if (date.length() == 8) {
            return "to_date('" + date + "','YYYYMMDD')";
        } else {
            return date;
        }
    }

    /**
     *  ȡ�������һ��
     *
     *@param  str  Description of Parameter
     *@return      String
     *@since       2003-0416
     */
    public static String getPreDate(String str) {
        String strRQ = str;
        java.text.SimpleDateFormat df = null;
        if (str == null) {
            return "";
        }
        if (str.length() == 10) {
            df = new java.text.SimpleDateFormat("yyyy-MM-dd");
        } else if (str.length() == 8) {
            df = new java.text.SimpleDateFormat("yyyyMMdd");
        } else {
            return "";
        }
        try {
            java.util.Date dt = df.parse(strRQ);
            long lg = dt.getTime() - 24 * 60 * 60 * 1000;
            dt.setTime(lg);
            return df.format(dt);
        } catch (Exception e) {
            LogUtil.getLogger().debug("getPreData:\n" + e.toString());
            LogUtil.getLogger().debug(e);
            return "";
        }
    }

    /**
     *  ȡ�������һ��
     *
     *@param  dqrq  Description of Parameter
     *@return       String
     *@since        2003-0416
     */
    public static String getPreMaxDayOfMonth(String dqrq) {
        String strDQRQ = dqrq;
        String strReturn = "";
        String strYear = "";
        String strMonth = "";
        String strDay = "";
        if (strDQRQ == null) {
            return "";
        }
        if (strDQRQ.length() == 8) {
            strYear = strDQRQ.substring(0, 4);
            strMonth = strDQRQ.substring(4, 6);
            strDay = strDQRQ.substring(6, 8);
        }
        if (strDQRQ.length() == 10) {
            strYear = strDQRQ.substring(0, 4);
            strMonth = strDQRQ.substring(5, 7);
            strDay = strDQRQ.substring(8, 10);
        }
        strDay = "01";
        if (strDQRQ.length() == 8) {
            strDQRQ = strYear + strMonth + strDay;
        }
        if (strDQRQ.length() == 10) {
            strDQRQ = strYear + "-" + strMonth + "-" + strDay;
        }
        strReturn = getPreDate(strDQRQ);
        if (strReturn == null) {
            strReturn = "";
        }
        return strReturn;
    }

    /**
     *  ���µ�һ��
     *
     *@param  dqrq
     *@return
     */
    public static String getPreMonthFirstDay(String dqrq) {
        return getFirstDayOfPreMonth(dqrq);
    }

    /**
     *  �������һ��
     *
     *@param  dqrq
     *@return
     */
    public static String getPreMonthMaxDay(String dqrq) {
        return getPreMaxDayOfMonth(dqrq);
    }

    /**
     *  Gets the RightDate attribute of the DateUtil class
     *
     *@return    The RightDate value
     */
    public static int getRightDate() {
        return getRightDate((Date) null);
    }

    /**
     *  Gets the RightDate attribute of the DateUtil class
     *
     *@param  date  Description of Parameter
     *@return       The RightDate value
     */
    public static int getRightDate(Date date) {
        Calendar rightMonth = Calendar.getInstance();
        if (date != null) {
            rightMonth.setTime(date);
        }
        return rightMonth.get(Calendar.DATE);
    }

    /**
     *  Gets the RightDate attribute of the DateUtil class
     *
     *@param  date  Description of Parameter
     *@return       The RightDate value
     */
    public static int getRightDate(Calendar date) {
        if (date == null) {
            return getRightDate((Date) null);
        } else {
            return getRightDate(date.getTime());
        }
    }

    /**
     *  Gets the RightHour attribute of the DateUtil class
     *
     *@return    The RightHour value
     */
    public static int getRightHour() {
        Calendar rightHour = Calendar.getInstance();
        return rightHour.get(Calendar.HOUR);
    }

    /**
     *  Gets the RightMinute attribute of the DateUtil class
     *
     *@return    The RightMinute value
     */
    public static int getRightMinute() {
        Calendar rightMinute = Calendar.getInstance();
        return rightMinute.get(Calendar.MINUTE);
    }

    /**
     *  Gets the RightMonth attribute of the DateUtil class
     *
     *@return    The RightMonth value
     */
    public static int getRightMonth() {
        return getRightMonth((Date) null);
    }

    /**
     *  Gets the RightMonth attribute of the DateUtil class
     *
     *@param  date  Description of Parameter
     *@return       The RightMonth value
     */
    public static int getRightMonth(Date date) {
        Calendar rightMonth = Calendar.getInstance();
        if (date != null) {
            rightMonth.setTime(date);
        }
        return rightMonth.get(Calendar.MONTH) + 1;
    }

    /**
     *  Gets the RightMonth attribute of the DateUtil class
     *
     *@param  date  Description of Parameter
     *@return       The RightMonth value
     */
    public static int getRightMonth(Calendar date) {
        if (date == null) {
            return getRightMonth((Date) null);
        } else {
            return getRightMonth(date.getTime());
        }
    }

    /**
     *  Gets the RightSecond attribute of the DateUtil class
     *
     *@return    The RightSecond value
     */
    public static int getRightSecond() {
        Calendar rightSecond = Calendar.getInstance();
        return rightSecond.get(Calendar.SECOND);
    }

    /**
     *  ������������Ƿֱ�ȡ�õ�ǰ���ꡢ�¡��ա�ʱ���֡��� ������������Ƿֱ�ȡ�õ�ǰ���ꡢ�¡��ա�ʱ���֡���
     *  ������������Ƿֱ�ȡ�õ�ǰ���ꡢ�¡��ա�ʱ���֡��� ������������Ƿֱ�ȡ�õ�ǰ���ꡢ�¡��ա�ʱ���֡���
     *  ������������Ƿֱ�ȡ�õ�ǰ���ꡢ�¡��ա�ʱ���֡��� ������������Ƿֱ�ȡ�õ�ǰ���ꡢ�¡��ա�ʱ���֡���
     *  ������������Ƿֱ�ȡ�õ�ǰ���ꡢ�¡��ա�ʱ���֡��� ȡ�õ�ǰ��ʱ�䣬���������ʽ����
     *
     *@param  isTime  Description of Parameter
     *@return         The RightTimes value
     *@return         The RightTimes value
     *@return         The RightTimes value
     *@return         The RightTimes value
     *@return         The RightTimes value
     *@return         The RightTimes value
     *@return         The RightTimes value
     *@return
     */
    public static int[] getRightTimes(boolean isTime) {
        Calendar rightTime = Calendar.getInstance();
        int time[];
        if (isTime) {
            time = new int[6];
            time[0] = rightTime.get(Calendar.YEAR);
            time[1] = rightTime.get(Calendar.MONTH) + 1;
            time[2] = rightTime.get(Calendar.DATE);
            time[3] = rightTime.get(Calendar.HOUR);
            time[4] = rightTime.get(Calendar.MINUTE);
            time[5] = rightTime.get(Calendar.SECOND);
        } else {
            time = new int[3];
            time[0] = rightTime.get(Calendar.YEAR);
            time[1] = rightTime.get(Calendar.MONTH) + 1;
            time[2] = rightTime.get(Calendar.DATE);
        }
        return time;
    }

    /**
     *  ȡ�õ�ǰ���� ��������ʽ���أ� ���磺1999
     *
     *@return    ���ص�ǰ��
     */
    public static int getRightYear() {
        return getRightYear((Date) null);
    }

    /**
     *  ���Date����ȡ�� ��� ������ʽ
     *
     *@param  date  java.util.Date ����
     *@return       ���� java.util.Date �����
     */
    public static int getRightYear(Date date) {
        Calendar rightYear = Calendar.getInstance();
        if (date != null) {
            rightYear.setTime(date);
        }
        return rightYear.get(Calendar.YEAR);
    }

    /**
     *  ���Calendar ���󷵻� ��� ������ʽ
     *
     *@param  date  Calendar ����
     *@return       Calendar �����
     */
    public static int getRightYear(Calendar date) {
        if (date != null) {
            return getRightYear(date.getTime());
        } else {
            return getRightYear((Date) null);
        }
    }

    /**
     *  Gets the S attribute of the DateUtil class
     *
     *@param  strDate  Description of Parameter
     *@return          The S value
     */
    public static long getS(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        return strtodate.getTime();
    }

    /**
     *  Gets the SN attribute of the DateUtil class
     *
     *@param  strDate  Description of Parameter
     *@return          The SN value
     */
    public static long getSN(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        return strtodate.getTime();
    }

    /**
     *  Gets the SkQData attribute of the DateUtil class
     *
     *@param  SSSQ  Description of Parameter
     *@return       The SkQData value
     */
    public static long getSkQData(String SSSQ) {
        return Long.valueOf(SSSQ + "01").longValue();
    }

    /**
     *  Gets the SkZData attribute of the DateUtil class
     *
     *@param  SSSQ  Description of Parameter
     *@return       The SkZData value
     */
    public static long getSkZData(String SSSQ) {
        int year = Integer.valueOf(SSSQ.substring(0, 4)).intValue();
        int month = Integer.valueOf(SSSQ.substring(4, 6)).intValue();
        int day = getDaysOfMonth(year, month);
        String strDay;
        if (day < 10) {
            strDay = "0" + String.valueOf(day);
        } else {
            strDay = String.valueOf(day);
        }
        return Long.valueOf(SSSQ + strDay).longValue();
    }

    /**
     *  Gets the SqDate attribute of the DateUtil class
     *
     *@param  SSSQ_QZ  Description of Parameter
     *@return          The SqDate value
     */
    public static long getSqDate(String SSSQ_QZ) {
        return Long.valueOf(SSSQ_QZ.substring(0, 6)).longValue();
    }

    /**
     *  Gets the SqlDate attribute of the DateUtil class
     *
     *@return    The SqlDate value
     */
    public static java.sql.Date getSqlDate() {
        return new java.sql.Date(new Date().getTime());
    }

    /**
     *  Gets the SqlDate attribute of the DateUtil class
     *
     *@param  d  Description of Parameter
     *@return    The SqlDate value
     */
    public static java.sql.Date getSqlDate(String d) {
        java.util.Date dd = strToDate(d);
        return new java.sql.Date(dd.getTime());
    }

    /**
     *  Gets the StringDate attribute of the DateUtil class
     *
     *@return    The StringDate value
     */
    public static String getStringDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    /**
     *  Gets the StringDate attribute of the DateUtil class
     *
     *@param  format  Description of Parameter
     *@return         The StringDate value
     */
    public static String getStringDate(String format) {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    /**
     *  Gets the StringDateShort attribute of the DateUtil class
     *
     *@return    The StringDateShort value
     */
    public static String getStringDateShort() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    /**
     *  Gets the tomorrow attribute of the DateUtil class
     *
     *@param  strDate  Description of the Parameter
     *@return          The tomorrow value
     */
    public static String getTomorrow(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date strtodate = formatter.parse(strDate);
            long tomorrowLong = strtodate.getTime() + 24 * 60 * 60 * 1000;
            System.err.println(tomorrowLong);
            Date tomorrow_date = new Date(tomorrowLong);
            return formatter.format(tomorrow_date);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *  Gets the yesterDay attribute of the DateUtil class
     *
     *@param  strDate  Description of the Parameter
     *@return          The yesterDay value
     */
    public static String getYesterDay(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date strtodate = formatter.parse(strDate);
            long yesterLong = strtodate.getTime() - 24 * 60 * 60 * 1000;
            Date yester_date = new Date(yesterLong);
            return formatter.format(yester_date);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *  У�������Ƿ�Ϸ���
     *
     *@param  strDate  �����ַ�����2003-03-05����20030507;
     *@return          �Ϸ�������true;�Ƿ�������false
     */
    public static boolean isVerifyDate(String strDate) {
        if (strDate == null || (strDate.trim().length() != 10 && strDate.trim().length() != 8)) {
            return false;
        }
        int iYear = 0;
        int iMonth = 0;
        int iDay = 0;
        boolean dateLenFlags = strDate.trim().length() == 10;
        try {
            if (dateLenFlags) {
                iYear = Integer.parseInt(strDate.substring(0, 4));
                iMonth = Integer.parseInt(strDate.substring(5, 7));
                iDay = Integer.parseInt(strDate.substring(8, 10));
            } else {
                iYear = Integer.parseInt(strDate.substring(0, 4));
                iMonth = Integer.parseInt(strDate.substring(4, 6));
                iDay = Integer.parseInt(strDate.substring(6, 8));
            }
        } catch (Exception e) {
            return false;
        }
        if (iMonth > 12 || iMonth < 1) {
            return false;
        } else if (iDay > getDaysOfMonth(iYear, iMonth)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     *  ȡ��һ���ڵ���ֹ���� return Vector
     *
     *@param  DQRQ  Description of Parameter
     *@return       Description of the Returned Value
     */
    public static Vector ChangeDate(String DQRQ) {
        Vector vcdate = new Vector();
        Date date = DateUtil.strToBirthday(DQRQ);
        Date[] nextmonth = DateUtil.getNextMonths(date);
        Date[] nextyear = DateUtil.getNextYears(date);
        Date[] nextquarter = DateUtil.getNextQuarters(date);
        vcdate.addElement(DateUtil.dateToString(nextmonth[0]));
        vcdate.addElement(DateUtil.dateToString(nextmonth[1]));
        vcdate.addElement(DateUtil.dateToString(nextquarter[0]));
        vcdate.addElement(DateUtil.dateToString(nextquarter[1]));
        vcdate.addElement(DateUtil.dateToString(nextyear[0]));
        vcdate.addElement(DateUtil.dateToString(nextyear[1]));
        return vcdate;
    }

    /**
     *  Description of the Method
     *
     *@param  DQRQ  Description of Parameter
     *@return       Description of the Returned Value
     */
    public static String changeDate(String DQRQ) {
        String strDQRQ = DQRQ;
        String strReturn = "";
        String strYear = "";
        String strMonth = "";
        String strDay = "";
        strYear = strDQRQ.substring(0, 4);
        strMonth = strDQRQ.substring(5, 7);
        strDay = strDQRQ.substring(8, 10);
        return strYear + strMonth + strDay;
    }

    /**
     *  Description of the Method
     *
     *@param  dateDate  Description of Parameter
     *@return           Description of the Returned Value
     */
    public static String dateToShortStr(java.util.Date dateDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String dateString = formatter.format(dateDate);
        return dateString;
    }

    /**
     *  Description of the Method
     *
     *@param  dateDate  Description of Parameter
     *@return           Description of the Returned Value
     */
    public static String dateToStr(java.util.Date dateDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(dateDate);
        return dateString;
    }

    /**
     *  ת������Ϊ�ַ��ʽ <p>
     *
     *  add by wjz 1125
     *
     *@param  dateDate
     *@param  format
     *@return
     */
    public static String dateToStr(java.util.Date dateDate, String format) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            String dateString = formatter.format(dateDate);
            return dateString;
        } catch (Exception ex) {
            LogUtil.getLogger().error(ex.getMessage(), ex);
            return "";
        }
    }

    /**
     *  ��Date���ڸ�ʽת��Ϊ�ַ��ʽ������ʱ�����ʽ
     *
     *@param  date
     *@return
     */
    public static final String dateToString(Date date) {
        return dateToString(date, false);
    }

    /**
     *  ��date����ת�����ַ�
     *
     *@param  date
     *@param  bHaveHour  �Ƿ񷵻ش���ʱ�����ʽ
     *@return            ����yyyy-mm-dd����yyyy-mm-dd hh:mm:ss
     */
    public static final String dateToString(java.util.Date date, boolean bHaveHour) {
        Calendar cal = getCalendar(date);
        if (!bHaveHour || (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0 && cal.get(Calendar.SECOND) == 0)) {
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DAY_OF_MONTH);
            return "" + year + (month < 10 ? "-0" : "-") + month + (day < 10 ? "-0" : "-") + day;
        }
        return date.toString();
    }

    /**
     *  ����}������֮����������
     *
     *@param  date1  Description of Parameter
     *@param  date2  Description of Parameter
     *@return        ������������
     */
    public static int diffDate(Calendar date1, Calendar date2) {
        return diffDate(date1.getTime(), date2.getTime());
    }

    /**
     *  ����}������֮����������
     *
     *@param  date1  Description of Parameter
     *@param  date2  Description of Parameter
     *@return        ������������
     */
    public static int diffDate(java.util.Date date1, java.util.Date date2) {
        return (int) ((date1.getTime() - date2.getTime()) / (24 * 60 * 60 * 1000));
    }

    /**
     *  ����}������֮����������
     *
     *@param  date1  Description of Parameter
     *@param  date2  Description of Parameter
     *@return        Description of the Returned Value
     */
    public static int diffDate(String date1, String date2) {
        return diffDate(strToDate(date1), strToDate(date2));
    }

    /**
     *  ��ĳһ����ϻ��߼�ȥ���죨��dayΪ�����ʱ�򣩵�����
     *
     *@param  date  ĳһ��
     *@param  day   ����
     *@return       ���صĽ������
     */
    public static java.util.Date incDate(java.util.Date date, int day) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_YEAR, day);
        return cal.getTime();
    }

    /**
     *@param  date
     *@param  month
     *@return
     */
    public static java.util.Date incMonth(java.util.Date date, int month) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.add(Calendar.MONTH, month);
        return cal.getTime();
    }

    /**
     *@param  date
     *@param  year
     *@return
     */
    public static java.util.Date incYear(java.util.Date date, int year) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.add(Calendar.YEAR, year);
        return cal.getTime();
    }

    /**
     *  The main program for the DateUtil class
     *
     *@param  args  The command line arguments
     */
    public static void main(String[] args) {
        System.out.println(dateToString(getNow(), false));
    }

    /**
     *  Description of the Method
     *
     *@param  strDate  Description of Parameter
     *@return          Description of the Returned Value
     */
    public static Date strToBirthday(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        return strtodate;
    }

    /**
     *  Description of the Method
     *
     *@param  strDate  Description of Parameter
     *@return          Description of the Returned Value
     */
    public static Date strToDate(String strDate) {
        if (strDate == null || strDate.length() < 6) {
            throw new IllegalArgumentException("illeage date format");
        }
        String fmt = "yyyy-MM-dd HH:mm:ss";
        if (strDate.length() == 10) {
            fmt = "yyyy-MM-dd";
        } else if (strDate.length() == 8) {
            if (strDate.indexOf("-") > 0) {
                fmt = "yy-MM-dd";
            } else {
                fmt = "yyyyMMdd";
            }
        } else if (strDate.length() == 6) {
            fmt = "yyMMdd";
        }
        SimpleDateFormat formatter = new SimpleDateFormat(fmt);
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        return strtodate;
    }

    /**
     *  �������յõ�һ��Date����
     *
     *@param  year   ��
     *@param  month  ��
     *@param  day    ��
     *@return        �õ���Date����
     */
    public static java.util.Date toDate(int year, int month, int day) {
        if (cld == null) {
            cld = new GregorianCalendar();
        }
        cld.clear();
        cld.set(Calendar.YEAR, year);
        cld.set(Calendar.MONTH, month - 1);
        cld.set(Calendar.DAY_OF_MONTH, day);
        return cld.getTime();
    }

    /**
     *  �Ӹ�� year,mongth,day �õ�ʱ���longֵ��ʾ(a point in time that is <tt>time</tt>
     *  milliseconds after January 1, 1970 00:00:00 GMT).
     *
     *@param  year   ��
     *@param  month  ��
     *@param  day    ��
     *@return        ��� year,mongth,day �õ�ʱ���longֵ��ʾ
     */
    public static long toLongTime(int year, int month, int day) {
        return toDate(year, month, day).getTime();
    }

    /**
     *  Description of the Method
     *
     *@param  year   Description of Parameter
     *@param  month  Description of Parameter
     *@param  day    Description of Parameter
     *@param  hour   Description of Parameter
     *@param  min    Description of Parameter
     *@param  sec    Description of Parameter
     *@return        Description of the Returned Value
     */
    public static long toLongTime(int year, int month, int day, int hour, int min, int sec) {
        if (cld == null) {
            cld = new GregorianCalendar();
        }
        cld.clear();
        cld.set(Calendar.YEAR, year);
        cld.set(Calendar.MONTH, month - 1);
        cld.set(Calendar.DAY_OF_MONTH, day);
        cld.set(Calendar.HOUR_OF_DAY, hour);
        cld.set(Calendar.MINUTE, min);
        cld.set(Calendar.SECOND, sec);
        return cld.getTime().getTime();
    }

    /**
     *  ���ַ�ת��ΪOracle��Ҫ�������ַ� add by wjz 030924
     *
     *@param  date
     *@return
     */
    public static String toOracleDateFormat(String date) {
        if (date == null) {
            return null;
        }
        if (date.length() == 10) {
            return "to_date('" + date + "','YYYY-MM-DD')";
        } else if (date.length() == 8) {
            return "to_date('" + date + "','YYYYMMDD')";
        } else {
            return null;
        }
    }
}
