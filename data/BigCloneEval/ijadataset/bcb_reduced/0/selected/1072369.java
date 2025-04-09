package ejb.bprocess.acquisitions;

import ejb.bprocess.util.ADM_FORM_LETTER_CREATOR;
import ejb.bprocess.util.DBConnector;
import ejb.bprocess.util.HomeFactory;
import ejb.bprocess.util.NewGenLibRoot;
import ejb.bprocess.util.NewGenXMLGenerator;
import ejb.objectmodel.acquisitions.ACQ_ON_APPROVALKey;
import ejb.objectmodel.acquisitions.ACQ_REQUESTKey;
import ejb.objectmodel.acquisitions.ACQ_REQUEST_AMKey;
import ejb.objectmodel.acquisitions.ACQ_SOLI_REQUESTKey;
import ejb.objectmodel.acquisitions.LocalACQ_ON_APPROVAL;
import ejb.objectmodel.acquisitions.LocalACQ_ON_APPROVALHome;
import ejb.objectmodel.acquisitions.LocalACQ_REQUEST;
import ejb.objectmodel.acquisitions.LocalACQ_REQUESTHome;
import ejb.objectmodel.acquisitions.LocalACQ_REQUEST_AM;
import ejb.objectmodel.acquisitions.LocalACQ_REQUEST_AMHome;
import ejb.objectmodel.acquisitions.LocalACQ_SOLI_REQUEST;
import ejb.objectmodel.acquisitions.LocalACQ_SOLI_REQUESTHome;
import ejb.objectmodel.administration.ACC_CREDIT_NOTEKey;
import ejb.objectmodel.administration.ACC_DEPOSITARY_ACCOUNTKey;
import ejb.objectmodel.administration.ACC_LIBRARY_BUDGETKey;
import ejb.objectmodel.administration.Corporate_Name_AFKey;
import ejb.objectmodel.administration.DEPTKey;
import ejb.objectmodel.administration.FORM_LETTER_FORMATKey;
import ejb.objectmodel.administration.LocalACC_BUDGET_APPROVAL;
import ejb.objectmodel.administration.LocalACC_BUDGET_APPROVALHome;
import ejb.objectmodel.administration.LocalACC_BUDGET_HEAD;
import ejb.objectmodel.administration.LocalACC_BUDGET_HEADHome;
import ejb.objectmodel.administration.LocalACC_BUDGET_TRANSACTIONHome;
import ejb.objectmodel.administration.LocalACC_CREDIT_NOTE;
import ejb.objectmodel.administration.LocalACC_CREDIT_NOTEHome;
import ejb.objectmodel.administration.LocalACC_CREDIT_NOTE_TRANSACTIONHome;
import ejb.objectmodel.administration.LocalACC_DEPOSITARY_ACCOUNT;
import ejb.objectmodel.administration.LocalACC_DEPOSITARY_ACCOUNTHome;
import ejb.objectmodel.administration.LocalACC_DEPOSITARY_ACCOUNT_TRANSACTIONHome;
import ejb.objectmodel.administration.LocalACC_FORIEGN_EXCHANGEHome;
import ejb.objectmodel.administration.LocalACC_LIBRARY_BUDGET;
import ejb.objectmodel.administration.LocalACC_LIBRARY_BUDGETHome;
import ejb.objectmodel.administration.LocalADM_CO_MATERIAL_TYPEHome;
import ejb.objectmodel.administration.LocalADM_CO_VENDOR;
import ejb.objectmodel.administration.LocalADM_CO_VENDORHome;
import ejb.objectmodel.administration.LocalADM_FORM_LETTERHome;
import ejb.objectmodel.administration.LocalCorporate_Name_AF;
import ejb.objectmodel.administration.LocalCorporate_Name_AFHome;
import ejb.objectmodel.administration.LocalDEPT;
import ejb.objectmodel.administration.LocalDEPTHome;
import ejb.objectmodel.administration.LocalFORM_LETTER_FORMAT;
import ejb.objectmodel.administration.LocalFORM_LETTER_FORMATHome;
import ejb.objectmodel.administration.LocalGENERAL_SETUP_PMT;
import ejb.objectmodel.administration.LocalGENERAL_SETUP_PMTHome;
import ejb.objectmodel.administration.LocalMAIL_SENDERHome;
import ejb.objectmodel.administration.LocalMeeting_Name_AF;
import ejb.objectmodel.administration.LocalMeeting_Name_AFHome;
import ejb.objectmodel.administration.LocalPatron;
import ejb.objectmodel.administration.LocalPatronHome;
import ejb.objectmodel.administration.LocalPatron_Category;
import ejb.objectmodel.administration.LocalPatron_CategoryHome;
import ejb.objectmodel.administration.LocalPersonal_name_AF;
import ejb.objectmodel.administration.LocalPersonal_name_AFHome;
import ejb.objectmodel.administration.LocalSearchable_Series_Ass;
import ejb.objectmodel.administration.LocalSearchable_Series_AssHome;
import ejb.objectmodel.administration.LocalSeries_Name_AF;
import ejb.objectmodel.administration.LocalSeries_Name_AFHome;
import ejb.objectmodel.administration.Meeting_Name_AFKey;
import ejb.objectmodel.administration.PatronKey;
import ejb.objectmodel.administration.Patron_CategoryKey;
import ejb.objectmodel.administration.Personal_name_AFKey;
import ejb.objectmodel.administration.Series_Name_AFKey;
import ejb.objectmodel.cataloguing.DocumentKey;
import ejb.objectmodel.cataloguing.LocalCAT_SERIALHome;
import ejb.objectmodel.cataloguing.LocalCAT_VOLUMEHome;
import ejb.objectmodel.cataloguing.LocalDocument;
import ejb.objectmodel.cataloguing.LocalDocumentHome;
import ejb.objectmodel.cataloguing.LocalSearchable_CatalogueRecord;
import ejb.objectmodel.cataloguing.LocalSearchable_CatalogueRecordHome;
import ejb.objectmodel.cataloguing.LocalSearchable_ISBN;
import ejb.objectmodel.cataloguing.LocalSearchable_ISBNHome;
import ejb.objectmodel.cataloguing.Searchable_CatalogueRecordKey;
import ejb.objectmodel.circulation.LocalCIR_WEEDOUT;
import ejb.objectmodel.circulation.LocalCIR_WEEDOUTHome;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import javax.ejb.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;

/**
 * Created Sep 29, 2003 4:45:31 PM
 * Code generated by the Sun ONE Studio EJB Builder
 * @author N VASU PRAVEEN
 */
public class UtilityBean implements SessionBean {

    private SessionContext context;

    private ejb.bprocess.util.Utility utility = null;

    private HomeFactory homeFactory = null;

    private NewGenXMLGenerator newGenXMLGenerator = null;

    static final int BUFFER = 2048;

    /**
     * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
     */
    public void setSessionContext(SessionContext aContext) {
        context = aContext;
        utility = ejb.bprocess.util.Utility.getInstance();
        homeFactory = HomeFactory.getInstance();
        newGenXMLGenerator = NewGenXMLGenerator.getInstance();
    }

    /**
     * @see javax.ejb.SessionBean#ejbActivate()
     */
    public void ejbActivate() {
    }

    /**
     * @see javax.ejb.SessionBean#ejbPassivate()
     */
    public void ejbPassivate() {
    }

    /**
     * @see javax.ejb.SessionBean#ejbRemove()
     */
    public void ejbRemove() {
    }

    /**
     * See section 7.10.3 of the EJB 2.0 specification
     */
    public void ejbCreate() {
    }

    public String getBudgetHeads(String xmlStr) {
        Element root = newGenXMLGenerator.getRootElementFromXMLDocument(xmlStr);
        Integer libID = new Integer(root.getChildText("LibraryID"));
        root = new Element("Response");
        Object[] objLocal = new Object[1];
        try {
            objLocal = ((LocalACC_BUDGET_HEADHome) HomeFactory.getInstance().getHome("ACC_BUDGET_HEAD")).findAll(libID).toArray();
        } catch (FinderException ex) {
        }
        Object[] bheads = new Object[objLocal.length];
        for (int i = 0; i < objLocal.length; i++) {
            bheads[i] = ((LocalACC_BUDGET_HEAD) objLocal[i]).getBudget_Head();
        }
        java.util.Arrays.sort(bheads, java.text.Collator.getInstance());
        for (int i = 0; i < bheads.length; i++) {
            Element element = new Element("BudgetHead");
            element.setText(String.valueOf(bheads[i]));
            root.addContent(element);
        }
        return newGenXMLGenerator.buildXMLDocument(root);
    }

    public String getCurrencyCodes(String xmlStr) {
        Element root = newGenXMLGenerator.getRootElementFromXMLDocument(xmlStr);
        Integer libID = new Integer(root.getChildText("LibraryID"));
        root = new Element("Response");
        Object[] objLocal = new Object[1];
        try {
            objLocal = ((LocalACC_FORIEGN_EXCHANGEHome) HomeFactory.getInstance().getHome("ACC_FORIEGN_EXCHANGE")).getDistinctCurrencyCodes(libID).toArray();
        } catch (FinderException ex) {
        }
        Object[] currcodes = new Object[objLocal.length];
        for (int i = 0; i < objLocal.length; i++) {
            currcodes[i] = objLocal[i].toString();
        }
        java.util.Arrays.sort(currcodes, java.text.Collator.getInstance());
        for (int i = 0; i < currcodes.length; i++) {
            Element element = new Element("CurrencyCode");
            element.setText(currcodes[i].toString());
            root.addContent(element);
        }
        return newGenXMLGenerator.buildXMLDocument(root);
    }

