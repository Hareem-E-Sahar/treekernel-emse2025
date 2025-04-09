package logique.calculateur;

import java.util.ArrayList;
import java.util.LinkedList;
import logique.reseau.Troncon;
import logique.reseau.Ville;

public class Fourmi implements Runnable {

    /**
	 * <pre>
	 * Double représentant l'inverse de l'importance donnée à l'exploitation
	 * ALPHA = 0 : seules les distances jouent
	 * 
	 * Paramétrage :
	 * ALPHA est appelé à être en exposant de valeurs égales à la valeur de la piste de
	 * phéromones sur un tronçon. On sait qu'une borne supérieure de cette valeur est sensée
	 * être (iteration * nb_fourmis).
	 * 
	 * </pre> 
	 */
    private static double ALPHA = 1;

    /**
	 * <pre>
	 * Double représentant l'inverse de l'importance donnée à l'exploration.
	 * 
	 * Paramétrage :
	 * 
	 * BETA est appelé à être en exposant de valeurs égales à l'inverse d'une distance
	 * entre deux villes. Si on imagine que les tronçons "normaux" font de 1 à 50 km, 
	 * on aura des valeurs allant de 0.02 à 1.
	 * 
	 * Pour que (1/d)^BETA soit un facteur cohérent, il faut que BETA soit positif (sinon
	 * on donne plus d'importance aux tronçons les plus longs). 
	 * 
	 * BETA = 0 : seules les pistes de phéromones jouent
	 * 
	 * BETA < 1 : on n'exclut que les tronçons les plus longs et on est tolérant envers 
	 * les tronçons moyennements longs.
	 * 
	 * BETA > 1 : on est élitiste. Seuls les tronçons les plus courts sont intéressants
	 * </pre>
	 */
    private static double BETA = 3;

    private Colonie colonie;

    private LinkedList<Troncon> chemin;

    private Ville ville_courante;

    private LinkedList<Ville> etapesVisitees;

    private Ville ville_depart;

    private LinkedList<Ville> villes_arrivee;

    private int score = 0;

    /**
	 * Liste de double représentant la probabilité d'atteindre une ville voisine à
	 * chaque pas d'une fourmi
	 */
    private ArrayList<Double> regles_deplacement = new ArrayList<Double>();

    /**
	 * Numérateur dans la formule de calcul des règle de déplacement
	 */
    private ArrayList<Double> elements = new ArrayList<Double>();

    /**
	 * Liste des troncons possibles depuis la ville courante
	 */
    private LinkedList<Troncon> voisins;

    private boolean start_anywhere;

    private boolean taille_max;

    private int max_etapes;

    private LinkedList<Ville> departs;

    /**
	 * Constructeur de Fourmi
	 * 
	 * @param e La liste des étapes du parcours recherché
	 */
    public Fourmi(Colonie c, int max_etapes, boolean taille_max, boolean start_anywhere) {
        super();
        colonie = c;
        this.start_anywhere = start_anywhere;
        this.taille_max = taille_max;
        this.max_etapes = max_etapes;
        etapesVisitees = new LinkedList<Ville>();
        villes_arrivee = new LinkedList<Ville>();
        chemin = new LinkedList<Troncon>();
    }

    public Fourmi(Colonie c, int max_etapes, boolean taille_max, LinkedList<Ville> departs) {
        super();
        colonie = c;
        this.start_anywhere = (departs != null && !departs.isEmpty());
        this.taille_max = taille_max;
        this.max_etapes = max_etapes;
        etapesVisitees = new LinkedList<Ville>();
        villes_arrivee = new LinkedList<Ville>();
        this.departs = departs;
        chemin = new LinkedList<Troncon>();
    }

    /**
	 * @return the ville_arrivee
	 */
    public LinkedList<Ville> getVilles_arrivee() {
        return villes_arrivee;
    }

    /**
	 * @param v the ville_arrivee to set
	 */
    public void addVilles_arrivee(Ville v) {
        villes_arrivee.add(v);
    }

    /**
	 * @return the ville_depart
	 */
    public Ville getVille_depart() {
        return ville_depart;
    }

