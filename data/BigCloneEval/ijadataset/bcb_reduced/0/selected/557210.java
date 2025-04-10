package org.hypergraphdb.app.dataflow;

import static org.hypergraphdb.peer.Messages.CONTENT;
import static org.hypergraphdb.peer.Messages.createMessage;
import static org.hypergraphdb.peer.Structs.combine;
import static org.hypergraphdb.peer.Structs.list;
import static org.hypergraphdb.peer.Structs.struct;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.Message;
import org.hypergraphdb.peer.Performative;
import org.hypergraphdb.peer.workflow.WorkflowState;

/**
 * 
 * <p>
 * Manages mapping b/w channel and connected peers. Implements channel networking
 * in cooperation with the NetworkPeerActivity associated with the running network.
 * </p>
 *
 * @author Borislav Iordanov
 *
 */
public class ChannelPeer<ContextType> extends DefaultChannelManager {

    Map<String, Set<HGPeerIdentity>> channelPeers = Collections.synchronizedMap(new HashMap<String, Set<HGPeerIdentity>>());

    Set<HGPeerIdentity> workPeers = Collections.synchronizedSet(new HashSet<HGPeerIdentity>());

    private HyperGraphPeer thisPeer;

    private JobDataFlow<ContextType> network;

    private NetworkPeerActivity activity;

    public ChannelPeer(HyperGraphPeer thisPeer, JobDataFlow<ContextType> network) {
        assert network != null : new NullPointerException("null JobDataFlow network");
        assert thisPeer != null : new NullPointerException("null HyperGraphPeer");
        this.thisPeer = thisPeer;
        this.network = network;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void initNetworking() {
        for (Channel<?> ch : network.getChannels()) {
            if (!JobDataFlow.isChannelJobSpecific(ch.getId())) {
                network.addChannel(new DistChannel(ch.getId(), ch.getEOS(), ch.getCapacity()));
            }
        }
        HGHandle netHandle = thisPeer.getGraph().getHandle(network);
        assert netHandle != null : new RuntimeException("Network " + network + " not in HyperGraph");
        this.activity = new NetworkPeerActivity(thisPeer, netHandle);
        thisPeer.getActivityManager().initiateActivity(activity);
    }

    public void shutdownNetworking() {
        if (!activity.getState().isFinished()) activity.getState().assign(WorkflowState.Completed);
    }

    public class DistJobChannel<D> extends JobChannel<D> implements DistributedChannel<D> {

        public DistJobChannel(int expectedOutputCount, String id, Job job, D EOF, int capacity) {
            super(expectedOutputCount, id, job, EOF, capacity);
        }

        public void putLocal(D x) throws InterruptedException {
            super.put(x);
        }

        public void put(D x) throws InterruptedException {
            lock.lock();
            try {
                putLocal(x);
                for (HGPeerIdentity target : getChannelPeers(getId())) {
                    HGHandle jobId = getJob().getHandle();
                    if (jobId == null) throw new RuntimeException("Attempt to put data into a channel with no associated peer task id.");
                    Message msg = createMessage(Performative.InformRef, activity);
                    try {
                        if (!thisPeer.getPeerInterface().send(thisPeer.getNetworkTarget(target), combine(msg, struct(CONTENT, struct("datum", activity.toJson(x), "channel", list(jobId, getId()))))).get()) throw new PeerFailedException(target, " while sending data on channel " + getId());
                    } catch (ExecutionException ex) {
                        throw new PeerFailedException(target, " while sending data on channel " + getId(), ex);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException("Don't know what to do with thread interrupts yet.");
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }

    public class DistChannel<D> extends Channel<D> implements DistributedChannel<D> {

        public DistChannel(String id, D EOF, int capacity) {
            super(id, EOF, capacity);
        }

        public void putLocal(D x) throws InterruptedException {
            super.put(x);
        }

        public void put(D x) throws InterruptedException {
            lock.lock();
            try {
                putLocal(x);
                for (HGPeerIdentity target : getChannelPeers(getId())) {
                    Message msg = createMessage(Performative.InformRef, activity);
                    try {
                        if (!thisPeer.getPeerInterface().send(thisPeer.getNetworkTarget(target), combine(msg, struct(CONTENT, struct("datum", activity.toJson(x), "channel", getId())))).get()) throw new PeerFailedException(target, " while sending data on channel " + getId());
                    } catch (ExecutionException ex) {
                        throw new PeerFailedException(target, " while sending data on channel " + getId(), ex);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException("Don't know what to do with thread interrupts yet.");
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    protected <D> JobChannel<D> createNewChannel(JobDataFlow<?> network, Channel<D> logicalChannel, Job job) {
        return new DistJobChannel<D>(network.getReaders(logicalChannel).size(), logicalChannel.getId(), job, logicalChannel.getEOS(), logicalChannel.getCapacity());
    }

    public void addChannelPeer(String channelId, HGPeerIdentity peer) {
        Set<HGPeerIdentity> S = channelPeers.get(channelId);
        if (S == null) {
            S = new HashSet<HGPeerIdentity>();
            channelPeers.put(channelId, S);
        }
        S.add(peer);
    }

    public Set<HGPeerIdentity> getChannelPeers(String channelId) {
        Set<HGPeerIdentity> S = channelPeers.get(channelId);
        if (S == null) return new HashSet<HGPeerIdentity>(); else return S;
    }
}
