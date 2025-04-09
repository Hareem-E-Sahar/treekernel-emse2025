package org.epo.jpxi.test;

import java.util.LinkedList;
import java.util.Properties;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.rmi.server.UID;
import java.sql.Timestamp;
import org.apache.log4j.Logger;
import org.epo.jpxi.cl.JpxiServiceDelegate;
import org.epo.jpxi.shared.JpxiDataToken;
import org.epo.jpxi.shared.JpxiDmsDbRec;
import org.epo.jpxi.shared.JpxiDocSkeletonObj;
import org.epo.jpxi.shared.JpxiDocSubSelection;
import org.epo.jpxi.shared.JpxiException;
import org.epo.jpxi.shared.JpxiTransactionAck;
import org.epoline.jsf.client.ServiceNotAvailableException;

/**
 * @author INFOTEL CONSEIL
 * this is the working class of the Junit tests
 */
public class JpxiTester {

    private String userName = null, userPwd = null;

    private String daDocId = null;

    private String dlDocId = null;

    private String dlDoc = null;

    private LinkedList dmsList = null;

    private int sigType = -1;

    private String electronicFormat = null;

    private int totalPages = -1;

    private int docSize = -1;

    private JpxiDmsDbRec myDMS = null;

    private int buffSize = -1;

    private int rupture = -1;

    private Timestamp publicationDate = null;

    private char copyright = ' ';

    private int totalRecords = -1;

    private String transacState = null;

    private java.util.LinkedList rank = null;

    private int nbIteration;

    private int nbSession;

    private static final String BEGIN_ELECTRONIQUE_FORMAT_TAG = "<electronicFormat>";

    private static final String BEGIN_TOTAL_PAGES_TAG = "<totalPages>";

    private static final String BEGIN_DOC_SIZE_TAG = "<docSize>";

    private static final String BEGIN_COPYRIGHT_TAG = "<copyrightStatus>";

    private static final String BEGIN_PUB_DATE_TAG = "<datePublication>";

    private static final String END_ELECTRONIQUE_FORMAT_TAG = "</electronicFormat>";

    private static final String END_TOTAL_PAGES_TAG = "</totalPages>";

    private static final String END_DOC_SIZE_TAG = "</docSize>";

    private static final String END_COPYRIGHT_TAG = "</copyrightStatus>";

    private static final String END_PUB_DATE_TAG = "</datePublication>";

    private static final int ST33_HEADER_LENGTH = 252;

    private static final int DATA_RECORD_LENGTH = 19740;

    /**
	 * Load parameters from Properties
	 * @param theProp the property
	 */
    protected void loadParameters(Properties theProp) {
        setUserName(theProp.getProperty("org.epo.jpxi.rmi.user.name"));
        setUserPwd(theProp.getProperty("org.epo.jpxi.rmi.user.pwd"));
        setDaDocId(theProp.getProperty("org.epo.jpxi.test.datransaction.DaDocID"));
        setSigType(theProp.getProperty("org.epo.jpxi.test.datransaction.sigtype"));
        setRupture(theProp.getProperty("org.epo.jpxi.test.datransaction.rupture"));
        setBuffSize(Integer.parseInt(theProp.getProperty("org.epo.jpxi.test.datransaction.buffer")));
        setDlDocId(theProp.getProperty("org.epo.jpxi.test.dltransaction.DlDocId"));
        setDlDoc(theProp.getProperty("org.epo.jpxi.test.dltransaction.DlDoc"));
        setElectronicFormat(theProp.getProperty("org.epo.jpxi.test.dltransaction.electronicFormat"));
        setPublicationDate(Timestamp.valueOf(theProp.getProperty("org.epo.jpxi.test.dltransaction.PublicationDate")));
    }

