public class Test {    private static BTree optimalBST(Comparable[] sorted, int left, int right) {
        BTree r = new BTree();
        BTreeItr ri = r.root();
        if (left > right) return r;
        int mid = (left + right) / 2;
        ri.insert(sorted[mid]);
        ri.left().paste(optimalBST(sorted, left, mid - 1));
        ri.right().paste(optimalBST(sorted, mid + 1, right));
        return r;
    }
}