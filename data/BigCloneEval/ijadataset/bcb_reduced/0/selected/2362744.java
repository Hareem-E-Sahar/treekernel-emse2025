package org.jnetpcap.packet;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.jnetpcap.JCaptureHeader;
import org.jnetpcap.nio.JBuffer;
import org.jnetpcap.nio.JMemory;
import org.jnetpcap.nio.JMemoryPool;
import org.jnetpcap.nio.JStruct;
import org.jnetpcap.packet.analysis.AnalysisUtils;
import org.jnetpcap.packet.analysis.JAnalysis;
import org.jnetpcap.packet.format.FormatUtils;
import org.jnetpcap.packet.format.JFormatter;
import org.jnetpcap.packet.format.TextFormatter;

/**
 * A native packet buffer object. This class references both packet data buffer
 * and decoded native packet structure. JPacket class is a subclass of a more
 * general JBuffer providing full access to raw packet buffer data. It also has
 * a reference to JPacket.State object which is peered, associated with, a
 * native packet state structure generated by the packet scanner, the JScanner.
 * <p>
 * The packet interface provides numerous methods for accessing the decoded
 * information. To check if any particular header is found within the packet's
 * data buffer at the time the packet was scanned, the user can use
 * {@link #hasHeader} methods. The method returns true if a particular header is
 * found within the packet data buffer, otherwise false. A convenience method
 * {@link #hasHeader(JHeader)} exists that performs both an existance check and
 * initializes the header instace supplied to point at the header within the
 * packet.
 * </p>
 * <p>
 * There are also numerous peer and deep copy methods. The peering methods do
 * not copy any buffers but simply re-orient the pointers to point at the source
 * peer structures to destination peer. The deep copy methods do copy physical
 * data out of buffers and entire structures using native copy functions, not in
 * java space.
 * </p>
 * 
 * @author Mark Bednarczyk
 * @author Sly Technologies, Inc.
 */
public abstract class JPacket extends JBuffer implements JHeaderAccessor {

    /**
	 * Class maintains the decoded packet state. The class is peered with
	 * <code>struct packet_state_t</code>
	 * 
	 * <pre>
	 * typedef struct packet_state_t {
	 * 	uint64_t pkt_header_map; // bit map of presence of headers
	 * 	char *pkt_data; // packet data buffer
	 * 	int32_t pkt_header_count; // total number of headers found
	 * 
	 * 	// Keep track of how many instances of each header we have
	 * 	uint8_t pkt_instance_counts[MAX_ID_COUNT];
	 * 	header_t pkt_headers[]; // One per header + 1 more for payload
	 * } packet_t;
	 * </pre>
	 * 
	 * and <code>struct header_t</code>
	 * 
	 * <pre>
	 * typedef struct header_t {
	 * 	int32_t hdr_id; // header ID
	 * 	uint32_t hdr_offset; // offset into the packet_t-&gt;data buffer
	 * 	int32_t hdr_length; // length of the header in packet_t-&gt;data buffer
	 * } header_t;
	 * 
	 * </pre>
	 * 
	 * <p>
	 * The methods in this <code>State</code> provide 3 sets of functions.
	 * Looking up global state of the packet found in packet_state_t structure,
	 * looking up header information in <code>struct header_t</code> by header
	 * ID retrieved from JRegistry and instance numbers, looking up header
	 * information by direct indexes into native maps and arrays. Instance numbers
	 * specify which instance of the header, if more than 1 exists in a packet.
	 * For example if there is a packet with 2 Ip4 headers such as
	 * 
	 * <pre>
	 * Ethernet-&gt;Ip4-&gt;Snmp-&gt;Ip4 
	 * or 
	 * Ethernet-&gt;Ip4-&gt;Ip4 (IP tunneled IP)
	 * </pre>
	 * 
	 * the first Ip4 header is instance 0 and the second Ip4 header is instance 2.
	 * You can use the method {@link #getInstanceCount(int)} to learn how many
	 * instance headers exists. That information is stored in the packet_state_t
	 * structure for efficiency.
	 * </p>
	 * 
	 * @author Mark Bednarczyk
	 * @author Sly Technologies, Inc.
	 */
    public static class State extends JStruct {

