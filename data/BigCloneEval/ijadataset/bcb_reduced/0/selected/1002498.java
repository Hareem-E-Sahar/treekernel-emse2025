package org.jtools.util.props;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class SimplePropertyRepository implements PropertyRepository {

    public static class Factory implements PropertyRepository.Factory {

        public PropertyRepository createRepository(@SuppressWarnings("unused") PropertySupport propSupport) {
            return new SimplePropertyRepository();
        }
    }

    protected static class PropertyEntry implements PropertyRepositoryEntry {

        protected static class SimpleDirectoryFilter implements FilenameFilter {

            public boolean accept(@SuppressWarnings("unused") File dir, @SuppressWarnings("unused") String name) {
                return true;
            }
        }

        protected static class SimpleFileFilter implements FilenameFilter {

            private final String[] excludes;

            private final String extension;

            public SimpleFileFilter(String Extension, String[] Excludes) {
                this.extension = Extension;
                this.excludes = Excludes;
            }

            public boolean accept(File dir, String name) {
                if (!name.endsWith(extension)) return false;
                if (excludes != null) for (String exclude : excludes) if ((exclude != null) && name.equals(exclude + extension)) return false;
                return true;
            }
        }

        private static final FilenameFilter DNF = new SimpleDirectoryFilter();

        private final boolean addTargetAsSuffix;

        private final String baseFilename;

        private final String concatListProperty;

        private final boolean dynamic;

        private final String entryKey;

        private final ItemType itemType;

        private final String listProperty;

        private final String mainEntryFilename;

        private final PropertyEntry parentEntry;

        private final String prefix;

        private final String staticDirectoryName;

        private List<PropertyRepositoryEntry> subEntries = new ArrayList<PropertyRepositoryEntry>(5);

        public PropertyEntry(PropertyEntry Parent, String key, ItemType itemType, boolean isDynamic, String MainEntry, String staticDirectoryName, String BaseFile, String Prefix, String ListProperty, String concatListProperty, boolean addTargetAsSuffix) {
            this.parentEntry = Parent;
            this.entryKey = ((key == null) ? "<unknown>" : key);
            this.itemType = itemType;
            this.dynamic = isDynamic;
            this.mainEntryFilename = ((MainEntry == null) ? "" : MainEntry.trim());
            this.staticDirectoryName = ((staticDirectoryName == null) ? "" : staticDirectoryName.trim());
            this.baseFilename = ((BaseFile == null) ? "" : BaseFile.trim());
            this.prefix = Prefix;
            this.listProperty = ListProperty;
            this.concatListProperty = concatListProperty;
            this.addTargetAsSuffix = addTargetAsSuffix;
        }

        public boolean addTargetAsSuffix() {
            return addTargetAsSuffix;
        }

        public PropertyRepositoryEntry createDirSubEntry(String key, String staticDirectoryName, String BaseFile, String MainEntry, String Prefix, String ListProperty, String concatListProperty, boolean addTargetAsSuffix) {
            if (this.addTargetAsSuffix) throw new RuntimeException("an entry with addTargetAsSuffix=true can not have subentries");
            PropertyEntry x = new PropertyEntry(this, key, PropertyRepositoryEntry.ItemType.DIR, isChildDynamic(), MainEntry, staticDirectoryName, BaseFile, Prefix, ListProperty, concatListProperty, addTargetAsSuffix);
            subEntries.add(x);
            return x;
        }

        public PropertyRepositoryEntry createFileSubEntry(String key, String staticDirectoryName, String BaseFile, String Prefix, String ListProperty, String concatListProperty, boolean addTargetAsSuffix) {
            if (this.addTargetAsSuffix) throw new RuntimeException("an entry with addTargetAsSuffix=true can not have subentries");
            PropertyEntry x = new PropertyEntry(this, key, PropertyRepositoryEntry.ItemType.FILE, isChildDynamic(), null, staticDirectoryName, BaseFile, Prefix, ListProperty, concatListProperty, addTargetAsSuffix);
            subEntries.add(x);
            return x;
        }

        private String createDirectoryName(String... targets) {
            String parentresult = ((parentEntry != null) ? parentEntry.createDirectoryName(targets) : null);
            if ("".equals(parentresult)) parentresult = null;
            String myresult = null;
            myresult = ("".equals(staticDirectoryName) ? null : staticDirectoryName);
            if (getItemType() == PropertyRepositoryEntry.ItemType.DIR) {
                int level = getEntryLevel();
                if (targets.length > level) {
                    if (myresult == null) myresult = targets[level]; else if (targets[level] != null) myresult += "/" + targets[level];
                }
            }
            if ((parentresult == null) && (myresult == null)) return "";
            if ((parentresult != null) && (myresult != null)) return parentresult + "/" + myresult;
            if (parentresult != null) return parentresult;
            return myresult;
        }

        public String getBaseFilename() {
            return baseFilename;
        }

        public String getConcatListProperty() {
            return concatListProperty;
        }

        public String getEntryKey() {
            if (parentEntry == null) return entryKey;
            return parentEntry.getEntryKey() + "/" + entryKey;
        }

        public ItemType getItemType() {
            return itemType;
        }

        public String getKeyPrefix() {
            return prefix;
        }

        public String getListProperty() {
            return listProperty;
        }

        public String getMainEntryFilename() {
            return mainEntryFilename;
        }

        public PropertyRepositoryEntry getParentEntry() {
            return parentEntry;
        }

        public String getStaticDirectoryName() {
            return staticDirectoryName;
        }

        private boolean isChildDynamic() {
            return (getItemType() != PropertyRepositoryEntry.ItemType.FILE);
        }

        public boolean isDynamic() {
            return dynamic;
        }

        private int getEntryLevel() {
            if (parentEntry == null) return 0;
            return (parentEntry.getEntryLevel() + 1);
        }

        private String createListProperty(String[] targets) {
            String listProperty = getListProperty();
            if (listProperty == null) return null;
            if (parentEntry == null) return listProperty;
            String parentresult = parentEntry.createTargets(targets);
            if ((parentresult == null) || (parentresult.length() == 0)) return listProperty;
            return parentresult + "." + listProperty;
        }

        public int read(PropertySupport dest, File repositoryBase, String Extension, String... targets) throws IOException {
            int resultCount = 0;
            final boolean itemsAreFiles = (getItemType() == PropertyRepositoryEntry.ItemType.FILE);
            File dir = new File(repositoryBase, createDirectoryName(targets));
            if (dir.isDirectory()) {
                String targetlist = null;
                String[] excludes = { getBaseFilename(), null };
                if (dynamic) excludes[1] = parentEntry.getMainEntryFilename();
                String[] files = dir.list((itemsAreFiles ? new SimpleFileFilter(Extension, excludes) : DNF));
                String[] mytargets = new String[targets.length + 1];
                System.arraycopy(targets, 0, mytargets, 0, targets.length);
                for (String fileName : files) {
                    File f = new File(dir, fileName);
                    if (itemsAreFiles) {
                        if (f.isFile()) {
                            int ppos = fileName.lastIndexOf(Extension);
                            String target = fileName.substring(0, ppos);
                            mytargets[targets.length] = target;
                            resultCount += readFile(dest, f, mytargets, false);
                            if (targetlist == null) targetlist = target; else targetlist += "," + target;
                        }
                    } else {
                        if (f.isDirectory()) {
                            String target = fileName;
                            mytargets[targets.length] = target;
                            for (PropertyRepositoryEntry subI : subEntries) {
                                PropertyEntry sub = (PropertyEntry) subI;
                                resultCount += sub.read(dest, repositoryBase, Extension, mytargets);
                            }
                            resultCount += readFile(dest, new File(f, mainEntryFilename + Extension), mytargets, false);
                            if (targetlist == null) targetlist = target; else targetlist += "," + target;
                        }
                    }
                }
                mytargets[targets.length] = null;
                if (itemsAreFiles) for (PropertyRepositoryEntry subI : subEntries) {
                    PropertyEntry sub = (PropertyEntry) subI;
                    resultCount += sub.read(dest, repositoryBase, Extension, mytargets);
                }
                if (!"".equals(baseFilename)) {
                    File bf = new File(dir, baseFilename + Extension);
                    resultCount += readFile(dest, bf, mytargets, true);
                }
                String list = createListProperty(targets);
                if (list != null) {
                    if (getConcatListProperty() != null) {
                        if (targetlist == null) targetlist = "@[" + dest.getPropertyDomain() + list + "." + getConcatListProperty() + "]"; else targetlist = targetlist + ",@[" + dest.getPropertyDomain() + list + "." + getConcatListProperty() + "]";
                    } else if (targetlist == null) targetlist = "";
                    dest.getProperties().put(dest.getPropertyDomain() + list + ".repository", targetlist);
                    if (!dest.getProperties().containsKey(dest.getPropertyDomain() + list)) dest.getProperties().put(dest.getPropertyDomain() + list, "@[" + dest.getPropertyDomain() + list + ".repository]");
                }
            }
            return resultCount;
        }

        private int readFile(PropertySupport dest, File fn, String[] targets, boolean basefile) throws IOException {
            if (fn.isFile()) {
                Properties fp = new Properties();
                fp.load(new FileInputStream(fn));
                String prefix = TargetPrefix(targets, basefile);
                String suffix = (basefile ? "" : TargetSuffix(targets));
                for (Iterator<Object> i = fp.keySet().iterator(); i.hasNext(); ) {
                    String p = ((String) i.next()).trim();
                    if (p.length() > 0) {
                        dest.getProperties().put(dest.getPropertyDomain() + prefix + p + suffix, fp.getProperty(p));
                    }
                }
                return 1;
            }
            return 0;
        }

        private String TargetPrefix(String[] targets, boolean basefile) {
            String parentresult = ((parentEntry != null) ? parentEntry.createTargets(targets) : null);
            if ("".equals(parentresult)) parentresult = null;
            String myresult = null;
            if (!basefile) {
                int level = getEntryLevel();
                myresult = prefix;
                if ((targets[level] != null) && (!addTargetAsSuffix)) {
                    if (prefix != null) myresult += "." + targets[level]; else myresult = targets[level];
                }
            }
            if ((parentresult == null) && (myresult == null)) return "";
            if ((parentresult != null) && (myresult != null)) return parentresult + "." + myresult + ".";
            if (parentresult != null) return parentresult + ".";
            return myresult + ".";
        }

        private String createTargets(String[] targets) {
            String parentresult = ((parentEntry != null) ? parentEntry.createTargets(targets) : null);
            if ("".equals(parentresult)) parentresult = null;
            int level = getEntryLevel();
            String myresult = prefix;
            if (targets[level] != null) {
                if (prefix != null) myresult += "." + targets[level]; else myresult = targets[level];
            }
            if (parentresult == null) parentresult = ""; else if (myresult != null) parentresult += ".";
            if (myresult == null) myresult = "";
            return parentresult + myresult;
        }

        private String TargetSuffix(String[] targets) {
            if (!addTargetAsSuffix) return "";
            int level = getEntryLevel();
            if (targets[level] == null) return "";
            return "." + targets[level];
        }

        @Override
        public String toString() {
            return getEntryKey();
        }
    }

    private static SimplePropertyRepository.Factory factory = new SimplePropertyRepository.Factory();

    public static PropertyRepository.Factory getFactory() {
        return factory;
    }

    private List<PropertyRepositoryEntry> mainentries = new ArrayList<PropertyRepositoryEntry>(10);

    public PropertyRepositoryEntry createDirEntry(String key, String Directory, String BaseFile, String MainEntry, String Prefix, String ListProperty, String concatListProperty, boolean addTargetAsSuffix) {
        PropertyRepositoryEntry x = new PropertyEntry(null, key, PropertyRepositoryEntry.ItemType.DIR, false, MainEntry, Directory, BaseFile, Prefix, ListProperty, concatListProperty, addTargetAsSuffix);
        mainentries.add(x);
        return x;
    }

    public PropertyRepositoryEntry createFileEntry(String key, String Directory, String BaseFile, String Prefix, String ListProperty, String concatListProperty, boolean addTargetAsSuffix) {
        PropertyRepositoryEntry x = new PropertyEntry(null, key, PropertyRepositoryEntry.ItemType.FILE, false, null, Directory, BaseFile, Prefix, ListProperty, concatListProperty, addTargetAsSuffix);
        mainentries.add(x);
        return x;
    }

    public Collection<PropertyRepositoryEntry> getEntries() {
        return mainentries;
    }

    public int read(PropertySupport dest, File repositoryBase, String Extension) throws IOException {
        int x = 0;
        for (PropertyRepositoryEntry entry : getEntries()) if (entry != null) x += entry.read(dest, repositoryBase, Extension);
        return x;
    }
}
