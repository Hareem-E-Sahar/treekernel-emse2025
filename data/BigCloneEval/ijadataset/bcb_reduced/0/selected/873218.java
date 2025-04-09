package edu.uga.dawgpack.aggregate;

import java.awt.font.NumericShaper.Range;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoServiceConfig;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.codec.serialization.ObjectSerializationDecoder;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.apache.mina.transport.socket.nio.SocketConnectorConfig;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import cern.colt.list.IntArrayList;
import cern.colt.list.LongArrayList;
import cern.colt.list.ObjectArrayList;
import cern.colt.map.OpenLongObjectHashMap;
import edu.uga.dawgpack.allvalid.align.Index;
import edu.uga.dawgpack.common.AlignMode;
import edu.uga.dawgpack.common.Constants;
import edu.uga.dawgpack.common.GenomeMetadata;
import edu.uga.dawgpack.common.Utils;
import edu.uga.dawgpack.index.util.HugeLongArray;
import edu.uga.dawgpack.singlematch.align.AlignerMessage;

/**
 * @author Juber Ahamad Patel
 * 
 */
public class Aggregator {

    private long maxDistance;

    private long minDistance;

    private File densityFile;

    private String[] alignerIPs;

    private int alignerPort;

    private GenomeMetadata metadata;

    private HugeLongArray sa;

    private HugeLongArray densities;

    private int readLength;

    private SocketConnector[] clients;

    private final int DISTRIBUTION_THREADS = 4;

    private final int UPDATER_THREADS = 8;

    private Thread[] updaterThreads;

    private Thread[] distributionThreads;

    private BlockingQueue<long[]>[] updateQueues;

    private BlockingQueue<long[]> distributionQueue;

    private long regionLength;

    private boolean aggregatorTerminated;

    private AlignMode alignMode;

    private LongArrayList startingPositions;

    private ExecutorService clientExecutor;

    private AtomicLong totalPairs;

    private AtomicInteger[] insertSizes;

    public Aggregator(Document config) throws XPathExpressionException, FileNotFoundException, IOException {
        System.out.println(new Date() + " new aggregator");
        XPath xPath = XPathFactory.newInstance().newXPath();
        String BWTFile = xPath.evaluate("dawgPackAnalyze/input/@BWTFile", config);
        String SAFile = xPath.evaluate("dawgPackAnalyze/input/@SAFile", config);
        String minDistance = xPath.evaluate("dawgPackAnalyze/input/@minDistance", config);
        String maxDistnace = xPath.evaluate("dawgPackAnalyze/input/@maxDistance", config);
        String densityFile = xPath.evaluate("dawgPackAnalyze/output/@densityFile", config);
        String mode = xPath.evaluate("dawgPackAnalyze/aggregate/@mode", config);
        String alignerPort = xPath.evaluate("dawgPackAnalyze/network/aligners/@port", config);
        NodeList nodes = (NodeList) xPath.evaluate("dawgPackAnalyze/network/aligners/aligner/@ip", config, XPathConstants.NODESET);
        alignerIPs = new String[nodes.getLength()];
        for (int i = 0; i < alignerIPs.length; i++) {
            alignerIPs[i] = nodes.item(i).getTextContent();
        }
        this.densityFile = new File(densityFile);
        this.alignerPort = Integer.parseInt(alignerPort);
        this.minDistance = Long.parseLong(minDistance);
        this.maxDistance = Long.parseLong(maxDistnace);
        System.out.println(new Date() + " minDistance = " + minDistance + " maxDistance = " + maxDistnace);
        makeBigArrays(new File(BWTFile), new File(SAFile));
        alignMode = AlignMode.valueOf(mode);
        totalPairs = new AtomicLong();
        insertSizes = new AtomicInteger[600];
        for (int j = 0; j < insertSizes.length; j++) {
            insertSizes[j] = new AtomicInteger();
        }
        startUpdaterThreads();
    }

    private void makeBigArrays(File BWTFile, File SAFile) throws FileNotFoundException, IOException {
        ObjectInputStream BWTIn = new ObjectInputStream(new FileInputStream(BWTFile));
        ObjectInputStream SAIn = new ObjectInputStream(new FileInputStream(SAFile));
        System.gc();
        Index index = new Index(readLength, 0, 0, 16, 0);
        index.readBWT(BWTIn);
        index.readSA(SAIn);
        BWTIn.close();
        BWTIn = null;
        SAIn.close();
        SAIn = null;
        sa = index.getSA();
        metadata = index.getMetadata();
        densities = new HugeLongArray(sa.size);
        regionLength = densities.size / UPDATER_THREADS;
        index = null;
        System.gc();
    }

