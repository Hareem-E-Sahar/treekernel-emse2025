package netxrv.jnlp.jardiff;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import netxrv.jnlp.DiffPatcher;
import netxrv.jnlp.tardiff.TarDiffPatcher;
import netxrv.jnlp.util.InputStreamUtil;
import netxrv.jnlp.util.StringUtil;

/**
 * JarDiff is able to create a jar file containing the delta between two
 * jar files (old and new). The delta jar file can then be applied to the
 * old jar file to reconstruct the new jar file.
 * <p>
 * Refer to the JNLP spec for details on how this is done.
 *
 * @version 1.11, 06/26/03
 */
public class JarDiffPatcher extends DiffPatcher {

    private static Logger logger = Logger.getLogger(JarDiffPatcher.class.getName());

    private static ZipFile getJarFile(String jarPath) throws IOException {
        File file = new File(jarPath);
        return new ZipFile(file);
    }

    public synchronized void applyRecursiveTarDiffPatch(ZipFile oldJar, ZipFile jarDiff, ZipEntry entry, Patcher.PatchDelegate delegate, JarOutputStream jos, String tarName) throws Exception {
        ZipEntry oldEntry = oldJar.getEntry(tarName);
        logger.info("old entry " + oldEntry.getName() + " tarName:" + tarName);
        String tarExt = null;
        if (tarName.endsWith(".tar.gz")) tarExt = ".tar.gz"; else tarExt = ".tar";
        InputStream oldJIS = null;
        InputStream jardiffIS = null;
        File oldEntryTempFile = null;
        File entryTarDiff = null;
        try {
            oldJIS = oldJar.getInputStream(oldEntry);
            oldEntryTempFile = InputStreamUtil.writeToFile(oldJIS, null, tarExt, null);
            jardiffIS = jarDiff.getInputStream(entry);
            entryTarDiff = InputStreamUtil.writeToFile(jardiffIS, null, tarExt, null);
        } finally {
            if (oldJIS != null) oldJIS.close();
            if (jardiffIS != null) jardiffIS.close();
        }
        File tempEntryFile = File.createTempFile("jdp", tarExt);
        tempEntryFile.deleteOnExit();
        FileOutputStream fileOutputStream = new FileOutputStream(tempEntryFile);
        BufferedOutputStream entryOutputStream = new BufferedOutputStream(fileOutputStream, 2048);
        (new TarDiffPatcher()).applyPatch(delegate, oldEntryTempFile.getAbsolutePath(), entryTarDiff.getAbsolutePath(), entryOutputStream);
        entryOutputStream.close();
        FileInputStream entryInputStream = new FileInputStream(tempEntryFile);
        writeEntry(jos, new ZipEntry(tarName), entryInputStream);
        entryInputStream.close();
        tempEntryFile.delete();
        oldEntryTempFile.delete();
        entryTarDiff.delete();
    }

    public synchronized void applyRecursiveJarDiffPatch(ZipFile oldJar, ZipFile jarDiff, ZipEntry entry, Patcher.PatchDelegate delegate, JarOutputStream jos, String jarName) throws Exception {
        ZipEntry oldEntry = oldJar.getEntry(jarName);
        File oldEntryTempFile = InputStreamUtil.writeToFile(oldJar.getInputStream(oldEntry), null, ".jar", null);
        File entryJarDiff = InputStreamUtil.writeToFile(jarDiff.getInputStream(entry), null, ".jar", null);
        ZipFile oldEntryJar = new ZipFile(oldEntryTempFile);
        File tempEntryFile = File.createTempFile("jdp", ".jar");
        tempEntryFile.deleteOnExit();
        FileOutputStream entryOutputStream = new FileOutputStream(tempEntryFile);
        applyPatch(delegate, oldEntryTempFile.getAbsolutePath(), entryJarDiff.getAbsolutePath(), entryOutputStream);
        entryOutputStream.close();
        FileInputStream entryInputStream = new FileInputStream(tempEntryFile);
        writeEntry(jos, new ZipEntry(jarName), entryInputStream);
        entryInputStream.close();
        tempEntryFile.delete();
        oldEntryJar.close();
        oldEntryTempFile.delete();
        entryJarDiff.delete();
    }

