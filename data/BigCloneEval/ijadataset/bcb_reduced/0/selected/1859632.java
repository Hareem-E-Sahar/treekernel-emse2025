package com.aelitis.azureus.core.diskmanager.access.impl;

import java.util.*;
import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.util.DirectByteBuffer;
import com.aelitis.azureus.core.diskmanager.access.DiskAccessController;
import com.aelitis.azureus.core.diskmanager.access.DiskAccessControllerStats;
import com.aelitis.azureus.core.diskmanager.access.DiskAccessRequest;
import com.aelitis.azureus.core.diskmanager.access.DiskAccessRequestListener;
import com.aelitis.azureus.core.diskmanager.cache.CacheFile;
import com.aelitis.azureus.core.stats.AzureusCoreStats;
import com.aelitis.azureus.core.stats.AzureusCoreStatsProvider;

public class DiskAccessControllerImpl implements DiskAccessController, AzureusCoreStatsProvider {

    private DiskAccessControllerInstance read_dispatcher;

    private DiskAccessControllerInstance write_dispatcher;

    public DiskAccessControllerImpl(String _name, int _max_read_threads, int _max_read_mb, int _max_write_threads, int _max_write_mb) {
        boolean enable_read_aggregation = COConfigurationManager.getBooleanParameter("diskmanager.perf.read.aggregate.enable");
        int read_aggregation_request_limit = COConfigurationManager.getIntParameter("diskmanager.perf.read.aggregate.request.limit", 4);
        int read_aggregation_byte_limit = COConfigurationManager.getIntParameter("diskmanager.perf.read.aggregate.byte.limit", 64 * 1024);
        boolean enable_write_aggregation = COConfigurationManager.getBooleanParameter("diskmanager.perf.write.aggregate.enable");
        int write_aggregation_request_limit = COConfigurationManager.getIntParameter("diskmanager.perf.write.aggregate.request.limit", 8);
        int write_aggregation_byte_limit = COConfigurationManager.getIntParameter("diskmanager.perf.write.aggregate.byte.limit", 128 * 1024);
        read_dispatcher = new DiskAccessControllerInstance(_name + "/" + "read", enable_read_aggregation, read_aggregation_request_limit, read_aggregation_byte_limit, _max_read_threads, _max_read_mb);
        write_dispatcher = new DiskAccessControllerInstance(_name + "/" + "write", enable_write_aggregation, write_aggregation_request_limit, write_aggregation_byte_limit, _max_write_threads, _max_write_mb);
        Set types = new HashSet();
        types.add(AzureusCoreStats.ST_DISK_READ_QUEUE_LENGTH);
        types.add(AzureusCoreStats.ST_DISK_READ_QUEUE_BYTES);
        types.add(AzureusCoreStats.ST_DISK_READ_REQUEST_COUNT);
        types.add(AzureusCoreStats.ST_DISK_READ_REQUEST_SINGLE);
        types.add(AzureusCoreStats.ST_DISK_READ_REQUEST_MULTIPLE);
        types.add(AzureusCoreStats.ST_DISK_READ_REQUEST_BLOCKS);
        types.add(AzureusCoreStats.ST_DISK_READ_BYTES_TOTAL);
        types.add(AzureusCoreStats.ST_DISK_READ_BYTES_SINGLE);
        types.add(AzureusCoreStats.ST_DISK_READ_BYTES_MULTIPLE);
        types.add(AzureusCoreStats.ST_DISK_READ_IO_TIME);
        types.add(AzureusCoreStats.ST_DISK_READ_IO_COUNT);
        types.add(AzureusCoreStats.ST_DISK_WRITE_QUEUE_LENGTH);
        types.add(AzureusCoreStats.ST_DISK_WRITE_QUEUE_BYTES);
        types.add(AzureusCoreStats.ST_DISK_WRITE_REQUEST_COUNT);
        types.add(AzureusCoreStats.ST_DISK_WRITE_REQUEST_BLOCKS);
        types.add(AzureusCoreStats.ST_DISK_WRITE_BYTES_TOTAL);
        types.add(AzureusCoreStats.ST_DISK_WRITE_BYTES_SINGLE);
        types.add(AzureusCoreStats.ST_DISK_WRITE_BYTES_MULTIPLE);
        types.add(AzureusCoreStats.ST_DISK_WRITE_IO_TIME);
        AzureusCoreStats.registerProvider(types, this);
    }

