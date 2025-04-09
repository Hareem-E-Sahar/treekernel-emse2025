package net.sf.fmj.media;

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.media.*;
import javax.media.control.*;
import javax.media.format.*;
import javax.media.protocol.*;
import javax.media.renderer.*;
import net.sf.fmj.filtergraph.*;
import net.sf.fmj.media.control.*;
import net.sf.fmj.media.protocol.*;
import net.sf.fmj.media.renderer.audio.*;
import net.sf.fmj.media.util.*;
import com.sun.media.controls.*;

/**
 * PlaybackEngine implements the media engine for playback.
 */
public class PlaybackEngine extends BasicController implements ModuleListener {

    class BitRateA extends BitRateAdapter implements Owned {

        public BitRateA(int initialBitRate, int minBitRate, int maxBitRate, boolean settable) {
            super(initialBitRate, minBitRate, maxBitRate, settable);
        }

        @Override
        public Component getControlComponent() {
            return null;
        }

        public Object getOwner() {
            return player;
        }

        @Override
        public int setBitRate(int rate) {
            this.value = rate;
            return this.value;
        }
    }

    class HeavyPanel extends java.awt.Panel implements VisualContainer {

        public HeavyPanel(Vector visuals) {
        }
    }

    class LightPanel extends java.awt.Container implements VisualContainer {

        public LightPanel(Vector visuals) {
        }
    }

    /**
     * This is the Graph builder extended from SimpleGraphBuilder to generate
     * the data flow graph for the given track.
     */
    class PlayerGraphBuilder extends SimpleGraphBuilder {

        protected PlaybackEngine engine;

        PlayerGraphBuilder(PlaybackEngine engine) {
            this.engine = engine;
        }

        @Override
        protected GraphNode buildTrackFromGraph(BasicTrackControl tc, GraphNode node) {
            return engine.buildTrackFromGraph(tc, node);
        }
    }

    /**
     * Track Control.
     */
    class PlayerTControl extends BasicTrackControl implements Owned {

        protected PlayerGraphBuilder gb;

        public PlayerTControl(PlaybackEngine engine, Track track, OutputConnector oc) {
            super(engine, track, oc);
        }

        /**
         * Top level routine to build a single track.
         */
        @Override
        public boolean buildTrack(int trackID, int numTracks) {
            if (gb == null) gb = new PlayerGraphBuilder(engine); else gb.reset();
            boolean rtn = gb.buildGraph(this);
            gb = null;
            return rtn;
        }

        @Override
        protected FrameRateControl frameRateControl() {
            return frameRateControl;
        }

        public Object getOwner() {
            return player;
        }

        /**
         * Returns true if this track holds the master time base.
         */
        @Override
        public boolean isTimeBase() {
            for (int j = 0; j < this.modules.size(); j++) {
                if (this.modules.elementAt(j) == masterSink) return true;
            }
            return false;
        }

        @Override
        protected ProgressControl progressControl() {
            return progressControl;
        }
    }

    class SlaveClock implements Clock {

        Clock master, current;

        BasicClock backup;

        SlaveClock() {
            backup = new BasicClock();
            current = backup;
        }

        public long getMediaNanoseconds() {
            return current.getMediaNanoseconds();
        }

        public Time getMediaTime() {
            return current.getMediaTime();
        }

        public float getRate() {
            return current.getRate();
        }

        public Time getStopTime() {
            return backup.getStopTime();
        }

        public Time getSyncTime() {
            return current.getSyncTime();
        }

        public TimeBase getTimeBase() {
            return current.getTimeBase();
        }

        public Time mapToTimeBase(Time t) throws ClockStoppedException {
            return current.mapToTimeBase(t);
        }

        protected void reset(boolean useMaster) {
            if (master != null && useMaster) current = master; else {
                if (master != null) {
                    synchronized (backup) {
                        boolean started = false;
                        if (backup.getState() == BasicClock.STARTED) {
                            backup.stop();
                            started = true;
                        }
                        backup.setMediaTime(master.getMediaTime());
                        if (started) backup.syncStart(backup.getTimeBase().getTime());
                    }
                }
                current = backup;
            }
        }

        public void setMaster(Clock master) {
            this.master = master;
            current = (master == null ? backup : master);
            if (master != null) {
                try {
                    backup.setTimeBase(master.getTimeBase());
                } catch (IncompatibleTimeBaseException e) {
                }
            }
        }

        public void setMediaTime(Time now) {
            synchronized (backup) {
                if (backup.getState() == BasicClock.STARTED) {
                    backup.stop();
                    backup.setMediaTime(now);
                    backup.syncStart(backup.getTimeBase().getTime());
                } else backup.setMediaTime(now);
            }
        }

        public float setRate(float factor) {
            return backup.setRate(factor);
        }

        public void setStopTime(Time t) {
            synchronized (backup) {
                backup.setStopTime(t);
            }
        }

        public void setTimeBase(TimeBase tb) throws IncompatibleTimeBaseException {
            synchronized (backup) {
                backup.setTimeBase(tb);
            }
        }

        public void stop() {
            synchronized (backup) {
                backup.stop();
            }
        }

        public void syncStart(Time tbt) {
            synchronized (backup) {
                if (backup.getState() != BasicClock.STARTED) backup.syncStart(tbt);
            }
        }
    }

    protected BasicPlayer player;

    protected DataSource dsource;

    protected Vector modules;

    protected Vector filters;

    protected Vector sinks;

    protected Vector waitPrefetched;

    protected Vector waitStopped;

    protected Vector waitEnded;

    protected Vector waitResetted;

    protected Track tracks[];

    protected Demultiplexer parser;

    protected BasicSinkModule masterSink = null;

    protected BasicSourceModule source;

    protected SlaveClock slaveClock;

    private boolean internalErrorOccurred = false;

