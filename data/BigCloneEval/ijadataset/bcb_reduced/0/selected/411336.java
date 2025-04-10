package com.fj.engine;

import java.util.Random;
import com.fj.torkel.Game;

public class RPG {

    public static final String DT_SPECIAL = "special";

    public static final String DT_NORMAL = "normal";

    public static final String DT_UNARMED = "unarmed";

    public static final String DT_FIRE = "fire";

    public static final String DT_ICE = "ice";

    public static final String DT_WATER = "water";

    public static final String DT_PIERCING = "piercing";

    public static final String DT_ACID = "acid";

    public static final String DT_SHOCK = "shock";

    public static final String DT_MAGIC = "magic";

    public static final String DT_IMPACT = "impact";

    public static final String DT_CHILL = "chill";

    public static final String DT_POISON = "poison";

    public static final String DT_DRAIN = "drain";

    public static final String DT_DISPELL = "dispell";

    public static final String DT_DISINTEGRATE = "disintegrate";

    public static final int WT_ERROR = -1;

    public static final int WT_NONE = 0;

    public static final int WT_MAINHAND = 1;

    public static final int WT_SECONDHAND = 2;

    public static final int WT_TWOHANDS = 3;

    public static final int WT_RIGHTRING = 4;

    public static final int WT_LEFTRING = 5;

    public static final int WT_NECK = 6;

    public static final int WT_HANDS = 7;

    public static final int WT_BOOTS = 8;

    public static final int WT_TORSO = 9;

    public static final int WT_LEGS = 10;

    public static final int WT_HEAD = 11;

    public static final int WT_CLOAK = 12;

    public static final int WT_FULLBODY = 13;

    public static final int WT_BRACERS = 14;

    public static final int WT_BELT = 15;

    public static final int WT_RANGEDWEAPON = 20;

    public static final int WT_MISSILE = 21;

    public static final int WT_BOOKBAG = 70;

    public static final int WT_FOODSACK = 71;

    public static final int WT_HOLDING = 72;

    public static final int WT_INGREDIENTPOUCH = 73;

    public static final int WT_JEWELRYCASE = 74;

    public static final int WT_KEYRING = 75;

    public static final int WT_POTIONCASE = 76;

    public static final int WT_QUIVER = 77;

    public static final int WT_RUNEBAG = 78;

    public static final int WT_SCROLLCASE = 79;

    public static final int WT_WANDCASE = 80;

    public static final int[] WT_BAGS = { WT_BOOKBAG, WT_FOODSACK, WT_HOLDING, WT_INGREDIENTPOUCH, WT_JEWELRYCASE, WT_KEYRING, WT_POTIONCASE, WT_QUIVER, WT_RUNEBAG, WT_SCROLLCASE, WT_WANDCASE };

    public static final int WT_EFFECT = 100;

    public static final int ST_STATE = 0;

    public static final String ST_SK = "SK";

    public static final String ST_ST = "ST";

    public static final String ST_AG = "AG";

    public static final String ST_TG = "TG";

    public static final String ST_IN = "IN";

    public static final String ST_WP = "WP";

    public static final String ST_CH = "CH";

    public static final String ST_CR = "CR";

    public static final String ST_LEVEL = "Level";

    public static final String ST_EXP = "Experience";

    public static final String ST_PEITY = "Peity";

    public static final String ST_FAME = "Fame";

    public static final String ST_HUNGER = "Hunger";

    public static final String ST_HUNGERTHRESHOLD = "HungerThreshold";

    public static final String ST_SKILLPOINTS = "SkillPoints";

    public static final String ST_SKILLPOINTSSPENT = "SkillPointsSpent";

    public static final int ST_RANGER = 21;

    public static final int ST_FIGHTER = 22;

    public static final int ST_THIEF = 23;

    public static final int ST_PRIEST = 24;

    public static final int ST_SCHOLAR = 25;

    public static final int ST_MAGE = 26;

    public static final int ST_BARD = 27;

    public static final int ST_ARTISAN = 28;

    /**
	 * Encumberance (Hero only) 0= Unencumbered 100= Completely overloaded
	 */
    public static final String ST_ENCUMBERANCE = "Encumberance";

    public static final String ST_ALIGNMENT = "Alignment";

    public static final String ST_ORDER = "Order";

    public static final String ST_SPEED = "Speed";

    public static final String ST_MOVESPEED = "MoveSpeed";

    public static final String ST_ATTACKSPEED = "AttackSpeed";

    public static final String ST_MAGICSPEED = "CastSpeed";

    public static final String ST_MOVECOST = "MoveCost";

    public static final String ST_ATTACKCOST = "AttackCost";

