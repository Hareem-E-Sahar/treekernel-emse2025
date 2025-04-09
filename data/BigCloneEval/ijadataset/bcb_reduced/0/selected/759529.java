package neembuu.core.file;

import java.io.File;
import java.io.RandomAccessFile;
import neembuu.core.DType;
import neembuu.core.DType;

/**
 * Stores information about file download progress, and describes the state of the abstract file being downloaded.
 */
public final class PartiallyDownloadedFile {

    private int dataAreaLength = 4;

    private static int EMPTY_LIMIT = 20;

    private static int OPTIMUM_EXTRA = 10;

    private static int ADDED_ON_EXPANSION = 16;

    private static final int OFFSET_AT_ZERO_DEFAULT_VALUE = -3;

    /**
     * Stores size of file. The size of file can change.
     */
    private long fs;

    /**
     * Abstract file name
     */
    private String fn;

    /**
     * Abstract file path 
     */
    private String pth;

    /**
     * OffsetCollectionState
     * It's a array of OffsetCollection. Corresponding properties of that offsetcollection is stored in the other arrays at identical index.
     * The size of this array is double that that in other data arrays, because it has to store 2 values for each offsetcollection entry (i.e. start offset and ending offset).
     * A new empty OffsetCollection has by default 2 entires in each data array, and 4 entries in this array.
     * The first entry is offsetcollection (-2,-2), this is obviously an offset inexistent in file. Another similar entry is the offsetcollection(filesize+1,filesize+1)
     * DownloadManager of both these is null
     *
     * The entries are not -1,-1 becaues an entry such 0,x (x>0) will absorb -1,-1 results in an entry -1,x . Similarly is the case with filesize+1,filesize+1
     */
    private long[] off;

    /**
     * Array that stores name of downloadmanager resposible for downloading the given offsetcollection
     */
    private static DType[] dndlr;

    /**
     * Array that stores verified(boolean) of a offsetcollection
     */
    private boolean[] vr;

    /**
     * request frequency
     */
    private int[] rfr;

    public java.io.File sourceFile;

    /** 
     * Creates a new instance of PartiallyDownloadedFile
     * This method should not be used to create an instance, instace makeNewEmptyFile method should be used
     * @param filename Name of the file
     * @param abstractpath Abtract path of the file
     * @param offsetstate A long array describing the state of the file (the method of creating this array may change anytime)
     * @param DType Array specifying the DType responsible for downloading the corresponding entry in offsetstate[]
     * @param verifiedstate Array specifying whether the the corresponding entry in offsetstate[] has been verified or not
     * @param requestfrequency Array specifying the number of times data in the corresponding data region of the file has been requested
     */
    protected PartiallyDownloadedFile(String filename, String abstractpath, long filesize, long[] offsetstate, DType[] DType, boolean[] verifiedstate, int[] requestfrequency) {
        fn = filename;
        pth = abstractpath;
        off = offsetstate;
        dndlr = DType;
        vr = verifiedstate;
        rfr = requestfrequency;
        fs = filesize;
    }

    /**
     * Make a new instance of a PartiallyDownloadedFile, with given filename, abstarct path and of given size
     * @param filename Name of the file
     * @param abstractpath Abstract path of the file
     * @param size Size of the file
     */
    public static PartiallyDownloadedFile makeNewEmptyFile(String filename, String abstractpath, long size) {
        long[] off1 = new long[4 + OPTIMUM_EXTRA];
        boolean[] ver1 = new boolean[2 + OPTIMUM_EXTRA / 2];
        DType[] dndlr1 = new DType[2 + OPTIMUM_EXTRA / 2];
        int[] rfr1 = new int[2 + OPTIMUM_EXTRA / 2];
        off1[0] = OFFSET_AT_ZERO_DEFAULT_VALUE;
        off1[1] = OFFSET_AT_ZERO_DEFAULT_VALUE;
        ver1[0] = false;
        dndlr1[0] = null;
        rfr1[0] = -1;
        off1[2] = size + 2;
        off1[3] = size + 2;
        ver1[1] = false;
        dndlr1[1] = null;
        rfr1[1] = -1;
        PartiallyDownloadedFile retx = new PartiallyDownloadedFile(filename, abstractpath, size, off1, dndlr1, ver1, rfr1);
        return (retx);
    }

