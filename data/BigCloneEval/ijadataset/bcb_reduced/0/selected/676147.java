package it.uniroma1.dis.omega.upnpqos.utils;

import it.uniroma1.dis.omega.upnpqos.argument.TrafficDescriptor;
import it.uniroma1.dis.omega.upnpqos.structure.PerStreamConfiguration;
import it.uniroma1.dis.omega.upnpqos.structure.v3LinkReachableMacsExtension;
import it.uniroma1.dis.omega.upnpqos.structure.v3RotameterObservation;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;

public class NetworkMonitor {

    /**
	 * Static variable containing the Observations performing (also not active) in
	 * a moment on this Device.
	 */
    private static RotameterObservationThreadManager ActiveRotameterObservationThreadManager = null;

    /**
	 * Static variable containing, for each QosSegmentId, a list of the last performed v3RotameterObservations 
	 * (ordered from the most recent to the oldest).
	 */
    public static Hashtable<String, List<v3RotameterObservation>> RotameterObservationTable;

    static {
        RotameterObservationTable = new Hashtable<String, List<v3RotameterObservation>>();
    }

    /**
	 * This is the Hashtable containing the mapping between the device network interfaces and the UPnP 
	 * Qos Segment Id where they are attached to...
	 * keys = Qos Segment Id
	 * value = PcapIf interface
	 */
    private static Hashtable<String, List<PcapIf>> QosSegmentTable = new Hashtable<String, List<PcapIf>>();

    /**
	 * The following table contains the mapping between
	 * the UpnP-QoS TrafficImportanceNumber and the
	 * DSCP tags (consult Table 2.1-1 if QosDevice:3 Addendum)
	 */
    public static Hashtable<Integer, Integer> dscpTinMappingTable = new Hashtable<Integer, Integer>();

    static {
        dscpTinMappingTable.put(new Integer(0), new Integer(0));
        dscpTinMappingTable.put(new Integer(32), new Integer(1));
        dscpTinMappingTable.put(new Integer(64), new Integer(2));
        dscpTinMappingTable.put(new Integer(96), new Integer(3));
        dscpTinMappingTable.put(new Integer(128), new Integer(4));
        dscpTinMappingTable.put(new Integer(160), new Integer(5));
        dscpTinMappingTable.put(new Integer(192), new Integer(6));
        dscpTinMappingTable.put(new Integer(224), new Integer(7));
    }

    private static List<List<PerStreamConfiguration>> PerStreamConfigurationGroups = new LinkedList<List<PerStreamConfiguration>>();

    /**
	 * This function associates to a PcapIf to a QosSegmentId
	 * 
	 * @param Interface the PcapIf associated to the QosSegmentId
	 * @param QosSegmentId the QosSegmentId to be associated to the PcapIf
	 * 
	 */
    public static void SetInterfaceToSegmentId(PcapIf Interface, String qosSegmentId) {
        if (Interface == null || qosSegmentId == null) return;
        List<PcapIf> IfList = null;
        if (QosSegmentTable.containsKey(qosSegmentId)) {
            IfList = QosSegmentTable.get(qosSegmentId);
            if (IfList != null) {
                if (IfList.contains(Interface)) return;
            } else {
                IfList = new LinkedList<PcapIf>();
            }
            IfList.add(Interface);
        } else {
            IfList = new LinkedList<PcapIf>();
            IfList.add(Interface);
            QosSegmentTable.put(qosSegmentId, IfList);
        }
    }

