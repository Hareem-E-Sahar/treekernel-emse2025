package com.aelitis.azureus.plugins.removerules;

import java.net.InetAddress;
import java.util.*;
import org.gudy.azureus2.plugins.torrent.*;
import org.gudy.azureus2.plugins.disk.DiskManagerFileInfo;
import org.gudy.azureus2.plugins.download.*;
import org.gudy.azureus2.plugins.logging.LoggerChannel;
import org.gudy.azureus2.plugins.ui.config.*;
import org.gudy.azureus2.plugins.ui.model.BasicPluginConfigModel;
import org.gudy.azureus2.plugins.*;
import org.gudy.azureus2.core3.util.*;

public class DownloadRemoveRulesPlugin implements Plugin, DownloadManagerListener {

    public static final int INITIAL_DELAY = 60 * 1000;

    public static final int DELAYED_REMOVAL_PERIOD = 60 * 1000;

    public static final int AELITIS_BIG_TORRENT_SEED_LIMIT = 10000;

    public static final int AELITIS_SMALL_TORRENT_SEED_LIMIT = 1000;

    public static final int MAX_SEED_TO_PEER_RATIO = 10;

    public static final String UPDATE_TRACKER = "tracker.update.vuze.com";

    protected PluginInterface plugin_interface;

    protected boolean closing;

    protected Map dm_listener_map = new HashMap(10);

    protected List monitored_downloads = new ArrayList();

    protected LoggerChannel log;

    protected BooleanParameter remove_unauthorised;

    protected BooleanParameter remove_unauthorised_seeding_only;

    protected BooleanParameter remove_unauthorised_data;

    protected BooleanParameter remove_update_torrents;

    public static void load(PluginInterface plugin_interface) {
        plugin_interface.getPluginProperties().setProperty("plugin.version", "1.0");
        plugin_interface.getPluginProperties().setProperty("plugin.name", "Download Remove Rules");
    }

    public void initialize(PluginInterface _plugin_interface) {
        plugin_interface = _plugin_interface;
        log = plugin_interface.getLogger().getChannel("DLRemRules");
        BasicPluginConfigModel config = plugin_interface.getUIManager().createBasicPluginConfigModel("torrents", "download.removerules.name");
        config.addLabelParameter2("download.removerules.unauthorised.info");
        remove_unauthorised = config.addBooleanParameter2("download.removerules.unauthorised", "download.removerules.unauthorised", false);
        remove_unauthorised_seeding_only = config.addBooleanParameter2("download.removerules.unauthorised.seedingonly", "download.removerules.unauthorised.seedingonly", true);
        remove_unauthorised_data = config.addBooleanParameter2("download.removerules.unauthorised.data", "download.removerules.unauthorised.data", false);
        remove_unauthorised.addEnabledOnSelection(remove_unauthorised_seeding_only);
        remove_unauthorised.addEnabledOnSelection(remove_unauthorised_data);
        remove_update_torrents = config.addBooleanParameter2("download.removerules.updatetorrents", "download.removerules.updatetorrents", true);
        new DelayedEvent("DownloadRemovalRules", INITIAL_DELAY, new AERunnable() {

            public void runSupport() {
                plugin_interface.getDownloadManager().addListener(DownloadRemoveRulesPlugin.this);
            }
        });
    }

    public void downloadAdded(final Download download) {
        if (!download.isPersistent()) {
            return;
        }
        if (download.getFlag(Download.FLAG_LOW_NOISE)) {
            DiskManagerFileInfo[] files = download.getDiskManagerFileInfo();
            if (files.length == 1) {
                DiskManagerFileInfo file = files[0];
                if (file.getDownloaded() == file.getLength() && !file.getFile().exists()) {
                    log.log("Removing low-noise download '" + download.getName() + " as data missing");
                    removeDownload(download, false);
                }
            }
        }
        DownloadTrackerListener listener = new DownloadTrackerListener() {

            public void scrapeResult(DownloadScrapeResult response) {
                if (closing) {
                    return;
                }
                handleScrape(download, response);
            }

            public void announceResult(DownloadAnnounceResult response) {
                if (closing) {
                    return;
                }
                handleAnnounce(download, response);
            }
        };
        monitored_downloads.add(download);
        dm_listener_map.put(download, listener);
        download.addTrackerListener(listener);
    }

    protected void handleScrape(Download download, DownloadScrapeResult response) {
        String status = response.getStatus();
        if (status == null) {
            status = "";
        }
        handleAnnounceScrapeStatus(download, status);
    }

    protected void handleAnnounce(Download download, DownloadAnnounceResult response) {
        String reason = "";
        if (response.getResponseType() == DownloadAnnounceResult.RT_ERROR) {
            reason = response.getError();
            if (reason == null) {
                reason = "";
            }
        }
        handleAnnounceScrapeStatus(download, reason);
    }

