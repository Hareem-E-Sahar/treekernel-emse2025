package pcap;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import org.jnetpcap.Pcap;
import org.jnetpcap.nio.JBuffer;
import org.jnetpcap.nio.JMemory.Type;
import org.jnetpcap.packet.JMemoryPacket;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.protocol.network.Ip4;

/**
 * This is a demonstration application for reassembling IP fragments.
 * The application is intended only for show purposes on how jNetPcap
 * API can be used.
 * <p>
 * This example application captures IP packets, makes sure they are
 * IPs and creates special packets that are ip only. We will use
 * JMemoryPacket which nicely allows us to construct a new custom
 * packet. Our new packets don't care about the lower OSI layers since
 * that information is irrelavent for Ip reassembly and for the user
 * as well. If we receive a packet that is not fragmented we simply
 * pass it through, no sense in doing anything special with it.
 * </p>
 * 
 * @author Mark Bednarczyk
 * @author Sly Technologies, Inc.
 */
public class IpReassemblyExample implements PcapPacketHandler<Object> {

    /**
   * Our custom interface that allows listeners to get our special
   * reassembled IP packets and also provide them with the actual
   * reassembled buffer.
   * 
   * @author Mark Bednarczyk
   * @author Sly Technologies, Inc.
   * @param <T>
   */
    public interface IpReassemblyBufferHandler {

        public void nextIpDatagram(IpReassemblyBuffer buffer);
    }

    /**
   * A special buffer used for reassembling the original IP datagram.
   * The reassembled buffer contains an IP header upfront and it
   * suitable for peering with a packet and then decoding.
   * 
   * <pre>
   * +-----------+-----------+-----------+--&tilde;&tilde;&tilde;&tilde;--+
   * | Ip Header | Ip frag 1 | Ip frag 2 | etc... |
   * +-----------+-----------+-----------+--&tilde;&tilde;&tilde;&tilde;--+
   * </pre>
   * 
   * The header comes from the first segment that was seen part of
   * this IP datagram. The original header is used as a template and
   * then several fields are reset to reflect the state of reassembled
   * packet. The fields modified are:
   * <ul>
   * <li> Ip4.flags</li>
   * <li> Ip4.length</li>
   * <li> Ip4.hlen</li>
   * <li> Ip4.offset</li>
   * </ul>
   * 
   * @author Mark Bednarczyk
   * @author Sly Technologies, Inc.
   */
    public static class IpReassemblyBuffer extends JBuffer implements Comparable<IpReassemblyBuffer> {

        /**
     * The IP header found at the beggining of this buffer
     */
        private Ip4 header = new Ip4();

        /**
     * Total length of the reassembled IP fragments including the ip
     * header
     */
        private int ipDatagramLength = -1;

        /**
     * Keeps track of how many bytes have been copied into this
     * buffer. When bytesCopiedIntoBuffer == ipDatagramLength, where
     * bytesCopiedIntoBuffer keeps track of the total size of the
     * original datagram in its entirety (including Ip4 header), then
     * the reassembly is complete.
     */
        private int bytesCopiedIntoBuffer = 20;

        /**
     * Offset where the Ip payload begins in the reassembled IP
     * datagram. Always constant since our IP header is constant as
     * well.
     */
        private final int start = 20;

        /**
     * Timestamp when this buffer is officially timedout
     */
        private final long timeout;

        /**
     * A hash of Ip4.source, Ip4.destination, Ip4.id, Ip4.type
     */
        private final int hash;

        /**
     * Override the default hashcode with our special Ip4 based one
     */
        @Override
        public int hashCode() {
            return this.hash;
        }

        /**
     * Creates a new buffer for IP fragment reassebly. The buffer
     * appends an Ip4 header to the front of the buffer. The supplied
     * ip header is only used as a template and a copy is made. This
     * allows the buffer to retain all the vital Ip4 information found
     * in the original Ip4 datagram before it was fragmented.
     * 
     * @param ip
     *            ip header of one of the fragments to be used as a
     *            template for the reassembled packet
     * @param size
     *            amount of memory to allocate for reassembly
     * @param timeout
     *            timestamp in millis when this buffer should be timed
     *            out
     * @param hash
     *            special Ip4 based hash used for identifying this
     *            buffer quickly
     */
        public IpReassemblyBuffer(Ip4 ip, int size, long timeout, int hash) {
            super(size);
            this.timeout = timeout;
            this.hash = hash;
            transferFrom(ip);
        }

        /**
     * Deep copy the supplied Ip4 header to the front of the buffer.
     * Reset some ip fields to reflect the state of this buffer.
     * 
     * @param ip
     *            source Ip4 header to use as template
     */
        private void transferFrom(Ip4 ip) {
            ip.transferTo(this, 0, 20, 0);
            header.peer(this, 0, 20);
            header.hlen(5);
            header.clearFlags(Ip4.FLAG_MORE_FRAGMENTS);
            header.offset(0);
            header.checksum(0);
        }

