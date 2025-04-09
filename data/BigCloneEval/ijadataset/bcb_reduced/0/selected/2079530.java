package cu.ftpd.modules.zipscript.internal;

import cu.ftpd.ServiceManager;
import cu.ftpd.filesystem.filters.DashMissingFileFilter;
import cu.ftpd.filesystem.metadata.Directory;
import cu.ftpd.filesystem.metadata.Metadata;
import cu.ftpd.logging.Logging;
import cu.ftpd.user.User;
import java.io.*;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

/**
 * one of these are created for each race.
 * they keep track of who has right to modify what files, and they also trigger sfv checks for new files
 * this class owns the sfvfile for a certain race
 *
 * @author Markus Jevring <markus@jevring.net>
 * @since 2007-maj-17 : 03:44:02
 * @version $Id: Race.java 292 2009-03-04 19:44:36Z jevring $
 */
public class Race implements Serializable {

    private final File racedir;

    private final SfvFile sfv;

    private final String section;

    private String leader = "";

    private String currentProgressBarDirname = "";

    private boolean alreadyHalfway = false;

    private boolean started;

    private long estimatedSize = 0;

    private long starttime = 0;

    private long endtime = 0;

    private long lastUpdated = 0;

    private final String siteShortName;

    private HashMap<String, Long> calculatedChecksums;

    private final HashMap<String, Racer> racers;

    private final HashMap<String, RaceGroup> groups;

    private final HashMap<String, Racer> completeFiles;

    private transient LinkedList<Racer> racerRanking;

    private transient LinkedList<RaceGroup> groupRanking;

    private transient RaceLog log;

    private final Pattern progressbarDeletePattern = Pattern.compile("\\[.*\\] - \\[.*\\] - \\[.*\\]");

    private final MessageFormat progress = new MessageFormat("[{0}] - [ INCOMPLETE {1} of {2} files] - [{0}]");

    private final MessageFormat complete = new MessageFormat("[{0}] - [ {1} {2} - COMPLETE ] - [{0}]");

    public Race(String sfvFilePath, String section, String siteShortName) {
        racers = new HashMap<String, Racer>();
        groups = new HashMap<String, RaceGroup>();
        calculatedChecksums = new HashMap<String, Long>();
        completeFiles = new HashMap<String, Racer>();
        starttime = System.currentTimeMillis();
        this.section = section;
        File sfvfile = new File(sfvFilePath);
        racedir = sfvfile.getParentFile();
        sfv = new SfvFile(sfvfile);
        this.siteShortName = siteShortName;
        sfv.populate();
    }

    public synchronized void start() {
        if (!started) {
            sfv.populate();
            for (Map.Entry<String, Long> file : sfv.getFiles().entrySet()) {
                Long precalcChecksum = calculatedChecksums.get(file.getKey());
                boolean ok;
                if (precalcChecksum != null) {
                    ok = verify(new File(racedir, file.getKey()), file.getValue(), precalcChecksum);
                } else {
                    ok = verify(new File(racedir, file.getKey()), file.getValue(), 0);
                }
                if (!ok) {
                    Racer racer = completeFiles.remove(file.getKey());
                    if (racer != null) {
                        RaceGroup group = groups.get(racer.getGroup());
                        RaceFile rf = racer.getFile(file.getKey());
                        if (rf != null) {
                            racer.deleteFile(rf);
                            group.deleteFile(rf);
                            saveRaceToDisk();
                        }
                    }
                } else {
                    Racer racer = completeFiles.get(file.getKey());
                    if (racer == null) {
                        String username = "unknown";
                        String group = "unknown";
                        Directory directory = ServiceManager.getServices().getMetadataHandler().getDirectory(racedir);
                        if (directory != null) {
                            Metadata m = directory.getMetadata(file.getKey());
                            if (m != null) {
                                username = m.getUsername();
                                group = m.getGroupname();
                            }
                        }
                        completeFiles.put(file.getKey(), new Racer(username, group));
                    }
                }
            }
            started = true;
            createProgressBar();
        }
    }

    public synchronized void rescan() {
        calculatedChecksums = new HashMap<String, Long>();
        started = false;
        start();
    }

