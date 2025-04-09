import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

public class Document {

    static Map freqWords = new Hashtable();

    String stFilename;

    Document(String stName) {
        stFilename = stName;
    }

    String stAuthor = null;

    double dDistance = 0.0;

    Vector unsortedTop50 = new Vector();

    double dNumProcessed = 0;

    public void setAuthor(String stAuth) {
        stAuthor = stAuth;
    }

    public void createWordTable() {
        createWordTable(stFilename);
    }

    public void createWordTable(String stFilename) {
        String stLine = null;
        String stWord = null;
        double dOccurCount = 0;
        try {
            BufferedReader fl = new BufferedReader(new FileReader(stFilename));
            while ((stLine = fl.readLine()) != null) {
                StringTokenizer line = new StringTokenizer(stLine);
                while (line.hasMoreTokens()) {
                    dNumProcessed++;
                    stWord = line.nextToken();
                    stWord = stWord.toLowerCase();
                    if (freqWords.containsKey(stWord)) {
                        Object temp = freqWords.get(stWord);
                        dOccurCount = ((Double) temp).doubleValue();
                        dOccurCount++;
                        freqWords.put(stWord, new Double(dOccurCount));
                    } else {
                        freqWords.put(stWord, new Double(1));
                    }
                }
            }
            fl.close();
        } catch (IOException e) {
            System.out.println("ERROR READING FILE: " + stFilename);
        }
    }

    public void createLetterTable() {
        createLetterTable(stFilename);
    }

    public void createLetterTable(String stFilename) {
        String stLine = null;
        String stLetter = null;
        double dOccurCount = 0;
        try {
            BufferedReader fl = new BufferedReader(new FileReader(stFilename));
            while ((stLine = fl.readLine()) != null) {
                for (int iCounter = 0; iCounter < stLine.length(); iCounter++) {
                    dNumProcessed++;
                    stLetter = stLine.substring(iCounter, iCounter + 1);
                    stLetter = stLetter.toLowerCase();
                    if (freqWords.containsKey(stLetter)) {
                        Object temp = freqWords.get(stLetter);
                        dOccurCount = ((Double) temp).doubleValue();
                        dOccurCount++;
                        freqWords.put(stLetter, new Double(dOccurCount));
                    } else {
                        freqWords.put(stLetter, new Double(1));
                    }
                }
            }
            fl.close();
        } catch (IOException e) {
            System.out.println("ERROR READING FILE: " + stFilename);
        }
    }

    public void displayTable() {
        System.out.println(freqWords.toString());
    }

    public void displayUnsortedTop50Vector() {
        System.out.println(unsortedTop50.toString());
    }

    public void createUnsortedTop50Vector() {
        Hashtable reverseTable = new Hashtable();
        Vector v = new Vector(freqWords.keySet());
        for (Enumeration e = v.elements(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            Double matchingValue = (Double) freqWords.get(key);
            double dMatchingValue = matchingValue.doubleValue();
            if (reverseTable.containsKey(matchingValue)) {
                dMatchingValue = dMatchingValue + 0.1;
                reverseTable.put(new Double(dMatchingValue), key);
            } else {
                reverseTable.put(matchingValue, key);
            }
        }
        List myList = new ArrayList(reverseTable.keySet());
        Collections.sort(myList);
        Collections.reverse(myList);
        int iUpperLimit = 0;
        if (reverseTable.size() < 50) {
            iUpperLimit = reverseTable.size();
        } else {
            iUpperLimit = 50;
        }
        List top50 = myList.subList(0, iUpperLimit);
        Object[] myArray = top50.toArray();
        Vector top50Vector = new Vector();
        for (int iCounter = 0; iCounter < iUpperLimit; iCounter++) {
            top50Vector.addElement(reverseTable.get(myArray[iCounter]));
        }
        String stLine = null;
        String stWord = null;
        double dOccurCount = 0;
        try {
            BufferedReader fl = new BufferedReader(new FileReader(stFilename));
            while ((stLine = fl.readLine()) != null) {
                StringTokenizer line = new StringTokenizer(stLine);
                while (line.hasMoreTokens()) {
                    dNumProcessed++;
                    stWord = line.nextToken();
                    stWord = stWord.toLowerCase();
                    if (top50Vector.contains(stWord)) {
                        unsortedTop50.addElement(stWord);
                    }
                }
            }
            fl.close();
        } catch (IOException e) {
            System.out.println("ERROR READING FILE: " + stFilename);
        }
    }

    public void createNonPreprocessedLetterVector() {
        String stLetter = null;
        String stLine = null;
        try {
            BufferedReader fl = new BufferedReader(new FileReader(stFilename));
            while ((stLine = fl.readLine()) != null) {
                for (int iCounter = 0; iCounter < stLine.length(); iCounter++) {
                    stLetter = stLine.substring(iCounter, iCounter + 1);
                    stLetter = stLetter.toLowerCase();
                    unsortedTop50.addElement(stLetter);
                }
            }
            fl.close();
        } catch (IOException e) {
            System.out.println("ERROR READING FILE");
        }
    }

    public void createNonPreprocessedWordVector() {
        String stLine = null;
        String stWord = null;
        double dOccurCount = 0;
        try {
            BufferedReader fl = new BufferedReader(new FileReader(stFilename));
            while ((stLine = fl.readLine()) != null) {
                StringTokenizer line = new StringTokenizer(stLine);
                while (line.hasMoreTokens()) {
                    dNumProcessed++;
                    stWord = line.nextToken();
                    stWord = stWord.toLowerCase();
                    unsortedTop50.addElement(stWord);
                }
            }
            fl.close();
        } catch (IOException e) {
            System.out.println("ERROR READING FILE: " + stFilename);
        }
    }
}