        /**
     * Adds a IP fragment to the buffer. This fragment is also the
     * last framgent of the fragment series which carries special
     * information about the original IP datagram.
     * 
     * @param packet
     *            a packet buffer containing IP fragment data
     * @param offset
     *            offset into this buffer where the fragment data
     *            should be copied to
     * @param length
     *            the length of the fragment data
     * @param packetOffset
     *            offset into the packet buffer where fragment data
     *            begins
     */
        public void addLastSegment(JBuffer packet, int offset, int length, int packetOffset) {
            addSegment(packet, offset, length, packetOffset);
            this.ipDatagramLength = start + offset + length;
            super.setSize(this.ipDatagramLength);
            header.length(ipDatagramLength);
        }

        /**
     * Adds a IP fragment to the buffer. The fragment data is copied
     * into this buffer at specified offset from the supplied packet
     * data buffer.
     * 
     * @param packet
     *            a packet buffer containing IP fragment data
     * @param offset
     *            offset into this buffer where the fragment data
     *            should be copied to
     * @param length
     *            the length of the fragment data
     * @param packetOffset
     *            offset into the packet buffer where fragment data
     *            begins
     */
        public void addSegment(JBuffer packet, int offset, int length, int packetOffset) {
            this.bytesCopiedIntoBuffer += length;
            packet.transferTo(this, packetOffset, length, offset + start);
        }

        /**
     * For ordering buffers according to their timeout value. This is
     * specifically useful when using a PriorityQueue which will order
     * the buffers for us according to the timeout timestamp. The
     * oldest buffers are on top of the queue, while the youngest are
     * at the bottom.
     */
        public int compareTo(IpReassemblyBuffer o) {
            return (int) (o.timeout - this.timeout);
        }

        /**
     * Checks if the buffer reassembly is complete. If the number of
     * bytes copied into this buffer including the ip header up front,
     * equals the length of the original IP datagram, that means the
     * fragmentation succeeded and is complete.
     * 
     * @return true if fragmentation succeeded and completely done
     */
        public boolean isComplete() {
            return this.ipDatagramLength == this.bytesCopiedIntoBuffer;
        }

        /**
     * Compares the timeout timestamp against the current time. If
     * timeout timestamp is still in the future, then it returns
     * false. If the timestamp is in the past, then true is returned
     * and buffer is considered timedout.
     * 
     * @return true if buffer is timedout, otherwise false
     */
        public boolean isTimedout() {
            return this.timeout < System.currentTimeMillis();
        }

        /**
     * Returns the working Ip4 header instance found at the front of
     * this buffer
     * 
     * @return Ip4 header for this IP datagram
     */
        public Ip4 getIpHeader() {
            return header;
        }
    }

    /**
   * Default buffer size to allocate for reassembly. Needs to be large
   * enough to hold the Ip4 header upfront and the contetents of all
   * fragment for a single fragmented Ip4 datagram.
   */
    private static final int DEFAULT_REASSEMBLY_SIZE = 8 * 1024;

    /**
   * Our example application. Arguments are ignored. Reads 6 packets
   * from file "tests/test-ipreassembly2.pcap" and reassembles the IP
   * fragments found into a new ip-only super packet. The new packet
   * contains the Ip4 header as DLT.
   * 
   * @param args
   *            ignored
   */
    public static void main(String[] args) {
        StringBuilder errbuf = new StringBuilder();
        Pcap pcap = Pcap.openOffline("test/dump.syn.bin", errbuf);
        if (pcap == null) {
            System.err.println(errbuf.toString());
            return;
        }
        pcap.loop(1, new IpReassemblyExample(1, new IpReassemblyBufferHandler() {

            public void nextIpDatagram(IpReassemblyBuffer buffer) {
                if (buffer.isComplete() == false) {
                    System.err.println("WARNING: missing fragments");
                } else {
                    JPacket packet = new JMemoryPacket(Type.POINTER);
                    packet.peer(buffer);
                    packet.scan(Ip4.ID);
                    System.out.println(packet.toString());
                }
            }
        }), null);
    }

    /**
   * Keeps track of all IP datagrams being reassembled
   */
    private Map<Integer, IpReassemblyBuffer> buffers = new HashMap<Integer, IpReassemblyBuffer>();

    /**
   * User registered handler.
   */
    private IpReassemblyBufferHandler handler;

    /**
   * Ip4 header we use for incoming packets from libpcap
   */
    private Ip4 ip = new Ip4();

    /**
   * Amount of time in milli seconds, after which reassembly buffers
   * are timedout out of the queue.
   */
    private final long timeout;

