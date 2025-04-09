import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import com.rbnb.plugins.PlugInTemplate;
import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.PlugInChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;
import com.rbnb.utility.ArgHandler;

public class DeadReckoningPlugIn extends PlugInTemplate {

    private static String default_host = "localhost:3333";

    private static int default_numLineSegments = 50;

    private static String default_pluginName = "DRPI";

    private static boolean default_bKMZ = true;

    private static double default_maxLatency = Double.MAX_VALUE;

    private Sink sink = null;

    private boolean bKMZ = default_bKMZ;

    private int numLineSegments = default_numLineSegments;

    private double maxLatency = default_maxLatency;

    public static void main(String[] argsI) throws Exception {
        DeadReckoningPlugIn drpi = new DeadReckoningPlugIn(argsI);
        drpi.run();
    }

    private static void showUsage() {
        boolean default_bKML = !default_bKMZ;
        System.err.println(PNGPlugIn.class.getName() + ":\n" + "    Constructs a dead reckoning ring around the last known\n" + "    vehicle position; returns a KML or KMZ to display this ring.\n" + "Options:\n" + "\t-a <host:port>              RBNB server to connect to\n" + "\t                            Default: " + default_host + "\n" + "\t-i <integer>                Number of line segments in the dead reckoning circle\n" + "\t                            Default: " + default_numLineSegments + "\n" + "\t-k                          Return KML?\n" + "\t                            Default: " + default_bKML + " (true means return KML; false means return KMZ)\n" + "\t-m <max-latency>            Maximum latency in seconds (the dead reckoning ring stops growing at max latency)\n" + "\t                            Default: " + default_maxLatency + "\n" + "\t-n name                     Client name for plugin\n" + "\t                            Default: " + default_pluginName + "\n");
        System.exit(1);
    }

    public DeadReckoningPlugIn(String[] argsI) throws Exception {
        ArgHandler ah = null;
        try {
            ah = new ArgHandler(argsI);
        } catch (Exception e) {
            showUsage();
        }
        if (ah.checkFlag('h')) {
            showUsage();
        }
        if (ah.checkFlag('i')) {
            numLineSegments = Integer.parseInt(ah.getOption('n'));
        }
        if (ah.checkFlag('k')) {
            bKMZ = false;
        }
        if (ah.checkFlag('m')) {
            maxLatency = Double.parseDouble(ah.getOption('m'));
        }
        setHost(ah.getOption('a', default_host));
        setName(ah.getOption('n', default_pluginName));
        System.err.println("Starting DeadReckoningPlugIn connected to host " + getHost() + ", PlugIn name = " + getName());
        setForwardRequests(false);
        sink = new Sink();
        sink.OpenRBNBConnection(getHost(), "DRSink");
    }

    public void run() throws SAPIException {
        start();
        try {
            synchronized (this) {
                wait();
            }
        } catch (InterruptedException ie) {
        }
    }

