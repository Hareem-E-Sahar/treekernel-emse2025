package com.liferay.portlet.reverendfun.util;

import com.liferay.portal.kernel.util.CalendarFactoryUtil;
import com.liferay.portal.kernel.util.StringComparator;
import com.liferay.portal.util.WebCacheable;
import com.liferay.util.ConverterException;
import com.liferay.util.Http;
import com.liferay.util.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;
import java.util.TreeSet;

/**
 * <a href="ReverendFunConverter.java.html"><b><i>View Source</i></b></a>
 *
 * @author Brian Wing Shun Chan
 *
 */
public class ReverendFunConverter implements WebCacheable {

    public ReverendFunConverter(String date) {
        _date = date;
    }

    public Object convert(String id) throws ConverterException {
        Set dates = new TreeSet(new StringComparator(false, true));
        try {
            DateFormat dateFormatYMD = new SimpleDateFormat("yyyyMMdd");
            DateFormat dateFormatYM = new SimpleDateFormat("yyyyMM");
            Calendar cal = CalendarFactoryUtil.getCalendar();
            cal.setTime(dateFormatYMD.parse(_date));
            cal.set(Calendar.DATE, 1);
            Calendar now = CalendarFactoryUtil.getCalendar();
            String url = "http://www.reverendfun.com/artchives/?search=";
            while (cal.before(now)) {
                String text = Http.URLtoString(url + dateFormatYM.format(cal.getTime()));
                int x = text.indexOf("date=");
                int y = text.indexOf("\"", x);
                while (x != -1 && y != -1) {
                    String fromDateString = text.substring(x + 5, y);
                    dates.add(fromDateString);
                    x = text.indexOf("date=", y);
                    y = text.indexOf("\"", x);
                }
                cal.add(Calendar.MONTH, 1);
            }
        } catch (Exception e) {
            throw new ConverterException(_date + " " + e.toString());
        }
        return dates;
    }

    public long getRefreshTime() {
        return _REFRESH_TIME;
    }

    private static final long _REFRESH_TIME = Time.DAY;

    private String _date;
}
