package drcl.inet.core;

import java.util.LinkedList;
import java.util.Iterator;
import java.util.ListIterator;
import drcl.comp.*;
import drcl.net.*;
import drcl.data.*;
import drcl.inet.InetConfig;
import drcl.inet.InetPacket;
import drcl.inet.TraceRTPkt;
import drcl.inet.contract.*;
import drcl.inet.data.RTKey;
import drcl.inet.data.RTEntry;
import drcl.util.StringUtil;
import drcl.util.ObjectUtil;
import drcl.util.scalar.IntSpace;

/**
 * Implements the packet dispatcher component in the inet router architecture.
 * Fragmentation is simulated, but not yet emulated.
 *
 * <ul>
 * <li>Ports:
 *    <ul>
 *    <li>*@up: (IN) the PacketSending Contract.
 *      (OUT) the PacketDelivery Contract.
 *    <li>*@down: (IN/OUT) drcl.net.Packet.
 *    <li>.service_rt@: initiator of the Route Lookup Contract.
 *    <li>.service_id@: initiator of the Identity Lookup Contract.
 *    <li>.pktarrival@: report of every packet arrival.
 *    </ul>
 * <li> Configuration: PDHeaderSize, fragmentEnabled, MTUs
 * </ul>
 * 
 * @see drcl.inet.contract.PktSending
 * @see drcl.inet.contract.PktDelivery
 * @see drcl.inet.contract.IDLookup
 * @see drcl.inet.contract.RTLookup
 */
public class PktDispatcher extends Module implements InetCoreConstants {

    static {
        Contract c1_ = new PktSending(Contract.Role_REACTOR);
        Contract c2_ = new PktDelivery(Contract.Role_INITIATOR);
        setContract(PktDispatcher.class, "*@" + Module.PortGroup_UP, new ContractMultiple(c1_, c2_));
    }

    static final long FLAG_FRAGMENT_ENABLED = 1L << FLAG_UNDEFINED_START;

    static final long FLAG_PIP_ENABLED = 1L << (FLAG_UNDEFINED_START + 1);

    static final long FLAG_TTL_CHECK_SKIP = 1L << (FLAG_UNDEFINED_START + 3);

    static final long FLAG_ROUTE_CACHE_ENABLED = 1L << (FLAG_UNDEFINED_START + 4);

    static final long FLAG_SWITCH_ENABLED = 1L << (FLAG_UNDEFINED_START + 5);

    static final long FLAG_LABEL_SWITCH_ENABLED = 1L << (FLAG_UNDEFINED_START + 6);

    static final long FLAG_ROUTE_BACK_ENABLED = 1L << (FLAG_UNDEFINED_START + 7);

    static final int DEFAULT_HEADER_SIZE = 20;

    static final double DEFAULT_FRAGMENT_TTL = 30;

    Identity id;

    RT rt;

    Port pktarrival = addEventPort(EVENT_PKT_ARRIVAL_PORT_ID);

    Port mcastHelp = addEventPort(MCAST_QUERY_PORT_ID);

    Port ucastHelp = addEventPort(UCAST_QUERY_PORT_ID);

    Port[] downPorts = null;

    int headerSize = DEFAULT_HEADER_SIZE;

    int seqno = 0;

    FragmentPack fragmentPack;

    VIFPack vifPack;

    LinkedList reassembleList;

    transient RouteCache routeCache = null;

    int[] connectTable = null;

    int[][][] incomingLabelMap = null;

    {
        removeDefaultUpPort();
        getPort(PortGroup_DOWN).set(PortGroup_DOWN, "0");
    }

    public static final int DEBUG_FRAGMENT = 0;

    public static final int DEBUG_REASSEMBLE = 1;

    public static final int DEBUG_PIP = 2;

    private static final String[] DEBUG_LEVEL_NAMES = { "debug_fragment", "debug_reassemble", "debug_pip" };

    public String[] getDebugLevelNames() {
        return DEBUG_LEVEL_NAMES;
    }

    public PktDispatcher() {
        super();
    }

    public PktDispatcher(String id_) {
        super(id_);
    }

    public void reset() {
        seqno = 0;
        super.reset();
        routeCache = null;
    }

    public void resetCache() {
        routeCache = null;
        downPorts = null;
    }

    public void duplicate(Object source_) {
        super.duplicate(source_);
        PktDispatcher that_ = (PktDispatcher) source_;
        headerSize = that_.headerSize;
        vifPack = that_.vifPack == null ? null : (VIFPack) that_.vifPack.clone();
        fragmentPack = that_.fragmentPack == null ? null : that_.fragmentPack._clone();
        incomingLabelMap = (int[][][]) ObjectUtil.clone(that_.incomingLabelMap);
        connectTable = (int[]) ObjectUtil.clone(that_.connectTable);
    }

    public Object clone() {
        PktDispatcher pd_ = new PktDispatcher(getID());
        pd_.duplicate(this);
        return pd_;
    }

