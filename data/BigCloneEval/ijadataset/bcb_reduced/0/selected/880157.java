package flex.messaging.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import flex.messaging.LocalizedException;
import flex.messaging.config.ConfigurationConstants;

/**
 * Flex MXMLC compiler uses the result of the client configuration parser
 * to generate mixin initialization source code to be added to the SWF by
 * PreLink. It also requires a list of channel classes to be added as
 * dependencies.
 *
 * @exclude
 */
public class ServicesDependencies {

    private static final String ADVANCED_CHANNELSET_CLASS = "mx.messaging.AdvancedChannelSet";

    private static final String ADVANCED_MESSAGING_SUPPORT_CLASS = "flex.messaging.services.AdvancedMessagingSupport";

    private static final String REDIRECT_URL = "redirect-url";

    private boolean containsClientLoadBalancing;

    private String xmlInit = "";

    private StringBuffer imports = new StringBuffer();

    private StringBuffer references = new StringBuffer();

    private List channelClasses;

    private Map configPaths;

    private Map lazyAssociations;

    private static final List channel_excludes = new ArrayList();

    static {
        channel_excludes.add(REDIRECT_URL);
    }

    public static final boolean traceConfig = (System.getProperty("trace.config") != null);

    public ServicesDependencies(String path, String parserClass, String contextRoot) {
        ClientConfiguration config = getClientConfiguration(path, parserClass);
        if (config != null) {
            Map importMap = new HashMap();
            lazyAssociations = new HashMap();
            configPaths = config.getConfigPaths();
            xmlInit = codegenXmlInit(config, contextRoot, importMap);
            codegenServiceImportsAndReferences(importMap, imports, references);
            channelClasses = listChannelClasses(config);
        }
    }

    public Set getLazyAssociations(String destination) {
        if (lazyAssociations == null) {
            lazyAssociations = new HashMap();
        }
        return (Set) lazyAssociations.get(destination);
    }

    public void addLazyAssociation(String destination, String associationProp) {
        Set la = getLazyAssociations(destination);
        if (la == null) {
            la = new HashSet();
            lazyAssociations.put(destination, la);
        }
        la.add(associationProp);
    }

    public String getServerConfigXmlInit() {
        return xmlInit;
    }

    public String getImports() {
        return imports.toString();
    }

    public String getReferences() {
        return references.toString();
    }

    public List getChannelClasses() {
        return channelClasses;
    }

    public void addChannelClass(String className) {
        channelClasses.add(className);
    }

    public void addConfigPath(String path, long modified) {
        configPaths.put(path, new Long(modified));
    }

    public Map getConfigPaths() {
        return configPaths;
    }

    public static ClientConfiguration getClientConfiguration(String path, String parserClass) {
        ClientConfiguration config = new ClientConfiguration();
        ConfigurationParser parser = getConfigurationParser(parserClass);
        if (parser == null) {
            LocalizedException lme = new LocalizedException();
            lme.setMessage(10138);
            throw lme;
        }
        LocalFileResolver local = new LocalFileResolver();
        parser.parse(path, local, config);
        config.addConfigPath(path, new File(path).lastModified());
        return config;
    }

    static ConfigurationParser getConfigurationParser(String className) {
        ConfigurationParser parser = null;
        Class parserClass = null;
        if (className != null) {
            try {
                parserClass = Class.forName(className);
                parser = (ConfigurationParser) parserClass.newInstance();
            } catch (Throwable t) {
                if (traceConfig) {
                    System.out.println("Could not load services configuration parser as: " + className);
                }
            }
        }
        if (parser == null) {
            try {
                Class.forName("org.apache.xpath.CachedXPathAPI");
                className = "flex.messaging.config.ApacheXPathClientConfigurationParser";
                parserClass = Class.forName(className);
                parser = (ConfigurationParser) parserClass.newInstance();
            } catch (Throwable t) {
                if (traceConfig) {
                    System.out.println("Could not load configuration parser as: " + className);
                }
            }
        }
        if (parser == null) {
            try {
                className = "flex.messaging.config.XPathClientConfigurationParser";
                parserClass = Class.forName(className);
                Class.forName("javax.xml.xpath.XPathExpressionException");
                parser = (ConfigurationParser) parserClass.newInstance();
            } catch (Throwable t) {
                if (traceConfig) {
                    System.out.println("Could not load configuration parser as: " + className);
                }
            }
        }
        if (traceConfig && parser != null) {
            System.out.println("Services Configuration Parser: " + parser.getClass().getName());
        }
        return parser;
    }