        /**
		 * Flag which is set when the packet that was decoded was truncated and not
		 * the original length seen on the wire.
		 */
        public static final int FLAG_TRUNCATED = 0x01;

        public static final String STRUCT_NAME = "packet_state_t";

        /**
		 * @param count
		 *          header counter, number of headers to calaculate in
		 * @return size in bytes
		 */
        public static native int sizeof(int count);

        private final JFlowKey flowKey = new JFlowKey();

        /**
		 * @param size
		 */
        public State(int size) {
            super(STRUCT_NAME, size);
        }

        public State(Type type) {
            super(STRUCT_NAME, type);
        }

        public void cleanup() {
            super.cleanup();
        }

        public int findHeaderIndex(int id) {
            return findHeaderIndex(id, 0);
        }

        public native int findHeaderIndex(int id, int instance);

        /**
		 * @param index
		 *          TODO: remove index, its no longer used natively
		 * @return
		 */
        public native long get64BitHeaderMap(int index);

        /**
		 * Retrieves the analysis object that is attached to this packet.
		 * 
		 * @return an attached analysis based object or null if not set
		 */
        public native JAnalysis getAnalysis();

        /**
		 * Retrieves the analysis object that is attached to the header at index.
		 * This method provides a way to retrive analysis object directly from a
		 * header without having to have a reference to the header, only its index
		 * in the packet state table of headers.
		 * 
		 * @param index
		 *          index of the header within the packet state structure
		 * @return attached analysis object or null if none are attached
		 */
        public native JAnalysis getAnalysis(int index);

        /**
		 * Gets the 32-bit counter that contains packet's flags in packet_state_t
		 * structure
		 * 
		 * @return bit flags for this packet
		 */
        public native int getFlags();

        public JFlowKey getFlowKey() {
            return this.flowKey;
        }

        /**
		 * The frame number is assigned by the scanner at the time of the scan.
		 * Therefore number is only unique within the same scanner.
		 * 
		 * @return frame number
		 */
        public native long getFrameNumber();

        public native int getHeaderCount();

        public native int getHeaderIdByIndex(int index);

        /**
		 * A convenience method that gets the length in the packet buffer of the
		 * header at specified index. Typically header information is retrieved
		 * using JHeader.State structure which can access all available header
		 * information.
		 * 
		 * @param index
		 *          header index
		 * @return length in bytes of the header
		 */
        public native int getHeaderLengthByIndex(int index);

        /**
		 * A convenience method that gets the offset into the packet buffer of the
		 * header at specified index. Typically header information is retrieved
		 * using JHeader.State structure which can access all available header
		 * information.
		 * 
		 * @param index
		 *          header index
		 * @return offset in bytes of the start of the header
		 */
        public native int getHeaderOffsetByIndex(int index);

        public native int getInstanceCount(int id);

        /**
		 * Gets the packet's wire length
		 * 
		 * @return original length of the packet
		 */
        public native int getWirelen();

        public int peer(ByteBuffer peer) throws PeeringException {
            int r = super.peer(peer);
            flowKey.peer(this);
            return r;
        }

        public int peer(JBuffer peer) {
            int r = super.peer(peer, 0, size());
            flowKey.peer(this);
            return r;
        }

        public int peer(JBuffer peer, int offset, int length) throws IndexOutOfBoundsException {
            int r = super.peer(peer, offset, length);
            flowKey.peer(this);
            return r;
        }

        /**
		 * @param memory
		 * @param offset
		 */
        public int peer(JMemory memory, int offset) {
            int r = super.peer(memory, offset, size());
            flowKey.peer(this);
            return r;
        }

        public int peer(JMemoryPool.Block peer, int offset, int length) throws IndexOutOfBoundsException {
            int r = super.peer(peer, offset, length);
            flowKey.peer(this);
            return r;
        }

