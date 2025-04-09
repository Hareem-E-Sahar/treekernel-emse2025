package jmash;

import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import jmash.component.MultiLineCellRenderer;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import se.diod.hippo.plaf.HippoLookAndFeel;

/**
 *
 * @author Alessandro
 */
public class Main {

    public static String userDir;

    public static String waterDir;

    public static String recipeDir;

    public static String batchDir;

    public static String shoppingDir;

    public static String mashDir;

    public static String Nome = "HobbyBrew";

    public static String luppoliXML = "config/luppoli.xml";

    public static String maltiXML = "config/malti.xml";

    public static String waterXML = "config/water.xml";

    public static String yeastXML = "config/lieviti.xml";

    public static String stiliXML = "config/stili.xml";

    public static String bjcpStylesXML = "config/styleguide.xml";

    public static String coloriXML = "config/colors.xml";

    public static String configXML = "config/config.xml";

    public static String inventarioXML = "config/inventario.xml";

    public static String printTemplate = "templates/ricetta.html";

    public static Gui gui;

    public static javax.swing.JDesktopPane desktopPane;

    public static MultiLineCellRenderer multiLineCellRenderer = new MultiLineCellRenderer();

    public static Image chartImage = java.awt.Toolkit.getDefaultToolkit().createImage(Main.class.getResource("/jmash/images/kettle.jpg"));

    public static Image hopImage = java.awt.Toolkit.getDefaultToolkit().createImage(Main.class.getResource("/jmash/images/hops.gif"));

    public static ImageIcon hopIcon = new ImageIcon(java.awt.Toolkit.getDefaultToolkit().createImage(Main.class.getResource("/jmash/images/hops.jpg")));

    public static ImageIcon spiceIcon = new ImageIcon(java.awt.Toolkit.getDefaultToolkit().createImage(Main.class.getResource("/jmash/images/icon_spices.png")));

    public static ImageIcon clockIcon = new ImageIcon(java.awt.Toolkit.getDefaultToolkit().createImage(Main.class.getResource("/jmash/images/clock.png")));

    public static ImageIcon maltIcon = new ImageIcon(java.awt.Toolkit.getDefaultToolkit().createImage(Main.class.getResource("/jmash/images/ingredients.jpg")));

    public static ImageIcon extractIcon = new ImageIcon(java.awt.Toolkit.getDefaultToolkit().createImage(Main.class.getResource("/jmash/images/extract.png")));

    public static ImageIcon glassColorIcon = new ImageIcon(java.awt.Toolkit.getDefaultToolkit().createImage(Ricetta.class.getResource("/jmash/images/glass_color.png")));

    public static ImageIcon addIcon = new ImageIcon(java.awt.Toolkit.getDefaultToolkit().createImage(Ricetta.class.getResource("/jmash/images/edit_add.png")));

    public static ImageIcon remIcon = new ImageIcon(java.awt.Toolkit.getDefaultToolkit().createImage(Ricetta.class.getResource("/jmash/images/edit_remove.png")));

    public static ImageIcon mainIcon = new ImageIcon(java.awt.Toolkit.getDefaultToolkit().createImage(Ricetta.class.getResource("/jmash/images/boccale.gif")));

    public static ImageIcon boilOffIcon = new ImageIcon(java.awt.Toolkit.getDefaultToolkit().createImage(Ricetta.class.getResource("/jmash/images/steam.png")));

    public static ImageIcon diluiteIcon = new ImageIcon(java.awt.Toolkit.getDefaultToolkit().createImage(Ricetta.class.getResource("/jmash/images/extract.png")));

    public static ImageIcon uploadIcon = new ImageIcon(java.awt.Toolkit.getDefaultToolkit().createImage(Ricetta.class.getResource("/jmash/images/upload.png")));

    public static ImageIcon strikeIcon = new ImageIcon(java.awt.Toolkit.getDefaultToolkit().createImage(Ricetta.class.getResource("/jmash/images/bowling.gif")));