    private static List listChannelClasses(ServicesConfiguration config) {
        List channelList = new ArrayList();
        Iterator it = config.getAllChannelSettings().values().iterator();
        while (it.hasNext()) {
            ChannelSettings settings = (ChannelSettings) it.next();
            if (!settings.serverOnly) {
                String clientType = settings.getClientType();
                channelList.add(clientType);
            }
        }
        return channelList;
    }

    /**
     * Emits source code declaration of public var xml:XML (unnamed package), containing ServicesConfiguration as e4x.
     */
    private String codegenXmlInit(ServicesConfiguration config, String contextRoot, Map serviceImportMap) {
        StringBuffer e4x = new StringBuffer();
        String channelSetImplToImport = null;
        e4x.append("<services>\n");
        if (config.getDefaultChannels().size() > 0) {
            e4x.append("\t<default-channels>\n");
            for (Iterator chanIter = config.getDefaultChannels().iterator(); chanIter.hasNext(); ) {
                String id = (String) chanIter.next();
                e4x.append("\t\t<channel ref=\"" + id + "\"/>\n");
            }
            e4x.append("\t</default-channels>\n");
        }
        ClusterSettings defaultCluster = config.getDefaultCluster();
        if (defaultCluster != null && !defaultCluster.getURLLoadBalancing()) defaultCluster = null;
        for (Iterator servIter = config.getAllServiceSettings().iterator(); servIter.hasNext(); ) {
            ServiceSettings entry = (ServiceSettings) servIter.next();
            String serviceType = entry.getId();
            e4x.append("\t<service id=\"");
            e4x.append(serviceType);
            e4x.append("\"");
            e4x.append(">\n");
            String serviceClass = entry.getClassName();
            if (ADVANCED_MESSAGING_SUPPORT_CLASS.equals(serviceClass)) channelSetImplToImport = ADVANCED_CHANNELSET_CLASS;
            String useTransactionsStr = entry.getProperties().getPropertyAsString("use-transactions", null);
            if (useTransactionsStr != null) {
                e4x.append("\t\t<properties>\n\t\t\t<use-transactions>" + useTransactionsStr + "</use-transactions>\n");
                e4x.append("\t\t</properties>\n");
            }
            for (Iterator destIter = entry.getDestinationSettings().values().iterator(); destIter.hasNext(); ) {
                DestinationSettings dest = (DestinationSettings) destIter.next();
                String destination = dest.getId();
                e4x.append("\t\t<destination id=\"" + destination + "\">\n");
                ConfigMap metadata = dest.getProperties().getPropertyAsMap("metadata", null);
                boolean closePropTag = false;
                if (metadata != null) {
                    e4x.append("\t\t\t<properties>\n\t\t\t\t<metadata\n");
                    String extendsStr = metadata.getPropertyAsString("extends", null);
                    if (extendsStr != null) {
                        e4x.append(" extends=\"");
                        e4x.append(extendsStr);
                        e4x.append("\"");
                    }
                    e4x.append(">");
                    closePropTag = true;
                    List identities = metadata.getPropertyAsList("identity", null);
                    if (identities != null) {
                        Iterator it = identities.iterator();
                        while (it.hasNext()) {
                            Object o = it.next();
                            String identityName = null;
                            String undefinedValue = null;
                            if (o instanceof String) {
                                identityName = (String) o;
                            } else if (o instanceof ConfigMap) {
                                identityName = ((ConfigMap) o).getPropertyAsString("property", null);
                                undefinedValue = ((ConfigMap) o).getPropertyAsString("undefined-value", null);
                            }
                            if (identityName != null) {
                                e4x.append("\t\t\t\t\t<identity property=\"");
                                e4x.append(identityName);
                                e4x.append("\"");
                                if (undefinedValue != null) {
                                    e4x.append(" undefined-value=\"");
                                    e4x.append(undefinedValue);
                                    e4x.append("\"");
                                }
                                e4x.append("/>\n");
                            }
                        }
                    }
                    codegenServiceAssociations(metadata, e4x, destination, "one-to-many");
                    codegenServiceAssociations(metadata, e4x, destination, "many-to-many");
                    codegenServiceAssociations(metadata, e4x, destination, "one-to-one");
                    codegenServiceAssociations(metadata, e4x, destination, "many-to-one");
                    e4x.append("\t\t\t\t</metadata>\n");
                }
                String itemClass = dest.getProperties().getPropertyAsString("item-class", null);
                if (itemClass != null) {
                    if (!closePropTag) {
                        e4x.append("\t\t\t<properties>\n");
                        closePropTag = true;
                    }
                    e4x.append("\t\t\t\t<item-class>");
                    e4x.append(itemClass);
                    e4x.append("</item-class>\n");
                }
                ConfigMap network = dest.getProperties().getPropertyAsMap("network", null);
                ConfigMap clusterInfo = null;
                ConfigMap pagingInfo = null;
                ConfigMap reconnectInfo = null;
                if (network != null || defaultCluster != null) {
                    if (!closePropTag) {
                        e4x.append("\t\t\t<properties>\n");
                        closePropTag = true;
                    }
                    e4x.append("\t\t\t\t<network>\n");
                    if (network != null) pagingInfo = network.getPropertyAsMap("paging", null);
                    if (pagingInfo != null) {
                        String enabled = pagingInfo.getPropertyAsString("enabled", "false");
                        e4x.append("\t\t\t\t\t<paging enabled=\"");
                        e4x.append(enabled);
                        e4x.append("\"");
                        String size = pagingInfo.getPropertyAsString("page-size", pagingInfo.getPropertyAsString("pageSize", null));
                        if (size != null) {
                            e4x.append(" page-size=\"");
                            e4x.append(size);
                            e4x.append("\"");
                            e4x.append(" pageSize=\"");
                            e4x.append(size);
                            e4x.append("\"");
                        }
                        e4x.append("/>\n");
                    }
                    if (network != null) reconnectInfo = network.getPropertyAsMap("reconnect", null);
                    if (reconnectInfo != null) {
                        String fetchOption = reconnectInfo.getPropertyAsString("fetch", "IDENTITY");
                        e4x.append("\t\t\t\t\t<reconnect fetch=\"");
                        e4x.append(fetchOption.toUpperCase());
                        e4x.append("\" />\n");
                    }
                    if (network != null) {
                        String reliable = network.getPropertyAsString("reliable", "false");
                        if (Boolean.valueOf(reliable).booleanValue()) {
                            e4x.append("\t\t\t\t\t<reliable>");
                            e4x.append(reliable);
                            e4x.append("</reliable>\n");
                        }
                    }
                    if (network != null) clusterInfo = network.getPropertyAsMap("cluster", null);
                    if (clusterInfo != null) {
                        String clusterId = clusterInfo.getPropertyAsString("ref", null);
                        ClusterSettings clusterSettings = config.getClusterSettings(clusterId);
                        if (clusterSettings != null && clusterSettings.getURLLoadBalancing()) {
                            e4x.append("\t\t\t\t\t<cluster ref=\"");
                            e4x.append(clusterId);
                            e4x.append("\"/>\n");
                        }
                    } else if (defaultCluster != null) {
                        e4x.append("\t\t\t\t\t<cluster");
                        if (defaultCluster.getClusterName() != null) {
                            e4x.append(" ref=\"");
                            e4x.append(defaultCluster.getClusterName());
                            e4x.append("\"");
                        }
                        e4x.append("/>\n");
                    }
                    e4x.append("\t\t\t\t</network>\n");
                }
                String useTransactions = dest.getProperties().getPropertyAsString("use-transactions", null);
                if (useTransactions != null) {
                    if (!closePropTag) {
                        e4x.append("\t\t\t<properties>\n");
                        closePropTag = true;
                    }
                    e4x.append("\t\t\t\t<use-transactions>" + useTransactions + "</use-transactions>\n");
                }
                String autoSyncEnabled = dest.getProperties().getPropertyAsString("auto-sync-enabled", "true");
                if (autoSyncEnabled.equalsIgnoreCase("false")) {
                    if (!closePropTag) {
                        e4x.append("\t\t\t<properties>\n");
                        closePropTag = true;
                    }
                    e4x.append("\t\t\t\t<auto-sync-enabled>false</auto-sync-enabled>\n");
                }
                if (closePropTag) {
                    e4x.append("\t\t\t</properties>\n");
                }
                e4x.append("\t\t\t<channels>\n");
                for (Iterator chanIter = dest.getChannelSettings().iterator(); chanIter.hasNext(); ) {
                    e4x.append("\t\t\t\t<channel ref=\"" + ((ChannelSettings) chanIter.next()).getId() + "\"/>\n");
                }
                e4x.append("\t\t\t</channels>\n");
                e4x.append("\t\t</destination>\n");
            }
            e4x.append("\t</service>\n");
        }
        e4x.append("\t<channels>\n");
        String channelType;
        for (Iterator chanIter = config.getAllChannelSettings().values().iterator(); chanIter.hasNext(); ) {
            ChannelSettings chan = (ChannelSettings) chanIter.next();
            if (chan.getServerOnly()) continue;
            channelType = chan.getClientType();
            serviceImportMap.put(channelType, channelType);
            e4x.append("\t\t<channel id=\"" + chan.getId() + "\" type=\"" + channelType + "\">\n");
            StringBuffer channelProps = new StringBuffer();
            containsClientLoadBalancing = false;
            channelProperties(chan.getProperties(), channelProps, "\t\t\t\t");
            if (!containsClientLoadBalancing) e4x.append("\t\t\t<endpoint uri=\"" + chan.getClientParsedUri(contextRoot) + "\"/>\n");
            containsClientLoadBalancing = false;
            e4x.append("\t\t\t<properties>\n");
            e4x.append(channelProps);
            e4x.append("\t\t\t</properties>\n");
            e4x.append("\t\t</channel>\n");
        }
        e4x.append("\t</channels>\n");
        FlexClientSettings flexClientSettings = (config instanceof ClientConfiguration) ? ((ClientConfiguration) config).getFlexClientSettings() : null;
        if (flexClientSettings != null && flexClientSettings.getHeartbeatIntervalMillis() > 0) {
            e4x.append("\t<flex-client>\n");
            e4x.append("\t\t<heartbeat-interval-millis>");
            e4x.append(flexClientSettings.getHeartbeatIntervalMillis());
            e4x.append("</heartbeat-interval-millis>");
            e4x.append("\t</flex-client>\n");
        }
        e4x.append("</services>");
        StringBuffer advancedMessagingSupport = new StringBuffer();
        if (channelSetImplToImport != null) {
            serviceImportMap.put("ChannelSetImpl", channelSetImplToImport);
            String alias = "flex.messaging.messages.ReliabilityMessage";
            String className = "mx.messaging.messages.ReliabilityMessage";
            advancedMessagingSupport.append("     ServerConfig.channelSetFactory = AdvancedChannelSet;\n");
            advancedMessagingSupport.append("     try {\n");
            advancedMessagingSupport.append("     if (flash.net.getClassByAlias(\"" + alias + "\") == null){\n");
            advancedMessagingSupport.append("         flash.net.registerClassAlias(\"" + alias + "\", " + className + ");}\n");
            advancedMessagingSupport.append("     } catch (e:Error) {\n");
            advancedMessagingSupport.append("         flash.net.registerClassAlias(\"" + alias + "\", " + className + "); }\n");
            if (flexClientSettings != null && flexClientSettings.getReliableReconnectDurationMillis() > 0) {
                advancedMessagingSupport.append("     AdvancedChannelSet.reliableReconnectDuration =");
                advancedMessagingSupport.append(flexClientSettings.getReliableReconnectDurationMillis());
                advancedMessagingSupport.append(";\n");
            }
        }
        String generatedChunk = "\n     ServerConfig.xml =\n" + e4x.toString() + ";\n" + advancedMessagingSupport.toString();
        return generatedChunk;
    }

