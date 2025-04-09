package edu.ksu.cis.util.graph.core;

public class GraphEntry {

    private Vertex _VertexEntry;

    private int _Children[];

    private int _Parents[];

    private int _ChildrenSize;

    private int _ParentsSize;

    private Vertex[] _ParentsVertexCache;

    private Vertex[] _ChildrenVertexCache;

    private boolean _ParentsCacheIsDirty;

    private boolean _ChildrensCacheIsDirty;

    Vertex v() {
        return _VertexEntry;
    }

    GraphEntry(Vertex V) {
        _VertexEntry = V;
        _ChildrenSize = 0;
        _ParentsSize = 0;
        _Children = new int[10];
        _Parents = new int[10];
        _ParentsCacheIsDirty = true;
        _ChildrensCacheIsDirty = true;
    }

    private void buffer(int b) {
        int[] nc = new int[_Children.length + b];
        int[] np = new int[_Parents.length + b];
        for (int i = 0; i < _ChildrenSize; i++) {
            nc[i] = _Children[i];
        }
        for (int i = 0; i < _ParentsSize; i++) {
            np[i] = _Parents[i];
        }
        _Children = nc;
        _Parents = np;
    }

    void Compact() {
        int[] nc = new int[_ChildrenSize];
        int[] np = new int[_ParentsSize];
        for (int i = 0; i < _ChildrenSize; i++) {
            nc[i] = _Children[i];
        }
        for (int i = 0; i < _ParentsSize; i++) {
            np[i] = _Parents[i];
        }
        _Children = nc;
        _Parents = np;
        _ParentsCacheIsDirty = true;
        _ChildrensCacheIsDirty = true;
    }

    private void swap(int[] a, int i, int j) {
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
    }

    void mapParent(int par) {
        if (canNavigatedByParent(par)) return;
        if (_Parents.length - 2 < _ParentsSize) buffer(5);
        _Parents[_ParentsSize] = par;
        int i = _ParentsSize;
        while (i >= 1 && _Parents[i] < _Parents[i - 1]) {
            swap(_Parents, i, i - 1);
            i--;
        }
        _ParentsCacheIsDirty = true;
        _ParentsSize++;
    }

    void mapChild(int chi) {
        if (canNavigateByChild(chi)) return;
        if (_Children.length - 2 < _ChildrenSize) buffer(5);
        _Children[_ChildrenSize] = chi;
        int i = _ChildrenSize;
        while (i >= 1 && _Children[i] < _Children[i - 1]) {
            swap(_Children, i, i - 1);
            i--;
        }
        _ChildrensCacheIsDirty = true;
        _ChildrenSize++;
    }

    void unmapParent(int par) {
        for (int i = 0; i < _ParentsSize; i++) {
            if (par == _Parents[i]) {
                _Parents[i] = -1;
            }
        }
        int b = 0;
        for (int i = 0; i < _ParentsSize; i++) {
            _Parents[i - b] = _Parents[i];
            if (_Parents[i] == -1) b++;
        }
        _ParentsSize -= b;
        _ParentsCacheIsDirty = true;
    }

    void empty() {
        _ChildrenSize = 0;
        _ChildrensCacheIsDirty = true;
        _ParentsSize = 0;
        _ParentsCacheIsDirty = true;
    }

    void unmapChild(int chi) {
        for (int i = 0; i < _ChildrenSize; i++) {
            if (chi == _Children[i]) _Children[i] = -1;
        }
        int b = 0;
        for (int i = 0; i < _ChildrenSize; i++) {
            _Children[i - b] = _Children[i];
            if (_Children[i] == -1) b++;
        }
        _ChildrenSize -= b;
        _ChildrensCacheIsDirty = true;
    }

    void remap(int vold, int vnew) {
        if (canNavigatedByParent(vold)) {
            unmapParent(vold);
            mapParent(vnew);
        }
        if (canNavigateByChild(vold)) {
            unmapChild(vold);
            mapChild(vnew);
        }
    }

    Vertex[] getParents(GraphEntry[] total) {
        if (_ParentsCacheIsDirty) {
            _ParentsVertexCache = new Vertex[_ParentsSize];
            for (int i = 0; i < _ParentsSize; i++) _ParentsVertexCache[i] = total[_Parents[i]].v();
            _ParentsCacheIsDirty = false;
        }
        return _ParentsVertexCache;
    }

    Vertex[] getChildren(GraphEntry[] total) {
        if (_ChildrensCacheIsDirty) {
            _ChildrenVertexCache = new Vertex[_ChildrenSize];
            for (int i = 0; i < _ChildrenSize; i++) _ChildrenVertexCache[i] = total[_Children[i]].v();
            _ChildrensCacheIsDirty = false;
        }
        return _ChildrenVertexCache;
    }

    int parentsSize() {
        return _ParentsSize;
    }

    int childrenSize() {
        return _ChildrenSize;
    }

    boolean canNavigateByChild(int c) {
        int high = _ChildrenSize;
        int low = -1;
        while ((high - low) > 1) {
            int p = (high + low) / 2;
            if (c < _Children[p]) {
                high = p;
            } else if (_Children[p] < c) {
                low = p;
            } else return true;
        }
        if (low >= 0 && _Children[low] == c) return true;
        return false;
    }

    boolean canNavigatedByParent(int c) {
        int high = _ParentsSize;
        int low = -1;
        while ((high - low) > 1) {
            int p = (high + low) / 2;
            if (c < _Parents[p]) {
                high = p;
            } else if (_Parents[p] < c) {
                low = p;
            } else return true;
        }
        if (low >= 0 && _Parents[low] == c) return true;
        return false;
    }

    void applyOrdering(int[] order) {
        int i;
        for (i = 0; i < _ChildrenSize; i++) {
            _Children[i] = order[_Children[i]];
        }
        for (i = 0; i < _ParentsSize; i++) {
            _Parents[i] = order[_Parents[i]];
        }
        _ParentsCacheIsDirty = true;
        _ChildrensCacheIsDirty = true;
    }
}
