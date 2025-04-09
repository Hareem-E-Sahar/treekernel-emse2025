package pt.gotham.gardenia.desktop;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;
import com.izforge.izpack.util.AbstractUIProcessHandler;

/**
 *  Description of the Class
 *
 * @author     appj
 * @created    September 5, 2005
 */
public class GardeniaDesktopSetup {

    /**
     *  The main program for the GardeniaDesktopSetup class
     *
     * @param  args           The command line arguments
     * @exception  Exception  Description of the Exception
     */
    public static void main(String[] args) throws Exception {
        GardeniaDesktopSetup.doInitialSetup(args[0], args[1]);
    }

    /**
     *  Gets the randomString attribute of the GardeniaDesktopSetup class
     *
     * @param  strlen         Description of the Parameter
     * @return                The randomString value
     * @exception  Exception  Description of the Exception
     */
    public static String getRandomString(int strlen) throws Exception {
        long rnd = Math.round(Math.random() * Math.pow(10, strlen));
        return "" + rnd;
    }

    /**
     *  Description of the Method
     *
     * @param  myKv           Description of the Parameter
     * @param  file           Description of the Parameter
     * @exception  Exception  Description of the Exception
     */
    public static void replaceVarsInFile(HashMap myKv, String file) throws Exception {
        GardeniaDesktopSetup.replaceVarsInFile(myKv, file, file);
    }

