package com.rbnb.api;

final class Archive extends com.rbnb.api.StorageManager {

    /**
     * cached <code>FrameSet</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 10/01/2001
     */
    private FrameSet cachedSet = null;

    /**
     * oldest <code>FileSet</code> still in the <code>Archive</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #newestFS
     * @since V2.0
     * @version 05/10/2001
     */
    private long newestFS = -1;

    /**
     * oldest <code>FileSet</code> still in the <code>Archive</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #newestFS
     * @since V2.0
     * @version 05/10/2001
     */
    private long oldestFS = -1;

    /**
     * is <code>Archive</code> known to be out-of-date?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.1
     * @version 04/24/2003
     */
    private boolean outOfDate = true;

    static final String SEPARATOR = System.getProperty("file.separator");

    static final boolean BINARYARCHIVE = true;

    static final boolean TEXTARCHIVE = !BINARYARCHIVE;

    static final String[] ARCHIVE_PARAMETERS = { "REG", "EXP" };

    static final int ARC_REG = 0;

    static final int ARC_EXP = 1;

    Archive(float flush, float trim) throws com.rbnb.api.SerializeException, java.io.EOFException, java.io.IOException, java.lang.InterruptedException {
        super(flush, trim);
    }

    final void deleteArchive() throws com.rbnb.api.SerializeException, java.io.IOException, java.lang.InterruptedException {
        try {
            getDoor().lock("Archive.deleteArchive");
            com.rbnb.compat.File aDir = new com.rbnb.compat.File(getArchiveDirectory());
            Directory asDirectory = new Directory(aDir);
            com.rbnb.compat.File[] files = asDirectory.listFiles(), nFiles = null;
            java.util.Vector levels = new java.util.Vector();
            levels.addElement(files);
            levels.addElement(new Integer(0));
            int filesIdx = 0, indexIdx = 1;
            while (levels.size() > 0) {
                files = (com.rbnb.compat.File[]) levels.elementAt(filesIdx);
                int idx = ((Integer) levels.elementAt(indexIdx)).intValue();
                for (; (files != null) && (idx < files.length); ++idx) {
                    Directory fAsDirectory = new Directory(files[idx]);
                    nFiles = fAsDirectory.listFiles();
                    if ((nFiles != null) && (nFiles.length != 0)) {
                        break;
                    }
                    files[idx].delete();
                    if (files[idx].exists()) {
                        throw new java.io.IOException("Failed to delete " + files[idx] + " from old archive.");
                    }
                }
                if ((files != null) && (idx < files.length)) {
                    levels.setElementAt(new Integer(idx), indexIdx);
                    levels.addElement(nFiles);
                    levels.addElement(new Integer(0));
                    filesIdx += 2;
                    indexIdx += 2;
                } else {
                    levels.removeElementAt(indexIdx);
                    levels.removeElementAt(filesIdx);
                    indexIdx -= 2;
                    filesIdx -= 2;
                }
            }
            aDir.delete();
            if (aDir.exists()) {
                throw new java.io.IOException("Failed to delete directory " + aDir + " from old archive.");
            }
        } finally {
            getDoor().unlock();
        }
    }

    final String getArchiveDirectory() {
        return (((RingBuffer) getParent()).getArchiveDirectory());
    }

    final FrameSet getCachedSet() {
        return (cachedSet);
    }

    public final long getLogClass() {
        return (Log.CLASS_ARCHIVE);
    }

    public final byte getLogLevel() {
        return (Log.STANDARD);
    }

    private final long getNewest() {
        return (newestFS);
    }

    private final long getOldest() {
        return (oldestFS);
    }

    final synchronized void markOutOfDate() {
        if (!outOfDate) {
            outOfDate = true;
            ((RingBuffer) getParent()).markOutOfDate();
            String directory = getArchiveDirectory() + SEPARATOR;
            com.rbnb.compat.File file = new com.rbnb.compat.File(directory + "summary.rbn");
            if (file.exists()) {
                file.delete();
            }
            file = new com.rbnb.compat.File(directory + "seal.rbn");
            if (file.exists()) {
                file.delete();
            }
        }
    }

