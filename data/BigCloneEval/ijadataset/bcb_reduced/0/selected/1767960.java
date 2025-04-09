package org.intellij.trinkets.pluginPacker;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.compiler.CompilerPaths;
import com.intellij.util.containers.HashMap;
import com.intellij.util.containers.HashSet;
import com.intellij.util.io.ZipUtil;
import org.intellij.trinkets.pluginPacker.util.PluginPackerBundle;
import org.intellij.trinkets.pluginPacker.util.PluginXmlUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.devkit.build.PluginBuildConfiguration;
import org.jetbrains.idea.devkit.build.PluginBuildUtil;
import org.jetbrains.idea.devkit.build.PrepareToDeployAction;
import java.io.*;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Plugin packer project service.
 *
 * @author Alexey Efimov
 */
public final class PluginPacker {

    @NonNls
    private static final String JAR_EXTENSION = ".jar";

    @NonNls
    private static final String ZIP_EXTENSION = ".zip";

    @NonNls
    private static final String TEMP_PREFIX = "temp";

    @NonNls
    private static final Pattern PATTERN = Pattern.compile("\\$\\{([\\w\\.\\-\\_]+)\\}");

    @NonNls
    private static final String LIB_PATH_WITH_EXT_FORMAT = "/{0}/lib/{1}{2}";

    @NonNls
    private static final String LIB_PATH_DIR_FORMAT = "/{0}/lib/{1}";

    @NonNls
    private static final String SRC_DIR_FORMAT = "{0}/src/{1}";

    @NonNls
    private static final String LIB_STRUCTURE_FORMAT = "{0}/lib";

    @NonNls
    private static final String SRC_STRUCTURE_FORMAT = "{0}/src";

    @NonNls
    private static final String PLUGIN_XML_PATH = "/META-INF/plugin.xml";

    @NonNls
    private static final String PLUGIN_ID = "plugin.id";

    @NonNls
    private static final String PLUGIN_VERSION = "plugin.version";

    @NonNls
    private static final String MODULE_NAME = "module.name";

    protected final Project project;

    private final FileTypeManager myFileTypeManager = FileTypeManager.getInstance();

    public PluginPacker(Project project) {
        this.project = project;
    }

