import java.io.*;
import java.net.*;
import java.util.*;

public class p2p implements ServerListener, ClientListener, Worker {

    ObjectOutputStream toclient;

    ObjectOutputStream toserver;

    ObjectOutputStream topreviousclient;

    int noofclients;

    String clientipadd, serveripadd;

    String previousclientipadd;

    String Directory;

    int clientport;

    int serverportno = 4011;

    boolean processon = false;

    PProcess proc;

    PProcess proctoclient;

    PProcess procforme;

    Vector processstack;

    Loader ld;

    ServerSocket ss;

    /** Creates a new instance of PeerNew */
    public p2p() {
        String newlist;
        String tempip;
        int tempportno;
        boolean connectedtoserver = false;
        CPoller c;
        SPoller as = new SPoller();
        as.addClientListener(this);
        while (true) {
            try {
                ss = new ServerSocket(serverportno);
                ss.close();
                break;
            } catch (Exception e) {
                serverportno = serverportno + 1;
                e.printStackTrace();
            }
        }
        System.out.println("Server bound to :" + serverportno);
        try {
            ld = new Loader("g:/newproj/tp.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Properties :" + ld.getIPeerIp() + " " + ld.getDirectory() + " " + ld.getClassName());
        IClient a = new IClient();
        String listip = a.getParent(ld.getIPeerIp(), 3000, serverportno);
        System.out.println(listip);
        StringTokenizer st = new StringTokenizer(listip);
        String type = st.nextToken();
        newlist = new String();
        if (type.equalsIgnoreCase("setParent")) {
            newlist = listip.substring(9);
            System.out.println(newlist);
        }
        as.listenForConnection(serverportno);
        StringTokenizer ips = new StringTokenizer(newlist);
        c = new CPoller();
        c.addServerListener(this);
        if (ips.countTokens() == 0) {
            String j = c.connect("localhost", serverportno);
            connectedtoserver = true;
            System.out.println("connected to the local host");
        } else {
            while (ips.hasMoreTokens()) {
                try {
                    tempip = ips.nextToken();
                    tempportno = Integer.parseInt((String) ips.nextElement());
                    c.connect(tempip, tempportno);
                    connectedtoserver = true;
                    System.out.println("connected to " + tempportno);
                    break;
                } catch (Exception e) {
                    tempip = ips.nextToken();
                    tempportno = Integer.parseInt((String) ips.nextToken());
                    e.printStackTrace();
                }
            }
        }
        if (connectedtoserver = false) {
            String j = c.connect("localhost", serverportno);
            System.out.println("connected to the local host by default");
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                String temp = br.readLine();
                System.out.println(temp);
                if (temp.equalsIgnoreCase("execute")) {
                    startJob();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String args[]) {
        p2p a = new p2p();
    }

    /** This method is invoked by an instance of CPoller when the client successfully connects to a server*/
    public void connected(ObjectOutputStream pipetoserver, String Serverip) {
        System.out.println("Client request processed");
        System.out.println("Client request processed: connected to" + Serverip);
        toserver = pipetoserver;
        serveripadd = Serverip;
        this.sendMsgToServer("needParent " + serverportno);
    }

    /** This method is invoked by an instance of CPoller when there is a message from the Server.
         */
    public void messageFromServer(String Message) {
        StringTokenizer st = new StringTokenizer(Message);
        String msgtype, field1, field2;
        msgtype = st.nextToken();
        System.out.println("Message from server:" + serveripadd + " ::" + Message);
        if (msgtype.equalsIgnoreCase("shiftConnectionTo")) {
            System.out.println("Trying to drop connection and reconnect...");
            field1 = st.nextToken();
            field2 = st.nextToken();
            CPoller newconn = new CPoller();
            newconn.addServerListener(this);
            newconn.connect(field1, Integer.parseInt(field2));
        }
        if (msgtype.equalsIgnoreCase("needProcessor")) {
            field1 = st.nextToken();
            field2 = st.nextToken();
            if (proc == null) {
                proc = new PProcess(Message);
                int start = Integer.parseInt(proc.getStart());
                int stop = Integer.parseInt(proc.getStop());
                int sep = (start + stop) / 2;
                procforme = new PProcess(proc.getMsg());
                procforme.setStop(new String() + sep);
                System.out.println("Process for me:" + procforme.getMsg());
                PProcessor comp = new PProcessor(procforme);
                comp.addListener(this);
                comp.startExecution();
                procforme.setRunning(true);
                proctoclient = new PProcess(proc.getMsg());
                proctoclient.setStart(new String() + (sep + 1));
                System.out.println("Process to client:" + proctoclient.getMsg());
                sendMsgToClient(proctoclient.getMsg());
            } else {
                PProcess temp1 = new PProcess(Message);
                PProcessor temp = new PProcessor(temp1);
                temp.addListener(this);
                temp.startExecution();
                System.out.println("The job has been distributed successfully");
                processstack.add(Message);
            }
        }
    }

    /** This method is invoked when the server accepts a connection request from a client.It is responsible for
     including the new client s into the "Circle" */
    public void connectionAccepted(ObjectOutputStream pipetoclient, String clientip) {
        System.out.println("System Accepted req from " + clientip);
        previousclientipadd = clientipadd;
        clientipadd = clientip;
        topreviousclient = toclient;
        noofclients = noofclients + 1;
        toclient = pipetoclient;
        this.sendMsgToClient("your request accepted");
    }

    /** This method is invoked by an instance of PServiceProvider when there is a message from the client.
        The responses for various requests from the client are defined in this method  */
    public void messageFromClient(String Message) {
        System.out.println(Message);
        String msgtype, field1, field2;
        StringTokenizer st = new StringTokenizer(Message);
        msgtype = st.nextToken();
        if (msgtype.equalsIgnoreCase("needParent")) {
            field1 = st.nextToken();
            if (noofclients == 2) {
                this.sendMsgToPreviousClient("shiftconnectionTo " + clientipadd + " " + field1);
                noofclients = 1;
            }
            if (toserver == null) {
                CPoller c = new CPoller();
                c.addServerListener(this);
                c.connect(clientipadd, Integer.parseInt(field1));
            }
        }
        if (msgtype.equalsIgnoreCase("success")) {
            this.sendMsgToServer("success " + serverportno);
        }
    }

    /** This method sends a Msg object to the server.The actual message is embedded in a Msg object */
    public void sendMsgToServer(String msg) {
        try {
            toserver.writeObject(new Msg(msg));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** This method sends a Msg object to the Client.The actual message is embedded in the Msg object */
    public void sendMsgToClient(String msg) {
        try {
            toclient.writeObject(new Msg(msg));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMsgToPreviousClient(String msg) {
        try {
            topreviousclient.writeObject(new Msg(msg));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startJob() {
        int start, stop;
        try {
            proc = new PProcess();
            System.out.println(proc.getClassName());
            proc.setNoOfHops("10");
            proc.setOriginIp(InetAddress.getLocalHost().getHostAddress());
            proc.setProcessId(new String() + serverportno);
            proc.setStart(ld.getStart());
            proc.setStop(ld.getStop());
            proc.setClassName(ld.getClassName());
            proc.setType(ld.getTypeOfInput());
            System.out.println(" The procees for the network" + proc.getMsg());
            processon = true;
            start = Integer.parseInt(proc.getStart());
            stop = Integer.parseInt(proc.getStop());
            int sep = (start + stop) / 2;
            procforme = new PProcess(proc.getMsg());
            procforme.setStop(new String() + sep);
            PProcessor comp = new PProcessor(procforme);
            comp.addListener(this);
            comp.startExecution();
            procforme.setRunning(true);
            proctoclient = new PProcess(proc.getMsg());
            proctoclient.setStart(new String() + (sep + 1));
            System.out.println("Process for client " + proctoclient.getMsg());
            sendMsgToClient(proctoclient.getMsg());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fileDownloaded(String filename) {
    }

    public void fileError(String filename) {
    }

    public void finished() {
        procforme.setRunning(false);
        procforme.setFinished(true);
        System.out.println("Finished the given task");
    }

    public void finshedTillNow(int i) {
    }
}
