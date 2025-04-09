package org.jmol.util;

import java.util.BitSet;
import java.util.Hashtable;
import java.util.Map;
import org.jmol.constant.EnumVdw;

public class Elements {

    /**
   * The default elementSymbols. Presumably the only entry which may cause
   * confusion is element 0, whose symbol we have defined as "Xx". 
   */
    public static final String[] elementSymbols = { "Xx", "H", "He", "Li", "Be", "B", "C", "N", "O", "F", "Ne", "Na", "Mg", "Al", "Si", "P", "S", "Cl", "Ar", "K", "Ca", "Sc", "Ti", "V", "Cr", "Mn", "Fe", "Co", "Ni", "Cu", "Zn", "Ga", "Ge", "As", "Se", "Br", "Kr", "Rb", "Sr", "Y", "Zr", "Nb", "Mo", "Tc", "Ru", "Rh", "Pd", "Ag", "Cd", "In", "Sn", "Sb", "Te", "I", "Xe", "Cs", "Ba", "La", "Ce", "Pr", "Nd", "Pm", "Sm", "Eu", "Gd", "Tb", "Dy", "Ho", "Er", "Tm", "Yb", "Lu", "Hf", "Ta", "W", "Re", "Os", "Ir", "Pt", "Au", "Hg", "Tl", "Pb", "Bi", "Po", "At", "Rn", "Fr", "Ra", "Ac", "Th", "Pa", "U", "Np", "Pu", "Am", "Cm", "Bk", "Cf", "Es", "Fm", "Md", "No", "Lr", "Rf", "Db", "Sg", "Bh", "Hs", "Mt" };

    public static final float[] atomicMass = { 0, 1.008f, 4.003f, 6.941f, 9.012f, 10.81f, 12.011f, 14.007f, 15.999f, 18.998f, 20.18f, 22.99f, 24.305f, 26.981f, 28.086f, 30.974f, 32.07f, 35.453f, 39.948f, 39.1f, 40.08f, 44.956f, 47.88f, 50.941f, 52f, 54.938f, 55.847f, 58.93f, 58.69f, 63.55f, 65.39f, 69.72f, 72.61f, 74.92f, 78.96f, 79.9f, 83.8f, 85.47f, 87.62f, 88.91f, 91.22f, 92.91f, 95.94f, 98.91f, 101.07f, 102.91f, 106.42f, 107.87f, 112.41f, 114.82f, 118.71f, 121.75f, 127.6f, 126.91f, 131.29f, 132.91f, 137.33f, 138.91f, 140.12f, 140.91f, 144.24f, 144.9f, 150.36f, 151.96f, 157.25f, 158.93f, 162.5f, 164.93f, 167.26f, 168.93f, 173.04f, 174.97f, 178.49f, 180.95f, 183.85f, 186.21f, 190.2f, 192.22f, 195.08f, 196.97f, 200.59f, 204.38f, 207.2f, 208.98f, 210f, 210f, 222f, 223f, 226.03f, 227.03f, 232.04f, 231.04f, 238.03f, 237.05f, 239.1f, 243.1f, 247.1f, 247.1f, 252.1f, 252.1f, 257.1f, 256.1f, 259.1f, 260.1f, 261f, 262f, 263f, 262f, 265f, 268f };

    public static float getAtomicMass(int i) {
        return (i < 1 || i >= atomicMass.length ? 0 : atomicMass[i]);
    }

    /**
   * one larger than the last elementNumber, same as elementSymbols.length
   */
    public static final int elementNumberMax = elementSymbols.length;

    /**
   * @param elementSymbol First char must be upper case, second char accepts upper or lower case
   * @param isSilent TODO
   * @return elementNumber = atomicNumber + IsotopeNumber*128
   */
    public static final short elementNumberFromSymbol(String elementSymbol, boolean isSilent) {
        if (htElementMap == null) {
            Map<String, Integer> map = new Hashtable<String, Integer>();
            for (int elementNumber = elementNumberMax; --elementNumber >= 0; ) {
                String symbol = elementSymbols[elementNumber];
                Integer boxed = Integer.valueOf(elementNumber);
                map.put(symbol, boxed);
                if (symbol.length() == 2) map.put(symbol.toUpperCase(), boxed);
            }
            for (int i = altElementMax; --i >= firstIsotope; ) {
                String symbol = altElementSymbols[i];
                Integer boxed = Integer.valueOf(altElementNumbers[i]);
                map.put(symbol, boxed);
                if (symbol.length() == 2) map.put(symbol.toUpperCase(), boxed);
            }
            htElementMap = map;
        }
        if (elementSymbol == null) return 0;
        Integer boxedAtomicNumber = htElementMap.get(elementSymbol);
        if (boxedAtomicNumber != null) return (short) boxedAtomicNumber.intValue();
        if (!isSilent) Logger.error("'" + elementSymbol + "' is not a recognized symbol");
        return 0;
    }

