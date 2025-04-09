package pl.otros.logview.store;

import pl.otros.logview.LogData;
import java.util.*;
import java.util.logging.Logger;

public class MemoryLogDataStore extends AbstractMemoryLogStore implements LogDataStore {

    private static final Logger LOGGER = Logger.getLogger(MemoryLogDataStore.class.getName());

    protected ArrayList<LogData> list;

    protected SortedSet<Date> s = new TreeSet<Date>();

    public MemoryLogDataStore() {
        list = new ArrayList<LogData>();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public void add(LogData... logDatas) {
        Arrays.sort(logDatas, logDataTimeComparator);
        for (int i = 0; i < logDatas.length; i++) {
            LogData logData = logDatas[i];
            logData.setId(getNextLogId());
            if (list.size() == 0 || logDataTimeComparator.compare(logData, list.get(list.size() - 1)) >= 0) {
                list.add(logData);
            } else {
                int index = getIndexToInsert(logData.getDate(), 0, list.size() - 1, list.size() / 2);
                list.add(index, logData);
            }
            if (list.size() > limit) {
                remove(0);
            }
        }
    }

    protected int getIndexToInsert(Date date, int downLimit, int upLimit, int startPoint) {
        Date dateInList = list.get(startPoint).getDate();
        int compareTo = date.compareTo(dateInList);
        if (upLimit - downLimit < 3) {
            for (int i = upLimit; i >= downLimit; i--) {
                dateInList = list.get(i).getDate();
                compareTo = date.compareTo(dateInList);
                if (compareTo == 0) {
                    return i;
                } else if (compareTo > 0) {
                    return i + 1;
                }
            }
            return downLimit;
        }
        if (compareTo < 0) {
            upLimit = startPoint;
        } else if (compareTo > 0) {
            downLimit = startPoint;
        } else {
            return startPoint;
        }
        startPoint = (downLimit + upLimit) / 2;
        return getIndexToInsert(date, downLimit, upLimit, startPoint);
    }

    @Override
    public void remove(int... rows) {
        LOGGER.fine(String.format("Removing %d rows, first sorting by id", rows.length));
        Arrays.sort(rows);
        LOGGER.finest("Rows sorted, removing from end");
        for (int i = rows.length - 1; i >= 0; i--) {
            LogData removed = list.remove(rows[i]);
            notable.removeNote(removed.getId(), false);
        }
        LOGGER.finest(String.format("%d rows where removed ", rows.length));
    }

    @Override
    public LogData getLogData(int id) {
        LogData logData = list.get(id);
        return logData;
    }

    @Override
    public LogData[] getLogData() {
        LogData[] datas = new LogData[list.size()];
        datas = list.toArray(datas);
        return datas;
    }

    @Override
    public int clear() {
        int size = list.size();
        if (size > 0) {
            list.clear();
            clearNotes();
        }
        return size;
    }

    @Override
    public Iterator<LogData> iterator() {
        return list.iterator();
    }

    @Override
    public Integer getLogDataIdInRow(int row) {
        return list.get(row).getId();
    }
}
