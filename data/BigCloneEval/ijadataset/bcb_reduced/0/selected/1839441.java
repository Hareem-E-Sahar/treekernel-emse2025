package tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import modele.modelesdelangage.MapModeleArpa;
import modele.modelesdelangage.ModeleArpa;

public class TestLectureRapide {

    public static int tailleCache = 1000;

    public static int indexCache = 0;

    public static ArrayList<String> cache = new ArrayList<String>(tailleCache);

    public static int index2Gram;

    public static int index3Gram;

    public static int lineCount(String fileName) {
        int lineCount = -1;
        try {
            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);
            boolean gram1 = false;
            boolean gram2 = false;
            boolean gram3 = false;
            String line = new String("");
            while ((line = br.readLine()) != null) {
                if (!gram1 && line.equals("\\1-grams:")) gram1 = true; else if (!gram2 && line.equals("\\2-grams:")) {
                    gram2 = true;
                    lineCount--;
                    continue;
                } else if (!gram3 && line.equals("\\3-grams:")) {
                    gram3 = true;
                    lineCount--;
                    continue;
                }
                if (gram1) lineCount++;
            }
            lineCount -= 3;
            br.close();
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lineCount;
    }

    public static void ajouterAuCache(String elt) {
        cache.set(indexCache % tailleCache, elt);
        indexCache = (indexCache + 1) % tailleCache;
    }

    public static String[][] mapper(String fileName, int elementCount) {
        int indexesCount = 10000;
        String[][] indexes = new String[indexesCount][5];
        int gap = elementCount / indexesCount;
        int index = 0;
        int lineCount = -1;
        int oldIndex = 0;
        long offset = 0;
        try {
            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);
            boolean gram1 = false;
            boolean gram2 = false;
            boolean gram3 = false;
            String line = new String("");
            while ((line = br.readLine()) != null) {
                offset += line.length() + 1;
                if (!gram1 && line.equals("\\1-grams:")) {
                    gram1 = true;
                    continue;
                } else if (!gram2 && line.equals("\\2-grams:")) {
                    gram2 = true;
                    index2Gram = index;
                    lineCount--;
                    continue;
                } else if (!gram3 && line.equals("\\3-grams:")) {
                    gram3 = true;
                    index3Gram = index;
                    lineCount--;
                    continue;
                }
                if (gram1 && oldIndex != lineCount && lineCount % gap == 0) {
                    indexes[index][0] = line;
                    indexes[index][1] = String.valueOf(offset - line.length());
                    String[] tmpSplit = line.split("\t");
                    indexes[index][2] = tmpSplit[1];
                    if (tmpSplit.length > 3) {
                        indexes[index][3] = tmpSplit[2];
                        if (tmpSplit[3].charAt(0) == '-' || Character.isDigit(tmpSplit[3].charAt(0))) indexes[index][4] = null; else indexes[index][4] = tmpSplit[3];
                    } else indexes[index][3] = null;
                    indexes[index][4] = tmpSplit.length > 3 ? tmpSplit[3] : null;
                    index++;
                }
                if (gram1) lineCount++;
            }
            lineCount -= 3;
            br.close();
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return indexes;
    }

    public static int rechercheDichotomique(String[][] indexes, String[] mots, int milieu, int debut, int fin, int niveau) {
        if (fin - debut == 1) {
            if (indexes[milieu][2 + niveau].compareTo(mots[niveau]) < 0) return milieu; else return indiceMotPrecedent(indexes, mots[niveau], milieu, niveau);
        }
        if (indexes[milieu][2 + niveau].compareTo(mots[niveau]) > 0) {
            fin = milieu;
            milieu = (debut + fin) / 2;
            return rechercheDichotomique(indexes, mots, milieu, debut, fin, niveau);
        } else if (indexes[milieu][2 + niveau].compareTo(mots[niveau]) < 0) {
            debut = milieu;
            milieu = (debut + fin) / 2;
            return rechercheDichotomique(indexes, mots, milieu, debut, fin, niveau);
        } else if (niveau + 1 < mots.length) {
            int[] intervalle = getIntervalleMot(indexes, milieu, niveau);
            niveau++;
            return rechercheDichotomique(indexes, mots, (intervalle[0] + intervalle[1]) / 2, intervalle[0], intervalle[1], niveau);
        } else {
            return milieu;
        }
    }

