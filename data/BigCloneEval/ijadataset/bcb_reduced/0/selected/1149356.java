package org.crazydays.gameplan.db.io;

import java.beans.XMLEncoder;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import org.crazydays.gameplan.db.Database;
import org.crazydays.gameplan.map.GameMap;

/**
 * DatabaseOutputStream
 */
public class DatabaseOutputStream extends FilterOutputStream {

    /**
     * DatabaseOutputStream constructor.
     * 
     * @param stream Stream
     */
    public DatabaseOutputStream(OutputStream stream) {
        super(new ZipOutputStream(stream));
    }

    /**
     * Write entry to the output stream.
     * 
     * @param name Name
     * @param object Object
     * @throws IOException
     */
    protected void write(String name, Object object) throws IOException {
        if (name == null) {
            throw new IllegalArgumentException("name == null");
        }
        if (object == null) {
            throw new IllegalArgumentException("object == null");
        }
        ZipEntry entry = new ZipEntry(name);
        ((ZipOutputStream) out).putNextEntry(entry);
        XMLEncoder encoder = new XMLEncoder(new XMLEncoderOutputStreamWrapper(out));
        encoder.writeObject(object);
        encoder.close();
        ((ZipOutputStream) out).closeEntry();
    }

    /**
     * Write database.
     * 
     * @param database Database
     * @throws IOException
     */
    public void write(Database database) throws IOException {
        writeProperties(database);
        writeGameMaps(database);
    }

    /**
     * Write database properties.
     * 
     * @param database Database
     * @throws IOException
     */
    protected void writeProperties(Database database) throws IOException {
        Map<String, Object> properties = buildProperties(database);
        write("properties.xml", properties);
    }

    /**
     * Build list of database properties.
     * 
     * @param database Database
     * @return Properties
     */
    protected Map<String, Object> buildProperties(Database database) {
        if (database == null) {
            throw new IllegalArgumentException("database == null");
        }
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("name", database.getName());
        return properties;
    }

    /**
     * Write game maps.
     * 
     * @param database Database
     * @throws IOException
     */
    protected void writeGameMaps(Database database) throws IOException {
        if (database == null) {
            throw new IllegalArgumentException("database == null");
        }
        for (GameMap gameMap : database.getGameMaps()) {
            writeGameMap(gameMap);
        }
    }

    /**
     * Write game map.
     * 
     * @param gameMap GameMap
     * @throws IOException
     */
    protected void writeGameMap(GameMap gameMap) throws IOException {
        if (gameMap == null) {
            throw new IllegalArgumentException("gameMap == null");
        }
        writeGameMapImage(gameMap);
        write("gameMaps/" + gameMap.getName() + "/geometry.xml", gameMap.getGeometry());
        write("gameMaps/" + gameMap.getName() + "/locations.xml", gameMap.getLocations());
    }

    /**
     * Write game map image.
     * 
     * @param gameMap GameMap
     * @throws IOException
     */
    protected void writeGameMapImage(GameMap gameMap) throws IOException {
        if (gameMap == null) {
            throw new IllegalArgumentException("gameMap == null");
        }
        ZipEntry entry = new ZipEntry("gameMaps/" + gameMap.getName() + "/image.png");
        ((ZipOutputStream) out).putNextEntry(entry);
        ImageIO.write(gameMap.getImage(), "png", out);
        ((ZipOutputStream) out).closeEntry();
    }
}
