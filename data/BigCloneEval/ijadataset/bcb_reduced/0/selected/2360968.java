package edu.cmu.sphinx.research.parallel;

import edu.cmu.sphinx.knowledge.acoustic.AcousticModel;
import edu.cmu.sphinx.knowledge.acoustic.Context;
import edu.cmu.sphinx.knowledge.acoustic.HMM;
import edu.cmu.sphinx.knowledge.acoustic.HMMState;
import edu.cmu.sphinx.knowledge.acoustic.HMMStateArc;
import edu.cmu.sphinx.knowledge.acoustic.LeftRightContext;
import edu.cmu.sphinx.knowledge.acoustic.Unit;
import edu.cmu.sphinx.knowledge.language.LanguageModel;
import edu.cmu.sphinx.decoder.linguist.AlternativeState;
import edu.cmu.sphinx.decoder.linguist.Color;
import edu.cmu.sphinx.decoder.linguist.Grammar;
import edu.cmu.sphinx.decoder.linguist.GrammarArc;
import edu.cmu.sphinx.decoder.linguist.GrammarNode;
import edu.cmu.sphinx.decoder.linguist.GrammarState;
import edu.cmu.sphinx.decoder.linguist.GrammarWord;
import edu.cmu.sphinx.decoder.linguist.HMMStateState;
import edu.cmu.sphinx.decoder.linguist.Linguist;
import edu.cmu.sphinx.knowledge.dictionary.Pronunciation;
import edu.cmu.sphinx.decoder.linguist.PronunciationState;
import edu.cmu.sphinx.decoder.linguist.SentenceHMMState;
import edu.cmu.sphinx.decoder.linguist.SentenceHMMStateArc;
import edu.cmu.sphinx.decoder.linguist.UnitState;
import edu.cmu.sphinx.decoder.linguist.WordState;
import edu.cmu.sphinx.util.LogMath;
import edu.cmu.sphinx.util.SphinxProperties;
import edu.cmu.sphinx.util.Timer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Constructs a SentenceHMM that is capable of decoding multiple
 * feature streams in parallel.
 */
public class ParallelLinguist implements edu.cmu.sphinx.decoder.linguist.Linguist {

    private static final String PROP_PREFIX = "edu.cmu.sphinx.research.parallel.ParallelLinguist.";

    private static final String PROP_ADD_SELF_LOOP_WORD_END_SIL = PROP_PREFIX + "addSelfLoopWordEndSilence";

    private static final String PROP_TIE_LEVEL = PROP_PREFIX + "tieLevel";

    private static final String PROP_TOKEN_STACK_CAPACITY = PROP_PREFIX + "tokenStackCapacity";

    private static final int PROP_TOKEN_STACK_CAPACITY_DEFAULT = 0;

    private static final double PROP_SILENCE_INSERTION_PROBABILITY_DEFAULT = 1.0;

    private static final double PROP_UNIT_INSERTION_PROBABILITY_DEFAULT = 1.0;

    private static final double PROP_WORD_INSERTION_PROBABILITY_DEFAULT = 1.0;

    private String context;

    private edu.cmu.sphinx.decoder.linguist.SentenceHMMState initialState;

    private LanguageModel languageModel;

    private AcousticModel[] acousticModels;

    private LogMath logMath;

    private String tieLevel;

    private double silenceInsertionProbability;

    private double unitInsertionProbability;

    private double wordInsertionProbability;

    private boolean addSelfLoopWordEndSilence;

    private int tokenStackCapacity;

    private Set allStates;

