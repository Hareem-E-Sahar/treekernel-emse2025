package com.izforge.izpack.compiler;

import com.izforge.izpack.Pack;
import com.izforge.izpack.PackFile;
import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.IXMLWriter;
import com.izforge.izpack.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.adaptator.impl.XMLWriter;
import com.izforge.izpack.util.FileUtil;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Pack200;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

/**
 * The packager class. The packager is used by the compiler to put files into an installer, and
 * create the actual installer files.
 *
 * @author Julien Ponge
 * @author Chadwick McHenry
 */
public class Packager extends PackagerBase {

    /**
     * Executable zipped output stream. First to open, last to close.
     * Attention! This is our own JarOutputStream, not the java standard!
     */
    private com.izforge.izpack.util.JarOutputStream primaryJarStream;

    /**
     * The constructor.
     *
     * @throws CompilerException
     */
    public Packager() throws CompilerException {
        this("default");
    }

    /**
     * Extended constructor.
     *
     * @param compr_format Compression format to be used for packs
     *                     compression format (if supported)
     * @throws CompilerException
     */
    public Packager(String compr_format) throws CompilerException {
        this(compr_format, -1);
    }

    /**
     * Extended constructor.
     *
     * @param compr_format Compression format to be used for packs
     * @param compr_level  Compression level to be used with the chosen
     *                     compression format (if supported)
     * @throws CompilerException
     */
    public Packager(String compr_format, int compr_level) throws CompilerException {
        initPackCompressor(compr_format, compr_level);
    }

    public void createInstaller(File primaryFile) throws Exception {
        String baseName = primaryFile.getName();
        if (baseName.endsWith(".jar")) {
            baseName = baseName.substring(0, baseName.length() - 4);
            baseFile = new File(primaryFile.getParentFile(), baseName);
        } else {
            baseFile = primaryFile;
        }
        info.setInstallerBase(baseFile.getName());
        packJarsSeparate = (info.getWebDirURL() != null);
        primaryJarStream = getJarOutputStream(baseFile.getName() + ".jar");
        sendStart();
        writeInstaller();
        primaryJarStream.closeAlways();
        sendStop();
    }

    /**
     * Write skeleton installer to primary jar. It is just an included jar, except that we copy the
     * META-INF as well.
     */
    protected void writeSkeletonInstaller() throws IOException {
        sendMsg("Copying the skeleton installer", PackagerListener.MSG_VERBOSE);
        InputStream is = Packager.class.getResourceAsStream("/" + SKELETON_SUBPATH);
        if (is == null) {
            File skeleton = new File(Compiler.IZPACK_HOME, SKELETON_SUBPATH);
            is = new FileInputStream(skeleton);
        }
        ZipInputStream inJarStream = new ZipInputStream(is);
        copyZip(inJarStream, primaryJarStream);
    }

    /**
     * Write an arbitrary object to primary jar.
     */
    protected void writeInstallerObject(String entryName, Object object) throws IOException {
        primaryJarStream.putNextEntry(new org.apache.tools.zip.ZipEntry(entryName));
        ObjectOutputStream out = new ObjectOutputStream(primaryJarStream);
        out.writeObject(object);
        out.flush();
        primaryJarStream.closeEntry();
    }

    /**
     * Write the data referenced by URL to primary jar.
     */
    protected void writeInstallerResources() throws IOException {
        sendMsg("Copying " + installerResourceURLMap.size() + " files into installer");
        Iterator<String> i = installerResourceURLMap.keySet().iterator();
        while (i.hasNext()) {
            String name = i.next();
            InputStream in = (installerResourceURLMap.get(name)).openStream();
            org.apache.tools.zip.ZipEntry newEntry = new org.apache.tools.zip.ZipEntry(name);
            long dateTime = FileUtil.getFileDateTime(installerResourceURLMap.get(name));
            if (dateTime != -1) {
                newEntry.setTime(dateTime);
            }
            primaryJarStream.putNextEntry(newEntry);
            PackagerHelper.copyStream(in, primaryJarStream);
            primaryJarStream.closeEntry();
            in.close();
        }
    }

    /**
     * Copy included jars to primary jar.
     */
    protected void writeIncludedJars() throws IOException {
        sendMsg("Merging " + includedJarURLs.size() + " jars into installer");
        Iterator<Object[]> i = includedJarURLs.iterator();
        while (i.hasNext()) {
            Object[] current = i.next();
            InputStream is = ((URL) current[0]).openStream();
            ZipInputStream inJarStream = new ZipInputStream(is);
            copyZip(inJarStream, primaryJarStream, (List<String>) current[1]);
        }
    }

