package com.mystictri.neotextureedit;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.TooManyListenersException;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import com.mystictri.neotexture.TextureGraph;
import com.mystictri.neotexture.TextureGraph.TextureNodeConnection;
import com.mystictri.neotexture.TextureGraphListener;
import com.mystictri.neotexture.TextureGraphNode;
import com.mystictri.neotexture.TextureGraphNode.ConnectionPoint;
import engine.base.Logger;
import engine.graphics.synthesis.texture.CacheTileManager;
import engine.graphics.synthesis.texture.Channel;
import engine.graphics.synthesis.texture.Channel.ChannelVizType;
import engine.graphics.synthesis.texture.ChannelChangeListener;
import engine.graphics.synthesis.texture.Pattern;

/**
 * This is the main texture graph editing panel that is used to create and modify
 * a texture generation graph using TextureGraphNode objects.
 * @author Holger Dammertz
 *
 */
public final class TextureGraphEditorPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, ActionListener, ChannelChangeListener, TextureGraphListener {

    private static final long serialVersionUID = 4535161419971720668L;

    int dragStartX = 0;

    int dragStartY = 0;

    private static final Color col_NodeBG = new Color(128, 128, 128);

    private static final Color col_NodeSlowBG = new Color(128 + 64, 64, 64);

    private static final Color col_NodeBorder = new Color(255, 255, 255);

    private static final Color col_NodeSelected = new Color(255, 255, 0);

    private static final Color col_NodeOutPort = new Color(0, 230, 64);

    private static final Color col_NodeInPort = new Color(230, 64, 0);

    private static final BasicStroke connectionLineStroke = new BasicStroke(1.5f);

    private static final Font font = new Font("Sans", Font.PLAIN, 10);

    private static final int helpW = 16;

    private static final int helpH = 16;

    private static final int helpX = TextureGraphNode.width - helpW;

    private static final int helpY = 0;

    boolean nodeDragging = false;

    boolean desktopDragging = false;

    boolean connectionDragging = false;

    PreviewWindow draggedWindow = null;

    TextureGraphNode.ConnectionPoint connectionSource = null;

    Point connectionOrigin;

    Point connectionTarget;

    Point mousePosition = new Point();

    JPopupMenu newChannelPopupMenu;

    JMenuItem newChannelInsertMenuItem;

    JPopupMenu selectedChannelPopupMenu;

    JMenuItem addToPresetsChannelMenuItem;

    TextureGraphNode toCopyTextureGraphNode;

    JMenuItem previewChannelMenuItem;

    JMenuItem copyChannelMenuItem;

    JMenuItem replacepasteChannelMenuItem;

    JMenuItem swapInputsChannelMenuItem;

    JMenuItem deleteChannelMenuItem;

    JMenuItem cloneChannelMenuItem;

    JMenuItem openGLDiffuseMenuItem;

    JMenuItem openGLNormalMenuItem;

    JMenuItem openGLSpecularMenuItem;

    JMenuItem openGLHeightmapMenuItem;

    JMenu replaceChannelMenu;

    ChannelParameterEditorPanel paramEditorPanel;

    TextureGraph graph;

    /**
	 * This interface is used by the TextureGraphEditorPanel to notify about changes in the
	 * graph when edit operations are performed.
	 * @author Holger Dammertz
	 *
	 */
    public static interface EditChangeListener {

        /** Called on each edit operation that changes the graph or the graph layout */
        public void graphWasEdited();
    }

    final Vector<EditChangeListener> editChangeListener = new Vector<EditChangeListener>();

    /** 
	 * Used in the draw method to generate a preview image that is cached
	 * @author Holger Dammertz
	 *
	 */
    class NodePreviewImage implements ChannelChangeListener {

        BufferedImage previewImage;

        TextureGraphNode node;

        public NodePreviewImage(TextureGraphNode node) {
            this.node = node;
            node.getChannel().addChannelChangeListener(this);
            updatePreviewImage();
        }

        public void updatePreviewImage() {
            if ((node.getChannel() != null) && (node.getChannel().chechkInputChannels())) {
                if (previewImage == null) previewImage = ChannelUtils.createAndComputeImage(node.getChannel(), 64, 64, null, 0); else ChannelUtils.computeImage(node.getChannel(), previewImage, null, 0);
            } else {
                previewImage = null;
            }
            repaint();
        }

        public void channelChanged(Channel source) {
            updatePreviewImage();
        }
    }

    int desktopX, desktopY;

    public class TextureGraphDropTarget extends DropTargetAdapter {

        public void drop(DropTargetDropEvent e) {
            TextureGraphNode n = new TextureGraphNode(Channel.cloneChannel(TextureEditor.INSTANCE.dragndropChannel));
            addTextureNode(n, e.getLocation().x - desktopX, e.getLocation().y - desktopY);
            repaint();
        }
    }