    /**
     * Initializes this ParallelLinguist, which means creating the 
     * SentenceHMM with parallel acoustic models.
     *
     * @param context the context to associate this linguist with
     * @param languageModel the languageModel for this linguist
     * @param grammar the grammar for this linguist
     * @param model this is not used in the ParallelLinguist, since
     *    we might have more than one acoustic models.
     */
    public void initialize(String context, LanguageModel languageModel, edu.cmu.sphinx.decoder.linguist.Grammar grammar, AcousticModel[] models) {
        this.context = context;
        this.languageModel = languageModel;
        this.logMath = LogMath.getLogMath(context);
        this.acousticModels = models;
        this.allStates = new HashSet();
        System.out.println("ParallelLinguist: using " + models.length + " acoustic models");
        SphinxProperties props = SphinxProperties.getSphinxProperties(context);
        silenceInsertionProbability = logMath.linearToLog(props.getDouble(edu.cmu.sphinx.decoder.linguist.Linguist.PROP_SILENCE_INSERTION_PROBABILITY, PROP_SILENCE_INSERTION_PROBABILITY_DEFAULT));
        unitInsertionProbability = logMath.linearToLog(props.getDouble(edu.cmu.sphinx.decoder.linguist.Linguist.PROP_UNIT_INSERTION_PROBABILITY, PROP_UNIT_INSERTION_PROBABILITY_DEFAULT));
        wordInsertionProbability = logMath.linearToLog(props.getDouble(edu.cmu.sphinx.decoder.linguist.Linguist.PROP_WORD_INSERTION_PROBABILITY, PROP_WORD_INSERTION_PROBABILITY_DEFAULT));
        addSelfLoopWordEndSilence = props.getBoolean(PROP_ADD_SELF_LOOP_WORD_END_SIL, true);
        tieLevel = props.getString(PROP_TIE_LEVEL, "word");
        tokenStackCapacity = props.getInt(PROP_TOKEN_STACK_CAPACITY, PROP_TOKEN_STACK_CAPACITY_DEFAULT);
        compileGrammar(grammar);
        System.out.println("ParallelLinguist total states: " + allStates.size());
    }

    /**
     * Returns the initial SentenceHMMState.
     *
     * @return the initial SentenceHMMState
     */
    public edu.cmu.sphinx.decoder.linguist.SentenceHMMState getInitialState() {
        return initialState;
    }

    /**
     * Called before a recognition.
     */
    public void start() {
        for (Iterator i = allStates.iterator(); i.hasNext(); ) {
            edu.cmu.sphinx.decoder.linguist.SentenceHMMState state = (edu.cmu.sphinx.decoder.linguist.SentenceHMMState) i.next();
            state.clear();
        }
    }

    /**
     * Called after a recognition.
     */
    public void stop() {
    }

    /**
     * Returns the language model for this ParallelLinguist.
     *
     * @return the language model (or null if there is none)
     */
    public LanguageModel getLanguageModel() {
        return languageModel;
    }

    /**
     * Compiles the given Grammar object into a SentenceHMM.
     *
     * @param grammar the Grammar object to compile
     */
    private void compileGrammar(edu.cmu.sphinx.decoder.linguist.Grammar grammar) {
        Timer compileTimer = Timer.getTimer(context, "compileGrammar");
        compileTimer.start();
        Map compiledNodes = new HashMap();
        edu.cmu.sphinx.decoder.linguist.GrammarNode firstGrammarNode = grammar.getInitialNode();
        this.initialState = compileGrammarNode(firstGrammarNode, compiledNodes);
        compileTimer.stop();
        assert this.initialState != null;
    }

    /**
     * Returns a GrammarState for the given GrammarNode.
     *
     * @param grammarNode the GrammarNode to return a GrammarState for
     */
    private edu.cmu.sphinx.decoder.linguist.GrammarState getGrammarState(edu.cmu.sphinx.decoder.linguist.GrammarNode grammarNode) {
        edu.cmu.sphinx.decoder.linguist.GrammarState grammarState = new edu.cmu.sphinx.decoder.linguist.GrammarState(grammarNode);
        return grammarState;
    }

    /**
     * Attach one state (dest) to another state (src).
     *
     * @param src the source state
     * @param dest the destination state
     * @param acousticProbability the acoustic probability of the transition
     * @param languageProbability the language probability of the transition
     * @param insertionProbability the insertion probability of the transition
     */
    private void attachState(edu.cmu.sphinx.decoder.linguist.SentenceHMMState src, edu.cmu.sphinx.decoder.linguist.SentenceHMMState dest, double acousticProbability, double languageProbability, double insertionProbability) {
        src.connect(new edu.cmu.sphinx.decoder.linguist.SentenceHMMStateArc(dest, (float) acousticProbability, (float) languageProbability, (float) insertionProbability));
        allStates.add(src);
        allStates.add(dest);
    }

