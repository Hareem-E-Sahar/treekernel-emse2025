package org.jmol.viewer;

import java.util.Hashtable;

public final class JmolConstants {

    public static final String copyright = "(C) 2006 Jmol Development";

    public static final String version = "10.00.56";

    public static final String cvsDate = "$Date: 2006-04-04 10:33:19 -0400 (Tue, 04 Apr 2006) $";

    public static final String date = cvsDate.substring(7, 23);

    public static final boolean officialRelease = false;

    public static final short MAR_DELETED = Short.MIN_VALUE;

    public static final int MOUSE_ROTATE = 0;

    public static final int MOUSE_ZOOM = 1;

    public static final int MOUSE_XLATE = 2;

    public static final int MOUSE_PICK = 3;

    public static final int MOUSE_DELETE = 4;

    public static final int MOUSE_MEASURE = 5;

    public static final int MOUSE_ROTATE_Z = 6;

    public static final int MOUSE_SLAB_PLANE = 7;

    public static final int MOUSE_POPUP_MENU = 8;

    public static final byte MULTIBOND_NEVER = 0;

    public static final byte MULTIBOND_WIREFRAME = 1;

    public static final byte MULTIBOND_SMALL = 2;

    public static final byte MULTIBOND_ALWAYS = 3;

    public static final short madMultipleBondSmallMaximum = 500;

    /**
   * picking modes
   */
    public static final int PICKING_OFF = 0;

    public static final int PICKING_IDENT = 1;

    public static final int PICKING_DISTANCE = 2;

    public static final int PICKING_MONITOR = 3;

    public static final int PICKING_ANGLE = 4;

    public static final int PICKING_TORSION = 5;

    public static final int PICKING_LABEL = 6;

    public static final int PICKING_CENTER = 7;

    public static final int PICKING_COORD = 8;

    public static final int PICKING_BOND = 9;

    public static final int PICKING_SELECT_ATOM = 10;

    public static final int PICKING_SELECT_GROUP = 11;

    public static final int PICKING_SELECT_CHAIN = 12;

    public static final String[] pickingModeNames = { "off", "ident", "distance", "monitor", "angle", "torsion", "label", "center", "coord", "bond", "atom", "group", "chain" };

    /**
   * Extended Bond Definition Types
   *
   */
    public static final short BOND_COVALENT_SINGLE = 1;

    public static final short BOND_COVALENT_DOUBLE = 2;

    public static final short BOND_COVALENT_TRIPLE = 3;

    public static final short BOND_COVALENT_MASK = 3;

    public static final short BOND_AROMATIC_MASK = (1 << 2);

    public static final short BOND_AROMATIC = (1 << 2) | 1;

    public static final short BOND_STEREO_MASK = (3 << 3);

    public static final short BOND_STEREO_NEAR = (1 << 3) | 1;

    public static final short BOND_STEREO_FAR = (2 << 3) | 2;

    public static final short BOND_SULFUR_MASK = (1 << 5);

    public static final short BOND_HBOND_SHIFT = 6;

    public static final short BOND_HYDROGEN_MASK = (0x0F << BOND_HBOND_SHIFT);

    public static final short BOND_H_REGULAR = (1 << BOND_HBOND_SHIFT);

    public static final short BOND_H_PLUS_2 = (2 << BOND_HBOND_SHIFT);

    public static final short BOND_H_PLUS_3 = (3 << BOND_HBOND_SHIFT);

    public static final short BOND_H_PLUS_4 = (4 << BOND_HBOND_SHIFT);

    public static final short BOND_H_PLUS_5 = (5 << BOND_HBOND_SHIFT);

    public static final short BOND_H_MINUS_3 = (6 << BOND_HBOND_SHIFT);

    public static final short BOND_H_MINUS_4 = (7 << BOND_HBOND_SHIFT);

    public static final short BOND_H_NUCLEOTIDE = (8 << BOND_HBOND_SHIFT);

    public static final short BOND_PARTIAL01 = (1 << 10);

    public static final short BOND_PARTIAL12 = (1 << 11);

    public static final short BOND_ALL_MASK = (short) 0xFFFF;

    static final String[] bondOrderNames = { "single", "double", "triple", "aromatic", "hbond" };

    static final short[] bondOrderValues = { BOND_COVALENT_SINGLE, BOND_COVALENT_DOUBLE, BOND_COVALENT_TRIPLE, BOND_AROMATIC, BOND_H_REGULAR };

    static final float ANGSTROMS_PER_BOHR = 0.5291772f;

    public static final int[] argbsHbondType = { 0xFFFF69B4, 0xFFFFFF00, 0xFFFFFFFF, 0xFFFF00FF, 0xFFFF0000, 0xFFFFA500, 0xFF00FFFF, 0xFF00FF00, 0xFFFF8080 };

    /**
   * The default elementSymbols. Presumably the only entry which may cause
   * confusion is element 0, whose symbol we have defined as "Xx". 
   */
    public static final String[] elementSymbols = { "Xx", "H", "He", "Li", "Be", "B", "C", "N", "O", "F", "Ne", "Na", "Mg", "Al", "Si", "P", "S", "Cl", "Ar", "K", "Ca", "Sc", "Ti", "V", "Cr", "Mn", "Fe", "Co", "Ni", "Cu", "Zn", "Ga", "Ge", "As", "Se", "Br", "Kr", "Rb", "Sr", "Y", "Zr", "Nb", "Mo", "Tc", "Ru", "Rh", "Pd", "Ag", "Cd", "In", "Sn", "Sb", "Te", "I", "Xe", "Cs", "Ba", "La", "Ce", "Pr", "Nd", "Pm", "Sm", "Eu", "Gd", "Tb", "Dy", "Ho", "Er", "Tm", "Yb", "Lu", "Hf", "Ta", "W", "Re", "Os", "Ir", "Pt", "Au", "Hg", "Tl", "Pb", "Bi", "Po", "At", "Rn", "Fr", "Ra", "Ac", "Th", "Pa", "U", "Np", "Pu", "Am", "Cm", "Bk", "Cf", "Es", "Fm", "Md", "No", "Lr", "Rf", "Db", "Sg", "Bh", "Hs", "Mt" };

