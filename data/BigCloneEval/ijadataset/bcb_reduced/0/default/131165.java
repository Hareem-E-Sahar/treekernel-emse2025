import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import javax.media.rtp.*;
import javax.media.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

public class Garatron extends JFrame implements ActionListener, KeyListener, MouseListener, WindowListener {

    Container contentPane = getContentPane();

    JTabbedPane jtabbedpane = new JTabbedPane();

    JPanel firstPanel = new JPanel(new BorderLayout());

    JPanel helpPanel = new JPanel(new BorderLayout());

    Vector targets;

    JList list;

    JButton startButton;

    JButton raddButton;

    JButton rremoveButton;

    JTextField iprText;

    JTextField datarText;

    JTextField locator;

    JTextField dataText;

    TTargetListModel listModel;

    GaraTransmitter garaTransmitter;

    JCheckBox loop;

    GaraTransmitConfig config;

    Vector rtargets;

    JList rlist;

    JButton rstartButton;

    JButton rraddButton;

    JButton rrremoveButton;

    JTextField riprText;

    JTextField rdatarText;

    JTextField rdataText;

    RTargetListModel rlistModel;

    GaraReceiveConfig rconfig;

    GaraReceiver garaReceiver;

    JPanel rremotePanel;

    JPanel remotePanel;

    JPanel controlsPanel;

    JPanel playerPanel;

    private Player player;

    private Component visualMedia;

    private Container container;

    private File mediaFile;

    private URL fileURL;

    private Component mediaControl;

    private Processor processor;

    JButton openFile;

    JButton urlButton;

    JTextArea firstpageTextArea;

    JTextArea helppageTextArea;

    JLabel page1Icon1;

    static Icon garaIcon = new ImageIcon("../images/GaratronMain.jpeg");

