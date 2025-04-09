package hu.sztaki.lpds.pgportal.services.timer.events;

import hu.sztaki.lpds.pgportal.services.timer.BaseEvent;
import hu.sztaki.lpds.pgportal.services.utils.PropertyLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CatalinaDeleteEvent implements BaseEvent {

    public void execute() {
        try {
            System.out.println("Catalina Delete Event Start...");
            String tomcatpath = new String(PropertyLoader.getInstance().getProperty("tomcat.absolute.path") + "logs/");
            String catalinaout = new String(tomcatpath + "catalina.out");
            DateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
            String stamp = formatter.format(new Date());
            String zipcatalinaout = new String(tomcatpath + "catalina_out_" + stamp + ".zip");
            System.out.println("tomcat path: " + tomcatpath);
            System.out.println("catalina out: " + catalinaout);
            System.out.println("stamp: " + stamp);
            System.out.println("zip file name: " + zipcatalinaout);
            if ((new File(catalinaout).exists())) {
                compressOldCatalinaOutFile(tomcatpath, catalinaout, zipcatalinaout);
                clearCatalinaOutFile(catalinaout);
            }
            System.out.println("Catalina Delete Event End...");
        } catch (Exception e) {
            System.out.println("CatalinaDeleteEvent.execute()");
            e.printStackTrace();
        }
    }

    private void compressOldCatalinaOutFile(String tomcatpath, String outfilename, String zipfilename) {
        try {
            File out = new File(outfilename);
            File zip = new File(zipfilename);
            if (zip.exists()) {
                zip.delete();
            }
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipfilename));
            zos.setLevel(Deflater.DEFAULT_COMPRESSION);
            addToZip(tomcatpath, out, zos);
            zos.close();
        } catch (Exception e) {
            System.out.println("CatalinaDeleteEvent.compressOldCatalinaOutFile()");
            e.printStackTrace();
        }
    }

    private boolean addToZip(String addFileFromThisDir, File addThis, ZipOutputStream zos) {
        String entryPath;
        String addThisPath = addThis.getAbsolutePath();
        if (!addThisPath.startsWith(addFileFromThisDir)) {
            System.out.println("CatalinaDeleteEvent.addToZip() --- parameter error !");
            return false;
        } else {
            entryPath = addThisPath.substring(addFileFromThisDir.length());
        }
        try {
            zos.putNextEntry(new ZipEntry(entryPath));
            FileInputStream input = new FileInputStream(addThis);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
            zos.closeEntry();
            return true;
        } catch (Exception e) {
            System.out.println("CatalinaDeleteEvent.addToZip()");
            e.printStackTrace();
            return false;
        }
    }

    private void clearCatalinaOutFile(String outfilename) {
        try {
            File file = new File(outfilename);
            FileWriter fw = new FileWriter(file);
            fw.write("Catalina Delete Event: clear catalina.out file...\n");
            fw.close();
        } catch (Exception e) {
            System.out.println("CatalinaDeleteEvent.clearCatalinaOutFile()");
            e.printStackTrace();
        }
    }
}
