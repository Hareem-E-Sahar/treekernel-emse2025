package org.fao.waicent.kids.giews.communication.providermodule;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.TreeMap;
import java.util.Vector;
import org.fao.waicent.db.dbConnectionManager;
import org.fao.waicent.db.dbConnectionManagerPool;
import org.fao.waicent.kids.giews.communication.Node;
import org.fao.waicent.kids.giews.communication.PooledRequestesUDP;
import org.fao.waicent.kids.giews.communication.apimodule.ApiModule;
import org.fao.waicent.kids.giews.communication.providermodule.TransferInfoModule.PooledTransfer;
import org.fao.waicent.kids.giews.communication.providermodule.TransferInfoModule.TransferInfo;
import org.fao.waicent.kids.giews.communication.providermodule.downloadmodule.PooledDownload;
import org.fao.waicent.kids.giews.communication.providermodule.searchengine.SearchEngine;
import org.fao.waicent.kids.giews.communication.providermodule.uploadmodule.PooledUpload;
import org.fao.waicent.kids.giews.communication.requestmodule.Requestes;
import org.fao.waicent.kids.giews.communication.utility.ConfigurationClass;
import org.fao.waicent.kids.giews.communication.utility.ConfigurationException;
import org.fao.waicent.kids.giews.communication.utility.DownloadInfo;
import org.fao.waicent.kids.giews.communication.utility.MyDebug;
import org.fao.waicent.kids.giews.communication.utility.Profile;
import org.fao.waicent.kids.giews.communication.utility.Resources;
import org.fao.waicent.kids.giews.communication.utility.Util;
import org.fao.waicent.kids.giews.communication.utility.message.Message;
import org.fao.waicent.kids.giews.communication.utility.message.MessageException;
import org.fao.waicent.kids.giews.communication.utility.message.MyParser;

/**
 * <p>Title: ProviderClass</p>
 *
 *
 * @author A. Tamburo
 * @version 1, last modified by A. Tamburo, 16/11/05
 */
public class ProviderClass extends Thread implements Node {

    private static int MAX_PACKET_SIZE = 512;

    public static byte ACTIVE = 0;

    public static byte REGISTRATION = 1;

    public static byte CONFIGURATION = 2;

    public static byte INITIALSTATE = 3;

    private PooledRequestesUDP poolRequestes;

    private InetAddress addrBootNode;

    private int UDPportBootNode;

    private int TCPportBootNode;

    private int SSLportBootNode;

    private byte[] idBootNode;

    private Profile profile;

    private MyParser parser;

    public Registration registration;

    private Requestes requestes;

    private DatagramSocket socket;

    private DatagramPacket inPacket;

    private byte[] buffer;

    private ThreadPing threadPing;

    private static int TIMEOUT_PING = 60 * 1000;

    private long lastTimePing;

    private byte state;

    public boolean running;

    private ConfigurationClass cf;

    private String database_ini;

    private MyDebug debug;

    private Hashtable projectsExported;

    private Hashtable featureLayersExported;

    private Hashtable rasterLayersExported;

    private Hashtable datasetExported;

    private PooledDownload poolDown;

    private PooledUpload poolUp;

    private String pathConfiguration;

    private DateFormat dateformat;

    private SPModule spModule;

    private PooledTransfer pooledTransferThread;

    public static int PAGE_SIZE = 50;

    public Util util;

    private TCPListeningThread tcpListen;

    private SSLTCPListening sslListen;

    private SearchEngine engine;

    private String ibatis_config = "ibatis_config" + File.separator + "SqlMapConfig.xml";

    /**
       * ProviderClass
       *
       * @version 1, last modified by A. Tamburo, 21/11/05
       */
    public ProviderClass(String path, String filename, String database_ini, MyDebug debug) throws ConfigurationException {
        this.state = CONFIGURATION;
        this.pathConfiguration = path;
        this.database_ini = "" + database_ini;
        idBootNode = new byte[Profile.ID_LENGTH];
        parser = new MyParser();
        cf = new ConfigurationClass();
        if (filename != null) cf.setFileName(this.pathConfiguration + java.io.File.separator + filename);
        this.profile = new Profile(Profile.newID());
        try {
            cf.configure(this.pathConfiguration, this);
        } catch (ConfigurationException e) {
            e.printStackTrace();
            throw e;
        }
        try {
            if (this.profile.getUDPPort() != -1) socket = new DatagramSocket(this.profile.getUDPPort()); else {
                socket = new DatagramSocket();
                this.profile.setUDPPort(socket.getLocalPort());
            }
        } catch (SocketException e) {
            throw new ConfigurationException("ProviderClass: " + "error socket creation.");
        }
        try {
            sslListen = new SSLTCPListening(this.profile, debug);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ConfigurationException("ProviderClass: " + " error SSLSocket creation.");
        }
        try {
            tcpListen = new TCPListeningThread(this.profile.getTCPPort(), debug);
        } catch (Exception e) {
            throw new ConfigurationException("ProviderClass: " + " error TCPSocket creation.");
        }
        java.io.File file = new java.io.File(profile.getDownload_GlobalPath());
        if (!file.isDirectory()) {
            profile.setPathDownload(this.pathConfiguration + java.io.File.separator + "tmp_download");
        }
        file = new java.io.File(profile.getUpload_GlobalPath());
        if (!file.isDirectory()) {
            profile.setPathUpload(this.pathConfiguration + java.io.File.separator + "tmp_upload");
        }
        this.debug = debug;
        this.projectsExported = new Hashtable();
        this.featureLayersExported = new Hashtable();
        this.rasterLayersExported = new Hashtable();
        this.datasetExported = new Hashtable();
        registration = new Registration(this.debug);
        this.dateformat = new java.text.SimpleDateFormat("yyyy-MM-dd");
        this.util = new Util(this.database_ini);
        this.engine = new SearchEngine(this.ibatis_config);
        this.state = INITIALSTATE;
    }

    /**
       * Get node profile
       *
       * @version 1, last modified by A. Tamburo, 14/10/05
       */
    public Profile getProfile() {
        return this.profile;
    }

