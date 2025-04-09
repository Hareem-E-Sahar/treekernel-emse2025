package com.izforge.izpack.compiler;

import com.izforge.izpack.Pack;
import com.izforge.izpack.PackFile;
import com.izforge.izpack.XPackFile;
import com.izforge.izpack.io.FileSpanningOutputStream;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.FileUtil;
import com.izforge.izpack.adaptator.IXMLElement;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.zip.*;

/**
 * The packager class. The packager is used by the compiler to put files into an installer, and
 * create the actual installer files.
 * <p/>
 * This is a packager, which packs everything into multi volumes.
 *
 * @author Dennis Reil, <Dennis.Reil@reddot.de>
 */
public class MultiVolumePackager extends PackagerBase {

    public static final String INSTALLER_PAK_NAME = "installer";

    /**
     * Executable zipped output stream. First to open, last to close.
     */
    private ZipOutputStream primaryJarStream;

    private IXMLElement configdata = null;

    /**
     * The constructor.
     *
     * @throws CompilerException
     */
    public MultiVolumePackager() throws CompilerException {
        this("default");
    }

    /**
     * Extended constructor.
     *
     * @param compr_format Compression format to be used for packs compression format (if supported)
     * @throws CompilerException
     */
    public MultiVolumePackager(String compr_format) throws CompilerException {
        this(compr_format, -1);
    }

    /**
     * Extended constructor.
     *
     * @param compr_format Compression format to be used for packs
     * @param compr_level  Compression level to be used with the chosen compression format (if
     *                     supported)
     * @throws CompilerException
     */
    public MultiVolumePackager(String compr_format, int compr_level) throws CompilerException {
        initPackCompressor(compr_format, compr_level);
    }

    /**
     * Create the installer, beginning with the specified jar. If the name specified does not end in
     * ".jar", it is appended. If secondary jars are created for packs (if the Info object added has
     * a webDirURL set), they are created in the same directory, named sequentially by inserting
     * ".pack#" (where '#' is the pack number) ".jar" suffix: e.g. "foo.pack1.jar". If any file
     * exists, it is overwritten.
     */
    public void createInstaller(File primaryFile) throws Exception {
        this.analyzeConfigurationInformation();
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
        String packfile = baseFile.getParent() + File.separator + INSTALLER_PAK_NAME;
        writePacks(new File(packfile));
        primaryJarStream.close();
        sendStop();
    }

    /**
     * ********************************************************************************************
     * Listener assistance
     * ********************************************************************************************
     */
    private void analyzeConfigurationInformation() {
        String classname = this.getClass().getName();
        String sizeprop = classname + ".volumesize";
        String freespaceprop = classname + ".firstvolumefreespace";
        if (this.configdata == null) {
            this.variables.setProperty(sizeprop, Long.toString(FileSpanningOutputStream.DEFAULT_VOLUME_SIZE));
            this.variables.setProperty(freespaceprop, Long.toString(FileSpanningOutputStream.DEFAULT_ADDITIONAL_FIRST_VOLUME_FREE_SPACE_SIZE));
        } else {
            String volumesize = configdata.getAttribute("volumesize", Long.toString(FileSpanningOutputStream.DEFAULT_VOLUME_SIZE));
            String freespace = configdata.getAttribute("firstvolumefreespace", Long.toString(FileSpanningOutputStream.DEFAULT_ADDITIONAL_FIRST_VOLUME_FREE_SPACE_SIZE));
            this.variables.setProperty(sizeprop, volumesize);
            this.variables.setProperty(freespaceprop, freespace);
        }
    }