    /**
   * one larger than the last elementNumber, same as elementSymbols.length
   */
    public static final int elementNumberMax = elementSymbols.length;

    private static Hashtable htElementMap;

    /**
   * @param elementSymbol First char must be upper case, second char accepts upper or lower case
   * @return elementNumber
   */
    public static byte elementNumberFromSymbol(String elementSymbol) {
        if (htElementMap == null) {
            Hashtable map = new Hashtable();
            for (int elementNumber = elementNumberMax; --elementNumber >= 0; ) {
                String symbol = elementSymbols[elementNumber];
                Integer boxed = new Integer(elementNumber);
                map.put(symbol, boxed);
                if (symbol.length() == 2) {
                    symbol = "" + symbol.charAt(0) + Character.toUpperCase(symbol.charAt(1));
                    map.put(symbol, boxed);
                }
                if (elementNumber == 1) {
                    map.put("D", boxed);
                }
            }
            htElementMap = map;
        }
        if (elementSymbol == null) return 0;
        Integer boxedAtomicNumber = (Integer) htElementMap.get(elementSymbol);
        if (boxedAtomicNumber != null) return (byte) boxedAtomicNumber.intValue();
        System.out.println("" + elementSymbol + "' is not a recognized symbol");
        return 0;
    }

    public static final String elementNames[] = { "unknown", "hydrogen", "helium", "lithium", "beryllium", "boron", "carbon", "nitrogen", "oxygen", "fluorine", "neon", "sodium", "magnesium", "aluminum", "silicon", "phosphorus", "sulfur", "chlorine", "argon", "potassium", "calcium", "scandium", "titanium", "vanadium", "chromium", "manganese", "iron", "cobalt", "nickel", "copper", "zinc", "gallium", "germanium", "arsenic", "selenium", "bromine", "krypton", "rubidium", "strontium", "yttrium", "zirconium", "niobium", "molybdenum", "technetium", "ruthenium", "rhodium", "palladium", "silver", "cadmium", "indium", "tin", "antimony", "tellurium", "iodine", "xenon", "cesium", "barium", "lanthanum", "cerium", "praseodymium", "neodymium", "promethium", "samarium", "europium", "gadolinium", "terbium", "dysprosium", "holmium", "erbium", "thulium", "ytterbium", "lutetium", "hafnium", "tantalum", "tungsten", "rhenium", "osmium", "iridium", "platinum", "gold", "mercury", "thallium", "lead", "bismuth", "polonium", "astatine", "radon", "francium", "radium", "actinium", "thorium", "protactinium", "uranium", "neptunium", "plutonium", "americium", "curium", "berkelium", "californium", "einsteinium", "fermium", "mendelevium", "nobelium", "lawrencium", "rutherfordium", "dubnium", "seaborgium", "bohrium", "hassium", "meitnerium" };

    public static final byte[] alternateElementNumbers = { 0, 13, 16, 55 };

    public static final String[] alternateElementNames = { "dummy", "aluminium", "sulphur", "caesium" };

    /**
   * Default table of van der Waals Radii.
   * values are stored as MAR -- Milli Angstrom Radius
   * Used for spacefill rendering of atoms.
   * Values taken from OpenBabel.
   * @see <a href="http://openbabel.sourceforge.net">openbabel.sourceforge.net</a>
   */
    public static final short[] vanderwaalsMars = { 1000, 1200, 1400, 1820, 1700, 2080, 1950, 1850, 1700, 1730, 1540, 2270, 1730, 2050, 2100, 2080, 2000, 1970, 1880, 2750, 1973, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1630, 1400, 1390, 1870, 1700, 1850, 1900, 2100, 2020, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1630, 1720, 1580, 1930, 2170, 2200, 2060, 2150, 2160, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1720, 1660, 1550, 1960, 2020, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1860, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700, 1700 };

    /**
   * Default table of covalent Radii
   * stored as a short mar ... Milli Angstrom Radius
   * Values taken from OpenBabel.
   * @see <a href="http://openbabel.sourceforge.net">openbabel.sourceforge.net</a>
   */
    private static final short[] covalentMars = { 0, 230, 930, 680, 350, 830, 680, 680, 680, 640, 1120, 970, 1100, 1350, 1200, 750, 1020, 990, 1570, 1330, 990, 1440, 1470, 1330, 1350, 1350, 1340, 1330, 1500, 1520, 1450, 1220, 1170, 1210, 1220, 1210, 1910, 1470, 1120, 1780, 1560, 1480, 1470, 1350, 1400, 1450, 1500, 1590, 1690, 1630, 1460, 1460, 1470, 1400, 1980, 1670, 1340, 1870, 1830, 1820, 1810, 1800, 1800, 1990, 1790, 1760, 1750, 1740, 1730, 1720, 1940, 1720, 1570, 1430, 1370, 1350, 1370, 1320, 1500, 1500, 1700, 1550, 1540, 1540, 1680, 1700, 2400, 2000, 1900, 1880, 1790, 1610, 1580, 1550, 1530, 1510, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1500, 1600, 1600, 1600, 1600, 1600, 1600 };

    /****************************************************************
   * ionic radii are looked up using a pair of parallel arrays
   * the ionicLookupTable contains both the elementNumber
   * and the ionization value, represented as follows:
   *   (elementNumber << 4) + (ionizationValue + 4)
   * if you don't understand this representation, don't worry about
   * the binary shifting and stuff. It is just a sorted list
   * of keys
   *
   * the values are stored in the ionicMars table
   * these two arrays are parallel
   *
   * This data is from
   *  Handbook of Chemistry and Physics. 48th Ed, 1967-8, p. F143
   *  (scanned for Jmol by Phillip Barak, Jan 2004)
   ****************************************************************/
    public static final int FORMAL_CHARGE_MIN = -4;

