package org.nightlabs.jfire.update.admin.ui.rcp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.i18n.I18nTextBuffer;
import org.nightlabs.io.DataBuffer;
import org.nightlabs.jfire.update.Requirement;
import org.nightlabs.jfire.update.AbstractClientElement.UnPublishableException;
import org.nightlabs.jfire.update.eclipsercp.RCPPlugin;
import org.nightlabs.jfire.update.id.ClientElementID;
import org.nightlabs.util.IOUtil;
import org.nightlabs.version.MalformedVersionException;
import org.nightlabs.version.OSGIVersionUtil;
import org.nightlabs.version.UnitIdentifier;
import org.nightlabs.version.Version;
import org.nightlabs.version.VersionPattern;
import org.nightlabs.version.VersionRangeEndPoint;
import org.nightlabs.version.VersionPattern.MatchRule;
import org.nightlabs.version.VersionRangeEndPoint.EndPointLocation;
import org.osgi.framework.Constants;

/**
 * @author Marius Heinzmann -- Marius[at]NightLabs[dot]de
 *
 */
public final class RCPParseUtil {

    /**
	 * Logger used throughout this class.
	 */
    private static Logger logger = Logger.getLogger(RCPParseUtil.class);

    /**
	 * The types of bundles that may be encountered (except for Features).
	 *
	 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
	 */
    public enum BundleType {

        Plugin, Fragment
    }

    public static String[] getBundleSymbolicNameAndOptions(Manifest manifest) {
        String symbolicNameOptions = manifest.getMainAttributes().getValue(Constants.BUNDLE_SYMBOLICNAME);
        if (symbolicNameOptions == null) throw new IllegalArgumentException("The Bundle's symbolic name is null! This must not be!");
        return symbolicNameOptions.split(";");
    }

    public static String getBundleName(Manifest manifest) {
        final String bundleName = manifest.getMainAttributes().getValue(Constants.BUNDLE_NAME);
        return (bundleName != null) ? bundleName : "";
    }

    public static String getBundleDescription(Manifest manifest) {
        final String desc = manifest.getMainAttributes().getValue(Constants.BUNDLE_DESCRIPTION);
        return (desc != null) ? desc : "";
    }

    public static String getBundleProvider(Manifest manifest) {
        final String provider = manifest.getMainAttributes().getValue(Constants.BUNDLE_VENDOR);
        return (provider != null) ? provider : "";
    }

