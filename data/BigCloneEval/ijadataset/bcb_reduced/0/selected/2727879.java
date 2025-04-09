package com.thyante.thelibrarian.model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.thyante.thelibrarian.info.IItemDatabaseProxy;
import com.thyante.thelibrarian.info.IItemDatabaseProxyConfiguration;
import com.thyante.thelibrarian.model.specification.IField;
import com.thyante.thelibrarian.model.specification.IItem;
import com.thyante.thelibrarian.model.specification.IMedium;
import com.thyante.thelibrarian.model.specification.ITemplate;
import com.thyante.thelibrarian.model.specification.TitleFunction;
import com.thyante.thelibrarian.util.FileStorageUtil;
import com.thyante.thelibrarian.util.FileUtil;
import com.thyante.thelibrarian.util.I18n;
import com.thyante.thelibrarian.util.XMLUtil;

/**
 * The collection model.
 * @author Matthias-M. Christen
 */
public class Collection implements ICollection, Iterable<IItem>, ITemplateListener, IItemListener, IConfigurationChangedListener, IIndexProvider, IDeleteMediaScheduler, ICategoryResolver {

    private static final Object SOMETHING = new Object();

    /**
	 * The template this collection is based on
	 */
    protected Template m_template;

    /**
	 * Holds the previous template when the template is being modified
	 */
    private ITemplate m_templateOld;

    /**
	 * The list of items contained in the collection
	 */
    protected List<IItem> m_listItems;

    /**
	 * The root of the categories tree
	 */
    protected Category m_categoryRoot;

    /**
	 * A map assiging a category ID the corresponding category
	 */
    protected Map<String, Category> m_mapCategories;

    /**
	 * List of collection listeners
	 */
    protected List<ICollectionListener> m_listListeners;

    /**
	 * Collection meta data
	 */
    protected CollectionMetaData m_metadata;

    /**
	 * Collection settings (view classes, print settings, ...)
	 */
    protected CollectionSettings m_settings;

    /**
	 * List of the media that have to be deleted at the next saving point
	 */
    private List<IMedium> m_listMediaToDelete;

    /**
	 * The collection's name. Coincides with the last part of the file name if
	 * the collection has been saved.
	 */
    protected String m_strName;

    /**
	 * The file name under which the collection is stored or <code>null</code>
	 * if the collection hasn't been saved yet
	 */
    protected String m_strFilename;

    /**
	 * Flag indicating whether changes have been made since the collection
	 * was last saved
	 */
    protected boolean m_bIsDirty;

    /**
	 * Flag indicating whether the collection is could be disposed.
	 * (If the collection is being saved, it must not be disposed.) 
	 */
    protected boolean m_bCanDispose;

    /**
	 * Constructs an empty collection.
	 */
    public Collection(String strName, Template template, CollectionMetaData metadata, boolean bCreateDefaultCategories) {
        m_template = template == null ? null : (Template) template.clone();
        if (m_template != null) m_template.addTemplateListener(this);
        m_strName = strName;
        m_listItems = new ArrayList<IItem>();
        m_listListeners = new LinkedList<ICollectionListener>();
        createCategories(bCreateDefaultCategories);
        m_metadata = metadata != null ? metadata : new CollectionMetaData();
        m_metadata.addChangeListener(this);
        m_settings = new CollectionSettings(template);
        if (metadata == null || metadata.getIconPath() == null || "".equals(metadata.getIconPath())) setIconFromTemplate();
        m_strFilename = null;
        m_bIsDirty = false;
        m_listMediaToDelete = null;
    }

    /**
	 * Disposes of the collection.
	 */
    public void dispose() {
        if (!m_bCanDispose) return;
        for (IItem item : m_listItems) {
            item.removeItemListener(this);
            item.dispose();
        }
        m_metadata.removeChangeListener(this);
        m_metadata.dispose();
        m_settings.dispose();
    }

