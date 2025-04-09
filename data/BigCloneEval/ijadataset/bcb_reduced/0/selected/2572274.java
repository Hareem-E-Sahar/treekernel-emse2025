package net.sourceforge.jbackupfw.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.sourceforge.jbackupfw.core.data.BackUpInfoFile;
import net.sourceforge.jbackupfw.core.data.BackUpInfoFileGroup;
import net.sourceforge.jbackupfw.core.data.BackupException;

/**
 * This class performs the update of a specific archive, it first cheks wich files
 * are to be updated and then preforms the update itself by repacking the files
 * that do not need to be updated, and back up the files that need to be, also in
 * the process it rejects the old version of those to prevent duplicates
 * 
 * @author Boris Horvat
 */
public class Update {

    /** this is constant that controls the size of the buffer */
    private static final byte[] BUFFER = new byte[1024];

    /**
     *  This is a method that is used updates the files that were selected for an update,
     *  it does so by separating each file from the list and calling other methods to perform
     *  an update of a single file
     *
     *  @param fileGroup - contains the information about the files that are to be updated
     *  @param zos - represents an object that is used to write date into stream
     */
    public void execute(BackUpInfoFileGroup fileGroup, ZipOutputStream zos) {
        for (int i = 0; i < fileGroup.getFileList().size(); i++) {
            updateFile(fileGroup.getFileList().get(i), zos);
        }
    }

    /**
     * This method cheks if the file should be updated or not
     *
     * @param updateFiles - informations about all of the files that are to be updated up
     * @param entryName - the name of the file that is being checked
     *
     * @return true if the file is to be updated, false otherwise
     */
    public boolean isToUpdate(BackUpInfoFileGroup updateFiles, String entryName) {
        for (int i = 0; i < updateFiles.getFileList().size(); i++) {
            if (updateFiles.getFileList().get(i).getId().equals(entryName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method is used to updated informations about the backed up files
     *
     * @param backedUpFiles - informations about all of the files that were previously backed up
     * @param updateFiles - informations about all of the files that are to be updated up
     *
     * @return object of type BackUpInfoFileGroup that contains updated informations about backed up files
     */
    public BackUpInfoFileGroup updateInformationFile(BackUpInfoFileGroup backedUpFiles, BackUpInfoFileGroup updateFiles) {
        for (int i = 0; i < backedUpFiles.getFileList().size(); i++) {
            for (int j = 0; j < updateFiles.getFileList().size(); j++) {
                if (backedUpFiles.getFileList().get(i).getId().equals(updateFiles.getFileList().get(j).getId())) {
                    backedUpFiles.getFileList().get(i).setSize(updateFiles.getFileList().get(j).getSize());
                    backedUpFiles.setSize(backedUpFiles.getSize() + updateFiles.getFileList().get(j).getSize() - backedUpFiles.getFileList().get(i).getSize());
                }
            }
        }
        return backedUpFiles;
    }

    /**
     * This is a private method that updates a single file
     *
     *  @param fileInfo - contains the information about the file that is to be updated
     *  @param zos - represents an object that is used to write date into stream
     */
    private void updateFile(BackUpInfoFile fileInfo, ZipOutputStream zos) {
        try {
            int bytesIn = 0;
            File file = new File(fileInfo.getPath() + fileInfo.getName() + "." + fileInfo.getType());
            FileInputStream fis = new FileInputStream(file);
            ZipEntry anEntry = new ZipEntry(fileInfo.getId());
            zos.putNextEntry(anEntry);
            while ((bytesIn = fis.read(BUFFER)) != -1) {
                zos.write(BUFFER, 0, bytesIn);
            }
            fileInfo.setSize(file.length());
            fis.close();
        } catch (IOException e) {
            throw new BackupException(e.getMessage());
        }
    }
}
