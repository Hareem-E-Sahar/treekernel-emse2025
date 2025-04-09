package com.ibm.tuningfork.infra.stream;

import com.ibm.tuningfork.infra.data.ITimedData;
import com.ibm.tuningfork.infra.event.IEvent;
import com.ibm.tuningfork.infra.stream.core.IStreamCursor;
import com.ibm.tuningfork.infra.stream.core.Stream;

public class IncrementalEventStreamCursor implements IStreamCursor {

    protected final NonblockingMultipleInputStreamManager eventStream;

    protected final IncrementalSummarizingEventStream summaryStream;

    protected final long minTime, maxTime;

    protected final boolean forward;

    protected IEventSummarizer summarizer;

    protected IEvent initialState;

    protected long initialIndex;

    protected Stream[] inputs;

    public IncrementalEventStreamCursor(IncrementalSummarizingEventStream summaryStream, Stream[] inputs, IEventSummarizer summarizer, long startTime, long endTime) {
        this.summaryStream = summaryStream;
        this.inputs = inputs;
        forward = startTime <= endTime;
        this.minTime = Math.min(startTime, endTime);
        this.maxTime = Math.max(startTime, endTime);
        this.eventStream = new NonblockingMultipleInputStreamManager(summaryStream, inputs, startTime, endTime);
        this.initialState = (IEvent) summaryStream.newUnderlyingCursor(startTime, 0).getNext();
        this.summarizer = summarizer;
        reset();
    }

    public boolean hasMore() {
        return eventStream.hasNext();
    }

    public boolean eof() {
        return !hasMore();
    }

    public void moveToEnd() {
    }

    public void blockForMore() {
    }

    long getStartingSummary(long tick) {
        long low = 0, mid = 0, high = summaryStream.getLength() - 1;
        IEvent f = null;
        while (high >= low) {
            mid = (high + low) / 2;
            f = summaryStream.getEvent(mid);
            if (tick == f.getTime()) return mid; else if (tick < f.getTime()) high = mid - 1; else low = mid + 1;
        }
        return high >= 0 ? high : 0;
    }

    public IEvent getNext() {
        if (forward) {
            ITimedData event = (ITimedData) eventStream.getNext();
            summarizer.addToSummary(event, inputs[eventStream.getCurrentStream()]);
        } else if (summaryStream.getLength() > 0) {
            IEvent start = summaryStream.getEvent(initialIndex);
            summarizer = summarizer.createNew(start);
            int count = 0;
            while (eventStream.hasNext()) {
                ITimedData event = (ITimedData) eventStream.getNext();
                if (event.getTime() > start.getTime()) {
                    break;
                }
                count++;
                summarizer.addToSummary(event, inputs[eventStream.getCurrentStream()]);
            }
            if (count == 0) {
                if (initialIndex > 0) {
                    initialIndex--;
                }
            }
        }
        return summarizer.getSummary();
    }

    public void reset() {
        eventStream.start();
        if (initialState != null) {
            summarizer = summarizer.createNew(initialState);
            initialIndex = getStartingSummary(initialState.getTime());
        } else {
            initialIndex = 0;
        }
    }
}
