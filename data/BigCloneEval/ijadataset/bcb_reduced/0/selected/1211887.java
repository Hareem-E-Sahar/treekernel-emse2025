package deduced.analyzer;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import assertion.AssertUtility;
import deduced.*;

/**
 * CreationAnalyzer
 * 
 * @author Duff
 */
public class CreationAnalyzer implements PropertyListener {

    private CreationFactory _factory;

    private PropertyCollection _source;

    private PropertyCollection _destination;

    public CreationAnalyzer() {
    }

    /**
     * @return Returns the destination.
     */
    public PropertyCollection getDestination() {
        return _destination;
    }

    /**
     * @param destination The destination to set.
     */
    public void setDestination(PropertyCollection destination) {
        if (_destination == destination) {
            return;
        }
        clearDestination();
        _destination = destination;
        fillDestination();
    }

    /**
     * @return Returns the factory.
     */
    public CreationFactory getFactory() {
        return _factory;
    }

    /**
     * @param factory The factory to set.
     */
    public void setFactory(CreationFactory factory) {
        if (_factory == factory) {
            return;
        }
        clearDestination();
        _factory = factory;
        fillDestination();
    }

    /**
     * @return Returns the source.
     */
    public PropertyCollection getSource() {
        return _source;
    }

    /**
     * @param source The source to set.
     */
    public void setSource(PropertyCollection source) {
        if (_source == source) {
            return;
        }
        clearDestination();
        removeSourceListener();
        _source = source;
        fillDestination();
        addSourceListener();
    }

    /**
     * addSourceListener
     */
    private void addSourceListener() {
        if (_source != null) {
            _source.addListener(this);
        }
    }

    /**
     * removeSourceListener
     */
    private void removeSourceListener() {
        if (_source != null) {
            _source.removeListener(this);
        }
    }

    private void fillDestination() {
        if (_destination == null || _source == null || _factory == null) {
            return;
        }
        Iterator it = _source.asValueMap().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Entry) it.next();
            Object value = entry.getValue();
            Object key = entry.getKey();
            Class createClass = _factory.create(key, value);
            createValue(key, createClass);
        }
    }

    private void clearDestination() {
        if (_destination != null) {
            _destination.clear();
        }
    }

    /**
     * (non-Javadoc)
     * 
     * @see deduced.PropertyListener#propertyChanged(deduced.PropertyChangeEvent)
     */
    public void propertyChanged(PropertyChangeEvent event) {
        if (_destination == null || _factory == null) {
            return;
        }
        ChangeType type = event.getType();
        Object key = event.getKey();
        Object value = event.getNewValue();
        if (type == ChangeType.ADD) {
            Class createClass = _factory.create(key, value);
            createValue(key, createClass);
        } else if (type == ChangeType.UPDATE) {
            Class createClass = _factory.create(key, value);
            Object currentValue = _destination.getPropertyValue(key);
            if (currentValue.getClass() != createClass) {
                removeValue(key);
                createValue(key, createClass);
            }
        } else if (type == ChangeType.REMOVE) {
            removeValue(key);
        }
    }

    /**
     * removeValue
     * 
     * @param key
     */
    private void removeValue(Object key) {
        if (_destination == null || !_destination.containsProperty(key)) {
            return;
        }
        _destination.removeProperty(key);
    }

    /**
     * createValue
     * 
     * @param key
     * @param createClass
     */
    private void createValue(Object key, Class createClass) {
        if (createClass == null || _destination == null) {
            return;
        }
        Object addValue = null;
        try {
            Class[] classArgs = null;
            Constructor constructor = createClass.getConstructor(classArgs);
            Object[] objectArgs = null;
            addValue = constructor.newInstance(objectArgs);
            _destination.addProperty(key, null, addValue);
        } catch (Exception e) {
            AssertUtility.exception(e);
        }
    }
}
