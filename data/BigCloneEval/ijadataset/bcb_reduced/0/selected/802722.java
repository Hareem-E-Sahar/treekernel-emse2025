package FGMP_Hotel_Management.GUI;

import java.io.File;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import FGMP_Hotel_Management.Meldungen;
import FGMP_Hotel_Management.Datenbank.*;
import FGMP_Hotel_Management.Language.ErrorMsg;
import FGMP_Hotel_Management.Language.LanguageFile;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * stellt Methoden für den Administrations-Tab zur Verfügung
 *
 * @author Daniel Fischer, David Gawehn, Martin Meyer
 */
public class GUI_Administration {

    /**
     * fügt einen neuen Benutzer in der Datenbank hinzu
     *
     * überprüft, ob Benutzername und Passowrt eingegeben wurde
     * überprüft, ob Passwort und Passwortwiederholung identisch sind
     * überprüft, ob Benutzername noch nicht existiert
     * falls, alle Bedingungen eintreffen, wird der Benutzer der Datenbank hinzugefügt
     *
     */
    public static void Benutzer_hinzu() {
        User neuerB = new User();
        if (GUI_main.jText_Benutzername.getText().equals("")) {
            Meldungen.show_Dialog(ErrorMsg.msg[24], "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!String.valueOf(GUI_main.jPasswordField_Passwort.getPassword()).equals(String.valueOf(GUI_main.jPasswordField_Passwort_wdh.getPassword()))) {
            Meldungen.show_Dialog(ErrorMsg.msg[25], "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (GUI_main.jPasswordField_Passwort.getPassword().length == 0) {
            Meldungen.show_Dialog(ErrorMsg.msg[26], "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        neuerB.setBenutzer_ID(String.valueOf(GUI_main.jText_Benutzername.getText()));
        neuerB.setPasswort(String.valueOf(GUI_main.jPasswordField_Passwort.getPassword()));
        neuerB.setBuchung_bearbeiten(GUI_main.jCheckBox_Buchung_bearbeiten.isSelected());
        neuerB.setGast_bearbeiten(GUI_main.jCheckBox_Gast_bearbeiten.isSelected());
        neuerB.setZimmer_bearbeiten(GUI_main.jCheckBox_Zimmer_bearbeiten.isSelected());
        neuerB.setKategorie_bearbeiten(GUI_main.jCheckBox_Kategorie_bearbeiten.isSelected());
        neuerB.setNutzer_bearbeiten(GUI_main.jCheckBox_Nutzer_bearbeiten.isSelected());
        neuerB.setKonfig_bearbeiten(GUI_main.jCheckBox_Konfig_bearbeiten.isSelected());
        if (neuerB.getDatenAusDB()) {
            Meldungen.show_Dialog(ErrorMsg.msg[27], "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        neuerB.insertAtDB();
        User.refreshData();
    }

    public static void Benutzer_bearbeiten() {
        GUI_main.jDialog_Benutzer_bearbeiten.setVisible(true);
        User bearbeiteter_benutzer = new User(String.valueOf(GUI_main.jCombo_Admin_Benutzer.getSelectedItem()), true);
        boolean hilf = true;
        if (bearbeiteter_benutzer.getBenutzer_ID().equals("admin")) {
            hilf = false;
        }
        GUI_main.jText_Bb_Benutzername.setEnabled(hilf);
        GUI_main.jCheckBox_Bb_Buchung_bearbeiten.setEnabled(hilf);
        GUI_main.jCheckBox_Bb_Gast_bearbeiten.setEnabled(hilf);
        GUI_main.jCheckBox_Bb_Zimmer_bearbeiten.setEnabled(hilf);
        GUI_main.jCheckBox_Bb_Kategorie_bearbeiten.setEnabled(hilf);
        GUI_main.jCheckBox_Bb_Nutzer_bearbeiten.setEnabled(hilf);
        GUI_main.jCheckBox_Bb_Konfig_bearbeiten.setEnabled(hilf);
        GUI_main.jButton_Bb_Benutzer_loeschen.setEnabled(hilf);
        GUI_main.jLabel_Bb_Benutzername.setText(String.valueOf(bearbeiteter_benutzer.getBenutzer_ID()));
        GUI_main.jText_Bb_Benutzername.setText(String.valueOf(bearbeiteter_benutzer.getBenutzer_ID()));
        GUI_main.jPasswordField_Bb_Passwort.setText(null);
        GUI_main.jPasswordField_Bb_Passwort_wdh.setText(null);
        GUI_main.jCheckBox_Bb_Buchung_bearbeiten.setSelected(bearbeiteter_benutzer.getBuchung_bearbeiten());
        GUI_main.jCheckBox_Bb_Gast_bearbeiten.setSelected(bearbeiteter_benutzer.getGast_bearbeiten());
        GUI_main.jCheckBox_Bb_Zimmer_bearbeiten.setSelected(bearbeiteter_benutzer.getZimmer_bearbeiten());
        GUI_main.jCheckBox_Bb_Kategorie_bearbeiten.setSelected(bearbeiteter_benutzer.getKategorie_bearbeiten());
        GUI_main.jCheckBox_Bb_Nutzer_bearbeiten.setSelected(bearbeiteter_benutzer.getNutzer_bearbeiten());
        GUI_main.jCheckBox_Bb_Konfig_bearbeiten.setSelected(bearbeiteter_benutzer.getKonfig_bearbeiten());
    }

    /**
     * aktualisiert Benutzerinformationen in der Datenbank
     *
     * überprüft, ob Benutzername eingegeben wurde
     * überprüft, ob Passwort und Passwortwiederholung identsich sind
     *
     * Falls die Bedingungen zutreffen, werden die Daten in der Datenbank aktualisiert.
     * Falls kein neues Passwort eingegeben wurde, wird das alte beibehalten.
     * Falls der momentan angemeldete Benutzer bearbeitet wird, wird dieser automatisch ausgeloggt.
     */
    public static void Benutzer_aktualisieren() {
        User neuerB = new User();
        if (GUI_main.jText_Bb_Benutzername.getText().equals("")) {
            Meldungen.show_Dialog(ErrorMsg.msg[24], "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!String.valueOf(GUI_main.jPasswordField_Bb_Passwort.getPassword()).equals(String.valueOf(GUI_main.jPasswordField_Bb_Passwort_wdh.getPassword()))) {
            Meldungen.show_Dialog(ErrorMsg.msg[25], "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        neuerB.setBenutzer_ID(String.valueOf(GUI_main.jText_Bb_Benutzername.getText()));
        neuerB.setBenutzer_ID_old(String.valueOf(GUI_main.jLabel_Bb_Benutzername.getText()));
        neuerB.setPasswort(String.valueOf(GUI_main.jPasswordField_Bb_Passwort.getPassword()));
        neuerB.setBuchung_bearbeiten(GUI_main.jCheckBox_Bb_Buchung_bearbeiten.isSelected());
        neuerB.setGast_bearbeiten(GUI_main.jCheckBox_Bb_Gast_bearbeiten.isSelected());
        neuerB.setZimmer_bearbeiten(GUI_main.jCheckBox_Bb_Zimmer_bearbeiten.isSelected());
        neuerB.setKategorie_bearbeiten(GUI_main.jCheckBox_Bb_Kategorie_bearbeiten.isSelected());
        neuerB.setNutzer_bearbeiten(GUI_main.jCheckBox_Bb_Nutzer_bearbeiten.isSelected());
        neuerB.setKonfig_bearbeiten(GUI_main.jCheckBox_Bb_Konfig_bearbeiten.isSelected());
        if (!GUI_main.jText_Bb_Benutzername.getText().equals(GUI_main.jLabel_Bb_Benutzername.getText()) && neuerB.getDatenAusDB()) {
            Meldungen.show_Dialog(ErrorMsg.msg[27], "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (GUI_main.jPasswordField_Bb_Passwort.getPassword().length == 0) {
            neuerB.updateDB(false);
        } else {
            neuerB.updateDB(true);
        }
        GUI_main.jDialog_Benutzer_bearbeiten.setVisible(false);
        if (GUI_main.benutzer.getBenutzer_ID().equals(neuerB.getBenutzer_ID())) {
            GUI_main.benutzer = null;
            for (int i = 1; i < GUI_main.jTabbedPane_Main.getTabCount(); i++) {
                GUI_main.jTabbedPane_Main.setEnabledAt(i, false);
            }
            GUI_main.jMenu_saveConfig.setEnabled(false);
            GUI_main.jButton_logout.setEnabled(false);
            GUI_main.jButton_login.setEnabled(true);
            GUI_main.jTabbedPane_Main.setSelectedIndex(0);
        }
        User.refreshData();
    }

    /**
     * Löscht eine Benutzer aus der Datenbank
     *
     * Falls der momentan angemeldete Benutzer gelscht wird, wird dieser automatisch ausgeloggt.
     */
    public static void Benutzer_loeschen() {
        User neuerB = new User(GUI_main.jLabel_Bb_Benutzername.getText(), false);
        neuerB.delDB();
        GUI_main.jDialog_Benutzer_bearbeiten.setVisible(false);
        if (GUI_main.benutzer.getBenutzer_ID().equals(neuerB.getBenutzer_ID())) {
            GUI_main.benutzer = null;
            for (int i = 1; i < GUI_main.jTabbedPane_Main.getTabCount(); i++) {
                GUI_main.jTabbedPane_Main.setEnabledAt(i, false);
            }
            GUI_main.jMenu_saveConfig.setEnabled(false);
            GUI_main.jButton_logout.setEnabled(false);
            GUI_main.jButton_login.setEnabled(true);
            GUI_main.jTabbedPane_Main.setSelectedIndex(0);
        }
        User.refreshData();
    }

    /**
     * Öffnet den Dialog "Kategorie bearbeiten". Dabei werden die Einträge der Dialogbox mit den Kategoriedaten aus der Datenbank gefüllt.
     */
    public static void Kategorie_bearbeiten() {
        GUI_main.jDialog_Kategorie_bearbeiten.setVisible(true);
        Kategorie bearbeitete_kategorie = new Kategorie(Integer.valueOf(String.valueOf(Kategorie.Kategorie_IDs.get(GUI_main.jCombo_Admin_Kategorie.getSelectedIndex()))), true);
        GUI_main.jText_Kategorie_bearbeiten_Betten.setText(String.valueOf(bearbeitete_kategorie.getBetten()));
        GUI_main.jText_Kategorie_bearbeiten_Ausstattung.setText(bearbeitete_kategorie.getAusstattung());
        GUI_main.jText_Kategorie_bearbeiten_Preis.setText(String.valueOf(bearbeitete_kategorie.getPreis()));
    }

    /**
     * Öffnet den Dialog "Zimmer bearbeiten". Dabei werden die Einträge der Dialogbox mit den Zimmerdaten aus der Datenbank gefüllt.
     *
     */
    public static void Zimmer_bearbeiten() {
        GUI_main.jDialog_Zimmer_bearbeiten.setVisible(true);
        GUI_main.jText_Zimmer_bearbeiten_Zimmernummer.setText((String) Zimmer.Zimmer_IDs.get(GUI_main.jCombo_Admin_Zimmer_Auswahl.getSelectedIndex()));
        Zimmer bearbeitetes_zimmer = new Zimmer(Integer.valueOf(String.valueOf(Zimmer.Zimmer_IDs.get(GUI_main.jCombo_Admin_Zimmer_Auswahl.getSelectedIndex()))), true);
        GUI_main.jText_Zimmer_bearbeiten_Etage.setText((String.valueOf(bearbeitetes_zimmer.getEtage())));
        if (bearbeitetes_zimmer.getGesperrt() == 1) {
            GUI_main.jCheckBox_Zimmer_bearbeiten_gesperrt.setSelected(true);
        }
        DB_Helpers.getComboItems((DefaultComboBoxModel) GUI_main.jCombo_Zimmer_bearbeiten_Kategorie.getModel(), Zimmer.Kategorie_IDs, "kategorie", "ausstattung", "kategorie_id");
        for (int i = 0; i < Zimmer.Kategorie_IDs.size(); i++) {
            if (String.valueOf(Zimmer.Kategorie_IDs.get(i)).equals(String.valueOf(bearbeitetes_zimmer.getKategorie()))) {
                GUI_main.jCombo_Zimmer_bearbeiten_Kategorie.setSelectedIndex(i);
            }
        }
    }

    /**
     * Fügt ein neues Zimmer in der Datenbank hinzu. Dabei wird abgefragt ob das Zimmer schon in der Datenbank existiert. Wenn ja, so wird eine Fehlermeldung ausgegeben. Es ist möglich mehrere Zimmer auf einmal hinzuzufügen.
     */
    public static void Zimmer_hinzu() {
        String text = GUI_main.jText_Admin_Zimmernummer.getText();
        text = text.trim();
        text = text.replaceAll(";", ",");
        ArrayList List = new ArrayList();
        boolean error = false;
        Pattern p1 = Pattern.compile("[0-9]+");
        Pattern p2 = Pattern.compile("[0-9]+[-][0-9]+");
        Matcher m1 = p1.matcher(text);
        Matcher m2 = p2.matcher(text);
        while (m1.find()) {
            List.add(Integer.valueOf(text.substring(m1.start(), m1.end())));
        }
        while (m2.find()) {
            String sub = text.substring(m2.start(), m2.end());
            Matcher m3 = p1.matcher(sub);
            ArrayList MatcherList = new ArrayList();
            while (m3.find()) {
                MatcherList.add(Integer.valueOf(sub.substring(m3.start(), m3.end())));
            }
            for (int i = (Integer) MatcherList.get(0); i != (Integer) MatcherList.get(1); i++) {
                if (!List.contains(i)) List.add(i);
            }
        }
        if (!error) {
            for (int i = 0; i < List.size(); i++) {
                int j = (Integer) List.get(i);
                Zimmer neuesZ = new Zimmer(j, false);
                neuesZ.setKategorie(Integer.valueOf(String.valueOf(Zimmer.Kategorie_IDs.get(GUI_main.jCombo_Admin_Zimmer_Kategorie.getSelectedIndex()))));
                neuesZ.setEtage(Integer.valueOf(GUI_main.jText_Admin_Etage.getText()));
                if (GUI_main.jCheckBox_Admin_Zimmer_gesperrt.isSelected()) {
                    neuesZ.setGesperrt(1);
                } else neuesZ.setGesperrt(0);
                neuesZ.insertAtDB();
            }
            Zimmer.refreshData();
        }
    }

    /**
    * Einträge vom Dialog "Zimmer bearbeiten" werden in die Datenbank geschrieben.
    */
    public static void Zimmer_aktualisieren() {
        Zimmer neuesZ = new Zimmer(Integer.valueOf(GUI_main.jText_Zimmer_bearbeiten_Zimmernummer.getText()), false);
        neuesZ.setKategorie(GUI_main.jCombo_Zimmer_bearbeiten_Kategorie.getSelectedIndex() + 1);
        neuesZ.setEtage(Integer.valueOf(GUI_main.jText_Zimmer_bearbeiten_Etage.getText()));
        if (GUI_main.jCheckBox_Zimmer_bearbeiten_gesperrt.isSelected()) {
            neuesZ.setGesperrt(1);
        } else {
            neuesZ.setGesperrt(0);
        }
        neuesZ.updateDB();
        Zimmer.refreshData();
        GUI_main.jDialog_Zimmer_bearbeiten.setVisible(false);
    }

    /**
     * Löscht ein Zimmer aus der Datenbank. Damit die Datenbank konsistent bleibt geschieht dies nur, wenn alle Buchungen dieses Zimmers bezahlt wurden. Alle bezahlten Buchungen des Zimers werden gelöscht.
     */
    public static void Zimmer_loeschen() {
        Zimmer neuesZ = new Zimmer(Integer.valueOf(GUI_main.jText_Zimmer_bearbeiten_Zimmernummer.getText()), false);
        if (DB_Helpers.isZimmerdeleteable(neuesZ.getID()) == true) {
            neuesZ.delDB();
            Zimmer.refreshData();
        } else {
            Meldungen.show_Dialog(ErrorMsg.msg[28], "Error", JOptionPane.ERROR_MESSAGE);
        }
        GUI_main.jDialog_Zimmer_bearbeiten.setVisible(false);
    }

    /**
     * Fügt eine neue Kategorie in der Datenbank hinzu. Die Kategorie-ID wird vom System vergeben.
     */
    public static void Kategorie_hinzu() {
        Kategorie neueK = new Kategorie();
        neueK.setBetten(Integer.valueOf(String.valueOf(GUI_main.jText_Betten.getText())));
        neueK.setAustattung(GUI_main.jText_Ausstattung.getText());
        neueK.setPreis(Double.valueOf(GUI_main.jText_Preis.getText().replaceFirst(",", ".")));
        neueK.insertAtDB();
        Zimmer.refreshData();
        Kategorie.refreshData();
        Buchung.refreshData();
    }

    /**
    * Einträge vom Dialog "Kategorie bearbeiten" werden in die Datenbank geschrieben.
    */
    public static void Kategorie_aktualisieren() {
        Kategorie bearbeitete_kategorie = new Kategorie(Integer.valueOf(String.valueOf(Kategorie.Kategorie_IDs.get(GUI_main.jCombo_Admin_Kategorie.getSelectedIndex()))), true);
        bearbeitete_kategorie.setBetten(Integer.valueOf(GUI_main.jText_Kategorie_bearbeiten_Betten.getText()));
        bearbeitete_kategorie.setAustattung(GUI_main.jText_Kategorie_bearbeiten_Ausstattung.getText());
        bearbeitete_kategorie.setPreis(Double.valueOf(GUI_main.jText_Kategorie_bearbeiten_Preis.getText().replaceFirst(",", ".")));
        bearbeitete_kategorie.updateDB();
        Zimmer.refreshData();
        Kategorie.refreshData();
        GUI_main.jDialog_Kategorie_bearbeiten.setVisible(false);
    }

    /**
     * Löscht eine Kategorie aus der Datenbank. Damit die Datenbank konsistent bleibt geschieht dies nur, wenn alle Zimmer dieser Kategorie gelöscht wurden, sonst wird eine Fehlermeldung ausgegeben.
     */
    public static void Kategorie_loeschen() {
        Kategorie bearbeitete_kategorie = new Kategorie(Integer.valueOf(String.valueOf(Kategorie.Kategorie_IDs.get(GUI_main.jCombo_Admin_Kategorie.getSelectedIndex()))), true);
        int i = 0;
        try {
            Statement stmt = DB_Backend.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM zimmer WHERE kategorie_id=" + Integer.valueOf(String.valueOf(Kategorie.Kategorie_IDs.get(GUI_main.jCombo_Admin_Kategorie.getSelectedIndex()))));
            while (rs.next()) {
                i = i + 1;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DB_Helpers.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (i == 0) {
            bearbeitete_kategorie.delDB();
            Zimmer.refreshData();
            Kategorie.refreshData();
        } else Meldungen.show_Dialog(ErrorMsg.msg[29], "Error", JOptionPane.ERROR_MESSAGE);
        GUI_main.jDialog_Kategorie_bearbeiten.setVisible(false);
    }

    /**
     * lädt die Konfiguration aus "hotel.conf"
     */
    public static void load_config() {
        XMLDecoder d = null;
        try {
            d = new XMLDecoder(new BufferedInputStream(new FileInputStream("hotel.conf")));
            GUI_main.jTextArea_Hoteladresse.setText((String) d.readObject());
            GUI_main.jText_Fax.setText((String) d.readObject());
            GUI_main.jText_Telefon.setText((String) d.readObject());
            GUI_main.jText_mail.setText((String) d.readObject());
            GUI_main.jText_web.setText((String) d.readObject());
            GUI_main.jText_rechnungen_mwst.setText((String) d.readObject());
            GUI_main.jText_rechnungen_ort.setText((String) d.readObject());
            GUI_main.jText_rechnungen_extD.setText((String) d.readObject());
            GUI_main.Model_Stornogeb.setDataVector((Vector) d.readObject(), (Vector) d.readObject());
            GUI_main.jText_DB.setText((String) d.readObject());
            GUI_main.jText_Host.setText((String) d.readObject());
            GUI_main.jText_User.setText((String) d.readObject());
            GUI_main.jText_Port.setText((String) d.readObject());
            GUI_main.jPassword_DBPW.setText(String.valueOf(d.readObject()));
            GUI_main.jText_currency.setText((String) d.readObject());
            GUI_main.jTextArea_bill_top.setText((String) d.readObject());
            GUI_main.jTextArea_bill_bottom.setText((String) d.readObject());
            ErrorMsg.init();
            String pa = (String) d.readObject();
            if (pa != null) {
                LanguageFile.applyLanguageFile(new File(pa));
            }
        } catch (FileNotFoundException ex) {
            Meldungen.show_Dialog(ErrorMsg.msg[30], "Error", JOptionPane.ERROR_MESSAGE);
        } catch (ArrayIndexOutOfBoundsException ex) {
            Meldungen.show_Dialog(ErrorMsg.msg[31], "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * @deprecated
     * 
     * speichert die Konfiguration als "hotel.conf"
     *
     * save_config() aus Main_Gonfig-Klasee benutzen!!!
     */
    public static void save_config() {
        try {
            XMLEncoder o = new XMLEncoder(new BufferedOutputStream(new FileOutputStream("hotel.conf")));
            o.writeObject(GUI_main.jTextArea_Hoteladresse.getText());
            o.writeObject(GUI_main.jText_Fax.getText());
            o.writeObject(GUI_main.jText_Telefon.getText());
            o.writeObject(GUI_main.jText_mail.getText());
            o.writeObject(GUI_main.jText_web.getText());
            o.writeObject(GUI_main.jText_rechnungen_mwst.getText());
            o.writeObject(GUI_main.jText_rechnungen_ort.getText());
            o.writeObject(GUI_main.jText_rechnungen_extD.getText());
            Vector namen = new Vector();
            namen.add(GUI_main.Model_Stornogeb.getColumnName(0));
            namen.add(GUI_main.Model_Stornogeb.getColumnName(1));
            o.writeObject(GUI_main.Model_Stornogeb.getDataVector());
            o.writeObject(namen);
            o.writeObject(GUI_main.jText_DB.getText());
            o.writeObject(GUI_main.jText_Host.getText());
            o.writeObject(GUI_main.jText_User.getText());
            o.writeObject(GUI_main.jText_Port.getText());
            char[] pw = GUI_main.jPassword_DBPW.getPassword();
            o.writeObject(String.valueOf(pw));
            o.writeObject(GUI_main.jText_currency.getText());
            o.writeObject(GUI_main.jTextArea_bill_top.getText());
            o.writeObject(GUI_main.jTextArea_bill_bottom.getText());
            if (GUI_main.choosen_langfile != null) {
                o.writeObject(GUI_main.choosen_langfile.getAbsolutePath());
            } else {
                o.writeObject(null);
            }
            o.close();
            Meldungen.show_Dialog(ErrorMsg.msg[32], "!", JOptionPane.INFORMATION_MESSAGE);
        } catch (FileNotFoundException ex) {
            Meldungen.show_Dialog(ErrorMsg.msg[33], "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
