package ProPesca.main;

import java.util.Vector;

/**
 *
 * @author Alberto Casagrande <alberto.casagrande@gmail.com>
 */
public class SamplingDates {

    public class NotRecordedDate extends Exception {

        public NotRecordedDate(Date d, int i) {
            position = i;
            date = d;
        }

        public int position;

        public Date date;
    }

    public class AlreadyStoredDate extends Exception {

        public AlreadyStoredDate() {
        }
    }

    /** Creates a new instance of SamplingDates */
    public SamplingDates() {
        dates = new Vector();
    }

    public int size() {
        return dates.size();
    }

    public int search(Date date) throws NotRecordedDate {
        int p_index;
        int min = 0;
        int max = dates.size() - 1;
        if (max < min) throw new NotRecordedDate(date, 0);
        do {
            p_index = (max + min) / 2;
            if (((Date) (dates.get(p_index))).Equal_To(date)) {
                return p_index;
            }
            if (min == max) {
                throw new NotRecordedDate(date, min + 1);
            }
            if (((Date) (dates.get(p_index))).Smaller_Than(date)) {
                min = p_index + 1;
            } else {
                max = p_index;
            }
        } while (true);
    }

    public boolean contains(Date date) {
        try {
            search(date);
            return true;
        } catch (SamplingDates.NotRecordedDate ex) {
            return false;
        }
    }

    public int add(Date date) throws AlreadyStoredDate {
        try {
            search(date);
            throw new AlreadyStoredDate();
        } catch (NotRecordedDate ex) {
            dates.add(ex.position, date);
            return ex.position;
        }
    }

    public Date get(int i) {
        return (Date) dates.get(i);
    }

    public String toString() {
        String output = new String();
        for (int i = 0; i < dates.size(); i++) {
            if (i > 0) output = output + "\n";
            output = output + new String("Date[") + (new Integer(i).toString()) + "]=" + dates.get(i).toString();
        }
        return output;
    }

    private Vector dates;
}
