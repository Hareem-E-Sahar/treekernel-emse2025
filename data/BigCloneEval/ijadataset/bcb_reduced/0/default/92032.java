import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.StringTokenizer;

public class ProjectWizard extends JInternalFrame implements ActionListener, MouseListener, FilenameFilter {

    static final int STARTUP = 0;

    static final int OPEN = 1;

    static final int NEW = 2;

    static final int MODIFY = 3;

    int step = 0;

    public long firstClickTime = 0;

    Container content;

    DefaultListModel listModel;

    DefaultListModel listModel_nr;

    DefaultListModel foundcomps;

    DefaultListModel connects;

    DefaultListModel connects_nr;

    File file;

    JLabel top_label;

    JTextField text;

    JPanel top_panel;

    JPanel center_panel;

    JPanel bottom_panel;

    JList list;

    JComboBox combo;

    JComboBox output;

    JComboBox output_pin;

    JComboBox input;

    JComboBox input_pin;

    JButton open;

    JButton newproject;

    JButton back;

    JButton next;

    JButton cancel;

    JButton finish;

    JButton add;

    JButton remove;

    JButton close;

    JScrollPane pane;

    String user_home;

    String picdev_projectdir;

    String project_name = "";

    String comps[];

    String compsInJar[] = { "PIC", "LEDS", "Segment7", "SquareToneGenerator", "OscilloscopProbe", "Togglebuttons", "Pushbuttons", "LogicAnd", "LogicOr", "LogicNot", "LogicXor", "Logic4511", "UnitDelay", "LCD", "MatrixPad", "QuadOR" };

    Comp components[];

    int mode;

    Font font;

    ProjectWizard(int width, int height, int mode) {
        this.mode = mode;
        setSize(500, 350);
        setLocation(width / 2 - 500 / 2, height / 2 - 350 / 2 - 40);
        setVisible(true);
        setOpaque(false);
        font = new Font("Arial", Font.BOLD, 30);
        text = new JTextField(15);
        content = this.getContentPane();
        content.setBackground(Color.white);
        content.setLayout(new BorderLayout());
        open = new JButton(" Open ");
        newproject = new JButton("  New  ");
        close = new JButton("Cancel");
        back = new JButton("< Back");
        next = new JButton("Next >");
        cancel = new JButton("Cancel");
        finish = new JButton("Finish");
        add = new JButton("Add");
        remove = new JButton("Remove");
        close.addActionListener(this);
        back.addActionListener(this);
        next.addActionListener(this);
        cancel.addActionListener(this);
        finish.addActionListener(this);
        add.addActionListener(this);
        remove.addActionListener(this);
        open.addActionListener(this);
        newproject.addActionListener(this);
        output_pin = new JComboBox();
        input_pin = new JComboBox();
        listModel = new DefaultListModel();
        listModel_nr = new DefaultListModel();
        connects = new DefaultListModel();
        connects_nr = new DefaultListModel();
        Workbench.createWindow(this);
        JFileChooser dummy = new JFileChooser();
        File selected = dummy.getCurrentDirectory();
        user_home = selected.getPath();
        picdev_projectdir = user_home + "/PIC Development Studio projects";
        if (mode == STARTUP || mode == OPEN) Step0();
        if (mode == NEW) {
            ReadInClasses();
            Step1();
        }
        if (mode == MODIFY) Step2(project_name);
    }

