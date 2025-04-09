import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import javax.swing.AbstractListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.GroupLayout.Alignment;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

/**
 * Sample application demonstrating the usage of SystemManager.
 *
 * @see SystemManager
 * @author Marius Mikucionis
 */
public class SystemMonitor extends JFrame implements Runnable, ListSelectionListener {

    SystemManager sm;

    StatusBar virtmem = new StatusBar("Virtual memory usage: ", " MB");

    StatusBar sysmem = new StatusBar("Physical memory usage: ", " MB");

    StatusBar swapmem = new StatusBar("Swap usage: ", " MB");

    StatusBar procvirt = new StatusBar("Virtual memory: ", " MB");

    StatusBar procmem = new StatusBar("Resident memory: ", " MB");

    StatusBar procswap = new StatusBar("Swap usage: ", " MB");

    StatusBar proccpu = new StatusBar("CPU usage: ", " %");

    StatusBar procsys = new StatusBar("CPU usage in kernel: ", " %");

    int processID = 0;

    class ProcList extends AbstractListModel {

        int[] procs = null;

        int size = 0;

        public synchronized void setList(int nopids, int[] pids) {
            if (procs != null && size > 0) fireIntervalRemoved(this, 0, size - 1);
            procs = pids;
            if (procs == null) size = 0; else if (procs.length < nopids) size = procs.length; else size = nopids;
            if (procs != null && size > 0) fireIntervalAdded(this, 0, size - 1);
        }

        public synchronized int getPIDAt(int i) {
            if (procs != null && i < size) return procs[i]; else return 0;
        }

        public synchronized Object getElementAt(int i) {
            if (i >= size) return null;
            String name = "<can't open>";
            int handle = sm.openProcessHandle(procs[i]);
            if (handle != 0) {
                name = sm.getProcessName(handle);
                if (name == null) name = "<unavailable>";
                sm.closeProcessHandle(handle);
            }
            return name + " (" + procs[i] + ")";
        }

        public synchronized int getSize() {
            return size;
        }
    }

    ;

    class ProcessConsole extends JTextArea implements Runnable {

        String cmdline;

        Process proc = null;

        boolean started = false, killed = false;

        public ProcessConsole() {
            setEditable(false);
            setToolTipText("Standard output stream from the executed command");
        }

        public void killProcess(String reason) {
            if (proc != null) {
                killed = true;
                proc.destroy();
                status.setText("Killed: " + reason);
            }
        }

        public void exec(String command) {
            cmdline = command;
            int begin = 0;
            while (begin < cmdline.length() && !Character.isLetterOrDigit(cmdline.charAt(begin))) ++begin;
            int end = begin;
            while (end < cmdline.length() && Character.isLetterOrDigit(cmdline.charAt(end))) ++end;
            exe = cmdline.substring(begin, end);
            System.out.println("Will search for '" + exe + "' in process name.");
            started = false;
            killed = false;
            new Thread(this).start();
            synchronized (this) {
                try {
                    while (!started) wait();
                } catch (InterruptedException ie) {
                }
            }
        }

        public void run() {
            if (cmdline == null) return;
            BufferedReader inp = null;
            String text = new String(), line;
            cmdfield.setEnabled(false);
            list.setEnabled(false);
            try {
                proc = Runtime.getRuntime().exec(cmdline);
                inp = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                synchronized (this) {
                    started = true;
                    notifyAll();
                }
                line = inp.readLine();
                while (line != null) {
                    text += line + "\n";
                    setText(text);
                    setCaretPosition(text.length());
                    line = inp.readLine();
                }
            } catch (IOException ioe) {
                text += ioe.getMessage() + "\n";
                synchronized (this) {
                    started = true;
                    notifyAll();
                }
                proc = null;
            }
            if (proc != null) {
                try {
                    int code = proc.waitFor();
                    if (killed) status.setText(status.getText() + " (code " + code + ")"); else status.setText("Exited with code: " + code);
                } catch (InterruptedException ie) {
                }
                sm.fetchProcessStatus(hProcess, ps);
                times.setText(ps.getTimes());
                memory.setText(ps.getMemory());
                proc = null;
            }
            setText(text);
            setCaretPosition(text.length());
            exe = null;
            cmdfield.setEnabled(true);
            list.setEnabled(true);
        }
    }

