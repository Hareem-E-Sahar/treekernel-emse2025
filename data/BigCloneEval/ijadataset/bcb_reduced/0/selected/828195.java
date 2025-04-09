package com.ibm.icu.util;

import java.util.Date;
import java.util.Locale;

/**
 * <code>GregorianCalendar</code> is a concrete subclass of
 * {@link Calendar}
 * and provides the standard calendar used by most of the world.
 *
 * <p>
 * The standard (Gregorian) calendar has 2 eras, BC and AD.
 *
 * <p>
 * This implementation handles a single discontinuity, which corresponds by
 * default to the date the Gregorian calendar was instituted (October 15, 1582
 * in some countries, later in others).  The cutover date may be changed by the
 * caller by calling <code>setGregorianChange()</code>.
 *
 * <p>
 * Historically, in those countries which adopted the Gregorian calendar first,
 * October 4, 1582 was thus followed by October 15, 1582. This calendar models
 * this correctly.  Before the Gregorian cutover, <code>GregorianCalendar</code>
 * implements the Julian calendar.  The only difference between the Gregorian
 * and the Julian calendar is the leap year rule. The Julian calendar specifies
 * leap years every four years, whereas the Gregorian calendar omits century
 * years which are not divisible by 400.
 *
 * <p>
 * <code>GregorianCalendar</code> implements <em>proleptic</em> Gregorian and
 * Julian calendars. That is, dates are computed by extrapolating the current
 * rules indefinitely far backward and forward in time. As a result,
 * <code>GregorianCalendar</code> may be used for all years to generate
 * meaningful and consistent results. However, dates obtained using
 * <code>GregorianCalendar</code> are historically accurate only from March 1, 4
 * AD onward, when modern Julian calendar rules were adopted.  Before this date,
 * leap year rules were applied irregularly, and before 45 BC the Julian
 * calendar did not even exist.
 *
 * <p>
 * Prior to the institution of the Gregorian calendar, New Year's Day was
 * March 25. To avoid confusion, this calendar always uses January 1. A manual
 * adjustment may be made if desired for dates that are prior to the Gregorian
 * changeover and which fall between January 1 and March 24.
 *
 * <p>Values calculated for the <code>WEEK_OF_YEAR</code> field range from 1 to
 * 53.  Week 1 for a year is the earliest seven day period starting on
 * <code>getFirstDayOfWeek()</code> that contains at least
 * <code>getMinimalDaysInFirstWeek()</code> days from that year.  It thus
 * depends on the values of <code>getMinimalDaysInFirstWeek()</code>,
 * <code>getFirstDayOfWeek()</code>, and the day of the week of January 1.
 * Weeks between week 1 of one year and week 1 of the following year are
 * numbered sequentially from 2 to 52 or 53 (as needed).

 * <p>For example, January 1, 1998 was a Thursday.  If
 * <code>getFirstDayOfWeek()</code> is <code>MONDAY</code> and
 * <code>getMinimalDaysInFirstWeek()</code> is 4 (these are the values
 * reflecting ISO 8601 and many national standards), then week 1 of 1998 starts
 * on December 29, 1997, and ends on January 4, 1998.  If, however,
 * <code>getFirstDayOfWeek()</code> is <code>SUNDAY</code>, then week 1 of 1998
 * starts on January 4, 1998, and ends on January 10, 1998; the first three days
 * of 1998 then are part of week 53 of 1997.
 *
 * <p>Values calculated for the <code>WEEK_OF_MONTH</code> field range from 0 or
 * 1 to 4 or 5.  Week 1 of a month (the days with <code>WEEK_OF_MONTH =
 * 1</code>) is the earliest set of at least
 * <code>getMinimalDaysInFirstWeek()</code> contiguous days in that month,
 * ending on the day before <code>getFirstDayOfWeek()</code>.  Unlike
 * week 1 of a year, week 1 of a month may be shorter than 7 days, need
 * not start on <code>getFirstDayOfWeek()</code>, and will not include days of
 * the previous month.  Days of a month before week 1 have a
 * <code>WEEK_OF_MONTH</code> of 0.
 *
 * <p>For example, if <code>getFirstDayOfWeek()</code> is <code>SUNDAY</code>
 * and <code>getMinimalDaysInFirstWeek()</code> is 4, then the first week of
 * January 1998 is Sunday, January 4 through Saturday, January 10.  These days
 * have a <code>WEEK_OF_MONTH</code> of 1.  Thursday, January 1 through
 * Saturday, January 3 have a <code>WEEK_OF_MONTH</code> of 0.  If
 * <code>getMinimalDaysInFirstWeek()</code> is changed to 3, then January 1
 * through January 3 have a <code>WEEK_OF_MONTH</code> of 1.
 *
 * <p>
 * <strong>Example:</strong>
 * <blockquote>
 * <pre>
 * // get the supported ids for GMT-08:00 (Pacific Standard Time)
 * String[] ids = TimeZone.getAvailableIDs(-8 * 60 * 60 * 1000);
 * // if no ids were returned, something is wrong. get out.
 * if (ids.length == 0)
 *     System.exit(0);
 *
 *  // begin output
 * System.out.println("Current Time");
 *
 * // create a Pacific Standard Time time zone
 * SimpleTimeZone pdt = new SimpleTimeZone(-8 * 60 * 60 * 1000, ids[0]);
 *
 * // set up rules for daylight savings time
 * pdt.setStartRule(Calendar.APRIL, 1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
 * pdt.setEndRule(Calendar.OCTOBER, -1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
 *
 * // create a GregorianCalendar with the Pacific Daylight time zone
 * // and the current date and time
 * Calendar calendar = new GregorianCalendar(pdt);
 * Date trialTime = new Date();
 * calendar.setTime(trialTime);
 *
 * // print out a bunch of interesting things
 * System.out.println("ERA: " + calendar.get(Calendar.ERA));
 * System.out.println("YEAR: " + calendar.get(Calendar.YEAR));
 * System.out.println("MONTH: " + calendar.get(Calendar.MONTH));
 * System.out.println("WEEK_OF_YEAR: " + calendar.get(Calendar.WEEK_OF_YEAR));
 * System.out.println("WEEK_OF_MONTH: " + calendar.get(Calendar.WEEK_OF_MONTH));
 * System.out.println("DATE: " + calendar.get(Calendar.DATE));
 * System.out.println("DAY_OF_MONTH: " + calendar.get(Calendar.DAY_OF_MONTH));
 * System.out.println("DAY_OF_YEAR: " + calendar.get(Calendar.DAY_OF_YEAR));
 * System.out.println("DAY_OF_WEEK: " + calendar.get(Calendar.DAY_OF_WEEK));
 * System.out.println("DAY_OF_WEEK_IN_MONTH: "
 *                    + calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH));
 * System.out.println("AM_PM: " + calendar.get(Calendar.AM_PM));
 * System.out.println("HOUR: " + calendar.get(Calendar.HOUR));
 * System.out.println("HOUR_OF_DAY: " + calendar.get(Calendar.HOUR_OF_DAY));
 * System.out.println("MINUTE: " + calendar.get(Calendar.MINUTE));
 * System.out.println("SECOND: " + calendar.get(Calendar.SECOND));
 * System.out.println("MILLISECOND: " + calendar.get(Calendar.MILLISECOND));
 * System.out.println("ZONE_OFFSET: "
 *                    + (calendar.get(Calendar.ZONE_OFFSET)/(60*60*1000)));
 * System.out.println("DST_OFFSET: "
 *                    + (calendar.get(Calendar.DST_OFFSET)/(60*60*1000)));

 * System.out.println("Current Time, with hour reset to 3");
 * calendar.clear(Calendar.HOUR_OF_DAY); // so doesn't override
 * calendar.set(Calendar.HOUR, 3);
 * System.out.println("ERA: " + calendar.get(Calendar.ERA));
 * System.out.println("YEAR: " + calendar.get(Calendar.YEAR));
 * System.out.println("MONTH: " + calendar.get(Calendar.MONTH));
 * System.out.println("WEEK_OF_YEAR: " + calendar.get(Calendar.WEEK_OF_YEAR));
 * System.out.println("WEEK_OF_MONTH: " + calendar.get(Calendar.WEEK_OF_MONTH));
 * System.out.println("DATE: " + calendar.get(Calendar.DATE));
 * System.out.println("DAY_OF_MONTH: " + calendar.get(Calendar.DAY_OF_MONTH));
 * System.out.println("DAY_OF_YEAR: " + calendar.get(Calendar.DAY_OF_YEAR));
 * System.out.println("DAY_OF_WEEK: " + calendar.get(Calendar.DAY_OF_WEEK));
 * System.out.println("DAY_OF_WEEK_IN_MONTH: "
 *                    + calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH));
 * System.out.println("AM_PM: " + calendar.get(Calendar.AM_PM));
 * System.out.println("HOUR: " + calendar.get(Calendar.HOUR));
 * System.out.println("HOUR_OF_DAY: " + calendar.get(Calendar.HOUR_OF_DAY));
 * System.out.println("MINUTE: " + calendar.get(Calendar.MINUTE));
 * System.out.println("SECOND: " + calendar.get(Calendar.SECOND));
 * System.out.println("MILLISECOND: " + calendar.get(Calendar.MILLISECOND));
 * System.out.println("ZONE_OFFSET: "
 *        + (calendar.get(Calendar.ZONE_OFFSET)/(60*60*1000))); // in hours
 * System.out.println("DST_OFFSET: "
 *        + (calendar.get(Calendar.DST_OFFSET)/(60*60*1000))); // in hours
 * </pre>
 * </blockquote>
 *
 * @see          Calendar
 * @see          TimeZone
 * @author David Goldsmith, Mark Davis, Chen-Lieh Huang, Alan Liu
 * @stable ICU 2.0
 */
