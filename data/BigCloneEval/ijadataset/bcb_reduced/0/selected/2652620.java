package net.urlgrey.mythpodcaster.transcode;

import java.io.File;
import java.io.FileWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import net.urlgrey.mythpodcaster.dao.MythRecordingsDAO;
import net.urlgrey.mythpodcaster.dao.TranscodingProfilesDAO;
import net.urlgrey.mythpodcaster.domain.Channel;
import net.urlgrey.mythpodcaster.domain.RecordedProgram;
import net.urlgrey.mythpodcaster.domain.RecordedSeries;
import net.urlgrey.mythpodcaster.xml.FeedSubscriptionItem;
import net.urlgrey.mythpodcaster.xml.ScopeEnum;
import net.urlgrey.mythpodcaster.xml.TranscodingProfile;
import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * @author scottkidder
 *
 */
public class IndividualFeedTranscodeTaskImpl implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(IndividualFeedTranscodeTaskImpl.class);

    private FeedSubscriptionItem subscription;

    private SyndFeed feed;

    private Comparator<SyndEntry> entryComparator = new FeedEntryComparator();

    private TranscodingProfilesDAO transcodingProfilesDao;

    private MythRecordingsDAO recordingsDao;

    private FeedFileAccessor feedFileAccessor;

    private String feedFilePath;

    private String feedFileExtension;

    private StatusBean status;

    public IndividualFeedTranscodeTaskImpl(FeedSubscriptionItem subscription, SyndFeed feed) {
        this.subscription = subscription;
        this.feed = feed;
    }

    @Override
    public void run() {
        boolean feedUpdated = false;
        if (subscription.isActive()) {
            final List entries = feed.getEntries();
            final RecordedSeries series;
            if (ScopeEnum.MOST_RECENT.equals(subscription.getScope())) {
                series = recordingsDao.findRecordedSeries(subscription.getSeriesId(), subscription.getNumberOfMostRecentToKeep());
            } else {
                series = recordingsDao.findRecordedSeries(subscription.getSeriesId());
            }
            if (series == null) {
                LOGGER.debug("No recordings found for recordId[" + subscription.getSeriesId() + "]");
            }
            if (series != null) {
                for (RecordedProgram program : series.getRecordedPrograms()) {
                    if (program.getEndTime() == null || program.getEndTime().after(new Date())) {
                        LOGGER.debug("Skipping recorded program, end-time is in future (still recording): programId[" + program.getProgramId() + "]");
                        continue;
                    }
                    if (ScopeEnum.SPECIFIC_RECORDINGS.equals(this.subscription.getScope()) && (false == this.subscription.getRecordedProgramKeys().contains(program.getKey()))) {
                        LOGGER.debug("Skipping recorded program, not specified as a recording to transcode for this subscription: key[" + program.getKey() + "]");
                        continue;
                    }
                    boolean found = false;
                    LOGGER.debug("Locating program in existing feed entries: programId[" + program.getProgramId() + "], key[" + program.getKey() + "]");
                    if (entries != null && entries.size() > 0) {
                        final Iterator it = entries.iterator();
                        while (it.hasNext()) {
                            final SyndEntry entry = (SyndEntry) it.next();
                            if (entry.getUri() == null) {
                                continue;
                            }
                            String entryKey = entry.getUri();
                            if (program.getKey().equalsIgnoreCase(entryKey)) {
                                found = true;
                                break;
                            }
                        }
                    }
                    if (found) {
                        LOGGER.debug("Program was found in feed, continuing");
                    } else {
                        status.setTranscodingProfileName(transcodingProfilesDao.findAllProfiles().get(subscription.getTranscodeProfile()).getDisplayName());
                        status.setTranscodingProgramEpisodeName(program.getKey());
                        status.setTranscodingProgramName(series.getTitle());
                        status.setCurrentTranscodeStart(new Date());
                        final Channel channel = this.recordingsDao.findChannel(program.getRecordedProgramKey().getChannelId());
                        feedFileAccessor.addProgramToFeed(series, program, channel, feed, subscription.getTranscodeProfile());
                        feedUpdated = true;
                        status.setCurrentTranscodeStart(null);
                    }
                }
            }
            if (entries != null && entries.size() > 0) {
                LOGGER.debug("Identifying series recordings no longer in database but still in feed, recordId[" + subscription.getSeriesId() + "]");
                final Map<String, TranscodingProfile> transcodingProfiles = transcodingProfilesDao.findAllProfiles();
                Set<SyndEntry> entryRemovalSet = new HashSet<SyndEntry>();
                final Iterator it = entries.iterator();
                while (it.hasNext()) {
                    final SyndEntry entry = (SyndEntry) it.next();
                    if (entry.getUri() == null) {
                        feedUpdated = true;
                        entryRemovalSet.add(entry);
                        LOGGER.debug("Feed entry has null URI (GUID), removing because it cannot be identified");
                        continue;
                    }
                    String episodeKey = entry.getUri();
                    boolean found = false;
                    if (series != null) {
                        for (RecordedProgram program : series.getRecordedPrograms()) {
                            if (program.getKey().equalsIgnoreCase(episodeKey)) {
                                found = true;
                                break;
                            }
                        }
                    }
                    if (ScopeEnum.SPECIFIC_RECORDINGS.equals(subscription.getScope()) && false == subscription.getRecordedProgramKeys().contains(episodeKey)) {
                        found = false;
                    }
                    if (found) {
                        LOGGER.debug("Feed entry is current, continuing: uid[" + entry.getUri() + "]");
                    } else {
                        LOGGER.debug("Feed entry will be deleted: uid[" + entry.getUri() + "]");
                        feedUpdated = true;
                        entryRemovalSet.add(entry);
                        final List enclosures = entry.getEnclosures();
                        if (enclosures.size() > 0) {
                            final SyndEnclosure enclosure = (SyndEnclosure) enclosures.get(0);
                            final TranscodingProfile transcodingProfile = transcodingProfiles.get(subscription.getTranscodeProfile());
                            transcodingProfile.deleteEncoding(this.feedFilePath, enclosure.getUrl(), entry.getUri());
                        } else {
                            LOGGER.info("No enclosures specified in the entry, removing from feed and continuing");
                        }
                    }
                }
                entries.removeAll(entryRemovalSet);
            }
            if (feedUpdated) {
                final File encodingDirectory = new File(feedFilePath, subscription.getTranscodeProfile());
                final File feedFile = new File(encodingDirectory, subscription.getSeriesId() + feedFileExtension);
                LOGGER.debug("Changes made to feed, updating feed file: path[" + feedFile.getAbsolutePath() + "]");
                Collections.sort(entries, entryComparator);
                File transformedFeedFile = null;
                try {
                    FileWriter writer = new FileWriter(feedFile);
                    SyndFeedOutput output = new SyndFeedOutput();
                    output.output(feed, writer);
                    transformedFeedFile = feedFileAccessor.generateTransformationFromFeed(feedFile, feed, subscription.getSeriesId());
                } catch (Exception e) {
                    LOGGER.error("Error rendering feed", e);
                    if (feedFile.canWrite()) {
                        feedFile.delete();
                    }
                    if (transformedFeedFile != null && transformedFeedFile.canWrite()) {
                        transformedFeedFile.delete();
                    }
                }
            }
        }
    }

    private class FeedEntryComparator implements Comparator<SyndEntry> {

        @Override
        public int compare(SyndEntry entry1, SyndEntry entry2) {
            if (entry1 == null || entry1.getPublishedDate() == null) {
                return 1;
            }
            if (entry2 == null) {
                return -1;
            }
            return ((-1) * entry1.getPublishedDate().compareTo(entry2.getPublishedDate()));
        }
    }

    @Required
    public void setStatus(StatusBean status) {
        this.status = status;
    }

    @Required
    public void setRecordingsDao(MythRecordingsDAO recordingsDao) {
        this.recordingsDao = recordingsDao;
    }

    @Required
    public void setTranscodingProfilesDao(TranscodingProfilesDAO transcodingProfilesDao) {
        this.transcodingProfilesDao = transcodingProfilesDao;
    }

    @Required
    public void setFeedFileAccessor(FeedFileAccessor feedFileAccessor) {
        this.feedFileAccessor = feedFileAccessor;
    }

    @Required
    public void setFeedFilePath(String feedFilePath) {
        this.feedFilePath = feedFilePath;
    }

    @Required
    public void setFeedFileExtension(String feedFileExtension) {
        this.feedFileExtension = feedFileExtension;
    }
}
