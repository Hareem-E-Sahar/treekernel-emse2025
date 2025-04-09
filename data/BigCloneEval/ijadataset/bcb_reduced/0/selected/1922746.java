package hu.sztaki.lpds.pgportal.services.utils.compress;

import java.io.IOException;
import java.io.*;
import java.util.zip.*;
import java.util.Enumeration;
import java.util.Vector;
import hu.sztaki.lpds.pgportal.services.utils.PropertyLoader;
import hu.sztaki.lpds.pgportal.services.utils.directoryUtil;

/**
 * @author  lpds
 */
public class libJavaZIP implements compressBase {

    static final int BUFFER = 2048;

    public libJavaZIP() {
    }

    public void decompressWorkflow(String file, String prefix) {
        System.out.println("******************-" + file);
        try {
            BufferedOutputStream dest = null;
            BufferedInputStream is = null;
            ZipEntry entry;
            ZipFile zipfile = new ZipFile(file);
            Enumeration e = zipfile.entries();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                System.out.println("Extracting: " + entry.getName());
                is = new BufferedInputStream(zipfile.getInputStream(entry));
                int count;
                byte data[] = new byte[BUFFER];
                String[] path_element = null;
                path_element = entry.getName().replaceAll("\\\\", "/").split("/");
                String path_part_dir = new String(prefix);
                for (int i = 0; i < path_element.length - 1; i++) {
                    path_part_dir += path_element[i] + "/";
                    File fpath_test = new File(path_part_dir);
                    if (!fpath_test.exists()) fpath_test.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(prefix + entry.getName().replaceAll("\\\\", "/"));
                dest = new BufferedOutputStream(fos, BUFFER);
                while ((count = is.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void compressWorkflow(Vector filenames, String output, String prefix) throws IOException {
        byte[] buf = new byte[1024];
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(output));
        for (int re = 0; re < filenames.size(); re++) {
            String s = (String) filenames.get(re);
            File f = new File((String) filenames.get(re));
            if (!f.isDirectory()) {
                System.out.println("FILE:**********" + s);
                FileInputStream in = new FileInputStream(s);
                out.putNextEntry(new ZipEntry(((String) filenames.get(re)).substring(prefix.length())));
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.closeEntry();
                in.close();
            } else {
                System.out.println("DIR:**********" + s);
            }
        }
        out.close();
    }

    public void compressWorkflow(String input, String output) {
        directoryUtil dru = new directoryUtil();
        try {
            dru.dirList(new File(input));
            String[] st = input.split("/");
            String s = "";
            for (int i = 0; i < st.length - 1; i++) s += st[i] + "/";
            compressWorkflow(dru.getDirList(), output, s);
        } catch (IOException e) {
            System.out.println("nincs meg a file:" + input);
            e.printStackTrace();
        }
    }

    public void compressWorkflowWin(String input, String output) {
        directoryUtil dru = new directoryUtil();
        try {
            dru.dirList(new File(input));
            String[] st = input.split("\\");
            String s = "";
            for (int i = 0; i < st.length - 1; i++) s += st[i] + "\\";
            compressWorkflow(dru.getDirList(), output, s);
        } catch (IOException e) {
            System.out.println("nincs meg a file:" + input);
            e.printStackTrace();
        }
    }
}
