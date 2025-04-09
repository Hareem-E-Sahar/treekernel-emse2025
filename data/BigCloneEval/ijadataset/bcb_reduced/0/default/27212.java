import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.IOException;
import java.net.ConnectException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import org.javasock.ICMPHandler;
import org.javasock.ICMPLayer;
import org.javasock.IPHandler;
import org.javasock.IPLayer;
import org.javasock.OSIDataLinkDevice;
import org.javasock.Packet;
import org.javasock.PacketHandler;
import org.javasock.PacketListener;
import org.javasock.PacketStatistics;
import org.javasock.PayloadLayer;
import org.javasock.sockets.TCPSocket;
import org.javasock.TCPHandler;
import org.javasock.TCPLayer;
import org.javasock.UDPHandler;
import org.javasock.UDPLayer;

public class JSScan implements ActionListener, Runnable {

    private boolean bScanRunning = false;

    private boolean bStopScan = false;

    private Thread scanThread = null;

    private JFrame jsscanFrame = null;

    private JList deviceList = null;

    private JTextField sourceSpoofAddressTF = null;

    private JTextField localPortTF = null;

    private JTextField sourceSpoofMACAddressTF = null;

    private JTextField targetHostnameTF = null;

    private JTextField targetPortsTF = null;

    private JCheckBox randomPortsCB = null;

    private JRadioButton tcpHalfOpenScanRB = null;

    private JRadioButton tcpConnectScanRB = null;

    private JRadioButton udpScanRB = null;

    private JCheckBox sendRSTOnHalfOpenCB = null;

    private ButtonGroup scanTypeGroup = null;

    private JTextField timeoutTF = null;

    private JLabel numRetriesLabel = null;

    private JTextField numRetriesTF = null;

    private JTextField scanDelayTF = null;

    private JButton startScanButton = null;

    private JButton stopScanButton = null;

    private JProgressBar progressBar;

    private JButton clearTableButton = null;

    private JButton exitButton = null;

    private JTable resultsTable = null;

    private JCheckBox displayOpenPortsCB = null;

    private JCheckBox displayClosedPortsCB = null;

    private JCheckBox displayFilteredPortsCB = null;

    private JCheckBox displayOpenOrFilteredPortsCB = null;

    private OSIDataLinkDevice[] dataLinkDevices = null;

    private OSIDataLinkDevice selectedDevice = null;

    private String sourceSpoofAddress = "";

    private InetAddress sourceAddress = null;

    private int localPort = 1000;

    private String sourceSpoofMACAddress = "";

    private String targetHostname = "";

    private InetAddress targetAddress = null;

    private PortList targetPorts = null;

    private boolean bRandomPorts = true;

    private static final int TCP_HALF_OPEN_SCAN = 1;

    private static final int TCP_CONNECT_SCAN = 2;

    private static final int UDP_SCAN = 3;

    private int scanType = TCP_HALF_OPEN_SCAN;

    private boolean bSendRSTOnHalfOpen = false;

    private int timeout = 100;

    private int numRetries = 10;

    private int scanDelay = 0;

    private ScanResultsTableModel resultsTableModel = null;

    public static final int PORT_CLOSED = 0;

    public static final int PORT_OPEN = 1;

    public static final int PORT_FILTERED = 2;

    public static final int PORT_OPEN_OR_FILTERED = 3;

    public static final int UNKNOWN_PORT_STATUS = 4;

    public static final String[] PORT_STATE = { "Closed", "Open", "Filtered", "Open|Filtered", "Unknown" };

    private int icmpCode = -1;

    private int icmpType = -1;

    private int icmpIPLayerDestinationPort = -1;

    private int udpSourcePort = -1;

    private Hashtable udpServices = new Hashtable();

    private Hashtable tcpServices = new Hashtable();

