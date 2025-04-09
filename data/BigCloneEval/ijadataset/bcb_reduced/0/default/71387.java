import info.clearthought.layout.TableLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.filechooser.FileFilter;
import Tools.AboutBox;
import Tools.ManifestInfo;
import Tools.Utils;
import Tools.XmlObject;
import com.fransson.app.keywarden.model.Account;
import com.fransson.app.keywarden.model.AccountsTreeNode;
import com.fransson.app.keywarden.model.AccountsTreeRootNode;

public class Keywarden extends JFrame {

    private static final long serialVersionUID = -134119459715600821L;

    private JTextField tfConverted;

    private JTextField tfKeywarden;

    private JButton bConvert;

    @SuppressWarnings("unused")
    private Global global = new Global();

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        new Keywarden().setVisible(true);
    }

    public Keywarden() {
        setTitle(ManifestInfo.info.product + "  V" + ManifestInfo.info.version);
        setSize(500, 250);
        try {
            setIconImage(Utils.getImageRes(this, "/images/logo.jpg"));
        } catch (Exception e2) {
        }
        final int border5 = 5;
        final double layoutPanelModules[][] = { { border5, 100, border5, TableLayout.FILL, border5, 100, border5 }, { border5, 20, 20, 20, 20, 20, 20, 50, border5 } };
        setLayout(new TableLayout(layoutPanelModules));
        JLabel labelKeywarden = new JLabel("Keywarden XML");
        add(labelKeywarden, "1,1,r,c");
        tfKeywarden = new JTextField();
        tfKeywarden.addCaretListener(new CaretListener() {

            public void caretUpdate(CaretEvent arg0) {
                if (new File(tfKeywarden.getText()).exists()) {
                    bConvert.setEnabled(true);
                }
            }
        });
        add(tfKeywarden, "3,1,f,c");
        JButton bKeywarden = new JButton("...");
        bKeywarden.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JFileChooser d = new JFileChooser("Keywarden XML file");
                d.setCurrentDirectory(new File("."));
                d.setFileFilter(new FileFilter() {

                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory() || f.getName().toLowerCase().endsWith(".xml");
                    }

                    @Override
                    public String getDescription() {
                        return "*.xml";
                    }
                });
                if (d.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    tfKeywarden.setText(d.getSelectedFile().getPath());
                }
            }
        });
        add(bKeywarden, "5,1,l,c");
        JLabel labelConverted = new JLabel("Converted XML");
        add(labelConverted, "1,3,r,c");
        tfConverted = new JTextField();
        add(tfConverted, "3,3,f,c");
        JButton bConverted = new JButton("...");
        bConverted.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                JFileChooser d = new JFileChooser("Converted XML file");
                d.setCurrentDirectory(new File("."));
                d.setFileFilter(new FileFilter() {

                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory() || f.getName().toLowerCase().endsWith(".xml");
                    }

                    @Override
                    public String getDescription() {
                        return "*.xml";
                    }
                });
                if (d.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    tfConverted.setText(d.getSelectedFile().getPath());
                }
            }
        });
        add(bConverted, "5,3,l,c");
        bConvert = new JButton("Convert");
        bConvert.setEnabled(false);
        bConvert.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (tfConverted.getText().isEmpty()) {
                    String s = new File(tfKeywarden.getText()).getParent();
                    s = s + "/New_" + new File(tfKeywarden.getText()).getName();
                    tfConverted.setText(s);
                }
                convert(new File(tfKeywarden.getText()), new File(tfConverted.getText()));
            }
        });
        add(bConvert, "3,7,c,f");
        JButton bAbout = new JButton("About");
        bAbout.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                new AboutBox(null, ManifestInfo.info, Global.LINKURL);
            }
        });
        add(bAbout, "5,7");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    /**
	 * 
	 * @param inputFile
	 * @param outputFile
	 */
    private void convert(File inputFile, File outputFile) {
        FileInputStream fis;
        try {
            fis = new FileInputStream(inputFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            XMLDecoder xmlDecoder = new XMLDecoder(bis);
            AccountsTreeRootNode mb = new AccountsTreeRootNode();
            mb = (AccountsTreeRootNode) xmlDecoder.readObject();
            OutputStreamWriter outFile;
            try {
                outFile = new OutputStreamWriter(new FileOutputStream(outputFile), "ISO-8859-1");
                outFile.write("<?xml version = \"1.0\" encoding = \"ISO-8859-1\" ?>\r\n");
                outFile.write("\t<!DOCTYPE PasswortTresor [\r\n");
                outFile.write("\t\t<!ELEMENT Groups (Group*, PassItem*) >\r\n");
                outFile.write("\t\t<!ELEMENT Group (groupname, Group*, PassItem*) >\r\n");
                outFile.write("\t\t<!ELEMENT groupname (#PCDATA)>\r\n");
                outFile.write("\t\t<!ELEMENT PassItem (itemname,url,username,password,description) >\r\n");
                outFile.write("\t\t<!ELEMENT itemname (#PCDATA)>\r\n");
                outFile.write("\t\t<!ELEMENT url (#PCDATA)>\r\n");
                outFile.write("\t\t<!ELEMENT username (#PCDATA)>\r\n");
                outFile.write("\t\t<!ELEMENT password (#PCDATA)>\r\n");
                outFile.write("\t\t<!ELEMENT description (#PCDATA)>\r\n");
                outFile.write("\t]>\r\n");
                outFile.write("\r\n");
                outFile.write("<Groups>\r\n");
                outFile.write("\t<Group>\r\n");
                outFile.write("\t\t<groupname>" + "Imported" + "</groupname>\r\n");
                outFile.write(XmlObject.toXml(mb, "\t\t").toString());
                outFile.write("\t</Group>\r\n");
                outFile.write("</Groups>\r\n");
                outFile.close();
            } catch (IOException e) {
            }
        } catch (FileNotFoundException e) {
        }
    }

    /**
	 * 
	 */
    @SuppressWarnings("unused")
    private void writeTest() {
        AccountsTreeRootNode mb = new AccountsTreeRootNode();
        mb.getUserData().add("user1");
        mb.getUserData().add("user2");
        mb.setUserObject("ob1");
        AccountsTreeNode userObject = new AccountsTreeNode();
        AccountsTreeNode e1 = new AccountsTreeNode();
        Account e2 = new Account();
        e2.setIdentity("id1");
        e2.setName("name1");
        e1.add(e2);
        e2 = new Account();
        e2.setIdentity("id2");
        e2.setName("name2");
        e1.add(e2);
        e1.setUserObject("Folder1");
        userObject.add(e1);
        e1 = new AccountsTreeNode();
        e1.setUserObject("Folder2");
        userObject.add(e1);
        mb.add(userObject);
        FileOutputStream fos;
        try {
            fos = new FileOutputStream("test1.xml");
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            XMLEncoder xmlEncoder = new XMLEncoder(bos);
            xmlEncoder.writeObject(mb);
            xmlEncoder.close();
        } catch (FileNotFoundException e) {
        }
    }
}