    /**
 * retrieve a document and check it
 * @param theProxy
 * @param theUID
 * @return true if ok false otherwise
 */
    public boolean dirInquire(JpxiServiceDelegate theDelegate, UID theUID) {
        LinkedList myList = null;
        try {
            myList = theDelegate.inquire(theUID, getDaDocId(), 0, true, true);
            if (myList.size() == 0) return (false);
            setMyDMS((JpxiDmsDbRec) myList.get(0));
        } catch (Exception e) {
            return (false);
        }
        saveDataFromDms();
        createRank();
        return (true);
    }

    /**
	 * done a DATransaction and check if is ok
	 * @param theProxy
	 * @param theUID
	 * @return true if ok false otherwise
	 */
    public boolean daTransactionTest(JpxiServiceDelegate theDelegate, UID theUID) {
        JpxiTransactionAck myAck = null;
        JpxiDocSubSelection mySelection = newSelection();
        UID myTransactionUID;
        JpxiDataToken jpxiDataToken = null;
        int myByteCount = 0;
        boolean myEod = false;
        int mySigUnitNumber = -1;
        int myLastSigUnitNumber = -1;
        int myOutputLength = 0;
        try {
            myAck = theDelegate.newDATransaction(theUID, getDaDocId(), getElectronicFormat(), mySelection, getRupture(), getBuffSize());
        } catch (Exception e) {
            return (false);
        }
        myTransactionUID = myAck.getTransactionUID();
        jpxiDataToken = myAck.getEmbededData();
        myByteCount += jpxiDataToken.getDataSize();
        myByteCount = 0;
        byte[] myData = null;
        int myDataLength = 0;
        byte[] myTotalData = new byte[0], myTempData = null;
        int myTotalLength = 0;
        try {
            while (!myEod) {
                if (mySigUnitNumber == -1) myLastSigUnitNumber = jpxiDataToken.getSigUnitNumber(); else jpxiDataToken = theDelegate.read(myAck.getTransactionUID());
                mySigUnitNumber = jpxiDataToken.getSigUnitNumber();
                if (mySigUnitNumber != myLastSigUnitNumber) {
                    myOutputLength += myTotalLength;
                    myTotalData = new byte[0];
                    myTotalLength = myTotalData.length;
                    myLastSigUnitNumber = mySigUnitNumber;
                }
                myData = jpxiDataToken.getData();
                myDataLength = jpxiDataToken.getDataSize();
                myEod = jpxiDataToken.isEod();
                myTempData = myTotalData;
                myTotalData = new byte[myTotalLength + myDataLength];
                System.arraycopy(myTempData, 0, myTotalData, 0, myTotalLength);
                System.arraycopy(myData, 0, myTotalData, myTotalLength, myDataLength);
                myTotalLength = myTotalData.length;
            }
        } catch (ServiceNotAvailableException e) {
            return (false);
        } catch (JpxiException e) {
            return (false);
        }
        return (true);
    }

