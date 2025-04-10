package modmanager.utility.update;

import modmanager.business.Mod;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;
import modmanager.controller.Manager;
import modmanager.utility.FileUtils;
import modmanager.exceptions.UpdateModException;

/**
 *
 * @author Shirkit
 */
public class UpdateThread implements Callable<UpdateThread> {

    Mod mod;

    File file;

    public UpdateThread(Mod mod) {
        this.mod = mod;
        this.file = null;
    }

    private void work(int timeout) throws Exception {
        Thread.currentThread().setName("Update - " + mod.getName());
        if (mod.getUpdateCheckUrl() != null && mod.getUpdateDownloadUrl() != null) {
            URL url = new URL(mod.getUpdateCheckUrl().trim());
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String str = in.readLine();
            in.close();
            if (str != null && !str.toLowerCase().trim().contains("error") && !str.toLowerCase().trim().contains("Error") && !Manager.getInstance().compareModsVersions(str, "*-" + mod.getVersion())) {
                InputStream is = new URL(mod.getUpdateDownloadUrl().trim()).openStream();
                file = new File(System.getProperty("java.io.tmpdir") + File.separator + new File(mod.getPath()).getName());
                FileOutputStream fos = new FileOutputStream(file, false);
                FileUtils.copyInputStream(is, fos);
                is.close();
                fos.flush();
                fos.close();
            }
        }
    }

    public UpdateThread call() throws UpdateModException {
        Exception e = null;
        for (int timeout = 3000; timeout < 10000; timeout += 2000) {
            if (file == null) {
                try {
                    work(timeout);
                    return this;
                } catch (Exception ex) {
                    e = ex;
                    file = null;
                }
            }
        }
        throw new UpdateModException(mod, e);
    }

    public Mod getMod() {
        return mod;
    }

    public File getFile() {
        return file;
    }
}
