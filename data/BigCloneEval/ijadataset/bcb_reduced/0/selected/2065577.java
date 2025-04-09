package com.htdsoft.ihm;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import com.htdsoft.action.ActionConstants;
import com.htdsoft.action.ActionUtils;
import com.htdsoft.exception.BusinessException;
import com.htdsoft.generic.Erreurs;
import com.htdsoft.generic.Formatter;
import com.htdsoft.generic.GBC;
import com.htdsoft.noyau.Categorie;
import com.htdsoft.noyau.DaoCategorie;
import com.htdsoft.noyau.DaoDevise;
import com.htdsoft.noyau.DaoEcriture;
import com.htdsoft.noyau.DaoEcritureLigne;
import com.htdsoft.noyau.Ecriture;
import com.htdsoft.noyau.EcritureLigne;

@SuppressWarnings("serial")
public class ActionSaisieVenteComp extends AbstractAction {

    private JPanel panel;

    private JPanel panelTitre;

    private JPanel panelNature;

    private JPanel panelType;

    private JPanel panelTotal;

    private Color couleur;

    private JLabel lblDate;

    private Date ladate;

    private ChampMonetaire chmTotalType;

    private ChampMonetaire chmTotalNature;

    DaoEcriture daoEcriture = new DaoEcriture();

    DaoEcritureLigne daoEcritureLigne = new DaoEcritureLigne();

    private DecimalFormat format = ActionConstants.ATT_CONS_format;

    DaoCategorie daoCategorie = new DaoCategorie();

    private String[] listeNomJour = ActionConstants.ATT_CONS_listeNomJour;

    public ActionSaisieVenteComp(String nom, JPanel panel) {
        super(nom);
        this.panel = panel;
        ladate = new Date();
    }

