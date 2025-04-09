package l1j.server.server.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import l1j.server.Config;
import l1j.server.server.ActionCodes;
import l1j.server.server.GeneralThreadPool;
import l1j.server.server.datatables.ItemTable;
import l1j.server.server.datatables.UBSpawnTable;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1MonsterInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.item.L1ItemId;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1Item;
import l1j.server.server.utils.IntRange;

public class L1UltimateBattle {

    private String _ubname;

    private int _locX;

    private int _locY;

    private L1Location _location;

    private short _mapId;

    private int _locX1;

    private int _locY1;

    private int _locX2;

    private int _locY2;

    private int _ubId;

    private int _pattern;

    private boolean _isNowUb;

    private boolean _active;

    private int _minLevel;

    private int _maxLevel;

    private int _maxPlayer;

    private boolean _enterRoyal;

    private boolean _enterKnight;

    private boolean _enterMage;

    private boolean _enterElf;

    private boolean _enterDarkelf;

    private boolean _enterDragonknight;

    private boolean _enterBlackwizard;

    private boolean _enterMale;

    private boolean _enterFemale;

    private boolean _usePot;

    private int _hpr;

    private int _mpr;

    private static int BEFORE_MINUTE = 5;

    private Set<Integer> _managers = new HashSet<Integer>();

    private SortedSet<Integer> _ubTimes = new TreeSet<Integer>();

    private static final Logger _log = Logger.getLogger(L1UltimateBattle.class.getName());

    private final ArrayList<L1PcInstance> _members = new ArrayList<L1PcInstance>();

    /**
	 * ���� ���ý��� �޼����� �۽��Ѵ�.
	 *
	 * @param curRound
	 *            �����ϴ� ����
	 */
    private void sendRoundMessage(int curRound) {
        if (curRound == 1) {
        } else if (curRound == 2) {
            sendMessage("�ݷԼ��� ����: �� 2 �� ����!");
        } else if (curRound == 3) {
            sendMessage("�ݷԼ��� ����: �� 3 �� ����!");
        } else if (curRound == 4) {
            sendMessage("�ݷԼ��� ����: ������ ����! ���� �ð��� 5�� �Դ�");
            sendMessage("    ��.");
        }
    }

    /**
	 * �Ϻε��� ���� �������� ������Ų��.
	 *
	 * @param curRound
	 *            ������ ����
	 */
    private void spawnSupplies(int curRound) {
        if (curRound == 1) {
            spawnGroundItem(L1ItemId.ADENA, 1000, 50);
            spawnGroundItem(L1ItemId.POTION_OF_CURE_POISON, 3, 20);
            spawnGroundItem(40023, 5, 20);
            spawnGroundItem(40024, 3, 20);
            spawnGroundItem(40317, 1, 5);
            spawnGroundItem(40079, 1, 20);
            sendMessage("�ݷԼ��� ����: �� 1 ���� ������ �Ϸ�Ǿ���ϴ�.");
            sendMessage("�ݷԼ��� ����: 1�� �Ŀ� �� 2 ���� ������ ���۵˴�");
            sendMessage("    ��.");
        } else if (curRound == 2) {
            spawnGroundItem(L1ItemId.ADENA, 5000, 30);
            spawnGroundItem(L1ItemId.POTION_OF_CURE_POISON, 5, 20);
            spawnGroundItem(40023, 10, 20);
            spawnGroundItem(40024, 5, 20);
            spawnGroundItem(40317, 1, 7);
            spawnGroundItem(40093, 1, 10);
            spawnGroundItem(40079, 1, 5);
            sendMessage("�ݷԼ��� ����: �� 2 ���� ������ �Ϸ�Ǿ���ϴ�.");
            sendMessage("�ݷԼ��� ����: 1�� �Ŀ� �� 3 ���� ������ ���۵˴�");
            sendMessage("    ��.");
        } else if (curRound == 3) {
            spawnGroundItem(L1ItemId.ADENA, 20000, 20);
            spawnGroundItem(L1ItemId.POTION_OF_CURE_POISON, 7, 20);
            spawnGroundItem(40023, 20, 20);
            spawnGroundItem(40024, 10, 20);
            spawnGroundItem(40317, 1, 10);
            spawnGroundItem(40094, 1, 10);
            spawnGroundItem(500028, 1, 1);
            sendMessage("�ݷԼ��� ����: �� 3 �������� ������ �Ϸ�Ǿ����");
            sendMessage("    ��.");
            sendMessage("�ݷԼ��� ����: 5�� �Ŀ� �������� ���۵˴ϴ�.");
        }
    }