    /**
	 * aggregate the paired end ranges with the densities
	 * 
	 * @param ranges
	 * @throws InterruptedException
	 */
    public AggregatorMessage aggregate(long batchNumber, long[][] alignments, boolean isLastStage) throws InterruptedException {
        long updateStart;
        long updateEnd;
        int k;
        IntArrayList repeatIndexes = new IntArrayList();
        LongArrayList updateRanges = new LongArrayList(200);
        long[] ranges;
        for (k = 0; k < alignments.length; k++) {
            ranges = alignments[k];
            if (ranges == null || ranges.length == 0) {
                continue;
            } else {
                long start;
                long end;
                int[] sizes = getSizes(ranges);
                long[] forwardMatches = new long[sizes[0]];
                long[] reverseMatches = new long[sizes[1]];
                int counter = 0;
                long j;
                int i;
                for (i = 0; ranges[i] != Long.MAX_VALUE; i = i + 2) {
                    start = ranges[i];
                    end = ranges[i + 1];
                    for (j = start; j <= end; j++) {
                        forwardMatches[counter] = sa.get(j);
                        counter++;
                    }
                }
                counter = 0;
                for (i = i + 1; i < ranges.length; i = i + 2) {
                    start = ranges[i];
                    end = ranges[i + 1];
                    for (j = start; j <= end; j++) {
                        reverseMatches[counter] = sa.get(j);
                        counter++;
                    }
                }
                Arrays.sort(reverseMatches);
                for (i = 0; i < forwardMatches.length; i++) {
                    updateStart = forwardMatches[i];
                    updateEnd = search(reverseMatches, updateStart);
                    if (updateEnd != Constants.INVALID) {
                        updateRanges.add(updateStart);
                        updateRanges.add(updateEnd);
                        break;
                    }
                }
                if (i == forwardMatches.length && !isLastStage) {
                    repeatIndexes.add(k);
                }
            }
        }
        if (distributionQueue.remainingCapacity() == 0) {
            System.out.println(new Date() + " distribution queue is full");
        }
        if (updateRanges.size() != 0) {
            distributionQueue.put(Arrays.copyOf(updateRanges.elements(), updateRanges.size()));
            totalPairs.addAndGet(updateRanges.size() / 2);
        }
        if (isLastStage) {
            return null;
        }
        int[] indexes = new int[repeatIndexes.size()];
        if (repeatIndexes.size() > 0) {
            System.arraycopy(repeatIndexes.elements(), 0, indexes, 0, indexes.length);
        }
        return new AggregatorMessage(batchNumber, indexes);
    }

    /**
	 * get the required sizes for forward and backward match arrays
	 * 
	 * @param ranges
	 * @return
	 */
    private int[] getSizes(long[] ranges) {
        int[] sizes = new int[2];
        int size = 0;
        int j;
        for (j = 0; ranges[j] != Long.MAX_VALUE; j = j + 2) {
            if (ranges[j] == Constants.INVALID) continue;
            size += (ranges[j + 1] - ranges[j] + 1);
        }
        sizes[0] = size;
        size = 0;
        for (j = j + 1; j < ranges.length; j = j + 2) {
            if (ranges[j] == Constants.INVALID) continue;
            size += (ranges[j + 1] - ranges[j] + 1);
        }
        sizes[1] = size;
        return sizes;
    }

    /**
	 * search for the pair of the value at index in the sorted array
	 * 
	 * @param matches
	 * @param separator
	 * @return
	 */
    private long search(long[] matches, long value) {
        int left = 0;
        int right = matches.length - 1;
        int middle = right / 2;
        long inserSize;
        while (left <= right) {
            inserSize = matches[middle] + readLength - value;
            if (inserSize >= minDistance && inserSize <= maxDistance) {
                insertSizes[(int) inserSize].incrementAndGet();
                return matches[middle] + readLength - 1;
            }
            if (inserSize < minDistance) {
                left = middle + 1;
            } else {
                right = middle - 1;
            }
            middle = (left + right) / 2;
        }
        return Constants.INVALID;
    }

    /**
	 * search for the pair of the value at index in the sorted array
	 * 
	 * @param matches
	 * @param separator
	 * @return
	 */
    private long searchSmallest(long[] matches, long value) {
        long distance;
        long minDistance = Long.MAX_VALUE;
        long minPos = Constants.INVALID - readLength;
        System.out.print("choices are ");
        for (long pos : matches) {
            distance = pos + readLength - value;
            if (distance <= 0) continue;
            if (distance < 5000) {
                System.out.print(distance + "  ");
            }
            if (distance < minDistance) {
                minDistance = distance;
                minPos = pos;
            }
        }
        System.out.println();
        return minPos + readLength;
    }

    public void save() throws IOException {
        long totalPlaces = 0;
        FileWriter writer = new FileWriter("insertSizes.txt");
        for (int i = 0; i < insertSizes.length; i++) {
            writer.write(i + "\t" + insertSizes[i] + "\n");
            totalPlaces = totalPlaces + (i * insertSizes[i].get());
        }
        writer.close();
        System.out.println(new Date() + " total pairs " + totalPairs + " total places " + totalPlaces);
        System.out.println(new Date() + " saving density arrays to " + densityFile);
        FileOutputStream fileOut = new FileOutputStream(densityFile);
        GZIPOutputStream gzip = new GZIPOutputStream(fileOut, 1024 * 1024 * 100);
        BufferedOutputStream buff = new BufferedOutputStream(gzip, 1024 * 1024 * 100);
        ObjectOutputStream out = new ObjectOutputStream(buff);
        out.writeObject(metadata);
        densities.write(out);
        out.flush();
        buff.flush();
        gzip.flush();
        fileOut.flush();
        out.close();
        buff.close();
        gzip.close();
        fileOut.close();
        System.out.println(new Date() + " done");
    }

