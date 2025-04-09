package fitnesse.revisioncontrol;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import fitnesse.util.StreamReader;
import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.NoSuchVersionException;
import fitnesse.wiki.PageData;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;

public class ZipFileRevisionController implements RevisionController {

    public Collection<VersionInfo> history(FileSystemPage page) throws Exception {
        File dir = new File(page.getFileSystemPath());
        File[] files = dir.listFiles();
        Set<VersionInfo> versions = new HashSet<VersionInfo>();
        if (files != null) {
            for (File file : files) {
                if (isVersionFile(file)) versions.add(new VersionInfo(makeVersionName(file)));
            }
        }
        return versions;
    }

    public VersionInfo makeVersion(FileSystemPage page, PageData data) throws Exception {
        String dirPath = page.getFileSystemPath();
        Set filesToZip = getFilesToZip(dirPath);
        VersionInfo version = makeVersionInfo(page, data);
        if (filesToZip.size() == 0) return new VersionInfo("first_commit", "", new Date());
        String filename = makeVersionFileName(page, version.getName());
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(filename));
        for (Iterator iterator = filesToZip.iterator(); iterator.hasNext(); ) addToZip((File) iterator.next(), zos);
        zos.finish();
        zos.close();
        return new VersionInfo(version.getName());
    }

    private VersionInfo makeVersionInfo(FileSystemPage page, PageData data) throws Exception {
        Date time = data.getProperties().getLastModificationTime();
        String versionName = VersionInfo.nextId() + "-" + ZipFileRevisionController.dateFormat().format(time);
        String user = data.getAttribute(WikiPage.LAST_MODIFYING_USER);
        if (user != null && !"".equals(user)) versionName = user + "-" + versionName;
        return new VersionInfo(versionName, user, time);
    }

    public static SimpleDateFormat dateFormat() {
        return new SimpleDateFormat("yyyyMMddHHmmss");
    }

    private String makeVersionFileName(FileSystemPage page, String name) throws Exception {
        return page.getFileSystemPath() + "/" + name + ".zip";
    }

    public void removeVersion(FileSystemPage page, String versionName) throws Exception {
        String versionFileName = makeVersionFileName(page, versionName);
        File versionFile = new File(versionFileName);
        versionFile.delete();
    }

    private String makeVersionName(File file) {
        String name = file.getName();
        return name.substring(0, name.length() - 4);
    }

    private boolean isVersionFile(File file) {
        return !file.isDirectory() && Pattern.matches("(\\S+)?\\d+\\.zip", file.getName());
    }

    private Set getFilesToZip(String dirPath) {
        Set<File> filesToZip = new HashSet<File>();
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null) return filesToZip;
        for (File file : files) {
            if (!(isVersionFile(file) || file.isDirectory())) filesToZip.add(file);
        }
        return filesToZip;
    }

    private void addToZip(File file, ZipOutputStream zos) throws IOException {
        ZipEntry entry = new ZipEntry(file.getName());
        zos.putNextEntry(entry);
        FileInputStream is = new FileInputStream(file);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        is.read(bytes);
        is.close();
        zos.write(bytes, 0, size);
    }

    public PageData getRevisionData(FileSystemPage page, String label) throws Exception {
        String filename = page.getFileSystemPath() + "/" + label + ".zip";
        File file = new File(filename);
        if (!file.exists()) throw new NoSuchVersionException("There is no version '" + label + "'");
        PageData data = new PageData(page);
        ZipFile zipFile = new ZipFile(file);
        loadVersionContent(zipFile, data);
        loadVersionAttributes(zipFile, data);
        data.addVersions(history(page));
        zipFile.close();
        return data;
    }

    private void loadVersionContent(ZipFile zipFile, PageData data) throws Exception {
        String content = "";
        ZipEntry contentEntry = zipFile.getEntry("content.txt");
        if (contentEntry != null) {
            InputStream contentIS = zipFile.getInputStream(contentEntry);
            StreamReader reader = new StreamReader(contentIS);
            content = reader.read((int) contentEntry.getSize());
            reader.close();
        }
        data.setContent(content);
    }

    private void loadVersionAttributes(ZipFile zipFile, PageData data) throws Exception {
        ZipEntry attributes = zipFile.getEntry("properties.xml");
        if (attributes != null) {
            InputStream attributeIS = zipFile.getInputStream(attributes);
            WikiPageProperties props = new WikiPageProperties(attributeIS);
            attributeIS.close();
            data.setProperties(props);
        }
    }

    public void commit(FileSystemPage page, PageData data) throws Exception {
    }

    public void removeChildPage(FileSystemPage page, String name) {
    }

    public State add(File[] filePaths) throws RevisionControlException {
        return null;
    }

    public State checkState(File[] filePaths) throws RevisionControlException {
        return null;
    }

    public State checkin(File[] filePaths) throws RevisionControlException {
        return null;
    }

    public State checkout(File[] filePaths) throws RevisionControlException {
        return null;
    }

    public State delete(File[] filePaths) throws RevisionControlException {
        return null;
    }

    public State revert(File[] filePaths) throws RevisionControlException {
        return null;
    }

    public State update(File[] filePaths) throws RevisionControlException {
        return null;
    }

    public State getState(String state) {
        return null;
    }
}
