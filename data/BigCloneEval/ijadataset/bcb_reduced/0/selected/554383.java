package org.vosao.business.impl.imex;

import static org.vosao.utils.XmlUtil.notNull;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.vosao.business.decorators.TreeItemDecorator;
import org.vosao.business.imex.PageExporter;
import org.vosao.business.imex.ResourceExporter;
import org.vosao.business.imex.task.TaskTimeoutException;
import org.vosao.business.imex.task.ZipOutStreamTaskAdapter;
import org.vosao.dao.DaoTaskException;
import org.vosao.entity.FileEntity;
import org.vosao.entity.FolderEntity;
import org.vosao.entity.FolderPermissionEntity;
import org.vosao.entity.GroupEntity;
import org.vosao.entity.PageEntity;
import org.vosao.enums.FolderPermissionType;
import org.vosao.utils.FolderUtil;
import org.vosao.utils.MimeType;

public class ResourceExporterImpl extends AbstractExporter implements ResourceExporter {

    public ResourceExporterImpl(ExporterFactoryImpl factory) {
        super(factory);
    }

    /**
	 * Add folder and _folder.xml file to zip archive.
	 * @param out - zip output stream
	 * @param folder - folder 
	 * @param zipPath - folder path under which resources will be placed to zip. 
	 * 	             Should not start with / symbol and should end with / symbol.
	 * @throws IOException
	 */
    public void addFolder(final ZipOutStreamTaskAdapter out, final FolderEntity folder, final String zipPath) throws IOException, TaskTimeoutException {
        if (zipPath.length() != 0) {
            out.putNextEntry(new ZipEntry(zipPath));
            out.closeEntry();
        }
        if (!out.isSkip(zipPath + "_folder.xml")) {
            out.putNextEntry(new ZipEntry(zipPath + "_folder.xml"));
            out.write(getFolderSystemFile(folder).getBytes("UTF-8"));
            out.closeEntry();
            out.nextFile();
        }
    }

    /**
	 * Add files from resource folder to zip archive.
	 * @param out - zip output stream
	 * @param folder - folder tree item
	 * @param zipPath - folder path under which resources will be placed to zip. 
	 * 	             Should not start with / symbol and should end with / symbol.
	 * @throws IOException
	 */
    public void addResourcesFromFolder(final ZipOutStreamTaskAdapter out, final TreeItemDecorator<FolderEntity> folder, final String zipPath) throws IOException, TaskTimeoutException {
        addFolder(out, folder.getEntity(), zipPath);
        List<String> childrenNames = new ArrayList<String>();
        for (TreeItemDecorator<FolderEntity> child : folder.getChildren()) {
            addResourcesFromFolder(out, child, zipPath + child.getEntity().getName() + "/");
            childrenNames.add(child.getEntity().getName());
        }
        if (zipPath.startsWith("page/")) {
            String pageURL = zipPath.replace("page", "");
            if (!pageURL.equals("/")) {
                pageURL = pageURL.substring(0, pageURL.length() - 1);
            }
            List<PageEntity> children = getDao().getPageDao().getByParent(pageURL);
            for (PageEntity child : children) {
                if (!childrenNames.contains(child.getPageFriendlyURL())) {
                    addResourcesFromPage(out, child.getFriendlyURL(), zipPath + child.getPageFriendlyURL() + "/");
                }
            }
            List<PageEntity> pages = getDao().getPageDao().selectByUrl(pageURL);
            if (pages.size() > 0) {
                addPageFiles(out, pages.get(0), zipPath);
            }
        }
        List<FileEntity> files = getDao().getFileDao().getByFolder(folder.getEntity().getId());
        for (FileEntity file : files) {
            String filePath = zipPath + file.getFilename();
            if (!out.isSkip(filePath)) {
                out.putNextEntry(new ZipEntry(filePath));
                out.write(getDao().getFileDao().getFileContent(file));
                out.closeEntry();
                out.nextFile();
            }
        }
    }

    /**
	 * Add files from resource folder to zip archive.
	 * @param out - zip output stream
	 * @param folder - folder tree item
	 * @param zipPath - folder path under which resources will be placed to zip. 
	 * 	             Should not start with / symbol and should end with / symbol.
	 * @throws IOException
	 */
    public void addResourcesFromPage(final ZipOutStreamTaskAdapter out, final String pageURL, final String zipPath) throws IOException, TaskTimeoutException {
        out.putNextEntry(new ZipEntry(zipPath));
        out.closeEntry();
        List<PageEntity> children = getDao().getPageDao().getByParent(pageURL);
        for (PageEntity child : children) {
            addResourcesFromPage(out, child.getFriendlyURL(), zipPath + child.getPageFriendlyURL() + "/");
        }
        List<PageEntity> pages = getDao().getPageDao().selectByUrl(pageURL);
        if (pages.size() > 0) {
            addPageFiles(out, pages.get(0), zipPath);
        }
    }

