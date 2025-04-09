package ClientUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.Date;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.Timer;
import Protocol.user.Login_R;

/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class LogFrame extends JComponent implements ComponentListener, WindowFocusListener, Runnable {

    JFrame frame;

    private Image background;

    private long lastupdate = 0;

    public boolean refreshRequested = true;

    public static JLabel lab1;

    public static JLabel lab2;

    static JTextField userName;

    static JPasswordField userPassword;

    private ClientGUI clientGui;

    private Login_R login_r;

    public void updateBackground() {
        try {
            Robot rbt = new Robot();
            Toolkit tk = Toolkit.getDefaultToolkit();
            Dimension dim = tk.getScreenSize();
            background = rbt.createScreenCapture(new Rectangle(0, 0, (int) dim.getWidth(), (int) dim.getHeight()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleLogin() {
        login_r = new Login_R();
        String ps = new String(this.userPassword.getPassword());
        login_r.setPasswd(ps);
        login_r.setStudentno(this.userName.getText());
        clientGui.doLogin();
    }

    public void refresh() {
        if (frame.isVisible()) {
            repaint();
            refreshRequested = true;
            lastupdate = new Date().getTime();
        }
    }

    public void componentShown(ComponentEvent evt) {
        repaint();
    }

    public void componentResized(ComponentEvent evt) {
        repaint();
    }

    public void componentMoved(ComponentEvent evt) {
        repaint();
    }

    public void componentHidden(ComponentEvent evt) {
    }

    public void windowGainedFocus(WindowEvent evt) {
        refresh();
    }

    public void windowLostFocus(WindowEvent evt) {
        refresh();
    }

    public LogFrame(JFrame frame, ClientGUI cgui) {
        super();
        this.frame = frame;
        this.clientGui = cgui;
        updateBackground();
        frame.addComponentListener(this);
        frame.addWindowFocusListener(this);
        new Thread(this).start();
    }

    public void run() {
        try {
            while (true) {
                Thread.sleep(250);
                long now = new Date().getTime();
                if (refreshRequested && ((now - lastupdate) > 1000)) {
                    if (frame.isVisible()) {
                        Point location = frame.getLocation();
                        updateBackground();
                        frame.show();
                        frame.setLocation(location);
                        refresh();
                    }
                    lastupdate = now;
                    refreshRequested = false;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void paintComponent(Graphics g) {
        Point pos = this.getLocationOnScreen();
        Point offset = new Point(-pos.x, -pos.y);
        g.drawImage(background, offset.x, offset.y, null);
    }

    /**
  * @param args
  */
    public Login_R getLogin_R() {
        return login_r;
    }

    public void go() {
        LogFrame bg = this;
        this.setLayout(null);
        JPanel panel = new JPanel() {

            public void paintComponent(Graphics g) {
                g.setColor(Color.blue);
                Image img = new ImageIcon("src/pic/sf5.jpg").getImage();
                g.drawImage(img, 0, 0, null);
            }
        };
        panel.setOpaque(false);
        bg.add(panel);
        panel.setBounds(0, 0, 323, 356);
        panel.setLayout(null);
        {
            lab1 = new JLabel();
            lab1.setText("�û���");
            frame.add(lab1);
            lab1.setBounds(40, 200, 152, 28);
        }
        {
            userName = new JTextField();
            frame.add(userName);
            userName.setBounds(100, 200, 152, 28);
        }
        {
            lab2 = new JLabel();
            lab2.setText("�� ��");
            frame.add(lab2);
            lab2.setBounds(40, 250, 152, 28);
        }
        {
            userPassword = new JPasswordField();
            frame.add(userPassword);
            userPassword.setBounds(100, 250, 152, 28);
        }
        MyButton bu = new MyButton(new ImageIcon("src/pic/Aqua04_2.jpg"));
        bg.add(bu);
        bu.setBounds(40, 300, 104, 101);
        bu.addActionListener(new entryListener(frame, userName, userPassword));
        MyButton buu = new MyButton(new ImageIcon("src/pic/Aqua13_1.jpg"));
        bg.add(buu);
        buu.setBounds(200, 300, 104, 101);
        buu.addActionListener(new registListener());
        frame.getContentPane().add("Center", bg);
        bg.setPreferredSize(new java.awt.Dimension(318, 260));
        frame.pack();
        frame.setSize(451, 448);
        frame.setLocation(500, 250);
        frame.setPreferredSize(new java.awt.Dimension(491, 448));
        frame.show();
    }

    /**
	* This method should return an instance of this class which does 
	* NOT initialize it's GUI elements. This method is ONLY required by
	* Jigloo if the superclass of this class is abstract or non-public. It 
	* is not needed in any other situation.
	 */
    public static Object getGUIBuilderInstance() {
        return new LogFrame(Boolean.FALSE);
    }

    /**
	 * This constructor is used by the getGUIBuilderInstance method to
	 * provide an instance of this class which has not had it's GUI elements
	 * initialized (ie, initGUI is not called in this constructor).
	 */
    public LogFrame(Boolean initGUI) {
        super();
    }
}

class entryListener implements ActionListener {

    JFrame f;

    JTextField n;

    JPasswordField m;

    public entryListener(JFrame f, JTextField userName, JPasswordField userPassword) {
        this.f = f;
        this.n = userName;
        this.m = userPassword;
    }

    public void actionPerformed(ActionEvent arg0) {
        String user = n.getText();
        String pass = m.getText();
        f.setVisible(false);
    }
}

class registListener implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        System.exit(0);
    }
}
