package addressbook;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import rogatkin.ActionPerformer;
import rogatkin.BaseController;
import rogatkin.Persistable;
import rogatkin.Program;
import rogatkin.Serializer;
import rogatkin.servlet.BaseFormProcessor;

public class AddressBookFrame extends JFrame implements ActionListener, Program, Persistable {

    public static final String PROGRAMNAME = "jAddressBook";

    public static final String VERSION = "version 1.3";

    public static final String DEDICATED = "E. B.";

    public static final String COPYRIGHT = "Copyright � 2000-2006 Dmitriy Rogatkin\n All rights reserved.";

    public static final int BUILD = 28;

    public static final String CRLF = "\r\n";

    public static final Integer COMP_ACTIONPERFORMER = new Integer(101);

    public static final Integer COMP_ADDRESSBOOK = new Integer(102);

    public static final String ABVIEW = "DataBookView";

    public static final String VIEW = "View";

    public static final String SORTBY = "SortBy";

    public static final String NAMESORT = "LastNameSort";

    public static final String SORTORDER = "SortOrder";

    public static final String BOUNDS = "Bounds";

    public static final String HOME = PROGRAMNAME + Serializer.HOMEDIRSUFX;

    public static final String STOR_NAME = "datastorage.xml";

    static final int MENU = 1;

    static final int TOOL = 2;

    static final int STATUS = 4;

    static final int SEARCH = 8;

    static final int FOLDER = 16;

    int view = MENU + TOOL + STATUS + SEARCH + FOLDER;

    boolean standalone;

    int sortField;

    int subSortField;

    boolean sortDir;

    BaseController controller;

    List folders;

    DataBookIO io;

    Properties properties;

    protected JLabel statusLine;

    protected boolean readFailed;

    JCheckBoxMenuItem m_toolBar, m_statusBar, m_folder;

