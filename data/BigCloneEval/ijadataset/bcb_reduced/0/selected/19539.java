package mynotes;

import java.util.*;
import javax.microedition.rms.*;

/**
 *
 * @author rschnz
 */
public class NotesFileSystem {

    static final int NOTHING = -1;

    static final int ROOT_DIR = 0;

    static final String RS_FDNAME = "fs";

    static final String RS_DIRNAME = "dir";

    static final int NOT_FOUND = -1;

    private int lastId;

    public Vector files;

    private static RecordStore rs;

    private Vector toDelete;

    /**
     * @brief ���� ���������� � �������� ��������������� � files
     *
     * @param id �������������
     * @return ��������� ����������
     */
    public FileDescriptor getDescriptorById(int id) {
        FileDescriptor fd = null;
        boolean found = false;
        for (int i = 0; i < files.size(); i++) {
            fd = (FileDescriptor) files.elementAt(i);
            if (id == fd.id) {
                found = true;
                break;
            }
        }
        return found ? fd : null;
    }

    /**
     * @brief ���� ���������� � �������� id � files
     * @param id ������������� ����� ��� ��������
     * @return ������ ����������� � �������� id � files
     */
    private int getFilesIndexById(int id) {
        int found = NOT_FOUND;
        FileDescriptor fd = null;
        int i;
        for (i = 0; i < files.size(); i++) {
            fd = (FileDescriptor) files.elementAt(i);
            if (fd == null) {
                continue;
            }
            if (id == fd.id) {
                found = i;
                break;
            }
        }
        return found;
    }

