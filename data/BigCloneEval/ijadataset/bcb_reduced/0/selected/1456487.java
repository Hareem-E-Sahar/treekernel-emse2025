package com.aelitis.azureus.core.networkmanager.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.gudy.azureus2.core3.util.AEDiagnostics;
import org.gudy.azureus2.core3.util.Debug;
import org.gudy.azureus2.core3.util.SystemTime;
import org.gudy.azureus2.core3.util.TimeFormatter;
import com.aelitis.azureus.core.networkmanager.EventWaiter;
import com.aelitis.azureus.core.networkmanager.Transport;

public abstract class TransportImpl implements Transport {

    private TransportHelperFilter filter;

    private static final TransportStats stats = AEDiagnostics.TRACE_TCP_TRANSPORT_STATS ? new TransportStats() : null;

    private ByteBuffer data_already_read = null;

    private volatile EventWaiter read_waiter;

    private volatile EventWaiter write_waiter;

    private volatile boolean is_ready_for_write = false;

    private volatile boolean is_ready_for_read = false;

    private Throwable write_select_failure = null;

    private Throwable read_select_failure = null;

    private long last_ready_for_read = SystemTime.getSteppedMonotonousTime();

    private boolean trace;

    protected TransportImpl() {
    }

    public void setFilter(TransportHelperFilter _filter) {
        filter = _filter;
        if (trace && _filter != null) {
            _filter.setTrace(true);
        }
    }

    public TransportHelperFilter getFilter() {
        return (filter);
    }

    public void setAlreadyRead(ByteBuffer bytes_already_read) {
        if (bytes_already_read != null && bytes_already_read.hasRemaining()) {
            if (data_already_read != null) {
                ByteBuffer new_bb = ByteBuffer.allocate(data_already_read.remaining() + bytes_already_read.remaining());
                new_bb.put(bytes_already_read);
                new_bb.put(data_already_read);
                new_bb.position(0);
                data_already_read = new_bb;
            } else {
                data_already_read = bytes_already_read;
            }
            is_ready_for_read = true;
        }
    }

    public String getEncryption(boolean verbose) {
        return (filter == null ? "" : filter.getName(verbose));
    }

    public boolean isEncrypted() {
        return (filter == null ? false : filter.isEncrypted());
    }

    /**
	   * Is the transport ready to write,
	   * i.e. will a write request result in >0 bytes written.
	   * @return true if the transport is write ready, false if not yet ready
	   */
    public boolean isReadyForWrite(EventWaiter waiter) {
        if (waiter != null) {
            write_waiter = waiter;
        }
        return is_ready_for_write;
    }

    protected boolean readyForWrite(boolean ready) {
        if (trace) {
            TimeFormatter.milliTrace("trans: readyForWrite -> " + ready);
        }
        if (ready) {
            boolean progress = !is_ready_for_write;
            is_ready_for_write = true;
            EventWaiter ww = write_waiter;
            if (ww != null) {
                ww.eventOccurred();
            }
            return progress;
        } else {
            is_ready_for_write = false;
            return (false);
        }
    }

    protected void writeFailed(Throwable msg) {
        msg.fillInStackTrace();
        write_select_failure = msg;
        is_ready_for_write = true;
    }

    /**
	   * Is the transport ready to read,
	   * i.e. will a read request result in >0 bytes read.
	   * @return 0 if the transport is read ready, millis since last ready or -1 if never ready
	   */
    public long isReadyForRead(EventWaiter waiter) {
        if (waiter != null) {
            read_waiter = waiter;
        }
        boolean ready = is_ready_for_read || data_already_read != null || (filter != null && filter.hasBufferedRead());
        long now = SystemTime.getSteppedMonotonousTime();
        if (ready) {
            last_ready_for_read = now;
            return (0);
        }
        long diff = now - last_ready_for_read + 1;
        return (diff);
    }

    protected boolean readyForRead(boolean ready) {
        if (ready) {
            boolean progress = !is_ready_for_read;
            is_ready_for_read = true;
            EventWaiter rw = read_waiter;
            if (rw != null) {
                rw.eventOccurred();
            }
            return progress;
        } else {
            is_ready_for_read = false;
            return (false);
        }
    }

