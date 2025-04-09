package games.basicgame;

import games.AbstractGameLoader;
import games.basicgame.properties.Property;
import games.basicgame.triggers.ScriptedTrigger;
import games.basicgame.triggers.Trigger;
import games.script.LuaObjectScript;
import games.script.LuaScriptConstructor;
import games.script.ObjectScript;
import games.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BasicGameLoader extends AbstractGameLoader {

    private static final Logger logger = Logger.getLogger("system.loading");

    private static final boolean DEFAULT_PERSIST_DATA_ON_NICK_CHANGE = true;

    @Override
    public BasicGame loadGame(File gameDirectory, Map<Object, Object> setup) {
        BasicGame game = new BasicGame();
        BasicGameConfig config = new BasicGameConfig(new AbstractBasicGameCallback(game));
        config.loadSettingsFrom(gameDirectory, setup);
        config.addDefaultTriggerFiles(gameDirectory);
        config.addDefaultPropertyFiles(gameDirectory);
        config.addDefaultLuaGlobals(gameDirectory);
        this.initGame(game, config);
        return game;
    }

    protected void initGame(BasicGame game, BasicGameConfig config) {
        logger.info("Initing DefaultGame");
        Boolean persistDataOnNickChange = config.getPersistDataOnNickChange();
        if (persistDataOnNickChange == null) persistDataOnNickChange = DEFAULT_PERSIST_DATA_ON_NICK_CHANGE;
        game.setPersistDataOnNickChange(persistDataOnNickChange);
        LuaScriptConstructor.addGlobals(config.getLuaGlobals());
        Set<Property> properties = this.loadProperties(config.getCallback(), config.getPropertyFiles());
        logger.info(properties.size() + " properties loaded");
        game.addProperties(properties);
        List<Trigger> triggers = this.loadTriggers(config.getCallback(), config.getTriggerFiles());
        logger.info(triggers.size() + " triggers loaded");
        game.addTriggers(triggers);
        File saveFile = config.getSaveFile();
        logger.info("Trying to set save file: " + saveFile);
        if (saveFile != null && !saveFile.exists()) {
            saveFile.getParentFile().mkdirs();
            try {
                saveFile.createNewFile();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error while loading save file.", e);
                saveFile = null;
            }
        }
        if (saveFile != null) {
            game.setSaveFile(saveFile);
        }
    }

    private List<Trigger> loadTriggers(BasicGameCallback callback, List<File> triggerFiles) {
        List<Trigger> triggers = new ArrayList<Trigger>();
        for (File triggerFile : triggerFiles) try {
            logger.fine("Loading trigger file " + triggerFile);
            ObjectScript script = LuaScriptConstructor.createLuaObjectScript(triggerFile);
            ScriptedTrigger trigger = new ScriptedTrigger(callback, script, Trigger.CALLBACK, Trigger.CHANNEL, Trigger.SENDER, Trigger.ISPRIVATE);
            trigger.init();
            triggers.add(trigger);
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Could not initialize trigger " + triggerFile.getName(), e);
        }
        return triggers;
    }

    private Set<Property> loadProperties(BasicGameCallback callback, List<File> propertyFiles) {
        Set<Property> properties = new HashSet<Property>();
        for (File propertyFile : propertyFiles) try {
            properties.add(this.loadProperty(callback, propertyFile));
        } catch (PropertyInstantiationException e) {
            logger.log(Level.SEVERE, "Property " + propertyFile.getName() + " could not be instantiated.", e);
        }
        return properties;
    }

    private Property loadProperty(BasicGameCallback callback, File propertyFile) throws PropertyInstantiationException {
        String propertyName = propertyFile.getName();
        String type = null;
        try {
            LuaObjectScript script = LuaScriptConstructor.createLuaObjectScript(propertyFile);
            type = script.callStringHook("getType", 1);
            Class<Property> propertyClass = getClass(type);
            Property property = propertyClass.getConstructor(BasicGameCallback.class).newInstance(callback);
            property.setScript(script);
            property.init();
            return property;
        } catch (ClassCastException e) {
            throw new PropertyInterfaceNotImplementedException(propertyName, type);
        } catch (InstantiationException e) {
            throw new AbstractPropertyTypeException(propertyName, type);
        } catch (NoSuchMethodException e) {
            throw new UndefinedConstructorException(propertyName, type);
        } catch (ClassNotFoundException e) {
            throw new UnknownPropertyClassException(propertyName, type);
        } catch (IllegalArgumentException e) {
            throw new UndefinedConstructorException(propertyName, type);
        } catch (IllegalAccessException e) {
            throw new UndefinedConstructorException(propertyName, type);
        } catch (ScriptException e) {
            throw new PropertyScriptException(propertyName, e);
        } catch (Throwable e) {
            throw new PropertyInstantiationException(propertyName, "unexpected exception occurred during construction", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> getClass(String className) throws ClassNotFoundException {
        return (Class<T>) Class.forName(className);
    }
}
