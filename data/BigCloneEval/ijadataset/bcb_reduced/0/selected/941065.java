package mediathek.io;

import java.io.File;
import mediathek.daten.DatenPod;
import mediathek.daten.DatenAbo;
import mediathek.daten.Daten;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.event.EventListenerList;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import mediathek.Konstanten;
import mediathek.daten.DatenBlacklist;
import mediathek.filme.DatenFilm;
import mediathek.daten.DatenFilmUpdateServer;
import mediathek.daten.DatenPgruppe;
import mediathek.daten.DatenPodster;
import mediathek.daten.DatenProg;
import mediathek.filme.FilmListener;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

public class IoXmlSchreiben {

    private XMLOutputFactory outFactory;

    private XMLStreamWriter writer;

    private OutputStreamWriter out = null;

    ZipOutputStream zipOutputStream = null;

    BZip2CompressorOutputStream bZip2CompressorOutputStream = null;

    private Daten daten;

    EventListenerList listeners = new EventListenerList();

    public boolean stop = false;

    public IoXmlSchreiben(Daten d) {
        daten = d;
        stop = false;
    }

    public void addAdListener(FilmListener listener) {
        listeners.add(FilmListener.class, listener);
    }

    public synchronized void datenSchreiben() {
        stop = false;
        xmlDatenSchreiben();
        if (!daten.noGui) {
            xmlFilmeSchreiben();
            xmlPodcastlisteSchreiben();
            daten.history.speichern();
        }
    }

    public synchronized void filmeSchreiben() {
        stop = false;
        xmlFilmeSchreiben();
    }

    public synchronized void exportFilme(String datei) {
        stop = false;
        try {
            daten.fehler.systemMeldung("Filme exportieren nach: " + datei);
            xmlSchreibenStart(datei);
            xmlSchreibenFilmliste();
            xmlSchreibenFilm();
            xmlSchreibenEnde(datei);
        } catch (Exception ex) {
            daten.fehler.fehlerMeldung(ex, "Filme exportieren nach: " + datei);
        }
    }

    public synchronized void exportPgruppe(DatenPgruppe pGruppe, String datei) {
        stop = false;
        try {
            daten.fehler.systemMeldung("Programmgruppe exportieren nach: " + datei);
            xmlSchreibenStart(datei);
            xmlSchreibenPGruppe(pGruppe);
            xmlSchreibenEnde(datei);
        } catch (Exception ex) {
            daten.fehler.fehlerMeldung(ex, "Programmgruppe exportieren nach: " + datei);
        }
    }

    private void xmlDatenSchreiben() {
        try {
            daten.fehler.systemMeldung("Daten schreiben!");
            xmlSchreibenStart(daten.getBasisVerzeichnis(true) + Konstanten.XML_DATEI);
            xmlSchreibenDaten(Konstanten.SYSTEM, Konstanten.SYSTEM_COLUMN_NAMES, daten.system);
            xmlSchreibenDaten(Konstanten.SENDERLISTE, daten.filmeLaden.getListeSender(), daten.filmeLaden.getSenderOn());
            xmlSchreibenProg();
            xmlSchreibenAbo();
            xmlSchreibenBlackList();
            xmlSchreibenPod();
            xmlSchreibenFilmUpdateServer();
            xmlSchreibenEnde();
        } catch (Exception ex) {
            System.err.println("IoXml.xmlDatenSchreiben: " + ex.getMessage());
        }
    }

    private void xmlFilmeSchreiben() {
        try {
            daten.fehler.systemMeldung("Filme schreiben!");
            xmlSchreibenStart(daten.getBasisVerzeichnis(true) + Konstanten.XML_DATEI_FILME);
            xmlSchreibenFilmliste();
            xmlSchreibenFilm();
            xmlSchreibenEnde();
        } catch (Exception ex) {
            daten.fehler.fehlerMeldung(ex, "IoXml.xmlFilmeSchreiben");
        }
    }

