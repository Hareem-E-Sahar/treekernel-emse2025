package com.hp.hpl.jena.xmloutput.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.Vector;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.vocabulary.DAML_OIL;
import com.hp.hpl.jena.vocabulary.RDFSyntax;

/**
 * This will test any Writer and Reader pair.
 * It writes out a random model, and reads it back in.
 * The test fails if the models are not 'the same'.
 * Quite what 'the same' means is debatable.
 * @author  jjc
 
 * @version  Release='$Name:  $' Revision='$Revision: 1.35 $' Date='$Date: 2006/03/22 13:53:14 $'
 */
public class testWriterAndReader extends ModelTestBase implements RDFErrorHandler {

    private static boolean showProgress = false;

    private static int firstTest = 4;

    private static int lastTest = 9;

    private static int repetitionsJ = 6;

    protected static Log logger = LogFactory.getLog(testWriterAndReader.class);

    String lang;

    String test;

    int fileNumber;

    int options = 0;

    testWriterAndReader(String name, String lang, int fName) {
        super(name);
        this.lang = lang;
        this.fileNumber = fName;
    }

    testWriterAndReader(String name, String lang, int fName, int options) {
        super(name);
        this.lang = lang;
        this.fileNumber = fName;
        this.options = options;
    }

    public String toString() {
        return getName() + " " + lang + " t" + fileNumber + "000.rdf" + (options != 0 ? ("[" + options + "]") : "");
    }

    static Test suite(String lang) {
        return suite(lang, false);
    }

    public static Test suite() {
        return suite("special");
    }

    private static boolean nBits(int i, int ok[]) {
        int cnt = 0;
        while (i > 0) {
            if ((i & 1) == 1) cnt++;
            i >>= 1;
        }
        for (int j = 0; j < ok.length; j++) if (cnt == ok[j]) return true;
        return false;
    }

    static Test suite(String lang, boolean lots) {
        TestSuite langsuite = new TestSuite();
        langsuite.setName(lang);
        if (lang.equals("special")) {
            langsuite.addTest(new TestXMLFeatures("testNoReification", "RDF/XML-ABBREV"));
            return langsuite;
        }
        langsuite.addTest(new testWriterInterface("testWriting", lang));
        langsuite.addTest(new testWriterInterface("testLineSeparator", lang));
        for (int k = firstTest; k <= lastTest; k++) {
            if (lang.indexOf("XML") > 0) {
                langsuite.addTest(new testWriterAndReader("testLongId", lang, k));
                for (int j = 1; j < (lang.equals("RDF/XML-ABBREV") ? (1 << blockRules.length) : 2); j++) {
                    if (lots || nBits(j, new int[] { 1, 6, 7 })) langsuite.addTest(new testWriterAndReader("testOptions " + k + " " + j, lang, k, j) {

                        public void runTest() throws IOException {
                            testOptions();
                        }
                    });
                }
            }
        }
        if (lang.indexOf("XML") > 0) {
            langsuite.addTest(new TestXMLFeatures("testBadURIAsProperty1", lang));
            langsuite.addTest(new TestXMLFeatures("testBadURIAsProperty2", lang));
            langsuite.addTest(new TestXMLFeatures("testBadProperty1", lang));
            langsuite.addTest(new TestXMLFeatures("testLiAsProperty1", lang));
            langsuite.addTest(new TestXMLFeatures("testDescriptionAsProperty", lang));
            langsuite.addTest(new TestXMLFeatures("testXMLBase", lang));
            langsuite.addTest(new TestXMLFeatures("testRelativeAPI", lang));
            langsuite.addTest(new TestXMLFeatures("testRelative", lang));
            langsuite.addTest(new TestXMLFeatures("testBug696057", lang));
            langsuite.addTest(new TestXMLFeatures("testPropertyURI", lang));
            langsuite.addTest(new TestXMLFeatures("testUseNamespace", lang));
            langsuite.addTest(new TestXMLFeatures("testUseDefaultNamespace", lang));
            langsuite.addTest(new TestXMLFeatures("testUseUnusedNamespace", lang));
            langsuite.addTest(new TestXMLFeatures("testBadPrefixNamespace", lang));
            langsuite.addTest(new TestXMLFeatures("testRDFNamespace", lang));
            langsuite.addTest(new TestXMLFeatures("testDuplicatePrefixSysPropAndExplicit", lang));
            langsuite.addTest(new TestXMLFeatures("testRDFDefaultNamespace", lang));
            langsuite.addTest(new TestXMLFeatures("testDuplicateNamespace", lang));
            langsuite.addTest(new TestXMLFeatures("testDuplicatePrefix", lang));
            langsuite.addTest(new TestXMLFeatures("testUseNamespaceSysProp", lang));
            langsuite.addTest(new TestXMLFeatures("testDefaultNamespaceSysProp", lang));
            langsuite.addTest(new TestXMLFeatures("testDuplicateNamespaceSysProp", lang));
            langsuite.addTest(new TestXMLFeatures("testDuplicatePrefixSysProp", lang));
            langsuite.addTest(new TestXMLFeatures("testUTF8DeclAbsent", lang));
            langsuite.addTest(new TestXMLFeatures("testUTF16DeclAbsent", lang));
            langsuite.addTest(new TestXMLFeatures("testUTF8DeclPresent", lang));
            langsuite.addTest(new TestXMLFeatures("testUTF16DeclPresent", lang));
            langsuite.addTest(new TestXMLFeatures("testISO8859_1_DeclAbsent", lang));
            langsuite.addTest(new TestXMLFeatures("testISO8859_1_DeclPresent", lang));
            langsuite.addTest(new TestXMLFeatures("testStringDeclAbsent", lang));
            langsuite.addTest(new TestXMLFeatures("testStringDeclPresent", lang));
            langsuite.addTest(new TestXMLFeatures("testTab", lang));
            langsuite.addTest(new TestXMLFeatures("testNoLiteral", lang));
            langsuite.addTest(new TestXMLFeatures("testNoTab", lang));
            langsuite.addTest(new TestXMLFeatures("testDoubleQuote", lang));
            langsuite.addTest(new TestXMLFeatures("testSingleQuote", lang));
            langsuite.addTest(new TestXMLFeatures("testNullBaseWithAbbrev", lang));
        }
        if (lang.equals("RDF/XML-ABBREV")) {
            langsuite.addTest(new TestXMLFeatures("testNoPropAttr", "RDF/XML-ABBREV"));
            langsuite.addTest(new TestXMLFeatures("testNoDamlCollection", "RDF/XML-ABBREV"));
            langsuite.addTest(new TestXMLFeatures("testNoRdfCollection", "RDF/XML-ABBREV"));
            langsuite.addTest(new TestXMLFeatures("testNoLi", "RDF/XML-ABBREV"));
            langsuite.addTest(new TestXMLFeatures("testNoID", lang));
            langsuite.addTest(new TestXMLFeatures("testNoID2", lang));
            langsuite.addTest(new TestXMLFeatures("testNoResource", lang));
            langsuite.addTest(new TestXMLFeatures("testNoReification", lang));
            langsuite.addTest(new TestXMLFeatures("testNoPropAttrs", lang));
            langsuite.addTest(new TestXMLFeatures("testNoCookUp", lang));
            langsuite.addTest(new TestXMLFeatures("testPropAttrs", lang));
        }
        return langsuite;
    }

