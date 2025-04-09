package de.bielefeld.uni.cebitec.cav.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;

/**
 * @author phuseman
 * 
 */
public class HelpFrame extends JFrame implements HyperlinkListener, ActionListener {

    private Vector<URL> history;

    private int historyIndex = 0;

    private JEditorPane editorpane;

    private URL mainPage;

    private JButton back;

    private JButton forw;

    private JButton home;

    public HelpFrame() {
        super("Help");
        history = new Vector<URL>();
        JPanel all = new JPanel(new BorderLayout());
        editorpane = new JEditorPane();
        editorpane.setEditable(false);
        editorpane.addHyperlinkListener(this);
        mainPage = Thread.currentThread().getContextClassLoader().getResource("extra/help.html");
        all.add(new JScrollPane(editorpane), BorderLayout.CENTER);
        back = new JButton("<");
        back.setActionCommand("history_back");
        back.addActionListener(this);
        back.setEnabled(false);
        forw = new JButton(">");
        forw.setActionCommand("history_forward");
        forw.addActionListener(this);
        forw.setEnabled(false);
        home = new JButton("Home");
        home.setActionCommand("home");
        home.addActionListener(this);
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(back);
        controlPanel.add(home);
        controlPanel.add(forw);
        all.add(controlPanel, BorderLayout.SOUTH);
        this.getContentPane().add(all);
        this.setPage(mainPage);
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            this.setPage(e.getURL());
        }
    }

    public void setPage(URL url) {
        try {
            if (url.getProtocol().startsWith("http")) {
                if (java.awt.Desktop.isDesktopSupported()) {
                    try {
                        java.awt.Desktop.getDesktop().browse(url.toURI());
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, url.toExternalForm(), "Please open this url:", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                if (history.size() > 50) {
                    history.remove(0);
                }
                if (history.isEmpty() || url != history.lastElement()) {
                    history.add(url);
                    historyIndex = history.size() - 1;
                    forw.setEnabled(false);
                    if (history.size() > 1) {
                        back.setEnabled(true);
                    }
                }
                editorpane.setPage(url);
                setBase();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void historyBack() {
        historyIndex--;
        if (historyIndex <= 0) {
            historyIndex = 0;
            back.setEnabled(false);
        }
        this.setPageToHistoryIndex(historyIndex);
        forw.setEnabled(true);
    }

    public void historyForward() {
        historyIndex++;
        if (historyIndex >= history.size() - 1) {
            historyIndex = history.size() - 1;
            forw.setEnabled(false);
        }
        this.setPageToHistoryIndex(historyIndex);
        back.setEnabled(true);
    }

    private void setPageToHistoryIndex(int index) {
        if (index >= 0 && index < history.size()) {
            try {
                editorpane.setPage(history.get(index));
                this.setBase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
	 * This is necessary so that images inside a jar file are found.
	 */
    private void setBase() {
        if (editorpane.getDocument().getClass() == HTMLDocument.class) {
            URL base;
            try {
                base = new URL(editorpane.getPage().toExternalForm());
                ((HTMLDocument) editorpane.getDocument()).setBase(base);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("history_back")) {
            this.historyBack();
        } else if (e.getActionCommand().equals("history_forward")) {
            this.historyForward();
        } else if (e.getActionCommand().equals("home")) {
            this.setPage(mainPage);
        }
    }
}
