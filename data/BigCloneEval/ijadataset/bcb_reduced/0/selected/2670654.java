package wtanaka.praya.obj;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StreamCorruptedException;
import java.util.Enumeration;
import java.util.Vector;
import wtanaka.debug.Debug;

/**
 * This is simply a folder that stores messages in some order.  Objs may be
 * added, they get put at the end of the folder.  The messages may be
 * sorted.  The contents of the folder may be examined, and Objs may be
 * removed.
 *
 * <p>
 * Return to <A href="http://sourceforge.net/projects/praya/">
 * <IMG src="http://sourceforge.net/sflogo.php?group_id=2302&type=1"
 *   alt="Sourceforge" width="88" height="31" border="0"></A>
 * or the <a href="http://praya.sourceforge.net/">Praya Homepage</a>
 *
 * @see LimitFolder
 * @see ReadOnlyFolder
 * @see SystemFolder
 *
 * @author $Author: wtanaka $
 * @version $Name:  $ $Date: 2003/12/17 01:27:21 $
 **/
public class NormalFolder extends Obj implements Fillable, Externalizable {

    /**
    * Serial Version UID
    **/
    static final long serialVersionUID = 405993817902443536L;

    /**
    * Random (/dev/urandom) magic number for serialized instances of
    * this class.
    **/
    static final int MAGIC_NUMBER = 0xe1c5e77c;

    /**
    * Container of all children of this folder. This is private to prevent a
    * malicious user from subclassing and twiddling with system folders.
    **/
    private Vector m_storage = new Vector();

    public transient NormalFolder parent = null;

    private transient Vector folderListeners = new Vector();

    /**
    * Constructs a new, initially empty root folder.
    **/
    public NormalFolder() {
    }

    /**
    * Constructs a new, initially empty subfolder.  The folder containing it
    * will be passed in as parent.
    **/
    public NormalFolder(NormalFolder parent) {
        this.parent = parent;
    }

    /**
    * @exception IOException if we encounter an error reading or a
    * corrupted stream.
    * @exception ClassNotFoundException if we read in a class name on
    * the input stream that we can't resolve.
    **/
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int version = in.readInt() - MAGIC_NUMBER;
        switch(version) {
            case 0:
                m_storage = (Vector) in.readObject();
                break;
            default:
                throw new StreamCorruptedException("Invalid version: " + version);
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        final int SERIAL_FORMAT_VERSION = 0;
        out.writeInt(MAGIC_NUMBER + SERIAL_FORMAT_VERSION);
        out.writeObject(m_storage);
    }

    public void addFolderListener(FolderListener fl) {
        if (Debug.on) {
            if (folderListeners.indexOf(fl) >= 0) throw new RuntimeException("Adding the same folderlistener " + fl + " more than once to " + this);
        }
        folderListeners.addElement(fl);
    }

    public void removeFolderListener(FolderListener fl) {
        folderListeners.removeElement(fl);
    }

    /**
    * Informs folder listeners that o was removed from this folder.
    **/
    private void fireRemoved(int firstIndex, int lastIndex) {
        for (int i = 0; i < folderListeners.size(); ++i) ((FolderListener) folderListeners.elementAt(i)).intervalRemoved(this, firstIndex, lastIndex);
    }

    /**
    * Informs folder listeners that o was added to this folder.
    **/
    private void fireAdded(int firstIndex, int lastIndex) {
        for (int i = 0; i < folderListeners.size(); ++i) ((FolderListener) folderListeners.elementAt(i)).intervalAdded(this, firstIndex, lastIndex);
    }

    public NormalFolder getParent() {
        return parent;
    }

    /**
    * Adds the given object to the end of this folder.
    * @param a the object to add to the folder.  It cannot be null.
    **/
    public synchronized void add(Obj a) {
        m_storage.addElement(a);
        fireAdded(getNumChildren() - 1, getNumChildren() - 1);
    }

    /**
    * Removes the given object from the folder.
    * @param a the object to remove from the folder.
    **/
    public synchronized void remove(Obj a) {
        if (!(a instanceof SystemFolder) || a instanceof ProtoObj) {
            int index = m_storage.indexOf(a);
            m_storage.removeElementAt(index);
            fireRemoved(index, index);
            if ((m_storage.size() << 2) < m_storage.capacity()) m_storage.trimToSize();
        }
    }

    /**
    * Removes the object at the given index from the folder.
    * @param the index of the object to remove from the folder.
    **/
    public synchronized void remove(int i) {
        if (i >= 0 && i < m_storage.size()) {
            if (!(((Obj) m_storage.elementAt(i)) instanceof SystemFolder) || (((Obj) m_storage.elementAt(i)) instanceof ProtoObj)) {
                Obj foo = (Obj) m_storage.elementAt(i);
                m_storage.removeElementAt(i);
                fireRemoved(i, i);
                if ((m_storage.size() << 2) < m_storage.capacity()) m_storage.trimToSize();
            }
        }
    }

