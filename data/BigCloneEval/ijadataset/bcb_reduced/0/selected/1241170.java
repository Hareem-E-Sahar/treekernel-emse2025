package overlaysim.protocol.overlay.BitTorrent;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import overlaysim.node.Node;
import overlaysim.simulator.Simulator;
import overlaysim.start.Start;

public class BT_Topology {

    public static final int UNIV_NODE_NUM = 0;

    public static final int AS_NUM = 14;

    public static final int HEADEND_PER_AS_NUM = 10;

    public static final int CABLE_PER_HEADEND_NUM = 5;

    public static final int[] landmark_node = { 1, 51, 101, 151, 201, 251, 301, 351, 401, 451 };

    public static final int nbit = 8;

    public static final int CLOSE_NULL = -1;

    public static final int INTRA_HEADEND = 0;

    public static final int INTRA_UNIV = 1;

    public static final int INTRA_AS = 2;

    public static final int INTER = 3;

    public static final int DEGREES_OF_CLOSENESS = 4;

    public static long[] g_closeness = new long[DEGREES_OF_CLOSENESS];

    public static int[][] _closeness_matrix = null;

    public static int[][] _delay_matrix = null;

    public static final int NORMAL_TRACKER = 0;

    public static final int SUB_TRACKER = 1;

    public static final int UNIV_TRACKER = 2;

    public static final int SUB_LIMIT_TRACKER = 3;

    public static final int UNIV_UNIV = 0;

    public static final int UNIV_VIRT = 1;

    public static final int VIRT_UNIV = 2;

    public static final int UNIV_AS = 3;

    public static final int AS_UNIV = 4;

    public static final int AS_AS = 5;

    public static final int AS_VIRT = 6;

    public static final int VIRT_AS = 7;

    public static final int HEADEND_HEADEND = 8;

    public static final int HEADEND_VIRT = 9;

    public static final int VIRT_HEADEND = 10;

    public static final int VIRT_LEAF = 11;

    public static final int LEAF_VIRT = 12;

    public static final int NUM_LINK_TYPES = 13;

    public static double[] _bandwidth = { 10000000.0, 10000000.0, 10000000.0, 10000000.0, 10000000.0, 10000000.0, 10000000.0, 10000000.0, 10000000.0, 10000000.0, 10000000.0, 1000000.0, 512000.0 };

    public static final int ADD = 0;

    public static final int REMOVE = 1;

    /*************
	 * CONSTANTS *
	 *************/
    public static final int MAX_CHARS_PER_LINE = 80;

    public static int _tracker_type = NORMAL_TRACKER;

    public static BT_Tracker _tracker = new BT_Tracker();

    public static BT_Subtracker univ_sub = null;

    public static boolean _seed_is_cable = false;

    public static int _arrivals_are_random = 0;

    public static int _departures_are_random = 0;

    public static int _num_external_peers = 0;

    public static int num_clents = 0;

    public static int _num_as = 0;

    public static int _as_num_node = HEADEND_PER_AS_NUM;

    public static int _as_office_fanout = CABLE_PER_HEADEND_NUM;

    public static LinkedList<As> _as = new LinkedList<As>();

    public static int _num_univ_nodes = 0;

    public static LinkedList<Univ_Node> _univ_node = new LinkedList<Univ_Node>();

    public static int _num_headend_nodes = 0;

    public static LinkedList<Headend_Node> _headend_node = new LinkedList<Headend_Node>();

    public static int _num_cable_leaf_nodes = 0;

    public static LinkedList<Cable_Leaf_Node> _cable_leaf_node = new LinkedList<Cable_Leaf_Node>();

    public static LinkedList<Link> _links = new LinkedList<Link>();

    class Conn {

        public long tick_update;

        public int sender_id;

        public int receiver_id;

        public int closeness;

        public Conn(int s_id, int r_id) {
            sender_id = s_id;
            receiver_id = r_id;
        }
    }

    public static Conn[][] _connection_map;

    public static LinkedList<Link> _link_vec = new LinkedList<Link>();

    public static LinkedList<Bytes_Transfer> _xfer_vec = new LinkedList<Bytes_Transfer>();

    class Bytes_Transfer {

        public int sender_id;

        public int receiver_id;

        public int num_bytes;

        public int traffic_type;

        public Bytes_Transfer(int s_id, int r_id, int b, int t) {
            sender_id = s_id;
            receiver_id = r_id;
            num_bytes = b;
            traffic_type = t;
        }
    }

