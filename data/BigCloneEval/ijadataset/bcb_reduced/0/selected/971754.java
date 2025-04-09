package com.inetmon.jn.ui.actions;

import java.io.File;
import java.io.IOException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.update.configuration.IConfiguredSite;
import org.eclipse.update.configuration.ILocalSite;
import org.eclipse.update.core.IFeatureReference;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.standalone.UninstallCommand;

public class DeleteUncofiguredFeatures {

    String path = "";

    public DeleteUncofiguredFeatures() {
        check();
    }

    private void check() {
        IProgressMonitor monitor = new NullProgressMonitor();
        try {
            ILocalSite ls = SiteManager.getLocalSite();
            IConfiguredSite ics = ls.getCurrentConfiguration().getConfiguredSites()[0];
            if (System.getProperty("os.name").contains("Win")) {
                path = System.getProperty("user.dir") + "\\";
            } else if (System.getProperty("os.name").contains("Mac") || System.getProperty("os.name").contains("Lin")) {
                path = ics.toString().split(":")[1];
            } else {
            }
            System.out.println("testing path : " + path);
            IFeatureReference[] lfrs = ics.getFeatureReferences();
            System.out.println("lfrs lenght is : " + lfrs.length);
            for (int j = 0; j < lfrs.length; j++) {
                try {
                    UninstallCommand unCom = new UninstallCommand(lfrs[j].getFeature(monitor).getVersionedIdentifier().getIdentifier(), lfrs[j].getFeature(monitor).getVersionedIdentifier().getVersion().toString(), ls.toString(), "false");
                    unCom.run(monitor);
                    String fileName = path + "features/" + lfrs[j].getFeature(monitor).getVersionedIdentifier().toString();
                    System.out.println("about to delete feature : " + fileName);
                    deleteFeaturePlugin(fileName);
                    fileName = path + "plugins/" + lfrs[j].getFeature(monitor).getVersionedIdentifier().toString();
                    System.out.println("about to delete plugin : " + fileName);
                    deleteFeaturePlugin(fileName);
                } catch (Exception e) {
                    System.out.println("error here");
                }
            }
        } catch (Exception e) {
        }
    }

    public static void recursivelyDeleteDirectory(File dir) throws IOException {
        if ((dir == null) || !dir.isDirectory()) throw new IllegalArgumentException(dir + " not a directory");
        final File[] files = dir.listFiles();
        final int size = files.length;
        for (int i = 0; i < size; i++) {
            if (files[i].isDirectory()) {
                recursivelyDeleteDirectory(files[i]);
            } else {
                if (files[i].delete()) System.out.println(files[i].getPath() + " file was deleted"); else System.out.println(files[i].getPath() + " file cant be deleted");
            }
        }
        dir.delete();
    }

    private void deleteFeaturePlugin(String fileName) {
        try {
            File f = new File(fileName);
            if (f.exists() && f.isDirectory()) recursivelyDeleteDirectory(f); else {
                f = new File(fileName + ".jar");
                f.delete();
            }
        } catch (Exception e) {
        }
    }
}