    public static int indiceMotPrecedent(String[][] tb, String motRef, int indice, int niveau) {
        int i = indice;
        for (; tb[i][2 + niveau].compareTo(motRef) > 0; i--) ;
        return indice;
    }

    public static int[] getIntervalleMot(String[][] tab, int indice, int niveau) {
        String ref = tab[indice][2 + niveau];
        int[] res = new int[2];
        int i = indice;
        for (; tab[i][2 + niveau].equals(ref); i--) ;
        res[0] = i;
        i = indice;
        for (; tab[i][2 + niveau].equals(ref); i++) ;
        res[1] = i;
        return res;
    }

    public static void afficher(String[][] tb) {
        for (int i = 0; i < tb.length; i++) System.out.println(tb[i][2] + " >>> " + tb[i][0] + " : " + tb[i][1]);
    }

    public static long afficherALaPosition(String fichier, long offset) {
        long deb = System.currentTimeMillis();
        try {
            RandomAccessFile raf = new RandomAccessFile(new File(fichier), "r");
            raf.seek(offset);
            System.out.println(raf.readLine());
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long fin = System.currentTimeMillis();
        return fin - deb;
    }

    public static void testMapping() {
        String fichier = "data/ML_3-gram_JEP-2002-2004-2008.arpa";
        MapModeleArpa mma = new MapModeleArpa(fichier, 2000);
        System.out.println("Mapping ...");
        mma.mapper();
        System.out.println("Fin mapping");
        System.out.println("Sauvegarde...");
        try {
            FileOutputStream f = new FileOutputStream("testsave.map");
            ObjectOutputStream o = new ObjectOutputStream(f);
            o.writeObject(mma);
            o.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("Fin sauvegarde");
        System.out.println("Restauration...");
        try {
            FileInputStream f = new FileInputStream("testsave.map");
            ObjectInputStream o = new ObjectInputStream(f);
            MapModeleArpa mma2 = (MapModeleArpa) o.readObject();
            o.close();
            String[] tb = { "en", "passant", "par" };
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        System.out.println("Fin restauration");
    }

    public static void testProbabilite() {
        long fin = 0;
        System.out.println("Creation du modele...");
        long deb = System.currentTimeMillis();
        ModeleArpa modele = new ModeleArpa("data/ML_3-gram_JEP-2002-2004-2008.arpa", "categorie");
        fin = System.currentTimeMillis();
        System.out.println("Execute en : " + (fin - deb) + "ms");
        System.out.println("Recherche de 'apprentissage'...");
        deb = fin;
        System.out.println(modele.getProbabilite("apprentissage"));
        fin = System.currentTimeMillis();
        System.out.println("Execute en : " + (fin - deb) + "ms");
        System.out.println("Recherche de 'en apprentissage des'...");
        deb = fin;
        System.out.println(modele.getProbabilite("des", "apprentissage", "en"));
        fin = System.currentTimeMillis();
        System.out.println("Execute en : " + (fin - deb) + "ms");
        System.out.println("Recherche de 'omagad oblige caexistepas'...");
        deb = fin;
        System.out.println(modele.getProbabilite("caexistepas", "oblige", "omagad"));
        fin = System.currentTimeMillis();
        System.out.println("Execute en : " + (fin - deb) + "ms");
        System.out.println("Recherche de 'zoo'...");
        deb = fin;
        System.out.println(modele.getProbabilite("zoo"));
        fin = System.currentTimeMillis();
        System.out.println("Execute en : " + (fin - deb) + "ms");
        try {
            Thread.sleep(25000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        testProbabilite();
    }
}