    public Hashtable getCatalogueRecord(Integer catalogueRecordID, Integer ownerLibID) {
        Hashtable ht = new Hashtable();
        ht.put("Author", "");
        ht.put("Title", "");
        ht.put("Edition", "");
        ht.put("Publisher", "");
        ht.put("ISBN", "");
        ht.put("ISSN", "");
        ht.put("Series", "");
        try {
            LocalSearchable_CatalogueRecord localSCatRec = null;
            Searchable_CatalogueRecordKey scatRecKey = new Searchable_CatalogueRecordKey();
            scatRecKey.catalogueRecordId = catalogueRecordID;
            scatRecKey.owner_Library_Id = ownerLibID;
            localSCatRec = ((LocalSearchable_CatalogueRecordHome) homeFactory.getHome("Searchable_CatalogueRecord")).findByPrimaryKey(scatRecKey);
            String title = localSCatRec.getTitle245a();
            String author = "";
            String publisher = utility.getTestedString(localSCatRec.getPublisher());
            String edition = utility.getTestedString(localSCatRec.getEdition());
            if (utility.getTestedString("" + localSCatRec.getPersonalName_Library_Id()).length() == 0) {
                if (utility.getTestedString("" + localSCatRec.getMeetingName_Library_Id()).length() == 0) {
                    if (utility.getTestedString("" + localSCatRec.getCorporateName_Library_Id()).length() != 0) {
                        LocalCorporate_Name_AF localCorporate_Name_AF = null;
                        Corporate_Name_AFKey corporate_Name_AFKey = new Corporate_Name_AFKey();
                        corporate_Name_AFKey.corporate_Name_Id = localSCatRec.getCorporate_Name_Id();
                        corporate_Name_AFKey.library_Id = localSCatRec.getCorporateName_Library_Id();
                        try {
                            localCorporate_Name_AF = ((LocalCorporate_Name_AFHome) homeFactory.getHome("Corporate_Name_AF")).findByPrimaryKey(corporate_Name_AFKey);
                            author = localCorporate_Name_AF.getName();
                        } catch (Exception ex) {
                            ex.printStackTrace(System.out);
                        }
                    }
                } else {
                    LocalMeeting_Name_AF localMeeting_Name_AF = null;
                    Meeting_Name_AFKey meeting_Name_AFKey = new Meeting_Name_AFKey();
                    meeting_Name_AFKey.meeting_Name_Id = localSCatRec.getMeeting_Name_Id();
                    meeting_Name_AFKey.library_Id = localSCatRec.getMeetingName_Library_Id();
                    try {
                        localMeeting_Name_AF = ((LocalMeeting_Name_AFHome) homeFactory.getHome("Meeting_Name_AF")).findByPrimaryKey(meeting_Name_AFKey);
                        author = localMeeting_Name_AF.getName();
                    } catch (Exception ex) {
                        ex.printStackTrace(System.out);
                    }
                }
            } else {
                LocalPersonal_name_AF localPersonal_name_AF = null;
                Personal_name_AFKey personal_name_AFKey = new Personal_name_AFKey();
                personal_name_AFKey.personal_name_Id = localSCatRec.getPersonal_name_Id();
                personal_name_AFKey.library_Id = localSCatRec.getPersonalName_Library_Id();
                try {
                    localPersonal_name_AF = ((LocalPersonal_name_AFHome) homeFactory.getHome("Personal_name_AF")).findByPrimaryKey(personal_name_AFKey);
                    author = localPersonal_name_AF.getName();
                } catch (Exception ex) {
                    ex.printStackTrace(System.out);
                }
            }
            ht.put("Author", author);
            ht.put("Title", title);
            ht.put("Publisher", publisher);
            ht.put("Edition", edition);
            Object[] ObjLocalSearchableSeriesAss = new Object[0];
            try {
                ObjLocalSearchableSeriesAss = ((LocalSearchable_Series_AssHome) homeFactory.getHome("Searchable_Series_Ass")).findByCatalogueRecordIDLibraryID(catalogueRecordID, ownerLibID).toArray();
            } catch (FinderException ex) {
            }
            String series = "";
            for (int i = 0; i < ObjLocalSearchableSeriesAss.length; i++) {
                LocalSearchable_Series_Ass local = (LocalSearchable_Series_Ass) ObjLocalSearchableSeriesAss[i];
                Series_Name_AFKey key = new Series_Name_AFKey();
                key.library_Id = local.getCatalogueRecordId();
                key.series_Name_Id = local.getOwner_Library_Id();
                LocalSeries_Name_AF localSeries_Name_AF = null;
                try {
                    localSeries_Name_AF = ((LocalSeries_Name_AFHome) homeFactory.getHome("Series_Name_AF")).findByPrimaryKey(key);
                    if (i == 0) series = utility.getTestedString(localSeries_Name_AF.getTitle()); else if (i == ObjLocalSearchableSeriesAss.length - 1) series += utility.getTestedString(localSeries_Name_AF.getTitle()); else series += utility.getTestedString(localSeries_Name_AF.getTitle()) + ", ";
                } catch (FinderException ex) {
                }
            }
            ht.put("Series", series);
            Object[] ObjLocalSearchableISBN = new Object[0];
            try {
                ObjLocalSearchableISBN = ((LocalSearchable_ISBNHome) homeFactory.getHome("Searchable_ISBN")).findByCatalogueRecordIDLibraryID(catalogueRecordID, ownerLibID).toArray();
            } catch (FinderException ex) {
            }
            String ISBN = "";
            for (int i = 0; i < ObjLocalSearchableISBN.length; i++) {
                LocalSearchable_ISBN local = (LocalSearchable_ISBN) ObjLocalSearchableISBN[i];
                if (i == 0) ISBN = utility.getTestedString(local.getIsbn()); else if (i == ObjLocalSearchableSeriesAss.length - 1) ISBN += utility.getTestedString(local.getIsbn()); else ISBN += utility.getTestedString(local.getIsbn()) + ", ";
            }
            ht.put("ISBN", ISBN);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        } finally {
            return ht;
        }
    }

    public Hashtable getPatronDetails(String patID, Integer patLibID) {
        Hashtable ht = new Hashtable();
        try {
            PatronKey patKey = new PatronKey();
            patKey.library_Id = patLibID;
            patKey.patron_Id = patID;
            LocalPatron localPatron = ((LocalPatronHome) homeFactory.getHome("Patron")).findByPrimaryKey(patKey);
            ht.put("LibraryID", localPatron.getLibrary_Id());
            ht.put("PatronName", localPatron.getFname() + " " + utility.getTestedString(localPatron.getMname()) + " " + utility.getTestedString((localPatron.getLname())));
            ht.put("StartDate", "" + localPatron.getMembership_Start_Date().getTime());
            ht.put("EndDate", "" + localPatron.getMembership_Expiry_Date().getTime());
            ht.put("Delinquency", (localPatron.getDelinquency_Reason() == null ? "" : localPatron.getDelinquency_Reason()));
            Integer patCatID = localPatron.getPatron_Category_Id();
            Integer patDeptID = localPatron.getDept_Id();
            ht.put("CategoryID", patCatID);
            ht.put("DeptID", patDeptID);
            ht.put("EMail", utility.getTestedString("" + localPatron.getEmail()));
            ht.put("CommEMail", localPatron.getComm_Email());
            ht.put("CommPrint", localPatron.getComm_Print());
            ht.put("CommInstantMsg", localPatron.getComm_Instant_Msg());
            ht.put("PatronID", patID);
            if (patCatID.intValue() == 4) {
                if (utility.getTestedString("" + localPatron.getLibrary_Patron_Id()).length() != 0) {
                    ht.put("Network", "Y");
                    ht.put("PatronLibraryID", localPatron.getLibrary_Patron_Id());
                } else {
                    ht.put("Network", "N");
                    ht.put("PatronLibraryID", localPatron.getOther_Library_Patron_Id());
                }
            }
            String dept = "";
            try {
                DEPTKey DEPTKey = new DEPTKey();
                DEPTKey.library_Id = patLibID;
                DEPTKey.dept_Id = patDeptID;
                LocalDEPT localDEPT = ((LocalDEPTHome) homeFactory.getHome("DEPT")).findByPrimaryKey(DEPTKey);
                ht.put("DeptName", localDEPT.getDept_Name());
            } catch (Exception ex) {
            }
            String patCat = "";
            try {
                Patron_CategoryKey patCatKey = new Patron_CategoryKey();
                patCatKey.library_Id = patLibID;
                patCatKey.patron_Category_Id = patCatID;
                LocalPatron_Category localPatronCategory = ((LocalPatron_CategoryHome) homeFactory.getHome("Patron_Category")).findByPrimaryKey(patCatKey);
                ht.put("CategoryName", localPatronCategory.getPatron_Category_Name());
            } catch (FinderException ex) {
            }
        } catch (Exception ex) {
        } finally {
            return ht;
        }
    }

    public String getBudgetApprovingAuthoritiesForTheBudgetHeads(String xmlStr) {
        Element root = newGenXMLGenerator.getRootElementFromXMLDocument(xmlStr);
        Integer libID = new Integer(root.getChildText("LibraryID"));
        Object[] object = root.getChildren("BudgetHead").toArray();
        Vector vector = new Vector();
        for (int i = 0; i < object.length; i++) {
            vector.addElement(utility.getBudgetApprovingAuthoritiesForTheBudgetHead(libID, ((Element) object[i]).getText()));
        }
        Vector vPatron = new Vector(1, 1);
        Vector vLibID = new Vector(1, 1);
        if (vector.size() > 0) {
            Vector vAuthority = (Vector) vector.elementAt(0);
            for (int i = 0; i < vAuthority.size(); i += 2) {
                boolean existsInAll = false;
                String match = vAuthority.elementAt(i).toString() + vAuthority.elementAt(i + 1).toString();
                if (vector.size() > 1) {
                    for (int j = 1; j < vector.size(); j++) {
                        Vector vAuthority1 = (Vector) vector.elementAt(j);
                        Vector vCheck = new Vector(1, 1);
                        for (int k = 0; k < vAuthority1.size(); k += 2) {
                            vCheck.addElement(vAuthority1.elementAt(k).toString() + vAuthority1.elementAt(k + 1).toString());
                        }
                        if (vCheck.contains(match)) {
                            existsInAll = true;
                        } else {
                            existsInAll = false;
                            break;
                        }
                    }
                } else {
                    existsInAll = true;
                }
                if (existsInAll && !vPatron.contains(vAuthority.elementAt(i))) {
                    vPatron.addElement(vAuthority.elementAt(i));
                    vLibID.addElement(vAuthority.elementAt(i + 1));
                }
            }
        }
        root = new Element("Response");
        Element element = null;
        for (int i = 0; i < vPatron.size(); i += 1) {
            element = new Element("Patron");
            String patronID = vPatron.elementAt(i).toString();
            Integer libraryID = new Integer(vLibID.elementAt(i).toString());
            Hashtable ht = getPatronDetails(patronID, libraryID);
            Element subElement = new Element("Name");
            subElement.setText(ht.get("PatronName").toString());
            element.addContent(subElement);
            subElement = new Element("LibraryID");
            subElement.setText(libraryID.toString());
            element.addContent(subElement);
            subElement = new Element("PatronID");
            subElement.setText(patronID);
            element.addContent(subElement);
            root.addContent(element);
        }
        return newGenXMLGenerator.buildXMLDocument(root);
    }

    public String getBudgetHeadsForBudgetApprovingAuthorityForThisLibrary(String xmlStr) {
        Element root = newGenXMLGenerator.getRootElementFromXMLDocument(xmlStr);
        Integer libID = new Integer(root.getChildText("LibraryID"));
        String approvalID = root.getChildText("ApprovalID");
        Integer approvalLibID = new Integer(root.getChildText("ApprovalLibraryID"));
        root = new Element("Response");
        Object[] objLocal = new Object[1];
        try {
            objLocal = ((LocalACC_BUDGET_APPROVALHome) HomeFactory.getInstance().getHome("ACC_BUDGET_APPROVAL")).findBudgetHeadsForApprovingAuthorityForThisLibrary(approvalLibID, approvalID, libID).toArray();
        } catch (FinderException ex) {
        }
        for (int i = 0; i < objLocal.length; i++) {
            Element element = new Element("BudgetHead");
            element.setText(((LocalACC_BUDGET_APPROVAL) objLocal[i]).getBudget_Head());
            root.addContent(element);
        }
        return newGenXMLGenerator.buildXMLDocument(root);
    }

