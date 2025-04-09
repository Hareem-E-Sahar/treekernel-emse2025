package com.hdmm.mediaserver.filemanagement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.primefaces.model.UploadedFile;
import com.hdmm.mediaserver.data.Const;
import com.hdmm.mediaserver.development.DevLogger;
import com.hdmm.mediaserver.entities.AudioData;
import com.hdmm.mediaserver.entities.VideoData;
import com.hdmm.mediaserver.metadata.MetaManagerAudio;
import com.hdmm.mediaserver.view.FileTableView;
import com.hdmm.mediaserver.view.FileTableView.Document;

/**
 * FileManager.java
 * Klasse zum Verwalten der Arbeit mit dem Dateisystem 
 * in jeder erdenklichen Art.
 * 
 * @author Hendrik Guelz (20966179) & Daniel Schnelle (20966593)
 * @version 25
 * @date 20.11.2011
 * @created 27.10.2011
 */
public class FileManager {

    @SuppressWarnings("unused")
    private static DevLogger logger = new DevLogger(FileManager.class);

    private MetaManagerAudio metaManager;

    /**
	 * L�scht eine Datei oder einen Ordner.
	 * @author Daniel Schnelle (20966593)
	 * @param path
	 *            - der Pfad des Ordners, der Datei die geloescht werden soll.
	 * @return true - falls die delete-Operation erfolgreich war.
	 */
    public static boolean delete(String path) {
        return delete(new File(path));
    }

    /**
	 * L�scht eine Datei oder einen Ordner (inkl. Inhalt).
	 * @author Daniel Schnelle (20966593)
	 * @param file
	 *            - der zu loeschende Ordner, oder Datei.
	 * @return true - falls die delete-Operation erfolgreich war.
	 */
    public static boolean delete(File file) {
        if (file.exists() && !file.equals(new File(Const.AUDIO_DIR)) && !file.equals(new File(Const.VIDEO_DIR)) && !file.equals(new File(Const.PLAYLISTS_DIR)) && !file.equals(new File(Const.TEMP_DIR)) && !file.equals(new File(Const.UPLOAD_DIR))) {
            if (file.isFile()) {
                return file.delete();
            } else {
                if (file.list().length <= 0) {
                    return file.delete();
                } else {
                    boolean retVal = true;
                    for (File tmp : file.listFiles()) {
                        retVal = retVal && delete(tmp);
                    }
                    return retVal && file.delete();
                }
            }
        } else {
            return false;
        }
    }

    /**
	 * Erstellt einen Ordner.
	 * @author Hendrik Guelz (20966179)
	 * @param dir
	 *            - der Pfad des zu erstellenden Ordners.
	 * @return true - falls erfolgreich.
	 */
    public static boolean addDir(String dir) {
        return addDir(new File(dir));
    }

    /**
	 * Erstellt einen Ordner. Es werden alle �berordner, die nicht vorhanden
	 * sind, ebenfalls erstellt.
	 * @author Hendrik Guelz (20966179)
	 * @param file
	 *            - der zu erstellende Ordner.
	 * @return true - falls erfolgreich.
	 */
    public static boolean addDir(File file) {
        if (!file.exists()) {
            return file.mkdirs();
        } else {
            return false;
        }
    }

    /**
	 * Umbenennen einer Datei oder eines Ordners.
	 * @author Hendrik Guelz (20966179)
	 * @param newName
	 *            - der neue Name des Ordners, als Pfad!
	 * @param oldName
	 *            - der Name des zu �ndernden Ordners.
	 * @return true - falls erfolgreich.
	 */
    public static boolean rename(String newName, String oldName) {
        return rename(new File(newName), new File(oldName));
    }

