package net.jxta.impl.meter;

import net.jxta.document.Advertisement;
import net.jxta.exception.JxtaException;
import net.jxta.id.ID;
import net.jxta.impl.util.TimeUtils;
import net.jxta.meter.MonitorEvent;
import net.jxta.meter.MonitorException;
import net.jxta.meter.MonitorFilter;
import net.jxta.meter.MonitorFilterException;
import net.jxta.meter.MonitorListener;
import net.jxta.meter.MonitorReport;
import net.jxta.meter.MonitorResources;
import net.jxta.meter.PeerMonitorInfo;
import net.jxta.meter.ServiceMetric;
import net.jxta.meter.ServiceMonitor;
import net.jxta.meter.ServiceMonitorFilter;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.ModuleClassID;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.service.Service;
import net.jxta.util.documentSerializable.DocumentSerializableUtilities;
import net.jxta.util.documentSerializable.DocumentSerializationException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

public class MonitorManager implements Service {

    private static final long timeZero = System.currentTimeMillis();

    public static final int NOT_PULSING = -1;

    private static final int NO_PRIOR_REPORT = 0;

    private static long supportedReportRates[] = new long[] { 500, TimeUtils.ASECOND, 5 * TimeUtils.ASECOND, 10 * TimeUtils.ASECOND, 15 * TimeUtils.ASECOND, 30 * TimeUtils.ASECOND, TimeUtils.AMINUTE, 5 * TimeUtils.AMINUTE, 10 * TimeUtils.AMINUTE, 15 * TimeUtils.AMINUTE, 30 * TimeUtils.AMINUTE, TimeUtils.ANHOUR, 3 * TimeUtils.ANHOUR, 6 * TimeUtils.ANHOUR, 12 * TimeUtils.ANHOUR, TimeUtils.ADAY, TimeUtils.AWEEK };

    private int pulsesPerRate[] = new int[supportedReportRates.length];

    private long startTime = System.currentTimeMillis();

    private LinkedList<MonitorListenerInfo> monitorListenerInfos = new LinkedList<MonitorListenerInfo>();

    private Hashtable<ModuleClassID, ServiceMonitorPulseInfo> serviceMonitorPulseInfos = new Hashtable<ModuleClassID, ServiceMonitorPulseInfo>();

    private int filtersPerRate[] = new int[supportedReportRates.length];

    private long previousReportTimes[] = new long[supportedReportRates.length];

    private PeerGroup peerGroup;

    private Thread reportThread;

    private long pulseRate = NOT_PULSING;

    private int pulseRateIndex = NOT_PULSING;

    private int pulseNumber = 0;

    private long nextPulseTime = NO_PRIOR_REPORT;

    private boolean isRunning = true;

    private ModuleClassID[] supportedModuleClassIDs;

    private ModuleImplAdvertisement implAdvertisement;

    private long lastResetTime = System.currentTimeMillis();

    public Advertisement getImplAdvertisement() {
        return implAdvertisement;
    }

    public Service getInterface() {
        return this;
    }

    public void init(PeerGroup peerGroup, ID assignedID, Advertisement implAdvertisement) {
        this.implAdvertisement = (ModuleImplAdvertisement) implAdvertisement;
        this.peerGroup = peerGroup;
        createReportThread();
        for (int i = 0; i < previousReportTimes.length; i++) {
            pulsesPerRate[i] = (int) (supportedReportRates[i] / supportedReportRates[0]);
        }
    }

    public int startApp(java.lang.String[] args) {
        return 0;
    }

    public void stopApp() {
        destroy();
    }

    private class MonitorListenerInfo {

        MonitorListener monitorListener;

        MonitorFilter monitorFilter;

        long reportRate;

        int reportRateIndex;

        boolean sendCumulativeFirst = false;

        boolean wasCumulativeSent = false;

        MonitorListenerInfo(MonitorListener monitorListener, long reportRate, MonitorFilter monitorFilter, boolean cumulativeFirst) {
            this.monitorListener = monitorListener;
            this.monitorFilter = monitorFilter;
            this.reportRate = reportRate;
            this.sendCumulativeFirst = cumulativeFirst;
            this.reportRateIndex = getReportRateIndex(reportRate);
        }
    }

