package edu.whitman.halfway.jigs.gui.desque;

import java.util.*;
import javax.swing.ListSelectionModel;
import javax.swing.event.EventListenerList;
import javax.swing.undo.*;
import org.apache.log4j.Logger;
import cern.colt.list.IntArrayList;
import edu.whitman.halfway.jigs.*;

public class AlbumModel {

    private static Logger log = Logger.getLogger(AlbumModel.class);

    private ListSelectionModel selectionModel;

    private EventListenerList listeners = new EventListenerList();

    private Album album = null;

    private AlbumObjectFilter filter = null;

    private boolean changesMade = false;

    private boolean sortAscending = true;

    private String sortColumnKey = DesqueConstants.DESQUE_NAME;

    private int parentIndex = -1;

    private WeakHashMap albumImageTypes = new WeakHashMap();

    private ImageType[] imageTypes = null;

    private ArrayList data = new ArrayList();

    private IntArrayList filterList = new IntArrayList();

    private int[] sortList = new int[0];

    public AlbumModel(ListSelectionModel selectionModel) {
        this.selectionModel = selectionModel;
    }

    public boolean isAlbum(int row) {
        return (getAlbumObject(row) instanceof Album);
    }

    public boolean isPicture(int row) {
        return (getAlbumObject(row) instanceof Picture);
    }

    public boolean isParentAlbum(int row) {
        return (getIndexForRow(row) == parentIndex);
    }

    public AlbumObject getAlbumObject(int row) {
        return (AlbumObject) data.get(getIndexForRow(row));
    }

    /** Returns the number of Albums in the current view (filtered) of this album
     */
    public int getSize() {
        if (album == null) {
            return 0;
        }
        return sortList.length;
    }

    /** DO NOT USE.  This method directly modifies the data in the
	underlying representation, and does not fire any events.  This
	method is meant to be used only by the AlbumUndoEdit class */
    private void setData(int index, String fieldKey, Object value) {
        AlbumObjectDescriptionInfo info = getAlbumObjectAtUnfilteredIndex(index).getDescriptionInfo();
        info.setData(fieldKey, value);
        if (JigsPrefs.getInt(DesqueConstants.SAVE_STYLE) == DesqueConstants.SAVEALWAYS) info.saveFile(); else changesMade = true;
    }

    public void setValue(int row, String fieldKey, Object value) {
        setValue(row, fieldKey, value, null);
    }

    public synchronized void setValue(int row, String fieldKey, Object value, Object instigator) {
        if (fieldKey.equals(DesqueConstants.DESQUE_NAME)) return;
        AlbumObjectDescriptionInfo info = getAlbumObject(row).getDescriptionInfo();
        info.setData(fieldKey, value);
        if (JigsPrefs.getInt(DesqueConstants.SAVE_STYLE) == DesqueConstants.SAVEALWAYS) info.saveFile(); else changesMade = true;
        fireValueChanged(row, row, fieldKey, instigator);
    }

    public void setValues(int[] rows, String fieldKey, Object value) {
        setValues(rows, fieldKey, value, null);
    }

    public synchronized void setValues(int[] rows, String fieldKey, Object value, Object instigator) {
        int min = rows[0];
        int max = rows[0];
        if (fieldKey.equals(DesqueConstants.DESQUE_NAME)) return;
        for (int i = 0; i < rows.length; i++) {
            int row = rows[i];
            AlbumObjectDescriptionInfo info = getAlbumObject(row).getDescriptionInfo();
            info.setData(fieldKey, value);
            if (JigsPrefs.getInt(DesqueConstants.SAVE_STYLE) == DesqueConstants.SAVEALWAYS) info.saveFile(); else changesMade = true;
            min = (row < min) ? row : min;
            max = (row > max) ? row : max;
        }
        fireValueChanged(min, max, fieldKey, instigator);
    }

    public Object getValue(int row, String fieldKey) {
        return getAlbumObject(row).getDescriptionInfo().getData(fieldKey);
    }

    public boolean changedMade() {
        return changesMade;
    }

    public boolean isSortedAscending() {
        return sortAscending;
    }

    public void sort(String fieldKey) {
        if (fieldKey.equals(sortColumnKey)) {
            sortAscending = !sortAscending;
        } else {
            sortAscending = true;
            sortColumnKey = fieldKey;
        }
        sortList = new int[filterList.size()];
        System.arraycopy(filterList.elements(), 0, sortList, 0, filterList.size());
        resortImpl();
        fireAlbumResorted();
    }

    public void resort() {
        resortImpl();
        fireAlbumResorted();
    }

