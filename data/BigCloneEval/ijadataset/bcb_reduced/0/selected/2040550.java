package org.processmining.analysis.performance.dottedchart.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.processmining.analysis.performance.dottedchart.logutil.AbstractLogUnit;
import org.processmining.analysis.performance.dottedchart.logutil.LogUnitList;
import org.processmining.analysis.performance.dottedchart.ui.DottedChartPanel;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;

public class DottedChartModel {

    public static final String STR_NONE = "None";

    public static final String ST_ORIG = "Originator";

    public static final String ST_TASK = "Task ID";

    public static final String ST_INST = "Instance ID";

    public static final String ST_EVEN = "Event";

    public static final String ST_DATA = "Data";

    public static final String STATISTICS_OVERALL = "Overall";

    private String typeHashMap = ST_TASK;

    private HashMap<String, LogUnitList> taskMap = new HashMap<String, LogUnitList>();

    private HashMap<String, LogUnitList> instanceMap = new HashMap<String, LogUnitList>();

    private HashMap<String, LogUnitList> originatorMap = new HashMap<String, LogUnitList>();

    private HashMap<String, LogUnitList> eventMap = new HashMap<String, LogUnitList>();

    private HashMap<String, LogUnitList> dataMap = new HashMap<String, LogUnitList>();

    private HashMap<String, Date> taskDateMap = new HashMap<String, Date>();

    private HashMap<String, Date> instanceDateMap = new HashMap<String, Date>();

    private HashMap<String, Date> originatorDateMap = new HashMap<String, Date>();

    private HashMap<String, Date> eventDateMap = new HashMap<String, Date>();

    private HashMap<String, Date> dataDateMap = new HashMap<String, Date>();

    private HashMap<String, Date> taskEndDateMap = new HashMap<String, Date>();

    private HashMap<String, Date> instanceEndDateMap = new HashMap<String, Date>();

    private HashMap<String, Date> originatorEndDateMap = new HashMap<String, Date>();

    private HashMap<String, Date> eventEndDateMap = new HashMap<String, Date>();

    private HashMap<String, Date> dataEndDateMap = new HashMap<String, Date>();

    private ArrayList<String> sortedKeys;

    protected Date logBoundaryLeft = null;

    protected Date logBoundaryRight = null;

    protected LogReader inputLog;

    protected ArrayList<String> eventTypeToKeep;

    private ArrayList instanceIDs;

    private ArrayList<DescriptiveStatistics> timeStatistics = null;

    private DescriptiveStatistics overallStatistics = null;

    public DottedChartModel(LogReader aInputLog) {
        inputLog = aInputLog;
        construct();
    }

    public DottedChartModel(LogReader aInputLog, ArrayList<String> aEventTypeToKeep, ArrayList anInstanceIDs) {
        eventTypeToKeep = aEventTypeToKeep;
        instanceIDs = anInstanceIDs;
        inputLog = aInputLog;
    }

    public LogReader getLogReader() {
        return inputLog;
    }

    public ArrayList<String> getSortedKeySetList() {
        return sortedKeys;
    }

    public HashMap<String, LogUnitList> getItemMap() {
        if (typeHashMap.equals(ST_INST)) {
            return instanceMap;
        } else if (typeHashMap.equals(ST_ORIG)) {
            return originatorMap;
        } else if (typeHashMap.equals(ST_TASK)) {
            return taskMap;
        } else if (typeHashMap.equals(ST_EVEN)) {
            return eventMap;
        } else if (typeHashMap.equals(ST_DATA)) {
            return dataMap;
        }
        return null;
    }

    public HashMap<String, LogUnitList> getItemMap(String type) {
        if (type.equals(ST_INST)) {
            return instanceMap;
        } else if (type.equals(ST_ORIG)) {
            return originatorMap;
        } else if (type.equals(ST_TASK)) {
            return taskMap;
        } else if (type.equals(ST_EVEN)) {
            return eventMap;
        } else if (type.equals(ST_DATA)) {
            return dataMap;
        }
        return null;
    }