    public static long[] getReportRates() {
        long copy[] = new long[supportedReportRates.length];
        System.arraycopy(supportedReportRates, 0, copy, 0, supportedReportRates.length);
        return copy;
    }

    public boolean isLocalMonitoringAvailable(ModuleClassID moduleClassID) {
        ServiceMonitor serviceMonitor = getServiceMonitor(moduleClassID);
        return (serviceMonitor != null);
    }

    public PeerGroup getPeerGroup() {
        return peerGroup;
    }

    private void setPeerGroup(PeerGroup pg) {
        PeerGroup tmp = peerGroup;
        peerGroup = pg;
        tmp.unref();
        tmp = null;
    }

    public PeerMonitorInfo getPeerMonitorInfo() {
        long[] reportRates = getReportRates();
        ModuleClassID[] moduleClassIDs = getMonitorableServiceTypes();
        long runningTime = System.currentTimeMillis() - lastResetTime;
        return new PeerMonitorInfo(MeterBuildSettings.METERING, moduleClassIDs, reportRates, lastResetTime, runningTime);
    }

    public int getReportRatesCount() {
        return supportedReportRates.length;
    }

    public int getReportRateIndex(long reportRate) {
        for (int i = 0; i < supportedReportRates.length; i++) {
            if (supportedReportRates[i] == reportRate) {
                return i;
            }
        }
        return -1;
    }

    public boolean isSupportedReportRate(long reportRate) {
        return getReportRateIndex(reportRate) >= 0;
    }

    public long getReportRate(int index) {
        return supportedReportRates[index];
    }

    public long getBestReportRate(long desiredReportRate) {
        for (long supportedReportRate : supportedReportRates) {
            if (desiredReportRate <= supportedReportRate) {
                return supportedReportRate;
            }
        }
        return supportedReportRates[supportedReportRates.length - 1];
    }

    public ServiceMonitor getServiceMonitor(ModuleClassID moduleClassID) {
        ServiceMonitorPulseInfo serviceMonitorPulseInfo = serviceMonitorPulseInfos.get(moduleClassID);
        if (serviceMonitorPulseInfo != null) {
            return serviceMonitorPulseInfo.serviceMonitor;
        } else {
            try {
                ModuleImplAdvertisement moduleImplAdvertisement = MonitorResources.getServiceMonitorImplAdvertisement(moduleClassID, implAdvertisement);
                ServiceMonitor serviceMonitor = (ServiceMonitor) peerGroup.loadModule(moduleClassID, moduleImplAdvertisement);
                MonitorResources.registerServiceMonitorModuleImplAdvertisement(moduleImplAdvertisement);
                if (serviceMonitor instanceof ServiceMonitorImpl) {
                    ((ServiceMonitorImpl) serviceMonitor).init(this);
                }
                serviceMonitorPulseInfo = new ServiceMonitorPulseInfo(this, serviceMonitor);
                serviceMonitorPulseInfos.put(moduleClassID, serviceMonitorPulseInfo);
                return serviceMonitor;
            } catch (JxtaException e) {
                throw new RuntimeException("Unable to load Service Monitor: " + moduleClassID + "\n\tException: " + e);
            }
        }
    }