    public synchronized void applyPatch(Patcher.PatchDelegate delegate, String oldJarPath, String jarDiffPath, OutputStream result) throws Exception {
        logger.info("Apply Jardiff to oldJarPath [" + oldJarPath + "] with jarDiff located at [" + jarDiffPath + "]");
        ZipFile jarDiff = getJarFile(jarDiffPath);
        Set<String> ignoreSet = new HashSet<String>();
        Map<String, String> renameMap = new HashMap<String, String>();
        determineNameMapping(jarDiff, ignoreSet, renameMap);
        Set<String> oldjarNames = new HashSet<String>();
        ZipFile oldJar = getJarFile(oldJarPath);
        Enumeration<? extends ZipEntry> oldEntries = oldJar.entries();
        if (oldEntries != null) {
            while (oldEntries.hasMoreElements()) {
                oldjarNames.add(oldEntries.nextElement().getName());
            }
        }
        Object[] keys = renameMap.keySet().toArray();
        double size = oldjarNames.size() + keys.length + jarDiff.size();
        double currentEntry = 0;
        oldjarNames.removeAll(ignoreSet);
        size -= ignoreSet.size();
        Enumeration<? extends ZipEntry> entries = jarDiff.entries();
        JarOutputStream jos = null;
        if (result instanceof JarOutputStream) {
            jos = (JarOutputStream) result;
        } else {
            jos = new JarOutputStream(result);
        }
        if (entries != null) {
            int jarDiffExtLength = JARDIFF_EXT.length();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (INDEX_NAME.equals(entryName)) {
                    size--;
                    continue;
                }
                if (entryName.endsWith(JARDIFF_EXT)) {
                    int entryNameLength = entryName.length();
                    String frontPart = entryName.substring(0, entryNameLength - jarDiffExtLength);
                    String jarName = frontPart + JAR_EXT;
                    ZipEntry oldEntry = oldJar.getEntry(jarName);
                    applyRecursiveJarDiffPatch(oldJar, jarDiff, entry, delegate, jos, jarName);
                    oldjarNames.remove(jarName);
                    size--;
                } else if (entryName.endsWith(TARDIFF_GZ_EXT) || entryName.endsWith(TARDIFF_EXT)) {
                    int entryNameLength = entryName.length();
                    String tarDiffExt = null;
                    String tarExt = null;
                    if (entryName.endsWith(TARDIFF_GZ_EXT)) {
                        tarDiffExt = TARDIFF_GZ_EXT;
                        tarExt = TAR_GZ_EXT;
                    } else {
                        tarDiffExt = TARDIFF_EXT;
                        tarExt = TAR_EXT;
                    }
                    String frontPart = entryName.substring(0, entryNameLength - tarDiffExt.length());
                    String tarName = frontPart + tarExt;
                    applyRecursiveTarDiffPatch(oldJar, jarDiff, entry, delegate, jos, tarName);
                    oldjarNames.remove(tarName);
                    size--;
                } else {
                    updateDelegate(delegate, currentEntry, size);
                    currentEntry++;
                    writeEntry(jos, entry, jarDiff.getInputStream(entry));
                    boolean wasInOld = oldjarNames.remove(entryName);
                    if (wasInOld) size--;
                }
            }
        }
        for (int j = 0; j < keys.length; j++) {
            String newName = (String) keys[j];
            String oldName = (String) renameMap.get(newName);
            ZipEntry oldEntry = oldJar.getEntry(oldName);
            if (oldEntry == null) {
                String moveCmd = MOVE_COMMAND + oldName + " " + newName;
                handleException("jardiff.error.badmove", moveCmd);
            }
            ZipEntry newEntry = createNewEntry(newName, oldEntry);
            updateDelegate(delegate, currentEntry, size);
            currentEntry++;
            writeEntry(jos, newEntry, oldJar.getInputStream(oldEntry));
            boolean wasInOld = oldjarNames.remove(oldName);
            if (wasInOld) size--;
        }
        for (String name : oldjarNames) {
            ZipEntry oldEntry = oldJar.getEntry(name);
            updateDelegate(delegate, currentEntry, size);
            currentEntry++;
            writeEntry(jos, oldEntry, oldJar.getInputStream(oldEntry));
        }
        updateDelegate(delegate, currentEntry, size);
        jos.finish();
    }

    private JarEntry createNewEntry(String newName, ZipEntry oldEntry) {
        JarEntry newEntry = new JarEntry(newName);
        newEntry.setTime(oldEntry.getTime());
        newEntry.setSize(oldEntry.getSize());
        newEntry.setCompressedSize(oldEntry.getCompressedSize());
        newEntry.setCrc(oldEntry.getCrc());
        newEntry.setMethod(oldEntry.getMethod());
        newEntry.setExtra(oldEntry.getExtra());
        newEntry.setComment(oldEntry.getComment());
        return newEntry;
    }

    private void determineNameMapping(ZipFile jarDiff, Set<String> ignoreSet, Map<String, String> renameMap) throws IOException {
        InputStream is = jarDiff.getInputStream(jarDiff.getEntry(INDEX_NAME));
        if (is == null) {
            handleException("jardiff.error.noindex", null);
        }
        LineNumberReader indexReader = new LineNumberReader(new InputStreamReader(is, "UTF-8"));
        String line = indexReader.readLine();
        if (line == null || !line.equals(VERSION_HEADER)) {
            handleException("jardiff.error.badheader", line);
        }
        while ((line = indexReader.readLine()) != null) {
            if (line.startsWith(REMOVE_COMMAND)) {
                List<String> sub = StringUtil.getSubpaths(line.substring(REMOVE_COMMAND.length()));
                if (sub.size() != 1) {
                    handleException("jardiff.error.badremove", line);
                }
                ignoreSet.add(sub.get(0));
            } else if (line.startsWith(MOVE_COMMAND)) {
                List<String> sub = StringUtil.getSubpaths(line.substring(MOVE_COMMAND.length()));
                if (sub.size() != 2) {
                    handleException("jardiff.error.badmove", line);
                }
                if (renameMap.put(sub.get(1), sub.get(0)) != null) {
                    handleException("jardiff.error.badmove", line);
                }
            } else if (line.length() > 0) {
                handleException("jardiff.error.badcommand", line);
            }
        }
    }

    private void writeEntry(JarOutputStream jos, ZipEntry entry, InputStream data) throws IOException {
        jos.putNextEntry(entry);
        byte[] newBytes = InputStreamUtil.createReadBuffer();
        int size = data.read(newBytes);
        while (size != -1) {
            jos.write(newBytes, 0, size);
            size = data.read(newBytes);
        }
        data.close();
    }
}