    class Conn_Bw {

        public Conn conn;

        public double bandwidth;
    }

    ;

    class As_Virt {

        public Link as_link;

        public LinkedList<Link> univ_links = new LinkedList<Link>();

        public LinkedList<Link> gateway_links = new LinkedList<Link>();

        public double outgoing_bw_percent = 0;

        public double incoming_bw_percent = 0;

        public String toString() {
            StringBuilder result = new StringBuilder("as_link" + as_link);
            return result.toString();
        }
    }

    class As {

        public int as_id;

        public As_Virt virt_node;

        public Link virt_link;

        public int num_nodes;

        public int low_client_id;

        public int hi_client_id;

        public Headend_Node gateway;

        public BT_Subtracker subtracker;

        public LinkedList<Headend_Node> as_handend_node = new LinkedList<Headend_Node>();

        public As(int id) {
            as_id = id;
            virt_node = new As_Virt();
            subtracker = new BT_Subtracker();
        }

        public String toString() {
            StringBuilder result = new StringBuilder("as_id" + as_id);
            return result.toString();
        }
    }

    ;

    class Univ_Virt {

        public Link univ_link;

        public LinkedList<Link> univ_links = new LinkedList<Link>();

        public LinkedList<Link> gateway_links = new LinkedList<Link>();

        public double outgoing_bw_percent = 0;

        public double incoming_bw_percent = 0;

        public String toString() {
            StringBuilder result = new StringBuilder("univ_link" + univ_link);
            return result.toString();
        }
    }

    ;

    class Univ_Node {

        public Univ_Virt virt_node;

        public Link virt_link;

        public int u_n_id;

        public BT_Subtracker univ_subtracker = null;

        public Univ_Node(int id, BT_Subtracker unsub) {
            u_n_id = id;
            virt_node = new Univ_Virt();
            univ_subtracker = unsub;
        }

        public String toString() {
            StringBuilder result = new StringBuilder("u_n_id" + u_n_id);
            return result.toString();
        }
    }

    ;

    class Cable_Virt {

        public Link headend_link;

        public LinkedList<Link> cable_leaf_links = new LinkedList<Link>();

        public double outgoing_bw_percent = 0;

        public double incoming_bw_percent = 0;

        public String toString() {
            StringBuilder result = new StringBuilder("Cable_Virt");
            return result.toString();
        }
    }

    ;

    class Headend_Node {

        public int he_id;

        public Cable_Virt virt_node;

        public As my_as;

        public int low_client_id;

        public int hi_client_id;

        public Link virt_link;

        public LinkedList<Link> headend_links;

        public Headend_Node(int id) {
            he_id = id;
            virt_node = new Cable_Virt();
            headend_links = new LinkedList<Link>();
        }

        public String toString() {
            StringBuilder result = new StringBuilder("Headend_Node:id=" + he_id + " virt_node=" + virt_node + " my_as" + my_as + " l_id=" + low_client_id + " h_id" + hi_client_id);
            return result.toString();
        }
    }

    ;

    class Cable_Leaf_Node {

        public int cln_id;

        public Link virt_link;

        public double outgoing_bw_percent = 0;

        public double incoming_bw_percent = 0;

        public Cable_Leaf_Node(int id) {
            cln_id = id;
        }

        public String toString() {
            StringBuilder result = new StringBuilder("cln_id" + cln_id);
            return result.toString();
        }
    }

    ;

    /***********************
	 * FUNCTION PROTOTYPES *
	 ***********************/
    public BT_Topology() {
    }

    public static BT_Tracker get_tracker(BT_Peer peer) {
        if (((_tracker_type != SUB_TRACKER) && (_tracker_type != SUB_LIMIT_TRACKER)) || peer.is_univ_node()) {
            return _tracker;
        }
        Cable_Leaf_Node cable_leaf = peer.cln;
        Cable_Virt c_v = (Cable_Virt) cable_leaf.virt_link.recv_node;
        Headend_Node he = (Headend_Node) c_v.headend_link.recv_node;
        return he.my_as.subtracker;
    }

    public static int get_tracker_type() {
        return _tracker_type;
    }

    public boolean get_seed_is_cable() {
        return _seed_is_cable;
    }

    public int get_arrivals_are_random() {
        return _arrivals_are_random;
    }

    public int get_departures_are_random() {
        return _departures_are_random;
    }

