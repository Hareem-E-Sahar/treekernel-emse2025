package ti.plato.ui.views.internal.console;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitionerExtension;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.WorkbenchJob;
import ti.plato.ui.views.console.ConsolePlugin;
import ti.plato.ui.views.console.IConsoleDocumentPartitioner;
import ti.plato.ui.views.console.IOConsole;
import ti.plato.ui.views.console.IOConsoleInputStream;
import ti.plato.ui.views.console.IOConsoleOutputStream;

/**
 * Partitions an IOConsole's document
 * @since 3.1
 *
 */
public class IOConsolePartitioner implements IConsoleDocumentPartitioner, IDocumentPartitionerExtension {

    private PendingPartition consoleClosedPartition;

    private IDocument document;

    private ArrayList partitions;

    /**
	 * Blocks of data that have not yet been appended to the document.
	 */
    private ArrayList pendingPartitions;

    /**
	 * A list of PendingPartitions to be appended by the updateJob
	 */
    private ArrayList updatePartitions;

    /**
	 * The last partition appended to the document
	 */
    private IOConsolePartition lastPartition;

    /**
	 * Job that appends pending partitions to the document.
	 */
    private QueueProcessingJob queueJob;

    /**
	 * The input stream attached to this document.
	 */
    private IOConsoleInputStream inputStream;

    /**
	 * Flag to indicate that the updateJob is updating the document.
	 */
    private boolean updateInProgress;

    /**
	 * A list of partitions containing input from the console, that have
	 * not been appended to the input stream yet.
	 */
    private ArrayList inputPartitions;

    /**
	 * offset used by updateJob
	 */
    private int firstOffset;

    /**
	 * An array of legal line delimiters
	 */
    private String[] lld;

    private int highWaterMark = -1;

    private int lowWaterMark = -1;

    private boolean connected = false;

    private IOConsole console;

    private TrimJob trimJob = new TrimJob();

    /**
	 * Lock for appending to and removing from the document - used
	 * to synchronize addition of new text/partitions in the update
	 * job and handling buffer overflow/clearing of the console. 
	 */
    private Object overflowLock = new Object();

    private int fBuffer;

    public IOConsolePartitioner(IOConsoleInputStream inputStream, IOConsole console) {
        this.inputStream = inputStream;
        this.console = console;
        trimJob.setRule(console.getSchedulingRule());
    }

    public IDocument getDocument() {
        return document;
    }

    public void connect(IDocument doc) {
        document = doc;
        document.setDocumentPartitioner(this);
        lld = document.getLegalLineDelimiters();
        partitions = new ArrayList();
        pendingPartitions = new ArrayList();
        inputPartitions = new ArrayList();
        queueJob = new QueueProcessingJob();
        queueJob.setSystem(true);
        queueJob.setPriority(Job.INTERACTIVE);
        queueJob.setRule(console.getSchedulingRule());
        connected = true;
    }

    public int getHighWaterMark() {
        return highWaterMark;
    }

    public int getLowWaterMark() {
        return lowWaterMark;
    }

    public void setWaterMarks(int low, int high) {
        lowWaterMark = low;
        highWaterMark = high;
        ConsolePlugin.getStandardDisplay().asyncExec(new Runnable() {

            public void run() {
                checkBufferSize();
            }
        });
    }

    /**
	 * Notification from the console that all of its streams have been closed.
	 */
    public void streamsClosed() {
        consoleClosedPartition = new PendingPartition(null, null);
        synchronized (pendingPartitions) {
            pendingPartitions.add(consoleClosedPartition);
        }
        queueJob.schedule();
    }

    public void disconnect() {
        synchronized (overflowLock) {
            document = null;
            partitions.clear();
            connected = false;
            try {
                inputStream.close();
            } catch (IOException e) {
            }
        }
    }

    public void documentAboutToBeChanged(DocumentEvent event) {
    }

    public boolean documentChanged(DocumentEvent event) {
        return documentChanged2(event) != null;
    }

    public String[] getLegalContentTypes() {
        return new String[] { IOConsolePartition.OUTPUT_PARTITION_TYPE, IOConsolePartition.INPUT_PARTITION_TYPE };
    }

    public String getContentType(int offset) {
        return getPartition(offset).getType();
    }