    private IndexAndDissolvabiltyWrapper indexOf(long x[], DType d, boolean ver) {
        IndexAndDissolvabiltyWrapper ret = new IndexAndDissolvabiltyWrapper();
        int mid, lb, ub;
        boolean[] done_x = { false, false };
        boolean mide, pullupperbound, pulllowerbound;
        System.out.println("                  inside indexOf");
        boolean cs1, cs2;
        for (int i = 0; i < 2; i++) {
            lb = 0;
            ub = dataAreaLength;
            do {
                mid = (lb + ub) / 2;
                mide = mid % 2 == 0;
                System.out.println();
                System.out.println("               mid=" + mid + " " + off[mid] + " " + off[mid + 1] + " i=" + i + "done i=" + done_x[i]);
                cs1 = cs2 = pulllowerbound = pullupperbound = false;
                if (i == 0) {
                    if (mide) {
                        if (off[mid] + 1 <= x[0]) cs1 = true;
                        if (x[0] <= off[mid + 1] + 1) cs2 = true;
                        if (cs1 && cs2) {
                            ret.x1 = mid;
                            done_x[0] = true;
                        } else if (!cs1 && cs2) {
                            pulllowerbound = false;
                            pullupperbound = true;
                        } else if (cs1 && !cs2) {
                            pulllowerbound = true;
                            pullupperbound = false;
                        }
                    } else {
                        if (off[mid] + 2 <= x[0]) cs1 = true;
                        if (x[0] <= off[mid + 1]) cs2 = true;
                        if (cs1 && cs2) {
                            ret.x1 = mid;
                            done_x[0] = true;
                        } else if (!cs1 && cs2) {
                            pulllowerbound = false;
                            pullupperbound = true;
                        } else if (cs1 && !cs2) {
                            pulllowerbound = true;
                            pullupperbound = false;
                        }
                    }
                } else {
                    if (mide) {
                        if (off[mid] - 1 <= x[1]) cs1 = true;
                        if (x[1] <= off[mid + 1] - 1) cs2 = true;
                        if (cs1 && cs2) {
                            ret.x2 = mid;
                            done_x[1] = true;
                        } else if (!cs1 && cs2) {
                            pulllowerbound = false;
                            pullupperbound = true;
                        } else if (cs1 && !cs2) {
                            pulllowerbound = true;
                            pullupperbound = false;
                        }
                    } else {
                        if (off[mid] <= x[1]) cs1 = true;
                        if (x[1] <= off[mid + 1] - 2) cs2 = true;
                        if (cs1 && cs2) {
                            ret.x2 = mid;
                            done_x[1] = true;
                        } else if (!cs1 && cs2) {
                            pulllowerbound = false;
                            pullupperbound = true;
                        } else if (cs1 && !cs2) {
                            pulllowerbound = true;
                            pullupperbound = false;
                        }
                    }
                }
                System.out.println("              " + cs1 + " " + cs2 + " " + pullupperbound + " " + pulllowerbound);
                if (pullupperbound) ub = mid - 1;
                if (pulllowerbound) lb = mid + 1;
            } while (lb <= ub && !done_x[i]);
        }
        System.out.println("                done indexOf " + x[0] + " " + x[1]);
        System.out.println("ret=" + ret.x1 + " " + ret.x2);
        ret.dx1 = isSame(ret.x1, d, ver);
        ret.dx2 = isSame(ret.x2, d, ver);
        return (ret);
    }

    public int absoluteIndexOf(long x) {
        int mid, lb = 0, ub = dataAreaLength;
        do {
            mid = (lb + ub) / 2;
            if (x >= off[mid] && x <= off[mid + 1]) {
                if (mid % 2 == 1) return mid + 1;
                return (mid);
            } else if (off[mid + 1] > x) ub = mid - 1; else lb = mid + 1;
        } while (lb <= ub);
        return (0);
    }