    public String info() {
        return "           Header size= " + headerSize + "\n" + "               Seq. no= " + seqno + "\n" + "             TTL check? " + isTTLCheckEnabled() + "\n" + "       Fragment enabled? " + isFragmentEnabled() + "\n" + (isFragmentEnabled() ? fragmentPack.info() : "") + "   Fragment List: " + reassembleList + "\n" + (vifPack == null ? "" : vifPack.info()) + (routeCache == null ? "" : "route cache hit = " + (100.0 * routeCache.routeHitCount / routeCache.routeCount) + "% (" + routeCache.routeHitCount + "/" + routeCache.routeCount + ")\n") + "      Switching enabled? " + isSwitchingEnabled() + (connectTable == null ? "" : ", " + StringUtil.toString(connectTable)) + "\n" + "Label switching enabled? " + isLabelSwitchingEnabled() + (incomingLabelMap == null ? "" : ", " + StringUtil.toString(incomingLabelMap)) + "\n" + "          downPorts= " + (downPorts == null ? "null\n" : downPorts.length + "\n");
    }

    protected void portAdded(Port p_) {
        downPorts = null;
    }

    public void bind(Identity id_) {
        id = id_;
    }

    public void bind(RT rt_) {
        rt = rt_;
    }

    private void ___SCRIPTING___() {
    }

    public boolean isTTLCheckEnabled() {
        return getComponentFlag(FLAG_TTL_CHECK_SKIP) == 0;
    }

    public final void setTTLCheckEnabled(boolean v_) {
        setComponentFlag(FLAG_TTL_CHECK_SKIP, !v_);
    }

    public void setFragmentEnabled(boolean enabled_) {
        if (enabled_) if (fragmentPack == null) fragmentPack = new FragmentPack();
        setComponentFlag(FLAG_FRAGMENT_ENABLED, enabled_);
    }

    public boolean isFragmentEnabled() {
        return getComponentFlag(FLAG_FRAGMENT_ENABLED) != 0;
    }

    public void setPIPEnabled(boolean enabled_) {
        if (enabled_) if (vifPack == null) vifPack = new VIFPack();
        setComponentFlag(FLAG_PIP_ENABLED, enabled_);
    }

    public boolean isPIPEnabled() {
        return getComponentFlag(FLAG_PIP_ENABLED) != 0;
    }

    public void setRouteCacheEnabled(boolean enabled_) {
        setComponentFlag(FLAG_ROUTE_CACHE_ENABLED, enabled_);
        if (!enabled_) routeCache = null;
    }

    public boolean isRouteCacheEnabled() {
        return getComponentFlag(FLAG_ROUTE_CACHE_ENABLED) != 0;
    }

    public void setSwitchingEnabled(boolean enabled_) {
        setComponentFlag(FLAG_SWITCH_ENABLED, enabled_);
    }

    public boolean isSwitchingEnabled() {
        return getComponentFlag(FLAG_SWITCH_ENABLED) != 0;
    }

    public void setLabelSwitchingEnabled(boolean enabled_) {
        setComponentFlag(FLAG_LABEL_SWITCH_ENABLED, enabled_);
    }

    public boolean isLabelSwitchingEnabled() {
        return getComponentFlag(FLAG_LABEL_SWITCH_ENABLED) != 0;
    }

    public void setRouteBackEnabled(boolean enabled_) {
        setComponentFlag(FLAG_ROUTE_BACK_ENABLED, enabled_);
    }

    public boolean isRouteBackEnabled() {
        return getComponentFlag(FLAG_ROUTE_BACK_ENABLED) != 0;
    }

    public void setRouteCacheSize(int size_) {
        if (routeCache == null) routeCache = new RouteCache();
        routeCache.CACHE_SIZE = size_;
    }

    public int getRouteCacheSize() {
        return routeCache == null ? -1 : routeCache.CACHE_SIZE;
    }

    public void setHeaderSize(int hsize_) {
        headerSize = hsize_;
    }

    public int getHeaderSize() {
        return headerSize;
    }

    /**
   * Returns true this PktDispatcher needs route lookup service.
   * A PktDispatcher may not need route lookup service if the number of
   * the outgoing interfaces is less than two.
   */
    public boolean isRTLookup() {
        if (rt != null) return true;
        if (downPorts == null) createDownPorts();
        return downPorts != null && downPorts.length > 1;
    }

    int[] rtLookup(RTKey key_) {
        RTEntry e_ = (RTEntry) rt.get(key_, RTConfig.MATCH_LONGEST);
        if (e_ == null) return null; else return e_._getOutIfs();
    }

    public void _setSequenceNo(int sn_) {
        seqno = sn_;
    }

    public int _getSequenceNo() {
        return seqno;
    }

    public double getFragmentTTL() {
        return fragmentPack == null ? DEFAULT_FRAGMENT_TTL : fragmentPack.fragmentTTL;
    }

    public void setFragmentTTL(double ttl_) {
        if (fragmentPack == null) {
            drcl.Debug.error(this, "setFragmentTTL(): fragmentation is not enabled");
            return;
        }
        fragmentPack.fragmentTTL = ttl_;
    }

