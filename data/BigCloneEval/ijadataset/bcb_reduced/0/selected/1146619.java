package net.sourceforge.hidapa.tests;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import net.sourceforge.hidapa.DateParser;
import junit.framework.*;

/**
 * @author Volker Berlin
 */
public class ParserTest extends TestCase {

    private static final int DEFAULT_START_2DIGIT_YEAR = new Date().getYear() + 1850;

    private final SimpleDateFormat format;

    private final Locale locale;

    public ParserTest(SimpleDateFormat format, Locale locale) {
        super(format.toPattern());
        this.format = format;
        this.locale = locale;
    }

    @Override
    protected void runTest() throws Throwable {
        Date date = getRandomDate();
        String str = format.format(date);
        DateParser parser = new DateParser(locale);
        Date parseDate = parser.parse(str);
        try {
            assertEquals(str, format.format(parseDate));
            assertEquals(date, parseDate);
        } catch (Throwable ex) {
            parser.parse(str);
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * Create a random date in a range of 100 years that 2 digit years can parse correctly
     * @return a new date
     */
    static Date getRandomDate() {
        Random random = new Random();
        Date date = new Date(DEFAULT_START_2DIGIT_YEAR - 1900, 0, Math.abs(random.nextInt()) / 60000, 0, 0, 0);
        return date;
    }

    public static Test suite() throws Exception {
        Locale loc = new Locale("ja");
        DateFormat sformat = DateFormat.getDateInstance(DateFormat.SHORT, loc);
        String str = sformat.format(new Date(106, 0, 11));
        DateParser parser = new DateParser(loc);
        Date date = parser.parse(str);
        String str2 = sformat.format(date);
        TestSuite suite = new TestSuite();
        Locale[] locales = Locale.getAvailableLocales();
        for (Locale locale : locales) {
            TestSuite testLocale = new TestSuite(locale.toString());
            suite.addTest(testLocale);
            SimpleDateFormat format = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, locale);
            testLocale.addTest(new ParserTest(format, locale));
            format = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
            testLocale.addTest(new ParserTest(format, locale));
            format = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.LONG, locale);
            testLocale.addTest(new ParserTest(format, locale));
            format = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.FULL, locale);
            testLocale.addTest(new ParserTest(format, locale));
        }
        return suite;
    }
}