public class GregorianCalendar extends Calendar {

    /**
     * Value of the <code>ERA</code> field indicating
     * the period before the common era (before Christ), also known as BCE.
     * The sequence of years at the transition from <code>BC</code> to <code>AD</code> is
     * ..., 2 BC, 1 BC, 1 AD, 2 AD,...
     * @see Calendar#ERA
     * @stable ICU 2.0
     */
    public static final int BC = 0;

    /**
     * Value of the <code>ERA</code> field indicating
     * the common era (Anno Domini), also known as CE.
     * The sequence of years at the transition from <code>BC</code> to <code>AD</code> is
     * ..., 2 BC, 1 BC, 1 AD, 2 AD,...
     * @see Calendar#ERA
     * @stable ICU 2.0
     */
    public static final int AD = 1;

    private static final int EPOCH_YEAR = 1970;

    private static final int[][] MONTH_COUNT = { { 31, 31, 0, 0 }, { 28, 29, 31, 31 }, { 31, 31, 59, 60 }, { 30, 30, 90, 91 }, { 31, 31, 120, 121 }, { 30, 30, 151, 152 }, { 31, 31, 181, 182 }, { 31, 31, 212, 213 }, { 30, 30, 243, 244 }, { 31, 31, 273, 274 }, { 30, 30, 304, 305 }, { 31, 31, 334, 335 } };

