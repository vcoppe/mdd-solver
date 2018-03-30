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
	
	private Map<StateRepresentation, State> states;
	private Problem problem;
	private VariableSelector variableSelector;
	private boolean exact;
	private int number;
	
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
	
	public void addState(State state) {
		this.exact &= state.isExact();
		state.setLayerNumber(this.number);
		if(this.states.containsKey(state.stateRepresentation())) {
			this.states.get(state.stateRepresentation()).update(state);
		} else {
			this.states.put(state.stateRepresentation(), state);
		}
	}
	
	public void addStates(Set<State> states) {
		for(State state : states) {
			this.exact &= state.isExact();
			state.setLayerNumber(this.number);
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
		this.exact = false;
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
		next.setExact(this.exact);
		
		return next;
	}
	
	public Set<State> states() {
		return new HashSet<State>(this.states.values());
	}
	
	public int width() {
		return this.states.size();
	}
	
	public State best() {
		State best = null;
		for(State state : this.states.values()) {
			if(best == null || state.value() > best.value()) {
				best = state;
			}
		}
		return best;
	}
	
	public boolean isExact() {
		return this.exact;
	}
	
	public void setExact(boolean exact) {
		this.exact = exact;
	}
	
	public boolean isFinal() {
		for(State state : this.states.values()) {
			return state.isFinal();
		}
		return false;
	}
	
	public int number() {
		return this.number;
	}
}
