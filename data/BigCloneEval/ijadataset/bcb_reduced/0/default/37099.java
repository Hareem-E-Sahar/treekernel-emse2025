import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

public class Detector {

    /**
	 * @param args
	 */
    public static boolean USE_STEMMING = true;

    public static boolean SORT_SENTENCE = true;

    public static int SUFFIX_DEPTH = 3;

    public static double EQUIVALENCY_THRESHOLD_LEVEL = 0.001;

    public static Node root;

    public static HashMap<String, Double> tempImpactValues;

    public static HashMap<String, Double> impactValues;

    public static HashMap<String, String> documentWords;

    public static int documentCount;

    @SuppressWarnings("unchecked")
    public static HashMap[] similarities;

    public static File[] fileList;

    public static void main(String[] args) throws IOException {
        String oneSentencePerLineDocumentsFolderPath = "C:\\Research\\NewDocuments";
        execute(oneSentencePerLineDocumentsFolderPath);
    }

    public static void execute(String DocumentsFolderPath, int thresholdLevel, int suffixDepth, int wordImpactAlgorithmNo, boolean useSort, boolean useStemming) throws IOException {
        EQUIVALENCY_THRESHOLD_LEVEL = thresholdLevel * thresholdLevel * 0.001;
        SUFFIX_DEPTH = suffixDepth;
        SORT_SENTENCE = useSort;
        USE_STEMMING = useStemming;
        execute(DocumentsFolderPath);
    }

    public static ArrayList<String> getDocumentNames() {
        int i;
        ArrayList<String> documentNames = new ArrayList<String>();
        for (i = 0; i < fileList.length; i++) documentNames.add(fileList[i].getName());
        return documentNames;
    }

    public static String getDocument(String documentName) throws IOException {
        String sentence = "", document = "";
        int i, fileNo = 0;
        for (i = 0; i < fileList.length; i++) if (fileList[i].getName().equals(documentName)) {
            fileNo = i;
            break;
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fileList[fileNo]);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(fis, "8859_9");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        BufferedReader bufferedReader = new BufferedReader(isr);
        while ((sentence = bufferedReader.readLine()) != null) {
            document += sentence + "\n";
        }
        bufferedReader.close();
        isr.close();
        fis.close();
        return document;
    }

    public static ArrayList<String> getSimilarDocumentNames(String documentName) {
        int i, fileNo = 0;
        double ownScore;
        ArrayList<String> similarDocumentNames = new ArrayList<String>();
        for (i = 0; i < fileList.length; i++) if (fileList[i].getName().equals(documentName)) {
            fileNo = i;
            break;
        }
        ownScore = (Double) similarities[fileNo].get(fileNo);
        for (i = 0; i < fileList.length; i++) if (similarities[fileNo].containsKey(i)) if ((Double) similarities[fileNo].get(i) > EQUIVALENCY_THRESHOLD_LEVEL * ownScore) similarDocumentNames.add(fileList[i].getName());
        return similarDocumentNames;
    }

    public static Double getSimilarity(String documentName1, String documentName2) {
        int i, documentNo1 = 0, documentNo2 = 0;
        for (i = 0; i < fileList.length; i++) {
            if (fileList[i].getName().equals(documentName1)) documentNo1 = i;
            if (fileList[i].getName().equals(documentName2)) documentNo2 = i;
        }
        return (Double) similarities[documentNo1].get(documentNo2);
    }

    @SuppressWarnings("unchecked")
    public static void execute(String documentFolderPath) throws IOException {
        int i, fcnt = 0;
        init();
        File documentFolder = new File(documentFolderPath);
        File[] fullFileList = documentFolder.listFiles();
        for (i = 0; i < fullFileList.length; i++) if (fullFileList[i].isFile()) fcnt++;
        fileList = new File[fcnt];
        for (i = 0, fcnt = 0; i < fullFileList.length; i++) if (fullFileList[i].isFile()) fileList[fcnt++] = fullFileList[i];
        similarities = new HashMap[fileList.length];
        for (i = 0; i < fileList.length; i++) similarities[i] = new HashMap();
        documentCount = fileList.length;
        for (i = 0; i < fileList.length; i++) getWordsFromFile(fileList[i], i);
        normalizetempImpactValues();
        System.out.println("getWordsFromFile is Finished!!");
        System.out.println("Total File Count: " + fileList.length);
        for (i = 0; i < fileList.length; i++) {
            if (i % 10 == 0) System.out.println(i + "th file is being processed!!!");
            processFile(fileList[i], i);
        }
        report();
    }

    public static void report() {
        int i, j;
        double val, equalVal;
        for (i = 0; i < 10 && i < fileList.length; i++) {
            System.out.print("Similar Documents to:\t");
            System.out.println(fileList[i].getName());
            for (j = 0; j < similarities.length; j++) {
                equalVal = (Double) similarities[i].get(i);
                if (similarities[i].containsKey(j)) {
                    val = (Double) similarities[i].get(j);
                    if (val > EQUIVALENCY_THRESHOLD_LEVEL * equalVal) System.out.println(fileList[j].getName() + " " + val);
                }
            }
            System.out.println("-------------------------------------");
        }
    }

