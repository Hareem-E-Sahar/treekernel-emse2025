package org.gudy.azureus2.ui.swt.debug;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.gudy.azureus2.core3.config.COConfigurationManager;
import org.gudy.azureus2.core3.logging.LogEvent;
import org.gudy.azureus2.core3.logging.LogIDs;
import org.gudy.azureus2.core3.logging.Logger;
import org.gudy.azureus2.core3.logging.impl.FileLogging;
import org.gudy.azureus2.core3.util.*;
import org.gudy.azureus2.platform.PlatformManagerFactory;
import org.gudy.azureus2.ui.swt.SimpleTextEntryWindow;
import org.gudy.azureus2.ui.swt.Utils;
import org.gudy.azureus2.ui.swt.shells.CoreWaiterSWT;
import org.gudy.azureus2.ui.swt.shells.MessageBoxShell;
import org.gudy.azureus2.ui.swt.shells.CoreWaiterSWT.TriggerInThread;
import com.aelitis.azureus.core.*;
import com.aelitis.azureus.ui.UserPrompterResultListener;

/**
 * @author TuxPaper
 * @created May 28, 2006
 *
 */
public class UIDebugGenerator {

    public static void generate() {
        Display display = Display.getCurrent();
        if (display == null) {
            return;
        }
        while (display.readAndDispatch()) {
        }
        Shell[] shells = display.getShells();
        if (shells == null || shells.length == 0) {
            return;
        }
        final File path = new File(SystemProperties.getUserPath(), "debug");
        if (!path.isDirectory()) {
            path.mkdir();
        } else {
            try {
                File[] files = path.listFiles();
                for (int i = 0; i < files.length; i++) {
                    files[i].delete();
                }
            } catch (Exception e) {
            }
        }
        for (int i = 0; i < shells.length; i++) {
            try {
                Shell shell = shells[i];
                Image image = null;
                if (shell.getData("class") instanceof ObfusticateShell) {
                    ObfusticateShell shellClass = (ObfusticateShell) shell.getData("class");
                    try {
                        image = shellClass.generateObfusticatedImage();
                    } catch (Exception e) {
                        Debug.out("Obfusticating shell " + shell, e);
                    }
                } else {
                    Rectangle clientArea = shell.getClientArea();
                    image = new Image(display, clientArea.width, clientArea.height);
                    GC gc = new GC(shell);
                    try {
                        gc.copyArea(image, clientArea.x, clientArea.y);
                    } finally {
                        gc.dispose();
                    }
                }
                if (image != null) {
                    File file = new File(path, "image-" + i + ".vpg");
                    String sFileName = file.getAbsolutePath();
                    ImageLoader imageLoader = new ImageLoader();
                    imageLoader.data = new ImageData[] { image.getImageData() };
                    imageLoader.save(sFileName, SWT.IMAGE_JPEG);
                }
            } catch (Exception e) {
                Logger.log(new LogEvent(LogIDs.GUI, "Creating Obfusticated Image", e));
            }
        }
        SimpleTextEntryWindow entryWindow = new SimpleTextEntryWindow("UIDebugGenerator.messageask.title", "UIDebugGenerator.messageask.text", true);
        entryWindow.prompt();
        if (!entryWindow.hasSubmittedInput()) {
            return;
        }
        String message = entryWindow.getSubmittedInput();
        if (message == null || message.length() == 0) {
            new MessageBoxShell(SWT.OK, "UIDebugGenerator.message.cancel", (String[]) null).open(null);
            return;
        }
        try {
            File fUserMessage = new File(path, "usermessage.txt");
            FileWriter fw;
            fw = new FileWriter(fUserMessage);
            fw.write(message);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        CoreWaiterSWT.waitForCore(TriggerInThread.ANY_THREAD, new AzureusCoreRunningListener() {

            public void azureusCoreRunning(AzureusCore core) {
                core.createOperation(AzureusCoreOperation.OP_PROGRESS, new AzureusCoreOperationTask() {

                    public void run(AzureusCoreOperation operation) {
                        try {
                            File fEvidence = new File(path, "evidence.log");
                            PrintWriter pw = new PrintWriter(fEvidence, "UTF-8");
                            AEDiagnostics.generateEvidence(pw);
                            pw.close();
                        } catch (IOException e) {
                            Debug.printStackTrace(e);
                        }
                    }
                });
            }
        });
        try {
            final File outFile = new File(SystemProperties.getUserPath(), "debug.zip");
            if (outFile.exists()) {
                outFile.delete();
            }
            AEDiagnostics.flushPendingLogs();
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFile));
            File logPath = new File(SystemProperties.getUserPath(), "logs");
            File[] files = logPath.listFiles(new FileFilter() {

                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(".log");
                }
            });
            addFilesToZip(out, files);
            File userPath = new File(SystemProperties.getUserPath());
            files = userPath.listFiles(new FileFilter() {

                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(".log");
                }
            });
            addFilesToZip(out, files);
            files = path.listFiles();
            addFilesToZip(out, files);
            final long ago = SystemTime.getCurrentTime() - 1000L * 60 * 60 * 24 * 90;
            File azureusPath = new File(SystemProperties.getApplicationPath());
            files = azureusPath.listFiles(new FileFilter() {

                public boolean accept(File pathname) {
                    return (pathname.getName().startsWith("hs_err") && pathname.lastModified() > ago);
                }
            });
            addFilesToZip(out, files);
            File javaLogPath = new File(System.getProperty("user.home"), "Library" + File.separator + "Logs" + File.separator + "Java");
            if (javaLogPath.isDirectory()) {
                files = javaLogPath.listFiles(new FileFilter() {

                    public boolean accept(File pathname) {
                        return (pathname.getName().endsWith("log") && pathname.lastModified() > ago);
                    }
                });
                addFilesToZip(out, files);
            }
            boolean bLogToFile = COConfigurationManager.getBooleanParameter("Logging Enable");
            String sLogDir = COConfigurationManager.getStringParameter("Logging Dir", "");
            if (bLogToFile && sLogDir != null) {
                File loggingFile = new File(sLogDir, FileLogging.LOG_FILE_NAME);
                if (loggingFile.isFile()) {
                    addFilesToZip(out, new File[] { loggingFile });
                }
            }
            out.close();
            if (outFile.exists()) {
                MessageBoxShell mb = new MessageBoxShell(SWT.OK | SWT.CANCEL | SWT.ICON_INFORMATION | SWT.APPLICATION_MODAL, "UIDebugGenerator.complete", new String[] { outFile.toString() });
                mb.open(new UserPrompterResultListener() {

                    public void prompterClosed(int result) {
                        if (result == SWT.OK) {
                            try {
                                PlatformManagerFactory.getPlatformManager().showFile(outFile.getAbsolutePath());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addFilesToZip(ZipOutputStream out, File[] files) {
        byte[] buf = new byte[1024];
        if (files == null) {
            return;
        }
        for (int j = 0; j < files.length; j++) {
            File file = files[j];
            FileInputStream in;
            try {
                in = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                continue;
            }
            try {
                ZipEntry entry = new ZipEntry(file.getName());
                entry.setTime(file.lastModified());
                out.putNextEntry(entry);
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.closeEntry();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void obfusticateArea(Display display, Image image, Rectangle bounds) {
        GC gc = new GC(image);
        try {
            gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
            gc.setForeground(display.getSystemColor(SWT.COLOR_RED));
            gc.fillRectangle(bounds);
            gc.drawRectangle(bounds);
            int x2 = bounds.x + bounds.width;
            int y2 = bounds.y + bounds.height;
            gc.drawLine(bounds.x, bounds.y, x2, y2);
            gc.drawLine(x2, bounds.y, bounds.x, y2);
        } finally {
            gc.dispose();
        }
    }

    /**
	 * @param image
	 * @param bounds
	 * @param text
	 */
    public static void obfusticateArea(Display display, Image image, Rectangle bounds, String text) {
        if (bounds.isEmpty()) return;
        if (text == "") {
            obfusticateArea(display, image, bounds);
            return;
        }
        GC gc = new GC(image);
        try {
            gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
            gc.setForeground(display.getSystemColor(SWT.COLOR_RED));
            gc.fillRectangle(bounds);
            gc.drawRectangle(bounds);
            gc.setClipping(bounds);
            gc.drawText(text, bounds.x + 2, bounds.y + 1);
        } finally {
            gc.dispose();
        }
    }

    /**
	 * @param image
	 * @param control
	 * @param shellOffset 
	 * @param text 
	 */
    public static void obfusticateArea(Image image, Control control, Point shellOffset, String text) {
        Rectangle bounds = control.getBounds();
        Point offset = control.getParent().toDisplay(bounds.x, bounds.y);
        bounds.x = offset.x - shellOffset.x;
        bounds.y = offset.y - shellOffset.y;
        obfusticateArea(control.getDisplay(), image, bounds, text);
    }
}
