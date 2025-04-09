package com.james.datetime;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author james�������
 */
public class datetime {

    /**
 * parse now to String 
 * @return
 */
    public String nowToStr() {
        java.text.SimpleDateFormat _$SimpleDateTimeFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Calendar _$curdate = java.util.Calendar.getInstance();
        _$curdate = java.util.Calendar.getInstance(java.util.Locale.CHINESE);
        String _$timeStr = _$SimpleDateTimeFormat.format(_$curdate.getTime());
        return _$timeStr;
    }

    /**
 * parse datetime string as  yyyy-MM-dd
 * @param timstr
 * @return
 * @throws java.lang.Exception
 */
    public String strToSimpleTimeFormat(String timstr) throws Exception {
        java.text.SimpleDateFormat $_SimpleDateTimeFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
        java.util.Date $_time = $_SimpleDateTimeFormat.parse(timstr);
        String $_timeStr = $_SimpleDateTimeFormat.format($_time);
        return $_timeStr;
    }

    public String strToSuperSimpleFormat(String timstr) throws Exception {
        java.text.SimpleDateFormat $_SimpleDateTimeFormat = new java.text.SimpleDateFormat("M-d");
        java.util.Date $_time = $_SimpleDateTimeFormat.parse(timstr);
        String $_timeStr = $_SimpleDateTimeFormat.format($_time);
        $_timeStr = $_timeStr.replaceAll("-", ".");
        return $_timeStr;
    }

    public String getDayOfWeek(String dateStr) {
        SimpleDateFormat formatYMD = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatD = new SimpleDateFormat("E");
        Date d = null;
        try {
            d = formatYMD.parse(dateStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return formatD.format(d);
    }

    public Date formatDate(String dateStr) {
        Format format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dt = null;
        try {
            dt = (Date) format.parseObject(dateStr);
        } catch (ParseException ex) {
            Logger.getLogger(datetime.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dt;
    }

    public String formatDate(Date dt) {
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateformat.format(dt);
    }

    public String formatDateStr(String dt) throws ParseException {
        SimpleDateFormat localTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date1 = localTime.parse(dt);
        return localTime.format(date1);
    }
}
