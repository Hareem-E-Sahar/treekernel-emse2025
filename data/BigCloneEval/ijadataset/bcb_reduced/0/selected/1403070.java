package xuniversewizard.gui.param.input;

import java.awt.Component;
import javax.swing.DefaultBoundedRangeModel;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import xuniversewizard.gui.components.AdvSlider;
import xuniversewizard.gui.param.Parameter;

/**
 * An input model for a slider that selects int values.
 * 
 * @author Tobias Weigel
 * @date 26.03.2009
 * 
 */
public class SliderInputModel extends IntValueInputModel {

    private AdvSlider slider;

    private DefaultBoundedRangeModel sliderModel;

    private Integer minSliderValue;

    private Integer maxSliderValue;

    public SliderInputModel(Parameter parameter, Node node) throws Exception {
        super(parameter, node);
        if (node == null) throw new Exception("A slider model cannot be used in default/parameter-free mode!");
        Node child = node.getFirstChild();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (child.getNodeName().equalsIgnoreCase("sliderLimits")) {
                    parseSliderLimits(child);
                }
            }
            child = child.getNextSibling();
        }
        if ((maxSliderValue == null) && (maxValue != null)) maxSliderValue = new Integer(maxValue);
        if ((minSliderValue == null) && (minValue != null)) minSliderValue = new Integer(minValue);
        if ((minSliderValue == null) || (maxSliderValue == null)) throw new Exception("A slider input model needs to specify slider limits or value limits!");
        int value = (minSliderValue + maxSliderValue) / 2;
        sliderModel = new DefaultBoundedRangeModel(value, 0, minSliderValue, maxSliderValue);
    }

    private void parseSliderLimits(Node node) {
        NamedNodeMap att = node.getAttributes();
        if (att.getNamedItem("min") != null) {
            minSliderValue = new Integer(att.getNamedItem("min").getNodeValue());
        }
        if (att.getNamedItem("max") != null) {
            maxSliderValue = new Integer(att.getNamedItem("max").getNodeValue());
        }
    }

    @Override
    public Component getInputComponent() {
        if (slider == null) {
            slider = new AdvSlider(sliderModel, minValue, maxValue);
            slider.setInputVerifier(new ValueLimitInputVerifier());
            slider.setInputFieldToolTipText(genericValueLimitsToolTipText(minValue, maxValue));
            slider.addPropertyChangeListener("value", new ValuePropertyChangeForwarder(this));
        }
        return slider;
    }

    @Override
    public String genericValueLimitsToolTipText(Object minValue, Object maxValue, boolean usePercentage) {
        if ((minValue != null) && (maxValue == null)) {
            String s = super.genericValueLimitsToolTipText(minValue, maxValue, usePercentage);
            s += ". " + parameter.getTextManager().getString("higherValuesAllowed");
            return s;
        } else return super.genericValueLimitsToolTipText(minValue, maxValue, usePercentage);
    }

    @Override
    public boolean hasValue() {
        return true;
    }

    @Override
    public String getStringValue() {
        return "" + sliderModel.getValue();
    }

    @Override
    public void setStringValue(String value) {
        Integer i = Integer.parseInt(value);
        setValue(i);
    }

    /**
	 * Sets a new slider value.
	 * 
	 * @param value
	 */
    public void setValue(int value) {
        slider.updateValue(value);
    }

    @Override
    public Object getValue() {
        return slider.getValue();
    }

    @Override
    public void setValue(Object value) {
        if (!(value instanceof Number)) throw new IllegalArgumentException("Only Number allowed as value for SliderInputModel!");
        setValue(((Number) value).intValue());
    }
}