    /**
	 * This function instantiates and starts a RotameterObservationThreadManager, which deals of the management 
	 * of Rotameter Observation, which has ROPeriod seconds duration, 
	 * with a frequency determined by MonitorResolutionPeriod, considering a RotameterObservation 
	 * address ROAddr, for the interface with segment identifier QosSegmentId.
	 * 
	 * @param MonitorResolutionPeriod
	 * @param ROPeriod
	 * @param PerStreamConfigurationList
	 * @param TDlist List of TrafficDescriptor associated with the list of PerStreamConfiguration.
	 */
    public static void StartRotameterObservations(int MonitorResolutionPeriod, int ROPeriod, List<PerStreamConfiguration> PerStreamConfigurationList, List<TrafficDescriptor> TDlist) {
        NetworkMonitor.setSegmentId();
        if (PerStreamConfigurationList == null || PerStreamConfigurationList.isEmpty()) {
            NetworkMonitor.updateCurrentObservations(MonitorResolutionPeriod, ROPeriod);
        } else {
            PerStreamConfigurationGroups = new LinkedList<List<PerStreamConfiguration>>();
            if (ActiveRotameterObservationThreadManager != null) {
                ActiveRotameterObservationThreadManager.interrupt();
                while (ActiveRotameterObservationThreadManager.isAlive()) ;
                List<List<PerStreamConfiguration>> group = ActiveRotameterObservationThreadManager.getPerStreamConfigurationGroups();
                while (PerStreamConfigurationList.size() > 0) {
                    PerStreamConfiguration psc = PerStreamConfigurationList.get(0);
                    String QosSegmentId = psc.QosSegmentId;
                    List<PerStreamConfiguration> observationStreams = NetworkMonitor.getPerStreamConfigurationsOf(QosSegmentId, PerStreamConfigurationList);
                    int x = 0;
                    while (PerStreamConfigurationList.size() > 0) {
                        PerStreamConfiguration tempx = PerStreamConfigurationList.get(x);
                        if (tempx.QosSegmentId.equals(QosSegmentId)) {
                            PerStreamConfigurationList.remove(x);
                        } else x++;
                    }
                    boolean add = false;
                    Iterator<List<PerStreamConfiguration>> iteratorPerStreamConfigurationList = group.iterator();
                    while (iteratorPerStreamConfigurationList.hasNext()) {
                        List<PerStreamConfiguration> streamList = iteratorPerStreamConfigurationList.next();
                        if (QosSegmentId.equals(streamList.get(0).QosSegmentId)) {
                            add = true;
                            Iterator<PerStreamConfiguration> iteratorPerStreamConfiguration = observationStreams.iterator();
                            while (iteratorPerStreamConfiguration.hasNext()) {
                                PerStreamConfiguration stream = iteratorPerStreamConfiguration.next();
                                streamList.add(stream);
                            }
                        }
                        PerStreamConfigurationGroups.add(getPerStreamConfigurationsOf(streamList.get(0).QosSegmentId, streamList));
                    }
                    if (!add) {
                        PerStreamConfigurationGroups.add(observationStreams);
                    }
                }
            } else {
                while (PerStreamConfigurationList.size() > 0) {
                    PerStreamConfiguration psc = PerStreamConfigurationList.get(0);
                    String QosSegmentId = psc.QosSegmentId;
                    List<PerStreamConfiguration> observationStreams = NetworkMonitor.getPerStreamConfigurationsOf(QosSegmentId, PerStreamConfigurationList);
                    int x = 0;
                    while (PerStreamConfigurationList.size() > 0) {
                        PerStreamConfiguration tempx = PerStreamConfigurationList.get(x);
                        if (tempx.QosSegmentId.equals(QosSegmentId)) {
                            PerStreamConfigurationList.remove(x);
                        } else x++;
                    }
                    PerStreamConfigurationGroups.add(observationStreams);
                }
            }
            ActiveRotameterObservationThreadManager = new RotameterObservationThreadManager(PerStreamConfigurationGroups, MonitorResolutionPeriod, ROPeriod, TDlist);
            ActiveRotameterObservationThreadManager.start();
        }
    }

