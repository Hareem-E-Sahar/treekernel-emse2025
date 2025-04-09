package org.xith3d.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.xith3d.scenegraph.Geometry;
import org.xith3d.scenegraph.SceneGraphObject;
import org.xith3d.utility.logging.X3DLog;

/**
 * Portable reader/writer for all cosm objects. This does not use object
 * serialization for efficiency. This is designed to be fast and portable.
 * Objects which implement Scribable can be written directly to a scribestream
 * 
 * 
 * :Id: Scribe.java,v 1.7 2003/02/24 00:13:44 wurp Exp $
 * 
 * :Log: Scribe.java,v $ Revision 1.7 2003/02/24 00:13:44 wurp Formatted all
 * java code for cvs (strictSunConvention.xml)
 * 
 * Revision 1.6 2002/09/11 00:53:53 dilvish Big commit of new changes
 * 
 * Revision 1.5 2002/01/20 05:26:02 dilvish JDK 1.4 port
 * 
 * Revision 1.4 2002/01/15 03:36:55 dilvish Improvements to the animation system
 * 
 * Revision 1.3 2001/07/14 14:48:10 wurp New animation, sky and avatar movement
 * 
 * Revision 1.2 2001/06/20 04:05:41 wurp added log4j.
 * 
 * Revision 1.1 2001/06/06 22:42:37 wizofid Massive commit / reintegration
 * 
 * @author David Yazel
 */
public class Scribe {

    public static final byte SCRIBE_GEOMETRY_ARRAY = 1;

    public static final byte SCRIBE_EXT = 2;

    public static boolean useNIO = true;

    public Scribe() {
    }

    /**
     * Method for reading a scene graph from a stream. Object(s) must have been
     * written by using the scribe method.
     */
    public static SceneGraphObject read(ScribeInputStream in) throws IOException, InvalidFormat {
        Scribable s = in.readScribable();
        if (!(s instanceof SceneGraphObject)) {
            throw new InvalidFormat();
        }
        return ((SceneGraphObject) s);
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
            X3DLog.print(e);
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

    public static void writeGeometryToFile(String filename, Geometry g) throws IOException {
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
    public static SceneGraphObject readSceneFromFile(String filename) throws IOException, InvalidFormat {
        ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(filename)));
        zip.getNextEntry();
        ScribeInputStream in = new ScribeInputStream(zip);
        SceneGraphObject object = (SceneGraphObject) in.readScribable();
        return (object);
    }

    public static Scribable read(String filename) throws IOException, InvalidFormat {
        ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(filename)));
        zip.getNextEntry();
        ScribeInputStream in = new ScribeInputStream(zip);
        Scribable object = in.readScribable();
        return (object);
    }

    public static Scribable readHeader(String filename) throws IOException, InvalidFormat {
        ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(filename)));
        zip.getNextEntry();
        ScribeInputStream in = new ScribeInputStream(zip);
        in.setReadHeaderOnly(true);
        Scribable object = in.readScribable();
        return (object);
    }

    public static Geometry readGeometryFromFile(String filename) throws IOException {
        ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(filename)));
        zip.getNextEntry();
        ScribeInputStream in = new ScribeInputStream(zip);
        Geometry g = ScribeGeometryArray.readGeometryArray(in);
        return (g);
    }
}