    public void setReadyForRead() {
        readyForRead(true);
    }

    protected void readFailed(Throwable msg) {
        msg.fillInStackTrace();
        read_select_failure = msg;
        is_ready_for_read = true;
    }

    /**
	   * Write data to the transport from the given buffers.
	   * NOTE: Works like GatheringByteChannel.
	   * @param buffers from which bytes are to be retrieved
	   * @param array_offset offset within the buffer array of the first buffer from which bytes are to be retrieved
	   * @param length maximum number of buffers to be accessed
	   * @return number of bytes written
	   * @throws IOException on write error
	   */
    public long write(ByteBuffer[] buffers, int array_offset, int length) throws IOException {
        if (write_select_failure != null) {
            throw new IOException("write_select_failure: " + write_select_failure.getMessage());
        }
        if (filter == null) return 0;
        long written = filter.write(buffers, array_offset, length);
        if (stats != null) stats.bytesWritten((int) written);
        if (written < 1) requestWriteSelect();
        return written;
    }

    /**
	   * Read data from the transport into the given buffers.
	   * NOTE: Works like ScatteringByteChannel.
	   * @param buffers into which bytes are to be placed
	   * @param array_offset offset within the buffer array of the first buffer into which bytes are to be placed
	   * @param length maximum number of buffers to be accessed
	   * @return number of bytes read
	   * @throws IOException on read error
	   */
    public long read(ByteBuffer[] buffers, int array_offset, int length) throws IOException {
        if (read_select_failure != null) {
            throw new IOException("read_select_failure: " + read_select_failure.getMessage());
        }
        if (data_already_read != null) {
            int inserted = 0;
            for (int i = array_offset; i < (array_offset + length); i++) {
                ByteBuffer bb = buffers[i];
                int orig_limit = data_already_read.limit();
                if (data_already_read.remaining() > bb.remaining()) {
                    data_already_read.limit(data_already_read.position() + bb.remaining());
                }
                inserted += data_already_read.remaining();
                bb.put(data_already_read);
                data_already_read.limit(orig_limit);
                if (!data_already_read.hasRemaining()) {
                    data_already_read = null;
                    break;
                }
            }
            if (!buffers[array_offset + length - 1].hasRemaining()) {
                return inserted;
            }
        }
        if (filter == null) {
            throw (new IOException("Transport not ready"));
        }
        long bytes_read = filter.read(buffers, array_offset, length);
        if (stats != null) stats.bytesRead((int) bytes_read);
        if (bytes_read == 0) {
            requestReadSelect();
        }
        return bytes_read;
    }

    private void requestWriteSelect() {
        is_ready_for_write = false;
        if (filter != null) {
            filter.getHelper().resumeWriteSelects();
        }
    }

    private void requestReadSelect() {
        is_ready_for_read = false;
        if (filter != null) {
            filter.getHelper().resumeReadSelects();
        }
    }

    public void connectedInbound() {
        registerSelectHandling();
    }

    public void connectedOutbound() {
        registerSelectHandling();
    }

    private void registerSelectHandling() {
        TransportHelperFilter filter = getFilter();
        if (filter == null) {
            Debug.out("ERROR: registerSelectHandling():: filter == null");
            return;
        }
        TransportHelper helper = filter.getHelper();
        helper.registerForReadSelects(new TransportHelper.selectListener() {

            public boolean selectSuccess(TransportHelper helper, Object attachment) {
                return (readyForRead(true));
            }

            public void selectFailure(TransportHelper helper, Object attachment, Throwable msg) {
                readFailed(msg);
            }
        }, null);
        helper.registerForWriteSelects(new TransportHelper.selectListener() {

            public boolean selectSuccess(TransportHelper helper, Object attachment) {
                return (readyForWrite(true));
            }

            public void selectFailure(TransportHelper helper, Object attachment, Throwable msg) {
                writeFailed(msg);
            }
        }, null);
    }

    public void setTrace(boolean on) {
        trace = on;
        TransportHelperFilter filter = getFilter();
        if (filter != null) {
            filter.setTrace(on);
        }
    }
}
