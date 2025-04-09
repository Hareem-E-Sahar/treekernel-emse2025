package rails.game;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import rails.algorithms.RevenueManager;
import rails.common.*;
import rails.common.parser.*;
import rails.game.action.*;
import rails.game.correct.*;
import rails.game.model.ModelObject;
import rails.game.move.*;
import rails.game.special.SpecialPropertyI;
import rails.game.special.SpecialTokenLay;
import rails.game.state.*;
import rails.util.GameFileIO;
import rails.util.Util;

/**
 * This class manages the playing rounds by supervising all implementations of
 * Round. Currently everything is hardcoded &agrave; la 1830.
 */
public class GameManager implements ConfigurableComponentI, GameManagerI {

    /** Version ID of the Save file header, as written in save() */
    private static final long saveFileHeaderVersionID = 3L;

    /**
     * Overall save file version ID, taking into account the version ID of the
     * action package.
     */
    public static final long saveFileVersionID = saveFileHeaderVersionID * PossibleAction.serialVersionUID;

    protected Class<? extends StockRound> stockRoundClass = StockRound.class;

    protected Class<? extends OperatingRound> operatingRoundClass = OperatingRound.class;

    protected Class<? extends ShareSellingRound> shareSellingRoundClass = ShareSellingRound.class;

    protected String gameUIManagerClassName = GuiDef.getDefaultClassName(GuiDef.ClassName.GAME_UI_MANAGER);

    protected String orUIManagerClassName = GuiDef.getDefaultClassName(GuiDef.ClassName.OR_UI_MANAGER);

    protected String gameStatusClassName = GuiDef.getDefaultClassName(GuiDef.ClassName.GAME_STATUS);

    protected String statusWindowClassName = GuiDef.getDefaultClassName(GuiDef.ClassName.STATUS_WINDOW);

    protected String orWindowClassName = GuiDef.getDefaultClassName(GuiDef.ClassName.OR_WINDOW);

    protected String startRoundWindowClassName = GuiDef.getDefaultClassName(GuiDef.ClassName.START_ROUND_WINDOW);

    protected PlayerManager playerManager;

    protected CompanyManagerI companyManager;

    protected PhaseManager phaseManager;

    protected TrainManager trainManager;

    protected StockMarketI stockMarket;

    protected MapManager mapManager;

    protected TileManager tileManager;

    protected RevenueManager revenueManager;

    protected Bank bank;

    protected Map<CorrectionType, CorrectionManagerI> correctionManagers = new HashMap<CorrectionType, CorrectionManagerI>();

    protected String gameName;

    protected Map<String, String> gameOptions;

    protected ArrayListState<Player> players;

    protected List<String> originalPlayerNamesList;

    protected int numberOfPlayers;

    protected State currentPlayer = new State("CurrentPlayer", Player.class);

    protected State priorityPlayer = new State("PriorityPlayer", Player.class);

    protected StringState[] playerNameModels;

    /** Map relating portfolio names and objects, to enable deserialization.
     * OBSOLETE since Rails 1.3.1, but still required to enable reading old saved files */
    protected Map<String, Portfolio> portfolioMap = new HashMap<String, Portfolio>();

    /** Map relating portfolio unique names and objects, to enable deserialization */
    protected Map<String, Portfolio> portfolioUniqueNameMap = new HashMap<String, Portfolio>();

    protected IntegerState playerCertificateLimit = new IntegerState("PlayerCertificateLimit", 0);

    protected int currentNumberOfOperatingRounds = 1;

    protected boolean skipFirstStockRound = false;

    protected boolean showCompositeORNumber = true;

    protected boolean forcedSellingCompanyDump = true;

    protected boolean gameEndsWithBankruptcy = false;

    protected int gameEndsWhenBankHasLessOrEqual = 0;

    protected boolean gameEndsAfterSetOfORs = true;

    protected boolean dynamicOperatingOrder = true;

    /** Will only be set during game reload */
    protected boolean reloading = false;

    protected EnumMap<GameDef.Parm, Object> gameParameters = new EnumMap<GameDef.Parm, Object>(GameDef.Parm.class);

    /**
     * Current round should not be set here but from within the Round classes.
     * This is because in some cases the round has already changed to another
     * one when the constructor terminates. Example: if the privates have not
     * been sold, it finishes by starting an Operating Round, which handles the
     * privates payout and then immediately starts a new Start Round.
     */
    protected State currentRound = new State("CurrentRound", Round.class);

    protected RoundI interruptedRound = null;

    /** Number of the current start round.  Currently only used in some 1835 variants. */
    protected IntegerState startRoundNumber = new IntegerState("StartRoundNumber", 0);

    protected IntegerState srNumber = new IntegerState("SRNumber");

    protected IntegerState absoluteORNumber = new IntegerState("AbsoluteORNUmber");

    protected IntegerState relativeORNumber = new IntegerState("RelativeORNumber");

    protected IntegerState numOfORs = new IntegerState("numOfORs");

    /** GameOver pending, a last OR or set of ORs must still be completed */
    protected BooleanState gameOverPending = new BooleanState("GameOverPending", false);

    /** GameOver is executed, no more moves */
    protected BooleanState gameOver = new BooleanState("GameOver", false);

    protected Boolean gameOverReportedUI = false;

    protected BooleanState endedByBankruptcy = new BooleanState("EndedByBankruptcy", false);

    /** UI display hints */
    protected GuiHints guiHints;

    /** Flags to be passed to the UI, aiding the layout definition */
    protected EnumMap<GuiDef.Parm, Boolean> guiParameters = new EnumMap<GuiDef.Parm, Boolean>(GuiDef.Parm.class);

    /**
     * Update:
     * NDC concept has been replaced by the origin singleton one.
     * Target design to be decided when distributed rails is being conceived
     * and developed.
     * 
     * Former design:
     * Map of GameManager instances.
     * Currently there can be only one instance, but in a possible
     * future multi-game server there may be several instances
     * running in parallel.
     *
     * <p>The reason for creating this map is the need to access
     * GameManager instances (or other common instances via the GM)
     * from many different classes, some of which
     * (like those in the move package) are many method calls away from
     * the actual GM.
     * <p>To prevent the need to pass GameManager instances or the keys to
     * this map around throughout the code, NDC is (mis-)used as the
     * mechanism to pass around a string key to each GM instance.
     * This is possible,because the server processes all player actions
     * in one thread. The key will be set in process(), which is where server
     * processing currently starts (in the future it will probably be moved
     * to the communication interface that will be added by then).
     * The key can be retrieved (via NDC.peek()) anywhere.
     * <p>
     * For now, the key is a fixed string, but that may change in the future.
     */
    protected static Map<String, GameManagerI> gameManagerMap = new HashMap<String, GameManagerI>();

    /**
     * The temporary fixed key to the currently single GameManager instance
     * in the GameManager map.
     * It will only be used inside the GM objects.
     * All other objects will access it via NDC.
     */
    public static final String GM_KEY = "01";

    public static final String GM_NAME = "GameManager";

