import java.io.*;
import java.util.*;
import java.net.URL;

public class GenomeFileParser {

    /**
     * This method calculates the pI from inputted sequence
     *
     * @param     pro     protein sequence
     */
    public static double getPI(String pro) {
        double pH = 7;
        double lowpH = 0, highpH = 14;
        int Plength = pro.length();
        double charge = 1;
        char type = 'n';
        double pK = 0;
        while (Math.abs(charge) >= .005) {
            charge = 0;
            for (int a = 0; a < Plength; a++) {
                switch(pro.charAt(a)) {
                    case 'R':
                        type = 'b';
                        pK = 12;
                        break;
                    case 'D':
                        type = 'a';
                        pK = 4.05;
                        break;
                    case 'C':
                        type = 'a';
                        pK = 9;
                        break;
                    case 'E':
                        type = 'a';
                        pK = 4.75;
                        break;
                    case 'H':
                        type = 'b';
                        pK = 5.98;
                        break;
                    case 'K':
                        type = 'b';
                        pK = 10;
                        break;
                    case 'Y':
                        type = 'a';
                        pK = 10;
                        break;
                    default:
                        type = 'n';
                        pK = 0;
                        break;
                }
                if (type == 'a') {
                    charge += -1 / (1 + Math.pow(10, pK - pH));
                }
                if (type == 'b') {
                    charge += 1 / (1 + Math.pow(10, pH - pK));
                }
            }
            charge += -1 / (1 + Math.pow(10, 3.2 - pH));
            charge += 1 / (1 + Math.pow(10, pH - 8.2));
            if (charge > 0.005) {
                lowpH = pH;
                pH = (lowpH + highpH) / 2;
            }
            if (charge < -0.005) {
                highpH = pH;
                pH = (lowpH + highpH) / 2;
            }
        }
        return pH;
    }

    /**
     * This method calculates the molecular weight from inputted sequence
     *
     * @param     pro     protein sequence
     */
    public static double getMW(String pro) {
        int Plength = pro.length();
        double weight = 0;
        for (int f = 0; f < Plength; f++) {
            switch(pro.charAt(f)) {
                case 'A':
                    weight += 71.0938;
                    break;
                case 'R':
                    weight += 156.2022;
                    break;
                case 'N':
                    weight += 114.1188;
                    break;
                case 'D':
                    weight += 115.1036;
                    break;
                case 'C':
                    weight += 103.1538;
                    break;
                case 'Q':
                    weight += 128.1456;
                    break;
                case 'E':
                    weight += 129.1304;
                    break;
                case 'G':
                    weight += 57.067;
                    break;
                case 'H':
                    weight += 137.156;
                    break;
                case 'I':
                    weight += 113.1742;
                    break;
                case 'L':
                    weight += 113.1742;
                    break;
                case 'K':
                    weight += 128.1888;
                    break;
                case 'M':
                    weight += 131.2074;
                    break;
                case 'F':
                    weight += 147.1914;
                    break;
                case 'P':
                    weight += 97.1316;
                    break;
                case 'S':
                    weight += 87.0932;
                    break;
                case 'T':
                    weight += 101.12;
                    break;
                case 'W':
                    weight += 186.228;
                    break;
                case 'Y':
                    weight += 163.1908;
                    break;
                case 'V':
                    weight += 99.1474;
                    break;
                default:
                    weight += 0;
            }
        }
        weight += 18;
        return weight;
    }

