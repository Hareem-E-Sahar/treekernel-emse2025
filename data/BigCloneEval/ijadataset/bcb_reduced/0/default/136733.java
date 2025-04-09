import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import forge_client.ImageLoader;

class OngletMonstres extends JPanel {

    private static final long serialVersionUID = 1L;

    FenetreSimple parent;

    DefaultListModel Monstremodel;

    public JList MonstreList;

    JScrollPane scrollpanemonlist;

    JPanel paneunmonstre;

    JLabel paneimg;

    JTextField Ed_Nom, Ed_W, Ed_H, Ed_Chipset, Ed_Vitesse;

    JTextField Ed_SonAttaque, Ed_SonBlesse, Ed_SonMagie;

    JTextField Ed_Vie, Ed_Attaque, Ed_Esquive, Ed_Defense, Ed_Dommage;

    JTextField Ed_XPMin, Ed_XPMax, Ed_GoldMin, Ed_GoldMax;

    JTextField Ed_VarSpecial, Ed_ResSpecial, Ed_Lvl;

    JButton Bt_ChooseSonAttaque, Bt_ChooseSonBlesse, Bt_ChooseSonMagie;

    JComboBox CB_TypeMonstre, CB_ClasseMonstre;

    JCheckBox Ch_Bloquant;

    JTable GainObj, Sort;

    ObjGridModel go;

    MagGridModel gm;

    Image imchipset, imaffiche;

    boolean AllowSave;

