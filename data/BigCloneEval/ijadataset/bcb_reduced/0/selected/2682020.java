package Modele;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;

/**
 * Classe permettant de gérer les mouvements
 * @author El Meknassi Hamza (10806466) - Migeon Cyril (11007322)
 */
public class GestionMouvement extends Observable {

    private Damier damier;

    private ControleDeplacement cD = new ControleDeplacement();

    private ArrayList<Noeud> ListeChemins = new ArrayList<Noeud>();

    private ArrayList<Case> casesJouables = new ArrayList<Case>();

    private int deplacementMax;

    /**
     * Constructeur de la classe GestionMouvement
     * @param damier
     */
    public GestionMouvement(Damier damier) {
        this.damier = damier;
    }

    /**
     * Retourne le déplacement max recalculé à chaque tour
     * @return deplacementMax
     */
    public int getDeplacementMax() {
        return this.deplacementMax;
    }

    /**
     * Calcule de tous les déplacements prises possibles
     * @param joueur 
     */
    public void nouveauTour(Joueur joueur) {
        Case maCase;
        this.ListeChemins.clear();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                maCase = this.damier.getCase(i, j);
                Noeud noeud;
                if (cD.pieceDisponibleSurCase(maCase)) {
                    if (cD.JoueurPeutJouerPiece(maCase, joueur)) {
                        noeud = new Noeud(null, i, j, 0);
                        char couleur;
                        if (cD.couleurBlanche(maCase)) couleur = 'b'; else couleur = 'n';
                        noeud = this.deplacementPrisesPossibles(maCase, noeud, couleur);
                        this.ListeChemins.add(noeud);
                    }
                }
            }
        }
        this.definirCasesJouables();
    }

    /**
     * Renvoie un arbre contenant tous les déplacements possibles de la pièce depuis la case 
     * @param caseDepart
     * @param noeud
     * @param couleur 
     * @return Noeud
     */
    public Noeud deplacementPrisesPossibles(Case caseDepart, Noeud noeud, char couleur) {
        int xCaseDepart = caseDepart.getPositionX();
        int yCaseDepart = caseDepart.getPositionY();
        Case caseInter = null;
        Case caseCible = null;
        boolean deplacementPossible;
        for (int i = 0; i < 4; i++) {
            boolean caseAtteignable = false;
            if (i == 0) {
                if (xCaseDepart + 2 < 10 && yCaseDepart + 2 < 10) {
                    caseInter = this.damier.getCase(xCaseDepart + 1, yCaseDepart + 1);
                    caseCible = this.damier.getCase(xCaseDepart + 2, yCaseDepart + 2);
                    caseAtteignable = true;
                }
            } else if (i == 1) {
                if (xCaseDepart + 2 < 10 && yCaseDepart - 2 > -1) {
                    caseInter = this.damier.getCase(xCaseDepart + 1, yCaseDepart - 1);
                    caseCible = this.damier.getCase(xCaseDepart + 2, yCaseDepart - 2);
                    caseAtteignable = true;
                }
            } else if (i == 2) {
                if (xCaseDepart - 2 > -1 && yCaseDepart + 2 < 10) {
                    caseInter = this.damier.getCase(xCaseDepart - 1, yCaseDepart + 1);
                    caseCible = this.damier.getCase(xCaseDepart - 2, yCaseDepart + 2);
                    caseAtteignable = true;
                }
            } else {
                if (xCaseDepart - 2 > -1 && yCaseDepart - 2 > -1) {
                    caseInter = this.damier.getCase(xCaseDepart - 1, yCaseDepart - 1);
                    caseCible = this.damier.getCase(xCaseDepart - 2, yCaseDepart - 2);
                    caseAtteignable = true;
                }
            }
            if (caseAtteignable) deplacementPossible = cD.deplacementPossible(caseDepart, caseInter, caseCible, couleur); else deplacementPossible = false;
            if (deplacementPossible) {
                int xCaseCible = caseCible.getPositionX();
                int yCaseCible = caseCible.getPositionY();
                boolean ajoutNoeud;
                ajoutNoeud = noeud.ajouterFils(xCaseCible, yCaseCible);
                if (!ajoutNoeud) break;
                Case nouvelleCaseDepart = this.damier.getCase(xCaseCible, yCaseCible);
                Noeud nouveauNoeud = noeud.getDernierFils();
                this.deplacementPrisesPossibles(nouvelleCaseDepart, nouveauNoeud, couleur);
            }
        }
        return noeud;
    }

    private void definirCasesJouables() {
        Iterator<Noeud> itr = this.ListeChemins.iterator();
        Noeud noeud;
        int profondeurMax = 0;
        Case caseJouable;
        this.casesJouables.clear();
        while (itr.hasNext()) {
            noeud = itr.next();
            if (noeud.getProfondeurArbre() > profondeurMax) profondeurMax = noeud.getProfondeurArbre();
        }
        this.deplacementMax = profondeurMax;
        itr = this.ListeChemins.iterator();
        while (itr.hasNext()) {
            noeud = itr.next();
            if (noeud.getProfondeurArbre() == profondeurMax) {
                int xCaseJouable = noeud.getX();
                int yCaseJouable = noeud.getY();
                caseJouable = this.damier.getCase(xCaseJouable, yCaseJouable);
                this.getCasesJouables().add(caseJouable);
            }
        }
    }

    /**
     * Effectue le déplacement demandé si il est réalisable
     * @param joueur
     * @param caseDepart
     * @param caseCible
     * @return true si le déplacement est effectué, false si il n'etait pas réalisable et donc pas fait
     */
    public boolean deplacement(Joueur joueur, Case caseDepart, Case caseCible) {
        boolean deplacementPossible = false;
        if (cD.JoueurPeutJouerPiece(caseDepart, joueur)) {
            if (this.deplacementMax == 0) {
                deplacementPossible = cD.deplacementPossible(caseDepart, caseCible);
                if (deplacementPossible) {
                    Piece piece = caseDepart.getPiece();
                    caseDepart.setPiece(null);
                    caseDepart.setEtat(0);
                    caseCible.setPiece(piece);
                    caseCible.setEtat(1);
                    damier.getNewDamesNoires();
                    damier.getNewDamesBlanches();
                    setChanged();
                    notifyObservers();
                }
            } else {
                Position positionCaseDepart = caseDepart.getPosition();
                boolean caseDansCasesJouables = false;
                Iterator<Case> itrCase = this.getCasesJouables().iterator();
                while (itrCase.hasNext()) {
                    Case maCase = itrCase.next();
                    if (positionCaseDepart.comparePosition(maCase.getPositionX(), maCase.getPositionY())) {
                        caseDansCasesJouables = true;
                        break;
                    }
                }
                if (caseDansCasesJouables) {
                    int xCaseDepart = caseDepart.getPositionX();
                    int yCaseDepart = caseDepart.getPositionY();
                    int xCaseCible = caseCible.getPositionX();
                    int yCaseCible = caseCible.getPositionY();
                    Noeud noeud = null;
                    int xNoeud;
                    int yNoeud;
                    Iterator<Noeud> itrNoeud = this.ListeChemins.iterator();
                    while (itrNoeud.hasNext()) {
                        noeud = itrNoeud.next();
                        xNoeud = noeud.getX();
                        yNoeud = noeud.getY();
                        if (xCaseDepart == xNoeud && yCaseDepart == yNoeud) break;
                    }
                    ArrayList<int[][]> plusLongChemin = new ArrayList<int[][]>();
                    noeud.getPlusLongChemins(plusLongChemin, noeud.getProfondeurArbre(), noeud);
                    Iterator<int[][]> itrTableauInt = plusLongChemin.iterator();
                    int[][] tabCoordonnees;
                    while (itrTableauInt.hasNext()) {
                        tabCoordonnees = itrTableauInt.next();
                        if (tabCoordonnees[1][0] == xCaseCible && tabCoordonnees[1][1] == yCaseCible) {
                            if (this.deplacementMax == tabCoordonnees.length - 1) deplacementPossible = true; else deplacementPossible = false;
                            break;
                        }
                    }
                    if (deplacementPossible) {
                        int xCaseInter = (xCaseDepart + xCaseCible) / 2;
                        int yCaseInter = (yCaseDepart + yCaseCible) / 2;
                        Case caseInter = this.damier.getCase(xCaseInter, yCaseInter);
                        Piece piece = caseDepart.getPiece();
                        caseDepart.setPiece(null);
                        caseDepart.setEtat(0);
                        caseInter.setPiece(null);
                        caseInter.setEtat(0);
                        caseCible.setPiece(piece);
                        caseCible.setEtat(1);
                        damier.getNewDamesNoires();
                        damier.getNewDamesBlanches();
                        setChanged();
                        notifyObservers();
                    }
                }
            }
        }
        return deplacementPossible;
    }

    /**
     * Retourne la liste des cases jouables
     * @return the casesJouables
     */
    public ArrayList<Case> getCasesJouables() {
        return casesJouables;
    }

    /**
     * Permet d'obtenir la position des cases jouables
     * @return positionCasesJouables
     */
    public int[] getPositionCasesJouables() {
        Iterator it = casesJouables.iterator();
        int tabPositions[] = new int[20];
        int i = 0;
        while (it.hasNext()) {
            Case tmp = (Case) it.next();
            tabPositions[i] = correspondance(tmp.getPositionX(), tmp.getPositionY());
            i++;
        }
        return tabPositions;
    }

    /**
     * Fonction permettant d'identifier les différentes cases en fonction de leurs coordonnées
     * @param a
     * @param b
     * @return id - L'identifiant de la case de coordonnées (a,b)
     */
    private int correspondance(int a, int b) {
        int id = 0;
        int tabId[][] = new int[10][10];
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                tabId[x][y] = id;
                id++;
            }
        }
        return tabId[a][b];
    }
}
