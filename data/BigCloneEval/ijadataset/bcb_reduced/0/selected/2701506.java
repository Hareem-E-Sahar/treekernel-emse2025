package net.sf.aoscat.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.zip.GZIPOutputStream;
import net.sf.aoscat.exceptions.CErrorException;
import net.sf.aoscat.i18n.EErrorMessages;
import net.sf.aoscat.utils.progress.CConsoleProgressBar;
import org.apache.log4j.Logger;

/**
 * Utility class to deal with file-specific issues.
 * @author dreichelt
 *
 */
public class CFileUtils {

    private static final transient Logger logger = Logger.getLogger(CFileUtils.class);

    private String currentRoot;

    private String targetRoot;

    private final CStringTransformer stringTransformer = new CStringTransformer();

    private ILineTransformer xmlPreprocessor = new CXMLPreprocessor();

    /**
	 * This method may be used to recursively delete a directory and all its contents.
	 * It should behave like 'rm -rf'.
	 * @param aDir the directory to remove
	 * @throws CErrorException in case something could not be deleted
	 */
    public void recursiveDelete(File aDir) throws CErrorException {
        if (aDir == null || !aDir.exists() || !aDir.isDirectory()) {
            throw new RuntimeException("Implementation error! " + aDir + " invalid");
        }
        for (File vChild : aDir.listFiles()) {
            if (vChild.isFile()) {
                if (!vChild.delete()) {
                    throw new CErrorException(EErrorMessages.COULDNOTDELETE, vChild.getAbsolutePath());
                }
            } else if (vChild.isDirectory()) {
                recursiveDelete(vChild);
            }
        }
        logger.debug("Deleting " + aDir);
        if (!aDir.delete()) {
            throw new CErrorException(EErrorMessages.COULDNOTDELETE, aDir.getAbsolutePath());
        }
    }

    private String getTargetFilenameFor(String absolutePath) {
        return absolutePath.replaceAll(stringTransformer.doubleEscape(currentRoot), stringTransformer.doubleEscape(targetRoot));
    }

    /**
	 * Preprocesses a single XML file, stripping it of invalid characters.
	 * @param aXMLPath the path of the XML file to preprocess
	 * @param aTargetXMLPath the target path of the preprocessed file created in this step
	 * @param aPreprocessors the list of the preprocessors to use for this task
	 * @return
	 */
    private boolean preprocessXMLFile(String aXMLPath, String aTargetXMLPath, ILineTransformer... aPreprocessors) {
        logger.debug("=========== Preprocessing " + aXMLPath);
        try {
            transformTemplate(aXMLPath, aTargetXMLPath, "UTF-8", aPreprocessors);
        } catch (CErrorException e1) {
            logger.fatal("Could not preprocess the XML File!", e1);
            return false;
        }
        logger.debug("=========== DONE Preprocessing ");
        return true;
    }

    /**
	 * Provides an empty directory by deleting any existing directory at that location
	 * if necessary and then recreating the directory.
	 * @param vPath the full path to provide as an empty directory
	 * @throws CErrorException in case anything could not be deleted
	 */
    public void provideCleanedDirectory(String vPath) throws CErrorException {
        logger.info("Emptying work directory '" + vPath + "'");
        System.gc();
        File vRoot = new File(vPath);
        if (vRoot.exists()) {
            recursiveDelete(vRoot);
        }
        createDirectoryIfNotExistent(vPath);
    }

    /**
	 * Creates a requested directory if it does not yet exist.
	 * @param aPath the full path of the directory to create
	 * @throws CErrorException in case the new directory could not be created
	 */
    public void createDirectoryIfNotExistent(String aPath) throws CErrorException {
        EErrorMessages vFolderStatus = checkFolder(aPath);
        logger.debug(aPath + " : " + vFolderStatus);
        File vTargetFile = new File(aPath);
        createParentDirectoriesIfNotExistent(vTargetFile);
        if (vFolderStatus == EErrorMessages.FOLDERDOESNTEXIST) {
            logger.debug("Creating work directory '" + aPath + "'");
            if (!new File(aPath).mkdir()) {
                throw new CErrorException(EErrorMessages.COULDNOTCREATEFOLDER, aPath);
            }
        }
    }

