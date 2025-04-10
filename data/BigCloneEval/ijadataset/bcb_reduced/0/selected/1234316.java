package net.sf.beezle.sushi.archive;

import net.sf.beezle.sushi.fs.Node;
import net.sf.beezle.sushi.fs.World;
import net.sf.beezle.sushi.io.Buffer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/** Represents a Zip or Jar file. For Zip files, the manifest is null. */
public class Archive {

    public static final String META_INF = "META-INF";

    public static final String MANIFEST = META_INF + "/MANIFEST.MF";

    public static Archive createZip(World world) {
        return new Archive(world.getMemoryFilesystem().root().node("", null), null);
    }

    public static Archive loadZip(Node src) throws IOException {
        return createZip(src.getWorld()).read(src);
    }

    public static Archive createJar(World world) {
        return new Archive(world.getMemoryFilesystem().root().node("", null), new Manifest());
    }

    public static Archive loadJar(Node src) throws IOException {
        return createJar(src.getWorld()).read(src);
    }

    private static String getPath(ZipEntry entry) {
        String path;
        path = entry.getName();
        if (entry.isDirectory()) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    public final Node data;

    /** null for zip files, not null for jars */
    public final Manifest manifest;

    public Archive(Node data, Manifest manifest) {
        this.data = data;
        this.manifest = manifest;
    }

    /** @return this */
    public Archive read(Node file) throws IOException {
        Buffer buffer;
        ZipInputStream zip;
        ZipEntry entry;
        Node node;
        buffer = file.getWorld().getBuffer();
        zip = new ZipInputStream(file.createInputStream());
        while (true) {
            entry = zip.getNextEntry();
            if (entry == null) {
                break;
            }
            node = data.join(getPath(entry));
            if ("".equals(node.getPath())) {
                continue;
            }
            if (entry.isDirectory()) {
                node.mkdirsOpt();
            } else if (isManifest(node)) {
                mergeManifest(new Manifest(zip));
                zip.closeEntry();
            } else {
                node.getParent().mkdirsOpt();
                buffer.copy(zip, node);
                zip.closeEntry();
            }
        }
        zip.close();
        return this;
    }

    private boolean isManifest(Node node) {
        return manifest != null && MANIFEST.equals(node.getPath());
    }

    public void mergeManifest(Manifest rightManifest) {
        Map<String, Attributes> rightSections;
        Attributes left;
        manifest.getMainAttributes().putAll(rightManifest.getMainAttributes());
        rightSections = rightManifest.getEntries();
        for (String name : rightSections.keySet()) {
            left = manifest.getAttributes(name);
            if (left == null) {
                left = new Attributes();
                manifest.getEntries().put(name, left);
            }
            left.putAll(rightSections.get(name));
        }
    }

    public Archive save(Node dest) throws IOException {
        OutputStream out;
        out = dest.createOutputStream();
        save(out);
        out.close();
        return this;
    }

    public Archive save(OutputStream dest) throws IOException {
        ZipOutputStream out;
        InputStream in;
        List<Node> content;
        List<Node> files;
        out = new ZipOutputStream(dest);
        if (manifest != null) {
            out.putNextEntry(new ZipEntry(MANIFEST));
            manifest.write(out);
            out.closeEntry();
        }
        content = data.find("**/*");
        files = new ArrayList<Node>();
        for (Node node : content) {
            if (isManifest(node)) {
                throw new ArchiveException("manifest file not allowed");
            } else if (node.isFile()) {
                files.add(node);
            } else {
                out.putNextEntry(new ZipEntry(node.getPath() + "/"));
                out.closeEntry();
            }
        }
        for (Node file : files) {
            in = file.createInputStream();
            out.putNextEntry(new ZipEntry(file.getPath()));
            file.getWorld().getBuffer().copy(in, out);
            out.closeEntry();
            in.close();
        }
        out.close();
        return this;
    }
}
