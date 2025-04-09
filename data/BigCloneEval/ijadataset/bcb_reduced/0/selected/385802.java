package vobs.dbaccess;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import javax.servlet.http.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import org.apache.commons.logging.*;
import org.exist.xmldb.XQueryService;
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
import org.jdom.output.Format;
import org.xml.sax.*;
import org.xmldb.api.base.*;
import org.xmldb.api.base.Collection;
import org.xmldb.api.modules.*;
import vobs.datamodel.*;
import wdc.settings.*;

public class VOAccess {

    private static CompiledExpression compiled = null;

    private static Log log = LogFactory.getLog(VOAccess.class);

    private static DefaultJDOMFactory factory = new DefaultJDOMFactory();

    private static XMLOutputter outXml = new XMLOutputter(Format.getPrettyFormat());

    private static DOMBuilder builder = new DOMBuilder();

    private static String forumDB = Settings.get("vo_meta.forumResource");

    public static String timeZone = Settings.get("vo_meta.timeZone");

    private static String logsDB = Settings.get("vo_meta.logsResource");

    private static String profilesDB = Settings.get("vo_meta.userProfilesResource");

    private static String settingsDB = Settings.get("vo_meta.settingsResource");

    private static String rootDB = Settings.get("vo_meta.rootCollection");

    private static String language = Settings.get("vo_meta.language");

    public static String parseDate(String myDate, String myInputDateFormat, String myOutputDateFormat) {
        String testValue = myDate;
        String output = "";
        SimpleDateFormat dfInput = new SimpleDateFormat(myInputDateFormat);
        SimpleDateFormat dfOutput = new SimpleDateFormat(myOutputDateFormat, Locale.US);
        try {
            Date date = dfInput.parse(testValue);
            output = dfOutput.format(date);
        } catch (java.text.ParseException e) {
            log.error(e.getMessage());
        }
        return output;
    }

