package mobat.bonesa;

import java.lang.reflect.InvocationTargetException;
import javax.swing.JPanel;
import mobat.bonesa.visualization.IBonesaVisualization;

public class VisualizationSettings {

    protected Class<? extends IBonesaVisualization> className;

    protected String helpTopic;

    protected String name;

    public VisualizationSettings(String name, Class<? extends IBonesaVisualization> className, String helpTopic) {
        this.name = name;
        this.className = className;
        this.helpTopic = helpTopic;
    }

    public String toString() {
        String[] pso = this.name.split(">");
        return pso[pso.length - 1];
    }

    public String getName() {
        return name;
    }

    public String getHelpTopic() {
        return helpTopic;
    }

    public IBonesaVisualization newVisualizationInstance(JPanel panel) {
        try {
            return className.getConstructor(JPanel.class).newInstance(panel);
        } catch (Throwable e) {
            return null;
        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof VisualizationSettings)) {
            if (o.getClass() == className) {
                return true;
            }
            return false;
        }
        if (((VisualizationSettings) o).className.equals(className)) {
            return true;
        }
        return false;
    }
}
