package jbotrace.base.dataServer;

import java.util.*;

/** <p>The class DataServer provides an easy way to distribute data between
  * different part of a program.</p>
  * <p>Program parts that send data can give a DataType object to a DataSever to discribe the
  * data they will send. They will then get an id and send new data to the DataServer
  * using this id.<br>
  * Program parts that want to receive data have different options. They can request a
  * list of available data and read a read a single information or request automatic
  * updates to receive every xth data or new data every x ms (see functions for details).
  */
public class DataServer implements Runnable {

    /** The list of all data destinations.*/
    List dataDestinationInfos;

    /** The list of all data sources sorted by id. At the moment the list is
	  * sorted by id, because nextId is increased every time a source is added to
	  * the end of the list.*/
    List dataSourceInfos;

    /** The id of the next created source. nextId is only increased. */
    int nextId;

    /** The thread for all timed destination. */
    Thread serverThread;

    /** False lets the thread stop. */
    boolean threadShouldRun;

    /** This is actually a list of list. Each of the lists in the list contains an integer saving
	  * the time between two updates and DestinationInformation that need this updates.
	  * The inner list are sorted by increasing delay times.*/
    List timedDestinations;

    /** The Constructor of DataServer just initializes the two lists and nextId. */
    public DataServer() {
        dataDestinationInfos = new ArrayList();
        dataSourceInfos = new ArrayList();
        nextId = 1;
        timedDestinations = new LinkedList();
    }

    /** Adds a new DataSourceInfo to the end of dataSourceInfos. The new DataSourceInfo is
	  * initialized with the given dataType and the nextId. nextId is increased.
	  * and the id of the new DataSourceInfo returned. */
    public int addDataSource(DataType dataType) {
        int id = nextId;
        nextId++;
        DataSourceInfo newDataSourceInfo = new DataSourceInfo(id, dataType);
        dataSourceInfos.add(newDataSourceInfo);
        return id;
    }

    /** Adds the dataDestination as a destination for data from the
	  * given dataSourceInfo. */
    protected void addDataDestination(DataDestination dataDestination, DataSourceInfo dataSourceInfo, int sendEveryXThUpdate, int sendEveryXMs, boolean sendOnlyWhenUpdated) {
        DataDestinationInfo dataDestinationInfo = new DataDestinationInfo(dataDestination, dataSourceInfo, sendEveryXThUpdate, sendEveryXMs, sendOnlyWhenUpdated);
        dataDestinationInfos.add(dataDestinationInfo);
        dataSourceInfo.addDataDestinationInfo(dataDestinationInfo);
        if (sendEveryXMs != 0) {
            Iterator timedDestinationsIterator = timedDestinations.iterator();
            while (timedDestinationsIterator.hasNext()) {
                List timeList = (List) timedDestinationsIterator.next();
                int delay = ((Integer) timeList.get(0)).intValue();
                if (delay == sendEveryXMs) {
                    timeList.add(dataDestinationInfo);
                    break;
                }
            }
            List timeList = new LinkedList();
            timeList.add(new Integer(sendEveryXMs));
            timeList.add(dataDestinationInfo);
            timedDestinations.add(timeList);
            startThread();
        }
    }

    /** Adds the dataDestination as a destination for a source providing the given
	  * dataType. */
    public void addDataDestination(DataDestination dataDestination, DataType dataType, int sendEveryXThUpdate, int sendEveryXMs, boolean sendOnlyWhenUpdated) {
        try {
            int sourcePosition = searchSourcePosition(dataType);
            DataSourceInfo dataSourceInfo = (DataSourceInfo) dataSourceInfos.get(sourcePosition);
            addDataDestination(dataDestination, dataSourceInfo, sendEveryXThUpdate, sendEveryXMs, sendOnlyWhenUpdated);
        } catch (Exception e) {
        }
    }

    /** Adds the dataDestination as a destination for a source providing a DataType with the
	  * the given name. */
    public void addDataDestination(DataDestination dataDestination, String fullName, int sendEveryXThUpdate, int sendEveryXMs, boolean sendOnlyWhenUpdated) {
        try {
            int sourcePosition = searchSourcePosition(fullName);
            DataSourceInfo dataSourceInfo = (DataSourceInfo) dataSourceInfos.get(sourcePosition);
            addDataDestination(dataDestination, dataSourceInfo, sendEveryXThUpdate, sendEveryXMs, sendOnlyWhenUpdated);
        } catch (Exception e) {
        }
    }

    /** Is called by the DataSources to send new information and calls the
	  * update function of the corresponding DataSourceInfo. */
    public void dataUpdate(int id, Object value) {
        int sourcePosition;
        try {
            sourcePosition = searchSourcePosition(id);
        } catch (Exception e) {
            return;
        }
        ((DataSourceInfo) dataSourceInfos.get(sourcePosition)).update(value);
    }