    /**
     * Compile the given GrammarNode and its successors into a series
     * of SentenceHMMStates.
     *
     * @param grammarNode the grammarNode to compile
     * @param compiledNodes a mapping of the compiled GrammarNodes
     *    and initial GrammarStates
     *
     * @return the first SentenceHMMState after compiling the GrammarNode
     *    into SentenceHMMStates
     */
    private edu.cmu.sphinx.decoder.linguist.SentenceHMMState compileGrammarNode(edu.cmu.sphinx.decoder.linguist.GrammarNode grammarNode, Map compiledNodes) {
        edu.cmu.sphinx.decoder.linguist.GrammarState firstState = getGrammarState(grammarNode);
        allStates.add(firstState);
        compiledNodes.put(grammarNode, firstState);
        edu.cmu.sphinx.decoder.linguist.SentenceHMMState lastState = expandGrammarState(firstState);
        for (int i = 0; i < grammarNode.getSuccessors().length; i++) {
            edu.cmu.sphinx.decoder.linguist.GrammarArc arc = grammarNode.getSuccessors()[i];
            edu.cmu.sphinx.decoder.linguist.GrammarNode nextNode = arc.getGrammarNode();
            edu.cmu.sphinx.decoder.linguist.SentenceHMMState nextFirstState = null;
            if (compiledNodes.containsKey(nextNode)) {
                nextFirstState = (edu.cmu.sphinx.decoder.linguist.SentenceHMMState) compiledNodes.get(nextNode);
            } else {
                nextFirstState = compileGrammarNode(nextNode, compiledNodes);
            }
            attachState(lastState, nextFirstState, logMath.getLogOne(), arc.getProbability(), logMath.getLogOne());
        }
        return firstState;
    }

    /**
     * Expands the given GrammarState into the full set of SentenceHMMStates
     * of AlternativeStates, WordStates, PronunciationStates, 
     * UnitStates, and HMMStateStates.
     *
     * @param grammarState the GrammarState to expand
     *
     * @return the last state after expanding the given GrammarState
     */
    private edu.cmu.sphinx.decoder.linguist.SentenceHMMState expandGrammarState(edu.cmu.sphinx.decoder.linguist.GrammarState grammarState) {
        edu.cmu.sphinx.decoder.linguist.GrammarWord[][] alternatives = grammarState.getGrammarNode().getAlternatives();
        if (alternatives.length == 0) {
            return grammarState;
        } else {
            edu.cmu.sphinx.decoder.linguist.SentenceHMMState endGrammarState = new CombineState(grammarState, 0);
            for (int i = 0; i < alternatives.length; i++) {
                edu.cmu.sphinx.decoder.linguist.AlternativeState alternativeState = new edu.cmu.sphinx.decoder.linguist.AlternativeState(grammarState, i);
                attachState(grammarState, alternativeState, logMath.getLogOne(), logMath.getLogOne() - logMath.linearToLog(alternatives.length), logMath.getLogOne());
                edu.cmu.sphinx.decoder.linguist.SentenceHMMState lastState = expandAlternative(alternativeState);
                if (alternatives.length == 1) {
                    endGrammarState = lastState;
                } else {
                    attachState(lastState, endGrammarState, logMath.getLogOne(), logMath.getLogOne(), logMath.getLogOne());
                }
            }
            return endGrammarState;
        }
    }

    /**
     * Expands the given AlternativeState into the set of associated
     * WordStates, PronunciationStates, UnitStates, and HMMStateStates.
     * 
     * @param state the AlternativeState to expand
     *
     * @return the last SentenceHMMState from the expansion
     */
    private edu.cmu.sphinx.decoder.linguist.SentenceHMMState expandAlternative(edu.cmu.sphinx.decoder.linguist.AlternativeState state) {
        edu.cmu.sphinx.decoder.linguist.GrammarWord alternative[] = state.getAlternative();
        edu.cmu.sphinx.decoder.linguist.SentenceHMMState lastState = state;
        for (int i = 0; i < alternative.length; i++) {
            edu.cmu.sphinx.decoder.linguist.WordState wordState = new edu.cmu.sphinx.decoder.linguist.WordState(state, i);
            double insertionProbability = wordInsertionProbability;
            if (wordState.getWord().isSilence()) {
                insertionProbability = silenceInsertionProbability;
            }
            attachState(lastState, wordState, logMath.getLogOne(), logMath.getLogOne(), insertionProbability);
            lastState = expandWord(wordState);
        }
        return lastState;
    }