    /**
	 * �ݷԼ������κ��� ���� ����� ��� ����Ʈ�κ��� �����Ѵ�.
	 */
    private void removeRetiredMembers() {
        L1PcInstance[] temp = getMembersArray();
        for (int i = 0; i < temp.length; i++) {
            if (temp[i].getMapId() != _mapId) {
                removeMember(temp[i]);
            }
        }
    }

    /**
	 * UB�� ���ϰ� �ִ� �÷��̾ �޼���(S_ServerMessage)�� �۽��Ѵ�.
	 *
	 * @param type
	 *            �޼��� Ÿ��
	 * @param msg
	 *            �۽��ϴ� �޼���
	 */
    private void sendMessage(String msg) {
        for (L1PcInstance pc : getMembersArray()) {
            pc.sendPackets(new S_SystemMessage(msg));
        }
    }

    /**
	 * �ݷԼ���� �������� ������Ų��.
	 *
	 * @param itemId
	 *            ������Ű�� �������� ������ ID
	 * @param stackCount
	 *            �������� ���ü�
	 * @param count
	 *            ������Ű�� ��
	 */
    private void spawnGroundItem(int itemId, int stackCount, int count) {
        L1Item temp = ItemTable.getInstance().getTemplate(itemId);
        if (temp == null) {
            return;
        }
        for (int i = 0; i < count; i++) {
            L1Location loc = _location.randomLocation((getLocX2() - getLocX1()) / 2, false);
            if (temp.isStackable()) {
                L1ItemInstance item = ItemTable.getInstance().createItem(itemId);
                item.setEnchantLevel(0);
                item.setCount(stackCount);
                L1GroundInventory ground = L1World.getInstance().getInventory(loc.getX(), loc.getY(), _mapId);
                if (ground.checkAddItem(item, stackCount) == L1Inventory.OK) {
                    ground.storeItem(item, null);
                }
            } else {
                L1ItemInstance item = null;
                for (int createCount = 0; createCount < stackCount; createCount++) {
                    item = ItemTable.getInstance().createItem(itemId);
                    item.setEnchantLevel(0);
                    L1GroundInventory ground = L1World.getInstance().getInventory(loc.getX(), loc.getY(), _mapId);
                    if (ground.checkAddItem(item, stackCount) == L1Inventory.OK) {
                        ground.storeItem(item, null);
                    }
                }
            }
        }
    }

    /**
	 * �ݷԼ������ �����۰� monster�� ��� �����Ѵ�.
	 */
    private void clearColosseum() {
        for (Object obj : L1World.getInstance().getVisibleObjects(_mapId).values()) {
            if (obj instanceof L1MonsterInstance) {
                L1MonsterInstance mob = (L1MonsterInstance) obj;
                if (!mob.isDead()) {
                    mob.setDead(true);
                    mob.setStatus(ActionCodes.ACTION_Die);
                    mob.setCurrentHpDirect(0);
                    mob.deleteMe();
                }
            } else if (obj instanceof L1Inventory) {
                L1Inventory inventory = (L1Inventory) obj;
                inventory.clearItems();
            }
        }
    }

    /**
	 * constructor�� ��.
	 */
    public L1UltimateBattle() {
    }

    class UbThread implements Runnable {