    public static void main(String[] argsI) {
        System.err.println(" ________________________________________________________ ");
        System.err.println("|                                                        |");
        System.err.println("|  JSScan                                                |");
        System.err.println("|  A sample port scanner which uses the JavaSock API.    |");
        System.err.println("|                                                        |");
        System.err.println("|  Copyright (c) 2006, 2007 Creare Inc.                  |");
        System.err.println("|  All rights reserved.                                  |");
        System.err.println("|                                                        |");
        System.err.println("|  Contract No.:  W15P7T-06-C-S202                       |");
        System.err.println("|  Contractor Name:  Creare Incorporated                 |");
        System.err.println("|  Contractor Address:  P.O. Box 71, Hanover, NH  03755  |");
        System.err.println("|  Expiration of SBIR Data Rights Period:  13 June 2010  |");
        System.err.println("|                                                        |");
        System.err.println("|  The Government's rights to use, modify, reproduce,    |");
        System.err.println("|  release, perform, display, or disclose technical data |");
        System.err.println("|  or computer software marked with this legend are      |");
        System.err.println("|  restricted during the period shown as provided in     |");
        System.err.println("|  paragraph (b) (4) of the Rights in Noncommercial      |");
        System.err.println("|  Technical Data and Computer Software--Small Business  |");
        System.err.println("|  Innovative Research (SBIR) Program clause contained   |");
        System.err.println("|  in the above identified contract.  No restrictions    |");
        System.err.println("|  apply after the expiration date shown above.  Any     |");
        System.err.println("|  reproduction of technical data, computer software, or |");
        System.err.println("|  portions thereof marked with this legend must also    |");
        System.err.println("|  reproduce the markings.                               |");
        System.err.println("|                                                        |");
        System.err.println("|________________________________________________________|");
        final JSScan jsscan = new JSScan(argsI);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                jsscan.createAndShowGUI();
            }
        });
    }

    public JSScan(String[] argsI) {
        loadDataLinkDevices();
        try {
            sourceAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException uhe) {
            JOptionPane.showMessageDialog(null, "Error: could not obtain local host address.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        loadServiceNames();
    }

    private void loadServiceNames() {
        try {
            java.io.InputStream inStr = null;
            ClassLoader cl = null;
            try {
                cl = ClassLoader.getSystemClassLoader();
            } catch (java.lang.NoClassDefFoundError e) {
            } catch (java.lang.NoSuchMethodError e) {
            }
            if (cl != null) {
                inStr = cl.getResourceAsStream("nmap-services");
            }
            if (inStr == null) {
                String classpath = System.getProperty("java.class.path");
                if (classpath == null) {
                    classpath = new String("");
                }
                java.util.StringTokenizer st = new java.util.StringTokenizer(classpath, ":");
                String path;
                java.io.File file;
                while (st.hasMoreTokens()) {
                    path = st.nextToken();
                    file = new java.io.File(path, "/nmap-services");
                    if (file.exists()) {
                        inStr = new java.io.FileInputStream(file);
                        break;
                    }
                }
                if (inStr == null) {
                    file = new java.io.File(".", "/nmap-services");
                    if (file.exists()) {
                        inStr = new java.io.FileInputStream(file);
                    }
                }
            }
            if (inStr != null) {
                java.io.InputStreamReader inStrReader = new java.io.InputStreamReader(inStr);
                java.io.BufferedReader bRead = new java.io.BufferedReader(inStrReader);
                String line;
                while ((line = bRead.readLine()) != null) {
                    int idx;
                    line = line.trim();
                    if ((line.length() == 0) || (line.charAt(0) == '#')) {
                        continue;
                    }
                    String[] toks = line.split("\\s+");
                    if (toks.length < 2) {
                        continue;
                    }
                    String serviceName = toks[0];
                    Integer port = new Integer(0);
                    String protocol = "";
                    String[] portAndProtocol = toks[1].split("/");
                    if (portAndProtocol.length != 2) {
                        continue;
                    }
                    try {
                        port = Integer.decode(portAndProtocol[0]);
                    } catch (NumberFormatException nfe) {
                        System.err.println("Unparsable port: " + portAndProtocol[0]);
                        continue;
                    }
                    protocol = portAndProtocol[1].trim();
                    if (protocol.equalsIgnoreCase("tcp")) {
                        tcpServices.put(port, serviceName);
                    } else if (protocol.equalsIgnoreCase("udp")) {
                        udpServices.put(port, serviceName);
                    } else {
                        System.err.println("Unrecognized protocol in line: \"" + line + "\"");
                    }
                }
                if (inStr instanceof java.io.FileInputStream) {
                    inStr.close();
                }
            }
        } catch (java.lang.Exception e) {
        } catch (java.lang.Error e) {
        }
    }

    private String getServiceName(int portI) {
        Integer intObj = new Integer(portI);
        if (scanType == UDP_SCAN) {
            String serviceStr = (String) udpServices.get(intObj);
            if (serviceStr != null) {
                return serviceStr;
            }
        } else {
            String serviceStr = (String) tcpServices.get(intObj);
            if (serviceStr != null) {
                return serviceStr;
            }
        }
        return new String("N/A");
    }

    private void createAndShowGUI() {
        JFrame.setDefaultLookAndFeelDecorated(true);
        jsscanFrame = new JFrame("JSScan");
        GridBagLayout gbl = new GridBagLayout();
        JPanel guiPanel = new JPanel(gbl);
        if ((dataLinkDevices == null) || (dataLinkDevices.length == 0)) {
            DefaultListModel defaultListModel = new DefaultListModel();
            deviceList = new JList(defaultListModel);
        } else {
            deviceList = new JList(dataLinkDevices);
            deviceList.setSelectedIndex(0);
        }
        deviceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sourceSpoofAddressTF = new JTextField(sourceSpoofAddress, 15);
        sourceSpoofAddressTF.setEnabled(false);
        localPortTF = new JTextField(Integer.toString(localPort), 5);
        localPortTF.setToolTipText("<HTML><BODY>Local port to bind to.<BR>" + "Integer value greater than zero.</BODY></HTML>");
        sourceSpoofMACAddressTF = new JTextField(sourceSpoofMACAddress, 15);
        sourceSpoofMACAddressTF.setEnabled(false);
        targetHostnameTF = new JTextField(targetHostname, 15);
        targetHostnameTF.setToolTipText("Target hostname or IP address.");
        targetPortsTF = new JTextField(15);
        targetPortsTF.setToolTipText("<HTML><BODY>Comma-delimited port list.<BR>" + "Example: 10, 100, 300-450, 500-1000</BODY></HTML>");
        randomPortsCB = new JCheckBox("Random order?", bRandomPorts);
        randomPortsCB.setToolTipText("Probe the ports in random order?");
        tcpHalfOpenScanRB = new JRadioButton("Half open (SYN)", (scanType == TCP_HALF_OPEN_SCAN));
        tcpHalfOpenScanRB.addActionListener(this);
        tcpConnectScanRB = new JRadioButton("TCP connect", (scanType == TCP_CONNECT_SCAN));
        tcpConnectScanRB.addActionListener(this);
        udpScanRB = new JRadioButton("UDP", (scanType == UDP_SCAN));
        udpScanRB.addActionListener(this);
        scanTypeGroup = new ButtonGroup();
        scanTypeGroup.add(tcpHalfOpenScanRB);
        scanTypeGroup.add(tcpConnectScanRB);
        scanTypeGroup.add(udpScanRB);
        sendRSTOnHalfOpenCB = new JCheckBox("Send RST on half-open?", bSendRSTOnHalfOpen);
        sendRSTOnHalfOpenCB.setToolTipText("<HTML><BODY>When an ACK is received in response to a<BR>" + "half-open's SYN probe, follow up with a RST?<BR>" + "Only applies to half-open scans.</BODY></HTML>");
        sendRSTOnHalfOpenCB.setEnabled(scanType == TCP_HALF_OPEN_SCAN);
        timeoutTF = new JTextField(Integer.toString(timeout), 8);
        timeoutTF.setToolTipText("<HTML><BODY>" + "Timeout (msec) to wait for a response from a port.<BR>" + "Integer value greater than zero." + "</BODY></HTML>");
        numRetriesLabel = new JLabel("Number of retries");
        numRetriesLabel.setEnabled(scanType != TCP_CONNECT_SCAN);
        numRetriesTF = new JTextField(Integer.toString(numRetries), 8);
        numRetriesTF.setEnabled(scanType != TCP_CONNECT_SCAN);
        numRetriesTF.setToolTipText("<HTML><BODY>" + "Number of times to retry if a probe times out.<BR>" + "NOTE: This value isn't used for a connect scan.<BR>" + "Integer value greater than or equal to zero." + "</BODY></HTML>");
        scanDelayTF = new JTextField(Integer.toString(scanDelay), 8);
        scanDelayTF.setToolTipText("<HTML><BODY>" + "Amount of time (msec) between probes either to the same<BR>" + "port (when retrying) or to a different port. If the value<BR>" + "is less than the timeout, and if a timeout occurs, then the<BR>" + "next probe will be sent immediately. Note that probes are<BR>" + "sent <U>sequentially</U>, not in parallel.<BR>" + "Integer value greater than or equal to zero." + "</BODY></HTML>");
        resultsTableModel = new ScanResultsTableModel();
        resultsTable = new JTable(resultsTableModel);
        resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsTableModel.setPreferredColumnSizes(resultsTable);
        startScanButton = new JButton("Start scan");
        startScanButton.addActionListener(this);
        stopScanButton = new JButton("Stop scan");
        stopScanButton.addActionListener(this);
        stopScanButton.setEnabled(false);
        progressBar = new JProgressBar();
        clearTableButton = new JButton("Clear table");
        clearTableButton.addActionListener(this);
        exitButton = new JButton("Exit");
        exitButton.addActionListener(this);
        displayOpenPortsCB = new JCheckBox("Open", true);
        displayOpenPortsCB.addActionListener(this);
        displayOpenPortsCB.setToolTipText("<HTML><BODY>" + "Display open ports in the table." + "</BODY></HTML>");
        displayClosedPortsCB = new JCheckBox("Closed", true);
        displayClosedPortsCB.addActionListener(this);
        displayClosedPortsCB.setToolTipText("<HTML><BODY>" + "Display closed ports in the table." + "</BODY></HTML>");
        displayFilteredPortsCB = new JCheckBox("Filtered", true);
        displayFilteredPortsCB.addActionListener(this);
        displayFilteredPortsCB.setToolTipText("<HTML><BODY>" + "Display filtered ports in the table.<BR>" + "Filtered ports are those ports where packet filtering<BR>" + "(for example, from a firewall) is preventing our probe<BR>" + "from determining if the port is open." + "</BODY></HTML>");
        displayOpenOrFilteredPortsCB = new JCheckBox("Open|Filtered", true);
        displayOpenOrFilteredPortsCB.addActionListener(this);
        displayOpenOrFilteredPortsCB.setToolTipText("<HTML><BODY>" + "Display ports that are either Open or Filtered (UDP scans only).<BR>" + "An Open|Filtered port is one where either the port is open<BR>" + "(but not giving any response) or filtered (and therefore not<BR>" + "giving any response).  Only occurs for UDP scans." + "</BODY></HTML>");
        jsscanFrame.setFont(new Font("Dialog", Font.PLAIN, 12));
        guiPanel.setFont(new Font("Dialog", Font.PLAIN, 12));
        int row = 0;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        GridBagLayout pcgbl = new GridBagLayout();
        JPanel containerPanel = new JPanel(pcgbl);
        GridBagConstraints pcgbc = new GridBagConstraints();
        pcgbc.anchor = GridBagConstraints.WEST;
        pcgbc.fill = GridBagConstraints.NONE;
        pcgbc.weightx = 0;
        pcgbc.weighty = 0;
        GridBagLayout panelgbl = new GridBagLayout();
        JPanel sourceInfoPanel = new JPanel(panelgbl);
        sourceInfoPanel.setBorder(BorderFactory.createTitledBorder("Source"));
        GridBagConstraints panelgbc = new GridBagConstraints();
        int panelrow = 0;
        panelgbc.anchor = GridBagConstraints.WEST;
        panelgbc.fill = GridBagConstraints.NONE;
        panelgbc.weightx = 0;
        panelgbc.weighty = 0;
        JLabel label = new JLabel("OSI Data Link Device");
        panelgbc.insets = new Insets(5, 5, 0, 5);
        add(sourceInfoPanel, label, panelgbl, panelgbc, 0, panelrow, 2, 1);
        ++panelrow;
        JScrollPane scrollPane = new JScrollPane(deviceList);
        scrollPane.setPreferredSize(new Dimension(200, 75));
        panelgbc.insets = new Insets(0, 5, 0, 5);
        panelgbc.fill = GridBagConstraints.HORIZONTAL;
        add(sourceInfoPanel, scrollPane, panelgbl, panelgbc, 0, panelrow, 2, 1);
        panelgbc.fill = GridBagConstraints.NONE;
        ++panelrow;
        label = new JLabel("Spoof address");
        label.setEnabled(false);
        panelgbc.insets = new Insets(5, 5, 0, 5);
        add(sourceInfoPanel, label, panelgbl, panelgbc, 0, panelrow, 1, 1);
        panelgbc.insets = new Insets(5, 0, 0, 5);
        add(sourceInfoPanel, sourceSpoofAddressTF, panelgbl, panelgbc, 1, panelrow, 1, 1);
        ++panelrow;
        label = new JLabel("Port");
        panelgbc.insets = new Insets(5, 5, 0, 5);
        add(sourceInfoPanel, label, panelgbl, panelgbc, 0, panelrow, 1, 1);
        panelgbc.insets = new Insets(5, 0, 0, 5);
        add(sourceInfoPanel, localPortTF, panelgbl, panelgbc, 1, panelrow, 1, 1);
        ++panelrow;
        label = new JLabel("Spoof MAC address");
        label.setEnabled(false);
        panelgbc.insets = new Insets(5, 5, 0, 5);
        add(sourceInfoPanel, label, panelgbl, panelgbc, 0, panelrow, 1, 1);
        panelgbc.insets = new Insets(5, 0, 5, 5);
        add(sourceInfoPanel, sourceSpoofMACAddressTF, panelgbl, panelgbc, 1, panelrow, 1, 1);
        ++panelrow;
        pcgbc.insets = new Insets(0, 0, 0, 15);
        add(containerPanel, sourceInfoPanel, pcgbl, pcgbc, 0, 0, 1, 1);
        panelgbl = new GridBagLayout();
        JPanel targetInfoPanel = new JPanel(panelgbl);
        targetInfoPanel.setBorder(BorderFactory.createTitledBorder("Target"));
        panelgbc = new GridBagConstraints();
        panelrow = 0;
        panelgbc.anchor = GridBagConstraints.WEST;
        panelgbc.fill = GridBagConstraints.NONE;
        panelgbc.weightx = 0;
        panelgbc.weighty = 0;
        label = new JLabel("Hostname");
        panelgbc.insets = new Insets(5, 5, 0, 5);
        add(targetInfoPanel, label, panelgbl, panelgbc, 0, panelrow, 1, 1);
        panelgbc.insets = new Insets(5, 0, 0, 5);
        add(targetInfoPanel, targetHostnameTF, panelgbl, panelgbc, 1, panelrow, 1, 1);
        ++panelrow;
        label = new JLabel("Port list");
        panelgbc.insets = new Insets(5, 5, 0, 5);
        add(targetInfoPanel, label, panelgbl, panelgbc, 0, panelrow, 1, 1);
        panelgbc.insets = new Insets(5, 0, 0, 5);
        add(targetInfoPanel, targetPortsTF, panelgbl, panelgbc, 1, panelrow, 1, 1);
        ++panelrow;
        panelgbc.insets = new Insets(5, 20, 5, 5);
        add(targetInfoPanel, randomPortsCB, panelgbl, panelgbc, 0, panelrow, 2, 1);
        pcgbc.insets = new Insets(0, 0, 0, 0);
        add(containerPanel, targetInfoPanel, pcgbl, pcgbc, 1, 0, 1, 1);
        gbc.insets = new Insets(15, 15, 0, 15);
        add(guiPanel, containerPanel, gbl, gbc, 0, row, 1, 1);
        ++row;
        pcgbl = new GridBagLayout();
        containerPanel = new JPanel(pcgbl);
        pcgbc = new GridBagConstraints();
        pcgbc.anchor = GridBagConstraints.WEST;
        pcgbc.fill = GridBagConstraints.NONE;
        pcgbc.weightx = 0;
        pcgbc.weighty = 0;
        panelgbl = new GridBagLayout();
        JPanel scanParamsPanel = new JPanel(panelgbl);
        scanParamsPanel.setBorder(BorderFactory.createTitledBorder("Scan parameters"));
        panelgbc = new GridBagConstraints();
        panelrow = 0;
        panelgbc.anchor = GridBagConstraints.WEST;
        panelgbc.fill = GridBagConstraints.NONE;
        panelgbc.weightx = 0;
        panelgbc.weighty = 0;
        JPanel rbPanel = new JPanel();
        rbPanel.add(tcpHalfOpenScanRB);
        rbPanel.add(tcpConnectScanRB);
        rbPanel.add(udpScanRB);
        panelgbc.insets = new Insets(0, 5, 0, 5);
        add(scanParamsPanel, rbPanel, panelgbl, panelgbc, 0, panelrow, 3, 1);
        ++panelrow;
        panelgbc.insets = new Insets(0, 5, 0, 5);
        add(scanParamsPanel, sendRSTOnHalfOpenCB, panelgbl, panelgbc, 0, panelrow, 3, 1);
        ++panelrow;
        label = new JLabel("Timeout");
        panelgbc.insets = new Insets(5, 5, 0, 5);
        add(scanParamsPanel, label, panelgbl, panelgbc, 0, panelrow, 1, 1);
        panelgbc.insets = new Insets(5, 0, 0, 5);
        add(scanParamsPanel, timeoutTF, panelgbl, panelgbc, 1, panelrow, 1, 1);
        label = new JLabel("msec");
        panelgbc.insets = new Insets(5, 0, 0, 5);
        add(scanParamsPanel, label, panelgbl, panelgbc, 2, panelrow, 1, 1);
        ++panelrow;
        panelgbc.insets = new Insets(5, 5, 0, 5);
        add(scanParamsPanel, numRetriesLabel, panelgbl, panelgbc, 0, panelrow, 1, 1);
        panelgbc.insets = new Insets(5, 0, 0, 5);
        add(scanParamsPanel, numRetriesTF, panelgbl, panelgbc, 1, panelrow, 1, 1);
        ++panelrow;
        label = new JLabel("Delay between probes");
        panelgbc.insets = new Insets(5, 5, 0, 5);
        add(scanParamsPanel, label, panelgbl, panelgbc, 0, panelrow, 1, 1);
        panelgbc.insets = new Insets(5, 0, 0, 5);
        add(scanParamsPanel, scanDelayTF, panelgbl, panelgbc, 1, panelrow, 1, 1);
        label = new JLabel("msec");
        panelgbc.insets = new Insets(5, 0, 0, 5);
        add(scanParamsPanel, label, panelgbl, panelgbc, 2, panelrow, 1, 1);
        ++panelrow;
        pcgbc.insets = new Insets(0, 0, 0, 15);
        add(containerPanel, scanParamsPanel, pcgbl, pcgbc, 0, 0, 1, 1);
        panelgbl = new GridBagLayout();
        JPanel controlPanel = new JPanel(panelgbl);
        panelgbc = new GridBagConstraints();
        panelrow = 0;
        panelgbc.anchor = GridBagConstraints.CENTER;
        panelgbc.fill = GridBagConstraints.NONE;
        panelgbc.weightx = 0;
        panelgbc.weighty = 0;
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        buttonPanel.add(startScanButton);
        buttonPanel.add(stopScanButton);
        panelgbc.insets = new Insets(0, 0, 5, 0);
        add(controlPanel, buttonPanel, panelgbl, panelgbc, 0, 0, 1, 1);
        panelgbc.insets = new Insets(0, 0, 5, 0);
        panelgbc.fill = GridBagConstraints.HORIZONTAL;
        add(controlPanel, progressBar, panelgbl, panelgbc, 0, 1, 1, 1);
        buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        buttonPanel.add(clearTableButton);
        buttonPanel.add(exitButton);
        panelgbc.insets = new Insets(0, 0, 0, 0);
        add(controlPanel, buttonPanel, panelgbl, panelgbc, 0, 2, 1, 1);
        pcgbc.insets = new Insets(0, 0, 0, 15);
        add(containerPanel, controlPanel, pcgbl, pcgbc, 1, 0, 1, 1);
        Box viewOptionsBox = Box.createVerticalBox();
        viewOptionsBox.setBorder(BorderFactory.createTitledBorder("Display"));
        viewOptionsBox.add(displayOpenPortsCB);
        viewOptionsBox.add(displayClosedPortsCB);
        viewOptionsBox.add(displayFilteredPortsCB);
        viewOptionsBox.add(displayOpenOrFilteredPortsCB);
        panelgbc.insets = new Insets(0, 0, 0, 0);
        add(containerPanel, viewOptionsBox, pcgbl, pcgbc, 2, 0, 1, 1);
        gbc.insets = new Insets(15, 15, 0, 15);
        add(guiPanel, containerPanel, gbl, gbc, 0, row, 1, 1);
        ++row;
        JScrollPane tableScrollPane = new JScrollPane(resultsTable);
        tableScrollPane.setPreferredSize(new Dimension(resultsTableModel.getPreferredWidth(resultsTable) + 20, 200));
        tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.weightx = 100;
        gbc.weighty = 100;
        add(guiPanel, tableScrollPane, gbl, gbc, 0, row, 1, 1);
        gbl = new GridBagLayout();
        jsscanFrame.getContentPane().setLayout(gbl);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 100;
        gbc.weighty = 100;
        gbc.insets = new Insets(0, 0, 0, 0);
        add(jsscanFrame.getContentPane(), guiPanel, gbl, gbc, 0, 0, 1, 1);
        jsscanFrame.pack();
        jsscanFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        jsscanFrame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                exit();
            }
        });
        jsscanFrame.setVisible(true);
    }

    public static void add(Container container, Component c, GridBagLayout gbl, GridBagConstraints gbc, int x, int y, int w, int h) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = w;
        gbc.gridheight = h;
        gbl.setConstraints(c, gbc);
        container.add(c);
    }

    public void actionPerformed(ActionEvent eventI) {
        Object source = eventI.getSource();
        if (source == null) return;
        if (source == startScanButton) {
            startScan();
        } else if (source == stopScanButton) {
            stopScan();
        } else if (source == clearTableButton) {
            resultsTableModel.clearTable();
        } else if (source == exitButton) {
            exit();
        } else if (source == tcpHalfOpenScanRB) {
            scanType = TCP_HALF_OPEN_SCAN;
            numRetriesLabel.setEnabled(true);
            numRetriesTF.setEnabled(true);
            sendRSTOnHalfOpenCB.setEnabled(true);
        } else if (source == tcpConnectScanRB) {
            scanType = TCP_CONNECT_SCAN;
            numRetriesLabel.setEnabled(false);
            numRetriesTF.setEnabled(false);
            sendRSTOnHalfOpenCB.setEnabled(false);
        } else if (source == udpScanRB) {
            scanType = UDP_SCAN;
            numRetriesLabel.setEnabled(true);
            numRetriesTF.setEnabled(true);
            sendRSTOnHalfOpenCB.setEnabled(false);
        } else if (source == displayOpenPortsCB) {
            if (resultsTableModel != null) {
                resultsTableModel.displayOpenPorts(displayOpenPortsCB.isSelected());
            }
        } else if (source == displayClosedPortsCB) {
            if (resultsTableModel != null) {
                resultsTableModel.displayClosedPorts(displayClosedPortsCB.isSelected());
            }
        } else if (source == displayFilteredPortsCB) {
            if (resultsTableModel != null) {
                resultsTableModel.displayFilteredPorts(displayFilteredPortsCB.isSelected());
            }
        } else if (source == displayOpenOrFilteredPortsCB) {
            if (resultsTableModel != null) {
                resultsTableModel.displayOpenOrFilteredPorts(displayOpenOrFilteredPortsCB.isSelected());
            }
        }
    }

    private void startScan() {
        if (bScanRunning) {
            JOptionPane.showMessageDialog(jsscanFrame, "A scan is already running.", "Scan running", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if ((dataLinkDevices == null) || (dataLinkDevices.length == 0) || (deviceList.getSelectedValue() == null)) {
            JOptionPane.showMessageDialog(jsscanFrame, "You must select an OSI data link device", "Device error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Scan not started: no device selected");
            refreshDataLinkDevices();
            return;
        }
        selectedDevice = (OSIDataLinkDevice) deviceList.getSelectedValue();
        System.err.println("Selected OSI data link device: " + selectedDevice);
        String localPortStr = localPortTF.getText().trim();
        try {
            localPort = Integer.parseInt(localPortStr);
            if (localPort <= 0) {
                throw new NumberFormatException("");
            }
            System.err.println("Source port: " + localPort);
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(jsscanFrame, "The source port must be an integer value greater than zero.", "Source port error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Scan not started: invalid source port");
            return;
        }
        targetHostname = targetHostnameTF.getText().trim();
        if (targetHostname.equals("")) {
            JOptionPane.showMessageDialog(jsscanFrame, "Must enter a target hostname.", "Target hostname error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Scan not started: invalid target hostname");
            return;
        }
        try {
            if (targetHostname.toUpperCase().equals("LOCALHOST")) {
                targetAddress = InetAddress.getLocalHost();
            } else {
                targetAddress = InetAddress.getByName(targetHostname);
            }
            System.err.println("Target address: " + targetAddress);
        } catch (UnknownHostException uhe) {
            JOptionPane.showMessageDialog(jsscanFrame, "IP address could not be found for the given target hostname.", "Target hostname error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Scan not started: IP address not found for target hostname");
            return;
        }
        String portsText = targetPortsTF.getText().trim();
        bRandomPorts = randomPortsCB.isSelected();
        targetPorts = new PortList(portsText, bRandomPorts, false);
        if (targetPorts.isEmpty()) {
            JOptionPane.showMessageDialog(jsscanFrame, "You must enter one or more ports in the target port list", "Port error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (scanType == TCP_HALF_OPEN_SCAN) {
            System.err.println("Scan type: Half open (SYN)");
        } else if (scanType == TCP_CONNECT_SCAN) {
            System.err.println("Scan type: TCP connect");
        } else if (scanType == UDP_SCAN) {
            System.err.println("Scan type: UDP");
        }
        bSendRSTOnHalfOpen = sendRSTOnHalfOpenCB.isSelected();
        String timeoutStr = timeoutTF.getText().trim();
        try {
            timeout = Integer.parseInt(timeoutStr);
            if (timeout <= 0) {
                throw new NumberFormatException("");
            }
            System.err.println("Timeout: " + timeout + " msec");
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(jsscanFrame, "Timeout must be an integer value greater than zero.", "Timeout error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Scan not started: invalid timeout");
            return;
        }
        String numRetriesStr = numRetriesTF.getText().trim();
        try {
            numRetries = Integer.parseInt(numRetriesStr);
            if (numRetries < 0) {
                throw new NumberFormatException("");
            }
            System.err.println("Number of retries: " + numRetries);
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(jsscanFrame, "Number of retries must be an integer value greater than or equal to zero.", "Number of retries error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Scan not started: invalid number of retries");
            return;
        }
        String scanDelayStr = scanDelayTF.getText().trim();
        try {
            scanDelay = Integer.parseInt(scanDelayStr);
            if (scanDelay < 0) {
                throw new NumberFormatException("");
            }
            System.err.println("Scan delay: " + scanDelay + " msec");
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(jsscanFrame, "Scan delay must be an integer value greater than or equal to zero.", "Scan delay error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Scan not started: invalid scan delay");
            return;
        }
        enableGUI(false);
        bStopScan = false;
        scanThread = new Thread(this);
        scanThread.start();
        bScanRunning = true;
        progressBar.setIndeterminate(bScanRunning);
    }

    private void stopScan() {
        bStopScan = true;
        if (scanThread != null) {
            try {
                scanThread.join(10000);
            } catch (InterruptedException ie) {
            }
        }
        if (bScanRunning) {
            System.err.println("Problem stopping scan");
        } else {
            System.err.println("Scan stopped");
        }
        enableGUI(true);
        progressBar.setIndeterminate(bScanRunning);
    }

    private void exit() {
        System.err.println("Exiting JSScan...");
        if (bScanRunning) {
            stopScan();
        }
        jsscanFrame.setVisible(false);
        System.exit(0);
    }

    private void enableGUI(boolean bEnableI) {
        deviceList.setEnabled(bEnableI);
        localPortTF.setEnabled(bEnableI);
        targetHostnameTF.setEnabled(bEnableI);
        targetPortsTF.setEnabled(bEnableI);
        randomPortsCB.setEnabled(bEnableI);
        tcpHalfOpenScanRB.setEnabled(bEnableI);
        tcpConnectScanRB.setEnabled(bEnableI);
        udpScanRB.setEnabled(bEnableI);
        sendRSTOnHalfOpenCB.setEnabled((bEnableI) && (scanType == TCP_HALF_OPEN_SCAN));
        timeoutTF.setEnabled(bEnableI);
        numRetriesTF.setEnabled((bEnableI) && (scanType != TCP_CONNECT_SCAN));
        scanDelayTF.setEnabled(bEnableI);
        startScanButton.setEnabled(bEnableI);
        stopScanButton.setEnabled(!bEnableI);
        clearTableButton.setEnabled(bEnableI);
    }

    public void run() {
        try {
            if ((scanType == TCP_CONNECT_SCAN) || (scanType == TCP_HALF_OPEN_SCAN)) {
                tcpScan();
            } else if (scanType == UDP_SCAN) {
                udpScan();
            } else {
                throw new IOException("Unknown scan type");
            }
        } catch (IOException ioe) {
            String exceptionMsg = new String("Caught exception doing scan:\n" + ioe);
            System.err.println(exceptionMsg);
            ioe.printStackTrace();
            JOptionPane.showMessageDialog(jsscanFrame, exceptionMsg, "Scan Error", JOptionPane.ERROR_MESSAGE);
        }
        System.err.println("Scan thread is exiting.");
        if (!bStopScan) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    stopScan();
                }
            });
        }
        bScanRunning = false;
    }

    public void tcpScan() throws IOException {
        Packet packet = selectedDevice.createPacket();
        IPLayer ipLayer = new IPLayer(sourceAddress, targetAddress);
        packet.addLayer(ipLayer);
        packet.addLayer(new TCPLayer());
        TCPSocket tcpSock = new TCPSocket(selectedDevice, null, packet);
        tcpSock.bind(null, localPort);
        if (scanType == TCP_HALF_OPEN_SCAN) {
            TCPHandler tcpHandler = tcpSock.getTCPHandler();
            IPHandler ipHandler = (IPHandler) tcpHandler.getParent();
            ICMPHandler icmpHandler = new ICMPHandler(ipHandler);
            icmpHandler.addPacketListener(new ICMPPacketListener());
        }
        selectedDevice.startCapture();
        try {
            for (Iterator iter = targetPorts.iterator(); iter.hasNext(); ) {
                if (bStopScan) {
                    break;
                }
                int port = ((Port) iter.next()).getPort();
                int answer = UNKNOWN_PORT_STATUS;
                for (int i = 0; i <= numRetries; ++i) {
                    if (bStopScan) {
                        break;
                    }
                    long startTime = System.currentTimeMillis();
                    if (scanType == TCP_HALF_OPEN_SCAN) {
                        answer = halfOpenProbe(tcpSock, port);
                    } else if (scanType == TCP_CONNECT_SCAN) {
                        answer = tcpConnectProbe(tcpSock, port);
                    }
                    System.err.println("Port: " + port + ", Try #: " + i + ", Answer: " + PORT_STATE[answer]);
                    if (answer != UNKNOWN_PORT_STATUS) {
                        resultsTableModel.addToTable(resultsTable, port, PORT_STATE[answer], getServiceName(port));
                    }
                    long endTime = System.currentTimeMillis();
                    long deltaTime = endTime - startTime;
                    if (deltaTime < scanDelay) {
                        try {
                            Thread.currentThread().sleep(scanDelay - deltaTime);
                        } catch (Exception e) {
                        }
                    }
                    if (answer != UNKNOWN_PORT_STATUS) {
                        break;
                    }
                }
                if (answer == UNKNOWN_PORT_STATUS) {
                    if (scanType == TCP_HALF_OPEN_SCAN) {
                        resultsTableModel.addToTable(resultsTable, port, PORT_STATE[PORT_FILTERED], getServiceName(port));
                    } else if (scanType == TCP_CONNECT_SCAN) {
                        resultsTableModel.addToTable(resultsTable, port, PORT_STATE[PORT_CLOSED], getServiceName(port));
                    }
                }
            }
        } finally {
            System.err.println("\n\nClosing the TCPSocket and stopping capture.\n\n");
            tcpSock.close();
            selectedDevice.stopCapture();
        }
    }

    public int halfOpenProbe(TCPSocket sockI, int portI) throws IOException {
        icmpCode = -1;
        icmpType = -1;
        icmpIPLayerDestinationPort = -1;
        sockI.getTCPLayer().setDestinationPort(portI);
        sockI.sendSYN(false);
        boolean bGotResponse = sockI.waitForACK(timeout);
        int response = UNKNOWN_PORT_STATUS;
        if (sockI.isReset()) {
            response = PORT_CLOSED;
        } else if (bGotResponse) {
            if (bSendRSTOnHalfOpen) {
                System.err.println("Send RST");
                sockI.sendRST();
            }
            return PORT_OPEN;
        }
        if ((icmpIPLayerDestinationPort == portI) && (icmpType == ICMPLayer.TYPE_DESTINATION_UNREACHABLE)) {
            if ((icmpCode == 1) || (icmpCode == 2) || (icmpCode == 3) || (icmpCode == 9) || (icmpCode == 10) || (icmpCode == 13)) {
                return PORT_FILTERED;
            }
        } else if ((icmpIPLayerDestinationPort != portI) && (icmpType == ICMPLayer.TYPE_DESTINATION_UNREACHABLE)) {
            System.err.println("TCP probe: received ICMP packet containing a different remote port (" + icmpIPLayerDestinationPort + ") than the port we are currently examining (" + portI + ")");
        }
        return response;
    }

    public int tcpConnectProbe(TCPSocket sockI, int portI) throws IOException {
        try {
            sockI.connect(targetAddress, portI, timeout);
            sockI.sendRST();
            return PORT_OPEN;
        } catch (ConnectException ce) {
            System.err.println("TCP connect: port " + portI + " unreachable: " + ce.getMessage());
            return PORT_CLOSED;
        }
    }

    public void udpScan() throws IOException {
        Packet packet = selectedDevice.createPacket();
        IPLayer ipLayer = new IPLayer(sourceAddress, targetAddress);
        packet.addLayer(ipLayer);
        DatagramSocket ds = new DatagramSocket(localPort, sourceAddress);
        int tempLocalPort = ds.getLocalPort();
        if (tempLocalPort != localPort) {
            localPort = tempLocalPort;
            System.err.println("The local port was remapped to " + localPort);
        }
        UDPLayer udpl = new UDPLayer(localPort, 0);
        packet.addLayer(udpl);
        PayloadLayer pl = new PayloadLayer();
        packet.addLayer(pl);
        IPHandler iph = new IPHandler(selectedDevice);
        UDPHandler udph = new UDPHandler(iph);
        udph.addPacketListener(new UDPPacketListener());
        ICMPHandler icmph = new ICMPHandler(iph);
        icmph.addPacketListener(new ICMPPacketListener());
        selectedDevice.startCapture();
        try {
            for (Iterator iter = targetPorts.iterator(); iter.hasNext(); ) {
                if (bStopScan) {
                    break;
                }
                int port = ((Port) iter.next()).getPort();
                udpl.setDestinationPort(port);
                int answer = UNKNOWN_PORT_STATUS;
                for (int i = 0; i <= numRetries; ++i) {
                    if (bStopScan) {
                        break;
                    }
                    long startTime = System.currentTimeMillis();
                    answer = udpProbe(packet, port);
                    System.err.println("Port: " + port + ", Try #: " + i + ", Answer: " + PORT_STATE[answer]);
                    if (answer != UNKNOWN_PORT_STATUS) {
                        resultsTableModel.addToTable(resultsTable, port, PORT_STATE[answer], getServiceName(port));
                    }
                    long endTime = System.currentTimeMillis();
                    long deltaTime = endTime - startTime;
                    if (deltaTime < scanDelay) {
                        try {
                            Thread.currentThread().sleep(scanDelay - deltaTime);
                        } catch (Exception e) {
                        }
                    }
                    if (answer != UNKNOWN_PORT_STATUS) {
                        break;
                    }
                }
                if (answer == UNKNOWN_PORT_STATUS) {
                    resultsTableModel.addToTable(resultsTable, port, PORT_STATE[PORT_OPEN_OR_FILTERED], getServiceName(port));
                }
            }
        } finally {
            System.err.println("\n\nClosing the DatagramSocket and stopping capture.\n\n");
            ds.close();
            selectedDevice.stopCapture();
        }
    }

    public int udpProbe(Packet packetI, int portI) throws IOException {
        icmpCode = -1;
        icmpType = -1;
        icmpIPLayerDestinationPort = -1;
        udpSourcePort = -1;
        selectedDevice.sendPacket(packetI);
        int partialTimeout = (int) Math.ceil(timeout / 10.0);
        for (int i = 0; i < 10; ++i) {
            try {
                Thread.sleep(partialTimeout);
            } catch (Exception e) {
            }
            if ((icmpIPLayerDestinationPort == portI) && (icmpType == ICMPLayer.TYPE_DESTINATION_UNREACHABLE)) {
                if (icmpCode == 3) {
                    return PORT_CLOSED;
                } else if ((icmpCode == 1) || (icmpCode == 2) || (icmpCode == 9) || (icmpCode == 10) || (icmpCode == 13)) {
                    return PORT_FILTERED;
                }
            } else if ((icmpIPLayerDestinationPort != portI) && (icmpType == ICMPLayer.TYPE_DESTINATION_UNREACHABLE)) {
                System.err.println("UDP probe: received ICMP packet containing a different remote port (" + icmpIPLayerDestinationPort + ") than the port we are currently examining (" + portI + ")");
            }
            if (udpSourcePort == portI) {
                return PORT_OPEN;
            } else if ((udpSourcePort != -1) && (udpSourcePort != portI)) {
                System.err.println("UDP probe: received UDP packet containing a different remote port (" + udpSourcePort + ") than the port we are currently examining (" + portI + ")");
            }
        }
        return UNKNOWN_PORT_STATUS;
    }

    private void loadDataLinkDevices() {
        try {
            dataLinkDevices = OSIDataLinkDevice.getDevices();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(jsscanFrame, new String("Failure loading data link devices:\n" + e), "Data Link Device Error", JOptionPane.ERROR_MESSAGE);
            dataLinkDevices = null;
        }
    }

    private void refreshDataLinkDevices() {
        int oldSelectionIndex = deviceList.getSelectedIndex();
        loadDataLinkDevices();
        deviceList.setListData(dataLinkDevices);
        if (oldSelectionIndex > -1) {
            deviceList.setSelectedIndex(oldSelectionIndex);
        }
    }

    private class UDPPacketListener implements PacketListener {

        public void packetReceived(PacketHandler handlerI, java.nio.ByteBuffer dataI, java.util.List chainI, PacketStatistics psI) {
            if (!(handlerI instanceof org.javasock.UDPHandler)) {
                System.err.println("ERROR: ICMPPacketListener.packetReceived() got wrong packet type.");
                return;
            }
            UDPLayer udpLayer = UDPLayer.createFromBytes(dataI);
            if (udpLayer.getDestinationPort() == localPort) {
                udpSourcePort = udpLayer.getSourcePort();
            }
        }
    }

    private class ICMPPacketListener implements PacketListener {

        public void packetReceived(PacketHandler handlerI, java.nio.ByteBuffer dataI, java.util.List layersI, PacketStatistics statsI) {
            if (!(handlerI instanceof org.javasock.ICMPHandler)) {
                System.err.println("ERROR: ICMPPacketListener.packetReceived() got wrong packet type.");
                return;
            }
            ByteBuffer moveable = dataI.duplicate();
            ICMPLayer icmpLayer = ICMPLayer.createFromBytes(dataI);
            int tempICMPCode = icmpLayer.getCode();
            int tempICMPType = icmpLayer.getType();
            if (tempICMPType == ICMPLayer.TYPE_DESTINATION_UNREACHABLE) {
                final int UNREACHABLE_SIZE = 8;
                moveable.position(moveable.position() + UNREACHABLE_SIZE);
                IPLayer ipl = IPLayer.createFromBytes(moveable);
                short ourPort = moveable.getShort();
                short theirPort = moveable.getShort();
                if (ourPort == localPort) {
                    icmpCode = tempICMPCode;
                    icmpType = tempICMPType;
                    icmpIPLayerDestinationPort = (int) theirPort;
                    System.err.println("\nGot ICMP Packet from remote port " + icmpIPLayerDestinationPort + ":\n\tcode = " + icmpCode + "\n\ttype = " + icmpType);
                }
            }
        }
    }
}
