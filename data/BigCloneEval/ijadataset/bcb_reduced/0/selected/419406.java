package com.cirnoworks.rpg;

import com.cirnoworks.rpg.ui.ActionSkill;
import com.cirnoworks.rpg.ui.SkillResult;

/**
 *
 * @author huangyx
 */
public class Chara {

    /**
     *
     */
    public static final int pos_head = 0;

    /**
     *
     */
    public static final int pos_weapon = 1;

    /**
     *
     */
    public static final int pos_second = 2;

    /**
     *
     */
    public static final int pos_armor = 3;

    /**
     * ��ֹ
     */
    public static final int control_still = 0;

    /**
     * �ֶ�����
     */
    public static final int control_manual = 0x0001;

    /**
     * ai����
     */
    public static final int control_auto0 = 0x1000;

    /**
     *
     */
    public String name;

    /**
     *
     */
    public int controlType = 0;

    /**
     *
     */
    public int spid;

    /**
     * 
     */
    public int faceid;

    /**
     *
     */
    public boolean isBlock = true;

    public static final int animMove = 0;

    public static final int animPlay = 1;

    public static final int animRoll = 2;

    /**
     * ����ģʽ
     * 0 - ����ƶ�
     * 1 - �������
     * 2 - ��ת
     */
    public int animMode = 0;

    /**
     *
     */
    public int posx;

    /**
     *
     */
    public int posy;

    /**
     * 
     */
    public int posh = 0;

    /**
     *
     */
    protected int framesFrom;

    /**
     *
     */
    protected int framesTo;

    /**
     *
     */
    protected int currentFrame;

    /**
     *
     */
    private int face = Team.faceDown;

    /**
     *
     */
    public int classId;

    /**
     * 0�Ѿ� 1�о� 2NPC
     */
    public int ffi;

    /**
     *
     */
    public int hp;

    /**
     *
     */
    public int mp;

    /**
     *
     */
    public int hpmax;

    /**
     *
     */
    public int mpmax;

    /**
     *
     */
    public int str;

    /**
     *
     */
    public int agi;

    /**
     *
     */
    public int dex;

    /**
     *
     */
    public int ini;

    /**
     *
     */
    public int luk;

    /**
     *
     */
    public int def;

    /**
     *
     */
    public int mdef;

    /**
     *
     */
    public int strb;

    /**
     *
     */
    public int agib;

    /**
     *
     */
    public int dexb;

    /**
     *
     */
    public int inib;

    /**
     *
     */
    public int lukb;

    /**
     *
     */
    public int defb;

    /**
     *
     */
    public int mdefb;

    /**
     *
     */
    public int patk;

    /**
     *
     */
    public int matk;

    /**
     *
     */
    public int phit;

    /**
     *
     */
    public int mhit;

    /**
     *
     */
    public int flee;

    /**
     *
     */
    public int cri;

    /**
     *
     */
    public int fcri;

    /**
     *
     */
    public int patkb;

    /**
     *
     */
    public int matkb;

    /**
     *
     */
    public int phitb;

    /**
     *
     */
    public int mhitb;

    /**
     *
     */
    public int fleeb;

    /**
     *
     */
    public int crib;

    /**
     *
     */
    public int fcrib;

    /**
     *
     */
    public int spd = 5;

    /**
     *
     */
    public int spdb;

    /**
     *
     */
    public int move = 100;

    /**
     *
     */
    public int moveb;

    /**
     *
     */
    public int lvl;

    /**
     *
     */
    public int exp;

    /**
     *
     */
    public int nextExp;

    /**
     *
     */
    public int[] eq = new int[10];

    /**
     *
     */
    public boolean isDead = false;

    /**
     *
     */
    public boolean inTeam = false;

    /**
     *
     */
    public int[] eleRes = new int[16];

    /**
     *
     */
    public int atb;

    /**
     *
     */
    public boolean automatic = true;

    /**
     *
     */
    public int[] skills = new int[256];

    /**
     *
     */
    public char[] skillsCD = new char[256];

    int frofs;

    boolean canMove;

    boolean canAct;

    /**
     *
     */
    protected int upHp;

    /**
     *
     */
    protected int upMp;

    /**
     *
     */
    protected int upStr;

    /**
     *
     */
    protected int upAgi;

    /**
     *
     */
    protected int upDex;

    /**
     *
     */
    protected int upIni;