    /**
	 * Get a List of PerStreamConfiguration structures which have the same
	 * QosSegmentId, searching them in the PerStreamConfigurationList
	 * passed as parameter.
	 * 
	 * @param QosSegmentId
	 * @param PerStreamConfigurationList
	 * @return the list of PerStreamConfiguration with have the same QosSegmentId.
	 */
    private static List<PerStreamConfiguration> getPerStreamConfigurationsOf(String QosSegmentId, List<PerStreamConfiguration> PerStreamConfigurationList) {
        List<PerStreamConfiguration> resultList = new ArrayList<PerStreamConfiguration>();
        Iterator<PerStreamConfiguration> iteratorPerStreamConfigurationList = PerStreamConfigurationList.iterator();
        while (iteratorPerStreamConfigurationList.hasNext()) {
            PerStreamConfiguration psc = iteratorPerStreamConfigurationList.next();
            if (psc.QosSegmentId.equals(QosSegmentId)) {
                if (isParameterized(QosSegmentId)) {
                    boolean result = false;
                    Iterator<PerStreamConfiguration> iteratorResultList = resultList.iterator();
                    while (iteratorResultList.hasNext()) {
                        PerStreamConfiguration psctemp = iteratorResultList.next();
                        if (psc.ROAddr.equals(psctemp.ROAddr) && psc.TrafficHandle.equals(psctemp.TrafficHandle) && !(psc.Layer2StreamId == null && psctemp.Layer2StreamId != null) && !(psc.Layer2StreamId != null && psctemp.Layer2StreamId == null) && ((psc.Layer2StreamId == null && psctemp.Layer2StreamId == null) || (psc.Layer2StreamId.equals(psctemp.Layer2StreamId)))) {
                            result = true;
                            break;
                        } else {
                            result = false;
                        }
                    }
                    if (!result) {
                        resultList.add(psc);
                    }
                } else {
                    boolean result = false;
                    Iterator<PerStreamConfiguration> iteratorResultList = resultList.iterator();
                    while (iteratorResultList.hasNext()) {
                        PerStreamConfiguration psctemp = iteratorResultList.next();
                        if (psctemp.ROAddr.equals(psc.ROAddr)) {
                            result = true;
                            break;
                        } else {
                            result = false;
                        }
                    }
                    if (!result) {
                        resultList.add(psc);
                    }
                }
            }
        }
        return resultList;
    }

    /**
	 * Update the current observations with a new MonitorResolutionPeriod 
	 * and a new ROPeriod.
	 * 
	 * @param MonitorResolutionPeriod
	 * @param Period
	 */
    private static void updateCurrentObservations(int MonitorResolutionPeriod, int ROPeriod) {
        RotameterObservationThreadManager rotm = ActiveRotameterObservationThreadManager;
        if (rotm == null) {
        } else {
            rotm.interrupt();
            while (rotm.isAlive()) ;
            RotameterObservationThreadManager newRotm = new RotameterObservationThreadManager(rotm.getPerStreamConfigurationGroups(), MonitorResolutionPeriod, ROPeriod, rotm.getTrafficDescriptorList());
            ActiveRotameterObservationThreadManager = newRotm;
            newRotm.start();
        }
    }

    /**
	 * This function adds in the Hashtable of Rotameter observations
	 * a new RotameterObservation concerning a specific QosSegmentId
	 * 
	 * @param QosSegmentId
	 * @param RotameterObservation
	 */
    public static void addRotameterObservation(String QosSegmentId, v3RotameterObservation RotameterObservation) {
        synchronized (RotameterObservationTable) {
            List<v3RotameterObservation> lro = RotameterObservationTable.get(QosSegmentId);
            if (lro == null) {
                lro = new ArrayList<v3RotameterObservation>();
                RotameterObservationTable.put(QosSegmentId, lro);
            }
            lro.add(0, RotameterObservation);
        }
    }