    /**
     * Write Packs to primary jar or each to a separate jar.
     */
    protected void writePacks() throws Exception {
        final int num = packsList.size();
        sendMsg("Writing " + num + " Pack" + (num > 1 ? "s" : "") + " into installer");
        Map<File, Object[]> storedFiles = new HashMap<File, Object[]>();
        Map<Integer, File> pack200Map = new HashMap<Integer, File>();
        int pack200Counter = 0;
        primaryJarStream.setEncoding("utf-8");
        int packNumber = 0;
        Iterator<PackInfo> packIter = packsList.iterator();
        IXMLElement root = new XMLElementImpl("packs");
        while (packIter.hasNext()) {
            PackInfo packInfo = packIter.next();
            Pack pack = packInfo.getPack();
            pack.nbytes = 0;
            if ((pack.id == null) || (pack.id.length() == 0)) {
                pack.id = pack.name;
            }
            com.izforge.izpack.util.JarOutputStream packStream = primaryJarStream;
            if (packJarsSeparate) {
                String name = baseFile.getName() + ".pack-" + pack.id + ".jar";
                packStream = getJarOutputStream(name);
            }
            OutputStream comprStream = packStream;
            sendMsg("Writing Pack " + packNumber + ": " + pack.name, PackagerListener.MSG_VERBOSE);
            org.apache.tools.zip.ZipEntry entry = new org.apache.tools.zip.ZipEntry("packs/pack-" + pack.id);
            if (!compressor.useStandardCompression()) {
                entry.setMethod(ZipEntry.STORED);
                entry.setComment(compressor.getCompressionFormatSymbols()[0]);
                packStream.putNextEntry(entry);
                packStream.flush();
                comprStream = compressor.getOutputStream(packStream);
            } else {
                int level = compressor.getCompressionLevel();
                if (level >= 0 && level < 10) {
                    packStream.setLevel(level);
                }
                packStream.putNextEntry(entry);
                packStream.flush();
            }
            ByteCountingOutputStream dos = new ByteCountingOutputStream(comprStream);
            ObjectOutputStream objOut = new ObjectOutputStream(dos);
            objOut.writeInt(packInfo.getPackFiles().size());
            Iterator iter = packInfo.getPackFiles().iterator();
            while (iter.hasNext()) {
                boolean addFile = !pack.loose;
                boolean pack200 = false;
                PackFile pf = (PackFile) iter.next();
                File file = packInfo.getFile(pf);
                if (file.getName().toLowerCase().endsWith(".jar") && info.isPack200Compression() && isNotSignedJar(file)) {
                    pf.setPack200Jar(true);
                    pack200 = true;
                }
                Object[] info = storedFiles.get(file);
                if (info != null && !packJarsSeparate) {
                    pf.setPreviousPackFileRef((String) info[0], (Long) info[1]);
                    addFile = false;
                }
                objOut.writeObject(pf);
                if (addFile && !pf.isDirectory()) {
                    long pos = dos.getByteCount();
                    if (pack200) {
                        pack200Map.put(pack200Counter, file);
                        objOut.writeInt(pack200Counter);
                        pack200Counter = pack200Counter + 1;
                    } else {
                        FileInputStream inStream = new FileInputStream(file);
                        long bytesWritten = PackagerHelper.copyStream(inStream, objOut);
                        inStream.close();
                        if (bytesWritten != pf.length()) {
                            throw new IOException("File size mismatch when reading " + file);
                        }
                    }
                    storedFiles.put(file, new Object[] { pack.id, pos });
                }
                pack.nbytes += pf.size();
            }
            objOut.writeInt(packInfo.getParsables().size());
            iter = packInfo.getParsables().iterator();
            while (iter.hasNext()) {
                objOut.writeObject(iter.next());
            }
            objOut.writeInt(packInfo.getExecutables().size());
            iter = packInfo.getExecutables().iterator();
            while (iter.hasNext()) {
                objOut.writeObject(iter.next());
            }
            objOut.writeInt(packInfo.getUpdateChecks().size());
            iter = packInfo.getUpdateChecks().iterator();
            while (iter.hasNext()) {
                objOut.writeObject(iter.next());
            }
            objOut.flush();
            if (!compressor.useStandardCompression()) {
                comprStream.close();
            }
            packStream.closeEntry();
            if (packJarsSeparate) {
                packStream.closeAlways();
            }
            IXMLElement child = new XMLElementImpl("pack", root);
            child.setAttribute("nbytes", Long.toString(pack.nbytes));
            child.setAttribute("name", pack.name);
            if (pack.id != null) {
                child.setAttribute("id", pack.id);
            }
            root.addChild(child);
            packNumber++;
        }
        primaryJarStream.putNextEntry(new org.apache.tools.zip.ZipEntry("packs.info"));
        ObjectOutputStream out = new ObjectOutputStream(primaryJarStream);
        out.writeInt(packsList.size());
        Iterator<PackInfo> i = packsList.iterator();
        while (i.hasNext()) {
            PackInfo pack = i.next();
            out.writeObject(pack.getPack());
        }
        out.flush();
        primaryJarStream.closeEntry();
        Pack200.Packer packer = createAgressivePack200Packer();
        for (Integer key : pack200Map.keySet()) {
            File file = pack200Map.get(key);
            primaryJarStream.putNextEntry(new org.apache.tools.zip.ZipEntry("packs/pack200-" + key));
            JarFile jar = new JarFile(file);
            packer.pack(jar, primaryJarStream);
            jar.close();
            primaryJarStream.closeEntry();
        }
    }

