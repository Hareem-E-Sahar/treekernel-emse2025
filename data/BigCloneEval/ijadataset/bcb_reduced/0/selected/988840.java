package net.sf.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class PackCompact {

    /**
	 * @param args
	 */
    public static void main(String[] args) throws Exception {
        PackCompact pkger = new PackCompact();
        pkger.debugEnabled = true;
        pkger.jarFile = "rt.jar";
        Set<String> ret = new HashSet<String>();
        ret.addAll(pkger.parseOutput(new FileInputStream("d:/zhanglong/temp/mywork1.lst")));
        ret.addAll(pkger.parseOutput(new FileInputStream("d:/zhanglong/temp/mywork2.lst")));
        ret.addAll(pkger.parseOutput(new FileInputStream("d:/zhanglong/temp/mywork3.lst")));
        ret.addAll(pkger.parseOutput(new FileInputStream("d:/zhanglong/temp/mywork4.lst")));
        ret.addAll(pkger.parseOutput(new FileInputStream("d:/zhanglong/temp/mywork5.lst")));
        ret.addAll(pkger.parseOutput(new FileInputStream("d:/zhanglong/temp/mywork6.lst")));
        ret.addAll(pkger.parseOutput(new FileInputStream("d:/zhanglong/temp/mywork7.lst")));
        ret.addAll(pkger.parseOutput(new FileInputStream("d:/zhanglong/temp/mywork8.lst")));
        ret.addAll(pkger.parseOutput(new FileInputStream("d:/zhanglong/temp/mywork9.lst")));
        ret.addAll(pkger.parseOutput2(new FileInputStream("d:/zhanglong/temp/mywork1.lst2")));
        ret.addAll(pkger.parseOutput2(new FileInputStream("d:/zhanglong/temp/mywork2.lst2")));
        pkger.pkgResources(ret, "d:/zhanglong/temp/" + pkger.jarFile);
    }

    private boolean debugEnabled;

    private String jarFile;

    public Set<String> parseOutput(InputStream in) throws IOException {
        HashSet<String> ret = new HashSet<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = reader.readLine();
        while (line != null) {
            if (line.indexOf(jarFile) != -1) {
                if (debugEnabled) System.out.println("Matches:" + line);
                int pos1 = line.indexOf("\t");
                int pos2 = line.indexOf("\t", pos1 + 1);
                ret.add(line.substring(0, pos1) + "." + line.substring(pos1 + 1, pos2));
            } else {
                if (debugEnabled) System.out.println("UnMatches:" + line);
            }
            line = reader.readLine();
        }
        return ret;
    }

    public Set<String> parseOutput2(InputStream in) throws IOException {
        HashSet<String> ret = new HashSet<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith("[Loaded") && line.indexOf(jarFile) != -1 && line.indexOf(" from ") != -1) {
                if (debugEnabled) System.out.println("Matches:" + line);
                ret.add(line.substring(8, line.indexOf(" from ")));
            } else {
                if (debugEnabled) System.out.println("UnMatches:" + line);
            }
            line = reader.readLine();
        }
        return ret;
    }

    public void pkgResources(Set<String> res, String fileName) throws IOException {
        File f = new File(fileName);
        if (!f.exists()) {
            f.createNewFile();
        }
        byte[] buf = new byte[1024];
        JarOutputStream out = new JarOutputStream(new FileOutputStream(fileName));
        for (String s : res) {
            s = s.replace('.', '/') + ".class";
            if (debugEnabled) {
                System.out.println("adding: " + s);
            }
            InputStream in = this.getClass().getClassLoader().getResourceAsStream(s);
            if (in == null) continue;
            out.putNextEntry(new ZipEntry(s));
            int w = in.read(buf);
            while (w >= 0) {
                out.write(buf, 0, w);
                w = in.read(buf);
            }
            in.close();
        }
        out.finish();
        out.close();
    }
}
