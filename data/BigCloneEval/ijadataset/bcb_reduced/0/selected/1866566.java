package org.jiopi.ibean.kernel.repository.config;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import org.jiopi.ibean.kernel.Version;
import org.jiopi.ibean.kernel.util.FileUtil;
import org.jiopi.ibean.share.ShareUtil.IOUtil;
import org.jiopi.ibean.kernel.util.ResourceUtil;
import org.jiopi.ibean.kernel.repository.RemoteFileManager;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * 
 * 模块的配置信息对象
 * 
 * @since iBean0.1 2010.4.24
 * @version 0.1
 *
 */
public class ModuleConfig {

    private static Logger logger = Logger.getLogger(ModuleConfig.class);

    private final String configFileDirPath;

    private final String moduleDirPath;

    private final UsernamePasswordCredentials creds;

    private List<Release> releases = null;

    private ModuleConfig(String moduleDirPath, UsernamePasswordCredentials creds) {
        this.moduleDirPath = moduleDirPath;
        this.creds = creds;
        if (moduleDirPath != null) this.configFileDirPath = FileUtil.joinPath(moduleDirPath, "/config/"); else this.configFileDirPath = null;
    }

    public Release getRelease(Version compatibleVersion) {
        for (Release release : releases) {
            if (compatibleVersion.isCompatible(release)) {
                return release;
            }
        }
        return null;
    }

    private boolean loadLocal(URL configFileURL) {
        File moduleDir = new File(configFileURL.getFile());
        if (moduleDir.isDirectory()) {
            File[] releases = moduleDir.listFiles();
            List<Release> moduleReleases = new ArrayList<Release>();
            for (File release : releases) {
                if (release.isDirectory()) {
                    String version = release.getName();
                    if (ResourceUtil.isCorrectVersion(version)) {
                        Release aRelease = new Release(version, null);
                        File libDir = new File(release, "lib");
                        File classesDir = new File(release, "classes");
                        File confDir = new File(release, "conf");
                        ArrayList<URL> libURLList = new ArrayList<URL>();
                        if (libDir.isDirectory()) {
                            File[] libFiles = libDir.listFiles();
                            for (File libFile : libFiles) {
                                if (libFile.isFile() && libFile.getName().toLowerCase().endsWith(".jar")) {
                                    libURLList.add(FileUtil.toURL(libFile.getAbsolutePath()));
                                }
                            }
                        }
                        if (classesDir.isDirectory()) {
                            libURLList.add(FileUtil.toURL(classesDir.getAbsolutePath()));
                        }
                        if (confDir.isDirectory()) {
                            libURLList.add(FileUtil.toURL(confDir.getAbsolutePath()));
                        }
                        aRelease.setResources(libURLList.toArray(new URL[libURLList.size()]));
                        moduleReleases.add(aRelease);
                    }
                }
            }
            this.releases = Collections.unmodifiableList(moduleReleases);
            return true;
        }
        return false;
    }

    /**
	 * 初始化模块配置
	 * @param configFileURL
	 * @return 是否初始化成功
	 */
    @SuppressWarnings("unchecked")
    private boolean loadRemote(URL configFileURL) {
        File moduleDir = FileUtil.confirmDir(moduleDirPath, true);
        if (!moduleDir.isDirectory()) return false;
        File configDir = FileUtil.confirmDir(configFileDirPath, true);
        if (!configDir.isDirectory()) return false;
        File moduleConfigFile = RemoteFileManager.getRemoteFile(configFileURL, configDir, creds);
        if (!moduleConfigFile.isFile()) return false;
        try {
            URI rootURI = configFileURL.toURI();
            SAXReader reader = new SAXReader();
            Document doc = reader.read(moduleConfigFile);
            Element module = doc.getRootElement();
            String reference = module.attributeValue("reference");
            String baseURL = module.attributeValue("base-url");
            baseURL = FileUtil.correctURIDirPath(baseURL);
            if (baseURL != null) rootURI = rootURI.resolve(baseURL);
            if (reference != null) {
                rootURI = rootURI.resolve(reference);
                moduleConfigFile = RemoteFileManager.getRemoteFile(rootURI.toURL(), configDir, creds);
                doc = reader.read(moduleConfigFile);
                module = doc.getRootElement();
                baseURL = module.attributeValue("base-url");
                baseURL = FileUtil.correctURIDirPath(baseURL);
                if (baseURL != null) rootURI = rootURI.resolve(baseURL);
            }
            List<Release> moduleReleases = new ArrayList<Release>();
            List<Element> releases = module.elements("release");
            for (Element release : releases) {
                String version = release.attributeValue("version");
                String releaseReference = release.attributeValue("reference");
                String releaseBaseURL = release.attributeValue("base-url");
                releaseBaseURL = FileUtil.correctURIDirPath(releaseBaseURL);
                URI releaseBaseURI = rootURI;
                if (releaseBaseURL != null) releaseBaseURI = releaseBaseURI.resolve(releaseBaseURL);
                Release aRelease = new Release(version, releaseBaseURI);
                if (releaseReference != null) {
                    URI releaseURI = releaseBaseURI.resolve(releaseReference);
                    aRelease.setReference(releaseURI.toURL());
                } else {
                    aRelease.setReleaseElement(release);
                }
                moduleReleases.add(aRelease);
            }
            this.releases = Collections.unmodifiableList(moduleReleases);
            return true;
        } catch (Exception e) {
            logger.error(e);
        }
        return false;
    }

