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
	
	Set<State> successors(State s, Variable var);

	State merge(Set<State> states);
	
}