    public String getFormContent(String[] arguments, String formFormat) {
        return MessageFormat.format(formFormat, arguments);
    }

    public Hashtable getFormLetterDetails(Integer libID, Integer formLetterID) throws FinderException {
        Hashtable ht = new Hashtable();
        FORM_LETTER_FORMATKey key = new FORM_LETTER_FORMATKey();
        key.format_Id = formLetterID;
        key.library_Id = libID;
        LocalFORM_LETTER_FORMAT local = null;
        local = ((LocalFORM_LETTER_FORMATHome) homeFactory.getHome("FORM_LETTER_FORMAT")).findByPrimaryKey(key);
        ht.put("FormLetterNumber", utility.getTestedString(local.getPrefix()) + local.getMax_No().intValue());
        ht.put("FormLetterFormat", utility.getTestedString(local.getFormat()));
        ht.put("Title", utility.getTestedString(local.getTitle()));
        local.setMax_No(new Integer(local.getMax_No().intValue() + 1));
        return ht;
    }

    public Hashtable addMailToBatch(Hashtable htMailDetails, String[] formContentArguments, Hashtable htReturn) throws CreateException, FinderException {
        Integer libID = new Integer("" + htMailDetails.get("LibraryID"));
        Integer patLibID = new Integer("" + htMailDetails.get("PatronLibraryID"));
        String emailID = "" + htMailDetails.get("EMail");
        Hashtable htForm = getFormLetterDetails(libID, new Integer("" + htMailDetails.get("FormLetterID")));
        String mailContent = getFormContent(formContentArguments, "" + htForm.get("FormLetterFormat"));
        String formLetterNumber = "" + htForm.get("FormLetterNumber");
        htReturn.put("FormLetterNumber", formLetterNumber);
        String print = "N";
        if (utility.getTestedString(htMailDetails.get("CommPrint")).equals("Y")) {
            print = "Y";
        }
        String email = "N";
        if (utility.getTestedString(htMailDetails.get("CommEMail")).equals("Y")) {
            email = "Y";
        }
        htReturn.put("EMailDispatched", email);
        htReturn.put("PrintJobAdded", print);
        htReturn.put("MailRecipent", htMailDetails.get("PatronName"));
        htReturn.put("MailRecipentLibraryID", patLibID);
        int shipID = utility.getShipID(libID.intValue());
        htReturn.put("ShipID", "" + shipID);
        ((LocalMAIL_SENDERHome) homeFactory.getHome("MAIL_SENDER")).create(libID, new Integer(shipID), utility.getTimestamp(), formLetterNumber, utility.getTimestamp(), "" + htMailDetails.get("Subject"), mailContent, "" + htMailDetails.get("PatronID"), patLibID, email, print, "" + htMailDetails.get("EntryID"), utility.getTimestamp());
        return htReturn;
    }

    public ArrayList updateBudgetDatabase(Element root, char expOrComm) throws CreateException, FinderException {
        ArrayList arrayList = new ArrayList();
        Integer libID = new Integer(root.getChildText("LibraryID"));
        Vector vBudgetID = new Vector();
        Vector vBudgetAmt = new Vector();
        Object[] obj = root.getChild("BudgetDetails").getChildren("Budget").toArray();
        for (int i = 0; i < obj.length; i++) {
            vBudgetID.addElement(((Element) obj[i]).getChildText("BudgetID"));
            vBudgetAmt.addElement(((Element) obj[i]).getChildText("Amount"));
        }
        Timestamp commitDate = null;
        Hashtable htPayslip = new Hashtable();
        String invoiceNumber = "";
        String paySlipNumber = "";
        if (expOrComm == 'C') {
            commitDate = utility.getTimestamp("" + root.getChildText("CommitDate"));
        } else if (expOrComm == 'E') {
            htPayslip = utility.getFormLetterDetails(libID.intValue(), 2);
            paySlipNumber = "" + htPayslip.get("FormLetterNumber");
            invoiceNumber = utility.getTestedString(root.getChildText("InvoiceNumber"));
        }
        arrayList.add(paySlipNumber);
        for (int i = 0; i < vBudgetID.size(); i++) {
            double budgetAmt = Double.parseDouble("" + vBudgetAmt.get(i));
            Timestamp budgetTaDate = utility.getTimestampWithoutTime();
            Integer budgetTaID = new Integer(utility.getBudgetTransactionID(libID.intValue()));
            arrayList.add(budgetTaID);
            ((LocalACC_BUDGET_TRANSACTIONHome) homeFactory.getHome("ACC_BUDGET_TRANSACTION")).create(libID, budgetTaID, budgetTaDate, new BigDecimal("" + vBudgetAmt.elementAt(i)), "D", "" + vBudgetID.elementAt(i), "" + expOrComm, commitDate, paySlipNumber, root.getChildText("EntryID"), budgetTaDate);
            ACC_LIBRARY_BUDGETKey aCC_LIBRARY_BUDGETKey = new ACC_LIBRARY_BUDGETKey();
            aCC_LIBRARY_BUDGETKey.library_Id = libID;
            aCC_LIBRARY_BUDGETKey.budget_Id = "" + vBudgetID.elementAt(i);
            LocalACC_LIBRARY_BUDGET localACC_LIBRARY_BUDGET = ((LocalACC_LIBRARY_BUDGETHome) homeFactory.getHome("ACC_LIBRARY_BUDGET")).findByPrimaryKey(aCC_LIBRARY_BUDGETKey);
            double alcAmt = localACC_LIBRARY_BUDGET.getBudget_Allocated_Amt().doubleValue();
            double balAmt = localACC_LIBRARY_BUDGET.getBalance_Amt().doubleValue();
            double expComAmt = 0.0;
            if (expOrComm == 'E') {
                expComAmt = localACC_LIBRARY_BUDGET.getExpenditure_Amt().doubleValue();
            } else if (expOrComm == 'C') {
                expComAmt = localACC_LIBRARY_BUDGET.getCommitted_Amt().doubleValue();
            }
            expComAmt += budgetAmt;
            balAmt -= expComAmt;
            if (expOrComm == 'E') {
                localACC_LIBRARY_BUDGET.setExpenditure_Amt(new BigDecimal(expComAmt));
            } else if (expOrComm == 'C') {
                localACC_LIBRARY_BUDGET.setCommitted_Amt(new BigDecimal(expComAmt));
            }
            localACC_LIBRARY_BUDGET.setBalance_Amt(new BigDecimal(balAmt));
        }
        return arrayList;
    }

    public Integer getVendorID(Integer libID, String vendorName) {
        LocalADM_CO_VENDOR local = null;
        try {
            local = ((LocalADM_CO_VENDORHome) HomeFactory.getInstance().getHome("ADM_CO_VENDOR")).findByVendorName(libID, vendorName);
            return local.getVendor_Id();
        } catch (FinderException ex) {
            return null;
        }
    }

    public String getCreditAndDepositDetailsForTheVendor(String xmlStr) {
        Element root = newGenXMLGenerator.getRootElementFromXMLDocument(xmlStr);
        Integer libID = new Integer(root.getChildText("LibraryID"));
        String vendorName = root.getChildText("VendorName");
        root = new Element("Response");
        Integer vendorID = getVendorID(libID, vendorName);
        Object[] obLocalDeposit = new Object[0];
        try {
            obLocalDeposit = ((LocalACC_DEPOSITARY_ACCOUNTHome) HomeFactory.getInstance().getHome("ACC_DEPOSITARY_ACCOUNT")).findByVendorIDAndLibraryID(vendorID, libID).toArray();
        } catch (FinderException ex) {
        }
        Element element1 = new Element("DepositDetails");
        for (int j = 0; j < obLocalDeposit.length; j++) {
            LocalACC_DEPOSITARY_ACCOUNT localACC_DEPOSITARY_ACCOUNT = (LocalACC_DEPOSITARY_ACCOUNT) obLocalDeposit[j];
            Element subElement = new Element("Deposit");
            Element subElement1 = new Element("DepositID");
            subElement1.setText(localACC_DEPOSITARY_ACCOUNT.getDeposit_Id());
            subElement.addContent(subElement1);
            subElement1 = new Element("CommittedAmount");
            subElement1.setText(localACC_DEPOSITARY_ACCOUNT.getCommitted_Amount().toString());
            subElement.addContent(subElement1);
            subElement1 = new Element("ExpenditureAmount");
            subElement1.setText(localACC_DEPOSITARY_ACCOUNT.getExpenditure_Amount().toString());
            subElement.addContent(subElement1);
            subElement1 = new Element("BalanceAmount");
            subElement1.setText(localACC_DEPOSITARY_ACCOUNT.getBalance_Amount().toString());
            subElement.addContent(subElement1);
            element1.addContent(subElement);
        }
        root.addContent(element1);
        Object[] obLocalCredit = new Object[0];
        try {
            obLocalCredit = ((LocalACC_CREDIT_NOTEHome) HomeFactory.getInstance().getHome("ACC_CREDIT_NOTE")).findByVendorIDAndLibraryID(vendorID, libID).toArray();
        } catch (FinderException ex) {
        }
        Element element2 = new Element("CreditDetails");
        for (int j = 0; j < obLocalCredit.length; j++) {
            LocalACC_CREDIT_NOTE localACC_CREDIT_NOTE = (LocalACC_CREDIT_NOTE) obLocalCredit[j];
            Element subElement = new Element("Credit");
            Element subElement1 = new Element("CreditID");
            subElement1.setText(localACC_CREDIT_NOTE.getCredit_Id().toString());
            subElement.addContent(subElement1);
            subElement1 = new Element("CommittedAmount");
            subElement1.setText(localACC_CREDIT_NOTE.getCommitted_Amount().toString());
            subElement.addContent(subElement1);
            subElement1 = new Element("ExpenditureAmount");
            subElement1.setText(localACC_CREDIT_NOTE.getExpenditure_Amount().toString());
            subElement.addContent(subElement1);
            subElement1 = new Element("BalanceAmount");
            subElement1.setText(localACC_CREDIT_NOTE.getBalance_Amount().toString());
            subElement.addContent(subElement1);
            element2.addContent(subElement);
        }
        root.addContent(element2);
        return newGenXMLGenerator.buildXMLDocument(root);
    }

