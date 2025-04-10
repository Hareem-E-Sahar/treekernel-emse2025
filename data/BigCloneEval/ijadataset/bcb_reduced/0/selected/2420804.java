package org.tonguetied.datatransfer;

import static fmpp.setting.Settings.NAME_DATA;
import static fmpp.setting.Settings.NAME_OUTPUT_ENCODING;
import static fmpp.setting.Settings.NAME_OUTPUT_ROOT;
import static fmpp.setting.Settings.NAME_REPLACE_EXTENSIONS;
import static fmpp.setting.Settings.NAME_SOURCES;
import static fmpp.setting.Settings.NAME_SOURCE_ROOT;
import static freemarker.log.Logger.LIBRARY_LOG4J;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import org.tonguetied.datatransfer.common.ExportParameters;
import org.tonguetied.datatransfer.common.FormatType;
import org.tonguetied.datatransfer.common.ImportParameters;
import org.tonguetied.datatransfer.dao.TransferRepository;
import org.tonguetied.datatransfer.exporting.ExportDataPostProcessor;
import org.tonguetied.datatransfer.exporting.ExportDataPostProcessorFactory;
import org.tonguetied.datatransfer.exporting.ExportException;
import org.tonguetied.datatransfer.exporting.Native2AsciiDirective;
import org.tonguetied.datatransfer.exporting.NoExportDataException;
import org.tonguetied.datatransfer.importing.Importer;
import org.tonguetied.datatransfer.importing.ImporterFactory;
import org.tonguetied.keywordmanagement.KeywordService;
import org.tonguetied.keywordmanagement.Translation;
import fmpp.ProcessingException;
import fmpp.progresslisteners.LoggerProgressListener;
import fmpp.setting.SettingException;
import fmpp.setting.Settings;

/**
 * Concrete implementation of the {@link DataService} interface.
 * 
 * @author bsion
 *
 */
public class DataServiceImpl implements DataService {

    private Settings settings;

    private TransferRepository transferRepository;

    private KeywordService keywordService;

    private File sourceRoot;

    private File outputRoot;

    private File outputDir;

    private static final File BASE_DIR = SystemUtils.getUserDir();

    private static final Logger logger = Logger.getLogger(DataServiceImpl.class);

    private static final String DATE_FORMAT = "yyyy-MM-dd_HH_mm_ss";

    /**
     * Create a new instance of the DataServiceImpl. After this constructor
     * has been called the {@link #init()} method should be called.
     */
    public DataServiceImpl() {
    }

    /**
     * Initialize an instance of the DataServiceImpl. This method configures
     * the exporter for use.
     *  
     * @throws ExportException if the exporter is fails to configure
     */
    public void init() throws ExportException {
        if (logger.isDebugEnabled()) logger.debug("loading freemarker settings");
        try {
            settings = new Settings(BASE_DIR);
            settings.set(NAME_SOURCE_ROOT, sourceRoot.getPath());
            settings.set(NAME_OUTPUT_ENCODING, "UTF-8");
            freemarker.log.Logger.selectLoggerLibrary(LIBRARY_LOG4J);
            createOutputDirectory();
        } catch (SettingException se) {
            throw new ExportException(se);
        } catch (ClassNotFoundException cnfe) {
            throw new ExportException(cnfe);
        }
    }

    /**
     * Create the output root directory if doesn't already exist.
     */
    private void createOutputDirectory() {
        if (!outputRoot.exists()) {
            if (outputRoot.mkdirs()) if (logger.isInfoEnabled()) logger.info("created directory " + outputRoot.getPath());
        }
    }

    public void exportData(final ExportParameters parameters) throws ExportException {
        if (parameters == null) {
            throw new IllegalArgumentException("cannot perform export with " + "null parameters");
        }
        if (parameters.getFormatType() == null) {
            throw new IllegalArgumentException("cannot perform export without" + " an export type set");
        }
        final long start = System.currentTimeMillis();
        if (logger.isDebugEnabled()) logger.debug("exporting based on filter " + parameters);
        try {
            List<Translation> translations = transferRepository.findTranslations(parameters);
            if (translations.isEmpty()) {
                throw new NoExportDataException(parameters);
            }
            File exportPath = getExportPath(true);
            final boolean isDirCreated = exportPath.mkdir();
            if (!isDirCreated) logger.warn("failed to create directory: " + exportPath);
            settings.set(NAME_OUTPUT_ROOT, exportPath.getAbsolutePath());
            settings.set(NAME_SOURCES, getTemplateName(parameters.getFormatType()));
            String[] replaceExtensions = new String[] { "ftl", parameters.getFormatType().getDefaultFileExtension() };
            settings.set(NAME_REPLACE_EXTENSIONS, replaceExtensions);
            Map<String, Object> root = postProcess(parameters, translations);
            root.put("native2ascii", new Native2AsciiDirective());
            settings.set(NAME_DATA, root);
            settings.addProgressListener(new LoggerProgressListener());
            settings.execute();
            if (parameters.isResultPackaged()) {
                createArchive(exportPath);
            }
        } catch (SettingException se) {
            throw new ExportException(se);
        } catch (ProcessingException pe) {
            throw new ExportException(pe);
        }
        if (logger.isInfoEnabled()) {
            final float totalMillis = System.currentTimeMillis() - start;
            logger.info("export complete in " + (totalMillis / 1000) + " seconds");
        }
    }