    public static Map<String, Integer> htElementMap;

    /**
   * @param elementNumber may be atomicNumber + isotopeNumber*128
   * @return elementSymbol
   */
    public static final String elementSymbolFromNumber(int elementNumber) {
        if (elementNumber >= elementNumberMax) {
            for (int j = altElementMax; --j >= 0; ) if (elementNumber == altElementNumbers[j]) return altElementSymbols[j];
            elementNumber %= 128;
        }
        if (elementNumber < 0 || elementNumber >= elementNumberMax) elementNumber = 0;
        return elementSymbols[elementNumber];
    }

    public static final String elementNames[] = { "unknown", "hydrogen", "helium", "lithium", "beryllium", "boron", "carbon", "nitrogen", "oxygen", "fluorine", "neon", "sodium", "magnesium", "aluminum", "silicon", "phosphorus", "sulfur", "chlorine", "argon", "potassium", "calcium", "scandium", "titanium", "vanadium", "chromium", "manganese", "iron", "cobalt", "nickel", "copper", "zinc", "gallium", "germanium", "arsenic", "selenium", "bromine", "krypton", "rubidium", "strontium", "yttrium", "zirconium", "niobium", "molybdenum", "technetium", "ruthenium", "rhodium", "palladium", "silver", "cadmium", "indium", "tin", "antimony", "tellurium", "iodine", "xenon", "cesium", "barium", "lanthanum", "cerium", "praseodymium", "neodymium", "promethium", "samarium", "europium", "gadolinium", "terbium", "dysprosium", "holmium", "erbium", "thulium", "ytterbium", "lutetium", "hafnium", "tantalum", "tungsten", "rhenium", "osmium", "iridium", "platinum", "gold", "mercury", "thallium", "lead", "bismuth", "polonium", "astatine", "radon", "francium", "radium", "actinium", "thorium", "protactinium", "uranium", "neptunium", "plutonium", "americium", "curium", "berkelium", "californium", "einsteinium", "fermium", "mendelevium", "nobelium", "lawrencium", "rutherfordium", "dubnium", "seaborgium", "bohrium", "hassium", "meitnerium" };

    /**
   * @param elementNumber may be atomicNumber + isotopeNumber*128
   * @return elementName
   */
    public static final String elementNameFromNumber(int elementNumber) {
        if (elementNumber >= elementNumberMax) {
            for (int j = altElementMax; --j >= 0; ) if (elementNumber == altElementNumbers[j]) return altElementNames[j];
            elementNumber %= 128;
        }
        if (elementNumber < 0 || elementNumber >= elementNumberMax) elementNumber = 0;
        return elementNames[elementNumber];
    }

    /**
   * @param i index into altElementNames
   * @return elementName
   */
    public static final String altElementNameFromIndex(int i) {
        return altElementNames[i];
    }

    /**
   * @param i index into altElementNumbers
   * @return elementNumber (may be atomicNumber + isotopeNumber*128)
   */
    public static final short altElementNumberFromIndex(int i) {
        return altElementNumbers[i];
    }

    /** 
   * @param i index into altElementSymbols
   * @return elementSymbol
   */
    public static final String altElementSymbolFromIndex(int i) {
        return altElementSymbols[i];
    }

    /**
   * @param i index into altElementSymbols
   * @return 2H
   */
    public static final String altIsotopeSymbolFromIndex(int i) {
        int code = altElementNumbers[i];
        return (code >> 7) + elementSymbolFromNumber(code & 127);
    }

    /**
   * @param i index into altElementSymbols
   * @return H2
   */
    public static final String altIsotopeSymbolFromIndex2(int i) {
        int code = altElementNumbers[i];
        return elementSymbolFromNumber(code & 127) + (code >> 7);
    }

