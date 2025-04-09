package cn.edu.thss.iise.beehivez.server.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.fsm.FSMTransition;
import org.processmining.framework.models.petrinet.Marking;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.State;
import org.processmining.framework.models.petrinet.StateSpace;
import org.processmining.framework.models.petrinet.Token;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.models.petrinet.algorithms.CoverabilityGraphBuilder;
import att.grappa.Edge;

/**
 * clean models according to the requirements
 * 
 * @author Tao Jin
 * 
 */
public class CleanModels {

    /**
	 * @param path
	 *            where the models exist
	 * @param recordFileName
	 *            record the file need to be removed in this file
	 */
    public static void findModelsShouldBeRemoved(String path, String recordFileName) {
        try {
            FileWriter fw = new FileWriter(recordFileName, false);
            BufferedWriter bw = new BufferedWriter(fw);
            int nRemoveModels = 0;
            File dir = new File(path);
            for (File f : dir.listFiles()) {
                System.out.println("parsing " + f.getName());
                PetriNet pn = PetriNetUtil.getPetriNetFromPnml(new FileInputStream(f));
                for (Place place : pn.getPlaces()) {
                    place.removeAllTokens();
                    if (place.inDegree() == 0) {
                        place.addToken(new Token());
                    }
                }
                if (build(pn) == null) {
                    bw.write(f.getName());
                    bw.newLine();
                    nRemoveModels++;
                }
            }
            System.out.println(nRemoveModels + " models should be removed");
            bw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeFiles(String path, String listFileName) {
        try {
            FileReader fr = new FileReader(listFileName);
            BufferedReader br = new BufferedReader(fr);
            HashSet<String> fileList = new HashSet<String>();
            String fileName = br.readLine();
            while (fileName != null) {
                fileList.add(fileName);
                fileName = br.readLine();
            }
            int nFileRemoved = 0;
            int nFileTotal = 0;
            File dir = new File(path);
            for (File file : dir.listFiles()) {
                nFileTotal++;
                if (fileList.contains(file.getName())) {
                    file.delete();
                    nFileRemoved++;
                }
            }
            int nFileRemain = nFileTotal - nFileRemoved;
            System.out.println("the total number of files: " + nFileTotal);
            System.out.println("the number of files removed: " + nFileRemoved);
            System.out.println("the number of files remained: " + nFileRemain);
            br.close();
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
	 * Constructs the full coverability graph for a given marked net. If a
	 * partial state space shall be constructed only, use
	 * {@link #build(PetriNet, int)} instead. <br>
	 * Remembers the start state (that is, the marking of the given net) in the
	 * {@link StateSpace#initialState initialState} attribute.
	 * 
	 * @param net
	 *            The marked net
	 * @return The coverability graph
	 */
    public static synchronized StateSpace build(PetriNet net) {
        return build(net, -1);
    }

    /**
	 * Constructs the coverability graph with a potentially limited depth for a
	 * given marked net. If the full state space shall be constructed, use
	 * {@link #build(PetriNet)} instead. <br>
	 * Remembers the start state (that is, the marking of the given net) in the
	 * {@link StateSpace#initialState initialState} attribute.
	 * 
	 * @param net
	 *            The marked net
	 * @param depth
	 *            The depth until which the state space should be constructed.
	 *            If it is 0, only the initial state will be contained in the
	 *            constructed state space. If it is 1, the initial state and all
	 *            its direct successor states are constructed. If it is 2, all
	 *            direct successor states of the previously constructed states
	 *            are built as well etc.
	 * @return The coverability graph
	 */
    public static synchronized StateSpace build(PetriNet net, int depth) {
        return build(net, depth, false, true);
    }

    /**
	 * Construct a coverability graph.
	 * 
	 * @param net
	 *            The net to construct a (fragment) coverability graph for.
	 * @param depth
	 *            The maximal depth for the fragment.
	 * @param useAllPredecessors
	 *            Whether to use all predecessors when checking for unbounded
	 *            places or only spanning tree predecessors.
	 * @param expandUnboundedStates
	 *            Whether to expand unbounded states.
	 * @return A (fragment of) a coverability graph.
	 */
    public static synchronized StateSpace build(PetriNet net, int depth, boolean useAllPredecessors, boolean expandUnboundedStates) {
        long startTime = System.currentTimeMillis();
        Transition transition;
        Place place;
        Iterator it, pit;
        Marking marking, newMarking;
        State state, newState;
        StateSpace graph;
        Hashtable preSet;
        Hashtable postSet;
        LinkedList toDo;
        TreeMap treeMap;
        ArrayList transitions;
        HashMap<State, Integer> depthLevel;
        Marking sinkMarking = new Marking();
        preSet = new Hashtable();
        postSet = new Hashtable();
        it = net.getTransitions().iterator();
        while (it.hasNext()) {
            transition = (Transition) it.next();
            marking = new Marking();
            pit = transition.getPredecessors().iterator();
            while (pit.hasNext()) {
                place = (Place) pit.next();
                marking.addPlace(place, net.getEdgesBetween(place, transition).size());
            }
            preSet.put(transition, marking);
            marking = new Marking();
            pit = transition.getSuccessors().iterator();
            while (pit.hasNext()) {
                place = (Place) pit.next();
                marking.addPlace(place, net.getEdgesBetween(transition, place).size());
            }
            postSet.put(transition, marking);
        }
        ModelGraphVertex sink = net.getSink();
        if (sink instanceof Place) {
            sinkMarking.addPlace((Place) sink, 1);
        }
        graph = new StateSpace(net);
        state = new State(graph);
        marking = state.getMarking();
        it = net.getPlaces().iterator();
        while (it.hasNext()) {
            place = (Place) it.next();
            if (place.getNumberOfTokens() > 0) {
                marking.addPlace(place, place.getNumberOfTokens());
            }
        }
        graph.addState(state);
        graph.setStartState(state);
        if (marking.equals(sinkMarking)) {
            graph.addAcceptState(state);
        }
        toDo = new LinkedList();
        toDo.addLast(state);
        depthLevel = new HashMap<State, Integer>();
        if (depth >= 0) {
            depthLevel.put(state, new Integer(depth));
        }
        treeMap = new TreeMap();
        treeMap.put(marking, state);
        transitions = net.getTransitions();
        Integer currentDistObject;
        int currentDistance = 0;
        while (!toDo.isEmpty()) {
            long consumedTime = System.currentTimeMillis() - startTime;
            if (consumedTime > 1000) {
                return null;
            }
            state = (State) toDo.removeFirst();
            currentDistObject = depthLevel.get(state);
            if (currentDistObject != null) {
                currentDistance = currentDistObject.intValue();
            }
            if (depth < 0 || currentDistance > 0) {
                marking = state.getMarking();
                it = transitions.iterator();
                while (it.hasNext()) {
                    transition = (Transition) it.next();
                    if (((Marking) preSet.get(transition)).isLessOrEqual(marking)) {
                        boolean isFresh = true;
                        newState = new State(graph);
                        newMarking = newState.getMarking();
                        newMarking.add(marking);
                        newMarking.sub((Marking) preSet.get(transition));
                        newMarking.add((Marking) postSet.get(transition));
                        if (treeMap.containsKey(newMarking)) {
                            newState = (State) treeMap.get(newMarking);
                            isFresh = false;
                        } else {
                            boolean isExtended = false;
                            if (useAllPredecessors) {
                                HashSet<State> predecessors = new HashSet<State>();
                                LinkedList<State> predecessorsToDo = new LinkedList<State>();
                                predecessors.add(state);
                                predecessorsToDo.addFirst(state);
                                while (!predecessorsToDo.isEmpty()) {
                                    State predecessor = predecessorsToDo.removeFirst();
                                    if (predecessor.getMarking().isLessOrEqual(newMarking)) {
                                        pit = newMarking.iterator();
                                        while (pit.hasNext()) {
                                            place = (Place) pit.next();
                                            if (predecessor.getMarking().getTokens(place) < newMarking.getTokens(place)) {
                                                newMarking.addPlace(place, Marking.OMEGA);
                                                isExtended = true;
                                            }
                                        }
                                    }
                                    Iterator it2 = predecessor.getInEdgesIterator();
                                    while (it2.hasNext()) {
                                        Edge edge = (Edge) it2.next();
                                        ModelGraphVertex vertex = (ModelGraphVertex) edge.getTail();
                                        if (vertex instanceof State) {
                                            State newPredecessor = (State) vertex;
                                            if (!predecessors.contains(newPredecessor)) {
                                                predecessors.add(newPredecessor);
                                                predecessorsToDo.addLast(newPredecessor);
                                            }
                                        }
                                    }
                                }
                            } else {
                                State predecessor = state;
                                while (predecessor != null) {
                                    if (predecessor.getMarking().isLessOrEqual(newMarking)) {
                                        pit = newMarking.iterator();
                                        while (pit.hasNext()) {
                                            place = (Place) pit.next();
                                            if (predecessor.getMarking().getTokens(place) < newMarking.getTokens(place)) {
                                                newMarking.addPlace(place, Marking.OMEGA);
                                                isExtended = true;
                                            }
                                        }
                                    }
                                    predecessor = predecessor.getPredecessor();
                                }
                            }
                            if (isExtended && treeMap.containsKey(newMarking)) {
                                newState = (State) treeMap.get(newMarking);
                                isFresh = false;
                            }
                        }
                        if (isFresh) {
                            graph.addState(newState);
                            if (newMarking.equals(sinkMarking)) {
                                graph.addAcceptState(newState);
                            }
                            newState.setPredecessor(state);
                            treeMap.put(newMarking, newState);
                            if (expandUnboundedStates || (newState.getMarking().getTokenCount() != Marking.OMEGA)) {
                                toDo.addLast(newState);
                            }
                            if (currentDistance > 0) {
                                depthLevel.put(newState, new Integer(currentDistance - 1));
                            }
                        }
                        FSMTransition edge = new FSMTransition(state, newState, transition.getIdentifier());
                        graph.addEdge(edge);
                        edge.object = transition;
                    }
                }
            }
        }
        if (!toDo.isEmpty()) {
            System.out.println("Insufficient memory available to construct complete coverability graph. Constructed coverability graph is truncated!");
        }
        return graph;
    }

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        findModelsShouldBeRemoved("cleanedEpcPnml", "removelist.txt");
    }
}