    protected boolean prefetched = false;

    protected boolean started = false;

    private boolean dataPathBlocked = false;

    private boolean useMoreRenderBuffer = false;

    private boolean deallocated = false;

    public boolean prefetchEnabled = true;

    protected static boolean needSavingDB = false;

    private Time timeBeforeAbortPrefetch = null;

    private float rate = 1.0f;

    protected BitRateControl bitRateControl;

    protected FrameRateControl frameRateControl;

    protected FramePositioningControl framePositioningControl = null;

    private long latency = 0;

    protected Container container = null;

    public static boolean TRACE_ON = false;

    protected BasicTrackControl trackControls[] = new BasicTrackControl[0];

    protected ProgressControl progressControl;

    private long realizeTime;

    private long prefetchTime;

    static String NOT_CONFIGURED_ERROR = "cannot be called before configured";

    static String NOT_REALIZED_ERROR = "cannot be called before realized";

    static String STARTED_ERROR = "cannot be called after started";

    /**
     * Turn on memory trace to assit debugging.
     */
    public static void setMemoryTrace(boolean on) {
        TRACE_ON = on;
    }

    String configError = "Failed to configure: " + this;

    String configIntError = "  The configure process is being interrupted.\n";

    String configInt2Error = "interrupted while the Processor is being configured.";

    String parseError = "failed to parse the input media.";

    protected String realizeError = "Failed to realize: " + this;

    protected String timeBaseError = "  Cannot manage the different time bases.\n";

    protected String genericProcessorError = "cannot handle the customized options set on the Processor.\nCheck " + Log.fileName + " for full details.";

    String prefetchError = "Failed to prefetch: " + this;

    RTPInfo rtpInfo = null;

    boolean testedRTP = false;

    boolean prefetchLogged = false;

    long markedDataStartTime = 0;

    boolean reportOnce = false;

    long lastBitRate = 0;

    long lastStatsTime = 0;

    static boolean USE_MASTER = true;

    static boolean USE_BACKUP = false;

    /**
     * Return true if the given format is a raw video format.
     */
    static boolean isRawVideo(Format fmt) {
        return (fmt instanceof RGBFormat || fmt instanceof YUVFormat);
    }

    static void profile(String msg, long time) {
        Log.profile("Profile: " + msg + ": " + (System.currentTimeMillis() - time) + " ms\n");
    }

    public PlaybackEngine(BasicPlayer p) {
        long initTime = System.currentTimeMillis();
        player = p;
        createProgressControl();
        setClock(slaveClock = new SlaveClock());
        stopThreadEnabled = false;
        profile("instantiation", initTime);
    }

    /**
     * Called when doConfigure() is aborted.
     */
    @Override
    protected synchronized void abortConfigure() {
        if (source != null) source.abortRealize();
    }

    /**
     * Called when the prefetch() is aborted, i.e. deallocate() was called while
     * prefetching. Release all resources claimed previously by the prefetch
     * call. Override this to implement subclass behavior.
     */
    @Override
    protected synchronized void abortPrefetch() {
        timeBeforeAbortPrefetch = getMediaTime();
        doReset();
        StateTransistor m;
        int size = modules.size();
        for (int i = 0; i < size; i++) {
            m = (StateTransistor) modules.elementAt(i);
            m.abortPrefetch();
        }
        deallocated = true;
    }

    /**
     * Called when the realize() is aborted, i.e. deallocate() was called while
     * realizing. Release all resources claimed previously by the realize()
     * call.
     */
    @Override
    protected synchronized void abortRealize() {
        StateTransistor m;
        int size = modules.size();
        for (int i = 0; i < size; i++) {
            m = (StateTransistor) modules.elementAt(i);
            m.abortRealize();
        }
    }

    /**
     * Return true if audio is present.
     */
    public boolean audioEnabled() {
        for (int i = 0; i < trackControls.length; i++) {
            if (trackControls[i].isEnabled() && trackControls[i].getOriginalFormat() instanceof AudioFormat) return true;
        }
        return false;
    }

    public void bufferPrefetched(Module src) {
        if (!prefetchEnabled) return;
        if (src instanceof BasicSinkModule) {
            synchronized (waitPrefetched) {
                if (waitPrefetched.contains(src)) waitPrefetched.removeElement(src);
                if (waitPrefetched.isEmpty()) {
                    waitPrefetched.notifyAll();
                    if (!prefetchLogged) {
                        profile("prefetch", prefetchTime);
                        prefetchLogged = true;
                    }
                    if (getState() != Controller.Started && getTargetState() != Controller.Started) {
                        source.pause();
                    }
                    prefetched = true;
                }
            }
        }
    }

