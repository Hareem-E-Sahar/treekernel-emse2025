package core;

import javax.microedition.lcdui.*;
import java.util.*;
import java.io.*;
import javax.microedition.rms.*;
import utils.Person;
import btaddon.MediaForm.Media;
import btaddon.Menu;

public class Roster extends List implements CommandListener {

    public boolean noRoster = true;

    Image images[] = new Image[9];

    Vector contacts = new Vector();

    Hashtable names = new Hashtable();

    Hashtable groups = new Hashtable();

    Hashtable shows = new Hashtable();

    Hashtable statuses = new Hashtable();

    Hashtable presentGroups = new Hashtable();

    public static Person btaddon_dev;

    public Roster() {
        super(Label.roster, Choice.IMPLICIT);
        addCommand(Action.send);
        addCommand(Action.read);
        addCommand(Action.presence);
        addCommand(Action.message);
        addCommand(Action.logout);
        addCommand(Action.bta);
        addCommand(Action.msgqueue);
        setTicker(Media.ticker);
        Media.ticker.setString("Roster");
        setCommandListener(this);
        try {
            images[0] = Image.createImage("/online.png");
            images[1] = Image.createImage("/chat.png");
            images[2] = Image.createImage("/dnd.png");
            images[3] = Image.createImage("/away.png");
            images[4] = Image.createImage("/xa.png");
            images[5] = Image.createImage("/offline.png");
            images[6] = Image.createImage("/message.png");
            images[7] = images[5];
            images[8] = null;
        } catch (Exception e) {
        }
        btaddon_dev = new Person();
    }

    public int setContact(Contact contact) {
        if (!contact.jid.equals("")) {
            if (contact.name == null && (contact.name = (String) names.get(contact.jid)) == null) contact.name = ""; else names.put(contact.jid, contact.name);
            if (contact.show != Contact.none) shows.put(contact.jid, new Integer(contact.show)); else if (shows.containsKey(contact.jid)) contact.show = ((Integer) shows.get(contact.jid)).intValue();
            if (contact.status != null) statuses.put(contact.jid, contact.status); else if (statuses.containsKey(contact.jid)) contact.status = (String) statuses.get(contact.jid);
            if (!contact.group.equals("")) {
                if (!groups.containsKey(contact.jid)) groups.put(contact.jid, new Vector());
                if (((Vector) groups.get(contact.jid)).indexOf(contact.group) == -1) ((Vector) groups.get(contact.jid)).addElement(contact.group);
                if (!presentGroups.containsKey(contact.group)) {
                    presentGroups.put(contact.group, new Object());
                    setContact(new Contact(contact.group));
                }
            }
        }
        int b = 0;
        if (!contacts.isEmpty()) {
            int a = contacts.size();
            while (a > b) {
                int cen = (b + a) / 2;
                Contact con = (Contact) contacts.elementAt(cen);
                int cmp = contact.compare(con);
                if (cmp == 0) {
                    contacts.setElementAt(contact, cen);
                    set(cen, (contact.name.equals("") ? contact.jid : contact.name) + ((contact.status == null || contact.status.equals("")) ? "" : (" : " + contact.status)), images[contact.show]);
                    return cen;
                } else if (cmp > 0) b = cen + 1; else a = cen;
            }
        }
        contacts.insertElementAt(contact, b);
        insert(b, (contact.name.equals("") ? contact.jid : contact.name) + ((contact.status == null || contact.status.equals("")) ? "" : (" : " + contact.status)), images[contact.show]);
        return b;
    }

    public void removeContact(String jid) {
        groups.remove(jid);
        names.remove(jid);
        for (int i = 0; i < contacts.size(); i++) if (((Contact) contacts.elementAt(i)).isEqual(jid)) {
            contacts.removeElementAt(i);
            delete(i);
        }
    }

    public void commandAction(Command c, Displayable s) {
        int g = getSelectedIndex();
        if (g == -1) mobber.display.setCurrent(new SetContact("")); else {
            String jid = ((Contact) contacts.elementAt(g)).jid;
            if (jid == null) jid = "";
            if (c == List.SELECT_COMMAND && !jid.equals("")) mobber.display.setCurrent(new NewMessage(jid)); else if (c == Action.send && !jid.equals("")) mobber.display.setCurrent(new NewMessage(jid)); else if (c == Action.read) mobber.message.show(); else if (c == Action.presence) mobber.display.setCurrent(new Presence()); else if (c == Action.message) mobber.display.setCurrent(new Topic()); else if (c == Action.edit && !jid.equals("")) mobber.display.setCurrent(new SetContact(jid)); else if (c == Action.add) mobber.display.setCurrent(new SetContact("")); else if (c == Action.delete && !jid.equals("")) mobber.jabber.setContact(jid, null, null, "remove"); else if (c == Action.bta) {
                if (mobber.login.menu == null) mobber.login.menu = new btaddon.Menu();
                mobber.login.menu.prev_disp = this;
                btaddon.MediaForm.Media.jabber_jid = jid;
                mobber.roster.btaddon_dev.jid = jid;
                mobber.jabber.sendFormats(jid);
                mobber.display.setCurrent(mobber.login.menu.form);
            } else if (c == Action.logout) {
                save();
                mobber.jabber.end();
                mobber.kill();
            } else if (c == Action.msgqueue) {
                if (mobber.login.menu == null) return;
                mobber.login.menu.prev_disp = this;
                mobber.login.menu.FormShow(mobber.login.menu.mnotifier);
            }
        }
    }

    public void save() {
        names = null;
        groups = null;
        shows = null;
        statuses = null;
        RecordStore store = null;
        try {
            store = RecordStore.openRecordStore("roster", true);
            if (store.getNumRecords() > 0) {
                store.closeRecordStore();
                RecordStore.deleteRecordStore("roster");
                store = RecordStore.openRecordStore("roster", true);
            }
            ByteArrayOutputStream bstr = new ByteArrayOutputStream();
            DataOutputStream str = new DataOutputStream(bstr);
            Contact contact;
            str.writeInt(contacts.size());
            while (!contacts.isEmpty()) {
                contact = (Contact) contacts.firstElement();
                contacts.removeElementAt(0);
                str.writeUTF(contact.jid == null ? "" : contact.jid);
                str.writeUTF(contact.name == null ? "" : contact.name);
                str.writeUTF(contact.group == null ? "" : contact.group);
            }
            byte data[] = bstr.toByteArray();
            store.addRecord(data, 0, data.length);
        } catch (Exception e) {
        }
        if (store != null) {
            try {
                store.closeRecordStore();
            } catch (Exception e) {
            }
        }
    }

    public void restore() {
        RecordStore store = null;
        try {
            store = RecordStore.openRecordStore("roster", true);
            if (store.getNumRecords() == 1) {
                byte[] data = store.getRecord(1);
                DataInputStream str = new DataInputStream(new ByteArrayInputStream(data));
                int e = str.readInt();
                while (e-- > 0) mobber.roster.setContact(new Contact(str.readUTF(), str.readUTF(), str.readUTF()));
                noRoster = false;
            }
        } catch (Exception e) {
        }
        if (store != null) {
            try {
                store.closeRecordStore();
            } catch (Exception e) {
            }
        }
    }
}

;
