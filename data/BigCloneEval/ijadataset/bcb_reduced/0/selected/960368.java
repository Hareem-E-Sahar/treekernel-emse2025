package graphlab.gui.plugins.main.core;

import graphlab.gui.core.graph.GShape;
import graphlab.gui.core.graph.GStroke;
import graphlab.gui.core.graph.PolygonArrow;
import graphlab.main.core.BlackBoard.BlackBoard;
import graphlab.ui.components.GPropertyEditor.GPropertyEditor;
import graphlab.ui.components.GPropertyEditor.editors.inplace.ArrowEditor;
import graphlab.ui.components.GPropertyEditor.editors.inplace.GSimpleComboEditor;
import graphlab.ui.components.GPropertyEditor.editors.inplace.GStrokeEditor;
import graphlab.ui.components.GPropertyEditor.renderers.ArrowRenderer;
import graphlab.ui.components.GPropertyEditor.renderers.GStrokeRenderer;
import javax.swing.*;
import java.lang.reflect.Constructor;
import java.net.URL;

/**
 * @author reza
 */
public class Init implements graphlab.main.plugin.PluginInterface {

    public static Class uiClass = null;

    public void init(BlackBoard blackboard) {
        try {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            uiClass = getClass().getClassLoader().loadClass("graphlab.ui.UI");
            Constructor uiConst = uiClass.getConstructor(BlackBoard.class, boolean.class);
            Object o[] = { blackboard, true };
            Object UIo = uiConst.newInstance(o);
            Class c[] = { String.class, Class.class };
            Object o2[] = { "/graphlab/gui/plugins/main/core/SampleUI.xml", getClass() };
            uiClass.getMethod("loadXML", c).invoke(UIo, o2);
            GPropertyEditor.registerRenderer(PolygonArrow.class, new ArrowRenderer());
            GPropertyEditor.registerEditor(PolygonArrow.class, new ArrowEditor());
            GPropertyEditor.registerRenderer(GShape.class, GShape.renderer);
            GPropertyEditor.registerRenderer(GStroke.class, new GStrokeRenderer());
            GPropertyEditor.registerEditor(GStroke.class, new GStrokeEditor());
            GPropertyEditor.registerEditor(GShape.class, new GSimpleComboEditor(GShape.editor));
            graphlab.ui.components.GFrame gFrame = graphlab.ui.UI.getGFrame(blackboard);
            gFrame.setTitle("GraphLab");
            URL resource = getClass().getResource("graphlab/graphlab_128.ico");
            if (resource != null) {
                ImageIcon icon = new ImageIcon(resource);
                gFrame.setIconImage(icon.getImage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
