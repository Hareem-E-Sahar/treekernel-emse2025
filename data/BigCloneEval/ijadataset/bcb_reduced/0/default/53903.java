import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.beans.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import javax.swing.text.*;
import javax.swing.undo.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
import javax.swing.*;
import org.jcad.JCad.JCadGraph;
import org.jcad.JCad.*;
import org.jcad.JCad.ui.*;
import org.jcad.JCad.action.*;
import org.jcad.JCad.util.*;

public class JCad extends JPanel {

    private static ResourceBundle resources;

    private JMenuBar menubar;

    private JToolBar maintoolbar;

    private JToolBar drawtoolbar;

    private JCadPanel cadpanel;

    private Hashtable commands;

    private Hashtable menuItems;

    private JCadStatus status;

    private JPopupMenu rightclickmenu;

    public static final String openAction = "open";

    public static final String newAction = "new";

    public static final String saveAction = "save";

    public static final String redrawAction = "redraw";

    public static final String exitAction = "exit";

    public static final String zoominAction = "zoomin";

    public static final String zoomoutAction = "zoomout";

    public static final String zoompageAction = "zoompage";

    public static final String layerAction = "layer";

    public static final String linetypeAction = "linetype";

    public static final String deleteAction = "delete";

    public static final String aboutAction = "about";

    private UndoAction undoAction = new UndoAction();

    private RedoAction redoAction = new RedoAction();

    private Action[] defaultActions = { new NewAction(), new OpenAction(), new JCadSaveAction(), new ExitAction(), new RedrawAction(), new ZoominAction(), new ZoomoutAction(), new ZoompageAction(), new JCadZoomWindowAction(), new DeleteAction(), new LayerAction(), new JCadBlockAction(), new LinetypeAction(), new AboutAction(), undoAction, redoAction, new JCadDrawLineAction(), new JCadDrawCircleAction(), new JCadDrawCircle3PAction(), new JCadDrawArcAction(), new JCadDrawArc3PAction(), new JCadDrawTextAction(), new JCadDeleteEntityAction(), new JCadEditEntityAction(), new JCadMoveEntityAction(), new JCadCancelAction() };

    static {
        try {
            resources = ResourceBundle.getBundle("resources.JCad", Locale.getDefault());
        } catch (MissingResourceException mre) {
            System.err.println("resources/JCad.properties not found");
            System.exit(1);
        }
    }

