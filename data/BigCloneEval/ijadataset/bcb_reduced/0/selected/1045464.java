package flex.messaging.cluster;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Collections;
import java.util.TreeSet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import flex.messaging.Destination;
import flex.messaging.MessageBroker;
import flex.messaging.config.ClusterSettings;
import flex.messaging.endpoints.Endpoint;
import flex.messaging.util.ClassUtil;

/**
 * @exclude
 * The manager of all clusters defined in services-config.xml, and the broker
 * for the clusters created for clustered destinations.
 */
public class ClusterManager {

    /**
     * A link to the MessageBroker.
     */
    private MessageBroker broker;

    /**
     * A mapping between the cluster ids and the Cluster instances.
     * name=clusterId value=clusterInstance
     */
    private LinkedHashMap<String, Cluster> clusters;

    /**
     * A mapping between destinations and the Cluster instances.
     */
    private Map<String, Cluster> clustersForDestination;

    /**
     * A mapping between cluster ids and their configuration files.
     * name=clusterId value=propsFile
     */
    private Map<String, Element> clusterConfig;

    /**
     * A mapping between cluster ids and ClusterSettings instances.
     * name=clusterId value=ClusterSettings
     */
    private Map<String, ClusterSettings> clusterSettings;

    /**
     * A mapped between destinations and a boolean representing
     * whether or not the backend for the destination is shared.
     */
    private Map<String, Boolean> backendSharedForDestination;

    /**
     * The default cluster when the cluster id for the destination
     * is unspecified.
     */
    private Cluster defaultCluster;

    /**
     * The id of the default cluster.
     */
    private String defaultClusterId;

    /**
     * The manager of all clusters defined in services-config.xml, and the broker
     * for the clusters created for clustered destinations.  This class provides
     * an entry point and abstraction to the logical cluster implementation as
     * well as the specific cluster implementation.
     */
    public ClusterManager(MessageBroker broker) {
        this.broker = broker;
        clusters = new LinkedHashMap<String, Cluster>();
        clusterConfig = new HashMap<String, Element>();
        clusterSettings = new HashMap<String, ClusterSettings>();
        clustersForDestination = new HashMap<String, Cluster>();
        backendSharedForDestination = new HashMap<String, Boolean>();
    }

    /**
     * The MessageBroker for this cluster.
     *
     * @return The defined MessageBroker.
     */
    public MessageBroker getMessageBroker() {
        return broker;
    }

    /**
     * The default cluster when the cluster id for the destination
     * is unspecified.
     */
    public Cluster getDefaultCluster() {
        return defaultCluster;
    }

    /**
     * The id of the default cluster.
     */
    public String getDefaultClusterId() {
        return defaultClusterId;
    }

    /**
     * Invoke an endpoint operation across the cluster.
     * <p>
     * NOTE: Endpoints don't reference a specific cluster so the default cluster is used for the broadcast.
     * If no default cluster is defined the operation is broadcast over all defined clusters.
     * </p>
     *
     * @param endpointId The id of the remote endpoint across the cluster to invoke an operation on.
     * @param operationName The name of the operation to invoke.
     * @param params The arguments to use for operation invocation.
     */
    public void invokeEndpointOperation(String endpointId, String operationName, Object[] params) {
        Object[] arguments = new Object[2 + params.length];
        arguments[0] = endpointId;
        arguments[1] = operationName;
        int n = params.length;
        for (int i = 2, j = 0; j < n; ++i, ++j) arguments[i] = params[j];
        if (defaultCluster != null) {
            defaultCluster.broadcastServiceOperation(operationName, arguments);
        } else {
            for (Cluster cluster : clusters.values()) cluster.broadcastServiceOperation(operationName, arguments);
        }
    }

    /**
     * Invoke an endpoint operation on a specific peer within the cluster.
     * <p>
     * NOTE: Endpoints don't reference a specific cluster so the default cluster is used for the broadcast.
     * If no default cluster is defined the operation is broadcast over all defined clusters.
     * </p>
     *
     * @param endpointId The id of the remote endpoint across the cluster to invoke an operation on.
     * @param operationName The name of the operation to invoke.
     * @param params The arguments to use for operation invocation.
     * @param targetAddress The peer node that the operation should be invoked on.
     */
    public void invokePeerToPeerEndpointOperation(String endpointId, String operationName, Object[] params, Object targetAddress) {
        Object[] arguments = new Object[2 + params.length];
        arguments[0] = endpointId;
        arguments[1] = operationName;
        int n = params.length;
        for (int i = 2, j = 0; j < n; ++i, ++j) arguments[i] = params[j];
        if (defaultCluster != null) {
            defaultCluster.sendPointToPointServiceOperation(operationName, arguments, targetAddress);
        } else {
            for (Cluster cluster : clusters.values()) {
                cluster.sendPointToPointServiceOperation(operationName, arguments, targetAddress);
            }
        }
    }

