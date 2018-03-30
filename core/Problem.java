package core;

import dp.State;

import java.util.Set;

/*
 * Enables solving new problems by implementing the successors and merge functions
 * The interface StateRepresentation should also be implemented
 */
public interface Problem {
	
	State root();
	
	int nVariables();
	
	/*
	 * Given a state and a variable, returns the list of states reached after having assigned
	 * the variable to every possible value.
	 * Should transmit the cost, the exact property, the variables and assign a valid StateRepresentation
	 * to the successors.
	 */
	Set<State> successors(State s, Variable var);

	State merge(Set<State> states);
	
}