    public static ImageIcon printIcon = new ImageIcon(java.awt.Toolkit.getDefaultToolkit().createImage(Ricetta.class.getResource("/jmash/images/fileprint.png")));

    public static ImageIcon checkInventoryIcon = new ImageIcon(java.awt.Toolkit.getDefaultToolkit().createImage(Ricetta.class.getResource("/jmash/images/checkInventario.png")));

    public static ImageIcon editIcon = new ImageIcon(java.awt.Toolkit.getDefaultToolkit().createImage(Ricetta.class.getResource("/jmash/images/edit.png")));

    public static MaltType getMaltTypeByWords(String des) {
        des = des.toLowerCase();
        MaltType res = null;
        double best = -1;
        String rxp = "[\\W]|[\\s]";
        String R[] = des.toLowerCase().split(rxp);
        for (MaltType m : getMalti()) {
            String str = m.getNome().toLowerCase();
            String S[] = str.split(rxp);
            double t = -1;
            int matches = 0;
            for (int i = 0; i < S.length; i++) {
                for (int j = 0; j < R.length; j++) {
                    if (S[i].equals(R[j])) {
                        t++;
                        matches++;
                    }
                }
            }
            if (des.indexOf("lme") >= 0 && m.getNome().toLowerCase().indexOf("lme") < 0) t = -1;
            if (des.indexOf("lme") < 0 && m.getNome().toLowerCase().indexOf("lme") >= 0) t = -1;
            if (des.indexOf("dme") >= 0 && m.getNome().toLowerCase().indexOf("dme") < 0) t = -1;
            if (des.indexOf("dme") < 0 && m.getNome().toLowerCase().indexOf("dme") >= 0) t = -1;
            if (des.indexOf("dry") >= 0 && m.getNome().toLowerCase().indexOf("dry") < 0) t = -1;
            if (des.indexOf("dry") < 0 && m.getNome().toLowerCase().indexOf("dry") >= 0) t = -1;
            if (des.indexOf("extract") >= 0 && m.getNome().toLowerCase().indexOf("extract") < 0) t = -1;
            if (des.indexOf("extract") < 0 && m.getNome().toLowerCase().indexOf("extract") >= 0) t = -1;
            if (t > best) {
                best = t;
                res = m;
            }
        }
        return res;
    }

    private static void check(String str) {
        MaltType m = getMaltTypeByWords(str);
        System.out.println(str + " -> " + (m == null ? "NON TROVATO" : m.getNome()));
    }

    /** Creates a new instance of Main */
    public Main() {
        try {
            readConfig();
            readLuppoli();
            readMalti();
            readLieviti();
            readWater();
            readColors();
            Document doc = Utils.readFileAsXml(bjcpStylesXML);
            Element root = doc.getRootElement();
            Gui.brewStylePickerTableModel.setRows(getBJCPStyles(root));
        } catch (Exception ex) {
            Utils.showException(ex);
            return;
        }
        try {
            loadCache();
        } catch (Exception e) {
            e.printStackTrace();
        }
        gui = new Gui();
        desktopPane = Gui.desktopPane;
        gui.btnUpdate.setVisible(false);
        new File("_runner.jar").delete();
        Utils.parseUtilizzo((String) getFromCache("Main.utilizzo", ""));
        gui.setVisible(true);
        if (config.getProxyHost() != null) System.setProperty("http.proxyHost", config.getProxyHost());
        if (config.getProxyPort() != null) System.setProperty("http.proxyPort", config.getProxyPort());
        update();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            HippoLookAndFeel lf = new HippoLookAndFeel();
            HippoLookAndFeel.setMyCurrentTheme(new se.diod.hippo.plaf.theme.HippoTheme() {

                public ColorUIResource getInactiveSystemTextColor() {
                    return new ColorUIResource(new Color(0, 50, 0));
                }
            });
            UIManager.setLookAndFeel(lf);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        userDir = System.getProperty("user.dir");
        recipeDir = userDir + "/recipes/";
        batchDir = userDir + "/batches/";
        mashDir = userDir + "/mashes/";
        shoppingDir = userDir + "/shopping/";
        waterDir = userDir + "/water/";
        new Main();
        try {
            if (args.length > 0 && args[0].compareToIgnoreCase("showNews") == 0) {
                String str = config.getRemoteRoot();
                if (!str.startsWith("http://")) str = "http://" + str;
                if (!str.endsWith("/")) str = str + "/";
                gui.addFrame(new ViewHtml(Utils.download(str + "news.html")));
            }
        } catch (Exception ex) {
        }
    }