        public int peer(State peer) {
            int r = super.peer(peer, 0, size());
            flowKey.peer(this);
            return r;
        }

        public native int peerHeaderById(int id, int instance, JHeader.State dst);

        public native int peerHeaderByIndex(int index, JHeader.State dst) throws IndexOutOfBoundsException;

        /**
		 * Peers this packet's state to buffer
		 * 
		 * @param buffer
		 *          source buffer
		 * @param offset
		 *          offset into the buffer
		 * @return number of bytes peered
		 */
        public int peerTo(JBuffer buffer, int offset) {
            int r = super.peer(buffer, offset, size());
            flowKey.peer(this);
            return r;
        }

        /**
		 * Peers this packet's state to buffer
		 * 
		 * @param buffer
		 *          source buffer
		 * @param offset
		 *          offset into the buffer
		 * @param size
		 *          specifies the number of bytes to peer
		 * @return number of bytes peered
		 */
        public int peerTo(JBuffer buffer, int offset, int size) {
            int r = super.peer(buffer, offset, size);
            flowKey.peer(this);
            return r;
        }

        /**
		 * @param state
		 * @param offset
		 */
        public int peerTo(State state, int offset) {
            int r = super.peer(state, offset, state.size());
            flowKey.peer(this);
            return r;
        }

        /**
		 * Sets analysis information for header at index
		 * 
		 * @param index
		 *          header index
		 * @param analysis
		 *          object to set
		 */
        public native void setAnalysis(int index, JAnalysis analysis);

        /**
		 * Sets the analysis object for this packet.
		 * 
		 * @param analysis
		 */
        public native void setAnalysis(JAnalysis analysis);

        /**
		 * Sets the 32-bit counter with packet flags
		 * 
		 * @param flags
		 *          bit flags for this packet
		 */
        public native void setFlags(int flags);

        /**
		 * Sets the packet's wire length.
		 * 
		 * @param length
		 *          the original length of the packet before truncation
		 */
        public native void setWirelen(int length);

        /**
		 * Dump packet_state_t structure and its sub structures to textual debug
		 * output
		 * <p>
		 * Explanation:
		 * 
		 * <pre>
		 * sizeof(packet_state_t)=16
		 * sizeof(header_t)=8 and *4=32
		 * pkt_header_map=0x1007         // bitmap, each bit represets a header
		 * pkt_header_count=4            // how many header found
		 * // per header information (4 header found in this example)
		 * pkt_headers[0]=&lt;hdr_id=1  ETHERNET ,hdr_offset=0  ,hdr_length=14&gt;
		 * pkt_headers[1]=&lt;hdr_id=2  IP4      ,hdr_offset=14 ,hdr_length=60&gt;
		 * pkt_headers[2]=&lt;hdr_id=12 ICMP     ,hdr_offset=74 ,hdr_length=2&gt;
		 * pkt_headers[3]=&lt;hdr_id=0  PAYLOAD  ,hdr_offset=76 ,hdr_length=62&gt;
		 * 
		 * // hdr_id = numerical ID of the header, asssigned by JRegistry
		 * // hdr_offset = offset in bytes into the packet buffer
		 * // hdr_length = length in bytes of the entire header
		 * </pre>
		 * 
		 * Packet state is made up of 2 structures: packet_stat_t and an array of
		 * header_t, one per header. Total size in bytes is all of the header
		 * structures combined, that is 16 + 32 = 48 bytes. Each bit in the
		 * header_map represents the presence of that header type. The index of the
		 * bit is the numerical ID of the header. If 2 headers of the same type are
		 * present, they are both represented by a single bit in the bitmap. This
		 * way the implementation JPacket.hasHeader(int id) is a simple bit
		 * operation to test if the header is present or not.
		 * </p>
		 * 
		 * @return multiline string containing dump of the entire structure
		 */
        public String toDebugString() {
            return super.toDebugString() + "\n" + toDebugStringJPacketState();
        }

        private native String toDebugStringJPacketState();

