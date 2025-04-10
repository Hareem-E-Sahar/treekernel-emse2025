package org.mozilla.javascript;

import java.util.Date;

/**
 * This class implements the Date native object.
 * See ECMA 15.9.
 * @author Mike McCabe
 */
final class NativeDate extends IdScriptableObject {

    static final long serialVersionUID = -8307438915861678966L;

    private static final Object DATE_TAG = new Object();

    private static final String js_NaN_date_str = "Invalid Date";

    static void init(Scriptable scope, boolean sealed) {
        NativeDate obj = new NativeDate();
        obj.date = ScriptRuntime.NaN;
        obj.exportAsJSClass(MAX_PROTOTYPE_ID, scope, sealed);
    }

    private NativeDate() {
        if (thisTimeZone == null) {
            thisTimeZone = java.util.TimeZone.getDefault();
            LocalTZA = thisTimeZone.getRawOffset();
        }
    }

    public String getClassName() {
        return "Date";
    }

    public Object getDefaultValue(Class typeHint) {
        if (typeHint == null) typeHint = ScriptRuntime.StringClass;
        return super.getDefaultValue(typeHint);
    }

    double getJSTimeValue() {
        return date;
    }

    protected void fillConstructorProperties(IdFunctionObject ctor) {
        addIdFunctionProperty(ctor, DATE_TAG, ConstructorId_now, "now", 0);
        addIdFunctionProperty(ctor, DATE_TAG, ConstructorId_parse, "parse", 1);
        addIdFunctionProperty(ctor, DATE_TAG, ConstructorId_UTC, "UTC", 1);
        super.fillConstructorProperties(ctor);
    }

    protected void initPrototypeId(int id) {
        String s;
        int arity;
        switch(id) {
            case Id_constructor:
                arity = 1;
                s = "constructor";
                break;
            case Id_toString:
                arity = 0;
                s = "toString";
                break;
            case Id_toTimeString:
                arity = 0;
                s = "toTimeString";
                break;
            case Id_toDateString:
                arity = 0;
                s = "toDateString";
                break;
            case Id_toLocaleString:
                arity = 0;
                s = "toLocaleString";
                break;
            case Id_toLocaleTimeString:
                arity = 0;
                s = "toLocaleTimeString";
                break;
            case Id_toLocaleDateString:
                arity = 0;
                s = "toLocaleDateString";
                break;
            case Id_toUTCString:
                arity = 0;
                s = "toUTCString";
                break;
            case Id_toSource:
                arity = 0;
                s = "toSource";
                break;
            case Id_valueOf:
                arity = 0;
                s = "valueOf";
                break;
            case Id_getTime:
                arity = 0;
                s = "getTime";
                break;
            case Id_getYear:
                arity = 0;
                s = "getYear";
                break;
            case Id_getFullYear:
                arity = 0;
                s = "getFullYear";
                break;
            case Id_getUTCFullYear:
                arity = 0;
                s = "getUTCFullYear";
                break;
            case Id_getMonth:
                arity = 0;
                s = "getMonth";
                break;
            case Id_getUTCMonth:
                arity = 0;
                s = "getUTCMonth";
                break;
            case Id_getDate:
                arity = 0;
                s = "getDate";
                break;
            case Id_getUTCDate:
                arity = 0;
                s = "getUTCDate";
                break;
            case Id_getDay:
                arity = 0;
                s = "getDay";
                break;
            case Id_getUTCDay:
                arity = 0;
                s = "getUTCDay";
                break;
            case Id_getHours:
                arity = 0;
                s = "getHours";
                break;
            case Id_getUTCHours:
                arity = 0;
                s = "getUTCHours";
                break;
            case Id_getMinutes:
                arity = 0;
                s = "getMinutes";
                break;
            case Id_getUTCMinutes:
                arity = 0;
                s = "getUTCMinutes";
                break;
            case Id_getSeconds:
                arity = 0;
                s = "getSeconds";
                break;
            case Id_getUTCSeconds:
                arity = 0;
                s = "getUTCSeconds";
                break;
            case Id_getMilliseconds:
                arity = 0;
                s = "getMilliseconds";
                break;
            case Id_getUTCMilliseconds:
                arity = 0;
                s = "getUTCMilliseconds";
                break;
            case Id_getTimezoneOffset:
                arity = 0;
                s = "getTimezoneOffset";
                break;
            case Id_setTime:
                arity = 1;
                s = "setTime";
                break;
            case Id_setMilliseconds:
                arity = 1;
                s = "setMilliseconds";
                break;
            case Id_setUTCMilliseconds:
                arity = 1;
                s = "setUTCMilliseconds";
                break;
            case Id_setSeconds:
                arity = 2;
                s = "setSeconds";
                break;
            case Id_setUTCSeconds:
                arity = 2;
                s = "setUTCSeconds";
                break;
            case Id_setMinutes:
                arity = 3;
                s = "setMinutes";
                break;
            case Id_setUTCMinutes:
                arity = 3;
                s = "setUTCMinutes";
                break;
            case Id_setHours:
                arity = 4;
                s = "setHours";
                break;
            case Id_setUTCHours:
                arity = 4;
                s = "setUTCHours";
                break;
            case Id_setDate:
                arity = 1;
                s = "setDate";
                break;
            case Id_setUTCDate:
                arity = 1;
                s = "setUTCDate";
                break;
            case Id_setMonth:
                arity = 2;
                s = "setMonth";
                break;
            case Id_setUTCMonth:
                arity = 2;
                s = "setUTCMonth";
                break;
            case Id_setFullYear:
                arity = 3;
                s = "setFullYear";
                break;
            case Id_setUTCFullYear:
                arity = 3;
                s = "setUTCFullYear";
                break;
            case Id_setYear:
                arity = 1;
                s = "setYear";
                break;
            default:
                throw new IllegalArgumentException(String.valueOf(id));
        }
        initPrototypeMethod(DATE_TAG, id, s, arity);
    }

