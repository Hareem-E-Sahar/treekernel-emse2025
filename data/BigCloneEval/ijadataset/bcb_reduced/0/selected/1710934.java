package com.bbn.vessel.author.situationEditor;

import java.awt.Point;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import com.bbn.vessel.author.imspec.IMConstants;
import com.bbn.vessel.author.instructionalSignalEditor.InstructionalSignal;
import com.bbn.vessel.author.instructionalSignalEditor.InstructionalSignalEditor;
import com.bbn.vessel.author.instructionalSignalEditor.InstructionalState;
import com.bbn.vessel.author.instructionalTacticsEditor.InstructionalTactic;
import com.bbn.vessel.author.logicEditor.editor.searcher.GraphSearcher;
import com.bbn.vessel.author.logicEditor.editor.searcher.NodeSpecMatcher;
import com.bbn.vessel.author.logicEditor.editor.searcher.NodeTypeMatcher;
import com.bbn.vessel.author.logicEditor.editor.searcher.SignatureMatcher;
import com.bbn.vessel.author.models.Connection;
import com.bbn.vessel.author.models.Graph;
import com.bbn.vessel.author.models.GraphNode;
import com.bbn.vessel.author.models.GroupNode;
import com.bbn.vessel.author.models.Side;
import com.bbn.vessel.author.models.Terminal;
import com.bbn.vessel.author.models.TerminalSpec;
import com.bbn.vessel.author.models.TerminalType;
import com.bbn.vessel.author.models.VesselNode;
import com.bbn.vessel.author.util.GraphHelper;
import com.bbn.vessel.author.workspace.Workspace;

/**
 * this class represents an instructional strategy.
 *
 * @author jostwald
 *
 */
public class InstructionalStrategy {

    private GroupNode strategyGroupNode;

    private final int workspaceId;

    private Map<Integer, InstructionalTactic> tactics;

    private static final Logger logger = Logger.getLogger(InstructionalStrategy.class);

    /**
     * name of the enableState terminal
     */
    public static final String TERM_NAME_ENABLE_STATE = "enableState";

    /**
     * name of teh guided discovery strategy, matching
     * config/instructional_strategies.xml
     */
    public static final String STRAT_NAME_GUIDED_DISCOVERY = "Guided Discovery";

    /**
     * name of teh query, test and perform strategy, matching
     * config/instructional_strategies.xml
     */
    public static final String STRAT_NAME_QUERY_TEST_PERFORM = "Query, Test and Perform";

    private InstructionalSignal enableState;

    private boolean enableStateInitialized = false;

    private boolean situationInitialized;

    private Situation situation;

    private InstructionalStrategySpec spec;

    /**
     * create a new instructional strategy with provided title
     *
     * @param strategyGroup
     *            the group node in the graph corresponding to this strategy
     * @param workspaceId
     *            the workspace this InstructionalStrategy lives in
     */
    public InstructionalStrategy(GroupNode strategyGroup, int workspaceId) {
        super();
        this.workspaceId = workspaceId;
        if ((strategyGroup.getSignature() == null) || (!strategyGroup.getSignature().equals(SituationEditor.SIGNATURE_INSTRUCTIONAL_STRATEGY))) {
            throw new IllegalArgumentException(strategyGroup + "is not an instructional strategy group");
        }
        strategyGroupNode = strategyGroup;
    }

    /**
     * construct a new instructional strategy in the graph
     *
     * @param strategyName
     *            name of the new instructional strategy
     * @param parentGraph
     *            graph to add the new group node to
     * @param workspaceId
     *            id of the workspace where all this is happening
     */
    public InstructionalStrategy(String strategyName, Graph parentGraph, int workspaceId) {
        this.workspaceId = workspaceId;
        strategyGroupNode = new GroupNode(parentGraph);
        parentGraph.addGraphElement(strategyGroupNode);
        strategyGroupNode.addOutsideAndExternalTerminals(new TerminalSpec(InstructionalStrategy.TERM_NAME_ENABLE_STATE, TerminalType.CONDITION_SINK, true, Side.LEFT), null, new Point(0, 0));
        strategyGroupNode.setTitle(Side.BOTTOM, strategyName);
        strategyGroupNode.getNodeSpec().setPrettyType(strategyName);
        strategyGroupNode.setTitle(Side.TOP, "Instructional Mechanic");
        strategyGroupNode.setSignature(SituationEditor.SIGNATURE_INSTRUCTIONAL_STRATEGY);
    }

