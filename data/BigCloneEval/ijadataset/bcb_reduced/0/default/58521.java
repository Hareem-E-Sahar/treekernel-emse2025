import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.awt.image.*;
import java.net.*;
import javax.imageio.ImageIO;

/**
 * This subclass of GDataSource retreives data specific to
 * Google's satellite database.
 * @author bill
 */
public class GDataSourceSatellite extends GDataSource {

    /**
    * Constructor for GDataSourceSatellite
    * @param cacheSatellite The directory in which to save data.
    */
    public GDataSourceSatellite(String cacheDirectory) {
        super(cacheDirectory);
    }

    /**
    * Create a String representing the path to this image in Google's satellite database.
    * @param x The horizontal cooridinate
    * @param y The vertical coordinate
    * @param zoom The zoom level
    * @return The path.
    */
    protected String makeRemoteName(int x, int y, int zoom) {
        int serverNumber = (int) Math.round(Math.random() * 3.0);
        String pathToNode = makeRemoteSatName(x, y, zoom);
        return "http://kh" + serverNumber + ".google.com/kh?n=404&v=18&t=" + pathToNode;
    }

    /**
    * This method returns the string representing the path through the tree to
    * the correct child node representing the desired map image.
    * @param x The horizontal cooridinate
    * @param y The vertical coordinate
    * @param zoom The zoom level
    * @return The path.
    */
    private String makeRemoteSatName(int x, int y, int zoom) {
        int ServerNumber = (int) Math.round(Math.random() * 3.0);
        char[] sat = new char[20];
        int i = 1;
        int curZoom = 16 - (zoom - 1);
        int midxTiles = (int) Math.pow(2, (curZoom - 1));
        int midyTiles = midxTiles;
        int maxxTiles = midxTiles * 2;
        int maxyTiles = midyTiles * 2;
        int minxTiles = 0;
        int minyTiles = 0;
        StringBuffer sb = new StringBuffer();
        sb.append("t");
        BufferedImage currentImage;
        while (curZoom != 0) {
            if (x >= 0 && y >= 0) {
                if (x >= midxTiles) {
                    minxTiles = midxTiles;
                    if (y >= midyTiles) {
                        sat[i] = 's';
                        minyTiles = midyTiles;
                    } else {
                        sat[i] = 'r';
                        maxyTiles = midyTiles;
                    }
                } else if (x < midxTiles) {
                    maxxTiles = midxTiles;
                    if (y >= midyTiles) {
                        sat[i] = 't';
                        minyTiles = midyTiles;
                    } else {
                        sat[i] = 'q';
                        maxyTiles = midyTiles;
                    }
                }
                midxTiles = minxTiles + (maxxTiles - minxTiles) / 2;
                midyTiles = minyTiles + (maxyTiles - minyTiles) / 2;
                String s = new Character(sat[i]).toString();
                sb.append(s);
                i++;
                curZoom--;
            } else {
            }
        }
        String satImage = sb.toString();
        return satImage;
    }
}