    /**
	 * done a DLTransaction and check if is ok
	 * @param theProxy
	 * @param theUID
	 * @return true if ok false otherwise
	 */
    public boolean dlTransactionTest(JpxiServiceDelegate theDelegate, UID theUID) {
        JpxiTransactionAck myAck = null;
        File myFile = new File(getDlDoc());
        File[] myFileList = null;
        boolean isDirectory = false;
        int nbFiles = 1;
        if (myFile.isDirectory()) {
            isDirectory = true;
            myFileList = myFile.listFiles();
        }
        if (isDirectory) {
            nbFiles = myFileList.length;
            setTotalPages(nbFiles);
        } else setTotalPages(1);
        setDocSize(0);
        int myNbFullRecord = 0;
        int myRestOfRecord = 0;
        int mySizeOfST33Array = 0;
        setTotalRecords(0);
        for (int i = 0; i < nbFiles; i++) {
            myNbFullRecord = 0;
            myRestOfRecord = 0;
            mySizeOfST33Array = 0;
            if (isDirectory) setDocSize((int) myFileList[i].length() + getDocSize()); else setDocSize((int) myFile.length());
            myNbFullRecord = getDocSize() / DATA_RECORD_LENGTH;
            myRestOfRecord = getDocSize() % DATA_RECORD_LENGTH;
            mySizeOfST33Array = myNbFullRecord * (DATA_RECORD_LENGTH + ST33_HEADER_LENGTH);
            if (myRestOfRecord != 0) {
                mySizeOfST33Array += ST33_HEADER_LENGTH + myRestOfRecord;
                setTotalRecords(myNbFullRecord + 1 + getTotalRecords());
            } else setTotalRecords(myNbFullRecord + getTotalRecords());
        }
        setTotalPages(nbFiles);
        String myXml = generateXmlDescr();
        JpxiDmsDbRec myDMS = new JpxiDmsDbRec(getDlDocId(), 0, new Timestamp(System.currentTimeMillis()), myXml);
        try {
            myAck = theDelegate.newDLTransaction(theUID, myDMS, getElectronicFormat(), getElectronicFormat(), "DEFAULT", getPublicationDate(), getCopyright());
        } catch (Exception e) {
            return (false);
        }
        for (int i = 0; i < nbFiles; i++) {
            int j = 0;
            int nbByteDone = 0;
            int nbByteToDo;
            int currentSize;
            if (isDirectory) {
                nbByteToDo = (int) myFileList[i].length();
                currentSize = nbByteToDo;
            } else {
                nbByteToDo = (int) myFile.length();
                currentSize = nbByteToDo;
            }
            RandomAccessFile myRandom = null;
            byte[] theData = new byte[nbByteToDo];
            try {
                if (isDirectory) myRandom = new RandomAccessFile(myFileList[i], "rw"); else myRandom = new RandomAccessFile(myFile, "rw");
            } catch (FileNotFoundException e2) {
                return (false);
            }
            try {
                myRandom.read(theData);
            } catch (IOException e1) {
                return (false);
            }
            while (nbByteToDo >= 20000) {
                byte[] myTempdata = new byte[20000];
                System.arraycopy(theData, j * 20000, myTempdata, 0, 20000);
                try {
                    theDelegate.write(myAck.getTransactionUID(), myTempdata);
                } catch (JpxiException e3) {
                    closeDlTrans(theDelegate, myAck.getTransactionUID());
                    return (false);
                } catch (ServiceNotAvailableException e) {
                    closeDlTrans(theDelegate, myAck.getTransactionUID());
                    return (false);
                }
                j++;
                nbByteDone = j * 20000;
                nbByteToDo = currentSize - nbByteDone;
            }
            if (currentSize > nbByteDone) {
                byte[] myTempdata = new byte[nbByteToDo];
                System.arraycopy(theData, j * 20000, myTempdata, 0, nbByteToDo);
                try {
                    theDelegate.write(myAck.getTransactionUID(), myTempdata);
                } catch (JpxiException e3) {
                    closeDlTrans(theDelegate, myAck.getTransactionUID());
                    return (false);
                } catch (ServiceNotAvailableException e) {
                    closeDlTrans(theDelegate, myAck.getTransactionUID());
                    return (false);
                }
            }
        }
        closeDlTrans(theDelegate, myAck.getTransactionUID());
        return (true);
    }

    /**
 * Close the DLTransaction
 * @param myUID the transaction UID
 * @param theProxy JpxiServiceDelegate
 */
    private void closeDlTrans(JpxiServiceDelegate theDelegate, UID myUID) {
        try {
            theDelegate.closeTrans(myUID);
        } catch (Exception e) {
            System.exit(1);
        }
    }

