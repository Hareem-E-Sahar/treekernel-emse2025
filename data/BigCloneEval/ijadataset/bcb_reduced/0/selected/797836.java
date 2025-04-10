package org.cishell.utilities;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import javax.imageio.ImageIO;
import org.osgi.service.log.LogService;
import com.google.common.collect.ImmutableMap;

public class FileUtilities {

    public static final int READ_TEXT_FILE_BUFFER_SIZE = 1024;

    public static final String DEFAULT_STREAM_TO_FILE_NAME = "stream_";

    /** Don't bother with "file:text/foo" -> "foo", we can find those automatically. */
    public static final ImmutableMap<String, String> MIME_TYPE_TO_FILE_EXTENSION = new ImmutableMap.Builder<String, String>().put("file:application/pajekmat", "mat").put("file:application/pajeknet", "net").put("file:application/parvis", "stf").put("file:text/bibtex", "bib").put("file:text/compartmentalmodel", "mdl").put("file:text/coord", "nwb").put("file:text/grace", "grace.dat").put("file:text/intsim", "int").put("file:text/plain", "txt").put("file:text/plot", "plot.dat").put("file:text/referbib", "refer").build();

    public static File createTemporaryDirectory(String temporaryDirectoryPath) {
        String fullDirectoryPath = String.format("%s%stemp", temporaryDirectoryPath, File.separator);
        return ensureDirectoryExists(fullDirectoryPath);
    }