    public void sortKeySet(String sort, boolean desc) {
        HashMap<String, LogUnitList> tempMap = null;
        if (typeHashMap.equals(ST_INST)) {
            tempMap = instanceMap;
        } else if (typeHashMap.equals(ST_ORIG)) {
            tempMap = originatorMap;
        } else if (typeHashMap.equals(ST_TASK)) {
            tempMap = taskMap;
        } else if (typeHashMap.equals(ST_EVEN)) {
            tempMap = eventMap;
        } else if (typeHashMap.equals(ST_DATA)) {
            tempMap = dataMap;
        }
        sortedKeys = new ArrayList(tempMap.keySet());
        quicksort(sortedKeys, tempMap, 0, (sortedKeys.size() - 1), desc, sort);
    }

    private void quicksort(ArrayList<String> key, HashMap<String, LogUnitList> tempMap, int left, int right, boolean desc, String type) {
        if (right <= left) return;
        int i = partition(key, tempMap, left, right, desc, type);
        quicksort(key, tempMap, left, i - 1, desc, type);
        quicksort(key, tempMap, i + 1, right, desc, type);
    }

    private int partition(ArrayList<String> key, HashMap<String, LogUnitList> tempMap, int left, int right, boolean desc, String type) {
        int i = left - 1;
        int j = right;
        while (true) {
            if (!desc) {
                while (less(key, tempMap, (++i), right, type)) ;
                while (less(key, tempMap, right, (--j), type)) if (j == left) break;
            } else {
                while (more(key, tempMap, (++i), right, type)) ;
                while (more(key, tempMap, right, (--j), type)) if (j == left) break;
            }
            if (i >= j) break;
            exch(key, i, j);
        }
        exch(key, i, right);
        return i;
    }

    private boolean less(ArrayList<String> keys, HashMap<String, LogUnitList> tempMap, int i, int j, String type) {
        if (type.equals(DottedChartPanel.ST_NAME)) {
            return (keys.get(i).compareTo(keys.get(j)) < 0);
        } else if (type.equals(DottedChartPanel.ST_SIZE)) {
            return (tempMap.get(keys.get(i)).size(eventTypeToKeep, instanceIDs) < tempMap.get(keys.get(j)).size(eventTypeToKeep, instanceIDs));
        } else if (type.equals(DottedChartPanel.ST_DURATION)) {
            long tempDuration = tempMap.get(keys.get(i)).getRightBoundaryTimestamp(eventTypeToKeep, instanceIDs).getTime() - tempMap.get(keys.get(i)).getLeftBoundaryTimestamp(eventTypeToKeep, instanceIDs).getTime();
            long tempDuration2 = tempMap.get(keys.get(j)).getRightBoundaryTimestamp(eventTypeToKeep, instanceIDs).getTime() - tempMap.get(keys.get(j)).getLeftBoundaryTimestamp(eventTypeToKeep, instanceIDs).getTime();
            return (tempDuration < tempDuration2);
        } else if (type.equals(DottedChartPanel.ST_START_TIME)) {
            Date tempDate1 = tempMap.get(keys.get(i)).getLeftBoundaryTimestamp(eventTypeToKeep, instanceIDs);
            Date tempDate2 = tempMap.get(keys.get(j)).getLeftBoundaryTimestamp(eventTypeToKeep, instanceIDs);
            return (tempDate1.before(tempDate2));
        } else if (type.equals(DottedChartPanel.ST_END_TIME)) {
            Date tempDate1 = tempMap.get(keys.get(i)).getRightBoundaryTimestamp(eventTypeToKeep, instanceIDs);
            Date tempDate2 = tempMap.get(keys.get(j)).getRightBoundaryTimestamp(eventTypeToKeep, instanceIDs);
            return (tempDate1.before(tempDate2));
        }
        return false;
    }

