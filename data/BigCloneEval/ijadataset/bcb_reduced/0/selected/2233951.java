package org.lemma.set;

import java.io.Serializable;
import java.util.Arrays;
import javax.annotation.Author;
import javax.annotation.Copyright;
import javax.annotation.Version;
import org.lemma.number.Natural;

/**
 * <p>
 *  TODO: Javadoc for {@code Tuple}
 * </p>
 *
 * @author Chris Beatty [christopher.beatty@gmail.com]
 * @version 1.0.0
 */
@Version(major = "1", minor = "0", patch = "0", date = "Nov 12, 2008 1:00:48 PM", authors = { @Author(name = "Chris Beatty", email = "christopher.beatty@gmail.com") })
@Copyright
public class Tuple<D1, D2> implements Serializable, Cloneable {

    protected D1 car = null;

    protected D2 cdr = null;

    protected Tuple() {
        this.car = null;
        this.cdr = null;
    }

    public Tuple(D1 first, D2 rest) {
        this();
        this.car = first;
        this.cdr = rest;
    }

    public Natural dimension() {
        Object[] array = this.toArray();
        return (array != null) ? Natural.ZERO : new Natural(array.length);
    }

    public D1 car() {
        return this.car;
    }

    public D2 cdr() {
        return this.cdr;
    }

    public <D0> Tuple<D0, Tuple<D1, D2>> prepend(D0 element) {
        return prepend(element, this);
    }

    public static <D0, D1, D2> Tuple<D0, Tuple<D1, D2>> prepend(D0 element, Tuple<D1, D2> tuple) {
        return new Tuple<D0, Tuple<D1, D2>>(element, tuple);
    }

    private static <T> T[] concat(T[] a, T[] b) {
        final int alen = a.length;
        final int blen = b.length;
        if (alen == 0) {
            return b;
        }
        if (blen == 0) {
            return a;
        }
        final T[] result = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), alen + blen);
        System.arraycopy(a, 0, result, 0, alen);
        System.arraycopy(b, 0, result, alen, blen);
        return result;
    }

    private static <T> boolean equals(T o1, T o2) {
        if (o1 == null && o2 == null) {
            return true;
        } else if (o1 == null && o2 != null) {
            return false;
        } else if (o1 != null && o2 == null) {
            return false;
        } else {
            return o1.equals(o2);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Tuple<D1, D2> o = (Tuple<D1, D2>) obj;
        return equals(this.car(), o.car()) && equals(this.cdr(), o.cdr());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Arrays.hashCode(this.toArray());
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        if (this.car() != null) {
            sb.append(this.car());
        }
        if (this.cdr() != null) {
            sb.append(' ');
            sb.append(this.cdr());
        }
        sb.append(')');
        return sb.toString();
    }

    public Object[] toArray() {
        Object[] a1 = (car instanceof Tuple) ? ((Tuple) car).toArray() : new Object[] { car };
        Object[] a2 = (cdr instanceof Tuple) ? ((Tuple) cdr).toArray() : new Object[] { cdr };
        return concat(a1, a2);
    }

    public static class Tuple0 extends Tuple<Void, Void> implements Serializable {

        /**
         *
         */
        public Tuple0() {
        }

        /**
         *
         * @return
         */
        @Override
        public Natural dimension() {
            return Natural.ZERO;
        }

        /**
         *
         * @return
         */
        @Override
        public Object[] toArray() {
            return new Object[] {};
        }
    }

    public static class Tuple1<D> extends Tuple<D, Tuple0> implements Serializable {

        protected Tuple1() {
            super();
        }

        public Tuple1(D element) {
            super(element, new Tuple0());
        }
    }

    public static class Tuple2<D1, D2> extends Tuple<D1, Tuple1<D2>> implements Serializable {

        protected Tuple2() {
            super();
        }

        public Tuple2(D1 element1, D2 element2) {
            super(element1, new Tuple1<D2>(element2));
        }
    }

    public static class Tuple3<D1, D2, D3> extends Tuple<D1, Tuple2<D2, D3>> implements Serializable {

        protected Tuple3() {
            super();
        }

        public Tuple3(D1 element1, D2 element2, D3 element3) {
            super(element1, new Tuple2<D2, D3>(element2, element3));
        }
    }
}
