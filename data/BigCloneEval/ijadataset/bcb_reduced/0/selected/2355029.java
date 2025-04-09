package ti.plato.scripts.ordering;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import ti.io.VersionedExternalizable;
import ti.mcore.Environment;
import ti.mcore.u.PluginUtil;
import ti.plato.scripts.ScriptsPlugin;

public class ScriptList extends VersionedExternalizable {

    private static int VERSION = 1;

    private static String SCRIPT_ROOT = "/Scripts/";

    private ArrayList<ScriptItem> items = new ArrayList<ScriptItem>();

    private boolean isLoaded = false;

    private static String preWorkspace = "WORKSPACE";

    private static String preDocuments = "DOCUMENTS";

    private static String prePlugIn = "PLUGIN";

    private static String preOther = "OTHER";

    @Override
    protected int getCurrentVersion() {
        return VERSION;
    }

    @Override
    protected void readVersioned(int version, DataInput in) throws IOException {
        items = new ArrayList<ScriptItem>();
        load(in, items);
        if (items.size() != 0) isLoaded = true;
        synchronize();
    }

    private void load(DataInput in, ArrayList<ScriptItem> internalItems) {
        String workspaceDir = ti.plato.util.WorkspaceManagement.getDefault().getCurrentWorkspaceDirectory();
        String myDocumentsDir = Environment.getEnvironment().getDocumentsDirectory().getAbsolutePath();
        String scriptPluginDir = new File(PluginUtil.getResource(ScriptsPlugin.getDefault(), "")).getAbsolutePath();
        try {
            int internalItemsCount = in.readInt();
            if (internalItemsCount != 0) {
                int internalItemsIndex;
                for (internalItemsIndex = 0; internalItemsIndex < internalItemsCount; internalItemsIndex++) {
                    String relativePath = in.readUTF();
                    String absolutePath = in.readUTF();
                    boolean doAdd = true;
                    if (absolutePath.startsWith(preWorkspace)) absolutePath = workspaceDir + absolutePath.substring(preWorkspace.length()); else if (absolutePath.startsWith(preDocuments)) absolutePath = myDocumentsDir + absolutePath.substring(preDocuments.length()); else if (absolutePath.startsWith(prePlugIn)) absolutePath = scriptPluginDir + absolutePath.substring(prePlugIn.length()); else if (absolutePath.startsWith(preOther)) absolutePath = absolutePath.replaceFirst(preOther, ""); else doAdd = false;
                    boolean hasItems = in.readBoolean();
                    if (!hasItems) {
                        ScriptItem item = new ScriptItem(absolutePath, relativePath, null, internalItems);
                        if (doAdd) internalItems.add(item);
                    } else {
                        ArrayList<ScriptItem> internalItemsTmp = new ArrayList<ScriptItem>();
                        ScriptItem item = new ScriptItem(absolutePath, relativePath, internalItemsTmp, internalItems);
                        if (doAdd) internalItems.add(item);
                        load(in, internalItemsTmp);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void writeVersioned(DataOutput out) throws IOException {
        synchronize();
        save(out, items);
    }

    private void save(DataOutput out, ArrayList<ScriptItem> internalItems) {
        String workspaceDir = ti.plato.util.WorkspaceManagement.getDefault().getCurrentWorkspaceDirectory();
        String myDocumentsDir = Environment.getEnvironment().getDocumentsDirectory().getAbsolutePath();
        String scriptPluginDir = new File(PluginUtil.getResource(ScriptsPlugin.getDefault(), "")).getAbsolutePath();
        try {
            int internalItemsCount = internalItems.size();
            int internalItemsIndex;
            out.writeInt(internalItemsCount);
            for (internalItemsIndex = 0; internalItemsIndex < internalItemsCount; internalItemsIndex++) {
                ScriptItem item = internalItems.get(internalItemsIndex);
                out.writeUTF(item.getRelativePath());
                String absolutePath = item.getAbsolutePath();
                if (absolutePath.startsWith(workspaceDir)) absolutePath = absolutePath.replaceFirst("\\Q" + workspaceDir + "\\E", preWorkspace); else if (absolutePath.startsWith(myDocumentsDir)) absolutePath = absolutePath.replaceFirst("\\Q" + myDocumentsDir + "\\E", preDocuments); else if (absolutePath.startsWith(scriptPluginDir)) absolutePath = absolutePath.replaceFirst("\\Q" + scriptPluginDir + "\\E", prePlugIn); else absolutePath = preOther + absolutePath;
                out.writeUTF(absolutePath);
                if (item.hasItems()) {
                    out.writeBoolean(true);
                    save(out, item.getItems());
                } else {
                    out.writeBoolean(false);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        synchronize();
    }

    public void setPosition(String absolutePath, int position) {
        synchronize();
        ScriptItem item = getItemFromAbsolutePath(absolutePath, items);
        if (item == null) return;
        ArrayList<ScriptItem> parent = item.getParent();
        parent.remove(item);
        parent.add(position, item);
        if (parent == null) return;
    }

    private void synchronize() {
        deleteUnexistingItems(items, true);
        detectNewItems(SCRIPT_ROOT, items, -1);
    }

    private ScriptItem getItemFromAbsolutePath(String absolutePath, ArrayList<ScriptItem> internalItems) {
        int internalItemsCount = internalItems.size();
        int internalItemsIndex;
        for (internalItemsIndex = 0; internalItemsIndex < internalItemsCount; internalItemsIndex++) {
            ScriptItem item = internalItems.get(internalItemsIndex);
            if (item.getAbsolutePath().equals(absolutePath)) return item;
            if (item.hasItems()) {
                ScriptItem item2 = getItemFromAbsolutePath(absolutePath, item.getItems());
                if (item2 != null) return item2;
            }
        }
        return null;
    }

    public ArrayList<ScriptItem> getItemsFromRelativePath(String relativePath) {
        if (relativePath.equals(SCRIPT_ROOT)) return items;
        return getItemsFromRelativePath(relativePath, items);
    }

    private ArrayList<ScriptItem> getItemsFromRelativePath(String relativePath, ArrayList<ScriptItem> internalItems) {
        int internalItemsCount = internalItems.size();
        int internalItemsIndex;
        for (internalItemsIndex = 0; internalItemsIndex < internalItemsCount; internalItemsIndex++) {
            ScriptItem item = internalItems.get(internalItemsIndex);
            if (item.getRelativePath().equals(relativePath)) return item.getItems();
            if (item.hasItems()) {
                ArrayList<ScriptItem> itemList2 = getItemsFromRelativePath(relativePath, item.getItems());
                if (itemList2 != null) return itemList2;
            }
        }
        return null;
    }

    private void deleteUnexistingItems(ArrayList<ScriptItem> internalItems, boolean recursive) {
        IWorkbench workbench = null;
        try {
            workbench = PlatformUI.getWorkbench();
        } catch (Exception e) {
            workbench = null;
        }
        ArrayList<ScriptItem> listToDelete = new ArrayList<ScriptItem>();
        int internalItemsCount = internalItems.size();
        int internalItemsIndex;
        for (internalItemsIndex = 0; internalItemsIndex < internalItemsCount; internalItemsIndex++) {
            ScriptItem item = internalItems.get(internalItemsIndex);
            if (workbench != null && !ScriptsPlugin.getDefault().isResolvable(item.getRelativePath())) {
                listToDelete.add(item);
            } else if (!item.exists()) listToDelete.add(item); else if (item.hasItems() != item.isDirectory()) listToDelete.add(item); else if (recursive && item.hasItems()) deleteUnexistingItems(item.getItems(), recursive);
        }
        int listToDeleteCount = listToDelete.size();
        int listToDeleteIndex;
        for (listToDeleteIndex = 0; listToDeleteIndex < listToDeleteCount; listToDeleteIndex++) {
            internalItems.remove(listToDelete.get(listToDeleteIndex));
        }
    }

    private void detectNewItems(String relativePath, ArrayList<ScriptItem> internalItems, int recursiveLevel) {
        for (Iterator itr = ScriptsPlugin.getDefault().getFiles(relativePath).iterator(); itr.hasNext(); ) {
            File file = (File) (itr.next());
            String name = file.getName();
            if (file.canRead() && (file.isDirectory() || (name.endsWith(".os") && !name.endsWith("_.os"))) && !name.startsWith(".")) {
                String relativePath2 = relativePath + "/" + file.getName();
                ScriptItem existingItem = isFileInList(relativePath2, internalItems);
                if (file.isDirectory()) {
                    boolean directoryAdded = false;
                    if (existingItem == null) {
                        directoryAdded = true;
                        existingItem = new ScriptItem(file.getAbsolutePath(), relativePath2, new ArrayList<ScriptItem>(), internalItems);
                        if (!isLoaded) alphabeticallyInsert(internalItems, existingItem); else internalItems.add(existingItem);
                    }
                    if (directoryAdded || (recursiveLevel > 0 || recursiveLevel == -1)) {
                        int newRecursiveLevel = recursiveLevel;
                        if (newRecursiveLevel > 0) newRecursiveLevel--;
                        detectNewItems(relativePath2, existingItem.getItems(), newRecursiveLevel);
                    }
                } else {
                    if (existingItem == null) {
                        existingItem = new ScriptItem(file.getAbsolutePath(), relativePath2, null, internalItems);
                        if (!isLoaded) alphabeticallyInsert(internalItems, existingItem); else internalItems.add(existingItem);
                    }
                }
            }
        }
    }

    private void alphabeticallyInsert(ArrayList<ScriptItem> internalItems, ScriptItem item) {
        int internalItemsCount = internalItems.size();
        int internalItemsIndex;
        for (internalItemsIndex = 0; internalItemsIndex < internalItemsCount; internalItemsIndex++) {
            ScriptItem item2 = internalItems.get(internalItemsIndex);
            if (item2.getName().compareToIgnoreCase(item.getName()) > 0) {
                internalItems.add(internalItemsIndex, item);
                return;
            }
        }
        internalItems.add(item);
    }

    private ScriptItem isFileInList(String relativePath, ArrayList<ScriptItem> internalItems) {
        if (internalItems == null) {
            return null;
        }
        int internalItemsCount = internalItems.size();
        int internalItemsIndex;
        for (internalItemsIndex = 0; internalItemsIndex < internalItemsCount; internalItemsIndex++) {
            ScriptItem item = internalItems.get(internalItemsIndex);
            if (relativePath.equals(item.getRelativePath())) return item;
        }
        return null;
    }

    public int compare(String absolutePath1, String absolutePath2) {
        ScriptItem item1 = getItemFromAbsolutePath(absolutePath1, items);
        ScriptItem item2 = getItemFromAbsolutePath(absolutePath2, items);
        int pos1 = item1.getPos();
        int pos2 = item2.getPos();
        return pos1 - pos2;
    }

    public File[] listFiles(String absolutePath) {
        File file = new File(absolutePath);
        if (!file.exists() || !file.isDirectory()) return null;
        ScriptItem item = getItemFromAbsolutePath(absolutePath, items);
        ArrayList<ScriptItem> children = null;
        if (item == null) children = items; else children = item.getItems();
        if (children == null || children.size() == 0) return null;
        deleteUnexistingItems(children, false);
        detectNewItems((item == null) ? SCRIPT_ROOT : item.getRelativePath(), children, 1);
        if (children == null || children.size() == 0) return null;
        int childrenCount = children.size();
        File[] result = new File[childrenCount];
        int childrenIndex;
        for (childrenIndex = 0; childrenIndex < childrenCount; childrenIndex++) {
            ScriptItem child = children.get(childrenIndex);
            result[childrenIndex] = child.getFile();
        }
        return result;
    }

    public void rename(String originalPath, String destinationPath) {
        ScriptItem scriptItem = getItemFromAbsolutePath(originalPath, items);
        if (scriptItem == null) return;
        scriptItem.rename(destinationPath);
    }
}
