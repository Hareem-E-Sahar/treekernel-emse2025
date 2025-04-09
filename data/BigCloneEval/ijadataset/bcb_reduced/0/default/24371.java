import vtk.util.VtkPanelContainer;
import vtk.vtkActor2D;
import vtk.vtkPanel;
import vtk.vtkTextMapper;
import vtk.vtkTextProperty;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * This example displays all possible combinations of font families and
 * styles.
 */
public class DispAllFonts extends JComponent implements VtkPanelContainer {

    private static final int MIN_FONT_SIZE = 5;

    private static final int MAX_FONT_SIZE = 50;

    private static final String DEFAULT_TEXT = "ABCDEFGHIJKLMnopqrstuvwxyz 0123456789, !@#%()-=_+{};:,./<>?";

    private static final double[] TEXT_COLOR = { 246 / 255.0, 255 / 255.0, 11 / 255.0 };

    private static final double[] BG_COLOR = { 56 / 255.0, 56 / 255.0, 154 / 255.0 };

    private static final String[] TEXT_FAMILY = { "Arial", "Courier", "Times" };

    private static final int[][] BOLD_ITALIC_SHADOW = new int[][] { { 0, 0, 0 }, { 0, 0, 1 }, { 1, 0, 0 }, { 0, 1, 0 }, { 1, 1, 0 } };

    private int current_font_size = 16;

    private JSlider slider = new JSlider();

    private ArrayList textActors = new ArrayList();

    private ArrayList textMappers = new ArrayList();

    private vtkPanel renWin = new vtkPanel();

    public DispAllFonts() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        for (int familyIndex = 0; familyIndex < TEXT_FAMILY.length; ++familyIndex) {
            for (int styleIndex = 0; styleIndex < BOLD_ITALIC_SHADOW.length; ++styleIndex) {
                vtkTextMapper mapper = new vtkTextMapper();
                int bold = BOLD_ITALIC_SHADOW[styleIndex][0];
                int italic = BOLD_ITALIC_SHADOW[styleIndex][1];
                int shadow = BOLD_ITALIC_SHADOW[styleIndex][2];
                String attrib = "";
                if (bold != 0) {
                    attrib += "b";
                }
                if (italic != 0) {
                    if (attrib.length() > 0) attrib += ",";
                    attrib += "i";
                }
                if (shadow != 0) {
                    if (attrib.length() > 0) attrib += ",";
                    attrib += "s";
                }
                String faceName = TEXT_FAMILY[familyIndex] + " " + attrib;
                mapper.SetInput(faceName + ": " + DEFAULT_TEXT);
                vtkTextProperty tprop = mapper.GetTextProperty();
                String methodName = "SetFontFamilyTo" + TEXT_FAMILY[familyIndex];
                Method method = tprop.getClass().getMethod(methodName, null);
                method.invoke(tprop, null);
                tprop.SetColor(TEXT_COLOR);
                tprop.SetBold(bold);
                tprop.SetItalic(italic);
                tprop.SetShadow(shadow);
                vtkActor2D actor = new vtkActor2D();
                actor.SetMapper(mapper);
                textActors.add(actor);
                renWin.GetRenderer().AddActor(actor);
                textMappers.add(mapper);
            }
        }
        slider.setMinimum(MIN_FONT_SIZE);
        slider.setMaximum(MAX_FONT_SIZE);
        slider.setMajorTickSpacing(5);
        slider.setMinorTickSpacing(1);
        slider.setSnapToTicks(true);
        slider.setPaintLabels(true);
        slider.setPaintTicks(true);
        slider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (e.getSource() == slider) {
                    current_font_size = slider.getValue();
                    renWin.setSize(800, 20 + textActors.size() * (current_font_size + 5));
                    for (int i = 0; i < textActors.size(); i++) {
                        vtkActor2D actor = (vtkActor2D) textActors.get(i);
                        vtkTextMapper mapper = (vtkTextMapper) textMappers.get(i);
                        mapper.GetTextProperty().SetFontSize(current_font_size);
                        actor.SetDisplayPosition(10, (i + 1) * (current_font_size + 5));
                    }
                }
            }
        });
        renWin.GetRenderer().SetBackground(BG_COLOR);
        setLayout(new BorderLayout());
        add(slider, BorderLayout.NORTH);
        add(renWin, BorderLayout.CENTER);
        slider.setValue(current_font_size);
    }

    public vtkPanel getRenWin() {
        return renWin;
    }

    public static void main(String s[]) {
        try {
            DispAllFonts panel = new DispAllFonts();
            JFrame frame = new JFrame("DispAllFonts");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add("Center", panel);
            frame.pack();
            frame.setVisible(true);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
