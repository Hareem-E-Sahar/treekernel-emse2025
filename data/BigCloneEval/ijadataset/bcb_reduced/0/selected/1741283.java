package Action.parse2D;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import Action.species.InitXml;
import Action.species.Species;

/**
 * @author isobe
 * 
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates. To enable and disable the creation of type
 * comments go to Window>Preferences>Java>Code Generation.
 */
public class TraitXml {

    static Logger log = Logger.getLogger(TraitXml.class);

    public static final String ONE_DIMENSION = "1d";

    public static final String TWO_DIMENSION = "2d";

    public static final int SCALED_MAX_VALUE = 127;

    private File xmlFile;

    private Species species;

    private String type;

    private ArrayList areas;

    private InitXml initXml;

    private String color;

    private float maxDrawValue = 0;

    private Species compositeSpecies;

    public TraitXml(File xmlFile, InitXml initXml, String color, float maxLodValue, Species compositeSpecies) throws ParseException {
        this.xmlFile = xmlFile;
        this.areas = new ArrayList();
        this.initXml = initXml;
        this.color = color;
        setMaxDrawValue(maxLodValue);
        this.compositeSpecies = compositeSpecies;
        try {
            this.parse();
        } catch (Exception e) {
            log.error("file parse error: " + this.xmlFile.getAbsolutePath(), e);
            throw new ParseException(e.getMessage(), 0);
        }
    }

    public TraitXml(File xmlFile, InitXml initXml, String color) throws IOException, ParseException {
        this.xmlFile = xmlFile;
        this.areas = new ArrayList();
        this.initXml = initXml;
        this.color = color;
        try {
            this.parse();
        } catch (Exception e) {
            log.error("file parse error: " + this.xmlFile.getAbsolutePath(), e);
            throw new ParseException(e.getMessage(), 0);
        }
    }

    private void parse() throws IOException {
        Document doc = file2Dom(xmlFile);
        if (doc == null) {
            return;
        }
        Element speciesElement = (Element) doc.getElementsByTagName("species").item(0);
        String speName = speciesElement.getFirstChild().getNodeValue();
        species = initXml.getSpecies(initXml.toAlias(speName), null);
        NodeList nlistDraw = doc.getElementsByTagName("draw");
        setMaxDrawValue(Float.parseFloat(((Element) nlistDraw.item(0)).getAttribute("maxValue")));
        NodeList nlistArea = doc.getElementsByTagName("area");
        for (int i = 0; i < nlistArea.getLength(); i++) {
            Element element = (Element) nlistArea.item(i);
            float value;
            String xChr;
            long xStart;
            long xEnd;
            value = Float.parseFloat(element.getAttribute("value"));
            if (value != 0) {
                StringTokenizer stS = new StringTokenizer(element.getAttribute("xs"), ":");
                StringTokenizer stE = new StringTokenizer(element.getAttribute("xe"), ":");
                xChr = stS.nextToken();
                xStart = this.maxmin2Long(xChr, stS.nextToken());
                stE.nextToken();
                xEnd = this.maxmin2Long(xChr, stE.nextToken());
                if (xStart > xEnd) {
                    long tmp = xEnd;
                    xEnd = xStart;
                    xStart = tmp;
                }
                if (element.hasAttribute("ys") && element.hasAttribute("ye")) {
                    type = TraitXml.TWO_DIMENSION;
                    stS = new StringTokenizer(element.getAttribute("ys"), ":");
                    stE = new StringTokenizer(element.getAttribute("ye"), ":");
                    String yChr = stS.nextToken();
                    long yStart = this.maxmin2Long(yChr, stS.nextToken());
                    stE.nextToken();
                    long yEnd = this.maxmin2Long(yChr, stE.nextToken());
                    if (yStart > yEnd) {
                        long tmp = yEnd;
                        yEnd = yStart;
                        yStart = tmp;
                    }
                    Area area = new Area();
                    area.setValue(scaledDrawValue(value));
                    area.setXSpeciesAlias(species.getAlias());
                    area.setXChr(xChr);
                    area.setXStart(xStart);
                    area.setXEnd(xEnd);
                    area.setYSpeciesAlias(species.getAlias());
                    area.setYChr(yChr);
                    area.setYStart(yStart);
                    area.setYEnd(yEnd);
                    area.setColor(color);
                    areas.add(area);
                } else {
                    type = TraitXml.ONE_DIMENSION;
                    List chrNames = compositeSpecies.getChromosomeNames();
                    for (int j = 0; j < chrNames.size(); j++) {
                        String yChr = (String) chrNames.get(j);
                        long yStart = 0;
                        long yEnd = compositeSpecies.getChromosomeLength(yChr);
                        Area area = new Area();
                        area.setValue(scaledDrawValue(value));
                        area.setXSpeciesAlias(species.getAlias());
                        area.setXChr(xChr);
                        area.setXStart(xStart);
                        area.setXEnd(xEnd);
                        area.setYSpeciesAlias(compositeSpecies.getAlias());
                        area.setYChr(yChr);
                        area.setYStart(yStart);
                        area.setYEnd(yEnd);
                        area.setColor(color);
                        areas.add(area);
                    }
                }
            }
        }
        slice();
    }