    /**
     * Construct a track (connected modules) from the specified node graph.
     * Return a non-null GraphNode if the track cannot be built. The non-null
     * node returned is the node that failed to be opened.
     */
    protected GraphNode buildTrackFromGraph(BasicTrackControl tc, GraphNode node) {
        BasicModule src = null, dst = null;
        InputConnector ic = null;
        OutputConnector oc = null;
        boolean lastNode = true;
        Vector used = new Vector(5);
        int indent = 0;
        if (node.plugin == null) {
            return null;
        }
        Log.setIndent(indent++);
        while (node != null && node.plugin != null) {
            if ((src = createModule(node, used)) == null) {
                Log.error("Internal error: buildTrackFromGraph");
                node.failed = true;
                return node;
            }
            if (lastNode) {
                if (src instanceof BasicRendererModule) {
                    tc.rendererModule = (BasicRendererModule) src;
                    if (useMoreRenderBuffer && tc.rendererModule.getRenderer() instanceof AudioRenderer) setRenderBufferSize(tc.rendererModule.getRenderer());
                } else if (src instanceof BasicFilterModule) {
                    tc.lastOC = src.getOutputConnector(null);
                    tc.lastOC.setFormat(node.output);
                }
                lastNode = false;
            }
            ic = src.getInputConnector(null);
            ic.setFormat(node.input);
            if (dst != null) {
                oc = src.getOutputConnector(null);
                ic = dst.getInputConnector(null);
                oc.setFormat(ic.getFormat());
            }
            src.setController(this);
            if (!src.doRealize()) {
                Log.setIndent(indent--);
                node.failed = true;
                return node;
            }
            if (oc != null && ic != null) connectModules(oc, ic, dst);
            dst = src;
            node = node.prev;
        }
        dst = src;
        while (true) {
            dst.setModuleListener(this);
            modules.addElement(dst);
            tc.modules.addElement(dst);
            if (dst instanceof BasicFilterModule) filters.addElement(dst); else if (dst instanceof BasicSinkModule) sinks.addElement(dst);
            oc = dst.getOutputConnector(null);
            if (oc == null || (ic = oc.getInputConnector()) == null || (dst = (BasicModule) ic.getModule()) == null) break;
        }
        tc.firstOC.setFormat(tc.getOriginalFormat());
        ic = src.getInputConnector(null);
        Format fmt = ic.getFormat();
        if (fmt == null || !fmt.equals(tc.getOriginalFormat())) ic.setFormat(tc.getOriginalFormat());
        connectModules(tc.firstOC, ic, src);
        Log.setIndent(indent--);
        return null;
    }

    /**
     * Connect the two given modules.
     */
    protected void connectModules(OutputConnector oc, InputConnector ic, BasicModule dst) {
        if (dst instanceof BasicRendererModule) oc.setProtocol(ic.getProtocol()); else ic.setProtocol(oc.getProtocol());
        oc.connectTo(ic, ic.getFormat());
    }

    /**
     * Create a realized filter module given the plugIn codec.
     */
    protected BasicModule createModule(GraphNode n, Vector used) {
        PlugIn p;
        BasicModule m = null;
        if (n.plugin == null) return null;
        if (used.contains(n.plugin)) {
            if (n.cname == null || (p = SimpleGraphBuilder.createPlugIn(n.cname, -1)) == null) {
                Log.write("Failed to instantiate " + n.cname);
                return null;
            }
        } else {
            p = n.plugin;
            used.addElement(p);
        }
        if ((n.type == -1 || n.type == PlugInManager.RENDERER) && p instanceof Renderer) {
            m = new BasicRendererModule((Renderer) p);
        } else if ((n.type == -1 || n.type == PlugInManager.CODEC) && p instanceof Codec) {
            m = new BasicFilterModule((Codec) p);
        }
        return m;
    }

    /**
     * Create the progress status control.
     */
    public void createProgressControl() {
        StringControl frameRate;
        StringControl bitRate;
        StringControl videoProps;
        StringControl audioProps;
        StringControl videoCodec;
        StringControl audioCodec;
        frameRate = new StringControlAdapter();
        frameRate.setValue(" N/A");
        bitRate = new StringControlAdapter();
        bitRate.setValue(" N/A");
        videoProps = new StringControlAdapter();
        videoProps.setValue(" N/A");
        audioProps = new StringControlAdapter();
        audioProps.setValue(" N/A");
        audioCodec = new StringControlAdapter();
        audioCodec.setValue(" N/A");
        videoCodec = new StringControlAdapter();
        videoCodec.setValue(" N/A");
        progressControl = new ProgressControlAdapter(frameRate, bitRate, videoProps, audioProps, videoCodec, audioCodec);
    }

    protected Component createVisualContainer(Vector visuals) {
        Boolean hint = (Boolean) Manager.getHint(Manager.LIGHTWEIGHT_RENDERER);
        if (container == null) {
            if (hint == null || hint.booleanValue() == false) {
                container = new HeavyPanel(visuals);
            } else {
                container = new LightPanel(visuals);
            }
            container.setLayout(new FlowLayout());
            container.setBackground(Color.black);
            for (int i = 0; i < visuals.size(); i++) {
                Component c = (Component) visuals.elementAt(i);
                container.add(c);
                c.setSize(c.getPreferredSize());
            }
        }
        return container;
    }

    public void dataBlocked(Module src, boolean blocked) {
        dataPathBlocked = blocked;
        if (blocked) {
            resetPrefetchedList();
            resetResettedList();
        }
        if (getTargetState() != Controller.Started) {
            return;
        }
        if (blocked) {
            localStop();
            setTargetState(Controller.Started);
            sendEvent(new RestartingEvent(this, Controller.Started, Controller.Prefetching, Controller.Started, getMediaTime()));
        } else {
            sendEvent(new StartEvent(this, Controller.Prefetched, Controller.Started, Controller.Started, getMediaTime(), getTimeBase().getTime()));
        }
    }

    /**
     * Invoked by close() to cleanup the Controller. Override this to implement
     * subclass behavior.
     */
    @Override
    protected synchronized void doClose() {
        if (modules == null) {
            if (source != null) source.doClose();
            return;
        }
        if (getState() == Started) localStop();
        if (getState() == Prefetched) doReset();
        StateTransistor m;
        int size = modules.size();
        for (int i = 0; i < size; i++) {
            m = (StateTransistor) modules.elementAt(i);
            m.doClose();
        }
        if (needSavingDB) {
            Resource.saveDB();
            needSavingDB = false;
        }
    }

    /**
     * Configuring the engine.
     */
    @Override
    protected boolean doConfigure() {
        if (!doConfigure1()) return false;
        String names[] = source.getOutputConnectorNames();
        trackControls = new BasicTrackControl[tracks.length];
        for (int i = 0; i < tracks.length; i++) {
            trackControls[i] = new PlayerTControl(this, tracks[i], source.getOutputConnector(names[i]));
        }
        return doConfigure2();
    }