    public void _setMTU(int index_, int mtu_) {
        if (fragmentPack == null) setFragmentEnabled(true);
        if (mtu_ == fragmentPack.commonMTU) return;
        fragmentPack.mtu = __createMTUs(fragmentPack.mtu, index_ + 1);
        fragmentPack.mtu[index_] = mtu_;
    }

    /**
   * Sets the maximum fragmentation size at all interfaces.
   */
    public void _setMTUs(int mtu_) {
        if (fragmentPack == null) setFragmentEnabled(true);
        fragmentPack.commonMTU = mtu_;
        fragmentPack.mtu = null;
    }

    public int _getMTU(int index_) {
        if (fragmentPack == null) return Integer.MAX_VALUE;
        if (fragmentPack.mtu == null || fragmentPack.mtu.length <= index_) return fragmentPack.commonMTU; else return fragmentPack.mtu[index_];
    }

    public void setMTUs(int[] mtu_) {
        if (fragmentPack == null) setFragmentEnabled(true);
        fragmentPack.mtu = mtu_;
    }

    /** Returns the MTUs array used by this PktDispatcher. */
    public int[] getMTUs() {
        if (downPorts == null) createDownPorts();
        if (fragmentPack == null) {
            int[] tmp_ = new int[downPorts.length];
            for (int i = 0; i < tmp_.length; i++) tmp_[i] = Integer.MAX_VALUE;
            return tmp_;
        }
        if (fragmentPack.mtu == null || fragmentPack.mtu.length < downPorts.length) fragmentPack.mtu = __createMTUs(fragmentPack.mtu, downPorts.length);
        return fragmentPack.mtu;
    }

    int[] __createMTUs(int[] mtu_, int newSize_) {
        if (mtu_ == null) {
            mtu_ = new int[newSize_];
            for (int i = 0; i < mtu_.length; i++) mtu_[i] = fragmentPack.commonMTU;
        } else if (mtu_.length < newSize_) {
            int[] tmp_ = new int[newSize_];
            System.arraycopy(mtu_, 0, tmp_, 0, mtu_.length);
            for (int i = mtu_.length; i < tmp_.length; i++) tmp_[i] = fragmentPack.commonMTU;
            mtu_ = tmp_;
        }
        return mtu_;
    }

    private void ___DISPATCH___() {
    }

    protected synchronized void timeout(Object data_) {
        reassembleList.remove(data_);
        if (isGarbageEnabled()) drop(data_, "fragment timed-out");
    }

    protected synchronized void dataArriveAtUpPort(Object data_, drcl.comp.Port upPort_) {
        PktSending.Message request_;
        InetPacket ipkt_;
        boolean defaultSending = data_ instanceof InetPacket;
        if (data_ instanceof InetPacket) {
            request_ = null;
            ipkt_ = (InetPacket) data_;
        } else {
            request_ = (PktSending.Message) data_;
            ipkt_ = request_.getInetPacket();
        }
        if (pktarrival._isEventExportEnabled()) pktarrival.exportEvent(EVENT_PKT_ARRIVAL, ipkt_, "from local up port " + upPort_.getID());
        if (ipkt_.getProtocol() == 0) {
            ipkt_.setProtocol(Integer.parseInt(upPort_.getID()));
        }
        ipkt_.setID(seqno++);
        ipkt_.setHeaderSize(headerSize);
        long src_ = ipkt_.getSource();
        try {
            if (InetConfig.Addr.isAny(src_) || InetConfig.Addr.isNull(src_)) ipkt_.setSource(id.getDefaultID());
        } catch (Exception e_) {
            error(data_, "dataArriveAtUpPort()", upPort_, "connect to Identity? !" + e_);
            e_.printStackTrace();
        }
        try {
            if (request_ == null) forward(ipkt_, -1); else {
                int[] ifs_ = (int[]) request_.getIfs();
                if (request_.isMulticast()) forward(ipkt_, -1, ifs_); else broadcast(ipkt_, ifs_);
            }
        } catch (StackOverflowError e) {
            error(data_, null, upPort_, "false route entries: " + e.toString());
        }
    }

    protected synchronized void dataArriveAtDownPort(Object data_, drcl.comp.Port downPort_) {
        InetPacket p_ = (InetPacket) data_;
        int incomingIf_ = Integer.parseInt(downPort_.getID());
        if (pktarrival._isEventExportEnabled()) pktarrival.exportEvent(EVENT_PKT_ARRIVAL, p_, "from interface " + incomingIf_);
        if (getComponentFlag(FLAG_SWITCH_ENABLED) != 0 && connectTable != null && incomingIf_ < connectTable.length && connectTable[incomingIf_] >= 0) {
            int hops_ = p_.getHops() + 1;
            if (getComponentFlag(FLAG_TTL_CHECK_SKIP) == 0 && hops_ > p_.getTTL()) {
                if (isGarbageEnabled()) drop(p_, "exceeds TTL: " + hops_);
                return;
            } else p_.setHops(hops_);
            _forward(p_, connectTable[incomingIf_]);
            return;
        }
        if (getComponentFlag(FLAG_LABEL_SWITCH_ENABLED) != 0 && incomingLabelMap != null) {
            int incomingLabel_ = p_.getLabel();
            if (incomingIf_ < incomingLabelMap.length && incomingLabel_ >= 0 && incomingLabel_ < incomingLabelMap[incomingIf_].length && incomingLabelMap[incomingIf_][incomingLabel_] != null) {
                int hops_ = p_.getHops() + 1;
                if (getComponentFlag(FLAG_TTL_CHECK_SKIP) == 0 && hops_ > p_.getTTL()) {
                    if (isGarbageEnabled()) drop(p_, "exceeds TTL: " + hops_);
                    return;
                } else p_.setHops(hops_);
                p_.setLabel((short) incomingLabelMap[incomingIf_][incomingLabel_][1]);
                _forward(p_, incomingLabelMap[incomingIf_][incomingLabel_][0]);
                return;
            }
        }
        forward(p_, incomingIf_);
    }