    private void resetPulseRate() {
        int oldPulseRateIndex = pulseRateIndex;
        pulseRateIndex = NOT_PULSING;
        pulseRate = NOT_PULSING;
        for (int i = 0; i < filtersPerRate.length; i++) {
            if (filtersPerRate[i] != 0) {
                pulseRateIndex = i;
                pulseRate = getReportRate(pulseRateIndex);
                break;
            }
        }
        if (oldPulseRateIndex == pulseRateIndex) {
            return;
        }
        long now = System.currentTimeMillis();
        if (oldPulseRateIndex == NOT_PULSING) {
            for (int i = 0; i < filtersPerRate.length; i++) {
                if (filtersPerRate[i] != 0) {
                    previousReportTimes[i] = now;
                } else {
                    previousReportTimes[i] = NO_PRIOR_REPORT;
                }
            }
            pulseNumber = 0;
            nextPulseTime = now + pulseRate;
        } else if (pulseRateIndex == NOT_PULSING) {
        } else if (pulseRateIndex < oldPulseRateIndex) {
            for (int i = pulseRateIndex; i < (oldPulseRateIndex - 1); i++) {
                if (filtersPerRate[i] != 0) {
                    previousReportTimes[i] = now;
                } else {
                    previousReportTimes[i] = NO_PRIOR_REPORT;
                }
            }
            long timeToNextPulse = nextPulseTime - now;
            if (pulseRate < timeToNextPulse) {
                int numPulsesToNow = (int) (timeToNextPulse / pulseRate);
                int numNewToOldPulses = (int) (supportedReportRates[oldPulseRateIndex] / supportedReportRates[pulseRateIndex]);
                pulseNumber += (numNewToOldPulses - numPulsesToNow) * pulsesPerRate[pulseRateIndex];
                timeToNextPulse = now - (numPulsesToNow * pulseRate);
            } else {
                pulseNumber += (pulsesPerRate[oldPulseRateIndex] - pulsesPerRate[pulseRateIndex]);
            }
        } else if (pulseRateIndex > oldPulseRateIndex) {
            int nextPulseNumber = pulseNumber + pulsesPerRate[oldPulseRateIndex];
            pulseNumber = ((nextPulseNumber - 1) / pulsesPerRate[pulseRateIndex]) * pulsesPerRate[pulseRateIndex];
            nextPulseTime += (nextPulseNumber - pulseNumber) * supportedReportRates[0];
            for (int i = 0; i < pulseRateIndex; i++) {
                previousReportTimes[i] = NO_PRIOR_REPORT;
            }
        }
        reportThread.interrupt();
    }

    private MonitorReport getMonitorReport(MonitorFilter monitorFilter, long reportRate, long previousDeltaTime, long beginReportTime) {
        MonitorReport monitorReport = new MonitorReport(previousDeltaTime, beginReportTime, false);
        for (Iterator i = monitorFilter.getModuleClassIDs(); i.hasNext(); ) {
            ModuleClassID moduleClassID = (ModuleClassID) i.next();
            ServiceMonitorFilter serviceMonitorFilter = monitorFilter.getServiceMonitorFilter(moduleClassID);
            ServiceMonitor serviceMonitor = getServiceMonitor(moduleClassID);
            if (serviceMonitorFilter != null) {
                ServiceMetric serviceMetric = serviceMonitor.getServiceMetric(serviceMonitorFilter, previousDeltaTime, beginReportTime, getReportRateIndex(reportRate), reportRate);
                if (serviceMetric != null) {
                    monitorReport.addServiceMetric(serviceMetric);
                }
            }
        }
        return monitorReport;
    }

    public void validateCumulativeMonitorFilter(MonitorFilter monitorFilter) throws MonitorFilterException {
        boolean isAnyServiceFilters = false;
        for (Iterator i = monitorFilter.getServiceMonitorFilters(); i.hasNext(); ) {
            ServiceMonitorFilter serviceMonitorFilter = (ServiceMonitorFilter) i.next();
            ModuleClassID moduleClassID = serviceMonitorFilter.getModuleClassID();
            ServiceMonitor serviceMonitor = getServiceMonitor(moduleClassID);
            if (serviceMonitor == null) {
                throw new MonitorFilterException(MonitorFilterException.SERVICE_NOT_SUPPORTED, moduleClassID);
            }
            serviceMonitor.validateCumulativeServiceMonitorFilter(serviceMonitorFilter);
            isAnyServiceFilters = true;
        }
        if (!isAnyServiceFilters) {
            throw new MonitorFilterException("Empty Monitor Filter");
        }
    }

