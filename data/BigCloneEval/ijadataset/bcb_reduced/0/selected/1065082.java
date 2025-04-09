package amap;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.NumberFormatter;
import java.beans.*;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.HashMap;

public class AmapPanel extends JPanel implements ChangeListener, PropertyChangeListener, ActionListener {

    AlignmentPanel alignPanel;

    JFormattedTextField indexField;

    JFormattedTextField weightField;

    JSlider alignSlider;

    java.util.List<Alignment> aligns;

    JButton nextButton;

    JButton prevButton;

    JButton startStopButton;

    int maxSliderVal;

    Map<JFormattedTextField, Boolean> ok2update;

    Timer timer;

    boolean frozen = true;

    int delay = 200;

    public AmapPanel(java.util.List<Alignment> aligns) {
        this.aligns = aligns;
        maxSliderVal = aligns.size() - 1;
        ok2update = new HashMap<JFormattedTextField, Boolean>();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        timer = new Timer(delay, this);
        timer.setCoalesce(true);
        JLabel sliderLabel = new JLabel("Alignment: ", JLabel.CENTER);
        sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        java.text.NumberFormat intFormat = java.text.NumberFormat.getIntegerInstance();
        NumberFormatter intFormatter = new NumberFormatter(intFormat);
        intFormatter.setMinimum(new Integer(0));
        intFormatter.setMaximum(new Integer(maxSliderVal));
        indexField = new JFormattedTextField(intFormatter);
        indexField.setColumns(5);
        indexField.addPropertyChangeListener(this);
        handleEnterKeyStroke(indexField);
        ok2update.put(indexField, false);
        JLabel weightLabel = new JLabel("Weight: ", JLabel.CENTER);
        java.text.NumberFormat numFormat = java.text.NumberFormat.getNumberInstance();
        NumberFormatter numFormatter = new NumberFormatter(numFormat);
        numFormatter.setMinimum(new Float(0f));
        numFormatter.setMaximum(new Float(Float.MAX_VALUE));
        weightField = new JFormattedTextField(numFormatter);
        weightField.setColumns(10);
        weightField.addPropertyChangeListener(this);
        handleEnterKeyStroke(weightField);
        ok2update.put(weightField, false);
        nextButton = new JButton("Next");
        nextButton.addActionListener(this);
        startStopButton = new JButton("Start Animation");
        startStopButton.addActionListener(this);
        prevButton = new JButton("Prev");
        prevButton.addActionListener(this);
        alignSlider = new JSlider(JSlider.HORIZONTAL, 0, maxSliderVal, 0);
        alignSlider.addChangeListener(this);
        alignSlider.setMajorTickSpacing(calcTickSpacing(maxSliderVal));
        alignSlider.setPaintTicks(true);
        alignSlider.setPaintLabels(true);
        alignPanel = new AlignmentPanel(aligns, 450);
        alignPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        updatePicture(0);
        JScrollPane alignScroll = new JScrollPane(alignPanel);
        alignScroll.setPreferredSize(new Dimension(500, 700));
        alignScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        alignScroll.addComponentListener(alignPanel);
        JPanel labelAndTextField = new JPanel();
        labelAndTextField.add(prevButton);
        labelAndTextField.add(startStopButton);
        labelAndTextField.add(nextButton);
        labelAndTextField.add(sliderLabel);
        labelAndTextField.add(indexField);
        labelAndTextField.add(weightLabel);
        labelAndTextField.add(weightField);
        add(alignScroll);
        add(alignSlider);
        add(labelAndTextField);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        update(maxSliderVal);
    }

    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
        if (source == alignSlider) update((int) alignSlider.getValue());
    }

    private void update(int index) {
        updatePicture(index);
        alignSlider.setValue(index);
        indexField.setText(Integer.toString(index));
        weightField.setText(Double.toString(aligns.get(index).getWeight()));
        PropertyChangeHandler.firePropertyChange(PropertyChangeIDs.CHANGE_ALIGNMENT.toString(), null, aligns.get(index));
    }

    public void propertyChange(PropertyChangeEvent e) {
        if (!"value".equals(e.getPropertyName())) return;
        if (e.getSource() == indexField && ok2update.get(indexField)) {
            Number value = (Number) e.getNewValue();
            if (value != null) {
                update(value.intValue());
                ok2update.put(indexField, false);
            }
        } else if (e.getSource() == weightField && ok2update.get(weightField)) {
            Float weight = (Float) e.getNewValue();
            int index = getIndexFromWeight(weight.floatValue());
            if (index >= 0 && index <= maxSliderVal) {
                update(index);
                ok2update.put(weightField, false);
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        int curr = (int) alignSlider.getValue();
        if (e.getSource() == nextButton || e.getSource() == timer) {
            if (curr == maxSliderVal) update(0); else update(curr + 1);
        } else if (e.getSource() == prevButton) {
            if (curr == 0) update(maxSliderVal); else update(curr - 1);
        } else if (e.getSource() == startStopButton) {
            if (frozen) {
                timer.start();
                frozen = false;
                startStopButton.setText("Stop Animation");
            } else {
                timer.stop();
                frozen = true;
                startStopButton.setText("Start Animation");
            }
        }
    }

    protected void updatePicture(int frameNum) {
        alignPanel.setIndex(frameNum);
        repaint();
    }

    protected int calcTickSpacing(int val) {
        return ((val / 10) - (val % 10));
    }

    private int getIndexFromWeight(float w) {
        return bSearch(0, aligns.size() - 1, w);
    }

    private int bSearch(int low, int high, float weight) {
        if (high < low) return -1;
        if (weight <= aligns.get(high).getWeight()) return high;
        if (weight >= aligns.get(low).getWeight()) return low;
        if (low == high - 1) return low;
        int mid = (high + low) / 2;
        if (weight < aligns.get(mid).getWeight()) return bSearch(mid, high, weight); else if (weight > aligns.get(mid).getWeight()) return bSearch(low, mid, weight); else return mid;
    }

    private void handleEnterKeyStroke(final JFormattedTextField field) {
        field.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "check");
        field.getActionMap().put("check", new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                if (!field.isEditValid()) {
                    Toolkit.getDefaultToolkit().beep();
                    field.selectAll();
                } else try {
                    ok2update.put(field, true);
                    field.commitEdit();
                } catch (java.text.ParseException exc) {
                }
            }
        });
    }
}