    InetPacket _reassemble(long src_, long dest_, InetPacket p_) {
        int id_ = p_.getID();
        int ulp_ = p_.getProtocol();
        if (reassembleList == null) reassembleList = new LinkedList();
        InetPacket fragPack_ = null;
        ACATimer timer_ = null;
        InetPacket fh_ = null;
        for (Iterator it_ = reassembleList.iterator(); it_.hasNext(); ) {
            fh_ = (InetPacket) it_.next();
            if (fh_.getID() == id_ && fh_.getSource() == src_ && fh_.getDestination() == dest_ && fh_.getProtocol() == ulp_) {
                fragPack_ = fh_;
                break;
            }
        }
        if (fragPack_ == null) {
            if (isDebugEnabled() && isDebugEnabledAt(DEBUG_REASSEMBLE)) debug("--- first fragment to arrive ---" + p_);
            Object[] tmp_ = new Object[] { null, new IntSpace(), setTimeout(fragPack_, getFragmentTTL()) };
            fragPack_ = new InetPacket(src_, dest_, ulp_, 0, 0, false, 0, id_, 0, 0, tmp_, 0);
            fragPack_.setPacketSize(0);
            reassembleList.add(fragPack_);
        }
        Object[] tmp_ = (Object[]) fragPack_.getBody();
        IntSpace bitmap_ = (IntSpace) tmp_[1];
        timer_ = (ACATimer) tmp_[2];
        int offset_ = p_.getFragmentOffset();
        int offsetend_ = offset_ + p_.size - p_.headerSize;
        bitmap_.checkout(offset_, offsetend_);
        if (offset_ == 0) {
            if (isDebugEnabled() && isDebugEnabledAt(DEBUG_REASSEMBLE)) debug("--- 'the first' fragment---" + p_);
            tmp_[0] = p_.getBody();
        }
        int originalPktSize_ = fragPack_.size;
        if (!p_.hasMoreFragment()) {
            if (isDebugEnabled() && isDebugEnabledAt(DEBUG_REASSEMBLE)) debug("--- 'the last' fragment---" + p_);
            fragPack_.size = originalPktSize_ = offsetend_;
        }
        if (originalPktSize_ > 0 && bitmap_.getSize(0, originalPktSize_) == 0) {
            if (isDebugEnabled() && isDebugEnabledAt(DEBUG_REASSEMBLE)) {
                debug("--- last seqment to arrive ---" + p_ + "\n" + "===== COMPLETE: " + tmp_[0]);
            }
            InetPacket completePkt_ = (InetPacket) tmp_[0];
            reassembleList.remove(fragPack_);
            cancelTimeout(timer_);
            tmp_[0] = tmp_[1] = tmp_[2] = null;
            fragPack_.setBody(null, 0);
            if (completePkt_.isFragment()) {
                if (isDebugEnabled()) debug("FRAGMENT AGAIN!");
                return _reassemble(completePkt_.getSource(), completePkt_.getDestination(), completePkt_);
            } else return completePkt_;
        } else return null;
    }

