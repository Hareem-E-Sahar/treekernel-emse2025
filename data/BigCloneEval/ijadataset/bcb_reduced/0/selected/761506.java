package sirf.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;

public class ComprimirZip {

    @SuppressWarnings("unused")
    private ZipEntry getEntry(String pathname) {
        return getEntry(pathname, "");
    }

    private ZipEntry getEntry(String pathname, String dirName) {
        File f = new File(pathname);
        ZipEntry ze = new ZipEntry(dirName + f.getName());
        ze.setMethod(ZipEntry.DEFLATED);
        ze.setSize(f.length());
        ze.setTime(f.lastModified());
        return ze;
    }

    private String getDirSubSistema(String pathname) {
        if (pathname.contains("yacimiento") && !pathname.contains("bibliografia")) {
            return "yacimiento/";
        } else if (pathname.contains("bibliografia")) {
            return "yacimiento/bibliografia/";
        } else if (pathname.contains("estructura")) {
            return "estructura/";
        }
        return "";
    }

    public void createZip(List<String> listado, String zip_filename) {
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(zip_filename);
            zos = new ZipOutputStream(fos);
            zos.setLevel(9);
            Logger.getLogger("sirf.actions").debug(listado);
            Set<String> listadoSet = new HashSet<String>(listado);
            for (String pathname : listadoSet) {
                Logger.getLogger("sirf.actions").debug(pathname);
                FileInputStream fis = new FileInputStream(pathname);
                byte[] content_file = new byte[1024];
                ZipEntry ze = this.getEntry(pathname, getDirSubSistema(pathname));
                zos.putNextEntry(ze);
                while (fis.read(content_file, 0, 1024) != -1) {
                    zos.write(content_file);
                }
                zos.closeEntry();
                fis.close();
            }
            zos.flush();
            zos.close();
        } catch (FileNotFoundException e) {
            throw new ComprimirException("Fichero no encontrado: " + e.getMessage());
        } catch (IOException e) {
            throw new ComprimirException("Error de entrada/salida. El fichero puede estar en uso.");
        }
    }
}
