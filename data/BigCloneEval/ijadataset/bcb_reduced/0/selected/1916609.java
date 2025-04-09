package com.tscribble.bitleech.core.download;

import static com.tscribble.bitleech.core.download.State.BUILDING;
import static com.tscribble.bitleech.core.download.State.CHECKING;
import static com.tscribble.bitleech.core.download.State.COMPLETED;
import static com.tscribble.bitleech.core.download.State.CONNECTING;
import static com.tscribble.bitleech.core.download.State.DEFAULT;
import static com.tscribble.bitleech.core.download.State.DOWNLOADING;
import static com.tscribble.bitleech.core.download.State.ERROR;
import static com.tscribble.bitleech.core.download.State.PAUSED;
import static com.tscribble.bitleech.core.download.State.READY;
import static com.tscribble.bitleech.core.download.State.STOPPED;
import static java.lang.Math.max;
import static java.lang.System.currentTimeMillis;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import com.tscribble.bitleech.core.config.ConfigManager;
import com.tscribble.bitleech.core.download.auth.IAuthProvider;
import com.tscribble.bitleech.core.download.handler.IProtocolHandler;
import com.tscribble.bitleech.core.download.handler.ProtocolHandlerManager;
import com.tscribble.bitleech.core.download.protocol.IProtocolClient;
import com.tscribble.bitleech.core.io.PartBuilder;

/**
 * @author triston  
 * 
 * Created on Feb 17, 2005 
 */
public class Download implements IDownloadWorkerListener {

    /**
	 * Logger for this class
	 */
    private static final Logger log = Logger.getLogger("Download");

    private String url;

    private String host;

    private String fileName;

    private String saveDir;

    private String tmpDir;

    private String file;

    private String partPrefix;

    private boolean resumable;

    private long size;

    private long modified;

    private long progress;

    private long startTime;

    private long elapsedTime;

    private long bytesRead;

    private long bytesReadLastSec;

    private long timeLeft;

    private long byteSpeed;

    private long byteSpeedLastSec;

    private long aveByteSpeed;

    private long chunk;

    private int maxthreads = 4;

    private int threads;

    private int dlCount;

    private int conCount;

    private int pauseCount;

    private int stopCount;

    private int doneCount;

    private int errCount;

    private IProtocolHandler hanndler;

    private State state;

    private FileDBClient rdbcl;

    private PartBuilder builder;

    private DownloadWorker[] warray;

    private List<DownloadWorker> workers;

    private List<IDownloadListener> calls;

    private Set<Host> hosts;

    static final int BUFF_SIZE = 1024 * 4;

    private static final int MIN_CHUNK = 1024 * 500;

    Download() {
        state = DEFAULT;
        hosts = new HashSet<Host>();
        calls = new ArrayList<IDownloadListener>();
        rdbcl = new FileDBClient(this);
        builder = new PartBuilder();
        saveDir = ConfigManager.getDownloadDir();
        tmpDir = ConfigManager.getTmpDir();
    }

    void init(String url, String file) {
        setURL(url);
        if (file != null) setFile(file); else setFileName(extractFileName(url));
        setPartPrefix(fileName);
    }

    public synchronized void getDownloadInfo() {
        Thread it = new Thread(new Runnable() {

            public void run() {
                IProtocolClient cl = null;
                IAuthProvider ap = null;
                try {
                    cl = hanndler.getClient(url);
                    cl.setURL(url);
                    ap = hanndler.getAuthProvider(url);
                    ap.setSite(extractSite(url));
                    cl.setAuthProvider(ap);
                    log.debug("Getting info for: " + url);
                    cl.getDownloadInfo();
                    setDownloadInfo(cl);
                    rdbcl.getResInfo();
                    hosts = rdbcl.getAltSources();
                    prepareWorkers();
                    setState(READY);
                } catch (Exception e) {
                    setState(ERROR);
                    log.error("getDownloadInfo() - " + e.getMessage(), e);
                } finally {
                    if (cl != null) cl.disconnect();
                    cl = null;
                }
            }
        });
        it.setName("GetInfo Thread");
        it.setDaemon(true);
        it.setPriority(Thread.MIN_PRIORITY);
        setState(CHECKING);
        it.start();
    }

    public FileDBClient getResDBClient() {
        return rdbcl;
    }

    public void setHandler(IProtocolHandler handler) {
        this.hanndler = handler;
    }

    private void setDownloadInfo(IProtocolClient cl) {
        setURL(cl.getURL());
        host = cl.getHostName();
        setFileName(extractFileName(getURL()));
        modified = cl.getLastModified();
        size = cl.getSize();
        resumable = cl.isResumable();
        maxthreads = resumable ? maxthreads : 1;
        partPrefix = fileName;
    }