    public static final short getElementNumber(short atomicAndIsotopeNumber) {
        return (short) (atomicAndIsotopeNumber % 128);
    }

    public static final short getIsotopeNumber(short atomicAndIsotopeNumber) {
        return (short) (atomicAndIsotopeNumber >> 7);
    }

    public static final short getAtomicAndIsotopeNumber(int n, int mass) {
        return (short) ((n < 0 ? 0 : n) + (mass <= 0 ? 0 : mass << 7));
    }

    /**
   * @param atomicAndIsotopeNumber (may be atomicNumber + isotopeNumber*128)
   * @return  index into altElementNumbers
   */
    public static final int altElementIndexFromNumber(int atomicAndIsotopeNumber) {
        for (int i = 0; i < altElementMax; i++) if (altElementNumbers[i] == atomicAndIsotopeNumber) return i;
        return 0;
    }

    private static int[] naturalIsotopeMasses = { 1, 1, 6, 12, 7, 14, 8, 16 };

    public static int getNaturalIsotope(int elementNumber) {
        for (int i = 0; i < naturalIsotopeMasses.length; i += 2) if (naturalIsotopeMasses[i] == elementNumber) return naturalIsotopeMasses[++i];
        return 0;
    }

    private static final String naturalIsotopes = "1H,12C,14N,";

    public static final boolean isNaturalIsotope(String isotopeSymbol) {
        return (naturalIsotopes.indexOf(isotopeSymbol + ",") >= 0);
    }

    /**
   * first entry of an actual isotope int the altElementSymbols, altElementNames, altElementNumbers arrays
   */
    public static final int firstIsotope = 4;

    private static final short[] altElementNumbers = { 0, 13, 16, 55, (2 << 7) + 1, (3 << 7) + 1, (11 << 7) + 6, (13 << 7) + 6, (14 << 7) + 6, (15 << 7) + 7 };

    /**
   * length of the altElementSymbols, altElementNames, altElementNumbers arrays
   */
    public static final int altElementMax = altElementNumbers.length;

    private static final String[] altElementSymbols = { "Xx", "Al", "S", "Cs", "D", "T", "11C", "13C", "14C", "15N" };

    private static final String[] altElementNames = { "dummy", "aluminium", "sulphur", "caesium", "deuterium", "tritium", "", "", "", "" };

