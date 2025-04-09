import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.mapviewer.GeoPosition;

public class MainWindow implements ActionListener, ChangeListener {

    /**
	 * Das Hauptfenster der Anwendung
	 */
    private static final long serialVersionUID = 1L;

    private JFrame mainFrame;

    private JPanel panelVar1;

    private JPanel panelVar2;

    private JPanel panelVar1_topGrid;

    private JTabbedPane register;

    private JMenuBar bar;

    private JMenu menuDatei;

    private JMenu menuOptions;

    private JMenu menuHelp;

    private JMenuItem itemOeffnen;

    private JMenuItem itemPrintKoords;

    private JMenuItem itemExport2KML;

    private JMenuItem itemAbout;

    private JMenuItem itemInvertKoords;

    private JCheckBoxMenuItem cbToggleDrawNames;

    private JToolBar toolbar;

    private JButton btExport2KML;

    private JButton btInvertKoords;

    private JButton btOpen;

    private JButton btToggleDrawNames;

    private JButton btVerfPumpen;

    final Icon iconExport2KML;

    final Icon iconInvertKoords;

    final Icon iconOpen;

    final Icon iconToggleDrawNames_on;

    final Icon iconToggleDrawNames_off;

    final Icon iconVerfPumpen;

    private Image imageTitelIcon;

    private JTextArea textfeld;

    private JButton buttonCalc;

    private JLabel lblAusgangsdruck;

    private JLabel lblDurchflussmenge;

    private JLabel lblMindeseingangsdruck;

    private JComboBox cmbAusgangsdruck;

    private JComboBox cmbDurchflussmenge;

    private JComboBox cmbMindesteingangsdruck;

    private WFLW wflw = null;

    private JScrollPane scrollPaneTextfeld;

    private JScrollPane scrollPaneTabelle;

    private PumpenTable pumpentabelle;

    private JSplitPane panelVar1SplitPane;

    private JXMapKit map;

    private OwnWaypointPainter painter;

    private JFileChooser fileDialog;

    private AboutDialog aboutDialog;

    private PumpenFenster pumpenFenster;

