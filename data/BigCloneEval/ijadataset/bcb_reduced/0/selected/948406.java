package com.bluebrim.font.impl.server;

import java.awt.*;
import java.awt.font.*;
import java.io.*;
import java.util.*;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.*;
import com.bluebrim.base.shared.*;
import com.bluebrim.base.shared.debug.*;
import com.bluebrim.font.impl.server.type1.*;
import com.bluebrim.font.impl.shared.*;
import com.bluebrim.font.impl.shared.metrics.*;
import com.bluebrim.font.shared.*;
import com.bluebrim.font.shared.metrics.*;
import com.bluebrim.xml.shared.*;

public class CoFontRepository implements CoFontRepositoryIF {

    public static final String XML_TAG = "font-repository";

    public static final String XML_FALLBACK_FAMILY = "fallback-family";

    public static final String XML_FONT_FACES = "font-faces";

    public static final String XML_SPEC_MAPPING = "spec-mapping";

    public static final String XML_METRICS_DATA = "metrics-data";

    public static final String XML_AWT_DATA = "awt-data";

    public static final String XML_POSTSCRIPT_DATA = "postscript-data";

    public static final String XML_FILE_CONTAINERS = "file-containers";

    public static final String XML_CATALOGS = "catalogs";

    public static final String XML_FONT_FAMILIES = "font-families";

    private static final int INITIAL_FONT_FACE_VERSION = 0;

    private int m_modCount;

    private CoStatusShower m_shower = new CoStatusShower.StdOutShower();

    private Set<CoFontFace> m_fontFaces = new HashSet<CoFontFace>();

    private Map<CoFontFaceSpec, CoFontFace> m_specMapping = new HashMap<CoFontFaceSpec, CoFontFace>();

    private Map<CoFontFace, CoFontMetricsData> m_metricsDataItems = new HashMap<CoFontFace, CoFontMetricsData>();

    private Map<CoFontFace, CoFontAwtData> m_awtDataItems = new HashMap<CoFontFace, CoFontAwtData>();

    private Map<CoFontFace, CoFontPostscriptData> m_postscriptDataItems = new HashMap<CoFontFace, CoFontPostscriptData>();

    private Map<CoFontFace, CoFontFileContainer> m_fontFileContainers = new HashMap<CoFontFace, CoFontFileContainer>();

    private String m_fallbackFamily;

    private Map m_catalogs = new HashMap();

    private CoFontCatalog m_fontFamilies = new CoFontCatalog();

    private ServerFontMapper m_fontMapper = new ServerFontMapper();

    /**
	 * A singleton caching client side interface to the font repository.
	 * Creation date: (2001-04-25 12:52:41)
	 * @author Magnus Ihse <magnus.ihse@appeal.se>
	 */
    public class ServerFontMapper extends CoAbstractFontMapper {

        private ServerFontMapper() {
            super();
        }

        /**
		 * Return a Set of the names of all CoFontCatalogs.
		 * Creation date: (2001-05-15 12:23:51)
		 * @author Magnus Ihse (magnus.ihse@appeal.se)
		 */
        public Set getAllCatalogs() {
            return m_catalogs.keySet();
        }

        /**
		 * Return the CoFontCatalog associated with this font catalog name, or null if none.
		 * Creation date: (2001-05-15 12:24:24)
		 * @author Magnus Ihse (magnus.ihse@appeal.se)
		 */
        public CoFontCatalog getCatalog(String name) {
            if (CoAssertion.ASSERT) CoAssertion.notNull(name, "name");
            return (CoFontCatalog) m_catalogs.get(name);
        }

        /**
		 * Return the CoFontFace currently associated with this CoFontFaceSpec, or null if none. This will
		 * only return an exact match. See getFontFaceOrFallback for a failsafe version.
		 * Creation date: (2001-05-15 12:24:53)
		 * @author Magnus Ihse (magnus.ihse@appeal.se)
		 */
        public CoFontFace getFontFace(CoFontFaceSpec spec) {
            if (CoAssertion.ASSERT) CoAssertion.notNull(spec, "spec");
            return (CoFontFace) m_specMapping.get(spec);
        }

        /**
		 * Returns true if the CoFontFace is "removed". Note that "removed" actually means "hidden"; a CoFontFace
		 * is normally never actually removed, but just made hidden so no new texts can be written with that font.
		 * A complete removal would mean that old text no longer could be rendered correct, and that is an
		 * unfortunate situation. */
        public boolean isRemoved(CoFontFace face) {
            if (CoAssertion.ASSERT) CoAssertion.notNull(face, "face");
            return !m_specMapping.containsKey(face.getSpec());
        }