    /**
	 * @param v the ville_depart to set
	 */
    public void setVille_depart(Ville v) {
        this.ville_depart = v;
        ville_courante = ville_depart;
    }

    /**
	 * Retourne la ville sur laquelle est située la fourmi
	 * 
	 * @return La ville sur laquelle est située la fourmi
	 */
    public Ville getVilleCourante() {
        return ville_courante;
    }

    /**
	 * La fourmi est-elle déjà passée par ce tronçon ?
	 * 
	 * @param t Le tronçon sur lequel porte la requête
	 * @return true si la fourmi est déjà passée par ce tronçon, false sinon
	 */
    public boolean estPasseePar(Troncon t) {
        return chemin.contains(t);
    }

    /**
	 * La fourmi a-t-elle déjà passé cette étape ?
	 * 
	 * @param etape L'étape sur laquelle porte la requête
	 * @return true si l'étape a déjà été atteinte, false sinon
	 */
    public boolean aDejaTraverseLEtape(Ville etape) {
        return etapesVisitees.contains(etape);
    }

    /**
	 * Ajout d'une ville à l'itinéraire de la fourmi
	 * 
	 * @param v La ville ajoutée
	 * @param poids La distance de la ville précédente à celle-ci
	 */
    public void ajoutTroncon(Troncon t) {
        score += t.getEvaluations().getScore();
        chemin.addLast(t);
        ville_courante = t.getExtremite(ville_courante);
    }

    /**
	 * Marque l'étape passée en paramètre comme visitée
	 * 
	 * TODO voir si on peut pas faire ca dans ajoutVille
	 *  
	 * @param e L'étape visitée
	 */
    public void retireEtape(Ville e) {
        etapesVisitees.add(e);
    }

    /**
	 * (Ré-)initialisation de la fourmi. Son itinéraire est vidé et ses étapes réinitialisées
	 *
	 */
    public void init() {
        etapesVisitees.clear();
        villes_arrivee.clear();
        chemin.clear();
        score = 0;
    }

    /**
	 * Retourne le chemin parcouru par la fourmi
	 * 
	 * @return Le chemin parcouru par la fourmi
	 */
    public LinkedList<Troncon> getChemin() {
        return chemin;
    }

    /**
	 * Retourne la longueur du chemin parcouru par la fourmi
	 * 
	 * @return La longueur du chemin parcouru par la fourmi
	 */
    public int getScore() {
        return score;
    }

    private void trouve_chemin() {
        Ville depart;
        int nb_etapes_restantes = colonie.getNb_etapes() - 2;
        boolean origine;
        if (start_anywhere) {
            if (departs != null) {
                depart = departs.get((int) (Math.random() * (departs.size() - 1)));
                origine = false;
            } else {
                depart = colonie.getToutesEtapes().get(new Double(Math.floor(Math.random() * colonie.getNb_etapes())).intValue());
                origine = (depart == colonie.getToutesEtapes().getFirst() || depart == colonie.getToutesEtapes().getLast());
            }
            addVilles_arrivee(colonie.getToutesEtapes().getFirst());
            addVilles_arrivee(colonie.getToutesEtapes().getLast());
        } else {
            if (Math.random() > 0.5) {
                depart = colonie.getToutesEtapes().getFirst();
                addVilles_arrivee(colonie.getToutesEtapes().getLast());
            } else {
                depart = colonie.getToutesEtapes().getLast();
                addVilles_arrivee(colonie.getToutesEtapes().getFirst());
            }
            origine = true;
        }
        setVille_depart(depart);
        Troncon troncon;
        while (((!origine || nb_etapes_restantes > 0 || !villes_arrivee.contains(ville_courante))) && (!taille_max || chemin.size() + 1 < max_etapes)) {
            troncon = choix_troncon(RATP());
            ajoutTroncon(troncon);
            if (colonie.getToutesEtapes().contains(ville_courante) && !aDejaTraverseLEtape(ville_courante) && !villes_arrivee.contains(ville_courante)) {
                retireEtape(ville_courante);
                nb_etapes_restantes--;
            }
        }
        if (chemin.size() < max_etapes) {
            if (origine) {
                boolean test = false;
                for (Troncon t : ville_courante.getTroncons()) {
                    if (villes_arrivee.contains(t.getExtremite(ville_courante))) {
                        score += t.getEvaluations().getScore();
                        chemin.addLast(t);
                        test = true;
                    }
                }
                if (test) {
                    if (score < colonie.getMeilleurItineraire().getScore() && (chemin.getFirst().getVille1() == depart || chemin.getFirst().getVille2() == depart) && (villes_arrivee.contains(chemin.getLast().getVille1()) || villes_arrivee.contains(chemin.getLast().getVille2()))) {
                        colonie.setMeilleurItineraire(new Itineraire(depart, chemin));
                    }
                }
            }
            for (Troncon t : chemin) {
                t.getEvaluations().addPheromones(calculPheromones(score));
            }
        } else if (!etapesVisitees.isEmpty()) {
            for (Troncon t : chemin) {
                t.getEvaluations().addPheromones(calculPheromones(score));
            }
        }
    }

