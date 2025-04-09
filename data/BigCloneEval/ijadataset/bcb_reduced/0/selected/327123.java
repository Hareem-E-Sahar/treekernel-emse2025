package com.kanteron.PacsViewer;

import ij.IJ;
import java.io.*;
import java.util.Vector;

public class MyDCM {

    public MyDCM() {
        if (IJ.isMacOSX()) {
            BIN_DCMECHO = Messages.getString("MyDCM.0");
            BIN_DCMQR = Messages.getString("MyDCM.1");
        }
        if (IJ.isLinux()) {
            BIN_DCMECHO = Messages.getString("MyDCM.2");
            BIN_DCMQR = Messages.getString("MyDCM.3");
        }
        if (IJ.isWindows()) {
            BIN_DCMECHO = Messages.getString("MyDCM.4");
            BIN_DCMQR = Messages.getString("MyDCM.5");
        }
    }

    public void setData(String nodo, String direccion, String puerto) {
        aeTitle = nodo;
        address = direccion;
        port = puerto;
    }

    public void setParent(PacsViewer visor) {
        parent = visor;
    }

    public void doEcho() {
        String cmd = Messages.getString("MyDCM.6");
        cmd = (new StringBuilder()).append(userDir).append(BIN_DCMECHO).append(aeTitle).append(Messages.getString("MyDCM.7")).append(address).append(Messages.getString("MyDCM.8")).append(port).toString();
        try {
            System.out.println(cmd);
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                System.out.println(line);
                parent.showMessage(line);
            }
            input.close();
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public void doQR1() {
        String cmd = Messages.getString("MyDCM.9");
        MyParser parser = new MyParser();
        Vector total1 = new Vector();
        Vector total2 = new Vector();
        try {
            cmd = (new StringBuilder()).append(userDir).append(BIN_DCMQR).append(aeTitle).append(Messages.getString("MyDCM.10")).append(address).append(Messages.getString("MyDCM.11")).append(port).toString();
            total1 = executer(cmd);
            cmd = (new StringBuilder()).append(userDir).append(BIN_DCMQR).append(Messages.getString("MyDCM.12")).append(aeTitle).append(Messages.getString("MyDCM.13")).append(address).append(Messages.getString("MyDCM.14")).append(port).toString();
            total2 = executer(cmd);
            combineVectors1(1, parser.parsePatient(total2.toArray()), parser.parseStudyUID(total1.toArray()), parser.parseNumberSeries(total1.toArray()), parser.parseNumberImages(total1.toArray()));
            parent.showMessage(Messages.getString("MyDCM.15") + aeTitle + Messages.getString("MyDCM.16") + address + Messages.getString("MyDCM.17") + port);
        } catch (Exception err) {
            err.printStackTrace();
            parent.showMessage(Messages.getString("MyDCM.18") + aeTitle + Messages.getString("MyDCM.19") + address + Messages.getString("MyDCM.20") + port);
        }
    }

    public void doQR1_2() {
        String cmd = Messages.getString("MyDCM.21");
        MyParser parser = new MyParser();
        Vector total1 = new Vector();
        try {
            cmd = (new StringBuilder()).append(userDir).append(BIN_DCMQR).append(aeTitle).append(Messages.getString("MyDCM.22")).append(address).append(Messages.getString("MyDCM.23")).append(port).toString();
            total1 = executer(cmd);
            combineVectors1(1, parser.parsePatient(total1.toArray()), parser.parseStudyUID(total1.toArray()), parser.parseNumberSeries(total1.toArray()), parser.parseNumberImages(total1.toArray()));
            parent.showMessage(Messages.getString("MyDCM.24") + aeTitle + Messages.getString("MyDCM.25") + address + Messages.getString("MyDCM.26") + port);
        } catch (Exception err) {
            err.printStackTrace();
            parent.showMessage(Messages.getString("MyDCM.27") + aeTitle + Messages.getString("MyDCM.28") + address + Messages.getString("MyDCM.29") + port);
        }
    }

    public void doQR2(String id) {
        String cmd = Messages.getString("MyDCM.30");
        MyParser parser = new MyParser();
        Vector total1 = new Vector();
        Vector total2 = new Vector();
        try {
            cmd = (new StringBuilder()).append(userDir).append(BIN_DCMQR).append(aeTitle).append(Messages.getString("MyDCM.31")).append(address).append(Messages.getString("MyDCM.32")).append(port).append(Messages.getString("MyDCM.33")).append(id).toString();
            total1 = executer(cmd);
            combineVectors1(2, parser.parseSeriesModality(total1.toArray()), parser.parseSeriesImagesCount(total1.toArray()), parser.parseStudyUID(total1.toArray()), parser.parseSeriesUID(total1.toArray()));
            parent.showMessage(Messages.getString("MyDCM.34") + aeTitle + Messages.getString("MyDCM.35") + address + Messages.getString("MyDCM.36") + port);
        } catch (Exception err) {
            err.printStackTrace();
            parent.showMessage(Messages.getString("MyDCM.37") + aeTitle + Messages.getString("MyDCM.38") + address + Messages.getString("MyDCM.39") + port);
        }
    }

    public void doQR2Send(String id, String dest) {
        String cmd = Messages.getString("MyDCM.40");
        MyParser parser = new MyParser();
        Vector total1 = new Vector();
        Vector total2 = new Vector();
        try {
            cmd = (new StringBuilder()).append(userDir).append(BIN_DCMQR).append(aeTitle).append(Messages.getString("MyDCM.41")).append(address).append(Messages.getString("MyDCM.42")).append(port).append(Messages.getString("MyDCM.43")).append(id).append(Messages.getString("MyDCM.44")).append(dest).toString();
            total1 = executer(cmd);
            parent.showMessage(Messages.getString("MyDCM.45") + aeTitle + Messages.getString("MyDCM.46") + address + Messages.getString("MyDCM.47") + port);
        } catch (Exception err) {
            err.printStackTrace();
            parent.showMessage(Messages.getString("MyDCM.48") + aeTitle + Messages.getString("MyDCM.49") + address + Messages.getString("MyDCM.50") + port);
        }
    }

    public void doQR3(String id) {
        String cmd = Messages.getString("MyDCM.51");
        MyParser parser = new MyParser();
        Vector total1 = new Vector();
        try {
            cmd = (new StringBuilder()).append(userDir).append(BIN_DCMQR).append(aeTitle).append(Messages.getString("MyDCM.52")).append(address).append(Messages.getString("MyDCM.53")).append(port).append(Messages.getString("MyDCM.54")).append(id).toString();
            total1 = executer(cmd);
            combineVectors3(3, parser.parseImages(total1.toArray()));
            parent.showMessage(Messages.getString("MyDCM.55") + aeTitle + Messages.getString("MyDCM.56") + address + Messages.getString("MyDCM.57") + port);
        } catch (Exception err) {
            err.printStackTrace();
            parent.showMessage(Messages.getString("MyDCM.58") + aeTitle + Messages.getString("MyDCM.59") + address + Messages.getString("MyDCM.60") + port);
        }
    }

    private Vector executer(String cmd) {
        Vector result = new Vector();
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) result.addElement(line);
            input.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    private void combineVectors1(int table, Vector vector0, Vector vector1, Vector vector2, Vector vector3) {
        int total = vector0.size();
        int init = 0;
        String str = (String) vector0.elementAt(0);
        if (str.startsWith(Messages.getString("MyDCM.61"))) init = 1;
        if (str.startsWith(Messages.getString("MyDCM.62"))) init = 1;
        if (str.startsWith(Messages.getString("MyDCM.63"))) init = 1;
        if (str.startsWith(Messages.getString("MyDCM.64"))) init = 1;
        if (str.startsWith(Messages.getString("MyDCM.65"))) init = 1;
        if (str.startsWith(Messages.getString("MyDCM.66"))) init = 1;
        if (str.startsWith(Messages.getString("MyDCM.67"))) init = 1;
        if (str.startsWith(Messages.getString("MyDCM.68"))) init = 1;
        for (int i = init; i < total; i++) {
            Vector vector = new Vector();
            vector.addElement(vector0.elementAt(i));
            vector.addElement(vector1.elementAt(i));
            vector.addElement(vector2.elementAt(i));
            vector.addElement(vector3.elementAt(i));
            toTable(table, vector);
        }
    }

    private void combineVectors2(int table, Vector vector0, Vector vector1) {
        int total = vector0.size();
        int init = 0;
        String str = (String) vector0.elementAt(0);
        if (str.startsWith(Messages.getString("MyDCM.69"))) init = 1;
        if (str.startsWith(Messages.getString("MyDCM.70"))) init = 1;
        if (str.startsWith(Messages.getString("MyDCM.71"))) init = 1;
        if (str.startsWith(Messages.getString("MyDCM.72"))) init = 1;
        if (str.startsWith(Messages.getString("MyDCM.73"))) init = 1;
        if (str.startsWith(Messages.getString("MyDCM.74"))) init = 1;
        if (str.startsWith(Messages.getString("MyDCM.75"))) init = 1;
        if (str.startsWith(Messages.getString("MyDCM.76"))) init = 1;
        for (int i = 1; i < total; i++) {
            Vector vector = new Vector();
            vector.addElement(vector0.elementAt(i));
            vector.addElement(vector1.elementAt(i));
            toTable(table, vector);
        }
    }

    private void toTable(int table, Vector vector) {
        if (table == 1) parent.addRowTable1(vector);
        if (table == 2) parent.addRowTable2(vector);
        if (table == 3) parent.addRowTable3(vector);
    }

    private void combineVectors3(int table, Vector vector0) {
        int total = vector0.size();
        int init = 0;
        String str = (String) vector0.elementAt(0);
        if (str.startsWith(Messages.getString("MyDCM.77"))) init = 1;
        for (int i = init; i < total; i++) {
            Vector vector = new Vector();
            vector.addElement(vector0.elementAt(i));
            toTable(table, vector);
        }
    }

    public static void main(String args1[]) {
    }

    private static final String BIN_DCMQR_X = Messages.getString("MyDCM.78");

    private static final String BIN_DCMQR_WIN = Messages.getString("MyDCM.79");

    private static String BIN_DCMQR = Messages.getString("MyDCM.80");

    private static final String BIN_DCMECHO_X = Messages.getString("MyDCM.81");

    private static final String BIN_DCMECHO_WIN = Messages.getString("MyDCM.82");

    private static String BIN_DCMECHO = Messages.getString("MyDCM.83");

    public static File userDir = new File(System.getProperty(Messages.getString("MyDCM.84")));

    PacsViewer parent;

    String aeTitle;

    String address;

    String port;
}
