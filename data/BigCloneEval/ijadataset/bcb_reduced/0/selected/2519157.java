package jdos.dos;

import jdos.misc.Cross;
import jdos.misc.Log;
import jdos.types.LogSeverities;
import jdos.types.LogTypes;
import jdos.util.BooleanRef;
import jdos.util.IntRef;
import jdos.util.StringHelper;
import jdos.util.StringRef;
import java.io.File;
import java.util.Comparator;
import java.util.Vector;

public class DOS_Drive_Cache {

    Comparator SortByName = new Comparator() {

        public int compare(Object o1, Object o2) {
            return ((CFileInfo) o1).shortname.compareTo(((CFileInfo) o2).shortname);
        }
    };

    Comparator SortByDirName = new Comparator() {

        public int compare(Object o1, Object o2) {
            CFileInfo a = (CFileInfo) o1;
            CFileInfo b = (CFileInfo) o2;
            if (a.isDir != b.isDir) return a.isDir ? 1 : -1;
            return a.shortname.compareTo(b.shortname);
        }
    };

    Comparator SortByDirNameRev = new Comparator() {

        public int compare(Object o1, Object o2) {
            CFileInfo a = (CFileInfo) o1;
            CFileInfo b = (CFileInfo) o2;
            if (a.isDir != b.isDir) return a.isDir ? 1 : -1;
            return b.shortname.compareTo(a.shortname);
        }
    };

    Comparator SortByNameRev = new Comparator() {

        public int compare(Object o1, Object o2) {
            CFileInfo a = (CFileInfo) o1;
            CFileInfo b = (CFileInfo) o2;
            return b.shortname.compareTo(a.shortname);
        }
    };

    public static final int MAX_OPENDIRS = 2048;

    public DOS_Drive_Cache() {
        dirBase = new CFileInfo();
        srchNr = 0;
        nextFreeFindFirst = 0;
        SetDirSort(TDirSort.DIRALPHABETICAL);
        updatelabel = true;
    }

    public DOS_Drive_Cache(String path) {
        dirBase = new CFileInfo();
        srchNr = 0;
        nextFreeFindFirst = 0;
        SetDirSort(TDirSort.DIRALPHABETICAL);
        SetBaseDir(path);
        updatelabel = true;
    }

    public static final class TDirSort {

        public static final int NOSORT = 0;

        public static final int ALPHABETICAL = 1;

        public static final int DIRALPHABETICAL = 2;

        public static final int ALPHABETICALREV = 3;

        public static final int DIRALPHABETICALREV = 4;
    }

    public void SetBaseDir(String baseDir) {
        IntRef id = new IntRef(0);
        basePath = baseDir;
        if (OpenDir(baseDir, id)) {
            StringRef result = new StringRef();
            ReadDir(id.value, result);
        }
        SetLabel(Cross.getVolumeLabel(basePath), Cross.isCDRom(basePath), true);
    }

    void SetDirSort(int sort) {
        sortDirType = sort;
    }

    boolean OpenDir(String path, IntRef id) {
        StringRef expand = new StringRef();
        CFileInfo dir = FindDirInfo(path, expand);
        if (OpenDir(dir, expand.value, id)) {
            dirSearch[id.value].nextEntry = 0;
            return true;
        }
        return false;
    }

    boolean ReadDir(int id, StringRef result) {
        if (id > MAX_OPENDIRS) return false;
        if (!IsCachedIn(dirSearch[id])) {
            Cross.dir_information dirp = Cross.open_directory(dirPath);
            if (dirp == null) {
                if (dirSearch[id] != null) {
                    dirSearch[id].id = MAX_OPENDIRS;
                    dirSearch[id] = null;
                }
                return false;
            }
            StringRef dir_name = new StringRef();
            BooleanRef is_directory = new BooleanRef();
            if (Cross.read_directory_first(dirp, dir_name, is_directory)) {
                CreateEntry(dirSearch[id], dir_name.value, is_directory.value);
                while (Cross.read_directory_next(dirp, dir_name, is_directory)) {
                    CreateEntry(dirSearch[id], dir_name.value, is_directory.value);
                }
            }
            Cross.close_directory(dirp);
        }
        if (SetResult(dirSearch[id], result, dirSearch[id].nextEntry)) return true;
        if (dirSearch[id] != null) {
            dirSearch[id].id = MAX_OPENDIRS;
            dirSearch[id] = null;
        }
        return false;
    }