    public TextureGraphEditorPanel() {
        graph = new TextureGraph();
        graph.graphListener = this;
        setBackground(Color.darkGray);
        setLayout(null);
        addMouseListener(this);
        addMouseWheelListener(this);
        addMouseMotionListener(this);
        createPopupMenus();
        paramEditorPanel = new ChannelParameterEditorPanel();
        DropTarget t = new DropTarget();
        try {
            t.addDropTargetListener(new TextureGraphDropTarget());
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        }
        setDropTarget(t);
        setPreferredSize(new Dimension(512, 512));
    }

    public ChannelParameterEditorPanel getParameterEditorPanel() {
        return paramEditorPanel;
    }

    private void createPopupMenus() {
        newChannelPopupMenu = new JPopupMenu("Create Channel");
        replaceChannelMenu = new JMenu("Replace Channel");
        newChannelInsertMenuItem = createChannelPopupMenuItem(newChannelPopupMenu, "Paste Node");
        newChannelPopupMenu.addSeparator();
        for (Class<?> c : TextureEditor.INSTANCE.allPatterns) {
            newChannelPopupMenu.add(createMenuItem_CreateFilter(c.getSimpleName(), c, true, false));
            replaceChannelMenu.add(createMenuItem_CreateFilter(c.getSimpleName(), c, true, true));
        }
        newChannelPopupMenu.addSeparator();
        replaceChannelMenu.addSeparator();
        for (Class<?> c : TextureEditor.INSTANCE.allChannels) {
            newChannelPopupMenu.add(createMenuItem_CreateFilter(c.getSimpleName(), c, false, false));
            replaceChannelMenu.add(createMenuItem_CreateFilter(c.getSimpleName(), c, false, true));
        }
        selectedChannelPopupMenu = new JPopupMenu();
        cloneChannelMenuItem = createChannelPopupMenuItem(selectedChannelPopupMenu, "Clone");
        copyChannelMenuItem = createChannelPopupMenuItem(selectedChannelPopupMenu, "Copy");
        replacepasteChannelMenuItem = createChannelPopupMenuItem(selectedChannelPopupMenu, "Paste Replace");
        selectedChannelPopupMenu.addSeparator();
        previewChannelMenuItem = createChannelPopupMenuItem(selectedChannelPopupMenu, "Preview Node");
        JMenu exportImage = new JMenu("Export Image");
        selectedChannelPopupMenu.add(exportImage);
        createChannelPopupSubMenuItem(exportImage, "256x256");
        createChannelPopupSubMenuItem(exportImage, "512x512");
        createChannelPopupSubMenuItem(exportImage, "1024x1024");
        createChannelPopupSubMenuItem(exportImage, "specify").setActionCommand("arbitraryResolutionExport");
        addToPresetsChannelMenuItem = createChannelPopupMenuItem(selectedChannelPopupMenu, "Add to Presets");
        swapInputsChannelMenuItem = createChannelPopupMenuItem(selectedChannelPopupMenu, "Swap First 2 Inputs");
        selectedChannelPopupMenu.add(replaceChannelMenu);
        selectedChannelPopupMenu.addSeparator();
        if (TextureEditor.GL_ENABLED) {
            JMenu opengl = new JMenu("OpenGL");
            selectedChannelPopupMenu.add(opengl);
            openGLDiffuseMenuItem = createChannelPopupSubMenuItem(opengl, "Set as Diffuse");
            openGLNormalMenuItem = createChannelPopupSubMenuItem(opengl, "Set as Normal");
            openGLSpecularMenuItem = createChannelPopupSubMenuItem(opengl, "Set as Specular");
            openGLHeightmapMenuItem = createChannelPopupSubMenuItem(opengl, "Set as Height");
        }
        deleteChannelMenuItem = createChannelPopupMenuItem(selectedChannelPopupMenu, "Delete");
    }

    private JMenuItem createChannelPopupSubMenuItem(JMenu menu, String name) {
        JMenuItem ret = new JMenuItem(name);
        ret.addActionListener(this);
        menu.add(ret);
        return ret;
    }

    private JMenuItem createChannelPopupMenuItem(JPopupMenu menu, String name) {
        JMenuItem ret = new JMenuItem(name);
        ret.addActionListener(this);
        menu.add(ret);
        return ret;
    }

    class CreateMenuItem extends JMenuItem {

        private static final long serialVersionUID = 3053710502290069301L;

        public Class<?> classType;

        public boolean isAReplaceCall;

        public CreateMenuItem(String name, boolean isReplace) {
            super(name);
            isAReplaceCall = isReplace;
        }

        public CreateMenuItem(String name, ImageIcon icon, boolean isReplace) {
            super(name, icon);
            isAReplaceCall = isReplace;
        }
    }