        /**
		 * ��� ���ñ����� ī��Ʈ�ٿ� �Ѵ�.
		 *
		 * @throws InterruptedException
		 */
        private void countDown() throws InterruptedException {
            L1World.getInstance().broadcastServerMessage("�ȳ��ϼ��� " + String.format(Config.GAME_SERVER_NAME) + "�Դϴ�.");
            Thread.sleep(1000 * 1 * 2);
            L1World.getInstance().broadcastServerMessage("����� " + _ubname + "(����:" + _minLevel + "~" + _maxLevel + ") �ݷԼ��򿡼� " + getUbname() + "�� �����ϴ�. ���� ����е��� �� �ٶ�ϴ�. �����մϴ�.");
            Thread.sleep(1000 * 60 * 3);
            L1World.getInstance().broadcastServerMessage("�ȳ��ϼ��� " + Config.GAME_SERVER_NAME + "�Դϴ�.");
            Thread.sleep(1000 * 1 * 2);
            L1World.getInstance().broadcastServerMessage("����� " + _ubname + "(����:" + _minLevel + "~" + _maxLevel + ") �ݷԼ��򿡼� " + getUbname() + "�� �����ϴ�. ���� ����е��� �� �ٶ�ϴ�. �����մϴ�.");
            for (int loop = 0; loop < BEFORE_MINUTE * 60 - 15; loop++) {
                Thread.sleep(1000);
            }
            removeRetiredMembers();
            sendMessage("�ݷԼ��� ����: ���� �� ���͵��� ������ ���Դϴ�");
            sendMessage("    . ������ ���ϴ�.");
            Thread.sleep(5000);
            sendMessage("�ݷԼ��� ����: 10�ʵڿ� ��⸦ ���� �մϴ�.");
            Thread.sleep(5000);
            sendMessage("�ݷԼ��� ����: 5 !!");
            Thread.sleep(1000);
            sendMessage("�ݷԼ��� ����: 4 !!");
            Thread.sleep(1000);
            sendMessage("�ݷԼ��� ����: 3 !!");
            Thread.sleep(1000);
            sendMessage("�ݷԼ��� ����: 2 !!");
            Thread.sleep(1000);
            sendMessage("�ݷԼ��� ����: 1 !!");
            Thread.sleep(1000);
            sendMessage("�ݷԼ��� ����: �� 1 �� ����!");
            removeRetiredMembers();
        }

        /**
		 * ��� monster�� ������ ��, ������ ���尡 ���۵� �������� �ð��� ����Ѵ�.
		 *
		 * @param curRound
		 *            ������ ����
		 * @throws InterruptedException
		 */
        private void waitForNextRound(int curRound) throws InterruptedException {
            final int WAIT_TIME_TABLE[] = { 6, 6, 30, 30 };
            int wait = WAIT_TIME_TABLE[curRound - 1];
            for (int i = 0; i < wait; i++) {
                Thread.sleep(10000);
            }
            removeRetiredMembers();
        }

        /**
		 * thread ���ν���.
		 */
        @Override
        public void run() {
            try {
                setActive(true);
                countDown();
                setNowUb(true);
                for (int round = 1; round <= 4; round++) {
                    sendRoundMessage(round);
                    L1UbPattern pattern = UBSpawnTable.getInstance().getPattern(_ubId, _pattern);
                    ArrayList<L1UbSpawn> spawnList = pattern.getSpawnList(round);
                    for (L1UbSpawn spawn : spawnList) {
                        if (getMembersCount() > 0) {
                            spawn.spawnAll();
                        }
                        Thread.sleep(spawn.getSpawnDelay() * 1000);
                    }
                    if (getMembersCount() > 0) {
                        spawnSupplies(round);
                    }
                    waitForNextRound(round);
                }
                for (L1PcInstance pc : getMembersArray()) {
                    Random random = new Random();
                    int rndx = random.nextInt(4);
                    int rndy = random.nextInt(4);
                    int locx = 33503 + rndx;
                    int locy = 32764 + rndy;
                    short mapid = 4;
                    L1Teleport.teleport(pc, locx, locy, mapid, 5, true);
                    removeMember(pc);
                }
                clearColosseum();
                setActive(false);
                setNowUb(false);
            } catch (Exception e) {
                _log.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
        }
    }

    /**
	 * ��Ƽ����Ʈ ��Ʋ�� �����Ѵ�.
	 *
	 * @param ubId
	 *            �����ϴ� ��Ƽ����Ʈ ��Ʋ�� ID
	 */
    public void start() {
        int patternsMax = UBSpawnTable.getInstance().getMaxPattern(_ubId);
        Random random = new Random();
        _pattern = random.nextInt(patternsMax) + 1;
        UbThread ub = new UbThread();
        GeneralThreadPool.getInstance().execute(ub);
    }

    /**
	 * �÷��̾ �� ��� ����Ʈ�� �߰��Ѵ�.
	 *
	 * @param pc
	 *            ���Ӱ� ���ϴ� �÷��̾�
	 */
    public void addMember(L1PcInstance pc) {
        if (!_members.contains(pc)) {
            _members.add(pc);
        }
    }

    /**
	 * �÷��̾ �� ��� ����Ʈ�κ��� �����Ѵ�.
	 *
	 * @param pc
	 *            �����ϴ� �÷��̾�
	 */
    public void removeMember(L1PcInstance pc) {
        _members.remove(pc);
    }