    /**
     * Invoke a service-related operation, which usually includes a Message as a method parameter. This method
     * allows a local service to process a Message and then send the Message to the services on all peer nodes
     * so that they may perform the same processing. Invoke the service operation for the cluster, identified by
     * serviceType and destinationName.
     *
     * @param serviceType The name for the service for this destination.
     * @param destinationName The name of the destination.
     * @param operationName The name of the service operation to invoke.
     * @param params Parameters needed for the service operation.
     */
    public void invokeServiceOperation(String serviceType, String destinationName, String operationName, Object[] params) {
        Cluster c = getCluster(serviceType, destinationName);
        ArrayList newParams = new ArrayList(Arrays.asList(params));
        newParams.add(0, serviceType);
        newParams.add(1, destinationName);
        c.broadcastServiceOperation(operationName, newParams.toArray());
    }

    /**
     * Send a service-related operation in point-to-point fashion to one and only one member of the cluster.
     * This is similar to the invokeServiceOperation except that this invocation is sent to the node,
     * identified by targetAddress.
     *
     * @param serviceType The name for the service for this destination.
     * @param destinationName The name of the destination.
     * @param operationName The name of the service operation to invoke.
     * @param params Parameters needed for the service operation.
     * @param targetAddress The node that the operation should be passed to.
     */
    public void invokePeerToPeerOperation(String serviceType, String destinationName, String operationName, Object[] params, Object targetAddress) {
        Cluster c = getCluster(serviceType, destinationName);
        ArrayList newParams = new ArrayList(Arrays.asList(params));
        newParams.add(0, serviceType);
        newParams.add(1, destinationName);
        c.sendPointToPointServiceOperation(operationName, newParams.toArray(), targetAddress);
    }

    /**
     * Determines whether the given destination is clustered.
     *
     * @param serviceType The name for the service for this destination.
     * @param destinationName The name of the destination.
     * @return Whether the destination is a clustered destination.
     */
    public boolean isDestinationClustered(String serviceType, String destinationName) {
        return getCluster(serviceType, destinationName) != null;
    }

    /**
     * Checks whether the give destination is configured for a shared backend.
     *
     * @param serviceType The name of the service for this destination.
     * @param destinationName The name of the destination.
     * @return Whether the destination is configured for shared backend.
     */
    public boolean isBackendShared(String serviceType, String destinationName) {
        String destKey = Cluster.getClusterDestinationKey(serviceType, destinationName);
        Boolean shared = backendSharedForDestination.get(destKey);
        return shared != null ? shared.booleanValue() : false;
    }

    /**
     * Retrieves a list of cluster nodes for the given cluster.
     *
     * @param The name of the service for the clustered destination.
     * @param The name of the destination.
     * @return List of cluster nodes for the given cluster.
     */
    public List getClusterMemberAddresses(String serviceType, String destinationName) {
        Cluster c = getCluster(serviceType, destinationName);
        return c != null ? c.getMemberAddresses() : Collections.EMPTY_LIST;
    }

    /**
     * Used for targeted endpoint operation invocations across the cluster.
     * If a default cluster is defined, its list of member addresses is returned.
     * Otherwise, a de-duped list of all member addresses from all registered clusters is returned.
     *
     * @return The list of cluster nodes that endpoint operation invocations can be issued against.
     */
    public List getClusterMemberAddresses() {
        if (defaultCluster != null) return defaultCluster.getMemberAddresses();
        TreeSet uniqueAddresses = new TreeSet();
        for (Cluster cluster : clusters.values()) uniqueAddresses.addAll(cluster.getMemberAddresses());
        return new ArrayList(uniqueAddresses);
    }

