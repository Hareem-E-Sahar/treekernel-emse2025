package org.ibex.js;

import java.text.DateFormat;

/**
 * This class implements the Date native object.
 * See ECMA 15.9.
 * @author Mike McCabe
 * @author Adam Megacz (many modifications
 */
public class JSDate extends JS {

    public JSDate() {
        if (thisTimeZone == null) {
            thisTimeZone = java.util.TimeZone.getDefault();
            LocalTZA = thisTimeZone.getRawOffset();
        }
    }

    public String toString() {
        return date_format(date, FORMATSPEC_FULL);
    }

    public Object callMethod(Object method, Object a0, Object a1, Object a2, Object[] rest, int nargs) throws JSExn {
        switch(nargs) {
            case 0:
                switch(SW.get(method)) {
                    case SW.toString:
                        return date_format(date, FORMATSPEC_FULL);
                    case SW.toTimeString:
                        return date_format(date, FORMATSPEC_TIME);
                    case SW.toDateString:
                        return date_format(date, FORMATSPEC_DATE);
                    case SW.toLocaleString:
                        return toLocaleString(date);
                    case SW.toLocaleTimeString:
                        return toLocaleTimeString(date);
                    case SW.toLocaleDateString:
                        return toLocaleDateString(date);
                    case SW.toUTCString:
                        return toUTCString(date);
                    case SW.valueOf:
                        return N(this.date);
                    case SW.getTime:
                        return N(this.date);
                    case SW.getYear:
                        return N(getYear(date));
                    case SW.getFullYear:
                        return N(YearFromTime(LocalTime(date)));
                    case SW.getUTCFullYear:
                        return N(YearFromTime(date));
                    case SW.getMonth:
                        return N(MonthFromTime(LocalTime(date)));
                    case SW.getUTCMonth:
                        return N(MonthFromTime(date));
                    case SW.getDate:
                        return N(DateFromTime(LocalTime(date)));
                    case SW.getUTCDate:
                        return N(DateFromTime(date));
                    case SW.getDay:
                        return N(WeekDay(LocalTime(date)));
                    case SW.getUTCDay:
                        return N(WeekDay(date));
                    case SW.getHours:
                        return N(HourFromTime(LocalTime(date)));
                    case SW.getUTCHours:
                        return N(HourFromTime(date));
                    case SW.getMinutes:
                        return N(MinFromTime(LocalTime(date)));
                    case SW.getUTCMinutes:
                        return N(MinFromTime(date));
                    case SW.getSeconds:
                        return N(SecFromTime(LocalTime(date)));
                    case SW.getUTCSeconds:
                        return N(SecFromTime(date));
                    case SW.getMilliseconds:
                        return N(msFromTime(LocalTime(date)));
                    case SW.getUTCMilliseconds:
                        return N(msFromTime(date));
                    case SW.getTimezoneOffset:
                        return N(getTimezoneOffset(date));
                    default:
                        return super.callMethod(method, a0, a1, a2, rest, nargs);
                }
            case 1:
                switch(SW.get(method)) {
                    case SW.setTime:
                        return N(this.setTime(toDouble(a0)));
                    case SW.setYear:
                        return N(this.setYear(toDouble(a0)));
                }
            default:
                {
                    Object[] args = new Object[nargs];
                    for (int i = 0; i < nargs; i++) args[i] = i == 0 ? a0 : i == 1 ? a1 : i == 2 ? a2 : rest[i - 3];
                    switch(SW.get(method)) {
                        case SW.setMilliseconds:
                            return N(this.makeTime(args, 1, true));
                        case SW.setUTCMilliseconds:
                            return N(this.makeTime(args, 1, false));
                        case SW.setSeconds:
                            return N(this.makeTime(args, 2, true));
                        case SW.setUTCSeconds:
                            return N(this.makeTime(args, 2, false));
                        case SW.setMinutes:
                            return N(this.makeTime(args, 3, true));
                        case SW.setUTCMinutes:
                            return N(this.makeTime(args, 3, false));
                        case SW.setHours:
                            return N(this.makeTime(args, 4, true));
                        case SW.setUTCHours:
                            return N(this.makeTime(args, 4, false));
                        case SW.setDate:
                            return N(this.makeDate(args, 1, true));
                        case SW.setUTCDate:
                            return N(this.makeDate(args, 1, false));
                        case SW.setMonth:
                            return N(this.makeDate(args, 2, true));
                        case SW.setUTCMonth:
                            return N(this.makeDate(args, 2, false));
                        case SW.setFullYear:
                            return N(this.makeDate(args, 3, true));
                        case SW.setUTCFullYear:
                            return N(this.makeDate(args, 3, false));
                    }
                }
        }
        return super.callMethod(method, a0, a1, a2, rest, nargs);
    }

