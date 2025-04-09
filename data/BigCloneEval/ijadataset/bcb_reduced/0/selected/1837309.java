package at.ac.ait.enviro.dscsv.util.source;

import at.ac.ait.enviro.tsapi.timeseries.TimeStamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Thomas Ponweiser
 */
public class TimeStampParserTest {

    public TimeStampParserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testDefault() throws ParseException {
        final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        final String[] timestamps = { "2009-04-01", "2009-04-02", "2009-04-03", "2009-04-04", "2009-04-05" };
        final int colIndex = 1;
        final int arrSz = 3;
        final String[] fields = new String[arrSz];
        final TimeStampParser p = new TimeStampParser(colIndex, fmt);
        for (int i = 0; i < timestamps.length; i++) {
            fields[colIndex] = timestamps[i];
            assertEquals("parsing failed.", timestamps[i], fmt.format(((TimeStamp) p.parse(fields)).getAsDate()));
        }
    }

    @Test
    public void testAdvanced() throws ParseException {
        final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        final String[] dates = { "2009-04-01", "2009-04-02", "2009-04-03" };
        final String[] times = { "00:00", "12:00" };
        final int dateIndex = 1;
        final int timeIndex = 3;
        final int arrSz = 4;
        final String[] fields = new String[arrSz];
        final TimeStampParser p = new TimeStampParser(TimeZone.getDefault());
        p.addColumn(dateIndex, "yyyy-MM-dd");
        p.addColumn(timeIndex, "HH:mm");
        for (int i = 0; i < dates.length; i++) {
            fields[dateIndex] = dates[i];
            for (int j = 0; j < times.length; j++) {
                fields[timeIndex] = times[j];
                assertEquals("parsing failed.", dates[i] + "T" + times[j], fmt.format(((TimeStamp) p.parse(fields)).getAsDate()));
            }
        }
    }
}
