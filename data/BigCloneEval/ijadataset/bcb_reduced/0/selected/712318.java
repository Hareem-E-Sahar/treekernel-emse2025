package org.gudy.azureus2.update;

import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.net.*;
import java.io.*;
import org.gudy.azureus2.core3.util.*;
import org.gudy.azureus2.core3.logging.*;
import org.gudy.azureus2.core3.config.*;
import org.gudy.azureus2.core3.html.*;
import org.gudy.azureus2.core3.internat.MessageText;
import org.gudy.azureus2.platform.win32.access.AEWin32Access;
import org.gudy.azureus2.platform.win32.access.AEWin32Manager;
import org.gudy.azureus2.plugins.*;
import org.gudy.azureus2.plugins.logging.LoggerChannel;
import org.gudy.azureus2.plugins.ui.UIManager;
import org.gudy.azureus2.plugins.ui.UIManagerEvent;
import org.gudy.azureus2.plugins.update.*;
import org.gudy.azureus2.plugins.utils.StaticUtilities;
import org.gudy.azureus2.plugins.utils.resourcedownloader.*;
import com.aelitis.azureus.core.util.GeneralUtils;
import com.aelitis.azureus.core.versioncheck.*;
import com.aelitis.azureus.ui.UIFunctions;
import com.aelitis.azureus.ui.UIFunctionsManager;

public class CoreUpdateChecker implements Plugin, UpdatableComponent {

    public static final String LATEST_VERSION_PROPERTY = "latest_version";

    public static final String MESSAGE_PROPERTY = "message";

    public static final int RD_GET_DETAILS_RETRIES = 3;

    public static final int RD_GET_MIRRORS_RETRIES = 3;

    public static final int RD_SIZE_RETRIES = 3;

    public static final int RD_SIZE_TIMEOUT = 10000;

    protected static CoreUpdateChecker singleton;

    protected PluginInterface plugin_interface;

    protected ResourceDownloaderFactory rdf;

    protected LoggerChannel log;

    protected ResourceDownloaderListener rd_logger;

    protected boolean first_check = true;

    public static void doUsageStats() {
        singleton.doUsageStatsSupport();
    }

    public CoreUpdateChecker() {
        singleton = this;
    }

    protected void doUsageStatsSupport() {
        try {
            Map decoded = VersionCheckClient.getSingleton().getVersionCheckInfo(first_check ? VersionCheckClient.REASON_UPDATE_CHECK_START : VersionCheckClient.REASON_UPDATE_CHECK_PERIODIC);
            displayUserMessage(decoded);
        } finally {
            first_check = false;
        }
    }

    public void initialize(PluginInterface _plugin_interface) {
        plugin_interface = _plugin_interface;
        plugin_interface.getPluginProperties().setProperty("plugin.name", "Core Updater");
        log = plugin_interface.getLogger().getChannel("CoreUpdater");
        rd_logger = new ResourceDownloaderAdapter() {

            public void reportActivity(ResourceDownloader downloader, String activity) {
                log.log(activity);
            }
        };
        Properties props = plugin_interface.getPluginProperties();
        props.setProperty("plugin.version", plugin_interface.getAzureusVersion());
        rdf = plugin_interface.getUtilities().getResourceDownloaderFactory();
        plugin_interface.getUpdateManager().registerUpdatableComponent(this, true);
    }

    public String getName() {
        return ("Azureus Core");
    }

    public int getMaximumCheckTime() {
        return ((RD_SIZE_RETRIES * RD_SIZE_TIMEOUT) / 1000);
    }

