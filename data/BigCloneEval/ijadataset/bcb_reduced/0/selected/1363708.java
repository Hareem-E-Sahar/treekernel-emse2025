package net.sourceforge.cobertura.instrument;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.util.ArchiveUtil;
import net.sourceforge.cobertura.util.CommandLineBuilder;
import net.sourceforge.cobertura.util.Header;
import net.sourceforge.cobertura.util.IOUtil;
import net.sourceforge.cobertura.util.RegexUtil;
import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * <p>
 * Add coverage instrumentation to existing classes.
 * </p>
 *
 * <h3>What does that mean, exactly?</h3>
 * <p>
 * It means Cobertura will look at each class you give it.  It
 * loads the bytecode into memory.  For each line of source,
 * Cobertura adds a few extra instructions.  These instructions 
 * do the following:
 * </p>
 * 
 * <ol>
 * <li>Get an instance of the ProjectData class.</li>
 * <li>Call a method in this ProjectData class that increments
 * a counter for this line of code.
 * </ol>
 *
 * <p>
 * After every line in a class has been "instrumented," Cobertura
 * edits the bytecode for the class one more time and adds
 * "implements net.sourceforge.cobertura.coveragedata.HasBeenInstrumented" 
 * This is basically just a flag used internally by Cobertura to
 * determine whether a class has been instrumented or not, so
 * as not to instrument the same class twice.
 * </p>
 */
public class Main {

    private static final LoggerWrapper logger = new LoggerWrapper();

    private File destinationDirectory = null;

    private Collection ignoreRegexes = new Vector();

    private Collection ignoreBranchesRegexes = new Vector();

    private Collection ignoreMethodAnnotations = new HashSet();

    private ClassPattern classPattern = new ClassPattern();

    private boolean ignoreTrivial = false;

    private ProjectData projectData = null;

    /**
	 * @param entry A zip entry.
	 * @return True if the specified entry has "class" as its extension,
	 * false otherwise.
	 */
    private static boolean isClass(ZipEntry entry) {
        return entry.getName().endsWith(".class");
    }

    private boolean addInstrumentationToArchive(CoberturaFile file, InputStream archive, OutputStream output) throws Throwable {
        ZipInputStream zis = null;
        ZipOutputStream zos = null;
        try {
            zis = new ZipInputStream(archive);
            zos = new ZipOutputStream(output);
            return addInstrumentationToArchive(file, zis, zos);
        } finally {
            zis = (ZipInputStream) IOUtil.closeInputStream(zis);
            zos = (ZipOutputStream) IOUtil.closeOutputStream(zos);
        }
    }

    private boolean addInstrumentationToArchive(CoberturaFile file, ZipInputStream archive, ZipOutputStream output) throws Throwable {
        boolean modified = false;
        ZipEntry entry;
        while ((entry = archive.getNextEntry()) != null) {
            try {
                String entryName = entry.getName();
                if (ArchiveUtil.isSignatureFile(entry.getName())) {
                    continue;
                }
                ZipEntry outputEntry = new ZipEntry(entry.getName());
                outputEntry.setComment(entry.getComment());
                outputEntry.setExtra(entry.getExtra());
                outputEntry.setTime(entry.getTime());
                output.putNextEntry(outputEntry);
                byte[] entryBytes = IOUtil.createByteArrayFromInputStream(archive);
                if ((classPattern.isSpecified()) && ArchiveUtil.isArchive(entryName)) {
                    Archive archiveObj = new Archive(file, entryBytes);
                    addInstrumentationToArchive(archiveObj);
                    if (archiveObj.isModified()) {
                        modified = true;
                        entryBytes = archiveObj.getBytes();
                        outputEntry.setTime(System.currentTimeMillis());
                    }
                } else if (isClass(entry) && classPattern.matches(entryName)) {
                    try {
                        ClassReader cr = new ClassReader(entryBytes);
                        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                        ClassInstrumenter cv = new ClassInstrumenter(projectData, cw, ignoreRegexes, ignoreBranchesRegexes, ignoreMethodAnnotations, ignoreTrivial);
                        cr.accept(cv, 0);
                        if (cv.isInstrumented()) {
                            logger.debug("Putting instrumented entry: " + entry.getName());
                            entryBytes = cw.toByteArray();
                            modified = true;
                            outputEntry.setTime(System.currentTimeMillis());
                        }
                    } catch (Throwable t) {
                        if (entry.getName().endsWith("_Stub.class")) {
                            logger.debug("Problems instrumenting archive entry: " + entry.getName(), t);
                        } else {
                            logger.warn("Problems instrumenting archive entry: " + entry.getName(), t);
                        }
                    }
                }
                output.write(entryBytes);
                output.closeEntry();
                archive.closeEntry();
            } catch (Exception e) {
                logger.warn("Problems with archive entry: " + entry.getName(), e);
            } catch (Throwable t) {
                logger.warn("Problems with archive entry: " + entry.getName(), t);
            }
            output.flush();
        }
        return modified;
    }

