package de.parsemis.algorithms.gaston;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import de.parsemis.graph.HPGraph;
import de.parsemis.miner.chain.Extension;
import de.parsemis.miner.chain.ExtensionSet;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.general.HPEmbedding;

/**
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */
public class GastonTree<NodeType, EdgeType> extends GastonNode<NodeType, EdgeType> {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public static <NodeType, EdgeType> GastonTree<NodeType, EdgeType> create(final GastonPath<NodeType, EdgeType> backbone, Leg<NodeType, EdgeType> ack, final Collection<Leg<NodeType, EdgeType>> siblings) {
        final HPGraph<NodeType, EdgeType> path = backbone.toHPFragment().toHPGraph();
        final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment.env(backbone);
        final int nodeA = ack.getNodeA();
        final int nodeLabel = ack.ref.getToLabel();
        final int edgeLabel = ack.ref.getEdgeLabel();
        int ln = backbone.frontNode;
        int rn = backbone.backNode;
        int le = path.getNodeEdge(ln, 0);
        int re = path.getNodeEdge(rn, 0);
        {
            final int iln = path.getOtherNode(le, ln), irn = path.getOtherNode(re, rn);
            final int lel = path.getEdgeLabelIndex(le, env), rel = path.getEdgeLabelIndex(re, env);
            final int lnl = path.getNodeLabelIndex(ln, env), rnl = path.getNodeLabelIndex(rn, env);
            if ((nodeA == iln && (lel < edgeLabel || (lel == edgeLabel && lnl < nodeLabel))) || (nodeA == irn && (rel < edgeLabel || (rel == edgeLabel && rnl < nodeLabel)))) {
                return null;
            }
        }
        final int length = path.getMaxNodeIndex();
        final DepthRefinement nodes[] = new DepthRefinement[length];
        final DepthRefinement right[] = new DepthRefinement[length];
        final DepthRefinement bb[] = new DepthRefinement[length];
        final DepthRefinement rmp[] = new DepthRefinement[length];
        final int leftArrayLength = path.getNodeCount() / 2;
        final int lastRigthDepth = (path.getNodeCount() - 1) / 2;
        final int rpmNodes[] = new int[length];
        final BitSet rmpEqualsbb = new BitSet(length);
        int sym = 0;
        int tmp;
        boolean rightExtension = false;
        DepthRefinement lend, rend, lack, rack, tmpr;
        nodes[ln] = lack = lend = new DepthRefinement(ln, path.getEdgeLabelIndex(le, env), path.getNodeLabelIndex(ln, env), rpmNodes);
        nodes[rn] = rack = rend = new DepthRefinement(rn, path.getEdgeLabelIndex(re, env), path.getNodeLabelIndex(rn, env), rpmNodes);
        tmp = lack.compareLabels(rack);
        if (tmp != 0) {
            sym = tmp;
        }
        while (re != le) {
            ln = path.getOtherNode(le, ln);
            rn = path.getOtherNode(re, rn);
            if (rn == ln) {
                break;
            }
            for (int i = 0, oldEdge = le; le == oldEdge; i++) {
                le = path.getNodeEdge(ln, i);
            }
            for (int i = 0, oldEdge = re; re == oldEdge; i++) {
                re = path.getNodeEdge(rn, i);
            }
            lack.prev = nodes[ln] = tmpr = new DepthRefinement(ln, path.getEdgeLabelIndex(le, env), path.getNodeLabelIndex(ln, env), rpmNodes);
            lack = tmpr;
            rack.prev = nodes[rn] = tmpr = new DepthRefinement(rn, path.getEdgeLabelIndex(re, env), path.getNodeLabelIndex(rn, env), rpmNodes);
            rack = tmpr;
            tmp = lack.compareLabels(rack);
            if (tmp != 0) {
                sym = tmp;
            }
            rightExtension |= rn == nodeA;
        }
        if (sym == 0 && rightExtension) {
            return null;
        }
        if (sym < 0) {
            tmpr = lack;
            lack = rack;
            rack = tmpr;
            tmpr = lend;
            lend = rend;
            rend = tmpr;
        }
        if (rn == ln) {
            rack.prev = nodes[rn] = tmpr = new DepthRefinement(rn, path.getEdgeLabelIndex(le, env), path.getNodeLabelIndex(rn, env), rpmNodes);
            rack = tmpr;
        }
        lack.prev = rend;
        tmpr = lend;
        int i = 0;
        for (i = length - 1; tmpr != null; i--) {
            rpmNodes[i] = tmpr.nodeA;
            tmpr.nodeA = i;
            rmp[i] = bb[i] = tmpr;
            tmpr = tmpr.prev;
        }
        final Collection<Leg<NodeType, EdgeType>> newSiblings = new ArrayList<Leg<NodeType, EdgeType>>();
        for (final Leg<NodeType, EdgeType> cur : siblings) {
            if (cur.ref.isCycleRefinement()) {
                newSiblings.add(cur);
            } else {
                final int cdepth = nodes[cur.getNodeA()].getDepth() + 1;
                final DepthRefinement ref = new DepthRefinement(cdepth, cur.ref.getEdgeLabel(), cur.ref.getToLabel(), rpmNodes);
                if (cdepth != length && cdepth != lastRigthDepth + 1 && (cdepth != length - 1 || bb[length - 1].compareTo(ref) >= 0) && (cdepth != lastRigthDepth || bb[lastRigthDepth].compareTo(ref) >= 0)) {
                    final Leg<NodeType, EdgeType> nl = new Leg<NodeType, EdgeType>(ref, cur.frag);
                    newSiblings.add(nl);
                    if (cur == ack) {
                        ack = nl;
                    }
                }
            }
        }
        final int ackDepth = ack.ref.getDepth();
        rpmNodes[ackDepth] = ack.frag.correspondingNode;
        DepthRefinement npn = null, pnpn = null, splittNode = null;
        final DepthRefinement ackr = (DepthRefinement) ack.ref;
        int maxDepth;
        if (ackDepth > lastRigthDepth) {
            if (sym == 0) {
                pnpn = ackr;
            }
            ackr.prev = lend;
            lend = ackr;
            maxDepth = lastRigthDepth + leftArrayLength;
        } else {
            ackr.prev = rend;
            rend = ackr;
            maxDepth = lastRigthDepth;
        }
        tmp = bb[ackDepth].compareTo(ackr);
        splittNode = ackr;
        if (tmp == 0) {
            npn = next(bb[ackDepth], ackr);
        } else if (tmp < 0) {
            maxDepth--;
        }
        for (i = 0; i < ackDepth - 1; i++) {
            if (i != lastRigthDepth) {
                right[i] = rmp[i + 1];
            }
        }
        rmp[ackDepth] = right[ackDepth - 1] = (DepthRefinement) ack.ref;
        rmpEqualsbb.set(0, ackDepth - 1);
        rmpEqualsbb.set(ackDepth, bb[ackDepth].compareTo(ackr) == 0);
        lack.prev = null;
        return new GastonTree<NodeType, EdgeType>(backbone.getLevel() + 1, ack, newSiblings, bb, rmp, npn, pnpn, splittNode, lend, rend, leftArrayLength, lastRigthDepth, maxDepth, rpmNodes, right, rmpEqualsbb, backbone.getThreadNumber());
    }