    public Garatron() {
        setTitle("Garatron");
        config = new GaraTransmitConfig();
        rconfig = new GaraReceiveConfig();
        playerPanel = new JPanel(new BorderLayout());
        JPanel receivePanel = new JPanel(new BorderLayout());
        JPanel transmitPanel = new JPanel(new BorderLayout());
        JPanel playerbuttonPanel = new JPanel(new BorderLayout());
        JPanel playerbuttonHoldPanel = new JPanel(new FlowLayout());
        openFile = new JButton("File");
        urlButton = new JButton("URL");
        playerbuttonHoldPanel.add(openFile);
        playerbuttonHoldPanel.add(urlButton);
        playerbuttonPanel.add(playerbuttonHoldPanel, BorderLayout.NORTH);
        TitledBorder titledplayerbuttonBorder = new TitledBorder(new EtchedBorder(), "Open");
        playerbuttonPanel.setBorder(titledplayerbuttonBorder);
        TitledBorder titledplayerBorder = new TitledBorder(new EtchedBorder(), "Player");
        playerPanel.setBorder(titledplayerBorder);
        playerPanel.add(playerbuttonPanel, BorderLayout.NORTH);
        firstPanel.add(page1Icon1 = new JLabel(garaIcon));
        page1Icon1.setBounds(0, 115, 550, 115);
        Manager.setHint(Manager.LIGHTWEIGHT_RENDERER, Boolean.TRUE);
        JPanel rhostsPanel = new JPanel(new BorderLayout());
        TitledBorder titledrhostsBorder = new TitledBorder(new EtchedBorder(), "Hosts");
        rhostsPanel.setBorder(titledrhostsBorder);
        JPanel rlocalPanel = rcreateLocalPanel();
        JPanel rlocalHoldPanel = new JPanel(new BorderLayout());
        rlocalHoldPanel.add(rlocalPanel, BorderLayout.NORTH);
        rhostsPanel.add(rlocalHoldPanel, BorderLayout.WEST);
        receivePanel.add(rhostsPanel, BorderLayout.NORTH);
        rremotePanel = new JPanel(new BorderLayout());
        TitledBorder titledrremoteBorder = new TitledBorder(new EtchedBorder(), "Remote");
        rremotePanel.setBorder(titledrremoteBorder);
        rhostsPanel.add(rremotePanel, BorderLayout.CENTER);
        JPanel rutilitiesPanel = rcreateutilitiesPanel();
        receivePanel.add(rutilitiesPanel, BorderLayout.CENTER);
        JPanel localPanel = createLocalPanel();
        JPanel localHoldPanel = new JPanel(new BorderLayout());
        localHoldPanel.add(localPanel, BorderLayout.NORTH);
        JPanel hostsPanel = new JPanel(new BorderLayout());
        TitledBorder titledhostsBorder = new TitledBorder(new EtchedBorder(), "Hosts");
        hostsPanel.setBorder(titledhostsBorder);
        hostsPanel.add(localHoldPanel, BorderLayout.WEST);
        transmitPanel.add(hostsPanel, BorderLayout.NORTH);
        remotePanel = new JPanel(new BorderLayout());
        TitledBorder titledremoteBorder = new TitledBorder(new EtchedBorder(), "Remote");
        remotePanel.setBorder(titledremoteBorder);
        hostsPanel.add(remotePanel, BorderLayout.CENTER);
        JPanel utilitiesPanel = createutilitiesPanel();
        controlsPanel = new JPanel(new BorderLayout());
        JPanel targetControlHoldPanel = new JPanel(new BorderLayout());
        TitledBorder titledtargetcontrolBorder = new TitledBorder(new EtchedBorder(), "Utilities");
        targetControlHoldPanel.setBorder(titledtargetcontrolBorder);
        TitledBorder titledcontrolBorder = new TitledBorder(new EtchedBorder(), "Controls");
        controlsPanel.setBorder(titledcontrolBorder);
        transmitPanel.add(targetControlHoldPanel, BorderLayout.CENTER);
        targetControlHoldPanel.add(utilitiesPanel, BorderLayout.WEST);
        JPanel mediaPanel = createMediaPanel();
        JPanel controlsHoldPanel = new JPanel(new BorderLayout());
        controlsHoldPanel.add(controlsPanel, BorderLayout.NORTH);
        targetControlHoldPanel.add(controlsHoldPanel, BorderLayout.CENTER);
        controlsPanel.add(mediaPanel, BorderLayout.NORTH);
        JPanel buttonPanel = new JPanel();
        controlsPanel.add(buttonPanel, BorderLayout.CENTER);
        Image Icon = Toolkit.getDefaultToolkit().getImage("../images/logo1.png");
        setIconImage(Icon);
        String helpText = "\n\n\t\t\tGaratron\t\t\n" + "\t\t\t------------\t\t\n\n\n" + "	Garatron is a project made by" + " 3 students" + "(Pasang Gurung, Pramod Nepal and\nRajendra BhadurThapa)" + " of Pulchowk Campus of 060 batch.\n" + "	It is an application that helps you assist your remote meetings, distant education \n" + "and even as a media player for some JMF supported media formats\n" + "\n" + "	To start and get going with Garatron and its features, you must first setup\n" + "necessary settings. Some of them are listed below:\n\n" + " 1. In Windows Operating System install Java 1.5 or later.\n" + " 2. Install JMF 2.1.1e\n" + " 3. Setup necessary environment variables\n" + " 4. Run the customizer.exe in the bin folder of installation directory\n" + "     - tick the necessary checkboxes\n" + "     - after pressing next and ticking necessary settings hit Customize\n" + "     - if some error appears about missing file RegistryLib.java make the required\n" + "       directory compile the file at cuswork directory and place the .class file at\n" + "       the specified directory\n" + " 5. Install webcamera,microphone(devices)\n" + "     - can be checked by JMStudio at capture.\n" + " 6. Start Garatron.\n\n" + "  Transmitter\n\n" + "  - At Local enter IP and Port Address of transmitting computer.\n" + "  - At Remote enter IP and Port Address of receiving computer and Add more Receivers.\n" + "  - Start to transmit the media at Locator \n\n" + "  Receiver\n\n" + "  - At Local enter IP and Port Address of local computer(receiving).\n" + "  - At Remote enter IP and Port Address of transmitting computer and Add more Transmitters.\n" + "  - Start to receive the transmitted file from remote transmitter\n" + "		\n\n\t\t\t\t\tGaratron Team\n\n";
        String firstText = "";
        firstpageTextArea = new JTextArea(firstText, 20, 20);
        firstpageTextArea.setEditable(false);
        firstPanel.add(new JScrollPane(firstpageTextArea), BorderLayout.CENTER);
        helppageTextArea = new JTextArea(helpText, 20, 20);
        helppageTextArea.setEditable(false);
        helpPanel.add(new JScrollPane(helppageTextArea), BorderLayout.CENTER);
        jtabbedpane.addTab("", new ImageIcon("../images/logo1.png"), firstPanel);
        jtabbedpane.addTab("Transmitter", new ImageIcon("../images/transmit.gif"), transmitPanel);
        jtabbedpane.addTab("Receiver", new ImageIcon("../images/transmit.gif"), receivePanel);
        jtabbedpane.addTab("Player", new ImageIcon("../images/open.gif"), playerPanel);
        jtabbedpane.addTab("Help", new ImageIcon("../images/help.gif"), helpPanel);
        openFile.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                mediaFile = getFile();
                if (mediaFile != null) {
                    try {
                        fileURL = mediaFile.toURL();
                    } catch (MalformedURLException badURL) {
                        badURL.printStackTrace();
                        showErrorMessage("Bad URL");
                    }
                    makePlayer(fileURL.toString());
                }
            }
        });
        urlButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                String addressName = getMediaLocation();
                if (addressName != null) makePlayer(addressName);
            }
        });
        JLabel playersidePictureLabel;
        Icon playersideIcon = new ImageIcon("../images/PlayerSide.png");
        playerPanel.add(playersidePictureLabel = new JLabel(playersideIcon), BorderLayout.WEST);
        playerPanel.add(playersidePictureLabel = new JLabel(playersideIcon), BorderLayout.EAST);
        playersidePictureLabel.setBounds(0, 0, 70, 250);
        JPanel sidePanel = new JPanel(new BorderLayout());
        JLabel sidePictureLabel;
        Icon sideIcon = new ImageIcon("../images/GaratronSide.jpeg");
        sidePanel.add(sidePictureLabel = new JLabel(sideIcon), BorderLayout.CENTER);
        sidePictureLabel.setBounds(0, 10, 50, 300);
        contentPane.setLayout(new BorderLayout());
        contentPane.add(sidePanel, BorderLayout.WEST);
        contentPane.add(jtabbedpane, BorderLayout.CENTER);
        list.addMouseListener(this);
        addWindowListener(this);
        pack();
        setLocation(180, 100);
        setResizable(false);
        setVisible(true);
    }

    public void makePlayer(String mediaLocation) {
        if (player != null) removePlayerComponents();
        MediaLocator mediaLocator = new MediaLocator(mediaLocation);
        if (mediaLocator == null) {
            showErrorMessage("Error opening file");
            return;
        }
        try {
            player = Manager.createPlayer(mediaLocator);
            player.addControllerListener(new PlayerEventHandler());
            player.realize();
        } catch (NoPlayerException noPlayerException) {
            noPlayerException.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void removePlayerComponents() {
        if (visualMedia != null) playerPanel.remove(visualMedia);
        if (mediaControl != null) playerPanel.remove(mediaControl);
        player.close();
    }

    public File getFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.CANCEL_OPTION) return null; else return fileChooser.getSelectedFile();
    }

    public String getMediaLocation() {
        String input = JOptionPane.showInputDialog(this, "Enter URL");
        if (input != null && input.length() == 0) return null;
        return input;
    }

    private class PlayerEventHandler extends ControllerAdapter {

        public void realizeComplete(RealizeCompleteEvent realizeDoneEvent) {
            player.prefetch();
        }

        public void prefetchComplete(PrefetchCompleteEvent prefetchDoneEvent) {
            getMediaComponents();
            validate();
            player.start();
        }

        public void endOfMedia(EndOfMediaEvent mediaEndEvent) {
            player.setMediaTime(new Time(0));
            player.stop();
        }
    }

    public void getMediaComponents() {
        visualMedia = player.getVisualComponent();
        if (visualMedia != null) playerPanel.add(visualMedia, BorderLayout.CENTER);
        mediaControl = player.getControlPanelComponent();
        if (mediaControl != null) playerPanel.add(mediaControl, BorderLayout.SOUTH);
    }

    private JPanel rcreateutilitiesPanel() {
        JPanel receivePanel = new JPanel(new BorderLayout());
        rtargets = rconfig.targets;
        rlistModel = new RTargetListModel(rtargets);
        rlist = new JList(rlistModel);
        rlist.addKeyListener(this);
        rlist.setPrototypeCellValue("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        JScrollPane rscrollPane = new JScrollPane(rlist, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        JPanel rtargetHoldPanel = new JPanel(new BorderLayout());
        TitledBorder titledrtargetHoldBorder = new TitledBorder(new EtchedBorder(), "Participants");
        rtargetHoldPanel.setBorder(titledrtargetHoldBorder);
        rtargetHoldPanel.add(rscrollPane, BorderLayout.NORTH);
        receivePanel.add(rtargetHoldPanel, BorderLayout.WEST);
        JPanel rp1 = new JPanel(new BorderLayout());
        JPanel rp1_ip = new JPanel(new FlowLayout());
        JLabel rlabel = new JLabel("IP      :");
        rp1.add(rp1_ip, BorderLayout.NORTH);
        rp1_ip.add(rlabel);
        riprText = new JTextField(15);
        rp1_ip.add(riprText);
        JPanel rp1_port = new JPanel(new FlowLayout());
        JPanel middle_rp = new JPanel(new BorderLayout());
        rp1.add(middle_rp, BorderLayout.CENTER);
        middle_rp.add(rp1_port, BorderLayout.NORTH);
        rlabel = new JLabel("Port :");
        rp1_port.add(rlabel);
        rdatarText = new JTextField(15);
        rp1_port.add(rdatarText);
        JPanel rp2 = new JPanel();
        rraddButton = new JButton("Add");
        rrremoveButton = new JButton("Remove");
        rp2.add(rraddButton);
        rp2.add(rrremoveButton);
        rraddButton.addActionListener(this);
        rrremoveButton.addActionListener(this);
        rp1.add(rp2, BorderLayout.SOUTH);
        rremotePanel.add(rp1, BorderLayout.CENTER);
        TitledBorder rtitledBorder = new TitledBorder(new EtchedBorder(), "Utilities");
        receivePanel.setBorder(rtitledBorder);
        if (rtargets.size() > 0) {
            rrremoveButton.setEnabled(true);
        } else {
            rrremoveButton.setEnabled(false);
        }
        JPanel rcontrolsPanel = new JPanel(new BorderLayout());
        TitledBorder titledrcontrolsBorder = new TitledBorder(new EtchedBorder(), "Controls");
        rcontrolsPanel.setBorder(titledrcontrolsBorder);
        JPanel rbuttonPanel = new JPanel();
        rstartButton = new JButton("Start");
        rstartButton.addActionListener(this);
        rbuttonPanel.add(rstartButton);
        rcontrolsPanel.add(rbuttonPanel, BorderLayout.NORTH);
        JPanel rcontrolsholdPanel = new JPanel(new BorderLayout());
        rcontrolsholdPanel.add(rcontrolsPanel, BorderLayout.NORTH);
        receivePanel.add(rcontrolsholdPanel, BorderLayout.CENTER);
        return receivePanel;
    }

    private JPanel rcreateLocalPanel() {
        JPanel receivePanel = new JPanel(new BorderLayout());
        JPanel rip_Panel = new JPanel(new FlowLayout());
        JLabel rlabel = new JLabel("IP      :");
        receivePanel.add(rip_Panel, BorderLayout.NORTH);
        rip_Panel.add(rlabel);
        JTextField rtf_local_host = new JTextField(15);
        rip_Panel.add(rtf_local_host);
        JPanel rp1_local_port = new JPanel(new FlowLayout());
        receivePanel.add(rp1_local_port, BorderLayout.CENTER);
        rlabel = new JLabel("Port :");
        rp1_local_port.add(rlabel);
        rdataText = new JTextField(15);
        rp1_local_port.add(rdataText);
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            rtf_local_host.setText(host);
        } catch (UnknownHostException e) {
        }
        TitledBorder rtitledBorder = new TitledBorder(new EtchedBorder(), "Local");
        receivePanel.setBorder(rtitledBorder);
        return receivePanel;
    }

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == rraddButton) {
            String rip = riprText.getText().trim();
            String rport = rdatarText.getText().trim();
            String rlocalPort = rdataText.getText().trim();
            if (garaReceiver != null) {
                garaReceiver.addTarget(rip, rport, rlocalPort);
            }
            raddTargetToList(rlocalPort, rip, rport);
        } else if (source == rrremoveButton) {
            String rip = riprText.getText().trim();
            String rport = rdatarText.getText().trim();
            int rindex = rlist.getSelectedIndex();
            if (rindex != -1) {
                GaraTarget rtarget = (GaraTarget) rtargets.elementAt(rindex);
                if (garaReceiver != null) {
                    garaReceiver.removeTarget(rip, rport);
                }
                rtargets.removeElement(rtarget);
                rlistModel.setData(rtargets);
                if (rtargets.size() == 0) {
                    rrremoveButton.setEnabled(false);
                }
                if (rtargets.size() > 0) {
                    if (rindex > 0) {
                        rindex--;
                    } else {
                        rindex = 0;
                    }
                    rlist.setSelectedIndex(rindex);
                    rsetTargetFields();
                } else {
                    rlist.setSelectedIndex(-1);
                }
            }
        } else if (source == rstartButton) {
            if (rstartButton.getLabel().equals("Start")) {
                garaReceiver = new GaraReceiver(this, rtargets);
                rstartButton.setLabel("Stop");
            } else {
                garaReceiver.close();
                garaReceiver = null;
                rstartButton.setLabel("Start");
            }
        }
        if (source == raddButton) {
            String ip = iprText.getText().trim();
            String port = datarText.getText().trim();
            String localPort = dataText.getText().trim();
            addTargetToList(localPort, ip, port);
            if (garaTransmitter != null) {
                garaTransmitter.addTarget(ip, port);
            }
        } else if (source == rremoveButton) {
            int index = list.getSelectedIndex();
            if (index != -1) {
                GaraTarget target = (GaraTarget) targets.elementAt(index);
                if (garaTransmitter != null) {
                    garaTransmitter.removeTarget(target.ip, target.port);
                }
                targets.removeElement(target);
                listModel.setData(targets);
            }
        } else if (source == startButton) {
            if (startButton.getLabel().equals("Start")) {
                int data_port = new Integer(dataText.getText()).intValue();
                garaTransmitter = new GaraTransmitter(this, data_port);
                garaTransmitter.start(locator.getText().trim(), targets);
                garaTransmitter.setLooping(loop.isSelected());
                startButton.setLabel("Stop");
            } else if (startButton.getLabel().equals("Stop")) {
                garaTransmitter.stop();
                garaTransmitter = null;
                removeNonBaseTargets();
                listModel.setData(targets);
                startButton.setLabel("Start");
            }
        } else if (source == loop) {
            if (garaTransmitter != null) {
                garaTransmitter.setLooping(loop.isSelected());
            }
        }
    }

    public synchronized void raddTargetToList(String rlocalPort, String rip, String rport) {
        RListUpdater rlistUpdater = new RListUpdater(rlocalPort, rip, rport, rlistModel, rtargets, rrremoveButton);
        SwingUtilities.invokeLater(rlistUpdater);
    }

    private JPanel createMediaPanel() {
        JPanel transmitPanel = new JPanel(new BorderLayout());
        JPanel locatorPanel = new JPanel(new FlowLayout());
        JLabel label = new JLabel("Locator :");
        locatorPanel.add(label);
        transmitPanel.add(locatorPanel, BorderLayout.NORTH);
        locator = new JTextField(15);
        locator.setText(config.media_locator);
        locatorPanel.add(locator);
        loop = new JCheckBox("loop");
        startButton = new JButton("Start");
        startButton.setEnabled(true);
        startButton.addActionListener(this);
        locatorPanel.add(loop);
        loop.setSelected(true);
        loop.addActionListener(this);
        JPanel startButtonPanel = new JPanel(new FlowLayout());
        startButtonPanel.add(startButton);
        transmitPanel.add(startButtonPanel, BorderLayout.SOUTH);
        return transmitPanel;
    }

    private JPanel createutilitiesPanel() {
        JPanel transmitPanel = new JPanel(new BorderLayout());
        targets = new Vector();
        for (int i = 0; i < config.targets.size(); i++) {
            targets.addElement(config.targets.elementAt(i));
        }
        listModel = new TTargetListModel(targets);
        list = new JList(listModel);
        list.addKeyListener(this);
        list.setPrototypeCellValue("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        JScrollPane scrollPane = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        transmitPanel.add(scrollPane, BorderLayout.NORTH);
        JPanel p1 = new JPanel(new BorderLayout());
        JPanel p1_ip = new JPanel(new FlowLayout());
        JLabel label = new JLabel("IP      :");
        JPanel p_ip = new JPanel(new BorderLayout());
        p1.add(p1_ip, BorderLayout.NORTH);
        p1_ip.add(label);
        iprText = new JTextField(15);
        p1_ip.add(iprText);
        JPanel p1_port = new JPanel(new FlowLayout());
        JPanel middle_p = new JPanel(new BorderLayout());
        p1.add(middle_p, BorderLayout.CENTER);
        middle_p.add(p1_port, BorderLayout.NORTH);
        label = new JLabel("Port :");
        p1_port.add(label);
        datarText = new JTextField(15);
        p1_port.add(datarText);
        JPanel p2 = new JPanel();
        raddButton = new JButton("Add");
        rremoveButton = new JButton("Remove");
        p2.add(raddButton);
        p2.add(rremoveButton);
        raddButton.addActionListener(this);
        rremoveButton.addActionListener(this);
        p1.add(p2, BorderLayout.SOUTH);
        remotePanel.add(p1, BorderLayout.NORTH);
        TitledBorder titledBorder = new TitledBorder(new EtchedBorder(), "Participants");
        transmitPanel.setBorder(titledBorder);
        return transmitPanel;
    }

    private JPanel createLocalPanel() {
        JPanel transmitPanel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("IP      :");
        JPanel ipPanel = new JPanel(new FlowLayout());
        transmitPanel.add(ipPanel, BorderLayout.NORTH);
        ipPanel.add(label);
        JTextField ipText = new JTextField(15);
        ipPanel.add(ipText);
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            ipText.setText(host);
        } catch (UnknownHostException e) {
        }
        JPanel dataPanel = new JPanel(new FlowLayout());
        transmitPanel.add(dataPanel, BorderLayout.CENTER);
        label = new JLabel("Port :");
        dataPanel.add(label);
        dataText = new JTextField(15);
        dataPanel.add(dataText);
        dataText.setText(config.local_data_port);
        TitledBorder titledBorder = new TitledBorder(new EtchedBorder(), "Local");
        transmitPanel.setBorder(titledBorder);
        return transmitPanel;
    }

    private void removeNonBaseTargets() {
        String localPort = dataText.getText().trim();
        for (int i = targets.size(); i > 0; ) {
            GaraTarget target = (GaraTarget) targets.elementAt(i - 1);
            if (!target.localPort.equals(localPort)) {
                targets.removeElement(target);
            }
            i--;
        }
    }

    public void addTargetToList(String localPort, String ip, String port) {
        TListUpdater listUpdater = new TListUpdater(localPort, ip, port, listModel, targets);
        SwingUtilities.invokeLater(listUpdater);
    }

    public void windowClosing(WindowEvent event) {
        if (processor != null) {
            processor.close();
        }
        rconfig.write();
        config.local_data_port = dataText.getText().trim();
        config.targets = new Vector();
        for (int i = 0; i < targets.size(); i++) {
            GaraTarget target = (GaraTarget) targets.elementAt(i);
            if (target.localPort.equals(config.local_data_port)) {
                config.addTarget(target.ip, target.port);
            }
        }
        config.media_locator = locator.getText().trim();
        config.write();
        System.exit(0);
    }

    public void windowClosed(WindowEvent event) {
    }

    public void windowDeiconified(WindowEvent event) {
    }

    public void windowIconified(WindowEvent event) {
    }

    public void windowActivated(WindowEvent event) {
    }

    public void windowDeactivated(WindowEvent event) {
    }

    public void windowOpened(WindowEvent event) {
    }

    public void keyPressed(KeyEvent event) {
    }

    public void keyReleased(KeyEvent event) {
        Object source = event.getSource();
        if (source == list) {
            int index = list.getSelectedIndex();
        }
    }

    public void keyTyped(KeyEvent event) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
        Object source = e.getSource();
        if (source == rlist) {
            rsetTargetFields();
        }
        if (source == list) {
            int index = list.getSelectedIndex();
            if (index != -1) {
                GaraTarget target = (GaraTarget) targets.elementAt(index);
                iprText.setText(target.ip);
                datarText.setText(target.port);
            }
        }
        int index = list.locationToIndex(e.getPoint());
    }

    public void rsetTargetFields() {
        int rindex = rlist.getSelectedIndex();
        if (rindex != -1) {
            GaraTarget rtarget = (GaraTarget) rtargets.elementAt(rindex);
            riprText.setText(rtarget.ip);
            rdatarText.setText(rtarget.port);
            rdataText.setText(rtarget.localPort);
        }
    }

    public void showErrorMessage(String error) {
        JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        new Garatron();
    }
}

