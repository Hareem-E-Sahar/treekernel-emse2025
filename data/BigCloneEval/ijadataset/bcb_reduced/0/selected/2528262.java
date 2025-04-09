package scoreboard.controller;

import java.lang.reflect.Constructor;
import java.util.TreeSet;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import scoreboard.config.GameMode;
import scoreboard.model.GameModelFactory;
import scoreboard.model.GameModelIfc;
import scoreboard.view.HtmlView;

/**
 * Provides the components for a game to run the score board server. This class
 * will be provided with parameterization for counting various kinds of games in
 * a future version.
 * <p>
 * The current implementation only provides fixed instances that are created
 * statically. On each call to the same method, the same object is returned.
 */
public class GameFactory {

    private static final String PACKAGE = "scoreboard.controller.";

    private static final Logger log = Logger.getLogger(GameFactory.class);

    private GameModelIfc model;

    private HtmlView view;

    private CommandProcessorIfc commandProcessor;

    /**
     * @param gameMode
     *            The game mode this factory works for.
     */
    public GameFactory(GameMode gameMode) {
        this.model = new GameModelFactory().createGameModel(gameMode.getName(), gameMode.getModelSuffix(), gameMode.isSetsIncreaseable());
        this.view = new HtmlView(gameMode.getTemplateFile(), gameMode.getStyleFile());
        this.commandProcessor = createCommandProcessor(gameMode);
    }

    /**
     * The model instance for MVC strategy.
     */
    public GameModelIfc model() {
        return model;
    }

    /**
     * The view instance for MVC strategy.
     */
    public HtmlView view() {
        return view;
    }

    /**
     * The command processor for executing controller commands.
     */
    public CommandProcessorIfc getCommandProcessor() {
        return commandProcessor;
    }

    /**
     * Builds the appropriate command processor for the active game.
     * 
     * @param gameMode
     *            The game mode to create processors for.
     * @return The command processor for gameName.
     */
    private CommandProcessorIfc createCommandProcessor(GameMode gameMode) {
        CommandProcessors comProcs = new CommandProcessors();
        comProcs.add(new UndoProcessor());
        comProcs.add(new ServerStateProcessor());
        comProcs.add(new TeamsProcessor());
        try {
            for (CommandProcessor cp : newCommandProcessors(gameMode.getLogic())) {
                comProcs.addAll(cp.explode().values());
            }
        } catch (JSONException e) {
            throw new RuntimeException("Failed instantiating game logic.", e);
        }
        CommandProcessorComposite result = new CommandProcessorComposite(comProcs);
        log.debug(result);
        return result;
    }

    /**
     * Creates a new {@link CommandProcessor} by JSON definition. Types and
     * number of parameters must match one of the command processor's public
     * constructors.
     * 
     * @param array
     *            Selects the command processor to instantiate and provides the
     *            parameters for it's constructor. <br>
     *            Required structure:
     *            <code>[&lt;command_processor_name>, &lt;param1>, &lt;param2>, ...]</code>
     * <br>
     *            The command processor's name must be written without package
     *            name and without "Processor" suffix. If a <code>param</code>
     *            is a {@link JSONArray} itself, this method is called
     *            recursively.
     * @return The created command processor instance.
     * @throws JSONException
     */
    protected CommandProcessor newCommandProcessor(JSONArray array) throws JSONException {
        if (array.length() == 0) {
            return new NopProcessor();
        } else if (array.get(0) instanceof JSONArray) {
            return new CommandProcessorComposite(newCommandProcessors(array));
        } else {
            Class<?>[] paramTypes = new Class<?>[array.length() - 1];
            Object[] params = new Object[array.length() - 1];
            for (int i = 0; i < array.length() - 1; i++) {
                if (array.get(i + 1) instanceof JSONArray) {
                    paramTypes[i] = CommandProcessor.class;
                    params[i] = newCommandProcessor(array.getJSONArray(i + 1));
                } else {
                    paramTypes[i] = array.get(i + 1).getClass();
                    params[i] = array.get(i + 1);
                }
            }
            String cpClassName = PACKAGE + array.getString(0) + "Processor";
            try {
                Class<?> cpClass = Class.forName(cpClassName);
                Constructor<?> constructor = cpClass.getConstructor(paramTypes);
                return (CommandProcessor) constructor.newInstance(params);
            } catch (Exception e) {
                throw new RuntimeException("Could not instantiate " + cpClassName, e);
            }
        }
    }

    /**
     * @param array
     * @return
     * @throws JSONException
     */
    private CommandProcessors newCommandProcessors(JSONArray array) throws JSONException {
        CommandProcessors comProcs = new CommandProcessors();
        for (int i = 0; i < array.length(); i++) {
            comProcs.add(newCommandProcessor(array.getJSONArray(i)));
        }
        return comProcs;
    }

    private static class CommandProcessors extends TreeSet<CommandProcessor> {

        private static final long serialVersionUID = -4926106106661213579L;
    }
}