    public boolean packModule(@NotNull final Module module, @NotNull String packagePattern, String sourcesPattern, final boolean inboxSources, @NotNull String outputDirectory, boolean silentOverwrite) {
        Map<String, String> variables = new HashMap<String, String>();
        String pluginId = StringUtil.decapitalize(PluginXmlUtil.getPluginId(module));
        String moduleName = StringUtil.decapitalize(module.getName());
        variables.put(MODULE_NAME, moduleName);
        variables.put(PLUGIN_ID, pluginId);
        variables.put(PLUGIN_VERSION, PluginXmlUtil.getPluginVersion(module));
        final String pluginName = packagePattern.indexOf(MODULE_NAME) != -1 ? moduleName : pluginId;
        final String packageName = replaceVariables(packagePattern, variables);
        final String sourcesName = sourcesPattern != null ? replaceVariables(sourcesPattern, variables) : null;
        try {
            final File binZipBuffer = createTempFile(ZIP_EXTENSION);
            final File srcZipBuffer = sourcesName != null && !inboxSources ? createTempFile(ZIP_EXTENSION) : null;
            final File outputBinFile = new File(outputDirectory, packageName);
            if (!silentOverwrite && !confirmFileOverwriting(outputBinFile)) {
                return false;
            }
            final File outputSrcFile = srcZipBuffer != null ? new File(outputDirectory, sourcesName) : null;
            if (outputSrcFile != null && !silentOverwrite && !confirmFileOverwriting(outputSrcFile)) {
                return false;
            }
            @NonNls final Set<String> errorSet = new HashSet<String>();
            final Set<Module> modules = new HashSet<Module>();
            PluginBuildUtil.getDependencies(module, modules);
            modules.add(module);
            final Set<Library> libs = new HashSet<Library>();
            for (Module module1 : modules) {
                PluginBuildUtil.getLibraries(module1, libs);
            }
            boolean isOk = ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {

                public void run() {
                    final ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
                    try {
                        if (progressIndicator != null) {
                            progressIndicator.setText(PluginPackerBundle.message("preparing.binary.package"));
                            progressIndicator.setIndeterminate(true);
                        }
                        File jarFile = createTempPluginJar(module, modules);
                        File srcFile = null;
                        if (srcZipBuffer == null && sourcesName != null) {
                            if (progressIndicator != null) {
                                progressIndicator.setText(PluginPackerBundle.message("preparing.sources.package"));
                                progressIndicator.setIndeterminate(true);
                            }
                            srcFile = createTempSourcesZip(modules);
                        }
                        if (progressIndicator != null) {
                            progressIndicator.setText(PluginPackerBundle.message("building.binary.package.0", outputBinFile.getName()));
                            progressIndicator.setIndeterminate(true);
                        }
                        processLibraries(pluginName, jarFile, srcFile, sourcesName, binZipBuffer, libs, progressIndicator);
                        if (srcZipBuffer != null) {
                            if (progressIndicator != null) {
                                progressIndicator.setText(PluginPackerBundle.message("building.sources.package.0", outputSrcFile.getName()));
                                progressIndicator.setIndeterminate(true);
                            }
                            zipSources(srcZipBuffer, modules);
                        }
                    } catch (final IOException e1) {
                        ApplicationManager.getApplication().invokeLater(new Runnable() {

                            public void run() {
                                errorSet.add("error");
                                Messages.showErrorDialog(e1.getMessage(), PluginPackerBundle.message("plugin.packing.error"));
                            }
                        }, ModalityState.NON_MODAL);
                    }
                }
            }, PluginPackerBundle.message("preparing.plugin.pack"), true, module.getProject());
            if (isOk && errorSet.isEmpty()) {
                FileUtil.copy(binZipBuffer, outputBinFile);
                if (srcZipBuffer != null) {
                    FileUtil.copy(srcZipBuffer, outputSrcFile);
                    WindowManager.getInstance().getStatusBar(project).setInfo(PluginPackerBundle.message("file.0.and.1.saved.successful", outputBinFile.getName(), outputSrcFile.getName()));
                } else {
                    WindowManager.getInstance().getStatusBar(project).setInfo(PluginPackerBundle.message("file.0.saved.successful", outputBinFile.getName()));
                }
                return true;
            }
        } catch (final IOException e) {
            ApplicationManager.getApplication().invokeLater(new Runnable() {

                public void run() {
                    Messages.showErrorDialog(e.getMessage(), PluginPackerBundle.message("plugin.packing.error"));
                }
            }, ModalityState.NON_MODAL);
        }
        return false;
    }

    private static File createTempFile(String extension) throws IOException {
        File tempFile = FileUtil.createTempFile(TEMP_PREFIX, extension);
        tempFile.deleteOnExit();
        return tempFile;
    }

    private boolean confirmFileOverwriting(@NotNull File file) {
        if (file.exists()) {
            if (Messages.showYesNoDialog(project, PluginPackerBundle.message("file.already.exist.ndelete.old.file.0", file.getPath()), PluginPackerBundle.message("packing.plugin"), Messages.getInformationIcon()) == DialogWrapper.OK_EXIT_CODE) {
                FileUtil.delete(file);
            } else {
                return false;
            }
        }
        return true;
    }

    private static String replaceVariables(String pattern, Map<String, String> variables) {
        Matcher matcher = PATTERN.matcher(pattern);
        while (matcher.find()) {
            String value = variables.get(matcher.group(1));
            if (StringUtil.isNotEmpty(value)) {
                int start = matcher.start();
                int end = matcher.end();
                pattern = pattern.substring(0, start).concat(value).concat(pattern.substring(end));
                matcher.reset(pattern);
            }
        }
        return pattern;
    }

    private void processLibraries(String name, final File jarFile, final File srcZipFile, final String sourcesName, final File zipFile, final Set<Library> libs, final ProgressIndicator progressIndicator) throws IOException {
        if (zipFile.exists() || zipFile.createNewFile()) {
            ZipOutputStream zos = null;
            try {
                zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
                addStructure(name, zos);
                addStructure(MessageFormat.format(LIB_STRUCTURE_FORMAT, name), zos);
                ZipUtil.addFileToZip(zos, jarFile, MessageFormat.format(LIB_PATH_WITH_EXT_FORMAT, name, name, JAR_EXTENSION), new HashSet<String>(), new FileFilter() {

                    public boolean accept(File pathname) {
                        if (progressIndicator != null) {
                            progressIndicator.setText2("");
                        }
                        return !myFileTypeManager.isFileIgnored(FileUtil.toSystemIndependentName(pathname.getPath()));
                    }
                });
                if (srcZipFile != null) {
                    addStructure(MessageFormat.format(SRC_STRUCTURE_FORMAT, name), zos);
                    ZipUtil.addFileToZip(zos, srcZipFile, MessageFormat.format(SRC_DIR_FORMAT, name, sourcesName), new HashSet<String>(), new FileFilter() {

                        public boolean accept(File pathname) {
                            if (progressIndicator != null) {
                                progressIndicator.setText2(PluginPackerBundle.message("adding.sources"));
                            }
                            return !myFileTypeManager.isFileIgnored(FileUtil.toSystemIndependentName(pathname.getPath()));
                        }
                    });
                }
                Set<String> names = new HashSet<String>();
                for (Library library : libs) {
                    final VirtualFile[] files = library.getFiles(OrderRootType.CLASSES);
                    Set<File> excludedRoots = new HashSet<File>();
                    excludedRoots.add(new File(PathManager.getLibPath()));
                    excludedRoots.add(new File(PathManager.getPluginsPath()));
                    excludedRoots.add(new File(PathManager.getPreinstalledPluginsPath()));
                    for (VirtualFile virtualFile : files) {
                        boolean excluded = false;
                        for (File excludedFile : excludedRoots) {
                            excluded |= VfsUtil.isAncestor(excludedFile, VfsUtil.virtualToIoFile(virtualFile), false);
                        }
                        if (!excluded) {
                            if (virtualFile.getFileSystem() instanceof JarFileSystem) {
                                addLibraryJar(virtualFile, zipFile, name, zos, progressIndicator);
                            } else {
                                makeAndAddLibraryJar(virtualFile, zos, zipFile, name, library, names, progressIndicator);
                            }
                        }
                    }
                }
            } finally {
                if (zos != null) {
                    zos.close();
                }
            }
        }
    }

    private void makeAndAddLibraryJar(final VirtualFile virtualFile, final ZipOutputStream zos, final File zipFile, final String name, final Library library, final Set<String> names, final ProgressIndicator progressIndicator) throws IOException {
        File libraryJar = createTempFile(JAR_EXTENSION);
        ZipOutputStream jar = null;
        try {
            jar = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(libraryJar)));
            ZipUtil.addFileOrDirRecursively(jar, libraryJar, VfsUtil.virtualToIoFile(virtualFile), "", new FileFilter() {

                public boolean accept(File pathname) {
                    if (progressIndicator != null) {
                        progressIndicator.setText2(PluginPackerBundle.message("adding.0", pathname));
                    }
                    return !myFileTypeManager.isFileIgnored(FileUtil.toSystemIndependentName(pathname.getPath()));
                }
            }, new HashSet<String>());
        } finally {
            if (jar != null) {
                jar.close();
            }
        }
        ZipUtil.addFileOrDirRecursively(zos, zipFile, libraryJar, MessageFormat.format(LIB_PATH_WITH_EXT_FORMAT, name, getLibraryJarName(library, names, virtualFile), JAR_EXTENSION), new FileFilter() {

            public boolean accept(File pathname) {
                if (progressIndicator != null) {
                    progressIndicator.setText2("");
                }
                return true;
            }
        }, new HashSet<String>());
    }

    private String getLibraryJarName(Library library, Set<String> names, final VirtualFile virtualFile) {
        final String name = library.getName();
        if (name != null && !names.contains(name)) {
            return name;
        }
        String libraryName = virtualFile.getName();
        for (int i = 1; names.contains(libraryName); i++) {
            libraryName = libraryName.concat(String.valueOf(i));
        }
        names.add(libraryName);
        return libraryName;
    }

    private void addLibraryJar(final VirtualFile virtualFile, final File zipFile, final String name, final ZipOutputStream zos, final ProgressIndicator progressIndicator) throws IOException {
        File ioFile = VfsUtil.virtualToIoFile(virtualFile);
        final FileFilter filter = new FileFilter() {

            public boolean accept(File pathname) {
                if (progressIndicator != null) {
                    progressIndicator.setText2("");
                }
                return true;
            }
        };
        ZipUtil.addFileOrDirRecursively(zos, zipFile, ioFile, MessageFormat.format(LIB_PATH_DIR_FORMAT, name, ioFile.getName()), filter, null);
    }

    private void addStructure(@NonNls final String relativePath, final ZipOutputStream zos) throws IOException {
        ZipEntry e = new ZipEntry(relativePath + "/");
        e.setMethod(ZipEntry.STORED);
        e.setSize(0);
        e.setCrc(0);
        zos.putNextEntry(e);
        zos.closeEntry();
    }

    /**
     * Jar plugin module
     *
     * @param module  Module
     * @param modules Depended modules
     * @return File of jar (in temp directory)
     * @throws java.io.IOException is IO oprations failed
     */
    @NotNull
    private File createTempPluginJar(Module module, final Set<Module> modules) throws IOException {
        final PluginBuildConfiguration buildConfiguration = PluginBuildConfiguration.getInstance(module);
        if (buildConfiguration != null) {
            final File jarFile = createTempFile(JAR_EXTENSION);
            final Manifest manifest = PrepareToDeployAction.createOrFindManifest(buildConfiguration);
            ZipOutputStream jarPlugin = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(jarFile)), manifest);
            try {
                final ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
                final HashSet<String> writtenItemRelativePaths = new HashSet<String>();
                for (Module dependence : modules) {
                    final VirtualFile compilerOutputPath = CompilerPaths.getModuleOutputDirectory(dependence, false);
                    if (compilerOutputPath != null) {
                        ZipUtil.addDirToZipRecursively(jarPlugin, jarFile, new File(compilerOutputPath.getPath()), "", new FileFilter() {

                            public boolean accept(File pathname) {
                                if (progressIndicator != null) {
                                    progressIndicator.setText2("");
                                }
                                return !myFileTypeManager.isFileIgnored(FileUtil.toSystemIndependentName(pathname.getName()));
                            }
                        }, writtenItemRelativePaths);
                    }
                }
                final String pluginXmlPath = buildConfiguration.getPluginXmlPath();
                ZipUtil.addFileToZip(jarPlugin, new File(pluginXmlPath), PLUGIN_XML_PATH, writtenItemRelativePaths, new FileFilter() {

                    public boolean accept(File pathname) {
                        if (progressIndicator != null) {
                            progressIndicator.setText2("");
                        }
                        return true;
                    }
                });
            } finally {
                jarPlugin.close();
            }
            return jarFile;
        }
        throw new IllegalArgumentException(MessageFormat.format(PluginPackerBundle.message("can.t.find.build.configuration.for.module.0"), module.getName()));
    }

    @NotNull
    private File createTempSourcesZip(final Set<Module> modules) throws IOException {
        File zipFile = createTempFile(ZIP_EXTENSION);
        zipSources(zipFile, modules);
        return zipFile;
    }

    private void zipSources(File zipFile, Set<Module> modules) throws IOException {
        ZipOutputStream zip = null;
        try {
            zip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
            final ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
            final HashSet<String> writtenItemRelativePaths = new HashSet<String>();
            for (Module dependence : modules) {
                final ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(dependence);
                final VirtualFile[] roots = moduleRootManager.getContentRoots();
                for (VirtualFile root : roots) {
                    ZipUtil.addDirToZipRecursively(zip, zipFile, new File(root.getPath()), "", new FileFilter() {

                        public boolean accept(File pathname) {
                            if (progressIndicator != null) {
                                progressIndicator.setText2("");
                            }
                            VirtualFile[] files = moduleRootManager.getExcludeRoots();
                            for (VirtualFile excluded : files) {
                                if (VfsUtil.isAncestor(VfsUtil.virtualToIoFile(excluded), pathname, false)) {
                                    return false;
                                }
                            }
                            return !myFileTypeManager.isFileIgnored(FileUtil.toSystemIndependentName(pathname.getName()));
                        }
                    }, writtenItemRelativePaths);
                }
            }
        } finally {
            if (zip != null) {
                zip.close();
            }
        }
    }
}