    /**
   * Default table of van der Waals Radii.
   * values are stored as MAR -- Milli Angstrom Radius
   * Used for spacefill rendering of atoms.
   * Values taken from OpenBabel.
   * 
   * Note that AUTO_JMOL, AUTO_BABEL, and AUTO_RASMOL are 4, 5, and 6, respectively,
   * so their mod will be JMOL, BABEL, and RASMOL. AUTO is 8, so will default to Jmol
   * 
   * @see <a href="http://openbabel.sourceforge.net">openbabel.sourceforge.net</a>
   * @see <a href="http://jmol.svn.sourceforge.net/viewvc/jmol/trunk/Jmol/src/org/jmol/_documents/vdw_comparison.xls">vdw_comparison.xls</a>
   */
    public static final short[] vanderwaalsMars = { 1000, 1000, 1000, 1000, 1200, 1100, 1100, 1200, 1400, 1400, 2200, 1400, 1820, 1810, 1220, 2200, 1700, 1530, 628, 1900, 2080, 1920, 1548, 1800, 1950, 1700, 1548, 1700, 1850, 1550, 1400, 1600, 1700, 1520, 1348, 1550, 1730, 1470, 1300, 1500, 1540, 1540, 2020, 1540, 2270, 2270, 2200, 2400, 1730, 1730, 1500, 2200, 2050, 1840, 1500, 2100, 2100, 2100, 2200, 2100, 2080, 1800, 1880, 1950, 2000, 1800, 1808, 1800, 1970, 1750, 1748, 1800, 1880, 1880, 2768, 1880, 2750, 2750, 2388, 2800, 1973, 2310, 1948, 2400, 1700, 2300, 1320, 2300, 1700, 2150, 1948, 2150, 1700, 2050, 1060, 2050, 1700, 2050, 1128, 2050, 1700, 2050, 1188, 2050, 1700, 2050, 1948, 2050, 1700, 2000, 1128, 2000, 1630, 2000, 1240, 2000, 1400, 2000, 1148, 2000, 1390, 2100, 1148, 2100, 1870, 1870, 1548, 2100, 1700, 2110, 3996, 2100, 1850, 1850, 828, 2050, 1900, 1900, 900, 1900, 2100, 1830, 1748, 1900, 2020, 2020, 1900, 2020, 1700, 3030, 2648, 2900, 1700, 2490, 2020, 2550, 1700, 2400, 1608, 2400, 1700, 2300, 1420, 2300, 1700, 2150, 1328, 2150, 1700, 2100, 1748, 2100, 1700, 2050, 1800, 2050, 1700, 2050, 1200, 2050, 1700, 2000, 1220, 2000, 1630, 2050, 1440, 2050, 1720, 2100, 1548, 2100, 1580, 2200, 1748, 2200, 1930, 2200, 1448, 2200, 2170, 1930, 1668, 2250, 2200, 2170, 1120, 2200, 2060, 2060, 1260, 2100, 2150, 1980, 1748, 2100, 2160, 2160, 2100, 2160, 1700, 3430, 3008, 3000, 1700, 2680, 2408, 2700, 1700, 2500, 1828, 2500, 1700, 2480, 1860, 2480, 1700, 2470, 1620, 2470, 1700, 2450, 1788, 2450, 1700, 2430, 1760, 2430, 1700, 2420, 1740, 2420, 1700, 2400, 1960, 2400, 1700, 2380, 1688, 2380, 1700, 2370, 1660, 2370, 1700, 2350, 1628, 2350, 1700, 2330, 1608, 2330, 1700, 2320, 1588, 2320, 1700, 2300, 1568, 2300, 1700, 2280, 1540, 2280, 1700, 2270, 1528, 2270, 1700, 2250, 1400, 2250, 1700, 2200, 1220, 2200, 1700, 2100, 1260, 2100, 1700, 2050, 1300, 2050, 1700, 2000, 1580, 2000, 1700, 2000, 1220, 2000, 1720, 2050, 1548, 2050, 1660, 2100, 1448, 2100, 1550, 2050, 1980, 2050, 1960, 1960, 1708, 2200, 2020, 2020, 2160, 2300, 1700, 2070, 1728, 2300, 1700, 1970, 1208, 2000, 1700, 2020, 1120, 2000, 1700, 2200, 2300, 2000, 1700, 3480, 3240, 2000, 1700, 2830, 2568, 2000, 1700, 2000, 2120, 2000, 1700, 2400, 1840, 2400, 1700, 2000, 1600, 2000, 1860, 2300, 1748, 2300, 1700, 2000, 1708, 2000, 1700, 2000, 1668, 2000, 1700, 2000, 1660, 2000, 1700, 2000, 1648, 2000, 1700, 2000, 1640, 2000, 1700, 2000, 1628, 2000, 1700, 2000, 1620, 2000, 1700, 2000, 1608, 2000, 1700, 2000, 1600, 2000, 1700, 2000, 1588, 2000, 1700, 2000, 1580, 2000, 1700, 2000, 1600, 2000, 1700, 2000, 1600, 2000, 1700, 2000, 1600, 2000, 1700, 2000, 1600, 2000, 1700, 2000, 1600, 2000, 1700, 2000, 1600, 2000 };

    /**
   * Default table of covalent Radii
   * stored as a short mar ... Milli Angstrom Radius
   * Values taken from OpenBabel.
   * @see <a href="http://openbabel.sourceforge.net">openbabel.sourceforge.net</a>
   */
    public static final short[] covalentMars = { 0, 230, 930, 680, 350, 830, 680, 680, 680, 640, 1120, 970, 1100, 1350, 1200, 750, 1020, 990, 1570, 1330, 990, 1440, 1470, 1330, 1350, 1350, 1340, 1330, 1500, 1520, 1450, 1220, 1170, 1210, 1220, 1210, 1910, 1470, 1120, 1780, 1560, 1480, 1470, 1350, 1400, 1450, 1500, 1590, 1690, 1630, 1460, 1460, 1470, 1400, 1980, 1670, 1340, 1870, 1830, 1820, 1810, 1800, 1800, 1990, 1790, 1760, 1750, 1740, 1730, 1720, 1940, 1720, 1570, 1430, 1370, 1350, 1370, 1320, 1500, 1500, 1700, 1550, 1540, 1540, 1680, 1700, 2400, 2000, 1900, 1880, 1790, 1610, 1580, 1550, 1530, 1510, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1600, 1600, 1600, 1600, 1600, 1600 };

