import java.io.*;
import java.util.Vector;

public class SourceUtil {

    public static void main(String[] args) {
        if (args.length == 0) {
            args = new String[1];
            System.out.println("Please enter a command:\ninsert_public\t\tinserts public declare\nmake\t\tbuilds a MakeFromSource.java file\ndelete_class\t\tdeletes (recursively) all class files\n\n");
            System.out.print("function??<-:");
            args[0] = Ask.ask();
        }
        String s = args[0];
        if (s.equals("insert_public")) {
            insertpublic();
            return;
        } else if (s.equals("make")) {
            make();
        } else if (s.equals("delete_class")) {
            delete_class();
        } else {
            System.out.println("Not a valid function");
        }
    }

    public static void delete_class() {
        File basedir = new File(System.getProperty("user.dir"));
        File[] alldirs = getalldirs(basedir);
        Vector classfiles = new Vector();
        for (int i = 0; i < alldirs.length; i++) {
            File[] f = alldirs[i].listFiles();
            for (int j = 0; j < f.length; j++) {
                if (f[j].isDirectory()) {
                    continue;
                }
                String s = f[j].toString();
                if (s.endsWith("SourceUtil.class")) {
                    continue;
                }
                if (s.endsWith("Ask.class")) {
                    continue;
                }
                if (s.endsWith(".class")) {
                    classfiles.addElement(f[j]);
                    System.out.println("Adding:" + s + " for DELETE!");
                }
            }
        }
        for (int m = 0; m < classfiles.size(); m++) {
            File to_del = (File) classfiles.elementAt(m);
            try {
                System.out.println("DELETING: " + to_del.toString());
                to_del.delete();
                System.out.println("->deleted");
            } catch (Exception e) {
                System.out.println("Failed to delete:" + to_del.toString());
            }
        }
    }

