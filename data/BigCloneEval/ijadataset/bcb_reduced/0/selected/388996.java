package org.fbmc.file;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import org.fbmc.FreeboxMediaCenter;
import org.fbmc.server.EmbeddedSQLServer;

public class FileSniffer extends Thread {

    private File[] roots;

    private HashMap<String, MediaFile> mediaFiles;

    private EmbeddedSQLServer sqlServer;

    private int nbNewMediaFiles = 0;

    private int nbUpdatedMediaFiles = 0;

    private int nbDeletedMediaFiles = 0;

    /**
	 * 
	 *
	 */
    public FileSniffer() {
        this(File.listRoots());
    }

    /**
	 * 
	 * @param roots
	 */
    public FileSniffer(File[] roots) {
        this.roots = roots;
        this.setPriority(Thread.MIN_PRIORITY);
        sqlServer = FreeboxMediaCenter.getInstance().getSQLServer();
    }

    /**
	 * 
	 * @see java.lang.Thread#run()
	 */
    public void run() {
        double start = System.currentTimeMillis();
        try {
            mediaFiles = sqlServer.retrieveAllMediaFiles();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            mediaFiles = new HashMap<String, MediaFile>();
        }
        System.out.println(mediaFiles.size() + " fichiers ont d�j� �t� index�s.");
        System.out.println("On commence l'indexation des fichiers");
        for (File root : roots) {
            if (root.isDirectory()) browserDirectory(root);
        }
        if (mediaFiles.size() > 0) {
            System.out.println(mediaFiles.size() + " fichiers ont �t� supprim�s depuis la derni�re indexation");
            for (Iterator<String> iter = mediaFiles.keySet().iterator(); iter.hasNext(); ) {
                try {
                    sqlServer.deleteMediaFile(mediaFiles.get(iter.next()));
                    nbDeletedMediaFiles++;
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
            }
        }
        try {
            sqlServer.commit();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        double end = System.currentTimeMillis();
        System.out.println("Fin de l'indexation. Dur�e : " + (end - start));
        System.out.println("Stats :");
        System.out.println("\tNewFiles : " + nbNewMediaFiles);
        System.out.println("\tUpdatesFiles : " + nbUpdatedMediaFiles);
        System.out.println("\tDeletedFiles : " + nbDeletedMediaFiles);
        System.exit(-1);
    }

    /**
	 * 
	 * @param directory
	 */
    private void browserDirectory(File directory) {
        if (!directory.isDirectory()) return;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) browserDirectory(file); else if (file.isFile() && MediaFile.isMediaFile(file)) {
                    try {
                        MediaFile newMediaFile = MediaFile.create(file);
                        if (!mediaFiles.containsKey(newMediaFile.getPath())) {
                            sqlServer.insertMediaFile(newMediaFile);
                            nbNewMediaFiles++;
                        } else {
                            MediaFile oldMediaFile = mediaFiles.get(newMediaFile.getPath());
                            if (oldMediaFile.compareTo(newMediaFile) != 0) {
                                System.out.println(oldMediaFile);
                                sqlServer.deleteMediaFile(oldMediaFile);
                                sqlServer.insertMediaFile(newMediaFile);
                                nbUpdatedMediaFiles++;
                            }
                            mediaFiles.remove(newMediaFile.getPath());
                        }
                    } catch (SQLException sqle) {
                        sqle.printStackTrace();
                    }
                }
            }
        }
    }
}
