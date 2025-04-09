package net.zycomm.source;

import java.awt.Dimension;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.*;

/**Description : This is used to migrate the data from existing database to new database. 
 * @Author :Sruthi 
 * @Version 1.0 
 * 
 * @(#)DataMigration.java	1.5 
 *
 * Copyright 2008 Zycomm Innovations, Inc. All rights reserved.
 * Zycomm PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
public class MigrateData {

    private java.util.HashMap tableDataMap;

    /**Variable declaration for storing the tablenames in a tableName vector and those are maintained in another 
	 temporary vector i.e,in tempVec.**/
    private java.util.Vector tableName, tempVec;

    /**Variable declaration for storing the Excel File.**/
    private java.io.File file;

    /**Variable declaration for Display Panel.**/
    private JPanel _dispPanel;

    /**Variable declaration for Specifying the constraints for components that are laid on the
	 * GridBagLayout class.**/
    private GridBagConstraints gbc;

    /**It is for maintaining the y-coordinate.*/
    private int y = 0;

    private int ThrdCnt = 0;

    /**Variable declaration for remigrate button.**/
    private JButton btn;

    /**Variable declaration for Maintaining the status of the Database Migration.If it is migrated 
	 * the status will be changed as 'YES' Otherwise the migStatus is "NO". **/
    private String migStatus = "no";

    /**Variable declaration for Maintaining the old,new column names,old,new datatypes and old ,new data lengths
	 *  in a String**/
    private String oldcolnm, newcolnm, olddttype, newdttype, olddtlen, newdtlen;

    /**Variable declaration for Maintaining the old datatype in a String.**/
    private String olddata = "";

    /**Variable declaration for Source Selected Driver.**/
    private String srcseldriver = "";

    /**Variable declaration for Destination Selected Driver.**/
    private String desselcdriver = "";

    /**Variable declaration for maintaining the status either true or false.Initially f = "True".  **/
    boolean f = true;

    /**Enumeration interface generates a series of elements, one at a time. Successive calls to the nextElement
	 method return successive elements of the series.*/
    Enumeration enm;

    /**This is the Constructor <p>
     *
     * @param file the <code>file</code> associated with this
     *			<code>MigrateData</code> <p>
     *
     * @param desselcdriver the <code>desselcdriver</code> associated with this
     *			<code>MigrateData</code> <p>
     *
     * @param srcseldriver the <code>srcseldriver</code> associated with this
     *			<code>MigrateData</code> <p>
     */
    public MigrateData(java.io.File file, String desselcdriver, String srcseldriver) {
        this.file = file;
        this.desselcdriver = desselcdriver;
        this.srcseldriver = srcseldriver;
        _dispPanel = new JPanel();
        _dispPanel.setLayout(new GridBagLayout());
        _dispPanel.setPreferredSize(new Dimension(848, 300));
        gbc = new GridBagConstraints();
    }

    /**Method declaration for getting the information from Display Panel.**/
    public JPanel getdispPanel() {
        return _dispPanel;
    }

    /**
	 * This function is used to get the table structure from Excel it means we get the metadata from the Excel
	 *Here TableDet is the one of the sheet name in Excel. It is having the oldtable names list and new table
	 * names list.
	 */
    public void getTableStructure() {
        tableName = new ReadExcel().readTableName("TableDet", file);
        enm = tableName.elements();
        ThrdCnt = tableName.size();
        for (int i = 0; i < tableName.size(); ) {
            for (int j = 0; j < 3; j++) {
                try {
                    tempVec = (Vector) tableName.get(i);
                    i++;
                    new GetConn(tempVec).start();
                } catch (ArrayIndexOutOfBoundsException aobe) {
                }
            }
        }
    }

    public int getThreadCount() {
        return ThrdCnt;
    }

    class GetConn extends Thread {

        /**Variable declaration for maintaining the table data in tableDataMap Hashmap.*/
        private java.util.HashMap tableDataMap;

        /**Variable declaration for storing the old and new column vector values in oldcolNameVec,
		 and newcolNameVec.And old and new data vector values stored in oldcoldtVec,newcoldtVec.*/
        private java.util.Vector colDataVec, oldcolNameVec, newcolNameVec, newcoldtVec, oldcoldtVec, tableName, tempVec, temp;

        /**Variable declaration for maintaining the oldtablenames and newtablenames. */
        private String oldtablename, newtablename;

        GetConn(Vector vec) {
            this.tempVec = vec;
        }

        /**The run() method executes the body of a thread.*/
        public void run() {
            GetData getData;
            getData = new GetData();
            getData.getConnection();
            readTableData(tempVec, getData);
            ThrdCnt--;
            if (ThrdCnt == 0) System.out.println("everything is finished sruthi" + new Date());
        }

        /**
		 * This function is used to convert the data from OldDataBase to NewDatabase.
		 * It Means Get the new column datatypes and convert according to new data type. */
        private Vector getConvertedData(Vector oldColNmVec, Vector newColNmVec, Vector oldDttypeVec, Vector newDttypeVec, Vector oldDtlnVec, Vector newDtlnVec, Vector colDataVec) {
            Vector newDataVec = new Vector();
            int length = newColNmVec.size();
            Vector temp = null, tempVec;
            for (int i = 0; i < length; i++) {
                Vector h = new Vector();
                try {
                    temp = new Vector();
                    oldcolnm = oldColNmVec.get(i).toString();
                    newcolnm = newColNmVec.get(i).toString();
                    olddttype = oldDttypeVec.get(i).toString();
                    newdttype = newDttypeVec.get(i).toString();
                    String s = "";
                    for (int j = 0; j < colDataVec.size(); j++) {
                        tempVec = (Vector) colDataVec.get(j);
                        try {
                            s = tempVec.get(i).toString();
                        } catch (NullPointerException npe) {
                            s = "null";
                        }
                        temp.addElement(s);
                    }
                    if (srcseldriver.equalsIgnoreCase("com.mysql.jdbc.Driver") && desselcdriver.equalsIgnoreCase("oracle.jdbc.driver.OracleDriver")) h = convertColDataMYSQL_ORACLE(olddttype, newdttype, temp); else if (srcseldriver.equalsIgnoreCase("oracle.jdbc.driver.OracleDriver") && desselcdriver.equalsIgnoreCase("net.sourceforge.jtds.jdbc.Driver")) h = convertColDataORACLE_MSSQL(olddttype, newdttype, temp); else if (srcseldriver.equalsIgnoreCase("net.sourceforge.jtds.jdbc.Driver") && desselcdriver.equalsIgnoreCase("oracle.jdbc.driver.OracleDriver")) h = convertColDataMSSQL_ORACLE(olddttype, newdttype, temp); else if (srcseldriver.equalsIgnoreCase("net.sourceforge.jtds.jdbc.Driver") && desselcdriver.equalsIgnoreCase("net.sourceforge.jtds.jdbc.Driver")) h = convertColDataMSSQL_MSSQL(olddttype, newdttype, temp); else if (srcseldriver.equalsIgnoreCase("oracle.jdbc.driver.OracleDriver") && desselcdriver.equalsIgnoreCase("com.mysql.jdbc.Driver")) h = convertColDataOracle_MYSQL(olddttype, newdttype, temp); else if (srcseldriver.equalsIgnoreCase("oracle.jdbc.driver.OracleDriver") && desselcdriver.equalsIgnoreCase("oracle.jdbc.driver.OracleDriver")) h = convertColDataOracle_Oracle(olddttype, newdttype, temp); else if (srcseldriver.equalsIgnoreCase("com.mysql.jdbc.Driver") && desselcdriver.equalsIgnoreCase("com.mysql.jdbc.Driver")) h = convertColDataMYSQL_MYSQL(olddttype, newdttype, temp); else return null;
                    newDataVec.addElement(h);
                } catch (Exception e) {
                    for (int k = i; k < length; k++) {
                        h.addAll(fillextradata(newdttype, colDataVec.size(), oldColNmVec.size()));
                        newDataVec.addElement(h);
                    }
                }
            }
            temp = null;
            temp = new Vector();
            tempVec = new Vector();
            Vector v = new Vector();
            length = 0;
            for (int i = 0; i < newDataVec.size(); i++) {
                temp = (Vector) newDataVec.get(i);
                if (temp.size() > length) length = temp.size();
            }
            String tempStr = "";
            for (int i = 0; i < length; i++) {
                tempVec = new Vector();
                for (int j = 0; j < newDataVec.size(); j++) {
                    temp = (Vector) newDataVec.get(j);
                    try {
                        tempStr = temp.get(i).toString();
                        tempVec.addElement(tempStr);
                    } catch (NullPointerException npe) {
                    }
                }
                v.addElement(tempVec);
            }
            newDataVec = v;
            return newDataVec;
        }

        /**
		 * This function is used to fill the extradata to the new database.What ever is the old datatype,if the new
		 * datatype is number then it fill to zero.If the new datatype is varchar or date fill it with ''. 
		 * @param newdttype Maintaing the new Datatype.
		 * @param srtno - Starting Number.
		 * @param endno - Ending Number. */
        private Vector fillextradata(String newdttype, int srtno, int endno) {
            Vector v = new Vector();
            String str = "";
            for (int l = 0; l < srtno; l++) {
                if (newdttype.equalsIgnoreCase("Number")) {
                    str = "'0'";
                }
                if (newdttype.equalsIgnoreCase("varchar") || newdttype.equalsIgnoreCase("Date")) {
                    str = "''";
                }
                v.addElement(str);
            }
            return v;
        }

        /**This function is used to convert the date from old database date format to 
		 * new database date format.
		 */
        private String convertDate(String date) {
            ParsePosition p1 = new ParsePosition(0);
            SimpleDateFormat dateFormat = null;
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            java.util.Date d1 = null;
            try {
                d1 = df.parse(date);
            } catch (Exception e) {
            }
            dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            String date1 = dateFormat.format(d1);
            return date1;
        }

        private String getDateFormat(String format, String target, String rdngDate) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            if (rdngDate != null && rdngDate.length() > 1) {
                SimpleDateFormat sdftomssql;
                Date rdngdate = null;
                try {
                    sdftomssql = new SimpleDateFormat(target);
                    rdngdate = sdf.parse(rdngDate);
                    cal.setTime(rdngdate);
                    rdngDate = sdftomssql.format(cal.getTime());
                } catch (Exception e) {
                    rdngDate = "";
                }
            } else {
                rdngDate = "";
            }
            return rdngDate;
        }

        /**
		 * Migrate the data from MYSQL TO MYSQL in that case it compares 
		 * olddatatypes and newdatatypes and then replace the corresponding
		 * converted data.
		 */
        private Vector convertColDataMYSQL_MYSQL(String olddttype, String newdttype, Vector olddataVec) {
            Vector newdataVec = new Vector();
            Enumeration enm = olddataVec.elements();
            while (enm.hasMoreElements()) {
                olddata = enm.nextElement().toString();
                String newdata = "'" + olddata + "'";
                if (olddttype.equalsIgnoreCase(newdttype)) {
                    newdata = "'" + olddata + "'";
                }
                newdataVec.addElement(newdata);
            }
            return newdataVec;
        }

        /**
		 * Migrate the data from ORACLE to ORACLE in that case it compares 
		 * olddatatypes and newdatatypes and then replace the corresponding
		 * converted data.
		 */
        private Vector convertColDataOracle_Oracle(String olddttype, String newdttype, Vector olddataVec) {
            Vector newdataVec = new Vector();
            Enumeration enm = olddataVec.elements();
            while (enm.hasMoreElements()) {
                olddata = enm.nextElement().toString();
                String newdata = "'" + olddata + "'";
                if ((olddttype.equalsIgnoreCase("Varchar") && newdttype.equalsIgnoreCase("Number"))) {
                    newdata = "0";
                }
                if (olddttype.equalsIgnoreCase(newdttype)) {
                    System.out.println("varchar in oracle");
                    newdata = "'" + olddata + "'";
                }
                if ((olddttype.equalsIgnoreCase("Char") && newdttype.equalsIgnoreCase("Number"))) {
                    newdata = "0";
                }
                if ((olddttype.equalsIgnoreCase("Varchar")) && newdttype.equalsIgnoreCase("Date")) {
                    newdata = "''";
                }
                if (olddttype.equalsIgnoreCase("Char") && (newdttype.equalsIgnoreCase("Timestamp") || newdttype.equalsIgnoreCase("Date"))) {
                    newdata = "''";
                }
                if ((olddttype.equalsIgnoreCase("Timestamp") || olddttype.equalsIgnoreCase("Date")) && newdttype.equalsIgnoreCase("Number")) {
                    newdata = "0";
                }
                if ((olddttype.equalsIgnoreCase("Varchar") && newdttype.equalsIgnoreCase("Timestamp"))) {
                    newdata = "''";
                }
                if (olddttype.equalsIgnoreCase("Timestamp") && newdttype.equalsIgnoreCase("Date")) {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                    try {
                        Date d = formatter.parse(olddata);
                        formatter = new SimpleDateFormat("dd-MM-yyyy");
                        String finalDate = formatter.format(d);
                        if (olddata.contains("0000-00-00")) newdata = "''"; else newdata = "to_date('" + finalDate + "','dd-MM-yyyy')";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (olddttype.equalsIgnoreCase("Date") && newdttype.equalsIgnoreCase("Timestamp")) {
                    if (olddata.contains("0000-00-00") || olddata.equals("null") || olddata.equals("")) newdata = "''"; else {
                        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                        try {
                            Date d = formatter.parse(olddata);
                            formatter = new SimpleDateFormat("dd-MM-yyyy");
                            String finalDate = formatter.format(d);
                            newdata = "to_date('" + finalDate + "','dd-MM-yyyy')";
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (olddttype.equalsIgnoreCase("Timestamp") && newdttype.equalsIgnoreCase("Timestamp")) {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                    if (olddata.contains("0000-00-00") || olddata.equals("null") || olddata.equals("")) newdata = "''"; else {
                        try {
                            Date d = formatter.parse(olddata);
                            formatter = new SimpleDateFormat("dd-MM-yyyy");
                            String finalDate = formatter.format(d);
                            newdata = "to_date('" + finalDate + "','dd-MM-yyyy')";
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                newdataVec.addElement(newdata);
            }
            return newdataVec;
        }

        /**
		 * Migrate the data from MYSQL to ORACLE in that case it compares 
		 * olddatatypes and newdatatypes and then replace the corresponding
		 * converted data.
		 */
        private Vector convertColDataMYSQL_ORACLE(String olddttype, String newdttype, Vector olddataVec) {
            Vector newdataVec = new Vector();
            Enumeration enm = olddataVec.elements();
            while (enm.hasMoreElements()) {
                olddata = enm.nextElement().toString();
                String newdata = "'" + olddata + "'";
                if ((olddttype.equalsIgnoreCase("Float") || olddttype.equalsIgnoreCase("Integer") || olddttype.equalsIgnoreCase("bigint") || olddttype.equalsIgnoreCase("tinyint") || olddttype.equalsIgnoreCase("Double") || olddttype.equalsIgnoreCase("Varchar")) && newdttype.equalsIgnoreCase("Date")) {
                    newdata = "''";
                }
                if ((olddttype.equalsIgnoreCase("Char") && newdttype.equalsIgnoreCase("Number"))) {
                    newdata = "'0'";
                }
                if (olddttype.equalsIgnoreCase("Char") && (newdttype.equalsIgnoreCase("Timestamp") || newdttype.equalsIgnoreCase("DateTime") || newdttype.equalsIgnoreCase("Date"))) {
                    newdata = "''";
                }
                if ((olddttype.equalsIgnoreCase("DateTime") || olddttype.equalsIgnoreCase("Timestamp") || olddttype.equalsIgnoreCase("Date")) && newdttype.equalsIgnoreCase("Number")) {
                    newdata = "'0'";
                }
                if (olddttype.equalsIgnoreCase("DateTime") && newdttype.equalsIgnoreCase("Timestamp")) {
                    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        Date d = sf.parse(olddata);
                        sf = new SimpleDateFormat("dd-MM-yyyy");
                        String finalDate = sf.format(d);
                        newdata = "to_date('" + finalDate + "','dd-MM-yyyy')";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if ((olddttype.equalsIgnoreCase("Varchar") && newdttype.equalsIgnoreCase("Timestamp"))) {
                    newdata = "''";
                }
                if ((olddttype.equalsIgnoreCase("Varchar") && newdttype.equalsIgnoreCase("Number"))) {
                    newdata = "'0'";
                }
                if (olddttype.equalsIgnoreCase("Date")) {
                    if (olddata.equals("0")) {
                        newdata = "";
                    } else {
                        if (newdttype.equalsIgnoreCase("date")) {
                            olddata = olddata.replaceAll("'", "''");
                            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
                            try {
                                Date d = sf.parse(olddata);
                                sf = new SimpleDateFormat("dd-MM-yyyy");
                                String finalDate = sf.format(d);
                                if (olddata.contains("0000-00-00")) newdata = "''"; else newdata = "to_date('" + finalDate + "','dd-MM-yyyy')";
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                if (olddttype.equalsIgnoreCase(newdttype) && (!olddttype.equalsIgnoreCase("Date"))) {
                    newdata = "'" + olddata + "'";
                }
                if (olddttype.equalsIgnoreCase("DateTime") && newdttype.equalsIgnoreCase("Date")) {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        Date d = formatter.parse(olddata);
                        formatter = new SimpleDateFormat("dd-MM-yyyy");
                        String finalDate = formatter.format(d);
                        if (olddata.contains("0000-00-00")) newdata = "''"; else newdata = "to_date('" + finalDate + "','dd-MM-yyyy')";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (olddttype.equalsIgnoreCase("Time") && newdttype.equalsIgnoreCase("Date")) {
                    newdata = "''";
                }
                if (olddttype.equalsIgnoreCase("Year") && newdttype.equalsIgnoreCase("Date")) {
                    newdata = "''";
                }
                newdataVec.addElement(newdata);
            }
            return newdataVec;
        }

        /**
		 * Migrate the data from MSSQL to ORACLE in that case it compares 
		 * olddatatypes and newdatatypes and then replace the corresponding 
		 * converted data.
		 */
        private Vector convertColDataMSSQL_ORACLE(String olddttype, String newdttype, Vector olddataVec) {
            Vector newdataVec = new Vector();
            Enumeration enm = olddataVec.elements();
            while (enm.hasMoreElements()) {
                olddata = enm.nextElement().toString();
                String newdata = "'" + olddata + "'";
                if ((olddttype.equalsIgnoreCase("Float") || olddttype.equalsIgnoreCase("Int") || olddttype.equalsIgnoreCase("bigint") || olddttype.equalsIgnoreCase("smallint") || olddttype.equalsIgnoreCase("tinyint") || olddttype.equalsIgnoreCase("Decimal") || olddttype.equalsIgnoreCase("Real") || olddttype.equalsIgnoreCase("numeric") || olddttype.equalsIgnoreCase("Varchar") || olddttype.equalsIgnoreCase("nVarchar") || olddttype.equalsIgnoreCase("nchar") || olddttype.equalsIgnoreCase("char")) && newdttype.equalsIgnoreCase("Date")) {
                    newdata = "''";
                }
                if ((olddttype.equalsIgnoreCase("Float") || olddttype.equalsIgnoreCase("Int") || olddttype.equalsIgnoreCase("bigint") || olddttype.equalsIgnoreCase("smallint") || olddttype.equalsIgnoreCase("tinyint") || olddttype.equalsIgnoreCase("Decimal") || olddttype.equalsIgnoreCase("Real") || olddttype.equalsIgnoreCase("numeric") || olddttype.equalsIgnoreCase("Varchar") || olddttype.equalsIgnoreCase("nVarchar") || olddttype.equalsIgnoreCase("nchar") || olddttype.equalsIgnoreCase("char")) && newdttype.equalsIgnoreCase("Timestamp")) {
                    newdata = "''";
                }
                if ((olddttype.equalsIgnoreCase("DateTime") || olddttype.equalsIgnoreCase("smalldatetime") || olddttype.equalsIgnoreCase("Timestamp")) && newdttype.equalsIgnoreCase("Date")) {
                    newdata = "''";
                }
                if ((olddttype.equalsIgnoreCase("char") || olddttype.equalsIgnoreCase("varchar") || olddttype.equalsIgnoreCase("nchar") || olddttype.equalsIgnoreCase("nvarchar")) && newdttype.equalsIgnoreCase("number")) {
                    newdata = "0";
                }
                if ((olddttype.equalsIgnoreCase("DateTime") || olddttype.equalsIgnoreCase("smalldatetime") || olddttype.equalsIgnoreCase("Timestamp")) && newdttype.equalsIgnoreCase("number")) {
                    newdata = "0";
                }
                if (olddttype.equalsIgnoreCase("Timestamp") && (newdttype.equalsIgnoreCase("Timestamp"))) {
                    newdata = "''";
                }
                if (olddttype.equalsIgnoreCase("DateTime") && (newdttype.equalsIgnoreCase("Timestamp"))) {
                    newdata = "''";
                }
                if (olddttype.equalsIgnoreCase("smalldatetime") && (newdttype.equalsIgnoreCase("Timestamp"))) {
                    newdata = "''";
                }
                if (olddttype.equalsIgnoreCase(newdttype) && (!olddttype.equalsIgnoreCase("Timestamp"))) {
                    newdata = "'" + olddata + "'";
                }
                newdataVec.addElement(newdata);
            }
            return newdataVec;
        }

        /** 
		 * Migrate the data from  ORACLE to MSSQL in that case it compares 
		 * olddatatypes and newdatatypes and then replace the corresponding
		 * converted data.
		 */
        private Vector convertColDataORACLE_MSSQL(String olddttype, String newdttype, Vector olddataVec) {
            Vector newdataVec = new Vector();
            Enumeration enm = olddataVec.elements();
            while (enm.hasMoreElements()) {
                olddata = enm.nextElement().toString();
                String newdata = "'" + olddata + "'";
                if (olddttype.equalsIgnoreCase("char") && (newdttype.equalsIgnoreCase("bigint") || newdttype.equalsIgnoreCase("tinyint") || newdttype.equalsIgnoreCase("smallint"))) {
                    newdata = "0";
                }
                if (olddttype.equalsIgnoreCase("char") && (newdttype.equalsIgnoreCase("float") || newdttype.equalsIgnoreCase("real") || newdttype.equalsIgnoreCase("decimal") || newdttype.equalsIgnoreCase("numeric"))) {
                    newdata = "0.0";
                }
                if (olddttype.equalsIgnoreCase("char") && (newdttype.equalsIgnoreCase("DateTime") || newdttype.equalsIgnoreCase("smalldatetime") || newdttype.equalsIgnoreCase("Timestamp"))) {
                    newdata = "''";
                }
                if (olddttype.equalsIgnoreCase("Date") && (newdttype.equalsIgnoreCase("bigint") || newdttype.equalsIgnoreCase("tinyint") || newdttype.equalsIgnoreCase("smallint") || newdttype.equalsIgnoreCase("decimal") || newdttype.equalsIgnoreCase("float") || newdttype.equalsIgnoreCase("real") || newdttype.equalsIgnoreCase("int") || newdttype.equalsIgnoreCase("numeric"))) {
                    newdata = "''";
                }
                if (olddttype.equalsIgnoreCase("Number") && (newdttype.equalsIgnoreCase("DateTime") || newdttype.equalsIgnoreCase("smalldatetime") || newdttype.equalsIgnoreCase("Timestamp"))) {
                    newdata = "''";
                }
                if (olddttype.equalsIgnoreCase("Number") && (newdttype.equalsIgnoreCase("tinyint") || newdttype.equalsIgnoreCase("smallint"))) {
                    newdata = "0";
                }
                if (olddttype.equalsIgnoreCase("VarChar2") && (newdttype.equalsIgnoreCase("DateTime") || newdttype.equalsIgnoreCase("smalldatetime") || newdttype.equalsIgnoreCase("Timestamp"))) {
                    newdata = "null";
                }
                if (olddttype.equalsIgnoreCase("VarChar2") && (newdttype.equalsIgnoreCase("bigint") || newdttype.equalsIgnoreCase("tinyint") || newdttype.equalsIgnoreCase("int") || newdttype.equalsIgnoreCase("smallint"))) {
                    newdata = "0";
                }
                if (olddttype.equalsIgnoreCase("VarChar2") && (newdttype.equalsIgnoreCase("numeric") || newdttype.equalsIgnoreCase("decimal") || newdttype.equalsIgnoreCase("float") || newdttype.equalsIgnoreCase("real"))) {
                    newdata = "0.0";
                }
                if (olddttype.equalsIgnoreCase("nVarChar2") && (newdttype.equalsIgnoreCase("bigint") || newdttype.equalsIgnoreCase("tinyint") || newdttype.equalsIgnoreCase("int") || newdttype.equalsIgnoreCase("smallint"))) {
                    newdata = "0";
                }
                if (olddttype.equalsIgnoreCase("nVarChar2") && (newdttype.equalsIgnoreCase("decimal") || newdttype.equalsIgnoreCase("numeric") || newdttype.equalsIgnoreCase("float") || newdttype.equalsIgnoreCase("real"))) {
                    newdata = "0.0";
                }
                if (olddttype.equalsIgnoreCase("nVarChar2") && (newdttype.equalsIgnoreCase("DateTime") || newdttype.equalsIgnoreCase("smalldatetime") || newdttype.equalsIgnoreCase("Timestamp"))) {
                    newdata = "''";
                }
                if (olddttype.equalsIgnoreCase("Timestamp") && (newdttype.equalsIgnoreCase("nchar") || newdttype.equalsIgnoreCase("Char"))) {
                    newdata = "''";
                }
                if (olddttype.equalsIgnoreCase("Timestamp") && (newdttype.equalsIgnoreCase("bigint") || newdttype.equalsIgnoreCase("tinyint") || newdttype.equalsIgnoreCase("decimal") || newdttype.equalsIgnoreCase("float") || newdttype.equalsIgnoreCase("real") || newdttype.equalsIgnoreCase("int") || newdttype.equalsIgnoreCase("numeric") || newdttype.equalsIgnoreCase("smallint"))) {
                    newdata = "''";
                }
                if (olddttype.equalsIgnoreCase("Timestamp") && (newdttype.equalsIgnoreCase("Timestamp") || newdttype.equalsIgnoreCase("smalldatetime") || newdttype.equalsIgnoreCase("DateTime"))) {
                    newdata = "null";
                }
                if (olddttype.equalsIgnoreCase(newdttype) && (!olddttype.equalsIgnoreCase("Timestamp"))) {
                    newdata = "'" + olddata + "'";
                }
                newdataVec.addElement(newdata);
            }
            return newdataVec;
        }

        /**Migrate the data from  MSSQL to MSSQL in that case it compares 
		 * olddatatypes and newdatatypes and then replace the corresponding
		 * converted data.
		 */
        private Vector convertColDataMSSQL_MSSQL(String olddttype, String newdttype, Vector olddataVec) {
            Vector newdataVec = new Vector();
            Enumeration enm = olddataVec.elements();
            while (enm.hasMoreElements()) {
                olddata = enm.nextElement().toString();
                String newdata = "'" + olddata + "'";
                if (olddttype.equals("nvarchar") || olddttype.equals("varchar")) {
                    if (olddata.contains("'")) {
                        olddata = olddata.replaceAll("'", "''");
                    }
                }
                if (olddttype.equalsIgnoreCase("Date")) {
                    if (olddata.equals("0")) {
                        newdata = "";
                    } else {
                        if (newdttype.equalsIgnoreCase("date")) {
                            olddata = olddata.replaceAll("'", "''");
                            if (desselcdriver.equalsIgnoreCase("oracle.jdbc.driver.OracleDriver")) {
                                newdata = "to_date('" + olddata + "','dd-MM-yyyy')";
                            }
                        } else {
                            olddata = olddata.replaceAll("'", "''");
                            newdata = "'" + getDateFormat("yyyy-MM-dd", "dd-MMM-yy", olddata) + "'";
                        }
                    }
                }
                if (olddttype.equalsIgnoreCase(newdttype) && (!olddttype.equalsIgnoreCase("Date"))) {
                    newdata = "'" + olddata + "'";
                }
                if ((olddttype.equalsIgnoreCase("nvarchar") && newdttype.equalsIgnoreCase("numeric")) || (olddttype.equalsIgnoreCase("nvarchar") && newdttype.equalsIgnoreCase("int")) || (olddttype.equalsIgnoreCase("nvarchar") && newdttype.equalsIgnoreCase("bigint"))) {
                    newdata = "";
                    for (int i = 0; i < olddata.length(); i++) {
                        int j = (int) olddata.charAt(i);
                        if (j >= 48 && j <= 57) {
                            newdata += olddata.charAt(i);
                        }
                    }
                    if (newdata.equalsIgnoreCase("") || newdata.equalsIgnoreCase(" ")) newdata = "0";
                    newdata = "'" + newdata + "'";
                }
                if (olddttype.equalsIgnoreCase("nvarchar") && newdttype.equalsIgnoreCase("varchar")) {
                    newdata = "'" + olddata + "'";
                }
                if (olddttype.equalsIgnoreCase("int") && newdttype.equalsIgnoreCase("numeric")) {
                    newdata = "'" + olddata + "'";
                }
                if (olddttype.equalsIgnoreCase("float") && newdttype.equalsIgnoreCase("numeric")) {
                    newdata = "'" + olddata + "'";
                }
                if (olddttype.equalsIgnoreCase("numeric") && newdttype.equalsIgnoreCase("int")) {
                    if (olddata.equalsIgnoreCase("") || olddata.equalsIgnoreCase(" ") || olddata.equalsIgnoreCase("null")) newdata = "'0'"; else newdata = String.valueOf(Integer.parseInt(olddata));
                }
                if (olddttype.equalsIgnoreCase("bigint") && newdttype.equalsIgnoreCase("numeric")) {
                    newdata = "'" + olddata + "'";
                }
                if (olddttype.equalsIgnoreCase("nvarchar") && newdttype.equalsIgnoreCase("datetime")) {
                    if (olddata.equals("null") || olddata.equals("") || olddata.equals(" ")) {
                        newdata = "''";
                    } else {
                        ParsePosition p1 = new ParsePosition(0);
                        SimpleDateFormat dateFormat = null;
                        java.util.Date d1 = null;
                        SimpleDateFormat df = null;
                        try {
                            df = new SimpleDateFormat("dd.MM.yy");
                        } catch (Exception e) {
                            df = new SimpleDateFormat("MMM dd yyyy");
                        }
                        try {
                            d1 = df.parse(olddata);
                        } catch (Exception e) {
                            return null;
                        }
                        dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                        newdata = dateFormat.format(d1);
                        newdata = "'" + olddata + "'";
                    }
                }
                if ((olddttype.equalsIgnoreCase("datetime")) || (olddttype.equalsIgnoreCase("smalldatetime") && newdttype.equalsIgnoreCase("datetime"))) {
                    if (olddata.equals("null") || olddata.equals("") || olddata.equals(" ")) {
                        newdata = "''";
                    } else newdata = "'" + olddata + "'";
                }
                if ((olddttype.equalsIgnoreCase("real") && newdttype.equalsIgnoreCase("numeric")) || (olddttype.equalsIgnoreCase("float") && newdttype.equalsIgnoreCase("numeric"))) if (olddata.contains("E")) {
                    newdata = "ABS(" + olddata + ")";
                } else newdata = "'" + olddata + "'";
                if (newdttype.equalsIgnoreCase("int") || newdttype.equalsIgnoreCase("numeric") || newdttype.equalsIgnoreCase("real") || newdttype.equalsIgnoreCase("bigint") || newdttype.equalsIgnoreCase("float")) if (olddata.equalsIgnoreCase("") || olddata.equalsIgnoreCase(" ") || olddata.equalsIgnoreCase("null")) newdata = "'0'";
                newdataVec.addElement(newdata);
            }
            return newdataVec;
        }

        /**Migrate the data from  Oracle to MYSQL in that case it compares 
		 * olddatatypes and newdatatypes and then replace the corresponding
		 * converted data.
		 */
        private Vector convertColDataOracle_MYSQL(String olddttype, String newdttype, Vector olddataVec) {
            Vector newdataVec = new Vector();
            Enumeration enm = olddataVec.elements();
            while (enm.hasMoreElements()) {
                olddata = enm.nextElement().toString();
                String newdata = "'" + olddata + "'";
                if (olddttype.equalsIgnoreCase("char") && (newdttype.equalsIgnoreCase("bigint") || newdttype.equalsIgnoreCase("tinyint") || newdttype.equalsIgnoreCase("smallint"))) {
                    newdata = "0";
                }
                if (olddttype.equalsIgnoreCase("char") && (newdttype.equalsIgnoreCase("float") || newdttype.equalsIgnoreCase("real") || newdttype.equalsIgnoreCase("decimal") || newdttype.equalsIgnoreCase("numeric"))) {
                    newdata = "0.0";
                }
                if (olddttype.equalsIgnoreCase("char") && (newdttype.equalsIgnoreCase("DateTime") || newdttype.equalsIgnoreCase("smalldatetime") || newdttype.equalsIgnoreCase("Timestamp"))) {
                    newdata = "''";
                }
                if (olddttype.equalsIgnoreCase("Date") && (newdttype.equalsIgnoreCase("bigint") || newdttype.equalsIgnoreCase("tinyint") || newdttype.equalsIgnoreCase("smallint") || newdttype.equalsIgnoreCase("decimal") || newdttype.equalsIgnoreCase("float") || newdttype.equalsIgnoreCase("real") || newdttype.equalsIgnoreCase("int") || newdttype.equalsIgnoreCase("numeric"))) {
                    newdata = "''";
                }
                if (olddttype.equalsIgnoreCase("Number") && (newdttype.equalsIgnoreCase("DateTime") || newdttype.equalsIgnoreCase("smalldatetime") || newdttype.equalsIgnoreCase("Timestamp"))) {
                    newdata = "''";
                }
                if (olddttype.equalsIgnoreCase("Number") && (newdttype.equalsIgnoreCase("tinyint") || newdttype.equalsIgnoreCase("smallint"))) {
                    newdata = "0";
                }
                if (olddttype.equalsIgnoreCase("VarChar2") && (newdttype.equalsIgnoreCase("DateTime") || newdttype.equalsIgnoreCase("smalldatetime") || newdttype.equalsIgnoreCase("Timestamp"))) {
                    newdata = "null";
                }
                if (olddttype.equalsIgnoreCase("VarChar2") && (newdttype.equalsIgnoreCase("bigint") || newdttype.equalsIgnoreCase("tinyint") || newdttype.equalsIgnoreCase("int") || newdttype.equalsIgnoreCase("smallint"))) {
                    newdata = "0";
                }
                if (olddttype.equalsIgnoreCase("VarChar2") && (newdttype.equalsIgnoreCase("numeric") || newdttype.equalsIgnoreCase("decimal") || newdttype.equalsIgnoreCase("float") || newdttype.equalsIgnoreCase("real"))) {
                    newdata = "0.0";
                }
                if (olddttype.equalsIgnoreCase("nVarChar2") && (newdttype.equalsIgnoreCase("bigint") || newdttype.equalsIgnoreCase("tinyint") || newdttype.equalsIgnoreCase("int") || newdttype.equalsIgnoreCase("smallint"))) {
                    newdata = "0";
                }
                if (olddttype.equalsIgnoreCase("nVarChar2") && (newdttype.equalsIgnoreCase("decimal") || newdttype.equalsIgnoreCase("numeric") || newdttype.equalsIgnoreCase("float") || newdttype.equalsIgnoreCase("real"))) {
                    newdata = "0.0";
                }
                if (olddttype.equalsIgnoreCase("nVarChar2") && (newdttype.equalsIgnoreCase("DateTime") || newdttype.equalsIgnoreCase("smalldatetime") || newdttype.equalsIgnoreCase("Timestamp"))) {
                    newdata = "''";
                }
                if (olddttype.equalsIgnoreCase("Timestamp") && (newdttype.equalsIgnoreCase("nchar") || newdttype.equalsIgnoreCase("Char"))) {
                    newdata = "''";
                }
                if (olddttype.equalsIgnoreCase("Timestamp") && (newdttype.equalsIgnoreCase("bigint") || newdttype.equalsIgnoreCase("tinyint") || newdttype.equalsIgnoreCase("decimal") || newdttype.equalsIgnoreCase("float") || newdttype.equalsIgnoreCase("real") || newdttype.equalsIgnoreCase("int") || newdttype.equalsIgnoreCase("numeric") || newdttype.equalsIgnoreCase("smallint"))) {
                    newdata = "''";
                }
                if (olddttype.equalsIgnoreCase("Timestamp") && (newdttype.equalsIgnoreCase("Timestamp") || newdttype.equalsIgnoreCase("smalldatetime") || newdttype.equalsIgnoreCase("DateTime"))) {
                    newdata = "null";
                }
                if (olddttype.equalsIgnoreCase(newdttype) && (!olddttype.equalsIgnoreCase("Timestamp"))) {
                    newdata = "'" + olddata + "'";
                }
                newdataVec.addElement(newdata);
            }
            return newdataVec;
        }

        /** This function is used to read the data from the table and stores in Vector. 
		 */
        private void readTableData(Vector tv, GetData g) {
            tempVec = tv;
            tableDataMap = new ReadExcel().readTable(tempVec.get(0).toString(), file);
            temp = (Vector) tableDataMap.get("Old Table Name");
            oldtablename = temp.get(0).toString();
            oldcolNameVec = (java.util.Vector) tableDataMap.get("Old Column Name");
            oldcoldtVec = (java.util.Vector) tableDataMap.get("Old Column Type");
            temp = (Vector) tableDataMap.get("New Table Name");
            newtablename = temp.get(0).toString();
            newcolNameVec = (java.util.Vector) tableDataMap.get("New Column Name");
            int count = newcolNameVec.size();
            Vector oldTempVec, newTempVec, newTempdtvec, oldTempdtvec;
            oldTempdtvec = new Vector();
            oldTempVec = new Vector();
            newcoldtVec = (java.util.Vector) tableDataMap.get("New Column Type");
            newTempVec = new Vector();
            newTempdtvec = new Vector();
            Object o;
            Vector lastcolVec = new Vector();
            Vector lastcoltypeVec = new Vector();
            for (int i = 0; i < count; i++) {
                if (!(newcolNameVec.get(i).toString().equalsIgnoreCase("null"))) {
                    try {
                        o = oldcolNameVec.get(i);
                        if (o == null || o.equals("") || o.equals("null")) {
                            lastcolVec.addElement(newcolNameVec.get(i));
                            lastcoltypeVec.addElement(newcoldtVec.get(i));
                        } else {
                            oldTempVec.addElement(o);
                            oldTempdtvec.addElement(oldcoldtVec.get(i));
                            newTempVec.addElement(newcolNameVec.get(i));
                            newTempdtvec.addElement(newcoldtVec.get(i));
                        }
                    } catch (ArrayIndexOutOfBoundsException aoe) {
                        if (newcolNameVec.size() > i) {
                            for (int k = i; k < count; k++) {
                                newTempVec.addElement(newcolNameVec.get(k));
                                newTempdtvec.addElement(newcoldtVec.get(k));
                            }
                        }
                    }
                }
            }
            newcolNameVec = new Vector();
            newcolNameVec = newTempVec;
            if (lastcolVec.size() > 0) newcolNameVec.addAll(lastcolVec);
            newcoldtVec = new Vector();
            newcoldtVec = newTempdtvec;
            if (lastcoltypeVec.size() > 0) newcoldtVec.addAll(newcoldtVec);
            oldcolNameVec = new Vector();
            oldcolNameVec = oldTempVec;
            oldcoldtVec = new Vector();
            oldcoldtVec = oldTempdtvec;
            oldcolNameVec = new Vector();
            oldcolNameVec = oldTempVec;
            oldcoldtVec = new Vector();
            oldcoldtVec = oldTempdtvec;
            oldTempVec = null;
            newTempVec = null;
            newTempdtvec = null;
            oldTempdtvec = null;
            int mn = g.getRowNumber(oldtablename, srcseldriver, desselcdriver)[0];
            int mx = g.getRowNumber(oldtablename, srcseldriver, desselcdriver)[1];
            int rowCnt = mx - mn;
            if (rowCnt == 0) {
                gbc.gridx = 0;
                gbc.gridy = y;
                gbc.anchor = GridBagConstraints.WEST;
                migStatus = "yes";
                g.closeOldConn();
                g.closeNewConn();
                _dispPanel.add(new JLabel("The table " + newtablename + " is migrated"), gbc);
                y++;
            }
            while (rowCnt != 0) {
                if (rowCnt > 10000) {
                    rowCnt = rowCnt - 10000;
                    colDataVec = g.getTableData(oldtablename, oldcolNameVec, oldcoldtVec, mn, mn + 10000, srcseldriver, desselcdriver);
                    mn = mn + 10001;
                    Vector tempColDataVec = getConvertedData(oldcolNameVec, newcolNameVec, oldcoldtVec, newcoldtVec, null, null, colDataVec);
                    colDataVec = new Vector();
                    if (tempColDataVec != null) colDataVec = tempColDataVec; else {
                        f = g.rollBack(newtablename);
                        final JLabel lbl = new JLabel("The table " + newtablename + " cannot be migrated");
                        gbc.gridx = 0;
                        gbc.gridy = y;
                        gbc.anchor = GridBagConstraints.WEST;
                        _dispPanel.add(lbl, gbc);
                        gbc.gridx = 1;
                        gbc.gridy = y;
                        btn = new JButton("Re-Migrate");
                        btn.addActionListener(new ActionListener() {

                            public void actionPerformed(java.awt.event.ActionEvent ae) {
                                migStatus = "no";
                                Vector v = new Vector();
                                v.addElement(oldtablename);
                                GetConn obj = new GetConn(v);
                                ThrdCnt++;
                                obj.start();
                                SwingUtilities.invokeLater(new Runnable() {

                                    public void run() {
                                        if (migStatus.equalsIgnoreCase("yes")) {
                                            btn.setVisible(false);
                                            lbl.setText("The table " + oldtablename + " is migrated");
                                        }
                                    }
                                });
                            }
                        });
                        gbc.anchor = GridBagConstraints.WEST;
                        _dispPanel.add(btn, gbc);
                        y++;
                        gbc.gridx = 0;
                        gbc.gridy = y;
                        gbc.anchor = GridBagConstraints.CENTER;
                        g.closeOldConn();
                        g.closeNewConn();
                        _dispPanel.add(new JLabel("The data is incorrect in " + oldcolnm), gbc);
                        y++;
                        break;
                    }
                    g.insertColData(newtablename, newcolNameVec, colDataVec);
                } else {
                    colDataVec = g.getTableData(oldtablename, oldcolNameVec, oldcoldtVec, mn, mx, srcseldriver, desselcdriver);
                    Vector tempColDataVec = getConvertedData(oldcolNameVec, newcolNameVec, oldcoldtVec, newcoldtVec, null, null, colDataVec);
                    if (tempColDataVec != null) colDataVec = tempColDataVec; else {
                        f = g.rollBack(newtablename);
                        gbc.gridx = 0;
                        gbc.gridy = y;
                        final JLabel lbl = new JLabel("The table " + newtablename + " cannot be migrated");
                        gbc.anchor = GridBagConstraints.WEST;
                        _dispPanel.add(lbl, gbc);
                        gbc.gridx = 1;
                        gbc.gridy = y;
                        btn = new JButton("Re-Migrate");
                        btn.addActionListener(new ActionListener() {

                            public void actionPerformed(java.awt.event.ActionEvent ae) {
                                migStatus = "no";
                                Vector v = new Vector();
                                v.addElement(oldtablename);
                                GetConn obj = new GetConn(v);
                                ThrdCnt++;
                                obj.start();
                                SwingUtilities.invokeLater(new Runnable() {

                                    public void run() {
                                        if (migStatus.equalsIgnoreCase("yes")) {
                                            btn.setVisible(false);
                                            lbl.setText("The table " + oldtablename + " is migrated");
                                        }
                                    }
                                });
                            }
                        });
                        _dispPanel.add(btn, gbc);
                        y++;
                        g.closeOldConn();
                        g.closeNewConn();
                        gbc.gridx = 0;
                        gbc.gridy = y;
                        _dispPanel.add(new JLabel("The data is incorrect in " + oldcolnm), gbc);
                        y++;
                        return;
                    }
                    colDataVec = new Vector();
                    colDataVec = tempColDataVec;
                    g.insertColData(newtablename, newcolNameVec, colDataVec);
                    rowCnt = 0;
                    g.closeOldConn();
                    g.closeNewConn();
                    gbc.gridx = 0;
                    gbc.gridy = y;
                    gbc.anchor = GridBagConstraints.WEST;
                    migStatus = "yes";
                    _dispPanel.add(new JLabel("The table " + newtablename + " is migrated"), gbc);
                    y++;
                }
            }
        }
    }
}
