package com.ivy.code2web.entry;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.ivy.code2web.utils.FileManagement;
import com.ivy.code2web.utils.HTMLManagement;
import com.ivy.code2web.utils.HTMLManagementIndexFile;
import com.ivy.code2web.utils.JavaFileBean;

/**
 * @author vijay.kumar.ivaturi
 * 
 *         ConvertSource2Web is the main class that orchestrates the creation of
 *         html file based on java source code provided
 * 
 */
public class ConvertSource2Web {

    private String sourceDir;

    private String destinationDir;

    private List<String> sourceCodeList;

    private List<String> htmlCodeList;

    private List<JavaFileBean> javaFileList = new ArrayList<JavaFileBean>();

    private HTMLManagement htmlManagement;

    private HTMLManagementIndexFile htmlManagementIndex;

    /**
	 * @param sourceDirInp
	 * @param destinationDirInp
	 * 
	 *            Constructor to create set values for input and output files
	 */
    public ConvertSource2Web(String sourceDirInp, String destinationDirInp) {
        this.sourceDir = sourceDirInp;
        this.destinationDir = destinationDirInp;
    }

    /**
	 * startFileConversion is the method that converts a file into HTML
	 */
    public void startFileConversion() {
        FileManagement.startTime = System.currentTimeMillis();
        FileManagement.fetchAllFiles(new File(sourceDir), javaFileList);
        Collections.sort(javaFileList);
        for (JavaFileBean javaFile : javaFileList) {
            performConversion(javaFile);
        }
        htmlManagementIndex = new HTMLManagementIndexFile();
        htmlManagementIndex.createIndexHtmlFile(javaFileList, destinationDir);
        Desktop desktop = null;
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
            URI uri = null;
            try {
                uri = new URI("file://" + destinationDir.replace('\\', '/') + "/index.html");
                desktop.browse(uri);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
	 * Perform conversion of source code to html file by calling sequence of
	 * static methods present in utility classes
	 */
    public void performConversion(JavaFileBean javaFile) {
        String srcFileName = javaFile.getFileFullName();
        sourceCodeList = FileManagement.file2Array(srcFileName);
        String newDestination = destinationDir + srcFileName.substring(sourceDir.length(), srcFileName.lastIndexOf("java")) + "html";
        javaFile.setDestFullName(newDestination);
        if (sourceCodeList != null) {
            htmlManagement = new HTMLManagement();
            htmlManagement.prepareLanguageSyntaxArrays();
            htmlCodeList = htmlManagement.createHTMLArray(sourceCodeList, javaFile);
            if (htmlCodeList != null) {
                FileManagement.array2File(htmlCodeList, newDestination, javaFile.getFileClassName());
            }
        }
    }

    /**
	 * @param arg0
	 * @param arg1
	 * @return
	 * 
	 *         validateInputs method performs basic validation on user inputs
	 */
    public boolean validateInputs() {
        File srcDir = new File(sourceDir);
        File destDir = new File(destinationDir);
        if (sourceDir.equalsIgnoreCase("help") || sourceDir.equalsIgnoreCase("-help") || sourceDir.equalsIgnoreCase("h") || sourceDir.equalsIgnoreCase("-h")) {
            System.out.println("INFO : First argument should be a directory that has to be scanned for source code");
            System.out.println("INFO : Second argument should be a directory that where converted html code will be written");
            return false;
        } else if ((!srcDir.exists()) || (!srcDir.isDirectory())) {
            System.out.println("ERROR : Source path " + sourceDir + " is not a valid directory");
            return false;
        } else if ((!destDir.exists()) || (!destDir.isDirectory())) {
            System.out.println("ERROR : Destination path " + destinationDir + " is not a valid directory");
            return false;
        } else {
            return true;
        }
    }
}
