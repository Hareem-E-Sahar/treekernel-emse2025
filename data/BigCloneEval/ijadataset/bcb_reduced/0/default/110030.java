import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.PlugIn;
import com.rbnb.sapi.PlugInChannelMap;
import com.rbnb.sapi.Sink;
import com.rbnb.utility.ArgHandler;
import com.rbnb.utility.KeyValueHash;
import com.rbnb.utility.RBNBProcess;
import java.io.ByteArrayOutputStream;
import java.util.Hashtable;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class KMLigPlugIn {

    private String address = "localhost:3333";

    private String sinkName = "kmligSink";

    private String pluginName = "KMLig";

    private Sink sink = null;

    private PlugIn plugin = null;

    private double baseinterval = 1;

    private int basecount = 10;

    private String urlBase = "http://localhost/RBNB/";

    private String kmlFileName = "KMLigResources/kml.kml";

    private String photooverlayFileName = "KMLigResources/photooverlay.kml";

    public static void main(String[] args) {
        (new KMLigPlugIn(args)).exec();
    }

    public KMLigPlugIn(String[] args) {
        try {
            ArgHandler ah = new ArgHandler(args);
            if (ah.checkFlag('h')) {
                System.err.println("KMLitPlugIn command line options");
                System.err.println("   -a <RBNB address>");
                System.err.println("       default: localhost:3333");
                System.err.println("   -c <count> : max number of images to return");
                System.err.println("      default: 100");
                System.err.println("   -h (display this help message)");
                System.err.println("   -i <interval> : min interval between images");
                System.err.println("      default: 1 second");
                System.err.println("   -n <PlugIn name>");
                System.err.println("       default: KMLig");
                System.err.println("   -u <URL base> : beginning of image URLs");
                System.err.println("       default: http://localhost/RBNB/");
            }
            if (ah.checkFlag('a')) {
                String addressL = ah.getOption('a');
                if (addressL != null) {
                    address = addressL;
                } else {
                    System.err.println("WARNING: Null argument to the \"-a\"" + " command line option.");
                }
            }
            if (ah.checkFlag('c')) {
                String countS = ah.getOption('c');
                if (countS != null) {
                    basecount = Integer.parseInt(countS);
                } else {
                    System.err.println("WARNING: Null argument to the -c command line option");
                }
            }
            if (ah.checkFlag('i')) {
                String intervalS = ah.getOption('i');
                if (intervalS != null) {
                    baseinterval = Double.parseDouble(intervalS);
                } else {
                    System.err.println("WARNING: Null argument to the -i command line option");
                }
            }
            if (ah.checkFlag('n')) {
                String name = ah.getOption('n');
                if (name != null) {
                    pluginName = name;
                } else {
                    System.err.println("WARNING: Null argument to the \"-n\"" + " command line option.");
                }
            }
            if (ah.checkFlag('p')) {
                String photooverlayFileNameL = ah.getOption('p');
                if (photooverlayFileNameL != null) {
                    photooverlayFileName = photooverlayFileNameL;
                } else {
                    System.err.println("WARNING: Null argument to the \"-p\"" + " command line option.");
                }
            }
            if (ah.checkFlag('u')) {
                String urlL = ah.getOption('u');
                if (urlL != null) {
                    urlBase = urlL;
                } else {
                    System.err.println("WARNING: Null argument to the \"-u\" command line option");
                }
            }
        } catch (Exception e) {
            System.err.println("AppendPlugIn argument exception " + e.getMessage());
            e.printStackTrace();
            RBNBProcess.exit(0);
        }
    }

    public void exec() {
        try {
            sink = new Sink();
            sink.OpenRBNBConnection(address, sinkName);
            plugin = new PlugIn();
            plugin.OpenRBNBConnection(address, pluginName);
        } catch (Exception e) {
            System.err.println("Exception opening RBNB connections, aborting.");
            e.printStackTrace();
            RBNBProcess.exit(0);
        }
        if (!plugin.GetClientName().equals(pluginName)) {
            pluginName = plugin.GetClientName();
            System.err.println("WARNING: The actual PlugIn name is " + pluginName);
        }
        System.err.println("connections open; awaiting requests");
        while (true) {
            PlugInChannelMap picm = null;
            try {
                picm = plugin.Fetch(3000);
                if ((picm.GetIfFetchTimedOut()) || (picm.NumberOfChannels() == 0)) continue;
                String[] message = null;
                if (picm.GetType(0) == ChannelMap.TYPE_STRING) {
                    message = picm.GetDataAsString(0);
                } else if (picm.GetType(0) == ChannelMap.TYPE_INT8) {
                    message = new String[1];
                    message[0] = new String(picm.GetDataAsInt8(0));
                }
                int count = basecount;
                double interval = baseinterval;
                if (message != null && message[0].trim().length() > 0) {
                    message[0] = message[0].trim();
                    char[] term = { '&' };
                    KeyValueHash kvh = new KeyValueHash(message[0], term);
                    String countS = kvh.get("kmlit_count");
                    String intervalS = kvh.get("kmlit_interval");
                    if (countS != null) try {
                        count = Integer.parseInt(countS);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (intervalS != null) try {
                        interval = Double.parseDouble(intervalS);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                ChannelMap cm = new ChannelMap();
                for (int i = 0; i < picm.NumberOfChannels(); i++) cm.Add(picm.GetName(i));
                if (message != null && message[0].length() > 0) for (int i = 0; i < cm.NumberOfChannels(); i++) cm.PutDataAsString(i, message[0]);
                sink.RequestRegistration(cm);
                cm = sink.Fetch(60000);
                if (cm.GetIfFetchTimedOut()) {
                    System.err.println("timed out making request, returning no data");
                    plugin.Flush(picm);
                    continue;
                }
                if (cm.NumberOfChannels() < 1) {
                    System.err.println("no data on requested channel, returning no data");
                    plugin.Flush(picm);
                    continue;
                }
                double begin = 0;
                double end = 0;
                double duration = cm.GetTimeDuration(0);
                if (duration > picm.GetRequestDuration()) duration = picm.GetRequestDuration();
                if (picm.GetRequestReference().equals("registration")) {
                    picm.PutTime(cm.GetTimeStart(0), cm.GetTimeDuration(0));
                    picm.PutDataAsInt8(0, new byte[1]);
                    plugin.Flush(picm);
                    continue;
                } else if (picm.GetRequestReference().equals("newest")) {
                    end = cm.GetTimeStart(0) + cm.GetTimeDuration(0);
                    begin = end - duration;
                } else if (picm.GetRequestReference().equals("oldest")) {
                    begin = cm.GetTimeStart(0);
                    end = begin + duration;
                } else {
                    begin = cm.GetTimeStart(0);
                    if (begin < picm.GetRequestStart()) begin = picm.GetRequestStart();
                    end = begin + duration;
                    double dataend = cm.GetTimeStart(0) + cm.GetTimeDuration(0);
                    double reqend = picm.GetRequestStart() + picm.GetRequestDuration();
                    if (end > dataend) end = dataend;
                    if (end > reqend) end = dataend;
                    duration = end - begin;
                    if (duration < 0) {
                        System.err.println("no data in requested range");
                        plugin.Flush(picm);
                        continue;
                    }
                }
                if (duration <= 0) count = 1; else if (duration / count < interval) count = (int) Math.round(duration / interval);
                interval = duration / count;
                byte[] zip = buildKMZ(urlBase + picm.GetName(0), begin, end, interval, count, picm.GetName(0));
                if (zip != null) {
                    picm.PutTime(begin, duration);
                    picm.PutDataAsByteArray(0, zip);
                }
                plugin.Flush(picm);
            } catch (Exception e) {
                System.err.println("RBNB exception; returning no data; restarting plugin and sink");
                e.printStackTrace();
                try {
                    if (picm != null) {
                        picm.Clear();
                        plugin.Flush(picm);
                    }
                    sink.CloseRBNBConnection();
                    sink.OpenRBNBConnection(address, sinkName);
                    plugin.CloseRBNBConnection();
                    plugin.OpenRBNBConnection(address, pluginName);
                } catch (Exception e2) {
                    System.err.println("RBNB exception; unable to establish connections; aborting");
                    e2.printStackTrace();
                    break;
                }
            }
        }
    }

    private byte[] buildKMZ(String url, double begin, double end, double intv, int cnt, String imgname) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StringBuffer sb;
        String kmls;
        String photooverlays;
        String daes;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(new java.util.SimpleTimeZone(0, "UTC"));
        try {
            String line;
            sb = new StringBuffer();
            java.io.File file = new java.io.File(kmlFileName);
            java.io.FileReader fr = new java.io.FileReader(file);
            java.io.BufferedReader br = new java.io.BufferedReader(fr);
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
            kmls = sb.toString();
            sb = new StringBuffer();
            file = new java.io.File(photooverlayFileName);
            fr = new java.io.FileReader(file);
            br = new java.io.BufferedReader(fr);
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
            photooverlays = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        String metaname = imgname.substring(0, imgname.lastIndexOf('.')) + ".xml";
        System.err.println("image channel: " + imgname);
        System.err.println("metadata channel: " + metaname);
        ChannelMap cm = new ChannelMap();
        ChannelMap cmin = null;
        try {
            cm.Add(imgname);
            cm.Add(metaname);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String[] daedoc = new String[cnt];
            byte[][] image = new byte[cnt][];
            String[] stime = new String[cnt];
            if (cnt > 1) intv = (end - begin) / (2 * (cnt - 1));
            System.err.println("cnt " + cnt + ", intv " + intv);
            double lastetime = begin;
            double btime = begin;
            double etime = begin + intv;
            double lastitime = 0;
            for (int i = 0; i < cnt; i++) {
                double rtime = begin + 2 * i * intv;
                sink.Request(cm, rtime, 0, "absolute");
                cmin = sink.Fetch(4000);
                int imgidx = cmin.GetIndex(imgname);
                boolean writekml = true;
                if (imgidx > -1) {
                    image[i] = cmin.GetDataAsByteArray(cmin.GetIndex(imgname))[0];
                    System.err.println("i=" + i + ", imagelength=" + image[i].length);
                    if (i < cnt - 1 && cmin.GetTimeStart(imgidx) - lastitime < 1e-3) {
                        System.err.println("duplicate image!!");
                        writekml = false;
                    } else {
                        lastetime = etime;
                        lastitime = cmin.GetTimeStart(imgidx);
                    }
                } else {
                    System.err.println("NO IMAGE!!");
                    writekml = false;
                }
                if (writekml) {
                    btime = lastetime;
                    if (i == 0) btime = begin;
                    etime = begin + (2 * i + 1) * intv;
                    if (i == cnt - 1) etime = end;
                    String bdate = sdf.format(new java.util.Date((long) (btime * 1000)));
                    String edate = sdf.format(new java.util.Date((long) (etime * 1000)));
                    stime[i] = (new Long(Math.round(rtime * 10000))).toString();
                    byte[] metastring = null;
                    int idx = cmin.GetIndex(metaname);
                    if (idx > -1) {
                        if (cmin.GetType(idx) == ChannelMap.TYPE_BYTEARRAY) {
                            metastring = cmin.GetDataAsByteArray(idx)[0];
                        } else if (cmin.GetType(idx) == ChannelMap.TYPE_STRING) {
                            metastring = (cmin.GetDataAsString(idx)[0]).getBytes();
                        }
                    } else {
                        try {
                            java.io.File metafile = new java.io.File(metaname.substring(1 + metaname.lastIndexOf('/')));
                            sb = new StringBuffer();
                            String line = null;
                            java.io.FileReader fr = new java.io.FileReader(metafile);
                            java.io.BufferedReader br = new java.io.BufferedReader(fr);
                            while ((line = br.readLine()) != null) sb.append(line).append('\n');
                            metastring = sb.toString().getBytes();
                        } catch (Exception e) {
                            System.err.println("NO METADATA!!");
                            e.printStackTrace();
                        }
                    }
                    String docname;
                    String clongitude, clatitude, caltitude, ctilt, cheading, croll;
                    String vnear, vleftfov, vrightfov, vbottomfov, vtopfov;
                    String pcoordinates;
                    try {
                        javax.xml.parsers.DocumentBuilder db = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
                        java.io.ByteArrayInputStream meta = new java.io.ByteArrayInputStream(metastring);
                        org.w3c.dom.Element e = db.parse(meta).getDocumentElement();
                        org.w3c.dom.Element ee = (org.w3c.dom.Element) e.getElementsByTagName("name").item(0);
                        docname = ee.getFirstChild().getNodeValue();
                        ee = (org.w3c.dom.Element) e.getElementsByTagName("Camera").item(0);
                        clongitude = ee.getElementsByTagName("longitude").item(0).getFirstChild().getNodeValue();
                        clatitude = ee.getElementsByTagName("latitude").item(0).getFirstChild().getNodeValue();
                        caltitude = ee.getElementsByTagName("altitude").item(0).getFirstChild().getNodeValue();
                        ctilt = ee.getElementsByTagName("tilt").item(0).getFirstChild().getNodeValue();
                        cheading = ee.getElementsByTagName("heading").item(0).getFirstChild().getNodeValue();
                        croll = ee.getElementsByTagName("roll").item(0).getFirstChild().getNodeValue();
                        ee = (org.w3c.dom.Element) e.getElementsByTagName("ViewVolume").item(0);
                        vnear = ee.getElementsByTagName("near").item(0).getFirstChild().getNodeValue();
                        vleftfov = ee.getElementsByTagName("leftFov").item(0).getFirstChild().getNodeValue();
                        vrightfov = ee.getElementsByTagName("rightFov").item(0).getFirstChild().getNodeValue();
                        vbottomfov = ee.getElementsByTagName("bottomFov").item(0).getFirstChild().getNodeValue();
                        vtopfov = ee.getElementsByTagName("topFov").item(0).getFirstChild().getNodeValue();
                        ee = (org.w3c.dom.Element) e.getElementsByTagName("Point").item(0);
                        pcoordinates = ee.getElementsByTagName("coordinates").item(0).getFirstChild().getNodeValue();
                    } catch (Exception e) {
                        System.err.println("Exception parsing metadata!");
                        e.printStackTrace();
                        return null;
                    }
                    if (i == 0) {
                    }
                    String po = photooverlays.replaceFirst("##name##", docname);
                    po = po.replaceFirst("##begin##", bdate);
                    po = po.replaceFirst("##end##", edate);
                    po = po.replaceFirst("##clongitude##", clongitude);
                    po = po.replaceFirst("##clatitude##", clatitude);
                    po = po.replaceFirst("##caltitude##", caltitude);
                    po = po.replaceFirst("##ctilt##", ctilt);
                    po = po.replaceFirst("##cheading##", cheading);
                    po = po.replaceFirst("##croll##", croll);
                    po = po.replaceFirst("##href-jpg##", "i" + stime[i] + ".jpg");
                    po = po.replaceFirst("##vnear##", vnear);
                    po = po.replaceFirst("##vleftfov##", vleftfov);
                    po = po.replaceFirst("##vrightfov##", vrightfov);
                    po = po.replaceFirst("##vbottomfov##", vbottomfov);
                    po = po.replaceFirst("##vtopfov##", vtopfov);
                    po = po.replaceAll("##coordinates##", pcoordinates);
                    kmls = kmls.replaceFirst("##photooverlay##", po + "##photooverlay##");
                }
            }
            kmls = kmls.replaceFirst("##photooverlay##", "");
            ZipOutputStream zos = new ZipOutputStream(baos);
            zos.setMethod(ZipOutputStream.DEFLATED);
            zos.setLevel(Deflater.DEFAULT_COMPRESSION);
            ZipEntry ze = new ZipEntry("foo.kml");
            zos.putNextEntry(ze);
            byte[] data = kmls.getBytes();
            zos.write(data, 0, data.length);
            zos.closeEntry();
            for (int i = 0; i < cnt; i++) {
                if (daedoc[i] != null) {
                    ze = new ZipEntry("d" + stime[i] + ".dae");
                    zos.putNextEntry(ze);
                    data = daedoc[i].getBytes();
                    zos.write(data, 0, data.length);
                    zos.closeEntry();
                }
            }
            for (int i = 0; i < cnt; i++) {
                if (stime[i] != null) {
                    ze = new ZipEntry("i" + stime[i] + ".jpg");
                    zos.putNextEntry(ze);
                    data = image[i];
                    zos.write(data, 0, data.length);
                    zos.closeEntry();
                }
            }
            zos.close();
        } catch (Exception e) {
            System.err.println("Exception zipping kml/dae documents");
            e.printStackTrace();
        }
        return baos.toByteArray();
    }
}
