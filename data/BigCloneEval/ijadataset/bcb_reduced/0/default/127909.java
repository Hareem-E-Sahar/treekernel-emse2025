import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    public static Boolean DEBUG = true;

    public static String INDEXDIR = System.getProperty("user.dir");

    public static String MEDIASEARCH = "MediaSearch";

    public static String FILESEARCH = "FileSearch";

    public MainFrame() {
        final CheckTree tree = new CheckTree();
        final JPanel selectionAna = new JPanel();
        JMenuBar menuBar = new JMenuBar();
        JMenu file = new JMenu("Dosya");
        file.setMnemonic(KeyEvent.VK_D);
        JMenuItem yolBelirle = new JMenuItem("Kaynak Dizin");
        yolBelirle.setMnemonic(KeyEvent.VK_K);
        yolBelirle.setToolTipText("Icerik Kontrolu icin Klasor secimi");
        yolBelirle.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(new File("."));
                chooser.setDialogTitle("Dizin Seciniz");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);
                if (chooser.showOpenDialog(selectionAna) == JFileChooser.APPROVE_OPTION) {
                    INDEXDIR = chooser.getSelectedFile().getAbsolutePath();
                    if (MainFrame.DEBUG) System.out.println(this.getClass().getName() + ": Dizin agaci icin indexDir degistiriliyor, yeni indexDir = " + INDEXDIR);
                    tree.setroot(INDEXDIR);
                }
            }
        });
        file.add(yolBelirle);
        JMenuItem closeWindow = new JMenuItem("Cikis");
        closeWindow.setMnemonic(KeyEvent.VK_C);
        closeWindow.setToolTipText("Programdan Cik");
        closeWindow.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        });
        file.add(closeWindow);
        menuBar.add(file);
        JMenu search = new JMenu("Arama");
        file.setMnemonic(KeyEvent.VK_S);
        JMenuItem searchElement = new JMenuItem("Arama");
        JMenuItem searchMedia = new JMenuItem("Medya ara");
        searchMedia.setToolTipText("Medya nin icerigini getir");
        searchMedia.addActionListener(new AramaDugmeDinleme(MEDIASEARCH));
        searchElement.setToolTipText("Icerik Arama");
        searchElement.addActionListener(new AramaDugmeDinleme(FILESEARCH));
        search.add(searchElement);
        search.add(searchMedia);
        menuBar.add(search);
        setJMenuBar(menuBar);
        JPanel treePanel = new JPanel();
        treePanel.setLayout(new BorderLayout());
        JButton saveDatabase = new JButton("Veritabanina Kaydet");
        selectionAna.setLayout(new BorderLayout());
        SelectionPanel selectionPanel = new SelectionPanel();
        JScrollPane selectionPane = new JScrollPane(selectionPanel);
        selectionAna.add(selectionPane);
        selectionAna.add(saveDatabase, BorderLayout.SOUTH);
        saveDatabase.addActionListener(new DatabaseSaveListener(selectionPanel));
        JScrollPane sp = new JScrollPane(tree);
        treePanel.add(sp);
        getContentPane().add(treePanel, BorderLayout.WEST);
        JButton listeGetir = new JButton("Liste Getir");
        listeGetir.addActionListener(new ListeCekmeDugmeDinleme(tree, selectionPanel));
        treePanel.add(listeGetir, BorderLayout.SOUTH);
        getContentPane().add(selectionAna, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        MainFrame frame = new MainFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setTitle("Medya indexleme");
        frame.setVisible(true);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int w = frame.getSize().width;
        int h = frame.getSize().height;
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;
        frame.setLocation(x, y);
    }
}
