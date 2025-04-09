package org.jcrpg.util.saveload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.jcrpg.game.CharacterCreationRules;
import org.jcrpg.game.GameLogic;
import org.jcrpg.game.GameStateContainer;
import org.jcrpg.game.scenario.Scenario;
import org.jcrpg.game.scenario.ScenarioLoader.ScenarioDescription;
import org.jcrpg.threed.J3DCore;
import org.jcrpg.threed.J3DCore.InitCallbackObject;
import org.jcrpg.ui.window.BusyPaneWindow;
import org.jcrpg.util.HashUtil;
import org.jcrpg.world.Engine;
import org.jcrpg.world.CoreTools;
import org.jcrpg.world.ai.DistanceBasedBoundary;
import org.jcrpg.world.ai.Ecology;
import org.jcrpg.world.ai.EcologyGenerator;
import org.jcrpg.world.ai.PersistentMemberInstance;
import org.jcrpg.world.ai.humanoid.HumanoidEntityDescription;
import org.jcrpg.world.ai.humanoid.MemberPerson;
import org.jcrpg.world.ai.player.Party;
import org.jcrpg.world.ai.player.PartyInstance;
import org.jcrpg.world.generator.WorldGenerator;
import org.jcrpg.world.generator.WorldParams;
import org.jcrpg.world.generator.WorldParamsConfigLoader;
import org.jcrpg.world.generator.program.DefaultClassFactory;
import org.jcrpg.world.generator.program.DefaultGenProgram;
import org.jcrpg.world.place.World;
import org.jcrpg.world.place.economic.residence.RoadShrine;
import org.jcrpg.world.place.orbiter.WorldOrbiterHandler;
import org.jcrpg.world.place.orbiter.moon.SimpleMoon;
import org.jcrpg.world.place.orbiter.sun.SimpleSun;
import org.jcrpg.world.time.Time;
import com.ardor3d.image.util.ScreenShotImageExporter;
import com.ardor3d.math.Vector3;
import com.ardor3d.util.screen.ScreenExporter;

/**
 * Object for creating new / saving / loading game state.
 * @author pali
 *
 */
public class SaveLoadNewGame {

    public static final String saveDirVanilla = "./save";

    public static final String charsDirVanilla = "./chars";

    public static final String prefix = (System.getProperty("os.name").contains("Windows") ? (System.getenv("APPDATA") != null ? (System.getenv("APPDATA") + "/jclassicrpg") : (System.getProperty("user.home") + "/AppData/Local/jclassicrpg")) : (System.getProperty("user.home") + "/.jclassicrpg"));

    public static final String saveDir = prefix + "/save";

    public static final String charsDir = prefix + "/chars";

    public static final String shotsDir = prefix + "/shots";

    public static final String logsDir = prefix + "/log";

