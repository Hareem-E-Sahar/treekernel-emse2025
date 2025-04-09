package globali;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;

public class jcFunzioniWR {

    public static java.util.ArrayList<Object[]> CACHE_CATEGORIE_WR = new java.util.ArrayList<Object[]>();

    public static java.util.ArrayList<Object[]> CACHE_AMMINISTRATORI_WR = new java.util.ArrayList<Object[]>();

    public static void caricaCategorieWR(int tipoID, JComboBox catWR) {
        boolean flag = false;
        int trovato = 0;
        catWR.removeAllItems();
        if (CACHE_CATEGORIE_WR.isEmpty()) {
            try {
                jcPostgreSQL.queryDB("SELECT wr_categorieid, nomecat, internet FROM wr_categorie WHERE attivo=TRUE ORDER BY nomecat");
                while (jcPostgreSQL.query.next()) {
                    CACHE_CATEGORIE_WR.add(new Object[] { jcPostgreSQL.query.getInt("wr_categorieid"), jcPostgreSQL.query.getString("nomecat").trim() + (jcPostgreSQL.query.getBoolean("internet") ? "" : " **") });
                }
            } catch (SQLException ex) {
                Logger.getLogger(jcFunzioniWR.class.getName()).log(Level.SEVERE, null, ex);
                jcFunzioni.erroreSQL(ex.toString());
            }
        }
        Object temp[] = null;
        for (int i = 0; i < CACHE_CATEGORIE_WR.size(); i++) {
            temp = (Object[]) CACHE_CATEGORIE_WR.get(i);
            catWR.addItem(temp);
            if ((Integer) temp[0] == tipoID) flag = true;
            if (!flag) trovato++;
        }
        if (flag) catWR.setSelectedIndex(trovato); else catWR.setSelectedIndex(0);
    }

    public static void caricaAmministratoriWR(int admID, JComboBox admin) {
        boolean flag = false;
        int trovato = 0;
        admin.removeAllItems();
        if (CACHE_AMMINISTRATORI_WR.isEmpty()) {
            try {
                jcPostgreSQL.queryDB("SELECT adminid, nome, cognome FROM administrators WHERE attivo=TRUE AND can_work_wr=TRUE ORDER BY nome, cognome");
                CACHE_AMMINISTRATORI_WR.add(new Object[] { -1, "- Assegnazione AUTOMATICA sistema ticket -" });
                while (jcPostgreSQL.query.next()) {
                    CACHE_AMMINISTRATORI_WR.add(new Object[] { jcPostgreSQL.query.getInt("adminid"), jcPostgreSQL.query.getString("nome").trim() + " " + jcPostgreSQL.query.getString("cognome").trim() });
                }
            } catch (SQLException ex) {
                Logger.getLogger(jcFunzioniWR.class.getName()).log(Level.SEVERE, null, ex);
                jcFunzioni.erroreSQL(ex.toString());
            }
        }
        Object temp[] = null;
        for (int i = 0; i < CACHE_AMMINISTRATORI_WR.size(); i++) {
            temp = (Object[]) CACHE_AMMINISTRATORI_WR.get(i);
            admin.addItem(temp);
            if ((Integer) temp[0] == admID) flag = true;
            if (!flag) trovato++;
        }
        if (flag) admin.setSelectedIndex(trovato); else admin.setSelectedIndex(0);
    }

