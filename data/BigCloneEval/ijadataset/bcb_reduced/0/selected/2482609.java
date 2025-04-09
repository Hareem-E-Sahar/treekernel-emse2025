package abbot.editor;

import abbot.script.*;
import abbot.*;
import abbot.tester.ComponentTester;
import javax.swing.AbstractButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.Window;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Dialog;
import java.awt.event.*;
import java.awt.AWTEvent;
import java.util.*;
import java.lang.reflect.*;

/** 
 * Provides recording of raw AWT events, attempting to parse them as
 * high-level semantic events.
 * Typing Shift-ESC will terminate the recording.<p>
 */
public class EventRecorder extends Recorder {

    ArrayList steps = new ArrayList();

    private int count = 0;

    /** Flag gets set when event recording should terminate. */
    private boolean stop = false;

    /** Create a Recorder for use in capturing raw AWTEvents. */
    public EventRecorder(Resolver resolver, ComponentFinder finder, ActionListener l) {
        super(resolver, finder, l);
    }

    /** Return the name of the type of GUI action to be recorded. */
    public String getName() {
        return "All Actions";
    }

    protected void initialize() {
        super.initialize();
        stop = false;
        count = 0;
        steps.clear();
        setStatus("Recording an event stream (press Shift-ESC to end)");
    }

    /** The stop flag gets set when we've determined we should stop recording
     * events.
     */
    protected boolean shouldStop() {
        return stop;
    }

    private void parseComboBoxSelection(ArrayList steps) {
    }

    /**
     * Return a sequence containing all the semantic and basic events captured
     * thus far.
     */
    protected Step createStep() {
        setStatus("Captured " + steps.size() + " events");
        return new Sequence(getResolver(), getFinder(), null, steps);
    }

    /** The current semantic recorder, if any. */
    private SemanticRecorder semanticRecorder = null;

    /** Handle an action.  This can either be ignored, contribute to the
     * recording, or cause the recording to be canceled.
     * For a given event, if no current semantic recorder is active,
     * select one based on the event's component.  If the semantic recorder
     * accepts the event, then it is used to consume each subsequent event,
     * until its recordEvent method returns true, indicating that the semantic
     * event has completed.
     */
    protected void eventDispatched(java.awt.AWTEvent event) {
        if (event.getID() == KeyEvent.KEY_PRESSED && ((KeyEvent) event).getKeyCode() == KeyEvent.VK_ESCAPE && ((KeyEvent) event).getModifiers() == KeyEvent.SHIFT_MASK) {
            if (semanticRecorder != null) {
                Step step = semanticRecorder.getStep();
                if (step != null) steps.add(step);
                semanticRecorder = null;
            }
            synchronized (this) {
                stop = true;
                notify();
            }
            return;
        }
        if (semanticRecorder == null && event instanceof ComponentEvent) {
            SemanticRecorder sr = getSemanticRecorder(((ComponentEvent) event).getComponent());
            setStatus("Trying " + sr);
            if (sr.accept(event)) {
                semanticRecorder = sr;
            } else {
                setStatus("Normal event");
            }
        }
        if (semanticRecorder != null) {
            boolean consumed = semanticRecorder.record(event);
            if (semanticRecorder.isFinished()) {
                Step step = semanticRecorder.getStep();
                if (step != null) steps.add(step); else setStatus("Semantic event discarded");
                semanticRecorder = null;
            }
            if (consumed) return;
        }
        int id = event.getID();
        switch(id) {
            case KeyEvent.KEY_PRESSED:
            case KeyEvent.KEY_RELEASED:
            case KeyEvent.KEY_TYPED:
            case MouseEvent.MOUSE_PRESSED:
            case MouseEvent.MOUSE_RELEASED:
            case MouseEvent.MOUSE_MOVED:
            case MouseEvent.MOUSE_DRAGGED:
            case MouseEvent.MOUSE_ENTERED:
            case MouseEvent.MOUSE_EXITED:
            case MouseEvent.MOUSE_CLICKED:
            case ComponentEvent.COMPONENT_SHOWN:
            case ComponentEvent.COMPONENT_HIDDEN:
            case WindowEvent.WINDOW_OPENED:
            case WindowEvent.WINDOW_CLOSED:
            case FocusEvent.FOCUS_GAINED:
            case FocusEvent.FOCUS_LOST:
            case WindowEvent.WINDOW_ACTIVATED:
            case WindowEvent.WINDOW_DEACTIVATED:
                steps.add(new SendEvent(getResolver(), getFinder(), null, event));
                setStatus("Captured event " + count++);
                break;
            default:
                break;
        }
    }

    /** Return the events of interest to this Recorder.  */
    public long getEventMask() {
        return -1;
    }

    /** 
     * Shift-ESC indicates the end of the stream, unless no windows are
     * showing, in which case it will cancel the recording.  We also want to
     * be able to record plain old ESC, since it's a fairly common GUI input. 
     */
    public boolean isCancelEvent(java.awt.AWTEvent event) {
        return super.isCancelEvent(event) && getFinder().getWindows().length == 0;
    }

    /** Maps component classes to corresponding semantic recorders. */
    private HashMap semanticRecorders = new HashMap();

    /** Return the semantic recorder for the given component. */
    private SemanticRecorder getSemanticRecorder(Component comp) {
        return getSemanticRecorder(comp.getClass());
    }

    /** Return the semantic recorder for the given component class. */
    private SemanticRecorder getSemanticRecorder(Class cls) {
        if (!(Component.class.isAssignableFrom(cls))) {
            throw new IllegalArgumentException("Class must derive from " + "Component");
        }
        SemanticRecorder sr = (SemanticRecorder) semanticRecorders.get(cls);
        if (sr == null) {
            String cname = simpleClassName(cls);
            try {
                String pkg = SemanticRecorder.class.getPackage().getName();
                cname = pkg + "." + cname + "Recorder";
                Class recorderClass = Class.forName(cname);
                Constructor ctor = recorderClass.getConstructor(new Class[] { Resolver.class, ComponentFinder.class, ActionListener.class });
                sr = (SemanticRecorder) ctor.newInstance(new Object[] { getResolver(), getFinder(), getListener() });
                semanticRecorders.put(cls, sr);
            } catch (InvocationTargetException e) {
                Log.warn(e);
            } catch (NoSuchMethodException e) {
                sr = getSemanticRecorder(cls.getSuperclass());
            } catch (InstantiationException e) {
                sr = getSemanticRecorder(cls.getSuperclass());
            } catch (IllegalAccessException iae) {
                sr = getSemanticRecorder(cls.getSuperclass());
            } catch (ClassNotFoundException cnf) {
                sr = getSemanticRecorder(cls.getSuperclass());
            }
        }
        return sr;
    }
}