    public int get_num_univ_nodes() {
        return _num_univ_nodes;
    }

    public static int get_num_external_peers() {
        return _num_external_peers;
    }

    public int init_topology(String topo_file) {
        int ret = 0;
        _arrivals_are_random = 0;
        _departures_are_random = 0;
        _seed_is_cable = true;
        _tracker_type = 0;
        for (int i = 0; i < DEGREES_OF_CLOSENESS; i++) {
            g_closeness[i] = 0;
        }
        switch(_tracker_type) {
            case NORMAL_TRACKER:
                break;
            case SUB_TRACKER:
                _num_external_peers = 0;
                break;
            case SUB_LIMIT_TRACKER:
                _num_external_peers = 0;
                break;
            case UNIV_TRACKER:
                _num_external_peers = 0;
                break;
        }
        _tracker = new BT_Tracker();
        univ_sub = new BT_Subtracker();
        univ_sub.set_tracker(_tracker);
        _num_univ_nodes = UNIV_NODE_NUM;
        if (_num_univ_nodes > 0) {
            for (int i = 0; i < _num_univ_nodes; i++) {
                Univ_Node u_n = new Univ_Node(i, univ_sub);
                BT_Peer peer = new BT_Peer(i, BT_Peer.UNIV_NODE, 1, -1);
                peer.un = u_n;
                _univ_node.addLast(u_n);
            }
        }
        _num_as = AS_NUM;
        for (int i = 0; i < _num_as; i++) {
            As new_as = new As(i);
            int gateway_node_idx = _num_headend_nodes;
            new_as.low_client_id = _num_univ_nodes + _num_cable_leaf_nodes;
            new_as.num_nodes = _as_num_node;
            _num_headend_nodes += new_as.num_nodes;
            for (int j = gateway_node_idx; j < _num_headend_nodes; j++) {
                Headend_Node h_n = new Headend_Node(j);
                h_n.low_client_id = _num_univ_nodes + _num_cable_leaf_nodes;
                _num_cable_leaf_nodes += _as_office_fanout;
                h_n.hi_client_id = h_n.low_client_id + _as_office_fanout - 1;
                h_n.my_as = new_as;
                new_as.as_handend_node.addLast(h_n);
                _headend_node.addLast(h_n);
            }
            new_as.hi_client_id = _num_univ_nodes + _num_cable_leaf_nodes - 1;
            new_as.subtracker.set_tracker(_tracker);
            _as.addLast(new_as);
        }
        for (int i = 0; i < _num_cable_leaf_nodes; i++) {
            int id = i + _num_univ_nodes;
            Cable_Leaf_Node cln = new Cable_Leaf_Node(id);
            As as = Id_id_as(id);
            BT_Peer peer = new BT_Peer(id, BT_Peer.CABLE_NODE, 1, as.as_id);
            peer.cln = cln;
            _cable_leaf_node.addLast(cln);
        }
        init_all_nodes();
        num_clents = _num_univ_nodes + _num_cable_leaf_nodes;
        if (BT_Peer.instances.size() != num_clents) {
            System.out.println("_peers.size() != num_clents");
            System.exit(1);
        }
        _delay_matrix = new int[num_clents][num_clents];
        for (Iterator<Integer> it = BT_Peer.instances.keySet().iterator(); it.hasNext(); ) {
            int src_id = (Integer) it.next();
            BT_Peer src = BT_Peer.instance(src_id);
            for (Iterator<Integer> j = BT_Peer.instances.keySet().iterator(); j.hasNext(); ) {
                int dest_id = (Integer) j.next();
                if (src_id == dest_id) {
                    _delay_matrix[src_id][dest_id] = 0;
                    continue;
                }
                BT_Peer dest = BT_Peer.instance(dest_id);
                LinkedList<Link> all_link = link_traveler(src, dest);
                for (Iterator<Link> i = all_link.iterator(); i.hasNext(); ) {
                    Link l_tmp = (Link) i.next();
                    _delay_matrix[src_id][dest_id] += l_tmp.delay_ms;
                }
            }
        }
        for (Iterator<Integer> it = BT_Peer.instances.keySet().iterator(); it.hasNext(); ) {
            int peer_id = (Integer) it.next();
            BT_Peer bp = BT_Peer.instance(peer_id);
            bp.get_landmark();
        }
        _connection_map = new Conn[num_clents][num_clents];
        for (int i = 0; i < num_clents; i++) {
            for (int j = 0; j < num_clents; j++) {
                _connection_map[i][j] = null;
            }
        }
        _closeness_matrix = new int[num_clents][num_clents];
        for (int i = 0; i < num_clents; i++) {
            for (int j = 0; j < num_clents; j++) {
                _closeness_matrix[i][j] = CLOSE_NULL;
            }
        }
        for (Iterator<Integer> src_it = BT_Peer.instances.keySet().iterator(); src_it.hasNext(); ) {
            int src_id = (Integer) src_it.next();
            for (Iterator<Integer> dest_it = BT_Peer.instances.keySet().iterator(); dest_it.hasNext(); ) {
                int dest_id = (Integer) dest_it.next();
                _closeness_matrix[src_id][dest_id] = get_cloessness(src_id, dest_id);
                _closeness_matrix[dest_id][src_id] = _closeness_matrix[src_id][dest_id];
            }
        }
        _link_vec.clear();
        _xfer_vec.clear();
        return ret;
    }