        public int transferTo(byte[] dst, int dstOffset) {
            return super.transferTo(dst, 0, size(), dstOffset);
        }

        public int transferTo(byte[] dst, int srcOffset, int length, int dstOffset) {
            return super.transferTo(dst, srcOffset, size(), dstOffset);
        }

        public int transferTo(JBuffer dst, int srcOffset, int length, int dstOffset) {
            return super.transferTo(dst, srcOffset, size(), dstOffset);
        }

        public int transferTo(State dst) {
            return super.transferTo(dst, 0, size(), 0);
        }
    }

    /**
	 * Default number of headers used when calculating memory requirements for an
	 * empty packet state structure. This value will be multiplied by the
	 * sizeof(header_t) structure and added to the size of the packet_t strcutre.
	 */
    public static final int DEFAULT_STATE_HEADER_COUNT = 20;

    private static JFormatter out = new TextFormatter(new StringBuilder());

    /**
	 * Packet's default memory pool out of which allocates memory for deep copies
	 */
    protected static JMemoryPool pool = new JMemoryPool();

    /**
	 * Default scanner used to scan a packet per user request
	 */
    protected static JScanner scanner = new JScanner();

    /**
	 * Gets the current internal packet formatter used in the {@link #toString}
	 * method.
	 * 
	 * @return current formatter
	 */
    public static JFormatter getFormatter() {
        return JPacket.out;
    }

    /**
	 * Gets the current memory allocation memory pool.
	 * 
	 * @return current memory pool
	 */
    public static JMemoryPool getMemoryPool() {
        return pool;
    }

    /**
	 * Replaced the default formatter for formatting output in the
	 * {@link #toString} method. The new formatter will be used by default for all
	 * packets. The formatter should internally build a string that will be
	 * returned with out.toString() method call to get meaningfull output.
	 * 
	 * @param out
	 *          new formatter
	 */
    public static void setFormatter(JFormatter out) {
        JPacket.out = out;
    }

    /**
	 * Replaces the default memory allocation mechanism with user supplied one.
	 * 
	 * @param pool
	 *          new memory pool to use.
	 */
    public static void setMemoryPool(JMemoryPool pool) {
        JPacket.pool = pool;
    }

    /**
	 * The allocated memory buffer. Initialy this buffer is empty, but may be
	 * peered with allocated memory for internal usage such as copying header,
	 * state and data into a single buffer
	 */
    protected final JBuffer memory = new JBuffer(Type.POINTER);

    /**
	 * Packet's state structure
	 */
    protected final State state = new State(Type.POINTER);

    /**
	 * Allocates a memory block and peers both the state and data buffer with it.
	 * The size parameter has to be big enough to hold both state and data for the
	 * packet.
	 * 
	 * @param size
	 *          amount of memory to allocate for packet data
	 * @param state
	 *          size of the state
	 */
    public JPacket(int size, int state) {
        super(Type.POINTER);
        allocate(size + state);
    }

    /**
	 * A JPacket pointer. This is a pointer type constructor that does not
	 * allocate any memory but its intended to be pointed at a scanner packet_t
	 * structure that contains meta information about the structure of the packet
	 * data buffer.
	 * <p>
	 * JPacket constists of 2 peers. The first and the main memory peering is with
	 * the packet_state_t structure which stores information about the decoded
	 * state of the packet, another words the result of the scanned packet data
	 * buffer. The second peer is to the actual packet data buffer which is a
	 * seperate pointer.
	 * <h2>Peering struct packet_t</h2>
	 * This structure contains the "packet state". This is the decoded state which
	 * specifies what headers are in the buffer and at what offsets. This
	 * structure is the output of a JScanner.scan() method. The memory for this
	 * state can be anywhere, but by default JScanner stores it in a round-robin
	 * buffer it uses for decoding fast incoming packets. The state can easily be
	 * copied into another buffer for longer storage using such methods as
	 * <code>transferStateAndDataTo</code> which will copy the packet state
	 * and/or data buffer into another memory area, such as a direct ByteBuffer or
	 * JBuffer.
	 * </p>
	 */
    public JPacket(Type type) {
        super(type);
    }

