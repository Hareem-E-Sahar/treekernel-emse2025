import javax.swing.JFileChooser;
import javax.swing.JPanel;
import java.awt.*;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Exception die bei ung�ltige Multimengenanzahl geworfen wird
 */
class MaximumExceeded extends Exception {

    static final long serialVersionUID = 1L;
}

;

public class Gelelektrophorese extends JPanel implements MouseMotionListener, MouseListener {

    class PositionsGrafik {

        long anzahlBP;

        int anzahlFragmente;

        int groesstesFragment;

        PositionsGrafik(long anzahlBP, int anzahlFragmente, int groesstesFragment) {
            this.anzahlBP = anzahlBP;
            this.anzahlFragmente = anzahlFragmente;
            this.groesstesFragment = groesstesFragment;
        }
    }

    static final long serialVersionUID = 1L;

    private PanelRestriktion panel;

    private Graphics2D g2;

    private ArrayList<RestriktionsResult> RestriktionsResultList = new ArrayList<RestriktionsResult>();

    private ArrayList<PositionsGrafik> positonsGrafikList;

    private double dauer;

    private int selected;

    private int AnzahlStriche = 10;

    private int StrichLaenge, AbstandStriche = 20;

    private int StartOffset = 0;

    private int AbstandOben = 35, AbstandUnten = 10;

    private int schriftSize = 12, schriftSize2 = 11;

    private String schriftArt = "Sans-serif";

    private Color textFarbe = Color.white;

    private Color defaultFragmentFarbe = Color.BLUE, fragmentFarbe = defaultFragmentFarbe;

    private Color defaultBackgroundFarbe = Color.BLACK, backgroundFarbe = defaultBackgroundFarbe;

    private Color defaultRahmenFarbe = Color.RED, rahmenFarbe = defaultRahmenFarbe;

    private double FragmentVerlaufFarbFaktor = 0.2;

    Gelelektrophorese(PanelRestriktion panel) {
        super();
        this.panel = panel;
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        this.RestriktionsResultList.add(new RestriktionsResult("", new Enzym("Enzym:", "", 0), null, null, true));
    }

    /**
	 * Anzeigen des Tabs Restriktion
	 */
    private void printRestriktionsResult() {
        if (this.selected >= this.RestriktionsResultList.size()) {
            panel.textfield_r_dna.setText("");
            panel.textfield_r_enzym.setText("");
            panel.textfield_r_bruchstuecke.setText("");
            panel.textfield_r_multimenge.setText("");
            panel.label_r_bruchstuecke.setText("Schnittpositionen:");
            panel.label_r_multimenge.setText("Multimenge:");
        } else {
            panel.textfield_r_dna.setText(this.RestriktionsResultList.get(this.selected).getSequenzID());
            panel.textfield_r_enzym.setText(this.RestriktionsResultList.get(this.selected).getEnzym().getName() + "  (" + this.RestriktionsResultList.get(this.selected).getEnzym().getSequenzWithCutpos() + ")");
            panel.textfield_r_bruchstuecke.setText(this.RestriktionsResultList.get(this.selected).getSchnittposString());
            panel.textfield_r_multimenge.setText(this.RestriktionsResultList.get(this.selected).getMultimengeString(true));
            panel.label_r_bruchstuecke.setText("Schnittpositionen (" + (this.RestriktionsResultList.get(this.selected).getSchnittpos().size() - 2) + "):");
            panel.label_r_multimenge.setText("Multimenge (" + this.RestriktionsResultList.get(this.selected).getMultimenge().size() + "):");
        }
    }

    public void copyMultimengeToLigation() {
        panel.panelLigation.multimenge.setText(this.RestriktionsResultList.get(this.selected).getMultimengeString(false));
    }

    /**
	 * 
	 * @param selected
	 *            Spalte die selektiert werden soll
	 * @return Spalte die aktuell ausgew�hlt ist
	 */
    private void setSelected(int selected) {
        this.selected = selected;
        printRestriktionsResult();
    }