    /**
   * Timeout queue to which all new reassembly buffers are added. The
   * queue is prioritized according to timeout timestamp of each
   * buffer.
   */
    private final Queue<IpReassemblyBuffer> timeoutQueue = new PriorityQueue<IpReassemblyBuffer>();

    /**
   * Creates a reassembly handler that reassembles all incoming IP
   * packet.
   * 
   * @param timeout
   *            sets the default amount of time in millis, before
   *            reassembly buffers are timedout
   * @param handler
   *            user supplied handler to call with reassembled buffers
   */
    public IpReassemblyExample(long timeout, IpReassemblyBufferHandler handler) {
        this.timeout = timeout;
        if (handler == null) {
            throw new NullPointerException();
        }
        this.handler = handler;
    }

    /**
   * Process an Ip4 fragment. Fragment is copied into appropriate
   * reassembly buffer
   * 
   * @param packet
   *            Ip4 fragment packet
   * @param ip
   *            our working Ip4 header already peered to the packet
   */
    private IpReassemblyBuffer bufferFragment(PcapPacket packet, Ip4 ip) {
        IpReassemblyBuffer buffer = getBuffer(ip);
        final int hlen = ip.hlen() * 4;
        final int len = ip.length() - hlen;
        final int packetOffset = ip.getOffset() + hlen;
        final int dgramOffset = ip.offset() * 8;
        buffer.addSegment(packet, dgramOffset, len, packetOffset);
        if (buffer.isComplete()) {
            if (buffers.remove(ip.hashCode()) == null) {
                System.err.println("bufferFragment(): failed to remove buffer");
                System.exit(0);
            }
            timeoutQueue.remove(buffer);
            dispatch(buffer);
        }
        return buffer;
    }

    /**
   * Process an Ip4 fragment. Fragment is copied into appropriate
   * reassembly buffer. This is also a special fragment as its the
   * last fragment of the fragmented IP datagram.
   * 
   * @param packet
   *            Ip4 fragment packet
   * @param ip
   *            our working Ip4 header already peered to the packet
   */
    private IpReassemblyBuffer bufferLastFragment(PcapPacket packet, Ip4 ip) {
        IpReassemblyBuffer buffer = getBuffer(ip);
        final int hlen = ip.hlen() * 4;
        final int len = ip.length() - hlen;
        final int packetOffset = ip.getOffset() + hlen;
        final int dgramOffset = ip.offset() * 8;
        buffer.addLastSegment(packet, dgramOffset, len, packetOffset);
        if (buffer.isComplete()) {
            if (buffers.remove(buffer.hashCode()) == null) {
                System.err.println("bufferLastFragment(): failed to remove buffer");
                System.exit(0);
            }
            timeoutQueue.remove(buffer);
            dispatch(buffer);
        }
        return buffer;
    }

    /**
   * Calls on user's handlers nextIpDatagram() callback method.
   * 
   * @param buffer
   *            reassembled buffer to send to the user's callback
   */
    private void dispatch(IpReassemblyBuffer buffer) {
        handler.nextIpDatagram(buffer);
    }

    /**
   * Retrieves a reassembly buffer for this particular Ip packet. The
   * supplied Ip4 header is used to determine if a buffer already
   * exists and if not a new one is created.
   * 
   * @param ip
   *            Ip4 header of current Ip4 fragment
   * @return a reassembly buffer used for reassembly of this Ip4
   *         datagram
   */
    private IpReassemblyBuffer getBuffer(Ip4 ip) {
        IpReassemblyBuffer buffer = buffers.get(ip.hashCode());
        if (buffer == null) {
            final long bufTimeout = System.currentTimeMillis() + this.timeout;
            buffer = new IpReassemblyBuffer(ip, DEFAULT_REASSEMBLY_SIZE, bufTimeout, ip.hashCode());
            buffers.put(ip.hashCode(), buffer);
        }
        return buffer;
    }

    /**
   * Catch incoming packets from libpcap and if they are Ip packets
   * reassemble them.
   * 
   * @param packet
   *            a temporary singleton packet received from libpcap
   * @param user
   *            user object
   */
    public void nextPacket(PcapPacket packet, Object user) {
        if (packet.hasHeader(ip)) {
            if ((ip.flags() & Ip4.FLAG_MORE_FRAGMENTS) != 0) {
                bufferFragment(packet, ip);
            } else {
                bufferLastFragment(packet, ip);
            }
            timeoutBuffers();
        }
    }

    /**
   * Check the timeout queue and timeout any buffers that are
   * timedout. The timed out buffer are dispatched, incomplete to the
   * user's handler. Buffers that are still on the queue, are
   * incomplete but have not timedout yet are ignored.
   */
    private void timeoutBuffers() {
        while (timeoutQueue.isEmpty() == false) {
            if (timeoutQueue.peek().isTimedout()) {
                dispatch(timeoutQueue.poll());
            } else {
                break;
            }
        }
    }
}
