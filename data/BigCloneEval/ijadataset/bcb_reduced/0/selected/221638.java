package com.yekmer.creasus.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.yekmer.creasus.client.model.CreasusUser;
import com.yekmer.creasus.client.model.Entry;

public class CronMail2Jobs extends HttpServlet {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String month = "";
        String year = "";
        GregorianCalendar calendar = new GregorianCalendar();
        month = String.valueOf(calendar.get(GregorianCalendar.MONTH));
        year = String.valueOf(calendar.get(GregorianCalendar.YEAR));
        Date today = new Date();
        if (today.getDate() == 2) {
            String msgBody = "Dear admin of CVM Turkey Team" + "<br /><br />Please find the KPI data of month " + month + " - " + year + " .<br /><br />" + "Best wishes.<br /><br />";
            String html = "<table width=\"400px\"><tr><td></td><td></td><td><b>KOL visit</b></td><td><b>Presentation</b></td><td><b>Training</b></td></tr>";
            List<CreasusUser> creasusUsersList = new ArrayList<CreasusUser>();
            List<Entry> entryList = new ArrayList<Entry>();
            int totalKolVisit = 0;
            int totalPresentation = 0;
            int totalTraining = 0;
            creasusUsersList = getCreasusUserList();
            Collections.sort(creasusUsersList, new Comparator() {

                public int compare(Object o1, Object o2) {
                    CreasusUser p1 = (CreasusUser) o1;
                    CreasusUser p2 = (CreasusUser) o2;
                    return p1.getName().compareToIgnoreCase(p2.getName());
                }
            });
            entryList = getEntryList();
            for (Iterator iterator = creasusUsersList.iterator(); iterator.hasNext(); ) {
                CreasusUser creasusUser = (CreasusUser) iterator.next();
                if (creasusUser.isActive()) {
                    String name = "";
                    String surname = "";
                    if (creasusUser.getName() != null) {
                        name = creasusUser.getName();
                    }
                    if (creasusUser.getSurname() != null) {
                        surname = creasusUser.getSurname();
                    }
                    int kolvisit = 0;
                    int presentation = 0;
                    int training = 0;
                    List<Entry> usersEntryList = getUsersEntries(creasusUser.getEmail(), entryList);
                    for (Iterator iterator2 = usersEntryList.iterator(); iterator2.hasNext(); ) {
                        Entry entry = (Entry) iterator2.next();
                        if (entry.getMode() == 0) {
                            kolvisit = entry.getMonthlyValues()[today.getMonth() - 1];
                            totalKolVisit += kolvisit;
                        } else if (entry.getMode() == 1) {
                            presentation = entry.getMonthlyValues()[today.getMonth() - 1];
                            totalPresentation += presentation;
                        } else if (entry.getMode() == 2) {
                            training = entry.getMonthlyValues()[today.getMonth() - 1];
                            totalTraining += training;
                        }
                    }
                    html += "<tr><td><b>" + name + "</b></td><td><b>" + surname + "</b></td><td>" + kolvisit + "</td><td>" + presentation + "</td><td>" + training + "</td></tr>";
                }
            }
            html += "<tr><td></td><td><b>TOTAL</b></td><td><b>" + totalKolVisit + "</b></td><td><b>" + totalPresentation + "</b></td><td><b>" + totalTraining + "</b></td></tr></table>";
            msgBody += html;
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            try {
                Message msg = new MimeMessage(session);
                msg.setFrom(new InternetAddress("cvmtmt@gmail.com", "CVM Turkey Medical Team"));
                msg.addRecipient(Message.RecipientType.TO, new InternetAddress("kemal.kendir@novartis.com", "Kemal Kendir"));
                msg.addRecipient(Message.RecipientType.BCC, new InternetAddress("mehmet.berktas@gmail.com", "Mehmet Berktas"));
                msg.addRecipient(Message.RecipientType.BCC, new InternetAddress("yekmerdogan@gmail.com", "hop hop"));
                msg.setSubject("KPI Data - " + month + " " + year + " ADMIN SENT");
                Multipart mp = new MimeMultipart();
                MimeBodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(msgBody, "text/html");
                mp.addBodyPart(htmlPart);
                msg.setContent(mp);
                Transport.send(msg);
            } catch (AddressException e) {
            } catch (MessagingException e) {
            }
        }
    }

    private List<Entry> getUsersEntries(String email, List<Entry> totalEntryList) {
        List<Entry> entryList = new ArrayList<Entry>();
        for (Iterator iterator = totalEntryList.iterator(); iterator.hasNext(); ) {
            Entry entry = (Entry) iterator.next();
            if (entry.getEmail().equalsIgnoreCase(email)) {
                entryList.add(entry);
            }
        }
        return entryList;
    }

    public List<CreasusUser> getCreasusUserList() {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        List<CreasusUser> incoming;
        ArrayList<CreasusUser> outgoingList = new ArrayList<CreasusUser>();
        try {
            Query query = pm.newQuery("select from " + CreasusUser.class.getName());
            incoming = (List<CreasusUser>) query.execute();
            for (Iterator iterator = incoming.iterator(); iterator.hasNext(); ) {
                CreasusUser creasusUser = (CreasusUser) iterator.next();
                CreasusUser tmp = new CreasusUser();
                tmp.setEmail(creasusUser.getEmail());
                tmp.setId(creasusUser.getId());
                tmp.setMode(creasusUser.getMode());
                tmp.setName(creasusUser.getName());
                tmp.setPassword(creasusUser.getPassword());
                tmp.setSecondaryEmail(creasusUser.getSecondaryEmail());
                tmp.setSurname(creasusUser.getSurname());
                tmp.setUserName(creasusUser.getUserName());
                tmp.setActive(creasusUser.isActive());
                outgoingList.add(tmp);
            }
        } finally {
            pm.close();
        }
        return outgoingList;
    }

    public List<Entry> getEntryList() {
        PersistenceManager pm = PMF.get().getPersistenceManager();
        String year = "";
        GregorianCalendar calendar = new GregorianCalendar();
        year = String.valueOf(calendar.get(GregorianCalendar.YEAR));
        List<Entry> incomingEntryList;
        ArrayList<Entry> outgoingEntryList = new ArrayList<Entry>();
        try {
            Query query = pm.newQuery("select from " + Entry.class.getName() + " where year == " + year);
            incomingEntryList = (List<Entry>) query.execute();
            for (Iterator iterator = incomingEntryList.iterator(); iterator.hasNext(); ) {
                Entry entry = (Entry) iterator.next();
                Entry tmp = new Entry();
                tmp.setEmail(entry.getEmail());
                tmp.setId(entry.getId());
                tmp.setMode(entry.getMode());
                tmp.setMonthlyValues(entry.getMonthlyValues());
                tmp.setNeZamanKaydedildi(entry.getNeZamanKaydedildi());
                tmp.setYear(entry.getYear());
                outgoingEntryList.add(tmp);
            }
        } finally {
            pm.close();
        }
        return outgoingEntryList;
    }
}