    /**
     * This method parses a .pdb file, extracting sequence information and 
     * appropriate descriptor for the sequence.
     * 
     * @param theFile   file to retrieve sequence data from
     * @param electro2D reference to calling applet
     * @param data      user-inputted file data
     */
    public static void pdbParse(String theFile, Electro2D electro2D, String data, int fileNum) {
        boolean anerror = false;
        Vector fileData = new Vector();
        Vector compoundInfo = new Vector();
        Vector sequenceInfo = new Vector();
        Vector keywordInfo = new Vector();
        Vector moleculeTitles = new Vector();
        Vector chainData = new Vector();
        Vector sequences = new Vector();
        Vector sequenceTitles = new Vector();
        Vector functions = new Vector();
        Hashtable aminoConversions = new Hashtable();
        String proteinFunction = "";
        String headerLine = "";
        double maxPi = -1;
        double minPi = -1;
        double maxMW = -1;
        double minMW = -1;
        BufferedReader in = null;
        Vector molecularWeights = new Vector();
        Vector piValues = new Vector();
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String temp = "";
        String tempLabel = "";
        String proteinID = "";
        String chainValue = "";
        String totalChain = "";
        boolean hasMoleculeTag = true;
        boolean foundChain = false;
        boolean noChainData = false;
        boolean hasECnumber = false;
        int foundIndex = 0;
        if (data == null || data.equals("")) {
            try {
                File f = new File("data" + File.separator + theFile);
                in = new BufferedReader(new InputStreamReader(f.toURL().openStream()));
                String temp1;
                while ((temp1 = in.readLine()) != null) {
                    fileData.addElement(temp1);
                }
            } catch (Exception e) {
                MessageFrame error = new MessageFrame();
                error.setMessage("Error reading from file.  Be sure you " + "typed the name correctly.");
                error.show();
                anerror = true;
                System.err.println("Exception was: " + e);
            }
        } else {
            StringTokenizer fileSplitter = new StringTokenizer(data, "\r\n");
            while (fileSplitter.hasMoreTokens()) {
                fileData.addElement(fileSplitter.nextToken());
            }
        }
        if (anerror == false) {
            proteinID = theFile.substring(0, theFile.indexOf("."));
            for (int x = 0; x < fileData.size(); x++) {
                temp = (String) fileData.elementAt(x);
                tempLabel = temp.substring(0, 6);
                if (tempLabel.equals("COMPND")) {
                    compoundInfo.addElement(temp);
                } else if (tempLabel.equals("SEQRES")) {
                    sequenceInfo.addElement(temp);
                } else if (tempLabel.equals("KEYWDS")) {
                    keywordInfo.addElement(temp);
                } else if (tempLabel.equals("HEADER")) {
                    headerLine = temp;
                }
            }
            for (int i = 0; i < compoundInfo.size(); i++) {
                if (((String) compoundInfo.elementAt(i)).indexOf("EC:") != -1) {
                    hasECnumber = true;
                    temp = (String) compoundInfo.elementAt(i);
                    temp = temp.substring(temp.indexOf("EC:") + 4, temp.indexOf(";") + 1);
                    proteinFunction = proteinFunction + temp;
                }
            }
            if (hasECnumber) {
                proteinFunction = "Enzyme " + proteinFunction;
                int index = proteinFunction.indexOf(",");
                if (index != -1) {
                    proteinFunction = proteinFunction.replace(',', ';');
                }
            }
            for (int x = 0; x < compoundInfo.size(); x++) {
                temp = (String) compoundInfo.elementAt(x);
                if (temp.indexOf("MOLECULE:") != -1) {
                    temp = temp.substring(temp.indexOf("MOLECULE:") + 10);
                    temp = temp.trim();
                    moleculeTitles.addElement(temp);
                }
            }
            if (moleculeTitles.size() == 0) {
                totalChain = "";
                hasMoleculeTag = false;
                for (int x = 0; x < compoundInfo.size(); x++) {
                    temp = (String) compoundInfo.elementAt(x);
                    totalChain += ((String) compoundInfo.elementAt(x)).substring(temp.indexOf("COMPND") + 10, temp.indexOf(proteinID));
                }
                totalChain = totalChain.trim();
                for (int x = 0; x < totalChain.length(); x++) {
                    tempLabel = totalChain.substring(x, x + 1);
                    if (tempLabel.equals(" ")) {
                        tempLabel = totalChain.substring(x + 1, x + 2);
                        if (tempLabel.equals(" ")) {
                            totalChain = totalChain.substring(0, x + 1) + totalChain.substring(x + 2);
                            x -= 1;
                        }
                    }
                }
                moleculeTitles.addElement(totalChain);
            } else {
                int counter = 0;
                for (int x = 0; x < compoundInfo.size(); x++) {
                    temp = (String) compoundInfo.elementAt(x);
                    foundIndex = temp.indexOf("CHAIN:");
                    if (foundIndex != -1) {
                        tempLabel = temp.substring(foundIndex + 7, foundIndex + 8);
                        chainData.addElement(tempLabel);
                        sequenceTitles.addElement((String) moleculeTitles.elementAt(counter));
                        while (temp.charAt(foundIndex + 8) == ',') {
                            temp = temp.substring(0, foundIndex + 7) + temp.substring(foundIndex + 10);
                            tempLabel = temp.substring(foundIndex + 7, foundIndex + 8);
                            chainData.addElement(tempLabel);
                            sequenceTitles.addElement((String) moleculeTitles.elementAt(counter));
                        }
                        counter++;
                    }
                }
            }
            temp = (String) sequenceInfo.elementAt(0);
            tempLabel = temp.substring(11, 12);
            if (tempLabel.equals(" ")) {
                noChainData = true;
                temp = "";
                for (int x = 0; x < sequenceInfo.size(); x++) {
                    temp += ((String) sequenceInfo.elementAt(x)).substring(19, 70);
                    temp += " ";
                }
                if (!(temp = temp.trim()).equals("")) {
                    sequences.addElement(temp);
                    sequenceTitles.addElement((String) moleculeTitles.elementAt(0));
                }
            } else if (hasMoleculeTag == false) {
                for (int whichChain = 0; whichChain < 26; whichChain++) {
                    chainValue = alphabet.substring(whichChain, whichChain + 1);
                    totalChain = "";
                    foundChain = false;
                    for (int x = 0; x < sequenceInfo.size(); x++) {
                        temp = (String) sequenceInfo.elementAt(x);
                        tempLabel = temp.substring(11, 12);
                        if (tempLabel.equals(chainValue)) {
                            foundChain = true;
                            totalChain += ((String) sequenceInfo.elementAt(x)).substring(19, 70);
                            totalChain += " ";
                        }
                    }
                    if (foundChain == true) {
                        chainData.addElement(alphabet.substring(whichChain, whichChain + 1));
                        sequenceTitles.addElement((String) moleculeTitles.elementAt(0));
                    }
                    if (!(totalChain = totalChain.trim()).equals("")) {
                        sequences.addElement(totalChain);
                    }
                }
            } else {
                for (int whichChain = 0; whichChain < chainData.size(); whichChain++) {
                    chainValue = (String) chainData.elementAt(whichChain);
                    totalChain = "";
                    for (int x = 0; x < sequenceInfo.size(); x++) {
                        temp = (String) sequenceInfo.elementAt(x);
                        tempLabel = temp.substring(11, 12);
                        if (tempLabel.equals(chainValue)) {
                            totalChain += ((String) sequenceInfo.elementAt(x)).substring(19, 70);
                            totalChain += " ";
                        }
                    }
                    if (!(totalChain = totalChain.trim()).equals("")) {
                        sequences.addElement(totalChain);
                    }
                }
            }
            try {
                File f = new File("./aminoconversiontable.txt");
                in = new BufferedReader(new InputStreamReader(f.toURL().openStream()));
                while ((temp = in.readLine()) != null) {
                    aminoConversions.put(temp.substring(0, 3), temp.substring(4, 5));
                }
            } catch (Exception e) {
                System.err.println("Exception was: " + e);
            }
            for (int x = 0; x < sequences.size(); x++) {
                totalChain = (String) sequences.elementAt(x);
                for (int y = 0; y < totalChain.length() - 2; y++) {
                    if (aminoConversions.containsKey(totalChain.substring(y, y + 3))) {
                        totalChain = totalChain.substring(0, y) + (String) aminoConversions.get(totalChain.substring(y, y + 3)) + totalChain.substring(y + 3);
                    }
                }
                totalChain = totalChain.trim();
                for (int z = 0; z < totalChain.length(); z++) {
                    tempLabel = totalChain.substring(z, z + 1);
                    if (tempLabel.equals(" ")) {
                        totalChain = totalChain.substring(0, z) + totalChain.substring(z + 1);
                        z -= 1;
                    }
                }
                sequences.setElementAt(totalChain, x);
            }
            double mW = 0.0;
            double pI = 0.0;
            String mWstring = "";
            String pIstring = "";
            for (int x = 0; x < sequences.size(); x++) {
                temp = (String) sequences.elementAt(x);
                mW = getMW(temp);
                pI = getPI(temp);
                mWstring = String.valueOf(mW);
                if (mWstring.length() > mWstring.indexOf('.') + 3) {
                    mWstring = mWstring.substring(0, mWstring.indexOf('.') + 3);
                }
                double doubleValue = Double.parseDouble(mWstring);
                if (minMW == -1 || doubleValue <= minMW) {
                    minMW = doubleValue;
                }
                if (maxMW == -1 || doubleValue >= maxMW) {
                    maxMW = doubleValue;
                }
                molecularWeights.addElement(mWstring);
                pIstring = String.valueOf(pI);
                if (pIstring.length() > pIstring.indexOf('.') + 3) {
                    pIstring = pIstring.substring(0, pIstring.indexOf('.') + 3);
                }
                doubleValue = Double.parseDouble(pIstring);
                if (minPi == -1 || doubleValue <= minPi) {
                    minPi = doubleValue;
                }
                if (maxPi == -1 || doubleValue >= maxPi) {
                    maxPi = doubleValue;
                }
                piValues.addElement(pIstring);
            }
            if (hasECnumber) {
                for (int fcn = 0; fcn < sequenceTitles.size(); fcn++) {
                    functions.addElement(proteinFunction);
                }
            } else if (keywordInfo.size() > 0) {
                for (int fcn = 0; fcn < keywordInfo.size(); fcn++) {
                    if (fcn == 0) {
                        temp = ((String) keywordInfo.elementAt(fcn)).substring(10);
                        temp.trim();
                    } else {
                        temp = temp + ((String) keywordInfo.elementAt(fcn)).substring(10);
                        temp.trim();
                    }
                }
                proteinFunction = temp;
                for (int fcn = 0; fcn < sequenceTitles.size(); fcn++) {
                    functions.addElement(proteinFunction);
                }
            } else if (!headerLine.equals("")) {
                headerLine = headerLine.substring(10, 50);
                headerLine.trim();
                for (int fcn = 0; fcn < sequenceTitles.size(); fcn++) {
                    functions.addElement(headerLine);
                }
            } else {
                for (int fcn = 0; fcn < sequenceTitles.size(); fcn++) {
                    functions.addElement(proteinFunction);
                }
            }
            if (fileNum == 1) {
                electro2D.setSequences(sequences);
                electro2D.setSequenceTitles(sequenceTitles);
                electro2D.setMolecularWeights(molecularWeights);
                electro2D.setPiValues(piValues);
                electro2D.setFunctionValues(functions);
                electro2D.setLastFileLoaded(theFile);
                electro2D.setMaxAndMinVals(maxMW, minMW, maxPi, minPi);
            } else if (fileNum == 2) {
                electro2D.setSequences2(sequences);
                electro2D.setSequenceTitles2(sequenceTitles);
                electro2D.setMolecularWeights2(molecularWeights);
                electro2D.setPiValues2(piValues);
                electro2D.setFunctionValues2(functions);
                electro2D.setLastFileLoaded(theFile);
                electro2D.setMaxAndMinVals(maxMW, minMW, maxPi, minPi);
            }
        }
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
        }
        Preprocessor p = new Preprocessor(electro2D);
        p.writeToFile();
    }

    /**
     * This method parses a FASTA file, extracting sequence information and 
     * appropriate descriptor for the sequence.
     */
    public static void fastaParse(String theFile, Electro2D electro2D, String data, int fileNum) {
        boolean anerror = false;
        Vector fileData = new Vector();
        Vector chainData = new Vector();
        Vector sequences = new Vector();
        Vector sequenceTitles = new Vector();
        Vector functions = new Vector();
        double maxMW = -1;
        double minMW = -1;
        double maxPi = -1;
        double minPi = -1;
        double doubleVal;
        BufferedReader in = null;
        Vector molecularWeights = new Vector();
        Vector piValues = new Vector();
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String temp = "";
        String proteinID = "";
        String totalChain = "";
        boolean foundChain = false;
        boolean noChainData = false;
        int foundIndex = 0;
        if (data == null || data.equals("")) {
            try {
                File f = new File("data" + File.separator + theFile);
                in = new BufferedReader(new InputStreamReader(f.toURL().openStream()));
                String temp1;
                while ((temp1 = in.readLine()) != null) {
                    fileData.addElement(temp1);
                }
            } catch (Exception e) {
                MessageFrame error = new MessageFrame();
                error.setMessage("Error reading from file.  Be sure you " + "typed the name correctly.");
                error.show();
                anerror = true;
                System.err.println("Exception was: " + e);
            }
        } else {
            StringTokenizer fileSplitter = new StringTokenizer(data, "\r\n");
            while (fileSplitter.hasMoreTokens()) {
                fileData.addElement(fileSplitter.nextToken());
            }
        }
        if (anerror == false) {
            proteinID = theFile.substring(0, theFile.indexOf("."));
            for (int x = 0; x < fileData.size(); x++) {
                temp = (String) fileData.elementAt(x);
                if (temp.substring(0, 1).equals(">")) {
                    if (x > 0) {
                        sequences.addElement(totalChain);
                    }
                    if (temp.indexOf(":") != -1) {
                        if (temp.indexOf("|") != -1 && temp.indexOf("|") < temp.indexOf(":")) {
                        } else {
                            chainData.addElement(temp.substring(temp.indexOf(":") + 1, temp.indexOf(":") + 2));
                        }
                    }
                    sequenceTitles.addElement(temp.substring(temp.indexOf(">") + 1));
                    functions.addElement(temp.substring(temp.lastIndexOf("|") + 1));
                    totalChain = "";
                } else {
                    totalChain += temp;
                    if (x == fileData.size() - 1) {
                        sequences.addElement(totalChain);
                    }
                }
            }
            double mW = 0.0;
            double pI = 0.0;
            String mWstring = "";
            String pIstring = "";
            for (int x = 0; x < sequences.size(); x++) {
                temp = (String) sequences.elementAt(x);
                mW = getMW(temp);
                pI = getPI(temp);
                mWstring = String.valueOf(mW);
                if (mWstring.length() > mWstring.indexOf('.') + 3) {
                    mWstring = mWstring.substring(0, mWstring.indexOf('.') + 3);
                }
                doubleVal = Double.parseDouble(mWstring);
                if (minMW == -1 || doubleVal <= minMW) {
                    minMW = doubleVal;
                }
                if (maxMW == -1 || doubleVal >= maxMW) {
                    maxMW = doubleVal;
                }
                molecularWeights.addElement(mWstring);
                pIstring = String.valueOf(pI);
                if (pIstring.length() > pIstring.indexOf('.') + 3) {
                    pIstring = pIstring.substring(0, pIstring.indexOf('.') + 3);
                }
                doubleVal = Double.parseDouble(pIstring);
                if (minPi == -1 || doubleVal <= minPi) {
                    minPi = doubleVal;
                }
                if (maxPi == -1 || doubleVal >= maxPi) {
                    maxPi = doubleVal;
                }
                piValues.addElement(pIstring);
            }
            if (fileNum == 1) {
                electro2D.setSequences(sequences);
                electro2D.setSequenceTitles(sequenceTitles);
                electro2D.setMolecularWeights(molecularWeights);
                electro2D.setPiValues(piValues);
                electro2D.setFunctionValues(functions);
                electro2D.setLastFileLoaded(theFile);
                electro2D.setMaxAndMinVals(maxMW, minMW, maxPi, minPi);
            } else if (fileNum == 2) {
                electro2D.setSequences2(sequences);
                electro2D.setSequenceTitles2(sequenceTitles);
                electro2D.setMolecularWeights2(molecularWeights);
                electro2D.setPiValues2(piValues);
                electro2D.setFunctionValues2(functions);
                electro2D.setLastFileLoaded(theFile);
                electro2D.setMaxAndMinVals(maxMW, minMW, maxPi, minPi);
            }
        }
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
        }
        Preprocessor p = new Preprocessor(electro2D);
        p.writeToFile();
    }

    /**
     * This method parses a .gbk file, extracting sequence information and 
     * appropriate descriptor for the sequence.
     *
     * @return  0 on success, 1 on error
     */
    public static int gbkParse(String theFile, Electro2D electro2D, String data, int fileNum) {
        boolean anerror = false;
        String organismID = "";
        String organismTitle = "";
        Vector fileData = new Vector();
        Vector sequences = new Vector();
        Vector sequenceTitles = new Vector();
        Vector functions = new Vector();
        double doubleVal;
        double minMW = -1;
        double maxMW = -1;
        double maxPi = -1;
        double minPi = -1;
        BufferedReader in = null;
        Vector molecularWeights = new Vector();
        Vector piValues = new Vector();
        String temp = "";
        String totalChain = "";
        boolean foundTranslation = false;
        boolean foundGene = false;
        String function = "";
        boolean hadFunctionLine = false;
        boolean hadEnzymeClassNumber = false;
        if (data == null || data.equals("")) {
            try {
                File f = new File("data" + File.separator + theFile);
                in = new BufferedReader(new InputStreamReader((f.toURL()).openStream()));
                String temp1;
                while ((temp1 = in.readLine()) != null) {
                    fileData.addElement(temp1);
                }
            } catch (Exception e) {
                MessageFrame error = new MessageFrame();
                error.setMessage("Error reading from file.  Be sure you " + "typed the name correctly.");
                error.show();
                anerror = true;
                System.err.println("Exception was: " + e);
            }
        } else {
            StringTokenizer fileSplitter = new StringTokenizer(data, "\r\n");
            while (fileSplitter.hasMoreTokens()) {
                fileData.addElement(fileSplitter.nextToken());
            }
        }
        if (anerror == false) {
            organismID = theFile.substring(0, theFile.indexOf("."));
            for (int x = 0; x < fileData.size(); x++) {
                temp = (String) fileData.elementAt(x);
                if (temp.length() >= 10 && temp.substring(0, 10).equals("DEFINITION")) {
                    organismTitle = temp.substring(12);
                }
                if (temp.length() >= 9 && temp.substring(5, 9).equals("gene")) {
                    if (foundTranslation == false && sequenceTitles.size() >= 1) {
                        sequenceTitles.removeElementAt(sequenceTitles.size() - 1);
                    }
                    while (foundGene == false) {
                        if (temp.length() >= 26 && temp.substring(21, 26).equals("/gene")) {
                            foundGene = true;
                        } else {
                            x++;
                            if (x < fileData.size()) {
                                temp = (String) fileData.elementAt(x);
                            } else {
                                return 1;
                            }
                        }
                    }
                    sequenceTitles.addElement(temp.substring(28, temp.lastIndexOf("\"")));
                    foundGene = false;
                    foundTranslation = false;
                }
                if (temp.length() >= 8 && temp.substring(5, 8).equals("CDS")) {
                    while (foundTranslation == false) {
                        if (temp.length() >= 33 && temp.substring(21, 33).equals("/translation")) {
                            foundTranslation = true;
                        } else if (temp.length() >= 33 && temp.substring(21, 31).equals("/EC_number")) {
                            if (function.equals("")) {
                                temp = temp.substring(33, temp.lastIndexOf("\""));
                                function = "Enzyme " + temp + ";";
                            } else {
                                temp = temp.substring(33, temp.lastIndexOf("\""));
                                function = function + " " + temp + ";";
                            }
                            x++;
                            if (x < fileData.size()) {
                                temp = (String) fileData.elementAt(x);
                            } else {
                                return 1;
                            }
                        } else if (temp.length() >= 30 && temp.substring(21, 30).equals("/function")) {
                            hadFunctionLine = true;
                            if (temp.substring(32).lastIndexOf("\"") != -1) {
                                temp = temp.substring(32);
                                function = function + " " + temp.substring(0, temp.lastIndexOf("\"")) + ".";
                            } else {
                                temp = temp.substring(32);
                                function = function + " " + temp;
                                x = x + 1;
                                temp = (String) fileData.elementAt(x);
                                while (temp.lastIndexOf("\"") == -1) {
                                    function = function + " " + temp.substring(21);
                                    x = x + 1;
                                    if (x < fileData.size()) {
                                        temp = (String) fileData.elementAt(x);
                                    } else {
                                        return 1;
                                    }
                                }
                                function = function + temp.substring(21, temp.lastIndexOf("\"")) + ".";
                            }
                            x++;
                            if (x < fileData.size()) {
                                temp = (String) fileData.elementAt(x);
                            } else {
                                return 1;
                            }
                        } else if (temp.length() >= 27 && temp.substring(21, 26).equals("/note")) {
                            if ((function.indexOf("unknown.") != -1) || function.equals("")) {
                                hadFunctionLine = false;
                                function = "";
                            }
                            if (temp.substring(28).lastIndexOf("\"") != -1) {
                                temp = temp.substring(28);
                                function = function + " " + temp.substring(0, temp.lastIndexOf("\"")) + ".";
                            } else {
                                temp = temp.substring(28);
                                function = function + " " + temp;
                                x = x + 1;
                                temp = (String) fileData.elementAt(x);
                                while (temp.lastIndexOf("\"") == -1) {
                                    function = function + " " + temp.substring(21);
                                    x = x + 1;
                                    if (x < fileData.size()) {
                                        temp = (String) fileData.elementAt(x);
                                    } else {
                                        return 1;
                                    }
                                }
                                function = function + " " + temp.substring(21, temp.lastIndexOf("\"")) + ".";
                            }
                            x++;
                            if (x < fileData.size()) {
                                temp = (String) fileData.elementAt(x);
                            } else {
                                return 1;
                            }
                        } else if (temp.length() >= 30 && temp.substring(21, 29).equals("/product")) {
                            if (temp.substring(31).lastIndexOf("\"") != -1) {
                                temp = temp.substring(31);
                                function = function + " " + temp.substring(0, temp.lastIndexOf("\"")) + ".";
                            } else {
                                temp = temp.substring(31);
                                function = function + " " + temp;
                                x = x + 1;
                                temp = (String) fileData.elementAt(x);
                                while (temp.lastIndexOf("\"") == -1) {
                                    function = function + " " + temp.substring(21);
                                    x = x + 1;
                                    if (x < fileData.size()) {
                                        temp = (String) fileData.elementAt(x);
                                    } else {
                                        return 1;
                                    }
                                }
                                function = function + " " + temp.substring(21, temp.lastIndexOf("\"")) + ".";
                            }
                            x++;
                            if (x < fileData.size()) {
                                temp = (String) fileData.elementAt(x);
                            } else {
                                return 1;
                            }
                        } else {
                            x++;
                            if (x < fileData.size()) {
                                temp = (String) fileData.elementAt(x);
                            } else {
                                System.err.println("Error! Protein lacking " + "sequence.");
                                return 1;
                            }
                        }
                    }
                    if (temp.length() >= 35 && temp.substring(35).lastIndexOf("\"") != -1) {
                        temp = temp.substring(35);
                        totalChain += temp.substring(0, temp.lastIndexOf("\""));
                    } else {
                        totalChain += temp.substring(35);
                        x++;
                        temp = (String) fileData.elementAt(x);
                        while (temp.lastIndexOf("\"") == -1) {
                            totalChain += temp.substring(21);
                            x++;
                            if (x < fileData.size()) {
                                temp = (String) fileData.elementAt(x);
                            } else {
                                return 1;
                            }
                        }
                        totalChain += temp.substring(21, temp.lastIndexOf("\""));
                    }
                    sequences.addElement(totalChain);
                    totalChain = "";
                    functions.addElement(function);
                    function = "";
                    hadEnzymeClassNumber = false;
                    hadFunctionLine = false;
                }
            }
            double mW = 0.0;
            double pI = 0.0;
            String mWstring = "";
            String pIstring = "";
            for (int x = 0; x < sequences.size(); x++) {
                temp = (String) sequences.elementAt(x);
                mW = getMW(temp);
                pI = getPI(temp);
                mWstring = String.valueOf(mW);
                if (mWstring.length() > mWstring.indexOf('.') + 3) {
                    mWstring = mWstring.substring(0, mWstring.indexOf('.') + 3);
                }
                doubleVal = Double.parseDouble(mWstring);
                if (minMW == -1 || doubleVal <= minMW) {
                    minMW = doubleVal;
                }
                if (maxMW == -1 || doubleVal >= maxMW) {
                    maxMW = doubleVal;
                }
                molecularWeights.addElement(mWstring);
                pIstring = String.valueOf(pI);
                if (pIstring.length() > pIstring.indexOf('.') + 3) {
                    pIstring = pIstring.substring(0, pIstring.indexOf('.') + 3);
                }
                doubleVal = Double.parseDouble(pIstring);
                if (minPi == -1 || doubleVal <= minPi) {
                    minPi = doubleVal;
                }
                if (maxPi == -1 || doubleVal >= maxPi) {
                    maxPi = doubleVal;
                }
                piValues.addElement(pIstring);
            }
            if (fileNum == 1) {
                electro2D.setSequences(sequences);
                electro2D.setSequenceTitles(sequenceTitles);
                electro2D.setMolecularWeights(molecularWeights);
                electro2D.setPiValues(piValues);
                electro2D.setFunctionValues(functions);
                electro2D.setLastFileLoaded(theFile);
                electro2D.setMaxAndMinVals(maxMW, minMW, maxPi, minPi);
            } else if (fileNum == 2) {
                electro2D.setSequences2(sequences);
                electro2D.setSequenceTitles2(sequenceTitles);
                electro2D.setMolecularWeights2(molecularWeights);
                electro2D.setPiValues2(piValues);
                electro2D.setFunctionValues2(functions);
                electro2D.setLastFileLoaded(theFile);
                electro2D.setMaxAndMinVals(maxMW, minMW, maxPi, minPi);
            }
        }
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
        }
        electro2D.setLastFileLoaded(theFile);
        Preprocessor p = new Preprocessor(electro2D);
        p.writeToFile();
        return 0;
    }

    /**
     * This method parses a .e2d file, extracting sequence information and 
     * appropriate descriptor for the sequence.
     *
     * @return  0 on success, 1 on error
     */
    public static int e2dParse(String theFile, Electro2D electro2D, String data, int fileNum) {
        File f = new File("data" + File.separator + theFile);
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(f.toURL().openStream()));
        } catch (Exception e) {
            System.err.println("Error reading from file.  Double-check " + "file name and try again.");
        }
        Preprocessor.readFromFile(in, electro2D, fileNum);
        return 0;
    }
}
