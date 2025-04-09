import java.awt.Rectangle;
import java.awt.ComponentOrientation;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.RenderingHints;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.KeyAdapter;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemListener;
import java.util.Locale;
import java.util.Properties;
import java.util.ArrayDeque;
import javax.swing.JPopupMenu;
import javax.swing.border.LineBorder;
import javax.swing.SwingUtilities;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.SwingWorker;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.JPanel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Calendar;
import javax.swing.text.AttributeSet;
import javax.swing.text.JTextComponent;
import org.xml.sax.SAXException;
import java.util.Collections;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.PlainDocument;

public class Cham3a extends JFrame {

    private JButton btnFindWord = null, btnPrevious = null;

    private JTextField txtWord = null;

    private JTextArea txtDefinition = null;

    private JLabel lblNbWords = null;

    private JList jListWords = null;

    private Locale currentLocale = null;

    private int x, y, w, h;

    private GradientBox bxMain = null;

    private JPanel pnTop = null, pnCenter = null;

    private Box statusBar = null;

    private JScrollPane scrollListWords = null, scrollTextDefinition = null;

    private Properties appProperties = null;

    private JMenuBar menuBar = null;

    private JMenu mnSearch = null;

    private JRadioButtonMenuItem itmBegin = null, itmContains = null, itmEnd = null, itmWhole = null;

    private JMenuItem itmFonts = null;

    private Cham3aGlassPane glassPane = null;

    private Font appFont = null, fontMenu = null, fontWord = null, fontListWords = null, fontDefinition = null;

    private Features feature = Features.QAMOUS;

    private JSplitPane splitPane = null;

    private ArrayDeque<SearchedWord> historiqueWord;

    private final int SIZE_HISTORIQUE = 20;

    private int nbWordHistorique = 0;

    private SearchedWord currentSearch;

    private int actualWidth, actualHeight;

    private JPopupMenu popupWordField;

    private Highlighter highlighter = null;

    private Highlighter.HighlightPainter highlightPainter = null;

    private Searchs searchType;

    private WordsList wordsList;

    private DefinitionFinder defFinder;

    private FindWordInQuran findInQuran;

    private JTabbedPane tabbedPane;

    private JScrollPane scrollListAyat;

    private JList jListAyat;

    private static final Calendar today = Calendar.getInstance();

    private static final String strLogToday = String.valueOf(today.get(Calendar.YEAR)) + String.valueOf(today.get(Calendar.MONTH)) + String.valueOf(today.get(Calendar.DATE));

    private static final Logger cham3aLogger = Logger.getLogger("cham3a" + strLogToday);

    ;

    enum Features {

        QAMOUS, QURAN
    }