    public static void readLuppoli() throws Exception {
        Gui.hopPickerTableModel.emptyRows();
        Document doc = Utils.readFileAsXml(Main.luppoliXML);
        if (doc == null) {
            return;
        }
        Element root = doc.getRootElement();
        if (root.getName().compareToIgnoreCase("hops") == 0) {
            System.out.println("hops detected");
            @SuppressWarnings("unchecked") Iterator iterator = root.getChildren().iterator();
            while (iterator.hasNext()) {
                HopType type = HopType.fromXml((Element) iterator.next());
                if (type != null) {
                    Gui.hopPickerTableModel.addRow(type);
                }
            }
        }
        Collections.sort(Gui.hopPickerTableModel.getRows());
    }

    public static List<MaltType> getMalti() {
        return Gui.maltPickerTableModel.getRows();
    }

    public static MaltType getMaltTypeByDes(String des) {
        for (MaltType m : getMalti()) {
            if (m.getNome().toLowerCase().startsWith(des.toLowerCase()) || des.toLowerCase().startsWith(m.getNome().toLowerCase())) return m;
        }
        return null;
    }

    public static BrewStyle getBrewStyleByDes(String des) {
        for (BrewStyle m : Gui.brewStylePickerTableModel.getRows()) {
            if (m.getNome() != null && m.getNome().compareToIgnoreCase(des) == 0) return m;
        }
        return null;
    }

    public static HopType getHopTypeByDes(String des) {
        for (HopType m : Gui.hopPickerTableModel.getRows()) {
            if (m.getNome().compareToIgnoreCase(des) == 0) return m;
        }
        return null;
    }

    public static void readMalti() throws Exception {
        Gui.maltPickerTableModel.emptyRows();
        Document doc = Utils.readFileAsXml(Main.maltiXML);
        if (doc == null) {
            return;
        }
        Element root = doc.getRootElement();
        if (root.getName().compareToIgnoreCase("malts") == 0) {
            System.out.println("malts detected");
            @SuppressWarnings("unchecked") Iterator iterator = root.getChildren().iterator();
            while (iterator.hasNext()) {
                Element malt = (Element) iterator.next();
                MaltType type = MaltType.fromXml(malt);
                if (type != null) {
                    Gui.maltPickerTableModel.addRow(type);
                }
            }
        }
        Collections.sort(Gui.maltPickerTableModel.getRows());
    }

    public static void readLieviti() throws Exception {
        Gui.yeastPickerTableModel.emptyRows();
        Document doc = Utils.readFileAsXml(Main.yeastXML);
        if (doc == null) {
            return;
        }
        Element root = doc.getRootElement();
        if (root.getName().compareToIgnoreCase("yeasts") == 0) {
            System.out.println("yeasts detected");
            @SuppressWarnings("unchecked") Iterator iterator = root.getChildren().iterator();
            while (iterator.hasNext()) {
                Element malt = (Element) iterator.next();
                try {
                    YeastType type = (YeastType) Utils.fromXml(new YeastType(), YeastType.getCampiXml(), malt);
                    if (type != null) {
                        Gui.yeastPickerTableModel.addRow(type);
                    }
                } catch (Exception ex) {
                    Utils.showException(ex);
                    return;
                }
            }
        }
    }

