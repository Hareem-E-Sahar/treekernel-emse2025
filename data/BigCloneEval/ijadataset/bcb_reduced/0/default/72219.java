import org.biomage.tools.helpers.*;
import org.biomage.Common.Extendable;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import java.io.FileReader;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xml.serialize.OutputFormat;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author  kjellp
 */
public class testMOE {

    java.util.HashSet entryClassSet = new java.util.HashSet();

    OntologyHelper oh = null;

    /** Creates a new instance of testMGEDOE */
    public testMOE(String MOdamlfile) throws java.lang.Exception {
        oh = new OntologyHelper(MOdamlfile);
        entryClassSet = new java.util.HashSet();
        entryClassSet.add(new EntryClassPair("Action", "org.biomage.BioMaterial.Treatment"));
        entryClassSet.add(new EntryClassPair("Annotations", "org.biomage.Description.Description"));
        entryClassSet.add(new EntryClassPair("Annotations", "org.biomage.Experiment.ExperimentalFactor"));
        entryClassSet.add(new EntryClassPair("Category", "org.biomage.Experiment.ExperimentalFactor"));
        entryClassSet.add(new EntryClassPair("Characteristics", "org.biomage.BioMaterial.BioMaterial"));
        entryClassSet.add(new EntryClassPair("Characteristics", "org.biomage.BioMaterial.BioSample"));
        entryClassSet.add(new EntryClassPair("CompoundIndices", "org.biomage.BioMaterial.Compound"));
        entryClassSet.add(new EntryClassPair("ControlType", "org.biomage.DesignElement.DesignElement"));
        entryClassSet.add(new EntryClassPair("DataType", "org.biomage.QuantitationType.QuantitationType"));
        entryClassSet.add(new EntryClassPair("DataType", "org.biomage.QuantitationType.Ratio"));
        entryClassSet.add(new EntryClassPair("DataType", "org.biomage.HigherLevelAnalysis.NodeValue"));
        entryClassSet.add(new EntryClassPair("DataType", "org.biomage.Protocol.Parameter"));
        entryClassSet.add(new EntryClassPair("DefectType", "org.biomage.Array.FeatureDefect"));
        entryClassSet.add(new EntryClassPair("DefectType", "org.biomage.Array.ZoneDefect"));
        entryClassSet.add(new EntryClassPair("FailTypes", "org.biomage.DesignElement.Reporter"));
        entryClassSet.add(new EntryClassPair("FeatureShape", "org.biomage.ArrayDesign.FeatureGroup"));
        entryClassSet.add(new EntryClassPair("FiducialType", "org.biomage.Array.Fiducial"));
        entryClassSet.add(new EntryClassPair("Format", "org.biomage.BioAssay.Image"));
        entryClassSet.add(new EntryClassPair("MaterialType", "org.biomage.BioMaterial.BioMaterial"));
        entryClassSet.add(new EntryClassPair("MaterialType", "org.biomage.BioMaterial.BioSample"));
        entryClassSet.add(new EntryClassPair("Parameters", "org.biomage.BQS.BibliographicReference"));
        entryClassSet.add(new EntryClassPair("PolymerType", "org.biomage.BioSequence.BioSequence"));
        entryClassSet.add(new EntryClassPair("Roles", "org.biomage.AuditAndSecurity.Contact"));
        entryClassSet.add(new EntryClassPair("Roles", "org.biomage.AuditAndSecurity.Person"));
        entryClassSet.add(new EntryClassPair("Scale", "org.biomage.QuantitationType.QuantitationType"));
        entryClassSet.add(new EntryClassPair("Scale", "org.biomage.QuantitationType.MeasuredSignal"));
        entryClassSet.add(new EntryClassPair("Scale", "org.biomage.HigherLevelAnalysis.NodeValue"));
        entryClassSet.add(new EntryClassPair("Species", "org.biomage.ArrayDesign.DesignElementGroup"));
        entryClassSet.add(new EntryClassPair("Species", "org.biomage.ArrayDesign.FeatureGroup"));
        entryClassSet.add(new EntryClassPair("Species", "org.biomage.BioSequence.BioSequence"));
        entryClassSet.add(new EntryClassPair("SubstrateType", "org.biomage.Array.ArrayGroup"));
        entryClassSet.add(new EntryClassPair("SurfaceType", "org.biomage.ArrayDesign.PhysicalArrayDesign"));
        entryClassSet.add(new EntryClassPair("TechnologyType", "org.biomage.ArrayDesign.FeatureGroup"));
        entryClassSet.add(new EntryClassPair("Type", "org.biomage.Description.DatabaseEntry"));
        entryClassSet.add(new EntryClassPair("Type", "org.biomage.HigherLevelAnalysis.NodeValue"));
        entryClassSet.add(new EntryClassPair("Type", "org.biomage.Protocol.Hardware"));
        entryClassSet.add(new EntryClassPair("Type", "org.biomage.Protocol.Protocol"));
        entryClassSet.add(new EntryClassPair("Type", "org.biomage.Protocol.Software"));
        entryClassSet.add(new EntryClassPair("Type", "org.biomage.BioSequence.BioSequence"));
        entryClassSet.add(new EntryClassPair("Type", "org.biomage.BioMaterial.BioSample"));
        entryClassSet.add(new EntryClassPair("Type", "org.biomage.BioAssay.DerivedBioAssay"));
        entryClassSet.add(new EntryClassPair("Value", "org.biomage.Experiment.FactorValue"));
        entryClassSet.add(new EntryClassPair("WarningType", "org.biomage.DesignElement.Reporter"));
    }