    public ITypedRegion[] computePartitioning(int offset, int length) {
        int rangeEnd = offset + length;
        int left = 0;
        int right = partitions.size() - 1;
        int mid = 0;
        IOConsolePartition position = null;
        if (left == right) {
            return new IOConsolePartition[] { (IOConsolePartition) partitions.get(0) };
        }
        while (left < right) {
            mid = (left + right) / 2;
            position = (IOConsolePartition) partitions.get(mid);
            if (rangeEnd < position.getOffset()) {
                if (left == mid) right = left; else right = mid - 1;
            } else if (offset > (position.getOffset() + position.getLength() - 1)) {
                if (right == mid) left = right; else left = mid + 1;
            } else {
                left = right = mid;
            }
        }
        List list = new ArrayList();
        int index = left - 1;
        if (index >= 0) {
            position = (IOConsolePartition) partitions.get(index);
            while (index >= 0 && (position.getOffset() + position.getLength()) > offset) {
                index--;
                if (index >= 0) {
                    position = (IOConsolePartition) partitions.get(index);
                }
            }
        }
        index++;
        position = (IOConsolePartition) partitions.get(index);
        while (index < partitions.size() && (position.getOffset() < rangeEnd)) {
            list.add(position);
            index++;
            if (index < partitions.size()) {
                position = (IOConsolePartition) partitions.get(index);
            }
        }
        return (ITypedRegion[]) list.toArray(new IOConsolePartition[list.size()]);
    }

    public ITypedRegion getPartition(int offset) {
        for (int i = 0; i < partitions.size(); i++) {
            ITypedRegion partition = (ITypedRegion) partitions.get(i);
            int start = partition.getOffset();
            int end = start + partition.getLength();
            if (offset >= start && offset < end) {
                return partition;
            }
        }
        if (lastPartition == null) {
            synchronized (partitions) {
                lastPartition = new IOConsolePartition(inputStream, "");
                lastPartition.setOffset(offset);
                partitions.add(lastPartition);
                inputPartitions.add(lastPartition);
            }
        }
        return lastPartition;
    }

    /**
	 * Enforces the buffer size.
	 * When the number of lines in the document exceeds the high water mark, the 
	 * beginning of the document is trimmed until the number of lines equals the 
	 * low water mark.
	 */
    private void checkBufferSize() {
        if (document != null && highWaterMark > 0) {
            int length = document.getLength();
            if (length > highWaterMark) {
                if (trimJob.getState() == Job.NONE) {
                    trimJob.setOffset(length - lowWaterMark);
                    trimJob.schedule();
                }
            }
        }
    }

    /**
	 * Clears the console
	 */
    public void clearBuffer() {
        synchronized (overflowLock) {
            trimJob.setOffset(-1);
            trimJob.schedule();
        }
    }

