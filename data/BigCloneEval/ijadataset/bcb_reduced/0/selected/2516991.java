package com.elibera.ccs.dialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import com.elibera.ccs.app.MLEConfig;
import com.elibera.ccs.img.HelperOp;
import com.elibera.ccs.img.MLEImage;
import com.elibera.ccs.panel.HelperPanel;
import com.elibera.ccs.panel.InterfaceDialogClose;
import com.elibera.ccs.panel.std.PanelRootFooter;
import com.elibera.ccs.panel.std.PanelRootHeader;
import com.elibera.ccs.parser.HelperXMLParserSimple;
import com.elibera.ccs.parser.InterfaceDocContainer;
import com.elibera.ccs.res.Msg;
import com.elibera.ccs.util.HelperStd;
import com.elibera.util.scorm.ContentPackage;
import com.elibera.util.scorm.HelperContentPackage;

/**
 * @author meisi
 *
 */
public class DialogMyMLE extends JDialog implements InterfaceDialogClose, ActionListener {

    private static final long serialVersionUID = 100000000202L;

    private JScrollPane jScrollPane = null;

    private JPanel jPanel = null;

    private MLEConfig conf;

    File projectFile = null;

    Properties bundle = null;

    private static DialogMyMLE dialog;

    public static void showDialog(MLEConfig conf, String projectFile) {
        dialog = new DialogMyMLE(conf, projectFile);
        dialog.setVisible(true);
    }

