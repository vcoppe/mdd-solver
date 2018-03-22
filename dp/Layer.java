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
	
	public Layer(Problem problem, VariableSelector variableSelector) {
		this.states = new HashMap<StateRepresentation, State>();
		this.problem = problem;
		this.variableSelector = variableSelector;
	}
	
	public Layer(State state, VariableSelector variableSelector, Problem problem) {
		this.states = new HashMap<StateRepresentation, State>();
		this.states.put(state.stateRepresentation(), state);
		this.problem = problem;
		this.variableSelector = variableSelector;
	}
	
	public Layer(Map<StateRepresentation, State> states, VariableSelector variableSelector, Problem problem) {
		this.states = states;
		this.problem = problem;
		this.variableSelector = variableSelector;
	}
	
	public void addState(State state) {
		if(this.states.containsKey(state.stateRepresentation())) {
			this.states.get(state.stateRepresentation()).update(state);
		} else {
			this.states.put(state.stateRepresentation(), state);
		}
	}
	
	public void addStates(Set<State> states) {
		for(State state : states) {
			if(this.states.containsKey(state.stateRepresentation())) {
				this.states.get(state.stateRepresentation()).update(state);
			} else {
				this.states.put(state.stateRepresentation(), state);
			}
		}
	}
	
	public void removeStates(Set<State> states) {
		for(State state : states) {
			if(this.states.containsKey(state.stateRepresentation())) {
				this.states.remove(state.stateRepresentation());
			}
		}
	}
	
	public Layer nextLayer() {
		Layer next = new Layer(this.problem, this.variableSelector);
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
	
}