    public void validateMonitorFilter(MonitorFilter monitorFilter, long reportRate) throws MonitorFilterException {
        if (!isSupportedReportRate(reportRate)) {
            throw new MonitorFilterException(MonitorFilterException.REPORT_RATE_NOT_SUPPORTED, reportRate);
        }
        boolean isAnyServiceFilters = false;
        for (Iterator i = monitorFilter.getServiceMonitorFilters(); i.hasNext(); ) {
            ServiceMonitorFilter serviceMonitorFilter = (ServiceMonitorFilter) i.next();
            ModuleClassID moduleClassID = serviceMonitorFilter.getModuleClassID();
            ServiceMonitor serviceMonitor = getServiceMonitor(moduleClassID);
            if (serviceMonitor == null) {
                throw new MonitorFilterException(MonitorFilterException.SERVICE_NOT_SUPPORTED, moduleClassID);
            }
            serviceMonitor.validateServiceMonitorFilter(serviceMonitorFilter, reportRate);
            isAnyServiceFilters = true;
        }
        if (!isAnyServiceFilters) {
            throw new MonitorFilterException("Empty Monitor Filter");
        }
    }

    public MonitorFilter createSupportedCumulativeMonitorFilter(MonitorFilter monitorFilter) throws MonitorFilterException {
        MonitorFilter newMonitorFilter = new MonitorFilter(monitorFilter.getDescription());
        boolean anythingAdded = false;
        for (Iterator i = monitorFilter.getServiceMonitorFilters(); i.hasNext(); ) {
            ServiceMonitorFilter serviceMonitorFilter = (ServiceMonitorFilter) i.next();
            ModuleClassID moduleClassID = serviceMonitorFilter.getModuleClassID();
            ServiceMonitor serviceMonitor = getServiceMonitor(moduleClassID);
            if (serviceMonitor == null) {
                continue;
            }
            ServiceMonitorFilter newServiceMonitorFilter = serviceMonitor.createSupportedCumulativeServiceMonitorFilter(serviceMonitorFilter);
            if (newServiceMonitorFilter != null) {
                newMonitorFilter.addServiceMonitorFilter(newServiceMonitorFilter);
                anythingAdded = true;
            }
        }
        if (anythingAdded) {
            return newMonitorFilter;
        } else {
            return null;
        }
    }

    public MonitorFilter createSupportedMonitorFilter(MonitorFilter monitorFilter, long reportRate) throws MonitorFilterException {
        MonitorFilter newMonitorFilter = new MonitorFilter(monitorFilter.getDescription());
        boolean anythingAdded = false;
        for (Iterator i = monitorFilter.getServiceMonitorFilters(); i.hasNext(); ) {
            ServiceMonitorFilter serviceMonitorFilter = (ServiceMonitorFilter) i.next();
            ModuleClassID moduleClassID = serviceMonitorFilter.getModuleClassID();
            ServiceMonitor serviceMonitor = getServiceMonitor(moduleClassID);
            if (serviceMonitor == null) {
                continue;
            }
            ServiceMonitorFilter newServiceMonitorFilter = serviceMonitor.createSupportedServiceMonitorFilter(serviceMonitorFilter, reportRate);
            if (newServiceMonitorFilter != null) {
                newMonitorFilter.addServiceMonitorFilter(newServiceMonitorFilter);
                anythingAdded = true;
            }
        }
        if (anythingAdded) {
            return newMonitorFilter;
        } else {
            return null;
        }
    }

    public synchronized long addMonitorListener(MonitorFilter monitorFilter, long reportRate, boolean includeCumulative, MonitorListener monitorListener) throws MonitorException {
        validateMonitorFilter(monitorFilter, reportRate);
        if (includeCumulative) {
            validateCumulativeMonitorFilter(monitorFilter);
        }
        int reportRateIndex = getReportRateIndex(reportRate);
        try {
            monitorFilter = (MonitorFilter) DocumentSerializableUtilities.copyDocumentSerializable(monitorFilter);
        } catch (DocumentSerializationException e) {
            throw new MonitorException(MonitorException.SERIALIZATION, "Error trying to copy MonitorFilter");
        }
        MonitorListenerInfo monitorListenerInfo = new MonitorListenerInfo(monitorListener, reportRate, monitorFilter, includeCumulative);
        monitorListenerInfos.add(monitorListenerInfo);
        filtersPerRate[reportRateIndex]++;
        if ((filtersPerRate[reportRateIndex] == 1) && (pulseRateIndex != NOT_PULSING) && (reportRateIndex > pulseRateIndex)) {
            previousReportTimes[reportRateIndex] = previousReportTimes[pulseRateIndex];
        }
        for (Iterator i = monitorFilter.getModuleClassIDs(); i.hasNext(); ) {
            ModuleClassID moduleClassID = (ModuleClassID) i.next();
            ServiceMonitorFilter serviceMonitorFilter = monitorFilter.getServiceMonitorFilter(moduleClassID);
            ServiceMonitorPulseInfo serviceMonitorPulseInfo = serviceMonitorPulseInfos.get(moduleClassID);
            serviceMonitorPulseInfo.registerServiceMonitorFilter(serviceMonitorFilter, reportRateIndex, reportRate);
        }
        resetPulseRate();
        return reportRate;
    }