    /**
	 * Umbenennen einer Datei, eines Ordner
	 * nur werden hier zwei Objecte der File-Klasse uebergeben
	 * @author Hendrik Guelz (20966179)
	 * @param newfile
	 *            - der Neue Ordnername, representiert als File-Objekt.
	 * @param oldfile
	 *            - der Alte Ordner als File-Objekt
	 * @return true - falls erfolgreich.
	 */
    public static boolean rename(File newfile, File oldfile) {
        if (!newfile.exists() && oldfile.exists()) {
            return oldfile.renameTo(newfile);
        } else {
            return false;
        }
    }

    public boolean renameByMetadata(String filePath, int ansicht) {
        switch(ansicht) {
            case Const.NR_INTERP_TITEL:
                String newName = metaManager.getTrackNumber() + ". " + metaManager.getArtist() + " - " + metaManager.getTitle() + filePath.substring(filePath.lastIndexOf("."));
                return FileManager.rename(filePath.substring(0, filePath.lastIndexOf(Const.FILE_SEPARATOR.toString()) + 1) + newName, filePath);
            case Const.INTERP_TITEL:
                String newName1 = metaManager.getArtist() + " - " + metaManager.getTitle() + filePath.substring(filePath.lastIndexOf("."));
                return FileManager.rename(filePath.substring(0, filePath.lastIndexOf(Const.FILE_SEPARATOR.toString()) + 1) + newName1, filePath);
            case Const.TITEL_INTERP:
                String newName2 = metaManager.getTitle() + " - " + metaManager.getArtist() + filePath.substring(filePath.lastIndexOf("."));
                return FileManager.rename(filePath.substring(0, filePath.lastIndexOf(Const.FILE_SEPARATOR.toString()) + 1) + newName2, filePath);
        }
        return false;
    }

    /**
	 * Verschiebt eine Datei/einen Ordner zu einem Zielpfad(Destinationpath).
	 * @author Hendrik Guelz (20966179)
	 * @param oldPath
	 *            - alter Pfad
	 * @param destinationPath
	 *            - Zielpfasd
	 * @return true - falls erfolgreich
	 * @throws IOException
	 */
    public static boolean move(String oldPath, String destinationPath) throws IOException {
        return move(new File(oldPath), new File(destinationPath));
    }

    /**
	 * Verschiebt eine Datei/einen Ordner zu einem Ziel.
	 * 
	 * @author Hendrik Guelz (20966179) 
	 * @param oldFile
	 *            - der alte Ordner/Datei als File-Objekt
	 * @param destinationFile
	 *            - Ziel, als File-Objekt
	 * @return true - falls erfolgreich
	 * @throws IOException
	 */
    public static boolean move(File oldFile, File destinationFile) throws IOException {
        if (oldFile.equals(destinationFile)) {
            return true;
        }
        if (destinationFile.exists()) {
            return false;
        }
        if (oldFile.isDirectory()) {
            if (!destinationFile.exists()) {
                destinationFile.mkdirs();
            }
            String[] content = oldFile.list();
            boolean worked = true;
            for (String tmpFile : content) {
                worked = worked && move(new File(oldFile, tmpFile), new File(destinationFile, tmpFile));
            }
            delete(oldFile);
            return worked;
        } else {
            destinationFile.getParentFile().mkdirs();
            FileReader in = new FileReader(oldFile);
            FileWriter out = new FileWriter(destinationFile);
            for (int i = in.read(); i != -1; i = in.read()) {
                out.write(i);
            }
            in.close();
            out.close();
            destinationFile.createNewFile();
            delete(oldFile);
            return true;
        }
    }

    /**
	 * laedt die uebergebene Datei in das Upload Verzeichnis des Servers hoch.
	 * 
	 * @author Hendrik Guelz (20966179) 
	 * @param file - das object der Primefaces "FileUpload" Componente
	 * @return - der pfad zu der hochgeladenen Datei
	 * @throws IOException
	 */
    public static String uploadFile(UploadedFile file) throws IOException {
        System.err.println("in fileupload " + file.getFileName());
        String path = Const.UPLOAD_DIR + Const.FILE_SEPARATOR;
        File up = new File(path + file.getFileName());
        System.err.println(up.getPath());
        FileOutputStream fos = new FileOutputStream(up);
        InputStream in = file.getInputstream();
        byte[] buffer = new byte[Const.BUFFER_SIZE];
        int length;
        while ((length = in.read(buffer)) > 0) {
            fos.write(buffer, 0, length);
            fos.flush();
        }
        fos.close();
        in.close();
        System.err.println(up.getParentFile().listFiles().length);
        return up.getPath();
    }