    private static final DepthRefinement next(final DepthRefinement src, DepthRefinement leaf) {
        while (leaf != null && leaf.prev != src) {
            leaf = leaf.prev;
        }
        return leaf;
    }

    final DepthRefinement npn;

    final DepthRefinement pnpn;

    final DepthRefinement splittNode;

    final DepthRefinement lend;

    final DepthRefinement rend;

    final DepthRefinement bb[];

    final DepthRefinement rmp[];

    final DepthRefinement right[];

    final int rpmNodes[];

    final int leftArrayLength;

    final int maxRightDepth;

    final int maxDepth;

    final BitSet rmpEqualsbb;

    public GastonTree(final int level, final Leg<NodeType, EdgeType> leg, final Collection<Leg<NodeType, EdgeType>> siblings, final DepthRefinement bb[], final DepthRefinement rmp[], final DepthRefinement npn, final DepthRefinement pnpn, final DepthRefinement splittNode, final DepthRefinement lend, final DepthRefinement rend, final int leftArrayDepth, final int maxRightDepth, final int maxDepth, final int rpmNodes[], final DepthRefinement right[], final BitSet rmpEqualsbb, final int threadIdx) {
        super(level, leg, siblings, threadIdx);
        this.bb = bb;
        this.rmp = rmp;
        this.npn = npn;
        this.pnpn = pnpn;
        this.splittNode = splittNode;
        this.leftArrayLength = leftArrayDepth;
        this.maxRightDepth = maxRightDepth;
        this.maxDepth = maxDepth;
        this.lend = lend;
        this.rend = rend;
        this.rpmNodes = rpmNodes;
        this.right = right;
        this.rmpEqualsbb = rmpEqualsbb;
    }