    public Object get(Object key) throws JSExn {
        switch(SW.get(key)) {
            case SW.toString:
                return METHOD;
            case SW.toTimeString:
                return METHOD;
            case SW.toDateString:
                return METHOD;
            case SW.toLocaleString:
                return METHOD;
            case SW.toLocaleTimeString:
                return METHOD;
            case SW.toLocaleDateString:
                return METHOD;
            case SW.toUTCString:
                return METHOD;
            case SW.valueOf:
                return METHOD;
            case SW.getTime:
                return METHOD;
            case SW.getYear:
                return METHOD;
            case SW.getFullYear:
                return METHOD;
            case SW.getUTCFullYear:
                return METHOD;
            case SW.getMonth:
                return METHOD;
            case SW.getUTCMonth:
                return METHOD;
            case SW.getDate:
                return METHOD;
            case SW.getUTCDate:
                return METHOD;
            case SW.getDay:
                return METHOD;
            case SW.getUTCDay:
                return METHOD;
            case SW.getHours:
                return METHOD;
            case SW.getUTCHours:
                return METHOD;
            case SW.getMinutes:
                return METHOD;
            case SW.getUTCMinutes:
                return METHOD;
            case SW.getSeconds:
                return METHOD;
            case SW.getUTCSeconds:
                return METHOD;
            case SW.getMilliseconds:
                return METHOD;
            case SW.getUTCMilliseconds:
                return METHOD;
            case SW.getTimezoneOffset:
                return METHOD;
            case SW.setTime:
                return METHOD;
            case SW.setYear:
                return METHOD;
            case SW.setMilliseconds:
                return METHOD;
            case SW.setUTCMilliseconds:
                return METHOD;
            case SW.setSeconds:
                return METHOD;
            case SW.setUTCSeconds:
                return METHOD;
            case SW.setMinutes:
                return METHOD;
            case SW.setUTCMinutes:
                return METHOD;
            case SW.setHours:
                return METHOD;
            case SW.setUTCHours:
                return METHOD;
            case SW.setDate:
                return METHOD;
            case SW.setUTCDate:
                return METHOD;
            case SW.setMonth:
                return METHOD;
            case SW.setUTCMonth:
                return METHOD;
            case SW.setFullYear:
                return METHOD;
            case SW.setUTCFullYear:
                return METHOD;
            default:
                return super.get(key);
        }
    }

    private static final double HalfTimeDomain = 8.64e15;

    private static final double HoursPerDay = 24.0;

    private static final double MinutesPerHour = 60.0;

    private static final double SecondsPerMinute = 60.0;

    private static final double msPerSecond = 1000.0;

    private static final double MinutesPerDay = (HoursPerDay * MinutesPerHour);

    private static final double SecondsPerDay = (MinutesPerDay * SecondsPerMinute);

    private static final double SecondsPerHour = (MinutesPerHour * SecondsPerMinute);

    private static final double msPerDay = (SecondsPerDay * msPerSecond);

    private static final double msPerHour = (SecondsPerHour * msPerSecond);

    private static final double msPerMinute = (SecondsPerMinute * msPerSecond);

