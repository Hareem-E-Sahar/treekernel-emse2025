package hypercast.DT;

import hypercast.*;
import hypercast.adapters.*;
import hypercast.util.XmlUtil;
import java.io.*;
import java.util.*;
import java.net.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.xpath.*;

/**
 * @author HyperCast Team
 * @author Haiyong Wang
 * @author Jianping Wang
 * @version 3.0, Jan. 08, 2004 
 */
public class GNP_Landmark implements I_AdapterCallback, I_Node {

    /** the total number of probes in one round of calculation*/
    static final int probeNumber = 6;

    /** the interval between probes, in terms of mili-second*/
    static final int probeInterval = 3000;

    /** the maximal waiting time of a probe */
    static final int maxReplyTime = 4000;

    /** the adapter used for sending/receving GNP probes */
    private I_UnicastAdapter adapter;

    /** the landmarks */
    private I_PhysicalAddress[] landmarks;

    /** the local physical address */
    private I_PhysicalAddress myPhyAddress;

    /** The property prefix, it can be changed by constructors. */
    protected static String PROPERTY_PROTO_PREFIX = "/Public/Node/DTBuddyList/";

    /**
     * Index of this landmark
     * Note the first landmark, index = 0, is the one who initiates
     * the probes, collects the measurement and then output the logical
     * coordinates
     */
    private int index;

    /** add some randomness between probes to avoid overload a landmark */
    private Random rand = new Random();

    /**
     * the delay measurement matrix
     * It should be of # of landmarks * # of landmarks dimension
     */
    private float[][] delays;

    /**
     * the measurement array from this landmark
     * It should be of # of landmarks * probeNumber dimension
     */
    private long[][] measurements;

    /** config object */
    private HyperCastConfig config;

    /** dimension is equal the # of landmarks */
    private int[] completedProbes;

    private long[] startTime;

    public GNP_Landmark(HyperCastConfig config, I_UnicastAdapter adapter, String prefix) {
        if (prefix != null) PROPERTY_PROTO_PREFIX = prefix;
        this.config = config;
        initialize(adapter);
    }

    /**
	 * Constructor.
	 * 
	 * @param cf		Name of configuration file.
	 * @param addrStr	String used to create address. It is not used in current version.
	 * @param prefix	Property prefix.
	 */
    public GNP_Landmark(String cf, String addrStr, String prefix) {
        if (prefix != null) PROPERTY_PROTO_PREFIX = prefix;
        try {
            config = HyperCastConfig.createConfig(cf);
        } catch (Exception e) {
            throw new HyperCastFatalRuntimeException("Error occured while trying to create overlay.", e);
        }
        AdapterFactory adapterF = new AdapterFactory();
        adapter = adapterF.createAdapter(config, null, "NodeAdapter", null);
        initialize(adapter);
    }

    /**
	 * Initialize this object with given adapter instance.
	 * 
	 * @param adapter	I_UnicastAdapter instance.
	 */
    void initialize(I_UnicastAdapter adapter) {
        adapter.setCallback(this);
        adapter.Start();
        myPhyAddress = adapter.createPhysicalAddress();
        int lmNum = config.getNonNegativeIntAttribute(XmlUtil.createXPath(PROPERTY_PROTO_PREFIX + "Coords/USE_LM/LandmarkNum"));
        if (lmNum < 3) {
            throw new HyperCastFatalRuntimeException(" not enough landmarks to perform the measurement");
        }
        landmarks = new I_PhysicalAddress[lmNum];
        for (int i = 0; i < lmNum; i++) {
            String s = null;
            try {
                String addrType = config.getTextAttribute(XmlUtil.createXPath(PROPERTY_PROTO_PREFIX + "Coords/USE_LM/Landmark[" + (i + 1) + "]/UnderlayAddress"));
                s = config.getTextAttribute(XmlUtil.createXPath(PROPERTY_PROTO_PREFIX + "Coords/USE_LM/Landmark[" + (i + 1) + "]/UnderlayAddress/" + addrType));
            } catch (HyperCastConfigException e) {
                throw new HyperCastFatalRuntimeException("could not read the physical address of landmark " + i);
            }
            if (null == s) {
                throw new HyperCastFatalRuntimeException("could not read the physical address of landmark " + i);
            }
            landmarks[i] = adapter.createPhysicalAddress(s);
            if (landmarks[i] == null) {
                throw new HyperCastFatalRuntimeException("Exception when creating the physical address of landmark" + i);
            }
        }
        index = -1;
        for (int i = 0; i < lmNum; i++) {
            if (myPhyAddress.equals(landmarks[i])) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            throw new HyperCastFatalRuntimeException(" this is not a valid landmark hosts");
        }
        if (index == 0) delays = new float[lmNum][lmNum]; else delays = null;
        measurements = new long[lmNum][probeNumber];
        config.log.println("Landmark Starts Successfully at " + landmarks[index].toString());
    }