    private CreateMenuItem createMenuItem_CreateFilter(String name, Class<?> c, boolean genIcon, boolean replace) {
        CreateMenuItem ret;
        if (genIcon) {
            Channel chan = null;
            try {
                chan = (Channel) c.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            ret = new CreateMenuItem(name, new ImageIcon(ChannelUtils.createAndComputeImage(chan, 16, 16, null, 0)), replace);
        } else ret = new CreateMenuItem(name, replace);
        ret.classType = c;
        ret.addActionListener(this);
        return ret;
    }

    private void askFileAndExportTexture(int resX, int resY) {
        TextureEditor.INSTANCE.m_TextureFileChooser_SaveLoadImage.setDialogTitle("Export Texture to " + resX + "x" + resY + " Image...");
        if (TextureEditor.INSTANCE.m_TextureFileChooser_SaveLoadImage.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String name = TextureEditor.INSTANCE.m_TextureFileChooser_SaveLoadImage.getSelectedFile().getAbsolutePath();
            if (!name.endsWith(".png")) name += ".png";
            boolean useCache = ChannelUtils.useCache;
            try {
                ChannelUtils.useCache = false;
                ImageIO.write(ChannelUtils.createAndComputeImage(graph.selectedNodes.lastElement().getChannel(), resX, resY, TextureEditor.INSTANCE.m_ProgressDialog, 3), "png", new File(name));
                Logger.log(this, "Saved image to " + name + ".");
            } catch (IOException exc) {
                exc.printStackTrace();
                Logger.logError(this, "IO Exception while exporting image: " + exc);
            }
            ChannelUtils.useCache = useCache;
        }
    }

    private void action_DeleteSelectedNodes() {
        graph.deleteSelection();
        repaint();
    }

    /**
	 * This is the main action method for TextureGraphEditorPanel. Here the actions from the popup-menus
	 * are processed.
	 */
    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println(e);
        if (e.getSource().getClass() == CreateMenuItem.class) {
            CreateMenuItem mi = (CreateMenuItem) e.getSource();
            try {
                if (!mi.isAReplaceCall) {
                    Channel chan = (Channel) mi.classType.newInstance();
                    addTextureNode(new TextureGraphNode(chan), mousePosition.x - desktopX, mousePosition.y - desktopY);
                    repaint();
                } else {
                    TextureGraphNode node = graph.selectedNodes.get(0);
                    if (node != null) {
                        TextureGraphNode newNode = new TextureGraphNode((Channel) mi.classType.newInstance());
                        replaceTextureNode(node, newNode);
                        repaint();
                    } else {
                        Logger.logWarning(this, "No node selected for replace.");
                    }
                }
            } catch (InstantiationException e1) {
                e1.printStackTrace();
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
            }
        } else if (e.getSource() == newChannelInsertMenuItem) {
            if (toCopyTextureGraphNode == null) {
                Logger.logError(this, "No node copied to insert.");
            } else {
                addTextureNode(toCopyTextureGraphNode.cloneThisNode(), mousePosition.x - desktopX, mousePosition.y - desktopY);
                repaint();
            }
        } else if (e.getSource() == copyChannelMenuItem) {
            if (graph.selectedNodes.size() > 0) {
                toCopyTextureGraphNode = graph.selectedNodes.get(0).cloneThisNode();
            } else {
                Logger.logError(this, "no selection in copyChannel popup menu.");
            }
        } else if (e.getSource() == replacepasteChannelMenuItem) {
            if (toCopyTextureGraphNode == null) {
                Logger.logError(this, "No node copied to replace paste.");
            } else if (graph.selectedNodes.size() > 0) {
                replaceTextureNode(graph.selectedNodes.get(0), toCopyTextureGraphNode.cloneThisNode());
                repaint();
            } else {
                Logger.logError(this, "no selection in insert-replaceChannel popup menu.");
            }
        } else if (e.getSource() == previewChannelMenuItem) {
            if (graph.selectedNodes.size() > 0) {
                addPreviewWindow(graph.selectedNodes.get(0));
                repaint();
            }
        } else if (e.getSource() == cloneChannelMenuItem) {
            if (graph.selectedNodes.size() > 0) {
                TextureGraphNode orig = graph.selectedNodes.get(0);
                TextureGraphNode n = new TextureGraphNode(Channel.cloneChannel(graph.selectedNodes.get(0).getChannel()));
                addTextureNode(n, orig.getX() + 32, orig.getY() + 32);
                repaint();
            } else {
                Logger.logError(this, "no selection in cloneChannel popup menu.");
            }
        } else if (e.getSource() == swapInputsChannelMenuItem) {
            TextureGraphNode node = graph.selectedNodes.get(0);
            if (node != null) {
                if (node.getChannel().getNumInputChannels() < 2) return;
                ConnectionPoint p0 = node.getInputConnectionPointByChannelIndex(0);
                ConnectionPoint p1 = node.getInputConnectionPointByChannelIndex(1);
                TextureNodeConnection c0 = graph.getConnectionAtInputPoint(p0);
                TextureNodeConnection c1 = graph.getConnectionAtInputPoint(p1);
                graph.removeConnection(c0);
                graph.removeConnection(c1);
                if (c0 != null && c1 != null) {
                    ConnectionPoint temp = c0.target;
                    c0.target = c1.target;
                    c1.target = temp;
                    graph.addConnection(c0);
                    graph.addConnection(c1);
                } else if (c1 != null) {
                    c1.target = p0;
                    graph.addConnection(c1);
                } else if (c0 != null) {
                    c0.target = p1;
                    graph.addConnection(c0);
                } else {
                    return;
                }
                repaint();
            }
        } else if (e.getSource() == addToPresetsChannelMenuItem) {
            if (graph.selectedNodes.size() > 0) {
                if (graph.selectedNodes.get(0).getChannel() instanceof Pattern) TextureEditor.INSTANCE.m_PatternSelector.addPatternPreset((Pattern) Channel.cloneChannel((Pattern) graph.selectedNodes.get(0).getChannel())); else Logger.logError(this, "Invalid action 'Add to Presets': selected node is not a pattern");
            } else Logger.logError(this, "Invalid action 'Add To Presets': no selected nodes exists.");
        } else if (e.getSource() == deleteChannelMenuItem) {
            action_DeleteSelectedNodes();
        } else if (e.getActionCommand().equals("arbitraryResolutionExport")) {
            String resolution = JOptionPane.showInputDialog(this, "Specify your desried resolution (for example 1024x1024)", "What Resolution?", JOptionPane.QUESTION_MESSAGE);
            if (resolution != null && resolution.matches("\\d+x\\d+")) {
                int resX = Integer.parseInt(resolution.substring(0, resolution.indexOf('x')));
                int resY = Integer.parseInt(resolution.substring(resolution.indexOf('x') + 1, resolution.length()));
                askFileAndExportTexture(resX, resY);
            }
        } else if (e.getActionCommand().matches("\\d+x\\d+")) {
            String s = e.getActionCommand();
            int resX = Integer.parseInt(s.substring(0, s.indexOf('x')));
            int resY = Integer.parseInt(s.substring(s.indexOf('x') + 1, s.length()));
            askFileAndExportTexture(resX, resY);
        } else {
            if (TextureEditor.GL_ENABLED) {
                TextureGraphNode n = graph.selectedNodes.get(0);
                if (n.getChannel().chechkInputChannels()) {
                    if (e.getSource() == openGLDiffuseMenuItem) {
                        TextureEditor.INSTANCE.m_OpenGLPreviewPanel.setDiffuseTextureNode(n);
                        repaint();
                    } else if (e.getSource() == openGLNormalMenuItem) {
                        TextureEditor.INSTANCE.m_OpenGLPreviewPanel.setNormalTextureNode(n);
                        repaint();
                    } else if (e.getSource() == openGLSpecularMenuItem) {
                        TextureEditor.INSTANCE.m_OpenGLPreviewPanel.setSpecWeightTextureNode(n);
                        repaint();
                    } else if (e.getSource() == openGLHeightmapMenuItem) {
                        TextureEditor.INSTANCE.m_OpenGLPreviewPanel.setHeightmapTextureNode(n);
                        repaint();
                    }
                } else Logger.logWarning(this, "Incomplete channel for preview.");
            }
        }
    }

