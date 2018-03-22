package dp;

import core.Problem;
import heuristics.VariableSelector;

import java.util.ArrayList;
import java.util.List;

public class Layer {
	
	List<State> states;
	Problem problem;
	VariableSelector variableSelector;
	
	public Layer(Problem problem, VariableSelector variableSelector) {
		this.states = new ArrayList<State>();
		this.problem = problem;
		this.variableSelector = variableSelector;
	}
	
	public Layer(State state, VariableSelector variableSelector, Problem problem) {
		this.states = new ArrayList<State>();
		this.states.add(state);
		this.problem = problem;
		this.variableSelector = variableSelector;
	}
	
	public Layer(List<State> states, VariableSelector variableSelector, Problem problem) {
		this.states = states;
		this.problem = problem;
		this.variableSelector = variableSelector;
	}
	
	public void addState(State state) {
		this.states.add(state);
	}
	
	public void addStates(List<State> states) {
		for(State state : states) {
			this.states.add(state);
		}
	}
	
	public Layer nextLayer() {
		Layer next = new Layer(this.problem, this.variableSelector);
		for(State state : this.states) {
			next.addStates(this.problem.successors(state, this.variableSelector.select(state.variables())));
		}
		return next;
	}
	
	public int width() {
		return this.states.size();
	}
	
	public double value() {
		if(this.width() == 0) {
			return Double.NaN;
		}
		
		double val = Double.MIN_VALUE;
		for(State state : this.states) {
			val = Math.max(val, state.value());
		}
		return val;
	}
	
}
