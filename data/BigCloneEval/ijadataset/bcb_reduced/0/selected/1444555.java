package org.fao.geonet.services.resources;

import jeeves.exceptions.BadParameterEx;
import jeeves.exceptions.MissingParameterEx;
import jeeves.exceptions.ResourceNotFoundEx;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.BinaryFile;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.MdInfo;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.Utils;
import org.fao.geonet.util.MailSender;
import org.jdom.Element;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Sends the resource to the client in a zip archive with metadata and license
  */
public class DownloadArchive implements Service {

    private static String FS = File.separator;

    private String appPath;

    private String stylePath;

    private static final String FILE_NAME_SUBSTR = "filename=";

    public void init(String appPath, ServiceConfig params) throws Exception {
        this.appPath = appPath;
        this.stylePath = appPath + FS + Geonet.Path.STYLESHEETS + FS;
    }

    public Element exec(Element params, ServiceContext context) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getDataManager();
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        UserSession session = context.getUserSession();
        String id = Utils.getIdentifierFromParameters(params, context);
        String access = Util.getParam(params, Params.ACCESS, Params.Access.PUBLIC);
        if (access.equals(Params.Access.PUBLIC)) {
            File dir = new File(Lib.resource.getDir(context, access, id));
            String fname = Util.getParam(params, Params.FNAME);
            if (fname.contains("..")) {
                throw new BadParameterEx("Invalid character found in resource name.", fname);
            }
            File file = new File(dir, fname);
            return BinaryFile.encode(200, file.getAbsolutePath(), false);
        }
        Element elData = (Element) session.getProperty(Geonet.Session.FILE_DISCLAIMER);
        if (elData == null) {
            return new Element("response");
        } else {
            String idAllowed = elData.getChildText(Geonet.Elem.ID);
            if (idAllowed == null || !idAllowed.equals(id)) {
                return new Element("response");
            }
        }
        boolean doNotify = false;
        Lib.resource.checkPrivilege(context, id, AccessManager.OPER_DOWNLOAD);
        doNotify = true;
        String username = session.getUsername();
        if (username == null) username = "internet";
        String profile = session.getProfile();
        String userId = session.getUserId();
        String name = Util.getParam(params, Params.NAME);
        String org = Util.getParam(params, Params.ORG);
        String email = Util.getParam(params, Params.EMAIL);
        String comments = Util.getParam(params, Params.COMMENTS);
        Element entered = new Element("entered").addContent(params.cloneContent());
        Element userDetails = new Element("userdetails");
        if (!username.equals("internet")) {
            Element elUser = dbms.select("SELECT username, surname, name, address, state, zip, country, email, organisation FROM Users WHERE id=?", new Integer(userId));
            if (elUser.getChild("record") != null) {
                userDetails.addContent(elUser.getChild("record").cloneContent());
            }
        }
        MdInfo info = dm.getMetadataInfo(dbms, id);
        File zFile = File.createTempFile(username + "_" + info.uuid, ".zip");
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zFile));
        out.setLevel(Deflater.NO_COMPRESSION);
        Element downloaded = new Element("downloaded");
        File dir = new File(Lib.resource.getDir(context, access, id));
        List files = params.getChildren(Params.FNAME);
        for (Object o : files) {
            Element elem = (Element) o;
            String fname = elem.getText();
            if (fname.contains("..")) {
                continue;
            }
            File file = new File(dir, fname);
            if (!file.exists()) throw new ResourceNotFoundEx(file.getAbsolutePath());
            Element fileInfo = new Element("file");
            Element details = BinaryFile.encode(200, file.getAbsolutePath(), false);
            String remoteURL = details.getAttributeValue("remotepath");
            if (remoteURL != null) {
                context.debug("Downloading " + remoteURL + " to archive " + zFile.getName());
                fileInfo.setAttribute("size", "unknown");
                fileInfo.setAttribute("datemodified", "unknown");
                fileInfo.setAttribute("name", remoteURL);
                notifyAndLog(doNotify, id, info.uuid, access, username, remoteURL + " (local config: " + file.getAbsolutePath() + ")", context);
                fname = details.getAttributeValue("remotefile");
            } else {
                context.debug("Writing " + fname + " to archive " + zFile.getName());
                fileInfo.setAttribute("size", file.length() + "");
                fileInfo.setAttribute("name", fname);
                Date date = new Date(file.lastModified());
                fileInfo.setAttribute("datemodified", sdf.format(date));
                notifyAndLog(doNotify, id, info.uuid, access, username, file.getAbsolutePath(), context);
            }
            addFile(out, file.getAbsolutePath(), details, fname);
            downloaded.addContent(fileInfo);
        }
        boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = false;
        Element elMd = dm.getMetadata(context, id, forEditing, withValidationErrors, keepXlinkAttributes);
        if (elMd == null) throw new MetadataNotFoundEx("Metadata not found - deleted?");
        String briefXslt = stylePath + Geonet.File.METADATA_BRIEF;
        Element elBrief = Xml.transform(elMd, briefXslt);
        Element root = new Element("root");
        elBrief.setAttribute("changedate", info.changeDate);
        elBrief.setAttribute("currdate", now());
        root.addContent(elBrief);
        root.addContent(downloaded);
        root.addContent(entered);
        root.addContent(userDetails);
        context.debug("Passed to metadata-license-annex.xsl:\n " + Xml.getString(root));
        String licenseAnnexXslt = stylePath + Geonet.File.LICENSE_ANNEX_XSL;
        File licenseAnnex = File.createTempFile(username + "_" + info.uuid, ".annex");
        FileOutputStream las = new FileOutputStream(licenseAnnex);
        Xml.transform(root, licenseAnnexXslt, las);
        las.close();
        InputStream in = new FileInputStream(licenseAnnex);
        addFile(out, Geonet.File.LICENSE_ANNEX, in);
        in.close();
        includeLicenseFiles(context, out, root);
        String outmef = MEFLib.doExport(context, info.uuid, MEFLib.Format.PARTIAL.toString(), false);
        in = new FileInputStream(outmef);
        addFile(out, "metadata.zip", in);
        in.close();
        if (out != null) out.close();
        return BinaryFile.encode(200, zFile.getAbsolutePath(), true);
    }

    private void includeLicenseFiles(ServiceContext context, ZipOutputStream out, Element root) throws Exception, MalformedURLException, FileNotFoundException, IOException {
        Element license = Xml.selectElement(root, "metadata/*/licenseLink");
        if (license != null) {
            String licenseURL = license.getText();
            context.debug("license URL = " + licenseURL);
            String licenseFilesPath = getLicenseFilesPath(licenseURL, context);
            context.debug(" licenseFilesPath = " + licenseFilesPath);
            if (licenseFilesPath != null) {
                File licenseFilesDir = new File(licenseFilesPath);
                File[] licenseFiles = licenseFilesDir.listFiles();
                if (licenseFiles == null) return;
                for (File licenseFile : licenseFiles) {
                    context.debug("adding " + licenseFile.getAbsolutePath() + " to zip file");
                    InputStream in = new FileInputStream(licenseFile);
                    addFile(out, licenseFile.getName(), in);
                }
            }
        }
    }

    private String getLicenseFilesPath(String licenseURL, ServiceContext context) throws MalformedURLException {
        URL url = new URL(licenseURL);
        String licenseFilesPath = url.getHost() + url.getPath();
        context.debug("licenseFilesPath= " + licenseFilesPath);
        String path = context.getAppPath();
        context.debug("path= " + path);
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        ServiceConfig configHandler = gc.getHandlerConfig();
        String licenseDir = configHandler.getValue(Geonet.Config.LICENSE_DIR);
        context.debug("licenseDir= " + licenseDir);
        if (licenseDir == null) return null;
        File directory = new File(licenseDir);
        if (!directory.isAbsolute()) licenseDir = path + licenseDir;
        return licenseDir + '/' + licenseFilesPath;
    }

    private void addFile(ZipOutputStream zos, String path, Element details, String name) throws Exception {
        ZipEntry entry = new ZipEntry(name);
        zos.putNextEntry(entry);
        BinaryFile.write(details, zos);
        zos.closeEntry();
    }

    private void addFile(ZipOutputStream zos, String name, InputStream in) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        zos.putNextEntry(entry);
        BinaryFile.copy(in, zos, true, false);
        zos.closeEntry();
    }

    private static String now() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(cal.getTime());
    }

    private void notifyAndLog(boolean doNotify, String id, String uuid, String access, String username, String theFile, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SettingManager sm = gc.getSettingManager();
        DataManager dm = gc.getDataManager();
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        if (access.equals(Params.Access.PRIVATE)) dm.increasePopularity(context, id);
        if (doNotify) {
            String site = sm.getValue("system/site/siteId");
            String host = sm.getValue("system/feedback/mailServer/host");
            String port = sm.getValue("system/feedback/mailServer/port");
            String from = sm.getValue("system/feedback/email");
            String fromDescr = "GeoNetwork administrator";
            String dateTime = now();
            context.info("DOWNLOADED:" + theFile + "," + id + "," + uuid + "," + context.getIpAddress() + "," + username);
            if (host.trim().length() == 0 || from.trim().length() == 0) {
                context.debug("Skipping email notification");
            } else {
                context.debug("Sending email notification for file : " + theFile);
                StringBuffer query = new StringBuffer();
                query.append("SELECT g.id, g.name, g.email ");
                query.append("FROM   OperationAllowed oa, Groups g ");
                query.append("WHERE  oa.operationId =" + AccessManager.OPER_NOTIFY + " ");
                query.append("AND    oa.metadataId = ?");
                query.append("AND    oa.groupId = g.id");
                Element groups = dbms.select(query.toString(), new Integer(id));
                for (Iterator i = groups.getChildren().iterator(); i.hasNext(); ) {
                    Element group = (Element) i.next();
                    String name = group.getChildText("name");
                    String email = group.getChildText("email");
                    if (email.trim().length() != 0) {
                        String subject = "File " + theFile + " has been downloaded at " + dateTime;
                        String message = "GeoNetwork (site: " + site + ") notifies you, as supervisor of group " + name + " that data file " + theFile + " attached to metadata record with id number " + id + " (uuid: " + uuid + ")" + " has been downloaded from address " + context.getIpAddress() + " by user " + username + ".";
                        try {
                            MailSender sender = new MailSender(context);
                            sender.send(host, Integer.parseInt(port), from, fromDescr, email, null, subject, message);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