    /**
      * Forwards the packet according to its destination field.
      * Also checks if needs to be delivered to local.
      */
    void forward(InetPacket p_, int incomingIf_) {
        try {
            long src_ = p_.getSource();
            long dest_ = p_.getDestination();
            boolean routerAlert_ = p_.isRouterAlertEnabled();
            boolean deliverToLocal_ = false;
            try {
                deliverToLocal_ = routerAlert_ && incomingIf_ >= 0 || !routerAlert_ && id.query(dest_);
            } catch (Exception e_) {
                error(p_, "forward(InetPacket, int)", null, "connect to Identity? !" + e_);
                return;
            }
            if (deliverToLocal_) {
                InetPacket complete_ = p_;
                while (deliverToLocal_ && complete_ != null && (complete_.isFragment() || complete_.isPacketInPacket())) {
                    if (complete_.isFragment()) complete_ = _reassemble(src_, dest_, complete_);
                    if (complete_ != null && complete_.isPacketInPacket()) {
                        int vif_ = _pipUnwrap(complete_);
                        if (vif_ < 0) return;
                        incomingIf_ = vif_;
                        complete_ = (InetPacket) complete_.getBody();
                        src_ = complete_.getSource();
                        dest_ = complete_.getDestination();
                        routerAlert_ = complete_.isRouterAlertEnabled();
                        try {
                            deliverToLocal_ = routerAlert_ || id.query(dest_);
                        } catch (Exception e_) {
                            error(complete_, "forward(InetPacket, int)", null, "connect to Identity? !" + e_);
                            return;
                        }
                    }
                }
                if (complete_ != null && deliverToLocal_) {
                    boolean traceRoute_ = p_ instanceof TraceRTPkt;
                    if (traceRoute_) {
                        int type_ = ((TraceRTPkt) p_).getType();
                        if (routerAlert_) {
                            ((TraceRTPkt) p_).addHop(getTime(), id.getDefaultID(), incomingIf_);
                        }
                        if (id.query(dest_)) {
                            if (type_ == TraceRTPkt.RT_REQUEST) {
                                long tmp_ = p_.getDestination();
                                p_.setDestination(p_.getSource());
                                p_.setSource(tmp_);
                                p_.setRouterAlertEnabled(false);
                                p_.setTTL(255);
                                ((TraceRTPkt) p_).setType(TraceRTPkt.RT_RESPONSE);
                                forward(p_, -1);
                                return;
                            } else traceRoute_ = false;
                        }
                    }
                    if (!traceRoute_) {
                        Port up_ = getPort(Module.PortGroup_UP, String.valueOf(complete_.getProtocol()));
                        if (up_ == null) {
                            if (isGarbageEnabled()) drop(complete_, "upper layer (" + complete_.getProtocol() + ") does not exist to deliver the pkt");
                            if (routerAlert_ || !InetConfig.Addr.isMcast(dest_)) return;
                        } else {
                            if (routerAlert_ || !InetConfig.Addr.isMcast(dest_)) {
                                complete_.setIncomingIf(incomingIf_);
                                up_.doSending(complete_);
                                return;
                            } else {
                                InetPacket tmp_ = (InetPacket) complete_.clone();
                                tmp_.setIncomingIf(incomingIf_);
                                up_.doSending(tmp_);
                            }
                        }
                    }
                }
                if (complete_ != null) p_ = complete_; else return;
            }
            int hops_ = p_.getHops() + 1;
            if (getComponentFlag(FLAG_TTL_CHECK_SKIP) == 0 && hops_ > p_.getTTL()) {
                if (isGarbageEnabled()) drop(p_, "exceeds TTL: " + hops_);
                return;
            } else p_.setHops(hops_);
            if (!_hasRoutingCapability()) {
                if (downPorts == null) createDownPorts();
                if (incomingIf_ < 0 && downPorts != null && downPorts.length == 1) {
                    if (!isFragmentEnabled() || p_.dontFragment()) downPorts[0].doLastSending(p_); else _fragmentOutput(p_, downPorts[0], Integer.parseInt(downPorts[0].getID()));
                } else if (!deliverToLocal_) error(p_, "forward(InetPacket, int)", infoPort, "no routing capability");
            } else {
                _routePacket(p_, src_, dest_, incomingIf_, deliverToLocal_);
            }
        } catch (Exception e_) {
            error(p_, "forward()", infoPort, "wrong header format?| " + e_);
            e_.printStackTrace();
        }
    }

    boolean _hasRoutingCapability() {
        return rt != null;
    }

    int[] _checkRouteCache(long src_, long dest_, int incomingIf_) {
        if (routeCache == null || routeCache.cache == null) return null;
        int top_ = 0;
        int bottom_ = routeCache.cache.size() - 1;
        while (top_ <= bottom_) {
            int i = (top_ + bottom_) / 2;
            RouteCacheEntry cache_ = (RouteCacheEntry) routeCache.cache.get(i);
            if (cache_.dest == dest_) {
                if (cache_.match(src_, incomingIf_)) {
                    routeCache.routeHitCount++;
                    return cache_.ifs;
                }
                for (ListIterator it_ = routeCache.cache.listIterator(i); it_.hasPrevious(); ) {
                    if (cache_.dest == dest_) {
                        if (cache_.match(src_, incomingIf_)) {
                            routeCache.routeHitCount++;
                            return cache_.ifs;
                        }
                    } else break;
                }
                for (ListIterator it_ = routeCache.cache.listIterator(i + 1); it_.hasNext(); ) {
                    if (cache_.dest == dest_) {
                        if (cache_.match(src_, incomingIf_)) {
                            routeCache.routeHitCount++;
                            return cache_.ifs;
                        }
                    } else break;
                }
                return null;
            } else if (cache_.dest > dest_) bottom_ = i - 1; else top_ = i + 1;
        }
        return null;
    }

