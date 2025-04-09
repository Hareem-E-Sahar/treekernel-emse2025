package com.ibm.tuningfork.distributed;

import com.ibm.tuningfork.infra.event.TypedEvent;

public class DriftRecord {

    public final long start;

    public final long middle;

    public final long end;

    public final long total;

    public final long error;

    public final long localMiddle;

    public final long drift;

    public static final int DRIFT_START_INDEX = 0;

    public static final int DRIFT_MIDDLE_INDEX = 1;

    public static final int DRIFT_END_INDEX = 2;

    public DriftRecord(TypedEvent e) {
        start = e.getLong(DRIFT_START_INDEX);
        middle = e.getLong(DRIFT_MIDDLE_INDEX);
        end = e.getLong(DRIFT_END_INDEX);
        total = end - start;
        error = total / 2;
        localMiddle = (end + start) / 2;
        drift = middle - localMiddle;
    }
}
