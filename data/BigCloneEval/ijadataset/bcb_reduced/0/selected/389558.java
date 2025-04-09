package net.picenum.radiant.codegen.patch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import net.picenum.radiant.codegen.Tokens;

public class RadPatchUtils {

    public static final String RADPATCH_SUFFIX = ".radPatch";

    public static final String RADPATCH_TOKEN_START = "@@@RADPATCH@START@";

    public static final String RADPATCH_TOKEN_END = "@@@RADPATCH@END@";

    public static final String ID_PREFIX = "id=\"";

    public static final String HASH_PREFIX = "hash=\"";

    private static final String ID_SUFFIX = "\"";

    private static final String HASH_SUFFIX = "\"";

    private static List<File> findPatchFiles(File dir) {
        List<File> retValues = new ArrayList<File>();
        findPatchFilesRecursive(retValues, dir);
        return retValues;
    }

    public static String createJavaPatch(String id, String code) {
        String str = Tokens.nl + "// @@@RADPATCH@START@ " + ID_PREFIX + id + ID_SUFFIX + " " + HASH_PREFIX + calcPatchHash(code) + HASH_SUFFIX + Tokens.nl + code + "// @@@RADPATCH@END@" + Tokens.nl + Tokens.nl;
        return str;
    }

    private static void findPatchFilesRecursive(List<File> retValues, File aFile) {
        if (aFile.isDirectory()) {
            String[] children = aFile.list();
            for (int i = 0; i < children.length; i++) {
                findPatchFilesRecursive(retValues, new File(aFile, children[i]));
            }
        } else {
            if (aFile.getName().endsWith(RADPATCH_SUFFIX)) retValues.add(aFile);
        }
    }

    private static String getFileNameFromPatchFile(File patchFile) {
        String absPath = patchFile.getAbsolutePath().substring(0, patchFile.getAbsolutePath().length() - RADPATCH_SUFFIX.length());
        return absPath;
    }

    private static void applyRadPatchs(File patchFile) throws Exception {
        File originalFile = new File(getFileNameFromPatchFile(patchFile));
        Map<String, RadPatchBean> patches = readPatches(patchFile);
        applyPatchesToFile(patches, originalFile.getAbsolutePath());
    }

    private static Map<String, RadPatchBean> readPatches(File patchFile) throws Exception {
        String line;
        String patchLine;
        Map<String, RadPatchBean> res = new HashMap<String, RadPatchBean>();
        FileInputStream fis = new FileInputStream(patchFile.getAbsolutePath());
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
        while ((line = reader.readLine()) != null) {
            if (line.contains(RADPATCH_TOKEN_END)) {
                throw new Exception("END BEFORE START... on file: " + patchFile.getAbsolutePath() + "\n" + line);
            }
            if (line.contains(RADPATCH_TOKEN_START)) {
                RadPatchBean pb = new RadPatchBean();
                pb.setPatchId(readPatchId(line));
                System.out.println(line);
                pb.setHashCode(readHash(line));
                StringBuffer patchBuffer = new StringBuffer();
                patchBuffer.append(line + "\n");
                boolean finishPatch = false;
                do {
                    patchLine = reader.readLine();
                    patchBuffer.append(patchLine + "\n");
                    if (patchLine.contains(RADPATCH_TOKEN_START)) {
                        reader.close();
                        throw new Exception("DOUBLE START BEFORE END... on file: " + patchFile.getAbsolutePath() + "\n" + line + "\n[...]\n" + patchLine);
                    }
                    if (patchLine.contains(RADPATCH_TOKEN_END)) {
                        finishPatch = true;
                    }
                } while (!finishPatch);
                pb.setPatchText(patchBuffer.toString());
                res.put(pb.getPatchId(), pb);
            }
        }
        reader.close();
        return res;
    }

