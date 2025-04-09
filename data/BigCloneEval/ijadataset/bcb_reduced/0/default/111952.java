import java.awt.Desktop;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.net.URI;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.*;

public class FPSViewer extends JFrame implements ActionListener {

    private static JEditorPane editorPane = new JEditorPane();

    private static JScrollPane scrollPane = new JScrollPane(editorPane);

    private static final long serialVersionUID = 1L;

    private static JMenuBar menuBar = new JMenuBar();

    private static JMenu menuFile = new JMenu("File");

    private static NodeList itemList;

    private static String APPDIR = System.getProperty("user.dir").replaceAll("\\\\", "/");

    private JMenuItem menuOpen = new JMenuItem("Open");

    public FPSViewer() {
        super("FPSingle");
        mkdirs();
        setSize(800, 600);
        loadPage("jar:file:" + APPDIR + "/FPSViewer.jar!/template.html");
        editorPane.setEditable(false);
        editorPane.setAutoscrolls(true);
        this.add(scrollPane);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuOpen.addActionListener(this);
        menuFile.add(menuOpen);
        menuBar.add(menuFile);
        this.setJMenuBar(menuBar);
        editorPane.addHyperlinkListener(new HyperlinkListener() {

            public void hyperlinkUpdate(HyperlinkEvent r) {
                try {
                    if (r.getEventType() == HyperlinkEvent.EventType.ACTIVATED) editorPane.setPage(r.getURL());
                } catch (Exception e) {
                }
            }
        });
        setVisible(true);
    }

    private static void mkdirs() {
        File dir = new File("cache/images");
        if (!dir.exists()) dir.mkdirs();
    }

    private static void loadPage(String URL) {
        try {
            editorPane.setPage(URL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void operBrowse(String url) {
        Desktop desktop = Desktop.getDesktop();
        URI uri;
        try {
            uri = new URI(url);
            desktop.browse(uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String loadFPS() {
        String filename = null;
        JFileChooser c = new JFileChooser();
        int rVal = c.showOpenDialog(this);
        if (rVal == JFileChooser.APPROVE_OPTION) {
            filename = (c.getSelectedFile().getAbsolutePath());
        }
        if (rVal == JFileChooser.CANCEL_OPTION) {
            filename = "jar:file:" + APPDIR + "/FPSViewer.jar!/demo.xml";
        }
        debug(filename);
        return filename;
    }

    private void debug(String filename) {
        System.out.println(filename);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.menuOpen) {
            FPStoProblems(loadFPS());
        }
    }

    public static void main(String[] args) {
        new FPSViewer();
    }

    private static void FPStoProblems(String filepath) {
        Document doc;
        doc = parseXML(filepath);
        itemList = doc.getElementsByTagName("item");
        try {
            PrintStream ps = new PrintStream("cache/cache.html");
            for (int i = 0; i < itemList.getLength(); i++) {
                Problem p = itemToProblem(itemList.item(i));
                ps.println("<a href=p" + p.num + ".html>" + p.title + "</a><br>");
                try {
                    problem2HTML(p);
                } catch (Exception e) {
                }
            }
            ps.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        loadPage("jar:file:" + APPDIR + "/FPSViewer.jar!/demo.xml");
        loadPage("file:" + APPDIR + "/cache/cache.html");
        operBrowse("file:" + APPDIR + "/cache/cache.html");
    }

    private static void problem2HTML(Problem p) {
        String content = readFileByLines("template.html");
        content = content.replaceFirst("\\$FPS_TITLE", p.title);
        content = content.replaceFirst("\\$FPS_TIME", p.time);
        content = content.replaceFirst("\\$FPS_MEMORY", p.memory);
        content = content.replaceFirst("\\$FPS_Description", p.description);
        content = content.replaceFirst("\\$FPS_INPUT", p.input);
        content = content.replaceFirst("\\$FPS_OUTPUT", p.output);
        content = content.replaceFirst("\\$FPS_Sample_Input", p.sample_input);
        content = content.replaceFirst("\\$FPS_Sample_Output", p.sample_output);
        content = content.replaceFirst("\\$FPS_HINT", p.hint);
        content = content.replaceFirst("\\$FPS_Source", p.source);
        PrintStream ps = null;
        try {
            ps = new PrintStream("cache/p" + p.num + ".html");
            ps.print(content);
            ps.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    public static String readFileByLines(String fileName) {
        StringBuffer sb = new StringBuffer();
        InputStream is = FPSViewer.class.getResourceAsStream("/template.html");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            String tempString = null;
            int line = 1;
            while ((tempString = reader.readLine()) != null) {
                sb.append(tempString);
                line++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return sb.toString();
    }

    private static Problem itemToProblem(Node item) {
        Problem p = new Problem();
        NodeList ch = item.getChildNodes();
        for (int i = 0; i < ch.getLength(); i++) {
            Node e = ch.item(i);
            String name = e.getNodeName();
            String value = e.getTextContent();
            if (name.equalsIgnoreCase("title")) {
                p.title = value;
            }
            if (name.equalsIgnoreCase("time_limit")) {
                p.time = value;
            }
            if (name.equalsIgnoreCase("memory_limit")) {
                p.memory = value;
            }
            if (name.equalsIgnoreCase("description")) {
                p.description = p.setImages(value);
            }
            if (name.equalsIgnoreCase("input")) {
                p.input = p.setImages(value);
            }
            if (name.equalsIgnoreCase("output")) {
                p.output = p.setImages(value);
            }
            if (name.equalsIgnoreCase("sample_input")) {
                p.sample_input = value;
            }
            if (name.equalsIgnoreCase("sample_output")) {
                p.sample_output = value;
            }
            if (name.equalsIgnoreCase("hint")) {
                p.hint = p.setImages(value);
            }
            if (name.equalsIgnoreCase("source")) {
                p.source = p.setImages(value);
            }
            if (name.equalsIgnoreCase("img")) {
                p.imageList.add(new Image(e, p));
            }
        }
        return p;
    }

    private static Document parseXML(String filepath) {
        Document doc = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(filepath);
            doc.normalize();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;
    }
}

class Problem {

    private static int counter = 0;

    public int num = counter++;

    Vector<Image> imageList = new Vector<Image>();

    String title = "";

    String time = "";

    String memory = "";

    String description = "";

    String input = "";

    String output = "";

    String sample_input = "";

    String sample_output = "";

    String hint = "";

    String source = "";

    public String setImages(String html) {
        Iterator<Image> i = imageList.iterator();
        while (i.hasNext()) {
            Image img = i.next();
            html = html.replaceAll(img.oldURL, img.URL);
        }
        return html;
    }
}

class Image {

    private static int counter = 0;

    int num = counter++;

    Problem p;

    public Image(Node e, Problem p) {
        this.p = p;
        NodeList cn = e.getChildNodes();
        oldURL = cn.item(0).getTextContent();
        URL = "images/pic" + p.num + "_" + num;
        try {
            byte[] decodeBuffer = new sun.misc.BASE64Decoder().decodeBuffer(cn.item(1).getTextContent());
            FileOutputStream fo = new FileOutputStream("cache/" + URL);
            fo.write(decodeBuffer);
            fo.close();
        } catch (DOMException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    String oldURL = "";

    String URL = "";
}
