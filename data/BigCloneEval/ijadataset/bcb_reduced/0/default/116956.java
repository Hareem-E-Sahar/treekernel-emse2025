import java.io.*;
import java.net.*;
import java.util.*;

/**holds the current host set, finds more hosts if none can connect.
  */
class HostCatcher extends Thread {

    private HostLibrary hostLibrary;

    private boolean needsMoreHosts = false;

    private int connectionIndex;

    private int catcherIndex;

    private GnubertProperties propertyList;

    private GnubertStatus currentStatus;

    /**constructor initializes the hostSet, and loads hosts from the hostfile
    */
    public HostCatcher(GnubertProperties _propertyList, GnubertStatus _currentStatus) {
        propertyList = _propertyList;
        currentStatus = _currentStatus;
        hostLibrary = new HostLibrary(propertyList);
        List hostList = readHostFile();
        if (hostList != null) {
            hostLibrary.addHosts(hostList);
        }
    }

    /**returns a set of Hosts containing all Hosts, both checked in and out
    */
    public ArraySet getHosts() {
        ArraySet hostSet;
        hostSet = hostLibrary.getHostSet();
        return hostSet;
    }

    /**checks to see if more hosts are needed, if so gets them.  Sleeps a while, checks again. 
    *sleeps longer if none needed.  Also deletes hosts to keep list at or below maximum size.
    */
    public void run() {
        Host host;
        ArraySet incomingHosts = null;
        int sleepTime = 1000;
        final int waitForConnectionsTime = 30000;
        while (true) {
            if (needsMoreHosts) {
                while (incomingHosts == null) {
                    host = hostLibrary.sneakHost();
                    incomingHosts = getHostsFrom(hostLibrary.sneakHost());
                    if (incomingHosts == null) {
                        try {
                            sleep(10000);
                        } catch (InterruptedException e) {
                        }
                    }
                }
                hostLibrary.addHosts(incomingHosts);
                needsMoreHosts = false;
                saveHostFile();
                while (propertyList.getNumberClientConnections() > currentStatus.getNumberHostsConnectedTo()) {
                    try {
                        if (needsMoreHosts) break;
                        sleep(waitForConnectionsTime);
                    } catch (java.lang.InterruptedException e) {
                    }
                }
                hostLibrary.trimHosts();
                saveHostFile();
                sleepTime -= 50;
                if (sleepTime < 0) sleepTime = 0;
            } else {
                sleepTime += 50;
                if (sleepTime > 30000) sleepTime = 30000;
            }
            try {
                sleep(sleepTime);
            } catch (InterruptedException e) {
            }
        }
    }

    /**connects to a host and gets it's host set.
   */
    protected ArraySet getHostsFrom(Host host) {
        ArraySet incomingHosts = new ArraySet();
        String response = null;
        try {
            Socket clientSocket = new Socket(host.getName(), host.getPort());
            BufferedReader inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            DataOutputStream outStream = new DataOutputStream(clientSocket.getOutputStream());
            outStream.writeBytes("get hosts\n");
            response = inStream.readLine();
            if (response.equals("Host List")) {
                response = inStream.readLine();
            } else {
                incomingHosts = null;
            }
            clientSocket.close();
        } catch (IOException e) {
            incomingHosts = null;
        }
        if (response != null) {
            response = response.substring(1, (response.length() - 1));
            StringTokenizer tokenizer = new StringTokenizer(response, ", ");
            Host incomingHost;
            while (tokenizer.hasMoreTokens()) {
                incomingHost = new Host(tokenizer.nextToken());
                incomingHosts.add(incomingHost);
            }
        } else {
            incomingHosts = null;
        }
        return incomingHosts;
    }

    /**Checks out a host from the hostLibrary and returns it
   */
    public Host checkOutHost() {
        Host checkMeOut;
        checkMeOut = hostLibrary.checkOutHost();
        if (checkMeOut == null) {
            needsMoreHosts = true;
        }
        return checkMeOut;
    }

    /**Check in a host which could not be connected to on the last try
    */
    public void checkInBadHost(Host badHost) {
        hostLibrary.checkInBadHost(badHost);
    }

    /**Check in a host which was reachable at the last attempt
    */
    public void checkInGoodHost(Host goodHost) {
        hostLibrary.checkInGoodHost(goodHost);
    }

    /**Add a host to the hostCatcher
   */
    public void addHost(Host newHost) {
        hostLibrary.addHost(newHost);
        saveHostFile();
    }

    /**Return a string representing the current host set
    */
    public String toString() {
        String returnString;
        returnString = hostLibrary.toString();
        return returnString;
    }

    /** read the hostfile into a list of Host objects.
    */
    private List readHostFile() {
        List hostList = null;
        try {
            File hostFile = new File(propertyList.getHostFileName());
            FileInputStream inStream = new FileInputStream(hostFile);
            ObjectInputStream objectInStream = new ObjectInputStream(inStream);
            hostList = (List) objectInStream.readObject();
        } catch (IOException ie) {
        } catch (ClassNotFoundException ce) {
        }
        return hostList;
    }

    /** save the current list of hosts to the hostfile.
    */
    private void saveHostFile() {
        try {
            File hostFile = new File(propertyList.getHostFileName());
            FileOutputStream outStream = new FileOutputStream(hostFile);
            ObjectOutputStream objectStream = new ObjectOutputStream(outStream);
            objectStream.writeObject((List) getHosts());
        } catch (IOException ie) {
        }
    }
}
