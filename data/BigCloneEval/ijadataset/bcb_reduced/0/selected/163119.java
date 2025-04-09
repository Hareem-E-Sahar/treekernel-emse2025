package soccersim.gui;

import soccersim.base.DefendingSide;
import soccersim.base.GameHandler;
import soccersim.team.Team;
import soccersim.team.random.RandomTeam;
import soccersim.team.diagonal.DiagonalTeam;
import soccersim.team.brute.BruteTeam;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.jar.*;
import java.util.Enumeration;
import java.net.URLClassLoader;
import java.net.URL;
import java.lang.reflect.Constructor;

/**
 * Creates a GUI to play through the Soccer Simulation.
 * Date: Sep 17, 2007
 * Time: 11:31:48 PM
 * @author frank hadder, frank.hadder@gmail.com
 * @version 1.0
 */
public class SoccerSimGui implements Runnable {

    private JPanel mainPanel;

    private FieldPanel fieldPanel;

    private JPanel buttonPanel;

    private JButton startButton;

    private JButton pauseButton;

    private JButton stopButton;

    private JLabel westTeamScore;

    private JLabel eastTeamScore;

    private JComboBox speedComboBox;

    private JButton westCustomTeamButton;

    private JButton eastCustomTeamButton;

    private JPanel chooseTeamPanel;

    private JComboBox chooseEastTeamComboBox;

    private JComboBox chooseWestTeamComboBox;

    private final JFileChooser fileChooser = new JFileChooser();

    private Thread paintThread;

    Team eastTeam = null;

    Team westTeam = null;

    private GameHandler game;

    private int speed = 50;

    private boolean isPaused = false;

    private boolean shouldStop = false;

    public SoccerSimGui() {
        initializeComponents();
        addActionListeners();
        initializeThread();
        initializeComboBoxes();
    }

