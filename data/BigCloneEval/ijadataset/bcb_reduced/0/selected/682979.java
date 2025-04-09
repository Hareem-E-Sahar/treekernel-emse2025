package eu.keep.gui.wizard.ea;

import eu.keep.gui.util.DBUtil;
import eu.keep.gui.util.RBLanguages;
import eu.keep.gui.wizard.ea.model.*;
import eu.keep.gui.wizard.ea.model.ImageFormat;
import net.miginfocom.swing.MigLayout;
import org.apache.log4j.Logger;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ConfirmPanel extends JPanel {

    private static final Logger logger = Logger.getLogger(ConfirmPanel.class.getName());

    private final JLabel explanation;

    private final EAWizardAdd parent;

    ConfirmPanel(final EAWizardAdd parent) {
        super.setLayout(new BorderLayout(5, 5));
        final JPanel center = new JPanel(new MigLayout());
        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        this.parent = parent;
        explanation = new JLabel();
        center.add(new JLabel(" "), "wrap");
        center.add(explanation, "span 2 1 wrap");
        center.add(new JLabel(" "), "wrap");
        center.add(new JLabel(" "), "wrap");
        final JButton previous = new JButton("<html>&larr;</html>");
        final JButton cancel = new JButton(RBLanguages.get("cancel"));
        final JButton confirm = new JButton(RBLanguages.get("confirm"));
        buttons.add(previous);
        buttons.add(cancel);
        buttons.add(confirm);
        super.add(center, BorderLayout.CENTER);
        super.add(buttons, BorderLayout.SOUTH);
        previous.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                parent.remove(parent.confirm);
                parent.add(parent.step2, BorderLayout.CENTER);
                parent.log("2/2, " + RBLanguages.get("select_hardware"));
                parent.validate();
                parent.repaint();
            }
        });
        cancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Object[] options = { RBLanguages.get("yes_exit"), RBLanguages.get("no_exit") };
                int returnValue = JOptionPane.showOptionDialog(parent, RBLanguages.get("sure_cancel") + "\n\n" + RBLanguages.get("all_information_discarded") + "!", RBLanguages.get("cancel") + "?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);
                if (returnValue == JOptionPane.YES_OPTION) {
                    parent.dispose();
                }
            }
        });
        confirm.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Object[] options = { RBLanguages.get("yes"), RBLanguages.get("cancel") };
                int returnValue = JOptionPane.showOptionDialog(parent, RBLanguages.get("sure_commit"), RBLanguages.get("commit_changes") + "?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
                if (returnValue == JOptionPane.YES_OPTION) {
                    previous.setEnabled(false);
                    cancel.setEnabled(false);
                    confirm.setEnabled(false);
                    parent.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    parent.log(RBLanguages.get("committing_changes") + ". " + RBLanguages.get("log_please_wait") + "...");
                    parent.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                    parent.setEnabled(false);
                    (new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                final Emulator emu = parent.step1.emu;
                                final Hardware[] hardwares = parent.step2.hardwares;
                                final ImageFormat[] formats = parent.step2.formats;
                                java.util.List<File> filesToZip = new ArrayList<File>();
                                readRecursive(emu.folder, filesToZip);
                                createZipFile(filesToZip, emu._package, emu.folder.getAbsolutePath());
                                execute(DBUtil.DB.EA, "successfully inserted " + emu.name, "could not insert " + emu.name, "INSERT INTO emulatorarchive.emulators " + "(emulator_id, name, version, exec_type, exec_name, description, language_id, package_name, package_type, package_version, package, user_instructions) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, FILE_READ(?), ?)", emu.emulator_id, emu.name, emu.version, emu.exec_type, emu.exec_name, emu.description, emu.language_id, emu.package_name, emu.package_type, emu.package_version, emu._package, emu.user_instructions);
                                for (Hardware hardware : hardwares) {
                                    execute(DBUtil.DB.EA, "successfully associated the emulator with the hardware", "could not associate the emulator with the hardware", "INSERT INTO emulatorarchive.emus_hardware (emulator_id, hardware_id) VALUES(?, ?)", emu.emulator_id, hardware.hardware_id);
                                }
                                for (ImageFormat format : formats) {
                                    execute(DBUtil.DB.EA, "successfully associated the emulator with the disk image format", "could not associate the emulator with the disk image format", "INSERT INTO emulatorarchive.emus_imageformats (emulator_id, imageformat_id) VALUES(?, ?)", emu.emulator_id, format.imageformat_id);
                                }
                                execute(DBUtil.DB.CEF, "successfully added the emulator to the whitelist", "could not add the emulator to the whitelist", "INSERT INTO engine.emulator_whitelist (emulator_id, emulator_descr) VALUES(?, ?)", emu.emulator_id, emu.description);
                                parent.log(RBLanguages.get("committed_changes") + "!");
                                if (!(new File(emu._package)).delete()) {
                                    logger.warn("Could not delete emulator zip-file from Java temp folder: " + emu._package);
                                }
                                parent.dispose();
                            } catch (Exception ex) {
                                parent.log(RBLanguages.get("error") + ": " + ex.getMessage());
                                confirm.setEnabled(false);
                            }
                            parent.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            parent.setEnabled(true);
                            parent.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        }
                    })).start();
                } else {
                    parent.log(RBLanguages.get("commit_canceled"));
                }
            }
        });
    }

    /**
     * Create a zip file
     * @param filesToZip a list of all files to be added to the zip file
     * @param zipFilePath the full path for the destination zip file
     * @param rootFolder the root folder where the files in <b>filesToZip</b> are located
     * @throws IOException
     */
    private void createZipFile(List<File> filesToZip, String zipFilePath, String rootFolder) throws IOException {
        byte[] buf = new byte[1024];
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFilePath));
        String regex = "^" + Pattern.quote(rootFolder) + "[\\\\/]?";
        for (File file : filesToZip) {
            FileInputStream in = new FileInputStream(file.getAbsolutePath());
            String relativePath = file.getAbsolutePath().replaceAll(regex, "");
            out.putNextEntry(new ZipEntry(relativePath));
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.closeEntry();
            in.close();
        }
        out.close();
    }

    /**
     * Recursively find all files in a directory and add them to a list of files to be zipped
     * @param folder the directory that will be scanned
     * @param files a list which will be populated with all files in <b>folder</b> and its subdirectories
     */
    private void readRecursive(File folder, java.util.List<File> files) {
        File[] contents = folder.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (!f.isDirectory()) {
                    files.add(f);
                } else {
                    readRecursive(f, files);
                }
            }
        }
    }

    public void init() {
        final Emulator emu = parent.step1.emu;
        final Hardware[] hardwares = parent.step2.hardwares;
        final ImageFormat[] formats = parent.step2.formats;
        StringBuilder explanationText = new StringBuilder(String.format(RBLanguages.get("confirm_commit_part1"), emu.name, emu.name, emu.folder, emu.name));
        for (Hardware hardware : hardwares) {
            explanationText.append("<li>" + hardware.name + "</li>");
        }
        explanationText.append(String.format(RBLanguages.get("confirm_commit_part2"), emu.name));
        for (ImageFormat format : formats) {
            explanationText.append("<li>" + format.name + "</li>");
        }
        explanationText.append(RBLanguages.get("confirm_commit_part3"));
        explanation.setText(explanationText.toString());
    }

    private void execute(DBUtil.DB db, String onSuccess, String onError, String sql, Object... params) {
        int count = DBUtil.update(db, sql, params);
        if (count == 1) {
            logger.info(onSuccess);
        } else {
            logger.error(onError);
            throw new RuntimeException(onError);
        }
    }
}