    int[] pids = new int[256];

    private void refreshPidList() {
        int retry = 5;
        do {
            int nopids = sm.fetchProcessIDs(pids);
            if (nopids > 0) proclist.setList(nopids, pids);
            if (exe == null) return;
            for (int i = 0; i < nopids; ++i) {
                int h = sm.openProcessHandle(pids[i]);
                String name = sm.getProcessName(h);
                if (name != null && name.contains(exe)) {
                    list.setSelectedIndex(i);
                    list.ensureIndexIsVisible(i);
                    sm.closeProcessHandle(h);
                    return;
                }
                sm.closeProcessHandle(h);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        } while (--retry > 0);
    }

    ActionListener alistener = new ActionListener() {

        public void actionPerformed(ActionEvent ae) {
            String cmd = ae.getActionCommand();
            if (ae.getSource() instanceof JButton && "Refresh".equals(cmd)) refreshPidList(); else if (ae.getSource() instanceof JTextField) {
                pc.exec(cmd);
                refreshPidList();
            }
        }
    };

    ProcList proclist = new ProcList();

    JList list = new JList(proclist);

    SystemMemoryStatus ms = new SystemMemoryStatus();

    ProcessStatus ps = new ProcessStatus();

    ProcessConsole pc = null;

    String exe = null;

    JCheckBox swapcheck = new JCheckBox("", false);

    JCheckBox cpucheck = new JCheckBox("", false);

    JCheckBox virtcheck = new JCheckBox("", true);

    JLabel statuslabel = new JLabel("Status:");

    JLabel status = new JLabel("not selected");

    JLabel timeslabel = new JLabel("Times (real/user/system):");

    JLabel times = new JLabel("<not updated yet>");

    JLabel memorylabel = new JLabel("Memory peaks (virtual/resident/swap):");

    JLabel memory = new JLabel("<not updated yet>");

    JTextField cmdfield = new JTextField("cpumemhog");

    private void createLayout() {
        setSize(800, 600);
        JPanel left = new JPanel();
        JButton refresh = new JButton("Refresh");
        refresh.setToolTipText("Refresh the list of processes below");
        refresh.addActionListener(alistener);
        list.setToolTipText("List of running processes in the system together with their process IDs. Select a process to display status information about it.");
        cmdfield.setToolTipText("Type a command line here and press enter to execute it.");
        left.setLayout(new BorderLayout());
        left.add(new JScrollPane(list), BorderLayout.CENTER);
        left.add(refresh, BorderLayout.SOUTH);
        JPanel system = new JPanel();
        system.setToolTipText("System status information, automatically refreshed every 2 seconds.");
        system.setBorder(new TitledBorder("System information"));
        JLabel swaplabel = new JLabel("Kill if system swap increases by 5%");
        JLabel cpulabel = new JLabel("Kill if CPU usage is below 5%");
        JLabel virtlabel = new JLabel("Kill if virtual > system memory");
        GroupLayout syslayout = new GroupLayout(system);
        system.setLayout(syslayout);
        syslayout.setAutoCreateGaps(true);
        syslayout.setAutoCreateContainerGaps(true);
        GroupLayout.SequentialGroup hGroup = syslayout.createSequentialGroup();
        hGroup.addGroup(syslayout.createParallelGroup(Alignment.TRAILING).addComponent(virtmem.getLabel()).addComponent(sysmem.getLabel()).addComponent(swapmem.getLabel()));
        hGroup.addGroup(syslayout.createParallelGroup(Alignment.LEADING).addComponent(virtmem).addComponent(sysmem).addComponent(swapmem));
        syslayout.setHorizontalGroup(hGroup);
        GroupLayout.SequentialGroup vGroup = syslayout.createSequentialGroup();
        vGroup.addGroup(syslayout.createParallelGroup(Alignment.BASELINE).addComponent(virtmem.getLabel()).addComponent(virtmem));
        vGroup.addGroup(syslayout.createParallelGroup(Alignment.BASELINE).addComponent(sysmem.getLabel()).addComponent(sysmem));
        vGroup.addGroup(syslayout.createParallelGroup(Alignment.BASELINE).addComponent(swapmem.getLabel()).addComponent(swapmem));
        syslayout.setVerticalGroup(vGroup);
        JPanel process = new JPanel();
        process.setToolTipText("Status information about the selected process, refreshed every 2 seconds.");
        process.setBorder(new TitledBorder("Process information"));
        GroupLayout pslayout = new GroupLayout(process);
        process.setLayout(pslayout);
        pslayout.setAutoCreateGaps(true);
        pslayout.setAutoCreateContainerGaps(true);
        hGroup = pslayout.createSequentialGroup();
        hGroup.addGroup(pslayout.createParallelGroup(Alignment.TRAILING).addComponent(procvirt.getLabel()).addComponent(procmem.getLabel()).addComponent(procswap.getLabel()).addComponent(proccpu.getLabel()).addComponent(procsys.getLabel()).addComponent(swaplabel).addComponent(cpulabel).addComponent(virtlabel).addComponent(statuslabel).addComponent(timeslabel).addComponent(memorylabel));
        hGroup.addGroup(pslayout.createParallelGroup(Alignment.LEADING).addComponent(procvirt).addComponent(procmem).addComponent(procswap).addComponent(proccpu).addComponent(procsys).addComponent(swapcheck).addComponent(cpucheck).addComponent(virtcheck).addComponent(status).addComponent(times).addComponent(memory));
        pslayout.setHorizontalGroup(hGroup);
        vGroup = pslayout.createSequentialGroup();
        vGroup.addGroup(pslayout.createParallelGroup(Alignment.BASELINE).addComponent(procvirt.getLabel()).addComponent(procvirt));
        vGroup.addGroup(pslayout.createParallelGroup(Alignment.BASELINE).addComponent(procmem.getLabel()).addComponent(procmem));
        vGroup.addGroup(pslayout.createParallelGroup(Alignment.BASELINE).addComponent(procswap.getLabel()).addComponent(procswap));
        vGroup.addGroup(pslayout.createParallelGroup(Alignment.BASELINE).addComponent(proccpu.getLabel()).addComponent(proccpu));
        vGroup.addGroup(pslayout.createParallelGroup(Alignment.BASELINE).addComponent(procsys.getLabel()).addComponent(procsys));
        vGroup.addGroup(pslayout.createParallelGroup(Alignment.BASELINE).addComponent(swaplabel).addComponent(swapcheck));
        vGroup.addGroup(pslayout.createParallelGroup(Alignment.BASELINE).addComponent(cpulabel).addComponent(cpucheck));
        vGroup.addGroup(pslayout.createParallelGroup(Alignment.BASELINE).addComponent(virtlabel).addComponent(virtcheck));
        vGroup.addGroup(pslayout.createParallelGroup(Alignment.BASELINE).addComponent(statuslabel).addComponent(status));
        vGroup.addGroup(pslayout.createParallelGroup(Alignment.BASELINE).addComponent(timeslabel).addComponent(times));
        vGroup.addGroup(pslayout.createParallelGroup(Alignment.BASELINE).addComponent(memorylabel).addComponent(memory));
        pslayout.setVerticalGroup(vGroup);
        JPanel console = new JPanel();
        console.setBorder(new TitledBorder("Console"));
        console.setLayout(new BorderLayout());
        cmdfield.addActionListener(alistener);
        console.add(cmdfield, BorderLayout.NORTH);
        console.add(new JScrollPane(pc), BorderLayout.CENTER);
        JPanel right = new JPanel();
        right.setLayout(new BorderLayout());
        GridBagLayout gbl = new GridBagLayout();
        right.setLayout(gbl);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        gbl.setConstraints(system, c);
        right.add(system);
        c.gridx = 0;
        c.gridy = 1;
        gbl.setConstraints(process, c);
        right.add(process);
        c.gridx = 0;
        c.gridy = 2;
        gbl.setConstraints(console, c);
        right.add(console);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setDividerLocation(0.25);
        split.setResizeWeight(0.25);
        add(split);
        setVisible(true);
        split.setDividerLocation(0.25);
        cmdfield.requestFocusInWindow();
    }

    public SystemMonitor() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                if (pc != null) pc.killProcess("Closing");
            }
        });
        setTitle("System Monitor");
        sm = SystemManager.getSystemManager();
        if (sm == null) {
            System.out.println("Can't find SystemMonitor implementation for your OS, sorry");
            System.exit(1);
        }
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(this);
        pc = new ProcessConsole();
        createLayout();
        new Thread(this).start();
        refreshPidList();
    }

    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            JList list = (JList) e.getSource();
            int n = list.getSelectedIndex();
            if (n >= 0) {
                int pid = proclist.getPIDAt(n);
                ps.resetPeaks();
                setProcessID(pid);
                status.setText("selected PID=" + pid);
                times.setText("<not yet updated>");
                memory.setText("<not yet updated>");
            }
        }
    }

    int hProcess = 0;

    public synchronized void setProcessID(int PID) {
        if (hProcess != 0) {
            sm.closeProcessHandle(hProcess);
            hProcess = 0;
        }
        if (PID != 0) hProcess = sm.openProcessHandle(PID);
        notifyAll();
    }

    public synchronized void run() {
        if (!sm.fetchSystemMemoryStatus(ms)) {
            System.err.println("Could not get system memory status");
            return;
        }
        virtmem.setMaxValue((int) (ms.virtualTotal >> 20));
        sysmem.setMaxValue((int) (ms.physTotal >> 20));
        swapmem.setMaxValue((int) (ms.swapTotal >> 20));
        procvirt.setMaxValue((int) (ms.virtualTotal >> 20));
        procmem.setMaxValue((int) (ms.physTotal >> 20));
        procswap.setMaxValue((int) (ms.swapTotal >> 20));
        proccpu.setMaxValue(100);
        procsys.setMaxValue(100);
        long oldswap = ms.swapTotal - ms.swapAvail, nowswap = ms.swapTotal - ms.swapAvail;
        long oldreal = 0, olduser = 0, oldsys = 0;
        double duration = 0, utime = 0, stime = 0;
        if (hProcess != 0) {
            sm.fetchProcessStatus(hProcess, ps);
            oldreal = ps.timestamp;
            olduser = ps.userTime;
            oldsys = ps.systemTime;
        }
        try {
            int oldh = hProcess;
            boolean valid = false;
            while (true) {
                wait(2000);
                sm.fetchSystemMemoryStatus(ms);
                if (swapcheck.isSelected()) {
                    nowswap = ms.swapTotal - ms.swapAvail;
                    if ((nowswap - oldswap) * 100 / oldswap > 5) pc.killProcess("swap increased more than 5%");
                }
                if (hProcess != 0) valid = sm.fetchProcessStatus(hProcess, ps); else valid = false;
                if (valid) {
                    if (hProcess == oldh) {
                        duration = (ps.timestamp - oldreal);
                        if (duration > 500000) {
                            utime = (ps.userTime - olduser) * 100 / duration;
                            stime = (ps.systemTime - oldsys) * 100 / duration;
                        } else {
                            utime = 0;
                            stime = 0;
                        }
                        procvirt.setValue((int) (ps.virtualSize >> 20));
                        procmem.setValue((int) (ps.residentSize >> 20));
                        procswap.setValue((int) (ps.swapSize >> 20));
                        proccpu.setValue((int) utime);
                        procsys.setValue((int) stime);
                        if (cpucheck.isSelected() && utime + stime < 5) pc.killProcess("CPU consumption was < 5%");
                        if (virtcheck.isSelected() && ps.virtualSize > ms.physTotal) pc.killProcess("virtual > system memory");
                        times.setText(ps.getTimes());
                        memory.setText(ps.getMemory());
                    } else oldh = hProcess;
                    olduser = ps.userTime;
                    oldsys = ps.systemTime;
                    oldreal = ps.timestamp;
                } else {
                    procvirt.setValue(0);
                    procmem.setValue(0);
                    procswap.setValue(0);
                    proccpu.setValue(0);
                    procsys.setValue(0);
                }
                virtmem.setValue((int) ((ms.virtualTotal - ms.virtualAvail) >> 20));
                sysmem.setValue((int) ((ms.physTotal - ms.physAvail) >> 20));
                swapmem.setValue((int) ((ms.swapTotal - ms.swapAvail) >> 20));
            }
        } catch (InterruptedException e) {
        }
    }

    public static void main(String args[]) {
        SystemMonitor sm = new SystemMonitor();
    }
}