        /**
		 * Fetch the AwtData from the repository for the associated CoFontFace, or from the cache if possible.
		 */
        public CoFontAwtData getAwtData(CoFontFace face) {
            if (CoAssertion.ASSERT) CoAssertion.notNull(face, "face");
            CoFontAwtData data = (CoFontAwtData) m_awtDataItems.get(face);
            return data;
        }

        public CoFontCatalog getFontFamilies() {
            return m_fontFamilies;
        }

        /**
		 * Fetch the CoFontFileContainer from the repository for the associated CoFontFace, or from the cache if possible.
		 */
        public CoFontFileContainer getFontFileContainer(CoFontFace face) {
            if (CoAssertion.ASSERT) CoAssertion.notNull(face, "face");
            CoFontFileContainer data = (CoFontFileContainer) m_fontFileContainers.get(face);
            return data;
        }

        /**
		 * Fetch the font metrics data from the repository for the associated CoFontFace, or from the cache if possible.
		 */
        public CoFontMetricsData getMetricsData(CoFontFace face) {
            if (CoAssertion.ASSERT) CoAssertion.notNull(face, "face");
            CoFontMetricsData data = (CoFontMetricsData) m_metricsDataItems.get(face);
            return data;
        }

        /**
		 * Fetch the postscript data from the repository for the associated CoFontFace, or from the cache if possible.
		 */
        public CoFontPostscriptData getPostscriptData(CoFontFace face) {
            if (CoAssertion.ASSERT) CoAssertion.notNull(face, "face");
            CoFontPostscriptData data = (CoFontPostscriptData) m_postscriptDataItems.get(face);
            return data;
        }

        public String getRealFallbackFamily() {
            return getFallbackFamily();
        }

        public Set getSpecsForFamily(String familyName) {
            if (CoAssertion.ASSERT) CoAssertion.notNull(familyName, "familyName");
            Set familySpecs = new HashSet();
            Iterator i = m_specMapping.keySet().iterator();
            while (i.hasNext()) {
                CoFontFaceSpec spec = (CoFontFaceSpec) i.next();
                if (spec.getFamilyName().equals(familyName)) {
                    familySpecs.add(spec);
                }
            }
            return familySpecs;
        }

        public Set getFontFaces() {
            return m_fontFaces;
        }

        public boolean isAwtFontAvailable(CoFontFace face) {
            return true;
        }
    }

    private CoFontRepository() {
    }

    public synchronized void addAll(CoFontRepositoryIF other) {
        m_fontFaces.addAll(other.getFontFaces());
        m_specMapping.putAll(other.getSpecMapping());
        m_metricsDataItems.putAll(other.getMetricsDataItems());
        m_awtDataItems.putAll(other.getAwtDataItems());
        m_postscriptDataItems.putAll(other.getPostscriptDataItems());
        m_fontFileContainers.putAll(other.getFontFileContainers());
        m_catalogs.putAll(other.getCatalogs());
        m_fontFamilies.addAll(other.getFontFamilies());
        markAsModified();
    }

    /**
	 * add a font family. does not trigger update, make sure caller does that.
	 */
    protected void addFontFamily(String familyName) {
        m_fontFamilies.add(familyName);
        if (m_fallbackFamily == null) {
            setFallbackFamily(familyName);
        }
    }

    protected boolean catalogIsValid(CoFontCatalog catalog) {
        Iterator i = catalog.iterator();
        while (i.hasNext()) {
            String familyName = (String) i.next();
            if (!fontFamilyExists(familyName)) {
                return false;
            }
        }
        return true;
    }

    /**
	 * Creates a new FontRepository. This should only be done once by the system. The correct way
	 * of getting hold of the font repository, once instantiated, is through the following:
	 <code>CoSystemProxy.getSystem().getFontRepository()</code>.
	 * Note that most users really want a CoFontMapper or a CoFontRepositoryManager instead.
	 * Also note that the font repository is not fully functional until the initial font have been installed.
	 * This can be done by calling either {@link #installInitialData} or {@link #installInitialFont}.
	 *
	 * @see #installInitialData
	 * @see #installInitialFont
	 * @see CoSystemProxy#getSystem
	 * @see CoSystemIF#getFontRepository
	 */
    public static synchronized CoFontRepository createFontRepository() {
        CoFontRepository repository = new CoFontRepository();
        return repository;
    }