    private void addInstrumentationToArchive(Archive archive) throws Throwable {
        InputStream in = null;
        ByteArrayOutputStream out = null;
        try {
            in = archive.getInputStream();
            out = new ByteArrayOutputStream();
            boolean modified = addInstrumentationToArchive(archive.getCoberturaFile(), in, out);
            if (modified) {
                out.flush();
                byte[] bytes = out.toByteArray();
                archive.setModifiedBytes(bytes);
            }
        } finally {
            in = IOUtil.closeInputStream(in);
            out = (ByteArrayOutputStream) IOUtil.closeOutputStream(out);
        }
    }

    private void addInstrumentationToArchive(CoberturaFile archive) throws Throwable {
        logger.debug("Instrumenting archive " + archive.getAbsolutePath());
        File outputFile = null;
        ZipInputStream input = null;
        ZipOutputStream output = null;
        boolean modified = false;
        try {
            try {
                input = new ZipInputStream(new FileInputStream(archive));
            } catch (FileNotFoundException e) {
                logger.warn("Cannot open archive file: " + archive.getAbsolutePath(), e);
                return;
            }
            try {
                if (destinationDirectory != null) {
                    outputFile = new File(destinationDirectory, archive.getPathname());
                } else {
                    outputFile = File.createTempFile("CoberturaInstrumentedArchive", "jar");
                    outputFile.deleteOnExit();
                }
                output = new ZipOutputStream(new FileOutputStream(outputFile));
            } catch (IOException e) {
                logger.warn("Cannot open file for instrumented archive: " + archive.getAbsolutePath(), e);
                return;
            }
            try {
                modified = addInstrumentationToArchive(archive, input, output);
            } catch (Throwable e) {
                logger.warn("Cannot instrument archive: " + archive.getAbsolutePath(), e);
                return;
            }
        } finally {
            input = (ZipInputStream) IOUtil.closeInputStream(input);
            output = (ZipOutputStream) IOUtil.closeOutputStream(output);
        }
        if (modified && (destinationDirectory == null)) {
            try {
                logger.debug("Moving " + outputFile.getAbsolutePath() + " to " + archive.getAbsolutePath());
                IOUtil.moveFile(outputFile, archive);
            } catch (IOException e) {
                logger.warn("Cannot instrument archive: " + archive.getAbsolutePath(), e);
                return;
            }
        }
        if ((destinationDirectory != null) && (!modified)) {
            outputFile.delete();
        }
    }

    private void addInstrumentationToSingleClass(File file) throws Throwable {
        logger.debug("Instrumenting class " + file.getAbsolutePath());
        InputStream inputStream = null;
        ClassWriter cw;
        ClassInstrumenter cv;
        try {
            inputStream = new FileInputStream(file);
            ClassReader cr = new ClassReader(inputStream);
            cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            cv = new ClassInstrumenter(projectData, cw, ignoreRegexes, ignoreBranchesRegexes, ignoreMethodAnnotations, ignoreTrivial);
            cr.accept(cv, 0);
        } catch (Throwable t) {
            logger.warn("Unable to instrument file " + file.getAbsolutePath(), t);
            return;
        } finally {
            inputStream = IOUtil.closeInputStream(inputStream);
        }
        OutputStream outputStream = null;
        try {
            if (cv.isInstrumented()) {
                File outputFile;
                if (destinationDirectory == null) outputFile = file; else outputFile = new File(destinationDirectory, cv.getClassName().replace('.', File.separatorChar) + ".class");
                File parentFile = outputFile.getParentFile();
                if (parentFile != null) {
                    parentFile.mkdirs();
                }
                byte[] instrumentedClass = cw.toByteArray();
                outputStream = new FileOutputStream(outputFile);
                outputStream.write(instrumentedClass);
            }
        } catch (Throwable t) {
            logger.warn("Unable to instrument file " + file.getAbsolutePath(), t);
            return;
        } finally {
            outputStream = IOUtil.closeOutputStream(outputStream);
        }
    }

