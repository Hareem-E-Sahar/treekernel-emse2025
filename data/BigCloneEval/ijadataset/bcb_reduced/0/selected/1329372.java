package net.jetrix.config;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import java.beans.*;
import org.apache.commons.digester.*;
import org.apache.commons.beanutils.*;
import org.xml.sax.*;
import net.jetrix.*;
import net.jetrix.winlist.*;
import net.jetrix.filter.*;
import net.jetrix.commands.*;

/**
 * Server configuration. This objet reads and retains server parameters,
 * channel definitions and the ban list.
 *
 * @author Emmanuel Bourg
 * @version $Revision: 866 $, $Date: 2010-08-23 11:02:51 -0400 (Mon, 23 Aug 2010) $
 */
public class ServerConfig {

    private static Logger log = Logger.getLogger("net.jetrix");

    public static final String ENCODING = "ISO-8859-1";

    public static final String VERSION = "@version@";

    public static final int STATUS_OPENED = 0;

    public static final int STATUS_LOCKED = 1;

    private String name;

    private InetAddress host;

    private int timeout;

    private int maxChannels;

    private int maxPlayers;

    private int maxConnections;

    private String operatorPassword;

    private String adminPassword;

    private String accesslogPath;

    private String errorlogPath;

    private String channelsFile = "channels.xml";

    private String motd;

    private Locale locale;

    private List<ChannelConfig> channels = new ArrayList<ChannelConfig>();

    private List<FilterConfig> globalFilters = new ArrayList<FilterConfig>();

    private List<Listener> listeners = new ArrayList<Listener>();

    private List<Service> services = new ArrayList<Service>();

    private boolean running;

    private int status;

    private Statistics statistics = new Statistics();

    /** Datasources configuration */
    private Map<String, DataSourceConfig> datasources = new LinkedHashMap<String, DataSourceConfig>();

    /** Mail session configuration */
    private MailSessionConfig mailSessionConfig;

    /** Extended properties */
    private Properties properties = new Properties();

    private URL serverConfigURL;

    private URL channelsConfigURL;

    /**
     * Load the configuration
     */
    public void load(File file) {
        try {
            load(file.toURI().toURL());
        } catch (MalformedURLException e) {
            log.log(Level.SEVERE, "Unable to load the configuration", e);
        }
    }

    /**
     * Load the configuration.
     */
    public void load(URL serverConfigURL) {
        this.serverConfigURL = serverConfigURL;
        try {
            Digester digester = new Digester();
            digester.register("-//LFJR//Jetrix TetriNET Server//EN", findResource("tetrinet-server.dtd").toString());
            digester.setValidating(true);
            digester.addRuleSet(new ServerRuleSet());
            digester.push(this);
            Reader reader = new InputStreamReader(serverConfigURL.openStream(), ENCODING);
            digester.parse(new InputSource(reader));
            reader.close();
            digester = new Digester();
            digester.register("-//LFJR//Jetrix Channels//EN", findResource("tetrinet-channels.dtd").toString());
            digester.setValidating(true);
            digester.addRuleSet(new ChannelsRuleSet());
            digester.push(this);
            channelsConfigURL = new URL(serverConfigURL, channelsFile);
            reader = new InputStreamReader(channelsConfigURL.openStream(), ENCODING);
            digester.parse(new InputSource(reader));
            reader.close();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Unable to load the configuration", e);
        }
    }

