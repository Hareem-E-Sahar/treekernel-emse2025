package theme.editor;

import hu.xsolutions.xcsirip.Messages;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import theme.ExtFileFilter;

public class SkinFrame extends JFrame implements ActionListener {

    private static final String SAVE = "s";

    private static final String RESTORE = "r";

    private static final String CANCEL = "c";

    private static final String LOAD = "l";

    private SkinEditor se;

    public SkinFrame() {
        super(Messages.getString("SkinFrame.TITLE"));
        setResizable(false);
        getContentPane().setLayout(new BorderLayout());
        se = new SkinEditor();
        getContentPane().add(se);
        JPanel buttonRow = new JPanel();
        JButton b;
        b = new JButton(Messages.getString("SkinEditor.LOAD"));
        b.setActionCommand(LOAD);
        b.addActionListener(this);
        buttonRow.add(b);
        b = new JButton(Messages.getString("SkinEditor.SAVE"));
        b.setActionCommand(SAVE);
        b.addActionListener(this);
        buttonRow.add(b);
        b = new JButton(Messages.getString("SkinEditor.RESTORE"));
        b.setActionCommand(RESTORE);
        b.addActionListener(this);
        buttonRow.add(b);
        b = new JButton(Messages.getString("SkinEditor.CANCEL"));
        b.setActionCommand(CANCEL);
        b.addActionListener(this);
        buttonRow.add(b);
        getContentPane().add(buttonRow, BorderLayout.SOUTH);
    }

    private JFileChooser fc;

    String loadedTheme;

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals(LOAD)) {
            if (fc == null) {
                fc = new JFileChooser(System.getProperty("user.dir"));
                fc.addChoosableFileFilter(new ExtFileFilter("jar", Messages.getString("XCsiripJAVA_ARCHIVES")));
                fc.addChoosableFileFilter(new ExtFileFilter("zip", Messages.getString("XCsiripZIP_ARCHIVES")));
            }
            if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(this)) {
                File file = fc.getSelectedFile();
                loadedTheme = file.getAbsolutePath();
                se.load(loadedTheme);
            }
        } else if (cmd.equals(RESTORE)) {
            if (loadedTheme != null) {
                se.load(loadedTheme);
            } else {
                this.remove(se);
                se = null;
                se = new SkinEditor();
                getContentPane().add(se);
            }
        } else if (cmd.equals(SAVE)) {
            if (fc == null) {
                fc = new JFileChooser(System.getProperty("user.dir"));
                fc.addChoosableFileFilter(new ExtFileFilter("jar", Messages.getString("XCsiripJAVA_ARCHIVES")));
                fc.addChoosableFileFilter(new ExtFileFilter("zip", Messages.getString("XCsiripZIP_ARCHIVES")));
            }
            String css = se.getCSS();
            String theme = se.getTheme();
            String saveZip = null;
            String themeName = null;
            if (JFileChooser.APPROVE_OPTION == fc.showSaveDialog(this)) {
                File file = fc.getSelectedFile();
                saveZip = file.getAbsolutePath();
                themeName = file.getName();
            }
            if (saveZip != null) {
                String[] filenames = new String[] { themeName + ".css", themeName + ".theme" };
                try {
                    BufferedWriter out = new BufferedWriter(new FileWriter(filenames[0]));
                    out.write(css);
                    out.close();
                    out = new BufferedWriter(new FileWriter(filenames[1]));
                    out.write(theme);
                    out.close();
                } catch (IOException ex) {
                }
                byte[] buf = new byte[1024];
                try {
                    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(saveZip));
                    for (int i = 0; i < filenames.length; i++) {
                        FileInputStream in = new FileInputStream(filenames[i]);
                        out.putNextEntry(new ZipEntry(filenames[i]));
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                        out.closeEntry();
                        in.close();
                    }
                    out.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                new File(filenames[0]).delete();
                new File(filenames[1]).delete();
            }
        } else if (cmd.equals(CANCEL)) {
            dispose();
        }
    }

    public static void main(String[] args) {
        SkinFrame sf = new SkinFrame();
        sf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        sf.pack();
        sf.setVisible(true);
    }
}
