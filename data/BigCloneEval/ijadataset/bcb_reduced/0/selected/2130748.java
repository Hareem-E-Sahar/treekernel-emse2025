package org.argouml.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import org.argouml.application.api.Argo;
import org.argouml.application.helpers.ApplicationVersion;
import org.argouml.i18n.Translator;
import org.argouml.kernel.Project;
import org.argouml.kernel.ProjectFactory;
import org.argouml.kernel.ProjectMember;
import org.argouml.kernel.ProfileConfiguration;
import org.argouml.util.FileConstants;
import org.argouml.util.ThreadUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * To persist to and from zargo (zipped file) storage.
 *
 * @author Bob Tarling
 */
class ZargoFilePersister extends UmlFilePersister {

    /**
     * Logger.
     */
    private static final Logger LOG = Logger.getLogger(ZargoFilePersister.class);

    /**
     * The constructor.
     */
    public ZargoFilePersister() {
    }

    @Override
    public String getExtension() {
        return "zargo";
    }

    @Override
    protected String getDesc() {
        return Translator.localize("combobox.filefilter.zargo");
    }

    /**
     * It is being considered to save out individual xmi's from individuals
     * diagrams to make it easier to modularize the output of Argo.
     *
     * @param file
     *            The file to write.
     * @param project
     *            the project to save
     * @throws SaveException
     *             when anything goes wrong
     * @throws InterruptedException     if the thread is interrupted
     *
     * @see org.argouml.persistence.ProjectFilePersister#save(
     *      org.argouml.kernel.Project, java.io.File)
     */
    @Override
    public void doSave(Project project, File file) throws SaveException, InterruptedException {
        LOG.info("Saving");
        ProgressMgr progressMgr = new ProgressMgr();
        progressMgr.setNumberOfPhases(4);
        progressMgr.nextPhase();
        File lastArchiveFile = new File(file.getAbsolutePath() + "~");
        File tempFile = null;
        try {
            tempFile = createTempFile(file);
        } catch (FileNotFoundException e) {
            throw new SaveException("Failed to archive the previous file version", e);
        } catch (IOException e) {
            throw new SaveException("Failed to archive the previous file version", e);
        }
        BufferedWriter writer = null;
        try {
            project.setFile(file);
            project.setVersion(ApplicationVersion.getVersion());
            project.setPersistenceVersion(PERSISTENCE_VERSION);
            ZipOutputStream stream = new ZipOutputStream(new FileOutputStream(file));
            writer = new BufferedWriter(new OutputStreamWriter(stream, "UTF-8"));
            for (ProjectMember projectMember : project.getMembers()) {
                if (projectMember.getType().equalsIgnoreCase("xmi")) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Saving member of type: " + projectMember.getType());
                    }
                    stream.putNextEntry(new ZipEntry(projectMember.getZipName()));
                    MemberFilePersister persister = getMemberFilePersister(projectMember);
                    persister.save(projectMember, writer);
                }
            }
            if (lastArchiveFile.exists()) {
                lastArchiveFile.delete();
            }
            if (tempFile.exists() && !lastArchiveFile.exists()) {
                tempFile.renameTo(lastArchiveFile);
            }
            if (tempFile.exists()) {
                tempFile.delete();
            }
            progressMgr.nextPhase();
        } catch (Exception e) {
            LOG.error("Exception occured during save attempt", e);
            try {
                writer.close();
            } catch (Exception ex) {
            }
            file.delete();
            tempFile.renameTo(file);
            throw new SaveException(e);
        }
        try {
            writer.close();
        } catch (IOException ex) {
            LOG.error("Failed to close save output writer", ex);
        }
    }

    public boolean isSaveEnabled() {
        return false;
    }

    @Override
    public Project doLoad(File file) throws OpenException, InterruptedException {
        ProgressMgr progressMgr = new ProgressMgr();
        progressMgr.setNumberOfPhases(3 + UML_PHASES_LOAD);
        ThreadUtils.checkIfInterrupted();
        int fileVersion;
        String releaseVersion;
        try {
            String argoEntry = getEntryNames(file, ".argo").iterator().next();
            URL argoUrl = makeZipEntryUrl(toURL(file), argoEntry);
            fileVersion = getPersistenceVersion(argoUrl.openStream());
            releaseVersion = getReleaseVersion(argoUrl.openStream());
        } catch (MalformedURLException e) {
            throw new OpenException(e);
        } catch (IOException e) {
            throw new OpenException(e);
        }
        boolean upgradeRequired = true;
        LOG.info("Loading zargo file of version " + fileVersion);
        final Project p;
        if (upgradeRequired) {
            File combinedFile = zargoToUml(file, progressMgr);
            p = super.doLoad(file, combinedFile, progressMgr);
        } else {
            p = loadFromZargo(file, progressMgr);
        }
        progressMgr.nextPhase();
        p.setURI(file.toURI());
        return p;
    }

    private Project loadFromZargo(File file, ProgressMgr progressMgr) throws OpenException {
        Project p = ProjectFactory.getInstance().createProject(file.toURI());
        try {
            progressMgr.nextPhase();
            ArgoParser parser = new ArgoParser();
            String argoEntry = getEntryNames(file, ".argo").iterator().next();
            parser.readProject(p, new InputSource(makeZipEntryUrl(toURL(file), argoEntry).toExternalForm()));
            List memberList = parser.getMemberList();
            LOG.info(memberList.size() + " members");
            String xmiEntry = getEntryNames(file, ".xmi").iterator().next();
            MemberFilePersister persister = getMemberFilePersister("xmi");
            persister.load(p, makeZipEntryUrl(toURL(file), xmiEntry));
            List<String> entries = getEntryNames(file, null);
            for (String name : entries) {
                String ext = name.substring(name.lastIndexOf('.') + 1);
                if (!"argo".equals(ext) && !"xmi".equals(ext)) {
                    persister = getMemberFilePersister(ext);
                    LOG.info("Loading member with " + persister.getClass().getName());
                    persister.load(p, openZipEntry(toURL(file), name));
                }
            }
            progressMgr.nextPhase();
            ThreadUtils.checkIfInterrupted();
            p.postLoad();
            return p;
        } catch (InterruptedException e) {
            return null;
        } catch (MalformedURLException e) {
            throw new OpenException(e);
        } catch (IOException e) {
            throw new OpenException(e);
        } catch (SAXException e) {
            throw new OpenException(e);
        }
    }

    private URL toURL(File file) throws MalformedURLException {
        return file.toURI().toURL();
    }

    private File zargoToUml(File file, ProgressMgr progressMgr) throws OpenException, InterruptedException {
        File combinedFile = null;
        try {
            combinedFile = File.createTempFile("combinedzargo_", ".uml");
            LOG.info("Combining old style zargo sub files into new style uml file " + combinedFile.getAbsolutePath());
            combinedFile.deleteOnExit();
            String encoding = Argo.getEncoding();
            FileOutputStream stream = new FileOutputStream(combinedFile);
            PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(stream, encoding)));
            writer.println("<?xml version = \"1.0\" " + "encoding = \"" + encoding + "\" ?>");
            copyArgo(file, encoding, writer);
            progressMgr.nextPhase();
            copyMember(file, "profile", encoding, writer);
            copyXmi(file, encoding, writer);
            copyDiagrams(file, encoding, writer);
            copyMember(file, "todo", encoding, writer);
            progressMgr.nextPhase();
            writer.println("</uml>");
            writer.close();
            LOG.info("Completed combining files");
        } catch (IOException e) {
            throw new OpenException(e);
        }
        return combinedFile;
    }

    private void copyArgo(File file, String encoding, PrintWriter writer) throws IOException, MalformedURLException, OpenException, UnsupportedEncodingException {
        int pgmlCount = getPgmlCount(file);
        boolean containsToDo = containsTodo(file);
        boolean containsProfile = containsProfile(file);
        ZipInputStream zis = openZipStreamAt(toURL(file), FileConstants.PROJECT_FILE_EXT);
        if (zis == null) {
            throw new OpenException("There is no .argo file in the .zargo");
        }
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(zis, encoding));
        String rootLine;
        do {
            rootLine = reader.readLine();
            if (rootLine == null) {
                throw new OpenException("Can't find an <argo> tag in the argo file");
            }
        } while (!rootLine.startsWith("<argo"));
        String version = getVersion(rootLine);
        writer.println("<uml version=\"" + version + "\">");
        writer.println(rootLine);
        LOG.info("Transfering argo contents");
        int memberCount = 0;
        while ((line = reader.readLine()) != null) {
            if (line.trim().startsWith("<member")) {
                ++memberCount;
            }
            if (line.trim().equals("</argo>") && memberCount == 0) {
                LOG.info("Inserting member info");
                writer.println("<member type='xmi' name='.xmi' />");
                for (int i = 0; i < pgmlCount; ++i) {
                    writer.println("<member type='pgml' name='.pgml' />");
                }
                if (containsToDo) {
                    writer.println("<member type='todo' name='.todo' />");
                }
                if (containsProfile) {
                    String type = ProfileConfiguration.EXTENSION;
                    writer.println("<member type='" + type + "' name='." + type + "' />");
                }
            }
            writer.println(line);
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("Member count = " + memberCount);
        }
        zis.close();
        reader.close();
    }

    private void copyXmi(File file, String encoding, PrintWriter writer) throws IOException, MalformedURLException, UnsupportedEncodingException {
        ZipInputStream zis = openZipStreamAt(toURL(file), ".xmi");
        BufferedReader reader = new BufferedReader(new InputStreamReader(zis, encoding));
        reader.readLine();
        readerToWriter(reader, writer);
        zis.close();
        reader.close();
    }

    private void copyDiagrams(File file, String encoding, PrintWriter writer) throws IOException {
        ZipInputStream zis = new ZipInputStream(toURL(file).openStream());
        SubInputStream sub = new SubInputStream(zis);
        ZipEntry currentEntry = null;
        while ((currentEntry = sub.getNextEntry()) != null) {
            if (currentEntry.getName().endsWith(".pgml")) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(sub, encoding));
                String firstLine = reader.readLine();
                if (firstLine.startsWith("<?xml")) {
                    reader.readLine();
                } else {
                    writer.println(firstLine);
                }
                readerToWriter(reader, writer);
                sub.close();
                reader.close();
            }
        }
        zis.close();
    }

    private void copyMember(File file, String tag, String outputEncoding, PrintWriter writer) throws IOException, MalformedURLException, UnsupportedEncodingException {
        ZipInputStream zis = openZipStreamAt(toURL(file), "." + tag);
        if (zis != null) {
            InputStreamReader isr = new InputStreamReader(zis, outputEncoding);
            BufferedReader reader = new BufferedReader(isr);
            String firstLine = reader.readLine();
            if (firstLine.startsWith("<?xml")) {
                reader.readLine();
            } else {
                writer.println(firstLine);
            }
            readerToWriter(reader, writer);
            zis.close();
            reader.close();
        }
    }

    private void readerToWriter(Reader reader, Writer writer) throws IOException {
        int ch;
        while ((ch = reader.read()) != -1) {
            if (ch == 0xFFFF) {
                LOG.info("Stripping out 0xFFFF from save file");
            } else if (ch == 8) {
                LOG.info("Stripping out 0x8 from save file");
            } else {
                writer.write(ch);
            }
        }
    }

    /**
     * Open a ZipInputStream to the first file found with a given extension.
     *
     * @param url
     *            The URL of the zip file.
     * @param ext
     *            The required extension.
     * @return the zip stream positioned at the required location or null
     * if the requested extension is not found.
     * @throws IOException
     *             if there is a problem opening the file.
     */
    private ZipInputStream openZipStreamAt(URL url, String ext) throws IOException {
        ZipInputStream zis = new ZipInputStream(url.openStream());
        ZipEntry entry = zis.getNextEntry();
        while (entry != null && !entry.getName().endsWith(ext)) {
            entry = zis.getNextEntry();
        }
        if (entry == null) {
            zis.close();
            return null;
        }
        return zis;
    }

    private InputStream openZipEntry(URL url, String entryName) throws MalformedURLException, IOException {
        return makeZipEntryUrl(url, entryName).openStream();
    }

    private URL makeZipEntryUrl(URL url, String entryName) throws MalformedURLException {
        String entryURL = "jar:" + url + "!/" + entryName;
        return new URL(entryURL);
    }

    /**
     * A stream of input streams for reading the Zipped file.
     */
    private static class SubInputStream extends FilterInputStream {

        private ZipInputStream in;

        /**
         * The constructor.
         *
         * @param z
         *            the zip input stream
         */
        public SubInputStream(ZipInputStream z) {
            super(z);
            in = z;
        }

        @Override
        public void close() throws IOException {
            in.closeEntry();
        }

        /**
         * Reads the next ZIP file entry and positions stream at the beginning
         * of the entry data.
         *
         * @return the ZipEntry just read
         * @throws IOException
         *             if an I/O error has occurred
         */
        public ZipEntry getNextEntry() throws IOException {
            return in.getNextEntry();
        }
    }

    private int getPgmlCount(File file) throws IOException {
        return getEntryNames(file, ".pgml").size();
    }

    private boolean containsTodo(File file) throws IOException {
        return !getEntryNames(file, ".todo").isEmpty();
    }

    private boolean containsProfile(File file) throws IOException {
        return !getEntryNames(file, "." + ProfileConfiguration.EXTENSION).isEmpty();
    }

    /**
     * Get a list of zip file entries which end with the given extension.
     * If the extension is null, all entries are returned.
     */
    private List<String> getEntryNames(File file, String extension) throws IOException, MalformedURLException {
        ZipInputStream zis = new ZipInputStream(toURL(file).openStream());
        List<String> result = new ArrayList<String>();
        ZipEntry entry = zis.getNextEntry();
        while (entry != null) {
            String name = entry.getName();
            if (extension == null || name.endsWith(extension)) {
                result.add(name);
            }
            entry = zis.getNextEntry();
        }
        zis.close();
        return result;
    }
}