    public static final int ST_ETHEREAL = 3001;

    public static final String ST_UNDEAD = "IsUndead";

    public static final String ST_FEARFACTOR = "FearFactor";

    public static final String ST_APS = "APS";

    public static final String ST_HPS = "HPS";

    public static final String ST_HPSMAX = "HPSMAX";

    public static final String ST_MPS = "MPS";

    public static final String ST_MPSMAX = "MPSMAX";

    public static final String ST_ASK = "ASK";

    public static final String ST_ASKMULTIPLIER = "ASKMul";

    public static final String ST_ASKBONUS = "ASKBonus";

    public static final String ST_AST = "AST";

    public static final String ST_ASTMULTIPLIER = "ASTMul";

    public static final String ST_ASTBONUS = "ASTBonus";

    public static final String ST_DSK = "DSK";

    public static final String ST_DSKMULTIPLIER = "DSKMul";

    public static final String ST_DSKBONUS = "DSKBonus";

    public static final int ST_DODGEMULTIPLIER = 10040;

    public static final int ST_DSKPARRY = 10041;

    public static final String ST_REGENERATE = "RegenerationRate";

    public static final String ST_RECHARGE = "RechargeRate";

    public static final String ST_RSKBONUS = "RSKBonus";

    public static final String ST_RSKMULTIPLIER = "RSKMul";

    public static final String ST_RSTBONUS = "RSTBonus";

    public static final String ST_RSTMULTIPLIER = "RSTMul";

    public static final String ST_RANGE = "Range";

    public static final String ST_ANTIMAGIC = "AntiMagic";

    public static final String ST_SCORE = "Score";

    public static final String ST_SCORE_BESTKILL = "BestKillLevel";

    public static final String ST_SCORE_BESTLEVEL = "BestLevel";

    public static final String ST_SCORE_KILLS = "KillCount";

    public static final String ST_FREQUENCY = "Frequency";

    public static final String ST_ITEMVALUE = "Value";

    public static final String ST_MISSILETYPE = "MissileType";

    public static final String ST_AIMODE = "AIMode";

    public static final String ST_TARGETX = "TargetX";

    public static final String ST_TARGETY = "TargetY";

    public static final String ST_CASTCHANCE = "CastChance";

    public static final String ST_SIDE = "Side";

    public static final int ST_QUESTNUMBER = 31000;

    public static final int ST_QUESTSTATE = 31001;

    public static final int ST_IMMOBILIZED = 32001;

    public static final String ST_CONFUSED = "IsConfused";

    public static final String ST_BLIND = "IsBlind";

    public static final String ST_PANICKED = "IsPanicked";

    public static final int ST_NON_DISPLACEABLE = 32005;

    public static final String ST_TRUEVIEW = "TrueView";

    public static final int MF_EDIBLE = 2 << 15;

    public static final int MF_MAGICAL = 2 << 16;

    public static final int MF_RESISTANT = 2 << 17;

    public static final int MF_LIVING = 2 << 18;

    public static final int MF_DIVINE = 2 << 19;

    public static final int MF_SKIN = 2 << 20;

    public static final int MF_STONE = 2 << 21;

    public static final int MF_GLASS = 2 << 22;

    public static final int MF_GEMSTONE = 2 << 23;

    public static final int MF_METAL = 2 << 24;

    public static final int MF_PLANT = 2 << 25;

    public static final int MF_CLOTH = 2 << 26;

    public static final int MF_INDESTRUCTIBLE = 2 << 27;

    public static final int MF_LIQUID = 2 << 28;

    public static final int MF_GASEOUS = 2 << 29;

    public static final int MF_ETHEREAL = 2 << 30;

    public static final int MF_PLASTIC = 2 << 31;

    public static final int MISSILE_THROWN = 0;

    public static final int MISSILE_ARROW = 1;

    public static final int MISSILE_BOLT = 2;

    public static final int MISSILE_STONE = 3;

    public static final IDescription DESC_GENERIC = new Describer("thing", "A generic thing. Don't ask.");

    public static final int[] POWER = { 2, 2, 3, 3, 3, 3, 4, 4, 5, 5, 6, 7, 8, 9, 10, 11, 13, 14, 16, 18, 20, 22, 25, 28, 32, 36, 40, 45, 51, 57, 64, 72, 81, 91, 102, 114, 128, 144, 161, 181, 203, 228, 256, 287, 323, 362, 406, 456, 512, 575, 645, 724, 813, 912, 1024, 1149, 1290, 1448, 1625, 1825, 2048, 2299, 2580, 2896, 3251, 3649, 4096 };

    public static float random() {
        return rand.nextFloat();
    }

