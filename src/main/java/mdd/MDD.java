package mdd;

import core.Problem;
import heuristics.DeleteSelector;
import heuristics.MergeSelector;
import heuristics.VariableSelector;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents the MDD graph.
 * Gives lower/upper bounds (or the exact solution)
 * by solving the MDD representation with the given width.
 * Solves a maximization problem by default.
 *
 * @author Vianney Coppé
 */
public class MDD {

    public Set<Node> frontier;
    public MergeSelector mergeSelector;
    public DeleteSelector deleteSelector;
    public VariableSelector variableSelector;
    private Layer root;
    private Layer lastExactLayer;
    private boolean exact;

    /**
     * Returns the MDD representation of the problem.
     *
     * @param problem          the implementation of a problem
     * @param mergeSelector    heuristic to select nodes to merge (to build relaxed MDDs)
     * @param deleteSelector   heuristic to select nodes to delete (to build restricted MDDs)
     * @param variableSelector heuristic to select the next variable to be assigned
     */
    public MDD(Problem problem, MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector) {
        this(problem, mergeSelector, deleteSelector, variableSelector, problem.root());
    }

    /**
     * Returns the MDD representation of the problem.
     *
     * @param problem          the implementation of a problem
     * @param mergeSelector    heuristic to select nodes to merge (to build relaxed MDDs)
     * @param deleteSelector   heuristic to select nodes to delete (to build restricted MDDs)
     * @param variableSelector heuristic to select the next variable to be assigned
     * @param initialNode     the state where to start the layers
     */
    private MDD(Problem problem, MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector, Node initialNode) {
        this.root = new Layer(problem, this, initialNode, initialNode.layerNumber());
        this.exact = true;
        this.lastExactLayer = null;
        this.frontier = new HashSet<>();
        this.mergeSelector = mergeSelector;
        this.deleteSelector = deleteSelector;
        this.variableSelector = variableSelector;
    }

    /**
     * Sets the initial state of the MDD representation.
     *
     * @param initialNode the state where to start the layers
     */
    public void setInitialNode(Node initialNode) {
        this.root.reset(initialNode.layerNumber());
        this.root.addNode(initialNode);
        this.lastExactLayer = null;
        this.exact = true;
    }

    /**
     * Solves the given problem starting from the given node with layers of at most {@code width}
     * nodes by deleting some nodes and thus providing a feasible solution.
     *
     * @param width the maximum width of the layers
     * @return the {@code Node} object representing the best solution found
     */
    public Node solveRestricted(int width) {
        this.lastExactLayer = null;
        Layer lastLayer = root;

        boolean first = true;

        while (!lastLayer.isFinal()) {
            if (first) {
                lastLayer = lastLayer.nextLayer(Integer.MAX_VALUE, false); // if the first layer is not complete,
                first = false;                                                     // the start node is put in the queue again
            } else lastLayer = lastLayer.nextLayer(width, false);

            if (!lastLayer.isExact()) {
                this.exact = false;
            }
        }

        return lastLayer.best();
    }

    /**
     * Solves the given problem starting from the given node with layers of at most {@code width}
     * nodes by merging some nodes and thus providing a solution not always feasible.
     *
     * @param width the maximum width of the layers
     * @return the {@code Node} object representing the best solution found
     */
    public Node solveRelaxed(int width) {
        this.lastExactLayer = null;
        this.frontier.clear();
        Layer lastLayer = root;

        boolean first = true;

        while (!lastLayer.isFinal()) {
            if (first) {
                lastLayer = lastLayer.nextLayer(Integer.MAX_VALUE, true); // if the first layer is not complete,
                first = false;                                                    // the start node is put in the queue again
            } else lastLayer = lastLayer.nextLayer(width, true);

            if (lastLayer.isExact()) {
                this.lastExactLayer = lastLayer;
            } else {
                this.exact = false;
            }
        }

        for (Node s : lastLayer.nodes()) {
            if (s.isExact()) {
                this.frontier.add(s);
            }
        }

        return lastLayer.best();
    }

    /**
     * Returns a {@code boolean} telling if this MDD resolution was exact.
     *
     * @return {@code true} <==> all the layers are exact
     */
    public boolean isExact() {
        return this.exact;
    }

    /**
     * Solves the given problem starting from the given node.
     *
     * @return the {@code Node} object representing the best solution found
     */
    public Node solveExact() {
        return this.solveRelaxed(Integer.MAX_VALUE);
    }

    /**
     * Returns an exact cutset of the current MDD tree.
     *
     * @return a set of exact nodes being an exact cutset
     */
    public Collection<Node> exactCutset() {
        return this.lastExactLayerCutset();
        //return this.frontierCutset();
    }

    /**
     * Returns the last exact layer cutset,
     * which is the deepest layer equal to the corresponding complete MDD layer.
     *
     * @return the nodes of the last exact layer
     */
    private Collection<Node> lastExactLayerCutset() {
        return this.lastExactLayer.nodes();
    }

    /**
     * Returns the frontier cutset.
     * A node is in the frontier cutset if it is an exact node
     * and if one of its successors is not.
     *
     * @return the nodes of the frontier cutset
     */
    private Set<Node> frontierCutset() {
        return this.frontier;
    }
}
