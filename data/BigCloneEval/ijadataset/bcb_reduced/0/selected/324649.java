package com.joe.security.pgp;

import java.sql.*;
import java.io.*;
import java.lang.Object;
import java.util.*;
import javax.naming.*;

class e4p_PgpEncryption {

    private String userid = "";

    private String fileName;

    private String filePath;

    private Runtime rt;

    private Process processCmd;

    private String batchId;

    private String processFile;

    private String successString;

    private int fileNotFoundErr;

    private int fileExistErr;

    private int ioExceptionErr;

    private String pgpReturnMsg;

    private int fileEncrytionFlag;

    private int unableToAdduserid;

    private int keyNotSigned;

    private int pgpDbConnectionError;

    private int databaseUpdationErr;

    private int directoryErr;

    private String errMsg;

    private int encryptedFile = 0;

    private String keyErrString;

    private int keyErrFlag;

    private int userIdNotmention;

    private String rec_id = "";

    e4p_PgpEncryption() {
    }

    public void setInitialValues(String ffName, String ffPath, String batidd, String publickeyy, String recid) {
        int i = 0;
        pgpReturnMsg = "";
        encryptedFile = 0;
        errMsg = "";
        fileName = ffName;
        filePath = ffPath;
        batchId = batidd;
        userid = publickeyy;
        rec_id = recid;
        processFile = "";
        successString = "";
        keyErrString = "";
    }

    public void setFlagValues() {
        fileNotFoundErr = 0;
        fileExistErr = 0;
        ioExceptionErr = 0;
        fileEncrytionFlag = 0;
        pgpDbConnectionError = 0;
        databaseUpdationErr = 0;
        directoryErr = 0;
        keyErrFlag = 0;
        userIdNotmention = 0;
        unableToAdduserid = 0;
        keyNotSigned = 0;
    }

    /********************
	 * update the file status after encryption in batchfiles_detail table
	 *
	 *
	 ****************************/
    public void updateDataBase() {
        int dbstat = 0;
        int filestat = 2;
        int row = 1;
        try {
            if (e4p_ConfigurationLoader.conn.isClosed() == true) {
                Exception ee = new Exception();
                dbstat = e4p_DatabaseConnectionChecker.invokeDBChecker(this, 0, ee);
            }
            if (dbstat == 1 && e4p_ConfigurationLoader.mainlSentFlag == 0) {
            } else {
                Statement stmt = e4p_ConfigurationLoader.conn.createStatement();
                row = stmt.executeUpdate("update batchfile_details SET file_status='" + filestat + "' where  batch_id='" + batchId + "'");
                if (row == 0) databaseUpdationErr = 1; else databaseUpdationErr = 0;
                stmt.close();
            }
        } catch (Exception E) {
            databaseUpdationErr = 1;
            dbstat = e4p_DatabaseConnectionChecker.invokeDBChecker(this, 0, E);
        }
    }

    /*********************
	 *
	 * method used to compose the response from pgp encryption process
	 *
	 *
	 ************************/
    public void pgpMessaging(String message) {
        if (ioExceptionErr == 1) {
            pgpReturnMsg = "-13" + "Encryption  Error: IO Exception occured";
        } else if (fileNotFoundErr == 1) {
            pgpReturnMsg = "-14" + fileName + " Encryption Error: File/path does not exist";
            String mailSubject = "Error:File not found for decryption";
            String mailContent = processFile + "does not exits for Encryption process";
            e4p_MailSender.send(mailSubject, mailContent);
        } else if (pgpDbConnectionError == 1) {
            pgpReturnMsg = "-15" + "Pgp e4p_Database connection error";
        } else if (databaseUpdationErr == 1) {
            pgpReturnMsg = "-16" + "Error while updating dataBase in Pgp";
        } else if (fileEncrytionFlag == 1) {
            pgpReturnMsg = "1" + fileName + " Encrypted SuccessFully;";
        } else if (directoryErr == 1) {
            pgpReturnMsg = "-17 " + filePath + " Directory is not valid";
            String mailSubject = "Error:Directory does not exist";
            String mailContent = "Directory: " + filePath + " is not a valid Directory";
            e4p_MailSender.send(mailSubject, mailContent);
        } else if (keyErrFlag == 1) {
            pgpReturnMsg = "-18 " + keyErrString;
            String mailSubject = "Error:Public key not Matching";
            String mailContent = keyErrString + " for encrypting file " + processFile;
            e4p_MailSender.send(mailSubject, mailContent);
        } else if (userIdNotmention == 1) {
            pgpReturnMsg = "-19" + " user id not mentioned or null userid";
        } else if (unableToAdduserid == 1) {
            pgpReturnMsg = "-20" + message;
        } else if (keyNotSigned == 1) {
            pgpReturnMsg = "-21" + userid + " is not signed key id";
            String mailSubject = "Error: " + userid + " is not signed id";
            ;
            String mailContent = userid + " is not signed id unable to encrypt file " + processFile;
            e4p_MailSender.send(mailSubject, mailContent);
        }
    }

    /*******************
	 * success string for checking the response from pgp
	 *
	 *
	 **********************/
    public void makeSuccessString() {
        successString = "Transport armor file:" + " " + processFile + ".asc";
        System.out.println("Print success string:" + successString);
    }

    /********
	 * message for checking the error 
	 *
	 *
	 *********************/
    public void makePublicErrorString() {
        keyErrString = "Cannot find the public key matching userid '" + userid + "'";
    }

    /****************
	 *
	 * used for composing filename used for encryption process with caring space problem
	 *
	 ****************/
    public void setNamePath() {
        processFile = filePath + "/" + fileName;
        makeSuccessString();
        System.out.println("Path:" + processFile);
        char cc = '"';
        StringBuffer ss = new StringBuffer("");
        ss.append(cc);
        ss.append(processFile);
        ss.append(cc);
        processFile = ss.toString();
    }

