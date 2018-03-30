package dp;

import core.Problem;
import core.Variable;
import heuristics.VariableSelector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * Represents a layer of the MDD
 */
public class Layer {
	
	Map<StateRepresentation, State> states;
	Problem problem;
	VariableSelector variableSelector;
	boolean exact;
	int number;
	
	public Layer(Problem problem, VariableSelector variableSelector, int number) {
		this.states = new HashMap<StateRepresentation, State>();
		this.problem = problem;
		this.variableSelector = variableSelector;
		this.exact = true;
		this.number = number;
	}
	
	public Layer(Problem problem, VariableSelector variableSelector, State state, int number) {
		this.states = new HashMap<StateRepresentation, State>();
		this.states.put(state.stateRepresentation(), state);
		this.problem = problem;
		this.variableSelector = variableSelector;
		this.exact = state.isExact();
		this.number = number;
	}
	
	public Layer(Problem problem, VariableSelector variableSelector, Map<StateRepresentation, State> states, int number) {
		this.states = states;
		this.problem = problem;
		this.variableSelector = variableSelector;
		this.exact = true;
		this.number = number;
	}
	
	public void addState(State state) {
		this.exact &= state.isExact();
		if(this.states.containsKey(state.stateRepresentation())) {
			this.states.get(state.stateRepresentation()).update(state);
		} else {
			this.states.put(state.stateRepresentation(), state);
		}
	}
	
	public void addStates(Set<State> states) {
		for(State state : states) {
			this.exact &= state.isExact();
			if(this.states.containsKey(state.stateRepresentation())) {
				this.states.get(state.stateRepresentation()).update(state);
			} else {
				this.states.put(state.stateRepresentation(), state);
			}
		}
	}
	
	/*
	 * Should only be use to solve the restricted case so no need
	 * to restore an eventually lost exact property.
	 */
	public void removeStates(Set<State> states) {
		for(State state : states) {
			if(this.states.containsKey(state.stateRepresentation())) {
				this.states.remove(state.stateRepresentation());
			}
		}
	}
	
	public Layer nextLayer() {
		Layer next = new Layer(this.problem, this.variableSelector, this.number+1);
		Variable nextVar = null;
		for(State state : this.states.values()) {
			if(nextVar == null) {
				nextVar = this.variableSelector.select(state.variables());
			}
			next.addStates(this.problem.successors(state, nextVar));
		}
		
		return next;
	}
	
	public Set<State> states() {
		return new HashSet<State>(this.states.values());
	}
	
	public int width() {
		return this.states.size();
	}
	
	public double value() {
		double val = Double.MIN_VALUE;
		for(State state : this.states.values()) {
			val = Math.max(val, state.value());
		}
		return val;
	}
	
	public boolean isExact() {
		return this.exact;
	}
	
	public boolean isLast() {
		for(State state : this.states.values()) {
			return state.nVariables() == this.number;
		}
		return false;
	}
}