    /**
	 * Adapted from Google Guava.
	 * 
	 * This will attempt to return you a temp directory in Java's temp
	 * directory. This method assumes that the temporary volume is writable, has
	 * free inodes and free blocks, and that it will not be called thousands of
	 * times per second.
	 * 
	 * @param prefix
	 *            A string that will appear at the beginning of a directories
	 *            name.
	 */
    public static File createTempDirectory(String prefix) {
        final int TEMP_DIR_ATTEMPTS = 1000;
        File baseDir = new File(getDefaultTemporaryDirectory());
        String baseName = prefix + "-" + System.currentTimeMillis() + "-";
        for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
            File tempDir = new File(baseDir, baseName + counter);
            if (tempDir.mkdir()) {
                return tempDir;
            }
        }
        throw new IllegalStateException("Failed to create directory within " + TEMP_DIR_ATTEMPTS + " attempts (tried " + baseName + "0 to " + baseName + (TEMP_DIR_ATTEMPTS - 1) + ')');
    }

    public static File createTemporaryFile(File temporaryDirectory, String temporaryDirectoryPath, String temporaryFileName, String temporaryFileExtension) {
        ensureDirectoryExists(temporaryDirectoryPath);
        File temporaryFile;
        try {
            temporaryFile = File.createTempFile(temporaryFileName, "." + temporaryFileExtension, temporaryDirectory);
        } catch (IOException e) {
            temporaryFile = new File(temporaryDirectoryPath + File.separator + temporaryFileName + "temp." + temporaryFileExtension);
            if (!temporaryFile.exists()) {
                try {
                    temporaryFile.createNewFile();
                } catch (IOException e2) {
                    throw new RuntimeException(e2);
                }
                temporaryFile.deleteOnExit();
            }
        }
        return temporaryFile;
    }

    public static File createTemporaryFileInTemporaryDirectory(String temporaryDirectoryPath, String temporaryFileName, String temporaryFileExtension) throws IOException {
        File temporaryDirectory = createTemporaryDirectory(temporaryDirectoryPath);
        File temporaryFile = createTemporaryFile(temporaryDirectory, temporaryDirectoryPath, temporaryFileName, temporaryFileExtension);
        if (temporaryFile == null) {
            throw new IOException("Failed to generate a file in the temporary directory.");
        }
        return temporaryFile;
    }

    public static String getDefaultTemporaryDirectory() {
        return System.getProperty("java.io.tmpdir");
    }

    public static File createTemporaryFileInDefaultTemporaryDirectory(String temporaryFileName, String temporaryFileExtension) throws IOException {
        return createTemporaryFileInTemporaryDirectory(getDefaultTemporaryDirectory(), temporaryFileName, temporaryFileExtension);
    }

    public static File writeBufferedImageIntoTemporaryDirectory(BufferedImage bufferedImage, String imageType) throws IOException, Exception {
        String temporaryDirectoryPath = getDefaultTemporaryDirectory();
        File temporaryImageFile = createTemporaryFileInTemporaryDirectory(temporaryDirectoryPath, "image-", imageType);
        if (!ImageIO.write(bufferedImage, imageType, temporaryImageFile)) {
            throw new Exception("No valid image writer was found for the image type " + imageType);
        }
        return temporaryImageFile;
    }

    public static File writeTextIntoTemporaryDirectory(String text, String fileExtension) throws IOException, Exception {
        String temporaryDirectoryPath = getDefaultTemporaryDirectory();
        File temporaryTextFile = createTemporaryFileInTemporaryDirectory(temporaryDirectoryPath, "text-", fileExtension);
        FileWriter textFileWriter = new FileWriter(temporaryTextFile);
        textFileWriter.write(text);
        textFileWriter.flush();
        return temporaryTextFile;
    }

    public static boolean isFileEmpty(File file) throws FileNotFoundException, IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(file));
        String firstLine = reader.readLine();
        reader.close();
        boolean fileIsEmpty = (firstLine == null);
        return fileIsEmpty;
    }

    public static String readEntireTextFile(File file) throws IOException {
        return extractReaderContents(new BufferedReader(new FileReader(file)));
    }

    public static String readEntireInputStream(InputStream stream) throws IOException {
        return extractReaderContents(new BufferedReader(new InputStreamReader(stream)));
    }

    public static String extractReaderContents(BufferedReader bufferedReader) throws IOException {
        StringBuffer contents = new StringBuffer();
        char[] readInCharacters = new char[1];
        int readCharacterCount = bufferedReader.read(readInCharacters);
        while (readCharacterCount > -1) {
            contents.append(String.valueOf(readInCharacters));
            readCharacterCount = bufferedReader.read(readInCharacters);
        }
        bufferedReader.close();
        return contents.toString();
    }

    public static void copyFile(File sourceFile, File targetFile) throws FileCopyingException {
        try {
            FileInputStream inputStream = new FileInputStream(sourceFile);
            FileOutputStream outputStream = new FileOutputStream(targetFile);
            FileChannel readableChannel = inputStream.getChannel();
            FileChannel writableChannel = outputStream.getChannel();
            writableChannel.truncate(0);
            writableChannel.transferFrom(readableChannel, 0, readableChannel.size());
            inputStream.close();
            outputStream.close();
        } catch (IOException ioException) {
            String exceptionMessage = "An error occurred when copying from the file \"" + sourceFile.getAbsolutePath() + "\" to the file \"" + targetFile.getAbsolutePath() + "\".";
            throw new FileCopyingException(exceptionMessage, ioException);
        }
    }

    public static File createTemporaryFileCopy(File sourceFile, String fileName, String fileExtension) throws FileCopyingException {
        try {
            File temporaryTargetFile = createTemporaryFileInDefaultTemporaryDirectory(fileName, fileExtension);
            copyFile(sourceFile, temporaryTargetFile);
            return temporaryTargetFile;
        } catch (IOException temporaryFileCreationException) {
            String exceptionMessage = "An error occurred when trying to create the temporary file " + "with file name \"" + fileName + "\" " + "and file extension \"" + fileExtension + "\" " + "for copying file \"" + sourceFile.getAbsolutePath() + "\".";
            throw new FileCopyingException(exceptionMessage, temporaryFileCreationException);
        }
    }

    public static File loadFileFromClassPath(Class clazz, String filePath) {
        URL fileURL = clazz.getResource(filePath);
        return new File(URI.create(fileURL.toString()));
    }

    public static File safeLoadFileFromClasspath(Class clazz, String filePath) throws IOException {
        InputStream input = clazz.getResourceAsStream(filePath);
        String fileExtension = getFileExtension(filePath);
        return writeEntireStreamToTemporaryFile(input, fileExtension);
    }

    public static File writeEntireStreamToTemporaryFile(InputStream stream, String fileExtension) throws IOException {
        return writeEntireStreamToTemporaryFile(stream, DEFAULT_STREAM_TO_FILE_NAME, fileExtension);
    }

    public static File writeEntireStreamToTemporaryFile(InputStream input, String fileName, String fileExtension) throws IOException {
        File temporaryFile = createTemporaryFileInDefaultTemporaryDirectory(fileName, fileExtension);
        OutputStream output = new FileOutputStream(temporaryFile);
        byte[] readCharacters = new byte[1];
        int readCharacterCount = input.read(readCharacters);
        while (readCharacterCount > 0) {
            output.write(readCharacters, 0, readCharacterCount);
            readCharacterCount = input.read(readCharacters);
        }
        output.close();
        input.close();
        return temporaryFile;
    }

    public static File writeEntireStreamToTemporaryFileInDirectory(InputStream input, File directory, String fileName, String fileExtension) throws IOException {
        File temporaryFile = createTemporaryFile(directory, directory.getAbsolutePath(), fileName, fileExtension);
        return writeStreamToFile(input, temporaryFile);
    }

    /**
	 * Read the input stream into the output file and return the file.
	 */
    public static File writeStreamToFile(InputStream input, File outputFile) throws IOException {
        OutputStream output = new FileOutputStream(outputFile);
        byte[] readCharacters = new byte[1];
        int readCharacterCount = input.read(readCharacters);
        while (readCharacterCount > 0) {
            output.write(readCharacters, 0, readCharacterCount);
            readCharacterCount = input.read(readCharacters);
        }
        output.close();
        input.close();
        return outputFile;
    }

    public static String getFileExtension(File file) {
        return getFileExtension(file.getAbsolutePath());
    }

    public static String getFileExtension(String filePath) {
        int periodPosition = filePath.lastIndexOf(".");
        if ((periodPosition != -1) && ((periodPosition + 1) < filePath.length())) {
            return filePath.substring(periodPosition);
        } else {
            return "";
        }
    }

    private static File ensureDirectoryExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
            directory.deleteOnExit();
        }
        return directory;
    }

    public static final char FILENAME_CHARACTER_REPLACEMENT = '#';

    public static final Collection INVALID_FILENAME_CHARACTERS;

    static {
        Collection s = new HashSet();
        s.add(new Character('\\'));
        s.add(new Character('/'));
        s.add(new Character(':'));
        s.add(new Character('*'));
        s.add(new Character('?'));
        s.add(new Character('"'));
        s.add(new Character('<'));
        s.add(new Character('>'));
        s.add(new Character('|'));
        s.add(new Character('%'));
        INVALID_FILENAME_CHARACTERS = Collections.unmodifiableCollection(s);
    }

    private static int uniqueIntForTempFile = 1;

    public static File getTempFile(String fileName, String extension, LogService logger) {
        File tempFile;
        if (fileName == null || fileName.equals("")) {
            fileName = "unknown";
        }
        if (extension == null || extension.equals("")) {
            extension = ".txt";
        }
        if (!extension.startsWith(".")) {
            extension = "." + extension;
        }
        String tempPath = System.getProperty("java.io.tmpdir");
        File tempDir = new File(tempPath + File.separator + "nwb");
        if (!tempDir.exists()) tempDir.mkdir();
        try {
            tempFile = File.createTempFile(fileName, extension, tempDir);
        } catch (IOException e1) {
            logger.log(LogService.LOG_WARNING, "Failed to create temp file with provided name and extension '" + fileName + extension + "'. Trying a generic name and extension instead.", e1);
            try {
                tempFile = File.createTempFile("unknown", ".txt", tempDir);
            } catch (IOException e2) {
                tempFile = new File(tempPath + File.separator + "nwb" + File.separator + "unknown" + uniqueIntForTempFile + ".txt");
                uniqueIntForTempFile++;
                logger.log(LogService.LOG_ERROR, "Failed to create temp file twice...");
                logger.log(LogService.LOG_ERROR, "First Try... \r\n " + e1.toString());
                logger.log(LogService.LOG_ERROR, "Second Try... \r\n " + e2.toString());
            }
        }
        return tempFile;
    }

    public static String extractExtension(String format) {
        String extension = "";
        if (format.startsWith("file-ext:")) {
            extension = format.substring("file-ext:".length());
        } else if (format.startsWith("file:")) {
            if (MIME_TYPE_TO_FILE_EXTENSION.containsKey(format)) {
                extension = MIME_TYPE_TO_FILE_EXTENSION.get(format);
            } else {
                if (format.contains("/")) {
                    extension = format.substring(format.indexOf("/") + 1);
                } else {
                    extension = format.substring("file:".length());
                }
            }
        }
        extension = extension.replace('+', '.');
        return extension;
    }

    public static String replaceInvalidFilenameCharacters(String filename) {
        String cleanedFilename = filename;
        for (Iterator invalidCharacters = INVALID_FILENAME_CHARACTERS.iterator(); invalidCharacters.hasNext(); ) {
            char invalidCharacter = ((Character) invalidCharacters.next()).charValue();
            cleanedFilename = cleanedFilename.replace(invalidCharacter, FILENAME_CHARACTER_REPLACEMENT);
        }
        return cleanedFilename;
    }

    public static String extractFileName(String fileLabel) {
        int descriptionEndIndex = fileLabel.lastIndexOf(":");
        int filePathEndIndex = fileLabel.lastIndexOf(File.separator);
        int startIndex = Math.max(descriptionEndIndex, filePathEndIndex) + 1;
        String fileNameWithExtension = fileLabel.substring(startIndex);
        int extensionBeginIndex = fileNameWithExtension.lastIndexOf(".");
        int endIndex;
        if (extensionBeginIndex != -1) {
            endIndex = extensionBeginIndex;
        } else {
            endIndex = fileNameWithExtension.length();
        }
        String fileNameWithoutExtension = fileNameWithExtension.substring(0, endIndex);
        return fileNameWithoutExtension.trim();
    }

    public static String extractFileNameWithExtension(String fileLabel) {
        return extractFileName(fileLabel) + getFileExtension(fileLabel);
    }
}
