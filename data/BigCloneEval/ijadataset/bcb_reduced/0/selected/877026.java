package org.TMSIM.TestData;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.TMSIM.Exceptions.TapeOutofBoundsException;
import org.TMSIM.Model.BaseConfiguration;
import org.TMSIM.Model.State;
import org.TMSIM.Model.Transition;
import org.TMSIM.Model.TransitionCollection;
import org.TMSIM.Model.TuringMachine;

/**
 * A set of functions to generate testdata in the programm´s internal<br/>
 * structure, and to provide some random operations.
 * Creation Date: 06.08.2010
 * @author Christoph Prybila <christoph@prybila.at>
 */
public final class TestDataGenerator {

    private TestDataGenerator() {
    }

    private static boolean debug = false;

    private static TransitionCollection transitionCollection;

    private static ArrayList<State> states;

    private static ArrayList marked;

    private static boolean removed = false;

    /**
     * Generates a random TuringMachine, the Array bounds of the baseConfiguration<br/>
     * are generated at random.
     * @param startChar name of the first state to generate.<br/>
     * Each state is the indexState of one TransitionArray.
     * @param random new instance of Java Random.
     * @param maxCountArray max. count of arrays to generate.
     * @param maxCountTransition max. count of Transitions in each<br/>
     * array to generate.
     * @param finiteAutomate determines if the TuringMachine should be generated<br/>
     * as a FiniteAutomate.
     * @param alphabetIT alphabet of the Input Tape
     * @param alphabetWT alphabet of the Working Tape
     * @param maxLengthIT max. InputTape´s length of the baseConfiguration<br/>
     * (startSymbolIT and endSymbolIT are added afterwards)
     * @param maxLengthWT max. WorkingTape´s length of the baseConfiguration<br/>
     * (startSymbolWT is added afterwards)
     * @return a random TuringMachine.
     */
    public static TuringMachine generateTestTuringMachine(char startChar, Random random, int maxCountArray, int maxCountTransition, boolean finiteAutomate, String alphabetIT, String alphabetWT, int maxLengthIT, int maxLengthWT) {
        int lengthIT = random.nextInt(maxLengthIT);
        int lengthWT = random.nextInt(maxLengthWT);
        int ItHead = random.nextInt(lengthIT + 2);
        int WtHead = random.nextInt(lengthWT + 1);
        return generateTestTuringMachine(startChar, new Random(), maxCountArray, maxCountTransition, finiteAutomate, alphabetIT, alphabetWT, lengthIT, lengthWT, ItHead, WtHead);
    }

