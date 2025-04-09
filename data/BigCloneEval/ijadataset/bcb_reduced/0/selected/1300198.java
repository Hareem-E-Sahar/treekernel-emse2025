package PddOriginblock;

import java.io.PrintStream;
import java.util.*;

public class Node {

    private Edge _inEdge;

    private int _label;

    private ArrayList<Node> _children;

    private boolean _isLeaf;

    private Set<Integer[]> _rule_list;

    private int _sig;

    private int _seq;

    public Node(Edge e, int nodeLabel, boolean iamLeaf) {
        if (e == null) _inEdge = new Edge(-1); else _inEdge = new Edge(e);
        _label = nodeLabel;
        _isLeaf = iamLeaf;
        _children = new ArrayList<Node>();
        _rule_list = new HashSet<Integer[]>();
    }

    public Node(Edge e, int nodeLabel, boolean iamLeaf, Set<Integer[]> origins) {
        if (e == null) _inEdge = new Edge(-1); else _inEdge = new Edge(e);
        _label = nodeLabel;
        _isLeaf = iamLeaf;
        _children = new ArrayList<Node>();
        _rule_list = new HashSet<Integer[]>();
        _rule_list.addAll(origins);
    }

    public void copy(Edge e, int nodeLabel, boolean iamLeaf, Set<Integer[]> origins) {
        if (e == null) _inEdge = new Edge(-1); else _inEdge = new Edge(e);
        _label = nodeLabel;
        _isLeaf = iamLeaf;
        setOrigins(origins);
    }

    public void setOrigins(Set<Integer[]> origins) {
        _rule_list.clear();
        _rule_list.addAll(origins);
    }

    public void addOrigins(Set<Integer[]> origins) {
        _rule_list.addAll(origins);
    }

    public Edge getInEdge() {
        return _inEdge;
    }

    public Interval getInterval(int i) {
        return (Interval) _inEdge.get(i);
    }

    public void setInEdge(Edge e) {
        _inEdge = e;
    }

    public int getLabel() {
        return _label;
    }

    public Set<Integer[]> getOrigins() {
        return _rule_list;
    }

    public void setLabel(int i) {
        _label = i;
    }

    public boolean isLeaf() {
        return _isLeaf;
    }

    public void addChild(Node son) {
        _children.add(son);
    }

    public void addChild(int i, Node son) {
        _children.add(i, son);
    }

    public Node getChild(int i) {
        return (Node) _children.get(i);
    }

    public ArrayList getAllChild() {
        return _children;
    }

    public int getChildrenNum() {
        return _children.size();
    }

    public Node deepCopy() {
        Node dest = new Node(_inEdge, _label, isLeaf());
        if (isLeaf()) {
            dest.setOrigins(_rule_list);
        } else {
            for (int i = 0; i < _children.size(); i++) dest.addChild(getChild(i).deepCopy());
        }
        return dest;
    }

    public void printInEdge() {
        _inEdge.print();
    }

    public void allMatchAppendAtomicRule(Rule r, int beginPos, String alg) {
        if (beginPos == r.size()) {
            if (alg.equals("deny-overrides")) {
                if (r.getDecision() == 0) setLabel(0);
                addOrigins(r.getOrigins());
            } else if (alg.equals("permit-overrides")) {
                if (r.getDecision() == 1) setLabel(1);
                addOrigins(r.getOrigins());
            } else if (alg.equals("first-applicable")) {
                addOrigins(r.getOrigins());
            }
            return;
        }
        if (_label != r.getLabel(beginPos)) System.out.println("Label = " + _label + ", but r.getLabel( beginPos ) = " + r.getLabel(beginPos)); else {
            Interval Si = new Interval((Interval) (r.get(beginPos).get(0)));
            Edge oldEdges = new Edge(_label);
            for (int i = 0; i < getChildrenNum(); i++) oldEdges.addAll(getChild(i).getInEdge());
            Edge newEdge = new Edge(_label, Si.minus(oldEdges));
            if (beginPos <= r.size() - 1) {
                for (int j = 0; j < _children.size(); j++) {
                    Node n = getChild(j);
                    Edge Ei = n.getInEdge();
                    if (Ei.isSubsetOf(Si)) n.allMatchAppendAtomicRule(r, beginPos + 1, alg); else if (Ei.overlap(Si)) {
                        ArrayList overlap = Ei.intersection(Si);
                        Node copy = n.deepCopy();
                        Edge e = copy.getInEdge();
                        e.clear();
                        e.addAll(overlap);
                        addChild(copy);
                        Ei.minus(Si);
                        copy.allMatchAppendAtomicRule(r, beginPos + 1, alg);
                    }
                }
            }
            if (newEdge.size() != 0) {
                Node newSon = r.buildChain(beginPos + 1);
                newSon.setInEdge(newEdge);
                addChild(newSon);
            }
        }
    }