    /**
 * generate a xmlDescr according to the property data
 * @return String the generated xml
 *
 */
    private String generateXmlDescr() {
        StringBuffer myXmlDescr = new StringBuffer("<?xml version=\"1.0\" standalone=\"yes\"?>");
        myXmlDescr.append("<JpxiDocumentFunctionalInformation>");
        myXmlDescr.append("<electronicFormat>" + getElectronicFormat() + "</electronicFormat>");
        myXmlDescr.append("<copyrightStatus>" + getCopyright() + "</copyrightStatus>");
        myXmlDescr.append("<docQuality> </docQuality>");
        myXmlDescr.append("<totalPages>" + getTotalPages() + "</totalPages>");
        myXmlDescr.append("<docSize>" + getDocSize() + "</docSize>");
        myXmlDescr.append("<revisoryBulletin> </revisoryBulletin>");
        myXmlDescr.append("<totalRecords>" + getTotalRecords() + "</totalRecords>");
        myXmlDescr.append("<outStatus> </outStatus>");
        Timestamp myLoadingDate = new Timestamp(System.currentTimeMillis());
        myXmlDescr.append("<dateLoading>" + myLoadingDate.toString() + "</dateLoading>");
        if (getPublicationDate() != null) myLoadingDate = getPublicationDate();
        myXmlDescr.append("<datePublication>" + myLoadingDate + "</datePublication>");
        myXmlDescr.append("<JpxiDocumentFunctionalInformation>");
        return (myXmlDescr.toString());
    }

    /**
 * create a list of page to treat
 *
 */
    private void createRank() {
        LinkedList mySubSelectionList = new LinkedList();
        for (int i = 1; i <= getTotalPages(); i++) mySubSelectionList.addLast(new Integer(i));
        setRank(mySubSelectionList);
    }

    /**
	 * retrieve the electronic format and the total number of pages
	 *@return true if ok false otherwise
	 */
    private boolean saveDataFromDms() {
        String myXmlData = getMyDMS().getXmlDescr();
        if (parseFile(myXmlData) == false) {
            return (false);
        }
        return (true);
    }

    /**
 * parse a string and find the value of electronic format and the total number of pages tags
 * @param theXmlFile
 * @return true if ok false otherwise
 */
    private boolean parseFile(String theXmlFile) {
        int startElectronicFormat = -1;
        int endElectronicFormat = -1;
        int startTotalPages = -1;
        int endTotalPages = -1;
        int startDocSize = -1;
        int endDocSize = -1;
        int endCopyright = -1;
        int startCopyright = -1;
        int endPubDate = -1;
        int startPubDate = -1;
        int i = 0;
        int myXmlSize = theXmlFile.length();
        while (i <= myXmlSize) {
            if ((i <= (myXmlSize - BEGIN_ELECTRONIQUE_FORMAT_TAG.length())) && theXmlFile.substring(i, i + BEGIN_ELECTRONIQUE_FORMAT_TAG.length()).equalsIgnoreCase(BEGIN_ELECTRONIQUE_FORMAT_TAG)) {
                startElectronicFormat = i + BEGIN_ELECTRONIQUE_FORMAT_TAG.length();
            } else if ((i <= (myXmlSize - END_ELECTRONIQUE_FORMAT_TAG.length())) && theXmlFile.substring(i, i + END_ELECTRONIQUE_FORMAT_TAG.length()).equalsIgnoreCase(END_ELECTRONIQUE_FORMAT_TAG)) {
                endElectronicFormat = i;
            } else if ((i <= (myXmlSize - BEGIN_TOTAL_PAGES_TAG.length())) && theXmlFile.substring(i, i + BEGIN_TOTAL_PAGES_TAG.length()).equalsIgnoreCase(BEGIN_TOTAL_PAGES_TAG)) {
                startTotalPages = i + BEGIN_TOTAL_PAGES_TAG.length();
            } else if ((i <= (myXmlSize - END_TOTAL_PAGES_TAG.length())) && theXmlFile.substring(i, i + END_TOTAL_PAGES_TAG.length()).equalsIgnoreCase(END_TOTAL_PAGES_TAG)) {
                endTotalPages = i;
            } else if ((i <= (myXmlSize - BEGIN_DOC_SIZE_TAG.length())) && theXmlFile.substring(i, i + BEGIN_DOC_SIZE_TAG.length()).equalsIgnoreCase(BEGIN_DOC_SIZE_TAG)) {
                startDocSize = i + BEGIN_DOC_SIZE_TAG.length();
            } else if ((i <= (myXmlSize - END_DOC_SIZE_TAG.length())) && theXmlFile.substring(i, i + END_DOC_SIZE_TAG.length()).equalsIgnoreCase(END_DOC_SIZE_TAG)) {
                endDocSize = i;
            } else if ((i <= (myXmlSize - BEGIN_PUB_DATE_TAG.length())) && theXmlFile.substring(i, i + BEGIN_PUB_DATE_TAG.length()).equalsIgnoreCase(BEGIN_PUB_DATE_TAG)) {
                startPubDate = i + BEGIN_PUB_DATE_TAG.length();
            } else if ((i <= (myXmlSize - END_PUB_DATE_TAG.length())) && theXmlFile.substring(i, i + END_PUB_DATE_TAG.length()).equalsIgnoreCase(END_PUB_DATE_TAG)) {
                endPubDate = i;
            } else if ((i <= (myXmlSize - BEGIN_COPYRIGHT_TAG.length())) && theXmlFile.substring(i, i + BEGIN_COPYRIGHT_TAG.length()).equalsIgnoreCase(BEGIN_COPYRIGHT_TAG)) {
                startCopyright = i + BEGIN_COPYRIGHT_TAG.length();
            } else if ((i <= (myXmlSize - END_COPYRIGHT_TAG.length())) && theXmlFile.substring(i, i + END_COPYRIGHT_TAG.length()).equalsIgnoreCase(END_COPYRIGHT_TAG)) {
                endCopyright = i;
            }
            i++;
        }
        if (startElectronicFormat == -1 || endElectronicFormat == -1 || startTotalPages == -1 || endTotalPages == -1 || startDocSize == -1 || endDocSize == -1 || endPubDate == -1 || startPubDate == -1 || endCopyright == -1 || startCopyright == -1) {
            return (false);
        }
        setElectronicFormat(theXmlFile.substring(startElectronicFormat, endElectronicFormat).trim());
        setTotalPages(Integer.parseInt(theXmlFile.substring(startTotalPages, endTotalPages).trim()));
        setDocSize(Integer.parseInt(theXmlFile.substring(startDocSize, endDocSize).trim()));
        setPublicationDate(Timestamp.valueOf(theXmlFile.substring(startPubDate, endPubDate)));
        String myCopyright = theXmlFile.substring(startCopyright, endCopyright);
        if (myCopyright.length() == 1) setCopyright(myCopyright.charAt(0)); else if (myCopyright.length() == 0) setCopyright(' '); else {
            return (false);
        }
        return (true);
    }

