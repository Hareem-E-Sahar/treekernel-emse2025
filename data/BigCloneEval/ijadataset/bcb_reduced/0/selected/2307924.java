package regnumhelper.gui.mule;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.RandomAccessFile;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import regnumhelper.Main;
import regnumhelper.Settings;
import regnumhelper.ValueChangedListener;
import regnumhelper.gui.ScreenshotAdjustment;
import regnumhelper.mule.Item;
import regnumhelper.mule.MuleInventory;
import regnumhelper.rmi.MuleUser;
import regnumhelper.rmi.RemoteMuleServer;

/**
 *
 * @author  Niels
 */
public class MulePanel extends javax.swing.JPanel implements ListSelectionListener {

    private Main main = null;

    ValueChangedListener positionChangeListener = new ValueChangedListener() {

        public void valueChanged(Object source) {
            if (main != null) {
                main.getSettings().setMuleXOffset(scrAdjust.getScreenshotRectangle().x);
                main.getSettings().setMuleYOffset(scrAdjust.getScreenshotRectangle().y);
                main.getSettings().setMuleWidth(scrAdjust.getScreenshotRectangle().width);
                main.getSettings().setMuleHeight(scrAdjust.getScreenshotRectangle().height);
                captureImages();
            }
        }
    };

    private Robot robot;

    /** Used for contacting the server.  **/
    private RemoteMuleServer server;

    private MuleInventory muleInventory = new MuleInventory();

    ;

    /** Used for storing the images.  **/
    private File imageDir;

    DefaultTableModel tblModelLocal = null;

    DefaultTableModel tblModelServer = null;

    DefaultComboBoxModel cmbModelLocal = new DefaultComboBoxModel();

    DefaultComboBoxModel cmbModelServer = new DefaultComboBoxModel();

    /** Used for caping the tops. **/
    private ImageIcon capTopIcon;

    /** Used for capping the bottom. **/
    private ImageIcon capBotIcon;

    /** Creates new form MulePanel */
    public MulePanel() {
        initComponents();
        String homeDir = System.getProperty("user.home");
        imageDir = new File(homeDir, ".regnumHelper/images");
        imageDir.mkdirs();
        Vector colNamesV = new Vector();
        colNamesV.add("Item Names");
        tblModelLocal = new DefaultTableModel(new Vector(), colNamesV);
        tblInventory.setModel(tblModelLocal);
        tblInventory.setDefaultRenderer(Item.class, new InventoryItemRenderer());
        tblModelServer = new DefaultTableModel(new Vector(), colNamesV);
        tblInventoryServer.setModel(tblModelServer);
        tblInventoryServer.setDefaultRenderer(Item.class, new InventoryItemRenderer());
        try {
            lblInvView.setIcon(new ImageIcon(javax.imageio.ImageIO.read(Main.class.getResource("images/itemDetails.png"))));
            lblInvViewServer.setIcon(new ImageIcon(javax.imageio.ImageIO.read(Main.class.getResource("images/itemDetails.png"))));
        } catch (Exception e) {
        }
        try {
            robot = new Robot();
        } catch (Exception e) {
        }
        scrAdjust.addValueChangedListener(positionChangeListener);
        cmbGroupLocal.setModel(cmbModelLocal);
        cmbGroupServer.setModel(cmbModelServer);
        this.butInvReceiveDataServer.setEnabled(false);
        this.butSendData.setEnabled(false);
        this.txtClientGroup.setText("");
        cmbGroupLocal.removeAllItems();
        cmbGroupServer.removeAllItems();
        tblInventory.setDefaultRenderer(Item.class, new InventoryItemRenderer());
        tblInventoryServer.setDefaultRenderer(Item.class, new InventoryItemRenderer());
    }