    public static final int FORMAL_CHARGE_MAX = 7;

    public static final short[] ionicLookupTable = { (1 << 4) + (-1 + 4), (3 << 4) + (1 + 4), (4 << 4) + (1 + 4), (4 << 4) + (2 + 4), (5 << 4) + (1 + 4), (5 << 4) + (3 + 4), (6 << 4) + (-4 + 4), (6 << 4) + (4 + 4), (7 << 4) + (-3 + 4), (7 << 4) + (1 + 4), (7 << 4) + (3 + 4), (7 << 4) + (5 + 4), (8 << 4) + (-2 + 4), (8 << 4) + (-1 + 4), (8 << 4) + (1 + 4), (8 << 4) + (6 + 4), (9 << 4) + (-1 + 4), (9 << 4) + (7 + 4), (10 << 4) + (1 + 4), (11 << 4) + (1 + 4), (12 << 4) + (1 + 4), (12 << 4) + (2 + 4), (13 << 4) + (3 + 4), (14 << 4) + (-4 + 4), (14 << 4) + (-1 + 4), (14 << 4) + (1 + 4), (14 << 4) + (4 + 4), (15 << 4) + (-3 + 4), (15 << 4) + (3 + 4), (15 << 4) + (5 + 4), (16 << 4) + (-2 + 4), (16 << 4) + (2 + 4), (16 << 4) + (4 + 4), (16 << 4) + (6 + 4), (17 << 4) + (-1 + 4), (17 << 4) + (5 + 4), (17 << 4) + (7 + 4), (18 << 4) + (1 + 4), (19 << 4) + (1 + 4), (20 << 4) + (1 + 4), (20 << 4) + (2 + 4), (21 << 4) + (3 + 4), (22 << 4) + (1 + 4), (22 << 4) + (2 + 4), (22 << 4) + (3 + 4), (22 << 4) + (4 + 4), (23 << 4) + (2 + 4), (23 << 4) + (3 + 4), (23 << 4) + (4 + 4), (23 << 4) + (5 + 4), (24 << 4) + (1 + 4), (24 << 4) + (2 + 4), (24 << 4) + (3 + 4), (24 << 4) + (6 + 4), (25 << 4) + (2 + 4), (25 << 4) + (3 + 4), (25 << 4) + (4 + 4), (25 << 4) + (7 + 4), (26 << 4) + (2 + 4), (26 << 4) + (3 + 4), (27 << 4) + (2 + 4), (27 << 4) + (3 + 4), (28 << 4) + (2 + 4), (29 << 4) + (1 + 4), (29 << 4) + (2 + 4), (30 << 4) + (1 + 4), (30 << 4) + (2 + 4), (31 << 4) + (1 + 4), (31 << 4) + (3 + 4), (32 << 4) + (-4 + 4), (32 << 4) + (2 + 4), (32 << 4) + (4 + 4), (33 << 4) + (-3 + 4), (33 << 4) + (3 + 4), (33 << 4) + (5 + 4), (34 << 4) + (-2 + 4), (34 << 4) + (-1 + 4), (34 << 4) + (1 + 4), (34 << 4) + (4 + 4), (34 << 4) + (6 + 4), (35 << 4) + (-1 + 4), (35 << 4) + (5 + 4), (35 << 4) + (7 + 4), (37 << 4) + (1 + 4), (38 << 4) + (2 + 4), (39 << 4) + (3 + 4), (40 << 4) + (1 + 4), (40 << 4) + (4 + 4), (41 << 4) + (1 + 4), (41 << 4) + (4 + 4), (41 << 4) + (5 + 4), (42 << 4) + (1 + 4), (42 << 4) + (4 + 4), (42 << 4) + (6 + 4), (43 << 4) + (7 + 4), (44 << 4) + (4 + 4), (45 << 4) + (3 + 4), (46 << 4) + (2 + 4), (46 << 4) + (4 + 4), (47 << 4) + (1 + 4), (47 << 4) + (2 + 4), (48 << 4) + (1 + 4), (48 << 4) + (2 + 4), (49 << 4) + (3 + 4), (50 << 4) + (-4 + 4), (50 << 4) + (-1 + 4), (50 << 4) + (2 + 4), (50 << 4) + (4 + 4), (51 << 4) + (-3 + 4), (51 << 4) + (3 + 4), (51 << 4) + (5 + 4), (52 << 4) + (-2 + 4), (52 << 4) + (-1 + 4), (52 << 4) + (1 + 4), (52 << 4) + (4 + 4), (52 << 4) + (6 + 4), (53 << 4) + (-1 + 4), (53 << 4) + (5 + 4), (53 << 4) + (7 + 4), (55 << 4) + (1 + 4), (56 << 4) + (1 + 4), (56 << 4) + (2 + 4), (57 << 4) + (1 + 4), (57 << 4) + (3 + 4), (58 << 4) + (1 + 4), (58 << 4) + (3 + 4), (58 << 4) + (4 + 4), (59 << 4) + (3 + 4), (59 << 4) + (4 + 4), (60 << 4) + (3 + 4), (61 << 4) + (3 + 4), (62 << 4) + (3 + 4), (63 << 4) + (2 + 4), (63 << 4) + (3 + 4), (64 << 4) + (3 + 4), (65 << 4) + (3 + 4), (65 << 4) + (4 + 4), (66 << 4) + (3 + 4), (67 << 4) + (3 + 4), (68 << 4) + (3 + 4), (69 << 4) + (3 + 4), (70 << 4) + (2 + 4), (70 << 4) + (3 + 4), (71 << 4) + (3 + 4), (72 << 4) + (4 + 4), (73 << 4) + (5 + 4), (74 << 4) + (4 + 4), (74 << 4) + (6 + 4), (75 << 4) + (4 + 4), (75 << 4) + (7 + 4), (76 << 4) + (4 + 4), (76 << 4) + (6 + 4), (77 << 4) + (4 + 4), (78 << 4) + (2 + 4), (78 << 4) + (4 + 4), (79 << 4) + (1 + 4), (79 << 4) + (3 + 4), (80 << 4) + (1 + 4), (80 << 4) + (2 + 4), (81 << 4) + (1 + 4), (81 << 4) + (3 + 4), (82 << 4) + (2 + 4), (82 << 4) + (4 + 4), (83 << 4) + (1 + 4), (83 << 4) + (3 + 4), (83 << 4) + (5 + 4), (84 << 4) + (6 + 4), (85 << 4) + (7 + 4), (87 << 4) + (1 + 4), (88 << 4) + (2 + 4), (89 << 4) + (3 + 4), (90 << 4) + (4 + 4), (91 << 4) + (3 + 4), (91 << 4) + (4 + 4), (91 << 4) + (5 + 4), (92 << 4) + (4 + 4), (92 << 4) + (6 + 4), (93 << 4) + (3 + 4), (93 << 4) + (4 + 4), (93 << 4) + (7 + 4), (94 << 4) + (3 + 4), (94 << 4) + (4 + 4), (95 << 4) + (3 + 4), (95 << 4) + (4 + 4) };

