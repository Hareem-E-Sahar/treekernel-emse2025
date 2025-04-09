package kanjitori;

import com.jme.light.PointLight;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.state.LightState;
import com.jme.system.DisplaySystem;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.jme.scene.Controller;
import kanjitori.graphics.bot.Bot;
import kanjitori.graphics.bot.BotController;
import kanjitori.events.AbstractListener;
import kanjitori.events.BotKilledEvent;
import kanjitori.events.EntrySolvedEvent;
import kanjitori.events.ManagerService;
import kanjitori.graphics.bot.BotAbstractFactory;
import kanjitori.graphics.bot.BotFactory;
import kanjitori.graphics.ortho.hud.AbstractHud;
import kanjitori.graphics.ortho.hud.DefaultHud;
import kanjitori.graphics.item.ChangeItem;
import kanjitori.graphics.item.HealthItem;
import kanjitori.graphics.item.Item;
import kanjitori.graphics.item.ItemController;
import kanjitori.graphics.item.TeleportItem;
import kanjitori.lesson.Entry;
import kanjitori.lesson.Lesson;
import kanjitori.map.ContentMatrix;
import kanjitori.map.Map;
import kanjitori.map.Position;
import kanjitori.stats.Statistics;
import kanjitori.util.Randomizer;

/**
 *
 * @author Pirx
 */
public class LevelManager {

    public static final Random RANDOM = new Random();

    private static ContentMatrix contentMatrix;

    private static List<Entry> entries;

    private static List<Entry> activeEntries = new ArrayList<Entry>();

    private static Node rootNode;

    private static BotDropController botDropper;

    private static ItemDropController itemDropper;

    private static BotFactory botFactory;

    private static java.util.Map<String, String> botParams;

    private static int botCount = 0;

    private static Statistics statistics;

    private LevelManager() {
    }

    public static void init(Map map, Lesson lesson, Node rootNode) {
        Main.loadingActivity(10, "get entries...");
        LevelManager.rootNode = rootNode;
        LevelManager.entries = lesson.getEntries();
        LevelManager.contentMatrix = map.getContentMatrix();
        Main.loadingActivity(10, "init light...");
        initLight();
        Main.loadingActivity(20, "init map...");
        initMap(map);
        Main.loadingActivity(30, "init bots...");
        initBots(lesson, map.getBotCount());
        Main.loadingActivity(40, "init items...");
        initItems();
        Main.loadingActivity(50, "init listeners...");
        initListeners();
        Main.loadingActivity(60, "init player...");
        initPlayer(map.getSkyboxTextures());
        Main.loadingActivity(90, "init statistics...");
        initStatistics(lesson, map.getName());
        Main.loadingActivity(100, "loading complete...");
    }

    private static void initMap(Map map) {
        rootNode.attachChild(map.getNode());
    }

    private static void initListeners() {
        ManagerService.register(new AbstractListener<EntrySolvedEvent>(EntrySolvedEvent.class) {

            public void notify(EntrySolvedEvent event) {
                entrySolved(event.getContent());
            }
        });
        ManagerService.register(new AbstractListener<BotKilledEvent>(BotKilledEvent.class) {

            public void notify(BotKilledEvent event) {
                botKilled(event.getContent().getValue());
            }
        });
    }

    public static void initStatistics(Lesson lesson, String mapName) {
        statistics = new Statistics(lesson.getName(), mapName);
        statistics.setQuestionsTotal(entries.size());
        int bots = 0;
        for (Entry entry : entries) {
            bots += entry.getValues().size();
        }
        statistics.setBotsTotal(bots);
    }

    public static void initItems() {
        Randomizer<Class<? extends Item>> itemRand = new Randomizer<Class<? extends Item>>();
        itemRand.add(HealthItem.class, 4);
        itemRand.add(TeleportItem.class, 2);
        itemRand.add(ChangeItem.class, 1);
        rootNode.addController(itemDropper = new ItemDropController(30, 120, itemRand));
    }

