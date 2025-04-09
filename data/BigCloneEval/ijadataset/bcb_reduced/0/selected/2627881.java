package org.eclipse.babel.build.core.languagepack;

import java.io.File;
import org.eclipse.babel.build.core.Configuration;
import org.eclipse.babel.build.core.Messages;
import org.eclipse.babel.build.core.PluginProxy;
import org.eclipse.babel.build.core.coverage.LanguagePackCoverageReport;
import org.eclipse.babel.build.core.eclipsetarget.EclipseTarget;
import org.eclipse.babel.build.core.exceptions.FailedDeletionException;
import org.eclipse.babel.build.core.exceptions.InvalidFilenameException;

public class LanguagePack {

    private Configuration config;

    private File absoluteWorkDirectory;

    public LanguagePack(Configuration config) {
        this.config = config;
    }

    /** Generates a language pack on disk and returns a report with coverage information. */
    public LanguagePackCoverageReport generate() throws InvalidFilenameException, FailedDeletionException, Exception {
        NLFragment fragment;
        File parentFragmentDirectory;
        EclipseTarget eclipseInstall = config.eclipseInstall();
        LanguagePackCoverageReport coverage;
        this.absoluteWorkDirectory = this.config.workingDirectory().getAbsoluteFile();
        setupLanguagePackLocation();
        coverage = new LanguagePackCoverageReport(config.translations().getAllLocales());
        for (PluginProxy plugin : eclipseInstall.getPlugins().values()) {
            String versionSuffix = "";
            if (!plugin.getVersion().equalsIgnoreCase("")) {
                versionSuffix = Messages.getString("Characters_underscore") + plugin.getVersion();
            }
            parentFragmentDirectory = new File(this.absoluteWorkDirectory.getAbsoluteFile() + File.separator + Messages.getString("Paths_plugins_directory") + plugin.getName() + Messages.getString("Extensions_nl") + config.localeExtension() + versionSuffix);
            config.notifyProgress(plugin.getName());
            fragment = new JarNLFragment(parentFragmentDirectory, plugin, eclipseInstall);
            coverage.addPluginCoverageToReport(fragment.generateFragment(this.config));
        }
        for (PluginProxy feature : eclipseInstall.getFeatures().values()) {
            String versionSuffix = "";
            if (!feature.getVersion().equalsIgnoreCase("")) {
                versionSuffix = Messages.getString("Characters_underscore") + feature.getVersion();
            }
            parentFragmentDirectory = new File(this.absoluteWorkDirectory.getAbsoluteFile() + File.separator + Messages.getString("Paths_features_directory") + feature.getName() + Messages.getString("Extensions_nl") + config.localeExtension() + versionSuffix);
            config.notifyProgress(feature.getName());
            if (!feature.isJar()) {
                fragment = new NonJarNLFragment(parentFragmentDirectory, feature);
            } else {
                fragment = new JarNLFragment(parentFragmentDirectory, feature, eclipseInstall);
            }
            coverage.addPluginCoverageToReport(fragment.generateFragment(this.config));
        }
        return coverage;
    }

    private void setupLanguagePackLocation() throws InvalidFilenameException, FailedDeletionException {
        if (this.absoluteWorkDirectory.exists()) {
            try {
                File eclipseFolder = new File(this.absoluteWorkDirectory, Messages.getString("Paths_eclipse_directory_name"));
                if (eclipseFolder.exists()) {
                    for (File file : eclipseFolder.listFiles()) {
                        deleteDirectory(file);
                    }
                }
            } catch (Exception e) {
                throw new FailedDeletionException();
            }
        } else {
            this.absoluteWorkDirectory.mkdirs();
        }
        generateBaseFolders();
    }

    private void generateBaseFolders() {
        File newDirectory = new File(this.absoluteWorkDirectory + File.separator + Messages.getString("Paths_plugins_directory"));
        newDirectory.mkdirs();
        newDirectory = new File(this.absoluteWorkDirectory + File.separator + Messages.getString("Paths_features_directory"));
        newDirectory.mkdirs();
    }

    private boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }
}
