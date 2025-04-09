import java.lang.System;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import edu.berkeley.guir.util.*;
import edu.berkeley.guir.gesture.*;

/** 
 * <P>
 * This software is distributed under the 
 * <A HREF="http://guir.cs.berkeley.edu/projects/COPYRIGHT.txt">
 * Berkeley Software License</A>.
 
*/
public class TriadFrame extends JFrame {

    boolean experimentMode = true;

    final String experimentSaveDir = "data" + File.pathSeparator + "experiment";

    final String version = "gdt 1.0";

    boolean autoSaveOn = true;

    final int SAVED = 2;

    final int AUTOSAVED = 1;

    final int NOT_SAVED = 0;

    int saveLevel = SAVED;

    TypedFile gestureSetFile = null;

    JLabel statusWindow;

    String lastDirectory = null;

    GestureSet gestureSet;

    TriadDisplay triadDisplay;

    Vector combinations;

    int[][] distances;

    boolean testMode = false;

    int currentCombinationIndex = -1;

    JMenuItem stopMenuItem;

    long testTime;

    Choice[] choices;

    public TriadFrame() {
        this("");
    }

    public TriadFrame(String name) {
        super(name);
        initFrame();
    }

    protected void initFrame() {
        JPanel contents = new JPanel(new BorderLayout());
        triadDisplay = new TriadDisplay();
        triadDisplay.addItemListener(new mySelector());
        contents.add(triadDisplay, BorderLayout.CENTER);
        contents.add(new JLabel("Which one is most different?"), BorderLayout.NORTH);
        statusWindow = new JLabel();
        contents.add(statusWindow, BorderLayout.SOUTH);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(contents, BorderLayout.CENTER);
        JMenuBar menuBar = new JMenuBar();
        JMenu menu;
        JMenuItem menuItem;
        ActionListener listener;
        menu = new JMenu("File");
        menuItem = new JMenuItem("Open");
        listener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String directory = (lastDirectory != null) ? lastDirectory : "data";
                JFileChooser fileChooser = new JFileChooser(directory);
                int returnVal = fileChooser.showOpenDialog(statusWindow.getTopLevelAncestor());
                switch(returnVal) {
                    case JFileChooser.APPROVE_OPTION:
                        openFile(fileChooser.getSelectedFile());
                        break;
                    case JFileChooser.CANCEL_OPTION:
                        message("Open cancelled", 10);
                        break;
                    default:
                        System.err.println("Bogosity from JFileChooser");
                        break;
                }
            }
        };
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        menuItem = new JMenuItem("Quit");
        listener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        };
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        menuBar.add(menu);
        menu = new JMenu("Test");
        menuItem = new JMenuItem("Start Test");
        listener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                startTest();
            }
        };
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        menuItem = new JMenuItem("Stop Test");
        listener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                stopTest();
            }
        };
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        menuItem.setEnabled(false);
        stopMenuItem = menuItem;
        menuBar.add(menu);
        menu = new JMenu("Debug");
        menuItem = new JMenuItem("Timings");
        listener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Gesture[] gestures = triadDisplay.getGestures();
                for (int i = 0; i < 3; i++) {
                    System.out.println("Gesture " + i + ":");
                    gestures[i].printTiming(System.out);
                }
            }
        };
        menuItem.addActionListener(listener);
        menu.add(menuItem);
        menuBar.add(menu);
        getRootPane().setJMenuBar(menuBar);
        HystericResizer hr = new HystericResizer();
        getRootPane().addComponentListener(hr);
    }

    File autoSaveFile(File f) {
        return new File(f.getParent(), "#" + f.getName() + "#");
    }

    public void openFile(File f) {
        lastDirectory = f.getParent();
        File asFile = autoSaveFile(f);
        boolean usingAutosave = false;
        try {
            if (asFile.exists() && (asFile.lastModified() > f.lastModified())) {
                int useAutosave = JOptionPane.showConfirmDialog(this, "Autosave file exists for '" + f.getName() + "'.  Use autosave file?", "gdt: Use Autosave File?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                switch(useAutosave) {
                    case JOptionPane.YES_OPTION:
                        f = asFile;
                        openActualFile(f);
                        break;
                    case JOptionPane.NO_OPTION:
                        message("Autosave disabled until file saved");
                        autoSaveOn = false;
                        openActualFile(f);
                        break;
                    case JOptionPane.CANCEL_OPTION:
                        return;
                    default:
                        message("WARNING: bogosity in TriadFrame.openFile");
                        return;
                }
            } else {
                openActualFile(f);
            }
        } catch (ClassNotFoundException e) {
            message("Cannot find required class while reading file '" + f + "': " + e);
        } catch (InvalidClassException e) {
            message("Problem with a class while reading file '" + f + "': " + e);
        } catch (StreamCorruptedException e) {
            message("The stream for file '" + f + "' is corrupted");
        } catch (ObjectStreamException e) {
            message("Expected an object but got primitive data while reading file '" + f + "': " + e);
        } catch (IOException e) {
            message("I/O error reading file '" + f + "': " + e);
        } catch (ParseException e) {
            message("Error parsing file '" + f + "': " + e);
        } catch (UnknownClassException e) {
            message("Error reading file '" + f + "': " + e);
        }
    }

    TypedFile openActualFile(File f) throws IOException, ParseException, UnknownClassException, ClassNotFoundException {
        final int OK = 0, NON_BINARY = 1, ERROR = 2;
        TypedFile openedFile = null;
        try {
            ObjectInputStream p = new ObjectInputStream(new FileInputStream(f));
            try {
                String v = (String) p.readObject();
                if (!version.equals(v)) {
                    message("Error while reading file '" + f + "': expected version '" + version + "' but got version '" + v + "'");
                }
                Object obj = p.readObject();
                if (obj instanceof GestureSet) {
                    setGestureSet((GestureSet) obj);
                } else if (obj instanceof GestureCategory) {
                    message("Cannot use GestureCategory files.  Need a GestureSet file..");
                    return null;
                } else {
                    throw new UnknownClassException("Unknown object type '" + obj.getClass().getName() + "'");
                }
            } finally {
                p.close();
            }
            stopTest();
            openedFile = new TypedFile(f, "binary");
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException("Cannot find required class while reading file '" + f + "': " + e);
        } catch (InvalidClassException e) {
            throw new InvalidClassException("Problem with a class while reading file '" + f + "': " + e);
        } catch (StreamCorruptedException e) {
        } catch (ObjectStreamException e) {
            throw new IOException("Expected an object but got primitive data while reading file '" + f + "': " + e);
        } catch (IOException e) {
            throw new IOException("I/O error reading file '" + f + "': " + e);
        }
        if (openedFile == null) {
            FileReader reader = new FileReader(f);
            try {
                setGestureSet(GestureSet.read(reader));
                openedFile = new TypedFile(f, "ASCII");
            } finally {
                reader.close();
            }
        }
        return openedFile;
    }

    private void setGestureSet(GestureSet gs) {
        gestureSet = gs;
        computeCombinations();
        Misc.shuffle(combinations);
        testDisplay();
    }

    transient Thread messageThread = null;

    public void message(String msg) {
        message(msg, 5 * 60 * 1000);
    }

    /**
   * Display message in status window.  Arrange for it to go away
   * after a while.  (Probably should be in its own class so it can
   * be used in other Frames.  Maybe later.)
   * Delay is in milliseconds.  If delay is 0, message never
   * automatically disappears.
   */
    public void message(String msg, int delay) {
        final int delaytime = delay;
        if (messageThread != null) {
            messageThread.interrupt();
        }
        statusWindow.setText(msg);
        statusWindow.paintImmediately(getBounds());
        if (delaytime > 0) {
            messageThread = new Thread() {

                public void run() {
                    try {
                        sleep(delaytime);
                    } catch (InterruptedException e) {
                    }
                    statusWindow.setText("");
                }
            };
            messageThread.setDaemon(true);
            messageThread.setName("message thread");
            messageThread.start();
        }
    }

    /** Show first example from first 3 classes, just as a test */
    void testDisplay() {
        Gesture[] gestures = new Gesture[3];
        int i = 0;
        for (Iterator iter = gestureSet.iterator(); iter.hasNext() && (i < 3); ) {
            GestureObject obj = (GestureObject) iter.next();
            if (obj instanceof GestureCategory) {
                GestureCategory gc = (GestureCategory) obj;
                gestures[i] = gc.gestureAt(0);
                i++;
            }
        }
        if (i < 3) {
            System.err.println("ERROR: can't find 3 gesture categories in the set");
            return;
        }
        triadDisplay.setGestures(gestures);
    }

    void computeCombinations() {
        combinations = new Vector();
        int numCategories = gestureSet.size();
        for (int i = 0; i < numCategories; i++) {
            for (int j = i + 1; j < numCategories; j++) {
                for (int k = j + 1; k < numCategories; k++) {
                    int[] triad = new int[3];
                    triad[0] = i;
                    triad[1] = j;
                    triad[2] = k;
                    combinations.addElement(triad);
                }
            }
        }
        System.err.println(combinations.size() + " combinations");
        choices = new Choice[combinations.size()];
    }

    /**
   * As of Swing 0.7, this doesn't work, but I'll leave it in anyway.  */
    class myFilenameFilter implements FilenameFilter {

        public boolean accept(File dir, String name) {
            return name.endsWith(".gs");
        }
    }

    void startTest() {
        testTime = System.currentTimeMillis();
        int numCategories = gestureSet.size();
        distances = new int[numCategories][numCategories];
        testMode = true;
        stopMenuItem.setEnabled(true);
        currentCombinationIndex = -1;
        nextTriad();
    }

    void nextTriad() {
        currentCombinationIndex++;
        int numCombinations = combinations.size();
        if (currentCombinationIndex < numCombinations) {
            message((currentCombinationIndex + 1) + " of " + numCombinations);
            int[] currentCombination = (int[]) combinations.elementAt(currentCombinationIndex);
            Gesture[] gestures = new Gesture[3];
            for (int i = 0; i < 3; i++) {
                GestureCategory gc = (GestureCategory) gestureSet.getChild(currentCombination[i]);
                gestures[i] = gc.gestureAt(0);
            }
            triadDisplay.setGestures(gestures, true);
        } else {
            stopTest();
        }
    }

    void stopTest() {
        long duration = (System.currentTimeMillis() - testTime) / 1000;
        System.out.println("test time: " + (duration / 60) + " minutes, " + (duration % 60) + " seconds");
        testMode = false;
        JOptionPane.showMessageDialog(this, new String("That's the end.  Thanks."), "Test finished", JOptionPane.INFORMATION_MESSAGE, null);
        saveResults();
        stopMenuItem.setEnabled(false);
    }

    class mySelector implements ItemListener {

        final long minDelay = 500;

        long lastItemTime = 0;

        public void itemStateChanged(ItemEvent e) {
            final TriadFrame triadFrame = TriadFrame.this;
            if (triadFrame.testMode) {
                long currentTime = System.currentTimeMillis();
                long deltaT = currentTime - lastItemTime;
                if (deltaT > minDelay) {
                    lastItemTime = currentTime;
                    int[] currentCombination = (int[]) combinations.elementAt(currentCombinationIndex);
                    currentCombination = (int[]) currentCombination.clone();
                    int selectionIndex = ((Integer) e.getItem()).intValue();
                    choices[currentCombinationIndex] = new Choice(deltaT, categoryAt(gestureSet, currentCombination[0]).getName(), categoryAt(gestureSet, currentCombination[1]).getName(), categoryAt(gestureSet, currentCombination[2]).getName(), selectionIndex);
                    if (selectionIndex != 0) {
                        int temp = currentCombination[selectionIndex];
                        currentCombination[selectionIndex] = currentCombination[0];
                        currentCombination[0] = temp;
                    }
                    triadFrame.distances[currentCombination[0]][currentCombination[1]]++;
                    triadFrame.distances[currentCombination[1]][currentCombination[0]]++;
                    triadFrame.distances[currentCombination[0]][currentCombination[2]]++;
                    triadFrame.distances[currentCombination[2]][currentCombination[0]]++;
                    triadFrame.nextTriad();
                }
            } else {
                triadFrame.message("Start test");
            }
        }
    }

    /** WARNING: this only works if gs only has GestureCategory children
      (not, for example, GestureGroups) */
    GestureCategory categoryAt(GestureSet gs, int i) {
        return (GestureCategory) gs.getChild(i);
    }

    TypedFile askForSaveFile() {
        String directory = (lastDirectory != null) ? lastDirectory : (experimentMode ? experimentSaveDir : "data");
        JFileChooser fileChooser = new JFileChooser(directory);
        JPanel panel = new JPanel();
        panel.add(new JLabel("File type"));
        JComboBox fileType = new JComboBox();
        fileType.addItem("ASCII");
        fileType.addItem("Binary");
        panel.add(fileType);
        panel.setMaximumSize(panel.getPreferredSize());
        fileChooser.setAccessory(panel);
        int returnVal = fileChooser.showSaveDialog(statusWindow.getTopLevelAncestor());
        switch(returnVal) {
            case JFileChooser.APPROVE_OPTION:
                return new TypedFile(fileChooser.getSelectedFile(), (String) fileType.getSelectedItem());
            case JFileChooser.CANCEL_OPTION:
                message("Save cancelled", 10);
                break;
            default:
                System.err.println("Bogosity from JFileChooser");
                break;
        }
        return null;
    }

    void saveResults() {
        File saveFile = askForSaveFile();
        if (saveFile != null) {
            try {
                System.err.println("Saving...");
                FileWriter writer = new FileWriter(saveFile.getPath(), true);
                int numCategories = gestureSet.size();
                DecimalFormat format = new DecimalFormat(" ########");
                for (int i = 0; i < numCategories; i++) {
                    for (int j = 0; j < numCategories; j++) {
                        if (j > 0) {
                            writer.write("\t");
                        }
                        writer.write(Integer.toString(distances[i][j]));
                    }
                    writer.write("\n");
                }
                writer.close();
                String logFileName = saveFile.getPath() + "-log";
                writer = new FileWriter(logFileName);
                for (int i = 0; (i < choices.length) && (choices[i] != null); i++) {
                    writer.write(choices[i].selection + "\t" + choices[i].time + "\t" + choices[i].gestureNames[0] + "\t" + choices[i].gestureNames[1] + "\t" + choices[i].gestureNames[2] + "\n");
                }
                writer.close();
                System.err.println("Saved");
            } catch (IOException e) {
                message("I/O error while writing file '" + saveFile.getPath() + "': " + e);
            }
        } else {
        }
    }

    /** store result of each choice */
    class Choice implements Externalizable {

        /** which of the 3 was selected (0, 1, or 2) */
        public int selection;

        /** How long this selection took to make */
        public long time;

        /** Names of the gestures in the triad */
        public String[] gestureNames;

        public Choice() {
        }

        public Choice(long time, String name1, String name2, String name3, int selection) {
            this.time = time;
            this.selection = selection;
            gestureNames = new String[3];
            gestureNames[0] = name1;
            gestureNames[1] = name2;
            gestureNames[2] = name3;
        }

        public Choice(long time, String[] names, int selection) {
            this(time, names[0], names[1], names[2], selection);
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(selection);
            out.writeChars(" ");
            out.writeLong(time);
            out.writeChars(" ");
            out.writeObject(gestureNames);
            out.writeChars("\n");
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        }
    }

    public class TypedFile extends File {

        String type;

        TypedFile(File f, String type) {
            super(f.getAbsolutePath());
            setType(type);
        }

        void setType(String t) {
            type = t.intern();
        }

        String getType() {
            return type;
        }
    }

    public class UnknownClassException extends Exception {

        public UnknownClassException() {
            super();
        }

        public UnknownClassException(String detail) {
            super(detail);
        }
    }
}