    public static void readWater() throws Exception {
        Gui.waterPickerTableModel.emptyRows();
        Document doc = Utils.readFileAsXml(Main.waterXML);
        if (doc == null) {
            return;
        }
        Element root = doc.getRootElement();
        if (root.getName().compareToIgnoreCase("waters") == 0) {
            System.out.println("waters detected");
            @SuppressWarnings("unchecked") Iterator iterator = root.getChildren().iterator();
            while (iterator.hasNext()) {
                Element malt = (Element) iterator.next();
                try {
                    WaterProfile type = (WaterProfile) Utils.fromXml(new WaterProfile(), WaterProfile.campiXml, malt);
                    if (type != null) {
                        Gui.waterPickerTableModel.addRow(type);
                    }
                } catch (Exception ex) {
                    Utils.showException(ex);
                    return;
                }
            }
        }
        Collections.sort(Gui.waterPickerTableModel.getRows());
    }

    public static Config config;

    public static void readConfig() throws Exception {
        Document doc = Utils.readFileAsXml(Main.configXML);
        if (doc == null) {
            return;
        }
        Element root = doc.getRootElement();
        config = Config.fromXml(root);
        System.out.println("config detected");
    }

    public static void readStili() throws Exception {
        Gui.brewStylePickerTableModel.emptyRows();
        Document doc = Utils.readFileAsXml(Main.stiliXML);
        if (doc == null) {
            return;
        }
        Element root = doc.getRootElement();
        if (root.getName().compareToIgnoreCase("styles") == 0) {
            System.out.println("styles detected");
            @SuppressWarnings("unchecked") Iterator iterator = root.getChildren().iterator();
            while (iterator.hasNext()) {
                BrewStyle type = BrewStyle.fromXml((Element) iterator.next());
                Gui.brewStylePickerTableModel.addRow(type);
            }
        }
    }

    public static BinaryTreeNode treeColor;