    /**
     * Process channel properties recursively.
     */
    private void channelProperties(ConfigMap properties, StringBuffer buf, String indent) {
        for (Iterator nameIter = properties.propertyNames().iterator(); nameIter.hasNext(); ) {
            String name = (String) nameIter.next();
            Object value = properties.get(name);
            if (value instanceof String) {
                addStringProperty(buf, indent, name, (String) value);
            } else if (value instanceof List) {
                List children = (List) value;
                for (Iterator childrenIter = children.iterator(); childrenIter.hasNext(); ) addStringProperty(buf, indent, name, (String) childrenIter.next());
                ;
            } else if (value instanceof ConfigMap) {
                ConfigMap childProperties = (ConfigMap) value;
                buf.append(indent);
                buf.append("<" + name + ">\n");
                if (ConfigurationConstants.CLIENT_LOAD_BALANCING_ELEMENT.equals(name)) containsClientLoadBalancing = true;
                channelProperties(childProperties, buf, indent + "\t");
                buf.append(indent);
                buf.append("</" + name + ">\n");
            }
        }
    }

    private void addStringProperty(StringBuffer buf, String indent, String name, String value) {
        if (!channel_excludes.contains(name)) {
            buf.append(indent);
            buf.append("<" + name + ">" + value + "</" + name + ">\n");
        }
    }

