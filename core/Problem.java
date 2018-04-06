package core;

import dp.State;

import java.util.Set;

/**
 * Enables solving new problems by implementing the successors and merge functions.
 * The interface StateRepresentation should also be implemented.
 * 
 * @author Vianney Coppé
 */
public interface Problem {
	
	/**
	 * Returns the initial state of the problem i. e. the empty assignment.
	 * @return an object {@code State} representing the root.
	 */
	State root();
	
	/**
	 * Returns the number of variables of the problem.
	 * @return an integer equal to the number of variables
	 */
	int nVariables();
	
	/**
	 * Given a state and a variable, returns the list of states reached after having assigned
	 * the variable to every possible value.
	 * Should transmit the cost, the exact property, the variables and assign a valid StateRepresentation
	 * to the successors.
	 * @param s a state
	 * @param var a variable belonging to the state's variables and not assigned yet
	 * @return a set of states resulting from a valid value assigned to the variable based on the given state
	 */
	Set<State> successors(State s, Variable var);

	/**
	 * Given a set of states, returns a new state with the same {@code StateRepresentation} 
	 * and a value leading to a relaxed MDD. 
	 * @param states a set of states
	 * @return the resulting merged state
	 */
	State merge(Set<State> states);
	
}
