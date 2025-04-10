package fr.itris.glips.compiler.rtdb.file;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import libraries.*;
import fr.itris.glips.compiler.rtdb.*;

/**
 * the class handling the copy of the actions classes in the output actions directory
 * @author ITRIS, Jordi SUC
 */
public class ActionClassesHandler {

    /**
	 * copies all the action classes that can be found in the project 
	 * and its referenced projects into the compiled action classes directory
	 * @param workspacePath the workspace path
	 * @param outputDirectory the output directory
	 * @param projectNamesList the list of all the referenced project names
	 * @throws DuplicateActionFileException 	the exception raised when two or more 
     * 																	action java classes have the same name
	 */
    public void copyActionClasses(String workspacePath, String outputDirectory, LinkedList<String> projectNamesList) throws DuplicateActionFileException {
        if (outputDirectory.endsWith("/")) {
            outputDirectory = outputDirectory.substring(0, outputDirectory.length() - 1);
        }
        File destActionDir = null;
        try {
            destActionDir = new File(new URI(outputDirectory + "/" + FileManager.ACTIONS_DIRECTORY_NAME));
            destActionDir.mkdir();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (destActionDir != null) {
            File projectFile = null, actionsDir = null;
            for (String prjName : projectNamesList) {
                try {
                    projectFile = new File(new URI(workspacePath + "/" + prjName));
                } catch (Exception ex) {
                    projectFile = null;
                    ex.printStackTrace();
                }
                if (projectFile != null && projectFile.exists()) {
                    actionsDir = getActionDirectory(projectFile);
                    if (actionsDir != null && actionsDir.exists()) {
                        copyActionFile(actionsDir, destActionDir);
                    }
                }
            }
        }
    }

    /**
	 * returns the actions directory of the given project file
	 * @param projectFile a project directory
	 * @return the actions directory of the given project file
	 */
    protected File getActionDirectory(File projectFile) {
        File actionsDirectory = null;
        if (projectFile != null) {
            File[] children = projectFile.listFiles();
            for (File child : children) {
                if (child != null && child.getName().equals(FileManager.ACTIONS_DIRECTORY_NAME)) {
                    actionsDirectory = child;
                    break;
                }
            }
        }
        return actionsDirectory;
    }

    /**
	 * copies all the action files and directory that can be found in the given source action directory
	 * @param sourceActionDirectory the source action directory
	 * @param destinationActionDirectory the destination action directory
	 * @throws DuplicateActionFileException 	the exception raised when two or more action java 
	 * 																	classes have the same name
	 */
    protected void copyActionFile(File sourceActionDirectory, File destinationActionDirectory) throws DuplicateActionFileException {
        File srcFile = null;
        LinkedList<String> segments = null;
        for (FileIterator it = new FileIterator(sourceActionDirectory); it.hasNext(); ) {
            srcFile = it.next();
            if (srcFile != null) {
                segments = getPathSegments(sourceActionDirectory, srcFile);
                if (srcFile.isDirectory()) {
                    createDirectory(destinationActionDirectory, segments);
                } else if (srcFile.isFile() && srcFile.getName().endsWith(FileManager.CLASS_FILE_EXTENSION)) {
                    createFile(sourceActionDirectory, destinationActionDirectory, segments);
                }
            }
        }
    }

    /**
	 * creates the directory denoted by the given segments list under the destination action directory
	 * @param destinationActionDirectory the output directory containing the actions
	 * @param segments the list of the path segments relatively to the actions directory
	 */
    protected void createDirectory(File destinationActionDirectory, LinkedList<String> segments) {
        File currentFile = destinationActionDirectory;
        for (String segment : segments) {
            currentFile = new File(currentFile, segment);
            if (!currentFile.exists()) {
                currentFile.mkdir();
            }
        }
    }

    /**
	 * creates the files denoted by the given segments list and copies 
	 * the content of the source file into the destination ones
	 * @param sourceActionDirectory
	 * @param destinationActionDirectory
	 * @param segments the list of the path segments relatively to the actions directory
	 * @throws DuplicateActionFileException 	the exception raised when two or more action java 
	 * 																	classes have the same name
	 */
    protected void createFile(File sourceActionDirectory, File destinationActionDirectory, LinkedList<String> segments) throws DuplicateActionFileException {
        File currentSrcDir = sourceActionDirectory;
        File currentDestDir = destinationActionDirectory;
        String segment = "";
        for (int i = 0; i < segments.size() - 1; i++) {
            segment = segments.get(i);
            currentSrcDir = new File(currentSrcDir, segment);
            currentDestDir = new File(currentDestDir, segment);
        }
        if (currentSrcDir != null && currentDestDir != null) {
            File srcFile = new File(currentSrcDir, segments.getLast());
            if (srcFile.exists()) {
                File destFile = new File(currentDestDir, segments.getLast());
                if (destFile.exists()) {
                    throw new DuplicateActionFileException(srcFile.toURI().toASCIIString());
                }
                try {
                    FileChannel srcChannel = new FileInputStream(srcFile).getChannel();
                    FileChannel destChannel = new FileOutputStream(destFile).getChannel();
                    ByteBuffer buffer = ByteBuffer.allocate((int) srcChannel.size());
                    while (srcChannel.position() < srcChannel.size()) {
                        srcChannel.read(buffer);
                    }
                    srcChannel.close();
                    buffer.rewind();
                    destChannel.write(buffer);
                    destChannel.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
	 * returns the list of the path segments relatively to the actions directory
	 * @param actionDirectory the base action directory for the project that contains the given file
	 * @param file a file in the actions directroy of a project
	 * @return the list of the path segments relatively to the actions directory
	 */
    protected LinkedList<String> getPathSegments(File actionDirectory, File file) {
        LinkedList<String> segments = new LinkedList<String>();
        URI actionDirectoryURI = actionDirectory.toURI();
        URI fileURI = file.toURI();
        URI relativeURI = null;
        try {
            relativeURI = URIEncoderDecoder.relative(actionDirectoryURI, fileURI);
        } catch (Exception ex) {
        }
        if (relativeURI != null) {
            String path = relativeURI.toASCIIString();
            String[] splitPath = path.split("/");
            for (String segment : splitPath) {
                if (segment != null && !segment.equals("")) {
                    segments.add(segment);
                }
            }
        }
        return segments;
    }
}