    public static final short[] ionicMars = { 1540, 680, 440, 350, 350, 230, 2600, 160, 1710, 250, 160, 130, 1320, 1760, 220, 90, 1330, 80, 1120, 970, 820, 660, 510, 2710, 3840, 650, 420, 2120, 440, 350, 1840, 2190, 370, 300, 1810, 340, 270, 1540, 1330, 1180, 990, 732, 960, 940, 760, 680, 880, 740, 630, 590, 810, 890, 630, 520, 800, 660, 600, 460, 740, 640, 720, 630, 690, 960, 720, 880, 740, 810, 620, 2720, 730, 530, 2220, 580, 460, 1910, 2320, 660, 500, 420, 1960, 470, 390, 1470, 1120, 893, 1090, 790, 1000, 740, 690, 930, 700, 620, 979, 670, 680, 800, 650, 1260, 890, 1140, 970, 810, 2940, 3700, 930, 710, 2450, 760, 620, 2110, 2500, 820, 700, 560, 2200, 620, 500, 1670, 1530, 1340, 1390, 1016, 1270, 1034, 920, 1013, 900, 995, 979, 964, 1090, 950, 938, 923, 840, 908, 894, 881, 870, 930, 858, 850, 780, 680, 700, 620, 720, 560, 880, 690, 680, 800, 650, 1370, 850, 1270, 1100, 1470, 950, 1200, 840, 980, 960, 740, 670, 620, 1800, 1430, 1180, 1020, 1130, 980, 890, 970, 800, 1100, 950, 710, 1080, 930, 1070, 920 };

    public static short getBondingMar(int elementNumber, int charge) {
        if (charge != 0) {
            short ionic = (short) ((elementNumber << 4) + (charge + 4));
            int iMin = 0, iMax = ionicLookupTable.length;
            while (iMin != iMax) {
                int iMid = (iMin + iMax) / 2;
                if (ionic < ionicLookupTable[iMid]) iMax = iMid; else if (ionic > ionicLookupTable[iMid]) iMin = iMid + 1; else return ionicMars[iMid];
            }
        }
        return (short) covalentMars[elementNumber];
    }

    public static final int MAXIMUM_AUTO_BOND_COUNT = 20;

    /**
   * Default table of CPK atom colors.
   * ghemical colors with a few proposed modifications
   */
    public static final int[] argbsCpk = { 0xFFFF1493, 0xFFFFFFFF, 0xFFD9FFFF, 0xFFCC80FF, 0xFFC2FF00, 0xFFFFB5B5, 0xFF909090, 0xFF3050F8, 0xFFFF0D0D, 0xFF90E050, 0xFFB3E3F5, 0xFFAB5CF2, 0xFF8AFF00, 0xFFBFA6A6, 0xFFF0C8A0, 0xFFFF8000, 0xFFFFFF30, 0xFF1FF01F, 0xFF80D1E3, 0xFF8F40D4, 0xFF3DFF00, 0xFFE6E6E6, 0xFFBFC2C7, 0xFFA6A6AB, 0xFF8A99C7, 0xFF9C7AC7, 0xFFE06633, 0xFFF090A0, 0xFF50D050, 0xFFC88033, 0xFF7D80B0, 0xFFC28F8F, 0xFF668F8F, 0xFFBD80E3, 0xFFFFA100, 0xFFA62929, 0xFF5CB8D1, 0xFF702EB0, 0xFF00FF00, 0xFF94FFFF, 0xFF94E0E0, 0xFF73C2C9, 0xFF54B5B5, 0xFF3B9E9E, 0xFF248F8F, 0xFF0A7D8C, 0xFF006985, 0xFFC0C0C0, 0xFFFFD98F, 0xFFA67573, 0xFF668080, 0xFF9E63B5, 0xFFD47A00, 0xFF940094, 0xFF429EB0, 0xFF57178F, 0xFF00C900, 0xFF70D4FF, 0xFFFFFFC7, 0xFFD9FFC7, 0xFFC7FFC7, 0xFFA3FFC7, 0xFF8FFFC7, 0xFF61FFC7, 0xFF45FFC7, 0xFF30FFC7, 0xFF1FFFC7, 0xFF00FF9C, 0xFF00E675, 0xFF00D452, 0xFF00BF38, 0xFF00AB24, 0xFF4DC2FF, 0xFF4DA6FF, 0xFF2194D6, 0xFF267DAB, 0xFF266696, 0xFF175487, 0xFFD0D0E0, 0xFFFFD123, 0xFFB8B8D0, 0xFFA6544D, 0xFF575961, 0xFF9E4FB5, 0xFFAB5C00, 0xFF754F45, 0xFF428296, 0xFF420066, 0xFF007D00, 0xFF70ABFA, 0xFF00BAFF, 0xFF00A1FF, 0xFF008FFF, 0xFF0080FF, 0xFF006BFF, 0xFF545CF2, 0xFF785CE3, 0xFF8A4FE3, 0xFFA136D4, 0xFFB31FD4, 0xFFB31FBA, 0xFFB30DA6, 0xFFBD0D87, 0xFFC70066, 0xFFCC0059, 0xFFD1004F, 0xFFD90045, 0xFFE00038, 0xFFE6002E, 0xFFEB0026 };