    /**
	 * Small helper method to recursively create all required parent folders of the path handed over.
	 * @param aFile the file whose parent directory to create
	 * @throws CErrorException in case the parent directory could not be detected
	 */
    private void createParentDirectoriesIfNotExistent(File aFile) throws CErrorException {
        LinkedList<File> vFilesToCreate = new LinkedList<File>();
        File vParentDir = aFile.getParentFile();
        while (!vParentDir.exists()) {
            vFilesToCreate.add(vParentDir);
            vParentDir = vParentDir.getParentFile();
            if (vParentDir == null) {
                throw new CErrorException(EErrorMessages.COULDNOTGETPARENTDIRECTORY, vFilesToCreate.getLast().getPath());
            }
        }
        while (vFilesToCreate.size() > 0) {
            File vUpmostFile = vFilesToCreate.pollLast();
            logger.info("Creating " + vUpmostFile.getAbsolutePath());
            if (!vUpmostFile.mkdir()) {
                throw new CErrorException(EErrorMessages.COULDNOTCREATEFOLDER, vUpmostFile.getAbsolutePath());
            }
        }
    }

    /**
	 * Small and simple helper method to check whether a given folder exists.
	 * 
	 * @param aPath
	 *            the path of the folder to check
	 * @return EErrorMessages.OK if everything's fine, else an error message
	 */
    public EErrorMessages checkFolder(String aPath) {
        File vRootFolder = new File(aPath);
        if (aPath == null) {
            return EErrorMessages.NOPATHGIVEN;
        }
        ;
        if (!vRootFolder.exists()) {
            return EErrorMessages.FOLDERDOESNTEXIST;
        }
        if (!vRootFolder.isDirectory()) {
            return EErrorMessages.NOTAFOLDER;
        }
        return EErrorMessages.OK;
    }

