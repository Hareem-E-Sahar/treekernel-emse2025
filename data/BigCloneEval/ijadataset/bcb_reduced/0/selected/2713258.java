package ndsromrenamer.file.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import ndsromrenamer.data.GamesDAO;
import ndsromrenamer.data.RegionCodes;
import ndsromrenamer.data.Settings;
import ndsromrenamer.data.entities.GameDB;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.application.Application;
import org.jdesktop.application.Task;

/**
 *
 * @author wusel
 */
public class RomRenamerTask extends Task<Void, Void> {

    private static final Log log = LogFactory.getLog(RomRenamerTask.class);

    private GameDB[] game;

    private Settings settings;

    private String os;

    private String tmpFolder;

    private static final Pattern ndsFilesPattern = Pattern.compile(".*(nds$)");

    public RomRenamerTask(Application app, GameDB[] game, Settings settings) {
        super(app);
        this.game = game;
        this.settings = settings;
        if (this.settings.getTmpFolder() == null || this.settings.getTmpFolder().equals("")) {
            this.tmpFolder = "./tmp";
        } else {
            this.tmpFolder = this.settings.getTmpFolder();
        }
    }

    private String buildRomName(GameDB game) {
        String pattern = settings.getRenamePattern();
        int length = pattern.length();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < length; i++) {
            char current = pattern.toCharArray()[i];
            if (current == '%') {
                i++;
                current = pattern.toCharArray()[i];
                if (current == 'N') {
                    buffer.append(game.getReleaseNumber());
                } else if (current == 'T') {
                    buffer.append(game.getTitle());
                } else if (current == 'R') {
                    buffer.append(RegionCodes.INSTANCE.getShortRegionForRegionCode(game.getRegionCode()));
                } else if (current == 'L') {
                    int langCount = RegionCodes.INSTANCE.getLanguageCountForCode(game.getLanguageCode());
                    if (langCount > 1) {
                        buffer.append("MULTI-" + langCount);
                    }
                } else if (current == 'l') {
                    int langCount = RegionCodes.INSTANCE.getLanguageCountForCode(game.getLanguageCode());
                    if (langCount > 1) {
                        buffer.append(langCount);
                    }
                }
            } else {
                buffer.append(current);
            }
        }
        buffer.append(".nds");
        String erg = buffer.toString();
        erg = erg.replaceAll("\\(\\)", "");
        return erg;
    }

    @Override
    protected Void doInBackground() throws Exception {
        this.os = System.getProperties().get("os.name").toString();
        log.debug(os);
        int exitValue = 0;
        for (GameDB currentGame : game) {
            if (currentGame.isAvailable()) {
                String romName = buildRomName(currentGame);
                if (log.isTraceEnabled()) {
                    log.trace("start rename for " + currentGame);
                    log.trace("new romname: " + romName);
                }
                if (!new File(currentGame.getCurrentlocation()).exists()) {
                    log.debug("game is no longer available");
                    message("rename.failed", romName);
                    currentGame.setAvailable(false);
                    currentGame.setCurrentlocation(null);
                    GamesDAO.INSTANCE.updateGame(currentGame);
                } else {
                    message("rename.start", currentGame.getTitle(), romName);
                    if (currentGame.getCurrentlocation().endsWith(".zip")) {
                        extractTmpFileFromZip(currentGame, romName);
                    }
                    if (this.settings.isCompressOutput()) {
                        if (settings.getCompressedOutputFormat().equals("zip")) {
                            exitValue = startCompressOutputToZip(currentGame, romName);
                        }
                    } else {
                        exitValue = startCopyMove(currentGame, romName);
                    }
                    if (exitValue == 0) {
                        message("rename.ok", romName);
                    } else {
                        message("rename.failed", romName);
                    }
                    if (exitValue == 0 && settings.isDeleteSourceFiles()) {
                        log.debug("delete sourcefile");
                        message("rename.delete", currentGame.getSourceFile());
                        new File(currentGame.getSourceFile()).delete();
                        currentGame.setAvailable(false);
                        GamesDAO.INSTANCE.updateGame(currentGame);
                    }
                }
            }
        }
        return null;
    }

    private void extractTmpFileFromZip(GameDB currentGame, String romName) {
        if (log.isDebugEnabled()) {
            log.debug("extractTmpFileFromZip(GameDB currentGame, String romName)");
            log.debug("CurrentLocation: " + currentGame.getCurrentlocation());
            log.debug("SourceFile: " + currentGame.getSourceFile());
        }
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(currentGame.getCurrentlocation());
            Enumeration<ZipEntry> entrys = (Enumeration<ZipEntry>) zipFile.entries();
            while (entrys.hasMoreElements()) {
                ZipEntry entry = entrys.nextElement();
                if (ndsFilesPattern.matcher(entry.getName().toLowerCase()).matches()) {
                    log.trace("extract nds file to tmpFolder");
                    String tmpPath = this.tmpFolder + File.separatorChar + entry.getName();
                    RandomAccessFile raf = new RandomAccessFile(tmpPath, "rw");
                    InputStream is = zipFile.getInputStream(entry);
                    byte[] buffer = new byte[512];
                    int len = 0;
                    while ((len = is.read(buffer)) != -1) {
                        raf.write(buffer, 0, len);
                    }
                    raf.close();
                    currentGame.setCurrentlocation(tmpPath);
                }
            }
        } catch (IOException ex) {
            log.error(null, ex);
        } finally {
            try {
                zipFile.close();
            } catch (Exception exception) {
            }
        }
    }

    private int startCompressOutputToZip(GameDB currentGame, String romName) {
        if (log.isDebugEnabled()) {
            log.debug("startCompressOutputToZip(GameDB currentGame, String romName)");
            log.debug("CurrentLocation: " + currentGame.getCurrentlocation());
            log.debug("SourceFile: " + currentGame.getSourceFile());
        }
        int returnCode = -1;
        String inputFile = currentGame.getCurrentlocation();
        String outputFile = this.settings.getOutputFolder() + File.separatorChar + romName.substring(0, romName.length() - 4) + ".zip";
        if (log.isDebugEnabled()) {
            log.debug("InputFile: " + inputFile);
            log.debug("OutputFile: " + outputFile);
        }
        message("rename.zip.start", romName, outputFile);
        ZipOutputStream zous = null;
        try {
            ZipEntry romEntry = new ZipEntry(romName);
            zous = new ZipOutputStream(new FileOutputStream(outputFile, false));
            zous.putNextEntry(romEntry);
            RandomAccessFile raf = new RandomAccessFile(inputFile, "r");
            long fileSize = raf.length();
            long currentPosition = 0;
            int bufferLength = 2048;
            int len;
            byte[] buffer = new byte[bufferLength];
            while ((len = raf.read(buffer)) != -1) {
                setProgress(currentPosition += len, 0, fileSize);
                zous.write(buffer, 0, len);
            }
            raf.close();
            returnCode = 0;
        } catch (IOException ex) {
            log.error(null, ex);
            returnCode = -1;
        } finally {
            try {
                zous.close();
            } catch (Exception ex) {
            }
        }
        return returnCode;
    }

    private int startCopyMove(GameDB currentGame, String romName) {
        if (log.isDebugEnabled()) {
            log.debug("startCopyMove(GameDB currentGame)");
            log.debug("CurrentLocation: " + currentGame.getCurrentlocation());
            log.debug("SourceFile: " + currentGame.getSourceFile());
        }
        int returnCode = -1;
        if (this.settings.isDeleteSourceFiles()) {
            message("rename.move.start", currentGame.getCurrentlocation(), romName);
            try {
                if (os.toLowerCase().contains("windows")) {
                    returnCode = moveFileWindows(currentGame, romName);
                } else {
                    returnCode = moveFileLinux(currentGame, romName);
                }
            } catch (IOException ex) {
                log.error(null, ex);
                message("rename.move.error", currentGame.getCurrentlocation(), romName);
            }
        } else {
            try {
                message("rename.copy.start", currentGame.getCurrentlocation(), romName);
                if (os.toLowerCase().contains("windows")) {
                    returnCode = copyFileWindows(currentGame, romName);
                } else {
                    returnCode = copyFileLinux(currentGame, romName);
                }
            } catch (IOException ex) {
                message("rename.copy.error", currentGame.getCurrentlocation(), romName);
                log.error(null, ex);
            }
        }
        return returnCode;
    }

    private int moveFileLinux(GameDB currentGame, String romName) throws IOException {
        Process process = new ProcessBuilder(new String[] { "mv", currentGame.getCurrentlocation(), settings.getOutputFolder() + "/" + romName }).start();
        try {
            process.waitFor();
        } catch (InterruptedException ex) {
            log.error(null, ex);
        }
        return process.exitValue();
    }

    private int moveFileWindows(GameDB currentGame, String romName) throws IOException {
        Process process = Runtime.getRuntime().exec("cmd /c move \"" + currentGame.getCurrentlocation() + "\" \"" + settings.getOutputFolder() + "\\" + romName + "\"");
        try {
            process.waitFor();
        } catch (InterruptedException ex) {
            log.error(null, ex);
        }
        return process.exitValue();
    }

    private int copyFileLinux(GameDB currentGame, String romName) throws IOException {
        Process process = new ProcessBuilder(new String[] { "cp", currentGame.getCurrentlocation(), settings.getOutputFolder() + "/" + romName }).start();
        try {
            process.waitFor();
        } catch (InterruptedException ex) {
            log.error(null, ex);
        }
        return process.exitValue();
    }

    private int copyFileWindows(GameDB currentGame, String romName) throws IOException {
        Process process = Runtime.getRuntime().exec("cmd /c copy \"" + currentGame.getCurrentlocation() + "\" \"" + settings.getOutputFolder() + "\\" + romName + "\"");
        try {
            process.waitFor();
        } catch (InterruptedException ex) {
            log.error(null, ex);
        }
        return process.exitValue();
    }
}