    private boolean isSame(int x, DType d, boolean ver) {
        if (x % 2 == 1) return false;
        return (dndlr[x / 2] == d);
    }

    private void twoNewElements(int i) {
        insertXnewElements(i, 2);
    }

    /**
     * index i is wrt to off[]; so to get equivalent storage point on other arrays, we use i/2
     * same is case with all other operations on data arrays
     */
    private void newElement(int i) {
        insertXnewElements(i, 1);
    }

    private void insertXnewElements(int i, int x) {
        if (dataAreaLength + 2 * x < off.length) {
            increaseLength();
        }
        dataAreaLength += 2 * x;
        System.arraycopy(off, i + 2, off, i + 2 + 2 * x, dataAreaLength - (2 * x));
        System.arraycopy(vr, i / 2 + 1, vr, i / 2 + 1 + x, dataAreaLength / 2 - x);
        System.arraycopy(dndlr, i / 2 + 1, dndlr, i / 2 + 1 + x, dataAreaLength / 2 - x);
    }

    private synchronized void increaseLength() {
        adjustArray();
    }

    /**
     * Remove elements from s to e (these indices are wrt off[]) inclusive of s and e
     */
    private int removeElements(int s, int e) {
        if (e < s) return (0);
        if (s % 2 == 1 || e % 2 == 1) throw new ArrayIndexOutOfBoundsException("Starting and ending index should be always even.");
        int r = getRequestFrequencySumOver(s, e);
        int index = -1;
        int number_of_elements_deleted = e - s + 2;
        System.arraycopy(off, e + 2, off, s, e - s);
        System.arraycopy(vr, e / 2 + 1, vr, s / 2, e / 2 - s / 2);
        System.arraycopy(dndlr, e / 2 + 1, dndlr, s / 2, e / 2 - s / 2);
        dataAreaLength -= number_of_elements_deleted;
        checkAndAdjustArray();
        return r;
    }

    /**
     * If the empty areas in the array exceed empty limit (EMPTY_LIMIT), then extra is reduced to optimum extras (OPTIMUM_EXTRA)
     */
    private void checkAndAdjustArray() {
        if (off.length - dataAreaLength > EMPTY_LIMIT) {
            adjustArray();
        }
    }

    private synchronized void adjustArray() {
        long[] off1 = new long[dataAreaLength + ADDED_ON_EXPANSION];
        boolean[] vr1 = new boolean[(dataAreaLength + ADDED_ON_EXPANSION) / 2];
        DType[] dndlr1 = new DType[(dataAreaLength + ADDED_ON_EXPANSION) / 2];
        int[] rfr1 = new int[(dataAreaLength + ADDED_ON_EXPANSION) / 2];
        int smaller = 0;
        if (dataAreaLength + ADDED_ON_EXPANSION < off.length) smaller = dataAreaLength + ADDED_ON_EXPANSION; else smaller = off.length;
        smaller = smaller / 2;
        for (int j = 0; j < smaller; j++) {
            off1[2 * j] = off[2 * j];
            off1[2 * j + 1] = off[2 * j + 1];
            vr1[j] = vr[j];
            dndlr1[j] = dndlr[j];
            rfr1[j] = rfr[j];
        }
        off = off1;
        vr = vr1;
        dndlr = dndlr1;
        rfr = rfr1;
    }

    private int getRequestFrequencySumOver(int s, int e) {
        int r = 0;
        for (int todel = s; todel <= e; todel += 2) r += rfr[todel / 2];
        return r;
    }

    public void printState() {
        for (int j = 0; j < dndlr.length; j++) {
            System.out.print(off[2 * j]);
            System.out.print(' ');
            System.out.print(off[2 * j + 1]);
            System.out.print(' ');
            System.out.print(dndlr[j]);
            System.out.print(' ');
            System.out.print(vr[j]);
            System.out.print(' ');
            System.out.println(rfr[j]);
        }
    }

    public void printStateOfDataRegionOnly() {
        for (int j = 0; j < dataAreaLength / 2; j++) {
            System.out.print(off[2 * j]);
            System.out.print(' ');
            System.out.print(off[2 * j + 1]);
            System.out.print(' ');
            System.out.print(dndlr[j]);
            System.out.print(' ');
            System.out.print(vr[j]);
            System.out.print(' ');
            System.out.println(rfr[j]);
        }
    }

