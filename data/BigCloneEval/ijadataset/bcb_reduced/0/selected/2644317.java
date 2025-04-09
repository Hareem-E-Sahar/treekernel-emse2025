package meta.codeanywhere.manager;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import meta.codeanywhere.bean.SourceFile;
import meta.codeanywhere.bean.User;
import meta.codeanywhere.dao.DAOFactory;
import meta.codeanywhere.dao.SourceFileDAO;

/**
 * The FileManager process all the request to create or open a file
 * from the database file system.
 * @author Biao Zhang
 * @version 11/10/2006
 */
public class FileManager {

    private static FileManager manager = null;

    static {
        manager = new FileManager();
    }

    public static FileManager getManager() {
        return manager;
    }

    private SourceFileDAO fileDAO = null;

    private FileManager() {
    }

    public SourceFile getFileByName(String fileName) {
        fileDAO = DAOFactory.DEFAULT.getSourceFileDAO();
        return fileDAO.getByFileName(fileName);
    }

    public SourceFile[] getFilesByOwner(User owner) {
        fileDAO = DAOFactory.DEFAULT.getSourceFileDAO();
        return fileDAO.getByOwner(owner).toArray(new SourceFile[0]);
    }

    public SourceFile saveFile(String fileName, User owner, String content) {
        fileDAO = DAOFactory.DEFAULT.getSourceFileDAO();
        SourceFile file = new SourceFile();
        file.setFileName(fileName);
        file.setOwner(owner);
        file.setStringData(content);
        fileDAO.makePersistent(file);
        return file;
    }

    public void outputZipPackage(String[] fileNames, byte[][] contents, OutputStream outputStream) {
        if (fileNames == null || contents == null || fileNames.length != contents.length) {
            return;
        }
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(outputStream));
        for (int i = 0; i < fileNames.length; i++) {
            ZipEntry zipEntry = new ZipEntry(fileNames[i]);
            try {
                out.putNextEntry(zipEntry);
                out.write(contents[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
