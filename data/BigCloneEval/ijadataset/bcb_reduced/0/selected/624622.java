package mobac.tools;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import mobac.mapsources.MapSourceTools;
import mobac.mapsources.MapSourcesUpdater;
import mobac.mapsources.impl.Google.GoogleEarth;
import mobac.mapsources.impl.Google.GoogleEarthMapsOverlay;
import mobac.mapsources.impl.Google.GoogleMapMaker;
import mobac.mapsources.impl.Google.GoogleMaps;
import mobac.mapsources.impl.Google.GoogleMapsChina;
import mobac.mapsources.impl.Google.GoogleMapsKorea;
import mobac.mapsources.impl.Google.GoogleTerrain;
import mobac.program.Logging;
import mobac.tools.MapSourcesTester.MapSourceTestFailed;
import mobac.utilities.Utilities;
import org.openstreetmap.gui.jmapviewer.interfaces.MapSource;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

public class GoogleUrlUpdater {

    static Properties MAPSOURCES_PROPERTIES = new Properties();

    static List<String> KEYS = new ArrayList<String>();

    /**
	 * <p>
	 * Recalculates the tile urls for Google Maps, Earth and Terrain and prints
	 * it to std out. Other map sources can not be updated this way.
	 * </p>
	 * 
	 * Requires <a href="http://jtidy.sourceforge.net/">JTidy</a> library.
	 */
    public static void main(String[] args) {
        Logging.disableLogging();
        MapSourcesUpdater.loadMapSourceProperties(MAPSOURCES_PROPERTIES);
        System.getProperties().putAll(MAPSOURCES_PROPERTIES);
        KEYS.add("mapsources.Date");
        KEYS.add("mapsources.Rev");
        GoogleUrlUpdater g = new GoogleUrlUpdater();
        g.testMapSource(new UpdateableMapSource("http://maps.google.com/?ie=UTF8&ll=0,0&spn=0,0&z=2", GoogleMaps.class));
        g.testMapSource(new UpdateableMapSource("http://maps.google.com/?ie=UTF8&t=k&ll=0,0&spn=0,0&z=2", GoogleEarth.class));
        g.testMapSource(new UpdateableMapSource("http://maps.google.com/?ie=UTF8&t=p&ll=0,0&spn=0,0&z=2", GoogleTerrain.class));
        g.testMapSource(new UpdateableMapSource("http://maps.google.com/?ie=UTF8&ll=0,0&spn=0,0&t=h&z=4", GoogleEarthMapsOverlay.class));
        g.testMapSource(new UpdateableMapSource("http://maps.google.com/?ie=UTF8&ll=36.27,128.20&spn=3.126164,4.932861&z=8", GoogleMapsKorea.class, false) {

            @Override
            protected String processFoundUrl(String url) {
                if (url.endsWith("&") && url.indexOf("gmaptiles.co.kr") > 0) return url + "x=0&y=0&z=0"; else return url;
            }
        });
        g.testMapSource(new UpdateableMapSource("", GoogleMapMaker.class) {

            @Override
            public String getUpdatedUrl(GoogleUrlUpdater g) {
                return g.getUppdatedGoogleMapMakerUrl();
            }
        });
        g.testMapSource(new UpdateableMapSource("", GoogleMapsChina.class) {

            @Override
            public String getUpdatedUrl(GoogleUrlUpdater g) {
                return g.getUpdateGoogleMapsChinaUrl();
            }
        });
        System.out.println("Updated map sources: " + g.updatedMapSources);
        if (g.updatedMapSources > 0) {
            ByteArrayOutputStream bo = new ByteArrayOutputStream(4096);
            PrintWriter pw = new PrintWriter(bo, true);
            for (String key : KEYS) {
                pw.println(key + "=" + MAPSOURCES_PROPERTIES.getProperty(key));
                MAPSOURCES_PROPERTIES.remove(key);
            }
            Enumeration<?> enu = MAPSOURCES_PROPERTIES.keys();
            while (enu.hasMoreElements()) {
                String key = (String) enu.nextElement();
                pw.println(key + "=" + MAPSOURCES_PROPERTIES.getProperty(key));
            }
            pw.flush();
            FileOutputStream fo = null;
            try {
                fo = new FileOutputStream("src/mobac/mapsources.properties");
                fo.write(bo.toByteArray());
                System.out.println("mapsources.properties has been updated");
            } catch (IOException e) {
            } finally {
                Utilities.closeStream(fo);
            }
        }
    }

