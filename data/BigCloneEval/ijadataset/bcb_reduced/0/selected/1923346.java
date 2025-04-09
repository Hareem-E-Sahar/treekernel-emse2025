package edu.washington.mysms.server.sample.starbus;

import java.lang.reflect.Constructor;
import java.sql.Time;
import java.util.Properties;
import edu.washington.mysms.coding.ColumnsDefinition;
import edu.washington.mysms.coding.ComplianceException;
import edu.washington.mysms.coding.ResultColumn;
import edu.washington.mysms.coding.ResultRow;
import edu.washington.mysms.coding.ResultTable;

public class TimeEstimate extends Time {

    private static final long serialVersionUID = -9077608211458239198L;

    private static String localized_eta_format = "edu.washington.mysms.server.sample.starbus.EnglishTimeEstimateFormat";

    private static long max_estimate_value = 900000;

    private boolean orMore;

    private Time earliest;

    private Time latest;

    public TimeEstimate(long time) {
        this(time, false);
    }

    public TimeEstimate(long time, boolean orMore) {
        super(time);
        this.orMore = orMore;
        this.earliest = null;
        this.latest = null;
        if (time > max_estimate_value) {
            this.setTime(max_estimate_value);
            this.orMore = true;
        }
    }

    public TimeEstimate(long time, Time earliest, Time latest) {
        super(time);
        this.orMore = false;
        this.earliest = earliest;
        this.latest = latest;
        if (earliest.getTime() > max_estimate_value) {
            this.setTime(max_estimate_value);
            this.orMore = true;
            this.earliest = null;
            this.latest = null;
        }
    }

    /**
	 * Initialize the static parameters to configured values from properties.
	 * 
	 * @param props
	 */
    public static void initializeParameters(Properties props) {
        TimeEstimate.localized_eta_format = props.getProperty("starbus.localized_eta_format", localized_eta_format);
        TimeEstimate.max_estimate_value = Long.parseLong(props.getProperty("starbus.max_estimate_value", Long.toString(max_estimate_value)));
    }

    public boolean isOrMore() {
        return orMore;
    }

    public Time getAsTime() {
        return new Time(this.getTime());
    }

    public Time getEarliest() {
        return earliest;
    }

    public Time getLatest() {
        return latest;
    }

    @SuppressWarnings("unchecked")
    public String toString() {
        try {
            Class<? extends TimeEstimateFormat> formatClass = (Class<? extends TimeEstimateFormat>) Class.forName(localized_eta_format);
            Constructor<? extends TimeEstimateFormat> constructor = formatClass.getConstructor();
            TimeEstimateFormat format = constructor.newInstance();
            return "ETA: " + format.format(this);
        } catch (Exception e) {
            return "ETA: " + super.toString();
        }
    }

    public ResultRow toRow(Route route, ColumnsDefinition columns) {
        ResultRow row = new ResultRow(columns);
        row.set("Route Number", route.getRouteNumber());
        row.set("ETA", this.getAsTime());
        row.set("orMore", new Boolean(this.isOrMore()));
        row.set("Earliest", this.getEarliest());
        row.set("Latest", this.getLatest());
        row.set("asString", this.toString());
        if (!row.isCompliant()) {
            throw new ComplianceException("Could not create a complaint row.");
        }
        return row;
    }

    public static class TimeEstimateTable extends ResultTable {

        private static final long serialVersionUID = -3919785470053145374L;

        public TimeEstimateTable() {
            super();
            ColumnsDefinition columns = new ColumnsDefinition();
            columns.add(new ResultColumn("Route Number", false, Short.class));
            columns.add(new ResultColumn("ETA", false, Time.class));
            columns.add(new ResultColumn("orMore", false, Boolean.class));
            columns.add(new ResultColumn("Earliest", true, Time.class));
            columns.add(new ResultColumn("Latest", true, Time.class));
            columns.add(new ResultColumn("asString", false, String.class));
            columns.finalize();
            this.setColumnsDefinition(columns);
        }
    }
}
