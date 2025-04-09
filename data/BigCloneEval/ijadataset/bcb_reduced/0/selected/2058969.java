package edu.mobbuzz.storage;

import edu.mobbuzz.bean.Category;
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
public class CategoryRecordStore {

    private final int MAX_CONTACT = 100;

    private RecordStore rsCategory;

    public Category[] categoryArr;

    private int nbCategory;

    private int tempInt;

    private byte[] data;

    ByteArrayOutputStream bout;

    DataOutputStream dout;

    public CategoryRecordStore() {
        openRecStore();
    }

    public boolean openRecStore() {
        try {
            rsCategory = RecordStore.openRecordStore("CATEGORY", true);
            categoryArr = new Category[MAX_CONTACT];
            setNbCategory(rsCategory.getNumRecords());
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        }
        return true;
    }

    public boolean readRSCategory() {
        try {
            RecordEnumeration reCategory = rsCategory.enumerateRecords(null, new RECategorySorter(), false);
            reCategory.reset();
            tempInt = 0;
            while (reCategory.hasNextElement()) {
                categoryArr[tempInt] = new Category();
                readCategory(reCategory.nextRecordId(), categoryArr[tempInt++]);
            }
            reCategory.destroy();
        } catch (RecordStoreException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void addCategory(Category contact) {
        if (getNbCategory() < MAX_CONTACT) {
            try {
                bout = new ByteArrayOutputStream();
                dout = new DataOutputStream(bout);
                dout.writeUTF(contact.getLabel());
                dout.flush();
                data = bout.toByteArray();
                bout.reset();
                categoryArr[nbCategory] = new Category();
                categoryArr[getNbCategory()].setRecId(rsCategory.addRecord(data, 0, data.length));
                categoryArr[getNbCategory()].setLabel(contact.getLabel());
                sortDataCategory(0, nbCategory++);
            } catch (Exception e) {
                if (e instanceof RecordStoreFullException) {
                }
                e.printStackTrace();
            }
        } else {
        }
    }

    public void updateCategory(int index, Category contact) {
        try {
            dout.writeUTF(contact.getLabel());
            dout.flush();
            data = bout.toByteArray();
            rsCategory.setRecord(contact.getRecId(), data, 0, data.length);
            bout.reset();
            categoryArr[index].setLabel(contact.getLabel());
            sortDataCategory(0, getNbCategory() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteCategory(int index) {
        try {
            rsCategory.deleteRecord(categoryArr[index].getRecId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        setNbCategory(getNbCategory() - 1);
        for (tempInt = index; tempInt < getNbCategory(); tempInt++) {
            categoryArr[tempInt] = categoryArr[tempInt + 1];
        }
        sortDataCategory(0, getNbCategory() - 1);
    }

    public boolean readCategory(int id, Category contact) {
        data = new byte[110];
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        DataInputStream din = new DataInputStream(bin);
        if (rsCategory != null) {
            try {
                rsCategory.getRecord(id, data, 0);
                contact.setRecId(id);
                contact.setLabel(din.readUTF());
                din.reset();
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    private void sortDataCategory(int start, int end) {
        Category pivot, temp;
        int lo = start;
        int hi = end;
        if (lo >= hi) return; else if (lo == hi - 1) {
            if (categoryArr[lo].getLabel().compareTo(categoryArr[hi].getLabel()) > 0) {
                pivot = categoryArr[lo];
                categoryArr[lo] = categoryArr[hi];
                categoryArr[hi] = pivot;
            }
            return;
        }
        tempInt = (lo + hi) / 2;
        pivot = categoryArr[tempInt];
        categoryArr[tempInt] = categoryArr[hi];
        categoryArr[hi] = pivot;
        while (lo < hi) {
            while ((categoryArr[lo].getLabel().compareTo(pivot.getLabel()) <= 0) && (lo < hi)) lo++;
            while ((pivot.getLabel().compareTo(categoryArr[hi].getLabel()) <= 0) && (lo < hi)) hi--;
            if (lo < hi) {
                temp = categoryArr[lo];
                categoryArr[lo] = categoryArr[hi];
                categoryArr[hi] = temp;
            }
        }
        categoryArr[end] = categoryArr[hi];
        categoryArr[hi] = pivot;
        sortDataCategory(start, lo - 1);
        sortDataCategory(hi + 1, end);
    }

    public void closeRecStore() {
        try {
            if (rsCategory != null) rsCategory.closeRecordStore();
        } catch (RecordStoreException e) {
            e.printStackTrace();
        }
    }

    public int getNbCategory() {
        return nbCategory;
    }

    public void setNbCategory(int nbCategory) {
        this.nbCategory = nbCategory;
    }
}

class RECategorySorter implements RecordComparator {

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
