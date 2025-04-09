import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;
import java.nio.channels.FileChannel;

/**
 * classe permettant de d�finir une phrase dite � la rencontre d'un personnage
 */
public class UneEntreeSortie extends UnEvenement {

    private JFileChooser sonSelectionneurDeFichier;

    private File sonFichier;

    private JTextField sonChampCarte;

    UneCarte saCarte;

    UnJoueur sonPerso;

    UneEntreeSortie(UneCase telleCase) {
        saCase = telleCase;
        sonNom = "Passage vers la carte suivante";
        sonSelectionneurDeFichier = new JFileChooser("./Campagnes/");
        sonSelectionneurDeFichier.setAcceptAllFileFilterUsed(false);
        sonSelectionneurDeFichier.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {

            public boolean accept(File telFic) {
                String lExtension = telFic.getName().substring((telFic.getName().lastIndexOf(".") + 1));
                if (lExtension.equals("cjay") || lExtension.equals(telFic.getName())) return true; else return false;
            }

            public String getDescription() {
                return "Fichier de carte (*.cjay)";
            }
        });
    }

    UneEntreeSortie(UneCase telleCase, String telNom, int telleRepetition, String telFic) {
        super(telleCase, telNom, telleRepetition);
        sonFichier = new File(telFic);
        saCarte = new UneCarte(sonFichier, saCase.sonEditeurDonjon);
    }

    UneEntreeSortie(UneCase telleCase, String telNom, int telleRepetition, UneCarte telleCarte) {
        super(telleCase, telNom, telleRepetition);
        sonFichier = null;
        saCarte = telleCarte;
    }

    /**
     * permet de modifier les param�tres
     * @return panneau d'�dition
     */
    public JPanel editeToi() {
        JPanel lePanneau = new JPanel(new GridLayout(0, 1));
        creerRadio(lePanneau);
        JPanel lePanneauInter = new JPanel();
        sonChampCarte = new JTextField(10);
        if (sonFichier != null) sonChampCarte.setText(sonFichier.getName()); else sonChampCarte.setText("carte m�re");
        sonChampCarte.setEditable(false);
        JButton leBouton = new JButton("Charger la carte");
        leBouton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent telleAction) {
                int leRetour = sonSelectionneurDeFichier.showOpenDialog(saCase.sonEditeurDonjon);
                if (leRetour == JFileChooser.APPROVE_OPTION) {
                    sonFichier = sonSelectionneurDeFichier.getSelectedFile();
                    sonChampCarte.setText(sonFichier.getName());
                }
            }
        });
        lePanneauInter.add(sonChampCarte);
        lePanneauInter.add(leBouton);
        lePanneau.add(lePanneauInter);
        JLabel leLabel = new JLabel("Laissez \"carte m�re\" si cette carte est la suivante d'une autre.");
        lePanneau.add(leLabel);
        lePanneauInter = new JPanel();
        leBouton = new JButton("Valider");
        leBouton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent telleAction) {
                if (sonFichier != null && sonFichier.exists()) {
                    saCarte = new UneCarte(sonFichier, saCase.sonEditeurDonjon);
                    sonNom = "Passage vers " + saCarte.getsonNom();
                } else {
                    saCarte = null;
                    sonNom = "Passage vers une carte m�re.";
                }
                JOptionPane.showMessageDialog(null, "Changement effectu�!");
            }
        });
        lePanneauInter.add(leBouton);
        lePanneau.add(lePanneauInter);
        return lePanneau;
    }

    public String toString() {
        return super.toString() + "carte:" + (sonFichier != null ? sonFichier.getAbsolutePath() : "null");
    }

    /**
     * indique si les condition requise pour d�clencher le dialogue sont r�unies
     * @return vrai si l'�v�nement doit se d�clencher, faux sinon
     */
    public boolean doitSeDeclencher() {
        if (((saCase.sonEditeurDonjon.getsaCarte().getsesCases()[saCase.getsonY()][saCase.getsonX()].getsonJoueur() != null)) && (saRepetition == REPETABLE || saRepetition == UNIQUE)) {
            sonPerso = saCase.sonEditeurDonjon.getsaCarte().getsesCases()[saCase.getsonY()][saCase.getsonX()].getsonJoueur();
            return true;
        }
        return false;
    }

    /**
     * d�roulement du dialoque
     */
    public void declencheToi() {
        if (saRepetition == UNIQUE) saRepetition = UNIQUE_FAIT;
        for (int i = 0; i < saCarte.getsonY(); i++) {
            for (int j = 0; j < saCarte.getsonX(); j++) {
                if (saCarte.getsesCases()[i][j].getsonEvenement() != null) if (saCarte.getsesCases()[i][j].getsonEvenement().sonNom.contains("Passage")) if (((UneEntreeSortie) saCarte.getsesCases()[i][j].getsonEvenement()).saCarte.getsonNom().equals(saCase.sonEditeurDonjon.getsaCarte().getsonNom())) {
                    sonPerso.saPosX = j + 1;
                    sonPerso.saPosY = i;
                    saCarte.getsesCases()[i][j + 1].setsonJoueur(sonPerso);
                    saCase.setsonJoueur(null);
                    ((UneEntreeSortie) saCarte.getsesCases()[i][j].getsonEvenement()).saCarte = saCase.sonEditeurDonjon.getsaCarte();
                    saCase.sonEditeurDonjon.setsaCarte(saCarte);
                    saCase.sonEditeurDonjon.afficheLaCarte();
                    return;
                }
            }
        }
    }
}