    void addTextureNode(TextureGraphNode n, int x, int y) {
        graph.addNode(n, x, y);
        setSelectedNode(n);
    }

    void replaceTextureNode(TextureGraphNode oldNode, TextureGraphNode newNode) {
        graph.replaceNode(oldNode, newNode);
        setSelectedNode(newNode);
    }

    public void setSelectedNode(TextureGraphNode node) {
        paramEditorPanel.setTextureNode(node);
        graph.setSelectedNode(node);
    }

    public void addSelectedNode(TextureGraphNode node) {
        graph.addOrRemoveNodeToSelection(node);
    }

    public boolean isNodeInSelection(TextureGraphNode node) {
        return graph.selectedNodes.contains(node);
    }

    public void deleteFullGraph() {
        paramEditorPanel.setTextureNode(null);
        removeAllPreviewWindows();
        graph.deleteFullGraph();
        CacheTileManager.clearCache();
        repaint();
    }

    public void save(String filename) {
        Logger.log(this, "Saving TextureGraph to " + filename);
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(filename));
            graph.save(w);
            if (TextureEditor.GL_ENABLED) TextureEditor.INSTANCE.m_OpenGLPreviewPanel.save(w);
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean load(String filename, boolean eraseOld) {
        try {
            Scanner s = new Scanner(new BufferedReader(new FileReader(filename)));
            if (eraseOld) deleteFullGraph();
            graph.load(s);
            if (TextureEditor.GL_ENABLED) TextureEditor.INSTANCE.m_OpenGLPreviewPanel.load(s);
            repaint();
            return true;
        } catch (FileNotFoundException e) {
            Logger.logError(this, "Could not load " + filename);
            return false;
        } catch (InputMismatchException ime) {
            ime.printStackTrace();
            Logger.logError(this, "Could not load " + filename);
            return false;
        }
    }

