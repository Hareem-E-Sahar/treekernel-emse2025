package com.gmail.dpierron.calibre.opds;

import com.gmail.dpierron.calibre.cache.CachedFile;
import com.gmail.dpierron.calibre.cache.CachedFileManager;
import com.gmail.dpierron.calibre.configuration.ConfigurationHolder;
import com.gmail.dpierron.calibre.configuration.ConfigurationManager;
import com.gmail.dpierron.calibre.configuration.DeviceMode;
import com.gmail.dpierron.calibre.configuration.Icons;
import com.gmail.dpierron.calibre.database.Database;
import com.gmail.dpierron.calibre.database.DatabaseManager;
import com.gmail.dpierron.calibre.datamodel.Book;
import com.gmail.dpierron.calibre.datamodel.DataModel;
import com.gmail.dpierron.calibre.datamodel.EBookFile;
import com.gmail.dpierron.calibre.datamodel.filter.BookFilter;
import com.gmail.dpierron.calibre.datamodel.filter.CalibreQueryInterpreter;
import com.gmail.dpierron.calibre.datamodel.filter.FilterHelper;
import com.gmail.dpierron.calibre.error.CalibreSavedSearchInterpretException;
import com.gmail.dpierron.calibre.error.CalibreSavedSearchNotFoundException;
import com.gmail.dpierron.calibre.opds.i18n.Localization;
import com.gmail.dpierron.calibre.opds.indexer.IndexManager;
import com.gmail.dpierron.calibre.opds.secure.SecureFileManager;
import com.gmail.dpierron.calibre.opf.OpfOutput;
import com.gmail.dpierron.calibre.trook.TrookSpecificSearchDatabaseManager;
import com.gmail.dpierron.tools.Composite;
import com.gmail.dpierron.tools.Helper;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import javax.swing.*;
import java.io.*;
import java.util.*;

public class Catalog {

    private static final Logger logger = Logger.getLogger(Catalog.class);

    private static long copyExistHits;

    private static long copyLengthHits;

    private static long copyDateMisses;

    private static long copyCrcHits;

    private static long copyCrcMisses;

    private static long copyCrcUnchecked;

    private static long copyToSelf;

    private static long copyDeleted;

    private static final ConfigurationHolder currentProfile = ConfigurationManager.INSTANCE.getCurrentProfile();

    private static final boolean checkCRC = currentProfile.getMinimizeChangedFiles();

    private CatalogCallbackInterface callback;

    private static final boolean syncFilesDetail = false;

    private static final boolean syncLog = true;

    private static PrintWriter syncLogFile;

    /**
   * Default Constructor
   */
    private Catalog() {
        super();
    }

    /**
   * Constructor setting callback interface for GUI
   *
   * @param callback
   */
    public Catalog(CatalogCallbackInterface callback) {
        this();
        this.callback = callback;
        CatalogContext.INSTANCE.setCallback(callback);
    }