    private void resortImpl() {
        if (filterList.size() != sortList.length) {
            System.out.println(filterList.size() + " " + sortList.length);
            throw new RuntimeException("Filter List and Sort List have Unequal Lengths!");
        }
        shuttlesort((int[]) sortList.clone(), sortList, 0, sortList.length);
    }

    public void setFilter(AlbumObjectFilter filter) {
        this.filter = filter;
        selectionModel.clearSelection();
        applyFilter();
        fireValueChanged(0, getSize() - 1, "", null);
    }

    private void applyFilter() {
        filterList = new IntArrayList(data.size());
        for (int i = 0; i < data.size(); i++) {
            if (filter == null || filter.accept((AlbumObject) data.get(i))) {
                filterList.add(i);
            }
        }
        filterList.trimToSize();
        sortList = (int[]) filterList.elements().clone();
        resortImpl();
    }

    /** aaaa
       @deprecated
     */
    public void openAlbum(Album a) {
        openAlbum(a, true);
    }

    public void openAlbum(Album a, boolean b) {
        if (a == null) {
            return;
        }
        if (album != null) {
            closeAlbum(b);
        }
        changesMade = false;
        album = a;
        loadCurrentAlbum();
        fireAlbumOpened();
        Thread t = new Thread() {

            public void run() {
                AlbumDescriptionInfo descInfo = album.getAlbumDescriptionInfo();
                Boolean b = (Boolean) descInfo.getData(DesqueConstants.READ_EXIF_FLAG);
                boolean hasKey = (b != null);
                if (!hasKey) {
                    readEXIFData();
                    descInfo.setData(DesqueConstants.READ_EXIF_FLAG, null);
                    fireAlbumResorted();
                    log.debug("EXIF Data");
                }
                determineImageTypes();
                fireImageTypesChanged();
                log.debug("ImageTypes");
            }
        };
        t.start();
    }

    /** 
        // Dump all slow stuff into another thread for execution
        // at a slightly later date.
        Thread t = new Thread() {
                public void run() {
                    // Get EXIF Data from pictures
                    readEXIFData();
                    fireAlbumResorted();
                    log.debug("EXIF Data");
                    
                    // Now load in image types
                    determineImageTypes();
                    fireImageTypesChanged();
                    log.debug("ImageTypes");
                    
                    // WARNING:  HACK HERE CAUSES PICTURES TO LOAD
                    // NOW SLOWING DOWN EVERYTHING!!!!
                    // SEE ALBUMIMAGEBUFFER.
                }
            };
        
        t.start();
        */
    private void determineImageTypes() {
        imageTypes = (ImageType[]) albumImageTypes.get(album);
        if (imageTypes == null) {
            imageTypes = album.getImageTypes();
            albumImageTypes.put(album, imageTypes);
        }
    }

    /** returns the imageTypes for this album, or null, if they have 
        yet to be determined
    */
    public ImageType[] getImageTypes() {
        return imageTypes;
    }

    public void refresh() {
        album.refresh();
        loadCurrentAlbum();
        readEXIFData();
        fireAlbumResorted();
    }

    private void loadCurrentAlbum() {
        selectionModel.clearSelection();
        AlbumObject[] child = album.getAlbumObjects();
        Album parentAlbum = DesqueAOUtil.getParentNoEnd(album, null);
        data = new ArrayList(child.length + 1);
        if (parentAlbum != null) {
            parentAlbum.getDescriptionInfo().setData(DesqueConstants.DESQUE_NAME, "..");
            data.add(parentAlbum);
            parentIndex = 0;
        } else {
            parentIndex = -1;
        }
        for (int i = 0; i < child.length; i++) {
            AlbumObjectDescriptionInfo aodi = child[i].getDescriptionInfo();
            aodi.setData(DesqueConstants.DESQUE_NAME, child[i].getName());
            data.add(child[i]);
        }
        applyFilter();
    }

    private synchronized void readEXIFData() {
        for (int i = 0; i < data.size(); i++) {
            AlbumObject ao = (AlbumObject) data.get(i);
            if (ao instanceof Picture) {
                PictureDescriptionInfo pdi = ((Picture) ao).getPictureDescriptionInfo();
                pdi.readExifData(false);
            }
        }
    }

    public void closeAlbum(boolean saveChanges) {
        fireAlbumToClose();
        if (saveChanges) {
            SaverThread saveThread = new SaverThread(data);
            saveThread.start();
        }
        album = null;
        imageTypes = null;
        fireAlbumClosed();
    }

    protected int getIndexForRow(int row) {
        return filterList.get(sortList[row]);
    }

    protected int getAlbumSize() {
        return data.size();
    }

    protected Album getAlbum() {
        return album;
    }

    /** Return true if an ablum is currently opened, false otherwise
     */
    public boolean hasAlbum() {
        return (album != null);
    }