    private static double Day(double t) {
        return java.lang.Math.floor(t / msPerDay);
    }

    private static double TimeWithinDay(double t) {
        double result;
        result = t % msPerDay;
        if (result < 0) result += msPerDay;
        return result;
    }

    private static int DaysInYear(int y) {
        if (y % 4 == 0 && (y % 100 != 0 || y % 400 == 0)) return 366; else return 365;
    }

    private static double DayFromYear(double y) {
        return ((365 * ((y) - 1970) + java.lang.Math.floor(((y) - 1969) / 4.0) - java.lang.Math.floor(((y) - 1901) / 100.0) + java.lang.Math.floor(((y) - 1601) / 400.0)));
    }

    private static double TimeFromYear(double y) {
        return DayFromYear(y) * msPerDay;
    }

    private static int YearFromTime(double t) {
        int lo = (int) java.lang.Math.floor((t / msPerDay) / 366) + 1970;
        int hi = (int) java.lang.Math.floor((t / msPerDay) / 365) + 1970;
        int mid;
        if (hi < lo) {
            int temp = lo;
            lo = hi;
            hi = temp;
        }
        while (hi > lo) {
            mid = (hi + lo) / 2;
            if (TimeFromYear(mid) > t) {
                hi = mid - 1;
            } else {
                if (TimeFromYear(mid) <= t) {
                    int temp = mid + 1;
                    if (TimeFromYear(temp) > t) {
                        return mid;
                    }
                    lo = mid + 1;
                }
            }
        }
        return lo;
    }

    private static boolean InLeapYear(double t) {
        return DaysInYear(YearFromTime(t)) == 366;
    }

    private static int DayWithinYear(double t) {
        int year = YearFromTime(t);
        return (int) (Day(t) - DayFromYear(year));
    }

    private static double DayFromMonth(int m, boolean leap) {
        int day = m * 30;
        if (m >= 7) {
            day += m / 2 - 1;
        } else if (m >= 2) {
            day += (m - 1) / 2 - 1;
        } else {
            day += m;
        }
        if (leap && m >= 2) {
            ++day;
        }
        return day;
    }

    private static int MonthFromTime(double t) {
        int d, step;
        d = DayWithinYear(t);
        if (d < (step = 31)) return 0;
        if (InLeapYear(t)) step += 29; else step += 28;
        if (d < step) return 1;
        if (d < (step += 31)) return 2;
        if (d < (step += 30)) return 3;
        if (d < (step += 31)) return 4;
        if (d < (step += 30)) return 5;
        if (d < (step += 31)) return 6;
        if (d < (step += 31)) return 7;
        if (d < (step += 30)) return 8;
        if (d < (step += 31)) return 9;
        if (d < (step += 30)) return 10;
        return 11;
    }

    private static int DateFromTime(double t) {
        int d, step, next;
        d = DayWithinYear(t);
        if (d <= (next = 30)) return d + 1;
        step = next;
        if (InLeapYear(t)) next += 29; else next += 28;
        if (d <= next) return d - step;
        step = next;
        if (d <= (next += 31)) return d - step;
        step = next;
        if (d <= (next += 30)) return d - step;
        step = next;
        if (d <= (next += 31)) return d - step;
        step = next;
        if (d <= (next += 30)) return d - step;
        step = next;
        if (d <= (next += 31)) return d - step;
        step = next;
        if (d <= (next += 31)) return d - step;
        step = next;
        if (d <= (next += 30)) return d - step;
        step = next;
        if (d <= (next += 31)) return d - step;
        step = next;
        if (d <= (next += 30)) return d - step;
        step = next;
        return d - step;
    }

    private static int WeekDay(double t) {
        double result;
        result = Day(t) + 4;
        result = result % 7;
        if (result < 0) result += 7;
        return (int) result;
    }

    private static double Now() {
        return (double) System.currentTimeMillis();
    }

    private static final boolean TZO_WORKAROUND = false;

