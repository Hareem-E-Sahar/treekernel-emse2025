import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import javax.swing.JPopupMenu;
import javax.swing.border.BevelBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

class MyJCreator extends JFrame {

    boolean newFile = true;

    String savedFilePath;

    Process p = null;

    JPopupMenu popup;

    JMenuBar topBar;

    JMenuItem itemCu, itemCo, itemPa, itemSe, showDoc;

    JMenu menuFile, menuEdit, menuHelp;

    JMenuItem itmNew, itmOpen, itmSave, itmClose, itmCopy, itmCut, itmPaste, itmAbout;

    JTextArea tAreaEditor;

    JFileChooser myFileChooser, mySaveFile;

    Container c;

    String nameOfTheFile, runPathforJava;

    Process pro = null;

    String title = "My JCreator";

    String OpenOrSaveFilePath;

    JButton buttonNew, buttonOpen, buttonSave, buttonCompile, buttonRun;

    Icon newIcon, saveIcon, openIcon, compileIcon, runIcon, copyic, cutic, docic, pasteic;

    Icon new_small, open_small, save_small, about_small, close_small;

    MyJCreator() {
        setTitle(title);
        c = getContentPane();
        c.setLayout(new BorderLayout());
        try {
            javax.swing.UIManager.setLookAndFeel("com.birosoft.liquid.LiquidLookAndFeel");
        } catch (Exception e) {
            System.out.println("I could not look and feel, sorry for the plaf !!");
        }
        popup = new JPopupMenu();
        tAreaEditor = new JTextArea(20, 40);
        tAreaEditor.setLineWrap(true);
        tAreaEditor.setDragEnabled(true);
        tAreaEditor.setFont(new Font("Helvetica", Font.PLAIN, 17));
        cutic = new ImageIcon("cut.jpeg");
        copyic = new ImageIcon("copy.jpeg");
        pasteic = new ImageIcon("paste.jpeg");
        docic = new ImageIcon("doc.jpeg");
        new_small = new ImageIcon("newSmall.png");
        open_small = new ImageIcon("openSmall.png");
        save_small = new ImageIcon("saveSmall.png");
        about_small = new ImageIcon("aboutSmall.png");
        close_small = new ImageIcon("closeSmall.png");
        myFileChooser = new JFileChooser();
        mySaveFile = new JFileChooser();
        topBar = new JMenuBar();
        menuFile = new JMenu("File");
        itmNew = new JMenuItem("New", new_small);
        itmOpen = new JMenuItem("Open", open_small);
        itmSave = new JMenuItem("Save", save_small);
        itmClose = new JMenuItem("Close", close_small);
        menuFile.add(itmNew);
        menuFile.add(itmOpen);
        menuFile.add(itmSave);
        menuFile.add(itmClose);
        topBar.add(menuFile);
        menuEdit = new JMenu("Edit");
        itmCut = new JMenuItem("Cut", cutic);
        itmCopy = new JMenuItem("Copy", copyic);
        itmPaste = new JMenuItem("Paste", pasteic);
        menuEdit.add(itmCut);
        menuEdit.add(itmCopy);
        menuEdit.add(itmPaste);
        topBar.add(menuEdit);
        menuHelp = new JMenu("Help");
        itmAbout = new JMenuItem("About", about_small);
        menuHelp.add(itmAbout);
        topBar.add(menuHelp);
        MyMenuListener mm1 = new MyMenuListener();
        itmNew.addActionListener(mm1);
        itmSave.addActionListener(mm1);
        itmOpen.addActionListener(mm1);
        itmCut.addActionListener(mm1);
        itmPaste.addActionListener(mm1);
        itmCopy.addActionListener(mm1);
        itmAbout.addActionListener(mm1);
        MousePopupListener pml = new MousePopupListener();
        tAreaEditor.addMouseListener(pml);
        Mypoplistener menuListener = new Mypoplistener();
        popup.add(itemCu = new JMenuItem("  Cut", cutic));
        itemCu.addActionListener(menuListener);
        popup.add(itemCo = new JMenuItem("\n  Copy", copyic));
        itemCo.addActionListener(menuListener);
        popup.add(itemPa = new JMenuItem("\n  Paste", pasteic));
        itemPa.addActionListener(menuListener);
        popup.add(itemSe = new JMenuItem("\t Select All"));
        itemSe.addActionListener(menuListener);
        popup.addSeparator();
        popup.add(showDoc = new JMenuItem("\n    JAVA Doc", docic));
        showDoc.addActionListener(menuListener);
        popup.setBorder(new BevelBorder(BevelBorder.RAISED, Color.GRAY, Color.BLACK));
        String add = new String("G:\\downloads from net\\javadwnlds\\docs\\api\\index.html");
        addMouseListener(new MousePopupListener());
        tAreaEditor.add(popup);
        setJMenuBar(topBar);
        JToolBar togglePanel = new JToolBar();
        newIcon = new ImageIcon("new.png");
        openIcon = new ImageIcon("open.png");
        saveIcon = new ImageIcon("save.png");
        compileIcon = new ImageIcon("compile.png");
        runIcon = new ImageIcon("run.png");
        buttonNew = new JButton(newIcon);
        buttonNew.setToolTipText("Create a new file");
        buttonOpen = new JButton();
        buttonOpen.setToolTipText("Open another file");
        buttonOpen.setIcon(openIcon);
        buttonSave = new JButton(saveIcon);
        buttonSave.setToolTipText("Save the source code");
        buttonCompile = new JButton(compileIcon);
        buttonCompile.setToolTipText("Compile the source code");
        buttonRun = new JButton(runIcon);
        buttonRun.setToolTipText("Run    the     compiled    code");
        togglePanel.add(buttonNew);
        togglePanel.add(buttonOpen);
        togglePanel.add(buttonSave);
        togglePanel.addSeparator(new Dimension(40, 10));
        togglePanel.add(buttonCompile);
        togglePanel.add(buttonRun);
        c.add(togglePanel, BorderLayout.NORTH);
        MyButtonListener butLisn = new MyButtonListener();
        buttonNew.addActionListener(mm1);
        buttonOpen.addActionListener(mm1);
        buttonSave.addActionListener(mm1);
        buttonCompile.addActionListener(butLisn);
        buttonRun.addActionListener(butLisn);
        JScrollPane myScrollPane = new JScrollPane(tAreaEditor);
        c.add(myScrollPane);
    }

