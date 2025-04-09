package saadadb.util.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import saadadb.database.Repository;
import saadadb.util.Messenger;

/**
 * @author laurent
 * @version $Id: ZIPUtil.java 118 2012-01-06 14:33:51Z laurent.mistahl $
 */
public class ZIPUtil {

    public static final int MAX_SIZE = 300;

    public static final void buildZipBall(ZipMap zipMap, String outputFilepath) throws Exception {
        String outFilename = outputFilepath;
        if (!outFilename.endsWith(".zip")) {
            outFilename += ".zip";
        }
        Messenger.printMsg(Messenger.TRACE, "Build ZIP file <" + outputFilepath + ">");
        ZipOutputStream out;
        out = new ZipOutputStream(new FileOutputStream(outFilename));
        long file_size = 0;
        boolean full = false;
        for (String dir : zipMap.keySet()) {
            Set<ZipEntryRef> zers = zipMap.get(dir);
            TreeSet<String> storedRef = new TreeSet<String>();
            for (ZipEntryRef zer : zers) {
                String fileitem = zer.getUri();
                byte[] buf = new byte[1024];
                FileInputStream in;
                File f;
                if ((f = new File(fileitem)).exists()) {
                    in = new FileInputStream(fileitem);
                    file_size += f.length() / 1000000;
                } else if ((f = new File(fileitem + ".gz")).exists()) {
                    in = new FileInputStream(fileitem + ".gz");
                    file_size += f.length() / 1000000;
                } else {
                    Messenger.printMsg(Messenger.ERROR, "File <" + fileitem + "> or <" + fileitem + ".gz> not found");
                    continue;
                }
                Messenger.printMsg(Messenger.TRACE, "Compress " + fileitem);
                String str = dir + "/" + zer.getName();
                ZipEntry ze = new ZipEntry(str);
                if (!storedRef.contains(str)) {
                    out.putNextEntry(ze);
                    storedRef.add(str);
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.closeEntry();
                }
                if (file_size > MAX_SIZE) {
                    String msg = "The size of the zipped file exceed " + MAX_SIZE + "Mb: zipball truncated";
                    Messenger.printMsg(Messenger.WARNING, msg);
                    full = true;
                    Messenger.printMsg(Messenger.TRACE, "Compress truncated.txt");
                    ze = new ZipEntry("truncated.txt");
                    out.putNextEntry(ze);
                    out.write(msg.getBytes(), 0, msg.length());
                    out.closeEntry();
                    break;
                }
            }
            if (full) {
                break;
            }
        }
        out.closeEntry();
        out.close();
    }

    /**
	 * Gunzip input file if its name ends with .gz
	 * @param input_file
	 * @return : the path of the uncompressed file
	 * @throws Exception
	 */
    public static String gunzip(String input_file) throws Exception {
        if (input_file.toLowerCase().endsWith(".gz")) {
            if (Messenger.debug_mode) Messenger.printMsg(Messenger.DEBUG, "Gunzip " + input_file);
            InputStream in = new FileInputStream(input_file);
            String outputfile = input_file.substring(0, input_file.length() - 3);
            OutputStream out = new FileOutputStream(outputfile);
            byte[] buffer = new byte[8192];
            in = new GZIPInputStream(in, buffer.length);
            int count = in.read(buffer);
            while (count > 0) {
                out.write(buffer, 0, count);
                count = in.read(buffer);
            }
            in.close();
            out.close();
            return outputfile;
        } else {
            return input_file;
        }
    }
}