    private static double DaylightSavingTA(double t) {
        if (!TZO_WORKAROUND) {
            java.util.Date date = new java.util.Date((long) t);
            if (thisTimeZone.inDaylightTime(date)) return msPerHour; else return 0;
        } else {
            t += LocalTZA + (HourFromTime(t) <= 2 ? msPerHour : 0);
            int year = YearFromTime(t);
            double offset = thisTimeZone.getOffset(year > 0 ? 1 : 0, year, MonthFromTime(t), DateFromTime(t), WeekDay(t), (int) TimeWithinDay(t));
            if ((offset - LocalTZA) != 0) return msPerHour; else return 0;
        }
    }

    private static double LocalTime(double t) {
        return t + LocalTZA + DaylightSavingTA(t);
    }

    public static double internalUTC(double t) {
        return t - LocalTZA - DaylightSavingTA(t - LocalTZA);
    }

    private static int HourFromTime(double t) {
        double result;
        result = java.lang.Math.floor(t / msPerHour) % HoursPerDay;
        if (result < 0) result += HoursPerDay;
        return (int) result;
    }

    private static int MinFromTime(double t) {
        double result;
        result = java.lang.Math.floor(t / msPerMinute) % MinutesPerHour;
        if (result < 0) result += MinutesPerHour;
        return (int) result;
    }

    private static int SecFromTime(double t) {
        double result;
        result = java.lang.Math.floor(t / msPerSecond) % SecondsPerMinute;
        if (result < 0) result += SecondsPerMinute;
        return (int) result;
    }

    private static int msFromTime(double t) {
        double result;
        result = t % msPerSecond;
        if (result < 0) result += msPerSecond;
        return (int) result;
    }

    private static double MakeTime(double hour, double min, double sec, double ms) {
        return ((hour * MinutesPerHour + min) * SecondsPerMinute + sec) * msPerSecond + ms;
    }

    private static double MakeDay(double year, double month, double date) {
        double result;
        boolean leap;
        double yearday;
        double monthday;
        year += java.lang.Math.floor(month / 12);
        month = month % 12;
        if (month < 0) month += 12;
        leap = (DaysInYear((int) year) == 366);
        yearday = java.lang.Math.floor(TimeFromYear(year) / msPerDay);
        monthday = DayFromMonth((int) month, leap);
        result = yearday + monthday + date - 1;
        return result;
    }

    private static double MakeDate(double day, double time) {
        return day * msPerDay + time;
    }

    private static double TimeClip(double d) {
        if (d != d || d == Double.POSITIVE_INFINITY || d == Double.NEGATIVE_INFINITY || java.lang.Math.abs(d) > HalfTimeDomain) {
            return Double.NaN;
        }
        if (d > 0.0) return java.lang.Math.floor(d + 0.); else return java.lang.Math.ceil(d + 0.);
    }

    public static double date_msecFromDate(double year, double mon, double mday, double hour, double min, double sec, double msec) {
        double day;
        double time;
        double result;
        day = MakeDay(year, mon, mday);
        time = MakeTime(hour, min, sec, msec);
        result = MakeDate(day, time);
        return result;
    }

    private static final int MAXARGS = 7;

    private static double jsStaticJSFunction_UTC(Object[] args) {
        double array[] = new double[MAXARGS];
        int loop;
        double d;
        for (loop = 0; loop < MAXARGS; loop++) {
            if (loop < args.length) {
                d = _toNumber(args[loop]);
                if (d != d || Double.isInfinite(d)) {
                    return Double.NaN;
                }
                array[loop] = toDouble(args[loop]);
            } else {
                array[loop] = 0;
            }
        }
        if (array[0] >= 0 && array[0] <= 99) array[0] += 1900;
        if (array[2] < 1) array[2] = 1;
        d = date_msecFromDate(array[0], array[1], array[2], array[3], array[4], array[5], array[6]);
        d = TimeClip(d);
        return d;
    }