    private Pack200.Packer createAgressivePack200Packer() {
        Pack200.Packer packer = Pack200.newPacker();
        Map<String, String> m = packer.properties();
        m.put(Pack200.Packer.EFFORT, "9");
        m.put(Pack200.Packer.SEGMENT_LIMIT, "-1");
        m.put(Pack200.Packer.KEEP_FILE_ORDER, Pack200.Packer.FALSE);
        m.put(Pack200.Packer.DEFLATE_HINT, Pack200.Packer.FALSE);
        m.put(Pack200.Packer.MODIFICATION_TIME, Pack200.Packer.LATEST);
        m.put(Pack200.Packer.CODE_ATTRIBUTE_PFX + "LineNumberTable", Pack200.Packer.STRIP);
        m.put(Pack200.Packer.CODE_ATTRIBUTE_PFX + "LocalVariableTable", Pack200.Packer.STRIP);
        m.put(Pack200.Packer.CODE_ATTRIBUTE_PFX + "SourceFile", Pack200.Packer.STRIP);
        return packer;
    }

    private boolean isNotSignedJar(File file) throws IOException {
        JarFile jar = new JarFile(file);
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().startsWith("META-INF") && entry.getName().endsWith(".SF")) {
                jar.close();
                return false;
            }
        }
        jar.close();
        return true;
    }

    /**
     * Return a stream for the next jar.
     */
    private com.izforge.izpack.util.JarOutputStream getJarOutputStream(String name) throws IOException {
        File file = new File(baseFile.getParentFile(), name);
        sendMsg("Building installer jar: " + file.getAbsolutePath());
        com.izforge.izpack.util.JarOutputStream jar = new com.izforge.izpack.util.JarOutputStream(file);
        jar.setLevel(Deflater.BEST_COMPRESSION);
        jar.setPreventClose(true);
        return jar;
    }

    /**
     * Copies contents of one jar to another.
     * <p/>
     * <p/>
     * TODO: it would be useful to be able to keep signature information from signed jar files, can
     * we combine manifests and still have their content signed?
     */
    private void copyZip(ZipInputStream zin, org.apache.tools.zip.ZipOutputStream out) throws IOException {
        copyZip(zin, out, null);
    }

    /**
     * Copies specified contents of one jar to another.
     * <p/>
     * <p/>
     * TODO: it would be useful to be able to keep signature information from signed jar files, can
     * we combine manifests and still have their content signed?
     */
    private void copyZip(ZipInputStream zin, org.apache.tools.zip.ZipOutputStream out, List<String> files) throws IOException {
        java.util.zip.ZipEntry zentry;
        if (!alreadyWrittenFiles.containsKey(out)) {
            alreadyWrittenFiles.put(out, new HashSet<String>());
        }
        HashSet<String> currentSet = alreadyWrittenFiles.get(out);
        while ((zentry = zin.getNextEntry()) != null) {
            String currentName = zentry.getName();
            String testName = currentName.replace('/', '.');
            testName = testName.replace('\\', '.');
            if (files != null) {
                Iterator<String> i = files.iterator();
                boolean founded = false;
                while (i.hasNext()) {
                    String doInclude = i.next();
                    if (testName.matches(doInclude)) {
                        founded = true;
                        break;
                    }
                }
                if (!founded) {
                    continue;
                }
            }
            if (currentSet.contains(currentName)) {
                continue;
            }
            try {
                org.apache.tools.zip.ZipEntry newEntry = new org.apache.tools.zip.ZipEntry(currentName);
                long fileTime = zentry.getTime();
                if (fileTime != -1) {
                    newEntry.setTime(fileTime);
                }
                out.putNextEntry(newEntry);
                PackagerHelper.copyStream(zin, out);
                out.closeEntry();
                zin.closeEntry();
                currentSet.add(currentName);
            } catch (ZipException x) {
            }
        }
    }

    public void addConfigurationInformation(IXMLElement data) {
    }
}