    protected void createCategories(boolean bCreateDefaultCategories) {
        m_categoryRoot = new Category(null, I18n.xl8("All Items"));
        if (bCreateDefaultCategories) {
            Category catRating = new Category(m_categoryRoot, I18n.xl8("Rating"));
            new Category(catRating, "●●●●●");
            new Category(catRating, "●●●●");
            new Category(catRating, "●●●");
            new Category(catRating, "●●");
            new Category(catRating, "●");
        }
        createCategoryMap();
    }

    private void createCategoryMap() {
        if (m_mapCategories == null) m_mapCategories = new HashMap<String, Category>(); else m_mapCategories.clear();
        createCategoryMap(m_categoryRoot);
    }

    private void createCategoryMap(Category catParent) {
        m_mapCategories.put(catParent.getID(), catParent);
        for (Category cat : catParent) createCategoryMap(cat);
    }

    /**
	 * Adds a category to the collection.
	 * @param category The category to add
	 */
    public void addCategory(Category category) {
        if (m_mapCategories == null) m_mapCategories = new HashMap<String, Category>();
        m_mapCategories.put(category.getID(), category);
    }

    /**
	 * Sets the flag indicating whether the collection could be disposed.
	 * If set to <code>false</code>, the collection won't be disposed if the
	 * {@link Collection#dispose()} method is called.
	 * @param bCanDispose Flag indicating whether it is save to dispose the collection
	 */
    public void setCanBeDisposed(boolean bCanDispose) {
        m_bCanDispose = bCanDispose;
    }

    public ITemplate getTemplate() {
        return m_template;
    }

    /**
	 * Returns the collection's meta data object. The meta data object
	 * saves collection-relevant meta data such as the creator of the
	 * collection, its purpose (description), etc.
	 * @return The collection meta data
	 */
    public CollectionMetaData getMetaData() {
        return m_metadata;
    }

    /**
	 * Returns the collection's settings. The {@link CollectionSettings} object
	 * stores collection-relevant setting data such as the currently used
	 * views or the print settings.
	 * @return The collection settings
	 */
    public CollectionSettings getSettings() {
        return m_settings;
    }

    /**
	 * Sets the collection icon in the collection meta data.
	 */
    public void setIconFromTemplate() {
        if (m_template != null) m_metadata.setIcon(m_template.getMetaData().getTemplateIconFilePath()); else m_metadata.setIcon("intern://record0");
    }

    public Iterator<IItem> iterator() {
        return m_listItems.iterator();
    }

    public java.util.Collection<IItem> getItems() {
        return m_listItems;
    }

    /**
	 * Returns the number of items in the collection.
	 * @return The number of items
	 */
    public int getItemsCount() {
        return m_listItems.size();
    }

    /**
	 * Gives the collection a hint how many items it will contain.
	 */
    @SuppressWarnings("unchecked")
    public void setCollectionSize(int nSize) {
        if (m_listItems instanceof ArrayList) ((ArrayList<IItem>) m_listItems).ensureCapacity(nSize);
    }

    /**
	 * Returns all the images as an <code>Item</code> array.
	 * @return Array of all the items contained in the collection
	 */
    public IItem[] getItemsAsArray() {
        IItem[] rgItems = new IItem[m_listItems.size()];
        m_listItems.toArray(rgItems);
        return rgItems;
    }

    public IItem getItem(int nIndex) {
        if (nIndex < 0 || nIndex >= m_listItems.size()) return null;
        return m_listItems.get(nIndex);
    }

    public IItem createItem() {
        return new Item(m_template.getFields(), m_metadata, this);
    }

    /**
	 * Adds the item <code>item</code> to the collection.
	 * @param item The item to add to the collection
	 */
    public void addItem(IItem item) {
        int nIdx = getIndex(m_listItems, item);
        m_listItems.add(nIdx, item);
        fireCollectionChanged(item, ICollectionListener.CollectionChangedType.ITEM_ADDED, nIdx);
        item.addItemListener(this);
    }

