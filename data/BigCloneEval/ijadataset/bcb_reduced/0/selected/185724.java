package org.eclipse.babel.build.core.eclipsetarget;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import org.eclipse.babel.build.core.Messages;
import org.eclipse.babel.build.core.PluginProxy;
import org.eclipse.babel.build.core.ResourceProxy;

public class FragmentEclipseInstallMediator implements FragmentEclipseTargetMediator {

    private PluginProxy eclipseInstallPlugin;

    public FragmentEclipseInstallMediator(PluginProxy eclipseInstallPlugin) {
        this.eclipseInstallPlugin = eclipseInstallPlugin;
    }

    public Map<String, Set<Object>> extractEclipseTargetProperties() throws ZipException, IOException, FileNotFoundException {
        Map<String, Set<Object>> propertiesMap = new HashMap<String, Set<Object>>();
        InputStream jarPluginInputStream = new FileInputStream(eclipseInstallPlugin.getPluginLocation());
        ZipInputStream inputStream = new ZipInputStream(jarPluginInputStream);
        ZipEntry entry;
        while ((entry = inputStream.getNextEntry()) != null) {
            String entryName = entry.getName();
            if (entryName.endsWith(Messages.getString("Extensions_properties"))) {
                Properties properties = new Properties();
                properties.load(inputStream);
                propertiesMap.put(entry.getName(), new HashSet<Object>(properties.keySet()));
            }
        }
        return propertiesMap;
    }

    public InputStream getResourceAsStream(ResourceProxy resource) throws ZipException, IOException, FileNotFoundException {
        InputStream stream;
        if (!resource.getFileResource().exists()) {
            String entryPath = resource.getCanonicalPath();
            if (entryPath.contains(JAR_RESOURCE_SUFFIX + SLASH)) {
                String target = entryPath.substring(0, entryPath.indexOf(JAR_RESOURCE_SUFFIX + SLASH) + JAR_RESOURCE_SUFFIX.length());
                entryPath = entryPath.replace(target + SLASH, "");
            }
            int jarResourceEndIndex = resource.getFileResource().getAbsolutePath().indexOf(JAR_EXTENSION) + JAR_EXTENSION.length();
            String jarResourcePath = resource.getFileResource().getAbsolutePath().substring(0, jarResourceEndIndex);
            JarFile jarFile = new JarFile(new File(jarResourcePath));
            JarEntry resourceEntry = jarFile.getJarEntry(entryPath);
            stream = jarFile.getInputStream(resourceEntry);
        } else {
            stream = new FileInputStream(resource.getFileResource());
        }
        return stream;
    }

    public boolean writeAboutHtmlFile(File resource, JarOutputStream out) throws Exception {
        try {
            ZipEntry newEntry;
            InputStream inputStream;
            if (eclipseInstallPlugin.isJar()) {
                JarFile pluginJar = new JarFile(this.eclipseInstallPlugin.getPluginLocation());
                newEntry = pluginJar.getJarEntry(ABOUT_FILE);
                out.putNextEntry(newEntry);
                inputStream = pluginJar.getInputStream(newEntry);
            } else {
                newEntry = new ZipEntry(resource.getName());
                out.putNextEntry(newEntry);
                inputStream = new FileInputStream(resource);
            }
            writeToOutputStream(out, inputStream);
            out.closeEntry();
            return true;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private void writeToOutputStream(JarOutputStream out, InputStream in) throws Exception {
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
    }
}
