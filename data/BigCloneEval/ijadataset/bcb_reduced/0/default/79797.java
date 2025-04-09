import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import java.io.*;

public class SqlParallelExecuter {

    private static Logger log;

    public static void main(String[] args) {
        String xmlDir = System.getProperty("user.dir");
        boolean parallel = true;
        boolean append = false;
        boolean testing = false;
        String logFileName = "SPE_Log.log";
        for (int i = 0; i < args.length; i = i + 2) {
            if (args[i].equalsIgnoreCase("-d")) {
                String folderName = args[i + 1];
                File f = new File(folderName);
                if (f.exists() && f.isDirectory()) {
                    xmlDir = f.getAbsolutePath();
                }
                f = null;
            }
            if (args[i].equalsIgnoreCase("-p")) {
                if (args[i + 1].equalsIgnoreCase("on")) {
                    parallel = true;
                } else {
                    parallel = false;
                }
            }
            if (args[i].equalsIgnoreCase("-a")) {
                if (args[i + 1].equalsIgnoreCase("yes")) {
                    append = true;
                }
            }
            if (args[i].equalsIgnoreCase("-l")) {
                logFileName = args[i + 1];
            }
            if (args[i].equalsIgnoreCase("-t")) {
                if (args[i + 1].equalsIgnoreCase("yes")) testing = true; else testing = false;
            }
        }
        FileHandler hand = null;
        try {
            hand = new FileHandler(logFileName, append);
        } catch (Exception e) {
            System.out.println("The specified log file cannot be created, please check the path specified: " + logFileName);
            System.out.println("A generic log file with name SPE_Log.log is generated in the working directory.");
            try {
                hand = new FileHandler("SPE_Log.log", append);
            } catch (Exception e1) {
                System.out.println("The file cannot be created, please read the following information for further details.");
                e1.printStackTrace();
                System.exit(0);
            }
        }
        hand.setFormatter(new SimpleFormatter());
        log = Logger.getLogger("SqlParallelExecuter_Logger");
        log.setUseParentHandlers(false);
        log.addHandler(hand);
        log.setLevel(Level.ALL);
        log.info("SqlParallelExecuter started.");
        System.out.println("SQL Parallel Executer Copyright (C) 2011  Emiliano Marin");
        System.out.println("This program comes with ABSOLUTELY NO WARRANTY.");
        System.out.println("This is free software, and you are welcome to redistribute it");
        System.out.println("under certain conditions.");
        System.out.println("Welcome! The SqlParallelExecuter is going to process your SQL scripts...");
        System.out.println("The scripts are located in this folder: " + xmlDir);
        if (parallel == true) {
            System.out.println("SqlParallelExecuter runs scripts in parallel");
            log.info("SqlParallelExecuter is run in parallel");
        } else {
            System.out.println("SqlParallelExecuter runs scripts sequentially");
            log.info("SqlParallelExecuter is run sequentially");
        }
        if (testing == true) {
            System.out.println("SqlParallelExecuter runs in testing mode");
            log.info("SqlParallelExecuter is run in testing mode");
        } else {
            System.out.println("SqlParallelExecuter runs in normal mode");
            log.info("SqlParallelExecuter is run in normal mode");
        }
        log.info("xml files are searched in " + xmlDir);
        if (!CheckFileExists(xmlDir + "\\scripts.xml")) {
            System.out.println("The file scripts.xml is not available in: " + xmlDir);
            System.out.println("SQL Parallel Executer stops.");
            System.exit(0);
        }
        if (!CheckFileExists(xmlDir + "\\SQLscripts.xml")) {
            System.out.println("The file SQLscripts.xml is not available in: " + xmlDir);
            System.out.println("SQL Parallel Executer stops.");
            System.exit(0);
        }
        String[] SQLscripts = loadSqlScripts(xmlDir);
        Document doc = getDocument(xmlDir + "\\scripts.xml");
        Element root = doc.getDocumentElement();
        Element sqlScript = (Element) root.getFirstChild();
        ArrayList<Script> scripts = new ArrayList<Script>();
        while (sqlScript != null) {
            SQLScript s = getScript(sqlScript);
            sqlScript = (Element) sqlScript.getNextSibling();
            scripts.add(new Script(SQLscripts[s.getSqlScript()], s.getDSN(), s.getName(), s.getDayOfWeek(), s.getStaticParameter(), s.getDynamicParameter(), s.getDynamicParameterReference(), testing, s.getDepends()));
        }
        ArrayList<Thread> tScripts = new ArrayList<Thread>();
        for (Runnable e : scripts) tScripts.add(new Thread(e));
        new Thread(new SqlExecuterConsole(scripts)).start();
        for (Thread e : tScripts) {
            e.start();
            if (!parallel) {
                try {
                    e.join();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        for (Thread e : tScripts) {
            try {
                e.join();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
        System.out.println("All jobs have been completed.");
        log.info("SqlParallelExecutor completed its jobs.");
    }

    private static String[] loadSqlScripts(String xmlDir) {
        String[] SQLscripts = new String[100];
        Document doc = getDocument(xmlDir + "\\SQLscripts.xml");
        Element root = doc.getDocumentElement();
        Element sqlScript = (Element) root.getFirstChild();
        while (sqlScript != null) {
            try {
                String sqlVersion = sqlScript.getFirstChild().getFirstChild().getNodeValue();
                int version = Integer.parseInt(sqlVersion);
                String SQL = sqlScript.getFirstChild().getNextSibling().getFirstChild().getNodeValue();
                if (version < 100) {
                    SQLscripts[version] = SQL;
                } else {
                    System.out.println("Script version " + sqlVersion + " will be excluded.");
                    System.out.println("Script version number expected to be integer between 0 and 99.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Script version expected to be a number! SqlParallelExecuter stops.");
                System.exit(0);
            }
            sqlScript = (Element) sqlScript.getNextSibling();
        }
        return SQLscripts;
    }

    private static boolean CheckFileExists(String fle) {
        File file = new File(fle);
        boolean exists = file.exists();
        if (!exists) {
            return false;
        } else {
            return true;
        }
    }

    private static Document getDocument(String name) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringComments(true);
            factory.setIgnoringElementContentWhitespace(true);
            factory.setValidating(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(name));
        } catch (Exception e) {
            System.out.println("The xml file " + name + " couldn't be processed. Read further for details.");
            System.out.println(e.getMessage());
            System.exit(0);
        }
        return null;
    }

    private static SQLScript getScript(Element e) {
        String sqlName = e.getFirstChild().getFirstChild().getNodeValue();
        String sqlDepends = e.getFirstChild().getNextSibling().getFirstChild().getNodeValue();
        String sqlDSN = e.getFirstChild().getNextSibling().getNextSibling().getFirstChild().getNodeValue();
        String sqlScript = e.getFirstChild().getNextSibling().getNextSibling().getNextSibling().getFirstChild().getNodeValue();
        String sqlDayOfWeek = e.getFirstChild().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getFirstChild().getNodeValue();
        String sqlStaticParameter = e.getFirstChild().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getFirstChild().getNodeValue();
        String sqlDynamicParameter = e.getFirstChild().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getFirstChild().getNodeValue();
        Element sqlDynParam = (Element) e.getFirstChild().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getNextSibling();
        String sqlDynamicParameterReference = sqlDynParam.getAttribute("referenceDate");
        SQLScript s = new SQLScript(sqlScript.trim(), sqlDSN.trim(), sqlName.trim(), sqlDayOfWeek.trim(), sqlStaticParameter.trim(), sqlDynamicParameter.trim(), sqlDynamicParameterReference.trim(), sqlDepends.trim());
        return s;
    }

    private static class SQLScript {

        private String sqlScript;

        private String dsn;

        private String name;

        private String depends;

        private String dayOfWeek;

        private String staticParameter;

        private String dynamicParameter;

        private String dynamicParameterReference;

        public SQLScript(String sqlScript, String dsn, String name, String dayOfWeek, String staticParameter, String dynamicParameter, String dynamicParameterReference, String depends) {
            setSqlScript(sqlScript);
            setDSN(dsn);
            setName(name);
            setDayOfWeek(dayOfWeek);
            setStaticParameter(staticParameter);
            setDynamicParameter(dynamicParameter);
            setDynamicParameterReference(dynamicParameterReference);
            setDepends(depends);
        }

        public void setName(String n) {
            name = n;
        }

        public String getName() {
            return name;
        }

        public void setDSN(String d) {
            dsn = d;
        }

        public String getDSN() {
            return dsn;
        }

        public void setSqlScript(String sqlScript) {
            this.sqlScript = sqlScript;
        }

        public int getSqlScript() {
            try {
                int version = Integer.parseInt(sqlScript);
                return version;
            } catch (NumberFormatException e) {
                System.out.println("The script number in the file scripts.xml is not correct. SQLExecuter stops.");
                System.exit(0);
            }
            return 100;
        }

        public void setDayOfWeek(String dayOfWeek) {
            this.dayOfWeek = dayOfWeek;
        }

        public String getDayOfWeek() {
            return dayOfWeek;
        }

        public void setDynamicParameter(String dynamicParameter) {
            this.dynamicParameter = dynamicParameter;
        }

        public String getDynamicParameter() {
            return dynamicParameter;
        }

        public void setStaticParameter(String staticParameter) {
            this.staticParameter = staticParameter;
        }

        public String getStaticParameter() {
            return staticParameter;
        }

        public void setDynamicParameterReference(String dynamicParameterReference) {
            this.dynamicParameterReference = dynamicParameterReference;
        }

        public String getDynamicParameterReference() {
            return dynamicParameterReference;
        }

        public String getDepends() {
            return depends;
        }

        public void setDepends(String depends) {
            this.depends = depends;
        }
    }
}