    private static String wtb[] = { "am", "pm", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday", "january", "february", "march", "april", "may", "june", "july", "august", "september", "october", "november", "december", "gmt", "ut", "utc", "est", "edt", "cst", "cdt", "mst", "mdt", "pst", "pdt" };

    private static int ttb[] = { -1, -2, 0, 0, 0, 0, 0, 0, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 10000 + 0, 10000 + 0, 10000 + 0, 10000 + 5 * 60, 10000 + 4 * 60, 10000 + 6 * 60, 10000 + 5 * 60, 10000 + 7 * 60, 10000 + 6 * 60, 10000 + 8 * 60, 10000 + 7 * 60 };

    private static boolean date_regionMatches(String s1, int s1off, String s2, int s2off, int count) {
        boolean result = false;
        int s1len = s1.length();
        int s2len = s2.length();
        while (count > 0 && s1off < s1len && s2off < s2len) {
            if (Character.toLowerCase(s1.charAt(s1off)) != Character.toLowerCase(s2.charAt(s2off))) break;
            s1off++;
            s2off++;
            count--;
        }
        if (count == 0) {
            result = true;
        }
        return result;
    }

    private static double date_parseString(String s) {
        double msec;
        int year = -1;
        int mon = -1;
        int mday = -1;
        int hour = -1;
        int min = -1;
        int sec = -1;
        char c = 0;
        char si = 0;
        int i = 0;
        int n = -1;
        double tzoffset = -1;
        char prevc = 0;
        int limit = 0;
        boolean seenplusminus = false;
        if (s == null) return Double.NaN;
        limit = s.length();
        while (i < limit) {
            c = s.charAt(i);
            i++;
            if (c <= ' ' || c == ',' || c == '-') {
                if (i < limit) {
                    si = s.charAt(i);
                    if (c == '-' && '0' <= si && si <= '9') {
                        prevc = c;
                    }
                }
                continue;
            }
            if (c == '(') {
                int depth = 1;
                while (i < limit) {
                    c = s.charAt(i);
                    i++;
                    if (c == '(') depth++; else if (c == ')') if (--depth <= 0) break;
                }
                continue;
            }
            if ('0' <= c && c <= '9') {
                n = c - '0';
                while (i < limit && '0' <= (c = s.charAt(i)) && c <= '9') {
                    n = n * 10 + c - '0';
                    i++;
                }
                if ((prevc == '+' || prevc == '-')) {
                    seenplusminus = true;
                    if (n < 24) n = n * 60; else n = n % 100 + n / 100 * 60;
                    if (prevc == '+') n = -n;
                    if (tzoffset != 0 && tzoffset != -1) return Double.NaN;
                    tzoffset = n;
                } else if (n >= 70 || (prevc == '/' && mon >= 0 && mday >= 0 && year < 0)) {
                    if (year >= 0) return Double.NaN; else if (c <= ' ' || c == ',' || c == '/' || i >= limit) year = n < 100 ? n + 1900 : n; else return Double.NaN;
                } else if (c == ':') {
                    if (hour < 0) hour = n; else if (min < 0) min = n; else return Double.NaN;
                } else if (c == '/') {
                    if (mon < 0) mon = n - 1; else if (mday < 0) mday = n; else return Double.NaN;
                } else if (i < limit && c != ',' && c > ' ' && c != '-') {
                    return Double.NaN;
                } else if (seenplusminus && n < 60) {
                    if (tzoffset < 0) tzoffset -= n; else tzoffset += n;
                } else if (hour >= 0 && min < 0) {
                    min = n;
                } else if (min >= 0 && sec < 0) {
                    sec = n;
                } else if (mday < 0) {
                    mday = n;
                } else {
                    return Double.NaN;
                }
                prevc = 0;
            } else if (c == '/' || c == ':' || c == '+' || c == '-') {
                prevc = c;
            } else {
                int st = i - 1;
                int k;
                while (i < limit) {
                    c = s.charAt(i);
                    if (!(('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z'))) break;
                    i++;
                }
                if (i <= st + 1) return Double.NaN;
                for (k = wtb.length; --k >= 0; ) if (date_regionMatches(wtb[k], 0, s, st, i - st)) {
                    int action = ttb[k];
                    if (action != 0) {
                        if (action < 0) {
                            if (hour > 12 || hour < 0) {
                                return Double.NaN;
                            } else {
                                if (action == -1 && hour == 12) {
                                    hour = 0;
                                } else if (action == -2 && hour != 12) {
                                    hour += 12;
                                }
                            }
                        } else if (action <= 13) {
                            if (mon < 0) {
                                mon = (action - 2);
                            } else {
                                return Double.NaN;
                            }
                        } else {
                            tzoffset = action - 10000;
                        }
                    }
                    break;
                }
                if (k < 0) return Double.NaN;
                prevc = 0;
            }
        }
        if (year < 0 || mon < 0 || mday < 0) return Double.NaN;
        if (sec < 0) sec = 0;
        if (min < 0) min = 0;
        if (hour < 0) hour = 0;
        if (tzoffset == -1) {
            double time;
            time = date_msecFromDate(year, mon, mday, hour, min, sec, 0);
            return internalUTC(time);
        }
        msec = date_msecFromDate(year, mon, mday, hour, min, sec, 0);
        msec += tzoffset * msPerMinute;
        return msec;
    }

    private static double jsStaticJSFunction_parse(String s) {
        return date_parseString(s);
    }

    private static final int FORMATSPEC_FULL = 0;

    private static final int FORMATSPEC_DATE = 1;

    private static final int FORMATSPEC_TIME = 2;

    private static String date_format(double t, int format) {
        if (t != t) return NaN_date_str;
        StringBuffer result = new StringBuffer(60);
        double local = LocalTime(t);
        int minutes = (int) java.lang.Math.floor((LocalTZA + DaylightSavingTA(t)) / msPerMinute);
        int offset = (minutes / 60) * 100 + minutes % 60;
        String dateStr = Integer.toString(DateFromTime(local));
        String hourStr = Integer.toString(HourFromTime(local));
        String minStr = Integer.toString(MinFromTime(local));
        String secStr = Integer.toString(SecFromTime(local));
        String offsetStr = Integer.toString(offset > 0 ? offset : -offset);
        int year = YearFromTime(local);
        String yearStr = Integer.toString(year > 0 ? year : -year);
        if (format != FORMATSPEC_TIME) {
            result.append(days[WeekDay(local)]);
            result.append(' ');
            result.append(months[MonthFromTime(local)]);
            if (dateStr.length() == 1) result.append(" 0"); else result.append(' ');
            result.append(dateStr);
            result.append(' ');
        }
        if (format != FORMATSPEC_DATE) {
            if (hourStr.length() == 1) result.append('0');
            result.append(hourStr);
            if (minStr.length() == 1) result.append(":0"); else result.append(':');
            result.append(minStr);
            if (secStr.length() == 1) result.append(":0"); else result.append(':');
            result.append(secStr);
            if (offset > 0) result.append(" GMT+"); else result.append(" GMT-");
            for (int i = offsetStr.length(); i < 4; i++) result.append('0');
            result.append(offsetStr);
            if (timeZoneFormatter == null) timeZoneFormatter = new java.text.SimpleDateFormat("zzz");
            if (timeZoneFormatter != null) {
                result.append(" (");
                java.util.Date date = new java.util.Date((long) t);
                result.append(timeZoneFormatter.format(date));
                result.append(')');
            }
            if (format != FORMATSPEC_TIME) result.append(' ');
        }
        if (format != FORMATSPEC_TIME) {
            if (year < 0) result.append('-');
            for (int i = yearStr.length(); i < 4; i++) result.append('0');
            result.append(yearStr);
        }
        return result.toString();
    }

    private static double _toNumber(Object o) {
        return JS.toDouble(o);
    }

    private static double _toNumber(Object[] o, int index) {
        return JS.toDouble(o[index]);
    }

    private static double toDouble(double d) {
        return d;
    }

    public JSDate(Object a0, Object a1, Object a2, Object[] rest, int nargs) {
        this();
        switch(nargs) {
            case 0:
                {
                    this.date = Now();
                    return;
                }
            case 1:
                {
                    double date;
                    if (a0 instanceof JS) a0 = ((JS) a0).toString();
                    if (!(a0 instanceof String)) {
                        date = _toNumber(a0);
                    } else {
                        String str = (String) a0;
                        date = date_parseString(str);
                    }
                    this.date = TimeClip(date);
                    return;
                }
            default:
                {
                    double array[] = new double[MAXARGS];
                    array[0] = toDouble(a0);
                    array[1] = toDouble(a1);
                    if (nargs >= 2) array[2] = toDouble(a2);
                    for (int i = 0; i < nargs; i++) {
                        double d = _toNumber(i == 0 ? a0 : i == 1 ? a1 : i == 2 ? a2 : rest[i - 3]);
                        if (d != d || Double.isInfinite(d)) {
                            this.date = Double.NaN;
                            return;
                        }
                        array[i] = d;
                    }
                    if (array[0] >= 0 && array[0] <= 99) array[0] += 1900;
                    if (array[2] < 1) array[2] = 1;
                    double day = MakeDay(array[0], array[1], array[2]);
                    double time = MakeTime(array[3], array[4], array[5], array[6]);
                    time = MakeDate(day, time);
                    time = internalUTC(time);
                    this.date = TimeClip(time);
                    return;
                }
        }
    }

    private static String NaN_date_str = "Invalid Date";

    private static String[] days = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };

    private static String[] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

    private static String toLocale_helper(double t, java.text.DateFormat formatter) {
        if (t != t) return NaN_date_str;
        java.util.Date tempdate = new java.util.Date((long) t);
        return formatter.format(tempdate);
    }

    private static String toLocaleString(double date) {
        if (localeDateTimeFormatter == null) localeDateTimeFormatter = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
        return toLocale_helper(date, localeDateTimeFormatter);
    }

    private static String toLocaleTimeString(double date) {
        if (localeTimeFormatter == null) localeTimeFormatter = DateFormat.getTimeInstance(DateFormat.LONG);
        return toLocale_helper(date, localeTimeFormatter);
    }

    private static String toLocaleDateString(double date) {
        if (localeDateFormatter == null) localeDateFormatter = DateFormat.getDateInstance(DateFormat.LONG);
        return toLocale_helper(date, localeDateFormatter);
    }

    private static String toUTCString(double date) {
        StringBuffer result = new StringBuffer(60);
        String dateStr = Integer.toString(DateFromTime(date));
        String hourStr = Integer.toString(HourFromTime(date));
        String minStr = Integer.toString(MinFromTime(date));
        String secStr = Integer.toString(SecFromTime(date));
        int year = YearFromTime(date);
        String yearStr = Integer.toString(year > 0 ? year : -year);
        result.append(days[WeekDay(date)]);
        result.append(", ");
        if (dateStr.length() == 1) result.append('0');
        result.append(dateStr);
        result.append(' ');
        result.append(months[MonthFromTime(date)]);
        if (year < 0) result.append(" -"); else result.append(' ');
        int i;
        for (i = yearStr.length(); i < 4; i++) result.append('0');
        result.append(yearStr);
        if (hourStr.length() == 1) result.append(" 0"); else result.append(' ');
        result.append(hourStr);
        if (minStr.length() == 1) result.append(":0"); else result.append(':');
        result.append(minStr);
        if (secStr.length() == 1) result.append(":0"); else result.append(':');
        result.append(secStr);
        result.append(" GMT");
        return result.toString();
    }

