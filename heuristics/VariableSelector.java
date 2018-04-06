package heuristics;

import core.Variable;

/**
 * Enables defining heuristics to select the variable to be assigned next in the MDD and/or in the
 * branch and bound algorithm.
 *  
 * @author Vianney Coppé
 */
public interface VariableSelector {
	
	/**
	 * Given the list of variables, choose the next variable that will be assigned.
	 * @param vars the list of variables to choose from
	 * @return the variable on which we will branch next
	 */
	Variable select(Variable [] vars);
	
}
