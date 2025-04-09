package edu.mobbuzz.storage;

import edu.mobbuzz.bean.RSSChannel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import javax.microedition.rms.RecordComparator;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;

public class RSSChannelRecordStore {

    private final int MAX_RSS_CHANNEL = 100;

    private RecordStore rsRSSChannel;

    public RSSChannel[] rssChannelArr;

    private int nbRSSChannel;

    private int tempInt;

    private byte[] data;

    ByteArrayOutputStream bout;

    DataOutputStream dout;

    public RSSChannelRecordStore() {
        openRecStore();
    }

    public boolean openRecStore() {
        try {
            rsRSSChannel = RecordStore.openRecordStore("RSS", true);
            rssChannelArr = new RSSChannel[MAX_RSS_CHANNEL];
            setNbRSSChannel(rsRSSChannel.getNumRecords());
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        }
        return true;
    }

    public String getSource(String source) {
        for (tempInt = 0; tempInt < getNbRSSChannel(); tempInt++) {
            if (rssChannelArr[tempInt].getSource().equals(source)) return rssChannelArr[tempInt].getTitle();
        }
        return source;
    }

    public boolean isExistID(String source) {
        for (tempInt = 0; tempInt < getNbRSSChannel(); tempInt++) {
            if (rssChannelArr[tempInt].getSource().equals(source)) return true;
        }
        return false;
    }

    public boolean readRSRSSChannel() {
        try {
            RecordEnumeration reRSSChannel = rsRSSChannel.enumerateRecords(null, new RERSSChannelSorter(), false);
            reRSSChannel.reset();
            tempInt = 0;
            while (reRSSChannel.hasNextElement()) {
                rssChannelArr[tempInt] = new RSSChannel();
                readRSSChannel(reRSSChannel.nextRecordId(), rssChannelArr[tempInt++]);
            }
            reRSSChannel.destroy();
        } catch (RecordStoreException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void addRSSChannel(RSSChannel rssChannel) {
        if (getNbRSSChannel() < MAX_RSS_CHANNEL) {
            try {
                bout = new ByteArrayOutputStream();
                dout = new DataOutputStream(bout);
                dout.writeUTF(rssChannel.getTitle());
                dout.writeUTF(rssChannel.getSource());
                dout.writeUTF(rssChannel.getLabel());
                dout.flush();
                data = bout.toByteArray();
                bout.reset();
                rssChannelArr[nbRSSChannel] = new RSSChannel();
                rssChannelArr[getNbRSSChannel()].setRecId(rsRSSChannel.addRecord(data, 0, data.length));
                rssChannelArr[getNbRSSChannel()].setTitle(rssChannel.getTitle());
                rssChannelArr[getNbRSSChannel()].setSource(rssChannel.getSource());
                rssChannelArr[getNbRSSChannel()].setLabel(rssChannel.getLabel());
                sortDataRSSChannel(0, nbRSSChannel++);
            } catch (Exception e) {
                if (e instanceof RecordStoreFullException) {
                }
                e.printStackTrace();
            }
        } else {
        }
    }

    public void updateRSSChannel(int index, RSSChannel rssChannel) {
        try {
            dout.writeUTF(rssChannel.getTitle());
            dout.writeUTF(rssChannel.getSource());
            dout.writeUTF(rssChannel.getLabel());
            dout.flush();
            data = bout.toByteArray();
            rsRSSChannel.setRecord(rssChannel.getRecId(), data, 0, data.length);
            bout.reset();
            rssChannelArr[index].setTitle(rssChannel.getTitle());
            rssChannelArr[index].setSource(rssChannel.getSource());
            rssChannelArr[index].setLabel(rssChannel.getLabel());
            sortDataRSSChannel(0, getNbRSSChannel() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteRSSChannel(int index) {
        try {
            rsRSSChannel.deleteRecord(rssChannelArr[index].getRecId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        setNbRSSChannel(getNbRSSChannel() - 1);
        for (tempInt = index; tempInt < getNbRSSChannel(); tempInt++) {
            rssChannelArr[tempInt] = rssChannelArr[tempInt + 1];
        }
        sortDataRSSChannel(0, getNbRSSChannel() - 1);
    }

    public boolean readRSSChannel(int id, RSSChannel rssChannel) {
        data = new byte[110];
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        DataInputStream din = new DataInputStream(bin);
        if (rsRSSChannel != null) {
            try {
                rsRSSChannel.getRecord(id, data, 0);
                rssChannel.setRecId(id);
                rssChannel.setTitle(din.readUTF());
                rssChannel.setSource(din.readUTF());
                rssChannel.setLabel(din.readUTF());
                din.reset();
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    private void sortDataRSSChannel(int start, int end) {
        RSSChannel pivot, temp;
        int lo = start;
        int hi = end;
        if (lo >= hi) return; else if (lo == hi - 1) {
            if (rssChannelArr[lo].getTitle().compareTo(rssChannelArr[hi].getTitle()) > 0) {
                pivot = rssChannelArr[lo];
                rssChannelArr[lo] = rssChannelArr[hi];
                rssChannelArr[hi] = pivot;
            }
            return;
        }
        tempInt = (lo + hi) / 2;
        pivot = rssChannelArr[tempInt];
        rssChannelArr[tempInt] = rssChannelArr[hi];
        rssChannelArr[hi] = pivot;
        while (lo < hi) {
            while ((rssChannelArr[lo].getTitle().compareTo(pivot.getTitle()) <= 0) && (lo < hi)) lo++;
            while ((pivot.getTitle().compareTo(rssChannelArr[hi].getTitle()) <= 0) && (lo < hi)) hi--;
            if (lo < hi) {
                temp = rssChannelArr[lo];
                rssChannelArr[lo] = rssChannelArr[hi];
                rssChannelArr[hi] = temp;
            }
        }
        rssChannelArr[end] = rssChannelArr[hi];
        rssChannelArr[hi] = pivot;
        sortDataRSSChannel(start, lo - 1);
        sortDataRSSChannel(hi + 1, end);
    }

    public void closeRecStore() {
        try {
            if (rsRSSChannel != null) rsRSSChannel.closeRecordStore();
        } catch (RecordStoreException e) {
            e.printStackTrace();
        }
    }

    public int getNbRSSChannel() {
        return nbRSSChannel;
    }

    public void setNbRSSChannel(int nbRSSChannel) {
        this.nbRSSChannel = nbRSSChannel;
    }
}

class RERSSChannelSorter implements RecordComparator {

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