    void Step0() {
        content.removeAll();
        setTitle("Project Manager - Open or create a project");
        top_panel = new JPanel();
        top_panel.setSize(500, 50);
        top_panel.setLayout(new BorderLayout());
        Icon top_pic = new ImageIcon("images/wizard.gif", "");
        top_panel.add(new JLabel(top_pic), BorderLayout.WEST);
        Container mitten = new Container();
        mitten.setSize(100, 100);
        mitten.setLayout(new FlowLayout());
        center_panel = new JPanel();
        center_panel.setBackground(Color.white);
        center_panel.setLayout(new FlowLayout());
        file = new File(picdev_projectdir);
        if (!file.exists()) file.mkdirs();
        String files[] = file.list(this);
        for (int i = 0; i < files.length; i++) files[i] = files[i].substring(0, files[i].length() - 4);
        String filescopy[] = new String[files.length + 1];
        System.arraycopy(files, 0, filescopy, 1, files.length);
        filescopy[0] = "[more files...]";
        list = new JList(filescopy);
        list.setBackground(new Color(214, 211, 206));
        list.addMouseListener(this);
        pane = new JScrollPane(list);
        pane.setPreferredSize(new Dimension(340, 140));
        JLabel caption = new JLabel("Existing projects in PIC Development Studio projects folder: ");
        caption.setPreferredSize(new Dimension(340, 12));
        center_panel.add(caption);
        center_panel.add(pane);
        bottom_panel = new JPanel();
        bottom_panel.setSize(500, 50);
        bottom_panel.setLayout(new BorderLayout());
        JPanel knappar = new JPanel();
        knappar.setLayout(new FlowLayout());
        JPanel close_panel = new JPanel();
        FlowLayout flow = new FlowLayout();
        flow.setVgap(0);
        flow.setHgap(0);
        flow.setAlignment(FlowLayout.RIGHT);
        close_panel.setLayout(flow);
        close_panel.setPreferredSize(new Dimension(340, 30));
        close_panel.setBackground(Color.WHITE);
        back.setEnabled(false);
        if (mode == STARTUP) knappar.add(newproject);
        close_panel.add(open);
        center_panel.add(close_panel);
        knappar.add(close);
        bottom_panel.add(knappar, BorderLayout.EAST);
        bottom_panel.setBorder(new PanelBorder());
        content.add(top_panel, BorderLayout.NORTH);
        content.add(center_panel, BorderLayout.CENTER);
        content.add(bottom_panel, BorderLayout.SOUTH);
        list.requestFocus();
        list.grabFocus();
    }

    void Step1() {
        step++;
        content.removeAll();
        setTitle("Create a project - Step 1/3");
        top_panel = new JPanel();
        top_panel.setSize(500, 50);
        top_panel.setLayout(new BorderLayout());
        Icon top_pic = new ImageIcon("images/wizard.gif", "");
        top_panel.add(new JLabel(top_pic), BorderLayout.WEST);
        Container mitten = new Container();
        mitten.setSize(100, 100);
        mitten.setLayout(new FlowLayout());
        center_panel = new JPanel();
        center_panel.setBackground(Color.white);
        center_panel.setLayout(new FlowLayout());
        center_panel.add(new JLabel("Enter project name: "));
        center_panel.add(text);
        bottom_panel = new JPanel();
        bottom_panel.setSize(500, 50);
        bottom_panel.setLayout(new BorderLayout());
        JPanel knappar = new JPanel();
        knappar.setLayout(new FlowLayout());
        back.setEnabled(false);
        if (mode == NEW) back.setEnabled(false);
        knappar.add(back);
        knappar.add(next);
        knappar.add(cancel);
        bottom_panel.add(knappar, BorderLayout.EAST);
        bottom_panel.setBorder(new PanelBorder());
        content.add(top_panel, BorderLayout.NORTH);
        content.add(center_panel, BorderLayout.CENTER);
        content.add(bottom_panel, BorderLayout.SOUTH);
        text.requestFocus();
    }

    void Step2(String filename) {
        step++;
        content.removeAll();
        project_name = filename;
        setTitle("Adding components - Step 2/3");
        top_panel = new JPanel();
        top_panel.setSize(500, 50);
        top_panel.setLayout(new BorderLayout());
        Icon top_pic = new ImageIcon("images/wizard.gif", "");
        top_panel.add(new JLabel(top_pic), BorderLayout.WEST);
        Container mitten = new Container();
        mitten.setSize(100, 100);
        mitten.setLayout(new FlowLayout());
        center_panel = new JPanel();
        center_panel.setBackground(Color.white);
        center_panel.setLayout(new FlowLayout());
        center_panel.add(new JLabel("     Add the components you want to use in your project " + filename + "     "));
        list = new JList(listModel);
        list.setBackground(new Color(214, 211, 206));
        combo = new JComboBox();
        for (int i = 0; i < foundcomps.size(); i++) {
            Comp temp = (Comp) foundcomps.get(i);
            combo.addItem(temp.getDispName());
        }
        combo.setPreferredSize(new Dimension(200, 25));
        center_panel.add(combo);
        center_panel.add(add);
        center_panel.add(remove);
        pane = new JScrollPane(list);
        pane.setPreferredSize(new Dimension(300, 120));
        center_panel.add(pane);
        bottom_panel = new JPanel();
        bottom_panel.setSize(500, 50);
        bottom_panel.setLayout(new BorderLayout());
        JPanel knappar = new JPanel();
        knappar.setLayout(new FlowLayout());
        back.setEnabled(true);
        knappar.add(back);
        knappar.add(next);
        knappar.add(cancel);
        bottom_panel.add(knappar, BorderLayout.EAST);
        bottom_panel.setBorder(new PanelBorder());
        content.add(top_panel, BorderLayout.NORTH);
        content.add(center_panel, BorderLayout.CENTER);
        content.add(bottom_panel, BorderLayout.SOUTH);
    }

