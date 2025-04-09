package jmelib.codegen.project;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Dmitry Shyshkin
 *         Date: 3/4/2007 19:36:02
 */
public class JarModuleWriter implements ModuleWriter {

    private File jarFile;

    public JarModuleWriter(File jarFile) {
        this.jarFile = jarFile;
    }

    public void writeModule(Bundle module) throws IOException {
        ZipOutputStream stream = new ZipOutputStream(new FileOutputStream(jarFile));
        try {
            for (Bundle.Entry entry : module.getEntries()) {
                ZipEntry zipEntry = new ZipEntry(entry.getName());
                stream.putNextEntry(zipEntry);
                stream.write(entry.getData());
            }
        } finally {
            stream.close();
        }
    }
}