    public static void make() {
        File basedir = new File(System.getProperty("user.dir"));
        File[] alldirs = getalldirs(basedir);
        String importstate = "";
        for (int i = 0; i < alldirs.length; i++) {
            if (countjavas(alldirs[i]) > 0) {
                importstate = importstate + make_import(alldirs[i]);
            }
        }
        System.out.println("Import statement:\n" + importstate);
        Vector javas = new Vector();
        for (int i = 0; i < alldirs.length; i++) {
            File[] f = alldirs[i].listFiles();
            for (int j = 0; j < f.length; j++) {
                if (f[j].isDirectory()) {
                    continue;
                }
                String s = f[j].toString();
                if (s.endsWith(".java")) {
                    if (!check_public(f[j])) {
                        continue;
                    }
                    javas.addElement(f[j]);
                    System.out.println("Adding " + s);
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                    }
                }
            }
        }
        String fs = System.getProperty("file.separator");
        String ls = System.getProperty("line.separator");
        System.out.println("Creating class body");
        try {
            Thread.sleep(400);
        } catch (Exception e) {
        }
        String cbody = "";
        while (!javas.isEmpty()) {
            String javafile = ((File) javas.elementAt(0)).toString();
            String cname = javafile.substring((javafile.lastIndexOf(fs) + fs.length()), javafile.indexOf(".java"));
            String lcname = cname.toLowerCase();
            if (lcname.equals("sourceutil") | lcname.equals("ask")) {
                javas.removeElementAt(0);
                continue;
            }
            cbody = cbody + cname + " " + lcname + ";" + ls;
            javas.removeElementAt(0);
        }
        System.out.println("class body:\n" + cbody);
        String towrite = importstate + ls + ls + "class MakeFromSource{" + ls + cbody + "}" + ls;
        File mksrc = new File("MakeFromSource.java");
        try {
            FileWriter fw = new FileWriter(mksrc);
            System.out.println("Writing Source File");
            fw.write(towrite);
            fw.close();
        } catch (Exception e) {
            System.out.println("Error writing source");
        }
        System.out.println("..->done");
    }

    public static int countjavas(File dir) {
        int cj = 0;
        File[] fls = dir.listFiles();
        for (int i = 0; i < fls.length; i++) {
            if (fls[i].isDirectory()) {
                continue;
            }
            if (fls[i].toString().endsWith(".java")) {
                cj++;
            }
            if (fls[i].toString().endsWith(".class")) {
                cj++;
            }
            if (fls[i].toString().indexOf("SourceUtil.java") > -1) {
                cj--;
            }
            if (fls[i].toString().indexOf("SourceUtil.class") > -1) {
                cj--;
            }
            if (fls[i].toString().indexOf("Ask.java") > -1) {
                cj--;
            }
            if (fls[i].toString().indexOf("Ask.class") > -1) {
                cj--;
            }
            if (fls[i].toString().indexOf("MakeFromSource.java") > -1) {
                cj--;
            }
            if (fls[i].toString().indexOf("MakeFromSource.class") > -1) {
                cj--;
            }
            if (cj > 0) {
                break;
            }
        }
        return cj;
    }

    public static String make_import(File dir) {
        String fs = System.getProperty("file.separator");
        String ls = System.getProperty("line.separator");
        String sdir = dir.toString();
        System.out.println("Creating import statement for " + sdir);
        String maindir = System.getProperty("user.dir");
        if (sdir.equals(maindir)) {
            int li = sdir.lastIndexOf(fs) + fs.length();
            return "import " + sdir.substring(li, sdir.length()) + ".*;" + ls;
        }
        int mdir = sdir.indexOf(maindir);
        if (mdir < 0) {
            System.out.println("This is weird!");
            return "";
        }
        String ss = "";
        if (countjavas(new File(maindir)) > 0) {
            int lfs = maindir.lastIndexOf(fs) + fs.length();
            ss = maindir.substring(lfs, maindir.length()) + ".";
        }
        int st = mdir + maindir.length();
        String bstring = sdir.substring(st, sdir.length());
        boolean ok = true;
        String imptstring = "import " + ss;
        int stit = 0;
        if (bstring.indexOf(fs, 0) == 0) {
            stit = 1;
        }
        int count = 0;
        while (ok) {
            count++;
            if (count > 2000) {
                System.out.println("stit==" + stit);
                break;
            }
            int fslocate = bstring.indexOf(fs, stit);
            if (fslocate < 0) {
                imptstring = imptstring + bstring.substring(stit, bstring.length());
                break;
            }
            for (int i = stit; i < fslocate; i++) {
                imptstring = imptstring + bstring.charAt(i);
            }
            imptstring = imptstring + ".";
            stit = fslocate + fs.length();
            if ((stit >= bstring.length()) | (stit < 0)) {
                ok = false;
                break;
            }
        }
        if (imptstring.endsWith(".")) {
            return imptstring + "*;" + ls;
        }
        return imptstring + ".*;" + ls;
    }

    public static void insertpublic() {
        File[] files;
        File[] directories;
        File basedir = new File(System.getProperty("user.dir"));
        File[] alldirs = getalldirs(basedir);
        Vector javas = new Vector();
        for (int i = 0; i < alldirs.length; i++) {
            File[] f = alldirs[i].listFiles();
            for (int j = 0; j < f.length; j++) {
                if (f[j].isDirectory()) {
                    continue;
                }
                String s = f[j].toString();
                if (s.endsWith(".java")) {
                    javas.addElement(f[j]);
                    System.out.println("Adding " + s);
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                    }
                }
            }
        }
        while (javas.isEmpty() == false) {
            fix((File) javas.elementAt(0));
            javas.removeElementAt(0);
        }
    }

    public static File[] getalldirs(File f) {
        Vector v = new Vector();
        v.addElement(f);
        int pos = 1;
        boolean recurse = true;
        File controlfile = f;
        while (recurse) {
            File[] fils = getdirs(controlfile);
            for (int x = 0; x < fils.length; x++) {
                v.addElement(fils[x]);
            }
            try {
                controlfile = (File) v.elementAt(pos);
            } catch (Exception e) {
                recurse = false;
            }
            pos++;
        }
        File[] totals = new File[v.size()];
        for (int i = 0; i < totals.length; i++) {
            totals[i] = (File) v.elementAt(i);
            System.out.println("Adding " + totals[i].toString());
        }
        return totals;
    }

    public static File[] getdirs(File f) {
        Vector v = new Vector();
        File[] fls = f.listFiles();
        for (int i = 0; i < fls.length; i++) {
            if (fls[i].isDirectory()) {
                v.addElement(fls[i]);
            }
        }
        File[] rets = new File[v.size()];
        for (int j = 0; j < rets.length; j++) {
            rets[j] = (File) v.elementAt(j);
        }
        return rets;
    }

    public static void fix(File f) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String contents = "";
            String ss;
            while ((ss = br.readLine()) != null) {
                contents = contents + ss + System.getProperty("line.separator");
            }
            br.close();
            String fname = f.toString();
            System.out.println("Read file:" + fname);
            String name = "";
            String fs = System.getProperty("file.separator");
            int st = fname.lastIndexOf(fs);
            int ed = fname.indexOf(".java", st);
            name = fname.substring((st + fs.length()), ed);
            int class_dec = contents.indexOf("class " + name);
            int pub_ck = contents.indexOf("public class " + name);
            if (pub_ck > -1) {
                System.out.println("This file already has public declare");
                return;
            }
            if (class_dec < 0) {
                System.out.println("This file has no \"class " + name + "\" declaration");
                return;
            }
            String newfile = "";
            for (int w = 0; w < class_dec; w++) {
                newfile = newfile + contents.charAt(w);
            }
            newfile = newfile + " public ";
            newfile = newfile + contents.substring(class_dec, contents.length());
            System.out.println("Writing new file " + fname);
            FileWriter fw = new FileWriter(f);
            fw.write(newfile);
            fw.close();
            System.out.println("Wrote the file");
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        } catch (Exception e) {
            System.out.println("error with " + f.toString());
        }
    }

    public static boolean check_public(File f) {
        if (f == null) {
            System.out.println("File null?");
            return true;
        }
        try {
            String contents = "";
            String s;
            try {
                BufferedReader br = new BufferedReader(new FileReader(f));
                while ((s = br.readLine()) != null) {
                    contents = contents + s;
                    int pubC = contents.indexOf("public class");
                    int pubI = contents.indexOf("public interface");
                    if ((pubC >= 0) | (pubI >= 0)) {
                        br.close();
                        return true;
                    }
                }
                br.close();
            } catch (Exception e) {
                return true;
            }
            int pubC = contents.indexOf("public class");
            int pubI = contents.indexOf("public interface");
            if ((pubC < 0) & (pubI < 0)) {
                return false;
            }
        } catch (Exception ez) {
            ez.printStackTrace();
        }
        return true;
    }
}