    public static void init() {
        root = new Node("", 0);
        tempImpactValues = new HashMap<String, Double>();
        impactValues = new HashMap<String, Double>();
    }

    public static void addWord(String word) {
        double val = 0;
        word = getStemmedWord(word);
        if (documentWords.containsKey(word)) return;
        if (tempImpactValues.containsKey(word)) {
            val = tempImpactValues.get(word);
            tempImpactValues.remove(word);
        }
        val++;
        tempImpactValues.put(word, val);
        documentWords.put(word, "");
    }

    public static void getWordsFromFile(File file, int documentNo) throws IOException {
        String sentence = "";
        documentWords = new HashMap<String, String>();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(fis, "8859_9");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        BufferedReader bufferedReader = new BufferedReader(isr);
        while ((sentence = bufferedReader.readLine()) != null) {
            StringTokenizer tokenizer = new StringTokenizer(sentence, " ");
            while (tokenizer.hasMoreTokens()) addWord(tokenizer.nextToken());
        }
        bufferedReader.close();
        isr.close();
        fis.close();
    }

    private static void normalizetempImpactValues() {
        String word;
        double val;
        Set<String> words = tempImpactValues.keySet();
        Iterator<String> wordsIterator = words.iterator();
        while (wordsIterator.hasNext()) {
            word = wordsIterator.next();
            val = tempImpactValues.get(word);
            val = documentCount / val;
            impactValues.put(word, val);
        }
    }

    public static void processFile(File file, int documentNo) throws IOException {
        String sentence = "";
        int i;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(fis, "8859_9");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        BufferedReader bufferedReader = new BufferedReader(isr);
        while ((sentence = bufferedReader.readLine()) != null) {
            sentence = sortSentence(sentence);
            for (i = 0; i < SUFFIX_DEPTH; i++) insertToTree(root, getNthSuffix(sentence, i, documentNo), 0);
        }
        bufferedReader.close();
        isr.close();
        fis.close();
    }

    private static String sortSentence(String sentence) {
        String orderedSentence = "";
        if (SORT_SENTENCE == false) return sentence;
        StringTokenizer tokenizer = new StringTokenizer(sentence, " ");
        ArrayList<String> words = new ArrayList<String>();
        while (tokenizer.hasMoreTokens()) words.add(getStemmedWord(tokenizer.nextToken()));
        Collections.sort(words, new Comparator<String>() {

            public int compare(String o1, String o2) {
                return (int) ((double) impactValues.get(o2) - impactValues.get(o1));
            }
        });
        Iterator<String> wordsIterator = words.iterator();
        while (wordsIterator.hasNext()) orderedSentence += " " + wordsIterator.next();
        return orderedSentence;
    }

    public static double getImpactValue(String word) {
        return impactValues.get(word);
    }

    public static void insertToTree(Node node, Suffix suffix, int depth) {
        StringTokenizer tokenizer = new StringTokenizer(suffix.getSuffixText(), " ");
        node.addDocument(suffix.getDocumentNo());
        Iterator<Integer> documentIterator = node.getDocumentIterator();
        int documentNo;
        while (documentIterator.hasNext()) {
            documentNo = documentIterator.next();
            increaseSimilarity(suffix.getDocumentNo(), documentNo, node.getImpactValue(), depth, node.word);
        }
        if (tokenizer.hasMoreTokens() == false) return;
        String word = tokenizer.nextToken();
        word = getStemmedWord(word);
        if (node.hasNext(word) == false) {
            node.addNext(word, getImpactValue(word));
        }
        insertToTree(node.getNext(word), getNthSuffix(suffix.getSuffixText(), 1, suffix.getDocumentNo()), depth + 1);
    }

    @SuppressWarnings("unchecked")
    public static void increaseSimilarity(int documentNo, int documentNo2, double impactValue, int depth, String word) {
        double newValue = impactValue * depth;
        if (impactValue == 0) return;
        if (similarities[documentNo].containsKey(documentNo2)) {
            newValue += (Double) similarities[documentNo].get(documentNo2);
            similarities[documentNo].remove(documentNo2);
        }
        similarities[documentNo].put(documentNo2, newValue);
        newValue = impactValue * depth;
        if (documentNo2 == documentNo) return;
        if (similarities[documentNo2].containsKey(documentNo)) {
            newValue += (Double) similarities[documentNo2].get(documentNo);
            similarities[documentNo2].remove(documentNo);
        }
        similarities[documentNo2].put(documentNo, newValue);
    }

    public static Suffix getNthSuffix(String sentence, int ignoreCount, int documentNo) {
        String suffixText = "";
        StringTokenizer tokenizer = new StringTokenizer(sentence, " ");
        for (int i = 0; i < ignoreCount && tokenizer.hasMoreTokens(); i++) tokenizer.nextToken();
        while (tokenizer.hasMoreTokens()) suffixText += " " + tokenizer.nextToken();
        return new Suffix(suffixText, documentNo);
    }

    public static String getStemmedWord(String word) {
        if (word.length() < 5) return word;
        return word.substring(0, 5);
    }
}
