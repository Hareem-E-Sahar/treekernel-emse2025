package se.kth.speech.skatta.player.media;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.util.List;

public class FolderParser implements StimuliParser {

    private JPanel m_panel;

    private StimuliParser[] m_parsers;

    private File[] m_files;

    private int[][] m_order;

    private int m_spp;

    private int m_innerCount;

    private int m_current;

    private boolean m_multiStimuliParsers;

    private int m_sequences;

    private Dimension m_maxsize;

    private Dimension m_maxsize_per_stimulus;

    private Dimension m_maxsize_all_but_one_stimulus;

    private boolean m_random;

    private int m_controls;

    /**
     * Creates a parser for a folder.
     *
     * @param maxsize The maximal size for the panel.
     * @param spp The number of stimuli to display per page.
     * @param sequences how many sequences all stimuli should be displayed.
     * @param controls A sum of ControlButton constants.
     */
    public FolderParser(Dimension maxsize, int spp, int sequences, int controls) {
        this(maxsize, spp, sequences, controls, true);
    }

    public FolderParser(Dimension maxsize, int spp, int sequences, int controls, boolean random) {
        m_random = random;
        m_maxsize = maxsize;
        m_controls = controls;
        if (spp != 0) {
            m_maxsize_per_stimulus = new Dimension(maxsize.width / spp - 20, maxsize.height);
            m_maxsize_all_but_one_stimulus = new Dimension(maxsize.width - maxsize.width / spp - 20, maxsize.height);
        } else {
            m_maxsize_per_stimulus = maxsize;
            m_maxsize_all_but_one_stimulus = new Dimension(0, 0);
        }
        m_spp = spp;
        m_sequences = sequences;
        m_panel = new JPanel();
        m_panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
    }

    /**
     * Sets the source file to get the current stimuli from.
     * Also resets the internal clock.
     */
    public synchronized boolean setSource(File file) {
        if (!file.isDirectory()) {
            return false;
        }
        m_current = -1;
        Random rand = new Random();
        m_files = file.listFiles(new ValidFileFilter());
        if (m_random) {
            List<File> fileList = Arrays.asList(m_files);
            Collections.shuffle(fileList, rand);
            m_files = fileList.toArray(m_files);
        }
        m_innerCount = 0;
        m_multiStimuliParsers = false;
        List<int[]> orderList = new LinkedList<int[]>();
        int[] order = null;
        StimuliParser parser = new FolderParser(m_maxsize, m_spp, 1, m_controls, false);
        StimuliParser parser2 = null;
        if (m_spp > 1) parser2 = new FolderParser(m_maxsize, m_spp - 1, 1, m_controls, false);
        if (parser.setSource(m_files[0])) {
            List<int[]> singleOrderList = new LinkedList<int[]>();
            m_multiStimuliParsers = true;
            order = new int[2];
            order[0] = 0;
            int thiscount = parser.getInnerStimuliCount();
            for (int i = 0; i < thiscount; ++i) {
                order[1] = i;
                singleOrderList.add(order.clone());
            }
            m_innerCount += thiscount;
            if (m_random) {
                Collections.shuffle(singleOrderList, rand);
            }
            orderList.addAll(singleOrderList);
        } else if (m_spp > 1) {
            File tiedFolder = getTiedFolder(m_files[0]);
            if (tiedFolder.isDirectory()) {
                List<int[]> singleOrderList = new LinkedList<int[]>();
                m_multiStimuliParsers = true;
                parser2.setSource(tiedFolder);
                order = new int[2];
                order[0] = 0;
                int thiscount = parser2.getInnerStimuliCount();
                for (int i = 0; i < thiscount; ++i) {
                    order[1] = i;
                    singleOrderList.add(order.clone());
                }
                m_innerCount += thiscount;
                if (m_random) Collections.shuffle(singleOrderList, rand);
                orderList.addAll(singleOrderList);
            }
        }
        if (m_multiStimuliParsers) {
            for (int i = 1; i < m_files.length; ++i) {
                List<int[]> singleOrderList = new LinkedList<int[]>();
                order[0] = i;
                int thiscount;
                if (!parser.setSource(m_files[i])) {
                    File tiedFolder = getTiedFolder(m_files[i]);
                    parser2.setSource(tiedFolder);
                    thiscount = parser2.getInnerStimuliCount();
                } else thiscount = parser.getInnerStimuliCount();
                for (int j = 0; j < thiscount; ++j) {
                    order[1] = j;
                    singleOrderList.add(order.clone());
                }
                m_innerCount += thiscount;
                if (m_random) {
                    Collections.shuffle(singleOrderList, rand);
                }
                orderList.addAll(singleOrderList);
            }
        } else {
            order = new int[m_spp];
            for (int i = 0; i < m_spp; ++i) order[i] = i;
            int h = 0;
            int choices = 0;
            while (true) {
                orderList.add(order.clone());
                ++choices;
                if (order[h] == m_files.length - m_spp + h) {
                    if (h == 0) break;
                    ++order[--h];
                    for (int i = h + 1; i < m_spp; ++i) order[i] = order[i - 1] + 1;
                } else {
                    h = m_spp - 1;
                    ++order[h];
                }
            }
            m_innerCount = choices;
            if (m_random) Collections.shuffle(orderList, rand);
        }
        if (m_sequences > 1) {
            m_innerCount *= m_sequences;
            List<int[]> holder = new LinkedList<int[]>(orderList);
            for (int i = 1; i < m_sequences; ++i) {
                List<int[]> temp = new LinkedList<int[]>();
                for (int[] v : orderList) temp.add(v.clone());
                Collections.shuffle(temp, rand);
                holder.addAll(temp);
            }
            orderList = holder;
        }
        m_order = orderList.toArray(new int[m_innerCount][order.length]);
        if (!m_multiStimuliParsers && m_random) {
            int temp;
            for (int[] v : m_order) {
                for (int i = 1; i < v.length; ++i) {
                    int swapi = rand.nextInt(i + 1);
                    if (swapi != i) {
                        temp = v[i];
                        v[i] = v[swapi];
                        v[swapi] = temp;
                    }
                }
            }
        }
        m_parsers = new StimuliParser[m_order[0].length];
        return true;
    }

