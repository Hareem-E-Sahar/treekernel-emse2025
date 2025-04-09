package cbviewer;

import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.lang.Runtime.*;
import java.lang.Process.*;

public class CBArchiveLoader {

    /**
	* Default Constructor
	**/
    public CBArchiveLoader(CBViewer theApp) {
        this.theApp = theApp;
    }

    /**
	* Opens an archive
	* @param entryMode 0=use the fileDialog 1=use provided file
	* @param archiveFile the archive file if entryMode = 1
	*/
    protected void openArchive(int entryMode, String archiveFile) {
        Runtime r = Runtime.getRuntime();
        Process testProc = null;
        int fileResult = 0;
        int progress = 0;
        if (entryMode == 0) {
            fileResult = getCBFile();
            if (fileResult == 1) {
                theApp.fileStuff.setArchiveDir((String) fileEntries.get(2));
                theApp.fileStuff.setCurrentArchive((String) fileEntries.get(1));
            }
        }
        if (entryMode == 1) {
            File tempFile = new File(archiveFile);
            fileEntries.clear();
            fileEntries.trimToSize();
            fileEntries.add(tempFile.getPath());
            fileEntries.add(tempFile.getName());
            fileEntries.add(tempFile.getParent());
            fileEntries.add(tempFile);
            fileResult = 1;
        }
        theApp.frame.setCursor(new Cursor(java.awt.Cursor.WAIT_CURSOR));
        if (fileResult == 1) {
            String filename = (String) fileEntries.get(0);
            String fileToWorkOn = (String) fileEntries.get(1);
            if ((fileToWorkOn.toLowerCase().endsWith(".cbr") || fileToWorkOn.toLowerCase().endsWith(".rar")) && (inZip == false)) {
                theApp.fileStuff.setZipFlag(false);
                if (theApp.fileStuff.getTempDirFlag() == true) {
                    removeTempDir();
                }
                switch(theApp.utils.checkOS()) {
                    case 0:
                    case 2:
                        handleRarLinux(filename, fileToWorkOn, r, testProc);
                        break;
                    case 1:
                        handleRarWindows(filename, fileToWorkOn, r, testProc);
                        break;
                    default:
                        System.err.println("This OS is not currently supported by CBViewer for rar files");
                        break;
                }
            }
            if (fileToWorkOn.toLowerCase().endsWith(".cbz") || fileToWorkOn.toLowerCase().endsWith(".zip") || (inZip == true)) {
                theApp.thePage.clearBuffImage();
                theApp.fileStuff.setZipFlag(true);
                theApp.fileStuff.zipEntries.clear();
                theApp.fileStuff.zipEntries.trimToSize();
                theApp.fileStuff.zipEntries.add((File) fileEntries.get(3));
                ZipInputStream zipin = null;
                try {
                    zipin = new ZipInputStream(new FileInputStream((File) theApp.fileStuff.zipEntries.get(0)));
                } catch (FileNotFoundException e) {
                    System.err.println("The Zip file was not found properly" + e);
                }
                ZipEntry currentEntry = null;
                try {
                    currentEntry = zipin.getNextEntry();
                    while (currentEntry != null) {
                        if (currentEntry.getName().toLowerCase().endsWith(".jpg") || currentEntry.getName().toLowerCase().endsWith(".jpeg") || currentEntry.getName().toLowerCase().endsWith(".png")) {
                            theApp.fileStuff.zipEntries.add(currentEntry.getName());
                        }
                        zipin.closeEntry();
                        currentEntry = zipin.getNextEntry();
                    }
                    zipin.close();
                } catch (IOException e) {
                    System.err.println("Problem reading the zip file" + e);
                }
            }
            if (theApp.fileStuff.getZipFlag() == false) {
                pictureDir = workingDir + File.separatorChar + tempDirName;
                theApp.fileStuff.setTempDir(new File(pictureDir));
                theApp.pageStuff.pictures = theApp.fileStuff.getTempDir().list();
                File f = new File(theApp.fileStuff.getTempPath(), theApp.pageStuff.pictures[0]);
                if (f.isDirectory()) {
                    pictureDir = f.getPath();
                    theApp.fileStuff.setTempDir(new File(pictureDir));
                    theApp.pageStuff.pictures = theApp.fileStuff.getTempDir().list();
                    f = new File(theApp.fileStuff.getTempPath(), theApp.pageStuff.pictures[0]);
                }
            }
            if (theApp.fileStuff.getZipFlag() == false) {
                theApp.pageStuff.setLastPage(theApp.pageStuff.pictures.length - 1);
                theApp.pageStuff.pictures = sortStuff.sortMethodOne(theApp.pageStuff.pictures);
                theApp.menu.jumpMenu.regenMenu(theApp.pageStuff.pictures);
            }
            if (theApp.fileStuff.getZipFlag() == true) {
                theApp.pageStuff.setLastPage(theApp.fileStuff.zipEntries.size() - 1);
                theApp.fileStuff.zipEntries = sortStuff.sortMethodTwo(theApp.fileStuff.zipEntries);
                String[] entries = new String[theApp.fileStuff.zipEntries.size() - 1];
                for (int tmp = 1; tmp < theApp.fileStuff.zipEntries.size(); tmp++) {
                    entries[tmp - 1] = (String) theApp.fileStuff.zipEntries.get(tmp);
                }
                theApp.menu.jumpMenu.regenMenu(entries);
            }
            theApp.pageStuff.setCurrentPage(0);
            theApp.controls.processButtons();
            theApp.frame.getContentPane().validate();
        }
        inZip = false;
        theApp.frame.setCursor(new Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        theApp.menu.enableCloseArchive(true);
    }

    /**
	* Handles RAR files in windows
	* @param filename File to work with including path
	* @param fileToWorkOn File to work with just filename
	* @param r Runtime environment for this program
	* @param testProc Process for the native commands
	*/
    private void handleRarWindows(String filename, String fileToWorkOn, Runtime r, Process testProc) {
        workingDir = System.getProperty("user.home");
        File tdir = new File(workingDir + System.getProperty("file.separator") + tempDirName);
        tdir.mkdir();
        theApp.fileStuff.setTempDirFlag(true);
        boolean wrongFile = true;
        try {
            ZipFile testme = new ZipFile(filename);
        } catch (ZipException e) {
            wrongFile = false;
        } catch (IOException e) {
            System.err.println("IOException: " + e);
        }
        if (wrongFile == true) {
            inZip = true;
            removeTempDir();
            openArchive(1, filename);
            return;
        }
        try {
            System.out.println("filename = " + filename);
            System.out.println("output = " + workingDir + System.getProperty("file.separator") + tempDirName);
            testProc = r.exec("cmd /C unrar e -y -ep \"" + filename + "\"", null, new File(workingDir + System.getProperty("file.separator") + tempDirName));
            InputStream stdin = testProc.getInputStream();
            InputStreamReader isr = new InputStreamReader(stdin);
            BufferedReader br = new BufferedReader(isr);
            String lineIn = null;
            while ((lineIn = br.readLine()) != null) {
                System.out.println(lineIn);
            }
            System.out.println("Waiting for process to complete");
            testProc.waitFor();
            System.out.println("Process complete");
            testProc = r.exec("cmd /C del *.nfo", null, new File(workingDir + System.getProperty("file.separator") + tempDirName));
            stdin = testProc.getInputStream();
            while ((lineIn = br.readLine()) != null) {
                System.out.println(lineIn);
            }
            testProc.waitFor();
            testProc = r.exec("cmd /C del *.sfv", null, new File(workingDir + System.getProperty("file.separator") + tempDirName));
            stdin = testProc.getInputStream();
            while ((lineIn = br.readLine()) != null) {
                System.out.println(lineIn);
            }
            testProc.waitFor();
            testProc = r.exec("cmd /C del *.md5", null, new File(workingDir + System.getProperty("file.separator") + tempDirName));
            stdin = testProc.getInputStream();
            while ((lineIn = br.readLine()) != null) {
                System.out.println(lineIn);
            }
            testProc.waitFor();
            testProc = r.exec("cmd /C del *.db", null, new File(workingDir + System.getProperty("file.separator") + tempDirName));
            stdin = testProc.getInputStream();
            while ((lineIn = br.readLine()) != null) {
                System.out.println(lineIn);
            }
            testProc.waitFor();
        } catch (IOException exception) {
            System.err.println("Problem unraring the file");
        } catch (InterruptedException exception) {
            System.err.println("Problem unraring the file");
        }
    }

    /**
	* This gets rid of the temp directory
	*/
    public void removeTempDir() {
        Runtime r = Runtime.getRuntime();
        Process p;
        switch(theApp.utils.checkOS()) {
            case 0:
                theApp.fileStuff.delDir(new File(workingDir + System.getProperty("file.separator") + tempDirName));
                theApp.fileStuff.setTempDirFlag(false);
                break;
            case 1:
            case 2:
                theApp.fileStuff.delDir(new File(System.getProperty("user.home") + System.getProperty("file.separator") + tempDirName));
                theApp.fileStuff.setTempDirFlag(false);
                break;
        }
    }

    /**
	* Method to get filename using the fileFinder
	* @return 0 = no file chosen; 1 = file chosen
	*/
    private int getCBFile() {
        JFileChooser fileFinder = new JFileChooser();
        fileFinder.setCurrentDirectory(new File(System.getProperty("user.home")));
        fileFinder.setMultiSelectionEnabled(false);
        fileFinder.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileFinder.setFileFilter(new cbFileFilter());
        int result = fileFinder.showOpenDialog(theApp.frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            fileEntries.clear();
            fileEntries.trimToSize();
            fileEntries.add(fileFinder.getSelectedFile().getPath());
            fileEntries.add(fileFinder.getSelectedFile().getName());
            fileEntries.add(fileFinder.getSelectedFile().getParent());
            fileEntries.add(fileFinder.getSelectedFile());
            theApp.fileStuff.setArchiveDir(fileFinder.getSelectedFile().getParent());
            return 1;
        } else {
            return 0;
        }
    }

    /**
	* Handles rar files in linux
	* @param filename path to the archive with file
	* @param fileToWorkOn filename alone no path
	* @param r Runtime for this program
	* @param testProc process to use for native commands
	*/
    private void handleRarLinux(String filename, String fileToWorkOn, Runtime r, Process testProc) {
        System.out.println(filename);
        System.out.println(fileToWorkOn);
        boolean wrongFile = true;
        try {
            ZipFile testme = new ZipFile(filename);
        } catch (ZipException e) {
            wrongFile = false;
        } catch (IOException e) {
            System.err.println("Problem testing cbr file: " + e);
        }
        if (wrongFile == true) {
            inZip = true;
            openArchive(1, filename);
            return;
        }
        File tdir = new File(System.getProperty("user.home") + System.getProperty("file.separator") + tempDirName);
        tdir.mkdir();
        theApp.fileStuff.setTempDirFlag(true);
        if (filename.indexOf(" ") != -1 || filename.indexOf("#") != -1) {
            filename = "\"" + filename + "\"";
            createShellScript(filename, 0, workingDir, tempDirName);
            System.out.println("fileToWorkOn " + fileToWorkOn + "workingDir " + workingDir + "tempDirName " + tempDirName);
        } else {
            try {
                testProc = r.exec("unrar e -y " + filename, null, new File(System.getProperty("user.home") + System.getProperty("file.separator") + tempDirName));
                InputStream stdin = testProc.getInputStream();
                InputStreamReader isr = new InputStreamReader(stdin);
                BufferedReader br = new BufferedReader(isr);
                String lineIn = null;
                while ((lineIn = br.readLine()) != null) {
                    System.out.println(lineIn);
                }
                testProc.waitFor();
            } catch (InterruptedException e) {
                System.err.println("Problem unraring the archive" + e);
            } catch (IOException e) {
                System.err.println("IOException: " + e);
            }
        }
        workingDir = System.getProperty("user.home");
    }

    /**
	* Creates a shell script for the file extraction
	* @param filename filename to be extracted
	* @param fileType 0 = rar file, 1 = zip file
	* @param workDir directory script will be going into
	* @param temporaryDirectory directory script will extract file to
	*/
    public void createShellScript(String filename, int fileType, java.lang.String workDir, String temporaryDirectory) {
        String theCommand = " ";
        if (fileType == 0) {
            theCommand = "unrar e -y " + filename;
            System.out.println(theCommand);
        }
        PrintWriter shellScript = null;
        try {
            shellScript = new PrintWriter(new FileOutputStream(System.getProperty("user.home") + File.separatorChar + tempDirName + File.separatorChar + "tempscript.sh"));
            shellScript.println(theCommand);
            shellScript.close();
        } catch (FileNotFoundException exception) {
            System.err.println("Problem opening the shell script");
        }
        Runtime theRuntime = Runtime.getRuntime();
        Process p;
        try {
            p = theRuntime.exec("sh tempscript.sh", null, new File(System.getProperty("user.home") + File.separatorChar + tempDirName));
            InputStream stdin = p.getInputStream();
            InputStreamReader isr = new InputStreamReader(stdin);
            BufferedReader br = new BufferedReader(isr);
            String lineIn = null;
            while ((lineIn = br.readLine()) != null) {
                System.out.println(lineIn);
            }
            p.waitFor();
        } catch (IOException exception) {
            System.err.println("problem executing script");
        } catch (InterruptedException exception) {
            System.err.println("problem executing shell script");
        }
        try {
            p = theRuntime.exec("rm tempscript.sh", null, new File(System.getProperty("user.home") + File.separatorChar + tempDirName));
            p.waitFor();
        } catch (IOException exception) {
            System.err.println("problem removing script");
        } catch (InterruptedException exception) {
            System.err.println("problem removing the shell script");
        }
        theApp.fileStuff.setTempDirFlag(true);
    }

    /**
	* Closes an archive
	* @param evt The ActionEvent for this handler
	*/
    protected void closeArchive(java.awt.event.ActionEvent evt) {
        if (theApp.fileStuff.getTempDirFlag() == true) {
            removeTempDir();
            theApp.thePage.setDefault();
        } else {
            if (theApp.fileStuff.getZipFlag() == true) {
                theApp.fileStuff.setZipFlag(false);
                theApp.thePage.setDefault();
            } else {
                System.err.println("There is no archive to close\n");
            }
        }
        theApp.controls.disableButtons();
        theApp.menu.jumpMenu.clearMenu();
        theApp.menu.enableCloseArchive(false);
    }

    private CBViewer theApp;

    private boolean inZip = false;

    private CBSortStuff sortStuff = new CBSortStuff();

    private String pictureDir;

    protected String tempDirName = ".cbtemp";

    private String workingDir;

    private ArrayList fileEntries = new ArrayList();
}