    /**
	 * check if a font family exists in the spec mapping.
	 */
    protected boolean fontFamilyExists(String familyName) {
        Iterator i = m_specMapping.keySet().iterator();
        while (i.hasNext()) {
            CoFontFaceSpec spec = (CoFontFaceSpec) i.next();
            if (spec.getFamilyName().equals(familyName)) {
                return true;
            }
        }
        return false;
    }

    public CoFontAwtData getAwtData(CoFontFace face) {
        return (CoFontAwtData) m_awtDataItems.get(face);
    }

    public Map getAwtDataItems() {
        return m_awtDataItems;
    }

    public CoFontCatalog getCatalog(String name) {
        return (CoFontCatalog) m_catalogs.get(name);
    }

    public Map getCatalogs() {
        return m_catalogs;
    }

    public synchronized String getFallbackFamily() {
        return m_fallbackFamily;
    }

    public Set getFontFaces() {
        return m_fontFaces;
    }

    public CoFontCatalog getFontFamilies() {
        return m_fontFamilies;
    }

    public CoFontFileContainer getFontFileContainer(CoFontFace face) {
        return (CoFontFileContainer) m_fontFileContainers.get(face);
    }

    public Map getFontFileContainers() {
        return m_fontFileContainers;
    }

    public CoAbstractFontMapper getFontMapper() {
        return m_fontMapper;
    }

    public CoFontMetricsData getMetricsData(CoFontFace face) {
        return (CoFontMetricsData) m_metricsDataItems.get(face);
    }

    public Map getMetricsDataItems() {
        return m_metricsDataItems;
    }

    public CoFontPostscriptData getPostscriptData(CoFontFace face) {
        return (CoFontPostscriptData) m_postscriptDataItems.get(face);
    }

    public Map getPostscriptDataItems() {
        return m_postscriptDataItems;
    }

    public Map getSpecMapping() {
        return m_specMapping;
    }

    public synchronized boolean hideFontFace(CoFontFaceSpec spec) {
        if (m_specMapping.containsKey(spec)) {
            String familyName = spec.getFamilyName();
            m_specMapping.remove(spec);
            if (!fontFamilyExists(familyName)) {
                removeFontFamily(familyName);
            }
            markAsModified();
            updateFallbackFamily();
            return true;
        } else {
            return false;
        }
    }

    private synchronized void installFontFile(String fileName, CoFontFaceSpec spec) throws CoFontException {
        m_shower.showStatus("Installing font " + spec + " from file " + fileName);
        File file;
        try {
            file = new ClassPathResource(fileName).getFile();
        } catch (IOException e) {
            throw new CoFontException(e);
        }
        CoFontFileInfoExtractor parser = CoAbstractFontFileInfoExtractor.parseFontFile(file);
        CoFontMetricsData metrics = parser.getMetricsData();
        CoFontPostscriptData postscriptData = parser.getPostscriptData();
        CoFontFileContainer fileContainer = parser.getFileContainer();
        CoFontAwtData awtData = parser.suggestedAwtData();
        if (parser instanceof CoType1FileInfoExtractor) {
            awtData.awtWorkaround_setType1(true);
        }
        if (fileName.toLowerCase().endsWith(".ttf")) {
            Font font;
            try {
                font = Font.createFont(Font.TRUETYPE_FONT, file);
            } catch (FontFormatException e) {
                throw new CoFontException(e);
            } catch (IOException e) {
                throw new CoFontException(e);
            }
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(font);
        }
        installFontFace(spec, metrics, postscriptData, awtData, fileContainer);
    }

    public synchronized void installInitialData(CoStatusShower shower) {
        if (shower != null) m_shower = shower;
        System.out.println("Loading Font Repository initial data");
        installInitialFont();
        if (!readFromXmlFile()) initializeFromFontMappingFile();
        System.out.println("Font Repository data loading finished.");
        markAsModified();
    }

    private void initializeFromFontMappingFile() {
        String iniFileName = "fonts.ini";
        try {
            ClassPathResource classPathResource = new ClassPathResource(iniFileName);
            InputStream iniStream = classPathResource.getInputStream();
            Reader initFileReader = new InputStreamReader(iniStream);
            readFontInitFile(initFileReader);
            initFileReader.close();
            iniStream.close();
        } catch (IOException ioe) {
            System.err.println("ERROR: Font ini file " + iniFileName + " not found.");
        }
    }

