package CORE;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 *
 * @author Olivier Combe
 */
public class FindSub extends Fichier {

    public static ArrayList<CORE.SousTitre> findSub(Episode ep) {
        ArrayList<SousTitre> zipFilesUrls = getOldSub(ep);
        ArrayList<CORE.SousTitre> possibleSubs = new ArrayList<CORE.SousTitre>();
        for (int i = 0; i < zipFilesUrls.size(); i++) {
            possibleSubs.addAll(getSub(zipFilesUrls.get(i), i + ".zip"));
        }
        return possibleSubs;
    }

    public static ArrayList<SousTitre> getOldSub(Episode ep) {
        String url = "http://www.seriessub.com/sous-titres/" + ep.getSerie().replace(" ", "_").replace("'", "_").replace("-", "_").replace(":", "").toLowerCase() + "/saison_" + ep.getSaison() + "/";
        System.out.println(url);
        Parser parser;
        NodeFilter filter;
        NodeList list;
        String[] motsSerie = ep.getSerie().replace("'", "").toLowerCase().split(" ");
        ArrayList<SousTitre> possibleMatch = new ArrayList<SousTitre>();
        filter = new NodeClassFilter(LinkTag.class);
        try {
            parser = new Parser(url);
            list = parser.extractAllNodesThatMatch(filter);
            for (int i = 0; i < list.size(); i++) {
                int indexDebut = 1 + list.elementAt(i).toHtml().indexOf(">");
                int indexFin = indexDebut + list.elementAt(i).toHtml().substring(indexDebut).indexOf("</a>");
                String linkTxt = list.elementAt(i).toHtml().substring(indexDebut, indexFin).toLowerCase();
                if (contains(linkTxt, motsSerie)) {
                    String epNum = list.elementAt(i).toHtml().substring(indexDebut, indexFin);
                    if (ep.getNum(new File(epNum)) == ep.getNum()) {
                        SousTitre s = new SousTitre();
                        int linkDebut = list.elementAt(i).toHtml().indexOf("href=") + 6;
                        int linkFin = list.elementAt(i).toHtml().indexOf(".php") + 4;
                        if (linkFin > linkDebut) {
                            s.setUrl(list.elementAt(i).toHtml().substring(linkDebut, linkFin));
                            possibleMatch.add(s);
                        }
                    }
                }
            }
        } catch (ParserException e) {
            e.printStackTrace();
        }
        return possibleMatch;
    }

    public static File FileDownload(String fileAddress, String localFileName) {
        OutputStream os = null;
        HttpURLConnection URLConn = null;
        InputStream is = null;
        try {
            URL fileUrl = null;
            byte[] buf;
            int ByteRead;
            try {
                fileUrl = new URL(fileAddress);
            } catch (MalformedURLException malformedURLException) {
                fileUrl = new URL(fileAddress);
            }
            os = new BufferedOutputStream(new FileOutputStream(localFileName));
            URLConn = (HttpURLConnection) fileUrl.openConnection();
            is = URLConn.getInputStream();
            buf = new byte[1024];
            while ((ByteRead = is.read(buf)) != -1) {
                os.write(buf, 0, ByteRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new File(localFileName);
    }

    public static ArrayList<SousTitre> getSub(SousTitre sub, String localFileName) {
        ArrayList<SousTitre> subList = new ArrayList<SousTitre>();
        String fileAddress = sub.url;
        ArrayList<String> ziplist = zipExtractedList(FileDownload(fileAddress, localFileName));
        for (int i = 0; i < ziplist.size(); i++) {
            subList.add(new SousTitre(ziplist.get(i), sub.url));
        }
        return subList;
    }

    public static final void copyInputStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }
        in.close();
        out.close();
    }

    public static ArrayList<String> zipExtractedList(File fileName) {
        ArrayList<String> fileNames = new ArrayList<String>();
        try {
            ZipFile zf = new ZipFile(fileName);
            for (Enumeration entries = zf.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                {
                    if (!entry.isDirectory() && entry.getName().contains(".srt")) {
                        fileNames.add(entry.getName());
                    }
                    if (!entry.isDirectory() && entry.getName().contains(".zip")) {
                        copyInputStream(zf.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(entry.getName())));
                        fileNames.addAll(zipExtractedList(new File(entry.getName())));
                    }
                }
            }
            zf.close();
            fileName.delete();
        } catch (IOException ex) {
            Logger.getLogger(FindSub.class.getName()).log(Level.SEVERE, null, ex);
        }
        return fileNames;
    }

