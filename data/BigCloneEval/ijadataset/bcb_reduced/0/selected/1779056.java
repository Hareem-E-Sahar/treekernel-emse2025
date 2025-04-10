package de.nava.informa.utils;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import de.nava.informa.core.CategoryIF;
import de.nava.informa.core.ChannelBuilderIF;
import de.nava.informa.core.ChannelGroupIF;
import de.nava.informa.core.ChannelIF;

/**
 * This class can be used as central repository for storing a
 * collection of channel objects and maintaining them (by specifying
 * update intervals).
 *
 * @author Niko Schmuck (niko@nava.de)
 */
public class ChannelRegistry {

    private static Log logger = LogFactory.getLog(ChannelRegistry.class);

    public static final int DEFAULT_ACCEPT_NR_ERRORS = 10;

    private int acceptNrOfErrors;

    private ChannelBuilderIF builder;

    private ChannelGroupIF channels;

    private Timer updateDaemon;

    private Map<URL, UpdateChannelTask> updateTasks;

    private Map<URL, UpdateChannelInfo> channelInfos;

    /**
   * Constructor for a new ChannelRegistry object, the new items found by
   * scanning the are created using the given <code>builder</code>.
   *
   * @param builder The ChannelBuilderIF to use for creating news items.
   */
    public ChannelRegistry(ChannelBuilderIF builder) {
        this.builder = builder;
        this.channels = builder.createChannelGroup("Default");
        updateDaemon = new Timer(true);
        updateTasks = new HashMap<URL, UpdateChannelTask>();
        channelInfos = new HashMap<URL, UpdateChannelInfo>();
        acceptNrOfErrors = DEFAULT_ACCEPT_NR_ERRORS;
    }

    /**
   * Adds one new channel object (instantiated with the help of the
   * given URL from where the channel can be retrieved from) to the
   * registry.
   *
   * @param url - the URL where the channel news can be retrieved.
   * @param interval - time in seconds between update retrieval.
   * @param active - wether regular updates should be executed.
   */
    public ChannelIF addChannel(URL url, int interval, boolean active) {
        return addChannel(url, null, interval, active);
    }

    /**
   * Adds one new channel object in the given category to the
   * registry.
   *
   * @param url - the URL where the channel news can be retrieved.
   * @param categories - the categories to which this channel should be
   *                     added to (collection of CategoryIF objects).
   * @param interval - time in seconds between update retrieval.
   * @param active - wether regular updates should be executed.
   */
    public ChannelIF addChannel(URL url, Collection<CategoryIF> categories, int interval, boolean active) {
        ChannelIF channel = builder.createChannel("[uninitialized channel]");
        channel.setCategories(categories);
        channel.setLocation(url);
        channel = addChannel(channel, active, interval);
        return channel;
    }

    /**
   * Given a stand-alone Channel (i.e. just read in from disk) we 
   * add it into the ChannelGroup and activated if necessary.
   * 
   * @param channel - Fully realized Channel
   * @param active - Same as above
   * @param interval - Same as above
   */
    public ChannelIF addChannel(ChannelIF channel, boolean active, int interval) {
        channels.add(channel);
        logger.debug("added channel " + channel.getId() + " to registry");
        if (active) {
            activateChannel(channel, interval);
        }
        return channel;
    }

    /**
   * Activates a channel and looks for new items for the given channel.
   *
   * @param channel The ChannelIF to scan for updates
   * @param interval Difference between channel updates in seconds
   */
    public void activateChannel(ChannelIF channel, int interval) {
        if (updateTasks.get(channel.getLocation()) == null) {
            UpdateChannelInfo info = channelInfos.get(channel.getLocation());
            if (info == null) {
                info = new UpdateChannelInfo(acceptNrOfErrors);
                info.setFormatDetected(false);
                channelInfos.put(channel.getLocation(), info);
            } else {
                info.reset();
            }
            UpdateChannelTask task = new UpdateChannelTask(this, builder, channel, info);
            updateDaemon.schedule(task, 100, interval * 1000);
            logger.info("activating channel updates for " + channel.getTitle());
            updateTasks.put(channel.getLocation(), task);
        }
    }

    public ChannelIF getChannel(long id) {
        return channels.getById(id);
    }

    /**
   * Gets all the channels in the registry.
   *
   * @return A collection of ChannelIF objects.
   */
    public Collection getChannels() {
        return channels.getAll();
    }

    public ChannelGroupIF getChannelGroup() {
        return channels;
    }

    public void setChannelGroup(ChannelGroupIF channels) {
        this.channels = channels;
    }

    /**
   * Removes a channel from the registry. First it is cleanly deactivated.
   *
   * @param channel The ChannelIF object to remove.
   */
    public void removeChannel(ChannelIF channel) {
        deactivateChannel(channel);
        channels.remove(channel);
        logger.debug("removing channel from registry: " + channel.getTitle());
    }

    /**
   * Deactivates a channel, no more updates are made.
   */
    public void deactivateChannel(ChannelIF channel) {
        UpdateChannelTask task = updateTasks.get(channel.getLocation());
        if (task != null) {
            logger.debug("update task canceled for " + channel.getTitle());
            task.cancel();
            updateTasks.remove(channel.getLocation());
        }
    }

    /**
   * Returns wether the channel update task is still active or not.
   */
    public boolean isActiveChannel(ChannelIF channel) {
        UpdateChannelTask task = updateTasks.get(channel.getLocation());
        return (task != null);
    }

    public UpdateChannelInfo getUpdateInfo(ChannelIF channel) {
        return channelInfos.get(channel.getLocation());
    }

    /**
   * Gets the scheduled time of the next channel update.
   *
   * @param channel The ChannelIF to retrieve information for.
   * @return The date of the next execution, 0 if not available
   */
    public long getScheduledUpdateTime(ChannelIF channel) {
        UpdateChannelTask task = updateTasks.get(channel.getLocation());
        if (task != null) {
            return task.scheduledExecutionTime();
        }
        return 0;
    }

    public int getAcceptNrOfErrors() {
        return acceptNrOfErrors;
    }

    /**
   * Set number of channel parser errors acceptable after channel is not
   * longer automatically updated.
   */
    public void setAcceptNrOfErrors(int acceptNrOfErrors) {
        this.acceptNrOfErrors = acceptNrOfErrors;
    }
}