    public void ExpandName(StringRef path) {
        path.value = GetExpandName(path.value);
    }

    public String GetExpandName(String path) {
        if (!File.separator.equals("\\")) path = StringHelper.replace(path, "\\", File.separator);
        StringRef work = new StringRef();
        String dir = path;
        int pos = path.lastIndexOf(File.separatorChar);
        if (pos >= 0) dir = dir.substring(0, pos + 1);
        CFileInfo dirInfo = FindDirInfo(dir, work);
        if (pos != 0) {
            StringRef d = new StringRef(path.substring(pos + 1));
            GetLongName(dirInfo, d);
            dir = d.value;
            work.value += dir;
        }
        if (work.value.endsWith(File.separator) && !(work.value.endsWith(":" + File.separator)) && work.value.length() > 1) {
            work.value = work.value.substring(0, work.value.length());
        }
        return work.value;
    }

    boolean GetShortName(String fullname, StringRef shortname) {
        StringRef expand = new StringRef();
        CFileInfo curDir = FindDirInfo(fullname, expand);
        int filelist_size = curDir.longNameList.size();
        if (filelist_size <= 0) return false;
        int low = 0;
        int high = filelist_size - 1;
        int mid, res;
        while (low <= high) {
            mid = (low + high) / 2;
            res = fullname.compareTo(((CFileInfo) curDir.longNameList.elementAt(mid)).orgname);
            if (res > 0) low = mid + 1; else if (res < 0) high = mid - 1; else {
                shortname.value = ((CFileInfo) curDir.longNameList.elementAt(mid)).shortname;
                return true;
            }
        }
        return false;
    }

    public boolean FindFirst(String path, IntRef id) {
        IntRef dirID = new IntRef(0);
        if (!OpenDir(path, dirID)) return false;
        int local_findcounter = 0;
        while (local_findcounter < MAX_OPENDIRS) {
            if (dirFindFirst[nextFreeFindFirst] == null) break;
            if (++nextFreeFindFirst >= MAX_OPENDIRS) nextFreeFindFirst = 0;
            local_findcounter++;
        }
        int dirFindFirstID = nextFreeFindFirst++;
        if (this.nextFreeFindFirst >= MAX_OPENDIRS) this.nextFreeFindFirst = 0;
        if (local_findcounter == MAX_OPENDIRS) {
            Log.log(LogTypes.LOG_DOSMISC, LogSeverities.LOG_ERROR, "DIRCACHE: FindFirst/Next: All slots full. Resetting");
            dirFindFirstID = 0;
            this.nextFreeFindFirst = 1;
            for (int n = 0; n < MAX_OPENDIRS; n++) {
                DeleteFileInfo(dirFindFirst[n]);
                dirFindFirst[n] = null;
            }
        }
        dirFindFirst[dirFindFirstID] = new CFileInfo();
        dirFindFirst[dirFindFirstID].nextEntry = 0;
        for (int i = 0; i < dirSearch[dirID.value].fileList.size(); i++) {
            CopyEntry((CFileInfo) dirFindFirst[dirFindFirstID], (CFileInfo) dirSearch[dirID.value].fileList.elementAt(i));
        }
        switch(sortDirType) {
            case TDirSort.ALPHABETICAL:
                break;
            case TDirSort.DIRALPHABETICAL:
                java.util.Collections.sort(dirFindFirst[dirFindFirstID].fileList, SortByDirName);
                break;
            case TDirSort.ALPHABETICALREV:
                java.util.Collections.sort(dirFindFirst[dirFindFirstID].fileList, SortByNameRev);
                break;
            case TDirSort.DIRALPHABETICALREV:
                java.util.Collections.sort(dirFindFirst[dirFindFirstID].fileList, SortByDirNameRev);
                break;
            case TDirSort.NOSORT:
                break;
        }
        id.value = dirFindFirstID;
        return true;
    }

