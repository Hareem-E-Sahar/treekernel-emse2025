import java.io.*;
import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.*;
import javax.swing.text.*;

public class Processor {

    Document currSampleWork;

    Document currTestingWork;

    Vector sampleWorks = new Vector();

    Vector testingWorks = new Vector();

    Vector unsortedTop50Vector = null;

    public void addTestingWork(String stFilename, String stAuthor) {
        Document newDoc = new Document(stFilename);
        newDoc.setAuthor(stAuthor);
        testingWorks.addElement(newDoc);
    }

    public void addSampleWork(String stFilename, String stAuthor) {
        Document newDoc = new Document(stFilename);
        newDoc.setAuthor(stAuthor);
        sampleWorks.addElement(newDoc);
    }

    public double displayDistance(int iWork) {
        currSampleWork = (Document) sampleWorks.elementAt(iWork);
        return currSampleWork.dDistance;
    }

    public String displayFilename(int iWork) {
        currSampleWork = (Document) sampleWorks.elementAt(iWork);
        return currSampleWork.stFilename;
    }

    public String displayAuthor(int iWork) {
        currSampleWork = (Document) sampleWorks.elementAt(iWork);
        return currSampleWork.stAuthor;
    }

    public String displayTestingFilename(int iWork) {
        currTestingWork = (Document) testingWorks.elementAt(iWork);
        return currTestingWork.stFilename;
    }

    public String displayTestingAuthor(int iWork) {
        currTestingWork = (Document) testingWorks.elementAt(iWork);
        return currTestingWork.stAuthor;
    }

    public void createData(String stEventType, String stPreType) {
        int iNumSampleWorks = sampleWorks.size();
        int iNumTestingWorks = testingWorks.size();
        for (int iCounter = 0; iCounter < iNumSampleWorks; iCounter++) {
            currSampleWork = (Document) sampleWorks.elementAt(iCounter);
            if (stPreType.equals("Yes")) {
                if (stEventType.equals("Word")) {
                    currSampleWork.createWordTable();
                } else {
                    currSampleWork.createLetterTable();
                }
            }
        }
        for (int iCounter = 0; iCounter < iNumTestingWorks; iCounter++) {
            currTestingWork = (Document) testingWorks.elementAt(iCounter);
            if (stPreType.equals("Yes")) {
                if (stEventType.equals("Word")) {
                    currTestingWork.createWordTable();
                } else {
                    currTestingWork.createLetterTable();
                }
            }
        }
        for (int iCounter = 0; iCounter < iNumSampleWorks; iCounter++) {
            currSampleWork = (Document) sampleWorks.elementAt(iCounter);
            if (stPreType.equals("Yes")) {
                currSampleWork.createUnsortedTop50Vector();
            } else {
                if (stEventType.equals("Word")) {
                    currSampleWork.createNonPreprocessedWordVector();
                } else {
                    currSampleWork.createNonPreprocessedLetterVector();
                }
            }
        }
        for (int iCounter = 0; iCounter < iNumTestingWorks; iCounter++) {
            currTestingWork = (Document) testingWorks.elementAt(iCounter);
            if (stPreType.equals("Yes")) {
                currTestingWork.createUnsortedTop50Vector();
            } else {
                if (stEventType.equals("Word")) {
                    currTestingWork.createNonPreprocessedWordVector();
                } else {
                    currTestingWork.createNonPreprocessedLetterVector();
                }
            }
        }
    }