    /**
     * Save the configuration.
     */
    public void save() throws IOException {
        PrintWriter out = null;
        try {
            File file = new File(serverConfigURL.toURI());
            out = new PrintWriter(file, ENCODING);
        } catch (URISyntaxException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        out.println("<?xml version=\"1.0\"?>");
        out.println("<!DOCTYPE tetrinet-server PUBLIC \"-//LFJR//Jetrix TetriNET Server//EN\" \"http://jetrix.sourceforge.net/dtd/tetrinet-server.dtd\">");
        out.println();
        out.println("<tetrinet-server host=\"" + (host == null ? "[ALL]" : host.getHostAddress()) + "\">");
        out.println();
        out.println("  <!-- Server name -->");
        out.println("  <name>" + getName() + "</name>");
        out.println();
        out.println("  <!-- Server default language (using an ISO-639 two-letter language code) -->");
        out.println("  <language>" + getLocale().getLanguage() + "</language>");
        out.println();
        out.println("  <!-- How many seconds of inactivity before timeout occurs -->");
        out.println("  <timeout>" + getTimeout() + "</timeout>");
        out.println();
        out.println("  <!-- How many channels should be available on the server -->");
        out.println("  <max-channels>" + getMaxChannels() + "</max-channels>");
        out.println();
        out.println("  <!-- Maximum number of players on the server -->");
        out.println("  <max-players>" + getMaxPlayers() + "</max-players>");
        out.println();
        out.println("  <!-- Maximum number of simultaneous connections allowed from the same IP -->");
        out.println("  <max-connections>" + getMaxConnections() + "</max-connections>");
        out.println();
        out.println("  <!-- Typing /op <thispassword> will give player operator status -->");
        out.println("  <op-password>" + getOpPassword() + "</op-password>");
        out.println();
        out.println("  <!-- Use this password to log on the administration console -->");
        out.println("  <admin-password>" + getAdminPassword() + "</admin-password>");
        out.println();
        out.println("  <!-- Access Log, where requests are logged to -->");
        out.println("  <access-log path=\"" + getAccessLogPath() + "\" />");
        out.println();
        out.println("  <!-- Error Log, where errors are logged to -->");
        out.println("  <error-log  path=\"" + getErrorLogPath() + "\" />");
        out.println();
        out.println("  <!-- Path to the channels descriptor file (relative to the current configuration file) -->");
        out.println("  <channels path=\"" + getChannelsFile() + "\"/>");
        out.println();
        out.println("  <!-- Client listeners -->");
        out.println("  <listeners>");
        for (Listener listener : getListeners()) {
            String autostart = !listener.isAutoStart() ? " auto-start=\"false\"" : "";
            out.println("    <listener class=\"" + listener.getClass().getName() + "\" port=\"" + listener.getPort() + "\"" + autostart + "/>");
        }
        out.println("  </listeners>");
        out.println();
        out.println("  <!-- Services -->");
        out.println("  <services>");
        for (Service service : getServices()) {
            try {
                Map<String, Object> params = PropertyUtils.describe(service);
                String autostart = !service.isAutoStart() ? "auto-start=\"false\"" : "";
                String classname = "class=\"" + service.getClass().getName() + "\"";
                if (params.size() <= 4) {
                    out.println("    <service " + classname + " " + autostart + "/>");
                } else {
                    out.println("    <service " + classname + " " + autostart + ">");
                    for (String param : params.keySet()) {
                        PropertyDescriptor desc = PropertyUtils.getPropertyDescriptor(service, param);
                        if (!"autoStart".equals(param) && desc.getWriteMethod() != null) {
                            out.println("      <param name=\"" + param + "\" value=\"" + params.get(param) + "\"/>");
                        }
                    }
                    out.println("    </service>");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        out.println("  </services>");
        out.println();
        out.println("  <!-- Server commands -->");
        out.println("  <commands>");
        Iterator<Command> commands = CommandManager.getInstance().getCommands(AccessLevel.ADMINISTRATOR);
        while (commands.hasNext()) {
            try {
                Command command = commands.next();
                String hidden = command.isHidden() ? " hidden=\"true\"" : "";
                Command command2 = command.getClass().newInstance();
                String level = command2.getAccessLevel() != command.getAccessLevel() ? " access-level=\"" + command.getAccessLevel() + "\"" : "";
                out.println("    <command class=\"" + command.getClass().getName() + "\"" + hidden + level + "/>");
            } catch (Exception e) {
                log.log(Level.WARNING, e.getMessage(), e);
            }
        }
        out.println("  </commands>");
        out.println();
        out.println("  <ban>");
        Iterator<Banlist.Entry> entries = Banlist.getInstance().getBanlist();
        while (entries.hasNext()) {
            Banlist.Entry entry = entries.next();
            out.println("    <host>" + entry.pattern + "</host>");
        }
        out.println("  </ban>");
        out.println();
        if (!datasources.isEmpty()) {
            out.println("  <!-- Database connection parameters -->");
            out.println("  <datasources>");
            out.println();
            for (DataSourceConfig datasource : datasources.values()) {
                out.println("    <datasource name=\"" + datasource.getName() + "\">");
                out.println("      <!-- The class of the JDBC driver used -->");
                out.println("      <driver>" + datasource.getDriver() + "</driver>");
                out.println();
                out.println("      <!-- The URL of the database (jdbc:<type>://<hostname>:<port>/<database>) -->");
                out.println("      <url>" + datasource.getUrl() + "</url>");
                out.println();
                out.println("      <!-- The username connecting to the database -->");
                out.println("      <username>" + datasource.getUsername() + "</username>");
                out.println();
                out.println("      <!-- The password of the user -->");
                if (datasource.getPassword() != null) {
                    out.println("      <password>" + datasource.getPassword() + "</password>");
                } else {
                    out.println("      <password/>");
                }
                if (datasource.getMinIdle() != DataSourceManager.DEFAULT_MIN_IDLE) {
                    out.println();
                    out.println("      <!-- The minimum number of idle connections -->");
                    out.println("      <min-idle>" + datasource.getMinIdle() + "</min-idle>");
                }
                if (datasource.getMaxActive() != DataSourceManager.DEFAULT_MAX_ACTIVE) {
                    out.println();
                    out.println("      <!-- The maximum number of active connections -->");
                    out.println("      <max-active>" + datasource.getMaxActive() + "</max-active>");
                }
                out.println("    </datasource>");
                out.println();
            }
            out.println("  </datasources>");
            out.println();
        }
        if (mailSessionConfig != null) {
            StringBuilder buffer = new StringBuilder();
            buffer.append("  <mailserver host=\"").append(mailSessionConfig.getHostname()).append("\"");
            buffer.append(" port=\"").append(mailSessionConfig.getPort()).append("\"");
            if (mailSessionConfig.isAuth()) {
                buffer.append(" auth=\"true\"");
                buffer.append(" username=\"").append(mailSessionConfig.getUsername()).append("\"");
                buffer.append(" password=\"").append(mailSessionConfig.getPassword()).append("\"");
            }
            if (mailSessionConfig.isDebug()) {
                buffer.append(" debug=\"true\"");
            }
            buffer.append("/>");
            out.println("  <!-- Mail server parameters -->");
            out.println(buffer.toString());
            out.println();
        }
        if (properties != null && !properties.isEmpty()) {
            out.println("  <!-- Extended properties -->");
            out.println("  <properties>");
            for (String key : properties.stringPropertyNames()) {
                out.println("    <property name=\"" + key + "\" value=\"" + properties.getProperty(key) + "\">");
            }
            out.println("  </properties>");
            out.println();
        }
        out.println("</tetrinet-server>");
        out.flush();
        out.close();
        try {
            File file = new File(channelsConfigURL.toURI());
            out = new PrintWriter(file, ENCODING);
        } catch (URISyntaxException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        out.println("<?xml version=\"1.0\"?>");
        out.println("<!DOCTYPE tetrinet-channels PUBLIC \"-//LFJR//Jetrix Channels//EN\" \"http://jetrix.sourceforge.net/dtd/tetrinet-channels.dtd\">");
        out.println();
        out.println("<tetrinet-channels>");
        out.println();
        out.println("  <!-- Message Of The Day -->");
        out.println("  <motd><![CDATA[");
        out.println(getMessageOfTheDay());
        out.println("  ]]></motd>");
        out.println();
        Map<String, String> aliases = FilterManager.getInstance().getFilterAliases();
        out.println("  <!-- Channel filters -->");
        out.println("  <filter-definitions>");
        for (String alias : aliases.keySet()) {
            out.println("    <alias name=\"" + alias + "\" class=\"" + aliases.get(alias) + "\"/>");
        }
        out.println("  </filter-definitions>");
        out.println();
        out.println("  <!-- Global filters -->");
        out.println("  <default-filters>");
        for (FilterConfig filter : globalFilters) {
            saveFilter(filter, out, "    ");
        }
        out.println("  </default-filters>");
        out.println();
        out.println("  <!-- Winlists -->");
        out.println("  <winlists>");
        for (Winlist winlist : WinlistManager.getInstance().getWinlists()) {
            WinlistConfig config = winlist.getConfig();
            Properties props = config.getProperties();
            if (props == null || props.isEmpty()) {
                out.println("    <winlist name=\"" + winlist.getId() + "\" class=\"" + winlist.getClass().getName() + "\"/>");
            } else {
                out.println("    <winlist name=\"" + winlist.getId() + "\" class=\"" + winlist.getClass().getName() + "\">");
                for (Object name : props.keySet()) {
                    out.println("      <param name=\"" + name + "\" value=\"" + props.get(name) + "\"/>");
                }
                out.println("    </winlist>");
            }
        }
        out.println("  </winlists>");
        out.println();
        Settings settings = Settings.getDefaultSettings();
        out.println("  <!-- Default game settings -->");
        out.println("  <default-settings>");
        out.println("    <!-- What level each player starts at -->");
        out.println("    <starting-level>" + settings.getStartingLevel() + "</starting-level>");
        out.println();
        out.println("    <!-- How many lines to make before player level increases -->");
        out.println("    <lines-per-level>" + settings.getLinesPerLevel() + "</lines-per-level>");
        out.println();
        out.println("    <!-- Number of levels to increase each time -->");
        out.println("    <level-increase>" + settings.getLevelIncrease() + "</level-increase>");
        out.println();
        out.println("    <!-- Lines to make to get a special block -->");
        out.println("    <lines-per-special>" + settings.getLinesPerSpecial() + "</lines-per-special>");
        out.println();
        out.println("    <!-- Number of special blocks added each time -->");
        out.println("    <special-added>" + settings.getSpecialAdded() + "</special-added>");
        out.println();
        out.println("    <!-- Capacity of Special block inventory -->");
        out.println("    <special-capacity>" + settings.getSpecialCapacity() + "</special-capacity>");
        out.println();
        out.println("    <!-- Play by classic rules? -->");
        out.println("    <classic-rules>" + (settings.getClassicRules() ? "true" : "false") + "</classic-rules>");
        out.println();
        out.println("    <!-- Average together all player's game level? -->");
        out.println("    <average-levels>" + (settings.getAverageLevels() ? "true" : "false") + "</average-levels>");
        out.println();
        out.println("    <!-- Same sequence of blocks for all players? (tetrinet 1.14 clients only) -->");
        out.println("    <same-blocks>" + (settings.getSameBlocks() ? "true" : "false") + "</same-blocks>");
        out.println();
        out.println("    <block-occurancy>");
        for (Block block : Block.values()) {
            out.println("      <" + block.getCode() + ">" + settings.getOccurancy(block) + "</" + block.getCode() + ">");
        }
        out.println("    </block-occurancy>");
        out.println();
        out.println("    <special-occurancy>");
        for (Special special : Special.values()) {
            out.println("      <" + special.getCode() + ">" + settings.getOccurancy(special) + "</" + special.getCode() + ">");
        }
        out.println("    </special-occurancy>");
        out.println();
        out.println("    <!-- Sudden death parameters -->");
        out.println("    <sudden-death>");
        out.println("      <!-- Time in seconds before the sudden death begins, 0 to disable the sudden death -->");
        out.println("      <time>" + settings.getSuddenDeathTime() + "</time>");
        out.println();
        out.println("      <!-- The message displayed when the sudden death begins. Use \"key:\" prefix to display an internationalized message -->");
        out.println("      <message>" + settings.getSuddenDeathMessage() + "</message>");
        out.println();
        out.println("      <!-- The delay in seconds between lines additions -->");
        out.println("      <delay>" + settings.getSuddenDeathDelay() + "</delay>");
        out.println();
        out.println("      <!-- The number of lines added -->");
        out.println("      <lines-added>" + settings.getSuddenDeathLinesAdded() + "</lines-added>");
        out.println("    </sudden-death>");
        out.println();
        out.println("  </default-settings>");
        out.println();
        out.println();
        out.println("  <channels>");
        for (Channel channel : ChannelManager.getInstance().channels()) {
            ChannelConfig config = channel.getConfig();
            if (config.isPersistent()) {
                out.println("    <channel name=\"" + config.getName() + "\">");
                if (config.getDescription() != null) {
                    String description = config.getDescription();
                    description = description.contains("<") ? "<![CDATA[" + description + "]]>" : description;
                    out.println("      <description>" + description + "</description>");
                }
                if (config.getTopic() != null && config.getTopic().trim().length() > 0) {
                    out.println("      <topic>");
                    out.println("<![CDATA[");
                    out.println(config.getTopic());
                    out.println("]]>");
                    out.println("      </topic>");
                }
                if (config.getSpeed() != Speed.MIXED) {
                    out.println("      <speed>" + config.getSpeed().name().toLowerCase() + "</speed>");
                }
                if (config.isPasswordProtected()) {
                    out.println("      <password>" + config.getPassword() + "</password>");
                }
                if (config.getAccessLevel() != AccessLevel.PLAYER) {
                    out.println("      <access-level>" + config.getAccessLevel() + "</access-level>");
                }
                if (config.isIdleAllowed()) {
                    out.println("      <idle>true</idle>");
                }
                if (!config.isVisible()) {
                    out.println("      <visible>false</visible>");
                }
                if (config.getMaxPlayers() != ChannelConfig.PLAYER_CAPACITY) {
                    out.println("      <max-players>" + config.getMaxPlayers() + "</max-players>");
                }
                if (config.getMaxSpectators() != ChannelConfig.SPECTATOR_CAPACITY) {
                    out.println("      <max-spectators>" + config.getMaxSpectators() + "</max-spectators>");
                }
                if (config.getWinlistId() != null) {
                    out.println("      <winlist name=\"" + config.getWinlistId() + "\"/>");
                }
                settings = config.getSettings();
                if (!settings.useDefaultSettings()) {
                    out.println("      <settings>");
                    if (!settings.isDefaultStartingLevel()) {
                        out.println("        <starting-level>" + settings.getStartingLevel() + "</starting-level>");
                    }
                    if (!settings.isDefaultLinesPerLevel()) {
                        out.println("        <lines-per-level>" + settings.getLinesPerLevel() + "</lines-per-level>");
                    }
                    if (!settings.isDefaultLevelIncrease()) {
                        out.println("        <level-increase>" + settings.getLevelIncrease() + "</level-increase>");
                    }
                    if (!settings.isDefaultLinesPerSpecial()) {
                        out.println("        <lines-per-special>" + settings.getLinesPerSpecial() + "</lines-per-special>");
                    }
                    if (!settings.isDefaultSpecialAdded()) {
                        out.println("        <special-added>" + settings.getSpecialAdded() + "</special-added>");
                    }
                    if (!settings.isDefaultSpecialCapacity()) {
                        out.println("        <special-capacity>" + settings.getSpecialCapacity() + "</special-capacity>");
                    }
                    if (!settings.isDefaultClassicRules()) {
                        out.println("        <classic-rules>" + (settings.getClassicRules() ? "true" : "false") + "</classic-rules>");
                    }
                    if (!settings.isDefaultAverageLevels()) {
                        out.println("        <average-levels>" + (settings.getAverageLevels() ? "true" : "false") + "</average-levels>");
                    }
                    if (!settings.isDefaultSameBlocks()) {
                        out.println("        <same-blocks>" + (settings.getSameBlocks() ? "true" : "false") + "</same-blocks>");
                    }
                    if (!settings.isDefaultBlockOccurancy()) {
                        out.println("        <block-occurancy>");
                        for (Block block : Block.values()) {
                            if (settings.getOccurancy(block) != 0) {
                                out.println("          <" + block.getCode() + ">" + settings.getOccurancy(block) + "</" + block.getCode() + ">");
                            }
                        }
                        out.println("        </block-occurancy>");
                    }
                    if (!settings.isDefaultSpecialOccurancy()) {
                        out.println("        <special-occurancy>");
                        for (Special special : Special.values()) {
                            if (settings.getOccurancy(special) != 0) {
                                out.println("          <" + special.getCode() + ">" + settings.getOccurancy(special) + "</" + special.getCode() + ">");
                            }
                        }
                        out.println("        </special-occurancy>");
                    }
                    if (!settings.isDefaultSuddenDeath()) {
                        out.println("        <sudden-death>");
                        if (!settings.isDefaultSuddenDeathTime()) {
                            out.println("          <time>" + settings.getSuddenDeathTime() + "</time>");
                        }
                        if (!settings.isDefaultSuddenDeathMessage()) {
                            out.println("          <message>" + settings.getSuddenDeathMessage() + "</message>");
                        }
                        if (!settings.isDefaultSuddenDeathDelay()) {
                            out.println("          <delay>" + settings.getSuddenDeathDelay() + "</delay>");
                        }
                        if (!settings.isDefaultSuddenDeathLinesAdded()) {
                            out.println("          <lines-added>" + settings.getSuddenDeathLinesAdded() + "</lines-added>");
                        }
                        out.println("        </sudden-death>");
                    }
                    out.println("      </settings>");
                }
                Collection<MessageFilter> filters = channel.getLocalFilters();
                if (!filters.isEmpty()) {
                    out.println("      <filters>");
                    for (MessageFilter filter : filters) {
                        saveFilter(filter.getConfig(), out, "        ");
                    }
                    out.println("      </filters>");
                }
                out.println("    </channel>");
                out.println();
            }
        }
        out.println("  </channels>");
        out.println();
        out.println("</tetrinet-channels>");
        out.close();
    }

    /**
     * Write the configuration of the specified filter.
     */
    private void saveFilter(FilterConfig filter, PrintWriter out, String indent) {
        Properties props = filter.getProperties();
        if (props == null || props.isEmpty()) {
            if (filter.getName() != null) {
                out.println(indent + "<filter name=\"" + filter.getName() + "\"/>");
            } else {
                out.println(indent + "<filter class=\"" + filter.getClassname() + "\"/>");
            }
        } else {
            if (filter.getName() != null) {
                out.println(indent + "<filter name=\"" + filter.getName() + "\">");
            } else {
                out.println(indent + "<filter class=\"" + filter.getClassname() + "\">");
            }
            for (Object name : props.keySet()) {
                out.println(indent + "  <param name=\"" + name + "\" value=\"" + props.get(name) + "\"/>");
            }
            out.println(indent + "</filter>");
        }
    }

    /**
     * Locate the specified resource by searching in the classpath and in
     * the current directory.
     *
     * @param name the name of the resource
     * @return the URL of the resource, or null if it cannot be found
     * @since 0.2
     */
    private URL findResource(String name) throws MalformedURLException {
        ClassLoader loader = ServerConfig.class.getClassLoader();
        URL url = loader.getResource(name);
        if (url == null) {
            File file = new File(name);
            url = file.toURI().toURL();
        }
        return url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InetAddress getHost() {
        return host;
    }

    public void setHost(InetAddress host) {
        this.host = host;
    }

    public void setHost(String hostname) {
        if (!"[ALL]".equals(hostname)) {
            try {
                host = InetAddress.getByName(hostname);
            } catch (UnknownHostException e) {
                log.log(Level.WARNING, e.getMessage(), e);
            }
        } else {
            host = null;
        }
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void setLocale(String language) {
        this.locale = new Locale(language);
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getMaxChannels() {
        return maxChannels;
    }

    public void setMaxChannels(int maxChannels) {
        this.maxChannels = maxChannels;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public String getOpPassword() {
        return operatorPassword;
    }

    public void setOpPassword(String oppass) {
        this.operatorPassword = oppass;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getAccessLogPath() {
        return accesslogPath;
    }

    public void setAccessLogPath(String accesslogPath) {
        this.accesslogPath = accesslogPath;
    }

    public String getErrorLogPath() {
        return errorlogPath;
    }

    public void setErrorLogPath(String errorlogPath) {
        this.errorlogPath = errorlogPath;
    }

    public String getChannelsFile() {
        return channelsFile;
    }

    public void setChannelsFile(String channelsFile) {
        this.channelsFile = channelsFile;
    }

    public String getMessageOfTheDay() {
        return motd;
    }

    public void setMessageOfTheDay(String motd) {
        this.motd = motd;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Settings getDefaultSettings() {
        return Settings.getDefaultSettings();
    }

    public void setDefaultSettings(Settings defaultSettings) {
        Settings.setDefaultSettings(defaultSettings);
    }

    public List<ChannelConfig> getChannels() {
        return channels;
    }

    public void addChannel(ChannelConfig cconf) {
        channels.add(cconf);
    }

    public Iterator<FilterConfig> getGlobalFilters() {
        return globalFilters.iterator();
    }

    public void addFilter(FilterConfig filterConfig) {
        filterConfig.setGlobal(true);
        globalFilters.add(filterConfig);
    }

    public void addFilterAlias(String name, String classname) {
        FilterManager.getInstance().addFilterAlias(name, classname);
    }

    public void addWinlist(WinlistConfig config) {
        WinlistManager.getInstance().addWinlist(config);
    }

    public void addCommand(Command command) {
        CommandManager.getInstance().addCommand(command);
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public List<Listener> getListeners() {
        return listeners;
    }

    /**
     * Add a new service to the server. The service is not started
     * automatically by calling this method.
     *
     * @param service the service to add
     * @since 0.2
     */
    public void addService(Service service) {
        services.add(service);
    }

    /**
     * Return the list of services currently registered on the server
     *
     * @since 0.2
     */
    public List<Service> getServices() {
        return services;
    }

    public void addBannedHost(String host) {
        Banlist.getInstance().ban(host);
    }

    /**
     * Return the server statistics.
     */
    public Statistics getStatistics() {
        return statistics;
    }

    /**
     * @since 0.3
     */
    public Collection<DataSourceConfig> getDataSources() {
        return datasources.values();
    }

    /**
     * @since 0.3
     */
    public void addDataSource(DataSourceConfig datasource) {
        datasources.put(datasource.getName(), datasource);
    }

    /**
     * @since 0.3
     */
    public MailSessionConfig getMailSessionConfig() {
        return mailSessionConfig;
    }

    /**
     * @since 0.3
     */
    public void setMailSessionConfig(MailSessionConfig mailSessionConfig) {
        this.mailSessionConfig = mailSessionConfig;
    }

    /**
     * @since 0.3
     */
    public String getProperty(String key) {
        return properties != null ? properties.getProperty(key) : null;
    }

    /**
     * @since 0.3
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
}
