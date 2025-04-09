package com.incendiaryblue.util;

import java.util.*;
import java.text.*;

public class CronInterval {

    private static final String ANY_PATTERN = "*";

    private static int[] dayOfWeekMap = { Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY };

    private int[] minute;

    private int[] hour;

    private int[] day;

    private int[] month;

    private int[] dayOfWeek;

    private String pattern;

    private long lastRun = -1;

    public CronInterval(String pattern) {
        this.pattern = pattern;
        StringTokenizer st = new StringTokenizer(pattern, " ");
        if (st.countTokens() != 5) {
            throw new IllegalArgumentException("Illegal cron interval pattern: " + pattern);
        }
        minute = parseInterval(st.nextToken(), 0, 59);
        hour = parseInterval(st.nextToken(), 0, 23);
        day = parseInterval(st.nextToken(), 1, 31);
        month = parseInterval(st.nextToken(), 1, 12);
        dayOfWeek = parseInterval(st.nextToken(), 0, 7);
        if ((day.length > 0) && (month.length > 0)) {
            if ((day[day.length - 1] > 31) || (month[month.length - 1] > 12)) {
                throw new IllegalArgumentException("Illegal values in day or month fields: " + pattern);
            }
        }
        if (day.length > 0) {
            if ((day[0] > 29) && (month.length == 1) && (month[0] == 2)) {
                throw new IllegalArgumentException("Illegal values - time never reached!.. " + pattern);
            } else if (day[0] > 30) {
                if (month.length > 0) {
                    boolean ok = false;
                    for (int i = 0; i < month.length; i++) {
                        if ((month[i] != 4) && (month[i] != 6) && (month[i] != 9) && (month[i] != 11) && (month[i] != 2)) {
                            ok = true;
                            break;
                        }
                    }
                    if (!ok) throw new IllegalArgumentException("Illegal values - time never reached!.. " + pattern);
                }
            }
        }
        for (int i = 0; i < month.length; ++i) --month[i];
        for (int i = 0; i < dayOfWeek.length; ++i) dayOfWeek[i] = dayOfWeekMap[dayOfWeek[i]];
    }

    public String getPattern() {
        return pattern;
    }

    private static int[] parseInterval(String interval, int minBoundary, int maxBoundary) {
        String next;
        int nextInt;
        StringTokenizer st = new StringTokenizer(interval.trim(), ",");
        Collection result = new TreeSet();
        while (st.hasMoreTokens()) {
            next = st.nextToken().trim();
            if (next.equals(ANY_PATTERN)) {
                if (st.countTokens() > 1) {
                    throw new IllegalArgumentException("Illegal cron interval pattern: " + interval);
                }
                return new int[0];
            } else {
                if (next.indexOf("-") != -1) {
                    String start = next.substring(0, next.indexOf("-")).trim();
                    String end = next.substring(next.indexOf("-") + 1).trim();
                    int startVal = getIntVal(start, minBoundary, maxBoundary);
                    int endVal = getIntVal(end, minBoundary, maxBoundary);
                    if (startVal >= endVal) {
                        throw new IllegalArgumentException("Illegal cron interval pattern: " + interval);
                    }
                    for (int i = startVal; i < endVal + 1; i++) {
                        result.add(new Integer(i));
                    }
                } else {
                    int val = getIntVal(next, minBoundary, maxBoundary);
                    result.add(new Integer(val));
                }
            }
        }
        int[] ret = new int[result.size()];
        int count = 0;
        for (Iterator i = result.iterator(); i.hasNext(); count++) {
            ret[count] = ((Integer) i.next()).intValue();
        }
        return ret;
    }

    private static int getIntVal(String val, int minBoundary, int maxBoundary) {
        try {
            int i = Integer.parseInt(val);
            if ((i < minBoundary) || (i > maxBoundary)) {
                throw new Exception();
            }
            return i;
        } catch (Exception e) {
            throw new IllegalArgumentException("Non-integer or out of range value found in cron interval");
        }
    }

    public Date getNextTime() {
        return getNextTimeAfter(new Date());
    }

    public Date getNextTimeAfter(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.add(Calendar.MINUTE, 1);
        findNextMonth(c);
        return c.getTime();
    }

