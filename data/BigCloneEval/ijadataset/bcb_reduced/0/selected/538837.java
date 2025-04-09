package org.vizzini.example.kenken;

import org.vizzini.VizziniRuntimeException;
import org.vizzini.game.IAgent;
import org.vizzini.game.IAgentCollection;
import org.vizzini.game.IEnvironment;
import org.vizzini.game.IGameState;
import org.vizzini.game.IPosition;
import org.vizzini.game.ITeamCollection;
import org.vizzini.game.IToken;
import org.vizzini.game.ITokenCollection;
import org.vizzini.game.IntegerPosition;
import org.vizzini.game.action.IAction;
import org.vizzini.game.boardgame.DefaultGridBoard;
import org.vizzini.game.boardgame.IGridBoard;
import org.vizzini.game.event.IStateListener;
import org.vizzini.game.event.StateManager;
import org.vizzini.util.IProvider;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Provides an environment for KenKen.
 *
 * @author   Jeffrey M. Thompson
 * @version  v0.4
 * @since    v0.4
 */
public class Environment implements IGridBoard {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Cages. */
    private final Set<Cage> _cages = new TreeSet<Cage>();

    /** Environment delegate. */
    private final IGridBoard _environmentDelegate;

    /**
     * Construct this object with the given parameters.
     *
     * @param  agentCollection          Agent collection.
     * @param  initialTokenProviders    List of initial token providers.
     * @param  teamCollection           Team collection.
     * @param  tokenCollectionProvider  Token collection provider.
     * @param  fileCount                File count.
     * @param  rankCount                Rank count.
     * @param  levelCount               Level count.
     *
     * @since  v0.4
     */
    public Environment(IAgentCollection agentCollection, List<IProvider<IToken>> initialTokenProviders, ITeamCollection teamCollection, IProvider<ITokenCollection> tokenCollectionProvider, int fileCount, int rankCount, int levelCount) {
        _environmentDelegate = new DefaultGridBoard(agentCollection, initialTokenProviders, teamCollection, tokenCollectionProvider, fileCount, rankCount, levelCount);
        ((DefaultGridBoard) _environmentDelegate).setDimensionResetPermitted(true);
    }

    /**
     * Construct this object with the given parameters.
     *
     * @param  owner                    Owner.
     * @param  agentCollection          Agent collection.
     * @param  initialTokenProviders    List of initial token providers.
     * @param  teamCollection           Team collection.
     * @param  tokenCollectionProvider  Token collection provider.
     * @param  fileCount                File count.
     * @param  rankCount                Rank count.
     * @param  levelCount               Level count.
     *
     * @since  v0.4
     */
    public Environment(IEnvironment owner, IAgentCollection agentCollection, List<IProvider<IToken>> initialTokenProviders, ITeamCollection teamCollection, IProvider<ITokenCollection> tokenCollectionProvider, int fileCount, int rankCount, int levelCount) {
        _environmentDelegate = new DefaultGridBoard(owner, agentCollection, initialTokenProviders, teamCollection, tokenCollectionProvider, fileCount, rankCount, levelCount);
        ((DefaultGridBoard) _environmentDelegate).setDimensionResetPermitted(true);
    }

    /**
     * @see  org.vizzini.game.IEnvironment#addStateListener(org.vizzini.game.event.IStateListener)
     */
    public void addStateListener(IStateListener listener) {
        _environmentDelegate.addStateListener(listener);
    }

    /**
     * @see  org.vizzini.game.boardgame.IGridBoard#agentTokenCount(org.vizzini.game.IAgent,
     *       int, int, int, int, int, int)
     */
    public int agentTokenCount(IAgent agent, int startF, int startR, int startL, int df, int dr, int dl) {
        return _environmentDelegate.agentTokenCount(agent, startF, startR, startL, df, dr, dl);
    }

    /**
     * @see  org.vizzini.game.boardgame.IGridBoard#clearActionManager()
     */
    public void clearActionManager() {
        _environmentDelegate.clearActionManager();
    }

    /**
     * Clear all values.
     *
     * @since  v0.4
     */
    public void clearAllValues() {
        ITokenCollection tokenCollection = getTokenCollection();
        Iterator<IToken> iter = tokenCollection.iterator();
        while (iter.hasNext()) {
            Token token = (Token) iter.next();
            token.setLocked(false);
            token.setValue(0);
        }
        clearActionManager();
        getStateManager().fireStateChange(this);
    }

    /**
     * @see  org.vizzini.game.IEnvironment#clone()
     */
    @Override
    public Object clone() {
        return _environmentDelegate.clone();
    }

    /**
     * Configure this object using the given parameter.
     *
     * @param   puzzleDescriptor  Puzzle descriptor.
     *
     * @throws  InstantiationException  if there is an instantiation problem.
     * @throws  IllegalAccessException  if there is an illegal access.
     *
     * @since   v0.4
     */
    public void configure(PuzzleDescriptor puzzleDescriptor) throws InstantiationException, IllegalAccessException {
        _cages.clear();
        setFiringStateChanges(false);
        clearActionManager();
        int size = puzzleDescriptor.getSize();
        _cages.addAll(puzzleDescriptor.getCages());
        setDimensions(size, size, 1);
        setFiringStateChanges(true);
        getStateManager().fireStateChange(this);
        reset();
    }