    /**
	 * Adds the item <code>item</code> to the end of the list.
	 * Use with care because the list might become unsorted.
	 * After adding a item bulk the collection may be sorted using
	 * the {@link Collection#sort()} method.
	 * @param item The item to add to the collection without sorting
	 */
    public void fastAddItem(IItem item) {
        m_listItems.add(item);
        item.addItemListener(this);
    }

    /**
	 * Sorts the items in the collection.
	 */
    public void sort() {
        Collections.sort(m_listItems);
    }

    /**
	 * Removes the item <code>item</code> from the collection.
	 * @param item The item to remove
	 */
    public void removeItem(IItem item) {
        int nIdx = Collections.binarySearch(m_listItems, item);
        boolean bItemFound = true;
        if (nIdx < 0 || !m_listItems.get(nIdx).isSameItem(item)) {
            bItemFound = false;
            String strTitle = item.getTitle();
            if (nIdx < 0) nIdx = -nIdx - 1;
            for (int i = nIdx - 1; i >= 0; i--) {
                IItem itemTest = m_listItems.get(i);
                if (!itemTest.getTitle().equals(strTitle)) break;
                if (itemTest.isSameItem(item)) {
                    nIdx = i;
                    bItemFound = true;
                    break;
                }
            }
            if (!bItemFound) for (int i = nIdx + 1; nIdx < m_listItems.size(); i++) {
                IItem itemTest = m_listItems.get(i);
                if (!itemTest.getTitle().equals(strTitle)) break;
                if (m_listItems.get(i).isSameItem(item)) {
                    nIdx = i;
                    bItemFound = true;
                    break;
                }
            }
        }
        if (nIdx < 0 || !bItemFound) return;
        m_listItems.remove(nIdx);
        fireCollectionChanged(item, ICollectionListener.CollectionChangedType.ITEM_REMOVED, nIdx);
        item.removeItemListener(this);
        item.removeAllMedia(false, this);
        item.dispose();
    }

    /**
	 * Returns the root category.
	 * @return The root category
	 */
    public Category getRootCategory() {
        return m_categoryRoot;
    }

    /**
	 * Returns the collection's name.
	 * @return The name of the collection
	 */
    public String getName() {
        return m_strName;
    }

    /**
	 * Sets the collection filename (without the collection being saved or any other
	 * serialization action taking place).
	 * @param strFilename The new filename
	 */
    public void setFilename(String strFilename) {
        m_strFilename = strFilename;
        if (!m_strFilename.endsWith(FileStorageUtil.LIBRARIAN_FILE_EXTENSION)) m_strFilename += FileStorageUtil.LIBRARIAN_FILE_EXTENSION;
        String strNewName = FileStorageUtil.stripPath(FileStorageUtil.stripExtension(m_strFilename));
        if (strNewName == null) {
            if (m_strName != null) {
                String strOldName = m_strName;
                m_strName = null;
                fireCollectionNameChanged(strOldName);
            }
        } else {
            if (!strNewName.equals(m_strName)) {
                String strOldName = m_strName;
                m_strName = strNewName;
                fireCollectionNameChanged(strOldName);
            }
        }
    }

    /**
	 * Returns the filename under which the collection is being stored or <code>null</code> if the
	 * collection hasn't been saved yet.
	 * @return The collection's filename
	 */
    public String getFilename() {
        return m_strFilename;
    }

    /**
	 * Returns a value indicating whether the collection has been modified since it was last saved.
	 * @return The status of the &quot;Modified flag&quot;
	 */
    public boolean isDirty() {
        return m_bIsDirty;
    }