    /****************************************************************
   * ionic radii are looked up using an array of shorts (16 bits each) 
   * that contains the atomic number, the charge, and the radius in two
   * consecutive values, encoded as follows:
   * 
   *   (atomicNumber << 4) + (charge + 4), radiusAngstroms*1000
   * 
   * That is, (atomicNumber * 16 + charge + 4), milliAngstromRadius
   * 
   * This allows for charges from -4 to 11, but we only really have -4 to 7.
   *
   * This data is from
   *  Handbook of Chemistry and Physics. 48th Ed, 1967-8, p. F143
   *  (scanned for Jmol by Phillip Barak, Jan 2004)
   *  
   * Reorganized from two separate arrays 9/2006 by Bob Hanson, who thought
   * it was just too hard to look these up and, if necessary, add or modify.
   * At the same time, the table was split into cations and anions for easier
   * retrieval.
   * 
   * O- and N+ removed 9/2008 - BH. The problem is that
   * the formal charge is used to determine bonding radius. 
   * But these formal charges are different than the charges used in 
   * compilation of HCP data (which is crystal ionic radii). 
   * Specifically, because O- and N+ are very common in organic 
   * compounds, I have removed their radii from the table FOR OUR PURPOSES HERE.
   * 
   * I suppose there are some ionic compounds that have O- and N+ as 
   * isolated ions, but what they would be I have no clue. Better to 
   * be safe and go with somewhat more reasonable values.
   * 
   *  Argh. Changed for Jmol 11.6.RC15
   * 
   *  
   ****************************************************************/
    public static final int FORMAL_CHARGE_MIN = -4;

    public static final int FORMAL_CHARGE_MAX = 7;

