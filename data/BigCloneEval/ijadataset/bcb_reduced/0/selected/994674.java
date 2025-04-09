package net.sourceforge.javabits.tool.zip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import net.sourceforge.javabits.error.DefaultProblem;
import net.sourceforge.javabits.error.ErrorHandler;
import net.sourceforge.javabits.error.ErrorHandler.Severity;
import net.sourceforge.javabits.io.FileFunctions;
import net.sourceforge.javabits.io.Files;
import net.sourceforge.javabits.io.PatternFileSelector;
import net.sourceforge.javabits.lang.Objects;
import net.sourceforge.javabits.task.AbstractFileTask;
import net.sourceforge.javabits.util.Sets;
import org.codehaus.plexus.util.IOUtil;

public class ZipExpandTask extends AbstractFileTask {

    private File archive;

    private File targetDirectory;

    private Map<File, File> fileMap;

    public ZipExpandTask(File baseDirectory, File archive, File targetDirectory, Map<File, File> fileMap) {
        super("Expanding archive '{this.localFile(this.archive)}'.", baseDirectory);
        this.archive = archive;
        this.fileMap = new HashMap<File, File>(fileMap);
        this.targetDirectory = targetDirectory;
    }

    @Override
    protected void executeTask(ErrorHandler err) throws Exception {
        File targetDirectory = Files.resolve(getBaseDirectory(), getTargetDirectory());
        PatternFileSelector existingFileSelector = new PatternFileSelector(targetDirectory, Collections.singleton("**/*"), null, null);
        Set<File> obsoleteFileSet = new HashSet<File>(existingFileSelector.getRelativeFileSet(err));
        long timestamp = getArchive().lastModified();
        Map<File, File> fileMap = getFileMap();
        ZipFile zipFile = null;
        try {
            if (err.isEnabled(Severity.DEBUG)) {
                err.debug("Processing zip file '%s'.", getArchive());
            }
            zipFile = new ZipFile(getArchive());
            for (Enumeration<? extends ZipEntry> i = zipFile.entries(); i.hasMoreElements(); ) {
                ZipEntry zipEntry = i.nextElement();
                File entryFile = new File(zipEntry.getName());
                if (!zipEntry.isDirectory() && fileMap.containsKey(entryFile)) {
                    File target = fileMap.get(entryFile);
                    File normalizedTarget = Files.child(targetDirectory, target);
                    if (normalizedTarget == null) {
                        err.warn(new DefaultProblem(null, Severity.WARNING, null, "Skipping illegal target '%s' for entry '%s'.", target, zipEntry.getName()));
                    } else {
                        try {
                            File file = new File(targetDirectory, normalizedTarget.getPath());
                            extractFile(err, zipFile, zipEntry, file);
                            obsoleteFileSet.remove(normalizedTarget);
                        } catch (IOException e) {
                            err.error(e);
                        }
                    }
                }
            }
        } catch (ZipException e) {
            err.error(e);
        } catch (IOException e) {
            err.error(e);
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    err.error(e);
                }
            }
        }
        List<File> obsoleteFileList = new ArrayList<File>(obsoleteFileSet);
        Collections.sort(obsoleteFileList, Objects.reverseComparator());
        for (File file : obsoleteFileList) {
            File f = new File(targetDirectory, file.getPath());
            if (f.isDirectory() && f.listFiles().length > 0) {
                f.setLastModified(timestamp);
            } else {
                if (err.isEnabled(Severity.DEBUG)) {
                    err.debug(String.format("Removing '%s'.", file));
                }
                f.delete();
            }
        }
    }

    protected void extractFile(ErrorHandler err, ZipFile file, ZipEntry entry, File targetFile) throws IOException {
        if (err.isEnabled(Severity.DEBUG)) {
            err.debug("Extracting '%s' to '%s'.", entry.getName(), targetFile);
        }
        if (entry.isDirectory()) {
            targetFile.mkdirs();
        } else {
            targetFile.getParentFile().mkdirs();
            FileOutputStream out = null;
            InputStream in = null;
            try {
                out = new FileOutputStream(targetFile);
                in = file.getInputStream(entry);
                IOUtil.copy(in, out);
            } finally {
                Objects.close(out);
                Objects.close(in);
            }
        }
    }

    public File getArchive() {
        return this.archive;
    }

    public void setArchive(File archive) {
        this.archive = archive;
    }

    public Map<File, File> getFileMap() {
        return this.fileMap;
    }

    public void setFileMap(Map<File, File> fileMap) {
        this.fileMap = fileMap;
    }

    public File getTargetDirectory() {
        return this.targetDirectory;
    }

    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    /**
     * @see net.sourceforge.javabits.task.AbstractFileTask#getSourceCollection()
     */
    @Override
    public Collection<File> getSourceCollection() {
        return Collections.singleton(archive);
    }

    /**
     * @see net.sourceforge.javabits.task.AbstractFileTask#getTargetCollection()
     */
    @Override
    public Collection<File> getTargetCollection() {
        return Sets.evaluate(fileMap.values(), FileFunctions.resolve(targetDirectory));
    }
}