    @Override
    public GastonNode<NodeType, EdgeType> extend(final Extension<NodeType, EdgeType> ext) {
        final ExtensionSet.Ext<NodeType, EdgeType, Leg<NodeType, EdgeType>> e = (ExtensionSet.Ext<NodeType, EdgeType, Leg<NodeType, EdgeType>>) ext;
        final Leg<NodeType, EdgeType> ack = e.getVal();
        final Collection<Leg<NodeType, EdgeType>> extensions = e.getSiblings();
        if (ack.ref.isCycleRefinement()) {
            return GastonCycle.create(this, ack, extensions);
        } else {
            int depth = ack.getDepth();
            final int[] nrpmNodes = ((DepthRefinement) ack.ref).rmpNodes;
            final DepthRefinement nrmp[] = rmp.clone();
            final DepthRefinement nright[] = right.clone();
            final DepthRefinement ackr = (DepthRefinement) ack.ref;
            final DepthRefinement leftSibling = nright[depth - 1];
            final BitSet rmpEbb = (BitSet) (rmpEqualsbb.clone());
            DepthRefinement splittN, nl, nr, nnpn, npnpn;
            int maxD;
            if (depth > maxRightDepth) {
                ackr.prev = lend;
                nl = ackr;
                nr = rend;
                maxD = maxRightDepth + leftArrayLength;
                npnpn = pnpn;
            } else {
                ackr.prev = rend;
                nl = lend;
                nr = ackr;
                maxD = maxRightDepth;
                if (pnpn != null && pnpn.compareTo(ackr, leftArrayLength) == 0) {
                    npnpn = next(pnpn, nl);
                    if (npnpn == null) {
                        npnpn = bb[maxRightDepth + 1];
                    }
                } else {
                    npnpn = null;
                }
            }
            if (npn != null && npn.compareTo(ackr) == 0) {
                nnpn = next(npn, ackr);
            } else if (leftSibling != null && leftSibling.compareTo(ackr) == 0) {
                nnpn = next(leftSibling, ackr);
            } else if (rmp[depth - 1] == bb[depth - 1] && bb[depth].compareTo(ackr) == 0) {
                nnpn = next(bb[depth], ackr);
            } else {
                nnpn = null;
            }
            if ((splittNode.compareTo(ackr) >= 0) || (rmp[depth - 1] == splittNode && bb[depth - 1].compareTo(splittNode) == 0)) {
                splittN = ackr;
                if (bb[depth].compareTo(ackr) < 0) {
                    maxD--;
                }
            } else {
                splittN = splittNode;
                maxD = maxDepth;
            }
            nrmp[depth] = nright[depth - 1] = ackr;
            rmpEbb.set(depth, rmpEbb.get(depth - 1) && bb[depth].compareTo(ackr) == 0);
            nright[depth++] = null;
            for (final int len = nright.length; depth < len && nright[depth] != null; depth++) {
                nrmp[depth] = nright[depth] = null;
                rmpEbb.clear(depth);
            }
            return new GastonTree<NodeType, EdgeType>(getLevel() + 1, ack, extensions, bb, nrmp, nnpn, npnpn, splittN, nl, nr, leftArrayLength, maxRightDepth, maxD, nrpmNodes, nright, rmpEbb, getThreadNumber());
        }
    }