    public void setMain(Main main) {
        this.main = main;
        txtClientServer.setText(main.getSettings().getMuleServerHost());
        txtClientServerPort.setText(main.getSettings().getMuleServerPort() + "");
        scrAdjust.setManualSettings(true);
        scrAdjust.setOffsetX(main.getSettings().getMuleXOffset());
        scrAdjust.setOffsetY(main.getSettings().getMuleYOffset());
        scrAdjust.setImgWidth(main.getSettings().getMuleWidth());
        scrAdjust.setImgHeight(main.getSettings().getMuleHeight());
        scrAdjust.setManualSettings(false);
    }

    /**
     * Gets the server.
     **/
    public RemoteMuleServer getServer() {
        String host = "";
        int port = 1099;
        try {
            if (server == null) {
                host = txtClientServer.getText();
                String portString = txtClientServerPort.getText();
                port = Integer.parseInt(portString);
                if (main != null) {
                    main.getSettings().setMuleServerHost(host);
                    main.getSettings().setMuleServerPort(port);
                }
                Registry registry = LocateRegistry.getRegistry(host, port);
                server = (RemoteMuleServer) registry.lookup("MuleServer");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Unable to reach the server " + host + ":" + port, "Network Error", JOptionPane.ERROR_MESSAGE);
        }
        return server;
    }

    private void doCreate() {
        String name = txtClientUsername.getText();
        char[] pwdData = pwdClientPassword.getPassword();
        StringBuffer pasBuf = new StringBuffer();
        for (int i = 0; i < pwdData.length; i++) {
            pasBuf.append(pwdData[i]);
        }
        String pass = pasBuf.toString();
        String group = txtClientGroup.getText();
        if (name == null || name.equals("")) {
            JOptionPane.showMessageDialog(this, "User Name is invalid", "User Name Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (pass == null || pass.equals("")) {
            JOptionPane.showMessageDialog(this, "Password is invalid", "Password Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        server = getServer();
        if (server == null) {
            return;
        }
        MuleUser muleUser = new MuleUser(name, pass, group);
        try {
            boolean success = server.addMuleUser(muleUser);
            if (!success) {
                JOptionPane.showMessageDialog(this, "Unable to create user " + name, "User Create Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (group != null && !group.equals("")) {
                if (group.contains(",")) {
                    StringTokenizer stok = new StringTokenizer(group, ",", false);
                    while (stok.hasMoreTokens()) {
                        String tmp = stok.nextToken();
                        success = server.addGroup(muleUser, tmp);
                        if (!success) {
                            JOptionPane.showMessageDialog(this, "Unable to create group" + tmp, "Group Create Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    success = server.addGroup(muleUser, group);
                    if (!success) {
                        JOptionPane.showMessageDialog(this, "Unable to create group" + group, "Group Create Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            JOptionPane.showMessageDialog(this, "The user was created sucessfully", "User Created", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MuleUser doLogin() {
        MuleUser retVal = null;
        this.butInvReceiveDataServer.setEnabled(false);
        this.butSendData.setEnabled(false);
        this.txtClientGroup.setText("");
        cmbGroupLocal.removeAllItems();
        cmbGroupServer.removeAllItems();
        String name = txtClientUsername.getText();
        char[] pwdData = pwdClientPassword.getPassword();
        StringBuffer pasBuf = new StringBuffer();
        for (int i = 0; i < pwdData.length; i++) {
            pasBuf.append(pwdData[i]);
        }
        String pass = pasBuf.toString();
        if (name == null || name.equals("")) {
            JOptionPane.showMessageDialog(this, "User Name is invalid", "User Name Error", JOptionPane.ERROR_MESSAGE);
            return retVal;
        }
        if (pass == null || pass.equals("")) {
            JOptionPane.showMessageDialog(this, "Password is invalid", "Password Error", JOptionPane.ERROR_MESSAGE);
            return retVal;
        }
        server = null;
        server = getServer();
        if (server == null) {
            return retVal;
        }
        try {
            retVal = new MuleUser(name, pass, null);
            retVal = server.getMuleUser(retVal.username, retVal.password);
            if (retVal == null) {
                JOptionPane.showMessageDialog(this, "Unable Login with supplied user/pass " + name, "Invalid Login", JOptionPane.ERROR_MESSAGE);
                return retVal;
            }
            this.butInvReceiveDataServer.setEnabled(true);
            this.butSendData.setEnabled(true);
            this.txtClientGroup.setText(retVal.group);
            StringTokenizer tokenizer = new StringTokenizer(retVal.group, ",");
            while (tokenizer.hasMoreTokens()) {
                String tok = tokenizer.nextToken();
                cmbModelLocal.addElement(tok);
                cmbModelServer.addElement(tok);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }

    public void getDataFromServer() {
        MuleUser mu = doLogin();
        if (mu == null) {
            return;
        }
        try {
            server = getServer();
            if (server == null) {
                return;
            }
            String currentGroup = (String) cmbGroupServer.getSelectedItem();
            if (currentGroup == null) {
                JOptionPane.showMessageDialog(this, "Group does not exist ", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Vector items = server.getMuleItems(doLogin(), currentGroup);
            if (items == null) {
                JOptionPane.showMessageDialog(this, "Unable to get inventory for group " + currentGroup, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            fillTableServer(items);
        } catch (Exception e) {
        }
    }

    public void getDataFromInventory() {
        try {
            Settings settings = main.getSettings();
            int x = settings.getMuleXOffset();
            int y = settings.getMuleYOffset();
            int w = 215;
            int h = settings.getMuleHeight();
            muleInventory.getData(x, y, w, h, imageDir.getPath());
            fillTableLocal();
        } catch (Exception e) {
        }
    }

    /**
     * Handles sending the data to the server.
     **/
    private void sendData() {
        MuleUser mu = doLogin();
        if (mu == null) {
            return;
        }
        try {
            String group = (String) cmbGroupLocal.getSelectedItem();
            if (mu.group == null || mu.group.equals("")) {
                JOptionPane.showMessageDialog(this, "This user account doesn't have a valid group", "Invalid Group", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!mu.group.contains(group)) {
                JOptionPane.showMessageDialog(this, "This user account is not a memeber of group " + group, "Invalid Group", JOptionPane.ERROR_MESSAGE);
                return;
            }
            server.clearMuleItems(mu, group);
            pgrSendData.setMaximum(muleInventory.itemsV.size());
            pgrSendData.setMinimum(0);
            for (int i = 0; i < muleInventory.itemsV.size(); i++) {
                Item item = (Item) muleInventory.itemsV.elementAt(i);
                pgrSendData.setValue(i);
                File file = new java.io.File(imageDir.getPath() + File.separator + item.getFileName());
                if (file.exists()) {
                    RandomAccessFile raf = new RandomAccessFile(file, "r");
                    byte[] buf = new byte[(int) file.length()];
                    raf.readFully(buf);
                    raf.close();
                    server.uploadMuleItem(mu, group, item, buf);
                }
            }
            JOptionPane.showMessageDialog(this, "Inventory and images uploaded successfully", "Upload Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles filling the table with the items from the inventory.
     **/
    private void fillTableLocal() {
        tblInventory.getSelectionModel().removeListSelectionListener(this);
        int max = tblModelLocal.getRowCount();
        for (int i = max - 1; i >= 0; i--) {
            tblModelLocal.removeRow(i);
        }
        for (int i = 0; i < muleInventory.itemsV.size(); i++) {
            Vector v = new Vector();
            v.add(muleInventory.itemsV.elementAt(i));
            tblModelLocal.addRow(v);
        }
        tblInventory.getSelectionModel().addListSelectionListener(this);
    }

    /**
     * Handles filling the table with the items from the inventory.
     **/
    private void fillTableServer(Vector items) {
        tblInventoryServer.getSelectionModel().removeListSelectionListener(this);
        int max = tblModelServer.getRowCount();
        for (int i = max - 1; i >= 0; i--) {
            tblModelServer.removeRow(i);
        }
        for (int i = 0; i < items.size(); i++) {
            Vector v = new Vector();
            v.add(items.elementAt(i));
            tblModelServer.addRow(v);
        }
        tblInventoryServer.getSelectionModel().addListSelectionListener(this);
    }

    /**
     * Pops open a dialog with an image of the item selected from the table.
     **/
    private void viewItem() {
        int row = tblInventory.getSelectedRow();
        if (row >= 0) {
            try {
                Item item = (Item) tblInventory.getModel().getValueAt(row, 0);
                BufferedImage bi = javax.imageio.ImageIO.read(new File(imageDir.getPath() + File.separator + item.getFileName()));
                ImageIcon icon = new ImageIcon(bi);
                lblInvView.setIcon(icon);
                lblInvView.repaint();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void viewItemServer() {
        int row = tblInventoryServer.getSelectedRow();
        if (row >= 0) {
            try {
                Item item = (Item) tblInventoryServer.getModel().getValueAt(row, 0);
                BufferedImage bi = javax.imageio.ImageIO.read(new File(imageDir.getPath() + File.separator + item.getFileName()));
                ImageIcon icon = new ImageIcon(bi);
                lblInvViewServer.setIcon(icon);
                lblInvViewServer.repaint();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void valueChanged(ListSelectionEvent event) {
        viewItem();
        viewItemServer();
    }

    private void captureImages() {
        int x, y, w, h;
        try {
            x = main.getSettings().getMuleXOffset();
            y = main.getSettings().getMuleYOffset();
            h = main.getSettings().getMuleHeight();
            w = main.getSettings().getMuleWidth();
        } catch (Exception e) {
            e.printStackTrace();
            x = 50;
            y = 50;
            w = 215;
            h = 150;
        }
        try {
            robot.delay(100);
            Rectangle rectTop = new Rectangle(x - 50, y - 50, 100, 100);
            Rectangle rectBot = new Rectangle(x - 50 + w, y - 50 + h, 100, 100);
            BufferedImage top = robot.createScreenCapture(rectTop);
            robot.delay(100);
            BufferedImage bot = robot.createScreenCapture(rectBot);
            Graphics2D g = top.createGraphics();
            g.setColor(Color.RED);
            g.drawLine(50, 40, 50, 60);
            g.drawLine(40, 50, 60, 50);
            g = bot.createGraphics();
            g.setColor(Color.RED);
            g.drawLine(50, 40, 50, 60);
            g.drawLine(40, 50, 60, 50);
            capTopIcon = new ImageIcon(top);
            capBotIcon = new ImageIcon(bot);
            lblCapTopImg.setIcon(capTopIcon);
            lblCapBotImg.setIcon(capBotIcon);
            lblCapTopImg.repaint();
            lblCapBotImg.repaint();
            this.repaint();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class InventoryItemRenderer extends DefaultTableCellRenderer {

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            if (((Item) value).type == Item.TYPE_NORM) {
                c.setForeground(Color.BLUE);
            } else {
                c.setForeground(Color.YELLOW);
            }
            return c;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        tabMulePanel = new javax.swing.JTabbedPane();
        panClient = new javax.swing.JPanel();
        lblClientUsername = new javax.swing.JLabel();
        lblClientPassword = new javax.swing.JLabel();
        txtClientUsername = new javax.swing.JTextField();
        pwdClientPassword = new javax.swing.JPasswordField();
        butLogin = new javax.swing.JButton();
        butCreate = new javax.swing.JButton();
        lblClientServer = new javax.swing.JLabel();
        txtClientServer = new javax.swing.JTextField();
        txtClientServerPort = new javax.swing.JTextField();
        lblClientServerPort = new javax.swing.JLabel();
        lblClientGroup = new javax.swing.JLabel();
        txtClientGroup = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        panInventoryLoc = new javax.swing.JPanel();
        scrInvTable = new javax.swing.JScrollPane();
        tblInventory = new javax.swing.JTable();
        panInvView = new javax.swing.JPanel();
        lblInvView = new javax.swing.JLabel();
        panInvControl = new javax.swing.JPanel();
        butInvGetData = new javax.swing.JButton();
        butSendData = new javax.swing.JButton();
        cmbGroupLocal = new javax.swing.JComboBox();
        pgrSendData = new javax.swing.JProgressBar();
        panInventoryServer = new javax.swing.JPanel();
        scrInvTableServer = new javax.swing.JScrollPane();
        tblInventoryServer = new javax.swing.JTable();
        panInvViewServer = new javax.swing.JPanel();
        lblInvViewServer = new javax.swing.JLabel();
        panInvControlServer = new javax.swing.JPanel();
        butInvReceiveDataServer = new javax.swing.JButton();
        cmbGroupServer = new javax.swing.JComboBox();
        panSettings = new javax.swing.JPanel();
        scrAdjust = new regnumhelper.gui.ScreenshotAdjustment();
        panScreens = new javax.swing.JPanel();
        panCapTopImg = new javax.swing.JPanel();
        lblCapTopImg = new javax.swing.JLabel();
        panSmplTopImg = new javax.swing.JPanel();
        lblSmplTop = new javax.swing.JLabel();
        panCapBotImg = new javax.swing.JPanel();
        lblCapBotImg = new javax.swing.JLabel();
        panSmplBotImg = new javax.swing.JPanel();
        lblSmplTop1 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        butCapture = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        setLayout(new java.awt.GridBagLayout());
        panClient.setLayout(new java.awt.GridBagLayout());
        lblClientUsername.setText("User Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panClient.add(lblClientUsername, gridBagConstraints);
        lblClientPassword.setText("Password");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panClient.add(lblClientPassword, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panClient.add(txtClientUsername, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panClient.add(pwdClientPassword, gridBagConstraints);
        butLogin.setText("Login");
        butLogin.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butLoginActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panClient.add(butLogin, gridBagConstraints);
        butCreate.setText("Create");
        butCreate.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butCreateActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panClient.add(butCreate, gridBagConstraints);
        lblClientServer.setText("Mule Server Adress");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panClient.add(lblClientServer, gridBagConstraints);
        txtClientServer.setText("monky-games.com");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panClient.add(txtClientServer, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panClient.add(txtClientServerPort, gridBagConstraints);
        lblClientServerPort.setText("Mule Server Port");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panClient.add(lblClientServerPort, gridBagConstraints);
        lblClientGroup.setText("Group");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panClient.add(lblClientGroup, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panClient.add(txtClientGroup, gridBagConstraints);
        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 92, Short.MAX_VALUE));
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 319, Short.MAX_VALUE));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        panClient.add(jPanel1, gridBagConstraints);
        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 433, Short.MAX_VALUE));
        jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 136, Short.MAX_VALUE));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        panClient.add(jPanel2, gridBagConstraints);
        tabMulePanel.addTab("Client", panClient);
        panInventoryLoc.setLayout(new java.awt.GridBagLayout());
        scrInvTable.setPreferredSize(new java.awt.Dimension(375, 100));
        tblInventory.setModel(new javax.swing.table.DefaultTableModel(new Object[][] { { null, null, null, null }, { null, null, null, null }, { null, null, null, null }, { null, null, null, null } }, new String[] { "Title 1", "Title 2", "Title 3", "Title 4" }));
        scrInvTable.setViewportView(tblInventory);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panInventoryLoc.add(scrInvTable, gridBagConstraints);
        panInvView.setLayout(new java.awt.BorderLayout());
        panInvView.add(lblInvView, java.awt.BorderLayout.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panInventoryLoc.add(panInvView, gridBagConstraints);
        panInvControl.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        butInvGetData.setText("Analyse Regnum Inventory");
        butInvGetData.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butInvGetDataActionPerformed(evt);
            }
        });
        panInvControl.add(butInvGetData);
        butSendData.setText("Send Data");
        butSendData.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butSendDataActionPerformed(evt);
            }
        });
        panInvControl.add(butSendData);
        cmbGroupLocal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbGroupLocal.setMinimumSize(new java.awt.Dimension(100, 22));
        cmbGroupLocal.setPreferredSize(new java.awt.Dimension(100, 22));
        cmbGroupLocal.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbGroupLocalActionPerformed(evt);
            }
        });
        panInvControl.add(cmbGroupLocal);
        panInvControl.add(pgrSendData);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panInventoryLoc.add(panInvControl, gridBagConstraints);
        tabMulePanel.addTab("Inventory Local", panInventoryLoc);
        panInventoryServer.setLayout(new java.awt.GridBagLayout());
        scrInvTableServer.setPreferredSize(new java.awt.Dimension(375, 100));
        tblInventoryServer.setModel(new javax.swing.table.DefaultTableModel(new Object[][] { { null, null, null, null }, { null, null, null, null }, { null, null, null, null }, { null, null, null, null } }, new String[] { "Title 1", "Title 2", "Title 3", "Title 4" }));
        scrInvTableServer.setViewportView(tblInventoryServer);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panInventoryServer.add(scrInvTableServer, gridBagConstraints);
        panInvViewServer.setLayout(new java.awt.BorderLayout());
        panInvViewServer.add(lblInvViewServer, java.awt.BorderLayout.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panInventoryServer.add(panInvViewServer, gridBagConstraints);
        panInvControlServer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        butInvReceiveDataServer.setText("Receive Data");
        butInvReceiveDataServer.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butInvReceiveDataServerActionPerformed(evt);
            }
        });
        panInvControlServer.add(butInvReceiveDataServer);
        cmbGroupServer.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbGroupServer.setMinimumSize(new java.awt.Dimension(100, 22));
        cmbGroupServer.setPreferredSize(new java.awt.Dimension(100, 22));
        cmbGroupServer.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbGroupServerActionPerformed(evt);
            }
        });
        panInvControlServer.add(cmbGroupServer);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panInventoryServer.add(panInvControlServer, gridBagConstraints);
        tabMulePanel.addTab("Inventory on Server", panInventoryServer);
        panSettings.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panSettings.add(scrAdjust, gridBagConstraints);
        panScreens.setLayout(new java.awt.GridBagLayout());
        panCapTopImg.setBorder(javax.swing.BorderFactory.createTitledBorder("Capture Top Image"));
        panCapTopImg.setMinimumSize(new java.awt.Dimension(112, 126));
        panCapTopImg.setPreferredSize(new java.awt.Dimension(112, 126));
        panCapTopImg.setLayout(new java.awt.BorderLayout());
        panCapTopImg.add(lblCapTopImg, java.awt.BorderLayout.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panScreens.add(panCapTopImg, gridBagConstraints);
        panSmplTopImg.setBorder(javax.swing.BorderFactory.createTitledBorder("Sample Top Image"));
        panSmplTopImg.setLayout(new java.awt.BorderLayout());
        lblSmplTop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/regnumhelper/images/exampleMuleTopSample.png")));
        panSmplTopImg.add(lblSmplTop, java.awt.BorderLayout.PAGE_START);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panScreens.add(panSmplTopImg, gridBagConstraints);
        panCapBotImg.setBorder(javax.swing.BorderFactory.createTitledBorder("Capture Bot Image"));
        panCapBotImg.setMinimumSize(new java.awt.Dimension(112, 126));
        panCapBotImg.setPreferredSize(new java.awt.Dimension(112, 126));
        panCapBotImg.setLayout(new java.awt.BorderLayout());
        panCapBotImg.add(lblCapBotImg, java.awt.BorderLayout.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panScreens.add(panCapBotImg, gridBagConstraints);
        panSmplBotImg.setBorder(javax.swing.BorderFactory.createTitledBorder("Sample Bot Image"));
        panSmplBotImg.setLayout(new java.awt.BorderLayout());
        lblSmplTop1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/regnumhelper/images/exampleMuleBotSample.png")));
        panSmplBotImg.add(lblSmplTop1, java.awt.BorderLayout.PAGE_START);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panScreens.add(panSmplBotImg, gridBagConstraints);
        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 0, Short.MAX_VALUE));
        jPanel9Layout.setVerticalGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 0, Short.MAX_VALUE));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        panScreens.add(jPanel9, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panSettings.add(panScreens, gridBagConstraints);
        butCapture.setText("Capture");
        butCapture.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butCaptureActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panSettings.add(butCapture, gridBagConstraints);
        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 272, Short.MAX_VALUE));
        jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 448, Short.MAX_VALUE));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        panSettings.add(jPanel3, gridBagConstraints);
        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 526, Short.MAX_VALUE));
        jPanel4Layout.setVerticalGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 7, Short.MAX_VALUE));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        panSettings.add(jPanel4, gridBagConstraints);
        tabMulePanel.addTab("Settings", panSettings);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        add(tabMulePanel, gridBagConstraints);
    }

    private void butLoginActionPerformed(java.awt.event.ActionEvent evt) {
        doLogin();
    }

    private void butCreateActionPerformed(java.awt.event.ActionEvent evt) {
        doCreate();
    }

    private void butInvGetDataActionPerformed(java.awt.event.ActionEvent evt) {
        getDataFromInventory();
    }

    private void butSendDataActionPerformed(java.awt.event.ActionEvent evt) {
        sendData();
    }

    private void butInvReceiveDataServerActionPerformed(java.awt.event.ActionEvent evt) {
        getDataFromServer();
    }

    private void butCaptureActionPerformed(java.awt.event.ActionEvent evt) {
        captureImages();
    }

    private void cmbGroupServerActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void cmbGroupLocalActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private javax.swing.JButton butCapture;

    private javax.swing.JButton butCreate;

    private javax.swing.JButton butInvGetData;

    private javax.swing.JButton butInvReceiveDataServer;

    private javax.swing.JButton butLogin;

    private javax.swing.JButton butSendData;

    private javax.swing.JComboBox cmbGroupLocal;

    private javax.swing.JComboBox cmbGroupServer;

    private javax.swing.JPanel jPanel1;

    private javax.swing.JPanel jPanel2;

    private javax.swing.JPanel jPanel3;

    private javax.swing.JPanel jPanel4;

    private javax.swing.JPanel jPanel9;

    private javax.swing.JLabel lblCapBotImg;

    private javax.swing.JLabel lblCapTopImg;

    private javax.swing.JLabel lblClientGroup;

    private javax.swing.JLabel lblClientPassword;

    private javax.swing.JLabel lblClientServer;

    private javax.swing.JLabel lblClientServerPort;

    private javax.swing.JLabel lblClientUsername;

    private javax.swing.JLabel lblInvView;

    private javax.swing.JLabel lblInvViewServer;

    private javax.swing.JLabel lblSmplTop;

    private javax.swing.JLabel lblSmplTop1;

    private javax.swing.JPanel panCapBotImg;

    private javax.swing.JPanel panCapTopImg;

    private javax.swing.JPanel panClient;

    private javax.swing.JPanel panInvControl;

    private javax.swing.JPanel panInvControlServer;

    private javax.swing.JPanel panInvView;

    private javax.swing.JPanel panInvViewServer;

    private javax.swing.JPanel panInventoryLoc;

    private javax.swing.JPanel panInventoryServer;

    private javax.swing.JPanel panScreens;

    private javax.swing.JPanel panSettings;

    private javax.swing.JPanel panSmplBotImg;

    private javax.swing.JPanel panSmplTopImg;

    private javax.swing.JProgressBar pgrSendData;

    private javax.swing.JPasswordField pwdClientPassword;

    private regnumhelper.gui.ScreenshotAdjustment scrAdjust;

    private javax.swing.JScrollPane scrInvTable;

    private javax.swing.JScrollPane scrInvTableServer;

    private javax.swing.JTabbedPane tabMulePanel;

    private javax.swing.JTable tblInventory;

    private javax.swing.JTable tblInventoryServer;

    private javax.swing.JTextField txtClientGroup;

    private javax.swing.JTextField txtClientServer;

    private javax.swing.JTextField txtClientServerPort;

    private javax.swing.JTextField txtClientUsername;
}
