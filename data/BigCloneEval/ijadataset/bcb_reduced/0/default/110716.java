import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.swing.*;
import javax.swing.event.*;

public class AppServer extends JFrame implements ActionListener {

    JLabel lheading, lsub, litem1, litem2;

    JButton bnext, bback;

    public AppServer() {
        Container cont = this.getContentPane();
        cont.setLayout(null);
        this.setSize(500, 400);
        this.setLocation(300, 200);
        this.setTitle("High Availability Using Virtualization");
        lheading = new JLabel("Installation Of Primary/Secondary Server");
        lheading.setBounds(50, 50, 250, 25);
        cont.add(lheading);
        lsub = new JLabel("Installing  :  ");
        lsub.setBounds(100, 100, 100, 25);
        cont.add(lsub);
        litem1 = new JLabel("RSYNC");
        litem1.setBounds(120, 130, 100, 25);
        cont.add(litem1);
        bnext = new JButton("NEXT");
        bnext.setBounds(200, 300, 100, 25);
        cont.add(bnext);
        bback = new JButton("BACK");
        bback.setBounds(320, 300, 100, 25);
        cont.add(bback);
        bnext.addActionListener(this);
        bback.addActionListener(this);
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource().equals(bnext)) {
            String cmd = "apt-get install rsync";
            System.out.println("in action performed 1");
            System.out.println("in action performed 2");
            try {
                Process p = Runtime.getRuntime().exec(cmd);
                System.out.println("in try");
                BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line = null;
                while ((line = input.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        } else {
            this.dispose();
            InstallMain im = new InstallMain();
            im.show();
        }
    }

    public static void main(String args[]) {
        AppServer a = new AppServer();
        a.show();
    }
}