    public IRegion documentChanged2(DocumentEvent event) {
        if (document == null) {
            return null;
        }
        if (document.getLength() == 0) {
            partitions.clear();
            inputPartitions.clear();
            pendingPartitions.clear();
            lastPartition = null;
            return new Region(0, 0);
        }
        if (updateInProgress) {
            synchronized (partitions) {
                if (updatePartitions != null) {
                    for (Iterator i = updatePartitions.iterator(); i.hasNext(); ) {
                        PendingPartition pp = (PendingPartition) i.next();
                        if (pp == consoleClosedPartition) {
                            continue;
                        }
                        int ppLen = pp.text.length();
                        if (lastPartition != null && lastPartition.getStream() == pp.stream) {
                            int len = lastPartition.getLength();
                            lastPartition.setLength(len + ppLen);
                        } else {
                            IOConsolePartition partition = new IOConsolePartition(pp.stream, ppLen);
                            partition.setOffset(firstOffset);
                            lastPartition = partition;
                            partitions.add(partition);
                        }
                        firstOffset += ppLen;
                    }
                }
            }
        } else {
            int amountDeleted = event.getLength();
            if (amountDeleted > 0) {
                int offset = event.fOffset;
                IOConsolePartition partition = (IOConsolePartition) getPartition(offset);
                if (partition == lastPartition) {
                    partition.delete(event.fOffset - partition.getOffset(), amountDeleted);
                }
            }
            synchronized (partitions) {
                if (lastPartition == null || lastPartition.isReadOnly()) {
                    lastPartition = new IOConsolePartition(inputStream, event.fText);
                    lastPartition.setOffset(event.fOffset);
                    partitions.add(lastPartition);
                    inputPartitions.add(lastPartition);
                } else {
                    lastPartition.insert(event.fText, (event.fOffset - lastPartition.getOffset()));
                }
                int lastLineDelimiter = -1;
                String partitionText = lastPartition.getString();
                for (int i = 0; i < lld.length; i++) {
                    String ld = lld[i];
                    int index = partitionText.lastIndexOf(ld);
                    if (index != -1) {
                        index += ld.length();
                    }
                    if (index > lastLineDelimiter) {
                        lastLineDelimiter = index;
                    }
                }
                if (lastLineDelimiter != -1) {
                    StringBuffer input = new StringBuffer();
                    Iterator it = inputPartitions.iterator();
                    while (it.hasNext()) {
                        IOConsolePartition partition = (IOConsolePartition) it.next();
                        if (partition.getOffset() + partition.getLength() <= event.fOffset + lastLineDelimiter) {
                            if (partition == lastPartition) {
                                lastPartition = null;
                            }
                            input.append(partition.getString());
                            partition.clearBuffer();
                            partition.setReadOnly();
                            it.remove();
                        } else {
                            String contentBefore = partitionText.substring(0, lastLineDelimiter);
                            IOConsolePartition newPartition = new IOConsolePartition(inputStream, contentBefore);
                            newPartition.setOffset(partition.getOffset());
                            newPartition.setReadOnly();
                            newPartition.clearBuffer();
                            int index = partitions.indexOf(partition);
                            partitions.add(index, newPartition);
                            input.append(contentBefore);
                            partition.delete(0, lastLineDelimiter);
                            partition.setOffset(lastLineDelimiter + partition.getOffset());
                            lastLineDelimiter = 0;
                        }
                    }
                    if (input.length() > 0) {
                        inputStream.appendData(input.toString());
                    }
                }
            }
        }
        return new Region(event.fOffset, event.fText.length());
    }

    private void setUpdateInProgress(boolean b) {
        updateInProgress = b;
    }