    /**
     * Configure - Part I.
     */
    protected boolean doConfigure1() {
        long parsingTime = System.currentTimeMillis();
        modules = new Vector();
        filters = new Vector();
        sinks = new Vector();
        waitPrefetched = new Vector();
        waitStopped = new Vector();
        waitEnded = new Vector();
        waitResetted = new Vector();
        source.setModuleListener(this);
        source.setController(this);
        modules.addElement(source);
        if (!source.doRealize()) {
            Log.error(configError);
            if (source.errMsg != null) Log.error("  " + source.errMsg + "\n");
            player.processError = parseError;
            return false;
        }
        if (isInterrupted()) {
            Log.error(configError);
            Log.error(configIntError);
            player.processError = configInt2Error;
            return false;
        }
        if ((parser = source.getDemultiplexer()) == null) {
            Log.error(configError);
            Log.error("  Cannot obtain demultiplexer for the source.\n");
            player.processError = parseError;
            return false;
        }
        try {
            tracks = parser.getTracks();
        } catch (Exception e) {
            Log.error(configError);
            Log.error("  Cannot obtain tracks from the demultiplexer: " + e + "\n");
            player.processError = parseError;
            return false;
        }
        if (isInterrupted()) {
            Log.error(configError);
            Log.error(configIntError);
            player.processError = configInt2Error;
            return false;
        }
        profile("parsing", parsingTime);
        return true;
    }

    /**
     * Configure - Part II.
     */
    protected boolean doConfigure2() {
        if (parser.isPositionable() && parser.isRandomAccess()) {
            Track master = FramePositioningAdapter.getMasterTrack(tracks);
            if (master != null) {
                framePositioningControl = new FramePositioningAdapter(player, master);
            }
        }
        return true;
    }

    /**
     * Called by deallocate(). Subclasses should implement this for its specific
     * behavior.
     */
    @Override
    protected void doDeallocate() {
    }

    /**
     * Called when the prefetch() has failed.
     */
    @Override
    protected synchronized void doFailedPrefetch() {
        StateTransistor m;
        int size = modules.size();
        for (int i = 0; i < size; i++) {
            m = (StateTransistor) modules.elementAt(i);
            m.doFailedPrefetch();
        }
        super.doFailedPrefetch();
    }

    /**
     * Called when realize() has failed.
     */
    @Override
    protected synchronized void doFailedRealize() {
        StateTransistor m;
        int size = modules.size();
        for (int i = 0; i < size; i++) {
            m = (StateTransistor) modules.elementAt(i);
            m.doFailedRealize();
        }
        super.doFailedRealize();
    }

    /**
     * The stub function to perform the steps to prefetch the controller.
     * 
     * @return true if successful.
     */
    @Override
    protected synchronized boolean doPrefetch() {
        if (prefetched) return true;
        return doPrefetch1() && doPrefetch2();
    }

    /**
     * doPrefetch - Part I
     */
    protected boolean doPrefetch1() {
        if (timeBeforeAbortPrefetch != null) {
            doSetMediaTime(timeBeforeAbortPrefetch);
            timeBeforeAbortPrefetch = null;
        }
        prefetchTime = System.currentTimeMillis();
        resetPrefetchedList();
        if (!source.doPrefetch()) {
            Log.error(prefetchError);
            if (dsource != null) Log.error("  Cannot prefetch the source: " + dsource.getLocator() + "\n");
            return false;
        }
        boolean atLeastOneTrack = false;
        boolean usedToFailed;
        for (int i = 0; i < trackControls.length; i++) {
            usedToFailed = trackControls[i].prefetchFailed;
            if (usedToFailed && getState() > Prefetching) continue;
            if (trackControls[i].prefetchTrack()) {
                atLeastOneTrack = true;
                if (usedToFailed) {
                    if (!manageTimeBases()) {
                        Log.error(prefetchError);
                        Log.error(timeBaseError);
                        return false;
                    }
                    doSetMediaTime(getMediaTime());
                }
            } else {
                trackControls[i].prError();
                if (trackControls[i].isTimeBase()) {
                    if (!manageTimeBases()) {
                        Log.error(prefetchError);
                        Log.error(timeBaseError);
                        player.processError = timeBaseError;
                        return false;
                    }
                }
                if (trackControls[i].getFormat() instanceof AudioFormat && trackControls[i].rendererFailed) {
                    player.processError = "cannot open the audio device.";
                }
            }
        }
        if (!atLeastOneTrack) {
            Log.error(prefetchError);
            return false;
        }
        player.processError = null;
        return true;
    }

    /**
     * doPrefetch - Part II
     */
    protected boolean doPrefetch2() {
        if (prefetchEnabled) {
            synchronized (waitPrefetched) {
                source.doStart();
                try {
                    if (!waitPrefetched.isEmpty()) {
                        waitPrefetched.wait(3000);
                    }
                } catch (InterruptedException e) {
                }
            }
        } else prefetched = true;
        deallocated = false;
        return true;
    }

    /**
     * @return true if successful.
     */
    @Override
    protected synchronized boolean doRealize() {
        return doRealize1() && doRealize2();
    }

