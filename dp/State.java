package dp;

import core.Variable;
import utils.InconsistencyException;

public class State {
	
	double value;
	
	int nVariables;
	Variable [] variables;
	
	public State(Variable [] variables, double value) {
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

}