    /**
     * Generates a random TuringMachine, the Array bounds of the baseConfiguration<br/>
     * are set via parameters.
     * @param startChar name of the first state to generate.<br/>
     * Each state is the indexState of one TransitionArray.
     * @param random new instance of Java Random.
     * @param maxCountArray max count of arrays to generate.
     * @param maxCountTransition max count of Transitions in each<br/>
     * array to generate.
     * @param finiteAutomate determines if the TuringMachine should be generated<br/>
     * as a FiniteAutomate.
     * @param alphabetIT alphabet of Input Tape
     * @param alphabetWT alphabet of Working Tape
     * @param LengthIT  InputTape´s length of the baseConfiguration<br/>
     * (startSymbolIT and endSymbolIT are added afterwards)
     * @param LengthWT WorkingTape´s length of the baseConfiguration<br/>
     * (startSymbolWT is added afterwards)
     * @param ITHead position of the InputTape´s Head of the baseConfiguration
     * @param WTHead position of the WorkingTape´s Head of the baseConfiguration
     * @return a random TuringMachine.
     */
    public static TuringMachine generateTestTuringMachine(char startChar, Random random, int maxCountArray, int maxCountTransition, boolean finiteAutomate, String alphabetIT, String alphabetWT, int LengthIT, int LengthWT, int ITHead, int WTHead) {
        TuringMachine testTuringMachine = new TuringMachine();
        int statecount = 1 + random.nextInt(maxCountArray + 1);
        states = generateTestStates(startChar, statecount);
        ArrayList testTransitionCollection = generateTestCollection(states, maxCountTransition, alphabetIT, alphabetWT);
        transitionCollection = new TransitionCollection();
        for (int f = 0; f < testTransitionCollection.size(); f++) {
            for (int g = 0; g < ((ArrayList) testTransitionCollection.get(f)).size(); g++) {
                transitionCollection.add(((ArrayList) testTransitionCollection.get(f)).get(g));
            }
        }
        if (debug) {
            System.out.println("collection generated");
            printTestData(testTransitionCollection);
            System.out.println("collection added");
            transitionCollection.printCollection();
        }
        int initalStateIndex = random.nextInt(states.size());
        State initalState = states.get(initalStateIndex);
        marked = new ArrayList();
        for (int i = 0; i < states.size(); i++) {
            marked.add(0);
        }
        DepthFirstSearch(initalStateIndex);
        if (debug) System.out.println("InitialState " + initalState.getStateByFormat(State.statePrintFormat));
        if (debug) transitionCollection.printCollection();
        for (int i = 0; i < marked.size(); i++) {
            if (!marked.get(i).equals(1)) {
                if (debug) System.out.println(states.get(i).getStateByFormat(State.statePrintFormat) + " is not reached -> remove from collection and stateArray");
                states.remove(i);
                marked.remove(i);
                removed = true;
                ArrayList<Transition> transitionsToRemove = (ArrayList<Transition>) testTransitionCollection.get(i);
                testTransitionCollection.remove(i);
                for (int e = 0; e < transitionsToRemove.size(); e++) {
                    transitionCollection.remove(transitionsToRemove.get(e));
                }
                i--;
            }
        }
        if (removed) {
            if (debug) {
                System.out.println("cleaned collection");
                transitionCollection.printCollection();
            }
        }
        ArrayList finalStates = new ArrayList();
        int numFinalStates;
        if (states.size() > 1) {
            numFinalStates = 1 + random.nextInt(states.size() - 1);
        } else {
            numFinalStates = 1;
        }
        int generatedPos = random.nextInt(states.size());
        if (numFinalStates != states.size()) {
            for (int i = 0; i < numFinalStates; i++) {
                while (finalStates.contains(states.get(generatedPos))) {
                    generatedPos = random.nextInt(states.size());
                }
                finalStates.add(states.get(generatedPos));
            }
        } else {
            finalStates = states;
        }
        BaseConfiguration testBaseConfiguration = new BaseConfiguration();
        String IT = "";
        for (int i = 0; i < LengthIT; i++) {
            IT += alphabetIT.charAt(random.nextInt(alphabetIT.length()));
        }
        IT = TuringMachine.startSymbolIT + IT + TuringMachine.endSymbolIT;
        String WT = "";
        for (int i = 0; i < LengthWT; i++) {
            WT += alphabetWT.charAt(random.nextInt(alphabetWT.length()));
        }
        WT = TuringMachine.startSymbolWT + WT;
        testBaseConfiguration.setIT(IT);
        testBaseConfiguration.setWT(WT);
        try {
            testBaseConfiguration.setITHead(ITHead);
            testBaseConfiguration.setWTHead(WTHead);
        } catch (TapeOutofBoundsException ex) {
            Logger.getLogger(TestDataGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        testTuringMachine.setStates(states);
        testTuringMachine.setAlphabetIT(alphabetIT);
        testTuringMachine.setAlphabetWT(alphabetWT);
        testTuringMachine.setTransitionFunction(transitionCollection);
        testTuringMachine.setInitalState(initalState);
        testTuringMachine.setFinalStates(finalStates);
        testTuringMachine.setBaseConfiguration(testBaseConfiguration);
        testTuringMachine.setFiniteAutomate(finiteAutomate);
        testTuringMachine.setBlankSymbol('_');
        return testTuringMachine;
    }

    /**
     * Runs a DFS algorithm to determine which states can be reached from <br/>
     * the initialState.
     * @param index of the algorithm´s current state
     */
    public static void DepthFirstSearch(int index) {
        marked.set(index, 1);
        if (debug) System.out.println("currentState " + states.get(index).getStateByFormat(State.statePrintFormat));
        ArrayList<State> neighbours = getNeighbours(states.get(index));
        if (debug) {
            System.out.println("Neighbours " + neighbours.size());
            for (int i = 0; i < neighbours.size(); i++) {
                neighbours.get(i).printState();
                System.out.print("-");
            }
            System.out.println("");
        }
        for (int i = 0; i < neighbours.size(); i++) {
            if (!marked.get(states.indexOf(neighbours.get(i))).equals(1)) {
                DepthFirstSearch(states.indexOf(neighbours.get(i)));
            }
        }
    }

    /**
     * Returns all neighbours of a given state.
     * @param state given state.
     * @return all neighbours of a given state.
     */
    public static ArrayList<State> getNeighbours(State state) {
        ArrayList<State> neighbours = new ArrayList();
        if (debug) {
            System.out.println("size " + transitionCollection.size());
            state.printState();
            System.out.println("");
        }
        for (int i = 0; i < transitionCollection.size(); i++) {
            ArrayList<Transition> currentTransitionArray = ((ArrayList) transitionCollection.getSortedTransitions().get(i));
            if (debug) {
                System.out.println("size2 " + currentTransitionArray.size());
                currentTransitionArray.get(0).getState().printState();
                System.out.println("");
            }
            if (currentTransitionArray.get(0).getState().equals(state)) {
                if (debug) System.out.println("index " + i);
                for (int e = 0; e < currentTransitionArray.size(); e++) {
                    Transition currentTransition = currentTransitionArray.get(e);
                    if (!neighbours.contains(currentTransition.getTargetState())) {
                        neighbours.add(currentTransition.getTargetState());
                    }
                }
                break;
            }
        }
        return neighbours;
    }

    /**
     * Prints the Collection
     * @param printdata ArrayList imitating the structure of the collection
     */
    private static void printTestData(ArrayList printdata) {
        for (int i = 0; i < printdata.size(); i++) {
            System.out.print("|");
            for (int e = 0; e < ((ArrayList) printdata.get(i)).size(); e++) {
                Transition printTransition = ((Transition) ((ArrayList) printdata.get(i)).get(e));
                System.out.print(" - ");
                printTransition.printTransition(Transition.transitionPrintFormat, State.statePrintFormat);
            }
            System.out.println("");
        }
    }

    /**
     * Generates a set of ArrayLists imitating the TransitionCollections<br/>
     * internal structure.
     * @param startChar name of the first state to generate.<br/>
     * Each state is the indexState of one TransitionArray.
     * @param random new instance of Java Random.
     * @param maxCountArray max count of arrays to generate.
     * @param maxCountTransition max count of Transitions in each<br/>
     * array to generate.
     * @param alphabetIT alphabet of the Input Tape
     * @param alphabetWT alphabet of the Working Tape
     * @return set of ArrayLists imitating the TransitionCollections<br/>
     * internal structure.
     */
    public static ArrayList generateTestCollection(char startChar, Random random, int maxCountArray, int maxCountTransition, String alphabetIT, String alphabetWT) {
        int localcountArray = 1 + random.nextInt(maxCountArray + 1);
        ArrayList stateArray = generateTestStates(startChar, localcountArray);
        return generateTestCollection(stateArray, maxCountTransition, alphabetIT, alphabetWT);
    }

    /**
     * Generates a set of ArrayLists imitating the TransitionCollections<br/>
     * internal structure.
     * @param stateArray ArrayList with States to use
     * @param maxCountTransition max count of Transitions in each<br/>
     * array to generate.
     * @param alphabetIT alphabet of the Input Tape
     * @param alphabetWT alphabet of the Working Tape
     * @return set of ArrayLists imitating the TransitionCollections<br/>
     * internal structure.
     */
    public static ArrayList generateTestCollection(ArrayList stateArray, int maxCountTransition, String alphabetIT, String alphabetWT) {
        ArrayList collectionArray = new ArrayList();
        for (int i = 0; i < stateArray.size(); i++) {
            collectionArray.add(generateTestTransitions(stateArray, i, new Random(), maxCountTransition, alphabetIT, alphabetWT));
        }
        return collectionArray;
    }

    /**
     * Generates a set of Transitions with states with the given name.
     * @param stateArray Array of states to be used during generation.
     * @param index Index of the dominating State of this Array.
     * @param random new instance of Java Random.
     * @param maxCountTransition max count of Transitions to generate.
     * @param alphabetIT alphabet of the Input Tape
     * @param alphabetWT alphabet of the Working Tape
     * @return ArrayList with Transitions
     */
    public static ArrayList generateTestTransitions(ArrayList stateArray, int index, Random random, int maxCountTransition, String alphabetIT, String alphabetWT) {
        int countTransition = 1 + random.nextInt(maxCountTransition + 1);
        ArrayList transitionArray = new ArrayList();
        State indexState = (State) stateArray.get(index);
        char randomChar = indexState.getName().charAt(0);
        for (int i = 0; i < countTransition; i++) {
            transitionArray.add(generateTestTransition(indexState, stateArray, new Random(), alphabetIT, alphabetWT));
        }
        return transitionArray;
    }

    /**
     * Generates a Transition out of the given Parameters.
     * @param indexState "current" state of the transition.
     * @param stateArray set of states to pick the target state from.
     * @param random new instance of Java Random.
     * @param alphabetIT alphabet of the Input Tape
     * @param alphabetWT alphabet of the Working Tape
     * @return generated Transition
     */
    public static Transition generateTestTransition(State indexState, ArrayList stateArray, Random random, String alphabetIT, String alphabetWT) {
        char a = alphabetIT.charAt(random.nextInt(alphabetIT.length()));
        char X = alphabetIT.charAt(random.nextInt(alphabetIT.length()));
        char Y = alphabetWT.charAt(random.nextInt(alphabetWT.length()));
        int randomState = random.nextInt(stateArray.size());
        return new Transition(indexState, a, X, (State) stateArray.get(randomState), Y, (1 + random.nextInt(3)), (1 + random.nextInt(3)));
    }

    /**
     * Generates a set of States starting with the given name and id. The <br/>
     * stateName is incremented.
     * @param stateName name of the first state.
     * @param countState count of States to generate.
     * @return ArrayList with States
     */
    public static ArrayList generateTestStates(char stateName, int countState) {
        ArrayList stateArray = new ArrayList();
        for (int i = 0; i < countState; i++) {
            stateArray.add(new State("" + ((char) stateName)));
            stateName++;
        }
        return stateArray;
    }

    /**
     * Fetches a random Transition from a given TransitionCollection.
     * @param collection given TransitionCollection.
     * @return a random Transition from a given TransitionCollection.
     */
    public static Transition fetchRandomTransitionFromCollection(ArrayList collection) {
        Random random = new Random();
        int arrayPos = random.nextInt(collection.size());
        int transitionPos = random.nextInt(((ArrayList) collection.get(arrayPos)).size());
        return (Transition) ((ArrayList) collection.get(arrayPos)).get(transitionPos);
    }

    /**
     * Fetches a random Transition from a given TransitionCollection at a<br/>
     * given index.
     * @param collection given TransitionCollection.
     * @param index from which the Transition will be fetched.
     * @return a random Transition from a given TransitionCollection at a<br/>
     * given index.
     */
    public static Transition fetchRandomTransitionFromCollectionIndex(ArrayList collection, int index) {
        Random random = new Random();
        int randomPos = random.nextInt(((ArrayList) collection.get(index)).size());
        return (Transition) ((ArrayList) collection.get(index)).get(randomPos);
    }

    /**
     * Fetches all equal Transitions on the given index out of the<br/>
     * collection. Two Transitions are equal if they contain the same State<br/>
     * and the same inSymbolIT and inSymbolWT.
     * @param collection given TransitionCollection.
     * @param index from which the Transitions will be fetched.
     * @param transition to filter the equal transitions
     * @return all equal Transitions on the given index.
     */
    public static ArrayList fetchAllTransitions(ArrayList collection, int index, Transition transition) {
        ArrayList expectedTransitions = new ArrayList();
        for (int i = 0; i < ((ArrayList) collection.get(index)).size(); i++) {
            Transition tempTransition = (Transition) ((ArrayList) (collection.get(index))).get(i);
            if (transition.getState().equals(tempTransition.getState()) && transition.getInSymbolIT() == tempTransition.getInSymbolIT() && transition.getInSymbolWT() == tempTransition.getInSymbolWT()) {
                expectedTransitions.add(tempTransition);
            }
        }
        return expectedTransitions;
    }

    /**
     * Return´s the Index of the ArrayList inside the collection with the<br/>
     * given Transition´s state.
     * @param collection given TransitionCollection.
     * @param transition which holds the state to be searched.
     * @return Index of the ArrayList inside the collection with the<br/>
     * given Transition´s state.
     */
    public static int fetchIndexOfTransition(ArrayList collection, Transition transition) {
        int index = -1;
        for (int i = 0; i < collection.size(); i++) {
            Transition tempTransition = ((Transition) ((ArrayList) collection.get(i)).get(0));
            if (transition.getState().equals(tempTransition.getState())) {
                index = i;
            }
        }
        return index;
    }

    /**
     * Fetch a TransitionSet from a random Transition out of the given<br/>
     * collection.
     * @param collection given TransitionCollection.
     * @return TransitionSet from a random Transition out of the given<br/>
     * collection.
     */
    public static ArrayList fetchRandomTransitionSet(ArrayList collection) {
        Transition randomTransition = fetchRandomTransitionFromCollection(collection);
        int arrayPos = fetchIndexOfTransition(collection, randomTransition);
        return fetchAllTransitions(collection, arrayPos, randomTransition);
    }
}