    /**
     * The MoveSet stack is maintained to enable Undo and Redo throughout the game.
     */
    protected MoveStack moveStack = new MoveStack();

    /**
     * The DisplayBuffer instance collects messages to be displayed in the UI.
     */
    protected DisplayBuffer displayBuffer;

    /**
     * nextPlayerMessages collects all messages to be displayed to the next player
     */
    protected List<String> nextPlayerMessages = new ArrayList<String>();

    /**
     * The ReportBuffer collects messages to be shown in the Game Report.
     */
    protected ReportBuffer reportBuffer;

    protected String gmName;

    protected String gmKey;

    protected StartPacket startPacket;

    protected PossibleActions possibleActions = PossibleActions.getInstance();

    protected List<PossibleAction> executedActions = new ArrayList<PossibleAction>();

    /** Special properties that can be used by other players or companies
     * than just the owner (such as buyable bonus tokens as in 1856).
     */
    protected List<SpecialPropertyI> commonSpecialProperties = null;

    /** A List of available game options */
    protected List<GameOption> availableGameOptions = new ArrayList<GameOption>();

    /** indicates that the recoverySave already issued a warning, avoids displaying several warnings */
    protected boolean recoverySaveWarning = true;

    /** Flag to skip a subsequent Done action (if present) during reloading.
     * <br>This is a fix to maintain backwards compatibility when redundant
     * actions are skipped in new code versions (such as the bypassing of
     * a treasury trading step if it cannot be executed).
     * <br>This flag must be reset after processing <i>any</i> action (not just Done).
     */
    protected boolean skipNextDone = false;

    /** Step that must be in effect to do an actual Done skip during reloading.
     * <br> This is to ensure that Done actions in different OR steps are
     * considered separately.
     */
    protected GameDef.OrStep skippedStep = null;

    protected Map<String, Object> objectStorage = new HashMap<String, Object>();

    protected Map<String, Integer> storageIds = new HashMap<String, Integer>();

    protected static Logger log = Logger.getLogger(GameManager.class.getPackage().getName());

    public BooleanState playerOrderHasChanged = new BooleanState("PlayerOrderHasChange", false);

    public GameManager() {
        gmName = GM_NAME;
        gmKey = GM_KEY;
        gameManagerMap.put(GM_KEY, this);
        displayBuffer = new DisplayBuffer();
        reportBuffer = new ReportBuffer();
        guiHints = new GuiHints();
    }

