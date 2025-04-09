package mou.core.starmap;

import java.awt.Point;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import mou.Main;
import mou.Universum;
import mou.core.colony.Colony;
import mou.core.colony.ForeignColonyPersistent;
import mou.core.res.natural.NaturalResource;
import mou.gui.GUI;

/**
 * Repr�sentationsklasse eines einzelnes Sternensystems Enth�lt nur statische Daten �ber Sternsystem
 * ohne dynamischen Komponenten. Diese Klasse ist nicht serialisierbar, weil Objekte dieser Klasse
 * immer neu generiert werden.
 */
public strictfp class StarSystem {

    public static final String STAR_TYP_O = "O";

    public static final int MAX_PLANETS_STAR_TYP_O = 20;

    public static final String STAR_TYP_B = "B";

    public static final int MAX_PLANETS_STAR_TYP_B = 15;

    public static final String STAR_TYP_A = "A";

    public static final int MAX_PLANETS_STAR_TYP_A = 10;

    public static final String STAR_TYP_F = "F";

    public static final int MAX_PLANETS_STAR_TYP_F = 8;

    public static final String STAR_TYP_G = "G";

    public static final int MAX_PLANETS_STAR_TYP_G = 6;

    public static final String STAR_TYP_K = "K";

    public static final int MAX_PLANETS_STAR_TYP_K = 3;

    public static final String STAR_TYP_M = "M";

    public static final int MAX_PLANETS_STAR_TYP_M = 1;

    public static final String STAR_TYP_L = "L";

    public static final int MAX_PLANETS_STAR_TYP_L = 0;

    public static final String STAR_TYP_ZWERG_0 = "Z0";

    public static final String STAR_TYP_ZWERG_1 = "Z1";

    public static final String STAR_TYP_ZWERG_2 = "Z2";

    public static final String STAR_TYP_ZWERG_3 = "Z3";

    public static final int MAX_PLANETS_STAR_TYP_Z = 2;

    public static final String STAR_TYP_RIESE_0 = "R0";

    public static final int MAX_PLANETS_STAR_TYP_R0 = 20;

    public static final String STAR_TYP_RIESE_1 = "R1";

    public static final int MAX_PLANETS_STAR_TYP_R1 = 15;

    public static final String STAR_TYP_RIESE_2 = "R2";

    public static final int MAX_PLANETS_STAR_TYP_R2 = 10;

    private static final int STAR_GEWICHTUNG_HAUPTLINIE = 80;

    private static final int STAR_GEWICHTUNG_ZWERGE = 10;

    private static final int STAR_GEWICHTUNG_ROTE_RIESEN = 10;

    private static final int MAX_POPULATION_PER_PLANET = 10000000;

    private static final int MIN_POPULATION = 1000000;

    private static final Random _random = new Random();

    private static final double MAX_PRODUCTION_DELTA = 0.5d;

    private static final double MAX_MINING_DELTA = 0.5d;

    private static final double MAX_SCIENCE_DELTA = 0.5d;

    private static final double MAX_FARMING_DELTA = 0.5d;

    private static final double MAX_POPULATION_GROW_BONUS = 0.04;

    private String starclass = STAR_TYP_G;

    private List<NaturalResource> natRessources;

    private int planetcount = 0;

    private Point position;

    private String name;

    private double productionFaktor;

    private double miningFaktor;

    private double scienceFaktor;

    private double farmingFaktor;

    private double populationGrowBonus;

    private boolean systemPropertiesGenerated = false;

    private long mSeed;

    StarSystem(Point pos) {
        position = pos;
    }

    public Point getPosition() {
        return position;
    }

    public Point getQuadrant() {
        return Universum.getQuadrantForPosition(getPosition());
    }

    public void setName(String newName) {
        name = newName;
    }

    public long getMaxPopulation() {
        return getPlanetcount() * MAX_POPULATION_PER_PLANET + MIN_POPULATION;
    }

    /**
	 * @return Die generierte Name des Sterns
	 */
    public String getName() {
        String nm = Main.instance().getMOUDB().getStarmapDB().getStarsystemName(getPosition());
        if (nm == null) return name;
        return nm;
    }

    /**
	 * Generiert komplett ein neues Sternensystem an der gegebener Position
	 */
    public static synchronized StarSystem generateStarSystem(final int x, final int y, final long seed) {
        _random.setSeed(seed);
        StarSystem ss = new StarSystem(new Point(x, y));
        int wert = _random.nextInt(100);
        wert = wert - STAR_GEWICHTUNG_HAUPTLINIE;
        if (wert <= 0) ss.generateHauptlinie(_random); else {
            wert = wert - STAR_GEWICHTUNG_ROTE_RIESEN;
            if (wert <= 0) ss.generateRoteRiese(_random); else {
                wert = wert - STAR_GEWICHTUNG_ZWERGE;
                if (wert <= 0) ss.generateZwerge(_random); else ss.generateHauptlinie(_random);
            }
        }
        ss.mSeed = seed;
        ss.setName((ss.getStarClass()));
        return ss;
    }

    /**
	 * Generiert detailierte Systemeigenschaften "on demand"
	 * 
	 * @param seed
	 */
    private final void generateStarSystemProperties(final long seed) {
        synchronized (_random) {
            _random.setSeed(seed);
            natRessources = Main.instance().getMOUDB().getNaturalRessourceDescriptionDB().generateRessource(seed);
            productionFaktor = computeBonusFaktor(_random, MAX_PRODUCTION_DELTA);
            miningFaktor = computeBonusFaktor(_random, MAX_MINING_DELTA);
            scienceFaktor = computeBonusFaktor(_random, MAX_SCIENCE_DELTA);
            farmingFaktor = computeBonusFaktor(_random, MAX_FARMING_DELTA);
            populationGrowBonus = _random.nextDouble() * MAX_POPULATION_GROW_BONUS;
            if (_random.nextBoolean()) populationGrowBonus = -populationGrowBonus;
            systemPropertiesGenerated = true;
        }
    }

    private final double computeBonusFaktor(final Random rnd, final double maxValue) {
        double ret = rnd.nextDouble() * maxValue;
        if (rnd.nextBoolean()) return 1 + ret; else return 1 - ret;
    }

    private void generateZwerge(Random rnd) {
        switch(rnd.nextInt(4)) {
            case 0:
                starclass = STAR_TYP_ZWERG_0;
                break;
            case 1:
                starclass = STAR_TYP_ZWERG_1;
                break;
            case 2:
                starclass = STAR_TYP_ZWERG_2;
                break;
            case 3:
                starclass = STAR_TYP_ZWERG_3;
                break;
        }
        planetcount = rnd.nextInt(MAX_PLANETS_STAR_TYP_Z);
    }

    /**
	 * Generiert einer der drei gro�en roten Riesen
	 */
    private void generateRoteRiese(Random rnd) {
        final int BIG = 30;
        final int NORMAL = 30;
        final int LITTLE = 30;
        int wert = rnd.nextInt(100) + 1;
        wert = wert - BIG;
        if (wert <= 0) {
            starclass = STAR_TYP_RIESE_0;
            planetcount = rnd.nextInt(MAX_PLANETS_STAR_TYP_R0 + 1);
            return;
        }
        wert = wert - NORMAL;
        if (wert <= 0) {
            starclass = STAR_TYP_RIESE_1;
            planetcount = rnd.nextInt(MAX_PLANETS_STAR_TYP_R1 + 1);
            return;
        }
        wert = wert - LITTLE;
        if (wert <= 0) {
            starclass = STAR_TYP_RIESE_2;
            planetcount = rnd.nextInt(MAX_PLANETS_STAR_TYP_R2 + 1);
            return;
        }
    }

    /**
	 * Generiert Sterne der Hauptlinie Gewichtung der Sternenklassen:
	 */
    private void generateHauptlinie(Random rnd) {
        final int O = 3;
        final int B = 10;
        final int A = 17;
        final int F = 21;
        final int G = 21;
        final int K = 17;
        final int M = 10;
        final int L = 1;
        int wert = rnd.nextInt(100) + 1;
        wert = wert - L;
        if (wert <= 0) {
            starclass = STAR_TYP_L;
            planetcount = rnd.nextInt(MAX_PLANETS_STAR_TYP_L + 1);
            return;
        }
        wert = wert - O;
        if (wert <= 0) {
            starclass = STAR_TYP_O;
            planetcount = rnd.nextInt(MAX_PLANETS_STAR_TYP_O + 1);
            return;
        }
        wert = wert - B;
        if (wert <= 0) {
            starclass = STAR_TYP_B;
            planetcount = rnd.nextInt(MAX_PLANETS_STAR_TYP_B + 1);
            return;
        }
        wert = wert - A;
        if (wert <= 0) {
            starclass = STAR_TYP_A;
            planetcount = rnd.nextInt(MAX_PLANETS_STAR_TYP_A + 1);
            return;
        }
        wert = wert - F;
        if (wert <= 0) {
            starclass = STAR_TYP_F;
            planetcount = rnd.nextInt(MAX_PLANETS_STAR_TYP_F + 1);
            return;
        }
        wert = wert - G;
        if (wert <= 0) {
            starclass = STAR_TYP_G;
            planetcount = rnd.nextInt(MAX_PLANETS_STAR_TYP_G + 1);
            return;
        }
        wert = wert - K;
        if (wert <= 0) {
            starclass = STAR_TYP_K;
            planetcount = rnd.nextInt(MAX_PLANETS_STAR_TYP_K + 1);
            return;
        }
        wert = wert - M;
        if (wert <= 0) {
            starclass = STAR_TYP_M;
            planetcount = rnd.nextInt(MAX_PLANETS_STAR_TYP_M + 1);
            return;
        }
    }

    /**
	 * Gibt die Klasse des Sterns zur�ck
	 * 
	 * @return eine der StaticStarSystem.TYP_ Konstanten
	 */
    public String getStarClass() {
        return starclass;
    }

    public void setStarclass(String newStarclass) {
        starclass = newStarclass;
    }

    /**
	 * Liefert Liste im System vorhandenen nat�rlichen Ressourcen
	 * 
	 * @return Liste mit NaturalResource Objekten
	 */
    public List<NaturalResource> getNatRessources() {
        if (!systemPropertiesGenerated) generateStarSystemProperties(mSeed);
        return natRessources;
    }

    public int getPlanetcount() {
        return planetcount;
    }

    public void setPlanetcount(int newPlanetcount) {
        planetcount = newPlanetcount;
    }

    /**
	 * Zwei Sterne werden als gleich erkannt wenn sie �bereinstimmende Raumkoordinaten haben
	 */
    public boolean equals(Object obj) {
        StarSystem ss = null;
        try {
            ss = (StarSystem) obj;
        } catch (ClassCastException e) {
            return false;
        }
        return ss.getPosition().equals(getPosition());
    }

    public String toString() {
        return getName() + " " + GUI.formatPoint(getPosition());
    }

    public Colony getMeineKolonie() {
        List cols = Main.instance().getMOUDB().getKolonieDB().getColoniesInSystem(getPosition());
        if (cols.isEmpty()) return null;
        return (Colony) cols.get(0);
    }

    public Collection<ForeignColonyPersistent> getFremdeKolonien() {
        return Main.instance().getMOUDB().getFremdeKolonienDB().getObjectsAt(getPosition()).values();
    }

    public boolean erforscht() {
        return Main.instance().getMOUDB().getStarmapDB().isStarsystemVisited(getPosition());
    }

    public void setStarName(String name) {
        Main.instance().getMOUDB().getStarmapDB().setStarsystemName(getPosition(), name);
    }

    /**
	 * Umwandelt Faktor (Werte > 0) in eine Prozentuelle Representation mit Plus und Minus Zeichen
	 */
    private final String bonusFaktorToString(double faktor) {
        double val = (faktor * 100) - 100;
        return GUI.formatProzentSigned(val);
    }

    /**
	 * @return ein Wert zwischen 0 und 1
	 */
    public double getFarmingFaktor() {
        if (!systemPropertiesGenerated) generateStarSystemProperties(mSeed);
        return farmingFaktor;
    }

    public final String getFarmingFaktorString() {
        return bonusFaktorToString(getFarmingFaktor());
    }

    /**
	 * @return ein Wert zwischen 0 und 1
	 */
    public double getMiningFaktor() {
        if (!systemPropertiesGenerated) generateStarSystemProperties(mSeed);
        return miningFaktor;
    }

    public String getMiningFaktorString() {
        return bonusFaktorToString(getMiningFaktor());
    }

    /**
	 * Bestimmt wieviel Prozentpunkte zum Bev�lkerungswachstum addiert werden
	 * 
	 * @return positiver oder negativer wert
	 */
    public double getPopulationGrowBonus() {
        if (!systemPropertiesGenerated) generateStarSystemProperties(mSeed);
        return populationGrowBonus;
    }

    public String getPopulationGrowBonusString() {
        return bonusFaktorToString(getPopulationGrowBonus() + 1);
    }

    /**
	 * @return ein Wert zwischen 0 und 1
	 */
    public double getProductionFaktor() {
        if (!systemPropertiesGenerated) generateStarSystemProperties(mSeed);
        return productionFaktor;
    }

    public String getProductionFaktorString() {
        return bonusFaktorToString(getProductionFaktor());
    }

    /**
	 * @return ein Wert zwischen 0 und 1
	 */
    public double getScienceFaktor() {
        if (!systemPropertiesGenerated) generateStarSystemProperties(mSeed);
        return scienceFaktor;
    }

    public String getScienceFaktorString() {
        return bonusFaktorToString(getScienceFaktor());
    }

    /**
	 * Liefert HTML-formatierte Textdarstellung eines Sternesystems
	 * 
	 * @return
	 */
    public String getTooltipHtmlText() {
        if (!erforscht()) return "<html>Nicht erforscht</html>";
        StringBuilder b = new StringBuilder("<html><b>").append(toString()).append("</b><table>");
        b.append("<tr><td><b>Max. Population:</b></td><td>").append(GUI.formatLong(getMaxPopulation())).append("</td></tr>");
        b.append("<tr><td><b>Wachstum:</b></td><td>").append(getPopulationGrowBonusString()).append("</td></tr>");
        b.append("<tr><td><b>Landwirtschaft:</b></td><td>").append(getFarmingFaktorString()).append("</td></tr>");
        b.append("<tr><td><b>Production:</b></td><td>").append(getProductionFaktorString()).append("</td></tr>");
        b.append("<tr><td><b>Bergbau:</b></td><td>").append(getMiningFaktorString()).append("</td></tr>");
        b.append("<tr><td><b>Forschung:</b></td><td>").append(getScienceFaktorString()).append("</td></tr></table>");
        if (getNatRessources().size() > 0) {
            b.append("<br>~~<b>Nat�rliche Ressourcen</b>~~");
            b.append("<ul>");
            for (Iterator iter = getNatRessources().iterator(); iter.hasNext(); ) {
                NaturalResource res = (NaturalResource) iter.next();
                b.append("<li>").append(res.getName()).append("</li>");
            }
            b.append("</ul");
        }
        b.append("</html>");
        return b.toString();
    }
}