    public static void initBots(Lesson lesson, int botCount) {
        try {
            String botType = lesson.getBotType();
            botFactory = (BotFactory) BotAbstractFactory.getInstance().getFactory(botType);
            botFactory.setParams(lesson.getBotParams());
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }
        rootNode.addController(botDropper = new BotDropController(40));
        for (int i = 0; i < botCount; i++) {
            dropNewBot();
        }
    }

    private static void initPlayer(String[] skyboxTextures) {
        new Player(rootNode, contentMatrix, skyboxTextures);
        Player.getPlayer().setEntry(getNextEntry());
    }

    private static void initLight() {
        PointLight light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 0.75f));
        light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        light.setLocation(new Vector3f(100, 100, 100));
        light.setEnabled(true);
        LightState lightState = DisplaySystem.getDisplaySystem().getRenderer().createLightState();
        lightState.setEnabled(true);
        lightState.attach(light);
        rootNode.setRenderState(lightState);
        rootNode.updateRenderState();
    }

    public static List<Entry> getActiveEntries() {
        return activeEntries;
    }

    public static void dropNewBot() {
        if (entries.size() == 0) {
            return;
        }
        Entry entry = entries.remove(0);
        try {
            for (String value : entry.getValues()) {
                Bot bot = botFactory.get(value);
                bot.getNode().addController(new BotController(bot, contentMatrix));
                rootNode.attachChild(bot.getNode());
                rootNode.updateRenderState();
                botDropper.resetTime();
                botCount++;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        activeEntries.add(entry);
        updateBotCount();
    }

    public static void dropNewItem(Item item) {
        Position pos = contentMatrix.findFreePos();
        item.getNode().setLocalTranslation(new Vector3f(pos.x() * 2, 0, pos.y() * 2));
        rootNode.attachChild(item.getNode());
        item.getNode().addController(new ItemController(item, contentMatrix));
        rootNode.updateRenderState();
    }

    private static void updateBotCount() {
        DefaultHud.getHud().showBotCount(botCount);
    }

    public static void win() {
        statistics.setResult(Statistics.Result.WIN);
        Main.finishGame(statistics);
    }

    public static void die() {
        statistics.setResult(Statistics.Result.LOSS);
        Main.finishGame(statistics);
    }

    public static void botKilled(String value) {
        statistics.incBotsRight();
        AbstractHud.getHud().addSolution(value);
        botCount--;
        updateBotCount();
        if (botCount < 5) {
            dropNewBot();
        }
    }

    public static void entrySolved(Entry entry) {
        statistics.incQuestionsRight();
        if (activeEntries.contains(entry)) {
            activeEntries.remove(entry);
            if (activeEntries.size() == 0) {
                win();
                return;
            }
        }
        Player.getPlayer().entrySolved();
    }

    public static Entry getNextEntry() {
        return activeEntries.get(RANDOM.nextInt(activeEntries.size()));
    }

    private static class BotDropController extends Controller {

        private float time = 0;

        private float dropRate;

        public BotDropController(float dropRate) {
            this.dropRate = dropRate;
        }

        public float getTime() {
            return time;
        }

        public void resetTime() {
            time = 0;
        }

        public void update(float f) {
            time += f;
            if (time > dropRate) {
                time = 0;
                dropNewBot();
            }
        }
    }

    public static Statistics getStatistics() {
        return statistics;
    }

    private static class ItemDropController extends Controller {

        private float time = 0;

        private int minDropRate;

        private int maxDropRate;

        private int currentDrop;

        private Randomizer<Class<? extends Item>> itemRand;

        public ItemDropController(int minDropRate, int maxDropRate, Randomizer<Class<? extends Item>> itemRand) {
            this.minDropRate = minDropRate;
            this.maxDropRate = maxDropRate;
            this.itemRand = itemRand;
            calcCurrentDrop();
        }

        private void calcCurrentDrop() {
            currentDrop = minDropRate + RANDOM.nextInt(maxDropRate - minDropRate);
        }

        public float getTime() {
            return time;
        }

        public void resetTime() {
            time = 0;
        }

        public void update(float f) {
            time += f;
            if (time > currentDrop) {
                time = 0;
                calcCurrentDrop();
                try {
                    dropNewItem(itemRand.get().newInstance());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