    /**
	 * baut die Standart DNA auf
	 */
    public void setsDNA() {
        int maxLength = 0;
        for (int i = 1; i < this.RestriktionsResultList.size(); i++) if (this.RestriktionsResultList.get(i).getMaxLength() > maxLength) maxLength = this.RestriktionsResultList.get(i).getMaxLength();
        int minLength = maxLength;
        for (int i = 1; i < this.RestriktionsResultList.size(); i++) if (this.RestriktionsResultList.get(i).getMinLength() < minLength) minLength = this.RestriktionsResultList.get(i).getMinLength();
        this.RestriktionsResultList.get(0).setsDNA(minLength, maxLength);
    }

    /**
	 * f�gt ein neues Ergebnis der Restriktion hinzu
	 * 
	 * @param rr
	 *            neues Restriktionsergebnis
	 * @throws MaximumExceeded
	 *             maximale Anzahl erreicht
	 */
    public void addResult(RestriktionsResult rr) throws MaximumExceeded {
        if (this.RestriktionsResultList.size() >= this.AnzahlStriche) throw new MaximumExceeded();
        this.RestriktionsResultList.add(rr);
        setSelected(RestriktionsResultList.size() - 1);
        setsDNA();
        this.repaint();
    }

    /**
	 * l�scht das ausgew�hlte Restriktions-Ergebnis
	 */
    public void deleteResult() {
        if (this.RestriktionsResultList.size() > 1) {
            this.RestriktionsResultList.remove(this.selected);
            if (this.selected == this.RestriktionsResultList.size() && this.selected > 1) setSelected(this.selected - 1); else setSelected(this.selected);
            setsDNA();
            this.repaint();
        }
    }

    /**
	 * zeichnet das Restriktions-Ergebnis
	 */
    public void paint(Graphics g) {
        g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Hintergrund();
        BerechneStrichLaenge();
        zeichneFragmente();
        zeichneAuswahlRahmen();
    }

