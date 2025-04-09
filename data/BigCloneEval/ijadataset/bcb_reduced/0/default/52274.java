import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
import org.apache.lucene.search.*;

public class NcitIndexGenerator {

    private static final int ONTOLOGY_VERSION = 39478;

    private static final String PARENT_URL = "http://rest.bioontology.org/bioportal/concepts/parents/";

    private static final String COMMON_SUFFIX = "?email=example@example.org";

    private static final String CONCEPTS_FILE = "/home/yining/workspace_caties/Playground/src/MRCON";

    private static final String LUCENE_INDEX_LOCATION = "/home/yining/workspace_caties/Playground/src/INDEX/";

    private IndexWriter writer;

    private FSDirectory fsDirectory;

    private DocumentBuilderFactory factory;

    private XPathFactory xfactory;

    private Map<String, String> conceptMap = new HashMap<String, String>();

    private Map<String, Set<String>> ancestorMap = new HashMap<String, Set<String>>();

    protected NcitIndexGenerator() {
        factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        xfactory = XPathFactory.newInstance();
    }

    private void buildConceptMap() {
        try {
            BufferedReader input = new BufferedReader(new FileReader(CONCEPTS_FILE));
            String line = null;
            while ((line = input.readLine()) != null) {
                String[] tokens = line.split("\\|");
                String key = tokens[6];
                String value = tokens[0];
                if (conceptMap.containsValue(value)) {
                    continue;
                }
                conceptMap.put(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildAncestorMap() throws ParserConfigurationException, XPathExpressionException {
        int progressCount = 0;
        DocumentBuilder builder = factory.newDocumentBuilder();
        XPath xpath = xfactory.newXPath();
        XPathExpression expr = xpath.compile("//classBean/id");
        XPathExpression levelExpr = xpath.compile("//classBean/relations/entry/int");
        Set<String> conceptNames = conceptMap.keySet();
        for (String conceptName : conceptNames) {
            progressCount += 1;
            if (progressCount % 1000 == 0) {
                System.out.println(progressCount);
            }
            String queryUrl = PARENT_URL + ONTOLOGY_VERSION + "/" + conceptName.replace(" ", "_") + COMMON_SUFFIX;
            try {
                org.w3c.dom.Document doc = builder.parse(queryUrl);
                Object result = expr.evaluate(doc, XPathConstants.NODESET);
                NodeList nodes = (NodeList) result;
                Object levelResult = levelExpr.evaluate(doc, XPathConstants.NODESET);
                NodeList levelNodes = (NodeList) levelResult;
                String conceptCui = conceptMap.get(conceptName);
                Set<String> ancestorCuisAndLevels = ancestorMap.get(conceptCui);
                if (ancestorCuisAndLevels == null) ancestorCuisAndLevels = new HashSet<String>();
                for (int i = 0, len = nodes.getLength(); i < len; i++) {
                    String name = nodes.item(i).getFirstChild().getNodeValue().replace("_", " ");
                    String ancestorCui = conceptMap.get(name);
                    if (ancestorCui == null) continue;
                    String level = levelNodes.item(i).getFirstChild().getNodeValue();
                    ancestorCuisAndLevels.add(ancestorCui + ":" + level);
                }
                ancestorMap.put(conceptCui, ancestorCuisAndLevels);
            } catch (IOException io) {
                continue;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void openLuceneIndex() {
        try {
            File f;
            boolean createDirectory = true;
            if ((f = new File(LUCENE_INDEX_LOCATION)).exists() && f.isDirectory()) {
                createDirectory = false;
            } else {
                createDirectory = true;
            }
            fsDirectory = FSDirectory.getDirectory(LUCENE_INDEX_LOCATION, createDirectory);
            writer = new IndexWriter(fsDirectory, new StandardAnalyzer(), createDirectory, IndexWriter.MaxFieldLength.UNLIMITED);
            writer.setMergeFactor(20);
        } catch (Exception x) {
            x.printStackTrace();
            fsDirectory = null;
        }
    }

    private void closeLuceneIndex() {
        try {
            if (writer == null) {
                System.err.println("Trying to close a null writer.  This shouldn't happen.");
            } else {
                writer.close();
            }
            if (fsDirectory == null) {
                System.err.println("Trying to close a null File System Lucene Index.  This shouldn't happen");
            } else {
                fsDirectory.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createIndex() {
        for (Map.Entry<String, Set<String>> set : ancestorMap.entrySet()) {
            Document doc = new Document();
            doc.add(new Field("cui", set.getKey(), Field.Store.YES, Field.Index.NOT_ANALYZED));
            String s = set.getValue().toString();
            s = s.substring(1, s.length() - 1).trim();
            doc.add(new Field("parents", s, Field.Store.YES, Field.Index.NO));
            try {
                writer.addDocument(doc);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void testIndex() {
        try {
            Directory dir = FSDirectory.getDirectory(new File(LUCENE_INDEX_LOCATION), null);
            IndexSearcher is = new IndexSearcher(dir);
            Query q = new TermQuery(new Term("cui", "C0334277"));
            TopDocs hits = is.search(q, 10);
            for (int i = 0; i < hits.scoreDocs.length; i++) {
                ScoreDoc scoreDoc = hits.scoreDocs[i];
                Document doc = is.doc(scoreDoc.doc);
                System.out.println(doc.get("parents"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void print() {
        for (Map.Entry<String, Set<String>> set : ancestorMap.entrySet()) {
            System.out.println(set.getKey());
            System.out.println(set.getValue());
        }
    }

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        NcitIndexGenerator ncit = new NcitIndexGenerator();
        ncit.buildConceptMap();
        ncit.buildAncestorMap();
        System.out.println("Building Ancestor Map completed.");
        ncit.openLuceneIndex();
        ncit.createIndex();
        System.out.println("Creating Indexes completed.");
        ncit.closeLuceneIndex();
    }
}
