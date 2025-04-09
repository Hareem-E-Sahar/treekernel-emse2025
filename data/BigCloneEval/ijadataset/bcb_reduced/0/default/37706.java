import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.io.*;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.tree.*;
import org.jgraph.JGraph;
import org.jgraph.event.*;
import org.jgraph.graph.*;

/**
 * The Main Window of the VEST Executable.
 */
public class MainWin extends JApplet implements GraphSelectionListener, KeyListener, DropTargetListener {

    /**
	 * Returns an ImageIcon, or null if the path was invalid.
	 * 
	 * @param path the path
	 * @param description the description
	 * 
	 * @return the image icon
	 */
    protected static ImageIcon createImageIcon(String path, String description) {
        java.net.URL imgURL = MainWin.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /** The graph. */
    protected JGraph graph;

    /** The drag bar. */
    protected JFrame dragBar;

    /** The drag panel. */
    protected JPanel dragPanel;

    /** The side pane. */
    protected JScrollPane sidePane;

    /** The editor version. */
    protected String editorVersion = "Version 0.91b";

    /** The all states. */
    public Hashtable<String, Object> allStates = new Hashtable<String, Object>();

    /** The all transitions. */
    public Hashtable<String, Object> allTransitions = new Hashtable<String, Object>();

    /** The undo manager. */
    protected GraphUndoManager undoManager;

    /** The collapse. */
    protected Action undo, redo, remove, group, ungroup, tofront, toback, cut, copy, paste, defaultSelection, collapse;

    /** The compound label. */
    protected JLabel basicLabel, compoundLabel;

    /** The Drop position. */
    private DropTarget dropTarget = null;

    /** The output. */
    private ObjectOutputStream output;

    /** The input. */
    private ObjectInputStream input;

    /** The cell count. */
    protected int cellCount = 0;

    /** The edge count. */
    protected int edgeCount = 0;

    /** The port count. */
    protected int portCount = 0;

    /** The status bar. */
    protected JPanel statusBar = null;

    /** The help. */
    protected JMenu file, edit, insert, zoom, focus, other, help;

    /** The file new. */
    protected JMenuItem fileSaveAs, fileOpen, fileNew;

    /** The insert history. */
    protected JMenuItem insertDefault, insertCompound, insertAnd, insertExecution, insertHistory;

    /** The edit delete. */
    protected JMenuItem editCopy, editCut, editPaste, editUndo, editRedo, editDelete;

    /** The zoom default. */
    protected JMenuItem zoomIn, zoomOut, zoomDefault;

    /** The focus back. */
    protected JMenuItem focusFront, focusBack;

    /** The other settings. */
    protected JMenuItem otherDefault, otherGroup, otherUngroup, otherCompile, otherCheck, otherLine, otherSettings;

    /** The help about. */
    protected JMenuItem helpContents, helpQuick, helpAbout;

    /** The cond count. */
    protected int condCount;

    /** The group count. */
    protected int groupCount;

    /**
	 * Instantiates a new main win.
	 * 
	 * @param g the g
	 * @param c the c
	 * @param projectName the project name
	 */
    public MainWin(int g, int c, String projectName) {
        getContentPane().setLayout(new BorderLayout());
        graph = createGraph();
        graph.setMarqueeHandler(new VESTMarqueeHandler(graph, this));
        graph.setAntiAliased(true);
        condCount = c;
        groupCount = g;
        System.out.println(groupCount);
        undoManager = new GraphUndoManager() {

            /**
			 * 
			 */
            private static final long serialVersionUID = 1L;

            public void undoableEditHappened(UndoableEditEvent e) {
                super.undoableEditHappened(e);
                updateHistoryButtons();
            }
        };
        graph.getGraphLayoutCache().setFactory(new DefaultCellViewFactory() {

            public CellView createView(GraphModel model, Object c) {
                CellView view = null;
                if (c instanceof AndStateCell) {
                    return new JGraphAndStateView(c);
                } else if (c instanceof SwimLaneCell) {
                    return new JGraphSwimlaneView(c);
                } else if (c instanceof basicCell) {
                    return new basicCellView(c);
                } else if (c instanceof orthogonalCell) {
                    return new orthogonalView(c);
                } else if (c instanceof circle) {
                    return new circleView(c);
                } else {
                    view = super.createView(model, c);
                }
                return view;
            }
        });
        graph.getModel().addUndoableEditListener(undoManager);
        graph.getSelectionModel().addGraphSelectionListener(this);
        graph.addKeyListener(this);
        getContentPane().add(createToolBar(), BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(graph), BorderLayout.CENTER);
        getContentPane().add(createStatusBar(), BorderLayout.SOUTH);
        JMenuBar menuBar = new JMenuBar();
        file = new JMenu("File");
        menuBar.add(file);
        file.setMnemonic(KeyEvent.VK_F);
        fileNew = new JMenuItem("New", new ImageIcon("resources/new.png"));
        file.add(fileNew);
        fileNew.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
            }
        });
        fileNew.setMnemonic(KeyEvent.VK_N);
        fileOpen = new JMenuItem("Open", new ImageIcon("resources/open.png"));
        file.add(fileOpen);
        fileOpen.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                open();
            }
        });
        fileOpen.setMnemonic(KeyEvent.VK_P);
        fileSaveAs = new JMenuItem("Save As..", new ImageIcon("resources/save.png"));
        file.add(fileSaveAs);
        fileSaveAs.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == fileSaveAs) {
                    saveAs();
                }
            }
        });
        fileSaveAs.setMnemonic(KeyEvent.VK_S);
        edit = new JMenu("Edit");
        menuBar.add(edit);
        edit.setMnemonic(KeyEvent.VK_E);
        editCopy = new JMenuItem("Copy", new ImageIcon("resources/page_copy.png"));
        edit.add(editCopy);
        editCopy.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Action action;
                action = javax.swing.TransferHandler.getCopyAction();
                e = new ActionEvent(graph, e.getID(), e.getActionCommand(), e.getModifiers());
                action.actionPerformed(e);
            }
        });
        editCopy.setMnemonic(KeyEvent.VK_C);
        editPaste = new JMenuItem("Paste", new ImageIcon("resources/paste_plain.png"));
        edit.add(editPaste);
        edit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Action action;
                action = javax.swing.TransferHandler.getPasteAction();
                e = new ActionEvent(graph, e.getID(), e.getActionCommand(), e.getModifiers());
                action.actionPerformed(e);
            }
        });
        editPaste.setMnemonic(KeyEvent.VK_P);
        editCut = new JMenuItem("Cut", new ImageIcon("resources/cut_red.png"));
        edit.add(editCut);
        editCut.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Action action;
                action = javax.swing.TransferHandler.getCutAction();
                e = new ActionEvent(graph, e.getID(), e.getActionCommand(), e.getModifiers());
                action.actionPerformed(e);
            }
        });
        editCut.setMnemonic(KeyEvent.VK_U);
        edit.addSeparator();
        editUndo = new JMenuItem("Undo Action", new ImageIcon("resources/arrow_undo.png"));
        editUndo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                undo();
            }
        });
        editUndo.setMnemonic(KeyEvent.VK_D);
        edit.add(editUndo);
        editRedo = new JMenuItem("Redo Action", new ImageIcon("resources/arrow_redo.png"));
        editRedo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                redo();
            }
        });
        edit.add(editRedo);
        editRedo.setMnemonic(KeyEvent.VK_R);
        edit.addSeparator();
        editDelete = new JMenuItem("Delete Seletection", new ImageIcon("resources/delete.png"));
        edit.add(editDelete);
        editDelete.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!graph.isSelectionEmpty()) {
                    Object[] cells = graph.getSelectionCells();
                    cells = graph.getDescendants(cells);
                    for (int i = 0; i < cells.length; i++) {
                        if (cells[i] instanceof SwimLaneCell) {
                            allStates.remove(cells[i]);
                        } else if (cells[i] instanceof AndStateCell) {
                            allStates.remove(cells[i]);
                        } else if (cells[i] instanceof basicCell) {
                            allStates.remove(cells[i].toString());
                        } else if (cells[i] instanceof DefaultEdge) {
                        } else {
                        }
                    }
                    graph.getModel().remove(cells);
                }
            }
        });
        editDelete.setMnemonic(KeyEvent.VK_L);
        insert = new JMenu("Insert");
        menuBar.add(insert);
        insert.setMnemonic(KeyEvent.VK_I);
        insertDefault = new JMenuItem("Basic Cell", new ImageIcon("resources/shape_square.png"));
        insert.add(insertDefault);
        insertDefault.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                insertBasic(new Point(10, 10));
            }
        });
        insertDefault.setMnemonic(KeyEvent.VK_B);
        insertCompound = new JMenuItem("Compound Cell", new ImageIcon("resources/shape_square_add.png"));
        insert.add(insertCompound);
        insertCompound.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                insertCompound(new Point(10, 10));
            }
        });
        insertCompound.setMnemonic(KeyEvent.VK_C);
        insertAnd = new JMenuItem("Orthogonal Cell", new ImageIcon("resources/shape_square_go.png"));
        insert.add(insertAnd);
        insertAnd.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                insertAND(new Point(10, 10));
            }
        });
        insertAnd.setMnemonic(KeyEvent.VK_O);
        insertExecution = new JMenuItem("Execution Cell", new ImageIcon("resources/table_multiple.png"));
        insert.add(insertExecution);
        insertExecution.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                insertOrtho(new Point(10, 10));
            }
        });
        insertExecution.setMnemonic(KeyEvent.VK_E);
        insertHistory = new JMenuItem("History", new ImageIcon("resources/time.png"));
        insert.add(insertHistory);
        insertHistory.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                insertHist(new Point(10, 10));
            }
        });
        insertHistory.setMnemonic(KeyEvent.VK_H);
        focus = new JMenu("Focus");
        menuBar.add(focus);
        focus.setMnemonic(KeyEvent.VK_O);
        focusFront = new JMenuItem("Bring to Front", new ImageIcon("resources/tofront.png"));
        focus.add(focusFront);
        focusFront.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                toFront(graph.getSelectionCells());
            }
        });
        focusFront.setMnemonic(KeyEvent.VK_F);
        focusBack = new JMenuItem("Send to Back", new ImageIcon("resources/toback.png"));
        focus.add(focusBack);
        focusBack.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                toBack(graph.getSelectionCells());
            }
        });
        focusBack.setMnemonic(KeyEvent.VK_B);
        zoom = new JMenu("Zoom");
        menuBar.add(zoom);
        zoom.setMnemonic(KeyEvent.VK_Z);
        zoomIn = new JMenuItem("Zoom In", new ImageIcon("resources/zoom_in.png"));
        zoom.add(zoomIn);
        zoomIn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                graph.setScale(2 * graph.getScale());
            }
        });
        zoomIn.setMnemonic(KeyEvent.VK_Z);
        zoomOut = new JMenuItem("Zoom Out", new ImageIcon("resources/zoom_out.png"));
        zoom.add(zoomOut);
        zoomOut.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                graph.setScale(graph.getScale() / 2);
            }
        });
        zoomOut.setMnemonic(KeyEvent.VK_O);
        zoom.addSeparator();
        zoomDefault = new JMenuItem("Zoom 1:1", new ImageIcon("resources/zoom.png"));
        zoom.add(zoomDefault);
        zoomDefault.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                graph.setScale(1.0);
            }
        });
        zoomDefault.setMnemonic(KeyEvent.VK_1);
        other = new JMenu("Other Options");
        menuBar.add(other);
        other.setMnemonic(KeyEvent.VK_T);
        otherDefault = new JMenuItem("Default Selected Cell", new ImageIcon("resources/default.gif"));
        other.add(otherDefault);
        otherDefault.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Object defaultCell;
                if (!graph.isSelectionEmpty() && (graph.getSelectionCells().length == 1)) {
                    defaultCell = graph.getSelectionCell();
                    if (defaultCell instanceof AndStateCell) {
                        ((AndStateCell) defaultCell).setDefault();
                        AttributeMap cellattrib = graph.getModel().getAttributes((AndStateCell) defaultCell);
                        if (((AndStateCell) defaultCell).isDefault()) {
                            GraphConstants.setGradientColor(cellattrib, Color.BLACK);
                        } else {
                            GraphConstants.setGradientColor(cellattrib, Color.GREEN);
                        }
                        ((AndStateCell) defaultCell).setAttributes(cellattrib);
                        CellView view = graph.getGraphLayoutCache().getMapping(((AndStateCell) defaultCell), false);
                        graph.getGraphLayoutCache().reload();
                        graph.repaint();
                    }
                    if (defaultCell instanceof SwimLaneCell) {
                        ((SwimLaneCell) defaultCell).setDefault();
                        AttributeMap cellattrib = graph.getModel().getAttributes((SwimLaneCell) defaultCell);
                        if (((SwimLaneCell) defaultCell).isDefault()) {
                            GraphConstants.setGradientColor(cellattrib, Color.BLACK);
                        } else {
                            GraphConstants.setGradientColor(cellattrib, Color.RED);
                        }
                        ((SwimLaneCell) defaultCell).setAttributes(cellattrib);
                        CellView view = graph.getGraphLayoutCache().getMapping(((SwimLaneCell) defaultCell), false);
                        graph.getGraphLayoutCache().reload();
                        graph.repaint();
                    }
                    if (defaultCell instanceof basicCell) {
                        ((basicCell) defaultCell).setDefault();
                        AttributeMap cellattrib = graph.getModel().getAttributes((basicCell) defaultCell);
                        if (((basicCell) defaultCell).isDefault()) {
                            GraphConstants.setGradientColor(cellattrib, Color.BLACK);
                        } else {
                            GraphConstants.setGradientColor(cellattrib, Color.BLUE);
                        }
                        ((basicCell) defaultCell).setAttributes(cellattrib);
                        CellView view = graph.getGraphLayoutCache().getMapping(((basicCell) defaultCell), false);
                        graph.getGraphLayoutCache().reload();
                        graph.repaint();
                    }
                }
            }
        });
        otherDefault.setMnemonic(KeyEvent.VK_D);
        otherGroup = new JMenuItem("Group Selected Cells", new ImageIcon("resources/shape_group.png"));
        other.add(otherGroup);
        otherGroup.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                group(graph.getSelectionCells());
            }
        });
        otherGroup.setMnemonic(KeyEvent.VK_G);
        otherUngroup = new JMenuItem("Ungroup Selected Cells", new ImageIcon("resources/shape_ungroup.png"));
        other.add(otherUngroup);
        otherUngroup.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ungroup(graph.getSelectionCells());
            }
        });
        otherUngroup.setMnemonic(KeyEvent.VK_U);
        other.addSeparator();
        otherCompile = new JMenuItem("Compile Specification", new ImageIcon("resources/cog.png"));
        other.add(otherCompile);
        otherCompile.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                printContained();
            }
        });
        otherCompile.setMnemonic(KeyEvent.VK_C);
        otherCheck = new JMenuItem("Check Specification", new ImageIcon("resources/bug.png"));
        other.add(otherCheck);
        otherCheck.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                checkChart();
            }
        });
        otherCheck.setMnemonic(KeyEvent.VK_H);
        other.addSeparator();
        otherLine = new JMenuItem("Disable/Enable Transitions", new ImageIcon("resources/chart_line.png"));
        other.add(otherLine);
        otherLine.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                graph.setPortsVisible(!graph.isPortsVisible());
                URL connectUrl;
                if (graph.isPortsVisible()) connectUrl = getClass().getClassLoader().getResource("resources/chart_line.png"); else connectUrl = getClass().getClassLoader().getResource("resources/chart_line_delete.png");
            }
        });
        otherLine.setMnemonic(KeyEvent.VK_T);
        other.addSeparator();
        otherSettings = new JMenuItem("Settings...", new ImageIcon("resources/tick.png"));
        other.add(otherSettings);
        otherSettings.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Put the Setting for the Other Charts here.", "Settings", JOptionPane.ERROR_MESSAGE);
            }
        });
        otherSettings.setMnemonic(KeyEvent.VK_S);
        help = new JMenu("Help");
        menuBar.add(help);
        help.setMnemonic(KeyEvent.VK_H);
        helpContents = new JMenuItem("Help Contents");
        help.add(helpContents);
        helpContents.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Sorry, help doesn't exist in this prototype", "Help?", JOptionPane.ERROR_MESSAGE);
            }
        });
        helpQuick = new JMenuItem("Quick Start Guide");
        help.add(helpQuick);
        helpQuick.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Sorry, help doesn't exist in this prototype", "Help?", JOptionPane.ERROR_MESSAGE);
            }
        });
        help.addSeparator();
        helpAbout = new JMenuItem("About");
        help.add(helpAbout);
        helpAbout.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                aboutFrame();
            }
        });
        this.setJMenuBar(menuBar);
        JGraph miniGraph = new JGraph();
        dropTarget = new DropTarget(graph, this);
        Object[] Custom = new Object[] { "Custom Charts", "C:\\BigTest.ESP" };
        Object[] hierarchy = { "Components", "Basic Cell", "Compound Cell", "Orthogonal Cell", "Execution Cell", "History", Custom };
        DefaultMutableTreeNode root = processHierarchy(hierarchy);
        DefaultTreeModel model = new DefaultTreeModel(root);
        DNDTree tree = new DNDTree(model);
        tree.setCellRenderer(new DNDrenderer());
        JPanel sideBar = new JPanel();
        sidePane = new JScrollPane(tree);
        sideBar.add(sidePane);
        getContentPane().add(sideBar, BorderLayout.EAST);
    }

    /**
	 * About frame.
	 */
    public void aboutFrame() {
        JFrame about = new JFrame("About Evaluation Flow Editor");
        about.setResizable(false);
        about.getContentPane().setLayout(new BorderLayout());
        about.setSize(252, 300);
        ImageIcon icon = createImageIcon("resources/logo.gif", "resources/logo.bmp");
        JLabel logo = new JLabel(icon);
        about.getContentPane().add(logo, BorderLayout.NORTH);
        String aboutText = "Evaluation Flow Editor " + getVersion() + "\n\nThis version is nonsense!";
        JTextArea text = new JTextArea(aboutText);
        text.setLineWrap(true);
        text.setEditable(false);
        about.getContentPane().add(new JScrollPane(text), BorderLayout.CENTER);
        about.setVisible(true);
        about.setLocation(320, 240);
    }

    /**
	 * Adds the sub chart.
	 * 
	 * @param chart the chart
	 * @param cell the cell
	 */
    void addSubChart(String chart, Point cell) {
        try {
            Object insertCell = graph.getFirstCellForLocation(cell.getX(), cell.getY());
            if (insertCell != null) {
                JGraph subchart;
                subchart = createGraph();
                subchart.getGraphLayoutCache().setFactory(new DefaultCellViewFactory() {

                    public CellView createView(GraphModel model, Object c) {
                        CellView view = null;
                        if (c instanceof AndStateCell) {
                            return new JGraphAndStateView(c);
                        } else if (c instanceof SwimLaneCell) {
                            return new JGraphSwimlaneView(c);
                        } else if (c instanceof basicCell) {
                            return new basicCellView(c);
                        } else if (c instanceof orthogonalCell) {
                            return new orthogonalView(c);
                        } else if (c instanceof circle) {
                            return new circleView(c);
                        } else {
                            view = super.createView(model, c);
                        }
                        return view;
                    }
                });
                Rectangle2D rect = graph.getCellBounds(insertCell);
                int x = (int) rect.getMaxX() - (int) rect.getMinX();
                int y = (int) rect.getMaxY() - (int) rect.getMinY();
                input = new ObjectInputStream(new FileInputStream(chart));
                int length = input.readInt();
                Object[] cells = new Object[length];
                Hashtable<DefaultGraphCell, AttributeMap> attrib = new Hashtable<DefaultGraphCell, AttributeMap>();
                for (int i = 0; i < length; i++) {
                    DefaultGraphCell tmp = (DefaultGraphCell) input.readObject();
                    if (tmp instanceof DefaultEdge) {
                    } else if (tmp instanceof basicCell) {
                        tmp.getAttributes().scale(.25, .25, cell);
                        attrib.put(tmp, tmp.getAttributes());
                        cells[i] = tmp;
                    } else if (tmp instanceof SwimLaneCell) {
                        tmp.getAttributes().scale(.25, .25, cell);
                        attrib.put(tmp, tmp.getAttributes());
                        cells[i] = tmp;
                    } else if (tmp instanceof AndStateCell) {
                        tmp.getAttributes().scale(.25, .25, cell);
                        attrib.put(tmp, tmp.getAttributes());
                        cells[i] = tmp;
                    } else if (tmp instanceof orthogonalCell) {
                        tmp.getAttributes().scale(.25, .25, cell);
                        attrib.put(tmp, tmp.getAttributes());
                        cells[i] = tmp;
                    } else if (tmp instanceof circle) {
                        tmp.getAttributes().scale(.25, .25, cell);
                        attrib.put(tmp, tmp.getAttributes());
                        cells[i] = tmp;
                    }
                }
                graph.getGraphLayoutCache().insert(cells, attrib, null, null, null);
                int counter = input.readInt();
                for (int j = 0; j < counter; j++) {
                    String name, source, target;
                    name = (String) input.readObject();
                    source = (String) input.readObject();
                    target = (String) input.readObject();
                    remakeConnection(name, source, target, graph);
                }
                input.close();
                System.out.println("Collapsing");
                graph.getGraphLayoutCache().collapse(cells);
                System.out.println("Done");
                graph.addSelectionCells(subchart.getRoots());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error Opening File", "Error", JOptionPane.ERROR_MESSAGE);
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "File is not an Evaluation!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
	 * Check chart.
	 */
    public void checkChart() {
        Object[] cells = graph.getDescendants(graph.getRoots());
        for (int i = 0; i < cells.length; i++) {
            if (isGroup(cells[i])) {
                System.out.println("I found a group Cell, do something please");
            }
        }
        allStates = new Hashtable<String, Object>();
        allTransitions = new Hashtable<String, Object>();
        for (int i = 0; i < cells.length; i++) {
            if (cells[i] instanceof DefaultEdge) {
                allTransitions.put(cells[i].toString(), cells[i]);
            } else if (cells[i] instanceof basicCell) {
                allStates.put(cells[i].toString(), cells[i]);
            } else if (cells[i] instanceof SwimLaneCell) {
                allStates.put(cells[i].toString(), cells[i]);
            } else if (cells[i] instanceof AndStateCell) {
                allStates.put(cells[i].toString(), cells[i]);
            } else if (cells[i] instanceof orthogonalCell) {
                allStates.put(cells[i].toString(), cells[i]);
            } else if (cells[i] instanceof circle) {
                allStates.put(cells[i].toString(), cells[i]);
            }
        }
        stateChart output = new stateChart(allStates, allTransitions, graph, condCount, groupCount);
        if (output.getValid()) {
            System.out.println("HOOHA");
        }
    }

    /**
	 * Clear graph.
	 */
    void clearGraph() {
    }

    /**
	 * Close file.
	 */
    public void closeFile() {
        try {
            output.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error closing file", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
	 * Collapse.
	 * 
	 * @param cells the cells
	 */
    public void collapse(Object[] cells) {
        System.out.println("Got the Call?");
        graph.getGraphLayoutCache().setVisible(cells, false);
        graph.getGraphLayoutCache().hideCells(cells, true);
    }

    /**
	 * Connect.
	 * 
	 * @param source the source
	 * @param target the target
	 */
    public void connect(Port source, Port target) {
        DefaultEdge edge = createDefaultEdge();
        allTransitions.put(edge.toString(), edge);
        if (graph.getModel().acceptsSource(edge, source) && graph.getModel().acceptsTarget(edge, target)) {
            edge.getAttributes().applyMap(createEdgeAttributes());
            graph.getGraphLayoutCache().insertEdge(edge, source, target);
        }
    }

    /**
	 * Creates the and state cell.
	 * 
	 * @return the and state cell
	 */
    protected AndStateCell createAndStateCell() {
        AndStateCell cell = new AndStateCell("And State " + new Integer(cellCount++));
        cell.add(new DefaultPort("Port " + new Integer(portCount++)));
        allStates.put(cell.toString(), cell);
        return cell;
    }

    /**
	 * Creates the basic cell.
	 * 
	 * @return the basic cell
	 */
    protected basicCell createBasicCell() {
        basicCell cell = new basicCell("Cell " + new Integer(cellCount++));
        cell.add(new DefaultPort("Port " + new Integer(portCount++)));
        allStates.put(cell.toString(), cell);
        return cell;
    }

    /**
	 * Creates the cell attributes.
	 * 
	 * @param point the point
	 * 
	 * @return the map<?,?>
	 */
    public Map<?, ?> createCellAttributes(Point2D point) {
        Map<?, ?> map = new Hashtable<Object, Object>();
        point = graph.snap((Point2D) point.clone());
        GraphConstants.setBounds(map, new Rectangle2D.Double(point.getX(), point.getY(), 0, 0));
        GraphConstants.setResize(map, true);
        GraphConstants.setGradientColor(map, Color.blue);
        GraphConstants.setBorderColor(map, Color.black);
        GraphConstants.setBackground(map, Color.white);
        GraphConstants.setOpaque(map, true);
        return map;
    }

    /**
	 * Creates the cell attributes and.
	 * 
	 * @param point the point
	 * 
	 * @return the map<?,?>
	 */
    public Map<?, ?> createCellAttributesAND(Point2D point) {
        Map<?, ?> map = new Hashtable<Object, Object>();
        point = graph.snap((Point2D) point.clone());
        GraphConstants.setBounds(map, new Rectangle2D.Double(point.getX(), point.getY(), 0, 0));
        GraphConstants.setResize(map, true);
        GraphConstants.setGradientColor(map, Color.green);
        GraphConstants.setBorderColor(map, Color.black);
        GraphConstants.setBackground(map, Color.white);
        GraphConstants.setOpaque(map, true);
        return map;
    }

    /**
	 * Creates the cell attributes com.
	 * 
	 * @param point the point
	 * 
	 * @return the map<?,?>
	 */
    public Map<?, ?> createCellAttributesCom(Point2D point) {
        Map<?, ?> map = new Hashtable<Object, Object>();
        point = graph.snap((Point2D) point.clone());
        GraphConstants.setBounds(map, new Rectangle2D.Double(point.getX(), point.getY(), 0, 0));
        GraphConstants.setResize(map, true);
        GraphConstants.setGradientColor(map, Color.red);
        GraphConstants.setBorderColor(map, Color.black);
        GraphConstants.setBackground(map, Color.white);
        GraphConstants.setOpaque(map, true);
        return map;
    }

    /**
	 * Creates the cell attributes his.
	 * 
	 * @param point the point
	 * 
	 * @return the map<?,?>
	 */
    public Map<?, ?> createCellAttributesHis(Point2D point) {
        Map<?, ?> map = new Hashtable<Object, Object>();
        point = graph.snap((Point2D) point.clone());
        GraphConstants.setBounds(map, new Rectangle2D.Double(point.getX(), point.getY(), 0, 0));
        GraphConstants.setResize(map, true);
        GraphConstants.setGradientColor(map, Color.black);
        GraphConstants.setBorderColor(map, Color.black);
        GraphConstants.setBackground(map, Color.black);
        GraphConstants.setOpaque(map, true);
        return map;
    }

    /**
	 * Creates the circle.
	 * 
	 * @return the circle
	 */
    protected circle createCircle() {
        circle cell = new circle("H" + new Integer(portCount++));
        cell.add(new DefaultPort("Port " + new Integer(portCount++)));
        allStates.put(cell.toString(), cell);
        return cell;
    }

    /**
	 * Creates the default edge.
	 * 
	 * @return the default edge
	 */
    protected DefaultEdge createDefaultEdge() {
        return new DefaultEdge("Transition " + new Integer(edgeCount++));
    }

    /**
	 * Creates the default edge.
	 * 
	 * @param name the name
	 * 
	 * @return the default edge
	 */
    protected DefaultEdge createDefaultEdge(String name) {
        return new DefaultEdge(name);
    }

    /**
	 * Creates the edge attributes.
	 * 
	 * @return the map<?,?>
	 */
    public Map<?, ?> createEdgeAttributes() {
        Map<?, ?> map = new Hashtable<Object, Object>();
        GraphConstants.setLineEnd(map, GraphConstants.ARROW_CLASSIC);
        GraphConstants.setLabelAlongEdge(map, true);
        return map;
    }

    /**
	 * Creates the graph.
	 * 
	 * @return the j graph
	 */
    protected JGraph createGraph() {
        return new VESTGraph(new VESTModel());
    }

    /**
	 * Creates the group cell.
	 * 
	 * @return the default graph cell
	 */
    protected DefaultGraphCell createGroupCell() {
        return new DefaultGraphCell();
    }

    /**
	 * Creates the orthogonal cell.
	 * 
	 * @return the orthogonal cell
	 */
    protected orthogonalCell createOrthogonalCell() {
        orthogonalCell cell = new orthogonalCell("Execution " + new Integer(cellCount++));
        allStates.put(cell.toString(), cell);
        return cell;
    }

    /**
	 * Creates the popup menu.
	 * 
	 * @param pt the pt
	 * @param cell the cell
	 * 
	 * @return the j popup menu
	 */
    public JPopupMenu createPopupMenu(final Point pt, final Object cell) {
        JPopupMenu menu = new JPopupMenu();
        if (cell != null) {
            menu.add(new AbstractAction("Change Name") {

                public void actionPerformed(ActionEvent e) {
                    graph.startEditingAtCell(cell);
                }
            });
        }
        if (cell != null) {
            if (cell instanceof orthogonalCell || cell instanceof circle) {
            } else {
                menu.add(new AbstractAction("Edit Cell Attributes") {

                    public void actionPerformed(ActionEvent e) {
                        editCellAttributes(cell);
                    }
                });
            }
        }
        if (!graph.isSelectionEmpty()) {
            menu.addSeparator();
            menu.add(new AbstractAction("Remove") {

                public void actionPerformed(ActionEvent e) {
                    remove.actionPerformed(e);
                }
            });
        }
        menu.addSeparator();
        menu.add(new AbstractAction("Insert Basic") {

            public void actionPerformed(ActionEvent ev) {
                insertBasic(pt);
            }
        });
        menu.add(new AbstractAction("Insert Compound") {

            public void actionPerformed(ActionEvent ev) {
                insertCompound(pt);
            }
        });
        menu.add(new AbstractAction("Insert Concurrent") {

            public void actionPerformed(ActionEvent ev) {
                insertAND(pt);
            }
        });
        menu.add(new AbstractAction("Insert Execution") {

            public void actionPerformed(ActionEvent ev) {
                insertOrtho(pt);
            }
        });
        return menu;
    }

    /**
	 * Create a status bar.
	 * 
	 * @return the j panel
	 */
    protected JPanel createStatusBar() {
        return new EdStatusBar(graph);
    }

    /**
	 * Creates the swim lane cell.
	 * 
	 * @return the swim lane cell
	 */
    protected SwimLaneCell createSwimLaneCell() {
        SwimLaneCell cell = new SwimLaneCell("Compound State " + new Integer(cellCount++));
        cell.add(new DefaultPort("Port " + new Integer(portCount++)));
        allStates.put(cell.toString(), cell);
        return cell;
    }

    /**
	 * Creates the tool bar.
	 * 
	 * @return the j tool bar
	 */
    public JToolBar createToolBar() {
        JToolBar toolbar = new JToolBar("test");
        toolbar.setFloatable(false);
        URL newUrl = getClass().getClassLoader().getResource("resources/new.png");
        ImageIcon newIcon = new ImageIcon(newUrl);
        toolbar.add(new AbstractAction("", newIcon) {

            public void actionPerformed(ActionEvent e) {
            }
        });
        URL openUrl = getClass().getClassLoader().getResource("resources/open.png");
        ImageIcon openIcon = new ImageIcon(openUrl);
        toolbar.add(new AbstractAction("", openIcon) {

            public void actionPerformed(ActionEvent e) {
                open();
            }
        });
        URL saveUrl = getClass().getClassLoader().getResource("resources/save.png");
        ImageIcon saveIcon = new ImageIcon(saveUrl);
        toolbar.add(new AbstractAction("", saveIcon) {

            public void actionPerformed(ActionEvent e) {
                saveAs();
            }
        });
        toolbar.addSeparator();
        URL insertUrl = getClass().getClassLoader().getResource("resources/shape_square.png");
        ImageIcon insertIcon = new ImageIcon(insertUrl);
        toolbar.add(new AbstractAction("", insertIcon) {

            public void actionPerformed(ActionEvent e) {
                insertBasic(new Point(10, 10));
            }
        });
        URL insertCompoundUrl = getClass().getClassLoader().getResource("resources/shape_square_add.png");
        ImageIcon insertCompoundIcon = new ImageIcon(insertCompoundUrl);
        toolbar.add(new AbstractAction("", insertCompoundIcon) {

            public void actionPerformed(ActionEvent e) {
                insertCompound(new Point(10, 10));
            }
        });
        URL insertAndUrl = getClass().getClassLoader().getResource("resources/shape_square_go.png");
        ImageIcon insertAndIcon = new ImageIcon(insertAndUrl);
        toolbar.add(new AbstractAction("", insertAndIcon) {

            /**
			 * 
			 */
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                insertAND(new Point(10, 10));
            }
        });
        URL insertOrthoUrl = getClass().getClassLoader().getResource("resources/table_multiple.png");
        ImageIcon insertOrthoIcon = new ImageIcon(insertOrthoUrl);
        toolbar.add(new AbstractAction("", insertOrthoIcon) {

            public void actionPerformed(ActionEvent e) {
                insertOrtho(new Point(10, 10));
            }
        });
        URL insertHistoryUrl = getClass().getClassLoader().getResource("resources/time.png");
        ImageIcon insertHistoryIcon = new ImageIcon(insertHistoryUrl);
        toolbar.add(new AbstractAction("", insertHistoryIcon) {

            public void actionPerformed(ActionEvent e) {
                insertHist(new Point(10, 10));
            }
        });
        toolbar.addSeparator();
        URL defaultUrl = getClass().getClassLoader().getResource("resources/default.gif");
        ImageIcon defaultIcon = new ImageIcon(defaultUrl);
        defaultSelection = new AbstractAction("", defaultIcon) {

            public void actionPerformed(ActionEvent e) {
                Object defaultCell;
                if (!graph.isSelectionEmpty() && (graph.getSelectionCells().length == 1)) {
                    defaultCell = graph.getSelectionCell();
                    if (defaultCell instanceof AndStateCell) {
                        ((AndStateCell) defaultCell).setDefault();
                        AttributeMap cellattrib = graph.getModel().getAttributes((AndStateCell) defaultCell);
                        if (((AndStateCell) defaultCell).isDefault()) {
                            GraphConstants.setGradientColor(cellattrib, Color.BLACK);
                        } else {
                            GraphConstants.setGradientColor(cellattrib, Color.GREEN);
                        }
                        ((AndStateCell) defaultCell).setAttributes(cellattrib);
                        CellView view = graph.getGraphLayoutCache().getMapping(((AndStateCell) defaultCell), false);
                        graph.getGraphLayoutCache().reload();
                        graph.repaint();
                    }
                    if (defaultCell instanceof SwimLaneCell) {
                        ((SwimLaneCell) defaultCell).setDefault();
                        AttributeMap cellattrib = graph.getModel().getAttributes((SwimLaneCell) defaultCell);
                        if (((SwimLaneCell) defaultCell).isDefault()) {
                            GraphConstants.setGradientColor(cellattrib, Color.BLACK);
                        } else {
                            GraphConstants.setGradientColor(cellattrib, Color.RED);
                        }
                        ((SwimLaneCell) defaultCell).setAttributes(cellattrib);
                        CellView view = graph.getGraphLayoutCache().getMapping(((SwimLaneCell) defaultCell), false);
                        graph.getGraphLayoutCache().reload();
                        graph.repaint();
                    }
                    if (defaultCell instanceof basicCell) {
                        ((basicCell) defaultCell).setDefault();
                        AttributeMap cellattrib = graph.getModel().getAttributes((basicCell) defaultCell);
                        if (((basicCell) defaultCell).isDefault()) {
                            GraphConstants.setGradientColor(cellattrib, Color.BLACK);
                        } else {
                            GraphConstants.setGradientColor(cellattrib, Color.BLUE);
                        }
                        ((basicCell) defaultCell).setAttributes(cellattrib);
                        CellView view = graph.getGraphLayoutCache().getMapping(((basicCell) defaultCell), false);
                        graph.getGraphLayoutCache().reload();
                        graph.repaint();
                    }
                }
            }
        };
        defaultSelection.setEnabled(false);
        toolbar.add(defaultSelection);
        URL connectUrl = getClass().getClassLoader().getResource("resources/chart_line.png");
        ImageIcon connectIcon = new ImageIcon(connectUrl);
        toolbar.add(new AbstractAction("", connectIcon) {

            public void actionPerformed(ActionEvent e) {
                graph.setPortsVisible(!graph.isPortsVisible());
                URL connectUrl;
                if (graph.isPortsVisible()) connectUrl = getClass().getClassLoader().getResource("resources/chart_line.png"); else connectUrl = getClass().getClassLoader().getResource("resources/chart_line_delete.png");
                ImageIcon connectIcon = new ImageIcon(connectUrl);
                putValue(SMALL_ICON, connectIcon);
            }
        });
        toolbar.addSeparator();
        URL whatContained = getClass().getClassLoader().getResource("resources/cog.png");
        ImageIcon whatContainedIcon = new ImageIcon(whatContained);
        toolbar.add(new AbstractAction("", whatContainedIcon) {

            public void actionPerformed(ActionEvent e) {
                printContained();
            }
        });
        URL checkContained = getClass().getClassLoader().getResource("resources/bug.png");
        ImageIcon checkContainedIcon = new ImageIcon(checkContained);
        toolbar.add(new AbstractAction("", checkContainedIcon) {

            public void actionPerformed(ActionEvent e) {
                checkChart();
            }
        });
        toolbar.addSeparator();
        URL undoUrl = getClass().getClassLoader().getResource("resources/arrow_undo.png");
        ImageIcon undoIcon = new ImageIcon(undoUrl);
        undo = new AbstractAction("", undoIcon) {

            public void actionPerformed(ActionEvent e) {
                undo();
            }
        };
        undo.setEnabled(false);
        toolbar.add(undo);
        URL redoUrl = getClass().getClassLoader().getResource("resources/arrow_redo.png");
        ImageIcon redoIcon = new ImageIcon(redoUrl);
        redo = new AbstractAction("", redoIcon) {

            public void actionPerformed(ActionEvent e) {
                redo();
            }
        };
        redo.setEnabled(false);
        toolbar.add(redo);
        toolbar.addSeparator();
        Action action;
        URL url;
        action = javax.swing.TransferHandler.getCopyAction();
        url = getClass().getClassLoader().getResource("resources/page_copy.PNG");
        action.putValue(Action.SMALL_ICON, new ImageIcon(url));
        toolbar.add(copy = new VESTEventRedirector(action, graph));
        action = javax.swing.TransferHandler.getPasteAction();
        url = getClass().getClassLoader().getResource("resources/paste_plain.png");
        action.putValue(Action.SMALL_ICON, new ImageIcon(url));
        toolbar.add(paste = new VESTEventRedirector(action, graph));
        action = javax.swing.TransferHandler.getCutAction();
        url = getClass().getClassLoader().getResource("resources/cut_red.png");
        action.putValue(Action.SMALL_ICON, new ImageIcon(url));
        toolbar.add(cut = new VESTEventRedirector(action, graph));
        URL removeUrl = getClass().getClassLoader().getResource("resources/delete.png");
        ImageIcon removeIcon = new ImageIcon(removeUrl);
        remove = new AbstractAction("", removeIcon) {

            public void actionPerformed(ActionEvent e) {
                if (!graph.isSelectionEmpty()) {
                    Object[] cells = graph.getSelectionCells();
                    cells = graph.getDescendants(cells);
                    for (int i = 0; i < cells.length; i++) {
                        if (cells[i] instanceof SwimLaneCell) {
                            allStates.remove(cells[i]);
                        } else if (cells[i] instanceof AndStateCell) {
                            allStates.remove(cells[i]);
                        } else if (cells[i] instanceof basicCell) {
                            allStates.remove(cells[i].toString());
                        } else if (cells[i] instanceof DefaultEdge) {
                        } else {
                        }
                    }
                    graph.getModel().remove(cells);
                }
            }
        };
        remove.setEnabled(false);
        toolbar.add(remove);
        toolbar.addSeparator();
        URL toFrontUrl = getClass().getClassLoader().getResource("resources/tofront.png");
        ImageIcon toFrontIcon = new ImageIcon(toFrontUrl);
        tofront = new AbstractAction("", toFrontIcon) {

            public void actionPerformed(ActionEvent e) {
                if (!graph.isSelectionEmpty()) toFront(graph.getSelectionCells());
            }
        };
        tofront.setEnabled(false);
        toolbar.add(tofront);
        URL toBackUrl = getClass().getClassLoader().getResource("resources/toback.png");
        ImageIcon toBackIcon = new ImageIcon(toBackUrl);
        toback = new AbstractAction("", toBackIcon) {

            public void actionPerformed(ActionEvent e) {
                if (!graph.isSelectionEmpty()) toBack(graph.getSelectionCells());
            }
        };
        toback.setEnabled(false);
        toolbar.add(toback);
        toolbar.addSeparator();
        URL zoomUrl = getClass().getClassLoader().getResource("resources/zoom.png");
        ImageIcon zoomIcon = new ImageIcon(zoomUrl);
        toolbar.add(new AbstractAction("", zoomIcon) {

            public void actionPerformed(ActionEvent e) {
                graph.setScale(1.0);
            }
        });
        URL zoomInUrl = getClass().getClassLoader().getResource("resources/zoom_in.png");
        ImageIcon zoomInIcon = new ImageIcon(zoomInUrl);
        toolbar.add(new AbstractAction("", zoomInIcon) {

            public void actionPerformed(ActionEvent e) {
                graph.setScale(2 * graph.getScale());
            }
        });
        URL zoomOutUrl = getClass().getClassLoader().getResource("resources/zoom_out.png");
        ImageIcon zoomOutIcon = new ImageIcon(zoomOutUrl);
        toolbar.add(new AbstractAction("", zoomOutIcon) {

            public void actionPerformed(ActionEvent e) {
                graph.setScale(graph.getScale() / 2);
            }
        });
        toolbar.addSeparator();
        URL groupUrl = getClass().getClassLoader().getResource("resources/shape_group.png");
        ImageIcon groupIcon = new ImageIcon(groupUrl);
        group = new AbstractAction("", groupIcon) {

            public void actionPerformed(ActionEvent e) {
                group(graph.getSelectionCells());
            }
        };
        group.setEnabled(false);
        toolbar.add(group);
        URL ungroupUrl = getClass().getClassLoader().getResource("resources/shape_ungroup.png");
        ImageIcon ungroupIcon = new ImageIcon(ungroupUrl);
        ungroup = new AbstractAction("", ungroupIcon) {

            public void actionPerformed(ActionEvent e) {
                ungroup(graph.getSelectionCells());
            }
        };
        ungroup.setEnabled(false);
        toolbar.add(ungroup);
        toolbar.addSeparator();
        URL collapseUrl = getClass().getClassLoader().getResource("resources/shape_group.png");
        ImageIcon collapseIcon = new ImageIcon(collapseUrl);
        collapse = new AbstractAction("", collapseIcon) {

            public void actionPerformed(ActionEvent e) {
                collapse(graph.getSelectionCells());
            }
        };
        collapse.setEnabled(true);
        toolbar.add(collapse);
        return toolbar;
    }

    public void dragEnter(DropTargetDragEvent event) {
        event.acceptDrag(DnDConstants.ACTION_MOVE);
    }

    public void dragExit(DropTargetEvent event) {
    }

    public void dragOver(DropTargetDragEvent event) {
    }

    public void drop(DropTargetDropEvent event) {
        String DNDtext;
        Transferable transferable = event.getTransferable();
        DNDtext = null;
        try {
            DNDtext = (String) transferable.getTransferData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (DNDtext.equals("Basic Cell")) {
            insertBasic(event.getLocation());
        } else if (DNDtext.equals("Compound Cell")) {
            insertCompound(event.getLocation());
        } else if (DNDtext.equals("Orthogonal Cell")) {
            insertAND(event.getLocation());
        } else if (DNDtext.equals("Execution Cell")) {
            insertOrtho(event.getLocation());
        } else if (DNDtext.equals("History")) {
            insertHist(event.getLocation());
        } else {
            addSubChart(DNDtext, event.getLocation());
        }
    }

    public void dropActionChanged(DropTargetDragEvent event) {
    }

    /**
	 * Edits the cell attributes.
	 * 
	 * @param cell the cell
	 */
    public void editCellAttributes(Object cell) {
        editFrame editCell = new editFrame(cell, graph, condCount, groupCount);
    }

    /**
	 * Gets the cell count.
	 * 
	 * @param graph the graph
	 * 
	 * @return the cell count
	 */
    protected int getCellCount(JGraph graph) {
        Object[] cells = graph.getDescendants(graph.getRoots());
        return cells.length;
    }

    /**
	 * Gets the graph.
	 * 
	 * @return Returns the graph.
	 */
    public JGraph getGraph() {
        return graph;
    }

    /**
	 * Gets the version.
	 * 
	 * @return a String representing the version of this application
	 */
    protected String getVersion() {
        return editorVersion;
    }

    /**
	 * Group.
	 * 
	 * @param cells the cells
	 */
    public void group(Object[] cells) {
        cells = graph.order(cells);
        if (cells != null && cells.length > 0) {
            DefaultGraphCell group = createGroupCell();
            graph.getGraphLayoutCache().insertGroup(group, cells);
        }
    }

    /**
	 * Insert and.
	 * 
	 * @param point the point
	 */
    public void insertAND(Point2D point) {
        AndStateCell vertex = createAndStateCell();
        vertex.getAttributes().applyMap(createCellAttributesAND(point));
        ((AndStateCell) vertex).setCondGroups(condCount, groupCount);
        graph.getGraphLayoutCache().insert(vertex);
    }

    /**
	 * Insert basic.
	 * 
	 * @param point the point
	 */
    public void insertBasic(Point2D point) {
        basicCell vertex = createBasicCell();
        vertex.getAttributes().applyMap(createCellAttributes(point));
        ((basicCell) vertex).setCondGroups(condCount, groupCount);
        graph.getGraphLayoutCache().insert(vertex);
    }

    /**
	 * Insert compound.
	 * 
	 * @param point the point
	 */
    public void insertCompound(Point2D point) {
        SwimLaneCell vertex = createSwimLaneCell();
        vertex.getAttributes().applyMap(createCellAttributesCom(point));
        ((SwimLaneCell) vertex).setCondGroups(condCount, groupCount);
        graph.getGraphLayoutCache().insert(vertex);
    }

    /**
	 * Insert hist.
	 * 
	 * @param point the point
	 */
    public void insertHist(Point2D point) {
        circle vertex = createCircle();
        vertex.getAttributes().applyMap(createCellAttributesHis(point));
        graph.getGraphLayoutCache().insert(vertex);
    }

    /**
	 * Insert ortho.
	 * 
	 * @param point the point
	 */
    public void insertOrtho(Point2D point) {
        orthogonalCell vertex = createOrthogonalCell();
        vertex.getAttributes().applyMap(createCellAttributes(point));
        graph.getGraphLayoutCache().insert(vertex);
    }

    /**
	 * Checks if is group.
	 * 
	 * @param cell the cell
	 * 
	 * @return true, if is group
	 */
    public boolean isGroup(Object cell) {
        CellView view = graph.getGraphLayoutCache().getMapping(cell, false);
        if (view != null) return !view.isLeaf();
        return false;
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE) remove.actionPerformed(null);
    }

    /**
	 * Open.
	 */
    public void open() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.addChoosableFileFilter(new VESTChartFilter());
        fileChooser.setCurrentDirectory(new File("."));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.CANCEL_OPTION) return;
        File fileName = fileChooser.getSelectedFile();
        if (fileName == null || fileName.getName().equals("")) {
            JOptionPane.showMessageDialog(this, "Invalid File Name", "Invalid File Name", JOptionPane.ERROR_MESSAGE);
        } else {
            try {
                clearGraph();
                input = new ObjectInputStream(new FileInputStream(fileName));
                int length = input.readInt();
                Object[] cells = new Object[length];
                Hashtable<DefaultGraphCell, AttributeMap> attrib = new Hashtable<DefaultGraphCell, AttributeMap>();
                for (int i = 0; i < length; i++) {
                    DefaultGraphCell tmp = (DefaultGraphCell) input.readObject();
                    if (tmp instanceof DefaultEdge) {
                    } else if (tmp instanceof basicCell) {
                        attrib.put(tmp, tmp.getAttributes());
                        cells[i] = tmp;
                    } else if (tmp instanceof SwimLaneCell) {
                        attrib.put(tmp, tmp.getAttributes());
                        cells[i] = tmp;
                    } else if (tmp instanceof AndStateCell) {
                        attrib.put(tmp, tmp.getAttributes());
                        cells[i] = tmp;
                    } else if (tmp instanceof orthogonalCell) {
                        attrib.put(tmp, tmp.getAttributes());
                        cells[i] = tmp;
                    } else if (tmp instanceof circle) {
                        attrib.put(tmp, tmp.getAttributes());
                        cells[i] = tmp;
                    }
                }
                graph.getGraphLayoutCache().insert(cells, attrib, null, null, null);
                int counter = input.readInt();
                for (int j = 0; j < counter; j++) {
                    String name, source, target;
                    name = (String) input.readObject();
                    source = (String) input.readObject();
                    target = (String) input.readObject();
                    remakeConnection(name, source, target);
                }
                input.close();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error Opening File", "Error", JOptionPane.ERROR_MESSAGE);
                System.out.println(e);
            } catch (ClassNotFoundException e) {
                JOptionPane.showMessageDialog(this, "File is not an Evaluation!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
	 * Prints the.
	 * 
	 * @param g the g
	 * @param pF the p f
	 * @param page the page
	 * 
	 * @return the int
	 */
    public int print(Graphics g, PageFormat pF, int page) {
        int pwi = (int) pF.getImageableWidth();
        int phi = (int) pF.getImageableHeight();
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        int sdpi = toolkit.getScreenResolution();
        double gs = 72.0 / sdpi / 2.0;
        int gw = (int) (graph.getWidth() * gs);
        int gh = (int) (graph.getHeight() * gs);
        int cols = (gw + (pwi - 1)) / pwi;
        int rows = (gh + (phi - 1)) / phi;
        int nPage = cols * rows;
        if (page >= nPage) {
            return 1;
        }
        int row = page / cols;
        int col = page - (row * cols);
        RepaintManager currentManager = RepaintManager.currentManager(graph);
        boolean isEnabled = currentManager.isDoubleBufferingEnabled();
        double os = graph.getScale();
        graph.setScale(os * gs);
        currentManager.setDoubleBufferingEnabled(false);
        g.translate((-col * pwi) + (int) pF.getImageableX(), (-row * phi) + (int) pF.getImageableY());
        g.setClip(col * pwi, row * phi, pwi, phi);
        graph.paint(g);
        String text = "Page " + (page + 1) + " of " + cols * rows;
        FontMetrics metrics = g.getFontMetrics();
        int width = metrics.stringWidth(text);
        int x = col * pwi + pwi - width;
        int y = row * phi + phi - metrics.getDescent() - metrics.getLeading();
        g.setColor(Color.BLACK);
        g.drawString(text, x, y);
        g.drawRect(col * pwi, row * phi, pwi, phi);
        graph.setScale(os);
        currentManager.setDoubleBufferingEnabled(isEnabled);
        return 0;
    }

    /**
	 * Prints the contained.
	 */
    public void printContained() {
        BufferedWriter outputChart;
        outputChart = null;
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.CANCEL_OPTION) return;
        File fileName = fileChooser.getSelectedFile();
        if (fileName == null || fileName.getName().equals("")) {
            JOptionPane.showMessageDialog(this, "Invalid File Name", "Invalid File Name", JOptionPane.ERROR_MESSAGE);
        } else {
            try {
                outputChart = new BufferedWriter(new FileWriter(fileName));
                System.out.println("I SHOULD work");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error Saving File", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        Object[] cells = graph.getDescendants(graph.getRoots());
        for (int i = 0; i < cells.length; i++) {
            if (isGroup(cells[i])) {
                System.out.println("I found a group Cell, do something please");
            }
        }
        allStates = new Hashtable<String, Object>();
        allTransitions = new Hashtable<String, Object>();
        for (int i = 0; i < cells.length; i++) {
            if (cells[i] instanceof DefaultEdge) {
                allTransitions.put(cells[i].toString(), cells[i]);
            } else if (cells[i] instanceof basicCell) {
                allStates.put(cells[i].toString(), cells[i]);
            } else if (cells[i] instanceof SwimLaneCell) {
                allStates.put(cells[i].toString(), cells[i]);
            } else if (cells[i] instanceof AndStateCell) {
                allStates.put(cells[i].toString(), cells[i]);
            } else if (cells[i] instanceof orthogonalCell) {
                allStates.put(cells[i].toString(), cells[i]);
            } else if (cells[i] instanceof circle) {
                allStates.put(cells[i].toString(), cells[i]);
            }
        }
        stateChart output = new stateChart(allStates, allTransitions, graph, condCount, groupCount);
        if (output.getValid()) {
            output.printChart(outputChart);
        }
        try {
            outputChart.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
	 * Small routine that will make node out of the first entry in the array,
	 * then make nodes out of subsequent entries and make them child nodes of
	 * the first one. The process is repeated recursively for entries that are
	 * arrays.
	 * 
	 * @param hierarchy the hierarchy
	 * 
	 * @return the default mutable tree node
	 */
    private DefaultMutableTreeNode processHierarchy(Object[] hierarchy) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(hierarchy[0]);
        DefaultMutableTreeNode child;
        for (int i = 1; i < hierarchy.length; i++) {
            Object nodeSpecifier = hierarchy[i];
            if (nodeSpecifier instanceof Object[]) child = processHierarchy((Object[]) nodeSpecifier); else child = new DefaultMutableTreeNode(nodeSpecifier);
            node.add(child);
        }
        return (node);
    }

    /**
	 * Redo.
	 */
    public void redo() {
        try {
            undoManager.redo(graph.getGraphLayoutCache());
        } catch (Exception ex) {
            System.err.println(ex);
        } finally {
            updateHistoryButtons();
        }
    }

    /**
	 * Remake connection.
	 * 
	 * @param name the name
	 * @param sourceCell the source cell
	 * @param targetCell the target cell
	 */
    public void remakeConnection(String name, String sourceCell, String targetCell) {
        DefaultEdge edge = createDefaultEdge(name);
        allTransitions.put(edge.toString(), edge);
        Object[] cells = graph.getRoots();
        Port source, target;
        source = target = null;
        for (int j = 0; j < cells.length; j++) {
            DefaultGraphCell temp = (DefaultGraphCell) cells[j];
            if (temp instanceof DefaultEdge) {
            } else if (temp != null) {
                if (((DefaultGraphCell) temp).toString() == sourceCell) {
                    source = (Port) ((DefaultGraphCell) temp).getFirstChild();
                } else if (((DefaultGraphCell) temp).toString() == targetCell) {
                    target = (Port) ((DefaultGraphCell) temp).getFirstChild();
                }
            }
        }
        if (graph.getModel().acceptsSource(edge, source) && graph.getModel().acceptsTarget(edge, target)) {
            edge.getAttributes().applyMap(createEdgeAttributes());
            graph.getGraphLayoutCache().insertEdge(edge, source, target);
        }
    }

    /**
	 * Remake connection.
	 * 
	 * @param name the name
	 * @param sourceCell the source cell
	 * @param targetCell the target cell
	 * @param subgraph the subgraph
	 */
    public void remakeConnection(String name, String sourceCell, String targetCell, JGraph subgraph) {
        DefaultEdge edge = createDefaultEdge(name);
        allTransitions.put(edge.toString(), edge);
        Object[] cells = graph.getRoots();
        Port source, target;
        source = target = null;
        for (int j = 0; j < cells.length; j++) {
            DefaultGraphCell temp = (DefaultGraphCell) cells[j];
            if (temp instanceof DefaultEdge) {
            } else if (temp != null) {
                if (((DefaultGraphCell) temp).toString() == sourceCell) {
                    source = (Port) ((DefaultGraphCell) temp).getFirstChild();
                } else if (((DefaultGraphCell) temp).toString() == targetCell) {
                    target = (Port) ((DefaultGraphCell) temp).getFirstChild();
                }
            }
        }
        if (subgraph.getModel().acceptsSource(edge, source) && subgraph.getModel().acceptsTarget(edge, target)) {
            edge.getAttributes().applyMap(createEdgeAttributes());
            subgraph.getGraphLayoutCache().insertEdge(edge, source, target);
        }
    }

    /**
	 * Save.
	 */
    public void save() {
        try {
            Object[] cells = graph.getRoots();
            output.writeInt(cells.length);
            int counter = 0;
            for (int i = 0; i < cells.length; i++) {
                DefaultGraphCell temp = (DefaultGraphCell) cells[i];
                if (temp instanceof DefaultEdge) {
                    counter++;
                }
                output.writeObject(temp);
            }
            output.writeInt(counter);
            for (int i = 0; i < cells.length; i++) {
                DefaultGraphCell temp = (DefaultGraphCell) cells[i];
                if (temp instanceof DefaultEdge) {
                    output.writeObject(((DefaultEdge) temp).toString());
                    for (int j = 0; j < cells.length; j++) {
                        DefaultGraphCell temp1 = (DefaultGraphCell) cells[j];
                        if (temp1 instanceof basicCell) {
                            if (((basicCell) temp1).getFirstChild().toString() == ((DefaultEdge) temp).getSource().toString()) {
                                output.writeObject(temp1.toString());
                            }
                        } else if (temp1 instanceof SwimLaneCell) {
                            if (((SwimLaneCell) temp1).getFirstChild().toString() == ((DefaultEdge) temp).getSource().toString()) {
                                output.writeObject(temp1.toString());
                            }
                        } else if (temp1 instanceof AndStateCell) {
                            if (((AndStateCell) temp1).getFirstChild().toString() == ((DefaultEdge) temp).getSource().toString()) {
                                output.writeObject(temp1.toString());
                            }
                        }
                    }
                    for (int j = 0; j < cells.length; j++) {
                        DefaultGraphCell temp1 = (DefaultGraphCell) cells[j];
                        if (temp1 instanceof basicCell) {
                            if (((basicCell) temp1).getFirstChild().toString() == ((DefaultEdge) temp).getTarget().toString()) {
                                output.writeObject(temp1.toString());
                            }
                        } else if (temp1 instanceof SwimLaneCell) {
                            if (((SwimLaneCell) temp1).getFirstChild().toString() == ((DefaultEdge) temp).getTarget().toString()) {
                                output.writeObject(temp1.toString());
                            }
                        } else if (temp1 instanceof AndStateCell) {
                            if (((AndStateCell) temp1).getFirstChild().toString() == ((DefaultEdge) temp).getTarget().toString()) {
                                output.writeObject(temp1.toString());
                            }
                        } else if (temp1 instanceof circle) {
                            if (((circle) temp1).getFirstChild().toString() == ((DefaultEdge) temp).getTarget().toString()) {
                                output.writeObject(temp1.toString());
                            }
                        }
                    }
                }
            }
            closeFile();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error Opening File", "Error", JOptionPane.ERROR_MESSAGE);
            System.out.println(e);
        }
    }

    /**
	 * Save as.
	 */
    public void saveAs() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.addChoosableFileFilter(new VESTChartFilter());
        fileChooser.setCurrentDirectory(new File("."));
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.CANCEL_OPTION) return;
        File fileName = fileChooser.getSelectedFile();
        if (fileName == null || fileName.getName().equals("")) {
            JOptionPane.showMessageDialog(this, "Invalid File Name", "Invalid File Name", JOptionPane.ERROR_MESSAGE);
        } else {
            try {
                output = new ObjectOutputStream(new FileOutputStream(fileName));
                save();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error Saving File", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
	 * Sets the graph.
	 * 
	 * @param graph The graph to set.
	 */
    public void setGraph(JGraph graph) {
        this.graph = graph;
    }

    /**
	 * To back.
	 * 
	 * @param c the c
	 */
    public void toBack(Object[] c) {
        graph.getGraphLayoutCache().toBack(c);
    }

    /**
	 * To front.
	 * 
	 * @param c the c
	 */
    public void toFront(Object[] c) {
        graph.getGraphLayoutCache().toFront(c);
    }

    /**
	 * Undo.
	 */
    public void undo() {
        try {
            undoManager.undo(graph.getGraphLayoutCache());
        } catch (Exception ex) {
            System.err.println(ex);
        } finally {
            updateHistoryButtons();
        }
    }

    /**
	 * Ungroup.
	 * 
	 * @param cells the cells
	 */
    public void ungroup(Object[] cells) {
        graph.getGraphLayoutCache().ungroup(cells);
    }

    /**
	 * Update history buttons.
	 */
    protected void updateHistoryButtons() {
        undo.setEnabled(undoManager.canUndo(graph.getGraphLayoutCache()));
        redo.setEnabled(undoManager.canRedo(graph.getGraphLayoutCache()));
    }

    public void valueChanged(GraphSelectionEvent e) {
        group.setEnabled(graph.getSelectionCount() > 1);
        boolean enabled = !graph.isSelectionEmpty();
        remove.setEnabled(enabled);
        ungroup.setEnabled(enabled);
        tofront.setEnabled(enabled);
        toback.setEnabled(enabled);
        copy.setEnabled(enabled);
        cut.setEnabled(enabled);
        defaultSelection.setEnabled(graph.getSelectionCount() == 1);
    }

    public void keyReleased(KeyEvent arg0) {
    }

    public void keyTyped(KeyEvent arg0) {
    }
}