    public static void readColors() throws Exception {
        Document doc = Utils.readFileAsXml(Main.coloriXML);
        if (doc == null) {
            return;
        }
        Element root = doc.getRootElement();
        if (root.getName().compareToIgnoreCase("lovibondToRGB") == 0) {
            System.out.println("colors detected");
            @SuppressWarnings("unchecked") Iterator iterator = root.getChildren().iterator();
            Srm2Rgb dao = new Srm2Rgb();
            while (iterator.hasNext()) {
                try {
                    Srm2Rgb type = (Srm2Rgb) Utils.fromXml(dao, Srm2Rgb.getCampiXml(), (Element) iterator.next());
                    type.setEbc(Utils.srmToEbc(type.getSrm()));
                    if (treeColor == null) {
                        treeColor = new BinaryTreeNode(type.getEbc(), new Color(type.getR(), type.getG(), type.getB()));
                    } else {
                        treeColor.add(type.getEbc(), new Color(type.getR(), type.getG(), type.getB()));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static HashMap<Double, Color> hmRGB = new HashMap<Double, Color>();

    public static class BinaryTreeNode {

        public Double data;

        public Color color;

        public BinaryTreeNode L, R;

        public BinaryTreeNode() {
        }

        public BinaryTreeNode(Double d, Color c) {
            this.data = d;
            this.color = c;
        }

        public void add(Double d, Color c) {
            if (d.compareTo(data) > 0) {
                if (this.R == null) {
                    this.R = new BinaryTreeNode(d, c);
                } else {
                    this.R.add(d, c);
                }
            }
            if (d.compareTo(data) < 0) {
                if (this.L == null) {
                    this.L = new BinaryTreeNode(d, c);
                } else {
                    this.L.add(d, c);
                }
            }
        }

        public static Color blend(Color a, Color b) {
            int r = a.getRed() + b.getRed();
            int g = a.getGreen() + b.getGreen();
            int blue = a.getBlue() + b.getBlue();
            return new Color(r / 2, g / 2, blue / 2);
        }

        public Color srmToRgb(Double d) {
            return ebcToRgb(Utils.srmToEbc(d));
        }

        public Color ebcToRgb(Double d) {
            if (hmRGB.containsKey(d)) return (Color) hmRGB.get(d);
            if (d.equals(this.data)) {
                hmRGB.put(d, this.color);
                return this.color;
            }
            if (d.compareTo(this.data) > 0) {
                if (this.R == null) {
                    hmRGB.put(d, this.color);
                    return this.color;
                } else if (this.R.data.compareTo(d) > 0) {
                    Color res = blend(this.color, this.R.color);
                    if (res != null) hmRGB.put(d, res);
                    return res;
                } else {
                    Color res = (Color) this.R.ebcToRgb(d);
                    if (res != null) hmRGB.put(d, res);
                    return res;
                }
            }
            if (d.compareTo(this.data) < 0) {
                if (this.L == null) {
                    hmRGB.put(d, this.color);
                    return this.color;
                } else if (this.L.data.compareTo(d) < 0) {
                    Color res = blend(this.color, this.L.color);
                    if (res != null) hmRGB.put(d, res);
                    return res;
                } else {
                    Color res = (Color) this.L.ebcToRgb(d);
                    if (res != null) hmRGB.put(d, res);
                    return res;
                }
            }
            hmRGB.put(d, this.color);
            return this.color;
        }
    }

    public static void update() {
        Document doc;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH.mm.ss");
        String remoteRoot = config.getRemoteRoot();
        if (remoteRoot == null) remoteRoot = "www.hobbybirra.it/hobbybrew";
        if (!remoteRoot.startsWith("http://")) remoteRoot = "http://" + remoteRoot;
        if (!remoteRoot.endsWith("/")) remoteRoot += "/";
        try {
            doc = XMLReader.readXML(remoteRoot + "checkversion.asp");
        } catch (IOException ex) {
            try {
                doc = XMLReader.readXML(remoteRoot + "checkversion.php");
            } catch (IOException ex2) {
                return;
            } catch (JDOMException ex2) {
                Utils.showException("Errore nel file restituito dal server: checkversion");
                return;
            }
        } catch (JDOMException ex) {
            Utils.showException("Errore nel file restituito dal server: checkversion");
            return;
        }
        Element root = doc.getRootElement();
        Iterator it = root.getChildren().iterator();
        int i = 0;
        String log = "";
        String what = "<html>";
        boolean flag = false;
        while (it.hasNext()) {
            Element elem = (Element) it.next();
            if (elem.getName().compareTo("file") == 0) {
                Attribute from = elem.getAttribute("from");
                Attribute _to = elem.getAttribute("to");
                Attribute _dir = elem.getAttribute("path");
                Attribute _time = elem.getAttribute("time");
                String dir = null;
                if (_dir != null) dir = _dir.getValue();
                if (dir != null && dir.length() > 0) {
                    File file = new File(dir);
                    file.mkdir();
                }
                if (dir == null) dir = "";
                String d = remoteRoot + dir + from.getValue();
                String t = dir + _to.getValue();
                File f = new File(t);
                if (f.exists()) {
                    try {
                        Date date = new Date(f.lastModified());
                        Date remoteDate = sdf.parse(_time.getValue());
                        if (date.compareTo(remoteDate) < 0) {
                            flag = true;
                            what += "Deve essere aggiornato il file " + t + "<br>";
                        }
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    what += "Deve essere aggiunto il file " + t + "<br>";
                    flag = true;
                }
            }
        }
        if (flag) {
            gui.btnUpdate.setVisible(true);
            gui.btnUpdate.setToolTipText(what + "</html>");
            Thread thread = new Thread() {

                javax.swing.border.LineBorder B = new javax.swing.border.LineBorder(new java.awt.Color(250, 0, 0), 2, true);

                public void run() {
                    while (true) {
                        try {
                            gui.btnUpdate.setBorder(null);
                            sleep(1000);
                            gui.btnUpdate.setBorder(B);
                            sleep(300);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            };
            thread.start();
        }
    }

    private static Double toDouble(Element el) {
        if (el == null) return null;
        if (el.getValue() == null) return null;
        if (el.getValue().length() == 0) return null;
        return new Double(el.getValue());
    }

    private static Element getChild(Element E, String[] d) {
        for (int i = 0; i < d.length; i++) {
            if (E == null) return null;
            E = E.getChild(d[i]);
        }
        return E;
    }

    private static List<BrewStyle> bjcpStyles = new ArrayList<BrewStyle>();

    private static List<BrewStyle> getBJCPStyles(Element el) {
        if (el.getName().compareTo("subcategory") == 0) {
            BrewStyle S = new BrewStyle();
            S.setNome(el.getChild("name").getValue().replaceAll("\n", ""));
            S.setNumero(el.getAttribute("id").getValue());
            S.setCategoria(el.getParentElement().getChild("name").getValue().replaceAll("\n", ""));
            S.setIbuMin(toDouble(getChild(el, new String[] { "stats", "ibu", "low" })));
            S.setIbuMax(toDouble(getChild(el, new String[] { "stats", "ibu", "high" })));
            S.setFgMin(toDouble(getChild(el, new String[] { "stats", "fg", "low" })));
            S.setFgMax(toDouble(getChild(el, new String[] { "stats", "fg", "high" })));
            S.setOgMin(toDouble(getChild(el, new String[] { "stats", "og", "low" })));
            S.setOgMax(toDouble(getChild(el, new String[] { "stats", "og", "high" })));
            S.setColorMin(toDouble(getChild(el, new String[] { "stats", "srm", "low" })));
            S.setColorMax(toDouble(getChild(el, new String[] { "stats", "srm", "high" })));
            bjcpStyles.add(S);
        }
        List<Element> children = el.getChildren();
        for (Element E : children) {
            getBJCPStyles(E);
        }
        return bjcpStyles;
    }

    private static HashMap<String, Object> cache = new HashMap<String, Object>();

    public static void putIntoCache(String k, Object o) {
        cache.put(k, o);
    }

    public static Object getFromCache(String k, Object defaultValue) {
        Object o = cache.get(k);
        if (o == null) return defaultValue;
        return o;
    }

    public static Integer getFromCache(String k, Integer defaultValue) {
        Object o = cache.get(k);
        if (o == null) return defaultValue;
        if (o instanceof Double) return ((Double) o).intValue();
        Integer i = (Integer) o;
        return i;
    }

    public static Double getFromCache(String k, Double defaultValue) {
        Object o = cache.get(k);
        if (o == null) return defaultValue;
        if (o instanceof Integer) return (double) ((Integer) o).intValue();
        Double d = (Double) o;
        return d;
    }

    public static void saveCache() {
        Document doc = new Document();
        Iterator it = cache.keySet().iterator();
        Element root = new Element("cache");
        while (it.hasNext()) {
            Object k = it.next();
            Element el = new Element(k.toString());
            el.setText(cache.get(k).toString());
            el.setAttribute("class", cache.get(k).getClass().getCanonicalName());
            root.addContent(el);
        }
        doc.setRootElement(root);
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        String xml = outputter.outputString(doc);
        try {
            FileWriter writer = new FileWriter("cache.xml");
            outputter.output(doc, writer);
            writer.close();
            System.out.println(xml);
        } catch (IOException e) {
        }
    }

    public static void loadCache() {
        Document doc = Utils.readFileAsXml("cache.xml");
        if (!doc.hasRootElement()) return;
        Element root = doc.getRootElement();
        Iterator iterator = root.getChildren().iterator();
        while (iterator.hasNext()) {
            Element elem = (Element) iterator.next();
            Attribute A = elem.getAttribute("class");
            String value = elem.getValue();
            String name = elem.getName();
            try {
                Class c = Class.forName(A.getValue());
                Constructor k = c.getConstructor(new Class[] { String.class });
                Object o = k.newInstance(new Object[] { value });
                putIntoCache(name, o);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            } catch (SecurityException ex) {
                ex.printStackTrace();
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            } catch (InvocationTargetException ex) {
                ex.printStackTrace();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            } catch (InstantiationException ex) {
                ex.printStackTrace();
            }
        }
    }
}
