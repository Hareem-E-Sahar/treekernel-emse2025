package fr.free.jchecs.swg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import static fr.free.jchecs.core.Constants.APPLICATION_AUTHOR;
import static fr.free.jchecs.core.Constants.APPLICATION_NAME;
import static fr.free.jchecs.core.Constants.APPLICATION_VERSION;
import static fr.free.jchecs.core.Constants.APPLICATION_WEB;
import static fr.free.jchecs.swg.ResourceUtils.getI18NString;
import static fr.free.jchecs.swg.ResourceUtils.getImageIcon;

/**
 * Boite de dialogue de l'option "A propos...".
 * 
 * @author David Cotton
 */
final class AboutDialog {

    /** Panel principal de la boite de dialogue. */
    private static JOptionPane S_optionPane;

    /** Composant affichant la licence. */
    private static JComponent S_licensePanel;

    /**
	 * Classe utilitaire : ne pas instancier.
	 */
    private AboutDialog() {
    }

    /**
	 * Renvoi le composant affichant la licence.
	 * 
	 * @return Zone affichant la licence.
	 */
    private static synchronized JComponent getLicensePanel() {
        if (S_licensePanel == null) {
            final JEditorPane ep = new JEditorPane();
            ep.setEditable(false);
            ep.setContentType("text/html");
            final String chemin = getI18NString("dialog.about.url.license");
            final LicenseLoader ll = new LicenseLoader(ep, Start.class.getResource(chemin));
            ll.start();
            S_licensePanel = new JScrollPane(ep);
        }
        assert S_licensePanel != null;
        return S_licensePanel;
    }