    /**
   * Sync Files between source and target
   * <p/>
   * Routine that handles synchronisation of files between source and target
   * It also handles deleting unwanted files/folders at the target location
   *
   * @param src
   * @param dst
   * @throws IOException
   */
    private void syncFiles(File src, File dst) throws IOException {
        callback.incStepProgressIndicatorPosition();
        if ((src == null) || (dst == null)) {
            if (src == null) logger.warn("syncFiles: Unexpected 'src' null parameter"); else logger.warn("syncFiles: Unexpected 'dst' null parameter");
            return;
        }
        if (!src.exists()) {
            File f = new File(src.getAbsolutePath());
            if (f.exists() == false) {
                logger.warn("syncFiles: Unexpected missing file: " + src.getAbsolutePath());
                return;
            } else {
                logger.debug("syncFiles: Incorrect caching of exists()=false status for file: " + src.getAbsolutePath());
            }
        }
        if (src.getAbsolutePath().equalsIgnoreCase(dst.getAbsolutePath())) {
            copyToSelf++;
            if (syncFilesDetail && logger.isTraceEnabled()) logger.trace("syncFiles: attempting to copy file to itself: " + src.getAbsolutePath());
            return;
        }
        CachedFile cf_src = CachedFileManager.INSTANCE.inCache(src);
        CachedFile cf_dst = CachedFileManager.INSTANCE.inCache(dst);
        if (cf_src == null) {
            cf_src = new CachedFile(src.getPath());
            if (syncFilesDetail && logger.isTraceEnabled()) logger.trace("syncFiles: Source not in cache: " + src.getPath());
        }
        if (cf_dst == null) {
            cf_dst = CachedFileManager.INSTANCE.addCachedFile(dst);
            if (syncFilesDetail && logger.isTraceEnabled()) logger.trace("syncFiles: Target not in cache: " + src.getPath());
            cf_dst.setTarget(true);
        }
        if (cf_src.isDirectory()) {
            if (logger.isTraceEnabled()) logger.trace("Directory " + cf_src.getName() + " Processing Started");
            callback.showMessage(src.getParentFile().getName() + File.separator + cf_src.getName());
            if (!cf_dst.exists()) {
                if (logger.isTraceEnabled()) logger.trace("Directory " + cf_dst.getName() + " Create missing target");
                if (syncLog) syncLogFile.printf("CREATED: %s\n", cf_dst.getName());
                dst.mkdirs();
            }
            if (!cf_dst.isDirectory()) {
                logger.warn("Directory " + cf_src.getName() + " Unexpected file with name expected for directory");
                return;
            }
            File sourceFiles[] = src.listFiles();
            List<File> targetNotInSourceFiles = new LinkedList<File>(Arrays.asList(dst.listFiles()));
            for (int i = 0; i < sourceFiles.length; i++) {
                File sourceFile = sourceFiles[i];
                String fileName = sourceFile.getName();
                File destFile = new File(dst, fileName);
                if (destFile.exists()) {
                    if ((cf_src.getName().endsWith(".xml")) && (currentProfile.getGenerateOpds() == true)) {
                        if (logger.isTraceEnabled()) logger.trace("No OPDS catalog so delete " + src.getName());
                    } else {
                        targetNotInSourceFiles.remove(destFile);
                        if (CachedFileManager.INSTANCE.inCache(destFile) == null) destFile = CachedFileManager.INSTANCE.addCachedFile(destFile);
                    }
                } else {
                    if (logger.isTraceEnabled()) logger.trace("Directory " + src.getName() + " Unexpected missing target");
                    CachedFileManager.INSTANCE.removeCachedFile(destFile);
                }
                syncFiles(sourceFile, destFile);
            }
            for (File file : targetNotInSourceFiles) {
                Helper.delete(file);
                if (syncLog) syncLogFile.printf("DELETED: %s\n", file.getAbsolutePath());
                if (CachedFileManager.INSTANCE.inCache(file) != null) {
                    CachedFileManager.INSTANCE.removeCachedFile(file);
                }
            }
            if (logger.isTraceEnabled()) logger.trace("Directory " + src.getName() + " Processing completed");
        } else {
            boolean copyflag;
            if (!currentProfile.getGenerateOpds()) {
                if (cf_src.getName().endsWith(".xml")) {
                    if (cf_dst.exists()) {
                        if (syncFilesDetail && logger.isTraceEnabled()) logger.trace("File " + cf_dst.getName() + ": Deleted as XML file and no OPDS catalog required");
                    } else {
                        if (syncFilesDetail && logger.isTraceEnabled()) logger.trace("File " + cf_src.getName() + ": Ignored as XML file and no OPDS catalog required");
                    }
                    CachedFileManager.INSTANCE.removeCachedFile(cf_src);
                    CachedFileManager.INSTANCE.removeCachedFile(cf_dst);
                    return;
                }
            }
            if (syncFilesDetail && logger.isTraceEnabled()) logger.trace("File " + cf_src.getName() + ": Checking to see if should be copied");
            if (!cf_dst.exists()) {
                if (syncFilesDetail && logger.isTraceEnabled()) logger.trace("File " + cf_src.getName() + ": Copy as target is missing");
                copyExistHits++;
                copyflag = true;
                if (syncLog) syncLogFile.printf("COPIED (New file): %s\n", cf_dst.getName());
            } else {
                if (syncFilesDetail && logger.isTraceEnabled()) logger.trace("File " + cf_src.getName() + ": .. exists on target");
                if (cf_src.length() != cf_dst.length()) {
                    if (logger.isTraceEnabled()) logger.trace("File " + cf_src.getName() + ": Copy as size changed");
                    copyLengthHits++;
                    copyflag = true;
                    if (syncLog) syncLogFile.printf("COPIED (length changed): %s\n", cf_src.getName());
                } else {
                    if (syncFilesDetail && logger.isTraceEnabled()) logger.trace("File " + cf_src.getName() + ": .. size same on source and target");
                    if (cf_src.lastModified() <= cf_dst.lastModified()) {
                        if (logger.isTraceEnabled()) logger.trace("File " + cf_src.getName() + ": Skip Copy as source is not newer");
                        copyDateMisses++;
                        copyflag = false;
                        if (syncLog) syncLogFile.printf("NOT COPIED (Source not newer): %s\n", cf_dst.getName());
                    } else {
                        if (syncFilesDetail && logger.isTraceEnabled()) logger.trace("File " + cf_src.getName() + ": .. source is newer");
                        if (!checkCRC) {
                            if (logger.isTraceEnabled()) logger.trace("File " + cf_src.getName() + ": Copy as CRC check not active");
                            if (cf_dst.isCrc()) if (logger.isTraceEnabled()) logger.trace("File " + cf_src.getName() + "CRC entry invalidated");
                            cf_dst.clearCrc();
                            copyCrcUnchecked++;
                            copyflag = true;
                            if (syncLog) syncLogFile.printf("COPIED (CRC check not active): %s\n", cf_src.getName());
                        } else {
                            if (cf_src.getCrc() != cf_dst.getCrc()) {
                                if (logger.isTraceEnabled()) logger.trace("File " + cf_src.getName() + ": Copy as CRC's different");
                                copyCrcHits++;
                                copyflag = true;
                                if (syncLog) syncLogFile.printf("COPIED (CRC changed): %s\n", cf_src.getName());
                            } else {
                                if (logger.isTraceEnabled()) logger.trace("File " + cf_src.getName() + ": Skip copy as CRC's match");
                                copyCrcMisses++;
                                copyflag = false;
                                if (syncLog) syncLogFile.printf("NOT COPIED (CRC same): %s\n", cf_src.getName());
                            }
                        }
                    }
                }
            }
            if (copyflag) {
                callback.showMessage(src.getParentFile().getName() + File.separator + src.getName());
                if (syncFilesDetail && logger.isDebugEnabled()) logger.debug("Copying file " + cf_src.getName());
                Helper.copy(cf_src, cf_dst);
                cf_dst.setCrc(cf_src.getCrc());
            }
        }
    }

    /**
   * @param book
   * @return
   */
    private boolean shouldReprocessEpubMetadata(Book book) {
        EBookFile epubFile = book.getEpubFile();
        if (epubFile == null) return false;
        File opfFile = new File(book.getBookFolder(), "metadata.opf");
        if (!opfFile.exists()) return true;
        long opfDate = opfFile.lastModified();
        long epubDate = epubFile.getFile().lastModified();
        return (opfDate > epubDate);
    }

    private Element computeSummary(List<Book> books) {
        Element contentElement = JDOM.INSTANCE.element("content");
        File calibreLibraryFolder = currentProfile.getDatabaseFolder();
        File summaryFile = new File(calibreLibraryFolder, "calibre2opds_summary.html");
        if (summaryFile.exists()) {
            contentElement.setAttribute("type", "text/html");
            try {
                FileInputStream is = new FileInputStream(summaryFile);
                String text = Helper.readTextFile(is);
                List<Element> htmlElements = JDOM.INSTANCE.convertBookCommentToXhtml(text);
                if (htmlElements != null) for (Element htmlElement : htmlElements) {
                    contentElement.addContent(htmlElement.detach());
                }
            } catch (FileNotFoundException e) {
                logger.error(Localization.Main.getText("error.summary.cannotFindFile", summaryFile.getAbsolutePath()), e);
            } catch (IOException e) {
                logger.error(Localization.Main.getText("error.summary.errorParsingFile"), e);
            }
        } else {
            contentElement.setAttribute("type", "text");
            String summary = Localization.Main.getText("main.summary", Constants.PROGTITLE, Summarizer.INSTANCE.getBookWord(books.size()));
            contentElement.addContent(summary);
        }
        return contentElement;
    }