    void _updateRouteCache(long src_, long dest_, int incomingIf_, int[] ifs_) {
        if (routeCache == null) routeCache = new RouteCache();
        if (routeCache.cache == null) {
            routeCache.cache = new LinkedList();
            routeCache.cache.add(new RouteCacheEntry(src_, dest_, incomingIf_, ifs_));
            return;
        }
        int top_ = 0;
        int bottom_ = routeCache.cache.size() - 1;
        int added_ = -1;
        while (top_ <= bottom_) {
            int i = (top_ + bottom_) / 2;
            RouteCacheEntry cache_ = (RouteCacheEntry) routeCache.cache.get(i);
            if (cache_.dest == dest_) {
                routeCache.cache.add(i, new RouteCacheEntry(src_, dest_, incomingIf_, ifs_));
                added_ = i;
                break;
            } else if (cache_.dest > dest_) bottom_ = i - 1; else top_ = i + 1;
        }
        if (added_ < 0) routeCache.cache.add(top_, new RouteCacheEntry(src_, dest_, incomingIf_, ifs_));
        if (routeCache.cache.size() > routeCache.CACHE_SIZE) routeCache.cache.remove((int) (Math.random() * routeCache.CACHE_SIZE));
    }

    void _routePacket(InetPacket p_, long src_, long dest_, int incomingIf_, boolean deliverToLocal_) {
        if (routeCache != null) routeCache.routeCount++;
        int[] ifs_ = getComponentFlag(FLAG_ROUTE_CACHE_ENABLED) != 0 ? _checkRouteCache(src_, dest_, incomingIf_) : null;
        if (ifs_ == null) {
            ifs_ = rt.lookup(p_, src_, dest_, incomingIf_);
            if (ifs_ == null || ifs_.length == 0) {
                if (InetConfig.Addr.isMcast(dest_)) {
                    if (mcastHelp.anyPeer()) ifs_ = RTLookup.lookup(p_, incomingIf_, mcastHelp);
                } else {
                    if (ucastHelp.anyPeer()) ifs_ = RTLookup.lookup(p_, incomingIf_, ucastHelp);
                }
            }
            if (ifs_ != null && ifs_.length > 0 && getComponentFlag(FLAG_ROUTE_CACHE_ENABLED) != 0) _updateRouteCache(src_, dest_, incomingIf_, ifs_);
        }
        if (ifs_ == null || ifs_.length == 0) {
            if (!deliverToLocal_) if (isGarbageEnabled()) drop(p_, "no route for the pkt");
        } else forward(p_, incomingIf_, ifs_);
    }

    static final Object FRAGMENT_STRING = "fragment";

    void _fragmentOutput(InetPacket p_, Port out_, int if_) {
        int mtu_ = _getMTU(if_);
        if (p_.size <= mtu_) {
            out_.doLastSending(p_);
            return;
        }
        int mtubodysize_ = mtu_ - p_.headerSize;
        if (mtubodysize_ < 0) {
            error(p_, "fragmentOutput()", null, "header size larger than mtu on " + out_);
            return;
        }
        int bodysize_ = p_.size;
        int npkt_ = (bodysize_ + mtubodysize_ - 1) / mtubodysize_;
        if (isDebugEnabled() && isDebugEnabledAt(DEBUG_FRAGMENT)) debug("fragment the packet into " + npkt_ + " pieces: " + p_);
        int offset_ = 0;
        int seqno_ = p_.getID();
        for (int i = 1; i <= npkt_; i++) {
            int size_ = bodysize_ >= mtubodysize_ ? mtubodysize_ : bodysize_;
            bodysize_ -= mtubodysize_;
            Object pb_ = i == 1 ? p_ : FRAGMENT_STRING;
            InetPacket fh_ = (InetPacket) p_._clone();
            fh_.setBody(pb_, size_);
            fh_.setFragmentParam(i < npkt_, offset_, seqno_);
            try {
                fh_.setSource(id.getDefaultID());
            } catch (Exception e_) {
                error(p_, "_fragmentOutput()", null, "connect to Identity? !" + e_);
            }
            offset_ += size_;
            if (out_ == null) _routePacket(fh_, fh_.getSource(), fh_.getDestination(), -1, false); else out_.doLastSending(fh_);
        }
    }

    void createDownPorts() {
        if (downPorts == null) {
            Port[] tmp_ = getAllPorts(PortGroup_DOWN);
            try {
                downPorts = new Port[tmp_.length];
                for (int i = 0; i < tmp_.length; i++) downPorts[Integer.parseInt(tmp_[i].getID())] = tmp_[i];
            } catch (ArrayIndexOutOfBoundsException e_) {
                int len_ = 0;
                for (int i = 0; i < tmp_.length; i++) {
                    int if_ = Integer.parseInt(tmp_[i].getID()) + 1;
                    if (len_ < if_) len_ = if_;
                }
                downPorts = new Port[len_];
                for (int i = 0; i < tmp_.length; i++) downPorts[Integer.parseInt(tmp_[i].getID())] = tmp_[i];
            }
        }
    }