    private void xmlPodcastlisteSchreiben() {
        try {
            daten.fehler.systemMeldung("Podcastliste schreiben!");
            xmlSchreibenStart(daten.getBasisVerzeichnis(true) + Konstanten.XML_DATEI_PODCASTER);
            xmlSchreibenPodcastliste();
            xmlSchreibenEnde();
        } catch (Exception ex) {
            System.err.println("IoXml.xmlPodcastlisteSchreiben: " + ex.getMessage());
        }
    }

    private void xmlSchreibenStart(String datei) throws Exception {
        File file = new File(datei);
        System.out.println("Datei schreiben: " + file.getAbsolutePath());
        outFactory = XMLOutputFactory.newInstance();
        if (datei.endsWith(Konstanten.FORMAT_BZ2)) {
            bZip2CompressorOutputStream = new BZip2CompressorOutputStream(new FileOutputStream(file), 2);
            out = new OutputStreamWriter(bZip2CompressorOutputStream, Konstanten.KODIERUNG_UTF);
        } else if (datei.endsWith(Konstanten.FORMAT_ZIP)) {
            zipOutputStream = new ZipOutputStream(new FileOutputStream(file));
            ZipEntry entry = new ZipEntry(Konstanten.XML_DATEI_FILME);
            zipOutputStream.putNextEntry(entry);
            out = new OutputStreamWriter(zipOutputStream, Konstanten.KODIERUNG_UTF);
        } else {
            out = new OutputStreamWriter(new FileOutputStream(file), Konstanten.KODIERUNG_UTF);
        }
        writer = outFactory.createXMLStreamWriter(out);
        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeCharacters("\n");
        writer.writeStartElement(Konstanten.XML_START);
        writer.writeCharacters("\n");
    }

    private void xmlSchreibenFilmliste() {
        xmlSchreibenDaten(Konstanten.FILMLISTE, Konstanten.FILMLISTE_COLUMN_NAMES, daten.filmeLaden.filmlisteMetaDaten);
    }

    private void xmlSchreibenPodcastliste() {
        ListIterator<DatenPodster> iterator;
        DatenPodster datenPodster;
        iterator = daten.listePodster.listIterator();
        while (iterator.hasNext()) {
            datenPodster = iterator.next();
            xmlSchreibenDaten(Konstanten.PODSTER, Konstanten.PODSTER_COLUMN_NAMES, datenPodster.arr);
        }
    }

    private void xmlSchreibenFilm() {
        ListIterator<DatenFilm> iterator;
        DatenFilm datenFilm;
        iterator = daten.filmeLaden.listeFilmeSchattenliste.listIterator();
        while (iterator.hasNext()) {
            datenFilm = iterator.next();
            xmlSchreibenDaten(Konstanten.FILME, Konstanten.FILME_COLUMN_NAMES, datenFilm.getClean().arr);
        }
    }

    private void xmlSchreibenProg() {
        ListIterator<DatenPgruppe> iterator;
        DatenPgruppe datenPgruppe;
        iterator = daten.listePgruppeAbo.listIterator();
        ListIterator<DatenProg> it;
        while (iterator.hasNext()) {
            datenPgruppe = iterator.next();
            xmlSchreibenDaten(Konstanten.PROGRAMMGRUPPE_ABO, Konstanten.PROGRAMMGRUPPE_COLUMN_NAMES, datenPgruppe.arr);
            it = datenPgruppe.getListeProg().listIterator();
            while (it.hasNext()) {
                xmlSchreibenDaten(Konstanten.PROGRAMM, Konstanten.PROGRAMM_COLUMN_NAMES, it.next().arr);
            }
        }
        iterator = daten.listePgruppeButton.listIterator();
        while (iterator.hasNext()) {
            datenPgruppe = iterator.next();
            xmlSchreibenDaten(Konstanten.PROGRAMMGRUPPE_BUTTON, Konstanten.PROGRAMMGRUPPE_COLUMN_NAMES, datenPgruppe.arr);
            it = datenPgruppe.getListeProg().listIterator();
            while (it.hasNext()) {
                xmlSchreibenDaten(Konstanten.PROGRAMM, Konstanten.PROGRAMM_COLUMN_NAMES, it.next().arr);
            }
        }
    }