    /**
	 * Create a new selection.
	 * Creation date: (01/10/2001 11:35:24)
	 * @return org.epo.jpxi.shared.JpxiDocSubSelection
	 */
    private JpxiDocSubSelection newSelection() {
        JpxiDocSubSelection subSelection;
        try {
            if (getSigType() == -1) subSelection = null; else subSelection = new JpxiDocSubSelection(getSigType(), getRank());
        } catch (Exception e) {
            subSelection = null;
        }
        return subSelection;
    }

    /**
	 * standard accessor
	 * @return userName
	 */
    public String getUserName() {
        return userName;
    }

    /**
	 * standard accessor
	 * @return String userPwd
	 */
    public String getUserPwd() {
        return userPwd;
    }

    /**
	 * standard accessor
	 * @param string String
	 */
    private void setUserName(String string) {
        userName = string;
    }

    /**
	 * standard accessor
	 * @param string String
	 */
    private void setUserPwd(String string) {
        userPwd = string;
    }

    /**
	 * standard accessor
	 * @return LinkedList
	 */
    public java.util.LinkedList getRank() {
        return rank;
    }

    /**
	 * standard accessor
	 * @return int
	 */
    public int getRupture() {
        return rupture;
    }

    /**
	 * standard accessor
	 * @param list LinkedList
	 */
    public void setRank(java.util.LinkedList list) {
        rank = list;
    }

