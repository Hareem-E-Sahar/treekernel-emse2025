package com.yahoo.zookeeper.server;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.Logger;
import com.yahoo.jute.BinaryOutputArchive;
import com.yahoo.jute.Record;
import com.yahoo.zookeeper.server.util.Profiler;
import com.yahoo.zookeeper.txn.TxnHeader;

/**
 * This RequestProcessor logs requests to disk. It batches the requests to do
 * the io efficiently. The request is not passed to the next RequestProcessor
 * until its log has been synced to disk.
 */
public class SyncRequestProcessor extends Thread implements RequestProcessor {

    private static final Logger LOG = Logger.getLogger(SyncRequestProcessor.class);

    static final int PADDING_TIMEOUT = 1000;

    ZooKeeperServer zks;

    LinkedBlockingQueue<Request> queuedRequests = new LinkedBlockingQueue<Request>();

    static boolean forceSync;

    static {
        forceSync = !System.getProperty("zookeeper.forceSync", "yes").equals("no");
    }

    static long preAllocSize = 65536 * 1024;

    static {
        String size = System.getProperty("zookeeper.preAllocSize");
        if (size != null) {
            try {
                preAllocSize = Long.parseLong(size) * 1024;
            } catch (NumberFormatException e) {
                LOG.warn(size + " is not a valid value for preAllocSize");
            }
        }
    }

    /**
     * The number of log entries to log before starting a snapshot
     */
    public static int snapCount = ZooKeeperServer.getSnapCount();

    Thread snapInProcess;

    RequestProcessor nextProcessor;

    boolean timeToDie = false;

    public SyncRequestProcessor(ZooKeeperServer zks, RequestProcessor nextProcessor) {
        super("SyncThread");
        this.zks = zks;
        this.nextProcessor = nextProcessor;
        start();
    }

    /**
     * Transactions that have been written and are waiting to be flushed to
     * disk. Basically this is the list of SyncItems whose callbacks will be
     * invoked after flush returns successfully.
     */
    LinkedList<Request> toFlush = new LinkedList<Request>();

    FileOutputStream logStream;

    BinaryOutputArchive logArchive;

    Random r = new Random(System.nanoTime());

    int logCount = 0;

    Request requestOfDeath = Request.requestOfDeath;

    private static ByteBuffer fill = ByteBuffer.allocateDirect(1024);

    LinkedList<FileOutputStream> streamsToFlush = new LinkedList<FileOutputStream>();

    private long padLogFile(FileChannel fc, long fileSize) throws IOException {
        long position = fc.position();
        if (position + 4096 >= fileSize) {
            fileSize = fileSize + preAllocSize;
            fill.position(0);
            fc.write(fill, fileSize);
        }
        return fileSize;
    }

    public void run() {
        try {
            long fileSize = 0;
            long lastZxidSeen = -1;
            FileChannel fc = null;
            while (true) {
                Request si = null;
                if (toFlush.isEmpty()) {
                    si = queuedRequests.take();
                } else {
                    si = queuedRequests.poll();
                    if (si == null) {
                        flush(toFlush);
                        continue;
                    }
                }
                if (si == requestOfDeath) {
                    break;
                }
                if (si != null) {
                    ZooTrace.logRequest(LOG, ZooTrace.CLIENT_REQUEST_TRACE_MASK, 'S', si, "");
                    TxnHeader hdr = si.hdr;
                    if (hdr != null) {
                        if (hdr.getZxid() <= lastZxidSeen) {
                            LOG.warn("Current zxid " + hdr.getZxid() + " is <= " + lastZxidSeen + " for " + hdr.getType());
                        }
                        Record txn = si.txn;
                        if (logStream == null) {
                            fileSize = 0;
                            logStream = new FileOutputStream(new File(zks.dataLogDir, ZooKeeperServer.getLogName(hdr.getZxid())));
                            synchronized (streamsToFlush) {
                                streamsToFlush.add(logStream);
                            }
                            fc = logStream.getChannel();
                            logArchive = BinaryOutputArchive.getArchive(logStream);
                        }
                        final long fsize = fileSize;
                        final FileChannel ffc = fc;
                        fileSize = Profiler.profile(new Profiler.Operation<Long>() {

                            public Long execute() throws Exception {
                                return SyncRequestProcessor.this.padLogFile(ffc, fsize);
                            }
                        }, PADDING_TIMEOUT, "Logfile padding exceeded time threshold");
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        BinaryOutputArchive boa = BinaryOutputArchive.getArchive(baos);
                        hdr.serialize(boa, "hdr");
                        if (txn != null) {
                            txn.serialize(boa, "txn");
                        }
                        logArchive.writeBuffer(baos.toByteArray(), "txnEntry");
                        logArchive.writeByte((byte) 0x42, "EOR");
                        logCount++;
                        if (logCount > snapCount / 2 && r.nextInt(snapCount / 2) == 0) {
                            if (snapInProcess != null && snapInProcess.isAlive()) {
                                LOG.warn("Too busy to snap, skipping");
                            } else {
                                logStream = null;
                                logArchive = null;
                                snapInProcess = new Thread() {

                                    public void run() {
                                        try {
                                            zks.snapshot();
                                        } catch (Exception e) {
                                            LOG.warn("Unexpected exception", e);
                                        }
                                    }
                                };
                                snapInProcess.start();
                            }
                            logCount = 0;
                        }
                    }
                    toFlush.add(si);
                    if (toFlush.size() > 1000) {
                        flush(toFlush);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Severe error, exiting", e);
            System.exit(11);
        }
        ZooTrace.logTraceMessage(LOG, ZooTrace.getTextTraceLevel(), "SyncRequestProcessor exiyed!");
    }

    private void flush(LinkedList<Request> toFlush) throws IOException {
        if (toFlush.size() == 0) {
            return;
        }
        LinkedList<FileOutputStream> streamsToFlushNow;
        synchronized (streamsToFlush) {
            streamsToFlushNow = (LinkedList<FileOutputStream>) streamsToFlush.clone();
        }
        for (FileOutputStream fos : streamsToFlushNow) {
            fos.flush();
            if (forceSync) {
                ((FileChannel) fos.getChannel()).force(false);
            }
        }
        while (streamsToFlushNow.size() > 1) {
            FileOutputStream fos = streamsToFlushNow.removeFirst();
            fos.close();
            synchronized (streamsToFlush) {
                streamsToFlush.remove(fos);
            }
        }
        while (toFlush.size() > 0) {
            Request i = toFlush.remove();
            nextProcessor.processRequest(i);
        }
    }

    public void shutdown() {
        timeToDie = true;
        queuedRequests.add(requestOfDeath);
        nextProcessor.shutdown();
    }

    public void processRequest(Request request) {
        queuedRequests.add(request);
    }
}
