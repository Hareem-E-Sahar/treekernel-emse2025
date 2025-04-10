package agentgui.core.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.swing.SwingUtilities;
import agentgui.core.application.Application;

/**
 * This class can be used in order zip or unzip a folder structure.<br><br>
 * Example of the usage for zipping:<br>
 * <blockquote><code>
 * 		Zipper zipper = new Zipper();<br>
 * 		zipper.setExcludePattern(".svn");<br>
 * 		zipper.setZipFolder(zipFolder);<br>
 *		zipper.setZipSourceFolder(srcFolder);<br>
 *		zipper.doZipFolder();<br>
 * </code></blockquote>   
 * <br>
 * Example of the usage for unzipping:<br>
 * <blockquote><code> 
 * 		Zipper zipper = new Zipper();<br>
 * 		zipper.setUnzipZipFolder(zipFolder);<br>
 * 		zipper.setUnzipDestinationFolder(destFolder);<br>
 * 		zipper.doUnzipFolder();<br>
 * </code></blockquote>
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg - Essen
 */
public class Zipper extends Thread {

    private int kindOfExec = 0;

    private final int execZip = 1;

    private final int execUnZip = 2;

    private ZipperMonitor zipMonitor = null;

    private String excludePattern = null;

    private Vector<File> fileList = new Vector<File>();

    private String zipFolder = null;

    private String zipSourceFolder = null;

    private String unzipZipFolder = null;

    private String unzipDestinationFolder = null;

    private String projectFolder2Open = null;

    /**
	 * Constructor of this class
	 */
    public Zipper() {
        this.setName("Zipper");
        this.zipMonitor = new ZipperMonitor(Application.MainWindow);
    }

