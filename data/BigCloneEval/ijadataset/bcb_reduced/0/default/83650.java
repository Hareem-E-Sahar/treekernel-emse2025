import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

class guiClientApp implements Runnable {

    MyClient myc;

    keyhandler kh = new keyhandler(this);

    JFrame f = new JFrame("Chat window");

    JPanel userp = new JPanel();

    JPanel chatp = new JPanel();

    JSplitPane split;

    JTextArea allarea = new JTextArea(15, 25);

    JTextArea mytext = new JTextArea(5, 25);

    JScrollPane sp;

    DefaultListModel names = new DefaultListModel();

    JList jl = new JList(names);

    public guiClientApp(MyClient myc) {
        this.myc = myc;
    }

    public void init() {
        f.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                kh.mc.out.println("�end");
                System.exit(0);
            }
        });
        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatp, userp);
        split.setOneTouchExpandable(true);
        GridBagLayout gblnames = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        f.getContentPane().add(split);
        GridBagLayout gblchat = new GridBagLayout();
        chatp.setLayout(gblchat);
        f.addComponentListener(new ComponentAdapter() {

            public void componentResized(ComponentEvent ce) {
                split.setDividerLocation(0.7);
            }
        });
        f.setLocation(125, 150);
        allarea.setLineWrap(true);
        allarea.setWrapStyleWord(true);
        allarea.setEditable(false);
        mytext.setLineWrap(true);
        mytext.setWrapStyleWord(true);
        mytext.requestDefaultFocus();
        mytext.addKeyListener(kh);
        JScrollPane sp = new JScrollPane(allarea);
        sp.setAutoscrolls(true);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0.75;
        gbc.fill = GridBagConstraints.BOTH;
        gblchat.setConstraints(sp, gbc);
        chatp.add(sp);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1;
        gbc.weighty = 0.25;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 0, 0, 0);
        gblchat.setConstraints(mytext, gbc);
        chatp.add(mytext);
        userp.setLayout(gblnames);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        JScrollPane sp1 = new JScrollPane(jl);
        gblnames.setConstraints(sp1, gbc);
        userp.add(sp1);
        f.pack();
        split.setDividerLocation(0.7);
        f.setVisible(true);
    }

    public void run() {
        try {
            kh.setClient(myc);
            init();
            String st;
            while (true) {
                st = myc.in.readLine();
                char ch = st.charAt(0);
                if (ch == '�') {
                    StringTokenizer stk = new StringTokenizer(st, "�");
                    String fn = stk.nextToken();
                    if (fn.equals("end")) break;
                    if (fn.equals("users")) {
                        names.removeAllElements();
                        while (stk.hasMoreTokens()) names.add(0, stk.nextToken());
                    }
                    continue;
                }
                allarea.append(st + "\n");
                allarea.setCaretPosition((allarea.getText().length()));
            }
        } catch (Exception e) {
        }
    }
}

class keyhandler extends KeyAdapter {

    guiClientApp gc;

    MyClient mc;

    public keyhandler(guiClientApp gc) {
        this.gc = gc;
    }

    public void keyReleased(KeyEvent ke) {
        int k = ke.getKeyCode();
        if (k == KeyEvent.VK_ENTER) {
            String str = gc.mytext.getText();
            str = str.substring(0, str.length() - 1);
            mc.out.println(str);
            gc.mytext.setText(null);
        }
    }

    public void setClient(MyClient myc) {
        mc = myc;
        System.out.println(mc.sock);
    }
}