    private static final short[] cationLookupTable = { (3 << 4) + (1 + 4), 680, (4 << 4) + (1 + 4), 440, (4 << 4) + (2 + 4), 350, (5 << 4) + (1 + 4), 350, (5 << 4) + (3 + 4), 230, (6 << 4) + (4 + 4), 160, (7 << 4) + (1 + 4), 680, (7 << 4) + (3 + 4), 160, (7 << 4) + (5 + 4), 130, (8 << 4) + (1 + 4), 220, (8 << 4) + (6 + 4), 90, (9 << 4) + (7 + 4), 80, (10 << 4) + (1 + 4), 1120, (11 << 4) + (1 + 4), 970, (12 << 4) + (1 + 4), 820, (12 << 4) + (2 + 4), 660, (13 << 4) + (3 + 4), 510, (14 << 4) + (1 + 4), 650, (14 << 4) + (4 + 4), 420, (15 << 4) + (3 + 4), 440, (15 << 4) + (5 + 4), 350, (16 << 4) + (2 + 4), 2190, (16 << 4) + (4 + 4), 370, (16 << 4) + (6 + 4), 300, (17 << 4) + (5 + 4), 340, (17 << 4) + (7 + 4), 270, (18 << 4) + (1 + 4), 1540, (19 << 4) + (1 + 4), 1330, (20 << 4) + (1 + 4), 1180, (20 << 4) + (2 + 4), 990, (21 << 4) + (3 + 4), 732, (22 << 4) + (1 + 4), 960, (22 << 4) + (2 + 4), 940, (22 << 4) + (3 + 4), 760, (22 << 4) + (4 + 4), 680, (23 << 4) + (2 + 4), 880, (23 << 4) + (3 + 4), 740, (23 << 4) + (4 + 4), 630, (23 << 4) + (5 + 4), 590, (24 << 4) + (1 + 4), 810, (24 << 4) + (2 + 4), 890, (24 << 4) + (3 + 4), 630, (24 << 4) + (6 + 4), 520, (25 << 4) + (2 + 4), 800, (25 << 4) + (3 + 4), 660, (25 << 4) + (4 + 4), 600, (25 << 4) + (7 + 4), 460, (26 << 4) + (2 + 4), 740, (26 << 4) + (3 + 4), 640, (27 << 4) + (2 + 4), 720, (27 << 4) + (3 + 4), 630, (28 << 4) + (2 + 4), 690, (29 << 4) + (1 + 4), 960, (29 << 4) + (2 + 4), 720, (30 << 4) + (1 + 4), 880, (30 << 4) + (2 + 4), 740, (31 << 4) + (1 + 4), 810, (31 << 4) + (3 + 4), 620, (32 << 4) + (2 + 4), 730, (32 << 4) + (4 + 4), 530, (33 << 4) + (3 + 4), 580, (33 << 4) + (5 + 4), 460, (34 << 4) + (1 + 4), 660, (34 << 4) + (4 + 4), 500, (34 << 4) + (6 + 4), 420, (35 << 4) + (5 + 4), 470, (35 << 4) + (7 + 4), 390, (37 << 4) + (1 + 4), 1470, (38 << 4) + (2 + 4), 1120, (39 << 4) + (3 + 4), 893, (40 << 4) + (1 + 4), 1090, (40 << 4) + (4 + 4), 790, (41 << 4) + (1 + 4), 1000, (41 << 4) + (4 + 4), 740, (41 << 4) + (5 + 4), 690, (42 << 4) + (1 + 4), 930, (42 << 4) + (4 + 4), 700, (42 << 4) + (6 + 4), 620, (43 << 4) + (7 + 4), 979, (44 << 4) + (4 + 4), 670, (45 << 4) + (3 + 4), 680, (46 << 4) + (2 + 4), 800, (46 << 4) + (4 + 4), 650, (47 << 4) + (1 + 4), 1260, (47 << 4) + (2 + 4), 890, (48 << 4) + (1 + 4), 1140, (48 << 4) + (2 + 4), 970, (49 << 4) + (3 + 4), 810, (50 << 4) + (2 + 4), 930, (50 << 4) + (4 + 4), 710, (51 << 4) + (3 + 4), 760, (51 << 4) + (5 + 4), 620, (52 << 4) + (1 + 4), 820, (52 << 4) + (4 + 4), 700, (52 << 4) + (6 + 4), 560, (53 << 4) + (5 + 4), 620, (53 << 4) + (7 + 4), 500, (55 << 4) + (1 + 4), 1670, (56 << 4) + (1 + 4), 1530, (56 << 4) + (2 + 4), 1340, (57 << 4) + (1 + 4), 1390, (57 << 4) + (3 + 4), 1016, (58 << 4) + (1 + 4), 1270, (58 << 4) + (3 + 4), 1034, (58 << 4) + (4 + 4), 920, (59 << 4) + (3 + 4), 1013, (59 << 4) + (4 + 4), 900, (60 << 4) + (3 + 4), 995, (61 << 4) + (3 + 4), 979, (62 << 4) + (3 + 4), 964, (63 << 4) + (2 + 4), 1090, (63 << 4) + (3 + 4), 950, (64 << 4) + (3 + 4), 938, (65 << 4) + (3 + 4), 923, (65 << 4) + (4 + 4), 840, (66 << 4) + (3 + 4), 908, (67 << 4) + (3 + 4), 894, (68 << 4) + (3 + 4), 881, (69 << 4) + (3 + 4), 870, (70 << 4) + (2 + 4), 930, (70 << 4) + (3 + 4), 858, (71 << 4) + (3 + 4), 850, (72 << 4) + (4 + 4), 780, (73 << 4) + (5 + 4), 680, (74 << 4) + (4 + 4), 700, (74 << 4) + (6 + 4), 620, (75 << 4) + (4 + 4), 720, (75 << 4) + (7 + 4), 560, (76 << 4) + (4 + 4), 880, (76 << 4) + (6 + 4), 690, (77 << 4) + (4 + 4), 680, (78 << 4) + (2 + 4), 800, (78 << 4) + (4 + 4), 650, (79 << 4) + (1 + 4), 1370, (79 << 4) + (3 + 4), 850, (80 << 4) + (1 + 4), 1270, (80 << 4) + (2 + 4), 1100, (81 << 4) + (1 + 4), 1470, (81 << 4) + (3 + 4), 950, (82 << 4) + (2 + 4), 1200, (82 << 4) + (4 + 4), 840, (83 << 4) + (1 + 4), 980, (83 << 4) + (3 + 4), 960, (83 << 4) + (5 + 4), 740, (84 << 4) + (6 + 4), 670, (85 << 4) + (7 + 4), 620, (87 << 4) + (1 + 4), 1800, (88 << 4) + (2 + 4), 1430, (89 << 4) + (3 + 4), 1180, (90 << 4) + (4 + 4), 1020, (91 << 4) + (3 + 4), 1130, (91 << 4) + (4 + 4), 980, (91 << 4) + (5 + 4), 890, (92 << 4) + (4 + 4), 970, (92 << 4) + (6 + 4), 800, (93 << 4) + (3 + 4), 1100, (93 << 4) + (4 + 4), 950, (93 << 4) + (7 + 4), 710, (94 << 4) + (3 + 4), 1080, (94 << 4) + (4 + 4), 930, (95 << 4) + (3 + 4), 1070, (95 << 4) + (4 + 4), 920 };