    /**
     * @see  org.vizzini.game.boardgame.IGridBoard#coordsToIndex(int, int, int)
     */
    public int coordsToIndex(int file, int rank, int level) {
        return _environmentDelegate.coordsToIndex(file, rank, level);
    }

    /**
     * @see  org.vizzini.game.IEnvironment#createInitialTokens()
     */
    public ITokenCollection createInitialTokens() throws InstantiationException, IllegalAccessException {
        return _environmentDelegate.createInitialTokens();
    }

    /**
     * Fill values.
     *
     * @since  v0.4
     */
    public void fillValues() {
        ITokenCollection tokenCollection = getTokenCollection();
        Iterator<IToken> iter = tokenCollection.iterator();
        int size = getMaxDimension();
        while (iter.hasNext()) {
            Token token = (Token) iter.next();
            IntegerPosition position = (IntegerPosition) token.getPosition();
            int newValue = ((position.getX() + position.getY()) % size) + 1;
            token.setLocked(false);
            token.setValue(newValue);
        }
        clearActionManager();
        getStateManager().fireStateChange(this);
    }

    /**
     * @see  org.vizzini.game.IEnvironment#getAgentCollection()
     */
    public IAgentCollection getAgentCollection() {
        return _environmentDelegate.getAgentCollection();
    }

    /**
     * @return  the cages
     */
    public Set<Cage> getCages() {
        return _cages;
    }

    /**
     * @see  org.vizzini.game.boardgame.IGridBoard#getCellCount()
     */
    public int getCellCount() {
        return _environmentDelegate.getCellCount();
    }

    /**
     * @see  org.vizzini.game.boardgame.IGridBoard#getFileCount()
     */
    public int getFileCount() {
        return _environmentDelegate.getFileCount();
    }

    /**
     * @see  org.vizzini.game.IEnvironment#getInitialTokenProviders()
     */
    public List<IProvider<IToken>> getInitialTokenProviders() {
        return _environmentDelegate.getInitialTokenProviders();
    }

    /**
     * @see  org.vizzini.game.boardgame.IGridBoard#getLastAgent()
     */
    public IAgent getLastAgent() {
        return _environmentDelegate.getLastAgent();
    }

    /**
     * @see  org.vizzini.game.boardgame.IGridBoard#getLevelCount()
     */
    public int getLevelCount() {
        return _environmentDelegate.getLevelCount();
    }

    /**
     * @see  org.vizzini.game.boardgame.IGridBoard#getMaxDimension()
     */
    public int getMaxDimension() {
        return _environmentDelegate.getMaxDimension();
    }

    /**
     * @see  org.vizzini.game.boardgame.IGridBoard#getRankCount()
     */
    public int getRankCount() {
        return _environmentDelegate.getRankCount();
    }

    /**
     * @see  org.vizzini.game.IEnvironment#getStateManager()
     */
    public StateManager getStateManager() {
        return _environmentDelegate.getStateManager();
    }

    /**
     * @see  org.vizzini.game.IEnvironment#getTeamCollection()
     */
    public ITeamCollection getTeamCollection() {
        return _environmentDelegate.getTeamCollection();
    }

    /**
     * @see  org.vizzini.game.IEnvironment#getTokenCollection()
     */
    public ITokenCollection getTokenCollection() {
        return _environmentDelegate.getTokenCollection();
    }

    /**
     * @see  org.vizzini.game.IEnvironment#getTokenCollectionProvider()
     */
    public IProvider<ITokenCollection> getTokenCollectionProvider() {
        return _environmentDelegate.getTokenCollectionProvider();
    }

    /**
     * @see  org.vizzini.game.IEnvironment#getTurnNumber()
     */
    public long getTurnNumber() {
        return _environmentDelegate.getTurnNumber();
    }

    /**
     * @see  org.vizzini.game.IEnvironment#incrementTurnNumber()
     */
    public void incrementTurnNumber() {
        _environmentDelegate.incrementTurnNumber();
    }

    /**
     * @see  org.vizzini.game.boardgame.IGridBoard#indexToFile(int)
     */
    public int indexToFile(int index) {
        return _environmentDelegate.indexToFile(index);
    }

    /**
     * @see  org.vizzini.game.boardgame.IGridBoard#indexToLevel(int)
     */
    public int indexToLevel(int index) {
        return _environmentDelegate.indexToLevel(index);
    }

    /**
     * @see  org.vizzini.game.boardgame.IGridBoard#indexToPosition(int)
     */
    public IntegerPosition indexToPosition(int index) {
        return _environmentDelegate.indexToPosition(index);
    }

    /**
     * @see  org.vizzini.game.boardgame.IGridBoard#indexToRank(int)
     */
    public int indexToRank(int index) {
        return _environmentDelegate.indexToRank(index);
    }

    /**
     * @see  org.vizzini.game.boardgame.IGridBoard#is3D()
     */
    public boolean is3D() {
        return _environmentDelegate.is3D();
    }