    /**
     *
     * @return the title of the strategy
     */
    public String getName() {
        return getStrategyGroupNode().getTitle(Side.BOTTOM);
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * set the group node in the graph corresponding to this strategy
     *
     * @param stratGroup
     *            the group node in the graph corresponding to this strategy
     */
    public void setStrategyGroupNode(GroupNode stratGroup) {
        this.strategyGroupNode = stratGroup;
    }

    /**
     * set the group node in the graph corresponding to this strategy
     *
     * @return the group node in the graph corresponding to this strategy
     */
    public GroupNode getStrategyGroupNode() {
        return strategyGroupNode;
    }

    /**
     * set the InstructionalState that enables this instructional strategy. wire
     * up the graph accordingly
     *
     * @param newState
     *            the instructional state, or null if you want the strategy
     *            globally enabled across its situation
     */
    public void setEnableState(InstructionalSignal newState) {
        if (!enableStateInitialized) {
            initializeEnableState();
        }
        Terminal strategyInsideTerminal = strategyGroupNode.getTerminal(TerminalType.CONDITION_SINK, TERM_NAME_ENABLE_STATE);
        Graph sitGraph = getStrategyGroupNode().getGraph();
        Collection<VesselNode> sitNodes = GraphSearcher.findNodes(new NodeTypeMatcher("IsSituation"), sitGraph, false, false);
        if (sitNodes.size() != 1) {
            logger.error("found " + sitNodes.size() + " IsSituation nodes in " + "sitGroup.  should be 1", new Exception());
            return;
        }
        VesselNode sitNode = sitNodes.iterator().next();
        if (strategyInsideTerminal.numConnections() > 1) {
            logger.error(strategyInsideTerminal.numConnections() + " things " + "are connected to are enable terminal.  should be 1", new Exception());
        }
        GroupNode sitGroup = sitGraph.getContainingGroupNode();
        if (enableState == null) {
            GraphHelper.disconnectTerminals(strategyInsideTerminal, sitNode.findTerminal(TerminalType.CONDITION_SOURCE, ""));
        } else {
            sitGroup.removeTerminalsForNode(getStrategyGroupNode());
        }
        this.enableState = newState;
        if (newState != null) {
            Terminal strategyExternalTerminal = sitGroup.addExternalTerminalForInsideTerminal(strategyInsideTerminal);
            GroupNode stateGroup = newState.getGroup();
            Terminal stateTerminal = stateGroup.getTerminal(TerminalType.CONDITION_SOURCE, InstructionalState.TERM_NAME_OUT);
            GraphHelper.connectTerminals(strategyExternalTerminal, stateTerminal);
        } else {
            GraphHelper.connectTerminals(strategyInsideTerminal, sitNode.findTerminal(TerminalType.CONDITION_SOURCE, ""));
        }
    }

    /**
     * get the InstructionalState that enables this instructional strategy.
     *
     * @return the instructional state, or null if the strategy is globally
     *         enabled across its situation
     */
    public InstructionalSignal getEnableState() {
        if (!enableStateInitialized) {
            initializeEnableState();
        }
        return enableState;
    }

    private void initializeEnableState() {
        Terminal enableTerm = strategyGroupNode.findTerminal(TerminalType.CONDITION_SINK, TERM_NAME_ENABLE_STATE);
        GroupNode sitGroup = strategyGroupNode.getGraph().getContainingGroupNode();
        for (Connection conn : enableTerm.getConnections()) {
            Terminal externalTerm = sitGroup.getExternalForOutside(conn.getFrom());
            if (externalTerm != null) {
                for (Connection extConn : externalTerm.getConnections()) {
                    InstructionalSignalEditor instructionalStateEditor = findInstructionalStateEditor();
                    GraphNode fromNode = extConn.getFrom().getGraphNode();
                    if (fromNode instanceof GroupNode) {
                        InstructionalSignal state = instructionalStateEditor.getStateForGroup((GroupNode) fromNode);
                        if (state != null) {
                            enableState = state;
                        }
                    }
                }
            }
        }
        enableStateInitialized = true;
    }

    private InstructionalSignalEditor findInstructionalStateEditor() {
        return (InstructionalSignalEditor) Workspace.getWorkspaceForId(workspaceId).getToolForName("Instructional State Editor");
    }

    /**
     *
     * add the tactic to the strategy. also modifies the graph editor's graph.
     * and sets the tactic's strategy backpointer
     *
     * @param tactic
     *            Instructional Tactic to add
     */
    public void importTactic(InstructionalTactic tactic) {
        addTactic(tactic);
        getStrategyGroupNode().addGraphNode(tactic.getGroupNode(), null);
    }

    /**
     * adds a tactic whose graph representation is already present in the
     * instructional strategy group.
     *
     * @param tactic
     *            a tactic whose graph representation is already present in the
     *            instructional strategy group. (or about to be added
     *            independently
     */
    public void addTactic(InstructionalTactic tactic) {
        if (tactics == null) {
            tactics = collectTacticsFromGraph();
        }
        tactics.put(tactic.getId(), tactic);
        tactic.setInstructionalStrategy(this);
    }

    /**
     *
     * @return get all the instructional tactics associated with this
     *         instructional strategy
     */
    public Collection<InstructionalTactic> getTactics() {
        if (tactics == null) {
            tactics = collectTacticsFromGraph();
        }
        return Collections.unmodifiableCollection(tactics.values());
    }

    private HashMap<Integer, InstructionalTactic> collectTacticsFromGraph() {
        HashMap<Integer, InstructionalTactic> tacts = new HashMap<Integer, InstructionalTactic>();
        for (Class<? extends InstructionalTactic> tactCl : getSpec().getTacticClasses()) {
            Collection<VesselNode> tactNodes = null;
            try {
                tactNodes = GraphSearcher.findNodes(new SignatureMatcher(IMConstants.GROUP_SIGNATURE), getStrategyGroupNode().getGraph());
            } catch (Exception e) {
                logger.error("couldn't get group signature of " + tactCl, e);
            }
            if (tactNodes != null) {
                for (VesselNode node : tactNodes) {
                    InstructionalTactic newTactic;
                    try {
                        newTactic = tactCl.getConstructor(GroupNode.class, Integer.class).newInstance(node, workspaceId);
                        newTactic.setInstructionalStrategy(this);
                        tacts.put(newTactic.getId(), newTactic);
                    } catch (Exception e) {
                        logger.error("couldn't construct " + tactCl, e);
                    }
                }
            }
        }
        return tacts;
    }

    /**
     *
     * @return the situation containing this strategy
     *
     */
    public Situation getSituation() {
        if (!situationInitialized) {
            initializeSituationFromGraph();
        }
        return situation;
    }

    /**
     * sets the pointer to the Situation. NOTE: this does not modify the graph
     * in any way. see situation.addConversation and
     * situation.removeConversation for the code that moves the conversation
     * into the situation's group
     *
     * @param situation
     *            the situation containing this strategy
     */
    public void setSituation(Situation situation) {
        situationInitialized = true;
        this.situation = situation;
    }

    private void initializeSituationFromGraph() {
        Workspace workspace = Workspace.getWorkspaceForId(workspaceId);
        Collection<VesselNode> sitNodes = GraphSearcher.findNodes(new NodeSpecMatcher(workspace.getNodeSpecTable().getNodeSpec("IsSituation")), getStrategyGroupNode().getGraph(), false, false);
        if (sitNodes.size() == 0) {
            situation = null;
            return;
        } else if (sitNodes.size() > 1) {
            logger.warn("more than one situation in the same group with conversation");
        }
        VesselNode sitNode = sitNodes.iterator().next();
        SituationEditor sitEditor = (SituationEditor) workspace.getToolForName(SituationEditor.NAME);
        situation = sitEditor.getDataForId(sitNode.getDataElementId(), Situation.class);
        situationInitialized = true;
    }

    /**
     * get an instructional tactic by its id
     *
     * @param tacticId
     *            the id of the desired instructional tactic
     * @return the tactic, or null if no such tactic is associated with this
     *         strategy
     */
    public InstructionalTactic getTacticForId(int tacticId) {
        if (tactics == null) {
            tactics = collectTacticsFromGraph();
        }
        return tactics.get(tacticId);
    }

    /**
     * remove an instructional tactic
     *
     * @param tactic
     *            the tactic to be removed
     */
    public void removeTactic(InstructionalTactic tactic) {
        if (tactics == null) {
            tactics = collectTacticsFromGraph();
        }
        tactics.remove(tactic.getId());
    }

    /**
     *
     * @return the names of the tactics that can be applied under this strategy
     */
    public Collection<? extends String> getTacticNames() {
        return getSpec().getTacticNames();
    }

    private InstructionalStrategySpec getSpec() {
        if (spec == null) {
            spec = Workspace.getWorkspaceForId(workspaceId).getInstructionalStrategySpec(getName());
        }
        return spec;
    }

    /**
     * get the class of a tactic for it's name
     *
     * @param name
     *            name of the tactic
     * @return the implementation class of the tactic
     */
    public Class<? extends InstructionalTactic> getTacticClassForName(String name) {
        return getSpec().getTacticClassForName(name);
    }
}
