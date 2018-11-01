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

    public Set<State> frontier;
    public MergeSelector mergeSelector;
    public DeleteSelector deleteSelector;
    public VariableSelector variableSelector;
    private Layer root;
    private Layer lastExactLayer;
    private boolean exact;
    private Problem problem;

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
     * @param initialState     the state where to start the layers
     */
    private MDD(Problem problem, MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector, State initialState) {
        this.problem = problem;
        this.root = new Layer(problem, this, initialState, initialState.layerNumber());
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
     * @param initialState the state where to start the layers
     */
    public void setInitialState(State initialState) {
        this.root.reset(initialState.layerNumber());
        this.root.addState(initialState);
        this.lastExactLayer = null;
        this.exact = true;
    }

    /**
     * Solves the given problem starting from the given node with layers of at most {@code width}
     * states by deleting some states and thus providing a feasible solution.
     *
     * @param width the maximum width of the layers
     * @return the {@code State} object representing the best solution found
     */
    public State solveRestricted(int width, long startTime, int timeOut) {
        this.lastExactLayer = null;
        Layer lastLayer = root;

        while (!lastLayer.isFinal()) {
            if (System.currentTimeMillis() - startTime > timeOut * 1000) {
                return null;
            }

            lastLayer = lastLayer.nextLayer(width, false);

            if (!lastLayer.isExact()) {
                this.exact = false;
            }
        }

        return lastLayer.best();
    }

    /**
     * Solves the given problem starting from the given node with layers of at most {@code width}
     * states by merging some states and thus providing a solution not always feasible.
     *
     * @param width the maximum width of the layers
     * @return the {@code State} object representing the best solution found
     */
    public State solveRelaxed(int width, long startTime, int timeOut) {
        this.lastExactLayer = null;
        this.frontier.clear();
        Layer lastLayer = root;

        while (!lastLayer.isFinal()) {
            if (System.currentTimeMillis() - startTime > timeOut * 1000) {
                return null;
            }

            lastLayer = lastLayer.nextLayer(width, true);

            if (lastLayer.isExact()) {
                this.lastExactLayer = lastLayer;
            } else {
                this.exact = false;
            }
        }

        for (State s : lastLayer.states()) {
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
     * @return the {@code State} object representing the best solution found
     */
    public State solveExact() {
        return this.solveRelaxed(Integer.MAX_VALUE, Integer.MIN_VALUE, 0);
    }

    /**
     * Returns an exact cutset of the current MDD tree.
     *
     * @return a set of exact states being an exact cutset
     */
    public Collection<State> exactCutset() {
        //return this.lastExactLayerCutset();
        return this.frontierCutset();
    }

    /**
     * Returns the last exact layer cutset,
     * which is the deepest layer equal to the corresponding complete MDD layer.
     *
     * @return the states of the last exact layer
     */
    private Collection<State> lastExactLayerCutset() {
        return this.lastExactLayer.states();
    }

    /**
     * Returns the frontier cutset.
     * A state is in the frontier cutset if it is an exact state
     * and if one of its successors is not.
     *
     * @return the states of the frontier cutset
     */
    private Set<State> frontierCutset() {
        return this.frontier;
    }
}