    protected AlbumObject getAlbumObjectAtUnfilteredIndex(int index) {
        return (AlbumObject) data.get(index);
    }

    /**  
         HACK.  PROBABLY SLOW.
    */
    public boolean isIndexDisplayed(int index) {
        return filterList.contains(index);
    }

    public void addAlbumModelListener(AlbumModelListener aml) {
        listeners.add(AlbumModelListener.class, aml);
    }

    public void removeAlbumModelListener(AlbumModelListener aml) {
        listeners.remove(AlbumModelListener.class, aml);
    }

    public void fireImageUpdated(int firstRow, int lastRow) {
        AlbumModelListener[] list = (AlbumModelListener[]) listeners.getListeners(AlbumModelListener.class);
        AlbumModelEvent ame = new AlbumModelEvent(this, firstRow, lastRow, null, AlbumModelEvent.IMAGE_UPDATED);
        for (int i = 0; i < list.length; i++) {
            list[i].albumModelChanged(ame);
        }
    }

    protected void fireValueChanged(int firstRow, int lastRow, String field, Object instigator) {
        if (field == null) throw new IllegalArgumentException();
        AlbumModelListener[] list = (AlbumModelListener[]) listeners.getListeners(AlbumModelListener.class);
        AlbumModelEvent ame = new AlbumModelEvent(this, firstRow, lastRow, field, AlbumModelEvent.VALUE_CHANGED, instigator);
        for (int i = 0; i < list.length; i++) {
            list[i].albumModelChanged(ame);
        }
    }

    protected void fireAlbumResorted() {
        AlbumModelListener[] list = (AlbumModelListener[]) listeners.getListeners(AlbumModelListener.class);
        AlbumModelEvent ame = new AlbumModelEvent(this, 0, data.size(), "", AlbumModelEvent.VALUE_CHANGED);
        for (int i = 0; i < list.length; i++) {
            list[i].albumModelChanged(ame);
        }
    }

    protected void fireImageTypesChanged() {
        AlbumModelListener[] list = (AlbumModelListener[]) listeners.getListeners(AlbumModelListener.class);
        AlbumModelEvent ame = new AlbumModelEvent(this, 0, data.size(), "", AlbumModelEvent.IMAGE_TYPES_CHANGED);
        for (int i = 0; i < list.length; i++) {
            list[i].albumModelChanged(ame);
        }
    }

    protected void fireAlbumOpened() {
        AlbumModelListener[] list = (AlbumModelListener[]) listeners.getListeners(AlbumModelListener.class);
        AlbumModelEvent ame = new AlbumModelEvent(this, 0, data.size(), "", AlbumModelEvent.ALBUM_OPENED);
        for (int i = 0; i < list.length; i++) {
            list[i].albumModelChanged(ame);
        }
    }

    protected void fireAlbumClosed() {
        AlbumModelListener[] list = (AlbumModelListener[]) listeners.getListeners(AlbumModelListener.class);
        AlbumModelEvent ame = new AlbumModelEvent(this, 0, data.size(), "", AlbumModelEvent.ALBUM_CLOSED);
        for (int i = 0; i < list.length; i++) {
            list[i].albumModelChanged(ame);
        }
    }

    protected void fireAlbumToClose() {
        AlbumModelListener[] list = (AlbumModelListener[]) listeners.getListeners(AlbumModelListener.class);
        AlbumModelEvent ame = new AlbumModelEvent(this, 0, data.size(), "", AlbumModelEvent.ALBUM_TO_CLOSE);
        for (int i = 0; i < list.length; i++) {
            list[i].albumModelChanged(ame);
        }
    }

    private void shuttlesort(int[] from, int[] to, int low, int high) {
        if (high - low < 2) {
            return;
        }
        int middle = (low + high) / 2;
        shuttlesort(to, from, low, middle);
        shuttlesort(to, from, middle, high);
        int p = low;
        int q = middle;
        if (high - low >= 4 && sortUpDown(from[middle - 1], from[middle]) <= 0) {
            for (int i = low; i < high; i++) {
                to[i] = from[i];
            }
            return;
        }
        for (int i = low; i < high; i++) {
            if (q >= high || (p < middle && sortUpDown(from[p], from[q]) <= 0)) {
                to[i] = from[p++];
            } else {
                to[i] = from[q++];
            }
        }
    }

    private int sortUpDown(int row1, int row2) {
        int result = compareRows(row1, row2);
        return (sortAscending) ? result : -result;
    }

    private Object getValueAtUnfilteredIndex(int index, String key) {
        if (key.equals(DesqueConstants.DESQUE_NAME)) {
            return data.get(index);
        }
        return ((AlbumObject) data.get(index)).getDescriptionInfo().getData(key);
    }