    @Override
    public void nodeDeleted(TextureGraphNode node) {
        if (TextureEditor.GL_ENABLED) TextureEditor.INSTANCE.m_OpenGLPreviewPanel.notifyTextureNodeRemoved(node);
        if (paramEditorPanel.getActiveTextureNode() == node) paramEditorPanel.setTextureNode(null);
        removePreviewWindowByTextureNode(node);
        repaint();
    }

    public static void drawConnectionLine(Graphics2D g, int x0, int y0, int x1, int y1) {
        final int ofs = 12;
        g.drawLine(x0, y0, x0, y0 - ofs);
        g.drawLine(x0, y0 - ofs, x1, y1 + ofs);
        g.drawLine(x1, y1 + ofs, x1, y1);
    }

    public void drawConnectionPoint(Graphics2D g, int ox, int oy, ConnectionPoint p) {
        if (p.channelIndex == -1) {
            g.setColor(col_NodeInPort);
            g.fillRect(ox + p.x, oy + p.y, p.width, p.height);
        } else {
            g.setColor(col_NodeOutPort);
            g.fillRect(ox + p.x, oy + p.y, p.width, p.height);
        }
    }

    public void drawNode(Graphics2D g, TextureGraphNode node) {
        final int roundRad = 16;
        if (node.userData == null) node.userData = new NodePreviewImage(node);
        if (node.getChannel().vizType == ChannelVizType.SLOW) g.setColor(col_NodeSlowBG); else g.setColor(col_NodeBG);
        int x = node.getX() + desktopX;
        int y = node.getY() + desktopY;
        int w = TextureGraphNode.width;
        int h = TextureGraphNode.height;
        g.fillRoundRect(x, y, w, h, roundRad, roundRad);
        g.drawImage(((NodePreviewImage) node.userData).previewImage, x + 4, y + 12 + 12, this);
        g.setFont(font);
        g.setColor(Color.white);
        g.drawString(node.getChannel().getName(), x + 2, y + 12 + 8);
        g.setColor(col_NodeBorder);
        g.drawRoundRect(x, y, w, h, roundRad, roundRad);
        for (ConnectionPoint p : node.getAllConnectionPointsVector()) {
            drawConnectionPoint(g, x, y, p);
        }
        g.setColor(Color.yellow);
        g.drawString("?", x + helpX + 6, y + helpY + 12);
        if (node.getChannel().isMarkedForExport()) {
            g.drawString("E", x + 4, y + helpY + 10);
        }
    }

    Color gridColor = new Color(0xFF606060);

    BufferedImage canvas;

    float zoom = 1.0f;