    private void xmlSchreibenPGruppe(DatenPgruppe datenPgruppe) {
        ListIterator<DatenProg> it;
        xmlSchreibenDaten(Konstanten.PROGRAMMGRUPPE_BUTTON, Konstanten.PROGRAMMGRUPPE_COLUMN_NAMES, datenPgruppe.arr);
        it = datenPgruppe.getListeProg().listIterator();
        while (it.hasNext()) {
            xmlSchreibenDaten(Konstanten.PROGRAMM, Konstanten.PROGRAMM_COLUMN_NAMES, it.next().arr);
        }
    }

    private void xmlSchreibenAbo() {
        ListIterator<DatenAbo> iterator;
        DatenAbo datenAbo;
        iterator = daten.listeAbo.listIterator();
        while (iterator.hasNext()) {
            datenAbo = iterator.next();
            xmlSchreibenDaten(Konstanten.ABO, Konstanten.ABO_COLUMN_NAMES, datenAbo.arr);
        }
    }

    private void xmlSchreibenBlackList() {
        Iterator<DatenBlacklist> it = daten.listeBlacklist.iterator();
        DatenBlacklist blacklist;
        while (it.hasNext()) {
            blacklist = it.next();
            xmlSchreibenDaten(Konstanten.BLACKLIST, Konstanten.BLACKLIST_COLUMN_NAMES, blacklist.arr);
        }
    }

    private void xmlSchreibenPod() {
        ListIterator<DatenPod> iterator;
        DatenPod datenPod;
        iterator = daten.listePod.listIterator();
        while (iterator.hasNext()) {
            datenPod = iterator.next();
            xmlSchreibenDaten(Konstanten.POD, Konstanten.POD_COLUMN_NAMES, datenPod.arr);
        }
    }

    private void xmlSchreibenFilmUpdateServer() {
        ListIterator<DatenFilmUpdateServer> iterator;
        DatenFilmUpdateServer datenFilmUpdate;
        iterator = daten.filmUpdateServer.serverSchattenliste.listIterator();
        while (iterator.hasNext()) {
            datenFilmUpdate = iterator.next();
            xmlSchreibenDaten(Konstanten.FILM_UPDATE_SERVER, Konstanten.FILM_UPDATE_SERVER_COLUMN_NAMES, datenFilmUpdate.arr);
        }
    }

    private void xmlSchreibenDaten(String xmlName, String[] xmlSpalten, String[] datenArray) {
        int xmlMax = datenArray.length;
        try {
            writer.writeStartElement(xmlName);
            for (int i = 0; i < xmlMax; ++i) {
                if (!datenArray[i].equals("")) {
                    writer.writeStartElement(xmlSpalten[i]);
                    writer.writeCharacters(datenArray[i]);
                    writer.writeEndElement();
                }
            }
            writer.writeEndElement();
            writer.writeCharacters("\n");
        } catch (Exception ex) {
            daten.fehler.fehlerMeldung(ex, "IoXml.xmlSchreibenDaten");
        }
    }

    private void xmlSchreibenEnde() throws Exception {
        xmlSchreibenEnde("");
    }

    private void xmlSchreibenEnde(String datei) throws Exception {
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.flush();
        if (datei.endsWith(Konstanten.FORMAT_BZ2)) {
            writer.close();
            bZip2CompressorOutputStream.close();
        } else if (datei.endsWith(Konstanten.FORMAT_ZIP)) {
            zipOutputStream.closeEntry();
            writer.close();
            zipOutputStream.close();
        } else {
            writer.close();
        }
        daten.fehler.systemMeldung("geschrieben!");
    }
}