class RTargetListModel extends AbstractListModel {

    private Vector roptions;

    public RTargetListModel(Vector roptions) {
        this.roptions = roptions;
    }

    public int getSize() {
        int rsize;
        if (roptions == null) {
            rsize = 0;
        } else {
            rsize = roptions.size();
        }
        return rsize;
    }

    public Object getElementAt(int rindex) {
        String rname;
        if (rindex < getSize()) {
            GaraTarget o = (GaraTarget) roptions.elementAt(rindex);
            rname = o.localPort + " <--- " + o.ip + ":" + o.port;
        } else {
            rname = null;
        }
        return rname;
    }

    public void setData(Vector rdata) {
        roptions = rdata;
        fireContentsChanged(this, 0, rdata.size());
    }
}

class RListUpdater implements Runnable {

    String rlocalPort, rip, rport;

    RTargetListModel rlistModel;

    Vector rtargets;

    JButton rrremoveButton;

    public RListUpdater(String rlocalPort, String rip, String rport, RTargetListModel rlistModel, Vector rtargets, JButton rrremoveButton) {
        this.rlocalPort = rlocalPort;
        this.rip = rip;
        this.rport = rport;
        this.rlistModel = rlistModel;
        this.rtargets = rtargets;
        this.rrremoveButton = rrremoveButton;
    }