    public boolean FindNext(int id, StringRef result) {
        if ((id >= MAX_OPENDIRS) || dirFindFirst[id] == null) {
            if (Log.level <= LogSeverities.LOG_ERROR) Log.log(LogTypes.LOG_DOSMISC, LogSeverities.LOG_ERROR, "DIRCACHE: FindFirst/Next failure : ID out of range: " + Integer.toString(id, 16));
            return false;
        }
        if (!SetResult(dirFindFirst[id], result, dirFindFirst[id].nextEntry)) {
            DeleteFileInfo(dirFindFirst[id]);
            dirFindFirst[id] = null;
            return false;
        }
        return true;
    }

    void ClearFileInfo(CFileInfo dir) {
        for (int i = 0; i < dir.fileList.size(); i++) {
            CFileInfo info = (CFileInfo) dir.fileList.elementAt(i);
            if (info != null) ClearFileInfo(info);
        }
        if (dir.id != MAX_OPENDIRS) {
            dirSearch[dir.id] = null;
            dir.id = MAX_OPENDIRS;
        }
    }

    void DeleteFileInfo(CFileInfo dir) {
        if (dir != null) ClearFileInfo(dir);
    }

    public void CacheOut(String path) {
        CacheOut(path, false);
    }

    public void CacheOut(String path, boolean ignoreLastDir) {
        StringRef expand = new StringRef();
        CFileInfo dir;
        if (ignoreLastDir) {
            String tmp;
            int pos = path.indexOf(File.separatorChar);
            if (pos > 0) {
                tmp = path.substring(pos);
            } else {
                tmp = path;
            }
            dir = FindDirInfo(tmp, expand);
        } else {
            dir = FindDirInfo(path, expand);
        }
        for (int i = 0; i < dir.fileList.size(); i++) {
            if (dirSearch[srchNr] == dir.fileList.elementAt(i)) dirSearch[srchNr] = null;
            DeleteFileInfo((CFileInfo) dir.fileList.elementAt(i));
            dir.fileList.setElementAt(null, i);
        }
        dir.fileList.clear();
        dir.longNameList.clear();
        save_dir = null;
    }

    public void AddEntry(String path, boolean checkExists) {
        StringRef file = new StringRef();
        StringRef expand = new StringRef();
        CFileInfo dir = FindDirInfo(path, expand);
        int pos = path.lastIndexOf(File.separatorChar);
        if (pos >= 0) {
            file.value = path.substring(pos + 1);
            if (checkExists) {
                if (GetLongName(dir, file) >= 0) return;
            }
            CreateEntry(dir, file.value, false);
            int index = GetLongName(dir, file);
            if (index >= 0) {
                int i;
                if (dir != null) for (i = 0; i < MAX_OPENDIRS; i++) {
                    if ((dirSearch[i] == dir) && (index <= dirSearch[i].nextEntry)) dirSearch[i].nextEntry++;
                }
            }
        } else {
        }
    }

    public void DeleteEntry(String path) {
        DeleteEntry(path, false);
    }

    public void DeleteEntry(String path, boolean ignoreLastDir) {
        CacheOut(path, ignoreLastDir);
        if (dirSearch[srchNr] != null && (dirSearch[srchNr].nextEntry > 0)) dirSearch[srchNr].nextEntry--;
        if (!ignoreLastDir) {
            int i;
            StringRef expand = new StringRef();
            CFileInfo dir = FindDirInfo(path, expand);
            if (dir != null) for (i = 0; i < MAX_OPENDIRS; i++) {
                if ((dirSearch[i] == dir) && (dirSearch[i].nextEntry > 0)) dirSearch[i].nextEntry--;
            }
        }
    }