    /**
	 * Creates a new collection object based on the data that is
	 * read from the file <code>file</code>.
	 * @param The file from which to read the collection
	 * @return The newly created collection object or <code>null</code> if the file
	 * 	could not be read
	 */
    public static Collection openFile(File f, IItemProgressMonitor monitor) throws CancelParsingException, ZipException, FileNotFoundException, IOException, ClassNotFoundException {
        Collection c = new Collection(null, null, null, false);
        ZipFile file = new ZipFile(f);
        ZipEntry entryDatabase = file.getEntry("db.xml");
        c.fromStream(f, file.getInputStream(entryDatabase), monitor);
        c.m_settings.setTemplate(c.getTemplate());
        ZipEntry entrySettings = file.getEntry("settings.xml");
        if (entrySettings != null) c.m_settings.fromStream(file.getInputStream(entrySettings));
        c.m_metadata.loadIcons(file);
        file.close();
        c.setFilename(f.getAbsolutePath());
        c.m_bIsDirty = false;
        return c;
    }

    /**
	 * Parses the XML file and populates the collection
	 * @param file The collection ZIP file
	 * @param in The XML input stream
	 * @param monitor UI element to visualize the parsing progress
	 * @throws CancelParsingException if the user has canceled the parsing process
	 * @throws ClassNotFoundException
	 */
    protected void fromStream(File file, InputStream in, IItemProgressMonitor monitor) throws CancelParsingException, ClassNotFoundException {
        FileStorageUtil.getSAXParser().setContentHandler(new CollectionFileContentHandler(this, file, monitor));
        try {
            FileStorageUtil.getSAXParser().parse(new InputSource(in));
        } catch (CancelParsingException e) {
            throw e;
        } catch (IOException e) {
        } catch (SAXException e) {
        }
        m_categoryRoot.reset();
        for (IItem item : this) for (String strCategoryRef : item.getCategoryRefs()) {
            Category category = getCategoryByRef(strCategoryRef);
            if (category != null) category.addItem(item);
        }
        fireCollectionLoaded();
    }

