package org.uithin.nodes.unit;

import java.lang.reflect.Constructor;
import nu.xom.Attribute;
import nu.xom.Element;
import org.uithin.nodes.Node;
import org.uithin.nodes.INode.Event;
import org.uithin.nodes.INode.Slot;
import zz.utils.properties.IProperty;
import zz.utils.properties.IPropertyListener;

public abstract class Connector<S, D> implements IPropertyListener {

    private final Slot<S> itsSource;

    private final Slot<D> itsDest;

    Connector(Slot<S> aSource, Slot<D> aDest) {
        itsSource = aSource;
        itsDest = aDest;
    }

    public void activate() {
        if (itsSource.canRead()) itsDest.set(s2d(itsSource.get()));
        if (itsSource.canWrite()) itsSource.addHardListener(this);
        if (isBidirectional()) itsDest.addHardListener(this);
    }

    public void deactivate() {
        if (itsSource.canWrite()) itsSource.removeListener(this);
        if (isBidirectional()) itsDest.removeListener(this);
    }

    public Slot<S> getSource() {
        return itsSource;
    }

    public Slot<D> getDest() {
        return itsDest;
    }

    protected abstract boolean isBidirectional();

    /**
	 * Converts a source value to a dest value.
	 */
    protected abstract D s2d(S aSourceValue);

    /**
	 * Converts a dest value to a source value.
	 */
    protected S d2s(D aDestValue) {
        throw new UnsupportedOperationException();
    }

    public void propertyChanged(IProperty aProperty, Object aOldValue, Object aNewValue) {
        if (aProperty == itsSource) itsDest.set(s2d((S) aNewValue)); else if (aProperty == itsDest) itsSource.set(d2s((D) aNewValue)); else throw new RuntimeException();
    }

    public void propertyValueChanged(IProperty aProperty) {
    }

    public Element toXML() {
        Element theElement = new Element("_connector");
        theElement.addAttribute(new Attribute("src", itsSource.getDesc()));
        theElement.addAttribute(new Attribute("dst", itsDest.getDesc()));
        return theElement;
    }

    /**
	 * Lets subclasses load their specific information. 
	 */
    protected void finishLoading(Element aElement) {
    }

    private static Slot findSlot(Unit aUnit, String aDesc) {
        String[] theParts = aDesc.split("/");
        if (theParts.length != 2) throw new RuntimeException("Cannot decode: " + aDesc);
        Node theNode = aUnit.getNode(theParts[0]);
        if (theNode == null) throw new RuntimeException("Cannot find node: " + theParts[0]);
        return theNode.getSlot(theParts[1]);
    }

    public static Connector load(Unit aUnit, Element aElement) {
        String theType = aElement.getLocalName();
        Class theClass;
        if ("simpleConnector".equals(theType)) theClass = SimpleConnector.class; else if ("toStringConnector".equals(theType)) theClass = ToStringConnector.class; else throw new RuntimeException("Not handled: " + theType);
        try {
            Constructor theConstructor = theClass.getConstructor(Slot.class, Slot.class);
            Connector theConnector = (Connector) theConstructor.newInstance(findSlot(aUnit, aElement.getAttributeValue("src")), findSlot(aUnit, aElement.getAttributeValue("dst")));
            theConnector.finishLoading(aElement);
            return theConnector;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
	 * The most basic property forwarder, that deals with properties of equal types.
	 * @author gpothier
	 */
    public static class SimpleConnector<T> extends Connector<T, T> {

        private boolean itsBidirectional = false;

        public SimpleConnector(Slot<T> aSource, Slot<T> aDest) {
            super(aSource, aDest);
        }

        @Override
        protected boolean isBidirectional() {
            return itsBidirectional;
        }

        @Override
        protected T d2s(T aDestValue) {
            return aDestValue;
        }

        @Override
        protected T s2d(T aSourceValue) {
            return aSourceValue;
        }

        public Element toXML() {
            Element theElement = super.toXML();
            theElement.setLocalName("simpleConnector");
            theElement.addAttribute(new Attribute("bidi", "" + itsBidirectional));
            return theElement;
        }
    }

    /**
	 * A forwarder that accepts any source and converts it to a string using toString().
	 * @author gpothier
	 */
    public static class ToStringConnector<S> extends Connector<S, String> {

        public ToStringConnector(Slot<S> aSource, Slot<String> aDest) {
            super(aSource, aDest);
        }

        @Override
        protected boolean isBidirectional() {
            return false;
        }

        @Override
        protected String s2d(S aSourceValue) {
            return "" + aSourceValue;
        }

        public Element toXML() {
            Element theElement = super.toXML();
            theElement.setLocalName("toStringConnector");
            return theElement;
        }
    }
}