    /**
     * Write skeleton installer to primary jar. It is just an included jar, except that we copy the
     * META-INF as well.
     */
    protected void writeSkeletonInstaller() throws IOException {
        sendMsg("Copying the skeleton installer", PackagerListener.MSG_VERBOSE);
        InputStream is = MultiVolumePackager.class.getResourceAsStream("/" + SKELETON_SUBPATH);
        if (is == null) {
            File skeleton = new File(Compiler.IZPACK_HOME, SKELETON_SUBPATH);
            is = new FileInputStream(skeleton);
        }
        ZipInputStream inJarStream = new ZipInputStream(is);
        List<String> excludes = new ArrayList<String>();
        excludes.add("META-INF.MANIFEST.MF");
        copyZipWithoutExcludes(inJarStream, primaryJarStream, excludes);
        is = MultiVolumePackager.class.getResourceAsStream("/" + SKELETON_SUBPATH);
        if (is == null) {
            File skeleton = new File(Compiler.IZPACK_HOME, SKELETON_SUBPATH);
            is = new FileInputStream(skeleton);
        }
        inJarStream = new ZipInputStream(is);
        boolean found = false;
        ZipEntry ze = null;
        String modifiedmanifest = null;
        while (((ze = inJarStream.getNextEntry()) != null) && !found) {
            if ("META-INF/MANIFEST.MF".equals(ze.getName())) {
                long size = ze.getSize();
                byte[] buffer = new byte[4096];
                int readbytes = 0;
                int totalreadbytes = 0;
                StringBuffer manifest = new StringBuffer();
                while (((readbytes = inJarStream.read(buffer)) > 0) && (totalreadbytes < size)) {
                    totalreadbytes += readbytes;
                    String tmp = new String(buffer, 0, readbytes, "utf-8");
                    manifest.append(tmp);
                }
                StringReader stringreader = new StringReader(manifest.toString());
                BufferedReader reader = new BufferedReader(stringreader);
                String line = null;
                StringBuffer modified = new StringBuffer();
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Main-Class:")) {
                        line = "Main-Class: com.izforge.izpack.installer.MultiVolumeInstaller";
                    }
                    modified.append(line);
                    modified.append("\r\n");
                }
                reader.close();
                modifiedmanifest = modified.toString();
                break;
            }
        }
        primaryJarStream.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
        primaryJarStream.write(modifiedmanifest.getBytes());
        primaryJarStream.closeEntry();
    }

    /**
     * Write an arbitrary object to primary jar.
     */
    protected void writeInstallerObject(String entryName, Object object) throws IOException {
        primaryJarStream.putNextEntry(new ZipEntry(entryName));
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
        for (String s : installerResourceURLMap.keySet()) {
            String name = s;
            InputStream in = (installerResourceURLMap.get(name)).openStream();
            org.apache.tools.zip.ZipEntry newEntry = new org.apache.tools.zip.ZipEntry(name);
            long dateTime = FileUtil.getFileDateTime(installerResourceURLMap.get(name));
            if (dateTime != -1) {
                newEntry.setTime(dateTime);
            }
            primaryJarStream.putNextEntry(newEntry);
            copyStream(in, primaryJarStream);
            primaryJarStream.closeEntry();
            in.close();
        }
    }

    /**
     * Copy included jars to primary jar.
     */
    protected void writeIncludedJars() throws IOException {
        sendMsg("Merging " + includedJarURLs.size() + " jars into installer");
        for (Object[] includedJarURL : includedJarURLs) {
            Object[] current = includedJarURL;
            InputStream is = ((URL) current[0]).openStream();
            ZipInputStream inJarStream = new ZipInputStream(is);
            copyZip(inJarStream, primaryJarStream, (List<String>) current[1]);
        }
    }

    /**
     * Write Packs to primary jar or each to a separate jar.
     */
    private void writePacks(File primaryfile) throws Exception {
        final int num = packsList.size();
        sendMsg("Writing " + num + " Pack" + (num > 1 ? "s" : "") + " into installer");
        Debug.trace("Writing " + num + " Pack" + (num > 1 ? "s" : "") + " into installer");
        Map storedFiles = new HashMap();
        String classname = this.getClass().getName();
        String volumesize = this.getVariables().getProperty(classname + ".volumesize");
        String extraspace = this.getVariables().getProperty(classname + ".firstvolumefreespace");
        long volumesizel = FileSpanningOutputStream.DEFAULT_VOLUME_SIZE;
        long extraspacel = FileSpanningOutputStream.DEFAULT_ADDITIONAL_FIRST_VOLUME_FREE_SPACE_SIZE;
        if (volumesize != null) {
            volumesizel = Long.parseLong(volumesize);
        }
        if (extraspace != null) {
            extraspacel = Long.parseLong(extraspace);
        }
        Debug.trace("Volumesize: " + volumesizel);
        Debug.trace("Extra space on first volume: " + extraspacel);
        FileSpanningOutputStream fout = new FileSpanningOutputStream(primaryfile.getParent() + File.separator + primaryfile.getName() + ".pak", volumesizel);
        fout.setFirstvolumefreespacesize(extraspacel);
        int packNumber = 0;
        for (PackInfo aPacksList : packsList) {
            PackInfo packInfo = aPacksList;
            Pack pack = packInfo.getPack();
            pack.nbytes = 0;
            sendMsg("Writing Pack " + packNumber + ": " + pack.name, PackagerListener.MSG_VERBOSE);
            Debug.trace("Writing Pack " + packNumber + ": " + pack.name);
            ZipEntry entry = new ZipEntry("packs/pack" + packNumber);
            primaryJarStream.putNextEntry(entry);
            ObjectOutputStream objOut = new ObjectOutputStream(primaryJarStream);
            objOut.writeInt(packInfo.getPackFiles().size());
            Iterator iter = packInfo.getPackFiles().iterator();
            for (Object o : packInfo.getPackFiles()) {
                boolean addFile = !pack.loose;
                PackFile packfile = (PackFile) o;
                XPackFile pf = new XPackFile(packfile);
                File file = packInfo.getFile(packfile);
                Debug.trace("Next file: " + file.getAbsolutePath());
                Object[] info = (Object[]) storedFiles.get(file);
                if (info != null && !packJarsSeparate) {
                    Debug.trace("File already included in other pack");
                    pf.setPreviousPackFileRef((String) info[0], (Long) info[1]);
                    addFile = false;
                }
                if (addFile && !pf.isDirectory()) {
                    long pos = fout.getFilepointer();
                    pf.setArchivefileposition(pos);
                    int volumecountbeforewrite = fout.getVolumeCount();
                    FileInputStream inStream = new FileInputStream(file);
                    long bytesWritten = copyStream(inStream, fout);
                    fout.flush();
                    long posafterwrite = fout.getFilepointer();
                    Debug.trace("File (" + pf.sourcePath + ") " + pos + " <-> " + posafterwrite);
                    if (fout.getFilepointer() != (pos + bytesWritten)) {
                        Debug.trace("file: " + file.getName());
                        Debug.trace("(Filepos/BytesWritten/ExpectedNewFilePos/NewFilePointer) (" + pos + "/" + bytesWritten + "/" + (pos + bytesWritten) + "/" + fout.getFilepointer() + ")");
                        Debug.trace("Volumecount (before/after) (" + volumecountbeforewrite + "/" + fout.getVolumeCount() + ")");
                        throw new IOException("Error new filepointer is illegal");
                    }
                    if (bytesWritten != pf.length()) {
                        throw new IOException("File size mismatch when reading " + file);
                    }
                    inStream.close();
                }
                objOut.writeObject(pf);
                objOut.flush();
                pack.nbytes += pf.length();
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
            packNumber++;
        }
        int volumes = fout.getVolumeCount();
        Debug.trace("Written " + volumes + " volumes");
        String volumename = primaryfile.getName() + ".pak";
        fout.flush();
        fout.close();
        primaryJarStream.putNextEntry(new ZipEntry("volumes.info"));
        ObjectOutputStream out = new ObjectOutputStream(primaryJarStream);
        out.writeInt(volumes);
        out.writeUTF(volumename);
        out.flush();
        primaryJarStream.closeEntry();
        primaryJarStream.putNextEntry(new ZipEntry("packs.info"));
        out = new ObjectOutputStream(primaryJarStream);
        out.writeInt(packsList.size());
        for (PackInfo aPacksList : packsList) {
            PackInfo pack = aPacksList;
            out.writeObject(pack.getPack());
        }
        out.flush();
        primaryJarStream.closeEntry();
    }

    /**
     * Return a stream for the next jar.
     */
    private ZipOutputStream getJarOutputStream(String name) throws IOException {
        File file = new File(baseFile.getParentFile(), name);
        sendMsg("Building installer jar: " + file.getAbsolutePath());
        Debug.trace("Building installer jar: " + file.getAbsolutePath());
        ZipOutputStream jar = new ZipOutputStream(new FileOutputStream(file));
        jar.setLevel(Deflater.BEST_COMPRESSION);
        return jar;
    }

    /**
     * Copies specified contents of one jar to another.
     * <p/>
     * <p/>
     * TODO: it would be useful to be able to keep signature information from signed jar files, can
     * we combine manifests and still have their content signed?
     *
     * @see #copyStream(InputStream, OutputStream)
     */
    private void copyZip(ZipInputStream zin, ZipOutputStream out, List<String> files) throws IOException {
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
                ZipEntry newEntry = new ZipEntry(currentName);
                long fileTime = zentry.getTime();
                if (fileTime != -1) {
                    newEntry.setTime(fileTime);
                }
                out.putNextEntry(newEntry);
                copyStream(zin, out);
                out.closeEntry();
                zin.closeEntry();
                currentSet.add(currentName);
            } catch (ZipException x) {
            }
        }
    }

    /**
     * Copies specified contents of one jar to another without the specified files
     * <p/>
     * <p/>
     * TODO: it would be useful to be able to keep signature information from signed jar files, can
     * we combine manifests and still have their content signed?
     *
     * @see #copyStream(InputStream, OutputStream)
     */
    private void copyZipWithoutExcludes(ZipInputStream zin, ZipOutputStream out, List<String> excludes) throws IOException {
        java.util.zip.ZipEntry zentry;
        if (!alreadyWrittenFiles.containsKey(out)) {
            alreadyWrittenFiles.put(out, new HashSet<String>());
        }
        HashSet<String> currentSet = alreadyWrittenFiles.get(out);
        while ((zentry = zin.getNextEntry()) != null) {
            String currentName = zentry.getName();
            String testName = currentName.replace('/', '.');
            testName = testName.replace('\\', '.');
            if (excludes != null) {
                Iterator<String> i = excludes.iterator();
                boolean skip = false;
                while (i.hasNext()) {
                    String doExclude = i.next();
                    if (testName.matches(doExclude)) {
                        skip = true;
                        break;
                    }
                }
                if (skip) {
                    continue;
                }
            }
            if (currentSet.contains(currentName)) {
                continue;
            }
            try {
                ZipEntry newEntry = new ZipEntry(currentName);
                long fileTime = zentry.getTime();
                if (fileTime != -1) {
                    newEntry.setTime(fileTime);
                }
                out.putNextEntry(newEntry);
                copyStream(zin, out);
                out.closeEntry();
                zin.closeEntry();
                currentSet.add(currentName);
            } catch (ZipException x) {
            }
        }
    }

    /**
     * Copies all the data from the specified input stream to the specified output stream.
     *
     * @param in  the input stream to read
     * @param out the output stream to write
     * @return the total number of bytes copied
     * @throws IOException if an I/O error occurs
     */
    private long copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[5120];
        long bytesCopied = 0;
        int bytesInBuffer;
        while ((bytesInBuffer = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesInBuffer);
            bytesCopied += bytesInBuffer;
        }
        return bytesCopied;
    }

    public void addConfigurationInformation(IXMLElement data) {
        this.configdata = data;
    }

    protected void writePacks() throws Exception {
    }
}