    public static long get_delay(int src_id, int dest_id) {
        int type = _closeness_matrix[src_id][dest_id];
        if (type == INTRA_UNIV) {
            return 10;
        } else if (type == INTRA_HEADEND) {
            return 2;
        } else if (type == INTRA_AS) {
            return 10;
        } else if (type == INTER) {
            return 100;
        } else {
            System.out.println("get_delay, the connection type is CLOSE_NULL");
            System.exit(1);
            return 0;
        }
    }

    public int get_cloessness(int src_id, int dest_id) {
        if (src_id == dest_id) return CLOSE_NULL;
        BT_Peer sender = BT_Peer.instance(src_id);
        BT_Peer receiver = BT_Peer.instance(dest_id);
        if (sender.is_univ_node()) {
            if (receiver.is_univ_node()) {
                return INTRA_UNIV;
            } else {
                return INTER;
            }
        } else {
            if (receiver.is_univ_node()) {
                return INTER;
            } else {
                Cable_Leaf_Node cln = sender.cln;
                Cable_Virt virt = (Cable_Virt) cln.virt_link.recv_node;
                Headend_Node he = (Headend_Node) virt.headend_link.recv_node;
                if (dest_id >= he.low_client_id && dest_id <= he.hi_client_id) {
                    return INTRA_HEADEND;
                } else {
                    As as = he.my_as;
                    if (dest_id >= as.low_client_id && dest_id <= as.hi_client_id) {
                        return INTRA_AS;
                    } else {
                        return INTER;
                    }
                }
            }
        }
    }

    private void init_all_nodes() {
        for (Iterator<Univ_Node> j = _univ_node.iterator(); j.hasNext(); ) {
            Univ_Node un = (Univ_Node) j.next();
            init_univ(un);
        }
        for (Iterator<As> j = _as.iterator(); j.hasNext(); ) {
            As as_tmp = (As) j.next();
            init_as(as_tmp);
        }
    }

    private void init_as(As as_node) {
        as_node.gateway = as_node.as_handend_node.getFirst();
        As_Virt virt_node = as_node.virt_node;
        as_node.virt_link = set_link(AS_VIRT, as_node, virt_node);
        virt_node.as_link = set_link(VIRT_AS, virt_node, as_node);
        for (Iterator<Univ_Node> j = _univ_node.iterator(); j.hasNext(); ) {
            Univ_Node un = (Univ_Node) j.next();
            Link l = set_link(AS_UNIV, virt_node, un.virt_node);
            virt_node.univ_links.addLast(l);
        }
        for (Iterator<As> j = _as.iterator(); j.hasNext(); ) {
            As as_tmp = (As) j.next();
            if (as_node.as_id == as_tmp.as_id) continue;
            Link l = set_link(AS_AS, virt_node, as_tmp.virt_node);
            virt_node.gateway_links.addLast(l);
        }
        for (Iterator<Headend_Node> j = as_node.as_handend_node.iterator(); j.hasNext(); ) {
            Headend_Node he = (Headend_Node) j.next();
            init_headend(he, as_node);
        }
    }

