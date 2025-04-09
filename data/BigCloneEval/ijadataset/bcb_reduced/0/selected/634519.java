package org.tru42.signal.lang;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.tru42.signal.model.AbstractSignal;
import org.tru42.signal.model.ISignalProcessor;
import org.tru42.signal.model.ISignalsSink;
import org.tru42.signal.model.ISignalsSource;
import org.tru42.signal.model.MethodSignal;
import org.tru42.signal.model.SignalDiagram;
import org.tru42.signal.model.Sink;

public abstract class SObject implements ISignalProcessor, ISignalsSink, ISignalsSource {

    protected String name;

    protected SignalDiagram model;

    protected final Map<String, ISignal> signals = new LinkedHashMap<String, ISignal>();

    protected final Map<String, Sink> sinks = new LinkedHashMap<String, Sink>();

    public SObject() {
        for (Class<?> c : this.getClass().getClasses()) try {
            if (ISignal.class.isAssignableFrom(c) && c.getConstructor(getClass(), SObject.class) != null) {
                ISignal s = (ISignal) c.getConstructor(getClass(), SObject.class).newInstance(this, this);
                signals.put(s.getName(), s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Method m : getSignalMethods(this)) {
            AbstractSignal s = new MethodSignal(this, m);
            signals.put(s.getName(), s);
        }
        for (Method m : getSinkMethods(this)) {
            Sink s = new Sink(this, m);
            sinks.put(s.getName(), s);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SignalDiagram getModel() {
        return model;
    }

    public void setModel(SignalDiagram model) {
        this.model = model;
    }

    public Sink getSink(String name) {
        return sinks.get(name);
    }

    public ISignal getSignal(String name) {
        return signals.get(name);
    }

    public Sink[] getSinks() {
        return sinks.values().toArray(new Sink[sinks.size()]);
    }

    public ISignal[] getSignals() {
        return signals.values().toArray(new ISignal[signals.size()]);
    }

    public void dispose() {
        try {
            for (ISignal signal : signals.values()) model.disconnect(signal);
        } catch (Exception e) {
        }
        ;
        try {
            for (Sink sink : sinks.values()) model.disconnect(sink);
        } catch (Exception e) {
        }
        ;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ISignalProcessor && name != null && ((ISignalProcessor) obj).getName() != null) return name.equals(((ISignalProcessor) obj).getName()); else return super.equals(obj);
    }

    private static Method[] getAnnotatedMethods(SObject obj, Class<? extends Annotation> a) {
        List<Method> methods = new LinkedList<Method>();
        for (Method m : obj.getClass().getMethods()) if (m.getAnnotation(a) != null) methods.add(m);
        return methods.toArray(new Method[methods.size()]);
    }

    public static Method[] getSignalMethods(SObject obj) {
        return getAnnotatedMethods(obj, org.tru42.signal.lang.annotation.Signal.class);
    }

    public static Method[] getSinkMethods(SObject obj) {
        return getAnnotatedMethods(obj, org.tru42.signal.lang.annotation.Sink.class);
    }

    public void trigger(String signal, long timestamp, Object... args) {
        trigger(signals.get(signal), timestamp, args);
    }

    public void trigger(ISignal signal, long timestamp, Object... args) {
        if (signal != null) signal.setValue(args);
    }

    @Override
    public String toString() {
        return getName();
    }
}