    public Cham3a() {
        super("الشمعة");
        File dirProp = new File(System.getProperty("user.home") + File.separator + ".cham3a");
        dirProp.mkdir();
        try {
            FileHandler fileHandler = new FileHandler(System.getProperty("user.home") + File.separator + ".cham3a" + File.separator + "cham3a" + strLogToday + ".log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            cham3aLogger.addHandler(fileHandler);
            cham3aLogger.setUseParentHandlers(false);
            appFont = Font.createFont(Font.TRUETYPE_FONT, this.getClass().getResourceAsStream("fonts/UthmanTN1B_Ver07.otf"));
        } catch (FontFormatException e) {
            cham3aLogger.log(Level.WARNING, e.getMessage(), e);
        } catch (IOException e) {
            cham3aLogger.log(Level.WARNING, e.getMessage(), e);
        }
        fontMenu = appFont.deriveFont(15f);
        fontWord = appFont.deriveFont(17f);
        fontDefinition = appFont.deriveFont(19f);
        fontListWords = appFont.deriveFont(17f);
        try {
            defFinder = new DefinitionFinder();
            findInQuran = new FindWordInQuran();
        } catch (SAXException e) {
            cham3aLogger.log(Level.WARNING, e.getMessage(), e);
        }
        historiqueWord = new ArrayDeque<SearchedWord>(SIZE_HISTORIQUE);
        new SwingWorker<Void, Void>() {

            @Override
            public Void doInBackground() {
                wordsList = new WordsList();
                return null;
            }
        }.execute();
        loadPorperties();
        restoreFrameProperties();
        actualWidth = w;
        actualHeight = h;
        currentLocale = new Locale("ar");
        searchType = Searchs.BEGIN;
        buildComponents();
        getRootPane().setDefaultButton(btnFindWord);
        new SwingWorker<Void, Void>() {

            @Override
            public Void doInBackground() {
                glassPane = new Cham3aGlassPane();
                setGlassPane(glassPane);
                return null;
            }
        }.execute();
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                quit();
            }

            @Override
            public void windowActivated(WindowEvent e) {
                getWordFromClipbrd();
            }
        });
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                actualWidth = e.getComponent().getWidth();
                actualHeight = e.getComponent().getHeight();
                glassPane.changeProgressLocation();
                if (!txtDefinition.getText().isEmpty()) {
                    highlighter.removeAllHighlights();
                    WordsListElement listElement = (WordsListElement) jListWords.getSelectedValue();
                    try {
                        highlightWord(txtDefinition.getText(), listElement.getWord());
                    } catch (BadLocationException ex) {
                        cham3aLogger.log(Level.WARNING, ex.getMessage(), ex);
                    }
                }
            }
        });
        setLocale(currentLocale);
        setBounds(x, y, w, h);
        setIconImage(new ImageIcon(this.getClass().getResource("img/candle32.png")).getImage());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        getRootPane().setBorder(new LineBorder(Color.BLACK));
        setJMenuBar(menuBar);
        applyComponentOrientation(ComponentOrientation.getOrientation(currentLocale));
        setVisible(true);
        splitPane.setDividerLocation(0.8);
        txtWord.requestFocusInWindow();
    }

    private void getWordFromClipbrd() {
        Clipboard clipBrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable t = clipBrd.getContents(null);
        if (t != null) {
            if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                try {
                    String clipContent = t.getTransferData(DataFlavor.stringFlavor).toString();
                    if (txtWord.getText().isEmpty() && clipContent != null && clipContent.matches("\\p{InArabic}+") && clipContent.length() < 10) {
                        txtWord.setText(clipContent);
                    }
                } catch (UnsupportedFlavorException e) {
                    cham3aLogger.log(Level.WARNING, e.getMessage(), e);
                } catch (IOException e) {
                    cham3aLogger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        }
    }

    private void quit() {
        appProperties.setProperty("widthFrame", String.valueOf(getWidth()));
        appProperties.setProperty("heightFrame", String.valueOf(getHeight()));
        appProperties.setProperty("xFrame", String.valueOf(getX()));
        appProperties.setProperty("yFrame", String.valueOf(getY()));
        saveProperties();
        System.exit(0);
    }

    private void restoreFrameProperties() {
        if (appProperties.getProperty("widthFrame") != null && !appProperties.getProperty("widthFrame").isEmpty() && appProperties.getProperty("heightFrame") != null && !appProperties.getProperty("heightFrame").isEmpty() && appProperties.getProperty("xFrame") != null && !appProperties.getProperty("xFrame").isEmpty() && appProperties.getProperty("yFrame") != null && !appProperties.getProperty("yFrame").isEmpty()) {
            x = Integer.parseInt(appProperties.getProperty("xFrame"));
            y = Integer.parseInt(appProperties.getProperty("yFrame"));
            w = Integer.parseInt(appProperties.getProperty("widthFrame"));
            h = Integer.parseInt(appProperties.getProperty("heightFrame"));
        } else {
            w = 800;
            h = 700;
            x = ((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() - w) / 2;
            y = ((int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() - h) / 2;
        }
    }

    private void loadPorperties() {
        appProperties = new Properties();
        File propExistant = new File(System.getProperty("user.home") + File.separator + ".cham3a" + File.separator + "dico.properties");
        try {
            if (propExistant.exists() && propExistant.length() > 0) {
                appProperties.load(new BufferedReader(new FileReader(propExistant)));
            } else {
                InputStream dicoInput = this.getClass().getResourceAsStream("dico.properties");
                appProperties.load(dicoInput);
            }
        } catch (FileNotFoundException e) {
            cham3aLogger.log(Level.WARNING, e.getMessage(), e);
        } catch (IOException e) {
            cham3aLogger.log(Level.WARNING, e.getMessage(), e);
        } catch (Exception e) {
            cham3aLogger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    private void saveProperties() {
        File dirProp = new File(System.getProperty("user.home") + File.separator + ".cham3a");
        try {
            PrintWriter propWriter = null;
            if (dirProp.exists()) {
                propWriter = new PrintWriter(new BufferedWriter(new FileWriter(dirProp.getPath() + File.separator + "dico.properties")));
                appProperties.store(propWriter, "");
            } else {
                if (!dirProp.mkdir()) {
                    JOptionPane.showMessageDialog(this, "لا يمكن حفظ التغييرات", "الشمعة", JOptionPane.ERROR_MESSAGE);
                } else {
                    propWriter = new PrintWriter(new BufferedWriter(new FileWriter(dirProp.getPath() + File.separator + "dico.properties")));
                    appProperties.store(propWriter, "");
                }
            }
        } catch (IOException ie) {
            JOptionPane.showMessageDialog(null, ie.getMessage(), "الشمعة", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateWordsList(String word, Searchs s) {
        txtDefinition.setText("");
        txtDefinition.getHighlighter().removeAllHighlights();
        List<WordsListElement> list = wordsList.getList(word, s);
        if (list.size() > 0) {
            Collections.sort(list);
            lblNbWords.setText("عدد الكلمات : " + list.size());
        } else {
            lblNbWords.setText("لا توجد أي " + "كلمة " + "مطابقة");
        }
        jListWords.setListData(list.toArray());
        jListWords.requestFocusInWindow();
    }

    private void populateAyatList(String word) {
        try {
            jListAyat.setListData(findInQuran.find(word).toArray());
            jListAyat.scrollRectToVisible(new Rectangle(jListAyat.getBounds().width, 0, scrollListAyat.getBounds().width, scrollListAyat.getBounds().height));
        } catch (IOException e) {
            cham3aLogger.log(Level.WARNING, e.getMessage(), e);
        } catch (SAXException e) {
            cham3aLogger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    private void buildComponents() {
        SpringLayout springlayoutTop = new SpringLayout();
        popupWordField = new JPopupMenu();
        popupWordField.setLocale(currentLocale);
        JMenuItem itmClear = new JMenuItem("مسح", new ImageIcon("img/clear.png"));
        itmClear.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                txtWord.setText("");
                popupWordField.setVisible(false);
            }
        });
        JMenuItem itmPaste = new JMenuItem("لصق", new ImageIcon("img/paste.png"));
        itmPaste.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                txtWord.paste();
                popupWordField.setVisible(false);
            }
        });
        popupWordField.add(itmClear);
        popupWordField.add(itmPaste);
        popupWordField.applyComponentOrientation(ComponentOrientation.getOrientation(currentLocale));
        pnTop = new JPanel(springlayoutTop);
        pnTop.setPreferredSize(new Dimension(w, 45));
        pnTop.setMinimumSize(new Dimension(w, 45));
        pnTop.setOpaque(false);
        txtWord = new JTextField();
        txtWord.setFont(fontWord);
        txtWord.setSelectionColor(new Color(0x3070a0));
        txtWord.setForeground(new Color(0x666666));
        txtWord.setBackground(new Color(0xd0d8e0));
        txtWord.setDocument(new PlainDocument() {

            @Override
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                if (str.matches("\\p{InArabic}+")) {
                    super.insertString(offs, str, a);
                }
            }
        });
        txtWord.setPreferredSize(new Dimension(330, 35));
        txtWord.setComponentPopupMenu(popupWordField);
        btnFindWord = new JButton();
        btnFindWord.setIcon(new ImageIcon(this.getClass().getResource("img/find.png")));
        btnFindWord.setPreferredSize(new Dimension(40, 35));
        btnFindWord.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                if (feature == Features.QAMOUS) {
                    search(searchType);
                } else if (feature == Features.QURAN) {
                    searchQuran();
                }
            }
        });
        btnPrevious = new JButton();
        btnPrevious.setIcon(new ImageIcon(this.getClass().getResource("img/undo.png")));
        btnPrevious.setPreferredSize(new Dimension(35, 35));
        btnPrevious.setToolTipText("الكلمة السابقة");
        btnPrevious.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                previousWord();
            }
        });
        pnTop.add(btnPrevious);
        pnTop.add(txtWord);
        pnTop.add(btnFindWord);
        springlayoutTop.putConstraint(SpringLayout.NORTH, btnPrevious, 5, SpringLayout.NORTH, pnTop);
        springlayoutTop.putConstraint(SpringLayout.WEST, btnPrevious, 10, SpringLayout.WEST, pnTop);
        springlayoutTop.putConstraint(SpringLayout.NORTH, txtWord, 5, SpringLayout.NORTH, pnTop);
        springlayoutTop.putConstraint(SpringLayout.WEST, txtWord, 5, SpringLayout.EAST, btnPrevious);
        springlayoutTop.putConstraint(SpringLayout.WEST, btnFindWord, 5, SpringLayout.EAST, txtWord);
        springlayoutTop.putConstraint(SpringLayout.NORTH, btnFindWord, 5, SpringLayout.NORTH, pnTop);
        springlayoutTop.putConstraint(SpringLayout.EAST, pnTop, 10, SpringLayout.EAST, btnFindWord);
        SpringLayout springLayoutCenter = new SpringLayout();
        pnCenter = new JPanel(springLayoutCenter);
        pnCenter.setPreferredSize(new Dimension(w, h - 40));
        pnCenter.setPreferredSize(new Dimension(1200, 1000));
        pnCenter.setOpaque(false);
        jListWords = new Cham3aList(new Color(0xd0d8e0), new Color(217, 223, 230), Color.LIGHT_GRAY, Color.RED);
        jListWords.addListSelectionListener(new WordSelectedListener());
        jListWords.setFont(fontWord);
        jListWords.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jListWords.setSelectionBackground(new Color(0x3070a0));
        jListWords.setForeground(new Color(0x666666));
        jListWords.setBackground(new Color(0xd0d8e0));
        jListWords.setFixedCellHeight(25);
        scrollListWords = new JScrollPane(jListWords);
        scrollListWords.setPreferredSize(new Dimension(120, 170));
        scrollListWords.setMaximumSize(new Dimension(120, 1000));
        txtDefinition = new JTextArea();
        txtDefinition.setEditable(false);
        txtDefinition.addCaretListener(new TextSelectedListener());
        txtDefinition.setSelectionColor(new Color(0x3070a0));
        txtDefinition.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        txtDefinition.setMargin(new Insets(10, 10, 10, 10));
        txtDefinition.setForeground(new Color(0x666666));
        txtDefinition.setBackground(new Color(0xd0d8e0));
        txtDefinition.setLineWrap(true);
        txtDefinition.setTabSize(10);
        txtDefinition.setFont(fontDefinition);
        scrollTextDefinition = new JScrollPane(txtDefinition);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollTextDefinition, scrollListWords);
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(1);
        jListAyat = new Cham3aList(new Color(0xd0d8e0), new Color(217, 223, 230), Color.LIGHT_GRAY);
        jListAyat.setFont(fontWord);
        jListAyat.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jListAyat.setSelectionBackground(new Color(0x3070a0));
        jListAyat.setForeground(new Color(0x666666));
        jListAyat.setBackground(new Color(0xd0d8e0));
        jListAyat.setFixedCellHeight(40);
        scrollListAyat = new JScrollPane(jListAyat);
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("المعجم", null, splitPane, "البحث في المعجم");
        tabbedPane.addTab("القرآن", null, scrollListAyat, "البحث في القرآن الكريم");
        tabbedPane.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                if (tabbedPane.getSelectedIndex() == 0) {
                    feature = Features.QAMOUS;
                    search(searchType);
                } else if (tabbedPane.getSelectedIndex() == 1) {
                    feature = Features.QURAN;
                    searchQuran();
                }
            }
        });
        pnCenter.add(tabbedPane);
        springLayoutCenter.putConstraint(SpringLayout.WEST, tabbedPane, 10, SpringLayout.WEST, pnCenter);
        springLayoutCenter.putConstraint(SpringLayout.EAST, pnCenter, 10, SpringLayout.EAST, tabbedPane);
        springLayoutCenter.putConstraint(SpringLayout.SOUTH, pnCenter, 0, SpringLayout.SOUTH, tabbedPane);
        springLayoutCenter.putConstraint(SpringLayout.NORTH, tabbedPane, 10, SpringLayout.NORTH, pnCenter);
        statusBar = new Box(BoxLayout.LINE_AXIS);
        lblNbWords = new JLabel("");
        lblNbWords.setPreferredSize(new Dimension(100, 20));
        statusBar.add(Box.createHorizontalStrut(20));
        statusBar.add(lblNbWords);
        statusBar.add(Box.createHorizontalGlue());
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.DARK_GRAY));
        bxMain = new GradientBox(BoxLayout.PAGE_AXIS, new Color(204, 211, 224), new Color(233, 236, 242), GradientBox.VER);
        bxMain.add(pnTop);
        bxMain.add(pnCenter);
        bxMain.add(statusBar);
        bxMain.add(Box.createVerticalGlue());
        setContentPane(bxMain);
        highlighter = new DefaultHighlighter();
        highlightPainter = new Cham3aHighlightPainter(new Color(0xECEBA3));
        txtDefinition.setHighlighter(highlighter);
        ButtonGroup buttonGroupSearch = new ButtonGroup();
        itmBegin = new JRadioButtonMenuItem("تبتدأ ب...");
        itmBegin.setFont(fontMenu);
        itmBegin.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                searchType = Searchs.BEGIN;
            }
        });
        itmBegin.setSelected(true);
        itmContains = new JRadioButtonMenuItem("تحتوي على...");
        itmContains.setFont(fontMenu);
        itmContains.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                searchType = Searchs.CONTAINS;
            }
        });
        itmEnd = new JRadioButtonMenuItem("تنتهي ب...");
        itmEnd.setFont(fontMenu);
        itmEnd.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                searchType = Searchs.END;
            }
        });
        itmWhole = new JRadioButtonMenuItem("الكلمة كاملة");
        itmWhole.setFont(fontMenu);
        itmWhole.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                searchType = Searchs.WHOLE;
            }
        });
        buttonGroupSearch.add(itmBegin);
        buttonGroupSearch.add(itmWhole);
        buttonGroupSearch.add(itmEnd);
        buttonGroupSearch.add(itmContains);
        mnSearch = new JMenu("نوع البحث");
        mnSearch.setFont(fontMenu);
        mnSearch.add(itmBegin);
        mnSearch.add(itmContains);
        mnSearch.add(itmEnd);
        mnSearch.add(itmWhole);
        menuBar = new JMenuBar();
        menuBar.add(mnSearch);
    }

    private String getDefinition(WordsListElement word) throws SAXException, IOException {
        String definition = defFinder.findDefinition(word);
        definition = definition.replaceAll("\\[sAya\\]", "﴿");
        definition = definition.replaceAll("\\[eAya\\]", "﴾");
        definition = definition.replaceAll("صلى الله عليه وسلم", "ﷺ");
        return definition;
    }

    private void highlightWord(String def, String word) throws BadLocationException {
        Pattern ptr = Pattern.compile(word);
        Matcher matcher = ptr.matcher(def);
        while (matcher.find()) {
            highlighter.addHighlight(matcher.start(), matcher.end(), highlightPainter);
        }
    }

    private class WordSelectedListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent le) {
            if (jListWords.getSelectedValue() != null) {
                highlighter.removeAllHighlights();
                WordsListElement listElement = (WordsListElement) jListWords.getSelectedValue();
                txtWord.setText(listElement.getWord());
                try {
                    String def = getDefinition(listElement);
                    txtDefinition.setText(def);
                    highlightWord(def, listElement.getWord());
                    txtDefinition.setCaretPosition(0);
                    txtWord.setText(listElement.getWord());
                    if (!listElement.getWord().equals(currentSearch.getWord())) {
                        currentSearch = new SearchedWord(Searchs.WHOLE, listElement.getWord());
                        if (nbWordHistorique < SIZE_HISTORIQUE) {
                            historiqueWord.push(currentSearch);
                            nbWordHistorique++;
                        } else {
                            historiqueWord.removeLast();
                            historiqueWord.push(currentSearch);
                        }
                    }
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class TextSelectedListener implements CaretListener {

        @Override
        public void caretUpdate(CaretEvent ce) {
            if (ce.getDot() != ce.getMark()) {
                String text = txtDefinition.getSelectedText();
                text = text.replaceAll("[ً-ْ]", "");
                txtWord.setText(text);
                search(Searchs.BEGIN, text);
            }
        }
    }

    private void search(final Searchs s, final String word, boolean incrementHistoCompteur) {
        if (!word.isEmpty()) {
            if (incrementHistoCompteur) {
                currentSearch = new SearchedWord(s, word);
                if (nbWordHistorique < SIZE_HISTORIQUE) {
                    historiqueWord.push(currentSearch);
                    nbWordHistorique++;
                } else {
                    historiqueWord.removeLast();
                    historiqueWord.push(currentSearch);
                }
            }
            new SwingWorker<Void, Void>() {

                @Override
                public Void doInBackground() {
                    try {
                        glassPane.startWaiting();
                        populateWordsList(word, s);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        glassPane.stopWaiting();
                    }
                    return null;
                }
            }.execute();
        } else {
            JOptionPane.showMessageDialog(null, "المرجو إدخال كلمة", "الشمعة", JOptionPane.ERROR_MESSAGE);
            txtWord.requestFocusInWindow();
        }
    }

    private void search(Searchs s, String word) {
        if (!txtWord.getText().isEmpty()) {
            search(s, word, true);
        }
    }

    private void search(Searchs s) {
        if (!txtWord.getText().isEmpty()) {
            search(s, txtWord.getText());
        }
    }

    private void searchQuran() {
        if (!txtWord.getText().isEmpty()) {
            populateAyatList(txtWord.getText());
        }
    }

    private void previousWord() {
        nbWordHistorique--;
        if (nbWordHistorique > 0) {
            if (currentSearch.equals(historiqueWord.getFirst())) {
                historiqueWord.removeFirst();
                nbWordHistorique--;
            }
            SearchedWord previousSearch = historiqueWord.pop();
            if (!previousSearch.getWord().isEmpty() && previousSearch.getWord() != null) {
                search(previousSearch.getSearch(), previousSearch.getWord(), false);
                txtWord.setText(previousSearch.getWord());
                jListWords.setSelectedIndex(0);
            }
        }
    }

    private class Cham3aHighlightPainter implements Highlighter.HighlightPainter {

        Color color;

        Cham3aHighlightPainter(Color c) {
            color = c;
        }

        @Override
        public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            try {
                Rectangle r0 = c.modelToView(offs0);
                Rectangle r1 = c.modelToView(offs1);
                Rectangle rHighlight = r0.union(r1);
                g2.setColor(color);
                g2.fillRoundRect(rHighlight.x, rHighlight.y, rHighlight.width, rHighlight.height - 3, 8, 8);
                g2.setColor(Color.LIGHT_GRAY);
                g2.drawRoundRect(rHighlight.x, rHighlight.y, rHighlight.width, rHighlight.height - 3, 8, 8);
            } catch (BadLocationException be) {
                be.printStackTrace();
            }
        }
    }

    public class Cham3aGlassPane extends JComponent {

        private JProgressBar progress;

        private int w = 120, h = 15, xProgress = 0, yProgress = 0;

        private boolean waiting = false;

        public Cham3aGlassPane() {
            super();
            progress = new JProgressBar();
            xProgress = ((actualWidth - w) / 2);
            yProgress = ((actualHeight - h) / 2);
            progress.setBounds(xProgress, yProgress, w, h);
            add(progress);
            progress.setIndeterminate(true);
            setLayout(null);
            addMouseListener(new MouseAdapter() {
            });
            addMouseMotionListener(new MouseMotionAdapter() {
            });
            addKeyListener(new KeyAdapter() {
            });
            addComponentListener(new ComponentAdapter() {

                @Override
                public void componentShown(ComponentEvent evt) {
                    requestFocusInWindow();
                }
            });
            setFocusTraversalKeysEnabled(false);
        }

        private void changeProgressLocation() {
            xProgress = (actualWidth - w) / 2;
            yProgress = (actualHeight - h) / 2;
            progress.setBounds(xProgress, yProgress, w, h);
        }

        public void startWaiting() {
            waiting = true;
            progress.setVisible(waiting);
            setVisible(true);
            revalidate();
        }

        public void stopWaiting() {
            waiting = false;
            progress.setVisible(waiting);
            setVisible(false);
        }

        @Override
        public void paintComponent(Graphics g) {
            if (waiting) {
                Rectangle clip = g.getClipBounds();
                Graphics2D g2 = (Graphics2D) g;
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
                g2.setColor(Cham3a.this.getBackground());
                g2.fillRect(clip.x, clip.y, clip.width, clip.height);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                for (LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
                    if (laf.getName().equalsIgnoreCase("Nimbus")) {
                        try {
                            UIManager.setLookAndFeel(laf.getClassName());
                            break;
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                UIManager.put("OptionPane.okButtonText", "موافق");
                UIManager.put("OptionPane.cancelButtonText", "إلغاء");
                new Cham3a();
            }
        });
    }
}
