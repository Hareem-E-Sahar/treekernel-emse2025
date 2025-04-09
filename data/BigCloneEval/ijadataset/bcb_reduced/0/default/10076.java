import org.ejen.ext.Version;
import java.io.FileWriter;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Install {

    private static final String ejenVersion = "Ejen-" + Version.toString(null);

    ;

    public Install() {
        MainFrame frame = new MainFrame();
        frame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        new Install();
    }

    public class MainFrame extends JFrame {

        JPanel contentPanel;

        JTextField jdkTextField;

        String javaRuntimeName;

        String javaVersion;

        String osName;

        String osVersion;

        String osArch;

        String javaHome;

        String strippedJavaHome;

        String fileSeparator;

        public MainFrame() {
            enableEvents(AWTEvent.WINDOW_EVENT_MASK);
            try {
                getSystemProperties();
                jInit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void jInit() throws Exception {
            this.setTitle(ejenVersion + " Installation");
            contentPanel = (JPanel) (this.getContentPane());
            contentPanel.setLayout(new BorderLayout());
            contentPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            JTextArea welcome = new JTextArea(javaRuntimeName + ", version " + javaVersion + ",\n" + osName + " " + osVersion + " (" + osArch + ").\n\n" + "Based on the file separator (" + fileSeparator + "), this is a " + (fileSeparator.equals("/") ? "Unix" : "Windows") + " system.\n" + "Your JDK installation directory is:");
            welcome.setEditable(false);
            welcome.setBackground(contentPanel.getBackground());
            welcome.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            ActionListener actionListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    String actionCommand = e.getActionCommand();
                    if (actionCommand.equals("fileChooserButton")) {
                        JFileChooser chooser = new JFileChooser();
                        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                        chooser.setDialogTitle("Choose your JDK installation directory");
                        if (chooser.showOpenDialog(contentPanel) == JFileChooser.APPROVE_OPTION) jdkTextField.setText(chooser.getSelectedFile().getAbsolutePath());
                    } else if (actionCommand.equals("okButton")) {
                        install();
                    } else if (actionCommand.equals("cancelButton")) {
                        System.exit(0);
                    }
                }
            };
            JPanel jdkPanel = new JPanel(new BorderLayout());
            jdkTextField = new JTextField(strippedJavaHome);
            JButton fileChooserButton = new JButton("...");
            fileChooserButton.setActionCommand("fileChooserButton");
            fileChooserButton.addActionListener(actionListener);
            jdkPanel.add(jdkTextField, BorderLayout.CENTER);
            jdkPanel.add(fileChooserButton, BorderLayout.EAST);
            JPanel actionPanel = new JPanel(new BorderLayout());
            JTextArea label = new JTextArea("Check those values before installing.");
            label.setEditable(false);
            label.setBackground(contentPanel.getBackground());
            label.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            JButton okButton = new JButton("Install");
            okButton.setActionCommand("okButton");
            okButton.addActionListener(actionListener);
            JButton cancelButton = new JButton("Cancel");
            cancelButton.setActionCommand("cancelButton");
            cancelButton.addActionListener(actionListener);
            actionPanel.add(label, BorderLayout.NORTH);
            actionPanel.add(okButton, BorderLayout.CENTER);
            actionPanel.add(cancelButton, BorderLayout.EAST);
            contentPanel.add(welcome, BorderLayout.NORTH);
            contentPanel.add(jdkPanel, BorderLayout.CENTER);
            contentPanel.add(actionPanel, BorderLayout.SOUTH);
        }

        protected void processWindowEvent(WindowEvent e) {
            super.processWindowEvent(e);
            if (e.getID() == WindowEvent.WINDOW_CLOSING) {
                System.exit(0);
            }
        }

        private void getSystemProperties() {
            try {
                javaRuntimeName = System.getProperty("java.runtime.name");
                javaVersion = System.getProperty("java.version");
                osName = System.getProperty("os.name");
                osVersion = System.getProperty("os.version");
                osArch = System.getProperty("os.arch");
                javaHome = System.getProperty("java.home");
                fileSeparator = System.getProperty("file.separator");
                if (fileSeparator == null || fileSeparator.length() == 0) showError("Empty file separator !");
                if (!fileSeparator.equals("/") && !fileSeparator.equals("\\")) showError("Unknown file separator [" + fileSeparator + "] !");
                if (javaHome == null) showError("Could not get java home directory !");
                int i = javaHome.lastIndexOf(fileSeparator);
                if (i == -1) showError("Invalid java home directory: " + javaHome);
                strippedJavaHome = javaHome.substring(0, i);
            } catch (Exception e) {
                showError(e.toString());
            }
        }

        private void showError(String msg) {
            JOptionPane.showMessageDialog(this, msg + "\n\nPlease send a bug report to <ejen@noos.fr>," + "\nSorry.", "Fatal error", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }

        private void showWarning(String msg) {
            JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
        }

        private void showInformation(String msg) {
            JOptionPane.showMessageDialog(this, msg);
        }

        private void install() {
            String fileName;
            String fileContent;
            if (fileSeparator.equals("/")) {
                fileName = "java-env";
                fileContent = "#!/bin/sh\nexport JAVA_HOME=\"" + strippedJavaHome + "\"";
            } else {
                fileName = "java-env.bat";
                fileContent = "set JAVA_HOME=\"" + strippedJavaHome + "\"";
            }
            FileWriter fw = null;
            try {
                fw = new FileWriter(fileName);
                fw.write(fileContent);
            } catch (Exception e) {
                showError("Could not create \"" + fileName + "\": " + e.toString());
            } finally {
                if (fw != null) try {
                    fw.close();
                } catch (Exception e) {
                } finally {
                    fw = null;
                }
            }
            if (fileSeparator.equals("/")) {
                try {
                    Runtime.getRuntime().exec("chmod +x " + fileName);
                } catch (Exception e) {
                    showWarning("Could not set the execute permission for \"" + fileName + "\": " + e.toString() + "\nDo it yourself...");
                }
            }
            showInformation("\"" + fileName + "\" created.\n\n" + ejenVersion + " installation finished.");
            System.exit(0);
        }
    }
}