    public void appendAtomicRule(Rule r, int beginPos) {
        if (_label != r.getLabel(beginPos)) {
            System.out.println((new StringBuilder("Label = ")).append(_label).append(", but r.getLabel( beginPos ) = ").append(r.getLabel(beginPos)).toString());
        } else {
            Interval Si = new Interval((Interval) r.get(beginPos).get(0));
            Edge oldEdges = new Edge(_label);
            for (int i = 0; i < getChildrenNum(); i++) oldEdges.addAll(getChild(i).getInEdge());
            Edge newEdge = new Edge(_label, Si.minus(oldEdges));
            if (beginPos < r.size() - 1) {
                for (int j = 0; j < _children.size(); j++) {
                    Node n = getChild(j);
                    Edge Ei = n.getInEdge();
                    if (Ei.isSubsetOf(Si)) n.appendAtomicRule(r, beginPos + 1); else if (Ei.overlap(Si)) {
                        ArrayList overlap = Ei.intersection(Si);
                        Node copy = n.deepCopy();
                        Edge e = copy.getInEdge();
                        e.clear();
                        e.addAll(overlap);
                        addChild(copy);
                        Ei.minus(Si);
                        copy.appendAtomicRule(r, beginPos + 1);
                    }
                }
            }
            if (newEdge.size() != 0) {
                Node newSon = r.buildChain(beginPos + 1);
                newSon.setInEdge(newEdge);
                addChild(newSon);
            }
        }
    }

    public Sequence deriveRules() {
        Rule rule = new Rule();
        Sequence result = new Sequence();
        derive(this, rule, result);
        return result;
    }

    private void derive(Node v, Rule rule, Sequence result) {
        if (v.isLeaf()) {
            rule.setDecision(v.getLabel());
            result.add(new Rule(rule));
            rule.setDecision(-1);
        } else {
            for (int i = 0; i < v.getChildrenNum(); i++) {
                Node son = v.getChild(i);
                rule.add(new Edge(son.getInEdge()));
                derive(son, rule, result);
                rule.removeLastEdge();
            }
        }
    }

    public void sort() {
        for (int i = 1; i < getChildrenNum(); i++) {
            for (int j = i; j > 0; j--) {
                Node nj = getChild(j);
                Interval Sj = nj.getInterval(0);
                long leftj = Sj.getLeft();
                Node nj1 = getChild(j - 1);
                Interval Sj1 = nj1.getInterval(0);
                long leftj1 = Sj1.getLeft();
                if (leftj >= leftj1) break;
                _children.remove(j);
                _children.add(j - 1, nj);
            }
        }
        for (int i = 0; i < getChildrenNum(); i++) {
            Node son = getChild(i);
            son.sort();
        }
    }

    public void simplify() {
        int m = _children.size();
        for (int i = 0; i < m; i++) {
            Node son = getChild(i);
            for (Edge e = son.getInEdge(); e.size() > 1; e.remove(1)) {
                Interval S = (Interval) e.get(1);
                Node brother = son.deepCopy();
                brother.setInEdge(new Edge(_label, S));
                _children.add(brother);
            }
        }
        for (int i = 0; i < getChildrenNum(); i++) {
            Node son = getChild(i);
            son.simplify();
        }
    }

    public long get_size() {
        long size = 0;
        if (_isLeaf) {
            size += 4;
            size += 1;
            size += 4 * 2 * _rule_list.size();
        } else {
            size += 4 * 2 * _inEdge.size();
            size += 4;
            size += 1;
        }
        return size;
    }

    public long get_memory_size() {
        Node current = this;
        if (current.isLeaf()) {
            return current.get_size();
        }
        long memory_size = 0;
        memory_size += current.get_size();
        for (int i = 0; i < current.getChildrenNum(); i++) {
            Node nj = current.getChild(i);
            memory_size += nj.get_memory_size();
        }
        return memory_size;
    }

    public int binary_query(long query[], Set<Integer[]> origins) {
        Node current = this;
        for (int i = 0; i < query.length; i++) {
            long v = query[i];
            boolean matched = false;
            int a = 0;
            int b = current.getChildrenNum() - 1;
            int s = (a + b) / 2;
            while (a <= b) {
                Node nj = current.getChild(s);
                Interval Sj = nj.getInterval(0);
                long leftj = Sj.getLeft();
                long rightj = Sj.getRight();
                if (leftj <= v && v <= rightj) {
                    current = nj;
                    matched = true;
                    break;
                }
                if (v < leftj) {
                    b = s - 1;
                    s = (a + b) / 2;
                } else {
                    a = s + 1;
                    s = (a + b) / 2;
                }
            }
            if (!matched) {
                return -1;
            }
        }
        if (current.isLeaf()) {
            origins.addAll(current.getOrigins());
            return current.getLabel();
        } else {
            System.out.println("Error in Node.java binary_query. Did not reach leaf.");
            return -1;
        }
    }

