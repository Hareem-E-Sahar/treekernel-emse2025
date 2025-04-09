package octopus.tools.Util;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import octopus.OctopusApplication;

public class Zip {

    public File dir;

    public String parentName;

    /**
     */
    public static int zipIt(String folder_that_contains_the_files) {
        if (folder_that_contains_the_files == null) return 0;
        try {
            String path_zip_file = OctopusApplication.PATH_EXPORT_FOLDER + folder_that_contains_the_files + ".zip";
            folder_that_contains_the_files = OctopusApplication.PATH_EXPORT_FOLDER + folder_that_contains_the_files;
            File zipFile = new File(path_zip_file);
            if (zipFile.exists()) {
                return 0;
            }
            ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
            File file = new File(folder_that_contains_the_files);
            if (file.isFile()) {
                addTargetFile(zos, file);
            } else if (file.isDirectory()) {
                ArrayList names = pathNames(file);
                Iterator it = names.iterator();
                while (it.hasNext()) {
                    File f = new File((String) it.next());
                    addTargetFile(zos, f);
                }
            }
            zos.close();
            return 1;
        } catch (FileNotFoundException e) {
            return 0;
        } catch (ZipException e) {
            return 0;
        } catch (IOException e) {
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private static ArrayList pathNames(File dir) throws FileNotFoundException, IOException {
        ArrayList names = new ArrayList();
        try {
            String parentName = dir.getPath();
            String[] list = dir.list();
            File[] files = new File[list.length];
            for (int i = 0; i < list.length; i++) {
                String pathName = parentName + File.separator + list[i];
                files[i] = new File(pathName);
                if (files[i].isFile()) names.add(files[i].getPath());
            }
            for (int i = 0; i < list.length; i++) {
                if (files[i].isDirectory()) {
                    String name = parentName + File.separator + list[i];
                    ArrayList sub = pathNames(files[i]);
                    Iterator it = sub.iterator();
                    while (it.hasNext()) names.add(it.next());
                }
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            throw e;
        }
        return names;
    }

    private static void addTargetFile(ZipOutputStream zos, File file) throws FileNotFoundException, ZipException, IOException {
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            String file_path = file.getPath();
            if (file_path.startsWith(OctopusApplication.PATH_EXPORT_FOLDER)) {
                file_path = file_path.substring(OctopusApplication.PATH_EXPORT_FOLDER.length(), file_path.length());
            }
            ZipEntry target = new ZipEntry(file_path);
            zos.putNextEntry(target);
            int c;
            while ((c = bis.read()) != -1) {
                zos.write((byte) c);
            }
            bis.close();
            zos.closeEntry();
        } catch (FileNotFoundException e) {
            throw e;
        } catch (ZipException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }
    }
}
