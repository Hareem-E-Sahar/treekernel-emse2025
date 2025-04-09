package org.genxdm.bridgekit.atoms;

import org.genxdm.bridgekit.misc.AbstractUnaryList;

public abstract class XmlAbstractAtom extends AbstractUnaryList<XmlAtom> implements XmlAtom {

    public final XmlAtom get(final int index) {
        if (0 == index) {
            return this;
        } else {
            throw new ArrayIndexOutOfBoundsException(index);
        }
    }

    @SuppressWarnings("unchecked")
    public final <T> T[] toArray(T[] a) {
        final int size = size();
        if (a.length < size) {
            a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        }
        System.arraycopy(toArray(), 0, a, 0, size);
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    @Override
    public final String toString() {
        return getNativeType().getLocalName() + "('" + getC14NForm() + "')";
    }
}
