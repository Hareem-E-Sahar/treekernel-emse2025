package net.sourceforge.ondex.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Installer for the sbml producer. Copies a given libsbmlj.jar file into
 * the ondex-mini lib/ directory and registers it with its main jar's 
 * manifest file.
 * 
 * @author Jochen Weile, M.Sc.
 *
 */
public class Installer {

    /**
	 * name of the main jar file.
	 */
    public static final String MAIN_JAR = "ondex-mini.jar";

    /**
	 * name of the libsbmlj jar file 
	 */
    public static final String LIBSBMLJ_FILENAME = "libsbmlj-3.2.jar";

    /**
	 * main method.
	 * @param args
	 */
    public static void main(String[] args) {
        if (args.length < 1) {
            printUsageAndDie();
        }
        File libsbmljFile = new File(args[0]);
        if (!libsbmljFile.exists()) {
            printUsageAndDie();
        }
        if (!libsbmljFile.canRead()) {
            System.err.println("Cannot read file " + args[0]);
            printUsageAndDie();
        }
        Installer installer = new Installer();
        System.out.println("Copying file...");
        installer.copyFile(libsbmljFile, new File(".." + File.separator + "lib" + File.separator + LIBSBMLJ_FILENAME));
        System.out.println("Registering file with ondex-mini's manifest...");
        installer.registerInManifest(LIBSBMLJ_FILENAME);
        System.out.println("Installation successful!");
    }

    /**
	 * Copies the original file to the given destination.
	 * @param original the original file.
	 * @param destination the destination to which the file will be copied.
	 */
    private void copyFile(File original, File destination) {
        try {
            FileInputStream in = new FileInputStream(original);
            FileOutputStream out = new FileOutputStream(destination);
            int len = 0;
            byte[] buf = new byte[1024];
            while ((len = in.read(buf)) > -1) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (IOException e) {
            System.err.println("Error copying file!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
	 * registers the given jar file with the ondex mini jar file's manifest.
	 */
    private void registerInManifest(String jarFileName) {
        try {
            String manifestName = "META-INF/MANIFEST.MF";
            String classPathPrefix = "Class-Path: ";
            File mainJarFile = new File(".." + File.separator + MAIN_JAR);
            ZipFile mainJar = new ZipFile(mainJarFile);
            ZipEntry manifestEntry = mainJar.getEntry(manifestName);
            StringBuilder preBuilder = new StringBuilder(""), postBuilder = new StringBuilder(""), mainBuilder = new StringBuilder("");
            BufferedReader br = new BufferedReader(new InputStreamReader(mainJar.getInputStream(manifestEntry)));
            State state = State.PRE;
            String line = null;
            while ((line = br.readLine()) != null) {
                switch(state) {
                    case PRE:
                        if (line.startsWith(classPathPrefix)) {
                            mainBuilder.append(classPathPrefix + "lib/" + jarFileName + " " + line.substring(classPathPrefix.length()));
                            state = State.MAIN;
                        } else {
                            preBuilder.append(line + "\r\n");
                        }
                        break;
                    case MAIN:
                        if (line.startsWith(" ")) {
                            mainBuilder.append(line.substring(1));
                        } else {
                            mainBuilder.append("\r\n");
                            postBuilder.append(line + "\r\n");
                            state = State.POST;
                        }
                        break;
                    case POST:
                        postBuilder.append(line + "\r\n");
                        break;
                }
            }
            br.close();
            for (int i = 69; i < mainBuilder.length(); i += 70) {
                mainBuilder.insert(i, "\r\n ");
            }
            String newManifest = preBuilder.toString() + mainBuilder.toString() + postBuilder.toString();
            File newJarFile = new File(mainJarFile.getAbsolutePath() + ".new");
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(newJarFile));
            int len = 0;
            byte[] buf = new byte[1024];
            Enumeration<? extends ZipEntry> entries = mainJar.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                out.putNextEntry(new ZipEntry(entry.getName()));
                if (entry.getName().equals(manifestName)) {
                    out.write(newManifest.getBytes());
                } else {
                    InputStream in = mainJar.getInputStream(entry);
                    while ((len = in.read(buf)) > -1) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                }
            }
            out.close();
            boolean success = mainJarFile.delete();
            success = success && newJarFile.renameTo(mainJarFile);
            if (!success) {
                throw new IOException("could not complete file operation!");
            }
        } catch (IOException ioe) {
            System.err.println("Error writing Manifest to jar: " + ioe.getMessage());
            ioe.printStackTrace();
            System.exit(1);
        }
    }

    /**
	 * State of the manifest reader.
	 * @author jweile
	 *
	 */
    private enum State {

        PRE, MAIN, POST
    }

    /**
	 * prints the usage instructions and ends the program with error state 1
	 */
    private static void printUsageAndDie() {
        System.err.println("Usage: java -jar sbml-module-0.0.1.jar <absolute path to " + LIBSBMLJ_FILENAME + ">");
        System.exit(1);
    }
}