    private boolean more(ArrayList<String> keys, HashMap<String, LogUnitList> tempMap, int i, int j, String type) {
        if (type.equals(DottedChartPanel.ST_NAME)) {
            return (keys.get(i).compareTo(keys.get(j)) > 0);
        } else if (type.equals(DottedChartPanel.ST_SIZE)) {
            return (tempMap.get(keys.get(i)).size(eventTypeToKeep, instanceIDs) > tempMap.get(keys.get(j)).size(eventTypeToKeep, instanceIDs));
        } else if (type.equals(DottedChartPanel.ST_DURATION)) {
            long tempDuration = tempMap.get(keys.get(i)).getRightBoundaryTimestamp(eventTypeToKeep, instanceIDs).getTime() - tempMap.get(keys.get(i)).getLeftBoundaryTimestamp(eventTypeToKeep, instanceIDs).getTime();
            long tempDuration2 = tempMap.get(keys.get(j)).getRightBoundaryTimestamp(eventTypeToKeep, instanceIDs).getTime() - tempMap.get(keys.get(j)).getLeftBoundaryTimestamp(eventTypeToKeep, instanceIDs).getTime();
            return (tempDuration > tempDuration2);
        } else if (type.equals(DottedChartPanel.ST_START_TIME)) {
            Date tempDate1 = tempMap.get(keys.get(i)).getLeftBoundaryTimestamp(eventTypeToKeep, instanceIDs);
            Date tempDate2 = tempMap.get(keys.get(j)).getLeftBoundaryTimestamp(eventTypeToKeep, instanceIDs);
            return (tempDate1.after(tempDate2));
        } else if (type.equals(DottedChartPanel.ST_END_TIME)) {
            Date tempDate1 = tempMap.get(keys.get(i)).getRightBoundaryTimestamp(eventTypeToKeep, instanceIDs);
            Date tempDate2 = tempMap.get(keys.get(j)).getRightBoundaryTimestamp(eventTypeToKeep, instanceIDs);
            return (tempDate1.after(tempDate2));
        }
        return false;
    }

    private void exch(ArrayList<String> key, int i, int j) {
        String swap = key.get(i);
        key.set(i, key.get(j));
        key.set(j, swap);
    }

    public HashMap<String, Date> getStartDateMap(String type) {
        if (type.equals(ST_INST)) {
            return instanceDateMap;
        } else if (type.equals(ST_ORIG)) {
            return originatorDateMap;
        } else if (type.equals(ST_TASK)) {
            return taskDateMap;
        } else if (type.equals(ST_EVEN)) {
            return eventDateMap;
        } else if (type.equals(ST_DATA)) {
            return dataDateMap;
        }
        return null;
    }

    public HashMap<String, Date> getEndDateMap(String type) {
        if (type.equals(ST_INST)) {
            return instanceEndDateMap;
        } else if (type.equals(ST_ORIG)) {
            return originatorEndDateMap;
        } else if (type.equals(ST_TASK)) {
            return taskEndDateMap;
        } else if (type.equals(ST_EVEN)) {
            return eventEndDateMap;
        } else if (type.equals(ST_DATA)) {
            return dataEndDateMap;
        }
        return null;
    }

    public void setTypeHashMap(String typeMap) {
        typeHashMap = typeMap;
    }

    public String getTypeHashMap() {
        return typeHashMap;
    }

    public Date getLogBoundaryLeft() {
        if (logBoundaryLeft != null) return logBoundaryLeft;
        return new Date(0);
    }

    public Date getLogBoundaryRight() {
        if (logBoundaryRight != null) return logBoundaryRight;
        return new Date(100);
    }

    public ArrayList<String> getEventTypeToKeep() {
        return eventTypeToKeep;
    }

    public void setEventTypeToKeep(ArrayList<String> aEventTypeToKeep) {
        this.eventTypeToKeep = aEventTypeToKeep;
    }

    public ArrayList getInstanceTypeToKeep() {
        return instanceIDs;
    }

    public void setInstanceTypeToKeep(ArrayList anInstanceIDs) {
        this.instanceIDs = anInstanceIDs;
    }