    /**
	 * �� ��� ����Ʈ�� Ŭ���� �Ѵ�.
	 */
    public void clearMembers() {
        _members.clear();
    }

    /**
	 * �÷��̾, �� ��������� �����ش�.
	 *
	 * @param pc
	 *            �����ϴ� �÷��̾�
	 * @return �� ����̸� true, �׷��� ������ false.
	 */
    public boolean isMember(L1PcInstance pc) {
        return _members.contains(pc);
    }

    /**
	 * �� ����� �迭�� �ۼ���, �����ش�.
	 *
	 * @return �� ����� �迭
	 */
    public L1PcInstance[] getMembersArray() {
        return _members.toArray(new L1PcInstance[_members.size()]);
    }

    /**
	 * �� ������� �����ش�.
	 *
	 * @return �� �����
	 */
    public int getMembersCount() {
        return _members.size();
    }

    /**
	 * UB�������� �����Ѵ�.
	 *
	 * @param i
	 *            true/false
	 */
    private void setNowUb(boolean i) {
        _isNowUb = i;
    }

    /**
	 * UB�������� �����ش�.
	 *
	 * @return UB���̸� true, �׷��� ������ false.
	 */
    public boolean isNowUb() {
        return _isNowUb;
    }

    public String getUbname() {
        return _ubname;
    }

    public void setUbname(String ubname) {
        this._ubname = ubname;
    }

    public int getUbId() {
        return _ubId;
    }

    public void setUbId(int id) {
        _ubId = id;
    }

    public short getMapId() {
        return _mapId;
    }

    public void setMapId(short mapId) {
        this._mapId = mapId;
    }

    public int getMinLevel() {
        return _minLevel;
    }

    public void setMinLevel(int level) {
        _minLevel = level;
    }

    public int getMaxLevel() {
        return _maxLevel;
    }

    public void setMaxLevel(int level) {
        _maxLevel = level;
    }

    public int getMaxPlayer() {
        return _maxPlayer;
    }

    public void setMaxPlayer(int count) {
        _maxPlayer = count;
    }

    public void setEnterRoyal(boolean enterRoyal) {
        this._enterRoyal = enterRoyal;
    }

    public void setEnterKnight(boolean enterKnight) {
        this._enterKnight = enterKnight;
    }

    public void setEnterMage(boolean enterMage) {
        this._enterMage = enterMage;
    }

    public void setEnterElf(boolean enterElf) {
        this._enterElf = enterElf;
    }

    public void setEnterDarkelf(boolean enterDarkelf) {
        this._enterDarkelf = enterDarkelf;
    }

    public void setEnterDragonknight(boolean enterDragonknight) {
        this._enterDragonknight = enterDragonknight;
    }

    public void setEnterBlackwizard(boolean enterBlackwizard) {
        this._enterBlackwizard = enterBlackwizard;
    }

    public void setEnterMale(boolean enterMale) {
        this._enterMale = enterMale;
    }

    public void setEnterFemale(boolean enterFemale) {
        this._enterFemale = enterFemale;
    }

    public boolean canUsePot() {
        return _usePot;
    }

    public void setUsePot(boolean usePot) {
        this._usePot = usePot;
    }

    public int getHpr() {
        return _hpr;
    }

    public void setHpr(int hpr) {
        this._hpr = hpr;
    }

    public int getMpr() {
        return _mpr;
    }

    public void setMpr(int mpr) {
        this._mpr = mpr;
    }

    public int getLocX1() {
        return _locX1;
    }

    public void setLocX1(int locX1) {
        this._locX1 = locX1;
    }

    public int getLocY1() {
        return _locY1;
    }

    public void setLocY1(int locY1) {
        this._locY1 = locY1;
    }

    public int getLocX2() {
        return _locX2;
    }

    public void setLocX2(int locX2) {
        this._locX2 = locX2;
    }

    public int getLocY2() {
        return _locY2;
    }

    public void setLocY2(int locY2) {
        this._locY2 = locY2;
    }

    public void resetLoc() {
        _locX = (_locX2 + _locX1) / 2;
        _locY = (_locY2 + _locY1) / 2;
        _location = new L1Location(_locX, _locY, _mapId);
    }

    public L1Location getLocation() {
        return _location;
    }