    public void crossEntDistance(JTextArea results) {
        Document currSampleWork = null;
        Document currTestingWork = null;
        Vector dataBase = new Vector();
        Vector prefix = new Vector();
        int dBaseLength = 100;
        int idBaseStart = 0;
        int iNumSampleWorks = sampleWorks.size();
        int iNumTestingWorks = testingWorks.size();
        int iMinDBLength = 99999;
        for (int iCounter = 0; iCounter < iNumSampleWorks; iCounter++) {
            if (((Document) sampleWorks.elementAt(iCounter)).unsortedTop50.size() < iMinDBLength) {
                iMinDBLength = ((Document) sampleWorks.elementAt(iCounter)).unsortedTop50.size();
            }
        }
        dBaseLength = iMinDBLength / 2;
        for (int iCounter3 = 0; iCounter3 < iNumTestingWorks; iCounter3++) {
            currTestingWork = (Document) testingWorks.elementAt(iCounter3);
            for (int iCounter = 0; iCounter < currTestingWork.unsortedTop50.size(); iCounter++) {
                prefix.addElement(currTestingWork.unsortedTop50.elementAt(iCounter));
            }
            for (int iCounter = 0; iCounter < iNumSampleWorks; iCounter++) {
                currSampleWork = (Document) sampleWorks.elementAt(iCounter);
                dataBase.removeAllElements();
                for (int iCounter2 = 0; iCounter2 < dBaseLength; iCounter2++) {
                    dataBase.addElement(currSampleWork.unsortedTop50.elementAt(iCounter2));
                }
                int iDepth = 0;
                int iPIndex = 0;
                int iDIndex = 0;
                int iPSavedIndex = 0;
                int iDSavedIndex = 0;
                int iPStartIndex = 0;
                int iHighestML = 0;
                int iML = 0;
                int iMLSum = 0;
                double dMLAverage = 0.0;
                int iNumML = 0;
                double H = 0.0;
                while (iPIndex < prefix.size()) {
                    while (iDIndex < dataBase.size()) {
                        if ((iPIndex >= prefix.size()) && (iDepth == 1)) {
                            iDIndex = iDSavedIndex;
                            iPIndex = iPSavedIndex;
                            iDepth = 0;
                            iDIndex++;
                            iML = 0;
                        }
                        if (iDepth == 0) {
                            if (dataBase.elementAt(iDIndex).equals(prefix.elementAt(iPIndex))) {
                                iML++;
                                if (iML > iHighestML) {
                                    iHighestML = iML;
                                }
                                iPSavedIndex = iPIndex;
                                iDSavedIndex = iDIndex;
                                iDepth = 1;
                                iDIndex++;
                                iPIndex++;
                            } else {
                                iML = 0;
                                if (iML > iHighestML) {
                                    iHighestML = iML;
                                }
                                iDepth = 0;
                                iDIndex++;
                            }
                        } else {
                            if (dataBase.elementAt(iDIndex).equals(prefix.elementAt(iPIndex))) {
                                iML++;
                                if (iML > iHighestML) {
                                    iHighestML = iML;
                                }
                                iDIndex++;
                                iPIndex++;
                            } else {
                                iPIndex = iPSavedIndex;
                                iDIndex = iDSavedIndex;
                                iDepth = 0;
                                iDIndex++;
                                iML = 0;
                            }
                        }
                    }
                    iDepth = 0;
                    iMLSum = iMLSum + iHighestML;
                    iNumML++;
                    iPStartIndex++;
                    iDIndex = idBaseStart;
                    iPIndex = iPStartIndex;
                    iHighestML = 0;
                    iML = 0;
                }
                dMLAverage = (double) iMLSum / (double) iNumML;
                H = (Math.log(dBaseLength) / Math.log(2.0)) / dMLAverage;
                currSampleWork.dDistance = H;
            }
            double dSmallestDistance = 99999;
            String stMatchingAuthor = "";
            for (int iCounter4 = 0; iCounter4 < iNumSampleWorks; iCounter4++) {
                Double dTemp = new Double(displayDistance(iCounter4));
                if (dTemp.doubleValue() < dSmallestDistance) {
                    dSmallestDistance = dTemp.doubleValue();
                    stMatchingAuthor = displayAuthor(iCounter4);
                }
            }
            results.append(stMatchingAuthor + "\n");
        }
    }