    /**
	 * Verschiebt die uebergebene Datei anhand ihrer MetaDaten
	 * in das entsprechende Zielverzeichnis
	 * 
	 * @author Hendrik Guelz (20966179) 
	 * @param uploadPath
	 * @return
	 */
    public static boolean moveToDestinationDir(String uploadPath) {
        try {
            File up = new File(uploadPath);
            if (up.exists()) {
                return false;
            }
            String destination = uploadPath;
            if (isAudiofile(getExtension(up.getName()))) {
                MetaManagerAudio audio = new MetaManagerAudio(up);
                destination = Const.AUDIO_DIR + Const.FILE_SEPARATOR + audio.getArtist() + Const.FILE_SEPARATOR + audio.getAlbum() + Const.FILE_SEPARATOR + up.getName();
            } else if (isVideofile(getExtension(up.getName()))) {
                destination = Const.VIDEO_DIR + Const.FILE_SEPARATOR + up.getName().replace(getExtension(up.getName()), "") + Const.FILE_SEPARATOR + up.getName();
            }
            return move(up, new File(destination));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
	 * Verschiebt eine Audiodatei die durch das AudioData
	 * Object representiert wird in das AudioVerzeichnis
	 * 
	 * @author Hendrik Guelz (20966179) 
	 * @param audio
	 * @return - pfad zur Datei im Audioverzeichnis
	 * @throws IOException
	 */
    public static String moveAudioFile(AudioData audio) throws IOException {
        File up = new File(audio.getPath());
        String destination = Const.AUDIO_DIR + Const.FILE_SEPARATOR + audio.getArtist() + Const.FILE_SEPARATOR + audio.getAlbum() + Const.FILE_SEPARATOR + up.getName();
        if (move(up, new File(destination))) {
            return destination;
        } else {
            delete(up);
            return "";
        }
    }

    /**
	 * Verschiebt eine Videodatei die durch das VideoData
	 * Object representiert wird in das Videoverzeichnis
	 * 
	 * @author Hendrik Guelz (20966179) 
	 * @param video
	 * @return - pfad zur Datei im videoverzeichnis
	 * @throws IOException
	 */
    public static String moveVideoFile(VideoData video) throws IOException {
        File up = new File(video.getPath());
        String destination = Const.VIDEO_DIR + Const.FILE_SEPARATOR + video.getTitle() + Const.FILE_SEPARATOR + up.getName();
        if (move(up, new File(destination))) {
            return destination;
        } else {
            delete(up);
            return "";
        }
    }

    /**
	 * Prueft ob die uebergebene Endung eine erlaubte Audio Endung ist.
	 * 
	 * @author Hendrik Guelz (20966179) 
	 * @param extension
	 * @return true falls Endung eine der erlaubten audio Endungen
	 */
    public static boolean isAudiofile(String extension) {
        for (String tmp : Const.AUDIO_TYPES) {
            if (tmp.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
	 * Prueft ob die uebergebene Endung eine erlaubte Video Endung ist.
	 * 
	 * @author Hendrik Guelz (20966179) 
	 * @param extension
	 * @return true falls Endung eine der erlaubten video Endungen
	 */
    public static boolean isVideofile(String extension) {
        for (String tmp : Const.VIDEO_TYPES) {
            if (tmp.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
	 * extrahiert die Dateiendung eines Dateinamens/pfades
	 * 
	 * @author Hendrik Guelz (20966179) 
	 * @param file
	 * @return
	 */
    public static String getExtension(String file) {
        int i = file.lastIndexOf(".");
        if (i > 0 && i < file.length() - 1) {
            return file.substring(i + 1);
        } else {
            return null;
        }
    }

    /**
	 * Zipt die zu Downloadenden Dateien, mithilfe der Java Zip API
	 * (java.util.zip). 
	 * 
	 * @author Hendrik Guelz (20966179) 
	 * @param pathList
	 *            - String-List die die Pfade zu den Dateien/Ordnern, die gezipt
	 *            werden sollen, enthaelt.
	 * @return - Dateipfad der erstellten .zip-Datei
	 * @throws IOException
	 */
    public static void downloadZip(String filepath, String tmpPath) throws IOException {
        List<String> list = new ArrayList<String>();
        list.add(filepath);
        downloadZip(list, tmpPath);
    }

    /**
	 * Zipt die zu Downloadenden Dateien, mithilfe der Java Zip API
	 * (java.util.zip). 
	 * 
	 * @author Hendrik Guelz (20966179) 
	 * @param pathList
	 *            - String-List die die Pfade zu den Dateien/Ordnern, die gezipt
	 *            werden sollen, enthaelt.
	 * @return - Dateipfad der erstellten .zip-Datei
	 * @throws IOException
	 */
    public static <T> void downloadZip(List<String> pathList, String tmpPath) throws IOException {
        if (pathList.isEmpty()) {
            return;
        }
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(new File(tmpPath)));
        out.setLevel(0);
        for (String tmpStr : pathList) {
            File tmp = new File(tmpStr);
            fork(out, new File(tmpStr), tmp.getName());
        }
        out.close();
    }

    /**
	 * !!!!! HELL YEA IT WORKS... TRUST ME YOU NEVER WANT TO LOOK AT THIS
	 * CODE!!! Hilfsmethode zum rekursiven hinzufuegen der Ordner/Datein zum zip
	 * 
	 * @author Hendrik Guelz (20966179) 
	 * @throws IOException
	 */
    private static void fork(ZipOutputStream out, File tmpFile, String path) throws IOException {
        System.out.println(tmpFile);
        if (tmpFile.isDirectory()) {
            System.out.println(tmpFile + " is DIR");
            for (String tmpStr : tmpFile.list()) {
                System.out.println(tmpStr + " is Child of " + tmpFile);
                fork(out, new File(tmpFile, tmpStr), path + Const.FILE_SEPARATOR + tmpStr);
            }
        } else if (tmpFile.isFile()) {
            System.out.println(tmpFile + " is File");
            byte[] buffer = new byte[Const.BUFFER_SIZE];
            FileInputStream in = new FileInputStream(tmpFile);
            out.putNextEntry(new ZipEntry(path));
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            out.closeEntry();
            in.close();
            return;
        } else {
            return;
        }
    }

    /**
	 * generiert den Pfad fuer die Zipdatei
	 * @author Hendrik Guelz (20966179) 
	 * @return
	 */
    public static String createDownloadZipPath() {
        String zipName = "Download_from_" + getDateAndTime() + ".zip";
        return Const.TEMP_DIR + Const.FILE_SEPARATOR + zipName;
    }

    /**
	 * Hilfsmethode um die aktuelle Zeit in einem vernünftigen Format zu
	 * bekommen.
	 * @author Hendrik Guelz (20966179) 
	 * @return aktuelle zeit/Datum
	 */
    private static String getDateAndTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd_MMM_yyyy_HH_mm_ss");
        return dateFormat.format(new Date());
    }

    /**
	 * Durchsucht einen Dateipfad und gibt wahlweise den gesamten Inhalt oder
	 * nur die im Dateien im Dateipfad in einer Liste von Strings zur�ck.
	 * 
	 * @author Michael Tiede (20966946)
	 * @param path
	 *            - der zu durchsuchende Dateipfad
	 * @param filesOnly
	 *            - wenn true, werden nur Dateien in der Liste ausgegeben, sonst
	 *            Verzeichnisse und Dateien
	 * @return
	 */
    public static List<String> browseDir(String path, boolean filesOnly) {
        File[] fileArray = new File(path).listFiles();
        List<String> fileNames = new ArrayList<String>();
        for (int i = 0; i < fileArray.length; i++) {
            if (filesOnly) {
                if (fileArray[i].isFile()) {
                    fileNames.add(fileArray[i].getName());
                }
            } else {
                fileNames.add(fileArray[i].getName());
            }
        }
        return fileNames;
    }

    /**
	 * erstellt den Root-Node fuer die TreeTable (Darstellung des Dateisystems)
	 * fuegt auch gleichzeitig alle noetigen Kinder der Root, bzw. der entsprechenden
	 * Nodes hinzu. Ausgeblendet werden temp-und upload-Ordner
	 * 
	 * @author Hendrik Guelz (20966179) 
	 * @param ftv
	 * @return
	 * @throws IOException
	 */
    public static TreeNode createRootTreeNode(FileTableView ftv) throws IOException {
        TreeNode root = new DefaultTreeNode("root", null);
        addToRootTreeNode(ftv, root, new File(Const.CONTENT_DIR).listFiles());
        return root;
    }

    /**
	 * fuegt die Kinder zur entsprechenden "root"-Node hinzu
	 * 
	 * @author Hendrik Guelz (20966179) 
	 * @param ftv
	 * @param root
	 * @param files
	 * @throws IOException
	 */
    private static void addToRootTreeNode(FileTableView ftv, TreeNode root, File[] files) throws IOException {
        for (File tmp : files) {
            if (tmp.isFile()) {
                String type = "document";
                if (isAudiofile(getExtension(tmp.getName()))) {
                    type = "audio";
                } else if (isVideofile(getExtension(tmp.getName()))) {
                    type = "picture";
                }
                new DefaultTreeNode(type, getDocumentFromFile(ftv, tmp, type), root);
            } else if (!tmp.equals(new File(Const.TEMP_DIR)) && !tmp.equals(new File(Const.UPLOAD_DIR))) {
                boolean render = !tmp.equals(new File(Const.AUDIO_DIR)) && !tmp.equals(new File(Const.PLAYLISTS_DIR)) && !tmp.equals(new File(Const.VIDEO_DIR));
                TreeNode newNode = new DefaultTreeNode(ftv.new Document(tmp.getName(), Document.NO_SIZE, Document.FOLDER_TYPE, tmp.getCanonicalPath(), render), root);
                addToRootTreeNode(ftv, newNode, tmp.listFiles());
            }
        }
    }

    /**
	 * erstellt ein Document-Object (FileTableView.Document) aus einer Datei
	 * dieses wird dann als Element in der TreeTable (DateiSystem Tabelle)
	 * genutzt
	 * 
	 * @author Hendrik Guelz (20966179) 
	 * @param ftv
	 * @param tmp
	 * @param type
	 * @return 
	 * @throws IOException
	 */
    private static Document getDocumentFromFile(FileTableView ftv, File tmp, String type) throws IOException {
        Document d;
        String sizeDesc = FileUtils.byteCountToDisplaySize(tmp.length());
        String typeDesc = Document.UNKNOWN_TYPE;
        if (type.equals("audio")) {
            typeDesc = Document.AUDIO_TYPE;
        } else if (type.equals("picture")) {
            typeDesc = Document.VIDEO_TYPE;
        }
        d = ftv.new Document(tmp.getName(), sizeDesc, typeDesc, tmp.getCanonicalPath(), true);
        return d;
    }

    /**
	 * prueft ob eine Datei, ein Ordner existiert
	 * 
	 * @author Hendrik Guelz (20966179) 
	 * @param path
	 * @return
	 */
    public static boolean fileExists(String path) {
        return fileExists(new File(path));
    }

    /**
	 * prueft ob eine Datei, ein Ordner existiert
	 * 
	 * @author Hendrik Guelz (20966179) 
	 * @param file
	 * @return
	 */
    public static boolean fileExists(File file) {
        return file.exists();
    }
}
