package it.ilz.hostingjava.util.file;

import java.util.zip.*;
import java.util.*;
import java.io.*;

/**
 *
 * @author andrea
 */
public abstract class Zippa {

    private static final int BUFFER = 2048;

    private static byte data[] = new byte[BUFFER];

    public static void zippaInUnaCartella(String strPathOrigine, String strPathDestinazione, String strNomeFileZip, LinkedList<String> lNomi) throws Exception {
        try {
            File f = new File(strPathOrigine);
            File fList[] = f.listFiles();
            FileOutputStream dest = new FileOutputStream(strPathDestinazione + "/" + strNomeFileZip);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            for (int i = 0; i < fList.length; i++) {
                if (fList[i].isFile()) {
                    if (lNomi.contains(fList[i].getName())) {
                        aggFile(out, strPathOrigine, fList[i].getName());
                    }
                }
            }
            ;
            out.close();
            out = null;
            dest.close();
            dest = null;
        } catch (Exception e) {
            new Exception("\n zippaInUnaCartella  : " + e.getMessage());
        }
    }

    public static void zippaCartella(String strPathOrigine, String strPathDestinazione, String[] tipofiles) throws Exception {
        String strTemp = "";
        int i = 0;
        try {
            File f = new File(strPathOrigine);
            File fList[] = f.listFiles();
            for (i = 0; i < fList.length; i++) {
                if (fList[i].isFile()) {
                    strTemp = fList[i].getName().toLowerCase();
                    if (strTemp.substring(strTemp.length() - 4).indexOf(".zip") == -1) {
                        for (String tipo : tipofiles) {
                            if (tipo.equals(strTemp.substring(strTemp.lastIndexOf(".")))) {
                                FileOutputStream dest = new FileOutputStream(strPathDestinazione + "/" + strTemp.substring(0, strTemp.length() - 4) + ".zip");
                                ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
                                aggFile(out, strPathOrigine, strTemp);
                                out.close();
                                out = null;
                                dest.close();
                                dest = null;
                                break;
                            }
                        }
                    }
                }
            }
            ;
            fList = null;
            f = null;
        } catch (Exception e) {
            new Exception("\n zippaCartella : " + e.getMessage());
        }
    }

    public static void deZippaFile(String strPathOrigine, String strPathDestinazione, String nomefile) throws Exception {
        String strTemp = "";
        int i = 0;
        try {
            int j = 0;
            String tmp = "";
            String nomeentry = "";
            String cartella = "";
            ZipInputStream zipIn = new ZipInputStream(new FileInputStream(strPathOrigine + System.getProperty("file.separator") + nomefile));
            ZipEntry entry = null;
            while ((entry = zipIn.getNextEntry()) != null) {
                nomeentry = entry.getName();
                tmp = nomeentry;
                cartella = strPathDestinazione;
                j = tmp.indexOf(separatore);
                while (j > 0) {
                    cartella = cartella + separatore + tmp.substring(0, tmp.indexOf(separatore));
                    GestioneFile.creaCartella(cartella);
                    tmp = tmp.substring(tmp.indexOf(separatore) + 1);
                    j = tmp.indexOf(separatore);
                }
                if (entry.isDirectory()) {
                    File f = new File(strPathDestinazione + separatore + entry.getName());
                    f.mkdir();
                    f = null;
                } else {
                    int nbyte = 2048;
                    int len = 0;
                    FileOutputStream fout = new FileOutputStream(new File(strPathDestinazione + separatore + entry.getName()));
                    byte by[] = new byte[nbyte];
                    while ((len = zipIn.read(by)) > 0) {
                        fout.write(by, 0, len);
                    }
                    fout.flush();
                    fout.close();
                    fout = null;
                }
                zipIn.closeEntry();
            }
            zipIn.close();
            zipIn = null;
        } catch (Exception e) {
            new Exception("\n deZippaFile : " + e.getMessage());
        }
    }

    private static void aggFile(ZipOutputStream out, String strPathOrigine, String nomefile) throws Exception {
        String separatore = System.getProperty("file.separator");
        try {
            FileInputStream fi = new FileInputStream(strPathOrigine + separatore + nomefile);
            BufferedInputStream origin = new BufferedInputStream(fi, BUFFER);
            ZipEntry entry = new ZipEntry(nomefile);
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            ;
            origin.close();
            origin = null;
            fi.close();
            fi = null;
        } catch (Exception e) {
            new Exception("\n aggFile: " + e.getMessage() + "\n");
        }
    }

    private static void aggFileInString(ZipOutputStream out, String strContenutoFile, String nomefile) throws Exception {
        String separatore = System.getProperty("file.separator");
        try {
            ZipEntry entry = new ZipEntry(nomefile);
            out.putNextEntry(entry);
            int count;
        } catch (Exception e) {
            new Exception("\n aggFileInString: " + e.getMessage() + "\n");
        }
    }

    private static String separatore = System.getProperty("file.separator");
}