    public static final int[] argbsCpkRasmol = { 0x00FF1493 + (0 << 24), 0x00FFFFFF + (1 << 24), 0x00FFC0CB + (2 << 24), 0x00B22222 + (3 << 24), 0x0000FF00 + (5 << 24), 0x00C8C8C8 + (6 << 24), 0x008F8FFF + (7 << 24), 0x00F00000 + (8 << 24), 0x00DAA520 + (9 << 24), 0x000000FF + (11 << 24), 0x00228B22 + (12 << 24), 0x00808090 + (13 << 24), 0x00DAA520 + (14 << 24), 0x00FFA500 + (15 << 24), 0x00FFC832 + (16 << 24), 0x0000FF00 + (17 << 24), 0x00808090 + (20 << 24), 0x00808090 + (22 << 24), 0x00808090 + (24 << 24), 0x00808090 + (25 << 24), 0x00FFA500 + (26 << 24), 0x00A52A2A + (28 << 24), 0x00A52A2A + (29 << 24), 0x00A52A2A + (30 << 24), 0x00A52A2A + (35 << 24), 0x00808090 + (47 << 24), 0x00A020F0 + (53 << 24), 0x00FFA500 + (56 << 24), 0x00DAA520 + (79 << 24) };

    static {
        if ((elementSymbols.length != elementNames.length) || (elementSymbols.length != vanderwaalsMars.length) || (elementSymbols.length != covalentMars.length) || (elementSymbols.length != argbsCpk.length)) {
            System.out.println("ERROR!!! Element table length mismatch:" + "\n elementSymbols.length=" + elementSymbols.length + "\n elementNames.length=" + elementNames.length + "\n vanderwaalsMars.length=" + vanderwaalsMars.length + "\n covalentMars.length=" + covalentMars.length + "\n argbsCpk.length=" + argbsCpk.length);
        }
    }

    /**
   * Default table of PdbStructure colors
   */
    public static final byte PROTEIN_STRUCTURE_NONE = 0;

    public static final byte PROTEIN_STRUCTURE_TURN = 1;

    public static final byte PROTEIN_STRUCTURE_SHEET = 2;

    public static final byte PROTEIN_STRUCTURE_HELIX = 3;

    public static final byte PROTEIN_STRUCTURE_DNA = 4;

    public static final byte PROTEIN_STRUCTURE_RNA = 5;

    /****************************************************************
   * In DRuMS, RasMol, and Chime, quoting from
   * http://www.umass.edu/microbio/rasmol/rascolor.htm
   *
   *The RasMol structure color scheme colors the molecule by
   *protein secondary structure.
   *
   *Structure                   Decimal RGB    Hex RGB
   *Alpha helices  red-magenta  [255,0,128]    FF 00 80  *
   *Beta strands   yellow       [255,200,0]    FF C8 00  *
   *
   *Turns          pale blue    [96,128,255]   60 80 FF
   *Other          white        [255,255,255]  FF FF FF
   *
   **Values given in the 1994 RasMol 2.5 Quick Reference Card ([240,0,128]
   *and [255,255,0]) are not correct for RasMol 2.6-beta-2a.
   *This correction was made above on Dec 5, 1998.
   ****************************************************************/
    public static final int[] argbsStructure = { 0xFFFFFFFF, 0xFF6080FF, 0xFFFFC800, 0xFFFF0080, 0xFFAE00FE, 0xFFFD0162 };

    public static final int[] argbsAmino = { 0xFFBEA06E, 0xFFC8C8C8, 0xFF145AFF, 0xFF00DCDC, 0xFFE60A0A, 0xFFE6E600, 0xFF00DCDC, 0xFFE60A0A, 0xFFEBEBEB, 0xFF8282D2, 0xFF0F820F, 0xFF0F820F, 0xFF145AFF, 0xFFE6E600, 0xFF3232AA, 0xFFDC9682, 0xFFFA9600, 0xFFFA9600, 0xFFB45AB4, 0xFF3232AA, 0xFF0F820F, 0xFFFF69B4, 0xFFFF69B4 };

    public static final int argbShapelyBackbone = 0xFFB8B8B8;

    public static final int argbShapelySpecial = 0xFF5E005E;

    public static final int argbShapelyDefault = 0xFFFF00FF;

    public static final int[] argbsShapely = { 0xFFFF00FF, 0xFF8CFF8C, 0xFF00007C, 0xFFFF7C70, 0xFFA00042, 0xFFFFFF70, 0xFFFF4C4C, 0xFF660000, 0xFFFFFFFF, 0xFF7070FF, 0xFF004C00, 0xFF455E45, 0xFF4747B8, 0xFFB8A042, 0xFF534C52, 0xFF525252, 0xFFFF7042, 0xFFB84C00, 0xFF4F4600, 0xFF8C704C, 0xFFFF8CFF, 0xFFFF00FF, 0xFFFF00FF, 0xFFFF00FF, 0xFFA0A0FF, 0xFFA0A0FF, 0xFFFF7070, 0xFFFF7070, 0xFF80FFFF, 0xFF80FFFF, 0xFFFF8C4B, 0xFFFF8C4B, 0xFFA0FFA0, 0xFFA0FFA0, 0xFFFF8080, 0xFFFF8080 };