    /**
     * Probe management:
     * 1. StartProbe sends out the trigger messages
     * 2. Two timers for each landmark,
     *      a. is used for creating the randomness between packets(even one)
     *      b. is used for safeguard the probe, i.e. timeout (old one)
     * 3. A third timer is used to guard the total time used for probe process
     *
     */
    public void startProbe() {
        for (int i = 0; i < landmarks.length; i++) for (int j = 0; j < probeNumber; j++) measurements[i][j] = -1;
        if (index == 0) {
            for (int i = 0; i < landmarks.length; i++) for (int j = 0; j < landmarks.length; j++) delays[i][j] = -1;
        }
        System.out.println("My Landmark ID is : " + index);
        if (0 != index) return;
        GNP_Message initMsg = new GNP_Message(GNP_Message.InitProbe, landmarks[0], null);
        for (int i = 0; i < landmarks.length; i++) {
            adapter.sendUnicastMessage(landmarks[i], initMsg);
        }
    }

    /**
	 * Process received message based on their types.
	 */
    public void messageArrivedFromAdapter(I_Message msg) {
        if (!(msg instanceof GNP_Message)) return;
        GNP_Message gmsg = (GNP_Message) msg;
        GNP_Message replyMsg;
        int hostIndex;
        System.out.println("trigger message received, type = " + gmsg.getType());
        switch(gmsg.getType()) {
            case GNP_Message.InitProbe:
                System.out.println("trigger message received, start the timers");
                completedProbes = new int[landmarks.length];
                startTime = new long[landmarks.length];
                for (int i = 0; i < landmarks.length; i++) completedProbes[i] = 0;
                for (int i = 0; i < landmarks.length; i++) {
                    if (index == i) continue;
                    adapter.setTimer(new Integer(i * 2), (int) (100 + probeInterval * rand.nextDouble()));
                }
                if (index != 0) adapter.setTimer(new Integer(landmarks.length * 2), (probeInterval + maxReplyTime) * probeNumber); else adapter.setTimer(new Integer(landmarks.length * 2), (probeInterval + maxReplyTime) * probeNumber + maxReplyTime);
                break;
            case GNP_Message.Ping:
                System.out.println("ping message received from " + gmsg.getSrcAddress().toString());
                replyMsg = new GNP_Message(GNP_Message.Pong, myPhyAddress, null);
                System.out.println("pong message sends to " + gmsg.getSrcAddress().toString());
                adapter.sendUnicastMessage(gmsg.getSrcAddress(), replyMsg);
                break;
            case GNP_Message.Pong:
                System.out.println("pong message recevied from " + gmsg.getSrcAddress().toString());
                hostIndex = getHostIndex(gmsg.getSrcAddress());
                if (hostIndex == -1) {
                    throw new HyperCastFatalRuntimeException("incorrect pong message received");
                }
                measurements[hostIndex][completedProbes[hostIndex]++] = adapter.getCurrentTime() - startTime[hostIndex];
                adapter.clearTimer(new Integer(hostIndex * 2 + 1));
                if (completedProbes[hostIndex] < probeNumber) {
                    adapter.setTimer(new Integer(hostIndex * 2), (int) (100 + probeInterval * rand.nextDouble()));
                    break;
                }
                if (measurementCompleted()) {
                    if (index != 0) {
                        float[] averageMeasure = new float[landmarks.length];
                        for (int i = 0; i < landmarks.length; i++) {
                            averageMeasure[i] = 0;
                            if (i == index) continue;
                            for (int j = 0; j < probeNumber; j++) averageMeasure[i] += measurements[i][j];
                            averageMeasure[i] /= probeNumber;
                        }
                        replyMsg = new GNP_Message(GNP_Message.EndProbe, myPhyAddress, averageMeasure);
                        System.out.println("send back measurement to landmark0");
                        adapter.sendUnicastMessage(landmarks[0], replyMsg);
                        adapter.clearTimer(new Integer(landmarks.length * 2));
                    } else {
                        System.out.println("wait for measurements from other landmarks");
                        for (int i = 0; i < landmarks.length; i++) {
                            delays[0][i] = 0;
                            if (i == 0) continue;
                            for (int j = 0; j < probeNumber; j++) delays[0][i] += measurements[i][j];
                            delays[0][i] /= probeNumber;
                        }
                        if (replyCompleted()) {
                            adapter.clearTimer(new Integer(landmarks.length * 2));
                            outputCoordinates();
                        }
                    }
                }
                break;
            case GNP_Message.EndProbe:
                System.out.println("measurement received from" + gmsg.getSrcAddress().toString());
                if (index != 0) {
                    throw new HyperCastFatalRuntimeException("EndProbe Message is sent to the wrong landmark");
                }
                hostIndex = getHostIndex(gmsg.getSrcAddress());
                if (-1 == hostIndex) {
                    throw new HyperCastFatalRuntimeException("incorrect message received");
                }
                float[] lmDelays = gmsg.getDelay();
                for (int i = 0; i < landmarks.length; i++) delays[hostIndex][i] = lmDelays[i];
                if (replyCompleted()) {
                    adapter.clearTimer(new Integer(landmarks.length * 2));
                    outputCoordinates();
                }
                break;
            default:
                throw new HyperCastFatalRuntimeException("wrong message type");
        }
    }

