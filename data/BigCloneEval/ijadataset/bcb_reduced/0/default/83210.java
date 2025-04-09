import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.sql.*;
import java.io.*;
import java.util.*;

public class integratorServerNew extends UnicastRemoteObject implements integratorSNew {

    integratorServerNew() throws RemoteException {
        super();
        prop = new Properties();
        try {
            prop.load(new FileInputStream("loadbalancer.properties"));
        } catch (IOException e) {
        }
        System.out.println("\nThe foo property111: " + prop.getProperty("Server1url"));
        Server1url = prop.getProperty("Server1url");
        Server2url = prop.getProperty("Server2url");
        Server1Ip = prop.getProperty("Server1Ip");
        Server2Ip = prop.getProperty("Server2Ip");
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

    public int processEnergy(Connection c1, int requestId, int loadId, double energy1, double energy2) throws RemoteException {
        String sql;
        Statement stmt;
        try {
            sql = "insert into os.ServerEnergy(requestId,serverNo,energy,creationDt,creationTime) 			values (" + requestId + ",1," + energy1 + ",curdate(),curtime())";
            PreparedStatement prest = c1.prepareStatement(sql);
            int count = prest.executeUpdate();
            System.out.println("processEnergy1:" + count + " " + sql);
            sql = "insert into os.ServerEnergy(requestId,serverNo,energy,creationDt,creationTime) 			values (" + requestId + ",2," + energy2 + ",curdate(),curtime())";
            prest = c1.prepareStatement(sql);
            count = prest.executeUpdate();
            System.out.println("processEnergy2:" + count + " " + sql);
        } catch (Exception e) {
        }
        return 901;
    }

    public int loadBalancer(int requestId, int loadId, double Energy1, double Energy2) throws RemoteException {
        float server1Threshold = 150;
        float server2Threshold = 150;
        float thresholdPercentage = 90;
        boolean valid = false;
        try {
            if (thresholdPercentage <= 100) {
                valid = true;
            }
            if (valid) {
                boolean con1 = Energy1 < server1Threshold * (thresholdPercentage / 100);
                boolean con2 = Energy2 < server2Threshold * (thresholdPercentage / 100);
                boolean con3 = Energy1 < Energy2;
                if (!con1 && !con2) {
                    return 0;
                } else if (!con1) {
                    return 2;
                } else if (!con2) {
                    return 1;
                } else if (con1 && con2) {
                    if (con3) {
                        return 1;
                    } else {
                        return 2;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
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

    public int[][] loadTransfer(double Energy1, double Energy2, int pId1[], int pId2[], double pId1Val[], double pId2Val[], int requestId1[], int requestId2[], int flagNew) throws RemoteException {
        System.out.println("INTO LOADTRANSFER");
        int Transfer[][] = new int[100][2];
        double minLoadServ1 = 0.0f;
        double minLoadServ2 = 0.0f;
        int inc = 0;
        int incTr = 0;
        int lengthpId1 = pId1.length;
        int lengthpId2 = pId2.length;
        int OrigTransfer[][];
        CommonService x1 = new CommonService();
        Connection c1 = x1.initiateCon();
        int pro = 0;
        double totalProcessEnergy1 = 0.0;
        double totalProcessEnergy2 = 0.0;
        boolean pId1yes = true;
        boolean pId2yes = true;
        double ThresholdLimit = 14;
        int serverId = 0;
        System.out.println("pId1 Length " + pId1.length);
        System.out.println("pId2 Length " + pId2.length);
        if (flagNew == 0) {
            if (pId1.length != 0) {
                serverId = 1;
                for (inc = 0; inc < pId1.length; inc++) {
                    Transfer[incTr + 1][0] = pId1[inc];
                    Transfer[incTr + 1][1] = requestId1[inc];
                    incTr++;
                }
            } else if (pId2.length != 0) {
                serverId = 2;
                for (inc = 0; inc < pId2.length; inc++) {
                    Transfer[incTr + 1][0] = pId2[inc];
                    Transfer[incTr + 1][1] = requestId2[inc];
                    incTr++;
                }
            }
            incTr = incTr - 1;
            Transfer[0][0] = serverId;
        } else {
            if (pId1.length == 0) {
                pId1yes = false;
            }
            if (pId2.length == 0) {
                pId2yes = false;
            }
            for (pro = 0; pro < pId1Val.length; pro++) {
                totalProcessEnergy1 = pId1Val[pro] + totalProcessEnergy1;
            }
            pro = 0;
            for (pro = 0; pro < pId2Val.length; pro++) {
                totalProcessEnergy2 = pId2Val[pro] + totalProcessEnergy2;
            }
            try {
                for (int m = 0; m < pId1Val.length; m++) {
                }
                for (int m = 0; m < pId2Val.length; m++) {
                }
                double temp = 0.0f;
                int tempId = 0;
                boolean flag = false;
                for (int i = 0; i < pId1Val.length; i++) {
                    for (int j = i; j < pId1Val.length; j++) {
                        if (pId1Val[i] < pId1Val[j]) {
                            temp = pId1Val[i];
                            pId1Val[i] = pId1Val[j];
                            pId1Val[j] = temp;
                            tempId = pId1[i];
                            pId1[i] = pId1[j];
                            pId1[j] = tempId;
                        }
                    }
                }
                for (int i = 0; i < pId2Val.length; i++) {
                    for (int j = i; j < pId2Val.length; j++) {
                        if (pId2Val[i] < pId2Val[j]) {
                            temp = pId2Val[i];
                            pId2Val[i] = pId2Val[j];
                            pId2Val[j] = temp;
                            tempId = pId2[i];
                            pId2[i] = pId2[j];
                            pId2[j] = tempId;
                        }
                    }
                }
                if (pId1yes && pId2yes) {
                    minLoadServ1 = pId1Val[pId1Val.length - 1];
                    minLoadServ2 = pId2Val[pId2Val.length - 1];
                } else if (!pId1yes && pId2yes) {
                    minLoadServ2 = pId2Val[pId2Val.length - 1];
                } else if (!pId2yes && pId1yes) {
                    minLoadServ1 = pId1Val[pId1Val.length - 1];
                }
                if (Energy1 < totalProcessEnergy1 || Energy2 < totalProcessEnergy2) {
                    System.out.println("Total Energy can not be less than total process Energy");
                    Transfer[0][0] = 0;
                    return Transfer;
                }
                incTr = -1;
                inc = -1;
                if ((Energy1 > Energy2 + minLoadServ1) && pId1yes) {
                    if (pId1.length == 1) {
                        Transfer[0][0] = 0;
                    } else {
                        serverId = 1;
                        while ((Energy1 > Energy2 + minLoadServ1)) {
                            if (inc < (lengthpId1 - 1)) {
                                inc++;
                                incTr++;
                            } else {
                                break;
                            }
                            if (((Energy1 - pId1Val[inc]) - Energy2 + pId1Val[inc]) > 0) {
                                Energy1 = Energy1 - pId1Val[inc];
                                Energy2 = Energy2 + pId1Val[inc];
                                Transfer[incTr + 1][0] = pId1[inc];
                                Transfer[incTr + 1][1] = requestId1[inc];
                            } else {
                                incTr--;
                            }
                        }
                    }
                } else if ((Energy2 > Energy1 + minLoadServ2 && Math.abs(Energy1 - Energy2) > ThresholdLimit) && pId2yes) {
                    if (pId2.length == 1) {
                        Transfer[0][0] = 0;
                    } else {
                        serverId = 2;
                        while ((Energy2 > Energy1 + minLoadServ2)) {
                            if (inc < (lengthpId2 - 1)) {
                                inc++;
                                incTr++;
                            } else {
                                break;
                            }
                            if (((Energy2 - pId2Val[inc]) - (Energy1 + pId2Val[inc])) > 0) {
                                Energy2 = Energy2 - pId2Val[inc];
                                Energy1 = Energy1 + pId2Val[inc];
                                Transfer[incTr + 1][0] = pId2[inc];
                                Transfer[incTr + 1][1] = requestId2[inc];
                            } else {
                                incTr--;
                            }
                        }
                    }
                }
                Transfer[0][0] = serverId;
                System.out.println("INTO TRANSFER:" + Transfer.length + "--" + Transfer[0] + "--" + Transfer[1] + "--" + Transfer[2]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        OrigTransfer = new int[incTr + 2][2];
        int m = 0;
        if (Transfer[0][0] == 0) {
            OrigTransfer[0][0] = 0;
        } else {
            while (Transfer[m][0] != 0) {
                OrigTransfer[m][0] = Transfer[m][0];
                OrigTransfer[m][1] = Transfer[m][1];
                if (m == 0) {
                } else {
                }
                m++;
            }
        }
        try {
            String sql = "";
            int count;
            PreparedStatement prest;
            for (int j = 1; j < OrigTransfer.length; j++) {
                sql = "insert into os.LoadTransfer" + "(requestId,processId,serverFrom,creationDt,creationTime) values (" + OrigTransfer[j][1] + "," + OrigTransfer[j][0] + "," + OrigTransfer[0][0] + ",curdate(),curtime())";
                prest = c1.prepareStatement(sql);
                count = prest.executeUpdate();
            }
        } catch (Exception e) {
        }
        int incr = 0;
        while (incr < OrigTransfer.length) {
            System.out.println(OrigTransfer[incr][0] + " " + OrigTransfer[incr][1]);
            incr++;
        }
        System.out.println("Energy1 is = " + Energy1);
        System.out.println("Energy2 is = " + Energy2);
        return OrigTransfer;
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
            integratorSNew remoteObject;
            NewThread n1 = new NewThread("1", prop);
            NewThread n2 = new NewThread("2", prop);
            try {
                System.out.println("Waiting for threads to finish.");
                n1.t.join();
                n2.t.join();
            } catch (InterruptedException e) {
                System.out.println("Main thread Interrupted");
            }
            System.out.println("Thread One is alive: " + n1.t.isAlive());
            System.out.println("Thread Two is alive: " + n2.t.isAlive());
            System.out.println("OUT: " + n1.getval(0));
            System.out.println("OUT: " + n2.getval(1));
            double energy1 = n1.getval(0);
            double energy2 = n2.getval(1);
            System.out.println("ENER1: " + energy1);
            System.out.println("ENER2: " + energy2);
            i = processEnergy(c1, requestId, loadId, energy1, energy2);
            System.out.println("2 After insert into processEnergy");
            if (flag1 == 0) i = loadBalancer(requestId, loadId, energy1, energy2); else i = MinEnergy.divertNewRequest((float) energy1, (float) energy2);
            System.out.println("3 After loadbalancer" + i);
            if (i == 1) {
                int k = createLoad(c1, requestId, loadId, i);
                remoteObject = (integratorSNew) Naming.lookup(Server1url);
                k = remoteObject.runLoad(requestId, loadId, i);
                System.out.println("before closeLoad");
                k = closeLoad(c1, requestId, loadId, i);
                System.out.println("after closeLoad");
            } else if (i == 2) {
                int k = createLoad(c1, requestId, loadId, i);
                remoteObject = (integratorSNew) Naming.lookup(Server2url);
                k = remoteObject.runLoad(requestId, loadId, i);
                k = closeLoad(c1, requestId, loadId, i);
            }
            if (i == 1 || i == 2) {
                sql = "update os.Request set processFlg='Y' where requestId=" + requestId;
                PreparedStatement prest = c1.prepareStatement(sql);
                int count = prest.executeUpdate();
            }
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

    public int GUIEnergy() throws RemoteException {
        CommonService x1 = new CommonService();
        Connection c1 = x1.initiateCon();
        int ins;
        Statement st;
        String sql123, sql;
        String url;
        double energy1, energy2;
        try {
            integratorSNew remoteObject = (integratorSNew) Naming.lookup(Server1url);
            energy1 = remoteObject.probeEnergy();
            remoteObject = (integratorSNew) Naming.lookup(Server2url);
            energy2 = remoteObject.probeEnergy();
            int i;
            i = processEnergy(c1, 0, 0, energy1, energy2);
        } catch (java.net.MalformedURLException me) {
            System.out.println("Malformed URL: " + me.toString());
        } catch (RemoteException re) {
            System.out.println("Remote exception: " + re.toString());
        } catch (java.rmi.NotBoundException exc) {
            System.out.println("NotBound: " + exc.toString());
        }
        return 0;
    }

    public int ProcessDaemon(int flag1) throws RemoteException {
        CommonService x1 = new CommonService();
        Connection c1 = x1.initiateCon();
        String[][] res1, res2;
        int ins;
        Statement st;
        String sql123, sql;
        String url;
        double energy1, energy2;
        int transfer[][];
        try {
            integratorSNew remoteObject = (integratorSNew) Naming.lookup(Server1url);
            energy1 = remoteObject.probeEnergy();
            remoteObject = (integratorSNew) Naming.lookup(Server2url);
            energy2 = remoteObject.probeEnergy();
            int i;
            i = processEnergy(c1, 0, 0, energy1, energy2);
            System.out.println("Step 1 Ener1:" + energy1 + " Ener2:" + energy2);
            i = Process2EnergyMain(1);
            i = Process2EnergyMain(2);
            System.out.println("Step 2");
            sql = "select processId,energy,requestId from os.ProcessEnergy where " + " processFlag ='N' and energy > 0 and serverNo=1";
            res1 = null;
            res2 = null;
            try {
                res1 = x1.Serlist(sql);
            } catch (Exception e0) {
                System.out.println("No res1");
            }
            int res1len, res2len;
            if (res1[0][0].equals("-1")) {
                res1 = null;
                res1len = 0;
            } else res1len = res1.length;
            System.out.println("Step 3");
            sql = "select processId,energy,requestId from os.ProcessEnergy where " + " processFlag ='N' and energy > 0 and serverNo=2";
            try {
                res2 = x1.Serlist(sql);
            } catch (Exception e1) {
                System.out.println("No res2");
            }
            if (res2[0][0].equals("-1")) {
                res2 = null;
                res2len = 0;
            } else res2len = res2.length;
            System.out.println("Step 41");
            System.out.println("Step 5" + res2len);
            int rs1_processId[] = new int[res1len];
            int rs1_requestId[] = new int[res1len];
            int rs2_processId[] = new int[res2len];
            int rs2_requestId[] = new int[res2len];
            double rs1_energy[] = new double[res1len];
            double rs2_energy[] = new double[res2len];
            for (i = 0; i < res1len; i++) {
                System.out.println("INPUT1 PRO:" + res1[i][0] + "ENER:" + res1[i][1] + "REQ:" + res1[i][2]);
                rs1_processId[i] = Integer.parseInt(res1[i][0]);
                rs1_energy[i] = Double.parseDouble(res1[i][1]);
                rs1_requestId[i] = Integer.parseInt(res1[i][2]);
            }
            for (i = 0; i < res2len; i++) {
                System.out.println("INPUT2 PRO:" + res2[i][0] + "ENER:" + res2[i][1] + "REQ:" + res2[i][2]);
                rs2_processId[i] = Integer.parseInt(res2[i][0]);
                rs2_energy[i] = Double.parseDouble(res2[i][1]);
                rs2_requestId[i] = Integer.parseInt(res2[i][2]);
            }
            System.out.println("Step 6");
            if (flag1 == 0) transfer = loadTransfer(energy1, energy2, rs1_processId, rs2_processId, rs1_energy, rs2_energy, rs1_requestId, rs2_requestId, 1); else transfer = MinEnergy.loadbalanceRequest((float) energy1, (float) energy2);
            System.out.println("Step 61" + transfer[0][0] + "len:" + transfer.length);
            int m;
            for (int j = 1; j < transfer.length; j++) {
                if (transfer[0][0] == 1) {
                    System.out.println("Stemp 62 ");
                    remoteObject = (integratorSNew) Naming.lookup(Server1url);
                    System.out.println("Stemp 63 ");
                    m = remoteObject.FtpPush(Server2Ip, "mohan", "vkmohan123", transfer[j][0]);
                    System.out.println("Stemp 64 ");
                    remoteObject = (integratorSNew) Naming.lookup(Server2url);
                    System.out.println("Stemp 65 ");
                    m = remoteObject.chkRestart(transfer[j][0]);
                    System.out.println(Server2url);
                    System.out.println("Stemp 66 ");
                }
                if (transfer[0][0] == 2) {
                    System.out.println("Stemp 62 ");
                    remoteObject = (integratorSNew) Naming.lookup(Server2url);
                    System.out.println("Stemp 63 ");
                    m = remoteObject.FtpPush(Server1Ip, "mohan", "vkmohan123", transfer[j][0]);
                    System.out.println("Stemp 64 ");
                    remoteObject = (integratorSNew) Naming.lookup(Server1url);
                    System.out.println("Stemp 65 ");
                    m = remoteObject.chkRestart(transfer[j][0]);
                    System.out.println(Server1url);
                    System.out.println("Stemp 66 ");
                }
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
                String url;
                if (serverId == 1) url = Server1url; else url = Server2url;
                integratorSNew remoteObject;
                remoteObject = (integratorSNew) Naming.lookup(url);
                energy = remoteObject.probeProcessEnergy(Integer.parseInt(res[m][0]));
                System.out.println("Step 9" + energy + "process:" + res[m][0]);
                try {
                    st = c11.createStatement();
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

    public int QueueDaemon() throws RemoteException {
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
            int i, count;
            int requestId, loadId;
            for (int m = 0; m < res.length; m++) {
                requestId = Integer.parseInt(res[m][0]);
                loadId = Integer.parseInt(res[m][1]);
                integratorSNew remoteObject = (integratorSNew) Naming.lookup(Server1url);
                double energy1 = remoteObject.probeEnergy();
                remoteObject = (integratorSNew) Naming.lookup(Server2url);
                double energy2 = remoteObject.probeEnergy();
                i = processEnergy(c1, requestId, loadId, energy1, energy2);
                System.out.println("2 After insert into processEnergy");
                i = loadBalancer(requestId, loadId, energy1, energy2);
                System.out.println("3 After loadbalancer");
                if (i == 1) {
                    int k = createLoad(c1, requestId, loadId, i);
                    remoteObject = (integratorSNew) Naming.lookup(Server1url);
                    k = remoteObject.runLoad(requestId, loadId, i);
                    System.out.println("before closeLoad");
                    k = closeLoad(c1, requestId, loadId, i);
                    System.out.println("after closeLoad");
                } else if (i == 2) {
                    int k = createLoad(c1, requestId, loadId, i);
                    remoteObject = (integratorSNew) Naming.lookup(Server2url);
                    k = remoteObject.runLoad(requestId, loadId, i);
                    k = closeLoad(c1, requestId, loadId, i);
                }
                if (i == 1 || i == 2) {
                    sql = "update os.Request set processFlg='Y' where requestId=" + requestId;
                    prest = c1.prepareStatement(sql);
                    count = prest.executeUpdate();
                }
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
        try {
            String execParm = linuxPath + "/chk.sh 2 " + linuxPath + "context." + pid;
            System.out.println("******into chkRestart" + execParm);
            Process P = Runtime.getRuntime().exec(execParm);
            System.out.println("******after run chkRestart");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public int SimulateLoadTransfer() throws RemoteException {
        CommonService x1 = new CommonService();
        Connection c1 = x1.initiateCon();
        String[][] res1, res2;
        int ins;
        Statement st;
        String sql123, sql;
        String url;
        double energy1, energy2;
        int transfer[][];
        try {
            integratorSNew remoteObject = (integratorSNew) Naming.lookup(Server1url);
            energy1 = remoteObject.probeEnergy();
            remoteObject = (integratorSNew) Naming.lookup(Server2url);
            energy2 = remoteObject.probeEnergy();
            int i;
            i = processEnergy(c1, 0, 0, energy1, energy2);
            System.out.println("Step 1 Ener1:" + energy1 + " Ener2:" + energy2);
            i = Process2EnergyMain(1);
            i = Process2EnergyMain(2);
            System.out.println("Step 2");
            sql = "select processId,energy,requestId from os.ProcessEnergy where " + " processFlag ='N' and energy > 0 and serverNo=1";
            res1 = null;
            res2 = null;
            try {
                res1 = x1.Serlist(sql);
            } catch (Exception e0) {
                System.out.println("No res1");
            }
            int res1len, res2len;
            if (res1[0][0].equals("-1")) {
                res1 = null;
                res1len = 0;
            } else res1len = res1.length;
            System.out.println("Step 3");
            sql = "select processId,energy,requestId from os.ProcessEnergy where " + " processFlag ='N' and energy > 0 and serverNo=2";
            try {
                res2 = x1.Serlist(sql);
            } catch (Exception e1) {
                System.out.println("No res2");
            }
            if (res2[0][0].equals("-1")) {
                res2 = null;
                res2len = 0;
            } else res2len = res2.length;
            System.out.println("Step 41");
            System.out.println("Step 5" + res2len);
            int rs1_processId[] = new int[res1len];
            int rs1_requestId[] = new int[res1len];
            int rs2_processId[] = new int[res2len];
            int rs2_requestId[] = new int[res2len];
            double rs1_energy[] = new double[res1len];
            double rs2_energy[] = new double[res2len];
            for (i = 0; i < res1len; i++) {
                System.out.println("INPUT1 PRO:" + res1[i][0] + "ENER:" + res1[i][1] + "REQ:" + res1[i][2]);
                rs1_processId[i] = Integer.parseInt(res1[i][0]);
                rs1_energy[i] = Double.parseDouble(res1[i][1]);
                rs1_requestId[i] = Integer.parseInt(res1[i][2]);
            }
            for (i = 0; i < res2len; i++) {
                System.out.println("INPUT2 PRO:" + res2[i][0] + "ENER:" + res2[i][1] + "REQ:" + res2[i][2]);
                rs2_processId[i] = Integer.parseInt(res2[i][0]);
                rs2_energy[i] = Double.parseDouble(res2[i][1]);
                rs2_requestId[i] = Integer.parseInt(res2[i][2]);
            }
            System.out.println("Step 6");
            transfer = loadTransfer(energy1, energy2, rs1_processId, rs2_processId, rs1_energy, rs2_energy, rs1_requestId, rs2_requestId, 0);
            System.out.println("Step 61" + transfer[0][0] + "len:" + transfer.length);
            int m;
            for (int j = 1; j < transfer.length; j++) {
                if (transfer[0][0] == 1) {
                    System.out.println("Stemp 62 ");
                    remoteObject = (integratorSNew) Naming.lookup(Server1url);
                    System.out.println("Stemp 63 ");
                    m = remoteObject.FtpPush(Server2Ip, "mohan", "vkmohan123", transfer[j][0]);
                    System.out.println("Stemp 64 ");
                    remoteObject = (integratorSNew) Naming.lookup(Server2url);
                    System.out.println("Stemp 65 ");
                    m = remoteObject.chkRestart(transfer[j][0]);
                    System.out.println(Server2url);
                    System.out.println("Stemp 66 ");
                }
                if (transfer[0][0] == 2) {
                    System.out.println("Stemp 62 ");
                    remoteObject = (integratorSNew) Naming.lookup(Server2url);
                    System.out.println("Stemp 63 ");
                    m = remoteObject.FtpPush(Server1Ip, "mohan", "vkmohan123", transfer[j][0]);
                    System.out.println("Stemp 64 ");
                    remoteObject = (integratorSNew) Naming.lookup(Server1url);
                    System.out.println("Stemp 65 ");
                    m = remoteObject.chkRestart(transfer[j][0]);
                    System.out.println(Server1url);
                    System.out.println("Stemp 66 ");
                }
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

    public static void main(String args[]) {
        try {
            System.setSecurityManager(new RMISecurityManager());
            integratorServerNew Server = new integratorServerNew();
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

    integratorSNew remoteObject;

    String name;

    Thread t;

    int i;

    Double var[] = new Double[10];

    String url = "//192.168.15.110/SAMPLE-SERVER";

    String Server1url;

    String Server2url;

    NewThread(String threadname, Properties prop) {
        System.out.println("\nThe foo property222:" + prop.getProperty("Server1url"));
        name = threadname;
        Server1url = prop.getProperty("Server1url");
        Server2url = prop.getProperty("Server2url");
        i = Integer.parseInt(name) - 1;
        t = new Thread(this, name);
        System.out.println("New thread: " + t);
        t.start();
    }

    public double getval(int i) {
        return var[i];
    }

    public void run() {
        try {
            if (i == 0) url = Server1url; else url = Server2url;
            remoteObject = (integratorSNew) Naming.lookup(url);
            var[i] = remoteObject.probeEnergy();
            System.out.println("Test:" + var[i] + "i:" + i);
            Thread.sleep(1);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
