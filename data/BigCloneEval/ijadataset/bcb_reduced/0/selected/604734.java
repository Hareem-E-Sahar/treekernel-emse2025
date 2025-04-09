package backend.core.persistent.berkeley.test;

import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.Hashtable;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import junit.framework.TestCase;
import backend.core.AbstractConcept;
import backend.core.AbstractONDEXIterator;
import backend.core.AttributeName;
import backend.core.CV;
import backend.core.ConceptAccession;
import backend.core.ConceptClass;
import backend.core.ConceptGDS;
import backend.core.ConceptName;
import backend.core.EvidenceType;
import backend.core.index.IndexONDEXGraph;
import backend.core.persistent.berkeley.BerkeleyEnv;
import backend.core.security.Session;
import backend.exchange.xml.ConceptParser;
import backend.exchange.xml.XmlParser;
import backend.logging.ONDEXLogger;

/**
 * This class tests the Concept Parser. You must have the
 * "testConcepts.xml"-file in your workspace.
 * 
 * @author sierenk , ernsts
 * 
 */
public class ConceptIndexTest extends TestCase {

    IndexONDEXGraph ig = null;

    BerkeleyEnv benv = null;

    Collection<ConceptClass> concClass;

    Session s = Session.NONE;

    String ondexDir = System.getProperty("ondex.dir");

    protected void setUp() throws Exception {
        String filename = System.getProperty("ondex.dir") + File.separator + "xml" + File.separator + "testConcepts.xml";
        File dir = new File(ondexDir + File.separator + "dbs" + File.separator + "berkeleyTest");
        dir.mkdir();
        benv = new BerkeleyEnv(s, ondexDir + File.separator + "dbs" + File.separator + "berkeleyTest", new ONDEXLogger());
        ig = new IndexONDEXGraph(s, benv.getONDEXGraph());
        System.setProperty("javax.xml.stream.XMLInputFactory", "com.bea.xml.stream.MXParserFactory");
        XMLInputFactory xmlif = XMLInputFactory.newInstance();
        System.out.println("INPUT FACTORY: " + xmlif);
        XMLStreamReader xmlr = xmlif.createXMLStreamReader(new FileReader(filename));
        XmlParser parser = new XmlParser();
        Hashtable<Integer, Integer> idOldNew = new Hashtable<Integer, Integer>();
        parser.registerParser("concept", new ConceptParser(s, ig, idOldNew));
        parser.parse(xmlr);
        xmlr.close();
    }