    /** Handle timer-related tasks. */
    public void timerExpired(Object Timer_ID) {
        int timerID = ((Integer) Timer_ID).intValue();
        int hostIndex = timerID / 2;
        System.out.println("timer id " + timerID);
        if (timerID == landmarks.length * 2) {
            System.out.println("the final safeguard timer expires");
            for (int i = 0; i < landmarks.length; i++) {
                if (delays[i][0] < 0) System.out.println("measurement not received from landmark " + i);
            }
            System.exit(1);
        }
        startTime[hostIndex] = adapter.getCurrentTime();
        GNP_Message gmsg = new GNP_Message(GNP_Message.Ping, myPhyAddress, null);
        System.out.println("ping message sends to " + landmarks[hostIndex].toString());
        adapter.sendUnicastMessage(landmarks[hostIndex], gmsg);
        adapter.setTimer(new Integer(hostIndex * 2 + 1), maxReplyTime);
    }

    /** Reconstruct GNP message from byte array. */
    public I_Message restoreMessage(byte[] receiveBuffer, int[] validBytesStart, int validBytesEnd) {
        return GNP_Message.restoreMessage(receiveBuffer, validBytesStart, validBytesEnd, adapter);
    }

    /** Return the index of Landmark nodes in the landmark table. */
    private int getHostIndex(I_PhysicalAddress addr) {
        int i;
        for (i = 0; i < landmarks.length; i++) {
            if (addr.equals(landmarks[i])) break;
        }
        if (i == landmarks.length) return -1; else return i;
    }

    /** Check if all measurements are finished. */
    private boolean measurementCompleted() {
        for (int i = 0; i < landmarks.length; i++) {
            if (index == i) continue;
            if (completedProbes[i] < probeNumber) return false;
        }
        return true;
    }

    /** Check if all replies are done. */
    private boolean replyCompleted() {
        for (int i = 0; i < landmarks.length; i++) {
            if (delays[i][0] < 0) return false;
        }
        return true;
    }

    /** Calculate the coordinated using measurement results. */
    private void outputCoordinates() {
        for (int i = 0; i < landmarks.length; i++) for (int j = 0; j < i; j++) {
            delays[i][j] = (delays[i][j] + delays[j][i]) / 2;
            delays[j][i] = delays[i][j];
        }
        GNP_Optimization opt = new GNP_Optimization(delays, 2);
        float[][] coor = opt.getLMCoordinates();
        for (int i = 0; i < landmarks.length; i++) {
            System.out.println(PROPERTY_PROTO_PREFIX + ".USE_LM.Landmark" + i + ".Coordinates = " + coor[i][0] + "," + coor[i][1]);
        }
    }

    /** Joins the overlay.  Do nothing */
    public void joinOverlay() {
    }

