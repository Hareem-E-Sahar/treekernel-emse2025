package org.apache.zookeeper.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.management.MBeanServerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.PortAssignment;
import org.apache.zookeeper.TestableZooKeeper;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZKTestCase;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.ServerCnxnFactoryAccessor;
import org.apache.zookeeper.server.ZKDatabase;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.apache.zookeeper.server.persistence.FileTxnLog;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import com.sun.management.UnixOperatingSystemMXBean;

public abstract class ClientBase extends ZKTestCase {

    protected static final Logger LOG = LoggerFactory.getLogger(ClientBase.class);

    public static int CONNECTION_TIMEOUT = 30000;

    static final File BASETEST = new File(System.getProperty("build.test.dir", "build"));

    protected String hostPort = "127.0.0.1:" + PortAssignment.unique();

    protected int maxCnxns = 0;

    protected ServerCnxnFactory serverFactory = null;

    protected File tmpDir = null;

    long initialFdCount;

    public ClientBase() {
        super();
    }

    /**
     * In general don't use this. Only use in the special case that you
     * want to ignore results (for whatever reason) in your test. Don't
     * use empty watchers in real code!
     *
     */
    protected class NullWatcher implements Watcher {

        public void process(WatchedEvent event) {
        }
    }

    protected static class CountdownWatcher implements Watcher {

        volatile CountDownLatch clientConnected;

        volatile boolean connected;

        public CountdownWatcher() {
            reset();
        }

        public synchronized void reset() {
            clientConnected = new CountDownLatch(1);
            connected = false;
        }

        public synchronized void process(WatchedEvent event) {
            if (event.getState() == KeeperState.SyncConnected || event.getState() == KeeperState.ConnectedReadOnly) {
                connected = true;
                notifyAll();
                clientConnected.countDown();
            } else {
                connected = false;
                notifyAll();
            }
        }

        synchronized boolean isConnected() {
            return connected;
        }

        synchronized void waitForConnected(long timeout) throws InterruptedException, TimeoutException {
            long expire = System.currentTimeMillis() + timeout;
            long left = timeout;
            while (!connected && left > 0) {
                wait(left);
                left = expire - System.currentTimeMillis();
            }
            if (!connected) {
                throw new TimeoutException("Did not connect");
            }
        }

        synchronized void waitForDisconnected(long timeout) throws InterruptedException, TimeoutException {
            long expire = System.currentTimeMillis() + timeout;
            long left = timeout;
            while (connected && left > 0) {
                wait(left);
                left = expire - System.currentTimeMillis();
            }
            if (connected) {
                throw new TimeoutException("Did not disconnect");
            }
        }
    }

    protected TestableZooKeeper createClient() throws IOException, InterruptedException {
        return createClient(hostPort);
    }

    protected TestableZooKeeper createClient(String hp) throws IOException, InterruptedException {
        CountdownWatcher watcher = new CountdownWatcher();
        return createClient(watcher, hp);
    }

    private LinkedList<ZooKeeper> allClients;

    private boolean allClientsSetup = false;

    protected TestableZooKeeper createClient(CountdownWatcher watcher, String hp) throws IOException, InterruptedException {
        return createClient(watcher, hp, CONNECTION_TIMEOUT);
    }

    protected TestableZooKeeper createClient(CountdownWatcher watcher, String hp, int timeout) throws IOException, InterruptedException {
        watcher.reset();
        TestableZooKeeper zk = new TestableZooKeeper(hp, timeout, watcher);
        if (!watcher.clientConnected.await(timeout, TimeUnit.MILLISECONDS)) {
            Assert.fail("Unable to connect to server");
        }
        synchronized (this) {
            if (!allClientsSetup) {
                LOG.error("allClients never setup");
                Assert.fail("allClients never setup");
            }
            if (allClients != null) {
                allClients.add(zk);
            } else {
                zk.close();
            }
        }
        JMXEnv.ensureAll("0x" + Long.toHexString(zk.getSessionId()));
        return zk;
    }

    public static class HostPort {

        String host;

        int port;

        public HostPort(String host, int port) {
            this.host = host;
            this.port = port;
        }
    }

    public static List<HostPort> parseHostPortList(String hplist) {
        ArrayList<HostPort> alist = new ArrayList<HostPort>();
        for (String hp : hplist.split(",")) {
            int idx = hp.lastIndexOf(':');
            String host = hp.substring(0, idx);
            int port;
            try {
                port = Integer.parseInt(hp.substring(idx + 1));
            } catch (RuntimeException e) {
                throw new RuntimeException("Problem parsing " + hp + e.toString());
            }
            alist.add(new HostPort(host, port));
        }
        return alist;
    }

