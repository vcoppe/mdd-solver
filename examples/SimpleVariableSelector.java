package examples;

import core.Variable;
import heuristics.VariableSelector;

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