    final byte moveDownFrom(RmapExtractor extractorI, ExtractedChain unsatisfiedI, java.util.Vector unsatisfiedO) throws com.rbnb.api.AddressException, com.rbnb.api.SerializeException, java.io.EOFException, java.io.InterruptedIOException, java.io.IOException, java.lang.InterruptedException {
        byte reasonR = MATCH_UNKNOWN;
        if ((System.getProperty("noreframe") == null) && (unsatisfiedI != null) && (unsatisfiedI.getInherited() != null) && (unsatisfiedI.getInherited().getFrange() != null)) {
            reasonR = MATCH_UNKNOWN;
        } else {
            reasonR = super.moveDownFrom(extractorI, unsatisfiedI, unsatisfiedO);
        }
        return (reasonR);
    }

    public final void nullify() {
        super.nullify();
        if (getCachedSet() != null) {
            getCachedSet().nullify();
            setCachedSet(null);
        }
    }

    final void readFromArchive() throws com.rbnb.api.AddressException, com.rbnb.api.SerializeException, java.io.EOFException, java.io.IOException, java.lang.InterruptedException {
        try {
            getDoor().setIdentification(getFullName() + "/" + getClass());
            getDoor().lock("Archive.readFromArchive");
            readOffsetsFromArchive();
            readSkeletonFromArchive();
            FileSet lastFS = null;
            for (long idx = getOldest(), endIdx = getNewest(); idx <= endIdx; ++idx) {
                com.rbnb.compat.File fileSetDirectory = new com.rbnb.compat.File(getArchiveDirectory() + Archive.SEPARATOR + "FS" + idx);
                if (fileSetDirectory.exists()) {
                    FileSet fs = new FileSet(idx);
                    addChild(fs);
                    fs.readFromArchive();
                    lastFS = fs;
                }
            }
            if (lastFS != null) {
                setNextIndex(lastFS.getIndex() + 1);
            }
            outOfDate = false;
        } finally {
            getDoor().unlock();
        }
    }

    final void readOffsetsFromArchive() throws com.rbnb.api.SerializeException, java.io.EOFException, java.io.IOException, java.lang.InterruptedException {
        String directory = getArchiveDirectory() + SEPARATOR;
    }

    final void readSkeletonFromArchive() throws com.rbnb.api.AddressException, com.rbnb.api.SerializeException, java.io.EOFException, java.io.IOException, java.lang.InterruptedException {
        try {
            getDoor().lock("Archive.readSkeletonFromArchive");
            String directory = getArchiveDirectory() + SEPARATOR;
        } finally {
            getDoor().unlock();
        }
    }

    final void recoverFromArchive(java.util.Vector validSealsI, java.util.Vector invalidSealsI, StringBuffer goodMessageO, StringBuffer notMessageO, StringBuffer unMessageO) throws java.lang.IllegalStateException {
        if ((validSealsI.size() == 0) && (invalidSealsI.size() == 0)) {
            throw new java.lang.IllegalStateException("No filesets were found in the archive.");
        }
        try {
            readOffsetsFromArchive();
        } catch (java.lang.Exception e) {
            ((RingBuffer) getParent()).getCache().setMs(0);
            ((RingBuffer) getParent()).getCache().setMeps(0);
            setMs(0);
            setMeps(0);
        }
        setOldest(-1);
        setNewest(-1);
        recoverFileSets(validSealsI, invalidSealsI, goodMessageO, notMessageO, unMessageO);
        setRegistered(new Registration());
        setLastRegistration(Long.MIN_VALUE);
        setAddedSets(0);
    }

