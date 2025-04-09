import javax.swing.*;
import java.util.*;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.*;
import java.lang.reflect.*;

public abstract class CGPalette extends JInternalFrame {

    private final String[] defaultTools = { "NewCanvasTool", "OpenTool", "SaveTool", "ClearTool", "ZoomInTool", "ZoomOutTool" };

    private static final int COLUMNS_NUMBER = 3;

    private CGDesktop desktop;

    private JPanel panel, row;

    private int columns;

    private Map tools;

    private Map groups;

    public CGPalette(CGDesktop desk) {
        this.desktop = desk;
        putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
        getContentPane().setLayout(new BorderLayout());
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        getContentPane().add(panel);
        row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        columns = 0;
        tools = new HashMap();
        groups = new HashMap();
        for (int i = 0; i < defaultTools.length; i++) {
            try {
                CGCanvasTool tool = loadTool(defaultTools[i]);
                if (tool != null) addTool(tool);
            } catch (Exception ex) {
                getDemo().setStatus("Cannot load tool " + defaultTools[i]);
            }
        }
        String[] moreTools = getTools();
        for (int i = 0; i < moreTools.length; i++) {
            if (moreTools[i] == null) addTool(null); else try {
                CGCanvasTool tool = loadTool(moreTools[i]);
                if (tool != null) addTool(tool);
            } catch (Exception ex) {
                getDemo().setStatus("Cannot load tool " + moreTools[i]);
            }
        }
        pack();
    }

    public abstract String[] getTools();

    public CGCanvasTool loadTool(String toolName) throws Exception {
        CGCanvasTool tool = null;
        String toolClassName = getString(toolName + ".class");
        Class toolClass = Class.forName(toolClassName);
        Constructor toolConstructor = toolClass.getConstructor(new Class[] { CGPalette.class });
        Object[] args = new Object[] { this };
        tool = (CGCanvasTool) toolConstructor.newInstance(args);
        String groupName = tool.getGroup();
        if (groupName != null) {
            ButtonGroup group = null;
            if (groups.containsKey(groupName)) {
                group = (ButtonGroup) groups.get(groupName);
            } else {
                group = new ButtonGroup();
                groups.put(groupName, group);
            }
            group.add(tool);
        }
        return tool;
    }

    public void addTool(CGCanvasTool tool) {
        if (columns == 0) {
            panel.add(row);
        } else if (columns >= getColumnsNumber()) {
            row = new JPanel();
            columns = 0;
            row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
            panel.add(row);
        }
        if (tool != null) {
            tools.put(tool.getToolName(), tool);
            row.add(tool);
        }
        ++columns;
    }

    public void enableTool(String toolName) {
        CGCanvasTool tool = (CGCanvasTool) tools.get(toolName);
        if (tool != null) tool.setEnabled(true);
    }

    public void disableTool(String toolName) {
        CGCanvasTool tool = (CGCanvasTool) tools.get(toolName);
        if (tool != null) tool.setEnabled(false);
    }

    /**
	 * Returns the canvas currently active in the desktop. If no canvas
	 * is active, returns <code>null</code>.
	 * @return the currently active canvas.
	 */
    public CGCanvas getSelectedCanvas() {
        return getDesktop().getSelectedCanvas();
    }

    /**
	 * Refreshes the palette's appereance. This method is called by
	 * the desktop after an <code>InternalFrameEvent</code> occurred.
	 */
    public void update() {
        Iterator i = tools.values().iterator();
        while (i.hasNext()) {
            CGCanvasTool tool = (CGCanvasTool) i.next();
            tool.update();
        }
    }

    /**
	 * Returns the number of columns this palette spans. This method
	 * may be overriden by subclasses which want a different number of
	 * columns in their palette.
	 * @return the number of palette's columns.
	 */
    public int getColumnsNumber() {
        return COLUMNS_NUMBER;
    }

    public CGDesktop getDesktop() {
        return desktop;
    }

    public CGDemoModule getDemo() {
        return getDesktop().getDemo();
    }

    public String getString(String key) {
        return getDesktop().getString(key);
    }
}