    @Override
    public Collection<Extension<NodeType, EdgeType>> getExtensions() {
        final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment.env(this);
        final GastonEnvironment<NodeType, EdgeType> tenv = (GastonEnvironment<NodeType, EdgeType>) env.getThreadEnv(threadIdx);
        final HPGraph<NodeType, EdgeType> subGraph = toHPFragment().toHPGraph();
        ExtensionSet<NodeType, EdgeType, Leg<NodeType, EdgeType>> newLegs = new ExtensionSet<NodeType, EdgeType, Leg<NodeType, EdgeType>>();
        final Leg<NodeType, EdgeType> last = getLeg();
        boolean sort = false;
        assert tenv.check(threadIdx) : "URGS! " + Thread.currentThread() + " tenv:" + tenv.threadIdx + " shall:" + threadIdx;
        {
            final int depth = last.getDepth();
            final boolean npntest = (npn == null || (depth != maxDepth && bb[depth] == npn));
            for (final Leg<NodeType, EdgeType> l1 : siblings) {
                final boolean noPNPNtest = ((pnpn == null || l1.getDepth() > maxRightDepth || (l1.getDepth() == 1 && bb.length % 2 == 1 && bb[pnpn.getDepth()] == pnpn)));
                if (l1.ref.isCycleRefinement() || (l1.compareTo(last) <= 0 && (npntest || npn.compareTo(l1.ref) >= 0) && (noPNPNtest || pnpn.compareTo(l1.ref, leftArrayLength) >= 0))) {
                    final Leg<NodeType, EdgeType> l = l1.join(me, tenv);
                    newLegs.add(l);
                    sort = true;
                }
            }
        }
        {
            final int fromNode = last.frag.correspondingNode;
            final int depth = last.getDepth() + 1;
            final boolean test = (depth <= maxDepth && depth != maxRightDepth + 1);
            final boolean noNPNtest = test && (npn == null || (depth != maxDepth && bb[depth] == npn));
            final boolean noPNPNtest = test && (pnpn == null || depth > maxRightDepth);
            final boolean noLongestPathTest = test && (depth < maxDepth || (maxDepth != maxRightDepth && maxDepth != bb.length - 1) || !rmpEqualsbb.get(depth - 1));
            for (final HPEmbedding<NodeType, EdgeType> emb : me.frag) {
                final int superNode = ((GastonEmbedding<NodeType, EdgeType>) emb).getSuperNode();
                final HPGraph<NodeType, EdgeType> g = emb.getSuperGraph();
                final int d = g.getDegree(superNode);
                for (int i = 0; i < d; i++) {
                    final int edge = g.getNodeEdge(superNode, i);
                    final int oNode = g.getOtherNode(edge, superNode);
                    final int cNode = emb.getSubGraphNode(oNode);
                    final int nli = g.getNodeLabelIndex(oNode, env);
                    final int eli = g.getEdgeLabelIndex(edge, env);
                    if (nli >= 0 && eli >= 0) {
                        if (cNode == HPGraph.NO_NODE) {
                            if (test && (noNPNtest || npn.compareTo(depth, eli, nli) >= 0) && (noPNPNtest || pnpn.compareTo(depth, eli, nli, leftArrayLength) >= 0) && (noLongestPathTest || bb[depth].compareTo(depth, eli, nli) >= 0)) {
                                final Leg<NodeType, EdgeType> n = tenv.getDepth(fromNode, eli, nli, depth, me.frag.subgraph, rpmNodes);
                                n.frag.add(tenv.createEmbedding(emb, oNode));
                            }
                        } else if (tenv.doCycles && subGraph.getEdge(fromNode, cNode) == HPGraph.NO_EDGE) {
                            final Leg<NodeType, EdgeType> n = tenv.getCycle(fromNode, eli, cNode, me.frag.subgraph);
                            n.frag.add(tenv.createEmbedding(emb, HPGraph.NO_NODE));
                            sort = true;
                        }
                    }
                }
            }
        }
        newLegs = tenv.clearAndAddExtensions(newLegs);
        if (sort) {
            newLegs.sort(new Comparator<Leg<NodeType, EdgeType>>() {

                public int compare(final Leg<NodeType, EdgeType> o1, final Leg<NodeType, EdgeType> o2) {
                    return o2.compareTo(o1);
                }
            });
        }
        return newLegs;
    }

    @Override
    public String toString() {
        return LocalEnvironment.env(this).serializer.serialize(me.frag.subgraph.toGraph());
    }
}
