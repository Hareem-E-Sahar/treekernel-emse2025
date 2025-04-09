import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * @author Massimo Bartoletti
 * @version 1.1
 */
public class CGDesktop extends JDesktopPane {

    public int PALETTE_X = 375;

    public int PALETTE_Y = 20;

    private final Integer CANVAS_LAYER = new Integer(1);

    private final Integer PALETTE_LAYER = new Integer(2);

    private CGPalette palette;

    private CGDemoModule demo;

    private CGEditingMode editingMode;

    private JFileChooser fileChooser;

    private int canvasCount = 0;

    static final String CG_DIRECTORY = "CGTutorial";

    public CGDesktop(CGDemoModule demo) {
        this.demo = demo;
        createPalette();
        createCanvas();
        try {
            String home = System.getProperty("user.home");
            String separator = System.getProperty("file.separator");
            File root = new File(home + separator + CG_DIRECTORY);
            fileChooser = new JFileChooser(root);
        } catch (Exception ex) {
            palette.disableTool("OpenTool");
            palette.disableTool("SaveTool");
        }
    }

    public void createPalette() {
        try {
            String paletteName = demo.getString(getResourceName() + ".palette");
            Class paletteClass = Class.forName(paletteName);
            Constructor paletteConstructor = paletteClass.getConstructor(new Class[] { CGDesktop.class });
            Object[] args = new Object[] { this };
            palette = (CGPalette) paletteConstructor.newInstance(args);
            palette.setLocation(PALETTE_X, PALETTE_Y);
            palette.show();
            add(palette, PALETTE_LAYER);
        } catch (Exception ex) {
            getDemo().setStatus("Cannot create palette: " + ex);
        }
    }

    public CGCanvas createCanvas() {
        try {
            String canvasName = demo.getString(getResourceName() + ".canvas");
            Class canvasClass = Class.forName(canvasName);
            Constructor canvasConstructor = canvasClass.getConstructor(new Class[] { getClass() });
            Object[] args = new Object[] { this };
            CGCanvas canvas = (CGCanvas) canvasConstructor.newInstance(args);
            CanvasFrame iframe = new CanvasFrame(this, canvas, ++canvasCount);
            iframe.show();
            add(iframe, CANVAS_LAYER);
            try {
                iframe.setSelected(true);
                setActiveFrame(iframe);
            } catch (java.beans.PropertyVetoException ex) {
            }
            return canvas;
        } catch (Exception ex) {
            getDemo().setStatus("Cannot create a new canvas: " + ex);
            ex.printStackTrace();
            return null;
        }
    }

    public CGDemoModule getDemo() {
        return demo;
    }

    public CGPalette getPalette() {
        return palette;
    }

    public String getResourceName() {
        return getDemo().getResourceName();
    }

    public String getString(String key) {
        return getDemo().getString(key);
    }

    /************************************************************
	 *                  PEEKING ACTIVE CANVAS
	 ************************************************************/
    private CanvasFrame activeFrame;

    private CanvasFrame getActiveFrame() {
        return activeFrame;
    }

    public void setActiveFrame(CanvasFrame iframe) {
        activeFrame = iframe;
    }

    public CGCanvas getSelectedCanvas() {
        CGCanvas selected = null;
        try {
            CanvasFrame iframe = (CanvasFrame) getActiveFrame();
            if (iframe != null && !iframe.isClosed()) selected = (CGCanvas) iframe.getCanvas();
        } catch (Exception ex) {
        }
        return selected;
    }

    /************************************************************
	 *                  EDITING MODE OPERATIONS
	 ************************************************************/
    public CGEditingMode getEditingMode() {
        return editingMode;
    }

    public void setEditingMode(CGEditingMode mode) {
        editingMode = mode;
    }

    /************************************************************
	 *                    OPEN / SAVE TOOLS
	 ************************************************************/
    public void open() {
        if (fileChooser == null) return;
        int action = fileChooser.showOpenDialog(this);
        if (action == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            CGCanvas canvas = createCanvas();
            try {
                if (canvas != null) canvas.open(file);
            } catch (Exception ex) {
                JInternalFrame iframe = getSelectedFrame();
                iframe.dispose();
                showOpenFailedDialog(ex.getMessage());
            }
        }
    }

    public void save() {
        if (fileChooser == null) return;
        CGCanvas canvas = getSelectedCanvas();
        if (canvas != null) {
            int action = fileChooser.showSaveDialog(this);
            if (action == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    canvas.save(file);
                } catch (Exception ex) {
                    showSaveFailedDialog(ex.getMessage());
                }
            }
        }
    }

    public void showOpenFailedDialog(String msg) {
        msg += "\n" + "Cannot open file.";
        JOptionPane.showMessageDialog(this, msg, "Open Failed", JOptionPane.ERROR_MESSAGE);
    }

    public void showSaveFailedDialog(String msg) {
        msg += "\n" + "Cannot save file.";
        JOptionPane.showMessageDialog(this, msg, "Save Failed", JOptionPane.ERROR_MESSAGE);
    }

    /************************************************************
	 *                    CanvasFrame (nested class)
	 ************************************************************/
    static class CanvasFrame extends JInternalFrame {

        private static final int xOffset = 30, yOffset = 30;

        private CGDesktop desktop;

        private CGCanvas canvas;

        private CanvasFrame iframe;

        public CanvasFrame(CGDesktop desk, CGCanvas cnv, int canvasCount) {
            super("Canvas #" + canvasCount);
            iframe = this;
            this.desktop = desk;
            this.canvas = cnv;
            setClosable(true);
            setMaximizable(true);
            setIconifiable(true);
            setResizable(true);
            setLocation(xOffset * (canvasCount % 10), yOffset * (canvasCount % 10));
            JScrollPane scrollPane = new JScrollPane(canvas);
            getContentPane().add(scrollPane);
            pack();
            addInternalFrameListener(new InternalFrameAdapter() {

                public void internalFrameActivated(InternalFrameEvent e) {
                    desktop.setActiveFrame(iframe);
                    CGPalette palette = desktop.getPalette();
                    if (palette != null) palette.update();
                    canvas.update();
                }
            });
        }

        public CGCanvas getCanvas() {
            return canvas;
        }
    }
}
