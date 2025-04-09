package uk.ac.cam.caret.imscp.impl;

import uk.ac.cam.caret.imscp.api.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import uk.ac.cam.caret.tagphage.parser.*;
import uk.ac.cam.caret.minibix.taggy.*;
import org.apache.log4j.Logger;
import org.xml.sax.*;

public class ZipFilePackageParser implements PackageParser {

    private static final String MANIFEST_FILE = "imsmanifest.xml";

    private ContentPackageImpl cp;

    private ZipFilePackageFactory factory;

    private static Logger log = Logger.getLogger(ZipFilePackageParser.class);

    ZipFilePackageParser(ZipFilePackageFactory f) {
        factory = f;
    }

    private String[] splitPath(String in) {
        return in.split("/|\\\\");
    }

    private PackageDirectoryImpl getBottomDirectory(PackageDirectoryImpl root, String[] path) throws BadParseException {
        PackageDirectoryImpl out = root;
        for (int i = 0; i < path.length - 1; i++) out = out.getOrMake(path[i]);
        return out;
    }

    private Manifest parseManifest(final ContentPackage cp, InputStream in) throws IOException, BadParseException {
        try {
            ParserFactory pf = factory.getManifestParserFactory();
            pf.addInterception(new ParseStateInterceptor() {

                public void intercept(ParseState in) {
                    in.getObjectStack().push(new ManifestImpl(cp, factory));
                }
            });
            ParseState parser = pf.getParser();
            XMLReader reader = parser.getReader();
            reader.parse(new InputSource(in));
            Object out = parser.getObjectStack().pop();
            if (!(out instanceof Manifest)) throw new BadParseException("top level was not manifest");
            return (Manifest) out;
        } catch (BadConfigException x) {
            throw new AssertionError("bad rules file: badly built imscp jar");
        } catch (SAXException x) {
            throw new BadParseException("Could not parse manifest", x);
        }
    }

    private void addFile(PackageDirectoryImpl root, ZipFile file, ZipEntry entry) throws IOException, BadParseException {
        String[] path = splitPath(entry.getName());
        if ("imsmanifest.xml".equals(entry.getName())) return;
        PackageDirectoryImpl dir = getBottomDirectory(root, path);
        if (entry.isDirectory()) return;
        PackageFileImpl file_entry = new PackageFileImpl(dir, file, path[path.length - 1], entry.getName());
        dir.addFile(file_entry);
    }

    public void parse(File in) throws IOException, BadParseException {
        try {
            ZipFile file = new ZipFile(in);
            ZipEntry manifest = file.getEntry(MANIFEST_FILE);
            if (manifest == null) throw new BadParseException("Zip file contained no imsmanifest.xml");
            if (manifest.isDirectory()) throw new BadParseException("imsmanifest.xml is a directory");
            cp = new ContentPackageImpl();
            InputStream manifest_stream = file.getInputStream(manifest);
            cp.setRootManifest(parseManifest(cp, manifest_stream));
            manifest_stream.close();
            PackageDirectoryImpl root = new PackageDirectoryImpl(new Updates(), null, null);
            cp.setRootDirectory(root);
            for (Enumeration e = file.entries(); e.hasMoreElements(); ) addFile(root, file, (ZipEntry) e.nextElement());
        } catch (IOException x) {
            cp = null;
            throw x;
        } catch (BadParseException x) {
            cp = null;
            throw x;
        }
    }

    public void setContentPackage(ContentPackage in) {
        cp = (ContentPackageImpl) in;
    }

    public void serialize(File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        ZipOutputStream out = new ZipOutputStream(fos);
        out.setLevel(Deflater.DEFAULT_COMPRESSION);
        out.putNextEntry(new ZipEntry("imsmanifest.xml"));
        try {
            cp.getRootManifest().serialize(out);
        } catch (Unserializable x) {
            log.error("Taggy threw unserializable, should be impossible", x);
            throw new IOException();
        }
        out.closeEntry();
        ((PackageDirectoryImpl) cp.getRootDirectory()).serialize(out);
        out.close();
    }

    public void destroy() {
    }

    public ContentPackage getPackage() {
        return cp;
    }
}