    public void testRandom() throws IOException {
        doTest(new String[] {}, new Object[] {});
    }

    public void testLongId() throws IOException {
        doTest(new String[] { "longId" }, new Object[] { new Boolean(true) });
    }

    static Resource blockRules[] = { RDFSyntax.parseTypeLiteralPropertyElt, RDFSyntax.parseTypeCollectionPropertyElt, RDFSyntax.propertyAttr, RDFSyntax.sectionReification, RDFSyntax.sectionListExpand, RDFSyntax.parseTypeResourcePropertyElt, DAML_OIL.collection };

    public void testOptions() throws IOException {
        Vector v = new Vector();
        for (int i = 0; i < blockRules.length; i++) {
            if ((options & (1 << i)) != 0) v.add(blockRules[i]);
        }
        Resource blocked[] = new Resource[v.size()];
        v.copyInto(blocked);
        doTest(new String[] { "blockRules" }, new Object[] { blocked });
    }

    public void doTest(String[] propNames, Object[] propVals) throws IOException {
        test(lang, 35, 1, propNames, propVals);
    }

    static final String baseUris[] = { "http://foo.com/Hello", "http://foo.com/Hello", "http://daml.umbc.edu/ontologies/calendar-ont", "http://www.daml.org/2001/03/daml+oil-ex" };

    ByteArrayOutputStream tmpOut;

