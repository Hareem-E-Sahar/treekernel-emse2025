import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.sql.*;
import java.io.*;
import java.util.*;

public class integratorServerNewGeneric extends UnicastRemoteObject implements integratorSNewGeneric {

    double thresholdPercentage = 90;

    int transfer[][];

    integratorServerNewGeneric() throws RemoteException {
        super();
        prop = new Properties();
        try {
            prop.load(new FileInputStream("loadbalancer_generic.properties"));
        } catch (IOException e) {
        }
        noServers = Integer.parseInt(prop.getProperty("noServers"));
        for (int i = 0; i < noServers; i++) {
            Serverurl[i] = prop.getProperty("Server" + i + "url");
            ServerIP[i] = prop.getProperty("Server" + i + "Ip");
        }
        linuxPath = prop.getProperty("linuxPath");
        idle_energy = prop.getProperty("idle_energy");
        utiltopower_factor = prop.getProperty("utiltopower_factor");
    }

    public int sum(int a, int b) throws RemoteException {
        return a + b;
    }

    public int mul(int a, int b) throws RemoteException {
        return a * b;
    }

    String prop1;

    Properties prop;

    String Serverurl[] = new String[100];

    String ServerIP[] = new String[100];

    int noServers;

    String Server1url;

    String Server2url;

    String Server1Ip;

    String Server2Ip;

    String linuxPath;

    String idle_energy;

    String utiltopower_factor;