    /**
     *
     */
    protected int upVit;

    /**
     *
     */
    protected int upLuk;

    /**
     *
     */
    protected int upDef;

    /**
     *
     */
    protected int upMdef;

    /**
     *
     * @param hp
     * @param mp
     * @param str
     * @param agi
     * @param dex
     * @param ini
     * @param luk
     * @param def
     * @param mdef
     */
    public void setUp(int hp, int mp, int str, int agi, int dex, int ini, int luk, int def, int mdef) {
        upHp = hp;
        upMp = mp;
        upStr = str;
        upAgi = agi;
        upDex = dex;
        upIni = ini;
        upLuk = luk;
        upDef = def;
        upMdef = mdef;
    }

    /**
     *
     */
    public void levelUp() {
        nextExp = World.instance.expTable[lvl];
        while (exp >= nextExp) {
            if (lvl >= 99) break;
            int hpUp = World.instance.rand(10000);
            int mpUp = World.instance.rand(10000);
            int strUp = World.instance.rand(10000);
            int agiUp = World.instance.rand(10000);
            int dexUp = World.instance.rand(10000);
            int iniUp = World.instance.rand(10000);
            int lukUp = World.instance.rand(10000);
            int defUp = World.instance.rand(10000);
            int mdefUp = World.instance.rand(10000);
            if (hpUp < upHp) hpmax++;
            if (mpUp < upMp) mpmax++;
            if (strUp < upStr) str++;
            if (agiUp < upAgi) agi++;
            if (dexUp < upDex) dex++;
            if (iniUp < upIni) ini++;
            if (lukUp < upLuk) luk++;
            if (defUp < upDef) def++;
            if (mdefUp < upMdef) mdef++;
            if (hpUp * 10000 < upHp * upHp) hpmax++;
            if (mpUp * 10000 < upMp * upMp) mpmax++;
            if (strUp * 10000 < upStr * upStr) str++;
            if (agiUp * 10000 < upAgi * upAgi) agi++;
            if (dexUp * 10000 < upDex * upDex) dex++;
            if (iniUp * 10000 < upIni * upIni) ini++;
            if (lukUp * 10000 < upLuk * upLuk) luk++;
            if (defUp * 10000 < upDef * upDef) def++;
            if (mdefUp < upMdef) mdef++;
            lvl++;
            nextExp = World.instance.expTable[lvl];
            calc();
            fullHeal();
        }
    }

    /**
     *
     * @param hp
     * @param mp
     * @param str
     * @param agi
     * @param dex
     * @param ini
     * @param luk
     * @param def
     * @param mdef
     * @param classId
     */
    public void setupBattle(int hp, int mp, int str, int agi, int dex, int ini, int luk, int def, int mdef, int classId) {
        this.hpmax = hp;
        this.mpmax = mp;
        this.str = str;
        this.agi = agi;
        this.dex = dex;
        this.ini = ini;
        this.luk = luk;
        this.def = def;
        this.mdef = mdef;
        this.classId = classId;
        calc();
    }

    /**
     *
     * @param spid
     * @param name
     * @param hp
     * @param mp
     * @param str
     * @param agi
     * @param dex
     * @param ini
     * @param luk
     * @param def
     * @param mdef
     * @param classId
     */
    public Chara(int spid, String name, int hp, int mp, int str, int agi, int dex, int ini, int luk, int def, int mdef, int classId) {
        this(name, spid);
        setupBattle(hp, mp, str, agi, dex, ini, luk, def, mdef, classId);
    }

    /**
     *
     * @param name
     * @param x
     * @param y
     * @param isBlock
     * @param spid
     * @param framesFrom
     * @param framesTo
     */
    public Chara(String name, int x, int y, boolean isBlock, int spid, int framesFrom, int framesTo) {
        this.name = name;
        this.posx = x;
        this.posy = y;
        this.spid = spid;
        this.framesFrom = framesFrom;
        this.framesTo = framesTo;
        this.animMode = 1;
        this.isBlock = isBlock;
    }

    /**
     *
     * @param name
     * @param x
     * @param y
     * @param isBlock
     * @param spid
     */
    public Chara(String name, int x, int y, boolean isBlock, int spid) {
        setupChara(name, spid);
        this.posx = x;
        this.posy = y;
        this.isBlock = isBlock;
    }

    /**
     *
     * @param name
     * @param spid
     */
    public Chara(String name, int spid) {
        setupChara(name, spid);
    }

