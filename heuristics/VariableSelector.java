package heuristics;

import core.Variable;

public interface VariableSelector {
	
	/*
	 * Given the list of variables, choose the next variable that will be assigned
	 * Find heuristics to select the most promising one to reduce the branching
	 */
	Variable select(Variable [] vars);
	
}
