package org.mobicents.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import javax.transaction.TransactionManager;
import org.apache.log4j.Logger;
import org.jboss.cache.Cache;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;
import org.jboss.cache.buddyreplication.BuddyGroup;
import org.jboss.cache.config.Configuration;
import org.jboss.cache.config.Configuration.CacheMode;
import org.jboss.cache.notifications.annotation.BuddyGroupChanged;
import org.jboss.cache.notifications.annotation.CacheListener;
import org.jboss.cache.notifications.annotation.NodeRemoved;
import org.jboss.cache.notifications.annotation.ViewChanged;
import org.jboss.cache.notifications.event.BuddyGroupChangedEvent;
import org.jboss.cache.notifications.event.NodeRemovedEvent;
import org.jboss.cache.notifications.event.ViewChangedEvent;
import org.jgroups.Address;
import org.mobicents.cache.MobicentsCache;
import org.mobicents.cluster.cache.ClusteredCacheData;
import org.mobicents.cluster.cache.ClusteredCacheDataIndexingHandler;
import org.mobicents.cluster.cache.DefaultClusteredCacheDataIndexingHandler;
import org.mobicents.cluster.election.ClientLocalListenerElector;
import org.mobicents.cluster.election.ClusterElector;

/**
 * Listener that is to be used for cluster wide replication(meaning no buddy
 * replication, no data gravitation). It will index activity on nodes marking
 * current node as owner(this is semi-gravitation behavior (we don't delete, we
 * just mark)). 
 * 
 * Indexing is only at node level, i.e., there is no
 * reverse indexing, so it has to iterate through whole resource group data FQNs to check which
 * nodes should be taken over.
 * 
 * @author <a href="mailto:baranowb@gmail.com">Bartosz Baranowski </a>
 * @author martins
 */
@CacheListener(sync = false)
public class DefaultMobicentsCluster implements MobicentsCluster {

    private static final String FQN_SEPARATOR = Fqn.SEPARATOR;

    private static final String BUDDY_BACKUP_FQN_ROOT = "/_BUDDY_BACKUP_/";

    private static final Logger logger = Logger.getLogger(DefaultMobicentsCluster.class);

    private static final String BUDDIES_STORE = "MC_BUDDIES";

    private final SortedSet<FailOverListener> failOverListeners;

    @SuppressWarnings("unchecked")
    private final ConcurrentHashMap<Fqn, DataRemovalListener> dataRemovalListeners;

    private final MobicentsCache mobicentsCache;

    private final TransactionManager txMgr;

    private final ClusterElector elector;

    private final DefaultClusteredCacheDataIndexingHandler clusteredCacheDataIndexingHandler;

    private List<Address> currentView;

    private boolean started;

    @SuppressWarnings("unchecked")
    public DefaultMobicentsCluster(MobicentsCache watchedCache, TransactionManager txMgr, ClusterElector elector) {
        this.failOverListeners = Collections.synchronizedSortedSet(new TreeSet<FailOverListener>(new FailOverListenerPriorityComparator()));
        this.dataRemovalListeners = new ConcurrentHashMap<Fqn, DataRemovalListener>();
        this.mobicentsCache = watchedCache;
        this.txMgr = txMgr;
        this.elector = elector;
        this.clusteredCacheDataIndexingHandler = new DefaultClusteredCacheDataIndexingHandler();
    }

    public Address getLocalAddress() {
        return mobicentsCache.getJBossCache().getLocalAddress();
    }

    public List<Address> getClusterMembers() {
        if (currentView != null) {
            return Collections.unmodifiableList(currentView);
        } else {
            final Address localAddress = getLocalAddress();
            if (localAddress == null) {
                return Collections.emptyList();
            } else {
                final List<Address> list = new ArrayList<Address>();
                list.add(localAddress);
                return Collections.unmodifiableList(list);
            }
        }
    }

    public boolean isHeadMember() {
        final Address localAddress = getLocalAddress();
        if (localAddress != null) {
            final List<Address> clusterMembers = getClusterMembers();
            return !clusterMembers.isEmpty() && clusterMembers.get(0).equals(localAddress);
        } else {
            return true;
        }
    }

    public boolean isSingleMember() {
        final Address localAddress = getLocalAddress();
        if (localAddress != null) {
            final List<Address> clusterMembers = getClusterMembers();
            return clusterMembers.size() == 1;
        } else {
            return true;
        }
    }