    void Step3() {
        step++;
        content.removeAll();
        setTitle("Connecting components - Step 3/3");
        top_panel = new JPanel();
        top_panel.setSize(500, 50);
        top_panel.setLayout(new BorderLayout());
        Icon top_pic = new ImageIcon("images/wizard.gif", "");
        top_panel.add(new JLabel(top_pic), BorderLayout.WEST);
        Container mitten = new Container();
        mitten.setLayout(new FlowLayout());
        center_panel = new JPanel();
        center_panel.setLayout(new GridLayout(2, 1));
        center_panel.setBackground(Color.white);
        Container out = new Container();
        out.setLayout(new FlowLayout());
        JLabel Output = new JLabel("Output:");
        Output.setPreferredSize(new Dimension(50, 25));
        out.add(Output);
        output = new JComboBox(comps);
        for (int i = 0; i < foundcomps.size(); i++) {
            String sel_comp = (String) listModel.get(0);
            Comp temp = (Comp) foundcomps.get(i);
            if (sel_comp.equals(temp.getDispName())) {
                String[] pinnames = temp.getPins();
                int j = 0;
                while (j < pinnames.length) output_pin.addItem(pinnames[j++]);
            }
        }
        output.setPreferredSize(new Dimension(200, 25));
        output.addActionListener(this);
        output_pin.setPreferredSize(new Dimension(80, 25));
        out.add(output);
        out.add(output_pin);
        Container in = new Container();
        in.setLayout(new FlowLayout());
        JLabel Input = new JLabel("Input:");
        Input.setPreferredSize(new Dimension(50, 25));
        in.add(Input);
        input = new JComboBox(comps);
        input.setPreferredSize(new Dimension(200, 25));
        input_pin.removeAllItems();
        for (int i = 0; i < foundcomps.size(); i++) {
            String sel_comp = (String) input.getSelectedItem();
            Comp temp = (Comp) foundcomps.get(i);
            if (sel_comp.equals(temp.getDispName())) {
                String[] pinnames = temp.getPins();
                int j = 0;
                while (j < pinnames.length) input_pin.addItem(pinnames[j++]);
            }
        }
        input_pin.setPreferredSize(new Dimension(80, 25));
        input.addActionListener(this);
        in.add(input);
        in.add(input_pin);
        center_panel.add(out);
        center_panel.add(in);
        combo.setPreferredSize(new Dimension(200, 25));
        list = new JList(connects);
        list.setBackground(new Color(214, 211, 206));
        pane = new JScrollPane(list);
        pane.setPreferredSize(new Dimension(340, 80));
        mitten.add(new JLabel("           Make all connection between the components your project needs            "));
        mitten.add(center_panel);
        add.setPreferredSize(new Dimension(80, 25));
        mitten.add(add);
        mitten.add(pane);
        remove.setPreferredSize(new Dimension(80, 25));
        mitten.add(remove);
        bottom_panel = new JPanel();
        bottom_panel.setSize(500, 50);
        bottom_panel.setLayout(new BorderLayout());
        bottom_panel.setBorder(new PanelBorder());
        JPanel knappar = new JPanel();
        knappar.setLayout(new FlowLayout());
        back.setEnabled(true);
        knappar.add(back);
        knappar.add(finish);
        knappar.add(cancel);
        bottom_panel.add(knappar, BorderLayout.EAST);
        content.add(top_panel, BorderLayout.NORTH);
        content.add(mitten, BorderLayout.CENTER);
        content.add(bottom_panel, BorderLayout.SOUTH);
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == close) {
            if (mode == STARTUP) System.exit(0); else {
                this.setVisible(false);
                dispose();
            }
        }
        if (evt.getSource() == back) {
            switch(step) {
                case 2:
                    step = 0;
                    Step1();
                    break;
                case 3:
                    step = 1;
                    Step2(project_name);
                    break;
            }
        }
        if (evt.getSource() == next) {
            switch(step) {
                case 1:
                    String name = new String(text.getText());
                    if (!name.equals("")) Step2(name);
                    break;
                case 2:
                    if (listModel.size() < 1) break;
                    comps = new String[listModel.size()];
                    for (int i = 0; i < listModel.size(); i++) comps[i] = (String) listModel.get(i);
                    Step3();
                    break;
            }
        }
        if (evt.getSource() == cancel) {
            if (mode == NEW || mode == MODIFY) {
                setVisible(false);
                dispose();
            } else {
                step = 0;
                Step0();
            }
        }
        if (evt.getSource() == add) {
            switch(step) {
                case 2:
                    if (combo.getSelectedItem().equals("PIC 16F84 Microcontroller")) {
                        String name = (String) combo.getSelectedItem();
                        if (listModel.contains(name)) {
                            listModel.addElement(name + " ");
                        } else listModel.addElement(name);
                        Comp temp = (Comp) foundcomps.get(combo.getSelectedIndex());
                        listModel_nr.addElement(temp.getName() + " " + project_name + "/" + project_name + ".asm");
                    } else {
                        String name = (String) combo.getSelectedItem();
                        if (listModel.contains(name)) {
                            listModel.addElement(name + " ");
                        } else listModel.addElement(name);
                        Comp temp = (Comp) foundcomps.get(combo.getSelectedIndex());
                        listModel_nr.addElement(temp.getName());
                    }
                    break;
                case 3:
                    String con = output.getSelectedItem() + " : " + output_pin.getSelectedItem() + " -> " + input.getSelectedItem() + " : " + input_pin.getSelectedItem();
                    String con_nr = output.getSelectedIndex() + ":" + output_pin.getSelectedIndex() + "," + input.getSelectedIndex() + ":" + input_pin.getSelectedIndex();
                    connects.addElement(con);
                    connects_nr.addElement(con_nr);
                    break;
            }
        }
        if (evt.getSource() == remove) {
            int index = list.getSelectedIndex();
            if (index != -1) {
                switch(step) {
                    case 2:
                        listModel.remove(index);
                        listModel_nr.remove(index);
                        connects = new DefaultListModel();
                        connects_nr = new DefaultListModel();
                        break;
                    case 3:
                        connects.remove(index);
                        connects_nr.remove(index);
                        break;
                }
            }
        }
        if (evt.getSource() == output) {
            output_pin.removeAllItems();
            for (int i = 0; i < foundcomps.size(); i++) {
                String sel_comp = ((String) output.getSelectedItem()).trim();
                Comp temp = (Comp) foundcomps.get(i);
                if (sel_comp.equals(temp.getDispName())) {
                    String[] pinnames = temp.getPins();
                    int j = 0;
                    while (j < pinnames.length) output_pin.addItem(pinnames[j++]);
                }
            }
        }
        if (evt.getSource() == input) {
            input_pin.removeAllItems();
            for (int i = 0; i < foundcomps.size(); i++) {
                String sel_comp = ((String) input.getSelectedItem()).trim();
                Comp temp = (Comp) foundcomps.get(i);
                if (sel_comp.equals(temp.getDispName())) {
                    String[] pinnames = temp.getPins();
                    int j = 0;
                    while (j < pinnames.length) input_pin.addItem(pinnames[j++]);
                }
            }
        }
        if (evt.getSource() == finish) {
            JFileChooser projectsaver = new JFileChooser(picdev_projectdir);
            projectsaver.setDialogTitle("Save Project as");
            ExampleFileFilter filter = new ExampleFileFilter();
            filter.addExtension("pds");
            filter.setDescription("PIC Development Studio project");
            projectsaver.setFileFilter(filter);
            File project_filename = new File(picdev_projectdir + "/" + project_name + ".pds");
            projectsaver.setSelectedFile(project_filename);
            if (projectsaver.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    project_filename = projectsaver.getSelectedFile();
                    RandomAccessFile project_file = new RandomAccessFile(project_filename, "rw");
                    project_file.writeBytes("# Do NOT modify the project file manually!!\n");
                    project_file.writeBytes("# Version 1.0\n");
                    project_file.writeBytes("# Components:\n");
                    for (int i = 0; i < listModel.size(); i++) project_file.writeBytes((String) listModel_nr.get(i) + "\n");
                    project_file.writeBytes("# Connections:\n");
                    for (int i = 0; i < connects_nr.size(); i++) project_file.writeBytes((String) connects_nr.get(i) + "\n");
                    project_file.writeBytes("# Connection info:\n");
                    for (int i = 0; i < connects.size(); i++) project_file.writeBytes((String) connects.get(i) + "\n");
                    project_file.writeBytes("# End");
                    project_file.close();
                    String project_path = project_filename.getPath();
                    project_path = project_path.substring(0, Math.max(project_path.lastIndexOf('/'), project_path.lastIndexOf('\\'))) + "/" + project_name;
                    File project_dir = new File(project_path);
                    project_dir.mkdir();
                    for (int i = 0; i < listModel.size(); i++) if (((String) listModel.get(i)).equals("PIC 16F84 Microcontroller")) {
                        File template = new File(System.getProperty("user.dir") + "/pic16f84 template.asm");
                        File dest = new File(project_path + "/" + project_name + ".asm");
                        CopyFile(template, dest);
                    }
                } catch (IOException e) {
                    System.out.println("Error writing project file");
                }
                this.setVisible(false);
                dispose();
                Workbench.loadProject(project_filename.getPath());
            }
        }
        if (evt.getSource() == open) {
            if (list.getSelectedIndex() != -1) {
                open();
            }
        }
        if (evt.getSource() == newproject) {
            ReadInClasses();
            Step1();
        }
    }

    public void open() {
        String in_list = (String) list.getSelectedValue();
        if (in_list.equals("[more files...]")) {
            JFileChooser projectloader = new JFileChooser();
            if (projectloader.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File selected = projectloader.getSelectedFile();
                Workbench.loadProject(selected.getPath());
                this.setVisible(false);
                dispose();
            }
        } else {
            Workbench.loadProject(picdev_projectdir + "/" + (String) list.getSelectedValue() + ".pds");
            this.setVisible(false);
            dispose();
        }
    }

    public void mouseClicked(MouseEvent evt) {
        long clickTime = System.currentTimeMillis();
        long clickInterval = clickTime - firstClickTime;
        if (clickInterval < 300) {
            open();
        } else firstClickTime = clickTime;
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    void ReadInClasses() {
        file = new File(System.getProperty("user.dir"));
        foundcomps = new DefaultListModel();
        StringTokenizer token;
        String filename = "";
        String filetype = "";
        String classes[] = file.list();
        for (int i = 0; i < compsInJar.length; i++) {
            try {
                Class comp = Class.forName(compsInJar[i]);
                Object temp = comp.newInstance();
                if (temp instanceof Component) {
                    Component addthis = (Component) temp;
                    foundcomps.addElement(new Comp(compsInJar[i], addthis.getName(), addthis.getPinNames()));
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < classes.length; i++) {
            token = new StringTokenizer(classes[i], ".\0", false);
            int nr = token.countTokens();
            filename = token.nextToken();
            if (nr == 2) {
                filetype = token.nextToken();
                if (filetype.equals("class")) {
                    Object temp = new Object();
                    try {
                        if (!(filename.equals("Workbench") | filename.equals("DebugWindow") | filename.equals("RamDebugWindow") | filename.equals("CpuDebugWindow") | filename.equals("BreakpointDebugWindow") | filename.equals("EEPROMDebugWindow") | filename.equals("HelpBrowser") | filename.equals("Logic") | filename.equals("Splash"))) {
                            Class comp = Class.forName(filename);
                            temp = comp.newInstance();
                            if (temp instanceof Component) {
                                Component addthis = (Component) temp;
                                System.out.println("found custom component: " + classes[i]);
                                foundcomps.addElement(new Comp(filename, addthis.getName(), addthis.getPinNames()));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(Workbench.mainframe, "An error occured while loading component: " + classes[i], "Error loading component", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    public boolean accept(File dir, String name) {
        if (name.length() > 4 && name.substring(name.length() - 4, name.length()).compareToIgnoreCase(".pds") == 0) return true; else return false;
    }

    public void CopyFile(File in, File out) {
        try {
            FileInputStream fis = new FileInputStream(in);
            FileOutputStream fos = new FileOutputStream(out);
            byte[] buf = new byte[1024];
            int i = 0;
            while ((i = fis.read(buf)) != -1) {
                fos.write(buf, 0, i);
            }
            fis.close();
            fos.close();
        } catch (Exception e) {
            System.out.println("Error copying template file");
        }
    }
}

class Comp {

    String name;

    String disp_name;

    String[] pins;

    Comp(String name, String disp_name, String[] pins) {
        this.name = name;
        this.disp_name = disp_name;
        this.pins = pins;
    }

    String getName() {
        return name;
    }

    String getDispName() {
        return disp_name;
    }

    String[] getPins() {
        return pins;
    }
}