    public void print() {
        System.out.print("Node:");
        if (_inEdge != null) _inEdge.print();
        System.out.println();
        for (int i = 0; i < getChildrenNum(); i++) getChild(i).print();
    }

    public void appendAtomicRuleForDownward(Rule r, int beginPos) {
        appendAtomicRuleForDownward(r, beginPos, 0);
    }

    public void appendAtomicRuleForDownward(Rule r, int beginPos, int rule_num) {
        if (_label != r.getLabel(beginPos)) {
            System.out.println((new StringBuilder("Label = ")).append(_label).append(", but r.getLabel( beginPos ) = ").append(r.getLabel(beginPos)).toString());
        } else {
            Interval Si = new Interval((Interval) r.get(beginPos).get(0));
            if (beginPos < r.size() - 1) {
                for (int j = 0; j < getChildrenNum(); j++) {
                    Node n = getChild(j);
                    Edge Ei = n.getInEdge();
                    if (Ei.isSubsetOf(Si)) n.appendAtomicRuleForDownward(r, beginPos + 1); else if (Ei.overlap(Si)) {
                        ArrayList overlap = Ei.intersection(Si);
                        Node copy = n.deepCopy();
                        Edge e = copy.getInEdge();
                        e.clear();
                        e.addAll(overlap);
                        addChild(copy);
                        Ei.minus(Si);
                        copy.appendAtomicRuleForDownward(r, beginPos + 1);
                    }
                }
            } else {
                for (int j = 0; j < getChildrenNum(); j++) {
                    Node n = getChild(j);
                    Edge Ei = n.getInEdge();
                    Ei.minus(Si);
                    if (Ei.isEmpty()) {
                        _children.remove(j);
                        j--;
                    }
                }
                addChild(new Node(new Edge(getLabel(), Si), r.getDecision(), true));
            }
        }
    }

    public int get_sig() {
        return _sig;
    }

    public int simple_reduction() {
        if (isLeaf()) {
            _sig = getLabel();
            return _sig;
        } else {
            for (int i = 0; i < _children.size(); i++) {
                Node t_child = getChild(i);
                t_child.simple_reduction();
            }
            boolean[] useful = new boolean[_children.size()];
            for (int i = 0; i < _children.size(); i++) useful[i] = true;
            for (int j = 1; j < _children.size(); j++) {
                Node tj = getChild(j);
                int sj = tj.get_sig();
                for (int i = 0; i < j; i++) {
                    Node ti = getChild(i);
                    if (sj == ti.get_sig()) {
                        ti.getInEdge().add(tj.getInEdge());
                        useful[j] = false;
                        break;
                    }
                }
            }
            ArrayList new_children = new ArrayList();
            for (int i = 0; i < _children.size(); i++) if (useful[i]) {
                new_children.add(_children.get(i));
            }
            _children = new_children;
            String s = "";
            for (int i = 0; i < _children.size(); i++) {
                Node t = (Node) _children.get(i);
                s += Integer.toString(t.get_sig());
                s += Integer.toString(t.getInEdge().hashCode());
            }
            _sig = s.hashCode();
            return _sig;
        }
    }

    public int get_seq() {
        return _seq;
    }

    public void traverse_set_seq(int[] num, int level) {
        if (!isLeaf()) {
            _seq = num[level];
            num[level]++;
            for (int i = 0; i < _children.size(); i++) {
                ((Node) _children.get(i)).traverse_set_seq(num, level + 1);
            }
        }
    }

    public void traverse_set_quicktable(int[][][] quicktable, ArrayList<ArrayList<Node>> table3, int level, int father_seq) {
        if (isLeaf()) {
            Edge e = getInEdge();
            for (int i = 0; i < e.size(); i++) for (int j = (int) ((Interval) e.get(i)).getLeft(); j <= (int) ((Interval) e.get(i)).getRight(); j++) {
                table3.get(father_seq).get(j).copy(e, _label, true, _rule_list);
            }
        } else {
            for (int i = 0; i < _children.size(); i++) {
                if (father_seq != -1) {
                    for (int k = 0; k < _inEdge.size(); k++) for (int j = (int) ((Interval) _inEdge.get(k)).getLeft(); j <= (int) ((Interval) _inEdge.get(k)).getRight(); j++) quicktable[level - 1][father_seq][j] = _seq;
                }
                ((Node) _children.get(i)).traverse_set_quicktable(quicktable, table3, level + 1, _seq);
            }
        }
    }
}