    private class Mypoplistener implements ActionListener {

        public void actionPerformed(ActionEvent event) {
            if (event.getSource() == itemCo) tAreaEditor.copy();
            if (event.getSource() == itemCu) tAreaEditor.cut();
            if (event.getSource() == itemPa) tAreaEditor.paste();
            if (event.getSource() == itemSe) tAreaEditor.selectAll();
            if (event.getSource() == showDoc) {
                String add = "G:\\downloads from net\\javadwnlds\\docs\\api\\index.html";
                Runtime r = Runtime.getRuntime();
                try {
                    pro = r.exec("C:\\Program Files\\Internet Explorer\\IEXPLORE.EXE" + add);
                } catch (Exception x) {
                    JOptionPane.showMessageDialog(null, "Contact your administrator", "Contact your administrator", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private class MyButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == buttonCompile) {
                Process pCompile = null;
                Runtime rCompile = Runtime.getRuntime();
                Runtime rt = Runtime.getRuntime();
                try {
                    Process proc = rt.exec("javac " + OpenOrSaveFilePath);
                    InputStream stderr = proc.getErrorStream();
                    InputStreamReader isr = new InputStreamReader(stderr);
                    BufferedReader br = new BufferedReader(isr);
                    String line = null;
                    String fullList = "";
                    while ((line = br.readLine()) != null) fullList = fullList + line + "\n";
                    int exitVal = proc.waitFor();
                    System.out.println("Process exitValue: " + exitVal);
                    if (fullList != null) JOptionPane.showMessageDialog(null, fullList, "Compile errors", JOptionPane.ERROR_MESSAGE);
                    System.out.println(fullList);
                } catch (Exception ex) {
                }
            }
            if (e.getSource() == buttonRun) {
                Process pRun = null;
                Runtime rRun = Runtime.getRuntime();
                try {
                    System.out.println("trying to run the program");
                    String runName = nameOfTheFile.substring(0, nameOfTheFile.length() - 5);
                    System.out.println(runName);
                    String arg = "java -classpath" + runPathforJava + "  " + runName;
                    pRun = rRun.exec("java -classpath " + runPathforJava + " " + runName);
                    InputStream stderr = pRun.getInputStream();
                    InputStreamReader isr = new InputStreamReader(stderr);
                    BufferedReader br = new BufferedReader(isr);
                    String line = null;
                    String fullList = "";
                    while ((line = br.readLine()) != null) fullList = fullList + line + "\n";
                    int exitVal = pRun.waitFor();
                    System.out.println("Process exitValue: " + exitVal);
                    if (fullList != null) JOptionPane.showMessageDialog(null, fullList, "Output of the program", JOptionPane.INFORMATION_MESSAGE);
                    System.out.println(fullList);
                } catch (Exception ex) {
                }
            }
        }
    }

    class MousePopupListener extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            showPopup(e);
        }