    /**
     * Old year limits were least max 292269054, max 292278994.
     */
    private static final int LIMITS[][] = { { 0, 0, 1, 1 }, { 1, 1, 5828963, 5838270 }, { 0, 0, 11, 11 }, { 1, 1, 52, 53 }, { 0, 0, 4, 6 }, { 1, 1, 28, 31 }, { 1, 1, 365, 366 }, {}, { -1, -1, 4, 6 }, {}, {}, {}, {}, {}, {}, {}, {}, { -5838270, -5838270, 5828964, 5838271 }, {}, { -5838269, -5838269, 5828963, 5838270 }, {}, {} };

    /**
     * @stable ICU 2.0
     */
    protected int handleGetLimit(int field, int limitType) {
        return LIMITS[field][limitType];
    }

    /**
     * The point at which the Gregorian calendar rules are used, measured in
     * milliseconds from the standard epoch.  Default is October 15, 1582
     * (Gregorian) 00:00:00 UTC or -12219292800000L.  For this value, October 4,
     * 1582 (Julian) is followed by October 15, 1582 (Gregorian).  This
     * corresponds to Julian day number 2299161.
     * @serial
     */
    private long gregorianCutover = -12219292800000L;

    /**
     * Julian day number of the Gregorian cutover.
     */
    private transient int cutoverJulianDay = 2299161;

