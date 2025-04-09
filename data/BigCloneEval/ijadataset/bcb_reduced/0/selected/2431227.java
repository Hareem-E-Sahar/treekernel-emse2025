package org.wfp.rita.util;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import org.mortbay.component.LifeCycle;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.FilterMapping;
import org.mortbay.jetty.webapp.WebAppClassLoader;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.ui.savedrequest.Enumerator;
import org.wfp.rita.base.RitaException;
import org.wfp.rita.exception.RitaInternalError;
import org.wfp.rita.exception.ServerStartupFailed;
import org.wfp.rita.web.common.MBeanUtils;
import org.wfp.rita.web.controller.HibernateSessionFilter;

public class JettyLoader {

    private static Logger log = LoggerFactory.getLogger(JettyLoader.class);

    private static SystemTray m_Tray;

    private static TrayIcon m_Icon;

    private static URL STARTING_IMG, RUNNING_IMG, STOPPING_IMG, APP_URL;

    /**
     * @see <a href="http://www.jguru.com/faq/view.jsp?EID=15835">JGuru</a>
     * @return the current environment's IP address, taking into account the
     * Internet connection to any of the available machine's Network 
     * interfaces. Examples of the outputs can be in octatos or in IPV6 format.
     * 
     * <table>
     * 
     * <tr>
     * <th>Address</th>
     * <th>siteLocal</th>
     * <th>isLoopback</th>
     * <th>isIPV6</th>
     * <th>Notes</th>
     * </tr>
     * <tr>
     * <td>fec0:0:0:9:213:e8ff:fef1:b717%4</td>
     * <td>true</td>
     * <td>false</td>
     * <td>isIPV6</td>
     * </td></td>
     * </tr>
     * <tr>
     * <td>130.212.150.216</td>
     * <td>false</td>
     * <td>false</td>
     * <td>false</td>
     * <td>This is the one we want to grab so that we can address the DSP 
     * on the network.</td>
     * </tr>
     * <tr>
     * <td>0:0:0:0:0:0:0:1%1</td>
     * <td>false</td>
     * <td>true</td>
     * <td>true</td>
     * <td></td>
     * </tr>
     * <tr>
     * <td>127.0.0.1</td>
     * <td>false</td>
     * <td>true</td>
     * <td>false</td>
     * <td></td>
     * </tr>
     * </table>
     */
    public static String getCurrentEnvironmentNetworkIp() {
        Enumeration<NetworkInterface> netInterfaces = null;
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            log.error("Failed to get interface IP address", e);
            netInterfaces = new Enumerator(Collections.emptyList());
        }
        while (netInterfaces.hasMoreElements()) {
            NetworkInterface ni = netInterfaces.nextElement();
            Enumeration<InetAddress> address = ni.getInetAddresses();
            while (address.hasMoreElements()) {
                InetAddress addr = address.nextElement();
                if (!addr.isLoopbackAddress() && !addr.isSiteLocalAddress() && addr.getHostAddress().indexOf(":") == -1) {
                    return addr.getHostAddress();
                }
            }
        }
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }

    /**
	 * Assuming that this is an ordinary executable, search up the
	 * filesystem from the location of the resource root (the directory
	 * containing JettyLoader.class), until we find a directory that 
	 * contains a subdirectory called <code>web/</code>, and return the
	 * <code>file:/</code> URL of that subdirectory. 
	 */
    public static URL getWebDirUrlBySearch() throws RitaException {
        String searchStartDir = JettyLoader.class.getResource("/").getPath();
        File searchDirFile = new File(searchStartDir);
        assert searchDirFile.exists();
        assert searchDirFile.isDirectory();
        while (searchDirFile != null) {
            File webDirFile = new File(searchDirFile, "web");
            if (webDirFile.isDirectory()) {
                try {
                    return new URL("file:" + webDirFile + "/");
                } catch (MalformedURLException e) {
                    throw new RitaInternalError(e);
                }
            }
            searchDirFile = searchDirFile.getParentFile();
        }
        throw new RitaInternalError("Cannot find web directory " + "in any parent of " + searchStartDir);
    }

    private String m_EnvironmentName;

    /**
     * @param environmentName The name of the database-X.properties file
     * to be used to configure the application and Hibernate, or 
     * <code>null</code> to use the default file, 
     * <code>database.properties</code>
     */
    public JettyLoader(String environmentName) {
        m_EnvironmentName = environmentName;
    }

    public static void main(String[] args) throws Exception {
        JettyLoader loader = new JettyLoader(null);
        try {
            loader.start();
        } catch (ServerStartupFailed e) {
            loader.stop();
            throw e;
        }
        loader.join();
    }

    public URL getApplicationUrl() {
        return APP_URL;
    }

    public void start() throws RitaException {
        Connector connector = new SelectChannelConnector();
        connector.setPort(8080);
        m_Server = new Server();
        m_Server.addConnector(connector);
        WebAppContext wac = new WebAppContext();
        wac.setContextPath("/");
        if (m_EnvironmentName != null) {
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("environment", m_EnvironmentName);
            wac.setInitParams(parameters);
        }
        URL webDirUrl = JettyLoader.class.getResource("/web/");
        if (webDirUrl != null) {
            log.info("This is an executable JAR, loading web resources " + "from the classpath: " + webDirUrl);
            wac.setBaseResource(new ClassPathResource("/web/"));
        } else {
            webDirUrl = getWebDirUrlBySearch();
            log.info("This is a deployed application, loading web resources " + "from directory: " + webDirUrl);
            wac.setResourceBase(webDirUrl.toString());
        }
        Pattern pat = Pattern.compile("^jar:(.*)!(.*)");
        Matcher mat = pat.matcher(webDirUrl.toString());
        if (mat.matches()) {
            try {
                final URL jar = new URL(mat.group(1));
                URLClassLoader cl = new URLClassLoader(new URL[] { jar }, new WebAppClassLoader(wac));
                wac.setClassLoader(cl);
            } catch (Exception e) {
                throw new ServerStartupFailed(e);
            }
        }
        wac.setParentLoaderPriority(true);
        wac.getServletHandler().setStartWithUnavailable(false);
        final List<Throwable> lifecycleExceptions = new ArrayList<Throwable>();
        wac.addLifeCycleListener(new LifeCycle.Listener() {

            @Override
            public void lifeCycleFailure(LifeCycle event, Throwable cause) {
                lifecycleExceptions.add(cause);
            }

            public void lifeCycleStarted(LifeCycle event) {
            }

            public void lifeCycleStarting(LifeCycle event) {
            }

            public void lifeCycleStopped(LifeCycle event) {
            }

            public void lifeCycleStopping(LifeCycle event) {
            }
        });
        m_Server.setHandler(wac);
        m_Server.setStopAtShutdown(true);
        URL loginUrl, setupUrl;
        try {
            STARTING_IMG = new URL(webDirUrl, "img/aircraft.png");
            RUNNING_IMG = new URL(webDirUrl, "img/wfp_small.png");
            STOPPING_IMG = new URL(webDirUrl, "img/spinner.gif");
            APP_URL = new URL("http", getCurrentEnvironmentNetworkIp(), connector.getPort(), "/");
            loginUrl = new URL(APP_URL, "public/login.xhtml");
            setupUrl = new URL(APP_URL, "setup/");
        } catch (MalformedURLException e) {
            throw new ServerStartupFailed(e);
        }
        if (SystemTray.isSupported()) {
            m_Tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().getImage(STARTING_IMG);
            PopupMenu popup = new PopupMenu();
            if (Desktop.isDesktopSupported()) {
                UrlListener loginEvent = new UrlListener(loginUrl);
                MenuItem loginMenuItem = new MenuItem("Open RITA");
                loginMenuItem.addActionListener(loginEvent);
                popup.add(loginMenuItem);
                UrlListener setupEvent = new UrlListener(setupUrl);
                MenuItem setupMenuItem = new MenuItem("Configure RITA");
                setupMenuItem.addActionListener(setupEvent);
                popup.add(setupMenuItem);
            }
            {
                ShutdownListener shutdownEvent = new ShutdownListener();
                MenuItem shutdownMenuItem = new MenuItem("Shut down RITA Server");
                shutdownMenuItem.addActionListener(shutdownEvent);
                popup.add(shutdownMenuItem);
            }
            m_Icon = new TrayIcon(image, "RITA Server is Starting: " + APP_URL, popup);
            m_Icon.setImageAutoSize(true);
            try {
                m_Tray.add(m_Icon);
            } catch (AWTException e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        }
        try {
            m_Server.start();
        } catch (Exception e) {
            throw new ServerStartupFailed(e);
        }
        try {
            if (wac.getUnavailableException() != null) {
                log.error("Server shutting down because web application " + "failed to start: " + wac.getUnavailableException().toString());
                throw new ServerStartupFailed(wac.getUnavailableException());
            } else if (wac.isFailed()) {
                if (lifecycleExceptions.size() > 0) {
                    log.error("Server shutting down because web application " + "failed to start: Handler failed: " + lifecycleExceptions.get(0).toString());
                    throw new ServerStartupFailed(lifecycleExceptions.get(0));
                } else {
                    log.error("Server shutting down because web application " + "failed to start without recording an exception " + "(probably a listener failed to start)");
                    throw new ServerStartupFailed("No exception recorded " + "(probably a listener failed to start)");
                }
            }
        } catch (ServerStartupFailed e) {
            try {
                stop();
            } catch (Exception e2) {
                log.error("Failed to shut down the server after startup failed", e2);
            }
            throw e;
        }
        log.info("Server running and waiting for requests.");
        if (m_Icon != null) {
            Image image = Toolkit.getDefaultToolkit().getImage(RUNNING_IMG);
            m_Icon.setToolTip("RITA Server is Running: " + APP_URL);
            m_Icon.setImage(image);
        }
    }

    public void join() throws RitaException {
        try {
            m_Server.join();
        } catch (InterruptedException e) {
            throw new RitaInternalError(e);
        } finally {
            if (m_Tray != null) {
                m_Tray.remove(m_Icon);
                m_Icon.getImage().flush();
            }
        }
    }

    private Server m_Server;

    public void stop() throws Exception {
        if (m_Icon != null) {
            Image image = Toolkit.getDefaultToolkit().getImage(STOPPING_IMG);
            m_Icon.setImage(image);
            m_Icon.setToolTip("RITA server is Stopping");
        }
        m_Server.setGracefulShutdown(1000);
        new Thread() {

            public void run() {
                try {
                    m_Server.stop();
                } catch (RuntimeException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                } finally {
                    if (m_Tray != null) {
                        m_Tray.remove(m_Icon);
                        m_Icon.getImage().flush();
                    }
                }
            }

            ;
        }.start();
    }

    private static class UrlListener implements ActionListener {

        private URL m_UrlToOpen;

        public UrlListener(URL urlToOpen) {
            m_UrlToOpen = urlToOpen;
        }

        public void actionPerformed(ActionEvent ev) {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(m_UrlToOpen.toURI());
                }
            } catch (Exception ex) {
                String msg = MBeanUtils.getMessage("lic_browser_error_message", ex.toString());
                log.error(msg, ex);
                JOptionPane.showMessageDialog(null, msg);
            }
        }
    }

    ;

    private class ShutdownListener implements ActionListener {

        public void actionPerformed(ActionEvent ev) {
            try {
                stop();
            } catch (Exception ex) {
                String msg = MBeanUtils.getMessage("lic_shutdown_error_message", ex.toString());
                log.error(msg, ex);
                JOptionPane.showMessageDialog(null, msg);
            }
        }
    }

    ;

    private static class ClassPathResource extends Resource {

        private String path;

        private Logger log = LoggerFactory.getLogger(ClassPathResource.class);

        public ClassPathResource(String path) {
            this.path = path;
        }

        @Override
        public Resource addPath(String subPath) throws IOException, MalformedURLException {
            StringBuffer newPath = new StringBuffer(this.path);
            if (!this.path.endsWith("/")) {
                newPath.append('/');
            }
            if (subPath.startsWith("/")) {
                subPath = subPath.substring(1);
            }
            newPath.append(subPath);
            return new ClassPathResource(newPath.toString());
        }

        @Override
        public boolean delete() throws SecurityException {
            return false;
        }

        @Override
        public boolean exists() {
            return getClass().getResource(path) != null;
        }

        /**
		 * @return null to force loading the resource as a stream
		 */
        @Override
        public File getFile() throws IOException {
            return null;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return getClass().getResourceAsStream(path);
        }

        @Override
        public String getName() {
            return path;
        }

        @Override
        public OutputStream getOutputStream() throws IOException, SecurityException {
            return null;
        }

        @Override
        public URL getURL() {
            return getClass().getResource(path);
        }

        @Override
        public boolean isDirectory() {
            return path.endsWith("/");
        }

        @Override
        public long lastModified() {
            return 0;
        }

        @Override
        public long length() {
            try {
                InputStream is = getInputStream();
                int len = is.available();
                is.close();
                return len;
            } catch (IOException e) {
                return -1;
            }
        }

        @Override
        public String[] list() {
            return null;
        }

        @Override
        public void release() {
        }

        @Override
        public boolean renameTo(Resource dest) throws SecurityException {
            return false;
        }
    }
}
