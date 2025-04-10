package com.g2inc.scap.library.domain.bundle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import com.g2inc.scap.library.domain.LibraryConfiguration;
import com.g2inc.scap.library.domain.SCAPValidationResult;
import com.g2inc.scap.library.util.CommonUtil;

public class BundleValidator implements Runnable {

    @Override
    public void run() {
        validate(useCase);
    }

    private static final Logger LOG = Logger.getLogger(BundleValidator.class);

    private SCAPDocumentBundle bundle;

    private UseCase useCase;

    public BundleValidator(SCAPDocumentBundle bundle, UseCase useCase) {
        this.bundle = bundle;
        this.useCase = useCase;
    }

    public SCAPValidationResult validate(UseCase useCase) {
        LOG.debug("Bundle validation started - usecase " + useCase.toString());
        SCAPValidationResult result = new SCAPValidationResult();
        ArrayList<String> errorList = new ArrayList<String>();
        result.setValidationMessageList(errorList);
        File zipFile = null;
        String tempDirName = System.getProperty("java.io.tmpdir");
        File tempDir = new File(tempDirName);
        try {
            if (bundle.getBundleType() != SCAPBundleType.ZIP) {
                zipFile = new File(tempDir, "tempBundle.zip");
                bundle.saveAsZipFile(zipFile);
            } else {
                File srcZipFile = new File(bundle.getFilename());
                zipFile = new File(tempDir, srcZipFile.getName());
                CommonUtil.copyFile(srcZipFile, zipFile);
            }
        } catch (IOException e) {
            String errorMessage = "Validation could not create zip file " + zipFile.getAbsolutePath();
            LOG.error(errorMessage, e);
            addToCompletion(result, errorMessage, null);
            return result;
        }
        File scapvalDir = LibraryConfiguration.getInstance().getScapValDir();
        if (!scapvalDir.exists() || !scapvalDir.isDirectory()) {
            String errorMessage = "Could not find scapval directory " + scapvalDir.getAbsolutePath();
            LOG.error(errorMessage);
            addToCompletion(result, errorMessage, null);
            return result;
        }
        LOG.debug("Preparing to validate; scapval directory: " + scapvalDir.getAbsolutePath());
        String scapvalJar = getScapvalJar(scapvalDir);
        if (scapvalJar == null) {
            String errorMessage = "Could not find scapval jar in directory " + scapvalDir.getAbsolutePath();
            LOG.error(errorMessage);
            addToCompletion(result, errorMessage, null);
            return result;
        }
        LOG.debug("Preparing to validate; scapval jar file: " + scapvalJar);
        File scapvalHtml = null;
        File scapvalStdout = null;
        try {
            scapvalHtml = File.createTempFile("scapvalResult", ".html");
            result.setHtmlFile(scapvalHtml);
            scapvalHtml.deleteOnExit();
            scapvalStdout = File.createTempFile("scapvalStdout", ".txt");
            scapvalStdout.deleteOnExit();
            result.setScapvalStdout(scapvalStdout);
        } catch (IOException e1) {
            String errorMessage = "Could not create temp files for html results and/or scapval stdout " + scapvalDir.getAbsolutePath();
            LOG.error(errorMessage);
            addToCompletion(result, errorMessage, null);
            return result;
        }
        List<String> command = new ArrayList<String>();
        command.add("java.exe");
        command.add("-jar");
        command.add(scapvalJar);
        command.add("-file");
        command.add(zipFile.getAbsolutePath());
        command.add("-usecase");
        command.add(useCase.toString());
        if (LibraryConfiguration.getInstance().isOnline()) {
            command.add("-online");
        }
        command.add("-result");
        command.add(scapvalHtml.getAbsolutePath());
        if (LOG.isDebugEnabled()) {
            LOG.debug("scapval command:");
            showCommand(command);
        }
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(scapvalDir);
        builder.redirectErrorStream(true);
        LOG.debug("Starting scapval process");
        Process process = null;
        try {
            process = builder.start();
            LOG.debug("Started scapval process");
        } catch (IOException e1) {
            String errorMessage = "IO error starting scapval process";
            LOG.error(errorMessage, e1);
            addToCompletion(result, errorMessage + e1.getMessage(), null);
            return result;
        }
        LOG.debug("Instantiating and starting stdout/stderr capture thread");
        StreamGobbler gobbler = new StreamGobbler(process.getInputStream(), scapvalStdout);
        gobbler.start();
        LOG.debug("Waiting for scapval process to complete");
        int exitVal = -1;
        try {
            exitVal = process.waitFor();
        } catch (InterruptedException e1) {
        }
        LOG.debug("scapval process completed, return code: " + exitVal);
        return result;
    }

    private void addToCompletion(SCAPValidationResult result, String message, Exception e) {
        StringBuilder sb = new StringBuilder();
        if (result.getValidationCompletion() != null) {
            sb.append(result.getValidationCompletion());
            sb.append("\n");
        }
        sb.append(message);
        result.setValidationCompletion(sb.toString());
        if (e != null) {
            LOG.error(message, e);
        } else {
            LOG.error(message);
        }
    }

    private String getScapvalJar(File scapvalDir) {
        File scapvalLibDir = new File(scapvalDir, "lib");
        if (!scapvalLibDir.exists() || !scapvalLibDir.isDirectory()) {
            LOG.error("Could not find scapval lib directory " + scapvalLibDir.getAbsolutePath());
            return null;
        }
        String[] scapvalLibJars = scapvalLibDir.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return (name.startsWith("scapval") && name.endsWith(".jar"));
            }
        });
        if (scapvalLibJars == null || scapvalLibJars.length == 0) {
            return null;
        }
        if (scapvalLibJars.length > 1) {
            LOG.warn("Using first of " + scapvalLibJars.length + " candidates for scapval jar from directory " + scapvalLibDir.getAbsolutePath());
        }
        return "lib" + File.separator + scapvalLibJars[0];
    }

    private void showCommand(List<String> command) {
        StringBuilder sb = new StringBuilder();
        for (String str : command) {
            sb.append(str);
            sb.append(" ");
        }
        LOG.debug(sb.toString());
    }

    class StreamGobbler extends Thread {

        InputStream is;

        File outFile;

        StreamGobbler(InputStream is, File outFile) {
            this.is = is;
            this.outFile = outFile;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                PrintWriter pw = new PrintWriter(outFile);
                String line = null;
                while ((line = br.readLine()) != null) {
                    pw.println(line);
                }
                pw.flush();
                pw.close();
                br.close();
            } catch (IOException e) {
                LOG.error("Error copying scapval output stream to file: " + outFile.getAbsolutePath(), e);
            }
        }
    }
}