    public void checkForUpdate(final UpdateChecker checker) {
        try {
            String current_version = plugin_interface.getAzureusVersion();
            log.log("Update check starts: current = " + current_version);
            Map decoded = VersionCheckClient.getSingleton().getVersionCheckInfo(first_check ? VersionCheckClient.REASON_UPDATE_CHECK_START : VersionCheckClient.REASON_UPDATE_CHECK_PERIODIC);
            displayUserMessage(decoded);
            if (decoded.isEmpty()) {
                return;
            }
            String latest_version;
            String latest_file_name;
            byte[] b_version = (byte[]) decoded.get("version");
            if (b_version != null) {
                latest_version = new String(b_version);
                plugin_interface.getPluginProperties().setProperty(LATEST_VERSION_PROPERTY, latest_version);
            } else {
                throw (new Exception("No version found in reply"));
            }
            byte[] b_filename = (byte[]) decoded.get("filename");
            if (b_filename != null) {
                latest_file_name = new String(b_filename);
            } else {
                throw (new Exception("No update file details in reply"));
            }
            String msg = "Core: latest_version = '" + latest_version + "', file = '" + latest_file_name + "'";
            URL full_download_url;
            if (latest_file_name.startsWith("http")) {
                try {
                    full_download_url = new URL(latest_file_name);
                } catch (Throwable e) {
                    full_download_url = null;
                    log.log(e);
                }
                int pos = latest_file_name.lastIndexOf('/');
                latest_file_name = latest_file_name.substring(pos + 1);
            } else {
                full_download_url = null;
            }
            checker.reportProgress(msg);
            log.log(msg);
            if (!shouldUpdate(current_version, latest_version)) {
                return;
            }
            final String f_latest_version = latest_version;
            final String f_latest_file_name = latest_file_name;
            ResourceDownloader top_downloader;
            if (full_download_url == null) {
                ResourceDownloader[] primary_mirrors;
                primary_mirrors = getPrimaryDownloaders(latest_file_name);
                ResourceDownloader random_primary_mirrors = rdf.getRandomDownloader(primary_mirrors);
                ResourceDownloader backup_downloader = rdf.create(new ResourceDownloaderDelayedFactory() {

                    public ResourceDownloader create() {
                        ResourceDownloader[] backup_mirrors = getBackupDownloaders(f_latest_file_name);
                        return (rdf.getRandomDownloader(backup_mirrors));
                    }
                });
                top_downloader = rdf.getAlternateDownloader(new ResourceDownloader[] { random_primary_mirrors, backup_downloader });
            } else {
                ResourceDownloader full_rd = rdf.create(full_download_url);
                full_rd = rdf.getSuffixBasedDownloader(full_rd);
                ResourceDownloader primary_downloader = rdf.create(new ResourceDownloaderDelayedFactory() {

                    public ResourceDownloader create() {
                        ResourceDownloader[] primary_mirrors = getPrimaryDownloaders(f_latest_file_name);
                        return (rdf.getRandomDownloader(primary_mirrors));
                    }
                });
                ResourceDownloader backup_downloader = rdf.create(new ResourceDownloaderDelayedFactory() {

                    public ResourceDownloader create() {
                        ResourceDownloader[] backup_mirrors = getBackupDownloaders(f_latest_file_name);
                        return (rdf.getRandomDownloader(backup_mirrors));
                    }
                });
                top_downloader = rdf.getAlternateDownloader(new ResourceDownloader[] { full_rd, primary_downloader, backup_downloader });
            }
            top_downloader.addListener(rd_logger);
            top_downloader.getSize();
            byte[] info_b = (byte[]) decoded.get("info");
            String info = null;
            if (info_b != null) {
                try {
                    info = new String(info_b, "UTF-8");
                } catch (Throwable e) {
                    Debug.printStackTrace(e);
                }
            }
            byte[] info_url_bytes = (byte[]) decoded.get("info_url");
            String info_url = null;
            if (info_url_bytes != null) {
                try {
                    info_url = new String(info_url_bytes);
                } catch (Exception e) {
                    Debug.out(e);
                }
            }
            if (info != null || info_url != null) {
                String check;
                if (info == null) {
                    check = info_url;
                } else if (info_url == null) {
                    check = info;
                } else {
                    check = info + "|" + info_url;
                }
                byte[] sig = (byte[]) decoded.get("info_sig");
                boolean ok = false;
                if (sig == null) {
                    Logger.log(new LogEvent(LogIDs.LOGGER, "info signature check failed - missing signature"));
                } else {
                    try {
                        AEVerifier.verifyData(check, sig);
                        ok = true;
                    } catch (Throwable e) {
                        Logger.log(new LogEvent(LogIDs.LOGGER, "info signature check failed", e));
                    }
                }
                if (!ok) {
                    info = null;
                    info_url = null;
                }
            }
            String[] desc;
            if (info == null) {
                desc = new String[] { "Core Azureus Version" };
            } else {
                desc = new String[] { "Core Azureus Version", info };
            }
            final Update update = checker.addUpdate("Core Azureus Version", desc, latest_version, top_downloader, Update.RESTART_REQUIRED_YES);
            if (info_url != null) {
                update.setDescriptionURL(info_url);
            }
            top_downloader.addListener(new ResourceDownloaderAdapter() {

                public boolean completed(final ResourceDownloader downloader, InputStream data) {
                    installUpdate(checker, update, downloader, f_latest_file_name, f_latest_version, data);
                    return (true);
                }

                public void failed(ResourceDownloader downloader, ResourceDownloaderException e) {
                    Debug.out(downloader.getName() + " failed", e);
                    update.complete(false);
                }
            });
        } catch (Throwable e) {
            log.log(e);
            Debug.printStackTrace(e);
            checker.reportProgress("Failed to check for core update: " + Debug.getNestedExceptionMessage(e));
            checker.failed();
        } finally {
            checker.completed();
            first_check = false;
        }
    }

