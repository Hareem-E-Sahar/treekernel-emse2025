package org.gudy.azureus2.pluginsimpl.local.update;

import java.io.*;
import org.gudy.azureus2.platform.PlatformManager;
import org.gudy.azureus2.platform.PlatformManagerCapabilities;
import org.gudy.azureus2.platform.PlatformManagerFactory;
import org.gudy.azureus2.plugins.update.*;
import org.gudy.azureus2.core3.util.*;
import org.gudy.azureus2.core3.internat.MessageText;
import org.gudy.azureus2.core3.logging.*;
import com.aelitis.azureus.core.update.AzureusRestarter;
import com.aelitis.azureus.core.update.AzureusRestarterFactory;

public class UpdateInstallerImpl implements UpdateInstaller {

    protected static final String UPDATE_DIR = "updates";

    protected static final String ACTIONS_LEGACY = "install.act";

    protected static final String ACTIONS_UTF8 = "install.act.utf8";

    protected static AEMonitor class_mon = new AEMonitor("UpdateInstaller:class");

    private UpdateManagerImpl manager;

    private File install_dir;

    protected static void checkForFailedInstalls(UpdateManagerImpl manager) {
        try {
            File update_dir = new File(manager.getUserDir() + File.separator + UPDATE_DIR);
            File[] dirs = update_dir.listFiles();
            if (dirs != null) {
                boolean found_failure = false;
                String files = "";
                for (int i = 0; i < dirs.length; i++) {
                    File dir = dirs[i];
                    if (dir.isDirectory()) {
                        found_failure = true;
                        File[] x = dir.listFiles();
                        if (x != null) {
                            for (int j = 0; j < x.length; j++) {
                                files += (files.length() == 0 ? "" : ",") + x[j].getName();
                            }
                        }
                        FileUtil.recursiveDelete(dir);
                    }
                }
                if (found_failure) {
                    Logger.log(new LogAlert(LogAlert.UNREPEATABLE, LogAlert.AT_ERROR, MessageText.getString("Alert.failed.update", new String[] { files })));
                }
            }
        } catch (Throwable e) {
            Debug.printStackTrace(e);
        }
    }

    protected UpdateInstallerImpl(UpdateManagerImpl _manager) throws UpdateException {
        manager = _manager;
        try {
            class_mon.enter();
            String update_dir = getUserDir() + File.separator + UPDATE_DIR;
            for (int i = 1; i < 1024; i++) {
                File try_dir = new File(update_dir + File.separator + "inst_" + i);
                if (!try_dir.exists()) {
                    if (!FileUtil.mkdirs(try_dir)) {
                        throw (new UpdateException("Failed to create a temporary installation dir"));
                    }
                    install_dir = try_dir;
                    break;
                }
            }
            if (install_dir == null) {
                throw (new UpdateException("Failed to find a temporary installation dir"));
            }
        } finally {
            class_mon.exit();
        }
    }

    public void addResource(String resource_name, InputStream is) throws UpdateException {
        addResource(resource_name, is, true);
    }

    public void addResource(String resource_name, InputStream is, boolean closeInputStream) throws UpdateException {
        try {
            File target_file = new File(install_dir, resource_name);
            FileUtil.copyFile(is, new FileOutputStream(target_file), closeInputStream);
        } catch (Throwable e) {
            throw (new UpdateException("UpdateInstaller: resource addition fails", e));
        }
    }

    public String getInstallDir() {
        return (manager.getInstallDir());
    }

    public String getUserDir() {
        return (manager.getUserDir());
    }

    public void addMoveAction(String from_file_or_resource, String to_file) throws UpdateException {
        if (from_file_or_resource.indexOf(File.separator) == -1) {
            from_file_or_resource = install_dir.toString() + File.separator + from_file_or_resource;
        }
        try {
            File to_f = new File(to_file);
            File parent = to_f.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            boolean log_perm_set_fail = true;
            if (parent != null) {
                if (!parent.canWrite()) {
                    log_perm_set_fail = false;
                    if (!Constants.isWindowsVistaOrHigher) {
                        Logger.log(new LogAlert(LogAlert.UNREPEATABLE, LogAlert.AT_WARNING, "The location '" + parent.toString() + "' isn't writable, this update will probably fail." + " Check permissions and retry the update"));
                    }
                }
            }
            try {
                PlatformManager pm = PlatformManagerFactory.getPlatformManager();
                if (pm.hasCapability(PlatformManagerCapabilities.CopyFilePermissions)) {
                    String parent_str = parent.getAbsolutePath();
                    PlatformManagerFactory.getPlatformManager().copyFilePermissions(parent_str, from_file_or_resource);
                }
            } catch (Throwable e) {
                if (log_perm_set_fail) {
                    if (!Constants.isWindowsVistaOrHigher) {
                        Debug.out(e);
                    }
                }
            }
        } catch (Throwable e) {
        }
        appendAction("move," + from_file_or_resource + "," + to_file);
    }