    public Object execIdCall(IdFunctionObject f, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        if (!f.hasTag(DATE_TAG)) {
            return super.execIdCall(f, cx, scope, thisObj, args);
        }
        int id = f.methodId();
        switch(id) {
            case ConstructorId_now:
                return ScriptRuntime.wrapNumber(now());
            case ConstructorId_parse:
                {
                    String dataStr = ScriptRuntime.toString(args, 0);
                    return ScriptRuntime.wrapNumber(date_parseString(dataStr));
                }
            case ConstructorId_UTC:
                return ScriptRuntime.wrapNumber(jsStaticFunction_UTC(args));
            case Id_constructor:
                {
                    if (thisObj != null) return date_format(now(), Id_toString);
                    return jsConstructor(args);
                }
        }
        if (!(thisObj instanceof NativeDate)) throw incompatibleCallError(f);
        NativeDate realThis = (NativeDate) thisObj;
        double t = realThis.date;
        switch(id) {
            case Id_toString:
            case Id_toTimeString:
            case Id_toDateString:
                if (t == t) {
                    return date_format(t, id);
                }
                return js_NaN_date_str;
            case Id_toLocaleString:
            case Id_toLocaleTimeString:
            case Id_toLocaleDateString:
                if (t == t) {
                    return toLocale_helper(t, id);
                }
                return js_NaN_date_str;
            case Id_toUTCString:
                if (t == t) {
                    return js_toUTCString(t);
                }
                return js_NaN_date_str;
            case Id_toSource:
                return "(new Date(" + ScriptRuntime.toString(t) + "))";
            case Id_valueOf:
            case Id_getTime:
                return ScriptRuntime.wrapNumber(t);
            case Id_getYear:
            case Id_getFullYear:
            case Id_getUTCFullYear:
                if (t == t) {
                    if (id != Id_getUTCFullYear) t = LocalTime(t);
                    t = YearFromTime(t);
                    if (id == Id_getYear) {
                        if (cx.hasFeature(Context.FEATURE_NON_ECMA_GET_YEAR)) {
                            if (1900 <= t && t < 2000) {
                                t -= 1900;
                            }
                        } else {
                            t -= 1900;
                        }
                    }
                }
                return ScriptRuntime.wrapNumber(t);
            case Id_getMonth:
            case Id_getUTCMonth:
                if (t == t) {
                    if (id == Id_getMonth) t = LocalTime(t);
                    t = MonthFromTime(t);
                }
                return ScriptRuntime.wrapNumber(t);
            case Id_getDate:
            case Id_getUTCDate:
                if (t == t) {
                    if (id == Id_getDate) t = LocalTime(t);
                    t = DateFromTime(t);
                }
                return ScriptRuntime.wrapNumber(t);
            case Id_getDay:
            case Id_getUTCDay:
                if (t == t) {
                    if (id == Id_getDay) t = LocalTime(t);
                    t = WeekDay(t);
                }
                return ScriptRuntime.wrapNumber(t);
            case Id_getHours:
            case Id_getUTCHours:
                if (t == t) {
                    if (id == Id_getHours) t = LocalTime(t);
                    t = HourFromTime(t);
                }
                return ScriptRuntime.wrapNumber(t);
            case Id_getMinutes:
            case Id_getUTCMinutes:
                if (t == t) {
                    if (id == Id_getMinutes) t = LocalTime(t);
                    t = MinFromTime(t);
                }
                return ScriptRuntime.wrapNumber(t);
            case Id_getSeconds:
            case Id_getUTCSeconds:
                if (t == t) {
                    if (id == Id_getSeconds) t = LocalTime(t);
                    t = SecFromTime(t);
                }
                return ScriptRuntime.wrapNumber(t);
            case Id_getMilliseconds:
            case Id_getUTCMilliseconds:
                if (t == t) {
                    if (id == Id_getMilliseconds) t = LocalTime(t);
                    t = msFromTime(t);
                }
                return ScriptRuntime.wrapNumber(t);
            case Id_getTimezoneOffset:
                if (t == t) {
                    t = (t - LocalTime(t)) / msPerMinute;
                }
                return ScriptRuntime.wrapNumber(t);
            case Id_setTime:
                t = TimeClip(ScriptRuntime.toNumber(args, 0));
                realThis.date = t;
                return ScriptRuntime.wrapNumber(t);
            case Id_setMilliseconds:
            case Id_setUTCMilliseconds:
            case Id_setSeconds:
            case Id_setUTCSeconds:
            case Id_setMinutes:
            case Id_setUTCMinutes:
            case Id_setHours:
            case Id_setUTCHours:
                t = makeTime(t, args, id);
                realThis.date = t;
                return ScriptRuntime.wrapNumber(t);
            case Id_setDate:
            case Id_setUTCDate:
            case Id_setMonth:
            case Id_setUTCMonth:
            case Id_setFullYear:
            case Id_setUTCFullYear:
                t = makeDate(t, args, id);
                realThis.date = t;
                return ScriptRuntime.wrapNumber(t);
            case Id_setYear:
                {
                    double year = ScriptRuntime.toNumber(args, 0);
                    if (year != year || Double.isInfinite(year)) {
                        t = ScriptRuntime.NaN;
                    } else {
                        if (t != t) {
                            t = 0;
                        } else {
                            t = LocalTime(t);
                        }
                        if (year >= 0 && year <= 99) year += 1900;
                        double day = MakeDay(year, MonthFromTime(t), DateFromTime(t));
                        t = MakeDate(day, TimeWithinDay(t));
                        t = internalUTC(t);
                        t = TimeClip(t);
                    }
                }
                realThis.date = t;
                return ScriptRuntime.wrapNumber(t);
            default:
                throw new IllegalArgumentException(String.valueOf(id));
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
        return Math.floor(t / msPerDay);
    }

    private static double TimeWithinDay(double t) {
        double result;
        result = t % msPerDay;
        if (result < 0) result += msPerDay;
        return result;
    }

    private static boolean IsLeapYear(int year) {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
    }

    private static double DayFromYear(double y) {
        return ((365 * ((y) - 1970) + Math.floor(((y) - 1969) / 4.0) - Math.floor(((y) - 1901) / 100.0) + Math.floor(((y) - 1601) / 400.0)));
    }

    private static double TimeFromYear(double y) {
        return DayFromYear(y) * msPerDay;
    }

    private static int YearFromTime(double t) {
        int lo = (int) Math.floor((t / msPerDay) / 366) + 1970;
        int hi = (int) Math.floor((t / msPerDay) / 365) + 1970;
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
                lo = mid + 1;
                if (TimeFromYear(lo) > t) {
                    return mid;
                }
            }
        }
        return lo;
    }

    private static double DayFromMonth(int m, int year) {
        int day = m * 30;
        if (m >= 7) {
            day += m / 2 - 1;
        } else if (m >= 2) {
            day += (m - 1) / 2 - 1;
        } else {
            day += m;
        }
        if (m >= 2 && IsLeapYear(year)) {
            ++day;
        }
        return day;
    }

    private static int MonthFromTime(double t) {
        int year = YearFromTime(t);
        int d = (int) (Day(t) - DayFromYear(year));
        d -= 31 + 28;
        if (d < 0) {
            return (d < -28) ? 0 : 1;
        }
        if (IsLeapYear(year)) {
            if (d == 0) return 1;
            --d;
        }
        int estimate = d / 30;
        int mstart;
        switch(estimate) {
            case 0:
                return 2;
            case 1:
                mstart = 31;
                break;
            case 2:
                mstart = 31 + 30;
                break;
            case 3:
                mstart = 31 + 30 + 31;
                break;
            case 4:
                mstart = 31 + 30 + 31 + 30;
                break;
            case 5:
                mstart = 31 + 30 + 31 + 30 + 31;
                break;
            case 6:
                mstart = 31 + 30 + 31 + 30 + 31 + 31;
                break;
            case 7:
                mstart = 31 + 30 + 31 + 30 + 31 + 31 + 30;
                break;
            case 8:
                mstart = 31 + 30 + 31 + 30 + 31 + 31 + 30 + 31;
                break;
            case 9:
                mstart = 31 + 30 + 31 + 30 + 31 + 31 + 30 + 31 + 30;
                break;
            case 10:
                return 11;
            default:
                throw Kit.codeBug();
        }
        return (d >= mstart) ? estimate + 2 : estimate + 1;
    }

    private static int DateFromTime(double t) {
        int year = YearFromTime(t);
        int d = (int) (Day(t) - DayFromYear(year));
        d -= 31 + 28;
        if (d < 0) {
            return (d < -28) ? d + 31 + 28 + 1 : d + 28 + 1;
        }
        if (IsLeapYear(year)) {
            if (d == 0) return 29;
            --d;
        }
        int mdays, mstart;
        switch(d / 30) {
            case 0:
                return d + 1;
            case 1:
                mdays = 31;
                mstart = 31;
                break;
            case 2:
                mdays = 30;
                mstart = 31 + 30;
                break;
            case 3:
                mdays = 31;
                mstart = 31 + 30 + 31;
                break;
            case 4:
                mdays = 30;
                mstart = 31 + 30 + 31 + 30;
                break;
            case 5:
                mdays = 31;
                mstart = 31 + 30 + 31 + 30 + 31;
                break;
            case 6:
                mdays = 31;
                mstart = 31 + 30 + 31 + 30 + 31 + 31;
                break;
            case 7:
                mdays = 30;
                mstart = 31 + 30 + 31 + 30 + 31 + 31 + 30;
                break;
            case 8:
                mdays = 31;
                mstart = 31 + 30 + 31 + 30 + 31 + 31 + 30 + 31;
                break;
            case 9:
                mdays = 30;
                mstart = 31 + 30 + 31 + 30 + 31 + 31 + 30 + 31 + 30;
                break;
            case 10:
                return d - (31 + 30 + 31 + 30 + 31 + 31 + 30 + 31 + 30) + 1;
            default:
                throw Kit.codeBug();
        }
        d -= mstart;
        if (d < 0) {
            d += mdays;
        }
        return d + 1;
    }

    private static int WeekDay(double t) {
        double result;
        result = Day(t) + 4;
        result = result % 7;
        if (result < 0) result += 7;
        return (int) result;
    }

    private static double now() {
        return System.currentTimeMillis();
    }

    private static final boolean TZO_WORKAROUND = false;

    private static double DaylightSavingTA(double t) {
        if (t < 0.0 || t > 2145916800000.0) {
            int year = EquivalentYear(YearFromTime(t));
            double day = MakeDay(year, MonthFromTime(t), DateFromTime(t));
            t = MakeDate(day, TimeWithinDay(t));
        }
        if (!TZO_WORKAROUND) {
            if (thisTimeZone.useDaylightTime()) return msPerHour; else return 0;
        } else {
            t += LocalTZA + (HourFromTime(t) <= 2 ? msPerHour : 0);
            int year = YearFromTime(t);
            double offset = thisTimeZone.getOffset(year > 0 ? 1 : 0, year, MonthFromTime(t), DateFromTime(t), WeekDay(t), (int) TimeWithinDay(t));
            if ((offset - LocalTZA) != 0) return msPerHour; else return 0;
        }
    }

    private static int EquivalentYear(int year) {
        int day = (int) DayFromYear(year) + 4;
        day = day % 7;
        if (day < 0) day += 7;
        if (IsLeapYear(year)) {
            switch(day) {
                case 0:
                    return 1984;
                case 1:
                    return 1996;
                case 2:
                    return 1980;
                case 3:
                    return 1992;
                case 4:
                    return 1976;
                case 5:
                    return 1988;
                case 6:
                    return 1972;
            }
        } else {
            switch(day) {
                case 0:
                    return 1978;
                case 1:
                    return 1973;
                case 2:
                    return 1974;
                case 3:
                    return 1975;
                case 4:
                    return 1981;
                case 5:
                    return 1971;
                case 6:
                    return 1977;
            }
        }
        throw Kit.codeBug();
    }

    private static double LocalTime(double t) {
        return t + LocalTZA + DaylightSavingTA(t);
    }

    private static double internalUTC(double t) {
        return t - LocalTZA - DaylightSavingTA(t - LocalTZA);
    }

    private static int HourFromTime(double t) {
        double result;
        result = Math.floor(t / msPerHour) % HoursPerDay;
        if (result < 0) result += HoursPerDay;
        return (int) result;
    }

    private static int MinFromTime(double t) {
        double result;
        result = Math.floor(t / msPerMinute) % MinutesPerHour;
        if (result < 0) result += MinutesPerHour;
        return (int) result;
    }

    private static int SecFromTime(double t) {
        double result;
        result = Math.floor(t / msPerSecond) % SecondsPerMinute;
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
        year += Math.floor(month / 12);
        month = month % 12;
        if (month < 0) month += 12;
        double yearday = Math.floor(TimeFromYear(year) / msPerDay);
        double monthday = DayFromMonth((int) month, (int) year);
        return yearday + monthday + date - 1;
    }

    private static double MakeDate(double day, double time) {
        return day * msPerDay + time;
    }

    private static double TimeClip(double d) {
        if (d != d || d == Double.POSITIVE_INFINITY || d == Double.NEGATIVE_INFINITY || Math.abs(d) > HalfTimeDomain) {
            return ScriptRuntime.NaN;
        }
        if (d > 0.0) return Math.floor(d + 0.); else return Math.ceil(d + 0.);
    }

    private static double date_msecFromDate(double year, double mon, double mday, double hour, double min, double sec, double msec) {
        double day;
        double time;
        double result;
        day = MakeDay(year, mon, mday);
        time = MakeTime(hour, min, sec, msec);
        result = MakeDate(day, time);
        return result;
    }

    private static final int MAXARGS = 7;

    private static double date_msecFromArgs(Object[] args) {
        double array[] = new double[MAXARGS];
        int loop;
        double d;
        for (loop = 0; loop < MAXARGS; loop++) {
            if (loop < args.length) {
                d = ScriptRuntime.toNumber(args[loop]);
                if (d != d || Double.isInfinite(d)) {
                    return ScriptRuntime.NaN;
                }
                array[loop] = ScriptRuntime.toInteger(args[loop]);
            } else {
                if (loop == 2) {
                    array[loop] = 1;
                } else {
                    array[loop] = 0;
                }
            }
        }
        if (array[0] >= 0 && array[0] <= 99) array[0] += 1900;
        return date_msecFromDate(array[0], array[1], array[2], array[3], array[4], array[5], array[6]);
    }

    private static double jsStaticFunction_UTC(Object[] args) {
        return TimeClip(date_msecFromArgs(args));
    }

    private static double date_parseString(String s) {
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
                    if (tzoffset != 0 && tzoffset != -1) return ScriptRuntime.NaN;
                    tzoffset = n;
                } else if (n >= 70 || (prevc == '/' && mon >= 0 && mday >= 0 && year < 0)) {
                    if (year >= 0) return ScriptRuntime.NaN; else if (c <= ' ' || c == ',' || c == '/' || i >= limit) year = n < 100 ? n + 1900 : n; else return ScriptRuntime.NaN;
                } else if (c == ':') {
                    if (hour < 0) hour = n; else if (min < 0) min = n; else return ScriptRuntime.NaN;
                } else if (c == '/') {
                    if (mon < 0) mon = n - 1; else if (mday < 0) mday = n; else return ScriptRuntime.NaN;
                } else if (i < limit && c != ',' && c > ' ' && c != '-') {
                    return ScriptRuntime.NaN;
                } else if (seenplusminus && n < 60) {
                    if (tzoffset < 0) tzoffset -= n; else tzoffset += n;
                } else if (hour >= 0 && min < 0) {
                    min = n;
                } else if (min >= 0 && sec < 0) {
                    sec = n;
                } else if (mday < 0) {
                    mday = n;
                } else {
                    return ScriptRuntime.NaN;
                }
                prevc = 0;
            } else if (c == '/' || c == ':' || c == '+' || c == '-') {
                prevc = c;
            } else {
                int st = i - 1;
                while (i < limit) {
                    c = s.charAt(i);
                    if (!(('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z'))) break;
                    i++;
                }
                int letterCount = i - st;
                if (letterCount < 2) return ScriptRuntime.NaN;
                String wtb = "am;pm;" + "monday;tuesday;wednesday;thursday;friday;" + "saturday;sunday;" + "january;february;march;april;may;june;" + "july;august;september;october;november;december;" + "gmt;ut;utc;est;edt;cst;cdt;mst;mdt;pst;pdt;";
                int index = 0;
                for (int wtbOffset = 0; ; ) {
                    int wtbNext = wtb.indexOf(';', wtbOffset);
                    if (wtbNext < 0) return ScriptRuntime.NaN;
                    if (wtb.regionMatches(true, wtbOffset, s, st, letterCount)) break;
                    wtbOffset = wtbNext + 1;
                    ++index;
                }
                if (index < 2) {
                    if (hour > 12 || hour < 0) {
                        return ScriptRuntime.NaN;
                    } else if (index == 0) {
                        if (hour == 12) hour = 0;
                    } else {
                        if (hour != 12) hour += 12;
                    }
                } else if ((index -= 2) < 7) {
                } else if ((index -= 7) < 12) {
                    if (mon < 0) {
                        mon = index;
                    } else {
                        return ScriptRuntime.NaN;
                    }
                } else {
                    index -= 12;
                    switch(index) {
                        case 0:
                            tzoffset = 0;
                            break;
                        case 1:
                            tzoffset = 0;
                            break;
                        case 2:
                            tzoffset = 0;
                            break;
                        case 3:
                            tzoffset = 5 * 60;
                            break;
                        case 4:
                            tzoffset = 4 * 60;
                            break;
                        case 5:
                            tzoffset = 6 * 60;
                            break;
                        case 6:
                            tzoffset = 5 * 60;
                            break;
                        case 7:
                            tzoffset = 7 * 60;
                            break;
                        case 8:
                            tzoffset = 6 * 60;
                            break;
                        case 9:
                            tzoffset = 8 * 60;
                            break;
                        case 10:
                            tzoffset = 7 * 60;
                            break;
                        default:
                            Kit.codeBug();
                    }
                }
            }
        }
        if (year < 0 || mon < 0 || mday < 0) return ScriptRuntime.NaN;
        if (sec < 0) sec = 0;
        if (min < 0) min = 0;
        if (hour < 0) hour = 0;
        double msec = date_msecFromDate(year, mon, mday, hour, min, sec, 0);
        if (tzoffset == -1) {
            return internalUTC(msec);
        } else {
            return msec + tzoffset * msPerMinute;
        }
    }

    private static String date_format(double t, int methodId) {
        StringBuffer result = new StringBuffer(60);
        double local = LocalTime(t);
        if (methodId != Id_toTimeString) {
            appendWeekDayName(result, WeekDay(local));
            result.append(' ');
            appendMonthName(result, MonthFromTime(local));
            result.append(' ');
            append0PaddedUint(result, DateFromTime(local), 2);
            result.append(' ');
            int year = YearFromTime(local);
            if (year < 0) {
                result.append('-');
                year = -year;
            }
            append0PaddedUint(result, year, 4);
            if (methodId != Id_toDateString) result.append(' ');
        }
        if (methodId != Id_toDateString) {
            append0PaddedUint(result, HourFromTime(local), 2);
            result.append(':');
            append0PaddedUint(result, MinFromTime(local), 2);
            result.append(':');
            append0PaddedUint(result, SecFromTime(local), 2);
            int minutes = (int) Math.floor((LocalTZA + DaylightSavingTA(t)) / msPerMinute);
            int offset = (minutes / 60) * 100 + minutes % 60;
            if (offset > 0) {
                result.append(" GMT+");
            } else {
                result.append(" GMT-");
                offset = -offset;
            }
            append0PaddedUint(result, offset, 4);
            if (timeZoneFormatter == null) timeZoneFormatter = new String("zzz");
            if (t < 0.0 || t > 2145916800000.0) {
                int equiv = EquivalentYear(YearFromTime(local));
                double day = MakeDay(equiv, MonthFromTime(t), DateFromTime(t));
                t = MakeDate(day, TimeWithinDay(t));
            }
            result.append(" (");
            java.util.Date date = new Date((long) t);
            synchronized (timeZoneFormatter) {
                timeZoneFormatter = date.toString();
                result.append(timeZoneFormatter);
            }
            result.append(')');
        }
        return result.toString();
    }

    private static Object jsConstructor(Object[] args) {
        NativeDate obj = new NativeDate();
        if (args.length == 0) {
            obj.date = now();
            return obj;
        }
        if (args.length == 1) {
            Object arg0 = args[0];
            if (arg0 instanceof Scriptable) arg0 = ((Scriptable) arg0).getDefaultValue(null);
            double date;
            if (arg0 instanceof String) {
                date = date_parseString((String) arg0);
            } else {
                date = ScriptRuntime.toNumber(arg0);
            }
            obj.date = TimeClip(date);
            return obj;
        }
        double time = date_msecFromArgs(args);
        if (!Double.isNaN(time) && !Double.isInfinite(time)) time = TimeClip(internalUTC(time));
        obj.date = time;
        return obj;
    }

    private static String toLocale_helper(double t, int methodId) {
        String formatter;
        Date datum = new Date();
        switch(methodId) {
            case Id_toLocaleDateString:
                if (localeDateFormatter == null) {
                    localeDateFormatter = datum.toString();
                }
                formatter = localeDateFormatter;
                break;
            default:
                formatter = null;
        }
        synchronized (formatter) {
            return formatter;
        }
    }

    private static String js_toUTCString(double date) {
        StringBuffer result = new StringBuffer(60);
        appendWeekDayName(result, WeekDay(date));
        result.append(", ");
        append0PaddedUint(result, DateFromTime(date), 2);
        result.append(' ');
        appendMonthName(result, MonthFromTime(date));
        result.append(' ');
        int year = YearFromTime(date);
        if (year < 0) {
            result.append('-');
            year = -year;
        }
        append0PaddedUint(result, year, 4);
        result.append(' ');
        append0PaddedUint(result, HourFromTime(date), 2);
        result.append(':');
        append0PaddedUint(result, MinFromTime(date), 2);
        result.append(':');
        append0PaddedUint(result, SecFromTime(date), 2);
        result.append(" GMT");
        return result.toString();
    }

    private static void append0PaddedUint(StringBuffer sb, int i, int minWidth) {
        if (i < 0) Kit.codeBug();
        int scale = 1;
        --minWidth;
        if (i >= 10) {
            if (i < 1000 * 1000 * 1000) {
                for (; ; ) {
                    int newScale = scale * 10;
                    if (i < newScale) {
                        break;
                    }
                    --minWidth;
                    scale = newScale;
                }
            } else {
                minWidth -= 9;
                scale = 1000 * 1000 * 1000;
            }
        }
        while (minWidth > 0) {
            sb.append('0');
            --minWidth;
        }
        while (scale != 1) {
            sb.append((char) ('0' + (i / scale)));
            i %= scale;
            scale /= 10;
        }
        sb.append((char) ('0' + i));
    }

    private static void appendMonthName(StringBuffer sb, int index) {
        String months = "Jan" + "Feb" + "Mar" + "Apr" + "May" + "Jun" + "Jul" + "Aug" + "Sep" + "Oct" + "Nov" + "Dec";
        index *= 3;
        for (int i = 0; i != 3; ++i) {
            sb.append(months.charAt(index + i));
        }
    }

    private static void appendWeekDayName(StringBuffer sb, int index) {
        String days = "Sun" + "Mon" + "Tue" + "Wed" + "Thu" + "Fri" + "Sat";
        index *= 3;
        for (int i = 0; i != 3; ++i) {
            sb.append(days.charAt(index + i));
        }
    }

    private static double makeTime(double date, Object[] args, int methodId) {
        int maxargs;
        boolean local = true;
        switch(methodId) {
            case Id_setUTCMilliseconds:
                local = false;
            case Id_setMilliseconds:
                maxargs = 1;
                break;
            case Id_setUTCSeconds:
                local = false;
            case Id_setSeconds:
                maxargs = 2;
                break;
            case Id_setUTCMinutes:
                local = false;
            case Id_setMinutes:
                maxargs = 3;
                break;
            case Id_setUTCHours:
                local = false;
            case Id_setHours:
                maxargs = 4;
                break;
            default:
                Kit.codeBug();
                maxargs = 0;
        }
        int i;
        double conv[] = new double[4];
        double hour, min, sec, msec;
        double lorutime;
        double time;
        double result;
        if (date != date) return date;
        if (args.length == 0) args = ScriptRuntime.padArguments(args, 1);
        for (i = 0; i < args.length && i < maxargs; i++) {
            conv[i] = ScriptRuntime.toNumber(args[i]);
            if (conv[i] != conv[i] || Double.isInfinite(conv[i])) {
                return ScriptRuntime.NaN;
            }
            conv[i] = ScriptRuntime.toInteger(conv[i]);
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
        return date;
    }

    private static double makeDate(double date, Object[] args, int methodId) {
        int maxargs;
        boolean local = true;
        switch(methodId) {
            case Id_setUTCDate:
                local = false;
            case Id_setDate:
                maxargs = 1;
                break;
            case Id_setUTCMonth:
                local = false;
            case Id_setMonth:
                maxargs = 2;
                break;
            case Id_setUTCFullYear:
                local = false;
            case Id_setFullYear:
                maxargs = 3;
                break;
            default:
                Kit.codeBug();
                maxargs = 0;
        }
        int i;
        double conv[] = new double[3];
        double year, month, day;
        double lorutime;
        double result;
        if (args.length == 0) args = ScriptRuntime.padArguments(args, 1);
        for (i = 0; i < args.length && i < maxargs; i++) {
            conv[i] = ScriptRuntime.toNumber(args[i]);
            if (conv[i] != conv[i] || Double.isInfinite(conv[i])) {
                return ScriptRuntime.NaN;
            }
            conv[i] = ScriptRuntime.toInteger(conv[i]);
        }
        if (date != date) {
            if (args.length < 3) {
                return ScriptRuntime.NaN;
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
        return date;
    }

    protected int findPrototypeId(String s) {
        int id;
        L0: {
            id = 0;
            String X = null;
            int c;
            L: switch(s.length()) {
                case 6:
                    X = "getDay";
                    id = Id_getDay;
                    break L;
                case 7:
                    switch(s.charAt(3)) {
                        case 'D':
                            c = s.charAt(0);
                            if (c == 'g') {
                                X = "getDate";
                                id = Id_getDate;
                            } else if (c == 's') {
                                X = "setDate";
                                id = Id_setDate;
                            }
                            break L;
                        case 'T':
                            c = s.charAt(0);
                            if (c == 'g') {
                                X = "getTime";
                                id = Id_getTime;
                            } else if (c == 's') {
                                X = "setTime";
                                id = Id_setTime;
                            }
                            break L;
                        case 'Y':
                            c = s.charAt(0);
                            if (c == 'g') {
                                X = "getYear";
                                id = Id_getYear;
                            } else if (c == 's') {
                                X = "setYear";
                                id = Id_setYear;
                            }
                            break L;
                        case 'u':
                            X = "valueOf";
                            id = Id_valueOf;
                            break L;
                    }
                    break L;
                case 8:
                    switch(s.charAt(3)) {
                        case 'H':
                            c = s.charAt(0);
                            if (c == 'g') {
                                X = "getHours";
                                id = Id_getHours;
                            } else if (c == 's') {
                                X = "setHours";
                                id = Id_setHours;
                            }
                            break L;
                        case 'M':
                            c = s.charAt(0);
                            if (c == 'g') {
                                X = "getMonth";
                                id = Id_getMonth;
                            } else if (c == 's') {
                                X = "setMonth";
                                id = Id_setMonth;
                            }
                            break L;
                        case 'o':
                            X = "toSource";
                            id = Id_toSource;
                            break L;
                        case 't':
                            X = "toString";
                            id = Id_toString;
                            break L;
                    }
                    break L;
                case 9:
                    X = "getUTCDay";
                    id = Id_getUTCDay;
                    break L;
                case 10:
                    c = s.charAt(3);
                    if (c == 'M') {
                        c = s.charAt(0);
                        if (c == 'g') {
                            X = "getMinutes";
                            id = Id_getMinutes;
                        } else if (c == 's') {
                            X = "setMinutes";
                            id = Id_setMinutes;
                        }
                    } else if (c == 'S') {
                        c = s.charAt(0);
                        if (c == 'g') {
                            X = "getSeconds";
                            id = Id_getSeconds;
                        } else if (c == 's') {
                            X = "setSeconds";
                            id = Id_setSeconds;
                        }
                    } else if (c == 'U') {
                        c = s.charAt(0);
                        if (c == 'g') {
                            X = "getUTCDate";
                            id = Id_getUTCDate;
                        } else if (c == 's') {
                            X = "setUTCDate";
                            id = Id_setUTCDate;
                        }
                    }
                    break L;
                case 11:
                    switch(s.charAt(3)) {
                        case 'F':
                            c = s.charAt(0);
                            if (c == 'g') {
                                X = "getFullYear";
                                id = Id_getFullYear;
                            } else if (c == 's') {
                                X = "setFullYear";
                                id = Id_setFullYear;
                            }
                            break L;
                        case 'M':
                            X = "toGMTString";
                            id = Id_toGMTString;
                            break L;
                        case 'T':
                            X = "toUTCString";
                            id = Id_toUTCString;
                            break L;
                        case 'U':
                            c = s.charAt(0);
                            if (c == 'g') {
                                c = s.charAt(9);
                                if (c == 'r') {
                                    X = "getUTCHours";
                                    id = Id_getUTCHours;
                                } else if (c == 't') {
                                    X = "getUTCMonth";
                                    id = Id_getUTCMonth;
                                }
                            } else if (c == 's') {
                                c = s.charAt(9);
                                if (c == 'r') {
                                    X = "setUTCHours";
                                    id = Id_setUTCHours;
                                } else if (c == 't') {
                                    X = "setUTCMonth";
                                    id = Id_setUTCMonth;
                                }
                            }
                            break L;
                        case 's':
                            X = "constructor";
                            id = Id_constructor;
                            break L;
                    }
                    break L;
                case 12:
                    c = s.charAt(2);
                    if (c == 'D') {
                        X = "toDateString";
                        id = Id_toDateString;
                    } else if (c == 'T') {
                        X = "toTimeString";
                        id = Id_toTimeString;
                    }
                    break L;
                case 13:
                    c = s.charAt(0);
                    if (c == 'g') {
                        c = s.charAt(6);
                        if (c == 'M') {
                            X = "getUTCMinutes";
                            id = Id_getUTCMinutes;
                        } else if (c == 'S') {
                            X = "getUTCSeconds";
                            id = Id_getUTCSeconds;
                        }
                    } else if (c == 's') {
                        c = s.charAt(6);
                        if (c == 'M') {
                            X = "setUTCMinutes";
                            id = Id_setUTCMinutes;
                        } else if (c == 'S') {
                            X = "setUTCSeconds";
                            id = Id_setUTCSeconds;
                        }
                    }
                    break L;
                case 14:
                    c = s.charAt(0);
                    if (c == 'g') {
                        X = "getUTCFullYear";
                        id = Id_getUTCFullYear;
                    } else if (c == 's') {
                        X = "setUTCFullYear";
                        id = Id_setUTCFullYear;
                    } else if (c == 't') {
                        X = "toLocaleString";
                        id = Id_toLocaleString;
                    }
                    break L;
                case 15:
                    c = s.charAt(0);
                    if (c == 'g') {
                        X = "getMilliseconds";
                        id = Id_getMilliseconds;
                    } else if (c == 's') {
                        X = "setMilliseconds";
                        id = Id_setMilliseconds;
                    }
                    break L;
                case 17:
                    X = "getTimezoneOffset";
                    id = Id_getTimezoneOffset;
                    break L;
                case 18:
                    c = s.charAt(0);
                    if (c == 'g') {
                        X = "getUTCMilliseconds";
                        id = Id_getUTCMilliseconds;
                    } else if (c == 's') {
                        X = "setUTCMilliseconds";
                        id = Id_setUTCMilliseconds;
                    } else if (c == 't') {
                        c = s.charAt(8);
                        if (c == 'D') {
                            X = "toLocaleDateString";
                            id = Id_toLocaleDateString;
                        } else if (c == 'T') {
                            X = "toLocaleTimeString";
                            id = Id_toLocaleTimeString;
                        }
                    }
                    break L;
            }
            if (X != null && X != s && !X.equals(s)) id = 0;
            break L0;
        }
        return id;
    }

    private static final int ConstructorId_now = -3, ConstructorId_parse = -2, ConstructorId_UTC = -1, Id_constructor = 1, Id_toString = 2, Id_toTimeString = 3, Id_toDateString = 4, Id_toLocaleString = 5, Id_toLocaleTimeString = 6, Id_toLocaleDateString = 7, Id_toUTCString = 8, Id_toSource = 9, Id_valueOf = 10, Id_getTime = 11, Id_getYear = 12, Id_getFullYear = 13, Id_getUTCFullYear = 14, Id_getMonth = 15, Id_getUTCMonth = 16, Id_getDate = 17, Id_getUTCDate = 18, Id_getDay = 19, Id_getUTCDay = 20, Id_getHours = 21, Id_getUTCHours = 22, Id_getMinutes = 23, Id_getUTCMinutes = 24, Id_getSeconds = 25, Id_getUTCSeconds = 26, Id_getMilliseconds = 27, Id_getUTCMilliseconds = 28, Id_getTimezoneOffset = 29, Id_setTime = 30, Id_setMilliseconds = 31, Id_setUTCMilliseconds = 32, Id_setSeconds = 33, Id_setUTCSeconds = 34, Id_setMinutes = 35, Id_setUTCMinutes = 36, Id_setHours = 37, Id_setUTCHours = 38, Id_setDate = 39, Id_setUTCDate = 40, Id_setMonth = 41, Id_setUTCMonth = 42, Id_setFullYear = 43, Id_setUTCFullYear = 44, Id_setYear = 45, MAX_PROTOTYPE_ID = 45;

    private static final int Id_toGMTString = Id_toUTCString;

    private static java.util.TimeZone thisTimeZone;

    private static double LocalTZA;

    private static String timeZoneFormatter;

    private static String localeDateFormatter;

    private double date;
}
