package com.volantis.xml.expression.sequence;

import com.volantis.xml.expression.SequenceIndexOutOfBoundsException;
import com.volantis.xml.expression.ExpressionException;
import com.volantis.xml.expression.ExpressionFactory;
import com.volantis.xml.expression.atomic.StringValue;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This is a simple, generic implementation of the Sequence interface. It also
 * implements the collection interface to allow the sequence to be manipulated
 * via the standard java collection API.
 *
 * @see com.volantis.xml.expression.ExpressionFactory
 */
public class SimpleSequence implements Sequence, Collection {

    /**
     * Used for outputting whitespace in string and stream conversions
     */
    private static final char[] WHITESPACE = { ' ' };

    /**
     * Used to handle a sequence created with a null Item array
     */
    private static final Item[] NO_ITEMS = new Item[0];

    /**
     * The factory by which this sequence was created.
     */
    protected ExpressionFactory factory;

    /**
     * The items in this sequence
     */
    private Item[] items;

    /**
     * Initializes the new instance with the given parameters.
     *
     * @param factory the factory by which this sequence was created
     * @param items   the items that the sequence is to contain
     */
    public SimpleSequence(ExpressionFactory factory, Item[] items) {
        this.factory = factory;
        if (items != null) {
            this.items = new Item[items.length];
            System.arraycopy(items, 0, this.items, 0, items.length);
        } else {
            this.items = NO_ITEMS;
        }
    }

    public int getLength() {
        return items.length;
    }

    public Item getItem(int index) throws SequenceIndexOutOfBoundsException {
        if ((index < 1) || (index > items.length)) {
            throw new SequenceIndexOutOfBoundsException("index " + index + " is out of bounds" + ((items.length > 0) ? " (1.." + items.length + ")" : ""));
        } else {
            return items[index - 1];
        }
    }

    public StringValue stringValue() throws ExpressionException {
        StringBuffer result = new StringBuffer(items.length * 16);
        for (int i = 0; i < items.length; i++) {
            result.append(items[i].stringValue().asJavaString());
            if (i < items.length - 1) {
                result.append(WHITESPACE);
            }
        }
        return factory.createStringValue(result.toString());
    }

    public void streamContents(ContentHandler contentHandler) throws ExpressionException, SAXException {
        for (int i = 0; i < items.length; i++) {
            items[i].streamContents(contentHandler);
            if (i < items.length - 1) {
                contentHandler.characters(WHITESPACE, 0, WHITESPACE.length);
            }
        }
    }

    public Sequence getSequence() throws ExpressionException {
        return this;
    }

    public int size() {
        return items.length;
    }

    public boolean isEmpty() {
        return items.length == 0;
    }

    public boolean contains(Object o) {
        boolean result = false;
        for (int i = 0; !result && (i < items.length); i++) {
            result = (items[i] == o);
        }
        return result;
    }

    public Iterator iterator() {
        return new Iterator() {

            /**
             * The next index within the sequence (based on java indices, not
             * XPath indices) to be returned.
             */
            private int index = 0;

            public boolean hasNext() {
                return index < items.length;
            }

            public Object next() {
                if (index < items.length) {
                    return items[index++];
                } else {
                    throw new NoSuchElementException("The sequence only contains " + items.length + " items");
                }
            }

            /**
             * Item removal is not permitted by this iterator.
             *
             * @throws UnsupportedOperationException since the <tt>remove</tt>
             *         operation is not supported by this Iterator.
             */
            public void remove() {
                throw new UnsupportedOperationException("Cannot remove items from a sequence");
            }
        };
    }

    public Object[] toArray() {
        Object[] array = new Object[items.length];
        System.arraycopy(items, 0, array, 0, items.length);
        return array;
    }

    public Object[] toArray(Object a[]) {
        Object[] array = a;
        if (array.length < items.length) {
            array = (Object[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), items.length);
        }
        System.arraycopy(items, 0, array, 0, items.length);
        if (array.length > items.length) {
            array[items.length] = null;
        }
        return array;
    }

    public boolean add(Object o) {
        throw new UnsupportedOperationException("Cannot add items to a sequence");
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Cannot remove items from a sequence");
    }

    public boolean containsAll(Collection c) {
        Iterator iterator = c.iterator();
        boolean result = true;
        while (result && iterator.hasNext()) {
            result = contains(iterator.next());
        }
        return result;
    }

    public boolean addAll(Collection c) {
        throw new UnsupportedOperationException("Cannot add items to a sequence");
    }

    public boolean removeAll(Collection c) {
        throw new UnsupportedOperationException("Cannot remove items from a sequence");
    }

    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException("Cannot modify a sequence");
    }

    public void clear() {
        throw new UnsupportedOperationException("Cannot clear a sequence");
    }
}