    /**
     *
     */
    public void codegenServiceAssociations(ConfigMap metadata, StringBuffer e4x, String destination, String relation) {
        List references = metadata.getPropertyAsList(relation, null);
        if (references != null) {
            Iterator it = references.iterator();
            while (it.hasNext()) {
                Object ref = it.next();
                if (ref instanceof ConfigMap) {
                    ConfigMap refMap = (ConfigMap) ref;
                    String name = refMap.getPropertyAsString("property", null);
                    String associatedDestination = refMap.getPropertyAsString("destination", null);
                    String lazy = refMap.getPropertyAsString("lazy", null);
                    String loadOnDemand = refMap.getPropertyAsString("load-on-demand", null);
                    String hierarchicalEvents = refMap.getPropertyAsString("hierarchical-events", null);
                    String pageSize = refMap.getPropertyAsString("page-size", refMap.getPropertyAsString("pageSize", null));
                    String pagedUpdates = refMap.getPropertyAsString("paged-updates", null);
                    String cascade = refMap.getPropertyAsString("cascade", null);
                    String ordered = refMap.getPropertyAsString("ordered", null);
                    e4x.append("\t\t\t\t\t<");
                    e4x.append(relation);
                    if (lazy != null) {
                        e4x.append(" lazy=\"");
                        e4x.append(lazy);
                        e4x.append("\"");
                        if (Boolean.valueOf(lazy.toLowerCase().trim()).booleanValue()) {
                            addLazyAssociation(destination, name);
                        }
                    }
                    e4x.append(" property=\"");
                    e4x.append(name);
                    e4x.append("\" destination=\"");
                    e4x.append(associatedDestination);
                    e4x.append("\"");
                    String readOnly = refMap.getPropertyAsString("read-only", null);
                    if (readOnly != null && readOnly.equalsIgnoreCase("true")) {
                        e4x.append(" read-only=\"true\"");
                    }
                    if (loadOnDemand != null && loadOnDemand.equalsIgnoreCase("true")) e4x.append(" load-on-demand=\"true\"");
                    if (hierarchicalEvents != null && hierarchicalEvents.equalsIgnoreCase("true")) e4x.append(" hierarchical-events=\"true\"");
                    if (pagedUpdates != null) e4x.append(" paged-updates=\"" + pagedUpdates + "\"");
                    if (pageSize != null) e4x.append(" page-size=\"" + pageSize + "\"");
                    if (cascade != null) e4x.append(" cascade=\"" + cascade + "\"");
                    if (ordered != null) e4x.append(" ordered=\"" + ordered + "\"");
                    e4x.append("/>\n");
                }
            }
        }
    }

    /**
     * This method will return an import and variable reference for channels specified in the map.
     * @param map HashMap containing the client side channel type to be used, typically of the form
     * "mx.messaging.channels.XXXXChannel", where the key and value are equal.
     * @param imports StringBuffer of the imports needed for the given channel definitions
     * @param references StringBuffer of the required references so that these classes will be linked in.
     */
    public static void codegenServiceImportsAndReferences(Map map, StringBuffer imports, StringBuffer references) {
        String channelSetImplType = (String) map.remove("ChannelSetImpl");
        String type;
        imports.append("import mx.messaging.config.ServerConfig;\n");
        references.append("   // static references for configured channels\n");
        for (Iterator chanIter = map.values().iterator(); chanIter.hasNext(); ) {
            type = (String) chanIter.next();
            imports.append("import ");
            imports.append(type);
            imports.append(";\n");
            references.append("   private static var ");
            references.append(type.replace('.', '_'));
            references.append("_ref:");
            references.append(type.substring(type.lastIndexOf(".") + 1) + ";\n");
        }
        if (channelSetImplType != null) imports.append("import mx.messaging.AdvancedChannelSet;\nimport mx.messaging.messages.ReliabilityMessage;\n");
    }
}
