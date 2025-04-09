package at.priv.hofer.itunes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLEditorKit;
import at.priv.hofer.tools.java.IProgressMonitor;
import at.priv.hofer.tools.java.StackTraceUtil;

public class GuiPanel extends JPanel {

    private File iTunesLibrary = null;

    private File musicDirectory = null;

    private JTextArea status = null;

    private JButton run = null;

    private Properties properties;

    public GuiPanel() {
        loadProperties();
        initUI();
        updateUIElements();
    }

    private void loadProperties() {
        properties = new Properties();
        try {
            properties.load(new FileInputStream("directories.properties"));
            if (properties.getProperty("iTunesLibrary") != null) {
                File f = new File(properties.getProperty("iTunesLibrary"));
                if (f.exists()) {
                    iTunesLibrary = f;
                }
            }
            if (properties.getProperty("musicDirectory") != null) {
                File f = new File(properties.getProperty("musicDirectory"));
                if (f.exists()) {
                    musicDirectory = f;
                }
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }

    private void storeProperties() {
        try {
            if (iTunesLibrary != null) {
                properties.setProperty("iTunesLibrary", iTunesLibrary.getAbsolutePath());
            }
            if (musicDirectory != null) {
                properties.setProperty("musicDirectory", musicDirectory.getAbsolutePath());
            }
            properties.store(new FileOutputStream("directories.properties"), "");
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }

    private void initUI() {
        this.setLayout(new GridBagLayout());
        this.setBorder(BorderFactory.createCompoundBorder(new BevelBorder(BevelBorder.RAISED), new TitledBorder("Select necessary Configuration")));
        this.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridheight = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        JTextArea description = new JTextArea();
        description.setEditable(false);
        description.setFocusable(false);
        description.setLineWrap(false);
        description.setText("Please select the iTunes directory and the directory, where you store your MP3-files");
        description.setFont(new JLabel().getFont().deriveFont(description.getFont().getStyle(), 14));
        description.setWrapStyleWord(true);
        this.add(description, gbc);
        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        this.add(new JLabel("Select your iTunes Library"), gbc);
        gbc.weightx = 0;
        gbc.gridx = 1;
        JButton chooseiTunesDir = new JButton("...");
        final JTextField iTunesDirectoryPath = new JTextField();
        iTunesDirectoryPath.setPreferredSize(new Dimension(300, iTunesDirectoryPath.getPreferredSize().height));
        if (iTunesLibrary != null) {
            iTunesDirectoryPath.setText(iTunesLibrary.getAbsolutePath());
        }
        chooseiTunesDir.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(new java.io.File("."));
                chooser.setDialogTitle("Select iTunes Library");
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.setMultiSelectionEnabled(false);
                chooser.setFileFilter(new FileFilter() {

                    @Override
                    public boolean accept(File f) {
                        if ("iTunes Library.itl".equalsIgnoreCase(f.getName())) {
                            return true;
                        }
                        if (f.isDirectory()) {
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public String getDescription() {
                        return "iTunes Library.itl";
                    }
                });
                if (iTunesLibrary != null) {
                    chooser.setCurrentDirectory(iTunesLibrary.getParentFile());
                }
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    iTunesLibrary = chooser.getSelectedFile();
                    iTunesDirectoryPath.setText(iTunesLibrary.getAbsolutePath());
                    iTunesDirectoryPath.setCaretPosition(0);
                } else {
                    iTunesLibrary = null;
                    iTunesDirectoryPath.setText("");
                }
                storeProperties();
                updateUIElements();
            }
        });
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        this.add(chooseiTunesDir, gbc);
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        iTunesDirectoryPath.setEditable(false);
        iTunesDirectoryPath.setFocusable(false);
        this.add(iTunesDirectoryPath, gbc);
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        this.add(new JLabel("Select your Music Directory"), gbc);
        gbc.weightx = 0;
        gbc.gridx = 1;
        JButton chooseMusicDir = new JButton("...");
        final JTextField musicDirectoryPath = new JTextField();
        musicDirectoryPath.setPreferredSize(new Dimension(300, musicDirectoryPath.getPreferredSize().height));
        if (musicDirectory != null) {
            musicDirectoryPath.setText(musicDirectory.getAbsolutePath());
        }
        chooseMusicDir.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(new java.io.File("."));
                chooser.setDialogTitle("Select your music directory");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (musicDirectory != null && musicDirectory.exists() && musicDirectory.isDirectory()) {
                    chooser.setCurrentDirectory(musicDirectory);
                }
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    musicDirectory = chooser.getSelectedFile();
                    musicDirectoryPath.setText(musicDirectory.getAbsolutePath());
                    musicDirectoryPath.setCaretPosition(0);
                } else {
                    musicDirectory = null;
                    musicDirectoryPath.setText("");
                }
                storeProperties();
                updateUIElements();
            }
        });
        gbc.anchor = GridBagConstraints.LINE_END;
        this.add(chooseMusicDir, gbc);
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 2;
        musicDirectoryPath.setEditable(false);
        musicDirectoryPath.setFocusable(false);
        this.add(musicDirectoryPath, gbc);
        run = new JButton("REPAIR");
        run.setEnabled(false);
        run.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        run.setEnabled(false);
                        Repair r = new Repair();
                        try {
                            r.repair(musicDirectory, iTunesLibrary, new IProgressMonitor() {

                                @Override
                                public void done() {
                                }

                                @Override
                                public int getProgress() {
                                    return 0;
                                }

                                @Override
                                public boolean isCanceled() {
                                    return false;
                                }

                                @Override
                                public void setCanceled(boolean c) {
                                }

                                @Override
                                public void setProgress(int p) {
                                }

                                @Override
                                public void setTaskName(final String name) {
                                    appendStatusText(name);
                                }

                                @Override
                                public void setTaskName(String name, int level) {
                                    setTaskName(name);
                                }

                                @Override
                                public void worked() {
                                }
                            });
                        } catch (Throwable t) {
                            appendStatusText("\n" + StackTraceUtil.getCustomStackTrace(t));
                            JOptionPane.showMessageDialog(GuiPanel.this, t.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                        run.setEnabled(true);
                    }
                }).start();
            }
        });
        gbc.weighty = 1;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(run, gbc);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        gbc.gridy++;
        this.add(new JLabel(), gbc);
        gbc.gridy++;
        gbc.weighty = 0;
        gbc.weightx = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.LINE_END;
        JPanel info = initCopyrightInfo();
        info.setBorder(BorderFactory.createTitledBorder("Info"));
        this.add(info, gbc);
        gbc.gridy++;
        status = new JTextArea();
        status.setEditable(false);
        status.setText("Welcome to iTunes Library Repair\nVisit http://sourceforge.net/projects/itunesrepair/\n\n\n");
        status.setBackground(Color.BLACK);
        status.setForeground(Color.GREEN);
        status.setBorder(BorderFactory.createTitledBorder("Status"));
        status.setLineWrap(true);
        status.setWrapStyleWord(true);
        final JPopupMenu statusContextMenu = new JPopupMenu("Clear");
        final JMenuItem statusContextMenuClear = new JMenuItem("Clear Status Panel");
        statusContextMenuClear.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        status.setText("");
                    }
                });
            }
        });
        final JMenuItem statusContextMenuCopy = new JMenuItem("Copy selected Text");
        statusContextMenuCopy.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                status.copy();
            }
        });
        statusContextMenu.add(statusContextMenuCopy);
        statusContextMenu.add(statusContextMenuClear);
        status.setComponentPopupMenu(statusContextMenu);
        JScrollPane statusScroll = new JScrollPane(status);
        statusScroll.setMinimumSize(statusScroll.getPreferredSize());
        gbc.weighty = 0;
        gbc.weightx = 1;
        this.add(statusScroll, gbc);
    }

    private void updateUIElements() {
        run.setEnabled(musicDirectory != null && iTunesLibrary != null);
    }

    private JPanel initCopyrightInfo() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        JTextPane area = new JTextPane();
        area.setBackground(Color.WHITE);
        area.setContentType("text/plain");
        area.setEditorKit(new HTMLEditorKit());
        area.setEditable(false);
        area.setFocusable(false);
        area.setText("<html><span style=\"font: " + area.getFont().getName() + "\"><span style=\"font-size: 9px\">©&nbsp;<a href=\"http://hofer.priv.at\">http://hofer.priv.at</a><br>©&nbsp;<a href=\"http://www.familie-hofer.net\">http://www.familie-hofer.net</a><br></span>" + "<span style=\"font-size: 8px; color: red\">no liability and no warranty for whatever. make backups of everything before you use this software.</span>" + "</span></html>");
        p.add(area, BorderLayout.PAGE_START);
        p.setMinimumSize(new Dimension(0, 0));
        return p;
    }

    private void appendStatusText(final String s) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                while (status.getLineCount() > 25) {
                    try {
                        status.setText(status.getText().substring(status.getLineStartOffset(1)));
                    } catch (BadLocationException e) {
                    }
                }
                status.append("\n" + s);
                status.setCaretPosition(status.getText().length());
            }
        });
    }
}
