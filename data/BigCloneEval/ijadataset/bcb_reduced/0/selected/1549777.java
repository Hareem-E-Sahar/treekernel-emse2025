package org.expasy.jpl.commons.collection.symbol;

import java.io.Serializable;
import java.lang.reflect.Constructor;

/**
 * The basic symbol class.
 * 
 * @author nikitin
 * 
 * @param <T> the symbol type.
 * 
 * @version 1.0
 * 
 */
public abstract class AbstractSymbol<T> implements Symbol<T>, Serializable {

    private static final long serialVersionUID = -5042441402526578891L;

    /** the symbol character */
    protected char name;

    /** the symbol type */
    protected SymbolType<T> type;

    /** for serialization only */
    public AbstractSymbol() {
    }

    protected AbstractSymbol(char name, SymbolType<T> type) {
        this.name = name;
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
        if (o instanceof AbstractSymbol) {
            AbstractSymbol<T> sym = (AbstractSymbol) o;
            if (sym.type.equals(type) && sym.name == name) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        return name;
    }

    /** {@inheritDoc} */
    public char getName() {
        return name;
    }

    /** {@inheritDoc} */
    public SymbolType<T> getType() {
        return type;
    }

    @SuppressWarnings("unchecked")
    public static <T> Symbol<T> newSymbolInstance(Class<? extends Symbol<T>> cls, SymbolType<T> type, char c) {
        try {
            Constructor<? extends Symbol<T>> ct = cls.getConstructor(char.class);
            Object retobj = ct.newInstance(c);
            ((AbstractSymbol<T>) retobj).type = type;
            return (AbstractSymbol<T>) retobj;
        } catch (Throwable e) {
            System.err.println(e);
        }
        return null;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.getName()).append(".").append(name);
        return sb.toString();
    }
}