    protected int updatedMapSources = 0;

    public void testMapSource(UpdateableMapSource ums) {
        String key = ums.key;
        KEYS.add(key);
        String oldUrlTemplate = MAPSOURCES_PROPERTIES.getProperty(key);
        if (oldUrlTemplate == null) throw new RuntimeException("Url for key not found: " + key);
        String newUrlTemplate = ums.getUpdatedUrl(this);
        if (newUrlTemplate == null) {
            System.out.println(ums.mapSourceClass.getSimpleName());
            System.out.println(" failed to extract url");
        } else if (!oldUrlTemplate.equals(newUrlTemplate)) {
            try {
                System.setProperty(key, newUrlTemplate);
                MapSourcesTester.testMapSource(ums.mapSourceClass);
                System.out.println(ums.mapSourceClass.getSimpleName());
                MAPSOURCES_PROPERTIES.setProperty(key, newUrlTemplate);
                updatedMapSources++;
            } catch (MapSourceTestFailed e) {
                System.err.print("Test of new url failed: ");
                System.err.println(key + "=" + newUrlTemplate);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<String> extractImgSrcList(String url) throws IOException, XPathExpressionException {
        LinkedList<String> list = new LinkedList<String>();
        URL u = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        Tidy tidy = new Tidy();
        tidy.setErrout(new NullPrintWriter());
        Document doc = tidy.parseDOM(conn.getInputStream(), null);
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        XPathExpression expr = xpath.compile("//img[@src]");
        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        for (int i = 0; i < nodes.getLength(); i++) {
            String imgUrl = nodes.item(i).getAttributes().getNamedItem("src").getNodeValue();
            if (imgUrl != null && imgUrl.length() > 0) list.add(imgUrl);
        }
        return list;
    }

    public List<String> extractUrlList(String url) throws IOException, XPathExpressionException {
        LinkedList<String> list = new LinkedList<String>();
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        Tidy tidy = new Tidy();
        tidy.setErrout(new NullPrintWriter());
        Document doc = tidy.parseDOM(conn.getInputStream(), null);
        int len = conn.getContentLength();
        if (len <= 0) len = 32000;
        ByteArrayOutputStream bout = new ByteArrayOutputStream(len);
        PrintStream ps = new PrintStream(bout);
        tidy.pprint(doc, ps);
        ps.flush();
        String content = bout.toString();
        Pattern p = Pattern.compile("(http://[\\w\\\\\\./=&?;-]+)");
        Matcher m = p.matcher(content);
        while (m.find()) {
            list.add(m.group());
        }
        return list;
    }

    public String getUpdatedUrl(UpdateableMapSource ums, String serviceUrl, boolean useImgSrcUrlsOnly) {
        try {
            List<String> urls;
            if (useImgSrcUrlsOnly) urls = extractImgSrcList(serviceUrl); else urls = extractUrlList(serviceUrl);
            HashSet<UrlString> tileUrlCandidates = new HashSet<UrlString>();
            for (String imgUrl : urls) {
                try {
                    if (!imgUrl.toLowerCase().startsWith("http://")) continue;
                    imgUrl = imgUrl.replaceAll("\\\\x26", "&");
                    imgUrl = imgUrl.replaceAll("\\\\x3d", "=");
                    imgUrl = ums.processFoundUrl(imgUrl);
                    URL tileUrl = new URL(imgUrl);
                    String host = tileUrl.getHost();
                    host = host.replaceFirst("[0-3]", "{\\$servernum}");
                    String path = tileUrl.getPath();
                    path = path.replaceFirst("x=\\d+", "x={\\$x}");
                    path = path.replaceFirst("y=\\d+", "y={\\$y}");
                    path = path.replaceFirst("z=\\d+", "z={\\$z}");
                    if (path.equalsIgnoreCase(tileUrl.getPath())) continue;
                    path = path.replaceFirst("hl=[^&]+", "hl={\\$lang}");
                    path = path.replaceFirst("&s=[Galieo]*", "");
                    String candidate = "http://" + host + path;
                    tileUrlCandidates.add(new UrlString(candidate));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (tileUrlCandidates.size() == 0) return null;
            if (tileUrlCandidates.size() == 1) return tileUrlCandidates.iterator().next().url;
            Levenshtein similarityAlgo = new Levenshtein();
            MapSource mapSource = ums.mapSourceClass.newInstance();
            String currentUrl = MapSourceTools.loadMapUrl(mapSource, "url");
            if (currentUrl == null) throw new RuntimeException("mapsources url not loaded: " + mapSource);
            ArrayList<UrlString> candidateList = new ArrayList<UrlString>(tileUrlCandidates);
            for (UrlString us : candidateList) {
                us.f = similarityAlgo.getSimilarity(us.url, currentUrl);
            }
            Collections.sort(candidateList);
            return candidateList.get(0).url;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getUppdatedGoogleMapMakerUrl() {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL("http://www.google.com/mapmaker").openConnection();
            InputStream in = c.getInputStream();
            String html = new String(Utilities.getInputBytes(in));
            in.close();
            Pattern p = Pattern.compile("\\\"gwm.([\\d]+)\\\"");
            Matcher m = p.matcher(html);
            if (!m.find()) throw new RuntimeException("pattern not found");
            String number = m.group(1);
            String url = "http://gt{$servernum}.google.com/mt/n=404&v=gwm." + number + "&x={$x}&y={$y}&z={$z}";
            c.disconnect();
            return url;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getUpdateGoogleMapsChinaUrl() {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL("http://ditu.google.com/").openConnection();
            InputStream in = c.getInputStream();
            String html = new String(Utilities.getInputBytes(in));
            in.close();
            c.disconnect();
            Pattern p = Pattern.compile("\\\"(http://mt\\d.google.cn/vt/v[^\\\"]*)\\\"");
            Matcher m = p.matcher(html);
            if (!m.find()) throw new RuntimeException("pattern not found");
            String url = m.group(1);
            url = url.replaceAll("\\\\x26", "&");
            url = url.replaceFirst("[0-3]", "{\\$servernum}");
            if (!url.endsWith("&")) url += "&";
            url = url.replaceFirst("hl=[^&]+", "hl={\\$lang}");
            url += "x={$x}&y={$y}&z={$z}";
            return url;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class UpdateableMapSource {

        public final String updateUrl;

        public final String key;

        public final Class<? extends MapSource> mapSourceClass;

        public final boolean useImgSrcUrlsOnly;

        public UpdateableMapSource(String updateUrl, Class<? extends MapSource> mapSourceClass) {
            this(updateUrl, mapSourceClass, true);
        }

        public UpdateableMapSource(String updateUrl, Class<? extends MapSource> mapSourceClass, boolean useImgSrcUrlsOnly) {
            super();
            this.updateUrl = updateUrl;
            this.key = mapSourceClass.getSimpleName() + ".url";
            this.mapSourceClass = mapSourceClass;
            this.useImgSrcUrlsOnly = useImgSrcUrlsOnly;
        }

        public String getUpdatedUrl(GoogleUrlUpdater g) {
            return g.getUpdatedUrl(this, updateUrl, useImgSrcUrlsOnly);
        }

        protected String processFoundUrl(String url) {
            return url;
        }
    }

    public static class NullPrintWriter extends PrintWriter {

        public NullPrintWriter() throws FileNotFoundException {
            super(new Writer() {

                @Override
                public void close() throws IOException {
                }

                @Override
                public void flush() throws IOException {
                }

                @Override
                public void write(char[] cbuf, int off, int len) throws IOException {
                }
            });
        }
    }

    public static class UrlString implements Comparable<UrlString> {

        public final String url;

        public float f;

        public UrlString(String url) {
            super();
            this.url = url;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((url == null) ? 0 : url.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            UrlString other = (UrlString) obj;
            if (url == null) {
                if (other.url != null) return false;
            } else if (!url.equals(other.url)) return false;
            return true;
        }

        @Override
        public String toString() {
            return String.format("%2.2f %s", f, url);
        }

        public int compareTo(UrlString o) {
            return Float.compare(o.f, f);
        }
    }
}