    /**
    * Removes selected items from the folder.  This is a way to remove a lot
    * of elements in O(N) time instead of O(N^2)
    **/
    public synchronized void remove(FolderSelectionModel selection) {
        int offset = (getParent() == null) ? 0 : 1;
        int newSize = Math.max(0, selection.getMinSelectionIndex() - offset);
        int max = Math.min(getNumChildren(), selection.getMaxSelectionIndex() + 1);
        int all = getNumChildren();
        Vector removedElements = new Vector();
        for (int i = newSize; i < max; ++i) {
            if (!selection.isSelectedIndex(i + offset) || ((Obj) m_storage.elementAt(i) instanceof SystemFolder && !((Obj) m_storage.elementAt(i) instanceof ProtoObj))) {
                m_storage.setElementAt(m_storage.elementAt(i), newSize++);
            } else {
                removedElements.addElement(m_storage.elementAt(i));
            }
        }
        for (int i = max; i < all; ++i) {
            m_storage.setElementAt(m_storage.elementAt(i), newSize++);
        }
        m_storage.setSize(newSize);
        if ((m_storage.size() << 2) < m_storage.capacity()) m_storage.trimToSize();
        System.err.println("@todo wtanaka call fireRemoved");
    }

    /**
    * This member variable keeps track of which key to sort on.
    **/
    private int sortKey;

    /**
    * Runs a selection sort on the folder, using sortKey as the key to sort
    * on.
    **/
    private synchronized void selectSort() {
        for (int i = 0; i < m_storage.size(); ++i) {
            int smallest = i;
            Obj smallObj = (Obj) m_storage.elementAt(i);
            for (int s = i + 1; s < m_storage.size(); ++s) {
                if (((Obj) m_storage.elementAt(s)).lessThan(smallObj, sortKey)) {
                    smallest = s;
                    smallObj = (Obj) m_storage.elementAt(s);
                }
            }
            m_storage.setElementAt(m_storage.elementAt(i), smallest);
            m_storage.setElementAt(smallObj, i);
        }
    }

    private synchronized void mergeSort(int start, int end, Obj[] tmp) {
        if (start < end) {
            int middle = (start + end) / 2;
            mergeSort(start, middle, tmp);
            mergeSort(middle + 1, end, tmp);
            int i0 = middle;
            int i1 = end;
            for (int i = end; i >= start; --i) {
                if (i0 < start) tmp[i] = (Obj) m_storage.elementAt(i1--); else if (i1 <= middle) tmp[i] = (Obj) m_storage.elementAt(i0--); else if (((Obj) m_storage.elementAt(i1)).lessThan((Obj) m_storage.elementAt(i0), sortKey)) tmp[i] = (Obj) m_storage.elementAt(i0--); else tmp[i] = (Obj) m_storage.elementAt(i1--);
            }
            for (int i = start; i <= end; ++i) {
                m_storage.setElementAt(tmp[i], i);
            }
        }
    }

    /**
    * Sorts the contents of this folder, based on the key given by the
    * parameter.  For instance, if the key is Obj.DATE, then the sort will
    * be by date.
    **/
    public synchronized void sort(int key) {
        sortKey = key;
        Obj[] tmp = new Obj[m_storage.size()];
        mergeSort(0, m_storage.size() - 1, tmp);
    }

    protected String name = "New Folder";

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
    * Gets a child of this folder.
    * @return the indexth child of this folder.
    * @exception ArrayIndexOutOfBoundsException if the given child does not
    * exist.
    **/
    public Obj getChildAt(int index) {
        return (Obj) m_storage.elementAt(index);
    }

    public int getNumChildren() {
        return m_storage.size();
    }

    public synchronized int indexOf(Obj foo) {
        return m_storage.indexOf(foo);
    }

    /**
    * Used to render this folder as an obj.
    **/
    public String getContents() {
        return getName();
    }

    /**
    * Gives the contents of the folder.
    * @return an Enumeration of Obj, the contents of this folder.
    **/
    public synchronized Enumeration elements() {
        return m_storage.elements();
    }

    public String toString() {
        return "[NormalFolder: " + getName() + "]";
    }

    /**
    * Whether or not removed messages should be placed in the trash.
    * @return true if we should put removed messages in the trash (for
    * instance for most normal folders) false if we should kill them
    * (for instance, for the trash can and protocol folders)
    **/
    public boolean isTrashCanUsed() {
        return true;
    }
}
