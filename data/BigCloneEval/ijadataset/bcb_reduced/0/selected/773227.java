package reports.utility;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Vector;
import org.hibernate.Session;
import reports.utility.datamodel.administration.ADM_CO_VENDOR;
import reports.utility.datamodel.administration.ADM_CO_VENDOR_KEY;
import reports.utility.datamodel.administration.ADM_CO_VENDOR_MANAGER;
import reports.utility.datamodel.administration.ADM_FORM_LETTER;
import reports.utility.datamodel.administration.ADM_FORM_LETTER_KEY;
import reports.utility.datamodel.administration.ADM_FORM_LETTER_MANAGER;
import reports.utility.datamodel.administration.CIR_CO_BINDER;
import reports.utility.datamodel.administration.CIR_CO_BINDER_KEY;
import reports.utility.datamodel.administration.CIR_CO_BINDER_MANAGER;
import reports.utility.datamodel.administration.FORM_LETTER_FORMAT;
import reports.utility.datamodel.administration.FORM_LETTER_FORMAT_KEY;
import reports.utility.datamodel.administration.FORM_LETTER_FORMAT_MANAGER;
import reports.utility.datamodel.administration.PATRON;
import reports.utility.datamodel.administration.PATRON_KEY;
import reports.utility.datamodel.administration.PATRON_MANAGER;

/**
 *
 * @author Administrator
 */
public class ADM_FORM_LETTER_CREATOR {

    static final int BUFFER = 2048;

    private String textContent = "";

    private String formLetterNo = "";

    /** Creates a new instance of ADM_FORM_LETTER_CREATOR */
    public ADM_FORM_LETTER_CREATOR() {
    }