    /**
      * This method presents the most general case where the packet is forwarded
      * toward arbitrarily specified interfaces.
      */
    void forward(InetPacket p_, int incomingIf_, int[] ifs_) {
        boolean first_ = true;
        boolean dontFragment_ = !isFragmentEnabled() || p_.dontFragment();
        boolean routeBack_ = isRouteBackEnabled();
        for (int i = 0; i < ifs_.length; i++) {
            int if_ = ifs_[i];
            if (if_ >= 0 && (routeBack_ || if_ != incomingIf_)) {
                InetPacket data_ = first_ ? p_ : (InetPacket) p_.clone();
                if (downPorts == null) createDownPorts();
                if (if_ >= downPorts.length || downPorts[if_] == null) {
                    if (isPIPEnabled()) {
                        _pipEncapsulate(data_, if_);
                    } else error(p_, "forward(InetPacket, int, int[])", infoPort, "interface " + if_ + " does not exist");
                } else if (dontFragment_) downPorts[if_].doLastSending(data_); else _fragmentOutput(data_, downPorts[if_], if_);
                first_ = false;
            }
        }
    }

    void _forward(InetPacket p_, int if_) {
        if (downPorts == null) createDownPorts();
        if (if_ >= downPorts.length || downPorts[if_] == null) {
            error(p_, "_forward(InetPacket, int)", infoPort, "interface " + if_ + " does not exist");
            return;
        }
        downPorts[if_].doLastSending(p_);
    }

    /**
      */
    void broadcast(InetPacket p_, int[] excludedIfs_) {
        if (downPorts == null) createDownPorts();
        boolean first_ = true;
        boolean dontFragment_ = !isFragmentEnabled() || p_.dontFragment();
        drcl.data.BitSet excludedIfBitSet_ = new drcl.data.BitSet(excludedIfs_);
        for (int i = 0; i < downPorts.length; i++) {
            Port out_ = downPorts[i];
            if (out_ != null && !excludedIfBitSet_.get(i)) {
                InetPacket data_ = first_ ? p_ : (InetPacket) p_.clone();
                try {
                    if (dontFragment_) out_.doLastSending(data_); else _fragmentOutput(data_, out_, i);
                    first_ = false;
                } catch (Exception e_) {
                }
            }
        }
        if (isPIPEnabled()) {
            if (vifPack.peers == null) return;
            for (int i = 0; i < vifPack.peers.length; i++) {
                if (vifPack.peers[i] == Address.NULL_ADDR || excludedIfBitSet_.get(vifPack.vifStartIndex + i)) continue;
                InetPacket data_ = first_ ? p_ : (InetPacket) p_.clone();
                _pipEncapsulate(data_, vifPack.vifStartIndex + i);
            }
        }
    }

    private void ___VIF___() {
    }

    /**
   * Adds a virtual interface and the corresponding up port to this component.
   */
    public synchronized void setVIF(int vif_, long peer_) {
        setPIPEnabled(true);
        if (vifPack == null) vifPack = new VIFPack();
        vifPack._setVIF(vif_, peer_);
    }

    /**
   * Adds a virtual interface and the corresponding up port to this component.
   */
    public synchronized void setVIF(int vif_, long myself_, long peer_) {
        setPIPEnabled(true);
        if (vifPack == null) vifPack = new VIFPack();
        vifPack._setVIF(vif_, myself_, peer_);
    }

    /**
   * Installs the VIF pack on this component.
   */
    public synchronized void setVIFs(VIFPack pack_) {
        vifPack = pack_;
        if (vifPack != null) setPIPEnabled(true);
    }

    /**
   * Returns the VIF pack data structure regarding the VIF setup on this 
   * component.
   */
    public synchronized VIFPack getVIFs() {
        return vifPack;
    }

    void _pipEncapsulate(InetPacket p_, int vif_) {
        long src_ = vifPack._getMyself(vif_);
        if (src_ == Address.NULL_ADDR) src_ = id.getDefaultID();
        long dest_ = vifPack._getPeer(vif_);
        if (dest_ == Address.NULL_ADDR) {
            error(p_, "_pipEncapsulate()", infoPort, "invalid vif_: " + vif_);
            return;
        }
        InetPacket new_ = new InetPacket(src_, dest_, 0, 255, 0, false, p_.getTOS(), 0, p_.getFlag(), 0, p_, p_.size);
        new_.setPacketInPacket(true);
        if (isFragmentEnabled() && !new_.dontFragment()) _fragmentOutput(new_, null, vif_); else _routePacket(new_, src_, dest_, -1, false);
    }

    int _pipUnwrap(InetPacket p_) {
        long peer_ = p_.getSource();
        int vif_ = vifPack._getVIFFromPeer(peer_);
        if (vif_ == -1) {
            error(p_, "_pipUnwrap()", infoPort, "no vif is set up for peer " + peer_);
            return -1;
        }
        return vif_;
    }

    /** Sets the switching cross connect table. */
    public void setSwitches(int[] connectTable_) {
        connectTable = connectTable_;
    }

    /** Returns the switching cross connect table. */
    public int[] getSwitches() {
        return connectTable;
    }

