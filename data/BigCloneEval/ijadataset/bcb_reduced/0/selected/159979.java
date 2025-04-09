package de.sooja.framework.update;

import java.io.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class InstallFeatureJob extends Job {

    private static final IStatus OK_STATUS = new Status(IStatus.OK, UpdateScheduler.getPluginId(), IStatus.OK, "Feature Installed!", null);

    String updateSite;

    String featureId;

    String featureVersion;

    public IStatus run(IProgressMonitor monitor) {
        try {
            Runtime rt = Runtime.getRuntime();
            java.lang.Process prcs = rt.exec("java -cp startup.jar org.eclipse.core.launcher.Main -application org.eclipse.update.core.standaloneUpdate -command install -from " + updateSite + " -featureId " + featureId + " -version " + featureVersion + " -to ./ -verifyOnly false");
            try {
                prcs.waitFor();
            } catch (InterruptedException e) {
                System.out.println(e);
            }
            copyDirectory(new File("./eclipse/"), new File("./"));
            deleteDir(new File("./eclipse/"));
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
        return OK_STATUS;
    }

    public InstallFeatureJob(String updateSite, String featureId, String featureVersion) {
        super("InstallFeatureJob");
        this.updateSite = updateSite;
        this.featureId = featureId;
        this.featureVersion = featureVersion;
        setPriority(Job.DECORATE);
    }

    public void copyDirectory(File srcDir, File dstDir) throws IOException {
        if (srcDir.isDirectory()) {
            if (!dstDir.exists()) {
                dstDir.mkdir();
            }
            String[] children = srcDir.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(srcDir, children[i]), new File(dstDir, children[i]));
            }
        } else {
            copyFile(srcDir, dstDir);
        }
    }

    void copyFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
}