    private void init_headend(Headend_Node he, As as_node) {
        for (Iterator<Headend_Node> j = as_node.as_handend_node.iterator(); j.hasNext(); ) {
            Headend_Node he_peer = (Headend_Node) j.next();
            if (he_peer.he_id == he.he_id) continue;
            Link l = set_link(HEADEND_HEADEND, he, he_peer);
            he.headend_links.addLast(l);
        }
        he.virt_link = set_link(HEADEND_VIRT, he, he.virt_node);
        he.virt_node.headend_link = set_link(VIRT_HEADEND, he.virt_node, he);
        int low_leaf_idx = he.low_client_id - _num_univ_nodes;
        int max_leaf_idx = he.hi_client_id - _num_univ_nodes;
        for (int i = low_leaf_idx; i <= max_leaf_idx; i++) {
            Cable_Leaf_Node cln = _cable_leaf_node.get(i);
            Link l = set_link(VIRT_LEAF, he.virt_node, cln);
            he.virt_node.cable_leaf_links.addLast(l);
        }
        for (int i = low_leaf_idx; i <= max_leaf_idx; i++) {
            Cable_Leaf_Node cln = _cable_leaf_node.get(i);
            cln.virt_link = set_link(LEAF_VIRT, cln, he.virt_node);
        }
    }

    private void init_univ(Univ_Node univ) {
        univ.virt_link = set_link(UNIV_VIRT, univ, univ.virt_node);
        Univ_Virt virt_node = univ.virt_node;
        virt_node.univ_link = set_link(VIRT_UNIV, virt_node, univ);
        for (Iterator<Univ_Node> j = _univ_node.iterator(); j.hasNext(); ) {
            Univ_Node un = (Univ_Node) j.next();
            if (un.u_n_id == univ.u_n_id) continue;
            Link l = set_link(UNIV_UNIV, virt_node, un.virt_node);
            virt_node.univ_links.addLast(l);
        }
        for (Iterator<As> j = _as.iterator(); j.hasNext(); ) {
            As as_tmp = (As) j.next();
            Link l = set_link(UNIV_AS, virt_node, as_tmp.virt_node);
            virt_node.gateway_links.addLast(l);
        }
    }

    public Link set_link(int linktype, Object sender, Object receiver) {
        Link link = new Link(linktype);
        link.bandwidth = 0;
        link.send_node = sender;
        link.recv_node = receiver;
        _links.addLast(link);
        if (link == null) {
            System.out.println("In set_link link == null ");
            System.exit(1);
        }
        return link;
    }

    public void remove_data_transfer(BT_Peer sender, BT_Peer receiver) {
        int sender_id = sender.my_node_id;
        int receiver_id = receiver.my_node_id;
        Conn data_conn = _connection_map[sender_id][receiver_id];
        LinkedList<Link> all_link = link_traveler(sender, receiver);
        for (Iterator<Link> i = all_link.iterator(); i.hasNext(); ) {
            Link l_tmp = (Link) i.next();
            if (!l_tmp.connections.contains(data_conn)) {
                System.out.println("The connections has not contained in the link");
                System.exit(1);
            }
            l_tmp.connections.remove(data_conn);
        }
        _connection_map[sender_id][receiver_id] = null;
    }

    public void add_data_transfer(BT_Peer sender, BT_Peer receiver) {
        int sender_id = sender.my_node_id;
        int receiver_id = receiver.my_node_id;
        if (_connection_map[sender_id][receiver_id] != null) {
            System.out.println("�Ѿ�����һ������ in _connection_map from " + sender_id + " to " + receiver_id);
            System.exit(1);
        }
        Conn data_conn = new Conn(sender_id, receiver_id);
        data_conn.tick_update = Simulator.GetRSimTimeMS();
        data_conn.closeness = _closeness_matrix[sender_id][receiver_id];
        _connection_map[sender_id][receiver_id] = data_conn;
        LinkedList<Link> all_link = link_traveler(sender, receiver);
        for (Iterator<Link> i = all_link.iterator(); i.hasNext(); ) {
            Link l_tmp = (Link) i.next();
            if (l_tmp.connections.contains(data_conn)) {
                System.out.println("The connections has been existing in the link");
                System.exit(1);
            }
            l_tmp.connections.addLast(data_conn);
        }
    }

    private LinkedList<Link> link_traveler(BT_Peer sender, BT_Peer receiver) {
        if (sender.is_univ_node()) {
            return univ_link_traveler(sender, receiver);
        } else {
            return cable_leaf_link_traveler(sender, receiver);
        }
    }