    public String getVendors(String xmlStr) {
        Element root = newGenXMLGenerator.getRootElementFromXMLDocument(xmlStr);
        Integer libID = new Integer(root.getChildText("LibraryID"));
        Object[] obLocal = new Object[0];
        try {
            obLocal = ((LocalADM_CO_VENDORHome) HomeFactory.getInstance().getHome("ADM_CO_VENDOR")).findAll(libID).toArray();
        } catch (FinderException ex) {
        }
        Vector vector1 = new Vector(1, 1);
        Vector vector2 = new Vector(1, 1);
        for (int i = 0; i < obLocal.length; i++) {
            vector1.addElement(((LocalADM_CO_VENDOR) obLocal[i]).getVendor_Id());
            vector2.addElement(((LocalADM_CO_VENDOR) obLocal[i]).getName());
        }
        return newGenXMLGenerator.buildXMLDocument("Vendor", new String[] { "ID", "Name" }, new Vector[] { vector1, vector2 });
    }

    public String getNewOrderNo(String xmlStr) {
        Hashtable ht = newGenXMLGenerator.parseXMLDocument(xmlStr);
        Integer libID = new Integer(ht.get("LibraryID").toString());
        try {
            xmlStr = newGenXMLGenerator.buildXMLDocument(getFormLetterDetails(libID, new Integer(12)));
        } catch (FinderException ex) {
            xmlStr = "";
        }
        return xmlStr;
    }

    public String getConversionRateForCurrency(String xmlStr) {
        Hashtable ht = newGenXMLGenerator.parseXMLDocument(xmlStr);
        Integer libID = new Integer(ht.get("LibraryID").toString());
        String currencyCode = ht.get("CurrencyCode").toString();
        double conversionRate = utility.getConversionRateForCurrency(libID, currencyCode);
        ht = new Hashtable();
        ht.put("ConversionRate", "" + conversionRate);
        return newGenXMLGenerator.buildXMLDocument(ht);
    }

    public ArrayList updateBudgetDepositCreditDatabase(Element root, char expOrComm) throws CreateException, FinderException {
        ArrayList arrayList = new ArrayList();
        ArrayList budgetArrayList = new ArrayList();
        ArrayList depositArrayList = new ArrayList();
        ArrayList creditArrayList = new ArrayList();
        Integer libID = new Integer(root.getChildText("LibraryID"));
        Timestamp commitDate = null;
        Hashtable htPayslip = new Hashtable();
        String invoiceNumber = "";
        String paySlipNumber = "";
        if (expOrComm == 'C') {
            commitDate = utility.getTimestamp("" + root.getChildText("CommitDate"));
        } else if (expOrComm == 'E') {
            htPayslip = getFormLetterDetails(libID, new Integer(2));
            paySlipNumber = "" + htPayslip.get("FormLetterNumber");
            invoiceNumber = utility.getTestedString(root.getChildText("InvoiceNumber"));
        }
        arrayList.add(paySlipNumber);
        Vector vBudgetID = new Vector();
        Vector vBudgetAmt = new Vector();
        Vector vBudgetLibID = new Vector();
        Object[] obj = root.getChild("BudgetDetails").getChildren("Budget").toArray();
        for (int i = 0; i < obj.length; i++) {
            vBudgetID.addElement(((Element) obj[i]).getChildText("BudgetID"));
            vBudgetAmt.addElement(((Element) obj[i]).getChildText("Amount"));
            vBudgetLibID.addElement(((Element) obj[i]).getChildText("BudgetLibID"));
        }
        for (int i = 0; i < vBudgetID.size(); i++) {
            Integer budgetLibID = new Integer(vBudgetLibID.elementAt(i).toString());
            double budgetAmt = Double.parseDouble("" + vBudgetAmt.get(i));
            Timestamp budgetTaDate = utility.getTimestampWithoutTime();
            Integer budgetTaID = new Integer(utility.getBudgetTransactionID(budgetLibID.intValue()));
            budgetArrayList.add(budgetLibID);
            budgetArrayList.add(budgetTaID);
            ((LocalACC_BUDGET_TRANSACTIONHome) homeFactory.getHome("ACC_BUDGET_TRANSACTION")).create(budgetLibID, budgetTaID, budgetTaDate, new BigDecimal("" + vBudgetAmt.elementAt(i)), "D", "" + vBudgetID.elementAt(i), "" + expOrComm, commitDate, paySlipNumber, root.getChildText("EntryID"), budgetTaDate);
            ACC_LIBRARY_BUDGETKey aCC_LIBRARY_BUDGETKey = new ACC_LIBRARY_BUDGETKey();
            aCC_LIBRARY_BUDGETKey.library_Id = budgetLibID;
            aCC_LIBRARY_BUDGETKey.budget_Id = "" + vBudgetID.elementAt(i);
            LocalACC_LIBRARY_BUDGET localACC_LIBRARY_BUDGET = ((LocalACC_LIBRARY_BUDGETHome) homeFactory.getHome("ACC_LIBRARY_BUDGET")).findByPrimaryKey(aCC_LIBRARY_BUDGETKey);
            double alcAmt = localACC_LIBRARY_BUDGET.getBudget_Allocated_Amt().doubleValue();
            double balAmt = localACC_LIBRARY_BUDGET.getBalance_Amt().doubleValue();
            double expComAmt = 0.0;
            if (expOrComm == 'E') {
                expComAmt = localACC_LIBRARY_BUDGET.getExpenditure_Amt().doubleValue();
            } else if (expOrComm == 'C') {
                expComAmt = localACC_LIBRARY_BUDGET.getCommitted_Amt().doubleValue();
            }
            expComAmt += budgetAmt;
            balAmt -= expComAmt;
            if (expOrComm == 'E') {
                localACC_LIBRARY_BUDGET.setExpenditure_Amt(new BigDecimal(expComAmt));
            } else if (expOrComm == 'C') {
                localACC_LIBRARY_BUDGET.setCommitted_Amt(new BigDecimal(expComAmt));
            }
            localACC_LIBRARY_BUDGET.setBalance_Amt(new BigDecimal(balAmt));
        }
        arrayList.add(budgetArrayList);
        Vector vDepositID = new Vector();
        Vector vDepositAmt = new Vector();
        Vector vDepositLibID = new Vector();
        Object[] obDeposit = root.getChild("DepositDetails").getChildren("Deposit").toArray();
        for (int i = 0; i < obDeposit.length; i++) {
            vDepositID.addElement(((Element) obDeposit[i]).getChildText("DepositID"));
            vDepositAmt.addElement(((Element) obDeposit[i]).getChildText("Amount"));
            vDepositLibID.addElement(((Element) obDeposit[i]).getChildText("DepositLibID"));
        }
        for (int i = 0; i < vDepositID.size(); i++) {
            Integer depositLibID = new Integer(vDepositLibID.elementAt(i).toString());
            double depositAmt = Double.parseDouble("" + vDepositAmt.elementAt(i));
            Timestamp depositTaDate = utility.getTimestampWithoutTime();
            Integer depositTaID = new Integer(utility.getDepositTransactionID(depositLibID.intValue()));
            depositArrayList.add(depositLibID);
            depositArrayList.add(depositTaID);
            ((LocalACC_DEPOSITARY_ACCOUNT_TRANSACTIONHome) homeFactory.getHome("ACC_DEPOSITARY_ACCOUNT_TRANSACTION")).create(depositLibID, depositTaID, depositTaDate, new BigDecimal("" + vDepositAmt.elementAt(i)), "D", "" + vDepositID.elementAt(i), "" + expOrComm, commitDate, paySlipNumber, root.getChildText("EntryID"), depositTaDate);
            ACC_DEPOSITARY_ACCOUNTKey aCC_DEPOSITARY_ACCOUNTKey = new ACC_DEPOSITARY_ACCOUNTKey();
            aCC_DEPOSITARY_ACCOUNTKey.library_Id = depositLibID;
            aCC_DEPOSITARY_ACCOUNTKey.deposit_Id = "" + vDepositID.elementAt(i);
            LocalACC_DEPOSITARY_ACCOUNT localACC_DEPOSITARY_ACCOUNT = ((LocalACC_DEPOSITARY_ACCOUNTHome) homeFactory.getHome("ACC_DEPOSITARY_ACCOUNT")).findByPrimaryKey(aCC_DEPOSITARY_ACCOUNTKey);
            double alcAmt = localACC_DEPOSITARY_ACCOUNT.getAmount().doubleValue();
            double balAmt = localACC_DEPOSITARY_ACCOUNT.getBalance_Amount().doubleValue();
            double expComAmt = 0.0;
            if (expOrComm == 'E') {
                expComAmt = localACC_DEPOSITARY_ACCOUNT.getExpenditure_Amount().doubleValue();
            } else if (expOrComm == 'C') {
                expComAmt = localACC_DEPOSITARY_ACCOUNT.getCommitted_Amount().doubleValue();
            }
            expComAmt += depositAmt;
            balAmt -= expComAmt;
            if (expOrComm == 'E') {
                localACC_DEPOSITARY_ACCOUNT.setExpenditure_Amount(new BigDecimal(expComAmt));
            } else if (expOrComm == 'C') {
                localACC_DEPOSITARY_ACCOUNT.setCommitted_Amount(new BigDecimal(expComAmt));
            }
            localACC_DEPOSITARY_ACCOUNT.setBalance_Amount(new BigDecimal(balAmt));
        }
        arrayList.add(depositArrayList);
        Vector vCreditID = new Vector();
        Vector vCreditAmt = new Vector();
        Vector vCreditLibID = new Vector();
        Object[] obCredit = root.getChild("CreditDetails").getChildren("Credit").toArray();
        for (int i = 0; i < obCredit.length; i++) {
            vCreditID.addElement(((Element) obCredit[i]).getChildText("CreditID"));
            vCreditAmt.addElement(((Element) obCredit[i]).getChildText("Amount"));
            vCreditLibID.addElement(((Element) obCredit[i]).getChildText("CreditLibID"));
        }
        for (int i = 0; i < vCreditID.size(); i++) {
            Integer creditLibID = new Integer(vCreditLibID.elementAt(i).toString());
            double creditAmt = Double.parseDouble("" + vCreditAmt.elementAt(i));
            Timestamp creditTaDate = utility.getTimestampWithoutTime();
            Integer creditTaID = new Integer(utility.getCreditTransactionID(creditLibID.intValue()));
            creditArrayList.add(creditLibID);
            creditArrayList.add(creditTaID);
            ((LocalACC_CREDIT_NOTE_TRANSACTIONHome) homeFactory.getHome("ACC_CREDIT_NOTE_TRANSACTION")).create(creditLibID, creditTaID, creditTaDate, new BigDecimal("" + vCreditAmt.elementAt(i)), "D", new Integer(vCreditID.elementAt(i).toString()), "" + expOrComm, commitDate, paySlipNumber, root.getChildText("EntryID"), creditTaDate);
            ACC_CREDIT_NOTEKey aCC_CREDIT_NOTEKey = new ACC_CREDIT_NOTEKey();
            aCC_CREDIT_NOTEKey.library_Id = creditLibID;
            aCC_CREDIT_NOTEKey.credit_Id = new Integer(vCreditID.elementAt(i).toString());
            LocalACC_CREDIT_NOTE localACC_CREDIT_NOTE = ((LocalACC_CREDIT_NOTEHome) homeFactory.getHome("ACC_CREDIT_NOTE")).findByPrimaryKey(aCC_CREDIT_NOTEKey);
            double alcAmt = localACC_CREDIT_NOTE.getAmount().doubleValue();
            double balAmt = localACC_CREDIT_NOTE.getBalance_Amount().doubleValue();
            double expComAmt = 0.0;
            if (expOrComm == 'E') {
                expComAmt = localACC_CREDIT_NOTE.getExpenditure_Amount().doubleValue();
            } else if (expOrComm == 'C') {
                expComAmt = localACC_CREDIT_NOTE.getCommitted_Amount().doubleValue();
            }
            expComAmt += creditAmt;
            balAmt -= expComAmt;
            if (expOrComm == 'E') {
                localACC_CREDIT_NOTE.setExpenditure_Amount(new BigDecimal(expComAmt));
            } else if (expOrComm == 'C') {
                localACC_CREDIT_NOTE.setCommitted_Amount(new BigDecimal(expComAmt));
            }
            localACC_CREDIT_NOTE.setBalance_Amount(new BigDecimal(balAmt));
        }
        arrayList.add(creditArrayList);
        return arrayList;
    }