    public static Long calcPatchHash(String patchText) {
        String line;
        StringBuffer inner = new StringBuffer();
        BufferedReader reader = new BufferedReader(new StringReader(patchText));
        try {
            while ((line = reader.readLine()) != null) {
                if (!line.contains(RADPATCH_TOKEN_END)) if (!line.contains(RADPATCH_TOKEN_START)) {
                    inner.append(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return calcHash(inner.toString());
    }

    private static Long calcHash(String patchText) {
        byte[] bytes = patchText.getBytes();
        Checksum checksumEngine = new CRC32();
        checksumEngine.update(bytes, 0, bytes.length);
        return checksumEngine.getValue();
    }

    private static String readPatchId(String line) {
        String startIdstr = line.substring(line.indexOf(ID_PREFIX) + ID_PREFIX.length());
        String id = startIdstr.substring(0, startIdstr.indexOf(ID_SUFFIX));
        return id;
    }

    private static Long readHash(String line) {
        String startIdstr = line.substring(line.indexOf(HASH_PREFIX) + HASH_PREFIX.length());
        String id = startIdstr.substring(0, startIdstr.indexOf(HASH_SUFFIX));
        return new Long(id);
    }

    public static void patchDir(String path) throws Exception {
        System.out.println("patchDir: " + path);
        File dir = new File(path);
        List<File> patchFiles = RadPatchUtils.findPatchFiles(dir);
        for (File patchFile : patchFiles) {
            RadPatchUtils.applyRadPatchs(patchFile);
        }
    }

    private static void applyPatchesToFile(Map<String, RadPatchBean> pbs, String originalFileName) {
        if (pbs.size() == 0) return;
        System.out.println("applyPatchesToFile: " + originalFileName);
        String line;
        StringBuffer outputFile = new StringBuffer();
        String patchLine;
        try {
            FileInputStream fis = new FileInputStream(originalFileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            while ((line = reader.readLine()) != null) {
                if (line.contains(RADPATCH_TOKEN_START)) {
                    String patchId = readPatchId(line);
                    RadPatchBean pb = pbs.get(patchId);
                    if (pb != null) {
                        boolean finishPatch = false;
                        do {
                            patchLine = reader.readLine();
                            if (patchLine.contains(RADPATCH_TOKEN_END)) {
                                finishPatch = true;
                            }
                        } while (!finishPatch);
                        outputFile.append(pb.getPatchText());
                    } else {
                        outputFile.append(line + "\n");
                    }
                } else {
                    outputFile.append(line + "\n");
                }
            }
            reader.close();
            BufferedWriter out = new BufferedWriter(new FileWriter(originalFileName));
            out.write(outputFile.toString());
            out.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static void updatePatchDir(String path) throws Exception {
        System.out.println("updating radPatch: " + path);
        List<File> files = findFiles(new File(path));
        for (File file : files) {
            System.out.println("Readind patch from file " + file);
            Map<String, RadPatchBean> patches = readPatches(file);
            for (RadPatchBean patch : patches.values()) {
                if (!patch.getHashCode().equals(calcPatchHash(patch.getPatchText()))) {
                    String patchFileName = file.getAbsolutePath() + RADPATCH_SUFFIX;
                    File patchFile = new File(patchFileName);
                    if (patchFile.exists()) {
                        Map<String, RadPatchBean> patchesInPatchFile = readPatches(patchFile);
                        if (patchesInPatchFile.containsKey(patch.getPatchId())) {
                            deletePatchFromFile(patch.getPatchId(), patchFile);
                        }
                    }
                    try {
                        BufferedWriter out = new BufferedWriter(new FileWriter(patchFileName, true));
                        patch.setPatchText(updateHashInPatchText(patch.getPatchText()));
                        out.write("\n" + patch.getPatchText());
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static String updateHashInPatchText(String patchText) {
        String oldHash = readHash(patchText).toString();
        String preHash = patchText.substring(0, patchText.indexOf(HASH_PREFIX) + HASH_PREFIX.length());
        String hash = calcPatchHash(patchText).toString();
        String postHash = patchText.substring(preHash.length() + oldHash.length());
        StringBuffer out = new StringBuffer();
        out.append(preHash);
        out.append(hash);
        out.append(postHash);
        return out.toString();
    }

    private static void deletePatchFromFile(String patchId, File patchFile) {
        String line;
        String patchLine;
        StringBuffer outputFile = new StringBuffer();
        try {
            FileInputStream fis = new FileInputStream(patchFile.getAbsolutePath());
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            while ((line = reader.readLine()) != null) {
                boolean isFirstLine = false;
                if (line.contains(RADPATCH_TOKEN_START)) {
                    String pid = readPatchId(line);
                    if (pid.equals(patchId)) {
                        isFirstLine = true;
                        boolean finishPatch = false;
                        do {
                            patchLine = reader.readLine();
                            if (patchLine.contains(RADPATCH_TOKEN_END)) {
                                finishPatch = true;
                            }
                        } while (!finishPatch);
                    }
                }
                if (!isFirstLine) outputFile.append(line + "\n");
            }
            reader.close();
            BufferedWriter out = new BufferedWriter(new FileWriter(patchFile.getAbsolutePath()));
            out.write(outputFile.toString());
            out.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void findFilesRecursive(List<File> retValues, File aFile) {
        if (aFile.isDirectory() && !aFile.getName().startsWith(".")) {
            String[] children = aFile.list();
            for (int i = 0; i < children.length; i++) {
                findFilesRecursive(retValues, new File(aFile, children[i]));
            }
        } else {
            if (!aFile.getName().endsWith(RADPATCH_SUFFIX) && !aFile.getName().startsWith(".")) retValues.add(aFile);
        }
    }

    public static List<File> findFiles(File dir) {
        List<File> retValues = new ArrayList<File>();
        findFilesRecursive(retValues, dir);
        return retValues;
    }
}
