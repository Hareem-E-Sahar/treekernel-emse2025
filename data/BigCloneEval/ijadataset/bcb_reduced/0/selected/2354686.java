package org.proteusframework.platformservice.persistence.packaging;

import org.proteusframework.platformservice.persistence.api.IPackagingVisitor;
import org.proteusframework.platformservice.persistence.api.IVFSFile;
import org.proteusframework.platformservice.persistence.api.IVFSManager;
import org.proteusframework.platformservice.persistence.api.IVFSVisitor;
import org.proteusframework.platformservice.persistence.vfs.FilePath;
import java.io.*;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

final class VFSFileZipSerializer extends ZipSerializer implements IVFSVisitor {

    private static final Logger logger = Logger.getLogger(VFSFileZipSerializer.class.getCanonicalName());

    VFSFileZipSerializer(ZipOutputStream zipOutputStream) {
        super(zipOutputStream);
    }

    VFSFileZipSerializer(ZipFile zipFile) {
        super(zipFile);
    }

    @Override
    public void visit(IVFSManager vfsManager) throws IOException {
        if (getDirection() == IPackagingVisitor.DIRECTION_OUT) {
            List<IVFSFile> files = vfsManager.getFiles(new FilePath("/"));
            for (IVFSFile vfsFile : files) {
                try {
                    ZipEntry ze = new ZipEntry(VFS_ENTRY + getRecordCount());
                    ze.setComment(vfsFile.getFilePath().toString());
                    FileInputStream fis = new FileInputStream(vfsFile.getFile());
                    zipStream.putNextEntry(ze);
                    while (true) {
                        int datum = fis.read();
                        if (datum == -1) {
                            break;
                        }
                        zipStream.write(datum);
                    }
                    zipStream.closeEntry();
                    fis.close();
                    incrementRecordCount();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Enumeration<? extends ZipEntry> zipFiles = this.zipFile.entries();
            while (zipFiles.hasMoreElements()) {
                ZipEntry entry = zipFiles.nextElement();
                if (entry.getName().startsWith(IVFSVisitor.VFS_ENTRY)) {
                    FilePath vfsPath = new FilePath(entry.getComment());
                    logger.info("Retrieved VFS path of " + vfsPath.toString());
                    IVFSFile vfsFile = vfsManager.createFile(vfsPath);
                    FileOutputStream fos = new FileOutputStream(vfsFile.getAbsolutePath());
                    InputStream is = zipFile.getInputStream(entry);
                    while (true) {
                        int datum = is.read();
                        if (datum == -1) {
                            break;
                        }
                        fos.write(datum);
                    }
                    is.close();
                    fos.flush();
                    fos.close();
                    logger.info("Wrote VFS file to absolute path of " + vfsFile.getAbsolutePath());
                }
            }
        }
    }
}