    public void generateFormLetter(Connection con, Session session, int loginLibraryId, int formatId, String toId, int toLibraryId, String toType, String toEmailId, Timestamp referenceDate, String entryId, Hashtable parameters, String[] contentParameters, int indexOfReferenceNumber, int indexOfTable) {
        ADM_FORM_LETTER_MANAGER admflManager = new ADM_FORM_LETTER_MANAGER();
        FORM_LETTER_FORMAT_KEY pkey = new FORM_LETTER_FORMAT_KEY();
        FORM_LETTER_FORMAT_MANAGER flmanager = new FORM_LETTER_FORMAT_MANAGER();
        PATRON_MANAGER patManager = new PATRON_MANAGER();
        pkey.setFormat_id(new Integer(formatId));
        pkey.setLibrary_id(new Integer(loginLibraryId));
        FORM_LETTER_FORMAT flformat = flmanager.load(session, pkey);
        String format = flformat.getFormat();
        String title = flformat.getTitle();
        String formLetterNo = "";
        String prefix = flformat.getPrefix();
        String maxno = flformat.getMax_no().toString();
        formLetterNo = prefix + maxno;
        setFormLetterNo(formLetterNo);
        flformat.setMax_no(new Integer(flformat.getMax_no().intValue() + 1));
        String femailstatus = flformat.getEmail_status();
        String fimstatus = flformat.getInstant_message_status();
        String fprintstatus = flformat.getPrint_status();
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
            PATRON_KEY patkey = new PATRON_KEY();
            patkey.setPatron_id(toId);
            patkey.setLibrary_id(new Integer(toLibraryId));
            PATRON patron = patManager.load(session, patkey);
            emailId = patron.getEmail();
        } else if (toType.equals("B")) {
            ADM_CO_VENDOR_MANAGER venmanager = new ADM_CO_VENDOR_MANAGER();
            ADM_CO_VENDOR_KEY venkey = new ADM_CO_VENDOR_KEY();
            venkey.setVendor_id(new Integer(toId));
            venkey.setLibrary_id(new Integer(toLibraryId));
            ADM_CO_VENDOR vendor = (ADM_CO_VENDOR) venmanager.load(session, venkey);
            emailId = vendor.getEmail_id();
        } else if (toType.equals("E")) {
            CIR_CO_BINDER_MANAGER binManager = new CIR_CO_BINDER_MANAGER();
            CIR_CO_BINDER_KEY bindkey = new CIR_CO_BINDER_KEY();
            bindkey.setBinder_id(new Integer(toId));
            bindkey.setLibrary_id(new Integer(toLibraryId));
            CIR_CO_BINDER binder = (CIR_CO_BINDER) binManager.load(session, bindkey);
            emailId = binder.getEmail();
        } else {
            emailId = toEmailId;
        }
        String content = "";
        if (indexOfReferenceNumber != -1) {
            contentParameters[indexOfReferenceNumber] = formLetterNo;
        }
        String tableContent = "";
        if (indexOfTable != -1) {
            String[] headervals = (String[]) parameters.get("HEADER");
            for (int i = 0; i < headervals.length; i++) {
                if (i == 0) tableContent += headervals[i]; else tableContent += "\t" + headervals[i];
            }
            tableContent += "\n";
            tableContent += "_________________________________________________________________\n";
            Vector vecdata = (Vector) parameters.get("DATA");
            for (int i = 0; i < vecdata.size(); i++) {
                String[] rowdata = (String[]) vecdata.elementAt(i);
                for (int j = 0; j < rowdata.length; j++) {
                    if (i == 0) tableContent += rowdata[i]; else tableContent += "\t" + rowdata[i];
                }
                tableContent += "\n";
            }
            contentParameters[indexOfTable] = tableContent;
        }
        content = java.text.MessageFormat.format(format, contentParameters);
        System.out.println("Content is sdjhfjsdk: " + content);
        this.setTextContent(content);
        String htmlcontent = "";
        String pathOfOODoc = "";
        Hashtable objParamters = new Hashtable();
        for (int i = 0; i < contentParameters.length; i++) {
            objParamters.put(String.valueOf(i), tools.StringProcessor.getInstance().verifyString(contentParameters[i]));
            if (i == indexOfTable) {
                objParamters.put(String.valueOf(i), parameters);
            }
        }
        htmlcontent = generateHTMLDocument(new Integer(formatId), new Integer(loginLibraryId), objParamters);
        pathOfOODoc = generateOODocument(new Integer(formatId), new Integer(loginLibraryId), objParamters);
        ADM_FORM_LETTER admFormLetter = new ADM_FORM_LETTER();
        admFormLetter.setEmail_status(emailStatus);
        admFormLetter.setEntry_date(StaticValues.getInstance().getReferenceDate());
        admFormLetter.setEntry_id(StaticValues.getInstance().getLoginPatronId());
        admFormLetter.setEntry_library_id(new Integer(StaticValues.getInstance().getLoginLibraryId()));
        admFormLetter.setForm_letter_content(content);
        admFormLetter.setForm_letter_no(formLetterNo);
        admFormLetter.setForm_letter_title(title);
        admFormLetter.setFormat_id(new Integer(formatId));
        admFormLetter.setHtml_content(htmlcontent);
        admFormLetter.setInstant_message_status(imStatus);
        admFormLetter.setOo_path(pathOfOODoc);
        admFormLetter.setPrint_status(printStatus);
        admFormLetter.setRead_status("B");
        admFormLetter.setTo_email_id(emailId);
        admFormLetter.setTo_execute_on_date(StaticValues.getInstance().getReferenceDate());
        admFormLetter.setTo_id(toId);
        admFormLetter.setTo_library_id(new Integer(toLibraryId));
        admFormLetter.setTo_type(toType);
        Integer maxid = admflManager.getMaxId(con);
        System.out.println("Maximum Id is: " + maxid);
        ADM_FORM_LETTER_KEY admflpkey = new ADM_FORM_LETTER_KEY();
        admflpkey.setForm_id(maxid);
        admflpkey.setLibrary_id(new Integer(loginLibraryId));
        admFormLetter.setPrimaryKey(admflpkey);
        admflManager.save(session, admFormLetter);
    }

    public java.lang.String generateHTMLDocument(java.lang.Integer formLetterId, java.lang.Integer libraryId, java.util.Hashtable parameters) {
        String generatedFileAbsolutePath = "";
        try {
            int formLetterIdInt = formLetterId.intValue();
            String fileName = "";
            switch(formLetterIdInt) {
                case 46:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "46_FirmOrderPurchase.html";
                        break;
                    }
                case 14:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "14_Receive for OnApproval.html";
                        break;
                    }
                case 16:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "16_Payment of Invoice.html";
                        break;
                    }
                case 17:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "17_Reconciliation of Advance Payment.html";
                        break;
                    }
                case 40:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "40_Request For Approval (Budget Heads).html";
                        break;
                    }
                case 32:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "32_Rejection of Requests for addition of items to library.html";
                        break;
                    }
                case 48:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "48_Intimation to Requester when book arrives.html";
                        break;
                    }
                case 44:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "44_Invoice Not Received.html";
                        break;
                    }
                case 45:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "45_Item Not Received.html";
                        break;
                    }
                case 36:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "36_Request for Donation of item.html";
                        break;
                    }
                case 37:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "37_Receive for Donation of item.html";
                        break;
                    }
                case 27:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "27_Claims for unfulfilled orders.html";
                        break;
                    }
                case 28:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "28_Listing for unfulfilled orders (by budget heads).html";
                        break;
                    }
                case 31:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "31_No Dues Certificate.html";
                        break;
                    }
                case 42:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "42_Loss of Book and charges recovered Report.html";
                        break;
                    }
                case 52:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "52_SMFirmOrderPurchase.html";
                        break;
                    }
                case 12:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "12_Cancellation of Subscription.html";
                        break;
                    }
                case 19:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "19_Binding Order for Serials Volumes.html";
                        break;
                    }
                case 18:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "18_Serials Binding invoice payment.html";
                        break;
                    }
                case 15:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "15_Serials Binding - Logical List.html";
                        break;
                    }
                case 21:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "21_Serials subscription Invoice Payment.html";
                        break;
                    }
                case 54:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "54_Renew Subscription.html";
                        break;
                    }
                case 22:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "22_Claim for serial IssuesTitlepageIndexpage.html";
                        break;
                    }
                case 2:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "2_Pay towards ILL courier charges.html";
                        break;
                    }
                case 3:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "3_Binder contains the details of the documents checked out.html";
                        break;
                    }
                case 4:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "4_Reservation notice.html";
                        break;
                    }
                case 5:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "5_Intimation  to Patron of ILL Request.html";
                        break;
                    }
                case 6:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "6_Recall notice.html";
                        break;
                    }
                case 7:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "7_Request for Inter Library Loan.html";
                        break;
                    }
                case 9:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "9_Request for Inter Library Loan Rejection.html";
                        break;
                    }
                case 10:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "10_Return of loaned item.html";
                        break;
                    }
                case 29:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "29_Check out slip.html";
                        break;
                    }
                case 30:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "30_Check in slip.html";
                        break;
                    }
                case 43:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "43_Collection of Overdue charges.html";
                        break;
                    }
                case 51:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "51_Renewal Slip.html";
                        break;
                    }
                case 55:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "55_Consolidated Check in slip.html";
                        break;
                    }
                case 47:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "47_Advance Payment-firmOrder.html";
                        break;
                    }
                case 53:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "53_Overdue notice.html";
                        break;
                    }
                case 33:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "33_Request for On-approval Supplies.html";
                        break;
                    }
                case 34:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "34_Receipt of on-approval supplies.html";
                        break;
                    }
                case 35:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "35_Return of on-approval supplies.html";
                        break;
                    }
                case 38:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "38_Request For invoice for on-approval supplies.html";
                        break;
                    }
                case 39:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "39_firmOrder advance payment on-approval supplies.html";
                        break;
                    }
                case 56:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "56_Cancellation of firm Orders.html";
                        break;
                    }
                case 57:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "57_renewal notice.html";
                        break;
                    }
                case 58:
                    {
                        System.out.println(reports.utility.NewGenLibDesktopRoot.getRoot());
                        System.out.println(java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath"));
                        System.out.println("/LIB_" + libraryId.toString());
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "58_SDIProfile.html";
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
            System.out.println("==============================================fine name" + fileName);
            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(fileName)));
            while (br.ready()) {
                str += br.readLine() + " \n";
            }
            br.close();
            System.out.println("str: " + str);
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
                System.out.println("liTables count is: " + liTables.size());
                org.jdom.Element newgenlibtable = null;
                for (int i = 0; i < liTables.size(); i++) {
                    System.out.println("Entered for loop");
                    org.jdom.Element eleTab = (org.jdom.Element) liTables.get(i);
                    org.jdom.output.XMLOutputter xout = new org.jdom.output.XMLOutputter();
                    String xmldet = xout.outputString(eleTab);
                    System.out.println("xmldet: " + xmldet);
                    if (xmldet.toUpperCase().indexOf("NEWGENLIBTABLE") != -1) {
                        newgenlibtable = eleTab;
                    }
                }
                if (newgenlibtable == null) newgenlibtable = new org.jdom.Element("TABLE");
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
                fileValid = reports.utility.NewGenLibDesktopRoot.getRoot() + "/Reports";
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
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "46_FirmOrderPurchase.odt";
                        break;
                    }
                case 14:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "14_Receive for OnApproval.odt";
                        break;
                    }
                case 16:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "16_Payment of Invoice.odt";
                        break;
                    }
                case 17:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "17_Reconciliation of Advance Payment.odt";
                        break;
                    }
                case 40:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "40_Request For Approval (Budget Heads).odt";
                        break;
                    }
                case 32:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "32_Rejection of Requests for addition of items to library.odt";
                        break;
                    }
                case 48:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "48_Intimation to Requester when book arrives.odt";
                        break;
                    }
                case 44:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "44_Invoice Not Received.odt";
                        break;
                    }
                case 45:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "45_Item Not Received.odt";
                        break;
                    }
                case 36:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "36_Request for Donation of item.odt";
                        break;
                    }
                case 37:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "37_Receive for Donation of item.odt";
                        break;
                    }
                case 27:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "27_Claims for unfulfilled orders.odt";
                        break;
                    }
                case 28:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "28_Listing for unfulfilled orders (by budget heads).odt";
                        break;
                    }
                case 31:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "31_No Dues Certificate.odt";
                        break;
                    }
                case 42:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "42_Loss of Book and charges recovered Report.odt";
                        break;
                    }
                case 52:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "52_SMFirmOrderPurchase.odt";
                        break;
                    }
                case 12:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "12_Cancellation of Subscription.odt";
                        break;
                    }
                case 19:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "19_Binding Order for Serials Volumes.odt";
                        break;
                    }
                case 18:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "18_Serials Binding invoice payment.odt";
                        break;
                    }
                case 15:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "15_Serials Binding - Logical List.odt";
                        break;
                    }
                case 21:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "21_Serials subscription Invoice Payment.odt";
                        break;
                    }
                case 54:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "54_Renew Subscription.odt";
                        break;
                    }
                case 22:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "22_Claim for serial IssuesTitlepageIndexpage.odt";
                        break;
                    }
                case 2:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "2_Pay towards ILL courier charges.odt";
                        break;
                    }
                case 3:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "3_Binder contains the details of the documents checked out.odt";
                        break;
                    }
                case 4:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "4_Reservation notice.odt";
                        break;
                    }
                case 5:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "5_Intimation  to Patron of ILL Request.odt";
                        break;
                    }
                case 6:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "6_Recall notice.odt";
                        break;
                    }
                case 7:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "7_Request for Inter Library Loan.odt";
                        break;
                    }
                case 9:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "9_Request for Inter Library Loan Rejection.odt";
                        break;
                    }
                case 10:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "10_Return of loaned item.odt";
                        break;
                    }
                case 29:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "29_Check out slip.odt";
                        break;
                    }
                case 30:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "30_Check in slip.odt";
                        break;
                    }
                case 43:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "43_Collection of Overdue charges.odt";
                        break;
                    }
                case 51:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "51_Renewal Slip.odt";
                        break;
                    }
                case 55:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "55_Consolidated Check in slip.odt";
                        break;
                    }
                case 47:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "47_Advance Payment-firmOrder.odt";
                        break;
                    }
                case 53:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "53_Overdue notice.odt";
                        break;
                    }
                case 33:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "33_Request for On-approval Supplies.odt";
                        break;
                    }
                case 34:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "34_Receipt of on-approval supplies.odt";
                        break;
                    }
                case 35:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "35_Return of on-approval supplies.odt";
                        break;
                    }
                case 38:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "38_Request For invoice for on-approval supplies.odt";
                        break;
                    }
                case 39:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "39_firmOrder advance payment on-approval supplies.odt";
                        break;
                    }
                case 56:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "56_Cancellation of firm Orders.odt";
                        break;
                    }
                case 57:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "57_renewal notice.odt";
                        break;
                    }
                case 58:
                    {
                        fileName = reports.utility.NewGenLibDesktopRoot.getRoot() + java.util.ResourceBundle.getBundle("reports.utility.server").getString("TemplatesPath") + "/LIB_" + libraryId.toString() + "/" + "58_SDIProfile.odt";
                        break;
                    }
            }
            System.out.println("Filenname: " + fileName);
            java.io.BufferedInputStream is = null;
            java.util.zip.ZipEntry entry;
            java.util.jar.JarFile zipfile = new java.util.jar.JarFile(fileName);
            java.util.Enumeration e = zipfile.entries();
            String subTempDirectory = "";
            boolean flag = true;
            int countFile = 0;
            while (flag) {
                String direcValid = reports.utility.NewGenLibDesktopRoot.getRoot() + "/Temp";
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
                System.out.println("Extracting: " + entry);
                is = new java.io.BufferedInputStream(zipfile.getInputStream(entry));
                int count;
                byte data[] = new byte[BUFFER];
                java.io.File tempDirectory = new java.io.File(reports.utility.NewGenLibDesktopRoot.getRoot() + "/Temp");
                if (!tempDirectory.exists()) tempDirectory.mkdirs();
                java.io.FileOutputStream fos = new java.io.FileOutputStream(subTempDirectory + "/" + entry.getName());
                java.io.BufferedOutputStream dest = new java.io.BufferedOutputStream(fos, BUFFER);
                while ((count = is.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
                is.close();
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
                System.out.println("Adding: " + files[i]);
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
                        System.out.println("str: " + str);
                        java.text.MessageFormat mf = new java.text.MessageFormat(str);
                        str = mf.format(str, obx);
                        System.out.println("str: " + str);
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
                    System.out.println(fi.getFD().valid());
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