    /**
     * Post process the result translations to put them into a desired format 
     * if needed.
     * 
     * @param parameters the parameters used to filter and format the data
     * @param translations the {@link Translation}s to process
     * @return a map of parameters used by the templating mechanism
     */
    private Map<String, Object> postProcess(final ExportParameters parameters, List<Translation> translations) {
        Map<String, Object> root = new HashMap<String, Object>();
        final ExportDataPostProcessor postProcessor = ExportDataPostProcessorFactory.getPostProcessor(parameters.getFormatType(), parameters, keywordService);
        if (postProcessor != null) {
            if (logger.isDebugEnabled()) logger.debug("post processing results using: " + postProcessor.getClass());
            final List<?> results = postProcessor.transformData(translations);
            root.put("translations", results);
            postProcessor.addItems(root);
        } else {
            root.put("translations", translations);
        }
        return root;
    }

    public void createArchive(File directory) throws ExportException, IllegalArgumentException {
        if (!directory.isDirectory()) throw new IllegalArgumentException("expecting a directory");
        ZipOutputStream zos = null;
        try {
            File[] files = directory.listFiles();
            if (files.length > 0) {
                final File archive = new File(directory, directory.getName() + ".zip");
                zos = new ZipOutputStream(new FileOutputStream(archive));
                for (File file : files) {
                    zos.putNextEntry(new ZipEntry(file.getName()));
                    IOUtils.write(FileUtils.readFileToByteArray(file), zos);
                    zos.closeEntry();
                }
                if (logger.isDebugEnabled()) logger.debug("archived " + files.length + " files to " + archive.getPath());
            }
        } catch (IOException ioe) {
            throw new ExportException(ioe);
        } finally {
            IOUtils.closeQuietly(zos);
        }
    }

    /**
     * Returns the the directory where exported files from the most recently 
     * executed export are saved. This method passes a value of false to 
     * {@link #getExportPath(boolean)} so as not to reset the output path.
     * 
     * @return the output directory
     * @see #getExportPath(boolean) 
     */
    public File getExportPath() {
        return getExportPath(false);
    }

    /**
     * Returns the the directory where exported files from the most recently 
     * executed export are saved.
     * 
     * @param reset flag indicating that the output directory should be 
     * re-initialised.
     * @return the output directory 
     */
    private File getExportPath(final boolean reset) {
        if (reset) {
            final DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
            outputDir = new File(outputRoot, formatter.format(new Date()));
        }
        return outputDir;
    }

    public void importData(final ImportParameters parameters) {
        final long start = System.currentTimeMillis();
        if (logger.isDebugEnabled()) logger.debug("importing based on filter " + parameters);
        Importer importer = ImporterFactory.getImporter(parameters.getFormatType(), keywordService, transferRepository);
        importer.importData(parameters);
        if (logger.isInfoEnabled()) {
            final long totalMillis = System.currentTimeMillis() - start;
            logger.info("import complete in " + (totalMillis / 1000) + " seconds");
        }
    }

    /**
     * Determine the name of the export template to use based off the type of
     * export being performed.
     * 
     * @param formatType the type of export being performed
     * @return the name of the export template to use
     */
    private String getTemplateName(final FormatType formatType) {
        return formatType.name() + ".ftl";
    }

    public void setTransferRepository(final TransferRepository transferRepository) {
        this.transferRepository = transferRepository;
    }

    /**
     * Assign the directory containing the templates.
     * 
     * @param sourceRoot the directory on the file system where template files
     * are stored 
     */
    public void setSourceRoot(final Resource sourceRoot) {
        this.sourceRoot = getFile(sourceRoot);
    }

    /**
     * @param outputRoot the base directory on the file system where all 
     * generated export files should be saved.
     */
    public void setOutputRoot(final Resource outputRoot) {
        this.outputRoot = getFile(outputRoot);
    }

    /**
     * Get the file object from the resource
     * 
     * @param resource the resource object from which to get the file
     * @throws ExportException thrown if an error occurs trying to get the file
     * from the resource
     */
    private File getFile(final Resource resource) throws ExportException {
        try {
            return resource.getFile();
        } catch (IOException ioe) {
            throw new ExportException(ioe);
        }
    }

    /**
     * @param keywordService the keywordService to set
     */
    public void setKeywordService(final KeywordService keywordService) {
        this.keywordService = keywordService;
    }
}
