package edu.mobbuzz.storage;

import edu.mobbuzz.bean.Message;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import javax.microedition.rms.RecordComparator;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;

public class OutboxRecordStore {

    private final int MAX_MESSAGE = 100;

    private RecordStore rsMessage;

    public Message[] messageArr;

    private int nbMessage;

    private int tempInt;

    private byte[] data;

    static ByteArrayOutputStream bout;

    static DataOutputStream dout;

    public OutboxRecordStore() {
        openRecStore();
    }

    public boolean openRecStore() {
        try {
            rsMessage = RecordStore.openRecordStore("OUTBOX", true);
            messageArr = new Message[MAX_MESSAGE];
            setNbMessage(rsMessage.getNumRecords());
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        }
        return true;
    }

    public boolean readRSMessage() {
        try {
            RecordEnumeration reMessage = rsMessage.enumerateRecords(null, new REMessageSorter(), false);
            reMessage.reset();
            tempInt = 0;
            while (reMessage.hasNextElement()) {
                messageArr[tempInt] = new Message();
                readMessage(reMessage.nextRecordId(), messageArr[tempInt++]);
            }
            reMessage.destroy();
        } catch (RecordStoreException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean readMessage(int id, Message message) {
        try {
            data = new byte[rsMessage.getRecordSize(id) + 4];
            ByteArrayInputStream bin = new ByteArrayInputStream(data);
            DataInputStream din = new DataInputStream(bin);
            if (rsMessage != null) {
                try {
                    rsMessage.getRecord(id, data, 0);
                    message.setRecId(id);
                    message.setTitle(din.readUTF());
                    message.setDescription(din.readUTF());
                    message.setLink(din.readUTF());
                    message.setPubdate(din.readUTF());
                    message.setSource(din.readUTF());
                    message.setFrom(din.readUTF());
                    message.setDate(din.readUTF());
                    message.setType(din.readInt());
                    message.setRead(din.readBoolean());
                    din.reset();
                } catch (Exception e) {
                    return false;
                }
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void addMessage(Message message) {
        if (getNbMessage() < MAX_MESSAGE) {
            try {
                bout = new ByteArrayOutputStream();
                dout = new DataOutputStream(bout);
                dout.writeUTF(message.getTitle());
                dout.writeUTF(message.getDescription());
                dout.writeUTF(message.getLink());
                dout.writeUTF(message.getPubdate());
                dout.writeUTF(message.getSource());
                dout.writeUTF(message.getFrom());
                dout.writeUTF(message.getDate());
                dout.writeInt(message.getType());
                dout.writeBoolean(message.isRead());
                dout.flush();
                data = bout.toByteArray();
                bout.reset();
                messageArr[nbMessage] = new Message();
                messageArr[nbMessage].setRecId(rsMessage.addRecord(data, 0, data.length));
                messageArr[nbMessage].setTitle(message.getTitle());
                messageArr[nbMessage].setDescription(message.getDescription());
                messageArr[nbMessage].setLink(message.getLink());
                messageArr[nbMessage].setPubdate(message.getPubdate());
                messageArr[nbMessage].setSource(message.getSource());
                messageArr[nbMessage].setFrom(message.getFrom());
                messageArr[nbMessage].setDate(message.getDate());
                messageArr[nbMessage].setType(message.getType());
                messageArr[nbMessage].setRead(message.isRead());
            } catch (Exception e) {
                if (e instanceof RecordStoreFullException) {
                }
                e.printStackTrace();
            }
        } else {
        }
    }

    public void updateMessage(int index, Message message) {
        try {
            dout.writeUTF(message.getTitle());
            dout.writeUTF(message.getDescription());
            dout.writeUTF(message.getLink());
            dout.writeUTF(message.getPubdate());
            dout.writeUTF(message.getSource());
            dout.writeUTF(message.getFrom());
            dout.writeUTF(message.getDate());
            dout.writeInt(message.getType());
            dout.writeBoolean(message.isRead());
            dout.flush();
            data = bout.toByteArray();
            rsMessage.setRecord(message.getRecId(), data, 0, data.length);
            bout.reset();
            messageArr[index].setTitle(message.getTitle());
            messageArr[index].setDescription(message.getDescription());
            messageArr[index].setLink(message.getLink());
            messageArr[index].setPubdate(message.getPubdate());
            messageArr[index].setSource(message.getSource());
            messageArr[index].setFrom(message.getFrom());
            messageArr[index].setDate(message.getDate());
            messageArr[index].setType(message.getType());
            messageArr[index].setRead(message.isRead());
            sortDataMessage(0, getNbMessage() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteMessage(int index) {
        try {
            rsMessage.deleteRecord(messageArr[index].getRecId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        setNbMessage(getNbMessage() - 1);
        for (tempInt = index; tempInt < getNbMessage(); tempInt++) {
            messageArr[tempInt] = messageArr[tempInt + 1];
        }
        sortDataMessage(0, getNbMessage() - 1);
    }

    private void sortDataMessage(int start, int end) {
        Message pivot, temp;
        int lo = start;
        int hi = end;
        if (lo >= hi) return; else if (lo == hi - 1) {
            if (messageArr[lo].getDate().compareTo(messageArr[hi].getDate()) > 0) {
                pivot = messageArr[lo];
                messageArr[lo] = messageArr[hi];
                messageArr[hi] = pivot;
            }
            return;
        }
        tempInt = (lo + hi) / 2;
        pivot = messageArr[tempInt];
        messageArr[tempInt] = messageArr[hi];
        messageArr[hi] = pivot;
        while (lo < hi) {
            while ((messageArr[lo].getDate().compareTo(pivot.getDate()) <= 0) && (lo < hi)) lo++;
            while ((pivot.getDate().compareTo(messageArr[hi].getDate()) <= 0) && (lo < hi)) hi--;
            if (lo < hi) {
                temp = messageArr[lo];
                messageArr[lo] = messageArr[hi];
                messageArr[hi] = temp;
            }
        }
        messageArr[end] = messageArr[hi];
        messageArr[hi] = pivot;
        sortDataMessage(start, lo - 1);
        sortDataMessage(hi + 1, end);
    }

    public void closeRecStore() {
        try {
            if (rsMessage != null) rsMessage.closeRecordStore();
        } catch (RecordStoreException e) {
            e.printStackTrace();
        }
    }

    public int getNbMessage() {
        return nbMessage;
    }

    public void setNbMessage(int nbMessage) {
        this.nbMessage = nbMessage;
    }
}

class REMessageOutboxSorter implements RecordComparator {

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