    /**
	 * This is the default constructor
	 */
    private DialogMyMLE(MLEConfig conf, String projectFile) {
        super(JOptionPane.getFrameForComponent(conf.ep), Msg.getMsg("DIALOG_MYMLE_TITLE"), true);
        this.conf = conf;
        this.projectFile = new File(projectFile);
        try {
            bundle = new Properties();
            if (this.projectFile.exists()) {
                FileInputStream in = new FileInputStream(this.projectFile);
                PropertyResourceBundle bundle2 = new PropertyResourceBundle(in);
                java.util.Enumeration en = bundle2.getKeys();
                while (en.hasMoreElements()) {
                    String key = (String) en.nextElement();
                    bundle.put(key, bundle2.getString(key));
                }
                in.close();
                in = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        initialize();
        pack();
    }

    /**
	 * This method initializes this
	 * 
	 * @return void
	 */
    private void initialize() {
        this.setContentPane(getJScrollPane());
    }

    public void setContentPane(Container contentPane) {
        HelperPanel.applyComponentOrientation(contentPane);
        super.setContentPane(contentPane);
    }

    /**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getJPanel());
        }
        return jScrollPane;
    }

    /**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
    private JPanel getJPanel() {
        if (jPanel == null) {
            jPanel = new JPanel();
            HelperPanel.formatPanel(jPanel);
            jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
            jPanel.add(new PanelRootHeader(conf, "HELP_MY_MLE_DIALOG", null));
            JPanel panel2 = new JPanel();
            HelperPanel.formatPanel(panel2);
            panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));
            export = new JTextField(40);
            export.setText(bundle.getProperty("export"));
            panel2.add(new JLabel(Msg.getMsg("MYMLE_EXPORT_PATH") + " "));
            panel2.add(export);
            JButton changeExport = new JButton(Msg.getMsg("MYMLE_CHOOSE_EXPORT_PATH"), null);
            changeExport.setActionCommand("changeExport");
            changeExport.addActionListener(this);
            panel2.add(changeExport);
            jPanel.add(panel2);
            jPanel.add(Box.createRigidArea(new Dimension(10, 10)));
            String mlolist = bundle.getProperty("mlos");
            if (mlolist == null) mlolist = "";
            String[] data = new String[0];
            if (mlolist.trim().length() > 0) data = mlolist.split(";");
            listModel = new DefaultListModel();
            for (int i = 0; i < data.length; i++) {
                listModel.addElement(data[i]);
            }
            jPanel.add(new JLabel(Msg.getMsg("MYMLE_USED_MLOS") + ":"));
            mlos = new JList(listModel);
            mlos.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            mlos.setLayoutOrientation(JList.VERTICAL);
            mlos.setVisibleRowCount(10);
            JScrollPane listScroller = new JScrollPane(mlos);
            listScroller.setPreferredSize(new Dimension(350, 150));
            jPanel.add(listScroller);
            jPanel.add(Box.createRigidArea(new Dimension(10, 10)));
            JPanel panel1 = new JPanel();
            HelperPanel.formatPanel(panel1);
            panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
            JButton add = new JButton(Msg.getMsg("MYMLE_ADD_MLO"), null);
            add.setActionCommand("add");
            add.addActionListener(this);
            panel1.add(add);
            panel1.add(Box.createRigidArea(new Dimension(4, 4)));
            JButton remove = new JButton(Msg.getMsg("MYMLE_REMOVE_SELECTED_MLOS"), null);
            remove.setActionCommand("remove");
            remove.addActionListener(this);
            panel1.add(remove);
            jPanel.add(panel1);
            jPanel.add(Box.createRigidArea(new Dimension(10, 10)));
            jPanel.add(Box.createRigidArea(new Dimension(10, 10)));
            JButton create = new JButton(Msg.getMsg("MYMLE_CREATE_APP"), null);
            create.setActionCommand("create");
            create.addActionListener(this);
            jPanel.add(create);
            panelRootFooter = new PanelRootFooter(this, conf);
            jPanel.add(panelRootFooter);
        }
        return jPanel;
    }

    PanelRootFooter panelRootFooter;

    JList mlos;

    DefaultListModel listModel;

    JTextField export;

    public void closeDialog() {
        setVisible(false);
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("add")) {
            String file = HelperOp.getMLOWithFileChoose(Msg.getMsg("MYMLE_SELECT_MLO"), this);
            if (file != null) {
                if (listModel.contains(file)) listModel.removeElement(file);
                listModel.addElement(file);
                updateConfigFile();
            }
        } else if (cmd.equals("remove")) {
            int[] selected = mlos.getSelectedIndices();
            if (selected == null) return;
            for (int i = selected.length - 1; i >= 0; i--) {
                listModel.removeElementAt(selected[i]);
            }
            updateConfigFile();
        } else if (cmd.equals("changeExport")) {
            File val = HelperOp.getFolderWithFileChooser(null, this);
            if (val != null && val.isDirectory()) {
                export.setText(val.getAbsolutePath());
                updateConfigFile();
            }
        } else if (cmd.equals("create")) {
            if (export.getText().length() <= 0) return;
            if (!new File(export.getText()).exists()) return;
            if (listModel.size() <= 0 || listModel.isEmpty() || (((String) listModel.getElementAt(0)).equals("") && listModel.size() == 1)) return;
            updateConfigFile();
            new CreateMyMLEClientApps();
        }
    }

    private void updateConfigFile() {
        try {
            Properties prop = new Properties();
            prop.put("export", export.getText());
            Enumeration en = listModel.elements();
            StringBuffer mlos = new StringBuffer();
            while (en.hasMoreElements()) {
                File f = new File((String) en.nextElement());
                if (f.exists()) {
                    if (mlos.length() > 0) mlos.append(";");
                    mlos.append(f.getAbsolutePath());
                }
            }
            prop.put("mlos", mlos.toString());
            if (!this.projectFile.exists()) this.projectFile.createNewFile();
            FileOutputStream fout = new FileOutputStream(this.projectFile);
            prop.store(fout, "");
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class CreateMyMLEClientApps extends JDialog implements Runnable {

        public JLabel lb;

        private CreateMyMLEClientApps() {
            super(JOptionPane.getFrameForComponent(conf.getEditorPanel()), Msg.getMsg("DIALOG_Processing_MyMLE_TITEL"), true);
            lb = new JLabel(Msg.getMsg("DIALOG_Processing_MyMLE_Msg"));
            Container contentPane = getContentPane();
            contentPane.add(lb, BorderLayout.CENTER);
            pack();
            setLocationRelativeTo(conf.getEditorPanel());
            Thread r = new Thread(this, "createMyMLEApps");
            r.start();
            this.setVisible(true);
        }

        public void run() {
            try {
                updateConfigFile();
                try {
                    createMyMLEApps();
                } catch (java.util.zip.ZipException zip) {
                    System.out.println("a ZIP exception occured: " + zip.getLocalizedMessage());
                    zip.printStackTrace();
                    System.out.println("Trying it again");
                    createMyMLEApps();
                }
                JOptionPane.showMessageDialog(this, Msg.getMsg("DIALOG_Processing_MyMLE_Msg_Success"), Msg.getMsg("WORD_SUCCESS"), JOptionPane.INFORMATION_MESSAGE);
                dialog.setVisible(false);
            } catch (Exception ee) {
                ee.printStackTrace();
                JOptionPane.showMessageDialog(this, Msg.getMsg("WORD_ERROR") + ": " + ee.getMessage(), Msg.getMsg("WORD_ERROR"), JOptionPane.ERROR_MESSAGE);
            }
            this.setVisible(false);
        }

        /**
		 * creates the MyMLE application Versions
		 * @throws Exception
		 */
        private boolean createMyMLEApps() throws Exception {
            boolean oswindows = (File.separatorChar == '\\');
            File dest = new File(export.getText());
            if (listModel.size() <= 0 || !dest.exists()) return false;
            File tempFolder = HelperOp.createTempDirectory();
            Enumeration en = listModel.elements();
            StringBuffer mloIds = new StringBuffer();
            StringBuffer xmlInstall = new StringBuffer();
            int count = 0;
            while (en.hasMoreElements()) {
                File f = new File((String) en.nextElement());
                if (f.exists()) {
                    ContentPackage lernobjekt = new ContentPackage();
                    HelperContentPackage.retrieveLernobjekt(lernobjekt, HelperOp.getLocalFile(f.getAbsolutePath()));
                    String xml = lernobjekt.getXml();
                    java.util.ArrayList vocTrainerIds = new java.util.ArrayList();
                    Pattern p = Pattern.compile("<voc[^>]*>");
                    Matcher m = p.matcher(xml);
                    while (m.find()) {
                        String voc = m.group(0);
                        Matcher m2 = Pattern.compile(" u[^=]*=\"([^\"]*)\"").matcher(voc);
                        if (!m2.find()) continue;
                        String id = m2.group(1);
                        System.out.println("Voc-Trainer found:" + id);
                        if (!HelperStd.isEmpty(id)) vocTrainerIds.add(id);
                    }
                    count++;
                    conf.parser.parseXMLPageSuite(xml, lernobjekt, false);
                    int binarycount = 0;
                    HashMap binariesReplace = new HashMap();
                    HashMap vocTrainers = new HashMap();
                    java.util.Iterator it = lernobjekt.getBinariesIDIterator();
                    while (it.hasNext()) {
                        binarycount++;
                        String id = (String) it.next();
                        File binary = new File(tempFolder.getAbsolutePath() + File.separatorChar + "mlo" + count + "_b" + binarycount);
                        if (!vocTrainerIds.contains(id)) {
                            HelperOp.saveFile(binary, lernobjekt.getBinary(id));
                        } else vocTrainers.put(id, binary);
                        xml = xml.replace("=\"" + id + "\"", "=\"" + "/mlo" + count + "_b" + binarycount + "\"");
                        binariesReplace.put(id, "/mlo" + count + "_b" + binarycount);
                    }
                    for (int v = 0; v < vocTrainerIds.size(); v++) {
                        String id = (String) vocTrainerIds.get(v);
                        File binary = (File) vocTrainers.get(id);
                        if (binary == null) continue;
                        String text = new String(lernobjekt.getBinary(id));
                        java.util.Iterator itbin = binariesReplace.keySet().iterator();
                        while (itbin.hasNext()) {
                            String idbin = (String) itbin.next();
                            text = text.replace("=\"" + idbin + "\"", "=\"" + binariesReplace.get(idbin) + "\"");
                        }
                        HelperOp.saveFile(binary, text.getBytes());
                    }
                    HelperOp.saveFile(new File(tempFolder.getAbsolutePath() + File.separatorChar + "mlo" + count + "_index.xml"), xml.getBytes());
                    xmlInstall.append("<button t=\"d\" action=\"/mlo" + count + "_index.xml\" data=\"l\" w=\"" + HelperXMLParserSimple.replaceXMLChars(lernobjekt.getTitel()) + "\" f=\"1\">\n");
                    if (mloIds.length() > 0) mloIds.append("|");
                    mloIds.append("/mlo" + count + "_index.xml");
                    lernobjekt = null;
                }
            }
            if (oswindows) Thread.sleep(500);
            boolean blackberrysupport = (new File(conf.appParams.getParameter("installpath") + File.separatorChar + "rapc.jar")).exists() && (new File(conf.appParams.getParameter("installpath") + File.separatorChar + "net_rim_api.jar").exists());
            System.out.println("BlackBerry support: " + blackberrysupport);
            String[] devices = conf.appParams.getParameter("mymle.devices", "").split(",");
            for (int d = 0; d < devices.length; d++) {
                File path = new File(conf.appParams.getParameter("mymle." + devices[d] + ".path", null));
                if (!path.exists()) continue;
                String[] files = conf.appParams.getParameter("mymle." + devices[d] + ".filenames", "").split(",");
                for (int f = 0; f < files.length; f++) {
                    File jar = new File(path.getAbsolutePath() + File.separatorChar + files[f]);
                    File jardestpath = new File(dest.getAbsolutePath() + File.separatorChar + devices[d]);
                    File jardest = new File(jardestpath.getAbsolutePath() + File.separatorChar + files[f]);
                    if (!jar.exists()) continue;
                    jardestpath.mkdirs();
                    jardest.createNewFile();
                    System.out.println("Reading: " + jar.getAbsolutePath());
                    System.out.println("Writing: " + jardest.getAbsolutePath());
                    ZipInputStream zin1 = new ZipInputStream(new FileInputStream(jar));
                    ZipOutputStream out1 = new ZipOutputStream(new FileOutputStream(jardest));
                    ZipEntry entry1 = zin1.getNextEntry();
                    byte[] buf = new byte[1024];
                    while (entry1 != null) {
                        String name = entry1.getName();
                        out1.putNextEntry(new java.util.zip.ZipEntry(name));
                        if (name.compareTo("inst.xml") == 0) {
                            int len;
                            ByteArrayOutputStream bout = new ByteArrayOutputStream();
                            while ((len = zin1.read(buf)) > 0) {
                                bout.write(buf, 0, len);
                            }
                            String installxml = new String(bout.toByteArray());
                            installxml = installxml.replace("{0}", "<button t=\"dm\" action=\"" + mloIds.toString() + "\" data=\"l\" w=\"--All--\" f=\"1\">\n" + xmlInstall);
                            byte[] b = null;
                            try {
                                b = installxml.getBytes("UTF-8");
                            } catch (Exception eee) {
                                b = installxml.getBytes();
                            }
                            out1.write(b);
                        } else {
                            int len;
                            while ((len = zin1.read(buf)) > 0) {
                                out1.write(buf, 0, len);
                            }
                        }
                        entry1 = zin1.getNextEntry();
                    }
                    zin1.close();
                    System.out.println("Adding MLOs: " + tempFolder.getAbsolutePath());
                    addRecursivly(out1, tempFolder, tempFolder.getAbsolutePath());
                    out1.close();
                    if (files[f].indexOf(".jar") > 0) {
                        File jad1 = new File(path.getAbsolutePath() + File.separatorChar + files[f].replace(".jar", ".jad"));
                        File jad2 = new File(path.getAbsolutePath() + File.separatorChar + "ota_" + files[f].replace(".jar", ".jad"));
                        System.out.println("Processing JAD: " + jad1.getAbsolutePath());
                        if (jad1.exists()) {
                            String jad = new String(HelperOp.getLocalFile(jad1.getAbsolutePath()));
                            jad = jad.replace(jar.length() + "", jardest.length() + "");
                            File jad1dest = new File(jardestpath.getAbsolutePath() + File.separatorChar + files[f].replace(".jar", ".jad"));
                            HelperOp.saveFile(jad1dest, jad.getBytes());
                        }
                        if (jad2.exists()) {
                            String jad = new String(HelperOp.getLocalFile(jad2.getAbsolutePath()));
                            jad = jad.replace(jar.length() + "", jardest.length() + "");
                            File jad2dest = new File(jardestpath.getAbsolutePath() + File.separatorChar + "ota_" + files[f].replace(".jar", ".jad"));
                            HelperOp.saveFile(jad2dest, jad.getBytes());
                        }
                    }
                    if (blackberrysupport && conf.appParams.getParameter("mymle." + devices[d] + ".blackberry", "false").compareTo("true") == 0) {
                        executeProcess((oswindows ? conf.appParams.getParameter("cmd.blackberry.rapc.windows") : conf.appParams.getParameter("cmd.blackberry.rapc.linux")).replace("{0}", processFilePathForOS(jardest.getAbsolutePath().replace(".jar", ""), oswindows)).replace("{1}", processFilePathForOS(path.getAbsolutePath() + File.separatorChar + files[f].replace(".jar", ".jad"), oswindows)).replace("{2}", processFilePathForOS(jardest.getAbsolutePath(), oswindows)), new File(conf.appParams.getParameter("installpath")), true);
                        File jad1 = new File(path.getAbsolutePath() + File.separatorChar + "cod" + File.separatorChar + files[f].replace(".jar", ".alx"));
                        if (!jad1.exists()) jad1 = new File(path.getAbsolutePath() + File.separatorChar + files[f].replace(".jar", ".alx"));
                        if (jad1.exists()) {
                            String jad = new String(HelperOp.getLocalFile(jad1.getAbsolutePath()));
                            jad = jad.replace(jar.length() + "", jardest.length() + "");
                            File jad1dest = new File(jardestpath.getAbsolutePath() + File.separatorChar + files[f].replace(".jar", ".alx"));
                            HelperOp.saveFile(jad1dest, jad.getBytes());
                        }
                    }
                    System.out.println("finished: " + devices[d] + ":" + files[f]);
                }
            }
            processAndroidPackages(dest, mloIds, xmlInstall, tempFolder);
            try {
                HelperOp.saveFile(new File(dest.getAbsolutePath() + File.separatorChar + "README-LIESMICH.txt"), HelperOp.getLocalFile(conf.appParams.getParameter("installpath") + File.separatorChar + "README.txt"));
            } catch (Exception eee) {
                eee.printStackTrace();
            }
            HelperOp.deleteDir(tempFolder);
            return true;
        }

        private String processFilePathForOS(String path, boolean oswindows) {
            return path.replace(" ", "\\ ");
        }

        private void addRecursivly(ZipOutputStream out, File path, String removePath) throws Exception {
            String[] children = path.list();
            byte[] buf = new byte[1024];
            for (int i = 0; i < children.length; i++) {
                File f = new File(path, children[i]);
                if (f.isDirectory()) {
                    addRecursivly(out, f, removePath);
                    continue;
                }
                String filename = f.getAbsolutePath().replace(removePath, "");
                if (filename.indexOf("/") == 0) filename = filename.substring(1);
                if (filename.indexOf(File.separatorChar) == 0) filename = filename.substring(1);
                out.putNextEntry(new java.util.zip.ZipEntry(filename.replace("\\", "/")));
                int len;
                FileInputStream zin = new FileInputStream(f);
                while ((len = zin.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                zin.close();
            }
        }

        private void addRecursivlyFiles(File dest, File path, String removePath) throws Exception {
            String[] children = path.list();
            byte[] buf = new byte[1024];
            for (int i = 0; i < children.length; i++) {
                File f = new File(path, children[i]);
                if (f.isDirectory()) {
                    addRecursivlyFiles(dest, f, removePath);
                    continue;
                }
                String filename = f.getAbsolutePath().replace(removePath, "");
                if (filename.indexOf("/") == 0) filename = filename.substring(1);
                if (filename.indexOf(File.separatorChar) == 0) filename = filename.substring(1);
                File destfile = new File(dest.getAbsolutePath() + File.separatorChar + filename);
                destfile.createNewFile();
                FileOutputStream out = new FileOutputStream(destfile);
                int len;
                FileInputStream zin = new FileInputStream(f);
                while ((len = zin.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                zin.close();
                out.close();
            }
        }

        private void executeProcess(String command, File workingDir, boolean onlyError) {
            try {
                System.out.println("executeProcess:" + command);
                command = command.replace("\\ ", "\r");
                String[] commands = command.split(" ");
                for (int i = 0; i < commands.length; i++) {
                    commands[i] = commands[i].replace("\r", " ");
                }
                Process child = null;
                child = Runtime.getRuntime().exec(commands, null, workingDir);
                InputStream in = child.getInputStream();
                InputStream err = child.getErrorStream();
                new StreamReader(in, !onlyError).start();
                new StreamReader(err, true).start();
                child.waitFor();
                in.close();
                err.close();
                System.out.println("");
                System.out.println("executeProcess finished");
            } catch (Exception e) {
                System.out.println("executeProcess Error occured:" + e.getMessage());
                e.printStackTrace();
            }
        }

        private class StreamReader extends Thread {

            InputStream in;

            boolean show;

            public StreamReader(InputStream in, boolean show) {
                this.in = in;
                this.show = show;
            }

            public void run() {
                try {
                    while (true) {
                        int c = in.read();
                        if (c < 10) continue;
                        if (show) System.out.print((char) c);
                    }
                } catch (Exception e) {
                }
            }
        }

        private void processAndroidPackages(File dest, StringBuffer mloIds, StringBuffer xmlInstall, File tempFolder) throws Exception {
            File androidzip = new File(conf.appParams.getParameter("installpath") + File.separatorChar + "android.zip");
            if (!androidzip.exists()) {
                System.out.println("No Android Support! Android.zip is missing!");
                return;
            }
            boolean oswindows = (File.separatorChar == '\\');
            System.out.println("processAndroidPackages:" + oswindows);
            String[] devices = conf.appParams.getParameter("mymle.android.devices", "").split(",");
            for (int d = 0; d < devices.length; d++) {
                if (devices[d].trim().length() <= 0) continue;
                File path = new File(conf.appParams.getParameter("mymle." + devices[d] + ".path", null));
                if (!path.exists()) continue;
                String[] files = conf.appParams.getParameter("mymle." + devices[d] + ".filenames", "").split(",");
                for (int f = 0; f < files.length; f++) {
                    File jar = new File(path.getAbsolutePath() + File.separatorChar + files[f]);
                    File jardestpath = new File(dest.getAbsolutePath() + File.separatorChar + devices[d]);
                    File jardest = new File(jardestpath.getAbsolutePath() + File.separatorChar + files[f]);
                    System.out.println("apk:" + jar);
                    if (!jar.exists()) continue;
                    jardestpath.mkdirs();
                    jardest.createNewFile();
                    File tempFolderAndroid = HelperOp.createTempDirectory();
                    ZipInputStream zinandroid1 = new ZipInputStream(new FileInputStream(androidzip));
                    System.out.println("android.zip:" + conf.appParams.getParameter("installpath") + File.separatorChar + "android.zip");
                    ZipEntry entry1 = zinandroid1.getNextEntry();
                    byte[] buf = new byte[1024];
                    while (entry1 != null) {
                        String name = entry1.getName();
                        int len;
                        File destAnd = new File(tempFolderAndroid.getAbsolutePath() + File.separatorChar + name);
                        if (entry1.isDirectory()) {
                            destAnd.mkdirs();
                            entry1 = zinandroid1.getNextEntry();
                            continue;
                        }
                        destAnd.getParentFile().mkdirs();
                        destAnd.createNewFile();
                        FileOutputStream bout = new FileOutputStream(destAnd);
                        while ((len = zinandroid1.read(buf)) > 0) {
                            bout.write(buf, 0, len);
                        }
                        bout.close();
                        entry1 = zinandroid1.getNextEntry();
                    }
                    zinandroid1.close();
                    File resraw = new File(tempFolderAndroid.getAbsolutePath() + File.separatorChar + "res" + File.separatorChar + "raw");
                    System.out.println("res/raw/:" + resraw);
                    resraw.mkdirs();
                    ZipInputStream zin1 = new ZipInputStream(new FileInputStream(jar));
                    ZipEntry entry2 = zin1.getNextEntry();
                    while (entry2 != null) {
                        String name = entry2.getName();
                        if (name.indexOf("res/raw/") >= 0) {
                            int len;
                            File destAnd = new File(tempFolderAndroid.getAbsolutePath() + File.separatorChar + name);
                            destAnd.createNewFile();
                            FileOutputStream bout = new FileOutputStream(destAnd);
                            while ((len = zin1.read(buf)) > 0) {
                                bout.write(buf, 0, len);
                            }
                            bout.close();
                        }
                        entry2 = zin1.getNextEntry();
                    }
                    zin1.close();
                    System.out.println("addRecursivlyFiles:" + tempFolder);
                    if (oswindows) Thread.sleep(150);
                    addRecursivlyFiles(resraw, tempFolder, tempFolder.getAbsolutePath());
                    if (!oswindows) executeProcess("chmod +x aapt", tempFolderAndroid, false);
                    if (oswindows) Thread.sleep(500);
                    executeProcess((oswindows ? conf.appParams.getParameter("cmd.android.aapk.windows") : conf.appParams.getParameter("cmd.android.aapk.linux")).replace("{0}", conf.appParams.getParameter("mymle." + devices[d] + ".version", "15")), tempFolderAndroid, false);
                    if (oswindows) Thread.sleep(500);
                    ZipInputStream zinandroid2 = new ZipInputStream(new FileInputStream(new File(tempFolderAndroid.getAbsolutePath() + File.separatorChar + "test.apk")));
                    System.out.println("test.apk:" + tempFolderAndroid.getAbsolutePath() + File.separatorChar + "test.apk");
                    ZipEntry entry3 = zinandroid2.getNextEntry();
                    while (entry3 != null) {
                        String name = entry3.getName();
                        if (name.compareTo("resources.arsc") == 0) {
                            File dres = new File(tempFolderAndroid.getAbsolutePath() + File.separatorChar + name);
                            dres.createNewFile();
                            int len;
                            FileOutputStream bout = new FileOutputStream(dres);
                            while ((len = zinandroid2.read(buf)) > 0) {
                                bout.write(buf, 0, len);
                            }
                            bout.close();
                            break;
                        }
                        entry3 = zinandroid2.getNextEntry();
                    }
                    zinandroid2.close();
                    System.out.println("package apk:" + jar);
                    System.out.println("package apk dest:" + jardest);
                    ZipInputStream zin2 = new ZipInputStream(new FileInputStream(jar));
                    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(jardest));
                    ZipEntry entry4 = zin2.getNextEntry();
                    while (entry4 != null) {
                        String name = entry4.getName();
                        if (name.indexOf("res/raw/") >= 0) {
                            entry4 = zin2.getNextEntry();
                            continue;
                        }
                        if (name.indexOf("META-INF/") >= 0) {
                            if (name.indexOf("MANIFEST") >= 0) {
                                out.putNextEntry(new java.util.zip.ZipEntry(name));
                                out.write("Manifest-Version: 1.0\nCreated-By: 1.0 (Android)\n".getBytes());
                            }
                            entry4 = zin2.getNextEntry();
                            continue;
                        }
                        out.putNextEntry(new java.util.zip.ZipEntry(name));
                        int len;
                        if (name.compareTo("resources.arsc") == 0) {
                            FileInputStream fin = new FileInputStream(tempFolderAndroid.getAbsolutePath() + File.separatorChar + "resources.arsc");
                            while ((len = fin.read(buf)) > 0) {
                                out.write(buf, 0, len);
                            }
                            fin.close();
                        } else {
                            while ((len = zin2.read(buf)) > 0) {
                                out.write(buf, 0, len);
                            }
                        }
                        entry4 = zin2.getNextEntry();
                    }
                    zin2.close();
                    System.out.println("adding MLOs to apk from:" + resraw);
                    String[] children = resraw.list();
                    for (int i = 0; i < children.length; i++) {
                        File file = new File(resraw, children[i]);
                        if (file.isDirectory()) continue;
                        String filename = file.getAbsolutePath().replace(tempFolderAndroid.getAbsolutePath(), "");
                        if (filename.indexOf("/") == 0) filename = filename.substring(1);
                        if (filename.indexOf(File.separatorChar) == 0) filename = filename.substring(1);
                        filename = filename.replace("\\", "/");
                        out.putNextEntry(new java.util.zip.ZipEntry(filename));
                        int len;
                        FileInputStream fin = new FileInputStream(file);
                        if (filename.indexOf("res/raw/inst.xml") >= 0) {
                            ByteArrayOutputStream bout = new ByteArrayOutputStream();
                            while ((len = fin.read(buf)) > 0) {
                                bout.write(buf, 0, len);
                            }
                            String installxml = new String(bout.toByteArray());
                            installxml = installxml.replace("{0}", "<button t=\"dm\" action=\"" + mloIds.toString() + "\" data=\"l\" w=\"--All--\" f=\"1\">\n" + xmlInstall);
                            byte[] b = null;
                            try {
                                b = installxml.getBytes("UTF-8");
                            } catch (Exception eee) {
                                b = installxml.getBytes();
                            }
                            out.write(b);
                        } else {
                            while ((len = fin.read(buf)) > 0) {
                                out.write(buf, 0, len);
                            }
                        }
                        fin.close();
                    }
                    out.close();
                    executeProcess((oswindows ? conf.appParams.getParameter("cmd.android.signing.windows") : conf.appParams.getParameter("cmd.android.signing.linux")).replace("{0}", processFilePathForOS(jardest.getAbsolutePath(), oswindows)), tempFolderAndroid, false);
                    HelperOp.deleteDir(tempFolderAndroid);
                }
            }
        }
    }
}