    private void startUpdaterThreads() {
        final int size = 1500;
        distributionQueue = new ArrayBlockingQueue<long[]>(size * UPDATER_THREADS * 2);
        updateQueues = new ArrayBlockingQueue[UPDATER_THREADS];
        distributionThreads = new Thread[DISTRIBUTION_THREADS];
        for (int i = 0; i < distributionThreads.length; i++) {
            distributionThreads[i] = new DistributionThread(distributionQueue, updateQueues, regionLength);
            distributionThreads[i].setName(Integer.toString(i));
            distributionThreads[i].start();
        }
        updaterThreads = new Thread[UPDATER_THREADS];
        for (int i = 0; i < updateQueues.length; i++) {
            updateQueues[i] = new ArrayBlockingQueue<long[]>(size);
            final BlockingQueue<long[]> queue = updateQueues[i];
            updaterThreads[i] = new Thread(new Runnable() {

                @Override
                public void run() {
                    long[] updateRanges = null;
                    try {
                        while (true) {
                            updateRanges = queue.take();
                            if (updateRanges.length == 0) {
                                System.out.println(new Date() + " updater thread " + Thread.currentThread().getName() + " received stop signal, returning");
                                return;
                            }
                            for (int i = 0; i < updateRanges.length; i = i + 2) {
                                densities.increment(updateRanges[i], updateRanges[i + 1]);
                            }
                        }
                    } catch (Throwable e) {
                        System.out.println(new Date() + " Probleeeeeeem in updater thread ");
                        e.printStackTrace();
                    }
                }
            });
            updaterThreads[i].setName(Integer.toString(i));
            updaterThreads[i].start();
        }
    }

    private void start() throws UnknownHostException {
        clientExecutor = Executors.newFixedThreadPool(52);
        ByteBuffer.setUseDirectBuffers(false);
        ByteBuffer.setAllocator(new SimpleByteBufferAllocator());
        clients = new SocketConnector[alignerIPs.length];
        System.out.println(new Date() + " DEBUUUUUUUG: connecting to aligners...");
        for (int i = 0; i < alignerIPs.length; i++) {
            clients[i] = new SocketConnector(4, clientExecutor);
            IoServiceConfig config = clients[i].getDefaultConfig();
            config.setThreadModel(ThreadModel.MANUAL);
            ObjectSerializationCodecFactory factory = new ObjectSerializationCodecFactory();
            ((ObjectSerializationDecoder) factory.getDecoder()).setMaxObjectSize(Integer.MAX_VALUE);
            SocketConnectorConfig connectorConfig = new SocketConnectorConfig();
            connectorConfig.getSessionConfig().setReceiveBufferSize(Constants.NETWORK_BUFFER_SIZE);
            connectorConfig.getFilterChain().addLast("codec", new ProtocolCodecFilter(factory));
            InetSocketAddress address = new InetSocketAddress(Inet4Address.getByName(alignerIPs[i]), alignerPort);
            clients[i].connect(address, new AlignerConnectionHandler(this), connectorConfig);
        }
        System.out.println(new Date() + " DEBUUUUUUUG: done");
    }

    public synchronized void doneWithAligner() throws IOException, InterruptedException {
        if (aggregatorTerminated) return;
        boolean allDone = true;
        for (SocketConnector client : clients) {
            Set<SocketAddress> addresses = client.getManagedServiceAddresses();
            for (SocketAddress address : addresses) {
                for (IoSession session : client.getManagedSessions(address)) {
                    if (!session.isClosing()) {
                        allDone = false;
                        break;
                    }
                }
            }
        }
        if (!allDone) return;
        System.out.println(new Date() + " done with all aligners");
        sa = null;
        distributionQueue.put(new long[0]);
        for (int i = 0; i < DISTRIBUTION_THREADS; i++) {
            distributionThreads[i].join();
        }
        for (int i = 0; i < UPDATER_THREADS; i++) {
            updateQueues[i].put(new long[0]);
        }
        for (int i = 0; i < UPDATER_THREADS; i++) {
            updaterThreads[i].join();
        }
        System.out.println(new Date() + " all threads have finished updating");
        System.gc();
        save();
        Utils.awaitTermination(clientExecutor);
        aggregatorTerminated = true;
    }

    /**
	 * @param args
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * @throws XPathExpressionException
	 */
    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document config = builder.parse(new File(args[0]));
        Aggregator aggregator = new Aggregator(config);
        aggregator.start();
    }
}
