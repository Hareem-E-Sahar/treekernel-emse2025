import java.io.*;
import java.util.*;
import java.sql.*;
import org.newsclub.net.mysql.AFUNIXDatabaseSocketFactory;

public class Megatron extends Thread {

    private static BarebonesLogger _log;

    public static boolean _debug;

    public Player _player;

    public MetadataSource _fallbackSource;

    public PlaylistManager _pManager;

    public RemoteBroadcastManager _rManager;

    private Connection _mysql;

    private ScheduleLoader _sloader;

    private Schedule _schedule;

    private HashMap<String, Process> _recorders;

    private LinkedList<TrackStamp> _playlist;

    private int _trackAdjustment;

    private int _internalPort;

    private int _externalPort;

    private String _recorderBase;

    private int _prequeue;

    private int _minimumQueue;

    private int _skipThreshold;

    private String _timezone;

    private String _remoteStream;

    private String _remoteDifficulty;

    private volatile boolean _sqlAvailable;

    private volatile boolean _scheduleAtomic;

    private volatile boolean _recorderAtomic;

    private volatile boolean _trackPlaylist;

    private volatile boolean _playerAvailable;

    private volatile boolean _megatronActive;

    private Object _scheduleLock, _playerLock, _SQLLock, _recorderLock;

    public Megatron(HashMap<String, String> cfg) {
        _scheduleLock = new Object();
        _playerLock = new Object();
        _SQLLock = new Object();
        _recorderLock = new Object();
        _player = null;
        _fallbackSource = null;
        _scheduleAtomic = true;
        _recorderAtomic = true;
        _trackPlaylist = false;
        _sqlAvailable = true;
        _playerAvailable = true;
        _megatronActive = false;
        _playlist = new LinkedList<TrackStamp>();
        _debug = (Integer.parseInt(cfg.get("debug")) > 0);
        _trackAdjustment = Integer.parseInt(cfg.get("trackAdjustment"));
        _internalPort = Integer.parseInt(cfg.get("internalPort"));
        _externalPort = Integer.parseInt(cfg.get("externalPort"));
        _prequeue = Integer.parseInt(cfg.get("prequeue"));
        _recorderBase = cfg.get("recorderBase");
        _minimumQueue = Integer.parseInt(cfg.get("minimumQueue"));
        _skipThreshold = Integer.parseInt(cfg.get("skipThreshold"));
        _timezone = cfg.get("timezone");
        _remoteStream = cfg.get("remoteStream");
        _remoteDifficulty = cfg.get("remoteDifficulty");
        getLog();
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Properties props = new Properties();
            props.put("user", cfg.get("dbUser"));
            props.put("password", cfg.get("dbPass"));
            props.put("socketFactory", AFUNIXDatabaseSocketFactory.class.getName());
            props.put("junixsocket.file", cfg.get("dbPath"));
            _mysql = DriverManager.getConnection("jdbc:mysql://", props);
            _mysql.setCatalog(cfg.get("dbName"));
        } catch (Exception e) {
            _log.log("Megatron: could not connect to database at the socket, playlist tracking disabled.");
            e.printStackTrace();
        }
        loadPlayer(cfg.get("player"));
        loadMetadataSource(cfg.get("fallback"));
        StringBuffer query = new StringBuffer();
        query.append("SELECT op, showid FROM ptracker_Playlists WHERE op=\"startshow\" ORDER BY date DESC LIMIT 1");
        acquireSQLLock();
        ResultSet rs = doQuery(query.toString());
        boolean initiateRobotShow = false;
        try {
            rs.next();
            if (rs.getInt("showid") == getAutorotationShow()) initiateRobotShow = true;
        } catch (SQLException e) {
            _log.error("Megatron: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
        releaseSQLLock();
        if (initiateRobotShow) {
            _log.log("Megatron: taking the helm we accidentally left...");
            startTracking();
        }
    }

    private void transform(String spath) {
        MegatronListener iListener, eListener;
        acquireScheduleLock();
        _recorders = new HashMap<String, Process>();
        _pManager = new PlaylistManager(this);
        _pManager.start();
        _rManager = new RemoteBroadcastManager(this);
        _rManager.start();
        loadSchedule(spath);
        iListener = new MegatronListener(this, _internalPort);
        iListener.start();
        eListener = new MegatronListener(this, _externalPort);
        eListener.start();
        releaseScheduleLock();
    }

    public void run() {
        StringBuffer query = new StringBuffer();
        getLog().log("Megatron: FCC bookkeeping enabled\n");
        while (true) {
            try {
                while (true) sleep(1000000000);
            } catch (InterruptedException e) {
            }
            acquirePlayerLock();
            if (_player == null || _fallbackSource == null) {
                releasePlayerLock();
                continue;
            }
            if (!_trackPlaylist) {
                releasePlayerLock();
                continue;
            }
            acquireSQLLock();
            getLog().log("Megatron: taking care of FCC bookkeeping\n");
            MetadataSource meta;
            TrackStamp ts;
            while ((ts = _playlist.pollFirst()) != null) {
                if (ts._block instanceof MetadataSource) meta = (MetadataSource) ts._block; else meta = _fallbackSource;
                meta.chooseTrack(ts._track, ts._pos);
                query.setLength(0);
                if (meta.isPSA() || meta.isStationID() || meta.isDisclaimer() || meta.isUnderwriting() || meta.isCommunity()) {
                    query.append("INSERT INTO ptracker_Playlists SET op=\"housekeeping\", date=FROM_UNIXTIME(" + ts._offset + "), ");
                    query.append("showid=\"" + getAutorotationShow() + "\", ");
                    query.append("housekeeping=\"" + (meta.isPSA() ? "PSA" : (meta.isStationID() ? "Station ID" : (meta.isDisclaimer() ? "Disclaimer" : (meta.isUnderwriting() ? "Underwriting" : (meta.isCommunity() ? "Community" : ""))))) + "\"");
                } else {
                    query.append("INSERT INTO ptracker_Playlists SET op=\"song\", date=FROM_UNIXTIME(" + ts._offset + "), ");
                    query.append("showid=\"" + getAutorotationShow() + "\", ");
                    query.append("artist=\"" + meta.artist() + "\", ");
                    query.append("song=\"" + meta.track() + "\", ");
                    query.append("album=\"" + meta.album() + "\", ");
                    query.append("recordlabel=\"" + meta.label() + "\", ");
                    query.append("r=\"" + (meta.isRequest() ? 1 : "") + "\", ");
                    query.append("local=\"" + (meta.isLocal() ? 1 : "") + "\", ");
                    query.append("nr=\"" + (meta.NR() > -1 ? meta.NR() : "") + "\", ");
                    query.append("va=\"" + (meta.isVA() ? 1 : "") + "\", ");
                    query.append("genre=\"" + meta.genre() + "\"");
                }
                doQuery(query.toString());
            }
            releaseSQLLock();
            releasePlayerLock();
            _megatronActive = false;
        }
    }

    public void acquireSQLLock() {
        synchronized (_SQLLock) {
            try {
                while (!_sqlAvailable) _SQLLock.wait();
                _sqlAvailable = false;
                _log.debug("SQL lock acquired!\n");
            } catch (InterruptedException e) {
            }
        }
    }

    public synchronized void releaseSQLLock() {
        synchronized (_SQLLock) {
            _sqlAvailable = true;
            _log.debug("SQL lock relesaed!\n");
            _SQLLock.notifyAll();
        }
    }

    public Schedule acquireScheduleLock() {
        synchronized (_scheduleLock) {
            try {
                while (!_scheduleAtomic) _scheduleLock.wait();
                _scheduleAtomic = false;
                _log.debug("Schedule lock acquired!\n");
                return _schedule;
            } catch (InterruptedException e) {
                e.printStackTrace();
                _log.error("Megatron: schedule lock acquisition interrupted, this is a bug\nExpect the schedule to get wrecked\n");
                return null;
            }
        }
    }

    public void releaseScheduleLock() {
        synchronized (_scheduleLock) {
            _log.debug("Schedule lock released!\n");
            _scheduleAtomic = true;
            _scheduleLock.notifyAll();
        }
    }

    public void acquireRecorderLock() {
        synchronized (_recorderLock) {
            try {
                while (!_recorderAtomic) wait();
                _recorderAtomic = false;
            } catch (InterruptedException e) {
                _log.error("Megatron: recorder lock interrupted, bad things will happen soon");
                e.printStackTrace();
            }
        }
    }

    public void releaseRecorderLock() {
        synchronized (_recorderLock) {
            _recorderAtomic = true;
            _recorderLock.notifyAll();
        }
    }

    public void acquirePlayerLock() {
        synchronized (_playerLock) {
            try {
                while (!_playerAvailable) _playerLock.wait();
                _log.debug("Player lock acquired!\n");
                _playerAvailable = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void releasePlayerLock() {
        synchronized (_playerLock) {
            _log.debug("Player lock released!\n");
            _playerAvailable = true;
            _playerLock.notifyAll();
        }
    }

    public void waitForSchedule() {
        synchronized (_scheduleLock) {
            while (_schedule == null) {
                try {
                    _log.log("Megatron: no schedule loaded, sleeping...\n");
                    _scheduleLock.wait();
                } catch (InterruptedException e) {
                    _log.log("Megatron: woke up waiting for a schedule\n");
                }
            }
        }
    }

    public void waitForPlayer() {
        synchronized (_playerLock) {
            while (_player == null || _fallbackSource == null) {
                try {
                    if (_player == null) _log.log("Megatron: no player loaded, sleeping...\n");
                    if (_fallbackSource == null) _log.log("Megatron: no fallback metadata source loaded, sleeping...\n");
                    _playerLock.wait();
                } catch (InterruptedException e) {
                    _log.log("Megatron: woke up waiting for a player\n");
                }
            }
        }
    }

    public void wakePlaylistManager() {
        _pManager.interrupt();
    }

    public void trackTrack(TrackStamp t) {
        if (_scheduleAtomic || _playerAvailable) {
            _log.error("WARNING: trackTrack() called from unlocked context!\n");
            (new Exception()).printStackTrace();
        }
        if (_trackPlaylist) _playlist.add(t);
        _log.debug("tracked track\n");
    }

    public boolean startTracking() {
        if (_trackPlaylist) return false;
        acquireSQLLock();
        doQuery("INSERT INTO ptracker_Playlists SET op=\"startshow\", date=NOW(), showid=\"" + getAutorotationShow() + "\"");
        _trackPlaylist = true;
        releaseSQLLock();
        _log.log("Megatron: GNU Girls now officially at the helm...\n");
        return true;
    }

    public boolean trackPlaylist() {
        return _trackPlaylist;
    }

    public void stopTracking() {
        if (!_trackPlaylist) return;
        interrupt();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
        }
        acquireSQLLock();
        doQuery("DELETE FROM ptracker_Playlists WHERE date >= NOW()");
        doQuery("INSERT INTO ptracker_Playlists SET op=\"endshow\", date=NOW(), showid=\"" + getAutorotationShow() + "\"");
        _playlist.clear();
        _trackPlaylist = false;
        releaseSQLLock();
        _log.log("Megatron: GNU Girls signing off...\n");
    }

    public int getInternalPort() {
        return _internalPort;
    }

    public void loadSchedule(String spath) {
        Schedule old;
        old = _schedule;
        try {
            _schedule = null;
            _sloader = new XMLScheduleLoader(spath, this);
            _log.debug("Schedule " + spath + " loaded from raw XML");
            _schedule = _sloader.produceSchedule();
            _log.debug("Schedule " + spath + " parsed into circular list structure");
            _schedule.initialize();
            _log.debug("Schedule " + spath + " initialized");
            _pManager.firstRun(true);
        } catch (Exception e) {
            e.printStackTrace();
            _log.log("Megatron: I could not load schedule " + spath + "\n");
            _schedule = old;
        }
    }

    public void useSchedule(Schedule s) {
        _schedule = s;
    }

    public ResultSet doQuery(String query) {
        try {
            Statement stmt = _mysql.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.execute(query);
            return stmt.getResultSet();
        } catch (SQLException e) {
            _log.error("SQL: " + e.getMessage() + "\n");
            return null;
        }
    }

    public int getAutorotationUser() {
        ResultSet rs = doQuery("SELECT ptracker_futaUser FROM admin_Metadata");
        try {
            rs.next();
            return rs.getInt("ptracker_futaUser");
        } catch (SQLException e) {
            _log.error("getAutorotationUser: no ptracker_futaUser defined\n");
            return -1;
        }
    }

    public int getAutorotationShow() {
        ResultSet rs = doQuery("SELECT admin_Shows.id AS id FROM admin_Shows, admin_Metadata WHERE admin_Shows.creatorid=admin_Metadata.ptracker_futaUser");
        try {
            rs.next();
            return rs.getInt("id");
        } catch (SQLException e) {
            _log.error("getAutorotationShow: " + e.getMessage() + "\n");
            e.printStackTrace();
            return -1;
        }
    }

    public void spawnRecorder(String stream, String fname) {
        acquireRecorderLock();
        String args[] = { "/usr/bin/wget", "http://192.168.1.1:8000/" + stream, "--output-document=" + _recorderBase + "/" + fname, "-q" };
        try {
            if (_recorders.containsKey(stream + fname)) _log.log("Autorecord: " + fname + " on stream " + stream + " already recording.\n"); else {
                Process p = (Runtime.getRuntime()).exec(args);
                _recorders.put(stream + fname, p);
                _log.log("Autorecord: " + fname + " on stream " + stream + " now recording\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        releaseRecorderLock();
    }

    public String queryRecorders() {
        StringBuffer sb = new StringBuffer();
        acquireRecorderLock();
        Iterator<String> i = (_recorders.keySet()).iterator();
        while (i.hasNext()) sb.append(i.next() + "\n");
        releaseRecorderLock();
        return sb.toString();
    }

    public void reapRecorder(String stream, String fname) {
        acquireRecorderLock();
        Process p;
        if (!_recorders.containsKey(stream + fname)) _log.log("Autorecord: could not find " + fname + " recording on stream " + stream + "\n"); else {
            (_recorders.remove(stream + fname)).destroy();
            _log.log("Autorecord: " + fname + " on stream " + stream + " stopped successfully.\n");
        }
        releaseRecorderLock();
    }

    public static BarebonesLogger getLog() {
        if (_log == null) _log = new TextfileLogger("megatrond-log", "megatrond-error");
        return _log;
    }

    public boolean loadPlayer(String classname) {
        Player p;
        boolean retval;
        acquirePlayerLock();
        p = _player;
        _player = null;
        try {
            _player = (Player) Class.forName(classname).getConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            (Megatron._log).log("PlayerLoader: " + e + "\n");
            _player = p;
        } catch (NoSuchMethodException e) {
            (Megatron._log).log("PlayerLoader: " + e + "\n");
            _player = p;
        } catch (IllegalAccessException e) {
            (Megatron._log).log("PlayerLoader: Could not access constructor of " + classname + "\n");
            _player = p;
        } catch (Exception e) {
            e.printStackTrace();
            _log.error("PlayerLoader: A queer error has been encountered\n" + e + "\n");
            _player = p;
        }
        _log.debug("PlayerLoader: made it through...\n");
        if (_player == p) retval = false; else retval = true;
        releasePlayerLock();
        return retval;
    }

    public boolean loadMetadataSource(String classname) {
        MetadataSource ms;
        boolean retval;
        _log.debug("loadMetadataSource");
        acquirePlayerLock();
        ms = _fallbackSource;
        _fallbackSource = null;
        try {
            _fallbackSource = (MetadataSource) Class.forName(classname).getConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            _log.log("MetadataSourceLoader: " + e + "\n");
            _fallbackSource = ms;
        } catch (NoSuchMethodException e) {
            _log.log("MetadataSourceLoader: " + e + "\n");
            _fallbackSource = ms;
        } catch (IllegalAccessException e) {
            _log.log("MetadataSourceLoader: Could not access constructor of " + classname + "\n");
            _fallbackSource = ms;
        } catch (Exception e) {
            e.printStackTrace();
            _log.error("MetadataSourceLoader: A queer error has been encountered\n" + e + "\n");
            _fallbackSource = ms;
        }
        _log.debug("MetadataLoader: made it through...\n");
        if (_fallbackSource == ms) retval = false; else retval = true;
        releasePlayerLock();
        return retval;
    }

    public int skipThreshold() {
        return _skipThreshold;
    }

    public void skipThreshold(int k) {
        _skipThreshold = k;
    }

    public int prequeue() {
        return _prequeue;
    }

    public void prequeue(int k) {
        _prequeue = k;
    }

    public int trackAdjustment() {
        return _trackAdjustment;
    }

    public void trackAdjustment(int k) {
        _trackAdjustment = k;
    }

    public int minimumQueue() {
        return _minimumQueue;
    }

    public void minimumQueue(int k) {
        _minimumQueue = k;
    }

    public String timezone() {
        return _timezone;
    }

    public String remoteStream() {
        return _remoteStream;
    }

    public String remoteDifficulty() {
        return _remoteDifficulty;
    }

    public void remoteDifficulty(String s) {
        _remoteDifficulty = s;
    }

    public static void main(String[] argv) {
        File conf;
        if (argv.length < 1) conf = new File("/etc/megatrond.conf"); else conf = new File(argv[0]);
        if (!conf.exists()) System.err.println("FATAL: configuration " + conf.getAbsolutePath() + " not found"); else {
            HashMap<String, String> cfg;
            try {
                cfg = (new ConfigurationParser(conf)).parse();
                Megatron m = new Megatron(cfg);
                m.transform(cfg.get("defaultSchedule"));
                m.start();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("FATAL: Errors encountered when trying to parse the configuration " + e + "\n");
                System.exit(0);
            }
        }
    }
}
