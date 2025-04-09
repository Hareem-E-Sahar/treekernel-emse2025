package org.rjam.gui.base;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

/**
 *
 * @author  Tony Bringardner
 */
public class LoggerPanel extends javax.swing.JFrame {

    private static final long serialVersionUID = 1L;

    private static LoggerPanel logger = new LoggerPanel();

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd:HH:mm.sss");

    private static final String PROP_LEVEL = "Level";

    public static int defaultLevel = Logger.LEVEL_INFO;

    static {
        String tmp = System.getProperty(PROP_LEVEL);
        if (tmp != null) {
            defaultLevel = getLevel(tmp);
        }
    }

    public static int getLevel(String level) {
        int ret = -1;
        for (int idx = 0; ret < 0 && idx < Logger.LEVEL_NAMES.length; idx++) {
            if (level.equalsIgnoreCase(Logger.LEVEL_NAMES[idx])) {
                ret = idx;
            }
        }
        if (ret < 0) {
            ret = Logger.LEVEL_NONE;
        }
        return ret;
    }

    public static String getLevelName(int level) {
        if (level < 0 || level > Logger.LEVEL_ALL) {
            level = Logger.LEVEL_NONE;
        }
        return Logger.LEVEL_NAMES[level];
    }

    public static SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    public static void setDateFormat(SimpleDateFormat dateFormat) {
        LoggerPanel.dateFormat = dateFormat;
    }

    public static int getDefaultLevel() {
        return defaultLevel;
    }

    public static void setDefaultLevel(int defaultLevel) {
        LoggerPanel.defaultLevel = defaultLevel;
    }

    public static LoggerPanel getLogger() {
        if (logger == null) {
            synchronized (LoggerPanel.class) {
                if (logger == null) {
                    logger = new LoggerPanel();
                    logger.setVisible(false);
                }
            }
        }
        return logger;
    }

    Highlighter.HighlightPainter myHighlightPainter = new MyHighlightPainter(Color.red);

    class MyHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {

        public MyHighlightPainter(Color color) {
            super(color);
        }
    }

    @SuppressWarnings("unused")
    private void search() {
        JTextArea jTextArea1 = this.text;
        removeHighlights(jTextArea1);
        String regExp = jTextArea1.getSelectedText();
        if (regExp == null || regExp.length() == 0) {
        }
        if (regExp != null && regExp.length() > 0) {
            Pattern pattern = Pattern.compile(regExp);
            String text = jTextArea1.getText();
            Matcher m = pattern.matcher(text);
            Highlighter hlter = jTextArea1.getHighlighter();
            while (m.find()) {
                int start = m.start();
                int end = m.end();
                try {
                    hlter.addHighlight(start, end, myHighlightPainter);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /** Creates new form LoggerPanel */
    public LoggerPanel() {
        initComponents();
        setIconImage(new javax.swing.ImageIcon(getClass().getResource(Constants.IMAGE_TITLE)).getImage());
        setTitle("Application Monitor Log");
    }

    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        text = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        clearButton = new javax.swing.JButton();
        scrolLock = new javax.swing.JCheckBox();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        text.setColumns(20);
        text.setRows(5);
        jScrollPane1.setViewportView(text);
        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        clearButton.setText("Clear");
        clearButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });
        jPanel1.add(clearButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 0, -1, -1));
        scrolLock.setText("Scroll Lock");
        scrolLock.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scrolLock.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel1.add(scrolLock, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 10, 80, -1));
        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);
        pack();
    }

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {
        text.setText("");
    }

    /**
	 * @param args the command line arguments
	 */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new LoggerPanel().setVisible(true);
            }
        });
    }

    public void removeHighlights(JTextComponent textComp) {
        Highlighter hilite = textComp.getHighlighter();
        Highlighter.Highlight[] hilites = hilite.getHighlights();
        for (int i = 0; i < hilites.length; i++) {
            if (hilites[i].getPainter() instanceof MyHighlightPainter) {
                hilite.removeHighlight(hilites[i]);
            }
        }
    }

    private javax.swing.JButton clearButton;

    private javax.swing.JPanel jPanel1;

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JCheckBox scrolLock;

    private javax.swing.JTextArea text;

    private void addMessage(String msg) {
        int loc = text.getCaretPosition();
        text.append(msg + "\n");
        boolean locked = scrolLock.isSelected();
        if (locked) {
            text.setCaretPosition(loc);
        } else {
            text.setCaretPosition(text.getDocument().getLength());
        }
    }

    public void println(String string) {
        addMessage(string);
    }
}