    public void addChangeRightsAction(String rights, String to_file) throws UpdateException {
        appendAction("chmod," + rights + "," + to_file);
    }

    public void addRemoveAction(String file) throws UpdateException {
        appendAction("remove," + file);
    }

    protected void appendAction(String data) throws UpdateException {
        PrintWriter pw_legacy = null;
        try {
            pw_legacy = new PrintWriter(new FileWriter(install_dir.toString() + File.separator + ACTIONS_LEGACY, true));
            pw_legacy.println(data);
        } catch (Throwable e) {
            throw (new UpdateException("Failed to write actions file", e));
        } finally {
            if (pw_legacy != null) {
                try {
                    pw_legacy.close();
                } catch (Throwable e) {
                    throw (new UpdateException("Failed to write actions file", e));
                }
            }
        }
        PrintWriter pw_utf8 = null;
        try {
            pw_utf8 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(install_dir.toString() + File.separator + ACTIONS_UTF8, true), "UTF-8"));
            pw_utf8.println(data);
        } catch (Throwable e) {
            throw (new UpdateException("Failed to write actions file", e));
        } finally {
            if (pw_utf8 != null) {
                try {
                    pw_utf8.close();
                } catch (Throwable e) {
                    throw (new UpdateException("Failed to write actions file", e));
                }
            }
        }
    }

    public void installNow(final UpdateInstallerListener listener) throws UpdateException {
        try {
            UpdateInstaller[] installers = manager.getInstallers();
            if (installers.length != 1 || installers[0] != this) {
                throw (new UpdateException("Other installers exist - aborting"));
            }
            listener.reportProgress("Update starts");
            AzureusRestarter ar = AzureusRestarterFactory.create(manager.getCore());
            ar.updateNow();
            new AEThread2("installNow:waiter", true) {

                public void run() {
                    try {
                        long start = SystemTime.getMonotonousTime();
                        UpdateException pending_error = null;
                        while (true) {
                            Thread.sleep(1000);
                            listener.reportProgress("Checking progress");
                            if (!install_dir.exists()) {
                                break;
                            }
                            File fail_file = new File(install_dir, "install.fail");
                            if (fail_file.exists()) {
                                try {
                                    String error = FileUtil.readFileAsString(fail_file, 1024);
                                    throw (new UpdateException(error));
                                } catch (Throwable e) {
                                    if (e instanceof UpdateException) {
                                        throw (e);
                                    }
                                    if (pending_error != null) {
                                        throw (pending_error);
                                    }
                                    pending_error = new UpdateException("Install failed, reason unknown");
                                }
                            }
                            if (SystemTime.getMonotonousTime() - start >= 5 * 60 * 1000) {
                                listener.reportProgress("Timeout");
                                throw (new UpdateException("Timeout waiting for update to apply"));
                            }
                        }
                        listener.reportProgress("Complete");
                        listener.complete();
                    } catch (Throwable e) {
                        UpdateException fail;
                        if (e instanceof UpdateException) {
                            fail = (UpdateException) e;
                        } else {
                            fail = new UpdateException("install failed", e);
                        }
                        listener.reportProgress(fail.getMessage());
                        listener.failed(fail);
                    } finally {
                        deleteInstaller();
                    }
                }
            }.start();
        } catch (Throwable e) {
            deleteInstaller();
            UpdateException fail;
            if (e instanceof UpdateException) {
                fail = (UpdateException) e;
            } else {
                fail = new UpdateException("install failed", e);
            }
            listener.reportProgress(fail.getMessage());
            listener.failed(fail);
            throw (fail);
        }
    }

    public void destroy() {
        deleteInstaller();
    }

    private void deleteInstaller() {
        manager.removeInstaller(this);
        if (install_dir.exists()) {
            FileUtil.recursiveDelete(install_dir);
        }
    }
}
