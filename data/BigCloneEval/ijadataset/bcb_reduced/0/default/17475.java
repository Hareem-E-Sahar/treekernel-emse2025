import java.awt.Color;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * Enthaellt die auszufuehrenden Aktionen aus dem Panel Restriktion
 * 
 *
 */
public class PanelRestriktionControls {

    private PanelRestriktion panel;

    PanelRestriktionControls(PanelRestriktion panel) {
        this.panel = panel;
    }

    /**
	 * Fuehrt neue Gelelektrophorese zur Anzeige hinzu
	 * @author Sven Hopfmann, Dennis Mokros
	 */
    public void restriktion_add() {
        String dna = "", dnaName = "";
        int enzymIndex = panel.combobox_r_enzym_waehlen.getSelectedIndex();
        if (enzymIndex == -1) {
            JOptionPane.showMessageDialog(null, "Bitte waehlen Sie ein Enzym aus!", "Fehler!", JOptionPane.ERROR_MESSAGE);
            return;
        }
        EnzymList el = new EnzymList();
        Enzym enzym = el.getEnzym(enzymIndex);
        if (panel.manuelle_sequenz_eingabe) {
            dnaName = "manuell";
            dna = panel.textfield_r_eingabe_manuell.getText();
            if (dna.length() == 0) {
                JOptionPane.showMessageDialog(null, "Bitte geben Sie eine Sequenz ein!", "Keine Sequenz!", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } else {
            int sequenzIndex = panel.combobox_r_sequenz_waehlen.getSelectedIndex();
            if (sequenzIndex == -1 || panel.fileRead == null) {
                JOptionPane.showMessageDialog(null, "Bitte waehlen Sie eine Datei und Sequenz aus!", "Fehler!", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                dnaName = panel.fileRead.getID(sequenzIndex);
                dna = panel.fileRead.getSequenz(sequenzIndex);
            } catch (SequenzIndexOfBounds e) {
                JOptionPane.showMessageDialog(null, "Ungueltige Sequenz gewaehlt!", "Fehler!", JOptionPane.ERROR_MESSAGE);
                return;
            } catch (SequenzCorrupt e) {
                JOptionPane.showMessageDialog(null, "Die Sequenz ist besch�digt! Pr�fen Sie ihre Datei!", "Fehler!", JOptionPane.ERROR_MESSAGE);
                return;
            } catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(null, "Datei nicht gefunden!", "Fehler!", JOptionPane.ERROR_MESSAGE);
                return;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Beim Lesen der Datei trat ein I/O Fehler auf!", "Fehler!", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        try {
            PDRestriktion pdr = new PDRestriktion(dna, enzymIndex, panel);
            panel.panel_r_center.addResult(new RestriktionsResult(dnaName, enzym, pdr.getSites(), pdr.getFraglaeng()));
        } catch (EnzymIndexOfBounds e) {
            JOptionPane.showMessageDialog(null, "Ungueltiger Index!", "Fehler!", JOptionPane.ERROR_MESSAGE);
            return;
        } catch (MaximumExceeded e) {
            JOptionPane.showMessageDialog(null, "Maximale Anzahl an Banden erreicht!", "Warnung!", JOptionPane.WARNING_MESSAGE);
            return;
        }
        panel.updateProgressBar(100);
        panel.updateProgressBar(0);
    }

    /**
	 * Loescht eine Gelelektrophorese aus der Anzeige
	 */
    public void restriktion_delete() {
        panel.panel_r_center.deleteResult();
    }

    /**
	 * setzt alle Komponeten der manuellen Sequeneingabe auf "true" und die der Datenbank auswahl "false"
	 * @author David John
	 */
    public void radiobutton_manuell() {
        panel.textfield_r_eingabe_manuell.setEnabled(true);
        panel.combobox_r_sequenz_waehlen.setEnabled(false);
        panel.button_r_datei_oeffnen.setEnabled(false);
        panel.label_r_dna_whaelen.setEnabled(false);
        panel.manuelle_sequenz_eingabe = true;
    }

    /**
	 * setzt alle Komponeten der Datenbank auswahl auf "true" und die der manuellen Sequeneingabe auf "false"
	 * @author David John
	 */
    public void radiobutton_datenbank() {
        panel.textfield_r_eingabe_manuell.setEnabled(false);
        panel.combobox_r_sequenz_waehlen.setEnabled(true);
        panel.button_r_datei_oeffnen.setEnabled(true);
        panel.label_r_dna_whaelen.setEnabled(true);
        panel.manuelle_sequenz_eingabe = false;
    }

    /**
	 * Laesst den Benutzer eine Datenbak auswaehlen,durchsucht diese und fuegt ihren Inhalt in die entsprechende ComboBox ein.
	 */
    public void waehle_Datei() {
        Thread t = new Thread() {

            public void run() {
                File datei = waehleDatei("Oeffnen");
                if (datei != null) {
                    panel.combobox_r_sequenz_waehlen.removeAllItems();
                    panel.button_r_datei_oeffnen.setEnabled(false);
                    panel.combobox_r_sequenz_waehlen.setEnabled(false);
                    panel.radiobutton_r_datenbank.setText("Datenbank (" + datei.length() / 1024 + " KB)");
                    panel.fileRead = new FileRead(datei.getAbsolutePath(), panel.progressbar_r_status);
                    try {
                        panel.fileRead.searchIDs();
                    } catch (FileCorrupt e) {
                        JOptionPane.showMessageDialog(null, "Unbekanntes Dateiformat!\nNur EMBL und FASTA moeglich.", "Unbekanntes Format!", JOptionPane.ERROR_MESSAGE);
                    } catch (FileNotFoundException e) {
                        JOptionPane.showMessageDialog(null, "Die Datei wurde nicht gefunden!", "Fehler beim Lesen", JOptionPane.ERROR_MESSAGE);
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(null, "I/O Fehler", "Fehler beim Lesen", JOptionPane.ERROR_MESSAGE);
                    }
                    panel.label_r_dna_whaelen.setText("DNA-Sequenz (" + panel.fileRead.getSequenzCount() + "):");
                    for (int n = 0; n < panel.fileRead.getSequenzCount(); n++) {
                        try {
                            panel.combobox_r_sequenz_waehlen.addItem(panel.fileRead.getName(n));
                        } catch (SequenzIndexOfBounds e) {
                            e.printStackTrace();
                        }
                    }
                }
                panel.button_r_datei_oeffnen.setEnabled(true);
                panel.combobox_r_sequenz_waehlen.setEnabled(true);
                panel.progressbar_r_status.setValue(0);
                panel.progressbar_r_status.paint(panel.progressbar_r_status.getGraphics());
            }
        };
        t.start();
    }

    /**
	 * Laesst den Benutzer eine Datenbank auswaehlen
	 * @param msg
	 * @return Pfad der ausgewaehlten Datenbank
	 */
    private File waehleDatei(String msg) {
        JFileChooser fc = new JFileChooser();
        fc.setApproveButtonText(msg);
        int returnVal = fc.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) return fc.getSelectedFile(); else return null;
    }

    /**
	 * Fuegt alle Enzyme in die "combobox_r_enzym_waehlen" ein
	 * @author Maf Zangana
	 */
    public void ladeEnzyme() {
        panel.combobox_r_enzym_waehlen.removeAllItems();
        EnzymList em = new EnzymList();
        int sizeEL = em.getSize();
        for (int x = 0; x < sizeEL; x++) {
            Enzym enzym = em.getEnzym(x);
            panel.combobox_r_enzym_waehlen.addItem(enzym.getName());
        }
        panel.panel_r_east_unten.setBorder(BorderFactory.createTitledBorder("Enzym waehlen (" + sizeEL + ")"));
    }

    /**
	 * Ruft "ladeEnzyme" auf
	 * @see ladeEnzyme
	 */
    public void init() {
        ladeEnzyme();
    }

    /** 
	 * Macht das Contextmenu zum kopieren der Multimenge sichbar.
	 * @param me(Mouse Event)
	 * @author David John
	 */
    public void contextmenu_textfield_multimenge(MouseEvent me) {
        panel.popup_multimenge.show(panel.textfield_r_multimenge, me.getX(), me.getY());
    }

    /**
	 * Macht das Kontextmenu zum erstellen einer zufaelligen Sequenz sichtbar.
	 * @param me(Mouse Event)
	 * @author David John
	 */
    public void contextmenu_textfield_eingabe_manuell(MouseEvent me) {
        if (panel.manuelle_sequenz_eingabe) panel.popup_manuell_waehlen.show(panel.textfield_r_eingabe_manuell, me.getX(), me.getY());
    }

    /**
	 * Macht das Kontextmenu zum Auswaehlen der Farben und zum laden und speichern Sichtbar
	 * @param me
	 * @author David John
	 */
    public void contextmenu_farbe_waehlen(MouseEvent me) {
        panel.popup_farbe_waehlen.show(panel.panel_r_center, me.getX(), me.getY());
    }

    /**
	 * kopiert Multimenge in die Ligation
	 */
    public void multimenge_kopieren() {
        panel.panel_r_center.copyMultimengeToLigation();
    }

    /**
	 * Zaehlt die eingegebenen Zeichen und gibt sie als Info im Radiobutton aus
	 */
    public void textfield_manuell() {
        String eingabe = panel.textfield_r_eingabe_manuell.getText().replaceAll("\n", "");
        panel.radiobutton_r_manuell.setText("Manuell (" + eingabe.length() + ")");
    }

    /**Erzeugt eine zufaellige Sequenz der Laenge "laenge" und setzt sie in das "Textfield_r_eingabe_manuell" ein
	 * 
	 * @param laenge (Laenge der zu erstellenden Sequenz)
	 */
    public void create_sequenz(int laenge) {
        String[] protein = { "a", "t", "g", "c" };
        StringBuffer sequenz = new StringBuffer(laenge);
        for (int i = 1; i <= laenge; i++) {
            sequenz.append(protein[(int) (Math.random() * 4)]);
            if (i % 15 == 0) sequenz.append("\n");
        }
        panel.textfield_r_eingabe_manuell.setText(sequenz.toString());
        textfield_manuell();
    }

    /**
	 * Zeigt die Infos ueber das gewaehlte Enzym an
	 */
    public void oeffneInfoFrame() {
        panel.neuesInfoFrame = new infoFrame(panel.combobox_r_enzym_waehlen.getSelectedIndex());
        panel.neuesInfoFrame.setLocationRelativeTo(panel);
        panel.neuesInfoFrame.setVisible(true);
    }

    /**
	 * laesste den Benutzer ein neues Enzym hinzuf�gen/erstellen
	 */
    public void oeffneNeuesEnzymFrame() {
        panel.neuEnzymFenster = new NeuesEnzymFrame(this);
        panel.neuEnzymFenster.setLocationRelativeTo(panel);
        panel.neuEnzymFenster.setVisible(true);
    }

    /** 
	 * Sucht in der EnzymListe.dat das zu entfernende String und veranlasst das die Datei ohne das Enzym neu geschrieben wird
	 * @author David John
	 * **/
    public void entferneEnzym() {
        int index = panel.combobox_r_enzym_waehlen.getSelectedIndex();
        EnzymList el = new EnzymList();
        String name = el.getEnzym(index).getName();
        String dateiInhalt = "";
        try {
            RandomAccessFile raf = new RandomAccessFile("EnzymListe.dat", "r");
            String zeile;
            while ((zeile = raf.readLine()) != null) {
                if (!zeile.startsWith(name + ",")) dateiInhalt = dateiInhalt + zeile + "\r\n";
            }
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Fehler beim loeschen des Enzyms");
        }
        try {
            FileOutputStream output = new FileOutputStream("EnzymListe.dat");
            for (int i = 0; i < dateiInhalt.length(); i++) {
                output.write((byte) dateiInhalt.charAt(i));
            }
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.err.println("Datei konnte nciht gefunden werden");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Fehler beim schreiben der Datei");
        }
        try {
            el.LeseEnzymListe();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Fehler beim erneutem lesem der Datei lesen der Datei, nach dem entfernen eines Enzyms ");
        }
        ladeEnzyme();
    }

    /**
	 * Setzt die Farben der Gelelektrophorese auf ihre default Werte
	 * @author David John
	 */
    public void setze_default_farben() {
        panel.panel_r_center.setDefaultFarben();
    }

    /**
	 * laesst den Benutzer die Farbe der Fragmente waehlen
	 * @author David John
	 */
    public void setze_Fragment_farbe() {
        Color c = Color.blue;
        c = JColorChooser.showDialog(panel.panel_r_center, "Color Chooser", Color.blue);
        if (c != null) panel.panel_r_center.setFragmentFarbe(c);
    }

    /**
	 * laesst den Benutzer die Farbe des Hintergrundes waehlen
	 * @author David John
	 */
    public void setze_Hintergrund_farbe() {
        Color c = Color.black;
        c = JColorChooser.showDialog(panel.panel_r_center, "Color Chooser", Color.black);
        if (c != null) panel.panel_r_center.setBackgroundFarbe(c);
    }

    /**
	 * laesst den Benutzer die Farbe des Rahmens waehlen
	 * @author David John
	 */
    public void setze_Rahmen_farbe() {
        Color c = Color.red;
        c = JColorChooser.showDialog(panel.panel_r_center, "Color Chooser", Color.red);
        if (c != null) panel.panel_r_center.setRahmenFarbe(c);
    }

    /**
	 * Laedt eine vorher gespeicherte Gelelektrophorese
	 * @author Maf Zangana
	 * @throws FileCorrupt
	 * @throws IOException
	 * @throws MaximumExceeded
	 */
    public void ladeDatei() throws FileCorrupt, IOException, MaximumExceeded {
        panel.panel_r_center.Load();
    }

    /**
	 * Speichert die Gelelektrophorese
	 * @author Maf Zangana
	 * @throws IOException
	 */
    public void speicherDatei() throws IOException {
        panel.panel_r_center.Save();
    }

    /**
	 * Cleart die Gelelektrophorese
	 * @author Denis Mokros
	 */
    public void gelelektrophoreseClear() {
        panel.panel_r_center.deleteAll();
    }

    /**
	 * Setz die Farben auf ein Ethidiumbromid+UV-Gel
	 * @author Denis Mokros
	 */
    public void gelFarbvorlage_ethidiumbromid() {
        panel.panel_r_center.setFarbvorlageEthidiumbromid();
    }

    /**
	 * Setz die Farben auf ein Ethidiumbromid+UV-Gel Schwart/Weiss
	 * @author Denis Mokros
	 */
    public void gelFarbvorlage_ethidiumbromidSW() {
        panel.panel_r_center.setFarbvorlageEthidiumbromidSW();
    }

    /**
	 * Setz die Farben auf ein Silberfaerbung
	 * @author Denis Mokros
	 */
    public void gelFarbvorlage_silberfaerbung() {
        panel.panel_r_center.setFarbvorlageSilberfaerbung();
    }

    /**
	 * Setz die Farben auf ein SYBR(c) Greenfaerbung
	 * @author Denis Mokros
	 */
    public void gelFarbvorlage_SYBRgreen() {
        panel.panel_r_center.setFarbvorlageSYBRgreen();
    }

    /**
	 * Setz die Farben auf ein SYBR(c) Goldfaerbung
	 * @author Denis Mokros
	 */
    public void gelFarbvorlage_SYBRgold() {
        panel.panel_r_center.setFarbvorlageSYBRgold();
    }
}