    public static boolean documentExist(String docName) throws XMLDBException {
        String xquery = " xquery version \"1.0\"; " + " let $exist_doc := if (exists(document('" + forumDB + "/" + docName + ".xml'))) " + " 	then \"true\" " + "       else \"false\" " + " return <result><doc>{$exist_doc}</doc></result>";
        XMLResource checkResource = (XMLResource) (((XQueryService) CollectionsManager.getService(forumDB, true, "XQueryService")).query(xquery)).getResource(0);
        Element resultElm = builder.build((org.w3c.dom.Document) (checkResource.getContentAsDOM())).getRootElement();
        if (resultElm.getChildText("doc").equals("true")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean xmlFileExist(String docPath, String xmlName) throws XMLDBException {
        String xquery = " xquery version \"1.0\"; " + " let $exist_doc := if (exists(document('" + docPath + "/" + xmlName + "'))) " + " 	then \"true\" " + "       else \"false\" " + " return <result><doc>{$exist_doc}</doc></result>";
        XMLResource checkResource = (XMLResource) (((XQueryService) CollectionsManager.getService(docPath, true, "XQueryService")).query(xquery)).getResource(0);
        Element resultElm = builder.build((org.w3c.dom.Document) (checkResource.getContentAsDOM())).getRootElement();
        if (resultElm.getChildText("doc").equals("true")) {
            return true;
        } else {
            return false;
        }
    }

    public static Vector existRequest(String DBpath, String documentName, String xquery) {
        Vector output = new Vector();
        try {
            String query = "document('" + DBpath + "/" + documentName + "')" + xquery;
            XQueryService service = (XQueryService) CollectionsManager.getService(DBpath, true, "XQueryService");
            CompiledExpression compiled = service.compile(query);
            ResourceSet result = service.execute(compiled);
            for (int i = 0; i < result.getSize(); i++) {
                XMLResource resource = (XMLResource) result.getResource(i);
                String res = resource.getContent().toString();
                output.add(res);
            }
            result.clear();
            compiled.reset();
        } catch (Exception e) {
            log.error("Error making eXist request for DBpath: " + DBpath);
            e.printStackTrace();
        }
        return output;
    }

    public static String getRandomDocId() throws XMLDBException {
        String randomDocId = "";
        Integer objNum = null;
        java.util.Random rand = new java.util.Random();
        int max = 1000;
        for (int i = 0; i < 3; i++) {
            objNum = rand.nextInt(max + 1);
        }
        if (null != objNum) {
            String xquery = "xquery version \"1.0\";" + "  let $col := collection('" + forumDB + "')[" + objNum + "]," + "  $docName := item-at($col, 1)" + "  return replace(util:document-name($docName), \".xml\", \"\")";
            XQueryService service = (XQueryService) vobs.dbaccess.CollectionsManager.getService(rootDB, true, "XQueryService");
            ResourceSet result = service.query(xquery);
            if (result.getSize() > 0) {
                XMLResource resource = (XMLResource) result.getResource(0);
                randomDocId = resource.getContent().toString();
            }
        }
        return randomDocId;
    }

    public static void addUserProfile(String userDB, String tmpProfile, vobs.datamodel.User voUser) {
        try {
            Collection col = CollectionsManager.getCollection(userDB, true);
            String xquery = "document('" + userDB + "/" + tmpProfile + "')";
            XQueryService service = (XQueryService) col.getService("XQueryService", "1.0");
            service.setProperty(OutputKeys.INDENT, "yes");
            service.setProperty(OutputKeys.ENCODING, "UTF-8");
            compiled = service.compile(xquery);
            ResourceSet result = service.execute(compiled);
            XMLResource resource = (XMLResource) result.getResource(0);
            String xml = resource.getContent().toString();
            XMLResource document = (XMLResource) col.createResource(voUser.getProfileName(), "XMLResource");
            document.setContent(xml);
            col.storeResource(document);
            result.clear();
            int timeRollBack = 7;
            String timeRollQuery = "document('settings.xml')/VO_SETTINGS/FILTER_DEFAULT/DAYS_DELAY/text()";
            XQueryService timeRollService = (XQueryService) CollectionsManager.getCollection(Settings.get("vo_meta.settingsResource"), true).getService("XQueryService", "1.0");
            ResourceSet timeRollResult = timeRollService.query(timeRollQuery);
            if (timeRollResult.getSize() > 0) {
                XMLResource timeRollResource = (XMLResource) timeRollResult.getResource(0);
                try {
                    timeRollBack = Integer.parseInt(timeRollResource.getContent().toString());
                } catch (NumberFormatException ex) {
                    log.error("Error parsing time roll back value from settings. Response is: " + timeRollResource.getContent());
                }
            } else {
                log.error("Error parsing time roll back value from settings: returned no resluts.");
            }
            xquery = "<xupdate:modifications version='1.0' xmlns:xupdate='http://www.xmldb.org/xupdate'>" + "  <xupdate:update select=\"document('" + userDB + "/" + voUser.getProfileName() + "')//USER_ID\">" + voUser.getProfileName() + "</xupdate:update>" + "  <xupdate:update select=\"document('" + userDB + "/" + voUser.getProfileName() + "')//USER_NAME\">" + voUser.getName() + "</xupdate:update>" + "  <xupdate:update select=\"document('" + userDB + "/" + voUser.getProfileName() + "')//USER_MAIL\">" + voUser.getEmail() + "</xupdate:update>" + "  <xupdate:update select=\"document('" + userDB + "/" + voUser.getProfileName() + "')//USER_TIME_ACTIVITY\">" + getRegistrationTime(timeZone, timeRollBack) + "</xupdate:update>" + "  <xupdate:update select=\"document('" + userDB + "/" + voUser.getProfileName() + "')//USER_TIME_PC_ACTIVITY\">" + (System.currentTimeMillis() - timeRollBack * 1000 * 60 * 60 * 24) + "</xupdate:update>" + "</xupdate:modifications>";
            XUpdateQueryService updateService = (XUpdateQueryService) col.getService("XUpdateQueryService", "1.0");
            updateService.setProperty(OutputKeys.INDENT, "yes");
            updateService.setProperty(OutputKeys.ENCODING, "UTF-8");
            long res = updateService.update(xquery);
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    public static void addUserLog(String URI, String logsDB, String profileName) {
        try {
            Collection col = CollectionsManager.getCollection(logsDB, true);
            XMLResource document = (XMLResource) col.createResource(profileName + ".log", "XMLResource");
            document.setContent("<LOG><USER_ID>" + profileName + "</USER_ID><EVENT/></LOG>");
            col.storeResource(document);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("add user log error: " + e);
        }
    }

    public static void addUserData(String URI, String settingsDB, String destinationDB, String templateName, Vector keys, Vector values, String objId) {
        try {
            String xquery = "document('" + settingsDB + "/" + templateName + "')";
            ((CollectionManagementService) CollectionsManager.getService(URI + rootDB, false, "CollectionManager")).createCollection(rootDB + destinationDB);
            XQueryService service = (XQueryService) CollectionsManager.getService(settingsDB, true, "XQueryService");
            ResourceSet result = service.query(xquery);
            XMLResource resource = (XMLResource) result.getResource(0);
            String xml = resource.getContent().toString();
            Collection col = CollectionsManager.getCollection(rootDB + destinationDB, true);
            XMLResource document = (XMLResource) col.createResource(objId + ".xml", XMLResource.RESOURCE_TYPE);
            document.setContent(xml);
            col.storeResource(document);
            xquery = "<xupdate:modifications version='1.0' xmlns:xupdate='http://www.xmldb.org/xupdate'>";
            for (int i = 0; i < keys.size(); i++) {
                if (values.get(i) == null || ((String) ((Vector) values.get(i)).get(0)).length() == 0) {
                    xquery = xquery + "  <xupdate:remove select=\"document('" + rootDB + destinationDB + objId + ".xml" + "')//*[text()='" + keys.get(i).toString() + "']/text()\"/>";
                } else {
                    Vector valuesVect = (Vector) values.get(i);
                    for (int j = 1; j < valuesVect.size(); j++) {
                        xquery = xquery + "  <xupdate:insert-after select=\"document('" + rootDB + destinationDB + objId + ".xml" + "')//*[text()='" + keys.get(i).toString() + "']" + "\">" + "    <xupdate:element name=\"Value\">" + valuesVect.get(j).toString() + "</xupdate:element>" + "  </xupdate:insert-after>";
                    }
                    xquery = xquery + "  <xupdate:update select=\"document('" + rootDB + destinationDB + objId + ".xml')//*[text()='" + keys.get(i).toString() + "']\">" + valuesVect.get(0).toString() + "</xupdate:update>" + "<xupdate:update select=\"document('" + rootDB + destinationDB + objId + ".xml')//*/@*[.='" + keys.get(i).toString() + "']\">" + valuesVect.get(0).toString() + "</xupdate:update>";
                }
            }
            xquery = xquery + "</xupdate:modifications>";
            XUpdateQueryService updateService = (XUpdateQueryService) col.getService("XUpdateQueryService", "1.0");
            long res = updateService.update(xquery);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }

    public static void editUserData(String URI, String settingsDB, String destinationDB, String templateName, Vector keys, Vector values, String objId) {
        try {
            String xquery = "document('" + settingsDB + "/" + templateName + "')";
            ((CollectionManagementService) CollectionsManager.getService(URI + rootDB, false, "CollectionManager")).createCollection(rootDB + destinationDB);
            XQueryService service = (XQueryService) CollectionsManager.getService(settingsDB, true, "XQueryService");
            ResourceSet result = service.query(xquery);
            XMLResource resource = (XMLResource) result.getResource(0);
            String xml = resource.getContent().toString();
            Collection col = CollectionsManager.getCollection(rootDB + destinationDB, true);
            XMLResource document = (XMLResource) col.createResource(objId + ".xml", XMLResource.RESOURCE_TYPE);
            document.setContent(xml);
            col.storeResource(document);
            xquery = "<xupdate:modifications version='1.0' xmlns:xupdate='http://www.xmldb.org/xupdate'>";
            for (int i = 0; i < keys.size(); i++) {
                if (values.get(i) == null || ((String) values.get(i)).length() == 0) {
                    xquery = xquery + "  <xupdate:remove select=\"document('" + rootDB + destinationDB + objId + ".xml" + "')//*[text()='" + keys.get(i).toString() + "']/text()\"/>";
                } else xquery = xquery + "  <xupdate:update select=\"document('" + rootDB + destinationDB + objId + ".xml" + "')//*[text()='" + keys.get(i).toString() + "']" + "\">" + values.get(i).toString() + "</xupdate:update>";
            }
            xquery = xquery + "</xupdate:modifications>";
            XUpdateQueryService updateService = (XUpdateQueryService) col.getService("XUpdateQueryService", "1.0");
            long res = updateService.update(xquery);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }

    public static void moveUserData(String URI, String oldDestinationDB, String newDestinationDB, String objId) {
        try {
            String xquery = "document('" + rootDB + oldDestinationDB + "/" + objId + ".xml')";
            ((CollectionManagementService) CollectionsManager.getService(URI + rootDB, false, "CollectionManager")).createCollection(rootDB + newDestinationDB);
            XQueryService service = (XQueryService) CollectionsManager.getService(settingsDB, true, "XQueryService");
            ResourceSet result = service.query(xquery);
            XMLResource resource = (XMLResource) result.getResource(0);
            String xml = resource.getContent().toString();
            Collection col = CollectionsManager.getCollection(rootDB + newDestinationDB, true);
            XMLResource document = (XMLResource) col.createResource(objId + ".xml", XMLResource.RESOURCE_TYPE);
            document.setContent(xml);
            col.storeResource(document);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }

    public static String getUniqueId() {
        RandomGUID dataID = new RandomGUID();
        String objId = dataID.toString();
        return objId;
    }

    public static String getCurrentTime(String timeZone) {
        GregorianCalendar clndr = new GregorianCalendar(TimeZone.getTimeZone(timeZone));
        return VO.defaultTimeFormat.format(clndr.getTime()).toUpperCase();
    }

    public static String getRegistrationTime(String timeZone, int daysDelay) {
        GregorianCalendar clndr = new GregorianCalendar(TimeZone.getTimeZone(timeZone));
        clndr.add(GregorianCalendar.DAY_OF_MONTH, 0 - daysDelay);
        return VO.defaultTimeFormat.format(clndr.getTime()).toUpperCase();
    }

    public static void updateLog(String logsDB, String eTime, long timeInt, String objId, String section, String eNode, String title, String descr, String onlink, String profileName) {
        try {
            String overhead = "    <xupdate:element name='ObservatoryOverheadInformation'>" + "    <xupdate:element name='Title'>" + title + "</xupdate:element>" + "    <xupdate:element name='Description'>" + descr + "</xupdate:element>" + "    <xupdate:element name='Onlink'>" + onlink + "</xupdate:element>" + "    <xupdate:element name='PubDate'>" + eTime + "</xupdate:element>" + "    <xupdate:element name='Object'>" + objId + "</xupdate:element>" + "    <xupdate:element name='Creator'>" + profileName + "</xupdate:element>" + "</xupdate:element>";
            String xquery = "<xupdate:modifications version='1.0' xmlns:xupdate='http://www.xmldb.org/xupdate'>" + "  <xupdate:insert-before select=\"document('" + logsDB + "/vo_log.xml')//LOG/EVENT[1]\">" + "    <xupdate:element name='EVENT'>" + "    <xupdate:element name='EVENT_TIME'>" + eTime + "</xupdate:element>" + "    <xupdate:element name='EVENT_PC_TIME'>" + timeInt + "</xupdate:element>" + "    <xupdate:element name='OBJECT_ID'>" + objId + "</xupdate:element>" + "    <xupdate:element name='SECTION'>" + section + "</xupdate:element>" + "    <xupdate:element name='EVENT_NODE'>" + section + "::" + eNode + "</xupdate:element>" + overhead + "</xupdate:element>" + "  </xupdate:insert-before>" + "</xupdate:modifications>";
            XUpdateQueryService service = (XUpdateQueryService) CollectionsManager.getService(logsDB, true, "XUpdateQueryService");
            long res = service.update(xquery);
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    public static void updateLog(String logsDB, String eTime, long timeInt, String objId, String section, String eNode, String title, String descr, String onlink, String previewimg, String profileName) {
        try {
            String overhead = "<xupdate:element name='ObservatoryOverheadInformation'>" + "    <xupdate:element name='Title'>" + title + "</xupdate:element>" + "    <xupdate:element name='Description'>" + descr + "</xupdate:element>" + "    <xupdate:element name='Onlink'>" + onlink + "</xupdate:element>" + "    <xupdate:element name='PubDate'>" + eTime + "</xupdate:element>" + "    <xupdate:element name='Object'>" + objId + "</xupdate:element>" + "    <xupdate:element name='Creator'>" + profileName + "</xupdate:element>" + "    <xupdate:element name='PreviewImg'>" + previewimg + "</xupdate:element>" + "</xupdate:element>";
            String xquery = "<xupdate:modifications version='1.0' xmlns:xupdate='http://www.xmldb.org/xupdate'>" + "  <xupdate:insert-before select=\"document('" + logsDB + "/vo_log.xml')//LOG/EVENT[1]\">" + "    <xupdate:element name='EVENT'>" + "    <xupdate:element name='EVENT_TIME'>" + eTime + "</xupdate:element>" + "    <xupdate:element name='EVENT_PC_TIME'>" + timeInt + "</xupdate:element>" + "    <xupdate:element name='OBJECT_ID'>" + objId + "</xupdate:element>" + "    <xupdate:element name='SECTION'>" + section + "</xupdate:element>" + "    <xupdate:element name='EVENT_NODE'>" + section + "::" + eNode + "</xupdate:element>" + overhead + "</xupdate:element>" + "  </xupdate:insert-before>" + "</xupdate:modifications>";
            XUpdateQueryService service = (XUpdateQueryService) CollectionsManager.getService(logsDB, true, "XUpdateQueryService");
            long res = service.update(xquery);
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    /**
   * updateUserAddedDataProfile
   *
   * @param string String
   * @param objId String
   * @param sectionName String
   */
    public static void updateUserAddedDataProfile(String userId, String objId, String sectionName) {
        Element partElm = factory.element("PART");
        Element sectionElm = factory.element("SECTION");
        sectionElm.setText(sectionName);
        partElm.addContent(sectionElm);
        Element dataElm = factory.element("DATASOURCE");
        dataElm.setText(objId);
        partElm.addContent(dataElm);
        String xquery = "<xupdate:modifications version='1.0' xmlns:xupdate='http://www.xmldb.org/xupdate'>";
        try {
            String query = "  <xupdate:append select=\"document('" + profilesDB + "/" + userId + "')/USER_PROFILE/USER_DATA\" child=\"1\">" + "  <xupdate:element name=\"PART\"> " + outXml.outputString(partElm.getChildren()) + "  </xupdate:element>  " + "</xupdate:append>";
            xquery = xquery + query;
            xquery = xquery + "</xupdate:modifications>";
            XUpdateQueryService service = (XUpdateQueryService) CollectionsManager.getService(profilesDB, true, "XUpdateQueryService");
            long res = service.update(xquery);
        } catch (Exception e) {
            log.debug("Error updating user added data section: " + e);
            e.printStackTrace();
        }
    }

    public static void setUserActivityTime(String URI, String userDB, String profileName, String time) {
        try {
            Collection col = CollectionsManager.getCollection(userDB, true);
            String xquery = "<xupdate:modifications version='1.0' xmlns:xupdate='http://www.xmldb.org/xupdate'>" + "  <xupdate:update select=\"document('" + userDB + "/" + profileName + "')/USER_PROFILE/USER_TIME_ACTIVITY\">" + time + "</xupdate:update>" + "  <xupdate:update select=\"document('" + userDB + "/" + profileName + "')/USER_PROFILE/USER_TIME_PC_ACTIVITY\">" + System.currentTimeMillis() + "</xupdate:update>" + "</xupdate:modifications>";
            XUpdateQueryService updateService = (XUpdateQueryService) col.getService("XUpdateQueryService", "1.0");
            long res = updateService.update(xquery);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error setting user activity time: " + e);
        }
    }

    public static String getUserLastActivityTime(String URI, String userDB, String profileName) {
        String output = "Unknown";
        if (profileName != null) {
            Vector userLastUpdateTime = existRequest(userDB, profileName, "/USER_PROFILE/USER_TIME_ACTIVITY/text()");
            output = userLastUpdateTime.get(0).toString();
        }
        return output;
    }

    public static Vector getUserMenuItems(String profileName, Element refreshElm) throws XMLDBException {
        Vector userMenuItems = new Vector();
        String query = "document('vo_description.xml')/OBSERVATORY/DATA_SECTIONS/SECTION";
        XQueryService service = (XQueryService) CollectionsManager.getService(settingsDB, true, "XQueryService");
        ResourceSet result = service.query(query);
        for (int i = 0; i < result.getSize(); i++) {
            XMLResource resource = (XMLResource) result.getResource(i);
            Element readSectionElm = builder.build((org.w3c.dom.Document) (resource.getContentAsDOM())).getRootElement();
            String sectionName = readSectionElm.getChild("NAME").getText();
            String sectionLongName = readSectionElm.getChild("LONG_NAME").getText();
            int tempInt = countLogRecords(readSectionElm.getChild("NAME").getText(), profileName);
            String count_new = (tempInt == -1) ? "-" : Integer.toString(tempInt);
            tempInt = countUserLogRecords(readSectionElm.getChild("NAME").getText(), profileName);
            String count_user = (tempInt == -1) ? "-" : Integer.toString(tempInt);
            String count_total = readSectionElm.getChild("TOTAL_FILES").getText();
            UserMenuItems myMenuItems = new UserMenuItems();
            myMenuItems.setCategoryId(sectionName);
            myMenuItems.setCategoryName(sectionLongName);
            myMenuItems.setTotalCategoryItems(count_total);
            myMenuItems.setNewCategoryItems(count_new);
            myMenuItems.setUserCategoryItems(count_user);
            userMenuItems.add(myMenuItems);
            if (null != refreshElm) {
                Element nameElement = factory.element("NAME");
                nameElement.setText(sectionName);
                Element longNameElement = factory.element("LONG_NAME");
                longNameElement.setText(sectionLongName);
                Element newFilesElement = factory.element("COUNT_NEW");
                newFilesElement.setText(count_new);
                Element totalFilesElement = factory.element("COUNT_ALL");
                totalFilesElement.setText(count_total);
                Element traceFilesElement = factory.element("COUNT_TRACE");
                traceFilesElement.setText(count_user);
                Element userSection = factory.element("SECTION");
                userSection.setContent(nameElement);
                userSection.addContent(longNameElement);
                userSection.addContent(totalFilesElement);
                userSection.addContent(newFilesElement);
                userSection.addContent(traceFilesElement);
                refreshElm.addContent(userSection);
            }
        }
        return userMenuItems;
    }

    public static String getElementByName(String element, String name) {
        String output = "";
        try {
            String xquery = "document('" + settingsDB + "/vo_description.xml')/OBSERVATORY/DATA_SECTIONS/SECTION/" + element + "[ ../NAME = '" + name + "']/text()";
            XQueryService service = (XQueryService) CollectionsManager.getService(settingsDB, true, "XQueryService");
            ResourceSet result = service.query(xquery);
            for (int i = 0; i < result.getSize(); i++) {
                XMLResource resource = (XMLResource) result.getResource(i);
                String res = resource.getContent().toString();
                output = res;
            }
        } catch (Exception e) {
            log.error("Error getting element by name: " + e);
            e.printStackTrace();
        }
        return output;
    }

    public static String getElementByName(String settingsDB, String element, String name) {
        String output = "";
        try {
            String xquery = "document('" + settingsDB + "/vo_description.xml')/OBSERVATORY/DATA_SECTIONS/SECTION/" + element + "[ ../NAME = '" + name + "']/text()";
            XQueryService service = (XQueryService) CollectionsManager.getService(settingsDB, true, "XQueryService");
            ResourceSet result = service.query(xquery);
            for (int i = 0; i < result.getSize(); i++) {
                XMLResource resource = (XMLResource) result.getResource(i);
                String res = resource.getContent().toString();
                output = res;
            }
        } catch (Exception e) {
            log.error("Error getting element by name: " + e);
            e.printStackTrace();
        }
        return output;
    }

    public static String getElementValue(String settingsDB, String xmlName, String elementQuery) {
        String output = "";
        try {
            String xquery = "document('" + settingsDB + "/" + xmlName + "')" + elementQuery + "/text()";
            XQueryService service = (XQueryService) CollectionsManager.getService(settingsDB, true, "XQueryService");
            ResourceSet result = service.query(xquery);
            for (int i = 0; i < result.getSize(); i++) {
                XMLResource resource = (XMLResource) result.getResource(i);
                String res = resource.getContent().toString();
                output = res;
            }
        } catch (Exception e) {
            log.error("Error getting element by name: " + e);
            e.printStackTrace();
        }
        return output;
    }

    public static String[] findUserByEMail(String eMail) {
        org.w3c.dom.Element resultDocument = null;
        try {
            String xquery = "<result> { for $i in /* " + "where $i//USER_PROFILE/USER_MAIL/text() = '" + eMail + "' " + "return " + "<user>" + "  <id>{$i//USER_PROFILE/USER_ID/text()}</id>" + "  <name>{$i//USER_PROFILE/USER_NAME/text()}</name>" + "</user> } </result>";
            XQueryService service = (XQueryService) CollectionsManager.getService(profilesDB, true, "XQueryService");
            ResourceSet result = service.query(xquery);
            if (result.getSize() > 0) {
                XMLResource resource = (XMLResource) result.getResource(0);
                resultDocument = ((org.w3c.dom.Document) resource.getContentAsDOM()).getDocumentElement();
            }
        } catch (Exception e) {
            log.error("Error getting user by e-mail: " + e);
            e.printStackTrace();
        }
        String userId = null;
        String userName = null;
        if (resultDocument != null) {
            org.w3c.dom.NodeList resources = resultDocument.getElementsByTagName("user");
            for (int i = 0; i < resources.getLength(); i++) {
                org.w3c.dom.Element res = (org.w3c.dom.Element) resources.item(i);
                boolean flag1 = false;
                boolean flag2 = false;
                if (res.getElementsByTagName("id").getLength() > 0 && res.getElementsByTagName("id").item(0).getFirstChild() != null) {
                    userId = res.getElementsByTagName("id").item(0).getFirstChild().getNodeValue();
                    flag1 = true;
                } else {
                    flag1 = false;
                }
                if (res.getElementsByTagName("name").getLength() > 0 && res.getElementsByTagName("name").item(0).getFirstChild() != null) {
                    userName = res.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
                    flag2 = true;
                } else {
                    flag2 = false;
                }
                if (flag1 && flag2) {
                    break;
                }
            }
        }
        return new String[] { userId, userName };
    }

    public static String getElementValueByKnownName(String settingsDB, String xmlName, String elementQuery, String elementKnown, String knownValue) {
        String output = "";
        try {
            String xquery = "document('" + settingsDB + "/" + xmlName + "')" + elementQuery + "[../" + elementKnown + "='" + knownValue + "']/text()";
            XQueryService service = (XQueryService) CollectionsManager.getService(settingsDB, true, "XQueryService");
            ResourceSet result = service.query(xquery);
            for (int i = 0; i < result.getSize(); i++) {
                XMLResource resource = (XMLResource) result.getResource(i);
                String res = resource.getContent().toString();
                output = res;
            }
        } catch (Exception e) {
            log.error("Error getting element by name: " + e);
            e.printStackTrace();
        }
        return output;
    }

    public static String getTranslation(String inTheEnglish) {
        String output = inTheEnglish;
        if (!language.equalsIgnoreCase("SYSTEM")) {
            try {
                String xquery = "document('" + settingsDB + "/vo_language.xml')/LANGUAGE/GLOSS/" + language + "[../SYSTEM='" + inTheEnglish + "']/text()";
                XQueryService service = (XQueryService) CollectionsManager.getService(settingsDB, true, "XQueryService");
                ResourceSet result = service.query(xquery);
                for (int i = 0; i < result.getSize(); i++) {
                    XMLResource resource = (XMLResource) result.getResource(i);
                    String res = resource.getContent().toString();
                    if (res.length() != 0) {
                        output = res;
                    }
                }
            } catch (Exception e) {
                log.error("Error translation: " + e);
                e.printStackTrace();
            }
        }
        return output;
    }

    public static String getTranslation(String userId, String systemGloss) {
        String output = systemGloss;
        String userLanguage = language;
        if (null != userId) userLanguage = getElementValue(profilesDB, userId, "/USER_PROFILE/USER_DESIGN/LANGUAGE");
        if (null == userLanguage || userLanguage.length() == 0) userLanguage = Settings.get("vo_meta.language");
        if (null != userLanguage && userLanguage.length() > 0 && !userLanguage.equalsIgnoreCase("SYSTEM")) {
            try {
                String xquery = "document('" + settingsDB + "/vo_language.xml')/LANGUAGE/GLOSS/" + userLanguage + "[../SYSTEM='" + systemGloss + "']/text()";
                XQueryService service = (XQueryService) CollectionsManager.getService(settingsDB, true, "XQueryService");
                ResourceSet result = service.query(xquery);
                for (int i = 0; i < result.getSize(); i++) {
                    XMLResource resource = (XMLResource) result.getResource(i);
                    String res = resource.getContent().toString();
                    if (res.length() != 0) {
                        output = res;
                    }
                }
            } catch (Exception e) {
                log.error("Error translation: " + e);
                e.printStackTrace();
            }
        }
        return output;
    }

    public static int getBasketItemsNumber(String profileName) throws XMLDBException {
        int n = 0;
        String activityQuery = "count(document('" + profileName + "')/USER_PROFILE/USER_BASKET/OBJECT_ID)";
        XQueryService queryService = (XQueryService) CollectionsManager.getCollection(Settings.get("vo_meta.userProfilesResource"), true).getService("XQueryService", "1.0");
        ResourceSet queryResult = queryService.query(activityQuery);
        if (queryResult.getSize() > 0) {
            XMLResource queryResource = (XMLResource) queryResult.getResource(0);
            try {
                n = Integer.parseInt(queryResource.getContent().toString());
            } catch (NumberFormatException ex) {
                log.error("Error parsing number of basket elements. Response is: " + queryResource.getContent());
            }
        } else {
            log.error("Error parsing number of basket elements: returned no results.");
        }
        return n;
    }

    public static String getSectionDataXsl(String section) {
        String xsl = null;
        try {
            String xquery = "document('vo_description.xml')/OBSERVATORY/DATA_SECTIONS/SECTION[NAME='" + section + "']/DATAXSL/text()";
            XQueryService service = (XQueryService) CollectionsManager.getService(settingsDB, true, "XQueryService");
            ResourceSet result = service.query(xquery);
            if (result.getSize() > 0) {
                XMLResource resource = (XMLResource) result.getResource(0);
                xsl = resource.getContent().toString();
            }
        } catch (Exception e) {
            log.error("Error getting section data xsl: " + e);
            e.printStackTrace();
        }
        return xsl;
    }

    public static int getImailsCount(String profileName, boolean unread) throws XMLDBException {
        int n = 0;
        String activityQuery = "count(document('" + profileName + ".xml')/IMAIL/RECIEVED/MESSAGE" + (unread ? "[STATUS/text()='Unread']" : "") + ")";
        XQueryService queryService = (XQueryService) CollectionsManager.getCollection(Settings.get("vo_meta.imailResource"), true).getService("XQueryService", "1.0");
        ResourceSet queryResult = queryService.query(activityQuery);
        if (queryResult.getSize() > 0) {
            XMLResource queryResource = (XMLResource) queryResult.getResource(0);
            try {
                n = Integer.parseInt(queryResource.getContent().toString());
            } catch (NumberFormatException ex) {
                log.error("Error parsing number of unread emails. Response is: " + queryResource.getContent());
            }
        } else {
            log.error("Error parsing number of unread emails: returned no results.");
        }
        return n;
    }

    /**
   * createDiscussionFile
   *
   * @param discussionFields Hashtable
   */
    public static void createDiscussionFile(Hashtable discussionFields) {
        try {
            Element discussionElm = factory.element("DISCUSSION");
            Element objIdElm = factory.element("OBJECT_ID");
            objIdElm.setText((String) discussionFields.get("OBJECT_ID"));
            discussionElm.addContent(objIdElm);
            Element authorNameElm = factory.element("AUTHOR_NAME");
            authorNameElm.setText((String) discussionFields.get("AUTHOR_NAME"));
            discussionElm.addContent(authorNameElm);
            Element authorIdElm = factory.element("AUTHOR_ID");
            authorIdElm.setText((String) discussionFields.get("AUTHOR_ID"));
            discussionElm.addContent(authorIdElm);
            Element coAuthorIdElm = factory.element("CO_AUTHORS");
            discussionElm.addContent(coAuthorIdElm);
            Element notifyElm = factory.element("NOTIFY_AUTHOR");
            notifyElm.setText("true");
            discussionElm.addContent(notifyElm);
            Element sectionElm = factory.element("OBJECT_SECTION");
            sectionElm.setText((String) discussionFields.get("OBJECT_SECTION"));
            discussionElm.addContent(sectionElm);
            Element pathElm = factory.element("OBJECT_PATH");
            pathElm.setText((String) discussionFields.get("OBJECT_PATH"));
            discussionElm.addContent(pathElm);
            Element fileElm = factory.element("FILE_PATH");
            fileElm.setText((String) discussionFields.get("FILE_PATH"));
            discussionElm.addContent(fileElm);
            Element viewCountElm = factory.element("VIEW_COUNTER");
            viewCountElm.setText("0");
            discussionElm.addContent(viewCountElm);
            Element basketCountElm = factory.element("BASKET_COUNTER");
            basketCountElm.setText("0");
            discussionElm.addContent(basketCountElm);
            Element basketUsersElm = factory.element("BASKET_USERS_ID");
            discussionElm.addContent(basketUsersElm);
            Element titleElm = factory.element("TITLE");
            titleElm.setText((String) discussionFields.get("TITLE"));
            discussionElm.addContent(titleElm);
            Element descrElm = factory.element("DESCRIPTION");
            descrElm.setText((String) discussionFields.get("DESCRIPTION"));
            discussionElm.addContent(descrElm);
            Element onlinkElm = factory.element("ONLINK");
            onlinkElm.setText((String) discussionFields.get("ONLINK"));
            discussionElm.addContent(onlinkElm);
            Element nCoverElm = factory.element("NCOVER");
            nCoverElm.setText((String) discussionFields.get("NCOVER"));
            discussionElm.addContent(nCoverElm);
            Element eCoverElm = factory.element("ECOVER");
            eCoverElm.setText((String) discussionFields.get("ECOVER"));
            discussionElm.addContent(eCoverElm);
            Element sCoverElm = factory.element("SCOVER");
            sCoverElm.setText((String) discussionFields.get("SCOVER"));
            discussionElm.addContent(sCoverElm);
            Element wCoverElm = factory.element("WCOVER");
            wCoverElm.setText((String) discussionFields.get("WCOVER"));
            discussionElm.addContent(wCoverElm);
            Element perStartElm = factory.element("PERIOD_START");
            perStartElm.setText((String) discussionFields.get("PERIOD_START"));
            discussionElm.addContent(perStartElm);
            Element perEndElm = factory.element("PERIOD_END");
            perEndElm.setText((String) discussionFields.get("PERIOD_END"));
            discussionElm.addContent(perEndElm);
            Element previewImgElm = factory.element("PREVIEW_IMG");
            previewImgElm.setText((String) discussionFields.get("PREVIEW_IMG"));
            discussionElm.addContent(previewImgElm);
            Element docIndexElm = factory.element("DOC_INDEX");
            docIndexElm.setText((String) discussionFields.get("DOC_INDEX"));
            discussionElm.addContent(docIndexElm);
            Element pubDateElm = factory.element("PUBDATE");
            pubDateElm.setText(VOAccess.getCurrentTime(timeZone));
            discussionElm.addContent(pubDateElm);
            Element pubPcDateElm = factory.element("PUB_PC_DATE");
            pubPcDateElm.setText(Long.toString(System.currentTimeMillis()));
            discussionElm.addContent(pubPcDateElm);
            Element repliesElm = factory.element("REPLIES");
            repliesElm.setAttribute("discussRestriction", (String) discussionFields.get("DISCUSSRESTRICTION"));
            discussionElm.addContent(repliesElm);
            Element votingElm = factory.element("VOTING");
            votingElm.setAttribute("averaged", "0");
            votingElm.setAttribute("votingRestriction", (String) discussionFields.get("VOTINGRESTRICTION"));
            discussionElm.addContent(votingElm);
            Element editorsElm = factory.element("EDITORS");
            editorsElm.setText("");
            discussionElm.addContent(editorsElm);
            Element tagsElm = factory.element("TAGS");
            tagsElm.setText("");
            discussionElm.addContent(tagsElm);
            Element relatedObjElm = factory.element("RELATED_OBJECTS");
            relatedObjElm.setText("");
            discussionElm.addContent(relatedObjElm);
            Collection col = CollectionsManager.getCollection(forumDB, true);
            XMLResource document = (XMLResource) col.createResource(discussionFields.get("OBJECT_ID") + ".xml", XMLResource.RESOURCE_TYPE);
            document.setContent(outXml.outputString(discussionElm));
            col.storeResource(document);
        } catch (Exception e) {
            log.error("Error creating new discussions file: " + e);
            e.printStackTrace();
        }
    }

    public static void updateDiscussionFile(Hashtable discussionFields, String editorId, String docVersion) {
        String objIdElm = (String) discussionFields.get("OBJECT_ID");
        objIdElm = objIdElm.replaceAll("&", "&amp;");
        objIdElm = objIdElm.replaceAll("<", "&lt;");
        objIdElm = objIdElm.replaceAll(">", "&gt;");
        String titleElm = (String) discussionFields.get("TITLE");
        titleElm = titleElm.replaceAll("&", "&amp;");
        titleElm = titleElm.replaceAll("<", "&lt;");
        titleElm = titleElm.replaceAll(">", "&gt;");
        String descrElm = (String) discussionFields.get("DESCRIPTION");
        descrElm = descrElm.replaceAll("&", "&amp;");
        descrElm = descrElm.replaceAll("<", "&lt;");
        descrElm = descrElm.replaceAll(">", "&gt;");
        String onlinkElm = (String) discussionFields.get("ONLINK");
        onlinkElm = onlinkElm.replaceAll("&", "&amp;");
        onlinkElm = onlinkElm.replaceAll("<", "&lt;");
        onlinkElm = onlinkElm.replaceAll(">", "&gt;");
        String nCoverElm = (String) discussionFields.get("NCOVER");
        nCoverElm = nCoverElm.replaceAll("&", "&amp;");
        nCoverElm = nCoverElm.replaceAll("<", "&lt;");
        nCoverElm = nCoverElm.replaceAll(">", "&gt;");
        String eCoverElm = (String) discussionFields.get("ECOVER");
        eCoverElm = eCoverElm.replaceAll("&", "&amp;");
        eCoverElm = eCoverElm.replaceAll("<", "&lt;");
        eCoverElm = eCoverElm.replaceAll(">", "&gt;");
        String sCoverElm = (String) discussionFields.get("SCOVER");
        sCoverElm = sCoverElm.replaceAll("&", "&amp;");
        sCoverElm = sCoverElm.replaceAll("<", "&lt;");
        sCoverElm = sCoverElm.replaceAll(">", "&gt;");
        String wCoverElm = (String) discussionFields.get("WCOVER");
        wCoverElm = wCoverElm.replaceAll("&", "&amp;");
        wCoverElm = wCoverElm.replaceAll("<", "&lt;");
        wCoverElm = wCoverElm.replaceAll(">", "&gt;");
        String perStartElm = (String) discussionFields.get("PERIOD_START");
        perStartElm = perStartElm.replaceAll("&", "&amp;");
        perStartElm = perStartElm.replaceAll("<", "&lt;");
        perStartElm = perStartElm.replaceAll(">", "&gt;");
        String perEndElm = (String) discussionFields.get("PERIOD_END");
        perEndElm = perEndElm.replaceAll("&", "&amp;");
        perEndElm = perEndElm.replaceAll("<", "&lt;");
        perEndElm = perEndElm.replaceAll(">", "&gt;");
        String previewImgElm = (String) discussionFields.get("PREVIEW_IMG");
        previewImgElm = previewImgElm.replaceAll("&", "&amp;");
        previewImgElm = previewImgElm.replaceAll("<", "&lt;");
        previewImgElm = previewImgElm.replaceAll(">", "&gt;");
        String docIndexElm = (String) discussionFields.get("DOC_INDEX");
        docIndexElm = docIndexElm.replaceAll("&", "&amp;");
        docIndexElm = docIndexElm.replaceAll("<", "&lt;");
        docIndexElm = docIndexElm.replaceAll(">", "&gt;");
        String xquery = "<xupdate:modifications version='1.0' xmlns:xupdate='http://www.xmldb.org/xupdate'>";
        xquery = xquery + "<xupdate:update select=\"document('" + forumDB + "/" + objIdElm + ".xml')/DISCUSSION/TITLE\">" + titleElm + "</xupdate:update>";
        xquery = xquery + "<xupdate:update select=\"document('" + forumDB + "/" + objIdElm + ".xml')/DISCUSSION/DESCRIPTION\">" + descrElm + "</xupdate:update>";
        xquery = xquery + "<xupdate:update select=\"document('" + forumDB + "/" + objIdElm + ".xml')/DISCUSSION/ONLINK\">" + onlinkElm + "</xupdate:update>";
        xquery = xquery + "<xupdate:update select=\"document('" + forumDB + "/" + objIdElm + ".xml')/DISCUSSION/NCOVER\">" + nCoverElm + "</xupdate:update>";
        xquery = xquery + "<xupdate:update select=\"document('" + forumDB + "/" + objIdElm + ".xml')/DISCUSSION/ECOVER\">" + eCoverElm + "</xupdate:update>";
        xquery = xquery + "<xupdate:update select=\"document('" + forumDB + "/" + objIdElm + ".xml')/DISCUSSION/SCOVER\">" + sCoverElm + "</xupdate:update>";
        xquery = xquery + "<xupdate:update select=\"document('" + forumDB + "/" + objIdElm + ".xml')/DISCUSSION/WCOVER\">" + wCoverElm + "</xupdate:update>";
        xquery = xquery + "<xupdate:update select=\"document('" + forumDB + "/" + objIdElm + ".xml')/DISCUSSION/PERIOD_START\">" + perStartElm + "</xupdate:update>";
        xquery = xquery + "<xupdate:update select=\"document('" + forumDB + "/" + objIdElm + ".xml')/DISCUSSION/PERIOD_END\">" + perEndElm + "</xupdate:update>";
        xquery = xquery + "<xupdate:update select=\"document('" + forumDB + "/" + objIdElm + ".xml')/DISCUSSION/PREVIEW_IMG\">" + previewImgElm + "</xupdate:update>";
        xquery = xquery + "<xupdate:update select=\"document('" + forumDB + "/" + objIdElm + ".xml')/DISCUSSION/DOC_INDEX\">" + docIndexElm + "</xupdate:update>";
        xquery = xquery + "</xupdate:modifications>";
        try {
            XUpdateQueryService service = (XUpdateQueryService) CollectionsManager.getService(forumDB, true, "XUpdateQueryService");
            long res = service.update(xquery);
        } catch (Exception e) {
            System.out.println(e);
        }
        Element editorElm = factory.element("EDITOR");
        Element idElm = factory.element("ID");
        idElm.setText(editorId);
        editorElm.addContent(idElm);
        Element nameElm = factory.element("NAME");
        nameElm.setText(getUserNameById(editorId));
        editorElm.addContent(nameElm);
        Element dateElm = factory.element("DATE");
        dateElm.setText(vobs.dbaccess.VOAccess.getCurrentTime(timeZone));
        editorElm.addContent(dateElm);
        Element versionElm = factory.element("VERSION");
        versionElm.setText(docVersion);
        editorElm.addContent(versionElm);
        xquery = "<xupdate:modifications version='1.0' xmlns:xupdate='http://www.xmldb.org/xupdate'>";
        try {
            String query = "  <xupdate:append select=\"document('" + forumDB + "/" + objIdElm + ".xml')/DISCUSSION/EDITORS\" child=\"1\">" + "  <xupdate:element name=\"EDITOR\"> " + outXml.outputString(editorElm.getChildren()) + "  </xupdate:element>  " + "</xupdate:append>";
            xquery = xquery + query;
            xquery = xquery + "</xupdate:modifications>";
            XUpdateQueryService service = (XUpdateQueryService) CollectionsManager.getService(forumDB, true, "XUpdateQueryService");
            long res = service.update(xquery);
        } catch (Exception e) {
            log.debug("Error updating editors information: " + e);
            e.printStackTrace();
        }
        return;
    }

    public static void updateXMLElement(String docPath, String objId, String elemPath, String elemValue) {
        String xquery = "<xupdate:modifications version='1.0' xmlns:xupdate='http://www.xmldb.org/xupdate'>";
        xquery = xquery + "<xupdate:update select=\"document('" + docPath + "/" + objId + ".xml')" + elemPath + "\">" + elemValue + "</xupdate:update>";
        xquery = xquery + "</xupdate:modifications>";
        try {
            XUpdateQueryService service = (XUpdateQueryService) CollectionsManager.getService(profilesDB, true, "XUpdateQueryService");
            long res = service.update(xquery);
        } catch (Exception e) {
            System.out.println(e);
        }
        return;
    }

    public static void updateUserLog(String objId, String section, String eNode, String title, String descr, String profileName, String time, long pcTime, String creator) {
        try {
            String overhead = "    <xupdate:element name='ObservatoryOverheadInformation'>" + "    <xupdate:element name='Title'>" + title + "</xupdate:element>" + "    <xupdate:element name='Description'>" + descr + "</xupdate:element>" + "    <xupdate:element name='PubDate'>" + time + "</xupdate:element>" + "    <xupdate:element name='Object'>" + objId + "</xupdate:element>" + "    <xupdate:element name='Creator'>" + profileName + "</xupdate:element>" + "</xupdate:element>";
            String xquery = "<xupdate:modifications version='1.0' xmlns:xupdate='http://www.xmldb.org/xupdate'>" + "  <xupdate:insert-before select=\"document('" + logsDB + "/" + creator + ".log')/LOG/EVENT[1]\">" + "    <xupdate:element name='EVENT'>" + "    <xupdate:element name='EVENT_TIME'>" + time + "</xupdate:element>" + "    <xupdate:element name='EVENT_PC_TIME'>" + pcTime + "</xupdate:element>" + "    <xupdate:element name='OBJECT_ID'>" + objId + "</xupdate:element>" + "    <xupdate:element name='SECTION'>" + section + "</xupdate:element>" + "    <xupdate:element name='EVENT_NODE'>" + section + "::" + eNode + "</xupdate:element>" + overhead + "</xupdate:element>" + "  </xupdate:insert-before>" + "</xupdate:modifications>";
            XUpdateQueryService service = (XUpdateQueryService) CollectionsManager.getService(logsDB, true, "XUpdateQueryService");
            long res = service.update(xquery);
        } catch (Exception e) {
            log.debug("Error adding node to user log: " + e);
            e.printStackTrace();
        }
    }

    public static void updateUserAdinfo(String profileName, String aditionalInfo) throws XMLDBException {
        String updateQuery = "<xupdate:modifications version='1.0' xmlns:xupdate='http://www.xmldb.org/xupdate'>" + "  <xupdate:update select=\"document('" + profilesDB + "/" + profileName + "')//USER_ADITIONAL_INFO\">" + aditionalInfo + "</xupdate:update>" + "</xupdate:modifications>";
        XUpdateQueryService updateService = (XUpdateQueryService) CollectionsManager.getService(profilesDB, true, "XUpdateQueryService");
        long res = updateService.update(updateQuery);
    }

    public static void updateUserInterests(String profileName, String[] interests) throws XMLDBException {
        String interestsElements = "";
        for (int i = 0; i < interests.length; i++) {
            interestsElements = interestsElements + "<INTEREST>" + interests[i] + "</INTEREST>";
        }
        ;
        String updateQuery = "<xupdate:modifications version='1.0' xmlns:xupdate='http://www.xmldb.org/xupdate'>" + "  <xupdate:update select=\"document('" + profilesDB + "/" + profileName + "')//USER_INTERESTS\">" + interestsElements + "</xupdate:update>" + "</xupdate:modifications>";
        XUpdateQueryService updateService = (XUpdateQueryService) CollectionsManager.getService(profilesDB, true, "XUpdateQueryService");
        long res = updateService.update(updateQuery);
    }

    public static void updateUserCity(String profileName, String city) throws XMLDBException {
        String updateQuery = "<xupdate:modifications version='1.0' xmlns:xupdate='http://www.xmldb.org/xupdate'>" + "  <xupdate:update select=\"document('" + profilesDB + "/" + profileName + "')//USER_CITY\">" + city + "</xupdate:update>" + "</xupdate:modifications>";
        XUpdateQueryService updateService = (XUpdateQueryService) CollectionsManager.getService(profilesDB, true, "XUpdateQueryService");
        long res = updateService.update(updateQuery);
    }

    public static void updateUserSex(String profileName, String Sex) throws XMLDBException {
        String updateQuery = "<xupdate:modifications version='1.0' xmlns:xupdate='http://www.xmldb.org/xupdate'>" + "  <xupdate:update select=\"document('" + profilesDB + "/" + profileName + "')//USER_SEX\">" + Sex + "</xupdate:update>" + "</xupdate:modifications>";
        XUpdateQueryService updateService = (XUpdateQueryService) CollectionsManager.getService(profilesDB, true, "XUpdateQueryService");
        long res = updateService.update(updateQuery);
    }

    public static void updateUserBirthdate(String profileName, String birthdateYear, String birthdateMonth, String birthdateDay) throws XMLDBException {
        String updateQuery = "<xupdate:modifications version='1.0' xmlns:xupdate='http://www.xmldb.org/xupdate'>" + "  <xupdate:update select=\"document('" + profilesDB + "/" + profileName + "')//USER_BIRTH_DATE/YEAR\">" + birthdateYear + "</xupdate:update>" + "  <xupdate:update select=\"document('" + profilesDB + "/" + profileName + "')//USER_BIRTH_DATE/MONTH\">" + birthdateMonth + "</xupdate:update>" + "  <xupdate:update select=\"document('" + profilesDB + "/" + profileName + "')//USER_BIRTH_DATE/DAY\">" + birthdateDay + "</xupdate:update>" + "  <xupdate:update select=\"document('" + profilesDB + "/" + profileName + "')//USER_BIRTH_DATE/@year\">" + birthdateYear + "</xupdate:update>" + "  <xupdate:update select=\"document('" + profilesDB + "/" + profileName + "')//USER_BIRTH_DATE/@month\">" + birthdateMonth + "</xupdate:update>" + "  <xupdate:update select=\"document('" + profilesDB + "/" + profileName + "')//USER_BIRTH_DATE/@day\">" + birthdateDay + "</xupdate:update>" + "</xupdate:modifications>";
        XUpdateQueryService updateService = (XUpdateQueryService) CollectionsManager.getService(profilesDB, true, "XUpdateQueryService");
        long res = updateService.update(updateQuery);
    }

    public static String getUserAddinfoById(String id) {
        String addInfo = "";
        try {
            String xquery = "document('" + profilesDB + "/" + id + "')/USER_PROFILE/USER_ADITIONAL_INFO/text()";
            XQueryService service = (XQueryService) CollectionsManager.getService(profilesDB, true, "XQueryService");
            ResourceSet result = service.query(xquery);
            if (result.getSize() > 0) {
                XMLResource resource = (XMLResource) result.getResource(0);
                addInfo = resource.getContent().toString();
            }
        } catch (Exception e) {
            log.error("Error getting user addInfo by id: " + e);
            e.printStackTrace();
        }
        return addInfo;
    }

    public static String getUserCityById(String id) {
        String userCity = "";
        try {
            String xquery = "document('" + profilesDB + "/" + id + "')/USER_PROFILE/USER_CITY/text()";
            XQueryService service = (XQueryService) CollectionsManager.getService(profilesDB, true, "XQueryService");
            ResourceSet result = service.query(xquery);
            if (result.getSize() > 0) {
                XMLResource resource = (XMLResource) result.getResource(0);
                userCity = resource.getContent().toString();
            }
        } catch (Exception e) {
            log.error("Error getting user addInfo by id: " + e);
            e.printStackTrace();
        }
        return userCity;
    }

    public static String getUserSexById(String id) {
        String userSex = "";
        try {
            String xquery = "document('" + profilesDB + "/" + id + "')/USER_PROFILE/USER_SEX/text()";
            XQueryService service = (XQueryService) CollectionsManager.getService(profilesDB, true, "XQueryService");
            ResourceSet result = service.query(xquery);
            if (result.getSize() > 0) {
                XMLResource resource = (XMLResource) result.getResource(0);
                userSex = resource.getContent().toString();
            }
        } catch (Exception e) {
            log.error("Error getting user addInfo by id: " + e);
            e.printStackTrace();
        }
        return userSex;
    }

    public static String[] getUserBirthdateById(String id) {
        String birthdateYear = "";
        String birthdateMonth = "";
        String birthdateDay = "";
        try {
            String xquery = "document('" + profilesDB + "/" + id + "')/USER_PROFILE/USER_BIRTH_DATE/YEAR/text()";
            XQueryService service = (XQueryService) CollectionsManager.getService(profilesDB, true, "XQueryService");
            ResourceSet result = service.query(xquery);
            if (result.getSize() > 0) {
                XMLResource resource = (XMLResource) result.getResource(0);
                birthdateYear = resource.getContent().toString();
            } else {
                System.err.println("Not found year in user profile! " + xquery);
            }
            xquery = "document('" + profilesDB + "/" + id + "')/USER_PROFILE/USER_BIRTH_DATE/MONTH/text()";
            service = (XQueryService) CollectionsManager.getService(profilesDB, true, "XQueryService");
            result = service.query(xquery);
            if (result.getSize() > 0) {
                XMLResource resource = (XMLResource) result.getResource(0);
                birthdateMonth = resource.getContent().toString();
            } else {
                System.err.println("Not found month in user profile!");
            }
            xquery = "document('" + profilesDB + "/" + id + "')/USER_PROFILE/USER_BIRTH_DATE/DAY/text()";
            service = (XQueryService) CollectionsManager.getService(profilesDB, true, "XQueryService");
            result = service.query(xquery);
            if (result.getSize() > 0) {
                XMLResource resource = (XMLResource) result.getResource(0);
                birthdateDay = resource.getContent().toString();
            } else {
                System.err.println("Not found day in user profile!");
            }
        } catch (Exception e) {
            log.error("Error getting user addInfo by id: " + e);
            e.printStackTrace();
        }
        return new String[] { birthdateYear, birthdateMonth, birthdateDay };
    }

    public static String getUserNameById(String id) {
        String name = "";
        try {
            String xquery = "document('" + profilesDB + "/" + id + "')/USER_PROFILE/USER_NAME/text()";
            XQueryService service = (XQueryService) CollectionsManager.getService(profilesDB, true, "XQueryService");
            ResourceSet result = service.query(xquery);
            if (result.getSize() > 0) {
                XMLResource resource = (XMLResource) result.getResource(0);
                name = resource.getContent().toString();
            }
        } catch (Exception e) {
            log.error("Error getting user name by id: " + e);
            e.printStackTrace();
        }
        return name;
    }

    public static String getUserAvatarById(String id) {
        String name = "";
        try {
            String xquery = "document('" + profilesDB + "/" + id + "')/USER_PROFILE/USER_AVATAR/text()";
            XQueryService service = (XQueryService) CollectionsManager.getService(profilesDB, true, "XQueryService");
            ResourceSet result = service.query(xquery);
            if (result.getSize() > 0) {
                XMLResource resource = (XMLResource) result.getResource(0);
                name = resource.getContent().toString();
            }
        } catch (Exception e) {
            log.error("Error getting user name by id: " + e);
            e.printStackTrace();
        }
        return name;
    }

    public static void updateUserAvatar(String profileName, String avatarId) throws XMLDBException {
        String updateQuery = "<xupdate:modifications version='1.0' xmlns:xupdate='http://www.xmldb.org/xupdate'>" + "  <xupdate:update select=\"document('" + profilesDB + "/" + profileName + "')//USER_AVATAR\">" + avatarId + "</xupdate:update>" + "</xupdate:modifications>";
        XUpdateQueryService updateService = (XUpdateQueryService) CollectionsManager.getService(profilesDB, true, "XUpdateQueryService");
        long res = updateService.update(updateQuery);
    }

    public static boolean getUserInterestById(String id, String interest) {
        try {
            String xquery = "document('" + profilesDB + "/" + id + "')/USER_PROFILE/USER_INTERESTS/INTEREST[text()='" + interest + "']/text()";
            XQueryService service = (XQueryService) CollectionsManager.getService(profilesDB, true, "XQueryService");
            ResourceSet result = service.query(xquery);
            if (result.getSize() > 0) {
                return true;
            }
        } catch (Exception e) {
            log.error("Error getting user interest by id: " + e);
            e.printStackTrace();
        }
        return false;
    }

    public static int countLogRecords(String section, String profileName) throws XMLDBException {
        long lastActivity = getLastTimeActivity(profileName);
        String xquery = "count(document('" + logsDB + "/vo_log.xml')/LOG/EVENT[SECTION ='" + section + "'][EVENT_PC_TIME gt '" + lastActivity + "'])";
        XQueryService service = (XQueryService) CollectionsManager.getService(logsDB, true, "XQueryService");
        ResourceSet result = service.query(xquery);
        if (result.getSize() > 0) {
            try {
                return Integer.parseInt(result.getIterator().nextResource().getContent().toString());
            } catch (NumberFormatException ex) {
                log.error("Error parsing number of records value. Response is: " + result.getIterator().nextResource().getContent());
                return -1;
            }
        }
        return -1;
    }

    public static int countUserLogRecords(String section, String profileName) throws XMLDBException {
        if (null == profileName) return -1;
        String xquery = "count(document('" + logsDB + "/" + profileName + ".log')/LOG/EVENT[SECTION ='" + section + "'])";
        XQueryService service = (XQueryService) CollectionsManager.getService(logsDB, true, "XQueryService");
        ResourceSet result = service.query(xquery);
        try {
            return Integer.parseInt(result.getIterator().nextResource().getContent().toString());
        } catch (NumberFormatException ex) {
            log.error("Error parsing number of user records value. Response is: " + result.getIterator().nextResource().getContent());
            return 0;
        }
    }

    public static long getLastTimeActivity(String profileName) throws XMLDBException {
        long lastActivity = 0;
        if (null == profileName) return lastActivity;
        String activityQuery = "document('" + profileName + "')/USER_PROFILE/USER_TIME_PC_ACTIVITY/text()";
        XQueryService activityService = (XQueryService) CollectionsManager.getCollection(profilesDB, true).getService("XQueryService", "1.0");
        ResourceSet activityResult = activityService.query(activityQuery);
        if (activityResult.getSize() > 0) {
            XMLResource activityResource = (XMLResource) activityResult.getResource(0);
            try {
                lastActivity = Long.parseLong(activityResource.getContent().toString());
            } catch (NumberFormatException ex) {
                log.error("Error parsing last activity time value. Response is: " + activityResource.getContent());
            }
        } else {
            log.error("Error parsing last activity time value. : returned no resluts.");
        }
        return lastActivity;
    }

    public static void updateLastItem(String objId, String sectionName) throws XMLDBException {
        String xquery = "<xupdate:modifications version='1.0' xmlns:xupdate='http://www.xmldb.org/xupdate'>";
        xquery = xquery + "<xupdate:update select=\"document('" + settingsDB + "/vo_description.xml')/OBSERVATORY/DATA_SECTIONS/SECTION/LAST_ITEM[ ../NAME ='" + sectionName + "']\">" + objId + "</xupdate:update>";
        xquery = xquery + "</xupdate:modifications>";
        try {
            XUpdateQueryService service = (XUpdateQueryService) CollectionsManager.getService(profilesDB, true, "XUpdateQueryService");
            long res = service.update(xquery);
        } catch (Exception e) {
            System.out.println(e);
        }
        return;
    }

    public static void fastRefresh(VO voBean, String profileName) throws XMLDBException {
        String query = "document('vo_description.xml')/OBSERVATORY/DATA_SECTIONS/SECTION";
        XQueryService service = (XQueryService) CollectionsManager.getService(settingsDB, true, "XQueryService");
        ResourceSet result = service.query(query);
        for (int i = 0; i < result.getSize(); i++) {
            XMLResource resource = (XMLResource) result.getResource(i);
            Element readSectionElm = builder.build((org.w3c.dom.Document) (resource.getContentAsDOM())).getRootElement();
            String sectionName = readSectionElm.getChild("NAME").getText();
            int tempInt = countUserLogRecords(readSectionElm.getChild("NAME").getText(), profileName);
            String count_user = (tempInt == -1) ? "-" : Integer.toString(tempInt);
            voBean.setTrackItems(sectionName, count_user);
        }
        voBean.setTotalMailItems(getImailsCount(profileName, false));
        voBean.setUnreadMailItems(getImailsCount(profileName, true));
        voBean.setBasketItems(getBasketItemsNumber(profileName));
        voBean.setForumItems(countUserLogRecords("Forum", profileName));
    }

    public static void setSessionCurrentPage(HttpServletRequest request) {
        request.getSession().setAttribute("currentPage", request.getRequestURI().substring(request.getRequestURI().lastIndexOf('/')) + (null == request.getQueryString() ? "" : "?" + request.getQueryString()));
        log.debug("Current page: " + request.getSession().getAttribute("currentPage"));
    }

    public static String getRequestUnicodeParameter(HttpServletRequest request, String paramName) throws UnsupportedEncodingException {
        if (null != request.getParameterMap()) {
            return (request.getParameterMap().containsKey(paramName)) ? new String(request.getParameter(paramName).getBytes("8859_1"), "UTF8") : null;
        } else {
            if (null == request.getParameter(paramName)) return null;
            return new String(request.getParameter(paramName).getBytes("8859_1"), "UTF8");
        }
    }

    public static String getMultipartRequestUnicodeParameter(HttpServletRequest request, String paramName) throws UnsupportedEncodingException {
        if (null != request.getParameterMap()) {
            return (request.getParameterMap().containsKey(paramName)) ? new String(request.getParameter(paramName).getBytes(), "UTF8") : null;
        } else {
            if (null == request.getParameter(paramName)) return null;
            return new String(request.getParameter(paramName).getBytes(), "UTF8");
        }
    }

    public static org.w3c.dom.Document readDocument(URL docUrl) throws IOException, SAXException, FactoryConfigurationError, ParserConfigurationException {
        InputStream xmlInputStream = docUrl.openStream();
        org.w3c.dom.Document curXml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlInputStream);
        xmlInputStream.close();
        return curXml;
    }

    public static Hashtable docToHashtable(DOMResult domres) {
        org.jdom.Element docElm = builder.build((org.w3c.dom.Document) domres.getNode()).getRootElement();
        Hashtable values = new Hashtable();
        List elements = docElm.getChildren("element");
        for (Iterator it = elements.iterator(); it.hasNext(); ) {
            Element elm = (Element) it.next();
            Vector newData = new Vector();
            if (values.containsKey(elm.getChildText("path"))) {
                newData = (Vector) values.get(elm.getChildText("path"));
            }
            newData.add(elm.getChildText("nodeValue"));
            values.put(elm.getChildText("path"), newData);
        }
        return values;
    }

    public static Hashtable displaysToHashtable(DOMResult domres) {
        org.jdom.Element docElm = builder.build((org.w3c.dom.Document) domres.getNode()).getRootElement();
        Hashtable values = new Hashtable();
        List elements = docElm.getChildren("element");
        for (Iterator it = elements.iterator(); it.hasNext(); ) {
            Element elm = (Element) it.next();
            if (null != elm.getChildText("type") && elm.getChildText("type").length() != 0) values.put(elm.getChildText("type"), elm.getChildText("path"));
        }
        return values;
    }

    public static org.w3c.dom.Document updateSchema(Hashtable data, org.w3c.dom.Document doc) throws org.jdom.JDOMException {
        Element docElm = builder.build(doc).getRootElement();
        List fieldChildElements = docElm.getChildren("FIELD");
        for (Iterator it = fieldChildElements.iterator(); it.hasNext(); ) {
            Element newFieldElm = (Element) it.next();
            newFieldElm = processFieldSchemaElm(data, newFieldElm);
        }
        org.jdom.output.DOMOutputter ou = new org.jdom.output.DOMOutputter();
        return ou.output(docElm.getDocument().getDocument());
    }

    public static Element processFieldSchemaElm(Hashtable data, Element fieldElm) {
        if (null != fieldElm.getChild("KEY")) {
            if (data.containsKey(fieldElm.getChild("KEY").getText())) {
                Vector dataElements = (Vector) data.get((String) fieldElm.getChild("KEY").getText());
                for (Iterator it = dataElements.iterator(); it.hasNext(); ) {
                    String nextElm = (String) it.next();
                    Element elm = new Element("DATAVALUE");
                    elm.setText(nextElm);
                    fieldElm.addContent(elm);
                }
            }
        } else {
            List fieldChildElements = fieldElm.getChildren("FIELD");
            for (Iterator it = fieldChildElements.iterator(); it.hasNext(); ) {
                Element newFieldElm = (Element) it.next();
                newFieldElm = processFieldSchemaElm(data, newFieldElm);
            }
        }
        return fieldElm;
    }
}
