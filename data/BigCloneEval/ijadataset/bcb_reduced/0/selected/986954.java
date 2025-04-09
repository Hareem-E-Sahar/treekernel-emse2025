package vue;

import gestionSites.Site;
import gestionSites.SiteListHandler;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * vue de la liste de sites
 *
 */
public class SiteListView extends Panel implements Observer {

    private static final long serialVersionUID = 1L;

    /**
	 * arbre des dossiers contenant les sites
	 */
    protected JTree arborescence;

    /**
	 * gestionnaire de sites
	 */
    protected SiteListHandler siteListH;

    /**
	 * vue principale
	 */
    protected MainView mainview;

    /**
	 * bool�en de tri par nom
	 */
    protected boolean nameAscending;

    /**
	 * bool�en de tri par taille 
	 */
    protected boolean sizeAscending;

    /**
	 * bool�en de tri par date
	 */
    protected boolean dateAscending;

    /**
	 * panneau � ascenceurs
	 */
    private JScrollPane jScrollPane;

    /**
	 * constructeur
	 * @param siteList gestionnaire de sites
	 * @param mv vue principale
	 */
    public SiteListView(SiteListHandler siteList, MainView mv) {
        mainview = mv;
        this.nameAscending = true;
        this.sizeAscending = true;
        this.dateAscending = true;
        this.setPreferredSize(new Dimension(300, 450));
        this.siteListH = siteList;
        siteListH.addObserver(this);
        DefaultMutableTreeNode racine = new DefaultMutableTreeNode("My Websites");
        JButton triNom = new JButton("Name");
        JButton triDate = new JButton("Date");
        JButton triTaille = new JButton("Size");
        triNom.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (nameAscending) {
                    siteListH.xmlSort("triAlphabetiqueAscending.xsl");
                    nameAscending = false;
                } else {
                    siteListH.xmlSort("triAlphabetiqueDescending.xsl");
                    nameAscending = true;
                }
            }
        });
        triTaille.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (sizeAscending) {
                    siteListH.xmlSort("triTailleAscending.xsl");
                    sizeAscending = false;
                } else {
                    siteListH.xmlSort("triTailleDescending.xsl");
                    sizeAscending = true;
                }
            }
        });
        triDate.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (dateAscending) {
                    siteListH.xmlSort("triDateAscending.xsl");
                    dateAscending = false;
                } else {
                    siteListH.xmlSort("triDateDescending.xsl");
                    dateAscending = true;
                }
            }
        });
        this.add(new JLabel("Sort by"));
        this.add(triTaille);
        this.add(triNom);
        this.add(triDate);
        for (int i = 0; i < (siteListH.getList()).getSize(); i++) {
            Site site = siteListH.getList().get(i);
            racine.add(site.getTree());
        }
        arborescence = new JTree(racine);
        this.addDoubleClickListener();
        jScrollPane = new JScrollPane(arborescence);
        jScrollPane.setPreferredSize(new Dimension(300, 387));
        jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane.getViewport().add(arborescence, null);
        this.add(jScrollPane);
    }

    /**
	 * acc�s � l'arborescence
	 * @return arborescence
	 */
    public JTree getArborescence() {
        return arborescence;
    }

    /**
	 * modification de l'arborescence
	 * @param nouvelle arborescence
	 */
    public void setArborescence(JTree arborescence2) {
        this.arborescence = arborescence2;
    }

    @Override
    public void update(Observable o, Object arg) {
        jScrollPane.remove(arborescence);
        if (arg != null) if (((String) arg).compareTo("TRI") == 0) siteListH.loadList();
        DefaultMutableTreeNode racine = new DefaultMutableTreeNode("My Websites");
        for (int i = 0; i < (siteListH.getList()).getSize(); i++) {
            Site site = siteListH.getList().get(i);
            racine.add(site.getTree());
        }
        arborescence = new JTree(racine);
        arborescence.addTreeSelectionListener(mainview);
        this.addDoubleClickListener();
        setArborescence(arborescence);
        jScrollPane.getViewport().add(arborescence, null);
        jScrollPane.getViewport().add(arborescence, null);
        mainview.majSiteView();
    }

    /**
	 * ajout de la fonction de double clic sur un fichier de l'arborescence
	 * afin d'ouvrir des fichiers
	 */
    public void addDoubleClickListener() {
        arborescence.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    TreePath tp = arborescence.getClosestPathForLocation(evt.getX(), evt.getY());
                    String path = tp.toString();
                    String name = path.substring(path.indexOf(',') + 2);
                    if (name.contains(" (")) {
                        name = name.substring(0, name.indexOf(" ("));
                    }
                    path = path.substring(path.indexOf(',') + 1, path.length());
                    path = path.replaceAll("\\([^,]*,", "/");
                    path = path.replaceAll(",", "/");
                    path = path.replaceAll(" ", "");
                    path = path.replaceAll("\\]", "");
                    Site site = siteListH.getList().getSite(name);
                    if (site != null) {
                        path = site.getTarget() + "\\" + path;
                    }
                    try {
                        File file = new File(path);
                        if (file.isFile()) {
                            if (Desktop.isDesktopSupported()) {
                                Desktop desktop = Desktop.getDesktop();
                                if (desktop.isSupported(Desktop.Action.OPEN)) {
                                    desktop.open(file);
                                }
                            }
                        }
                    } catch (Exception rex) {
                    }
                }
            }
        });
    }
}