    /**
     * @see  org.vizzini.game.boardgame.IGridBoard#isActionHistoryUsed()
     */
    public boolean isActionHistoryUsed() {
        return _environmentDelegate.isActionHistoryUsed();
    }

    /**
     * @see  org.vizzini.game.IEnvironment#isFiringStateChanges()
     */
    public boolean isFiringStateChanges() {
        return _environmentDelegate.isFiringStateChanges();
    }

    /**
     * @see  org.vizzini.game.boardgame.IGridBoard#isPositionUsable(org.vizzini.game.IntegerPosition)
     */
    public boolean isPositionUsable(IntegerPosition position) {
        return _environmentDelegate.isPositionUsable(position);
    }

    /**
     * @see  org.vizzini.game.boardgame.IGridBoard#isPositionUsable(int, int,
     *       int)
     */
    public boolean isPositionUsable(int file, int rank, int level) {
        return _environmentDelegate.isPositionUsable(file, rank, level);
    }

    /**
     * @see  org.vizzini.game.boardgame.IGridBoard#isRedoAllowed()
     */
    public boolean isRedoAllowed() {
        return _environmentDelegate.isRedoAllowed();
    }

    /**
     * @see  org.vizzini.game.boardgame.IGridBoard#isUndoAllowed()
     */
    public boolean isUndoAllowed() {
        return _environmentDelegate.isUndoAllowed();
    }

    /**
     * @see  org.vizzini.game.IEnvironment#open(org.vizzini.game.IGameState)
     */
    public void open(IGameState gameState) {
        _environmentDelegate.open(gameState);
    }

    /**
     * @see  org.vizzini.game.IEnvironment#performAction(org.vizzini.game.action.IAction)
     */
    public void performAction(IAction action) {
        _environmentDelegate.performAction(action);
    }

    /**
     * @see  org.vizzini.game.IEnvironment#performActions(java.util.List)
     */
    public void performActions(List<IAction> actions) {
        _environmentDelegate.performActions(actions);
    }

    /**
     * @see  org.vizzini.game.boardgame.IGridBoard#positionToIndex(org.vizzini.game.IntegerPosition)
     */
    public int positionToIndex(IntegerPosition position) {
        return _environmentDelegate.positionToIndex(position);
    }

    /**
     * @see  org.vizzini.game.IEnvironment#removeStateListener(org.vizzini.game.event.IStateListener)
     */
    public void removeStateListener(IStateListener listener) {
        _environmentDelegate.removeStateListener(listener);
    }

    /**
     * @see  org.vizzini.game.IEnvironment#reset()
     */
    public void reset() {
        _environmentDelegate.reset();
        ITokenCollection tokenCollection = getTokenCollection();
        tokenCollection.clear();
        int size = getFileCount();
        String name = "0";
        int value = 0;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                IntegerPosition position = IntegerPosition.get(x, y);
                try {
                    Constructor<Token> constructor = Token.class.getConstructor(new Class[] { IPosition.class, String.class, int.class });
                    Token token = constructor.newInstance(new Object[] { position, name, value });
                    tokenCollection.add(token);
                } catch (Exception e) {
                    throw new VizziniRuntimeException(e);
                }
            }
        }
    }

    /**
     * @see  org.vizzini.game.boardgame.IGridBoard#setActionHistoryUsed(boolean)
     */
    public void setActionHistoryUsed(boolean isActionHistoryUsed) {
        _environmentDelegate.setActionHistoryUsed(isActionHistoryUsed);
    }

    /**
     * Set all tokens locked state.
     *
     * @param  isLocked  Flag indicating whether the tokens are locked.
     *
     * @since  v0.4
     */
    public void setAllCellsLocked(boolean isLocked) {
        ITokenCollection tokenCollection = getTokenCollection();
        for (IToken token0 : tokenCollection) {
            Token token = (Token) token0;
            token.setLocked(isLocked);
        }
    }

    /**
     * @see  org.vizzini.game.boardgame.IGridBoard#setDimensions(int, int, int)
     */
    public void setDimensions(int fileCount, int rankCount, int levelCount) {
        _environmentDelegate.setDimensions(fileCount, rankCount, levelCount);
    }

    /**
     * @see  org.vizzini.game.IEnvironment#setFiringStateChanges(boolean)
     */
    public void setFiringStateChanges(boolean isFiring) {
        _environmentDelegate.setFiringStateChanges(isFiring);
    }

    /**
     * @see  org.vizzini.game.IEnvironment#setTurnNumber(long)
     */
    public void setTurnNumber(long turnNumber) {
        _environmentDelegate.setTurnNumber(turnNumber);
    }

    /**
     * @see  org.vizzini.game.IEnvironment#undoAction(org.vizzini.game.action.IAction)
     */
    public void undoAction(IAction action) {
        _environmentDelegate.undoAction(action);
    }

    /**
     * @see  org.vizzini.game.IEnvironment#update(long)
     */
    public void update(long deltaTime) {
        _environmentDelegate.update(deltaTime);
    }
}
