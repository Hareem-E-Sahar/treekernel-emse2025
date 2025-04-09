package rafa.midi.saiph.gen;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.sound.midi.InvalidMidiDataException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import rafa.midi.saiph.Saiph;
import rafa.midi.saiph.SaiphXML;
import rafa.midi.saiph.Segment;
import rafa.midi.saiph.event.MusicalEvent;
import rafa.midi.saiph.gen.gui.SegmentGeneratorDialog;
import rafa.midi.saiph.gen.gui.SegmentGeneratorEditor;
import rafa.midi.saiph.values.Values;
import rafa.midi.saiph.values.gen.ConstantGenerator;
import rafa.midi.saiph.values.gen.ValuesGenerator;

/**
 * Generator of a segment.
 * @author rafa
  */
public abstract class SegmentGenerator implements SaiphXML {

    public static final int LIMIT_EVENTS = 0;

    public static final int LIMIT_DURATION = 1;

    public static final String XML_TAG_NAME = "segmentGenerator";

    /** Name for this Generator. */
    protected String name = "";

    /** Extended info */
    protected String info = "";

    /** Type of limit imposed to this segment:
	 *  number of events (default) or total duration. */
    protected int limitType;

    /** Limit to this segment (in number of events or MIDI ticks,
	 *  depending on <code>limitType</code>). Defaults to 1. */
    protected long limit = 1l;

    /** Generator of durations for the events of the segment. */
    protected ValuesGenerator durationGenerator;

    /** The generated segment. */
    protected Segment segment;

    /** The editor for this generator */
    protected SegmentGeneratorEditor editor;

    /** The dialog to edit this generator */
    protected SegmentGeneratorDialog dialog;

    public SegmentGenerator() {
        this.segment = new Segment(this);
        this.durationGenerator = new ConstantGenerator(Values.DURATION);
    }

    /**
	 * Generates the next musical event.
	 * @return
	 */
    protected abstract MusicalEvent generateMusicalEvent();

    /**
	 * @return The editor for this class of SegmentGenerator.
	 */
    public SegmentGeneratorEditor getEditor() {
        if (editor == null) {
            editor = createEditor();
        }
        return editor;
    }

    /**
	 * @return Returns the dialog.
	 */
    public SegmentGeneratorDialog getDialog() {
        if (dialog == null) {
            dialog = new SegmentGeneratorDialog(Saiph.getFrame(), this);
        }
        return dialog;
    }

    protected abstract SegmentGeneratorEditor createEditor();

    /** Resets and generates the whole segment. */
    public void generateSegment() {
        segment.notifyChanging();
        reset();
        do {
            addMusicalEvent();
        } while (limitNotReached());
        segment.notifyChanged();
        segment.getGui().refresh();
    }

    /** Generates the next musical event and adds it to the list */
    private void addMusicalEvent() {
        MusicalEvent event = generateMusicalEvent();
        segment.add(event);
        if (limitType == LIMIT_DURATION && segment.getTotalDuration() > limit) {
            long excess = segment.getTotalDuration() - limit;
            event.setDuration(event.getDuration() - excess);
        }
    }

    /**
	 * @return true if the note generation has not reached the limit.
	 */
    private boolean limitNotReached() {
        boolean result = true;
        switch(limitType) {
            case LIMIT_EVENTS:
                result = (segment.getMusicalEvents().size() < limit);
                break;
            case LIMIT_DURATION:
                result = (segment.getTotalDuration() < limit);
                break;
        }
        return result;
    }

    /** Resets the segment and the durations generator. */
    protected void reset() {
        segment.reset();
        durationGenerator.reset();
    }

    public long getLimit() {
        return limit;
    }

    /**
	 * @return
	 */
    public ValuesGenerator getDurationGenerator() {
        return durationGenerator;
    }

    public int getLimitType() {
        return limitType;
    }

    /**
	 * @return Returns the segment.
	 */
    public Segment getSegment() {
        return segment;
    }

    /**
	 * @param generator
	 */
    public void setDurationGenerator(ValuesGenerator generator) {
        durationGenerator = generator;
    }

    /**
	 * @param type
	 * @param limit
	 */
    public void setLimit(int type, long limit) {
        this.limitType = type;
        this.limit = limit;
    }

    /**
	 * @return Returns the info.
	 */
    public String getInfo() {
        return info;
    }

    public String getName() {
        return name;
    }

    /**
	 * @param info The info to set.
	 */
    public void setInfo(String info) {
        this.info = info;
    }

    public void setName(String s) {
        name = s;
    }

    public Node toSaiphXML(Document doc) {
        Element node = doc.createElement(SegmentGenerator.XML_TAG_NAME);
        node.setAttribute("name", name);
        node.setAttribute("info", info);
        Element limitElement = doc.createElement("limit");
        limitElement.setAttribute("type", String.valueOf(limitType));
        limitElement.appendChild(doc.createTextNode(String.valueOf(limit)));
        node.appendChild(limitElement);
        Node durationGeneratorNode = doc.createElement("durationGenerator");
        durationGeneratorNode.appendChild(durationGenerator.toSaiphXML(doc));
        node.appendChild(durationGeneratorNode);
        return node;
    }

    public boolean fromSaiphXML(Node node) throws DOMException, InvalidMidiDataException, ClassNotFoundException, SecurityException, NoSuchMethodException, NumberFormatException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        boolean ok = true;
        setName(((Element) node).getAttribute("name"));
        setInfo(((Element) node).getAttribute("info"));
        Node limitNode = ((Element) node).getElementsByTagName("limit").item(0);
        String t = ((Element) limitNode).getAttribute("type");
        String l = limitNode.getFirstChild().getNodeValue();
        int ix = Integer.parseInt(t);
        ok = (ix >= 0 && ix <= 9);
        if (!ok) return false;
        long lx = Long.parseLong(l);
        ok = (lx > 1 && lx <= Long.MAX_VALUE);
        if (!ok) return false;
        setLimit(ix, lx);
        Node dgNode = ((Element) node).getElementsByTagName("durationGenerator").item(0);
        Node vgNode = ((Element) dgNode).getFirstChild();
        String typeAttr = ((Element) vgNode).getAttribute("type");
        String dgClassName = ((Element) vgNode).getAttribute("class");
        Class dgClass = Class.forName(dgClassName);
        Constructor dgClassConstructor = dgClass.getConstructor(new Class[] { int.class });
        Object dg = dgClassConstructor.newInstance(new Object[] { Integer.valueOf(typeAttr) });
        ok = ((SaiphXML) dg).fromSaiphXML(vgNode);
        if (!ok) return false;
        setDurationGenerator((ValuesGenerator) dg);
        return ok;
    }
}