    /**
	 * This method performs a transformation of a given template by reading from the template
	 * file path and writing to the 'filePath', calling the transformHandler method at every line to
	 * perform line-by-line transformations.
	 * @param templatePath the path of the template file
	 * @param filePath the output file path
	 * @param aCharset the charset to write to the output file with
	 * @param aPreprocessors the handlers for the read template line - NOTE that the order of the line transformers is significant!
	 * @throws CErrorException in case anything goes wrong
	 */
    public void transformTemplate(String templatePath, String filePath, String aCharset, ILineTransformer... aPreprocessors) throws CErrorException {
        if (aPreprocessors.length < 1) {
            throw new CErrorException(EErrorMessages.UNEXPECTEDERROR, "Call this method only with preprocessors!");
        }
        File templateFile = new File(templatePath);
        if (!templateFile.exists()) {
            this.createDefaultTemplateFile(templateFile);
        }
        logger.debug(String.format("================ Transforming %s -> %s", templateFile.getPath(), filePath));
        BufferedReader vRead = null;
        BufferedWriter vWrite = null;
        File vOutFile = null;
        try {
            vRead = new BufferedReader(new FileReader(templateFile));
            vOutFile = new File(filePath);
            logger.debug("Creating new File '" + vOutFile.getAbsolutePath() + "'");
            if (!vOutFile.createNewFile()) {
                throw new IOException("Unable to create new file " + vOutFile.getAbsolutePath());
            }
            vWrite = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(vOutFile), aCharset));
            StringBuilder vBuffer = new StringBuilder();
            for (String vLineIn = vRead.readLine(); vLineIn != null; vLineIn = vRead.readLine()) {
                vBuffer.delete(0, vBuffer.length());
                vBuffer.append(vLineIn);
                for (ILineTransformer vPreprocessor : aPreprocessors) {
                    vPreprocessor.handle(vBuffer);
                }
                vWrite.write(vBuffer.toString() + "\n");
            }
        } catch (IOException e) {
            throw new CErrorException(EErrorMessages.READWRITEERROR, e, templatePath, filePath);
        } finally {
            try {
                if (vRead != null) {
                    vRead.close();
                }
            } catch (IOException e) {
                logger.error("", new CErrorException(EErrorMessages.COULDNOTCLOSEFILE, e, templatePath));
            }
            try {
                if (vWrite != null) {
                    vWrite.close();
                }
            } catch (IOException e) {
                logger.error("", new CErrorException(EErrorMessages.COULDNOTCLOSEFILE, e, vOutFile.getAbsolutePath()));
            }
        }
        logger.debug("================ DONE");
    }

    /**
	 * This method performs a transformation of a given template by reading from the template
	 * file path and writing to the input stream via a piped outputstream, calling the transformHandler method at every line to
	 * perform line-by-line transformations.the piped inputstream to write to
	 * @param aOutStream the piped outputstream to write to
	 * @param aPathOfFileToPreprocess the path of the template file
	 * @param aInCharset the charset to use to read from the file
	 * @param aOutCharset the charset to write to the output stream with
	 * @param aPreprocessors the handlers for the read template line - NOTE that the order of the line transformers is significant!
	 * @throws CErrorException in case anything goes wrong
	 */
    public void readPreprocessedFileToOutputStream(OutputStream aOutStream, String aPathOfFileToPreprocess, String aInCharset, String aOutCharset, ILineTransformer... aPreprocessors) throws CErrorException {
        if (aPreprocessors.length < 1) {
            throw new CErrorException(EErrorMessages.UNEXPECTEDERROR, "Call this method only with preprocessors!");
        }
        File vInFile = new File(aPathOfFileToPreprocess);
        if (!vInFile.exists()) {
            this.createDefaultTemplateFile(vInFile);
        }
        BufferedReader vRead = null;
        BufferedWriter vWrite = null;
        try {
            FileInputStream vFileReader = new FileInputStream(vInFile);
            vRead = new BufferedReader(new InputStreamReader(vFileReader, aInCharset));
            vWrite = new BufferedWriter(new OutputStreamWriter(aOutStream, aOutCharset));
            StringBuilder vBuffer = new StringBuilder();
            for (String vLineIn = vRead.readLine(); vLineIn != null; vLineIn = vRead.readLine()) {
                vBuffer.setLength(0);
                vBuffer.append(vLineIn);
                for (ILineTransformer vPreprocessor : aPreprocessors) {
                    vPreprocessor.handle(vBuffer);
                }
                vWrite.append(vBuffer);
                vWrite.newLine();
            }
        } catch (IOException e) {
            throw new CErrorException(EErrorMessages.READWRITEERROR, e, aPathOfFileToPreprocess, "Outputstream");
        } finally {
            try {
                if (vRead != null) {
                    vRead.close();
                }
            } catch (IOException e) {
                throw new CErrorException(EErrorMessages.COULDNOTCLOSEFILE, e, aPathOfFileToPreprocess);
            }
            try {
                if (vWrite != null) {
                    vWrite.close();
                }
            } catch (IOException e) {
                throw new CErrorException(EErrorMessages.COULDNOTPREPROCESS, e, aPathOfFileToPreprocess);
            }
        }
    }

    /**
	 * Small helper method to copy the default template file to the config folder.
	 * @param templateFile the template file to create
	 * @throws CErrorException in case the default template file cannot be copied (out of the JAR)
	 */
    public void createDefaultTemplateFile(File templateFile) throws CErrorException {
        logger.warn("Default template '" + templateFile.getAbsolutePath() + "' not yet existing, creating!");
        this.createDirectoryIfNotExistent(templateFile.getParentFile().getAbsolutePath());
        InputStream vIn = new BufferedInputStream(this.getClass().getClassLoader().getResourceAsStream(templateFile.getName()));
        BufferedOutputStream vOut = null;
        try {
            vOut = new BufferedOutputStream(new FileOutputStream(templateFile));
            while (true) {
                int datum = vIn.read();
                if (datum == -1) break;
                vOut.write(datum);
            }
            vOut.flush();
        } catch (FileNotFoundException e) {
            throw new CErrorException(EErrorMessages.COULDNOTOPENFILEFORWRITING, e, templateFile.getAbsolutePath());
        } catch (IOException e) {
            throw new CErrorException(EErrorMessages.READWRITEERROR, e, templateFile.getName(), templateFile.getAbsolutePath());
        } finally {
            try {
                vIn.close();
            } catch (IOException e) {
                logger.error(new CErrorException(EErrorMessages.COULDNOTCLOSEFILE, e, " JAR Resource Stream"));
            }
            if (vOut != null) {
                try {
                    vOut.close();
                } catch (IOException e) {
                    throw new CErrorException(EErrorMessages.COULDNOTCLOSEFILE, e, templateFile.getAbsolutePath());
                }
            }
        }
    }

    /**
	 * Small helper method to test whether an executable file exists.
	 * @param aPath the path of the file to check
	 * @return whether the file exists and is executable
	 */
    public boolean existsAndIsExecutable(String aPath) {
        File vFile = new File(aPath);
        return vFile != null && vFile.exists() && vFile.canExecute();
    }

    /**
	 * Deletes a file if it exists
	 * @param file the file to delete
	 * @return whether the file is not existing anymore
	 */
    @Deprecated
    public boolean deleteFileIfExists(File file) {
        if (file.exists()) {
            return file.delete();
        }
        return true;
    }

    /**
	 * This method recursively preprocesses XML files in a directory, removing invalid characters
	 * and optionally calling an additional linetransformer (e.g. to counteract faulty XML files).
	 * @param aDir the directory containing the XML files before preprocessing
	 * @param aDestinationRoot the root directory to store the preprocessed XML files in
	 * @param aFirstCall when calling externally please use 'true' in order to distinguish from later recursive calls
	 * @param aAdditionalTransformer an additional line transformer to apply when doing the transformation
	 * @throws CErrorException in case the preprocessing faileds
	 */
    public void recursivePreprocessXML(File aDir, File aDestinationRoot, boolean aFirstCall, ILineTransformer aAdditionalTransformer) throws CErrorException {
        if (aDir == null || !aDir.exists() || !aDir.isDirectory()) {
            throw new CErrorException(EErrorMessages.UNEXPECTEDERROR, "Implementation error! " + aDir + " invalid");
        }
        this.createDirectoryIfNotExistent(aDestinationRoot.getAbsolutePath());
        if (aFirstCall) {
            currentRoot = aDir.getAbsolutePath();
            targetRoot = aDestinationRoot.getAbsolutePath();
        }
        logger.debug("Preprocessing Files in " + aDir.getPath());
        CConsoleProgressBar vProgress = null;
        if (aFirstCall) {
            int vSubDirs = aDir.listFiles().length;
            vProgress = new CConsoleProgressBar(20, vSubDirs);
        }
        for (File vChild : aDir.listFiles()) {
            if (vChild.isFile()) {
                if (!this.preprocessXMLFile(vChild.getAbsolutePath(), getTargetFilenameFor(vChild.getAbsolutePath()), xmlPreprocessor, aAdditionalTransformer)) {
                    throw new CErrorException(EErrorMessages.COULDNOTPREPROCESS, aDir.getAbsolutePath());
                }
            } else if (vChild.isDirectory()) {
                recursivePreprocessXML(vChild, new File(getTargetFilenameFor(vChild.getAbsolutePath())), false, aAdditionalTransformer);
            }
            if (aFirstCall) {
                vProgress.tick();
            }
        }
    }

    /**
	 * Stores the given string in a file, overwriting the file.
	 * @param aData the string to store in the file
	 * @param aPath the filename to store the string in
	 * @throws IOException in case that did not work
	 */
    public void saveStringToFile(String aData, String aPath) throws IOException {
        File vFile = new File(aPath);
        FileOutputStream vStream;
        if (!vFile.exists()) {
            if (!vFile.createNewFile()) {
                throw new IOException("Could not create new file " + vFile);
            }
        } else {
            if (!vFile.delete()) {
                throw new IOException("Could not delete file " + vFile);
            }
            if (!vFile.createNewFile()) {
                throw new IOException("Could not create new file " + vFile);
            }
        }
        vStream = new FileOutputStream(vFile);
        try {
            vStream.write(aData.getBytes());
        } finally {
            vStream.close();
        }
    }

    /**
	 * Copies the given file to the given directory.
	 * @param aFile the file to copy
	 * @param aDestDir the path of the destination directory
	 * @return a file object referencing the newly created file
	 * @throws CErrorException in case the file could not be copied there
	 */
    public File copyFileToDir(File aFile, String aDestDir) throws CErrorException {
        File vRet = new File(aDestDir + aFile.getName());
        createDirectoryIfNotExistent(aDestDir);
        if (vRet.exists()) {
            if (!vRet.delete()) {
                throw new CErrorException(EErrorMessages.COULDNOTDELETE, vRet.getAbsolutePath());
            }
        }
        try {
            vRet.createNewFile();
        } catch (IOException e) {
            throw new CErrorException(EErrorMessages.COULDNOTCREATEFILE, e, vRet.getAbsolutePath());
        }
        BufferedInputStream vIn;
        BufferedOutputStream vOut;
        try {
            vIn = new BufferedInputStream(new FileInputStream(aFile));
        } catch (FileNotFoundException e) {
            throw new CErrorException(EErrorMessages.COULDNOTOPEN, e, aFile.getAbsolutePath());
        }
        try {
            vOut = new BufferedOutputStream(new FileOutputStream(vRet));
        } catch (FileNotFoundException e) {
            throw new CErrorException(EErrorMessages.COULDNOTOPENFILEFORWRITING, e, vRet.getAbsolutePath());
        }
        try {
            CStreamUtils.streamCopy(vIn, vOut);
        } catch (IOException e) {
            throw new CErrorException(EErrorMessages.READWRITEERROR, e, aFile.getAbsolutePath(), vRet.getAbsolutePath());
        }
        return vRet;
    }

    /**
	 * Copies a given file to another file.
	 * @param aSrc the source
	 * @param aDest destination
	 * @throws CErrorException in case the file could not be copied
	 */
    public void copyFile(File aSrc, File aDest) throws CErrorException {
        BufferedInputStream vIn;
        try {
            vIn = new BufferedInputStream(new FileInputStream(aSrc));
        } catch (FileNotFoundException e) {
            throw new CErrorException(EErrorMessages.COULDNOTOPEN, e, aSrc.getAbsolutePath());
        }
        copyStreamToFile(vIn, aDest);
    }

    /**
	 * Compresses the given file to a given out file using GZip.
	 * @param aIn file to compress
	 * @param aOut file to compress to
	 * @throws CErrorException in case an IO Exception occurs
	 */
    public void archiveFile(File aIn, File aOut) throws CErrorException {
        createDirectoryIfNotExistent(aOut.getParentFile().getAbsolutePath());
        GZIPOutputStream out = null;
        FileInputStream in = null;
        try {
            out = new GZIPOutputStream(new FileOutputStream(aOut));
            in = new FileInputStream(aIn);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            throw new CErrorException(EErrorMessages.READWRITEERROR, e, aIn.getAbsolutePath(), aOut.getAbsolutePath());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                logger.error("", e);
            }
            if (out != null) {
                try {
                    out.finish();
                    out.close();
                } catch (IOException e) {
                    logger.error("", e);
                }
            }
        }
    }

    /**
	 * recursively walks a given directory and calls the file callback listener for every file / directory.
	 * @param aRootDirectory the root directory where to start
	 * @param vListener the listener to notify upon the encounter of a file 
	 * @throws CErrorException in case not everything could be walked (e.g. because root dir does not exist)
	 */
    public void walkDirectory(File aRootDirectory, IFileCallback vListener) throws CErrorException {
        if (aRootDirectory == null || !aRootDirectory.exists()) {
            throw new CErrorException(EErrorMessages.UNEXPECTEDERROR, "Implementation error! " + aRootDirectory + " invalid");
        }
        if (aRootDirectory.isDirectory()) {
            for (File vChild : aRootDirectory.listFiles()) {
                walkDirectory(vChild, vListener);
            }
        }
        vListener.fileEncountered(aRootDirectory);
    }

    /**
	 * Reads a complete file (its contents) into a string.
	 * @param aFile the file to read
	 * @param aWriter the string writer to write the file contents to
	 */
    public void readFileToStringWriter(File aFile, StringWriter aWriter) {
        try {
            FileReader vFis = new FileReader(aFile);
            BufferedReader vIn = new BufferedReader(vFis);
            final char[] buffer = new char[1024 * 512];
            int vRead = vIn.read(buffer);
            while (vRead > 0) {
                aWriter.write(buffer, 0, vRead);
                vRead = vIn.read(buffer);
            }
            vIn.close();
        } catch (FileNotFoundException e) {
            logger.error("", e);
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    /**
	 * Copies a given stream to a given file, creating the file if necessary.
	 * @param vData the data stream to write
	 * @param aDest the file object to write tos
	 * @throws CErrorException in case the file could not be written to easily
	 */
    public void copyStreamToFile(InputStream vData, File aDest) throws CErrorException {
        createDirectoryIfNotExistent(aDest.getParentFile().getAbsolutePath());
        if (aDest.exists()) {
            if (!aDest.delete()) {
                throw new CErrorException(EErrorMessages.COULDNOTDELETE, aDest.getAbsolutePath());
            }
        }
        try {
            aDest.createNewFile();
        } catch (IOException e) {
            throw new CErrorException(EErrorMessages.COULDNOTCREATEFILE, e, aDest.getAbsolutePath());
        }
        BufferedOutputStream vOut;
        try {
            vOut = new BufferedOutputStream(new FileOutputStream(aDest));
        } catch (FileNotFoundException e) {
            throw new CErrorException(EErrorMessages.COULDNOTOPENFILEFORWRITING, e, aDest.getAbsolutePath());
        }
        try {
            CStreamUtils.streamCopy(vData, vOut);
        } catch (IOException e) {
            throw new CErrorException(EErrorMessages.READWRITEERROR, e, "", aDest.getAbsolutePath());
        }
    }

    /**
	 * Inefficient but convenient way to read the contents of a file to a string.
	 * @param aPath the path of the file to read
	 * @return the text content as string
	 * @throws IOException in case the file could not be read
	 */
    public String readStringFromFile(String aPath) throws IOException {
        File vFile = new File(aPath);
        if (!vFile.exists()) {
            throw new IOException("Could not read file " + vFile);
        }
        FileReader vReader = new FileReader(vFile);
        StringBuffer fileData = new StringBuffer(1000);
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = vReader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        vReader.close();
        return fileData.toString();
    }
}
