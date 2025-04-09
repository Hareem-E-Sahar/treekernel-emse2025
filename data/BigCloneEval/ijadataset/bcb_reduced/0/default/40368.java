import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import javax.swing.border.EmptyBorder;
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JFileChooser;
import java.io.FileNotFoundException;
import javax.swing.SwingWorker;
import java.util.EventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * The Display class is the graphical user interface of the simulator that
 * the user interacts with to load test data files and watch operating
 * system simulation runs. Most of the methods are factory methods, which
 * construct new custom GUI components.
 *
 * @author Timmy Yee
 * @author Miaoer Yu
 */
public final class Display {

    private static int displayInterval_ = 5000;

    private static JButton flow_;

    /**
     * A private constructor to prevent instantiating this static class.
     */
    private Display() {
    }

    /**
     * Gets the queue content display interval for the trace.
     *
     * @return The queue content display interval.
     */
    public static int getDisplayInterval() {
        return displayInterval_;
    }

    /**
     * Sets the queue content display interval for the trace.
     *
     * @param displayInterval The queue content display interval.
     */
    public static void setDisplayInterval(int displayInterval) {
        displayInterval_ = displayInterval;
    }

    /**
     * Creates a button for the user interface.
     *
     * @param label Label for the button.
     * @param al    Listener for the button.
     * @return      Button for the user interface.
     */
    private static JButton newButton(String label, EventListener al) {
        JButton button = new JButton(label);
        button.addActionListener((ActionListener) al);
        return button;
    }

    /**
     * Creates a combo box for the user interface.
     *
     * @param s     List of options to display in combo box.
     * @param i     Index of default selected option.
     * @param al    Listener for the combo box.
     * @return      Combo box for the user interface.
     */
    private static JComboBox newComboBox(String[] s, int i, EventListener al) {
        JComboBox cb = new JComboBox(s);
        cb.setSelectedIndex(i);
        cb.addActionListener((ActionListener) al);
        return cb;
    }

    /**
     * Creates a spinner box for the user interface.
     *
     * @param initial   Initial value on the spinner.
     * @param cl        Listener for the spinner.
     * @return          Labeled spinner for the user interface.
     */
    private static JComponent newSpinner(int initial, EventListener cl) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
        JSpinner s = new JSpinner(new SpinnerNumberModel(initial, 0, Integer.MAX_VALUE, 1));
        s.addChangeListener((ChangeListener) cl);
        p.add(s);
        p.add(new JLabel(" msec"));
        return p;
    }

    /**
     * Creates a row of components.
     *
     * @param c1    Left component.
     * @param c2    Right component.
     * @return      A packed horizontal-oriented panel.
     */
    private static JPanel newRowPanel(JComponent c1, JComponent c2) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.LINE_AXIS));
        row.add(c1);
        row.add(c2);
        return row;
    }

    /**
     * Creates the top panel of the user interface.
     * This panel contains the trace text area.
     *
     * @return Constructed top panel.
     */
    private static JPanel newTopPanel() {
        JPanel panel = new JPanel();
        Font font = new Font("monospaced", Font.PLAIN, 12);
        Trace.initialize(font);
        panel.add(new JScrollPane(Trace.getTextArea()));
        return panel;
    }

    /**
     * Creates the center panel of the user interface.
     * This panel contains the user input interface.
     *
     * @return Constructed center panel.
     */
    private static JPanel newCenterPanel() {
        String[] procAlgorithms = { "Preemptive Priority", "Non-preemptive Priority", "Round Robin" };
        String[] memAlgorithms = { "Clock LRU (Second-chance)", "Random Replacement" };
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        JComponent compLeft, compRight;
        EventListener listen;
        listen = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                switch(((JComboBox) e.getSource()).getSelectedIndex()) {
                    case 0:
                        Scheduler.setProcessAlgorithm(Algorithm.PREEMPT);
                        return;
                    case 1:
                        Scheduler.setProcessAlgorithm(Algorithm.NON_PREEMPT);
                        return;
                    case 2:
                        Scheduler.setProcessAlgorithm(Algorithm.ROUND_ROBIN);
                        return;
                }
            }
        };
        compLeft = new JLabel("Process Scheduling Algorithm: ");
        compRight = newComboBox(procAlgorithms, 2, listen);
        panel.add(newRowPanel(compLeft, compRight));
        listen = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                switch(((JComboBox) e.getSource()).getSelectedIndex()) {
                    case 0:
                        Scheduler.setMemoryAlgorithm(Algorithm.CLOCK);
                        return;
                    case 1:
                        Scheduler.setMemoryAlgorithm(Algorithm.RANDOM);
                        return;
                }
            }
        };
        compLeft = new JLabel("Memory Management Algorithm: ");
        compRight = newComboBox(memAlgorithms, 0, listen);
        panel.add(newRowPanel(compLeft, compRight));
        panel.add(new JLabel(" "));
        listen = new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                JSpinner s = (JSpinner) e.getSource();
                Processor.setTimeSlice((Integer) s.getValue());
            }
        };
        compLeft = new JLabel("Time slice: ");
        compRight = newSpinner(Processor.getTimeSlice(), listen);
        panel.add(newRowPanel(compLeft, compRight));
        listen = new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                JSpinner s = (JSpinner) e.getSource();
                Processor.setContextSwitch((Integer) s.getValue());
            }
        };
        compLeft = new JLabel("Context switch: ");
        compRight = newSpinner(Processor.getContextSwitch(), listen);
        panel.add(newRowPanel(compLeft, compRight));
        listen = new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                JSpinner s = (JSpinner) e.getSource();
                setDisplayInterval((Integer) s.getValue());
            }
        };
        compLeft = new JLabel("Display info every: ");
        compRight = newSpinner(getDisplayInterval(), listen);
        panel.add(newRowPanel(compLeft, compRight));
        return panel;
    }

    /**
     * Creates the bottom panel of the user interface.
     * This panel contains the buttons.
     *
     * @param frame The Display GUI frame.
     * @return      Constructed bottom panel.
     */
    private static JPanel newBottomPanel(final JFrame frame) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        flow_ = newButton("Pause", new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (Simulator.isPaused()) {
                    Simulator.resume();
                    flow_.setText("Pause");
                } else {
                    Simulator.pause();
                    flow_.setText("Resume");
                }
            }
        });
        final JFileChooser chooser = new JFileChooser(".");
        EventListener listen = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SwingWorker<Void, Void> worker = null;
                int value;
                Simulator.pause();
                value = chooser.showOpenDialog(frame);
                Simulator.resume();
                if (value == JFileChooser.APPROVE_OPTION) try {
                    Simulator.readTestData(chooser.getSelectedFile());
                } catch (FileNotFoundException ex) {
                    return;
                } else if (value == JFileChooser.CANCEL_OPTION) return;
                Simulator.pause();
                if (worker != null) worker.cancel(true);
                worker = new SwingWorker<Void, Void>() {

                    public Void doInBackground() {
                        Trace.clear();
                        Simulator.resume();
                        Simulator.mainLoop();
                        return null;
                    }
                };
                worker.execute();
            }
        };
        panel.add(newRowPanel(flow_, newButton("Load Test", listen)));
        return panel;
    }

    /**
     * Creates the graphical user interface of the simulator.
     *
     * @param frame The Display GUI frame.
     * @return      The entire panel for the simulator GUI.
     */
    public static JPanel newWholePanel(JFrame frame) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.add(newTopPanel(), BorderLayout.PAGE_START);
        panel.add(newCenterPanel(), BorderLayout.CENTER);
        panel.add(newBottomPanel(frame), BorderLayout.PAGE_END);
        return panel;
    }
}