    public void EmptyCache() {
        Clear();
        dirBase = new CFileInfo();
        save_dir = null;
        srchNr = 0;
        SetBaseDir(basePath);
    }

    public void SetLabel(String vname, boolean cdrom, boolean allowupdate) {
        if (!updatelabel) return;
        updatelabel = allowupdate;
        StringRef l = new StringRef(label);
        Drives.Set_Label(vname, l, cdrom);
        label = l.value;
        if (label == null) label = "";
        if (Log.level <= LogSeverities.LOG_NORMAL) Log.log(LogTypes.LOG_DOSMISC, LogSeverities.LOG_NORMAL, "DIRCACHE: Set volume label to " + label);
    }

    public String GetLabel() {
        return label;
    }

    private static class CFileInfo {

        public CFileInfo() {
            nextEntry = shortNr = 0;
            isDir = false;
            id = MAX_OPENDIRS;
        }

        String orgname;

        String shortname;

        boolean isDir;

        int id;

        int nextEntry;

        int shortNr;

        Vector fileList = new Vector();

        Vector longNameList = new Vector();
    }

    private boolean RemoveTrailingDot(StringRef shortname) {
        int len = shortname.value.length();
        if (len > 0 && (shortname.value.charAt(len - 1) == '.')) {
            if (len == 1) return false;
            if ((len == 2) && (shortname.value.charAt(0) == '.')) return false;
            shortname.value = shortname.value.substring(0, len - 1);
            return true;
        }
        return false;
    }

    private int GetLongName(CFileInfo curDir, StringRef shortName) {
        int filelist_size = curDir.fileList.size();
        if (filelist_size <= 0) return -1;
        RemoveTrailingDot(shortName);
        int low = 0;
        int high = filelist_size - 1;
        int mid, res;
        while (low <= high) {
            mid = (low + high) / 2;
            res = shortName.value.compareTo(((CFileInfo) curDir.fileList.elementAt(mid)).shortname);
            if (res > 0) low = mid + 1; else if (res < 0) high = mid - 1; else {
                shortName.value = ((CFileInfo) curDir.fileList.elementAt(mid)).orgname;
                return mid;
            }
        }
        return -1;
    }

    private void CreateShortName(CFileInfo curDir, CFileInfo info) {
        int len;
        boolean createShort;
        String tmpName;
        tmpName = info.orgname.toUpperCase();
        tmpName = StringHelper.replace(tmpName, " ", "");
        createShort = tmpName.length() != info.orgname.length();
        int pos = tmpName.indexOf('.');
        if (pos >= 0) {
            if (tmpName.length() - pos > 4) {
                while (tmpName.startsWith(".")) tmpName = tmpName.substring(1);
                createShort = true;
            }
            pos = tmpName.indexOf('.');
            if (pos >= 0) len = pos - 1; else len = tmpName.length();
        } else {
            len = tmpName.length();
        }
        createShort = createShort || (len > 8);
        if (!createShort) {
            StringRef buffer = new StringRef(tmpName);
            createShort = (GetLongName(curDir, buffer) >= 0);
        }
        if (createShort) {
            info.shortNr = CreateShortNameID(curDir, tmpName);
            String buffer = String.valueOf(info.shortNr);
            int tocopy;
            int buflen = buffer.length();
            if (len + buflen + 1 > 8) tocopy = 8 - buflen - 1; else tocopy = len;
            info.shortname = tmpName.substring(0, tocopy);
            info.shortname += "~";
            info.shortname += buffer;
            if (pos >= 0) {
                info.shortname += tmpName.substring(tmpName.lastIndexOf('.'));
            }
            if (curDir.longNameList.size() > 0) {
                if (info.shortname.compareTo(((CFileInfo) curDir.longNameList.lastElement()).shortname) >= 0) {
                    curDir.longNameList.add(info);
                } else {
                    boolean found = false;
                    int i;
                    for (i = 0; i < curDir.longNameList.size(); i++) {
                        CFileInfo it = (CFileInfo) curDir.longNameList.elementAt(i);
                        if (info.shortname.compareTo(it.shortname) < 0) {
                            found = true;
                            break;
                        }
                    }
                    if (found) curDir.longNameList.insertElementAt(info, i); else curDir.longNameList.add(info);
                }
            } else {
                curDir.longNameList.add(info);
            }
        } else {
            info.shortname = tmpName;
        }
        StringRef sn = new StringRef(info.shortname);
        RemoveTrailingDot(sn);
        info.shortname = sn.value;
    }