    /**
     * Add a entry which decribes the current state of the download. 
     * State is decribed by, how much has been downloaded, who is downloading what, and other similar properties.
     * Suppose a DownloadManager downloaded the data of a file from offset 's' to offset 'e' (both inclusive)
     * Then these offsets are passed to this function with other parameters to make an update in the array which tells which regions of the file are in which state.
     * All DownloadManager s must keep updating the state of the file by invoking this method.
     * For better performance, unless specified the DownloadManager should always make a decently big download and then update.
     * @param s Starting offset (inclusive)
     * @param e Ending offset (inclusive)
     * @param d The DType resposible/assigned for downloading from starting offset(long s) to ending offset (long e).
     * @param ver Has the validity of the downloaded region been tested or not
     */
    public synchronized void addStateComponent(long s, long e, DType d, boolean ver) {
        IndexAndDissolvabiltyWrapper ind = indexOf(new long[] { s, e }, d, ver);
        int x1 = ind.x1, x2 = ind.x2;
        boolean x1e = x1 % 2 == 0, x2e = x2 % 2 == 0;
        boolean eq = x1 == x2;
        boolean dx1 = ind.dx1, dx2 = ind.dx2;
        int rf = 0;
        System.out.println("dataAreaLength=" + dataAreaLength);
        System.out.println("Adding entry");
        System.out.print("x1=" + x1);
        System.out.print(" x2=" + x2);
        System.out.print(" x1e=" + x1e);
        System.out.print(" x2e=" + x2e);
        System.out.print(" eq=" + eq);
        System.out.print(" dx1=" + dx1);
        System.out.println(" dx2=" + dx2);
        if (eq) {
            if (x1e) {
                if (dx1) {
                    return;
                } else {
                    twoNewElements(x1);
                    modifyEntry(x1 + 4, e + 1, off[x1 + 1], dndlr[x1 / 2], vr[x1 / 2], rfr[x1 / 2] / 2);
                    modifyEntry(x1, off[x1], s - 1, dndlr[x1 / 2], vr[x1 / 2], (int) (rfr[x1 / 2] / 2 + 0.5));
                    modifyEntry(x1 + 2, s, e, d, ver, 0);
                    return;
                }
            } else {
                newElement(x1 - 1);
                modifyEntry(x1 + 1, s, e, d, ver, 0);
                return;
            }
        }
        if (!x1e && !x2e) {
            rf = removeElements(x1 + 3, x2 - 1);
            modifyEntry(x1 + 1, s, e, d, ver, rf);
            return;
        }
        if (x1e && x2e && !dx1 && !dx2) {
            modifyEntry(x1, off[x1], s - 1, dndlr[x1 / 2], vr[x1 / 2], (int) (rfr[x1 / 2] / 2 + 0.5));
            modifyEntry(x2, e + 1, off[x2 + 1], dndlr[x2 / 2], vr[x2 / 2], (int) (rfr[x2 / 2] / 2));
            if (x2 - x1 == 2) {
                newElement(x1);
                modifyEntry(x1 + 2, s, e, d, ver, 0);
                return;
            }
            if (x2 - x1 == 4) {
                modifyEntry(x1 + 2, s, e, d, ver, 0);
                return;
            }
            rf = removeElements(x1 + 4, x2 - 2);
            modifyEntry(x1 + 2, s, e, d, ver, rf);
            return;
        }
        if (!x2e) {
            if (dx1) {
                if (x2 - x1 == 1) {
                    modifyEntry(x1, off[x1], e, d, ver, rfr[x1 / 2]);
                    return;
                }
                rf = removeElements(x1 + 2, x2 - 1);
                if (rf < 0) rf = 0;
                modifyEntry(x1, off[x1], e, d, ver, rfr[x1 / 2] + rf);
                return;
            }
            modifyEntry(x1, off[x1], s - 1, dndlr[x1 / 2], vr[x1 / 2], rfr[x1 / 2]);
            if (x2 - x1 == 1) {
                newElement(x1);
                modifyEntry(x1 + 2, s, e, d, ver, 0);
                return;
            }
            rf = removeElements(x1 + 4, x2 - 1);
            modifyEntry(x1 + 2, s, e, d, ver, rf);
            return;
        }
        if (!x1e) {
            if (dx2) {
                if (x2 - x1 == 1) {
                    modifyEntry(x1 + 1, s, off[x1 + 1], d, ver, rfr[x1 / 2]);
                    return;
                }
                int tmp = rfr[x2 / 2];
                rf = removeElements(x1 + 1, x2 - 2);
                if (rf < 0) rf = 0;
                modifyEntry(x1 + 1, s, off[x1 + 1], d, ver, tmp + rf);
                return;
            }
            modifyEntry(x2, e + 1, off[x2 + 1], dndlr[x2 / 2], vr[x2 / 2], rfr[x2 / 2] + rf);
            if (x2 - x1 == 1) {
                newElement(x1 - 1);
                modifyEntry(x1 + 1, s, e, d, ver, 0);
                return;
            }
            rf = removeElements(x1 + 1, x2 - 2);
            modifyEntry(x1 + 1, s, e, d, ver, rf);
            return;
        }
        if (x1e && x2e) {
            if (dx1) {
                if (dx2) {
                    rf = getRequestFrequencySumOver(x1, x2);
                    modifyEntry(x1, off[x1], off[x2 + 1], d, ver, rf);
                    removeElements(x1 + 2, x2);
                    return;
                }
                rf = getRequestFrequencySumOver(x1, x2 - 2);
                modifyEntry(x1, off[x1], e, d, ver, rf);
                modifyEntry(x2, e + 1, off[x2 + 1], dndlr[x2 / 2], vr[x2 / 2], rfr[x2 / 2]);
                removeElements(x1 + 2, x2 - 2);
                return;
            }
            if (dx2) {
                rf = getRequestFrequencySumOver(x1 + 2, x2);
                modifyEntry(x1, off[x1], s - 1, dndlr[x1 / 2], vr[x1 / 2], rfr[x1 / 2]);
                modifyEntry(x2, s, off[x2 + 1], d, ver, rf);
                removeElements(x1 + 2, x2 - 2);
                return;
            }
        }
    }