    /**
	 * 
	 * @param rwLang Use Writer for this lang
	 * @param seed  A seed for the random number generator
	 * @param jjjMax Number of random variations
	 * @param wopName  Property names to set on Writer
	 * @param wopVal   Property values to set on Writer
	 */
    public void test(String rwLang, int seed, int jjjMax, String[] wopName, Object[] wopVal) throws IOException {
        Model m1 = createMemModel();
        Model m2 = createMemModel();
        test = "testWriterAndReader lang=" + rwLang + " seed=" + seed;
        String filebase = "testing/regression/testWriterAndReader/";
        if (showProgress) System.out.println("Beginning " + test);
        Random random = new Random(seed);
        RDFReader rdfRdr = m1.getReader(rwLang);
        RDFWriter rdfWtr = m1.getWriter(rwLang);
        if (wopName != null) {
            for (int i = 0; i < wopName.length; i++) {
                rdfWtr.setProperty(wopName[i], wopVal[i]);
            }
        }
        rdfRdr.setErrorHandler(this);
        rdfWtr.setErrorHandler(this);
        for (int jjj = 0; jjj < jjjMax; jjj++) {
            String fileName = "t" + (fileNumber * 1000) + ".rdf";
            m1 = createMemModel();
            String baseUriRead;
            if (fileNumber < baseUris.length) baseUriRead = baseUris[fileNumber]; else baseUriRead = "http://foo.com/Hello";
            InputStream rdr = new FileInputStream(filebase + fileName);
            m1.read(rdr, baseUriRead);
            rdr.close();
            for (int j = 0; j < repetitionsJ; j++) {
                String baseUriWrite = j % 2 == 0 ? baseUriRead : "http://bar.com/irrelevant";
                int cn = (int) m1.size();
                if ((j % 2) == 0 && j > 0) prune(m1, random, 1 + cn / 10);
                if ((j % 2) == 0 && j > 0) expand(m1, random, 1 + cn / 10);
                tmpOut = new ByteArrayOutputStream();
                rdfWtr.write(m1, tmpOut, baseUriWrite);
                tmpOut.flush();
                tmpOut.close();
                m2 = createMemModel();
                InputStream in = new ByteArrayInputStream(tmpOut.toByteArray());
                rdfRdr.read(m2, in, baseUriWrite);
                in.close();
                Model s1 = m1;
                Model s2 = m2;
                assertTrue("Comparison of file written out, and file read in.", s1.isIsomorphicWith(s2));
                tmpOut.reset();
                tmpOut = null;
            }
            if (showProgress) {
                System.out.print("+");
                System.out.flush();
            }
        }
        if (showProgress) System.out.println("End of " + test);
    }

    static boolean linuxFileDeleteErrorFlag = false;

    /**Deletes cnt edges from m chosen by random.
	 * @param cnt The number of statements to delete.
	 * @param m A model with more than cnt statements.
	 */
    private void prune(Model m, Random random, int cnt) {
        Statement die[] = new Statement[cnt];
        int sz = (int) m.size();
        StmtIterator ss = m.listStatements();
        try {
            for (int i = 0; i < cnt; i++) die[i] = ss.nextStatement();
            while (ss.hasNext()) {
                int ix = random.nextInt(sz);
                if (ix < cnt) die[ix] = ss.nextStatement();
            }
        } finally {
            ss.close();
        }
        for (int i = 0; i < cnt; i++) m.remove(die[i]);
    }

    /**
	 *  Adds cnt edges to m chosen by random.
	 *
	 * @param cnt The number of statements to add.
	 * @param m A model with more than cnt statements.
	 */
    private void expand(Model m, Random random, int cnt) {
        Resource subject[] = new Resource[cnt];
        Property predicate[] = new Property[cnt];
        RDFNode object[] = new RDFNode[cnt];
        int sz = (int) m.size();
        StmtIterator ss = m.listStatements();
        try {
            for (int i = 0; i < cnt; i++) {
                Statement s = ss.nextStatement();
                subject[i] = s.getSubject();
                predicate[i] = s.getPredicate();
                object[i] = s.getObject();
            }
            while (ss.hasNext()) {
                Statement s = ss.nextStatement();
                Resource subj = s.getSubject();
                RDFNode obj = s.getObject();
                int ix = random.nextInt(sz);
                if (ix < cnt) subject[ix] = subj;
                ix = random.nextInt(sz);
                if (ix < cnt) object[ix] = subj;
                ix = random.nextInt(sz);
                if (ix < cnt) predicate[ix] = s.getPredicate();
                ix = random.nextInt(sz);
                if (ix < cnt) object[ix] = obj;
                if (obj instanceof Resource) {
                    ix = random.nextInt(sz);
                    if (ix < cnt) subject[ix] = (Resource) obj;
                }
            }
        } finally {
            ss.close();
        }
        for (int i = 0; i < cnt; i++) m.add(subject[i], predicate[i], object[i]);
    }

    /** report a warning
	 * @param e an exception representing the error
	 */
    public void warning(Exception e) {
        System.out.println(new String(tmpOut.toString()));
        throw new JenaException(e);
    }

    public void error(Exception e) {
        fail(e.getMessage());
    }

    public void fatalError(Exception e) {
        error(e);
        throw new JenaException(e);
    }
}