    public static Version getBundleVersion(Manifest manifest) {
        String version = manifest.getMainAttributes().getValue(Constants.BUNDLE_VERSION);
        if (version == null) throw new IllegalArgumentException("The Bundle's version is null! This must not be!");
        try {
            return OSGIVersionUtil.parseOSGIVersionString(version);
        } catch (MalformedVersionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
	 * Returns the BundleType of the given File if it can be determined, <code>null</code> otherwise.
	 *
	 * @param bundleFile The BundleType of the given file object.
	 * @return the BundleType of the given File if it can be determined, <code>null</code> otherwise.
	 */
    public static BundleType getBundleType(File bundleFile) {
        if (bundleFile == null) return null;
        if (bundleFile.isDirectory()) {
            if (new File(bundleFile, "plugin.xml").exists()) return BundleType.Plugin;
            if (new File(bundleFile, "fragment.xml").exists()) return BundleType.Fragment;
            File manifestFile = new File(bundleFile, JarFile.MANIFEST_NAME);
            if (!manifestFile.exists()) return null;
            try {
                InputStream manifestStream = new FileInputStream(manifestFile);
                Manifest manifest = new Manifest(manifestStream);
                Attributes attributes = manifest.getMainAttributes();
                if (attributes == null) {
                    logger.error("The given bundle doesn't have a main section in its MANIFEST.MF! " + "Hence, we cannot determine its bundle type => it will be skipped! bundle=" + bundleFile.getAbsolutePath());
                    return null;
                }
                String value = attributes.getValue("Fragment-Host");
                if (value == null) return BundleType.Plugin; else return BundleType.Fragment;
            } catch (IOException e) {
                throw new RuntimeException("Couldn't read the MANIFEST.MF!", e);
            }
        } else {
            if (!"jar".equalsIgnoreCase(IOUtil.getFileExtension(bundleFile.getName()))) return null;
            JarFile jarFileToUpload;
            try {
                jarFileToUpload = new JarFile(bundleFile, false);
                ZipEntry entry = jarFileToUpload.getEntry("fragment.xml");
                if (entry != null && !entry.isDirectory()) return BundleType.Fragment;
                entry = jarFileToUpload.getEntry("plugin.xml");
                if (entry != null && !entry.isDirectory()) return BundleType.Plugin;
                Manifest manifest = jarFileToUpload.getManifest();
                if (manifest == null) return null;
                Attributes attributes = manifest.getMainAttributes();
                if (attributes == null) {
                    logger.error("The given bundle doesn't have a main section in its MANIFEST.MF! " + "Hence, we cannot determine its bundle type => it will be skipped! bundle=" + bundleFile.getAbsolutePath());
                    return null;
                }
                String value = attributes.getValue("Fragment-Host");
                if (value == null) return BundleType.Plugin; else return BundleType.Fragment;
            } catch (IOException e) {
                logger.warn("Couldn't read the jarFile for BundleType checking!", e);
            }
        }
        return null;
    }

    /**
	 * Returns a set of all requirements defined in the given MANIFEST.MF according to the
	 * <a href="http://help.eclipse.org/help33/topic/org.eclipse.platform.doc.isv/reference/misc/plugin_manifest.html">
	 * 	Eclipse definition</a>.
	 *
	 * @param manifest the MANIFEST.MF to parse.
	 * @return a set of all requirements defined in the given {@link Manifest}.
	 */
    public static Set<Requirement> getRequirements(Manifest manifest) {
        final Set<Requirement> requirements = new HashSet<Requirement>();
        final String requirementsString = manifest.getMainAttributes().getValue(Constants.REQUIRE_BUNDLE);
        if (requirementsString != null) {
            String[] tmpBundles = requirementsString.split(",");
            Stack<String> bundles = new Stack<String>();
            int quoteCount = 0;
            boolean needsConcat = false;
            final Pattern quotePattern = Pattern.compile("(\")");
            for (String tmpBundle : tmpBundles) {
                Matcher matcher = quotePattern.matcher(tmpBundle);
                quoteCount += countRegexpMatches(matcher);
                if (needsConcat) bundles.push(bundles.pop().concat("," + tmpBundle)); else bundles.push(tmpBundle);
                if (quoteCount % 2 != 0) needsConcat = true; else needsConcat = false;
            }
            for (String requiredBundle : bundles) {
                String[] bundleOptions = requiredBundle.split(";");
                String bundleName = bundleOptions[0];
                VersionPattern versionPattern = null;
                if (bundleOptions.length > 1) {
                    boolean patternFound = false;
                    for (String option : bundleOptions) {
                        String[] keyValue;
                        if (option.contains(":=")) keyValue = option.split(":="); else keyValue = option.split("=");
                        if (Constants.BUNDLE_VERSION_ATTRIBUTE.equals(keyValue[0].trim())) {
                            String versionString = keyValue[1];
                            versionString = versionString.substring(1, versionString.length() - 1);
                            if (versionString.startsWith(String.valueOf(OSGIVersionUtil.EXCLUSIVE_BRACKETS[0])) || versionString.startsWith(String.valueOf(OSGIVersionUtil.INCLUSIVE_BRACKETS[0]))) {
                                try {
                                    versionPattern = OSGIVersionUtil.parseOSGIVersionPattern(versionString);
                                } catch (MalformedVersionException e) {
                                    throw new RuntimeException("The string representation of the given OSGI " + "pattern was not correct: ", e);
                                }
                            } else {
                                try {
                                    versionPattern = new VersionPattern(new VersionRangeEndPoint(OSGIVersionUtil.parseOSGIVersionString(versionString), true, EndPointLocation.LOWER), MatchRule.Compatible);
                                } catch (MalformedVersionException e) {
                                    throw new RuntimeException("The string representation of the given OSGI " + "version was not correct: ", e);
                                }
                            }
                            patternFound = true;
                        }
                    }
                    if (!patternFound) versionPattern = VersionPattern.MATCHES_ALL;
                    requirements.add(new Requirement(bundleName, versionPattern, null));
                }
            }
        }
        return requirements;
    }

    /**
	 * @param matcher the matcher to return its match count.
	 * @return the number of times this matcher matches its input string.
	 */
    private static int countRegexpMatches(Matcher matcher) {
        if (matcher == null) return 0;
        int result = 0;
        while (matcher.find()) result++;
        return result;
    }

    /**
	 * Creates an RCPPlugin from the given <code>pluginFile</code>, which can be the jar file
	 * containing the plugin or the its base directory.
	 * @param allRCPPluginIDs The ClientElementIDs of all already in the datastore existing RCPPlugins.
	 * 	This is needed to check whether we need to upload a selected plugin or not.
	 * @param pluginFile Either the file object pointing to the jar file containing the plugin or
	 * 	the plugin's base directory.
	 *
	 * @return an RCPPlugin from the given <code>pluginFile</code>, which can be the jar file
	 * containing the plugin or the its base directory.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
    public static RCPPlugin createRCPPlugin(Set<ClientElementID> allRCPPluginIDs, File pluginFile) throws IOException {
        JarFile jarFileToUpload = null;
        File tempJarFile = null;
        DataBuffer pluginBuffer = null;
        Manifest manifest;
        RCPPlugin plugin;
        try {
            if (pluginFile.isDirectory()) {
                File manifestFile = new File(pluginFile, JarFile.MANIFEST_NAME);
                if (!manifestFile.exists()) {
                    logger.warn("Couldn't upload fragment '" + pluginFile.getName() + "', because it doesn't " + "provide a META-INF/MANIFEST.MF!");
                    return null;
                }
                tempJarFile = File.createTempFile("jaredPlugin", ".jar");
                pluginBuffer = createJarFileInBuffer(pluginFile, tempJarFile);
                jarFileToUpload = new JarFile(tempJarFile);
                manifest = jarFileToUpload.getManifest();
            } else {
                try {
                    jarFileToUpload = new JarFile(pluginFile, false);
                    manifest = jarFileToUpload.getManifest();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            Set<PropertyResult> localisationProps = PropertiesUtil.getAllProperties(jarFileToUpload);
            Properties defaultProps = null;
            if (localisationProps != null) {
                for (PropertyResult props : localisationProps) {
                    if (props.getLocale().equals(Locale.ENGLISH)) {
                        defaultProps = props.getProperties();
                        break;
                    }
                }
                if (defaultProps == null && !localisationProps.isEmpty()) defaultProps = localisationProps.iterator().next().getProperties();
            }
            final String symbolicName = RCPParseUtil.getBundleSymbolicNameAndOptions(manifest)[0];
            final String _name = RCPParseUtil.getBundleName(manifest);
            String name = "";
            if (!_name.trim().startsWith("%")) {
                name = _name;
            } else {
                if (defaultProps == null) {
                    logger.warn(symbolicName + "'s Manifest.MF uses localised Strings (" + _name + "), but there is no localisation file!");
                } else {
                    name = defaultProps.getProperty(_name.substring(1));
                    if (name == null) {
                        logger.error(symbolicName + "'s Manifest.MF declares a localised String (" + _name + "), but there is no entry in for it in the default localisation file!");
                    }
                }
            }
            final String _provider = RCPParseUtil.getBundleProvider(manifest);
            String provider = "";
            if (!_provider.trim().startsWith("%")) {
                provider = _provider;
            } else {
                if (defaultProps == null) {
                    logger.warn(symbolicName + "'s Manifest.MF uses localised Strings (" + _provider + "), but there is no localisation file!");
                } else {
                    provider = defaultProps.getProperty(_provider.substring(1));
                    if (provider == null) {
                        logger.warn(symbolicName + "'s Manifest.MF declares a localised String (" + _provider + "), but there is no entry in for it in the default localisation file!");
                    }
                }
            }
            String _description = RCPParseUtil.getBundleDescription(manifest);
            I18nText description;
            if (!_description.trim().startsWith("%")) {
                description = new I18nTextBuffer();
                description.setText(I18nText.DEFAULT_LANGUAGEID, _description);
            } else {
                if (defaultProps == null) {
                    throw new RuntimeException("The Plugin's (" + symbolicName + ") Manifest.MF uses localised Strings (" + _description + "), but there is no localisation file!");
                } else {
                    description = PropertiesUtil.getAllLanguageEntries(localisationProps, _description);
                    if (description == null) {
                        throw new RuntimeException("The Plugin's (" + symbolicName + ") Manifest.MF declares a localised String (" + _description + "), but there seems to be no entry in the localisation files!");
                    }
                }
            }
            Version pluginVersion = RCPParseUtil.getBundleVersion(manifest);
            ClientElementID pluginID = new ClientElementID(symbolicName, RCPPlugin.PLUGIN_ELEMENT_TYPE, pluginVersion);
            if (allRCPPluginIDs != null && allRCPPluginIDs.contains(pluginID)) {
                logger.warn("Skipping plugin: " + symbolicName + " in version:" + pluginVersion + ", since it is " + "already in the datastore.");
                return null;
            }
            try {
                DataBuffer bufferToUpload;
                if (pluginBuffer != null) bufferToUpload = pluginBuffer; else {
                    InputStream inStream = new BufferedInputStream(new FileInputStream(pluginFile));
                    try {
                        bufferToUpload = new DataBuffer(inStream);
                    } finally {
                        inStream.close();
                    }
                }
                plugin = new RCPPlugin(new UnitIdentifier(symbolicName, RCPPlugin.PLUGIN_ELEMENT_TYPE, pluginVersion), bufferToUpload.createByteArray(), name, provider, description);
            } catch (FileNotFoundException e1) {
                throw new RuntimeException(e1);
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
        } finally {
            if (tempJarFile != null && !tempJarFile.delete()) logger.warn("Couldn't delete the temporary jar file:" + tempJarFile.getAbsolutePath());
        }
        Set<Requirement> requirements = RCPParseUtil.getRequirements(manifest);
        plugin.addRequirements(requirements);
        if (plugin.getResolveException() == null) {
            try {
                plugin.publish();
            } catch (UnPublishableException e) {
                throw new RuntimeException("Couldn't publish the unpublished new RCPPlugin '" + plugin.getIdentifier() + "!! WTF!", e);
            }
        }
        return plugin;
    }

    /**
	 * Creates a DataBuffer containing the jared content of the whole subdirectory structure rooted at
	 * <code>pluginBaseDir</code>.
	 *
	 * @param pluginBaseDir The directory containing all the data that shall be jared into the
	 * 	DataBuffer.
	 * @param targetFile The file to pack all the pluginBaseDir into.
	 * @return a DataBuffer containing the jared content of the whole subdirectory structure rooted at
	 * <code>pluginBaseDir</code>.
	 */
    private static DataBuffer createJarFileInBuffer(File pluginBaseDir, File targetFile) {
        assert pluginBaseDir != null;
        assert targetFile != null && targetFile.isFile();
        ZipOutputStream jarOutStream = null;
        try {
            DataBuffer buffer = new DataBuffer(1024, targetFile);
            OutputStream outstream = buffer.createOutputStream();
            jarOutStream = new ZipOutputStream(outstream);
            for (File child : pluginBaseDir.listFiles()) {
                addRecursivelyToJar(child, "", jarOutStream);
            }
            jarOutStream.close();
            outstream.close();
            return buffer;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (jarOutStream != null) {
                try {
                    jarOutStream.close();
                } catch (IOException e) {
                    throw new RuntimeException("IO Error when trying to close output stream.", e);
                }
            }
        }
    }

    /**
	 * Adds all files and directories recursively to the given JarOutputstream.
	 *
	 * @param src the file/dir to start with.
	 * @param prefix the prefix for the ZipEntry (the folder hierarchy up to the file)
	 * @param jout the ZipOutputstream to write all the content of the <code>src</code> file to.
	 * @throws IOException
	 */
    private static void addRecursivelyToJar(File src, String prefix, ZipOutputStream jout) throws IOException {
        if (src.isDirectory()) {
            prefix = prefix + src.getName() + "/";
            ZipEntry entry = new ZipEntry(prefix);
            entry.setTime(src.lastModified());
            entry.setMethod(ZipEntry.STORED);
            entry.setSize(0L);
            entry.setCrc(0L);
            jout.putNextEntry(entry);
            jout.closeEntry();
            File files[] = src.listFiles();
            for (int i = 0; i < files.length; i++) addRecursivelyToJar(files[i], prefix, jout);
        } else {
            ZipEntry entry = new ZipEntry(prefix + src.getName());
            entry.setTime(src.lastModified());
            jout.putNextEntry(entry);
            FileInputStream in = new FileInputStream(src);
            IOUtil.transferStreamData(in, jout);
            in.close();
            jout.closeEntry();
        }
    }
}