    public MainWindow(WFLW wflw) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.wflw = wflw;
        mainFrame = new JFrame("Wasserf�rderung �ber lange Wege");
        mainFrame.setSize(800, 600);
        panelVar1 = new JPanel(new BorderLayout());
        panelVar2 = new JPanel(new BorderLayout());
        panelVar1_topGrid = new JPanel(new GridLayout(0, 2));
        register = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        register.addTab("Standorte", panelVar1);
        register.addTab("Karte", panelVar2);
        bar = new JMenuBar();
        menuDatei = new JMenu("Datei");
        menuOptions = new JMenu("Optionen");
        menuHelp = new JMenu("Hilfe");
        itemOeffnen = new JMenuItem("NMEA-Log �ffnen...");
        itemPrintKoords = new JMenuItem("Koordinaten ausgeben");
        itemExport2KML = new JMenuItem("Ergebnis exportieren...");
        itemInvertKoords = new JMenuItem("Start/Ziel tauschen");
        itemAbout = new JMenuItem("�ber WFLW");
        itemPrintKoords.setEnabled(false);
        itemExport2KML.setEnabled(false);
        cbToggleDrawNames = new JCheckBoxMenuItem("Pumpennamen anzeigen");
        cbToggleDrawNames.setSelected(true);
        itemInvertKoords.setEnabled(false);
        mainFrame.setJMenuBar(bar);
        menuDatei.add(itemOeffnen);
        menuDatei.add(itemExport2KML);
        menuDatei.add(itemPrintKoords);
        menuOptions.add(itemInvertKoords);
        menuOptions.addSeparator();
        menuOptions.add(cbToggleDrawNames);
        menuHelp.add(itemAbout);
        bar.add(menuDatei);
        bar.add(menuOptions);
        bar.add(menuHelp);
        toolbar = new JToolBar();
        iconExport2KML = new ImageIcon(this.getClass().getResource("export2KML.gif"));
        iconInvertKoords = new ImageIcon(this.getClass().getResource("invertKoords.gif"));
        iconOpen = new ImageIcon(this.getClass().getResource("open.gif"));
        iconToggleDrawNames_on = new ImageIcon(this.getClass().getResource("toggleDrawName_on.gif"));
        iconToggleDrawNames_off = new ImageIcon(this.getClass().getResource("toggleDrawName_off.gif"));
        iconVerfPumpen = new ImageIcon(this.getClass().getResource("verfPumpen.gif"));
        imageTitelIcon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("pumpe_icon_32x32.gif"));
        btExport2KML = new JButton(iconExport2KML);
        btExport2KML.setToolTipText("Ergebnis in KML exportieren");
        btExport2KML.setEnabled(false);
        btInvertKoords = new JButton(iconInvertKoords);
        btInvertKoords.setToolTipText("Start/Ziel tauschen");
        btInvertKoords.setEnabled(false);
        btOpen = new JButton(iconOpen);
        btOpen.setToolTipText("Streckendatei �ffnen");
        btToggleDrawNames = new JButton(iconToggleDrawNames_on);
        btToggleDrawNames.setToolTipText("Pumpennamen anzeigen/verstecken");
        btVerfPumpen = new JButton(iconVerfPumpen);
        btVerfPumpen.setToolTipText("verf�gbare Pumpen verwalten");
        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        toolbar.add(btOpen);
        toolbar.add(btExport2KML);
        toolbar.addSeparator();
        toolbar.add(btVerfPumpen);
        toolbar.add(btInvertKoords);
        toolbar.addSeparator();
        toolbar.add(btToggleDrawNames);
        textfeld = new JTextArea(4, 1);
        scrollPaneTextfeld = new JScrollPane(textfeld);
        scrollPaneTextfeld.setMinimumSize(new Dimension(20, 80));
        textfeld.setEditable(false);
        buttonCalc = new JButton("berechnen");
        buttonCalc.setEnabled(false);
        lblAusgangsdruck = new JLabel("Ausgangsdruck (bar)");
        lblDurchflussmenge = new JLabel("Durchflussmenge (l/min)");
        lblMindeseingangsdruck = new JLabel("Mindesteingangsdruck (bar)");
        String cmbAusgangsdruckListe[] = { "8", "10", "12" };
        cmbAusgangsdruck = new JComboBox(cmbAusgangsdruckListe);
        cmbAusgangsdruck.setEditable(true);
        String cmbDurchflussmengeListe[] = { "200", "300", "400", "500", "600", "700", "800", "900", "1000", "1100", "1200", "1300", "1400", "1500", "1600", "1800", "2000", "2200", "2400" };
        cmbDurchflussmenge = new JComboBox(cmbDurchflussmengeListe);
        cmbDurchflussmenge.setSelectedIndex(6);
        String cmbMindesteingangsdruckListe[] = { "1", "2", "3" };
        cmbMindesteingangsdruck = new JComboBox(cmbMindesteingangsdruckListe);
        cmbMindesteingangsdruck.setSelectedIndex(1);
        cmbMindesteingangsdruck.setEditable(true);
        pumpentabelle = new PumpenTable(wflw.getPumpen());
        pumpentabelle.addMouseListener(pumpentabelle);
        scrollPaneTabelle = new JScrollPane(pumpentabelle);
        panelVar1SplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        panelVar1SplitPane.setTopComponent(scrollPaneTextfeld);
        panelVar1SplitPane.setBottomComponent(scrollPaneTabelle);
        map = new JXMapKit();
        map.setDefaultProvider(org.jdesktop.swingx.JXMapKit.DefaultProviders.OpenStreetMaps);
        map.setZoom(3);
        painter = new OwnWaypointPainter(map);
        map.getMainMap().setOverlayPainter(painter);
        buttonCalc.addActionListener(this);
        btExport2KML.addActionListener(this);
        btInvertKoords.addActionListener(this);
        btOpen.addActionListener(this);
        btToggleDrawNames.addActionListener(this);
        btVerfPumpen.addActionListener(this);
        cbToggleDrawNames.addActionListener(this);
        itemAbout.addActionListener(this);
        itemExport2KML.addActionListener(this);
        itemInvertKoords.addActionListener(this);
        itemOeffnen.addActionListener(this);
        itemPrintKoords.addActionListener(this);
        register.addChangeListener(this);
        itemOeffnen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        cbToggleDrawNames.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK));
        itemExport2KML.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        itemPrintKoords.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
        itemInvertKoords.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK));
        mainFrame.setIconImage(imageTitelIcon);
        mainFrame.add(toolbar, BorderLayout.PAGE_START);
        mainFrame.add(register, BorderLayout.CENTER);
        panelVar1_topGrid.add(lblAusgangsdruck);
        panelVar1_topGrid.add(cmbAusgangsdruck);
        panelVar1_topGrid.add(lblDurchflussmenge);
        panelVar1_topGrid.add(cmbDurchflussmenge);
        panelVar1_topGrid.add(lblMindeseingangsdruck);
        panelVar1_topGrid.add(cmbMindesteingangsdruck);
        panelVar1.add(panelVar1_topGrid, BorderLayout.PAGE_START);
        panelVar1.add(panelVar1SplitPane, BorderLayout.CENTER);
        panelVar1.add(buttonCalc, BorderLayout.PAGE_END);
        panelVar2.add(map);
        fileDialog = new JFileChooser();
        aboutDialog = new AboutDialog(mainFrame);
        pumpenFenster = new PumpenFenster("verf�gbare Pumpen", this);
        mainFrame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (arg0.getSource() == this.cbToggleDrawNames) toogleDrawNames(); else if (arg0.getSource() == this.buttonCalc) funcCalc(); else if (arg0.getSource() == this.btExport2KML) funcSaveKML(); else if (arg0.getSource() == this.btInvertKoords) invertKoords(); else if (arg0.getSource() == this.btOpen) funcOeffnen(); else if (arg0.getSource() == this.btToggleDrawNames) toogleDrawNames(); else if (arg0.getSource() == this.btVerfPumpen) pumpenFenster.setVisible(!pumpenFenster.isVisible()); else if (arg0.getSource() == this.itemAbout) aboutDialog.setVisible(true); else if (arg0.getSource() == this.itemExport2KML) funcSaveKML(); else if (arg0.getSource() == this.itemInvertKoords) invertKoords(); else if (arg0.getSource() == this.itemOeffnen) funcOeffnen(); else if (arg0.getSource() == this.itemPrintKoords) funcPrintKoords();
    }

    private void toogleDrawNames() {
        if (painter.toggleDrawNames()) btToggleDrawNames.setIcon(iconToggleDrawNames_on); else btToggleDrawNames.setIcon(iconToggleDrawNames_off);
        map.repaint();
    }

    private void invertKoords() {
        wflw.invertKoords();
        funcCalc();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == this.register) funcDrawMap();
    }

    private void funcOeffnen() {
        fileDialog.resetChoosableFileFilters();
        fileDialog.addChoosableFileFilter(new FileNameExtensionFilter("NMEA-Logs (*.txt)", "txt"));
        fileDialog.addChoosableFileFilter(new FileNameExtensionFilter("GPS Exchange Format (*.gpx)", "gpx"));
        fileDialog.addChoosableFileFilter(new FileNameExtensionFilter("GPS-Tracks (*.txt),(*.gpx)", "txt", "gpx"));
        int state = fileDialog.showOpenDialog(null);
        String file = null;
        if (state == JFileChooser.APPROVE_OPTION) {
            file = fileDialog.getSelectedFile().getPath();
        }
        if (file != null) {
            if (wflw.open(file)) {
                textfeld.append("\"" + file + "\" erfolgreich geladen\n");
                textfeld.append(wflw.anzKoordinaten() + " Koordinaten-Datens�tze\n");
                setAllEnabled();
            }
        }
        funcDrawMap();
    }

    private void setAllEnabled() {
        buttonCalc.setEnabled(true);
        btExport2KML.setEnabled(true);
        btInvertKoords.setEnabled(true);
        itemPrintKoords.setEnabled(true);
        itemExport2KML.setEnabled(true);
        itemInvertKoords.setEnabled(true);
    }

    private void funcPrintKoords() {
        textfeld.setText("");
        for (Koordinate koord : wflw.getKoordList()) {
            textfeld.append(koord.string());
            textfeld.append("\n");
        }
    }

    private void funcSaveKML() {
        String filename = null;
        fileDialog.resetChoosableFileFilters();
        fileDialog.addChoosableFileFilter(new FileNameExtensionFilter("KML-GPS-Logs (*.kml)", "kml"));
        int state = fileDialog.showSaveDialog(null);
        if (state == JFileChooser.APPROVE_OPTION) filename = fileDialog.getSelectedFile().getPath();
        if (filename != null) Save.saveKML(filename, wflw.getPumpen(), wflw.getKoordList());
    }

    private void funcCalc() {
        Boolean calcDone = false;
        textfeld.setText("");
        textfeld.append("Pumpenstandortberechnung\nAusgangsdruck: " + Double.parseDouble((String) cmbAusgangsdruck.getSelectedItem()) + "bar\nF�rdermenge: " + Integer.parseInt((String) cmbDurchflussmenge.getSelectedItem()) + "l/min\nReibungsverlust: " + wflw.calcReibungsverlust(Integer.parseInt((String) cmbDurchflussmenge.getSelectedItem())) + "bar/100m\n");
        if (pumpenFenster.verfPumpenBeachten()) {
            textfeld.append("Nutze die vorgegebenen Pumpen");
            calcDone = wflw.calcEnginePoints(Integer.parseInt((String) cmbDurchflussmenge.getSelectedItem()), Double.parseDouble((String) cmbMindesteingangsdruck.getSelectedItem()), pumpenFenster.getVerfPumpen());
        } else {
            calcDone = wflw.calcEnginePoints(Double.parseDouble((String) cmbAusgangsdruck.getSelectedItem()), Integer.parseInt((String) cmbDurchflussmenge.getSelectedItem()), Double.parseDouble((String) cmbMindesteingangsdruck.getSelectedItem()));
        }
        if (calcDone) funcDrawMap(); else System.out.println("Fehler in der Berechnung");
    }

    private void funcDrawMap() {
        Koordinate k = wflw.getMitte();
        painter.setWflw(wflw);
        map.setAddressLocation(new GeoPosition(k.getLat(), k.getLon()));
        map.repaint();
    }

    public void setEnabledPaEingabe(Boolean enable) {
        cmbAusgangsdruck.setEnabled(enable);
    }

    public Image getIconImage() {
        return mainFrame.getIconImage();
    }

    public Point getLocationForChild() {
        return new Point(mainFrame.getLocation().x + (int) mainFrame.getWidth(), mainFrame.getLocation().y);
    }
}
