package net.sourceforge.glsof.common.preferences;

import static net.sourceforge.glsof.common.i18n.Messages.NLS;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import net.sourceforge.glsof.common.about.AbstractButtonsDialog;
import net.sourceforge.glsof.common.model.Filter;
import net.sourceforge.glsof.common.model.OtherPreferences;
import net.sourceforge.glsof.common.model.Preferences;

public class PreferencesDialog extends AbstractButtonsDialog {

    private Map<String, AbstractCommand> _commands = new HashMap<String, AbstractCommand>();

    protected JTable _table;

    protected List<Filter> _filters;

    protected OtherPreferences _page;

    private ButtonEditor _removeButtonEditor;

    private List<Integer> _removeIndexes = new LinkedList<Integer>();

    private ButtonGroup _idLogin;

    private ButtonGroup _sizeOffset;

    private JComponent[] _timeout;

    private JComponent[] _links;

    private JCheckBox _avoid;

    private JCheckBox _ipFormat;

    private JCheckBox _nfs;

    private JCheckBox _ports;

    private JCheckBox _sockets;

    private JCheckBox _and;

    private String[] _urls;

    public PreferencesDialog(Preferences prefs, String[] urls) {
        super(new Dimension(640, 400), NLS("Preferences"));
        _urls = urls;
        _filters = prefs.getFilters();
        _page = prefs.getOtherPreferences();
    }

