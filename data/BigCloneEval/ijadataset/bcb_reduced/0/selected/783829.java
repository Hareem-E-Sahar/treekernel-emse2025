package photospace.meta;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import org.apache.commons.io.*;
import org.apache.commons.logging.*;
import org.w3c.tools.jpeg.*;
import com.drew.imaging.jpeg.*;
import com.drew.metadata.*;
import com.drew.metadata.jpeg.*;
import com.hp.hpl.jena.rdf.model.*;
import photospace.beans.*;
import photospace.meta.rdf.*;
import photospace.vfs.FileSystem;

public class PersisterImpl implements java.io.Serializable, Persister {

    private static final Log log = LogFactory.getLog(PersisterImpl.class);

    public static final String RDF_EXT = ".rdf";

    private FileSystem filesystem;

    private Translator translator;

    private boolean storingInJpeg = false;

    public Meta getMeta(String path) throws IOException, JpegProcessingException, MetadataException, ParseException {
        Meta meta = getMeta(filesystem.getFile(path));
        meta.setPath(path);
        return meta;
    }

    public Meta getMeta(File file) throws IOException, JpegProcessingException, MetadataException, ParseException {
        file = getMetaFile(file.getCanonicalFile());
        Meta meta;
        try {
            if (rdfIsInJpeg(file)) {
                Metadata exif = getExif(file);
                String comments = getRdfComments(exif);
                if (comments == null) {
                    meta = translator.fromExif(exif);
                } else {
                    meta = translator.fromRdf(getRdf(comments));
                    if (new Date(file.lastModified()).after(meta.getUpdated())) {
                        Meta fromExif = translator.fromExif(exif);
                        Beans.merge(fromExif, meta);
                        if (!meta.isLocated() && fromExif.isLocated()) meta.setPosition(fromExif.getPosition());
                        ((PhotoMeta) meta).setWidth(((PhotoMeta) fromExif).getWidth());
                        ((PhotoMeta) meta).setHeight(((PhotoMeta) fromExif).getHeight());
                    }
                }
            } else {
                Model rdf = getRdfFromFile(file);
                if (rdf == null) {
                    if (isJpeg(file)) {
                        Metadata exif = getExif(file);
                        meta = translator.fromExif(exif);
                    } else if (file.isDirectory()) {
                        meta = new FolderMeta();
                    } else {
                        throw new IllegalStateException("Not sure what to do with " + file);
                    }
                } else {
                    meta = translator.fromRdf(rdf);
                }
            }
        } catch (MetadataException e) {
            throw new MetadataException("Exception getting EXIF metadata for " + file, e);
        }
        meta.setName(file.getName());
        meta.setPath(getPath(file));
        return meta;
    }

    public File getMetaFile(File file) {
        if (file.getName().equals("folder" + RDF_EXT)) return file.getParentFile(); else if (file.getName().endsWith(RDF_EXT)) return new File(file.getPath().substring(0, file.getPath().length() - RDF_EXT.length())); else return file;
    }

    public String getPath(File file) {
        return filesystem.getPath(getMetaFile(file));
    }

    public void saveMeta(String path, Meta meta) throws IOException, FileNotFoundException {
        saveMeta(filesystem.getFile(path), meta);
    }

    public void saveMeta(File file, Meta meta) throws IOException, FileNotFoundException {
        meta.setUpdated(new Date());
        if (meta.getPath() == null) meta.setPath(getPath(file.getAbsoluteFile()));
        log.info(("Saving " + meta));
        ByteArrayOutputStream rdf = new ByteArrayOutputStream();
        translator.toRdf(meta).write(rdf);
        if (rdfIsInJpeg(file)) {
            saveRdfToJpeg(file, rdf.toString());
        } else {
            saveRdfToFile(file, rdf.toString());
        }
    }

    private void saveRdfToFile(File file, String rdf) throws IOException {
        FileWriter writer = new FileWriter(getRdfFile(file));
        try {
            writer.write(rdf);
        } finally {
            writer.close();
        }
    }

    private void saveRdfToJpeg(File jpeg, String rdf) throws IOException {
        writeJpegComment(jpeg, rdf);
    }

    public void writeJpegComment(File jpeg, String rdf) throws IOException {
        ByteArrayOutputStream jpegOS = new ByteArrayOutputStream();
        JpegCommentWriter jcw = new JpegCommentWriter(jpegOS, new FileInputStream(jpeg));
        try {
            jcw.write(rdf);
        } finally {
            jcw.close();
        }
        FileOutputStream fos = new FileOutputStream(jpeg);
        try {
            fos.write(jpegOS.toByteArray());
        } finally {
            fos.close();
        }
    }

    private File getRdfFile(File file) {
        if (file.isDirectory()) return new File(file.getPath(), "folder" + RDF_EXT);
        if (file.getName().toLowerCase().endsWith(RDF_EXT)) return file;
        return new File(file.getPath() + RDF_EXT);
    }

    public Metadata getExif(File jpeg) throws FileNotFoundException, IOException, JpegProcessingException {
        InputStream in = new FileInputStream(jpeg);
        try {
            return getExif(in);
        } finally {
            in.close();
        }
    }

    public Metadata getExif(InputStream in) throws JpegProcessingException {
        return JpegMetadataReader.readMetadata(in);
    }

    public void remove(Meta meta) throws IOException, JpegProcessingException {
        File file = filesystem.getFile(meta.getPath());
        removeRdf(file);
        FileUtils.forceDelete(file);
    }

    public void removeRdf(File file) throws IOException, JpegProcessingException {
        if (rdfIsInJpeg(file)) {
            writeJpegComment(file, "");
        } else {
            File rdf = getRdfFile(file);
            if (rdf.exists()) FileUtils.forceDelete(rdf);
        }
    }

    public Model getRdf(File file) throws IOException, JpegProcessingException {
        if (rdfIsInJpeg(file)) {
            return getRdfFromJpeg(file);
        } else {
            return getRdfFromFile(file);
        }
    }

    private Model getRdfFromFile(File file) throws MalformedURLException {
        File rdfFile = getRdfFile(file);
        if (!rdfFile.exists()) return null;
        Model rdf = ModelFactory.createDefaultModel();
        rdf.read(rdfFile.toURL().toString());
        return rdf;
    }

    private Model getRdfFromJpeg(File jpeg) throws JpegProcessingException, IOException {
        InputStream in = new FileInputStream(jpeg);
        Metadata exif;
        try {
            exif = getExif(in);
        } finally {
            in.close();
        }
        String comments = getRdfComments(exif);
        if (comments == null) return null;
        return getRdf(comments);
    }

    private boolean rdfIsInJpeg(File file) {
        return isStoringInJpeg() && isJpeg(file);
    }

    private boolean isJpeg(File file) {
        return file.getName().toLowerCase().endsWith(".jpg") || file.getName().toLowerCase().endsWith(".jpeg");
    }

    private String getRdfComments(Metadata exif) {
        return RdfTools.getRdfString(exif.getDirectory(JpegCommentDirectory.class).getString(JpegCommentDirectory.TAG_JPEG_COMMENT));
    }

    private Model getRdf(String str) {
        Model rdf = ModelFactory.createDefaultModel();
        rdf.read(new StringReader(str), "");
        return rdf;
    }

    private boolean isStoringInJpeg() {
        return storingInJpeg;
    }

    public void setStoringInJpeg(boolean storingInJpeg) {
        this.storingInJpeg = storingInJpeg;
    }

    public void setFilesystem(FileSystem filesystem) {
        this.filesystem = filesystem;
    }

    public void setTranslator(Translator translator) {
        this.translator = translator;
    }
}
