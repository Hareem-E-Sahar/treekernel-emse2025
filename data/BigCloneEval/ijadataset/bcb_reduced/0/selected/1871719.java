package org.blojsom.plugin.calendar;

import org.blojsom.util.BlojsomUtils;
import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * BlogCalendar
 *
 * @author David Czarnecki
 * @author Mark Lussier
 * @since blojsom 3.0
 * @version $Id: BlogCalendar.java,v 1.1 2006/03/20 21:30:53 czarneckid Exp $
 */
public class BlogCalendar {

    private Calendar _calendar;

    private Calendar _today;

    private DateFormatSymbols _symbols;

    private Locale _locale;

    private Boolean[] _dayswithentry;

    private String[] _shortdownames;

    private String _blogURL;

    private int currentmonth;

    private int currentyear;

    private int currentday;

    private String _requestedDateKey;

    /**
     * Public Constructor
     *
     * @param calendar Caledar instance
     * @param blogurl  The blog's url for calendar navigation
     */
    public BlogCalendar(Calendar calendar, String blogurl) {
        this(calendar, blogurl, Locale.getDefault());
    }

    /**
     * Public Constructor
     *
     * @param calendar Caledar instance
     * @param blogurl  The blog's url for calendar navigation
     * @param locale   Locale for the Calendar
     */
    public BlogCalendar(Calendar calendar, String blogurl, Locale locale) {
        _locale = locale;
        _calendar = calendar;
        _today = Calendar.getInstance(_locale);
        _today.setTime(new Date());
        _symbols = new DateFormatSymbols(_locale);
        _blogURL = blogurl;
        currentmonth = calendar.get(Calendar.MONTH);
        currentyear = calendar.get(Calendar.YEAR);
        currentday = calendar.get(Calendar.DAY_OF_MONTH);
        _dayswithentry = new Boolean[_calendar.getActualMaximum(Calendar.DAY_OF_MONTH)];
        Arrays.fill(_dayswithentry, Boolean.FALSE);
        _shortdownames = new String[7];
        String[] downames = _symbols.getShortWeekdays();
        if (_calendar.getFirstDayOfWeek() == Calendar.SUNDAY) {
            for (int x = 0; x < _shortdownames.length; x++) {
                _shortdownames[x] = downames[x + 1];
            }
        } else {
            for (int x = 2; x <= _shortdownames.length; x++) {
                _shortdownames[x - 2] = downames[x];
            }
            _shortdownames[6] = downames[1];
        }
    }

    /**
     * Returns the current Month as MMMMM yyyy (ex: March 2003)
     *
     * @return the current month and year as a string
     */
    public String getCaption() {
        return BlojsomUtils.getFormattedDate(_calendar.getTime(), AbstractCalendarPlugin.BLOJSOM_CALENDAR_FORMAT, _locale);
    }

