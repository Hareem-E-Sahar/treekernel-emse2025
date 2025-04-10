package guineu.database.intro.impl;

import guineu.data.Dataset;
import guineu.data.PeakListRow;
import guineu.data.DatasetType;
import guineu.data.impl.datasets.SimpleLCMSDataset;
import guineu.data.impl.datasets.SimpleGCGCDataset;
import guineu.data.impl.peaklists.SimplePeakListRowGCGC;
import guineu.data.impl.peaklists.SimplePeakListRowLCMS;
import guineu.database.retrieve.impl.OracleRetrievement;
import guineu.data.impl.datasets.SimpleQualityControlDataset;
import guineu.modules.mylly.datastruct.Spectrum;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Functions to save different data set into de database.
 * Only usefull for VTT biotechnology group
 *
 * @author SCSANDRA
 */
public class WriteDataBase {

    private double progress;

    public double getProgress() {
        return progress;
    }

    /**
     * Fills Table EXPERIMENT
     *
     *      EPID            NUMBER(38,0)
     *      NAME            VARCHAR2(2000 BYTE)
     *      TYPE            VARCHAR2(2000 BYTE)
     *      PROJECT         VARCHAR2(2000 BYTE)
     *      PERSON          VARCHAR2(1000 BYTE)
     *      REPLICATE       VARCHAR2(100 BYTE)
     *      CDF             BFILE
     *      AMOUNT          NVARCHAR2(1000 CHAR)
     *      UNIT            NVARCHAR2(20 CHAR)
     *      METHOD          VARCHAR2(400 BYTE)
     *      SAMPLE          VARCHAR2(400 BYTE)
     *      EDATE           VARCHAR2(20 BYTE)
     *      INSFILE_NAME	VARCHAR2(4000 BYTE)
     *
     * @param conn Connection to the database
     * @param dataset LCxMS or GCGC-MS data set
     * @param datasetId data set ID
     */
    public void tableEXPERIMENT(Connection conn, Dataset dataset, int datasetId) {
        double progressDone = 0;
        try {
            Statement statement = conn.createStatement();
            ResultSet r = null;
            for (String sampleName : dataset.getAllColumnNames()) {
                String sampleNameExp;
                try {
                    sampleNameExp = sampleName.substring(0, sampleName.indexOf(" "));
                } catch (Exception e) {
                    sampleNameExp = sampleName;
                }
                if (sampleName != null) {
                    r = statement.executeQuery("SELECT * FROM EXPERIMENT " + "WHERE NAME = '" + sampleNameExp + "'");
                    if (r.next()) {
                        statement.executeUpdate("INSERT INTO DATASET_COLUMNS (NAME, EXPERIMENT_ID ,DATASET_ID) VALUES ('" + sampleName + "', '" + r.getInt(1) + "', '" + datasetId + "')");
                    } else {
                        statement.executeUpdate("INSERT INTO DATASET_COLUMNS (NAME,DATASET_ID) VALUES ('" + sampleName + "', '" + datasetId + "')");
                    }
                }
                progressDone++;
                progress = progressDone / dataset.getNumberCols();
            }
            r.close();
            statement.close();
        } catch (SQLException ex) {
            Logger.getLogger(InOracle.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Fills Table DATASET
     *
     *       STUDY          NUMBER(38,0)
     *       EXCEL_NAME	VARCHAR2(200 BYTE)
     *       D_TYPE         VARCHAR2(20 BYTE)
     *       AUTHOR         VARCHAR2(20 BYTE)
     *       D_DATE         DATE
     *       UNITS          VARCHAR2(20 BYTE)
     *       PARAMETERS	BFILE
     *       DATASETID	NUMBER
     *       INFORMATION	VARCHAR2(4000 BYTE)
     *       NUMBER_ROWS	NUMBER
     *
     * @param conn Connection
     * @param excelName The name of the excel file where the experiments are.
     * @param type (LSMS or GCGCTof)
     * @param author (Author of the data)
     * @param parameters parameters
     * @param study Study of the dataset
     * @param info Information about the dataset (writen by the user in a textbox)
     * @return the ID of the data in the database.
     */
    public int tableDATASET(Connection conn, String excelName, String type, String author, String parameters, String study, String info, int numberRows) {
        try {
            int exp_id = 0;
            if (excelName != null) {
                Statement statement = conn.createStatement();
                try {
                    String dir = "";
                    String file = "";
                    try {
                        int line = parameters.lastIndexOf("\\");
                        dir = parameters;
                        file = parameters;
                        if (line > 0) {
                            dir = parameters.substring(0, line);
                            file = parameters.substring(line + 1);
                        }
                    } catch (Exception exception) {
                    }
                    if (info.length() > 3999) {
                        info = info.substring(0, 3999);
                    }
                    statement.executeUpdate("INSERT INTO DATASET (EXCEL_NAME,D_TYPE,AUTHOR,D_DATE,UNITS,PARAMETERS, STUDY,INFORMATION, NUMBER_ROWS) VALUES ('" + excelName + "', '" + type + "', '" + author + "', to_date(sysdate,'dd/MM/yyyy'),'µl', bfilename('" + dir + "', '" + file + "'), '" + OracleRetrievement.getStudyID(study, conn) + "', '" + info + "', '" + numberRows + "')");
                } catch (SQLException sqlexception) {
                    sqlexception.printStackTrace();
                }
                ResultSet r = statement.executeQuery("SELECT * FROM DATASET WHERE EXCEL_NAME = '" + excelName + "' ORDER BY DATASETID desc");
                if (r.next()) {
                    exp_id = r.getInt(8);
                }
                statement.close();
                return exp_id;
            }
            return -1;
        } catch (Exception exception) {
            System.out.println("ERROR : " + exception);
            exception.printStackTrace();
            return -1;
        }
    }

    /**
     * Fills Table MOL_LCMS
     *
     *        ID                    NUMBER(38,0)
     *        AVERAGE_MZ            FLOAT
     *        AVERAGE_RT            FLOAT
     *        LIPID_NAME            VARCHAR2(1000 BYTE)
     *        LIPID_CLASS           NUMBER
     *        N_FOUND               NUMBER
     *        STD                   NUMBER(1,0)
     *        EPID                  NUMBER(38,0)
     *        FA_COMPOSITION        VARCHAR2(4000 BYTE)
     *        PUBCHEM_ID            VARCHAR2(1000 BYTE)
     *        VTTID                 VARCHAR2(20 BYTE)
     *        VTTALLIDS             VARCHAR2(20 BYTE)
     *        ALL_NAMES             VARCHAR2(1000 BYTE)
     *        IDENTIFICATION_TYPE	VARCHAR2(1000 BYTE)
     *
     * @param conn Connection
     * @param dataset LC-MS data set
     * @param datasetID data set ID
     * @return Array with the IDs of the data set metabolites
     */
    public int[] tableMOL_LCMS(Connection conn, SimpleLCMSDataset dataset, int datasetID) {
        double progressDone = 0;
        try {
            int[] mol_ID = new int[dataset.getNumberRows()];
            Statement statement = conn.createStatement();
            for (int i = 0; i < dataset.getNumberRows(); i++) {
                SimplePeakListRowLCMS lipid = (SimplePeakListRowLCMS) dataset.getRow(i);
                try {
                    statement.executeUpdate("INSERT INTO MOL_LCMS (AVERAGE_MZ," + "AVERAGE_RT,LIPID_NAME,LIPID_CLASS,N_FOUND,STD,EPID, " + "FA_COMPOSITION,PUBCHEM_ID, VTTID, VTTALLIDS,ALL_NAMES,IDENTIFICATION_TYPE)" + " VALUES ( '" + Double.valueOf(lipid.getMZ()).floatValue() + "', '" + Double.valueOf(lipid.getRT()).floatValue() + "', '" + lipid.getName() + "', '" + Integer.valueOf(lipid.getMolClass()) + "', '" + (int) lipid.getNumFound() + "', '" + lipid.getStandard() + "', '" + datasetID + "', '" + lipid.getFAComposition() + "', '" + lipid.getPubChemID() + "', '" + lipid.getVTTID() + "', '" + lipid.getAllVTTID() + "', '" + lipid.getAllNames() + "', '" + lipid.getIdentificationType() + "') ");
                    ResultSet r = statement.executeQuery("SELECT * FROM MOL_LCMS ORDER BY ID desc");
                    if (r.next()) {
                        mol_ID[i] = r.getInt(1);
                    }
                } catch (SQLException se) {
                    System.out.println("We got an exception while preparing a statement:" + "Probably bad SQL.");
                    se.printStackTrace();
                }
                progressDone++;
                progress = progressDone / dataset.getNumberRows();
            }
            statement.close();
            return mol_ID;
        } catch (SQLException ex) {
            ex.printStackTrace();
            Logger.getLogger(InOracle.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Intro dates into the table MEASUREMENT
     *
     *       ID             NUMBER(38,0)
     *       DATASET_CID	NUMBER(38,0)
     *       MOL_LCMS_ID	NUMBER(38,0)
     *       MOL_GCGCTOF_ID	NUMBER(38,0)
     *       CONCENTRATION	FLOAT
     *       DATASETID	NUMBER(38,0)
     *
     * @param conn Connection
     * @param dataset LC-MS or GCxGC-MS data set
     * @param metaboliteID
     * @param datasetID
     */
    public void tableMEASUREMENT(Connection conn, Dataset dataset, int[] metaboliteID, int datasetID) {
        double progressDone = 0;
        double totalDone = dataset.getNumberRows() * dataset.getNumberCols();
        Statement statement = null;
        try {
            statement = conn.createStatement();
            for (int i = 0; i < dataset.getNumberRows(); i++) {
                PeakListRow row = dataset.getRow(i);
                for (String experimentName : dataset.getAllColumnNames()) {
                    try {
                        Double peak = (Double) row.getPeak(experimentName);
                        ResultSet r = statement.executeQuery("SELECT COLUMN_ID FROM DATASET_COLUMNS WHERE NAME = '" + experimentName + "'");
                        int ID_sample = 0;
                        if (r.next()) {
                            ID_sample = r.getInt(1);
                            statement.close();
                        } else {
                            statement.close();
                            break;
                        }
                        statement = conn.createStatement();
                        if (dataset.getType() == DatasetType.LCMS) {
                            statement.executeUpdate("INSERT INTO MEASUREMENT (DATASET_CID," + "MOL_LCMS_ID,CONCENTRATION, DATASETID) VALUES ('" + ID_sample + "', '" + metaboliteID[i] + "', '" + (float) ((Double) peak).floatValue() + "', '" + datasetID + "') ");
                        } else if (dataset.getType() == DatasetType.GCGCTOF) {
                            statement.executeUpdate("INSERT INTO MEASUREMENT (DATASET_CID,MOL_GCGCTOF_ID,CONCENTRATION, DATASETID) VALUES " + "('" + ID_sample + "', '" + metaboliteID[i] + "', '" + (float) ((Double) peak).floatValue() + "', '" + datasetID + "') ");
                        }
                    } catch (Exception e) {
                    }
                    progressDone++;
                    progress = progressDone / totalDone;
                }
            }
            statement.close();
        } catch (SQLException ex) {
            try {
                statement.close();
                Logger.getLogger(InOracle.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex1) {
                Logger.getLogger(InOracle.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }

    /**
     * Fills table MOL_GCGCTOF
     *
     *      ID                      NUMBER(38,0)
     *      RT1                     FLOAT
     *      RT2                     FLOAT
     *      RTI                     FLOAT
     *      N_FOUND                 NUMBER
     *      MAX_SIMILARITY          NUMBER
     *      MEAN_SIMILARITY         FLOAT
     *      SIMILARITY_STD_DEV	FLOAT
     *      METABOLITE_NAME         VARCHAR2(500 BYTE)
     *      PUBCHEM_ID              VARCHAR2(100 BYTE)
     *      METABOLITE_ALLNAMES	VARCHAR2(2000 BYTE)
     *      EPID                    NUMBER(38,0)
     *      MASS                    FLOAT
     *      DIFFERENCE              FLOAT
     *      SPECTRUM                VARCHAR2(4000 BYTE)
     *      CAS                     VARCHAR2(20 BYTE)
     *      CLASS                   VARCHAR2(2000 BYTE)
     *
     * @param conn Connection
     * @param dataset GCxGC-MS data set
     * @param datasetID data set ID
     * @return Array with the IDs of the data set metabolites
     */
    public int[] tableMOL_GCGCTOF(Connection conn, SimpleGCGCDataset dataset, int datasetID) {
        double progressDone = 0;
        try {
            Statement st = conn.createStatement();
            int[] mol_ID = new int[dataset.getNumberRows() + 1];
            for (int i = 0; i < dataset.getNumberRows(); i++) {
                SimplePeakListRowGCGC metabolite = (SimplePeakListRowGCGC) dataset.getRow(i);
                double mass = metabolite.getMass();
                if (mass < 0) {
                    mass = 0;
                }
                String name = metabolite.getName().replaceAll("'", "ç");
                String allNames = metabolite.getAllNames().replaceAll("'", "ç");
                try {
                    st.executeUpdate("INSERT INTO MOL_GCGCTOF (RT1, RT2, RTI, N_FOUND, MAX_SIMILARITY, MEAN_SIMILARITY, SIMILARITY_STD_DEV, METABOLITE_NAME, PUBCHEM_ID, METABOLITE_ALLNAMES, EPID, MASS, DIFFERENCE, SPECTRUM, CAS, CLASS) VALUES " + "('" + (float) metabolite.getRT1() + "', '" + (float) metabolite.getRT2() + "', '" + (float) metabolite.getRTI() + "', '" + (int) metabolite.getNumFound() + "', '" + (int) metabolite.getMaxSimilarity() + "', '" + (float) metabolite.getMeanSimilarity() + "', '" + (float) metabolite.getSimilaritySTDDev() + "', '" + name + "', '" + metabolite.getPubChemID() + "', '" + allNames + "', '" + (int) datasetID + "', '" + (float) mass + "', '" + (float) metabolite.getDifference() + "', '" + metabolite.getSpectrumString() + "', '" + metabolite.getCAS() + "', '" + metabolite.getMolClass() + "') ");
                    ResultSet r = st.executeQuery("SELECT * FROM MOL_GCGCTOF ORDER BY ID desc");
                    r.next();
                    mol_ID[i] = r.getInt("ID");
                    r.close();
                } catch (SQLException se) {
                    System.out.println("We got an exception while preparing a statement:" + "Probably bad SQL.");
                    se.printStackTrace();
                }
                progressDone++;
                progress = progressDone / dataset.getNumberRows();
            }
            st.close();
            return mol_ID;
        } catch (SQLException ex) {
            Logger.getLogger(WriteDataBase.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Fills table SPECTRUMS
     *
     *      ID              NUMBER(38,0)
     *      MOL_ID          NUMBER(38,0)
     *      MASS            FLOAT
     *      INTENSITY	FLOAT
     *
     * @param conn Connection
     * @param dataset GCxGC-MS data set
     * @param st Database statement
     * @param metabolitesID Array with the IDs of the data set metabolites in the table MOL_GCGCTOF
     */
    public void tableSPECTRUM(Connection conn, SimpleGCGCDataset dataset, Statement st, int[] metabolitesID) {
        double progressDone = 0;
        try {
            st = conn.createStatement();
            for (int i = 0; i < dataset.getNumberRows(); i++) {
                SimplePeakListRowGCGC peak = (SimplePeakListRowGCGC) dataset.getRow(i);
                Spectrum spectrum = peak.getSpectrum();
                if (spectrum == null) {
                    break;
                }
                for (int e = 0; e < spectrum.length(); e++) {
                    try {
                        if (e == 0) {
                            ResultSet r = st.executeQuery("SELECT * FROM SPECTRUMS WHERE MOL_ID = '" + metabolitesID[i] + "'");
                            if (r.next()) {
                                break;
                            }
                            r.close();
                        }
                        st.executeUpdate("INSERT INTO SPECTRUMS (MOL_ID, MASS, INTENSITY) VALUES ( '" + (float) metabolitesID[i] + "', '" + (float) spectrum.getPeakList().get(e).getFirst() + "', '" + (float) spectrum.getPeakList().get(e).getSecond() + "') ");
                    } catch (SQLException se) {
                        System.out.println("We got an exception while preparing a statement:" + "Probably bad SQL.");
                        se.printStackTrace();
                    }
                }
                progressDone++;
                progress = progressDone / dataset.getNumberRows();
            }
        } catch (SQLException ex) {
            Logger.getLogger(InOracle.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Connects with Pubchem database and gets the pubchem ID of the metabolite
     *
     * @param search Name of the metabolite
     * @return Pubchem ID
     */
    public static int getPubChemID(String metaboliteName) {
        try {
            System.setProperty("http.proxyHost", "rohto.vtt.fi");
            System.setProperty("http.proxyPort", "8000");
            String page = "http://www.ncbi.nlm.nih.gov/sites/entrez?db=pcsubstance&term=" + URLEncoder.encode("\"" + metaboliteName + "\"", "UTF-8");
            URL urlObject = new URL(page);
            URLConnection con = urlObject.openConnection();
            BufferedReader webData = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            int mark = 0;
            int result = 0;
            while ((line = webData.readLine()) != null) {
                if (line.indexOf("- - - - - - - - begin Results - - - - - -") > -1) {
                    mark = 1;
                }
                if (mark == 1) {
                    if (line.indexOf("var Menu") > -1) {
                        int i = line.indexOf("var Menu") + 8;
                        if (i != -1) {
                            int j = line.indexOf("_");
                            line = line.substring(i, j);
                            result = Integer.parseInt(line.trim());
                            webData.close();
                            webData.close();
                            return result;
                        } else {
                            webData.close();
                            webData.close();
                            return 0;
                        }
                    }
                }
            }
            return 0;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Fills table QUALITYC
     *
     *      SAMPLESET               VARCHAR2(1000 BYTE)
     *      DATESET                    VARCHAR2(10 BYTE)
     *      ION_MODE                VARCHAR2(100 BYTE)
     *      INJECTION_VOLUME	VARCHAR2(100 BYTE)
     *      SAMPLE_TYPE             VARCHAR2(100 BYTE)
     *      QC_ID                   NUMBER
     *      COMMENTS                VARCHAR2(4000 BYTE)
     *
     * @param conn Connection
     * @param QCDataset Quality control data set
     * @return ID of the data set
     */
    public int TableQUALITYC(Connection conn, SimpleQualityControlDataset QCDataset) {
        try {
            int QC_ID = 0;
            Statement statement = conn.createStatement();
            statement.executeUpdate("INSERT INTO QUALITYC (SAMPLESET, DATESET, ION_MODE, INJECTION_VOLUME, SAMPLE_TYPE, COMMENTS) VALUES ('" + QCDataset.getSampleSet() + "', '" + QCDataset.getDate() + "', '" + QCDataset.getIonMode() + "', '" + QCDataset.getInjection() + "', '" + QCDataset.getSampleType() + "', '" + QCDataset.getComments() + "')");
            ResultSet r = statement.executeQuery("SELECT * FROM QUALITYC ORDER BY QC_ID desc");
            if (r.next()) {
                QC_ID = r.getInt("QC_ID");
            }
            statement.close();
            return QC_ID;
        } catch (SQLException ex) {
            ex.printStackTrace();
            Logger.getLogger(InOracle.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    /**
     * Fills table QCSAMPLE
     *
     *      QC_ID                   NUMBER
     *      SAMPLE_NAME             VARCHAR2(1000 BYTE)
     *      LYSOPC_RT               NUMBER(10,0)
     *      LYSOPC_HEIGHT/AREA	NUMBER(10,0)
     *      LYSOPC_HEIGHT_RATIO	NUMBER(10,0)
     *      PC_RT                   NUMBER(10,0)
     *      PC_HEIGHT/AREA  	NUMBER(10,0)
     *      PC_HEIGHT_RATIO         NUMBER(10,0)
     *      TG_RT                   NUMBER(10,0)
     *      TG_HEIGHT/AREA          NUMBER(10,0)
     *      TG_HEIGHT_RATIO         NUMBER(10,0)
     *      DATESET                    DATE
     *      LYSOPC_HEIGHT           NUMBER(10,0)
     *      PC_HEIGHT               NUMBER(10,0)
     *      TG_HEIGHT               NUMBER(10,0)
     *      S_LYSOPC_HEIGHT         NUMBER(10,0)
     *      S_LYSOPC_RT             NUMBER(10,0)
     *      S_PC_HEIGHT             NUMBER(10,0)
     *      S_PC_RT                 NUMBER(10,0)
     *      S_TG_HEIGHT             NUMBER(10,0)
     *      S_TG_RT                 NUMBER(10,0)
     *
     * @param conn Connection
     * @param QCDataset Quality control data set
     * @param QC_ID ID of the data set in the table QUALITIC
     */
    public void TableQCSample(Connection conn, SimpleQualityControlDataset QCDataset, int QC_ID) {
        try {
            double progressDone = 0;
            Statement statement = conn.createStatement();
            for (PeakListRow row : QCDataset.getRowsDB()) {
                Object[] peaks = row.getPeaks(null);
                DateFormat dfm = new SimpleDateFormat("ddMMyy");
                try {
                    statement.executeUpdate("INSERT INTO QCSAMPLE (DATASETID," + "SAMPLE_NAME, LYSOPC_RT, LYSOPC_HEIGHT_AREA, LYSOPC_HEIGHT_RATIO," + " PC_RT, PC_HEIGHT_AREA, PC_HEIGHT_RATIO, TG_RT, TG_HEIGHT_AREA, " + "TG_HEIGHT_RATIO, DATESET, TIMESET, LYSOPC_HEIGHT, PC_HEIGHT, TG_HEIGHT," + "S_LYSOPC_HEIGHT, S_LYSOPC_RT, S_PC_HEIGHT, S_PC_RT, S_TG_HEIGHT, S_TG_RT)" + " VALUES ( '" + (int) QC_ID + "', '" + (String) peaks[17] + "', '" + Double.parseDouble((String) peaks[20]) + "', '" + Double.parseDouble((String) peaks[19]) + "', '" + Double.parseDouble((String) peaks[18]) + "', '" + Double.parseDouble((String) peaks[16]) + "', '" + Double.parseDouble((String) peaks[15]) + "', '" + Double.parseDouble((String) peaks[14]) + "', '" + Double.parseDouble((String) peaks[13]) + "', '" + Double.parseDouble((String) peaks[12]) + "', '" + Double.parseDouble((String) peaks[11]) + "', '" + dfm.parse(peaks[10].toString()).toString() + "', '" + (String) peaks[9] + "', '" + Double.parseDouble((String) peaks[1]) + "', '" + Double.parseDouble((String) peaks[2]) + "', '" + Double.parseDouble((String) peaks[3]) + "', '" + Double.parseDouble((String) peaks[4]) + "', '" + Double.parseDouble((String) peaks[5]) + "', '" + Double.parseDouble((String) peaks[6]) + "', '" + Double.parseDouble((String) peaks[7]) + "', '" + Double.parseDouble((String) peaks[8]) + "', '" + Double.parseDouble((String) peaks[0]) + "') ");
                } catch (Exception se) {
                    System.out.println("We got an exception while preparing a statement:" + "Probably bad SQL.");
                    se.printStackTrace();
                }
                progressDone++;
                progress = progressDone / QCDataset.getRowsDB().size();
            }
            statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            Logger.getLogger(InOracle.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