    public AddressBookFrame(BaseController controller) {
        super(PROGRAMNAME);
        this.controller = controller;
        if (this.controller == null) this.controller = new BaseController(this) {

            {
                standalone = true;
            }

            Image mainicon;

            public Image getMainIcon() {
                if (mainicon == null) mainicon = AddressBookFrame.this.getResourceIcon(AddressBookResources.IMG_PROGRAM).getImage();
                return mainicon;
            }
        };
        this.controller.add(this, getName());
        properties = System.getProperties();
        try {
            io = new DataBookIO(properties);
        } catch (NoClassDefFoundError ncde) {
            JOptionPane.showMessageDialog(this, "Exception: " + ncde);
            return;
        }
        setIconImage(this.controller.getMainIcon());
        if (standalone) this.controller.load(); else load();
        Container c = getContentPane();
        if ((view & MENU) != 0) setJMenuBar(createMenu());
        if ((view & TOOL) != 0) c.add(createToolBar(JToolBar.HORIZONTAL), "North");
        c.add(createBook(), "Center");
        if ((view & STATUS) != 0) c.add(createStatusBar(), "South");
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                actionPerformed(new ActionEvent(this, 0, standalone ? AddressBookResources.MENU_EXIT : AddressBookResources.MENU_CLOSE));
            }
        });
        pack();
        setVisible(true);
        setStatusText(PROGRAMNAME);
    }

    protected ImageIcon getResourceIcon(String iconName) {
        return BaseController.getResourceIcon("resource/image/" + iconName);
    }

    public String getName() {
        return PROGRAMNAME;
    }

    public String getVersion() {
        return VERSION;
    }

    public void save() {
        Serializer s = controller.getSerializer();
        s.setProperty(ABVIEW, VIEW, new Integer(view));
        s.setProperty(ABVIEW, SORTBY, new Integer(sortField));
        s.setProperty(ABVIEW, NAMESORT, new Integer(subSortField));
        s.setProperty(ABVIEW, SORTORDER, new Integer(sortDir ? 1 : 0));
        Rectangle r = getBounds();
        Integer[] boundsHolder = new Integer[] { new Integer(r.x), new Integer(r.y), new Integer(r.width), new Integer(r.height) };
        s.setProperty(ABVIEW, BOUNDS, boundsHolder);
        if (standalone) s.save();
        if (readFailed) {
            if (JOptionPane.showConfirmDialog(this, AddressBookResources.LABEL_CONFIRM_OVERWRITE, AddressBookResources.TITLE_CONFIRM, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.CANCEL_OPTION) return;
        }
        boolean locAcc = s.getInt(s.getProperty(OptionsFrame.OPTIONS, OptionsFrame.ACCESS), 0) == 0;
        String loc = (String) s.getProperty(OptionsFrame.OPTIONS, OptionsFrame.LOCATION);
        OutputStream os = null;
        try {
            if (locAcc) {
                if (loc != null && loc.length() > 0) os = new FileOutputStream(loc); else os = new FileOutputStream(properties.getProperty(HOME, "." + File.separatorChar) + File.separatorChar + STOR_NAME);
                io.write(folders, os, "utf-8");
            } else {
                if (loc.indexOf('?') > 0) loc += "&submit.x=1"; else loc += "?submit.x=1";
                URLConnection con = new URL(loc).openConnection();
                String pass = (String) s.getProperty(OptionsFrame.OPTIONS, OptionsFrame.PASSWORD);
                if (pass != null) {
                    if (pass.length() > 0) pass = pass.substring(1);
                    if (AddressBookResources.LABEL_AUTHONTEFICATS[1].equals((String) s.getProperty(OptionsFrame.OPTIONS, OptionsFrame.AUTH))) con.setRequestProperty(BaseFormProcessor.AUTHORIZATION, "Basic " + new sun.misc.BASE64Encoder().encode(("" + s.getProperty(OptionsFrame.OPTIONS, OptionsFrame.LOGIN) + ":" + BaseController.encryptXor(new String(BaseController.hexToBytes(pass), BaseController.ISO_8859_1))).getBytes()));
                }
                String boundary = Long.toHexString(new Random().nextLong());
                con.setRequestProperty(BaseFormProcessor.CONTENT_TYPE, BaseFormProcessor.MULTIPARTDATA + "; " + BaseFormProcessor.BOUNDARY_EQ + boundary);
                con.setDoOutput(true);
                os = con.getOutputStream();
                os.write((BaseFormProcessor.END_SFX + boundary + CRLF).getBytes());
                os.write((BaseFormProcessor.CONTENT_DESP + ": " + BaseFormProcessor.FORM_DATA + "; " + BaseFormProcessor.NAME_EQ_QT + "data" + "\"; " + BaseFormProcessor.FILENAME_EQ_QT + STOR_NAME + "\"" + CRLF).getBytes());
                os.write((BaseFormProcessor.CONTENT_TYPE + ": " + "text/xml" + "; charset=utf-8" + CRLF).getBytes());
                os.write((CRLF).getBytes());
                io.write(folders, os, "utf-8");
                os.write((CRLF + BaseFormProcessor.END_SFX + boundary + BaseFormProcessor.END_SFX + CRLF).getBytes());
                os.flush();
                System.err.println("code:" + ((HttpURLConnection) con).getResponseCode());
            }
        } catch (FileNotFoundException fnfe) {
            System.err.println("Can't create file " + properties.getProperty(HOME, "./") + STOR_NAME + ' ' + fnfe);
        } catch (IOException ioe) {
            System.err.println("IO problem at writing back: " + ioe);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (Exception e) {
            }
        }
    }

    public void load() {
        Serializer s = controller.getSerializer();
        if (standalone) s.load();
        view = s.getInt(s.getProperty(ABVIEW, VIEW), MENU + TOOL + STATUS + SEARCH + FOLDER);
        sortField = s.getInt(s.getProperty(ABVIEW, SORTBY), BaseAttrTableModel.UN_SORT);
        subSortField = s.getInt(s.getProperty(ABVIEW, NAMESORT), BaseAttrTableModel.FIRSTNAME_SORT);
        sortDir = s.getInt(s.getProperty(ABVIEW, SORTORDER), 0) != 0;
        Object[] boundsHolder = (Object[]) s.getProperty(ABVIEW, BOUNDS);
        ;
        if (boundsHolder != null) setBounds(((Integer) boundsHolder[0]).intValue(), ((Integer) boundsHolder[1]).intValue(), ((Integer) boundsHolder[2]).intValue(), ((Integer) boundsHolder[3]).intValue());
        InputStream is = null;
        boolean locAcc = s.getInt(s.getProperty(OptionsFrame.OPTIONS, OptionsFrame.ACCESS), 0) != 0;
        String loc = (String) s.getProperty(OptionsFrame.OPTIONS, OptionsFrame.LOCATION);
        if (loc == null) try {
            is = new FileInputStream(loc = properties.getProperty(HOME, "." + File.separatorChar) + File.separatorChar + STOR_NAME);
        } catch (FileNotFoundException fnfe) {
            System.err.println("Couldn't load from " + loc + ", " + fnfe);
        } else {
            try {
                URLConnection con = new URL(loc).openConnection();
                String pass = (String) s.getProperty(OptionsFrame.OPTIONS, OptionsFrame.PASSWORD);
                if (pass != null) {
                    if (pass.length() > 0) pass = pass.substring(1);
                    if (AddressBookResources.LABEL_AUTHONTEFICATS[1].equals((String) s.getProperty(OptionsFrame.OPTIONS, OptionsFrame.AUTH))) con.setRequestProperty(BaseFormProcessor.AUTHORIZATION, "Basic " + new sun.misc.BASE64Encoder().encode(("" + s.getProperty(OptionsFrame.OPTIONS, OptionsFrame.LOGIN) + ":" + BaseController.encryptXor(new String(BaseController.hexToBytes(pass), BaseController.ISO_8859_1))).getBytes()));
                }
                is = con.getInputStream();
                locAcc = false;
            } catch (Exception e) {
                System.err.println("Can't open URL " + loc + ", " + e);
            }
            if (is == null) try {
                is = new FileInputStream(loc);
            } catch (FileNotFoundException fnfe) {
                System.err.println("Couldn't load from " + loc + ", " + fnfe);
            }
        }
        if (is != null) {
            try {
                folders = io.read(is);
            } catch (AddressException ae) {
                readFailed = true;
                System.err.println("Exception at reading ab:" + ae);
            } finally {
                try {
                    is.close();
                } catch (IOException ioe) {
                }
            }
        } else readFailed = true;
        if (folders == null) {
            folders = new Vector();
            folders.add(new Folder<Contact>(AddressBookResources.LABEL_PERSONS));
        }
        OptionsFrame.applyLocale(s);
    }

    public void setStatusText(String _text) {
        if (statusLine != null) statusLine.setText(_text);
    }

    JMenuBar createMenu() {
        JMenuBar menubar = new JMenuBar();
        JMenu menu, menu2;
        JMenuItem item;
        menubar.add(menu = new JMenu(AddressBookResources.MENU_FILE));
        createNewMenu(menu, this);
        menu.addSeparator();
        menu.add(item = new JMenuItem(AddressBookResources.MENU_PROPERTIES));
        item.addActionListener(this);
        menu.add(item = new JMenuItem(AddressBookResources.MENU_DELETE));
        item.addActionListener(this);
        menu.addSeparator();
        menu.add(menu2 = new JMenu(AddressBookResources.MENU_IMPORT));
        menu2.add(item = new JMenuItem(AddressBookResources.MENU_ADDRBOOK));
        menu2.add(item = new JMenuItem(AddressBookResources.MENU_BUSCARD));
        menu2.add(new AbstractAction(AddressBookResources.MENU_OTHERADDRBOOK) {

            public void actionPerformed(ActionEvent ae) {
                Object importType = JOptionPane.showInputDialog(AddressBookFrame.this, AddressBookResources.LABEL_SELECT_IMPORT_FORMAT, AddressBookResources.TITLE_IMPORT_TYPE, JOptionPane.QUESTION_MESSAGE, null, AddressBookResources.LABELS_IMP_EXP_FMT_NAME, "Outlook .CSV format");
                if ("Outlook .CSV format".equals(importType)) {
                    JFileChooser chooser = new JFileChooser() {

                        public boolean accept(File f) {
                            return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
                        }
                    };
                    if (chooser.CANCEL_OPTION != chooser.showOpenDialog(AddressBookFrame.this)) {
                        InputStream fis = null;
                        try {
                            if (folders == null) folders = new ArrayList();
                            if (folders.size() == 0) folders.add(new Folder<Contact>(AddressBookResources.LABEL_PERSONS));
                            Folder contacts = (Folder) folders.get(0);
                            Csv csv = new Csv(fis = new FileInputStream(chooser.getSelectedFile()), false, "utf-8", ',', true);
                            Map header = csv.getMetaData();
                            int[] names = new int[4];
                            Arrays.fill(names, 0, names.length - 1, -1);
                            int titleIdx = -1;
                            for (Object fn : header.keySet()) {
                                String s = ((String) fn).toLowerCase();
                                if (s.indexOf("name") >= 0) {
                                    if (s.indexOf("last") >= 0) names[1] = (Integer) header.get(fn); else if (s.indexOf("first") >= 0) names[0] = (Integer) header.get(fn); else if (s.indexOf("middle") >= 0) names[2] = (Integer) header.get(fn); else if (names[0] < 0) names[0] = (Integer) header.get(fn);
                                } else if (s.indexOf("suff") >= 0) names[3] = (Integer) header.get(fn); else if (s.indexOf("title") >= 0) titleIdx = (Integer) header.get(fn);
                            }
                            while (csv.next()) {
                                try {
                                    Contact c = new Contact(new Name((names[0] > 0 ? csv.getString(names[0]) : "") + " " + (names[1] > 0 ? csv.getString(names[1]) : "") + ", " + (names[2] > 0 ? csv.getString(names[2]) : "") + (names[3] > 0 ? "(" + csv.getString(names[0]) + ")" : "")));
                                    if (titleIdx > 0) c.setTitle(csv.getString(titleIdx));
                                    for (String fn : (Set<String>) header.keySet()) {
                                        String s = fn.toLowerCase();
                                        if (s.indexOf("e-mail") >= 0) {
                                            String e = csv.getString(fn);
                                            if (e != null) {
                                                String dn = fn;
                                                try {
                                                    dn = csv.getString(fn + " Display Name");
                                                } catch (IOException ioe) {
                                                }
                                                c.add(new EMail(e, dn, s));
                                            }
                                        } else if (s.indexOf("phone") >= 0 || s.indexOf("fax") >= 0 || s.indexOf("pager") >= 0) {
                                            String n = csv.getString(fn);
                                            if (n != null) c.add(new Telephone(n, fn, s));
                                        } else if (s.indexOf("address") >= 0 && s.indexOf("e-mail") < 0) {
                                            String a = csv.getString(fn);
                                            if (a != null) c.add(new Address(a, fn, s));
                                        } else if (s.indexOf("street") >= 0) {
                                            String pr = fn.substring(0, s.indexOf("street"));
                                            String a = csv.getString(pr + "Street");
                                            if (a != null && a.length() > 0) {
                                                a += csv.getString(pr + "Street 2") + csv.getString(pr + "Street 3") + '\n' + csv.getString(pr + "City") + ',' + csv.getString(pr + "Postal Code") + '\n' + csv.getString(pr + "Country");
                                                c.add(new Address(a, fn, s));
                                            }
                                        } else if (s.indexOf("web") >= 0) {
                                            String l = csv.getString(fn);
                                            if (l != null) c.add(new Link(l, fn));
                                        } else if (s.indexOf("birthday") >= 0) {
                                            String b = csv.getString(fn);
                                            if (b != null) try {
                                                c.setDOB(new SimpleDateFormat("MM/dd/yy").parse(b));
                                            } catch (ParseException pe) {
                                            }
                                        }
                                    }
                                    contacts.add(c);
                                } catch (java.text.ParseException pe) {
                                    System.err.println("Couldn't parse");
                                }
                            }
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        } finally {
                            try {
                                fis.close();
                            } catch (Exception e) {
                            }
                        }
                        tree.setModel(new DefaultTreeModel(createTreeModel(folders), false));
                    }
                }
            }
        });
        menu.add(menu2 = new JMenu(AddressBookResources.MENU_EXPORT));
        menu2.add(new AbstractAction(AddressBookResources.MENU_ADDRBOOK) {

            public void actionPerformed(ActionEvent ae) {
                JFileChooser chooser = new JFileChooser() {

                    public boolean accept(File f) {
                        return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
                    }
                };
                if (chooser.CANCEL_OPTION != chooser.showOpenDialog(AddressBookFrame.this)) {
                }
            }
        });
        menu2.add(item = new JMenuItem(AddressBookResources.MENU_BUSCARD));
        menu2.add(new AbstractAction(AddressBookResources.MENU_IPODCONTACTS) {

            public void actionPerformed(ActionEvent a) {
                OutputStream out = null;
                try {
                    Serializer s = controller.getSerializer();
                    boolean locAcc = s.getInt(s.getProperty(OptionsFrame.OPTIONS, OptionsFrame.ACCESS), 0) == 0;
                    if (locAcc) {
                        String loc = (String) s.getProperty("IpodOptionsTab", OptionsFrame.IPOD_DEVICE);
                        if (loc != null && loc.length() > 0) {
                            Folder f = (Folder) folders.get(0);
                            List<XMLSaver> l = f.getContent();
                            for (XMLSaver saver : l) {
                                try {
                                    out = new FileOutputStream(loc + "/Contacts/" + saver + ".vcf");
                                    saver.saveVCard(out, "UTF-8", 0);
                                } finally {
                                    if (out != null) try {
                                        out.close();
                                    } catch (IOException ioe) {
                                    }
                                    out = null;
                                }
                            }
                        } else {
                            JFileChooser fc = new JFileChooser();
                            fc.setDialogType(JFileChooser.SAVE_DIALOG);
                            fc.showSaveDialog(controller.getMainFrame());
                            File targetPath = fc.getSelectedFile();
                            if (targetPath != null) out = new FileOutputStream(targetPath);
                            ((XMLSaver) folders.get(0)).saveVCard(out, "UTF-8", 0);
                        }
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } finally {
                    if (out != null) try {
                        out.close();
                    } catch (IOException ioe) {
                    }
                }
            }
        });
        menu2.add(new AbstractAction(AddressBookResources.MENU_OTHERADDRBOOK) {

            public void actionPerformed(ActionEvent ae) {
                CSVOptionPanel.doExport((Folder) folders.get(0), AddressBookFrame.this);
            }
        });
        menu.addSeparator();
        menu.add(item = new JMenuItem(AddressBookResources.MENU_PRINT));
        item.addActionListener(this);
        menu.addSeparator();
        menu.add(item = new JMenuItem(standalone ? AddressBookResources.MENU_EXIT : AddressBookResources.MENU_CLOSE));
        item.addActionListener(this);
        menubar.add(menu = new JMenu(AddressBookResources.MENU_EDIT));
        menu.add(item = new JMenuItem(AddressBookResources.MENU_COPY));
        item.setAccelerator(KeyStroke.getKeyStroke("control C"));
        item.addActionListener(this);
        menu.add(item = new JMenuItem(AddressBookResources.MENU_PASTE));
        item.setAccelerator(KeyStroke.getKeyStroke("control V"));
        item.addActionListener(this);
        menu.addSeparator();
        menu.add(item = new JMenuItem(AddressBookResources.MENU_SELECTALL));
        item.setAccelerator(KeyStroke.getKeyStroke("control A"));
        item.addActionListener(this);
        menu.addSeparator();
        menu.add(item = new JMenuItem(AddressBookResources.MENU_PROFILE));
        item.addActionListener(this);
        menu.addSeparator();
        menu.add(item = new JMenuItem(AddressBookResources.MENU_FINDPEOPLE));
        item.setAccelerator(KeyStroke.getKeyStroke("control F"));
        item.addActionListener(this);
        menubar.add(menu = new JMenu(AddressBookResources.MENU_VIEW));
        menu.add(m_toolBar = new JCheckBoxMenuItem(AddressBookResources.MENU_TOOLBAR));
        m_toolBar.addActionListener(this);
        m_toolBar.setSelected((view & TOOL) != 0);
        menu.add(m_statusBar = new JCheckBoxMenuItem(AddressBookResources.MENU_STATUSBAR));
        m_statusBar.addActionListener(this);
        m_statusBar.setSelected((view & STATUS) != 0);
        menu.add(m_folder = new JCheckBoxMenuItem(AddressBookResources.MENU_FOLDERGROUP));
        m_folder.addActionListener(this);
        m_folder.setSelected((view & FOLDER) != 0);
        menu.addSeparator();
        menu.add(menu2 = new JMenu(AddressBookResources.MENU_SORTBY));
        ButtonGroup bg = new ButtonGroup();
        menu2.add(item = new JRadioButtonMenuItemEx(new RadioAction(AddressBookResources.MENU_NAME)));
        bg.add(item);
        item.setSelected(BaseAttrTableModel.NAME_SORT == sortField);
        menu2.add(item = new JRadioButtonMenuItemEx(new RadioAction(AddressBookResources.MENU_EMAILADDR)));
        bg.add(item);
        item.setSelected(BaseAttrTableModel.E_MAIL_SORT == sortField);
        menu2.add(item = new JRadioButtonMenuItemEx(new RadioAction(AddressBookResources.MENU_PHONE)));
        bg.add(item);
        item.setSelected(BaseAttrTableModel.TPHONE_SORT == sortField);
        menu2.addSeparator();
        bg = new ButtonGroup();
        menu2.add(item = new JRadioButtonMenuItemEx(new RadioAction(AddressBookResources.MENU_FIRSTNAME)));
        bg.add(item);
        item.setSelected(BaseAttrTableModel.FIRSTNAME_SORT == subSortField);
        menu2.add(item = new JRadioButtonMenuItemEx(new RadioAction(AddressBookResources.MENU_LASTNAME)));
        bg.add(item);
        item.setSelected(BaseAttrTableModel.LASTNAME_SORT == subSortField);
        menu2.addSeparator();
        bg = new ButtonGroup();
        menu2.add(item = new JRadioButtonMenuItemEx(new RadioAction(AddressBookResources.MENU_ASC)));
        bg.add(item);
        item.setSelected(sortDir);
        menu2.add(item = new JRadioButtonMenuItemEx(new RadioAction(AddressBookResources.MENU_DESC)));
        bg.add(item);
        item.setSelected(sortDir);
        menu.addSeparator();
        menu.add(item = new JMenuItem(AddressBookResources.MENU_REFRESH));
        item.setAccelerator(KeyStroke.getKeyStroke("F5"));
        item.addActionListener(this);
        menubar.add(menu = new JMenu(AddressBookResources.MENU_TOOLS));
        menu.add(item = new JMenuItem(AddressBookResources.MENU_ACCOUNTS));
        item.addActionListener(this);
        menu.addSeparator();
        menu.add(item = new JMenuItem(AddressBookResources.MENU_OPTIONS));
        item.addActionListener(this);
        menu.addSeparator();
        menu.add(menu2 = new JMenu(AddressBookResources.MENU_ACTION));
        createActionMenu(menu2, this);
        menu.addSeparator();
        menu.add(item = new JMenuItem(AddressBookResources.MENU_SYNCHRONIZE));
        item.addActionListener(this);
        menubar.add(menu = new JMenu(AddressBookResources.MENU_HELP));
        menu.add(item = new JMenuItem(AddressBookResources.MENU_CONTENTS));
        item.setAccelerator(KeyStroke.getKeyStroke("F1"));
        item.addActionListener(this);
        menu.addSeparator();
        menu.add(item = new JMenuItem(AddressBookResources.MENU_ABOUT + AddressBookFrame.PROGRAMNAME));
        item.addActionListener(this);
        return menubar;
    }

    JComponent createNewMenu(JComponent wrapper, ActionListener listener) {
        JMenuItem item;
        wrapper.add(item = new JMenuItem(AddressBookResources.MENU_NEWCONTACT));
        item.setAccelerator(KeyStroke.getKeyStroke("control N"));
        item.addActionListener(listener);
        wrapper.add(item = new JMenuItem(AddressBookResources.MENU_NEWBOOKMARK));
        item.setAccelerator(KeyStroke.getKeyStroke("control B"));
        item.addActionListener(listener);
        wrapper.add(item = new JMenuItem(AddressBookResources.MENU_NEWCOOKIE));
        item.setAccelerator(KeyStroke.getKeyStroke("control O"));
        item.addActionListener(listener);
        wrapper.add(item = new JMenuItem(AddressBookResources.MENU_NEWGROUP));
        item.setAccelerator(KeyStroke.getKeyStroke("control G"));
        item.addActionListener(listener);
        wrapper.add(item = new JMenuItem(AddressBookResources.MENU_NEWFOLDER));
        item.setAccelerator(KeyStroke.getKeyStroke("control R"));
        item.addActionListener(listener);
        return wrapper;
    }

    JComponent createActionMenu(JComponent wrapper, ActionListener listener) {
        JMenuItem item;
        for (int i = 0; i < AddressBookResources.MENUS_ACTION.length; i++) {
            wrapper.add(item = new JMenuItem(AddressBookResources.MENUS_ACTION[i]));
            item.addActionListener(listener);
        }
        return wrapper;
    }

    JToolBar createToolBar(int orientation) {
        JToolBar toolbar = new JToolBar(orientation);
        JButton btn;
        btn = toolbar.add(new ToolAction(AddressBookResources.IMG_NEW, null, AddressBookResources.TTIP_NEW_ELEMENT));
        btn = toolbar.add(new ToolAction(AddressBookResources.IMG_PROPERTIES, AddressBookResources.MENU_PROPERTIES, AddressBookResources.TTIP_PROPERTY));
        btn = toolbar.add(new ToolAction(AddressBookResources.IMG_DELETE, AddressBookResources.MENU_DELETE));
        btn = toolbar.add(new ToolAction(AddressBookResources.IMG_FINDPEOPLE, AddressBookResources.MENU_FINDPEOPLE));
        btn = toolbar.add(new ToolAction(AddressBookResources.IMG_PRINT, AddressBookResources.MENU_PRINT));
        btn = toolbar.add(new ToolAction(AddressBookResources.IMG_ACTION));
        return toolbar;
    }

    JTable table;

    JTree tree;

    JComponent lastFocused;

    JComponent createBook() {
        JPanel abp = new JPanel();
        abp.setLayout(new BorderLayout());
        JPanel sp = new JPanel();
        sp.setLayout(new FlowLayout());
        sp.add(new JLabel(AddressBookResources.LABEL_TNAMEORLIST));
        JTextField tfSearch;
        sp.add(tfSearch = new JTextField(20));
        SearchPerformer searcher = new SearchPerformer();
        tfSearch.getDocument().addDocumentListener(searcher);
        tfSearch.addActionListener(searcher);
        abp.add(sp, "North");
        table = new JTable(null);
        table.addFocusListener(new FocusTracer());
        ComboCellRenderer cellEdRer;
        table.setDefaultRenderer(JComboBox.class, cellEdRer = new ComboCellRenderer());
        table.setDefaultEditor(JComboBox.class, cellEdRer);
        table.addMouseListener(new MouseInputAdapter() {

            public void mouseClicked(MouseEvent e) {
                if ((e.getModifiers() & InputEvent.BUTTON3_MASK) > 0) {
                } else if (e.getClickCount() == 2) {
                    actionPerformed(new ActionEvent(this, 0, AddressBookResources.MENU_PROPERTIES));
                }
            }
        });
        abp.add(new JScrollPane(table), "Center");
        if ((view & FOLDER) != 0) {
            tree = new JTree(createTreeModel(folders));
            tree.addFocusListener(new FocusTracer());
            tree.setRootVisible(false);
            tree.addTreeSelectionListener(new TreeSelectionListener() {

                public void valueChanged(TreeSelectionEvent e) {
                    try {
                        table.setModel(new BaseAttrTableModel(((Folder) getSelectedValueByName(folders, tree.getLastSelectedPathComponent().toString()))));
                    } catch (NullPointerException npe) {
                        table.setModel(new BaseAttrTableModel(new Folder("")));
                    }
                }
            });
            tree.setSelectionRow(0);
            return new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, new JScrollPane(tree), new JScrollPane(abp));
        } else {
            return abp;
        }
    }

    static Object getSelectedValueByName(List _elements, String _name) {
        for (int i = 0; i < _elements.size(); i++) if (_elements.get(i).toString().equals(_name)) return _elements.get(i);
        return null;
    }

    static TreeNode createTreeModel(List _elements) {
        return new FolderTreeNode(_elements);
    }

    public static class FolderTreeNode implements TreeNode {

        List<Folder> folders;

        FolderTreeNode parent;

        String name;

        FolderTreeNode(List _folders) {
            folders = _folders;
            name = PROGRAMNAME;
        }

        FolderTreeNode(Folder _folder, FolderTreeNode _parent) {
            folders = new ArrayList<Folder>();
            for (Object o : _folder.getContent()) {
                if (o instanceof Folder) folders.add((Folder) o);
            }
            name = _folder.getShortName();
            parent = _parent;
        }

        public TreeNode getChildAt(int arg0) {
            if (folders != null && folders.size() > 0 && arg0 >= 0 && arg0 < folders.size()) return new FolderTreeNode(folders.get(arg0), this);
            return null;
        }

        public int getChildCount() {
            return folders == null ? 0 : folders.size();
        }

        public TreeNode getParent() {
            return parent;
        }

        public int getIndex(TreeNode arg0) {
            for (int i = 0; i < folders.size(); i++) if (folders.get(i).getShortName().equals(arg0.toString())) return i;
            throw new IllegalArgumentException("No such node:" + arg0);
        }

        public boolean getAllowsChildren() {
            return true;
        }

        public boolean isLeaf() {
            return getChildCount() == 0;
        }

        public Enumeration children() {
            throw new RuntimeException();
        }

        public String toString() {
            return name;
        }
    }

    public static JPanel createButtonPanel(ActionListener al) {
        JButton btn;
        JPanel result = new JPanel();
        result.setLayout(new FlowLayout(FlowLayout.RIGHT));
        result.add(btn = new JButton(AddressBookResources.CMD_OK));
        btn.addActionListener(al);
        result.add(btn = new JButton(AddressBookResources.CMD_APPLY));
        btn.addActionListener(al);
        result.add(btn = new JButton(AddressBookResources.CMD_CANCEL));
        btn.addActionListener(al);
        return result;
    }

    JPanel createStatusBar() {
        JPanel p = new JPanel();
        p.add(statusLine = new JLabel(AddressBookResources.LABEL_STATUS_BAR, SwingConstants.RIGHT));
        p.setBorder(new BevelBorder(BevelBorder.LOWERED));
        return p;
    }

    public void actionPerformed(ActionEvent a) {
        String cmd = a.getActionCommand();
        if (cmd == null) return;
        if (cmd.equals(AddressBookResources.MENU_FINDPEOPLE)) {
        } else if (cmd.equals(AddressBookResources.MENU_NEWCONTACT)) {
            if (table.getModel() != null && table.getModel() instanceof BaseAttrTableModel) new ContactFrame(this); else JOptionPane.showMessageDialog(this, AddressBookResources.LABEL_NOFOLDER);
        } else if (cmd.equals(AddressBookResources.MENU_NEWFOLDER)) {
            String folderName = JOptionPane.showInputDialog(this, AddressBookResources.LABEL_FOLDER_NAME, AddressBookResources.TITLE_ENTER, JOptionPane.QUESTION_MESSAGE);
            if (folderName != null) {
                if (getSelectedValueByName(folders, folderName) == null) {
                    folders.add(new Folder<Contact>(folderName));
                    tree.setModel(new DefaultTreeModel(createTreeModel(folders), false));
                } else {
                    JOptionPane.showMessageDialog(this, AddressBookResources.LABEL_DUP_FLDR, AddressBookResources.TITLE_WARNING, JOptionPane.WARNING_MESSAGE);
                }
            }
        } else if (cmd.equals(AddressBookResources.MENU_PROPERTIES)) {
            if (table.hasFocus() || lastFocused == table) {
                int sel = table.getSelectedRow();
                if (sel > -1) new ContactFrame(this, sel);
            } else if (tree.hasFocus() || lastFocused == tree) {
                try {
                    Folder folder = (Folder) getSelectedValueByName(folders, tree.getLastSelectedPathComponent().toString());
                    if (folder != null) JOptionPane.showMessageDialog(this, "Properties of folder " + folder);
                } catch (Exception e) {
                }
            }
        } else if (cmd.equals(AddressBookResources.MENU_DELETE)) {
            if (table.hasFocus() || lastFocused == table) {
                int[] sel = table.getSelectedRows();
                if (sel != null && sel.length > 0) {
                    if (JOptionPane.showConfirmDialog(this, AddressBookResources.LABEL_CONF_DEL_CONT, AddressBookResources.TITLE_CONFIRM, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                        Contact[] contacts = new Contact[sel.length];
                        for (int i = 0; i < sel.length; i++) contacts[i] = ((BaseAttrTableModel) table.getModel()).getValueAt(sel[i]);
                        for (int i = 0; i < contacts.length; i++) ((BaseAttrTableModel) table.getModel()).removeValue(contacts[i]);
                        table.revalidate();
                    }
                }
            } else if (tree.hasFocus() || lastFocused == tree) {
                try {
                    if (JOptionPane.showConfirmDialog(this, AddressBookResources.LABEL_CONF_DEL_FLDR, AddressBookResources.TITLE_CONFIRM, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                        folders.remove(getSelectedValueByName(folders, tree.getLastSelectedPathComponent().toString()));
                        tree.setModel(new DefaultTreeModel(createTreeModel(folders), false));
                    }
                } catch (Exception e) {
                }
            }
        } else if (cmd.equals(AddressBookResources.MENU_OPTIONS)) {
            new OptionsFrame(this);
        } else if (cmd.equals(AddressBookResources.MENU_EXIT)) {
            save();
            dispose();
            System.exit(0);
        } else if (cmd.equals(AddressBookResources.MENU_SENDMAIL)) {
            Contact contact = getSelectedContact();
            if (contact != null) {
                List mails = contact.getEMails();
                if (mails != null) {
                    if (mails.size() == 1) sendMail(((EMail) mails.get(0)).getValue()); else if (mails.size() > 1) {
                        EMail[] mailsArr = new EMail[mails.size()];
                        mails.toArray(mailsArr);
                        mailsArr[0] = (EMail) JOptionPane.showInputDialog(this, AddressBookResources.LABEL_SELECT_EMAIL, AddressBookResources.TITLE_ENTER, JOptionPane.QUESTION_MESSAGE, null, mailsArr, mailsArr[0]);
                        if (mailsArr[0] != null) sendMail(mailsArr[0].getValue());
                    }
                }
            }
        } else if (cmd.equals(AddressBookResources.MENU_SHOWURL)) {
            Contact contact = getSelectedContact();
            if (contact != null) {
                List links = contact.getLinks();
                if (links != null) {
                    if (links.size() == 1) showBookmark(((Link) links.get(0)).getValue()); else if (links.size() > 1) {
                        Link[] linksArr = new Link[links.size()];
                        links.toArray(linksArr);
                        linksArr[0] = (Link) JOptionPane.showInputDialog(this, AddressBookResources.LABEL_SELECT_LINK, AddressBookResources.TITLE_ENTER, JOptionPane.QUESTION_MESSAGE, null, linksArr, linksArr[0]);
                        if (linksArr[0] != null) showBookmark(linksArr[0].getValue());
                    }
                }
            }
        } else if (cmd.equals(AddressBookResources.MENU_GETDIRECTIONS)) {
        } else if (cmd.equals(AddressBookResources.MENU_DIALTO)) {
            Contact contact = getSelectedContact();
            if (contact != null) {
                List phones = contact.getTelephones();
                if (phones != null) {
                    if (phones.size() == 1) dialPhone(((EMail) phones.get(0)).getValue()); else if (phones.size() > 1) {
                        Telephone[] phonesArr = new Telephone[phones.size()];
                        phones.toArray(phonesArr);
                        phonesArr[0] = (Telephone) JOptionPane.showInputDialog(this, AddressBookResources.LABEL_SELECT_PHONE, AddressBookResources.TITLE_ENTER, JOptionPane.QUESTION_MESSAGE, null, phonesArr, phonesArr[0]);
                        if (phonesArr[0] != null) dialPhone(phonesArr[0].getValue());
                    }
                }
            }
        } else if (cmd.equals(AddressBookResources.MENU_COPY)) {
        } else if (cmd.equals(AddressBookResources.MENU_PASTE)) {
        } else if (cmd.equals(AddressBookResources.MENU_SELECTALL)) {
            table.setRowSelectionInterval(0, table.getRowCount() - 1);
        } else if (cmd.equals(AddressBookResources.MENU_CHAT_TO)) {
        } else if (cmd.equals(AddressBookResources.MENU_CLOSE)) {
            save();
            dispose();
        } else if (cmd.equals(AddressBookResources.MENU_TOOLBAR)) {
            if (m_toolBar.isSelected()) view |= TOOL; else view &= ~TOOL;
        } else if (cmd.equals(AddressBookResources.MENU_STATUSBAR)) {
            if (m_statusBar.isSelected()) view |= STATUS; else view &= ~STATUS;
        } else if (cmd.equals(AddressBookResources.MENU_FOLDERGROUP)) {
            if (m_folder.isSelected()) view |= FOLDER; else view &= ~FOLDER;
        } else if (cmd.equals(AddressBookResources.MENU_NAME)) {
            sortField = BaseAttrTableModel.NAME_SORT;
            sortModel();
        } else if (cmd.equals(AddressBookResources.MENU_EMAILADDR)) {
            sortField = BaseAttrTableModel.E_MAIL_SORT;
            sortModel();
        } else if (cmd.equals(AddressBookResources.MENU_PHONE)) {
            sortField = BaseAttrTableModel.TPHONE_SORT;
            sortModel();
        } else if (cmd.equals(AddressBookResources.MENU_FIRSTNAME)) {
            subSortField = BaseAttrTableModel.FIRSTNAME_SORT;
            sortModel();
        } else if (cmd.equals(AddressBookResources.MENU_LASTNAME)) {
            subSortField = BaseAttrTableModel.LASTNAME_SORT;
            sortModel();
        } else if (cmd.equals(AddressBookResources.MENU_ASC)) {
            sortDir = true;
            sortModel();
        } else if (cmd.equals(AddressBookResources.MENU_DESC)) {
            sortDir = false;
            sortModel();
        } else if (cmd.equals(AddressBookResources.MENU_REFRESH)) {
            sortModel();
        } else if (cmd.equals(AddressBookResources.MENU_PROFILE)) {
            new ProfileFrame();
        } else if (cmd.indexOf(AddressBookResources.MENU_ABOUT) >= 0) {
            JOptionPane.showMessageDialog(this, "<html><i>" + PROGRAMNAME + "\n" + VERSION + '.' + BUILD + '\n' + "For " + DEDICATED + '\n' + COPYRIGHT + '\n' + "Java " + System.getProperty("java.version") + "JVM " + System.getProperty("java.vendor") + " OS " + System.getProperty("os.name") + ' ' + System.getProperty("os.version") + ' ' + System.getProperty("os.arch") + '\n' + Locale.getDefault().getDisplayName(), AddressBookResources.MENU_ABOUT, JOptionPane.PLAIN_MESSAGE, getResourceIcon(AddressBookResources.IMG_PROGRAM));
        } else if (cmd.equals(AddressBookResources.MENU_CONTENTS)) {
            controller.showUrl(AddressBookResources.URL_HELP);
        }
    }

    void sortModel() {
        if (table.getModel() instanceof BaseAttrTableModel) {
            BaseAttrTableModel bam = (BaseAttrTableModel) table.getModel();
            if (bam != null) {
                bam.sort(sortField, subSortField, sortDir);
                table.revalidate();
                table.repaint();
            }
        }
    }

    Contact getSelectedContact() {
        int sel = table.getSelectedRow();
        if (sel > -1) {
            return ((BaseAttrTableModel) table.getModel()).getValueAt(sel);
        }
        return null;
    }

    void sendMail(String _address) {
        ActionPerformer ap = null;
        try {
            ap = (ActionPerformer) controller.component(COMP_ACTIONPERFORMER);
        } catch (Exception e) {
        }
        if (ap != null) {
            ap.act(ActionPerformer.SENDMAIL, _address);
            setState(Frame.ICONIFIED);
        } else {
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            cb.setContents(new StringSelection(_address), null);
        }
    }

    void showBookmark(String _bookmark) {
        ActionPerformer ap = null;
        try {
            ap = (ActionPerformer) controller.component(COMP_ACTIONPERFORMER);
        } catch (Exception e) {
        }
        if (ap != null) {
            ap.act(ActionPerformer.SHOWBOOKMARK, _bookmark);
            setState(Frame.ICONIFIED);
        } else {
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            cb.setContents(new StringSelection(_bookmark), null);
            String lcbm = _bookmark.toLowerCase();
            if (lcbm.startsWith("http://") || lcbm.startsWith("ftp://")) controller.showUrl(_bookmark);
        }
    }

    void dialPhone(String _number) {
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        cb.setContents(new StringSelection(_number), null);
    }

    class SearchPerformer implements ActionListener, DocumentListener {

        public void actionPerformed(ActionEvent e) {
            try {
                if (doSearch(((JTextField) e.getSource()).getText())) table.requestFocus();
            } catch (Exception ex) {
            }
        }

        public void changedUpdate(DocumentEvent e) {
            try {
                doSearch(e.getDocument().getText(0, e.getDocument().getLength()));
            } catch (javax.swing.text.BadLocationException ble) {
            }
        }

        public void insertUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        public void removeUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        private boolean doSearch(String _text) {
            Object tm = table.getModel();
            if (tm != null && tm instanceof BaseAttrTableModel) {
                BaseAttrTableModel atm = (BaseAttrTableModel) tm;
                for (int row = 0; row < atm.getRowCount(); row++) {
                    Contact contact = atm.getValueAt(row);
                    if (sortField == BaseAttrTableModel.NAME_SORT) {
                        if (_text.regionMatches(true, 0, subSortField == BaseAttrTableModel.FIRSTNAME_SORT ? contact.getName().getFirst() : contact.getName().getLast(), 0, _text.length())) {
                            table.setRowSelectionInterval(row, row);
                            return true;
                        }
                    } else if (sortField == BaseAttrTableModel.E_MAIL_SORT || sortField == BaseAttrTableModel.TPHONE_SORT) {
                        List esa = sortField == BaseAttrTableModel.TPHONE_SORT ? contact.getTelephones() : contact.getEMails();
                        for (int j = 0; j < esa.size(); j++) if (_text.regionMatches(true, 0, esa.get(j).toString(), 0, _text.length())) {
                            table.setRowSelectionInterval(row, row);
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    class FocusTracer extends FocusAdapter {

        public void focusGained(FocusEvent e) {
            lastFocused = null;
        }

        public void focusLost(FocusEvent e) {
            lastFocused = (JComponent) e.getSource();
        }
    }

    static class ComboCellRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {

        Component cellEditor;

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cellRenderer = null;
            if (value != null) {
                if (value instanceof List) return new JComboBox(((List) value).toArray()); else if (value instanceof Vector) return new JComboBox((Vector) value);
                try {
                    cellRenderer = new JComboBox((String[]) table.getModel().getClass().getDeclaredMethod("getTypes", new Class[] {}).invoke(table.getModel(), new Object[] {}));
                    ((JComboBox) cellRenderer).setSelectedItem(value);
                } catch (Exception e) {
                }
            }
            return cellRenderer;
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            cellEditor = getTableCellRendererComponent(table, value, isSelected, true, row, column);
            return cellEditor;
        }

        public Object getCellEditorValue() {
            if (cellEditor != null && cellEditor instanceof JComboBox) return ((JComboBox) cellEditor).getSelectedItem();
            return null;
        }
    }

    class ToolAction extends AbstractAction {

        Icon im;

        ToolAction(String name) {
            this(name, null);
        }

        ToolAction(String name, String cmd) {
            this(name, cmd, null);
        }

        ToolAction(String name, String cmd, String _toolTip) {
            super(name);
            if (cmd != null) putValue(ACTION_COMMAND_KEY, cmd);
            if (_toolTip != null) putValue(SHORT_DESCRIPTION, _toolTip);
            im = getResourceIcon(name + AddressBookResources.EXT_GIF);
        }

        public Object getValue(String key) {
            if (key == SMALL_ICON) return im;
            return super.getValue(key);
        }

        public void actionPerformed(ActionEvent a) {
            if (AddressBookResources.IMG_NEW.equals(getValue(NAME))) {
                Rectangle r = ((Component) a.getSource()).getBounds();
                Point p = new Point(0, r.height);
                p = SwingUtilities.convertPoint((Component) a.getSource(), p, AddressBookFrame.this);
                ((JPopupMenu) createNewMenu(new JPopupMenu(), AddressBookFrame.this)).show(AddressBookFrame.this, p.x, p.y);
            } else if (AddressBookResources.IMG_ACTION.equals(getValue(NAME))) {
                Rectangle r = ((Component) a.getSource()).getBounds();
                Point p = new Point(0, r.height);
                p = SwingUtilities.convertPoint((Component) a.getSource(), p, AddressBookFrame.this);
                ((JPopupMenu) createActionMenu(new JPopupMenu(), AddressBookFrame.this)).show(AddressBookFrame.this, p.x, p.y);
            } else AddressBookFrame.this.actionPerformed(new ActionEvent(a.getSource(), a.getID(), (String) getValue(ACTION_COMMAND_KEY)));
        }
    }

    class RadioAction extends AbstractAction {

        RadioAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent a) {
            AddressBookFrame.this.actionPerformed(a);
        }
    }

    class JRadioButtonMenuItemEx extends JRadioButtonMenuItem {

        Action action;

        PropertyChangeListener actionPropertyChangeListener;

        JRadioButtonMenuItemEx(Action action) {
            setAction(action);
        }

        public Action getAction() {
            return action;
        }

        public void setAction(Action new_action) {
            Action prev_action = getAction();
            if (action == null || !action.equals(new_action)) {
                action = new_action;
                if (prev_action != null) {
                    removeActionListener(prev_action);
                    prev_action.removePropertyChangeListener(actionPropertyChangeListener);
                    actionPropertyChangeListener = null;
                }
                configurePropertiesFromAction(action);
                if (action != null) {
                    addActionListener(action);
                    actionPropertyChangeListener = createActionPropertyChangeListener(action);
                    action.addPropertyChangeListener(actionPropertyChangeListener);
                }
                firePropertyChange("action", prev_action, action);
                revalidate();
                repaint();
            }
        }

        protected void configurePropertiesFromAction(Action action) {
            setEnabled(action.isEnabled());
            setText((String) action.getValue(Action.NAME));
        }

        protected PropertyChangeListener createActionPropertyChangeListener(Action action) {
            return new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {
                }
            };
        }
    }

    public static void main(String[] args) {
        new AddressBookFrame(null);
    }
}