    /**
     *
     * @param name
     * @param spid
     */
    public void setupChara(String name, int spid) {
        framesFrom = 0;
        framesTo = 16;
        this.spid = spid;
        this.name = name;
        this.animMode = 0;
    }

    /**
     *
     */
    public Chara() {
        this("", 0);
        this.canMove = true;
        this.canAct = true;
        this.atb = 0;
    }

    /**
     *
     * @return
     */
    public int getFrame() {
        switch(animMode) {
            case animPlay:
                currentFrame++;
                if (currentFrame >= framesTo) currentFrame = framesFrom;
                return currentFrame;
            case animMove:
                return currentFrame + frofs;
            case animRoll:
                frofs = (frofs + 1) & 3;
                return frofs * 4;
        }
        return 0;
    }

    /**
     *
     * @param x
     * @param y
     */
    public void setPos(int x, int y) {
        posx = x;
        posy = y;
    }

    /**
     * 
     * @param dir 
     */
    public void setFace(int dir) {
        if (animMode == animMove) currentFrame = dir * 4;
        face = dir;
    }

    /**
     * 
     * @return 
     */
    public int getFace() {
        return face;
    }

    /**
     *
     */
    public void moveLeft() {
        if (animMode == animMove) {
            currentFrame = Team.faceLeft * 4;
            if (face == Team.faceLeft) {
                frofs = (frofs + 1) & 3;
            } else {
                frofs = 0;
            }
        }
        posx--;
        face = Team.faceLeft;
    }

    /**
     *
     */
    public void moveRight() {
        if (animMode == animMove) {
            currentFrame = Team.faceRight * 4;
            if (face == Team.faceRight) {
                frofs = (frofs + 1) & 3;
            } else {
                frofs = 0;
            }
        }
        posx++;
        face = Team.faceRight;
    }

    /**
     *
     */
    public void moveUp() {
        if (animMode == animMove) {
            currentFrame = Team.faceUp * 4;
            if (face == Team.faceUp) {
                frofs = (frofs + 1) & 3;
            } else {
                frofs = 0;
            }
        }
        posy--;
        face = Team.faceUp;
    }

    /**
     *
     */
    public void moveDown() {
        if (animMode == animMove) {
            currentFrame = Team.faceDown * 4;
            if (face == Team.faceDown) {
                frofs = (frofs + 1) & 3;
            } else {
                frofs = 0;
            }
        }
        posy++;
        face = Team.faceDown;
    }

    /**
     *
     * @param idx
     * @return
     */
    public boolean addSkill(int idx) {
        if (skills[idx] > 0) return false;
        if (skills[idx] == 0) skills[idx]++;
        return true;
    }

    /**
     *
     * @param idx
     * @return
     */
    public boolean removeSkill(int idx) {
        if (skills[idx] == 0) return false;
        skills[idx] = 0;
        return true;
    }

    /**
     *
     * @param idx
     * @param lvl
     * @param t
     * @return
     */
    public SkillResult castSkill(int idx, byte lvl, Chara t) {
        return World.instance.getSkill(idx).cast(lvl, this, t);
    }

    /**
     *
     * @return
     */
    public ActionSkill[] getSkills() {
        int j = 0;
        ActionSkill[] k;
        for (char i = 0; i < 256; i++) {
            if (skills[i] > 0) j++;
        }
        if (j == 0) return null;
        k = new ActionSkill[j];
        j = 0;
        for (char i = 0; i < 256; i++) {
            if (skills[i] > 0) {
                try {
                    k[j] = new ActionSkill(j, this, World.instance.getSkill(i), 1);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("i=" + (int) i);
                }
                j++;
            }
        }
        return k;
    }

    /**
     *
     */
    public void calc() {
        clearAll();
        calcBase();
        calcBase();
        arrange();
    }

    private void clearAll() {
        strb = 0;
        agib = 0;
        dexb = 0;
        inib = 0;
        lukb = 0;
        defb = 0;
        mdefb = 0;
        patkb = 0;
        matkb = 0;
        phitb = 0;
        mhitb = 0;
        fleeb = 0;
        crib = 0;
        fcrib = 0;
        spdb = 0;
    }

