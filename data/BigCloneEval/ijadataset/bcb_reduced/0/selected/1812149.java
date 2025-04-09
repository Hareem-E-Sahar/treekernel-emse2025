package ch.amotta.qweely.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Alessandro
 */
public class MainFrame implements ActionListener, ListSelectionListener {

    private JFrame _frame;

    private JMenuItem _menuHelpHomepage;

    private JList _list;

    private FileinfoTableModel _tableModel;

    private JPanel _blackHole;

    private JLabel _status;

    private ArrayList<Result> _results;

    public MainFrame() {
        _results = new ArrayList<Result>();
        _frame = new JFrame("Qweely");
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        _frame.setResizable(false);
        BorderLayout guiLayout = new BorderLayout();
        guiLayout.setVgap(5);
        JPanel guiContentPane = new JPanel(guiLayout);
        guiContentPane.setPreferredSize(new Dimension(500, 300));
        JMenuBar guiMenuBar = new JMenuBar();
        JMenu guiMenuButton = new JMenu("Help");
        _menuHelpHomepage = new JMenuItem("Homepage");
        _menuHelpHomepage.addActionListener(this);
        guiMenuButton.add(_menuHelpHomepage);
        guiMenuBar.add(guiMenuButton);
        _frame.setJMenuBar(guiMenuBar);
        DefaultListModel guiListModel = new DefaultListModel();
        _list = new JList(guiListModel);
        _list.setLayoutOrientation(JList.VERTICAL_WRAP);
        _list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _list.setVisibleRowCount(10);
        _list.addListSelectionListener(this);
        JScrollPane guiListScroll = new JScrollPane(_list);
        guiListScroll.setPreferredSize(new Dimension(195, 235));
        guiContentPane.add(guiListScroll, BorderLayout.WEST);
        JTable guiTable = new JTable();
        _tableModel = new FileinfoTableModel();
        _tableModel.setDataVector(new Object[][] {}, new String[] { "Property", "Value" });
        guiTable.setModel(_tableModel);
        JScrollPane guiTableScroll = new JScrollPane(guiTable);
        guiTable.setPreferredScrollableViewportSize(new Dimension(300, 235));
        guiContentPane.add(guiTableScroll, BorderLayout.EAST);
        _blackHole = new JPanel();
        _blackHole.setBackground(new Color(255, 255, 255));
        JLabel guiStatus = new JLabel("Drop a WAVE file here", JLabel.CENTER);
        _blackHole.add(guiStatus, BorderLayout.CENTER);
        JScrollPane guiBlackHoleScroll = new JScrollPane(_blackHole);
        guiBlackHoleScroll.setPreferredSize(new Dimension(500, 60));
        guiContentPane.add(guiBlackHoleScroll, BorderLayout.SOUTH);
        _frame.setContentPane(guiContentPane);
        _frame.pack();
        _frame.setVisible(true);
    }

    public void addResult(Result result) {
        int resultID = _results.size();
        _results.add(result);
        DefaultListModel listModel = (DefaultListModel) _list.getModel();
        listModel.insertElementAt(result.getFilename(), resultID);
        showResult(resultID);
    }

    public void showResult(int id) {
        try {
            _list.setSelectedIndex(id);
            String[] dataLabels = { "Property", "Value" };
            _tableModel.setDataVector(_results.get(id).getAsDataVector(), dataLabels);
        } catch (IndexOutOfBoundsException ex) {
        }
    }

    public JPanel getBlackHole() {
        return _blackHole;
    }

    public void actionPerformed(ActionEvent e) {
        Object actionSource = e.getSource();
        if (actionSource == _menuHelpHomepage) {
            openURI("http://sourceforge.net/projects/qweely/");
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() == false) {
            if (_list.getSelectedIndex() != -1) {
                showResult(_list.getSelectedIndex());
            }
        }
    }

    private void openURI(String uriString) {
        try {
            URI uri = new URI(uriString);
            if (java.awt.Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(uri);
                }
            }
        } catch (Exception ex) {
        }
    }
}