    /**
     * @brief ��������� (��� ������������� �������) ��
     *
     * @return false � ������ ������
     */
    public static boolean openFDStorage() {
        try {
            rs = RecordStore.openRecordStore(RS_FDNAME, true);
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * @brief ��������� (��� ������������� �������) ��
     *
     * @param dirId id �������� (�� ����� �������, ���� ��� ��� ���)
     * @return false � ������ ������
     */
    public static boolean openDirStorage(int dirId) {
        try {
            rs = RecordStore.openRecordStore(RS_DIRNAME + dirId, true);
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean closeStorage() {
        if (rs != null) {
            try {
                rs.closeRecordStore();
            } catch (RecordStoreException ex) {
                return false;
            }
        }
        return true;
    }

    /**
     * @brief �������������� �������� �������
     * ��������� (��� ������������� �������) ��
     * ��������� �� � ������ (� ������ files)
     * ���� �� ������, ������� ���������� ��������� �������� � ��� ��
     *
     * @return ����� ������� � �� (0 �������� ������)
     */
    public int init() {
        rs = null;
        toDelete = new Vector();
        files = new Vector();
        try {
            openFDStorage();
            if (rs.getNumRecords() == 0) {
                lastId = makeDir(".", NOTHING);
            } else {
                FileDescriptor fd = new FileDescriptor();
                RecordEnumeration re = rs.enumerateRecords(null, null, false);
                lastId = 0;
                while (re.hasNextElement()) {
                    int id = re.nextRecordId();
                    byte[] data = rs.getRecord(id);
                    fd.fromByteArray(data);
                    fd.recordId = id;
                    if (fd.id > lastId) {
                        lastId = fd.id;
                    }
                    files.addElement(fd.clone());
                }
            }
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        }
        closeStorage();
        return files.size();
    }

    /**
     * @brief ������� �������
     *
     * @param name ��� ��������
     * @param parent ������������� ������������� ��������
     * @return ������������� ���������� ��������
     */
    public int makeDir(String name, int parent) {
        try {
            FileDescriptor fd = new FileDescriptor();
            fd.isDirectory = true;
            fd.type = FileDescriptor.T_NORMAL | FileDescriptor.T_ENCRYPTED;
            fd.name = name;
            fd.size = 0;
            fd.date = java.lang.System.currentTimeMillis();
            if (parent == NOTHING) {
                fd.id = ROOT_DIR;
                fd.parentDirId = parent;
                if (files != null) {
                    files.removeAllElements();
                }
            } else {
                fd.id = nextId();
                fd.parentDirId = parent;
            }
            byte[] data = fd.toByteArray();
            openFDStorage();
            fd.recordId = rs.addRecord(data, 0, data.length);
            closeStorage();
            if (files == null) {
                files = new Vector();
            }
            files.addElement(fd);
            openDirStorage(fd.id);
            closeStorage();
            return fd.id;
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
            return NOTHING;
        }
    }

    /**
     * @brief ��������������� ���� ��� �������
     *
     * @param id ������������� ����� ��� ��������
     * @param newName ����� ���
     * @return false � ������ �����-���� ������
     */
    public boolean rename(int id, String newName) {
        int index = getFilesIndexById(id);
        if (index == NOT_FOUND) {
            return false;
        }
        FileDescriptor fd = (FileDescriptor) files.elementAt(index);
        try {
            fd.name = newName;
            files.setElementAt(fd, index);
            byte[] data = fd.toByteArray();
            openFDStorage();
            rs.setRecord(fd.recordId, data, 0, data.length);
            closeStorage();
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        }
        return true;
    }

    public boolean delete(int id) {
        toDelete.removeAllElements();
        delete0(id);
        for (int i = 0; i < toDelete.size(); i++) {
            int delId = ((Integer) toDelete.elementAt(i)).intValue();
            int index = getFilesIndexById(delId);
            files.setElementAt(null, index);
        }
        Vector f = new Vector();
        for (int i = 0; i < files.size(); i++) {
            Object o = files.elementAt(i);
            if (o != null) {
                f.addElement(((FileDescriptor) files.elementAt(i)).clone());
            }
        }
        files.removeAllElements();
        files = f;
        return true;
    }

    private boolean deleteNote(FileDescriptor fd) {
        try {
            openDirStorage(fd.parentDirId);
            if (fd.dirRecordId != -1) {
                rs.deleteRecord(fd.dirRecordId);
            }
            closeStorage();
            openFDStorage();
            rs.deleteRecord(fd.recordId);
            closeStorage();
            toDelete.addElement(new Integer(fd.id));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }

    private boolean deleteEmptyDir(int id, int recordId) {
        try {
            openFDStorage();
            rs.deleteRecord(recordId);
            closeStorage();
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        }
        try {
            RecordStore.deleteRecordStore(RS_DIRNAME + new Integer(id).toString());
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        }
        toDelete.addElement(new Integer(id));
        return true;
    }

    /**
     * @brief �������� ����� ��� ��������
     *
     * @param id ������������� ���������� �������� ��� �����
     * @return false � ������ ������
     */
    private boolean delete0(int id) {
        if (id == ROOT_DIR) {
            return false;
        }
        FileDescriptor fd = getDescriptorById(id);
        if (fd == null) {
            return false;
        }
        if (fd.isDirectory == false) {
            deleteNote(fd);
        } else {
            int delCount = 0;
            for (int i = 0; i < files.size(); i++) {
                FileDescriptor f = (FileDescriptor) files.elementAt(i);
                if (f.parentDirId == id) {
                    delCount++;
                }
            }
            if (delCount == 0) {
                deleteEmptyDir(id, fd.recordId);
                return true;
            } else {
                for (int i = 0; i < files.size(); i++) {
                    FileDescriptor f = (FileDescriptor) files.elementAt(i);
                    if (f.parentDirId == id) {
                        delete0(f.id);
                    }
                }
                deleteEmptyDir(id, fd.recordId);
            }
        }
        return true;
    }

    /**
     * @brief ������� ���������� ������ � �� (�� �������� ��!)
     *
     * @param name ��� ������������ �����
     * @param parentDir �������������� ������������� ��������
     *
     * @return ������������� ������
     */
    public int makeNote(String name, int parentDir, int type) {
        FileDescriptor fd = new FileDescriptor();
        fd.name = name;
        fd.date = java.lang.System.currentTimeMillis();
        fd.id = nextId();
        fd.size = 0;
        fd.type = type;
        fd.isDirectory = false;
        fd.dirRecordId = -1;
        fd.parentDirId = parentDir;
        try {
            byte[] data = fd.toByteArray();
            if (!openFDStorage()) {
                return -1;
            }
            fd.recordId = rs.addRecord(data, 0, data.length);
            closeStorage();
            files.addElement(fd);
        } catch (RecordStoreException ex) {
            fd.recordId = -1;
        }
        return fd.id;
    }

    /**
     * @brief ��������� ������ � ��
     *
     * @param id ������������� ������
     * @param note ������, ������� ���������� � ��
     * @return ����� ������ � ��
     */
    public synchronized int setNote(int id, Note note) {
        int index = getFilesIndexById(id);
        if (index == NOT_FOUND) {
            return -1;
        }
        FileDescriptor fd = (FileDescriptor) files.elementAt(index);
        if (fd.isDirectory) {
            return -1;
        }
        try {
            openDirStorage(fd.parentDirId);
            byte[] data = note.toByteArray();
            if (fd.dirRecordId == -1) {
                fd.dirRecordId = rs.getNextRecordID();
                fd.dirRecordId = rs.addRecord(data, 0, data.length);
                files.setElementAt(fd, index);
            } else {
                rs.setRecord(fd.dirRecordId, data, 0, data.length);
            }
            closeStorage();
            openFDStorage();
            fd.size = data.length;
            data = fd.toByteArray();
            rs.setRecord(fd.recordId, data, 0, data.length);
            closeStorage();
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
        return fd.dirRecordId;
    }

    public synchronized Note getNote(int id) {
        FileDescriptor fd = getDescriptorById(id);
        Note note = null;
        if (fd == null) {
            return null;
        }
        if (fd.dirRecordId == -1) {
            note = new Note();
            note.id = id;
            note.text = "";
            return note;
        }
        try {
            if (!openDirStorage(fd.parentDirId)) {
                return null;
            }
            byte[] data = rs.getRecord(fd.dirRecordId);
            closeStorage();
            if (data == null) {
                return null;
            }
            note = new Note();
            note.fromByteArray(data);
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        }
        return note;
    }

    /**
     * @brief ������� �������� � ������������ � �������� �������� ���������� (������� "..")
     *
     * @param id ������������� ��������
     * @param sortType ��� ���������� (�����, �������� ������ �� �����)
     * @return ��������������� ������ ������������ (����� ������)
     */
    public Vector getDirectoryListing(int id, int sortType) {
        Vector f = new Vector();
        Vector d = new Vector();
        for (int i = 0; i < files.size(); i++) {
            FileDescriptor fd = (FileDescriptor) files.elementAt(i);
            if (fd.parentDirId != id) {
                continue;
            }
            if (fd.isDirectory) {
                d.addElement(fd);
            } else {
                f.addElement(fd);
            }
        }
        QuickSort.quicksort(f);
        QuickSort.quicksort(d);
        Vector all = new Vector();
        if (id != ROOT_DIR) {
            FileDescriptor fd = getDescriptorById(id).clone();
            fd.name = "..";
            all.addElement(fd);
        }
        for (int i = 0; i < d.size(); i++) {
            all.addElement(d.elementAt(i));
        }
        for (int i = 0; i < f.size(); i++) {
            all.addElement(f.elementAt(i));
        }
        return all;
    }

    /**
     * @brief ���������� ����������� �������, ���������� text � �����
     *
     * @param text ����� �����
     * @return ������ ������������ �������, ��������������� �������
     */
    public Vector getListingByName(String text, boolean caseSensitive) {
        Vector f = new Vector();
        for (int i = 0; i < files.size(); i++) {
            FileDescriptor fd = (FileDescriptor) files.elementAt(i);
            MyNotes.find.updateGauge();
            if (fd.isDirectory) {
                continue;
            }
            boolean match = false;
            String altText = text;
            String altName = fd.name;
            if (caseSensitive == false) {
                altText = altText.toUpperCase();
                altName = altName.toUpperCase();
            }
            if (altName.indexOf(altText) != -1) {
                match = true;
            }
            if (match) {
                f.addElement(fd);
            }
        }
        QuickSort.quicksort(f);
        return f;
    }

    /**
     * @brief ���������� ����������� �������, ���������� text � �����
     *
     * @param text ����� �����
     * @return ������ ������������ �������, ��������������� �������
     */
    public Vector getListingByContent(String text, boolean caseSensitive) {
        Vector f = new Vector();
        String altText = text;
        if (caseSensitive == false) {
            altText = altText.toUpperCase();
        }
        for (int i = 0; i < files.size(); i++) {
            FileDescriptor fd = (FileDescriptor) files.elementAt(i);
            MyNotes.find.updateGauge();
            if (fd.isDirectory) {
                continue;
            }
            boolean match = false;
            Note note = MyNotes.fs.getNote(fd.id);
            String altContent = "";
            if (note != null) {
                altContent = note.text;
            }
            if (caseSensitive == false) {
                altContent = altContent.toUpperCase();
            }
            if (altContent.indexOf(altText) != -1) {
                match = true;
            }
            if (match) {
                f.addElement(fd);
            }
        }
        QuickSort.quicksort(f);
        return f;
    }

    private int nextId() {
        return ++lastId;
    }

    public int getLastId() {
        return lastId;
    }

    /**
     * @brief �������� ������ � ��������������� id � ����� � ��������������� dir
     *
     * @param id ������������� ������ ��� �����, ����� ������� ���������� �������
     * @param destDirId ������������� �����, � ������� ������� �����
     * @param deleteAfterCopy ���� true, �� ����� ����������� ������� ��������
     * @return ������������� ��������� �����
     */
    public int makeCopy(int id, int destDirId, boolean deleteAfterCopy) {
        int newId = NOTHING;
        FileDescriptor fd = getDescriptorById(id);
        if (fd == null) {
            return newId;
        }
        if (fd.isDirectory) {
            if (deleteAfterCopy) {
                newId = moveDir(fd, destDirId);
            } else {
                newId = makeCopyOfDir(fd, destDirId);
            }
        } else {
            newId = makeCopyOfNote(fd, destDirId);
            if (deleteAfterCopy) {
                delete(fd.id);
            }
        }
        return newId;
    }

    private int makeCopyOfNote(FileDescriptor fd, int destDirId) {
        if (fd.isDirectory) {
            return NOTHING;
        }
        String newName = (destDirId == fd.parentDirId) ? ResourceBundle.getString("nb-copyof") : "";
        newName += fd.name;
        int newId = makeNote(newName, destDirId, fd.type);
        Note note = getNote(fd.id);
        note.id = newId;
        setNote(newId, note);
        return newId;
    }

    private int makeCopyOfDir(FileDescriptor fd, int destDirId) {
        if (!fd.isDirectory) {
            return NOTHING;
        }
        int dirId = destDirId;
        while (dirId != ROOT_DIR) {
            if (fd.id == dirId) {
                return NOTHING;
            }
            FileDescriptor f = getDescriptorById(dirId);
            dirId = f.parentDirId;
        }
        String newName = (destDirId == fd.parentDirId) ? ResourceBundle.getString("nb-copyof") : "";
        newName += fd.name;
        int newId = makeDir(newName, destDirId);
        Vector ids = getDirectoryListing(fd.id, 0);
        for (int i = 0; i < ids.size(); i++) {
            FileDescriptor f = (FileDescriptor) ids.elementAt(i);
            if (!f.name.equals("..")) {
                makeCopy(f.id, newId, false);
            }
        }
        return newId;
    }

    private int moveDir(FileDescriptor fd, int destDirId) {
        if (!fd.isDirectory) {
            return NOTHING;
        }
        int dirId = destDirId;
        while (dirId != ROOT_DIR) {
            if (fd.id == dirId) {
                return NOTHING;
            }
            FileDescriptor f = getDescriptorById(dirId);
            dirId = f.parentDirId;
        }
        fd.parentDirId = destDirId;
        files.setElementAt(fd, getFilesIndexById(fd.id));
        rename(fd.id, fd.name);
        return 0;
    }

    /**
     * �������� ������������ ����� ����������, ������� ����� ������������
     * ��� �������� �� Bluetooth.
     *
     * @return
     */
    public int getAllDataSize() {
        int size = 0;
        size += 4;
        int notesCount = 0;
        for (int i = 0; i < files.size(); i++) {
            FileDescriptor fd = (FileDescriptor) files.elementAt(i);
            byte[] data = fd.toByteArray();
            size += 4 + data.length;
            if (!fd.isDirectory) {
                notesCount++;
            }
        }
        size += 4;
        for (int i = 0; i < files.size(); i++) {
            FileDescriptor fd = (FileDescriptor) files.elementAt(i);
            if (fd.isDirectory) {
                continue;
            }
            Note note = MyNotes.fs.getNote(fd.id);
            if (note != null) {
                byte[] data = note.toByteArray();
                size += 4 + data.length;
            }
        }
        return size;
    }

    /**
     * Encrypts/decrypts all the data (notes and descriptors)
     *
     * ��������/���������� ������ ���������� � ������.
     * ���������� ��������, ����� ��� ��� ���� ��������, ���� �������������,
     * ����� ��������� ��������������� �����.
     *
     * ���� ����� ���������� ������� ��������� ��������, �� ��������� �����
     * ������, ��� ��� �������, � �� ����� ���� ���������� �����������.
     */
    public static void encodeEverythigOnce() {
        Vector tempFiles = new Vector();
        try {
            if (MyNotes.properties.getStringValue("encrypted") != null) {
                return;
            }
            openFDStorage();
            RecordEnumeration re = rs.enumerateRecords(null, null, false);
            while (re.hasNextElement()) {
                int id = re.nextRecordId();
                byte[] data = rs.getRecord(id);
                Utils.encrypt(data);
                FileDescriptor fd = new FileDescriptor();
                fd.fromByteArray(data);
                fd.recordId = id;
                tempFiles.addElement(fd.clone());
            }
            closeStorage();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        byte[] data;
        for (int i = 0; i < tempFiles.size(); i++) {
            FileDescriptor fd = (FileDescriptor) tempFiles.elementAt(i);
            try {
                if (fd.isDirectory == false && fd.dirRecordId != -1) {
                    openDirStorage(fd.parentDirId);
                    data = rs.getRecord(fd.dirRecordId);
                    Utils.encrypt(data);
                    rs.setRecord(fd.dirRecordId, data, 0, data.length);
                    closeStorage();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                closeStorage();
            }
            try {
                openFDStorage();
                fd.type |= FileDescriptor.T_ENCRYPTED;
                data = fd.toByteArray();
                rs.setRecord(fd.recordId, data, 0, data.length);
                closeStorage();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        MyNotes.properties.put("encrypted", "y", Property.PT_STRING);
    }

    /**
     * @brief Deletes all data from all storages
     */
    public void deleteAllData() {
        String[] names = RecordStore.listRecordStores();
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(MyNotes.SETTINGS_RSNAME)) {
                continue;
            }
            try {
                RecordStore.deleteRecordStore(names[i]);
            } catch (Exception ex) {
            }
        }
        this.files.removeAllElements();
    }
}