    /**
     * The year of the gregorianCutover, with 0 representing
     * 1 BC, -1 representing 2 BC, etc.
     */
    private transient int gregorianCutoverYear = 1582;

    /**
     * Used by handleComputeJulianDay() and handleComputeMonthStart().
     * @stable ICU 2.0
     */
    protected transient boolean isGregorian;

    /**
     * Used by handleComputeJulianDay() and handleComputeMonthStart().
     * @stable ICU 2.0
     */
    protected transient boolean invertGregorian;

    /**
     * Constructs a default GregorianCalendar using the current time
     * in the default time zone with the default locale.
     * @stable ICU 2.0
     */
    public GregorianCalendar() {
        this(TimeZone.getDefault(), Locale.getDefault());
    }

    /**
     * Constructs a GregorianCalendar based on the current time
     * in the given time zone with the default locale.
     * @param zone the given time zone.
     * @stable ICU 2.0
     */
    public GregorianCalendar(TimeZone zone) {
        this(zone, Locale.getDefault());
    }

    /**
     * Constructs a GregorianCalendar based on the current time
     * in the default time zone with the given locale.
     * @param aLocale the given locale.
     * @stable ICU 2.0
     */
    public GregorianCalendar(Locale aLocale) {
        this(TimeZone.getDefault(), aLocale);
    }

    /**
     * Constructs a GregorianCalendar based on the current time
     * in the given time zone with the given locale.
     * @param zone the given time zone.
     * @param aLocale the given locale.
     * @stable ICU 2.0
     */
    public GregorianCalendar(TimeZone zone, Locale aLocale) {
        super(zone, aLocale);
        setTimeInMillis(System.currentTimeMillis());
    }

    /**
     * Constructs a GregorianCalendar with the given date set
     * in the default time zone with the default locale.
     * @param year the value used to set the YEAR time field in the calendar.
     * @param month the value used to set the MONTH time field in the calendar.
     * Month value is 0-based. e.g., 0 for January.
     * @param date the value used to set the DATE time field in the calendar.
     * @stable ICU 2.0
     */
    public GregorianCalendar(int year, int month, int date) {
        super(TimeZone.getDefault(), Locale.getDefault());
        set(ERA, AD);
        set(YEAR, year);
        set(MONTH, month);
        set(DATE, date);
    }

    /**
     * Constructs a GregorianCalendar with the given date
     * and time set for the default time zone with the default locale.
     * @param year the value used to set the YEAR time field in the calendar.
     * @param month the value used to set the MONTH time field in the calendar.
     * Month value is 0-based. e.g., 0 for January.
     * @param date the value used to set the DATE time field in the calendar.
     * @param hour the value used to set the HOUR_OF_DAY time field
     * in the calendar.
     * @param minute the value used to set the MINUTE time field
     * in the calendar.
     * @stable ICU 2.0
     */
    public GregorianCalendar(int year, int month, int date, int hour, int minute) {
        super(TimeZone.getDefault(), Locale.getDefault());
        set(ERA, AD);
        set(YEAR, year);
        set(MONTH, month);
        set(DATE, date);
        set(HOUR_OF_DAY, hour);
        set(MINUTE, minute);
    }