    /**
     * Returns the number of stimuli that can be generated from the current file.
     */
    public synchronized int getInnerStimuliCount() {
        return m_innerCount;
    }

    /**
     * Decides which inner stimuli is the current.
     * Also resets the internal clock.
     */
    public synchronized void setInnerStimuli(int index) {
        int old = m_current;
        m_current = index;
        if (m_multiStimuliParsers) {
            if (old == -1 || m_order[m_current][0] != m_order[old][0]) {
                File file = m_files[m_order[m_current][0]];
                if (file.isDirectory()) {
                    if (!(m_parsers[0] instanceof FolderParser)) m_parsers[0] = new FolderParser(m_maxsize, m_spp, 1, m_controls, false);
                    m_parsers[0].setSource(file);
                } else {
                    if (m_parsers[0] == null || !m_parsers[0].setSource(file)) {
                        if (!(m_parsers[0] instanceof ImageParser)) {
                            m_parsers[0] = new ImageParser(m_maxsize_per_stimulus);
                            m_parsers[0].setSource(file);
                        }
                        if (m_parsers[0].getStatus() == STATUS_NO_SOURCE) {
                            m_parsers[0] = new JMFParser(m_maxsize_per_stimulus, m_controls);
                            m_parsers[0].setSource(file);
                        }
                    }
                    if (!(m_parsers[1] instanceof FolderParser)) m_parsers[1] = new FolderParser(m_maxsize_all_but_one_stimulus, m_spp - 1, 1, m_controls, false);
                    m_parsers[1].setSource(getTiedFolder(file));
                }
            }
            if (m_parsers[0] instanceof FolderParser) m_parsers[0].setInnerStimuli(m_order[m_current][1]); else m_parsers[1].setInnerStimuli(m_order[m_current][1]);
        } else {
            for (int i = 0; i < m_order[m_current].length; ++i) {
                File file = m_files[m_order[m_current][i]];
                if (m_parsers[i] == null || !m_parsers[i].setSource(file)) {
                    if (!(m_parsers[i] instanceof ImageParser)) {
                        m_parsers[i] = new ImageParser(m_maxsize_per_stimulus);
                        m_parsers[i].setSource(file);
                    }
                    if (m_parsers[i].getStatus() == STATUS_NO_SOURCE) {
                        m_parsers[i] = new JMFParser(m_maxsize_per_stimulus, m_controls);
                        m_parsers[i].setSource(file);
                    }
                }
            }
        }
        m_panel.removeAll();
        for (StimuliParser sp : m_parsers) {
            if (sp != null) {
                m_panel.add(sp.getPanel());
            }
        }
        m_panel.revalidate();
    }

    public synchronized void nextPage() {
        if (m_current == m_innerCount - 1) setInnerStimuli(0); else setInnerStimuli(m_current + 1);
    }

