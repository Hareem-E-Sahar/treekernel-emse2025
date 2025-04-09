package pt.utl.ist.lucene.filter;

import com.pjaol.search.geo.utils.BoundaryBoxFilter;
import org.apache.solr.util.NumberUtils;
import pt.utl.ist.lucene.filter.distancebuilders.TimeDistanceBuilderFilter;
import pt.utl.ist.lucene.utils.Dates;
import java.util.GregorianCalendar;
import java.util.Map;

/**
 * @author Jorge Machado
 * @date 14/Ago/2008
 * @see pt.utl.ist.lucene
 */
public class TimeIntervalChainBuilder implements ITimeDistancesWrapper {

    public BoundaryBoxFilter timeFilter;

    public TimeDistanceBuilderFilter timeDistanceBuilderFilter;

    public TimeIntervalChainBuilder(long startTime, long endTime, String fld) {
        init(startTime, endTime, fld);
    }

    public TimeIntervalChainBuilder(String startTime, String endTime, String fld) {
        GregorianCalendar c1 = Dates.getGregorianCalendar(startTime);
        GregorianCalendar c2 = Dates.getGregorianCalendar(endTime);
        init(c1.getTimeInMillis(), c2.getTimeInMillis(), fld);
    }

    public void init(long startTime, long endTime, String fld) {
        String endDate = NumberUtils.long2sortableStr(endTime);
        String startDate = NumberUtils.long2sortableStr(startTime);
        timeFilter = new BoundaryBoxFilter(fld, startDate, endDate, true, true);
        long middleTimeMili = (endTime + startTime) / 2;
        long radiumMili = Math.abs(endTime - startTime) / 2;
        timeDistanceBuilderFilter = new TimeDistanceBuilderFilter(fld, middleTimeMili, radiumMili);
    }

    public void build(SerialChainFilterBuilder builder) {
        builder.andFilter(timeFilter);
        builder.serialAndFilter(timeDistanceBuilderFilter);
    }

    public TimeDistanceBuilderFilter getTimeDistanceBuilder() {
        return timeDistanceBuilderFilter;
    }

    public Map<Integer, Long> getTimeDistances() {
        return timeDistanceBuilderFilter.getTimeDistances();
    }

    public Long getTimeDistance(int doc) {
        return timeDistanceBuilderFilter.getTimeDistance(doc);
    }
}