    /**
     * doRealize Part I
     */
    protected boolean doRealize1() {
        Log.comment("Building flow graph for: " + dsource.getLocator() + "\n");
        realizeTime = System.currentTimeMillis();
        boolean atLeastOneTrack = false;
        int trackID = 0;
        int numTracks = getNumTracks();
        for (int i = 0; i < trackControls.length; i++) {
            if (!trackControls[i].isEnabled()) continue;
            Log.setIndent(0);
            Log.comment("Building Track: " + i);
            if (trackControls[i].buildTrack(trackID, numTracks)) {
                atLeastOneTrack = true;
                trackControls[i].setEnabled(true);
            } else if (trackControls[i].isCustomized()) {
                Log.error(realizeError);
                trackControls[i].prError();
                player.processError = genericProcessorError;
                return false;
            } else {
                trackControls[i].setEnabled(false);
                Log.warning("Failed to handle track " + i);
                trackControls[i].prError();
            }
            if (isInterrupted()) {
                Log.error(realizeError);
                Log.error("  The graph building process is being interrupted.\n");
                player.processError = "interrupted while the player is being constructed.";
                return false;
            }
            trackID++;
            Log.write("\n");
        }
        if (!atLeastOneTrack) {
            Log.error(realizeError);
            player.processError = "input media not supported: " + getCodecList();
            return false;
        }
        return true;
    }

    /**
     * doRealize Part II
     */
    protected boolean doRealize2() {
        if (!manageTimeBases()) {
            Log.error(realizeError);
            Log.error(timeBaseError);
            player.processError = timeBaseError;
            return false;
        }
        Log.comment("Here's the completed flow graph:");
        traceGraph(source);
        Log.write("\n");
        profile("graph building", realizeTime);
        realizeTime = System.currentTimeMillis();
        updateFormats();
        profile("realize, post graph building", realizeTime);
        return true;
    }

    /**
     * The real reset code. This is a blocking call and should only be called
     * when the engine is stopped.
     */
    protected synchronized void doReset() {
        synchronized (waitResetted) {
            resetResettedList();
            BasicModule m;
            int size = modules.size();
            for (int i = size - 1; i >= 0; i--) {
                m = (BasicModule) modules.elementAt(i);
                if (!m.prefetchFailed()) m.reset();
            }
            BasicSinkModule bsm;
            size = sinks.size();
            for (int i = 0; i < size; i++) {
                bsm = (BasicSinkModule) sinks.elementAt(i);
                if (!bsm.prefetchFailed()) bsm.triggerReset();
            }
            if (!waitResetted.isEmpty()) {
                try {
                    waitResetted.wait(3000);
                } catch (Exception e) {
                }
            }
            size = sinks.size();
            for (int i = 0; i < size; i++) {
                bsm = (BasicSinkModule) sinks.elementAt(i);
                if (!bsm.prefetchFailed()) bsm.doneReset();
            }
        }
        prefetched = false;
    }

    @Override
    protected void doSetMediaTime(Time when) {
        slaveClock.setMediaTime(when);
        Time t;
        if ((t = source.setPosition(when, 0)) == null) t = when;
        int size = sinks.size();
        BasicSinkModule bsm;
        for (int i = 0; i < size; i++) {
            bsm = (BasicSinkModule) sinks.elementAt(i);
            bsm.doSetMediaTime(when);
            bsm.setPreroll(when.getNanoseconds(), t.getNanoseconds());
        }
    }

    @Override
    public synchronized float doSetRate(float r) {
        if (r <= 0f) r = 1.0f;
        if (r == rate) return r;
        if (masterSink == null) r = getClock().setRate(r); else r = masterSink.doSetRate(r);
        BasicModule m;
        int size = modules.size();
        for (int i = 0; i < size; i++) {
            m = (BasicModule) modules.elementAt(i);
            if (m != masterSink) m.doSetRate(r);
        }
        rate = r;
        return r;
    }

    /**
     * Start immediately. Invoked from start(tbt) when the scheduled start time
     * is reached. Use the public start(tbt) method for the public interface.
     * Override this to implement subclass behavior.
     */
    @Override
    protected synchronized void doStart() {
        if (started) return;
        doStart1();
        doStart2();
    }

    /**
     * doStart - Part I.
     */
    protected void doStart1() {
        if (dsource instanceof CaptureDevice && !isRTP()) reset();
        resetPrefetchedList();
        resetStoppedList();
        resetEndedList();
        for (int i = 0; i < trackControls.length; i++) {
            if (trackControls[i].isEnabled()) trackControls[i].startTrack();
        }
    }

    /**
     * doStart - Part II
     */
    protected void doStart2() {
        source.doStart();
        started = true;
        prefetched = true;
    }

    /**
     * Invoked from stop(). Override this to implement subclass behavior.
     */
    @Override
    protected synchronized void doStop() {
        if (!started) return;
        doStop1();
        doStop2();
    }

    /**
     * doStop - Part I.
     */
    protected void doStop1() {
        resetPrefetchedList();
        source.doStop();
        for (int i = 0; i < trackControls.length; i++) {
            if (trackControls[i].isEnabled()) trackControls[i].stopTrack();
        }
    }

    /**
     * doStop - Part II.
     */
    protected void doStop2() {
        if (!prefetchEnabled) source.pause();
        started = false;
    }

    /**
     * Returns the DataSink which holds the timebase.
     */
    protected BasicSinkModule findMasterSink() {
        for (int i = 0; i < trackControls.length; i++) {
            if (!trackControls[i].isEnabled()) continue;
            if (trackControls[i].rendererModule != null && trackControls[i].rendererModule.getClock() != null) {
                return trackControls[i].rendererModule;
            }
        }
        return null;
    }

    public void formatChanged(Module src, Format oldFormat, Format newFormat) {
        Log.comment(src + ": input format changed: " + newFormat);
        if (src instanceof BasicRendererModule && oldFormat instanceof VideoFormat && newFormat instanceof VideoFormat) {
            Dimension s1 = ((VideoFormat) oldFormat).getSize();
            Dimension s2 = ((VideoFormat) newFormat).getSize();
            if (s2 != null && (s1 == null || !s1.equals(s2))) {
                sendEvent(new SizeChangeEvent(this, s2.width, s2.height, 1.0f));
            }
        }
    }

    public void formatChangedFailure(Module src, Format oldFormat, Format newFormat) {
        if (!internalErrorOccurred) {
            sendEvent(new InternalErrorEvent(this, "Internal module " + src + ": failed to handle a data format change!"));
            internalErrorOccurred = true;
            close();
        }
    }