    /**
       * Run the communication module.
       *
       * @version 1, last modified by A. Tamburo, 11/10/05
       */
    public void run() {
        debug.println("Provider: Start node");
        boolean boot = false;
        this.running = true;
        this.setNodeState(REGISTRATION);
        registration.setAddress(this.addrBootNode);
        registration.setPort(this.SSLportBootNode);
        while (this.running) {
            boot = registration.runRegistration2(this, this.profile);
            if (boot) break;
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                debug.println("Provider: sleeping interrupted");
            }
        }
        this.poolRequestes = new PooledRequestesUDP(PooledRequestesUDP.PROVIDER_CLASS, this, socket, debug);
        try {
            socket.setSoTimeout(0);
        } catch (SocketException e) {
            e.printStackTrace();
            return;
        }
        buffer = new byte[MAX_PACKET_SIZE];
        inPacket = new DatagramPacket(buffer, buffer.length);
        this.initializeModule();
        this.testDirectory();
        sslListen.setProviderClass(this);
        sslListen.start();
        tcpListen.setProviderClass(this);
        tcpListen.start();
        while (this.running) {
            try {
                socket.receive(inPacket);
                Message msg = parser.parse(buffer, inPacket.getLength());
                if (this.getNodeState() == CONFIGURATION) {
                    continue;
                }
                if (this.getNodeState() != ACTIVE) {
                    continue;
                }
                InetAddress sender = inPacket.getAddress();
                if (msg.getType() == Message.PUSHDOWNLOAD) msg.setAddress(sender);
                this.poolRequestes.addRequest(msg);
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (MessageException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
       * initialize the module after the Registration Phase
       *
       */
    public void initializeModule() {
        debug.println("Provider: Node profile after registration " + "{\n" + profile.toString() + "\n}");
        this.requestes = new Requestes(this.debug, this.socket, this.profile);
        this.poolDown = new PooledDownload(this.profile, this.requestes, this.debug);
        this.poolUp = new PooledUpload(this.profile.getMaxUpload(), this.profile.getUploadQueueLength(), this.profile.getBandwithUp(), this.debug);
        if (this.profile.getIsSP()) this.spModule = new SPModule(this, this.socket, debug);
        pooledTransferThread = new PooledTransfer(this, debug);
        Vector[] vect;
        this.cf.setFileName(this.pathConfiguration + File.separator + "download.list");
        vect = this.cf.getDownloadQueue();
        DownloadInfo di;
        if (vect != null) {
            while (vect[0].size() > 0) {
                di = (DownloadInfo) vect[0].remove(0);
                debug.println("Provider: reload download " + di.getIDString() + " in queue ,resource=" + di.getResource().getName() + "|" + di.getResource().getIDString());
                this.poolDown.addDownload(di);
            }
            while (vect[1].size() > 0) {
                di = (DownloadInfo) vect[1].remove(0);
                debug.println("Provider: reload download stopped " + di.getIDString() + " in queue ,resource=" + di.getResource().getName() + "|" + di.getResource().getIDString());
                this.poolDown.addDownloadStopped(di, false);
            }
            while (vect[2].size() > 0) {
                di = (DownloadInfo) vect[2].remove(0);
                debug.println("Provider: reload download terminated " + di.getIDString() + ", resource=" + di.getResource().getName() + "|" + di.getResource().getIDString() + ", path=" + di.getResource().getPath());
                this.poolDown.reloadResourceDownloaded(di);
            }
        }
        this.threadPing = new ThreadPing(this, this.socket, TIMEOUT_PING, this.debug);
        this.threadPing.start();
        this.setNodeState(ACTIVE);
    }

    /**
        * getSPModule
        *
        *
        * @version 1, last modified by A. Tamburo, 13/03/06
       */
    public SPModule getSPModule() {
        return this.spModule;
    }

    /**
        * disconnect: send the disconnection message
        *
        * @version 1, last modified by A. Tamburo, 22/11/05
        */
    private boolean disconnect() {
        if (this.state == REGISTRATION) {
            return false;
        }
        debug.println("Provider: disconnection from the network");
        try {
            Message msg = new Message(Message.DISCONNECT, Message.newID(this.profile.getAddr().getAddress()[0]));
            msg.setArea(this.profile.getGroup());
            msg.setIDNode(this.profile.getIDNode());
            DatagramPacket pkt = new DatagramPacket(msg.getBytes(), msg.getBytes().length);
            if (this.profile.getIsSP()) {
                this.spModule.sendDisconnectMessage(pkt);
            } else {
                this.sendTo(pkt, this.profile.getGroup().getSPSocketAddress());
            }
            this.clearAll();
            return true;
        } catch (MessageException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
        * sendTo  
        * @version 1, last modified by A. Tamburo, 21/11/05
        */
    private void sendTo(DatagramPacket pkt, SocketAddress receiver) {
        try {
            pkt.setSocketAddress(receiver);
            socket.send(pkt);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
        * Start the registration phase
        *
        * @version 1, last modified by A. Tamburo, 29/11/05
        */
    public void startReboot() {
        this.threadPing.stopThread();
        this.setNodeState(REGISTRATION);
        debug.println("Provider: reboot");
        boolean boot = false;
        this.clearAll();
        this.cf.setFileName(this.pathConfiguration + File.separator + "download.list");
        debug.println("Provider: save download queue");
        this.cf.saveDownload(this.poolDown.stopAllDownload());
        this.poolDown.clearAllThread();
        this.poolUp.stopAllUpload();
        this.poolUp.clearAllThread();
        this.pooledTransferThread.stopAllThread();
        registration.setAddress(this.addrBootNode);
        registration.setPort(this.SSLportBootNode);
        while (this.running) {
            boot = registration.runRegistration2(this, this.profile);
            if (boot) break;
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
            }
        }
        this.initializeModule();
    }

    /**
        * getResourcesListSize
        *
        * @param boolean 
        * @return int
        * @version 1, last modified by A. Tamburo, 12/04/06
        */
    public int getResourcesListSize(byte type, boolean verified) {
        int size = -1;
        String query;
        String access_type = "";
        if (verified) {
            access_type = "(NetworkAccess_Type=1 OR NetworkAccess_Type=2)";
        } else access_type = "NetworkAccess_Type=1 ";
        if (type == Resources.PROJECT) {
            query = "select count(*) from project where " + access_type;
        } else if (type == Resources.RASTER_LAYER) {
            query = "select count(*) from  rasterlayer where " + access_type;
        } else if (type == Resources.FEATURE_LAYER) {
            query = "select count(*) from featurelayer where " + access_type;
        } else if (type == Resources.DATASET) {
            query = "select count(*) from dataset where " + access_type;
        } else {
            return size;
        }
        Statement stmt = null;
        ResultSet rs;
        Connection con = popConnection();
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            if (rs.next()) {
                size = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            size = 0;
        } finally {
            pushConnection(con);
        }
        return size;
    }

    /**
        * getResourcesList
        *
        *
        * @return int
        * @version 1, last modified by A. Tamburo, 03/05/06
        */
    public int getResourcesList(byte type, boolean verified, int page, TreeMap resources) {
        int size = 0;
        if (page <= 0) page = 1;
        int min_index = (page - 1) * PAGE_SIZE + 1;
        int max_index = (page) * PAGE_SIZE;
        String access_type = "";
        if (verified) {
            access_type = "NetworkAccess_Type=1 OR NetworkAccess_Type=2 ";
        } else access_type = "NetworkAccess_Type=1 ";
        String query = "";
        String nameCol = "";
        String idCol = "";
        if (type == Resources.PROJECT) {
            query = "select Proj_ID,Proj_Name from project where " + access_type + " ORDER BY Proj_Name";
            nameCol = "Proj_Name";
            idCol = "Proj_ID";
        } else if (type == Resources.RASTER_LAYER) {
            query = "select Raster_ID,Raster_Name,Proj_ID from  rasterlayer where " + access_type + " ORDER BY Raster_Name";
            nameCol = "Raster_Name";
            idCol = "Raster_ID";
        } else if (type == Resources.FEATURE_LAYER) {
            query = "select Feature_ID,Feature_Name,Proj_ID from featurelayer where " + access_type + " ORDER BY Feature_Name";
            nameCol = "Feature_Name";
            idCol = "Feature_ID";
        } else if (type == Resources.DATASET) {
            query = "select Dataset_ID,Dataset_Name from dataset where " + access_type + " ORDER BY Dataset_Name";
            nameCol = "Dataset_Name";
            idCol = "Dataset_ID";
        } else {
            return size;
        }
        Statement stmt = null;
        PreparedStatement stmt2 = null;
        ResultSet rs = null;
        ResultSet rsproj = null, rslayer = null;
        Connection con = popConnection();
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            int id;
            int idProj;
            int index = 0;
            String name, nameProj = "", nameLayer = "";
            Resources res;
            while (rs.next()) {
                nameProj = "";
                nameLayer = "";
                index++;
                if (index < min_index) {
                    pushConnection(con);
                    continue;
                }
                if (index > max_index) {
                    pushConnection(con);
                    break;
                }
                size++;
                id = rs.getInt(idCol);
                name = rs.getString(nameCol);
                res = new Resources();
                res.setID(Resources.sizeIntToBytes(id));
                res.setName(name);
                if (type == Resources.FEATURE_LAYER || type == Resources.RASTER_LAYER) {
                    idProj = rs.getInt("Proj_ID");
                    stmt2 = con.prepareStatement("select Proj_Name from project where Proj_ID= ?");
                    stmt2.setInt(1, idProj);
                    rsproj = stmt2.executeQuery();
                    if (rsproj.next()) {
                        nameProj = rsproj.getString("Proj_Name");
                    }
                }
                if (type == Resources.DATASET) {
                    stmt2 = con.prepareStatement("select Layer_ID,Feature_Name,Proj_Name from layerdataset,featurelayer,project where Dataset_ID=?" + " and layerdataset.Layer_ID=featurelayer.Feature_ID and featurelayer.Proj_ID=project.Proj_ID");
                    stmt2.setInt(1, id);
                    rslayer = stmt2.executeQuery();
                    if (rslayer.next()) {
                        nameProj = rslayer.getString("Proj_Name");
                        nameLayer = rslayer.getString("Feature_Name");
                    }
                }
                res.setNameProject(nameProj);
                res.setNameLayer(nameLayer);
                res.setType(type);
                resources.put(name + res.getIDString(), res);
            }
        } catch (Exception e) {
            e.printStackTrace();
            size = 0;
        } finally {
            pushConnection(con);
        }
        return size;
    }

    /**
        * getProjectInfo
        *
        * @param boolean 
        * @return Resources
        * @version 1, last modified by A. Tamburo, 16/12/05
        */
    public Resources getProjectInfo(byte[] id, boolean verified) {
        Resources res = null;
        String query = "";
        int idInt = (Resources.sizeBytesToInt(id));
        String access_type = "";
        if (verified) {
            access_type = "(NetworkAccess_Type=1 OR NetworkAccess_Type=2)";
        } else access_type = "NetworkAccess_Type=1";
        query = "select * from project where Proj_ID = ? AND " + access_type;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection con = popConnection();
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, idInt);
            rs = stmt.executeQuery();
            int gaulCode = 0, idProj;
            String name;
            Date last_update;
            while (rs.next()) {
                idProj = rs.getInt("Proj_ID");
                gaulCode = rs.getInt("Proj_Code");
                name = rs.getString("Proj_Name");
                try {
                    last_update = rs.getDate("Proj_LastUpdated");
                } catch (SQLException e) {
                    last_update = java.sql.Date.valueOf("1900-01-01");
                }
                synchronized (this.projectsExported) {
                    int size = 0;
                    res = (Resources) this.projectsExported.get(idProj);
                    if (res != null && last_update.compareTo(res.getLastUpdate()) > 0) {
                        this.projectsExported.remove(idProj);
                        res = null;
                    }
                    if (res == null) {
                        String filename = name + "_" + idProj + ".zip";
                        String dirname = this.profile.getUpload_GlobalPath() + File.separatorChar + name + dateformat.format(new Date()) + "_" + idProj;
                        File dir = new File(dirname);
                        String tmp = "_";
                        while (dir.exists()) {
                            tmp += "_";
                            dirname = this.profile.getUpload_GlobalPath() + File.separatorChar + name + dateformat.format(new Date()) + tmp + idProj;
                            dir = new File(dirname);
                        }
                        dir.mkdir();
                        filename = dirname + File.separatorChar + filename;
                        File file = new File(filename);
                        size = ExportUtility.exportProject(this.profile.getURLPath(), gaulCode, file);
                        if (size > 0) {
                            byte[] digest = ExportUtility.generateDigest(filename);
                            res = new Resources();
                            res.setID(Resources.sizeIntToBytes(idProj));
                            res.setLastUpdate(last_update);
                            res.setType(Resources.PROJECT);
                            res.setName(name);
                            res.setSize(size);
                            res.setPath(filename);
                            res.setDigest(digest);
                            this.projectsExported.put(idProj, res);
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pushConnection(con);
        }
        return res;
    }

    /**
        * getFeatureLayerInfo
        *
        * @return Resources
        * @version 1, last modified by A. Tamburo, 11/06/01
        */
    public Resources getFeatureLayerInfo(byte[] id, boolean verified) {
        Resources res = null;
        String query = "";
        int idInt = (Resources.sizeBytesToInt(id));
        String access_type = "";
        if (verified) {
            access_type = "(NetworkAccess_Type=1 OR NetworkAccess_Type=2)";
        } else access_type = "NetworkAccess_Type=1";
        query = "select * from featurelayer where Feature_ID = ? AND " + access_type;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        ResultSet rs = null;
        ResultSet rsproj = null;
        Connection con = popConnection();
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, idInt);
            rs = stmt.executeQuery();
            int code;
            String name, nameProj = "";
            int idProj;
            Date last_update;
            while (rs.next()) {
                code = rs.getInt("Feature_ID");
                idProj = rs.getInt("Proj_ID");
                name = rs.getString("Feature_Name");
                try {
                    last_update = rs.getDate("Feature_LastUpdated");
                } catch (SQLException e) {
                    last_update = java.sql.Date.valueOf("1900-01-01");
                }
                synchronized (this.featureLayersExported) {
                    res = (Resources) this.featureLayersExported.get(code);
                    int size;
                    if (res != null && last_update.compareTo(res.getLastUpdate()) > 0) {
                        this.featureLayersExported.remove(code);
                        res = null;
                    }
                    if (res == null) {
                        String filename = name + "_" + code + ".zip";
                        String dirname = this.profile.getUpload_GlobalPath() + File.separatorChar + name + dateformat.format(new Date()) + "_" + code;
                        File dir = new File(dirname);
                        String tmp = "_";
                        while (dir.exists()) {
                            tmp += "_";
                            dirname = this.profile.getUpload_GlobalPath() + File.separatorChar + name + dateformat.format(new Date()) + tmp + code;
                            dir = new File(dirname);
                        }
                        dir.mkdir();
                        filename = dirname + File.separatorChar + filename;
                        File file = new File(filename);
                        ExportUtility ex_ut = new ExportUtility(this.database_ini);
                        size = ex_ut.exportLayer(this.pathConfiguration, file, Resources.FEATURE_LAYER, code);
                        if (size != -1) {
                            byte[] digest = ExportUtility.generateDigest(filename);
                            res = new Resources();
                            res.setID(Resources.sizeIntToBytes(code));
                            res.setType(Resources.FEATURE_LAYER);
                            res.setName(name);
                            stmt2 = con.prepareStatement("select Proj_Name from project where Proj_ID=?");
                            stmt2.setInt(1, idProj);
                            rsproj = stmt2.executeQuery();
                            if (rsproj.next()) {
                                nameProj = rsproj.getString("Proj_Name");
                            }
                            res.setNameProject(nameProj);
                            res.setSize(size);
                            res.setLastUpdate(last_update);
                            res.setPath(filename);
                            res.setDigest(digest);
                            this.featureLayersExported.put(code, res);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pushConnection(con);
        }
        return res;
    }

    /**
         * getRasterLayerInfo
         *
         * @return Resources
         * @version 1, last modified by A. Tamburo, 12/06/01
         */
    public Resources getRasterLayerInfo(byte[] id, boolean verified) {
        Resources res = null;
        String query = "";
        int idInt = (Resources.sizeBytesToInt(id));
        String access_type = "";
        if (verified) {
            access_type = "(NetworkAccess_Type=1 OR NetworkAccess_Type=2)";
        } else access_type = "NetworkAccess_Type=1";
        query = "select * from rasterlayer where Raster_ID = ? AND " + access_type;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        ResultSet rs = null;
        ResultSet rsproj = null;
        Connection con = popConnection();
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, idInt);
            rs = stmt.executeQuery();
            int code;
            String name, nameProj = "";
            int idProj;
            Date last_update;
            while (rs.next()) {
                code = rs.getInt("Raster_ID");
                idProj = rs.getInt("Proj_ID");
                name = rs.getString("Raster_Name");
                try {
                    last_update = rs.getDate("Raster_LastUpdated");
                } catch (SQLException e) {
                    last_update = java.sql.Date.valueOf("1900-01-01");
                }
                synchronized (this.rasterLayersExported) {
                    res = (Resources) this.rasterLayersExported.get(code);
                    int size;
                    if (res != null && last_update.compareTo(res.getLastUpdate()) > 0) {
                        this.rasterLayersExported.remove(code);
                        res = null;
                    }
                    if (res == null) {
                        String filename = name + "_" + code + ".zip";
                        String dirname = this.profile.getUpload_GlobalPath() + File.separatorChar + name + dateformat.format(new Date()) + "_" + code;
                        File dir = new File(dirname);
                        String tmp = "_";
                        while (dir.exists()) {
                            tmp += "_";
                            dirname = this.profile.getUpload_GlobalPath() + File.separatorChar + name + dateformat.format(new Date()) + tmp + code;
                            dir = new File(dirname);
                        }
                        dir.mkdir();
                        filename = dirname + File.separatorChar + filename;
                        File file = new File(filename);
                        ExportUtility ex_ut = new ExportUtility(this.database_ini);
                        size = ex_ut.exportLayer(this.pathConfiguration, file, Resources.RASTER_LAYER, code);
                        if (size > 0) {
                            byte[] digest = ExportUtility.generateDigest(filename);
                            res = new Resources();
                            res.setID(Resources.sizeIntToBytes(code));
                            res.setLastUpdate(last_update);
                            res.setType(Resources.RASTER_LAYER);
                            res.setName(name);
                            stmt2 = con.prepareStatement("select Proj_Name from project where Proj_ID=?");
                            stmt2.setInt(1, idProj);
                            rsproj = stmt2.executeQuery();
                            if (rsproj.next()) {
                                nameProj = rsproj.getString("Proj_Name");
                            }
                            res.setNameProject(nameProj);
                            res.setSize(size);
                            res.setPath(filename);
                            res.setDigest(digest);
                            this.rasterLayersExported.put(code, res);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pushConnection(con);
        }
        return res;
    }

    /**
         * getRasterLayerInfo
         *
         * @return Resources
         * @version 1, last modified by A. Tamburo, 12/06/01
         */
    public Resources getDatasetInfo(byte[] id, boolean verified) {
        Resources res = null;
        String query = "";
        int idInt = (Resources.sizeBytesToInt(id));
        String access_type = "";
        if (verified) {
            access_type = "(NetworkAccess_Type=1 OR NetworkAccess_Type=2)";
        } else access_type = "NetworkAccess_Type=1";
        query = "select * from dataset where Dataset_ID = ? AND " + access_type;
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        ResultSet rs = null;
        ResultSet rslayer = null;
        Connection con = popConnection();
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, idInt);
            rs = stmt.executeQuery();
            int code;
            String name, layerName = "", projectName = "";
            int id_layer = 0;
            Date last_update;
            while (rs.next()) {
                layerName = "";
                projectName = "";
                code = rs.getInt("Dataset_ID");
                name = rs.getString("Dataset_Name");
                try {
                    last_update = rs.getDate("Dataset_LastUpdated");
                } catch (SQLException e) {
                    last_update = java.sql.Date.valueOf("1900-01-01");
                }
                synchronized (this.datasetExported) {
                    res = (Resources) this.datasetExported.get(code);
                    int size;
                    if (res != null && last_update.compareTo(res.getLastUpdate()) > 0) {
                        this.datasetExported.remove(code);
                        res = null;
                    }
                    if (res == null) {
                        String filename = name + "_" + code + ".zip";
                        String dirname = this.profile.getUpload_GlobalPath() + File.separatorChar + name + dateformat.format(new Date()) + "_" + code;
                        File dir = new File(dirname);
                        String tmp = "_";
                        while (dir.exists()) {
                            tmp += "_";
                            dirname = this.profile.getUpload_GlobalPath() + File.separatorChar + name + dateformat.format(new Date()) + tmp + code;
                            dir = new File(dirname);
                        }
                        dir.mkdir();
                        filename = dirname + File.separatorChar + filename;
                        File file = new File(filename);
                        size = ExportUtility.exportDataset(this.profile.getURLPath(), file, code);
                        if (size != -1) {
                            byte[] digest = ExportUtility.generateDigest(filename);
                            res = new Resources();
                            res.setID(Resources.sizeIntToBytes(code));
                            res.setLastUpdate(last_update);
                            res.setType(Resources.DATASET);
                            res.setName(name);
                            stmt2 = con.prepareStatement("select Layer_ID,Feature_Name,Proj_Name from layerdataset,featurelayer,project where Dataset_ID=?" + " and layerdataset.Layer_ID=featurelayer.Feature_ID and featurelayer.Proj_ID=project.Proj_ID");
                            stmt2.setInt(1, idInt);
                            rslayer = stmt2.executeQuery();
                            if (rslayer.next()) {
                                projectName = rslayer.getString("Proj_Name");
                                layerName = rslayer.getString("Feature_Name");
                                id_layer = rslayer.getInt("Layer_ID");
                            }
                            res.setNameProject(projectName);
                            res.setNameLayer(layerName);
                            res.setIDLayer(id_layer);
                            res.setSize(size);
                            res.setPath(filename);
                            res.setDigest(digest);
                            this.datasetExported.put(code, res);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pushConnection(con);
        }
        return res;
    }

    /**
        * popConnection
        *
        * @return Connection
        * @version 1, last modified by A. Tamburo, 16/12/05
        */
    private Connection popConnection() {
        dbConnectionManager manager = dbConnectionManagerPool.getConnectionManager(database_ini);
        Connection con = manager.popConnection();
        return con;
    }

    /**
        * pushConnection
        *
        * @param Connection
        * @version 1, last modified by A. Tamburo, 16/12/05
        */
    private void pushConnection(Connection con) {
        dbConnectionManager manager = dbConnectionManagerPool.getConnectionManager(database_ini);
        manager.pushConnection(con);
    }

    /**
        * getIDBootNode
        *
        * @return byte[]
        * @version 1, last modified by A. Tamburo, 11/11/05
       */
    public byte[] getIDBootNode() {
        return this.idBootNode;
    }

    /**
        * setIDBootNode
        *
        * @version 1, last modified by A. Tamburo, 19/04/06
       */
    public void setIDBootNode(byte[] id) {
        this.idBootNode = new byte[Profile.ID_LENGTH];
        if (id.length != Profile.ID_LENGTH) return;
        for (int i = 0; i < Profile.ID_LENGTH; i++) {
            this.idBootNode[i] = id[i];
        }
    }

    /**
        * getLastTimePing
        *
        * @return long
        * @version 1, last modified by A. Tamburo, 11/11/05
       */
    public long getLastTimePing() {
        return this.lastTimePing;
    }

    /**
        * setLastTimePing
        *
        * @param long
        * @version 1, last modified by A. Tamburo, 11/11/05
       */
    public void setLastTimePing(long ltp) {
        this.lastTimePing = ltp;
    }

    /**
       * addTransferResourcesList
       *     aggiunge un nuovo trasferimento della lista di risorse condivise
       *
       */
    protected void addTransferResourcesList(java.io.DataInputStream in, java.io.DataOutputStream out, byte type, boolean verified) {
        TransferInfo ti = new TransferInfo();
        ti.setType(TransferInfo.TRANSFER_RESOURCES);
        ti.setInputStream(in);
        ti.setOutputStream(out);
        ti.setTypeResource(type);
        ti.setVerifiedNode(verified);
        pooledTransferThread.addTransfer(ti);
    }

    /**
       * addTransferResourceInfo
       * aggiunge un nuovo trasferimento sulle informazioni riguardanti una
       * risorsa
       *
       */
    protected void addTransferResourceInfo(java.io.DataInputStream in, java.io.DataOutputStream out, byte type, byte[] IDResource, boolean verified) {
        TransferInfo ti = new TransferInfo();
        ti.setType(TransferInfo.TRANSFER_RESOURCEINFO);
        ti.setInputStream(in);
        ti.setOutputStream(out);
        ti.setTypeResource(type);
        ti.setIdRes(IDResource);
        ti.setVerifiedNode(verified);
        pooledTransferThread.addTransfer(ti);
    }

    /**
        * addTransferGroupsList
        * aggiunge un nuovo trasferimento della lista di gruppi condivise
        *
        * @param  InetAddress addr
        * @param  int tcpPort
        * @param  DataInputStream in
        * @param  DataOutputStream out
       */
    protected void addTransferGWsList(java.io.DataInputStream in, java.io.DataOutputStream out) {
        TransferInfo ti = new TransferInfo();
        ti.setType(TransferInfo.TRANSFER_GWS);
        ti.setInputStream(in);
        ti.setOutputStream(out);
        pooledTransferThread.addTransfer(ti);
    }

    /**
        * addDownload 
        *
        * @param  DownloadInfo di
        */
    public boolean addDownload(DownloadInfo di) {
        String filename = di.getResource().getName() + "-" + Resources.sizeBytesToInt(di.getResource().getID()) + ".zip";
        String dirname = this.profile.getDownload_GlobalPath() + File.separatorChar + di.getResource().getName() + dateformat.format(new Date()) + "d" + Resources.sizeBytesToInt(di.getResource().getID());
        File dir = new File(dirname);
        String tmp = "d";
        while (dir.exists()) {
            tmp += "d";
            dirname = this.profile.getDownload_GlobalPath() + File.separatorChar + di.getResource().getName() + dateformat.format(new Date()) + tmp + Resources.sizeBytesToInt(di.getResource().getID());
            dir = new File(dirname);
        }
        dir.mkdir();
        di.getResource().setPath(dirname + File.separatorChar + filename);
        di.setByteResidue(di.getResource().getSize());
        di.setStartDate(System.currentTimeMillis());
        if (this.poolDown.addDownload(di)) {
            debug.println("Provider: add download " + di.getIDString() + " in queue ,resource=" + di.getResource().getName() + "/" + di.getResource().getIDString());
            return true;
        }
        debug.println("Provider: download " + di.getResource().getName() + " / " + di.getResource().getIDString() + " not inserted in queue");
        return false;
    }

    /**
        * addPushDownload
        *
        * @param  DownloadInfo di
        */
    public boolean addPushDownload(DownloadInfo di) {
        debug.println("Provider: receveid push download request, " + " resource=" + di.getResource().getName() + "/" + di.getResource().getIDString() + ", from " + di.getAddrSource() + ":" + di.getUDPPortSource());
        String filename = di.getResource().getName() + "-" + Resources.sizeBytesToInt(di.getResource().getID()) + ".zip";
        String dirname = this.profile.getDownload_GlobalPath() + File.separatorChar + di.getResource().getName() + dateformat.format(new Date()) + "d" + Resources.sizeBytesToInt(di.getResource().getID());
        File dir = new File(dirname);
        String tmp = "d";
        while (dir.exists()) {
            tmp += "d";
            dirname = this.profile.getDownload_GlobalPath() + File.separatorChar + di.getResource().getName() + dateformat.format(new Date()) + tmp + Resources.sizeBytesToInt(di.getResource().getID());
            dir = new File(dirname);
        }
        dir.mkdir();
        di.getResource().setPath(dirname + File.separatorChar + filename);
        di.setByteResidue(di.getResource().getSize());
        di.setStartDate(System.currentTimeMillis());
        di.setUserID("" + util.getIDGuest());
        if (this.poolDown.addDownload(di)) {
            debug.println("Provider Push Download: add download " + di.getIDString() + " in queue ,resource=" + di.getResource().getName() + "/" + di.getResource().getIDString());
            return true;
        }
        debug.println("Provider Push Download: download " + di.getResource().getName() + "/" + di.getResource().getIDString() + " not inserted in queue");
        return false;
    }

    /**
        * restartDownload 
        *
        * @param  DownloadInfo di
        */
    public boolean restartDownload(byte[] ID) {
        if (this.poolDown.restartDownload(ID)) {
            debug.println("Provider: restart download " + DownloadInfo.idByteToString(ID));
            return true;
        }
        debug.println("Provider: can't to restart download " + DownloadInfo.idByteToString(ID));
        return false;
    }

    /**
        * addUpload 
        *
        * @param  DownloadInfo di
        */
    public boolean addUpload(DownloadInfo di, boolean verified) {
        byte type = di.getResource().getType();
        Resources resToUp = di.getResource();
        Resources resExported;
        if (type == Resources.PROJECT) {
            resExported = this.getProjectInfo(resToUp.getID(), verified);
        } else if (type == Resources.FEATURE_LAYER) {
            resExported = this.getFeatureLayerInfo(resToUp.getID(), verified);
        } else if (type == Resources.RASTER_LAYER) {
            resExported = this.getRasterLayerInfo(resToUp.getID(), verified);
        } else if (type == Resources.DATASET) {
            resExported = this.getDatasetInfo(resToUp.getID(), verified);
        } else {
            return false;
        }
        if (resExported == null) return false;
        if (!Resources.matchingDigest(di.getResource().getDigest(), resExported.getDigest())) {
            return false;
        }
        di.setResource(resExported);
        if (this.poolUp.addUpload(di)) {
            debug.println("Provider: add upload " + di.getResource().getName() + "/" + di.getResource().getIDString() + " in queue");
        } else return false;
        return true;
    }

    /**
        * isUploadInQueue 
        *
        * @param  byte[] IDUpload
        */
    public boolean isUploadInQueue(byte[] IDUpload) {
        return this.poolUp.isUploadInQueue(IDUpload);
    }

    /**
        * getDownloadQueueMy 
        *
        * @param userID: user id
        * @param userType: MY or OTHERS
        *
        */
    public Vector getDownloadQueue(String userID, byte userType) {
        if (userType == ApiModule.MY) return this.poolDown.getDownloadQueueMy(userID); else return this.poolDown.getDownloadQueueOthers(userID);
    }

    /**
         *  getDownloadStopped
          * @param userID: user id
        * @param userType: MY or OTHERS
        */
    public Vector getDownloadStopped(String userID, byte userType) {
        if (userType == ApiModule.MY) return this.poolDown.getDownloadStoppedMy(userID); else return this.poolDown.getDownloadStoppedOthers(userID);
    }

    /**
         *  getDownloadActive
          * @param userID: user id
        * @param userType: MY or OTHERS
        *
        */
    public Vector getDownloadActive(String userID, byte userType) {
        if (userType == ApiModule.MY) return this.poolDown.getDownloadActiveMy(userID); else return this.poolDown.getDownloadActiveOthers(userID);
    }

    /**
        * getUploadQueue 
        *
        */
    public Vector getUploadQueue() {
        return this.poolUp.getUploadQueue();
    }

    /**
        * ggetUploadActive
        *
        */
    public Vector getUploadActive() {
        return this.poolUp.getUploadActive();
    }

    /**
       * deleteDownloadFromQueue
       *
       * @version 1, last modified by A. Tamburo, 18/01/06
       */
    public boolean deleteDownloadFromQueue(byte[] ID) {
        boolean res = this.poolDown.deleteFromQueue(ID);
        if (res == true) {
            debug.println("Provider: remove download from queue " + DownloadInfo.idByteToString(ID));
        }
        return res;
    }

    /**
       * stopDownload
       *
       * @version 1, last modified by A. Tamburo, 18/01/06
       */
    public boolean stopDownload(byte[] ID) {
        boolean res = this.poolDown.stopDownload(ID);
        if (res == true) {
            debug.println("Provider: stop download " + DownloadInfo.idByteToString(ID));
            return true;
        }
        return res;
    }

    /**
       * deleteDownload
       *
       * @version 1, last modified by A. Tamburo, 31/01/06
       */
    public boolean deleteDownload(byte[] ID) {
        boolean res = this.poolDown.deleteDownload(ID);
        if (res == true) {
            debug.println("Provider: delete download " + DownloadInfo.idByteToString(ID));
        }
        return res;
    }

    /**
       * deleteUploadFromQueue
       *
       * @version 1, last modified by A. Tamburo, 19/01/06
       */
    public boolean deleteUploadFromQueue(byte[] ID) {
        boolean res = this.poolUp.deleteFromQueue(ID);
        if (res == true) {
            debug.println("Provider: remove upload from queue " + DownloadInfo.idByteToString(ID));
        }
        return res;
    }

    /**
       * deleteDownloadStopped
       *
       * @version 1, last modified by A. Tamburo, 1/02/06
       */
    public boolean deleteDownloadStopped(byte[] ID) {
        boolean res = this.poolDown.deleteDownloadStopped(ID);
        if (res == true) {
            debug.println("Provider: remove download stopped " + DownloadInfo.idByteToString(ID));
        }
        return res;
    }

    /**
       * deleteResourceDownloaded
       *
       * @version 1, last modified by A. Tamburo, 6/03/06
       */
    public boolean deleteResourceDownloaded(byte[] ID) {
        boolean res = this.poolDown.deleteResourceDownloaded(ID);
        if (res == true) {
            debug.println("Provider: remove resource downloaded " + DownloadInfo.idByteToString(ID));
        }
        return res;
    }

    /**
       * stopDownload
       *
       * @version 1, last modified by A. Tamburo, 19/01/06
       */
    public boolean stopUpload(byte[] ID) {
        boolean res = this.poolUp.stopUpload(ID);
        if (res == true) {
            debug.println("ProviderClass: stop upload " + DownloadInfo.idByteToString(ID));
        }
        return res;
    }

    /**
        * addPongDownload
        *
        * @param byte[] idPong
        * @param byte[] idClient
        * @version 1, last modified by A. Tamburo, 21/10/05
        */
    public void addPongDownload(byte[] idPong, byte[] idDownload) {
        debug.println("ExceuteRequest: pong received");
        this.poolDown.addPongDownload(idPong, idDownload);
    }

    /**
        * getDownloadStopped 
        */
    public Vector getResourcesDownloaded(byte type, String userID) {
        return this.poolDown.getResourcesDownloaded(type, userID);
    }

    /**
        * getDownloadStopped 
        */
    public Vector getResourcesDownloadedByOtherUser(byte type, String userID) {
        return this.poolDown.getResourcesDownloadedByOtherUser(type, userID);
    }

    /**
        * getResourceDownloaded 
        *
        */
    public DownloadInfo getResourceDownloaded(byte[] id) {
        return this.poolDown.getResourceDownloaded(id);
    }

    /**
        * clearAll 
        *
        * @param  int ID
        * @return boolean
        */
    protected void clearAll() {
        if (this.profile.getIsSP()) this.spModule.clearAll();
        this.profile.setIsSP(false);
    }

    /**
        * setState 
        *
        * @param byte
        * @return  boolean
        */
    public boolean setNodeState(byte s) {
        this.state = s;
        return true;
    }

    /**
        * getNodeState 
        *
        * @return byte
        */
    public byte getNodeState() {
        return this.state;
    }

    /**
        * getAddressBootNode 
        *
        * @return InetAddress
        */
    public InetAddress getAddressBootNode() {
        return this.addrBootNode;
    }

    /**
        * getUDPPortBootNode 
        *
        * @return int
        */
    public int getUDPPortBootNode() {
        return this.UDPportBootNode;
    }

    /**
        * getTCPPortBootNode 
        *
        * @return int
        */
    public int getTCPPortBootNode() {
        return this.TCPportBootNode;
    }

    /**
        * getSSLPortBootNode 
        *
        * @return int
        */
    public int getSSLPortBootNode() {
        return this.SSLportBootNode;
    }

    /**
        * setAddressBootNode
        *
        * @param InetAddress
        */
    public void setAddressBootNode(InetAddress addr) {
        this.addrBootNode = addr;
    }

    /**
        * setUDPPortBootNode 
        *
        * @param byte
        */
    public void setUDPPortBootNode(int p) {
        this.UDPportBootNode = p;
    }

    /**
        * setTCPPortBootNode 
        *
        * @param byte
        */
    public void setTCPPortBootNode(int p) {
        this.TCPportBootNode = p;
    }

    /**
        * setSSLPortBootNode 
        *
        * @param byte
        */
    public void setSSLPortBootNode(int p) {
        this.SSLportBootNode = p;
    }

    /**
        * saveConfiguration
        *
        * @param String filename
        * @return boolean
        * @version 1, last modified by A. Tamburo, 5/12/05
        */
    public boolean saveConfiguration(String filename) {
        if (this.getNodeState() != CONFIGURATION && this.getNodeState() != INITIALSTATE) {
            return false;
        }
        try {
            if (filename != null) cf.setFileName(filename);
            cf.save(this);
        } catch (ConfigurationException e) {
            debug.println("Provider: error while save configuration, " + e.getMessage());
            return false;
        }
        debug.println("Provider: save configuration");
        return true;
    }

    /**
        * exit
        *
        * @version 1, last modified by A. Tamburo, 5/12/05
        */
    public boolean exit() {
        if (this.getNodeState() != CONFIGURATION) {
            return false;
        }
        debug.println("Provider: save download queue");
        this.disconnect();
        this.running = false;
        this.threadPing.stopThread();
        this.cf.setFileName(this.pathConfiguration + File.separator + "download.list");
        Vector[] v = this.poolDown.stopAllDownload();
        this.cf.saveDownload(v);
        debug.println("Provider: save download queue");
        this.poolDown.clearAllThread();
        this.poolUp.stopAllUpload();
        this.poolUp.clearAllThread();
        this.pooledTransferThread.stopAllThread();
        this.tcpListen.stopThread();
        this.sslListen.stopThread();
        this.socket.close();
        this.poolRequestes.clearAllThread(PooledRequestesUDP.PROVIDER_CLASS);
        debug.println("Provider: stop");
        return true;
    }

    /**
        * closeSocket()
        *
        * @version 1, last modified by A. Tamburo, 5/12/05
        */
    public void closeSocket() {
        if (this.socket != null) this.socket.close();
        if (this.tcpListen != null) this.tcpListen.stopThread();
        if (this.sslListen != null) this.sslListen.stopThread();
    }

    /**
        * exitRegistration()
        *
        * @version 1, last modified by A. Tamburo, 5/12/05
        */
    public void exitRegistration() {
        this.running = false;
        this.closeSocket();
        if (this.threadPing != null && this.threadPing.isAlive()) {
            this.threadPing.stopThread();
        }
        debug.println("Provider: stop registration");
    }

    /**
        * sendPushDownload
        *
        * @version 1, last modified by A. Tamburo, 02/02/06
       */
    public boolean sendPushDownload(byte[] id, byte type, InetAddress rec, int port) {
        Message msg;
        DatagramPacket pkt;
        debug.println("ProviderClass: send push download request to " + rec.getCanonicalHostName() + ":" + port);
        try {
            Resources res = null;
            if (type == Resources.PROJECT) res = this.getProjectInfo(id, true);
            if (type == Resources.DATASET) res = this.getDatasetInfo(id, true);
            if (type == Resources.FEATURE_LAYER) res = this.getFeatureLayerInfo(id, true);
            if (type == Resources.RASTER_LAYER) res = this.getRasterLayerInfo(id, true);
            if (res == null) {
                debug.println("ProviderClass: impossible send push download request, resource " + Resources.idByteToString(id) + " doesn't exist");
                return false;
            }
            msg = new Message(Message.PUSHDOWNLOAD, Message.newID((byte) 10));
            msg.setIDResource(res.getID());
            msg.setResourceType(res.getType());
            msg.setResourceDigest(res.getDigest());
            msg.setResourceSize(res.getSize());
            msg.setDesc(res.getName());
            if (type != Resources.PROJECT) msg.setNameProject(res.getNameProject());
            if (type == Resources.DATASET) msg.setNameLayer(res.getNameLayer());
            msg.setAddress(this.profile.getAddr());
            msg.setUDPPort(this.profile.getUDPPort());
            msg.setTCPPort(this.profile.getTCPPort());
            pkt = new DatagramPacket(msg.getBytes(), msg.getBytes().length);
            this.sendTo(pkt, new InetSocketAddress(rec, port));
            return true;
        } catch (MessageException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
        * testDirectory
        * 
        * @version 1, last modified by A. Tamburo, 6/3/06
        */
    public void testDirectory() {
        File dirUpload = new File(this.profile.getUpload_GlobalPath());
        if (!dirUpload.exists()) {
            dirUpload.mkdir();
        }
        File files[] = dirUpload.listFiles();
        File dirs[];
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    dirs = files[i].listFiles();
                    if (dirs != null) {
                        for (int j = 0; j < dirs.length; j++) {
                            dirs[j].delete();
                        }
                    }
                }
                files[i].delete();
            }
        }
        File dirDownload = new File(this.profile.getDownload_GlobalPath());
        if (!dirDownload.exists()) {
            dirDownload.mkdir();
        }
        files = dirDownload.listFiles();
        boolean delete_dir = true;
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    delete_dir = true;
                    dirs = files[i].listFiles();
                    if (dirs != null) {
                        for (int j = 0; j < dirs.length; j++) {
                            try {
                                if (this.poolDown.isUsed(dirs[j].getPath())) {
                                    delete_dir = false;
                                }
                            } catch (Exception e) {
                                delete_dir = false;
                            }
                        }
                        if (delete_dir) {
                            for (int j = 0; j < dirs.length; j++) {
                                dirs[j].delete();
                            }
                        }
                    }
                }
                files[i].delete();
            }
        }
    }

    /**
       * getRequestes
       *
       * @version 1, last modified by A. Tamburo, 19/04/06
       */
    public Requestes getRequestes() {
        return this.requestes;
    }

    /**
         * getDebug()
         *
         * @return MyDubeg
         * @version 1, last modified by A. Tamburo, 29/05/06
       */
    public MyDebug getDebug() {
        return this.debug;
    }

    /**
        *
        * @version 1, last modified by A. Tamburo, 02/11/06
      */
    public void addQueryToPool(String query, DataInputStream in, DataOutputStream out) {
        TransferInfo ti = new TransferInfo();
        ti.setType(TransferInfo.EXECUTE_QUERY);
        ti.setQuery(query);
        ti.setInputStream(in);
        ti.setOutputStream(out);
        pooledTransferThread.addTransfer(ti);
    }

    /**
      *
      * @version 1, last modified by A. Tamburo, 02/11/06
    */
    public HashMap executeQuery(String query) {
        HashMap result;
        synchronized (this.engine) {
            result = this.engine.executeQuery(query);
        }
        return result;
    }

    public boolean checkFeatureLayer(int idLayer) {
        System.out.println("Check Layer " + idLayer);
        String query = "select count(*) from featurelayer where Feature_ID = ?";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection con = popConnection();
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, idLayer);
            rs = stmt.executeQuery();
            int num = 0;
            if (rs.next()) {
                num = rs.getInt(1);
            }
            if (num > 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pushConnection(con);
        }
        return false;
    }
}
