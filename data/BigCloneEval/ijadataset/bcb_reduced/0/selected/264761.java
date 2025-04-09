package vobs.webapp;

import javax.servlet.http.*;
import org.apache.log4j.*;
import org.apache.struts.action.*;
import org.jdom.*;
import org.jdom.output.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import org.xmldb.api.modules.*;
import org.xmldb.api.base.*;
import org.exist.xmldb.*;
import org.exist.xmldb.XQueryService;
import vobs.datamodel.*;
import vobs.dbaccess.*;
import vobs.store.*;
import wdc.settings.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.net.URL;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.zip.*;
import java.io.IOException;
import java.io.*;

public final class EditBasketAction extends Action {

    private static Logger log = Logger.getLogger(EditBasketAction.class);

    private static DefaultJDOMFactory factory = new DefaultJDOMFactory();

    private static XMLOutputter outXml = new XMLOutputter(Format.getPrettyFormat());

    private static String userDB = Settings.get("vo_meta.userProfilesResource");

    private static String forumDB = Settings.get("vo_meta.forumResource");

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ActionErrors errors = new ActionErrors();
        HttpSession session = request.getSession();
        User voUser = (User) session.getAttribute("voUser");
        VO virtObs = (VO) session.getAttribute("vobean");
        if (null == virtObs) {
            session.removeAttribute("currentPage");
            return (mapping.findForward("logon"));
        }
        if (voUser == null) {
            session.removeAttribute("currentPage");
            log.error("Session is missing or has expired for client from " + request.getRemoteAddr());
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.session.nouser"));
            saveErrors(request, errors);
            return (mapping.findForward("logon"));
        }
        if (!voUser.isAnonymous()) {
            String objectId = request.getParameter("objId");
            String action = request.getParameter("action");
            String taskValue = request.getParameter("task");
            if (null != objectId && objectId.length() > 0 && null != action) {
                if (action.equalsIgnoreCase("remove")) {
                    removeBasketObject(voUser.getProfileName(), objectId);
                }
                if (action.equalsIgnoreCase("saveNote") && null != request.getParameter("noteText")) {
                    noteBasketObject(voUser.getProfileName(), objectId, request.getParameter("noteText"));
                }
            }
            if (null != action && action.equalsIgnoreCase("archive")) {
                archiveBasketObjects(voUser.getProfileName(), request, false);
            }
            if (null != action && action.equalsIgnoreCase("taskSel")) {
                if (null != taskValue && taskValue.equalsIgnoreCase("archive")) {
                    archiveBasketObjects(voUser.getProfileName(), request, true);
                }
                if (null != taskValue && taskValue.equalsIgnoreCase("delete")) {
                    String[] items = request.getParameterValues("selectedItem");
                    for (int i = 0; i < items.length; i++) {
                        removeBasketObject(voUser.getProfileName(), items[i].toString());
                    }
                }
            }
            return (mapping.findForward("success"));
        } else {
            session.removeAttribute("currentPage");
            log.error("User from " + request.getRemoteAddr() + " is not authorize to use this page.");
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("error.voUser.authorize"));
            saveErrors(request, errors);
            return (mapping.findForward("logon"));
        }
    }

    public static void writeBasketObject(String profileName, String objId, String requestString, String note, String outputObjectType) {
        org.jdom.Element objIdElm = factory.element("BASKET_OBJECT");
        objIdElm.addContent(factory.element("ID").setText(objId));
        objIdElm.addContent(factory.element("NOTE").setText(note));
        objIdElm.addContent(factory.element("OBJECT_TYPE").setText(outputObjectType));
        objIdElm.addContent(factory.element("REQUEST").setText(requestString));
        objIdElm.addContent(factory.element("TIME").setText(VO.defaultTimeFormat.format(new Date())));
        String xquery = "<xupdate:modifications version='1.0' xmlns:xupdate='http://www.xmldb.org/xupdate'>";
        try {
            String query = "<xupdate:append select=\"document('" + userDB + "/" + profileName + "')/USER_PROFILE/USER_DATA_BASKET\" child=\"1\">" + outXml.outputString(objIdElm) + "</xupdate:append>";
            xquery = xquery + query;
            xquery = xquery + "</xupdate:modifications>";
            XUpdateQueryService service = (XUpdateQueryService) CollectionsManager.getService(userDB, true, "XUpdateQueryService");
            long res = service.update(xquery);
        } catch (Exception e) {
            log.debug("Error updating basket object: " + e);
            e.printStackTrace();
        }
    }

    private static void removeBasketObject(String profileName, String objId) {
        String xquery = "<xupdate:modifications version='1.0' xmlns:xupdate='http://www.xmldb.org/xupdate'>";
        try {
            String query = "  <xupdate:remove select=\"document('" + userDB + "/" + profileName + "')/USER_PROFILE/USER_DATA_BASKET/BASKET_OBJECT[ID/text() = '" + objId + "']\"/>";
            xquery = xquery + query;
            xquery = xquery + "</xupdate:modifications>";
            XUpdateQueryService service = (XUpdateQueryService) CollectionsManager.getService(userDB, true, "XUpdateQueryService");
            long res = service.update(xquery);
        } catch (Exception e) {
            log.debug("Error removing basket object: " + e);
            e.printStackTrace();
        }
    }

    private static void noteBasketObject(String profileName, String objId, String noteText) {
        try {
            String query = "<xupdate:modifications version='1.0' xmlns:xupdate='http://www.xmldb.org/xupdate'>" + "<xupdate:update select=\"document('" + userDB + "/" + profileName + "')/USER_PROFILE/USER_DATA_BASKET/BASKET_OBJECT[ID/text() = '" + objId + "']/NOTE\">" + noteText + "</xupdate:update></xupdate:modifications>";
            XUpdateQueryService service = (XUpdateQueryService) CollectionsManager.getService(userDB, true, "XUpdateQueryService");
            long res = service.update(query);
        } catch (Exception e) {
            log.debug("Error updating basket object: " + e);
            e.printStackTrace();
        }
    }

    private static void archiveBasketObjects(String profileName, HttpServletRequest request, boolean onlySelected) throws XMLDBException, MalformedURLException, IOException, DataStoreException {
        String archiveId = "BA_" + profileName;
        String note = "User basket archive";
        String objType = "basketArchive";
        removeBasketObject(profileName, archiveId);
        Vector objIds = new Vector();
        String prdString = "";
        String[] items = null;
        if (onlySelected) {
            items = request.getParameterValues("selectedItem");
            prdString = prdString + "[";
            for (int i = 0; i < items.length; i++) {
                if (i < (items.length - 1)) {
                    prdString = prdString + "ID/text()='" + items[i].toString() + "' or ";
                } else {
                    prdString = prdString + "ID/text()='" + items[i].toString() + "'";
                }
            }
            prdString = prdString + "]";
        }
        org.w3c.dom.Element archiveResultDocument = null;
        String metadataFile = "";
        if (null != profileName) {
            String queryStr = "xquery version \"1.0\"; " + "  let $basketObjects := document(\"" + profileName + "\")/USER_PROFILE/USER_DATA_BASKET/BASKET_OBJECT";
            if (onlySelected) {
                queryStr = queryStr + prdString;
            }
            queryStr = queryStr + " " + "  return <BasketArchive><User>" + VOAccess.getUserNameById(profileName) + "</User> {" + "    for $obj in $basketObjects " + "  	return <Item> " + "       <Id>{$obj/ID/text()}</Id> " + "       <Time>{$obj/TIME/text()}</Time> " + "       <Note>{$obj/NOTE/text()}</Note> " + "       <Type>{$obj/OBJECT_TYPE/text()}</Type> " + "  	</Item> }" + "  </BasketArchive> ";
            XQueryService queryService = (XQueryService) CollectionsManager.getService(userDB, true, "XQueryService");
            ResourceSet queryResult = queryService.query(queryStr);
            if (queryResult.getSize() > 0) {
                XMLResource resource = (XMLResource) queryResult.getResource(0);
                archiveResultDocument = ((org.w3c.dom.Document) resource.getContentAsDOM()).getDocumentElement();
                metadataFile = (String) resource.getContent();
            }
            if (archiveResultDocument != null) {
                NodeList resources = archiveResultDocument.getElementsByTagName("Item");
                for (int i = 0; i < resources.getLength(); i++) {
                    org.w3c.dom.Element res = (org.w3c.dom.Element) resources.item(i);
                    if (res.getElementsByTagName("Id").getLength() > 0 && res.getElementsByTagName("Id").item(0).getFirstChild() != null) {
                        String id = res.getElementsByTagName("Id").item(0).getFirstChild().getNodeValue();
                        objIds.add(id);
                    }
                }
            }
        }
        if (null == Settings.get("vo_store.dir") || !(new File(Settings.get("vo_store.dir"))).isDirectory()) {
            log.error("Error accessing directory vo_store.dir: " + Settings.get("vo_store.dir"));
            return;
        }
        File archiveFile = new File(Settings.get("vo_store.dir") + "/" + archiveId + ".tmp");
        ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(archiveFile));
        for (Iterator it = objIds.iterator(); it.hasNext(); ) {
            String nextObjId = (String) it.next();
            URL objectUrl = new URL(request.getRequestURL().substring(0, request.getRequestURL().lastIndexOf("/")) + "/filestore?objId=" + nextObjId);
            InputStream inp = objectUrl.openStream();
            if (null != inp) {
                zout.putNextEntry(new ZipEntry(nextObjId));
                byte[] buf = new byte[1048576];
                while (inp.available() > 0) {
                    int count = inp.read(buf, 0, buf.length);
                    zout.write(buf, 0, count);
                    zout.flush();
                }
                inp.close();
                zout.closeEntry();
            } else {
                log.error("Error getting " + nextObjId + " basket object");
            }
        }
        zout.putNextEntry(new ZipEntry("metadata.xml"));
        zout.write(metadataFile.getBytes());
        zout.closeEntry();
        zout.close();
        FileInputStream inputDataStream = new FileInputStream(archiveFile);
        FileStoreSave.storeFile(inputDataStream, archiveId, objType, inputDataStream.available() + "", archiveId, "fileAction");
        inputDataStream.close();
        archiveFile.delete();
        writeBasketObject(profileName, archiveId, "", note + ": " + objIds.size() + " item(s)", objType);
    }
}