    public void framesBehind(Module src, float frames, InputConnector ic) {
        OutputConnector oc;
        BasicFilterModule bfm;
        while (ic != null) {
            if ((oc = ic.getOutputConnector()) == null) break;
            if ((src = oc.getModule()) == null) break;
            if (!(src instanceof BasicFilterModule)) break;
            bfm = (BasicFilterModule) src;
            bfm.setFramesBehind(frames);
            ic = src.getInputConnector(null);
        }
    }

    protected long getBitRate() {
        return source.getBitsRead();
    }

    public String getCNAME() {
        if (rtpInfo == null) {
            if ((rtpInfo = (RTPInfo) dsource.getControl(RTPInfo.class.getName())) == null) return null;
        }
        return rtpInfo.getCNAME();
    }

    String getCodecList() {
        String list = "";
        Format fmt;
        for (int i = 0; i < trackControls.length; i++) {
            fmt = trackControls[i].getOriginalFormat();
            if (fmt == null || fmt.getEncoding() == null) continue;
            list += fmt.getEncoding();
            if (fmt instanceof VideoFormat) list += " video"; else if (fmt instanceof AudioFormat) list += " audio";
            if (i + 1 < trackControls.length) list += ", ";
        }
        return list;
    }

    /**
     * Return a list of <b>Control</b> objects this <b>Controller</b> supports.
     * In this case, it is all the controls from all the modules controlled by
     * this engine.
     * 
     * @return list of <b>Controller</b> controls.
     */
    @Override
    public Control[] getControls() {
        Control controls[];
        Vector cv = new Vector();
        Control c;
        Object cs[];
        Module m;
        int i, size = (modules == null ? 0 : modules.size());
        int otherSize = 0;
        for (i = 0; i < size; i++) {
            m = (Module) modules.elementAt(i);
            cs = m.getControls();
            if (cs == null) continue;
            for (int j = 0; j < cs.length; j++) {
                cv.addElement(cs[j]);
            }
        }
        size = cv.size();
        if (videoEnabled()) {
            if (frameRateControl == null) {
                frameRateControl = new FrameRateAdapter(player, 0f, 0f, 30f, false) {

                    @Override
                    public Component getControlComponent() {
                        return null;
                    }

                    @Override
                    public Object getOwner() {
                        return player;
                    }

                    @Override
                    public float setFrameRate(float rate) {
                        this.value = rate;
                        return -1f;
                    }
                };
            }
        }
        if (bitRateControl == null) {
            bitRateControl = new BitRateA(0, -1, -1, false);
        }
        if (frameRateControl != null) otherSize++;
        if (bitRateControl != null) otherSize++;
        if (framePositioningControl != null) otherSize++;
        controls = new Control[size + otherSize + trackControls.length];
        for (i = 0; i < size; i++) controls[i] = (Control) cv.elementAt(i);
        if (bitRateControl != null) controls[size++] = bitRateControl;
        if (frameRateControl != null) controls[size++] = frameRateControl;
        if (framePositioningControl != null) controls[size++] = framePositioningControl;
        for (i = 0; i < trackControls.length; i++) {
            controls[size + i] = trackControls[i];
        }
        return controls;
    }

    /**
     * Return the duration of the media. It's unknown until we implement a
     * particular node.
     * 
     * @return the duration of the media.
     */
    @Override
    public Time getDuration() {
        return source.getDuration();
    }

    /**
     * Get audio gain control.
     */
    public GainControl getGainControl() {
        return (GainControl) getControl("javax.media.GainControl");
    }

    /**
     * Return the run-time latency. It's the time it takes for a packet to
     * travel from the first module to the last module. The time is computed in
     * nanoseconds.
     */
    public long getLatency() {
        return latency;
    }

    int getNumTracks() {
        int num = 0;
        for (int i = 0; i < trackControls.length; i++) {
            if (trackControls[i].isEnabled()) num++;
        }
        return num;
    }

    /**
     * Get the plugin from a module. For debugging.
     */
    protected PlugIn getPlugIn(BasicModule m) {
        if (m instanceof BasicSourceModule) return ((BasicSourceModule) m).getDemultiplexer();
        if (m instanceof BasicFilterModule) return ((BasicFilterModule) m).getCodec();
        if (m instanceof BasicRendererModule) return ((BasicRendererModule) m).getRenderer();
        return null;
    }

    /**
     * Returns the start latency. Don't know until the particular node is
     * implemented.
     * 
     * @return the start latency.
     */
    @Override
    public Time getStartLatency() {
        if ((state == Unrealized) || (state == Realizing)) throwError(new NotRealizedError("Cannot get start latency from an unrealized controller"));
        return LATENCY_UNKNOWN;
    }

    /**
     * Override the parent's method to not check for realized state. There's no
     * need to.
     */
    @Override
    public TimeBase getTimeBase() {
        return getClock().getTimeBase();
    }

    /**
     * Get the visual component where the video is presented.
     */
    public Component getVisualComponent() {
        Vector visuals = new Vector(1);
        if (modules == null) return null;
        for (int i = 0; i < modules.size(); i++) {
            BasicModule bm = (BasicModule) modules.elementAt(i);
            PlugIn pi = getPlugIn(bm);
            if (pi instanceof VideoRenderer) {
                Component comp = ((VideoRenderer) pi).getComponent();
                if (comp != null) visuals.addElement(comp);
            }
        }
        if (visuals.size() == 0) return null; else if (visuals.size() == 1) return (Component) visuals.elementAt(0); else {
            return createVisualContainer(visuals);
        }
    }

    public void internalErrorOccurred(Module src) {
        if (!internalErrorOccurred) {
            sendEvent(new InternalErrorEvent(this, "Internal module " + src + " failed!"));
            internalErrorOccurred = true;
            close();
        }
    }

