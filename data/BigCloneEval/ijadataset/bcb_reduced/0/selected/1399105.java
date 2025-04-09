package pt.utl.ist.lucene.forms;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Jorge Machado
 * @date 18/Ago/2008
 * @see pt.utl.ist.lucene.forms
 */
public class TimeBox {

    private long from;

    private long to;

    private long middle;

    public TimeBox(long from, long to) {
        this.from = from;
        this.to = to;
        middle = (from + to) / 2;
    }

    public long getFrom() {
        return from;
    }

    public void setFrom(long from) {
        this.from = from;
    }

    public long getTo() {
        return to;
    }

    public void setTo(long to) {
        this.to = to;
    }

    public int getTimeYear() {
        Date time = new Date(middle);
        return Integer.parseInt(new SimpleDateFormat("yyyy").format(time));
    }

    public int getStartTimeYear() {
        Date time = new Date(from);
        return Integer.parseInt(new SimpleDateFormat("yyyy").format(time));
    }

    public int getEndTimeYear() {
        Date time = new Date(to);
        return Integer.parseInt(new SimpleDateFormat("yyyy").format(time));
    }
}