    /**
     * Modify entry at i th index (wrt off[]) and place following values
     */
    private void modifyEntry(int i, long s, long e, DType d, boolean ver, int rf) {
        off[i] = s;
        off[i + 1] = e;
        dndlr[i / 2] = d;
        vr[i / 2] = ver;
        rfr[i / 2] = rf;
    }

    /**
     * Append request requency of the offset region where the given offset lies by byValue
     */
    public void appendRequestFrequency(long offset, int byValue) {
        int index = absoluteIndexOf(offset);
        if (index % 2 == 1) throw new NullPointerException("Given offset entry " + offset + " doesn't exist in our collection. First add such an entry.\n" + index);
        rfr[index / 2] += byValue;
        System.out.println("appended value of frequenct at " + index + " to" + rfr[index / 2]);
    }

    /**
     * Get abstract file name
     */
    public String getName() {
        return (fn);
    }

    public long bytesDownloaded() {
        long cs = 0;
        for (int j = 2; j < dataAreaLength - 2; j += 2) {
            cs += off[j + 1] - off[j] + 1;
        }
        return cs;
    }

    /**
     * Get abstract file path. 
     * Example : 
     * let pathseparator be '/'
     * perhaps we can return a URI here
     */
    public String getAbstractPath() {
        return (pth);
    }

    /**
     * Get array of files that store this partially downloaded file
     */
    public java.io.File[] getPartialDataFilesArray() {
        return null;
    }

    /**
     * Returns antisipated size of this partially downloaded on completion.
     */
    public long getFileSize() {
        return fs;
    }

    /**
     * Get the PartiallyDownloadedFileGroup object associated with this PartiallDownloadedFileStream.
     */
    public PartiallyDownloadedFileGroup getGroupHead() {
        return (null);
    }

