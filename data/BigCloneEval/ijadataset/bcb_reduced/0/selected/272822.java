package org.threadswarm.reversi;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.apache.commons.cli.*;
import org.jdesktop.core.animation.timing.Animator;
import org.threadswarm.reversi.bot.LocalReversiBot;
import org.threadswarm.reversi.bot.ReversiBot;
import org.threadswarm.reversi.gui.AboutActionListener;
import org.threadswarm.reversi.gui.CellClickActionListener;
import org.threadswarm.reversi.gui.CellComponent;
import org.threadswarm.reversi.gui.CheckVersionActionListener;
import org.threadswarm.reversi.gui.MessageGlassPane;
import org.threadswarm.reversi.gui.NewGameActionListener;
import org.threadswarm.reversi.gui.PreferencesActionListener;
import org.threadswarm.reversi.gui.animation.AlphaSettingTimingTarget;
import org.threadswarm.reversi.gui.animation.AnimationType;
import org.threadswarm.reversi.gui.animation.AnimatorRepository;

/**
 *
 * @author steve
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("a", "advanced", false, "enable advanced features");
        options.addOption("help", false, "display usage information");
        boolean advancedModeTmp = false;
        CommandLineParser cmdParser = new PosixParser();
        HelpFormatter helpFormatter = new HelpFormatter();
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            if (cmd.hasOption("a")) advancedModeTmp = true;
            if (cmd.hasOption("help")) {
                helpFormatter.printHelp("Reversi_Contender", options);
                return;
            }
        } catch (ParseException ex) {
            System.out.println("issue reading command-line arguments\r\n");
            helpFormatter.printHelp("Reversi_Contender", options);
            return;
        }
        final boolean advancedMode = advancedModeTmp;
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                GameSession.getInstance().setAdvancedMode(advancedMode);
                final JFrame frame = new JFrame("Reversi Contender");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                getGameContentPanel(frame);
                JMenuBar menuBar = new JMenuBar();
                JMenu gameMenu = new JMenu("Game");
                JMenuItem newGameMenuItem = new JMenuItem("New Game");
                newGameMenuItem.addActionListener(new NewGameActionListener(frame));
                gameMenu.add(newGameMenuItem);
                JMenuItem exitGameMenuItem = new JMenuItem("Exit");
                exitGameMenuItem.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        System.exit(0);
                    }
                });
                gameMenu.add(exitGameMenuItem);
                menuBar.add(gameMenu);
                JMenu editMenu = new JMenu("Edit");
                JMenuItem prefsItemMenu = new JMenuItem("Preferences");
                prefsItemMenu.addActionListener(new PreferencesActionListener(frame));
                editMenu.add(prefsItemMenu);
                menuBar.add(editMenu);
                JMenu helpMenu = new JMenu("Help");
                JMenuItem homePageMenuItem = new JMenuItem("Visit Homepage");
                homePageMenuItem.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        Desktop desktop = Desktop.getDesktop();
                        try {
                            desktop.browse(new URI("http://code.google.com/p/reversi-contender"));
                        } catch (URISyntaxException ex) {
                            throw new RuntimeException();
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(frame, "We can't seem to open your browser, sorry!", "Browser Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                if (!Desktop.isDesktopSupported()) {
                    homePageMenuItem.setEnabled(false);
                }
                helpMenu.add(homePageMenuItem);
                JMenuItem versionCheckItem = new JMenuItem("Check for new version");
                versionCheckItem.addActionListener(new CheckVersionActionListener(frame));
                helpMenu.add(versionCheckItem);
                JMenuItem aboutMenuItem = new JMenuItem("About");
                aboutMenuItem.addActionListener(new AboutActionListener(frame));
                helpMenu.add(aboutMenuItem);
                menuBar.add(helpMenu);
                frame.setJMenuBar(menuBar);
                frame.pack();
                frame.setResizable(false);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    public static void getGameContentPanel(final JFrame frame) {
        Runnable edtTask = new Runnable() {

            @Override
            public void run() {
                final GameSession gameSession = GameSession.getInstance();
                final JPanel contentPanel = (JPanel) frame.getContentPane();
                contentPanel.removeAll();
                BoxLayout layout = new BoxLayout(contentPanel, BoxLayout.Y_AXIS);
                contentPanel.setLayout(layout);
                final MessageGlassPane glassPane = new MessageGlassPane();
                glassPane.setMaximumSize(new Dimension(440, 440));
                frame.setGlassPane(glassPane);
                final ReversiGame game = new ReversiGameImp();
                Status botStatus = (gameSession.getPlayerStatus() == Status.BLACK) ? Status.WHITE : Status.BLACK;
                ReversiBot bot = new LocalReversiBot(botStatus, game, glassPane);
                gameSession.setReversiBot(bot);
                game.addPlayer((Player) bot, botStatus);
                JPanel gridPanel = new JPanel();
                gridPanel.setLayout(new GridLayout(8, 8, 0, 0));
                gridPanel.setMaximumSize(new Dimension(384, 384));
                final Map<Point, CellComponent> panelMap = new HashMap<Point, CellComponent>();
                final List<CellComponent> panelList = new ArrayList<CellComponent>(64);
                for (int x = 0; x < 8; x++) {
                    for (int y = 0; y < 8; y++) {
                        CellComponent cell = new CellComponent(new Point(x, y), game);
                        panelList.add(cell);
                        cell.addMouseListener(new CellClickActionListener(cell, panelList));
                        game.addBoardChangeListener(cell.getCellCoordinates(), cell);
                        gridPanel.add(cell);
                        panelMap.put(cell.getCellCoordinates(), cell);
                    }
                }
                contentPanel.add(gridPanel);
                contentPanel.updateUI();
                game.addPlayer(new Player() {

                    @Override
                    public void turnAcquired() {
                        List<Point> availableInsertions = game.getAvailableInsertions();
                        if (availableInsertions.isEmpty()) {
                            if (game.isPreviousPassed()) {
                                Map<Status, Integer> countMap = game.getCountMap();
                                Integer cellsWhite = countMap.get(Status.WHITE);
                                Integer cellsBlack = countMap.get(Status.BLACK);
                                final Status winner;
                                if (cellsWhite == null) {
                                    winner = Status.BLACK;
                                } else if (cellsBlack == null) {
                                    winner = Status.WHITE;
                                } else {
                                    winner = (cellsWhite > cellsBlack) ? Status.WHITE : Status.BLACK;
                                }
                                SwingUtilities.invokeLater(new Runnable() {

                                    public void run() {
                                        JOptionPane.showMessageDialog(frame, winner + " won the game!");
                                        Main.getGameContentPanel(frame);
                                    }
                                });
                            } else {
                                game.pass();
                            }
                        } else {
                            final List<CellComponent> cells = new LinkedList<CellComponent>();
                            Set<Point> keySet = panelMap.keySet();
                            for (Point key : keySet) {
                                if (availableInsertions.contains(key)) cells.add(panelMap.get(key));
                            }
                            List<CellComponent> sessionList = gameSession.getAvailableInsertionsList();
                            sessionList.clear();
                            sessionList.addAll(cells);
                            SwingUtilities.invokeLater(new Runnable() {

                                public void run() {
                                    if (gameSession.isMarkAvailable() && gameSession.isAvailInsertionAnimationEnabled()) {
                                        for (CellComponent cell : cells) {
                                            cell.setInsertable(true, true);
                                        }
                                        Animator availInsertionAnimator = AnimatorRepository.getInstance().getAnimator(AnimationType.AVAIL_INSERTION);
                                        availInsertionAnimator.addTarget(new AlphaSettingTimingTarget(cells));
                                        if (!availInsertionAnimator.isRunning()) availInsertionAnimator.start();
                                    } else {
                                        for (CellComponent cell : cells) {
                                            cell.setInsertable(true);
                                        }
                                    }
                                }
                            });
                        }
                    }
                }, gameSession.getPlayerStatus());
                Thread botThread = new Thread(bot);
                botThread.start();
                game.start();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            edtTask.run();
        } else {
            SwingUtilities.invokeLater(edtTask);
        }
    }
}
