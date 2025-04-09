package edu.ucsd.ncmir.jinx.gui.object_properties;

import edu.ucsd.ncmir.asynchronous_event.AbstractAsynchronousEventListener;
import edu.ucsd.ncmir.asynchronous_event.AsynchronousEvent;
import edu.ucsd.ncmir.jinx.core.JxFixMode;
import edu.ucsd.ncmir.jinx.core.JxHandlerStatus;
import edu.ucsd.ncmir.jinx.core.JxPreferences;
import edu.ucsd.ncmir.jinx.core.JxTraceDisplayMode;
import edu.ucsd.ncmir.jinx.core.JxTraceType;
import edu.ucsd.ncmir.jinx.events.JxConfirmEvent;
import edu.ucsd.ncmir.jinx.events.JxErrorEvent;
import edu.ucsd.ncmir.jinx.events.JxGetObjectSegmentationOwnerEvent;
import edu.ucsd.ncmir.jinx.events.JxPrintEvent;
import edu.ucsd.ncmir.jinx.events.JxSaveTextEvent;
import edu.ucsd.ncmir.jinx.events.JxSegmentationUpdateEvent;
import edu.ucsd.ncmir.jinx.events.JxSendTextEvent;
import edu.ucsd.ncmir.jinx.events.gui.JxUpdateDisplayEvent;
import edu.ucsd.ncmir.jinx.exception.JxGeneralErrorException;
import edu.ucsd.ncmir.jinx.gui.color_picker.JxColorPicker;
import edu.ucsd.ncmir.jinx.gui.object_browser.JxObjectBrowser;
import edu.ucsd.ncmir.jinx.gui.workspace.JxOntologyChooser;
import edu.ucsd.ncmir.jinx.interfaces.JxObjectHandlerInterface;
import edu.ucsd.ncmir.jinx.objects.JxObject;
import edu.ucsd.ncmir.jinx.objects.JxObjectQuality;
import edu.ucsd.ncmir.jinx.objects.JxObjectTreeNode;
import edu.ucsd.ncmir.jinx.segmentation.JxSegmentation;
import edu.ucsd.ncmir.ontology.Ontology;
import edu.ucsd.ncmir.ontology.OntologyNode;
import edu.ucsd.ncmir.ontology.browser.OntologyBrowserPanel;
import edu.ucsd.ncmir.ontology.browser.OntologyChooserComboBox;
import edu.ucsd.ncmir.ontology.browser.OntologyElement;
import edu.ucsd.ncmir.ontology.browser.OntologyGroup;
import edu.ucsd.ncmir.ontology.browser.OntologyListener;
import edu.ucsd.ncmir.spl.core.NamedThread;
import edu.ucsd.ncmir.spl.gui.DialogDestroyer;
import edu.ucsd.ncmir.spl.gui.StatusDialog;
import edu.ucsd.ncmir.spl.gui.WindowLauncher;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author  spl
 */
public final class JxObjectPropertiesDialog extends JDialog {

    private static final long serialVersionUID = 42L;

    private static final String NOTHING = " Nothing ";

    private Exception _exception = null;

    /**
     * 
     * @param container
     * @param obj
     * @param segmentation
     * @param acceptor
     * @return a dialog
     */
    public static JxObjectPropertiesDialog create(Container container, Object obj, JxSegmentation segmentation, JxPreferences preferences, JxAcceptorInterface acc) throws JxGeneralErrorException {
        while ((!(container instanceof Frame)) && (!(container instanceof Dialog)) && (container != null)) container = container.getParent();
        if (container == null) throw new NullPointerException("Impossible!");
        JxObjectPropertiesDialog dialog = null;
        if ((obj == null) || (obj instanceof JxObject)) {
            JxObject object = (JxObject) obj;
            if (container instanceof Frame) dialog = new JxObjectPropertiesDialog((Frame) container, object, segmentation, preferences, acc); else if (container instanceof Dialog) dialog = new JxObjectPropertiesDialog((Dialog) container, object, segmentation, preferences, acc);
        } else if (obj instanceof JxObjectTreeNode) {
            JxObjectTreeNode node = (JxObjectTreeNode) obj;
            if (container instanceof Frame) dialog = new JxObjectPropertiesDialog((Frame) container, node, segmentation, preferences, acc); else if (container instanceof Dialog) dialog = new JxObjectPropertiesDialog((Dialog) container, node, segmentation, preferences, acc);
        }
        return dialog;
    }

    /**
     * Creates new form JxObjectPropertiesDialog
     */
    private JxObjectPropertiesDialog(Frame window, JxObjectTreeNode node, JxSegmentation segmentation, JxPreferences preferences, JxAcceptorInterface acceptor) throws JxGeneralErrorException {
        super(window, true);
        this.init((Window) window, node, null, segmentation, preferences, acceptor);
    }

    private JxObjectPropertiesDialog(Dialog window, JxObjectTreeNode node, JxSegmentation segmentation, JxPreferences preferences, JxAcceptorInterface acceptor) throws JxGeneralErrorException {
        super(window, true);
        this.init((Window) window, node, null, segmentation, preferences, acceptor);
    }

    private JxObjectPropertiesDialog(Frame window, JxObject parent, JxSegmentation segmentation, JxPreferences preferences, JxAcceptorInterface acceptor) throws JxGeneralErrorException {
        super(window, true);
        this.init((Window) window, null, parent, segmentation, preferences, acceptor);
    }

    private JxObjectPropertiesDialog(Dialog window, JxObject parent, JxSegmentation segmentation, JxPreferences preferences, JxAcceptorInterface acceptor) throws JxGeneralErrorException {
        super(window, true);
        this.init((Window) window, null, parent, segmentation, preferences, acceptor);
    }

    private JxObjectTreeNode _node;

    private JxSegmentation _segmentation;

    private JxPreferences _preferences;

    private JxAcceptorInterface _acceptor;

    private SpinnerNumberModel _diameter_model;

    private String _parent_name = JxObjectPropertiesDialog.NOTHING;

    private OntologyGroup _ontologies;

    private Ontology _ontology;

    private OntologyBrowserPanel _obp;

    private QualityBox[] _quality_list;

    private void init(Window parent, JxObjectTreeNode node, JxObject parent_object, JxSegmentation segmentation, JxPreferences preferences, JxAcceptorInterface acceptor) throws JxGeneralErrorException {
        this._node = node;
        this._segmentation = segmentation;
        this._preferences = preferences;
        this._acceptor = acceptor;
        this._ontologies = preferences.getOntologies();
        OntologyElement element = this._ontologies.getCurrent();
        PleaseWait please_wait = new PleaseWait();
        Timer t = new Timer("Please Wait");
        t.schedule(please_wait, 3000);
        this._ontology = element.getOntology();
        t.cancel();
        please_wait.hide();
        while (this._ontology == null) {
            JxConfirmEvent ce = new JxConfirmEvent();
            ce.setHeader("Ontology Loader");
            ce.setMessage("<center>" + "Ontology<br>" + element.getName() + "<br>" + " not found in local cache.<br>" + "Search for it?" + "</center>");
            ce.sendWait();
            if (ce.isYes()) {
                JxOntologyChooser chooser = new JxOntologyChooser(this._ontologies);
                chooser.setLocationRelativeTo(parent);
                chooser.setVisible(true);
                element = chooser.getOntology();
                if (element != null) this._ontology = element.getOntology();
            } else throw new JxGeneralErrorException("Unable to initialize " + "Object Properties");
        }
        this.initComponents();
        this._initializing = false;
        this.loadQualityList();
        this._obp.addOntologyListener(new OntologyListenerTask(this));
        if (this._node != null) new ParameterHandler(this, this._node.getUserObject());
        this._diameter_model = new SpinnerNumberModel(0.01, 0.01, 5000.0, 0.01);
        this.diameter.setModel(this._diameter_model);
        this.max_points.setValue(200);
        this.setColor(this.pick_color.getBackground());
        this.suggested_classes.setVisible(false);
        if (node != null) {
            JxObjectTreeNode object_parent = (JxObjectTreeNode) node.getParent();
            if (object_parent != null) this._parent_name = object_parent.toString();
            this.setOntologyNode(node.getUserObject().getOntologyNode());
            this._parent = object_parent;
        }
        if (parent_object != null) {
            this.loadSuggestedClasses(parent_object);
            this._parent_name = parent_object.getName();
            this._parent = segmentation.findObjectTreeNode(parent_object);
        }
        this.part_of.setText(this._parent_name);
        new WindowLauncher(this);
    }