    private void addPageFiles(final ZipOutStreamTaskAdapter out, PageEntity page, String zipPath) throws IOException, TaskTimeoutException {
        if (!out.isSkip(zipPath + "_content.xml")) {
            saveFile(out, zipPath + "_content.xml", getPageExporter().createPageContentXML(page));
        }
        if (!out.isSkip(zipPath + "_comments.xml")) {
            saveFile(out, zipPath + "_comments.xml", getPageExporter().createPageCommentsXML(page.getFriendlyURL()));
        }
        if (!out.isSkip(zipPath + "_permissions.xml")) {
            saveFile(out, zipPath + "_permissions.xml", getPageExporter().createPagePermissionsXML(page.getFriendlyURL()));
        }
        if (!out.isSkip(zipPath + "_tag.xml")) {
            saveFile(out, zipPath + "_tag.xml", getPageExporter().createPageTagXML(page.getFriendlyURL()));
        }
    }

    @Override
    public String getFolderSystemFile(FolderEntity folder) {
        Document doc = DocumentHelper.createDocument();
        Element e = doc.addElement("folder");
        e.addElement("title").setText(notNull(folder.getTitle()));
        Element p = e.addElement("permissions");
        List<FolderPermissionEntity> list = getDao().getFolderPermissionDao().selectByFolder(folder.getId());
        for (FolderPermissionEntity permission : list) {
            createFolderPermissionXML(p, permission);
        }
        return doc.asXML();
    }

    private void createFolderPermissionXML(Element permissionsElement, final FolderPermissionEntity permission) {
        GroupEntity group = getDao().getGroupDao().getById(permission.getGroupId());
        Element permissionElement = permissionsElement.addElement("permission");
        permissionElement.addElement("group").setText(group.getName());
        permissionElement.addElement("permissionType").setText(permission.getPermission().name());
    }

    @Override
    public String importResourceFile(final ZipEntry entry, byte[] data) throws UnsupportedEncodingException, DaoTaskException {
        return importResourceFile("/" + entry.getName(), data);
    }

    @Override
    public String importResourceFile(String name, byte[] data) throws UnsupportedEncodingException, DaoTaskException {
        String folderPath = FolderUtil.getFilePath(name);
        String fileName = FolderUtil.getFileName(name);
        FolderEntity folderEntity = getBusiness().getFolderBusiness().createFolder(folderPath);
        String contentType = MimeType.getContentTypeByExt(FolderUtil.getFileExt(fileName));
        FileEntity fileEntity = getDao().getFileDao().getByName(folderEntity.getId(), fileName);
        if (fileEntity != null) {
            fileEntity.setLastModifiedTime(new Date());
            fileEntity.setSize(data.length);
        } else {
            fileEntity = new FileEntity(fileName, fileName, folderEntity.getId(), contentType, new Date(), data.length);
        }
        getDaoTaskAdapter().fileSave(fileEntity, data);
        return name;
    }

    private PageExporter getPageExporter() {
        return getExporterFactory().getPageExporter();
    }

    /**
	 * Read and import data from _folder.xml file.
	 * @param folderPath - folder path.
	 * @param xml - _folder.xml file.
	 * @throws DocumentException 
	 * @throws DaoTaskException 
	 */
    public void readFolderFile(String folderPath, String xml) throws DocumentException, DaoTaskException {
        FolderEntity folder = getBusiness().getFolderBusiness().createFolder(folderPath);
        Element root = DocumentHelper.parseText(xml).getRootElement();
        String title = root.elementText("title");
        if (!StringUtils.isEmpty(title)) {
            folder.setTitle(title);
            getDaoTaskAdapter().folderSave(folder);
        }
        readFolderPermissions(root.element("permissions"), folder);
    }

    private void readFolderPermissions(Element element, FolderEntity folder) {
        for (Element permElement : (List<Element>) element.elements("permission")) {
            GroupEntity group = getDao().getGroupDao().getByName(permElement.elementText("group"));
            FolderPermissionType permission = FolderPermissionType.valueOf(permElement.elementText("permissionType"));
            getBusiness().getFolderPermissionBusiness().setPermission(folder, group, permission);
        }
    }
}