    private void findNextMonth(Calendar c) {
        if (month.length == 0) {
            findNextDay(c);
            return;
        }
        int year = c.get(Calendar.YEAR);
        int i = findFirstGreaterOrEqual(month, c.get(Calendar.MONTH));
        while (true) {
            if (i == -1 || i == month.length) {
                i = 0;
                c.clear();
                c.set(Calendar.YEAR, ++year);
            }
            int m = month[i];
            if (m != c.get(Calendar.MONTH)) {
                c.set(Calendar.MONTH, m);
                c.set(Calendar.DAY_OF_MONTH, 1);
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
            }
            findNextDay(c);
            if (c.get(Calendar.MONTH) == m) return;
            ++i;
        }
    }

    private void findNextDay(Calendar c) {
        if (dayOfWeek.length == 0 && day.length == 0) {
            findNextHour(c);
            return;
        }
        while (true) {
            while (!dayMatches(c)) {
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.add(Calendar.DAY_OF_MONTH, 1);
            }
            findNextHour(c);
            if (dayMatches(c)) return;
        }
    }

    private boolean dayMatches(Calendar c) {
        if (dayOfWeek.length != 0 && !contains(dayOfWeek, c.get(Calendar.DAY_OF_WEEK))) return false;
        if (day.length != 0 && !contains(day, c.get(Calendar.DAY_OF_MONTH))) return false;
        return true;
    }

    private void findNextHour(Calendar c) {
        if (hour.length == 0) {
            findNextMinute(c);
            return;
        }
        int i = findFirstGreaterOrEqual(hour, c.get(Calendar.HOUR_OF_DAY));
        while (true) {
            if (i == -1 || i == hour.length) {
                i = 0;
                c.set(Calendar.MINUTE, 0);
                c.add(Calendar.DAY_OF_YEAR, 1);
            }
            int h = hour[i];
            if (h != c.get(Calendar.HOUR_OF_DAY)) {
                c.set(Calendar.HOUR_OF_DAY, h);
                c.set(Calendar.MINUTE, 0);
            }
            findNextMinute(c);
            if (h == c.get(Calendar.HOUR_OF_DAY)) return;
            ++i;
        }
    }

    private void findNextMinute(Calendar c) {
        if (minute.length == 0) return;
        int i = findFirstGreaterOrEqual(minute, c.get(Calendar.MINUTE));
        if (i == -1) {
            i = 0;
            c.add(Calendar.HOUR_OF_DAY, 1);
        }
        c.set(Calendar.MINUTE, minute[i]);
        return;
    }

    public Iterator iterator() {
        return this.new CronIterator(new Date());
    }

    public Iterator iterator(Date d) {
        return this.new CronIterator(d);
    }

    private static boolean contains(int[] ia, int i) {
        for (int j = 0; j < ia.length; j++) {
            if (ia[j] == i) {
                return true;
            }
        }
        return false;
    }

