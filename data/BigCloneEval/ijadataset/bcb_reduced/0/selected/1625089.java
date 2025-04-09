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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import jeeves.constants.Jeeves;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.BinaryFile;
import jeeves.utils.Xml;
import jeeves.xlink.Processor;
import jeeves.xlink.XLink;
import org.apache.batik.css.engine.value.css2.SrcManager;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.guiservices.util.GetFeatureCatalog;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.mef.MEFLib.Format;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.gm03.Gm03Service;
import org.fao.geonet.services.gm03.ISO19139CHEtoGM03;
import org.fao.geonet.services.gm03.ISO19139CHEtoGM03Base;
import org.fao.geonet.util.ISODate;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.DOMBuilder;
import org.jdom.output.DOMOutputter;
import static org.fao.geonet.kernel.mef.MEFConstants.*;

class MEF2Exporter {

    public static String doExport(ServiceContext context, Set<String> uuids, Format format, boolean skipUUID, String stylePath) throws Exception {
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        File file = File.createTempFile("mef-", ".mef");
        FileOutputStream fos = new FileOutputStream(file);
        ZipOutputStream zos = new ZipOutputStream(fos);
        for (Iterator iter = uuids.iterator(); iter.hasNext(); ) {
            String uuid = (String) iter.next();
            createMetadataFolder(context, dbms, uuid, zos, skipUUID, stylePath);
        }
        zos.close();
        return file.getAbsolutePath();
    }

    private static void createMetadataFolder(ServiceContext context, Dbms dbms, String uuid, ZipOutputStream zos, boolean skipUUID, String stylePath) throws Exception {
        Format format = Format.FULL;
        createDir(zos, uuid + FS);
        Element record = retrieveMetadata(dbms, uuid);
        String id = record.getChildText("id");
        String isTemp = record.getChildText("istemplate");
        String schema = record.getChildText("schemaid");
        if (!"y".equals(isTemp) && !"n".equals(isTemp)) throw new Exception("Cannot export sub template");
        String pubDir = Lib.resource.getDir(context, "public", id);
        String priDir = Lib.resource.getDir(context, "private", id);
        createDir(zos, uuid + FS + DIR_PUBLIC);
        createDir(zos, uuid + FS + DIR_PRIVATE);
        if (schema.contains("iso19139") && !schema.equals("iso19139")) {
            String path = stylePath + schema + "/convert/to19139.xsl";
            Element profilMetadata = (Element) record.clone();
            if (profilMetadata != null) {
                ByteArrayInputStream data19139 = formatData(profilMetadata, true, path);
                addFile(zos, uuid + FS + MD_DIR + FILE_METADATA_19139, data19139);
            }
            if (schema.equals("iso19139.che")) {
                Element md = null;
                GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
                DataManager dm = gc.getDataManager();
                Element elMd = dm.getMetadata(context, id, false, true, true);
                try {
                    DOMOutputter outputter = new DOMOutputter();
                    org.jdom.Document doc = new org.jdom.Document(elMd);
                    org.w3c.dom.Document domIn = outputter.output(doc);
                    ISO19139CHEtoGM03Base toGm03 = new ISO19139CHEtoGM03(null, context.getAppPath() + "xsl/conversion/import/ISO19139CHE-to-GM03.xsl");
                    org.w3c.dom.Document domOut = toGm03.convert(domIn);
                    DOMBuilder builder = new DOMBuilder();
                    md = builder.build(domOut).getRootElement();
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw e;
                }
                if (md != null) {
                    String data = Xml.getString(md);
                    byte[] binData = data.getBytes("UTF-8");
                    ByteArrayInputStream dataGM03 = new ByteArrayInputStream(binData);
                    addFile(zos, uuid + FS + MD_DIR + "metadata.gm03.xml", dataGM03);
                }
            }
        }
        ByteArrayInputStream data = formatData(record);
        addFile(zos, uuid + FS + MD_DIR + FILE_METADATA, data);
        String ftUUID = getFeatureCatalogID(context, dbms, uuid);
        if (!ftUUID.equals("")) {
            Element ft = retrieveMetadata(dbms, ftUUID);
            ByteArrayInputStream ftData = formatData(ft);
            addFile(zos, uuid + FS + SCHEMA + FILE_METADATA, ftData);
        }
        byte[] binData = buildInfoFile(context, record, format, pubDir, priDir, skipUUID).getBytes("UTF-8");
        addFile(zos, uuid + FS + FILE_INFO, new ByteArrayInputStream(binData));
        if (format == Format.PARTIAL || format == Format.FULL) savePublic(zos, pubDir, uuid);
        if (format == Format.FULL) savePrivate(zos, priDir, uuid);
    }

    /**
	 * Format xml data
	 * @param elt
	 * @return
	 * @throws Exception
	 */
    private static ByteArrayInputStream formatData(Element elt) throws Exception {
        return formatData(elt, false, "");
    }

    /**
	 * Format xml data
	 * @param elt
	 * @param transform 
	 * @return ByteArrayInputStream
	 * @throws Exception
	 */
    private static ByteArrayInputStream formatData(Element elt, boolean transform, String stylePath) throws Exception {
        String xmlData = elt.getChildText("data");
        Element md = Xml.loadString(xmlData, false);
        md = Processor.processXLink(md);
        XLink.removeXLinkAttributes(md);
        if (transform) {
            md = Xml.transform(md, stylePath);
        }
        String data = Xml.getString(md);
        if (!data.startsWith("<?xml")) data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n" + data;
        byte[] binData = data.getBytes("UTF-8");
        return new ByteArrayInputStream(binData);
    }

    /**
	 * Get Feature Catalog ID if exists
	 * @param context
	 * @param dbms
	 * @param uuid
	 * @return String
	 * @throws Exception
	 */
    private static String getFeatureCatalogID(ServiceContext context, Dbms dbms, String uuid) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getDataManager();
        String id = dm.getMetadataId(dbms, uuid);
        if (id == null) throw new MetadataNotFoundEx("uuid=" + uuid);
        String ftId = GetFeatureCatalog.getRelatedId(id, context);
        String ftUuid = null;
        if (!ftId.equals("")) ftUuid = dm.getMetadataUuid(dbms, ftId);
        return ftUuid != null ? ftUuid : "";
    }

    private static Element retrieveMetadata(Dbms dbms, String uuid) throws SQLException, MetadataNotFoundEx {
        List list = dbms.select("SELECT * FROM Metadata WHERE uuid=?", uuid).getChildren();
        if (list.size() == 0) throw new MetadataNotFoundEx("uuid=" + uuid);
        final Element md = (Element) list.get(0);
        return md;
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

    private static void savePublic(ZipOutputStream zos, String dir, String uuid) throws IOException {
        File[] files = new File(dir).listFiles(filter);
        if (files != null) for (File file : files) addFile(zos, uuid + FS + DIR_PUBLIC + file.getName(), new FileInputStream(file));
    }

    private static void savePrivate(ZipOutputStream zos, String dir, String uuid) throws IOException {
        File[] files = new File(dir).listFiles(filter);
        if (files != null) for (File file : files) addFile(zos, uuid + FS + DIR_PRIVATE + file.getName(), new FileInputStream(file));
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
