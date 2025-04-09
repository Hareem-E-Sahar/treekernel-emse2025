package org.jadira.repositorysnapshot;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;
import org.jadira.dependencynavigator.gui.ProgressMeter;
import org.jadira.dependencynavigator.implementations.zip.ZipFileRepository;
import org.jadira.dependencynavigator.implementations.zip.ZipFileWorkspace;

public class RepositorySnapshot {

    private final File snapshotFile;

    private final File repositoryRoot;

    private final File workspace;

    public RepositorySnapshot(File repositoryRoot, File snapshotFile, File workspace) {
        this.repositoryRoot = repositoryRoot;
        this.snapshotFile = snapshotFile;
        this.workspace = workspace;
    }

    public void createSnapshot(ProgressMeter progressMeter) throws ZipException, IOException {
        snapshotFile.delete();
        snapshotFile.getParentFile().mkdirs();
        snapshotFile.createNewFile();
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(snapshotFile)));
        out.setLevel(Deflater.BEST_COMPRESSION);
        out.setMethod(ZipOutputStream.DEFLATED);
        HierarchyFilter workspaceFilter = new HierarchyFilter() {

            @Override
            public boolean includeDirectory(File directory) throws IOException {
                if (directory.getName().equals(".metadata")) {
                    return false;
                }
                if (directory.getCanonicalPath().equals(repositoryRoot.getCanonicalPath())) {
                    return false;
                }
                return true;
            }
        };
        addHeirarchy(workspace, out, ZipFileWorkspace.PREFIX_WORKSPACE, workspaceFilter, progressMeter);
        HierarchyFilter repositoryFilter = new HierarchyFilter() {

            @Override
            public boolean includeFile(File file) throws IOException {
                if (!file.getName().endsWith(".pom")) {
                    return false;
                }
                if (file.getName().matches("^.*-\\d{8}\\.\\d{6}-\\d+\\.pom$")) {
                    return false;
                }
                return true;
            }
        };
        addHeirarchy(repositoryRoot, out, ZipFileRepository.PREFIX_REPOSITORY, repositoryFilter, progressMeter);
        out.close();
    }

    private void addHeirarchy(File directory, ZipOutputStream out, String repoPath, HierarchyFilter filter, ProgressMeter progressMeter) throws IOException {
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory() && filter.includeDirectory(file)) {
                progressMeter.scanning(file);
                addHeirarchy(file, out, repoPath + "/" + file.getName(), filter, progressMeter);
            } else {
                if (filter.includeFile(file)) {
                    progressMeter.adding(file);
                    addEntry(file, repoPath + "/" + file.getName(), out);
                }
            }
        }
    }

    private class HierarchyFilter {

        public boolean includeFile(File file) throws IOException {
            return file.getName().equals("pom.xml");
        }

        public boolean includeDirectory(File directory) throws IOException {
            return true;
        }
    }

    private void addEntry(File target, String repoPath, ZipOutputStream out) throws IOException {
        ZipEntry entry = new ZipEntry(repoPath);
        int fileLength = (int) target.length();
        FileInputStream fis = new FileInputStream(target);
        byte[] wholeFile = new byte[fileLength];
        fis.read(wholeFile, 0, fileLength);
        fis.close();
        out.putNextEntry(entry);
        out.write(wholeFile, 0, fileLength);
        out.closeEntry();
    }
}