    private class EntryClassPair {

        String entryName;

        String className;

        public EntryClassPair(String e, String c) {
            entryName = e;
            className = c;
        }
    }

    public static void main(String[] argv) {
        try {
            StringOutputHelpers.setVerbose(1);
            testMOE testObj = new testMOE(argv[0]);
            java.util.Vector mgedOntClassEntries = new java.util.Vector();
            mgedOntClassEntries.addAll(testObj.testSimpleExample());
            mgedOntClassEntries.addAll(testObj.testSlightlyMoreComplicatedExample());
            mgedOntClassEntries.addAll(testObj.testMuchMoreComplicatedExample());
            mgedOntClassEntries.addAll(testObj.testFullySpecifiedButLeavingOneBranchOutExample());
            testObj.writeRawMAGEML(mgedOntClassEntries);
            testObj.writePrettyMAGEML();
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }

    /** The first simple example :
     *
     * parentObject => bioSequence,
     * association => 'PolymerType',
     * values => {
     *             PolymerType => 'DNA',
     *           }
     */
    public java.util.Vector testSimpleExample() {
        java.util.Vector mgedOntClassEntries = new java.util.Vector(1);
        java.lang.String className = new String("org.biomage.BioSequence.BioSequence");
        java.lang.String entryName = new String("PolymerType");
        Extendable MAGEobj = createMAGEobject(className);
        String mgedOntologyClassName = resolve2MOclassName(MAGEobj, className, entryName);
        java.util.HashMap choiceMap = new java.util.HashMap();
        choiceMap.put("PolymerType", "DNA");
        MGEDOntologyClassEntry entry = new MGEDOntologyClassEntry(mgedOntologyClassName, oh, choiceMap);
        mgedOntClassEntries.add(entry);
        checkStatus(choiceMap, entry);
        return mgedOntClassEntries;
    }

    /** Slightly more complicated examples :    
     *
     * parentObject => bioSource,
     * association => 'Characteristics',
     * values => {
     *              Organism => 'Organism',
     *              has_value => 'Mus musculus',
     *              has_accession => '10900',
     *              has_database => 'has_database',     # optional
     *              OrganismDatabase => 'NCBI-taxonomy',
     *           }
     *
     * parentObject => bioSource,
     * association => 'MaterialType',
     * values => {
     *              MaterialType => 'whole_organism',
     *           }
     **/
    public java.util.Vector testSlightlyMoreComplicatedExample() {
        java.util.Vector mgedOntClassEntries = new java.util.Vector(2);
        java.lang.String className = new String("org.biomage.BioMaterial.BioSource");
        java.lang.String entryName = new String("Characteristics");
        Extendable MAGEobj = createMAGEobject(className);
        String mgedOntologyClassName = resolve2MOclassName(MAGEobj, className, entryName);
        java.util.HashMap choiceMap = new java.util.HashMap();
        choiceMap.put("Organism", "Organism");
        choiceMap.put("has_value", "Mus musculus");
        choiceMap.put("has_accession", "10900");
        choiceMap.put("OrganismDatabase", "NCBI_taxonomy");
        MGEDOntologyClassEntry entry = new MGEDOntologyClassEntry(mgedOntologyClassName, oh, choiceMap);
        mgedOntClassEntries.add(entry);
        checkStatus(choiceMap, entry);
        entryName = new String("MaterialType");
        MAGEobj = createMAGEobject(className);
        mgedOntologyClassName = resolve2MOclassName(MAGEobj, className, entryName);
        choiceMap.clear();
        choiceMap.put("MaterialType", "whole_organism");
        entry = new MGEDOntologyClassEntry(mgedOntologyClassName, oh, choiceMap);
        mgedOntClassEntries.add(entry);
        checkStatus(choiceMap, entry);
        return mgedOntClassEntries;
    }

    /** Much more complicated example
     *
     * parentObject => bioSample,
     * association => 'Characteristics',
     * values => {
     *               Age => 'Age',
     *               IntitialTimePoint => 'birth',
     *               has_measurement => {
     *                                    has_measurement_type => 'relative',
     *                                    TimeUnit => 'years',
     *                                    has_value => 40,
     *                                  },
     *               has_maximum_meaurement => {
     *                                           has_measurement_type => 'relative',
     *                                           TimeUnit => 'years',
     *                                           has_value => 100,
     *                                         },
     *           }
     **/
    public java.util.Vector testMuchMoreComplicatedExample() {
        java.util.Vector mgedOntClassEntries = new java.util.Vector(1);
        java.lang.String className = new String("org.biomage.BioMaterial.BioSample");
        java.lang.String entryName = new String("Characteristics");
        Extendable MAGEobj = createMAGEobject(className);
        String mgedOntologyClassName = resolve2MOclassName(MAGEobj, className, entryName);
        java.util.HashMap choiceMap = new java.util.HashMap();
        java.util.HashMap nestedMap1 = new java.util.HashMap();
        java.util.HashMap nestedMap2 = new java.util.HashMap();
        choiceMap.put("Age", "Age");
        choiceMap.put("InitialTimePoint", "birth");
        nestedMap1.put("has_measurement_type", "relative");
        nestedMap1.put("TimeUnit", "years");
        nestedMap1.put("has_value", "40");
        choiceMap.put("has_measurement", nestedMap1);
        nestedMap2.put("has_measurement_type", "relative");
        nestedMap2.put("TimeUnit", "years");
        nestedMap2.put("has_value", "100");
        choiceMap.put("has_maximum_measurement", nestedMap2);
        MGEDOntologyClassEntry entry = new MGEDOntologyClassEntry(mgedOntologyClassName, oh, choiceMap);
        mgedOntClassEntries.add(entry);
        checkStatus(choiceMap, entry);
        return mgedOntClassEntries;
    }

    /** Ambiguous example, will throw an error
     * 
     * parentObject => biosample,
     * association => 'Characteristics',
     * values => {
     *             Age => 'Age',
     *             IntitialTimePoint => 'birth',
     *             has_measurement_type => 'relative',
     *             TimeUnit => 'years',
     *             has_value => 40,
     *           }
     **/
    public java.util.Vector testAmbiguousExample() {
        java.util.Vector mgedOntClassEntries = new java.util.Vector(1);
        java.lang.String className = new String("org.biomage.BioMaterial.BioSample");
        java.lang.String entryName = new String("Characteristics");
        Extendable MAGEobj = createMAGEobject(className);
        String mgedOntologyClassName = resolve2MOclassName(MAGEobj, className, entryName);
        java.util.HashMap choiceMap = new java.util.HashMap();
        java.util.HashMap nestedMap1 = new java.util.HashMap();
        java.util.HashMap nestedMap2 = new java.util.HashMap();
        choiceMap.put("Age", "Age");
        choiceMap.put("InitialTimePoint", "birth");
        choiceMap.put("has_measurement_type", "relative");
        choiceMap.put("TimeUnit", "years");
        choiceMap.put("has_value", "40");
        MGEDOntologyClassEntry entry = new MGEDOntologyClassEntry(mgedOntologyClassName, oh, choiceMap);
        mgedOntClassEntries.add(entry);
        checkStatus(choiceMap, entry);
        return mgedOntClassEntries;
    }

    /** Fully specified, but leaving has_maximum_measurement out 
     *
     * parentObject => biosample,
     * association => 'Characteristics',
     * values => {
     *             Age => 'Age',
     *             IntitialTimePoint => 'birth',
     *             has_measurement => {
     *                                  MeasurementType => 'relative',
     *                                  TimeUnit => 'years',
     *                                  has_value => 40,
     *                                },
     *           }
     */
    public java.util.Vector testFullySpecifiedButLeavingOneBranchOutExample() {
        java.util.Vector mgedOntClassEntries = new java.util.Vector(1);
        java.lang.String className = new String("org.biomage.BioMaterial.BioSample");
        java.lang.String entryName = new String("Characteristics");
        Extendable MAGEobj = createMAGEobject(className);
        String mgedOntologyClassName = resolve2MOclassName(MAGEobj, className, entryName);
        java.util.HashMap choiceMap = new java.util.HashMap();
        java.util.HashMap nestedMap1 = new java.util.HashMap();
        choiceMap.put("Age", "Age");
        choiceMap.put("InitialTimePoint", "birth");
        nestedMap1.put("has_measurement_type", "relative");
        nestedMap1.put("TimeUnit", "years");
        nestedMap1.put("has_value", "40");
        choiceMap.put("has_measurement", nestedMap1);
        MGEDOntologyClassEntry entry = new MGEDOntologyClassEntry(mgedOntologyClassName, oh, choiceMap);
        mgedOntClassEntries.add(entry);
        checkStatus(choiceMap, entry);
        return mgedOntClassEntries;
    }

    /** Old test methods from before the TIGR jam **/
    public void testAssigningValues(java.util.Vector mgedOntClassEntries) {
        java.util.Iterator it = mgedOntClassEntries.iterator();
        while (it.hasNext()) {
            MGEDOntologyClassEntry entry = (MGEDOntologyClassEntry) it.next();
            assignFirstValue(entry);
        }
    }

    public java.util.Vector testInitOfMGEDOE(java.util.Vector mgedOntClassNames) {
        java.util.Vector mgedOntClassEntries = new java.util.Vector();
        try {
            java.util.Iterator it = mgedOntClassNames.iterator();
            while (it.hasNext()) {
                String className = (String) it.next();
                MGEDOntologyClassEntry entry = new MGEDOntologyClassEntry(className, oh);
                mgedOntClassEntries.add(entry);
            }
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
        return mgedOntClassEntries;
    }

    /** Test looking up all associations from MAGE to MO **/
    public java.util.Vector testLookUpFromClassNames() {
        java.util.Vector mgedOntClassNames = new java.util.Vector();
        try {
            java.util.Iterator it = entryClassSet.iterator();
            int counter = 0;
            while (it.hasNext()) {
                EntryClassPair pair = (EntryClassPair) it.next();
                counter++;
                java.lang.Class MAGEclass = java.lang.Class.forName(pair.className);
                java.lang.reflect.Constructor constructor = MAGEclass.getConstructor(null);
                Extendable MAGEobj = null;
                try {
                    MAGEobj = (Extendable) constructor.newInstance(null);
                    String mgedOntologyClassName = oh.resolveOntologyClassNameFromModel(MAGEobj, pair.entryName);
                    StringOutputHelpers.writeOutput("Resolve (" + pair.className + "," + pair.entryName + ") ===> ", 3);
                    if (mgedOntologyClassName != null) {
                        StringOutputHelpers.writeOutput("OK : " + mgedOntologyClassName + "\n", 3);
                        mgedOntClassNames.add(mgedOntologyClassName);
                    } else {
                        StringOutputHelpers.writeOutput("ERROR : Couldn't resolve" + "\n", 3);
                    }
                } catch (java.lang.InstantiationException e) {
                    StringOutputHelpers.writeOutput("Couldn't instantiate class " + MAGEclass.getName() + "\n", 3);
                }
            }
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
        return mgedOntClassNames;
    }

    /** Try to resolve MO classname from MAGE object and assoc name (entryName) */
    protected String resolve2MOclassName(org.biomage.Common.Extendable MAGEobj, String className, String entryName) {
        String mgedOntologyClassName = oh.resolveOntologyClassNameFromModel(MAGEobj, entryName);
        StringOutputHelpers.writeOutput("Resolve (" + className + "," + entryName + ") ===> " + "\n", 3);
        if (mgedOntologyClassName != null) {
            StringOutputHelpers.writeOutput("OK : " + mgedOntologyClassName + "\n", 3);
        } else {
            StringOutputHelpers.writeOutput("ERROR : Couldn't resolve" + "\n", 3);
        }
        return mgedOntologyClassName;
    }

    protected void checkStatus(java.util.HashMap choiceMap, MGEDOntologyClassEntry entry) {
        if (choiceMap.size() == 0) {
            StringOutputHelpers.writeOutput("All arguments filled in" + "\n", 3);
        } else {
            StringOutputHelpers.writeOutput("Somethings wrong :( Unused arguments :" + "\n", 3);
            java.util.Iterator it = choiceMap.keySet().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                try {
                    String val = (String) choiceMap.get(key);
                    StringOutputHelpers.writeOutput(key + " => " + val + "\n", 3);
                } catch (Exception e) {
                    StringOutputHelpers.writeOutput(key + " => " + "\n", 3);
                    java.util.HashMap nestedMap = (java.util.HashMap) choiceMap.get(key);
                    java.util.Iterator it2 = nestedMap.keySet().iterator();
                    while (it2.hasNext()) {
                        key = (String) it2.next();
                        String val = (String) nestedMap.get(key);
                        StringOutputHelpers.writeOutput("\t" + key + " => " + val + "\n", 3);
                    }
                }
            }
        }
        if (findUnAssignedMOE(entry) == null) {
            StringOutputHelpers.writeOutput("Success :)" + "\n", 3);
        } else {
            StringOutputHelpers.writeOutput("Partly success :| Still some unassigned, we'll try pruning..." + "\n", 3);
            entry.prune();
            if (findUnAssignedMOE(entry) == null) {
                StringOutputHelpers.writeOutput("None unassigned left => Success :)" + "\n", 3);
            } else {
                StringOutputHelpers.writeOutput("Some unassigned still left :(" + "\n", 3);
                java.util.Iterator it = choiceMap.keySet().iterator();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    try {
                        String val = (String) choiceMap.get(key);
                        StringOutputHelpers.writeOutput(key + " => " + val + "\n", 3);
                    } catch (Exception e) {
                        StringOutputHelpers.writeOutput(key + " => " + "\n", 3);
                        java.util.HashMap nestedMap = (java.util.HashMap) choiceMap.get(key);
                        java.util.Iterator it2 = nestedMap.keySet().iterator();
                        while (it2.hasNext()) {
                            key = (String) it2.next();
                            String val = (String) nestedMap.get(key);
                            StringOutputHelpers.writeOutput("\t" + key + " => " + val + "\n", 3);
                        }
                    }
                }
            }
        }
    }

    protected Extendable createMAGEobject(String className) {
        Extendable MAGEobj = null;
        try {
            java.lang.Class MAGEclass = java.lang.Class.forName(className);
            java.lang.reflect.Constructor constructor = MAGEclass.getConstructor(null);
            MAGEobj = (Extendable) constructor.newInstance(null);
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
        return MAGEobj;
    }

    protected MGEDOntologyEntry findUnAssignedMOE(MGEDOntologyEntry entry) {
        java.util.ArrayList MOElist = new java.util.ArrayList();
        MOElist.add(entry);
        boolean found = false;
        while (MOElist.size() != 0 && !found) {
            MGEDOntologyEntry moe = (MGEDOntologyEntry) MOElist.remove(0);
            if (moe.isAssignable()) {
                if (moe.isAssigned()) {
                } else {
                    java.util.ArrayList possibleValues = moe.getAssignableValues();
                    StringOutputHelpers.writeOutput(moe + "\n", 3);
                    StringOutputHelpers.writeOutput("\t" + "is not Assigned, possible values are:" + "\n", 3);
                    if (possibleValues != null) {
                        java.util.Iterator it = possibleValues.iterator();
                        while (it.hasNext()) {
                            StringOutputHelpers.writeOutput("\t" + it.next() + "\n", 3);
                        }
                    } else {
                        StringOutputHelpers.writeOutput("\t" + "value can be anything" + "\n", 3);
                    }
                    entry = moe;
                    found = true;
                    StringOutputHelpers.writeOutput("==========================================" + "\n", 3);
                }
            } else {
            }
            MOElist.addAll(moe.getAssociations());
        }
        if (!found) {
            entry = null;
        }
        return entry;
    }

    protected void assignFirstValue(MGEDOntologyEntry entry) {
        if (entry.isAssignable()) {
            java.util.ArrayList values = entry.getAssignableValues();
            if (values != null) {
                entry.assignValue(values.get(0));
            } else {
                entry.setValue("value 1");
            }
        }
        java.util.Iterator it = entry.getAssociations().iterator();
        while (it.hasNext()) {
            entry = (MGEDOntologyEntry) it.next();
            assignFirstValue(entry);
        }
    }

    /*******************
     * MAGE-ML methods *
     *******************/
    public void writeRawMAGEML(java.util.Vector mgedOntClassEntries) {
        try {
            java.util.Iterator it = mgedOntClassEntries.iterator();
            java.io.FileWriter tmpFile = new java.io.FileWriter("./raw.xml");
            tmpFile.write("<dummy>");
            while (it.hasNext()) {
                ((MGEDOntologyEntry) it.next()).writeMAGEML(tmpFile);
            }
            tmpFile.write("</dummy>");
            tmpFile.flush();
            tmpFile.close();
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }

    public void writePrettyMAGEML() {
        try {
            OutputFormat xmlFormat = new OutputFormat("xml", "utf-8", true);
            xmlFormat.setLineWidth(60);
            xmlFormat.setLineSeparator("\n");
            xmlFormat.setIndent(2);
            XMLSerializer xmlWriterDocHndler = new XMLSerializer(System.out, xmlFormat);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser xmlWriterParser = factory.newSAXParser();
            xmlWriterParser.getXMLReader().setFeature("http://xml.org/sax/features/namespaces", true);
            xmlWriterParser.getXMLReader().setFeature("http://xml.org/sax/features/namespace-prefixes", false);
            xmlWriterParser.getXMLReader().setContentHandler(xmlWriterDocHndler);
            xmlWriterParser.getXMLReader().parse(new InputSource(new FileReader("./raw.xml")));
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }
}