    /**
	 * @param nid
	 * @param sequence
	 */
    public <T extends JAnalysis> void addAnalysis(int id, int instance, T analysis) {
        int index = state.findHeaderIndex(id, instance);
        this.state.setAnalysis(index, analysis);
    }

    /**
	 * @param nid
	 * @param sequence
	 */
    public <T extends JAnalysis> void addAnalysis(int id, T analysis) {
        addAnalysis(id, 0, analysis);
    }

    /**
	 * @param sequence
	 */
    public <T extends JAnalysis> void addAnalysis(T analysis) {
        this.state.setAnalysis(analysis);
    }

    /**
	 * Creates a new memory buffer of given size for internal usage
	 * 
	 * @param size
	 *          size in bytes
	 */
    public void allocate(int size) {
        pool.allocate(size, memory);
    }

    /**
	 * Gets the size of the current internal memory buffer
	 * 
	 * @return length in bytes
	 */
    public int getAllocatedMemorySize() {
        if (!memory.isInitialized()) {
            return 0;
        }
        return memory.size();
    }

    public <T extends JAnalysis> T getAnalysis(Class<? extends JHeader> c, T analysis) {
        return getAnalysis(JRegistry.lookupId(c), 0, analysis);
    }

    public <T extends JAnalysis> T getAnalysis(int id, int instance, T analysis) {
        int index = state.findHeaderIndex(id, instance);
        return this.state.getAnalysis(index).getAnalysis(analysis);
    }

    public <T extends JAnalysis> T getAnalysis(int id, T analysis) {
        return getAnalysis(id, 0, analysis);
    }

    public <T extends JAnalysis> T getAnalysis(T analysis) {
        return this.state.getAnalysis().getAnalysis(analysis);
    }

    /**
	 * Gets the capture header as generated by the native capture library.
	 * 
	 * @return capture header
	 */
    public abstract JCaptureHeader getCaptureHeader();

    /**
	 * Returns the frame number as assigned by either the packet scanner or
	 * analyzers.
	 * 
	 * @return zero based frame number
	 */
    public long getFrameNumber() {
        return state.getFrameNumber() + 1;
    }

    /**
	 * Peers the supplied header with the native header state structure and packet
	 * data buffer.
	 * 
	 * @param <T>
	 *          name of the header
	 * @param header
	 *          instance of a header object
	 * @return the supplied instance of the header
	 */
    public <T extends JHeader> T getHeader(T header) {
        return getHeader(header, 0);
    }

    /**
	 * Peers the supplied header with the native header state structure and packet
	 * data buffer. This method allows retrieval of a specific instance of a
	 * header if more than one instance has been found.
	 * 
	 * @param <T>
	 *          name of the header
	 * @param header
	 *          instance of a header object
	 * @param instance
	 *          instance number of the header since more than one header of the
	 *          same type can exist in the same packet buffer
	 * @return the supplied instance of the header
	 */
    public <T extends JHeader> T getHeader(T header, int instance) {
        check();
        final int index = this.state.findHeaderIndex(header.getId(), instance);
        if (index == -1) {
            return null;
        }
        return getHeaderByIndex(index, header);
    }

    /**
	 * Peers a header with specific index, not the numerical header ID assigned by
	 * JRegistry, of a header.
	 * 
	 * @param <T>
	 *          name of the header
	 * @param header
	 *          instance of a header object
	 * @param index
	 *          index into the header array the scanner has found
	 * @return the supplied header
	 * @throws IndexOutOfBoundsException
	 */
    public <T extends JHeader> T getHeaderByIndex(int index, T header) throws IndexOutOfBoundsException {
        JHeader.State hstate = header.getState();
        this.state.peerHeaderByIndex(index, hstate);
        header.peer(this, hstate.getOffset(), hstate.getLength());
        header.setPacket(this);
        header.setIndex(index);
        header.decode();
        return header;
    }