    private int CreateShortNameID(CFileInfo curDir, String name) {
        int filelist_size = curDir.longNameList.size();
        if (filelist_size <= 0) return 1;
        int foundNr = 0;
        int low = 0;
        int high = filelist_size - 1;
        int mid, res;
        while (low <= high) {
            mid = (low + high) / 2;
            res = CompareShortname(name, ((CFileInfo) curDir.longNameList.elementAt(mid)).shortname);
            if (res > 0) low = mid + 1; else if (res < 0) high = mid - 1; else {
                do {
                    foundNr = ((CFileInfo) curDir.longNameList.elementAt(mid)).shortNr;
                    mid++;
                } while (mid < curDir.longNameList.size() && (CompareShortname(name, ((CFileInfo) curDir.longNameList.elementAt(mid)).shortname) == 0));
                break;
            }
        }
        return foundNr + 1;
    }

    private int CompareShortname(String compareName, String shortName) {
        int pos = shortName.indexOf('~');
        if (pos >= 0) {
            String cpos = shortName.substring(pos);
            int compareCount1 = shortName.indexOf("~");
            int numberSize = cpos.indexOf(".");
            int compareCount2 = compareName.indexOf(".");
            if (compareCount2 > 8) compareCount2 = 8;
            if (compareCount2 > compareCount1 + numberSize) compareCount1 = compareCount2 - numberSize;
            return compareName.substring(0, compareCount1).compareToIgnoreCase(shortName.substring(0, Math.min(compareCount1, shortName.length())));
        }
        return compareName.compareTo(shortName);
    }

    private boolean SetResult(CFileInfo dir, StringRef result, int entryNr) {
        if (entryNr >= dir.fileList.size()) return false;
        CFileInfo info = (CFileInfo) dir.fileList.elementAt(entryNr);
        result.value = info.shortname;
        dir.nextEntry = entryNr + 1;
        return true;
    }

    private boolean IsCachedIn(CFileInfo curDir) {
        return (curDir.fileList.size() > 0);
    }

    private CFileInfo FindDirInfo(String path, StringRef expandedPath) {
        StringRef dir = new StringRef();
        String work;
        String start;
        int pos;
        CFileInfo curDir = dirBase;
        IntRef id = new IntRef(0);
        if (save_dir != null && path.equals(save_path)) {
            expandedPath.value = save_expanded;
            return save_dir;
        }
        if (basePath.length() >= path.length()) start = ""; else start = path.substring(basePath.length());
        expandedPath.value = basePath;
        if (!IsCachedIn(curDir)) {
            work = basePath;
            if (OpenDir(curDir, work, id)) {
                String buffer;
                StringRef result = new StringRef();
                buffer = dirPath;
                ReadDir(id.value, result);
                dirPath = buffer;
                if (dirSearch[id.value] != null) {
                    dirSearch[id.value].id = MAX_OPENDIRS;
                    dirSearch[id.value] = null;
                }
            }
        }
        do {
            pos = start.indexOf(File.separatorChar);
            if (pos >= 0) {
                dir.value = start.substring(0, pos);
            } else {
                dir.value = start;
            }
            int nextDir = GetLongName(curDir, dir);
            expandedPath.value += dir.value;
            if ((nextDir >= 0) && ((CFileInfo) curDir.fileList.elementAt(nextDir)).isDir) {
                curDir = (CFileInfo) curDir.fileList.elementAt(nextDir);
                curDir.orgname = dir.value;
                if (!IsCachedIn(curDir)) {
                    if (OpenDir(curDir, expandedPath.value, id)) {
                        String buffer = dirPath;
                        StringRef result = new StringRef();
                        ReadDir(id.value, result);
                        dirPath = buffer;
                        if (dirSearch[id.value] != null) {
                            dirSearch[id.value].id = MAX_OPENDIRS;
                            dirSearch[id.value] = null;
                        }
                    }
                }
            }
            if (pos >= 0) {
                expandedPath.value += File.separator;
                start = start.substring(pos + 1);
            }
        } while (pos >= 0);
        save_path = path;
        save_expanded = expandedPath.value;
        save_dir = curDir;
        return curDir;
    }

