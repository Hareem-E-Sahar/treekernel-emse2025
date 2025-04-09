package com.threerings.getdown.tools;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.security.MessageDigest;
import com.samskivert.io.StreamUtil;
import com.threerings.getdown.data.Application;
import com.threerings.getdown.data.Digest;
import com.threerings.getdown.data.Resource;

/**
 * Generates patch files between two particular revisions of an
 * application. The differences between all the files in the two
 * revisions are bundled into a single patch file which is placed into the
 * target version directory.
 */
public class Differ {

    /**
     * Creates a single patch file that contains the differences between
     * the two specified application directories. The patch file will be
     * created in the <code>nvdir</code> directory with name
     * <code>patchV.dat</code> where V is the old application version.
     */
    public void createDiff(File nvdir, File ovdir, boolean verbose) throws IOException {
        String nvers = nvdir.getName();
        String overs = ovdir.getName();
        try {
            if (Long.parseLong(nvers) <= Long.parseLong(overs)) {
                String err = "New version (" + nvers + ") must be greater " + "than old version (" + overs + ").";
                throw new IOException(err);
            }
        } catch (NumberFormatException nfe) {
            throw new IOException("Non-numeric versions? [nvers=" + nvers + ", overs=" + overs + "].");
        }
        Application oapp = new Application(ovdir, null);
        oapp.init(false);
        ArrayList<Resource> orsrcs = new ArrayList<Resource>();
        orsrcs.addAll(oapp.getCodeResources());
        orsrcs.addAll(oapp.getResources());
        Application napp = new Application(nvdir, null);
        napp.init(false);
        ArrayList<Resource> nrsrcs = new ArrayList<Resource>();
        nrsrcs.addAll(napp.getCodeResources());
        nrsrcs.addAll(napp.getResources());
        File patch = new File(nvdir, "patch" + overs + ".dat");
        createPatch(patch, orsrcs, nrsrcs, verbose);
        for (String auxgroup : napp.getAuxGroups()) {
            orsrcs = new ArrayList<Resource>();
            orsrcs.addAll(oapp.getResources(auxgroup));
            nrsrcs = new ArrayList<Resource>();
            nrsrcs.addAll(napp.getResources(auxgroup));
            patch = new File(nvdir, "patch-" + auxgroup + overs + ".dat");
            createPatch(patch, orsrcs, nrsrcs, verbose);
        }
    }

    protected void createPatch(File patch, ArrayList<Resource> orsrcs, ArrayList<Resource> nrsrcs, boolean verbose) throws IOException {
        MessageDigest md = Digest.getMessageDigest();
        JarOutputStream jout = null;
        try {
            jout = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(patch)));
            for (Resource rsrc : nrsrcs) {
                int oidx = orsrcs.indexOf(rsrc);
                Resource orsrc = (oidx == -1) ? null : orsrcs.remove(oidx);
                if (orsrc != null) {
                    String odig = orsrc.computeDigest(md, null);
                    String ndig = rsrc.computeDigest(md, null);
                    if (odig.equals(ndig)) {
                        if (verbose) {
                            System.out.println("Unchanged: " + rsrc.getPath());
                        }
                        continue;
                    }
                    if (rsrc.getPath().endsWith(".jar")) {
                        if (verbose) {
                            System.out.println("JarDiff: " + rsrc.getPath());
                        }
                        File otemp = rebuildJar(orsrc.getLocal());
                        File temp = rebuildJar(rsrc.getLocal());
                        jout.putNextEntry(new ZipEntry(rsrc.getPath() + Patcher.PATCH));
                        jarDiff(otemp, temp, jout);
                        otemp.delete();
                        temp.delete();
                        continue;
                    }
                }
                if (verbose) {
                    System.out.println("Addition: " + rsrc.getPath());
                }
                jout.putNextEntry(new ZipEntry(rsrc.getPath() + Patcher.CREATE));
                pipe(rsrc.getLocal(), jout);
            }
            for (Resource rsrc : orsrcs) {
                if (verbose) {
                    System.out.println("Removal: " + rsrc.getPath());
                }
                jout.putNextEntry(new ZipEntry(rsrc.getPath() + Patcher.DELETE));
            }
            StreamUtil.close(jout);
            System.out.println("Created patch file: " + patch);
        } catch (IOException ioe) {
            StreamUtil.close(jout);
            patch.delete();
            throw ioe;
        }
    }

    protected File rebuildJar(File target) throws IOException {
        JarFile jar = new JarFile(target);
        File temp = File.createTempFile("differ", "jar");
        JarOutputStream jout = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(temp)));
        byte[] buffer = new byte[4096];
        for (Enumeration<JarEntry> iter = jar.entries(); iter.hasMoreElements(); ) {
            JarEntry entry = iter.nextElement();
            entry.setCompressedSize(-1);
            jout.putNextEntry(entry);
            InputStream in = jar.getInputStream(entry);
            int size = in.read(buffer);
            while (size != -1) {
                jout.write(buffer, 0, size);
                size = in.read(buffer);
            }
            in.close();
        }
        jout.close();
        jar.close();
        return temp;
    }

    protected void jarDiff(File ofile, File nfile, JarOutputStream jout) throws IOException {
        JarDiff.createPatch(ofile.getPath(), nfile.getPath(), jout, false);
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: Differ [-verbose] new_vers_dir old_vers_dir");
            System.exit(255);
        }
        Differ differ = new Differ();
        boolean verbose = false;
        int aidx = 0;
        if (args[0].equals("-verbose")) {
            verbose = true;
            aidx++;
        }
        try {
            differ.createDiff(new File(args[aidx++]), new File(args[aidx++]), verbose);
        } catch (IOException ioe) {
            System.err.println("Error: " + ioe.getMessage());
            System.exit(255);
        }
    }

    protected static void pipe(File file, JarOutputStream jout) throws IOException {
        FileInputStream fin = null;
        try {
            StreamUtil.copy(fin = new FileInputStream(file), jout);
        } finally {
            StreamUtil.close(fin);
        }
    }
}