    private static int findFirstGreaterOrEqual(int[] ia, int i) {
        for (int j = 0; j < ia.length; j++) {
            if (ia[j] >= i) {
                return j;
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        try {
            if (args.length >= 2 && args.length <= 3) runCommandLine(args); else if (args.length == 0) runTests(); else {
                System.err.println("Usage: CronInterval [ \"* * * * *\" \"yyyy-MM-dd hh:mm\" [ COUNT ] ]");
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void runCommandLine(String[] args) throws ParseException {
        CronInterval c = new CronInterval(args[0]);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date arg1 = df.parse(args[1]);
        int loopCount = (args.length > 2) ? Integer.parseInt(args[2]) : 1;
        for (Iterator i = c.iterator(arg1); loopCount > 0; loopCount--) {
            Date d = (Date) i.next();
            System.out.println(loopCount + ": " + df.format(d));
        }
    }

    private static void runTests() throws ParseException {
        boolean ok = true;
        ok &= test("* * * * *", "2000-01-01 00:00", "2000-01-01 00:01");
        ok &= test("2 * * * *", "2000-01-01 00:00", "2000-01-01 00:02");
        ok &= test("2 * * * *", "2000-01-01 00:02", "2000-01-01 01:02");
        ok &= test("2,17 * * * *", "2000-01-01 00:05", "2000-01-01 00:17");
        ok &= test("2,17 * * * *", "2000-01-01 00:19", "2000-01-01 01:02");
        ok &= test("* 0 * * *", "2000-01-01 00:00", "2000-01-01 00:01");
        ok &= test("* 0 * * *", "2000-01-01 00:59", "2000-01-02 00:00");
        ok &= test("* 7 * * *", "2000-01-01 00:00", "2000-01-01 07:00");
        ok &= test("2,17 7 * * *", "2000-01-01 00:00", "2000-01-01 07:02");
        ok &= test("2,17 7 * * *", "2000-01-01 07:05", "2000-01-01 07:17");
        ok &= test("2,17 7 * * *", "2000-01-01 07:19", "2000-01-02 07:02");
        ok &= test("2,17 7,9 * * *", "2000-01-01 07:19", "2000-01-01 09:02");
        ok &= test("2,17 7,9 * * *", "2000-01-01 09:05", "2000-01-01 09:17");
        ok &= test("2,17 7,9 * * *", "2000-01-01 09:19", "2000-01-02 07:02");
        ok &= test("* * 1 * *", "2000-01-01 00:00", "2000-01-01 00:01");
        ok &= test("* * 1 * *", "2000-01-01 00:01", "2000-01-01 00:02");
        ok &= test("* * 1 * *", "2000-01-02 00:00", "2000-02-01 00:00");
        ok &= test("* * 5 * *", "2000-01-01 00:00", "2000-01-05 00:00");
        ok &= test("* * 31 * *", "2000-01-01 00:00", "2000-01-31 00:00");
        ok &= test("* * 31 * *", "2000-02-01 00:00", "2000-03-31 00:00");
        ok &= test("2,17 7,9 31 * *", "2000-01-01 00:00", "2000-01-31 07:02");
        ok &= test("2,17 7,9 31 * *", "2000-01-31 07:05", "2000-01-31 07:17");
        ok &= test("2,17 7,9 31 * *", "2000-01-31 07:19", "2000-01-31 09:02");
        ok &= test("2,17 7,9 31 * *", "2000-01-31 09:05", "2000-01-31 09:17");
        ok &= test("2,17 7,9 31 * *", "2000-01-31 09:19", "2000-03-31 07:02");
        ok &= test("* * * 3 *", "2000-01-01 00:00", "2000-03-01 00:00");
        ok &= test("* * * 3 *", "2000-03-01 00:00", "2000-03-01 00:01");
        ok &= test("* * * * 0", "2002-02-26 00:00", "2002-03-03 00:00");
        ok &= test("* * 1 * 0", "2002-02-26 00:00", "2002-09-01 00:00");
        ok &= test("* * * 2 *", "2000-01-02 00:00", "2000-02-01 00:00");
        if (ok) System.out.println("Passed all tests."); else System.out.println("Failed.");
    }

    private static boolean test(String pattern, String fromDateString, String expectedResultString) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date fromDate = dateFormat.parse(fromDateString);
        CronInterval c = new CronInterval(pattern);
        Date result = c.getNextTimeAfter(fromDate);
        String resultString = dateFormat.format(result);
        boolean ok = resultString.equals(expectedResultString);
        System.out.print(ok ? "  " : "X ");
        System.out.print(pattern);
        System.out.print(", ");
        System.out.print(fromDateString);
        System.out.print(" -> ");
        System.out.print(resultString);
        if (ok) System.out.println(", ok"); else {
            System.out.print(", exp ");
            System.out.println(expectedResultString);
        }
        return ok;
    }

    class CronIterator implements Iterator {

        long lastVal = -1;

        private CronIterator(Date startAt) {
            lastVal = startAt.getTime();
        }

        public boolean hasNext() {
            return true;
        }

        public Object next() {
            Date d = getNextTimeAfter(new Date(lastVal));
            lastVal = d.getTime();
            return d;
        }

        public void remove() throws UnsupportedOperationException {
            throw new UnsupportedOperationException("remove() not defined on a cron interval iterator");
        }
    }
}
