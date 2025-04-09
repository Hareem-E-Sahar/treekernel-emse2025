import uiuc.oai.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;
import java.text.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;
import javax.xml.parsers.*;
import org.apache.xalan.serialize.*;
import org.apache.xalan.templates.OutputProperties;
import org.apache.xpath.*;
import org.apache.xpath.objects.*;
import org.apache.xml.utils.*;
import org.w3c.dom.*;
import org.xml.sax.*;

public class OAIHarvesterTest {

    public OAIHarvesterTest() {
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream("OAI.ini"));
            Class.forName(prop.getProperty("Driver")).newInstance();
            conn = DriverManager.getConnection(prop.getProperty("Database"), prop.getProperty("Username"), prop.getProperty("Password"));
            try {
                maxNumOfFilesPerDir = Integer.parseInt(prop.getProperty("MaxNumOfFilesPerDir"));
            } catch (NumberFormatException ne) {
                maxNumOfFilesPerDir = 0;
            }
            if (prop.getProperty("ParseID", "false").toLowerCase().equals("true")) {
                parseID = true;
            } else {
                parseID = false;
            }
            java.util.Date from = null;
            java.util.Date to = null;
            SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd");
            try {
                from = fm.parse(prop.getProperty("HarvestFrom"));
                to = fm.parse(prop.getProperty("HarvestTo"));
            } catch (NullPointerException ne) {
            } catch (ParseException pe) {
            }
            if (prop.getProperty("Debug") != null && prop.getProperty("Debug").trim().equals("1")) {
                debug = true;
            } else {
                debug = false;
            }
            hInfo = new HarvesterInfo(Integer.parseInt(prop.getProperty("repositoryID")), Integer.parseInt(prop.getProperty("jobID")), prop.getProperty("Set"), Integer.parseInt(prop.getProperty("Method")), from, to, prop.getProperty("Prefix"), Integer.parseInt(prop.getProperty("recurringID")), prop.getProperty("Filter"), prop.getProperty("Normalize"), Integer.parseInt(prop.getProperty("Validation")));
            total = 0;
            subtotal = 0;
            warningCount = 0;
            strWarning = "";
            xmlFilter = null;
            subdir = "";
            basedir = "";
            stopFlag = false;
            repo = new OAIRepository();
            if (debug) {
                System.out.println("Contact Repository successfully!");
            }
            if (prop.getProperty("UserAgent") != null) {
                repo.setUserAgent(prop.getProperty("UserAgent"));
            }
            if (prop.getProperty("From") != null) {
                repo.setFrom(prop.getProperty("From"));
            }
        } catch (Exception e) {
            System.out.println(e);
            writeHistory(STATUS_FAILED, e.getMessage());
        }
        if (prepareHarvestCommand() == false) {
            writeHistory(STATUS_FAILED, "Failed to prepare harvest command.");
            System.exit(1);
        }
        if (debug) {
            System.out.println("Prepare harvest command successfully!");
        }
        if (initXSLNormalize() == false) {
            writeHistory(STATUS_FAILED, "Failed to initialize XSL Normalize.");
        }
        if (debug) {
            System.out.println("Initialize XSL normalization successfully!");
        }
        if (initFilter() == false) {
            writeHistory(STATUS_FAILED, "Failed to initialize XML filter.");
            System.exit(1);
        }
        if (debug) {
            System.out.println("Initialize XML filter successfully!");
        }
        if (initNormalize() == false) {
            writeHistory(STATUS_FAILED, "Failed to initialize XML normalization.");
            System.exit(1);
        }
        if (debug) {
            System.out.println("Initialize XML normalization successfully!");
        }
        if (harvest()) {
            writeHistory(STATUS_SUCCESS, "Success");
        }
        return;
    }

    public void terminate() {
        stopFlag = true;
        writeHistory(STATUS_FAILED, "Terminated by OAIManager.");
        return;
    }

    /**
	 * Purpose: Harvest Records from a repository
	 *
	 * Returns: True if the harvest is successful, else False
	 */
    private boolean harvest() {
        boolean ret = false;
        OAIRecordList rlist = null;
        OAIRecord r = null;
        String msg = "";
        try {
            msg = "Initialize the OAIRepository";
            repo.setValidation(hInfo.getValidation());
            repo.setBaseURL(hInfo.getBaseURL());
            if (hInfo.getHarvestMethod().equals("ListIdentifiers")) {
                msg = "ListIdentifiers";
                if (debug) {
                    System.out.println("Waiting for the result from ListIdentifiers...");
                }
                rlist = repo.listIdentifiers(hInfo.getHarvestTo(), hInfo.getHarvestFrom(), hInfo.getSetSpec());
            } else {
                msg = "ListRecords";
                if (debug) {
                    System.out.println("Waiting for the result from ListRecords...");
                }
                rlist = repo.listRecords(hInfo.getMetadataPrefix(), hInfo.getHarvestTo(), hInfo.getHarvestFrom(), hInfo.getSetSpec());
            }
            ret = true;
            while (rlist.moreItems() && !stopFlag) {
                try {
                    r = rlist.getCurrentItem();
                    if (r != null) {
                        if (debug) {
                            System.out.println(r.getIdentifier());
                        }
                        if (r.isIdentifierOnly()) {
                            r.refreshRecord(hInfo.getMetadataPrefix());
                        }
                        msg = checkXMLFile(r.getRecord(), r.getIdentifier(), r.getDatestamp(), r.getStatus(), r.isRecordValid());
                        if (rlist.isListValid() == false || r.isRecordValid() == false) {
                            warningCount++;
                            strWarning = "WARNING: An invalid response was returned: " + msg + ".";
                        }
                    }
                } catch (OAIException ex) {
                    writeHistory(STATUS_FAILED, msg + ": (" + ex.code + ") " + ex.getMessage());
                }
                try {
                    rlist.moveNext();
                } catch (OAIException ex) {
                    writeHistory(STATUS_FAILED, msg + ": (" + ex.code + ") " + ex.getMessage());
                    stopFlag = true;
                    ret = false;
                }
            }
        } catch (OAIException oe) {
            ret = false;
            writeHistory(STATUS_FAILED, msg + ": (" + oe.code + ") " + oe.getMessage());
        }
        return ret;
    }

    private void writeHistory(int st, String msg) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            String sql = "Insert Into OAIHistory (repositoryID, setSpec, harvestStartTime, harvestEndTime, recordCount, ";
            if (warningCount > 0) {
                if (st == STATUS_SUCCESS) {
                    st = STATUS_WARNING;
                }
                msg += " (WARNINGS: " + warningCount + " Last Warning: " + strWarning + ")";
            }
            System.out.println(msg + " - " + total + " records received.");
            sql += "jobID, status, failureReason, recurringID) Values(";
            sql += hInfo.getRepositoryID() + ", ";
            sql += "'" + hInfo.getSetSpec() + "', ";
            if (hInfo.getHarvestStartTime() != null) {
                sql += "'" + formatter.format(hInfo.getHarvestStartTime()) + "', ";
            } else {
                sql += "null, ";
            }
            sql += "'" + formatter.format(new java.util.Date()) + "', ";
            sql += total + ", ";
            sql += hInfo.getJobID() + ", ";
            sql += st + ", ";
            sql += "'" + msg + "', ";
            sql += hInfo.getRecurringID() + ");";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (SQLException se) {
            System.out.println("Cannot update OAIHistory with '" + msg + "'.");
        }
    }

    /**
	 * Purpose:   Update the database and save the XML file
	 *
	 * Inputs:    node	an XML node containing the metadata record
	 *                      (the XML starting at the <record> tag)
	 *            identifier  the OAI identifier of the record, used to generate a file name
	 *	      ds	the OAI datestamp of the record, used to check if the record has been changed
	 *	      status	the OAI status of the record
	 *            valid	True if this is a valid record, else False
	 * 			Invalid records are saved with a different file extension
	 */
    private String checkXMLFile(Node node, String identifier, String ds, String status, boolean valid) {
        Node newNode;
        SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String xmlFilename = "";
        java.util.Date datestamp = new java.util.Date();
        String strDatestamp = "";
        int i = 0;
        if (includeRecord(node) == false) {
            return identifier;
        }
        normalizeRecord(node);
        newNode = normalizeXML(node);
        total++;
        subtotal++;
        xmlFilename = createFileName(identifier);
        if (valid == false) {
            xmlFilename += ".not";
        }
        if (ds != null) {
            try {
                datestamp = formatter1.parse(ds);
                strDatestamp = ds;
            } catch (ParseException pe) {
                strDatestamp = formatter1.format(new java.util.Date());
            }
        } else {
            strDatestamp = formatter1.format(datestamp);
        }
        if (status.equals("deleted")) {
            xmlFilename += ".del";
        }
        try {
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rst = stmt.executeQuery("Select * from OAIRecord where repositoryID=" + hInfo.getRepositoryID() + " And identifier='" + identifier + "';");
            String sql;
            boolean saveFile = false;
            int recordID = 0;
            if (rst.next() == false) {
                rst.close();
                sql = "Insert into OAIRecord ";
                sql += "(repositoryID, identifier, filename, harvestTime,";
                sql += "datestamp, status, metadataPrefix, updateFlag) values(";
                sql += hInfo.getRepositoryID() + ",";
                sql += "'" + identifier + "',";
                sql += "'" + xmlFilename + "',";
                sql += "'" + formatter2.format(new java.util.Date()) + "',";
                sql += "'" + strDatestamp + "',";
                sql += "'" + status + "',";
                sql += "'" + hInfo.getMetadataPrefix() + "',";
                sql += FLAG_RECORD_NEW + ");";
                stmt.executeUpdate(sql);
                rst = stmt.executeQuery("Select recordID from OAIRecord where repositoryID=" + hInfo.getRepositoryID() + " And identifier='" + identifier + "';");
                rst.next();
                recordID = rst.getInt("recordID");
                saveFile = true;
                rst.close();
            } else {
                recordID = rst.getInt("recordID");
                String rstFilename = fixNull(rst.getString("filename"));
                java.sql.Date rstDatestamp = rst.getDate("datestamp");
                String rstStatus = rst.getString("status");
                rst.close();
                sql = "Update OAIRecord Set ";
                if (!rstDatestamp.equals(datestamp) || !fixPendingStatus(rstStatus).equals(fixNull(status))) {
                    sql += "harvestTime = '" + formatter2.format(new java.util.Date()) + "', ";
                    sql += "datestamp = '" + strDatestamp + "', ";
                    sql += "status = '" + status + "', ";
                    sql += "updateFlag = " + FLAG_RECORD_MODIFIED;
                    File file = new File(rstFilename);
                    if (fixNull(status).equals("deleted") && !rstFilename.endsWith(".del")) {
                        file.renameTo(new File(rstFilename + ".del"));
                        sql += ", filename = '" + rstFilename + ".del" + "'";
                    } else if (!fixNull(status).equals("deleted") && rstFilename.endsWith(".del")) {
                        file.renameTo(new File(rstFilename.substring(0, rstFilename.length() - 4)));
                        sql += ", filename = '" + rstFilename.substring(0, rstFilename.length() - 4) + "'";
                        saveFile = true;
                        xmlFilename = rstFilename.substring(0, rstFilename.length() - 4);
                    } else {
                        saveFile = true;
                        xmlFilename = rstFilename;
                    }
                } else {
                    sql += "status = '" + status + "', ";
                    sql += "updateFlag = " + FLAG_RECORD_OLD;
                }
                sql += " Where recordID = " + recordID + ";";
                stmt.executeUpdate(sql);
            }
            if (hInfo.getSetSpec().length() > 0) {
                rst = stmt.executeQuery("Select * from OAISetMapping where recordID=" + recordID + " And setSpec='" + hInfo.getSetSpec() + "';");
                if (rst.next() == false) {
                    stmt.executeUpdate("Insert Into OAISetMapping (recordID, setSpec) Values(" + recordID + ", '" + hInfo.getSetSpec() + "');");
                }
                rst.close();
            }
            if (saveFile) {
                try {
                    File xmlFile = new File(xmlFilename);
                    if (xmlFile.exists() == false) {
                        boolean ret = xmlFile.createNewFile();
                    }
                    Serializer serializer = SerializerFactory.getSerializer(OutputProperties.getDefaultMethodProperties("xml"));
                    serializer.setOutputStream(new FileOutputStream(xmlFile));
                    serializer.asDOMSerializer().serialize(newNode);
                    xmlFile = null;
                } catch (IOException ie) {
                    writeHistory(STATUS_FAILED, "Failed to write to file.  Reason:  " + ie.getMessage() + " " + xmlFilename);
                }
            }
            stmt.close();
        } catch (SQLException se) {
            writeHistory(STATUS_FAILED, "Error: SQLState: " + se.getMessage());
        }
        return xmlFilename;
    }

    private String createFileName(String identifier) {
        String xmlFilename;
        int i = 0;
        xmlFilename = identifier;
        if (parseID) {
            xmlFilename = xmlFilename.replace(':', File.separatorChar);
            xmlFilename = xmlFilename.replace('/', File.separatorChar);
            xmlFilename = xmlFilename.replace('\\', File.separatorChar);
            xmlFilename = xmlFilename.replace('|', File.separatorChar);
            if (xmlFilename.startsWith("oai" + File.separator)) {
                xmlFilename = xmlFilename.substring(xmlFilename.indexOf(File.separatorChar) + 1);
            }
        } else {
            while ((i = xmlFilename.indexOf(File.separatorChar)) >= 0) {
                xmlFilename = xmlFilename.substring(0, i) + "_" + Character.digit(File.separatorChar, 16) + xmlFilename.substring(i + 1);
            }
        }
        i = xmlFilename.lastIndexOf(File.separatorChar, xmlFilename.length() - 2);
        if (maxNumOfFilesPerDir != 0) {
            if ((subtotal % maxNumOfFilesPerDir) == 0) {
                subdir = "starting_at_" + xmlFilename.substring(i + 1) + File.separator;
            }
        }
        if (i >= 0) {
            if (xmlFilename.substring(0, i).toLowerCase().equals(basedir) == false) {
                subdir = "";
                subtotal = 0;
                basedir = xmlFilename.substring(0, i).toLowerCase();
            }
        }
        xmlFilename = xmlFilename.substring(0, i + 1) + subdir + xmlFilename.substring(i + 1);
        xmlFilename = hInfo.getBaseDirectory() + (hInfo.getBaseDirectory().endsWith(File.separator) ? "" : File.separator) + escapeForbiddenChars(xmlFilename);
        File newDir = new File(xmlFilename.substring(0, xmlFilename.lastIndexOf(File.separator)));
        newDir.mkdirs();
        newDir = null;
        if (xmlFilename.endsWith(".xml") == false) {
            xmlFilename += ".xml";
        }
        return xmlFilename;
    }

    private String fixNull(String s) {
        String ret = "";
        if (s != null) {
            ret = s;
        }
        return ret;
    }

    private String escapeForbiddenChars(String s) {
        String str = s;
        int i;
        while ((i = str.indexOf('%')) >= 0) {
            str = str.substring(0, i) + "_25" + str.substring(i + 1);
        }
        if (File.separatorChar != '/') {
            while ((i = str.indexOf('/')) >= 0) {
                str = str.substring(0, i) + "_5C" + str.substring(i + 1);
            }
        }
        while ((i = str.indexOf(':')) >= 0) {
            str = str.substring(0, i) + "_3A" + str.substring(i + 1);
        }
        while ((i = str.indexOf('*')) >= 0) {
            str = str.substring(0, i) + "_2A" + str.substring(i + 1);
        }
        while ((i = str.indexOf('?')) >= 0) {
            str = str.substring(0, i) + "_3F" + str.substring(i + 1);
        }
        while ((i = str.indexOf('\"')) >= 0) {
            str = str.substring(0, i) + "_22" + str.substring(i + 1);
        }
        while ((i = str.indexOf('<')) >= 0) {
            str = str.substring(0, i) + "_3C" + str.substring(i + 1);
        }
        while ((i = str.indexOf('>')) >= 0) {
            str = str.substring(0, i) + "_3E" + str.substring(i + 1);
        }
        while ((i = str.indexOf('|')) >= 0) {
            str = str.substring(0, i) + "_7C" + str.substring(i + 1);
        }
        if (File.separatorChar != '\\') {
            while ((i = str.indexOf('\\')) >= 0) {
                str = str.substring(0, i) + "_2F" + str.substring(i + 1);
            }
        }
        return str;
    }

    /**
	 * Purpose: Return the status of a record before it was changed to pending
	 */
    private String fixPendingStatus(String s) {
        String ret = fixNull(s);
        if (ret.startsWith("delete-")) {
            ret = "";
        } else if (ret.startsWith("pending")) {
            ret = "deleted";
        }
        return ret;
    }

    /**
	 *  Purpose:   create an XSLT used to normalize records
	 *
	 */
    private boolean initXSLNormalize() {
        String xsls;
        boolean ret = false;
        xsls = "<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>\n";
        xsls += "<xsl:output method='xml' encoding='UTF-8' omit-xml-declaration='no'/>\n";
        xsls += "<xsl:template match='/'>\n";
        xsls += "<xsl:text>\n";
        xsls += "</xsl:text>\n";
        xsls += "<xsl:apply-templates select='@*|node()'/>\n";
        xsls += "</xsl:template>\n";
        xsls += "<xsl:template match='@xsi:*'>";
        xsls += "<xsl:attribute name='schemaLocation' namespace='http://www.w3.org/2001/XMLSchema-instance'>";
        xsls += "<xsl:value-of select='.' />";
        xsls += "</xsl:attribute>";
        xsls += "</xsl:template>";
        xsls += "<xsl:template match='@*|node()'>\n";
        xsls += "<xsl:copy>\n";
        xsls += "<xsl:apply-templates select='@*|node()'/>\n";
        xsls += "</xsl:copy>\n";
        xsls += "</xsl:template>\n";
        xsls += "<xsl:template match='text()'>\n";
        xsls += "<xsl:value-of select='normalize-space(.)'/>\n";
        xsls += "</xsl:template>\n";
        xsls += "<xsl:template match='*'>\n";
        xsls += "<xsl:value-of select=\"substring('                    ',1,2 * count(ancestor-or-self::*)) \"/>\n";
        xsls += "<xsl:element name='{local-name(.)}'>";
        xsls += "<xsl:apply-templates select='@*'/>\n";
        xsls += "<xsl:if test='*'>\n";
        xsls += "<xsl:text>\n";
        xsls += "</xsl:text>\n";
        xsls += "</xsl:if>\n";
        xsls += "<xsl:apply-templates select='node()'/>\n";
        xsls += "<xsl:if test='*'>\n";
        xsls += "<xsl:value-of select=\"substring('                    ',1,2 * count(ancestor-or-self::*))\"/>\n";
        xsls += "</xsl:if>\n";
        xsls += "</xsl:element>\n";
        xsls += "<xsl:if test='following-sibling::* or ancestor::*'>\n";
        xsls += "<xsl:text>\n";
        xsls += "</xsl:text>\n";
        xsls += "</xsl:if>\n";
        xsls += "</xsl:template>\n";
        xsls += "</xsl:stylesheet>\n";
        try {
            StringReader sr = new StringReader(xsls);
            InputSource is = new InputSource(sr);
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setValidating(false);
            docFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            xslNormalize = docBuilder.parse(is);
            ret = true;
            sr = null;
            is = null;
            docFactory = null;
            docBuilder = null;
        } catch (IllegalArgumentException iae) {
            writeHistory(STATUS_FAILED, "The xslNormalize is not valid. Reason: " + iae.getMessage());
        } catch (IOException ie) {
            writeHistory(STATUS_FAILED, "The xslNormalize is not valid. Reason: " + ie.getMessage());
        } catch (SAXException se) {
            writeHistory(STATUS_FAILED, "The xslNormalize is not valid. Reason: " + se.getMessage());
        } catch (FactoryConfigurationError fce) {
            writeHistory(STATUS_FAILED, "The xslNormalize is not valid. Reason: " + fce.getMessage());
        } catch (ParserConfigurationException pce) {
            writeHistory(STATUS_FAILED, "The xslNormalize is not valid. Reason: " + pce.getMessage());
        }
        xsls = null;
        return ret;
    }

    /**
	 * Purpose:   Normalize an XML file to remove all namespace prefixes and use default
	 *                namespaces instead, such as:'
	 *
	 *                <dc:dc xmlns:dc="....">
	 *                  <dc:creator>....</dc:creator>
	 *                  <dc:title>....</dc:creator>
	 *                </dc:dc>'
	 *
	 *                 becomes'
	 *
	 *                <dc xmlns="....">
	 *                  <creator>....</creator>
	 *                  <title>....</creator>
	 *                </dc>
	 *
	 *                Also, normalize the space inside of all text strings, and introduce
	 *                new lines and indentation as appropriate
	 *
	 * Returns: The normalized XML
	 *
	 */
    private Node normalizeXML(Node node) {
        Node ret = node;
        try {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer(new DOMSource(xslNormalize));
            DOMResult dr = new DOMResult();
            transformer.transform(new DOMSource(node), dr);
            ret = dr.getNode();
            tFactory = null;
            transformer = null;
        } catch (TransformerException te) {
            te.printStackTrace();
            writeHistory(STATUS_FAILED, "The 'Normalized XML' is not valid.  Reason:  " + te.getMessage());
        }
        return ret;
    }

    /**
	 * Purpose: Initialize the XML file used to filter OAi records
	 *
	 * Returns: True if the filter could be initialized, else False
	 */
    private boolean initFilter() {
        boolean ret = false;
        if (hInfo.getFilterFile().length() > 0) {
            try {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                docFactory.setValidating(false);
                docFactory.setNamespaceAware(false);
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                xmlFilter = docBuilder.parse(new File(hInfo.getFilterFile()));
                docFactory = null;
                docBuilder = null;
                ret = true;
            } catch (IllegalArgumentException iae) {
                writeHistory(STATUS_FAILED, "The " + hInfo.getFilterFile() + " is not valid. Reason: " + iae.getMessage());
            } catch (IOException ie) {
                writeHistory(STATUS_FAILED, "The " + hInfo.getFilterFile() + "is not valid. Reason: " + ie.getMessage());
            } catch (SAXException se) {
                writeHistory(STATUS_FAILED, "The " + hInfo.getFilterFile() + "is not valid. Reason: " + se.getMessage());
            } catch (FactoryConfigurationError fce) {
                writeHistory(STATUS_FAILED, "The " + hInfo.getFilterFile() + "is not valid. Reason: " + fce.getMessage());
            } catch (ParserConfigurationException pce) {
                writeHistory(STATUS_FAILED, "The " + hInfo.getFilterFile() + "is not valid. Reason: " + pce.getMessage());
            }
        } else {
            ret = true;
        }
        return ret;
    }

    /**
	 * Purpose:   Use a filter to determine whether a given record should be
	 *            included or not
	 *
	 * Inputs:    doc       an XML Document containing the metadata record
	 *
	 * Returns:   True if the record should be included, else false
	 *
	 * NOTE:  The function uses a filterFile whose name is contained in the
	 *        hInfo structure.  The filterFIle is an XML file with the
	 *        following structure
	 *
	 *        <filters match="any|all">
	 *            <selectionNamespaces>...</selectionNamespaces>
	 *            <filter invert="true|false" xpath="..." regexp="..." ignoreCase="true|false" />
	 *            <filter invert="true|false" xpath="..." regexp="..." ignoreCase="true|false" />
	 *            ...
	 *        </filters>
	 *
	 *        The filters/@match attribute indicates whether all of the filters
	 *        must be matched to include a record or whether any of them matching
	 *        is enough.  The selectionNamespaces contains a space separated list
	 *        of namespace declarations, such as used with DOM selectionNamespaces
	 *        property.
	 *
	 *        The filter/@invert attribute indicates whether that filter
	 *        should be inverted (boolean NOT) or not.  The filter/@xpath
	 *        attribute contains an xpath expression used to determine the nodes
	 *        to which to apply the filter/@regexp string.  The "filter/@ignoreCase
	 *        attribute indicates whether the regexp should ignore case or not
	 */
    private boolean includeRecord(Node node) {
        boolean ret = false;
        String match = "any";
        NamedNodeMap map;
        String xpath;
        String regexp;
        boolean invert;
        boolean ic;
        Pattern p;
        if (hInfo.getFilterFile().length() == 0) {
            ret = true;
        } else {
            if (xmlFilter == null) {
                writeHistory(STATUS_FAILED, "The '" + hInfo.getFilterFile() + "' could not be loaded.");
            } else {
                try {
                    Element namespaceNode = xmlFilter.createElement("filter");
                    NodeList list = xmlFilter.getElementsByTagName("filters");
                    if (list.getLength() > 0) {
                        Node matchNode = list.item(0).getAttributes().getNamedItem("match");
                        if (matchNode != null) {
                            match = matchNode.getNodeValue().toLowerCase();
                        }
                    }
                    list = xmlFilter.getElementsByTagName("selectionNamespaces");
                    if (list.getLength() > 0) {
                        StringTokenizer st = new StringTokenizer(list.item(0).getFirstChild().getNodeValue());
                        while (st.hasMoreTokens()) {
                            String t = st.nextToken();
                            namespaceNode.setAttribute(t.substring(0, t.indexOf('=')), t.substring(t.indexOf('=') + 1));
                        }
                    } else {
                        namespaceNode.setAttribute("xmlns:oai_dc", "http://purl.org/dc/elements/1.1/");
                        namespaceNode.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
                        namespaceNode.setAttribute("xmlns:rfc1807", "http://info.internet.isi.edu:80/in-notes/rfc/files/rfc1807.txt");
                        namespaceNode.setAttribute("xmlns:oai_marc", "http://www.openarchives.org/OAI/1.1/oai_marc");
                    }
                    NodeList nlist = xmlFilter.getElementsByTagName("filter");
                    for (int i = 0; i < nlist.getLength(); i++) {
                        map = nlist.item(i).getAttributes();
                        if (map.getNamedItem("xpath") != null) {
                            xpath = map.getNamedItem("xpath").getNodeValue();
                            if (!xpath.startsWith(".")) {
                                xpath = "." + xpath;
                            }
                        } else {
                            xpath = "";
                        }
                        if (map.getNamedItem("regexp") != null) {
                            regexp = map.getNamedItem("regexp").getNodeValue();
                        } else {
                            regexp = "";
                        }
                        if (map.getNamedItem("invert") != null) {
                            invert = map.getNamedItem("invert").getNodeValue().toLowerCase().equals("true");
                        } else {
                            invert = false;
                        }
                        if (map.getNamedItem("ignoreCase") != null) {
                            ic = map.getNamedItem("ignoreCase").getNodeValue().toLowerCase().equals("true");
                        } else {
                            ic = false;
                        }
                        if (ic) {
                            p = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);
                        } else {
                            p = Pattern.compile(regexp, Pattern.MULTILINE);
                        }
                        NodeList nlist2 = XPathAPI.selectNodeList(node, xpath, namespaceNode);
                        for (int j = 0; j < nlist2.getLength(); j++) {
                            if (nlist2.item(j).getFirstChild() == null) {
                                continue;
                            }
                            String txt = nlist2.item(j).getFirstChild().getNodeValue();
                            Matcher m = p.matcher(txt);
                            boolean bool = m.find();
                            if (match.equals("any") && bool) {
                                return true;
                            } else if (match.equals("all") && !bool) {
                                return false;
                            }
                        }
                    }
                } catch (TransformerException te) {
                    writeHistory(STATUS_FAILED, "Failed to filter records.");
                }
            }
        }
        return ret;
    }

    /**
	 * Purpose: Initialize the XML file used to normalize OAi records
	 *
	 * Returns: True if the filter could be initialized, else False
	 */
    private boolean initNormalize() {
        boolean ret = false;
        if (hInfo.getNormalizeFile().length() > 0) {
            try {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                docFactory.setValidating(false);
                docFactory.setNamespaceAware(false);
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                xmlNormalize = docBuilder.parse(new File(hInfo.getNormalizeFile()));
                docFactory = null;
                docBuilder = null;
                ret = true;
            } catch (IllegalArgumentException iae) {
                writeHistory(STATUS_FAILED, "The " + hInfo.getNormalizeFile() + " is not valid. Reason: " + iae.getMessage());
            } catch (IOException ie) {
                writeHistory(STATUS_FAILED, "The " + hInfo.getNormalizeFile() + "is not valid. Reason: " + ie.getMessage());
            } catch (SAXException se) {
                writeHistory(STATUS_FAILED, "The " + hInfo.getNormalizeFile() + "is not valid. Reason: " + se.getMessage());
            } catch (FactoryConfigurationError fce) {
                writeHistory(STATUS_FAILED, "The " + hInfo.getNormalizeFile() + "is not valid. Reason: " + fce.getMessage());
            } catch (ParserConfigurationException pce) {
                writeHistory(STATUS_FAILED, "The " + hInfo.getNormalizeFile() + "is not valid. Reason: " + pce.getMessage());
            }
        } else {
            ret = true;
        }
        return ret;
    }

    /**
	 * Purpose:   normalize the record by adding some fields if it contains
	 *            some certain values
	 *
	 * Inputs:    myDom       an XML DOM containing the metadata record
	 *
	 * Returns:   the same XLM DOM with zero or more new elements added
	 *
	 * NOTE:  The function uses a normalizeFile whose name is contained in the
	 *        hInfo structure.  The normalizeFile is an XML file with the
	 *        following structure
	 *
	 *
	 *        <normalize>
	 *           <selectionNamespaces>...</selectionNamespaces>
	 *           <prefix>...</prefix>
	 *            <mapping xpath="...">
	 *                <newElement regexp="..." ignoreCase="true|false" name="...">
	 *                    <newValue>...</newValue>
	 *                </newElement>
	 *            </mapping>
	 *        </normalize>
	 *
	 *
	 *       The selectionNamespaces element contains a space seperated list
	 *       of namespace declarations, such as used with DOM selectionNamespaces
	 *       property.
	 *
	 *       The prefix element can be added to discriminate the newly added elements from existing
	 *       elements.
	 *
	 *       The mapping/@xpath attribute contains an xpath expression used to determine
	 *       the nodes to which to apply normalization. The newElement element is the
	 *       element to be added to the document if the node value matches
	 *       newElement/@regexp. The newElement/@ignoreCase attribute indicates whether
 	 *       the regexp should ignore case or not. The newElement/@name attribute is the
	 *       name of the newly added element. The newValue element is the value of the new
	 *       element. If multiple newValue elements are specifed, mutiple newElement will
	 *       be created.
	 */
    private void normalizeRecord(Node node) {
        String prefix = "";
        NamedNodeMap map;
        String xpath;
        String regexp;
        String nName;
        boolean ic;
        Pattern p;
        boolean bool;
        if (hInfo.getNormalizeFile().length() > 0) {
            if (xmlNormalize == null) {
                writeHistory(STATUS_FAILED, "The '" + hInfo.getNormalizeFile() + "' could not be loaded.");
            } else {
                try {
                    Element namespaceNode = xmlNormalize.createElement("normalize");
                    NodeList list = xmlNormalize.getElementsByTagName("prefix");
                    if (list.getLength() > 0) {
                        prefix = list.item(0).getFirstChild().getNodeValue();
                    }
                    list = xmlNormalize.getElementsByTagName("selectionNamespaces");
                    if (list.getLength() > 0) {
                        StringTokenizer st = new StringTokenizer(list.item(0).getFirstChild().getNodeValue());
                        while (st.hasMoreTokens()) {
                            String t = st.nextToken();
                            namespaceNode.setAttribute(t.substring(0, t.indexOf('=')), t.substring(t.indexOf('=') + 1));
                        }
                    } else {
                        namespaceNode.setAttribute("xmlns:oai_dc", "http://purl.org/dc/elements/1.1/");
                        namespaceNode.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
                        namespaceNode.setAttribute("xmlns:rfc1807", "http://info.internet.isi.edu:80/in-notes/rfc/files/rfc1807.txt");
                        namespaceNode.setAttribute("xmlns:oai_marc", "http://www.openarchives.org/OAI/1.1/oai_marc");
                    }
                    NodeList mapList = xmlNormalize.getElementsByTagName("mapping");
                    for (int i = 0; i < mapList.getLength(); i++) {
                        map = mapList.item(i).getAttributes();
                        if (map.getNamedItem("xpath") != null) {
                            xpath = map.getNamedItem("xpath").getNodeValue();
                            if (!xpath.startsWith(".")) {
                                xpath = "." + xpath;
                            }
                        } else {
                            xpath = "";
                        }
                        NodeList nlist = XPathAPI.selectNodeList(node, xpath, namespaceNode);
                        for (int j = 0; j < nlist.getLength(); j++) {
                            String txt = nlist.item(j).getFirstChild().getNodeValue();
                            NodeList eleList = mapList.item(i).getChildNodes();
                            bool = false;
                            for (int k = 0; k < eleList.getLength(); k++) {
                                if (!eleList.item(k).getNodeName().equals("newElement")) {
                                    continue;
                                }
                                map = eleList.item(k).getAttributes();
                                if (map.getNamedItem("regexp") != null) {
                                    regexp = map.getNamedItem("regexp").getNodeValue();
                                } else {
                                    regexp = "";
                                }
                                if (map.getNamedItem("ignoreCase") != null) {
                                    ic = map.getNamedItem("ignoreCase").getNodeValue().toLowerCase().equals("true");
                                } else {
                                    ic = false;
                                }
                                if (map.getNamedItem("name") != null) {
                                    nName = map.getNamedItem("name").getNodeValue();
                                } else {
                                    nName = nlist.item(j).getNodeName();
                                }
                                if (ic) {
                                    p = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);
                                } else {
                                    p = Pattern.compile(regexp, Pattern.MULTILINE);
                                }
                                Matcher m = p.matcher(txt);
                                bool = m.find();
                                if (bool) {
                                    NodeList valList = eleList.item(k).getChildNodes();
                                    for (int n = 0; n < valList.getLength(); n++) {
                                        if (!valList.item(n).getNodeName().equals("newValue")) {
                                            continue;
                                        }
                                        String valTxt = valList.item(n).getFirstChild().getNodeValue();
                                        boolean found = false;
                                        for (int q = 0; q < nlist.getLength(); q++) {
                                            if (nlist.item(q).getFirstChild() == null) {
                                                continue;
                                            }
                                            String txt2 = nlist.item(q).getFirstChild().getNodeValue();
                                            if (txt2.indexOf(valTxt) >= 0) {
                                                found = true;
                                                break;
                                            }
                                        }
                                        if (!found) {
                                            Node newNode = node.getOwnerDocument().createElementNS(nlist.item(j).getNamespaceURI(), nName);
                                            newNode.appendChild(node.getOwnerDocument().createTextNode(prefix + valTxt));
                                            nlist.item(j).getParentNode().appendChild(newNode);
                                        }
                                    }
                                    break;
                                }
                            }
                            if (bool) {
                                break;
                            }
                        }
                    }
                } catch (DOMException de) {
                    writeHistory(STATUS_FAILED, "Failed to create normalized node." + de);
                } catch (TransformerException te) {
                    writeHistory(STATUS_FAILED, "Failed to filter records.");
                }
            }
        }
    }

    /**
	 *
	 *
	 */
    private boolean prepareHarvestCommand() {
        boolean ret = true;
        hInfo.setHarvestStartTime(new java.util.Date());
        try {
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rst = stmt.executeQuery("Select * from OAIRepository where repositoryID=" + hInfo.getRepositoryID());
            if (rst.next()) {
                hInfo.setBaseURL(rst.getString("baseURL"));
                hInfo.setBaseDirectory(rst.getString("baseDIR"));
            } else {
                writeHistory(STATUS_FAILED, "Could not find repositoryID in database");
                ret = false;
            }
            rst.close();
            stmt.close();
        } catch (SQLException se) {
            ret = false;
        }
        return ret;
    }

    public long getTotal() {
        return total;
    }

    public HarvesterInfo getHarvesterInfo() {
        return hInfo;
    }

    public static void main(String args[]) {
        new OAIHarvesterTest();
    }

    private Connection conn;

    private long total;

    private long subtotal;

    private HarvesterInfo hInfo;

    private OAIRepository repo;

    private Document xmlFilter;

    private Document xmlNormalize;

    private Document xslNormalize;

    private int warningCount;

    private String subdir;

    private String basedir;

    private String strWarning;

    private boolean stopFlag;

    private boolean parseID;

    private int maxNumOfFilesPerDir;

    private boolean debug;

    private final int STATUS_SUCCESS = 0;

    private final int STATUS_FAILED = 1;

    private final int STATUS_WARNING = 2;

    private final int FLAG_RECORD_NEW = 0;

    private final int FLAG_RECORD_MODIFIED = 1;

    private final int FLAG_RECORD_OLD = 2;
}