    private boolean readFromXmlFile() {
        String xmlFileName = System.getProperty(PROPERTY_KEY_XML_DATA);
        File xmlFile = null;
        if (xmlFileName != null) {
            xmlFile = new File(xmlFileName);
        }
        if (xmlFile != null && xmlFile.exists()) {
            System.out.println("... from XML file: " + xmlFile.getAbsolutePath());
            CoXmlContext xmlContext = new CoXmlContext();
            Object newRepository = CoXmlFileUtilities.loadXml(xmlFile, xmlContext);
            if (newRepository == null) {
                System.err.println("ERROR: Font Repository XML file (" + xmlFileName + ") as specified by the calvin.fonts.repository.xml property is not a correct XML file");
            } else {
                if (newRepository instanceof CoFontRepository) {
                    addAll((CoFontRepository) newRepository);
                } else {
                    System.err.println("ERROR: Font Repository XML file (" + xmlFileName + ") as specified by the calvin.fonts.repository.xml property is not a Font Repository XML file");
                }
            }
            return true;
        }
        return false;
    }

    protected void installInitialFont() {
        CoFontFaceSpec spec = new CoFontFaceSpec(CoAbstractFontMapper.BASIC_FALLBACK_FONT, CoFontConstants.NORMAL_WEIGHT, CoFontConstants.NORMAL_STYLE, CoFontConstants.NORMAL_VARIANT, CoFontConstants.NORMAL_STRETCH);
        Font sansserif = new Font(CoAbstractFontMapper.BASIC_FALLBACK_FONT, Font.PLAIN, 10);
        LineMetrics awtMetrics = sansserif.getLineMetrics("demo text needed by AWT", new FontRenderContext(null, false, false));
        CoLineMetrics lineMetrics = new CoLineMetricsImplementation(awtMetrics.getAscent(), awtMetrics.getDescent(), awtMetrics.getHeight(), awtMetrics.getLeading(), awtMetrics.getStrikethroughOffset(), awtMetrics.getStrikethroughThickness(), awtMetrics.getUnderlineOffset(), awtMetrics.getUnderlineThickness(), awtMetrics.getAscent() * 2 / 3, awtMetrics.getAscent());
        float advance = new Font(CoAbstractFontMapper.BASIC_FALLBACK_FONT, Font.PLAIN, 1).createGlyphVector(CoAbstractFontMapper.getFontRenderContext(), "x").getGlyphMetrics(0).getAdvance();
        CoHorizontalMetrics horizontalMetrics = new CoFixedWidthHorizontalMetrics(advance);
        CoPairKerningMetrics pairKerningMetrics = new CoPairKerningMetricsImplementation();
        CoTrackingMetrics trackingMetrics = new CoTrackingMetricsImplementation(new float[0], new float[0]);
        CoFontMetricsData data = new CoFontMetricsDataImplementation(horizontalMetrics, lineMetrics, pairKerningMetrics, trackingMetrics);
        CoFontPostscriptData postscriptData = new CoFontPostscriptDataImplementation(new byte[0], "");
        CoFontAwtData awtData = new CoFontAwtDataImplementation(false, false, CoAbstractFontMapper.BASIC_FALLBACK_FONT);
        CoFontFileContainer fileContainer = null;
        CoFontFace face = new CoFontFace(spec, INITIAL_FONT_FACE_VERSION);
        m_awtDataItems.put(face, awtData);
        m_postscriptDataItems.put(face, postscriptData);
        m_metricsDataItems.put(face, data);
        m_fontFileContainers.put(face, fileContainer);
        m_specMapping.put(spec, face);
    }

    public synchronized boolean permanentlyRemoveFontFace(CoFontFace face) {
        if (m_fontFaces.contains(face)) {
            if (m_specMapping.get(face.getSpec()).equals(face)) {
                hideFontFace(face.getSpec());
            }
            m_fontFaces.remove(face);
            m_metricsDataItems.remove(face);
            m_awtDataItems.remove(face);
            m_postscriptDataItems.remove(face);
            m_fontFileContainers.remove(face);
            markAsModified();
            return true;
        } else {
            return false;
        }
    }