    private boolean OpenDir(CFileInfo dir, String expand, IntRef id) {
        id.value = GetFreeID(dir);
        dirSearch[id.value] = dir;
        String expandcopy = expand;
        if (expandcopy.endsWith(File.separator)) expandcopy += File.separator;
        if (dirSearch[id.value] != null) {
            Cross.dir_information dirp = Cross.open_directory(expandcopy);
            if (dirp != null) {
                Cross.close_directory(dirp);
                dirPath = expandcopy;
                return true;
            }
        }
        if (dirSearch[id.value] != null) {
            dirSearch[id.value].id = MAX_OPENDIRS;
            dirSearch[id.value] = null;
        }
        return false;
    }

    private void CreateEntry(CFileInfo dir, String name, boolean is_directory) {
        CFileInfo info = new CFileInfo();
        info.orgname = name;
        info.shortNr = 0;
        info.isDir = is_directory;
        CreateShortName(dir, info);
        boolean found = false;
        if (dir.fileList.size() > 0) {
            if (!(info.shortname.compareTo(((CFileInfo) dir.fileList.lastElement()).shortname) < 0)) {
                dir.fileList.add(info);
            } else {
                int it;
                for (it = 0; it < dir.fileList.size(); ++it) {
                    if (info.shortname.compareTo(((CFileInfo) dir.fileList.elementAt(it)).shortname) < 0) {
                        found = true;
                        break;
                    }
                }
                if (found) dir.fileList.insertElementAt(info, it); else dir.fileList.add(info);
            }
        } else {
            dir.fileList.add(info);
        }
    }

    void CopyEntry(CFileInfo dir, CFileInfo from) {
        CFileInfo info = new CFileInfo();
        info.orgname = from.orgname;
        info.shortname = from.shortname;
        info.shortNr = from.shortNr;
        info.isDir = from.isDir;
        dir.fileList.add(info);
    }

    int GetFreeID(CFileInfo dir) {
        if (dir.id != MAX_OPENDIRS) return dir.id;
        for (int i = 0; i < MAX_OPENDIRS; i++) {
            if (dirSearch[i] == null) {
                dir.id = i;
                return i;
            }
        }
        Log.log(LogTypes.LOG_FILES, LogSeverities.LOG_NORMAL, "DIRCACHE: Too many open directories!");
        dir.id = 0;
        return 0;
    }

    void Clear() {
        DeleteFileInfo(dirBase);
        dirBase = null;
        nextFreeFindFirst = 0;
        for (int i = 0; i < MAX_OPENDIRS; i++) dirSearch[i] = null;
    }

    private CFileInfo dirBase;

    private String dirPath;

    private String basePath;

    private int sortDirType;

    private CFileInfo save_dir;

    private String save_path;

    private String save_expanded;

    private int srchNr;

    private CFileInfo[] dirSearch = new CFileInfo[MAX_OPENDIRS];

    private CFileInfo[] dirFindFirst = new CFileInfo[MAX_OPENDIRS];

    private int nextFreeFindFirst;

    private String label = "";

    private boolean updatelabel;
}