    public double probeEnergy() throws RemoteException {
        float idle_energy1 = Float.parseFloat(idle_energy);
        double utiltopower_factor1 = Double.parseDouble(utiltopower_factor);
        double cpu_util = 10;
        String energy = "";
        double total_energy = 0;
        try {
            Process P = Runtime.getRuntime().exec(linuxPath + "/you.sh");
            StringBuffer strBuf = new StringBuffer();
            String strLine = "";
            BufferedReader outCommand = new BufferedReader(new InputStreamReader(P.getInputStream()));
            while ((strLine = outCommand.readLine()) != null) {
                energy = strLine;
            }
            P.waitFor();
            cpu_util = Double.parseDouble(energy);
            total_energy = idle_energy1 + utiltopower_factor1 * cpu_util;
        } catch (Exception e) {
            total_energy = -1;
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return total_energy;
    }

    public int processEnergy(Connection c1, int requestId, int loadId, double energy[]) throws RemoteException {
        String sql;
        Statement stmt;
        try {
            for (int i = 0; i < energy.length; i++) {
                sql = "insert into os.ServerEnergy(requestId,serverNo,energy,creationDt,creationTime) 			values (" + requestId + "," + i + "," + energy[i] + ",curdate(),curtime())";
                PreparedStatement prest = c1.prepareStatement(sql);
                int count = prest.executeUpdate();
                System.out.println("processEnergy1:" + count + " " + sql);
            }
        } catch (Exception e) {
        }
        return 901;
    }

    public int loadbalancer(double energy[], double thresholdEnergy[]) throws RemoteException {
        int eligible[] = new int[energy.length];
        thresholdEnergy = new double[energy.length];
        for (int th = 0; th < thresholdEnergy.length; th++) {
            thresholdEnergy[th] = 50;
        }
        double temp;
        int m = 0;
        int i = 0;
        int tempposition = 0;
        int serverPositionAfterChange[] = new int[energy.length];
        for (i = 0; i < energy.length; i++) {
            serverPositionAfterChange[i] = i;
        }
        for (i = 0; i < energy.length; i++) {
            for (int j = i; j < energy.length; j++) {
                if (energy[i] > energy[j]) {
                    temp = energy[j];
                    energy[j] = energy[i];
                    energy[i] = temp;
                    temp = thresholdEnergy[j];
                    thresholdEnergy[j] = thresholdEnergy[i];
                    thresholdEnergy[i] = temp;
                    tempposition = serverPositionAfterChange[i];
                    serverPositionAfterChange[i] = serverPositionAfterChange[j];
                    serverPositionAfterChange[j] = tempposition;
                }
            }
        }
        for (i = 0; i < energy.length; i++) {
            if (energy[i] < ((thresholdPercentage * thresholdEnergy[i]) / 100)) {
                return serverPositionAfterChange[i];
            }
        }
        return -1;
    }

    public int createLoad(Connection c1, int requestId, int loadId, int server) throws RemoteException {
        try {
            String sql = "";
            sql = "insert into os.LoadRequestResponse(requestId,loadId,serverNo,requestcreationDt,requestcreationTime) values (" + requestId + "," + loadId + "," + server + ",curdate(),curtime())";
            PreparedStatement prest = c1.prepareStatement(sql);
            int count = prest.executeUpdate();
        } catch (Exception e) {
            return -1;
        }
        return 0;
    }

    public int closeLoad(Connection c1, int requestId, int loadId, int server) throws RemoteException {
        try {
            String sql = "";
            sql = "update os.LoadRequestResponse set processFlag='Y',responsecreationDt = curdate(),responsecreationTime=curtime() where requestId=" + requestId;
            PreparedStatement prest = c1.prepareStatement(sql);
            int count = prest.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    public int runLoad(int requestId, int loadId, int server) throws RemoteException {
        int ret = 0;
        try {
            Process P;
            System.out.println("LINUX: " + linuxPath + "/Load1 " + requestId);
            System.out.println("java " + linuxPath + "/LoadId " + requestId);
            if (loadId == 1) P = Runtime.getRuntime().exec("cr_run java LoadId " + requestId); else P = Runtime.getRuntime().exec("cr_run java LoadId " + requestId);
            StringBuffer strBuf = new StringBuffer();
            String strLine = "";
            String strLine1 = "";
            BufferedReader outCommand = new BufferedReader(new InputStreamReader(P.getInputStream()));
            while ((strLine = outCommand.readLine()) != null) {
                strLine1 = strLine;
            }
            P.waitFor();
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int[][] loadTransfer(double Energy[], int pId[][], double pIdValues[][], int requestId[][]) throws RemoteException {
        int thresholdlimit = 2;
        int i = 0;
        int j = 0;
        double temp = 0;
        double totalEnergy = 0;
        double tempEnergy[] = Energy;
        int serverPosition[] = new int[Energy.length];
        int eligibility[][] = new int[100][2];
        double averageEnergy = 0;
        int temp1[];
        double temp2[];
        int tempposition = 0;
        int temp3;
        int serverPositionAfterChange[] = new int[Energy.length];
        CommonService x1 = new CommonService();
        Connection c1 = x1.initiateCon();
        for (i = 0; i < Energy.length; i++) {
            serverPositionAfterChange[i] = i;
        }
        for (i = 0; i < Energy.length; i++) {
            for (j = i; j < Energy.length; j++) {
                if (Energy[i] < Energy[j]) {
                    temp = Energy[i];
                    Energy[i] = Energy[j];
                    Energy[j] = temp;
                    temp1 = pId[i];
                    pId[i] = pId[j];
                    pId[j] = temp1;
                    temp1 = requestId[i];
                    requestId[i] = requestId[j];
                    requestId[j] = temp1;
                    tempposition = serverPositionAfterChange[i];
                    serverPositionAfterChange[i] = serverPositionAfterChange[j];
                    serverPositionAfterChange[j] = tempposition;
                    temp2 = pIdValues[i];
                    pIdValues[i] = pIdValues[j];
                    pIdValues[j] = temp2;
                    System.out.println("the value of i " + i + "the value of j" + j);
                }
            }
            totalEnergy = totalEnergy + Energy[i];
        }
        for (i = 0; i < serverPositionAfterChange.length; i++) {
            System.out.println("Server Postion" + serverPositionAfterChange[i]);
        }
        averageEnergy = totalEnergy / Energy.length;
        if (Energy[0] - Energy[Energy.length - 1] < thresholdlimit) {
            System.out.println("no need to transfer");
        }
        for (int k = 0; k < pId.length; k++) {
            for (i = 0; i < pId[k].length; i++) {
                for (j = i; j < pId[k].length; j++) {
                    if (pIdValues[k][i] < pIdValues[k][j]) {
                        temp = pIdValues[k][i];
                        pIdValues[k][i] = pIdValues[k][j];
                        pIdValues[k][j] = temp;
                        temp3 = pId[k][i];
                        pId[k][i] = pId[k][j];
                        pId[k][j] = temp3;
                        temp3 = requestId[k][i];
                        requestId[k][i] = requestId[k][j];
                        requestId[k][j] = temp3;
                    }
                }
            }
        }
        for (i = 0; i < pId.length; i++) {
            for (j = 0; j < pId.length; j++) {
            }
        }
        int eligible[][] = new int[100][4];
        int k = 0;
        int m = 0;
        int pinc = 0;
        for (i = 0; i < Energy.length; i++) {
            pinc = 0;
            for (j = Energy.length - 1; j > i + 1; j--) {
                if (Energy[i] > Energy[j] + thresholdlimit) {
                    int flag = 0;
                    while (pinc < pId[i].length && (Energy[j] + pIdValues[i][pinc] < averageEnergy)) {
                        if (((Energy[i] - pIdValues[i][pinc]) > (Energy[j] + pIdValues[i][pinc]))) {
                            Energy[i] = Energy[i] - pIdValues[i][pinc];
                            Energy[j] = Energy[j] + pIdValues[i][pinc];
                            eligible[k][0] = i;
                            eligible[k][1] = j;
                            eligible[k][2] = pId[i][pinc];
                            eligible[k][3] = requestId[i][pinc];
                            k++;
                            flag++;
                        }
                        pinc++;
                    }
                }
            }
            System.out.println("");
        }
        eligible[k][0] = -1;
        int OrigTransfer[][] = new int[k][4];
        int serverTransfer[][] = new int[k][4];
        int ab = 0;
        k = 0;
        System.out.println("Transfer Eligible" + eligible[0][0]);
        while (eligible[ab][0] >= 0) {
            for (i = 0; i < 4; i++) {
                OrigTransfer[ab][i] = eligible[k][i];
                System.out.println("Transfer" + OrigTransfer[ab][i]);
            }
            ab++;
            k++;
        }
        for (i = 0; i < Energy.length; i++) {
            System.out.println("Energy " + Energy[i]);
        }
        for (k = 0; k < ab; k++) {
            for (i = 0; i < 4; i++) {
                if (i < 2) {
                    serverTransfer[k][i] = serverPositionAfterChange[OrigTransfer[k][i]];
                    System.out.println("TransferOrig " + serverTransfer[k][i]);
                } else {
                    serverTransfer[k][i] = OrigTransfer[k][i];
                    System.out.println("TransferOrig " + serverTransfer[k][i]);
                }
            }
        }
        try {
            String sql = "";
            int count;
            PreparedStatement prest;
            for (j = 0; j < serverTransfer.length; j++) {
                sql = "insert into os.LoadTransfer" + "(requestId,processId,serverFrom,serverTo,creationDt,creationTime) values (" + serverTransfer[j][3] + "," + serverTransfer[j][2] + "," + serverTransfer[j][0] + "," + serverTransfer[j][1] + ",curdate(),curtime())";
                prest = c1.prepareStatement(sql);
                count = prest.executeUpdate();
            }
        } catch (Exception e) {
        }
        return serverTransfer;
    }

    public int startProcess1(int loadId) throws RemoteException {
        System.out.println("OUUT");
        return 0;
    }

    public int startProcess(int loadId, int flag1) throws RemoteException {
        CommonService x1 = new CommonService();
        Connection c1 = x1.initiateCon();
        int requestId = x1.sequence("Request");
        System.out.println("RequestId: " + requestId);
        int i;
        int reqType = 0;
        String sql;
        Statement stmt;
        System.out.println("1" + Server1url);
        System.out.println("2" + Server2url);
        System.out.println("1" + Server1Ip);
        System.out.println("2" + Server2Ip);
        System.out.println("L" + linuxPath);
        try {
            sql = "insert into os.Request(requestId,loadId,creationDt,creationTime) values (" + requestId + "," + loadId + ",curdate(),curtime())";
            PreparedStatement prest = c1.prepareStatement(sql);
            int count = prest.executeUpdate();
            System.out.println("1 After insert into LoadBalancer");
        } catch (Exception e) {
        }
        try {
            integratorSNewGeneric remoteObject;
            NewThread n[] = new NewThread[noServers];
            for (i = 0; i < noServers; i++) {
                String m = "" + i;
                n[i] = new NewThread(m, prop);
            }
            for (i = 0; i < noServers; i++) {
                try {
                    n[i].t.join();
                } catch (InterruptedException e) {
                    System.out.println("Main thread Interrupted");
                }
            }
            for (i = 0; i < noServers; i++) {
                System.out.println("Thread One is alive: " + n[i].t.isAlive());
            }
            double energy[] = new double[noServers];
            for (i = 0; i < noServers; i++) {
                energy[i] = n[i].getval(i);
            }
            i = processEnergy(c1, requestId, loadId, energy);
            System.out.println("2 After insert into processEnergy");
            int serverSelection = 0;
            if (flag1 == 0) serverSelection = loadbalancer(energy, null); else serverSelection = MinEnergyGeneric.divertNewRequest(energy);
            System.out.println("3 After loadbalancer" + i);
            if (serverSelection >= 0) {
                int k = createLoad(c1, requestId, loadId, serverSelection);
                remoteObject = (integratorSNewGeneric) Naming.lookup(Serverurl[serverSelection]);
                k = remoteObject.runLoad(requestId, loadId, serverSelection);
                System.out.println("before closeLoad");
                k = closeLoad(c1, requestId, loadId, serverSelection);
                System.out.println("after closeLoad");
                sql = "update os.Request set processFlg='Y' where requestId=" + requestId;
                PreparedStatement prest = c1.prepareStatement(sql);
                int count = prest.executeUpdate();
            }
            return 0;
        } catch (java.net.MalformedURLException me) {
            System.out.println("Malformed URL: " + me.toString());
        } catch (RemoteException re) {
            System.out.println("Remote exception: " + re.toString());
        } catch (java.rmi.NotBoundException exc) {
            System.out.println("NotBound: " + exc.toString());
        } catch (Exception e) {
        }
        return 0;
    }

    public int ProcessDaemon(int flag1) throws RemoteException {
        CommonService x1 = new CommonService();
        Connection c1 = x1.initiateCon();
        String[][] res;
        int ins;
        Statement st;
        String sql123, sql;
        String url;
        int j;
        double energy[] = new double[noServers];
        integratorSNewGeneric remoteObject;
        try {
            for (int i = 0; i < noServers; i++) {
                remoteObject = (integratorSNewGeneric) Naming.lookup(Serverurl[i]);
                energy[i] = remoteObject.probeEnergy();
            }
            int i;
            i = processEnergy(c1, 0, 0, energy);
            for (j = 0; j < noServers; j++) {
                i = Process2EnergyMain(j);
                System.out.println("IIIII:" + j);
            }
            System.out.println("Step 2");
            int[][] procId;
            double[][] procIdVal;
            int[][] reqId;
            String[][] temp;
            procId = new int[noServers][];
            procIdVal = new double[noServers][];
            reqId = new int[noServers][];
            for (i = 0; i < noServers; i++) {
                sql = "select processId,requestId,energy from os.ProcessEnergy where " + " processFlag ='N' and energy > 0 and serverNo=" + i;
                try {
                    temp = x1.Serlist(sql);
                    procId[i] = new int[temp.length];
                    procIdVal[i] = new double[temp.length];
                    reqId[i] = new int[temp.length];
                    System.out.println("T0:" + temp[0][0] + "T1:" + temp[0][1] + "T2:" + temp[0][2]);
                    for (j = 0; j < temp.length; j++) {
                        procId[i][j] = Integer.parseInt(temp[j][0]);
                        reqId[i][j] = Integer.parseInt(temp[j][1]);
                        procIdVal[i][j] = Double.parseDouble(temp[j][2]);
                        System.out.println("NOSERVER:" + j);
                    }
                } catch (Exception e0) {
                    e0.printStackTrace();
                    System.out.println("No Res");
                }
            }
            if (flag1 == 0) transfer = loadTransfer(energy, procId, procIdVal, reqId); else transfer = MinEnergyGeneric.loadbalanceRequest(energy);
            System.out.println("Step 61" + transfer[0][0] + "len:" + transfer.length);
            int m;
            sql = "";
            int count;
            PreparedStatement prest;
            for (j = 0; j < transfer.length; j++) {
                try {
                    sql = "insert into os.LoadTransfer" + "(requestId,processId,serverFrom,serverTo,creationDt,creationTime) values (" + "(select distinct requestId from os.ProcessEnergy where processId=" + transfer[j][2] + ")" + "," + transfer[j][2] + "," + transfer[j][0] + "," + transfer[j][1] + ",curdate(),curtime())";
                    System.out.println("Transfer:" + sql);
                    prest = c1.prepareStatement(sql);
                    count = prest.executeUpdate();
                    sql = "update os.ProcessEnergy set serverNo=" + transfer[j][1] + " where processId=" + transfer[j][2];
                    prest = c1.prepareStatement(sql);
                    count = prest.executeUpdate();
                } catch (Exception e) {
                }
                System.out.println("Stemp 62 ");
                remoteObject = (integratorSNewGeneric) Naming.lookup(Serverurl[transfer[j][0]]);
                System.out.println("Stemp 63 ");
                System.out.println("transfer: " + j + "0:" + transfer[j][0] + "1:" + transfer[j][1] + "2:" + transfer[j][2]);
                System.out.println("Remote source sev url:" + Serverurl[transfer[j][0]]);
                System.out.println("Loc ftp sev:" + ServerIP[transfer[j][0]]);
                System.out.println("REM ftp sev:" + ServerIP[transfer[j][1]]);
                m = remoteObject.FtpPush(ServerIP[transfer[j][1]], "mohan", "vkmohan123", transfer[j][2]);
                System.out.println("Stemp 64 ");
                remoteObject = (integratorSNewGeneric) Naming.lookup(Serverurl[transfer[j][1]]);
                System.out.println("Stemp 65 ");
                m = remoteObject.chkRestart(transfer[j][2]);
                System.out.println(Serverurl[j]);
                System.out.println("Stemp 66 ");
            }
            try {
                sql123 = "update os.ProcessEnergy set processFlag='Y' " + " where processFlag ='N'";
                st = c1.createStatement();
                ins = st.executeUpdate(sql123);
                st.close();
            } catch (Exception e) {
                return -1;
            }
        } catch (java.net.MalformedURLException me) {
            System.out.println("Malformed URL: " + me.toString());
        } catch (RemoteException re) {
            System.out.println("Remote exception: " + re.toString());
        } catch (java.rmi.NotBoundException exc) {
            System.out.println("NotBound: " + exc.toString());
        }
        return 0;
    }

    public int Process2EnergyMain(int serverId) throws RemoteException {
        String res[][];
        Statement st;
        String sql123;
        int ins;
        double energy;
        CommonService x11 = new CommonService();
        Connection c11 = x11.initiateCon();
        try {
            System.out.println("Step 7");
            String sql = "select processId,requestId from os.LoadRequestResponse where " + " processFlag is NULL and serverNo=" + serverId;
            System.out.println(sql);
            try {
                res = x11.Serlist(sql);
            } catch (Exception e) {
                System.out.println("***************** Step 7 Exception");
                return -1;
            }
            System.out.println("Step 8 " + res.length);
            for (int m = 0; m < res.length; m++) {
                integratorSNewGeneric remoteObject;
                System.out.println("Serverurl ----" + Serverurl[serverId]);
                remoteObject = (integratorSNewGeneric) Naming.lookup(Serverurl[serverId]);
                energy = remoteObject.probeProcessEnergy(Integer.parseInt(res[m][0]));
                System.out.println("Step 9" + energy + "process:" + res[m][0]);
                try {
                    st = c11.createStatement();
                    sql123 = "update os.ProcessEnergy set processFlag='Y' where processFlag = 'N' and " + " requestId=" + res[m][1];
                    ins = st.executeUpdate(sql123);
                    sql123 = "insert into os.ProcessEnergy" + "(serverNo,requestId,processId,energy,processFlag,creationDt,creationTime)" + " values (" + serverId + "," + res[m][1] + "," + res[m][0] + "," + energy + ",'N',curdate(),curtime())";
                    System.out.println("process:" + sql123);
                    ins = st.executeUpdate(sql123);
                    st.close();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    return -1;
                }
            }
            return 0;
        } catch (java.net.MalformedURLException me) {
            System.out.println("Malformed URL: " + me.toString());
        } catch (RemoteException re) {
            System.out.println("Remote exception: " + re.toString());
        } catch (java.rmi.NotBoundException exc) {
            System.out.println("NotBound: " + exc.toString());
        }
        return 0;
    }

    public double probeProcessEnergy(int processId) throws RemoteException {
        float idle_energy1 = Float.parseFloat(idle_energy);
        double utiltopower_factor1 = Double.parseDouble(utiltopower_factor);
        double cpu_util = 10;
        String energy = "";
        double total_energy = 0;
        try {
            Process P = Runtime.getRuntime().exec(linuxPath + "/PidCPU.sh " + processId);
            StringBuffer strBuf = new StringBuffer();
            String strLine = "";
            BufferedReader outCommand = new BufferedReader(new InputStreamReader(P.getInputStream()));
            while ((strLine = outCommand.readLine()) != null) {
                energy = strLine;
            }
            P.waitFor();
            cpu_util = Double.parseDouble(energy);
            total_energy = utiltopower_factor1 * cpu_util;
        } catch (Exception e) {
            total_energy = -1;
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return total_energy;
    }

    public int QueueDaemon(int flag1) throws RemoteException {
        CommonService x1 = new CommonService();
        Connection c1 = x1.initiateCon();
        PreparedStatement prest;
        String[][] res;
        try {
            String sql = "select requestId,loadId from os.Request where " + " processFlg is NULL";
            res = x1.Serlist(sql);
            int ins;
            String sql123;
            Statement st;
            int i;
            int requestId, loadId;
            integratorSNewGeneric remoteObject;
            for (int m1 = 0; m1 < res.length; m1++) {
                requestId = Integer.parseInt(res[m1][0]);
                loadId = Integer.parseInt(res[m1][1]);
                NewThread n[] = new NewThread[noServers];
                for (i = 0; i < noServers; i++) {
                    String m = "" + i;
                    n[i] = new NewThread(m, prop);
                }
                for (i = 0; i < noServers; i++) {
                    try {
                        n[i].t.join();
                    } catch (InterruptedException e) {
                        System.out.println("Main thread Interrupted");
                    }
                }
                for (i = 0; i < noServers; i++) {
                    System.out.println("Thread One is alive: " + n[i].t.isAlive());
                }
                double energy[] = new double[noServers];
                for (i = 0; i < noServers; i++) {
                    energy[i] = n[i].getval(i);
                }
                i = processEnergy(c1, requestId, loadId, energy);
                System.out.println("2 After insert into processEnergy");
                int serverSelection = 0;
                if (flag1 == 0) serverSelection = loadbalancer(energy, null); else serverSelection = MinEnergyGeneric.divertNewRequest(energy);
                System.out.println("3 After loadbalancer" + i);
                if (serverSelection >= 0) {
                    int k = createLoad(c1, requestId, loadId, serverSelection);
                    remoteObject = (integratorSNewGeneric) Naming.lookup(Serverurl[serverSelection]);
                    k = remoteObject.runLoad(requestId, loadId, serverSelection);
                    System.out.println("before closeLoad");
                    k = closeLoad(c1, requestId, loadId, serverSelection);
                    System.out.println("after closeLoad");
                    sql = "update os.Request set processFlg='Y' where requestId=" + requestId;
                    prest = c1.prepareStatement(sql);
                    int count = prest.executeUpdate();
                }
                return 0;
            }
        } catch (java.net.MalformedURLException me) {
            System.out.println("Malformed URL: " + me.toString());
        } catch (RemoteException re) {
            System.out.println("Remote exception: " + re.toString());
        } catch (java.rmi.NotBoundException exc) {
            System.out.println("NotBound: " + exc.toString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public int FtpPush(String remoteHost, String user, String pw, int pid) throws RemoteException {
        try {
            String execParm = linuxPath + "/chk.sh 1 " + pid;
            System.out.println("Stemp 66" + execParm);
            Process P = Runtime.getRuntime().exec(execParm);
            P.waitFor();
            System.out.println("Stemp 67");
            String fle = "context." + pid;
            String fle1 = "" + pid;
            execParm = linuxPath + "/ftpautomated.sh " + remoteHost + " put  " + fle;
            P = Runtime.getRuntime().exec(execParm);
            P.waitFor();
            execParm = linuxPath + "/ftpautomated1.sh " + remoteHost + " put  " + fle1;
            P = Runtime.getRuntime().exec(execParm);
            P.waitFor();
            System.out.println("Stemp 68" + execParm);
            System.out.println("Stemp 69");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public int chkRestart(int pid) throws RemoteException {
        CommonService x1 = new CommonService();
        Connection c1 = x1.initiateCon();
        Statement st;
        try {
            String execParm = linuxPath + "/chk.sh 2 " + linuxPath + "context." + pid;
            System.out.println("******into chkRestart" + execParm);
            Process P = Runtime.getRuntime().exec(execParm);
            P.waitFor();
            String sql = "";
            sql = "update os.LoadRequestResponse set processFlag='Y',responsecreationDt = curdate(),responsecreationTime=curtime() where processFlag is NULL and processId=" + pid;
            PreparedStatement prest = c1.prepareStatement(sql);
            int count = prest.executeUpdate();
            System.out.println("LoadRequestResponse update" + count);
            sql = "update os.ProcessEnergy set processFlag='Y' " + " where processId=" + pid;
            prest = c1.prepareStatement(sql);
            count = prest.executeUpdate();
            System.out.println("ProcessEnergy update" + count);
            System.out.println("******after run chkRestart");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public static void main(String args[]) {
        try {
            System.setSecurityManager(new RMISecurityManager());
            integratorServerNewGeneric Server = new integratorServerNewGeneric();
            Naming.rebind("SAMPLE-SERVER", Server);
            System.out.println("Server waiting.....");
        } catch (java.net.MalformedURLException me) {
            System.out.println("Malformed URL: " + me.toString());
        } catch (RemoteException re) {
            System.out.println("Remote exception: " + re.toString());
        }
    }
}

class NewThread implements Runnable {

    integratorSNewGeneric remoteObject;

    String name;

    int serv;

    Thread t;

    int i;

    double var[] = new double[100];

    String Serverurl[] = new String[100];

    int noServers;

    NewThread(String threadname, Properties prop) {
        System.out.println("\nThe foo property222:" + prop.getProperty("Server1url"));
        name = threadname;
        serv = Integer.parseInt(threadname);
        noServers = Integer.parseInt(prop.getProperty("noServers"));
        for (i = 0; i < noServers; i++) {
            Serverurl[i] = prop.getProperty("Server" + i + "url");
        }
        i = Integer.parseInt(name);
        t = new Thread(this, name);
        t.start();
    }

    public double getval(int i) {
        return var[i];
    }

    public void run() {
        try {
            remoteObject = (integratorSNewGeneric) Naming.lookup(Serverurl[serv]);
            var[serv] = remoteObject.probeEnergy();
            Thread.sleep(1);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
