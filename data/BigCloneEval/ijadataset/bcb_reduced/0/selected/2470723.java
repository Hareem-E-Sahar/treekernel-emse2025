package com.etracks.dades;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.xmlpull.v1.XmlSerializer;
import android.content.Context;
import android.util.Log;
import android.util.Xml;
import com.etracks.domini.Photo;
import com.etracks.domini.Placemark;
import com.etracks.domini.Punt;
import com.etracks.domini.Route;

/**
 * This class handles the creation of kmz.
 * 
 * @author Albert
 * 
 */
public class KmzAdapter {

    /**
	 * TAG used in the logs.
	 */
    private static String TAG = "KmzAdapter";

    /**
	 * Temporal folder.
	 */
    private static String DIR = "tmp";

    /**
	 * The execution context.
	 */
    private Context ctx;

    /**
	 * Constructor of the class.
	 * 
	 * @param c
	 *            The execution context.
	 */
    public KmzAdapter(Context c) {
        ctx = c;
    }

    /**
	 * Prepare a temporal kmz.
	 * 
	 * @param r
	 *            Identifier of the Route.
	 * @return The kmz File.
	 */
    public File getTmpKmz(Route r) {
        File dir = ctx.getDir(DIR, Context.MODE_PRIVATE);
        deleteTmpFiles(dir);
        String name = r.getName().replace(' ', '_');
        File tmpFile = new File(dir + "/" + name + ".kmz");
        generateKMZ(tmpFile, r);
        return tmpFile;
    }

    /**
	 * Delete the temporal directory.
	 */
    public void deleteTmpFile() {
        File dir = ctx.getDir(DIR, Context.MODE_PRIVATE);
        deleteTmpFiles(dir);
    }

    /**
	 * Generate a kmz file of the specified route.
	 * 
	 * @param kmzFile
	 *            Kmz File.
	 * @param r
	 *            Route.
	 */
    public void generateKMZ(File kmzFile, Route r) {
        byte[] buf = new byte[2048];
        try {
            kmzFile.createNewFile();
            ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(kmzFile));
            zipFile.putNextEntry(new ZipEntry("doc.kml"));
            writeKml(zipFile, r);
            zipFile.closeEntry();
            ListIterator<Photo> iter = r.getPhotos().listIterator();
            while (iter.hasNext()) {
                String p = iter.next().getPath();
                if (p != null) {
                    File f = new File(p);
                    String name = "files/" + f.getName();
                    zipFile.putNextEntry(new ZipEntry(name));
                    FileInputStream in = new FileInputStream(f);
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        zipFile.write(buf, 0, len);
                    }
                    zipFile.closeEntry();
                }
            }
            zipFile.flush();
            zipFile.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void writeKml(ZipOutputStream fileos, Route r) {
        XmlSerializer serializer = Xml.newSerializer();
        try {
            serializer.setOutput(fileos, "UTF-8");
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startTag(null, "kml");
            serializer.attribute(null, "xmlns", "http://www.opengis.net/kml/2.2");
            serializer.startTag(null, "Document");
            serializer.startTag(null, "name");
            serializer.text(r.getName());
            serializer.endTag(null, "name");
            serializer.startTag(null, "description");
            serializer.text(r.getDescription());
            serializer.endTag(null, "description");
            serializer.startTag(null, "Placemark");
            serializer.startTag(null, "name");
            serializer.text(r.getName());
            serializer.endTag(null, "name");
            serializer.startTag(null, "description");
            serializer.text(r.getDescription());
            serializer.endTag(null, "description");
            serializer.startTag(null, "LineString");
            serializer.startTag(null, "extrude");
            serializer.text("1");
            serializer.endTag(null, "extrude");
            serializer.startTag(null, "altitudeMode");
            serializer.text("absolute");
            serializer.endTag(null, "altitudeMode");
            serializer.startTag(null, "coordinates");
            serializer.text(prepareCoords(r));
            serializer.endTag(null, "coordinates");
            serializer.endTag(null, "LineString");
            serializer.endTag(null, "Placemark");
            ListIterator<Photo> li = r.getPhotos().listIterator();
            while (li.hasNext()) {
                Photo p = li.next();
                serializer.startTag(null, "PhotoOverlay");
                serializer.startTag(null, "name");
                serializer.text(p.getDesc());
                serializer.endTag(null, "name");
                serializer.startTag(null, "description");
                serializer.text(p.getDesc());
                serializer.endTag(null, "description");
                if (p.getPath() != null) {
                    serializer.startTag(null, "Icon");
                    serializer.startTag(null, "href");
                    File f = new File(p.getPath());
                    serializer.text("files/" + f.getName());
                    serializer.endTag(null, "href");
                    serializer.endTag(null, "Icon");
                }
                serializer.startTag(null, "Point");
                serializer.startTag(null, "coordinates");
                serializer.text(p.getLon() + "," + p.getLat() + "," + p.getAltitude());
                serializer.endTag(null, "coordinates");
                serializer.endTag(null, "Point");
                serializer.endTag(null, "PhotoOverlay");
            }
            ListIterator<Placemark> li2 = r.getPlacemarks().listIterator();
            while (li2.hasNext()) {
                Placemark p = li2.next();
                serializer.startTag(null, "Placemark");
                serializer.startTag(null, "name");
                serializer.text(p.getName());
                serializer.endTag(null, "name");
                serializer.startTag(null, "description");
                serializer.text(p.getDesc());
                serializer.endTag(null, "description");
                serializer.startTag(null, "Point");
                serializer.startTag(null, "coordinates");
                serializer.text(p.getLon() + "," + p.getLat() + "," + p.getAltitude());
                serializer.endTag(null, "coordinates");
                serializer.endTag(null, "Point");
                serializer.endTag(null, "Placemark");
            }
            serializer.endTag(null, "Document");
            serializer.endTag(null, "kml");
            serializer.endDocument();
            serializer.flush();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private String prepareCoords(Route r) {
        ArrayList<Punt> a = r.getPunts();
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < a.size(); i++) {
            Punt p = a.get(i);
            s.append(p.getLon() + "," + p.getLat() + ",");
            s.append(p.getAltitude() + "\n");
        }
        return s.toString();
    }

    private void deleteTmpFiles(File dir) {
        File fs[] = dir.listFiles();
        for (int i = 0; i < fs.length; i++) fs[i].delete();
    }
}