    public void configureFromXML(Tag tag) throws ConfigurationException {
        Tag gameTag = tag.getChild("Game");
        if (gameTag == null) throw new ConfigurationException("No Game tag specified in GameManager tag");
        gameName = gameTag.getAttributeAsString("name");
        if (gameName == null) throw new ConfigurationException("No name specified in Game tag");
        gameOptions = tag.getGameOptions();
        initGameParameters();
        GameOption option;
        String optionName, optionType, optionValues, optionDefault;
        String optionNameParameters;
        String[] optionParameters;
        List<Tag> optionTags = tag.getChildren("GameOption");
        if (optionTags != null) {
            for (Tag optionTag : optionTags) {
                optionName = optionTag.getAttributeAsString("name");
                if (optionName == null) throw new ConfigurationException("GameOption without name");
                optionParameters = null;
                optionNameParameters = optionTag.getAttributeAsString("parm");
                if (optionNameParameters != null) {
                    optionParameters = optionNameParameters.split(",");
                }
                optionName = GameOption.constructParametrisedName(optionName, optionParameters);
                if (gameOptions.containsKey(optionName)) continue;
                option = new GameOption(optionName, optionParameters);
                availableGameOptions.add(option);
                optionType = optionTag.getAttributeAsString("type");
                if (optionType != null) option.setType(optionType);
                optionValues = optionTag.getAttributeAsString("values");
                if (optionValues != null) option.setAllowedValues(optionValues.split(","));
                optionDefault = optionTag.getAttributeAsString("default", "");
                if (optionDefault != null) option.setDefaultValue(optionDefault);
                gameOptions.put(optionName, optionDefault);
            }
        }
        Tag gameParmTag = tag.getChild("GameParameters");
        if (gameParmTag != null) {
            Tag srTag = gameParmTag.getChild("StockRound");
            if (srTag != null) {
                String srClassName = srTag.getAttributeAsString("class", "rails.game.StockRound");
                try {
                    stockRoundClass = Class.forName(srClassName).asSubclass(StockRound.class);
                } catch (ClassNotFoundException e) {
                    throw new ConfigurationException("Cannot find class " + srClassName, e);
                }
                String stockRoundSequenceRuleString = srTag.getAttributeAsString("sequence");
                if (Util.hasValue(stockRoundSequenceRuleString)) {
                    if (stockRoundSequenceRuleString.equalsIgnoreCase("SellBuySell")) {
                        setGameParameter(GameDef.Parm.STOCK_ROUND_SEQUENCE, StockRound.SELL_BUY_SELL);
                    } else if (stockRoundSequenceRuleString.equalsIgnoreCase("SellBuy")) {
                        setGameParameter(GameDef.Parm.STOCK_ROUND_SEQUENCE, StockRound.SELL_BUY);
                    } else if (stockRoundSequenceRuleString.equalsIgnoreCase("SellBuyOrBuySell")) {
                        setGameParameter(GameDef.Parm.STOCK_ROUND_SEQUENCE, StockRound.SELL_BUY_OR_BUY_SELL);
                    }
                }
                skipFirstStockRound = srTag.getAttributeAsBoolean("skipFirst", skipFirstStockRound);
                for (String ruleTagName : srTag.getChildren().keySet()) {
                    if (ruleTagName.equals("NoSaleInFirstSR")) {
                        setGameParameter(GameDef.Parm.NO_SALE_IN_FIRST_SR, true);
                    } else if (ruleTagName.equals("NoSaleIfNotOperated")) {
                        setGameParameter(GameDef.Parm.NO_SALE_IF_NOT_OPERATED, true);
                    } else if (ruleTagName.equals("NoSaleOfJustBoughtShare")) {
                        setGameParameter(GameDef.Parm.NO_SALE_OF_JUST_BOUGHT_CERT, true);
                    }
                }
            }
            Tag orTag = gameParmTag.getChild("OperatingRound");
            if (orTag != null) {
                String orClassName = orTag.getAttributeAsString("class", "rails.game.OperatingRound");
                try {
                    operatingRoundClass = Class.forName(orClassName).asSubclass(OperatingRound.class);
                } catch (ClassNotFoundException e) {
                    throw new ConfigurationException("Cannot find class " + orClassName, e);
                }
                Tag orderTag = orTag.getChild("OperatingOrder");
                if (orderTag != null) {
                    dynamicOperatingOrder = orderTag.getAttributeAsBoolean("dynamic", dynamicOperatingOrder);
                }
                Tag emergencyTag = orTag.getChild("EmergencyTrainBuying");
                if (emergencyTag != null) {
                    setGameParameter(GameDef.Parm.EMERGENCY_MUST_BUY_CHEAPEST_TRAIN, emergencyTag.getAttributeAsBoolean("mustBuyCheapestTrain", GameDef.Parm.EMERGENCY_MUST_BUY_CHEAPEST_TRAIN.defaultValueAsBoolean()));
                    setGameParameter(GameDef.Parm.EMERGENCY_MAY_ALWAYS_BUY_NEW_TRAIN, emergencyTag.getAttributeAsBoolean("mayAlwaysBuyNewTrain", GameDef.Parm.EMERGENCY_MAY_ALWAYS_BUY_NEW_TRAIN.defaultValueAsBoolean()));
                    setGameParameter(GameDef.Parm.EMERGENCY_MAY_BUY_FROM_COMPANY, emergencyTag.getAttributeAsBoolean("mayBuyFromCompany", GameDef.Parm.EMERGENCY_MAY_BUY_FROM_COMPANY.defaultValueAsBoolean()));
                }
            }
            Tag ssrTag = gameParmTag.getChild("ShareSellingRound");
            if (ssrTag != null) {
                String ssrClassName = ssrTag.getAttributeAsString("class", "rails.game.ShareSellingRound");
                try {
                    shareSellingRoundClass = Class.forName(ssrClassName).asSubclass(ShareSellingRound.class);
                } catch (ClassNotFoundException e) {
                    throw new ConfigurationException("Cannot find class " + ssrClassName, e);
                }
            }
            Tag shareLimitTag = gameParmTag.getChild("PlayerShareLimit");
            if (shareLimitTag != null) {
                setGameParameter(GameDef.Parm.PLAYER_SHARE_LIMIT, shareLimitTag.getAttributeAsInteger("percentage", GameDef.Parm.PLAYER_SHARE_LIMIT.defaultValueAsInt()));
            }
            Tag poolLimitTag = gameParmTag.getChild("BankPoolShareLimit");
            if (poolLimitTag != null) {
                setGameParameter(GameDef.Parm.POOL_SHARE_LIMIT, shareLimitTag.getAttributeAsInteger("percentage", GameDef.Parm.POOL_SHARE_LIMIT.defaultValueAsInt()));
            }
            Tag treasuryLimitTag = gameParmTag.getChild("TreasuryShareLimit");
            if (treasuryLimitTag != null) {
                setGameParameter(GameDef.Parm.TREASURY_SHARE_LIMIT, shareLimitTag.getAttributeAsInteger("percentage", GameDef.Parm.TREASURY_SHARE_LIMIT.defaultValueAsInt()));
            }
        }
        Tag endOfGameTag = tag.getChild("EndOfGame");
        if (endOfGameTag != null) {
            Tag forcedSellingTag = endOfGameTag.getChild("ForcedSelling");
            if (forcedSellingTag != null) {
                forcedSellingCompanyDump = forcedSellingTag.getAttributeAsBoolean("CompanyDump", true);
            }
            if (endOfGameTag.getChild("Bankruptcy") != null) {
                gameEndsWithBankruptcy = true;
            }
            Tag bankBreaksTag = endOfGameTag.getChild("BankBreaks");
            if (bankBreaksTag != null) {
                gameEndsWhenBankHasLessOrEqual = bankBreaksTag.getAttributeAsInteger("limit", gameEndsWhenBankHasLessOrEqual);
                String attr = bankBreaksTag.getAttributeAsString("finish");
                if (attr.equalsIgnoreCase("setOfORs")) {
                    gameEndsAfterSetOfORs = true;
                } else if (attr.equalsIgnoreCase("currentOR")) {
                    gameEndsAfterSetOfORs = false;
                }
            }
        }
        Tag guiClassesTag = tag.getChild("GuiClasses");
        if (guiClassesTag != null) {
            Tag gameUIMgrTag = guiClassesTag.getChild("GameUIManager");
            if (gameUIMgrTag != null) {
                gameUIManagerClassName = gameUIMgrTag.getAttributeAsString("class", gameUIManagerClassName);
                canClassBeInstantiated(gameUIManagerClassName);
            }
            Tag orMgrTag = guiClassesTag.getChild("ORUIManager");
            if (orMgrTag != null) {
                orUIManagerClassName = orMgrTag.getAttributeAsString("class", orUIManagerClassName);
                canClassBeInstantiated(orUIManagerClassName);
            }
            Tag gameStatusTag = guiClassesTag.getChild("GameStatus");
            if (gameStatusTag != null) {
                gameStatusClassName = gameStatusTag.getAttributeAsString("class", gameStatusClassName);
                canClassBeInstantiated(gameStatusClassName);
            }
            Tag statusWindowTag = guiClassesTag.getChild("StatusWindow");
            if (statusWindowTag != null) {
                statusWindowClassName = statusWindowTag.getAttributeAsString("class", statusWindowClassName);
                canClassBeInstantiated(statusWindowClassName);
            }
            Tag orWindowTag = guiClassesTag.getChild("ORWindow");
            if (orWindowTag != null) {
                orWindowClassName = orWindowTag.getAttributeAsString("class", orWindowClassName);
                canClassBeInstantiated(orWindowClassName);
            }
            Tag startRoundWindowTag = guiClassesTag.getChild("StartRoundWindow");
            if (startRoundWindowTag != null) {
                startRoundWindowClassName = startRoundWindowTag.getAttributeAsString("class", startRoundWindowClassName);
                canClassBeInstantiated(startRoundWindowClassName);
            }
        }
    }

    public void finishConfiguration(GameManagerI gameManager) {
    }