    /**
	 * Returns a list of LinkReachableMacs containing, for each segment attesting on this device,
	 * the set of the most recent RotameterObservation performed.
	 * 
	 * @param numberOfRotameterObservations
	 * @return
	 */
    public static List<v3LinkReachableMacsExtension> getRotameterInformation(int numberOfRotameterObservations) {
        List<v3LinkReachableMacsExtension> resultList = new ArrayList<v3LinkReachableMacsExtension>();
        synchronized (RotameterObservationTable) {
            if (RotameterObservationTable.isEmpty()) {
                System.out.println("NetworkMonitor:getRotameterInformation -> There are not available RotameterObservation(s) for any segment.");
                return null;
            }
            Enumeration<String> keys = RotameterObservationTable.keys();
            while (keys.hasMoreElements()) {
                String qs = keys.nextElement();
                List<v3RotameterObservation> elem = RotameterObservationTable.get(qs);
                if (elem != null) {
                    if (elem.size() < numberOfRotameterObservations) {
                        System.out.println("NetworkMonitor:getRotameterInformation -> " + numberOfRotameterObservations + " are not available for Segment: " + qs);
                        return null;
                    }
                    v3LinkReachableMacsExtension lrm = new v3LinkReachableMacsExtension();
                    lrm = new v3LinkReachableMacsExtension();
                    lrm.BridgeId = "?";
                    lrm.LinkId = "?";
                    PcapIf pcapif = GetInterfaceFromSegmentId(qs);
                    try {
                        lrm.MacAddress = NetworkUtil.getMacAddressNotFormatted(pcapif.getHardwareAddress());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    lrm.QosSegmentId = qs;
                    resultList.add(lrm);
                    for (int i = 0; i < numberOfRotameterObservations; i++) {
                        lrm.RotameterObservation.add(elem.get(i));
                    }
                }
            }
        }
        return resultList;
    }

    /**
	 * This function returns the interface with the
	 * segment identifier QosSegmentId passed as parameter.
	 * 
	 * We assume that in a Device there is only one interface connected to a single Qos Segment.
	 * So different device interfaces belong to different QoS Segments. 
	 * 
	 * WARNING: If takes effect only if it has previously called the method getinterfacefromsegmentid
	 * 
	 * @param qosSegmentId
	 * @return the PcapIf describing the interface, or null if an interface with
	 *         QosSegmentId identifier is not present in the list.
	 */
    public static PcapIf GetInterfaceFromSegmentId(String qosSegmentId) {
        if (QosSegmentTable.containsKey(qosSegmentId)) {
            List<PcapIf> PcapIfList = QosSegmentTable.get(qosSegmentId);
            if (!PcapIfList.isEmpty()) {
                return PcapIfList.get(0);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
	 * 
	  * Find the QosSegmentIDs of any active interface on the device.
	  *
	 */
    public static void setSegmentId() {
        List<PcapIf> allInterfaces = new ArrayList<PcapIf>();
        StringBuilder errorBuffer = new StringBuilder();
        int r = Pcap.findAllDevs(allInterfaces, errorBuffer);
        if (r == Pcap.NOT_OK || allInterfaces.isEmpty()) {
            System.err.printf("StartRotameterObservations::Can't read list of interfaces, error is %s", errorBuffer.toString());
            return;
        }
        int i = 0;
        for (PcapIf device : allInterfaces) {
            String MACPcapIf = "";
            i++;
            try {
                MACPcapIf = NetworkUtil.getMacAddress(device.getHardwareAddress());
                if (MACPcapIf != null) {
                    try {
                        Enumeration<NetworkInterface> enu = NetworkInterface.getNetworkInterfaces();
                        while (enu.hasMoreElements()) {
                            NetworkInterface ni = enu.nextElement();
                            if (ni.isUp() && !ni.isVirtual() && !ni.isLoopback()) {
                                String MACnetInt = NetworkUtil.getMacAddress(ni.getHardwareAddress());
                                if (MACPcapIf.equals(MACnetInt) && ((System.getProperty("os.name").equals("Linux") && ni.getName().equals(device.getName())) || (System.getProperty("os.name").equals("Windows Vista")) || (System.getProperty("os.name").equals("Windows XP")))) {
                                    String Segmentid = NetworkUtil.getQosSegmentId(ni);
                                    if (Segmentid == null) {
                                    } else {
                                        SetInterfaceToSegmentId(device, Segmentid);
                                    }
                                }
                            }
                        }
                    } catch (SocketException e) {
                        System.out.println("Error in setSegmentId()");
                    }
                }
            } catch (IOException e) {
            }
        }
    }

    private static boolean isParameterized(String QosSegmentID) {
        return false;
    }
}
