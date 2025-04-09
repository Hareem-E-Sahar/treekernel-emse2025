package com.rbnb.api;

import java.util.Random;
import com.rbnb.compat.File;

final class RBNB extends com.rbnb.api.PeerServer implements com.rbnb.api.ServerHandler {

    /**
     * version 2.1 of the RBNB for compatibility mode.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/19/2003
     */
    public static final String VERSION_V2_1 = "V2.1";

    /**
     * version 2.2 of the RBNB for compatibility mode.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/19/2003
     */
    public static final String VERSION_V2_2 = "V2.2";

    /**
     * current version of the RBNB for compatibility mode.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/19/2003
     */
    public static final String VERSION_CURRENT = VERSION_V2_2;

    /**
     * compatibility versions supported.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/19/2003
     */
    public static final String[] COMPATIBLE_VERSIONS = { VERSION_V2_1, VERSION_V2_2 };

    /**
     * activity queue to provide one or more threads to handle events.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/13/2003
     */
    private ActionThreadQueue activityQueue = null;

    /**
     * archive home directory (can be set via "-H" command line flag)
     * <p>
     *
     * @author John Wilson
     *
     * @since V2.6
     * @version 06/22/2006
     */
    private String archiveHomeDirectory = ".";

    /**
     * automatically load archives in the current directory?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 04/03/2003
     */
    private boolean autoLoadArchives = false;

    /**
     * the client side <code>Server</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/26/2001
     */
    private Server clientSide = null;

    /**
      * Stores the values entered with the -P flag, or null.
      * @author WHF
      * @since V2.5
      * @version 2005/01/20
      */
    private Username username;

    /**
     * compatibility mode.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/19/2003
     */
    private String compatibilityMode = VERSION_CURRENT;

    /**
     * the current number of connections.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/17/2003
     */
    private int currentConnections = 0;

    /**
     * the debug level.
     * <p>
     *
     * @author Ian Brown
     *
     * @see com.rbnb.api.Log
     * @see #debugMask
     * @since V2.0
     * @version 08/03/2004
     */
    private byte debugLevel = (byte) Log.STANDARD;

    /**
     * the debug mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @see com.rbnb.api.Log
     * @see #debugLevel
     * @since V2.0
     * @version 01/30/2002
     */
    private long debugMask = Log.NONE;

    /**
     * the log for this RBNB server.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private Log log = null;

    /**
     * the period for logging current status.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/25/2002
     */
    private long logStatusPeriod = TimerPeriod.LOG_STATUS;

    /**
     * the maximum number of activity threads.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/13/2003
     */
    private int maxActivityThreads = 100;

    /**
     * the maximum number of connections allowed.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.2
     * @version 11/17/2003
     */
    private int maximumConnections = 100;

    /**
     * the maximum number of open filesets.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/13/2003
     */
    private int maxOpenFileSets = 25;

    /**
     * metrics <code>Rmap</code> hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/19/2002
     */
    private Rmap metrics = null;

    /**
     * metrics: number of bytes transferred.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/19/2002
     */
    private long metricsBytes = 0;

    /**
     * the period for collecting metrics.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/25/2002
     */
    private long metricsPeriod = TimerPeriod.METRICS;

    /**
     * the <code>Source</code> to write metrics to.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/25/2002
     */
    private Source metricsSource = null;

    /**
     * limited resource: open filesets.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 03/13/2003
     */
    LimitedResource openFileSets = null;

    /**
     * the <code>Path</code> finding <code>Door</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/21/2001
     */
    private Door pathDoor = null;

    /**
     * the list of <code>Paths</code> to use to get to our peers.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/21/2001
     */
    private com.rbnb.utility.SortedVector paths = new com.rbnb.utility.SortedVector();

    /**
     * the <code>RAMServerCommunications</code> port.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/17/2001
     */
    private RAMServerCommunications ramPort = null;

    /**
     * list of <code>RemoteClientHandlers</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 12/03/2001
     */
    private RmapVector rClients = null;

    /**
     * Door for the <code>rClients</code> vector.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/15/2002
     */
    private Door rClientsDoor = null;

    /**
     * controls access to this <code>RBNB</code> by remote servers attempting
     * to initiate routes.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 04/24/2003
     */
    private Door routingDoor = null;

    /**
     * the server-port communications object.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/14/2001
     */
    private Object serverPort = null;

    /**
     * terminating client's name.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/14/2001
     */
    private String terminatingClient = null;

    /**
     * the thread running this <code>RBNB</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private Thread thread = null;

    /**
     * the task timer.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/04/2002
     */
    private Timer timer = null;

    RBNB() {
        super();
        try {
            setPathDoor(new Door(Door.READ_WRITE));
            rClientsDoor = new Door(Door.READ_WRITE);
            routingDoor = new Door(Door.STANDARD);
        } catch (java.lang.Exception e) {
        }
        setServerSide(this);
    }

    RBNB(String nameI, String addressI) throws com.rbnb.api.AddressException, com.rbnb.api.SerializeException, java.io.EOFException, java.io.InterruptedIOException, java.io.IOException, java.lang.InterruptedException {
        super(nameI, addressI);
        setPathDoor(new Door(Door.READ_WRITE));
        setServerSide(this);
        rClientsDoor = new Door(Door.READ_WRITE);
        routingDoor = new Door(Door.STANDARD);
    }

    RBNB(String nameI, Address addressHandlerI) throws com.rbnb.api.AddressException, com.rbnb.api.SerializeException, java.io.EOFException, java.io.InterruptedIOException, java.io.IOException, java.lang.InterruptedException {
        super(nameI);
        setPathDoor(new Door(Door.READ_WRITE));
        setAddressHandler(addressHandlerI);
        setServerSide(this);
        rClientsDoor = new Door(Door.READ_WRITE);
        routingDoor = new Door(Door.STANDARD);
    }

    public final void addRemoteClientHandler(RemoteClientHandler rchI) throws java.lang.InterruptedException {
        try {
            rClientsDoor.lock("RBNB.addRemoteClientHandler");
            rClients = RmapVector.addToVector(rClients, rchI);
        } finally {
            rClientsDoor.unlock();
        }
    }

    public final void broadcastUpdate(PeerUpdate peerUpdateI) {
        synchronized (getShortcuts()) {
            for (int idx = 0; idx < getShortcuts().size(); ++idx) {
                try {
                    Shortcut shortcut = (Shortcut) getShortcuts().elementAt(idx);
                    if (!shortcut.getDestinationName().equals(peerUpdateI.getPeerName())) {
                        Rmap rmap = getRoutingMapHandler().findDescendant(shortcut.getDestinationName(), false);
                        if (rmap != null) {
                            ((PeerServer) rmap).passUpdate(peerUpdateI);
                        }
                    }
                } catch (com.rbnb.api.AddressException e) {
                } catch (com.rbnb.api.SerializeException e) {
                } catch (java.io.IOException e) {
                } catch (java.lang.InterruptedException e) {
                }
            }
        }
    }

    public final void calculateDataSizes(long[] cacheDSIO, long[] archiveDSIO) {
        try {
            synchronized (metricsSyncObj) {
                for (int idx = 0; idx < getNchildren(); ++idx) {
                    if (getChildAt(idx) instanceof DataSizeMetricsInterface) {
                        DataSizeMetricsInterface dsm = (DataSizeMetricsInterface) getChildAt(idx);
                        dsm.calculateDataSizes(cacheDSIO, archiveDSIO);
                    }
                }
            }
        } catch (java.lang.Exception e) {
        }
    }