    public void LZWDistance(JTextArea results) {
        int iNumSampleWorks = sampleWorks.size();
        int iNumTestingWorks = testingWorks.size();
        Document currSampleWork = null;
        Document currTestingWork = null;
        Vector LZWResults = new Vector();
        for (int iCounter2 = 0; iCounter2 < iNumTestingWorks; iCounter2++) {
            currTestingWork = (Document) testingWorks.elementAt(iCounter2);
            for (int iCounter = 0; iCounter < iNumSampleWorks; iCounter++) {
                currSampleWork = (Document) sampleWorks.elementAt(iCounter);
                String tempString = null;
                try {
                    PrintWriter testingText = new PrintWriter(new BufferedWriter(new FileWriter("testingText.txt")));
                    for (int iCounter3 = 0; iCounter3 < currTestingWork.unsortedTop50.size(); iCounter3++) {
                        tempString = (String) currTestingWork.unsortedTop50.elementAt(iCounter3);
                        if (tempString.length() > 1) {
                            testingText.print(tempString + " ");
                        } else {
                            testingText.print(tempString);
                        }
                    }
                    testingText.close();
                } catch (Exception e) {
                }
                try {
                    PrintWriter sampleText = new PrintWriter(new BufferedWriter(new FileWriter("sampleText.txt")));
                    for (int iCounter3 = 0; iCounter3 < currSampleWork.unsortedTop50.size(); iCounter3++) {
                        tempString = (String) currSampleWork.unsortedTop50.elementAt(iCounter3);
                        if (tempString.length() > 1) {
                            sampleText.print(tempString + " ");
                        } else {
                            sampleText.print(tempString);
                        }
                    }
                    sampleText.close();
                } catch (Exception e) {
                }
                try {
                    PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter("tempfile.txt")));
                    Process p = Runtime.getRuntime().exec("cat testingText.txt sampleText.txt");
                    BufferedInputStream buffer = new BufferedInputStream(p.getInputStream());
                    DataInputStream commandResult = new DataInputStream(buffer);
                    String s = null;
                    try {
                        while ((s = commandResult.readLine()) != null) output.print(s);
                        commandResult.close();
                        output.close();
                    } catch (Exception e) {
                    }
                } catch (Exception e) {
                }
                try {
                    Process p = Runtime.getRuntime().exec("gzip -v -9 tempfile.txt");
                    BufferedInputStream buffer = new BufferedInputStream(p.getInputStream());
                    DataInputStream commandResult = new DataInputStream(buffer);
                    String s = null;
                    try {
                        while ((s = commandResult.readLine()) != null) System.out.println(s);
                        commandResult.close();
                    } catch (Exception e) {
                    }
                } catch (Exception e) {
                }
                try {
                    Process p = Runtime.getRuntime().exec("gzip -l tempfile.txt.gz");
                    BufferedInputStream buffer = new BufferedInputStream(p.getInputStream());
                    DataInputStream commandResult = new DataInputStream(buffer);
                    String s = null;
                    try {
                        while ((s = commandResult.readLine()) != null) {
                            StringTokenizer line = new StringTokenizer(s);
                            while (line.hasMoreTokens()) {
                                LZWResults.addElement(line.nextToken());
                            }
                        }
                        commandResult.close();
                    } catch (Exception e) {
                    }
                } catch (Exception e) {
                }
                try {
                    Process p = Runtime.getRuntime().exec("rm tempfile.txt.gz");
                    BufferedInputStream buffer = new BufferedInputStream(p.getInputStream());
                    DataInputStream commandResult = new DataInputStream(buffer);
                    String s = null;
                    try {
                        while ((s = commandResult.readLine()) != null) {
                            StringTokenizer line = new StringTokenizer(s);
                            while (line.hasMoreTokens()) {
                                LZWResults.addElement(line.nextToken());
                            }
                        }
                        commandResult.close();
                    } catch (Exception e) {
                    }
                } catch (Exception e) {
                }
                try {
                    Process p = Runtime.getRuntime().exec("rm tempfile.txt");
                    BufferedInputStream buffer = new BufferedInputStream(p.getInputStream());
                    DataInputStream commandResult = new DataInputStream(buffer);
                    String s = null;
                    try {
                        while ((s = commandResult.readLine()) != null) {
                            StringTokenizer line = new StringTokenizer(s);
                            while (line.hasMoreTokens()) {
                                LZWResults.addElement(line.nextToken());
                            }
                        }
                        commandResult.close();
                    } catch (Exception e) {
                    }
                } catch (Exception e) {
                }
                try {
                    Process p = Runtime.getRuntime().exec("rm testingText.txt");
                    BufferedInputStream buffer = new BufferedInputStream(p.getInputStream());
                    DataInputStream commandResult = new DataInputStream(buffer);
                    String s = null;
                    try {
                        while ((s = commandResult.readLine()) != null) {
                            StringTokenizer line = new StringTokenizer(s);
                            while (line.hasMoreTokens()) {
                                LZWResults.addElement(line.nextToken());
                            }
                        }
                        commandResult.close();
                    } catch (Exception e) {
                    }
                } catch (Exception e) {
                }
                try {
                    Process p = Runtime.getRuntime().exec("rm sampleText.txt");
                    BufferedInputStream buffer = new BufferedInputStream(p.getInputStream());
                    DataInputStream commandResult = new DataInputStream(buffer);
                    String s = null;
                    try {
                        while ((s = commandResult.readLine()) != null) {
                            StringTokenizer line = new StringTokenizer(s);
                            while (line.hasMoreTokens()) {
                                LZWResults.addElement(line.nextToken());
                            }
                        }
                        commandResult.close();
                    } catch (Exception e) {
                    }
                } catch (Exception e) {
                }
                String stResult = (String) LZWResults.elementAt(6);
                String stTrimmedResult = stResult.substring(0, (stResult.length() - 1));
                double distance = 0.0;
                currSampleWork.dDistance = Double.valueOf(stTrimmedResult).doubleValue();
                LZWResults.removeAllElements();
            }
            double dHighestPercent = 0.0;
            String stMatchingAuthor = "";
            for (int iCounter4 = 0; iCounter4 < iNumSampleWorks; iCounter4++) {
                Double dTemp = new Double(displayDistance(iCounter4));
                if (dTemp.doubleValue() > dHighestPercent) {
                    dHighestPercent = dTemp.doubleValue();
                    stMatchingAuthor = displayAuthor(iCounter4);
                }
            }
            results.append(stMatchingAuthor + "\n");
        }
    }
}