    /**
     * The PlaybackEngine is configurable.
     */
    @Override
    protected boolean isConfigurable() {
        return true;
    }

    public boolean isRTP() {
        if (testedRTP) return rtpInfo != null;
        rtpInfo = (RTPInfo) dsource.getControl(RTPInfo.class.getName());
        testedRTP = true;
        return rtpInfo != null;
    }

    /**
     * Given a chain of FilterModules, return the last one of the chain.
     */
    protected BasicModule lastModule(BasicModule bm) {
        OutputConnector oc;
        InputConnector ic;
        oc = bm.getOutputConnector(null);
        while (oc != null && (ic = oc.getInputConnector()) != null) {
            bm = (BasicModule) ic.getModule();
            oc = bm.getOutputConnector(null);
        }
        return bm;
    }

    protected synchronized void localStop() {
        super.stop();
    }

    /**
     * Search and update the master time base.
     */
    boolean manageTimeBases() {
        masterSink = findMasterSink();
        return updateMasterTimeBase();
    }

    public void markedDataArrived(Module src, Buffer buffer) {
        if (src instanceof BasicSourceModule) {
            markedDataStartTime = getMediaNanoseconds();
        } else {
            long t = getMediaNanoseconds() - markedDataStartTime;
            if (t > 0 && t < 1000000000) {
                if (!reportOnce) {
                    Log.comment("Computed latency for video: " + t / 1000000 + " ms\n");
                    reportOnce = true;
                }
                latency = (t + latency) / 2;
            }
        }
    }

    public void mediaEnded(Module src) {
        if (src instanceof BasicSinkModule) {
            synchronized (waitEnded) {
                if (waitEnded.contains(src)) waitEnded.removeElement(src);
                if (waitEnded.isEmpty()) {
                    started = false;
                    stopControllerOnly();
                    sendEvent(new EndOfMediaEvent(this, Started, Prefetched, getTargetState(), getMediaTime()));
                    slaveClock.reset(USE_MASTER);
                } else if (src == masterSink) {
                    slaveClock.reset(USE_BACKUP);
                }
            }
        }
    }

    public void pluginTerminated(Module src) {
        if (!internalErrorOccurred) {
            sendEvent(new ControllerClosedEvent(this));
            internalErrorOccurred = true;
            close();
        }
    }

    /**
     * Flush (reset) the flow graph. This is a blocking call and should only be
     * called when the engine is stopped.
     */
    protected synchronized void reset() {
        if (started || !prefetched || dataPathBlocked) return;
        doReset();
    }

    protected void resetBitRate() {
        source.resetBitsRead();
    }

    /**
     * Reset the renderer lists.
     */
    private void resetEndedList() {
        synchronized (waitEnded) {
            waitEnded.removeAllElements();
            int size = sinks.size();
            BasicSinkModule bsm;
            for (int i = 0; i < size; i++) {
                bsm = (BasicSinkModule) sinks.elementAt(i);
                if (!bsm.prefetchFailed()) waitEnded.addElement(bsm);
            }
            waitEnded.notifyAll();
        }
    }

    /**
     * Reset the renderer lists.
     */
    private void resetPrefetchedList() {
        synchronized (waitPrefetched) {
            waitPrefetched.removeAllElements();
            int size = sinks.size();
            BasicSinkModule bsm;
            for (int i = 0; i < size; i++) {
                bsm = (BasicSinkModule) sinks.elementAt(i);
                if (!bsm.prefetchFailed()) waitPrefetched.addElement(bsm);
            }
            waitPrefetched.notifyAll();
        }
    }

    /**
     * Reset the renderer lists.
     */
    private void resetResettedList() {
        synchronized (waitResetted) {
            waitResetted.removeAllElements();
            int size = sinks.size();
            BasicSinkModule bsm;
            for (int i = 0; i < size; i++) {
                bsm = (BasicSinkModule) sinks.elementAt(i);
                if (!bsm.prefetchFailed()) waitResetted.addElement(bsm);
            }
            waitResetted.notifyAll();
        }
    }

    /**
     * Reset the renderer lists.
     */
    private void resetStoppedList() {
        synchronized (waitStopped) {
            waitStopped.removeAllElements();
            int size = sinks.size();
            BasicSinkModule bsm;
            for (int i = 0; i < size; i++) {
                bsm = (BasicSinkModule) sinks.elementAt(i);
                if (!bsm.prefetchFailed()) waitStopped.addElement(bsm);
            }
            waitStopped.notifyAll();
        }
    }

    public void resetted(Module src) {
        synchronized (waitResetted) {
            if (waitResetted.contains(src)) {
                waitResetted.removeElement(src);
            }
            if (waitResetted.isEmpty()) {
                waitResetted.notifyAll();
            }
        }
    }

    /**
     * Override BasicController's setMediaTime so as not to set the media time
     * on the master clock twice.
     */
    @Override
    public synchronized void setMediaTime(Time when) {
        if (state < Realized) throwError(new NotRealizedError("Cannot set media time on a unrealized controller"));
        if (when.getNanoseconds() == getMediaNanoseconds()) return;
        reset();
        timeBeforeAbortPrefetch = null;
        doSetMediaTime(when);
        doPrefetch();
        sendEvent(new MediaTimeSetEvent(this, when));
    }

    public void setProgressControl(ProgressControl p) {
        progressControl = p;
    }

    protected void setRenderBufferSize(Renderer r) {
        BufferControl bc = (BufferControl) r.getControl(BufferControl.class.getName());
        if (bc != null) bc.setBufferLength(2000);
    }

