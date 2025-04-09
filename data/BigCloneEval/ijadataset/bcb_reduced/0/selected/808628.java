package eu.future.earth.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import junit.framework.TestCase;

public class WeekCounterTest extends TestCase {

    public void testGetWeekOfYear() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String start = "01/01/2002";
        GregorianCalendar walker = new GregorianCalendar();
        walker.setFirstDayOfWeek(Calendar.MONDAY);
        walker.setMinimalDaysInFirstWeek(4);
        walker.setTime(formatter.parse(start));
        eu.future.earth.gwt.emul.java.util.GregorianCalendar emul = new eu.future.earth.gwt.emul.java.util.GregorianCalendar();
        emul.setFirstDayOfWeek(Calendar.MONDAY);
        emul.setMinimalDaysInFirstWeek(4);
        for (int i = 0; i < 90000; i++) {
            emul.setTime(walker.getTime());
            int realWeek = walker.get(Calendar.WEEK_OF_YEAR);
            int emulWeek = emul.get(Calendar.WEEK_OF_YEAR);
            int week = WeekCounter.getWeekOfYear(walker.getTime(), Calendar.MONDAY, 4);
            assertEquals("Failed on " + formatter.format(walker.getTime()), realWeek, week);
            assertEquals("Failed on " + formatter.format(walker.getTime()), realWeek, emulWeek);
            walker.add(Calendar.DATE, 1);
        }
    }

    public void testGetWeekOfYear01() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = "04/01/2010";
        Date date = formatter.parse(dateString);
        GregorianCalendar weekTest = new GregorianCalendar();
        weekTest.setFirstDayOfWeek(Calendar.MONDAY);
        weekTest.setTime(date);
        weekTest.setMinimalDaysInFirstWeek(4);
        int week = WeekCounter.getWeekOfYear(date, Calendar.MONDAY, 4);
        int real = weekTest.get(Calendar.WEEK_OF_YEAR);
        assertEquals("Failed on " + formatter.format(date), real, week);
        eu.future.earth.gwt.emul.java.util.GregorianCalendar emul = new eu.future.earth.gwt.emul.java.util.GregorianCalendar();
        emul.setFirstDayOfWeek(Calendar.MONDAY);
        emul.setTime(date);
        emul.setMinimalDaysInFirstWeek(4);
        int emulTest = emul.get(Calendar.WEEK_OF_YEAR);
        assertEquals("Failed on " + formatter.format(date), real, emulTest);
    }

    public void testGetWeekOfYear01012006() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = "01/01/2006";
        Date date = formatter.parse(dateString);
        GregorianCalendar weekTest = new GregorianCalendar();
        weekTest.setFirstDayOfWeek(Calendar.MONDAY);
        weekTest.setTime(date);
        weekTest.setMinimalDaysInFirstWeek(4);
        int week = WeekCounter.getWeekOfYear(date, Calendar.MONDAY, 4);
        int real = weekTest.get(Calendar.WEEK_OF_YEAR);
        assertEquals("Failed on " + formatter.format(date), real, week);
        eu.future.earth.gwt.emul.java.util.GregorianCalendar emul = new eu.future.earth.gwt.emul.java.util.GregorianCalendar();
        emul.setFirstDayOfWeek(Calendar.MONDAY);
        emul.setMinimalDaysInFirstWeek(4);
        emul.setTime(date);
        int emulTest = emul.get(Calendar.WEEK_OF_YEAR);
        assertEquals("Failed on " + formatter.format(date), real, emulTest);
    }

    public void testGetWeekOfYear02012006() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = "02/01/2006";
        Date date = formatter.parse(dateString);
        GregorianCalendar weekTest = new GregorianCalendar();
        weekTest.setFirstDayOfWeek(Calendar.MONDAY);
        weekTest.setTime(date);
        weekTest.setMinimalDaysInFirstWeek(4);
        int week = WeekCounter.getWeekOfYear(date, Calendar.MONDAY, 4);
        int real = weekTest.get(Calendar.WEEK_OF_YEAR);
        assertEquals("Failed on " + formatter.format(date), real, week);
        eu.future.earth.gwt.emul.java.util.GregorianCalendar emul = new eu.future.earth.gwt.emul.java.util.GregorianCalendar();
        emul.setFirstDayOfWeek(Calendar.MONDAY);
        emul.setTime(date);
        emul.setMinimalDaysInFirstWeek(4);
        int emulTest = emul.get(Calendar.WEEK_OF_YEAR);
        assertEquals("Failed on " + formatter.format(date), real, emulTest);
    }

    public void testGetWeekOfYear31122007() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = "31/12/2007";
        Date date = formatter.parse(dateString);
        GregorianCalendar weekTest = new GregorianCalendar();
        weekTest.setFirstDayOfWeek(Calendar.MONDAY);
        weekTest.setTime(date);
        weekTest.setMinimalDaysInFirstWeek(4);
        int week = WeekCounter.getWeekOfYear(date, Calendar.MONDAY, 4);
        int real = weekTest.get(Calendar.WEEK_OF_YEAR);
        assertEquals("Failed on " + formatter.format(date), real, week);
        eu.future.earth.gwt.emul.java.util.GregorianCalendar emul = new eu.future.earth.gwt.emul.java.util.GregorianCalendar();
        emul.setFirstDayOfWeek(Calendar.MONDAY);
        emul.setTime(date);
        emul.setMinimalDaysInFirstWeek(4);
        int emulTest = emul.get(Calendar.WEEK_OF_YEAR);
        assertEquals("Failed on " + formatter.format(date), real, emulTest);
    }

    public void testGetWeekOfYear07012008() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = "07/01/2008";
        Date date = formatter.parse(dateString);
        GregorianCalendar weekTest = new GregorianCalendar();
        weekTest.setFirstDayOfWeek(Calendar.MONDAY);
        weekTest.setTime(date);
        weekTest.setMinimalDaysInFirstWeek(4);
        int week = WeekCounter.getWeekOfYear(date, Calendar.MONDAY, 4);
        int real = weekTest.get(Calendar.WEEK_OF_YEAR);
        assertEquals("Failed on " + formatter.format(date), real, week);
        eu.future.earth.gwt.emul.java.util.GregorianCalendar emul = new eu.future.earth.gwt.emul.java.util.GregorianCalendar();
        emul.setFirstDayOfWeek(Calendar.MONDAY);
        emul.setTime(date);
        emul.setMinimalDaysInFirstWeek(4);
        int emulTest = emul.get(Calendar.WEEK_OF_YEAR);
        assertEquals("Failed on " + formatter.format(date), real, emulTest);
    }

    public void testGetWeekOfYear29122008() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = "29/12/2008";
        Date date = formatter.parse(dateString);
        GregorianCalendar weekTest = new GregorianCalendar();
        weekTest.setFirstDayOfWeek(Calendar.MONDAY);
        weekTest.setTime(date);
        weekTest.setMinimalDaysInFirstWeek(4);
        int week = WeekCounter.getWeekOfYear(date, Calendar.MONDAY, 4);
        int real = weekTest.get(Calendar.WEEK_OF_YEAR);
        assertEquals("Failed on " + formatter.format(date), real, week);
        eu.future.earth.gwt.emul.java.util.GregorianCalendar emul = new eu.future.earth.gwt.emul.java.util.GregorianCalendar();
        emul.setFirstDayOfWeek(Calendar.MONDAY);
        emul.setTime(date);
        emul.setMinimalDaysInFirstWeek(4);
        int emulTest = emul.get(Calendar.WEEK_OF_YEAR);
        assertEquals("Failed on " + formatter.format(date), real, emulTest);
    }

    public void testGetWeekOfYear01012010() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = "01/01/2010";
        Date date = formatter.parse(dateString);
        GregorianCalendar weekTest = new GregorianCalendar();
        weekTest.setFirstDayOfWeek(Calendar.MONDAY);
        weekTest.setTime(date);
        weekTest.setMinimalDaysInFirstWeek(4);
        int week = WeekCounter.getWeekOfYear(date, Calendar.MONDAY, 4);
        int real = weekTest.get(Calendar.WEEK_OF_YEAR);
        assertEquals("Failed on " + formatter.format(date), real, week);
        eu.future.earth.gwt.emul.java.util.GregorianCalendar emul = new eu.future.earth.gwt.emul.java.util.GregorianCalendar();
        emul.setFirstDayOfWeek(Calendar.MONDAY);
        emul.setTime(date);
        emul.setMinimalDaysInFirstWeek(4);
        int emulTest = emul.get(Calendar.WEEK_OF_YEAR);
        assertEquals("Failed on " + formatter.format(date), real, emulTest);
    }

    public void testCompare() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = "01/01/2006";
        Date date = formatter.parse(dateString);
        String dateString02 = "26/12/2005";
        Date date02 = formatter.parse(dateString02);
        int diff = WeekCounter.compareDate(date02, date);
        assertEquals(6, diff);
    }
}
