package com.medsol.tasks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;
import com.medsol.common.DBConnection;
import com.medsol.common.util.DateUtil;
import com.medsol.common.util.PropertiesUtil;

/**
 * Task Class written to call the procedure to create the load files.
 * Then migrate the data from Master Machine to Slave machine. 
 * @author Vinay.Puthenveettil
 *
 */
public class DailyDataMigrationTask {

    private static final String INCRDATA_SQL_FILE = "copyincrdata.sql";

    private static final String INCRDATA_BAT_FILE = "copyincrdata.bat";

    private static Logger logger = Logger.getLogger(DailyDataMigrationTask.class.getName());

    private static final String BASE_DIR = PropertiesUtil.getMainProperty("medsol.incr.data.basedir", "D:/MedSol/Database/Backup/DBCopy/");

    private static final String ARCHIVE_BASE_DIR = PropertiesUtil.getMainProperty("medsol.incr.data.archivebasedir", "D:/MedSol/Database/Backup/Archive/");

    private static final String FILE_SEPARATOR = "/";

    private static final String INCR_DATA_DIR = PropertiesUtil.getMainProperty("medsol.incr.data.incrdatadir", "incrdata/");

    private static final String FOLDER_DATE_FMT = "dd_MM_yyyy__HH_mm_ss";

    private static String slvHostNames = PropertiesUtil.getMainProperty("jdbc.slv.hostnames", "sd,sdv");

    /**
	 * This method takes the master DB connection and will call the incremental
	 * load data back up file creation procedure
	 * Once the load files are created, it will upload the same to the slaves data base
	 * Once the same is done it will delete the load file so that next time the procedure is called
	 * it will get new data.
	 */
    public void performDailyDataCopy() {
        if (!"false".equalsIgnoreCase(PropertiesUtil.getMainProperty("medsol.incr.back.application.context", "false"))) {
            try {
                logger.info("Daily Data Migration Started");
                confirmSlaveServersOnline();
                callDailyProcForSchemasInMasterDB();
                archiveAllFilesAfterSuccessfulLoad();
                logger.info("Daily Data Migration Finished without any exceptions");
            } catch (Exception e) {
                logger.error("Exception while calling daily procedure", e);
            }
        }
    }

    /**
	 * Method which will check whether the slave servers are available in the network
	 */
    private void confirmSlaveServersOnline() {
        logger.info("Checking whether slave servers are online");
        while (!isSlaveServersOnline()) {
            logger.info("Slave servers are not yet online");
        }
        logger.info("All slave servers are online");
    }

    private boolean isSlaveServersOnline() {
        if (StringUtils.hasText(slvHostNames)) {
            for (StringTokenizer tokenizer = new StringTokenizer(slvHostNames, ","); tokenizer.hasMoreTokens(); ) {
                String slvHostName = (String) tokenizer.nextElement();
                Process process;
                try {
                    process = Runtime.getRuntime().exec("ping " + slvHostName);
                    BufferedReader inputStream = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    for (String outptFromPrc = inputStream.readLine(); (outptFromPrc = inputStream.readLine()) != null; ) {
                        logger.info(outptFromPrc);
                    }
                    BufferedReader errorInputStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    for (String outptFromPrc = errorInputStream.readLine(); (outptFromPrc = errorInputStream.readLine()) != null; ) {
                        logger.info(outptFromPrc);
                    }
                    if (process.exitValue() != 0) {
                        return false;
                    }
                } catch (IOException e) {
                    logger.error("error while pinging", e);
                }
            }
        }
        return true;
    }

    private void copyDataToSlaveDB(String schemaName) throws IOException {
        File incrDataCopyDataDir = new File(BASE_DIR + schemaName + FILE_SEPARATOR + INCR_DATA_DIR);
        String incrDataSQLFile = BASE_DIR + schemaName + FILE_SEPARATOR + INCR_DATA_DIR + INCRDATA_SQL_FILE;
        if (incrDataCopyDataDir != null && incrDataCopyDataDir.listFiles() != null && incrDataCopyDataDir.listFiles().length > 0) {
            BufferedWriter incrDataCopySQLWriter = new BufferedWriter(new FileWriter(incrDataSQLFile));
            addMySqlSlaveConfScript(incrDataCopySQLWriter, schemaName);
            addIncrDataCopySpecificScript(incrDataCopySQLWriter, new File(BASE_DIR + schemaName + FILE_SEPARATOR + INCR_DATA_DIR));
            addMySqlConfRevertScript(incrDataCopySQLWriter);
            incrDataCopySQLWriter.close();
            loadDataToSlaveDBs(schemaName, incrDataSQLFile);
        }
    }

    private void loadDataToSlaveDBs(String schemaName, String incrDataSQLFile) throws IOException {
        if (StringUtils.hasText(slvHostNames)) {
            for (StringTokenizer tokenizer = new StringTokenizer(slvHostNames, ","); tokenizer.hasMoreTokens(); ) {
                String slvHostName = (String) tokenizer.nextElement();
                String incrDataBatFile = BASE_DIR + schemaName + FILE_SEPARATOR + INCR_DATA_DIR + slvHostName + INCRDATA_BAT_FILE;
                BufferedWriter incrDataCopyBatWriter = new BufferedWriter(new FileWriter(incrDataBatFile));
                addMySqlCallBatScript(incrDataCopyBatWriter, schemaName, incrDataSQLFile, slvHostName);
                incrDataCopyBatWriter.close();
                callExceutableBatFileToLoadDataToSlave(incrDataBatFile);
            }
        }
    }