    public String getVendorName(Integer library_Id, Integer vendor_Id) {
        Object[] obLocal = new Object[0];
        try {
            obLocal = ((LocalADM_CO_VENDORHome) HomeFactory.getInstance().getHome("ADM_CO_VENDOR")).findByName(library_Id, vendor_Id).toArray();
        } catch (FinderException ex) {
            ex.printStackTrace();
        }
        String VendorName = ((LocalADM_CO_VENDOR) obLocal[0]).getName();
        return VendorName;
    }

    public String getBarcodesAssociatedWithCatalogueRecord(String xmlStr) {
        Hashtable ht = newGenXMLGenerator.parseXMLDocument(xmlStr);
        Integer catRecID = new Integer(ht.get("CatalogueRecordID").toString());
        Integer ownerLibID = new Integer(ht.get("OwnerLibraryID").toString());
        Integer libID = new Integer(ht.get("LibraryID").toString());
        ht = utility.getBarcodesAssociatedWithCatalogueRecord(libID, catRecID, ownerLibID);
        return newGenXMLGenerator.buildXMLDocument(ht);
    }

    public String identifyTheDocumentsForWeedingout(String xmlStr) {
        Element root = newGenXMLGenerator.getRootElementFromXMLDocument(xmlStr);
        Integer libID = new Integer(root.getChildText("LibraryID"));
        Object[] object = new Object[0];
        object = root.getChildren("CatalogueRecord").toArray();
        root = new Element("Response");
        Element element1 = new Element("Success");
        for (int i = 0; i < object.length; i++) {
            Element element = (Element) object[i];
            Integer catRecID = new Integer(element.getChildText("CatalogueRecordID"));
            Integer ownerLibID = new Integer(element.getChildText("OwnerLibraryID"));
            try {
                ((LocalCIR_WEEDOUTHome) HomeFactory.getInstance().getHome("CIR_WEEDOUT")).create(libID, catRecID, ownerLibID, "A", root.getChildText("EntryID"), utility.getTimestamp());
            } catch (DuplicateKeyException ex) {
                context.setRollbackOnly();
                ex.printStackTrace();
                element1.setText("ND");
                break;
            } catch (CreateException ex) {
                context.setRollbackOnly();
                ex.printStackTrace();
                element1.setText("NC");
                break;
            }
            element1.setText("Y");
        }
        root.addContent(element1);
        return newGenXMLGenerator.buildXMLDocument(root);
    }

    public String getCatalogueRecordsIdentifiedForWeedout(String xmlStr) {
        Hashtable ht = newGenXMLGenerator.parseXMLDocument(xmlStr);
        Integer libID = new Integer(ht.get("LibraryID").toString());
        Object[] obLocal = new Object[0];
        Vector vector1 = new Vector();
        Vector vector2 = new Vector();
        Vector vector3 = new Vector();
        Vector vector4 = new Vector();
        try {
            obLocal = ((LocalCIR_WEEDOUTHome) HomeFactory.getInstance().getHome("CIR_WEEDOUT")).findByLibraryIDAndStatus(libID, "A").toArray();
            for (int i = 0; i < obLocal.length; i++) {
                LocalCIR_WEEDOUT localCIR_WEEDOUT = (LocalCIR_WEEDOUT) obLocal[i];
                Integer catRecID = localCIR_WEEDOUT.getCatalogue_Record_Id();
                Integer ownerLibID = localCIR_WEEDOUT.getOwner_Library_Id();
                ht = getCatalogueRecord(catRecID, ownerLibID);
                vector1.addElement(catRecID);
                vector2.addElement(ownerLibID);
                vector3.addElement(ht.get("Title"));
                vector4.addElement(ht.get("Author"));
            }
        } catch (FinderException ex) {
            ex.printStackTrace();
        } finally {
            return newGenXMLGenerator.buildXMLDocument("CatalogueRecord", new String[] { "CatalogueRecordID", "OwnerLibraryID", "Title", "Author" }, new Vector[] { vector1, vector2, vector3, vector4 });
        }
    }

    public String weedoutCatalogueRecord(String xmlStr) {
        Element root = newGenXMLGenerator.getRootElementFromXMLDocument(xmlStr);
        Integer libID = new Integer(root.getChildText("LibraryID"));
        Object[] object = new Object[0];
        object = root.getChildren("CatalogueRecord").toArray();
        root = new Element("Response");
        Element element1 = new Element("Success");
        for (int i = 0; i < object.length; i++) {
            Element element = (Element) object[i];
            Integer catRecID = new Integer(element.getChildText("CatalogueRecordID"));
            Integer ownerLibID = new Integer(element.getChildText("OwnerLibraryID"));
            try {
                ((LocalCIR_WEEDOUTHome) HomeFactory.getInstance().getHome("CIR_WEEDOUT")).findByLibraryIDCatalogueRecordIDOWnerLibraryID(libID, catRecID, ownerLibID).setStatus("B");
            } catch (FinderException ex) {
                ex.printStackTrace();
                element1.setText("N");
                break;
            }
            if (utility.weedoutBarcodes(libID, catRecID, ownerLibID)) {
                element1.setText("Y");
            } else {
                element1.setText("N");
                break;
            }
        }
        root.addContent(element1);
        return newGenXMLGenerator.buildXMLDocument(root);
    }

    public String getMaterialTypeName(Integer materialTypeID) {
        try {
            return ((LocalADM_CO_MATERIAL_TYPEHome) homeFactory.getHome("ADM_CO_MATERIAL_TYPE")).findByPrimaryKey(materialTypeID).getMaterial_Type();
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            return "";
        }
    }

    public Hashtable getCloseFormLetterDetails(Integer library_Id, Integer form_Id) {
        Hashtable ht = new Hashtable();
        FORM_LETTER_FORMATKey key = new FORM_LETTER_FORMATKey();
        key.format_Id = form_Id;
        key.library_Id = library_Id;
        LocalFORM_LETTER_FORMAT local = null;
        try {
            local = ((LocalFORM_LETTER_FORMATHome) homeFactory.getHome("FORM_LETTER_FORMAT")).findByPrimaryKey(key);
        } catch (FinderException ex) {
            ex.printStackTrace();
        }
        ht.put("FormLetterNumber", local.getPrefix() + local.getMax_No().intValue());
        ht.put("FormLetterFormat", utility.getTestedString(local.getFormat()));
        ht.put("Title", utility.getTestedString(local.getTitle()));
        local.setMax_No(new Integer(local.getMax_No().intValue()));
        return ht;
    }