    private void readFontInitFile(Reader initFileReader) {
        try {
            LineNumberReader in = new LineNumberReader(initFileReader);
            String line = null;
            while (in.ready()) {
                line = in.readLine();
                if (line == null) break;
                if (line.length() == 0) continue;
                if (line.charAt(0) == '#') continue;
                StringTokenizer tokenizer = new StringTokenizer(line, ",");
                if (tokenizer.countTokens() < 6) {
                    System.out.println("Error in font init file on line " + in.getLineNumber());
                    System.out.println("Line is: " + line);
                    System.out.println("Aborting font reading!");
                    return;
                }
                String fontName = tokenizer.nextToken();
                String fileName = tokenizer.nextToken();
                int weight = Integer.parseInt(tokenizer.nextToken());
                int style = Integer.parseInt(tokenizer.nextToken());
                int variant = Integer.parseInt(tokenizer.nextToken());
                int stretch = Integer.parseInt(tokenizer.nextToken());
                installFontFile(fileName, new CoFontFaceSpec(fontName, weight, style, variant, stretch));
            }
        } catch (CoFontException e) {
            System.out.println("ERROR: Test data fonts were not correctly read!!!");
            System.out.println(e.toString());
        } catch (IOException e) {
            System.out.println("Font reading error.");
            System.out.println(e.toString());
        }
        markAsModified();
    }

    /**
	 * create a new fontface. returns true if the font is created, and false if it replaces
	 * another fontface with the same spec.
	 */
    public synchronized boolean registerNewFontFace(CoFontFaceSpec spec, CoFontMetricsData data, CoFontPostscriptData postscriptData, CoFontAwtData awtData, CoFontFileContainer fileContainer) throws CoFontException {
        boolean isNew = installFontFace(spec, data, postscriptData, awtData, fileContainer);
        markAsModified();
        return isNew;
    }

    public synchronized boolean removeCatalog(String name) {
        if (m_catalogs.containsKey(name)) {
            m_catalogs.remove(name);
            markAsModified();
            return true;
        } else {
            return false;
        }
    }

    /**
	 * remove a font family from all catalogs. does not trigger update, make sure caller does that.
	 */
    protected void removeFontFamily(String familyName) {
        Iterator i = m_catalogs.keySet().iterator();
        while (i.hasNext()) {
            CoFontCatalog catalog = (CoFontCatalog) i.next();
            catalog.remove(familyName);
        }
        m_fontFamilies.remove(familyName);
    }

    public synchronized void replaceWith(CoFontRepositoryIF other) {
        m_fontFaces = other.getFontFaces();
        m_specMapping = other.getSpecMapping();
        m_metricsDataItems = other.getMetricsDataItems();
        m_awtDataItems = other.getAwtDataItems();
        m_postscriptDataItems = other.getPostscriptDataItems();
        m_fontFileContainers = other.getFontFileContainers();
        m_fallbackFamily = other.getFallbackFamily();
        m_catalogs = other.getCatalogs();
        m_fontFamilies = other.getFontFamilies();
        markAsModified();
    }

    public synchronized void setFallbackFamily(String familyName) {
        m_fallbackFamily = familyName;
        markAsModified();
    }

    /**
	 * updateCatalog method comment. return true if catalog is correct.
	 */
    public synchronized boolean updateCatalog(String name, CoFontCatalog catalog) {
        if (!catalogIsValid(catalog)) {
            return false;
        }
        CoFontCatalog oldCatalog = (CoFontCatalog) m_catalogs.get(name);
        if (oldCatalog == null || !oldCatalog.equals(catalog)) {
            m_catalogs.put(name, catalog);
            markAsModified();
        }
        return true;
    }

