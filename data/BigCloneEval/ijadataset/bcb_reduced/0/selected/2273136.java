package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

/**
 * This class is for retrieving the meaning of a word from Merriam-Webster's Online Dictionary
 * @author reza
 */
public class getMeaningWB implements getMeaningNet, Runnable {

    /** Creates a new instance of getMeaningWB */
    private String word;

    private JEditorPane editor;

    private boolean stopped;

    private Thread thisthread;

    private JButton button;

    public String getName() {
        return "Merriam-Webster";
    }

    public void init(String word, JEditorPane editor, JButton button) {
        this.button = button;
        this.editor = editor;
        this.word = word;
    }

    public void run() {
        String s;
        s = "";
        try {
            URL url = new URL("http://www.m-w.com/dictionary/" + word);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            while (((str = in.readLine()) != null) && (!stopped)) {
                s = s + str;
            }
            in.close();
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
        Pattern pattern = Pattern.compile("Main Entry:.+?<br>(.+?)</td>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(s);
        java.io.StringWriter wr = new java.io.StringWriter();
        HTMLDocument doc = null;
        HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
        try {
            doc = (HTMLDocument) editor.getDocument();
        } catch (Exception e) {
        }
        System.out.println(wr);
        editor.setContentType("text/html");
        if (matcher.find()) try {
            kit.insertHTML(doc, editor.getCaretPosition(), "<HR>" + matcher.group(1) + "<HR>", 0, 0, null);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } else try {
            kit.insertHTML(doc, editor.getCaretPosition(), "<HR><FONT COLOR='RED'>NOT FOUND!!</FONT><HR>", 0, 0, null);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        button.setEnabled(true);
    }

    public void start() {
        thisthread = new Thread(this);
        stopped = false;
        button.setEnabled(false);
        thisthread.start();
    }

    public void stop() {
        stopped = true;
        thisthread = null;
    }
}
