package org.xanot.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * <p>
 * This class will greatly help programmers when creating REGEX expression. Its
 * like a simple tool called Regex Harnes Tool that was orriginaly published by
 * sun in its regex tutorial. This tool is much-much easier to use and more
 * sophisticated by far compared to the original Regex harnes tool.
 * </p>
 */
public class RegexEvaluator extends JFrame {

    private static final long serialVersionUID = -8016700083717648588L;

    private JTextField dfRegex = new JTextField();

    private JTextArea taSource = new JTextArea();

    private JEditorPane epTarget = new JEditorPane();

    private JScrollPane sourceScroll = new JScrollPane(taSource);

    private JScrollPane targetScroll = new JScrollPane(epTarget);

    private JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sourceScroll, targetScroll);

    /**
	 * Create new instance of RegexEvaluator
	 */
    public RegexEvaluator() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(dfRegex, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dfRegex.setText("(regex)|(goes)|(here)|[.]|(And)|(press)|(enter)");
        taSource.setText("String to parse goes here.\nQuick Brown Fox Jumps Over A Lazy Dogs");
        dfRegex.addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == 10) {
                    doRegex();
                }
            }

            public void keyPressed(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
            }
        });
        setTitle("Regex Evaluator");
        split.setPreferredSize(new Dimension(800, 600));
        split.setDividerLocation(300);
        epTarget.setContentType("text/html");
        epTarget.setEditable(false);
        dfRegex.setFont(dfRegex.getFont().deriveFont(Font.BOLD, 18f));
        this.setContentPane(panel);
        this.pack();
        int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        this.setLocation((screenWidth - this.getSize().width) / 2, (screenHeight - this.getSize().height) / 2);
    }

    /**
	 * This method will take the regex and source text and use them during regex
	 * operation. The result will be displayed directy at the bottom most editor
	 * pane in an easy to understand way.
	 */
    public void doRegex() {
        if (dfRegex.getText().trim().length() == 0 || taSource.getText().trim().length() == 0) {
            epTarget.setText("<h2>ERROR</h2>Please specify the regex and text to parse.");
            return;
        }
        try {
            Pattern pattern = Pattern.compile(dfRegex.getText());
            Matcher matcher = pattern.matcher(taSource.getText());
            boolean found = false;
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                found = true;
                String source = taSource.getText();
                String first = source.substring(0, matcher.start());
                String last = source.substring(matcher.end(), source.length());
                String match = matcher.group();
                String res = escape(first) + "<font color=\"#FF0000\"><b><u>" + escape(match) + "</u></b></font>" + escape(last);
                sb.append("<code><pre bgcolor=\"#E0E0E0\">" + res + "</pre></code>");
            }
            if (!found) {
                epTarget.setText("<h2>RESULT</h2>Regex not found !!!!. Please refine your regex.");
            } else {
                epTarget.setText("<h2>RESULT</h2>" + sb.toString());
            }
        } catch (PatternSyntaxException pse) {
            epTarget.setText("<h2>INVALID REGEX</h2>" + pse.getMessage());
        }
    }

    private String escape(String s) {
        return s.replaceAll(">", "&gt;").replaceAll("<", "&lt;").replaceAll("\"", "quot;");
    }

    /**
	 * The startup hook of Regex Evaluator.
	 * 
	 * @param arg
	 *            String arguments. This arguments will not be used.
	 */
    public static void main(String[] arg) {
        RegexEvaluator frame = new RegexEvaluator();
        frame.setVisible(true);
    }
}
