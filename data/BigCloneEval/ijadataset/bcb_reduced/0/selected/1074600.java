package org.webcastellum.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;

public final class RegularExpressionMatchTester extends JFrame {

    private final JTextField textField = new JTextField("Enter regular expression here");

    private final JTextArea textArea = new JTextArea("Enter test content here");

    private final JScrollPane scrollPane = new JScrollPane(this.textArea);

    private final JPanel panel = new JPanel(new BorderLayout());

    private final JPanel subPanel = new JPanel(new BorderLayout());

    private final JPanel subSubPanel = new JPanel();

    private final JLabel label = new JLabel("Please enter some text");

    private final JToggleButton toggle = new JToggleButton("Wrap");

    public RegularExpressionMatchTester() {
        init();
    }

    private void init() {
        setPreferredSize(new Dimension(600, 350));
        setTitle("Regular Expression Match Tester");
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final int left = (int) screenSize.getWidth() / 2 - 300;
        final int right = (int) screenSize.getHeight() / 2 - 300;
        if (left > 0 && right > 0) {
            setLocation(left, right);
        }
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        textArea.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent evt) {
                updateHighlighters();
            }

            public void removeUpdate(DocumentEvent evt) {
                updateHighlighters();
            }

            public void changedUpdate(DocumentEvent evt) {
                updateHighlighters();
            }
        });
        textField.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent evt) {
                updateHighlighters();
            }

            public void removeUpdate(DocumentEvent evt) {
                updateHighlighters();
            }

            public void changedUpdate(DocumentEvent evt) {
                updateHighlighters();
            }
        });
        subSubPanel.add(toggle);
        toggle.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                textArea.setLineWrap(toggle.isSelected());
            }
        });
        subPanel.add(label, BorderLayout.CENTER);
        subPanel.add(subSubPanel, BorderLayout.WEST);
        panel.add(subPanel, BorderLayout.NORTH);
        final Container contentPane = getContentPane();
        contentPane.add(textField, BorderLayout.NORTH);
        contentPane.add(scrollPane, BorderLayout.CENTER);
        contentPane.add(panel, BorderLayout.SOUTH);
    }

    private void removeHighlighters() {
        Highlighter hilite = textArea.getHighlighter();
        Highlighter.Highlight[] hilites = hilite.getHighlights();
        for (int i = 0; i < hilites.length; i++) {
            if (hilites[i].getPainter() instanceof MyHighlightPainter) {
                hilite.removeHighlight(hilites[i]);
            }
        }
    }

    private void updateMatches() {
        final String regExp = textField.getText();
        try {
            int counter = 0;
            removeHighlighters();
            if (regExp != null && regExp.length() > 0) {
                counter = refreshHighlighters();
            } else {
                printExceptionMessage("Please enter a regular expresssion");
            }
            final String messageText;
            if (counter == 1) {
                messageText = "Exactly 1 match found";
            } else {
                messageText = counter + " matches found";
            }
            label.setForeground(Color.BLACK);
            label.setText(messageText);
        } catch (BadLocationException e) {
            printExceptionMessage(e);
        } catch (PatternSyntaxException e) {
            printExceptionMessage(e);
        } catch (IllegalStateException e) {
            printExceptionMessage(e);
        } catch (RuntimeException e) {
            printExceptionMessage(e);
        }
    }

    private int refreshHighlighters() throws BadLocationException {
        int counter = 0;
        try {
            final String regExp = textField.getText();
            Highlighter hilite = textArea.getHighlighter();
            final Document doc = textArea.getDocument();
            final String text = doc.getText(0, doc.getLength());
            final Pattern pattern = Pattern.compile(regExp);
            final Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    hilite.addHighlight(matcher.start(i), matcher.end(i), myHighlightPainterDarker);
                }
                hilite.addHighlight(matcher.start(), matcher.end(), myHighlightPainter);
                counter++;
            }
            label.setText("");
        } catch (BadLocationException e) {
            showExceptionMessage(e);
        } catch (PatternSyntaxException e) {
            showExceptionMessage(e);
        } catch (IllegalStateException e) {
            showExceptionMessage(e);
        } catch (RuntimeException e) {
            showExceptionMessage(e);
        }
        return counter;
    }

    private void updateHighlighters() {
        removeHighlighters();
        try {
            refreshHighlighters();
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    private final Highlighter.HighlightPainter myHighlightPainter = new MyHighlightPainter(Color.CYAN);

    private final Highlighter.HighlightPainter myHighlightPainterDarker = new MyHighlightPainter(Color.CYAN.darker());

    class MyHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {

        public MyHighlightPainter(Color color) {
            super(color);
        }
    }

    private void showExceptionMessage(final Exception e) {
        label.setText(e.getMessage());
    }

    private void printExceptionMessage(final Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void printExceptionMessage(final String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static final void main(String[] args) {
        final RegularExpressionMatchTester tester = new RegularExpressionMatchTester();
        tester.pack();
        tester.setVisible(true);
    }
}
