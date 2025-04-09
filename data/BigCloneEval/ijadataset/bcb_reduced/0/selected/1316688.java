package org.jenmo.core.adapter.spe;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import org.jenmo.core.adapter.AbstractAdapter.AdapterToSet;
import org.jenmo.core.domain.Node;
import org.jenmo.core.domain.NodeField;
import org.jenmo.core.domain.NodeRevision;
import org.jenmo.core.domain.SplitBlob;

/**
 * Implementations of {@link Set} of {@link SplitBlob} for {@link NodeRevision}.
 * <p>
 * A {@link NodeRevision} could accept new {@link SplitBlob} but cannot modify {@link SplitBlob} of
 * the revised {@link Node}. Such a behavior is managed by this class.
 * 
 * @author Nicolas Ocquidant
 * @since 1.0
 */
public class BlobIntoSetForRev implements Set<SplitBlob> {

    /** Blobs for the revised Node: unmodifiable */
    private AdapterToSet<SplitBlob, NodeField> blobsForRevised;

    /** Blobs for the revision Node: modifiable */
    private AdapterToSet<SplitBlob, NodeField> blobsForRevision;

    public BlobIntoSetForRev() {
    }

    public void decorate(AdapterToSet<SplitBlob, NodeField> blobsForRevised, AdapterToSet<SplitBlob, NodeField> blobsForRevision) {
        this.blobsForRevised = blobsForRevised;
        this.blobsForRevision = blobsForRevision;
    }

    @Override
    public boolean add(SplitBlob e) {
        if (blobsForRevised.contains(e)) {
            return false;
        }
        return blobsForRevision.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends SplitBlob> c) {
        boolean out = false;
        for (SplitBlob each : c) {
            out |= add(each);
        }
        return out;
    }

    @Override
    public void clear() {
        blobsForRevision.clear();
    }

    @Override
    public boolean contains(Object o) {
        if (blobsForRevision.contains(o)) {
            return true;
        }
        return blobsForRevised.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object each : c) {
            if (contains(each) == false) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isEmpty() {
        return (blobsForRevision.isEmpty() && blobsForRevised.isEmpty());
    }

    @Override
    public Iterator<SplitBlob> iterator() {
        return new DecoItr(blobsForRevised.iterator(), blobsForRevision.iterator());
    }

    @Override
    public boolean remove(Object o) {
        return blobsForRevision.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return blobsForRevision.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return blobsForRevision.retainAll(c);
    }

    @Override
    public int size() {
        return (blobsForRevision.size() + blobsForRevised.size());
    }

    @Override
    public Object[] toArray() {
        Object[] array1 = blobsForRevision.toArray();
        Object[] array2 = blobsForRevised.toArray();
        Object[] out = new Object[array1.length + array2.length];
        System.arraycopy(array1, 0, out, 0, array1.length);
        System.arraycopy(array2, 0, out, array1.length, array2.length);
        return out;
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public <T> T[] toArray(T[] a) {
        a = blobsForRevision.toArray(a);
        T[] other = (T[]) Array.newInstance(a.getClass().getComponentType(), blobsForRevised.size());
        other = blobsForRevised.toArray(other);
        System.arraycopy(other, 0, a, blobsForRevision.size(), other.length);
        return other;
    }

    private class DecoItr implements Iterator<SplitBlob> {

        Iterator<SplitBlob> itForRevised;

        Iterator<SplitBlob> itForRevision;

        Iterator<SplitBlob> itCurrent;

        DecoItr(Iterator<SplitBlob> itForRevised, Iterator<SplitBlob> itForRevision) {
            this.itForRevised = itForRevised;
            this.itForRevision = itForRevision;
            itCurrent = itForRevision;
        }

        private void checkCurrent() {
            if (itCurrent == itForRevision) {
                itCurrent = itForRevised;
            }
        }

        @Override
        public boolean hasNext() {
            if (itCurrent.hasNext() == true) {
                return true;
            }
            checkCurrent();
            return itCurrent.hasNext();
        }

        @Override
        public SplitBlob next() {
            return itCurrent.next();
        }

        @Override
        public void remove() {
            if (itCurrent == itForRevision) {
                itCurrent.remove();
            }
        }
    }
}
