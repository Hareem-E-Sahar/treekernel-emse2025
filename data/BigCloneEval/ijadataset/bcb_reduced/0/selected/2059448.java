package org.openstreetmap.gui.jmapviewer;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.openstreetmap.gui.jmapviewer.interfaces.TileImageCache;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderJobCreator;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;

/**
 * A {@link TileLoaderJobCreator} implementation that loads tiles from OSM via HTTP.
 * 
 * @author Jan Peter Stotz
 */
public class OsmTileLoader implements TileLoaderJobCreator {

    /**
     * Holds the used user agent used for HTTP requests. If this field is 
     * <code>null</code>, the default Java user agent is used.
     */
    public static String USER_AGENT = null;

    public static String ACCEPT = "text/html, image/png, image/jpeg, image/gif, */*";

    protected TileLoaderListener listener;

    public OsmTileLoader(TileLoaderListener listener) {
        this.listener = listener;
    }

    public Runnable createTileLoaderJob(final MapSource source, final int tilex, final int tiley, final int zoom) {
        return new Runnable() {

            InputStream input = null;

            public void run() {
                TileImageCache cache = listener.getTileImageCache();
                Tile tile;
                synchronized (cache) {
                    tile = cache.getTile(source, tilex, tiley, zoom);
                    if (tile == null || tile.isLoaded() || tile.loading) return;
                    tile.loading = true;
                }
                try {
                    input = loadTileFromOsm(tile).getInputStream();
                    tile.loadImage(input);
                    tile.setLoaded(true);
                    listener.tileLoadingFinished(tile, true);
                    input.close();
                    input = null;
                } catch (Exception e) {
                    tile.setImage(Tile.ERROR_IMAGE);
                    listener.tileLoadingFinished(tile, false);
                    if (input == null) System.err.println("failed loading " + zoom + "/" + tilex + "/" + tiley + " " + e.getMessage());
                } finally {
                    tile.loading = false;
                    tile.setLoaded(true);
                }
            }
        };
    }

    protected HttpURLConnection loadTileFromOsm(Tile tile) throws IOException {
        URL url;
        url = new URL(tile.getUrl());
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        prepareHttpUrlConnection(urlConn);
        urlConn.setReadTimeout(30000);
        return urlConn;
    }

    protected void prepareHttpUrlConnection(HttpURLConnection urlConn) {
        if (USER_AGENT != null) urlConn.setRequestProperty("User-agent", USER_AGENT);
        urlConn.setRequestProperty("Accept", ACCEPT);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