    private static double getYear(double date) {
        int result = YearFromTime(LocalTime(date));
        result -= 1900;
        return result;
    }

    private static double getTimezoneOffset(double date) {
        return (date - LocalTime(date)) / msPerMinute;
    }

    public double setTime(double time) {
        this.date = TimeClip(time);
        return this.date;
    }

    private double makeTime(Object[] args, int maxargs, boolean local) {
        int i;
        double conv[] = new double[4];
        double hour, min, sec, msec;
        double lorutime;
        double time;
        double result;
        double date = this.date;
        if (date != date) return date;
        if (args.length == 0) args = new Object[] { null };
        for (i = 0; i < args.length && i < maxargs; i++) {
            conv[i] = _toNumber(args[i]);
            if (conv[i] != conv[i] || Double.isInfinite(conv[i])) {
                this.date = Double.NaN;
                return this.date;
            }
            conv[i] = toDouble(conv[i]);
        }
        if (local) lorutime = LocalTime(date); else lorutime = date;
        i = 0;
        int stop = args.length;
        if (maxargs >= 4 && i < stop) hour = conv[i++]; else hour = HourFromTime(lorutime);
        if (maxargs >= 3 && i < stop) min = conv[i++]; else min = MinFromTime(lorutime);
        if (maxargs >= 2 && i < stop) sec = conv[i++]; else sec = SecFromTime(lorutime);
        if (maxargs >= 1 && i < stop) msec = conv[i++]; else msec = msFromTime(lorutime);
        time = MakeTime(hour, min, sec, msec);
        result = MakeDate(Day(lorutime), time);
        if (local) result = internalUTC(result);
        date = TimeClip(result);
        this.date = date;
        return date;
    }