    /**
	 * construct dotted chart model
	 */
    public void construct() {
        inputLog.reset();
        LogUnitList.resetIdCounter();
        AbstractLogUnit event = null;
        String[] originators = inputLog.getLogSummary().getOriginators();
        ArrayList originatorList = new ArrayList(Arrays.asList(originators));
        LogUnitList[] logUnitList = new LogUnitList[originators.length];
        for (int i = 0; i < originators.length; i++) {
            logUnitList[i] = new LogUnitList();
        }
        String[] tasks = inputLog.getLogSummary().getModelElements();
        ArrayList taskList = new ArrayList(Arrays.asList(tasks));
        LogUnitList[] logUnitforTaskList = new LogUnitList[tasks.length];
        for (int i = 0; i < tasks.length; i++) {
            logUnitforTaskList[i] = new LogUnitList();
        }
        String[] events = inputLog.getLogSummary().getEventTypes();
        ArrayList eventList = new ArrayList(Arrays.asList(events));
        LogUnitList[] logUnitforEventList = new LogUnitList[events.length];
        for (int i = 0; i < events.length; i++) {
            logUnitforEventList[i] = new LogUnitList();
        }
        Iterator it = inputLog.instanceIterator();
        while (it.hasNext()) {
            ProcessInstance pi = (ProcessInstance) it.next();
            Iterator ates = pi.getAuditTrailEntryList().iterator();
            LogUnitList logUnitListforInstance = new LogUnitList();
            while (ates.hasNext()) {
                AuditTrailEntry ate = (AuditTrailEntry) ates.next();
                if (ate.getTimestamp() == null) continue;
                event = AbstractLogUnit.create(pi, ate);
                logUnitListforInstance.addEvent(event);
                if (taskDateMap.get(event.getElement()) == null || taskDateMap.get(event.getElement()).after(ate.getTimestamp())) {
                    taskDateMap.remove(event.getElement());
                    taskDateMap.put(event.getElement(), ate.getTimestamp());
                }
                if (taskEndDateMap.get(event.getElement()) == null || taskEndDateMap.get(event.getElement()).before(ate.getTimestamp())) {
                    taskEndDateMap.remove(event.getElement());
                    taskEndDateMap.put(event.getElement(), ate.getTimestamp());
                }
                logUnitforTaskList[taskList.indexOf(event.getElement())].addEvent(event);
                if (eventDateMap.get(event.getType()) == null || eventDateMap.get(event.getType()).after(ate.getTimestamp())) {
                    eventDateMap.remove(event.getType());
                    eventDateMap.put(event.getType(), ate.getTimestamp());
                }
                if (eventEndDateMap.get(event.getType()) == null || eventEndDateMap.get(event.getType()).before(ate.getTimestamp())) {
                    eventEndDateMap.remove(event.getType());
                    eventEndDateMap.put(event.getType(), ate.getTimestamp());
                }
                logUnitforEventList[eventList.indexOf(event.getType())].addEvent(event);
                Iterator it2 = ate.getAttributes().keySet().iterator();
                while (it2.hasNext()) {
                    String tempString = (String) it2.next();
                    if (tempString != "") {
                        if (dataMap.containsKey(tempString)) {
                            ((LogUnitList) dataMap.get(tempString)).addEvent(event);
                        } else {
                            LogUnitList tempLogUnitList = new LogUnitList(tempString);
                            tempLogUnitList.addEvent(event);
                            dataMap.put(tempString, tempLogUnitList);
                        }
                    }
                }
                if (originatorList.indexOf(event.getOriginator()) < 0) continue;
                if (originatorDateMap.get(event.getOriginator()) == null || originatorDateMap.get(event.getOriginator()).after(ate.getTimestamp())) {
                    originatorDateMap.remove(event.getOriginator());
                    originatorDateMap.put(event.getOriginator(), ate.getTimestamp());
                }
                if (originatorEndDateMap.get(event.getOriginator()) == null || originatorEndDateMap.get(event.getOriginator()).before(ate.getTimestamp())) {
                    originatorEndDateMap.remove(event.getOriginator());
                    originatorEndDateMap.put(event.getOriginator(), ate.getTimestamp());
                }
                logUnitList[originatorList.indexOf(event.getOriginator())].addEvent(event);
            }
            instanceMap.put(pi.getName(), logUnitListforInstance);
        }
        for (int i = 0; i < tasks.length; i++) {
            taskMap.put(tasks[i], logUnitforTaskList[i]);
        }
        for (int i = 0; i < events.length; i++) {
            eventMap.put(events[i], logUnitforEventList[i]);
        }
        for (int i = 0; i < originators.length; i++) {
            originatorMap.put(originators[i], logUnitList[i]);
        }
        sortedKeys = new ArrayList(taskMap.keySet());
    }