    /**
     * Find the properties file in the given cluster settings.  Read the XML based
     * cluster configuration file and save the settings and configuration for the
     * given cluster for retrieval later.
     *
     * @param settings The cluster settings for a specific cluster.
     */
    public void prepareCluster(ClusterSettings settings) {
        if (settings.getPropsFileName() == null) {
            ClusterException cx = new ClusterException();
            cx.setMessage(10201, new Object[] { settings.getClusterName(), settings.getPropsFileName() });
            throw cx;
        }
        InputStream propsFile;
        try {
            propsFile = broker.resolveInternalPath(settings.getPropsFileName());
        } catch (Throwable t) {
            ClusterException cx = new ClusterException();
            cx.setMessage(10208, new Object[] { settings.getPropsFileName() });
            cx.setRootCause(t);
            throw cx;
        }
        if (propsFile == null) {
            ClusterException cx = new ClusterException();
            cx.setMessage(10208, new Object[] { settings.getPropsFileName() });
            throw cx;
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setValidating(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(propsFile);
            if (settings.isDefault()) {
                defaultClusterId = settings.getClusterName();
            }
            clusterConfig.put(settings.getClusterName(), doc.getDocumentElement());
            clusterSettings.put(settings.getClusterName(), settings);
        } catch (Exception ex) {
            ClusterException cx = new ClusterException();
            cx.setMessage(10213);
            cx.setRootCause(ex);
            throw cx;
        }
    }

    /**
     * Retrieve the local address for the specified clustered destination.
     *
     * @param serviceType The service type of the clustered destination.
     * @param destinationName The name of the clustered destination.
     * @return The local address of the clustered destination.
     */
    public Object getLocalAddress(String serviceType, String destinationName) {
        Cluster c = getCluster(serviceType, destinationName);
        return c != null ? c.getLocalAddress() : null;
    }

    /**
     * Retrieve the local address for the default cluster or if no default cluster is defined
     * return the local address derived from the first cluster of any defined.
     *
     * @return The local address for this cluster node, or <code>null</code> if this node
     *         is not a member of any cluster.
     */
    public Object getLocalAddress() {
        if (defaultCluster != null) return defaultCluster.getLocalAddress();
        for (Entry<String, Cluster> entry : clusters.entrySet()) return entry.getValue().getLocalAddress();
        return null;
    }

    /**
     * Find the cluster for the specified cluster id.
     *
     * @param clusterId
     * @return The cluster identified by the given id.
     */
    public Cluster getClusterById(String clusterId) {
        return clusters.get(clusterId);
    }

    /**
     * Find the cluster identified by the service type and destination name.
     *
     * @param serviceType The service type of the clustered destination.
     * @param destinationName The name of the clustered destination.
     * @return The cluster identified by the serviec type and destination naem.
     */
    public Cluster getCluster(String serviceType, String destinationName) {
        Cluster cluster = null;
        try {
            String destKey = Cluster.getClusterDestinationKey(serviceType, destinationName);
            cluster = clustersForDestination.get(destKey);
            if (cluster == null) cluster = defaultCluster;
        } catch (NoClassDefFoundError nex) {
            ClusterException cx = new ClusterException();
            cx.setMessage(10202, new Object[] { destinationName });
            cx.setRootCause(nex);
            throw cx;
        }
        return cluster;
    }

    /**
     * Call destroy on each of the managed clusters.
     */
    public void destroyClusters() {
        for (Iterator<Cluster> iter = clusters.values().iterator(); iter.hasNext(); ) {
            Cluster cluster = iter.next();
            cluster.destroy();
            iter.remove();
        }
    }

    /**
     * Add the specified destination to the cluster, identitied by clusterId if available.  If the cluster
     * is not currently defined, create the cluster.  Also, setup the load balancing urls and shared
     * backend information for this clustered destination and endpoint.
     *
     * @param clusterId The cluster id that this destination wants to be associated with.
     * @param serviceType The service type for the clustered destination.
     * @param destinationName The name of the clustered destination.
     * @param channelId The channel id that should be added to the cluster load balancing.
     * @param endpointUrl The endpoint url that should be added to the cluster load balancing.
     * @param endpointPort The endpoint port that should be added to the cluster load balancing.
     * @param sharedBackend Whether the destination has shared backend set to true or not.
     */
    public void clusterDestinationChannel(String clusterId, String serviceType, String destinationName, String channelId, String endpointUrl, int endpointPort, boolean sharedBackend) {
        Cluster cluster = getClusterById(clusterId);
        String destKey = Cluster.getClusterDestinationKey(serviceType, destinationName);
        if (cluster == null) {
            if (!clusterConfig.containsKey(clusterId)) {
                ClusterException cx = new ClusterException();
                cx.setMessage(10207, new Object[] { destinationName, clusterId });
                throw cx;
            }
            cluster = createCluster(clusterId, serviceType, destinationName);
        } else {
            clustersForDestination.put(destKey, cluster);
        }
        backendSharedForDestination.put(destKey, sharedBackend ? Boolean.TRUE : Boolean.FALSE);
        if (cluster.getURLLoadBalancing()) cluster.addLocalEndpointForChannel(serviceType, destinationName, channelId, endpointUrl, endpointPort);
    }

    /**
     * Adds the destination to the cluster.  The settings for the clustered destination are
     * available from the <code>Destination</code> object.
     *
     * @param destination The destination to be clustered.
     */
    public void clusterDestination(Destination destination) {
        String clusterId = destination.getNetworkSettings().getClusterId();
        if (clusterId == null) clusterId = getDefaultClusterId();
        ClusterSettings cls = clusterSettings.get(clusterId);
        if (cls == null) {
            ClusterException ce = new ClusterException();
            ce.setMessage(10217, new Object[] { destination.getId(), clusterId });
            throw ce;
        }
        for (String channelId : destination.getChannels()) {
            Endpoint endpoint = broker.getEndpoint(channelId);
            String endpointUrl = endpoint.getUrl();
            int endpointPort = endpoint.getPort();
            if (cls.getURLLoadBalancing()) {
                int tokenStart = endpointUrl.indexOf('{');
                if (tokenStart != -1) {
                    int tokenEnd = endpointUrl.indexOf('}', tokenStart);
                    if (tokenEnd == -1) tokenEnd = endpointUrl.length(); else tokenEnd++;
                    ClusterException ce = new ClusterException();
                    ce.setMessage(10209, new Object[] { destination.getId(), channelId, endpointUrl.substring(tokenStart, tokenEnd) });
                    throw ce;
                }
            }
            clusterDestinationChannel(clusterId, destination.getServiceType(), destination.getId(), channelId, endpointUrl, endpointPort, destination.getNetworkSettings().isSharedBackend());
        }
    }

    public List getEndpointsForDestination(String serviceType, String destinationName) {
        Cluster c = getCluster(serviceType, destinationName);
        return c != null ? c.getAllEndpoints(serviceType, destinationName) : null;
    }

    /**
     * Create the cluster based on the cluster settings already available. The cluster
     * is added to the cluster managers list of clusters indexed by the cluster id.
     * The cluster is also associated with the specified service type and destination
     * name.  The cluster id is unique across all clusters managed by this cluster
     * manager.  The cluster may be associated with more than one cluster destination.
     *
     * @param clusterId The cluster id.
     * @param serviceType The service type of the clustered destination.
     * @param destinationName The destination name for the clustered destination.
     * @return The new cluster.
     */
    private Cluster createCluster(String clusterId, String serviceType, String destinationName) {
        String destKey = Cluster.getClusterDestinationKey(serviceType, destinationName);
        Element propsFile = clusterConfig.get(clusterId);
        ClusterSettings cls = clusterSettings.get(clusterId);
        Cluster cluster = null;
        Class clusterClass = ClassUtil.createClass(cls.getImplementationClass());
        Constructor clusterConstructor = null;
        try {
            clusterConstructor = clusterClass.getConstructor(new Class[] { ClusterManager.class });
        } catch (Exception e) {
            ClusterException cx = new ClusterException();
            cx.setMessage(10210);
            cx.setRootCause(e);
            throw cx;
        }
        try {
            cluster = (Cluster) clusterConstructor.newInstance(new Object[] { this });
            cluster.setClusterPropertiesFile(propsFile);
            cluster.setURLLoadBalancing(cls.getURLLoadBalancing());
            cluster.initialize(clusterId, cls.getProperties());
        } catch (Exception e) {
            ClusterException cx = new ClusterException();
            cx.setMessage(10211);
            cx.setRootCause(e);
            throw cx;
        }
        clustersForDestination.put(destKey, cluster);
        clusters.put(clusterId, cluster);
        if (defaultClusterId != null && defaultClusterId.equals(clusterId)) defaultCluster = cluster;
        return cluster;
    }
}