    void prepareWorkers() {
        chunk = max(MIN_CHUNK, (size / maxthreads));
        workers = new ArrayList<DownloadWorker>();
        long rangeEnd = size - 1;
        long tmpEnd = 0;
        long start = 0;
        long end = 0;
        Iterator<Host> it = hosts.iterator();
        while (true) {
            tmpEnd = (start + chunk - 1);
            if (tmpEnd > rangeEnd || (rangeEnd - tmpEnd) <= (0.50 * chunk)) end = rangeEnd; else end = tmpEnd;
            DownloadWorker w = new DownloadWorker();
            String wurl;
            if (it.hasNext()) wurl = it.next().getUrl(); else wurl = getURL();
            IProtocolHandler handler = ProtocolHandlerManager.getHandler(wurl);
            IProtocolClient cl = handler.getClient(wurl);
            IAuthProvider ap = handler.getAuthProvider(wurl);
            ap.setSite(extractSite(wurl));
            cl.setAuthProvider(ap);
            w.setURL(wurl);
            w.setProtocolClient(cl);
            w.addListener(this);
            w.setRangeStart(start);
            w.setPosition(start);
            w.setRangeEnd(end);
            w.setPartSize((end - start) + 1);
            w.setID(workers.size());
            w.setTmpDir(tmpDir);
            w.setPartPrefix(partPrefix);
            w.setPartName(partPrefix + '.' + workers.size());
            w.init();
            workers.add(w);
            if (end == rangeEnd) break; else start += chunk;
        }
        warray = workers.toArray(new DownloadWorker[workers.size()]);
        threads = warray.length;
    }

    private String extractSite(String url) {
        String site;
        int dslash = url.indexOf("//");
        int sslash = url.indexOf("/", dslash + 2);
        site = url.substring(0, sslash);
        return site;
    }

    private String extractFileName(String url) {
        String fileName = null;
        try {
            int lastSlash = url.lastIndexOf('/');
            fileName = url.substring(lastSlash + 1);
            int eq = fileName.lastIndexOf('=');
            int ques = fileName.lastIndexOf('?');
            fileName = (ques > -1) ? fileName.substring(ques + 1) : ((eq > -1) ? fileName.substring(eq + 1) : fileName);
        } catch (Exception e) {
            fileName = "Unknown";
            log.error("Error extracting filename");
        }
        return fileName;
    }

    public synchronized void start() {
        if (!isReady()) {
            log.debug("Download not Ready!");
            return;
        }
        startTime = currentTimeMillis();
        for (DownloadWorker worker : workers) {
            if (worker.isCompleted()) continue;
            worker.start();
        }
    }

    public synchronized void stop() {
        for (DownloadWorker worker : workers) {
            if (worker.isCompleted()) continue;
            if (!worker.isStopped()) worker.stop();
        }
    }

    public synchronized void pause() {
        for (DownloadWorker worker : workers) {
            if (worker.isCompleted()) continue;
            if (!worker.isStopped()) worker.pause();
        }
    }

    public synchronized void resume() {
        for (DownloadWorker worker : workers) {
            if (worker.isCompleted()) continue;
            worker.resume();
        }
    }

    public boolean isChecking() {
        return state == CHECKING;
    }

    public boolean isReady() {
        return state == READY;
    }

    public boolean isConnecting() {
        return state == CONNECTING;
    }

    public boolean isDownloading() {
        return state == DOWNLOADING;
    }

    public boolean isStopped() {
        return state == STOPPED;
    }

    public boolean isPaused() {
        return state == PAUSED;
    }

    public boolean isBuilding() {
        return state == BUILDING;
    }

    public boolean isCompleted() {
        return state == COMPLETED;
    }

    public boolean isError() {
        return state == ERROR;
    }

    public String getHost() {
        return host;
    }

    synchronized void setState(State st) {
        state = st;
        fireStateChanged(state);
    }

    void setResumable(boolean resumable) {
        this.resumable = resumable;
    }

    public boolean isResumable() {
        return resumable;
    }

    public State getState() {
        return state;
    }

    public String getStateTxt() {
        return state.toString();
    }

    public String getURL() {
        return url;
    }

    void setURL(String url) {
        this.url = url.replace((char) 92, (char) 47);
        this.url = url.replaceAll(" ", "%20");
    }

    public String getSaveDir() {
        return saveDir;
    }

    public void setSaveDir(String dir) {
        this.saveDir = dir;
    }

    public String getTmpDir() {
        return tmpDir;
    }

