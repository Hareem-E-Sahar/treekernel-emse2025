package splitnass.data;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class Spieltag implements RundeListener, Serializable {

    public void bock(EventObject e) {
        addBock();
    }

    public void boecke(EventObject e) {
        addBoecke(spieler.size());
    }

    public void undoBoecke(EventObject e) {
        for (int i = 0; i < spieler.size(); i++) {
            removeBock();
        }
    }

    public void undoBock(EventObject e) {
        removeBock();
    }

    public void rundeDataChanged(EventObject e) {
        fireRundeUpdatedEvent(aktuelleRunde);
    }

    public void ergebnisBerechnet(EventObject e) {
    }

    private static Logger log = Logger.getLogger("Spieltag");

    private Date start;

    private Date ende;

    private int gesamtRunden = 42;

    private List<Runde> runden;

    private Runde aktuelleRunde;

    private List<Spieler> spieler;

    public static Random randomizer = new Random();

    private transient List<SpieltagListener> listener = new ArrayList<SpieltagListener>();

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        listener = new ArrayList<SpieltagListener>();
        for (Runde r : runden) {
            r.addListener(this);
        }
    }

    public void addListener(SpieltagListener l) {
        listener.add(l);
    }

    public void removeListener(SpieltagListener l) {
        listener.remove(l);
    }

    private void fireRundeUpdatedEvent(Runde r) {
        for (SpieltagListener l : listener) {
            l.rundeUpdated(r);
        }
    }

    private void fireSpieltagUpdatedEvent() {
        for (SpieltagListener l : listener) {
            l.spieltagUpdated();
        }
    }

    public Date getStart() {
        return start;
    }

    private void addBock() {
        Runde r = getNaechsteRunde(aktuelleRunde());
        while (r != null && r.getBoecke() >= 2) {
            r = getNaechsteRunde(r);
        }
        if (r != null) {
            r.addBock();
            fireRundeUpdatedEvent(r);
        }
    }

    private void addBoecke(int count) {
        Runde r = getNaechsteRunde(aktuelleRunde());
        while (r != null && r.getBoecke() >= 2) {
            r = getNaechsteRunde(r);
        }
        if (r != null) {
            for (int i = count; i > 0; i--) {
                r.addBock();
                fireRundeUpdatedEvent(r);
                r = getNaechsteRunde(r);
                if (r == null) {
                    addBoecke(i - 1);
                    break;
                }
            }
        }
    }

    private void removeBock() {
        Runde r = getNaechsteRunde(aktuelleRunde());
        Runde bockRunde = null;
        if (r.getBoecke() == 2) {
            while (r != null && r.getBoecke() == 2) {
                r = getNaechsteRunde(r);
            }
            bockRunde = getVorherigeRunde(r);
        } else if (r.getBoecke() == 1) {
            while (r != null && r.getBoecke() == 1) {
                r = getNaechsteRunde(r);
            }
            bockRunde = getVorherigeRunde(r);
        }
        if (bockRunde != null) {
            bockRunde.removeBock();
            fireRundeUpdatedEvent(bockRunde);
        }
    }

    public boolean isBeendet() {
        return ende != null;
    }

    public boolean isGestartet() {
        return start != null;
    }

    public void setGesamtRunden(int value) {
        if (value <= 0) return;
        if (runden == null) {
            runden = new ArrayList<Runde>(value);
            addRunden(value);
        } else {
            if (gesamtRunden <= value) {
                addRunden(value - gesamtRunden);
            } else {
                for (int i = 0; i < gesamtRunden - value; i++) {
                    Runde r = runden.remove(runden.size() - 1);
                    r.removeListener(this);
                }
            }
        }
        gesamtRunden = value;
        fireSpieltagUpdatedEvent();
    }

    private void addRunden(int count) {
        for (int i = 0; i < count; i++) {
            Runde newRunde = new Runde(i + 1);
            newRunde.addListener(this);
            runden.add(newRunde);
        }
    }

    public void start(List<Spieler> spieler, Spieler geber) {
        start = new Date();
        this.spieler = spieler;
        for (Spieler s : spieler) {
            s.setIsAktiv(true);
        }
        aktuelleRunde = runden.get(0);
        aktuelleRunde.setGeber(geber);
        aktuelleRunde.setSpieler(getSpieler(geber));
        aktuelleRunde.setAufspieler(getNaechstenSpieler(geber));
        aktuelleRunde.start();
    }

    public void ende() {
        ende = new Date();
    }

    public Runde aktuelleRunde() {
        return aktuelleRunde;
    }

    public void rundeAbrechnenUndNeu() {
        berechneSpielstand();
        aktuelleRunde.ende();
        Spieler geber = null;
        if (aktuelleRunde.isSolo() && !aktuelleRunde.getSolo().isRegulaeresAufspiel()) {
            geber = aktuelleRunde.getGeber();
        } else {
            geber = getNaechstenSpieler(aktuelleRunde.getGeber());
        }
        log.info(aktuelleRunde.dump());
        if (aktuelleRunde.getId() < runden.size()) {
            fireRundeUpdatedEvent(aktuelleRunde);
            aktuelleRunde = runden.get(aktuelleRunde.getId());
            aktuelleRunde.setGeber(geber);
            aktuelleRunde.setSpieler(getSpieler(geber));
            aktuelleRunde.setAufspieler(getNaechstenSpieler(geber));
            aktuelleRunde.start();
        }
        fireRundeUpdatedEvent(aktuelleRunde);
    }

    public void undoLetzteRunde() {
        aktuelleRunde.reset();
        Runde vorherigeRunde = getVorherigeRunde(aktuelleRunde);
        if (vorherigeRunde == null) return;
        aktuelleRunde = vorherigeRunde;
        aktuelleRunde.reset();
        fireRundeUpdatedEvent(aktuelleRunde);
        fireRundeUpdatedEvent(getNaechsteRunde(aktuelleRunde));
        fireSpieltagUpdatedEvent();
    }

    private void berechneSpielstand() {
        Set<Spielstand> spielstaende = new HashSet<Spielstand>();
        Runde vorherigeRunde = getVorherigeRunde(aktuelleRunde);
        if (vorherigeRunde != null) {
            for (Spielstand bisherigerStand : vorherigeRunde.getSpielstand()) {
                spielstaende.add((Spielstand) bisherigerStand.clone());
            }
        } else {
            for (Spieler s : spieler) {
                spielstaende.add(new Spielstand(s, 0));
            }
        }
        if (!aktuelleRunde.isGespaltenerArsch()) {
            int punkte = aktuelleRunde.ergebnis();
            for (Spieler gewinner : aktuelleRunde.getGewinner()) {
                Spielstand stand = Spielstand.find(gewinner, spielstaende);
                stand.setPunkte(stand.getPunkte() + punkte);
            }
        }
        aktuelleRunde.setSpielstand(spielstaende);
    }

    private Spieler getNaechstenSpieler(Spieler s) {
        Spieler result = null;
        int i = spieler.indexOf(s);
        if (i == spieler.size() - 1) {
            result = spieler.get(0);
        } else {
            result = spieler.get(i + 1);
        }
        if (result.isAktiv()) {
            return result;
        } else {
            return getNaechstenSpieler(result);
        }
    }

    public Runde getVorherigeRunde(Runde r) {
        int i = runden.indexOf(r);
        if (i == 0) {
            return null;
        } else {
            return runden.get(i - 1);
        }
    }

    public Runde getNaechsteRunde(Runde r) {
        int i = runden.indexOf(r);
        if (i == runden.size() - 1) {
            return null;
        } else {
            return runden.get(i + 1);
        }
    }

    private List<Spieler> getSpieler(Spieler geber) {
        List<Spieler> result = new ArrayList<Spieler>(4);
        Spieler spieler = geber;
        for (int i = 0; i < 4; i++) {
            spieler = getNaechstenSpieler(spieler);
            result.add(spieler);
        }
        return result;
    }

    public Runde getAktuelleRunde() {
        return aktuelleRunde;
    }

    public int getGesamtRunden() {
        return gesamtRunden;
    }

    public List<Runde> getRunden() {
        return runden;
    }

    public List<Spieler> getSpieler() {
        return spieler;
    }

    public List<Spieler> getAktiveSpieler() {
        List<Spieler> result = new ArrayList<Spieler>();
        for (Spieler s : spieler) {
            if (s.isAktiv()) {
                result.add(s);
            }
        }
        return result;
    }

    public void spielerSteigtAus(Spieler s) {
        if (s != null) {
            s.setIsAktiv(false);
            fireSpieltagUpdatedEvent();
        }
    }

    public void spielerSteigtEin(Spieler s) {
        if (s != null) {
            if (!s.isAktiv()) {
                s.setIsAktiv(true);
                fireSpieltagUpdatedEvent();
            }
            if (!spieler.contains(s)) {
                spieler.add(s);
                Runde vorherigeRunde = getVorherigeRunde(aktuelleRunde);
                if (vorherigeRunde != null) {
                    vorherigeRunde.setSpielstand(s, getStartwertNeuerSpieler());
                }
                for (SpieltagListener l : listener) {
                    l.spielerSteigtEin();
                }
            }
        }
    }

    private int getStartwertNeuerSpieler() {
        int result = 0;
        Runde vorherigeRunde = getVorherigeRunde(aktuelleRunde);
        if (vorherigeRunde != null) {
            List<Spielstand> resultate = new ArrayList(vorherigeRunde.getSpielstand());
            Collections.sort(resultate);
            int lowest = resultate.get(0).getPunkte();
            int secondLowest = resultate.get(1).getPunkte();
            result = (lowest + secondLowest) / 2;
        }
        return result;
    }

    /**
	 * Unit-test
	 */
    public static void main(String[] args) {
        List<Spieler> spieler = new ArrayList<Spieler>(6);
        for (int i = 0; i < 6; i++) spieler.add(new Spieler("Spieler" + i));
        Spieltag spieltag = new Spieltag();
        spieltag.setGesamtRunden(6);
        spieltag.start(spieler, spieler.get(0));
        spieltag.aktuelleRunde().addBock();
        spieltag.aktuelleRunde().addBock();
        spieltag.aktuelleRunde().setReAngesagt();
        spieltag.aktuelleRunde().setKontraAngesagt();
        spieltag.getNaechsteRunde(spieltag.aktuelleRunde()).addBock();
        spieltag.aktuelleRunde().setHerzGehtRum();
        System.exit(0);
    }
}