    protected void handleAnnounceScrapeStatus(Download download, String status) {
        if (!monitored_downloads.contains(download)) {
            return;
        }
        status = status.toLowerCase();
        boolean download_completed = download.isComplete();
        if (status.indexOf("not authori") != -1 || status.toLowerCase().indexOf("unauthori") != -1) {
            if (remove_unauthorised.getValue() && ((!remove_unauthorised_seeding_only.getValue()) || download_completed)) {
                log.log(download.getTorrent(), LoggerChannel.LT_INFORMATION, "Download '" + download.getName() + "' is unauthorised and removal triggered");
                removeDownload(download, remove_unauthorised_data.getValue());
                return;
            }
        }
        Torrent torrent = download.getTorrent();
        if (torrent != null && torrent.getAnnounceURL() != null) {
            String url_string = torrent.getAnnounceURL().toString().toLowerCase();
            if (url_string.indexOf(UPDATE_TRACKER) != -1) {
                if ((download_completed && status.indexOf("too many seeds") != -1) || status.indexOf("too many peers") != -1) {
                    log.log(download.getTorrent(), LoggerChannel.LT_INFORMATION, "Download '" + download.getName() + "' being removed on instruction from the tracker");
                    removeDownloadDelayed(download, false);
                } else if (download_completed && remove_update_torrents.getValue()) {
                    long seeds = download.getLastScrapeResult().getSeedCount();
                    long peers = download.getLastScrapeResult().getNonSeedCount();
                    if (seeds / (peers == 0 ? 1 : peers) > MAX_SEED_TO_PEER_RATIO) {
                        log.log(download.getTorrent(), LoggerChannel.LT_INFORMATION, "Download '" + download.getName() + "' being removed to reduce swarm size");
                        removeDownloadDelayed(download, false);
                    } else {
                        long creation_time = download.getCreationTime();
                        long running_mins = (SystemTime.getCurrentTime() - creation_time) / (60 * 1000);
                        if (running_mins > 15) {
                            boolean big_torrent = torrent.getSize() > 1024 * 1024;
                            if ((seeds > AELITIS_BIG_TORRENT_SEED_LIMIT && big_torrent) || (seeds > AELITIS_SMALL_TORRENT_SEED_LIMIT && !big_torrent)) {
                                log.log("Download '" + download.getName() + "' being removed to reduce swarm size");
                                removeDownloadDelayed(download, false);
                            }
                        }
                    }
                }
            }
        }
    }

    protected void removeDownloadDelayed(final Download download, final boolean remove_data) {
        monitored_downloads.remove(download);
        plugin_interface.getUtilities().createThread("delayedRemoval", new AERunnable() {

            public void runSupport() {
                try {
                    Thread.sleep(DELAYED_REMOVAL_PERIOD);
                    removeDownload(download, remove_data);
                } catch (Throwable e) {
                    Debug.printStackTrace(e);
                }
            }
        });
    }

    protected void removeDownload(final Download download, final boolean remove_data) {
        monitored_downloads.remove(download);
        if (download.getState() == Download.ST_STOPPED) {
            try {
                download.remove(false, remove_data);
            } catch (Throwable e) {
                log.logAlert("Automatic removal of download '" + download.getName() + "' failed", e);
            }
        } else {
            download.addListener(new DownloadListener() {

                public void stateChanged(Download download, int old_state, int new_state) {
                    log.log(download.getTorrent(), LoggerChannel.LT_INFORMATION, "download state changed to '" + new_state + "'");
                    if (new_state == Download.ST_STOPPED) {
                        try {
                            download.remove(false, remove_data);
                            String msg = plugin_interface.getUtilities().getLocaleUtilities().getLocalisedMessageText("download.removerules.removed.ok", new String[] { download.getName() });
                            if (download.getFlag(Download.FLAG_LOW_NOISE)) {
                                log.log(download.getTorrent(), LoggerChannel.LT_INFORMATION, msg);
                            } else {
                                log.logAlert(LoggerChannel.LT_INFORMATION, msg);
                            }
                        } catch (Throwable e) {
                            log.logAlert("Automatic removal of download '" + download.getName() + "' failed", e);
                        }
                    }
                }

                public void positionChanged(Download download, int oldPosition, int newPosition) {
                }
            });
            try {
                download.stop();
            } catch (DownloadException e) {
                log.logAlert("Automatic removal of download '" + download.getName() + "' failed", e);
            }
        }
    }

    public void downloadRemoved(Download download) {
        monitored_downloads.remove(download);
        DownloadTrackerListener listener = (DownloadTrackerListener) dm_listener_map.remove(download);
        if (listener != null) {
            download.removeTrackerListener(listener);
        }
    }

    public void destroyInitiated() {
        closing = true;
    }

    public void destroyed() {
    }
}