    public void xmlAddSubModel(String parameter, Object subModel, CoXmlContext context) throws CoXmlReadException {
        if (subModel instanceof Map) {
            if (XML_SPEC_MAPPING.equals(parameter)) {
                m_specMapping = new HashMap();
                Map xmlSpecMap = (Map) subModel;
                Iterator i = xmlSpecMap.keySet().iterator();
                while (i.hasNext()) {
                    CoFontFaceSpec.XmlWrapper specWrapper = (CoFontFaceSpec.XmlWrapper) i.next();
                    CoFontFaceSpec spec = specWrapper.getFontFaceSpec();
                    CoFontFace face = (CoFontFace) xmlSpecMap.get(specWrapper);
                    m_specMapping.put(spec, face);
                }
            } else if (XML_METRICS_DATA.equals(parameter)) {
                m_metricsDataItems = new HashMap((Map) subModel);
            } else if (XML_AWT_DATA.equals(parameter)) {
                m_awtDataItems = new HashMap((Map) subModel);
            } else if (XML_POSTSCRIPT_DATA.equals(parameter)) {
                m_postscriptDataItems = new HashMap((Map) subModel);
            } else if (XML_FILE_CONTAINERS.equals(parameter)) {
                m_fontFileContainers = new HashMap((Map) subModel);
            } else if (XML_CATALOGS.equals(parameter)) {
                m_catalogs = new HashMap((Map) subModel);
            }
        } else if (subModel instanceof Iterator) {
            Iterator i = (Iterator) subModel;
            if (XML_FONT_FACES.equals(parameter)) {
                while (i.hasNext()) {
                    m_fontFaces.add((CoFontFace) i.next());
                }
            } else if (XML_FONT_FAMILIES.equals(parameter)) {
                while (i.hasNext()) {
                    m_fontFamilies.add(i.next());
                }
            }
        } else if (subModel instanceof String) {
            if (XML_FALLBACK_FAMILY.equals(parameter)) {
                m_fallbackFamily = (String) subModel;
            }
        }
    }

    /**
	 * Called before XML import. See {@link CoXmlImportEnabledIF} for details.
	 */
    public static CoXmlImportEnabledIF xmlCreateModel(Object superModel, Node node, CoXmlContext context) {
        return new CoFontRepository();
    }

    public void xmlImportFinished(Node node, CoXmlContext context) throws CoXmlReadException {
        markAsModified();
    }

    public void xmlVisit(CoXmlVisitorIF visitor) {
        visitor.export(XML_FONT_FAMILIES, m_fontFamilies);
        visitor.exportString(XML_FALLBACK_FAMILY, m_fallbackFamily);
        visitor.export(XML_CATALOGS, m_catalogs);
        visitor.export(XML_FONT_FACES, m_fontFaces);
        Map xmlSpecMapping = new HashMap();
        Iterator i = m_specMapping.keySet().iterator();
        while (i.hasNext()) {
            CoFontFaceSpec spec = (CoFontFaceSpec) i.next();
            CoFontFace face = (CoFontFace) m_specMapping.get(spec);
            xmlSpecMapping.put(new CoFontFaceSpec.XmlWrapper(spec), face);
        }
        visitor.export(XML_SPEC_MAPPING, xmlSpecMapping);
        visitor.export(XML_METRICS_DATA, m_metricsDataItems);
        visitor.export(XML_AWT_DATA, m_awtDataItems);
        visitor.export(XML_POSTSCRIPT_DATA, m_postscriptDataItems);
        visitor.export(XML_FILE_CONTAINERS, m_fontFileContainers);
    }

    /**
	 * create a new fontface. returns true if the font is created, and false if it replaces
	 * another fontface with the same spec.
	 */
    protected synchronized boolean installFontFace(CoFontFaceSpec spec, CoFontMetricsData data, CoFontPostscriptData postscriptData, CoFontAwtData awtData, CoFontFileContainer fileContainer) throws CoFontException {
        CoFontFace face;
        boolean isNew;
        CoFontFace oldFace = (CoFontFace) m_specMapping.get(spec);
        if (oldFace != null) {
            face = oldFace.createNextVersion();
            isNew = false;
        } else {
            face = new CoFontFace(spec, INITIAL_FONT_FACE_VERSION);
            isNew = true;
        }
        m_awtDataItems.put(face, awtData);
        m_postscriptDataItems.put(face, postscriptData);
        m_metricsDataItems.put(face, data);
        m_fontFileContainers.put(face, fileContainer);
        if (!fontFamilyExists(spec.getFamilyName())) {
            addFontFamily(spec.getFamilyName());
        }
        m_fontFaces.add(face);
        m_specMapping.put(spec, face);
        updateFallbackFamily();
        return isNew;
    }

    protected void markAsModified() {
        m_modCount++;
        if (CoAssertion.SIMULATION_SUPPORT) CoAssertion.addChangedObject(this);
    }

    private synchronized void updateFallbackFamily() {
        if (m_fontFamilies == null || m_fontFamilies.isEmpty()) {
            if (m_fallbackFamily != null) {
                m_fallbackFamily = null;
                markAsModified();
            }
        } else {
            if (m_fallbackFamily == null) {
                setFallbackFamily((String) (m_fontFamilies.iterator().next()));
                System.out.println("Warning: No fallback font family specified. Defaulting to: " + m_fallbackFamily);
                markAsModified();
            }
        }
    }
}