    /**
     * Send the 4letterword
     * @param host the destination host
     * @param port the destination port
     * @param cmd the 4letterword
     * @return
     * @throws IOException
     */
    public static String send4LetterWord(String host, int port, String cmd) throws IOException {
        LOG.info("connecting to " + host + " " + port);
        Socket sock = new Socket(host, port);
        BufferedReader reader = null;
        try {
            OutputStream outstream = sock.getOutputStream();
            outstream.write(cmd.getBytes());
            outstream.flush();
            sock.shutdownOutput();
            reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            return sb.toString();
        } finally {
            sock.close();
            if (reader != null) {
                reader.close();
            }
        }
    }

    public static boolean waitForServerUp(String hp, long timeout) {
        long start = System.currentTimeMillis();
        while (true) {
            try {
                HostPort hpobj = parseHostPortList(hp).get(0);
                String result = send4LetterWord(hpobj.host, hpobj.port, "stat");
                if (result.startsWith("Zookeeper version:") && !result.contains("READ-ONLY")) {
                    return true;
                }
            } catch (IOException e) {
                LOG.info("server " + hp + " not up " + e);
            }
            if (System.currentTimeMillis() > start + timeout) {
                break;
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
            }
        }
        return false;
    }

    public static boolean waitForServerDown(String hp, long timeout) {
        long start = System.currentTimeMillis();
        while (true) {
            try {
                HostPort hpobj = parseHostPortList(hp).get(0);
                send4LetterWord(hpobj.host, hpobj.port, "stat");
            } catch (IOException e) {
                return true;
            }
            if (System.currentTimeMillis() > start + timeout) {
                break;
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
            }
        }
        return false;
    }

    static void verifyThreadTerminated(Thread thread, long millis) throws InterruptedException {
        thread.join(millis);
        if (thread.isAlive()) {
            LOG.error("Thread " + thread.getName() + " : " + Arrays.toString(thread.getStackTrace()));
            Assert.assertFalse("thread " + thread.getName() + " still alive after join", true);
        }
    }

    public static File createTmpDir() throws IOException {
        return createTmpDir(BASETEST);
    }

    static File createTmpDir(File parentDir) throws IOException {
        File tmpFile = File.createTempFile("test", ".junit", parentDir);
        File tmpDir = new File(tmpFile + ".dir");
        Assert.assertFalse(tmpDir.exists());
        Assert.assertTrue(tmpDir.mkdirs());
        return tmpDir;
    }

    private static int getPort(String hostPort) {
        String[] split = hostPort.split(":");
        String portstr = split[split.length - 1];
        String[] pc = portstr.split("/");
        if (pc.length > 1) {
            portstr = pc[0];
        }
        return Integer.parseInt(portstr);
    }

    static ServerCnxnFactory createNewServerInstance(File dataDir, ServerCnxnFactory factory, String hostPort, int maxCnxns) throws IOException, InterruptedException {
        ZooKeeperServer zks = new ZooKeeperServer(dataDir, dataDir, 3000);
        final int PORT = getPort(hostPort);
        if (factory == null) {
            factory = ServerCnxnFactory.createFactory(PORT, maxCnxns);
        }
        factory.startup(zks);
        Assert.assertTrue("waiting for server up", ClientBase.waitForServerUp("127.0.0.1:" + PORT, CONNECTION_TIMEOUT));
        return factory;
    }

    static void shutdownServerInstance(ServerCnxnFactory factory, String hostPort) {
        if (factory != null) {
            ZKDatabase zkDb;
            {
                ZooKeeperServer zs = getServer(factory);
                zkDb = zs.getZKDatabase();
            }
            factory.shutdown();
            try {
                zkDb.close();
            } catch (IOException ie) {
                LOG.warn("Error closing logs ", ie);
            }
            final int PORT = getPort(hostPort);
            Assert.assertTrue("waiting for server down", ClientBase.waitForServerDown("127.0.0.1:" + PORT, CONNECTION_TIMEOUT));
        }
    }

    /**
     * Test specific setup
     */
    public static void setupTestEnv() {
        System.setProperty("zookeeper.preAllocSize", "100");
        FileTxnLog.setPreallocSize(100 * 1024);
    }

    protected void setUpAll() throws Exception {
        allClients = new LinkedList<ZooKeeper>();
        allClientsSetup = true;
    }

