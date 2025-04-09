package ring.gui;

import gbl.common.entity.TSProblemModel;
import gbl.common.util.ColorUtil;
import java.lang.reflect.Constructor;
import ring.controller.AlgorithmController;
import ring.gui.event.AlgorithmObserver;
import ring.gui.event.WorkspaceObserver;
import ring.gui.graphdrawing.GraphWorkspace;
import diamante.core.command.Algorithm;
import diamante.core.graph.Graph;
import diamante.core.util.AlgorithmStarter;

/**
 * The thread responsible for executing an algorithm, which makes it
 * possible for the input of parameters while the other parts of the
 * program remain functional.
 */
public class AlgorithmExecutionThread implements Runnable, WorkspaceObserver {

    /**
	 * Name of the algorithm to be run.
	 */
    String algName;

    /**
	 * The graph on which the algorithm will be run.
	 */
    Graph g;

    /**
	 * The workspace in which the execution is being shown.
	 */
    GraphWorkspace workspace;

    /**
	 * The index of a node selected in the workspace so that it can be used
	 * as a parameter for the algorithm.
	 */
    int selectedNode = -1;

    /**
	 * The flag that transmits the end of node selection in the workspace.
	 */
    boolean endNodeSelection;

    /**
	 * The listener that will be notified of events that happen in this
	 * algorithm execution thread.
	 */
    AlgorithmObserver algorithmListener;

    /**
	 * 
	 */
    Algorithm algorithmInstance;

    /**
	 * Creates the execution thread.
	 * @param algName name of the algorithm to be run
	 * @param g graph on which the algorithm will be run
	 */
    public AlgorithmExecutionThread(String algName, Graph g, GraphWorkspace workspace, AlgorithmObserver listener) {
        this.algName = algName;
        this.g = g;
        this.workspace = workspace;
        this.algorithmListener = listener;
        workspace.setMode(GraphWorkspace.NODE);
    }

    /**
	 * Analyses the algorithm via reflection and requests its parameters
	 * accordingly, executing the algorithm after all parameters have been
	 * entered by the user.
	 */
    public void run() {
        AlgorithmController controller = AlgorithmController.getInstance();
        Class algorithm = ((Class) controller.algorithms.get(algName));
        Constructor[] constructors = algorithm.getConstructors();
        Object[] parameters;
        int pCount = constructors[0].getParameterTypes().length;
        parameters = new Object[pCount];
        parameters[0] = g;
        endNodeSelection = false;
        TSProblemModel tModel = TSProblemModel.getInstance();
        tModel.reset();
        workspace.setListener(this);
        workspace.setEditable(true);
        workspace.requestNode(true);
        try {
            while (selectedNode == -1) Thread.sleep(500);
        } catch (InterruptedException exc) {
        }
        tModel.setSourceNodeIndex(selectedNode);
        ColorUtil.setSourceNodeColor(g, selectedNode);
        workspace.refresh();
        selectedNode = -1;
        while (!endNodeSelection) {
            workspace.requestNode(false);
            try {
                while (selectedNode == -1 && !endNodeSelection) Thread.sleep(500);
            } catch (InterruptedException exc) {
            }
            tModel.addClient(selectedNode);
            ColorUtil.setClientNodeColor(g, selectedNode);
            workspace.refresh();
            selectedNode = -1;
        }
        workspace.setEditable(false);
        try {
            Object instance = constructors[0].newInstance(parameters);
            fireAlgorithmStarted((Algorithm) instance);
            AlgorithmStarter starter = AlgorithmStarter.getInstance();
            starter.start((Algorithm) instance);
            fireAlgorithmFinished((Algorithm) instance);
            workspace.refresh();
        } catch (final Exception exc) {
            exc.printStackTrace();
            algorithmListener.exceptionRaised(exc);
        }
    }

    /**
	 * 
	 */
    private void fireAlgorithmStarted(Algorithm alg) {
        if (algorithmListener != null) algorithmListener.algorithmStarted(alg);
    }

    /**
	 * 
	 */
    private void fireAlgorithmFinished(Algorithm alg) {
        if (algorithmListener != null) algorithmListener.algorithmFinished(alg);
    }

    /**
	 * @see ring.gui.event.WorkspaceObserver#nodeSelected(int)
	 */
    public void nodeSelected(int i) {
        selectedNode = i;
    }

    /**
	 * @see ring.gui.event.WorkspaceObserver#endNodeSelection()
	 */
    public void endNodeSelection() {
        endNodeSelection = true;
    }

    public Algorithm getAlgorithmInstance() {
        return algorithmInstance;
    }
}
