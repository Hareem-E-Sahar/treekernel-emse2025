package rafa.midi.saiph.gen;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.sound.midi.InvalidMidiDataException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import rafa.midi.saiph.SaiphXML;
import rafa.midi.saiph.event.MusicalEvent;
import rafa.midi.saiph.event.Note;
import rafa.midi.saiph.gen.gui.NoteSegmentGeneratorEditor;
import rafa.midi.saiph.gen.gui.SegmentGeneratorEditor;
import rafa.midi.saiph.values.Values;
import rafa.midi.saiph.values.gen.ConstantGenerator;
import rafa.midi.saiph.values.gen.ValuesGenerator;

/**
 * Generator of a track containing just notes.
 * @author rafa
  */
public class NoteSegmentGenerator extends CMSegmentGenerator {

    /**
	 * Generator of articulation values, i.e. the percentage of the 
	 * nominal note duration which is really played.
	 */
    protected ValuesGenerator articulationGenerator;

    /**
	 * Calls the default constructor for a half-tone pitch type.
	 */
    public NoteSegmentGenerator() {
        this(Values.PITCH_1_2_TONES);
    }

    /**
	 * Default constructor.
	 * The generators for pitch, velocity and articulation
	 * are initialized as constant generators with their default values.
	 * @param pitchType Pitch type (one of Values.PITCH_1_2_TONES,
	 * 		Values.PITCH_1_4_TONES or Values.PITCH_1_8_TONES).
	 */
    public NoteSegmentGenerator(int pitchType) {
        byte1Generator = new ConstantGenerator(pitchType);
        byte2Generator = new ConstantGenerator(Values.VELOCITY);
        articulationGenerator = new ConstantGenerator(Values.ARTICULATION);
    }

    protected MusicalEvent generateMusicalEvent() {
        long duration = durationGenerator.generateValue();
        int channel = (int) channelGenerator.generateValue();
        int pitchType = byte1Generator.getType();
        int pitch = (int) byte1Generator.generateValue();
        int velocity = (int) byte2Generator.generateValue();
        int articulation = (int) articulationGenerator.generateValue();
        MusicalEvent event = new Note(channel, pitchType, pitch, velocity, articulation, duration);
        return event;
    }

    public ValuesGenerator getArticulationGenerator() {
        return articulationGenerator;
    }

    public ValuesGenerator getPitchGenerator() {
        return byte1Generator;
    }

    public ValuesGenerator getVelocityGenerator() {
        return byte2Generator;
    }

    public void setArticulationGenerator(ValuesGenerator articulationGenerator) {
        this.articulationGenerator = articulationGenerator;
    }

    public void setPitchGenerator(ValuesGenerator vg) {
        this.byte1Generator = vg;
    }

    public void setVelocityGenerator(ValuesGenerator vg) {
        this.byte2Generator = vg;
    }

    protected void reset() {
        super.reset();
        articulationGenerator.reset();
    }

    protected SegmentGeneratorEditor createEditor() {
        return new NoteSegmentGeneratorEditor(this);
    }

    public Node toSaiphXML(Document doc) {
        Node node = super.toSaiphXML(doc);
        ((Element) node).setAttribute("class", this.getClass().getName());
        Node pitchGeneratorNode = doc.createElement("pitchGenerator");
        pitchGeneratorNode.appendChild(byte1Generator.toSaiphXML(doc));
        node.appendChild(pitchGeneratorNode);
        Node velocityGeneratorNode = doc.createElement("velocityGenerator");
        velocityGeneratorNode.appendChild(byte2Generator.toSaiphXML(doc));
        node.appendChild(velocityGeneratorNode);
        Node articulationGeneratorNode = doc.createElement("articulationGenerator");
        articulationGeneratorNode.appendChild(articulationGenerator.toSaiphXML(doc));
        node.appendChild(articulationGeneratorNode);
        return node;
    }

    public boolean fromSaiphXML(Node node) throws NumberFormatException, DOMException, SecurityException, IllegalArgumentException, InvalidMidiDataException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        boolean ok = super.fromSaiphXML(node);
        if (!ok) return false;
        Node pgNode = ((Element) node).getElementsByTagName("pitchGenerator").item(0);
        Node vgNode = ((Element) pgNode).getFirstChild();
        String typeAttr = ((Element) vgNode).getAttribute("type");
        String pgClassName = ((Element) vgNode).getAttribute("class");
        Class pgClass = Class.forName(pgClassName);
        Constructor pgClassConstructor = pgClass.getConstructor(new Class[] { int.class });
        Object pg = pgClassConstructor.newInstance(new Object[] { Integer.valueOf(typeAttr) });
        ok = ((SaiphXML) pg).fromSaiphXML(vgNode);
        if (!ok) return false;
        setPitchGenerator((ValuesGenerator) pg);
        Node velgNode = ((Element) node).getElementsByTagName("velocityGenerator").item(0);
        vgNode = ((Element) velgNode).getFirstChild();
        typeAttr = ((Element) vgNode).getAttribute("type");
        String velgClassName = ((Element) vgNode).getAttribute("class");
        Class velgClass = Class.forName(velgClassName);
        Constructor velgClassConstructor = velgClass.getConstructor(new Class[] { int.class });
        Object velg = velgClassConstructor.newInstance(new Object[] { Integer.valueOf(typeAttr) });
        ok = ((SaiphXML) velg).fromSaiphXML(vgNode);
        if (!ok) return false;
        setVelocityGenerator((ValuesGenerator) velg);
        Node agNode = ((Element) node).getElementsByTagName("articulationGenerator").item(0);
        vgNode = ((Element) agNode).getFirstChild();
        typeAttr = ((Element) vgNode).getAttribute("type");
        String agClassName = ((Element) vgNode).getAttribute("class");
        Class agClass = Class.forName(agClassName);
        Constructor agClassConstructor = agClass.getConstructor(new Class[] { int.class });
        Object ag = agClassConstructor.newInstance(new Object[] { Integer.valueOf(typeAttr) });
        ok = ((SaiphXML) ag).fromSaiphXML(vgNode);
        if (!ok) return false;
        setArticulationGenerator((ValuesGenerator) ag);
        return ok;
    }
}