    public static void caricaStatiWr(int wrID, JComboBox menu, boolean caricaChiusi) {
        try {
            String carChiusi = caricaChiusi ? "" : " AND is_close=FALSE ";
            jcPostgreSQL.queryDB("SELECT stato_wr_id, stato, is_close, is_nulla, predefinito FROM stato_wr WHERE attivo=TRUE " + carChiusi + " ORDER BY ordina, stato");
            menu.removeAllItems();
            Object stato[] = null;
            boolean flag = false, flagPredefinito = false;
            int trovato = 0, predefinito = 0;
            while (jcPostgreSQL.query.next()) {
                stato = new Object[4];
                stato[0] = jcPostgreSQL.query.getInt("stato_wr_id");
                stato[1] = jcPostgreSQL.query.getString("stato").trim();
                stato[2] = jcPostgreSQL.query.getBoolean("is_close");
                stato[3] = jcPostgreSQL.query.getBoolean("is_nulla");
                if (jcPostgreSQL.query.getBoolean("predefinito")) flagPredefinito = true;
                if (!flagPredefinito) predefinito++;
                if (jcPostgreSQL.query.getInt("stato_wr_id") == wrID) flag = true;
                if (!flag) trovato++;
                menu.addItem(stato);
            }
            if (flag) menu.setSelectedIndex(trovato); else {
                if (flagPredefinito) menu.setSelectedIndex(predefinito); else menu.setSelectedIndex(0);
            }
        } catch (SQLException ex) {
            Logger.getLogger(jcFunzioni.class.getName()).log(Level.SEVERE, null, ex);
            jcFunzioni.erroreSQL(ex.toString());
        }
    }

    public static void inviaAppuntamentoPerCalendarioTramiteEmail(int wrID) throws Exception {
        try {
            String from = "noreply@jmagazzino.org";
            String to = "xx@xx.com";
            java.util.Properties prop = new java.util.Properties();
            prop.put("mail.smtp.host", "mailhost");
            javax.mail.Session session = javax.mail.Session.getDefaultInstance(prop, null);
            javax.mail.internet.MimeMessage message = new javax.mail.internet.MimeMessage(session);
            message.addHeaderLine("method=REQUEST");
            message.addHeaderLine("charset=UTF-8");
            message.addHeaderLine("component=VEVENT");
            message.setFrom(new javax.mail.internet.InternetAddress(from));
            message.addRecipient(javax.mail.Message.RecipientType.TO, new javax.mail.internet.InternetAddress(to));
            message.setSubject("jMagazzino - Invio appuntamento WR - tramite javaMail");
            StringBuffer sb = new StringBuffer();
            StringBuffer buffer = sb.append("BEGIN:VCALENDAR\n" + "PRODID:-//Microsoft Corporation//Outlook 9.0 MIMEDIR//EN\n" + "VERSION:2.0\n" + "METHOD:REQUEST\n" + "BEGIN:VEVENT\n" + "ATTENDEE;ROLE=REQ-PARTICIPANT;RSVP=TRUE:MAILTO:xx@xx.com\n" + "ORGANIZER:MAILTO:xx@xx.com\n" + "DTSTART:20051208T053000Z\n" + "DTEND:20051208T060000Z\n" + "LOCATION:Conference room\n" + "TRANSP:OPAQUE\n" + "SEQUENCE:0\n" + "UID:WR-APPUNTAMENTO-ID-" + Integer.toString(wrID) + "\n" + "DTSTAMP:20051206T120102Z\n" + "CATEGORIES:Meeting\n" + "DESCRIPTION:This the description of the meeting.\n\n" + "SUMMARY:Richiesta appuntamento WR\n" + "PRIORITY:5\n" + "CLASS:PUBLIC\n" + "BEGIN:VALARM\n" + "TRIGGER:PT1440M\n" + "ACTION:DISPLAY\n" + "DESCRIPTION:Reminder\n" + "END:VALARM\n" + "END:VEVENT\n" + "END:VCALENDAR");
            javax.mail.BodyPart messageBodyPart = new javax.mail.internet.MimeBodyPart();
            messageBodyPart.setHeader("Content-Class", "urn:content-classes:calendarmessage");
            messageBodyPart.setHeader("Content-ID", "calendar_message");
            messageBodyPart.setDataHandler(new javax.activation.DataHandler(new javax.mail.util.ByteArrayDataSource(buffer.toString(), "text/calendar")));
            javax.mail.Multipart multipart = new javax.mail.internet.MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            message.setContent(multipart);
            javax.mail.Transport.send(message);
        } catch (javax.mail.MessagingException me) {
            me.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