    public void paint(Graphics gr) {
        if (canvas == null || (int) (getWidth() * zoom) != canvas.getWidth() || (int) (getHeight() * zoom) != canvas.getHeight()) {
            canvas = new BufferedImage((int) (getWidth() * zoom), (int) (getHeight() * zoom), BufferedImage.TYPE_INT_RGB);
        }
        Graphics2D g = (Graphics2D) canvas.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setBackground(Color.DARK_GRAY);
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        int w = canvas.getWidth();
        int h = canvas.getHeight();
        g.setColor(gridColor);
        for (int y = desktopY % 16; y < h; y += 16) {
            g.drawLine(0, y, w, y);
        }
        for (int x = desktopX % 16; x < w; x += 16) {
            g.drawLine(x, 0, x, h);
        }
        g.setColor(Color.white);
        g.setStroke(connectionLineStroke);
        for (TextureNodeConnection c : graph.allConnections) {
            drawConnectionLine(g, desktopX + c.target.getWorldSpaceX(), desktopY + c.target.getWorldSpaceY(), desktopX + c.source.getWorldSpaceX(), desktopY + c.source.getWorldSpaceY());
        }
        g.setColor(col_NodeSelected);
        for (TextureGraphNode n : graph.selectedNodes) {
            g.fillRoundRect(desktopX + n.getX() - 3, desktopY + n.getY() - 3, TextureGraphNode.width + 6, TextureGraphNode.height + 6, 20, 20);
        }
        g.setColor(Color.blue);
        for (TextureGraphNode n : graph.allNodes) {
            drawNode(g, n);
        }
        if (TextureEditor.GL_ENABLED) {
            TextureEditor.INSTANCE.m_OpenGLPreviewPanel.drawTokens(g, desktopX, desktopY);
        }
        if (connectionDragging) {
            g.setColor(Color.red);
            drawConnectionLine(g, connectionTarget.x, connectionTarget.y, desktopX + connectionOrigin.x, desktopY + connectionOrigin.y);
        }
        for (int i = previewWindows.size() - 1; i >= 0; i--) {
            previewWindows.get(i).draw(g);
        }
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        if (getWidth() != canvas.getWidth() || getHeight() != canvas.getHeight()) {
            gr.drawImage(canvas.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH), 0, 0, null);
        } else {
            gr.drawImage(canvas, 0, 0, null);
        }
    }

    public void moveDesktop(int dx, int dy) {
        desktopX += dx;
        desktopY += dy;
    }

    /**
	 *  computes the bounding box of all positions and centers it computes the bounding box of all positions and centers it
	 */
    public void centerDesktop() {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (TextureGraphNode n : graph.allNodes) {
            minX = Math.min(minX, n.getX());
            minY = Math.min(minY, n.getY());
            maxX = Math.max(maxX, n.getX() + TextureGraphNode.width);
            maxY = Math.max(maxY, n.getY() + TextureGraphNode.height);
        }
        int dx = getWidth() / 2 - (minX + maxX) / 2;
        int dy = getHeight() / 2 - (minY + maxY) / 2;
        desktopX = dx;
        desktopY = dy;
        repaint();
    }

    void showSelectedChannelPopupMenu(TextureGraphNode node, int x, int y) {
        if (node.getChannel() instanceof Pattern) addToPresetsChannelMenuItem.setEnabled(true); else addToPresetsChannelMenuItem.setEnabled(false);
        replacepasteChannelMenuItem.setEnabled(toCopyTextureGraphNode != null);
        selectedChannelPopupMenu.show(this, x, y);
    }

    void showNewChannelPopupMenu(Component c, int x, int y) {
        newChannelInsertMenuItem.setEnabled(toCopyTextureGraphNode != null);
        newChannelPopupMenu.show(c, x, y);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (desktopDragging) {
            int dx = (int) ((e.getXOnScreen() * zoom) - dragStartX);
            int dy = (int) ((e.getYOnScreen() * zoom) - dragStartY);
            moveDesktop(dx, dy);
            repaint();
        } else if (nodeDragging) {
            for (TextureGraphNode node : graph.selectedNodes) {
                node.movePosition((int) (e.getXOnScreen() * zoom) - dragStartX, (int) (e.getYOnScreen() * zoom) - dragStartY);
            }
            repaint();
        } else if (connectionDragging) {
            connectionTarget.x = (int) (e.getX() * zoom);
            connectionTarget.y = (int) (e.getY() * zoom);
            repaint();
        } else if (draggedWindow != null) {
            int dx = (int) ((e.getXOnScreen()) - dragStartX / zoom);
            int dy = (int) ((e.getYOnScreen()) - dragStartY / zoom);
            draggedWindow.move(dx, dy);
            repaint();
        }
        dragStartX = (int) (e.getXOnScreen() * zoom);
        dragStartY = (int) (e.getYOnScreen() * zoom);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
    }

    public int getActionTypeForMouseClick(int x, int y, TextureGraphNode n) {
        if (n.getOutputConnectionPoint().inside(x, y)) {
            return 2;
        }
        for (ConnectionPoint cp : n.getAllConnectionPointsVector()) {
            if (cp.inside(x, y)) return -cp.channelIndex - 1;
        }
        if ((x >= helpX + n.getX()) && (x <= (helpX + n.getX() + helpW)) && (y >= helpY + n.getY()) && (y <= (helpY + n.getY() + helpH))) {
            JOptionPane.showMessageDialog(this, n.getChannel().getHelpText(), n.getChannel().getName() + " Help", JOptionPane.PLAIN_MESSAGE);
            return 3;
        }
        return 1;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mousePosition.x = (int) (e.getX() * zoom);
        mousePosition.y = (int) (e.getY() * zoom);
        dragStartX = (int) (e.getXOnScreen() * zoom);
        dragStartY = (int) (e.getYOnScreen() * zoom);
        int wsX = (int) ((e.getX()) * zoom) - desktopX;
        int wsY = (int) ((e.getY()) * zoom) - desktopY;
        if (e.getButton() == 2 || (e.isControlDown())) {
            desktopDragging = true;
        } else if ((draggedWindow = getPreviewWindowAtPosition(mousePosition.x, mousePosition.y)) != null) {
            previewWindows.setElementAt(previewWindows.firstElement(), previewWindows.indexOf(draggedWindow));
            previewWindows.setElementAt(draggedWindow, 0);
            if (draggedWindow.doClick(mousePosition.x, mousePosition.y)) {
                draggedWindow = null;
            }
        } else {
            TextureGraphNode node = graph.getNodeAtPosition(wsX, wsY);
            if (node != null) {
                if (e.getButton() == 3) {
                    if (!isNodeInSelection(node)) setSelectedNode(node);
                    showSelectedChannelPopupMenu(node, e.getX(), e.getY());
                } else {
                    int actionType = getActionTypeForMouseClick(wsX, wsY, node);
                    if (e.isShiftDown()) {
                        addSelectedNode(node);
                    } else if (actionType == 1) {
                        if (!isNodeInSelection(node)) setSelectedNode(node);
                        nodeDragging = true;
                    } else if (actionType == 2) {
                        connectionDragging = true;
                        connectionSource = node.getOutputConnectionPoint();
                        connectionOrigin = new Point(connectionSource.getWorldSpaceX(), connectionSource.getWorldSpaceY());
                        connectionTarget = e.getPoint();
                    } else if (actionType < 0) {
                        int index = -actionType - 1;
                        TextureGraphNode.ConnectionPoint inputPoint = node.getInputConnectionPointByChannelIndex(index);
                        TextureNodeConnection connection = graph.getConnectionAtInputPoint(inputPoint);
                        if (connection != null) {
                            graph.removeConnection(connection);
                            connectionDragging = true;
                            connectionSource = connection.source;
                            connectionOrigin = new Point(connectionSource.getWorldSpaceX(), connectionSource.getWorldSpaceY());
                            connectionTarget = e.getPoint();
                        }
                    }
                }
            } else if (e.getButton() == 3) {
                showNewChannelPopupMenu(e.getComponent(), e.getX(), e.getY());
            } else {
                setSelectedNode(null);
            }
        }
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mousePosition.x = (int) (e.getX() * zoom);
        mousePosition.y = (int) (e.getY() * zoom);
        int wsX = (int) ((mousePosition.x) * zoom) - desktopX;
        int wsY = (int) ((mousePosition.y) * zoom) - desktopY;
        if (nodeDragging || connectionDragging) notifyEditChangeListener();
        if (connectionDragging) {
            TextureGraphNode targetPat = graph.getNodeAtPosition(wsX, wsY);
            if (targetPat != null) {
                int actionType = getActionTypeForMouseClick(wsX, wsY, targetPat);
                if (actionType < 0) {
                    int index = -actionType - 1;
                    if (e.isControlDown()) {
                        for (int i = 0; i < targetPat.getChannel().getNumInputChannels(); i++) {
                            TextureGraphNode.ConnectionPoint ip = targetPat.getInputConnectionPointByChannelIndex(i);
                            graph.addConnection(new TextureNodeConnection(connectionSource, ip));
                        }
                    } else {
                        TextureGraphNode.ConnectionPoint inputPoint = targetPat.getInputConnectionPointByChannelIndex(index);
                        graph.addConnection(new TextureNodeConnection(connectionSource, inputPoint));
                    }
                }
            }
        }
        nodeDragging = false;
        connectionDragging = false;
        desktopDragging = false;
        draggedWindow = null;
        repaint();
    }

    public void addEditChangeListener(EditChangeListener listener) {
        editChangeListener.add(listener);
    }

    public boolean removeEditChangeListener(EditChangeListener listener) {
        return editChangeListener.remove(listener);
    }

    protected void notifyEditChangeListener() {
        for (EditChangeListener l : editChangeListener) l.graphWasEdited();
    }

    @Override
    public void channelChanged(Channel source) {
        notifyEditChangeListener();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
    }

    Vector<PreviewWindow> previewWindows = new Vector<PreviewWindow>();

    /** Checks if a PreviewWindow exists for the given TextureGraphNode and
	 * removes it.
	 * @param node
	 */
    void removePreviewWindowByTextureNode(TextureGraphNode node) {
        for (int i = 0; i < previewWindows.size(); i++) {
            PreviewWindow w = previewWindows.get(i);
            if (w.previewNode == node) {
                removePreviewWindow(w);
                i--;
            }
        }
    }

    void removeAllPreviewWindows() {
        for (int i = previewWindows.size() - 1; i >= 0; i--) {
            removePreviewWindow(previewWindows.get(i));
        }
    }

    void removePreviewWindow(PreviewWindow w) {
        w.previewNode.getChannel().removeChannelChangeListener(w);
        previewWindows.remove(w);
    }

    void addPreviewWindow(TextureGraphNode node) {
        PreviewWindow w = new PreviewWindow(node);
        previewWindows.add(w);
    }

    PreviewWindow getPreviewWindowAtPosition(int x, int y) {
        for (PreviewWindow w : previewWindows) {
            if (w.isInside(x, y)) return w;
        }
        return null;
    }

    class PreviewWindow implements ChannelChangeListener {

        int posX;

        int posY;

        final BufferedImage previewNodeImage;

        BufferedImage tempImage;

        final TextureGraphNode previewNode;

        float zoom = 1.0f;

        class MiniButton {

            int px, py;

            int w, h;

            String t;

            public MiniButton(int posX, int posY, int width, int height, String type) {
                px = posX;
                py = posY;
                w = width;
                h = height;
                t = type;
            }

            public void draw(Graphics2D g, int ox, int oy) {
                g.drawString(t, px + ox + 4, py + oy + h - 4);
            }

            boolean inside(int x, int y) {
                return (x >= px && y >= py && x <= (px + w) && y <= (py + h));
            }
        }

        Vector<MiniButton> miniButtons = new Vector<MiniButton>();

        public PreviewWindow(TextureGraphNode node) {
            previewNode = node;
            node.getChannel().addChannelChangeListener(this);
            previewNodeImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
            updatePreviewImage();
            miniButtons.add(new MiniButton(256 - 16, 0, 16, 16, "X"));
            miniButtons.add(new MiniButton(0, 0, 16, 16, "+"));
            miniButtons.add(new MiniButton(16, 0, 16, 16, "-"));
        }

        public void move(int dx, int dy) {
            posX += dx;
            posY += dy;
            if (posX < -128) posX = -128;
            if (posY < -128) posY = -128;
            if (posX > getWidth() - 128) posX = getWidth() - 128;
            if (posY > getHeight() - 128) posY = getHeight() - 128;
        }

        public void draw(Graphics2D g) {
            g.drawImage(previewNodeImage, posX, posY + 16, null);
            g.setColor(Color.black);
            g.fillRect(posX, posY, previewNodeImage.getWidth(), 16);
            g.setColor(Color.white);
            g.drawRect(posX + -1, posY + -1, 258, 256 + 16 + 2);
            for (MiniButton b : miniButtons) {
                b.draw(g, posX, posY);
            }
            g.drawString(String.format("Zoom: %.3f", zoom), posX + 48, posY + 12);
        }

        /** call this with world space position to make a click inside the window */
        public boolean doClick(int x, int y) {
            for (MiniButton b : miniButtons) {
                if (b.inside(x - posX, y - posY)) {
                    if ("X".equals(b.t)) {
                        removePreviewWindow(this);
                    } else if ("+".equals(b.t)) {
                        if (zoom < 256.0f) {
                            zoom *= 2.0f;
                            updatePreviewImage();
                        }
                    } else if ("-".equals(b.t)) {
                        if (zoom > 0.125) {
                            zoom /= 2.0f;
                            updatePreviewImage();
                        }
                    }
                    return true;
                }
            }
            return false;
        }

        public boolean isInside(int x, int y) {
            int width = previewNodeImage.getWidth();
            int height = previewNodeImage.getHeight() + 16;
            return (x >= posX) && (x <= posX + width) && (y >= posY) && (y <= posY + height);
        }

        void updatePreviewImage() {
            if (previewNode != null) {
                if (previewNode.getChannel().chechkInputChannels()) {
                    if (zoom == 1.0f) {
                        ChannelUtils.computeImage(previewNode.getChannel(), previewNodeImage, null, 0);
                    } else if (zoom > 1.0f) {
                        ChannelUtils.computeImage(previewNode.getChannel(), previewNodeImage, null, 0, (int) (previewNodeImage.getWidth() * zoom), (int) (previewNodeImage.getHeight() * zoom), 0, 0);
                    } else if (zoom < 1.0f) {
                        int lx = (int) (previewNodeImage.getWidth() * zoom);
                        int ly = (int) (previewNodeImage.getHeight() * zoom);
                        if (tempImage == null || tempImage.getWidth() != lx || tempImage.getHeight() != ly) {
                            tempImage = new BufferedImage(lx, ly, BufferedImage.TYPE_INT_RGB);
                        }
                        ChannelUtils.computeImage(previewNode.getChannel(), tempImage, null, 0);
                        Graphics g = previewNodeImage.getGraphics();
                        for (int py = 0; py < previewNodeImage.getHeight(); py += ly) {
                            for (int px = 0; px < previewNodeImage.getWidth(); px += lx) {
                                g.drawImage(tempImage, px, py, null);
                            }
                        }
                    }
                } else {
                    Graphics g = previewNodeImage.getGraphics();
                    g.fillRect(0, 0, previewNodeImage.getWidth(), previewNodeImage.getHeight());
                }
            }
        }

        @Override
        public void channelChanged(Channel source) {
            updatePreviewImage();
        }
    }
}
