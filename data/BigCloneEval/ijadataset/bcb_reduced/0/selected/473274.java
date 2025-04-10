package mobac.tools;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import mobac.program.download.TileDownLoader;
import mobac.program.interfaces.HttpMapSource;
import mobac.program.interfaces.HttpMapSource.TileUpdate;
import mobac.program.interfaces.MapSpace;
import mobac.program.model.EastNorthCoordinate;
import mobac.program.model.Settings;
import mobac.program.model.TileImageType;
import mobac.utilities.Utilities;
import org.apache.log4j.Logger;

public class MapSourceCapabilityDetector {

    public static final Logger log = Logger.getLogger(MapSourceCapabilityDetector.class);

    private final HttpMapSource mapSource;

    private final EastNorthCoordinate coordinate;

    private final int zoom;

    private URL url;

    private HttpURLConnection c;

    private boolean success = false;

    private Exception error = null;

    private boolean eTagPresent = false;

    private boolean expirationTimePresent = false;

    private boolean lastModifiedTimePresent = false;

    private boolean ifNoneMatchSupported = false;

    private boolean ifModifiedSinceSupported = false;

    private String contentType = "?";

    public MapSourceCapabilityDetector(Class<? extends HttpMapSource> mapSourceClass, EastNorthCoordinate coordinate, int zoom) throws InstantiationException, IllegalAccessException {
        this(mapSourceClass.newInstance(), coordinate, zoom);
    }

    public MapSourceCapabilityDetector(HttpMapSource mapSource, EastNorthCoordinate coordinate, int zoom) {
        this.mapSource = mapSource;
        if (mapSource == null) throw new NullPointerException("MapSource not set");
        this.coordinate = coordinate;
        this.zoom = zoom;
    }

    public void testMapSource() {
        try {
            log.debug("Testing " + mapSource.toString());
            MapSpace mapSpace = mapSource.getMapSpace();
            int tilex = mapSpace.cLonToX(coordinate.lon, zoom) / mapSpace.getTileSize();
            int tiley = mapSpace.cLatToY(coordinate.lat, zoom) / mapSpace.getTileSize();
            c = mapSource.getTileUrlConnection(zoom, tilex, tiley);
            url = c.getURL();
            log.trace("Sample url: " + c.getURL());
            log.trace("Connecting...");
            c.setReadTimeout(10000);
            c.addRequestProperty("User-agent", Settings.getInstance().getUserAgent());
            c.setRequestProperty("Accept", TileDownLoader.ACCEPT);
            c.connect();
            log.debug("Connection established - response HTTP " + c.getResponseCode());
            if (c.getResponseCode() != 200) return;
            byte[] content = Utilities.getInputBytes(c.getInputStream());
            TileImageType detectedContentType = Utilities.getImageType(content);
            contentType = c.getContentType();
            contentType = contentType.substring(6);
            if ("png".equals(contentType)) contentType = "png"; else if ("jpeg".equals(contentType)) contentType = "jpg"; else contentType = "unknown: " + c.getContentType();
            if (contentType.equals(detectedContentType.getFileExt())) contentType += " (verified)"; else contentType += " (unverified)";
            log.debug("Image format          : " + contentType);
            String eTag = c.getHeaderField("ETag");
            Utilities.checkForInterruption();
            eTagPresent = (eTag != null);
            if (eTagPresent) {
                testIfNoneMatch(content);
            }
            long exp = c.getExpiration();
            expirationTimePresent = (c.getHeaderField("expires") != null) && (exp != 0);
            if (exp == 0) {
            } else {
            }
            long modified = c.getLastModified();
            lastModifiedTimePresent = (c.getHeaderField("last-modified") != null) && (modified != 0);
            Utilities.checkForInterruption();
            testIfModified();
            success = true;
        } catch (Exception e) {
            this.error = e;
            log.error("", e);
        }
    }

    private void testIfNoneMatch(byte[] content) throws Exception {
        String eTag = c.getHeaderField("ETag");
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] digest = md5.digest(content);
        String hexDigest = getHexString(digest);
        if (hexDigest.equals(eTag)) log.debug("eTag content          : md5 hex string");
        String quotedHexDigest = "\"" + hexDigest + "\"";
        if (quotedHexDigest.equals(eTag)) log.debug("eTag content          : quoted md5 hex string");
        HttpURLConnection c2 = (HttpURLConnection) url.openConnection();
        c2.addRequestProperty("If-None-Match", eTag);
        c2.connect();
        int code = c2.getResponseCode();
        boolean supported = (code == 304);
        ifNoneMatchSupported = supported;
        c2.disconnect();
    }

    private void testIfModified() throws IOException {
        HttpURLConnection c2 = (HttpURLConnection) url.openConnection();
        c2.setIfModifiedSince(System.currentTimeMillis() + 1000);
        c2.connect();
        int code = c2.getResponseCode();
        boolean supported = (code == 304);
        ifModifiedSinceSupported = supported;
    }

    protected void printHeaders() {
        log.trace("\nHeaders:");
        for (Map.Entry<String, List<String>> entry : c.getHeaderFields().entrySet()) {
            String key = entry.getKey();
            for (String elem : entry.getValue()) {
                if (key != null) log.debug(key + " = ");
                log.debug(elem);
            }
        }
    }

    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        sw.append("Mapsource.........: " + mapSource.getName() + "\n");
        sw.append("Current TileUpdate: " + mapSource.getTileUpdate() + "\n");
        sw.append("If-None-Match.....: " + b2s(ifNoneMatchSupported) + "\n");
        sw.append("ETag..............: " + b2s(eTagPresent) + "\n");
        sw.append("If-Modified-Since.: " + b2s(ifModifiedSinceSupported) + "\n");
        sw.append("LastModified......: " + b2s(lastModifiedTimePresent) + "\n");
        sw.append("Expires...........: " + b2s(expirationTimePresent) + "\n");
        return sw.toString();
    }

    public boolean isSuccess() {
        return success;
    }

    public Exception getError() {
        return error;
    }

    public int getZoom() {
        return zoom;
    }

    public boolean iseTagPresent() {
        return eTagPresent;
    }

    public boolean isExpirationTimePresent() {
        return expirationTimePresent;
    }

    public boolean isLastModifiedTimePresent() {
        return lastModifiedTimePresent;
    }

    public boolean isIfModifiedSinceSupported() {
        return ifModifiedSinceSupported;
    }

    public boolean isIfNoneMatchSupported() {
        return ifNoneMatchSupported;
    }

    public String getContentType() {
        return contentType;
    }

    public TileUpdate getRecommendedTileUpdate() {
        if (ifNoneMatchSupported) return TileUpdate.IfNoneMatch;
        if (ifModifiedSinceSupported) return TileUpdate.IfModifiedSince;
        if (eTagPresent) return TileUpdate.ETag;
        if (lastModifiedTimePresent) return TileUpdate.LastModified;
        return TileUpdate.None;
    }

    private static String b2s(boolean b) {
        if (b) return "supported"; else return "-";
    }

    static final byte[] HEX_CHAR_TABLE = { (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f' };

    public static String getHexString(byte[] raw) throws UnsupportedEncodingException {
        byte[] hex = new byte[2 * raw.length];
        int index = 0;
        for (byte b : raw) {
            int v = b & 0xFF;
            hex[index++] = HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = HEX_CHAR_TABLE[v & 0xF];
        }
        return new String(hex, "ASCII");
    }
}
