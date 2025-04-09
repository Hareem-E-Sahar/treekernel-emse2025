package net.sf.openrock.ui.java2d;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Desktop.Action;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.sf.openrock.Main;
import net.sf.openrock.game.Game;

class SwingGui {

    private final Frame parent;

    private JDialog gameDialog;

    private JLabel messageLabel;

    private JSpinner endsField;

    private JTextField team1Field;

    private JTextField team2Field;

    private JTextField hostField;

    private JSpinner portField;

    private JRadioButton hotSeatOption;

    private JRadioButton netServerOption;

    private JRadioButton netClientOption;

    private JDialog networkDialog;

    private JLabel networkLabel;

    private JDialog aboutDialog;

    private Game game;

    private JButton startButton;

    public SwingGui(Frame parent) {
        this.parent = parent;
        createGameDialog();
        createNetworkDialog();
        createAboutDialog();
    }

    public void setGame(Game game) {
        this.game = game;
    }

    private void createGameDialog() {
        gameDialog = new JDialog(parent, "New Game", true);
        gameDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        gameDialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        JLabel logoLabel = new JLabel(new ImageIcon(getClass().getResource("/openrock.png")));
        messageLabel = new JLabel("Some text");
        messageLabel.setFont(messageLabel.getFont().deriveFont(messageLabel.getFont().getSize2D() * 1.5f));
        JSeparator separator1 = new JSeparator();
        JSeparator separator2 = new JSeparator();
        JLabel typeLabel = new JLabel("Game type:");
        JLabel endsLabel = new JLabel("Ends:");
        JLabel team1Label = new JLabel("Team 1:");
        JLabel team2Label = new JLabel("Team 2:");
        JLabel hostLabel = new JLabel("Network host:");
        JLabel portLabel = new JLabel("Network port:");
        hotSeatOption = new JRadioButton("Hot seat", true);
        hotSeatOption.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                hotSeatSelected();
            }
        });
        netServerOption = new JRadioButton("Network server", false);
        netServerOption.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                netServerSelected();
            }
        });
        netClientOption = new JRadioButton("Network client", false);
        netClientOption.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                netClientSelected();
            }
        });
        ButtonGroup typeGroup = new ButtonGroup();
        typeGroup.add(hotSeatOption);
        typeGroup.add(netServerOption);
        typeGroup.add(netClientOption);
        DocumentListener docListener = new DocListener();
        endsField = new JSpinner(new SpinnerNumberModel(10, 1, 10, 1));
        team1Field = new JTextField("Team 1", 10);
        team1Field.getDocument().addDocumentListener(docListener);
        team2Field = new JTextField("Team 2", 10);
        team2Field.getDocument().addDocumentListener(docListener);
        hostField = new JTextField("", 10);
        hostField.getDocument().addDocumentListener(docListener);
        portField = new JSpinner(new SpinnerNumberModel(25000, 1024, 65535, 1));
        portField.setEditor(new JSpinner.NumberEditor(portField, "#####"));
        JButton quitButton = new JButton("Quit");
        quitButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        JButton aboutButton = new JButton("About");
        aboutButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                aboutDialog.setLocationRelativeTo(parent);
                aboutDialog.setVisible(true);
            }
        });
        startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                gameDialog.setVisible(false);
                startGamePressed();
            }
        });
        GroupLayout layout = new GroupLayout(gameDialog.getContentPane());
        gameDialog.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        layout.linkSize(quitButton, aboutButton, startButton);
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.TRAILING).addComponent(messageLabel, Alignment.CENTER).addComponent(separator1).addGroup(layout.createSequentialGroup().addComponent(logoLabel).addGroup(layout.createParallelGroup(Alignment.TRAILING).addComponent(typeLabel).addComponent(endsLabel).addComponent(team1Label).addComponent(team2Label).addComponent(hostLabel).addComponent(portLabel)).addGroup(layout.createParallelGroup().addComponent(hotSeatOption).addComponent(netServerOption).addComponent(netClientOption).addComponent(endsField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(team1Field, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(team2Field, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(hostField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(portField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))).addComponent(separator2).addGroup(layout.createSequentialGroup().addComponent(quitButton).addComponent(aboutButton).addGap(0, 0, Integer.MAX_VALUE).addComponent(startButton)));
        layout.setVerticalGroup(layout.createSequentialGroup().addComponent(messageLabel).addComponent(separator1).addGroup(layout.createParallelGroup().addComponent(logoLabel).addGroup(layout.createSequentialGroup().addGroup(layout.createBaselineGroup(false, false).addComponent(typeLabel).addComponent(hotSeatOption)).addComponent(netServerOption).addComponent(netClientOption).addGroup(layout.createBaselineGroup(false, false).addComponent(endsLabel).addComponent(endsField)).addGroup(layout.createBaselineGroup(false, false).addComponent(team1Label).addComponent(team1Field)).addGroup(layout.createBaselineGroup(false, false).addComponent(team2Label).addComponent(team2Field)).addGroup(layout.createBaselineGroup(false, false).addComponent(hostLabel).addComponent(hostField)).addGroup(layout.createBaselineGroup(false, false).addComponent(portLabel).addComponent(portField)))).addComponent(separator2).addGroup(layout.createBaselineGroup(false, false).addComponent(quitButton).addComponent(aboutButton).addComponent(startButton)));
        gameDialog.pack();
        gameDialog.setResizable(false);
        hotSeatSelected();
    }

    protected void netClientSelected() {
        team1Field.setEnabled(false);
        team2Field.setEnabled(true);
        hostField.setEnabled(true);
        portField.setEnabled(true);
        validateFields();
    }

    protected void netServerSelected() {
        team1Field.setEnabled(true);
        team2Field.setEnabled(false);
        hostField.setEnabled(false);
        portField.setEnabled(true);
        validateFields();
    }

    protected void hotSeatSelected() {
        team1Field.setEnabled(true);
        team2Field.setEnabled(true);
        hostField.setEnabled(false);
        portField.setEnabled(false);
        validateFields();
    }

    private static final Color BAD_FIELD_COLOR = new Color(255, 128, 128);

    protected void validateFields() {
        boolean ok = true;
        if ((hotSeatOption.isSelected() || netServerOption.isSelected()) && team1Field.getText().isEmpty()) {
            team1Field.setBackground(BAD_FIELD_COLOR);
            ok = false;
        } else {
            team1Field.setBackground(Color.WHITE);
        }
        if ((hotSeatOption.isSelected() || netClientOption.isSelected()) && team2Field.getText().isEmpty()) {
            team2Field.setBackground(BAD_FIELD_COLOR);
            ok = false;
        } else {
            team2Field.setBackground(Color.WHITE);
        }
        if (netClientOption.isSelected() && hostField.getText().isEmpty()) {
            hostField.setBackground(BAD_FIELD_COLOR);
            ok = false;
        } else {
            hostField.setBackground(Color.WHITE);
        }
        startButton.setEnabled(ok);
    }

    private void createNetworkDialog() {
        networkDialog = new JDialog(parent, "Network", true);
        networkDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        networkDialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        networkLabel = new JLabel();
        JButton abortButton = new JButton("Abort");
        abortButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                networkDialog.setVisible(false);
                game.abortNetwork();
            }
        });
        GroupLayout layout = new GroupLayout(networkDialog.getContentPane());
        networkDialog.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER).addComponent(networkLabel).addComponent(abortButton));
        layout.setVerticalGroup(layout.createSequentialGroup().addComponent(networkLabel).addComponent(abortButton));
        networkDialog.pack();
        networkDialog.setResizable(false);
    }

    private void createAboutDialog() {
        aboutDialog = new JDialog(parent, "About " + Main.APP_NAME, true);
        aboutDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        JLabel versionLabel = new JLabel(Main.APP_NAME + " " + Main.APP_VERSION);
        versionLabel.setFont(versionLabel.getFont().deriveFont(versionLabel.getFont().getSize2D() * 1.5f));
        JLabel copyrightLabel = new JLabel(Main.COPYRIGHT);
        JTextArea licenseArea = new JTextArea(loadLicenseText());
        licenseArea.setEditable(false);
        licenseArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        JScrollPane licenseScroll = new JScrollPane(licenseArea);
        licenseScroll.setPreferredSize(new Dimension(licenseScroll.getPreferredSize().width, 200));
        JLabel homepageLabel = new JLabel(Main.HOMEPAGE);
        JButton homepageButton = new JButton(Main.HOMEPAGE);
        homepageButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(Main.HOMEPAGE));
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }
        });
        JComponent homepageComponent;
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.BROWSE)) {
            homepageComponent = homepageButton;
        } else {
            homepageComponent = homepageLabel;
        }
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                aboutDialog.setVisible(false);
            }
        });
        GroupLayout layout = new GroupLayout(aboutDialog.getContentPane());
        aboutDialog.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER).addComponent(versionLabel).addComponent(copyrightLabel).addComponent(licenseScroll).addGroup(layout.createSequentialGroup().addComponent(homepageComponent).addGap(0, 0, Integer.MAX_VALUE).addComponent(closeButton)));
        layout.setVerticalGroup(layout.createSequentialGroup().addComponent(versionLabel).addComponent(copyrightLabel).addComponent(licenseScroll, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE).addGroup(layout.createBaselineGroup(false, false).addComponent(homepageComponent).addComponent(closeButton)));
        aboutDialog.pack();
        aboutDialog.setResizable(false);
    }

    private String loadLicenseText() {
        String text = null;
        try {
            InputStream in = getClass().getResourceAsStream("/COPYING.txt");
            if (in != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder sb = new StringBuilder();
                char[] buf = new char[1024];
                int n;
                while ((n = reader.read(buf)) != -1) {
                    sb.append(buf, 0, n);
                }
                reader.close();
                text = sb.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (text == null) {
            text = "OpenRock is free software: you can redistribute it and/or modify\n" + "it under the terms of the GNU General Public License as published by\n" + "the Free Software Foundation, either version 3 of the License, or\n" + "(at your option) any later version.\n" + "\n" + "OpenRock is distributed in the hope that it will be useful,\n" + "but WITHOUT ANY WARRANTY; without even the implied warranty of\n" + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" + "GNU General Public License for more details.\n" + "\n" + "You should have received a copy of the GNU General Public License\n" + "along with OpenRock.  If not, see <http://www.gnu.org/licenses/>.\n";
        }
        return text;
    }

    public void showGameDialog(final String msg) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                messageLabel.setText(msg);
                gameDialog.setLocationRelativeTo(parent);
                gameDialog.setVisible(true);
            }
        });
    }

    public void showNetworkDialog(final String string) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                networkLabel.setText(string);
                networkDialog.pack();
                networkDialog.setLocationRelativeTo(parent);
                networkDialog.setVisible(true);
            }
        });
    }

    public void hideNetworkDialog() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                networkDialog.setVisible(false);
            }
        });
    }

    private void startGamePressed() {
        String team1 = team1Field.getText();
        String team2 = team2Field.getText();
        String host = hostField.getText();
        int ends = (int) (Integer) endsField.getValue();
        int port = (int) (Integer) portField.getValue();
        if (hotSeatOption.isSelected()) {
            game.newHotSeatMatch(team1, team2, ends);
        } else if (netServerOption.isSelected()) {
            game.newNetServerMatch(team1, ends, port);
        } else if (netClientOption.isSelected()) {
            game.newNetClientMatch(team2, ends, host, port);
        } else {
            System.err.println("No game type selected");
        }
    }

    private class DocListener implements DocumentListener {

        @Override
        public void changedUpdate(DocumentEvent e) {
            validateFields();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            validateFields();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            validateFields();
        }
    }
}