    public void updateStats(Set types, Map values) {
        if (types.contains(AzureusCoreStats.ST_DISK_READ_QUEUE_LENGTH)) {
            values.put(AzureusCoreStats.ST_DISK_READ_QUEUE_LENGTH, new Long(read_dispatcher.getQueueSize()));
        }
        if (types.contains(AzureusCoreStats.ST_DISK_READ_QUEUE_BYTES)) {
            values.put(AzureusCoreStats.ST_DISK_READ_QUEUE_BYTES, new Long(read_dispatcher.getQueuedBytes()));
        }
        if (types.contains(AzureusCoreStats.ST_DISK_READ_REQUEST_COUNT)) {
            values.put(AzureusCoreStats.ST_DISK_READ_REQUEST_COUNT, new Long(read_dispatcher.getTotalRequests()));
        }
        if (types.contains(AzureusCoreStats.ST_DISK_READ_REQUEST_SINGLE)) {
            values.put(AzureusCoreStats.ST_DISK_READ_REQUEST_SINGLE, new Long(read_dispatcher.getTotalSingleRequests()));
        }
        if (types.contains(AzureusCoreStats.ST_DISK_READ_REQUEST_MULTIPLE)) {
            values.put(AzureusCoreStats.ST_DISK_READ_REQUEST_MULTIPLE, new Long(read_dispatcher.getTotalAggregatedRequests()));
        }
        if (types.contains(AzureusCoreStats.ST_DISK_READ_REQUEST_BLOCKS)) {
            values.put(AzureusCoreStats.ST_DISK_READ_REQUEST_BLOCKS, new Long(read_dispatcher.getBlockCount()));
        }
        if (types.contains(AzureusCoreStats.ST_DISK_READ_BYTES_TOTAL)) {
            values.put(AzureusCoreStats.ST_DISK_READ_BYTES_TOTAL, new Long(read_dispatcher.getTotalBytes()));
        }
        if (types.contains(AzureusCoreStats.ST_DISK_READ_BYTES_SINGLE)) {
            values.put(AzureusCoreStats.ST_DISK_READ_BYTES_SINGLE, new Long(read_dispatcher.getTotalSingleBytes()));
        }
        if (types.contains(AzureusCoreStats.ST_DISK_READ_BYTES_MULTIPLE)) {
            values.put(AzureusCoreStats.ST_DISK_READ_BYTES_MULTIPLE, new Long(read_dispatcher.getTotalAggregatedBytes()));
        }
        if (types.contains(AzureusCoreStats.ST_DISK_READ_IO_TIME)) {
            values.put(AzureusCoreStats.ST_DISK_READ_IO_TIME, new Long(read_dispatcher.getIOTime()));
        }
        if (types.contains(AzureusCoreStats.ST_DISK_READ_IO_COUNT)) {
            values.put(AzureusCoreStats.ST_DISK_READ_IO_COUNT, new Long(read_dispatcher.getIOCount()));
        }
        if (types.contains(AzureusCoreStats.ST_DISK_WRITE_QUEUE_LENGTH)) {
            values.put(AzureusCoreStats.ST_DISK_WRITE_QUEUE_LENGTH, new Long(write_dispatcher.getQueueSize()));
        }
        if (types.contains(AzureusCoreStats.ST_DISK_WRITE_QUEUE_BYTES)) {
            values.put(AzureusCoreStats.ST_DISK_WRITE_QUEUE_BYTES, new Long(write_dispatcher.getQueuedBytes()));
        }
        if (types.contains(AzureusCoreStats.ST_DISK_WRITE_REQUEST_COUNT)) {
            values.put(AzureusCoreStats.ST_DISK_WRITE_REQUEST_COUNT, new Long(write_dispatcher.getTotalRequests()));
        }
        if (types.contains(AzureusCoreStats.ST_DISK_WRITE_REQUEST_BLOCKS)) {
            values.put(AzureusCoreStats.ST_DISK_WRITE_REQUEST_BLOCKS, new Long(write_dispatcher.getBlockCount()));
        }
        if (types.contains(AzureusCoreStats.ST_DISK_WRITE_BYTES_TOTAL)) {
            values.put(AzureusCoreStats.ST_DISK_WRITE_BYTES_TOTAL, new Long(write_dispatcher.getTotalBytes()));
        }
        if (types.contains(AzureusCoreStats.ST_DISK_WRITE_BYTES_SINGLE)) {
            values.put(AzureusCoreStats.ST_DISK_WRITE_BYTES_SINGLE, new Long(write_dispatcher.getTotalSingleBytes()));
        }
        if (types.contains(AzureusCoreStats.ST_DISK_WRITE_BYTES_MULTIPLE)) {
            values.put(AzureusCoreStats.ST_DISK_WRITE_BYTES_MULTIPLE, new Long(write_dispatcher.getTotalAggregatedBytes()));
        }
        if (types.contains(AzureusCoreStats.ST_DISK_WRITE_IO_TIME)) {
            values.put(AzureusCoreStats.ST_DISK_WRITE_IO_TIME, new Long(write_dispatcher.getIOTime()));
        }
        if (types.contains(AzureusCoreStats.ST_DISK_WRITE_IO_COUNT)) {
            values.put(AzureusCoreStats.ST_DISK_WRITE_IO_COUNT, new Long(write_dispatcher.getIOCount()));
        }
    }

    public DiskAccessRequest queueReadRequest(CacheFile file, long offset, DirectByteBuffer buffer, short cache_policy, DiskAccessRequestListener listener) {
        DiskAccessRequestImpl request = new DiskAccessRequestImpl(file, offset, buffer, listener, DiskAccessRequestImpl.OP_READ, cache_policy);
        read_dispatcher.queueRequest(request);
        return (request);
    }

    public DiskAccessRequest queueWriteRequest(CacheFile file, long offset, DirectByteBuffer buffer, boolean free_buffer, DiskAccessRequestListener listener) {
        DiskAccessRequestImpl request = new DiskAccessRequestImpl(file, offset, buffer, listener, free_buffer ? DiskAccessRequestImpl.OP_WRITE_AND_FREE : DiskAccessRequestImpl.OP_WRITE, CacheFile.CP_NONE);
        write_dispatcher.queueRequest(request);
        return (request);
    }

    public DiskAccessControllerStats getStats() {
        return (new DiskAccessControllerStats() {

            long read_total_req = read_dispatcher.getTotalRequests();

            long read_total_bytes = read_dispatcher.getTotalBytes();

            public long getTotalReadRequests() {
                return (read_total_req);
            }

            public long getTotalReadBytes() {
                return (read_total_bytes);
            }
        });
    }

    public String getString() {
        return ("read: " + read_dispatcher.getString() + ", write: " + write_dispatcher.getString());
    }
}