    /** 
   * Sets the switching cross connect table entry at the incoming interface.
   */
    public void setSwitch(int incomingIf_, int outgoingIf_) {
        if (connectTable == null || incomingIf_ >= connectTable.length) {
            int[] tmp_ = new int[incomingIf_ + 1];
            for (int i = 0; i < tmp_.length; i++) tmp_[i] = -1;
            if (connectTable != null) System.arraycopy(connectTable, 0, tmp_, 0, connectTable.length);
            connectTable = tmp_;
        }
        connectTable[incomingIf_] = outgoingIf_;
    }

    /** 
   * Removes the switching cross connect table entry at the incoming 
   * interface. 
   */
    public void removeSwitch(int incomingIf_) {
        if (incomingIf_ < 0) return;
        if (connectTable != null && incomingIf_ < connectTable.length) connectTable[incomingIf_] = -1;
    }

    /** Sets the label switching incoming label map. */
    public void setLabelSwitches(int[][][] map_) {
        incomingLabelMap = map_;
    }

    /** Returns the label switching incoming label map. */
    public int[][][] getLabelSwitches() {
        return incomingLabelMap;
    }

    /**
   * Sets the label switching incoming label map entry at the incoming 
   * interface. */
    public void setLabelSwitch(int incomingIf_, int[][] switch_) {
        if (incomingLabelMap == null || incomingIf_ >= incomingLabelMap.length) {
            int[][][] tmp_ = new int[incomingIf_ + 1][][];
            if (incomingLabelMap != null) System.arraycopy(incomingLabelMap, 0, tmp_, 0, incomingLabelMap.length);
            incomingLabelMap = tmp_;
        }
        incomingLabelMap[incomingIf_] = switch_;
    }

    /**
   * Sets the label switching incoming label map entry at the incoming 
   * interface and the incoming label. */
    public void setLabelSwitch(int incomingIf_, int incomingLabel_, int outgoingIf_, int outgoingLabel_) {
        if (incomingLabelMap == null || incomingIf_ >= incomingLabelMap.length) {
            int[][][] tmp_ = new int[incomingIf_ + 1][][];
            if (incomingLabelMap != null) System.arraycopy(incomingLabelMap, 0, tmp_, 0, incomingLabelMap.length);
            incomingLabelMap = tmp_;
        }
        if (incomingLabelMap[incomingIf_] == null || incomingLabel_ >= incomingLabelMap[incomingIf_].length) {
            int[][] tmp_ = new int[incomingLabel_ + 1][];
            if (incomingLabelMap[incomingIf_] != null) System.arraycopy(incomingLabelMap[incomingIf_], 0, tmp_, 0, incomingLabelMap[incomingIf_].length);
            incomingLabelMap[incomingIf_] = tmp_;
        }
        if (incomingLabelMap[incomingIf_][incomingLabel_] == null) incomingLabelMap[incomingIf_][incomingLabel_] = new int[] { outgoingIf_, outgoingLabel_ }; else {
            incomingLabelMap[incomingIf_][incomingLabel_][0] = outgoingIf_;
            incomingLabelMap[incomingIf_][incomingLabel_][1] = outgoingLabel_;
        }
    }

    /** 
   * Removes the label switching incoming label map entry at the incoming 
   * interface. */
    public void removeLabelSwitches(int incomingIf_) {
        if (incomingIf_ < 0) return;
        if (incomingLabelMap != null && incomingIf_ < incomingLabelMap.length) incomingLabelMap[incomingIf_] = null;
    }

    /**
   * Removes the label switching incoming label map entry at the incoming 
   * interface and the incoming label. */
    public void removeLabelSwitch(int incomingIf_, int incomingLabel_) {
        if (incomingIf_ < 0 || incomingLabel_ < 0) return;
        if (incomingLabelMap != null && incomingIf_ < incomingLabelMap.length && incomingLabelMap[incomingIf_] != null && incomingLabel_ < incomingLabelMap[incomingIf_].length) incomingLabelMap[incomingIf_][incomingLabel_] = null;
    }

    class FragmentPack {

        double fragmentTTL = DEFAULT_FRAGMENT_TTL;

        int commonMTU = DEFAULT_MTU;

        int[] mtu;

        String info() {
            return "    Fragment TTL= " + fragmentTTL + "\n" + "      Common MTU= " + commonMTU + "\n" + "            MTUs= " + drcl.util.StringUtil.toString(mtu) + "\n";
        }

        FragmentPack _clone() {
            FragmentPack new_ = new FragmentPack();
            new_.fragmentTTL = fragmentTTL;
            new_.commonMTU = commonMTU;
            return new_;
        }
    }

    class RouteCache {

        int CACHE_SIZE = 100;

        long routeCount = 0;

        long routeHitCount = 0;

        LinkedList cache = null;
    }

    class RouteCacheEntry {

        long src, dest;

        int incomingIf;

        int[] ifs;

        RouteCacheEntry(long src_, long dest_, int incomingIf_, int[] ifs_) {
            src = src_;
            dest = dest_;
            incomingIf = incomingIf_;
            ifs = ifs_;
        }

        boolean match(long src_, int incomingIf_) {
            return src == src_ && incomingIf == incomingIf_;
        }
    }
}