    /**
	 * Starts the execution of the zip- or unzip process in the current thread.<br>
	 * During the process a Zip-Monitor will be shown to the user.
	 */
    @Override
    public void run() {
        if (kindOfExec == execZip) {
            this.zipFolder(zipSourceFolder, zipFolder);
        } else if (kindOfExec == execUnZip) {
            this.unzipFolder(unzipZipFolder, unzipDestinationFolder);
        }
        this.zipMonitor.setVisible(false);
        if (this.projectFolder2Open != null) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    Application.Projects.add(projectFolder2Open);
                }
            });
        }
    }

    /**
	 * This exclude pattern can be used in oder to prevent some 
	 * types of files to be packed. We use this method to prevent
	 * zipping the svn-folder structure during the project export.
	 * 
	 * @param excludePattern the excludePattern to set
	 */
    public void setExcludePattern(String excludePattern) {
        this.excludePattern = excludePattern;
    }

    /**
	 * This method returns the current String, which is used for excluding files to be packed to the zip-file.
	 * @return the excludePattern
	 */
    public String getExcludePattern() {
        return this.excludePattern;
    }

    /**
	 * This method can be used in order to specify the zip-file, which has to be packed
	 * @param zipFolder the zip-folder to set
	 */
    public void setZipFolder(String zipFolder) {
        this.zipFolder = zipFolder;
    }

    /**
	 * Returns the path to the current zip-file for zipping a folder structure
	 * @return the zipZipFolder
	 */
    public String getZipFolder() {
        return this.zipFolder;
    }

    /**
	 * Define the source folder, which has to be packed here.
	 * @param zipSourceFolder the zipSourceFolder to set
	 */
    public void setZipSourceFolder(String zipSourceFolder) {
        this.zipSourceFolder = zipSourceFolder;
    }

    /**
	 * Get the current source folder for packing into a zip-file 
	 * @return the zipSourceFolder
	 */
    public String getZipSourceFolder() {
        return this.zipSourceFolder;
    }

    /**
	 * Specify the zip-File/Folder here, which has to unpacked
	 * @param zipFolder the zip-folder to unzip
	 */
    public void setUnzipZipFolder(String zipFolder) {
        this.unzipZipFolder = zipFolder;
    }

    /**
	 * Get the currently specified zipFile/Folder here 
	 * @return the zip-folder to unzip
	 */
    public String getUnzipZipFolder() {
        return this.unzipZipFolder;
    }

    /**
	 * Set the destination folder for unzipping a zip-File/Folder here
	 * @param unzipDestinationFolder the destination folder for unzipping
	 */
    public void setUnzipDestinationFolder(String unzipDestinationFolder) {
        this.unzipDestinationFolder = unzipDestinationFolder;
    }

    /**
	 * Get the current destination folder for unzipping here
	 * @return the unzipDestinationFolder
	 */
    public String getUnzipDestinationFolder() {
        return this.unzipDestinationFolder;
    }

    /**
	 * Specify the project-folder here, which has to be opened later on 
	 * @return the projectFolder2Open
	 */
    public String getProjectFolder2Open() {
        return projectFolder2Open;
    }

    /**
	 * Get the current project folder here, which has to be opened later on.
	 * @param projectFolder2Open the projectFolder2Open to set
	 */
    public void setProjectFolder2Open(String projectFolder2Open) {
        this.projectFolder2Open = projectFolder2Open;
    }

    /**
	 * This method will evaluate the zip-file in order to find the root-folder of the zip
	 * @return Returns the root folder name of the zip-file
	 */
    public String getRootFolder2Extract() {
        try {
            if (this.unzipZipFolder == null) {
                throw new Exception("Use 'setUnzipZipFolder(String)' to specify the zip-file to unzip");
            }
            if (this.unzipDestinationFolder == null) {
                throw new Exception("Use 'setUnzipDestinationFolder(String)' to specify the destination for unzipping");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.unzipZipFolder != null && this.unzipDestinationFolder != null) {
            String zipRootFolder = null;
            try {
                ZipFile zf = new ZipFile(this.unzipZipFolder);
                Enumeration<? extends ZipEntry> zipEnum = zf.entries();
                while (zipEnum.hasMoreElements()) {
                    ZipEntry item = (ZipEntry) zipEnum.nextElement();
                    String entryName = item.getName();
                    String fileSeperator = null;
                    if (entryName.contains("\\")) {
                        fileSeperator = "\\";
                    } else if (entryName.contains("/")) {
                        fileSeperator = "/";
                    }
                    int cut = entryName.indexOf(fileSeperator, 1);
                    zipRootFolder = entryName.substring(0, cut);
                    String[] zipRootFolderArr = zipRootFolder.split("|");
                    zipRootFolder = "";
                    for (int i = 0; i < zipRootFolderArr.length; i++) {
                        if (zipRootFolderArr[i].equals(fileSeperator)) {
                            zipRootFolderArr[i] = "";
                        }
                        zipRootFolder += zipRootFolderArr[i];
                    }
                    zipRootFolder.replace(fileSeperator, "");
                    zipRootFolder.trim();
                    break;
                }
                zf.close();
                return zipRootFolder;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            return null;
        }
    }

    /**
	 * Will do the unzipping of the previously specified zip-file.
	 * Afterwards the specified project will be opened.
	 * @param projectFolder
	 */
    public void doUnzipProject(String projectFolder) {
        this.setProjectFolder2Open(projectFolder);
        this.doUnzipFolder();
    }

    /**
	 * Will do the unzipping of the previously specified zip-file
	 * @throws Exception 
	 */
    public void doUnzipFolder() {
        try {
            if (this.unzipZipFolder == null) {
                throw new Exception("Use 'setUnzipZipFolder(String)' to specify the zip-file to unzip");
            }
            if (this.unzipDestinationFolder == null) {
                throw new Exception("Use 'setUnzipDestinationFolder(String)' to specify the destination for unzipping");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.unzipZipFolder != null && this.unzipDestinationFolder != null) {
            this.kindOfExec = execUnZip;
            this.start();
        }
    }

    /**
	* Will do the actual unzipping of a zip file. 
	* @param zipFolder the zip file that needs to be unzipped
	* @param destFolder the folder into which unzip the zip file and create the folder structure
	*/
    private void unzipFolder(String zipFolder, String destFolder) {
        try {
            ZipFile zf = new ZipFile(zipFolder);
            Enumeration<? extends ZipEntry> zipEnum = zf.entries();
            String dir = destFolder;
            this.zipMonitor.setNumberOfFilesMax(zf.size());
            this.zipMonitor.setProcessDescription(false, zipFolder);
            this.zipMonitor.setVisible(true);
            while (zipEnum.hasMoreElements()) {
                ZipEntry item = (ZipEntry) zipEnum.nextElement();
                String itemName = PathHandling.getPathName4LocalFileSystem(item.getName());
                if (item.isDirectory()) {
                    File newdir = new File(dir + File.separator + itemName);
                    newdir.mkdir();
                } else {
                    String newfilePath = dir + File.separator + itemName;
                    File newFile = new File(newfilePath);
                    if (!newFile.getParentFile().exists()) {
                        newFile.getParentFile().mkdirs();
                    }
                    zipMonitor.setNumberNextFile();
                    zipMonitor.setCurrentJobFile(newfilePath);
                    if (zipMonitor.isCanceled()) {
                        this.setProjectFolder2Open(null);
                        zf.close();
                        File destFile = new File(destFolder + this.getRootFolder2Extract() + File.separator);
                        if (destFile.isDirectory()) {
                            destFile.delete();
                        }
                        return;
                    }
                    InputStream is = zf.getInputStream(item);
                    FileOutputStream fos = new FileOutputStream(newfilePath);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    is.close();
                    fos.close();
                }
            }
            zf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
	 * This method can be invoked after the parameters are set for zipping a folder.<br>
	 * See example above.
	 */
    public void doZipFolder() {
        try {
            if (this.zipFolder == null) {
                throw new Exception("Use 'setZipFolder(String)' to specify the zip-file to zip");
            }
            if (this.zipSourceFolder == null) {
                throw new Exception("Use 'setZipSourceFolder(String)' to specify the destination for unzipping");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.zipFolder != null && this.zipSourceFolder != null) {
            this.kindOfExec = execZip;
            this.start();
        }
    }

    /**
	 * @param srcFolder: path to the folder to be zipped
	 * @param destZipFile: path to the final zip file
	 */
    private void zipFolder(String srcFolder, String destZipFile) {
        if (new File(srcFolder).isDirectory()) {
            this.evaluateFolder(srcFolder);
            this.zipMonitor.setNumberOfFilesMax(this.fileList.size());
            this.zipMonitor.setProcessDescription(true, srcFolder);
            this.zipMonitor.setVisible(true);
            ZipOutputStream zip = null;
            FileOutputStream fileWriter = null;
            try {
                fileWriter = new FileOutputStream(destZipFile);
                zip = new ZipOutputStream(fileWriter);
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
            addFolderToZip("", srcFolder, zip);
            try {
                zip.flush();
                zip.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (zipMonitor.isCanceled()) {
                File destFileZip = new File(destZipFile);
                destFileZip.delete();
            }
        }
    }

    /**
	 * Adds a single source file to the zip-file
	 * @param path
	 * @param srcFile
	 * @param zip
	 */
    private void addToZip(String path, String srcFile, ZipOutputStream zip) {
        File folder = new File(srcFile);
        if (folder.isDirectory()) {
            this.addFolderToZip(path, srcFile, zip);
        } else {
            try {
                boolean includeFile = false;
                if (this.excludePattern == null) {
                    includeFile = true;
                } else {
                    if (srcFile.contains(this.excludePattern)) {
                        includeFile = false;
                    } else {
                        includeFile = true;
                    }
                }
                if (includeFile == true) {
                    this.zipMonitor.setNumberNextFile();
                    this.zipMonitor.setCurrentJobFile(srcFile);
                    if (zipMonitor.isCanceled()) {
                        return;
                    }
                    FileInputStream in = new FileInputStream(srcFile);
                    zip.putNextEntry(new ZipEntry(path + File.separator + folder.getName()));
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        zip.write(buf, 0, len);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
	 * Adds a complete folder to the zip-file 
	 * @param path
	 * @param srcFolder
	 * @param zip
	 */
    private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) {
        File folder = new File(srcFolder);
        String listOfFiles[] = folder.list();
        try {
            for (int i = 0; i < listOfFiles.length; i++) {
                this.addToZip(path + File.separator + folder.getName(), srcFolder + File.separator + listOfFiles[i], zip);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
	 * This method will evaluate the given folder and it's sub-folder.
	 * The containing File objects will be stored in the Vector 'fileList'
	 * @param folder
	 */
    private void evaluateFolder(String srcFolder) {
        File folder = new File(srcFolder);
        String listOfFiles[] = folder.list();
        for (int i = 0; i < listOfFiles.length; i++) {
            boolean includeFile = false;
            if (excludePattern == null) {
                includeFile = true;
            } else {
                if (listOfFiles[i].contains(excludePattern)) {
                    includeFile = false;
                } else {
                    includeFile = true;
                }
            }
            if (includeFile == true) {
                File sngFileObject = new File(srcFolder + File.separator + listOfFiles[i]);
                if (sngFileObject.isDirectory()) {
                    this.evaluateFolder(sngFileObject.getAbsolutePath());
                } else {
                    this.fileList.add(sngFileObject);
                }
            }
        }
    }
}