    private void calcBase() {
        patk = str + strb;
        matk = ini + inib;
        phit = (dex + dexb) * 2 + luk;
        mhit = 30 + (ini + inib) * 5 + dex + dexb;
        flee = (agi + agib) * 2 + (luk + lukb);
        cri = (dex + dexb) / 2;
        fcri = luk + lukb;
        spd = 5 + agi / 10;
    }

    /**
     *
     */
    public void printStat() {
        this.calc();
        System.out.println("=======================");
        System.out.println(this.name);
        System.out.print("hpmax:" + hpmax);
        System.out.println(" mpmax:" + mpmax);
        System.out.print("str:" + str + "+" + strb);
        System.out.print(" agi:" + agi + "+" + agib);
        System.out.print(" dex:" + dex + "+" + dexb);
        System.out.print(" ini:" + ini + "+" + inib);
        System.out.println(" luk:" + luk + "+" + lukb);
        System.out.println("patk:" + patk + "+" + patkb + " matk:" + matk + "+" + matkb);
        System.out.print("def:" + def + "+" + defb);
        System.out.println(" mdef:" + mdef + "+" + mdefb);
        System.out.print("hit:" + phit + "+" + phitb);
        System.out.print(" flee:" + flee);
        System.out.println(" cri:" + cri);
        System.out.print("cri flee:" + fcri);
        for (int i = 0; i < 10; i++) {
            if (eq[i] > 0) System.out.println(World.instance.items[eq[i]].name);
        }
    }

    /**
     *
     */
    public void fullHeal() {
        hp = hpmax;
        mp = mpmax;
        this.isDead = false;
    }

    /**
     *
     * @param s
     * @param t
     * @return
     */
    public String doAttackAI(Chara[] s, Chara[] t) {
        int i = s.length;
        while (true) {
            i = World.instance.rand(t.length - 1);
            if (!t[i].isDead) break;
        }
        return i + "|" + this.castSkill(0, (byte) 0, t[i]);
    }

    /**
     *
     */
    public void arrange() {
        if (this.hp > this.hpmax) this.hp = this.hpmax;
        if (this.mp > this.mpmax) this.mp = this.mpmax;
    }

    /**
     * �����������
     * @return
     */
    public ItemWeapon getPrimaryWeapon() {
        if (World.instance.items[eq[1]] == null) return World.hand;
        try {
            return (ItemWeapon) World.instance.items[eq[1]];
        } catch (RuntimeException e) {
            return World.hand;
        }
    }

    /**
     * ��ø�������
     * @return ����������null
     */
    public ItemWeapon getSecondaryWeapon() {
        try {
            return (ItemWeapon) World.instance.items[eq[2]];
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * ����Buff
     * @param t
     */
    public void calcBuffSkill(Chara t, Skill sk) {
        for (int i = 0; i < eq.length; i++) {
            if (eq[i] != 0) {
                try {
                    ((ItemEquip) World.instance.items[eq[i]]).dealOnSkill(this, t, sk);
                } catch (java.lang.ClassCastException e) {
                }
            }
        }
    }

    /**
     *
     * @param pos
     * @param itemId
     * @return
     */
    public boolean equip(int pos, int itemId) {
        try {
            ItemEquip equip = (ItemEquip) World.instance.items[itemId];
            if (!equip.isFit(this, pos)) return false;
            eq[pos] = itemId;
            this.calc();
            return true;
        } catch (Exception e) {
            System.out.println("Equip Failed:" + e.toString());
            return false;
        }
    }

    /**
     *
     * @param spid
     * @param name
     * @param hp
     * @param mp
     * @param str
     * @param agi
     * @param dex
     * @param ini
     * @param luk
     * @param def
     * @param mdef
     * @param classId
     * @return
     */
    public static Chara createMob(int spid, String name, int hp, int mp, int str, int agi, int dex, int ini, int luk, int def, int mdef, int classId) {
        Chara ret = new Chara(spid, name, hp, mp, str, agi, dex, ini, luk, def, mdef, classId);
        ret.hp = ret.hpmax;
        ret.mp = ret.mpmax;
        ret.controlType = control_auto0;
        return ret;
    }

    /**
     * 
     */
    public void cooldown() {
        for (int i = 0; i < 256; i++) {
            if (skillsCD[i] > 0) skillsCD[i]--;
        }
    }

    public void playRange(int from, int to) {
        this.animMode = Chara.animPlay;
        this.framesFrom = from;
        this.framesTo = to;
    }

    public String toString() {
        return "Chara:" + name + "@" + posx + "," + posy;
    }
}