    /**
	 * Gets number of headers found within the packet header. The last header may
	 * or may not be the builtin Payload header
	 * 
	 * @return number of headers present
	 */
    public int getHeaderCount() {
        return this.state.getHeaderCount();
    }

    /**
	 * Gets the numerical ID of the header at specified index into header array as
	 * found by the packet scanner
	 * 
	 * @param index
	 *          index into the header array
	 * @return numerical ID of the header found at the specific index
	 */
    public int getHeaderIdByIndex(int index) {
        return this.state.getHeaderIdByIndex(index);
    }

    /**
	 * Gets number of headers with the same numerical ID as assigned by JRegistry
	 * within the same packet. For example Ip4 in ip4 packet would contain 2
	 * instances of Ip4 header.
	 * 
	 * @param id
	 *          numerical ID of the header to search for
	 * @return number of headers of the same type in the packet
	 */
    public int getHeaderInstanceCount(int id) {
        return this.state.getInstanceCount(id);
    }

    /**
	 * Gets the memory buffer with the supplied byte array data copied into it.
	 * The internal memory buffer is allocated if neccessary.
	 * 
	 * @param buffer
	 *          source array buffer to copy data out of
	 * @return the memory buffer
	 */
    protected JBuffer getMemoryBuffer(byte[] buffer) {
        pool.allocate(buffer.length, memory);
        memory.transferFrom(buffer);
        return memory;
    }

    /**
	 * Gets the memory buffer with the supplied ByteBuffer data copied into it.
	 * The internal memory buffer is allocated if neccessary.
	 * 
	 * @param buffer
	 *          source array buffer to copy data out of
	 * @return the memory buffer
	 */
    protected JBuffer getMemoryBuffer(ByteBuffer buffer) throws PeeringException {
        memory.peer(buffer);
        return memory;
    }

    /**
	 * Retrieves a memory buffer, allocated if neccessary, at least minSize in
	 * bytes. If existing buffer is already big enough, it is returned, otherwise
	 * a new buffer is allocated and the existing one released.
	 * 
	 * @param minSize
	 *          minimum number of bytes required for the buffer
	 * @return the buffer
	 */
    protected JBuffer getMemoryBuffer(int minSize) {
        if (!memory.isInitialized() || memory.size() < minSize) {
            allocate(minSize);
        }
        return memory;
    }

    /**
	 * Gets the memory buffer with the supplied JBuffer data copied into it. The
	 * internal memory buffer is allocated if neccessary.
	 * 
	 * @param buffer
	 *          source array buffer to copy data out of
	 * @return the memory buffer
	 */
    protected JBuffer getMemoryBuffer(JBuffer buffer) {
        memory.peer(buffer);
        return memory;
    }

    /**
	 * Gets the wire length of the packet. This is the original length as seen on
	 * the wire. This length may different JPacket.size() length, as the packet
	 * may have been truncated at the time of the capture.
	 * 
	 * @return original packet length
	 */
    public int getPacketWirelen() {
        return getCaptureHeader().wirelen();
    }

    public JScanner getScanner() {
        return scanner;
    }

    /**
	 * Gets the peered packet state object
	 * 
	 * @return packet native state
	 */
    public State getState() {
        return state;
    }

    /**
	 * Gets the total size of this packet. The size includes state, header and
	 * packet data.
	 * 
	 * @return size in bytes
	 */
    public abstract int getTotalSize();

    public int getType() {
        return AnalysisUtils.ROOT_TYPE;
    }

    public boolean hasAnalysis(Class<? extends JAnalysis> analysis) {
        return state.getAnalysis() != null && state.getAnalysis().hasAnalysis(analysis);
    }

    public boolean hasAnalysis(int type) {
        return state.getAnalysis() != null && state.getAnalysis().hasAnalysis(type);
    }

    public <T extends JAnalysis> boolean hasAnalysis(T analysis) {
        return (state.getAnalysis() != null) ? state.getAnalysis().hasAnalysis(analysis) : null;
    }