    public void addManager(int npcId) {
        _managers.add(npcId);
    }

    public boolean containsManager(int npcId) {
        return _managers.contains(npcId);
    }

    public void addUbTime(int time) {
        _ubTimes.add(time);
    }

    public String getNextUbTime() {
        return intToTimeFormat(nextUbTime());
    }

    private int nextUbTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
        int nowTime = Integer.valueOf(sdf.format(getRealTime().getTime()));
        SortedSet<Integer> tailSet = _ubTimes.tailSet(nowTime);
        if (tailSet.isEmpty()) {
            tailSet = _ubTimes;
        }
        return tailSet.first();
    }

    private static String intToTimeFormat(int n) {
        return n / 100 + ":" + n % 100 / 10 + "" + n % 10;
    }

    private static Calendar getRealTime() {
        TimeZone _tz = TimeZone.getTimeZone(Config.TIME_ZONE);
        Calendar cal = Calendar.getInstance(_tz);
        return cal;
    }

    public boolean checkUbTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
        Calendar realTime = getRealTime();
        realTime.add(Calendar.MINUTE, BEFORE_MINUTE);
        int nowTime = Integer.valueOf(sdf.format(realTime.getTime()));
        return _ubTimes.contains(nowTime);
    }

    private void setActive(boolean f) {
        _active = f;
    }

    /**
	 * @return UB���� ����~��� ��������� true, �� �ܴ̿� false�� �����ش�.
	 */
    public boolean isActive() {
        return _active;
    }

    /**
	 * UB�� �� �����Ѱ�, ����, Ŭ������ üũ�Ѵ�.
	 *
	 * @param pc
	 *            UB�� ���� �� �ִ��� üũ�ϴ� PC
	 * @return �� �� �� �ִ� ���� true, �� �� ��� ���� false
	 */
    public boolean canPcEnter(L1PcInstance pc) {
        _log.log(Level.FINE, "pcname=" + pc.getName() + " ubid=" + _ubId + " minlvl=" + _minLevel + " maxlvl=" + _maxLevel);
        if (!IntRange.includes(pc.getLevel(), _minLevel, _maxLevel)) {
            return false;
        }
        if (!((pc.isCrown() && _enterRoyal) || (pc.isKnight() && _enterKnight) || (pc.isWizard() && _enterMage) || (pc.isElf() && _enterElf) || (pc.isDarkelf() && _enterDarkelf || (pc.isDragonKnight() && _enterDragonknight) || (pc.isBlackWizard() && _enterBlackwizard)))) {
            return false;
        }
        return true;
    }

    private String[] _ubInfo;

    public String[] makeUbInfoStrings() {
        if (_ubInfo != null) {
            return _ubInfo;
        }
        String nextUbTime = getNextUbTime();
        StringBuilder classesBuff = new StringBuilder();
        if (_enterDarkelf) {
            classesBuff.append("��ũ���� ");
        }
        if (_enterMage) {
            classesBuff.append("����� ");
        }
        if (_enterElf) {
            classesBuff.append("���� ");
        }
        if (_enterKnight) {
            classesBuff.append("��� ");
        }
        if (_enterRoyal) {
            classesBuff.append("���� ");
        }
        if (_enterDragonknight) {
            classesBuff.append("����  ");
        }
        if (_enterBlackwizard) {
            classesBuff.append("ȯ���");
        }
        String classes = classesBuff.toString().trim();
        StringBuilder sexBuff = new StringBuilder();
        if (_enterMale) {
            sexBuff.append("�� ");
        }
        if (_enterFemale) {
            sexBuff.append("�� ");
        }
        String sex = sexBuff.toString().trim();
        String loLevel = String.valueOf(_minLevel);
        String hiLevel = String.valueOf(_maxLevel);
        String teleport = _location.getMap().isEscapable() ? "����" : "�Ұ���";
        String res = _location.getMap().isUseResurrection() ? "����" : "�Ұ���";
        String pot = "����";
        String hpr = String.valueOf(_hpr);
        String mpr = String.valueOf(_mpr);
        String summon = _location.getMap().isTakePets() ? "����" : "�Ұ���";
        String summon2 = _location.getMap().isRecallPets() ? "����" : "�Ұ���";
        _ubInfo = new String[] { nextUbTime, classes, sex, loLevel, hiLevel, teleport, res, pot, hpr, mpr, summon, summon2 };
        return _ubInfo;
    }
}
