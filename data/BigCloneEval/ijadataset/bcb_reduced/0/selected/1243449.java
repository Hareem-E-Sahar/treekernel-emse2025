package jp.ac.keio.ae.comp.yamaguti.doddle.data;

import java.io.*;
import java.util.*;

/**
 * @author takeshi morita
 */
public class JpnWordNetDic {

    private static String TREE_DATA_FILE = "tree.data";

    private static String WORD_DATA_FILE = "word.data";

    private static String CONCEPT_DATA_FILE = "concept.data";

    private static String RELATION_DATA_FILE = "relation.data";

    private static String TREE_INDEX_FILE = "tree.index";

    private static String WORD_INDEX_FILE = "word.index";

    private static String CONCEPT_INDEX_FILE = "concept.index";

    private static String RELATION_INDEX_FILE = "relation.index";

    private static RandomAccessFile jpnwnTreeDataFile;

    private static RandomAccessFile jpnwnWordDataFile;

    private static RandomAccessFile jpnwnConceptDataFile;

    private static RandomAccessFile jpnwnRelationDataFile;

    private static RandomAccessFile jpnwnTreeIndexFile;

    private static RandomAccessFile jpnwnWordIndexFile;

    private static RandomAccessFile jpnwnConceptIndexFile;

    private static RandomAccessFile jpnwnRelationIndexFile;

    private static Map<String, Concept> jpnwnURIConceptMap;

    private static Map<String, Set<String>> jpnwnWordIDSetMap;

    public static boolean initJPNWNDic() {
        if (jpnwnURIConceptMap != null && 0 < jpnwnURIConceptMap.size()) {
            return true;
        }
        jpnwnURIConceptMap = new HashMap<String, Concept>();
        jpnwnWordIDSetMap = new HashMap<String, Set<String>>();
        String baseDir = DODDLEConstants.JPNWN_HOME + File.separator;
        try {
            jpnwnTreeDataFile = new RandomAccessFile(baseDir + TREE_DATA_FILE, "r");
            jpnwnWordDataFile = new RandomAccessFile(baseDir + WORD_DATA_FILE, "r");
            jpnwnConceptDataFile = new RandomAccessFile(baseDir + CONCEPT_DATA_FILE, "r");
            jpnwnTreeIndexFile = new RandomAccessFile(baseDir + TREE_INDEX_FILE, "r");
            jpnwnWordIndexFile = new RandomAccessFile(baseDir + WORD_INDEX_FILE, "r");
            jpnwnConceptIndexFile = new RandomAccessFile(baseDir + CONCEPT_INDEX_FILE, "r");
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return false;
        }
        return true;
    }