    public String generateOODocument(Integer formLetterId, Integer libraryId, Hashtable parameters) {
        String generatedFileAbsolutePath = "";
        try {
            boolean flag1 = true;
            int countFile1 = 0;
            String fileValid = "";
            while (flag1) {
                fileValid = NewGenLibRoot.getRoot() + ResourceBundle.getBundle("server").getString("Reports");
                fileValid += "/" + System.currentTimeMillis() + "_" + countFile1 + ".sxw";
                File thisDirec = new File(fileValid);
                if (thisDirec.exists()) {
                    countFile1++;
                    continue;
                } else {
                    flag1 = false;
                    break;
                }
            }
            int formLetterIdInt = formLetterId.intValue();
            String fileName = "";
            switch(formLetterIdInt) {
                case 46:
                    {
                        fileName = NewGenLibRoot.getRoot() + ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "46_FirmOrderPurchase.sxw";
                        break;
                    }
            }
            BufferedInputStream is = null;
            ZipEntry entry;
            JarFile zipfile = new JarFile(fileName);
            Enumeration e = zipfile.entries();
            String subTempDirectory = "";
            boolean flag = true;
            int countFile = 0;
            while (flag) {
                String direcValid = NewGenLibRoot.getRoot() + ResourceBundle.getBundle("server").getString("NewGenLibFiles") + "/Temp";
                direcValid += "/" + System.currentTimeMillis() + "_" + countFile;
                File thisDirec = new File(direcValid);
                if (thisDirec.exists()) {
                    countFile++;
                    continue;
                } else {
                    subTempDirectory = direcValid;
                    String forpic = direcValid + "/Pictures";
                    direcValid += "/META-INF";
                    File thisDirecWithMeta = new File(direcValid);
                    thisDirecWithMeta.mkdirs();
                    thisDirecWithMeta = new File(forpic);
                    thisDirecWithMeta.mkdirs();
                    flag = false;
                    break;
                }
            }
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                is = new BufferedInputStream(zipfile.getInputStream(entry));
                int count;
                byte data[] = new byte[BUFFER];
                File tempDirectory = new File(NewGenLibRoot.getRoot() + ResourceBundle.getBundle("server").getString("NewGenLibFiles") + "/Temp");
                if (!tempDirectory.exists()) tempDirectory.mkdirs();
                FileOutputStream fos = new FileOutputStream(subTempDirectory + "/" + entry.getName());
                BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
                while ((count = is.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
                is.close();
            }
            Object[] obx = new Object[parameters.size()];
            Enumeration enumParam = parameters.keys();
            while (enumParam.hasMoreElements()) {
                String key = enumParam.nextElement().toString();
                Object val = parameters.get(key);
                if (val.getClass().getName().equals("java.util.Hashtable")) {
                    Hashtable htMain = (Hashtable) val;
                    String[] headerValues = null;
                    Integer[] columnSizes = null;
                    Vector vecData = null;
                    if (htMain.get("Header") != null) {
                        headerValues = (String[]) htMain.get("Header");
                    }
                    if (htMain.get("ColumnSizes") != null) {
                        columnSizes = (Integer[]) htMain.get("ColumnSizes");
                    }
                    vecData = (Vector) htMain.get("Data");
                    Namespace ns = Namespace.getNamespace("table", "todelete");
                    Namespace nstext = Namespace.getNamespace("text", "todelete");
                    Element rootele = new Element("table", ns);
                    rootele.setAttribute("name", "newgenlibTable", ns);
                    rootele.setAttribute("style-name", "newgenlibTable", ns);
                    String[] rowdataone = (String[]) vecData.elementAt(0);
                    Element tablecolumn = new Element("table-column", ns);
                    tablecolumn.setAttribute("style-name", "newgenlibTable", ns);
                    tablecolumn.setAttribute("number-columns-repeated", String.valueOf(rowdataone.length), ns);
                    rootele.addContent(tablecolumn);
                    for (int i = 0; i < vecData.size(); i++) {
                        String[] rowdata = (String[]) vecData.elementAt(i);
                        Element tablerow = new Element("table-row", ns);
                        for (int j = 0; j < rowdata.length; j++) {
                            Element tablecell = new Element("table-cell", ns);
                            tablecell.setAttribute("style-name", "newgenlibTable", ns);
                            tablecell.setAttribute("value-type", "string", ns);
                            Element celltext = new Element("p", nstext);
                            celltext.setAttribute("style-name", "Table Contents", nstext);
                            celltext.setText(rowdata[j]);
                            tablecell.addContent(celltext);
                            tablerow.addContent(tablecell);
                        }
                        rootele.addContent(tablerow);
                    }
                    Document doc = new Document(rootele);
                    XMLOutputter xout = new XMLOutputter();
                    xout.setOmitDeclaration(true);
                    xout.setOmitEncoding(true);
                    String finalstr = xout.outputString(doc);
                    finalstr = finalstr.replaceAll("xmlns=todelete", "");
                    obx[Integer.parseInt(key)] = "</text:p>" + finalstr + "<text:p>";
                } else {
                    obx[Integer.parseInt(key)] = val.toString();
                }
            }
            BufferedInputStream origin = null;
            FileOutputStream dest1 = new FileOutputStream(fileValid);
            JarOutputStream out = new JarOutputStream(new BufferedOutputStream(dest1));
            byte data[] = new byte[BUFFER];
            File f = new File(subTempDirectory);
            String files[] = f.list();
            File[] allfiles = f.listFiles();
            Vector vecfiles = new Vector(1, 1);
            for (int i = 0; i < allfiles.length; i++) {
                if (allfiles[i].isDirectory()) {
                    File[] subfiles = allfiles[i].listFiles();
                    for (int j = 0; j < subfiles.length; j++) {
                        vecfiles.addElement(allfiles[i].getName() + "/" + subfiles[j].getName());
                    }
                } else {
                    vecfiles.addElement(allfiles[i].getName());
                }
            }
            files = new String[vecfiles.size()];
            for (int i = 0; i < vecfiles.size(); i++) {
                files[i] = vecfiles.elementAt(i).toString();
            }
            for (int i = 0; i < files.length; i++) {
                File file = new File(subTempDirectory + "/" + files[i]);
                if (!file.isDirectory()) {
                    FileInputStream fi = new FileInputStream(subTempDirectory + "/" + files[i]);
                    String str = "";
                    if (file.getName().equals("content.xml")) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(fi));
                        while (br.ready()) {
                            str += br.readLine();
                        }
                        br.close();
                        MessageFormat mf = new MessageFormat(str);
                        str = mf.format(str, obx);
                        FileOutputStream fo = new FileOutputStream(subTempDirectory + "/" + files[i]);
                        fo.write(str.getBytes());
                    }
                    origin = new BufferedInputStream(fi, BUFFER);
                    if (!fi.getFD().valid()) {
                        fi.close();
                        fi = new FileInputStream(subTempDirectory + "/" + files[i]);
                        origin = new BufferedInputStream(fi, BUFFER);
                    }
                    JarEntry entryx = new JarEntry(files[i]);
                    out.putNextEntry(entryx);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    origin.close();
                }
            }
            out.close();
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return generatedFileAbsolutePath;
    }

