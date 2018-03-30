package dp;

import core.Variable;
import utils.InconsistencyException;

/*
 * Represents a particular state of the MDD
 */
public class State {
	
	private double value;
	private boolean exact;
	private StateRepresentation stateRepresentation;
	
	private int nVariables;
	private Variable [] variables;
	
	public State(StateRepresentation stateRepresentation, Variable [] variables, double value) {
		this(stateRepresentation, variables, value, true);
	}
	
	public State(StateRepresentation stateRepresentation, Variable [] variables, double value, boolean exact) {
		this.stateRepresentation = stateRepresentation;
		this.value = value;
		this.exact = exact;
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
	
	public void setExact(boolean exact) {
		this.exact = exact;
	}
	
	public boolean isExact() {
		return this.exact;
	}
	
	public void update(State other) {
		if(this.value < other.value()) {
			for (int i = 0; i < this.nVariables; i++) {
				this.variables[i] = variables[i].copy();
			}
			this.value = other.value();
		}
		this.exact &= other.exact;
	}
	
	public StateRepresentation stateRepresentation() {
		return this.stateRepresentation;
	}

	public int nVariables() {
		return this.nVariables;
	}
}
