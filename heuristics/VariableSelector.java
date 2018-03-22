package heuristics;

import core.Variable;

import java.util.List;

public interface VariableSelector {
	
	Variable select(Variable [] vars);
	
}