    /**
	 * Règle Aléatoire de Transition Proportionnelle : règle de déplacement (additive
	 * pour pouvoir etre utilisée plus simplement)
	 * 
	 * @param de Ville où est située la fourmi
	 * @param a Ville candidate pour le prochain mouvement
	 */
    private ArrayList<Double> RATP() {
        voisins = ville_courante.getTroncons();
        for (Troncon t : voisins) {
            if (t.getEvaluations() == null) {
                t.setEvaluations(new Evaluations(colonie.getPreferences(), t));
                colonie.calculateur.addEvaluation(t.getEvaluations());
            }
        }
        double total = 0;
        double element;
        double somme = 0;
        for (int i = 0; i < voisins.size(); i++) {
            element = elementRATP(voisins.get(i));
            elements.add(element);
            total += element;
        }
        for (int i = 0; i < elements.size(); i++) {
            regles_deplacement.add(Math.max(somme + elements.get(i) / total, Float.MIN_VALUE));
            somme += elements.get(i) / total;
        }
        elements.clear();
        return regles_deplacement;
    }

    /**
	 * Valeur du numérateur pour une ville candidate donnée
	 * 
	 * @param de Ville où est située la fourmi
	 * @param a Ville candidate pour le prochain mouvement
	 * 
	 * @return Le numérateur utilisé dans le calcul de la probabilité de se 
	 * déplacer vers la ville candidate
	 */
    private double elementRATP(Troncon troncon) {
        if (!estPasseePar(troncon)) {
            return Math.min(Math.pow(troncon.getEvaluations().getPheromones(), ALPHA) * Math.pow(troncon.getEvaluations().getScore(), -BETA), Float.MAX_VALUE);
        }
        return Double.MIN_VALUE;
    }

    /**
	 * Méthode d'évaluation de la trace de phéromones à laisser après un parcours
	 * 
	 * @param score		Le score du parcours
	 * @return La valeur des phéromones à ajouter à la piste
	 */
    private float calculPheromones(int score) {
        return 1.0f * Colonie.Q / score;
    }

    /**
	 * Méthode de tirage au sort du tronçon suivant pour la fourmi f en fonction de 
	 * la règle de déplacement courante. 
	 * Recherche dichotomique.
	 *
	 * @return le troncon choisi
	 */
    private Troncon choix_troncon(ArrayList<Double> ratp) {
        double rand = Math.random();
        int d = ratp.size() - 1;
        int i = 0;
        int g = 0;
        boolean trouve = false;
        while (!trouve) {
            i = (g + d) / 2;
            if (ratp.get(i) >= 0.0) {
                if (ratp.get(i) > rand) d = i - 1; else g = i + 1;
                trouve = (i > 0 && ratp.get(i) >= rand && ratp.get(i - 1) < rand) || (i == 0 && ratp.get(i) >= rand);
            } else {
                throw new IllegalArgumentException(ratp.get(i).toString());
            }
        }
        ratp.clear();
        return voisins.get(i);
    }

    @Override
    public void run() {
        trouve_chemin();
        colonie.incrementeFinished();
        colonie.attendreLesAutres();
    }
}