    private final void recoverFileSets(java.util.Vector validSealsI, java.util.Vector invalidSealsI, StringBuffer goodMessageO, StringBuffer notMessageO, StringBuffer unMessageO) {
        boolean useValid = false, notrecovered = false, unrecoverable = false;
        int lmeps = 0, ceps = 0;
        com.rbnb.compat.File lastFile = null, validFile = null, invalidFile = null, unrecoverableDirectory = new com.rbnb.compat.File(getArchiveDirectory() + Archive.SEPARATOR + "UNRECOVERABLE"), notrecoveredDirectory = new com.rbnb.compat.File(getArchiveDirectory() + Archive.SEPARATOR + "NOTRECOVERED");
        long validFS = Long.MAX_VALUE, invalidFS = Long.MAX_VALUE, oldestFS = Long.MIN_VALUE, newestFS = Long.MIN_VALUE;
        Seal lastSeal = null, validSeal = null, invalidSeal = null;
        goodMessageO.append("\n\t\tFileSets recovered:");
        FileSet previousFS = null, lastFS = null;
        boolean forceDeep = false;
        for (int idx = 0, endIdx = validSealsI.size(), idx1 = 0, endIdx1 = invalidSealsI.size(); (idx < endIdx) || (idx1 < endIdx1) || validFile != null || invalidFile != null; ) {
            com.rbnb.compat.File fileSetDirectory;
            Seal theSeal;
            long fsIndex;
            if ((validFile == null) && (idx < endIdx)) {
                validFile = (com.rbnb.compat.File) validSealsI.elementAt(idx++);
                validSeal = (Seal) validSealsI.elementAt(idx++);
                String name = validFile.getName();
                validFS = Long.parseLong(name.substring(2));
            }
            if ((invalidFile == null) && (idx1 < endIdx1)) {
                invalidFile = (com.rbnb.compat.File) invalidSealsI.elementAt(idx1++);
                invalidSeal = ((com.rbnb.api.InvalidSealException) invalidSealsI.elementAt(idx1++)).getInvalid();
                String name = invalidFile.getName();
                invalidFS = Long.parseLong(name.substring(2));
            }
            if (validFS < invalidFS) {
                fileSetDirectory = validFile;
                theSeal = validSeal;
                fsIndex = validFS;
                validFile = null;
                validFS = Long.MAX_VALUE;
            } else {
                fileSetDirectory = invalidFile;
                theSeal = invalidSeal;
                fsIndex = invalidFS;
                invalidFile = null;
                invalidFS = Long.MAX_VALUE;
            }
            FileSet fs = null;
            boolean added = false;
            try {
                fs = new FileSet(fsIndex);
                addChild(fs);
                previousFS = lastFS;
                lastFS = fs;
                added = true;
                try {
                    if (forceDeep || theSeal == null || theSeal.equals(invalidSeal)) {
                        throw new Exception();
                    } else {
                        fs.accessFiles(true);
                        fs.readFromArchive();
                        fs.releaseFiles();
                    }
                } catch (java.lang.Exception e) {
                    forceDeep = true;
                    try {
                        String dir = fs.getArchiveDirectory() + Archive.SEPARATOR;
                        System.err.println("\nRecovering " + dir);
                        com.rbnb.compat.File foo = new com.rbnb.compat.File(dir + "regdat.rbn");
                        if (foo.exists()) foo.delete();
                        foo = new com.rbnb.compat.File(dir + "reghdr.rbn");
                        if (foo.exists()) foo.delete();
                        foo = new com.rbnb.compat.File(dir + "offsets.rbn");
                        if (foo.exists()) foo.delete();
                    } catch (Exception ee) {
                        ee.printStackTrace();
                    }
                    theSeal = fs.recoverFromDataFiles();
                }
                if (theSeal == null) {
                    Seal.seal(fs.getArchiveDirectory());
                    theSeal = Seal.validate(fs.getArchiveDirectory(), ((lastSeal == null) ? Long.MIN_VALUE : lastSeal.getAsOf()), Long.MAX_VALUE);
                    if (theSeal == null) {
                        throw new com.rbnb.api.InvalidSealException(theSeal, ((lastSeal == null) ? Long.MIN_VALUE : lastSeal.getAsOf()), Long.MAX_VALUE);
                    }
                }
                if (lastFile == null) {
                    lastFile = fileSetDirectory;
                    lastSeal = theSeal;
                    oldestFS = newestFS = fsIndex;
                    goodMessageO.append("\n\t\t\t").append(fileSetDirectory.getName());
                } else {
                    try {
                        theSeal.validate(lastSeal.getAsOf(), Long.MAX_VALUE);
                        goodMessageO.append("\n\t\t\t").append(fileSetDirectory.getName());
                        newestFS = fsIndex;
                    } catch (com.rbnb.api.InvalidSealException e) {
                        added = false;
                        removeChild(fs);
                        lastFS = previousFS;
                        try {
                            if (!notrecovered) {
                                notrecovered = true;
                                notMessageO.append("\n\t\tFilesets not recovered " + "(in NOTRECOVERED):");
                                notrecoveredDirectory.mkdirs();
                            }
                            com.rbnb.compat.File newName = new com.rbnb.compat.File(notrecoveredDirectory.getAbsolutePath() + Archive.SEPARATOR + fileSetDirectory.getName());
                            if (fileSetDirectory.renameTo(newName)) {
                                notMessageO.append("\n\t\t\t").append(fileSetDirectory.getName());
                            } else {
                                notMessageO.append("\n\t\t\t").append(fileSetDirectory.getName()).append(" rename failed.");
                            }
                        } catch (java.lang.Exception e1) {
                        }
                    }
                }
                if (added) {
                    lmeps = Math.max(lmeps, fs.getNchildren());
                    for (int idx2 = 0; idx2 < fs.getNchildren(); ++idx2) {
                        Rmap rm = fs.getChildAt(idx2);
                        if (rm instanceof FrameSet) {
                            FrameSet fsFS = (FrameSet) rm;
                            if ((fsFS.getSummary() != null) && (fsFS.getSummary().getFrange() != null)) {
                                ceps = Math.max(ceps, (int) fsFS.getSummary().getFrange().getDuration());
                            }
                        }
                    }
                }
                if (ceps == 0) {
                    ceps = 10;
                }
            } catch (java.lang.Exception e) {
                System.err.println("Archive recovery failed... ");
                try {
                    if (added) {
                        removeChild(fs);
                        lastFS = previousFS;
                    }
                    if (!unrecoverable) {
                        unrecoverable = true;
                        unMessageO.append("\n\t\tUnrecoverable filesets ").append("(in UNRECOVERABLE):");
                        unrecoverableDirectory.mkdirs();
                    }
                    com.rbnb.compat.File newName = new com.rbnb.compat.File(unrecoverableDirectory.getAbsolutePath() + Archive.SEPARATOR + fileSetDirectory.getName());
                    if (fileSetDirectory.renameTo(newName)) {
                        unMessageO.append("\n\t\t\t").append(fileSetDirectory.getName());
                    } else {
                        unMessageO.append("\n\t\t\t").append(fileSetDirectory.getName()).append(" rename failed.");
                    }
                } catch (java.lang.Exception e1) {
                }
            }
        }
        try {
            if (oldestFS != Long.MIN_VALUE) {
                setOldest(oldestFS);
                setNewest(newestFS);
            } else {
                throw new java.lang.IllegalStateException("No filesets could be recovered from archive.");
            }
            if (getMeps() < lmeps) {
                setMeps(lmeps);
            }
            if (getMs() < getNchildren() + 1) {
                setMs(getNchildren() + 1);
            }
            Cache cache = ((RingBuffer) getParent()).getCache();
            if (cache.getMeps() < ceps) {
                cache.setMeps(ceps);
            }
            if (cache.getMs() < lmeps) {
                cache.setMs(lmeps);
            }
            if (lastFS != null) {
                setNextIndex(lastFS.getIndex() + 1);
            }
        } catch (java.lang.IllegalStateException e) {
            throw e;
        } catch (java.lang.Exception e) {
            throw new java.lang.IllegalStateException("Archive recovery failed.\n\t" + e.getMessage());
        }
    }