    protected void processRequest(ChannelMap fwdData, PlugInChannelMap out) throws SAPIException {
        String[] chanList = out.GetChannelList();
        String requestChanStr = chanList[0];
        if (requestChanStr.endsWith("/")) {
            requestChanStr = requestChanStr.substring(0, requestChanStr.length() - 1);
        }
        System.err.println((new Date()).toString() + "  Source: " + requestChanStr);
        ChannelMap reqMap = new ChannelMap();
        reqMap.Add(requestChanStr + "/Altitude");
        reqMap.Add(requestChanStr + "/Latitude");
        reqMap.Add(requestChanStr + "/Longitude");
        reqMap.Add(requestChanStr + "/GroundSpeed");
        reqMap.Add(requestChanStr + "/Heading");
        sink.Request(reqMap, 0, 0, "newest");
        ChannelMap dataMap = sink.Fetch(60000);
        int altIndex = dataMap.GetIndex(requestChanStr + "/Altitude");
        double alt = 0.0;
        if (dataMap.GetType(altIndex) == ChannelMap.TYPE_FLOAT64) {
            alt = dataMap.GetDataAsFloat64(altIndex)[0];
        } else if (dataMap.GetType(altIndex) == ChannelMap.TYPE_FLOAT32) {
            alt = (double) dataMap.GetDataAsFloat32(altIndex)[0];
        }
        int latIndex = dataMap.GetIndex(requestChanStr + "/Latitude");
        double lat = 0.0;
        if (dataMap.GetType(latIndex) == ChannelMap.TYPE_FLOAT64) {
            lat = dataMap.GetDataAsFloat64(latIndex)[0];
        } else if (dataMap.GetType(latIndex) == ChannelMap.TYPE_FLOAT32) {
            lat = (double) dataMap.GetDataAsFloat32(latIndex)[0];
        }
        int lonIndex = dataMap.GetIndex(requestChanStr + "/Longitude");
        double lon = 0.0;
        if (dataMap.GetType(lonIndex) == ChannelMap.TYPE_FLOAT64) {
            lon = dataMap.GetDataAsFloat64(lonIndex)[0];
        } else if (dataMap.GetType(lonIndex) == ChannelMap.TYPE_FLOAT32) {
            lon = (double) dataMap.GetDataAsFloat32(lonIndex)[0];
        }
        int gsIndex = dataMap.GetIndex(requestChanStr + "/GroundSpeed");
        double gs = 0.0;
        if (dataMap.GetType(gsIndex) == ChannelMap.TYPE_FLOAT64) {
            gs = dataMap.GetDataAsFloat64(gsIndex)[0];
        } else if (dataMap.GetType(gsIndex) == ChannelMap.TYPE_FLOAT32) {
            gs = (double) dataMap.GetDataAsFloat32(gsIndex)[0];
        }
        int headIndex = dataMap.GetIndex(requestChanStr + "/Heading");
        double heading = 0.0;
        if (dataMap.GetType(headIndex) == ChannelMap.TYPE_FLOAT64) {
            heading = dataMap.GetDataAsFloat64(headIndex)[0];
        } else if (dataMap.GetType(headIndex) == ChannelMap.TYPE_FLOAT32) {
            heading = (double) dataMap.GetDataAsFloat32(headIndex)[0];
        }
        double dataTime = dataMap.GetTimes(altIndex)[0];
        double currTime = System.currentTimeMillis() / 1000.0;
        double latency = currTime - dataTime;
        boolean bMaxLatencyExceeded = false;
        if (latency > maxLatency) {
            latency = maxLatency;
            bMaxLatencyExceeded = true;
        }
        double radius = latency * gs;
        String kmlStr = createDRCircle(lat, lon, alt, radius, heading, bMaxLatencyExceeded);
        out.PutTime(System.currentTimeMillis() / 1000.0, out.GetRequestDuration());
        if (!bKMZ) {
            out.PutDataAsString(0, kmlStr);
            out.PutMime(0, "application/vnd.google-earth.kml+xml");
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ZipOutputStream zos = new ZipOutputStream(baos);
                ZipEntry ze = new ZipEntry("doc.kml");
                zos.setMethod(ZipOutputStream.DEFLATED);
                zos.setLevel(Deflater.DEFAULT_COMPRESSION);
                zos.putNextEntry(ze);
                byte[] kmlBytes = kmlStr.getBytes();
                zos.write(kmlBytes, 0, kmlBytes.length);
                zos.close();
                out.PutDataAsByteArray(0, baos.toByteArray());
                out.PutMime(0, "application/vnd.google-earth.kmz");
            } catch (Exception ex) {
                System.err.println("Exception generating KMZ: " + ex.getMessage());
                throw new SAPIException("Exception generating KMZ");
            }
        }
    }

    protected void processRegistrationRequest(ChannelMap fwdReg, PlugInChannelMap out) throws SAPIException {
        out.PutTime((System.currentTimeMillis() / 1000.0), 0.0);
        String mimeStr = "application/vnd.google-earth.kml+xml";
        if (bKMZ) {
            mimeStr = "application/vnd.google-earth.kmz";
        }
        String result = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + "<!DOCTYPE rbnb>\n" + "<rbnb>\n" + "\t\t<size>" + 1 + "</size>\n" + "\t\t<mime>" + mimeStr + "</mime>\n" + "</rbnb>\n";
        out.PutDataAsString(0, result);
        out.PutMime(0, "text/xml");
        System.err.println((new Date()).toString() + "  Responded to registration request.");
    }

    private String createDRCircle(double latI, double lonI, double altI, double radiusI, double headingI, boolean bMaxLatencyExceeded) {
        String ringColor = "ff00ff00";
        if (bMaxLatencyExceeded) {
            ringColor = "ff0000ff";
        }
        String headingLineColor = "ff00ffff";
        StringBuffer sb = new StringBuffer();
        double lat = Math.toRadians(latI);
        double lon = Math.toRadians(lonI);
        float altF = (float) altI;
        double d_rad = radiusI / 6378137;
        double heading = headingI;
        if (heading < 0) {
            while (heading < 0) {
                heading = heading + 360.0;
            }
        } else if (heading > 360) {
            while (heading > 360) {
                heading = heading - 360.0;
            }
        }
        double resolution = 360 / numLineSegments;
        int estimatedIdx_lo = (int) Math.floor(heading / resolution);
        int estimatedIdx_hi = (int) Math.ceil(heading / resolution);
        float estimatedLat_lo = (float) 0.0;
        float estimatedLon_lo = (float) 0.0;
        float estimatedLat_hi = (float) 0.0;
        float estimatedLon_hi = (float) 0.0;
        sb.append("<Folder>\n" + "<name>Dead Reckoning Circle</name>\n" + "<visibility>1</visibility>\n" + "<Placemark>\n" + "<name>circle</name>\n" + "<visibility>1</visibility>\n" + "<Style>\n" + "<geomColor>" + ringColor + "</geomColor>\n" + "<LineStyle>\n" + "<color>" + ringColor + "</color>\n" + "<width>2</width>\n" + "</LineStyle>\n" + "</Style>\n" + "<LineString>\n" + "<altitudeMode>absolute</altitudeMode>\n" + "<coordinates>\n");
        for (int i = 0; i <= numLineSegments; ++i) {
            double radial = Math.toRadians(i * 360 / numLineSegments);
            double lat_rad = Math.asin(Math.sin(lat) * Math.cos(d_rad) + Math.cos(lat) * Math.sin(d_rad) * Math.cos(radial));
            double dlon_rad = Math.atan2(Math.sin(radial) * Math.sin(d_rad) * Math.cos(lat), Math.cos(d_rad) - Math.sin(lat) * Math.sin(lat_rad));
            double lon_rad = ((lon + dlon_rad + Math.PI) % (2 * Math.PI)) - Math.PI;
            float dr_point_lon = (float) Math.toDegrees(lon_rad);
            sb.append(dr_point_lon);
            sb.append(",");
            float dr_point_lat = (float) Math.toDegrees(lat_rad);
            sb.append(dr_point_lat);
            sb.append(",");
            sb.append(altF);
            sb.append(" ");
            if (i == estimatedIdx_lo) {
                estimatedLat_lo = dr_point_lat;
                estimatedLon_lo = dr_point_lon;
            }
            if (i == estimatedIdx_hi) {
                estimatedLat_hi = dr_point_lat;
                estimatedLon_hi = dr_point_lon;
            }
        }
        sb.append("</coordinates>\n</LineString>\n</Placemark>\n");
        if (false) {
            sb.append("<Placemark>\n" + "<visibility>1</visibility>\n" + "<Style>\n" + "<IconStyle>\n" + "<Icon>\n" + "<href>root://icons/palette-4.png</href>\n" + "<x>224</x>\n" + "<y>64</y>\n" + "<w>32</w>\n" + "<h>32</h>\n" + "</Icon>\n" + "</IconStyle>\n" + "</Style>\n" + "<Point>\n" + "<altitudeMode>absolute</altitudeMode>\n" + "<coordinates>\n");
            sb.append(estimatedLon_lo);
            sb.append(",");
            sb.append(estimatedLat_lo);
            sb.append(",");
            sb.append(altF);
            sb.append("</coordinates>\n" + "</Point>\n" + "</Placemark>\n");
        }
        sb.append("<Placemark>\n" + "<name>heading</name>\n" + "<visibility>1</visibility>\n" + "<Style>\n" + "<geomColor>" + headingLineColor + "</geomColor>\n" + "<LineStyle>\n" + "<color>" + headingLineColor + "</color>\n" + "<width>6</width>\n" + "</LineStyle>\n" + "</Style>\n" + "<LineString>\n" + "<altitudeMode>absolute</altitudeMode>\n" + "<coordinates>\n");
        sb.append(estimatedLon_lo);
        sb.append(",");
        sb.append(estimatedLat_lo);
        sb.append(",");
        sb.append(altF);
        sb.append(" ");
        sb.append(estimatedLon_hi);
        sb.append(",");
        sb.append(estimatedLat_hi);
        sb.append(",");
        sb.append(altF);
        sb.append("</coordinates>\n</LineString>\n</Placemark>\n");
        sb.append("</Folder>");
        return sb.toString();
    }
}