    /**
     * Returns the day of the week for the 1st of the month occurs on
     *
     * @return the day of the week for the 1st of the month as an int
     */
    public int getFirstDayOfMonth() {
        _calendar.set(Calendar.DAY_OF_MONTH, 1);
        return _calendar.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * Returns the number of days in the current month
     *
     * @return days in this month
     */
    public int getDaysInMonth() {
        return _calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * Flag a day in the current month as having entries
     *
     * @param dom the day of the month
     */
    public void setEntryForDOM(int dom) {
        if (dom > 0 && dom <= _dayswithentry.length) {
            _dayswithentry[dom - 1] = Boolean.valueOf(true);
        }
    }

    /**
     * Flag a day in the current month as NOT having entries
     *
     * @param dom the day of the month
     */
    public void removeEntryForDOM(int dom) {
        if (dom > 0 && dom <= _dayswithentry.length) {
            _dayswithentry[dom - 1] = Boolean.valueOf(false);
        }
    }

    /**
     * Determines if a day of the month has entries
     *
     * @param dom the day of the month
     * @return a boolean indicating entries exist
     */
    public boolean dayHasEntry(int dom) {
        boolean result = false;
        if (dom > 0 && dom <= _dayswithentry.length) {
            result = _dayswithentry[dom - 1].booleanValue();
        }
        return result;
    }

    /**
     * Get the array of day of month entry flags
     *
     * @return a boolean array of the months entries
     */
    public Boolean[] getEntryDates() {
        return _dayswithentry;
    }

    /**
     * Get the localized name of the given month
     *
     * @param month the month (as defined by Calendar)
     * @return the month as a localized string
     */
    public String getMonthName(int month) {
        return getMonthNames()[month];
    }

    /**
     * Get a list of the month names (localized)
     *
     * @return String array of localized month names
     */
    public String[] getMonthNames() {
        return _symbols.getMonths();
    }

    /**
     * Get the localized abbreviated name of the given month
     *
     * @param month the month (as defined by Calendar)
     * @return the abbreviated month as a localized string (ex: Feb)
     */
    public String getShortMonthName(int month) {
        return getShortMonthNames()[month];
    }

    /**
     * Get a list of the abbreviated month names (localized)
     *
     * @return String array of localized abbreviated month names
     */
    public String[] getShortMonthNames() {
        return _symbols.getShortMonths();
    }

    /**
     * Get the localized name of a Day of the Week
     *
     * @param dow the day of the week (as defined by Calendar)
     * @return the day of the week as a localized string
     */
    public String getDayOfWeekName(int dow) {
        return getDayOfWeekNames()[dow];
    }

    /**
     * Get a lit of the day of the week names (localized)
     *
     * @return String array of localized Day of Week names
     */
    public String[] getDayOfWeekNames() {
        return _symbols.getWeekdays();
    }

    /**
     * Get the localized abbreviated name of a Day of the Week
     *
     * @param dow the day of the week (as defined by Calendar)
     * @return the abbreviated day of the week as a localized string
     */
    public String getShortDayOfWeekName(int dow) {
        return _shortdownames[dow - 1];
    }

    /**
     * Get a lit of the abbreviated day of the week names (localized)
     *
     * @return String array of localized abbreviated Day of Week names
     */
    public String[] getShortDayOfWeekNames() {
        return _shortdownames;
    }

    /**
     * Get the Blog URL used by the calendar
     *
     * @return the blog url
     */
    public String getCalendarUrl() {
        return _blogURL;
    }

    /**
     * Get the Calendar instance
     *
     * @return Calendar instance
     */
    public Calendar getCalendar() {
        return _calendar;
    }

    /**
     * Get today as a Calendar instance
     *
     * @return Calendar instance
     */
    public Calendar getToday() {
        return _today;
    }

    /**
     * Gets the current month for this Calendar
     *
     * @return current month as an int (as defined by Calendar)
     */
    public int getCurrentMonth() {
        return currentmonth;
    }

    /**
     * Sets the current month for this Calendar
     *
     * @param currentmonth current month as an int (as defined by Calendar)
     */
    public void setCurrentMonth(int currentmonth) {
        this.currentmonth = currentmonth;
        _calendar.set(Calendar.MONTH, currentmonth);
    }

    /**
     * Gets the current year for this Calendar
     *
     * @return current year as an int (as defined by Calendar)
     */
    public int getCurrentYear() {
        return currentyear;
    }

    /**
     * Sets the current year for this Calendar
     *
     * @param currentyear current year as an int (as defined by Calendar)
     */
    public void setCurrentYear(int currentyear) {
        this.currentyear = currentyear;
        _calendar.set(Calendar.YEAR, currentyear);
    }

    /**
     * Gets the current day for this Calendar
     *
     * @return current day as an int (as defined by Calendar)
     */
    public int getCurrentDay() {
        return currentday;
    }

    /**
     * Sets the current day for this Calendar
     *
     * @param currentday current day as an int (as defined by Calendar)
     */
    public void setCurrentDay(int currentday) {
        this.currentday = currentday;
        _calendar.set(Calendar.DAY_OF_MONTH, currentday);
    }

    /**
     * Gets the current entry date match key (year+month+day)
     *
     * @return Date match key as a String
     */
    public String getRequestedDateKey() {
        return _requestedDateKey;
    }

    /**
     * Sets the current entry date match key (year+month+day)
     *
     * @param requestedDateKey current entry match key
     */
    public void setRequestedDateKey(String requestedDateKey) {
        _requestedDateKey = requestedDateKey;
    }
}