    private LinkedList<Link> cable_leaf_link_traveler(BT_Peer sender, BT_Peer receiver) {
        LinkedList<Link> result = new LinkedList<Link>();
        Cable_Leaf_Node send_cln = sender.cln;
        result.addLast(send_cln.virt_link);
        Cable_Virt send_cv = (Cable_Virt) send_cln.virt_link.recv_node;
        Headend_Node send_hn = (Headend_Node) send_cv.headend_link.recv_node;
        As send_as = Id_id_as(sender.my_node_id);
        if (receiver.un != null) {
            result.addLast(send_cv.headend_link);
            if (send_hn != send_as.gateway) {
                Link l_h_h = link_match(send_hn.headend_links, send_hn, send_as.gateway);
                result.addLast(l_h_h);
            }
            result.addLast(send_as.virt_link);
            Univ_Node recv_un = receiver.un;
            Univ_Virt recv_uv = recv_un.virt_node;
            Link l_av_uv = link_match(send_as.virt_node.univ_links, send_as.virt_node, recv_uv);
            result.addLast(l_av_uv);
            result.addLast(recv_uv.univ_link);
            return result;
        } else {
            As recv_as = Id_id_as(receiver.my_node_id);
            Headend_Node recv_hn = Id_id_Headend_Node(recv_as, receiver);
            Cable_Leaf_Node recv_cln = receiver.cln;
            if (send_as == recv_as) {
                if (recv_hn == send_hn) {
                    Link l_cv_c = link_match(send_cv.cable_leaf_links, send_cv, receiver.cln);
                    result.addLast(l_cv_c);
                    return result;
                }
                result.addLast(send_cv.headend_link);
                Link l_h_h = link_match(send_hn.headend_links, send_hn, recv_hn);
                result.addLast(l_h_h);
                result.addLast(recv_hn.virt_link);
                Link l_v_c = link_match(recv_hn.virt_node.cable_leaf_links, recv_hn.virt_node, recv_cln);
                result.addLast(l_v_c);
                return result;
            } else {
                result.addLast(send_cv.headend_link);
                if (send_hn != send_as.gateway) {
                    Link l_h_h = link_match(send_hn.headend_links, send_hn, send_as.gateway);
                    result.addLast(l_h_h);
                }
                result.addLast(send_as.virt_link);
                Link l_av_av = link_match(send_as.virt_node.gateway_links, send_as.virt_node, recv_as.virt_node);
                result.addLast(l_av_av);
                result.addLast(recv_as.virt_node.as_link);
                if (recv_hn != recv_as.gateway) {
                    Link l_g_h = link_match(recv_as.gateway.headend_links, recv_as.gateway, recv_hn);
                    result.addLast(l_g_h);
                }
                result.addLast(recv_hn.virt_link);
                Cable_Leaf_Node cln = receiver.cln;
                Link l_v_c = link_match(recv_hn.virt_node.cable_leaf_links, recv_hn.virt_node, cln);
                result.addLast(l_v_c);
                return result;
            }
        }
    }

    private LinkedList<Link> univ_link_traveler(BT_Peer sender, BT_Peer receiver) {
        LinkedList<Link> result = new LinkedList<Link>();
        Univ_Node sender_un = sender.un;
        result.addLast(sender_un.virt_link);
        Univ_Virt send_uv = sender_un.virt_node;
        if (receiver.is_univ_node()) {
            Univ_Node recv_un = receiver.un;
            Univ_Virt recv_uv = recv_un.virt_node;
            Link l = link_match(send_uv.univ_links, send_uv, recv_uv);
            result.addLast(l);
            result.addLast(recv_uv.univ_link);
            return result;
        } else {
            As as_in = Id_id_as(receiver.my_node_id);
            Link l = link_match(send_uv.gateway_links, send_uv, as_in.virt_node);
            result.addLast(l);
            result.addLast(as_in.virt_node.as_link);
            Headend_Node he_in = Id_id_Headend_Node(as_in, receiver);
            if (he_in != as_in.gateway) {
                Link l_g_h = link_match(as_in.gateway.headend_links, as_in.gateway, he_in);
                result.addLast(l_g_h);
            }
            result.addLast(he_in.virt_link);
            Cable_Leaf_Node cln = receiver.cln;
            Link l_v_c = link_match(he_in.virt_node.cable_leaf_links, he_in.virt_node, cln);
            result.addLast(l_v_c);
        }
        return result;
    }