    /**
     * Expands the given WordState into the set of associated
     * PronunciationStates, UnitStates, and HMMStateStates.
     *
     * @param wordState the WordState to expand
     *
     * @return the last SentenceHMMState from the expansion
     */
    private edu.cmu.sphinx.decoder.linguist.SentenceHMMState expandWord(edu.cmu.sphinx.decoder.linguist.WordState wordState) {
        edu.cmu.sphinx.decoder.linguist.GrammarWord word = wordState.getWord();
        Pronunciation[] pronunciations = word.getPronunciations();
        edu.cmu.sphinx.decoder.linguist.SentenceHMMState endWordState = new CombineState(wordState.getParent(), wordState.getWhich());
        for (int i = 0; i < pronunciations.length; i++) {
            edu.cmu.sphinx.decoder.linguist.PronunciationState pronunciationState = new edu.cmu.sphinx.decoder.linguist.PronunciationState(wordState, i);
            attachState(wordState, pronunciationState, logMath.getLogOne(), logMath.getLogOne() - logMath.linearToLog(pronunciations.length), logMath.getLogOne());
            edu.cmu.sphinx.decoder.linguist.SentenceHMMState lastState = expandPronunciation(pronunciationState);
            if (pronunciations.length == 1) {
                endWordState = lastState;
            } else {
                attachState(lastState, endWordState, logMath.getLogOne(), logMath.getLogOne(), logMath.getLogOne());
            }
        }
        return endWordState;
    }

    /**
     * Expands the given PronunciationState into the set of associated
     * UnitStates and HMMStateStates.
     *
     * @param pronunciationState the PronunciationState to expand
     *
     * @return the last SentenceHMMState from the expansion
     */
    private edu.cmu.sphinx.decoder.linguist.SentenceHMMState expandPronunciation(edu.cmu.sphinx.decoder.linguist.PronunciationState state) {
        Pronunciation pronunciation = state.getPronunciation();
        Unit[] units = pronunciation.getUnits();
        edu.cmu.sphinx.decoder.linguist.SentenceHMMState lastState = state;
        for (int i = 0; i < units.length; i++) {
            edu.cmu.sphinx.decoder.linguist.UnitState unitState = null;
            if (i == 0 || i == (units.length - 1)) {
                unitState = new edu.cmu.sphinx.decoder.linguist.UnitState(state, i, units[i]);
            } else {
                Unit[] leftContext = new Unit[1];
                Unit[] rightContext = new Unit[1];
                leftContext[0] = units[i - 1];
                rightContext[0] = units[i + 1];
                Context context = LeftRightContext.get(leftContext, rightContext);
                Unit unit = new Unit(units[i].getName(), units[i].isFiller(), context);
                unitState = new edu.cmu.sphinx.decoder.linguist.UnitState(state, i, unit);
            }
            attachState(lastState, unitState, logMath.getLogOne(), logMath.getLogOne(), unitInsertionProbability);
            lastState = expandUnit(unitState);
            if (unitState.getUnit().isSilence()) {
                attachState(lastState, unitState, logMath.getLogOne(), logMath.getLogOne(), logMath.getLogOne());
            }
        }
        Unit lastUnit = units[units.length - 1];
        if (addSelfLoopWordEndSilence && !lastUnit.isSilence()) {
            addLoopSilence(lastState, state);
        }
        return lastState;
    }

