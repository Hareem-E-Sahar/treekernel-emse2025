package org.mobicents.servlet.sip.startup.jboss;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.SubDeployer;
import org.jboss.metadata.WebMetaData;
import org.jboss.metadata.XmlFileLoader;
import org.jboss.mx.loading.LoaderRepositoryFactory.LoaderRepositoryConfig;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.util.file.JarUtils;
import org.jboss.web.AbstractWebContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class extends the Abstract Web Container so that the .war hard coded value
 * is not mandatory and that .sar2 can be used instead
 * 
 * @author Jean Deruelle
 * 
 */
public abstract class AbstractConvergedContainer extends AbstractWebContainer {

    @Override
    public synchronized void init(DeploymentInfo di) throws DeploymentException {
        log.debug("Begin init");
        this.server = di.getServer();
        try {
            if (di.url.getPath().endsWith("/")) {
                di.watch = new URL(di.url, "WEB-INF/web.xml");
            } else {
                di.watch = di.url;
            }
            boolean unpackWebservice = di.localCl.findResource("WEB-INF/webservices.xml") != null;
            unpackWebservice |= server.isRegistered(ObjectNameFactory.create("jboss.ws:service=ServiceEndpointManager"));
            File warFile = new File(di.localUrl.getFile());
            if (warFile.isDirectory() == false && (unpackWars || unpackWebservice)) {
                String prefix = warFile.getCanonicalPath();
                int prefixIndex = prefix.lastIndexOf(".");
                String extension = "war";
                if (prefixIndex < 0) {
                    prefix = prefix.substring(0, prefixIndex);
                    extension = prefix.substring(prefixIndex + 1, prefix.length());
                }
                File expWarFile = new File(prefix + "-exp." + extension);
                if (expWarFile.mkdir() == false) throw new DeploymentException("Was unable to mkdir: " + expWarFile);
                log.debug("Unpacking " + extension + " to: " + expWarFile);
                FileInputStream fis = new FileInputStream(warFile);
                JarUtils.unjar(fis, expWarFile);
                fis.close();
                log.debug("Replaced " + extension + " with unpacked contents");
                if (warFile.delete() == false) log.debug("Was unable to delete " + extension + " file"); else log.debug("Deleted " + extension + " archive");
                di.localUrl = expWarFile.toURL();
                URL[] localCl = new URL[] { di.localUrl };
                di.localCl = new URLClassLoader(localCl);
            }
            WebMetaData metaData = new WebMetaData();
            metaData.setResourceClassLoader(di.localCl);
            metaData.setJava2ClassLoadingCompliance(this.java2ClassLoadingCompliance);
            di.metaData = metaData;
            String webContext = di.webContext;
            if (webContext != null) {
                if (webContext.length() > 0 && webContext.charAt(0) != '/') webContext = "/" + webContext;
            }
            URL warURL = di.localUrl != null ? di.localUrl : di.url;
            log.debug("webContext: " + webContext);
            log.debug("warURL: " + warURL);
            parseMetaData(webContext, warURL, di.shortName, metaData);
            LoaderRepositoryConfig config = metaData.getLoaderConfig();
            if (config != null) di.setRepositoryInfo(config);
            processNestedDeployments(di);
            emitNotification(SubDeployer.INIT_NOTIFICATION, di);
        } catch (DeploymentException e) {
            log.debug("Problem in init ", e);
            throw e;
        } catch (Exception e) {
            log.error("Problem in init ", e);
            throw new DeploymentException(e);
        }
        log.debug("End init");
    }

    /**
	 * This method creates a context-root string from either the
	 * WEB-INF/jboss-web.xml context-root element is one exists, or the filename
	 * portion of the warURL. It is called if the DeploymentInfo webContext
	 * value is null which indicates a standalone war deployment. A war name of
	 * ROOT.war is handled as a special case of a war that should be installed
	 * as the default web context.
	 */
    protected void parseMetaData(String ctxPath, URL warURL, String warName, WebMetaData metaData) throws DeploymentException {
        InputStream jbossWebIS = null;
        InputStream webIS = null;
        try {
            File warDir = new File(warURL.getFile());
            if (warURL.getProtocol().equals("file") && warDir.isDirectory() == true) {
                File webDD = new File(warDir, "WEB-INF/web.xml");
                if (webDD.exists() == true) webIS = new FileInputStream(webDD);
                File jbossWebDD = new File(warDir, "WEB-INF/jboss-web.xml");
                if (jbossWebDD.exists() == true) jbossWebIS = new FileInputStream(jbossWebDD);
            } else {
                InputStream warIS = warURL.openStream();
                java.util.zip.ZipInputStream zipIS = new java.util.zip.ZipInputStream(warIS);
                java.util.zip.ZipEntry entry;
                byte[] buffer = new byte[512];
                int bytes;
                while ((entry = zipIS.getNextEntry()) != null) {
                    if (entry.getName().equals("WEB-INF/web.xml")) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        while ((bytes = zipIS.read(buffer)) > 0) {
                            baos.write(buffer, 0, bytes);
                        }
                        webIS = new ByteArrayInputStream(baos.toByteArray());
                    } else if (entry.getName().equals("WEB-INF/jboss-web.xml")) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        while ((bytes = zipIS.read(buffer)) > 0) {
                            baos.write(buffer, 0, bytes);
                        }
                        jbossWebIS = new ByteArrayInputStream(baos.toByteArray());
                    }
                }
                zipIS.close();
            }
            XmlFileLoader xmlLoader = new XmlFileLoader();
            String warURI = warURL.toExternalForm();
            try {
                if (webIS != null) {
                    Document webDoc = xmlLoader.getDocument(webIS, warURI + "/WEB-INF/web.xml");
                    Element web = webDoc.getDocumentElement();
                    metaData.importXml(web);
                }
            } catch (Exception e) {
                throw new DeploymentException("Failed to parse WEB-INF/web.xml", e);
            }
            try {
                if (jbossWebIS != null) {
                    Document jbossWebDoc = xmlLoader.getDocument(jbossWebIS, warURI + "/WEB-INF/jboss-web.xml");
                    Element jbossWeb = jbossWebDoc.getDocumentElement();
                    metaData.importXml(jbossWeb);
                }
            } catch (Exception e) {
                throw new DeploymentException("Failed to parse WEB-INF/jboss-web.xml", e);
            }
        } catch (DeploymentException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Failed to parse descriptors for war(" + warURL + ")", e);
        }
        String webContext = ctxPath;
        if (webContext == null) webContext = metaData.getContextRoot();
        if (webContext == null) {
            webContext = warName;
            webContext = webContext.replace('\\', '/');
            if (webContext.endsWith("/")) webContext = webContext.substring(0, webContext.length() - 1);
            int prefix = webContext.lastIndexOf('/');
            if (prefix > 0) webContext = webContext.substring(prefix + 1);
            int suffix = webContext.lastIndexOf(".");
            if (suffix > 0) webContext = webContext.substring(0, suffix);
            int index = 0;
            for (; index < webContext.length(); index++) {
                char c = webContext.charAt(index);
                if (Character.isDigit(c) == false && c != '.') break;
            }
            webContext = webContext.substring(index);
        }
        if (webContext.length() > 0 && webContext.charAt(0) != '/') webContext = "/" + webContext; else if (webContext.equals("/")) webContext = "";
        metaData.setContextRoot(webContext);
    }
}