    public void calculateCurrentTimeLogical() {
        ArrayList<AbstractLogUnit> arrayList = new ArrayList<AbstractLogUnit>();
        AbstractLogUnit item;
        String key = null;
        for (Iterator itSets = getItemMap().keySet().iterator(); itSets.hasNext(); ) {
            key = (String) itSets.next();
            if (typeHashMap.equals(ST_INST) && !instanceIDs.contains(key)) continue;
            LogUnitList tempLogUnitList = (LogUnitList) getItemMap().get(key);
            tempLogUnitList.resetPositionOfItems();
            for (Iterator itItm = tempLogUnitList.iterator(); itItm.hasNext(); ) {
                item = (AbstractLogUnit) itItm.next();
                if (eventTypeToKeep != null && (!eventTypeToKeep.contains(item.getType()) || !instanceIDs.contains(item.getProcessInstance().getName()))) continue;
                if (arrayList.size() != 0) {
                    if (arrayList.get(arrayList.size() - 1).getActualTimeStamp().before(item.getActualTimeStamp())) {
                        arrayList.add(item);
                        continue;
                    } else if (!arrayList.get(arrayList.size() - 1).getActualTimeStamp().after(item.getActualTimeStamp())) {
                        arrayList.add(item);
                        continue;
                    }
                    if (arrayList.get(0).getActualTimeStamp().after(item.getActualTimeStamp())) {
                        arrayList.add(0, item);
                        continue;
                    } else if (!arrayList.get(0).getActualTimeStamp().before(item.getActualTimeStamp())) {
                        arrayList.add(0, item);
                        continue;
                    }
                    int x_min = 0;
                    int x_max = arrayList.size();
                    int x_mean;
                    while (true) {
                        x_mean = (x_min + x_max) / 2;
                        if (arrayList.get(x_mean).getActualTimeStamp().before(item.getActualTimeStamp())) {
                            if (x_min == (x_mean + x_max) / 2) {
                                arrayList.add(x_min + 1, item);
                                break;
                            }
                            x_min = x_mean;
                        } else if (arrayList.get(x_mean).getActualTimeStamp().after(item.getActualTimeStamp())) {
                            if (x_min == (x_min + x_mean) / 2) {
                                arrayList.add(x_min + 1, item);
                                break;
                            }
                            x_max = x_mean;
                        } else {
                            arrayList.add(x_mean + 1, item);
                            break;
                        }
                    }
                } else {
                    arrayList.add(item);
                }
            }
        }
        if (arrayList.size() > 0) {
            arrayList.get(0).setPosition(0);
            arrayList.get(0).setCurrentTimeStampLogical();
            for (int i = 1; i < arrayList.size(); i++) {
                AbstractLogUnit abs = (AbstractLogUnit) arrayList.get(i);
                AbstractLogUnit abs0 = (AbstractLogUnit) arrayList.get(i - 1);
                if (!abs0.getActualTimeStamp().before(abs.getActualTimeStamp())) {
                    abs.setPosition(abs0.getPosition());
                } else abs.setPosition(i);
                abs.setCurrentTimeStampLogical();
            }
        }
    }

