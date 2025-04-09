package struct;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class Projet implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int PROJET_VERSION = 80;

    private String Name;

    private short style_projet;

    private boolean MenuFreeze;

    private short TileWidth, TileHeight;

    private ArrayList<Monstre> monstres;

    private ArrayList<Carte> cartes;

    private ArrayList<Magie> magies;

    private ArrayList<Objet> objets;

    private ArrayList<ClasseJoueur> classesjoueur;

    private ArrayList<ClasseMonstre> classesmonstre;

    private ArrayList<Block> Blocage;

    private ArrayList<String> StatsBase;

    private ArrayList<Integer> CourbeXP;

    private Depart depart, mort;

    @SuppressWarnings("unchecked")
    private static Object resizeArray(Object oldArray, int newSize) {
        int oldSize = java.lang.reflect.Array.getLength(oldArray);
        Class elementType = oldArray.getClass().getComponentType();
        Object newArray = java.lang.reflect.Array.newInstance(elementType, newSize);
        int preserveLength = Math.min(oldSize, newSize);
        if (preserveLength > 0) System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
        return newArray;
    }

    public class Carte implements Serializable {

        private static final long serialVersionUID = 1L;

        public Evenements[][] evenements;

        public Case[][] cases;

        public ArrayList<Zone> zones;

        public String Name;

        public byte TypeCarte, Effect;

        public String Static, Music, Chipset;

        public boolean DecToResPoint;

        public short TailleX, TailleY;

        public String Parent;

        public Carte() {
            zones = new ArrayList<Zone>();
        }

        public void resizeEvenement(int W, int H) {
            Evenements[][] event = this.evenements;
            event = (Evenements[][]) resizeArray(event, W);
            for (int i = 0; i < W; i++) {
                if (event[i] != null) event[i] = (Evenements[]) resizeArray(event[i], H); else event[i] = new Evenements[H];
            }
            this.evenements = event;
        }

        public void resizeCase(int W, int H) {
            Case[][] tab = this.cases;
            int oldw = this.cases.length;
            int oldh = this.cases[0].length;
            tab = (Case[][]) resizeArray(tab, W);
            for (int i = 0; i < W; i++) {
                if (tab[i] != null) tab[i] = (Case[]) resizeArray(tab[i], H); else tab[i] = new Case[H];
            }
            for (int i = oldw; i < W; i++) for (int j = 0; j < oldh; j++) tab[i][j] = new Case();
            for (int i = 0; i < W; i++) for (int j = oldh; j < H; j++) tab[i][j] = new Case();
            this.cases = tab;
        }
    }

    public class Zone implements Serializable {

        private static final long serialVersionUID = 1L;

        public int X1, X2;

        public int Y1, Y2, MonstreMax;

        public int ZoneTypeMonstre;

        public int VitesseSpawn;

        public String Variable, Resultat;

        public Zone() {
            X1 = 0;
            X2 = 0;
            Y1 = 0;
            Y2 = 0;
            MonstreMax = 0;
            ZoneTypeMonstre = 0;
            VitesseSpawn = 0;
        }

        public Zone clone() {
            Zone z = new Zone();
            z.X1 = X1;
            z.X2 = X2;
            z.Y1 = Y1;
            z.Y2 = Y2;
            z.MonstreMax = MonstreMax;
            z.ZoneTypeMonstre = ZoneTypeMonstre;
            z.VitesseSpawn = VitesseSpawn;
            return z;
        }
    }

    public class Case implements Serializable {

        private static final long serialVersionUID = 1L;

        public int X1, Y1, X2, Y2;

        public Case() {
            X1 = 0;
            Y1 = 0;
            X2 = 0;
            Y2 = 0;
        }
    }

    public class Evenements implements Serializable {

        private static final long serialVersionUID = 1L;

        public ArrayList<Evenement> evenement;

        public ArrayList<ArrayList<String>> CondDecl, CommandeEv;

        public Evenements() {
            evenement = new ArrayList<Evenement>();
            CondDecl = new ArrayList<ArrayList<String>>();
            CommandeEv = new ArrayList<ArrayList<String>>();
        }

        public Evenements clone() {
            int i, j;
            String s;
            Evenements ev = new Evenements();
            ev.evenement = new ArrayList<Evenement>();
            ev.CondDecl = new ArrayList<ArrayList<String>>();
            ev.CommandeEv = new ArrayList<ArrayList<String>>();
            for (i = 0; i < evenement.size(); i++) {
                ev.evenement.add(evenement.get(i).clone());
                ev.CondDecl.add(new ArrayList<String>());
                for (j = 0; j < CondDecl.get(i).size(); j++) {
                    s = CondDecl.get(i).get(j);
                    ev.CondDecl.get(i).add(s);
                }
                ev.CommandeEv.add(new ArrayList<String>());
                for (j = 0; j < CommandeEv.get(i).size(); j++) {
                    s = CommandeEv.get(i).get(j);
                    ev.CommandeEv.get(i).add(s);
                }
            }
            return ev;
        }
    }

    public class Evenement implements Serializable {

        private static final long serialVersionUID = 1L;

        public String Name;

        public String Chipset;

        public int X, Y;

        public boolean Visible, Bloquant, Transparent, EvSuisSprite;

        public short TypeAnim, Direction, Vitesse;

        public short W, H, Z, NumAnim;

        public Evenement() {
            Visible = true;
            X = 0;
            Y = 0;
            W = 24;
            H = 32;
            Z = 0;
            Direction = 2;
            Vitesse = 1;
            NumAnim = 1;
            Name = "";
            Chipset = "";
        }

        public Evenement clone() {
            Evenement ev = new Evenement();
            ev.Name = this.Name;
            ev.Chipset = this.Chipset;
            ev.X = this.X;
            ev.Y = this.Y;
            ev.Visible = this.Visible;
            ev.Bloquant = this.Bloquant;
            ev.Transparent = this.Transparent;
            ev.EvSuisSprite = this.EvSuisSprite;
            ev.TypeAnim = this.TypeAnim;
            ev.Direction = this.Direction;
            ev.Vitesse = this.Vitesse;
            ev.W = this.W;
            ev.H = this.H;
            ev.Z = this.Z;
            ev.NumAnim = this.NumAnim;
            return ev;
        }
    }

    public class Monstre implements Serializable {

        private static final long serialVersionUID = 1L;

        public String Name;

        public short TypeMonstre, Level, ClasseMonstre;

        public String Chipset, SoundAttaque, SoundWound, SoundConcentration;

        public short Vitesse, W, H;

        public short Attaque, Esquive, Dommage, Defense;

        public int Lvl, Vie;

        public int XPMin, XPMax, GoldMin, GoldMax;

        public String VarSpecial;

        public int ResSpecial;

        public ArrayList<Short> Spell, LuckSpell;

        public ArrayList<Short> ObjectWin, PercentWin;

        public boolean Bloquant;

        public Monstre(String NomMonstre) {
            Name = NomMonstre;
            W = 24;
            H = 32;
            Vitesse = 1;
            Attaque = 0;
            Defense = 0;
            Dommage = 0;
            Vie = 0;
            XPMin = 0;
            XPMax = 0;
            GoldMin = 0;
            GoldMax = 0;
            Spell = new ArrayList<Short>();
            LuckSpell = new ArrayList<Short>();
            ObjectWin = new ArrayList<Short>();
            PercentWin = new ArrayList<Short>();
        }
    }

    public class Objet implements Serializable {

        private static final long serialVersionUID = 1L;

        public String Name, Explication, Chipset;

        public int X, Y;

        public short W, H;

        public short Classe, ObjType, MagieAssoc;

        public int Prix;

        public ArrayList<Integer> Stats, StatsMin;

        public short LvlMin;

        public short Attaque, Defense;

        public int PV, PM;

        public Objet(String NomObjet) {
            Name = NomObjet;
            Stats = new ArrayList<Integer>();
            StatsMin = new ArrayList<Integer>();
            X = 0;
            Y = 0;
            W = 16;
            H = 16;
        }
    }

    public class Magie implements Serializable {

        private static final long serialVersionUID = 1L;

        public String Name, Explication;

        public String Chipset;

        public String SoundMagie, FormuleZone, FormuleDuree, FormuleTouche, FormuleEffet;

        public int X, Y, W, H, Tran;

        public short Z, DureeAnim;

        public short Classe, MagieType, MPNeeded, LvlMin, OnMonster, TempsIncantation;

        public Magie(String NomMagie) {
            Name = NomMagie;
            X = 0;
            Y = 0;
            W = 24;
            H = 32;
            Tran = 255;
            Z = 0;
            DureeAnim = 42;
            Classe = 0;
            MagieType = 0;
            MPNeeded = 0;
            LvlMin = 0;
            OnMonster = 0;
            TempsIncantation = 60;
        }
    }

    public class Depart implements Serializable {

        private static final long serialVersionUID = 1L;

        public int X, Y;

        public String Carte;

        public Depart() {
            Carte = "";
        }
    }

    public class ClasseJoueur implements Serializable {

        private static final long serialVersionUID = 1L;

        public String Name;

        public String SoundAttaque, SoundWound, SoundConcentration;

        public int LvlMax, LvlupPoint;

        public ArrayList<Integer> StatsMax, StatsMin;

        public String FormuleAttaque, FormuleEsquive, FormuleDegat, FormuleDefense, FormuleXP, FormuleGold;

        public String FormuleVieMax, FormuleMagMax;

        public ClasseJoueur(String Nom) {
            Name = Nom;
            StatsMin = new ArrayList<Integer>();
            StatsMax = new ArrayList<Integer>();
        }
    }

    public class ClasseMonstre implements Serializable {

        private static final long serialVersionUID = 1L;

        public String Name;

        public String FormuleAttaque, FormuleEsquive, FormuleDegat, FormuleDefense;

        public ClasseMonstre(String Nom) {
            Name = Nom;
        }
    }

    public class Block implements Serializable {

        private static final long serialVersionUID = 1L;

        public String Chipset;

        public boolean[][][] blocage;

        public Block(String Chip, int W, int H) {
            Chipset = Chip;
            blocage = new boolean[2][W][H];
        }
    }

    public Projet() {
        monstres = new ArrayList<Monstre>();
        cartes = new ArrayList<Carte>();
        magies = new ArrayList<Magie>();
        objets = new ArrayList<Objet>();
        classesjoueur = new ArrayList<ClasseJoueur>();
        classesmonstre = new ArrayList<ClasseMonstre>();
        Blocage = new ArrayList<Block>();
        CourbeXP = new ArrayList<Integer>();
        StatsBase = new ArrayList<String>();
        depart = new Depart();
        mort = new Depart();
    }

    public String getName() {
        return Name;
    }

    public void setName(String nom) {
        Name = nom;
    }

    public ArrayList<ClasseJoueur> getClassesJoueur() {
        return classesjoueur;
    }

    public ArrayList<ClasseMonstre> getClassesMonstre() {
        return classesmonstre;
    }

    public ArrayList<Carte> getCartes() {
        return cartes;
    }

    public ArrayList<Integer> getCourbeXP() {
        return CourbeXP;
    }

    public void setCartes(ArrayList<Carte> c) {
        cartes = c;
    }

    public Carte getCarteByName(String Name) {
        boolean ok = false;
        Iterator<Carte> itr = cartes.iterator();
        while ((itr.hasNext()) && (ok == false)) {
            Carte c = itr.next();
            if (c.Name.compareTo(Name) == 0) return c;
        }
        return null;
    }

    public int getIndexCarteByName(String Name) {
        boolean ok = false;
        int i = 0;
        Iterator<Carte> itr = cartes.iterator();
        while ((itr.hasNext()) && (ok == false)) {
            Carte c = itr.next();
            if (c.Name.compareTo(Name) == 0) return i;
            i++;
        }
        return -1;
    }

    public ArrayList<String> getStatsBase() {
        return StatsBase;
    }

    public ArrayList<Monstre> getMonstres() {
        return monstres;
    }

    public Monstre getMonstreByIndex(int index) {
        return monstres.get(index);
    }

    public ArrayList<Objet> getObjets() {
        return objets;
    }

    public Objet getObjetByIndex(int index) {
        return objets.get(index);
    }

    public int getIndexObjetByName(String Name) {
        boolean ok = false;
        int i = 0;
        Iterator<Objet> itr = objets.iterator();
        while ((itr.hasNext()) && (ok == false)) {
            Objet o = itr.next();
            if (o.Name.compareTo(Name) == 0) return i;
            i++;
        }
        return -1;
    }

    public ArrayList<Magie> getMagies() {
        return magies;
    }

    public Magie getMagieByIndex(int index) {
        return magies.get(index);
    }

    public int getIndexMagieByName(String Name) {
        boolean ok = false;
        int i = 0;
        Iterator<Magie> itr = magies.iterator();
        while ((itr.hasNext()) && (ok == false)) {
            Magie m = itr.next();
            if (m.Name.compareTo(Name) == 0) return i;
            i++;
        }
        return -1;
    }

    public ArrayList<Block> getBlocages() {
        return Blocage;
    }

    public Block getBlocageByName(String Name) {
        boolean ok = false;
        Iterator<Block> itr = Blocage.iterator();
        while ((itr.hasNext()) && (ok == false)) {
            Block c = itr.next();
            if (c.Chipset.compareTo(Name) == 0) return c;
        }
        return null;
    }

    public Block resizeBlocage(Block b, int W, int H) {
        boolean[][][] tab = b.blocage;
        for (int j = 0; j < 2; j++) {
            tab = (boolean[][][]) resizeArray(tab[j], W);
            for (int i = 0; i < W; i++) {
                if (tab[j][i] != null) tab[j][i] = (boolean[]) resizeArray(tab[j][i], H); else tab[j][i] = new boolean[H];
            }
        }
        b.blocage = tab;
        return b;
    }

    public Depart getDepart() {
        return depart;
    }

    public void setDepart(Depart dep) {
        depart = dep;
    }

    public Depart getMort() {
        return mort;
    }

    public void setMort(Depart mrt) {
        mort = mrt;
    }

    public short getStyleProjet() {
        return style_projet;
    }

    public void setStyleProjet(short sp) {
        style_projet = sp;
    }

    public short getTileWidth() {
        return TileWidth;
    }

    public void setTileWidth(short tw) {
        TileWidth = tw;
    }

    public short getTileHeight() {
        return TileHeight;
    }

    public void setTileHeight(short th) {
        TileHeight = th;
    }

    public void setMenuFreeze(boolean mf) {
        MenuFreeze = mf;
    }

    public boolean isMenuFreeze() {
        return MenuFreeze;
    }
}
