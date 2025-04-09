package com.cafe.serve.util.file;

import org.apache.log4j.Logger;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import com.cafe.serve.server.Drink;
import com.cafe.serve.server.GetraenkeKarte;
import com.cafe.serve.util.IConstants;
import com.cafe.serve.util.string.StringUtil;

/**
 * @author Raptis Asterios
 * Created first on 13.01.2005 in project ServerThreaded
 */
public class DrinkPropertiesUtil implements IConstants {

    private static Logger logger = Logger.getLogger(DrinkPropertiesUtil.class.getName());

    /**
     * Diese Methode setzt ein Drink-objekt in einer propertie-datei.
     * 
     * @param d
     *            Das Drink-objekt das in eine propertie-datei gespeichert
     *            werden soll.
     */
    public static void setDrinkInProp(Drink d) {
        Properties pr = new Properties();
        pr.setProperty("id." + d.getName(), new Integer(d.getId()).toString());
        pr.setProperty("name." + d.getName(), d.getName());
        pr.setProperty("kategorie." + d.getName(), d.getKategory());
        pr.setProperty("groesse." + d.getName(), d.getGroesse());
        pr.setProperty("preis." + d.getName(), d.getPreis());
        pr.setProperty("mwst." + d.getMehrwertSteuer(), new Integer(d.getMehrwertSteuer()).toString());
        String dateiName = d.getName() + d.getGroesse() + ".drn";
        dateiName = StringUtil.ersetzeZeichenArrayString(dateiName, StringUtil.sonderzeichen, "_");
        System.out.println("setDrinkInProp:" + dateiName);
        try {
            PrintStream pw = new PrintStream(new BufferedOutputStream(new FileOutputStream(dateiName)));
            pr.store(pw, "Properties von Getraenk " + d.getName());
            pw.flush();
            pw.close();
            pw = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Diese Methode packt alle Drink-Objekte in einer zipdatei.
     * 
     * @param zipfile
     *            .Der Name der datei als String.
     */
    public static void packeAlleDrinkObjekte(String zipfile) {
        File f = new File(".");
        try {
            String entries[] = f.list(new DrnFilenameFilter());
            byte[] buf = new byte[BLOCKSIZE];
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipfile));
            for (int i = 0; i < entries.length; ++i) {
                File ff = new File(entries[i]);
                System.out.println("adding " + ff);
                FileReader in = new FileReader(ff);
                out.putNextEntry(new ZipEntry(entries[i]));
                int len;
                while ((len = in.read()) > 0) {
                    out.write(len);
                }
                in.close();
                in = null;
                FileUtil.deleteFile(ff);
            }
            out.close();
            out = null;
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }

    /**
     * Diese Methode entpackt eine Zipdatei wie der Name schon sagt.
     * 
     * @param zipfile
     *            .Der Name der datei die entpackt werden soll als String.
     */
    public static void entpackeZipDatei(String zipfile) {
        try {
            byte[] buf = new byte[BLOCKSIZE];
            ZipInputStream in = new ZipInputStream(new FileInputStream(zipfile));
            while (true) {
                ZipEntry entry = in.getNextEntry();
                if (entry == null) {
                    break;
                }
                FileOutputStream out = new FileOutputStream(entry.getName());
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                out = null;
                in.closeEntry();
            }
            in.close();
            in = null;
            File ff = new File(zipfile);
            FileUtil.deleteFile(ff);
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }

    /**
     * Diese Methode konvertiert die Drink-Propertie-Dateien in Drink-Objekte
     * und speichert Sie in die GetraenkeKarte-Objekt das als argument �bergeben
     * wird.
     * 
     * @param g
     */
    public static GetraenkeKarte convertPropInDrinks(GetraenkeKarte g) {
        File f = new File(".");
        String entries[] = f.list(new DrnFilenameFilter());
        if (entries == null || entries.length < 1) {
            System.out.println("Keine Getraenke gespeichert.");
        } else {
            for (int i = 0; i < entries.length; i++) {
                File entry = new File(entries[i]);
                System.out.println("lesbar: " + entry.canRead());
                String path = f.getAbsolutePath();
                System.out.println("Wir befinden uns im Verzeichnis: " + path);
                if (entry.isDirectory()) {
                    System.out.println(entry.getName() + " ist ein Ordner.");
                } else {
                    System.out.println(entry.getName() + " ist ein Datei.");
                    try {
                        Properties props = new Properties();
                        FileInputStream fis = new FileInputStream(entry);
                        props.load(fis);
                        Drink d = makeDrink(props);
                        g.addDrink(d);
                        fis.close();
                        fis = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return g;
    }

    /**
     * Diese Methode macht aus einer Propertie-Datei ein Drink-Objekt.
     * 
     * @param p .
     *            Die Propertie-Datei aus dem das Drink-Objekt erzeugt werden
     *            soll.
     * @return . Das Drink-Objekt das aus der Propertie-Datei erzeugt wurde.
     */
    public static Drink makeDrink(Properties p) {
        Drink d = new Drink();
        Enumeration e = p.keys();
        while (e.hasMoreElements()) {
            String t = (String) e.nextElement();
            String tmp = null;
            if (t.startsWith("id")) {
                tmp = p.getProperty(t);
                d.setId(Integer.parseInt(tmp));
            } else if (t.startsWith("name")) {
                tmp = p.getProperty(t);
                d.setName(tmp);
            } else if (t.startsWith("kategorie")) {
                tmp = p.getProperty(t);
                d.setKategory(tmp);
            } else if (t.startsWith("groesse")) {
                tmp = p.getProperty(t);
                d.setGroesse(tmp);
            } else if (t.startsWith("preis")) {
                tmp = p.getProperty(t);
                d.setPreis(tmp);
            } else if (t.startsWith("mwst")) {
                tmp = p.getProperty(t);
                d.setMehrwertSteuer(Integer.parseInt(tmp));
            }
        }
        return d;
    }
}