    /**
	 * Renvoi, après l'avoir construit si nécessaire, le panel principal de la
	 * boite de dialogue.
	 * 
	 * @return Panel principal de la boite de dialogue.
	 */
    private static synchronized JOptionPane getOptionPane() {
        if (S_optionPane == null) {
            final JTabbedPane onglets = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
            final JPanel auteur = new JPanel(new BorderLayout());
            final JLabel app = new JLabel(APPLICATION_NAME, getImageIcon("icon64.png"), SwingConstants.CENTER);
            final Font ft = app.getFont();
            app.setFont(ft.deriveFont(Font.BOLD, ft.getSize() * 2.5F));
            auteur.add(app, BorderLayout.NORTH);
            final JPanel centre = new JPanel(new GridLayout(4, 1));
            final JLabel ver = new JLabel("v " + APPLICATION_VERSION, SwingConstants.CENTER);
            ver.setFont(ft.deriveFont(Font.BOLD, ft.getSize() * 1.5F));
            centre.add(ver);
            centre.add(new JLabel("© 2006-2009 - " + APPLICATION_AUTHOR, SwingConstants.CENTER));
            final JLabel traducteur = new JLabel(getI18NString("dialog.about.translater"), SwingConstants.CENTER);
            traducteur.setFont(ft.deriveFont(Font.ITALIC));
            centre.add(traducteur);
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                final JPanel panelWeb = new JPanel();
                final JButton boutonWeb = new JButton(APPLICATION_WEB);
                boutonWeb.setToolTipText(getI18NString("dialog.about.web.tooltip"));
                boutonWeb.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent pEvt) {
                        assert pEvt != null;
                        try {
                            Desktop.getDesktop().browse(new URI(APPLICATION_WEB));
                        } catch (final IOException e) {
                        } catch (final URISyntaxException e) {
                        }
                    }
                });
                panelWeb.add(boutonWeb);
                centre.add(panelWeb);
            } else {
                centre.add(new JLabel(APPLICATION_WEB, SwingConstants.CENTER));
            }
            auteur.add(centre, BorderLayout.CENTER);
            final JTextArea remerciements = new JTextArea(getI18NString("dialog.about.thanks"));
            remerciements.setFont(ft.deriveFont(Font.ITALIC, ft.getSize() * 0.9F));
            remerciements.setLineWrap(true);
            remerciements.setWrapStyleWord(true);
            remerciements.setOpaque(false);
            remerciements.setForeground(Color.DARK_GRAY);
            remerciements.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
            auteur.add(remerciements, BorderLayout.SOUTH);
            onglets.add(getI18NString("dialog.about.author"), auteur);
            onglets.add(getI18NString("dialog.about.license"), getLicensePanel());
            final TableModel modele = new AboutModel();
            final JTable proprietes = new JTable(modele);
            proprietes.getTableHeader().setReorderingAllowed(false);
            proprietes.setEnabled(false);
            proprietes.setRowSelectionAllowed(false);
            proprietes.setRowSorter(new TableRowSorter<TableModel>(modele));
            proprietes.setShowHorizontalLines(false);
            proprietes.setDefaultRenderer(Object.class, new EvenOddRowsRenderer());
            onglets.add(getI18NString("dialog.about.system"), new JScrollPane(proprietes));
            onglets.setPreferredSize(new Dimension(450, 300));
            S_optionPane = new JOptionPane(onglets, JOptionPane.PLAIN_MESSAGE);
        }
        assert S_optionPane != null;
        return S_optionPane;
    }

    /**
	 * Affiche la boite de dialogue.
	 * 
	 * @param pParent
	 *            Composant parent de la boite de dialogue (peut être à null).
	 */
    static void showAboutDialog(final Component pParent) {
        final JDialog dlg = getOptionPane().createDialog(pParent, getI18NString("dialog.about.title"));
        dlg.pack();
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(pParent);
        dlg.setVisible(true);
    }

    /**
	 * Processus de chargement de la licence en tâche de fond.
	 */
    private static final class LicenseLoader extends Thread {

        /** Zone texte accueillant la licence. */
        private final JEditorPane _editorPane;

        /** URL de la licence. */
        private final URL _url;

        /**
		 * Construit le processus.
		 * 
		 * @param pZoneTexte
		 *            Zone texte accueillant la licence.
		 * @param pURL
		 *            URL de la licence.
		 */
        LicenseLoader(final JEditorPane pZoneTexte, final URL pURL) {
            super("loadLicense");
            _editorPane = pZoneTexte;
            _url = pURL;
            setDaemon(true);
        }

        /**
		 * Charge le texte de la licence en tâche de fond.
		 */
        @Override
        public void run() {
            try {
                _editorPane.setPage(_url);
            } catch (final IOException e) {
                _editorPane.setText("<center><h3>This program is under GPL.</h3></center>");
            }
        }
    }

    /**
	 * Modèle de données interne pour le tableau des propriétés.
	 */
    private static final class AboutModel extends DefaultTableModel {

        /** Identifiant de la classe pour la sérialisation. */
        private static final long serialVersionUID = -5557358361119743070L;

        /** Liste des clés des propriétés système. */
        private final List<Object> _keys;

        /**
		 * Constructeur par défaut.
		 */
        AboutModel() {
            _keys = new ArrayList<Object>(System.getProperties().keySet());
            setColumnCount(2);
            setRowCount(_keys.size());
            final String[] enTetes = { getI18NString("dialog.about.properties"), getI18NString("dialog.about.values") };
            setColumnIdentifiers(enTetes);
        }

        /**
		 * Renvoi le contenu d'une cellule.
		 * 
		 * @param pLigne
		 *            Indice de la ligne.
		 * @param pColonne
		 *            Indice de la colonne.
		 * @return Contenu de la cellule correspondante.
		 */
        @Override
        public Object getValueAt(final int pLigne, final int pColonne) {
            assert (pLigne >= 0) && (pLigne < _keys.size());
            assert (pColonne == 0) || (pColonne == 1);
            String res = (String) _keys.get(pLigne);
            if (pColonne == 1) {
                res = System.getProperties().getProperty(res);
            }
            assert res != null;
            return res;
        }
    }
}