    /**
   * -----------------------------------------------
   * Control the overall catalog generation process
   * -----------------------------------------------
   *
   * @throws IOException
   */
    public void createMainCatalog() throws IOException {
        long countMetadata;
        long countThumbnails;
        long countCovers;
        String textYES = Localization.Main.getText("boolean.yes");
        String textNO = Localization.Main.getText("boolean.no");
        File generateFolder = null;
        File targetFolder = null;
        File libraryFolder = null;
        String where = null;
        boolean generationStopped = false;
        boolean generationCrashed = false;
        logger.info(Localization.Main.getText("config.profile.label", ConfigurationManager.INSTANCE.getCurrentProfileName()));
        callback.dumpOptions();
        CachedFileManager.INSTANCE.initialize();
        CatalogContext.INSTANCE.initialize();
        CatalogContext.INSTANCE.setCallback(callback);
        if (logger.isTraceEnabled()) logger.trace("Start sanity checks against user errors that might cause data loss");
        if ((!currentProfile.getGenerateOpds()) && (!currentProfile.getGenerateHtml())) {
            callback.errorOccured(Localization.Main.getText("error.nogeneratetype"), null);
            return;
        }
        if (!currentProfile.getGenerateAuthors() && !currentProfile.getGenerateTags() && !currentProfile.getGenerateSeries() && !currentProfile.getGenerateRecent() && !currentProfile.getGenerateRatings() && !currentProfile.getGenerateAllbooks()) {
            callback.errorOccured(Localization.Main.getText("error.noSubcatalog"), null);
            return;
        }
        libraryFolder = currentProfile.getDatabaseFolder();
        if (Helper.isNullOrEmpty(libraryFolder)) {
            callback.errorOccured(Localization.Main.getText("error.databasenotset"), null);
            return;
        }
        assert libraryFolder != null : "libraryFolder must be set to continue with generation";
        if (!DatabaseManager.INSTANCE.databaseExists()) {
            callback.errorOccured(Localization.Main.getText("error.nodatabase", libraryFolder), null);
            return;
        }
        if (Helper.isNullOrEmpty(currentProfile.getCatalogFolderName())) {
            callback.errorOccured(Localization.Main.getText("error.nocatalog"), null);
            return;
        }
        targetFolder = currentProfile.getTargetFolder();
        if (Helper.isNullOrEmpty(targetFolder)) {
            switch(currentProfile.getDeviceMode()) {
                case Nook:
                    callback.errorOccured(Localization.Main.getText("error.nooktargetnotset"), null);
                    return;
                case Nas:
                    callback.errorOccured(Localization.Main.getText("error.targetnotset"), null);
                    return;
                case Dropbox:
                    assert currentProfile.getCopyToDatabaseFolder() : "Copy to database folder MUST be set in Default mode";
                    break;
                default:
                    assert false : "Unknown DeviceMode " + currentProfile.getDeviceMode();
            }
        } else {
            switch(currentProfile.getDeviceMode()) {
                case Nook:
                    if (!targetFolder.exists()) {
                        callback.errorOccured(Localization.Main.getText("error.nooktargetdoesnotexist"), null);
                        return;
                    }
                    targetFolder = new File(targetFolder.getAbsolutePath() + "/" + currentProfile.getCatalogFolderName() + Constants.TROOK_FOLDER_EXTENSION);
                    break;
                case Nas:
                    if (!targetFolder.exists()) {
                        callback.errorOccured(Localization.Main.getText("error.targetdoesnotexist"), null);
                        return;
                    }
                    break;
                case Dropbox:
                    assert false : "Setting Target folder should be disabled in Dropbox mode";
                default:
                    assert false : "Unknown DeviceMode " + currentProfile.getDeviceMode();
            }
        }
        logger.trace("targetFolder set to " + targetFolder);
        if (targetFolder != null) {
            if (libraryFolder.getAbsolutePath().equals(targetFolder.getAbsolutePath())) {
                callback.errorOccured(Localization.Main.getText("error.targetsame"), null);
                return;
            }
            if (libraryFolder.getAbsolutePath().startsWith(targetFolder.getAbsolutePath())) {
                callback.errorOccured(Localization.Main.getText("error.targetparent"), null);
                return;
            }
            if (!checkCatalogExistence(targetFolder, false)) {
                int n = callback.askUser(Localization.Main.getText("gui.confirm.clear", targetFolder), textYES, textNO);
                if (1 == n) {
                    return;
                }
            }
        }
        File catalogParentFolder = targetFolder;
        if (catalogParentFolder == null || catalogParentFolder.getName().length() == 0) {
            if (!checkCatalogExistence(libraryFolder, true)) {
                int n = callback.askUser(Localization.Main.getText("gui.confirm.clear", libraryFolder + File.separator + currentProfile.getCatalogFolderName()), textYES, textNO);
                if (1 == n) {
                    return;
                }
            }
            catalogParentFolder = libraryFolder;
        }
        logger.trace("catalogParentFolder set to " + catalogParentFolder);
        File catalogFolder = new File(catalogParentFolder, CatalogContext.INSTANCE.getCatalogManager().getCatalogFolderName());
        if (logger.isTraceEnabled()) logger.trace("New catalog to be generated at " + catalogFolder.getPath());
        if (true == currentProfile.getCopyToDatabaseFolder()) {
            File databaseFolder = currentProfile.getDatabaseFolder();
            if (!checkCatalogExistence(databaseFolder, true)) {
                int n = callback.askUser(Localization.Main.getText("gui.confirm.clear", databaseFolder + File.separator + currentProfile.getCatalogFolderName()), textYES, textNO);
                if (1 == n) {
                    return;
                }
            }
        }
        logger.trace("Passed sanity checks, so proceed with generation");
        try {
            File temp = File.createTempFile("calibre2opds", "");
            String tempPath = temp.getAbsolutePath();
            temp.delete();
            generateFolder = new File(tempPath);
            if (logger.isTraceEnabled()) logger.trace("generateFolder set to " + generateFolder);
            CatalogContext.INSTANCE.getCatalogManager().setCatalogFolder(generateFolder);
            callback.startCreateMainCatalog();
            long now = System.currentTimeMillis();
            callback.startReadDatabase();
            now = System.currentTimeMillis();
            DataModel.INSTANCE.reset();
            DataModel.INSTANCE.preloadDataModel();
            callback.checkIfContinueGenerating();
            BookFilter featuredBookFilter = null;
            String featuredCatalogSearch = ConfigurationManager.INSTANCE.getCurrentProfile().getFeaturedCatalogSavedSearchName();
            if (Helper.isNotNullOrEmpty(featuredCatalogSearch)) {
                try {
                    featuredBookFilter = CalibreQueryInterpreter.interpret(featuredCatalogSearch);
                } catch (CalibreSavedSearchInterpretException e) {
                    callback.errorOccured(Localization.Main.getText("gui.error.calibreQuery.interpret", e.getQuery()), e);
                } catch (CalibreSavedSearchNotFoundException e) {
                    callback.errorOccured(Localization.Main.getText("gui.error.calibreQuery.noSuchSavedSearch", e.getSavedSearchName()), null);
                }
                if (featuredBookFilter == null) {
                    int n = callback.askUser(Localization.Main.getText("gui.confirm.continueGenerating"), textYES, textNO);
                    if (n == 1) {
                        callback.endCreateMainCatalog(null, CatalogContext.INSTANCE.getHtmlManager().getTimeInHtml());
                        return;
                    }
                }
            }
            CatalogContext.INSTANCE.getCatalogManager().setFeaturedBooksFilter(featuredBookFilter);
            callback.checkIfContinueGenerating();
            Map<String, BookFilter> customCatalogsFilters = new HashMap<String, BookFilter>();
            List<Composite<String, String>> customCatalogs = ConfigurationManager.INSTANCE.getCurrentProfile().getCustomCatalogs();
            if (Helper.isNotNullOrEmpty(customCatalogs)) {
                for (Composite<String, String> customCatalog : customCatalogs) {
                    callback.checkIfContinueGenerating();
                    String customCatalogTitle = customCatalog.getFirstElement();
                    String customCatalogSearch = customCatalog.getSecondElement();
                    if (Helper.isNotNullOrEmpty(customCatalogTitle) && Helper.isNotNullOrEmpty(customCatalogSearch)) {
                        if (customCatalogSearch.toUpperCase().startsWith("HTTP://") || customCatalogSearch.toUpperCase().startsWith("HTTPS://") || customCatalogSearch.toUpperCase().startsWith("OPDS://")) continue;
                        BookFilter customCatalogFilter = null;
                        try {
                            customCatalogFilter = CalibreQueryInterpreter.interpret(customCatalogSearch);
                        } catch (CalibreSavedSearchInterpretException e) {
                            callback.errorOccured(Localization.Main.getText("gui.error.calibreQuery.interpret", e.getQuery()), e);
                        } catch (CalibreSavedSearchNotFoundException e) {
                            callback.errorOccured(Localization.Main.getText("gui.error.calibreQuery.noSuchSavedSearch", e.getSavedSearchName()), null);
                        }
                        if (customCatalogFilter == null) {
                            int n = callback.askUser(Localization.Main.getText("gui.confirm.continueGenerating"), textYES, textNO);
                            if (n == 1) {
                                callback.endCreateMainCatalog(null, CatalogContext.INSTANCE.getHtmlManager().getTimeInHtml());
                                return;
                            }
                        } else {
                            customCatalogsFilters.put(customCatalogTitle, customCatalogFilter);
                        }
                    }
                }
            }
            callback.checkIfContinueGenerating();
            try {
                RemoveFilteredOutBooks.INSTANCE.runOnDataModel();
            } catch (CalibreSavedSearchInterpretException e) {
                callback.errorOccured(Localization.Main.getText("gui.error.calibreQuery.interpret", e.getQuery()), e);
            } catch (CalibreSavedSearchNotFoundException e) {
                callback.errorOccured(Localization.Main.getText("gui.error.calibreQuery.noSuchSavedSearch", e.getSavedSearchName()), null);
            }
            List<Book> books = DataModel.INSTANCE.getListOfBooks();
            if (Helper.isNullOrEmpty(books)) {
                if (Database.INSTANCE.wasSqlEsception() == 0) {
                    callback.errorOccured(Localization.Main.getText("error.nobooks"), null);
                } else callback.errorOccured("Error accessing database: code=" + Database.INSTANCE.wasSqlEsception(), null);
                return;
            } else {
                logger.info("Database loaded: " + books.size() + " books");
            }
            callback.checkIfContinueGenerating();
            callback.endReadDatabase(System.currentTimeMillis() - now, Summarizer.INSTANCE.getBookWord(books.size()));
            now = System.currentTimeMillis();
            switch(currentProfile.getDeviceMode()) {
                case Nook:
                    CachedFileManager.INSTANCE.setCacheFolder(targetFolder);
                    break;
                default:
                    CachedFileManager.INSTANCE.setCacheFolder(catalogFolder);
                    break;
            }
            if (checkCRC) {
                if (logger.isTraceEnabled()) logger.trace("Loading Cache");
                callback.showMessage(Localization.Main.getText("info.step.loadingcache"));
                CachedFileManager.INSTANCE.loadCache();
            } else {
                if (logger.isTraceEnabled()) logger.trace("Deleting Cache");
                CachedFileManager.INSTANCE.deleteCache();
            }
            logger.info(Localization.Main.getText("info.step.donein", System.currentTimeMillis() - now));
            callback.checkIfContinueGenerating();
            String filename = SecureFileManager.INSTANCE.encode("index.xml");
            if (currentProfile.getDeviceMode() == DeviceMode.Nook) {
                TrookSpecificSearchDatabaseManager.INSTANCE.setDatabaseFile(new File(generateFolder, Constants.TROOK_SEARCH_DATABASE_FILENAME));
                TrookSpecificSearchDatabaseManager.INSTANCE.getConnection();
            }
            String title = currentProfile.getCatalogTitle();
            String urn = "calibre:catalog";
            String urlExt = "../" + filename;
            Breadcrumbs breadcrumbs = Breadcrumbs.newBreadcrumbs(title, urlExt);
            Element main = FeedHelper.INSTANCE.getFeedRootElement(null, title, urn, urlExt);
            Element entry;
            if (currentProfile.getIncludeAboutLink()) {
                entry = FeedHelper.INSTANCE.getAboutEntry(Localization.Main.getText("about.title", Constants.PROGTITLE), "urn:calibre2opds:about", Constants.HOME_URL, Localization.Main.getText("about.summary"), currentProfile.getExternalIcons() ? Icons.ICONFILE_ABOUT : Icons.ICON_ABOUT);
                if (entry != null) main.addContent(entry);
            }
            callback.checkIfContinueGenerating();
            logger.debug("STARTED: Generating All Books catalog");
            callback.startCreateAllbooks(DataModel.INSTANCE.getListOfBooks().size());
            now = System.currentTimeMillis();
            if (currentProfile.getGenerateAllbooks()) {
                entry = new AllBooksSubCatalog(books).getSubCatalogEntry(breadcrumbs).getFirstElement();
                if (entry != null) main.addContent(entry);
            }
            callback.endCreateAllbooks(System.currentTimeMillis() - now);
            logger.debug("COMPLETED: Generating All Books catalog");
            callback.checkIfContinueGenerating();
            logger.debug("STARTING: Generating Authors catalog");
            callback.startCreateAuthors(DataModel.INSTANCE.getListOfAuthors().size());
            now = System.currentTimeMillis();
            entry = new AuthorsSubCatalog(books).getSubCatalogEntry(breadcrumbs).getFirstElement();
            if (entry != null) main.addContent(entry);
            callback.endCreateAuthors(System.currentTimeMillis() - now);
            logger.debug("COMPLETED: Generating Authors catalog");
            callback.checkIfContinueGenerating();
            logger.debug("STARTING: Generating Tags catalog");
            callback.startCreateTags(DataModel.INSTANCE.getListOfTags().size());
            now = System.currentTimeMillis();
            if (currentProfile.getGenerateTags()) {
                entry = TagSubCatalog.getTagSubCatalog(books).getSubCatalogEntry(breadcrumbs).getFirstElement();
                if (entry != null) main.addContent(entry);
            }
            callback.endCreateTags(System.currentTimeMillis() - now);
            logger.debug("COMPLETED: Generating Tags catalog");
            callback.checkIfContinueGenerating();
            logger.debug("STARTING: Generating Series catalog");
            callback.startCreateSeries(DataModel.INSTANCE.getListOfSeries().size());
            now = System.currentTimeMillis();
            if (currentProfile.getGenerateSeries()) {
                Composite<Element, String> subcat = new SeriesSubCatalog(books).getSubCatalogEntry(breadcrumbs);
                if (subcat != null) {
                    entry = subcat.getFirstElement();
                    if (entry != null) main.addContent(entry);
                }
            }
            callback.endCreateSeries(System.currentTimeMillis() - now);
            logger.debug("COMPLETED: Generating Series catalog");
            callback.checkIfContinueGenerating();
            logger.debug("STARTING: Generating Recent catalog");
            int nbRecentBooks = Math.min(currentProfile.getBooksInRecentAdditions(), DataModel.INSTANCE.getListOfBooks().size());
            callback.startCreateRecent(nbRecentBooks);
            now = System.currentTimeMillis();
            if (currentProfile.getGenerateRecent()) {
                Composite<Element, String> recent = new RecentBooksSubCatalog(books).getSubCatalogEntry(breadcrumbs);
                if (recent != null) {
                    main.addContent(recent.getFirstElement());
                }
            }
            callback.endCreateRecent(System.currentTimeMillis() - now);
            logger.debug("COMPLETED: Generating Recent catalog");
            callback.checkIfContinueGenerating();
            logger.debug("STARTING: Generating Ratings catalog");
            callback.startCreateRated(DataModel.INSTANCE.getListOfBooks().size());
            now = System.currentTimeMillis();
            if (currentProfile.getGenerateRatings()) {
                entry = new RatingsSubCatalog(books).getSubCatalogEntry(breadcrumbs).getFirstElement();
                if (entry != null) main.addContent(entry);
            }
            callback.endCreateRated(System.currentTimeMillis() - now);
            logger.debug("COMPLETED: Generating Ratings catalog");
            callback.checkIfContinueGenerating();
            now = System.currentTimeMillis();
            if (CatalogContext.INSTANCE.getCatalogManager().getFeaturedBooksFilter() != null) {
                logger.debug("STARTED: Generating Featured books catalog");
                List<Book> featuredBooks = FilterHelper.filter(CatalogContext.INSTANCE.getCatalogManager().getFeaturedBooksFilter(), books);
                callback.startCreateFeaturedBooks(featuredBooks.size());
                Composite<Element, String> featuredCatalog = new FeaturedBooksSubCatalog(featuredBooks).getSubCatalogEntry(breadcrumbs);
                if (featuredCatalog != null) {
                    main.addContent(6, FeedHelper.INSTANCE.getFeaturedLink(featuredCatalog.getSecondElement(), ConfigurationManager.INSTANCE.getCurrentProfile().getFeaturedCatalogTitle()));
                    main.addContent(featuredCatalog.getFirstElement());
                }
            }
            callback.endCreateFeaturedBooks(System.currentTimeMillis() - now);
            callback.checkIfContinueGenerating();
            now = System.currentTimeMillis();
            if (Helper.isNotNullOrEmpty(customCatalogs)) {
                int pos = 1;
                logger.debug("STARTED: Generating custom catalogs");
                callback.startCreateCustomCatalogs(customCatalogs.size());
                for (Composite<String, String> customCatalog : customCatalogs) {
                    callback.checkIfContinueGenerating();
                    String customCatalogTitle = customCatalog.getFirstElement();
                    BookFilter customCatalogBookFilter = customCatalogsFilters.get(customCatalogTitle);
                    if (Helper.isNotNullOrEmpty(customCatalogTitle)) {
                        if (customCatalogBookFilter != null) {
                            if (logger.isDebugEnabled()) logger.debug("STARTED: Generating custom catalog " + title);
                            List<Book> customCatalogBooks = FilterHelper.filter(customCatalogBookFilter, books);
                            if (Helper.isNotNullOrEmpty(customCatalogBooks)) {
                                Composite<Element, String> customCatalogEntry = new CustomSubCatalog(customCatalogBooks, customCatalogTitle).getSubCatalogEntry(breadcrumbs);
                                main.addContent(customCatalogEntry.getFirstElement());
                            }
                        } else {
                            if (logger.isDebugEnabled()) logger.debug("STARTED: Adding external link " + title);
                            String externalLinkUrl = customCatalog.getSecondElement();
                            entry = FeedHelper.INSTANCE.getExternalLinkEntry(customCatalogTitle, "urn:calibre2opds:externalLink" + (pos++), externalLinkUrl, currentProfile.getExternalIcons() ? Icons.ICONFILE_EXTERNAL : Icons.ICON_EXTERNAL);
                            if (entry != null) main.addContent(entry);
                        }
                    }
                    callback.incStepProgressIndicatorPosition();
                    callback.checkIfContinueGenerating();
                }
            }
            callback.endCreateCustomCatalogs(System.currentTimeMillis() - now);
            callback.checkIfContinueGenerating();
            File outputFile = new File(CatalogContext.INSTANCE.getCatalogManager().getCatalogFolder(), filename);
            Document document = new Document();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(outputFile);
                document.addContent(main);
                JDOM.INSTANCE.getOutputter().output(document, fos);
            } finally {
                if (fos != null) fos.close();
            }
            callback.checkIfContinueGenerating();
            logger.debug("STARTED: Copying Resource files");
            for (String resource : Constants.FILE_RESOURCES) {
                callback.checkIfContinueGenerating();
                InputStream resourceStream = ConfigurationManager.INSTANCE.getResourceAsStream(resource);
                File resourceFile = new File(generateFolder, CatalogContext.INSTANCE.getCatalogManager().getCatalogFolderName() + "/" + resource);
                Helper.copy(resourceStream, resourceFile);
                logger.trace("Copying Resource " + resource);
            }
            logger.debug("COMPLETED: Copying Resource files");
            callback.checkIfContinueGenerating();
            logger.debug("STARTING: Generating Thumbnails");
            int nbThumbnails = CatalogContext.INSTANCE.getThumbnailManager().getNbImagesToGenerate();
            callback.startCreateThumbnails(nbThumbnails);
            now = System.currentTimeMillis();
            countThumbnails = CatalogContext.INSTANCE.getThumbnailManager().generateImages();
            callback.endCreateThumbnails(System.currentTimeMillis() - now);
            logger.debug("COMPLETED: Generating Thumbnails");
            callback.checkIfContinueGenerating();
            logger.debug("STARTING: Generating Reduced Covers");
            int nbCovers = CatalogContext.INSTANCE.getThumbnailManager().getNbImagesToGenerate();
            callback.startCreateCovers(nbCovers);
            now = System.currentTimeMillis();
            countCovers = CatalogContext.INSTANCE.getCoverManager().generateImages();
            callback.endCreateCovers(System.currentTimeMillis() - now);
            logger.debug("COMPLETED: Generating Reduced Covers");
            callback.checkIfContinueGenerating();
            logger.debug("STARTING: Generating Javascript database");
            long nbKeywords = IndexManager.INSTANCE.size();
            callback.startCreateJavascriptDatabase(nbKeywords);
            now = System.currentTimeMillis();
            if (currentProfile.getGenerateIndex()) IndexManager.INSTANCE.exportToJavascriptArrays();
            callback.endCreateJavascriptDatabase(System.currentTimeMillis() - now);
            logger.debug("COMPLETED: Generating Javascript database");
            callback.checkIfContinueGenerating();
            logger.debug("STARTING: Processing ePub Metadata");
            callback.startReprocessingEpubMetadata(DataModel.INSTANCE.getListOfBooks().size());
            now = System.currentTimeMillis();
            countMetadata = 0;
            if (currentProfile.getReprocessEpubMetadata()) {
                for (Book book : DataModel.INSTANCE.getListOfBooks()) {
                    callback.checkIfContinueGenerating();
                    callback.incStepProgressIndicatorPosition();
                    if (shouldReprocessEpubMetadata(book)) {
                        try {
                            callback.showMessage(book.getAuthors() + ": " + book.getTitle());
                            new OpfOutput(book).processEPubFile();
                        } catch (IOException e) {
                            String message = Localization.Main.getText("gui.error.tools.processEpubMetadataOfAllBooks", book.getTitle(), e.getMessage());
                            logger.error(message, e);
                        }
                        countMetadata++;
                    }
                }
            }
            callback.endReprocessingEpubMetadata(System.currentTimeMillis() - now);
            logger.debug("COMPLETED: Processing ePub Metadata");
            callback.checkIfContinueGenerating();
            logger.debug("STARTED: Generating HTML Files");
            CatalogContext.INSTANCE.getHtmlManager().generateHtmlFromXml(document, outputFile, HtmlManager.FeedType.MainCatalog);
            logger.debug("COMPLETED: Generating HTML Files");
            callback.checkIfContinueGenerating();
            if (syncLog) syncLogFile = new PrintWriter(ConfigurationManager.INSTANCE.getConfigurationDirectory() + "/" + Constants.LOGFILE_FOLDER + "/" + Constants.SYNCFILE_NAME);
            copyExistHits = copyLengthHits = copyCrcUnchecked = copyCrcHits = copyCrcMisses = copyDateMisses = copyCrcUnchecked = 0;
            int nbFilesToCopyToTarget = CatalogContext.INSTANCE.getCatalogManager().getListOfFilesPathsToCopy().size();
            callback.startCopyLibToTarget(nbFilesToCopyToTarget);
            if (currentProfile.getDeviceMode() != DeviceMode.Dropbox) {
                logger.debug("STARTING: syncFiles eBook files to target");
                now = System.currentTimeMillis();
                for (String pathToCopy : CatalogContext.INSTANCE.getCatalogManager().getListOfFilesPathsToCopy()) {
                    callback.checkIfContinueGenerating();
                    CachedFile sourceFile = CachedFileManager.INSTANCE.addCachedFile(currentProfile.getDatabaseFolder(), pathToCopy);
                    File targetFile = CachedFileManager.INSTANCE.addCachedFile(targetFolder, pathToCopy);
                    syncFiles(sourceFile, targetFile);
                }
                logger.debug("COMPLETED: syncFiles eBook files to target");
                callback.checkIfContinueGenerating();
                callback.showMessage(Localization.Main.getText("info.step.tidyingtarget"));
                logger.debug("STARTING: Build list of files to delete from target");
                Set<File> usefulTargetFiles = new TreeSet<File>();
                List<String> sourceFiles = new LinkedList<String>(CatalogContext.INSTANCE.getCatalogManager().getListOfFilesPathsToCopy());
                for (String sourceFile : sourceFiles) {
                    callback.checkIfContinueGenerating();
                    File targetFile = new File(targetFolder, sourceFile);
                    while (targetFile != null) {
                        usefulTargetFiles.add(targetFile);
                        targetFile = targetFile.getParentFile();
                    }
                }
                logger.debug("COMPLETED: Build list of files to delete from target");
                callback.checkIfContinueGenerating();
                logger.debug("STARTED: Creating list of files on target");
                List<File> existingTargetFiles = Helper.listFilesIn(targetFolder);
                logger.debug("COMPLETED: Creating list of files on target");
                String targetCatalogFolderPath = new File(targetFolder, CatalogContext.INSTANCE.getCatalogManager().getCatalogFolderName()).getAbsolutePath();
                String calibreFolderPath = currentProfile.getDatabaseFolder().getAbsolutePath();
                logger.debug("STARTING: Delete superfluous files from target");
                for (File existingTargetFile : existingTargetFiles) {
                    callback.checkIfContinueGenerating();
                    if (!usefulTargetFiles.contains(existingTargetFile)) {
                        if (!existingTargetFile.getAbsolutePath().startsWith(targetCatalogFolderPath)) {
                            if (!existingTargetFile.getAbsolutePath().startsWith(calibreFolderPath)) {
                                if (logger.isTraceEnabled()) logger.trace("deleting " + existingTargetFile.getPath());
                                Helper.delete(existingTargetFile);
                                if (syncLog) syncLogFile.printf("DELETED: %s\n", existingTargetFile);
                                CachedFileManager.INSTANCE.removeCachedFile(existingTargetFile);
                            }
                        }
                    }
                }
                logger.debug("COMPLETED: Delete superfluous files from target");
            }
            callback.endCopyLibToTarget(System.currentTimeMillis() - now);
            callback.checkIfContinueGenerating();
            long nbCatalogFilesToCopyToTarget = Helper.count(CatalogContext.INSTANCE.getCatalogManager().getCatalogFolder());
            callback.startCopyCatToTarget(nbCatalogFilesToCopyToTarget);
            now = System.currentTimeMillis();
            logger.debug("STARTING: syncFiles Catalog Folder");
            switch(currentProfile.getDeviceMode()) {
                case Nook:
                    if (TrookSpecificSearchDatabaseManager.INSTANCE.getDatabaseFile() != null) {
                        TrookSpecificSearchDatabaseManager.INSTANCE.closeConnection();
                        File destinationFile = new File(targetFolder, Constants.TROOK_SEARCH_DATABASE_FILENAME);
                        syncFiles(TrookSpecificSearchDatabaseManager.INSTANCE.getDatabaseFile(), destinationFile);
                    }
                    File indexFile = new File(generateFolder, "/" + CatalogContext.INSTANCE.getCatalogManager().getCatalogFolderName() + "/index.xml");
                    File catalogFile = new File(generateFolder, "/" + CatalogContext.INSTANCE.getCatalogManager().getCatalogFolderName() + "/catalog.xml");
                    if (logger.isTraceEnabled()) logger.trace("copy '" + indexFile + "' to '" + catalogFile + "'");
                    syncFiles(indexFile, catalogFile);
                    File targetCatalogZipFile = new File(targetFolder, Constants.TROOK_CATALOG_FILENAME);
                    if (targetCatalogZipFile.exists()) {
                        targetCatalogZipFile.delete();
                    }
                    if (currentProfile.getZipTrookCatalog()) {
                        Helper.recursivelyZipFiles(CatalogContext.INSTANCE.getCatalogManager().getCatalogFolder(), true, targetCatalogZipFile);
                        File targetCatalogFolder = new File(targetFolder, CatalogContext.INSTANCE.getCatalogManager().getCatalogFolderName());
                        Helper.delete(targetCatalogFolder);
                        break;
                    }
                case Nas:
                    File targetCatalogFolder = new File(targetFolder, CatalogContext.INSTANCE.getCatalogManager().getCatalogFolderName());
                    syncFiles(CatalogContext.INSTANCE.getCatalogManager().getCatalogFolder(), targetCatalogFolder);
                    break;
                case Dropbox:
                    break;
            }
            logger.debug("COMPLETED: syncFiles Catalog Folder");
            callback.checkIfContinueGenerating();
            if (currentProfile.getCopyToDatabaseFolder()) {
                logger.debug("STARTING: Copy Catalog Folder to Database Folder");
                File generateCatalogFolder = new File(generateFolder, CatalogContext.INSTANCE.getCatalogManager().getCatalogFolderName());
                File libraryCatalogFolder = new File(libraryFolder, CatalogContext.INSTANCE.getCatalogManager().getCatalogFolderName());
                ;
                syncFiles(generateCatalogFolder, libraryCatalogFolder);
                logger.debug("COMPLETED: Copy Catalog Folder to Database Folder");
            }
            callback.endCopyCatToTarget(System.currentTimeMillis() - now);
            callback.checkIfContinueGenerating();
            if (syncLog) {
                logger.info("Sync Log: " + ConfigurationManager.INSTANCE.getConfigurationDirectory() + "/" + Constants.LOGFILE_FOLDER + "/" + Constants.SYNCFILE_NAME);
            }
            now = System.currentTimeMillis();
            logger.info(Localization.Main.getText("info.step.savingcache"));
            callback.showMessage(Localization.Main.getText("info.step.savingcache"));
            CachedFileManager.INSTANCE.saveCache();
            logger.info(Localization.Main.getText("info.step.donein", System.currentTimeMillis() - now));
            callback.checkIfContinueGenerating();
            callback.showMessage("Saving Cache data");
            SecureFileManager.INSTANCE.save();
            callback.checkIfContinueGenerating();
            if (syncLog) {
                syncLogFile.println();
                syncLogFile.println(Localization.Main.getText("stats.copy.header"));
                syncLogFile.println(String.format("%8d  ", copyExistHits) + Localization.Main.getText("stats.copy.notexist"));
                syncLogFile.println(String.format("%8d  ", copyLengthHits) + Localization.Main.getText("stats.copy.lengthdiffer"));
                syncLogFile.println(String.format("%8d  ", copyCrcUnchecked) + Localization.Main.getText("stats.copy.unchecked"));
                syncLogFile.println(String.format("%8d  ", copyCrcHits) + Localization.Main.getText("stats.copy.crcdiffer"));
                syncLogFile.println(String.format("%8d  ", copyCrcMisses) + Localization.Main.getText("stats.copy.crcsame"));
                syncLogFile.println(String.format("%8d  ", copyDateMisses) + Localization.Main.getText("stats.copy.older"));
                syncLogFile.close();
            }
            logger.info("");
            logger.info(Localization.Main.getText("stats.library.header"));
            logger.info(String.format("%8d  ", DataModel.INSTANCE.getListOfBooks().size()) + Localization.Main.getText("bookword.title"));
            logger.info(String.format("%8d  ", DataModel.INSTANCE.getListOfAuthors().size()) + Localization.Main.getText("authorword.title"));
            logger.info(String.format("%8d  ", DataModel.INSTANCE.getListOfSeries().size()) + Localization.Main.getText("seriesword.title"));
            logger.info(String.format("%8d  ", DataModel.INSTANCE.getListOfTags().size()) + Localization.Main.getText("tagword.title"));
            logger.info("");
            logger.info(Localization.Main.getText("stats.run.header"));
            logger.info(String.format("%8d  ", countMetadata) + Localization.Main.getText("stats.run.metadata"));
            logger.info(String.format("%8d  ", countThumbnails) + Localization.Main.getText("stats.run.thumbnails"));
            logger.info(String.format("%8d  ", countCovers) + Localization.Main.getText("stats.run.covers"));
            logger.info("");
            logger.info(Localization.Main.getText("stats.copy.header"));
            logger.info(String.format("%8d  ", copyExistHits) + Localization.Main.getText("stats.copy.notexist"));
            logger.info(String.format("%8d  ", copyLengthHits) + Localization.Main.getText("stats.copy.lengthdiffer"));
            logger.info(String.format("%8d  ", copyCrcUnchecked) + Localization.Main.getText("stats.copy.unchecked"));
            logger.info(String.format("%8d  ", copyCrcHits) + Localization.Main.getText("stats.copy.crcdiffer"));
            logger.info(String.format("%8d  ", copyCrcMisses) + Localization.Main.getText("stats.copy.crcsame"));
            logger.info(String.format("%8d  ", copyDateMisses) + Localization.Main.getText("stats.copy.older"));
            logger.info("");
            if (copyToSelf != 0) logger.warn(String.format("%8d  ", copyToSelf) + Localization.Main.getText("stats.copy.toself"));
            if (logger.isTraceEnabled()) logger.trace("try to determine where the results have been put");
            switch(currentProfile.getDeviceMode()) {
                case Nook:
                    where = Localization.Main.getText("info.step.done.nook");
                    break;
                case Nas:
                    where = currentProfile.getTargetFolder().getPath();
                    break;
                case Dropbox:
                    File libraryCatalogFolder = new File(libraryFolder, currentProfile.getCatalogFolderName());
                    where = libraryCatalogFolder.getPath();
                    break;
            }
            if (targetFolder != null && currentProfile.getCopyToDatabaseFolder()) {
                where = where + " " + Localization.Main.getText("info.step.done.andYourDb");
            }
            if (logger.isTraceEnabled()) logger.trace("where=" + where);
            callback.checkIfContinueGenerating();
        } catch (GenerationStoppedException gse) {
            generationStopped = true;
        } catch (Throwable t) {
            generationCrashed = true;
            logger.error(" ");
            logger.error("*************************************************");
            logger.error(Localization.Main.getText("error.unexpectedFatal").toUpperCase());
            logger.error(Localization.Main.getText("error.cause").toUpperCase() + ": " + t + ": " + t.getCause());
            logger.error(Localization.Main.getText("error.message").toUpperCase() + ": " + t.getMessage());
            logger.error(Localization.Main.getText("error.stackTrace").toUpperCase() + ":");
            for (StackTraceElement element : t.getStackTrace()) logger.error(element.toString());
            logger.error("*************************************************");
            logger.error(" ");
        } finally {
            long now = System.currentTimeMillis();
            logger.info(Localization.Main.getText("info.step.deletingfiles"));
            if (generateFolder != null) {
                callback.showMessage(Localization.Main.getText("info.step.deletingfiles"));
                Helper.delete(generateFolder);
            }
            logger.info(Localization.Main.getText("info.step.donein", System.currentTimeMillis() - now));
            if (generationStopped) callback.errorOccured(Localization.Main.getText("error.userAbort"), null); else if (generationCrashed) callback.errorOccured(Localization.Main.getText("error.unexpectedFatal"), null); else callback.endCreateMainCatalog(where, CatalogContext.INSTANCE.getHtmlManager().getTimeInHtml());
        }
    }

    /**
   * Check to see if there appears to already be an existing calibre2opds catalog
   * at the specified location (by checking for specific files).  Note that a false
   * is always definitive, while a true could return a false (although unlikely) positive.
   *
   * @param catalogParentFolder    Path that contains the catalog folder
   * @param checkCatalogFolderOnly Set to true if it is OK if parent exists and catalog does not
   * @return true if cataog appears to be present
   *         false if catalog definitely not there.
   */
    private boolean checkCatalogExistence(File catalogParentFolder, boolean checkCatalogFolderOnly) {
        if (!catalogParentFolder.exists()) {
            if (logger.isTraceEnabled()) logger.trace("checkCatalogExistence: true (parent does not exist");
            return true;
        }
        switch(currentProfile.getDeviceMode()) {
            case Nook:
                File trookFile = new File(catalogParentFolder, Constants.TROOK_SEARCH_DATABASE_FILENAME);
                if (!trookFile.exists()) {
                    if (logger.isTraceEnabled()) logger.trace("checkCatalogExistence: false (trook database file does not exist");
                    return false;
                }
                break;
            default:
                File catalogFolder = new File(catalogParentFolder, CatalogContext.INSTANCE.getCatalogManager().getCatalogFolderName());
                if ((false == catalogFolder.exists()) && (true == checkCatalogFolderOnly)) {
                    if (logger.isTraceEnabled()) logger.trace("checkCatalogExistence: true (catalog folder does not exist");
                    return true;
                }
                if (logger.isTraceEnabled()) logger.trace("checkCatalogExistence: Check for catalog at " + catalogFolder.getPath());
                if (!catalogFolder.exists()) {
                    if (logger.isTraceEnabled()) logger.trace("checkCatalogExistence: false (catalog folder does not exist)");
                    return false;
                }
                File desktopFile = new File(catalogFolder, "desktop.css");
                if (!desktopFile.exists()) {
                    if (logger.isTraceEnabled()) logger.trace("checkCatalogExistence: false (desktop.css file does not exist)");
                    return false;
                }
                File mobileFile = new File(catalogFolder, "mobile.css");
                if (!mobileFile.exists()) {
                    if (logger.isTraceEnabled()) logger.trace("checkCatalogExistence: false (desktop.css file does not exist)");
                    return false;
                }
                break;
        }
        if (logger.isTraceEnabled()) logger.trace("checkCatalogExistence: true");
        return true;
    }
}