    /**
     * Constructs a GregorianCalendar with the given date
     * and time set for the default time zone with the default locale.
     * @param year the value used to set the YEAR time field in the calendar.
     * @param month the value used to set the MONTH time field in the calendar.
     * Month value is 0-based. e.g., 0 for January.
     * @param date the value used to set the DATE time field in the calendar.
     * @param hour the value used to set the HOUR_OF_DAY time field
     * in the calendar.
     * @param minute the value used to set the MINUTE time field
     * in the calendar.
     * @param second the value used to set the SECOND time field
     * in the calendar.
     * @stable ICU 2.0
     */
    public GregorianCalendar(int year, int month, int date, int hour, int minute, int second) {
        super(TimeZone.getDefault(), Locale.getDefault());
        set(ERA, AD);
        set(YEAR, year);
        set(MONTH, month);
        set(DATE, date);
        set(HOUR_OF_DAY, hour);
        set(MINUTE, minute);
        set(SECOND, second);
    }

    /**
     * Sets the GregorianCalendar change date. This is the point when the switch
     * from Julian dates to Gregorian dates occurred. Default is October 15,
     * 1582. Previous to this, dates will be in the Julian calendar.
     * <p>
     * To obtain a pure Julian calendar, set the change date to
     * <code>Date(Long.MAX_VALUE)</code>.  To obtain a pure Gregorian calendar,
     * set the change date to <code>Date(Long.MIN_VALUE)</code>.
     *
     * @param date the given Gregorian cutover date.
     * @stable ICU 2.0
     */
    public void setGregorianChange(Date date) {
        gregorianCutover = date.getTime();
        if (gregorianCutover <= MIN_MILLIS) {
            gregorianCutoverYear = cutoverJulianDay = Integer.MIN_VALUE;
        } else if (gregorianCutover >= MAX_MILLIS) {
            gregorianCutoverYear = cutoverJulianDay = Integer.MAX_VALUE;
        } else {
            cutoverJulianDay = (int) floorDivide(gregorianCutover, ONE_DAY);
            GregorianCalendar cal = new GregorianCalendar(getTimeZone());
            cal.setTime(date);
            gregorianCutoverYear = cal.get(EXTENDED_YEAR);
        }
    }

    /**
     * Gets the Gregorian Calendar change date.  This is the point when the
     * switch from Julian dates to Gregorian dates occurred. Default is
     * October 15, 1582. Previous to this, dates will be in the Julian
     * calendar.
     * @return the Gregorian cutover date for this calendar.
     * @stable ICU 2.0
     */
    public final Date getGregorianChange() {
        return new Date(gregorianCutover);
    }

    /**
     * Determines if the given year is a leap year. Returns true if the
     * given year is a leap year.
     * @param year the given year.
     * @return true if the given year is a leap year; false otherwise.
     * @stable ICU 2.0
     */
    public boolean isLeapYear(int year) {
        return year >= gregorianCutoverYear ? ((year % 4 == 0) && ((year % 100 != 0) || (year % 400 == 0))) : (year % 4 == 0);
    }

    /**
     * Returns true if the given Calendar object is equivalent to this
     * one.  Calendar override.
     *
     * @param other the Calendar to be compared with this Calendar   
     * @draft ICU 2.4
     */
    public boolean isEquivalentTo(Calendar other) {
        return super.isEquivalentTo(other) && gregorianCutover == ((GregorianCalendar) other).gregorianCutover;
    }

    /**
     * Override hashCode.
     * Generates the hash code for the GregorianCalendar object
     * @stable ICU 2.0
     */
    public int hashCode() {
        return super.hashCode() ^ (int) gregorianCutover;
    }

    /**
     * Roll a field by a signed amount.
     * @stable ICU 2.0
     */
    public void roll(int field, int amount) {
        switch(field) {
            case WEEK_OF_YEAR:
                {
                    int woy = get(WEEK_OF_YEAR);
                    int isoYear = get(YEAR_WOY);
                    int isoDoy = internalGet(DAY_OF_YEAR);
                    if (internalGet(MONTH) == Calendar.JANUARY) {
                        if (woy >= 52) {
                            isoDoy += handleGetYearLength(isoYear);
                        }
                    } else {
                        if (woy == 1) {
                            isoDoy -= handleGetYearLength(isoYear - 1);
                        }
                    }
                    woy += amount;
                    if (woy < 1 || woy > 52) {
                        int lastDoy = handleGetYearLength(isoYear);
                        int lastRelDow = (lastDoy - isoDoy + internalGet(DAY_OF_WEEK) - getFirstDayOfWeek()) % 7;
                        if (lastRelDow < 0) lastRelDow += 7;
                        if ((6 - lastRelDow) >= getMinimalDaysInFirstWeek()) lastDoy -= 7;
                        int lastWoy = weekNumber(lastDoy, lastRelDow + 1);
                        woy = ((woy + lastWoy - 1) % lastWoy) + 1;
                    }
                    set(WEEK_OF_YEAR, woy);
                    set(YEAR, isoYear);
                    return;
                }
            default:
                super.roll(field, amount);
                return;
        }
    }

