package org.openbroad.client.user.view.control;

import java.io.*;
import java.util.*;
import java.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 *
 * @author  aslan
 */
public class Util {

    /** Creates a new instance of Control_Util */
    private Util() {
    }

    public static MouseInputAdapter changeClickColor() {
        MouseInputAdapter adapter = new MouseInputAdapter() {

            public void mousePressed(MouseEvent e) {
                checkModifier(e);
            }

            public void mouseClicked(MouseEvent e) {
                checkModifier(e);
            }

            public void mouseReleased(MouseEvent e) {
                checkModifier(e);
            }

            public void mouseDragged(MouseEvent e) {
                checkModifier(e);
            }

            private void checkModifier(MouseEvent e) {
                if (e.getModifiers() == InputEvent.BUTTON3_MASK) {
                    dispatchLeftClickEvent(e, e.getSource());
                }
            }

            private void dispatchLeftClickEvent(MouseEvent e, Object sender) {
                MouseEvent m = new MouseEvent((Component) sender, e.getID(), e.getWhen(), InputEvent.BUTTON1_MASK, e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger());
                ((Component) sender).dispatchEvent(m);
                e.consume();
            }
        };
        return adapter;
    }

    public static String getDate() {
        Calendar date = new GregorianCalendar();
        String day = ((date.get(date.DAY_OF_MONTH) < 10) ? "0" + date.get(date.DAY_OF_MONTH) : "" + date.get(date.DAY_OF_MONTH));
        String mon = ((date.get(date.MONTH) + 1) < 10) ? "0" + (date.get(date.MONTH) + 1) : "" + (date.get(date.MONTH) + 1);
        String year = "" + date.get(date.YEAR);
        return day + "-" + mon + "-" + year;
    }

    public static java.sql.Date convertToDate(String date) {
        if (date == null) return null;
        java.util.Date newDate = new java.util.Date();
        try {
            SimpleDateFormat dFormat = new SimpleDateFormat("dd-MM-yyyy");
            dFormat.setLenient(false);
            newDate = dFormat.parse(date);
        } catch (ParseException v) {
            System.out.println(v);
        }
        return new java.sql.Date(newDate.getTime());
    }

    public static String convertToString(Date date) {
        if (date == null) return null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        df.setLenient(false);
        java.util.Date newDate = new java.util.Date();
        try {
            newDate = df.parse(date.toString());
        } catch (ParseException e) {
            System.out.println(e);
        }
        df = new SimpleDateFormat("dd-MM-yyyy");
        return df.format(newDate);
    }

    public static Calendar toCalendar(String dateTime) throws ParseException {
        Calendar calendar = null;
        calendar = new GregorianCalendar();
        calendar.setTime(new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(dateTime));
        return calendar;
    }

    public static String calendarToString(Calendar cal) {
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        String hou = "" + hour;
        if (hour < 10) hou = "0" + hou;
        String min = "" + minute;
        if (minute < 10) min = "0" + min;
        String sec = "" + second;
        if (second < 10) sec = "0" + sec;
        return hou + ":" + min + ":" + sec;
    }

    public static String secondsBetweenTime(Calendar time1, Calendar time2) {
        time2.add(GregorianCalendar.SECOND, -time1.get(GregorianCalendar.SECOND) + 1);
        time2.add(GregorianCalendar.MINUTE, -time1.get(GregorianCalendar.MINUTE) + 1);
        time2.add(GregorianCalendar.HOUR_OF_DAY, -time1.get(GregorianCalendar.HOUR_OF_DAY) + 1);
        time2.add(GregorianCalendar.DAY_OF_MONTH, -time1.get(GregorianCalendar.DAY_OF_MONTH) + 1);
        time2.add(GregorianCalendar.MONTH, -time1.get(GregorianCalendar.MONTH));
        time2.add(GregorianCalendar.SECOND, -1);
        time2.add(GregorianCalendar.MINUTE, -1);
        time2.add(GregorianCalendar.HOUR_OF_DAY, -1);
        int seconds = time2.get(GregorianCalendar.SECOND);
        int minutes = time2.get(GregorianCalendar.MINUTE);
        int hours = time2.get(GregorianCalendar.HOUR_OF_DAY);
        int days = time2.get(GregorianCalendar.DAY_OF_MONTH) - 1;
        int months = time2.get(GregorianCalendar.MONTH);
        int years = time2.get(GregorianCalendar.YEAR) - time1.get(GregorianCalendar.YEAR);
        int sec = (minutes * 60) + seconds;
        return "" + sec;
    }

    public static String getUser() {
        return "aslani";
    }
}