    public final Rmap calculateMetrics(long lastTimeI, long nowI) {
        Rmap rmapR = null;
        try {
            if (metrics == null) {
                metrics = new Rmap();
                metrics.addChild(new Rmap("TotalMemory"));
                metrics.addChild(new Rmap("MemoryUsed"));
                metrics.addChild(new Rmap("SocketBytes"));
                metrics.addChild(new Rmap("SocketRate"));
                metrics.addChild(new Rmap("CacheDataBytes"));
                metrics.addChild(new Rmap("ArchiveDataBytes"));
            }
            rmapR = metrics;
            double duration = 1.;
            if (lastTimeI == Long.MIN_VALUE) {
                rmapR.setTrange(new TimeRange(nowI / 1000. - duration, duration));
            } else {
                duration = (nowI - lastTimeI - 1) / 1000.;
                if (duration < 0.) {
                    duration = 0.;
                }
                rmapR.setTrange(new TimeRange(nowI / 1000. - duration, duration));
            }
            long[] totalMemory = { Runtime.getRuntime().totalMemory() };
            rmapR.findDescendant("/TotalMemory", false).setDblock(new DataBlock(totalMemory, 1, 8, DataBlock.TYPE_INT64, DataBlock.ORDER_MSB, false, 0, 8));
            long[] availableMemory = { totalMemory[0] - Runtime.getRuntime().freeMemory() };
            rmapR.findDescendant("/MemoryUsed", false).setDblock(new DataBlock(availableMemory, 1, 8, DataBlock.TYPE_INT64, DataBlock.ORDER_MSB, false, 0, 8));
            long[] socketBytes = { getRoutingMapHandler().bytesTransferred() };
            rmapR.findDescendant("/SocketBytes", false).setDblock(new DataBlock(socketBytes, 1, 8, DataBlock.TYPE_INT64, DataBlock.ORDER_MSB, false, 0, 8));
            double[] socketRate = { (socketBytes[0] - metricsBytes) / duration };
            metricsBytes = socketBytes[0];
            rmapR.findDescendant("/SocketRate", false).setDblock(new DataBlock(socketRate, 1, 8, DataBlock.TYPE_FLOAT64, DataBlock.ORDER_MSB, false, 0, 8));
            long[] cacheDS = new long[1], archiveDS = new long[1];
            cacheDS[0] = archiveDS[0] = 0L;
            calculateDataSizes(cacheDS, archiveDS);
            rmapR.findDescendant("/CacheDataBytes", false).setDblock(new DataBlock(cacheDS, 1, 8, DataBlock.TYPE_INT64, DataBlock.ORDER_MSB, false, 0, 8));
            rmapR.findDescendant("/ArchiveDataBytes", false).setDblock(new DataBlock(archiveDS, 1, 8, DataBlock.TYPE_INT64, DataBlock.ORDER_MSB, false, 0, 8));
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
        return (rmapR);
    }

    public final ClientHandler createClientHandler(RCO rcoI, ClientInterface clientInterfaceI) throws com.rbnb.api.AddressException, com.rbnb.api.SerializeException, java.io.InterruptedIOException, java.io.IOException, java.lang.InterruptedException {
        ClientHandler cHandlerR = null;
        if (clientInterfaceI instanceof RouterInterface) {
            if (rcoI.isAllowedAccess(getAddressHandler(), AddressPermissions.ROUTER)) {
                cHandlerR = new RBNBRouter(rcoI, (RouterInterface) clientInterfaceI);
            } else {
                throw new com.rbnb.api.AddressException("Client address is not allowed to start routing " + "connections.");
            }
        } else if (clientInterfaceI instanceof ControllerInterface) {
            if ((clientInterfaceI.getName().startsWith("_RR.") && rcoI.isAllowedAccess(getAddressHandler(), AddressPermissions.ROUTER)) || rcoI.isAllowedAccess(getAddressHandler(), AddressPermissions.CONTROL)) {
                cHandlerR = new RBNBController(rcoI, (ControllerInterface) clientInterfaceI);
            } else if (clientInterfaceI.getName().startsWith("_RR.")) {
                throw new com.rbnb.api.AddressException("Client address is not allowed to start routing " + "connections.");
            } else {
                throw new com.rbnb.api.AddressException("Client address is not allowed to start control " + "connections.");
            }
        } else if (clientInterfaceI instanceof PlugInInterface) {
            if (rcoI.isAllowedAccess(getAddressHandler(), AddressPermissions.PLUGIN)) {
                cHandlerR = new RBNBPlugIn(rcoI, (PlugInInterface) clientInterfaceI);
            } else {
                throw new com.rbnb.api.AddressException("Client address is not allowed to start plugin " + "connections.");
            }
        } else if (clientInterfaceI instanceof SinkInterface) {
            if (rcoI.isAllowedAccess(getAddressHandler(), AddressPermissions.SINK)) {
                cHandlerR = new NBO(rcoI, (SinkInterface) clientInterfaceI);
            } else {
                throw new com.rbnb.api.AddressException("Client address is not allowed to start sink " + "connections.");
            }
        } else if (clientInterfaceI instanceof SourceInterface) {
            if (rcoI.isAllowedAccess(getAddressHandler(), AddressPermissions.SOURCE)) {
                cHandlerR = new RBO(rcoI, (SourceInterface) clientInterfaceI);
            } else {
                throw new com.rbnb.api.AddressException("Client address is not allowed to start source " + "connections.");
            }
        } else {
            throw new java.lang.IllegalArgumentException(clientInterfaceI + " is not a supported client.");
        }
        addChild((Rmap) cHandlerR);
        return (cHandlerR);
    }

    public final Serializable deliver(RoutedMessage messageI, int offsetI) throws com.rbnb.api.AddressException, com.rbnb.api.SerializeException, java.io.InterruptedIOException, java.io.IOException, java.lang.InterruptedException {
        Serializable serializableR = null;
        if (offsetI >= messageI.getTarget().length()) {
            if (!(messageI.getMessage() instanceof Login)) {
                java.lang.IllegalArgumentException laEx = new java.lang.IllegalArgumentException(messageI + " is not a valid message.");
                serializableR = Language.exception(laEx);
                if (getLog() != null) {
                    getLog().addException(Log.STANDARD, getLogClass(), getName(), laEx);
                }
            } else {
                Login login = (Login) messageI.getMessage();
                ClientInterface clientInterface = (ClientInterface) login.getChildAt(0);
                if (clientInterface instanceof Sink) {
                    if (findRemoteClientHandler(clientInterface.getName()) != null) {
                        java.lang.IllegalStateException isEx = new java.lang.IllegalStateException(messageI + " produces a naming conflict with an" + " existing remote client object.");
                        serializableR = Language.exception(isEx);
                        if (getLog() != null) {
                            getLog().addException(Log.STANDARD, getLogClass(), getName(), isEx);
                        }
                    } else {
                        RemoteClientHandler rClient = new RemoteClientHandler();
                        rClient.setServerHandler(this);
                        rClient.setName(clientInterface.getName());
                        rClient.setUsername(login.getUsername());
                        addRemoteClientHandler(rClient);
                        rClient.start();
                        serializableR = Language.ping();
                    }
                } else {
                    java.lang.IllegalArgumentException laEx = new java.lang.IllegalArgumentException(messageI + " tries to create an illegal remote client.");
                    serializableR = Language.exception(laEx);
                    if (getLog() != null) {
                        getLog().addException(Log.STANDARD, getLogClass(), getName(), laEx);
                    }
                }
            }
        } else {
            int nSlash = messageI.getTarget().indexOf(PATHDELIMITER, offsetI);
            String nextName;
            if (nSlash == -1) {
                nSlash = messageI.getTarget().length();
            } else if (nSlash == offsetI) {
                serializableR = getRoutingMapHandler().deliver(messageI, nSlash + 1);
            }
            if (nSlash != offsetI) {
                nextName = messageI.getTarget().substring(offsetI, nSlash);
                Rmap rTarget = findChild(new Rmap(nextName));
                if (rTarget == null) {
                    com.rbnb.api.AddressException addrEx = new com.rbnb.api.AddressException(getFullName() + " does not have a child named " + nextName + ".\nCannot find " + messageI.getTarget() + ".");
                    serializableR = Language.exception(addrEx);
                    if (getLog() != null) {
                        getLog().addException(Log.STANDARD, getLogClass(), getName(), addrEx);
                    }
                } else {
                    if (rTarget instanceof RoutedTarget) {
                        serializableR = ((RoutedTarget) rTarget).deliver(messageI, nSlash + 1);
                    } else {
                        RemoteClientHandler rClient = findRemoteClientHandler(nextName);
                        if (rClient != null) {
                            rClient.send(messageI);
                            serializableR = Language.ping();
                        } else {
                            com.rbnb.api.AddressException addrEx = new com.rbnb.api.AddressException(getFullName() + " failed to find handler for " + nextName + ".");
                            serializableR = Language.exception(addrEx);
                            if (getLog() != null) {
                                getLog().addException(Log.STANDARD, getLogClass(), getName(), addrEx);
                            }
                        }
                    }
                }
            }
        }
        return (serializableR);
    }

    public final Path findPath() throws com.rbnb.api.AddressException, com.rbnb.api.SerializeException, java.io.EOFException, java.io.IOException, java.lang.InterruptedException {
        LocalPath pathR = new LocalPath();
        pathR.add(this);
        pathR.setCost(0);
        return (pathR);
    }

    public final void findPaths(SortedStrings toFindIO) throws com.rbnb.api.AddressException, com.rbnb.api.SerializeException, java.io.IOException, java.lang.InterruptedException {
        try {
            getPathDoor().lock("RBNB.findPaths");
            long lPathFindCounter = getPathFindCounter() + 1;
            LocalPath lPath = new LocalPath();
            lPath.add(this);
            com.rbnb.utility.SortedVector pathsFound = getValidPaths(toFindIO), pathsToCheck = new com.rbnb.utility.SortedVector(Path.BY_COST);
            findPaths(lPathFindCounter, lPath, toFindIO, pathsToCheck, pathsFound);
            while (((toFindIO == null) || (toFindIO.size() > 0)) && (pathsToCheck.size() > 0)) {
                LocalPath pToCheck = (LocalPath) pathsToCheck.firstElement();
                pathsToCheck.removeElementAt(0);
                RemoteServer rServer = (RemoteServer) getRoutingMapHandler().findDescendant((String) pToCheck.getOrdered().lastElement(), false);
                if (rServer != null) {
                    rServer.findPaths(lPathFindCounter, pToCheck, toFindIO, pathsToCheck, pathsFound);
                }
            }
            setPaths(pathsFound);
        } finally {
            getPathDoor().unlock();
        }
    }

    public final Path findPathTo(PeerServer peerServerI) throws com.rbnb.api.AddressException, com.rbnb.api.SerializeException, java.io.IOException, java.lang.InterruptedException {
        Path pathR = null;
        boolean locked = false;
        String peerName = peerServerI.getFullName();
        try {
            getPathDoor().lockRead("RBNB.findPathTo");
            locked = true;
            if ((pathR = (Path) getPaths().find(peerName)) == null) {
                SortedStrings toFind = new SortedStrings();
                toFind.add(peerName);
                getPathDoor().unlockRead();
                locked = false;
                findPaths(toFind);
                getPathDoor().lockRead("RBNB.findPathTo_2");
                locked = true;
                pathR = (Path) getPaths().find(peerName);
            }
        } catch (com.rbnb.utility.SortException e) {
            throw new com.rbnb.compat.InternalError();
        } finally {
            if (locked) {
                getPathDoor().unlockRead();
                locked = false;
            }
        }
        return (pathR);
    }

    public final RemoteClientHandler findRemoteClientHandler(String nameI) throws com.rbnb.api.AddressException, com.rbnb.api.SerializeException, java.io.EOFException, java.io.InterruptedIOException, java.io.IOException, java.lang.InterruptedException {
        RemoteClientHandler rchR = null;
        boolean locked = false;
        try {
            rClientsDoor.lockRead("RBNB.findRemoteClientHandler");
            locked = true;
            java.util.Vector check = null;
            if ((rClients != null) && (rClients.size() > 0) && ((check = rClients.findName(nameI)) != null) && (check.size() > 0)) {
                rchR = (RemoteClientHandler) check.firstElement();
            }
        } finally {
            if (locked) {
                rClientsDoor.unlockRead();
            }
        }
        return (rchR);
    }

    public final ActionThreadQueue getActivityQueue() {
        return (activityQueue);
    }

    public final String getArchiveHomeDirectory() {
        return (archiveHomeDirectory);
    }

    public final boolean getAutoLoadArchives() {
        return (autoLoadArchives);
    }

    public final Server getClientSide() {
        return (clientSide);
    }

    public final String getCompatibilityMode() {
        return (compatibilityMode);
    }

    final byte getDebugLevel() {
        return (debugLevel);
    }

    final long getDebugMask() {
        return (debugMask);
    }

    public final ServerHandler getLocalServerHandler() {
        return (this);
    }

    public final long getLogClass() {
        return (super.getLogClass() | Log.CLASS_RBNB);
    }

    public final Log getLog() {
        return (log);
    }

    public final long getLogStatusPeriod() {
        return (logStatusPeriod);
    }

    final int getMaxActivityThreads() {
        return (maxActivityThreads);
    }

    final int getMaxOpenFileSets() {
        return (maxOpenFileSets);
    }

    public final long getMetricsPeriod() {
        return (metricsPeriod);
    }

    private final Source getMetricsSource() {
        return (metricsSource);
    }

    public final LimitedResource getOpenFileSets() {
        return (openFileSets);
    }

    private final com.rbnb.utility.SortedVector getPaths() {
        return (paths);
    }

    private final Door getPathDoor() {
        return (pathDoor);
    }

    public final RAMServerCommunications getRAMPort() {
        if (ramPort == null) {
            setRAMPort(new RAMServerCommunications(this));
            getRAMPort().start();
        }
        return (ramPort);
    }

    public final Rmap getRegistered(Rmap requestI) throws com.rbnb.api.AddressException, com.rbnb.api.SerializeException, java.io.EOFException, java.io.IOException, java.lang.InterruptedException {
        if ((requestI.compareNames(".") != 0) && (requestI.compareNames("..") != 0) && (compareNames(requestI) != 0)) {
            throw new java.lang.IllegalArgumentException(requestI + " does not have a matching name.");
        }
        Rmap rmapR = null;
        if (requestI.compareNames("..") == 0) {
            Rmap request = (Rmap) requestI.clone();
            request.setName(getParent().getName());
            rmapR = ((RegisteredInterface) getParent()).getRegistered(request);
            if (rmapR instanceof EndOfStream) {
                if (rmapR.getNchildren() == 0) {
                    return (null);
                }
                rmapR = rmapR.getChildAt(0);
                rmapR.getParent().removeChildAt(0);
            }
            rmapR.setName("..");
        } else {
            Rmap child, nChild, request, request2, answer;
            java.util.Vector rList;
            rmapR = newInstance();
            if (requestI.compareNames(".") == 0) {
                rmapR.setName(".");
            }
            for (int idx = 0; idx < requestI.getNchildren(); ++idx) {
                request = requestI.getChildAt(idx);
                rList = new java.util.Vector();
                if ((request.compareNames("...") != 0) && (request.compareNames(">...") != 0)) {
                    rList.addElement(request);
                } else {
                    for (int idx1 = 0; idx1 < getNchildren(); ++idx1) {
                        child = getChildAt(idx1);
                        request2 = child.newInstance();
                        request2.addChild(new Rmap(">...", MarkerBlock, null));
                        rList.addElement(request2);
                    }
                }
                for (int idx1 = 0; idx1 < rList.size(); ++idx1) {
                    request2 = (Rmap) rList.elementAt(idx1);
                    if (request2.compareNames("..") == 0) {
                        request2 = (Rmap) request2.clone();
                        request2.setName(getParent().getName());
                        answer = ((RegisteredInterface) getParent()).getRegistered(request2);
                        if (answer instanceof EndOfStream) {
                            if (answer.getNchildren() == 0) {
                                continue;
                            }
                            answer = answer.getChildAt(0);
                            answer.getParent().removeChildAt(0);
                        }
                        answer.setName("..");
                        rmapR.addChild(answer);
                    } else if (request2.compareNames("*") == 0) {
                        request2 = (Rmap) request2.clone();
                        for (int idx2 = 0; idx2 < getNchildren(); ++idx2) {
                            child = getChildAt(idx2);
                            if (child instanceof RegisteredInterface) {
                                request2.setName(child.getName());
                                if ((child instanceof RegisteredInterface) && ((nChild = ((RegisteredInterface) child).getRegistered(request2)) != null)) {
                                    rmapR.addChild(nChild);
                                }
                            }
                        }
                    } else {
                        child = findChild(request2);
                        if (child != null) {
                            if ((child instanceof RegisteredInterface) && ((nChild = ((RegisteredInterface) child).getRegistered(request2)) != null)) {
                                rmapR.addChild(nChild);
                            }
                        }
                    }
                }
            }
        }
        return (rmapR);
    }

    public final RoutingMapHandler getRoutingMapHandler() {
        return (super.getRoutingMapHandler());
    }

    public final Object getServerPort() {
        return (serverPort);
    }

    final String getTerminatingClient() {
        return (terminatingClient);
    }

    public final Thread getThread() {
        return (thread);
    }

    public final Timer getTimer() {
        return (timer);
    }

    private final com.rbnb.utility.SortedVector getValidPaths(SortedStrings toFindI) throws com.rbnb.api.AddressException, com.rbnb.api.SerializeException, java.io.InterruptedIOException, java.io.IOException, java.lang.InterruptedException {
        com.rbnb.utility.SortedVector validR = null;
        if (toFindI == null) {
            validR = new com.rbnb.utility.SortedVector();
        } else {
            validR = (com.rbnb.utility.SortedVector) getPaths().clone();
            String invalid;
            Path path, lPath;
            int low = 0, lo, hi, idx1, direction;
            for (int idx = 0; idx < toFindI.size(); ++idx) {
                invalid = toFindI.elementAt(idx);
                path = null;
                for (lo = low, hi = validR.size() - 1, idx1 = (lo + hi) / 2; lo <= hi; idx1 = (lo + hi) / 2) {
                    lPath = (Path) validR.elementAt(idx1);
                    try {
                        direction = lPath.compareTo(null, invalid);
                    } catch (com.rbnb.utility.SortException e) {
                        continue;
                    }
                    if (direction == 0) {
                        path = lPath;
                        low = idx1;
                        break;
                    } else if (direction < 0) {
                        lo = idx1 + 1;
                    } else if (direction > 0) {
                        hi = idx1 - 1;
                    }
                }
                if (path != null) {
                    validR.remove(path);
                }
            }
        }
        return (validR);
    }

    public final Serializable initiateReverseRoute(ReverseRoute reverseI) {
        Serializable answerR = Language.ping();
        int idx = 0;
        try {
            PeerServer pServer = (PeerServer) reverseI.getObject();
            Server server = (Server) pServer.newInstance();
            RoutingMapHandler rmh = null;
            PeerServer lpServer = null;
            String rhName;
            Controller controller;
            RouterHandler rHandler;
            for (idx = 0; idx < MAXIMUM_SIMULTANEOUS_ROUTERS; ++idx) {
                rhName = "_RR." + getFullName() + "." + idx;
                controller = server.createController(rhName.replace('/', '_'));
                controller.start();
                reverseI.setFromPrimary(false);
                rHandler = ((ControllerHandle) controller).reverseRoute(reverseI, this);
                addChild((Rmap) rHandler);
                ((RBNBRouter) rHandler).serverName = pServer.getFullName();
                if (rmh == null) {
                    rmh = getRoutingMapHandler();
                    lpServer = (PeerServer) ((Rmap) rmh).findDescendant(pServer.getFullName(), false);
                }
                lpServer.addRelated(rHandler);
                rHandler.start((ClientHandler) null);
            }
            answerR = Language.ping();
        } catch (java.lang.Exception e) {
            try {
                getLog().addException(Log.STANDARD, getLogClass(), getName(), e);
            } catch (java.lang.Exception e1) {
            }
            if (idx == 0) {
                answerR = new ExceptionMessage(e);
            } else {
                answerR = Language.ping();
            }
        }
        return (answerR);
    }

    public final Server initiateRouteTo(Server peerHierarchyI, Server localI) throws com.rbnb.api.AddressException, com.rbnb.api.SerializeException, java.io.InterruptedIOException, java.io.IOException, java.lang.InterruptedException {
        Server localR = null;
        try {
            getRoutingMapHandler().getPeerDoor().lock("RBNB.initiateRouteTo");
            PeerServer pServer = null;
            if (localI == null) {
                if (peerHierarchyI.getName() == null) {
                    pServer = new PeerServer(null, peerHierarchyI.getAddress());
                    pServer.setLocalServerHandler(this);
                } else {
                    pServer = getRoutingMapHandler().createPeer(peerHierarchyI, null);
                }
            } else if (localI instanceof PeerServer) {
                pServer = (PeerServer) localI;
            } else {
                pServer = new PeerServer(localI.getName(), localI.getAddress());
                pServer.setLocalServerHandler(this);
                pServer.setChildren(localI.getChildren());
                for (int idx = 0; idx < pServer.getNchildren(); ++idx) {
                    Rmap child = pServer.getChildAt(idx);
                    child.setParent(null);
                    child.setParent(pServer);
                }
                Rmap tParent = localI.getParent();
                tParent.removeChild(localI);
                tParent.addChild(pServer);
            }
            if (!pServer.getConnected()) {
                pServer.start();
            }
            if (pServer.getActiveShortcuts() == 0) {
                if ((pServer.getPassiveShortcuts() > 0) || (pServer instanceof ChildServer)) {
                    pServer.switchToActive();
                } else {
                    Router router = pServer.grabRouter();
                    java.util.Vector additional = new java.util.Vector();
                    additional.addElement("peer");
                    Server top = (Server) this.newInstance(), current = this, build;
                    while ((current.getParent() != null) && (current.getParent() instanceof Server)) {
                        current = (Server) current.getParent();
                        build = (Server) current.newInstance();
                        build.addChild(top);
                        top = build;
                    }
                    additional.addElement(top);
                    router.send(new Ask(Ask.ROUTEFROM, additional));
                    Serializable serializable;
                    do {
                        serializable = router.receive(ACO.rmapClass, false, Client.FOREVER);
                        if (serializable instanceof ExceptionMessage) {
                            Language.throwException((ExceptionMessage) serializable);
                        } else if (serializable instanceof ReverseRoute) {
                            router.send(initiateReverseRoute((ReverseRoute) serializable));
                            serializable = null;
                        }
                    } while (serializable == null);
                    Rmap hierarchy = (Rmap) serializable;
                    localR = pServer;
                    pServer.releaseRouter(router);
                    if ((peerHierarchyI.getName() == null) && (localI == null)) {
                        localR = getRoutingMapHandler().createPeer(hierarchy, pServer);
                    }
                    ((PeerServer) localR).setConnected(true);
                }
            }
        } finally {
            getRoutingMapHandler().getPeerDoor().unlock();
        }
        return (localR);
    }

    public final void interrupt() {
        if (getThread() != null) {
            getThread().interrupt();
        }
    }

    public final boolean isRunning() {
        return ((getThread() != null) && getThread().isAlive());
    }

    private final void loadArchives() {
        try {
            com.rbnb.compat.File homeDir = new com.rbnb.compat.File(getArchiveHomeDirectory());
            Directory asDirectory = new Directory(homeDir);
            com.rbnb.compat.File[] files = asDirectory.listFiles();
            for (int idx = 0; idx < files.length; ++idx) {
                if (files[idx].isDirectory()) {
                    getActivityQueue().addEvent(new LoadSource(this.getClientSide(), files[idx].getName()));
                }
            }
        } catch (java.lang.Exception e) {
        }
    }

    public final void lockRouting() throws java.lang.InterruptedException {
        routingDoor.lock("RBNB.lockRouting");
    }

    final void lostRouting() {
        return;
    }

    private final void parseArguments(String[] argsI) throws com.rbnb.api.SerializeException, java.lang.IllegalArgumentException {
        com.rbnb.utility.ArgHandler argHandler = null;
        boolean haveAddrAuth = false;
        try {
            argHandler = new com.rbnb.utility.ArgHandler(argsI);
        } catch (java.lang.Exception e) {
            throw new java.lang.IllegalArgumentException(e.getMessage());
        }
        String value;
        if (((value = argHandler.getOption('A')) != null) && !value.equalsIgnoreCase("<none>")) {
            if (haveAddrAuth) {
                throw new java.lang.IllegalArgumentException("Inconsistent security switches.");
            }
            try {
                getAddressHandler().setAuthorization(new AddressAuthorization(value));
                haveAddrAuth = true;
            } catch (java.lang.Exception e) {
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                String message = new String(baos.toByteArray());
                throw new com.rbnb.api.SerializeException("Failed to read authorization file: " + value + ".\n" + message);
            }
        }
        if ((value = argHandler.getOption('C')) != null) {
            setCompatibilityMode(value);
        }
        if ((value = argHandler.getOption('D')) != null) {
            com.rbnb.compat.StringTokenizer st = new com.rbnb.compat.StringTokenizer(value, ",+", true);
            String subvalue, mask = null, level = null;
            if (st.hasMoreTokens()) {
                subvalue = st.nextToken();
                if (!subvalue.equals(",") && !subvalue.equals("+")) {
                    mask = subvalue;
                    if (st.hasMoreTokens()) {
                        subvalue = st.nextToken();
                    }
                }
                if (st.hasMoreTokens()) {
                    level = st.nextToken();
                }
            }
            if (mask != null) {
                if ((mask.indexOf("0x") == 0) || (mask.indexOf("0X") == 0)) {
                    setDebugMask(Long.parseLong(mask.substring(2), 16));
                } else {
                    setDebugMask(Long.parseLong(mask));
                }
            }
            if (level != null) {
                setDebugLevel(Byte.parseByte(level));
            }
        }
        setAutoLoadArchives(argHandler.checkFlag('F'));
        if ((value = argHandler.getOption('H')) != null) {
            File tempDir = new File(value);
            if (tempDir.exists() && tempDir.isDirectory()) {
                Random random = new Random(System.currentTimeMillis());
                try {
                    File tempFile = new File(tempDir, new String("rbnb" + random.nextInt() + ".tmp"));
                    tempFile.delete();
                    setArchiveHomeDirectory(value);
                } catch (Exception e) {
                    System.err.println("\nWARNING: Unable to use the specified archive " + "home directory, \"" + value + "\". (You may not have permission to use " + "this directory.)\n" + "Using the default archive home directory \"" + getArchiveHomeDirectory() + "\"\n");
                }
            } else {
                System.err.println("\nWARNING: The specified archive home directory, \"" + value + "\", either does not exist or is not a directory.\n" + "Using the default archive home directory \"" + getArchiveHomeDirectory() + "\"\n");
            }
        }
        if ((value = argHandler.getOption('l')) != null) {
            PeriodicSource logSettings = new PeriodicSource("log", value, TimerPeriod.LOG_STATUS, 1000, 0, SourceInterface.ACCESS_NONE);
            try {
                setLogStatusPeriod(logSettings.getPeriod());
                getLog().setCframes(logSettings.getCframes());
                getLog().setAframes(logSettings.getAframes());
                getLog().setAmode(logSettings.getAmode());
            } catch (java.io.IOException e) {
                throw new java.lang.IllegalArgumentException("Unable to parse log argument: " + value + ".\n" + e.getClass() + " " + e.getMessage());
            } catch (java.lang.InterruptedException e) {
                throw new java.lang.IllegalArgumentException("Unable to parse log argument: " + value + ".\n" + e.getClass() + " " + e.getMessage());
            }
        }
        if ((value = argHandler.getOption('m')) != null) {
            PeriodicSource metricsSettings = new PeriodicSource("metrics", value, TimerPeriod.METRICS, 60 * 60 * 1000 / TimerPeriod.METRICS, 0, SourceInterface.ACCESS_NONE);
            try {
                setMetricsPeriod(metricsSettings.getPeriod());
                getMetricsSource().setCframes(metricsSettings.getCframes());
                getMetricsSource().setAframes(metricsSettings.getAframes());
                getMetricsSource().setAmode(metricsSettings.getAmode());
            } catch (java.io.IOException e) {
                throw new java.lang.IllegalArgumentException("Unable to parse metrics argument: " + value + ".\n" + e.getClass() + " " + e.getMessage());
            } catch (java.lang.InterruptedException e) {
                throw new java.lang.IllegalArgumentException("Unable to parse metrics argument: " + value + ".\n" + e.getClass() + " " + e.getMessage());
            }
        }
        if ((value = argHandler.getOption('M')) != null) {
            setMaxActivityThreads(Integer.parseInt(value));
            if (getMaxActivityThreads() <= 0) {
                throw new java.lang.IllegalArgumentException("Need at least one activity thread.");
            }
        }
        if (argHandler.checkFlag('L')) {
            if (haveAddrAuth) {
                throw new java.lang.IllegalArgumentException("Inconsistent security switches.");
            }
            try {
                AddressAuthorization addrAuth = new AddressAuthorization();
                addrAuth.removeAllow(new AddressPermissions("*"));
                addrAuth.addAllow(new AddressPermissions("localhost"));
                getAddressHandler().setAuthorization(addrAuth);
                haveAddrAuth = true;
            } catch (java.lang.Exception e) {
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                String message = new String(baos.toByteArray());
                throw new com.rbnb.api.SerializeException("Failed to lock down authorization for server.\n" + message);
            }
            haveAddrAuth = true;
        }
        if ((value = argHandler.getOption('P')) != null) {
            String user, pwd;
            int commaI = value.indexOf(',');
            if (commaI != -1) {
                user = value.substring(0, commaI);
                pwd = value.substring(commaI + 1);
            } else {
                user = value;
                try {
                    pwd = promptUser("PASSWORD: ");
                } catch (java.io.IOException ioe) {
                    pwd = null;
                }
            }
            try {
                username = new Username(user, pwd);
            } catch (Exception e) {
                throw new IllegalArgumentException("Cannot set user id or password properties.");
            }
        } else if (argHandler.checkFlag('P')) {
            try {
                username = new Username(promptUser("User [rbnb]: "), promptUser("PASSWORD: "));
                if (username.getUsername() == null || username.getUsername().length() == 0) {
                    username.setUsername("rbnb");
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Cannot set user id or password properties.");
            }
        }
        if ((value = argHandler.getOption('S')) != null) {
            setMaxOpenFileSets(Integer.parseInt(value));
            if (getMaxOpenFileSets() <= 0) {
                throw new java.lang.IllegalArgumentException("Need at least one open filesets allowed.");
            }
        }
    }

    /**
      * Prompt the user for input.
      *
      * @author WHF
      * @version 2005/01/19
      * @since V2.5
      */
    private String promptUser(String prompt) throws java.io.IOException {
        System.out.print(prompt);
        return "";
    }

    public final void removeRemoteClientHandler(RemoteClientHandler rchI) throws java.lang.InterruptedException {
        try {
            rClientsDoor.lock("RBNB.removeRemoteClientHandler");
            if (rClients != null) {
                rClients.remove(rchI);
            }
        } finally {
            rClientsDoor.unlock();
        }
    }

    public final void run() {
        setConnected(true);
        TimerTask metricsTT = null;
        try {
            try {
            } catch (Exception e) {
            }
            setTimer(new Timer(true));
            setActivityQueue(new ActionThreadQueue(getMaxActivityThreads()));
            setOpenFileSets(new LimitedResource(getMaxOpenFileSets()));
            try {
                addChild(getLog());
                if ((getLog().getCframes() > 0) || (getLog().getAframes() > 0)) {
                    getLog().start((ClientHandler) null);
                } else {
                    removeChild(getLog());
                    getLog().setRunnable(false);
                }
            } catch (SerializeException e) {
                removeChild(getLog());
                getLog().setRunnable(false);
            }
            while (getLog().getRunnable() && !getLog().isRunning()) {
                Thread.currentThread().sleep(100);
            }
            if ((System.getProperty("os.name").indexOf("Windows") != -1) && (System.getProperty("java.fullversion") != null) && (System.getProperty("java.fullversion").indexOf("J2RE 1.3.0 IBM") == 0)) {
                getLog().addMessage(getLogLevel(), getLogClass(), getName(), "WARNING: This JVM (" + System.getProperty("java.fullversion") + ")\ndoes not reliably run the RBNB V2 server.");
            }
            BuildFile.loadBuildFile(this);
            setServerPort(getAddressHandler().newServerSide(this));
            synchronized (this) {
                notifyAll();
            }
            RoutingMapHandler rMap = new RBNBRoutingMap();
            rMap.start();
            Rmap hierarchy = this;
            if (getParent() == null) {
                rMap.addChild(this);
            } else {
                ParentServer pServer = (ParentServer) getParent();
                pServer.setConnected(true);
                rMap.addChild(pServer);
                pServer.setLocalServerHandler(this);
                Router router = pServer.grabRouter();
                java.util.Vector additional = new java.util.Vector();
                additional.addElement("child");
                additional.addElement(this.newInstance());
                router.send(new Ask(Ask.ROUTEFROM, additional));
                Serializable serializable = router.receive(ACO.rmapClass, false, Client.FOREVER);
                if (serializable instanceof ExceptionMessage) {
                    Language.throwException((ExceptionMessage) serializable);
                }
                hierarchy = (Rmap) serializable;
                if (hierarchy != null) {
                    Rmap above = hierarchy.getChildAt(0), locator = null;
                    hierarchy = null;
                    above.getParent().removeChildAt(0);
                    Rmap bottom, temp;
                    for (bottom = above; (bottom.getNchildren() == 1); bottom = bottom.getChildAt(0)) {
                        temp = new RemoteServer(bottom.getName(), ((Server) bottom).getAddress());
                        if (locator != null) {
                            locator.addChild(temp);
                            locator = temp;
                        } else {
                            hierarchy = locator = temp;
                        }
                    }
                    getParent().setName(bottom.getName());
                    if (hierarchy != null) {
                        ((Rmap) rMap).removeChild(getParent());
                        locator.addChild(getParent());
                        rMap.addChild(hierarchy);
                    }
                }
                if (getParent() instanceof PeerServer) {
                    ((PeerServer) getParent()).setConnected(true);
                    Rmap lServer = getParent().newInstance(), entry, nEntry, top = lServer;
                    for (entry = getParent().getParent(); entry != null; entry = entry.getParent()) {
                        nEntry = entry.newInstance();
                        nEntry.addChild(top);
                        top = nEntry;
                    }
                    lServer.addChild(newInstance());
                    ReverseRoute reverse = new ReverseRoute(lServer);
                    initiateReverseRoute(reverse);
                }
                pServer.releaseRouter(router);
            }
            rMap.setLocalName(getFullName());
            getPathDoor().setIdentification(getFullName() + "_path");
            rClientsDoor.setIdentification(getFullName() + "_rclients");
            routingDoor.setIdentification(getFullName() + "_routing");
            String message;
            message = ("RBNB (Ring Buffered Network Bus) " + getBuildVersion() + " (built " + getBuildDate() + ")");
            message += "\nCopyright 2006 Creare Inc.";
            message += "\nStarted at address " + getAddress() + ".";
            message += "\nArchive home directory: \"" + getArchiveHomeDirectory() + "\"";
            getLog().setClasses(getDebugMask());
            getLog().setLevel(getDebugLevel());
            if ((getLog().getClasses() != Log.NONE) || (getLog().getLevel() != Log.STANDARD)) {
                message += "\nDebug mask=" + Long.toString(getLog().getClasses(), 16) + "  Debug level=" + getLog().getLevel() + ".";
            }
            getLog().addMessage(getLogLevel(), getLogClass(), getName(), message);
            if (getMetricsSource() != null) {
                getMetricsSource().start();
                Thread.currentThread().yield();
                MetricsCollector metricsCollector = new MetricsCollector();
                metricsCollector.setObject(this);
                metricsCollector.setSource(getMetricsSource());
                metricsTT = new TimerTask(metricsCollector, MetricsInterface.TT_METRICS);
                getTimer().schedule(metricsTT, getMetricsPeriod(), getMetricsPeriod());
            }
            if (getAutoLoadArchives()) {
                loadArchives();
            }
            while (!getTerminateRequested() && !com.rbnb.compat.Utilities.interrupted(getThread())) {
                try {
                    getAddressHandler().setUsername(username);
                    Object clientSideObj = getAddressHandler().accept(getServerPort(), 1000);
                    if (clientSideObj != null) {
                        RCO rco = RCO.newRCO(clientSideObj, this);
                        rco.start();
                    }
                } catch (com.rbnb.api.AddressException e) {
                    getLog().addException(Log.STANDARD, getLogClass(), getName(), e);
                    getLog().addMessage(getLogLevel(), getLogClass(), getName(), "A client failed to connect to the server.");
                } catch (java.io.IOException e) {
                    getLog().addException(Log.STANDARD, getLogClass(), getName(), e);
                    getLog().addMessage(getLogLevel(), getLogClass(), getName(), "A client failed to connect to the server.");
                }
            }
        } catch (java.lang.InterruptedException e) {
        } catch (java.io.InterruptedIOException e) {
        } catch (com.rbnb.api.AddressException e) {
            try {
                getLog().addException(Log.STANDARD, getLogClass(), getName(), e);
            } catch (java.lang.Exception e1) {
            }
        } catch (com.rbnb.api.SerializeException e) {
            try {
                getLog().addException(Log.STANDARD, getLogClass(), getName(), e);
            } catch (java.lang.Exception e1) {
            }
        } catch (java.io.IOException e) {
            try {
                getLog().addException(Log.STANDARD, getLogClass(), getName(), e);
            } catch (java.lang.Exception e1) {
            }
        } catch (java.lang.Exception e) {
            try {
                getLog().addException(Log.STANDARD, getLogClass(), getName(), e);
            } catch (java.lang.Exception e1) {
            }
        } finally {
            if (getThread() != null) {
                ((ThreadWithLocks) getThread()).clearLocks();
            }
        }
        if (metricsTT != null) {
            try {
                metricsTT.cancel();
            } catch (java.lang.Exception e) {
            }
            metricsTT = null;
        }
        if (getTimer() != null) {
            try {
                getTimer().cancel();
            } catch (java.lang.Exception e) {
            }
            setTimer(null);
        }
        java.util.Vector lChildren = new java.util.Vector();
        try {
            for (int idx = 0; idx < getNchildren(); ++idx) {
                try {
                    lChildren.addElement(getChildAt(idx));
                } catch (java.lang.Exception e) {
                }
            }
        } catch (java.lang.Exception e) {
        }
        for (int idx = 0; idx < lChildren.size(); ++idx) {
            if (lChildren.elementAt(idx) instanceof ClientHandler) {
                ClientHandler client = (ClientHandler) lChildren.elementAt(idx);
                if ((client != getLog()) && ((getTerminatingClient() == null) || (client.compareNames(getTerminatingClient()) != 0))) {
                    try {
                        if (client instanceof SourceHandler) {
                            ((SourceHandler) client).setCkeep(false);
                        }
                        client.stop(null);
                    } catch (java.lang.Exception e) {
                    }
                }
            }
        }
        if (getRAMPort() != null) {
            getRAMPort().interrupt();
            setRAMPort(null);
        }
        try {
            if (getAddressHandler() != null) {
                getAddressHandler().close(getServerPort());
                setServerPort(null);
                System.gc();
            }
        } catch (java.lang.Exception e) {
        }
        if (getParent() instanceof Server) {
            try {
                Rmap hierarchy = createFromName(getParent().getFullName(), getParent());
                hierarchy.moveToBottom().addChild(newInstance());
                Stop stop = new Stop((ServerInterface) hierarchy);
                ConnectedServer cServer = (ConnectedServer) getParent();
                Router router = cServer.grabRouter();
                if (router != null) {
                    router.send(stop);
                    router.receive(TimerPeriod.SHUTDOWN_ROUTER);
                    cServer.releaseRouter(router);
                }
            } catch (java.lang.Exception e) {
            }
        }
        if (getRoutingMapHandler() != null) {
            getRoutingMapHandler().stop();
            Thread.currentThread().yield();
        }
        try {
            if ((getLog() == null) || !getLog().getRunnable()) {
                if (getLog() != null) {
                    getLog().addMessage(getLogLevel(), getLogClass(), getName(), "Cleaning up (terminating connections).");
                }
            } else {
                getLog().addMessage(getLogLevel(), getLogClass(), getName(), "Cleaning up (terminating connections and log).");
                Thread.currentThread().sleep(2000);
                getLog().stop((ClientHandler) null);
            }
            Thread.currentThread().yield();
        } catch (java.lang.Exception e) {
        }
        lChildren = new java.util.Vector();
        try {
            for (int idx = 0; idx < getNchildren(); ++idx) {
                try {
                    lChildren.addElement(getChildAt(idx));
                } catch (java.lang.Exception e) {
                }
            }
        } catch (java.lang.Exception e) {
        }
        for (int idx = 0; idx < lChildren.size(); ++idx) {
            if (lChildren.elementAt(idx) instanceof ClientHandler) {
                ClientHandler client = (ClientHandler) lChildren.elementAt(idx);
                if ((getTerminatingClient() != null) && (client.compareNames(getTerminatingClient()) == 0)) {
                    try {
                        if (client instanceof SourceHandler) {
                            ((SourceHandler) client).setCkeep(false);
                        }
                        client.stop(null);
                        Thread.currentThread().yield();
                    } catch (java.lang.Exception e) {
                    }
                    break;
                }
            }
        }
        try {
            String time = Time.since1970(System.currentTimeMillis() / 1000.);
            while (time.length() < 28) {
                time += " ";
            }
            String message = "<" + time + "> <" + getName() + ">\n   Terminated.";
            System.err.println(message);
        } catch (java.lang.Exception e) {
        }
        getActivityQueue().stop();
        setTerminateRequested(false);
        setThread(null);
        synchronized (this) {
            notifyAll();
        }
        Thread.currentThread().yield();
    }

    public final void setActivityQueue(ActionThreadQueue activityQueueI) {
        activityQueue = activityQueueI;
    }

    public final void setArchiveHomeDirectory(String archiveHomeDirectoryI) {
        archiveHomeDirectory = archiveHomeDirectoryI;
    }

    public final void setAutoLoadArchives(boolean autoLoadArchivesI) {
        autoLoadArchives = autoLoadArchivesI;
    }

    public final void setClientSide(Server clientSideI) {
        clientSide = clientSideI;
    }

    public final void setCompatibilityMode(String compatibilityModeI) {
        int idx;
        for (idx = 0; idx < COMPATIBLE_VERSIONS.length; ++idx) {
            if (compatibilityModeI.equalsIgnoreCase(COMPATIBLE_VERSIONS[idx])) {
                break;
            }
        }
        if (idx == COMPATIBLE_VERSIONS.length) {
            throw new java.lang.IllegalArgumentException(compatibilityModeI + " is not a supported version.");
        }
        compatibilityMode = compatibilityModeI;
    }

    private final void setDebugLevel(byte debugLevelI) {
        debugLevel = debugLevelI;
    }

    private final void setDebugMask(long debugMaskI) {
        debugMask = debugMaskI;
    }

    private final void setLog(Log logI) {
        log = logI;
    }

    private final void setLogStatusPeriod(long logStatusPeriodI) {
        logStatusPeriod = logStatusPeriodI;
    }

    final void setMaxActivityThreads(int maxActivityThreadsI) {
        maxActivityThreads = maxActivityThreadsI;
    }

    final void setMaxOpenFileSets(int maxOpenFileSetsI) {
        maxOpenFileSets = maxOpenFileSetsI;
    }

    private final void setMetricsPeriod(long metricsPeriodI) {
        metricsPeriod = metricsPeriodI;
    }

    private final void setMetricsSource(Source metricsSourceI) {
        metricsSource = metricsSourceI;
    }

    public final void setOpenFileSets(LimitedResource openFileSetsI) {
        openFileSets = openFileSetsI;
    }

    private final void setPaths(com.rbnb.utility.SortedVector pathsI) {
        paths = pathsI;
    }

    private final void setPathDoor(Door pathDoorI) {
        pathDoor = pathDoorI;
    }

    public final void setRAMPort(RAMServerCommunications portI) {
        ramPort = portI;
    }

    public final void setServerPort(Object serverPortI) {
        serverPort = serverPortI;
        if (serverPort instanceof RAMServerCommunications) {
            setRAMPort((RAMServerCommunications) serverPortI);
        }
    }

    final void setTerminatingClient(String clientI) {
        terminatingClient = clientI;
    }

    private final void setThread(Thread threadI) {
        thread = threadI;
    }

    private final void setTimer(Timer timerI) {
        timer = timerI;
    }

    public final void start(ClientHandler clientI) throws com.rbnb.api.AddressException, com.rbnb.api.SerializeException, java.io.InterruptedIOException, java.io.IOException, java.lang.InterruptedException {
        start(clientI, null);
    }

    public final void start(ClientHandler clientI, String[] argsI) throws com.rbnb.api.AddressException, com.rbnb.api.SerializeException, java.io.InterruptedIOException, java.io.IOException, java.lang.InterruptedException {
        setLog(new Log());
        String doTrim = System.getProperty("trimbytime");
        if (doTrim.equals("true")) {
            System.err.println("Trim-By-Time Mode!");
            getLog().setCframes(3600);
            getLog().setAframes(31536000);
            getLog().setAmode(Source.ACCESS_CREATE);
        } else {
            getLog().setCframes(1000);
        }
        setMetricsSource(getClientSide().createRAMSource("_Metrics"));
        getMetricsSource().setCframes(60 * 60 * 1000 / TimerPeriod.METRICS);
        if (argsI != null) {
            parseArguments(argsI);
            if (getMetricsPeriod() == 0) {
                setMetricsSource(null);
            }
        }
        setThread(new ThreadWithLocks(this, getName()));
        getThread().start();
        synchronized (this) {
            while ((getThread() != null) && getThread().isAlive() && (getServerPort() == null)) {
                wait(TimerPeriod.NORMAL_WAIT);
            }
        }
        Thread.currentThread().yield();
    }

    public final void stop(ClientHandler clientI) {
        try {
            if (getTimer() != null) {
                try {
                    getTimer().cancel();
                } catch (java.lang.Exception e) {
                }
                setTimer(null);
            }
            if (clientI == null) {
                getLog().addMessage(getLogLevel(), getLogClass(), getName(), "Terminating.");
            } else {
                getLog().addMessage(getLogLevel(), getLogClass(), getName(), "Terminating on request from " + clientI.getName() + ".");
            }
            Thread.currentThread().yield();
        } catch (java.lang.Exception e) {
        }
        synchronized (this) {
            if (clientI != null) {
                setTerminatingClient(clientI.getName());
            } else {
                setTerminatingClient(null);
            }
            setTerminateRequested(true);
            notifyAll();
            if (getThread() != null) {
                getThread().interrupt();
            }
            long shutdownAt = System.currentTimeMillis();
            while (isRunning() && getTerminateRequested() && (System.currentTimeMillis() - shutdownAt < TimerPeriod.SHUTDOWN)) {
                try {
                    wait(TimerPeriod.NORMAL_WAIT);
                } catch (java.lang.InterruptedException e) {
                    break;
                }
            }
        }
    }

    public final void timerTask(TimerTask ttI) {
        try {
            if (ttI.getCode().equalsIgnoreCase(TT_RECONNECT)) {
                if (((ParentServer) getParent()).reconnect()) {
                    try {
                        ttI.cancel();
                    } catch (java.lang.Exception e) {
                    }
                }
            }
        } catch (java.lang.Exception e) {
            try {
                getLog().addException(Log.STANDARD, getLogClass(), getName(), e);
            } catch (java.lang.Exception e1) {
            }
        }
    }

    public final synchronized void uniqueName(ClientInterface ciIO) throws com.rbnb.api.AddressException, com.rbnb.api.SerializeException, java.io.EOFException, java.io.InterruptedIOException, java.io.IOException, java.lang.InterruptedException {
        if (ciIO.getName() == null) {
            ciIO.setName("_");
            uniqueName(ciIO);
        } else {
            String name = ciIO.getName();
            int idx = 1;
            if (name.equals(getName())) {
                ciIO.setName(name + "_" + idx);
                ++idx;
            }
            Rmap chld;
            for (; (chld = findChild((Rmap) ciIO)) != null; ++idx) {
                if (ciIO != chld) {
                    ciIO.setName(name + "_" + idx);
                } else {
                    break;
                }
            }
        }
    }

    public final void unlockRouting() throws java.lang.InterruptedException {
        routingDoor.unlock();
    }

    private final class PeriodicSource {

        /**
	 * the size of the archive in frames.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 11/25/2002
	 */
        private long aFrames;

        /**
	 * the archive access mode.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 11/25/2002
	 */
        private byte aMode;

        /**
	 * the size of the cache in frames.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 11/25/2002
	 */
        private long cFrames;

        /**
	 * the period in milliseconds.
	 * <p>
	 *
	 * @author Ian Brown
	 *
	 * @since V2.0
	 * @version 11/25/2002
	 */
        private long period;

        private PeriodicSource() {
            super();
        }

        PeriodicSource(String nameI, String valueI, long periodI, long cFramesI, long aFramesI, byte aModeI) {
            this();
            setPeriod(periodI);
            setCframes(cFramesI);
            setAframes(aFramesI);
            setAmode(aModeI);
            try {
                com.rbnb.compat.StringTokenizer st = new com.rbnb.compat.StringTokenizer(valueI, ",", true);
                String token;
                for (int aIdx = 0; st.hasMoreTokens() && (aIdx < 4); ++aIdx) {
                    token = st.nextToken();
                    if (!token.equals(",")) {
                        switch(aIdx) {
                            case 0:
                                if (token.equalsIgnoreCase("OFF")) {
                                    setPeriod(0);
                                    setCframes(0);
                                    setAframes(0);
                                    setAmode(SourceInterface.ACCESS_NONE);
                                } else if (token.equalsIgnoreCase("NONE")) {
                                    setPeriod(0);
                                } else {
                                    try {
                                        setPeriod((long) (Double.parseDouble(token) * 1000.));
                                    } catch (Exception e) {
                                        try {
                                            setPeriod((long) (Double.parseDouble((token)) * 1000.));
                                        } catch (NumberFormatException f) {
                                        }
                                    }
                                }
                                break;
                            case 1:
                                setCframes(Long.parseLong(token));
                                break;
                            case 2:
                                setAframes(Long.parseLong(token));
                                if (getAmode() != SourceInterface.ACCESS_APPEND) {
                                    if (getAframes() == 0) {
                                        setAmode(SourceInterface.ACCESS_NONE);
                                    } else {
                                        setAmode(SourceInterface.ACCESS_CREATE);
                                    }
                                }
                                break;
                            case 3:
                                if (token.equalsIgnoreCase("CREATE")) {
                                    setAmode(SourceInterface.ACCESS_CREATE);
                                    if (getAframes() == 0) {
                                        setAframes(getCframes());
                                    }
                                } else if (token.equalsIgnoreCase("APPEND")) {
                                    setAmode(SourceInterface.ACCESS_APPEND);
                                } else {
                                    setAmode(SourceInterface.ACCESS_NONE);
                                }
                        }
                        if (st.hasMoreTokens()) {
                            token = st.nextToken();
                        }
                    }
                }
            } catch (java.lang.Exception e) {
                throw new java.lang.IllegalArgumentException("Unable to parse " + nameI + " argument: " + valueI + ".\n" + e.getClass() + " " + e.getMessage());
            }
        }

        final long getAframes() {
            return (aFrames);
        }

        final byte getAmode() {
            return (aMode);
        }

        final long getCframes() {
            return (cFrames);
        }

        final long getPeriod() {
            return (period);
        }

        private final void setAframes(long aFramesI) {
            aFrames = aFramesI;
        }

        private final void setAmode(byte aModeI) {
            aMode = aModeI;
        }

        private final void setCframes(long cFramesI) {
            cFrames = cFramesI;
        }

        private final void setPeriod(long periodI) {
            period = periodI;
        }
    }

    public Object clone() {
        RBNB o = new RBNB();
        cloned(o);
        return o;
    }

    /** Copies all the fields of the object to the given object
	 */
    protected void cloned(Object o) {
        super.cloned(o);
        RBNB clonedR = (RBNB) o;
        clonedR.activityQueue = activityQueue;
        clonedR.archiveHomeDirectory = archiveHomeDirectory;
        clonedR.autoLoadArchives = autoLoadArchives;
        clonedR.clientSide = clientSide;
        clonedR.username = username;
        clonedR.compatibilityMode = compatibilityMode;
        clonedR.currentConnections = currentConnections;
        clonedR.debugLevel = debugLevel;
        clonedR.debugMask = debugMask;
        clonedR.log = log;
        clonedR.logStatusPeriod = logStatusPeriod;
        clonedR.maxActivityThreads = maxActivityThreads;
        clonedR.maximumConnections = maximumConnections;
        clonedR.maxOpenFileSets = maxOpenFileSets;
        clonedR.metrics = metrics;
        clonedR.metricsBytes = metricsBytes;
        clonedR.metricsPeriod = metricsPeriod;
        clonedR.metricsSource = metricsSource;
        clonedR.openFileSets = openFileSets;
        clonedR.pathDoor = pathDoor;
        clonedR.paths = paths;
        clonedR.ramPort = ramPort;
        clonedR.rClients = rClients;
        clonedR.rClientsDoor = rClientsDoor;
        clonedR.routingDoor = routingDoor;
        clonedR.serverPort = serverPort;
        clonedR.terminatingClient = terminatingClient;
        clonedR.thread = thread;
        clonedR.timer = timer;
    }
}