    /**
	 * Method handle a change on the cluster members set
	 * @param event
	 */
    @ViewChanged
    public synchronized void onViewChangeEvent(ViewChangedEvent event) {
        if (logger.isDebugEnabled()) {
            logger.debug("onViewChangeEvent : pre[" + event.isPre() + "] : event local address[" + event.getCache().getLocalAddress() + "]");
        }
        final List<Address> oldView = currentView;
        currentView = new ArrayList<Address>(event.getNewView().getMembers());
        final Address localAddress = getLocalAddress();
        if (oldView != null) {
            final Cache jbossCache = mobicentsCache.getJBossCache();
            final Configuration config = jbossCache.getConfiguration();
            final boolean isBuddyReplicationEnabled = config.getBuddyReplicationConfig() != null && config.getBuddyReplicationConfig().isEnabled();
            Runnable runnable = new Runnable() {

                public void run() {
                    for (Address oldMember : oldView) {
                        if (!currentView.contains(oldMember)) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("onViewChangeEvent : processing lost member " + oldMember);
                            }
                            for (FailOverListener localListener : failOverListeners) {
                                ClientLocalListenerElector localListenerElector = localListener.getElector();
                                if (localListenerElector != null && !isBuddyReplicationEnabled) {
                                    performTakeOver(localListener, oldMember, localAddress, true, isBuddyReplicationEnabled);
                                } else {
                                    List<Address> electionView = getElectionView(oldMember);
                                    if (electionView != null && elector.elect(electionView).equals(localAddress)) {
                                        performTakeOver(localListener, oldMember, localAddress, false, isBuddyReplicationEnabled);
                                    }
                                    cleanAfterTakeOver(localListener, oldMember);
                                }
                            }
                        }
                    }
                }
            };
            Thread t = new Thread(runnable);
            t.start();
        }
    }

    @BuddyGroupChanged
    public void onBuddyGroupChangedEvent(BuddyGroupChangedEvent event) {
        Node root = event.getCache().getRoot();
        root.put(BUDDIES_STORE, event.getBuddyGroup().getBuddies());
    }

    /**
	 * 
	 */
    @SuppressWarnings("unchecked")
    private void performTakeOver(FailOverListener localListener, Address lostMember, Address localAddress, boolean useLocalListenerElector, boolean isBuddyReplicationEnabled) {
        if (logger.isDebugEnabled()) {
            logger.debug("onViewChangeEvent : " + localAddress + " failing over lost member " + lostMember + ", useLocalListenerElector=" + useLocalListenerElector + ", isBuddyReplicationEnabled=" + isBuddyReplicationEnabled);
        }
        final Cache jbossCache = mobicentsCache.getJBossCache();
        final Fqn rootFqnOfChanges = localListener.getBaseFqn();
        boolean createdTx = false;
        boolean doRollback = true;
        try {
            if (txMgr != null && txMgr.getTransaction() == null) {
                txMgr.begin();
                createdTx = true;
            }
            if (isBuddyReplicationEnabled) {
                String fqn = getBuddyBackupFqn(lostMember) + localListener.getBaseFqn();
                Node buddyGroupRootNode = jbossCache.getNode(Fqn.fromString(fqn));
                if (buddyGroupRootNode != null) {
                    Set<Node> children = buddyGroupRootNode.getChildren();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Fqn : " + fqn + " : children " + children);
                    }
                    for (Node child : children) {
                        Fqn childFqn = Fqn.fromRelativeElements(localListener.getBaseFqn(), child.getFqn().getLastElement());
                        if (logger.isDebugEnabled()) {
                            logger.debug("forcing data gravitation on following child fqn " + childFqn);
                        }
                        jbossCache.getInvocationContext().getOptionOverrides().setForceDataGravitation(true);
                        Node n = jbossCache.getNode(childFqn);
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Fqn : " + fqn + " : doesn't return any node, this node  " + localAddress + "might not be a buddy group of the failed node " + lostMember);
                    }
                }
            }
            if (createdTx) {
                txMgr.commit();
                createdTx = false;
            }
            if (txMgr != null && txMgr.getTransaction() == null) {
                txMgr.begin();
                createdTx = true;
            }
            localListener.failOverClusterMember(lostMember);
            Set<Object> children = jbossCache.getChildrenNames(rootFqnOfChanges);
            for (Object childName : children) {
                final ClusteredCacheData clusteredCacheData = new ClusteredCacheData(Fqn.fromRelativeElements(rootFqnOfChanges, childName), this);
                if (clusteredCacheData.exists()) {
                    Address address = clusteredCacheData.getClusterNodeAddress();
                    if (address != null && address.equals(lostMember)) {
                        if (!isBuddyReplicationEnabled && useLocalListenerElector) {
                            if (!localAddress.equals(localListener.getElector().elect(currentView, clusteredCacheData))) {
                                continue;
                            }
                        }
                        localListener.wonOwnership(clusteredCacheData);
                        clusteredCacheData.setClusterNodeAddress(localAddress);
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug(" Attempt to index: " + Fqn.fromRelativeElements(rootFqnOfChanges, childName) + " failed, node does not exist.");
                    }
                }
            }
            doRollback = false;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (createdTx) {
                try {
                    if (!doRollback) {
                        txMgr.commit();
                    } else {
                        txMgr.rollback();
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    @NodeRemoved
    public void onNodeRemovedEvent(NodeRemovedEvent event) {
        if (!event.isOriginLocal() && !event.isPre()) {
            final DataRemovalListener dataRemovalListener = dataRemovalListeners.get(event.getFqn().getParent());
            if (dataRemovalListener != null) {
                dataRemovalListener.dataRemoved(event.getFqn());
            }
        }
    }

    private List<Address> getElectionView(Address deadMember) {
        final Cache jbossCache = mobicentsCache.getJBossCache();
        final Configuration config = jbossCache.getConfiguration();
        final boolean isBuddyReplicationEnabled = config.getBuddyReplicationConfig() != null && config.getBuddyReplicationConfig().isEnabled();
        if (isBuddyReplicationEnabled) {
            boolean createdTx = false;
            boolean doRollback = true;
            try {
                if (txMgr != null && txMgr.getTransaction() == null) {
                    txMgr.begin();
                    createdTx = true;
                }
                String fqnBackupRoot = getBuddyBackupFqn(deadMember);
                Node backupRoot = jbossCache.getNode(fqnBackupRoot);
                if (backupRoot != null) {
                    List<Address> buddies = (List<Address>) backupRoot.get(BUDDIES_STORE);
                    if (buddies == null) {
                        buddies = new ArrayList<Address>();
                        buddies.add(config.getRuntimeConfig().getChannel().getLocalAddress());
                    }
                    return buddies;
                } else {
                    return null;
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                if (createdTx) {
                    try {
                        if (!doRollback) {
                            txMgr.commit();
                        } else {
                            txMgr.rollback();
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
            return null;
        } else {
            return currentView;
        }
    }

    private String getBuddyBackupFqn(Address owner) {
        String lostMemberFqnizied = owner.toString().replace(":", "_");
        String fqn = BUDDY_BACKUP_FQN_ROOT + lostMemberFqnizied;
        return fqn;
    }

    private void cleanAfterTakeOver(FailOverListener localListener, Address deadMember) {
        final Cache jbossCache = mobicentsCache.getJBossCache();
        final Configuration config = jbossCache.getConfiguration();
        final boolean isBuddyReplicationEnabled = config.getBuddyReplicationConfig() != null && config.getBuddyReplicationConfig().isEnabled();
        if (isBuddyReplicationEnabled) {
            String fqn = getBuddyBackupFqn(deadMember) + localListener.getBaseFqn();
            jbossCache.getInvocationContext().getOptionOverrides().setCacheModeLocal(true);
            jbossCache.removeNode(Fqn.fromString(fqn));
            BuddyGroup bg = config.getRuntimeConfig().getBuddyGroup();
            if (bg != null && bg.getBuddies().size() == 1 && bg.getBuddies().contains(deadMember)) {
                jbossCache.getRoot().remove(BUDDIES_STORE);
            }
        }
    }

    public boolean addFailOverListener(FailOverListener localListener) {
        if (logger.isDebugEnabled()) {
            logger.debug("Adding local listener " + localListener);
        }
        for (FailOverListener failOverListener : failOverListeners) {
            if (failOverListener.getBaseFqn().equals(localListener.getBaseFqn())) {
                return false;
            }
        }
        return failOverListeners.add(localListener);
    }

    public boolean removeFailOverListener(FailOverListener localListener) {
        if (logger.isDebugEnabled()) {
            logger.debug("Removing local listener " + localListener);
        }
        return failOverListeners.remove(localListener);
    }

    public boolean addDataRemovalListener(DataRemovalListener listener) {
        return dataRemovalListeners.putIfAbsent(listener.getBaseFqn(), listener) == null;
    }

    public boolean removeDataRemovalListener(DataRemovalListener listener) {
        return dataRemovalListeners.remove(listener.getBaseFqn()) != null;
    }

    public MobicentsCache getMobicentsCache() {
        return mobicentsCache;
    }

    public ClusteredCacheDataIndexingHandler getClusteredCacheDataIndexingHandler() {
        return clusteredCacheDataIndexingHandler;
    }

    @Override
    public void startCluster() {
        synchronized (this) {
            if (started) {
                throw new IllegalStateException("cluster already started");
            }
            mobicentsCache.startCache();
            final Cache<?, ?> cache = mobicentsCache.getJBossCache();
            if (!cache.getConfiguration().getCacheMode().equals(CacheMode.LOCAL)) {
                currentView = new ArrayList<Address>(cache.getConfiguration().getRuntimeConfig().getChannel().getView().getMembers());
                cache.addCacheListener(this);
                Configuration conf = cache.getConfiguration();
                if (conf.getBuddyReplicationConfig() != null && conf.getBuddyReplicationConfig().isEnabled()) {
                    if (conf.getRuntimeConfig().getBuddyGroup() != null) {
                        Node root = cache.getRoot();
                        root.put(BUDDIES_STORE, conf.getRuntimeConfig().getBuddyGroup().getBuddies());
                    }
                }
            }
            started = true;
        }
    }

    @Override
    public boolean isStarted() {
        synchronized (this) {
            return started;
        }
    }

    @Override
    public void stopCluster() {
        synchronized (this) {
            if (!started) {
                throw new IllegalStateException("cluster already started");
            }
            mobicentsCache.stopCache();
            started = false;
        }
    }
}