    /**
     * Get array of download managers actively downloading this file. References to inactive download managers may not be included in this array.
     */
    public neembuu.core.DownloadManager[] getActiveDownloadManagers() {
        return null;
    }

    /**
     * TODO:
     * implement this
     * first it fills , if present
     * then it fires a thread to download un-available region
     * returns the data
     * Stores requested region of the file, inside the short array d.
     * @param d The short array where the data is stored
     * @param start The starting file offset of the request
     * @param size The size of the request. The request spans from start ---to--> start + size - 1, if both boudns are taken inclusive
     */
    public int getData(short[] d, long start, int size) {
        byte[] tmp = new byte[size];
        if (sourceFile == null) {
            for (int j = 0; j < size; j++) d[j] = (short) ((start % 10) + 48);
            return size;
        }
        try {
            RandomAccessFile ramdomaccess_fakeFile = new RandomAccessFile(sourceFile, "r");
            ramdomaccess_fakeFile.seek(start);
            ramdomaccess_fakeFile.readFully(tmp);
            for (int j = 0; j < size; j++) d[j] = (short) (tmp[j] & 0xFF);
        } catch (Exception any) {
            any.printStackTrace();
        }
        return size;
    }

    public int getData(byte[] d, long start, int size) {
        byte[] tmp = new byte[size];
        if (sourceFile == null) {
            for (int j = 0; j < size; j++) d[j] = (byte) ((start % 10) + 48);
            return size;
        }
        try {
            RandomAccessFile ramdomaccess_fakeFile = new RandomAccessFile(sourceFile, "r");
            ramdomaccess_fakeFile.seek(start);
            ramdomaccess_fakeFile.readFully(d);
        } catch (Exception any) {
            any.printStackTrace();
        }
        return size;
    }

    public void watchAsYouDownloadStateChanged() {
    }

    public static void main(String[] args) throws Exception {
        PartiallyDownloadedFile pf = makeNewEmptyFile("Hello.txt", "\\", Integer.MAX_VALUE);
        java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
        pf.printStateOfDataRegionOnly();
        for (String x; ; ) {
            System.out.println("order=start end DType verified requestfr");
            x = r.readLine();
            long s = Long.parseLong(x.substring(0, x.indexOf(" ")));
            x = x.substring(x.indexOf(" ") + 1, x.length());
            long e = Long.parseLong(x.substring(0, x.indexOf(" ")));
            x = x.substring(x.indexOf(" ") + 1, x.length());
            DType da = null;
            if (x.substring(0, x.indexOf(" ")).trim().equals("0")) da = DType.TORRENT; else if (x.substring(0, x.indexOf(" ")).trim().equals("1")) da = DType.HTTP; else if (x.substring(0, x.indexOf(" ")).trim().equals("2")) da = DType.FILE;
            x = x.substring(x.indexOf(" ") + 1, x.length());
            boolean veri = false;
            if (x.substring(0, x.indexOf(" ")).equals("1")) veri = true;
            x = x.substring(x.indexOf(" ") + 1, x.length());
            int rfrf = 0;
            Integer.parseInt(x);
            pf.addStateComponent(s, e, da, veri);
            System.out.println("added");
            pf.appendRequestFrequency(s + 1, rfrf);
            pf.printStateOfDataRegionOnly();
        }
    }

    /**
     * Checks if a given file name is valid.
     * Depending on the file system, file names have different contraints on them.
     * Like in FAT8, file name can have maximum 8 chacter name, and 3 character extension.
     * In ext2/ext3/NTFS etc., space is allowed in file name, while in FAT8 it isnt't
     * @param name The file name that has to be checked for validity
     * @return true if the file name is valid, false otherwise
     */
    public static boolean isFileNameValid(String name) {
        if (name == null) {
            return false;
        }
        File f = new File(name);
        if (f.exists()) return true;
        try {
            boolean b = f.createNewFile();
            if (b) {
                f.delete();
            }
            return b;
        } catch (java.io.IOException ioe) {
            return false;
        }
    }

    private final class IndexAndDissolvabiltyWrapper {

        public int x1, x2;

        public boolean dx1, dx2;
    }
}