    JCad() {
        super(true);
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Error loading Look & Feel:" + e);
        }
        setBorder(BorderFactory.createEtchedBorder());
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        setLayout(gridbag);
        commands = new Hashtable();
        Action[] actions = getActions();
        for (int i = 0; i < actions.length; i++) {
            Action a = actions[i];
            commands.put(a.getValue(Action.NAME), a);
        }
        JScrollPane scroller = new JScrollPane();
        JViewport port = scroller.getViewport();
        menuItems = new Hashtable();
        cadpanel = new JCadPanel();
        cadpanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        JCadUtil.setCadPanel(cadpanel);
        cadpanel.margin = Integer.valueOf(resources.getString("Margin")).intValue();
        port.add(cadpanel);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        rightclickmenu = createPopupMenu("rightclick");
        JCadUtil.setRightClickMenu(rightclickmenu);
        menubar = createMenuBar();
        maintoolbar = createToolbar("maintoolbar");
        gridbag.setConstraints(menubar, gbc);
        add(menubar);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridheight = 1;
        gbc.weighty = 0.0;
        gridbag.setConstraints(maintoolbar, gbc);
        add(maintoolbar);
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(panel, gbc);
        drawtoolbar = createToolbar("drawtoolbar");
        drawtoolbar.setEnabled(false);
        panel.add("North", drawtoolbar);
        panel.add("Center", scroller);
        panel.add("South", createStatusbar());
        JCadUtil.setStatus(status);
        add(panel);
    }

    public static void main(String[] args) {
        try {
            String vers = System.getProperty("java.version");
            if (vers.compareTo("1.1.2") < 0) {
                System.out.println("!!!WARNING: Swing must be run with a " + "1.1.2 or higher version VM!!!");
            }
            JFrame frame = new JFrame();
            frame.setTitle(resources.getString("Title"));
            frame.setBackground(Color.lightGray);
            frame.getContentPane().setLayout(new BorderLayout());
            frame.getContentPane().add("Center", new JCad());
            frame.pack();
            frame.addWindowListener(new AppCloser());
            frame.setSize(800, 700);
            frame.show();
        } catch (Throwable t) {
            System.out.println("uncaught exception: " + t);
            t.printStackTrace();
        }
    }

    protected static final class AppCloser extends WindowAdapter {

        public void windowClosing(WindowEvent e) {
            String exitMessage = JCadUtil.getResources().getString("exitMessage");
            if (JOptionPane.showConfirmDialog(null, exitMessage, "Exit", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                System.exit(0);
            } else {
                e.getWindow().show();
            }
        }
    }

    protected URL getResource(String key) {
        String name = getResourceString(key);
        if (name != null) {
            URL url = this.getClass().getResource(name);
            return url;
        }
        return null;
    }

    protected Action getAction(String key) {
        return (Action) commands.get(key);
    }

    protected Action[] getActions() {
        return defaultActions;
    }

    protected String getResourceString(String nm) {
        String str;
        try {
            str = resources.getString(nm);
        } catch (MissingResourceException mre) {
            str = null;
        }
        return str;
    }

    private Component createStatusbar() {
        status = new JCadStatus();
        return status;
    }

    private JMenuBar createMenuBar() {
        JMenuBar mb = new JMenuBar();
        String[] menuKeys = tokenize(getResourceString("menubar"));
        for (int i = 0; i < menuKeys.length; i++) {
            JMenu m = createMenu(menuKeys[i]);
            if (m != null) {
                mb.add(m);
            }
        }
        return mb;
    }

    /**
     * Create a menu for the app.  By default this pulls the
     * definition of the menu from the associated resource file.
     */
    protected JMenu createMenu(String key) {
        String[] itemKeys = tokenize(getResourceString(key));
        JMenu menu = new JMenu(getResourceString(key + "Label"));
        for (int i = 0; i < itemKeys.length; i++) {
            if (itemKeys[i].equals("-")) {
                menu.addSeparator();
            } else {
                String option = getResourceString(itemKeys[i] + "Option");
                if (option == null) {
                    JMenuItem mi = createMenuItem(itemKeys[i]);
                    menu.add(mi);
                } else {
                    JMenu submenu = new JMenu(getResourceString(itemKeys[i] + "Label"));
                    menu.add(submenu);
                    String[] optionKeys = tokenize(option);
                    for (int k = 0; k < optionKeys.length; k++) {
                        JMenuItem mi = createMenuItem(optionKeys[k]);
                        submenu.add(mi);
                    }
                }
            }
        }
        return menu;
    }

    /**
     * Create a menu for the app.  By default this pulls the
     * definition of the menu from the associated resource file.
     */
    protected JPopupMenu createPopupMenu(String key) {
        String[] itemKeys = tokenize(getResourceString(key));
        JPopupMenu menu = new JPopupMenu(getResourceString(key + "Label"));
        for (int i = 0; i < itemKeys.length; i++) {
            if (itemKeys[i].equals("-")) {
                menu.addSeparator();
            } else {
                String option = getResourceString(itemKeys[i] + "Option");
                if (option == null) {
                    JMenuItem mi = createMenuItem(itemKeys[i]);
                    menu.add(mi);
                } else {
                    JMenu submenu = new JMenu(getResourceString(itemKeys[i] + "Label"));
                    menu.add(submenu);
                    String[] optionKeys = tokenize(option);
                    for (int k = 0; k < optionKeys.length; k++) {
                        JMenuItem mi = createMenuItem(optionKeys[k]);
                        submenu.add(mi);
                    }
                }
            }
        }
        return menu;
    }

    /**
     * This is the hook through which all menu items are
     * created.  It registers the result with the menuitem
     * hashtable so that it can be fetched with getMenuItem().
     * @see #getMenuItem
     */
    protected JMenuItem createMenuItem(String cmd) {
        JMenuItem mi = new JMenuItem(getResourceString(cmd + "Label"));
        URL url = getResource(cmd + "Image");
        if (url != null) {
            mi.setHorizontalTextPosition(JButton.RIGHT);
            mi.setIcon(new ImageIcon(url));
        }
        String astr = getResourceString(cmd + "Action");
        if (astr == null) {
            astr = cmd;
        }
        mi.setActionCommand(astr);
        Action a = getAction(astr);
        if (a != null) {
            mi.addActionListener(a);
            mi.setEnabled(a.isEnabled());
        } else {
            mi.setEnabled(false);
        }
        menuItems.put(cmd, mi);
        return mi;
    }

    /**
     * Find the Frame,for filedialog
     **/
    protected Frame getFrame() {
        for (Container p = getParent(); p != null; p = p.getParent()) {
            if (p instanceof Frame) {
                return (Frame) p;
            }
        }
        return null;
    }

    /**
     * Create the toolbar.  By default this reads the
     * resource file for the definition of the toolbar.
     */
    private JToolBar createToolbar(String name) {
        JToolBar toolbar = new JToolBar();
        String[] toolKeys = tokenize(getResourceString(name));
        for (int i = 0; i < toolKeys.length; i++) {
            if (toolKeys[i].equals("-")) {
                toolbar.add(Box.createHorizontalStrut(5));
            } else {
                toolbar.add(createTool(toolKeys[i]));
            }
        }
        toolbar.add(Box.createHorizontalGlue());
        return toolbar;
    }

    /**
     * Hook through which every toolbar item is created.
     */
    protected Component createTool(String key) {
        return createToolbarButton(key);
    }

    /**
     * Create a button to go inside of the toolbar.  By default this
     * will load an image resource.  The image filename is relative to
     * the classpath (including the '.' directory if its a part of the
     * classpath), and may either be in a JAR file or a separate file.
     *
     * @param key The key in the resource file to serve as the basis
     *  of lookups.
     */
    protected JComponent createToolbarButton(String key) {
        String option = getResourceString(key + "Option");
        if (option == null) {
            URL url = getResource(key + "Image");
            JButton b = new JButton(new ImageIcon(url)) {

                public float getAlignmentY() {
                    return 0.5f;
                }
            };
            b.setRequestFocusEnabled(false);
            b.setMargin(new Insets(1, 1, 1, 1));
            String astr = getResourceString(key + "Action");
            if (astr == null) {
                astr = key;
            }
            Action a = getAction(astr);
            if (a != null) {
                b.setActionCommand(astr);
                b.addActionListener(a);
            } else {
                b.setEnabled(false);
            }
            String tip = getResourceString(key + "Tooltip");
            if (tip != null) {
                b.setToolTipText(tip);
            }
            return b;
        } else {
            String[] optionKeys = tokenize(option);
            URL url = getResource(key + "Image");
            JCadComboButton b = new JCadComboButton(new ImageIcon(url));
            b.setRequestFocusEnabled(false);
            b.setMargin(new Insets(1, 1, 1, 1));
            for (int i = 0; i < optionKeys.length; i++) {
                JMenuItem mi = createMenuItem(optionKeys[i]);
                b.addItem(mi);
            }
            String tip = getResourceString(key + "Tooltip");
            if (tip != null) {
                b.setToolTipText(tip);
            }
            return b;
        }
    }

    /**
     * Take the given string and chop it up into a series
     * of strings on whitespace boundries.  This is useful
     * for trying to get an array of strings out of the
     * resource file.
     */
    protected String[] tokenize(String input) {
        Vector v = new Vector();
        StringTokenizer t = new StringTokenizer(input);
        String cmd[];
        while (t.hasMoreTokens()) v.addElement(t.nextToken());
        cmd = new String[v.size()];
        for (int i = 0; i < cmd.length; i++) cmd[i] = (String) v.elementAt(i);
        return cmd;
    }

    class UndoAction extends AbstractAction {

        public UndoAction() {
            super("Undo");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
        }

        protected void update() {
        }
    }

    class RedoAction extends AbstractAction {

        public RedoAction() {
            super("Redo");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
        }

        protected void update() {
        }
    }

    class OpenAction extends NewAction {

        OpenAction() {
            super(openAction);
        }

        public void actionPerformed(ActionEvent e) {
            Frame frame = getFrame();
            JFileChooser chooser = new JFileChooser(new File("."));
            JCadFileFilter filter = new JCadFileFilter(new String("DXF"), "DXF Files");
            chooser.addChoosableFileFilter(filter);
            int returnVal = chooser.showOpenDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                String file = chooser.getSelectedFile().getName();
                if (file == null) {
                    return;
                }
                File f = chooser.getSelectedFile();
                if (f.exists()) {
                    frame.setTitle(file);
                    cadpanel.loadFile(f);
                }
                repaint();
            }
        }
    }

    class NewAction extends AbstractAction {

        NewAction() {
            super(newAction);
        }

        NewAction(String nm) {
            super(nm);
        }

        public void actionPerformed(ActionEvent e) {
            JCadNewDialog newdialog = new JCadNewDialog(getFrame());
            if (newdialog.show() == 0) {
            }
        }
    }

    class DeleteAction extends AbstractAction {

        DeleteAction() {
            super(deleteAction);
        }

        DeleteAction(String nm) {
            super(nm);
        }

        public void actionPerformed(ActionEvent e) {
        }
    }

    class ZoompageAction extends AbstractAction {

        ZoompageAction() {
            super(zoompageAction);
        }

        ZoompageAction(String nm) {
            super(nm);
        }

        public void actionPerformed(ActionEvent e) {
            cadpanel.zoompage();
            repaint();
        }
    }

    class ZoominAction extends AbstractAction {

        ZoominAction() {
            super(zoominAction);
        }

        ZoominAction(String nm) {
            super(nm);
        }

        public void actionPerformed(ActionEvent e) {
            cadpanel.zoomin();
            repaint();
        }
    }

    class ZoomoutAction extends AbstractAction {

        ZoomoutAction() {
            super(zoomoutAction);
        }

        ZoomoutAction(String nm) {
            super(nm);
        }

        public void actionPerformed(ActionEvent e) {
            cadpanel.zoomout();
            repaint();
        }
    }

    class RedrawAction extends AbstractAction {

        RedrawAction() {
            super(redrawAction);
        }

        RedrawAction(String nm) {
            super(nm);
        }

        public void actionPerformed(ActionEvent e) {
            repaint();
        }
    }

    class LinetypeAction extends AbstractAction {

        LinetypeAction() {
            super(linetypeAction);
        }

        LinetypeAction(String nm) {
            super(nm);
        }

        public void actionPerformed(ActionEvent e) {
            JCadLinetypeDialog linetypedlg = new JCadLinetypeDialog(getFrame(), cadpanel.getGraph());
            linetypedlg.pack();
            linetypedlg.setVisible(true);
        }
    }

    class LayerAction extends AbstractAction {

        LayerAction() {
            super(layerAction);
        }

        LayerAction(String nm) {
            super(nm);
        }

        public void actionPerformed(ActionEvent e) {
            JCadLayerDialog layerdlg = new JCadLayerDialog(getFrame(), cadpanel.getGraph());
            layerdlg.pack();
            layerdlg.setVisible(true);
            repaint();
        }
    }

    class AboutAction extends AbstractAction {

        AboutAction() {
            super(aboutAction);
        }

        AboutAction(String nm) {
            super(nm);
        }

        public void actionPerformed(ActionEvent e) {
            String aboutMessage = JCadUtil.getResources().getString("aboutMessage");
            JOptionPane.showMessageDialog(null, aboutMessage, "About", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    class ExitAction extends AbstractAction {

        ExitAction() {
            super(exitAction);
        }

        public void actionPerformed(ActionEvent e) {
            String exitMessage = JCadUtil.getResources().getString("exitMessage");
            if (JOptionPane.showConfirmDialog(null, exitMessage, "Exit", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) System.exit(0);
        }
    }
}