    /**
	 * Returns a luck-weighted random number
	 * 
	 * Lower values if the attacker has better luck
	 * 
	 * @param a
	 *            The attacker
	 * @param b
	 *            The defender
	 * @return
	 */
    public static float luckRandom(Thing a, Thing b) {
        int l = 0;
        if (a != null) l += a.getStat("Luck");
        if (b != null) l -= b.getStat("Luck");
        return luckRandom(l);
    }

    public static float luckRandom(int l) {
        float f = rand.nextFloat();
        if (l > 0) {
            for (int i = RPG.r(100); i < l; i += 100) {
                float nf = random();
                if (nf < f) f = nf;
            }
        }
        if (l < 0) {
            l = -l;
            for (int i = RPG.r(100); i < l; i += 100) {
                float nf = random();
                if (nf > f) f = nf;
            }
        }
        return f;
    }

    /**
	 * Performs a standard skill test
	 * 
	 * @param a
	 *            Skill of attacker
	 * @param b
	 *            Skill of defender or task difficulty
	 * @return true if attempt successful
	 */
    public static boolean test(int a, int b) {
        if (a <= 0) return false;
        if (b <= 0) return true;
        return rand.nextInt(a + b) < a;
    }

    public static boolean test(int a, int b, Thing attacker, Thing defender) {
        int l = 0;
        if (attacker != null) l += attacker.getStat("Luck");
        if (defender != null) l -= defender.getStat("Luck");
        return test(a, b, l);
    }

    public static boolean test(int a, int b, int l) {
        if (a <= 0) return false;
        if (b <= 0) return true;
        return (luckRandom(l) * (a + b)) < a;
    }

    public static int hitFactor(int a, int b, Thing ta, Thing tb) {
        int factor = 0;
        while (test(a, RPG.max(1, b), ta, tb)) {
            factor += 1;
            a /= 2;
        }
        return factor;
    }

    public static int power(int a) {
        if ((a < 0) || (a > 66)) return (int) Math.pow(2, 1 + ((double) a) / 6);
        return POWER[a];
    }

    public static int level(int a) {
        if (a < 1) return -1000000;
        if (a == 1) return -6;
        if (a > 66) return (int) Math.round(Math.log(a) * 8.656170245 - 6);
        int i = 0;
        while (POWER[i] < a) i++;
        return i;
    }

    private static Random rand = new Random();

    public static final int Z_ELSEWHERE = -10;

    public static final String[] stats = { "SK", "ST", "AG", "TG", "IN", "WP", "CH", "CR" };

    public static void setRandSeed(long n) {
        rand.setSeed(n);
    }

    public static final int d(int sides) {
        if (sides <= 0) return 0;
        return rand.nextInt(sides) + 1;
    }

    public static final int distSquared(int x1, int y1, int x2, int y2) {
        return ((x1 - x2) * (x1 - x2)) + ((y1 - y2) * (y1 - y2));
    }

