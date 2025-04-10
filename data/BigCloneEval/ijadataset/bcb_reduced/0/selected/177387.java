package org.waveprotocol.box.server.frontend;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import org.waveprotocol.box.common.DeltaSequence;
import org.waveprotocol.wave.model.id.IdFilter;
import org.waveprotocol.wave.model.id.WaveId;
import org.waveprotocol.wave.model.id.WaveletId;
import org.waveprotocol.wave.model.id.WaveletName;
import org.waveprotocol.wave.model.operation.wave.TransformedWaveletDelta;
import org.waveprotocol.wave.model.version.HashedVersion;
import org.waveprotocol.wave.util.logging.Log;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * A client's subscription to a wave view.
 *
 * @author anorth@google.com (Alex North)
 */
final class WaveViewSubscription {

    /**
   * State of a wavelet endpoint.
   */
    private static final class WaveletChannelState {

        /**
     * Resulting versions of deltas submitted on this wavelet for which
     * the outbound delta has not yet been seen.
     */
        public final Collection<Long> submittedEndVersions = Sets.newHashSet();

        /**
     * Resulting version of the most recent outbound delta.
     */
        public HashedVersion lastVersion = null;

        /**
     * Whether a submit request is awaiting a response.
     */
        public boolean hasOutstandingSubmit = false;

        /**
     * Outbound deltas held back while a submit is in-flight.
     */
        public List<TransformedWaveletDelta> heldBackDeltas = Lists.newLinkedList();
    }

    private static final Log LOG = Log.get(WaveViewSubscription.class);

    private final WaveId waveId;

    private final IdFilter waveletIdFilter;

    private final ClientFrontend.OpenListener openListener;

    private final String channelId;

    private final ConcurrentMap<WaveletId, WaveletChannelState> channels = new MapMaker().makeComputingMap(new Function<WaveletId, WaveletChannelState>() {

        @Override
        public WaveletChannelState apply(WaveletId id) {
            return new WaveletChannelState();
        }
    });

    public WaveViewSubscription(WaveId waveId, IdFilter waveletIdFilter, String channelId, ClientFrontend.OpenListener openListener) {
        Preconditions.checkNotNull(waveId, "null wave id");
        Preconditions.checkNotNull(waveletIdFilter, "null filter");
        Preconditions.checkNotNull(openListener, "null listener");
        Preconditions.checkNotNull(channelId, "null channel id");
        this.waveId = waveId;
        this.waveletIdFilter = waveletIdFilter;
        this.channelId = channelId;
        this.openListener = openListener;
    }

    public WaveId getWaveId() {
        return waveId;
    }

    public ClientFrontend.OpenListener getOpenListener() {
        return openListener;
    }

    public String getChannelId() {
        return channelId;
    }

    /**
   * Checks whether the subscription includes a wavelet.
   */
    public boolean includes(WaveletId waveletId) {
        return IdFilter.accepts(waveletIdFilter, waveletId);
    }

    /** This client sent a submit request */
    public synchronized void submitRequest(WaveletName waveletName) {
        WaveletChannelState state = channels.get(waveletName.waveletId);
        Preconditions.checkState(!state.hasOutstandingSubmit, "Received overlapping submit requests to subscription %s", this);
        LOG.info("Submit oustandinding on channel " + channelId);
        state.hasOutstandingSubmit = true;
    }

    /**
   * A submit response for the given wavelet and version has been sent to this
   * client.
   */
    public synchronized void submitResponse(WaveletName waveletName, HashedVersion version) {
        Preconditions.checkNotNull(version, "Null delta application version");
        WaveletId waveletId = waveletName.waveletId;
        WaveletChannelState state = channels.get(waveletId);
        Preconditions.checkState(state.hasOutstandingSubmit);
        state.submittedEndVersions.add(version.getVersion());
        state.hasOutstandingSubmit = false;
        LOG.info("Submit resolved on channel " + channelId);
        List<TransformedWaveletDelta> filteredDeltas = filterOwnDeltas(state.heldBackDeltas, state);
        if (!filteredDeltas.isEmpty()) {
            sendUpdate(waveletName, filteredDeltas, null);
        }
        state.heldBackDeltas.clear();
    }

    /**
   * Sends deltas for this subscription (if appropriate).
   *
   * If the update contains a delta for a wavelet where the delta is actually
   * from this client, the delta is dropped. If there's an outstanding submit
   * request the delta is queued until the submit finishes.
   */
    public synchronized void onUpdate(WaveletName waveletName, DeltaSequence deltas) {
        Preconditions.checkArgument(!deltas.isEmpty());
        WaveletChannelState state = channels.get(waveletName.waveletId);
        checkUpdateVersion(waveletName, deltas, state);
        state.lastVersion = deltas.getEndVersion();
        if (state.hasOutstandingSubmit) {
            state.heldBackDeltas.addAll(deltas);
        } else {
            List<TransformedWaveletDelta> filteredDeltas = filterOwnDeltas(deltas, state);
            if (!filteredDeltas.isEmpty()) {
                sendUpdate(waveletName, filteredDeltas, null);
            }
        }
    }

    /**
   * Filters any deltas sent by this client from a list of received deltas.
   *
   * @param deltas received deltas
   * @param state channel state
   * @return deltas, if none are from this client, or a copy with own client's
   *         deltas removed
   */
    private List<TransformedWaveletDelta> filterOwnDeltas(List<TransformedWaveletDelta> deltas, WaveletChannelState state) {
        List<TransformedWaveletDelta> filteredDeltas = deltas;
        if (!state.submittedEndVersions.isEmpty()) {
            filteredDeltas = Lists.newArrayList();
            for (TransformedWaveletDelta delta : deltas) {
                long deltaEndVersion = delta.getResultingVersion().getVersion();
                if (!state.submittedEndVersions.remove(deltaEndVersion)) {
                    filteredDeltas.add(delta);
                }
            }
        }
        return filteredDeltas;
    }

    /**
   * Sends a commit notice for this subscription.
   */
    public synchronized void onCommit(WaveletName waveletName, HashedVersion committedVersion) {
        sendUpdate(waveletName, ImmutableList.<TransformedWaveletDelta>of(), committedVersion);
    }

    /**
   * Sends an update to the client.
   */
    private void sendUpdate(WaveletName waveletName, List<TransformedWaveletDelta> deltas, HashedVersion committedVersion) {
        openListener.onUpdate(waveletName, null, deltas, committedVersion, null, channelId);
    }

    /**
   * Checks the update targets the next expected version.
   */
    private void checkUpdateVersion(WaveletName waveletName, DeltaSequence deltas, WaveletChannelState state) {
        if (state.lastVersion != null) {
            long expectedVersion = state.lastVersion.getVersion();
            long targetVersion = deltas.getStartVersion();
            Preconditions.checkState(targetVersion == expectedVersion, "Subscription expected delta for %s targeting %s, was %s", waveletName, expectedVersion, targetVersion);
        }
    }

    @Override
    public String toString() {
        return "[WaveViewSubscription wave: " + waveId + ", channel: " + channelId + "]";
    }
}