    /** Leaves the overlay.  Do nothing. */
    public void leaveOverlay() {
    }

    /** Returns the addresspair of the next hop for a message routed by this node
	 * towards the root.  Return null;
	 */
    public I_AddressPair[] getParent(I_LogicalAddress root) {
        return null;
    }

    /** Returns the node's children's physical/logical address pairs, with respect to
	 * the spanning tree rooted at <code>root</code>. Return null;
	 */
    public I_AddressPair[] getChildren(I_LogicalAddress root) {
        return null;
    }

    /** Returns the node's neighbors' physical/logical address pairs.
	 * Return null;
	 */
    public I_AddressPair[] getAllNeighbors() {
        return null;
    }

    /** Returns this logical and physical addresses of this node.
	 */
    public I_AddressPair getAddressPair() {
        return null;
    }

    /** Creates a logical address object from a byte array. Return null.
	 */
    public I_LogicalAddress createLogicalAddress(byte[] laddr, int offset) {
        return null;
    }

    /** Creates a logical address object from a String. Return null.
   */
    public I_LogicalAddress createLogicalAddress(String laStr) {
        return null;
    }

    /** Sets the logical address to specified one. Do nothing.
   */
    public void setLogicalAddress(I_LogicalAddress la) {
    }

    /** Verify the previous hop of the message. If checkmode is set to "neighborCheck",
   * this method checks if the previous hop is a neighbor; otherwise this method checks
   * if the previous hop is a valid sender of OL messages. It does nothing in DT_server.
   */
    public boolean previousHopCheck(I_LogicalAddress src, I_LogicalAddress dst, I_LogicalAddress prehop) {
        return true;
    }

    /**
   * Set notification handler. Do noting.
   */
    public void setNotificationHandler(NotificationHandler nh) {
    }

    /** 
   * Return the result of query for the statistics specified by the given xpath.
   * 
   * @param doc   Document used for create new elements or nodes.
   * @param xpath XPath instance represents the statistics to be queried.
   * @see I_Stats#getStats
   */
    public Element[] getStats(Document doc, XPath xpath) throws HyperCastStatsException {
        return null;
    }

    /** 
   * Set the statistics specified by the given xpath. The value actually set is
   * returned.
   * 
   * @param doc   	Document used for create new elements or nodes.
   * @param xpath 	XPath instance represents the statistics to be queried.
   * @param newValue	Element representing the value or sub-tree to be set.
   * @see I_Stats#setStats
   */
    public Element[] setStats(Document doc, XPath xpath, Element newValue) throws HyperCastStatsException {
        return null;
    }

    /** 
   * Return the schema element which represents the root of the sub-tree, specified
   * by the given xpath, in read schema tree. 
   * 
   * @param doc   Document used for create new elements or nodes.
   * @param xpath XPath instance representing the statistics which is the root
   * 	of the sub-tree to be queried.
   * @see I_Stats#getReadSchema(Document, XPath)
   */
    public Element[] getReadSchema(Document doc, XPath xpath) throws HyperCastStatsException {
        return null;
    }

    /** 
   * Return the schema element which represents the root of the sub-tree, specified
   * by the given xpath, in write schema tree. 
   * 
   * @param doc   Document used for create new elements or nodes.
   * @param xpath XPath instance representing the statistics which is the root
   * 	of the sub-tree to be queried.
   * @see I_Stats#getWriteSchema(Document, XPath)
   */
    public Element[] getWriteSchema(Document doc, XPath xpath) throws HyperCastStatsException {
        return null;
    }

    /** @see I_Stats#getStatsName() */
    public String getStatsName() {
        return null;
    }

    /** @see I_Stats#setStatsName(String) */
    public void setStatsName(String name) {
    }

    public static void main(String[] args) {
        GNP_Landmark lm;
        if (args.length < 2) {
            throw new HyperCastFatalRuntimeException("USAGE: java -classpath hypercast.jar hypercast.DT.GNP_Landmark" + "            config_file addrportString|port# property_prefix ");
        }
        if (args.length > 2) lm = new GNP_Landmark(args[0], args[1], args[2]); else lm = new GNP_Landmark(args[0], args[1], null);
        lm.startProbe();
        while (true) {
            System.out.println(".");
            try {
                Thread.sleep(1000000);
            } catch (Exception e) {
            }
        }
    }
}