    private void slice() {
        ArrayList slicedAreas = new ArrayList();
        List hChrNames = species.getChromosomeNames();
        List vChrNames = compositeSpecies.getChromosomeNames();
        ArrayList[][] chrAreas = new ArrayList[hChrNames.size()][vChrNames.size()];
        for (int i = 0; i < hChrNames.size(); i++) {
            for (int j = 0; j < vChrNames.size(); j++) {
                chrAreas[i][j] = new ArrayList();
            }
        }
        for (int k = 0; k < areas.size(); k++) {
            Area area = (Area) areas.get(k);
            String areaX = area.getXChr();
            String areaY = area.getYChr();
            for (int i = 0; i < hChrNames.size(); i++) {
                String xChr = (String) hChrNames.get(i);
                for (int j = 0; j < vChrNames.size(); j++) {
                    String yChr = (String) vChrNames.get(j);
                    if (areaX.equals(xChr) && areaY.equals(yChr)) {
                        chrAreas[i][j].add(area);
                    }
                }
            }
        }
        for (int i = 0; i < hChrNames.size(); i++) {
            for (int j = 0; j < vChrNames.size(); j++) {
                slicedAreas.addAll(sliceArray(chrAreas[i][j]));
            }
        }
        areas = slicedAreas;
    }

    private ArrayList sliceArray(ArrayList areas) {
        ArrayList newArray = new ArrayList();
        int size = areas.size();
        if (size == 0) {
            return newArray;
        }
        HashSet xpointSet = new HashSet();
        HashSet ypointSet = new HashSet();
        for (int i = 0; i < size; i++) {
            Area area = (Area) areas.get(i);
            xpointSet.add(new Long(area.getXStart()));
            xpointSet.add(new Long(area.getXEnd()));
            ypointSet.add(new Long(area.getYStart()));
            ypointSet.add(new Long(area.getYEnd()));
        }
        Long[] xpoints = new Long[xpointSet.size()];
        Long[] ypoints = new Long[ypointSet.size()];
        xpointSet.toArray(xpoints);
        ypointSet.toArray(ypoints);
        Arrays.sort(xpoints);
        Arrays.sort(ypoints);
        Area area = (Area) areas.get(0);
        String xAlias = area.getXSpeciesAlias();
        String xChr = area.getXChr();
        String yAlias = area.getYSpeciesAlias();
        String yChr = area.getYChr();
        String color = this.color;
        for (int i = 0; i < xpoints.length - 1; i++) {
            long xs = xpoints[i].longValue();
            long xe = xpoints[i + 1].longValue();
            for (int j = 0; j < ypoints.length - 1; j++) {
                long ys = ypoints[j].longValue();
                long ye = ypoints[j + 1].longValue();
                int value = getMaxValue(xChr, yChr, xs, xe, ys, ye);
                if (value != 0) {
                    Area addArea = new Area();
                    addArea.setColor(color);
                    addArea.setValue(value);
                    addArea.setXSpeciesAlias(xAlias);
                    addArea.setXChr(xChr);
                    addArea.setXStart(xs);
                    addArea.setXEnd(xe);
                    addArea.setYSpeciesAlias(yAlias);
                    addArea.setYChr(yChr);
                    addArea.setYStart(ys);
                    addArea.setYEnd(ye);
                    newArray.add(addArea);
                }
            }
        }
        return newArray;
    }

    private int getMaxValue(String xChr, String yChr, long xs, long xe, long ys, long ye) {
        int value = 0;
        long interX = (xs + xe) / 2;
        long interY = (ys + ye) / 2;
        for (int i = 0; i < areas.size(); i++) {
            Area area = (Area) areas.get(i);
            if (xChr.equals(area.getXChr()) && yChr.equals(area.getYChr())) {
                if (area.getXStart() <= xs && xe <= area.getXEnd() && area.getYStart() <= ys && ye <= area.getYEnd()) {
                    if (value < area.getValue()) {
                        value = area.getValue();
                    }
                }
            }
        }
        return value;
    }

    private long maxmin2Long(String chrName, String position) {
        if (position.equalsIgnoreCase("max")) {
            return species.getChromosomeLength(chrName);
        } else if (position.equalsIgnoreCase("min")) {
            return 0;
        } else {
            return Long.parseLong(position);
        }
    }

    /**
	 * Returns the areas.
	 * 
	 * @return ArrayList
	 */
    public ArrayList getAreas() {
        return areas;
    }

    private Document file2Dom(File file) {
        DocumentBuilderFactory _factory = DocumentBuilderFactory.newInstance();
        _factory.setNamespaceAware(false);
        _factory.setValidating(false);
        DocumentBuilder builder = null;
        try {
            builder = _factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            System.out.println(e.toString());
        }
        Document _doc = null;
        try {
            try {
                _doc = builder.parse(file);
            } catch (IOException e) {
                System.out.println(e.toString());
                _doc = null;
            }
        } catch (SAXException e) {
            log.error(e);
        }
        return _doc;
    }

    /**
	 * Returns the alias.
	 * 
	 * @return String
	 */
    public String getAlias() {
        return species.getAlias();
    }

    /**
	 * Returns the type.
	 * 
	 * @return String
	 */
    public String getType() {
        return type;
    }

    private int scaledDrawValue(float value) {
        int baseMinValue = Math.round(0.2f * SCALED_MAX_VALUE);
        int scaledValue = baseMinValue + Math.round((SCALED_MAX_VALUE - baseMinValue) * value / maxDrawValue);
        if (type.equals(TWO_DIMENSION)) {
            scaledValue = scaledValue * 2;
        }
        return scaledValue;
    }

    /**
	 * @param i
	 */
    public void setMaxDrawValue(float maxValue) {
        this.maxDrawValue = Math.max(this.maxDrawValue, maxValue);
    }
}