    /** Returns a list of all available DataTypes */
    public List getDataTypeList() {
        List dataTypes = new LinkedList();
        Iterator dataSourceInfoIterator = dataSourceInfos.iterator();
        while (dataSourceInfoIterator.hasNext()) {
            DataSourceInfo dataSourceInfo = (DataSourceInfo) dataSourceInfoIterator.next();
            dataTypes.add(dataSourceInfo.getDataType());
        }
        return dataTypes;
    }

    /** Returns a single date. */
    public Object getDate(DataType dataType) {
        try {
            int sourcePosition = searchSourcePosition(dataType);
            return ((DataSourceInfo) dataSourceInfos.get(sourcePosition)).getValue();
        } catch (Exception e) {
        }
        return null;
    }

    /** Returns a single date. */
    public Object getDate(String fullname) {
        try {
            int sourcePosition = searchSourcePosition(fullname);
            return ((DataSourceInfo) dataSourceInfos.get(sourcePosition)).getValue();
        } catch (Exception e) {
        }
        return null;
    }

    public void removeDataType(int id) {
        int sourcePosition;
        try {
            sourcePosition = searchSourcePosition(id);
        } catch (Exception e) {
            return;
        }
        dataSourceInfos.remove(sourcePosition);
        System.out.println("Buggy function: DataServer:removeDataType(int id)!");
    }

    /** The implementation of Runnable. This Thread is used to send updates
	  * to the destinations that want updates in regular intervals. */
    public void run() {
        while (threadShouldRun) {
            long minDelay = Long.MAX_VALUE;
            List nextDestinations = null;
            long currentTimeMillis = System.currentTimeMillis();
            Iterator timedDestinationsIterator = timedDestinations.iterator();
            while (timedDestinationsIterator.hasNext()) {
                List timeList = (List) timedDestinationsIterator.next();
                int modulo = ((Integer) timeList.get(0)).intValue();
                if (currentTimeMillis % modulo < minDelay) {
                    minDelay = currentTimeMillis % modulo;
                    nextDestinations = timeList;
                }
            }
            try {
                Thread.sleep(minDelay);
            } catch (Exception e) {
            }
            Iterator nextDestinationsIterator = nextDestinations.iterator();
            nextDestinationsIterator.next();
            while (nextDestinationsIterator.hasNext()) {
                DataDestinationInfo dataDestinationInfo = (DataDestinationInfo) nextDestinationsIterator.next();
                dataDestinationInfo.sendUpdate();
            }
        }
    }

    /** Searches for the DataSourceInfo with the given id and returns its position
	  * in the dataSourceInfos list. Binary search is used. */
    protected int searchSourcePosition(int id) throws Exception {
        int left = 0;
        int right = dataSourceInfos.size() - 1;
        if (dataSourceInfos.size() == 0) throw new Exception();
        while (left != right) {
            int middle = (left + right) / 2;
            DataSourceInfo dataSourceInfo = (DataSourceInfo) dataSourceInfos.get(middle);
            if (dataSourceInfo.getId() > id) {
                right = middle - 1;
            } else if (dataSourceInfo.getId() < id) {
                left = middle + 1;
            } else {
                return middle;
            }
        }
        DataSourceInfo dataSourceInfo = (DataSourceInfo) dataSourceInfos.get(left);
        if (dataSourceInfo.getId() == id) return left;
        throw new Exception();
    }

    /** Iterates through the dataSourceInfos list to find a source providing the
	  * requested dataType and returns the sources position in the dataSourceInfos list. */
    protected int searchSourcePosition(DataType dataType) throws Exception {
        int position = 0;
        Iterator dataSourceInfosIterator = dataSourceInfos.iterator();
        while (dataSourceInfosIterator.hasNext()) {
            DataSourceInfo dataSourceInfo = (DataSourceInfo) dataSourceInfosIterator.next();
            if (dataSourceInfo.getDataType() == dataType) return position;
            position++;
        }
        throw new Exception();
    }

    /** Iterates through the dataSourceInfos list to find a source providing the
	  * dataType with the requested name and returns the sources position in the
	  * dataSourceInfos list. */
    protected int searchSourcePosition(String fullName) throws Exception {
        int position = 0;
        Iterator dataSourceInfosIterator = dataSourceInfos.iterator();
        while (dataSourceInfosIterator.hasNext()) {
            DataSourceInfo dataSourceInfo = (DataSourceInfo) dataSourceInfosIterator.next();
            if (dataSourceInfo.getDataType().getFullName().equals(fullName)) return position;
            position++;
        }
        throw new Exception();
    }

    /** Start race thread. */
    protected void startThread() {
        if (threadShouldRun == false) {
            threadShouldRun = true;
            Thread thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }
    }

    /** Stop race thread. */
    protected void stopThread() {
        threadShouldRun = false;
    }
}
