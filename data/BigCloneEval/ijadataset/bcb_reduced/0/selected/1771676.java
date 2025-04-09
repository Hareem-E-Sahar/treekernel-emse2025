package net.cryff.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import net.cryff.event.MapEvent;
import net.cryff.map.Map;
import net.cryff.map.Parser;

/**
 * The <code>CffMapStandard</code> class 
 * stores all information that is needed to
 * draw a map. 
 * Currently it saves:
 * the mapfile that stores information about the TileMaps and ChipSets
 *<br>
 * MapEvent object in serialized form.
 * <br>
 * 
 * @author Nino Wagensonner
 * @version 0.3 06/05/2008
 * @since CFF V.0.1r-2
 */
public class CffMapStandard {

    /**
		 * this method saves a Map object into a file
		 * it does not only serializable the object but extracts the data 
		 * of every tile, event, chipset etc and saves them in a
		 * zip file to reduce the disk space that is needed and also
		 * to faster send it over the internet. 
		 * 
		 * @param map the map object that will be saved
		 * @param path the path to where the file is saved
		 * @throws IOException if something goes wrong....
		 */
    public static void saveMap(Map map, String path) throws IOException {
        ObjectOutputStream oout = null;
        ArrayList<MapEvent> events = map.getEvents();
        FileOutputStream t = new FileOutputStream(path);
        CheckedOutputStream csum = new CheckedOutputStream(t, new Adler32());
        BufferedOutputStream bos = new BufferedOutputStream(csum);
        ZipOutputStream zipper = new ZipOutputStream(bos);
        LinkedList<String> toWrite = Parser.getMapInformation(map);
        ZipEntry mapfile = new ZipEntry("mapfile.cnf");
        zipper.putNextEntry(mapfile);
        for (int i = 0; i < toWrite.size(); i++) {
            zipper.write(toWrite.get(i).getBytes());
            zipper.write(System.getProperty("line.separator").getBytes());
        }
        zipper.closeEntry();
        for (int i = 0; i < events.size(); i++) {
            ZipEntry test = new ZipEntry(events.get(i).getFilename());
            zipper.putNextEntry(test);
            oout = new ObjectOutputStream(zipper);
            oout.writeObject(events.get(i));
            zipper.closeEntry();
        }
        if (oout != null) {
            oout.flush();
            oout.close();
        } else {
            zipper.close();
        }
    }

    /**
		 * this method loads a map object that was saved with the
		 * {@link #saveMap(Map, String)} function. 
		 * @param input path to the map file
		 * @return a map object with the loaded values of the file
		 * @throws IOException if the map cannot be read correctly...
		 */
    public static Map getMap(String input) throws IOException {
        ArrayList<MapEvent> data = new ArrayList<MapEvent>();
        ZipFile t = new ZipFile(input);
        Enumeration test = t.entries();
        ObjectInputStream oin = null;
        Map map = null;
        while (test.hasMoreElements()) {
            ZipEntry temp = (ZipEntry) test.nextElement();
            InputStream is = t.getInputStream(temp);
            if (temp.getName().equals("mapfile.cnf")) {
                LinkedList<String> map_data = new LinkedList<String>();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line = "";
                while (true) {
                    line = br.readLine();
                    if (line == null) break;
                    if (!line.startsWith("#")) {
                        map_data.add(line);
                    }
                }
                map = Parser.parseMap(map_data);
            } else {
                oin = new ObjectInputStream(new BufferedInputStream(is));
                try {
                    Object o = oin.readObject();
                    if (o instanceof MapEvent) {
                        MapEvent e = (MapEvent) o;
                        data.add(e);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        if (oin != null) oin.close();
        map.setEventData(data);
        return map;
    }
}
