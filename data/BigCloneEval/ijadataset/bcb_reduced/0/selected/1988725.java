package de.tu_clausthal.in.agrausch.weit.export.steuerung.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import de.tu_clausthal.in.agrausch.weit.export.ootrans.main.ImageReferenceXtractor;

/**
 * @author Dragan
 * 
 */
public class FileUtilities {

    private static Logger logger = Logger.getLogger("de.tu_clausthal.in.agrausch.weit.export.steuerung.handler.FileUtilities");

    private static Set<String> imageLog = new HashSet<String>();

    /**
	 * Copies the images from imagefolder to destfolder.
	 * 
	 * @param destFolder
	 * @param refImages
	 * @param overwrite Should existing files be overwritten?
	 * @throws IOException
	 */
    public static void copyFolderContentsToFolder(File from, File destFolder, Set<String> refImages, boolean overwrite) throws IOException {
        destFolder.mkdirs();
        if (from == null || from.listFiles() == null) {
            throw new IOException("Keine Dateien im Bilderverzeichnis!");
        }
        for (File file : from.listFiles()) {
            if (file.isHidden() || !fileContainedInRefSet(file.getName(), refImages)) continue;
            File destFile = new File(destFolder, file.getName());
            if (file.isDirectory()) {
                copyFolderContentsToFolder(file, destFile, refImages, overwrite);
            } else {
                if (overwrite || !destFile.exists()) {
                    copyFile(file, destFile);
                }
            }
        }
    }

    /**
	 * Checks if name is contained in the set of referenced images.
	 * 
	 * @param name
	 * @param refImages
	 * @return
	 */
    private static boolean fileContainedInRefSet(String name, Set<String> refImages) {
        for (String refImage : refImages) {
            if (refImage.substring(refImage.lastIndexOf("/") + 1).equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    /**
	 * Copies the contents of a folder to a destination folder.
	 * 
	 * @param exportFiles
	 * @param dest
	 * @param prefixPath
	 * @throws IOException
	 */
    public static void copyFolderContentsToFolder(List<String> exportFiles, File dest, String prefixPath) throws IOException {
        dest.mkdirs();
        for (String fileLoc : exportFiles) {
            File srcFile = new File(prefixPath + File.separator + fileLoc);
            File destFile = new File(dest.getAbsolutePath() + File.separator + srcFile.getName());
            copyFile(srcFile, destFile);
            logger.debug("FILE COPY: " + srcFile.getCanonicalPath() + " -> " + destFile.getCanonicalPath());
        }
    }

    /**
	 * An (so far unused) method for copying files from the JAR to the outside
	 * world :)
	 * 
	 * @param dest
	 * @param fileLoc
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
    @SuppressWarnings("unused")
    private static void jarFileCopy(File dest, String fileLoc) throws FileNotFoundException, IOException {
        InputStream stream = ClassLoader.getSystemResourceAsStream(fileLoc);
        int len = 32768;
        byte[] buff = new byte[(int) Math.min(len, 32768)];
        String outFile = dest.getAbsoluteFile() + File.separator + fileLoc.substring(fileLoc.lastIndexOf("/"), fileLoc.length());
        FileOutputStream fos = new FileOutputStream(outFile, false);
        while (0 < (len = stream.read(buff))) fos.write(buff, 0, len);
        fos.flush();
        fos.close();
        stream.close();
    }

    /**
	 * Checks if there are any dead image links. If so, this method places a
	 * dummy.jpg in its place so that the link is not broken.
	 */
    public static void checkForDeadImages(File odtResult) {
        try {
            Set<String> xImages = ImageReferenceXtractor.xtract(odtResult);
            if (xImages == null) {
                logger.info("No referenced images in document, nothing to" + " extract.");
                return;
            }
            byte[] bytesJPG = convertToByteArray(loadImageFromJAR("resources/dummy.jpg"));
            byte[] bytesGIF = convertToByteArray(loadImageFromJAR("resources/dummy.gif"));
            if (bytesJPG == null) {
                logger.warn("Dummy image JPG not found.");
                return;
            } else if (bytesGIF == null) {
                logger.warn("Dummy image GIF not found.");
                return;
            }
            for (String xtractedImage : xImages) {
                File imageFile;
                if (new File(xtractedImage).isAbsolute()) {
                    imageFile = new File(xtractedImage);
                } else {
                    imageFile = new File(odtResult.getParentFile().getAbsolutePath() + File.separator + xtractedImage);
                }
                imageFile = new File(imageFile.getCanonicalPath().replaceAll("%20", " "));
                if (!imageFile.exists()) {
                    logger.info("Dummy image check: " + imageFile.getCanonicalPath() + " does not exist!");
                    byte[] bytesToWrite = null;
                    if (imageFile.getCanonicalPath().toLowerCase().endsWith("gif")) {
                        bytesToWrite = bytesGIF;
                    } else if (imageFile.getCanonicalPath().toLowerCase().endsWith("jpg")) {
                        bytesToWrite = bytesJPG;
                    } else {
                        logger.warn("Image is of unknown type - can't replace it with dummy image: " + imageFile.getCanonicalPath());
                        continue;
                    }
                    FileOutputStream fOut = new FileOutputStream(imageFile);
                    fOut.write(bytesToWrite);
                    fOut.close();
                    logger.info("Written DUMMY image to: " + imageFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("ERROR CheckForDeadImages: " + e.getLocalizedMessage());
        }
    }

    private static byte[] convertToByteArray(Byte[] bytes) {
        byte[] result = new byte[bytes.length];
        int i = 0;
        for (Byte bytte : bytes) {
            result[i++] = bytte;
        }
        return result;
    }

    private static Byte[] loadImageFromJAR(String resource) {
        byte[] bytesJPG = null;
        List<Byte> list = new ArrayList<Byte>();
        try {
            InputStream stream = ClassLoader.getSystemResourceAsStream(resource);
            while (stream.available() > 0) {
                bytesJPG = new byte[1];
                stream.read(bytesJPG, 0, 1);
                list.add(new Byte(bytesJPG[0]));
            }
            logger.debug("The dummy image " + resource + " is " + list.size() + " bytes big.");
        } catch (Exception e) {
            logger.debug(e.getMessage());
        }
        return list.toArray(new Byte[0]);
    }

    public static void addToImageLog(String xtractedImage) {
        imageLog.add(xtractedImage);
    }

    public static void resetImageLog() {
        imageLog.clear();
    }

    public static Set<String> getImageLog() {
        return imageLog;
    }

    /**
	 * Move a file.
	 * 
	 * @param from
	 * @param to
	 * @param move
	 * @throws IOException
	 */
    public static void copyFile(File in, File out) throws IOException {
        FileChannel sourceChannel = new FileInputStream(in).getChannel();
        FileChannel destinationChannel = new FileOutputStream(out).getChannel();
        try {
            sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
        } finally {
            sourceChannel.close();
            destinationChannel.close();
        }
    }
}
