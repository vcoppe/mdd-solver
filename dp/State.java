package dp;

import core.Variable;
import utils.InconsistencyException;

/*
 * Represents a particular state of the MDD
 */
public class State {
	
	double value;
	StateRepresentation stateRepresentation;
	
	int nVariables;
	Variable [] variables;
	
	public State(StateRepresentation stateRepresentation, Variable [] variables, double value) {
		this.stateRepresentation = stateRepresentation;
		this.value = value;
		this.nVariables = variables.length;
		this.variables = new Variable[this.nVariables];
		
		for (int i = 0; i < this.nVariables; i++) {
			this.variables[i] = variables[i].copy();
		}
	}
	
	public Variable [] variables() {
		return this.variables;
	}
	
	public void assign(int id, int value) throws InconsistencyException {
		this.variables[id].assign(value);
	}
	
	public double value() {
		return this.value;
	}
	
	public void update(State other) {
		if(this.value < other.value()) {
			for (int i = 0; i < this.nVariables; i++) {
				this.variables[i] = variables[i].copy();
			}
			this.value = other.value();
		}
	}
	
	public StateRepresentation stateRepresentation() {
		return this.stateRepresentation;
	}

}