    public String getCustomColumns(String libraryId, String category) {
        Integer libraryIdInt = new Integer(libraryId);
        String xml = "";
        try {
            LocalGENERAL_SETUP_PMT local = ((LocalGENERAL_SETUP_PMTHome) HomeFactory.getInstance().getHome("GENERAL_SETUP_PMT")).findByLibraryID(libraryIdInt);
            String s1 = local.getPat_Field_Cust();
            String s2 = local.getHold_Field_Cust();
            ejb.bprocess.util.Utility utility = ejb.bprocess.util.Utility.getInstance();
            if (category.trim().equals("PATRON")) {
                xml = utility.getTestedString(s1);
            } else if (category.trim().equals("HOLDINGS")) {
                xml = utility.getTestedString(s2);
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return xml;
    }

    public String removeOnApprovalEntry(String xmlStr) {
        Element response = new Element("Response");
        Element success = new Element("Success");
        try {
            Element request = newGenXMLGenerator.getRootElementFromXMLDocument(xmlStr);
            String mode = request.getChildText("Mode");
            if (mode.equals("B")) {
                String approvalId = request.getChildText("Id");
                String libId = request.getChildText("LibId");
                ACQ_ON_APPROVALKey acqApprovalKey = new ACQ_ON_APPROVALKey();
                acqApprovalKey.on_Approval_Id = new Integer(approvalId);
                acqApprovalKey.library_Id = new Integer(libId);
                LocalACQ_ON_APPROVAL localAcqOnApproval = ((LocalACQ_ON_APPROVALHome) homeFactory.getHome("ACQ_ON_APPROVAL")).findByPrimaryKey(acqApprovalKey);
                if (localAcqOnApproval != null) {
                    localAcqOnApproval.remove();
                    success.setText("SUCCESS");
                } else {
                    success.setText("NOTFOUND");
                }
            }
        } catch (Exception e) {
            success.setText("FAILED");
        }
        response.addContent(success);
        return newGenXMLGenerator.buildXMLDocument(response);
    }

    public String getBibliographicDetails(String xmlStr) {
        Element response = new Element("Response");
        Element success = new Element("Success");
        try {
            Element request = newGenXMLGenerator.getRootElementFromXMLDocument(xmlStr);
            String mode = request.getChildText("Mode");
            Integer id = null;
            if (mode.equals("D")) {
                ACQ_REQUEST_AMKey acqRequestAmKey = new ACQ_REQUEST_AMKey();
                acqRequestAmKey.request_Id = new Integer(request.getChildText("Id"));
                acqRequestAmKey.library_Id = new Integer(request.getChildText("LibId"));
                LocalACQ_REQUEST_AM localAcqRequestAm = ((LocalACQ_REQUEST_AMHome) homeFactory.getHome("ACQ_REQUEST_AM")).findByPrimaryKey(acqRequestAmKey);
                if (localAcqRequestAm != null) {
                    if (localAcqRequestAm.getRequest_Type().equals("F")) {
                        mode = "A";
                        id = localAcqRequestAm.getAss_Request_Id();
                    } else if (localAcqRequestAm.getRequest_Type().equals("O")) {
                        mode = "B";
                        id = localAcqRequestAm.getOn_Approval_Id();
                    }
                }
            }
            if (mode.equals("A")) {
                ACQ_REQUESTKey acqRequestKey = new ACQ_REQUESTKey();
                if (id != null) acqRequestKey.request_Id = id; else acqRequestKey.request_Id = new Integer(request.getChildText("Id"));
                acqRequestKey.library_Id = new Integer(request.getChildText("LibId"));
                LocalACQ_REQUEST localAcqRequest = ((LocalACQ_REQUESTHome) homeFactory.getHome("ACQ_REQUEST")).findByPrimaryKey(acqRequestKey);
                if (localAcqRequest != null) {
                    Element title = new Element("Title");
                    title.setText(utility.getTestedString(localAcqRequest.getTitle()));
                    response.addContent(title);
                    Element author = new Element("Author");
                    author.setText(utility.getTestedString(localAcqRequest.getAuthor()));
                    response.addContent(author);
                    Element edition = new Element("Edition");
                    edition.setText(utility.getTestedString(localAcqRequest.getEdition()));
                    response.addContent(edition);
                    Element isbn = new Element("ISBN");
                    isbn.setText(utility.getTestedString(localAcqRequest.getIsbn()));
                    response.addContent(isbn);
                    Element series = new Element("Series");
                    series.setText(utility.getTestedString(localAcqRequest.getSeries()));
                    response.addContent(series);
                    Element publishYear = new Element("PublishYear");
                    publishYear.setText(utility.getTestedString(localAcqRequest.getPublish_Year()));
                    response.addContent(publishYear);
                    Element publisher = new Element("Publisher");
                    publisher.setText(utility.getTestedString(localAcqRequest.getPublisher()));
                    response.addContent(publisher);
                    Element publishPlace = new Element("PublishPlace");
                    publishPlace.setText(utility.getTestedString(localAcqRequest.getPublish_Place()));
                    response.addContent(publishPlace);
                    Element volume = new Element("Volume");
                    volume.setText(utility.getTestedString(localAcqRequest.getVolume_No()));
                    response.addContent(volume);
                }
            } else if (mode.equals("B")) {
                ACQ_ON_APPROVALKey acqOnApprovalKey = new ACQ_ON_APPROVALKey();
                if (id != null) acqOnApprovalKey.on_Approval_Id = id; else acqOnApprovalKey.on_Approval_Id = new Integer(request.getChildText("Id"));
                acqOnApprovalKey.library_Id = new Integer(request.getChildText("LibId"));
                LocalACQ_ON_APPROVAL localAcqOnApproval = ((LocalACQ_ON_APPROVALHome) homeFactory.getHome("ACQ_ON_APPROVAL")).findByPrimaryKey(acqOnApprovalKey);
                if (localAcqOnApproval != null) {
                    Element title = new Element("Title");
                    title.setText(utility.getTestedString(localAcqOnApproval.getTitle()));
                    response.addContent(title);
                    Element author = new Element("Author");
                    author.setText(utility.getTestedString(localAcqOnApproval.getAuthor()));
                    response.addContent(author);
                    Element edition = new Element("Edition");
                    edition.setText(utility.getTestedString(localAcqOnApproval.getEdition()));
                    response.addContent(edition);
                    Element isbn = new Element("ISBN");
                    isbn.setText(utility.getTestedString(localAcqOnApproval.getIsbn()));
                    response.addContent(isbn);
                    Element series = new Element("Series");
                    series.setText(utility.getTestedString(localAcqOnApproval.getSeries()));
                    response.addContent(series);
                    Element publishYear = new Element("PublishYear");
                    publishYear.setText(utility.getTestedString(localAcqOnApproval.getPublish_Year()));
                    response.addContent(publishYear);
                    Element publisher = new Element("Publisher");
                    publisher.setText(utility.getTestedString(localAcqOnApproval.getPublisher()));
                    response.addContent(publisher);
                    Element publishPlace = new Element("PublishPlace");
                    publishPlace.setText(utility.getTestedString(localAcqOnApproval.getPublish_Place()));
                    response.addContent(publishPlace);
                    Element volume = new Element("Volume");
                    volume.setText(utility.getTestedString(localAcqOnApproval.getVolume_No()));
                    response.addContent(volume);
                }
            } else if (mode.equals("C")) {
                ACQ_SOLI_REQUESTKey acqSoliRequestKey = new ACQ_SOLI_REQUESTKey();
                acqSoliRequestKey.request_Id = new Integer(request.getChildText("Id"));
                acqSoliRequestKey.library_Id = new Integer(request.getChildText("LibId"));
                LocalACQ_SOLI_REQUEST localAcqSoliRequest = ((LocalACQ_SOLI_REQUESTHome) homeFactory.getHome("ACQ_SOLI_REQUEST")).findByPrimaryKey(acqSoliRequestKey);
                if (localAcqSoliRequest != null) {
                    Element title = new Element("Title");
                    title.setText(utility.getTestedString(localAcqSoliRequest.getTitle()));
                    response.addContent(title);
                    Element author = new Element("Author");
                    author.setText(utility.getTestedString(localAcqSoliRequest.getAuthor()));
                    response.addContent(author);
                    Element edition = new Element("Edition");
                    edition.setText(utility.getTestedString(localAcqSoliRequest.getEdition()));
                    response.addContent(edition);
                    Element isbn = new Element("ISBN");
                    isbn.setText(utility.getTestedString(localAcqSoliRequest.getIsbn()));
                    response.addContent(isbn);
                    Element series = new Element("Series");
                    series.setText("");
                    response.addContent(series);
                    Element publishYear = new Element("PublishYear");
                    publishYear.setText(utility.getTestedString(localAcqSoliRequest.getPublish_Year()));
                    response.addContent(publishYear);
                    Element publisher = new Element("Publisher");
                    publisher.setText(utility.getTestedString(localAcqSoliRequest.getPublisher()));
                    response.addContent(publisher);
                    Element publishPlace = new Element("PublishPlace");
                    publishPlace.setText(utility.getTestedString(localAcqSoliRequest.getPublish_Place()));
                    response.addContent(publishPlace);
                    Element volume = new Element("Volume");
                    volume.setText(utility.getTestedString(localAcqSoliRequest.getVolume_No()));
                    response.addContent(volume);
                }
            }
            success.setText("Y");
        } catch (Exception e) {
            success.setText("N");
            e.printStackTrace();
        }
        response.addContent(success);
        return newGenXMLGenerator.buildXMLDocument(response);
    }

    public String modifyBibliographicDetails(String xmlStr) {
        Element success = new Element("Success");
        try {
            Element request = newGenXMLGenerator.getRootElementFromXMLDocument(xmlStr);
            String mode = request.getChildText("Mode");
            Integer id = null;
            if (mode.equals("D")) {
                ACQ_REQUEST_AMKey acqRequestAmKey = new ACQ_REQUEST_AMKey();
                acqRequestAmKey.request_Id = new Integer(request.getChildText("Id"));
                acqRequestAmKey.library_Id = new Integer(request.getChildText("LibId"));
                LocalACQ_REQUEST_AM localAcqRequestAm = ((LocalACQ_REQUEST_AMHome) homeFactory.getHome("ACQ_REQUEST_AM")).findByPrimaryKey(acqRequestAmKey);
                if (localAcqRequestAm != null) {
                    if (localAcqRequestAm.getRequest_Type().equals("F")) {
                        mode = "A";
                        id = localAcqRequestAm.getAss_Request_Id();
                    } else if (localAcqRequestAm.getRequest_Type().equals("O")) {
                        mode = "B";
                        id = localAcqRequestAm.getOn_Approval_Id();
                    }
                }
            }
            if (mode.equals("A")) {
                ACQ_REQUESTKey acqRequestKey = new ACQ_REQUESTKey();
                if (id != null) acqRequestKey.request_Id = id; else acqRequestKey.request_Id = new Integer(request.getChildText("Id"));
                acqRequestKey.library_Id = new Integer(request.getChildText("LibId"));
                LocalACQ_REQUEST localAcqRequest = ((LocalACQ_REQUESTHome) homeFactory.getHome("ACQ_REQUEST")).findByPrimaryKey(acqRequestKey);
                if (localAcqRequest != null) {
                    localAcqRequest.setTitle(request.getChildText("Title"));
                    localAcqRequest.setAuthor(request.getChildText("Author"));
                    localAcqRequest.setEdition(request.getChildText("Edition"));
                    localAcqRequest.setIsbn(request.getChildText("ISBN"));
                    localAcqRequest.setSeries(request.getChildText("Series"));
                    localAcqRequest.setPublish_Year(new Integer(request.getChildText("PublishYear")));
                    localAcqRequest.setPublisher(request.getChildText("Publisher"));
                    localAcqRequest.setPublish_Place(request.getChildText("PublishPlace"));
                    localAcqRequest.setVolume_No(request.getChildText("Volume"));
                }
            } else if (mode.equals("B")) {
                ACQ_ON_APPROVALKey acqOnApprovalKey = new ACQ_ON_APPROVALKey();
                if (id != null) acqOnApprovalKey.on_Approval_Id = id; else acqOnApprovalKey.on_Approval_Id = new Integer(request.getChildText("Id"));
                acqOnApprovalKey.library_Id = new Integer(request.getChildText("LibId"));
                LocalACQ_ON_APPROVAL localAcqOnApproval = ((LocalACQ_ON_APPROVALHome) homeFactory.getHome("ACQ_ON_APPROVAL")).findByPrimaryKey(acqOnApprovalKey);
                if (localAcqOnApproval != null) {
                    localAcqOnApproval.setTitle(request.getChildText("Title"));
                    localAcqOnApproval.setAuthor(request.getChildText("Author"));
                    localAcqOnApproval.setEdition(request.getChildText("Edition"));
                    localAcqOnApproval.setIsbn(request.getChildText("ISBN"));
                    localAcqOnApproval.setSeries(request.getChildText("Series"));
                    localAcqOnApproval.setPublish_Year(new Integer(request.getChildText("PublishYear")));
                    localAcqOnApproval.setPublisher(request.getChildText("Publisher"));
                    localAcqOnApproval.setPublish_Place(request.getChildText("PublishPlace"));
                    localAcqOnApproval.setVolume_No(request.getChildText("Volume"));
                }
            } else if (mode.equals("C")) {
                ACQ_SOLI_REQUESTKey acqSoliRequestKey = new ACQ_SOLI_REQUESTKey();
                acqSoliRequestKey.request_Id = new Integer(request.getChildText("Id"));
                acqSoliRequestKey.library_Id = new Integer(request.getChildText("LibId"));
                LocalACQ_SOLI_REQUEST localAcqSoliRequest = ((LocalACQ_SOLI_REQUESTHome) homeFactory.getHome("ACQ_SOLI_REQUEST")).findByPrimaryKey(acqSoliRequestKey);
                if (localAcqSoliRequest != null) {
                    localAcqSoliRequest.setTitle(request.getChildText("Title"));
                    localAcqSoliRequest.setAuthor(request.getChildText("Author"));
                    localAcqSoliRequest.setEdition(request.getChildText("Edition"));
                    localAcqSoliRequest.setIsbn(request.getChildText("ISBN"));
                    localAcqSoliRequest.setPublish_Year(new Integer(request.getChildText("PublishYear")));
                    localAcqSoliRequest.setPublisher(request.getChildText("Publisher"));
                    localAcqSoliRequest.setPublish_Place(request.getChildText("PublishPlace"));
                    localAcqSoliRequest.setVolume_No(request.getChildText("Volume"));
                }
            }
            success.setText("Y");
        } catch (Exception e) {
            success.setText("N");
            e.printStackTrace();
        }
        return newGenXMLGenerator.buildXMLDocument(success);
    }

    public String validateAccessioNoForWeedout(String xmlStr) {
        String docStat = "";
        Element ele = newGenXMLGenerator.getRootElementFromXMLDocument(xmlStr);
        String accessNo = ele.getChildText("AccessionNumber");
        String libId = ele.getChildText("LibraryId");
        DocumentKey dockey = new DocumentKey();
        dockey.accession_Number = accessNo;
        dockey.library_Id = new Integer(libId);
        LocalDocument localDoc = null;
        String title = "";
        String author = "";
        String voldet = "";
        try {
            localDoc = ((LocalDocumentHome) HomeFactory.getInstance().getHome("Document")).findByPrimaryKey(dockey);
        } catch (Exception exp) {
        }
        if (localDoc == null) {
            docStat = "INVALID";
        } else {
            docStat = "VALID";
            String status = localDoc.getStatus();
            if (status != null && status.equals("B")) {
                docStat = "AVAILABLE";
                Hashtable ht = utility.getCatalogueRecord(accessNo, Integer.parseInt(libId));
                title = ht.get("Title").toString();
                author = ht.get("Author").toString();
                voldet = ht.get("VolumeDetails").toString();
            } else {
                docStat = "NOTAVAILABLE";
            }
        }
        Element retRootEle = new Element("Response");
        Element eley = new Element("Status");
        eley.setText(docStat);
        retRootEle.addContent(eley);
        eley = new Element("Title");
        eley.setText(title);
        retRootEle.addContent(eley);
        eley = new Element("Author");
        eley.setText(author);
        retRootEle.addContent(eley);
        eley = new Element("VolumeDetails");
        eley.setText(voldet);
        retRootEle.addContent(eley);
        Document retdoc = new Document(retRootEle);
        String retstr = (new XMLOutputter()).outputString(retdoc);
        return retstr;
    }

    private LocalDocumentHome lookupDocument() {
        try {
            Context c = new InitialContext();
            LocalDocumentHome rv = (LocalDocumentHome) c.lookup("java:comp/env/ejb/Document");
            return rv;
        } catch (NamingException ne) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        }
    }

    public String identifyDocumentsForWeedingOut(String xmlStr) {
        Element ele = newGenXMLGenerator.getRootElementFromXMLDocument(xmlStr);
        String libId = ele.getChildText("LibraryId");
        List listAccessionEle = ele.getChildren("AccessionNumber");
        Vector vecAcc = new Vector(1, 1);
        for (int i = 0; i < listAccessionEle.size(); i++) {
            Element eleone = (Element) listAccessionEle.get(i);
            vecAcc.addElement(eleone.getChildText("Number"));
            vecAcc.addElement(eleone.getChildText("Reason"));
        }
        String patronId = ele.getChildText("PatronId");
        String patronLibId = ele.getChildText("PatronLibraryId");
        String entryId = ele.getChildText("EntryId");
        String entryLibId = ele.getChildText("EntryLibraryId");
        Connection con = DBConnector.getInstance().getDBConnection();
        Vector vecResult = new Vector(1, 1);
        PreparedStatement psCount = null;
        PreparedStatement psInsert = null;
        PreparedStatement psUpdate = null;
        String formid = "";
        try {
            psCount = con.prepareStatement("select count(*) from cir_weedout_material where accession_number=? and library_id=?");
            psInsert = con.prepareStatement("insert into cir_weedout_material (accession_number,library_id,approving_authority_id,approving_authority_lib_id,reason,status,entry_id,entry_library_id,entry_date,approval_date) values (?,?,?,?,?,?,?,?,?,?)");
            psUpdate = con.prepareStatement("update cir_weedout_material set approving_authority_id=?,approving_authority_lib_id=?,reason=?,status=?,entry_id=?,entry_library_id=?,entry_date=?,approval_date=? where accession_number=? and library_id=?");
            for (int i = 0; i < vecAcc.size(); i += 2) {
                vecResult.addElement(vecAcc.elementAt(i));
                psCount.setString(1, vecAcc.elementAt(i).toString());
                psCount.setString(2, libId);
                ResultSet rs = psCount.executeQuery();
                int count = 0;
                while (rs.next()) count = rs.getInt(1);
                rs.close();
                if (count == 0) {
                    vecResult.addElement("INSERT");
                    psInsert.setString(1, vecAcc.elementAt(i).toString());
                    psInsert.setInt(2, Integer.parseInt(libId));
                    psInsert.setString(3, patronId);
                    psInsert.setInt(4, Integer.parseInt(patronLibId));
                    psInsert.setString(5, vecAcc.elementAt(i + 1).toString());
                    psInsert.setString(6, "A");
                    psInsert.setString(7, entryId);
                    psInsert.setInt(8, Integer.parseInt(entryLibId));
                    psInsert.setTimestamp(9, utility.getTimestamp());
                    psInsert.setTimestamp(10, utility.getTimestamp());
                    psInsert.executeUpdate();
                } else {
                    vecResult.addElement("UPDATE");
                    psUpdate.setString(1, patronId);
                    psUpdate.setInt(2, Integer.parseInt(patronLibId));
                    psUpdate.setString(3, vecAcc.elementAt(i + 1).toString());
                    psUpdate.setString(4, "A");
                    psUpdate.setString(5, entryId);
                    psUpdate.setInt(6, Integer.parseInt(entryLibId));
                    psUpdate.setTimestamp(7, utility.getTimestamp());
                    psUpdate.setTimestamp(8, utility.getTimestamp());
                    psUpdate.setString(9, vecAcc.elementAt(i).toString());
                    psUpdate.setInt(10, Integer.parseInt(libId));
                    psUpdate.executeUpdate();
                }
            }
            psCount.close();
            psInsert.close();
            psUpdate.close();
            con.close();
            String date = utility.getFormattedDate(utility.getTimestamp());
            Hashtable patdet = utility.getPatronDetails(patronId, new Integer(patronLibId));
            String[] contentparamters = { "", date, utility.getTestedString(patdet.get("PatronName")), utility.getTestedString(patdet.get("Address1")), utility.getTestedString(patdet.get("Address2")), utility.getTestedString(patdet.get("City")), utility.getTestedString(patdet.get("State")), utility.getTestedString(patdet.get("Pin")), utility.getTestedString(patdet.get("Country")), "" };
            ADM_FORM_LETTER_CREATOR admCreator = new ADM_FORM_LETTER_CREATOR();
            Hashtable htparams = new Hashtable();
            String[] header = { "Accession number", "Title", "Author", "Edition", "Publication information", "Reason" };
            htparams.put("HEADER", header);
            Vector vecData = new Vector(1, 1);
            for (int i = 0; i < vecAcc.size(); i += 2) {
                String[] data1 = new String[6];
                String accno = vecAcc.elementAt(i).toString();
                String reason = vecAcc.elementAt(i + 1).toString();
                Hashtable htcatdet = utility.getCatalogueRecord(accno, Integer.parseInt(libId));
                data1[0] = accno;
                data1[1] = utility.getTestedString(htcatdet.get("Title")) + " " + utility.getTestedString(htcatdet.get("VolumeDetails"));
                data1[2] = utility.getTestedString(htcatdet.get("Author"));
                data1[3] = utility.getTestedString(htcatdet.get("Edition"));
                data1[4] = utility.getTestedString(htcatdet.get("PUBLISHER"));
                data1[5] = reason;
                vecData.addElement(data1);
            }
            htparams.put("DATA", vecData);
            formid = admCreator.generateFormLetter(Integer.parseInt(libId), 59, patronId, Integer.parseInt(patronLibId), "A", utility.getTestedString(patdet.get("Email")), utility.getTimestamp(), entryId, htparams, contentparamters, 0, 9);
        } catch (Exception exp) {
            exp.printStackTrace();
            try {
                psCount.close();
                psInsert.close();
                psUpdate.close();
                con.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        Element retEle = new Element("Response");
        Document retDoc = new Document(retEle);
        for (int i = 0; i < vecResult.size(); i += 2) {
            Element eleone = new Element("AccessionNumber");
            Element elesub = new Element("Number");
            elesub.setText(vecResult.elementAt(i).toString());
            eleone.addContent(elesub);
            elesub = new Element("Status");
            elesub.setText(vecResult.elementAt(i + 1).toString());
            eleone.addContent(elesub);
            retEle.addContent(eleone);
        }
        Element elesub = new Element("FormId");
        elesub.setText(formid);
        retEle.addContent(elesub);
        String xmlret = (new XMLOutputter()).outputString(retDoc);
        return xmlret;
    }

    private LocalCAT_VOLUMEHome lookupCAT_VOLUME() {
        try {
            Context c = new InitialContext();
            LocalCAT_VOLUMEHome rv = (LocalCAT_VOLUMEHome) c.lookup("java:comp/env/ejb/CAT_VOLUME");
            return rv;
        } catch (NamingException ne) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        }
    }

    private LocalCAT_SERIALHome lookupCAT_SERIAL() {
        try {
            Context c = new InitialContext();
            LocalCAT_SERIALHome rv = (LocalCAT_SERIALHome) c.lookup("java:comp/env/ejb/CAT_SERIAL");
            return rv;
        } catch (NamingException ne) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        }
    }

    private LocalADM_FORM_LETTERHome lookupADM_FORM_LETTER() {
        try {
            Context c = new InitialContext();
            LocalADM_FORM_LETTERHome rv = (LocalADM_FORM_LETTERHome) c.lookup("java:comp/env/ejb/ADM_FORM_LETTER");
            return rv;
        } catch (NamingException ne) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        }
    }