    public void setTmpDir(String dir) {
        this.tmpDir = dir;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        File f = new File(file);
        saveDir = f.getParent();
        fileName = f.getName();
        this.file = file;
    }

    public void setFileName(String name) {
        fileName = (name.length() < 1) ? "Unknown" : name;
        try {
            file = new File(saveDir, fileName).getCanonicalPath();
        } catch (IOException e) {
            fileName = "Unknown";
        }
    }

    public String getFileName() {
        return fileName;
    }

    void setPartPrefix(String prefix) {
        partPrefix = prefix;
    }

    public String getPartPrefix() {
        return partPrefix;
    }

    public long getProgress() {
        return progress;
    }

    public long getSize() {
        return size;
    }

    void setSize(long size) {
        this.size = size;
    }

    public long getLastModified() {
        return modified;
    }

    void setLastModified(long lastMod) {
        modified = lastMod;
    }

    public long getBytesRead() {
        return bytesRead;
    }

    /**
	 * bytesSpeed is the bytesReads since the last second.
	 * The current second's speed will then become the 
	 * last second's speed stored as bytesReadLastSec.
	 */
    public long getSpeed() {
        return byteSpeed;
    }

    public long getAveSpeed() {
        return aveByteSpeed;
    }

    public long getTimeLeft() {
        return timeLeft;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public int getMaxThreads() {
        return maxthreads;
    }

    public void setMaxThreads(int threads) {
        this.maxthreads = threads;
    }

    public int getThreadCount() {
        return threads;
    }

    public List<DownloadWorker> getWorkers() {
        return workers;
    }

    /**
	 * Updates properties of this download. This should be called once every
	 * second.
	 */
    final void update() {
        if (state == DEFAULT) return;
        long count = 0;
        for (DownloadWorker w : warray) {
            count += w.bytesRead;
        }
        bytesRead = count;
        byteSpeed = bytesRead - bytesReadLastSec;
        bytesReadLastSec = bytesRead;
        aveByteSpeed = (byteSpeed + byteSpeedLastSec) / 2;
        byteSpeedLastSec = byteSpeed;
        long bytesLeft = size - bytesRead;
        if (state == DOWNLOADING) {
            timeLeft = bytesLeft / max(1, byteSpeed);
            long currentTime = System.currentTimeMillis();
            elapsedTime = (currentTime - startTime) / 1000;
        }
        progress = (state == BUILDING) ? builder.getProgress() : (size < 1) ? 0 : (bytesRead * 100) / size;
    }

    public void addListener(IDownloadListener dl) {
        if (!calls.contains(dl)) calls.add(dl);
    }

    public void removeListener(IDownloadListener dl) {
        calls.remove(dl);
    }

    private void fireStateChanged(State state) {
        if (!isError()) log.debug(getFileName() + ": " + state);
        for (IDownloadListener l : calls) {
            l.stateChanged(state, this);
        }
    }

    public void stateChanged(State state, DownloadWorker dw) {
        synchronized (warray) {
            int wcount = warray.length;
            int active = wcount - doneCount;
            switch(state) {
                case CONNECTING:
                    if (++conCount == 1) {
                        setState(state);
                    }
                    if (conCount == active) conCount = 0;
                    break;
                case DOWNLOADING:
                    if (++dlCount == 1) {
                        setState(state);
                    }
                    if (dlCount == active) dlCount = 0;
                    break;
                case PAUSED:
                    if (++pauseCount == active) {
                        pauseCount = 0;
                        setState(state);
                    }
                    break;
                case STOPPED:
                    if (++stopCount == active) {
                        stopCount = 0;
                        setState(state);
                    }
                    break;
                case ERROR:
                    if (++errCount == active) {
                        errCount = 0;
                        setState(state);
                    }
                    break;
                case COMPLETED:
                    if (wcount == ++doneCount) {
                        setState(BUILDING);
                        Thread t = new Thread(new Runnable() {

                            public void run() {
                                builder.setTmpDir(tmpDir);
                                builder.setPrefix(partPrefix);
                                builder.setDestFile(file);
                                builder.build();
                                setState(COMPLETED);
                            }
                        });
                        t.setDaemon(true);
                        t.setPriority(Thread.MIN_PRIORITY);
                        t.start();
                    }
                    break;
                default:
                    state = DEFAULT;
                    break;
            }
        }
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((fileName == null) ? 0 : fileName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final Download other = (Download) obj;
        if (fileName == null) {
            if (other.fileName != null) return false;
        } else if (!fileName.equals(other.fileName)) return false;
        return true;
    }

    public String toString() {
        return getFileName();
    }
}
