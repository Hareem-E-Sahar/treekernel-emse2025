import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import java.awt.event.*;
import java.io.*;

public class EncoderProperties extends Encoder implements DocumentListener {

    public JTextPane text;

    public mjbButton clear, saveToFileButton;

    /**
   * constructor for properties
   * @param parent main xes class
   * @param str title
   */
    public EncoderProperties(xes parent, String str) {
        try {
            this.parent = parent;
            setLayout(new BorderLayout());
            setBorder(new TitledBorder(str));
            addComponents(str);
            addListeners();
        } catch (Exception e) {
            parent.EerrorMsg.text.setText("EncoderProperties constructor " + e);
            parent.jtp.setSelectedIndex(3);
        }
    }

    public void addComponents(String str) {
        JPanel bPanel = new JPanel();
        bPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        text = new JTextPane();
        text.setEditable(true);
        Document document1 = text.getDocument();
        document1.addDocumentListener(this);
        clear = new mjbButton("Clear", KeyEvent.VK_C, this, this);
        saveToFileButton = new mjbButton("save this property to file", KeyEvent.VK_S, this, this);
        bPanel.add(clear);
        bPanel.add(saveToFileButton);
        add(new JScrollPane(text), BorderLayout.CENTER);
        add(bPanel, BorderLayout.SOUTH);
    }

    public void addListeners() {
        if (saveToFileButton != null) {
            saveToFileButton.addMouseListener(new MyMouseListener());
        }
        clear.addMouseListener(new MyMouseListener());
    }

    public void actionPerformed(ActionEvent ae) {
        Object object = ae.getSource();
        if (object == clear) {
            text.setText("");
        } else if (object == saveToFileButton) {
            saveToFile(parent.baseNode);
        }
    }

    public void saveToFile(nodeBase baseNode) {
        try {
            JFileChooser jfc = new JFileChooser();
            int retValue = jfc.showSaveDialog(parent);
            File baseDirectoryFile = new File("");
            if (EncoderLoad.directory == null) EncoderLoad.directory = new File(System.getProperty("user.dir"));
            jfc.setCurrentDirectory(EncoderLoad.directory);
            if (retValue == JFileChooser.APPROVE_OPTION) {
                ((EncoderLoad) (parent.ELoad)).setBaseDir(jfc.getCurrentDirectory());
                baseDirectoryFile = jfc.getSelectedFile();
                BufferedWriter out = new BufferedWriter(new FileWriter(baseDirectoryFile));
                out.write(text.getText());
                out.close();
            }
        } catch (Exception e) {
            parent.EerrorMsg.text.setText("EncoderProperties.saveToFile " + e);
            parent.jtp.setSelectedIndex(3);
        }
    }

    public void insertUpdate(DocumentEvent e) {
        String s = text.getText();
        parent.textChanged(s);
    }

    public void removeUpdate(DocumentEvent e) {
        String s = text.getText();
        parent.textChanged(s);
    }

    public void changedUpdate(DocumentEvent e) {
        String s = text.getText();
        parent.textChanged(s);
    }
}