    private void deleteTree(File path) {
        File files[] = path.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) deleteTree(files[i]);
            System.out.println("delete " + files[i].getPath());
            files[i].delete();
        }
        path.delete();
    }

    private void deleteTree(String path) {
        deleteTree(new File(path));
    }

    protected void tearDown() throws Exception {
        ig = null;
        benv.cleanup();
        benv = null;
        deleteTree(ondexDir + File.separator + "dbs" + File.separator + "berkeleyTest");
    }

    public void testConceptParsing() throws Exception {
        assertEquals("Christmas-tree", ig.getConcept(s, new Integer(1)).getConceptName(s).getName(s));
        assertEquals("Norway Spruce", ig.getConcept(s, new Integer(2)).getConceptName(s).getName(s));
        assertEquals("Picea abies", ig.getConcept(s, new Integer(2)).getConceptName(s, "Picea abies").getName(s));
        assertEquals(null, ig.getConcept(s, new Integer(5)).getConceptName(s));
        AbstractONDEXIterator col = ig.getConcepts(s);
        assertTrue(col.contains(ig.getConcept(s, new Integer(1))));
        assertTrue(col.contains(ig.getConcept(s, new Integer(2))));
        assertTrue(col.contains(ig.getConcept(s, new Integer(3))));
        assertTrue(col.contains(ig.getConcept(s, new Integer(5))));
    }

    public void testCreateDeleteConcept() throws Exception {
        Hashtable<String, Integer> idMapping = new Hashtable<String, Integer>();
        AbstractConcept c = ig.createConcept(s, "33290", ig.getONDEXGraphData(s).getCV(s, "TP"), ig.getONDEXGraphData(s).getConceptClass(s, "Taxon"), ig.getONDEXGraphData(s).getEvidenceType(s, "Taxonomy"));
        idMapping.put(c.getPID(s), c.getId(s));
        AbstractConcept ac = (ig.createConcept(s, "3329011", ig.getONDEXGraphData(s).getCV(s, "TP"), ig.getONDEXGraphData(s).getConceptClass(s, "Taxon"), ig.getONDEXGraphData(s).getEvidenceType(s, "Taxonomy")));
        idMapping.put(ac.getPID(s), ac.getId(s));
        AbstractConcept abc = (ig.createConcept(s, "33211", ig.getONDEXGraphData(s).getCV(s, "TP"), ig.getONDEXGraphData(s).getConceptClass(s, "Taxon"), ig.getONDEXGraphData(s).getEvidenceType(s, "Taxonomy")));
        idMapping.put(abc.getPID(s), abc.getId(s));
        assertEquals(7, ig.getIntID(c));
        assertEquals(ig.getConcept(7), c);
        assertEquals(8, ig.getIntID(ac));
        assertEquals(ig.getConcept(8), ac);
        assertEquals(9, ig.getIntID(abc));
        assertEquals(ig.getConcept(9), abc);
        c = ig.deleteConcept(s, idMapping.get("33290"));
        ac = ig.deleteConcept(s, idMapping.get("3329011"));
        abc = ig.deleteConcept(s, idMapping.get("33211"));
        assertNull(ig.getConcept(7));
        assertNull(ig.getConcept(s, idMapping.get("33290")));
        assertNull(ig.getConcept(8));
        assertNull(ig.getConcept(s, idMapping.get("3329011")));
        assertNull(ig.getConcept(9));
        assertNull(ig.getConcept(s, idMapping.get("33211")));
        assertEquals(7, ig.getIntID(c));
        assertEquals(8, ig.getIntID(ac));
        assertEquals(9, ig.getIntID(abc));
    }

    public void testConceptIndex() throws Exception {
        AbstractConcept a = ig.getConcept(s, new Integer(6));
        assertNotNull(ig.getIntID(a));
        assertNotNull(ig.getConcept(5));
        assertNotNull(ig.getConcept(6));
    }

    public void testConceptNameParsing() throws Exception {
        assertEquals("Christmas-tree", ig.getConcept(s, new Integer(1)).getConceptName(s).getName(s));
        assertEquals("Norway Spruce", ig.getConcept(s, new Integer(2)).getConceptName(s).getName(s));
        assertEquals("Picea abies", ig.getConcept(s, new Integer(2)).getConceptName(s, "Picea abies").getName(s));
        assertEquals(null, ig.getConcept(s, new Integer(5)).getConceptName(s));
        AbstractONDEXIterator<ConceptName> cncol1 = ig.getConcept(s, new Integer(1)).getConceptNames(s);
        assertEquals(4, cncol1.size());
        AbstractONDEXIterator<ConceptName> cncol2 = ig.getConcept(s, new Integer(2)).getConceptNames(s);
        assertEquals(3, cncol2.size());
        AbstractONDEXIterator<ConceptName> cncol3 = ig.getConcept(s, new Integer(3)).getConceptNames(s);
        assertEquals(2, cncol3.size());
        AbstractONDEXIterator<ConceptName> cncol4 = ig.getConcept(s, new Integer(5)).getConceptNames(s);
        assertEquals(0, cncol4.size());
    }

    public void testConceptAccessionParsing() throws Exception {
        assertEquals("accession", ig.getConcept(s, new Integer(6)).getConceptName(s).getName(s));
        CV cv = ig.getONDEXGraphData(s).getCV(s, "TF");
        assertNotNull(cv);
        assertNotNull(ig.getConcept(s, new Integer(6)).getConceptAccession(s, "G000001", cv));
        ConceptAccession ca = ig.getConcept(s, new Integer(6)).getConceptAccession(s, "G000001", cv);
        assertEquals("G000001", ca.getAccession(s));
        assertEquals(cv, ca.getElementOf(s));
        assertEquals("TF", ca.getElementOf(s).getName(s));
        assertEquals("TF", ca.getElementOf(s).getFullname(s));
        assertEquals("BIOBASE Transfac Database", ca.getElementOf(s).getDescription(s));
        assertFalse(ca.isAmbiguous(s));
        assertEquals("accession", ig.getConcept(s, new Integer(6)).getConceptName(s).getName(s));
        CV cv2 = ig.getONDEXGraphData(s).getCV(s, "TP");
        assertNotNull(cv2);
        assertNotNull(ig.getConcept(s, new Integer(6)).getConceptAccession(s, "G000001", cv2));
        ConceptAccession ca2 = ig.getConcept(s, new Integer(6)).getConceptAccession(s, "G000001", cv2);
        assertEquals("G000001", ca.getAccession(s));
        assertEquals(cv2, ca2.getElementOf(s));
        assertEquals("TP", ca2.getElementOf(s).getName(s));
        assertEquals("TP", ca2.getElementOf(s).getFullname(s));
        assertEquals("BIOBASE Transpath Database", ca2.getElementOf(s).getDescription(s));
        assertFalse(ca2.isAmbiguous(s));
        assertEquals("accession", ig.getConcept(s, new Integer(6)).getConceptName(s).getName(s));
        CV cv3 = ig.getONDEXGraphData(s).getCV(s, "EMBL");
        assertNotNull(cv3);
        assertNotNull(ig.getConcept(s, new Integer(6)).getConceptAccession(s, "J01901", cv3));
        ConceptAccession ca3 = ig.getConcept(s, new Integer(6)).getConceptAccession(s, "J01901", cv3);
        assertEquals("J01901", ca3.getAccession(s));
        assertEquals(cv3, ca3.getElementOf(s));
        assertEquals("EMBL", ca3.getElementOf(s).getName(s));
        assertEquals("EMBL", ca3.getElementOf(s).getFullname(s));
        assertEquals("EMBL-EBI International Nucleotide Sequence Data Library", ca3.getElementOf(s).getDescription(s));
        assertTrue(ca3.isAmbiguous(s));
        AbstractONDEXIterator<ConceptAccession> col = ig.getConcept(s, new Integer(6)).getConceptAccessions(s);
        assertEquals(3, col.size());
        assertTrue(col.contains(ca));
        assertTrue(col.contains(ca2));
        assertTrue(col.contains(ca3));
    }

    public void testConceptGDSIntegerParsing() throws Exception {
        assertNotNull(ig.getONDEXGraphData(s).getAttributeName(s, "Taxonomy ID"));
        AttributeName attr = ig.getONDEXGraphData(s).getAttributeName(s, "Taxonomy ID");
        assertNotNull(ig.getONDEXGraphData(s).getAttributeName(s, "Taxonomy ID").getDataType(s));
        Class datatype = ig.getONDEXGraphData(s).getAttributeName(s, "Taxonomy ID").getDataType(s);
        assertEquals(Integer.class, datatype);
        assertNotNull(ig.getConcept(s, new Integer(1)).getConceptGDS(s, attr));
        Integer i = new Integer(45372);
        assertEquals(i, attr.getDataType(s).cast(ig.getConcept(s, new Integer(1)).getConceptGDS(s, attr).getValue(s)));
        assertNotNull(ig.getONDEXGraphData(s).getAttributeName(s, "Taxonomy ID"));
        AttributeName attr2 = ig.getONDEXGraphData(s).getAttributeName(s, "Taxonomy ID");
        assertNotNull(ig.getONDEXGraphData(s).getAttributeName(s, "Taxonomy ID").getDataType(s));
        Class datatype2 = ig.getONDEXGraphData(s).getAttributeName(s, "Taxonomy ID").getDataType(s);
        assertEquals(Integer.class, datatype2);
        assertNotNull(ig.getConcept(s, new Integer(5)).getConceptGDS(s, attr2));
        assertEquals(1, attr2.getDataType(s).cast(ig.getConcept(s, new Integer(5)).getConceptGDS(s, attr2).getValue(s)));
        assertNotNull(ig.getConcept(s, new Integer(5)).getConceptGDS(s, attr2).getAttrname(s).getUnit(s));
    }

    public void testConceptGDSStringParsing() throws Exception {
        assertNotNull(ig.getONDEXGraphData(s).getAttributeName(s, "Mery Christmas"));
        AttributeName attr = ig.getONDEXGraphData(s).getAttributeName(s, "Mery Christmas");
        assertNotNull(ig.getONDEXGraphData(s).getAttributeName(s, "Mery Christmas").getDataType(s));
        Class datatype = ig.getONDEXGraphData(s).getAttributeName(s, "Mery Christmas").getDataType(s);
        assertEquals(String.class, datatype);
        assertNotNull(ig.getConcept(s, new Integer(1)).getConceptGDS(s, attr));
        assertEquals("Santa Claus", attr.getDataType(s).cast(ig.getConcept(s, new Integer(1)).getConceptGDS(s, attr).getValue(s)));
        AbstractONDEXIterator<ConceptGDS> cncol1 = ig.getConcept(s, new Integer(1)).getConceptGDSs(s);
        assertEquals(2, cncol1.size());
        AbstractONDEXIterator<ConceptGDS> cncol2 = ig.getConcept(s, new Integer(2)).getConceptGDSs(s);
        assertEquals(1, cncol2.size());
        AbstractONDEXIterator<ConceptGDS> cncol3 = ig.getConcept(s, new Integer(3)).getConceptGDSs(s);
        assertEquals(1, cncol3.size());
        AbstractONDEXIterator<ConceptGDS> cncol4 = ig.getConcept(s, new Integer(5)).getConceptGDSs(s);
        assertEquals(1, cncol4.size());
    }

    public void testEvidenceParsing() throws Exception {
        assertNotNull(ig.getONDEXGraphData(s).getEvidenceType(s, "Taxonomy"));
        assertNotNull(ig.getONDEXGraphData(s).getEvidenceType(s, "root"));
        AbstractONDEXIterator<EvidenceType> evidences = ig.getONDEXGraphData(s).getEvidenceTypes(s);
        assertEquals(2, evidences.size());
        assertEquals(2, ig.getConcept(s, new Integer(5)).getEvidence(s).size());
        assertEquals(1, ig.getConcept(s, new Integer(1)).getEvidence(s).size());
        assertEquals("Taxonomy", ig.getConcept(s, new Integer(1)).getEvidence(s).next().getName(s));
        assertEquals("Taxonomy", ig.getConcept(s, new Integer(1)).getEvidence(s).next().getFullname(s));
    }
}
