package pt.utl.ist.lucene.treceval.geotime.utiltasks;

import jomm.utils.StreamsUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Jorge
 * Date: 5/Jul/2011
 * Time: 12:46:48
 * To change this template use File | Settings | File Templates.
 */
public class ZipXmls {

    public static void main(String[] args) throws IOException {
        File[] files = new File("D:\\Servidores\\DATA\\ntcir\\PlaceMaker").listFiles();
        for (File f : files) {
            if (f.getName().endsWith(".xml")) {
                System.out.println("Zipping " + f.getName());
                ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(new File(f.getParentFile().getAbsolutePath() + "/" + f.getName().substring(0, f.getName().lastIndexOf(".")) + ".zip")));
                ZipEntry ze = new ZipEntry(f.getName());
                zout.putNextEntry(ze);
                FileInputStream fs = new FileInputStream(f);
                StreamsUtils.inputStream2OutputStream(fs, zout);
                fs.close();
                zout.flush();
                zout.close();
            }
        }
    }
}
