package org.fao.geonet.kernel.mef;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.BinaryFile;
import jeeves.utils.Xml;
import jeeves.xlink.XLink;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.mef.MEFLib.Format;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.util.ISODate;
import org.jdom.Document;
import org.jdom.Element;
import static org.fao.geonet.kernel.mef.MEFConstants.*;

class Exporter {

    public static String doExport(ServiceContext context, String uuid, Format format, boolean skipUUID) throws Exception {
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        Element record = retrieveMetadata(dbms, uuid);
        String id = record.getChildText("id");
        String data = record.getChildText("data");
        String isTemp = record.getChildText("istemplate");
        if (!"y".equals(isTemp) && !"n".equals(isTemp)) throw new Exception("Cannot export sub template");
        File file = File.createTempFile("mef-", ".mef");
        String pubDir = Lib.resource.getDir(context, "public", id);
        String priDir = Lib.resource.getDir(context, "private", id);
        FileOutputStream fos = new FileOutputStream(file);
        ZipOutputStream zos = new ZipOutputStream(fos);
        createDir(zos, DIR_PUBLIC);
        createDir(zos, DIR_PRIVATE);
        if (!data.startsWith("<?xml")) data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n" + data;
        Element md = Xml.loadString(data, false);
        XLink.removeXLinkAttributes(md);
        data = Xml.getString(md);
        byte[] binData = data.getBytes("UTF-8");
        addFile(zos, FILE_METADATA, new ByteArrayInputStream(binData));
        binData = buildInfoFile(context, record, format, pubDir, priDir, skipUUID).getBytes("UTF-8");
        addFile(zos, FILE_INFO, new ByteArrayInputStream(binData));
        if (format == Format.PARTIAL || format == Format.FULL) savePublic(zos, pubDir);
        if (format == Format.FULL) savePrivate(zos, priDir);
        zos.close();
        return file.getAbsolutePath();
    }

    private static Element retrieveMetadata(Dbms dbms, String uuid) throws SQLException, MetadataNotFoundEx {
        List list = dbms.select("SELECT * FROM Metadata WHERE uuid=?", uuid).getChildren();
        if (list.size() == 0) throw new MetadataNotFoundEx("uuid=" + uuid);
        return (Element) list.get(0);
    }

    private static void createDir(ZipOutputStream zos, String name) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        zos.putNextEntry(entry);
        zos.closeEntry();
    }

    private static void addFile(ZipOutputStream zos, String name, InputStream is) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        zos.putNextEntry(entry);
        BinaryFile.copy(is, zos, true, false);
        zos.closeEntry();
    }

    private static void savePublic(ZipOutputStream zos, String dir) throws IOException {
        File[] files = new File(dir).listFiles(filter);
        if (files != null) for (File file : files) addFile(zos, DIR_PUBLIC + file.getName(), new FileInputStream(file));
    }

    private static void savePrivate(ZipOutputStream zos, String dir) throws IOException {
        File[] files = new File(dir).listFiles(filter);
        if (files != null) for (File file : files) addFile(zos, DIR_PRIVATE + file.getName(), new FileInputStream(file));
    }

    private static String buildInfoFile(ServiceContext context, Element md, Format format, String pubDir, String priDir, boolean skipUUID) throws Exception {
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        Element info = new Element("info");
        info.setAttribute("version", VERSION);
        info.addContent(buildInfoGeneral(md, format, skipUUID, context));
        info.addContent(buildInfoCategories(dbms, md));
        info.addContent(buildInfoPrivileges(context, md));
        info.addContent(buildInfoFiles("public", pubDir));
        info.addContent(buildInfoFiles("private", priDir));
        return Xml.getString(new Document(info));
    }

    private static Element buildInfoGeneral(Element md, Format format, boolean skipUUID, ServiceContext context) {
        String id = md.getChildText("id");
        String uuid = md.getChildText("uuid");
        String schema = md.getChildText("schemaid");
        String isTemplate = md.getChildText("istemplate").equals("y") ? "true" : "false";
        String createDate = md.getChildText("createdate");
        String changeDate = md.getChildText("changedate");
        String siteId = md.getChildText("source");
        String rating = md.getChildText("rating");
        String popularity = md.getChildText("popularity");
        Element general = new Element("general").addContent(new Element("createDate").setText(createDate)).addContent(new Element("changeDate").setText(changeDate)).addContent(new Element("schema").setText(schema)).addContent(new Element("isTemplate").setText(isTemplate)).addContent(new Element("localId").setText(id)).addContent(new Element("format").setText(format.toString())).addContent(new Element("rating").setText(rating)).addContent(new Element("popularity").setText(popularity));
        if (!skipUUID) {
            GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
            general.addContent(new Element("uuid").setText(uuid));
            general.addContent(new Element("siteId").setText(siteId));
            general.addContent(new Element("siteName").setText(gc.getSiteName()));
        }
        return general;
    }

    private static Element buildInfoCategories(Dbms dbms, Element md) throws SQLException {
        Element categ = new Element("categories");
        String id = md.getChildText("id");
        String query = "SELECT name FROM MetadataCateg, Categories " + "WHERE categoryId = id AND metadataId = " + id;
        List list = dbms.select(query).getChildren();
        for (int i = 0; i < list.size(); i++) {
            Element record = (Element) list.get(i);
            String name = record.getChildText("name");
            Element cat = new Element("category");
            cat.setAttribute("name", name);
            categ.addContent(cat);
        }
        return categ;
    }

    private static Element buildInfoPrivileges(ServiceContext context, Element md) throws Exception {
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        String id = md.getChildText("id");
        String query = "SELECT Groups.id as grpid, Groups.name as grpName, Operations.name as operName " + "FROM   OperationAllowed, Groups, Operations " + "WHERE  groupId = Groups.id " + "  AND  operationId = Operations.id " + "  AND  metadataId = " + id;
        HashMap<String, ArrayList<String>> hmPriv = new HashMap<String, ArrayList<String>>();
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        AccessManager am = gc.getAccessManager();
        Set<String> userGroups = am.getUserGroups(dbms, context.getUserSession(), context.getIpAddress());
        List list = dbms.select(query).getChildren();
        for (int i = 0; i < list.size(); i++) {
            Element record = (Element) list.get(i);
            String grpId = record.getChildText("grpid");
            String grpName = record.getChildText("grpname");
            String operName = record.getChildText("opername");
            if (!userGroups.contains(grpId)) continue;
            ArrayList<String> al = hmPriv.get(grpName);
            if (al == null) {
                al = new ArrayList<String>();
                hmPriv.put(grpName, al);
            }
            al.add(operName);
        }
        Element privil = new Element("privileges");
        for (String grpName : hmPriv.keySet()) {
            Element group = new Element("group");
            group.setAttribute("name", grpName);
            privil.addContent(group);
            for (String operName : hmPriv.get(grpName)) {
                Element oper = new Element("operation");
                oper.setAttribute("name", operName);
                group.addContent(oper);
            }
        }
        return privil;
    }

    private static Element buildInfoFiles(String name, String dir) {
        Element root = new Element(name);
        File[] files = new File(dir).listFiles(filter);
        if (files != null) for (File file : files) {
            String date = new ISODate(file.lastModified()).toString();
            Element el = new Element("file");
            el.setAttribute("name", file.getName());
            el.setAttribute("changeDate", date);
            root.addContent(el);
        }
        return root;
    }

    private static FileFilter filter = new FileFilter() {

        public boolean accept(File pathname) {
            if (pathname.getName().equals(".svn")) return false;
            return true;
        }
    };
}