    public void run() {
        GaraTarget rtarget = new GaraTarget(rlocalPort, rip, rport);
        if (!targetExists(rlocalPort, rip, rport)) {
            rtargets.addElement(rtarget);
            rlistModel.setData(rtargets);
            rrremoveButton.setEnabled(true);
        }
    }

    public boolean targetExists(String localPort, String ip, String port) {
        boolean rexists = false;
        for (int i = 0; i < rtargets.size(); i++) {
            GaraTarget rtarget = (GaraTarget) rtargets.elementAt(i);
            if (rtarget.localPort.equals(localPort) && rtarget.ip.equals(ip) && rtarget.port.equals(port)) {
                rexists = true;
                break;
            }
        }
        return rexists;
    }
}

class TTargetListModel extends AbstractListModel {

    private Vector options;

    public TTargetListModel(Vector options) {
        this.options = options;
    }

    public int getSize() {
        int size;
        if (options == null) {
            size = 0;
        } else {
            size = options.size();
        }
        return size;
    }

    public Object getElementAt(int index) {
        String name;
        if (index < getSize()) {
            GaraTarget o = (GaraTarget) options.elementAt(index);
            name = o.localPort + " ---> " + o.ip + ":" + o.port;
        } else {
            name = null;
        }
        return name;
    }

    public void setData(Vector data) {
        options = data;
        fireContentsChanged(this, 0, data.size());
    }
}

class TListUpdater implements Runnable {

    String localPort, ip, port;

    TTargetListModel listModel;

    Vector targets;

    public TListUpdater(String localPort, String ip, String port, TTargetListModel listModel, Vector targets) {
        this.localPort = localPort;
        this.ip = ip;
        this.port = port;
        this.listModel = listModel;
        this.targets = targets;
    }

    public void run() {
        GaraTarget target = new GaraTarget(localPort, ip, port);
        if (!targetExists(localPort, ip, port)) {
            targets.addElement(target);
            listModel.setData(targets);
        }
    }

    public boolean targetExists(String localPort, String ip, String port) {
        boolean exists = false;
        for (int i = 0; i < targets.size(); i++) {
            GaraTarget target = (GaraTarget) targets.elementAt(i);
            if (target.localPort.equals(localPort) && target.ip.equals(ip) && target.port.equals(port)) {
                exists = true;
                break;
            }
        }
        return exists;
    }
}