    /**
	 * standard accessor
	 * @param newRupture String
	 */
    private void setRupture(String newRupture) {
        try {
            if (newRupture.equalsIgnoreCase("SIGTYPE_PAGE")) {
                rupture = JpxiDocSkeletonObj.SIGTYPE_PAGE;
                return;
            }
            if (newRupture.equalsIgnoreCase("SIGTYPE_ST33REC")) {
                rupture = JpxiDocSkeletonObj.SIGTYPE_ST33REC;
                return;
            }
            if (newRupture.equalsIgnoreCase("SIGTYPE_SUBPART")) {
                rupture = JpxiDocSkeletonObj.SIGTYPE_SUBPART;
                return;
            }
            if (newRupture.equalsIgnoreCase("SIGTYPE_PDF_ROOT")) {
                rupture = JpxiDocSkeletonObj.SIGTYPE_PDF_ROOT;
                return;
            }
            if (newRupture.equalsIgnoreCase("SIGTYPE_PDF_PAGES")) {
                rupture = JpxiDocSkeletonObj.SIGTYPE_PDF_PAGES;
                return;
            }
            if (newRupture.equalsIgnoreCase("SIGTYPE_PDF_PAGE")) {
                rupture = JpxiDocSkeletonObj.SIGTYPE_PDF_PAGE;
                return;
            }
            if (newRupture.equalsIgnoreCase("SIGTYPE_PDF_OBJ")) {
                rupture = JpxiDocSkeletonObj.SIGTYPE_PDF_OBJ;
                return;
            }
            if (newRupture.equalsIgnoreCase("SIGTYPE_TIFF_DATA")) {
                rupture = JpxiDocSkeletonObj.SIGTYPE_TIFF_DATA;
                return;
            }
            if (newRupture.equalsIgnoreCase("SIGTYPE_NULL")) {
                rupture = JpxiDocSkeletonObj.SIGTYPE_NULL;
                return;
            }
        } catch (Exception e) {
        }
    }

    /**
	 * standard accessor
	 * @return int
	 */
    public int getNbIteration() {
        return nbIteration;
    }

    /**
	 * standard accessor
	 * @return int
	 */
    public int getNbSession() {
        return nbSession;
    }

    /**
	 * standard accessor
	 * @param i int
	 */
    public void setNbIteration(int i) {
        nbIteration = i;
    }

    /**
	 * standard accessor
	 * @param i int
	 */
    public void setNbSession(int i) {
        nbSession = i;
    }

    /**
	 * standard accessor
	 * @return LinkedList
	 */
    public LinkedList getDmsList() {
        return dmsList;
    }

    /**
	 * standard accessor
	 * @param list LinkedList
	 */
    public void setDmsList(LinkedList list) {
        dmsList = list;
    }

    /**
	 * standard accessor
	 * @return int
	 */
    public int getSigType() {
        return sigType;
    }

    /**
	 * Standard Acessor, set the signet type.
	 * Creation date: (01/10/2001 11:30:43)
	 * @param newSignetType int
	 */
    private void setSigType(String newSignetType) {
        try {
            if (newSignetType.equalsIgnoreCase("SIGTYPE_PAGE")) {
                sigType = JpxiDocSkeletonObj.SIGTYPE_PAGE;
                return;
            }
            if (newSignetType.equalsIgnoreCase("SIGTYPE_ST33REC")) {
                sigType = JpxiDocSkeletonObj.SIGTYPE_ST33REC;
                return;
            }
            if (newSignetType.equalsIgnoreCase("SIGTYPE_SUBPART")) {
                sigType = JpxiDocSkeletonObj.SIGTYPE_SUBPART;
                return;
            }
            if (newSignetType.equalsIgnoreCase("SIGTYPE_PDF_ROOT")) {
                sigType = JpxiDocSkeletonObj.SIGTYPE_PDF_ROOT;
                return;
            }
            if (newSignetType.equalsIgnoreCase("SIGTYPE_PDF_PAGES")) {
                sigType = JpxiDocSkeletonObj.SIGTYPE_PDF_PAGES;
                return;
            }
            if (newSignetType.equalsIgnoreCase("SIGTYPE_PDF_PAGE")) {
                sigType = JpxiDocSkeletonObj.SIGTYPE_PDF_PAGE;
                return;
            }
            if (newSignetType.equalsIgnoreCase("SIGTYPE_PDF_OBJ")) {
                sigType = JpxiDocSkeletonObj.SIGTYPE_PDF_OBJ;
                return;
            }
            if (newSignetType.equalsIgnoreCase("SIGTYPE_TIFF_DATA")) {
                sigType = JpxiDocSkeletonObj.SIGTYPE_TIFF_DATA;
                return;
            }
            if (newSignetType.equalsIgnoreCase("SIGTYPE_NULL")) {
                sigType = JpxiDocSkeletonObj.SIGTYPE_NULL;
                return;
            }
            if (newSignetType.equalsIgnoreCase("FULLDOC")) {
                sigType = -2;
                return;
            }
        } catch (Exception e) {
        }
    }

