package mdd;

import core.Problem;
import core.Variable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a layer of the MDD.
 *
 * @author Vianney Coppé
 */
public class Layer {

    private Map<StateRepresentation, State> states;
    private Problem problem;
    private MDD mdd;
    private boolean exact;
    public int number;

    /**
     * Returns an empty layer of the problem.
     *
     * @param problem the implementation of a problem
     * @param mdd     the  associated decision diagram
     * @param number  the number of the layer
     */
    public Layer(Problem problem, MDD mdd, int number) {
        this.states = new HashMap<>();
        this.problem = problem;
        this.mdd = mdd;
        this.exact = true;
        this.number = number;
    }

    /**
     * Returns a layer containing the given state.
     *
     * @param problem the implementation of a problem
     * @param mdd     the  associated decision diagram
     * @param state   the state to be contained
     * @param number  the number of the layer
     */
    public Layer(Problem problem, MDD mdd, State state, int number) {
        this(problem, mdd, number);
        this.states.put(state.stateRepresentation, state);
        this.exact = state.isExact();
    }

    /**
     * Clears the content of the layer in order to reuse the object.
     *
     * @param number the number of the layer
     */
    public void reset(int number) {
        this.states.clear();
        this.exact = true;
        this.number = number;
    }

    /**
     * Returns the next layer of the MDD using the {@code variableSelector} to choose the next variable
     * to assign and the {@code problem} implementation to provide the successors of all the states of
     * the layer.
     *
     * @return the next layer of the MDD
     */
    public Layer nextLayer(int width, boolean relaxed) {
        Variable nextVar = null;
        Layer next = new Layer(this.problem, this.mdd, this.number + 1);

        next.setExact(this.exact);
        for (State state : this.states.values()) {
            if (state.isExact()) {
                state.exactParents().clear(); // we do not need them anymore -> garbage collection
            }

            if (nextVar == null) {
                nextVar = this.mdd.variableSelector.select(state.freeVariables(), this);
            }

            for (State s : this.problem.successors(state, nextVar)) {
                if (state.isExact()) {
                    s.addParent(state);
                } else {
                    s.setExact(false);
                }
                next.addState(s);
            }

            if (next.width() > width) {
                if (relaxed) {
                    State[] toMerge = this.mdd.mergeSelector.select(next, next.width() - width + 1);
                    next.removeStates(toMerge, this.mdd.frontier);

                    State mergedState = this.problem.merge(toMerge);
                    mergedState.setExact(false);

                    next.addState(mergedState);
                } else {
                    State[] toRemove = this.mdd.deleteSelector.select(next, next.width() - width);
                    next.removeStates(toRemove);
                }
            }
        }

        return next;
    }

    /**
     * Adds states to the layer or updates an existing state in the layer with the same {@code StateRepresentation}.
     *
     * @param state the state to be added
     */
    public void addState(State state) {
        this.exact &= state.isExact();
        state.setLayerNumber(this.number);
        State existing = this.states.get(state.stateRepresentation);
        if (existing == null) this.states.put(state.stateRepresentation, state);
        else existing.update(state);
    }

    /**
     * Remove the states from the layer.
     *
     * @param states the states to be removed
     */
    public void removeStates(State[] states) {
        for (State state : states) {
            this.states.remove(state.stateRepresentation);
        }
        this.exact = false;
    }

    /**
     * Remove the states from the layer.
     *
     * @param states   the states to be removed
     * @param frontier the frontier cutset in order to add exact parents
     */
    public void removeStates(State[] states, Set<State> frontier) {
        for (State state : states) {
            this.states.remove(state.stateRepresentation);
            frontier.addAll(state.exactParents());
        }
        this.exact = false;
    }

    /**
     * Returns a {@code Set} of all states contained in the layer.
     *
     * @return a {@code Set} with all the states
     */
    public Collection<State> states() {
        return this.states.values();
    }

    /**
     * Returns the width of the layer.
     *
     * @return the number of states in the layer
     */
    public int width() {
        return this.states.size();
    }

    /**
     * Returns the best state of the layer.
     *
     * @return a {@code State} object representing the best state of the layer
     */
    public State best() {
        State best = null;
        for (State state : this.states.values()) {
            if (best == null || state.value() > best.value()) {
                best = state;
            }
        }
        return best;
    }

    /**
     * Returns a {@code boolean} telling if the layer is equal to the corresponding complete MDD layer.
     *
     * @return {@code true} <==> the layer is equal to the corresponding complete MDD layer
     */
    public boolean isExact() {
        return this.exact;
    }

    /**
     * Help function to set the exact property of the layer.
     *
     * @param exact a {@code boolean} telling if the layer is exact or not
     */
    private void setExact(boolean exact) {
        this.exact = exact;
    }

    /**
     * Returns a {@code boolean} telling if the layer is the last one i. e. all the variables are assigned.
     *
     * @return {@code true} <==> the layer is the final one
     */
    public boolean isFinal() {
        return this.number == this.problem.nVariables();
    }
}
