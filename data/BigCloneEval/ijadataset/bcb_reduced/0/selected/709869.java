package de.fzi.mappso.communication;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.WeakHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import de.fzi.mappso.align.Config;
import de.fzi.mappso.align.MapPSOConfigurationException;

/**
 * Factory for {@link ClusterCommunicator}s.
 * The factory creates only a single instance of the {@link ClusterCommunicator} specified
 * by the global configuration parameters.
 * 
 * @author Juergen Bock (bock@fzi.de)
 *
 */
public class ClusterCommunicatorFactory {

    private static final Log logger = LogFactory.getLog(ClusterCommunicatorFactory.class);

    /**
     * Singleton instance of this factory.
     */
    private static final ClusterCommunicatorFactory theInstance = new ClusterCommunicatorFactory();

    private Map<Topology, ClusterCommunicator> ccMap;

    /**
     * Creates a new instance of this factory.
     * Explicit instantiation is forbidden.
     * Use {@link #getInstance()} instead.
     */
    private ClusterCommunicatorFactory() {
        ccMap = new WeakHashMap<Topology, ClusterCommunicator>();
    }

    /**
     * Obtains an instance of this factory.
     * @return Instance of this factory.
     */
    public static ClusterCommunicatorFactory getInstance() {
        return theInstance;
    }

    /**
     * Obtains an instance of the {@link ClusterCommunicator}
     * specified by the configuration paramters
     * for the given {@link Topology}.
     * @param topology Topology whose particle clusters shall communicate
     *                 via the {@link ClusterCommunicator}.
     * @return {@link ClusterCommunicator} instance.
     * @throws NullPointerException if argument is <code>null</code>.
     * @throws MapPSOConfigurationException 
     */
    public ClusterCommunicator getClusterCommunication(Topology topology) throws MapPSOConfigurationException {
        guard_getClusterCommunication(topology);
        logger.info("Initialising cluster communication strategy ...");
        if (ccMap.containsKey(topology)) return ccMap.get(topology);
        final String ccClassName = Config.getMainConfig().getProperty(Config.CLUSTER_COMMUNICATOR);
        if (ccClassName == null) {
            final String errMsg = "Cluster communication configuration parameter missing.";
            logger.error(errMsg);
            throw new MapPSOConfigurationException(errMsg);
        }
        if (ccClassName.isEmpty()) {
            final String errMsg = "Cluster communication configuration parameter is empty.";
            logger.error(errMsg);
            throw new MapPSOConfigurationException(errMsg);
        }
        ClusterCommunicator newCC = initialiseClusterCommunication(ccClassName, topology);
        ccMap.put(topology, newCC);
        if (logger.isDebugEnabled()) logger.debug("Created cluster communication " + newCC.getClass().getName());
        return newCC;
    }

    /**
     * Creates a new instance of the specified cluster communication.
     * @param ccClassName Qualified Java class name of the cluster communication
     *                    to be instantiated.
     * @param topology Topology, whose particle clusters shall communicate via the cluster communication.
     * @return New cluster communication instance.
     * @throws MapPSOConfigurationException if the specified class cannot be instantiated.
     */
    private ClusterCommunicator initialiseClusterCommunication(String ccClassName, Topology topology) throws MapPSOConfigurationException {
        logger.info("Initialising cluster communication ...");
        ClusterCommunicator clusterCommunicator;
        Class<?> classInstance;
        try {
            classInstance = Class.forName(ccClassName);
            Constructor<?> cons = classInstance.getConstructor(new Class[] { Topology.class });
            clusterCommunicator = (ClusterCommunicator) cons.newInstance(topology);
        } catch (ClassNotFoundException e) {
            final String errMsg = "Cannot find specified cluster communication class " + ccClassName;
            logger.error(errMsg, e);
            throw new MapPSOConfigurationException(errMsg, e);
        } catch (Exception e) {
            final String errMsg = "Unable to instantiate specified cluster communication class " + ccClassName;
            logger.error(errMsg, e);
            throw new MapPSOConfigurationException(errMsg, e);
        }
        if (logger.isDebugEnabled()) logger.debug("Created cluster communication " + clusterCommunicator.getClass().getName());
        return clusterCommunicator;
    }

    /**
     * Guard for the {@link #getClusterCommunication(Topology)} method.
     * Checks if the argument is <code>null</code>.
     * @param topology Argument to be checked.
     * @throws NullPointerException if argument is <code>null</code>.
     */
    private void guard_getClusterCommunication(Topology topology) {
        if (topology == null) {
            final String errMsg = "topology == null";
            logger.error(errMsg);
            throw new NullPointerException(errMsg);
        }
    }
}
