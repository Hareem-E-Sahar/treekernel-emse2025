import java.lang.*;
import java.io.*;
import java.util.regex.*;
import java.text.*;
import java.util.*;

public class jun {

    public LinkedList<File> fileList = new LinkedList<File>();

    public Iterator listAllFiles(File inFile, String strType, boolean child) {
        try {
            File[] files = inFile.listFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    File robots = new File(f.getCanonicalPath() + File.separator + "robots.txt");
                    if (!robots.exists()) {
                        if (child) listAllFiles(f, strType, child);
                    }
                } else {
                    if (f.getCanonicalPath().indexOf("gather") == -1 && f.getCanonicalPath().indexOf(strType) != -1) {
                        fileList.addFirst(f);
                    }
                }
            }
            Collections.sort(fileList);
        } catch (Exception e) {
            System.out.println(e + " listAllFiles() \n");
        }
        Iterator fileIt = fileList.iterator();
        return fileIt;
    }

    public static float str2float(String str) {
        try {
            return Float.parseFloat(str.trim());
        } catch (Exception e) {
            return 999999999;
        }
    }

    public static double str2double(String str) {
        try {
            return Double.parseDouble(str.trim());
        } catch (Exception e) {
            return 999999999;
        }
    }

    public static int str2int(String str) {
        try {
            return Integer.parseInt(str.trim());
        } catch (Exception e) {
            return 999999999;
        }
    }

    public static String numFormat(float m, int n) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(n);
        return nf.format(m);
    }

    /**
	* ɾ��Ŀ¼�������ļ�
	*/
    public static void delete(File f) {
        File[] files = f.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                files[i].delete();
            } else if (files[i].isDirectory()) {
                if (!files[i].delete()) {
                    delete(files[i]);
                }
            }
        }
        deleteDirectory(f);
    }

    /**
	* ɾ��Ŀ¼�������ļ���
	*/
    public static void deleteDirectory(File f) {
        File[] filed = f.listFiles();
        for (int i = 0; i < filed.length; i++) {
            deleteDirectory(filed[i]);
            filed[i].delete();
        }
    }

    public static String[] getNames(String strDir, final String fileType) {
        File f = new File(strDir);
        String[] names = f.list(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                File f = new File(dir, name);
                if (f.isDirectory() == false && name.indexOf("." + fileType) != -1) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        return names;
    }

    public static void main(String s[]) {
        try {
            jun Jun = new jun();
            Iterator it = Jun.listAllFiles(new File("I:\\data CP\\SL6027B8 2SXD29336.1 CP DATA"), "", true);
            while (it.hasNext()) {
                System.out.println(((File) it.next()).getCanonicalPath());
            }
        } catch (Exception e) {
            System.out.println(e + " main() \n");
        }
    }
}