    /**
     * Return the minimum value that this field could have, given the current date.
     * For the Gregorian calendar, this is the same as getMinimum() and getGreatestMinimum().
     * @stable ICU 2.0
     */
    public int getActualMinimum(int field) {
        return getMinimum(field);
    }

    /**
     * Return the maximum value that this field could have, given the current date.
     * For example, with the date "Feb 3, 1997" and the DAY_OF_MONTH field, the actual
     * maximum would be 28; for "Feb 3, 1996" it s 29.  Similarly for a Hebrew calendar,
     * for some years the actual maximum for MONTH is 12, and for others 13.
     * @stable ICU 2.0
     */
    public int getActualMaximum(int field) {
        switch(field) {
            case YEAR:
                {
                    Calendar cal = (Calendar) clone();
                    cal.setLenient(true);
                    int era = cal.get(ERA);
                    Date d = cal.getTime();
                    int lowGood = LIMITS[YEAR][1];
                    int highBad = LIMITS[YEAR][2] + 1;
                    while ((lowGood + 1) < highBad) {
                        int y = (lowGood + highBad) / 2;
                        cal.set(YEAR, y);
                        if (cal.get(YEAR) == y && cal.get(ERA) == era) {
                            lowGood = y;
                        } else {
                            highBad = y;
                            cal.setTime(d);
                        }
                    }
                    return lowGood;
                }
            default:
                return super.getActualMaximum(field);
        }
    }

    /**
     * Return true if the current time for this Calendar is in Daylignt
     * Savings Time.
     *
     * Note -- MAKE THIS PUBLIC AT THE NEXT API CHANGE.  POSSIBLY DEPRECATE
     * AND REMOVE TimeZone.inDaylightTime().
     */
    boolean inDaylightTime() {
        if (!getTimeZone().useDaylightTime()) return false;
        complete();
        return internalGet(DST_OFFSET) != 0;
    }

    /**
     * @stable ICU 2.0
     */
    protected int handleGetMonthLength(int extendedYear, int month) {
        return MONTH_COUNT[month][isLeapYear(extendedYear) ? 1 : 0];
    }

    /**
     * @stable ICU 2.0
     */
    protected int handleGetYearLength(int eyear) {
        return isLeapYear(eyear) ? 366 : 365;
    }

    /**
     * Override Calendar to compute several fields specific to the hybrid
     * Gregorian-Julian calendar system.  These are:
     *
     * <ul><li>ERA
     * <li>YEAR
     * <li>MONTH
     * <li>DAY_OF_MONTH
     * <li>DAY_OF_YEAR
     * <li>EXTENDED_YEAR</ul>
     * @stable ICU 2.0
     */
    protected void handleComputeFields(int julianDay) {
        int eyear, month, dayOfMonth, dayOfYear;
        if (julianDay >= cutoverJulianDay) {
            month = getGregorianMonth();
            dayOfMonth = getGregorianDayOfMonth();
            dayOfYear = getGregorianDayOfYear();
            eyear = getGregorianYear();
        } else {
            long julianEpochDay = julianDay - (JAN_1_1_JULIAN_DAY - 2);
            eyear = (int) floorDivide(4 * julianEpochDay + 1464, 1461);
            long january1 = 365 * (eyear - 1) + floorDivide(eyear - 1, 4);
            dayOfYear = (int) (julianEpochDay - january1);
            boolean isLeap = ((eyear & 0x3) == 0);
            int correction = 0;
            int march1 = isLeap ? 60 : 59;
            if (dayOfYear >= march1) {
                correction = isLeap ? 1 : 2;
            }
            month = (12 * (dayOfYear + correction) + 6) / 367;
            dayOfMonth = dayOfYear - MONTH_COUNT[month][isLeap ? 3 : 2] + 1;
            ++dayOfYear;
        }
        internalSet(MONTH, month);
        internalSet(DAY_OF_MONTH, dayOfMonth);
        internalSet(DAY_OF_YEAR, dayOfYear);
        internalSet(EXTENDED_YEAR, eyear);
        int era = AD;
        if (eyear < 1) {
            era = BC;
            eyear = 1 - eyear;
        }
        internalSet(ERA, era);
        internalSet(YEAR, eyear);
    }