    /**
     * Verifies to see if the engine accepts the given source.
     */
    public void setSource(DataSource ds) throws IOException, IncompatibleSourceException {
        try {
            source = BasicSourceModule.createModule(ds);
        } catch (IOException ioe) {
            Log.warning("Input DataSource: " + ds);
            Log.warning("  Failed with IO exception: " + ioe.getMessage());
            throw ioe;
        } catch (IncompatibleSourceException ise) {
            Log.warning("Input DataSource: " + ds);
            Log.warning("  is not compatible with the MediaEngine.");
            Log.warning("  It's likely that the DataSource is required to extend PullDataSource;");
            Log.warning("  and that its source streams implement the Seekable interface ");
            Log.warning("  and with random access capability.");
            throw ise;
        }
        if (source == null) throw new IncompatibleSourceException();
        source.setController(this);
        dsource = ds;
        if (dsource instanceof Streamable && !((Streamable) dsource).isPrefetchable()) {
            prefetchEnabled = false;
            dataPathBlocked = true;
        }
        if (dsource instanceof CaptureDevice) prefetchEnabled = false;
    }

    /**
     * Override BasicController.setStopTime to allow for more accurate stop time
     * set.
     */
    @Override
    public void setStopTime(Time t) {
        if (getState() < Realized) throwError(new NotRealizedError("Cannot set stop time on an unrealized controller."));
        if (getStopTime() != null && getStopTime().getNanoseconds() != t.getNanoseconds()) sendEvent(new StopTimeChangeEvent(this, t));
        if (getState() == Started && t != Clock.RESET && t.getNanoseconds() < getMediaNanoseconds()) {
            localStop();
            setStopTime(Clock.RESET);
            sendEvent(new StopAtTimeEvent(this, getState(), Prefetched, getTargetState(), getMediaTime()));
        } else {
            getClock().setStopTime(t);
            int size = sinks.size();
            BasicSinkModule bsm;
            for (int i = 0; i < size; i++) {
                bsm = (BasicSinkModule) sinks.elementAt(i);
                bsm.setStopTime(t);
            }
        }
    }

    /**
     * Override the parent's method to not check for realized state. There's no
     * need to.
     */
    @Override
    public void setTimeBase(TimeBase tb) throws IncompatibleTimeBaseException {
        getClock().setTimeBase(tb);
        if (sinks == null) return;
        int size = sinks.size();
        BasicSinkModule bsm;
        for (int i = 0; i < size; i++) {
            bsm = (BasicSinkModule) sinks.elementAt(i);
            bsm.setTimeBase(tb);
        }
    }

    /**
     * This is stop by request.
     */
    @Override
    public synchronized void stop() {
        super.stop();
        sendEvent(new StopByRequestEvent(this, Started, Prefetched, getTargetState(), getMediaTime()));
    }

    public void stopAtTime(Module src) {
        if (src instanceof BasicSinkModule) {
            synchronized (waitStopped) {
                if (waitStopped.contains(src)) waitStopped.removeElement(src);
                if (waitStopped.isEmpty() || waitEnded.size() == 1 && waitEnded.contains(src)) {
                    started = false;
                    stopControllerOnly();
                    setStopTime(Clock.RESET);
                    sendEvent(new StopAtTimeEvent(this, Started, Prefetched, getTargetState(), getMediaTime()));
                    slaveClock.reset(USE_MASTER);
                } else if (src == masterSink) {
                    slaveClock.reset(USE_BACKUP);
                }
            }
        }
    }

    /**
     * Trace the flow graph for debugging.
     */
    void traceGraph(BasicModule source) {
        Module m;
        OutputConnector oc;
        InputConnector ic;
        String names[];
        names = source.getOutputConnectorNames();
        for (int i = 0; i < names.length; i++) {
            oc = source.getOutputConnector(names[i]);
            if ((ic = oc.getInputConnector()) == null) continue;
            if ((m = ic.getModule()) == null) continue;
            Log.write("  " + getPlugIn(source));
            Log.write("     connects to: " + getPlugIn((BasicModule) m));
            Log.write("     format: " + oc.getFormat());
            traceGraph((BasicModule) m);
        }
    }

    /**
     * Update the format info per track on the progress control.
     */
    public void updateFormats() {
        for (int i = 0; i < trackControls.length; i++) {
            trackControls[i].updateFormat();
        }
    }

    /**
     * Update the master timebase on all the modules.
     */
    boolean updateMasterTimeBase() {
        BasicSinkModule bsm;
        int size = sinks.size();
        if (masterSink != null) slaveClock.setMaster(masterSink.getClock()); else slaveClock.setMaster(null);
        for (int i = 0; i < size; i++) {
            bsm = (BasicSinkModule) sinks.elementAt(i);
            if (bsm != masterSink && !bsm.prefetchFailed()) {
                try {
                    bsm.setTimeBase(slaveClock.getTimeBase());
                } catch (IncompatibleTimeBaseException e) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Update the aggregate bit rate and frame rate per track on the progress
     * control.
     */
    public void updateRates() {
        if (getState() < Realized) return;
        long now = System.currentTimeMillis();
        long rate, avg;
        if (now == lastStatsTime) rate = lastBitRate; else rate = (long) (getBitRate() * 8.0 / (now - lastStatsTime) * 1000.0);
        avg = (lastBitRate + rate) / 2;
        if (bitRateControl != null) {
            bitRateControl.setBitRate((int) avg);
        }
        lastBitRate = rate;
        lastStatsTime = now;
        resetBitRate();
        for (int i = 0; i < trackControls.length; i++) {
            trackControls[i].updateRates(now);
        }
        source.checkLatency();
    }

    /**
     * Return true if video is present.
     */
    public boolean videoEnabled() {
        for (int i = 0; i < trackControls.length; i++) {
            if (trackControls[i].isEnabled() && trackControls[i].getOriginalFormat() instanceof VideoFormat) return true;
        }
        return false;
    }
}
