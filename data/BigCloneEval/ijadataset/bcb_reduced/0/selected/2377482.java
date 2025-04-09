package ejb.bprocess.util;

import java.io.FileNotFoundException;
import java.sql.*;
import java.util.*;
import ejb.bprocess.util.NewGenLibRoot;

/**
 *
 * @author  Administrator
 */
public class ADM_FORM_LETTER_CREATOR {

    private ejb.bprocess.util.HomeFactory homeFactory = null;

    static final int BUFFER = 2048;

    private String textContent = "";

    private String formLetterNo = "";

    /** Creates a new instance of ADM_FORM_LETTER_CREATOR */
    public ADM_FORM_LETTER_CREATOR() {
        homeFactory = homeFactory.getInstance();
    }

    public String generateFormLetter(int loginLibraryId, int formatId, String toId, int toLibraryId, String toType, String toEmailId, Timestamp referenceDate, String entryId, Hashtable parameters, String[] contentParameters, int indexOfReferenceNumber, int indexOfTable) {
        ejb.objectmodel.administration.FORM_LETTER_FORMATKey key = new ejb.objectmodel.administration.FORM_LETTER_FORMATKey();
        key.format_Id = new Integer(formatId);
        key.library_Id = new Integer(loginLibraryId);
        ejb.objectmodel.administration.LocalFORM_LETTER_FORMAT local = null;
        try {
            local = ((ejb.objectmodel.administration.LocalFORM_LETTER_FORMATHome) homeFactory.getHome("FORM_LETTER_FORMAT")).findByPrimaryKey(key);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
        String format = local.getFormat();
        String title = local.getTitle();
        String femailstatus = local.getEmail_Status();
        String fimstatus = local.getInstant_Message_Status();
        String fprintstatus = local.getPrint_Status();
        String emailStatus = "";
        String imStatus = "";
        String printStatus = "";
        if (femailstatus.equals("A")) {
            emailStatus = "B";
        } else {
            emailStatus = "C";
        }
        if (fimstatus.equals("A")) {
            imStatus = "B";
        } else {
            imStatus = "C";
        }
        if (fprintstatus.equals("A")) {
            printStatus = "B";
        } else {
            printStatus = "C";
        }
        String emailId = "";
        if (toType.equals("A")) {
            ejb.objectmodel.administration.PatronKey patKey = new ejb.objectmodel.administration.PatronKey();
            patKey.patron_Id = toId;
            patKey.library_Id = new Integer(toLibraryId);
            ejb.objectmodel.administration.LocalPatron localPat = null;
            try {
                localPat = ((ejb.objectmodel.administration.LocalPatronHome) homeFactory.getHome("Patron")).findByPrimaryKey(patKey);
            } catch (Exception ex) {
                ex.printStackTrace(System.out);
            }
            emailId = localPat.getEmail();
        } else if (toType.equals("B")) {
            ejb.objectmodel.administration.ADM_CO_VENDORKey admVendorKey = new ejb.objectmodel.administration.ADM_CO_VENDORKey();
            admVendorKey.vendor_Id = new Integer(toId);
            admVendorKey.library_Id = new Integer(toLibraryId);
            ejb.objectmodel.administration.LocalADM_CO_VENDOR localAdmVendor = null;
            try {
                localAdmVendor = ((ejb.objectmodel.administration.LocalADM_CO_VENDORHome) homeFactory.getHome("ADM_CO_VENDOR")).findByPrimaryKey(admVendorKey);
            } catch (Exception ex) {
                ex.printStackTrace(System.out);
            }
            emailId = localAdmVendor.getEmail_Id();
        } else if (toType.equals("E")) {
            ejb.objectmodel.administration.CIR_CO_BINDERKey cirCoBinderKey = new ejb.objectmodel.administration.CIR_CO_BINDERKey();
            cirCoBinderKey.binder_Id = new Integer(toId);
            cirCoBinderKey.library_Id = new Integer(toLibraryId);
            ejb.objectmodel.administration.LocalCIR_CO_BINDER localCirCoBinder = null;
            try {
                localCirCoBinder = ((ejb.objectmodel.administration.LocalCIR_CO_BINDERHome) homeFactory.getHome("CIR_CO_BINDER")).findByPrimaryKey(cirCoBinderKey);
            } catch (Exception ex) {
                ex.printStackTrace(System.out);
            }
            emailId = localCirCoBinder.getEmail();
        } else {
            emailId = toEmailId;
        }
        String content = "";
        if (indexOfReferenceNumber != -1 && contentParameters[indexOfReferenceNumber].equals("")) {
            String prefix = local.getPrefix();
            String maxno = local.getMax_No().toString();
            formLetterNo = prefix + maxno;
            setFormLetterNo(formLetterNo);
            local.setMax_No(new Integer(local.getMax_No().intValue() + 1));
            contentParameters[indexOfReferenceNumber] = formLetterNo;
        }
        String tableContent = "\n";
        if (indexOfTable != -1) {
            Vector vecdata = (Vector) parameters.get("DATA");
            String[] headervals = (String[]) parameters.get("HEADER");
            String[] data = new String[headervals.length + 1];
            for (int i = 0; i < vecdata.size(); i++) {
                String[] rowdata = (String[]) vecdata.elementAt(i);
                for (int j = 0; j < rowdata.length; j++) {
                    if (i == 0) data[j] = rowdata[j]; else {
                        if (data[j] != null && rowdata[j] != null && data[j].length() < rowdata[j].length()) data[j] = rowdata[j];
                        if (data[j] == null || data[j].toString().trim().equals("")) data[j] = rowdata[j];
                    }
                }
            }
            for (int i = 0; i < vecdata.size(); i++) {
                String[] rowdata = (String[]) vecdata.elementAt(i);
                for (int j = 0; j < rowdata.length; j++) {
                    String space = "";
                    try {
                        if (data[j] != null && headervals[j].length() > data[j].length()) {
                            for (int k = 1; k <= (headervals[j].length() - rowdata[j].length()); k++) space += " ";
                        } else {
                            int len = 0;
                            if (rowdata[j] != null || !rowdata[j].trim().equals("")) {
                                len = rowdata[j].length();
                            }
                            if (data[j].length() > len) {
                                space = "";
                                for (int k = 1; k <= (data[j].length() - len); k++) space += " ";
                            }
                        }
                    } catch (Exception e) {
                        for (int k = 1; k <= headervals[j].length(); k++) space += " ";
                        e.printStackTrace();
                    }
                    tableContent += rowdata[j] + space + "\t";
                }
                tableContent += "\n";
            }
            String headerContent = "\n";
            for (int i = 0; i < headervals.length; i++) {
                String spaces = "";
                if (data[i] != null && headervals[i] != null) {
                    if (data[i].length() > headervals[i].length()) {
                        for (int j = 1; j <= (data[i].length() - headervals[i].length()); j++) spaces += " ";
                    }
                    headerContent += headervals[i] + spaces + "\t";
                } else headerContent += headervals[i] + "\t";
            }
            headerContent += "\n";
            headerContent += "_________________________________________________________________________________________\n";
            tableContent = headerContent + tableContent;
            contentParameters[indexOfTable] = tableContent;
        }
        content = java.text.MessageFormat.format(format, contentParameters);
        this.setTextContent(content);
        String htmlcontent = "";
        String pathOfOODoc = "";
        Hashtable objParamters = new Hashtable();
        for (int i = 0; i < contentParameters.length; i++) {
            objParamters.put(String.valueOf(i), contentParameters[i]);
            if (i == indexOfTable) {
                objParamters.put(String.valueOf(i), parameters);
            }
        }
        htmlcontent = generateHTMLDocument(new Integer(formatId), new Integer(loginLibraryId), objParamters);
        pathOfOODoc = generateOODocument(new Integer(formatId), new Integer(loginLibraryId), objParamters);
        Integer maxid = new Integer(ejb.bprocess.util.Utility.getInstance().getFormID(loginLibraryId));
        ejb.objectmodel.administration.LocalADM_FORM_LETTER localAdmFormLetter = null;
        try {
            localAdmFormLetter = ((ejb.objectmodel.administration.LocalADM_FORM_LETTERHome) homeFactory.getHome("ADM_FORM_LETTER")).create(maxid, new Integer(loginLibraryId), formLetterNo, content, title, toId, new Integer(toLibraryId), toType, "A", printStatus, emailStatus, imStatus, Utility.getInstance().getTimestamp(), emailId, entryId, new Integer(loginLibraryId), Utility.getInstance().getTimestamp());
            localAdmFormLetter.setPrint_Priority("A");
            localAdmFormLetter.setPrint_Copies(new Integer(1));
            localAdmFormLetter.setGenerate_Type("B");
            localAdmFormLetter.setDocument_Type("B");
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
        localAdmFormLetter.setFormat_Id(new Integer(formatId));
        localAdmFormLetter.setHtml_Content(htmlcontent);
        localAdmFormLetter.setOo_path(pathOfOODoc);
        localAdmFormLetter.setRead_Status("B");
        return maxid.toString();
    }

    public java.lang.String generateHTMLDocument(java.lang.Integer formLetterId, java.lang.Integer libraryId, java.util.Hashtable parameters) {
        String generatedFileAbsolutePath = "";
        try {
            int formLetterIdInt = formLetterId.intValue();
            String fileName = "";
            switch(formLetterIdInt) {
                case 46:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "46_FirmOrderPurchase.html";
                        break;
                    }
                case 14:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "14_Receive for OnApproval.html";
                        break;
                    }
                case 16:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "16_Payment of Invoice.html";
                        break;
                    }
                case 17:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "17_Reconciliation of Advance Payment.html";
                        break;
                    }
                case 40:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "40_Request For Approval (Budget Heads).html";
                        break;
                    }
                case 32:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "32_Rejection of Requests for addition of items to library.html";
                        break;
                    }
                case 48:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "48_Intimation to Requester when book arrives.html";
                        break;
                    }
                case 44:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "44_Invoice Not Received.html";
                        break;
                    }
                case 45:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "45_Item Not Received.html";
                        break;
                    }
                case 36:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "36_Request for Donation of item.html";
                        break;
                    }
                case 37:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "37_Receive for Donation of item.html";
                        break;
                    }
                case 27:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "27_Claims for unfulfilled orders.html";
                        break;
                    }
                case 28:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "28_Listing for unfulfilled orders (by budget heads).html";
                        break;
                    }
                case 31:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "31_No Dues Certificate.html";
                        break;
                    }
                case 42:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "42_Loss of Book and charges recovered Report.html";
                        break;
                    }
                case 52:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "52_SMFirmOrderPurchase.html";
                        break;
                    }
                case 12:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "12_Cancellation of Subscription.html";
                        break;
                    }
                case 19:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "19_Binding Order for Serials Volumes.html";
                        break;
                    }
                case 18:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "18_Serials Binding invoice payment.html";
                        break;
                    }
                case 15:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "15_Serials Binding - Logical List.html";
                        break;
                    }
                case 21:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "21_Serials subscription Invoice Payment.html";
                        break;
                    }
                case 54:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "54_Renew Subscription.html";
                        break;
                    }
                case 22:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "22_Claim for serial IssuesTitlepageIndexpage.html";
                        break;
                    }
                case 2:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "2_Pay towards ILL courier charges.html";
                        break;
                    }
                case 3:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "3_Binder contains the details of the documents checked out.html";
                        break;
                    }
                case 4:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "4_Reservation notice.html";
                        break;
                    }
                case 5:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "5_Intimation  to Patron of ILL Request.html";
                        break;
                    }
                case 6:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "6_Recall notice.html";
                        break;
                    }
                case 7:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "7_Request for Inter Library Loan.html";
                        break;
                    }
                case 9:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "9_Request for Inter Library Loan Rejection.html";
                        break;
                    }
                case 10:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "10_Return of loaned item.html";
                        break;
                    }
                case 29:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "29_Check out slip.html";
                        break;
                    }
                case 30:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "30_Check in slip.html";
                        break;
                    }
                case 43:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "43_Collection of Overdue charges.html";
                        break;
                    }
                case 51:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "51_Renewal Slip.html";
                        break;
                    }
                case 55:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "55_Consolidated Check in slip.html";
                        break;
                    }
                case 47:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "47_Advance Payment-firmOrder.html";
                        break;
                    }
                case 53:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "53_Overdue notice.html";
                        break;
                    }
                case 33:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "33_Request for On-approval Supplies.html";
                        break;
                    }
                case 34:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "34_Receipt of on-approval supplies.html";
                        break;
                    }
                case 35:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "35_Return of on-approval supplies.html";
                        break;
                    }
                case 38:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "38_Request For invoice for on-approval supplies.html";
                        break;
                    }
                case 39:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "39_firmOrder advance payment on-approval supplies.html";
                        break;
                    }
                case 56:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "56_Cancellation of firm Orders.html";
                        break;
                    }
                case 57:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "57_renewal notice.html";
                        break;
                    }
                case 58:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "58_SDIProfile.html";
                        break;
                    }
                case 59:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "59_ApproveWeedoutMaterial.html";
                        break;
                    }
                case 60:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "60_Request for quotation.html";
                        break;
                    }
                case 61:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "61_Firm Order Purchase.html";
                        break;
                    }
                case 62:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "62_ILL Check out slip.html";
                        break;
                    }
                case 63:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "63_ILL Check in slip.html";
                        break;
                    }
            }
            Object[] obx = new Object[parameters.size()];
            java.util.Enumeration enumParam = parameters.keys();
            String[] header = null;
            java.util.Vector vecData = null;
            String[] columnSizes = null;
            while (enumParam.hasMoreElements()) {
                String key = enumParam.nextElement().toString();
                Object val = parameters.get(key);
                if (val.getClass().getName().equals("java.util.Hashtable")) {
                    obx[Integer.parseInt(key)] = "";
                    java.util.Hashtable htInter = (java.util.Hashtable) val;
                    header = (String[]) htInter.get("HEADER");
                    vecData = (java.util.Vector) htInter.get("DATA");
                    columnSizes = (String[]) htInter.get("COLUMNSIZES");
                } else {
                    obx[Integer.parseInt(key)] = val.toString();
                }
            }
            String str = "";
            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(fileName)));
            while (br.ready()) {
                str += br.readLine() + " \n";
            }
            br.close();
            org.jdom.input.SAXBuilder sb = new org.jdom.input.SAXBuilder();
            sb.setIgnoringElementContentWhitespace(true);
            sb.setValidation(false);
            org.jdom.Document htmldoc = sb.build(new java.io.StringReader(str));
            htmldoc.getRootElement().removeChild("HEAD");
            htmldoc.getRootElement().removeChild("head");
            org.jdom.output.XMLOutputter xout1 = new org.jdom.output.XMLOutputter();
            str = xout1.outputString(htmldoc.getRootElement());
            java.text.MessageFormat mf = new java.text.MessageFormat(str);
            str = mf.format(str, obx);
            sb = new org.jdom.input.SAXBuilder();
            sb.setIgnoringElementContentWhitespace(true);
            sb.setValidation(false);
            htmldoc = sb.build(new java.io.StringReader(str));
            if (vecData != null && vecData.size() != 0) {
                java.util.List liTables = htmldoc.getRootElement().getChild("BODY").getChildren("TABLE");
                org.jdom.Element newgenlibtable = null;
                for (int i = 0; i < liTables.size(); i++) {
                    org.jdom.Element eleTab = (org.jdom.Element) liTables.get(i);
                    org.jdom.output.XMLOutputter xout = new org.jdom.output.XMLOutputter();
                    String xmldet = xout.outputString(eleTab);
                    if (xmldet.toUpperCase().indexOf("NEWGENLIBTABLE") != -1) {
                        newgenlibtable = eleTab;
                    }
                }
                if (newgenlibtable == null) newgenlibtable = new org.jdom.Element("TABLE");
                newgenlibtable.removeChildren();
                newgenlibtable.setAttribute("WIDTH", "100%");
                newgenlibtable.setAttribute("BORDERCOLOR", "#000000");
                org.jdom.Element eleHeaderRows = new org.jdom.Element("THEAD");
                org.jdom.Element eleRow = new org.jdom.Element("TR");
                for (int j = 0; j < header.length; j++) {
                    org.jdom.Element eleTableCell = new org.jdom.Element("TH");
                    eleTableCell.setText(header[j]);
                    eleRow.addContent(eleTableCell);
                }
                eleHeaderRows.addContent(eleRow);
                newgenlibtable.addContent(eleHeaderRows);
                org.jdom.Element elebody = new org.jdom.Element("TBODY");
                for (int j = 0; j < vecData.size(); j++) {
                    eleRow = new org.jdom.Element("TR");
                    String[] rowData = (String[]) vecData.elementAt(j);
                    for (int k = 0; k < rowData.length; k++) {
                        org.jdom.Element eleTableCell = new org.jdom.Element("TD");
                        if (rowData[k] != null && !rowData[k].trim().equals("")) eleTableCell.setText(rowData[k]); else eleTableCell.setText("-");
                        eleRow.addContent(eleTableCell);
                    }
                    elebody.addContent(eleRow);
                }
                newgenlibtable.addContent(elebody);
            }
            org.jdom.output.XMLOutputter outputter = new org.jdom.output.XMLOutputter();
            String output = outputter.outputString(htmldoc);
            generatedFileAbsolutePath = output;
            System.out.println("HTML: " + output);
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return generatedFileAbsolutePath;
    }

    public java.lang.String generateOODocument(java.lang.Integer formLetterId, java.lang.Integer libraryId, java.util.Hashtable parameters) {
        String generatedFileAbsolutePath = "";
        System.out.println("Form letter Id: " + formLetterId);
        try {
            boolean flag1 = true;
            int countFile1 = 0;
            String fileValid = "";
            while (flag1) {
                fileValid = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("Reports");
                fileValid += "/" + System.currentTimeMillis() + "_" + countFile1 + ".odt";
                java.io.File thisDirec = new java.io.File(fileValid);
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
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "46_FirmOrderPurchase.odt";
                        break;
                    }
                case 14:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "14_Receive for OnApproval.odt";
                        break;
                    }
                case 16:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "16_Payment of Invoice.odt";
                        break;
                    }
                case 17:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "17_Reconciliation of Advance Payment.odt";
                        break;
                    }
                case 40:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "40_Request For Approval (Budget Heads).odt";
                        break;
                    }
                case 32:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "32_Rejection of Requests for addition of items to library.odt";
                        break;
                    }
                case 48:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "48_Intimation to Requester when book arrives.odt";
                        break;
                    }
                case 44:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "44_Invoice Not Received.odt";
                        break;
                    }
                case 45:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "45_Item Not Received.odt";
                        break;
                    }
                case 36:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "36_Request for Donation of item.odt";
                        break;
                    }
                case 37:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "37_Receive for Donation of item.odt";
                        break;
                    }
                case 27:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "27_Claims for unfulfilled orders.odt";
                        break;
                    }
                case 28:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "28_Listing for unfulfilled orders (by budget heads).odt";
                        break;
                    }
                case 31:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "31_No Dues Certificate.odt";
                        break;
                    }
                case 42:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "42_Loss of Book and charges recovered Report.odt";
                        break;
                    }
                case 52:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "52_SMFirmOrderPurchase.odt";
                        break;
                    }
                case 12:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "12_Cancellation of Subscription.odt";
                        break;
                    }
                case 19:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "19_Binding Order for Serials Volumes.odt";
                        break;
                    }
                case 18:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "18_Serials Binding invoice payment.odt";
                        break;
                    }
                case 15:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "15_Serials Binding - Logical List.odt";
                        break;
                    }
                case 21:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "21_Serials subscription Invoice Payment.odt";
                        break;
                    }
                case 54:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "54_Renew Subscription.odt";
                        break;
                    }
                case 22:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "22_Claim for serial IssuesTitlepageIndexpage.odt";
                        break;
                    }
                case 2:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "2_Pay towards ILL courier charges.odt";
                        break;
                    }
                case 3:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "3_Binder contains the details of the documents checked out.odt";
                        break;
                    }
                case 4:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "4_Reservation notice.odt";
                        break;
                    }
                case 5:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "5_Intimation  to Patron of ILL Request.odt";
                        break;
                    }
                case 6:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "6_Recall notice.odt";
                        break;
                    }
                case 7:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "7_Request for Inter Library Loan.odt";
                        break;
                    }
                case 9:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "9_Request for Inter Library Loan Rejection.odt";
                        break;
                    }
                case 10:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "10_Return of loaned item.odt";
                        break;
                    }
                case 29:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "29_Check out slip.odt";
                        break;
                    }
                case 30:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "30_Check in slip.odt";
                        break;
                    }
                case 43:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "43_Collection of Overdue charges.odt";
                        break;
                    }
                case 51:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "51_Renewal Slip.odt";
                        break;
                    }
                case 55:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "55_Consolidated Check in slip.odt";
                        break;
                    }
                case 47:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "47_Advance Payment-firmOrder.odt";
                        break;
                    }
                case 53:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "53_Overdue notice.odt";
                        break;
                    }
                case 33:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "33_Request for On-approval Supplies.odt";
                        break;
                    }
                case 34:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "34_Receipt of on-approval supplies.odt";
                        break;
                    }
                case 35:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "35_Return of on-approval supplies.odt";
                        break;
                    }
                case 38:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "38_Request For invoice for on-approval supplies.odt";
                        break;
                    }
                case 39:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "39_firmOrder advance payment on-approval supplies.odt";
                        break;
                    }
                case 56:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "56_Cancellation of firm Orders.odt";
                        break;
                    }
                case 57:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "57_renewal notice.odt";
                        break;
                    }
                case 58:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "58_SDIProfile.odt";
                        break;
                    }
                case 59:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "59_ApproveWeedoutMaterial.odt";
                        break;
                    }
                case 60:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "60_Request for quotation.odt";
                        break;
                    }
                case 61:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "61_Firm Order Purchase.odt";
                        break;
                    }
                case 62:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "62_ILL Check out slip.odt";
                        break;
                    }
                case 63:
                    {
                        fileName = ejb.bprocess.util.NewGenLibRoot.getRoot() + java.util.ResourceBundle.getBundle("server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "63_ILL Check in slip.odt";
                        break;
                    }
            }
            java.io.BufferedInputStream is = null;
            java.util.zip.ZipEntry entry;
            java.util.jar.JarFile zipfile = new java.util.jar.JarFile(fileName);
            java.util.Enumeration e = zipfile.entries();
            String subTempDirectory = "";
            boolean flag = true;
            int countFile = 0;
            while (flag) {
                String direcValid = ejb.bprocess.util.NewGenLibRoot.getRoot() + "/Temp";
                direcValid += "/" + System.currentTimeMillis() + "_" + countFile;
                java.io.File thisDirec = new java.io.File(direcValid);
                if (thisDirec.exists()) {
                    countFile++;
                    continue;
                } else {
                    subTempDirectory = direcValid;
                    String forpic = direcValid + "/Pictures";
                    String thumbnails = direcValid + "/Thumbnails";
                    direcValid += "/META-INF";
                    java.io.File thisDirecWithMeta = new java.io.File(direcValid);
                    thisDirecWithMeta.mkdirs();
                    thisDirecWithMeta = new java.io.File(forpic);
                    thisDirecWithMeta.mkdirs();
                    thisDirecWithMeta = new java.io.File(thumbnails);
                    thisDirecWithMeta.mkdirs();
                    flag = false;
                    break;
                }
            }
            while (e.hasMoreElements()) {
                entry = (java.util.zip.ZipEntry) e.nextElement();
                is = new java.io.BufferedInputStream(zipfile.getInputStream(entry));
                int count;
                byte data[] = new byte[BUFFER];
                java.io.File tempDirectory = new java.io.File(ejb.bprocess.util.NewGenLibRoot.getRoot() + "/Temp");
                if (!tempDirectory.exists()) tempDirectory.mkdirs();
                java.io.FileOutputStream fos = null;
                try {
                    fos = new java.io.FileOutputStream(subTempDirectory + "/" + entry.getName());
                } catch (Exception ex) {
                }
                if (fos != null) {
                    java.io.BufferedOutputStream dest = new java.io.BufferedOutputStream(fos, BUFFER);
                    while ((count = is.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                    dest.close();
                    is.close();
                }
            }
            Object[] obx = new Object[parameters.size()];
            java.util.Enumeration enumParam = parameters.keys();
            String[] header = null;
            java.util.Vector vecData = null;
            String[] columnSizes = null;
            while (enumParam.hasMoreElements()) {
                String key = enumParam.nextElement().toString();
                Object val = parameters.get(key);
                if (val.getClass().getName().equals("java.util.Hashtable")) {
                    obx[Integer.parseInt(key)] = "";
                    java.util.Hashtable htInter = (java.util.Hashtable) val;
                    header = (String[]) htInter.get("HEADER");
                    vecData = (java.util.Vector) htInter.get("DATA");
                    columnSizes = (String[]) htInter.get("COLUMNSIZES");
                } else {
                    obx[Integer.parseInt(key)] = val.toString();
                }
            }
            java.io.BufferedInputStream origin = null;
            java.io.FileOutputStream dest1 = new java.io.FileOutputStream(fileValid);
            java.util.jar.JarOutputStream out = new java.util.jar.JarOutputStream(new java.io.BufferedOutputStream(dest1));
            byte data[] = new byte[BUFFER];
            java.io.File f = new java.io.File(subTempDirectory);
            String files[] = f.list();
            java.io.File[] allfiles = f.listFiles();
            java.util.Vector vecfiles = new java.util.Vector(1, 1);
            for (int i = 0; i < allfiles.length; i++) {
                if (allfiles[i].isDirectory()) {
                    java.io.File[] subfiles = allfiles[i].listFiles();
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
                java.io.File file = new java.io.File(subTempDirectory + "/" + files[i]);
                if (!file.isDirectory()) {
                    java.io.FileInputStream fi = new java.io.FileInputStream(subTempDirectory + "/" + files[i]);
                    String str = "";
                    if (file.getName().equals("content.xml")) {
                        java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(fi));
                        while (br.ready()) {
                            str += br.readLine();
                        }
                        br.close();
                        java.text.MessageFormat mf = new java.text.MessageFormat(str);
                        str = mf.format(str, obx);
                        org.jdom.input.SAXBuilder sb = new org.jdom.input.SAXBuilder();
                        org.jdom.Document doc = sb.build(new java.io.StringReader(str));
                        org.jdom.Element doccontent = doc.getRootElement();
                        org.jdom.Namespace officens = doccontent.getNamespace();
                        org.jdom.Namespace textns = doccontent.getNamespace("text");
                        org.jdom.Namespace tablens = doccontent.getNamespace("table");
                        org.jdom.Namespace stylens = doccontent.getNamespace("style");
                        if (vecData != null) {
                            java.util.List liTables = doccontent.getChild("body", officens).getChild("text", officens).getChildren("table", tablens);
                            org.jdom.Element newgenlibtable = null;
                            for (int j = 0; j < liTables.size(); j++) {
                                org.jdom.Element presele = (org.jdom.Element) liTables.get(j);
                                String nameoftab = presele.getAttributeValue("name", tablens);
                                if (nameoftab.trim().toUpperCase().equals("NEWGENLIBTABLE")) {
                                    newgenlibtable = presele;
                                }
                            }
                            if (newgenlibtable == null) newgenlibtable = new org.jdom.Element("table", tablens);
                            newgenlibtable.removeChildren();
                            newgenlibtable.setAttribute("name", "newgenlibtable", tablens);
                            org.jdom.Element eleColumns = new org.jdom.Element("table-column", tablens);
                            eleColumns.setAttribute("number-columns-repeated", String.valueOf(header.length), tablens);
                            eleColumns.setAttribute("style-name", "newgenlibtable", tablens);
                            newgenlibtable.addContent(eleColumns);
                            org.jdom.Element eleHeaderRows = new org.jdom.Element("table-header-rows", tablens);
                            org.jdom.Element eleRow = new org.jdom.Element("table-row", tablens);
                            for (int j = 0; j < header.length; j++) {
                                org.jdom.Element eleTableCell = new org.jdom.Element("table-cell", tablens);
                                eleTableCell.setAttribute("style-name", "newgenlibtable.A", tablens);
                                eleTableCell.setAttribute("value-type", "string", officens);
                                org.jdom.Element eleTableText = new org.jdom.Element("p", textns);
                                eleTableText.setAttribute("style-name", "Table_20_Heading", textns);
                                eleTableText.setText(header[j]);
                                eleTableCell.addContent(eleTableText);
                                eleRow.addContent(eleTableCell);
                            }
                            eleHeaderRows.addContent(eleRow);
                            newgenlibtable.addContent(eleHeaderRows);
                            for (int j = 0; j < vecData.size(); j++) {
                                eleRow = new org.jdom.Element("table-row", tablens);
                                String[] rowData = (String[]) vecData.elementAt(j);
                                for (int k = 0; k < rowData.length; k++) {
                                    org.jdom.Element eleTableCell = new org.jdom.Element("table-cell", tablens);
                                    eleTableCell.setAttribute("style-name", "newgenlibtable.A", tablens);
                                    eleTableCell.setAttribute("value-type", "string", officens);
                                    org.jdom.Element eleTableText = new org.jdom.Element("p", textns);
                                    eleTableText.setAttribute("style-name", "Table_20_Contents", textns);
                                    eleTableText.setText(rowData[k]);
                                    eleTableCell.addContent(eleTableText);
                                    eleRow.addContent(eleTableCell);
                                }
                                newgenlibtable.addContent(eleRow);
                            }
                        }
                        org.jdom.output.XMLOutputter outputter = new org.jdom.output.XMLOutputter();
                        String output = outputter.outputString(doc);
                        System.out.println(output);
                        java.io.FileOutputStream fo = new java.io.FileOutputStream(subTempDirectory + "/" + files[i]);
                        fo.write(output.getBytes());
                    }
                    origin = new java.io.BufferedInputStream(fi, BUFFER);
                    if (!fi.getFD().valid()) {
                        fi.close();
                        fi = new java.io.FileInputStream(subTempDirectory + "/" + files[i]);
                        origin = new java.io.BufferedInputStream(fi, BUFFER);
                    }
                    java.util.jar.JarEntry entryx = new java.util.jar.JarEntry(files[i]);
                    out.putNextEntry(entryx);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    origin.close();
                }
            }
            out.close();
            try {
                java.io.File subtemp = new java.io.File(subTempDirectory);
                subtemp.delete();
            } catch (Exception exp) {
            }
            generatedFileAbsolutePath = fileValid;
        } catch (Exception exp) {
            exp.printStackTrace();
        }
        return generatedFileAbsolutePath;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public String getFormLetterNo() {
        return formLetterNo;
    }

    public void setFormLetterNo(String title) {
        this.formLetterNo = title;
    }
}