    private static long getIndexFpListSize() {
        RandomAccessFile indexFpListFile = null;
        indexFpListFile = jpnwnWordIndexFile;
        try {
            return indexFpListFile.length() / 10;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return -1;
    }

    private static long getIndexFp(long fp) {
        RandomAccessFile indexFpListFile = null;
        indexFpListFile = jpnwnWordIndexFile;
        try {
            indexFpListFile.seek(fp);
            String fpStr = indexFpListFile.readLine();
            if (fpStr == null) {
                return -1;
            }
            return Long.valueOf(fpStr);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return -1;
    }

    private static long getIndexFileSize(RandomAccessFile indexFile) {
        try {
            return indexFile.length() / 10;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return -1;
    }

    private static long getConceptIndexFileSize() {
        RandomAccessFile indexFile = null;
        indexFile = jpnwnConceptIndexFile;
        return getIndexFileSize(indexFile);
    }

    private static long getTreeIndexFileSize() {
        RandomAccessFile indexFile = null;
        indexFile = jpnwnTreeIndexFile;
        return getIndexFileSize(indexFile);
    }

    private static long getRelationIndexFileSize() {
        return getIndexFileSize(jpnwnRelationIndexFile);
    }

    private static long getDataFp(long fp, RandomAccessFile indexFile) {
        try {
            indexFile.seek(fp);
            return Long.valueOf(indexFile.readLine());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return -1;
    }

    private static long getConceptDataFp(long fp) {
        RandomAccessFile indexFile = null;
        indexFile = jpnwnConceptIndexFile;
        return getDataFp(fp, indexFile);
    }

    private static long getTreeDataFp(long fp) {
        RandomAccessFile indexFile = null;
        indexFile = jpnwnTreeIndexFile;
        return getDataFp(fp, indexFile);
    }

    private static long getRelationDataFp(long fp) {
        return getDataFp(fp, jpnwnRelationIndexFile);
    }

    private static String getTermAndIndexFpSet(long ifp) {
        RandomAccessFile indexFile = null;
        indexFile = jpnwnWordDataFile;
        try {
            indexFile.seek(ifp);
            return new String(indexFile.readLine().getBytes("ISO8859_1"), "UTF-8");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

    private static String getData(long dfp, RandomAccessFile dataFile, String encoding) {
        try {
            dataFile.seek(dfp);
            return new String(dataFile.readLine().getBytes("ISO8859_1"), encoding);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

    private static String getConceptData(long dfp) {
        RandomAccessFile dataFile = null;
        dataFile = jpnwnConceptDataFile;
        return getData(dfp, dataFile, "UTF-8");
    }

    private static String getTreeData(long dfp) {
        RandomAccessFile dataFile = null;
        dataFile = jpnwnTreeDataFile;
        return getData(dfp, dataFile, "ISO8859_1");
    }

    private static String getRelationData(long dfp) {
        return getData(dfp, jpnwnRelationDataFile, "ISO8859_1");
    }

    public static String getConceptData(String id) {
        long low = 0;
        long conceptIndexFileSize = getConceptIndexFileSize();
        long high = conceptIndexFileSize;
        while (low <= high) {
            long mid = (low + high) / 2;
            if (conceptIndexFileSize - 1 < mid) {
                return null;
            }
            long conceptDataFP = getConceptDataFp(mid * 10);
            if (conceptDataFP == -1) {
                return null;
            }
            String conceptData = getConceptData(conceptDataFP);
            if (conceptData == null) {
                return null;
            }
            String[] lines = conceptData.split("\t");
            String searchedID = lines[0];
            if (searchedID.compareTo(id) == 0) {
                return conceptData;
            } else if (0 < searchedID.compareTo(id)) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return null;
    }

    public static String getTreeData(String id) {
        long low = 0;
        long treeIndexFileSize = getTreeIndexFileSize();
        long high = treeIndexFileSize;
        while (low <= high) {
            long mid = (low + high) / 2;
            if (treeIndexFileSize - 1 <= mid) {
                return null;
            }
            long treeDataFP = getTreeDataFp(mid * 10);
            if (treeDataFP == -1) {
                return null;
            }
            String treeData = getTreeData(treeDataFP);
            if (treeData == null) {
                return null;
            }
            String[] lines = treeData.split("\t");
            String searchedID = lines[0];
            if (searchedID.compareTo(id) == 0) {
                return treeData;
            } else if (0 < searchedID.compareTo(id)) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return null;
    }

    public static String getRelationData(String id) {
        long low = 0;
        long relationIndexFileSize = getRelationIndexFileSize();
        long high = relationIndexFileSize;
        while (low <= high) {
            long mid = (low + high) / 2;
            if (relationIndexFileSize - 1 <= mid) {
                return null;
            }
            long relationDataFP = getRelationDataFp(mid * 10);
            if (relationDataFP == -1) {
                return null;
            }
            String relationData = getRelationData(relationDataFP);
            if (relationData == null) {
                return null;
            }
            String[] lines = relationData.split("\t");
            String searchedID = lines[0];
            if (searchedID.compareTo(id) == 0) {
                return relationData;
            } else if (0 < searchedID.compareTo(id)) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return null;
    }

    private static Concept getConcept(long dfp) {
        RandomAccessFile dataFile = null;
        try {
            dataFile = jpnwnConceptDataFile;
            dataFile.seek(dfp);
            String data = new String(dataFile.readLine().getBytes("ISO8859_1"), "UTF-8");
            String[] dataArray = data.split("\\^");
            String[] conceptData = new String[4];
            String id = dataArray[0].replaceAll("\t", "");
            System.arraycopy(dataArray, 1, conceptData, 0, conceptData.length);
            String uri = "";
            Concept c = null;
            uri = DODDLEConstants.JPN_WN_URI + id;
            c = new Concept(uri, conceptData);
            jpnwnURIConceptMap.put(uri, c);
            return c;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

    private static Set<Long> getdataFpSet(long high, String term) {
        long low = 0;
        Set<Long> dataFpSet = new HashSet<Long>();
        while (low <= high) {
            long mid = (low + high) / 2;
            long indexFP = getIndexFp(mid * 10);
            if (indexFP == -1) {
                return dataFpSet;
            }
            String line = getTermAndIndexFpSet(indexFP);
            String[] lines = line.split("\t");
            String searchedTerm = lines[0];
            if (searchedTerm.compareTo(term) == 0) {
                for (int i = 1; i < lines.length; i++) {
                    dataFpSet.add(Long.valueOf(lines[i]));
                }
                return dataFpSet;
            } else if (0 < searchedTerm.compareTo(term)) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return dataFpSet;
    }

    public static Set<String> getSynsetSet(String word) {
        Map<String, Set<String>> wordIDSetMap = null;
        wordIDSetMap = jpnwnWordIDSetMap;
        if (wordIDSetMap.get(word) != null) {
            return wordIDSetMap.get(word);
        }
        Set<Long> dataFpSet = getdataFpSet(getIndexFpListSize(), word);
        Set<String> idSet = new HashSet<String>();
        for (Long dfp : dataFpSet) {
            Concept c = getConcept(dfp);
            idSet.add(c.getLocalName());
        }
        wordIDSetMap.put(word, idSet);
        return idSet;
    }

    public static Set<String> getJPNWNSynsetSet(String word) {
        return getSynsetSet(word);
    }

    private static void addURISet(String data, String relation, Set<String> uriSet) {
        String[] idSet = data.split("\\|" + relation)[1].split("\t");
        for (String id : idSet) {
            if (id.indexOf("|") != -1) {
                break;
            }
            if (!id.equals("")) {
                uriSet.add(DODDLEConstants.JPN_WN_URI + id);
            }
        }
    }

    /**
     * 
     * 入力概念集合を入力として，その中から動詞的概念の集合を返す
     * 
     */
    public static Set<Concept> getVerbConceptSet(Set<Concept> inputConceptSet) {
        Set<Concept> verbConceptSet = new HashSet<Concept>();
        for (Concept c : inputConceptSet) {
            String id = c.getLocalName();
            String data = getRelationData(id);
            if (data != null && (data.indexOf("|agent") != -1 || data.indexOf("|object") != -1)) {
                verbConceptSet.add(c);
            }
        }
        return verbConceptSet;
    }

    public static Set<String> getRelationValueSet(String relation, String vid, List<List<Concept>> trimmedConceptList) {
        Set<String> uriSet = new HashSet<String>();
        String data = getRelationData(vid);
        if (data != null) {
            if (data.indexOf("|" + relation) == -1) {
                return uriSet;
            }
            addURISet(data, relation, uriSet);
        }
        for (List<Concept> conceptList : trimmedConceptList) {
            for (Concept c : conceptList) {
                String tid = c.getLocalName();
                data = getRelationData(tid);
                if (data == null) {
                    continue;
                }
                if (data.indexOf("|" + relation) == -1) {
                    continue;
                }
                addURISet(data, relation, uriSet);
            }
        }
        return uriSet;
    }

    public static Concept getConcept(String id) {
        Map<String, Concept> uriConceptMap = null;
        String ns = DODDLEConstants.JPN_WN_URI;
        uriConceptMap = jpnwnURIConceptMap;
        String uri = ns + id;
        if (uriConceptMap.get(uri) != null) {
            return uriConceptMap.get(uri);
        }
        String data = getConceptData(id);
        if (data == null) {
            return null;
        }
        String[] dataArray = data.split("\\^");
        String[] conceptData = new String[4];
        System.arraycopy(dataArray, 1, conceptData, 0, conceptData.length);
        Concept c = new Concept(uri, conceptData);
        uriConceptMap.put(uri, c);
        return c;
    }

    public static Concept getJPNWNConcept(String synset) {
        return getConcept(synset);
    }

    public static void main(String[] args) throws Exception {
        JpnWordNetDic.initJPNWNDic();
        String id1 = "08675967-n";
        Concept c = JpnWordNetDic.getConcept(id1);
        System.out.println(c);
        Set<String> idSet = new HashSet<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(DODDLEConstants.JPNWN_HOME + "tree.data"), "UTF-8"));
        while (reader.ready()) {
            String line = reader.readLine();
            if (line.indexOf(id1) != -1) {
                String id = line.split("\t\\|")[0];
                idSet.add(id);
            }
        }
        System.out.println(idSet);
        for (String id : idSet) {
            c = JpnWordNetDic.getConcept(id);
            System.out.println(c);
        }
    }
}
