package sk.tuke.ess.editor.base.helpers;

import sk.tuke.ess.editor.base.components.logger.Logger;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: zladovan
 * Date: 31.1.2012
 * Time: 22:22
 * To change this template use File | Settings | File Templates.
 */
public class ZipUpdater {

    private File zipFile;

    private Map<String, byte[]> entryMap;

    public ZipUpdater(File zipFile) throws IOException {
        this.zipFile = zipFile;
        loadEntries();
    }

    private void loadEntries() throws IOException {
        entryMap = new HashMap<String, byte[]>();
        ZipInputStream zipInputStream = null;
        if (!zipFile.exists()) return;
        try {
            zipInputStream = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (!zipEntry.isDirectory()) {
                    entryMap.put(zipEntry.getName(), FileHelper.readInputStreamToByteArray(zipInputStream));
                }
            }
            zipInputStream.close();
        } catch (ZipException e) {
        } finally {
            if (zipInputStream != null) {
                zipInputStream.close();
            }
        }
    }

    public void addFilesWithCommonDirReplace(String commonDirReplacement, URI[] files) {
        String commonDirPath = FileHelper.getCommonDirPath(files);
        for (URI uri : files) {
            try {
                addEntry(uri.toString().replace(commonDirPath, commonDirReplacement), FileHelper.readURIToByteArray(uri));
            } catch (IOException e) {
                handleAddURIException(e, uri);
            }
        }
    }

    public void addFiles(String path, URI[] files) {
        for (URI uri : files) {
            try {
                addEntry(path + "/" + FileHelper.getFileName(uri), FileHelper.readURIToByteArray(uri));
            } catch (IOException e) {
                handleAddURIException(e, uri);
            }
        }
    }

    private void handleAddURIException(Exception e, URI uri) {
        Logger.getLogger().addError("Nepodarilo sa pridať súbor <b>%s</b> do archívu <b>%s</b>", uri.toString(), zipFile.getPath());
    }

    public void addEntry(String name, InputStream inputStream) throws IOException {
        addEntry(name, FileHelper.readInputStreamToByteArray(inputStream));
    }

    public void addEntry(String name, byte[] entryData) {
        entryMap.put(name, entryData);
    }

    public String addEntryWithoutRewritingExisting(String name, InputStream inputStream) throws IOException {
        return addEntryWithoutRewritingExisting(name, FileHelper.readInputStreamToByteArray(inputStream));
    }

    public String addEntryWithoutRewritingExisting(String name, byte[] entryData) {
        name = findUniqueName(name);
        entryMap.put(name, entryData);
        return name;
    }

    private String findUniqueName(String baseName) {
        if (entryMap.get(baseName) == null) return baseName;
        String ext = FileHelper.getFileExtension(baseName);
        String baseNameWithoutExt = baseName.replace(".".concat(ext), "");
        return findUniqueName(baseNameWithoutExt.concat(Long.toString(System.currentTimeMillis()).concat(".").concat(ext)));
    }

    public void saveChanges() throws IOException {
        zipFile.delete();
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
        for (String entryName : entryMap.keySet()) {
            zipOutputStream.putNextEntry(new ZipEntry(entryName));
            zipOutputStream.write(entryMap.get(entryName));
            zipOutputStream.closeEntry();
        }
        zipOutputStream.flush();
        try {
            zipOutputStream.close();
        } catch (ZipException e) {
        }
    }

    public List<String> removeEntries(String regexPattern, URI[] exceptions) {
        Pattern pattern = Pattern.compile(regexPattern);
        Set<String> entryNameSet = new HashSet<String>(entryMap.keySet());
        List<String> removedEntries = new ArrayList<String>();
        for (String name : entryNameSet) {
            if (pattern.matcher(name).matches() && !isInExceptions(name, exceptions)) {
                entryMap.remove(name);
                removedEntries.add(name);
            }
        }
        return removedEntries;
    }

    private boolean isInExceptions(String name, URI[] exceptions) {
        for (URI uri : exceptions) {
            if (uri.toString().endsWith(name)) return true;
        }
        return false;
    }

    public void moveEntriesTo(String regexPattern, ZipUpdater destZipUpdater) {
        copyEntriesTo(regexPattern, destZipUpdater);
        removeEntries(regexPattern, new URI[0]);
    }

    public void copyEntriesTo(String regexPattern, ZipUpdater destZipUpdater) {
        Pattern pattern = Pattern.compile(regexPattern);
        for (String name : entryMap.keySet()) {
            if (pattern.matcher(name).matches()) {
                destZipUpdater.addEntry(name, entryMap.get(name));
            }
        }
    }
}
