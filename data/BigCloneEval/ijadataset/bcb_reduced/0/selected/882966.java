package kfschmidt.quickvol.io;

import java.util.zip.*;
import java.io.*;
import kfschmidt.quickvol.Project;

public class ProjectFileWriter {

    public static void writeProjectToFile(Project p, File proj_file) throws Exception {
        ZipOutputStream zout = openZipArchive(proj_file);
        String xml = SerializationHelper.getXMLForProject(p);
        writeXMLToZipArchive("quickvol_project.xml", xml, zout);
        if (p.getStack().getLayers() != null) {
            for (int a = 0; a < p.getStack().getLayers().length; a++) {
                String fname = p.getStack().getLayers()[a].getName();
                String entryname = null;
                if (fname.toLowerCase().endsWith(".png")) entryname = fname; else {
                    entryname = fname + ".png";
                }
                ZipEntry entry = new ZipEntry(entryname);
                zout.putNextEntry(entry);
                javax.imageio.ImageIO.write(p.getStack().getLayers()[a].getOrigImage(), "png", zout);
            }
        }
        zout.close();
    }

    private static ZipOutputStream openZipArchive(File proj_file) throws Exception {
        return new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(proj_file)));
    }

    private static void writeXMLToZipArchive(String filename, String xml, ZipOutputStream zout) throws Exception {
        ZipEntry entry = new ZipEntry(filename);
        zout.putNextEntry(entry);
        zout.write(xml.getBytes());
    }

    /**
     *    METHOD IS NOT TESTED
     */
    private static void addFileToZipArchive(String name, File f, ZipOutputStream zout) throws Exception {
        byte data[] = new byte[2048];
        FileInputStream fi = new FileInputStream(f);
        BufferedInputStream origin = new BufferedInputStream(fi, 2048);
        ZipEntry entry = new ZipEntry(name);
        zout.putNextEntry(entry);
        int count = 0;
        while ((count = origin.read(data, 0, 2048)) != -1) {
            zout.write(data, 0, count);
        }
        origin.close();
    }
}