    /**************
	 * ///actually encypting the file and track success message from Pgp
	 * execute the pgp from  command line
	 *
	 *
	 **********/
    public void doEncryption() {
        rt = Runtime.getRuntime();
        String makePgpString = "";
        makePgpString = "pgp +pubring=" + e4p_ConfigurationLoader.pubringPath + " +batchmode -ea " + processFile + " " + userid;
        String ls_str = "";
        BufferedReader buffer;
        try {
            processCmd = rt.exec(makePgpString);
            buffer = new BufferedReader(new InputStreamReader(processCmd.getInputStream()));
            ls_str = buffer.readLine();
            int countLine = 0;
            System.out.println("Start printl=ing;");
            while (ls_str != null) {
                System.out.println(ls_str);
                countLine++;
                if (ls_str.equals(successString)) {
                    fileEncrytionFlag = 1;
                }
                if (ls_str.equals(keyErrString)) {
                    keyErrFlag = 1;
                }
                ls_str = buffer.readLine();
            }
            if (countLine == 4) {
                fileNotFoundErr = 1;
            }
            if (countLine >= 15) {
                keyNotSigned = 1;
            }
            buffer.close();
            processCmd.destroy();
            System.out.println("********The total count lines are:" + countLine);
        } catch (Exception e) {
            e.printStackTrace();
            ioExceptionErr = 1;
        }
    }

    /**************
	 * check the file is already in encrypted format if there then delete
	 *
	 *
	 *
	 ************/
    public void checkFileExist() {
        int len = 0;
        String checkFilename = fileName + ".asc";
        int i = 0;
        File checkfile[];
        File dir = new File(filePath);
        if (dir.isDirectory() == true) {
            checkfile = dir.listFiles();
            len = checkfile.length;
            String tmpname = "";
            for (i = 0; i < len; i++) {
                tmpname = checkfile[i].getName().toString();
                if (checkFilename.equals(tmpname)) {
                    fileExistErr = 1;
                    if (checkfile[i].delete()) {
                        System.out.println(tmpname + " deleted");
                    }
                    break;
                }
            }
        } else {
            directoryErr = 1;
        }
    }

    /************
	 * update the new user id added in the keyring to edi_misc table
	 *
	 *
	 *******************/
    public void updateUserid() {
        int dbstat = 0;
        try {
            if (e4p_ConfigurationLoader.conn.isClosed() == true) {
                Exception ee = new Exception();
                dbstat = e4p_DatabaseConnectionChecker.invokeDBChecker(this, 0, ee);
            }
            if (dbstat == 1 && e4p_ConfigurationLoader.mainlSentFlag == 0) {
            } else {
                String keepnull = "";
                Statement stmt = e4p_ConfigurationLoader.conn.createStatement();
                stmt.executeUpdate("update edi_misc SET enc_id='" + userid + "',enc_publickeypath='" + keepnull + "' where rec_id='" + rec_id + "' ");
                stmt.close();
            }
        } catch (Exception ee) {
            dbstat = e4p_DatabaseConnectionChecker.invokeDBChecker(this, 0, ee);
        }
    }

    public void checkConnection() {
        try {
            if (e4p_ConfigurationLoader.conn.isClosed() == true) {
                e4p_ConfigurationLoader.setDataBaseConnection();
            }
        } catch (Exception ee) {
            e4p_ConfigurationLoader.setDataBaseConnection();
        }
    }

    /******************
	 * pgp encryption process entry point
	 * call encryption process
	 * synchronize the message 
	 *
	 *
	 *************/
    public String pgpEncryptionStarter(String fName, String fPath, String batid, String publickey, String importPath, String reccid) {
        setInitialValues(fName, fPath, batid, publickey, reccid);
        e4p_PgpkeyImport pgpImport = new e4p_PgpkeyImport();
        String response = pgpImport.keyImportStarter(importPath);
        if (response.charAt(0) != '1') {
            String colectRes = response.substring(2, response.length());
            unableToAdduserid = 1;
            pgpMessaging(colectRes);
        } else {
            response = response.substring(1, response.length());
            int indexval = response.indexOf("$");
            if (indexval == -1) {
            } else {
                String extractid = response.substring(indexval + 2, response.length() - 1);
                userid = extractid;
                updateUserid();
            }
            setFlagValues();
            if (filePath.startsWith("/") == false) {
                filePath = e4p_ConfigurationLoader.relative_path + "/" + filePath;
            } else {
                filePath = e4p_ConfigurationLoader.relative_path + filePath;
            }
            checkFileExist();
            if (directoryErr == 0) {
                setNamePath();
                makePublicErrorString();
                if (userid.equals("") || userid == null) {
                    userIdNotmention = 1;
                    pgpMessaging("");
                } else {
                    char cc = '"';
                    StringBuffer ss = new StringBuffer("");
                    ss.append(cc);
                    ss.append(userid);
                    ss.append(cc);
                    userid = ss.toString();
                    doEncryption();
                }
            } else {
                pgpMessaging("");
            }
            if (fileNotFoundErr == 1) {
                pgpMessaging("");
            }
            if (ioExceptionErr == 1) {
                pgpMessaging("");
            }
            if (keyErrFlag == 1) {
                pgpMessaging("");
            }
            if (keyNotSigned == 1) {
                pgpMessaging("");
            }
            if (fileEncrytionFlag == 1) {
                pgpMessaging("");
                updateDataBase();
                if (databaseUpdationErr == 1) {
                    pgpMessaging("");
                }
            }
        }
        System.out.println("Pgp message :" + pgpReturnMsg);
        return pgpReturnMsg;
    }
}