    private MonitorListenerInfo getMonitorListenerInfo(MonitorListener monitorListener) {
        for (Object monitorListenerInfo1 : monitorListenerInfos) {
            MonitorListenerInfo monitorListenerInfo = (MonitorListenerInfo) monitorListenerInfo1;
            if (monitorListenerInfo.monitorListener == monitorListener) {
                return monitorListenerInfo;
            }
        }
        return null;
    }

    public synchronized int removeMonitorListener(MonitorListener monitorListener) {
        int numRemoved = 0;
        for (; ; ) {
            MonitorListenerInfo monitorListenerInfo = getMonitorListenerInfo(monitorListener);
            if (monitorListenerInfo == null) {
                break;
            } else {
                MonitorFilter monitorFilter = monitorListenerInfo.monitorFilter;
                long reportRate = monitorListenerInfo.reportRate;
                int reportRateIndex = monitorListenerInfo.reportRateIndex;
                monitorListenerInfos.remove(monitorListenerInfo);
                numRemoved++;
                filtersPerRate[reportRateIndex]--;
                for (Iterator i = monitorFilter.getModuleClassIDs(); i.hasNext(); ) {
                    ModuleClassID moduleClassID = (ModuleClassID) i.next();
                    ServiceMonitorFilter serviceMonitorFilter = monitorFilter.getServiceMonitorFilter(moduleClassID);
                    ServiceMonitorPulseInfo serviceMonitorPulseInfo = serviceMonitorPulseInfos.get(moduleClassID);
                    serviceMonitorPulseInfo.deregisterServiceMonitorFilter(serviceMonitorFilter, reportRateIndex, reportRate);
                }
            }
        }
        resetPulseRate();
        return numRemoved;
    }

    public synchronized MonitorReport getCumulativeMonitorReport(MonitorFilter monitorFilter) throws MonitorException {
        validateCumulativeMonitorFilter(monitorFilter);
        long beginReportTime = System.currentTimeMillis();
        MonitorReport monitorReport = new MonitorReport(startTime, beginReportTime, true);
        for (Iterator i = monitorFilter.getModuleClassIDs(); i.hasNext(); ) {
            ModuleClassID moduleClassID = (ModuleClassID) i.next();
            ServiceMonitorFilter serviceMonitorFilter = monitorFilter.getServiceMonitorFilter(moduleClassID);
            ServiceMonitor serviceMonitor = getServiceMonitor(moduleClassID);
            ServiceMetric serviceMetric = serviceMonitor.getCumulativeServiceMetric(serviceMonitorFilter, timeZero, beginReportTime);
            monitorReport.addServiceMetric(moduleClassID, serviceMetric);
        }
        return monitorReport;
    }

    public ModuleClassID[] getMonitorableServiceTypes() {
        if (supportedModuleClassIDs == null) {
            ModuleClassID[] registeredModuleClassIDs = MonitorResources.getRegisteredModuleClassIDs();
            LinkedList<ModuleClassID> supportedModuleClassIDsList = new LinkedList<ModuleClassID>();
            for (ModuleClassID registeredModuleClassID : registeredModuleClassIDs) {
                if (isLocalMonitoringAvailable(registeredModuleClassID)) {
                    supportedModuleClassIDsList.add(registeredModuleClassID);
                }
            }
            supportedModuleClassIDs = supportedModuleClassIDsList.toArray(new ModuleClassID[0]);
        }
        return supportedModuleClassIDs;
    }

