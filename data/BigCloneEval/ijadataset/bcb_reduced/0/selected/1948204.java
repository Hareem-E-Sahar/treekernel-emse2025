package net.sourceforge.liftoff.installer.items;

import java.io.*;
import net.sourceforge.liftoff.installer.*;
import net.sourceforge.liftoff.installer.zip.*;
import java.util.Enumeration;

/**
 * This is an installable for the installer itself.<p>
 * 
 * All files that belong to the package 'installer' will be written
 * into a zip file that can be used later as archive for the uninstall app.
 */
public class InstallerLib extends FSInstallable {

    private InstallableContainer container;

    public InstallerLib(InstallableContainer container, String location) {
        super("installer_lib", null, location, "Uninstall.class", 0);
        this.container = container;
    }

    public boolean install(InstallMonitor moni) throws AbortInstallException {
        installedName = Info.getSystemActions().getTargetName(location, target);
        backupFile(moni, installedName);
        OutputStream os = Info.getSystemActions().openOutputFile(location, target);
        if (os == null) return false;
        long bytes_written = 0;
        try {
            InputStream is = getClass().getResourceAsStream("/Uninstall.class");
            if (is != null) {
                byte[] buffer = new byte[4096];
                int bytes = 0;
                while ((bytes = is.read(buffer)) >= 0) {
                    os.write(buffer, 0, bytes);
                    bytes_written += bytes;
                }
            }
        } catch (Exception e) {
            System.err.println("can not write installer zip : " + e);
            System.err.println("last file was Uninstall.class");
            e.printStackTrace();
            return false;
        }
        ZipOutputStream zips = new ZipOutputStream(os, bytes_written);
        zips.setLevel(9);
        InstallableFile insf = null;
        long count = 0;
        try {
            Enumeration instEn = container.getAllInstallables();
            while (instEn.hasMoreElements()) {
                Installable inst = (Installable) (instEn.nextElement());
                if (!(inst instanceof InstallableFile)) continue;
                insf = (InstallableFile) inst;
                if (!insf.getPackage().equals("installer")) continue;
                InputStream is = Info.getInstallationSource().getFile(insf.getName(), insf.getLocation());
                if (is == null) {
                    System.err.println("can not get installer component " + insf.getName());
                    return false;
                }
                ZipEntry ze = new ZipEntry(insf.getName());
                zips.putNextEntry(ze);
                byte[] buffer = new byte[4096];
                int bytes = 0;
                count = 0;
                try {
                    while ((bytes = is.read(buffer)) >= 0) {
                        count += bytes;
                        zips.write(buffer, 0, bytes);
                    }
                } catch (EOFException e) {
                }
                is.close();
            }
            wasInstalled = true;
            ZipEntry ze = new ZipEntry("uninstall.dat");
            zips.putNextEntry(ze);
            Info.saveState(zips);
            zips.finish();
        } catch (Exception e) {
            System.err.println("can not write installer zip : " + e);
            System.err.println("last file was " + insf);
            System.err.println("got " + count + " bytes from that file\n");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Returns true if this Installable was modified.<p>
     *
     * We need to lie here because of an Henn-And-Egg problem whith the
     * hash for the UninstallInfo ...
     *
     * @return false.
     */
    public boolean wasModified() {
        return false;
    }

    public boolean needInstall() {
        return true;
    }
}