    /****************************************************************
   * some pastel colors
   * 
   * C0D0FF - pastel blue
   * B0FFB0 - pastel green
   * B0FFFF - pastel cyan
   * FFC0C8 - pink
   * FFC0FF - pastel magenta
   * FFFF80 - pastel yellow
   * FFDEAD - navajowhite
   * FFD070 - pastel gold

   * FF9898 - light coral
   * B4E444 - light yellow-green
   * C0C000 - light olive
   * FF8060 - light tomato
   * 00FF7F - springgreen
   * 
cpk on; select atomno>100; label %i; color chain; select selected & hetero; cpk off
   ****************************************************************/
    public static final int[] argbsChainAtom = { 0xFFffffff, 0xFFC0D0FF, 0xFFB0FFB0, 0xFFFFC0C8, 0xFFFFFF80, 0xFFFFC0FF, 0xFFB0F0F0, 0xFFFFD070, 0xFFF08080, 0xFFF5DEB3, 0xFF00BFFF, 0xFFCD5C5C, 0xFF66CDAA, 0xFF9ACD32, 0xFFEE82EE, 0xFF00CED1, 0xFF00FF7F, 0xFF3CB371, 0xFF00008B, 0xFFBDB76B, 0xFF006400, 0xFF800000, 0xFF808000, 0xFF800080, 0xFF008080, 0xFFB8860B, 0xFFB22222 };

    public static final int[] argbsChainHetero = { 0xFFffffff, 0xFFC0D0FF - 0x00303030, 0xFFB0FFB0 - 0x00303018, 0xFFFFC0C8 - 0x00303018, 0xFFFFFF80 - 0x00303010, 0xFFFFC0FF - 0x00303030, 0xFFB0F0F0 - 0x00303030, 0xFFFFD070 - 0x00303010, 0xFFF08080 - 0x00303010, 0xFFF5DEB3 - 0x00303030, 0xFF00BFFF - 0x00001830, 0xFFCD5C5C - 0x00181010, 0xFF66CDAA - 0x00101818, 0xFF9ACD32 - 0x00101808, 0xFFEE82EE - 0x00301030, 0xFF00CED1 - 0x00001830, 0xFF00FF7F - 0x00003010, 0xFF3CB371 - 0x00081810, 0xFF00008B + 0x00000030, 0xFFBDB76B - 0x00181810, 0xFF006400 + 0x00003000, 0xFF800000 + 0x00300000, 0xFF808000 + 0x00303000, 0xFF800080 + 0x00300030, 0xFF008080 + 0x00003030, 0xFFB8860B + 0x00303008, 0xFFB22222 + 0x00101010 };

    public static final short FORMAL_CHARGE_COLIX_RED = (short) elementSymbols.length;

    public static final short FORMAL_CHARGE_COLIX_WHITE = (short) (FORMAL_CHARGE_COLIX_RED + 4);

    public static final short FORMAL_CHARGE_COLIX_BLUE = (short) (FORMAL_CHARGE_COLIX_WHITE + 7);

    public static final int FORMAL_CHARGE_RANGE_SIZE = 12;

    public static final int[] argbsFormalCharge = { 0xFFFF0000, 0xFFFF4040, 0xFFFF8080, 0xFFFFC0C0, 0xFFFFFFFF, 0xFFD8D8FF, 0xFFB4B4FF, 0xFF9090FF, 0xFF6C6CFF, 0xFF4848FF, 0xFF2424FF, 0xFF0000FF };

    public static final int FORMAL_CHARGE_INDEX_WHITE = 4;

    public static final int FORMAL_CHARGE_INDEX_MAX = argbsFormalCharge.length;

    public static final short PARTIAL_CHARGE_COLIX_RED = (short) (FORMAL_CHARGE_COLIX_BLUE + 1);

    public static final short PARTIAL_CHARGE_COLIX_WHITE = (short) (PARTIAL_CHARGE_COLIX_RED + 15);

    public static final short PARTIAL_CHARGE_COLIX_BLUE = (short) (PARTIAL_CHARGE_COLIX_WHITE + 15);

    public static final int PARTIAL_CHARGE_RANGE_SIZE = 31;

    public static final int[] argbsRwbScale = { 0xFFFF0000, 0xFFFF1010, 0xFFFF2020, 0xFFFF3030, 0xFFFF4040, 0xFFFF5050, 0xFFFF6060, 0xFFFF7070, 0xFFFF8080, 0xFFFF9090, 0xFFFFA0A0, 0xFFFFB0B0, 0xFFFFC0C0, 0xFFFFD0D0, 0xFFFFE0E0, 0xFFFFFFFF, 0xFFE0E0FF, 0xFFD0D0FF, 0xFFC0C0FF, 0xFFB0B0FF, 0xFFA0A0FF, 0xFF9090FF, 0xFF8080FF, 0xFF7070FF, 0xFF6060FF, 0xFF5050FF, 0xFF4040FF, 0xFF3030FF, 0xFF2020FF, 0xFF1010FF, 0xFF0000FF };

    public static final int[] argbsRoygbScale = { 0xFFFF0000, 0xFFFF2000, 0xFFFF4000, 0xFFFF6000, 0xFFFF8000, 0xFFFFA000, 0xFFFFC000, 0xFFFFE000, 0xFFFFF000, 0xFFFFFF00, 0xFFF0F000, 0xFFE0FF00, 0xFFC0FF00, 0xFFA0FF00, 0xFF80FF00, 0xFF60FF00, 0xFF40FF00, 0xFF20FF00, 0xFF00FF00, 0xFF00FF20, 0xFF00FF40, 0xFF00FF60, 0xFF00FF80, 0xFF00FFA0, 0xFF00FFC0, 0xFF00FFE0, 0xFF00FFFF, 0xFF00E0FF, 0xFF00C0FF, 0xFF00A0FF, 0xFF0080FF, 0xFF0060FF, 0xFF0040FF, 0xFF0020FF, 0xFF0000FF };