    public OngletMonstres(FenetreSimple p) {
        setLayout(null);
        parent = p;
        Font font = new Font("MS Sans Serif", Font.PLAIN, 10);
        Monstremodel = new DefaultListModel();
        MonstreList = new JList(Monstremodel);
        MonstreList.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                if (MonstreList.getSelectedIndex() >= 0) {
                    paneunmonstre.setVisible(true);
                    AllowSave = false;
                    Ed_Nom.setText(parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).Name);
                    Ed_Chipset.setText(parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).Chipset);
                    Ed_W.setText(Integer.toString(parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).W));
                    Ed_H.setText(Integer.toString(parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).H));
                    Ed_Vitesse.setText(Integer.toString(parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).Vitesse));
                    Ed_SonAttaque.setText(parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).SoundAttaque);
                    Ed_SonBlesse.setText(parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).SoundWound);
                    Ed_SonMagie.setText(parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).SoundConcentration);
                    Ed_Vie.setText(Integer.toString(parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).Vie));
                    Ed_Lvl.setText(Integer.toString(parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).Lvl));
                    Ed_Attaque.setText(Integer.toString(parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).Attaque));
                    Ed_Esquive.setText(Integer.toString(parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).Esquive));
                    Ed_Defense.setText(Integer.toString(parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).Defense));
                    Ed_Dommage.setText(Integer.toString(parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).Dommage));
                    Ed_XPMin.setText(Integer.toString(parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).XPMin));
                    Ed_XPMax.setText(Integer.toString(parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).XPMax));
                    Ed_GoldMin.setText(Integer.toString(parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).GoldMin));
                    Ed_GoldMax.setText(Integer.toString(parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).GoldMax));
                    Ed_VarSpecial.setText(parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).VarSpecial);
                    Ed_ResSpecial.setText(Integer.toString(parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).ResSpecial));
                    CB_TypeMonstre.setSelectedIndex(parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).TypeMonstre);
                    CB_ClasseMonstre.setSelectedIndex(parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).ClasseMonstre);
                    Ch_Bloquant.setSelected(parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).Bloquant);
                    go = new ObjGridModel();
                    GainObj.setModel(go);
                    gm = new MagGridModel();
                    Sort.setModel(gm);
                    for (int i = 0; i < GainObj.getColumnCount(); i++) {
                        TableColumn col = GainObj.getColumnModel().getColumn(i);
                        if (i == 1) {
                            col.setPreferredWidth(163);
                            String[] values = new String[parent.general.getObjets().size() + 1];
                            for (int j = 0; j < parent.general.getObjets().size(); j++) values[j + 1] = parent.general.getObjetByIndex(j).Name;
                            values[0] = "Pas d'objet";
                            col.setCellEditor(new MyComboBoxEditor(values));
                            col.setCellRenderer(new MyComboBoxRenderer(values));
                        } else col.setPreferredWidth(30);
                    }
                    for (int i = 0; i < Sort.getColumnCount(); i++) {
                        TableColumn col = Sort.getColumnModel().getColumn(i);
                        if (i == 1) {
                            col.setPreferredWidth(163);
                            String[] values = new String[parent.general.getMagies().size() + 1];
                            for (int j = 0; j < parent.general.getMagies().size(); j++) values[j + 1] = parent.general.getMagieByIndex(j).Name;
                            values[0] = "Pas de magie";
                            col.setCellEditor(new MyComboBoxEditor(values));
                            col.setCellRenderer(new MyComboBoxRenderer(values));
                        } else col.setPreferredWidth(30);
                    }
                    GainObj.updateUI();
                    Sort.updateUI();
                    LoadImage();
                    AllowSave = true;
                }
            }
        });
        scrollpanemonlist = new JScrollPane(MonstreList);
        scrollpanemonlist.setBounds(new Rectangle(10, 10, 190, 470));
        add(scrollpanemonlist);
        JButton Bt_AjouteMonstre = new JButton("Ajouter un monstre");
        Bt_AjouteMonstre.setBounds(new Rectangle(205, 10, 180, 20));
        add(Bt_AjouteMonstre);
        Bt_AjouteMonstre.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String NomMonstre = JOptionPane.showInputDialog(null, "Entrez le nom du nouveau monstre", "", 1);
                if (NomMonstre != null) {
                    if (NomMonstre.compareTo("") != 0) {
                        Monstremodel.add(MonstreList.getModel().getSize(), NomMonstre);
                        parent.general.getMonstres().add(parent.general.new Monstre(NomMonstre));
                    }
                }
            }
        });
        JButton Bt_RetireMonstre = new JButton("Retirer un monstre");
        Bt_RetireMonstre.setBounds(new Rectangle(400, 10, 180, 20));
        Bt_RetireMonstre.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (MonstreList.getSelectedIndex() >= 0) {
                    if (JOptionPane.showConfirmDialog(null, "Etes vous sûr de vouloir effacer ce monstre?", "Effacer", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                        parent.general.getMonstres().remove(MonstreList.getSelectedIndex());
                        Monstremodel.remove(MonstreList.getSelectedIndex());
                        paneunmonstre.setVisible(false);
                    }
                }
            }
        });
        add(Bt_RetireMonstre);
        paneunmonstre = new JPanel();
        paneunmonstre.setLayout(null);
        paneunmonstre.setBounds(new Rectangle(205, 35, 580, 550));
        paneunmonstre.setVisible(false);
        add(paneunmonstre);
        paneimg = new JLabel();
        paneimg.setBorder(BorderFactory.createLoweredBevelBorder());
        paneimg.setLayout(null);
        paneimg.setBounds(new Rectangle(0, 0, 112, 110));
        paneimg.setVerticalAlignment(JLabel.TOP);
        paneimg.setHorizontalAlignment(JLabel.LEFT);
        paneunmonstre.add(paneimg);
        JLabel NomMonstre = new JLabel("Nom : ");
        NomMonstre.setBounds(new Rectangle(120, 0, 200, 20));
        paneunmonstre.add(NomMonstre);
        JLabel ClasseMonstre = new JLabel("Classe : ");
        ClasseMonstre.setBounds(new Rectangle(120, 20, 200, 20));
        paneunmonstre.add(ClasseMonstre);
        JLabel TypeMonstre = new JLabel("Type : ");
        TypeMonstre.setBounds(new Rectangle(120, 40, 200, 20));
        paneunmonstre.add(TypeMonstre);
        JLabel Chipset = new JLabel("Chipset : ");
        Chipset.setBounds(new Rectangle(120, 60, 200, 20));
        paneunmonstre.add(Chipset);
        KeyListener keyListener = new KeyListener() {

            public void keyTyped(KeyEvent keyEvent) {
            }

            public void keyPressed(KeyEvent keyEvent) {
            }

            public void keyReleased(KeyEvent keyEvent) {
                if ((keyEvent.getSource().equals(Ed_W)) || (keyEvent.getSource().equals(Ed_H))) RefreshImage();
                SaveMonstre();
            }
        };
        Ed_Nom = new JTextField();
        Ed_Nom.setBounds(new Rectangle(210, 0, 340, 20));
        Ed_Nom.addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent keyEvent) {
            }

            public void keyPressed(KeyEvent keyEvent) {
            }

            public void keyReleased(KeyEvent e) {
                parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).Name = Ed_Nom.getText();
                Monstremodel.set(MonstreList.getSelectedIndex(), Ed_Nom.getText());
            }
        });
        paneunmonstre.add(Ed_Nom);
        CB_ClasseMonstre = new JComboBox(new String[] { "Aucune classe existante" });
        CB_ClasseMonstre.setBounds(new Rectangle(210, 20, 340, 20));
        CB_ClasseMonstre.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SaveMonstre();
            }
        });
        paneunmonstre.add(CB_ClasseMonstre);
        CB_TypeMonstre = new JComboBox(new String[] { "N'attaque que si attaqué", "Attaque toujours", "Monstre statique", "Objet destructible" });
        CB_TypeMonstre.setBounds(new Rectangle(210, 40, 340, 20));
        CB_TypeMonstre.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SaveMonstre();
            }
        });
        paneunmonstre.add(CB_TypeMonstre);
        Ed_Chipset = new JTextField();
        Ed_Chipset.setBounds(new Rectangle(230, 60, 260, 20));
        Ed_Chipset.addKeyListener(keyListener);
        paneunmonstre.add(Ed_Chipset);
        JButton Bt_ChooseChipset = new JButton("...");
        Bt_ChooseChipset.setBounds(new Rectangle(491, 60, 18, 20));
        Bt_ChooseChipset.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JFileChooser choix = new JFileChooser();
                choix.addChoosableFileFilter(parent.new FilterImage());
                choix.setCurrentDirectory(new java.io.File(parent.NomCarte + "/Chipset"));
                int retour = choix.showOpenDialog(null);
                if (retour == JFileChooser.APPROVE_OPTION) {
                    if (!new File(parent.NomCarte + "/Chipset/" + choix.getSelectedFile().getName()).exists()) parent.copyfile(choix.getSelectedFile().getAbsolutePath(), parent.NomCarte + "/Chipset/" + choix.getSelectedFile().getName());
                    Ed_Chipset.setText("Chipset\\" + choix.getSelectedFile().getName());
                    SaveMonstre();
                    LoadImage();
                }
            }
        });
        paneunmonstre.add(Bt_ChooseChipset);
        JLabel Lbl_W = new JLabel("W :");
        Lbl_W.setFont(font);
        Lbl_W.setBounds(new Rectangle(0, 112, 30, 17));
        paneunmonstre.add(Lbl_W);
        Ed_W = new JTextField();
        Ed_W.setFont(font);
        Ed_W.setBounds(new Rectangle(20, 112, 33, 17));
        Ed_W.addKeyListener(keyListener);
        paneunmonstre.add(Ed_W);
        JLabel Lbl_H = new JLabel("H :");
        Lbl_H.setFont(font);
        Lbl_H.setBounds(new Rectangle(60, 112, 30, 17));
        paneunmonstre.add(Lbl_H);
        Ed_H = new JTextField();
        Ed_H.setFont(font);
        Ed_H.setBounds(new Rectangle(80, 112, 33, 17));
        Ed_H.addKeyListener(keyListener);
        paneunmonstre.add(Ed_H);
        JLabel Lbl_Vitesse = new JLabel("Vitesse :");
        Lbl_Vitesse.setFont(font);
        Lbl_Vitesse.setBounds(new Rectangle(0, 129, 70, 17));
        paneunmonstre.add(Lbl_Vitesse);
        Ed_Vitesse = new JTextField();
        Ed_Vitesse.setFont(font);
        Ed_Vitesse.setBounds(new Rectangle(80, 129, 33, 17));
        Ed_Vitesse.addKeyListener(keyListener);
        paneunmonstre.add(Ed_Vitesse);
        Ch_Bloquant = new JCheckBox("Bloquant");
        Ch_Bloquant.setFont(font);
        Ch_Bloquant.setBounds(new Rectangle(0, 146, 100, 17));
        Ch_Bloquant.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SaveMonstre();
            }
        });
        paneunmonstre.add(Ch_Bloquant);
        JLabel SoundAttaque = new JLabel("Son d'attaque : ");
        SoundAttaque.setBounds(new Rectangle(120, 80, 200, 20));
        paneunmonstre.add(SoundAttaque);
        JLabel SoundWound = new JLabel("Son blessure : ");
        SoundWound.setBounds(new Rectangle(120, 100, 200, 20));
        paneunmonstre.add(SoundWound);
        JLabel SoundConcentration = new JLabel("Son magie : ");
        SoundConcentration.setBounds(new Rectangle(120, 120, 200, 20));
        paneunmonstre.add(SoundConcentration);
        Ed_SonAttaque = new JTextField();
        Ed_SonAttaque.setBounds(new Rectangle(230, 80, 260, 20));
        Ed_SonAttaque.addKeyListener(keyListener);
        paneunmonstre.add(Ed_SonAttaque);
        ActionListener ChooseSound = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JFileChooser choix = new JFileChooser();
                choix.addChoosableFileFilter(parent.new FilterSound());
                choix.setCurrentDirectory(new java.io.File(parent.NomCarte + "/Sound"));
                int retour = choix.showOpenDialog(null);
                if (retour == JFileChooser.APPROVE_OPTION) {
                    if (!new File(parent.NomCarte + "/Sound/" + choix.getSelectedFile().getName()).exists()) parent.copyfile(choix.getSelectedFile().getAbsolutePath(), parent.NomCarte + "/Sound/" + choix.getSelectedFile().getName());
                    JTextField Edit = null;
                    if (e.getSource().equals(Bt_ChooseSonAttaque)) Edit = Ed_SonAttaque; else if (e.getSource().equals(Bt_ChooseSonBlesse)) Edit = Ed_SonBlesse; else if (e.getSource().equals(Bt_ChooseSonMagie)) Edit = Ed_SonMagie;
                    if (Edit != null) Edit.setText("Sound\\" + choix.getSelectedFile().getName());
                    SaveMonstre();
                }
            }
        };
        Bt_ChooseSonAttaque = new JButton("...");
        Bt_ChooseSonAttaque.setBounds(new Rectangle(491, 80, 18, 20));
        Bt_ChooseSonAttaque.addActionListener(ChooseSound);
        paneunmonstre.add(Bt_ChooseSonAttaque);
        Ed_SonBlesse = new JTextField();
        Ed_SonBlesse.setBounds(new Rectangle(230, 100, 260, 20));
        Ed_SonBlesse.addKeyListener(keyListener);
        paneunmonstre.add(Ed_SonBlesse);
        Bt_ChooseSonBlesse = new JButton("...");
        Bt_ChooseSonBlesse.setBounds(new Rectangle(491, 100, 18, 20));
        Bt_ChooseSonBlesse.addActionListener(ChooseSound);
        paneunmonstre.add(Bt_ChooseSonBlesse);
        Bt_ChooseSonMagie = new JButton("...");
        Bt_ChooseSonMagie.setBounds(new Rectangle(491, 120, 18, 20));
        Bt_ChooseSonMagie.addActionListener(ChooseSound);
        paneunmonstre.add(Bt_ChooseSonMagie);
        Ed_SonMagie = new JTextField();
        Ed_SonMagie.setBounds(new Rectangle(230, 120, 260, 20));
        Ed_SonMagie.addKeyListener(keyListener);
        paneunmonstre.add(Ed_SonMagie);
        JLabel Lbl_Attaque = new JLabel("Attaque : ");
        Lbl_Attaque.setBounds(new Rectangle(120, 140, 200, 20));
        paneunmonstre.add(Lbl_Attaque);
        Ed_Attaque = new JTextField();
        Ed_Attaque.setBounds(new Rectangle(210, 140, 90, 20));
        Ed_Attaque.addKeyListener(keyListener);
        paneunmonstre.add(Ed_Attaque);
        JLabel Lbl_Vie = new JLabel("Vie : ");
        Lbl_Vie.setBounds(new Rectangle(305, 140, 200, 20));
        paneunmonstre.add(Lbl_Vie);
        Ed_Vie = new JTextField();
        Ed_Vie.setBounds(new Rectangle(350, 140, 90, 20));
        Ed_Vie.addKeyListener(keyListener);
        paneunmonstre.add(Ed_Vie);
        JLabel Lbl_Lvl = new JLabel("Lvl : ");
        Lbl_Lvl.setBounds(new Rectangle(450, 140, 100, 20));
        paneunmonstre.add(Lbl_Lvl);
        Ed_Lvl = new JTextField();
        Ed_Lvl.setBounds(new Rectangle(475, 140, 90, 20));
        Ed_Lvl.addKeyListener(keyListener);
        paneunmonstre.add(Ed_Lvl);
        JLabel Lbl_Esquive = new JLabel("Esquive : ");
        Lbl_Esquive.setBounds(new Rectangle(120, 160, 100, 20));
        paneunmonstre.add(Lbl_Esquive);
        Ed_Esquive = new JTextField();
        Ed_Esquive.setBounds(new Rectangle(210, 160, 90, 20));
        Ed_Esquive.addKeyListener(keyListener);
        paneunmonstre.add(Ed_Esquive);
        JLabel Lbl_Dommage = new JLabel("Dégat : ");
        Lbl_Dommage.setBounds(new Rectangle(305, 160, 200, 20));
        paneunmonstre.add(Lbl_Dommage);
        Ed_Dommage = new JTextField();
        Ed_Dommage.setBounds(new Rectangle(350, 160, 90, 20));
        Ed_Dommage.addKeyListener(keyListener);
        paneunmonstre.add(Ed_Dommage);
        JLabel Lbl_Defense = new JLabel("Déf : ");
        Lbl_Defense.setBounds(new Rectangle(450, 160, 200, 20));
        paneunmonstre.add(Lbl_Defense);
        Ed_Defense = new JTextField();
        Ed_Defense.setBounds(new Rectangle(475, 160, 90, 20));
        Ed_Defense.addKeyListener(keyListener);
        paneunmonstre.add(Ed_Defense);
        JLabel VarSpecial = new JLabel("Lorsque le monstre meurt, Variable[");
        VarSpecial.setBounds(new Rectangle(120, 180, 300, 20));
        paneunmonstre.add(VarSpecial);
        Ed_VarSpecial = new JTextField();
        Ed_VarSpecial.setBounds(new Rectangle(350, 180, 90, 20));
        Ed_VarSpecial.addKeyListener(keyListener);
        paneunmonstre.add(Ed_VarSpecial);
        JLabel ResSpecial = new JLabel("]=");
        ResSpecial.setBounds(new Rectangle(450, 180, 50, 20));
        paneunmonstre.add(ResSpecial);
        Ed_ResSpecial = new JTextField();
        Ed_ResSpecial.setBounds(new Rectangle(475, 180, 90, 20));
        Ed_ResSpecial.addKeyListener(keyListener);
        paneunmonstre.add(Ed_ResSpecial);
        JPanel panegain = new JPanel();
        TitledBorder title = BorderFactory.createTitledBorder("Gain du joueur");
        title.setTitleFont(font);
        panegain.setBorder(title);
        panegain.setBounds(new Rectangle(0, 200, 150, 80));
        panegain.setLayout(null);
        paneunmonstre.add(panegain);
        JLabel Lbl_XP = new JLabel("XP :");
        Lbl_XP.setFont(font);
        Lbl_XP.setBounds(new Rectangle(5, 20, 30, 17));
        panegain.add(Lbl_XP);
        Ed_XPMin = new JTextField();
        Ed_XPMin.setFont(font);
        Ed_XPMin.setBounds(new Rectangle(30, 20, 30, 17));
        Ed_XPMin.addKeyListener(keyListener);
        panegain.add(Ed_XPMin);
        JLabel Lbl_Fleche = new JLabel("->");
        Lbl_Fleche.setFont(font);
        Lbl_Fleche.setBounds(new Rectangle(70, 20, 30, 17));
        panegain.add(Lbl_Fleche);
        Lbl_Fleche = new JLabel("->");
        Lbl_Fleche.setFont(font);
        Lbl_Fleche.setBounds(new Rectangle(70, 40, 30, 17));
        panegain.add(Lbl_Fleche);
        Ed_XPMax = new JTextField();
        Ed_XPMax.setFont(font);
        Ed_XPMax.setBounds(new Rectangle(100, 20, 30, 17));
        Ed_XPMax.addKeyListener(keyListener);
        panegain.add(Ed_XPMax);
        JLabel Lbl_Gold = new JLabel("Or :");
        Lbl_Gold.setFont(font);
        Lbl_Gold.setBounds(new Rectangle(5, 40, 30, 17));
        panegain.add(Lbl_Gold);
        Ed_GoldMin = new JTextField();
        Ed_GoldMin.setFont(font);
        Ed_GoldMin.setBounds(new Rectangle(30, 40, 30, 17));
        Ed_GoldMin.addKeyListener(keyListener);
        panegain.add(Ed_GoldMin);
        Ed_GoldMax = new JTextField();
        Ed_GoldMax.setFont(font);
        Ed_GoldMax.setBounds(new Rectangle(100, 40, 30, 17));
        Ed_GoldMax.addKeyListener(keyListener);
        panegain.add(Ed_GoldMax);
        JPanel panegainobj = new JPanel();
        title = BorderFactory.createTitledBorder("Gain objet");
        title.setTitleFont(font);
        panegainobj.setBorder(title);
        panegainobj.setBounds(new Rectangle(160, 200, 200, 200));
        panegainobj.setLayout(null);
        paneunmonstre.add(panegainobj);
        JButton Bt_AddRowObj = new JButton("+");
        Bt_AddRowObj.setBounds(new Rectangle(10, 17, 90, 20));
        Bt_AddRowObj.setToolTipText("Ajouter une récompense");
        Bt_AddRowObj.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).PercentWin.add((short) 0);
                parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).ObjectWin.add((short) 0);
                GainObj.updateUI();
            }
        });
        panegainobj.add(Bt_AddRowObj);
        JButton Bt_RmRowObj = new JButton("-");
        Bt_RmRowObj.setBounds(new Rectangle(90, 17, 90, 20));
        Bt_RmRowObj.setToolTipText("Enlever une récompense");
        Bt_RmRowObj.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).PercentWin.remove(GainObj.getSelectedRow());
                parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).ObjectWin.remove(GainObj.getSelectedRow());
                GainObj.updateUI();
            }
        });
        panegainobj.add(Bt_RmRowObj);
        go = new ObjGridModel();
        GainObj = new JTable(go);
        GainObj.setRowSelectionAllowed(false);
        GainObj.setColumnSelectionAllowed(false);
        JScrollPane scrollpaneGOG = new JScrollPane(GainObj);
        GainObj.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        GainObj.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        GainObj.setRowHeight(20);
        for (int i = 0; i < GainObj.getColumnCount(); i++) {
            TableColumn col = GainObj.getColumnModel().getColumn(i);
            if (i == 0) col.setPreferredWidth(30); else col.setPreferredWidth(163);
        }
        scrollpaneGOG.setBounds(new Rectangle(2, 37, 196, 160));
        panegainobj.add(scrollpaneGOG);
        JPanel panesort = new JPanel();
        title = BorderFactory.createTitledBorder("Sort");
        title.setTitleFont(font);
        panesort.setBorder(title);
        panesort.setBounds(new Rectangle(370, 200, 200, 200));
        panesort.setLayout(null);
        paneunmonstre.add(panesort);
        JButton Bt_AddRowSort = new JButton("+");
        Bt_AddRowSort.setBounds(new Rectangle(10, 17, 90, 20));
        Bt_AddRowSort.setToolTipText("Ajouter une magie");
        Bt_AddRowSort.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).LuckSpell.add((short) 0);
                parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).Spell.add((short) 0);
                Sort.updateUI();
            }
        });
        panesort.add(Bt_AddRowSort);
        JButton Bt_RmRowSort = new JButton("-");
        Bt_RmRowSort.setBounds(new Rectangle(90, 17, 90, 20));
        Bt_RmRowSort.setToolTipText("Enlever une magie");
        Bt_RmRowSort.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).LuckSpell.remove(Sort.getSelectedRow());
                parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).Spell.remove(Sort.getSelectedRow());
                Sort.updateUI();
            }
        });
        panesort.add(Bt_RmRowSort);
        gm = new MagGridModel();
        Sort = new JTable(gm);
        Sort.setRowSelectionAllowed(false);
        Sort.setColumnSelectionAllowed(false);
        JScrollPane scrollpaneGMG = new JScrollPane(Sort);
        Sort.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        Sort.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Sort.setRowHeight(20);
        for (int i = 0; i < Sort.getColumnCount(); i++) {
            TableColumn col = Sort.getColumnModel().getColumn(i);
            if (i == 0) col.setPreferredWidth(30); else col.setPreferredWidth(163);
        }
        scrollpaneGMG.setBounds(new Rectangle(2, 37, 196, 160));
        panesort.add(scrollpaneGMG);
        ComponentAdapter listener = new ComponentAdapter() {

            public void componentResized(ComponentEvent evt) {
                Component c = (Component) evt.getSource();
                Dimension newSize = c.getSize();
                scrollpanemonlist.setBounds(new Rectangle(10, 10, 190, newSize.height - 105));
            }
        };
        parent.addComponentListener(listener);
    }

    private void SaveMonstre() {
        if (AllowSave) {
            parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).Name = Ed_Nom.getText();
            parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).Chipset = Ed_Chipset.getText();
            parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).W = (short) Integer.parseInt(Ed_W.getText());
            parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).H = (short) Integer.parseInt(Ed_H.getText());
            parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).Vitesse = (short) Integer.parseInt(Ed_Vitesse.getText());
            parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).SoundAttaque = Ed_SonAttaque.getText();
            parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).SoundWound = Ed_SonBlesse.getText();
            parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).SoundConcentration = Ed_SonMagie.getText();
            parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).Vie = Integer.parseInt(Ed_Vie.getText());
            parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).Lvl = Integer.parseInt(Ed_Lvl.getText());
            parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).Attaque = (short) Integer.parseInt(Ed_Attaque.getText());
            parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).Esquive = (short) Integer.parseInt(Ed_Esquive.getText());
            parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).Defense = (short) Integer.parseInt(Ed_Defense.getText());
            parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).Dommage = (short) Integer.parseInt(Ed_Dommage.getText());
            parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).XPMin = Integer.parseInt(Ed_XPMin.getText());
            parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).XPMax = Integer.parseInt(Ed_XPMax.getText());
            parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).GoldMin = Integer.parseInt(Ed_GoldMin.getText());
            parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).GoldMax = Integer.parseInt(Ed_GoldMax.getText());
            parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).VarSpecial = Ed_VarSpecial.getText();
            parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).ResSpecial = Integer.parseInt(Ed_ResSpecial.getText());
            parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).TypeMonstre = (short) CB_TypeMonstre.getSelectedIndex();
            parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).ClasseMonstre = (short) CB_ClasseMonstre.getSelectedIndex();
            parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).Bloquant = Ch_Bloquant.isSelected();
        }
    }

    private void LoadImage() {
        if (Ed_Chipset.getText().compareTo("") != 0) {
            ImageLoader im = new ImageLoader(null);
            imchipset = im.loadImage(parent.general.getName() + "/" + Ed_Chipset.getText().replace("Chipset\\", "Chipset/"));
        } else {
            imchipset = null;
            imaffiche = null;
        }
        RefreshImage();
    }

    private void RefreshImage() {
        if (imchipset != null) {
            ImageFilter cif2 = new CropImageFilter(Integer.parseInt(Ed_W.getText()), Integer.parseInt(Ed_H.getText()) * 2, Integer.parseInt(Ed_W.getText()), Integer.parseInt(Ed_H.getText()));
            imaffiche = createImage(new FilteredImageSource(imchipset.getSource(), cif2));
            paneimg.setIcon(new ImageIcon(imaffiche.getScaledInstance(Integer.parseInt(Ed_W.getText()) * 2, Integer.parseInt(Ed_H.getText()) * 2, Image.SCALE_SMOOTH)));
        } else paneimg.setIcon(null);
    }

    public void StatsBaseChange() {
        if (parent.general != null) {
            paneunmonstre.setVisible(false);
            CB_ClasseMonstre.removeAllItems();
            if (parent.general.getClassesMonstre().size() > 0) {
                for (int i = 0; i < parent.general.getClassesMonstre().size(); i++) {
                    CB_ClasseMonstre.addItem(parent.general.getClassesMonstre().get(i).Name);
                }
            } else CB_ClasseMonstre.addItem("Aucune classe existante");
            Monstremodel.clear();
            for (int i = 0; i < parent.general.getMonstres().size(); i++) Monstremodel.add(i, parent.general.getMonstreByIndex(i).Name);
            for (int i = 0; i < GainObj.getColumnCount(); i++) {
                TableColumn col = GainObj.getColumnModel().getColumn(i);
                if (i == 1) {
                    String[] values = new String[parent.general.getObjets().size() + 1];
                    for (int j = 0; j < parent.general.getObjets().size(); j++) values[j + 1] = parent.general.getObjetByIndex(j).Name;
                    values[0] = "Pas d'objet";
                    col.setCellEditor(new MyComboBoxEditor(values));
                    col.setCellRenderer(new MyComboBoxRenderer(values));
                }
            }
            for (int i = 0; i < Sort.getColumnCount(); i++) {
                TableColumn col = Sort.getColumnModel().getColumn(i);
                if (i == 1) {
                    String[] values = new String[parent.general.getMagies().size() + 1];
                    for (int j = 0; j < parent.general.getMagies().size(); j++) values[j + 1] = parent.general.getMagieByIndex(j).Name;
                    values[0] = "Pas de magie";
                    col.setCellEditor(new MyComboBoxEditor(values));
                    col.setCellRenderer(new MyComboBoxRenderer(values));
                }
            }
        }
    }

    public class ObjGridModel extends AbstractTableModel {

        private static final long serialVersionUID = 1L;

        public String getColumnName(int column) {
            switch(column) {
                case 0:
                    return "%";
                case 1:
                    return "Objet";
                default:
                    return "unavailable";
            }
        }

        public int getColumnCount() {
            return 2;
        }

        public int getRowCount() {
            if (parent.general != null) return parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).ObjectWin.size(); else return 0;
        }

        public String getValueAt(int row, int col) {
            switch(col) {
                case 0:
                    return Integer.toString(parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).PercentWin.get(row));
                case 1:
                    if (parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).ObjectWin.get(row) == 0) return "Pas d'objet"; else return parent.general.getObjetByIndex(parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).ObjectWin.get(row) - 1).Name;
                default:
                    return "unavailable";
            }
        }

        public boolean isCellEditable(int row, int col) {
            return true;
        }

        public void setValueAt(Object value, int row, int col) {
            switch(col) {
                case 0:
                    parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).PercentWin.set(row, (short) Integer.parseInt((String) value));
                    break;
                case 1:
                    if (((String) value).compareTo("Pas d'objet") == 0) parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).ObjectWin.set(row, (short) 0); else for (int i = 0; i < parent.general.getObjets().size(); i++) {
                        if (parent.general.getObjetByIndex(i).Name.compareTo((String) value) == 0) parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).ObjectWin.set(row, (short) (i + 1));
                    }
                    break;
            }
            fireTableCellUpdated(row, col);
        }
    }

    ;

    public class MagGridModel extends AbstractTableModel {

        private static final long serialVersionUID = 1L;

        public String getColumnName(int column) {
            switch(column) {
                case 0:
                    return "%";
                case 1:
                    return "Magie";
                default:
                    return "unavailable";
            }
        }

        public int getColumnCount() {
            return 2;
        }

        public int getRowCount() {
            if (parent.general != null) return parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).Spell.size(); else return 0;
        }

        public String getValueAt(int row, int col) {
            switch(col) {
                case 0:
                    return Integer.toString(parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).LuckSpell.get(row));
                case 1:
                    if (parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).Spell.get(row) == 0) return "Pas de magie"; else return parent.general.getMagieByIndex(parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).Spell.get(row) - 1).Name;
                default:
                    return "unavailable";
            }
        }

        public boolean isCellEditable(int row, int col) {
            return true;
        }

        public void setValueAt(Object value, int row, int col) {
            switch(col) {
                case 0:
                    parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).LuckSpell.set(row, (short) Integer.parseInt((String) value));
                    break;
                case 1:
                    if (((String) value).compareTo("Pas de magie") == 0) parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).Spell.set(row, (short) 0); else for (int i = 0; i < parent.general.getMagies().size(); i++) {
                        if (parent.general.getMagieByIndex(i).Name.compareTo((String) value) == 0) parent.general.getMonstreByIndex(MonstreList.getSelectedIndex()).Spell.set(row, (short) (i + 1));
                    }
                    break;
            }
            fireTableCellUpdated(row, col);
        }
    }

    ;

    public class MyComboBoxRenderer extends JComboBox implements TableCellRenderer {

        private static final long serialVersionUID = 1L;

        public MyComboBoxRenderer(String[] items) {
            super(items);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
            setSelectedItem(value);
            return this;
        }
    }

    public class MyComboBoxEditor extends DefaultCellEditor {

        private static final long serialVersionUID = 1L;

        public MyComboBoxEditor(String[] items) {
            super(new JComboBox(items));
        }
    }
}