    /**
     *  Description of the Method
     *
     * @param  myKv           Description of the Parameter
     * @param  fileIn         Description of the Parameter
     * @param  fileOut        Description of the Parameter
     * @exception  Exception  Description of the Exception
     */
    public static void replaceVarsInFile(HashMap myKv, String fileIn, String fileOut) throws Exception {
        StringBuffer fileOutStr = new StringBuffer("");
        BufferedReader in = new BufferedReader(new FileReader(fileIn));
        String line = "";
        while ((line = in.readLine()) != null) {
            Iterator it = myKv.keySet().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                line = line.replaceAll(key, (String) myKv.get(key));
            }
            fileOutStr.append(line + "\n");
        }
        in.close();
        FileWriter out = new FileWriter(fileOut);
        out.write(fileOutStr.toString());
        out.flush();
        out.close();
        return;
    }

    public static void executeSQLScript(Connection conn, String fileName) throws Exception {
        BufferedReader in = new BufferedReader(new FileReader(fileName));
        String line = "";
        Statement stmt = conn.createStatement();
        while ((line = in.readLine()) != null) {
            line = line.trim();
            if (!"".equals(line)) {
                stmt.execute(line);
            }
        }
        stmt.close();
        in.close();
        return;
    }

    /**
     *  Description of the Method
     *
     * @param  gardeniaHome   Description of the Parameter
     * @exception  Exception  Description of the Exception
     */
    public static void doInitialSetup(String gardeniaHome, String lang) throws Exception {
        if ("eng".equalsIgnoreCase(lang)) {
            lang = "en";
        } else if ("prt".equalsIgnoreCase(lang)) {
            lang = "pt";
        } else if ("por".equalsIgnoreCase(lang)) {
            lang = "pt";
        } else {
            lang = "en";
        }
        gardeniaHome = '\\' == File.separatorChar ? gardeniaHome.replace('\\', '/') : gardeniaHome;
        gardeniaHome = gardeniaHome.endsWith("/") ? gardeniaHome : gardeniaHome + "/";
        String jdbcOldUrl = "jdbc:hsqldb:file:" + gardeniaHome + "data/db/gardenia01";
        String jdbcOldPass = "";
        String jdbcDriver = "org.hsqldb.jdbcDriver";
        String jdbcUrl = "jdbc:hsqldb:hsql://127.0.0.1:9001/gardenia01";
        String jdbcUser = "sa";
        String jdbcPass = getRandomString(10);
        String docReportDataDir = gardeniaHome + "data/document/";
        String filesDataDir = gardeniaHome + "data/files/";
        String docReportDefDir = gardeniaHome + "webapps/gardenia/WEB-INF/reports/document/";
        String genReportDefDir = gardeniaHome + "webapps/gardenia/WEB-INF/reports/general/";
        Class.forName(jdbcDriver);
        Connection conn = DriverManager.getConnection(jdbcOldUrl, jdbcUser, jdbcOldPass);
        Statement stmt = conn.createStatement();
        executeSQLScript(conn, gardeniaHome + "tmp/install/gardenia-baseline." + lang + ".sql");
        stmt.executeUpdate("update tbl_Parameter set ParameterValue='" + docReportDataDir + "' where ParameterID='DocumentReportDataDir'");
        stmt.executeUpdate("update tbl_Parameter set ParameterValue='" + docReportDefDir + "' where ParameterID='DocumentReportDefDir'");
        stmt.executeUpdate("update tbl_Parameter set ParameterValue='" + genReportDefDir + "' where ParameterID='GeneralReportDefDir'");
        stmt.executeUpdate("update tbl_Parameter set ParameterValue='" + lang + "' where ParameterID='DefaultLocale'");
        stmt.execute("set password '" + jdbcPass + "'");
        stmt.execute("COMMIT");
        stmt.execute("CHECKPOINT");
        stmt.close();
        conn.close();
        HashMap keyValues = new HashMap();
        keyValues.put("__GARDENIA_HOME__", gardeniaHome);
        keyValues.put("__GARDENIA_JDBC_DRIVER__", jdbcDriver);
        keyValues.put("__GARDENIA_JDBC_URL__", jdbcUrl);
        keyValues.put("__GARDENIA_JDBC_USER__", jdbcUser);
        keyValues.put("__GARDENIA_JDBC_PASS__", jdbcPass);
        keyValues.put("__GARDENIA_FILES_DATA_DIR__", filesDataDir);
        keyValues.put("__GARDENIA_LOCALE__", lang);
        replaceVarsInFile(keyValues, gardeniaHome + "webapps/gardenia/WEB-INF/log4j.properties");
        replaceVarsInFile(keyValues, gardeniaHome + "webapps/gardenia/WEB-INF/dbforms-config.xml");
        replaceVarsInFile(keyValues, gardeniaHome + "webapps/gardenia/WEB-INF/jetty-realm.properties");
        replaceVarsInFile(keyValues, gardeniaHome + "webapps/gardenia/WEB-INF/jetty-web.xml.template", gardeniaHome + "webapps/gardenia/WEB-INF/jetty-web.xml");
        replaceVarsInFile(keyValues, gardeniaHome + "webapps/gardenia/META-INF/context.xml.template", gardeniaHome + "webapps/gardenia/META-INF/context.xml");
        replaceVarsInFile(keyValues, gardeniaHome + "etc/gardenia.properties");
        replaceVarsInFile(keyValues, gardeniaHome + "etc/webserver.xml");
        replaceVarsInFile(keyValues, gardeniaHome + "etc/log4j.properties");
    }

    public static List getFilesRecursive(File basePath) {
        return getFilesRecursive(basePath, null);
    }

    public static List getFilesRecursive(File basePath, List result) {
        if (result == null) result = new ArrayList();
        File[] files = basePath.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    getFilesRecursive(files[i], result);
                } else {
                    result.add(files[i]);
                }
            }
        }
        return result;
    }

    /**
     *  Backups a Gardenia Desktop Edition Data to a .zip file
     *
     * @param  handler        Description of the Parameter
     * @param  args           Description of the Parameter
     * @exception  Exception  Description of the Exception
     */
    public static void backupGardenia(String gardeniaHome, String backupFile) throws Exception {
        gardeniaHome = '\\' == File.separatorChar ? gardeniaHome.replace('\\', '/') : gardeniaHome;
        gardeniaHome = gardeniaHome.endsWith("/") ? gardeniaHome : gardeniaHome + "/";
        List allToZip = getFilesRecursive(new File(gardeniaHome + "data"));
        byte[] buffer = new byte[4096];
        FileOutputStream out = new FileOutputStream(backupFile);
        ZipOutputStream zout = new ZipOutputStream(out);
        zout.setLevel(9);
        int baseNameSize = gardeniaHome.length();
        for (Iterator it = allToZip.iterator(); it.hasNext(); ) {
            File f = (File) it.next();
            if (f.getName().equals("gardenia01.lck")) continue;
            ZipEntry zipEntry = new ZipEntry(f.getCanonicalPath().substring(baseNameSize));
            zipEntry.setTime(f.lastModified());
            zout.putNextEntry(zipEntry);
            FileInputStream fis = new FileInputStream(f);
            int bytes = 0;
            while ((bytes = fis.read(buffer, 0, buffer.length)) > 0) {
                zout.write(buffer, 0, bytes);
            }
            fis.close();
        }
        zout.close();
        out.close();
    }

    /**
     *  Restores a Gardenia Desktop Edition Data to a .zip file
     *
     * @param  handler        Description of the Parameter
     * @param  args           Description of the Parameter
     * @exception  Exception  Description of the Exception
     */
    public static void restoreGardenia(String gardeniaHome, String backupFile) throws Exception {
        gardeniaHome = '\\' == File.separatorChar ? gardeniaHome.replace('\\', '/') : gardeniaHome;
        gardeniaHome = gardeniaHome.endsWith("/") ? gardeniaHome : gardeniaHome + "/";
        FileInputStream propsIn = new FileInputStream(gardeniaHome + "etc/gardenia.properties");
        Properties props = new Properties();
        props.load(propsIn);
        propsIn.close();
        List allToDel = getFilesRecursive(new File(gardeniaHome + "data"));
        for (Iterator it = allToDel.iterator(); it.hasNext(); ) {
            File f = (File) it.next();
            f.delete();
        }
        (new File(gardeniaHome + "data/db")).mkdirs();
        (new File(gardeniaHome + "data/document")).mkdirs();
        (new File(gardeniaHome + "data/files")).mkdirs();
        byte[] buffer = new byte[4096];
        FileInputStream in = new FileInputStream(backupFile);
        ZipInputStream zin = new ZipInputStream(in);
        ZipEntry zentry = null;
        while ((zentry = zin.getNextEntry()) != null) {
            FileOutputStream fos = new FileOutputStream(gardeniaHome + zentry.getName());
            int bytes = 0;
            while ((bytes = zin.read(buffer, 0, buffer.length)) > 0) {
                fos.write(buffer, 0, bytes);
            }
            fos.flush();
            fos.close();
        }
        zin.close();
        in.close();
        BufferedReader br = new BufferedReader(new FileReader(gardeniaHome + "data/db/gardenia01.script"));
        String line = null;
        StringBuffer strOut = new StringBuffer("");
        while ((line = br.readLine()) != null) {
            if (line.startsWith("CREATE USER SA PASSWORD")) {
                line = "CREATE USER SA PASSWORD \"" + props.getProperty("database.jdbcPass", "") + "\"";
            }
            strOut.append(line).append("\n");
        }
        br.close();
        FileWriter fw = new FileWriter(gardeniaHome + "data/db/gardenia01.script");
        fw.write(strOut.toString());
        fw.flush();
        fw.close();
    }

    /**
     *  Main processing method for the GardeniaDesktopSetup object
     *
     * @param  handler        Description of the Parameter
     * @param  args           Description of the Parameter
     * @exception  Exception  Description of the Exception
     */
    public void run(AbstractUIProcessHandler handler, String[] args) throws Exception {
        handler.logOutput("A actualizar configuracao - Updating configuration...", false);
        try {
            GardeniaDesktopSetup.doInitialSetup(args[0], args[1]);
        } catch (Exception ex) {
            handler.logOutput("ERRO - Erro ao actualizar parametros - Error updating config.: " + ex, true);
            throw ex;
        }
        handler.logOutput("OK - Actualizacao concluida com sucesso! - Update finished sucessfully!", false);
    }
}
