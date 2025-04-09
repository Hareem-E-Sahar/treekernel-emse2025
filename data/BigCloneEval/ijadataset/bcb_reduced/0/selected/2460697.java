package gx.calc;

public class AboutDialog extends javax.swing.JDialog implements java.awt.event.ActionListener {

    private static final long serialVersionUID = 1L;

    private javax.swing.JButton jbnOk;

    private java.awt.Image img;

    public static final String project_url = "http://code.google.com/p/gx-calculator/";

    public AboutDialog(javax.swing.JFrame parent) {
        super(parent, "About GX Calculator", true);
        setLocationRelativeTo(parent);
        img = new javax.swing.ImageIcon(getClass().getResource("/gx/calc/calculator.png")).getImage();
        StringBuffer details = new StringBuffer();
        StringBuffer changes = new StringBuffer();
        details.append(Configuration.APPLICATION_NAME);
        details.append("\nDeveloper:\tGX");
        details.append("\nVersion:\t" + Configuration.VERSION);
        details.append("\nMore infomation:");
        changes.append("\nChanges:");
        changes.append("\n1.2.1 Presision 2009/10");
        changes.append("\n\tDisplay presion changed to 6 decimal places i.e. 100*1.15 no longer shows 114.99999999999999 but 115");
        changes.append("\n1.2 Plugins 2009/09");
        changes.append("\n\tplugins are now supported with customizable buttons");
        changes.append("\n\tstates persistant, the toggle state of extra buttons, screen location and text size is kept when closing the application");
        changes.append("\n1.1 Sticky buttons 2009/09");
        changes.append("\n\toperator buttons ('+' '-' '/' '*' etc) toggle buttons and stay pressed until next event");
        changes.append("\n\ticon added");
        changes.append("\n1.0 Initial release 2009/08");
        changes.append("\n\tfirst release");
        javax.swing.JTextArea jTextAbout = new javax.swing.JTextArea(details.toString()) {

            private static final long serialVersionUID = 1L;

            {
                setOpaque(false);
            }

            public void paintComponent(java.awt.Graphics g) {
                g.drawImage(img, 150, 5, this);
                super.paintComponent(g);
            }
        };
        jTextAbout.setEditable(false);
        javax.swing.JLabel jLink = new javax.swing.JLabel(project_url);
        jLink.setForeground(java.awt.Color.BLUE);
        jLink.addMouseListener(new javax.swing.event.MouseInputAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (javax.swing.SwingUtilities.isRightMouseButton(e)) {
                    javax.swing.JPopupMenu p = new javax.swing.JPopupMenu();
                    p.add(new OpenLink());
                    p.add(new CopyLink());
                    p.show(e.getComponent(), e.getX(), e.getY());
                } else openBrowserLink(((javax.swing.JLabel) e.getSource()).getText());
            }
        });
        javax.swing.JTextArea jTextChanges = new javax.swing.JTextArea(changes.toString());
        jTextChanges.setEditable(false);
        jTextChanges.setCaretPosition(0);
        javax.swing.JScrollPane sc = new javax.swing.JScrollPane(jTextChanges);
        javax.swing.JPanel wrapper = new javax.swing.JPanel(new java.awt.BorderLayout());
        javax.swing.JPanel n = new javax.swing.JPanel(new java.awt.BorderLayout());
        n.add(jTextAbout, java.awt.BorderLayout.CENTER);
        n.add(jLink, java.awt.BorderLayout.SOUTH);
        wrapper.add(n, java.awt.BorderLayout.NORTH);
        wrapper.add(sc, java.awt.BorderLayout.CENTER);
        getContentPane().add(wrapper, java.awt.BorderLayout.CENTER);
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));
        jbnOk = new javax.swing.JButton(" OK ");
        jbnOk.addActionListener(this);
        p.add(jbnOk);
        getContentPane().add(p, java.awt.BorderLayout.SOUTH);
        getContentPane().setPreferredSize(new java.awt.Dimension(350, 350));
        addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowClosing(java.awt.event.WindowEvent e) {
                java.awt.Window aboutDialog = e.getWindow();
                aboutDialog.dispose();
            }
        });
        pack();
        setVisible(true);
    }

    public static void openBrowserLink(String url) {
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) desktop.browse(new java.net.URI(url));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (e.getSource() == jbnOk) this.dispose();
    }
}

class CopyLink extends javax.swing.AbstractAction {

    private static final long serialVersionUID = 1L;

    public CopyLink() {
        super("Copy link location");
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(AboutDialog.project_url), null);
    }
}

class OpenLink extends javax.swing.AbstractAction {

    private static final long serialVersionUID = 1L;

    public OpenLink() {
        super("Open link");
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        AboutDialog.openBrowserLink(AboutDialog.project_url);
    }
}