    public void calculateCurrentTimeLogical_Relative() {
        String key = null;
        for (Iterator itSets = getItemMap().keySet().iterator(); itSets.hasNext(); ) {
            key = (String) itSets.next();
            if (typeHashMap.equals(ST_INST) && !instanceIDs.contains(key)) continue;
            LogUnitList tempLogUnitList = (LogUnitList) getItemMap().get(key);
            tempLogUnitList.resetRelativePositionOfItems();
            ArrayList<AbstractLogUnit> arrayList = new ArrayList<AbstractLogUnit>();
            AbstractLogUnit item;
            for (Iterator itItm = tempLogUnitList.iterator(); itItm.hasNext(); ) {
                item = (AbstractLogUnit) itItm.next();
                if (eventTypeToKeep != null && (!eventTypeToKeep.contains(item.getType()) || !instanceIDs.contains(item.getProcessInstance().getName()))) continue;
                if (arrayList.size() != 0) {
                    if (arrayList.get(arrayList.size() - 1).getActualTimeStamp().before(item.getActualTimeStamp())) {
                        arrayList.add(item);
                        continue;
                    } else if (!arrayList.get(arrayList.size() - 1).getActualTimeStamp().after(item.getActualTimeStamp())) {
                        arrayList.add(item);
                        continue;
                    }
                    if (arrayList.get(0).getActualTimeStamp().after(item.getActualTimeStamp())) {
                        arrayList.add(0, item);
                        continue;
                    } else if (!arrayList.get(0).getActualTimeStamp().before(item.getActualTimeStamp())) {
                        arrayList.add(0, item);
                        continue;
                    }
                    int x_min = 0;
                    int x_max = arrayList.size();
                    int x_mean;
                    while (true) {
                        x_mean = (x_min + x_max) / 2;
                        if (arrayList.get(x_mean).getActualTimeStamp().before(item.getActualTimeStamp())) {
                            if (x_min == (x_mean + x_max) / 2) {
                                arrayList.add(x_min + 1, item);
                                break;
                            }
                            x_min = x_mean;
                        } else if (arrayList.get(x_mean).getActualTimeStamp().after(item.getActualTimeStamp())) {
                            if (x_min == (x_min + x_mean) / 2) {
                                arrayList.add(x_min + 1, item);
                                break;
                            }
                            x_max = x_mean;
                        } else {
                            arrayList.add(x_mean + 1, item);
                            break;
                        }
                    }
                } else {
                    arrayList.add(item);
                }
            }
            if (arrayList.size() > 0) {
                arrayList.get(0).setRelativePosition(0);
                arrayList.get(0).setCurrentTimeStampLogicalRelative();
                for (int i = 1; i < arrayList.size(); i++) {
                    AbstractLogUnit abs = (AbstractLogUnit) arrayList.get(i);
                    AbstractLogUnit abs0 = (AbstractLogUnit) arrayList.get(i - 1);
                    if (!abs0.getActualTimeStamp().before(abs.getActualTimeStamp())) {
                        abs.setRelativePosition(abs0.getRelativePosition());
                    } else abs.setRelativePosition(i);
                    abs.setCurrentTimeStampLogicalRelative();
                }
            }
        }
    }

    public void setLogicalRelativeTime() {
        if (getItemMap().size() <= 0) return;
        String key = null;
        AbstractLogUnit item = null;
        int index = -1;
        for (Iterator itSets = getItemMap().keySet().iterator(); itSets.hasNext(); ) {
            index++;
            key = (String) itSets.next();
            if (typeHashMap.equals(ST_INST) && !instanceIDs.contains(key)) continue;
            LogUnitList tempLogUnit = (LogUnitList) getItemMap().get(key);
            for (Iterator itItm = tempLogUnit.iterator(); itItm.hasNext(); ) {
                item = (AbstractLogUnit) itItm.next();
                if (eventTypeToKeep != null && (!eventTypeToKeep.contains(item.getType()) || !instanceIDs.contains(item.getProcessInstance().getName()))) continue;
                item.setCurrentTimeStampLogicalRelative();
            }
        }
    }

    public void setRelativeTime() {
        if (getItemMap().size() <= 0) return;
        String key = null;
        AbstractLogUnit item = null;
        int index = -1;
        for (Iterator itSets = getItemMap().keySet().iterator(); itSets.hasNext(); ) {
            index++;
            key = (String) itSets.next();
            if (typeHashMap.equals(ST_INST) && !instanceIDs.contains(key)) continue;
            LogUnitList tempLogUnit = (LogUnitList) getItemMap().get(key);
            for (Iterator itItm = tempLogUnit.iterator(); itItm.hasNext(); ) {
                item = (AbstractLogUnit) itItm.next();
                if (eventTypeToKeep != null && (!eventTypeToKeep.contains(item.getType()) || !instanceIDs.contains(item.getProcessInstance().getName()))) continue;
                item.setCurrentTimeStampRelative();
            }
        }
    }