    private class PleaseWait extends TimerTask {

        private StatusDialog _status = null;

        public void run() {
            this._status = new StatusDialog("Loading Ontology");
            this._status.updateText("Please Wait. . .");
            this._status.setCancelHandler(null);
        }

        void hide() {
            if (this._status != null) this._status.close();
        }
    }

    private QualityBox[] loadQualities(JxObject object) {
        OntologyNode quality = this._ontology.getNodeByInternalName("snap:quality");
        HashSet<QualityBox> quality_list = new HashSet<QualityBox>();
        if (quality != null) this.recurseQualities(quality, quality_list);
        QualityBox[] quality_array = quality_list.toArray(new QualityBox[quality_list.size()]);
        Arrays.sort(quality_array);
        if (object != null) {
            JxObjectQuality[] object_qualities = object.getQualities();
            if (object_qualities != null) {
                for (JxObjectQuality oq : object_qualities) {
                    int i0 = 0;
                    int i1 = quality_array.length;
                    int n = quality_array.length;
                    do {
                        int i = (i0 + i1) / 2;
                        QualityBox qb = quality_array[i];
                        int diff = oq.getQuality().compareTo(qb.getOntologyNode());
                        if (diff == 0) {
                            qb.setSelected(true);
                            qb.setText(oq.getValue());
                            break;
                        } else if (diff < 0) i1 = i; else i0 = i;
                    } while ((n >>= 1) > 0);
                }
            }
        }
        return quality_array;
    }

    private class QualityBox extends JPanel implements Comparable<QualityBox> {

        private static final long serialVersionUID = 42L;

        private OntologyNode _ontology_node;

        private JCheckBox _box;

        private JTextField _value;

        QualityBox(OntologyNode ontology_node) {
            super();
            GridBagLayout gbl = new GridBagLayout();
            this.setLayout(gbl);
            this._box = new JCheckBox(ontology_node.toString());
            this._value = new JTextField();
            GridBagConstraints c;
            c = new GridBagConstraints();
            c.gridwidth = GridBagConstraints.RELATIVE;
            c.fill = GridBagConstraints.HORIZONTAL;
            add(this._box, c);
            this._value.setColumns(5);
            c = new GridBagConstraints();
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            add(this._value, c);
            this._value.setEnabled(false);
            this._box.addActionListener(new ButtonListener(this._value));
            this._ontology_node = ontology_node;
        }

        private class ButtonListener implements ActionListener {

            JTextField _text;

            ButtonListener(JTextField text) {
                this._text = text;
            }

            public void actionPerformed(ActionEvent ae) {
                JCheckBox cb = (JCheckBox) ae.getSource();
                this._text.setEnabled(cb.isSelected());
            }
        }

        OntologyNode getOntologyNode() {
            return this._ontology_node;
        }

        @Override
        public int compareTo(QualityBox qb) {
            return this._ontology_node.compareTo(qb._ontology_node);
        }

        @Override
        public int hashCode() {
            return this._ontology_node.toString().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            boolean result = false;
            if ((obj != null) && (obj instanceof QualityBox)) result = this.hashCode() == obj.hashCode();
            return result;
        }

        boolean isSelected() {
            return this._box.isSelected();
        }

        void setSelected(boolean b) {
            this._box.setSelected(b);
            this._value.setEnabled(b);
        }

        void setText(String text) {
            this._value.setText(text);
        }

        String getText() {
            return this._value.getText();
        }
    }

    private OntologyNode _ontology_node;

    private void recurseQualities(OntologyNode quality, HashSet<QualityBox> qualities) {
        if (quality.isLeaf()) qualities.add(new QualityBox(quality)); else for (OntologyNode q : quality.getChildren()) this.recurseQualities(q, qualities);
    }

    private void setOntologyNode(OntologyNode ontology_node) {
        this._ontology_node = ontology_node;
    }

    private OntologyNode getOntologyNode() {
        return this._ontology_node;
    }

    protected void setObjectName(String name) {
        this.object_name.setText(name);
    }

    protected String getObjectName() {
        return this.object_name.getText();
    }

    protected void setObjectInformation(JxObject object, String name) {
        new ParameterHandler(this, object, name);
    }

    protected void setObjectInformation(JxObject object) {
        new ParameterHandler(this, object);
    }

    private class ParameterHandler implements Runnable {

        private JxObjectPropertiesDialog _opd;

        private JxObject _object;

        private String _name;

        private ParameterHandler(JxObjectPropertiesDialog opd, JxObject object) {
            if (object != null) {
                this._opd = opd;
                this._object = object;
                this._name = object.getName();
                EventQueue.invokeLater(this);
            }
        }

        private ParameterHandler(JxObjectPropertiesDialog _opd, JxObject object, String name) {
            if (object != null) {
                this._opd = _opd;
                this._object = object;
                this._name = name;
                EventQueue.invokeLater(this);
            }
        }

        public void run() {
            this._opd.object_name.setText(this._name);
            this._opd.description.setText(this._object.getDescription());
            this._opd.setColor(this._object.getColor());
            int max_points = this._object.getMaxPoints();
            this._opd.setMaxPoints(max_points);
            int setval = (int) (Math.log10((double) max_points) * 100.0);
            this._opd.max_points.setValue(setval);
            this._opd.setAggregate(this._object.isAggregate());
            this._opd.setFixMode(this._object.getFixMode());
            this._opd.setTraceType(this._object.getTraceType());
            this._opd.setTraceDisplayMode(this._object.getTraceDisplayMode());
            this._opd.setSegmented(this._object.isSegmented());
            this._opd._diameter_model.setValue(this._object.getDiameter());
            JxGetObjectSegmentationOwnerEvent gosoe = new JxGetObjectSegmentationOwnerEvent();
            gosoe.sendWait(this._object);
            JxObjectTreeNode node = gosoe.getSegmentation().findObjectTreeNode(this._object);
            JxObjectTreeNode parent = (JxObjectTreeNode) node.getParent();
            if (parent != null) this._opd.setPartOf(parent.getUserObject()); else this._opd.setPartOf(null);
        }
    }

    private Color _color;

    private void setColor(Color color) {
        this._color = color;
        this.pick_color.setBackground(color);
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        int rgb = Color.HSBtoRGB(hsb[0], hsb[1], (hsb[2] + 0.5f) % 1.0f);
        this.pick_color.setForeground(new Color(rgb));
    }

    private Color getColor() {
        return this._color;
    }

    private ArrayList<JxObject> _relations = new ArrayList<JxObject>();

    private ArrayList<JxObject> _contacts = new ArrayList<JxObject>();

    private JxObjectTreeNode _parent = null;

    private void setParentObjectNode(JxObjectTreeNode parent) {
        this._parent = parent;
    }

    private void setNullParentObjectNode() {
        this.setParentObjectNode((JxObjectTreeNode) null);
    }

    private void setParentObjectNode(JxObject parent_object) {
        JxObjectTreeNode object_parent = this._segmentation.findObjectTreeNode(parent_object);
        this.setParentObjectNode(object_parent);
    }

    private JxObjectTreeNode getParentObjectNode() {
        return this._parent;
    }

