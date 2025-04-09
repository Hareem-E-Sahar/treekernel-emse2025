import javax.swing.*;
import java.io.*;
import java.awt.event.*;
import java.util.StringTokenizer;

class ConcreteProject implements Project, Runnable, ActionListener {

    String[] comp_types;

    String[] comp_data;

    Component[] comps;

    Connection[] connects;

    int loop = 0;

    int loop2 = 0;

    int project_clock;

    int last_run = 0;

    int algebraicLoopDetection = 0;

    double version = 0;

    Thread running;

    BottomPanel bottompanel;

    HelpBrowser helpbrowser;

    JMenuItem start;

    String filename;

    DefaultListModel connectioninfo = new DefaultListModel();

    ConnectionInfo infowindow;

    ConcreteProject(String filename) {
        this.filename = filename;
        Workbench.addToTab("Project", new ProjectTab(this), false);
        project_clock = 0;
        StringTokenizer token;
        int nr_comps = 0;
        int nr_connects = 0;
        try {
            RandomAccessFile project_file = new RandomAccessFile(filename, "r");
            String line = " ";
            line = project_file.readLine();
            line = project_file.readLine();
            if (line.indexOf("Version") > 0) {
                String verparts[] = line.split(" ");
                version = Double.parseDouble(verparts[2]);
                line = project_file.readLine();
            }
            line = project_file.readLine();
            while (!(line.charAt(0) == '#')) {
                line = project_file.readLine();
                nr_comps++;
            }
            line = project_file.readLine();
            while (!(line.charAt(0) == '#')) {
                line = project_file.readLine();
                nr_connects++;
            }
            comps = new Component[nr_comps];
            connects = new Connection[nr_connects];
            comp_types = new String[nr_comps];
            comp_data = new String[nr_comps];
            project_file.seek(0);
            while (!line.equals("# Components:")) line = project_file.readLine();
            for (int i = 0; i < nr_comps; i++) {
                token = new StringTokenizer(project_file.readLine());
                comp_types[i] = token.nextToken();
                if (token.hasMoreTokens()) comp_data[i] = token.nextToken();
                while (token.hasMoreTokens()) comp_data[i] = comp_data[i] + " " + token.nextToken();
            }
            createComponents();
            while (!line.equals("# Connections:")) line = project_file.readLine();
            for (int i = 0; i < nr_connects; i++) {
                token = new StringTokenizer(project_file.readLine(), ":,", false);
                String src = token.nextToken();
                String src_pin = token.nextToken();
                String dest = token.nextToken();
                String dest_pin = token.nextToken();
                connects[i] = new Connection(comps[Integer.valueOf(src).intValue()], Integer.valueOf(src_pin).intValue(), comps[Integer.valueOf(dest).intValue()], Integer.valueOf(dest_pin).intValue());
            }
            if (version >= 1.0) {
                while (!line.equals("# Connection info:")) line = project_file.readLine();
                for (int i = 0; i < nr_connects; i++) connectioninfo.addElement(project_file.readLine());
                infowindow = new ConnectionInfo(connectioninfo);
            }
        } catch (IOException e) {
            System.out.println("Error reading project file");
        }
        bottompanel = new BottomPanel(this, filename);
        postPinUpdate(null);
    }

    private void createComponents() {
        int i = 0;
        while (i < comp_types.length) {
            try {
                Class comp = Class.forName(comp_types[i]);
                comps[i] = (Component) comp.newInstance();
                comps[i].init(this, comp_data[i]);
            } catch (ClassNotFoundException e) {
                JOptionPane.showMessageDialog(Workbench.mainframe, "Couldn't find " + comp_types[i] + ".class", "Class not found", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            } catch (IllegalAccessException e) {
            } catch (InstantiationException e) {
            }
            i++;
        }
    }

    public void postPinUpdate(Component caller) {
        algebraicLoopDetection++;
        if (algebraicLoopDetection > 1000) {
            new Throwable("Algebraic loop detected").printStackTrace();
            stop();
            JOptionPane.showMessageDialog(Workbench.mainframe, "Try to add unit delays between components.", "Algebraic loop detected", JOptionPane.ERROR_MESSAGE);
        } else {
            for (int i = 0; i < connects.length; i++) connects[i].Update();
            for (int i = 0; i < comps.length; i++) comps[i].ioChangeNotify();
        }
        algebraicLoopDetection--;
    }

    public void clock() {
        algebraicLoopDetection = 0;
        project_clock++;
        for (int i = 0; i < comps.length; i++) comps[i].clock(project_clock);
    }

    public void start() {
        algebraicLoopDetection = 0;
        if (running == null) {
            for (int i = 0; i < comps.length; i++) comps[i].starting();
            last_run = 0;
            bottompanel.setStatus("running");
            running = new Thread(this);
            running.setPriority(Thread.NORM_PRIORITY);
            running.start();
        }
    }

    public void stop() {
        if (running != null) {
            running = null;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            for (int i = 0; i < comps.length; i++) comps[i].stopping();
            bottompanel.setStatus("stopped");
            bottompanel.setClock(project_clock, last_run);
        }
    }

    public void run() {
        while (running != null) {
            loop++;
            loop2++;
            last_run++;
            clock();
            if (loop2 >= 10000) {
                loop2 = 0;
                bottompanel.setClock(project_clock, last_run);
            }
            if (loop >= 500000) {
                loop = 0;
                bottompanel.setSpeed(project_clock);
            }
        }
    }

    public boolean isRunning() {
        if (running == null) return false; else return true;
    }

    public void actionPerformed(ActionEvent e) {
        System.out.println("action in project");
    }

    public String getFilename() {
        return filename;
    }

    public void createWindow(JInternalFrame window) {
        Workbench.createWindow(window);
    }

    public void addToTab(String label, JPanel panel, boolean newTab) {
        Workbench.addToTab(label, panel, newTab);
    }
}

class Connection {

    Component src;

    Component dest;

    int src_pin;

    int dest_pin;

    Connection(Component src, int src_pin, Component dest, int dest_pin) {
        this.src = src;
        this.dest = dest;
        this.src_pin = src_pin;
        this.dest_pin = dest_pin;
    }

    void Update() {
        dest.setInput(dest_pin, src.getOutput(src_pin));
    }
}
