package speedyviewer.core;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import speedyviewer.util.ChunkIntArray;

public abstract class AbstractFileIndexer {

    protected int charCount = 0;

    protected Vector<IIndexerMonitor> listeners = new Vector<IIndexerMonitor>();

    protected ChunkIntArray index;

    public AbstractFileIndexer(int chunkSize) {
        super();
        index = new ChunkIntArray(chunkSize);
    }

    protected int getChunkSize() {
        return index.getChunksize();
    }

    protected boolean sendIndexChunk(int[] indexChunk, int len, int chunkCharCount) {
        boolean cancel = false;
        IIndexerMonitor[] listenerArray = listeners.toArray(new IIndexerMonitor[listeners.size()]);
        for (IIndexerMonitor listener : listenerArray) listener.addingIndexChunk(this, indexChunk, len, chunkCharCount);
        synchronized (index) {
            index.add(indexChunk, len);
            charCount += chunkCharCount;
        }
        for (IIndexerMonitor listener : listenerArray) listener.newIndexChunk(this);
        for (IIndexerMonitor listener : listenerArray) if (listener.isCanceled()) {
            cancel = true;
            break;
        }
        return cancel;
    }

    /**
	 * Clears the index.
	 * 
	 */
    public void clear() {
        synchronized (index) {
            index.clear();
            charCount = 0;
        }
    }

    public int getLineCount() {
        synchronized (index) {
            return index.size();
        }
    }

    public int getCharCount() {
        synchronized (index) {
            return charCount;
        }
    }

    public int getLineForOffset(int offset) {
        int line;
        int min;
        int max;
        int off1;
        int off2;
        synchronized (index) {
            if (charCount > 0 && offset >= charCount) return index.size() - 1;
            min = 0;
            max = index.size() - 1;
            if (max <= 0) return 0;
            do {
                line = min + (max - min) / 2;
                off1 = index.get(line);
                off2 = index.get(line + 1);
                if (offset >= off1 && offset < off2) max = min = line; else if (offset >= off2) min = line + 1; else max = line - 1;
            } while (max > min);
        }
        line = min + (max - min) / 2;
        return line;
    }

    public int getOffsetForLine(int line) {
        synchronized (index) {
            if (line < index.size()) return index.get(line); else if (line == index.size()) return charCount; else throw new ArrayIndexOutOfBoundsException();
        }
    }

    public void addListener(IIndexerMonitor listener) {
        if (listener != null) listeners.add(listener);
    }

    public abstract void index(File file) throws IOException;
}
