public class Test {    public NodeSet selectFollowingSiblings(NodeSet contextSet, int contextId) {
        sort();
        NodeSet result = new NewArrayNodeSet();
        for (NodeProxy reference : contextSet) {
            NodeId parentId = reference.getNodeId().getParentId();
            int docIdx = findDoc(reference.getDocument());
            if (docIdx < 0) return NodeSet.EMPTY_SET;
            int low = documentOffsets[docIdx];
            int high = low + (documentLengths[docIdx] - 1);
            int end = low + documentLengths[docIdx];
            int mid = low;
            int cmp;
            NodeProxy p;
            while (low <= high) {
                mid = (low + high) / 2;
                p = nodes[mid];
                if (p.getNodeId().isDescendantOf(parentId)) {
                    break;
                }
                cmp = p.getNodeId().compareTo(parentId);
                if (cmp > 0) {
                    high = mid - 1;
                } else {
                    low = mid + 1;
                }
            }
            if (low > high) {
                continue;
            }
            while (mid > documentOffsets[docIdx] && nodes[mid - 1].getNodeId().compareTo(parentId) > -1) {
                --mid;
            }
            NodeId refId = reference.getNodeId();
            for (int i = mid; i < end; i++) {
                NodeId currentId = nodes[i].getNodeId();
                if (!currentId.isDescendantOf(parentId)) break;
                if (currentId.getTreeLevel() == refId.getTreeLevel() && currentId.compareTo(refId) > 0) {
                    if (Expression.IGNORE_CONTEXT != contextId) {
                        if (Expression.NO_CONTEXT_ID == contextId) {
                            nodes[i].copyContext(reference);
                        } else {
                            nodes[i].addContextNode(contextId, reference);
                        }
                    }
                    result.add(nodes[i]);
                }
            }
        }
        return result;
    }
}