    /**
   * Log and display a user message if contained within reply.
   * @param reply from server
   */
    private void displayUserMessage(Map reply) {
        try {
            Iterator it = reply.keySet().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                if (key.startsWith("message_sig") || !key.startsWith("message")) {
                    continue;
                }
                byte[] message_bytes = (byte[]) reply.get(key);
                if (message_bytes != null && message_bytes.length > 0) {
                    String message;
                    try {
                        message = new String(message_bytes, "UTF-8");
                    } catch (Throwable e) {
                        message = new String(message_bytes);
                    }
                    String sig_key;
                    int pos = key.indexOf('_');
                    if (pos == -1) {
                        sig_key = "message_sig";
                    } else {
                        sig_key = "message_sig" + key.substring(pos);
                    }
                    String last_message_key = "CoreUpdateChecker.last" + key;
                    String last = COConfigurationManager.getStringParameter(last_message_key, "");
                    if (!message.equals(last)) {
                        byte[] signature = (byte[]) reply.get(sig_key);
                        if (signature == null) {
                            Logger.log(new LogEvent(LogIDs.LOGGER, "Signature missing from message"));
                            return;
                        }
                        try {
                            AEVerifier.verifyData(message, signature);
                        } catch (Throwable e) {
                            Logger.log(new LogEvent(LogIDs.LOGGER, "Message signature check failed", e));
                            return;
                        }
                        boolean completed = false;
                        if (message.startsWith("x:")) {
                            try {
                                URL jar_url = new URL(message.substring(2));
                                Logger.log(new LogEvent(LogIDs.LOGGER, "Patch application requsted: url=" + jar_url));
                                File temp_dir = AETemporaryFileHandler.createTempDir();
                                File jar_file = new File(temp_dir, "patch.jar");
                                InputStream is = rdf.create(jar_url).download();
                                try {
                                    FileUtil.copyFile(is, jar_file);
                                    is = null;
                                    AEVerifier.verifyData(jar_file);
                                    ClassLoader cl = CoreUpdateChecker.class.getClassLoader();
                                    if (cl instanceof URLClassLoader) {
                                        URL[] old = ((URLClassLoader) cl).getURLs();
                                        URL[] new_urls = new URL[old.length + 1];
                                        System.arraycopy(old, 0, new_urls, 1, old.length);
                                        new_urls[0] = jar_file.toURL();
                                        cl = new URLClassLoader(new_urls, cl);
                                    } else {
                                        cl = new URLClassLoader(new URL[] { jar_file.toURL() }, cl);
                                    }
                                    Class cla = cl.loadClass("org.gudy.azureus2.update.version.Patch");
                                    cla.newInstance();
                                    completed = true;
                                } finally {
                                    if (is != null) {
                                        is.close();
                                    }
                                }
                            } catch (Throwable e) {
                                Logger.log(new LogEvent(LogIDs.LOGGER, "Patch application failed", e));
                            }
                        } else if (message.startsWith("u:") && message.length() > 4) {
                            try {
                                String type = message.substring(2, 3);
                                String url = message.substring(4);
                                UIFunctions uif = UIFunctionsManager.getUIFunctions();
                                if (uif != null) {
                                    uif.viewURL(url, null, 0.9, 0.9, true, type.equals("1"));
                                }
                            } catch (Throwable t) {
                                Logger.log(new LogEvent(LogIDs.LOGGER, "URL message failed", t));
                            }
                            completed = true;
                        } else {
                            int alert_type = LogAlert.AT_WARNING;
                            String alert_text = message;
                            boolean force = false;
                            if (alert_text.startsWith("f:")) {
                                force = true;
                                alert_text = alert_text.substring(2);
                            }
                            if (alert_text.startsWith("i:")) {
                                alert_type = LogAlert.AT_INFORMATION;
                                alert_text = alert_text.substring(2);
                            }
                            plugin_interface.getPluginProperties().setProperty(MESSAGE_PROPERTY, alert_text);
                            if (force) {
                                UIFunctions uif = UIFunctionsManager.getUIFunctions();
                                if (uif != null) {
                                    try {
                                        uif.forceNotify(UIFunctions.STATUSICON_NONE, null, alert_text, null, null, 0);
                                        completed = true;
                                    } catch (Throwable e) {
                                    }
                                }
                            }
                            if (!completed) {
                                Logger.log(new LogAlert(LogAlert.UNREPEATABLE, alert_type, alert_text, 0));
                            }
                            completed = true;
                        }
                        if (completed) {
                            COConfigurationManager.setParameter(last_message_key, message);
                            COConfigurationManager.save();
                        }
                    }
                }
            }
        } catch (Throwable e) {
            Debug.printStackTrace(e);
        }
    }

    protected ResourceDownloader[] getPrimaryDownloaders(String latest_file_name) {
        log.log("Downloading primary mirrors");
        List res = new ArrayList();
        try {
            if (latest_file_name == null) {
                res.add(new URL(Constants.SF_WEB_SITE + "Azureus2.jar"));
            } else {
                URL mirrors_url = new URL("http://prdownloads.sourceforge.net/azureus/" + latest_file_name + "?download");
                ResourceDownloader rd = rdf.create(mirrors_url);
                rd = rdf.getRetryDownloader(rd, RD_GET_MIRRORS_RETRIES);
                rd.addListener(rd_logger);
                String page = HTMLPageFactory.loadPage(rd.download()).getContent();
                String pattern = "/azureus/" + latest_file_name + "?use_mirror=";
                int position = page.indexOf(pattern);
                while (position > 0) {
                    int end = page.indexOf(">", position);
                    if (end < 0) {
                        position = -1;
                    } else {
                        String mirror = page.substring(position, end);
                        if (mirror.endsWith("\"")) {
                            mirror = mirror.substring(0, mirror.length() - 1);
                        }
                        try {
                            res.add(new URL("http://prdownloads.sourceforge.net" + mirror));
                        } catch (Throwable e) {
                            log.log("Invalid URL read:" + mirror, e);
                        }
                        position = page.indexOf(pattern, position + 1);
                    }
                }
            }
        } catch (Throwable e) {
            log.log("Failed to read primary mirror list", e);
        }
        ResourceDownloader[] dls = new ResourceDownloader[res.size()];
        for (int i = 0; i < res.size(); i++) {
            URL url = (URL) res.get(i);
            log.log("    Primary mirror:" + url.toString());
            ResourceDownloader dl = rdf.create(url);
            dl = rdf.getMetaRefreshDownloader(dl);
            dl = rdf.getSuffixBasedDownloader(dl);
            dls[i] = dl;
        }
        return (dls);
    }

    protected ResourceDownloader[] getBackupDownloaders(String latest_file_name) {
        List res = new ArrayList();
        try {
            if (latest_file_name != null) {
                log.log("Downloading backup mirrors");
                URL mirrors_url = new URL("http://azureus.sourceforge.net/mirrors.php");
                ResourceDownloader rd = rdf.create(mirrors_url);
                rd = rdf.getRetryDownloader(rd, RD_GET_MIRRORS_RETRIES);
                rd.addListener(rd_logger);
                BufferedInputStream data = new BufferedInputStream(rd.download());
                Map decoded = BDecoder.decode(data);
                data.close();
                List mirrors = (List) decoded.get("mirrors");
                for (int i = 0; i < mirrors.size(); i++) {
                    String mirror = new String((byte[]) mirrors.get(i));
                    try {
                        res.add(new URL(mirror + latest_file_name));
                    } catch (Throwable e) {
                        log.log("Invalid URL read:" + mirror, e);
                    }
                }
            }
        } catch (Throwable e) {
            log.log("Failed to read backup mirror list", e);
        }
        ResourceDownloader[] dls = new ResourceDownloader[res.size()];
        for (int i = 0; i < res.size(); i++) {
            URL url = (URL) res.get(i);
            log.log("    Backup mirror:" + url.toString());
            ResourceDownloader dl = rdf.create(url);
            dl = rdf.getSuffixBasedDownloader(dl);
            dls[i] = dl;
        }
        return (dls);
    }

    protected void installUpdate(UpdateChecker checker, Update update, ResourceDownloader rd, String filename, String version, InputStream data) {
        try {
            data = update.verifyData(data, true);
            rd.reportActivity("Data verified successfully");
            if (filename.toLowerCase().endsWith(".zip.torrent")) {
                handleZIPUpdate(checker, data);
            } else {
                String temp_jar_name = "Azureus2_" + version + ".jar";
                String target_jar_name = "Azureus2.jar";
                UpdateInstaller installer = checker.createInstaller();
                installer.addResource(temp_jar_name, data);
                if (Constants.isOSX) {
                    installer.addMoveAction(temp_jar_name, installer.getInstallDir() + "/" + SystemProperties.getApplicationName() + ".app/Contents/Resources/Java/" + target_jar_name);
                } else {
                    installer.addMoveAction(temp_jar_name, installer.getInstallDir() + File.separator + target_jar_name);
                }
            }
            update.complete(true);
        } catch (Throwable e) {
            update.complete(false);
            rd.reportActivity("Update install failed:" + e.getMessage());
        }
    }

    protected void handleZIPUpdate(UpdateChecker checker, InputStream data) throws Exception {
        ZipInputStream zip = null;
        Properties update_properties = new Properties();
        File temp_dir = AETemporaryFileHandler.createTempDir();
        File update_file = null;
        try {
            zip = new ZipInputStream(data);
            ZipEntry entry = null;
            while ((entry = zip.getNextEntry()) != null) {
                String name = entry.getName().trim();
                if (name.equals("azureus.sig") || name.endsWith("/") || name.length() == 0) {
                    continue;
                }
                if (name.equals("update.properties")) {
                    update_properties.load(zip);
                } else {
                    if (update_file != null) {
                        throw (new Exception("Multiple update files are not supported"));
                    }
                    update_file = new File(temp_dir, name);
                    FileUtil.copyFile(zip, update_file, false);
                }
            }
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (Throwable e) {
                }
            }
        }
        if (update_properties == null) {
            throw (new Exception("Update properties missing"));
        }
        if (update_file == null) {
            throw (new Exception("Update file missing"));
        }
        String info_url = update_properties.getProperty("info.url");
        if (info_url == null) {
            throw (new Exception("Update property 'info.url' missing"));
        }
        String s_args = update_properties.getProperty("launch.args", "").trim();
        final String[] args;
        if (s_args.length() > 0) {
            args = GeneralUtils.splitQuotedTokens(s_args);
        } else {
            args = new String[0];
        }
        UIFunctions uif = UIFunctionsManager.getUIFunctions();
        if (uif == null) {
            throw (new Exception("Update can't proceed - UI functions unavailable"));
        }
        checker.getCheckInstance().setProperty(UpdateCheckInstance.PT_CLOSE_OR_RESTART_ALREADY_IN_PROGRESS, true);
        final File f_update_file = update_file;
        boolean silent = update_properties.getProperty("launch.silent", "false").equals("true");
        if (silent) {
            if (Constants.isOSX) {
                String app_name = SystemProperties.getApplicationName();
                if (!(app_name.equals("Vuze") || app_name.equals("Azureus"))) {
                    UIManager ui_manager = StaticUtilities.getUIManager(120 * 1000);
                    String details = MessageText.getString("update.fail.app.changed", new String[] { app_name });
                    ui_manager.showMessageBox("update.fail.app.changed.title", "!" + details + "!", UIManagerEvent.MT_OK);
                    return;
                }
            }
            uif.performAction(UIFunctions.ACTION_UPDATE_RESTART_REQUEST, !FileUtil.canReallyWriteToAppDirectory(), new UIFunctions.actionListener() {

                public void actionComplete(Object result) {
                    if ((Boolean) result) {
                        launchUpdate(f_update_file, args);
                    }
                }
            });
        } else {
            uif.performAction(UIFunctions.ACTION_FULL_UPDATE, info_url, new UIFunctions.actionListener() {

                public void actionComplete(Object result) {
                    if ((Boolean) result) {
                        launchUpdate(f_update_file, args);
                    }
                }
            });
        }
    }

    protected void launchUpdate(File file, String[] args) {
        try {
            if (file.getName().endsWith(".exe")) {
                try {
                    AEWin32Access accessor = AEWin32Manager.getAccessor(true);
                    String s_args = null;
                    if (args.length > 0) {
                        s_args = "";
                        for (String s : args) {
                            s_args += (s_args.length() == 0 ? "" : " ") + s;
                        }
                    }
                    accessor.shellExecute(null, file.getAbsolutePath(), s_args, SystemProperties.getApplicationPath(), AEWin32Access.SW_NORMAL);
                } catch (Throwable e) {
                    Logger.log(new LogEvent(LogIDs.LOGGER, "AEWin32Access failed", e));
                    if (args.length > 0) {
                        String[] s_args = new String[args.length + 1];
                        s_args[0] = file.getAbsolutePath();
                        System.arraycopy(args, 0, s_args, 1, args.length);
                        Runtime.getRuntime().exec(s_args);
                    } else {
                        Runtime.getRuntime().exec(file.getAbsolutePath());
                    }
                }
            } else {
                File dir = file.getParentFile();
                ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)));
                Throwable unzip_error = null;
                String chmod_command = findCommand("chmod");
                try {
                    while (true) {
                        ZipEntry entry = zis.getNextEntry();
                        if (entry == null) {
                            break;
                        }
                        if (entry.isDirectory()) {
                            continue;
                        }
                        String name = entry.getName();
                        FileOutputStream entry_os = null;
                        File entry_file = null;
                        if (!name.endsWith("/")) {
                            entry_file = new File(dir, name.replace('/', File.separatorChar));
                            entry_file.getParentFile().mkdirs();
                            entry_os = new FileOutputStream(entry_file);
                        }
                        try {
                            byte[] buffer = new byte[65536];
                            while (true) {
                                int len = zis.read(buffer);
                                if (len <= 0) {
                                    break;
                                }
                                if (entry_os != null) {
                                    entry_os.write(buffer, 0, len);
                                }
                            }
                        } finally {
                            if (entry_os != null) {
                                entry_os.close();
                                if (name.endsWith(".jnilib") || name.endsWith("JavaApplicationStub")) {
                                    try {
                                        String[] to_run = { chmod_command, "a+x", entry_file.getAbsolutePath() };
                                        runCommand(to_run, true);
                                    } catch (Throwable e) {
                                        unzip_error = e;
                                    }
                                }
                            }
                        }
                    }
                } finally {
                    zis.close();
                }
                if (unzip_error != null) {
                    throw (unzip_error);
                }
                File[] files = dir.listFiles();
                boolean launched = false;
                for (File f : files) {
                    if (f.getName().endsWith(".app")) {
                        String[] to_run;
                        if (args.length == 0 || !Constants.isOSX_10_6_OrHigher) {
                            to_run = new String[] { "/bin/sh", "-c", "open \"" + f.getAbsolutePath() + "\"" };
                        } else {
                            to_run = new String[3 + args.length];
                            to_run[0] = findCommand("open");
                            to_run[1] = f.getAbsolutePath();
                            to_run[2] = "--args";
                            System.arraycopy(args, 0, to_run, 3, args.length);
                        }
                        runCommand(to_run, false);
                        launched = true;
                    }
                }
                if (!launched) {
                    throw (new Exception("No .app files found in '" + dir + "'"));
                }
            }
        } catch (Throwable e) {
            Logger.log(new LogEvent(LogIDs.LOGGER, "Failed to launch update '" + file + "'", e));
        }
    }

    private static String findCommand(String name) {
        final String[] locations = { "/bin", "/usr/bin" };
        for (String s : locations) {
            File f = new File(s, name);
            if (f.exists() && f.canRead()) {
                return (f.getAbsolutePath());
            }
        }
        return (name);
    }

    private static void runCommand(String[] command, boolean wait) throws Throwable {
        try {
            String str = "";
            for (String s : command) {
                str += (str.length() == 0 ? "" : " ") + s;
            }
            System.out.println("running " + str);
            Process proc = Runtime.getRuntime().exec(command);
            if (wait) {
                proc.waitFor();
            }
        } catch (Throwable e) {
            System.err.println(e);
            throw (e);
        }
    }

    protected static boolean shouldUpdate(String current_version, String latest_version) {
        String current_base = Constants.getBaseVersion(current_version);
        int current_inc = Constants.getIncrementalBuild(current_version);
        String latest_base = Constants.getBaseVersion(latest_version);
        int latest_inc = Constants.getIncrementalBuild(latest_version);
        int major_comp = Constants.compareVersions(current_base, latest_base);
        if (major_comp < 0 && latest_inc >= 0) {
            return (true);
        }
        return (major_comp == 0 && current_inc > 0 && latest_inc > 0 && latest_inc > current_inc);
    }

    public static void main(String[] args) {
        String[][] tests = { { "2.4.0.0", "2.4.0.2", "true" }, { "2.4.0.1_CVS", "2.4.0.2", "true" }, { "2.4.0.1_B12", "2.4.0.2", "true" }, { "2.4.0.1_B12", "2.4.0.1_B34", "true" }, { "2.4.0.1_B12", "2.4.0.1_B6", "false" }, { "2.4.0.0", "2.4.0.1_CVS", "false" }, { "2.4.0.0", "2.4.0.1_B12", "true" }, { "2.4.0.0", "2.4.0.0", "false" }, { "2.4.0.1_CVS", "2.4.0.1_CVS", "false" }, { "2.4.0.1_B2", "2.4.0.1_B2", "false" }, { "2.4.0.1_CVS", "2.4.0.1_B2", "false" }, { "2.4.0.1_B2", "2.4.0.1_CVS", "false" } };
        for (int i = 0; i < tests.length; i++) {
            System.out.println(shouldUpdate(tests[i][0], tests[i][1]) + " / " + tests[i][2]);
        }
    }
}