    /**
	 * A stream has been appended, add to pendingPartions list and schedule updateJob.
	 * updateJob is scheduled with a slight delay, this allows the console to run the job
	 * less frequently and update the document with a greater amount of data each time 
	 * the job is run
	 * @param stream The stream that was written to.
	 * @param s The string that should be appended to the document.
	 */
    public void streamAppended(IOConsoleOutputStream stream, String s) throws IOException {
        if (document == null) {
            throw new IOException("Document is closed");
        }
        synchronized (pendingPartitions) {
            PendingPartition last = (PendingPartition) (pendingPartitions.size() > 0 ? pendingPartitions.get(pendingPartitions.size() - 1) : null);
            if (last != null && last.stream == stream) {
                last.append(s);
            } else {
                pendingPartitions.add(new PendingPartition(stream, s));
                if (fBuffer > 1000) {
                    queueJob.schedule();
                } else {
                    queueJob.schedule(100);
                }
            }
            if (fBuffer > 160000) {
                try {
                    pendingPartitions.wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /**
	 * Holds data until updateJob can be run and the document can be updated.
	 */
    private class PendingPartition {

        StringBuffer text = new StringBuffer(8192);

        IOConsoleOutputStream stream;

        PendingPartition(IOConsoleOutputStream stream, String text) {
            this.stream = stream;
            if (text != null) {
                append(text);
            }
        }

        void append(String moreText) {
            text.append(moreText);
            fBuffer += moreText.length();
        }
    }

    /**
	 * Updates the document. Will append everything that is available before 
	 * finishing.
	 */
    private class QueueProcessingJob extends Job {

        QueueProcessingJob() {
            super("IOConsole Updater");
        }

        protected IStatus run(IProgressMonitor monitor) {
            synchronized (overflowLock) {
                Display display = ConsolePlugin.getStandardDisplay();
                ArrayList pendingCopy = new ArrayList();
                StringBuffer buffer = null;
                boolean consoleClosed = false;
                while (display != null && pendingPartitions.size() > 0) {
                    synchronized (pendingPartitions) {
                        pendingCopy.addAll(pendingPartitions);
                        pendingPartitions.clear();
                        fBuffer = 0;
                        pendingPartitions.notifyAll();
                    }
                    buffer = new StringBuffer();
                    for (Iterator i = pendingCopy.iterator(); i.hasNext(); ) {
                        PendingPartition pp = (PendingPartition) i.next();
                        if (pp != consoleClosedPartition) {
                            buffer.append(pp.text);
                        } else {
                            consoleClosed = true;
                        }
                    }
                }
                final ArrayList finalCopy = pendingCopy;
                final String toAppend = buffer.toString();
                final boolean notifyClosed = consoleClosed;
                display.asyncExec(new Runnable() {

                    public void run() {
                        if (connected) {
                            setUpdateInProgress(true);
                            updatePartitions = finalCopy;
                            firstOffset = document.getLength();
                            try {
                                document.replace(firstOffset, 0, toAppend.toString());
                            } catch (BadLocationException e) {
                            }
                            updatePartitions = null;
                            setUpdateInProgress(false);
                        }
                        if (notifyClosed) {
                            console.partitionerFinished();
                        }
                        checkBufferSize();
                    }
                });
            }
            return Status.OK_STATUS;
        }

        public boolean shouldRun() {
            boolean shouldRun = connected && pendingPartitions != null && pendingPartitions.size() > 0;
            return shouldRun;
        }
    }

    /**
     * Job to trim the console document, runs in the  UI thread.
     */
    private class TrimJob extends WorkbenchJob {

        /**
         * trims output up to the line containing the given offset,
         * or all output if -1.
         */
        private int truncateOffset;

        /**
         * Creates a new job to trim the buffer.
         */
        TrimJob() {
            super("Trim Job");
            setSystem(true);
        }

        /**
         * Sets the trim offset.
         * 
         * @param offset trims output up to the line containing the given offset
         */
        public void setOffset(int offset) {
            truncateOffset = offset;
        }

        public IStatus runInUIThread(IProgressMonitor monitor) {
            if (document == null) {
                return Status.OK_STATUS;
            }
            int length = document.getLength();
            if (truncateOffset < length) {
                synchronized (overflowLock) {
                    try {
                        if (truncateOffset < 0) {
                            setUpdateInProgress(true);
                            document.set("");
                            setUpdateInProgress(false);
                            partitions.clear();
                        } else {
                            int cutoffLine = document.getLineOfOffset(truncateOffset);
                            int cutOffset = document.getLineOffset(cutoffLine);
                            IOConsolePartition partition = (IOConsolePartition) getPartition(cutOffset);
                            partition.setLength(partition.getOffset() + partition.getLength() - cutOffset);
                            setUpdateInProgress(true);
                            document.replace(0, cutOffset, "");
                            setUpdateInProgress(false);
                            int index = partitions.indexOf(partition);
                            for (int i = 0; i < index; i++) {
                                partitions.remove(0);
                            }
                            int offset = 0;
                            for (Iterator i = partitions.iterator(); i.hasNext(); ) {
                                IOConsolePartition p = (IOConsolePartition) i.next();
                                p.setOffset(offset);
                                offset += p.getLength();
                            }
                        }
                    } catch (BadLocationException e) {
                    }
                }
            }
            return Status.OK_STATUS;
        }
    }

    public boolean isReadOnly(int offset) {
        return ((IOConsolePartition) getPartition(offset)).isReadOnly();
    }

    public StyleRange[] getStyleRanges(int offset, int length) {
        if (!connected) {
            return new StyleRange[0];
        }
        IOConsolePartition[] computedPartitions = (IOConsolePartition[]) computePartitioning(offset, length);
        StyleRange[] styles = new StyleRange[computedPartitions.length];
        for (int i = 0; i < computedPartitions.length; i++) {
            int rangeStart = Math.max(computedPartitions[i].getOffset(), offset);
            int rangeLength = computedPartitions[i].getLength();
            styles[i] = computedPartitions[i].getStyleRange(rangeStart, rangeLength);
        }
        return styles;
    }
}