    public String getPendingWeedoutMaterialForApproval(String xmlStr) {
        Element eleroot = newGenXMLGenerator.getRootElementFromXMLDocument(xmlStr);
        Element retRootEle = new Element("Response");
        String libId = eleroot.getChildTextTrim("LibraryId");
        Connection con = DBConnector.getInstance().getDBConnection();
        String retStr = "";
        try {
            Statement stat = con.createStatement();
            String sql = "select accession_number,approving_authority_id,approving_authority_lib_id,reason from cir_weedout_material where library_id=" + libId + " and status='A'";
            ResultSet rs = stat.executeQuery(sql);
            while (rs.next()) {
                Element recordEle = new Element("Record");
                String accessNo = rs.getString(1);
                Element elex = new Element("AccessionNumber");
                elex.setText(accessNo);
                recordEle.addContent(elex);
                String appId = rs.getString(2);
                String appLibId = rs.getString(3);
                String reason = rs.getString(4);
                elex = new Element("Reason");
                elex.setText(reason);
                recordEle.addContent(elex);
                Hashtable htBib = utility.getCatalogueRecord(accessNo, Integer.parseInt(libId));
                elex = new Element("Title");
                elex.setText(utility.getTestedString(htBib.get("Title")));
                recordEle.addContent(elex);
                elex = new Element("Author");
                elex.setText(utility.getTestedString(htBib.get("Author")));
                recordEle.addContent(elex);
                elex = new Element("VolumeDetails");
                elex.setText(utility.getTestedString(htBib.get("VolumeDetails")));
                recordEle.addContent(elex);
                Hashtable htPat = utility.getPatronDetails(appId, new Integer(appLibId));
                elex = new Element("PatronName");
                elex.setText(utility.getTestedString(htPat.get("PatronName")));
                recordEle.addContent(elex);
                elex = new Element("PatronId");
                elex.setText(appId);
                recordEle.addContent(elex);
                retRootEle.addContent(recordEle);
            }
            rs.close();
            stat.close();
            con.close();
            Document retdoc = new Document(retRootEle);
            retStr = (new XMLOutputter()).outputString(retdoc);
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return retStr;
    }

    public String weedOutMaterialUpdateDatabase(String xmlStr) {
        Element rootele = newGenXMLGenerator.getRootElementFromXMLDocument(xmlStr);
        String libId = rootele.getChildText("LibraryId");
        List li = rootele.getChildren("Record");
        Connection con = DBConnector.getInstance().getDBConnection();
        Element retroot = new Element("Response");
        for (int i = 0; i < li.size(); i++) {
            Element eleone = (Element) li.get(i);
            String accessNo = eleone.getChildText("AccessionNumber");
            String status = eleone.getChildText("Status");
            String statusCode = "";
            if (status.equals("APPROVE")) statusCode = "B"; else if (status.equals("REJECT")) statusCode = "C";
            try {
                String sql = "update cir_weedout_material set status='" + statusCode + "' where accession_number='" + accessNo + "' and library_id=" + libId;
                Statement stat = con.createStatement();
                stat.addBatch(sql);
                if (statusCode.equals("B")) {
                    sql = "update document set status='I' where accession_number='" + accessNo + "' and library_id=" + libId;
                    stat.addBatch(sql);
                }
                stat.executeBatch();
                stat.close();
                Element elex = new Element("AccessionNumber");
                elex.setText(accessNo);
                retroot.addContent(elex);
            } catch (Exception exp) {
                exp.printStackTrace();
            }
        }
        try {
            con.close();
        } catch (Exception exp) {
        }
        Document retDoc = new Document(retroot);
        String xmlRet = (new XMLOutputter()).outputString(retDoc);
        return xmlRet;
    }
}