        public void mouseClicked(MouseEvent e) {
            showPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            showPopup(e);
        }

        private void showPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    private class MyMenuListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == itmNew || e.getSource() == buttonNew) {
                tAreaEditor.setText("");
                newFile = true;
            }
            if (e.getSource() == itmOpen || e.getSource() == buttonOpen) {
                FileInputStream readFile;
                int returnval = mySaveFile.showOpenDialog(c);
                File f = mySaveFile.getSelectedFile();
                String openFilePath = f.getPath();
                OpenOrSaveFilePath = openFilePath;
                nameOfTheFile = f.getName();
                setTitle(title + "-  [" + nameOfTheFile + "]");
                runPathforJava = f.getParent();
                System.out.println("The path of the file is:  " + runPathforJava);
                try {
                    readFile = new FileInputStream(openFilePath);
                    byte data[] = new byte[readFile.available()];
                    readFile.read(data);
                    String byteText = new String(data);
                    tAreaEditor.setText(byteText);
                } catch (Exception excp) {
                    System.out.println("get the hell out of here");
                }
            }
            if (e.getSource() == itmSave || e.getSource() == buttonSave) {
                FileOutputStream outputfile;
                if (newFile) {
                    int returnval = mySaveFile.showSaveDialog(c);
                    File f = mySaveFile.getSelectedFile();
                    savedFilePath = f.getPath();
                    OpenOrSaveFilePath = savedFilePath;
                    nameOfTheFile = f.getName();
                    runPathforJava = f.getParent();
                    newFile = false;
                }
                System.out.println("The path of the file is:" + savedFilePath);
                try {
                    outputfile = new FileOutputStream(savedFilePath);
                    byte data[] = tAreaEditor.getText().getBytes();
                    outputfile.write(data);
                } catch (Exception excp) {
                    System.out.println("get the hell out of here");
                }
            }
            if (e.getSource() == itmCut) tAreaEditor.cut();
            if (e.getSource() == itmCopy) tAreaEditor.copy();
            if (e.getSource() == itmPaste) tAreaEditor.paste();
            if (e.getSource() == itmAbout) JOptionPane.showMessageDialog(null, "name:Nandagopal" + "\n\nLicense:GNU GPL" + "\n\ne-mail: nand...@gmail.com");
        }
    }
}

class MyJCreatorTest {

    public static void main(String args[]) {
        MyJCreator m1 = new MyJCreator();
        m1.setSize(640, 450);
        m1.setVisible(true);
        m1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
