package edu.uno.edesk.student;

import static edu.uno.edesk.common.ServiceFinder.findBean;
import static edu.uno.edesk.common.ServiceFinder.getSession;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;
import edu.uno.edesk.faculty.Submission;

public class UploadBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String path = null;

    private String jarDirs = null;

    private StringBuilder stdIn;

    private StringBuilder stdErr;

    private List<String> fileNames = new ArrayList<String>();

    private List<File> uploadedFiles = new ArrayList<File>();

    private boolean compiled;

    private boolean error;

    private String lastSubmitted;

    private Submission submission = null;

    private LoginFormMgr mgr;

    public UploadBean() {
        mgr = (LoginFormMgr) findBean("LoginFormMgr");
    }

    public String getLastSubmitted() {
        String unoid = getSession().getAttribute("unoid").toString();
        String dir = getSession().getAttribute("uploaddir").toString();
        this.submission = mgr.getLastSubmission(unoid, dir.split("/")[0], dir.split("/")[1]);
        this.lastSubmitted = "N/A";
        if (this.submission != null) {
            java.util.Date date = this.submission.getSubDate();
            lastSubmitted = DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
        }
        return lastSubmitted;
    }

    public synchronized void upload(UploadEvent event) {
        List<UploadItem> items = event.getUploadItems();
        for (Iterator<UploadItem> i = items.iterator(); i.hasNext(); ) {
            UploadItem item = i.next();
            fileNames.add(item.getFileName());
            uploadedFiles.add(item.getFile());
        }
    }

    public synchronized void process() {
        if (fileNames.size() > 0) {
            Character compSucc;
            Character testSucc;
            this.path = new String(getSession().getAttribute("path").toString().replace(' ', '_'));
            copyToStudentFolder();
            this.error = false;
            this.compiled = false;
            compile();
            if (error) {
                saveErrorsInTxt("compile_errors.txt", "Exception saving compilation errors ::  ", true);
                compSucc = new Character('0');
                getSession().setAttribute("includePage", "/compilerror.jsp");
            } else {
                compSucc = new Character('1');
                compiled = true;
            }
            fileNames.clear();
            uploadedFiles.clear();
            if (!error) {
                runTests();
                if (error) {
                    saveErrorsInTxt("test_errors.txt", "Exception saving unit-test errors ::  ", false);
                }
            }
            testSucc = error ? new Character('0') : new Character('1');
            saveSubmission(compSucc, testSucc);
        }
    }

    private void saveSubmission(Character c, Character t) {
        String unoid = getSession().getAttribute("unoid").toString();
        String dir = getSession().getAttribute("uploaddir").toString();
        this.submission = new Submission();
        this.submission.setCompSucc(c);
        this.submission.setTestSucc(t);
        this.submission.setGrade(0);
        this.submission.setSubDate(new Date(System.currentTimeMillis()));
        mgr.saveSubmission(this.submission, unoid, dir.split("/")[0], dir.split("/")[1]);
    }

    private void compile() {
        String cmd;
        String s = null;
        Process p = null;
        BufferedReader stdError = null;
        stdIn = new StringBuilder();
        stdErr = new StringBuilder();
        String javaFile = null;
        javaFile = fileNames.get(0);
        if (getSession().getAttribute("type").toString().equals("jar")) {
            handleIfJar(javaFile);
            if (this.error) {
                if (p != null) {
                    try {
                        p.waitFor();
                        p.destroy();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return;
            }
        }
        String newPath = this.path;
        if (this.jarDirs != null) {
            newPath = this.path + this.jarDirs + "/";
        }
        try {
            for (Iterator<String> i = fileNames.iterator(); i.hasNext(); ) {
                javaFile = i.next();
                cmd = "javac " + javaFile;
                p = Runtime.getRuntime().exec(cmd, null, new File(newPath));
                stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                s = stdError.readLine();
                if (s != null) {
                    error = true;
                    stdErr.append("---------------------- " + javaFile + " -----------------------");
                    do {
                        stdErr.append("\n" + s);
                    } while ((s = stdError.readLine()) != null);
                    if (error) {
                        stdErr.append("\n");
                    }
                }
            }
            if (p != null) {
                p.waitFor();
                stdError.close();
                p.destroy();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleIfJar(String javaFile) {
        try {
            extractJar(javaFile);
        } catch (Exception e) {
            this.stdErr.append("Error while extracting jar file.\n");
            this.error = true;
            return;
        }
        this.jarDirs = getSession().getAttribute("pkg").toString().replace('.', '/');
        String newPath = this.path + this.jarDirs + "/";
        File jarredJava = new File(newPath);
        if (!(jarredJava).exists()) {
            this.stdErr.append("Incorrect package naming.\n");
            this.error = true;
            return;
        }
        fileNames.clear();
        fileNames.addAll(Arrays.asList(jarredJava.list()));
    }

    private void extractJar(String jarFile) throws Exception {
        String cmd;
        Process p = null;
        cmd = "jar xf " + jarFile;
        p = Runtime.getRuntime().exec(cmd, null, new File(this.path));
        if (p != null) {
            p.waitFor();
            p.destroy();
        }
    }

    private void copyToStudentFolder() {
        InputStream in;
        OutputStream out;
        byte[] buf;
        int len;
        String newFile;
        File dest = new File(path);
        if (!dest.exists()) {
            dest.mkdirs();
        }
        int j = 0;
        try {
            for (Iterator<File> i = uploadedFiles.iterator(); i.hasNext(); ) {
                File item = i.next();
                in = new FileInputStream(item);
                newFile = path + fileNames.get(j++);
                out = new FileOutputStream(newFile);
                buf = new byte[1024];
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                item.delete();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveErrorsInTxt(String writeTo, String errMsg, boolean isCompile) {
        try {
            FileWriter fstream = new FileWriter(path + writeTo);
            BufferedWriter out = new BufferedWriter(fstream);
            if (isCompile) {
                out.write(stdErr.toString());
            } else {
                out.write(stdIn.toString());
            }
            out.close();
        } catch (IOException e) {
            System.out.println(errMsg);
            e.printStackTrace();
        }
    }

    public synchronized void cancelUpload() {
        for (Iterator<File> i = uploadedFiles.iterator(); i.hasNext(); ) {
            i.next().delete();
        }
        this.fileNames.clear();
        this.uploadedFiles.clear();
        this.compiled = false;
        this.path = new String(getSession().getAttribute("path").toString().replace(' ', '_'));
        File dest = new File(this.path);
        deleteAll(dest);
        if (this.submission != null) {
            mgr.deleteSubmission(this.submission);
        }
    }

    private boolean deleteAll(File delFile) {
        if (delFile.exists()) {
            for (File f : delFile.listFiles()) {
                if (f.isDirectory()) {
                    deleteAll(f);
                } else {
                    f.delete();
                }
            }
        }
        return delFile.delete();
    }

    private void runTests() {
        String cmd;
        String s = null;
        Process p = null;
        BufferedReader stdInput = null;
        stdIn = new StringBuilder();
        stdErr = new StringBuilder();
        String newPath = this.path;
        if (this.jarDirs != null) {
            newPath = this.path + this.jarDirs + "/";
        }
        try {
            String testCase = "";
            String testCasePath = "C:/EclipseGany_Wksp/edesk/upload/junit/" + getSession().getAttribute("uploaddir").toString().replace(' ', '_');
            File testPath = new File(testCasePath);
            File[] children = testPath.listFiles();
            if (children != null) {
                int index = children[0].getName().lastIndexOf(".");
                testCase = children[0].getName().substring(0, index);
            }
            String classpath = " -cp C:/EclipseGany_Wksp/edesk/upload/junit/*;" + newPath + ";" + testCasePath;
            String tester = " org.junit.runner.JUnitCore ";
            cmd = "java" + classpath + tester + testCase;
            p = Runtime.getRuntime().exec(cmd);
            stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            s = stdInput.readLine();
            if (s != null) {
                error = true;
                do {
                    stdIn.append("\n" + s);
                } while ((s = stdInput.readLine()) != null);
                if (stdIn.length() < 50) {
                    error = false;
                }
                if (error) {
                    stdIn.append("\n");
                }
            }
            if (p != null) {
                p.waitFor();
                stdInput.close();
                p.destroy();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public List<String> getFileNames() {
        return fileNames;
    }

    public void setLastSubmitted(String lastSubmitted) {
        this.lastSubmitted = lastSubmitted;
    }

    public StringBuilder getStdErr() {
        return stdErr;
    }

    public boolean getCompiled() {
        return compiled;
    }

    public void setCompiled(boolean compiled) {
        this.compiled = compiled;
    }
}