    @Before
    public void setUp() throws Exception {
        OperatingSystemMXBean osMbean = ManagementFactory.getOperatingSystemMXBean();
        if (osMbean != null && osMbean instanceof UnixOperatingSystemMXBean) {
            UnixOperatingSystemMXBean unixos = (UnixOperatingSystemMXBean) osMbean;
            initialFdCount = unixos.getOpenFileDescriptorCount();
            LOG.info("Initial fdcount is: " + initialFdCount);
        }
        setupTestEnv();
        JMXEnv.setUp();
        setUpAll();
        tmpDir = createTmpDir(BASETEST);
        startServer();
        LOG.info("Client test setup finished");
    }

    protected void startServer() throws Exception {
        LOG.info("STARTING server");
        serverFactory = createNewServerInstance(tmpDir, serverFactory, hostPort, maxCnxns);
        JMXEnv.ensureOnly("InMemoryDataTree", "StandaloneServer_port");
    }

    protected void stopServer() throws Exception {
        LOG.info("STOPPING server");
        shutdownServerInstance(serverFactory, hostPort);
        serverFactory = null;
        JMXEnv.ensureOnly();
    }

    protected static ZooKeeperServer getServer(ServerCnxnFactory fac) {
        ZooKeeperServer zs = ServerCnxnFactoryAccessor.getZkServer(fac);
        return zs;
    }

    protected void tearDownAll() throws Exception {
        synchronized (this) {
            if (allClients != null) for (ZooKeeper zk : allClients) {
                try {
                    if (zk != null) zk.close();
                } catch (InterruptedException e) {
                    LOG.warn("ignoring interrupt", e);
                }
            }
            allClients = null;
        }
    }

    @After
    public void tearDown() throws Exception {
        LOG.info("tearDown starting");
        tearDownAll();
        stopServer();
        if (tmpDir != null) {
            Assert.assertTrue("delete " + tmpDir.toString(), recursiveDelete(tmpDir));
        }
        serverFactory = null;
        JMXEnv.tearDown();
        OperatingSystemMXBean osMbean = ManagementFactory.getOperatingSystemMXBean();
        if (osMbean != null && osMbean instanceof UnixOperatingSystemMXBean) {
            UnixOperatingSystemMXBean unixos = (UnixOperatingSystemMXBean) osMbean;
            long fdCount = unixos.getOpenFileDescriptorCount();
            String message = "fdcount after test is: " + fdCount + " at start it was " + initialFdCount;
            LOG.info(message);
            if (fdCount > initialFdCount) {
                LOG.info("sleeping for 20 secs");
            }
        }
    }

    public static MBeanServerConnection jmxConn() throws IOException {
        return JMXEnv.conn();
    }

    public static boolean recursiveDelete(File d) {
        if (d.isDirectory()) {
            File children[] = d.listFiles();
            for (File f : children) {
                Assert.assertTrue("delete " + f.toString(), recursiveDelete(f));
            }
        }
        return d.delete();
    }

    public static void logAllStackTraces() {
        StringBuilder sb = new StringBuilder();
        sb.append("Starting logAllStackTraces()\n");
        Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
        for (Entry<Thread, StackTraceElement[]> e : threads.entrySet()) {
            sb.append("Thread " + e.getKey().getName() + "\n");
            for (StackTraceElement elem : e.getValue()) {
                sb.append("\tat " + elem + "\n");
            }
        }
        sb.append("Ending logAllStackTraces()\n");
        LOG.error(sb.toString());
    }

    void verifyRootOfAllServersMatch(String hostPort) throws InterruptedException, KeeperException, IOException {
        String parts[] = hostPort.split(",");
        int[] counts = new int[parts.length];
        int failed = 0;
        for (int j = 0; j < 100; j++) {
            int newcounts[] = new int[parts.length];
            int i = 0;
            for (String hp : parts) {
                try {
                    ZooKeeper zk = createClient(hp);
                    try {
                        newcounts[i++] = zk.getChildren("/", false).size();
                    } finally {
                        zk.close();
                    }
                } catch (Throwable t) {
                    failed++;
                    logAllStackTraces();
                }
            }
            if (Arrays.equals(newcounts, counts)) {
                LOG.info("Found match with array:" + Arrays.toString(newcounts));
                counts = newcounts;
                break;
            } else {
                counts = newcounts;
                Thread.sleep(10000);
            }
            if (failed > 10) {
                break;
            }
        }
        String logmsg = "node count not consistent{} {}";
        for (int i = 1; i < parts.length; i++) {
            if (counts[i - 1] != counts[i]) {
                LOG.error(logmsg, Integer.valueOf(counts[i - 1]), Integer.valueOf(counts[i]));
            } else {
                LOG.info(logmsg, Integer.valueOf(counts[i - 1]), Integer.valueOf(counts[i]));
            }
        }
    }
}