    public long getPulseRate() {
        return getReportRate(pulseRateIndex);
    }

    public int getPulseRateIndex() {
        return pulseRateIndex;
    }

    public long getPulseRate(ServiceMonitor serviceMonitor) {
        ServiceMonitorPulseInfo serviceMonitorPulseInfo = serviceMonitorPulseInfos.get(serviceMonitor.getModuleClassID());
        if (serviceMonitorPulseInfo != null) {
            return serviceMonitorPulseInfo.getPulseRate();
        } else {
            return ServiceMonitorPulseInfo.NOT_PULSING;
        }
    }

    public long getPulseRateIndex(ServiceMonitor serviceMonitor) {
        ServiceMonitorPulseInfo serviceMonitorPulseInfo = serviceMonitorPulseInfos.get(serviceMonitor.getModuleClassID());
        if (serviceMonitorPulseInfo != null) {
            return serviceMonitorPulseInfo.getPulseRateIndex();
        } else {
            return ServiceMonitorPulseInfo.NOT_PULSING;
        }
    }

    private void generateReports() {
        long beginReportTime = System.currentTimeMillis();
        for (Enumeration<ServiceMonitorPulseInfo> e = serviceMonitorPulseInfos.elements(); e.hasMoreElements(); ) {
            ServiceMonitorPulseInfo serviceMonitorPulseInfo = e.nextElement();
            int servicePulseRateIndex = serviceMonitorPulseInfo.getPulseRateIndex();
            if ((serviceMonitorPulseInfo.serviceMonitor instanceof ServiceMonitorImpl) && isEvenPulseForRateIndex(servicePulseRateIndex)) {
                ((ServiceMonitorImpl) serviceMonitorPulseInfo.serviceMonitor).beginPulse(serviceMonitorPulseInfo);
            }
        }
        for (Object monitorListenerInfo1 : monitorListenerInfos) {
            MonitorListenerInfo monitorListenerInfo = (MonitorListenerInfo) monitorListenerInfo1;
            MonitorFilter monitorFilter = monitorListenerInfo.monitorFilter;
            MonitorListener monitorListener = monitorListenerInfo.monitorListener;
            int reportRateIndex = monitorListenerInfo.reportRateIndex;
            long reportRate = monitorListenerInfo.reportRate;
            if (isEvenPulseForRateIndex(reportRateIndex)) {
                MonitorReport monitorReport = null;
                try {
                    if (monitorListenerInfo.sendCumulativeFirst && !monitorListenerInfo.wasCumulativeSent) {
                        monitorReport = getCumulativeMonitorReport(monitorFilter);
                        MonitorEvent monitorEvent = new MonitorEvent(peerGroup.getPeerGroupID(), monitorReport);
                        monitorListener.processMonitorReport(monitorEvent);
                        monitorListenerInfo.wasCumulativeSent = true;
                    } else {
                        monitorReport = getMonitorReport(monitorFilter, reportRate, previousReportTimes[reportRateIndex], beginReportTime);
                        MonitorEvent monitorEvent = new MonitorEvent(peerGroup.getPeerGroupID(), monitorReport);
                        monitorListener.processMonitorReport(monitorEvent);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        for (int rateIndex = 0; rateIndex < supportedReportRates.length; rateIndex++) {
            if (isEvenPulseForRateIndex(rateIndex)) {
                if (filtersPerRate[rateIndex] != 0) {
                    previousReportTimes[rateIndex] = beginReportTime;
                } else {
                    previousReportTimes[rateIndex] = NO_PRIOR_REPORT;
                }
            }
        }
        for (Enumeration<ServiceMonitorPulseInfo> e = serviceMonitorPulseInfos.elements(); e.hasMoreElements(); ) {
            ServiceMonitorPulseInfo serviceMonitorPulseInfo = e.nextElement();
            int servicePulseRateIndex = serviceMonitorPulseInfo.getPulseRateIndex();
            if ((serviceMonitorPulseInfo.serviceMonitor instanceof ServiceMonitorImpl) && isEvenPulseForRateIndex(servicePulseRateIndex)) {
                ((ServiceMonitorImpl) serviceMonitorPulseInfo.serviceMonitor).endPulse(serviceMonitorPulseInfo);
            }
        }
    }

    boolean isEvenPulseForRateIndex(int pulseRateIndex) {
        if (pulseRateIndex < 0 || pulseRateIndex > pulsesPerRate.length) {
            return false;
        }
        return ((pulseNumber % pulsesPerRate[pulseRateIndex]) == 0);
    }

    private void createReportThread() {
        reportThread = new Thread(new Runnable() {

            public void run() {
                mainLoop: while (isRunning) {
                    synchronized (MonitorManager.this) {
                        while (pulseRate == NOT_PULSING) {
                            try {
                                MonitorManager.this.wait();
                            } catch (InterruptedException e) {
                                continue mainLoop;
                            }
                        }
                        while (pulseRate != NOT_PULSING) {
                            if (Thread.interrupted()) {
                                continue mainLoop;
                            }
                            long now = System.currentTimeMillis();
                            try {
                                long waitTime = nextPulseTime - now;
                                if (waitTime > 0) {
                                    MonitorManager.this.wait(nextPulseTime - now);
                                }
                                pulseNumber += pulsesPerRate[pulseRateIndex];
                                generateReports();
                                nextPulseTime += pulseRate;
                            } catch (InterruptedException e) {
                                if (pulseRateIndex == NOT_PULSING) {
                                    continue mainLoop;
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        }, "Meter-Monitor-Report");
        reportThread.setDaemon(true);
        reportThread.start();
    }

    public synchronized void destroy() {
        isRunning = false;
        reportThread.interrupt();
        for (Enumeration<ServiceMonitorPulseInfo> e = serviceMonitorPulseInfos.elements(); e.hasMoreElements(); ) {
            ServiceMonitorPulseInfo serviceMonitorPulseInfo = e.nextElement();
            ServiceMonitor serviceMonitor = serviceMonitorPulseInfo.serviceMonitor;
            serviceMonitor.destroy();
        }
    }

    /**
     * DO NOT USE THIS FIELD: It will be deprecated when MonitorManager becomes a
     * FULL FLEDGED SERVICE
     */
    private static Hashtable<PeerGroupID, MonitorManager> monitorManagers = new Hashtable<PeerGroupID, MonitorManager>();

    /**
     * DO NOT USE THIS METHOD: It will be deprecated when MonitorManager becomes a
     * FULL FLEDGED SERVICE
     */
    public static MonitorManager registerMonitorManager(PeerGroup peerGroup) throws JxtaException {
        PeerGroupID peerGroupID = peerGroup.getPeerGroupID();
        MonitorManager monitorManager = monitorManagers.get(peerGroupID);
        if (monitorManager == null) {
            boolean includeTransports = true;
            ModuleImplAdvertisement moduleImplAdvertisement = MonitorResources.getReferenceAllPurposeMonitorServiceImplAdvertisement(includeTransports);
            monitorManager = (MonitorManager) peerGroup.loadModule(MonitorResources.refMonitorServiceSpecID, moduleImplAdvertisement);
            monitorManagers.put(peerGroupID, monitorManager);
            monitorManager.setPeerGroup(peerGroup);
        }
        return monitorManager;
    }

    /**
     * DO NOT USE THIS METHOD: It will be deprecated when MonitorManager becomes a
     * FULL FLEDGED SERVICE
     */
    public static void unregisterMonitorManager(PeerGroup peerGroup) {
        PeerGroupID peerGroupID = peerGroup.getPeerGroupID();
        monitorManagers.remove(peerGroupID);
    }

    public static ServiceMonitor getServiceMonitor(PeerGroup peerGroup, ModuleClassID serviceClassID) {
        try {
            PeerGroupID peerGroupID = peerGroup.getPeerGroupID();
            MonitorManager monitorManager = monitorManagers.get(peerGroupID);
            return monitorManager.getServiceMonitor(serviceClassID);
        } catch (Exception e) {
            throw new RuntimeException("Unable to find MonitorManager or MonitorService");
        }
    }
}