    private double setHours(Object[] args) {
        return makeTime(args, 4, true);
    }

    private double setUTCHours(Object[] args) {
        return makeTime(args, 4, false);
    }

    private double makeDate(Object[] args, int maxargs, boolean local) {
        int i;
        double conv[] = new double[3];
        double year, month, day;
        double lorutime;
        double result;
        double date = this.date;
        if (args.length == 0) args = new Object[] { null };
        for (i = 0; i < args.length && i < maxargs; i++) {
            conv[i] = _toNumber(args[i]);
            if (conv[i] != conv[i] || Double.isInfinite(conv[i])) {
                this.date = Double.NaN;
                return this.date;
            }
            conv[i] = toDouble(conv[i]);
        }
        if (date != date) {
            if (args.length < 3) {
                return Double.NaN;
            } else {
                lorutime = 0;
            }
        } else {
            if (local) lorutime = LocalTime(date); else lorutime = date;
        }
        i = 0;
        int stop = args.length;
        if (maxargs >= 3 && i < stop) year = conv[i++]; else year = YearFromTime(lorutime);
        if (maxargs >= 2 && i < stop) month = conv[i++]; else month = MonthFromTime(lorutime);
        if (maxargs >= 1 && i < stop) day = conv[i++]; else day = DateFromTime(lorutime);
        day = MakeDay(year, month, day);
        result = MakeDate(day, TimeWithinDay(lorutime));
        if (local) result = internalUTC(result);
        date = TimeClip(result);
        this.date = date;
        return date;
    }

    private double setYear(double year) {
        double day, result;
        if (year != year || Double.isInfinite(year)) {
            this.date = Double.NaN;
            return this.date;
        }
        if (this.date != this.date) {
            this.date = 0;
        } else {
            this.date = LocalTime(this.date);
        }
        if (year >= 0 && year <= 99) year += 1900;
        day = MakeDay(year, MonthFromTime(this.date), DateFromTime(this.date));
        result = MakeDate(day, TimeWithinDay(this.date));
        result = internalUTC(result);
        this.date = TimeClip(result);
        return this.date;
    }

    private static java.util.TimeZone thisTimeZone;

    private static double LocalTZA;

    private static java.text.DateFormat timeZoneFormatter;

    private static java.text.DateFormat localeDateTimeFormatter;

    private static java.text.DateFormat localeDateFormatter;

    private static java.text.DateFormat localeTimeFormatter;

    private double date;

    public long getRawTime() {
        return (long) this.date;
    }
}