    private Headend_Node Id_id_Headend_Node(As as_node, BT_Peer peer) {
        for (Iterator<Headend_Node> i = as_node.as_handend_node.iterator(); i.hasNext(); ) {
            Headend_Node he_tmp = (Headend_Node) i.next();
            if (peer.my_node_id >= he_tmp.low_client_id && peer.my_node_id <= he_tmp.hi_client_id) {
                return he_tmp;
            }
        }
        System.out.println("Can't find a Headend_Node for the receiver:" + peer);
        System.exit(1);
        return null;
    }

    public static As Id_id_as(int peer_id) {
        for (Iterator<As> i = _as.iterator(); i.hasNext(); ) {
            As as_tmp = i.next();
            if (peer_id >= as_tmp.low_client_id && peer_id <= as_tmp.hi_client_id) return as_tmp;
        }
        System.out.println("Can't find a As for the receiver:" + peer_id);
        System.exit(1);
        return null;
    }

    private Link link_match(LinkedList<Link> links, Object sender, Object receiver) {
        for (Iterator<Link> i = links.iterator(); i.hasNext(); ) {
            Link l = (Link) i.next();
            if (l.send_node == sender && l.recv_node == receiver) return l;
        }
        System.out.println("the links doesn't contain link for sender and receiver");
        System.exit(1);
        return null;
    }

    public void calc_data_transfers(long tick) {
        tick = Simulator.GetRSimTimeMS();
        _link_vec.clear();
        _xfer_vec.clear();
        for (Iterator<Link> i = _links.iterator(); i.hasNext(); ) {
            Link l_tmp = (Link) i.next();
            if (l_tmp.connections.isEmpty()) continue;
            l_tmp.bandwidth = _bandwidth[l_tmp.type];
            l_tmp.conns_copy.clear();
            for (Iterator<Conn> j = l_tmp.connections.iterator(); j.hasNext(); ) {
                Conn conn = (Conn) j.next();
                l_tmp.conns_copy.addLast(conn);
                g_closeness[conn.closeness]++;
            }
            _link_vec.addLast(l_tmp);
        }
        Link link = null;
        while (!_link_vec.isEmpty()) {
            link = _link_vec.getFirst();
            double more_bandwith = link.bandwidth / link.conns_copy.size();
            for (Iterator<Link> i = _link_vec.iterator(); i.hasNext(); ) {
                Link l_tmp = i.next();
                double band = l_tmp.bandwidth / l_tmp.conns_copy.size();
                if (band < more_bandwith) {
                    more_bandwith = band;
                    link = l_tmp;
                }
            }
            _link_vec.remove(link);
            if (!link.conns_copy.isEmpty()) {
                double bw_per_conn = link.bandwidth / (double) link.conns_copy.size();
                for (Iterator<Conn> j = link.conns_copy.iterator(); j.hasNext(); ) {
                    Conn conn = (Conn) j.next();
                    Conn_Bw conn_bw = new Conn_Bw();
                    conn_bw.conn = conn;
                    conn_bw.bandwidth = bw_per_conn;
                    BT_Peer sender = BT_Peer.instance(conn.sender_id);
                    BT_Peer receiver = BT_Peer.instance(conn.receiver_id);
                    LinkedList<Link> all_link = link_traveler(sender, receiver);
                    for (Iterator<Link> k = all_link.iterator(); k.hasNext(); ) {
                        Link l_tmp = (Link) k.next();
                        if (l_tmp.link_id == link.link_id) {
                            j.remove();
                        } else {
                            l_tmp.conns_copy.remove(conn);
                        }
                        if (!(conn_bw.bandwidth <= l_tmp.bandwidth + 100)) {
                            System.out.println("!(conn_bw.bandwidth <= link.bandwidth + 100)");
                            System.exit(1);
                        }
                        if (conn_bw.bandwidth > l_tmp.bandwidth) {
                            l_tmp.bandwidth = 0;
                        } else {
                            l_tmp.bandwidth -= conn_bw.bandwidth;
                        }
                    }
                    int bytes = (int) ((tick - conn.tick_update) * conn_bw.bandwidth) / Btsim.TICKS_PER_SECOND;
                    ;
                    conn.tick_update = tick;
                    Bytes_Transfer xfer = new Bytes_Transfer(conn.sender_id, conn.receiver_id, bytes, conn.closeness);
                    _xfer_vec.addLast(xfer);
                }
                link.conns_copy.clear();
                link = null;
                for (Iterator<Link> i = _link_vec.iterator(); i.hasNext(); ) {
                    Link l_tmp = i.next();
                    if (l_tmp.conns_copy.isEmpty()) {
                        i.remove();
                    }
                }
            }
        }
        while (!_xfer_vec.isEmpty()) {
            Bytes_Transfer xfer = _xfer_vec.removeLast();
            BT_Peer sender = BT_Peer.instance(xfer.sender_id);
            BT_Peer receiver = BT_Peer.instance(xfer.receiver_id);
            receiver.receive_bytes(sender, xfer.num_bytes, xfer.traffic_type);
        }
        update_bw_usage();
    }

