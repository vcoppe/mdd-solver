package examples;

import core.Variable;
import heuristics.VariableSelector;

public class SimpleVariableSelector implements VariableSelector {

	public Variable select(Variable [] vars) {
		for(Variable var : vars) {
			if(!var.isAssigned()) {
				// System.out.println(">> Variable " + var.id() + " selected");
				return var;
			}
			// System.out.println(">> Variable " + var.id() + " already assigned");
		}
		return null;
	}

}
