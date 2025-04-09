package visugraph.layout;

import java.util.Iterator;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import visugraph.data.Data;
import visugraph.graph.Graph;
import visugraph.graph.GraphException;
import visugraph.graph.GraphUtils;
import visugraph.graph.Tree;
import visugraph.graph.TreeGraph;
import visugraph.plugin.UserProperty;
import visugraph.plugin.UserPlugin;

/**
 * <p>Classe qui place une arborescence en alignant les noeuds de même profondeur sur une ligne
 * ou sur un cercle (mode radial). Complexité : O(N). Ce layout ne tient pas compte de la taille des noeuds.</p>
 */
@UserPlugin("Arborescence")
public class TreeLayout<N, E> extends AbstractLayout<N, E> {

    private int nodeSize = 50;

    private boolean polarMode = true;

    private int maxColumns;

    private int maxDepth;

    private int nbNodesPassed;

    public TreeLayout(Graph<N, E> graph) {
        super(graph, createPosData(graph));
    }

    /**
	 * Créer un nouveau placeur d'arbre. Le mode polaire est utilisé par défaut
	 * et les noeuds ont une taille de 50.
	 * @param graph le graphe à placer
	 * @param pos les positions des noeuds
	 */
    public TreeLayout(Graph<N, E> graph, Data<N, Point2D> pos) {
        super(graph, pos);
    }

    /**
	 * Créer un nouveau placeur d'arbre. Le mode polaire est utilisé par défaut
	 * et les noeuds ont une taille de 50.
	 * @param graph le graphe à placer
	 * @param pos les positions des noeuds
	 * @param dim la taille des noeuds
	 */
    public TreeLayout(Graph<N, E> graph, Data<N, Point2D> pos, Data<N, Dimension2D> dim) {
        super(graph, pos, dim);
    }

    /**
	 * Change la taille par défaut des noeuds.
	 */
    @UserProperty("Taille par défaut des noeuds")
    public void setNodeSize(int space) {
        this.nodeSize = space;
    }

    /**
	 * Retourne la taille par défaut des noeuds.
	 */
    @UserProperty("Taille par défaut des noeuds")
    public int getNodeSize() {
        return this.nodeSize;
    }

    /**
	 * Change pour le mode polaire/radial.
	 */
    @UserProperty("Mode polaire")
    public void setPolarMode(boolean polar) {
        this.polarMode = polar;
    }

    /**
	 * Retourne true si on est en mode radial.
	 */
    @UserProperty("Mode polaire")
    public boolean isPolarMode() {
        return this.polarMode;
    }

    protected void process() {
        Tree<N, E> trGraph;
        if (!(this.getGraph() instanceof Tree<?, ?>)) {
            this.setMessage("Copie de la structure du graphe...");
            try {
                trGraph = new TreeGraph<N, E>(this.getGraph().nbNodes(), this.getGraph().isDirected());
                GraphUtils.duplicate(this.getGraph(), trGraph);
            } catch (GraphException e) {
                throw new IllegalArgumentException("Le graphe doit être un arbre");
            }
        } else {
            trGraph = (Tree<N, E>) this.getGraph();
        }
        this.process(trGraph);
    }

    private void process(Tree<N, E> graph) {
        this.setMessage("Placement des noeuds en cours...");
        int sumDepths = 0;
        int sumColumns = 0;
        this.nbNodesPassed = 0;
        Iterator<N> itNodes = graph.rootNodes().iterator();
        while (itNodes.hasNext() && !this.stopAsked()) {
            this.maxColumns = 0;
            this.maxDepth = 0;
            N node = itNodes.next();
            this.placeTree(graph, node, node, 0, sumColumns);
            sumDepths += this.maxDepth + 2;
            sumColumns += this.maxColumns + 2;
            if (this.polarMode) {
                this.toPolar(graph, node, node, this.getPos(node), sumDepths);
            }
            sumDepths += this.maxDepth;
        }
    }

    /**
	 * Place une composante connexe particulière de l'arbre et retourne l'emplacement du dernier noeud racine traité.
	 */
    private double placeTree(Tree<N, E> graph, N node, N precNode, int depth, int sumColumns) {
        Iterator<E> itEdges = graph.edgesIterator(node);
        double posNode = this.maxColumns;
        double lastPos = -1;
        double firstPos = -1;
        while (itEdges.hasNext() && !this.stopAsked()) {
            N oneNode = graph.getOpposite(itEdges.next(), node);
            if (!oneNode.equals(precNode)) {
                lastPos = this.placeTree(graph, oneNode, node, depth + 1, sumColumns);
                if (firstPos == -1) {
                    firstPos = lastPos;
                }
            }
        }
        if (graph.degree(node) >= 2 || precNode.equals(node)) {
            posNode = (firstPos + lastPos) / 2;
        } else {
            this.maxColumns++;
        }
        if (depth > this.maxDepth) {
            this.maxDepth = depth;
        }
        this.getPos(node).setLocation((posNode + sumColumns) * this.nodeSize, depth * this.nodeSize);
        this.nbNodesPassed++;
        this.setAdvancement(100.0 * this.nbNodesPassed / graph.nbNodes());
        return posNode;
    }

    /**
	 * Met tous les noeuds sur des cercles. S'exécute après les avoir placé par niveau sur des lignes parallèles
	 */
    private void toPolar(Tree<N, E> graph, N node, N precNode, Point2D planarOrigin, int sumDepths) {
        this.setMessage("Passage au mode polaire...");
        double circum = this.maxColumns * this.nodeSize;
        double angle = 2 * Math.PI * (this.getPos(node).getX() - planarOrigin.getX()) / (circum + 0.001);
        double r = this.getPos(node).getY() - planarOrigin.getY();
        this.getPos(node).setLocation(r * Math.cos(angle) + sumDepths * this.nodeSize, r * Math.sin(angle));
        Iterator<E> itEdges = graph.edgesIterator(node);
        while (itEdges.hasNext() && !this.stopAsked()) {
            N oneNode = graph.getOpposite(itEdges.next(), node);
            if (!oneNode.equals(precNode)) {
                this.toPolar(graph, oneNode, node, planarOrigin, sumDepths);
            }
        }
    }
}
