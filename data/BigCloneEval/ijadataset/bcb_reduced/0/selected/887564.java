package pogvue.analysis;

import pogvue.datamodel.AlignmentI;
import pogvue.datamodel.SequenceGroup;
import pogvue.datamodel.SequenceI;
import pogvue.datamodel.SequenceNode;
import pogvue.util.Comparison;
import pogvue.util.QuickSort;
import java.util.Vector;

/** Data structure to hold and manipulate a multiple sequence alignment
 */
public class AlignmentSorter {

    private AlignmentSorter() {
    }

    public static void sortGroups(AlignmentI align) {
        Vector groups = align.getGroups();
        int nGroup = groups.size();
        float[] arr = new float[nGroup];
        Object[] s = new Object[nGroup];
        for (int i = 0; i < nGroup; i++) {
            arr[i] = ((SequenceGroup) groups.elementAt(i)).getSize();
            s[i] = groups.elementAt(i);
        }
        QuickSort.sort(arr, s);
        Vector newg = new Vector(nGroup);
        for (int i = nGroup - 1; i >= 0; i--) {
            newg.addElement(s[i]);
        }
    }

    /**
	 * @param s
	 * @param align		*/
    public static void sortByPID(AlignmentI align, SequenceI s) {
        int nSeq = align.getHeight();
        float scores[] = new float[nSeq];
        SequenceI seqs[] = new SequenceI[nSeq];
        for (int i = 0; i < nSeq; i++) {
            scores[i] = Comparison.compare(align.getSequenceAt(i), s);
            seqs[i] = align.getSequenceAt(i);
        }
        QuickSort.sort(scores, 0, scores.length - 1, seqs);
        setReverseOrder(align, seqs);
    }

    private static void setReverseOrder(AlignmentI align, SequenceI[] seqs) {
        int nSeq = seqs.length;
        int len;
        if (nSeq % 2 == 0) {
            len = nSeq / 2;
        } else {
            len = (nSeq + 1) / 2;
        }
        for (int i = 0; i < len; i++) {
            align.getSequences().setElementAt(seqs[nSeq - i - 1], i);
            align.getSequences().setElementAt(seqs[i], nSeq - i - 1);
        }
    }

    private static void setOrder(AlignmentI align, Vector tmp) {
        setOrder(align, vectorToArray(tmp));
    }

    private static void setOrder(AlignmentI align, SequenceI[] seqs) {
        for (int i = 0; i < seqs.length; i++) {
            align.getSequences().setElementAt(seqs[i], i);
        }
    }

    /**
	 * @param align		*/
    public static void sortByID(AlignmentI align) {
        int nSeq = align.getHeight();
        String ids[] = new String[nSeq];
        SequenceI seqs[] = new SequenceI[nSeq];
        for (int i = 0; i < nSeq; i++) {
            ids[i] = align.getSequenceAt(i).getName();
            seqs[i] = align.getSequenceAt(i);
        }
        QuickSort.sort(ids, seqs);
        setReverseOrder(align, seqs);
    }

    public static void sortByGroup(AlignmentI align) {
        int nSeq = align.getHeight();
        Vector groups = align.getGroups();
        Vector seqs = new Vector();
        for (int i = 0; i < groups.size(); i++) {
            SequenceGroup sg = (SequenceGroup) groups.elementAt(i);
            for (int j = 0; j < sg.getSize(); j++) {
                seqs.addElement(sg.getSequenceAt(j));
            }
        }
        if (seqs.size() != nSeq) {
            System.err.println("ERROR: tmp.size() != nseq in sortByGroups");
            if (seqs.size() < nSeq) {
                addStrays(align, seqs);
            }
        }
        setOrder(align, seqs);
    }

    private static SequenceI[] vectorToArray(Vector tmp) {
        SequenceI[] seqs = new SequenceI[tmp.size()];
        for (int i = 0; i < tmp.size(); i++) {
            seqs[i] = (SequenceI) tmp.elementAt(i);
        }
        return seqs;
    }

    public static void sortByTree(AlignmentI align, NJTree tree) {
        int nSeq = align.getHeight();
        Vector tmp = new Vector();
        tmp = _sortByTree(tree.getTopNode(), tmp);
        if (tmp.size() != nSeq) {
            System.err.println("ERROR: tmp.size() != nseq in sortByTree");
            if (tmp.size() < nSeq) {
                addStrays(align, tmp);
            }
        }
        setOrder(align, tmp);
    }

    private static void addStrays(AlignmentI align, Vector seqs) {
        int nSeq = align.getHeight();
        for (int i = 0; i < nSeq; i++) {
            if (!seqs.contains(align.getSequenceAt(i))) {
                seqs.addElement(align.getSequenceAt(i));
            }
        }
        if (nSeq != seqs.size()) {
            System.err.println("ERROR: Size still not right even after addStrays");
        }
    }

    private static Vector _sortByTree(SequenceNode node, Vector tmp) {
        if (node == null) {
            return tmp;
        }
        SequenceNode left = (SequenceNode) node.left();
        SequenceNode right = (SequenceNode) node.right();
        if (left == null && right == null) {
            if (node.element() instanceof SequenceI) {
                tmp.addElement(node.element());
                return tmp;
            }
        } else {
            _sortByTree(left, tmp);
            _sortByTree(right, tmp);
        }
        return tmp;
    }
}
