package net.java.sip.communicator.plugin.updatechecker;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.security.cert.*;
import java.util.*;
import javax.net.ssl.*;
import javax.swing.*;
import net.java.sip.communicator.service.browserlauncher.*;
import net.java.sip.communicator.service.configuration.*;
import net.java.sip.communicator.service.contactlist.*;
import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.resources.*;
import net.java.sip.communicator.service.version.*;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.swing.*;
import org.osgi.framework.*;

/**
 * Activates the UpdateCheck plugin
 * 
 * @author Damian Minkov
 */
public class UpdateCheckActivator implements BundleActivator {

    private static final Logger logger = Logger.getLogger(UpdateCheckActivator.class);

    private static BundleContext bundleContext = null;

    private static BrowserLauncherService browserLauncherService;

    private static ResourceManagementService resourcesService;

    private static ConfigurationService configService;

    private static UIService uiService = null;

    private String downloadLink = null;

    private String lastVersion = null;

    private String changesLink = null;

    private static UserCredentials userCredentials = null;

    private static final String UPDATE_USERNAME_CONFIG = "net.java.sip.communicator.plugin.updatechecker.UPDATE_SITE_USERNAME";

    private static final String UPDATE_PASSWORD_CONFIG = "net.java.sip.communicator.plugin.updatechecker.UPDATE_SITE_PASSWORD";

    static {
        removeDownloadRestrictions();
    }

    /**
     * Starts this bundle
     *
     * @param bundleContext BundleContext
     * @throws Exception
     */
    public void start(BundleContext bundleContext) throws Exception {
        try {
            logger.logEntry();
            UpdateCheckActivator.bundleContext = bundleContext;
        } finally {
            logger.logExit();
        }
        Thread updateThread = new Thread(new UpdateCheckThread());
        updateThread.setDaemon(true);
        updateThread.start();
    }

    /**
    * stop the bundle
    */
    public void stop(BundleContext bundleContext) throws Exception {
    }

    /**
     * Returns the <tt>BrowserLauncherService</tt> obtained from the bundle
     * context.
     * @return the <tt>BrowserLauncherService</tt> obtained from the bundle
     * context
     */
    public static BrowserLauncherService getBrowserLauncher() {
        if (browserLauncherService == null) {
            ServiceReference serviceReference = bundleContext.getServiceReference(BrowserLauncherService.class.getName());
            browserLauncherService = (BrowserLauncherService) bundleContext.getService(serviceReference);
        }
        return browserLauncherService;
    }

    /**
     * Returns the <tt>ConfigurationService</tt> obtained from the bundle
     * context.
     *
     * @return the <tt>ConfigurationService</tt> obtained from the bundle
     *         context
     */
    public static ConfigurationService getConfigurationService() {
        if (configService == null) {
            ServiceReference configReference = bundleContext.getServiceReference(ConfigurationService.class.getName());
            configService = (ConfigurationService) bundleContext.getService(configReference);
        }
        return configService;
    }

    /**
     * Returns a reference to the UIService implementation currently registered
     * in the bundle context or null if no such implementation was found.
     *
     * @return a reference to a UIService implementation currently registered
     * in the bundle context or null if no such implementation was found.
     */
    public static UIService getUIService() {
        if (uiService == null) {
            ServiceReference uiServiceReference = bundleContext.getServiceReference(UIService.class.getName());
            uiService = (UIService) bundleContext.getService(uiServiceReference);
        }
        return uiService;
    }

    public static ResourceManagementService getResources() {
        if (resourcesService == null) {
            ServiceReference serviceReference = bundleContext.getServiceReference(ResourceManagementService.class.getName());
            if (serviceReference == null) return null;
            resourcesService = (ResourceManagementService) bundleContext.getService(serviceReference);
        }
        return resourcesService;
    }