    public static long _bw_log_events = 0;

    private void update_bw_usage() {
        double old_percent = (double) _bw_log_events / (double) (_bw_log_events + 1);
        double new_percent = 1 / (double) (_bw_log_events + 1);
        for (Iterator<Integer> it = BT_Peer.instances.keySet().iterator(); it.hasNext(); ) {
            int id = (Integer) it.next();
            BT_Peer peer = BT_Peer.instance(id);
            if (!peer.is_active_peer) continue;
            if (peer.is_univ_node()) {
                Univ_Node univ = peer.un;
                univ.virt_node.outgoing_bw_percent *= old_percent;
                double bw_used = _bandwidth[univ.virt_link.type] - univ.virt_link.bandwidth;
                double bw_avail = _bandwidth[univ.virt_link.type];
                univ.virt_node.outgoing_bw_percent += new_percent * bw_used / bw_avail;
                univ.virt_node.incoming_bw_percent *= old_percent;
                bw_used = _bandwidth[univ.virt_node.univ_link.type] - univ.virt_node.univ_link.bandwidth;
                bw_avail = _bandwidth[univ.virt_node.univ_link.type];
                univ.virt_node.incoming_bw_percent += new_percent * bw_used / bw_avail;
            } else {
                Cable_Leaf_Node leaf = peer.cln;
                leaf.outgoing_bw_percent *= old_percent;
                double bw_used = _bandwidth[leaf.virt_link.type] - leaf.virt_link.bandwidth;
                double bw_avail = _bandwidth[leaf.virt_link.type];
                leaf.outgoing_bw_percent += new_percent * bw_used / bw_avail;
                leaf.incoming_bw_percent *= old_percent;
                Cable_Virt virt_node = (Cable_Virt) leaf.virt_link.recv_node;
                Link link = link_match(virt_node.cable_leaf_links, virt_node, leaf);
                bw_used = _bandwidth[link.type] - link.bandwidth;
                bw_avail = _bandwidth[link.type];
                leaf.incoming_bw_percent += new_percent * bw_used / bw_avail;
            }
        }
        for (Iterator<As> it = _as.iterator(); it.hasNext(); ) {
            As as_tmp = (As) it.next();
            As_Virt virt_node = as_tmp.virt_node;
            virt_node.outgoing_bw_percent *= old_percent;
            double bw_used = _bandwidth[as_tmp.virt_link.type] - as_tmp.virt_link.bandwidth;
            double bw_avail = _bandwidth[as_tmp.virt_link.type];
            virt_node.outgoing_bw_percent += new_percent * bw_used / bw_avail;
            virt_node.incoming_bw_percent *= old_percent;
            bw_used = _bandwidth[virt_node.as_link.type] - virt_node.as_link.bandwidth;
            bw_avail = _bandwidth[virt_node.as_link.type];
            virt_node.incoming_bw_percent += new_percent * bw_used / bw_avail;
        }
        for (Iterator<Headend_Node> it = _headend_node.iterator(); it.hasNext(); ) {
            Headend_Node he = (Headend_Node) it.next();
            Cable_Virt virt_node = he.virt_node;
            virt_node.outgoing_bw_percent *= old_percent;
            double bw_used = _bandwidth[virt_node.headend_link.type] - virt_node.headend_link.bandwidth;
            double bw_avail = _bandwidth[virt_node.headend_link.type];
            virt_node.outgoing_bw_percent += new_percent * bw_used / bw_avail;
            virt_node.incoming_bw_percent *= old_percent;
            bw_used = _bandwidth[he.virt_link.type] - he.virt_link.bandwidth;
            bw_avail = _bandwidth[he.virt_link.type];
            virt_node.incoming_bw_percent += new_percent * bw_used / bw_avail;
        }
        _bw_log_events++;
    }
}
