package org.sf.smartsite;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import org.sf.bee.commons.localstore.BeeLocalStore;
import org.sf.bee.commons.localstore.BeeResource;
import org.sf.bee.commons.logging.Level;
import org.sf.bee.commons.logging.LogItemRepository;
import org.sf.bee.commons.logging.Logger;
import org.sf.bee.commons.logging.util.LoggingUtils;
import org.sf.bee.commons.util.StringUtils;
import org.sf.bee.commons.util.fs.PathUtils;
import org.sf.bee.jsdb.file.JSDBFile;
import org.sf.smartsite.jsdb.SmartData;
import org.sf.smartsite.templating.SmartTemplate;
import org.sf.smartsite.templating.SmartToolbox;
import org.sf.smartsite.deploy.root.Root;
import org.sf.smartsite.impexp.SmartExporter;
import org.sf.smartsite.impexp.SmartImporter;

/**
 *
 * @author angelo.geminiani
 */
public class SmartLibFacade {

    public static final String NAME = "smartsiteclient";

    public static final String PATH_ROOT = PathUtils.validateFolderSeparator(BeeLocalStore.getInstance().getRoot().concat("/").concat(NAME));

    public static final String PATH_TEMPLATES = PATH_ROOT.concat("/templates");

    public static final String PATH_UTILS = PATH_ROOT.concat("/utils");

    public static final String PATH_UTILS_EDITOR = PATH_UTILS.concat("/jsoneditor/JSONeditor.html");

    public static final String PATH_UTILS_HELP = "http://www.smartfeeling.org/smartsites/tutorial/";

    public static final String PATH_STORE = PATH_ROOT.concat("/data");

    public static final String PATH_OUTPUTSITES = PATH_ROOT.concat("/sites");

    public static final String PATH_LOGS = PATH_ROOT.concat("/log");

    public static final String PATH_IMPORTER = PATH_STORE.concat("/import");

    public static final String PATH_EXPORTER = PATH_STORE.concat("/export");

    private boolean _active;

    private SmartLibFacade() {
        this.init();
    }

    public boolean isActive() {
        return _active;
    }

    public void close() {
        _active = false;
    }

    public String getPathTemplates() {
        return PATH_TEMPLATES;
    }

    public String getPathOutputSites() {
        return PATH_OUTPUTSITES;
    }

    public SmartTemplate getTemplateManager() {
        return SmartTemplate.getInstance();
    }

    public JSDBFile getDB() {
        return SmartData.getInstance().getDb();
    }

    public void openUrl(final String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public void openFile(final String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(url));
            }
        } catch (Exception ex) {
            this.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Import CSV File into collections
     * @param fileName (Optional) 
     */
    public void importCSVFile(final String fileName) throws Exception {
        final SmartImporter importer = new SmartImporter();
        if (StringUtils.hasText(fileName)) {
            importer.load(fileName);
        } else {
            importer.load();
        }
    }

    /**
     * Export collection into CSV file
     * @param collectionName
     * @throws Exception
     */
    public void exportCSVFile(final String collectionName) throws Exception {
        final SmartExporter importer = new SmartExporter();
        if (StringUtils.hasText(collectionName)) {
            importer.export(collectionName);
        } else {
            importer.export();
        }
    }

    private void init() {
        _active = true;
        SmartData.getInstance();
        LogItemRepository.getInstance().setFilePath(PATH_LOGS + "/logging.log");
        Root.deploy();
        SmartToolbox.getInstance();
        this.initFolders();
    }

    private Logger getLogger() {
        return LoggingUtils.getLogger(this);
    }

    private void initFolders() {
        final BeeResource imp = new BeeResource(PATH_IMPORTER.concat("/foo.txt"));
        imp.mkdirs();
        final BeeResource exp = new BeeResource(PATH_EXPORTER.concat("/foo.txt"));
        exp.mkdirs();
        final BeeResource sites = new BeeResource(PATH_OUTPUTSITES.concat("/foo.txt"));
        sites.mkdirs();
    }

    private static SmartLibFacade __instance;

    public static SmartLibFacade getInstance() {
        if (null == __instance) {
            __instance = new SmartLibFacade();
        }
        return __instance;
    }
}