    private int compareRows(int row1, int row2) {
        AlbumObject ao1 = (AlbumObject) data.get(filterList.get(row1));
        AlbumObject ao2 = (AlbumObject) data.get(filterList.get(row2));
        if (ao1 instanceof Album) {
            if (ao2 instanceof Album) {
                return 0;
            }
            return -1;
        } else if (ao2 instanceof Album) {
            return 1;
        }
        Object o1 = getValueAtUnfilteredIndex(filterList.get(row1), sortColumnKey);
        Object o2 = getValueAtUnfilteredIndex(filterList.get(row2), sortColumnKey);
        if (o1 == null) {
            if (o2 == null) {
                return 0;
            }
            return 1;
        } else if (o2 == null) {
            return -1;
        }
        if (o1 instanceof AlbumObject) {
            String ao1n = (String) ao1.getDescriptionInfo().getData(DesqueConstants.DESQUE_NAME);
            String ao2n = (String) ao2.getDescriptionInfo().getData(DesqueConstants.DESQUE_NAME);
            return compareStringsCaseless(ao1n, ao2n);
        } else if (o1 instanceof Rating) {
            return ((Rating) o1).compareTo((Rating) o2);
        } else if (o1 instanceof CategorySet) {
            return 0;
        } else if (o1 instanceof Date) {
            Date d1 = (Date) o1;
            Date d2 = (Date) o2;
            int result = d1.compareTo(d2);
            if (result == 0) return 0;
            return (result < 0) ? -1 : 1;
        } else if (o1 instanceof String) {
            return compareStrings((String) o1, (String) o2);
        } else {
            log.error("Unknown Type: " + o1.getClass());
            return 0;
        }
    }

    private int compareStringsCaseless(String s1, String s2) {
        return compareStrings(s1.toLowerCase(), s2.toLowerCase());
    }

    private int compareStrings(String s1, String s2) {
        int result = s1.compareTo(s2);
        if (result == 0) return 0;
        return (result < 0) ? -1 : 1;
    }

    class SaverThread extends Thread {

        private ArrayList myData;

        SaverThread(ArrayList data) {
            myData = (ArrayList) data.clone();
        }

        public void run() {
            Iterator iter = myData.iterator();
            while (iter.hasNext()) {
                AlbumObject ao = (AlbumObject) iter.next();
                ao.getDescriptionInfo().saveFile();
            }
        }
    }

    private void fireUndoEdit(UndoableEdit ue) {
    }

    class SortUndoEdit extends AbstractUndoableEdit {

        private final Logger log = Logger.getLogger(SortUndoEdit.class);

        private String prevField;

        private String newField;

        private boolean prevAscend;

        private boolean newAscend;

        public SortUndoEdit(String prevField, boolean prevAscend, String newField, boolean newAscend) {
            this.prevField = prevField;
            this.newField = newField;
            this.prevAscend = prevAscend;
            this.newAscend = newAscend;
        }

        public void redo() {
            super.redo();
            sortColumnKey = newField;
            sortAscending = newAscend;
            resort();
        }

        public void undo() {
            super.undo();
            sortColumnKey = prevField;
            sortAscending = prevAscend;
            resort();
        }

        public String getPresentationName() {
            return "Sort " + newField;
        }

        public boolean isSignificant() {
            return true;
        }
    }

    class UpdateUndoEdit extends AbstractUndoableEdit {

        private final Logger log = Logger.getLogger(UpdateUndoEdit.class);

        private String fieldKey;

        private Object[] prevVals;

        private Object[] newVals;

        private int[] indexes;

        public UpdateUndoEdit(int index, String fieldKey, Object prevVal, Object newVal) {
            this(new int[] { index }, fieldKey, new Object[] { prevVal }, new Object[] { newVal });
        }

        public UpdateUndoEdit(int[] indexes, String fieldKey, Object[] prevVals, Object[] newVals) {
            this.indexes = indexes;
            this.fieldKey = fieldKey;
            this.prevVals = prevVals;
            this.newVals = newVals;
        }

        public void redo() {
            super.redo();
            for (int i = 0; i < indexes.length; i++) {
                setData(i, fieldKey, newVals[i]);
            }
            fireAlbumResorted();
        }

        public void undo() {
            super.undo();
            for (int i = 0; i < indexes.length; i++) {
                setData(i, fieldKey, prevVals[i]);
            }
            fireAlbumResorted();
        }

        public String getPresentationName() {
            return fieldKey + ": " + indexes.length + " edits";
        }

        public boolean isSignificant() {
            return true;
        }
    }
}
