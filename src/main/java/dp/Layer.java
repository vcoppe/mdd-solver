package dp;

import core.Problem;
import core.Variable;
import heuristics.VariableSelector;

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
	private VariableSelector variableSelector;
	private boolean exact;
	private int number;
	
	/**
	 * Returns an empty layer of the problem.
	 * @param problem the implementation of a problem
	 * @param variableSelector heuristic to select the next variable to be assigned
	 * @param number the number of the layer
	 */
	public Layer(Problem problem, VariableSelector variableSelector, int number) {
        this.states = new HashMap<>();
		this.problem = problem;
		this.variableSelector = variableSelector;
		this.exact = true;
		this.number = number;
	}
	
	/**
	 * Returns a layer containing the given state.
	 * @param problem the implementation of a problem
	 * @param variableSelector heuristic to select the next variable to be assigned
	 * @param state the state to be contained
	 * @param number the number of the layer
	 */
	public Layer(Problem problem, VariableSelector variableSelector, State state, int number) {
        this(problem, variableSelector, number);
		this.states.put(state.stateRepresentation(), state);
		this.exact = state.isExact();
	}
	
	/**
	 * Adds states to the layer or updates an existing state in the layer with the same {@code StateRepresentation}.
	 * @param state the state to be added
	 */
	public void addState(State state) {
		this.exact &= state.isExact();
		state.setLayerNumber(this.number);
		if(this.states.containsKey(state.stateRepresentation())) {
			this.states.get(state.stateRepresentation()).update(state);
		} else {
			this.states.put(state.stateRepresentation(), state);
		}
	}
	
	/**
	 * Adds a state to the layer or updates existing states in the layer with the same {@code StateRepresentation}.
     * @param states an array of {@code State} objects to be added
	 */
    private void addStates(State[] states) {
		for(State state : states) {
			this.addState(state);
		}
	}

	/**
	 * Remove the states from the layer.
     *
     * @param states   the states to be removed
	 */
    public void removeStates(State[] states) {
        for (State state : states) {
            if (this.states.containsKey(state.stateRepresentation())) {
				this.states.remove(state.stateRepresentation());
			}
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
            if (this.states.containsKey(state.stateRepresentation())) {
                this.states.remove(state.stateRepresentation());
            }
            frontier.addAll(state.exactParents());
        }
        this.exact = false;
    }
	
	/**
	 * Returns the next layer of the MDD using the {@code variableSelector} to choose the next variable
	 * to assign and the {@code problem} implementation to provide the successors of all the states of
	 * the layer.
	 * @return the next layer of the MDD
	 */
	public Layer nextLayer() {
		Layer next = new Layer(this.problem, this.variableSelector, this.number+1);
		Variable nextVar = null;

		next.setExact(this.exact);
		for(State state : this.states.values()) {
			if(nextVar == null) {
				nextVar = this.variableSelector.select(state.variables(), this);
			}

            State[] succ = this.problem.successors(state, nextVar);
			for(State s : succ) {
				if(state.isExact()) {
					s.addParent(state);
				} else {
					s.setExact(false);
				}
			}
			next.addStates(succ);
		}
		
		return next;
	}
	
	/**
	 * Returns a {@code Set} of all states contained in the layer.
	 * @return a {@code Set} with all the states
	 */
    public Collection<State> states() {
        return this.states.values();
	}
	
	/**
	 * Returns the width of the layer.
	 * @return the number of states in the layer
	 */
	public int width() {
		return this.states.size();
	}
	
	/**
	 * Returns the best state of the layer.
	 * @return a {@code State} object representing the best state of the layer
	 */
	public State best() {
		State best = null;
		for(State state : this.states.values()) {
			if(best == null || state.value() > best.value()) {
				best = state;
			}
		}
		return best;
	}
	
	/**
	 * Returns a {@code boolean} telling if the layer is equal to the corresponding complete MDD layer.
	 * @return {@code true} <==> the layer is equal to the corresponding complete MDD layer
	 */
	public boolean isExact() {
		return this.exact;
	}
	
	/**
	 * Help function to set the exact property of the layer.
	 * @param exact a {@code boolean} telling if the layer is exact or not
	 */
    private void setExact(boolean exact) {
		this.exact = exact;
	}
	
	/**
	 * Returns a {@code boolean} telling if the layer is the last one i. e. all the variables are assigned.
	 * @return {@code true} <==> the layer is the final one
	 */
	public boolean isFinal() {
		for(State state : this.states.values()) {
			return state.isFinal();
		}
		return false;
	}
	
	/**
	 * Help function to get the number of the layer in the MDD.
	 * @return the number of the layer
	 */
	public int number() {
		return this.number;
	}
}
