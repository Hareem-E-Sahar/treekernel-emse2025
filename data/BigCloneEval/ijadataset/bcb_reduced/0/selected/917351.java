package org.grailrtls.solver;

import java.util.*;
import java.io.*;
import java.util.Date;
import java.text.SimpleDateFormat;

public class FastSolver implements SolverIfc {

    public static final String STAT_FILE_SUFFIX = "_log.txt";

    private String model_file = "wireless_dat_fast.txt";

    private String landmarks_file = "landmarks.txt";

    private String dataWorkDir, codeWorkDir;

    private String filesCreated = "";

    private HashMap<String, Object> info;

    public FastSolver() {
        this.info = null;
    }

    public void setInfo(HashMap<String, Object> info, String workDir) {
        this.info = info;
        this.dataWorkDir = workDir;
        this.codeWorkDir = workDir;
    }

    private void createWorkDirectory() {
        Date current = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String timeStamp = formatter.format(current);
        dataWorkDir = dataWorkDir + "/" + timeStamp;
        try {
            boolean success = (new File(dataWorkDir)).mkdir();
            if (success) System.out.println("Directory: " + dataWorkDir + " created for this run...");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void deleteWorkDirectory() {
        try {
            boolean success = (new File(dataWorkDir)).delete();
            if (success) System.out.println("Directory: " + dataWorkDir + " deleted...");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void solve() throws Exception {
        System.out.println("Fast Solver triggered");
        ArrayList<Object> list;
        String train_str = "";
        String test_str = "";
        String landmarks_str = "";
        String cmd, stats_file, tmp;
        Process p;
        this.createWorkDirectory();
        File codeWorkDirFile = (new File(this.codeWorkDir)).getAbsoluteFile();
        list = (ArrayList<Object>) this.info.get("landmarks");
        for (Object obj : list) landmarks_str = landmarks_str + ((Landmark) obj).toString2() + "\n";
        PrintStream landmarks_f = new PrintStream(new File(this.dataWorkDir + "/" + landmarks_file));
        this.filesCreated = this.filesCreated + " " + landmarks_file;
        landmarks_f.print(landmarks_str);
        landmarks_f.close();
        list = (ArrayList<Object>) this.info.get("training");
        for (Object obj : list) train_str = train_str + obj.toString() + "\n";
        list = (ArrayList<Object>) this.info.get("testing");
        if (this.info.get("algorithm").equals("fs_m1")) {
            for (int i = 0; i < list.size(); i++) {
                test_str = list.get(i).toString();
                PrintStream model_f = new PrintStream(new File(this.dataWorkDir + "/" + model_file));
                this.filesCreated = this.filesCreated + " " + model_file;
                model_f.println(train_str + test_str);
                model_f.close();
                stats_file = dataWorkDir + "/" + i + STAT_FILE_SUFFIX;
                this.filesCreated = this.filesCreated + " " + stats_file;
                tmp = (this.info.containsKey("burin") ? this.info.get("burnin") : new Integer(1000)) + " " + (this.info.containsKey("iterations") ? this.info.get("iterations") : new Integer(5000)) + " " + dataWorkDir + "/" + model_file + " " + dataWorkDir + "/" + landmarks_file + " " + this.info.get("maxx") + " " + this.info.get("maxy");
                cmd = codeWorkDirFile.getPath() + "/M1 " + tmp + " slice1 -a " + stats_file + " -d";
                System.out.println("Solve:\n" + cmd);
                p = Runtime.getRuntime().exec(cmd);
                ProcessOutputHandler.create(p);
                while (true) {
                    try {
                        p.waitFor();
                        break;
                    } catch (InterruptedException e) {
                    } finally {
                        if (p != null) {
                            closehandle(p.getOutputStream());
                            closehandle(p.getInputStream());
                            closehandle(p.getErrorStream());
                            p.destroy();
                        }
                    }
                }
            }
        } else {
            test_str = "";
            for (int i = 0; i < list.size(); i++) test_str = test_str + list.get(i).toString() + "\n";
            PrintStream model_f = new PrintStream(new File(this.dataWorkDir + "/" + model_file));
            this.filesCreated = this.filesCreated + " " + model_file;
            model_f.println(train_str + test_str);
            model_f.close();
            stats_file = dataWorkDir + "/" + "all" + STAT_FILE_SUFFIX;
            this.filesCreated = this.filesCreated + " " + stats_file;
            tmp = (this.info.containsKey("burin") ? this.info.get("burnin") : new Integer(1000)) + " " + (this.info.containsKey("iterations") ? this.info.get("iterations") : new Integer(5000)) + " " + dataWorkDir + "/" + model_file + " " + dataWorkDir + "/" + landmarks_file + " " + this.info.get("maxx") + " " + this.info.get("maxy");
            if (this.info.get("algorithm").equals("fs_m2")) cmd = codeWorkDirFile.getPath() + "/M2 " + tmp + " slice1 -a " + stats_file + " -d"; else cmd = codeWorkDirFile.getPath() + "/M2 " + tmp + " slice1all -a " + stats_file + " -d";
            System.out.println("Solve:\n" + cmd);
            p = Runtime.getRuntime().exec(cmd);
            ProcessOutputHandler.create(p);
            while (true) {
                try {
                    p.waitFor();
                    break;
                } catch (InterruptedException e) {
                } finally {
                    if (p != null) {
                        closehandle(p.getOutputStream());
                        closehandle(p.getInputStream());
                        closehandle(p.getErrorStream());
                        p.destroy();
                    }
                }
            }
        }
        System.out.println("Done!");
    }

    private static void closehandle(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void printResult(PrintWriter out) throws Exception {
        ArrayList<FingerPrint> list = (ArrayList<FingerPrint>) this.info.get("testing");
        String netID = null;
        Scanner sc = null;
        String next;
        double xMedian, yMedian, xSTD, ySTD;
        System.out.println("----------------------");
        System.out.println("print result:");
        System.out.println("----------------------");
        if (this.info.get("algorithm").equals("fs_m1")) {
            for (int i = 0; i < list.size(); i++) {
                netID = list.get(i).getNetID();
                sc = new Scanner(new File(this.dataWorkDir + "/" + i + STAT_FILE_SUFFIX));
                next = sc.next();
                while (!next.startsWith("X")) {
                    sc.nextLine();
                    next = sc.next();
                }
                sc.next();
                xSTD = sc.nextDouble();
                sc.next();
                xMedian = sc.nextDouble();
                sc.nextLine();
                sc.next();
                sc.next();
                ySTD = sc.nextDouble();
                sc.next();
                yMedian = sc.nextDouble();
                sc.close();
                out.println(netID + " " + xMedian + " " + yMedian + " " + xSTD + " " + ySTD);
                System.out.println(netID + " " + xMedian + " " + yMedian + " " + xSTD + " " + ySTD);
            }
        } else {
            double[] xMedianArray = new double[list.size()];
            double[] xStd = new double[list.size()];
            double[] yMedianArray = new double[list.size()];
            double[] yStd = new double[list.size()];
            sc = new Scanner(new File(this.dataWorkDir + "/" + "all" + STAT_FILE_SUFFIX));
            next = sc.next();
            while (!next.startsWith("X")) {
                sc.nextLine();
                next = sc.next();
            }
            for (int i = 0; i < list.size(); i++) {
                sc.next();
                xStd[i] = sc.nextDouble();
                sc.next();
                xMedianArray[i] = sc.nextDouble();
                sc.nextLine();
                next = sc.next();
            }
            for (int i = 0; i < list.size(); i++) {
                sc.next();
                yStd[i] = sc.nextDouble();
                sc.next();
                yMedianArray[i] = sc.nextDouble();
                sc.nextLine();
                if (i < (list.size() - 1)) next = sc.next();
            }
            for (int i = 0; i < list.size(); i++) {
                netID = list.get(i).getNetID();
                out.println(netID + " " + xMedianArray[i] + " " + yMedianArray[i] + " " + xStd[i] + " " + yStd[i]);
                System.out.println(netID + " " + xMedianArray[i] + " " + yMedianArray[i] + " " + xStd[i] + " " + yStd[i]);
            }
            sc.close();
        }
        System.out.println("----------------------");
    }

    public void cleanUp() throws Exception {
        File workDirFile = (new File(this.dataWorkDir)).getAbsoluteFile();
        String cmd = "/bin/rm " + this.filesCreated;
        this.filesCreated = "";
        this.info = null;
        System.out.println("cleanUp:\n" + cmd);
        Process p = Runtime.getRuntime().exec(cmd, null, workDirFile);
        ProcessOutputHandler.create(p);
        while (true) {
            try {
                p.waitFor();
                break;
            } catch (InterruptedException e) {
            } finally {
                if (p != null) {
                    closehandle(p.getOutputStream());
                    closehandle(p.getInputStream());
                    closehandle(p.getErrorStream());
                    p.destroy();
                }
            }
        }
        this.deleteWorkDirectory();
    }
}