    private static final short[] anionLookupTable = { (1 << 4) + (-1 + 4), 1540, (6 << 4) + (-4 + 4), 2600, (7 << 4) + (-3 + 4), 1710, (8 << 4) + (-2 + 4), 1360, (8 << 4) + (-1 + 4), 680, (9 << 4) + (-1 + 4), 1330, (15 << 4) + (-3 + 4), 2120, (16 << 4) + (-2 + 4), 1840, (17 << 4) + (-1 + 4), 1810, (32 << 4) + (-4 + 4), 2720, (33 << 4) + (-3 + 4), 2220, (34 << 4) + (-2 + 4), 1980, (35 << 4) + (-1 + 4), 1960, (50 << 4) + (-4 + 4), 2940, (50 << 4) + (-1 + 4), 3700, (51 << 4) + (-3 + 4), 2450, (52 << 4) + (-2 + 4), 2110, (52 << 4) + (-1 + 4), 2500, (53 << 4) + (-1 + 4), 2200 };

    private static final BitSet bsCations = new BitSet();

    private static final BitSet bsAnions = new BitSet();

    static {
        for (int i = 0; i < anionLookupTable.length; i += 2) bsAnions.set(anionLookupTable[i] >> 4);
        for (int i = 0; i < cationLookupTable.length; i += 2) bsCations.set(cationLookupTable[i] >> 4);
    }

    public static float getBondingRadiusFloat(short atomicNumberAndIsotope, int charge) {
        int atomicNumber = getElementNumber(atomicNumberAndIsotope);
        if (charge > 0 && bsCations.get(atomicNumber)) return getBondingRadiusFloat(atomicNumber, charge, cationLookupTable);
        if (charge < 0 && bsAnions.get(atomicNumber)) return getBondingRadiusFloat(atomicNumber, charge, anionLookupTable);
        return covalentMars[atomicNumber] / 1000f;
    }

    public static float getBondingRadiusFloat(int atomicNumber, int charge, short[] table) {
        short ionic = (short) ((atomicNumber << 4) + (charge + 4));
        int iVal = 0, iMid = 0, iMin = 0, iMax = table.length / 2;
        while (iMin != iMax) {
            iMid = (iMin + iMax) / 2;
            iVal = table[iMid << 1];
            if (iVal > ionic) iMax = iMid; else if (iVal < ionic) iMin = iMid + 1; else return table[(iMid << 1) + 1] / 1000f;
        }
        if (iVal > ionic) iMid--;
        iVal = table[iMid << 1];
        if (atomicNumber != (iVal >> 4)) iMid++;
        return table[(iMid << 1) + 1] / 1000f;
    }

    public static int getVanderwaalsMar(int i, EnumVdw type) {
        return vanderwaalsMars[(i << 2) + (type.pt % 4)];
    }

    public static float getHydrophobicity(int i) {
        return (i < 1 || i >= Elements.hydrophobicities.length ? 0 : Elements.hydrophobicities[i]);
    }

    private static final float[] hydrophobicities = { 0f, 0.62f, -2.53f, -0.78f, -0.90f, 0.29f, -0.85f, -0.74f, 0.48f, -0.40f, 1.38f, 1.06f, -1.50f, 0.64f, 1.19f, 0.12f, -0.18f, -0.05f, 0.81f, 0.26f, 1.08f };

    static {
        if ((elementNames.length != elementNumberMax) || (vanderwaalsMars.length / 4 != elementNumberMax) || (covalentMars.length != elementNumberMax)) {
            Logger.error("ERROR!!! Element table length mismatch:" + "\n elementSymbols.length=" + elementSymbols.length + "\n elementNames.length=" + elementNames.length + "\n vanderwaalsMars.length=" + vanderwaalsMars.length + "\n covalentMars.length=" + covalentMars.length);
        }
    }
}
