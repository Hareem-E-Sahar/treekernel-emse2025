package com.belmont.backup.server;

import java.io.*;
import com.marimba.intf.util.IConfig;
import com.marimba.intf.application.*;
import com.belmont.backup.*;

public class BackupServiceChannel implements IApplication, IBackupConstants {

    IApplicationContext context;

    IConfig tunerConfig;

    File dataDir;

    BackupService service;

    /**
     * remove a file or directory (recursively).
     */
    public boolean delete(final File f) {
        if (f.isDirectory()) {
            final String files[] = f.list();
            boolean ok = true;
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    ok &= Utils.delete(new File(f, files[i]));
                }
            }
            return f.delete() && ok;
        } else {
            return f.delete();
        }
    }

    /**
     * Copies data from one stream to another.
     */
    private void copy(InputStream in, OutputStream out) throws IOException {
        byte buf[] = new byte[4096];
        int n;
        while ((n = in.read(buf, 0, buf.length)) > 0) {
            out.write(buf, 0, n);
        }
    }

    /**
     * This method copies the channel files into the data directory.
     */
    private void copyChannelFiles(String dirName) throws IOException {
        String[] files = context.listChannelDirectory(dirName);
        for (int i = 0; i < files.length; i++) {
            String channelFileName = dirName + "/" + files[i];
            if (files[i].indexOf('.') == -1) {
                copyChannelFiles(channelFileName);
            } else {
                File dest = new File(dataDir, channelFileName);
                new File(dest.getParent()).mkdirs();
                InputStream in = context.getInputStream(channelFileName);
                try {
                    OutputStream out = new FileOutputStream(dest);
                    try {
                        copy(in, out);
                    } finally {
                        out.close();
                    }
                } finally {
                    in.close();
                }
            }
        }
    }

    public void notify(final Object sender, final int msg, final Object arg) {
        switch(msg) {
            case APP_INIT:
                System.out.println("APP_INIT");
                context = (IApplicationContext) arg;
                dataDir = new File(context.getDataDirectory());
                break;
            case APP_START:
                System.out.println("APP_START");
                try {
                    String host = context.getChannelURL().getHost();
                    if (!dataDir.exists()) {
                        dataDir.mkdirs();
                    } else if (new File(dataDir, "webapps").exists()) {
                        delete(new File(dataDir, "webapps"));
                    }
                    tunerConfig = (IConfig) context.getFeature("config");
                    Utils.installChannelDLLs(tunerConfig, context);
                    copyChannelFiles("webapps");
                    Config c = null;
                    if (!new File(dataDir, SERVER_CONFIG_FILE).exists()) {
                        c = new Config();
                        c.initFrom(context.getConfiguration());
                    }
                    service = new BackupService(dataDir, c);
                    service.start();
                } catch (IOException ex) {
                    System.out.println("Exception starting service");
                    ex.printStackTrace();
                    context.stop();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    context.stop();
                }
                break;
            case APP_ARGV:
                System.out.println("APP_ARGV");
                break;
            case APP_DATA_AVAILABLE:
                System.out.println("APP_DATA_AVAILABLE");
                break;
            case APP_DATA_NONE_AVAILABLE:
                break;
            case APP_DATA_INSTALLED:
                break;
            case APP_STOP:
                stop();
                break;
        }
    }

    public void stop() {
        if (service != null) {
            service.stop();
        }
    }
}