    public static final double dist(int x1, int y1, int x2, int y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2)) + ((y1 - y2) * (y1 - y2));
    }

    public static final int radius(int r2) {
        int r = 0;
        while ((r * r) <= r2) r++;
        return r - 1;
    }

    public static final int pick(int[] a) {
        return a[RPG.r(a.length)];
    }

    public static final String pick(String[] a) {
        return a[RPG.r(a.length)];
    }

    public static final char pick(String a) {
        return a.charAt(RPG.r(a.length()));
    }

    public static final int min(int a, int b) {
        return (a < b) ? a : b;
    }

    public static final int max(int a, int b) {
        return (a > b) ? a : b;
    }

    public static final int middle(int a, int b, int c) {
        if (a > b) {
            if (b > c) return b;
            if (a > c) return c;
            return a;
        }
        if (a > c) return a;
        if (b > c) return c;
        return b;
    }

    public static final int niceNumber(int x) {
        int p = 1;
        while (x >= 100) {
            x /= 10;
            p *= 10;
        }
        if (x > 30) x = 5 * (x / 5);
        return x * p;
    }

    public static int e(int n) {
        if (n <= 0) return 0;
        int result = 0;
        while ((rand.nextInt() % (n + 1)) != 0) result++;
        return result;
    }

    public static int ln(double x) {
        return ln(x, 1.0);
    }

    public static int ln(double x, double s) {
        double n = rand.nextGaussian();
        n = n * s;
        return (int) Math.round(x * Math.exp(n));
    }

    public static int po(double x) {
        int r = 0;
        double a = rand.nextDouble();
        if (a >= 0.99999999) return 0;
        double p = Math.exp(-x);
        while (a >= p) {
            r++;
            a = a - p;
            p = p * x / r;
        }
        return r;
    }

    public static int po(int numerator, int denominator) {
        return po(((double) numerator) / denominator);
    }

    public static boolean sometimes() {
        return (rand.nextFloat() < 0.1);
    }

    public static boolean often() {
        return (rand.nextFloat() < 0.4);
    }

    public static boolean rarely() {
        return (rand.nextFloat() < 0.01);
    }

    public static boolean usually() {
        return (rand.nextFloat() < 0.8);
    }

    public static int rspread(int a, int b) {
        if (a > b) {
            int t = a;
            a = b;
            b = t;
        }
        return rand.nextInt(b - a + 1) + a;
    }

    public static final int sign(double a) {
        return (a < 0) ? -1 : ((a > 0) ? 1 : 0);
    }

    public static final int sign(int a) {
        return (a < 0) ? -1 : ((a > 0) ? 1 : 0);
    }

    public static final int abs(int a) {
        return (a >= 0) ? a : -a;
    }

    /**
	 * Random number from zero to s-1
	 * 
	 * @param s
	 *            Upper bound (excluded)
	 * @return
	 */
    public static final int r(int s) {
        if (s <= 0) return 0;
        return rand.nextInt(s);
    }

    /**
	 * Returns random number uniformly distributed in [n1, n2] range. It is
	 * allowed to have to n1 > n2, or n1 < n2, or n1 == n2.
	 */
    public static final int r(int n1, int n2) {
        int min, max;
        min = Math.min(n1, n2);
        max = Math.max(n1, n2);
        final int diff = max - min;
        Game.assertTrue(diff >= 0);
        final int ret = min + rand.nextInt(diff + 1);
        Game.assertTrue(ret <= max);
        return ret;
    }

    /**
	 * The method evaluates if some event happened with specified probability.
	 * You pass probability of event as argument. The method calls
	 * Random.nextDouble() and returns true when event happened; otherwise
	 * returns false.
	 * 
	 * @param prob
	 *            Probability (in [0,1] range) of event.
	 */
    public static boolean p(final double prob) {
        Game.assertTrue(prob >= 0.);
        Game.assertTrue(prob <= 1.);
        if (prob == 0.) return false;
        return rand.nextDouble() <= prob;
    }

    public static final int round(double n) {
        int i = (int) n;
        if (rand.nextDouble() < (n - i)) i++;
        return i;
    }

    public static final int a(int s) {
        return r(s + 1) + r(s + 1);
    }

    public static final int d3() {
        return d(3);
    }

    public static final int d4() {
        return d(4);
    }

    public static final int d6() {
        return d(6);
    }

    public static final int d8() {
        return d(8);
    }

    public static final int d10() {
        return d(10);
    }

    public static final int d12() {
        return d(12);
    }

    public static final int d20() {
        return d(20);
    }

    public static final int d100() {
        return d(100);
    }

    public static int best(int r, int n, int s) {
        if ((n <= 0) || (r < 0) || (r > n) || (s < 0)) return 0;
        int[] rolls = new int[n];
        for (int i = 0; i < n; i++) rolls[i] = d(s);
        boolean found;
        do {
            found = false;
            for (int x = 0; x < n - 1; x++) {
                if (rolls[x] < rolls[x + 1]) {
                    int t = rolls[x];
                    rolls[x] = rolls[x + 1];
                    rolls[x + 1] = t;
                    found = true;
                }
            }
        } while (found);
        int sum = 0;
        for (int i = 0; i < r; i++) sum += rolls[i];
        return sum;
    }

    public static int d(int number, int sides) {
        int total = 0;
        for (int i = 0; i < number; i++) {
            total += d(sides);
        }
        return total;
    }

    public static final int index(int a, int[] aa) {
        for (int i = 0; i < aa.length; i++) {
            if (aa[i] == a) return i;
        }
        return -1;
    }

    public static final int index(String s, String[] ss) {
        for (int i = 0; i < ss.length; i++) {
            if (ss[i].equals(s)) return i;
        }
        return -1;
    }

    public static int percentile(int var, int base) {
        if (base == 0) {
            return 0;
        }
        int p = var * 100 / base;
        if ((var > 0) && (p == 0)) {
            p = 1;
        }
        return p;
    }

    public static Object[] subList(Object[] list, Class<?> c) {
        Object[] temp = new Object[list.length];
        int tempcount = 0;
        for (int i = 0; i < list.length; i++) {
            Object t = list[i];
            if (c.isInstance(t)) {
                temp[tempcount] = t;
                tempcount++;
            }
        }
        Object[] result = new Object[tempcount];
        if (tempcount > 0) System.arraycopy(temp, 0, result, 0, tempcount);
        return result;
    }
}