    private void addInstrumentation(CoberturaFile coberturaFile) throws Throwable {
        if (coberturaFile.isClass() && classPattern.matches(coberturaFile.getPathname())) {
            addInstrumentationToSingleClass(coberturaFile);
        } else if (coberturaFile.isDirectory()) {
            String[] contents = coberturaFile.list();
            for (int i = 0; i < contents.length; i++) {
                File relativeFile = new File(coberturaFile.getPathname(), contents[i]);
                CoberturaFile relativeCoberturaFile = new CoberturaFile(coberturaFile.getBaseDir(), relativeFile.toString());
                addInstrumentation(relativeCoberturaFile);
            }
        }
    }

    private void parseArguments(String[] args) throws Throwable {
        File dataFile = CoverageDataFileHandler.getDefaultDataFile();
        List filePaths = new ArrayList();
        String baseDir = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--basedir")) baseDir = args[++i]; else if (args[i].equals("--datafile")) dataFile = new File(args[++i]); else if (args[i].equals("--destination")) destinationDirectory = new File(args[++i]); else if (args[i].equals("--ignore")) {
                RegexUtil.addRegex(ignoreRegexes, args[++i]);
            } else if (args[i].equals("--ignoreBranches")) {
                RegexUtil.addRegex(ignoreBranchesRegexes, args[++i]);
            } else if (args[i].equals("--ignoreMethodAnnotation")) {
                ignoreMethodAnnotations.add(args[++i]);
            } else if (args[i].equals("--ignoreTrivial")) {
                ignoreTrivial = true;
            } else if (args[i].equals("--includeClasses")) {
                classPattern.addIncludeClassesRegex(args[++i]);
            } else if (args[i].equals("--excludeClasses")) {
                classPattern.addExcludeClassesRegex(args[++i]);
            } else if (args[i].equals("--failOnError")) {
                logger.setFailOnError(true);
            } else {
                CoberturaFile coberturaFile = new CoberturaFile(baseDir, args[i]);
                filePaths.add(coberturaFile);
            }
        }
        if (dataFile.isFile()) projectData = CoverageDataFileHandler.loadCoverageData(dataFile);
        if (projectData == null) projectData = new ProjectData();
        System.out.println("Instrumenting " + filePaths.size() + " " + (filePaths.size() == 1 ? "file" : "files") + (destinationDirectory != null ? " to " + destinationDirectory.getAbsoluteFile() : ""));
        Iterator iter = filePaths.iterator();
        while (iter.hasNext()) {
            CoberturaFile coberturaFile = (CoberturaFile) iter.next();
            if (coberturaFile.isArchive()) {
                addInstrumentationToArchive(coberturaFile);
            } else {
                addInstrumentation(coberturaFile);
            }
        }
        CoverageDataFileHandler.saveCoverageData(projectData, dataFile);
    }

    public static void main(String[] args) throws Throwable {
        Header.print(System.out);
        long startTime = System.currentTimeMillis();
        Main main = new Main();
        try {
            args = CommandLineBuilder.preprocessCommandLineArguments(args);
        } catch (Exception ex) {
            System.err.println("Error: Cannot process arguments: " + ex.getMessage());
            System.exit(1);
        }
        main.parseArguments(args);
        long stopTime = System.currentTimeMillis();
        System.out.println("Instrument time: " + (stopTime - startTime) + "ms");
    }

    private static class LoggerWrapper {

        private final Logger logger = Logger.getLogger(Main.class);

        private boolean failOnError = false;

        public void setFailOnError(boolean failOnError) {
            this.failOnError = failOnError;
        }

        public void debug(String message) {
            logger.debug(message);
        }

        public void debug(String message, Throwable t) {
            logger.debug(message, t);
        }

        public void warn(String message, Throwable t) throws Throwable {
            logger.warn(message, t);
            if (failOnError) {
                throw t;
            }
        }
    }
}