    public void actionPerformed(ActionEvent event) {
        panel.removeAll();
        panelTitre = creePanelTitre();
        panelNature = creePanelNature(null);
        panelNature.setPreferredSize(new Dimension(300, 250));
        panelType = creePanelType(null);
        panelType.setPreferredSize(new Dimension(300, 250));
        panelTotal = creePanelTotal();
        panelTotal.setPreferredSize(new Dimension(600, 50));
        couleur = panel.getBackground();
        panelTitre.setBackground(couleur);
        panelNature.setBackground(couleur);
        panelType.setBackground(couleur);
        panelTotal.setBackground(couleur);
        JButton btnEnregistrer = new JButton("Enregistrer");
        navig(panelNature, panelType, btnEnregistrer);
        btnEnregistrer.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg) {
                String totalNature = "0";
                String totalType = "0";
                for (int i = 0; i < panelTotal.getComponentCount(); i++) {
                    if (panelTotal.getComponent(i) instanceof ChampMonetaire) {
                        ChampMonetaire chmTotal = (ChampMonetaire) panelTotal.getComponent(i);
                        if (totalNature.equals("0")) totalNature = chmTotal.getText(); else totalType = chmTotal.getText();
                    }
                }
                if (checkEgaliteTotal(totalNature, totalType)) enregistreData(); else JOptionPane.showMessageDialog(null, "Les totaux ne sont pas �gaux!!");
            }
        });
        panel.setLayout(new GridBagLayout());
        panel.add(panelTitre, new GBC(0, 0, 2, 1).setAnchor(GBC.CENTER));
        panel.add(new JScrollPane(panelNature), new GBC(0, 1).setAnchor(GBC.EAST));
        panel.add(new JScrollPane(panelType), new GBC(1, 1).setAnchor(GBC.WEST));
        panel.add(new JScrollPane(panelTotal), new GBC(0, 2, 2, 1).setAnchor(GBC.WEST));
        panel.add(btnEnregistrer, new GBC(0, 3, 2, 1).setAnchor(GBC.CENTER).setInsets(10));
        panel.updateUI();
        updatePage(ladate);
        for (int i = 0; i < panelNature.getComponentCount() - 1; i++) {
            if (panelNature.getComponent(i) instanceof ChampMonetaire) {
                panelNature.getComponent(i).requestFocus();
                break;
            }
        }
    }

    /**
	 * Cree le panel de titre avec la date et
	 * un navigateur par jour et par mois
	 *
	 * @return le JPanel cr�e
	 */
    private JPanel creePanelTitre() {
        JPanel paneltitre = new JPanel();
        JLabel lblTitre = new JLabel("Saisie Ventes Comptant");
        paneltitre.setLayout(new GridBagLayout());
        paneltitre.add(lblTitre, new GBC(0, 0, 5, 1).setAnchor(GBC.CENTER).setInsets(10));
        JButton btnMoisPrec = new JButton("<<");
        JButton btnJourPrec = new JButton("<");
        final Calendar calendar = Calendar.getInstance();
        int mois = calendar.get(Calendar.MONTH) + 1;
        String madate = calendar.get(Calendar.DATE) + "/" + mois + "/" + calendar.get(Calendar.YEAR);
        try {
            ladate = ActionUtils.stringToDate(madate, "dd/MM/yy");
        } catch (Exception e) {
            Erreurs.Warning("" + e);
        }
        lblDate = new JLabel(listeNomJour[calendar.get(Calendar.DAY_OF_WEEK)] + " " + new SimpleDateFormat("dd/MM/yy").format(ladate));
        JButton btnJourSuiv = new JButton(">");
        JButton btnMoisSuiv = new JButton(">>");
        paneltitre.add(btnMoisPrec, new GBC(0, 1).setAnchor(GBC.CENTER).setInsets(0, 0, 10, 5));
        paneltitre.add(btnJourPrec, new GBC(1, 1).setAnchor(GBC.CENTER).setInsets(0, 0, 10, 5));
        paneltitre.add(lblDate, new GBC(2, 1).setAnchor(GBC.CENTER).setInsets(0, 0, 10, 5));
        paneltitre.add(btnJourSuiv, new GBC(3, 1).setAnchor(GBC.CENTER).setInsets(0, 0, 10, 5));
        paneltitre.add(btnMoisSuiv, new GBC(4, 1).setAnchor(GBC.CENTER).setInsets(0, 0, 10, 0));
        btnMoisPrec.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                calendar.add(Calendar.MONTH, -1);
                int mois = calendar.get(Calendar.MONTH) + 1;
                String madate = calendar.get(Calendar.DATE) + "/" + mois + "/" + calendar.get(Calendar.YEAR);
                try {
                    ladate = ActionUtils.stringToDate(madate, "dd/MM/yy");
                } catch (Exception e) {
                    Erreurs.Warning("" + e);
                }
                lblDate.setText("" + listeNomJour[calendar.get(Calendar.DAY_OF_WEEK)] + " " + new SimpleDateFormat("dd/MM/yy").format(ladate));
                updatePage(ladate);
            }
        });
        btnJourPrec.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                calendar.add(Calendar.DATE, -1);
                int mois = calendar.get(Calendar.MONTH) + 1;
                String madate = calendar.get(Calendar.DATE) + "/" + mois + "/" + calendar.get(Calendar.YEAR);
                try {
                    ladate = ActionUtils.stringToDate(madate, "dd/MM/yy");
                } catch (Exception e) {
                    Erreurs.Warning("" + e);
                }
                lblDate.setText("" + listeNomJour[calendar.get(Calendar.DAY_OF_WEEK)] + " " + new SimpleDateFormat("dd/MM/yy").format(ladate));
                updatePage(ladate);
            }
        });
        btnJourSuiv.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                calendar.add(Calendar.DATE, +1);
                int mois = calendar.get(Calendar.MONTH) + 1;
                String madate = calendar.get(Calendar.DATE) + "/" + mois + "/" + calendar.get(Calendar.YEAR);
                try {
                    ladate = ActionUtils.stringToDate(madate, "dd/MM/yy");
                } catch (Exception e) {
                    Erreurs.Warning("" + e);
                }
                lblDate.setText("" + listeNomJour[calendar.get(Calendar.DAY_OF_WEEK)] + " " + new SimpleDateFormat("dd/MM/yy").format(ladate));
                updatePage(ladate);
            }
        });
        btnMoisSuiv.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                calendar.add(Calendar.MONTH, +1);
                int mois = calendar.get(Calendar.MONTH) + 1;
                String madate = calendar.get(Calendar.DATE) + "/" + mois + "/" + calendar.get(Calendar.YEAR);
                try {
                    ladate = ActionUtils.stringToDate(madate, "dd/MM/yy");
                } catch (Exception e) {
                    Erreurs.Warning("" + e);
                }
                lblDate.setText("" + listeNomJour[calendar.get(Calendar.DAY_OF_WEEK)] + " " + new SimpleDateFormat("dd/MM/yy").format(ladate));
                updatePage(ladate);
            }
        });
        return paneltitre;
    }

    /**
	 * Cree le panel de nature pour
	 * la ventilation par nature
	 *
	 * @return le JPanel cr�e
	 */
    private JPanel creePanelNature(Date date) {
        JPanel panelnature = new JPanel();
        JLabel lblTitre = new JLabel("Par Nature");
        panelnature.setLayout(new GridBagLayout());
        List<Categorie> listCategorie = new ArrayList<Categorie>();
        listCategorie = getListCategorie("Nature");
        panelnature.add(lblTitre, new GBC(0, 0, 3, 1).setAnchor(GBC.CENTER).setInsets(0, 0, 20, 0));
        int i = 1;
        for (final Categorie uneCategorie : listCategorie) {
            JLabel lblcat = new JLabel(uneCategorie.getLibelle());
            final ChampMonetaire chmMontantNature = new ChampMonetaire(0, 7, Formatter.getInstance().getDecimalFormat());
            chmMontantNature.getDocument().addDocumentListener(new CalculNature());
            panelnature.add(lblcat, new GBC(0, i).setAnchor(GBC.EAST).setInsets(0, 0, 10, 10));
            panelnature.add(chmMontantNature, new GBC(1, i).setAnchor(GBC.WEST).setInsets(0, 0, 10, 0));
            chmMontantNature.addFocusListener(new FocusListener() {

                public void focusGained(FocusEvent arg0) {
                    if (chmMontantNature.getText().equals("0,00")) {
                        chmMontantNature.setText("");
                    }
                }

                public void focusLost(FocusEvent arg0) {
                    if (chmMontantNature.getText().equals("")) {
                        chmMontantNature.setText("0,00");
                    }
                }
            });
            if (uneCategorie.getOptions() != null && uneCategorie.getOptions().equals("Tiers payant")) {
                JButton btnOption = new JButton("Caisses");
                btnOption.setPreferredSize(new Dimension(80, 20));
                btnOption.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent arg0) {
                        lanceVentilCaisses(lblDate.getText(), chmMontantNature.getText(), uneCategorie.getidCat());
                    }
                });
                panelnature.add(btnOption, new GBC(2, i).setAnchor(GBC.WEST).setInsets(0, 0, 10, 0));
            }
            i++;
        }
        return panelnature;
    }

    /**
	 * Cree le panel de type pour
	 * la ventilation par type
	 *
	 * @return le JPanel cr�e
	 */
    private JPanel creePanelType(Date date) {
        JPanel paneltype = new JPanel();
        JLabel lblTitre = new JLabel("Par Type");
        paneltype.setLayout(new GridBagLayout());
        List<Categorie> listCategorie = new ArrayList<Categorie>();
        listCategorie = getListCategorie("Type");
        paneltype.add(lblTitre, new GBC(0, 0, 3, 1).setAnchor(GBC.CENTER).setInsets(0, 0, 20, 0));
        int i = 1;
        for (final Categorie uneCategorie : listCategorie) {
            JLabel lblcat = new JLabel(uneCategorie.getLibelle());
            final ChampMonetaire chmMontantType = new ChampMonetaire(0, 7, Formatter.getInstance().getDecimalFormat());
            chmMontantType.getDocument().addDocumentListener(new CalculType());
            paneltype.add(lblcat, new GBC(0, i).setAnchor(GBC.EAST).setInsets(0, 0, 10, 10));
            paneltype.add(chmMontantType, new GBC(1, i).setAnchor(GBC.WEST).setInsets(0, 0, 10, 0));
            chmMontantType.addFocusListener(new FocusListener() {

                public void focusGained(FocusEvent arg0) {
                    if (chmMontantType.getText().equals("0,00")) {
                        chmMontantType.setText("");
                    }
                }

                public void focusLost(FocusEvent arg0) {
                    if (chmMontantType.getText().equals("")) {
                        chmMontantType.setText("0,00");
                    }
                }
            });
            if (uneCategorie.getOptions() != null && uneCategorie.getOptions().equals("Remise de ch�ques")) {
                JButton btnOption = new JButton("Remise");
                btnOption.setPreferredSize(new Dimension(80, 20));
                btnOption.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent arg0) {
                        lanceRemiseDeCheques(lblDate.getText(), chmMontantType.getText(), uneCategorie.getidCat());
                    }
                });
                paneltype.add(btnOption, new GBC(2, i).setAnchor(GBC.WEST).setInsets(0, 0, 10, 0));
            }
            i++;
        }
        return paneltype;
    }

    /**
	 * Cr�e le panel pour afficher les totaux
	 * @return
	 */
    private JPanel creePanelTotal() {
        JPanel paneltotal = new JPanel();
        JLabel lblTitreNature = new JLabel("TOTAL : ");
        paneltotal.setLayout(new GridBagLayout());
        chmTotalNature = new ChampMonetaire(0, 7, Formatter.getInstance().getDecimalFormat());
        JLabel lblTitreType = new JLabel("TOTAL : ");
        chmTotalType = new ChampMonetaire(0, 7, Formatter.getInstance().getDecimalFormat());
        chmTotalNature.setBackground(panel.getBackground());
        chmTotalNature.setEditable(false);
        chmTotalType.setBackground(panel.getBackground());
        chmTotalType.setEditable(false);
        paneltotal.add(lblTitreNature, new GBC(0, 0).setAnchor(GBC.EAST).setInsets(0, 0, 0, 5));
        paneltotal.add(chmTotalNature, new GBC(1, 0).setAnchor(GBC.EAST));
        paneltotal.add(lblTitreType, new GBC(2, 0).setAnchor(GBC.EAST).setInsets(0, 175, 0, 5));
        paneltotal.add(chmTotalType, new GBC(3, 0).setAnchor(GBC.EAST).setInsets(0, 0, 0, 50));
        return paneltotal;
    }

    /**
	 *
	 * @author grabriel
	 *
	 */
    class CalculType implements DocumentListener {

        public void insertUpdate(DocumentEvent arg0) {
            this.update(arg0);
        }

        public void removeUpdate(DocumentEvent arg0) {
            this.update(arg0);
        }

        public void changedUpdate(DocumentEvent arg0) {
            this.update(arg0);
        }

        protected void update(DocumentEvent arg0) {
            Component[] tab = panelType.getComponents();
            ChampMonetaire chmMon;
            BigDecimal bdMttType = new BigDecimal(0);
            BigDecimal bdMtt = new BigDecimal(0);
            for (int i = 0; i < panelType.getComponentCount(); i++) {
                Component element = tab[i];
                if (element instanceof ChampMonetaire) {
                    chmMon = (ChampMonetaire) element;
                    Number nombre = 0;
                    bdMtt = new BigDecimal(0);
                    if (!chmMon.getText().equals("")) {
                        try {
                            nombre = format.parse(chmMon.getText().replace(".", ","));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        bdMtt = new BigDecimal(nombre.toString());
                    }
                    bdMttType = bdMttType.add(bdMtt);
                }
            }
            chmTotalType.setText(format.format(bdMttType, new StringBuffer(), new FieldPosition(0)).toString());
        }
    }

    /**
	 *
	 * @author grabriel
	 *
	 */
    class CalculNature implements DocumentListener {

        public void insertUpdate(DocumentEvent arg0) {
            this.update(arg0);
        }

        public void removeUpdate(DocumentEvent arg0) {
            this.update(arg0);
        }

        public void changedUpdate(DocumentEvent arg0) {
            this.update(arg0);
        }

        protected void update(DocumentEvent arg0) {
            Component[] tab = panelNature.getComponents();
            ChampMonetaire chmMon;
            BigDecimal bdMttNature = new BigDecimal(0);
            BigDecimal bdMtt = new BigDecimal(0);
            for (int i = 0; i < panelNature.getComponentCount(); i++) {
                Component element = tab[i];
                if (element instanceof ChampMonetaire) {
                    chmMon = (ChampMonetaire) element;
                    Number nombre = 0;
                    bdMtt = new BigDecimal(0);
                    if (!chmMon.getText().equals("")) {
                        try {
                            nombre = format.parse(chmMon.getText().replace(".", ","));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        bdMtt = new BigDecimal(nombre.toString());
                    }
                    bdMttNature = bdMttNature.add(bdMtt);
                }
            }
            chmTotalNature.setText(format.format(bdMttNature, new StringBuffer(), new FieldPosition(0)).toString());
        }
    }

    /**
	 *
	 * @param date
	 * @param mtt
	 */
    private void lanceRemiseDeCheques(String date, String mtt, int idcat) {
        JFrame fenetreRemise = new FrameRemiseDeCheques(date, mtt, idcat);
        fenetreRemise.setVisible(true);
    }

    /**
	 *
	 * @param date
	 */
    private void lanceVentilCaisses(String date, String mtt, int idcat) {
        JFrame fenetreCaisse = new FrameVentilCaisses(date, mtt, idcat);
        fenetreCaisse.setVisible(true);
    }

    /**
	 * V�rifie que les totaux sont �gaux
	 *
	 * @return
	 */
    private boolean checkEgaliteTotal(String totalA, String totalB) {
        boolean result = false;
        if (totalA.equals(totalB) && !totalA.equals("0,00")) result = true;
        return result;
    }

    /**
	 * Enregistre les infos en ecritures
	 *
	 */
    private void enregistreData() {
        DaoDevise daoDevise = new DaoDevise();
        int idEcriture = 0;
        int idjou = 1;
        Ecriture uneEcriture = ecritureDejaPassee(ladate);
        if (uneEcriture != null) {
            try {
                daoEcritureLigne.supprimeEcritureLigneByIdEcriture(uneEcriture.getIdEcriture());
            } catch (BusinessException be) {
                Erreurs.Warning(be);
            }
            idEcriture = uneEcriture.getIdEcriture();
        } else {
            try {
                idEcriture = daoEcriture.getNewIdEcriture();
            } catch (BusinessException be) {
                Erreurs.Warning(be);
            }
            try {
                daoEcriture.ajouteEcriture(new Ecriture(idEcriture, new SimpleDateFormat("yyyy-MM-dd").format(ladate), daoDevise.getDeviseById(ActionConstants.ATT_PARAM_IDDEV).getNom_devise(), "b", 0, false, ActionConstants.ATT_PARAM_IDEXE, idjou, ActionConstants.ATT_PARAM_IDDEV, 0, ""));
            } catch (BusinessException be) {
                Erreurs.Warning(be);
            }
        }
        List<Categorie> listCategorieNat = getListCategorie("Nature");
        BigDecimal bdMttNature = new BigDecimal(0);
        for (Categorie uneCat : listCategorieNat) {
            ChampMonetaire chmMtt = null;
            for (int i = 0; i < panelNature.getComponentCount(); i++) {
                if (panelNature.getComponent(i) instanceof JLabel) {
                    JLabel unLabel = (JLabel) panelNature.getComponent(i);
                    if (unLabel.getText().equals(uneCat.getLibelle())) {
                        int a = i + 1;
                        chmMtt = (ChampMonetaire) panelNature.getComponent(a);
                    }
                }
            }
            if (chmMtt != null && !chmMtt.getText().equals("0,00")) {
                Number nombre = 0;
                try {
                    nombre = format.parse(chmMtt.getText().replace(".", ","));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                bdMttNature = new BigDecimal(nombre.toString());
                try {
                    EcritureLigne uneEcritureLigne = new EcritureLigne(daoEcritureLigne.getNewIdEcritureLigne(), "Ventes comptant pour le " + new SimpleDateFormat("dd-MM-yyyy").format(ladate), bdMttNature, false, "+", "0000", new BigDecimal(0), false, new BigDecimal(1), uneCat.getIdCom(), ActionConstants.ATT_PARAM_IDTVAZ, idEcriture);
                    daoEcritureLigne.ajouteEcritureLigne(uneEcritureLigne);
                } catch (BusinessException be) {
                    Erreurs.Warning(be);
                }
            }
        }
        List<Categorie> listCategorieTyp = getListCategorie("Type");
        BigDecimal bdMttType = new BigDecimal(0);
        for (Categorie uneCat : listCategorieTyp) {
            ChampMonetaire chmMtt = null;
            for (int i = 0; i < panelType.getComponentCount(); i++) {
                if (panelType.getComponent(i) instanceof JLabel) {
                    JLabel unLabel = (JLabel) panelType.getComponent(i);
                    if (unLabel.getText().equals(uneCat.getLibelle())) {
                        int a = i + 1;
                        chmMtt = (ChampMonetaire) panelType.getComponent(a);
                    }
                }
            }
            if (chmMtt != null && !chmMtt.getText().equals("0,00")) {
                Number nombre = 0;
                try {
                    nombre = format.parse(chmMtt.getText().replace(".", ","));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                bdMttType = new BigDecimal(nombre.toString());
                try {
                    EcritureLigne uneEcritureLigne = new EcritureLigne(daoEcritureLigne.getNewIdEcritureLigne(), "Ventes comptant pour le " + new SimpleDateFormat("dd-MM-yyyy").format(ladate), bdMttType, true, "+", "0000", new BigDecimal(0), false, new BigDecimal(1), uneCat.getIdCom(), ActionConstants.ATT_PARAM_IDTVAZ, idEcriture);
                    daoEcritureLigne.ajouteEcritureLigne(uneEcritureLigne);
                } catch (BusinessException be) {
                    Erreurs.Warning(be);
                }
            }
        }
        JOptionPane.showMessageDialog(null, "Ecriture Enregistr�e");
        updatePage(ladate);
    }

    /**
	 *
	 * @param titre
	 * @return
	 */
    private List<Categorie> getListCategorie(String titre) {
        List<Categorie> listCategorie = new ArrayList<Categorie>();
        try {
            listCategorie = daoCategorie.getAllCategoriesByCategorie(titre);
        } catch (BusinessException be) {
            Erreurs.Warning(be);
        }
        return listCategorie;
    }

    /**
	 *
	 * @param ladate
	 */
    private void updatePage(Date ladate) {
        upadtePanelType(ladate);
        upadtePanelNature(ladate);
        panelType.updateUI();
        panelNature.updateUI();
    }

    /**
	 *
	 * @param ladate
	 */
    private void upadtePanelNature(Date ladate) {
        Object tabMtt[][] = null;
        Ecriture uneEcriture = ecritureDejaPassee(ladate);
        if (uneEcriture != null) {
            List<EcritureLigne> listEcritureLigne = new ArrayList<EcritureLigne>();
            try {
                listEcritureLigne = daoEcritureLigne.getAllEcritureLignesByIdEcriture(uneEcriture.getIdEcriture());
            } catch (BusinessException be) {
                Erreurs.Warning(be);
            }
            tabMtt = new Object[listEcritureLigne.size()][2];
            int i = 0;
            for (EcritureLigne uneEcritureligne : listEcritureLigne) {
                tabMtt[i][0] = uneEcritureligne.getEidCompte();
                tabMtt[i][1] = uneEcritureligne.getMontant();
                i++;
            }
        }
        Component[] tab = panelNature.getComponents();
        ChampMonetaire chmMon;
        Number montant = 0;
        JButton btnOption;
        boolean option = false;
        for (int i = 0; i < panelNature.getComponentCount(); i++) {
            Component element = tab[i];
            if (tabMtt != null) {
                if (element instanceof JLabel) {
                    List<Categorie> listCategorie = new ArrayList<Categorie>();
                    listCategorie = getListCategorie("Nature");
                    for (Categorie uneCategorie : listCategorie) {
                        if (((JLabel) element).getText().equals(uneCategorie.getLibelle())) {
                            montant = 0;
                            for (int o = 0; o < tabMtt.length; o++) {
                                if ((Integer) tabMtt[o][0] == uneCategorie.getIdCom()) {
                                    montant = (Number) tabMtt[o][1];
                                    if (uneCategorie.getLibelle().equals("Tiers payant") && montant.intValue() > 0) {
                                        option = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (element instanceof ChampMonetaire) {
                chmMon = (ChampMonetaire) element;
                if (tabMtt == null) chmMon.setText("0,00"); else {
                    chmMon.setText(format.format(montant, new StringBuffer(), new FieldPosition(0)).toString());
                }
            }
            if (element instanceof JButton) {
                btnOption = (JButton) element;
                btnOption.setEnabled(option);
                option = false;
            }
        }
    }

    /**
	 *
	 * @param ladate
	 */
    private void upadtePanelType(Date ladate) {
        Object tabMtt[][] = null;
        Ecriture uneEcriture = ecritureDejaPassee(ladate);
        if (uneEcriture != null) {
            List<EcritureLigne> listEcritureLigne = new ArrayList<EcritureLigne>();
            try {
                listEcritureLigne = daoEcritureLigne.getAllEcritureLignesByIdEcriture(uneEcriture.getIdEcriture());
            } catch (BusinessException be) {
                Erreurs.Warning(be);
            }
            tabMtt = new Object[listEcritureLigne.size()][2];
            int i = 0;
            for (EcritureLigne uneEcritureligne : listEcritureLigne) {
                tabMtt[i][0] = uneEcritureligne.getEidCompte();
                tabMtt[i][1] = uneEcritureligne.getMontant();
                i++;
            }
        }
        Component[] tab = panelType.getComponents();
        ChampMonetaire chmMon;
        Number montant = 0;
        JButton btnOption;
        boolean option = false;
        for (int i = 0; i < panelType.getComponentCount(); i++) {
            Component element = tab[i];
            if (tabMtt != null) {
                if (element instanceof JLabel) {
                    List<Categorie> listCategorie = new ArrayList<Categorie>();
                    listCategorie = getListCategorie("Type");
                    for (Categorie uneCategorie : listCategorie) {
                        if (((JLabel) element).getText().equals(uneCategorie.getLibelle())) {
                            montant = 0;
                            for (int o = 0; o < tabMtt.length; o++) {
                                if ((Integer) tabMtt[o][0] == uneCategorie.getIdCom()) {
                                    montant = (Number) tabMtt[o][1];
                                    if (uneCategorie.getLibelle().equals("Ch�ques") && montant.intValue() > 0) {
                                        option = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (element instanceof ChampMonetaire) {
                chmMon = (ChampMonetaire) element;
                if (tabMtt == null) chmMon.setText("0,00"); else {
                    chmMon.setText(format.format(montant, new StringBuffer(), new FieldPosition(0)).toString());
                }
            }
            if (element instanceof JButton) {
                btnOption = (JButton) element;
                btnOption.setEnabled(option);
                option = false;
            }
        }
    }

    /**
	 *
	 * @param ladate
	 * @return
	 */
    private Ecriture ecritureDejaPassee(Date ladate) {
        Ecriture uneEcriture = null;
        try {
            uneEcriture = daoEcriture.getEcritureVenteComptantByDate(ladate);
        } catch (BusinessException be) {
            Erreurs.Warning(be);
        }
        return uneEcriture;
    }

    private void navig(JPanel panelNature, JPanel panelType, final JButton btnenr) {
        final List<ChampMonetaire> listchamp = new ArrayList<ChampMonetaire>();
        for (int i = 0; i < panelNature.getComponentCount(); i++) {
            if (panelNature.getComponent(i) instanceof ChampMonetaire) listchamp.add((ChampMonetaire) panelNature.getComponent(i));
        }
        for (int i = 0; i < panelType.getComponentCount(); i++) {
            if (panelType.getComponent(i) instanceof ChampMonetaire) listchamp.add((ChampMonetaire) panelType.getComponent(i));
        }
        if (listchamp.size() > 0) {
            for (int i = 0; i < listchamp.size() - 1; i++) {
                final ChampMonetaire champ = listchamp.get(i + 1);
                listchamp.get(i).addKeyListener(new KeyAdapter() {

                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            champ.requestFocus();
                        }
                    }
                });
            }
            listchamp.get(listchamp.size() - 1).addKeyListener(new KeyAdapter() {

                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        btnenr.requestFocus();
                    }
                }
            });
            btnenr.addKeyListener(new KeyAdapter() {

                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        btnenr.doClick();
                    }
                }
            });
        }
    }
}