    /**
     * Check the first link as files on the web are sorted by date
     * @param currentVersionStr
     * @return
     */
    private boolean isNewestVersion() {
        try {
            ServiceReference serviceReference = bundleContext.getServiceReference(net.java.sip.communicator.service.version.VersionService.class.getName());
            VersionService verService = (VersionService) bundleContext.getService(serviceReference);
            net.java.sip.communicator.service.version.Version ver = verService.getCurrentVersion();
            String configString = Resources.getConfigString("update_link");
            if (configString == null) {
                logger.debug("Updates are disabled. Faking latest version.");
                return true;
            }
            URL url = new URL(configString);
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            Properties props = new Properties();
            props.load(conn.getInputStream());
            lastVersion = props.getProperty("last_version");
            downloadLink = props.getProperty("download_link");
            changesLink = configString.substring(0, configString.lastIndexOf("/") + 1) + props.getProperty("changes_html");
            return lastVersion.compareTo(ver.toString()) <= 0;
        } catch (Exception e) {
            logger.warn("Cannot get and compare versions!");
            logger.debug("Error was: ", e);
            return true;
        }
    }

    /**
     * Shows dialog informing about new version with button Install
     * which trigers the update process.
     */
    private void windowsUpdaterShow() {
        final JDialog dialog = new SIPCommDialog() {

            protected void close(boolean isEscaped) {
            }
        };
        dialog.setTitle(getResources().getI18NString("plugin.updatechecker.DIALOG_TITLE"));
        JEditorPane contentMessage = new JEditorPane();
        contentMessage.setContentType("text/html");
        contentMessage.setOpaque(false);
        contentMessage.setEditable(false);
        String dialogMsg = getResources().getI18NString("plugin.updatechecker.DIALOG_MESSAGE", new String[] { getResources().getSettingsString("service.gui.APPLICATION_NAME") });
        if (lastVersion != null) dialogMsg += getResources().getI18NString("plugin.updatechecker.DIALOG_MESSAGE_2", new String[] { getResources().getSettingsString("service.gui.APPLICATION_NAME"), lastVersion });
        contentMessage.setText(dialogMsg);
        JPanel contentPane = new SIPCommFrame.MainContentPane();
        contentMessage.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        contentPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        contentPane.add(contentMessage, BorderLayout.NORTH);
        JScrollPane scrollChanges = new JScrollPane();
        scrollChanges.setPreferredSize(new Dimension(400, 200));
        JEditorPane changesHtml = new JEditorPane();
        changesHtml.setContentType("text/html");
        changesHtml.setEditable(false);
        changesHtml.setBorder(BorderFactory.createLoweredBevelBorder());
        scrollChanges.setViewportView(changesHtml);
        contentPane.add(scrollChanges, BorderLayout.CENTER);
        try {
            changesHtml.setPage(new URL(changesLink));
        } catch (Exception e) {
            logger.error("Cannot set changes Page", e);
        }
        JPanel buttonPanel = new TransparentPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton closeButton = new JButton(getResources().getI18NString("plugin.updatechecker.BUTTON_CLOSE"));
        closeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
            }
        });
        if (downloadLink != null) {
            JButton installButton = new JButton(getResources().getI18NString("plugin.updatechecker.BUTTON_INSTALL"));
            installButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    dialog.dispose();
                    windowsUpdate();
                }
            });
            buttonPanel.add(installButton);
        }
        buttonPanel.add(closeButton);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setContentPane(contentPane);
        dialog.pack();
        dialog.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - dialog.getWidth() / 2, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - dialog.getHeight() / 2);
        dialog.setVisible(true);
    }

    /**
     * The update process itself.
     * - Downloads the installer in a temp directory.
     * - Warns that update will shutdown.
     * - Triggers update (installer) in separate process with the help
     * of update.exe and shutdowns.
     */
    private void windowsUpdate() {
        File tempF = null;
        try {
            final File temp = File.createTempFile("sc-install", ".exe");
            tempF = temp;
            URL u = new URL(downloadLink);
            URLConnection uc = u.openConnection();
            if (uc instanceof HttpURLConnection) {
                int responseCode = ((HttpURLConnection) uc).getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    new Thread(new Runnable() {

                        public void run() {
                            ExportedWindow authWindow = getUIService().getExportedWindow(ExportedWindow.AUTHENTICATION_WINDOW);
                            UserCredentials cred = new UserCredentials();
                            authWindow.setParams(new Object[] { cred });
                            authWindow.setVisible(true);
                            userCredentials = cred;
                            if (cred.getUserName() == null) {
                                userCredentials = null;
                            } else windowsUpdate();
                        }
                    }).start();
                } else if (responseCode == HttpURLConnection.HTTP_OK && userCredentials != null && userCredentials.getUserName() != null && userCredentials.isPasswordPersistent()) {
                    getConfigurationService().setProperty(UPDATE_USERNAME_CONFIG, userCredentials.getUserName());
                    getConfigurationService().setProperty(UPDATE_PASSWORD_CONFIG, new String(Base64.encode(userCredentials.getPasswordAsString().getBytes())));
                }
            }
            InputStream in = uc.getInputStream();
            final ProgressMonitorInputStream pin = new ProgressMonitorInputStream(null, u.toString(), in);
            ProgressMonitor pm = pin.getProgressMonitor();
            pm.setMaximum(uc.getContentLength());
            final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(temp));
            new Thread(new Runnable() {

                public void run() {
                    try {
                        int read = -1;
                        byte[] buff = new byte[1024];
                        while ((read = pin.read(buff)) != -1) {
                            out.write(buff, 0, read);
                        }
                        pin.close();
                        out.flush();
                        out.close();
                        if (getUIService().getPopupDialog().showConfirmPopupDialog(getResources().getI18NString("plugin.updatechecker.DIALOG_WARN"), getResources().getI18NString("plugin.updatechecker.DIALOG_TITLE"), PopupDialog.YES_NO_OPTION, PopupDialog.QUESTION_MESSAGE) != PopupDialog.YES_OPTION) {
                            return;
                        }
                        String workingDir = System.getProperty("user.dir");
                        ProcessBuilder processBuilder = new ProcessBuilder(new String[] { workingDir + File.separator + "up2date.exe", "--wait-parent", "--allow-elevation", temp.getCanonicalPath(), workingDir });
                        processBuilder.start();
                        getUIService().beginShutdown();
                    } catch (Exception e) {
                        logger.error("Error saving", e);
                        try {
                            pin.close();
                            out.close();
                        } catch (Exception e1) {
                        }
                    }
                }
            }).start();
        } catch (FileNotFoundException e) {
            getUIService().getPopupDialog().showMessagePopupDialog(getResources().getI18NString("plugin.updatechecker.DIALOG_MISSING_UPDATE"), getResources().getI18NString("plugin.updatechecker.DIALOG_NOUPDATE_TITLE"), PopupDialog.INFORMATION_MESSAGE);
            tempF.delete();
        } catch (Exception e) {
            logger.info("Error starting update process!", e);
            tempF.delete();
        }
    }

    /**
     * Invokes action for checking for updates.
     */
    private void checkForUpdate() {
        if (isNewestVersion()) {
            getUIService().getPopupDialog().showMessagePopupDialog(getResources().getI18NString("plugin.updatechecker.DIALOG_NOUPDATE"), getResources().getI18NString("plugin.updatechecker.DIALOG_NOUPDATE_TITLE"), PopupDialog.INFORMATION_MESSAGE);
        } else windowsUpdaterShow();
    }

    /**
     * Installs Dummy TrustManager will not try to validate self-signed certs.
     * Fix some problems with not proper use of certs.
     */
    private static void removeDownloadRestrictions() {
        try {
            SSLContext sc = SSLContext.getInstance("SSLv3");
            TrustManager[] tma = { new DummyTrustManager() };
            sc.init(null, tma, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            logger.warn("Failed to init dummy trust magaer", e);
        }
        HostnameVerifier hv = new HostnameVerifier() {

            public boolean verify(String urlHostName, SSLSession session) {
                logger.warn("Warning: URL Host: " + urlHostName + " vs. " + session.getPeerHost());
                return true;
            }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
        Authenticator.setDefault(new Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {
                String uName = (String) getConfigurationService().getProperty(UPDATE_USERNAME_CONFIG);
                if (uName != null) {
                    String pass = (String) getConfigurationService().getProperty(UPDATE_PASSWORD_CONFIG);
                    if (pass != null) return new PasswordAuthentication(uName, new String(Base64.decode(pass)).toCharArray());
                }
                if (userCredentials != null) {
                    return new PasswordAuthentication(userCredentials.getUserName(), userCredentials.getPassword());
                } else {
                    return null;
                }
            }
        });
    }

    /**
     * Checks whether we are running on 64 bit Linux.
     * (Not really a correct check as if we are running a 32bit jvm
     * on 64bit linux it will report 32bit).
     * @return is Linux 64bit.
     */
    private static boolean isLinux64() {
        String osName = System.getProperty("os.name");
        String arch = System.getProperty("sun.arch.data.model");
        return (osName != null) && (arch != null) && (osName.indexOf("Linux") != -1) && (arch.indexOf("64") != -1);
    }

    /**
     * The menu entry under tools menu.
     */
    private class UpdateMenuButtonComponent implements PluginComponent {

        private final Container container;

        private final JMenuItem updateMenuItem = new JMenuItem(getResources().getI18NString("plugin.updatechecker.UPDATE_MENU_ENTRY"));

        UpdateMenuButtonComponent(Container c) {
            this.container = c;
            updateMenuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    checkForUpdate();
                }
            });
        }

        public String getName() {
            return getResources().getI18NString("plugin.updatechecker.UPDATE_MENU_ENTRY");
        }

        public Container getContainer() {
            return this.container;
        }

        public String getConstraints() {
            return null;
        }

        public int getPositionIndex() {
            return -1;
        }

        public Object getComponent() {
            return updateMenuItem;
        }

        public void setCurrentContact(MetaContact metaContact) {
        }

        public void setCurrentContactGroup(MetaContactGroup metaGroup) {
        }

        public boolean isNativeComponent() {
            return false;
        }
    }

    /**
     * Dummy trust manager, trusts everything.
     */
    private static class DummyTrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    private class UpdateCheckThread implements Runnable {

        public void run() {
            String osName = System.getProperty("os.name");
            if (osName.startsWith("Windows")) {
                Hashtable<String, String> toolsMenuFilter = new Hashtable<String, String>();
                toolsMenuFilter.put(Container.CONTAINER_ID, Container.CONTAINER_HELP_MENU.getID());
                bundleContext.registerService(PluginComponent.class.getName(), new UpdateMenuButtonComponent(Container.CONTAINER_HELP_MENU), toolsMenuFilter);
            }
            if (isNewestVersion()) return;
            if (osName.startsWith("Windows")) {
                windowsUpdaterShow();
                return;
            }
            final JDialog dialog = new SIPCommDialog() {

                protected void close(boolean isEscaped) {
                }
            };
            dialog.setTitle(getResources().getI18NString("plugin.updatechecker.DIALOG_TITLE"));
            JEditorPane contentMessage = new JEditorPane();
            contentMessage.setContentType("text/html");
            contentMessage.setOpaque(false);
            contentMessage.setEditable(false);
            String dialogMsg = getResources().getI18NString("plugin.updatechecker.DIALOG_MESSAGE", new String[] { getResources().getSettingsString("service.gui.APPLICATION_NAME") });
            if (lastVersion != null) dialogMsg += getResources().getI18NString("plugin.updatechecker.DIALOG_MESSAGE_2", new String[] { getResources().getSettingsString("service.gui.APPLICATION_NAME"), lastVersion });
            contentMessage.setText(dialogMsg);
            JPanel contentPane = new TransparentPanel(new BorderLayout(5, 5));
            contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            contentPane.add(contentMessage, BorderLayout.CENTER);
            JPanel buttonPanel = new TransparentPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            JButton closeButton = new JButton(getResources().getI18NString("plugin.updatechecker.BUTTON_CLOSE"));
            closeButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    dialog.setVisible(false);
                }
            });
            if (downloadLink != null) {
                JButton downloadButton = new JButton(getResources().getI18NString("plugin.updatechecker.BUTTON_DOWNLOAD"));
                downloadButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        if (isLinux64()) {
                            downloadLink = downloadLink.replace("i386", "amd64");
                        }
                        getBrowserLauncher().openURL(downloadLink);
                        dialog.dispose();
                    }
                });
                buttonPanel.add(downloadButton);
            }
            buttonPanel.add(closeButton);
            contentPane.add(buttonPanel, BorderLayout.SOUTH);
            dialog.setContentPane(contentPane);
            dialog.pack();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            dialog.setLocation(screenSize.width / 2 - dialog.getWidth() / 2, screenSize.height / 2 - dialog.getHeight() / 2);
            dialog.setVisible(true);
        }
    }
}
