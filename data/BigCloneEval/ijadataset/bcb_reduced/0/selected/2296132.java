package modele.modelesdelangage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;

public class MapModeleArpa implements Serializable {

    private static final long serialVersionUID = 6L;

    private String[][] indexes;

    private int nbEntrees;

    private String nomFichier;

    private int nbNGram;

    private int tailleEnTete;

    private int index2Gram = 0;

    private int index3Gram = 0;

    public MapModeleArpa(String nomFichier, int nbEntrees) {
        indexes = new String[nbEntrees][5];
        this.nbEntrees = nbEntrees;
        this.nomFichier = nomFichier;
        nbNGram = elementCount();
        tailleEnTete = 2;
    }

    public void mapper() {
        int gap = nbNGram / nbEntrees;
        int index = 0;
        int lineCount = -1;
        int oldLineCount = 0;
        long offset = 0;
        try {
            FileReader fr = new FileReader(nomFichier);
            BufferedReader br = new BufferedReader(fr);
            boolean gram1 = false;
            boolean gram2 = false;
            boolean gram3 = false;
            boolean firstGram2 = false;
            boolean firstGram3 = false;
            String line = new String("");
            while ((line = br.readLine()) != null && index < nbEntrees) {
                offset += line.length() + 1;
                if (!gram1 && line.equals("\\1-grams:")) {
                    gram1 = true;
                    continue;
                } else if (!gram2 && line.equals("\\2-grams:")) {
                    gram2 = true;
                    firstGram2 = true;
                    this.index2Gram = index;
                    lineCount--;
                    continue;
                } else if (!gram3 && line.equals("\\3-grams:")) {
                    gram3 = true;
                    firstGram2 = true;
                    this.index3Gram = index;
                    lineCount--;
                    continue;
                } else if (line.isEmpty()) continue;
                if ((gram1 && oldLineCount < lineCount && lineCount % gap == 0) || (gram2 && firstGram2) || (gram3 && firstGram3)) {
                    try {
                        indexes[index][0] = line;
                        indexes[index][1] = String.valueOf(offset - line.length() - 1);
                        String[] tmpSplit = line.split("\t");
                        indexes[index][2] = tmpSplit[1];
                        if (tmpSplit.length > 3) {
                            indexes[index][3] = tmpSplit[2];
                            if (tmpSplit[3].charAt(0) == '-' || Character.isDigit(tmpSplit[3].charAt(0))) indexes[index][4] = null; else indexes[index][4] = tmpSplit[3];
                        } else indexes[index][3] = null;
                        oldLineCount = lineCount;
                        index++;
                        firstGram2 = firstGram3 = false;
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        System.out.println("Line = " + line + "\nOldlinecount = " + oldLineCount + "\nGap = " + gap + "\nIndex = " + index + "\nlineCount = " + lineCount + "\ntotal = " + nbNGram);
                        ex.printStackTrace();
                    }
                }
                if (gram1) lineCount++;
            }
            lineCount -= 3;
            br.close();
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int elementCount() {
        int lineCount = 0;
        try {
            FileReader fr = new FileReader(nomFichier);
            BufferedReader br = new BufferedReader(fr);
            String ligne = "";
            while (!(ligne = br.readLine()).equals("\\1-grams:")) {
                if (ligne.contains("ngram")) lineCount += Integer.parseInt(ligne.split("=")[1]);
            }
            br.close();
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lineCount;
    }

    public String[] getPortionRecherche(String[] mots) {
        int position;
        if (mots.length == 1) position = rechercheDichotomique(mots, index2Gram / 2, 0, index2Gram, 0); else if (mots.length == 2) position = rechercheDichotomique(mots, (index2Gram + index3Gram) / 2, index2Gram, index3Gram, 0); else if (mots.length == 3) position = rechercheDichotomique(mots, (index3Gram + this.nbEntrees) / 2, index3Gram, nbEntrees, 0); else return null;
        int offset = Integer.parseInt(indexes[position][1]);
        int offsetSuivant = Integer.parseInt(indexes[position + 1][1]);
        int taille = offsetSuivant - offset;
        byte[] buffer = new byte[taille];
        try {
            RandomAccessFile raf = new RandomAccessFile(this.nomFichier, "r");
            raf.seek(offset);
            raf.read(buffer);
            raf.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        String stringFromBuffer = new String(buffer);
        String[] lignes = stringFromBuffer.split("\\n");
        return lignes;
    }

    private int rechercheDichotomique(String[] mots, int milieu, int debut, int fin, int niveau) {
        if (fin - debut == 1) {
            if (indexes[milieu][tailleEnTete + niveau].compareTo(mots[niveau]) < 0) return milieu; else return indiceMotPrecedent(mots[niveau], milieu, niveau);
        }
        if (indexes[milieu][tailleEnTete + niveau].compareTo(mots[niveau]) > 0) {
            fin = milieu;
            milieu = (debut + fin) / 2;
            return rechercheDichotomique(mots, milieu, debut, fin, niveau);
        } else if (indexes[milieu][tailleEnTete + niveau].compareTo(mots[niveau]) < 0) {
            debut = milieu;
            milieu = (debut + fin) / 2;
            return rechercheDichotomique(mots, milieu, debut, fin, niveau);
        } else if (niveau + 1 < mots.length) {
            int[] intervalle = getIntervalleMot(milieu, niveau);
            niveau++;
            return rechercheDichotomique(mots, (intervalle[0] + intervalle[1]) / 2, intervalle[0], intervalle[1], niveau);
        } else {
            return milieu;
        }
    }

    private int[] getIntervalleMot(int indice, int niveau) {
        String ref = indexes[indice][tailleEnTete + niveau];
        int[] res = new int[2];
        int i = indice;
        for (; indexes[i][tailleEnTete + niveau].equals(ref); i--) ;
        res[0] = i;
        i = indice;
        for (; indexes[i][tailleEnTete + niveau].equals(ref); i++) ;
        res[1] = i;
        return res;
    }

    private int indiceMotPrecedent(String motRef, int indice, int niveau) {
        int i = indice;
        for (; indexes[i][tailleEnTete + niveau].compareTo(motRef) > 0 && i > 0; i--) ;
        return indice;
    }
}
