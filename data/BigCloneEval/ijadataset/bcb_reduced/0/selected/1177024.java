package com.xith3d.io;

import com.xith3d.utility.logs.*;
import com.xith3d.scenegraph.*;
import org.apache.log4j.Category;
import java.io.*;
import java.util.zip.*;
import javax.vecmath.*;

public class Scribe {

    public static final Category LOG = Category.getInstance(Scribe.class.getName());

    public static final byte SCRIBE_GEOMETRY_ARRAY = 1;

    public static final byte SCRIBE_EXT = 2;

    public static boolean useNIO = true;

    public Scribe() {
    }

    /**
     * Method for reading a scene graph from a stream.  Object(s) must have been
     * written by using the scribe method.
     */
    public static SceneGraphObject read(ScribeInputStream in) throws IOException, InvalidFormat {
        Scribable s = in.readScribable();
        if (!(s instanceof SceneGraphObject)) {
            throw new InvalidFormat();
        }
        return (SceneGraphObject) s;
    }

    /**
     * Writes a scene to a compressed file.
     */
    public static void writeSceneToFile(String filename, Scribable object) throws IOException {
        ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
        zip.putNextEntry(new ZipEntry("object"));
        ScribeOutputStream out = new ScribeOutputStream(zip);
        try {
            out.writeScribable(object);
        } catch (UnscribableNodeEncountered e) {
            Log.log.print(e);
            throw new IOException(e.getMessage());
        }
        out.flush();
        out.close();
    }

    public static void write(String filename, Scribable object) throws IOException {
        ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
        zip.putNextEntry(new ZipEntry("object"));
        ScribeOutputStream out = new ScribeOutputStream(zip);
        try {
            out.writeScribable(object);
        } catch (UnscribableNodeEncountered e) {
            throw new IOException(e.getMessage());
        }
        out.flush();
        out.close();
    }

    public static void writeGeometryToFile(String filename, GeometryArray g) throws IOException {
        ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
        zip.putNextEntry(new ZipEntry("object"));
        ScribeOutputStream out = new ScribeOutputStream(zip);
        ScribeGeometryArray.writeGeometryArray(out, g);
        out.flush();
        out.close();
    }

    /**
     * Simple method to read a scene from a compressed file
     */
    public static SceneGraphObject readSceneFromFile(String filename) throws IOException, ClassNotFoundException, InvalidFormat {
        ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(filename)));
        zip.getNextEntry();
        ScribeInputStream in = new ScribeInputStream(zip);
        SceneGraphObject object = (SceneGraphObject) in.readScribable();
        return object;
    }

    public static Scribable read(String filename) throws IOException, ClassNotFoundException, InvalidFormat {
        ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(filename)));
        zip.getNextEntry();
        ScribeInputStream in = new ScribeInputStream(zip);
        Scribable object = in.readScribable();
        return object;
    }

    public static Scribable readHeader(String filename) throws IOException, ClassNotFoundException, InvalidFormat {
        ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(filename)));
        zip.getNextEntry();
        ScribeInputStream in = new ScribeInputStream(zip);
        in.setReadHeaderOnly(true);
        Scribable object = in.readScribable();
        return object;
    }

    public static GeometryArray readGeometryFromFile(String filename) throws IOException, ClassNotFoundException, InvalidFormat {
        ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(filename)));
        zip.getNextEntry();
        ScribeInputStream in = new ScribeInputStream(zip);
        GeometryArray g = (GeometryArray) ScribeGeometryArray.readGeometryArray(in);
        return g;
    }
}