    public synchronized void previousPage() {
        if (m_current == 0) setInnerStimuli(m_innerCount - 1); else setInnerStimuli(m_current - 1);
    }

    public synchronized boolean isAtFirst() {
        return m_current == 0;
    }

    public synchronized boolean isAtLast() {
        return m_current == m_innerCount - 1;
    }

    public synchronized int getCurrentPage() {
        return m_current;
    }

    /**
     * Starts displaying the stimuli and starts the internal clock.
     */
    public synchronized void play() {
        for (StimuliParser sp : m_parsers) if (sp != null) sp.play();
    }

    /**
     * Stops displaying the stimuli and stops and resets the internal clock.
     */
    public synchronized void stop() {
        for (StimuliParser sp : m_parsers) if (sp != null) sp.stop();
    }

    /**
     * Stops displaying the stimuli if it was playing starts displaying the stimuli if it was not.
     */
    public synchronized void pause() {
        for (StimuliParser sp : m_parsers) if (sp != null) sp.pause();
    }

    /**
     * Returns the panel displaying the stimuli, this panel will remain the same as stimuli are changed.
     */
    public JPanel getPanel() {
        return m_panel;
    }

    /**
     * Returns the name of the current stimuli.
     */
    public synchronized String getName() {
        if (getStatus() == STATUS_NO_SOURCE) return "";
        StringBuffer sb = new StringBuffer(m_parsers[0].getName());
        for (int i = 1; i < m_parsers.length; ++i) {
            if (m_parsers[i] == null) break;
            sb.append(" / " + m_parsers[i].getName());
        }
        return sb.toString();
    }

    public String toString() {
        return "FolderParser: spp=" + m_spp + " source=" + getName();
    }

    /**
     * Returns the status of the stimuli.
     */
    public synchronized int getStatus() {
        if (m_parsers == null || m_parsers[0] == null) return STATUS_NO_SOURCE;
        return m_parsers[0].getStatus();
    }

    /**
     * Returns the value of the internal clock in milliseconds.
     */
    public synchronized long getPlaytime() {
        if (getStatus() == STATUS_NO_SOURCE) return 0;
        return m_parsers[0].getPlaytime();
    }

    private static File getTiedFolder(File f) {
        return new File(f.getParentFile(), f.getName().substring(0, f.getName().lastIndexOf('.')));
    }

    /**
     * Used by DragAndDropTest.
     * Quick hack that should be generalized or replaced.
     */
    public void nextMajorPage() {
        if (hasNextMajorPage()) {
            int idx = m_current;
            while (m_order[idx][0] == m_order[idx + 1][0]) ++idx;
            ++idx;
            setInnerStimuli(idx);
        }
    }

    public boolean hasNextMajorPage() {
        return m_multiStimuliParsers && (m_order[m_current][0] != m_order[m_innerCount - 1][0]);
    }

    public int getMajorPageStart() {
        if (!m_multiStimuliParsers || m_order[m_current][0] == m_order[0][0]) return 0;
        int idx = m_current;
        while (m_order[idx][0] == m_order[idx - 1][0]) --idx;
        return idx;
    }

    public int getMajorPageEnd() {
        if (!m_multiStimuliParsers) return getInnerStimuliCount() - 1;
        return (getMajorPageStart() + m_parsers[0].getInnerStimuliCount() - 1);
    }

    private static class ValidFileFilter implements FileFilter {

        public boolean accept(File f) {
            String name = f.getName();
            if (f.isHidden()) return false;
            if (f.isDirectory()) {
                String[] subnames = f.getParentFile().list();
                for (String s : subnames) {
                    if (s.startsWith(name) && s.lastIndexOf('.') == name.length()) return false;
                }
            }
            return true;
        }
    }

    public static void main(String args[]) {
        final FolderParser fp = new FolderParser(new Dimension(100, 100), 2, 0xf, 2);
        fp.setSource(new File(args[0]));
        javax.swing.JFrame frame = new javax.swing.JFrame();
        frame.add(fp.getPanel());
        System.out.println(fp.getInnerStimuliCount());
        fp.setInnerStimuli(0);
        System.out.println(fp.getName());
        frame.setVisible(true);
        frame.pack();
        frame.addKeyListener(new java.awt.event.KeyAdapter() {

            int idx = 0;

            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyChar() == 'q') fp.setInnerStimuli(--idx); else if (e.getKeyChar() == 'w') fp.setInnerStimuli(++idx);
                System.out.println(fp.getName());
            }
        });
    }
}