    /**
     * Adds a self-looping silence to the given SentenceHMMState.
     * The resulting states look like:
     * <code>
     *           ----------------
     *          |                |
     *          V                |
     *         -- U - H - H - H -
     *        /                  |
     *       /                   |
     *   state <-----------------
     *
     * </code>
     *
     * @param state the SentenceHMMState to add the looping silence to
     */
    private void addLoopSilence(edu.cmu.sphinx.decoder.linguist.SentenceHMMState state, edu.cmu.sphinx.decoder.linguist.PronunciationState pronunciationState) {
        int which = pronunciationState.getPronunciation().getUnits().length;
        edu.cmu.sphinx.decoder.linguist.UnitState unitState = new edu.cmu.sphinx.decoder.linguist.UnitState(pronunciationState, which, Unit.SILENCE);
        attachState(state, unitState, logMath.getLogOne(), logMath.getLogOne(), silenceInsertionProbability);
        edu.cmu.sphinx.decoder.linguist.SentenceHMMState lastSilenceState = expandUnit(unitState);
        attachState(lastSilenceState, unitState, logMath.getLogOne(), logMath.getLogOne(), logMath.getLogOne());
        attachState(lastSilenceState, state, logMath.getLogOne(), logMath.getLogOne(), logMath.getLogOne());
    }

    /**
     * Expands the given UnitState into the set of associated
     * HMMStateStates.
     *
     * @param unitState the UnitState to expand
     *
     * @return the last SentenceHMMState from the expansion
     */
    private edu.cmu.sphinx.decoder.linguist.SentenceHMMState expandUnit(edu.cmu.sphinx.decoder.linguist.UnitState unitState) {
        edu.cmu.sphinx.decoder.linguist.SentenceHMMState combineState = new CombineState(unitState.getParent(), unitState.getWhich());
        for (int i = 0; i < acousticModels.length; i++) {
            HMM hmm = acousticModels[i].lookupNearestHMM(unitState.getUnit(), unitState.getPosition(), false);
            ParallelHMMStateState firstHMMState = new ParallelHMMStateState(unitState, acousticModels[i].getName(), hmm.getInitialState(), tokenStackCapacity);
            firstHMMState.setColor(Color.GREEN);
            attachState(unitState, firstHMMState, logMath.getLogOne(), logMath.getLogOne(), logMath.getLogOne());
            Map hmmStates = new HashMap();
            hmmStates.put(firstHMMState.getHMMState(), firstHMMState);
            edu.cmu.sphinx.decoder.linguist.SentenceHMMState lastState = expandHMMTree(firstHMMState, acousticModels[i].getName(), hmmStates);
            attachState(lastState, combineState, logMath.getLogOne(), logMath.getLogOne(), logMath.getLogOne());
        }
        return combineState;
    }

    /**
     * Expands the given HMM tree into the full set of HMMStateStates.
     *
     * @param hmmStateState the first state of the HMM tree
     * @param modelName the name of the acoustic model behind this HMM tree
     * @param expandedStates the map of HMMStateStates
     *
     * @return the last state of the expanded tree
     */
    private edu.cmu.sphinx.decoder.linguist.SentenceHMMState expandHMMTree(ParallelHMMStateState hmmStateState, String modelName, Map expandedStates) {
        edu.cmu.sphinx.decoder.linguist.SentenceHMMState lastState = hmmStateState;
        HMMState hmmState = hmmStateState.getHMMState();
        HMMStateArc[] arcs = hmmState.getSuccessors();
        for (int i = 0; i < arcs.length; i++) {
            HMMState nextHmmState = arcs[i].getHMMState();
            if (nextHmmState == hmmState) {
                attachState(hmmStateState, hmmStateState, logMath.linearToLog(arcs[i].getProbability()), logMath.getLogOne(), logMath.getLogOne());
                lastState = hmmStateState;
            } else {
                ParallelHMMStateState nextState = null;
                if (expandedStates.containsKey(nextHmmState)) {
                    nextState = (ParallelHMMStateState) expandedStates.get(nextHmmState);
                } else {
                    nextState = new ParallelHMMStateState(hmmStateState.getParent(), modelName, nextHmmState, tokenStackCapacity);
                    expandedStates.put(nextHmmState, nextState);
                }
                nextState.setColor(Color.GREEN);
                attachState(hmmStateState, nextState, logMath.linearToLog(arcs[i].getProbability()), logMath.getLogOne(), logMath.getLogOne());
                lastState = expandHMMTree(nextState, modelName, expandedStates);
            }
        }
        return lastState;
    }
}