    public void setLogicalTime() {
        if (getItemMap().size() <= 0) return;
        String key = null;
        AbstractLogUnit item = null;
        int index = -1;
        for (Iterator itSets = getItemMap().keySet().iterator(); itSets.hasNext(); ) {
            index++;
            key = (String) itSets.next();
            if (typeHashMap.equals(ST_INST) && !instanceIDs.contains(key)) continue;
            LogUnitList tempLogUnit = (LogUnitList) getItemMap().get(key);
            for (Iterator itItm = tempLogUnit.iterator(); itItm.hasNext(); ) {
                item = (AbstractLogUnit) itItm.next();
                if (eventTypeToKeep != null && (!eventTypeToKeep.contains(item.getType()) || !instanceIDs.contains(item.getProcessInstance().getName()))) continue;
                item.setCurrentTimeStampLogical();
            }
        }
    }

    public void initTimeStatistics() {
        timeStatistics = new ArrayList<DescriptiveStatistics>();
        for (int i = 0; i < getItemMap().size() + 1; i++) {
            DescriptiveStatistics tempDS = DescriptiveStatistics.newInstance();
            timeStatistics.add(tempDS);
        }
        overallStatistics = DescriptiveStatistics.newInstance();
    }

    public ArrayList<DescriptiveStatistics> getTimeStatistics() {
        return timeStatistics;
    }

    public DescriptiveStatistics getOverallStatistics() {
        return overallStatistics;
    }

    public void calculateStatisticsLogical() {
        String key = null;
        AbstractLogUnit item, itemOld;
        DescriptiveStatistics overallDS = timeStatistics.get(0);
        overallDS.clear();
        overallStatistics.clear();
        int index = -1;
        for (Iterator itSets = getItemMap().keySet().iterator(); itSets.hasNext(); ) {
            key = (String) itSets.next();
            index++;
            DescriptiveStatistics tempDS = timeStatistics.get(index + 1);
            tempDS.clear();
            if (typeHashMap.equals(ST_INST) && !instanceIDs.contains(key)) continue;
            LogUnitList tempLogUnitList = (LogUnitList) getItemMap().get(key);
            itemOld = null;
            ArrayList<AbstractLogUnit> abst = tempLogUnitList.getEvents();
            TreeSet treeSet = new TreeSet<AbstractLogUnit>(abst);
            for (Iterator itItm = treeSet.iterator(); itItm.hasNext(); ) {
                item = (AbstractLogUnit) itItm.next();
                if (eventTypeToKeep != null && (!eventTypeToKeep.contains(item.getType()) || !instanceIDs.contains(item.getProcessInstance().getName()))) continue;
                if (itemOld == null) {
                    itemOld = item;
                } else {
                    double temp = (double) item.getCurrentTimeStamp().getTime() - (double) itemOld.getCurrentTimeStamp().getTime();
                    overallStatistics.addValue(temp);
                    tempDS.addValue(temp);
                    itemOld = item;
                }
            }
            if (tempLogUnitList.getRightBoundaryTimestamp(eventTypeToKeep, instanceIDs) == null || tempLogUnitList.getLeftBoundaryTimestamp(eventTypeToKeep, instanceIDs) == null) overallDS.addValue(0); else overallDS.addValue((tempLogUnitList.getRightBoundaryTimestamp(eventTypeToKeep, instanceIDs).getTime() - tempLogUnitList.getLeftBoundaryTimestamp(eventTypeToKeep, instanceIDs).getTime()));
        }
    }