    /**
     * Verifies the integrity of a file, based on a CRC checksum. The checksum to check against comes from the supplied sfv-file.
     *
     * @param file the file to be processed.
     * @param checksum the checksum according to the sfv
     * @param precalcChecksum this is provided from teh transfer, as the crc is being calculated on-the-fly. If not, set it to 0, and one will be calculated.
     * @return true if the checksum of the file corresponded to the one in the sfv.
     */
    private boolean verify(File file, long checksum, long precalcChecksum) {
        boolean ok = false;
        File missing = new File(file.getParentFile(), file.getName() + "-missing");
        if (!file.exists()) {
            ok = false;
        } else {
            if (precalcChecksum == 0) {
                CRC32 checker = new CRC32();
                byte[] buf = new byte[8192];
                BufferedInputStream in = null;
                try {
                    in = new BufferedInputStream(new FileInputStream(file));
                    int len;
                    while ((len = in.read(buf)) >= 0) {
                        checker.update(buf, 0, len);
                    }
                    if (checker.getValue() == checksum) {
                        ok = true;
                    } else {
                        ok = false;
                    }
                } catch (FileNotFoundException e) {
                    ok = false;
                } catch (IOException e) {
                    ok = false;
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            Logging.getErrorLog().reportException("Failed to close input stream", e);
                        }
                    }
                }
            } else {
                if (checksum == precalcChecksum) {
                    ok = true;
                }
            }
        }
        if (ok) {
            calculatedChecksums.put(file.getName(), checksum);
            if (missing.exists()) {
                missing.delete();
            }
            return true;
        } else {
            try {
                if (file.exists()) {
                    boolean delok = file.delete();
                }
                if (!missing.exists()) {
                    missing.createNewFile();
                }
            } catch (IOException e1) {
                Logging.getErrorLog().reportError("Failed to create file: " + missing.getAbsolutePath());
            }
            return false;
        }
    }

    public synchronized void createProgressBar() {
        for (File file : racedir.listFiles()) {
            if (file.isDirectory() && progressbarDeletePattern.matcher(file.getName()).matches()) {
                file.delete();
            }
        }
        if (isComplete()) {
            currentProgressBarDirname = complete.format(new Object[] { siteShortName, cu.ftpd.logging.Formatter.size(getSize()), completeFiles.size() + "F" });
        } else {
            currentProgressBarDirname = progress.format(new Object[] { siteShortName, getNumerOfCurrentFiles(), getNumberOfExpectedFiles() });
        }
        File currentProgressBarDir = new File(racedir, currentProgressBarDirname);
        currentProgressBarDir.mkdir();
    }

    public synchronized boolean newFile(File file, User user, long checksum, long bytesTransferred, long transferTime) {
        String filename = file.getName();
        Long sfvfileChecksum = sfv.getChecksum(filename);
        boolean ok = false;
        if (sfvfileChecksum != null) {
            ok = verify(new File(racedir, filename), sfvfileChecksum, checksum);
            if (ok) {
                final long now = System.currentTimeMillis();
                lastUpdated = now;
                Racer racer = racers.get(user.getUsername());
                if (racer == null) {
                    racer = new Racer(user.getUsername(), user.getPrimaryGroup());
                    racers.put(user.getUsername(), racer);
                }
                RaceGroup group = groups.get(user.getPrimaryGroup());
                if (group == null) {
                    group = new RaceGroup(user.getPrimaryGroup());
                    groups.put(user.getPrimaryGroup(), group);
                }
                RaceFile rf = new RaceFile(filename, bytesTransferred, transferTime);
                racer.addFile(rf);
                group.addFile(rf);
                completeFiles.put(filename, racer);
                if (racers.size() == 1) {
                    estimatedSize = sfv.getFiles().size() * bytesTransferred;
                    leader = user.getUsername();
                    log.firstRacer(this, user);
                } else {
                    log.newRacer(this, user);
                }
                if (racers.size() > 1) {
                    String newLeader = calculateLeadingUser();
                    if (!leader.equals(newLeader)) {
                        log.newLeader(this, leader, newLeader);
                        leader = newLeader;
                    }
                }
                if (completeFiles.size() >= Math.ceil(sfv.getFiles().size() / 2.0)) {
                    if (!alreadyHalfway) {
                        alreadyHalfway = true;
                        log.dirHalfway(this);
                    }
                } else {
                    alreadyHalfway = false;
                }
                if (isComplete()) {
                    endtime = now;
                    log.raceComplete(this);
                    log.userStats(this);
                    log.groupStats(this);
                }
            }
            createProgressBar();
            saveRaceToDisk();
        }
        return ok;
    }

    private String calculateLeadingUser() {
        int highest = 0;
        String tempLeader = "";
        for (Map.Entry<String, Racer> entry : racers.entrySet()) {
            if (entry.getValue().getNumberOfFiles() > highest) {
                highest = entry.getValue().getNumberOfFiles();
                tempLeader = entry.getKey();
            }
        }
        return tempLeader;
    }

    private RaceGroup calculateLeadingGroup() {
        int highest = 0;
        RaceGroup tempLeader = null;
        for (RaceGroup group : groups.values()) {
            if (group.getNumberOfFiles() > highest) {
                highest = group.getNumberOfFiles();
                tempLeader = group;
            }
        }
        return tempLeader;
    }

    public LinkedList<Racer> getRacersInWinningOrder() {
        if (racerRanking == null) {
            racerRanking = new LinkedList<Racer>();
            racerRanking.addAll(racers.values());
            Collections.sort(racerRanking);
        }
        return racerRanking;
    }

    public LinkedList<RaceGroup> getRaceGroupsInWinningOrder() {
        if (groupRanking == null) {
            groupRanking = new LinkedList<RaceGroup>();
            groupRanking.addAll(this.groups.values());
            Collections.sort(groupRanking);
        }
        return groupRanking;
    }

    public String getName() {
        return racedir.getName();
    }

    public int getNumberOfExpectedFiles() {
        return sfv.getFiles().size();
    }

    public int getNumerOfCurrentFiles() {
        return completeFiles.size();
    }

    public long getEstimatedSize() {
        return estimatedSize;
    }

    public long getSize() {
        long size = 0;
        File f;
        for (String filename : completeFiles.keySet()) {
            f = new File(racedir, filename);
            size += f.length();
        }
        return size;
    }

    public long getStarttime() {
        return starttime;
    }

    public long getEndtime() {
        return endtime;
    }

    public long getFinalRaceSpeed() {
        double dtime = (double) (endtime - starttime) / 1000.0d;
        return (long) (((double) estimatedSize / 1024.0D) / dtime);
    }

    public long getCurrentRaceSpeed() {
        double dtime = (double) (lastUpdated - starttime) / 1000.0d;
        return (long) (((double) estimatedSize / 1024.0D) / dtime);
    }

    public String getSectionName() {
        return section;
    }

    public String getLeader() {
        return leader;
    }

    public int getNumberOfRacers() {
        return racers.size();
    }

    public synchronized void deleteFile(File file, User user) {
        lastUpdated = System.currentTimeMillis();
        if (getNumberOfExpectedFiles() == getNumerOfCurrentFiles()) {
            log.raceIncomplete(this, user);
        }
        Racer racer = racers.get(user.getUsername());
        if (racer != null) {
            RaceGroup group = groups.get(racer.getGroup());
            completeFiles.remove(file.getName());
            calculatedChecksums.remove(file.getName());
            RaceFile rf = racer.getFile(file.getName());
            if (rf != null) {
                racer.deleteFile(rf);
                group.deleteFile(rf);
                saveRaceToDisk();
            }
        }
        if (sfv.getChecksum(file.getName()) != null) {
            File missing = new File(file.getAbsolutePath() + "-missing");
            try {
                missing.createNewFile();
            } catch (IOException e) {
                Logging.getErrorLog().reportError("Failed to create file: " + missing.getAbsolutePath());
            }
            createProgressBar();
        }
        if (file.getName().endsWith(".sfv")) {
            File[] missingFiles = racedir.listFiles(new DashMissingFileFilter());
            for (File missingFile : missingFiles) {
                missingFile.delete();
            }
            File raceinfo = new File(racedir, ".raceinfo");
            boolean ok = raceinfo.delete();
            File bar = new File(racedir, currentProgressBarDirname);
            ok = bar.delete();
        }
    }

    public boolean isComplete() {
        return sfv.getFiles().size() == completeFiles.size();
    }

    private void saveRaceToDisk() {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(new File(racedir, ".raceinfo")));
            oos.writeObject(this);
        } catch (IOException e) {
            Logging.getErrorLog().reportError("Writing race to disk failed for file: " + new File(racedir, ".raceinfo").getAbsolutePath());
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    Logging.getErrorLog().reportError("Closing race file failed: " + new File(racedir, ".raceinfo").getAbsolutePath());
                }
            }
        }
    }

    public HashMap<String, Racer> getRacers() {
        return racers;
    }

    public HashMap<String, RaceGroup> getGroups() {
        return groups;
    }

    public Racer getRacer(String username) {
        return racers.get(username);
    }

    public RaceGroup getLeadingGroup() {
        return calculateLeadingGroup();
    }

    public Racer getRacerOfFile(String filename) {
        return completeFiles.get(filename);
    }

    public File getRacedir() {
        return racedir;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    /**
     * We have to set the log here, because we serialize it, and the log isn't a serializable object by nature.
     * @param log the logger to use when logging events.
     */
    public void setLog(RaceLog log) {
        this.log = log;
    }
}