    private void initComponents() {
        trace_type_group = new javax.swing.ButtonGroup();
        ghost_trace_group = new javax.swing.ButtonGroup();
        self_intersection_check_group = new javax.swing.ButtonGroup();
        view_group = new javax.swing.ButtonGroup();
        tabs = new javax.swing.JTabbedPane();
        ontology_panel = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        selector = new OntologyChooserComboBox(this._ontologies);
        this.selector.setSelectedItem(this._ontologies.getCurrent());
        try {
            ontology_browser = new OntologyBrowserPanel(this._ontology);
            this._obp = (OntologyBrowserPanel) ontology_browser;
        } catch (InterruptedException ie) {
            this._exception = ie;
            ie.printStackTrace();
        }
        jPanel21 = new javax.swing.JPanel();
        jPanel22 = new javax.swing.JPanel();
        jPanel23 = new javax.swing.JPanel();
        relationship_panel = new javax.swing.JPanel();
        jPanel28 = new javax.swing.JPanel();
        jPanel29 = new javax.swing.JPanel();
        jPanel30 = new javax.swing.JPanel();
        jPanel31 = new javax.swing.JPanel();
        jPanel26 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        part_of = new javax.swing.JLabel();
        jPanel35 = new javax.swing.JPanel();
        suggested_classes = new javax.swing.JComboBox();
        jPanel19 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jPanel18 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jPanel17 = new javax.swing.JPanel();
        relatedToButton = new javax.swing.JButton();
        contactsButton = new javax.swing.JButton();
        jPanel12 = new javax.swing.JPanel();
        topLevelButton = new JButton("Top Level");
        partOfButton = new javax.swing.JButton();
        jPanel15 = new javax.swing.JPanel();
        aggregate = new javax.swing.JCheckBox();
        copyFromButton = new javax.swing.JButton();
        jPanel36 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        description = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        qualities_list = new javax.swing.JPanel();
        property_panel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel33 = new javax.swing.JPanel();
        object_properties_panel = new javax.swing.JPanel();
        jPanel37 = new javax.swing.JPanel();
        jPanel38 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        object_info = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        jPanel39 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel24 = new javax.swing.JPanel();
        closed = new javax.swing.JRadioButton();
        open = new javax.swing.JRadioButton();
        point_thread = new javax.swing.JRadioButton();
        thread = new javax.swing.JRadioButton();
        filament = new javax.swing.JRadioButton();
        jPanel5 = new javax.swing.JPanel();
        diameter = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jPanel32 = new javax.swing.JPanel();
        random_color = new javax.swing.JButton();
        pick_color = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jPanel34 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        ghost_off = new javax.swing.JCheckBox();
        ghost_previous = new javax.swing.JCheckBox();
        ghost_following = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        auto_fix = new javax.swing.JCheckBox();
        warn_me = new javax.swing.JCheckBox();
        ignore = new javax.swing.JCheckBox();
        max_points_panel = new javax.swing.JPanel();
        max_points_readout = new javax.swing.JFormattedTextField();
        max_points = new javax.swing.JSlider();
        jPanel10 = new javax.swing.JPanel();
        object_name = new javax.swing.JTextField();
        accept = new javax.swing.JButton();
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Jinx Object Properties");
        setAlwaysOnTop(true);
        addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }

            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        tabs.setToolTipText("Object Relationships");
        tabs.addChangeListener(new javax.swing.event.ChangeListener() {

            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabsStateChanged(evt);
            }
        });
        ontology_panel.setLayout(new java.awt.BorderLayout());
        jPanel8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        jLabel2.setText("Select Ontology. . .");
        jPanel8.add(jLabel2);
        selector.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectorActionPerformed(evt);
            }
        });
        jPanel8.add(selector);
        ontology_panel.add(jPanel8, java.awt.BorderLayout.NORTH);
        ontology_panel.add(ontology_browser, java.awt.BorderLayout.CENTER);
        jPanel21.setMaximumSize(new java.awt.Dimension(5, 5));
        jPanel21.setMinimumSize(new java.awt.Dimension(5, 5));
        org.jdesktop.layout.GroupLayout jPanel21Layout = new org.jdesktop.layout.GroupLayout(jPanel21);
        jPanel21.setLayout(jPanel21Layout);
        jPanel21Layout.setHorizontalGroup(jPanel21Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 635, Short.MAX_VALUE));
        jPanel21Layout.setVerticalGroup(jPanel21Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 5, Short.MAX_VALUE));
        ontology_panel.add(jPanel21, java.awt.BorderLayout.SOUTH);
        jPanel22.setMaximumSize(new java.awt.Dimension(5, 5));
        jPanel22.setMinimumSize(new java.awt.Dimension(5, 5));
        jPanel22.setPreferredSize(new java.awt.Dimension(5, 100));
        org.jdesktop.layout.GroupLayout jPanel22Layout = new org.jdesktop.layout.GroupLayout(jPanel22);
        jPanel22.setLayout(jPanel22Layout);
        jPanel22Layout.setHorizontalGroup(jPanel22Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 5, Short.MAX_VALUE));
        jPanel22Layout.setVerticalGroup(jPanel22Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 564, Short.MAX_VALUE));
        ontology_panel.add(jPanel22, java.awt.BorderLayout.WEST);
        jPanel23.setMaximumSize(new java.awt.Dimension(5, 0));
        jPanel23.setMinimumSize(new java.awt.Dimension(5, 0));
        jPanel23.setPreferredSize(new java.awt.Dimension(5, 100));
        org.jdesktop.layout.GroupLayout jPanel23Layout = new org.jdesktop.layout.GroupLayout(jPanel23);
        jPanel23.setLayout(jPanel23Layout);
        jPanel23Layout.setHorizontalGroup(jPanel23Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 5, Short.MAX_VALUE));
        jPanel23Layout.setVerticalGroup(jPanel23Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 564, Short.MAX_VALUE));
        ontology_panel.add(jPanel23, java.awt.BorderLayout.EAST);
        tabs.addTab("Ontology", null, ontology_panel, "Ontology");
        relationship_panel.setLayout(new java.awt.BorderLayout());
        jPanel28.setMaximumSize(new java.awt.Dimension(5, 5));
        jPanel28.setMinimumSize(new java.awt.Dimension(5, 5));
        org.jdesktop.layout.GroupLayout jPanel28Layout = new org.jdesktop.layout.GroupLayout(jPanel28);
        jPanel28.setLayout(jPanel28Layout);
        jPanel28Layout.setHorizontalGroup(jPanel28Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 635, Short.MAX_VALUE));
        jPanel28Layout.setVerticalGroup(jPanel28Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 5, Short.MAX_VALUE));
        relationship_panel.add(jPanel28, java.awt.BorderLayout.NORTH);
        jPanel29.setMaximumSize(new java.awt.Dimension(5, 5));
        jPanel29.setMinimumSize(new java.awt.Dimension(5, 5));
        org.jdesktop.layout.GroupLayout jPanel29Layout = new org.jdesktop.layout.GroupLayout(jPanel29);
        jPanel29.setLayout(jPanel29Layout);
        jPanel29Layout.setHorizontalGroup(jPanel29Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 635, Short.MAX_VALUE));
        jPanel29Layout.setVerticalGroup(jPanel29Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 5, Short.MAX_VALUE));
        relationship_panel.add(jPanel29, java.awt.BorderLayout.SOUTH);
        jPanel30.setMaximumSize(new java.awt.Dimension(5, 5));
        jPanel30.setMinimumSize(new java.awt.Dimension(5, 5));
        jPanel30.setPreferredSize(new java.awt.Dimension(5, 100));
        org.jdesktop.layout.GroupLayout jPanel30Layout = new org.jdesktop.layout.GroupLayout(jPanel30);
        jPanel30.setLayout(jPanel30Layout);
        jPanel30Layout.setHorizontalGroup(jPanel30Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 5, Short.MAX_VALUE));
        jPanel30Layout.setVerticalGroup(jPanel30Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 593, Short.MAX_VALUE));
        relationship_panel.add(jPanel30, java.awt.BorderLayout.WEST);
        jPanel31.setMaximumSize(new java.awt.Dimension(5, 0));
        jPanel31.setMinimumSize(new java.awt.Dimension(5, 0));
        jPanel31.setPreferredSize(new java.awt.Dimension(5, 100));
        org.jdesktop.layout.GroupLayout jPanel31Layout = new org.jdesktop.layout.GroupLayout(jPanel31);
        jPanel31.setLayout(jPanel31Layout);
        jPanel31Layout.setHorizontalGroup(jPanel31Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 5, Short.MAX_VALUE));
        jPanel31Layout.setVerticalGroup(jPanel31Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 593, Short.MAX_VALUE));
        relationship_panel.add(jPanel31, java.awt.BorderLayout.EAST);
        jPanel26.setLayout(new java.awt.BorderLayout());
        jPanel13.setLayout(new java.awt.BorderLayout());
        jPanel14.setLayout(new java.awt.GridLayout(1, 2, 5, 0));
        part_of.setFont(new java.awt.Font("Dialog", 0, 12));
        part_of.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        part_of.setText("Some_Random_Thing.0001");
        part_of.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Part of...", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 11), new java.awt.Color(153, 153, 255)));
        jPanel14.add(part_of);
        jPanel35.setLayout(new java.awt.GridLayout(1, 1));
        suggested_classes.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        suggested_classes.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 255)), "Object Parts", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 11), new java.awt.Color(153, 153, 255)));
        suggested_classes.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                suggested_classesActionPerformed(evt);
            }
        });
        jPanel35.add(suggested_classes);
        jPanel14.add(jPanel35);
        jPanel13.add(jPanel14, java.awt.BorderLayout.CENTER);
        jPanel19.setLayout(new java.awt.GridLayout(2, 1, 0, 5));
        jPanel11.setLayout(new java.awt.GridLayout(1, 4, 5, 0));
        org.jdesktop.layout.GroupLayout jPanel18Layout = new org.jdesktop.layout.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(jPanel18Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 121, Short.MAX_VALUE));
        jPanel18Layout.setVerticalGroup(jPanel18Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 25, Short.MAX_VALUE));
        jPanel11.add(jPanel18);
        org.jdesktop.layout.GroupLayout jPanel16Layout = new org.jdesktop.layout.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(jPanel16Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 121, Short.MAX_VALUE));
        jPanel16Layout.setVerticalGroup(jPanel16Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 25, Short.MAX_VALUE));
        jPanel11.add(jPanel16);
        org.jdesktop.layout.GroupLayout jPanel17Layout = new org.jdesktop.layout.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(jPanel17Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 121, Short.MAX_VALUE));
        jPanel17Layout.setVerticalGroup(jPanel17Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 25, Short.MAX_VALUE));
        jPanel11.add(jPanel17);
        relatedToButton.setText("Related To");
        relatedToButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                relatedToButtonActionPerformed(evt);
            }
        });
        jPanel11.add(relatedToButton);
        contactsButton.setText("Contacts With");
        contactsButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contactsButtonActionPerformed(evt);
            }
        });
        jPanel11.add(contactsButton);
        jPanel19.add(jPanel11);
        jPanel12.setLayout(new java.awt.GridLayout(1, 5, 5, 0));
        topLevelButton.setText("Top Level");
        topLevelButton.setToolTipText("Set object to top level of hierarchy");
        topLevelButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                topLevelButtonActionPerformed(evt);
            }
        });
        jPanel12.add(topLevelButton);
        partOfButton.setText("Part of ...");
        partOfButton.setToolTipText("Select \"Part Of\"");
        partOfButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                partOfButtonActionPerformed(evt);
            }
        });
        jPanel12.add(partOfButton);
        org.jdesktop.layout.GroupLayout jPanel15Layout = new org.jdesktop.layout.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(jPanel15Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 121, Short.MAX_VALUE));
        jPanel15Layout.setVerticalGroup(jPanel15Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 25, Short.MAX_VALUE));
        jPanel12.add(jPanel15);
        aggregate.setText("Aggregate");
        aggregate.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        aggregate.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel12.add(aggregate);
        copyFromButton.setText("Copy From");
        copyFromButton.setToolTipText("Copy from existing object");
        copyFromButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyFromButtonActionPerformed(evt);
            }
        });
        jPanel12.add(copyFromButton);
        jPanel19.add(jPanel12);
        jPanel13.add(jPanel19, java.awt.BorderLayout.PAGE_START);
        jPanel26.add(jPanel13, java.awt.BorderLayout.NORTH);
        jPanel36.setLayout(new java.awt.GridLayout(1, 2));
        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder(""), "Description", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 11), new java.awt.Color(153, 153, 255)));
        description.setColumns(20);
        description.setRows(3);
        jScrollPane1.setViewportView(description);
        jPanel36.add(jScrollPane1);
        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder(""), "Qualities", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 11), new java.awt.Color(153, 153, 255)));
        jScrollPane2.setAutoscrolls(true);
        qualities_list.setAutoscrolls(true);
        qualities_list.setLayout(new java.awt.GridLayout(1, 1));
        jScrollPane2.setViewportView(qualities_list);
        jPanel36.add(jScrollPane2);
        jPanel26.add(jPanel36, java.awt.BorderLayout.CENTER);
        relationship_panel.add(jPanel26, java.awt.BorderLayout.CENTER);
        tabs.addTab("Relationship", null, relationship_panel, "Object Relationships");
        property_panel.setLayout(new java.awt.BorderLayout(5, 5));
        jPanel2.setLayout(new java.awt.BorderLayout(5, 5));
        jPanel33.setLayout(new java.awt.BorderLayout(5, 5));
        object_properties_panel.setLayout(new java.awt.BorderLayout());
        jPanel37.setLayout(new java.awt.BorderLayout(5, 5));
        jPanel38.setLayout(new java.awt.BorderLayout(5, 0));
        jButton1.setText("Print");
        jButton1.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel38.add(jButton1, java.awt.BorderLayout.WEST);
        jButton2.setText("Save");
        jButton2.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel38.add(jButton2, java.awt.BorderLayout.EAST);
        jPanel37.add(jPanel38, java.awt.BorderLayout.NORTH);
        object_info.setColumns(20);
        object_info.setEditable(false);
        object_info.setFont(new java.awt.Font("Courier", 1, 12));
        object_info.setLineWrap(true);
        object_info.setRows(5);
        object_info.setText("Object foo\nbaz\nbar\nwoof\n");
        jScrollPane3.setViewportView(object_info);
        jPanel37.add(jScrollPane3, java.awt.BorderLayout.CENTER);
        object_properties_panel.add(jPanel37, java.awt.BorderLayout.CENTER);
        jPanel33.add(object_properties_panel, java.awt.BorderLayout.CENTER);
        jPanel4.setLayout(new java.awt.BorderLayout());
        jPanel39.setLayout(new java.awt.BorderLayout());
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Trace Type", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 11), new java.awt.Color(153, 153, 255)));
        jPanel3.setLayout(new java.awt.BorderLayout());
        trace_type_group.add(closed);
        closed.setSelected(true);
        closed.setText("Closed");
        closed.setToolTipText("Object created will be closed");
        closed.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closedActionPerformed(evt);
            }
        });
        jPanel24.add(closed);
        trace_type_group.add(open);
        open.setText("Open");
        open.setToolTipText("Object created will be open");
        open.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openActionPerformed(evt);
            }
        });
        jPanel24.add(open);
        trace_type_group.add(point_thread);
        point_thread.setText("Point Thread");
        point_thread.setToolTipText("Object created will be open");
        point_thread.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                point_threadActionPerformed(evt);
            }
        });
        jPanel24.add(point_thread);
        trace_type_group.add(thread);
        thread.setText("Thread");
        thread.setToolTipText("Object created will be open");
        thread.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                threadActionPerformed(evt);
            }
        });
        jPanel24.add(thread);
        trace_type_group.add(filament);
        filament.setText("Filament");
        filament.setToolTipText("Object created will be a filament");
        filament.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filamentActionPerformed(evt);
            }
        });
        jPanel24.add(filament);
        jPanel3.add(jPanel24, java.awt.BorderLayout.NORTH);
        jPanel5.setMaximumSize(new java.awt.Dimension(32767, 20));
        jPanel5.setMinimumSize(new java.awt.Dimension(132, 20));
        jPanel5.setPreferredSize(new java.awt.Dimension(132, 20));
        jPanel5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));
        diameter.setMaximumSize(new java.awt.Dimension(60, 20));
        diameter.setMinimumSize(new java.awt.Dimension(60, 20));
        diameter.setOpaque(false);
        diameter.setPreferredSize(new java.awt.Dimension(60, 20));
        jPanel5.add(diameter);
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setLabelFor(diameter);
        jLabel1.setText("Thread Diameter      \n");
        jPanel5.add(jLabel1);
        jCheckBox1.setText("Allow Intersections");
        jPanel5.add(jCheckBox1);
        jPanel3.add(jPanel5, java.awt.BorderLayout.SOUTH);
        jPanel39.add(jPanel3, java.awt.BorderLayout.CENTER);
        jPanel32.setLayout(new java.awt.BorderLayout());
        random_color.setText("Random Color");
        random_color.setToolTipText("Random Color");
        random_color.setFocusPainted(false);
        random_color.setMargin(new java.awt.Insets(2, 0, 2, 0));
        random_color.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                random_colorActionPerformed(evt);
            }
        });
        jPanel32.add(random_color, java.awt.BorderLayout.SOUTH);
        pick_color.setBackground(new java.awt.Color(204, 204, 204));
        pick_color.setText("Color");
        pick_color.setFocusPainted(false);
        pick_color.setMargin(new java.awt.Insets(2, 0, 2, 0));
        pick_color.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pick_colorActionPerformed(evt);
            }
        });
        jPanel32.add(pick_color, java.awt.BorderLayout.CENTER);
        jPanel39.add(jPanel32, java.awt.BorderLayout.EAST);
        jPanel4.add(jPanel39, java.awt.BorderLayout.SOUTH);
        jPanel9.setLayout(new java.awt.GridLayout(2, 1));
        jPanel34.setLayout(new java.awt.BorderLayout());
        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Ghosting", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 11), new java.awt.Color(153, 153, 255)));
        ghost_trace_group.add(ghost_off);
        ghost_off.setText("Off");
        ghost_off.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ghost_off.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ghost_off.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ghost_offActionPerformed(evt);
            }
        });
        jPanel7.add(ghost_off);
        ghost_trace_group.add(ghost_previous);
        ghost_previous.setSelected(true);
        ghost_previous.setText("Previous");
        ghost_previous.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ghost_previous.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ghost_previous.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ghost_previousActionPerformed(evt);
            }
        });
        jPanel7.add(ghost_previous);
        ghost_trace_group.add(ghost_following);
        ghost_following.setText("Following");
        ghost_following.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ghost_following.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ghost_following.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ghost_followingActionPerformed(evt);
            }
        });
        jPanel7.add(ghost_following);
        jPanel34.add(jPanel7, java.awt.BorderLayout.EAST);
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Self-Intersection", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 11), new java.awt.Color(153, 153, 255)));
        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 5));
        self_intersection_check_group.add(auto_fix);
        auto_fix.setSelected(true);
        auto_fix.setText("Automatic");
        auto_fix.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        auto_fix.setFocusPainted(false);
        auto_fix.setMargin(new java.awt.Insets(0, 0, 0, 0));
        auto_fix.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                auto_fixActionPerformed(evt);
            }
        });
        jPanel1.add(auto_fix);
        self_intersection_check_group.add(warn_me);
        warn_me.setText("Warn Only");
        warn_me.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        warn_me.setFocusPainted(false);
        warn_me.setMargin(new java.awt.Insets(0, 0, 0, 0));
        warn_me.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                warn_meActionPerformed(evt);
            }
        });
        jPanel1.add(warn_me);
        self_intersection_check_group.add(ignore);
        ignore.setText("Ignore");
        ignore.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ignore.setFocusPainted(false);
        ignore.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ignore.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ignoreActionPerformed(evt);
            }
        });
        jPanel1.add(ignore);
        jPanel34.add(jPanel1, java.awt.BorderLayout.CENTER);
        jPanel9.add(jPanel34);
        max_points_panel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Maximum Points Per Trace", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 11), new java.awt.Color(153, 153, 255)));
        max_points_panel.setLayout(new java.awt.BorderLayout(5, 0));
        max_points_readout.setBorder(null);
        max_points_readout.setEditable(false);
        max_points_readout.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        max_points_readout.setText("100");
        max_points_readout.setOpaque(false);
        max_points_readout.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                max_points_readoutActionPerformed(evt);
            }
        });
        max_points_panel.add(max_points_readout, java.awt.BorderLayout.NORTH);
        max_points.setMaximum(500);
        max_points.setMinimum(100);
        max_points.setMinorTickSpacing(50);
        max_points.setPaintTicks(true);
        max_points.setToolTipText("Set Number of Points");
        max_points.addChangeListener(new javax.swing.event.ChangeListener() {

            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                max_pointsStateChanged(evt);
            }
        });
        max_points_panel.add(max_points, java.awt.BorderLayout.CENTER);
        jPanel9.add(max_points_panel);
        jPanel4.add(jPanel9, java.awt.BorderLayout.CENTER);
        jPanel33.add(jPanel4, java.awt.BorderLayout.NORTH);
        jPanel2.add(jPanel33, java.awt.BorderLayout.CENTER);
        property_panel.add(jPanel2, java.awt.BorderLayout.CENTER);
        tabs.addTab("Properties", property_panel);
        getContentPane().add(tabs, java.awt.BorderLayout.CENTER);
        tabs.getAccessibleContext().setAccessibleName("Object Relationships");
        jPanel10.setLayout(new java.awt.BorderLayout());
        jPanel10.add(object_name, java.awt.BorderLayout.CENTER);
        accept.setText("Accept");
        accept.setFocusPainted(false);
        accept.setOpaque(false);
        accept.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                acceptActionPerformed(evt);
            }
        });
        jPanel10.add(accept, java.awt.BorderLayout.EAST);
        getContentPane().add(jPanel10, java.awt.BorderLayout.PAGE_START);
        pack();
    }

    private void partOfButtonActionPerformed(java.awt.event.ActionEvent evt) {
        JxObjectTreeNode object_parent = this.getParentObjectNode();
        if (object_parent != null) new JxObjectBrowser(this, this.getPrunedList(), new PartOfHandler(this), object_parent.getUserObject());
    }

    private void copyFromButtonActionPerformed(java.awt.event.ActionEvent evt) {
        ArrayList<JxObject> objects = new ArrayList<JxObject>();
        new JxObjectBrowser(this, this._segmentation.getObjectList(), new CopyFromHandler(this));
    }

    private void suggested_classesActionPerformed(java.awt.event.ActionEvent evt) {
        Object obj = this.suggested_classes.getSelectedItem();
        if (obj instanceof OntologyNode) this.updateNode((OntologyNode) obj);
    }

    private void relatedToButtonActionPerformed(java.awt.event.ActionEvent evt) {
        new JxObjectBrowser(this, this.getPrunedList(), new RelationsHandler(this._relations), this._relations);
    }

    private void contactsButtonActionPerformed(java.awt.event.ActionEvent evt) {
        new JxObjectBrowser(this, this.getPrunedList(), new ContactsHandler(this._contacts), this._contacts);
    }

    private void pick_colorActionPerformed(java.awt.event.ActionEvent evt) {
        new JxColorPickedEventListener(this).enable();
        JxColorPicker color_picker = new JxColorPicker(this, this._color);
    }

    private void ghost_offActionPerformed(java.awt.event.ActionEvent evt) {
        this._trace_display_mode = JxTraceDisplayMode.OFF;
    }

    private void ghost_previousActionPerformed(java.awt.event.ActionEvent evt) {
        this._trace_display_mode = JxTraceDisplayMode.PREVIOUS;
    }

    private void ghost_followingActionPerformed(java.awt.event.ActionEvent evt) {
        this._trace_display_mode = JxTraceDisplayMode.FOLLOWING;
    }

    private void closedActionPerformed(java.awt.event.ActionEvent evt) {
        this._trace_type = JxTraceType.CLOSED;
    }

    private void openActionPerformed(java.awt.event.ActionEvent evt) {
        this._trace_type = JxTraceType.OPEN;
    }

    private void threadActionPerformed(java.awt.event.ActionEvent evt) {
        this._trace_type = JxTraceType.THREAD;
    }

    private void point_threadActionPerformed(java.awt.event.ActionEvent evt) {
        this._trace_type = JxTraceType.POINT_THREAD;
    }

    private void filamentActionPerformed(java.awt.event.ActionEvent evt) {
        this._trace_type = JxTraceType.FILAMENT;
    }

    private void auto_fixActionPerformed(java.awt.event.ActionEvent evt) {
        this._fix_mode = JxFixMode.AUTO;
    }

    private void warn_meActionPerformed(java.awt.event.ActionEvent evt) {
        this._fix_mode = JxFixMode.WARN;
    }

    private void ignoreActionPerformed(java.awt.event.ActionEvent evt) {
        this._fix_mode = JxFixMode.NONE;
    }

    private void max_points_readoutActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void max_pointsStateChanged(javax.swing.event.ChangeEvent evt) {
        double log = this.max_points.getValue() / 100.0;
        double log_floor = Math.floor(log);
        double significance = Math.floor(Math.pow(10.0, log - log_floor) + 0.5);
        this.setMaxPoints((int) (significance * Math.pow(10.0, log_floor)));
    }

    private void random_colorActionPerformed(java.awt.event.ActionEvent evt) {
        this.setColor(new Color((float) Math.random(), (float) Math.random(), (float) Math.random()));
    }

    private class PrunedTreeLister implements JxObjectHandlerInterface {

        private ArrayList<JxObject> _list = new ArrayList<JxObject>();

        private JxObjectTreeNode _target;

        PrunedTreeLister(JxObjectTreeNode target) {
            this._target = target;
        }

        public JxHandlerStatus handler(JxObjectTreeNode node) {
            JxHandlerStatus status = JxHandlerStatus.OK;
            if ((this._target != null) && node.equals(this._target)) status = JxHandlerStatus.PRUNE; else _list.add(node.getUserObject());
            return status;
        }

        public void push() {
        }

        public void pop() {
        }

        JxObject[] getList() {
            return _list.toArray(new JxObject[_list.size()]);
        }
    }

    private JxObject[] getPrunedList() {
        JxObjectTreeNode root = this._segmentation.getObjectTree();
        PrunedTreeLister ptl = new PrunedTreeLister(this._node);
        if (root != null) root.treeWalker(ptl);
        return ptl.getList();
    }

    private void formWindowClosing(java.awt.event.WindowEvent evt) {
        this.delete();
    }

    private void setAggregate(boolean aggregate) {
        this.aggregate.setSelected(aggregate);
    }

    private JxFixMode _fix_mode = JxFixMode.AUTO;

    private void setFixMode(JxFixMode fix_mode) {
        switch(fix_mode) {
            case AUTO:
                {
                    this.auto_fix.setSelected(true);
                    break;
                }
            case WARN:
                {
                    this.warn_me.setSelected(true);
                    break;
                }
            case NONE:
                {
                    this.ignore.setSelected(true);
                    break;
                }
        }
        this._fix_mode = fix_mode;
    }

    private JxFixMode getFixMode() {
        return this._fix_mode;
    }

    private JxTraceType _trace_type = JxTraceType.CLOSED;

    private void setTraceType(JxTraceType trace_type) {
        switch(trace_type) {
            case CLOSED:
                {
                    this.closed.setSelected(true);
                    break;
                }
            case OPEN:
                {
                    this.open.setSelected(true);
                    break;
                }
            case THREAD:
                {
                    this.thread.setSelected(true);
                    break;
                }
            case FILAMENT:
                {
                    this.filament.setSelected(true);
                    break;
                }
            case POINT_THREAD:
                {
                    this.point_thread.setSelected(true);
                    break;
                }
        }
        this.closed.setEnabled(false);
        this.open.setEnabled(false);
        this.thread.setEnabled(false);
        this.filament.setEnabled(false);
        this.point_thread.setEnabled(false);
        this._trace_type = trace_type;
    }

    private JxTraceType getTraceType() {
        return this._trace_type;
    }

    private JxTraceDisplayMode _trace_display_mode = JxTraceDisplayMode.PREVIOUS;

    private void setTraceDisplayMode(JxTraceDisplayMode trace_display_mode) {
        switch(trace_display_mode) {
            case OFF:
                {
                    this.ghost_off.setSelected(true);
                    break;
                }
            case PREVIOUS:
                {
                    this.ghost_previous.setSelected(true);
                    break;
                }
            case FOLLOWING:
                {
                    this.ghost_following.setSelected(true);
                    break;
                }
        }
        this._trace_display_mode = trace_display_mode;
    }

    private JxTraceDisplayMode getTraceDisplayMode() {
        return this._trace_display_mode;
    }

    private boolean _is_segmented = false;

    private void setSegmented(boolean is_segmented) {
        this._is_segmented = is_segmented;
    }

    private boolean isSegmented() {
        return this._is_segmented;
    }

    private int _points;

    private void setMaxPoints(int points) {
        this._points = points;
        this.max_points_readout.setValue("" + points);
    }

    private int getMaxPoints() {
        return this._points;
    }

    private class OntologyListenerTask implements OntologyListener {

        private JxObjectPropertiesDialog _dialog;

        OntologyListenerTask(JxObjectPropertiesDialog dialog) {
            this._dialog = dialog;
        }

        public void nodeSelected(OntologyNode ontology_node) {
            if (ontology_node != null) this._dialog.updateNode(ontology_node);
        }
    }

    private void updateNode(OntologyNode ontology_node) {
        this.setOntologyNode(ontology_node);
        String name = this._segmentation.createObjectName(ontology_node);
        new UpdateName(this.object_name, name.replaceAll(" ", "_"));
    }

    private class UpdateName implements Runnable {

        private JTextField _object_name;

        private String _name;

        UpdateName(JTextField object_name, String name) {
            this._object_name = object_name;
            this._name = name;
            EventQueue.invokeLater(this);
        }

        public void run() {
            this._object_name.setText(_name);
        }
    }

    private class JxColorPickedEventListener extends AbstractAsynchronousEventListener {

        private JxObjectPropertiesDialog _object_properties;

        JxColorPickedEventListener(JxObjectPropertiesDialog object_properties) {
            this._object_properties = object_properties;
        }

        public void handler(AsynchronousEvent event, Object object) {
            if (object != null) this._object_properties.setColor((Color) object);
            this.disable();
        }
    }

    private void acceptActionPerformed(java.awt.event.ActionEvent event) {
        new AcceptThread(this);
    }

    private class AcceptThread extends NamedThread {

        private JxObjectPropertiesDialog _dialog;

        AcceptThread(JxObjectPropertiesDialog dialog) {
            super("Accept");
            this._dialog = dialog;
            this.start();
        }

        @Override
        public void run() {
            this._dialog.acceptor();
        }
    }

    private void acceptor() {
        String name = this.object_name.getText();
        if (name.length() > 0) {
            if (Pattern.matches("^[-A-Za-z0-9_.]+$", name)) {
                JxObjectTreeNode tree_node = this._acceptor.accept(name);
                if (tree_node != null) {
                    JxObjectTreeNode parent_node = this.getParentObjectNode();
                    JxObject object = tree_node.getUserObject();
                    if (parent_node == null) this._segmentation.addToRoot(tree_node); else tree_node.reparent(parent_node);
                    for (JxObject contact : this._contacts) {
                        object.addContact(contact);
                        contact.addContact(object);
                    }
                    for (JxObject related : this._relations) {
                        object.addRelation(related);
                        related.addRelation(object);
                    }
                    object.setColor(this.getColor());
                    object.setDescription(this.description.getText());
                    ArrayList<JxObjectQuality> on = new ArrayList<JxObjectQuality>();
                    for (QualityBox qb : this._quality_list) if (qb.isSelected()) on.add(new JxObjectQuality(qb.getOntologyNode(), qb.getText()));
                    JxObjectQuality[] list = on.toArray(new JxObjectQuality[on.size()]);
                    object.setQualities(list);
                    object.setOntologyIdentifier(this._ontologies.getCurrent().toString());
                    object.setAggregate(this.aggregate.isSelected());
                    object.setFixMode(this.getFixMode());
                    object.setMaxPoints(this.getMaxPoints());
                    object.setName(object_name.getText());
                    object.setOntologyNode(this.getOntologyNode());
                    object.setTraceDisplayMode(this.getTraceDisplayMode());
                    object.setSegmented(this.isSegmented());
                    object.setTraceType(this.getTraceType());
                    object.setDiameter(this._diameter_model.getNumber().doubleValue());
                    object.createUniqueID();
                    new JxObjectUpdatedEvent(_segmentation).send(object);
                    new JxSegmentationUpdateEvent(_segmentation).send();
                    _segmentation.acceptObjectName(object.getName());
                    this.delete();
                } else new DuplicatedNameHandler(this).start();
            } else new JxErrorEvent().sendWait("Name contains invalid characters.");
        } else new JxErrorEvent().sendWait("Name missing.");
    }

    private class DuplicatedNameHandler extends NamedThread {

        private JxObjectPropertiesDialog dialog;

        DuplicatedNameHandler(JxObjectPropertiesDialog dialog) {
            super("Duplicated Name Handler");
            this.dialog = dialog;
        }

        @Override
        public void run() {
            this.dialog.handleDuplicateName();
        }
    }

    private void handleDuplicateName() {
        String name = this.object_name.getText();
        boolean fixed = false;
        if (name.matches(".*[0-9]+$")) {
            String incremented_name = this.getIncrementedName(name);
            if (incremented_name != null) {
                String new_name = JOptionPane.showInputDialog(this, "Name duplicated. " + "Use this one?", incremented_name);
                if (new_name != null) {
                    new UpdateObjectName(this, new_name);
                    fixed = true;
                }
            }
        }
        if (!fixed) new JxErrorEvent().sendWait("Duplicated name.");
    }

    private String getIncrementedName(String name) {
        String head = name.replaceAll("[0-9]+$", "");
        String format = "%0" + (name.length() - head.length()) + "d";
        String template = head + "[0-9]+$";
        int maxhead = -1;
        for (JxObject o : this._segmentation.getObjectList()) {
            String oname = o.getName();
            if (oname.matches(template)) {
                String[] numbers = oname.replaceAll("[^0-9]", " ").split(" ");
                int n = new Integer(numbers[numbers.length - 1]).intValue();
                if (n > maxhead) maxhead = n;
            }
        }
        if (maxhead != -1) name = head + String.format(format, ++maxhead); else name = null;
        return name;
    }

    private class UpdateObjectName extends NamedThread {

        private JxObjectPropertiesDialog dialog;

        private String name;

        UpdateObjectName(JxObjectPropertiesDialog dialog, String name) {
            super("Update Object Name");
            this.dialog = dialog;
            this.name = name;
            EventQueue.invokeLater(this);
        }

        @Override
        public void run() {
            this.dialog.object_name.setText(name);
        }
    }

    private void topLevelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        this.setNullParentObjectNode();
        this.setupSuggestedClasses();
    }

    private void formWindowClosed(java.awt.event.WindowEvent evt) {
        new JxUpdateDisplayEvent(this._segmentation).send();
    }

    private boolean _initializing = true;

    private void tabsStateChanged(javax.swing.event.ChangeEvent evt) {
        if (!this._initializing) this.object_info.setText(this.getObjectSummary());
    }

    private void writeSummary(JxSendTextEvent send_text_event) {
        send_text_event.setHeader("Jinx Object Summary");
        send_text_event.setRows(this.getObjectSummary().split("\n"));
        send_text_event.send();
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        this.writeSummary(new JxPrintEvent());
    }

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        this.writeSummary(new JxSaveTextEvent());
    }

    private void selectorActionPerformed(java.awt.event.ActionEvent evt) {
        Object o = this.selector.getSelectedItem();
        if (o instanceof OntologyElement) {
            try {
                OntologyElement element = (OntologyElement) o;
                Ontology ontology = element.getOntology();
                if (ontology != null) {
                    OntologyBrowserPanel browser_panel = new OntologyBrowserPanel(ontology);
                    this.ontology_panel.remove(this._obp);
                    this.ontology_panel.add(browser_panel, BorderLayout.CENTER);
                    this.ontology_browser = this._obp = browser_panel;
                    this._ontology = ontology;
                    this._obp.addOntologyListener(new OntologyListenerTask(this));
                    this.loadQualityList();
                } else new JxErrorEvent().send("Ontology load failure!");
            } catch (InterruptedException ie) {
                this._exception = ie;
            }
        } else new JxErrorEvent().send("Not allowed");
    }

    private void loadQualityList() {
        this._quality_list = (this._node != null) ? this.loadQualities(this._node.getUserObject()) : this.loadQualities(null);
        GridLayout gl = (GridLayout) this.qualities_list.getLayout();
        gl.setRows(this._quality_list.length);
        this.qualities_list.removeAll();
        for (QualityBox qb : this._quality_list) this.qualities_list.add(qb);
    }

    private String getObjectSummary() {
        String text = "Object: " + this.getObjectName();
        JxObjectTreeNode parent = this.getParentObjectNode();
        text += "\n\nPart of: " + ((parent != null) ? parent.toString() : "nothing");
        String qualities = "";
        for (QualityBox qb : this._quality_list) if (qb.isSelected()) {
            qualities += "\n  " + qb.getOntologyNode().toString();
            String quality_text = qb.getText();
            if ((quality_text != null) && (quality_text.trim().length() > 0)) qualities += ":\t" + quality_text;
        }
        if (qualities.length() > 0) text += "\n\nQualities:\n" + qualities;
        text += this.getListProperties(this._relations, "Related to");
        text += this.getListProperties(this._contacts, "Contacts with");
        return text;
    }

    private String getListProperties(ArrayList<JxObject> list, String prefix) {
        String text = "";
        if (list.size() > 0) {
            text += "\n\n" + prefix + ":";
            for (JxObject object : list) text += "\n  " + object.toString();
        }
        return text;
    }

    private OntologyNode _object_quality = null;

    private class JxObjectUpdatedEvent extends AsynchronousEvent {

        JxObjectUpdatedEvent(JxSegmentation segmentation) {
            super(segmentation);
        }
    }

    private void delete() {
        new DialogDestroyer(this);
    }

    private javax.swing.JButton accept;

    private javax.swing.JCheckBox aggregate;

    private javax.swing.JCheckBox auto_fix;

    private javax.swing.JRadioButton closed;

    private javax.swing.JButton contactsButton;

    private javax.swing.JButton copyFromButton;

    private javax.swing.JTextArea description;

    private javax.swing.JSpinner diameter;

    private javax.swing.JRadioButton filament;

    private javax.swing.JCheckBox ghost_following;

    private javax.swing.JCheckBox ghost_off;

    private javax.swing.JCheckBox ghost_previous;

    private javax.swing.ButtonGroup ghost_trace_group;

    private javax.swing.JCheckBox ignore;

    private javax.swing.JButton jButton1;

    private javax.swing.JButton jButton2;

    private javax.swing.JCheckBox jCheckBox1;

    private javax.swing.JLabel jLabel1;

    private javax.swing.JLabel jLabel2;

    private javax.swing.JPanel jPanel1;

    private javax.swing.JPanel jPanel10;

    private javax.swing.JPanel jPanel11;

    private javax.swing.JPanel jPanel12;

    private javax.swing.JPanel jPanel13;

    private javax.swing.JPanel jPanel14;

    private javax.swing.JPanel jPanel15;

    private javax.swing.JPanel jPanel16;

    private javax.swing.JPanel jPanel17;

    private javax.swing.JPanel jPanel18;

    private javax.swing.JPanel jPanel19;

    private javax.swing.JPanel jPanel2;

    private javax.swing.JPanel jPanel21;

    private javax.swing.JPanel jPanel22;

    private javax.swing.JPanel jPanel23;

    private javax.swing.JPanel jPanel24;

    private javax.swing.JPanel jPanel26;

    private javax.swing.JPanel jPanel28;

    private javax.swing.JPanel jPanel29;

    private javax.swing.JPanel jPanel3;

    private javax.swing.JPanel jPanel30;

    private javax.swing.JPanel jPanel31;

    private javax.swing.JPanel jPanel32;

    private javax.swing.JPanel jPanel33;

    private javax.swing.JPanel jPanel34;

    private javax.swing.JPanel jPanel35;

    private javax.swing.JPanel jPanel36;

    private javax.swing.JPanel jPanel37;

    private javax.swing.JPanel jPanel38;

    private javax.swing.JPanel jPanel39;

    private javax.swing.JPanel jPanel4;

    private javax.swing.JPanel jPanel5;

    private javax.swing.JPanel jPanel7;

    private javax.swing.JPanel jPanel8;

    private javax.swing.JPanel jPanel9;

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JScrollPane jScrollPane2;

    private javax.swing.JScrollPane jScrollPane3;

    private javax.swing.JSlider max_points;

    private javax.swing.JPanel max_points_panel;

    private javax.swing.JFormattedTextField max_points_readout;

    private javax.swing.JTextArea object_info;

    private javax.swing.JTextField object_name;

    private javax.swing.JPanel object_properties_panel;

    private javax.swing.JPanel ontology_browser;

    private javax.swing.JPanel ontology_panel;

    private javax.swing.JRadioButton open;

    private javax.swing.JButton partOfButton;

    private javax.swing.JLabel part_of;

    private javax.swing.JButton pick_color;

    private javax.swing.JRadioButton point_thread;

    private javax.swing.JPanel property_panel;

    private javax.swing.JPanel qualities_list;

    private javax.swing.JButton random_color;

    private javax.swing.JButton relatedToButton;

    private javax.swing.JPanel relationship_panel;

    private javax.swing.JComboBox selector;

    private javax.swing.ButtonGroup self_intersection_check_group;

    private javax.swing.JComboBox suggested_classes;

    private javax.swing.JTabbedPane tabs;

    private javax.swing.JRadioButton thread;

    private javax.swing.JButton topLevelButton;

    private javax.swing.ButtonGroup trace_type_group;

    private javax.swing.ButtonGroup view_group;

    private javax.swing.JCheckBox warn_me;

    private class ListHandler implements JxPropertyHandler {

        private ArrayList<JxObject> list;

        ListHandler(ArrayList<JxObject> list) {
            this.list = list;
        }

        public void handler(ArrayList<JxObject> new_list) {
            this.list.clear();
            this.list.addAll(new_list);
        }
    }

    private class ContactsHandler extends ListHandler {

        ContactsHandler(ArrayList<JxObject> contacts) {
            super(contacts);
        }
    }

    private class RelationsHandler extends ListHandler {

        RelationsHandler(ArrayList<JxObject> relations) {
            super(relations);
        }
    }

    private class PartOfHandler implements JxPropertyHandler {

        private JxObjectPropertiesDialog properties;

        PartOfHandler(JxObjectPropertiesDialog properties) {
            this.properties = properties;
        }

        public void handler(ArrayList<JxObject> object) {
            this.properties.setPartOf(object.get(0));
        }
    }

    private void setPartOf(JxObject parent) {
        if (parent != null) this.setParentObjectNode(parent); else this.setNullParentObjectNode();
        this.setupSuggestedClasses();
    }

    private void setupSuggestedClasses() {
        JxObjectTreeNode parent_node = this.getParentObjectNode();
        String text = JxObjectPropertiesDialog.NOTHING;
        if (parent_node != null) {
            text = parent_node.getUserObject().toString();
            this.loadSuggestedClasses(parent_node.getUserObject());
        }
        this.part_of.setText(text);
    }

    private class CopyFromHandler implements JxPropertyHandler {

        private JxObjectPropertiesDialog _props;

        CopyFromHandler(JxObjectPropertiesDialog properties) {
            this._props = properties;
        }

        public void handler(ArrayList<JxObject> object) {
            if (object.size() > 0) {
                JxObject selected = object.get(0);
                String name = selected.toString();
                String[] name_parts = name.split("\\.");
                if (name_parts.length == 2) {
                    OntologyNode on = this._props._ontology.getNodeByInternalName(name_parts[0]);
                    if (on != null) name = this._props._segmentation.createObjectName(on);
                } else {
                    String new_name = this._props.getIncrementedName(name);
                    if (new_name != null) name = new_name;
                }
                this._props.setObjectInformation(selected, name);
            }
        }
    }

    private static final String HAS_PART = "sao5598458770";

    private static final String IS_PART_OF = "sao9906015852";

    private static final String HAS_COMPONENT = "sao1623809261";

    private static final String IS_COMPONENT_OF = "sao138767806";

    private static final String HAS_REGIONAL_PART = "sao1434436507";

    private static final String IS_REGIONAL_PART_OF = "sao1239937685";

    private static final String[] _part_keys = { HAS_PART, HAS_COMPONENT, HAS_REGIONAL_PART };

    private void loadSuggestedClasses(JxObject parent) {
        OntologyNode parent_node = parent.getOntologyNode();
        if (parent_node != null) {
            ArrayList<OntologyNode> list = new ArrayList<OntologyNode>();
            for (String key : _part_keys) for (String r : parent_node.getRestrictionList(key)) {
                r = r.replaceAll("\\(", " ( ").replaceAll("\\)", " ) ");
                ArrayList<String> parts_list = new ArrayList<String>();
                this.restrictionParser(r.split("[\t ]+"), 0, parts_list);
                for (String p : parts_list.toArray(new String[parts_list.size()])) list.add(this._ontology.getNodeByInternalName(p));
            }
            if (list.size() > 0) this.buildComboBox(list, this.suggested_classes, "Parts"); else this.suggested_classes.setVisible(false);
        }
    }

    private void buildComboBox(ArrayList<OntologyNode> list, JComboBox box, String label) {
        DefaultComboBoxModel model = (DefaultComboBoxModel) box.getModel();
        model.addElement(new String("-- " + label + " --"));
        for (OntologyNode on : list.toArray(new OntologyNode[list.size()])) model.addElement(on);
    }

    private int restrictionParser(String[] tokens, int t0, ArrayList<String> parts) {
        int t = t0;
        do {
            if (tokens[t + 1].equals("some") || tokens[t + 1].equals("only")) {
                if (tokens[t + 2].equals("(")) t = this.restrictionParser(tokens, t + 3, parts); else {
                    parts.add(tokens[t + 2]);
                    t += 3;
                }
            } else if (tokens[t + 1].equals("and") || tokens[t + 1].equals("or")) {
                parts.add(tokens[t]);
                t += 2;
            } else if (tokens[t].equals("(")) t = this.restrictionParser(tokens, t + 1, parts);
        } while ((t < tokens.length) && !tokens[t].equals(")"));
        return t + 1;
    }

    private void recurse(ArrayList<OntologyNode> list, OntologyNode node) {
        list.add(node);
        for (OntologyNode child : node.getChildren()) this.recurse(list, child);
    }

    public synchronized void waitForCompletion() {
        JxUpdateDisplayEventListener udel = new JxUpdateDisplayEventListener();
        try {
            udel.enable();
            this.wait();
        } catch (InterruptedException ie) {
            udel.disable();
        }
    }

    private class JxUpdateDisplayEventListener extends AbstractAsynchronousEventListener {

        private Thread _thread;

        public JxUpdateDisplayEventListener() {
            this._thread = Thread.currentThread();
        }

        public void handler(AsynchronousEvent event, Object object) {
            this._thread.interrupt();
        }
    }
}