    public static final int[] argbsIsosurfacePositive = { 0xFF5020A0, 0xFF7040C0, 0xFF9060E0, 0xFFB080FF };

    public static final int[] argbsIsosurfaceNegative = { 0xFFA02050, 0xFFC04070, 0xFFE06090, 0xFFFF80B0 };

    public static final String[] specialAtomNames = { null, "N", "CA", "C", null, "O5'", "C5'", "C4'", "C3'", "O3'", "C2'", "C1'", "P", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "OXT", "H", "1H", "2H", "3H", "HA", "1HA", "2HA", "O", "O1", null, null, null, "H5T", "O5T", "O1P", "O2P", "O4'", "O2'", "1H5'", "2H5'", "H4'", "H3'", "1H2'", "2H2'", "2HO'", "H1'", "H3T", null, null, null, null, "N1", "C2", "N3", "C4", "C5", "C6", "O2", "N7", "C8", "N9", "N4", "N2", "N6", "C5M", "O6", "O4", "S4" };

    public static final int ATOMID_MAX = specialAtomNames.length;

    public static final byte ATOMID_AMINO_NITROGEN = 1;

    public static final byte ATOMID_ALPHA_CARBON = 2;

    public static final byte ATOMID_CARBONYL_CARBON = 3;

    public static final byte ATOMID_O5_PRIME = 5;

    public static final byte ATOMID_C5_PRIME = 6;

    public static final byte ATOMID_C3_PRIME = 8;

    public static final byte ATOMID_O3_PRIME = 9;

    public static final byte ATOMID_NUCLEIC_PHOSPHORUS = 12;

    public static final byte ATOMID_TERMINATING_OXT = 32;

    public static final byte ATOMID_CARBONYL_OXYGEN = 40;

    public static final byte ATOMID_O1 = 41;

    public static final byte ATOMID_H5T_TERMINUS = 45;

    public static final byte ATOMID_O5T_TERMINUS = 46;

    public static final byte ATOMID_RNA_O2PRIME = 50;

    public static final byte ATOMID_H3T_TERMINUS = 59;

    public static final byte ATOMID_N1 = 64;

    public static final byte ATOMID_C2 = 65;

    public static final byte ATOMID_N3 = 66;

    public static final byte ATOMID_C4 = 67;

    public static final byte ATOMID_C5 = 68;

    public static final byte ATOMID_C6 = 69;

    public static final byte ATOMID_O2 = 70;

    public static final byte ATOMID_N7 = 71;

    public static final byte ATOMID_C8 = 72;

    public static final byte ATOMID_N9 = 73;

    public static final byte ATOMID_N4 = 74;

    public static final byte ATOMID_N2 = 75;

    public static final byte ATOMID_N6 = 76;

    public static final byte ATOMID_C5M = 77;

    public static final byte ATOMID_O6 = 78;

    public static final byte ATOMID_O4 = 79;

    public static final byte ATOMID_S4 = 80;

    public static final byte ATOMID_NUCLEIC_WING = 69;

    public static final int ATOMID_PROTEIN_MASK = 0x07 << 1;

    public static final int ATOMID_ALPHA_ONLY_MASK = 1 << ATOMID_ALPHA_CARBON;

    public static final int ATOMID_NUCLEIC_MASK = 0x7F << 5;

    public static final int ATOMID_PHOSPHORUS_ONLY_MASK = 1 << ATOMID_NUCLEIC_PHOSPHORUS;

    public static final int ATOMID_DISTINGUISHING_ATOM_MAX = 32;

    public static final int ATOMID_BACKBONE_MAX = 64;

    /****************************************************************
   * PDB file format spec says that the 'residue name' must be
   * right-justified. However, Eric Martz says that some files
   * are not. Therefore, we will be 'flexible' in reading the
   * group name ... we will trim() when read in the field.
   * So a 'group3' can now be less than 3 characters long.
   ****************************************************************/
    public static final int GROUPID_PROLINE = 15;

    public static final int GROUPID_PURINE_MIN = 24;

    public static final int GROUPID_PURINE_LAST = 29;

    public static final int GROUPID_PYRIMIDINE_MIN = 30;

    public static final int GROUPID_PYRIMIDINE_LAST = 35;

    public static final int GROUPID_GUANINE = 26;

    public static final int GROUPID_PLUS_GUANINE = 27;

    public static final int GROUPID_GUANINE_1_MIN = 40;

    public static final int GROUPID_GUANINE_1_LAST = 46;

    public static final int GROUPID_GUANINE_2_MIN = 55;

    public static final int GROUPID_GUANINE_2_LAST = 57;

    public static final short GROUPID_AMINO_MAX = 23;

    public static final short GROUPID_SHAPELY_MAX = 36;

    public static final String[] predefinedGroup3Names = { "", "ALA", "ARG", "ASN", "ASP", "CYS", "GLN", "GLU", "GLY", "HIS", "ILE", "LEU", "LYS", "MET", "PHE", "PRO", "SER", "THR", "TRP", "TYR", "VAL", "ASX", "GLX", "UNK", "A", "+A", "G", "+G", "I", "+I", "C", "+C", "T", "+T", "U", "+U", "1MA", "AMO", "5MC", "OMC", "1MG", "2MG", "M2G", "7MG", "G7M", "OMG", "YG", "QUO", "H2U", "5MU", "4SU", "PSU", "AMP", "ADP", "ATP", "GMP", "GDP", "GTP", "IMP", "IDP", "ITP", "CMP", "CDP", "CTP", "TMP", "TDP", "TTP", "UMP", "UDP", "UTP", "HOH", "DOD", "WAT", "PO4", "SO4" };

