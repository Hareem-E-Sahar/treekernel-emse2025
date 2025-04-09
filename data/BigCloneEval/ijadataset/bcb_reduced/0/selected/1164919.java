package visugraph.converter;

/**
 * <p>Convertisseur linéaire qui permet de transformer n'importe quel nombre en un autre à l'aide
 * d'une fonction affine par morceaux.</p>
 * 
 * <p>La fonction est définit par 4 paramètres (3 tableaux et un flottant).</p>
 * <ul>
 * 	<li>Le tableau des pentes (slopes). Chaque cellule contient la pente d'un des morceaux.</li>
 * 	<li>Le tableau qui contient les ordonnées à l'origine (intercepts).
 * 	Ce paramètre peut eventuellement être calculé dans le cas des fonctions totalement continues
 * (fonctions <em>linéaires</em> par morceaux)</li>
 * 	<li>Le tableau des abscisses indiquant sur quel intervalle est défini chacun des morceaux.
 * 	Ce tableau doit impérativement être ordonné par ordre croissant.</li>
 *  <li>Le dernier paramètre (start) indique à quel abscisse commencer les conversions.</li>
 * </ul>
 * 
 * <p>Le premier morceau va ainsi correspondre à l'équation {@code y = slopes[0]*x + intercepts[0]}
 * et sera défini sur {@code [start,abscisses[0] [}. Le deuxième morceau à
 * {@code y = slopes[1]*x + intercepts[1]} défini sur {@code [abscisses[0],abscisses[1] [ }, ...</p>
 * 
 * <p>La rapidité de ce convertisseur dépend avant tout du nombre de morceaux. L'implémentation repose
 * sur une recherche dichotomique garantissant une complexité en O(log(N)) où N est le nombre de morceaux.</p>
 * 
 * <p><em>A noter :</em> ce convertisseur ne fonctionne que dans un seul sens. Rien ne garantit en effet
 * que la fonction soit entièrement bijective.</p>
 */
public class LinearConverter<N extends Number> implements Converter<N, Double> {

    private double[] slopes;

    private double[] intercepts;

    private double[] abscisses;

    private double start;

    /**
	 * Créer un nouveau convertisseur linéaire sous forme de fonction affine.
	 * La fonction sera définie par {@code y = slope*x + intercept} sur l'intervalle {@code ]-inf, +inf[}. 
	 * @param slope la pente de la droite
	 * @param intercept l'ordonnée à l'origine
	 */
    public LinearConverter(double slope, double intercept) {
        this(Double.NEGATIVE_INFINITY, slope, intercept, Double.POSITIVE_INFINITY);
    }

    /**
	 * Créer un nouveau convertisseur linéaire sous forme de fonction affine.
	 * La fonction sera définie par {@code y = slope*x + intercept} sur l'intervalle {@code [start, end[}.
	 * @param slope la pente de la droite
	 * @param intercept l'ordonnée à l'origine.
	 * @param start intervalle de définition
	 * @param end intervalle de définition.
	 */
    public LinearConverter(double start, double slope, double intercept, double end) {
        this(start, new double[] { slope }, new double[] { intercept }, new double[] { end });
    }

    /**
	 * Créer un nouveau convertisseur linéaire sous forme de fonction affine par morceaux.
	 * Les abscisses doivent être fournies par ordre croissant.
	 * @param slopes les pentes des morceaux
	 * @param intercepts les ordonnées à l'origine des morceaux
	 * @param abscisses les intervalles de définition des morceaux
	 * @param start l'abscisse de départ (ie du premier morceau) de la fonction.
	 * @throws IllegalArgumentException si les abscisses ne sont pas classées par ordre croissant.
	 */
    public LinearConverter(double start, double[] slopes, double[] intercepts, double[] abscisses) {
        if (slopes.length != intercepts.length || slopes.length != abscisses.length || slopes.length == 0) {
            throw new IllegalArgumentException("Les tableaux n'ont pas le même nombre d'éléments ou sont vides");
        }
        this.checkOrder(start, abscisses);
        this.start = start;
        this.slopes = slopes.clone();
        this.intercepts = intercepts.clone();
        this.abscisses = abscisses.clone();
    }

    /**
	 * Créer un nouveau convertisseur linéaire sous forme d'une fonction linéaire continue par morceaux.
	 * Les ordonnées à l'origine seront automatiquement calculées. La fonction résultante est donc
	 * entièrement continue.
	 * @param slopes les pentes des morceaux
	 * @param abscisses les intervalles de définition des morceaux
	 * @param firstIntercept première ordonnée à l'origine pour le premier morceau.
	 * @param start l'abscisse de départ (ie du premier morceau).
	 * @throws IllegalArgumentException si les abscisses ne sont pas classées par ordre croissant.
	 */
    public LinearConverter(double start, double[] slopes, double firstIntercept, double[] abscisses) {
        if (slopes.length != abscisses.length || slopes.length == 0) {
            throw new IllegalArgumentException("Les tableaux n'ont pas le même nombre d'éléments ou sont vides");
        }
        this.checkOrder(start, abscisses);
        this.start = start;
        this.slopes = slopes.clone();
        this.abscisses = abscisses.clone();
        this.intercepts = new double[slopes.length];
        double endPiece = (abscisses[0] - start) * slopes[0] + firstIntercept;
        this.intercepts[0] = firstIntercept;
        for (int i = 1; i < abscisses.length; i++) {
            this.intercepts[i - 1] = endPiece;
            endPiece += (abscisses[i] - abscisses[i - 1]) * slopes[i];
        }
    }

    /**
	 * S'assure que les abscisses fournies sont classées par ordre croissant.
	 */
    private void checkOrder(double start, double[] abscisses) {
        for (int i = 0; i < abscisses.length; i++) {
            double prev = i == 0 ? start : abscisses[i - 1];
            if (prev > abscisses[i]) {
                throw new IllegalArgumentException("Les abscisses doivent être ordonnées par ordre croissant.");
            }
        }
    }

    public Double convert(N source) {
        double dValue = source.doubleValue();
        if (dValue < this.start || dValue > this.abscisses[this.abscisses.length - 1]) {
            throw new IllegalArgumentException("Cette valeur est en dehors des bornes du convertisseur. " + String.format("(%d <= value <= %d)", this.start, this.abscisses[this.abscisses.length - 1]));
        }
        int idx = this.absIndex(dValue, 0, this.abscisses.length - 1);
        return this.slopes[idx] * dValue + intercepts[idx];
    }

    public N convertBack(Double target) {
        throw new UnsupportedOperationException("Ce convertisseur n'est pas réversible : fonction non forcément bijective.");
    }

    /**
	 * Trouve récursivement l'index du morceau correspondant à l'abscisse.
	 * Cette méthode fonctionne par recherche dichotomique...
	 */
    private int absIndex(double abscisse, int startIdx, int endIdx) {
        if (startIdx == endIdx) {
            return startIdx;
        } else {
            int middleIdx = (startIdx + endIdx) / 2;
            double valStart = startIdx == 0 ? this.start : this.abscisses[startIdx - 1];
            double valMidd = this.abscisses[middleIdx - 1];
            if (abscisse >= valStart && abscisse < valMidd) {
                return this.absIndex(abscisse, startIdx, middleIdx);
            } else {
                return this.absIndex(abscisse, middleIdx, endIdx);
            }
        }
    }
}