    public static void newGame(J3DCore core, Collection<PersistentMemberInstance> partyMembers, CharacterCreationRules cCR, ScenarioDescription desc) {
        try {
            core.busyPane.setToType(BusyPaneWindow.LOADING, "Creating World...");
            core.busyPane.show();
            core.drawForced();
            System.gc();
            CoreTools.initializeWorld(false, core, partyMembers, cCR, desc);
            if (core.coreFullyInitialized) {
                core.sEngine.reinit();
                core.eEngine.reinit();
            }
            core.uiBase.hud.mainBox.addEntry("Press RIGHT button on mouse to toggle between mouselook and cursor.");
        } catch (Exception ex) {
            core.busyPane.hide();
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public static void saveGame(J3DCore core, String slotName, InitCallbackObject callbackObject) {
        if (core.gameLost) {
            core.uiBase.hud.mainBox.addEntry("Cannot save lost game.");
            return;
        }
        try {
            core.busyPane.setToType(BusyPaneWindow.LOADING, "Saving...");
            core.busyPane.show();
            core.drawForced();
            Date d = new Date();
            String dT = new SimpleDateFormat("yyyyMMdd-HHmmss.SSS").format(d);
            String slot = saveDir + "/" + core.gameState.gameId + "_" + dT + "/";
            File f = new File(slot);
            f.mkdirs();
            File desc = new File(slot + "desc.txt");
            FileWriter fw = new FileWriter(desc);
            String dT2 = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(d) + " Seed:" + core.gameState.scenarioDesc.seed;
            fw.write((slotName != null && slotName.length() > 15 ? slotName.substring(0, 15) : slotName) + "\n(" + dT2 + ")");
            fw.close();
            File saveGame = new File(slot + "savegame.zip");
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(saveGame));
            zipOutputStream.putNextEntry(new ZipEntry("gamestate.xml"));
            long time = System.currentTimeMillis();
            core.gameState.getGameStateXml(zipOutputStream);
            System.out.println("][][][][][][ SAVE TIME = " + (System.currentTimeMillis() - time));
            zipOutputStream.close();
            core.busyPane.hide();
            core.drawForced();
            try {
                ScreenShotImageExporter _screenShotExp = new ScreenShotImageExporter(new File(slot), "screen", "jpg", false);
                core.screenshotControlled(_screenShotExp, callbackObject);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            core.uiBase.hud.mainBox.addEntry("Game saved.");
        } catch (Exception ex) {
            core.busyPane.hide();
            ex.printStackTrace();
        }
    }

    public static void loadGame(J3DCore core, File saveGame) {
        try {
            core.busyPane.setToType(BusyPaneWindow.LOADING, "Loading...");
            core.busyPane.show();
            core.drawForced();
            System.gc();
            if (core.engineThread != null) {
                core.engineThread.interrupt();
            }
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(saveGame));
            zipInputStream.getNextEntry();
            Reader reader = new InputStreamReader(zipInputStream);
            long time = System.currentTimeMillis();
            GameStateContainer gameState = GameStateContainer.createGameStateFromXml(reader);
            HashUtil.WORLD_RANDOM_SEED = gameState.scenarioDesc.seed;
            System.out.println("[][][][][][] LOAD TIME = " + (System.currentTimeMillis() - time));
            gameState.world.onLoad();
            gameState.ecology.onLoad();
            core.setGameState(gameState);
            zipInputStream.close();
            gameState.engine.setPause(true);
            Thread t = new Thread(gameState.engine);
            t.start();
            core.engineThread = t;
            gameState.gameLogic.core = core;
            if (core.coreFullyInitialized) {
                core.sEngine.reinit();
                core.eEngine.reinit();
            }
            core.behaviorWindow.party = gameState.player;
            core.behaviorWindow.updateToParty();
            gameState.resetGeneral();
            gameState.onLoad();
            core.uiBase.hud.mainBox.addEntry("Game loaded. Scen.: " + gameState.scenarioDesc.name + " " + gameState.scenarioDesc.version);
            core.uiBase.hud.mainBox.addEntry("Press RIGHT button on mouse to toggle between mouselook and cursor.");
            core.gameLost = false;
        } catch (Exception ex) {
            core.busyPane.hide();
            ex.printStackTrace();
        }
    }

    public static void saveCharacter(MemberPerson person) {
        Date d = new Date();
        String dT = new SimpleDateFormat("yyyyddMM-HH.mm.ss.SSS").format(d);
        String slot = charsDir + "/" + dT + "_" + person.foreName + "/";
        File f = new File(slot);
        f.mkdirs();
        File saveGame = new File(slot + "character.zip");
        try {
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(saveGame));
            zipOutputStream.putNextEntry(new ZipEntry("character.xml"));
            person.getXml(zipOutputStream);
            zipOutputStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static MemberPerson loadCharacter(File fileName) {
        try {
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(fileName));
            zipInputStream.getNextEntry();
            Reader reader = new InputStreamReader(zipInputStream);
            return MemberPerson.createFromXml(reader);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static SaveLoadNewGame getInstance() {
        return new SaveLoadNewGame();
    }

    public void callbackAfterInit(Object param) {
    }
}
