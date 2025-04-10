package com.tomgibara.crinch.record.index;

import java.io.File;
import java.util.List;
import com.tomgibara.crinch.bits.BitWriter;
import com.tomgibara.crinch.bits.NullBitWriter;
import com.tomgibara.crinch.coding.CodedStreams;
import com.tomgibara.crinch.coding.CodedWriter;
import com.tomgibara.crinch.coding.ExtendedCoding;
import com.tomgibara.crinch.coding.FibonacciCoding;
import com.tomgibara.crinch.record.LinearRecord;
import com.tomgibara.crinch.record.RecordConsumer;
import com.tomgibara.crinch.record.RecordStats;
import com.tomgibara.crinch.record.def.ColumnType;
import com.tomgibara.crinch.record.process.ProcessContext;

public class PositionConsumer implements RecordConsumer<LinearRecord> {

    private static void writeEntry(CodedWriter coded, int ordinal, long position) {
        coded.writePositiveInt(2 * ordinal);
        coded.writePositiveLong(2 * position + 1);
    }

    private ProcessContext context;

    private PositionStats posStats;

    private RecordStats recStats;

    private File file;

    private long[] positions;

    @Override
    public void prepare(ProcessContext context) {
        this.context = context;
        posStats = new PositionStats(context);
        recStats = context.getRecordStats();
        if (recStats == null) throw new IllegalStateException("no stats");
        if (recStats.getRecordCount() > Integer.MAX_VALUE) throw new UnsupportedOperationException("long record counts not currently supported.");
        List<ColumnType> types = context.getColumnTypes();
        if (types == null) throw new IllegalStateException("no types");
        file = context.file("positions", false, posStats.definition);
        if (context.isClean()) file.delete();
    }

    @Override
    public int getRequiredPasses() {
        return file.isFile() ? 0 : 1;
    }

    @Override
    public void beginPass() {
        context.setPassName("Building position index");
        int count = (int) recStats.getRecordCount();
        positions = new long[count];
    }

    @Override
    public void consume(LinearRecord record) {
        long position = record.getPosition();
        int ordinal = (int) record.getOrdinal();
        if (position < 0L) throw new IllegalArgumentException("record without position");
        if (ordinal < 0) throw new IllegalArgumentException("record without ordinal");
        positions[ordinal] = position;
    }

    @Override
    public void endPass() {
        posStats.bottomPosition = positions[0];
        posStats.topPosition = positions[positions.length - 1];
        calcOffsets(0, positions.length - 1);
        positions[positions.length - 1] = 0;
        chooseFixedBitSize();
        posStats.write();
        writeFile();
    }

    @Override
    public void complete() {
        positions = null;
    }

    @Override
    public void quit() {
        positions = null;
    }

    private void calcOffsets(int bottom, int top) {
        if (bottom == top || bottom + 1 == top) return;
        int index = (bottom + top) / 2;
        long pos = positions[index];
        long est = (positions[bottom] + positions[top]) / 2;
        calcOffsets(bottom, index);
        calcOffsets(index, top);
        positions[index] = pos - est;
    }

    private void chooseFixedBitSize() {
        long[] errCounts = new long[65];
        for (int i = 0; i < positions.length; i++) {
            long pos = positions[i];
            long err = Math.abs(pos);
            int bits = 65 - Long.numberOfLeadingZeros(err);
            errCounts[bits]++;
        }
        long[] sizes = new OverSizedMeasurer().sizeEntries();
        long bestSize = Long.MAX_VALUE;
        int bestFixed = 0;
        for (int fixed = 0; fixed < 65; fixed++) {
            long size = fixed * positions.length;
            for (int free = fixed + 1; free < 65; free++) {
                size += sizes[free];
            }
            if (size < bestSize) {
                bestSize = size;
                bestFixed = fixed;
            }
        }
        posStats.fixedBitSize = bestFixed;
        posStats.bitsWritten = bestSize;
        context.getLogger().log("Fixed bit size: " + bestFixed);
    }

    private void writeFile() {
        CodedStreams.writeToFile(new CodedStreams.WriteTask() {

            @Override
            public void writeTo(CodedWriter writer) {
                final BitWriter w = writer.getWriter();
                final long[] positions = PositionConsumer.this.positions;
                final int fixedBitSize = PositionConsumer.this.posStats.fixedBitSize;
                final long invalid = 1 << (fixedBitSize - 1);
                for (int i = 0; i < positions.length; i++) {
                    long err = positions[i];
                    int bits = 65 - Long.numberOfLeadingZeros(Math.abs(err));
                    long value = bits <= fixedBitSize ? err : invalid;
                    w.write(value, fixedBitSize);
                }
                long fixedSize = w.getPosition();
                new OversizedWriter(w).writeEntries();
                long totalSize = w.getPosition();
                long variableSize = totalSize - fixedSize;
                context.getLogger().log("Non-fixed percentage: " + String.format("%.2f", variableSize / (double) totalSize * 100.0));
            }
        }, posStats.coding, file);
    }

    private abstract class OversizedBase {

        private final long[] positions = PositionConsumer.this.positions;

        void writeEntries() {
            writeEntry(0, posStats.bottomPosition, 0L);
            writeEntries(0, posStats.bottomPosition, positions.length - 1, posStats.topPosition);
            writeEntry(0, posStats.topPosition, 0L);
        }

        private void writeEntries(int bottomOrdinal, long bottomPosition, int topOrdinal, long topPosition) {
            if (bottomOrdinal == topOrdinal || bottomOrdinal + 1 == topOrdinal) return;
            int index = (bottomOrdinal + topOrdinal) / 2;
            long err = positions[index];
            long est = (bottomPosition + topPosition) / 2;
            long pos = est + err;
            writeEntries(bottomOrdinal, bottomPosition, index, pos);
            writeEntry(index, pos, err);
            writeEntries(index, pos, topOrdinal, topPosition);
        }

        abstract void writeEntry(int ordinal, long position, long err);
    }

    private class OversizedWriter extends OversizedBase {

        private final CodedWriter coded;

        private final int fixedBitSize = posStats.fixedBitSize;

        OversizedWriter(BitWriter writer) {
            this.coded = new CodedWriter(writer, FibonacciCoding.extended);
        }

        void writeEntry(int ordinal, long position, long err) {
            int bits = 65 - Long.numberOfLeadingZeros(Math.abs(err));
            if (bits > fixedBitSize) PositionConsumer.writeEntry(coded, ordinal, position);
        }
    }

    private class OverSizedMeasurer extends OversizedBase {

        final CodedWriter[] coded = new CodedWriter[65];

        public OverSizedMeasurer() {
            ExtendedCoding coding = FibonacciCoding.extended;
            for (int i = 0; i < coded.length; i++) {
                coded[i] = new CodedWriter(new NullBitWriter(), coding);
            }
        }

        long[] sizeEntries() {
            writeEntries();
            long[] sizes = new long[coded.length];
            for (int i = 0; i < sizes.length; i++) {
                sizes[i] = coded[i].getWriter().getPosition();
            }
            return sizes;
        }

        @Override
        void writeEntry(int ordinal, long position, long err) {
            int bits = 65 - Long.numberOfLeadingZeros(Math.abs(err));
            PositionConsumer.writeEntry(coded[bits], ordinal, position);
        }
    }
}