    public static void unzip(File f, String filter, String destinationFolder) {
        Enumeration entries;
        ZipFile zipFile;
        ArrayList<File> directories = new ArrayList<File>();
        File extractedFile = null;
        if (filter == null) {
            filter = "";
        }
        try {
            zipFile = new ZipFile(f);
            entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                if (entry.isDirectory()) {
                    System.err.println("Extracting directory: " + entry.getName());
                    File tempFile = new File(entry.getName());
                    tempFile.mkdir();
                    directories.add(tempFile);
                    continue;
                }
                if (entry.getName().equalsIgnoreCase(filter)) {
                    System.err.println("Extracting file: " + entry.getName());
                    extractedFile = new File(entry.getName());
                    copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(entry.getName())));
                }
                if (entry.getName().contains(".zip")) {
                    System.err.println("Extracting file: " + entry.getName());
                    extractedFile = new File(entry.getName());
                    copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(entry.getName())));
                    unzip(extractedFile, filter, destinationFolder);
                }
            }
            zipFile.close();
        } catch (IOException ioe) {
            System.err.println("Unhandled exception:");
            ioe.printStackTrace();
            return;
        }
        f.delete();
        if (!filter.equals("")) {
            if (extractedFile != null && extractedFile.exists()) {
                if (extractedFile.renameTo(new File(destinationFolder + "\\" + extractedFile.getName()))) {
                    for (int i = 0; i < directories.size(); i++) {
                        directories.get(i).delete();
                    }
                }
            }
        }
    }

    public static ArrayList<SousTitre> findDownloadLink(Episode ep) {
        String url = "http://fr.tvsubtitles.net/tvshows.html";
        Parser parser;
        NodeFilter filter;
        NodeList list;
        ArrayList<SousTitre> subs = new ArrayList<SousTitre>();
        filter = new NodeClassFilter(LinkTag.class);
        try {
            parser = new Parser(url);
            list = parser.extractAllNodesThatMatch(filter);
            for (int i = 0; i < list.size(); i++) {
                if (list.elementAt(i).toHtml().contains(ep.getSerie())) {
                    int linkDebut = 1 + list.elementAt(i).toHtml().indexOf("href=") + 5;
                    int linkFin = linkDebut + list.elementAt(i).toHtml().substring(linkDebut).indexOf(".html");
                    url = "http://fr.tvsubtitles.net/" + list.elementAt(i).toHtml().substring(linkDebut, linkFin - 1) + ep.getSaison() + ".html";
                    filter = new TagNameFilter("DIV");
                    parser = new Parser(url);
                    list = parser.extractAllNodesThatMatch(filter);
                    String saisonAndNum;
                    if (ep.getNum() < 10) {
                        saisonAndNum = ep.getSaison() + "x0" + ep.getNum();
                    } else {
                        saisonAndNum = ep.getSaison() + "x" + ep.getNum();
                    }
                    for (int j = 0; j < list.size(); j++) {
                        if (list.elementAt(j).toHtml().contains(saisonAndNum)) {
                            String[] Ligne = list.elementAt(j).toHtml().split("\n");
                            int numLigne = 0;
                            for (int k = 0; k < Ligne.length; k++) {
                                if (Ligne[k].contains(saisonAndNum)) {
                                    numLigne = k + 1;
                                    break;
                                }
                            }
                            linkDebut = 1 + Ligne[numLigne].indexOf("href=") + 5;
                            linkFin = linkDebut + Ligne[numLigne].substring(linkDebut).indexOf(".html") + 5;
                            url = "http://fr.tvsubtitles.net/" + Ligne[numLigne].substring(linkDebut, linkFin);
                            parser = new Parser(url);
                            list = parser.extractAllNodesThatMatch(filter);
                            for (int k = 0; k < list.size(); k++) {
                                if (list.elementAt(k).toHtml().contains("subtitle-")) {
                                    Ligne = list.elementAt(k).toHtml().split("\n");
                                    break;
                                }
                            }
                            for (int k = 0; k < Ligne.length; k++) {
                                if (Ligne[k].contains("Télécharger Français sous-titres") || Ligne[k].contains("Télécharger Anglais sous-titres")) {
                                    linkDebut = 1 + Ligne[k].indexOf("href=") + 5;
                                    linkFin = linkDebut + Ligne[k].substring(linkDebut).indexOf(".html") + 5;
                                    int noteBadDebut = Ligne[k + 1].indexOf("red\">") + 5;
                                    int noteBadFin = linkDebut + Ligne[k + 1].substring(linkDebut).indexOf("</span>/<span style=\"color:green\">");
                                    int noteGoodDebut = Ligne[k + 1].indexOf("green\">") + 7;
                                    int noteGoodFin = linkDebut + Ligne[k + 1].substring(linkDebut).indexOf("</span></span><img src=\"images/flags");
                                    int noteBad = Integer.parseInt(Ligne[k + 1].substring(noteBadDebut, noteBadFin));
                                    int noteGood = Integer.parseInt(Ligne[k + 1].substring(noteGoodDebut, noteGoodFin));
                                    int dateDebut = Ligne[k + 7].indexOf("<nobr>") + 6;
                                    int dateFin = Ligne[k + 7].indexOf("</nobr>") - 3;
                                    String date = Ligne[k + 7].substring(dateDebut, dateFin).replace(".", "/");
                                    String infos = "Note: " + (noteGood - noteBad) + ", date: " + date + " ";
                                    Parser parser2 = new Parser("http://www.tvsubtitles.net/" + Ligne[k].substring(linkDebut, linkFin));
                                    list = parser2.extractAllNodesThatMatch(filter);
                                    String[] Ligne2 = null;
                                    for (int m = 0; m < list.size(); m++) {
                                        if (list.elementAt(m).toHtml().contains("images/down.png")) {
                                            Ligne2 = list.elementAt(m).toHtml().split("\n");
                                            break;
                                        }
                                    }
                                    String nom = "";
                                    for (int m = 0; m < Ligne2.length; m++) {
                                        if (Ligne2[m].contains("images/save.png")) {
                                            int nomDebut = Ligne2[m].indexOf("70%>") + 4;
                                            int nomFin = Ligne2[m].indexOf("</td></tr>");
                                            nom = Ligne2[m].substring(nomDebut, nomFin);
                                        }
                                        if (Ligne2[m].contains("images/down.png")) {
                                            linkDebut = 1 + Ligne2[m].indexOf("href=") + 5;
                                            linkFin = linkDebut + Ligne2[m].substring(linkDebut).indexOf(".html") + 5;
                                            url = "http://www.tvsubtitles.net/" + Ligne2[m].substring(linkDebut, linkFin);
                                        }
                                    }
                                    subs.add(new SousTitre(nom, url, infos));
                                }
                            }
                            break;
                        }
                    }
                }
            }
        } catch (ParserException e) {
            e.printStackTrace();
        }
        return subs;
    }

    /**
     * Recup l'url d'une page de http://www.tvsubtitles.net/
     * @param url
     * @return
     */
    public static String getDownloadLink(String url) {
        Parser parser;
        NodeFilter filter;
        NodeList list;
        filter = new NodeClassFilter(LinkTag.class);
        try {
            parser = new Parser(url);
            list = parser.extractAllNodesThatMatch(filter);
            for (int i = 0; i < list.size(); i++) {
                if (list.elementAt(i).toHtml().contains("images/down.png")) {
                    int linkDebut = 1 + list.elementAt(i).toHtml().indexOf("href=") + 5;
                    int linkFin = linkDebut + list.elementAt(i).toHtml().substring(linkDebut).indexOf(".html") + 5;
                    url = list.elementAt(i).toHtml().substring(linkDebut, linkFin);
                    return "http://www.tvsubtitles.net/" + url;
                }
            }
        } catch (ParserException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean contains(String linkTxt, String[] motsSerie) {
        boolean ok = true;
        for (int i = 0; i < motsSerie.length; i++) {
            if (!linkTxt.contains(motsSerie[i])) {
                ok = false;
            }
        }
        return ok;
    }
}