    /** Check if a classname can be instantiated.
     * Throws a ConfiguratioNException if not.
     * @param className
     * @throws ConfigurationException
     */
    protected void canClassBeInstantiated(String className) throws ConfigurationException {
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("Cannot find class " + className, e);
        }
    }

    public void init(String gameName, PlayerManager playerManager, CompanyManagerI companyManager, PhaseManager phaseManager, TrainManager trainManager, StockMarketI stockMarket, MapManager mapManager, TileManager tileManager, RevenueManager revenueManager, Bank bank) {
        this.gameName = gameName;
        this.playerManager = playerManager;
        this.companyManager = companyManager;
        this.phaseManager = phaseManager;
        this.trainManager = trainManager;
        this.stockMarket = stockMarket;
        this.mapManager = mapManager;
        this.tileManager = tileManager;
        this.revenueManager = revenueManager;
        this.bank = bank;
        players = new ArrayListState<Player>("Players", playerManager.getPlayers());
        originalPlayerNamesList = playerManager.getPlayerNames();
        numberOfPlayers = players.size();
        priorityPlayer.setState(players.get(0));
        setPlayerCertificateLimit(playerManager.getInitialPlayerCertificateLimit());
        playerNameModels = new StringState[numberOfPlayers];
        for (int i = 0; i < numberOfPlayers; i++) {
            playerNameModels[i] = new StringState("Player_" + (i + 1), players.get(i).getName());
        }
        showCompositeORNumber = !"simple".equalsIgnoreCase(Config.get("or.number_format"));
    }

    public void startGame(Map<String, String> gameOptions) {
        this.gameOptions = gameOptions;
        setGuiParameters();
        if (startPacket == null) startPacket = companyManager.getStartPacket(StartPacket.DEFAULT_NAME);
        if (startPacket != null && !startPacket.areAllSold()) {
            startPacket.init(this);
            startStartRound();
        } else {
            startStockRound();
        }
        moveStack.enable();
    }

    protected void setGuiParameters() {
        for (PublicCompanyI company : companyManager.getAllPublicCompanies()) {
            if (company.hasParPrice()) guiParameters.put(GuiDef.Parm.HAS_ANY_PAR_PRICE, true);
            if (company.canBuyPrivates()) guiParameters.put(GuiDef.Parm.CAN_ANY_COMPANY_BUY_PRIVATES, true);
            if (company.canHoldOwnShares()) guiParameters.put(GuiDef.Parm.CAN_ANY_COMPANY_HOLD_OWN_SHARES, true);
            if (company.getMaxNumberOfLoans() != 0) guiParameters.put(GuiDef.Parm.HAS_ANY_COMPANY_LOANS, true);
        }
        loop: for (PrivateCompanyI company : companyManager.getAllPrivateCompanies()) {
            for (SpecialPropertyI sp : company.getSpecialProperties()) {
                if (sp instanceof SpecialTokenLay && ((SpecialTokenLay) sp).getToken() instanceof BonusToken) {
                    guiParameters.put(GuiDef.Parm.DO_BONUS_TOKENS_EXIST, true);
                    break loop;
                }
            }
        }
        if (GameOption.convertValueToBoolean(getGameOption("NoMapMode"))) {
            guiParameters.put(GuiDef.Parm.NO_MAP_MODE, true);
            guiParameters.put(GuiDef.Parm.ROUTE_HIGHLIGHT, false);
            guiParameters.put(GuiDef.Parm.REVENUE_SUGGEST, false);
        } else {
            if ("Highlight".equalsIgnoreCase(getGameOption("RouteAwareness"))) {
                guiParameters.put(GuiDef.Parm.ROUTE_HIGHLIGHT, true);
            }
            if ("Suggest".equalsIgnoreCase(getGameOption("RevenueCalculation"))) {
                guiParameters.put(GuiDef.Parm.REVENUE_SUGGEST, true);
            }
        }
    }

    private void initGameParameters() {
        for (GameDef.Parm parm : GameDef.Parm.values()) {
            gameParameters.put(parm, parm.defaultValue());
        }
    }

    /**
     * @return instance of GameManager
     */
    public static GameManagerI getInstance() {
        return gameManagerMap.get(GM_KEY);
    }

    public CompanyManagerI getCompanyManager() {
        return companyManager;
    }

    protected void setRound(RoundI round) {
        currentRound.set(round);
    }

    public void nextRound(RoundI round) {
        if (round instanceof StartRound) {
            if (startPacket != null && !startPacket.areAllSold()) {
                startOperatingRound(runIfStartPacketIsNotCompletelySold());
            } else if (skipFirstStockRound) {
                PhaseI currentPhase = phaseManager.getCurrentPhase();
                if (currentPhase.getNumberOfOperatingRounds() != numOfORs.intValue()) {
                    numOfORs.set(currentPhase.getNumberOfOperatingRounds());
                }
                log.info("Phase=" + currentPhase.getName() + " ORs=" + numOfORs);
                relativeORNumber.set(1);
                startOperatingRound(true);
            } else {
                startStockRound();
            }
        } else if (round instanceof StockRound) {
            PhaseI currentPhase = getCurrentPhase();
            if (currentPhase == null) log.error("Current Phase is null??", new Exception(""));
            numOfORs.set(currentPhase.getNumberOfOperatingRounds());
            log.info("Phase=" + currentPhase.getName() + " ORs=" + numOfORs);
            relativeORNumber.set(1);
            startOperatingRound(true);
        } else if (round instanceof OperatingRound) {
            if (gameOverPending.booleanValue() && !gameEndsAfterSetOfORs) {
                finishGame();
            } else if (relativeORNumber.add(1) <= numOfORs.intValue()) {
                startOperatingRound(true);
            } else if (startPacket != null && !startPacket.areAllSold()) {
                startStartRound();
            } else {
                if (gameOverPending.booleanValue() && gameEndsAfterSetOfORs) {
                    finishGame();
                } else {
                    ((OperatingRound) round).checkForeignSales();
                    startStockRound();
                }
            }
        }
    }

    /** Stub, to be overridden if companies can run before the Start Packet has been completely sold
     * (as in 1835).
     * @return true if companies can run regardless. Default false.
     */
    protected boolean runIfStartPacketIsNotCompletelySold() {
        return false;
    }

    protected void startStartRound() {
        String startRoundClassName = startPacket.getRoundClassName();
        Class<? extends StartRound> startRoundClass = null;
        try {
            startRoundClass = Class.forName(startRoundClassName).asSubclass(StartRound.class);
        } catch (Exception e) {
            log.fatal("Cannot find class " + startRoundClassName, e);
            System.exit(1);
        }
        StartRound startRound = createRound(startRoundClass);
        startRoundNumber.add(1);
        startRound.start();
    }

    protected void startStockRound() {
        StockRound sr = createRound(stockRoundClass);
        srNumber.add(1);
        sr.start();
    }

    protected void startOperatingRound(boolean operate) {
        log.debug("Operating round started with operate-flag=" + operate);
        OperatingRound or = createRound(operatingRoundClass);
        if (operate) absoluteORNumber.add(1);
        or.start();
    }

    protected <T extends RoundI> T createRound(Class<T> roundClass) {
        T round = null;
        try {
            Constructor<T> cons = roundClass.getConstructor(GameManagerI.class);
            round = cons.newInstance(this);
        } catch (Exception e) {
            log.fatal("Cannot instantiate class " + roundClass.getName(), e);
            System.exit(1);
        }
        setRound(round);
        return round;
    }

    protected <T extends RoundI, U extends RoundI> T createRound(Class<T> roundClass, U parentRound) {
        if (parentRound == null) {
            return createRound(roundClass);
        }
        T round = null;
        try {
            Constructor<T> cons = roundClass.getConstructor(GameManagerI.class, RoundI.class);
            round = cons.newInstance(this, parentRound);
        } catch (Exception e) {
            log.fatal("Cannot instantiate class " + roundClass.getName(), e);
            System.exit(1);
        }
        setRound(round);
        return round;
    }

    /** Stub, can be overridden in subclasses with actual actions */
    public void newPhaseChecks(RoundI round) {
    }

    public String getORId() {
        if (showCompositeORNumber) {
            return getCompositeORNumber();
        } else {
            return String.valueOf(absoluteORNumber.intValue());
        }
    }

    public int getAbsoluteORNumber() {
        return absoluteORNumber.intValue();
    }

    public String getCompositeORNumber() {
        return srNumber.intValue() + "." + relativeORNumber.intValue();
    }

    public int getRelativeORNumber() {
        return relativeORNumber.intValue();
    }

    public String getNumOfORs() {
        return numOfORs.getText();
    }

    public int getStartRoundNumber() {
        return startRoundNumber.intValue();
    }

    public int getSRNumber() {
        return srNumber.intValue();
    }

    public void startShareSellingRound(Player player, int cashToRaise, PublicCompanyI cashNeedingCompany, boolean problemDumpOtherCompanies) {
        interruptedRound = getCurrentRound();
        createRound(shareSellingRoundClass, interruptedRound).start(player, cashToRaise, cashNeedingCompany, !problemDumpOtherCompanies || forcedSellingCompanyDump);
    }

    public void startTreasuryShareTradingRound() {
        interruptedRound = getCurrentRound();
        createRound(TreasuryShareRound.class, interruptedRound).start();
    }

    public boolean process(PossibleAction action) {
        NDC.clear();
        NDC.push(GM_KEY);
        boolean result = true;
        DisplayBuffer.clear();
        guiHints.clearVisibilityHints();
        if (action instanceof NullAction && ((NullAction) action).getMode() == NullAction.START_GAME) {
            result = true;
        } else if (action != null) {
            action.setActed();
            result = false;
            String actionPlayerName = action.getPlayerName();
            String currentPlayerName = getCurrentPlayer().getName();
            if (!actionPlayerName.equals(currentPlayerName)) {
                DisplayBuffer.add(LocalText.getText("WrongPlayer", actionPlayerName, currentPlayerName));
                return false;
            }
            if (!possibleActions.validate(action)) {
                DisplayBuffer.add(LocalText.getText("ActionNotAllowed", action.toString()));
                return false;
            }
            for (; ; ) {
                if (action instanceof GameAction) {
                    GameAction gameAction = (GameAction) action;
                    result = processGameActions(gameAction);
                    if (result) break;
                }
                result = processCorrectionActions(action) || getCurrentRound().process(action);
                break;
            }
            if (result && !(action instanceof GameAction) && action.hasActed()) {
                new AddToList<PossibleAction>(executedActions, action, "ExecutedActions");
            }
        }
        possibleActions.clear();
        getCurrentRound().setPossibleActions();
        if (!isGameOver() && possibleActions.containsOnlyPass()) {
            result = process(possibleActions.getList().get(0));
        }
        if (action != null) {
            if (result && !(action instanceof GameAction) && action.hasActed()) {
                if (moveStack.isOpen()) moveStack.finish();
                recoverySave();
            } else {
                if (moveStack.isOpen()) moveStack.cancel();
            }
        }
        if (!isGameOver()) setCorrectionActions();
        if (moveStack.isUndoableByPlayer()) {
            possibleActions.add(new GameAction(GameAction.UNDO));
        }
        if (moveStack.isUndoableByManager()) {
            possibleActions.add(new GameAction(GameAction.FORCED_UNDO));
        }
        if (moveStack.isRedoable()) {
            possibleActions.add(new GameAction(GameAction.REDO));
        }
        for (PossibleAction pa : possibleActions.getList()) {
            log.debug(((Player) currentPlayer.get()).getName() + " may: " + pa.toString());
        }
        return result;
    }

    /**
     * Adds all Game actions
     * Examples are: undo/redo/corrections
     */
    private void setCorrectionActions() {
        for (CorrectionType ct : EnumSet.allOf(CorrectionType.class)) {
            CorrectionManagerI cm = getCorrectionManager(ct);
            if (cm.isActive()) {
                possibleActions.clear();
            }
        }
        for (CorrectionType ct : EnumSet.allOf(CorrectionType.class)) {
            CorrectionManagerI cm = getCorrectionManager(ct);
            possibleActions.addAll(cm.createCorrections());
        }
    }

    private boolean processCorrectionActions(PossibleAction a) {
        boolean result = false;
        if (a instanceof CorrectionAction) {
            CorrectionAction ca = (CorrectionAction) a;
            CorrectionType ct = ca.getCorrectionType();
            CorrectionManagerI cm = getCorrectionManager(ct);
            result = cm.executeCorrection(ca);
        }
        return result;
    }

    private boolean processGameActions(GameAction gameAction) {
        boolean result = false;
        int index = gameAction.getmoveStackIndex();
        switch(gameAction.getMode()) {
            case GameAction.SAVE:
                result = save(gameAction);
                break;
            case GameAction.RELOAD:
                result = reload(gameAction);
                break;
            case GameAction.UNDO:
                moveStack.undoMoveSet(false);
                result = true;
                break;
            case GameAction.FORCED_UNDO:
                if (index != -1) {
                    moveStack.gotoIndex(index);
                } else {
                    moveStack.undoMoveSet(true);
                }
                result = true;
                break;
            case GameAction.REDO:
                if (index != -1) {
                    moveStack.gotoIndex(index);
                } else {
                    moveStack.redoMoveSet();
                }
                result = true;
                break;
            case GameAction.EXPORT:
                result = export(gameAction);
                break;
        }
        return result;
    }

    public boolean processOnReload(PossibleAction action) throws Exception {
        DisplayBuffer.clear();
        if (gameName.equals("1856") && possibleActions.contains(RepayLoans.class) && (!possibleActions.contains(action.getClass()) || (action.getClass() == NullAction.class && ((NullAction) action).getMode() != NullAction.DONE))) {
            log.debug("Action DONE inserted");
            getCurrentRound().process(new NullAction(NullAction.DONE));
            possibleActions.clear();
            getCurrentRound().setPossibleActions();
            if (!isGameOver()) setCorrectionActions();
        }
        try {
            log.debug("Action (" + action.getPlayerName() + "): " + action);
            boolean doProcess = true;
            if (skipNextDone) {
                if (action instanceof NullAction && ((NullAction) action).getMode() == NullAction.DONE) {
                    if (currentRound.get() instanceof OperatingRound && ((OperatingRound) currentRound.get()).getStep() == skippedStep) {
                        doProcess = false;
                    }
                }
            }
            skipNextDone = false;
            skippedStep = null;
            if (doProcess && !processCorrectionActions(action) && !getCurrentRound().process(action)) {
                String msg = "Player " + action.getPlayerName() + "\'s action \"" + action.toString() + "\"\n  in " + getCurrentRound().getRoundName() + " is considered invalid by the game engine";
                log.error(msg);
                DisplayBuffer.add(msg);
                if (moveStack.isOpen()) moveStack.finish();
                return false;
            }
            possibleActions.clear();
            getCurrentRound().setPossibleActions();
            String playerName = getCurrentPlayer().getName();
            for (PossibleAction a : possibleActions.getList()) {
                log.debug(playerName + " may: " + a.toString());
            }
            if (!isGameOver()) setCorrectionActions();
        } catch (Exception e) {
            log.error("Error while reprocessing " + action.toString(), e);
            throw new Exception("Reload failure", e);
        }
        new AddToList<PossibleAction>(executedActions, action, "ExecutedActions");
        if (moveStack.isOpen()) moveStack.finish();
        log.debug("Turn: " + getCurrentPlayer().getName());
        return true;
    }

    /** allows callback from GameLoader */
    public void finishLoading() {
        guiHints.clearVisibilityHints();
    }

    /** recoverySave method
     * Uses filePath defined in save.recovery.filepath
     *  */
    protected void recoverySave() {
        if (Config.get("save.recovery.active", "yes").equalsIgnoreCase("no")) return;
        String filePath = Config.get("save.recovery.filepath", "18xx_autosave.rails");
        File tempFile = null;
        tempFile = new File(filePath + ".tmp");
        if (!save(tempFile, recoverySaveWarning, "RecoverySaveFailed")) {
            recoverySaveWarning = false;
            return;
        }
        File recoveryFile = null;
        boolean result = false;
        try {
            log.debug("Created temporary recovery file, path = " + tempFile.getPath());
            recoveryFile = new File(filePath);
            log.debug("Potential recovery filePath = " + recoveryFile.getPath());
            if (recoveryFile.exists()) {
                log.debug("Potential recovery filePath = " + recoveryFile.getPath());
                File backupFile = new File(filePath + ".bak");
                if (backupFile.exists()) backupFile.delete();
                recoveryFile.renameTo(backupFile);
                result = tempFile.renameTo(recoveryFile);
            } else {
                log.debug("Tries to rename temporary file");
                result = tempFile.renameTo(recoveryFile);
            }
        } catch (Exception e) {
            DisplayBuffer.add(LocalText.getText("RecoverySaveFailed", e.getMessage()));
            recoverySaveWarning = false;
            return;
        }
        if (result) {
            log.debug("Renamed to recovery file, path = " + recoveryFile.getPath());
            if (!recoverySaveWarning) {
                DisplayBuffer.add(LocalText.getText("RecoverySaveSuccessAgain"));
                recoverySaveWarning = true;
            }
        } else {
            if (recoverySaveWarning) {
                DisplayBuffer.add(LocalText.getText("RecoverySaveFailed", "file renaming not possible"));
                recoverySaveWarning = false;
            }
        }
    }

    protected boolean save(GameAction saveAction) {
        File file = new File(saveAction.getFilepath());
        return save(file, true, "SaveFailed");
    }

    protected boolean save(File file, boolean displayErrorMessage, String errorMessageKey) {
        GameFileIO gameSaver = new GameFileIO();
        gameSaver.initSave(saveFileVersionID, gameName, gameOptions, originalPlayerNamesList);
        gameSaver.setActions(executedActions);
        gameSaver.setComments(ReportBuffer.getCommentItems());
        return gameSaver.saveGame(file, displayErrorMessage, errorMessageKey);
    }

    /**
     * tries to reload the current game
     * executes the additional action(s)
     */
    protected boolean reload(GameAction reloadAction) {
        log.info("Reloading started");
        GameFileIO gameLoader = new GameFileIO();
        String filepath = reloadAction.getFilepath();
        gameLoader.loadGameData(filepath);
        try {
            gameLoader.loadActionsAndComments();
        } catch (ConfigurationException e) {
            log.fatal("Load failed", e);
            DisplayBuffer.add(LocalText.getText("LoadFailed", e.getMessage()));
        }
        log.debug("Starting to compare loaded actions");
        List<PossibleAction> savedActions = gameLoader.getActions();
        setReloading(true);
        if (savedActions.size() < executedActions.size()) {
            DisplayBuffer.add(LocalText.getText("LoadFailed", "loaded file has less actions than current game"));
            return true;
        }
        int index = 0;
        PossibleAction executedAction;
        try {
            for (PossibleAction savedAction : savedActions) {
                if (index < executedActions.size()) {
                    executedAction = executedActions.get(index);
                    if (!savedAction.equalsAsAction(executedAction)) {
                        DisplayBuffer.add(LocalText.getText("LoadFailed", "loaded action \"" + savedAction.toString() + "\"<br>   is not same as game action \"" + executedAction.toString() + "\""));
                        return true;
                    }
                } else {
                    if (index == executedActions.size()) {
                        log.info("Finished comparing old actions, starting to process new actions");
                    }
                    if (!processOnReload(savedAction)) {
                        log.error("Reload interrupted");
                        DisplayBuffer.add(LocalText.getText("LoadFailed", " loaded action \"" + savedAction.toString() + "\" is invalid"));
                        break;
                    }
                }
                index++;
            }
        } catch (Exception e) {
            log.error("Reload failed", e);
            DisplayBuffer.add(LocalText.getText("LoadFailed", e.getMessage()));
            return true;
        }
        setReloading(false);
        finishLoading();
        ReportBuffer.setCommentItems(gameLoader.getComments());
        log.info("Reloading finished");
        return true;
    }

    protected boolean export(GameAction exportAction) {
        String filename = exportAction.getFilepath();
        boolean result = false;
        try {
            PrintWriter pw = new PrintWriter(filename);
            MapHex[][] allHexes = mapManager.getHexes();
            for (MapHex[] hexRow : allHexes) for (MapHex hex : hexRow) if (hex != null) {
                pw.println(hex.getName() + "," + hex.getCurrentTile().getExternalId() + "," + hex.getCurrentTileRotation() + "," + hex.getOrientationName(hex.getCurrentTileRotation()));
            }
            pw.close();
            result = true;
        } catch (IOException e) {
            log.error("Save failed", e);
            DisplayBuffer.add(LocalText.getText("SaveFailed", e.getMessage()));
        }
        return result;
    }

    public void finishShareSellingRound() {
        setRound(interruptedRound);
        guiHints.setCurrentRoundType(interruptedRound.getClass());
        guiHints.setVisibilityHint(GuiDef.Panel.STOCK_MARKET, false);
        guiHints.setActivePanel(GuiDef.Panel.MAP);
        getCurrentRound().resume();
    }

    public void finishTreasuryShareRound() {
        setRound(interruptedRound);
        guiHints.setCurrentRoundType(interruptedRound.getClass());
        guiHints.setVisibilityHint(GuiDef.Panel.STOCK_MARKET, false);
        guiHints.setActivePanel(GuiDef.Panel.MAP);
        ((OperatingRound) getCurrentRound()).nextStep();
    }

    public void registerBankruptcy() {
        endedByBankruptcy.set(true);
        String message = LocalText.getText("PlayerIsBankrupt", getCurrentPlayer().getName());
        ReportBuffer.add(message);
        DisplayBuffer.add(message);
        if (gameEndsWithBankruptcy) {
            finishGame();
        } else {
            processBankruptcy();
        }
    }

    protected void processBankruptcy() {
    }

    public void registerBrokenBank() {
        gameOverPending.set(true);
        ReportBuffer.add(LocalText.getText("BankIsBrokenReportText"));
        String msgContinue;
        if (gameEndsAfterSetOfORs) msgContinue = LocalText.getText("gameOverPlaySetOfORs"); else msgContinue = LocalText.getText("gameOverPlayOnlyOR");
        String msg = LocalText.getText("BankIsBrokenDisplayText", msgContinue);
        DisplayBuffer.add(msg);
        addToNextPlayerMessages(msg, true);
    }

    public void registerMaxedSharePrice(PublicCompanyI company, StockSpaceI space) {
        gameOverPending.set(true);
        ReportBuffer.add(LocalText.getText("MaxedSharePriceReportText", company.getName(), Bank.format(space.getPrice())));
        String msgContinue;
        if (gameEndsAfterSetOfORs) msgContinue = LocalText.getText("gameOverPlaySetOfORs"); else msgContinue = LocalText.getText("gameOverPlayOnlyOR");
        String msg = LocalText.getText("MaxedSharePriceDisplayText", company.getName(), Bank.format(space.getPrice()), msgContinue);
        DisplayBuffer.add(msg);
        addToNextPlayerMessages(msg, true);
    }

    protected void finishGame() {
        gameOver.set(true);
        String message = LocalText.getText("GameOver");
        ReportBuffer.add(message);
        DisplayBuffer.add(message);
        ReportBuffer.add("");
        List<String> gameReport = getGameReport();
        for (String s : gameReport) ReportBuffer.add(s);
        setGameOverReportedUI(false);
        createRound(EndOfGameRound.class);
    }

    public boolean isDynamicOperatingOrder() {
        return dynamicOperatingOrder;
    }

    public boolean isGameOver() {
        return gameOver.booleanValue();
    }

    public ModelObject getGameOverPendingModel() {
        return gameOverPending;
    }

    public void setGameOverReportedUI(boolean b) {
        gameOverReportedUI = b;
    }

    public boolean getGameOverReportedUI() {
        return (gameOverReportedUI);
    }

    public List<String> getGameReport() {
        List<String> b = new ArrayList<String>();
        List<Player> rankedPlayers = new ArrayList<Player>();
        for (Player player : getPlayers()) {
            rankedPlayers.add(player);
        }
        Collections.sort(rankedPlayers);
        Player winner = rankedPlayers.get(0);
        b.add(LocalText.getText("EoGWinner") + winner.getName() + "!");
        b.add(LocalText.getText("EoGFinalRanking") + " :");
        int i = 0;
        for (Player p : rankedPlayers) {
            b.add((++i) + ". " + Bank.format(p.getWorth()) + " " + p.getName());
        }
        return b;
    }

    public RoundI getCurrentRound() {
        return (RoundI) currentRound.get();
    }

    public ModelObject getCurrentRoundModel() {
        return currentRound;
    }

    public ModelObject getCurrentPlayerModel() {
        return currentPlayer;
    }

    public int getCurrentPlayerIndex() {
        return getCurrentPlayer().getIndex();
    }

    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        currentPlayerIndex = currentPlayerIndex % numberOfPlayers;
        setCurrentPlayer(players.get(currentPlayerIndex));
    }

    public void setCurrentPlayer(Player player) {
        if ((Player) currentPlayer.get() != player && !nextPlayerMessages.isEmpty()) {
            DisplayBuffer.add(LocalText.getText("NextPlayerMessage", getCurrentPlayer().getName()));
            for (String s : nextPlayerMessages) DisplayBuffer.add(s);
            nextPlayerMessages.clear();
        }
        currentPlayer.set(player);
    }

    public void setPriorityPlayer() {
        int priorityPlayerIndex = (getCurrentPlayer().getIndex() + 1) % numberOfPlayers;
        setPriorityPlayer(players.get(priorityPlayerIndex));
    }

    public void setPriorityPlayer(Player player) {
        priorityPlayer.set(player);
        log.debug("Priority player set to " + player.getIndex() + " " + player.getName());
    }

    public Player getPriorityPlayer() {
        return (Player) priorityPlayer.get();
    }

    public Player getCurrentPlayer() {
        return (Player) currentPlayer.get();
    }

    public List<Player> getPlayers() {
        return players.viewList();
    }

    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public int getPlayerCertificateLimit(Player player) {
        return playerCertificateLimit.intValue();
    }

    public void setPlayerCertificateLimit(int newLimit) {
        playerCertificateLimit.set(newLimit);
    }

    public IntegerState getPlayerCertificateLimitModel() {
        return playerCertificateLimit;
    }

    public List<PublicCompanyI> getAllPublicCompanies() {
        return companyManager.getAllPublicCompanies();
    }

    public List<PrivateCompanyI> getAllPrivateCompanies() {
        return companyManager.getAllPrivateCompanies();
    }

    public Player getPlayerByIndex(int index) {
        return players.get(index % numberOfPlayers);
    }

    public void setNextPlayer() {
        int currentPlayerIndex = getCurrentPlayerIndex();
        do {
            currentPlayerIndex = ++currentPlayerIndex % numberOfPlayers;
        } while (players.get(currentPlayerIndex).isBankrupt());
        setCurrentPlayerIndex(currentPlayerIndex);
    }

    public void addPortfolio(Portfolio portfolio) {
        portfolioMap.put(portfolio.getName(), portfolio);
        portfolioUniqueNameMap.put(portfolio.getUniqueName(), portfolio);
    }

    public Portfolio getPortfolioByName(String name) {
        return portfolioMap.get(name);
    }

    public Portfolio getPortfolioByUniqueName(String name) {
        return portfolioUniqueNameMap.get(name);
    }

    public StartPacket getStartPacket() {
        return startPacket;
    }

    public PhaseI getCurrentPhase() {
        return phaseManager.getCurrentPhase();
    }

    public PhaseManager getPhaseManager() {
        return phaseManager;
    }

    public String getGameName() {
        return gameName;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public TrainManager getTrainManager() {
        return trainManager;
    }

    public StockMarketI getStockMarket() {
        return stockMarket;
    }

    public MapManager getMapManager() {
        return mapManager;
    }

    public TileManager getTileManager() {
        return tileManager;
    }

    /**
     * The RevenueManager is optional, thus a null reference might be returned
     */
    public RevenueManager getRevenueManager() {
        return revenueManager;
    }

    public Bank getBank() {
        return bank;
    }

    public String getGameOption(String key) {
        if (Util.hasValue(System.getProperty(key))) {
            return System.getProperty(key);
        } else {
            return gameOptions.get(key);
        }
    }

    public void initialiseNewPhase(PhaseI phase) {
        ReportBuffer.add(LocalText.getText("StartOfPhase", phase.getName()));
        phase.activate();
        if (phase.doPrivatesClose()) {
            companyManager.closeAllPrivates();
        }
    }

    public String getHelp() {
        return getCurrentRound().getHelp();
    }

    public boolean canAnyCompanyHoldShares() {
        return (Boolean) getGuiParameter(GuiDef.Parm.CAN_ANY_COMPANY_HOLD_OWN_SHARES);
    }

    public String getClassName(GuiDef.ClassName key) {
        switch(key) {
            case GAME_UI_MANAGER:
                return gameUIManagerClassName;
            case OR_UI_MANAGER:
                return orUIManagerClassName;
            case STATUS_WINDOW:
                return statusWindowClassName;
            case START_ROUND_WINDOW:
                return startRoundWindowClassName;
            case GAME_STATUS:
                return gameStatusClassName;
            default:
                return "";
        }
    }

    public Object getGuiParameter(GuiDef.Parm key) {
        if (guiParameters.containsKey(key)) {
            return guiParameters.get(key);
        } else {
            return false;
        }
    }

    public void setGuiParameter(GuiDef.Parm key, boolean value) {
        guiParameters.put(key, value);
    }

    public void setGameParameter(GameDef.Parm key, Object value) {
        gameParameters.put(key, value);
    }

    public Object getGameParameter(GameDef.Parm key) {
        if (gameParameters.containsKey(key)) {
            return gameParameters.get(key);
        } else {
            return false;
        }
    }

    public RoundI getInterruptedRound() {
        return interruptedRound;
    }

    /**
     * Add an object.
     *
     * @param object The object to add.
     * @return True if successful.
     */
    public boolean addObject(Moveable object, int[] position) {
        if (object instanceof SpecialPropertyI) {
            SpecialPropertyI sp = (SpecialPropertyI) object;
            sp.setHolder(null);
            return addSpecialProperty(sp, position == null ? -1 : position[0]);
        } else {
            return false;
        }
    }

    public int[] getListIndex(Moveable object) {
        if (object instanceof SpecialPropertyI) {
            return new int[] { commonSpecialProperties.indexOf(object) };
        } else {
            return Moveable.AT_END;
        }
    }

    /**
     * Remove an object.
     *
     * @param object The object to remove.
     * @return True if successful.
     */
    public boolean removeObject(Moveable object) {
        if (object instanceof SpecialPropertyI) {
            return removeSpecialProperty((SpecialPropertyI) object);
        } else {
            return false;
        }
    }

    public boolean addSpecialProperty(SpecialPropertyI property, int position) {
        if (commonSpecialProperties == null) {
            commonSpecialProperties = new ArrayList<SpecialPropertyI>(2);
        }
        return Util.addToList(commonSpecialProperties, property, position);
    }

    /**
     * Remove a special property.
     *
     * @param property The special property object to remove.
     * @return True if successful.
     */
    public boolean removeSpecialProperty(SpecialPropertyI property) {
        if (commonSpecialProperties != null) {
            return commonSpecialProperties.remove(property);
        }
        return false;
    }

    public List<SpecialPropertyI> getCommonSpecialProperties() {
        return getSpecialProperties(null, false);
    }

    @SuppressWarnings("unchecked")
    public <T extends SpecialPropertyI> List<T> getSpecialProperties(Class<T> clazz, boolean includeExercised) {
        List<T> result = new ArrayList<T>();
        if (commonSpecialProperties != null) {
            for (SpecialPropertyI sp : commonSpecialProperties) {
                if ((clazz == null || clazz.isAssignableFrom(sp.getClass())) && sp.isExecutionable() && (!sp.isExercised() || includeExercised)) {
                    result.add((T) sp);
                }
            }
        }
        return result;
    }

    /**
     * Get name of the GM instance. Currently, the name is fixed,
     * but that will change whenever a multi-game server will be implemented.
     */
    public String getName() {
        return gmName;
    }

    public String getGMKey() {
        return gmKey;
    }

    public MoveStack getMoveStack() {
        return moveStack;
    }

    public DisplayBuffer getDisplayBuffer() {
        return displayBuffer;
    }

    public void addToNextPlayerMessages(String s, boolean undoable) {
        if (undoable) new AddToList<String>(nextPlayerMessages, s, "nextPlayerMessages"); else nextPlayerMessages.add(s);
    }

    public ReportBuffer getReportBuffer() {
        return reportBuffer;
    }

    public GuiHints getUIHints() {
        return guiHints;
    }

    public CorrectionManagerI getCorrectionManager(CorrectionType ct) {
        CorrectionManagerI cm = correctionManagers.get(ct);
        if (cm == null) {
            cm = ct.newCorrectionManager(this);
            correctionManagers.put(ct, cm);
            log.debug("Added CorrectionManager for " + ct);
        }
        return cm;
    }

    /** Return a list of companies in operation order.
     * <p>Note that, unlike Round.setOperatingCompanies(), this method does <b>not</b> check
     * if the companies are actualy allowed to operate. One purpose is to check for upping the
     * share price at the end of an SR un sucn a way, that the token order gets preserved.
     * @return
     */
    public List<PublicCompanyI> getCompaniesInRunningOrder() {
        Map<Integer, PublicCompanyI> operatingCompanies = new TreeMap<Integer, PublicCompanyI>();
        StockSpaceI space;
        int key;
        int minorNo = 0;
        for (PublicCompanyI company : companyManager.getAllPublicCompanies()) {
            if (company.hasStockPrice() && company.hasStarted()) {
                space = company.getCurrentSpace();
                key = 1000000 * (999 - space.getPrice()) + 10000 * (99 - space.getColumn()) + 100 * space.getRow() + space.getStackPosition(company);
            } else {
                key = ++minorNo;
            }
            operatingCompanies.put(new Integer(key), company);
        }
        return new ArrayList<PublicCompanyI>(operatingCompanies.values());
    }

    public boolean isReloading() {
        return reloading;
    }

    public void setReloading(boolean reloading) {
        this.reloading = reloading;
    }

    public void setSkipDone(GameDef.OrStep step) {
        skipNextDone = true;
        skippedStep = step;
    }

    /**
     *
     *@param ascending Boolean to determine if the playerlist will be sorted in ascending or descending order based on their cash
     *@return Returns the player at index position 0 that is either the player with the most or least cash depending on sort order.
     */
    public Player reorderPlayersByCash(boolean ascending) {
        final boolean _ascending = ascending;
        List<Player> reorderedPlayers = new ArrayList<Player>(players.viewList());
        Collections.sort(reorderedPlayers, new Comparator<Player>() {

            public int compare(Player p1, Player p2) {
                return _ascending ? p1.getCash() - p2.getCash() : p2.getCash() - p1.getCash();
            }
        });
        players.clear();
        Player player;
        for (int i = 0; i < reorderedPlayers.size(); i++) {
            player = reorderedPlayers.get(i);
            players.add(player);
            player.setIndex(i);
            playerNameModels[i].set(player.getName());
            log.debug("New player " + i + " is " + player.getName() + " (cash=" + Bank.format(player.getCash()) + ")");
        }
        return this.players.get(0);
    }

    public StringState getPlayerNameModel(int index) {
        return playerNameModels[index];
    }

    public void resetStorage() {
        objectStorage = new HashMap<String, Object>();
        storageIds = new HashMap<String, Integer>();
    }

    public int storeObject(String typeName, Object object) {
        Integer id = storageIds.get(typeName);
        if (id == null) id = 0;
        objectStorage.put(typeName + id, object);
        storageIds.put(typeName, id + 1);
        return id;
    }

    public Object retrieveObject(String typeName, int id) {
        return objectStorage.get(typeName + id);
    }

    /** Process an action triggered by a phase change. */
    public void processPhaseAction(String name, String value) {
        getCurrentRound().processPhaseAction(name, value);
    }
}
