package com.tomgibara.crinch.record.index;

import java.io.File;
import java.util.NoSuchElementException;
import com.tomgibara.crinch.bits.BitReader;
import com.tomgibara.crinch.bits.FileBitReaderFactory;
import com.tomgibara.crinch.bits.FileBitReaderFactory.Mode;
import com.tomgibara.crinch.coding.CodedReader;
import com.tomgibara.crinch.coding.FibonacciCoding;
import com.tomgibara.crinch.record.EmptyRecord;
import com.tomgibara.crinch.record.RecordProducer;
import com.tomgibara.crinch.record.RecordSequence;
import com.tomgibara.crinch.record.RecordStats;
import com.tomgibara.crinch.record.process.ProcessContext;

public class PositionProducer implements RecordProducer<EmptyRecord> {

    private RecordStats recStats;

    private PositionStats posStats;

    private FileBitReaderFactory fbrf;

    private long oversizedStart;

    private long oversizedFinish;

    @Override
    public void prepare(ProcessContext context) {
        recStats = context.getRecordStats();
        if (recStats == null) throw new IllegalArgumentException("no record stats");
        posStats = new PositionStats(context);
        posStats.read();
        File file = context.file(posStats.type, false, posStats.definition);
        fbrf = new FileBitReaderFactory(file, Mode.CHANNEL);
        oversizedStart = posStats.fixedBitSize * recStats.getRecordCount();
        oversizedFinish = posStats.bitsWritten;
    }

    @Override
    public Accessor open() {
        return new Accessor();
    }

    @Override
    public void complete() {
        fbrf = null;
    }

    public class Accessor implements RecordSequence<EmptyRecord> {

        private final BitReader reader;

        private final CodedReader coded;

        private final long recordCount = recStats.getRecordCount();

        private final int fixedBitSize = posStats.fixedBitSize;

        private final long invalid = 1 << (fixedBitSize - 1);

        private final long negativeBoundary = 1 << (fixedBitSize - 1);

        private final long negativeMask = -1L << fixedBitSize;

        private final int maxDepth = 65 - Long.numberOfLeadingZeros(recordCount);

        private final long[] stack = new long[maxDepth * 4];

        private long count;

        private int depth;

        Accessor() {
            reader = fbrf.openReader();
            coded = new CodedReader(reader, FibonacciCoding.extended);
            stack[0] = 0L;
            stack[1] = posStats.bottomPosition;
            stack[2] = recordCount - 1L;
            stack[3] = posStats.topPosition;
            depth = 0;
        }

        @Override
        public boolean hasNext() {
            return count < recordCount;
        }

        @Override
        public EmptyRecord next() {
            if (!hasNext()) throw new NoSuchElementException();
            if (count == recordCount - 1L) {
                return new EmptyRecord(count++, posStats.topPosition);
            }
            while (depth > 0 && stack[depth * 4 + 2] == stack[(depth - 1) * 4 + 2]) {
                depth--;
            }
            if (depth > 0) {
                stack[depth * 4 + 0] = stack[depth * 4 + 2];
                stack[depth * 4 + 1] = stack[depth * 4 + 3];
                stack[depth * 4 + 2] = stack[(depth - 1) * 4 + 2];
                stack[depth * 4 + 3] = stack[(depth - 1) * 4 + 3];
            }
            while (true) {
                long bottomOrdinal = stack[depth * 4 + 0];
                long bottomPosition = stack[depth * 4 + 1];
                long topOrdinal = stack[depth * 4 + 2];
                long topPosition = stack[depth * 4 + 3];
                long ord = (bottomOrdinal + topOrdinal) / 2;
                if (ord == bottomOrdinal || ord == topOrdinal) {
                    count++;
                    return new EmptyRecord(bottomOrdinal, bottomPosition);
                }
                reader.setPosition(ord * fixedBitSize);
                long err = reader.readLong(fixedBitSize);
                long pos;
                if (err == invalid) {
                    pos = findPosition(ord);
                } else {
                    if (err >= negativeBoundary) err |= negativeMask;
                    long est = (topPosition + bottomPosition) / 2;
                    pos = est + err;
                }
                depth++;
                stack[depth * 4 + 0] = bottomOrdinal;
                stack[depth * 4 + 1] = bottomPosition;
                stack[depth * 4 + 2] = ord;
                stack[depth * 4 + 3] = pos;
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() {
            fbrf.closeReader(reader);
        }

        private long findPositionSlow(long ordinal) {
            reader.setPosition(oversizedStart);
            while (reader.getPosition() < oversizedFinish) {
                long ord = coded.readPositiveLong() / 2;
                long pos = (coded.readPositiveLong() - 1L) / 2;
                if (ord == ordinal) return pos;
            }
            return -1L;
        }

        private long findPosition(long ordinal) {
            if (oversizedStart == oversizedFinish) return -1L;
            return findPosition(oversizedStart, oversizedFinish, ordinal);
        }

        private long findPosition(long from, long to, long ordinal) {
            long mid = (from + to) / 2;
            long offset = 100L;
            long start = Math.max(mid - offset, oversizedStart);
            reader.setPosition(start);
            if (start != oversizedStart) {
                boolean last = false;
                for (; start < mid; start++) {
                    boolean bit = reader.readBoolean();
                    if (bit && last) break;
                    last = bit;
                }
            }
            if (start == mid) throw new IllegalStateException("No consecutive set bits between " + (mid - offset) + " and mid");
            while (true) {
                long left = reader.getPosition();
                long a = coded.readPositiveLong();
                if ((a & 1L) == 1L) continue;
                long b = coded.readPositiveLong();
                long right = reader.getPosition();
                long ord = a / 2;
                long pos = (b - 1L) / 2;
                if (ord == ordinal) {
                    return pos;
                } else if (ordinal < ord) {
                    return left == oversizedStart ? -1L : findPosition(from, left, ordinal);
                } else if (right >= mid) {
                    return right == oversizedFinish ? -1L : findPosition(right, to, ordinal);
                }
            }
        }
    }
}