    /**
	 * consolidates the global log viewing area. Iterates through all contained
	 * subsets and compares their boundaries to the current log boundaries set.
	 * If necessary, global log boundaries are adjusted to the outside (i.e.,
	 * only extended)
	 */
    public void adjustLogBoundaries(String timeOption) {
        LogUnitList aLogUnitList = null;
        Date dStart = null;
        Date dEnd = null;
        logBoundaryLeft = null;
        logBoundaryRight = null;
        if (timeOption.equals(DottedChartPanel.TIME_RELATIVE_RATIO)) {
            logBoundaryLeft = new Date(0);
            logBoundaryRight = new Date(10000);
        } else {
            for (Iterator it = getItemMap().values().iterator(); it.hasNext(); ) {
                aLogUnitList = (LogUnitList) it.next();
                dStart = aLogUnitList.getLeftBoundaryTimestamp(eventTypeToKeep, instanceIDs);
                dEnd = aLogUnitList.getRightBoundaryTimestamp(eventTypeToKeep, instanceIDs);
                if (logBoundaryLeft == null || (dStart != null && logBoundaryLeft.after(dStart))) {
                    logBoundaryLeft = dStart;
                }
                if (logBoundaryRight == null || (dEnd != null && logBoundaryRight.before(dEnd))) {
                    logBoundaryRight = dEnd;
                }
            }
        }
    }

    public Date getStartDateofLogUniList(String key) {
        return ((LogUnitList) getItemMap().get(key)).getLeftBoundaryTimestamp(eventTypeToKeep, instanceIDs);
    }

    public Date getEndDateofLogUniList(String key) {
        return ((LogUnitList) getItemMap().get(key)).getRightBoundaryTimestamp(eventTypeToKeep, instanceIDs);
    }

    public int getNumberOfLogUnits(String key) {
        return ((LogUnitList) getItemMap().get(key)).size(eventTypeToKeep, instanceIDs);
    }

    public void calculateStatistics() {
        String key = null;
        AbstractLogUnit item = null;
        AbstractLogUnit itemOld;
        DescriptiveStatistics overallDS = getTimeStatistics().get(0);
        overallDS.clear();
        overallStatistics.clear();
        int index = -1;
        for (Iterator itSets = getItemMap().keySet().iterator(); itSets.hasNext(); ) {
            index++;
            key = (String) itSets.next();
            DescriptiveStatistics tempDS = getTimeStatistics().get(index + 1);
            tempDS.clear();
            if (typeHashMap.equals(ST_INST) && !instanceIDs.contains(key)) continue;
            LogUnitList tempLogUnit = (LogUnitList) getItemMap().get(key);
            itemOld = null;
            ArrayList<AbstractLogUnit> abst = ((LogUnitList) getItemMap().get(key)).getEvents();
            TreeSet treeSet = new TreeSet<AbstractLogUnit>(abst);
            int k = 0;
            for (Iterator itItm = treeSet.iterator(); itItm.hasNext(); ) {
                k++;
                item = (AbstractLogUnit) itItm.next();
                if (eventTypeToKeep != null && (!eventTypeToKeep.contains(item.getType()) || !instanceIDs.contains(item.getProcessInstance().getName()))) continue;
                if (itemOld == null) {
                    itemOld = item;
                } else {
                    double temp = (double) item.getCurrentTimeStamp().getTime() - (double) itemOld.getCurrentTimeStamp().getTime();
                    tempDS.addValue(temp);
                    overallStatistics.addValue(temp);
                    itemOld = item;
                }
            }
            for (int j = k; j < abst.size(); j++) tempDS.addValue(0);
            if (tempLogUnit.getRightBoundaryTimestamp(eventTypeToKeep, instanceIDs) == null || tempLogUnit.getLeftBoundaryTimestamp(eventTypeToKeep, instanceIDs) == null) overallDS.addValue(0); else overallDS.addValue((tempLogUnit.getRightBoundaryTimestamp(eventTypeToKeep, instanceIDs).getTime() - tempLogUnit.getLeftBoundaryTimestamp(eventTypeToKeep, instanceIDs).getTime()));
        }
    }

    public ArrayList<String> getDescriptiveStatisticsTitles() {
        ArrayList<String> st = new ArrayList<String>();
        st.add(0, STATISTICS_OVERALL);
        for (Iterator it = getItemMap().keySet().iterator(); it.hasNext(); ) st.add((String) it.next());
        return st;
    }
}