    private void initializeComponents() {
        GridLayout layout = new GridLayout();
        layout.setRows(1);
        layout.setColumns(1);
        layout.setHgap(-1);
        layout.setVgap(-1);
        mainPanel = new JPanel();
        mainPanel.setLayout(layout);
        mainPanel.setBounds(20, 20, 804, 540);
        mainPanel.setEnabled(true);
        fieldPanel = new FieldPanel();
        FlowLayout buttonPanelLayout = new FlowLayout(FlowLayout.CENTER);
        buttonPanelLayout.setHgap(10);
        buttonPanelLayout.setVgap(5);
        buttonPanel = new JPanel(buttonPanelLayout);
        westTeamScore = new JLabel("West Team: 0");
        buttonPanel.add(westTeamScore);
        startButton = new JButton("Start");
        buttonPanel.add(startButton);
        pauseButton = new JButton("Pause");
        buttonPanel.add(pauseButton);
        stopButton = new JButton("Stop");
        buttonPanel.add(stopButton);
        speedComboBox = new JComboBox();
        buttonPanel.add(speedComboBox);
        eastTeamScore = new JLabel("East Team: 0");
        buttonPanel.add(eastTeamScore);
        chooseTeamPanel = new JPanel(buttonPanelLayout);
        JLabel chooseWest = new JLabel("West Team: ");
        chooseTeamPanel.add(chooseWest);
        chooseWestTeamComboBox = new JComboBox();
        chooseTeamPanel.add(chooseWestTeamComboBox);
        westCustomTeamButton = new JButton("Custom...");
        chooseTeamPanel.add(westCustomTeamButton);
        JLabel chooseEast = new JLabel("East Team: ");
        chooseTeamPanel.add(chooseEast);
        chooseEastTeamComboBox = new JComboBox();
        chooseTeamPanel.add(chooseEastTeamComboBox);
        eastCustomTeamButton = new JButton("Custom...");
        chooseTeamPanel.add(eastCustomTeamButton);
        JSplitPane buttonSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buttonPanel, chooseTeamPanel);
        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, fieldPanel, buttonSplit);
        mainSplit.setDividerSize(0);
        mainSplit.setOrientation(0);
        mainSplit.setEnabled(false);
        mainPanel.add(mainSplit);
    }

    private void initializeThread() {
        if (eastTeam != null && westTeam != null) {
            game = new GameHandler(eastTeam, westTeam);
            fieldPanel.updateField(game.getField());
            fieldPanel.repaint();
            updateScoreLabels();
        }
        paintThread = new Thread(this);
    }

    private void initializeComboBoxes() {
        speedComboBox.addItem("Slower");
        speedComboBox.addItem("Slow");
        speedComboBox.addItem("Normal");
        speedComboBox.addItem("Fast");
        speedComboBox.addItem("Faster");
        speedComboBox.addItem("Instant");
        speedComboBox.setSelectedItem("Normal");
        String[] teams = { "Choose team...", "Custom Team...", "The Random Rangers", "The Brutal Brutes", "The Diagonal Demons" };
        chooseEastTeamComboBox.addItem(teams[0]);
        chooseEastTeamComboBox.addItem(teams[1]);
        chooseEastTeamComboBox.addItem(teams[2]);
        chooseEastTeamComboBox.addItem(teams[3]);
        chooseEastTeamComboBox.addItem(teams[4]);
        chooseEastTeamComboBox.setSelectedIndex(0);
        chooseWestTeamComboBox.addItem(teams[0]);
        chooseWestTeamComboBox.addItem(teams[1]);
        chooseWestTeamComboBox.addItem(teams[2]);
        chooseWestTeamComboBox.addItem(teams[3]);
        chooseWestTeamComboBox.addItem(teams[4]);
        chooseWestTeamComboBox.setSelectedIndex(0);
    }

    private void addActionListeners() {
        startButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!paintThread.isAlive() && eastTeam != null && westTeam != null) {
                    shouldStop = false;
                    initializeThread();
                    paintThread.start();
                }
            }
        });
        pauseButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                isPaused = !isPaused;
                if (isPaused) {
                    pauseButton.setText("Resume");
                } else {
                    pauseButton.setText("Pause");
                }
            }
        });
        stopButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                shouldStop = true;
            }
        });
        speedComboBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String selected = (String) speedComboBox.getSelectedItem();
                if (selected.equals("Normal")) {
                    speed = 75;
                } else if (selected.equals("Slower")) {
                    speed = 275;
                } else if (selected.equals("Slow")) {
                    speed = 125;
                } else if (selected.equals("Fast")) {
                    speed = 50;
                } else if (selected.equals("Faster")) {
                    speed = 25;
                } else if (selected.equals("Instant")) {
                    speed = 1;
                }
            }
        });
        chooseEastTeamComboBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                loadTeamDropDown(chooseEastTeamComboBox.getSelectedIndex(), DefendingSide.East);
            }
        });
        chooseWestTeamComboBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                loadTeamDropDown(chooseWestTeamComboBox.getSelectedIndex(), DefendingSide.West);
            }
        });
        westCustomTeamButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                fileChooser.setFileFilter(new JarFileFilter());
                int returnVal = fileChooser.showOpenDialog(SoccerSimGui.this.mainPanel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    loadTeam(file, DefendingSide.West);
                }
            }
        });
        eastCustomTeamButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                fileChooser.setFileFilter(new JarFileFilter());
                int returnVal = fileChooser.showOpenDialog(SoccerSimGui.this.mainPanel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    loadTeam(file, DefendingSide.East);
                }
            }
        });
    }

    private void loadTeamDropDown(int selectedIndex, DefendingSide side) {
        Team team = null;
        if (selectedIndex != 1) {
            if (side == DefendingSide.East) {
                eastCustomTeamButton.setEnabled(false);
            } else {
                westCustomTeamButton.setEnabled(false);
            }
        }
        switch(selectedIndex) {
            case 0:
                break;
            case 1:
                if (side == DefendingSide.East) {
                    eastCustomTeamButton.setEnabled(true);
                } else {
                    westCustomTeamButton.setEnabled(true);
                }
                break;
            case 2:
                team = new RandomTeam(side);
                break;
            case 3:
                team = new BruteTeam(side);
                break;
            case 4:
                team = new DiagonalTeam(side);
                break;
            default:
                break;
        }
        if (side == DefendingSide.East) {
            eastTeam = team;
        } else {
            westTeam = team;
        }
        if (eastTeam != null && westTeam != null) {
            initializeThread();
        }
    }

    private void loadTeam(File jarFile, DefendingSide side) {
        if (!jarFile.getName().endsWith(".jar")) {
        }
        try {
            URL[] urlList = { jarFile.toURL() };
            System.out.println("classloader try");
            ClassLoader loader = new URLClassLoader(urlList);
            JarFile jf = new JarFile(jarFile);
            Team team = null;
            for (Enumeration e = jf.entries(); e.hasMoreElements(); ) {
                String jarEntry = e.nextElement().toString();
                if (jarEntry.endsWith(".class")) {
                    jarEntry = jarEntry.replace('/', '.');
                    jarEntry = jarEntry.substring(0, jarEntry.length() - 6);
                    Class c = loader.loadClass(jarEntry);
                    if (c.getSuperclass().equals(Team.class)) {
                        System.out.println("Found a team! (" + c.getCanonicalName() + ")");
                        Class[] types = new Class[] { DefendingSide.class };
                        Constructor cons = c.getConstructor(types);
                        Object[] args = new Object[] { side };
                        team = (Team) cons.newInstance(args);
                    }
                }
            }
            if (side == DefendingSide.East) {
                eastTeam = team;
            } else {
                westTeam = team;
            }
            if (eastTeam != null && westTeam != null) {
                initializeThread();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (game.gameInProgress() && !shouldStop) {
            while (isPaused) {
                try {
                    paintThread.wait();
                } catch (Exception ex) {
                }
            }
            game.executeStep();
            updateScoreLabels();
            fieldPanel.updateField(game.getField());
            fieldPanel.repaint();
            try {
                java.lang.Thread.sleep(this.speed);
            } catch (Exception ex) {
            }
        }
    }

    private void updateScoreLabels() {
        westTeamScore.setText(westTeam.getName() + ": " + game.getScore().getScore(DefendingSide.West));
        eastTeamScore.setText(eastTeam.getName() + ": " + game.getScore().getScore(DefendingSide.East));
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("Java Soccer Simulator");
        frame.setContentPane(new SoccerSimGui().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public class JarFileFilter extends FileFilter {

        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            String extension = getExtension(file);
            return extension != null && extension.equals("jar");
        }

        public String getDescription() {
            return "Java Jar";
        }

        private String getExtension(File file) {
            String ext = null;
            String s = file.getName();
            int i = s.lastIndexOf('.');
            if (i > 0 && i < s.length() - 1) {
                ext = s.substring(i + 1).toLowerCase();
            }
            return ext;
        }
    }
}