    @Override
    protected void createDialogArea(JPanel parent) {
        _table = createTable();
        JScrollPane tableScrollPane = new JScrollPane(_table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JPanel ancestor = (JPanel) parent.getParent();
        ancestor.add(createFilters(), BorderLayout.LINE_START);
        ancestor.add(tableScrollPane, BorderLayout.CENTER);
        ancestor.add(createGeneralOptions(), BorderLayout.LINE_END);
    }

    private JPanel createFilters() {
        JPanel main = new JPanel();
        main.setLayout(new GridBagLayout());
        int y = 0;
        _commands.put("Process", new TextExcludeCommand(main, this, "Process", y));
        _commands.put("ID/Login name", new TextExcludeCommand(main, this, "ID/Login name", ++y));
        _commands.put("File Descriptor", new TextExcludeCommand(main, this, "File Descriptor", ++y));
        _commands.put("PID", new NumericExcludeCommand(main, this, "PID", ++y));
        _commands.put("PGID", new NumericExcludeCommand(main, this, "PGID", ++y));
        _commands.put("Network", new NetworkCommand(main, this, "Network", ++y));
        _commands.put("Path", new PathCommand(main, this, "Path", y + 5));
        _commands.put("Directory", new DirectoryCommand(main, this, "Directory", y + 6));
        return main;
    }

    private JTable createTable() {
        Vector<String> columns = new Vector<String>(Arrays.asList(new String[] { "Type", "Value", "Action", "", "Index" }));
        Vector<Vector<Object>> model = new Vector<Vector<Object>>();
        for (int i = 0; i < _filters.size(); i++) {
            final Filter tv = _filters.get(i);
            model.add(new Vector<Object>(Arrays.asList(new Object[] { tv.getType(), fromDataToString(tv.getType(), tv.getValues()), !tv.getType().equals("Path") && !tv.getType().equals("Directory") && !tv.getType().equals("Network") ? tv.getValues().get(1) : "Include", "Remove", i })));
        }
        final JTable table = new JTable(model, columns);
        table.getColumn("").setCellRenderer(new ButtonRenderer());
        table.removeColumn(table.getColumnModel().getColumn(4));
        _removeButtonEditor = new ButtonEditor(table, new JCheckBox(), _removeIndexes);
        table.getColumn("").setCellEditor(_removeButtonEditor);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent event) {
                if (!_removeButtonEditor.removeTableRow() && !event.getValueIsAdjusting()) enableButtons();
            }
        });
        return table;
    }

    private JPanel createGeneralOptions() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setAlignmentY(Component.TOP_ALIGNMENT);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(createLabel(panel, NLS("Global")), createGridBagConstraints(0, 0));
        _and = getCheckButton(NLS("AND_all_settings"), _page.isAnd());
        panel.add(_and, createGridBagConstraints(0, 1));
        _avoid = getCheckButton(NLS("Avoid"), _page.isAvoid());
        panel.add(_avoid, createGridBagConstraints(0, 2));
        _ipFormat = getCheckButton(NLS("Show_addresses_in_IP-format"), _page.isIpFormat());
        panel.add(_ipFormat, createGridBagConstraints(0, 3));
        _nfs = getCheckButton(NLS("NFS_files"), _page.isNfs());
        panel.add(_nfs, createGridBagConstraints(0, 4));
        _ports = getCheckButton(NLS("Show_port-numbers"), _page.isPortNumbers());
        panel.add(_ports, createGridBagConstraints(0, 5));
        _sockets = getCheckButton(NLS("UNIX_domain_socket_files"), _page.isDomainSocket());
        panel.add(_sockets, createGridBagConstraints(0, 6));
        _idLogin = getRadioButtons(new String[] { NLS("ID_Number"), NLS("Login_Name") }, _page.isIdNumber());
        Enumeration<AbstractButton> buttons = _idLogin.getElements();
        packTwoElements(panel, buttons.nextElement(), buttons.nextElement(), 7);
        _sizeOffset = getRadioButtons(new String[] { NLS("File_Size"), NLS("File_Offset") }, _page.isSize());
        buttons = _sizeOffset.getElements();
        packTwoElements(panel, buttons.nextElement(), buttons.nextElement(), 8);
        _links = getCheckButtonAndSpinner(NLS("Max_number_of_links_for_a_file"), _page.getLinksFileValue(), 1, _page.isLinksFile());
        packTwoElements(panel, _links[0], _links[1], 9);
        _timeout = getCheckButtonAndSpinner(NLS("Timeout_s"), _page.getTimeoutValue(), 2, _page.isTimeout());
        packTwoElements(panel, _timeout[0], _timeout[1], 10);
        JLabel externalLinks = new JLabel(NLS("External_Links"));
        externalLinks.setFont(new Font("Dialog", Font.BOLD, 16));
        panel.add(externalLinks, createGridBagConstraints(0, 11));
        final Color linkColor = new Color(0x0000aa);
        JLabel clickHere = new JLabel("<html><u>" + NLS("Documentation") + "</u></html>");
        clickHere.setForeground(linkColor);
        panel.add(clickHere, createGridBagConstraints(0, 12));
        clickHere.addMouseListener(new LinkAction(_urls[0]));
        clickHere = new JLabel("<html><u>" + NLS("Quick_Start_Guide") + "</u></html>");
        clickHere.setForeground(linkColor);
        panel.add(clickHere, createGridBagConstraints(0, 13));
        clickHere.addMouseListener(new LinkAction(_urls[1]));
        return panel;
    }

    private void packTwoElements(JPanel panel, Component c1, Component c2, int gridy) {
        JPanel sub = new JPanel(new GridBagLayout());
        sub.add(c1, createGridBagConstraints(0, 0));
        sub.add(c2, createGridBagConstraints(1, 0));
        GridBagConstraints createGridBagConstraints = createGridBagConstraints(0, gridy);
        createGridBagConstraints.gridwidth = 2;
        panel.add(sub, createGridBagConstraints);
    }

    private GridBagConstraints createGridBagConstraints(int gridx, int gridy) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weighty = 1;
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        return gbc;
    }

    private JLabel createLabel(JPanel panel, String text) {
        JLabel label = new JLabel(NLS(text));
        label.setVerticalTextPosition(SwingConstants.TOP);
        label.setVerticalAlignment(SwingConstants.TOP);
        label.setAlignmentY(Component.TOP_ALIGNMENT);
        label.setFont(new Font("Dialog", Font.BOLD, 16));
        return label;
    }

    private JCheckBox getCheckButton(final String text, final boolean state) {
        final JCheckBox button = new JCheckBox(text);
        button.setSelected(state);
        return button;
    }

    private ButtonGroup getRadioButtons(final String[] texts, final boolean state) {
        ButtonGroup group = new ButtonGroup();
        JRadioButton button1 = new JRadioButton(texts[0]);
        JRadioButton button2 = new JRadioButton(texts[1]);
        group.add(button1);
        group.add(button2);
        group.setSelected(state ? button1.getModel() : button2.getModel(), true);
        return group;
    }

    private JComponent[] getCheckButtonAndSpinner(final String text, final int value, final int min, final boolean state) {
        final JComponent[] controls = new JComponent[2];
        controls[0] = getCheckButton(text, state);
        controls[1] = new JSpinner(new SpinnerNumberModel(value, min, 10000000, 1));
        controls[1].setEnabled(state);
        ((JCheckBox) controls[0]).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                controls[1].setEnabled(((JCheckBox) event.getSource()).isSelected());
            }
        });
        return controls;
    }

    private String fromDataToString(String type, List<String> values) {
        String value = values.get(0);
        if (type.equals("Network")) {
            value += !values.get(1).equals(" ") ? " " + values.get(1) : "";
            value += !values.get(2).equals(" ") ? " " + values.get(2) : "";
            value += !values.get(3).equals(" ") ? " IPV" + values.get(3) : "";
        } else if (type.equals("Directory")) {
            value += !values.get(1).equals(" ") ? " D" : "";
            value += !values.get(2).equals(" ") ? " M" : "";
            value += !values.get(3).equals(" ") ? " L" : "";
        }
        return value.replaceAll("^\\s+", "");
    }

    private void enableButtons() {
        if (_table.getSelectedRow() < 0) return;
        String command = (String) (_table.getModel()).getValueAt(_table.getSelectedRow(), 0);
        Integer index = (Integer) (_table.getModel()).getValueAt(_table.getSelectedRow(), 4);
        _commands.get(command).copyValuesInTheMask(index);
    }

    @Override
    protected void createButtonsForButtonBar(JPanel parent) {
        super.createButtonsForButtonBar(parent);
        createButton(parent, AbstractButtonsDialog.OK_RESTART_ID, NLS("Ok_Restart"));
    }

    @Override
    protected void buttonPressed(int i) {
        if (i == OK_ID || i == OK_RESTART_ID) {
            Collections.sort(_removeIndexes);
            for (int l = _removeIndexes.size() - 1; l >= 0; l--) _filters.set(_removeIndexes.get(l), null);
            _page.setIdNumber(_idLogin.getElements().nextElement().isSelected());
            _page.setSize(_sizeOffset.getElements().nextElement().isSelected());
            _page.setTimeout(((JCheckBox) _timeout[0]).isSelected());
            _page.setTimeoutValue((Integer) ((JSpinner) _timeout[1]).getValue());
            _page.setLinksFile(((JCheckBox) _links[0]).isSelected());
            _page.setLinksFileValue((Integer) ((JSpinner) _links[1]).getValue());
            _page.setAvoid(_avoid.isSelected());
            _page.setIpFormat(_ipFormat.isSelected());
            _page.setNfs(_nfs.isSelected());
            _page.setPortNumbers(_ports.isSelected());
            _page.setDomainSocket(_sockets.isSelected());
            _page.setAnd(_and.isSelected());
        }
        dispose();
    }

    private class LinkAction extends MouseAdapter {

        private String _url;

        public LinkAction(String url) {
            _url = url;
        }

        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() <= 0 && !Desktop.isDesktopSupported()) return;
            try {
                Desktop.getDesktop().browse(new URI(_url));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
