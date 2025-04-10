public class Test {    public DiskAccessControllerImpl(String _name, int _max_read_threads, int _max_read_mb, int _max_write_threads, int _max_write_mb) {
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
}