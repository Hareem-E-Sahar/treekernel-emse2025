package net.villonanny;

import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.villonanny.entity.Server;
import org.apache.commons.configuration.AbstractFileConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.log4j.Logger;

/**
 * Starts the Nanny, loads the configuration, starts each server, starts the console, 
 */
public class VilloNanny {

    private static final Logger log = Logger.getLogger(VilloNanny.class);

    public static SimpleDateFormat formatter = new SimpleDateFormat();

    private Map<String, Server> allServers;

    private Date startTime = null;

    private static VilloNanny singleton;

    private VilloNanny(String[] args) {
        formatter = new SimpleDateFormat(ConfigManager.getString("/dateFormat", "EEE dd MMMM yyyy HH:mm:ss Z"));
        String waitUntil = Util.startTimeString(args);
        if (waitUntil != null) {
            try {
                startTime = formatter.parse(waitUntil);
            } catch (ParseException e) {
                String message = "Invalid date: " + waitUntil + "; format should be like \"" + formatter.format(new Date()) + "\"";
                EventLog.log(message);
                log.error(message, e);
            }
        }
    }

    public static void main(String[] args) {
        String s = VilloNanny.class.getSimpleName() + " " + Version.VERSION + " starting...";
        EventLog.log(s);
        EventLog.log("");
        EventLog.log("evt.start01", VilloNanny.class);
        EventLog.log("evt.start02", VilloNanny.class);
        EventLog.log("evt.start03", VilloNanny.class);
        EventLog.log("evt.start04", VilloNanny.class);
        EventLog.log("");
        Util.setUtf8(args);
        log.debug(String.format("Current os.name = %s, os.arch = %s, os.version = %s", System.getProperty("os.name"), System.getProperty("os.arch"), System.getProperty("os.version")));
        log.debug("Current locale = " + Locale.getDefault());
        log.debug("Current character encoding = " + new OutputStreamWriter(System.out).getEncoding());
        if (!ConfigManager.isConfigurationThere()) {
            EventLog.log("evt.newConfigurationStart", VilloNanny.class);
            if (ConfigManager.createNewConfiguration(args)) {
                EventLog.log("evt.newConfigurationEnd01", VilloNanny.class);
                EventLog.log("evt.newConfigurationEnd02", VilloNanny.class, ConfigManager.CONFIGDIR);
            }
            return;
        } else {
            ConfigManager.loadConfiguration();
        }
        singleton = new VilloNanny(args);
        singleton.execute();
    }

    public static VilloNanny getInstance() {
        return singleton;
    }

    public Map<String, Server> getAllServers() {
        return allServers;
    }

    private void execute() {
        int totServers = 0;
        try {
            allServers = createServerList();
            if (allServers.size() == 0) {
                EventLog.log("No servers defined. Exiting...");
                return;
            }
            for (Server server : allServers.values()) {
                try {
                    if (server.isEnabled()) {
                        server.login();
                        totServers++;
                    }
                } catch (ConversationException e) {
                    EventLog.log("Can't login, disabling server \"" + server.getServerDesc() + "\"");
                    server.setEnabledAndStartStop(false);
                }
            }
            if (totServers == 0) {
                EventLog.log("msg.noServers", this.getClass());
                return;
            }
            if (startTime != null) {
                Util.sleep(startTime.getTime() - System.currentTimeMillis());
            }
            Console c = Console.getInstance();
            for (Server server : allServers.values()) {
                server.begin();
            }
        } catch (Exception e) {
            Util.log("Program error", e);
            EventLog.log(e.getMessage());
            EventLog.log("Aborting...");
        }
    }

    /**
	 * Add a configuration listener that updates the server list when the configuration changes
	 */
    private void addConfigurationListener() {
        ConfigManager.addListener(new ConfigurationListener() {

            public void configurationChanged(ConfigurationEvent event) {
                try {
                    if (event.getType() == AbstractFileConfiguration.EVENT_RELOAD && !event.isBeforeUpdate()) {
                        Map<String, Server> addedServers = updateServerList(allServers);
                        for (Server server : addedServers.values()) {
                            server.begin();
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to reload configuration", e);
                }
            }
        });
    }

    /**
	 * Initial creation of the server list
	 * @return
	 */
    private Map<String, Server> createServerList() {
        return updateServerList(new HashMap<String, Server>());
    }

    /**
	 * If servers are added or deleted, modify the server list
	 * @param allServers the previous servers
	 * @return the map of added servers than need to be started
	 */
    private Map<String, Server> updateServerList(Map<String, Server> allServers) {
        List<Server> deletableServers = new ArrayList<Server>(allServers.values());
        Map<String, Server> addedServers = new HashMap<String, Server>();
        List<SubnodeConfiguration> serverConfigs = ConfigManager.configurationsAt("/server");
        for (SubnodeConfiguration serverConfig : serverConfigs) {
            String serverId = Server.idFromConfig(serverConfig);
            Server existingServer = allServers.get(serverId);
            if (existingServer == null) {
                Server newServer = new Server(serverConfig);
                addedServers.put(serverId, newServer);
                allServers.put(serverId, newServer);
            } else {
                if (addedServers.values().contains(existingServer)) {
                    EventLog.log("evt.duplicateServer", this.getClass(), serverId);
                    continue;
                }
                deletableServers.remove(existingServer);
                existingServer.updateConfig(serverConfig);
                boolean enabledInConfig = serverConfig.getBoolean("/@enabled");
                existingServer.setEnabledAndStartStop(enabledInConfig);
            }
        }
        for (Server removedServer : deletableServers) {
            removedServer.terminate();
            allServers.remove(removedServer.getServerId());
        }
        return addedServers;
    }
}