    /**
     * @stable ICU 2.0
     */
    protected int handleGetExtendedYear() {
        int year;
        if (newerField(EXTENDED_YEAR, YEAR) == EXTENDED_YEAR) {
            year = internalGet(EXTENDED_YEAR, EPOCH_YEAR);
        } else {
            int era = internalGet(ERA, AD);
            if (era == BC) {
                year = 1 - internalGet(YEAR, 1);
            } else {
                year = internalGet(YEAR, EPOCH_YEAR);
            }
        }
        return year;
    }

    /**
     * Override Calendar to improve performance.  This method tries to use
     * the EXTENDED_YEAR, MONTH, DATE, fields if they are set, instead of
     * computing them.  If they are not set, this method defers to the
     * default implemenation.
     * @param millis milliseconds of the date fields
     * @param millisInDay milliseconds of the time fields; may be out
     * or range.
     * @stable ICU 2.0
     */
    protected int computeZoneOffset(long millis, int millisInDay) {
        int[] normalizedMillisInDay = new int[1];
        int days = floorDivide(millis + millisInDay, (int) ONE_DAY, normalizedMillisInDay);
        if (isLenient() || !isSet(MONTH) || !isSet(DAY_OF_MONTH) || millisInDay != normalizedMillisInDay[0]) {
            return super.computeZoneOffset(millis, millisInDay);
        }
        int julianDay = millisToJulianDay(days * ONE_DAY);
        int year = internalGet(EXTENDED_YEAR);
        int month = internalGet(MONTH);
        int previousMonthLength = (month == 0) ? 31 : handleGetMonthLength(year, month - 1);
        return getTimeZone().getOffset(year, month, internalGet(DATE), julianDayToDayOfWeek(julianDay), normalizedMillisInDay[0], handleGetMonthLength(year, month), previousMonthLength);
    }

    /**
     * @stable ICU 2.0
     */
    protected int handleComputeJulianDay(int bestField) {
        invertGregorian = false;
        int jd = super.handleComputeJulianDay(bestField);
        if (isGregorian != (jd >= cutoverJulianDay)) {
            invertGregorian = true;
            jd = super.handleComputeJulianDay(bestField);
        }
        return jd;
    }

    /**
     * Return JD of start of given month/year
     * @stable ICU 2.0
     */
    protected int handleComputeMonthStart(int eyear, int month, boolean useMonth) {
        if (month < 0 || month > 11) {
            int[] rem = new int[1];
            eyear += floorDivide(month, 12, rem);
            month = rem[0];
        }
        boolean isLeap = eyear % 4 == 0;
        int y = eyear - 1;
        int julianDay = 365 * y + floorDivide(y, 4) + (JAN_1_1_JULIAN_DAY - 3);
        isGregorian = (eyear >= gregorianCutoverYear);
        if (invertGregorian) {
            isGregorian = !isGregorian;
        }
        if (isGregorian) {
            isLeap = isLeap && ((eyear % 100 != 0) || (eyear % 400 == 0));
            julianDay += floorDivide(y, 400) - floorDivide(y, 100) + 2;
        }
        if (month != 0) {
            julianDay += MONTH_COUNT[month][isLeap ? 3 : 2];
        }
        return julianDay;
    }
}
