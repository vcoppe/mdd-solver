package heuristics;

import core.Variable;

public class SimpleVariableSelector implements VariableSelector {

	public Variable select(Variable [] vars) {
		for(Variable var : vars) {
			if(!var.isAssigned()) {
				return var;
			}
		}
		return null;
	}

}
