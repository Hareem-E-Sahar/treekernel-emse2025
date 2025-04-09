package edu.mobbuzz.storage;

import edu.mobbuzz.bean.Contact;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import javax.microedition.rms.RecordComparator;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;

/**
 *
 * @author penyihir kecil
 */
public class ContactRecordStore {

    private final int MAX_CONTACT = 100;

    private RecordStore rsContact;

    public Contact[] contactArr;

    private int nbContact;

    private int tempInt;

    private byte[] data;

    ByteArrayOutputStream bout;

    DataOutputStream dout;

    public ContactRecordStore() {
        openRecStore();
    }

    public boolean openRecStore() {
        try {
            rsContact = RecordStore.openRecordStore("CONTACT", true);
            contactArr = new Contact[MAX_CONTACT];
            setNbContact(rsContact.getNumRecords());
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        }
        return true;
    }

    public String getContactName(String contactID) {
        for (tempInt = 0; tempInt < getNbContact(); tempInt++) {
            if (contactArr[tempInt].getId().equals(contactID)) return contactArr[tempInt].getName();
        }
        return contactID;
    }

    public boolean isExistID(String contactID) {
        for (tempInt = 0; tempInt < getNbContact(); tempInt++) {
            if (contactArr[tempInt].getId().equals(contactID)) return true;
        }
        return false;
    }

    public boolean readRSContact() {
        try {
            RecordEnumeration reContact = rsContact.enumerateRecords(null, new REContactSorter(), false);
            reContact.reset();
            tempInt = 0;
            while (reContact.hasNextElement()) {
                contactArr[tempInt] = new Contact();
                readContact(reContact.nextRecordId(), contactArr[tempInt++]);
            }
            reContact.destroy();
        } catch (RecordStoreException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void addContact(Contact contact) {
        if (getNbContact() < MAX_CONTACT) {
            try {
                bout = new ByteArrayOutputStream();
                dout = new DataOutputStream(bout);
                dout.writeUTF(contact.getName());
                dout.writeUTF(contact.getId());
                dout.writeInt(contact.getSex());
                dout.flush();
                data = bout.toByteArray();
                bout.reset();
                contactArr[nbContact] = new Contact();
                contactArr[getNbContact()].setRecId(rsContact.addRecord(data, 0, data.length));
                contactArr[getNbContact()].setName(contact.getName());
                contactArr[getNbContact()].setId(contact.getId());
                contactArr[getNbContact()].setSex(contact.getSex());
                sortDataContact(0, nbContact++);
            } catch (Exception e) {
                if (e instanceof RecordStoreFullException) {
                }
                e.printStackTrace();
            }
        } else {
        }
    }

    public void updateContact(int index, Contact contact) {
        try {
            dout.writeUTF(contact.getName());
            dout.writeUTF(contact.getId());
            dout.writeInt(contact.getSex());
            dout.flush();
            data = bout.toByteArray();
            rsContact.setRecord(contact.getRecId(), data, 0, data.length);
            bout.reset();
            contactArr[index].setName(contact.getName());
            contactArr[index].setId(contact.getId());
            contactArr[index].setSex(contact.getSex());
            sortDataContact(0, getNbContact() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteContact(int index) {
        try {
            rsContact.deleteRecord(contactArr[index].getRecId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        setNbContact(getNbContact() - 1);
        for (tempInt = index; tempInt < getNbContact(); tempInt++) {
            contactArr[tempInt] = contactArr[tempInt + 1];
        }
        sortDataContact(0, getNbContact() - 1);
    }

    public boolean readContact(int id, Contact contact) {
        data = new byte[110];
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        DataInputStream din = new DataInputStream(bin);
        if (rsContact != null) {
            try {
                rsContact.getRecord(id, data, 0);
                contact.setRecId(id);
                contact.setName(din.readUTF());
                contact.setId(din.readUTF());
                contact.setSex(din.readInt());
                din.reset();
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    private void sortDataContact(int start, int end) {
        Contact pivot, temp;
        int lo = start;
        int hi = end;
        if (lo >= hi) return; else if (lo == hi - 1) {
            if (contactArr[lo].getName().compareTo(contactArr[hi].getName()) > 0) {
                pivot = contactArr[lo];
                contactArr[lo] = contactArr[hi];
                contactArr[hi] = pivot;
            }
            return;
        }
        tempInt = (lo + hi) / 2;
        pivot = contactArr[tempInt];
        contactArr[tempInt] = contactArr[hi];
        contactArr[hi] = pivot;
        while (lo < hi) {
            while ((contactArr[lo].getName().compareTo(pivot.getName()) <= 0) && (lo < hi)) lo++;
            while ((pivot.getName().compareTo(contactArr[hi].getName()) <= 0) && (lo < hi)) hi--;
            if (lo < hi) {
                temp = contactArr[lo];
                contactArr[lo] = contactArr[hi];
                contactArr[hi] = temp;
            }
        }
        contactArr[end] = contactArr[hi];
        contactArr[hi] = pivot;
        sortDataContact(start, lo - 1);
        sortDataContact(hi + 1, end);
    }

    public void closeRecStore() {
        try {
            if (rsContact != null) rsContact.closeRecordStore();
        } catch (RecordStoreException e) {
            e.printStackTrace();
        }
    }

    public int getNbContact() {
        return nbContact;
    }

    public void setNbContact(int nbContact) {
        this.nbContact = nbContact;
    }
}

class REContactSorter implements RecordComparator {

    public int compare(byte[] rec1, byte[] rec2) {
        ByteArrayInputStream bin = new ByteArrayInputStream(rec1);
        DataInputStream din = new DataInputStream(bin);
        String name1, name2;
        try {
            name1 = din.readUTF();
            bin = new ByteArrayInputStream(rec2);
            din = new DataInputStream(bin);
            name2 = din.readUTF();
            int cmp = name1.compareTo(name2);
            if (cmp != 0) return (cmp < 0 ? PRECEDES : FOLLOWS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return EQUIVALENT;
    }
}

;
