package ch.ethz.dcg.spamato.peerato.tracker;

import java.util.*;
import ch.ethz.dcg.spamato.peerato.common.msg.data.*;

/**
 * Strategy to create a random graph of peers.
 * A new peer gets a random set of neighbors from the existing graph.
 * 
 * @author Michelle Ackermann
 */
public class RandomSelector extends NeighborSelectionStrategy {

    private Random random = new Random();

    private Map<Hash, Map<InetSocketAddressInfo, Peer>> fileIdToPeerMap = new HashMap<Hash, Map<InetSocketAddressInfo, Peer>>();

    private Map<InetSocketAddressInfo, Peer> addressToPeer = new HashMap<InetSocketAddressInfo, Peer>();

    private int peerListAnswerConst;

    private int peerTimeoutMillis;

    public RandomSelector() {
        super();
        peerListAnswerConst = getSettings().getPeerListAnswerConst();
        peerTimeoutMillis = getSettings().getPeerTimeoutMillis();
    }

    /**
	 * Gets the peer with the given address and updates the peer's timestamp.
	 * If the peer does not exist, a new one is created and put to the peer list.
	 */
    public Peer getPeer(InetSocketAddressInfo address) {
        Peer peer;
        peer = (Peer) addressToPeer.get(address);
        if (peer == null) {
            peer = new Peer(address);
            addressToPeer.put(address, peer);
        }
        return peer;
    }

    public void removeOldData() {
        removeOldPeers();
        removeOldFiles();
    }

    /**
	 * Removes old peers from all data structures.
	 */
    private void removeOldPeers() {
        Iterator<Peer> iter = addressToPeer.values().iterator();
        boolean hasOldPeers = true;
        while (hasOldPeers && iter.hasNext()) {
            Peer client = (Peer) iter.next();
            long difference = System.currentTimeMillis() - client.getTimestamp();
            if (difference >= peerTimeoutMillis) {
                client.delete();
                iter.remove();
            } else {
                hasOldPeers = false;
            }
        }
    }

    /**
	 * Removes all files that are not shared by any peers.
	 */
    private void removeOldFiles() {
        Iterator<Hash> iter = fileIdToPeerMap.keySet().iterator();
        while (iter.hasNext()) {
            Hash fileId = (Hash) iter.next();
            Map<InetSocketAddressInfo, Peer> peerMap = (Map) fileIdToPeerMap.get(fileId);
            if (peerMap.size() == 0) {
                iter.remove();
            }
        }
    }

    public List<InetSocketAddressInfo> getNeighbors(Peer peer, SharedFileInfo[] fileInfos) {
        List<InetSocketAddressInfo> answerList = new ArrayList<InetSocketAddressInfo>();
        for (int i = 0; i < fileInfos.length; i++) {
            SharedFileInfo fileInfo = fileInfos[i];
            Hash fileId = fileInfo.getFileId();
            boolean complete = fileInfo.complete();
            int numNeighbors = fileInfo.getNumNeighbors();
            if (!complete) {
                List<InetSocketAddressInfo> fileList = getPeerList(fileId, peer, numNeighbors);
                answerList.addAll(fileList);
            }
        }
        return answerList;
    }

    /**
	 * Gets a random set of peers sharing the file with the given <code>fileId</code>.
	 * @return list containing contact information to peers sharing the file
	 */
    private List<InetSocketAddressInfo> getPeerList(Hash fileId, Peer peer, int numNeighbors) {
        Map<InetSocketAddressInfo, Peer> peerMap = fileIdToPeerMap.get(fileId);
        int numSharers = peerMap.size();
        int missingNeighbors = ((int) Math.ceil(Math.log(numSharers))) + peerListAnswerConst - numNeighbors;
        List<InetSocketAddressInfo> peerListAnswer = new ArrayList<InetSocketAddressInfo>();
        if (peerMap == null) {
            return peerListAnswer;
        }
        Object[] peerList = peerMap.values().toArray();
        int lastPeer = peerList.length - 1;
        while (peerListAnswer.size() < missingNeighbors && lastPeer >= 0) {
            int randomPeer = random.nextInt(lastPeer + 1);
            Peer neighbor = (Peer) peerList[randomPeer];
            InetSocketAddressInfo address = neighbor.getAddress();
            if (!peer.neighbor(neighbor)) {
                peer.addNeighbor(neighbor);
                neighbor.addNeighbor(peer);
                peerListAnswer.add(neighbor.getAddress());
            }
            peerList[randomPeer] = peerList[lastPeer];
            peerList[lastPeer] = neighbor;
            lastPeer--;
        }
        return peerListAnswer;
    }

    /**
	 * Removes the peer from the peer list of the file with the specified <code>fileId</code>.
	 * This method is called when the peer is being deleted or does not share the file anymore,
	 * or before adding a peer to a file to ensure the client is inserted at the end of the list. 
	 */
    public void removePeerFromFile(Peer peer, Hash fileId) {
        Map<InetSocketAddressInfo, Peer> peerMap = fileIdToPeerMap.get(fileId);
        if (peerMap != null) {
            peerMap.remove(peer.getAddress());
        }
    }

    /**
	 * Adds the client to the peer list of the file with the specified <code>fileId</code>.
	 * If the file does not exist, a new peer list is created for the file.
	 * <code>removePeerFromFile</code> has to be called first to ensure
	 * the client is inserted at the end of the peer list.
	 */
    public void addPeerToFile(Peer peer, Hash fileId) {
        if (allowedFiles == null || allowedFiles.contains(fileId)) {
            Map<InetSocketAddressInfo, Peer> peerMap = fileIdToPeerMap.get(fileId);
            if (peerMap == null) {
                peerMap = new LinkedHashMap<InetSocketAddressInfo, Peer>();
                fileIdToPeerMap.put(fileId, peerMap);
            }
            peerMap.put(peer.getAddress(), peer);
            getLogger().info(peer.getAddress() + " registred for file " + fileId);
        } else {
            getLogger().warning(peer.getAddress() + " tried to register for file " + fileId + " , but is not allowed.");
        }
    }
}