    public static final String[] predefinedGroup1Names = { "", "A", "R", "N", "D", "C", "Q", "E", "G", "H", "I", "L", "K", "M", "F", "P", "S", "T", "W", "Y", "V" };

    public static String[] predefinedSets = { "@amino _g>0 & _g<=23", "@acidic asp,glu", "@basic arg,his,lys", "@charged acidic,basic", "@negative acidic", "@positive basic", "@neutral amino&!(acidic,basic)", "@polar amino&!hydrophobic", "@cyclic his,phe,pro,trp,tyr", "@acyclic amino&!cyclic", "@aliphatic ala,gly,ile,leu,val", "@aromatic his,phe,trp,tyr", "@buried ala,cys,ile,leu,met,phe,trp,val", "@surface !buried", "@hydrophobic ala,gly,ile,leu,met,phe,pro,trp,tyr,val", "@ligand hetero & !solvent", "@mainchain backbone", "@small ala,gly,ser", "@medium asn,asp,cys,pro,thr,val", "@large arg,glu,gln,his,ile,leu,lys,met,phe,trp,tyr", "@c nucleic & within(group,_a=74)", "@g nucleic & within(group,_a=75)", "@cg c,g", "@a nucleic & within(group,_a=76)", "@t nucleic & within(group,_a=77)", "@at a,t", "@i nucleic & within(group,_a=78) & !g", "@u nucleic & within(group,_a=79) & !t", "@tu nucleic & within(group,_a=80)", "@solvent _g>=70 & _g<=74", "@hoh water", "@water _g>=70 & _g<=72", "@ions _g=73,_g=74", "@alpha _a=2", "@backbone (protein,nucleic) & _a>0 & _a<=63", "@sidechain (protein,nucleic) & !backbone", "@base nucleic & !backbone", "@turn _structure=1", "@sheet _structure=2", "@helix _structure=3", "@bonded bondcount>0", "@hbonded hbondcount>0" };

    public static final String DEFAULT_FONTFACE = "SansSerif";

    public static final String DEFAULT_FONTSTYLE = "Plain";

    public static final int LABEL_MINIMUM_FONTSIZE = 6;

    public static final int LABEL_MAXIMUM_FONTSIZE = 63;

    public static final int LABEL_DEFAULT_FONTSIZE = 13;

    public static final int LABEL_DEFAULT_X_OFFSET = 4;

    public static final int LABEL_DEFAULT_Y_OFFSET = 4;

    public static final int MEASURE_DEFAULT_FONTSIZE = 15;

    public static final int AXES_DEFAULT_FONTSIZE = 14;

    public static final int SHAPE_BALLS = 0;

    public static final int SHAPE_STICKS = 1;

    public static final int SHAPE_HSTICKS = 2;

    public static final int SHAPE_SSSTICKS = 3;

    public static final int SHAPE_LABELS = 4;

    public static final int SHAPE_VECTORS = 5;

    public static final int SHAPE_MEASURES = 6;

    public static final int SHAPE_DOTS = 7;

    public static final int SHAPE_BACKBONE = 8;

    public static final int SHAPE_TRACE = 9;

    public static final int SHAPE_CARTOON = 10;

    public static final int SHAPE_STRANDS = 11;

    public static final int SHAPE_MESHRIBBON = 12;

    public static final int SHAPE_RIBBONS = 13;

    public static final int SHAPE_ROCKETS = 14;

    public static final int SHAPE_STARS = 15;

    public static final int SHAPE_MIN_SELECTION_INDEPENDENT = 16;

    public static final int SHAPE_AXES = 16;

    public static final int SHAPE_BBCAGE = 17;

    public static final int SHAPE_UCCAGE = 18;

    public static final int SHAPE_FRANK = 19;

    public static final int SHAPE_ECHO = 20;

    public static final int SHAPE_HOVER = 21;

    public static final int SHAPE_PMESH = 22;

    public static final int SHAPE_POLYHEDRA = 23;

    public static final int SHAPE_SASURFACE = 24;

    public static final int SHAPE_ISOSURFACE = 25;

    public static final int SHAPE_PRUEBA = 26;

    public static final int SHAPE_MAX = 27;

    public static final String[] shapeClassBases = { "Balls", "Sticks", "Hsticks", "Sssticks", "Labels", "Vectors", "Measures", "Dots", "Backbone", "Trace", "Cartoon", "Strands", "MeshRibbon", "Ribbons", "Rockets", "Stars", "Axes", "Bbcage", "Uccage", "Frank", "Echo", "Hover", "Pmesh", "Polyhedra", "Sasurface", "Isosurface", "Prueba" };

    public static final int STEREO_NONE = 0;

    public static final int STEREO_DOUBLE = 1;

    public static final int STEREO_REDCYAN = 2;

    public static final int STEREO_REDBLUE = 3;

    public static final int STEREO_REDGREEN = 4;

    static {
        if (ionicLookupTable.length != ionicMars.length) {
            System.out.println("ionic table mismatch!");
            throw new NullPointerException();
        }
        for (int i = ionicLookupTable.length; --i > 0; ) {
            if (ionicLookupTable[i - 1] >= ionicLookupTable[i]) {
                System.out.println("ionicLookupTable not sorted properly");
                throw new NullPointerException();
            }
        }
        if (argbsFormalCharge.length != FORMAL_CHARGE_MAX - FORMAL_CHARGE_MIN + 1) {
            System.out.println("formal charge color table length");
            throw new NullPointerException();
        }
        if (shapeClassBases.length != SHAPE_MAX) {
            System.out.println("graphicBaseClasses wrong length");
            throw new NullPointerException();
        }
        if (argbsAmino.length != GROUPID_AMINO_MAX) {
            System.out.println("argbsAmino wrong length");
            throw new NullPointerException();
        }
        if (argbsShapely.length != GROUPID_SHAPELY_MAX) {
            System.out.println("argbsShapely wrong length");
            throw new NullPointerException();
        }
    }
}