    /**
	 * standard accessor
	 * @return String
	 */
    public String getElectronicFormat() {
        return electronicFormat;
    }

    /**
	 * standard accessor
	 * @return int
	 */
    public int getTotalPages() {
        return totalPages;
    }

    /**
	 * standard accessor
	 * @param string String
	 */
    public void setElectronicFormat(String string) {
        electronicFormat = string;
    }

    /**
	 * standard accessor
	 * @param int i
	 */
    public void setTotalPages(int i) {
        totalPages = i;
    }

    /**
	 * standard accessor
	 * @return JpxiDmsDbRec
	 */
    public JpxiDmsDbRec getMyDMS() {
        return myDMS;
    }

    /**
	 * standard accessor
	 * @param rec JpxiDmsDbRec
	 */
    public void setMyDMS(JpxiDmsDbRec rec) {
        myDMS = rec;
    }

    /**
	 * standard accessor
	 * @return int
	 */
    public int getBuffSize() {
        return buffSize;
    }

    /**
	 * standard accessor
	 * @param i int
	 */
    public void setBuffSize(int i) {
        buffSize = i;
    }

    /**
	 * standard accessor
	 * @param string String
	 */
    public void setTransacState(String string) {
        transacState = string;
    }

    /**
	 * standard accessor
	 * @return int
	 */
    public int getDocSize() {
        return docSize;
    }

    /**
	 * standard accessor
	 * @param i int
	 */
    public void setDocSize(int i) {
        docSize = i;
    }

    /**
	 * standard accessor
	 * @return char
	 */
    public char getCopyright() {
        return copyright;
    }

    /**
	 * standard accessor
	 * @return Timestamp
	 */
    public Timestamp getPublicationDate() {
        return publicationDate;
    }

    /**
	 * standard accessor
	 * @param c char
	 */
    public void setCopyright(char c) {
        copyright = c;
    }

    /**
	 * standard accessor
	 * @param timestamp TimeStamp
	 */
    public void setPublicationDate(Timestamp timestamp) {
        publicationDate = timestamp;
    }

    /**
	 * standard accessor
	 * @return String
	 */
    public String getDaDocId() {
        return daDocId;
    }

    /**
	 * standard accessor
	 * @param string String
	 */
    public void setDaDocId(String string) {
        daDocId = string;
    }

    /**
	 * standard accessor
	 * @return String
	 */
    public String getDlDocId() {
        return dlDocId;
    }

    /**
	 * standard accessor
	 * @param string String
	 */
    public void setDlDocId(String string) {
        dlDocId = string;
    }

    /**
	 * standard accessor
	 * @return int
	 */
    public int getTotalRecords() {
        return totalRecords;
    }

    /**
	 * standard accessor
	 * @param i int
	 */
    public void setTotalRecords(int i) {
        totalRecords = i;
    }

    /**
	 * standard accessor
	 * @return String
	 */
    public String getDlDoc() {
        return dlDoc;
    }

    /**
	 * standard accessor
	 * @param string 
	 */
    public void setDlDoc(String string) {
        dlDoc = string;
    }
}