    /**
	 * 
	 * @param moduleDirPath 模块保存路径
	 * @param configFileURL 模块的配置文件路径
     * @param isLocal whether this module is from local repository
	 * @return
	 */
    public static ModuleConfig getModuleConfig(String moduleDirPath, URL configFileURL, boolean isLocal, UsernamePasswordCredentials creds) {
        ModuleConfig mc = new ModuleConfig(moduleDirPath, creds);
        if (!isLocal && mc.loadRemote(configFileURL)) return mc; else if (isLocal && mc.loadLocal(configFileURL)) return mc;
        return null;
    }

    /**
	 * 
	 * release对象
	 * 
	 * 使用配置延迟加载模式
	 * 如果release使用了reference引用，则仅在需要用到时才加载配置信息
	 * 
	 * @since 0.1
	 *
	 */
    public class Release extends Version {

        private URL reference = null;

        private URI baseURI = null;

        private volatile Element release = null;

        private URL[] resources = null;

        public Release(String version, URI baseURI) {
            super(version);
            this.baseURI = baseURI;
        }

        protected void setReference(URL reference) throws URISyntaxException {
            this.reference = reference;
            baseURI = reference.toURI();
        }

        protected void setReleaseElement(Element release) {
            this.release = release;
        }

        protected void setResources(URL[] resources) {
            this.resources = resources;
        }

        /**
		 * 获取当前Release的资源列表
		 * @return
		 */
        @SuppressWarnings("unchecked")
        public synchronized URL[] getResources() {
            if (this.resources == null) {
                if (release == null) initRelease();
                if (release == null) return null;
                List<Element> resources = release.elements("resource");
                ArrayList<URL> resourceURLList = new ArrayList<URL>();
                for (Element resource : resources) {
                    String resourcePath = resource.getTextTrim();
                    try {
                        URL resourceURL = baseURI.resolve(resourcePath).toURL();
                        resourceURLList.add(resourceURL);
                    } catch (MalformedURLException e) {
                        logger.error(e);
                    }
                }
                ArrayList<URL> localResourceURLList = new ArrayList<URL>();
                String releaseDirPath = FileUtil.joinPath(moduleDirPath, this.version);
                File cacheDir = FileUtil.confirmDir(releaseDirPath, true);
                File confDir = null;
                for (URL remote : resourceURLList) {
                    File localFile = RemoteFileManager.getRemoteFile(remote, cacheDir, creds);
                    if (localFile.isFile()) {
                        if (localFile.getName().endsWith(".jar")) {
                            localResourceURLList.add(FileUtil.toURL(localFile.getAbsolutePath()));
                        } else {
                            if (confDir == null) {
                                String confDirPath = FileUtil.joinPath(releaseDirPath, "conf");
                                confDir = FileUtil.confirmDir(confDirPath, true);
                                if (confDir.isDirectory()) {
                                    localResourceURLList.add(FileUtil.toURL(confDir.getAbsolutePath()));
                                }
                            }
                            if (confDir.isDirectory()) {
                                File newFile = new File(confDir, new File(remote.getFile()).getName());
                                try {
                                    IOUtil.copyFile(localFile, newFile);
                                } catch (IOException e) {
                                    logger.error("", e);
                                }
                            }
                        }
                    } else logger.warn("can't load file :" + remote);
                }
                this.resources = localResourceURLList.toArray(new URL[localResourceURLList.size()]);
            }
            return this.resources;
        }

        private void initRelease() {
            if (release == null && reference != null) {
                File configDir = FileUtil.confirmDir(configFileDirPath, true);
                File releaseConfigFile = RemoteFileManager.getRemoteFile(reference, configDir, creds);
                if (logger.isDebugEnabled()) logger.debug("get release config :" + releaseConfigFile);
                if (releaseConfigFile.isFile()) {
                    try {
                        SAXReader reader = new SAXReader();
                        Document doc = reader.read(releaseConfigFile);
                        Element release = doc.getRootElement();
                        String releaseBaseURL = release.attributeValue("base-url");
                        releaseBaseURL = FileUtil.correctURIDirPath(releaseBaseURL);
                        if (releaseBaseURL != null) this.baseURI = this.baseURI.resolve(releaseBaseURL);
                        setReleaseElement(release);
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
            }
        }
    }
}
