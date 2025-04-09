package mediathek.filme.sender;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipInputStream;
import mediathek.Konstanten;
import mediathek.daten.Daten;
import mediathek.filme.DatenFilm;

public class MediathekBr extends MediathekReader implements Runnable {

    public MediathekBr(Daten ddaten) {
        super(ddaten);
        sender = Konstanten.SENDER_BR;
        text = "BR  (ca. 3 MB, 100 Filme)";
    }

    private class ThemaLaden implements Runnable {

        @Override
        public synchronized void run() {
            notifyStart(1);
            addThread();
            try {
                laden();
            } catch (Exception ex) {
                System.err.println("MediathekBr.ThemaLaden.run: " + ex.getMessage());
            }
            threadUndFertig();
        }
    }

    @Override
    void addToList() {
        new Thread(new MediathekBr.ThemaLaden()).start();
    }

    void laden() {
        StringBuilder seite = new StringBuilder();
        int pos = 0;
        int posEnde = 0;
        int pos1 = 0;
        int pos2 = 0;
        String url = "";
        String thema = "";
        String link = "";
        String datum = "";
        String zeit = "";
        String titel = "";
        String tmp = "";
        final String ITEM_1 = "<ausstrahlung";
        final String ITEM_2 = "</ausstrahlung>";
        final String MUSTER_URL = "<video ";
        final String MUSTER_THEMA = "<titel>";
        final String MUSTER_TITEL = "<nebentitel>";
        final String MUSTER_DATUM = "<beginnPlan>";
        final String ADRESSE = "http://rd.gl-systemhaus.de/br/b7/nc/archive/archive.xml.zip.adler32";
        this.notifyProgress(ADRESSE);
        try {
            String user_agent = daten.system[Konstanten.SYSTEM_USER_AGENT_NR];
            InputStreamReader inReader = null;
            int timeout = 30000;
            char[] zeichen = new char[1];
            URLConnection conn = new URL(ADRESSE).openConnection();
            conn.setRequestProperty("User-Agent", user_agent);
            conn.setReadTimeout(timeout);
            conn.setConnectTimeout(timeout);
            ZipInputStream zipInputStream = new ZipInputStream(conn.getInputStream());
            zipInputStream.getNextEntry();
            inReader = new InputStreamReader(zipInputStream, Konstanten.KODIERUNG_UTF);
            seite.setLength(0);
            while (!stop && inReader.read(zeichen) != -1) {
                seite.append(zeichen);
            }
            while ((pos = seite.indexOf(ITEM_1, pos)) != -1) {
                pos += ITEM_1.length();
                if ((posEnde = seite.indexOf(ITEM_2, pos)) == -1) {
                    break;
                }
                url = "";
                thema = "";
                link = "";
                datum = "";
                zeit = "";
                titel = "";
                tmp = "";
                pos1 = pos;
                while (true) {
                    pos1 = seite.indexOf(MUSTER_URL, pos1);
                    if (pos1 == -1) {
                        break;
                    } else {
                        pos1 += MUSTER_URL.length();
                        if ((pos2 = seite.indexOf("/>", pos1)) != -1) {
                            if (pos1 > posEnde || pos2 > posEnde) {
                                break;
                            }
                            url = seite.substring(pos1, pos2);
                            if (url.contains("xlarge")) {
                                break;
                            }
                        }
                    }
                }
                if (url.equals("")) {
                } else {
                    if ((pos1 = seite.indexOf(MUSTER_THEMA, pos)) != -1) {
                        pos1 += MUSTER_THEMA.length();
                        if ((pos2 = seite.indexOf("</", pos1)) != -1) {
                            if (pos1 < posEnde && pos2 < posEnde) {
                                thema = seite.substring(pos1, pos2);
                                thema = thema.replace("<!", "");
                                thema = thema.replace("[", "");
                                thema = thema.replace("CDATA", "");
                                thema = thema.replace("]", "");
                                thema = thema.replace(">", "");
                            }
                        }
                    }
                    if ((pos1 = seite.indexOf(MUSTER_TITEL, pos)) != -1) {
                        pos1 += MUSTER_TITEL.length();
                        if ((pos2 = seite.indexOf("</", pos1)) != -1) {
                            if (pos1 < posEnde && pos2 < posEnde) {
                                titel = seite.substring(pos1, pos2);
                                titel = titel.replace("<!", "");
                                titel = titel.replace("[", "");
                                titel = titel.replace("CDATA", "");
                                titel = titel.replace("]", "");
                                titel = titel.replace(">", "");
                            }
                        }
                    }
                    if (titel.equals("")) {
                        titel = thema;
                    }
                    if ((pos1 = seite.indexOf(MUSTER_DATUM, pos)) != -1) {
                        pos1 += MUSTER_DATUM.length();
                        if ((pos2 = seite.indexOf("<", pos1)) != -1) {
                            if (pos1 < posEnde && pos2 < posEnde) {
                                tmp = seite.substring(pos1, pos2);
                                datum = convertDatum(tmp);
                                zeit = convertTime(tmp);
                            }
                        }
                    }
                    int p = 0;
                    String host = "";
                    String app = "";
                    String play = "";
                    if ((p = url.indexOf("host=\"")) != -1) {
                        p += "host=\"".length();
                        host = url.substring(p, url.indexOf("\"", p));
                    }
                    p = 0;
                    if ((p = url.indexOf("application=\"")) != -1) {
                        p += "application=\"".length();
                        app = url.substring(p, url.indexOf("\"", p));
                    }
                    p = 0;
                    if ((p = url.indexOf("stream=\"")) != -1) {
                        p += "stream=\"".length();
                        play = url.substring(p, url.indexOf("\"", p));
                    }
                    String urlOrg = "rtmp://" + host + "/" + app + "/" + play;
                    DatenFilm film = new DatenFilm(daten, sender, thema, link, titel, urlOrg, datum, zeit);
                    daten.filmeLaden.listeFilmeSchattenliste.addSenderRtmp(film);
                }
            }
        } catch (Exception ex) {
            daten.fehler.fehlerMeldung(ex, "MediathekBr.addToList");
        }
    }

    public String convertDatum(String datum) {
        try {
            SimpleDateFormat sdfIn = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date filmDate = sdfIn.parse(datum);
            SimpleDateFormat sdfOut;
            sdfOut = new SimpleDateFormat("dd.MM.yyyy");
            datum = sdfOut.format(filmDate);
        } catch (Exception ex) {
            daten.fehler.fehlerMeldung(ex, "MediathekBr.convertDatum");
        }
        return datum;
    }

    public String convertTime(String datum) {
        try {
            SimpleDateFormat sdfIn = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date filmDate = sdfIn.parse(datum);
            SimpleDateFormat sdfOut;
            sdfOut = new SimpleDateFormat("HH:mm:ss");
            datum = sdfOut.format(filmDate);
        } catch (Exception ex) {
            daten.fehler.fehlerMeldung(ex, "MediatheBr.convertDatum");
        }
        return datum;
    }
}
