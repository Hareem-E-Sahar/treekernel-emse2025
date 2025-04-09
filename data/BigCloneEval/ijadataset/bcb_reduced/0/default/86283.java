import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;

public class PulseGenerator extends IO implements Component, ActionListener {

    private int[] pulse;

    private int time = 0;

    private int endtime;

    private int olddata = -1;

    private boolean enabled = false;

    private JInternalFrame f;

    private String name = "Pulse Generator";

    private String[] pin_names = { "Out", "Enable" };

    private JButton importpulse;

    private JButton Show_button;

    private Container content;

    private Project parent;

    public void init(Project parent, String data) {
        this.parent = parent;
        setWidth(2);
        setDirection(0x02);
        f = new JInternalFrame(name);
        f.setSize(100, 100);
        f.setLocation(30, 500);
        f.setVisible(true);
        content = f.getContentPane();
        content.setBackground(Color.white);
        content.setLayout(new FlowLayout());
        importpulse = new JButton("Import");
        importpulse.addActionListener(this);
        parent.createWindow(f);
        JPanel panel = new JPanel();
        Show_button = new JButton(new ImageIcon("images/tonegen.gif"));
        Show_button.setBorder(BorderFactory.createEmptyBorder());
        panel.add(Show_button);
        panel.add(importpulse);
        parent.addToTab(name, panel, false);
        Show_button.addActionListener(this);
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == Show_button) f.setVisible(!f.isVisible());
        if (evt.getSource() == importpulse) {
            try {
                Runtime rt = Runtime.getRuntime();
                Process pr = null;
                pr = rt.exec("tools/Pulse Recorder.exe");
                BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()), 10000);
                int size = Integer.parseInt(input.readLine());
                if (size > 0) {
                    pulse = new int[size + 1];
                    endtime = size + 1;
                    String line;
                    int pos = 0;
                    while ((line = input.readLine()) != null) {
                        String parts[] = line.split(":");
                        int length = Integer.parseInt(parts[1]) + pos;
                        int data = Integer.parseInt(parts[0]);
                        for (; pos < length && pos < size; pos++) pulse[pos] = data;
                    }
                }
            } catch (java.io.IOException ioe) {
                System.out.println("Problem with PulseRecorder");
            }
        }
    }

    public void ioChangeNotify() {
        if (state[1] == true) enabled = true; else enabled = false;
    }

    public String getName() {
        return name;
    }

    public String[] getPinNames() {
        return pin_names;
    }

    public void clock(int project_clock) {
        if (enabled && time < endtime && pulse[time] != olddata) {
            olddata = pulse[time];
            if (olddata == 1) {
                state[0] = true;
            } else {
                state[0] = false;
            }
            parent.postPinUpdate(this);
        }
        if (enabled) time++;
        if (time == endtime) time = 0;
    }

    public void starting() {
    }

    public void stopping() {
    }

    /** Project manager is notifying that Component should reset to initial state 
     */
    public void reset() {
    }

    /** This method is called to get the component to set it's windows location 
	 * @param xPos the x coordinate for the components window
	 * @param yPos the y coordinate for the components window
     */
    public void setLocation(int xPos, int yPos) {
        f.setLocation(xPos, yPos);
    }

    /** This method is called to return the components current location
	 *  @ return  Point containing the location
	 */
    public Point getLocation() {
        Point rv = new java.awt.Point();
        rv = f.getLocation(rv);
        return rv;
    }
}