    final void setCachedSet(FrameSet setI) {
        cachedSet = setI;
    }

    private final void setNewest(long newestFSI) {
        newestFS = newestFSI;
    }

    final void setOldest(long oldestFSI) {
        oldestFS = oldestFSI;
    }

    final void setSet(FrameManager setI) throws com.rbnb.api.AddressException, com.rbnb.api.SerializeException, java.io.EOFException, java.io.IOException, java.lang.InterruptedException {
        super.setSet(setI);
        if (setI != null) {
            setNewest(setI.getIndex());
            if (getOldest() == -1) {
                setOldest(setI.getIndex());
            }
        }
    }

    final boolean validate(long afterI, long beforeI, java.util.Vector validSealsO, java.util.Vector invalidSealsO) throws com.rbnb.api.InvalidSealException {
        long after = afterI, before = beforeI;
        boolean validR = validateFileSets(after, before, validSealsO, invalidSealsO);
        return (validR);
    }

    private final boolean validateFileSets(long afterI, long beforeI, java.util.Vector validSealsO, java.util.Vector invalidSealsO) {
        boolean validR = true;
        long after = afterI, before = beforeI;
        String directory = getArchiveDirectory();
        com.rbnb.compat.File archive = new com.rbnb.compat.File(directory);
        Directory asDirectory = new Directory(archive);
        com.rbnb.compat.File[] files;
        try {
            if (!archive.exists()) {
                throw new java.lang.IllegalStateException("Cannot find archive directory " + directory);
            } else if ((files = asDirectory.listFiles()) == null) {
                throw new java.lang.IllegalStateException("Archive location " + directory + " is not a directory.");
            }
        } catch (java.io.IOException e) {
            throw new java.lang.IllegalStateException("Archive in " + directory + " is in a bad state.\n" + e.getMessage());
        }
        boolean summary = false;
        java.util.Vector fileSets = new java.util.Vector();
        for (int idx = 0; idx < files.length; ++idx) {
            try {
                String name = files[idx].getName();
                Directory fAsDirectory = new Directory(files[idx]);
                if (name.equalsIgnoreCase("summary.rbn")) {
                    summary = true;
                } else if (name.substring(0, 2).equalsIgnoreCase("FS") && (fAsDirectory.listFiles() != null)) {
                    long fsIndex = 0;
                    try {
                        fsIndex = Long.parseLong(name.substring(2));
                    } catch (java.lang.NumberFormatException e) {
                        continue;
                    }
                    int lo, hi, idx1;
                    for (lo = 0, hi = fileSets.size() - 1, idx1 = (lo + hi) / 2; (lo <= hi); idx1 = (lo + hi) / 2) {
                        com.rbnb.compat.File other = (com.rbnb.compat.File) fileSets.elementAt(idx1);
                        long oIndex = Long.parseLong(other.getName().substring(2));
                        if (fsIndex < oIndex) {
                            hi = idx1 - 1;
                        } else if (fsIndex > oIndex) {
                            lo = idx1 + 1;
                        } else {
                            break;
                        }
                    }
                    fileSets.insertElementAt(files[idx], lo);
                }
            } catch (java.io.IOException e) {
            }
        }
        for (int idx = 0, endIdx = fileSets.size(); idx < endIdx; ++idx) {
            com.rbnb.compat.File fileSetFile = (com.rbnb.compat.File) fileSets.elementAt(idx);
            try {
                Seal theSeal = Seal.validate(fileSetFile.getAbsolutePath(), after, before);
                if (theSeal == null) {
                    throw new com.rbnb.api.InvalidSealException(theSeal, after, before);
                } else {
                    validSealsO.addElement(fileSetFile);
                    validSealsO.addElement(theSeal);
                    after = theSeal.getAsOf();
                }
            } catch (com.rbnb.api.InvalidSealException e) {
                invalidSealsO.addElement(fileSetFile);
                invalidSealsO.addElement(e);
                validR = false;
            }
        }
        if (!summary) {
            validR = false;
        }
        return (validR);
    }

    final void writeToArchive() throws com.rbnb.api.AddressException, com.rbnb.api.SerializeException, java.io.EOFException, java.io.IOException, java.lang.InterruptedException {
        try {
            getDoor().lock("writeToArchive");
            updateRegistration();
            if (getSet() != null) {
                if (getSet().getNchildren() == 0) {
                    setSet(null);
                } else {
                    getSet().close();
                }
            }
            int endIdx;
            if ((endIdx = getNchildren()) > 0) {
                String directory = getArchiveDirectory() + SEPARATOR;
                Seal.seal(getArchiveDirectory());
            }
        } finally {
            getDoor().unlock();
        }
    }

    /** Makes a copy of the object
	 */
    public Object clone() {
        try {
            Object clonedR = new Archive(0, 0);
            cloned(clonedR);
            return clonedR;
        } catch (Exception e) {
            return null;
        }
    }

    /** Copies all the fields of the object to the given object
	 */
    protected void cloned(Object o) {
        super.cloned(o);
        Archive clonedR = (Archive) o;
        clonedR.cachedSet = cachedSet;
        clonedR.newestFS = newestFS;
        clonedR.oldestFS = oldestFS;
        clonedR.outOfDate = outOfDate;
    }
}