    private void archiveAllFilesAfterSuccessfulLoad() {
        File baseDir = new File(BASE_DIR);
        File dest = new File(ARCHIVE_BASE_DIR + DateUtil.convertToString(new Date(), FOLDER_DATE_FMT));
        dest.mkdirs();
        if (baseDir.renameTo(new File(dest, baseDir.getName()))) {
            logger.info("Archiving successfull");
        } else {
            logger.error("Error in archiving!!!!");
        }
    }

    private void callExceutableBatFileToLoadDataToSlave(String batFile) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(batFile);
        boolean exceptionOccured = false;
        BufferedReader inputStream = new BufferedReader(new InputStreamReader(process.getInputStream()));
        for (String outptFromPrc = inputStream.readLine(); (outptFromPrc = inputStream.readLine()) != null; ) {
            logger.info("Output from the command execution input for bat file" + outptFromPrc);
        }
        BufferedReader errorInputStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        for (String outptFromPrc = errorInputStream.readLine(); (outptFromPrc = errorInputStream.readLine()) != null; ) {
            logger.error("Exception while calling bat file " + batFile + " : " + outptFromPrc);
            exceptionOccured = true;
        }
        if (exceptionOccured) {
            throw new IOException("Exception while calling bat file" + batFile);
        }
    }

    private void addMySqlCallBatScript(BufferedWriter fullDataCopyBatWriter, String schemaName, String sqlFile, String slvHostName) throws IOException {
        String userName = PropertiesUtil.getMainProperty("jdbc.slv.username", "root");
        String pwd = PropertiesUtil.getMainProperty("jdbc.slv.password", "welcome");
        fullDataCopyBatWriter.write("mysql --local-infile=1 -u " + userName + " -p" + pwd + " -h" + slvHostName + " " + schemaName + " < " + sqlFile);
        fullDataCopyBatWriter.newLine();
    }

    private void addIncrDataCopySpecificScript(BufferedWriter incrDataCopyBatWriter, File file) throws IOException {
        if (file.isDirectory() && file.listFiles() != null) {
            for (int i = 0; i < file.listFiles().length; i++) {
                File fullDataLoadFile = file.listFiles()[i];
                if (fullDataLoadFile.getName().indexOf(".") == -1) {
                    incrDataCopyBatWriter.write("load data local infile '" + fullDataLoadFile.getAbsolutePath().replaceAll("\\\\+", "/") + "' into table " + fullDataLoadFile.getName() + ";");
                    incrDataCopyBatWriter.newLine();
                }
            }
        }
    }

    private void addMySqlConfRevertScript(BufferedWriter dataCopyBatWriter) throws IOException {
        dataCopyBatWriter.write("SET FOREIGN_KEY_CHECKS=1;");
        dataCopyBatWriter.newLine();
    }

    private void addMySqlSlaveConfScript(BufferedWriter dataCopyBatWriter, String schemaName) throws IOException {
        dataCopyBatWriter.write("SET FOREIGN_KEY_CHECKS=0;");
        dataCopyBatWriter.newLine();
    }

    /**
	 * Method which will call the procedure in one of the master databases where the procedure exits.
	 * It gets the databases that needs to be copied from main properties.
	 * Gets the directory in which the incremental data load files need to be stored.
	 * Calls the procedure with schema name and base directory.
	 * This procedure will create load data infiles in the master database machine with subdirectories as
	 * fulldata - which contains table data that needs to be restored completely
	 * incrdata - which contains only incremental data for tables which needs to be restored. 
	 * @throws Exception 
	 */
    private void callDailyProcForSchemasInMasterDB() throws Exception {
        DBConnection conn = null;
        try {
            conn = new DBConnection();
            String masterDatabases = PropertiesUtil.getMainProperty("medsol.incr.data.master.databases", "aanda,hindustan,mathilakam,mathilakampharma");
            if (StringUtils.hasText(masterDatabases)) {
                for (StringTokenizer tokenizer = new StringTokenizer(masterDatabases, ","); tokenizer.hasMoreTokens(); ) {
                    String schemaName = (String) tokenizer.nextElement();
                    if (isPreviousDataLoadSuccess(schemaName)) {
                        CallableStatement cs = conn.getConnection().prepareCall("{call " + PropertiesUtil.getMainProperty("medsol.load.data.proc", "createtableloadfiles") + " (?,?)}");
                        cs.setString(1, schemaName);
                        cs.setString(2, BASE_DIR);
                        cs.execute();
                        cs.close();
                        copyDataToSlaveDB(schemaName);
                    } else {
                        logger.error("Previous data load not successful for database : " + schemaName);
                    }
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Exception while closing DB Connection", e);
                }
            }
        }
    }

    private boolean isPreviousDataLoadSuccess(String schemaName) {
        File incrDataDir = new File(BASE_DIR + schemaName + FILE_SEPARATOR + INCR_DATA_DIR);
        if (checkIfDirEmpty(incrDataDir)) {
            return true;
        }
        return false;
    }

    private boolean checkIfDirEmpty(File file) {
        if (file.list() == null) {
            file.mkdirs();
        }
        if (file.list().length == 0) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        DailyDataMigrationTask task = new DailyDataMigrationTask();
        task.performDailyDataCopy();
    }
}