    /**
	 * Saves the collection to the currently associated file.
	 * The file is in ZIP format containing two entries, <code>settings.xml</code> and
	 * <code>db.xml</code>.
	 * code>settings.xml</code> contains collection-relevant settings (such as which view
	 * has been last used), where as <code>db.xml</code> contains the actual data.
	 * @param monitor The progress monitor
	 * @throws ZipException
	 * @throws IOException
	 */
    public void save(IItemProgressMonitor monitor) throws ZipException, IOException {
        if (m_strFilename == null || "".equals(m_strFilename)) throw new FileNotFoundException();
        deleteMedia();
        File fileTmp = File.createTempFile("librarian-save", FileStorageUtil.LIBRARIAN_FILE_EXTENSION);
        File file = new File(m_strFilename);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(fileTmp));
        out.setLevel(Deflater.DEFAULT_COMPRESSION);
        out.putNextEntry(new ZipEntry("settings.xml"));
        m_settings.toStream(out);
        out.closeEntry();
        m_metadata.storeIcons(out);
        out.putNextEntry(new ZipEntry("db.xml"));
        if (toStream(file, out, monitor)) {
            out.closeEntry();
            out.close();
            m_bIsDirty = false;
            FileUtil.copyFile(fileTmp, file);
        } else {
            out.close();
            fileTmp.delete();
        }
    }

    /**
	 * Only save the collection settings to the <code>settings.xml</code> entry
	 * within the collection ZIP file.
	 */
    public void saveSettings() {
        if (m_strFilename == null || "".equals(m_strFilename)) return;
        if (!m_settings.isDirty()) return;
        try {
            ZipFile fileIn = new ZipFile(new File(m_strFilename));
            File fileTmp = File.createTempFile("librarian-save", FileStorageUtil.LIBRARIAN_FILE_EXTENSION);
            File file = new File(m_strFilename);
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(fileTmp));
            out.setLevel(Deflater.DEFAULT_COMPRESSION);
            out.putNextEntry(new ZipEntry("settings.xml"));
            m_settings.toStream(out);
            out.closeEntry();
            Enumeration<? extends ZipEntry> enumZipEntries = fileIn.entries();
            while (enumZipEntries.hasMoreElements()) {
                ZipEntry zeIn = enumZipEntries.nextElement();
                if (zeIn.getName().equals("settings.xml")) continue;
                out.putNextEntry(new ZipEntry(zeIn.getName()));
                BufferedInputStream bis = new BufferedInputStream(fileIn.getInputStream(zeIn));
                byte[] rgBuf = new byte[4096];
                for (; ; ) {
                    int nBytesRead = bis.read(rgBuf);
                    if (nBytesRead == -1) break;
                    out.write(rgBuf, 0, nBytesRead);
                }
                out.closeEntry();
            }
            fileIn.close();
            out.close();
            FileUtil.copyFile(fileTmp, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
	 * Saves the collection to the stream <code>out</code>.
	 * @param file The file to which the collection data is saved.
	 * 	This information is needed for constructing the name of the
	 * 	folder containing the collection cache images.
	 * @param osData The output stream to which the collection data is
	 * 	written
	 */
    protected boolean toStream(File file, OutputStream osData, IItemProgressMonitor monitor) {
        int nMonitorItemsCount = m_listItems.size();
        int nMonitorStep = 1;
        if (nMonitorItemsCount > 500) {
            nMonitorItemsCount /= 100;
            nMonitorStep = 100;
        }
        monitor.beginTask(m_strFilename, nMonitorItemsCount);
        PrintWriter p = null;
        try {
            p = new PrintWriter(new OutputStreamWriter(osData, "UTF8"));
        } catch (UnsupportedEncodingException e) {
            p = new PrintWriter(osData);
        }
        p.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
        p.print("<Collection");
        XMLUtil.printAttribute(p, "numberOfItems", String.valueOf(m_listItems.size()));
        m_metadata.toNode(p);
        p.println('>');
        m_template.getMetaData().toNodeDatabaseProxy(p);
        p.println("\t<FieldDefinitions>");
        m_template.toNode(p);
        p.println("\t</FieldDefinitions>");
        p.println("\t<CollectionCategories>");
        m_categoryRoot.toNode(file, p, 0);
        p.println("\t</CollectionCategories>");
        p.println("\t<Items>");
        int i = 0;
        for (IItem item : m_listItems) {
            ((Item) item).toNode(file, p);
            i++;
            if (i == nMonitorStep) {
                if (monitor.isCanceled()) return false;
                monitor.step(item);
                i = 0;
            }
        }
        p.println("\t</Items>");
        p.println("</Collection>");
        p.flush();
        return true;
    }

    public IItemDatabaseProxy getDatabaseProxy() {
        if (m_template == null) return null;
        if (m_template.getMetaData() == null) return null;
        return m_template.getMetaData().getDatabaseProxy();
    }

    public IItemDatabaseProxyConfiguration getDatabaseProxyConfiguration() {
        if (m_template == null) return null;
        if (m_template.getMetaData() == null) return null;
        return m_template.getMetaData().getDatabaseProxyConfiguration();
    }

    public void onMediaAdding(IItem item, IMedium[] rgMedia) {
    }

    public void onMediaAdded(IItem item, IMedium[] rgMedia) {
        m_bIsDirty = true;
    }

    public void onMediaRemoving(IItem item, IMedium[] rgMedia) {
    }

    public void onMediaRemoved(IItem item, IMedium[] rgMedia) {
        m_bIsDirty = true;
    }

    public void onValueChanging(IItem item, IField field) {
    }

    public void onValueChanged(IItem item, IField field) {
        if (field.getTitleFunction() != TitleFunction.NO_TITLE) {
            int nIdxOld = 0;
            for (IItem itemTmp : m_listItems) {
                if (itemTmp == item) break;
                nIdxOld++;
            }
            m_listItems.remove(nIdxOld);
            int nIdxNew = getIndex(m_listItems, item);
            m_listItems.add(nIdxNew, item);
            if (nIdxOld != nIdxNew) fireCollectionChanged(item, ICollectionListener.CollectionChangedType.ITEM_REORDERED, nIdxNew);
        }
        m_bIsDirty = true;
    }

    public void onBeginChangeMultipleValues(IItem item, ChangeMultipleValuesHint hint) {
    }

    public void onEndChangeMultipleValues(IItem item, ChangeMultipleValuesHint hint) {
    }

    /**
	 * Adds a listener to the collection that is notified whenever the collection changes.
	 * @param listener The listener to add
	 */
    public void addCollectionListener(ICollectionListener listener) {
        m_listListeners.add(listener);
    }

    /**
	 * Removes the listener <code>listener</code> from the collection.
	 * @param listener The listener to remove
	 */
    public void removeCollectionListener(ICollectionListener listener) {
        m_listListeners.remove(listener);
    }

    /**
	 * Fires a collection changed event.
	 * @param item The item that has been added or removed
	 * @param type The type of the change (add/remove/...)
	 * @param nIndex The new index of the item
	 */
    protected void fireCollectionChanged(IItem item, ICollectionListener.CollectionChangedType type, int nIndex) {
        for (ICollectionListener l : m_listListeners) l.onCollectionChanged(this, item, type, nIndex);
        m_bIsDirty = true;
    }

    protected void fireTemplateChanging() {
        ITemplate template = getTemplate();
        for (ICollectionListener l : m_listListeners) l.onTemplateChanging(this, template, m_listItems);
    }

    /**
	 * Fires a template changed event.
	 * @param listItems The list of items that have changed as a consequence of the template modification
	 */
    protected void fireTemplateChanged(Iterable<IItem> itItems) {
        ITemplate template = getTemplate();
        for (ICollectionListener l : m_listListeners) l.onTemplateChanged(this, m_templateOld, template, itItems);
        m_bIsDirty = true;
    }

    /**
	 * Fires an event notifying the listeners that the name of the collection has changed.
	 * @param strOldName The old collection name
	 */
    protected void fireCollectionNameChanged(String strOldName) {
        for (ICollectionListener l : m_listListeners) l.onCollectionNameChanged(this, strOldName, m_strName);
    }

    /**
	 * Notifies the collection listeners that the collection has been loaded.
	 */
    protected void fireCollectionLoaded() {
        for (ICollectionListener l : m_listListeners) l.onCollectionLoaded(this);
    }

    public void onConfigurationChanged(String strProperty) {
        m_bIsDirty = true;
    }

    public int getIndex(List<IItem> list, IItem item) {
        return Collection.getItemIndex(list == null ? m_listItems : list, item);
    }

    /**
	 * Returns the index of the item <code>item</code> within the list <code>list</code>.
	 * The list is assumed to be sorted.
	 * If the item does not exist in the list, a value is returned at which the item
	 * has to be inserted into the list such that the list remains sorted.
	 * @param list The list in which the item is searched
	 * @param item The item to search
	 * @return The index of the item in the list
	 */
    public static int getItemIndex(List<? extends IItem> list, IItem item) {
        if (list == null) return -1;
        int nIdx = Collections.binarySearch(list, item);
        if (nIdx < 0) nIdx = -nIdx - 1; else {
            nIdx = Collection.findItemIndex(list, item, nIdx);
        }
        return nIdx;
    }

    /**
	 * Searches the list <code>listItems</code> for the item <code>item</code> starting from
	 * the user-provided starting point <code>nIdx</code>.
	 * @param listItems The list that is searched
	 * @param item The item to find
	 * @param nIdx The starting point at which the search is started. The search proceeds in
	 * 	both directions until the item is found.
	 * @return The index in the list corresponding to <code>item</code> or the initial starting
	 * 	point if the item hasn't been found
	 */
    public static int findItemIndex(List<? extends IItem> listItems, IItem item, int nIdx) {
        if (listItems.get(nIdx) == item) return nIdx;
        String strTitle = item.getTitle();
        char chTitle = strTitle == null || strTitle.length() == 0 ? '\0' : strTitle.charAt(0);
        for (int i = nIdx + 1; i < listItems.size(); i++) {
            IItem itemTest = listItems.get(i);
            String strTitleTest = itemTest.getTitle();
            char chTitleTest = strTitleTest == null || strTitleTest.length() == 0 ? '\0' : strTitleTest.charAt(0);
            if (chTitle != chTitleTest) break;
            if (itemTest == item) return i;
        }
        for (int i = nIdx - 1; i >= 0; i--) {
            IItem itemTest = listItems.get(i);
            String strTitleTest = itemTest.getTitle();
            char chTitleTest = strTitleTest == null || strTitleTest.length() == 0 ? '\0' : strTitleTest.charAt(0);
            if (chTitle != chTitleTest) break;
            if (itemTest == item) return i;
        }
        for (int i = nIdx + 1; i < listItems.size(); i++) if (!strTitle.equals(listItems.get(i).getTitle())) return i;
        return nIdx;
    }

    public void addMediumToDelete(IMedium medium) {
        if (m_listMediaToDelete == null) m_listMediaToDelete = new LinkedList<IMedium>();
        m_listMediaToDelete.add(medium);
    }

    public void removeMediumToDelete(IMedium medium) {
        if (m_listMediaToDelete != null) m_listMediaToDelete.remove(medium);
    }

    public void deleteMedia() {
        if (m_listMediaToDelete == null) return;
        for (IMedium m : m_listMediaToDelete) m.deleteImages();
        m_listMediaToDelete.clear();
    }

    public void onModifyingTemplate(ITemplate template) {
        m_templateOld = ((Template) template).clone();
    }

    /**
	 * Checks whether the name of the field has changed and if so, updates
	 * the items and adds the it item to the set of changed items <code>setItemChanged</code>.
	 * @param fieldNew The new field definition
	 * @param fieldOld The old field definition
	 * @param setItemsChanged The method fills in the items that have changed into this set
	 */
    private void checkNameChange(IField fieldNew, IField fieldOld, Set<IItem> setItemsChanged) {
        String strNameNew = fieldNew.getName() == null ? "" : fieldNew.getName();
        if (!strNameNew.equals(fieldOld.getName())) {
            for (IItem item : this) {
                if (item.setValue(fieldNew, item.getValue(fieldOld))) setItemsChanged.add(item);
                item.removeValue(fieldOld);
            }
        }
    }

    /**
	 * Checks whether the display as list attribute of the field has changed and if so, updates
	 * the items and adds the it item to the set of changed items <code>setItemChanged</code>.
	 * The item update consists in concatenating the list values if the &quot;display as list&quot;
	 * attribute has changed from true to false. In all other cases no modifications are done to the items.
	 * @param fieldNew The new field definition
	 * @param fieldOld The old field definition
	 * @param setItemsChanged The method fills in the items that have changed into this set
	 */
    private void checkDisplayAsListChange(IField fieldNew, IField fieldOld, Set<IItem> setItemsChanged) {
        if (!fieldNew.isDisplayedAsList() && fieldOld.isDisplayedAsList()) {
            for (IItem item : this) {
                List<String> listValues = item.getValue(fieldOld);
                if (listValues.size() > 1) {
                    StringBuffer sb = new StringBuffer();
                    for (String strValue : listValues) {
                        sb.append(strValue);
                        sb.append(", ");
                    }
                    if (item.setValue(fieldNew, sb.substring(0, sb.length() - 2))) setItemsChanged.add(item);
                }
            }
        }
    }

    /**
	 * Checks whether the name of the field has changed and if so, resets the item caches holding
	 * the item's title and subtitle string.
	 * @param fieldNew The new field definition
	 * @param fieldOld The old field definition
	 * @param setItemsChanged The method fills in the items that have changed into this set
	 */
    private void checkTitleFunctionChanged(IField fieldNew, IField fieldOld, Set<IItem> setItemsChanged) {
        if (fieldNew.getTitleFunction() != fieldOld.getTitleFunction()) for (IItem item : this) ((Item) item).resetCaches();
    }

    private static final byte LIST_STRUCTURE_CHANGED = 1;

    private static final byte LIST_VALUES_CHANGED = 2;

    /**
	 * Checks whether the name of the field has changed and if so, updates
	 * the items and adds the it item to the set of changed items <code>setItemChanged</code>.
	 * The item updates occurs only if values have been deleted from the old field definition.
	 * @param fieldNew The new field definition
	 * @param fieldOld The old field definition
	 * @param setItemsChanged The method fills in the items that have changed into this set
	 */
    private byte checkPossibleValuesChanged(IField fieldNew, IField fieldOld, Set<IItem> setItemsChanged) {
        if (!fieldNew.hasPossibleValues()) return 0;
        boolean bListChanged = !fieldOld.hasPossibleValues();
        boolean bValuesChanged = false;
        Map<String, EnumeratedValue> mapOldValues = null;
        if (!bListChanged) {
            mapOldValues = new HashMap<String, EnumeratedValue>();
            for (EnumeratedValue value : fieldOld.getPossibleValues()) mapOldValues.put(value.getMagicNumberAsString(), value);
            for (EnumeratedValue value : fieldNew.getPossibleValues()) {
                EnumeratedValue valueOld = mapOldValues.remove(value.getMagicNumberAsString());
                if (valueOld == null) {
                    bListChanged = true;
                } else if (!bValuesChanged) {
                    bValuesChanged = value.getText() == null ? valueOld.getText() != null : !value.getText().equals(valueOld.getText());
                }
            }
            if (!bListChanged) bListChanged = mapOldValues.size() > 0;
        }
        if (!bListChanged) return bValuesChanged ? LIST_VALUES_CHANGED : 0;
        if (mapOldValues != null) {
            for (IItem item : this) {
                boolean bItemChanged = false;
                List<String> listValues = item.getReferences(fieldOld);
                if (listValues != null) for (Iterator<String> it = listValues.iterator(); it.hasNext(); ) if (mapOldValues.containsKey(it.next())) {
                    it.remove();
                    bItemChanged = true;
                }
                if (bItemChanged) setItemsChanged.add(item);
            }
        }
        return (byte) (LIST_STRUCTURE_CHANGED | (bValuesChanged ? LIST_VALUES_CHANGED : 0));
    }

    public void onTemplateModified(ITemplate template) {
        fireTemplateChanging();
        Set<IItem> setItemsChanged = new HashSet<IItem>();
        Map<IField, Object> mapOldFields = new HashMap<IField, Object>();
        for (IField f : m_templateOld) mapOldFields.put(f, SOMETHING);
        for (IField fieldNew : template) {
            IField fieldOld = m_templateOld.getFieldByMagicNumber(fieldNew.getMagicNumber());
            mapOldFields.remove(fieldOld);
            checkNameChange(fieldNew, fieldOld, setItemsChanged);
            checkDisplayAsListChange(fieldNew, fieldOld, setItemsChanged);
            checkTitleFunctionChanged(fieldNew, fieldOld, setItemsChanged);
            checkPossibleValuesChanged(fieldNew, fieldOld, setItemsChanged);
        }
        for (IField fieldOld : mapOldFields.keySet()) for (IItem item : this) {
            item.removeValue(fieldOld);
            setItemsChanged.add(item);
        }
        fireTemplateChanged(setItemsChanged);
    }

    public Category getCategoryByRef(String strCategoryRef) {
        return m_mapCategories.get(strCategoryRef);
    }
}