    /**
	 * Checks if header with specified numerical ID exists within the decoded
	 * packet
	 * 
	 * @param id
	 *          protocol header ID as assigned by JRegistry
	 * @return true header exists, otherwise false
	 */
    public boolean hasHeader(int id) {
        return hasHeader(id, 0);
    }

    /**
	 * Check if requested instance of header with specified numerical ID exists
	 * within the decoded packet
	 * 
	 * @param id
	 *          protocol header ID as assigned by JRegistry
	 * @param instance
	 *          instance number of the specific header within the packet
	 * @return true header exists, otherwise false
	 */
    public boolean hasHeader(int id, int instance) {
        check();
        final int index = this.state.findHeaderIndex(id, instance);
        if (index == -1) {
            return false;
        }
        return true;
    }

    /**
	 * Check if requested instance of header with specified numerical ID exists
	 * within the decoded packet and if found peers the supplied header with the
	 * located header within the decoded packet. This method executes as hasHeader
	 * followed by getHeader if found more efficiently.
	 * 
	 * @param <T>
	 *          name of the header type
	 * @param header
	 *          protocol header object instance
	 * @return true header exists, otherwise false
	 */
    public <T extends JHeader> boolean hasHeader(T header) {
        return (state.get64BitHeaderMap(0) & (1L << header.getId())) != 0 && hasHeader(header, 0);
    }

    /**
	 * Check if requested instance of header with specified numerical ID exists
	 * within the decoded packet and if found peers the supplied header with the
	 * located header within the decoded packet. This method executes as hasHeader
	 * followed by getHeader if found more efficiently.
	 * 
	 * @param <T>
	 *          name of the header type
	 * @param header
	 *          protocol header object instance
	 * @param instance
	 *          instance number of the specific header within the packet
	 * @return true header exists, otherwise false
	 */
    public <T extends JHeader> boolean hasHeader(T header, int instance) {
        check();
        final int index = this.state.findHeaderIndex(header.getId(), instance);
        if (index == -1) {
            return false;
        }
        getHeaderByIndex(index, header);
        return true;
    }

    /**
	 * Calculates the number of bytes remaining within the packet given a specific
	 * offset
	 * 
	 * @param offset
	 *          offset into the packet in bytes
	 * @return number of bytes remaining from specified offset
	 */
    public int remaining(int offset) {
        return size() - offset;
    }

    /**
	 * Calculates the remaining number of bytes within the packet buffer taking
	 * into account offset and length of a header supplied. The smaller of the 2
	 * is returned. This should typically be the length field unless the header
	 * has been truncated and remaining number of bytes is less.
	 * 
	 * @param offset
	 *          offset of the header to take into account
	 * @param length
	 *          length of the header
	 * @return smaller number of bytes either remaining or legth
	 */
    public int remaining(int offset, int length) {
        final int remaining = size() - offset;
        return (remaining >= length) ? length : remaining;
    }

    /**
	 * Scan and decode the packet using current scanner. The new packet state
	 * replaces any existing packet state already asigned to this packet.
	 * 
	 * @param id
	 *          numerical ID as assigned by JRegistry of the first protocol header
	 *          to be found in the packet, the DLT
	 */
    public void scan(int id) {
        scanner.scan(this, id, getCaptureHeader().wirelen());
    }

    /**
	 * Formats packet raw data as a hexdump output and marks header boundaries
	 * with special characters.
	 */
    @Override
    public String toHexdump() {
        if (state.isInitialized()) {
            return FormatUtils.hexdump(this);
        } else {
            byte[] b = this.getByteArray(0, this.size());
            return FormatUtils.hexdump(b);
        }
    }

    /**
	 * Generates text formatted output using the default builtin formatter. The
	 * default is to generate TextFormatter that uses a StringBuilder for output
	 * buffer and gerate a single string that is returned from here.
	 * 
	 * @return formatted output of this packet
	 */
    public String toString() {
        out.reset();
        try {
            out.format(this);
            return out.toString();
        } catch (IOException e) {
            throw new IllegalStateException("internal error, StringBuilder threw IOException");
        }
    }
}