    /**
	 * Loesche Hintergrund
	 */
    private void Hintergrund() {
        g2.setColor(this.backgroundFarbe);
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());
    }

    /**
	 * Zeichnet den Auswahl-Rahmen
	 */
    private void zeichneAuswahlRahmen() {
        if ((this.RestriktionsResultList.size()) > 1) {
            int x1 = (this.AbstandStriche * (this.selected + 1) + this.StrichLaenge * (this.selected)) - this.AbstandStriche / 2;
            int x2 = x1 + this.StrichLaenge;
            int y1 = this.AbstandOben;
            int y2 = this.getHeight() - this.AbstandUnten;
            g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
            g2.setColor(this.rahmenFarbe);
            g2.drawRect(x1 - 4, y1 - 4, x2 - x1 + 8, y2 - y1 + 8);
        }
    }

    /**
	 * Setzt die Standart Farben der Gelelektrophorese
	 */
    public void setDefaultFarben() {
        this.fragmentFarbe = this.defaultFragmentFarbe;
        this.backgroundFarbe = this.defaultBackgroundFarbe;
        this.rahmenFarbe = this.defaultRahmenFarbe;
        this.repaint();
    }

    /**
	 * Setzt die Farbe auf Ehtidiumbromid + UV-Licht
	 */
    public void setFarbvorlageEthidiumbromid() {
        this.fragmentFarbe = Color.MAGENTA;
        this.backgroundFarbe = new Color(0, 0, 100);
        this.rahmenFarbe = Color.RED;
        this.repaint();
    }

    /**
	 * Setzt die Farbe auf Ehtidiumbromid + UV-Licht Schwarz/Weiss
	 */
    public void setFarbvorlageEthidiumbromidSW() {
        this.fragmentFarbe = new Color(200, 200, 200);
        this.backgroundFarbe = new Color(20, 20, 20);
        this.rahmenFarbe = Color.RED;
        this.repaint();
    }

    /**
	 * Setzt die Farbe auf Silberfaerbung
	 */
    public void setFarbvorlageSilberfaerbung() {
        this.fragmentFarbe = new Color(70, 50, 0);
        this.backgroundFarbe = new Color(160, 90, 0);
        this.rahmenFarbe = Color.WHITE;
        this.repaint();
    }

    /**
	 * Setzt die Farbe auf SYBR(c) Green
	 */
    public void setFarbvorlageSYBRgreen() {
        this.fragmentFarbe = Color.GREEN;
        this.backgroundFarbe = new Color(0, 0, 0);
        this.rahmenFarbe = Color.GREEN;
        this.repaint();
    }

    /**
	 * Setzt die Farbe auf SYBR(c) Gold
	 */
    public void setFarbvorlageSYBRgold() {
        this.fragmentFarbe = new Color(250, 240, 70);
        this.backgroundFarbe = new Color(0, 0, 0);
        this.rahmenFarbe = Color.YELLOW;
        this.repaint();
    }

    /**
	 * setzt die Fragment Farbe
	 * 
	 * @param color
	 *            Fragmentfarbe
	 */
    public void setFragmentFarbe(Color color) {
        this.fragmentFarbe = color;
        this.repaint();
    }

    /**
	 * setzt die Hintergrund Farbe
	 * 
	 * @param color
	 *            Hintergrundfarbe
	 */
    public void setBackgroundFarbe(Color color) {
        this.backgroundFarbe = color;
        this.repaint();
    }

    /**
	 * setzt die Rahmen Farbe
	 * 
	 * @param color
	 *            Rahmenfarbe
	 */
    public void setRahmenFarbe(Color color) {
        this.rahmenFarbe = color;
        this.repaint();
    }

    /**
	 * beschriftet die Standart DNA Fragmente
	 */
    private void zeichneFragmentBezeichnung(Point point, boolean letztes) {
        Font myFont = new Font(this.schriftArt, 0, this.schriftSize2);
        FontMetrics fm = getFontMetrics(myFont);
        String fragmentLaenge = new Integer(this.positonsGrafikList.get(point.y).groesstesFragment).toString();
        int textXOffset = (this.StrichLaenge - fm.stringWidth(fragmentLaenge)) / 2;
        int y1 = point.y - fm.getHeight() / 2 + 3;
        int dy = fm.getHeight() - 5;
        if (y1 < this.AbstandOben) {
            dy = dy - (this.AbstandOben - y1);
            y1 = this.AbstandOben;
        }
        if (letztes) {
            dy = point.y - y1 + 1;
        }
        Color deckFarbe = getTransparentColor(this.fragmentFarbe, this.backgroundFarbe, this.FragmentVerlaufFarbFaktor);
        g2.setColor(deckFarbe);
        g2.fillRect(point.x + textXOffset - 3, y1, fm.stringWidth(fragmentLaenge) + 5, dy);
        zeichneText(myFont, fragmentLaenge, point.x + textXOffset, point.y + (int) (this.schriftSize2 / 2));
    }

    /**
	 * zeichnet die Beschriftung �ber dem Restriktions Ergebnis
	 * 
	 * @param resultIndex
	 *            zu beschriftendes Restriktions Ergebnis
	 */
    private void zeichneBezeichnung(int resultIndex) {
        Font myFont = new Font(this.schriftArt, Font.BOLD, this.schriftSize);
        FontMetrics fm = getFontMetrics(myFont);
        Point point = berechneFragmentPosition(resultIndex, 0);
        String enzymName = this.RestriktionsResultList.get(resultIndex).getEnzym().getName();
        int textXOffset = (this.StrichLaenge - fm.stringWidth(enzymName)) / 2;
        zeichneText(myFont, enzymName, point.x + textXOffset, (int) this.AbstandOben / 2 - 2);
        String fragmentAnzahl;
        if (this.RestriktionsResultList.get(resultIndex).issDNA()) fragmentAnzahl = "Anzahl:"; else fragmentAnzahl = new Integer(this.RestriktionsResultList.get(resultIndex).getMultimenge().size()).toString();
        textXOffset = (this.StrichLaenge - fm.stringWidth(fragmentAnzahl)) / 2;
        zeichneText(myFont, fragmentAnzahl, point.x + textXOffset, (int) this.AbstandOben - 7);
    }

    /**
	 * zeichnet Text
	 * 
	 * @param font
	 *            Schriftart
	 * @param text
	 *            zu zeichnender Text
	 * @param x
	 *            x Koordinate des Textes
	 * @param y
	 *            y Koordinate des Textes
	 */
    private void zeichneText(Font font, String text, int x, int y) {
        g2.setFont(font);
        g2.setColor(this.textFarbe);
        g2.drawString(text, x, y);
    }

    /**
	 * Berechnet Strichlaenge in Bezug auf die aktuelle Fenstergroesse
	 */
    private void BerechneStrichLaenge() {
        this.StrichLaenge = (this.getWidth() - (this.AnzahlStriche * this.AbstandStriche)) / this.AnzahlStriche;
    }

    /**
	 * Zeit die das kleinste Element fuer seinen Weg von der Ober- zur
	 * Untergrenze benoetigt
	 */
    private void berechneDauer() {
        double maxSpeed = 0;
        for (int i = 0; i < this.RestriktionsResultList.size(); i++) if (this.RestriktionsResultList.get(i).getMaxSpeed() > maxSpeed) maxSpeed = this.RestriktionsResultList.get(i).getMaxSpeed();
        double minSpeed = maxSpeed;
        for (int i = 0; i < this.RestriktionsResultList.size(); i++) if (this.RestriktionsResultList.get(i).getMinSpeed() < minSpeed) minSpeed = this.RestriktionsResultList.get(i).getMinSpeed();
        this.dauer = (this.getHeight() - (this.AbstandOben + this.AbstandUnten)) / maxSpeed;
        int tempOffset;
        if (minSpeed != maxSpeed) {
            do {
                tempOffset = this.StartOffset;
                this.StartOffset = (int) (minSpeed * this.dauer);
                this.dauer = (this.getHeight() - (this.AbstandOben + this.AbstandUnten) + this.StartOffset) / maxSpeed;
            } while (this.StartOffset != tempOffset);
        }
        this.dauer = Math.pow(this.dauer, Math.sqrt(this.dauerScaleFactor));
    }

    /**
	 * zeichnet Schattierung oberhalb des ersten Fragments
	 * 
	 * @param resultIndex
	 *            zu schattierndes Result Ergebnis
	 */
    private void zeichneFragmentVerlauf(int resultIndex) {
        int x1 = (this.AbstandStriche * (resultIndex + 1) + this.StrichLaenge * (resultIndex)) - this.AbstandStriche / 2;
        int y1 = this.AbstandOben;
        int y2 = (int) (this.dauer * this.RestriktionsResultList.get(resultIndex).getMaxSpeed()) - this.StartOffset + this.AbstandOben;
        if (y2 > this.getHeight() - (this.AbstandUnten)) y2 = this.getHeight() - (this.AbstandUnten);
        g2.setColor(getTransparentColor(this.fragmentFarbe, this.backgroundFarbe, this.FragmentVerlaufFarbFaktor));
        g2.fillRect(x1, y1, this.StrichLaenge, y2 - y1);
    }

    /**
	 * @param Farbe
	 *            Fragmentfarbe
	 * @param Background
	 *            Hintergrundfarbe
	 * @param Alpha
	 *            Mischverhaeltnis
	 * @return gibt den Mischwert aus Fragment- und Hintergrundfarbe
	 */
    private Color getTransparentColor(Color Farbe, Color Background, double Alpha) {
        int red = (int) ((Farbe.getRed() * Alpha) + (Background.getRed() * (1 - Alpha)));
        int green = (int) ((Farbe.getGreen() * Alpha) + (Background.getGreen() * (1 - Alpha)));
        int blue = (int) ((Farbe.getBlue() * Alpha) + (Background.getBlue() * (1 - Alpha)));
        return new Color(red, green, blue);
    }

    /**
	 * zeichnet die Fragmente
	 */
    private void zeichneFragmente() {
        if (this.RestriktionsResultList.size() > 1) {
            berechneDauer();
            for (int i = 0; i < this.RestriktionsResultList.size(); i++) {
                zeichneBezeichnung(i);
                zeichneFragmentVerlauf(i);
                positionsGrafikListVerwaltung(i);
                Point point = berechneFragmentPosition(i, 0);
                ArrayList<Integer> beschriftungsPositionen = new ArrayList<Integer>();
                int letzteBeschriftung = 0;
                for (int y = 0; y < this.positonsGrafikList.size(); y++) {
                    if (this.positonsGrafikList.get(y).anzahlFragmente > 0) {
                        point.y = y;
                        int anzahlFragmente = this.positonsGrafikList.get(y).anzahlFragmente;
                        if (anzahlFragmente > 5) anzahlFragmente = 5;
                        if (this.RestriktionsResultList.get(i).issDNA()) anzahlFragmente = 1;
                        ZeichneStrich(point.x, point.y, point.x + this.StrichLaenge, point.y, anzahlFragmente, getTransparentColor(this.fragmentFarbe, Color.white, 1 - (anzahlFragmente / 10.0)));
                        if (this.RestriktionsResultList.get(i).issDNA() && ((y - letzteBeschriftung) >= 9)) {
                            letzteBeschriftung = y;
                            beschriftungsPositionen.add(y);
                        }
                    }
                }
                for (int p = 0; p < beschriftungsPositionen.size(); p++) {
                    point.y = beschriftungsPositionen.get(p);
                    if (p == beschriftungsPositionen.size() - 1) zeichneFragmentBezeichnung(point, true); else zeichneFragmentBezeichnung(point, false);
                }
            }
        }
    }

    /**
	 * berechnet die Position des Fragmentes
	 * 
	 * @param resultIndex
	 *            das betreffende Restriktions Ergebnis
	 * @param fragmentIndex
	 *            Index des zu zeichnenden Fragmentes
	 * @return gibt Koordinate zurueck
	 */
    private Point berechneFragmentPosition(int resultIndex, int fragmentIndex) {
        Point point = new Point();
        int x = (this.AbstandStriche * (resultIndex + 1) + this.StrichLaenge * (resultIndex)) - this.AbstandStriche / 2;
        int y = (int) (this.dauer * this.RestriktionsResultList.get(resultIndex).getSpeed(fragmentIndex)) - this.StartOffset + this.AbstandOben;
        if (y > this.getHeight() - (this.AbstandUnten)) y = this.getHeight() - (this.AbstandUnten);
        if (y < this.AbstandOben) y = this.AbstandOben;
        point.x = x;
        point.y = y;
        return point;
    }

    /**
	 * Zeichne Fragment/Strich
	 * 
	 * @param x1
	 *            beginn x-Richtung
	 * @param x2
	 *            ende x-Richtung
	 * @param y1
	 *            beginn y-Richtung
	 * @param y2
	 *            ende y-Richtung
	 * @param breite
	 *            Strichbreite
	 * @param farbe
	 *            Strichfarbe
	 * 
	 */
    private void ZeichneStrich(int x1, int y1, int x2, int y2, int breite, Color farbe) {
        g2.setStroke(new BasicStroke(breite, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g2.setColor(farbe);
        g2.drawLine(x1, y1, x2, y2);
    }

    /**
	 * speichern der zu zeichnenden Fragmente
	 * 
	 * @param resultIndex
	 *            Restriktions Ergebnis
	 */
    private void positionsGrafikListVerwaltung(int resultIndex) {
        positonsGrafikList = new ArrayList<PositionsGrafik>(this.getHeight());
        for (int i = 0; i < this.getHeight(); i++) positonsGrafikList.add(new PositionsGrafik(0, 0, 0));
        for (int i = 0; i < this.RestriktionsResultList.get(resultIndex).getMultimenge().size(); i++) {
            Point point = berechneFragmentPosition(resultIndex, i);
            PositionsGrafik old = positonsGrafikList.get(point.y);
            PositionsGrafik temp = new PositionsGrafik(old.anzahlBP + this.RestriktionsResultList.get(resultIndex).getLength(i), old.anzahlFragmente + 1, 0);
            if (this.RestriktionsResultList.get(resultIndex).getLength(i) > old.groesstesFragment) temp.groesstesFragment = this.RestriktionsResultList.get(resultIndex).getLength(i);
            positonsGrafikList.set(point.y, temp);
        }
    }

    public void deleteAll() {
        while (this.RestriktionsResultList.size() > 1) {
            deleteResult();
        }
    }

    private Point DragStart = new Point();

    private double deltaY, lastDeltaY = 0.0;

    private double dauerScaleFactor = 1.0;

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }

    /**
	 * Auswahlrahmen auf gew�nschtes Restriktions Ergebnis setzen
	 */
    public void mouseClicked(MouseEvent e) {
        int tempselected = 0;
        for (int i = e.getX(); i > (this.AbstandStriche + this.StrichLaenge); i -= (this.AbstandStriche + this.StrichLaenge)) {
            tempselected++;
        }
        if (tempselected > 0 && tempselected <= this.RestriktionsResultList.size() - 1) {
            this.selected = tempselected;
            this.repaint();
            this.printRestriktionsResult();
        }
    }

    /**
	 * wandern lassen der Fragmente in Richtung X-max/X-min
	 */
    public void mouseDragged(MouseEvent e) {
        if (e.getY() != this.DragStart.y) {
            this.deltaY = (e.getY() - this.DragStart.y) + this.lastDeltaY;
            this.dauerScaleFactor = (this.deltaY / this.getHeight()) + 1;
            this.repaint();
        }
    }

    /**
	 * Klicken und ziehen laesst DNA wandern Doppelklick setzt die Zeichnung
	 * zurueck
	 */
    public void mousePressed(MouseEvent e) {
        if (e.getClickCount() == 1) this.DragStart.y = e.getY();
        if (e.getClickCount() == 2) {
            this.deltaY = this.lastDeltaY = 0.0;
            this.dauerScaleFactor = 1.0;
            this.repaint();
        }
    }

    /**
	 * ermittelte Distanz zwischen MousePressed und MouseReleased
	 */
    public void mouseReleased(MouseEvent e) {
        this.lastDeltaY = this.deltaY;
    }

    public void Save() throws IOException {
        File ausgabedatei;
        FileWriter fw;
        BufferedWriter bw;
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showSaveDialog(panel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            ausgabedatei = new File(file.getAbsolutePath());
            fw = new FileWriter(ausgabedatei);
            bw = new BufferedWriter(fw);
            for (int i = 1; i < this.RestriktionsResultList.size(); i++) {
                bw.write((this.RestriktionsResultList.get(i).getSequenzID()) + "!");
                bw.write(this.RestriktionsResultList.get(i).getEnzym().getName() + "!" + this.RestriktionsResultList.get(i).getEnzym().getSequenz() + "!" + this.RestriktionsResultList.get(i).getEnzym().getCutpos() + "!");
                StringBuffer xy = new StringBuffer();
                for (int ii = 0; ii < this.RestriktionsResultList.get(i).getSchnittpos().size(); ii++) {
                    if (ii == this.RestriktionsResultList.get(i).getSchnittpos().size() - 1) {
                        xy.append(this.RestriktionsResultList.get(i).getSchnittpos().get(ii) + "!");
                    } else {
                        xy.append(this.RestriktionsResultList.get(i).getSchnittpos().get(ii) + ",");
                    }
                }
                bw.write(xy.toString());
                xy = new StringBuffer();
                for (int ii = 0; ii < this.RestriktionsResultList.get(i).getMultimenge().size(); ii++) {
                    if (ii == this.RestriktionsResultList.get(i).getMultimenge().size() - 1) {
                        xy.append(this.RestriktionsResultList.get(i).getMultimenge().get(ii) + "!");
                    } else {
                        xy.append(this.RestriktionsResultList.get(i).getMultimenge().get(ii) + ",");
                    }
                }
                bw.write(xy.toString());
            }
            bw.close();
            fw.close();
        }
    }

    public void Load() throws IOException, FileCorrupt, MaximumExceeded {
        String sequenzID = "";
        char lesekopf = 'x';
        String EName = "";
        String ESQ = "";
        int ECP = 0;
        ArrayList<Integer> schnittPos = new ArrayList<Integer>();
        ArrayList<Integer> multimenge;
        StringBuffer sb = new StringBuffer();
        JFileChooser fc = new JFileChooser();
        fc.showOpenDialog(null);
        if (fc.getSelectedFile() != null) {
            FileInputStream f = new FileInputStream(fc.getSelectedFile());
            int x = -1;
            deleteAll();
            try {
                do {
                    if (lesekopf != '!') {
                        do {
                            if (f.available() == 0) {
                                break;
                            } else {
                                lesekopf = (char) f.read();
                                if (lesekopf != '!') {
                                    sb.append(lesekopf);
                                }
                            }
                        } while (lesekopf != '!');
                        x++;
                    }
                    if (lesekopf == '!') {
                        if (x == 0) {
                            sequenzID = sb.toString();
                            sb = new StringBuffer();
                            lesekopf = (char) f.read();
                            sb.append(lesekopf);
                        }
                        if (x == 1) {
                            EName = sb.toString();
                            sb = new StringBuffer();
                            lesekopf = (char) f.read();
                            sb.append(lesekopf);
                        }
                        if (x == 2) {
                            ESQ = sb.toString();
                            sb = new StringBuffer();
                            lesekopf = (char) f.read();
                            sb.append(lesekopf);
                        }
                        if (x == 3) {
                            ECP = Integer.parseInt(sb.toString());
                            sb = new StringBuffer();
                            lesekopf = (char) f.read();
                            sb.append(lesekopf);
                        }
                        if (x == 4) {
                            schnittPos = new ArrayList<Integer>();
                            StringTokenizer st = new StringTokenizer(sb.toString(), ",");
                            while (st.hasMoreTokens() == true) {
                                schnittPos.add((Integer.parseInt(st.nextToken())));
                            }
                            sb = new StringBuffer();
                            lesekopf = (char) f.read();
                            sb.append(lesekopf);
                        }
                        if (x == 5) {
                            multimenge = new ArrayList<Integer>();
                            StringTokenizer st = new StringTokenizer(sb.toString(), ",");
                            while (st.hasMoreTokens() == true) {
                                multimenge.add(Integer.parseInt(st.nextToken()));
                            }
                            sb = new StringBuffer();
                            x = -1;
                            lesekopf = (char) f.read();
                            sb.append(lesekopf);
                            panel.panel_r_center.addResult(new RestriktionsResult(sequenzID, new Enzym(EName, ESQ, ECP), schnittPos, multimenge));
                            this.repaint();
                        }
                    }
                } while (f.available() != 0 && lesekopf != ' ');
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            f.close();
        }
    }
}
