package core;

import utils.InconsistencyException;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a variable that can take integer values.
 */
public class Variable {
	
	private int id;
	private Set<Integer> domain;
	
	/**
	 * Returns a variable with the domain [min, max].
	 * @param id the id of the variable in the problem
	 * @param min the minimum value of the domain
	 * @param max the maximum value of the domain
	 */
	public Variable(int id, int min, int max) {
		this.id = id;
		this.domain = new HashSet<Integer>();
		for(int i = min; i <= max; i++) {
			this.domain.add(i);
		}
	}
	
	/**
	 * Returns a variable with the domain [0, n-1].
	 * @param id the id of the variable in the problem
	 * @param n the size of the domain
	 */
	public Variable(int id, int n) {
		this(id, 0, n-1);
	}
	
	/**
	 * Returns a variable with the given domain.
	 * @param id the id of the variable in the problem
	 * @param domain the possible values of the variable
	 */
	public Variable(int id, Set<Integer> domain) {
		this.id = id;
		this.domain = new HashSet<Integer>();
		for(Integer i : domain) {
			this.domain.add(i);
		}
	}
	
	/**
	 * Returns a copy of the variable.
	 * @return an different object {@code Variable} with the same domain
	 */
	public Variable copy() {
		return new Variable(this.id, this.domain);
	}
	
	/**
	 * Returns {@code true} <==> the variable is assigned.
	 * @return a {@code boolean} with the status of the variable 
	 */
	public boolean isAssigned() {
		return this.domainSize() == 1;
	}
	
	/**
	 * Returns a {@code Set} representation of the domain.
	 * @return a {@code Set} with the possible values of the variable
	 */
	public Set<Integer> domain() {
		return this.domain;
	}
	
	/**
	 * Returns the size of the domain.
	 * @return an integer equal to the number of possible values of the variable
	 */
	public int domainSize() {
		return this.domain.size();
	}
	
	/**
	 * Returns the id of the variable.
	 * @return an integer identifying the variable
	 */
	public int id() {
		return this.id;
	}
	
	/**
	 * Returns the value of the variable if it is assigned.
	 * @return the value of the variable of {@code -1} if it is not assigned
	 */
	public int value() {
		if(!this.isAssigned()) {
			return -1;
		}
		
		for(int v : this.domain) {
			return v;
		}
		return -1;
	}
	
	/**
	 * Assigns the variable to a value.
	 * @param value the value to be assigned
	 * @throws InconsistencyException if the variable is already assigned or if the value is
	 * not in the domain
	 */
	public void assign(int value) throws InconsistencyException {
		if(!this.contains(value)) {
			throw new InconsistencyException("Assigning incorrect value to variable : " + value + ".");
		}
		
		this.domain.clear();
		this.domain.add(value);
	}
	
	/**
	 * Returns a boolean telling if a value is in the domain of the variable.
	 * @param value an integer
	 * @return a {@code boolean}, {@code true} <==> value is a possible value of the variable
	 */
	public boolean contains(int value) {
		return this.domain.contains(value);
	}
	
	/**
	 * Removes a value from the domain of the variable.
	 * @param value an integer to be removed from the domain
	 * @throws InconsistencyException if the variable is already assigned or if the value is
	 * not in the domain
	 */
	public void remove(int value) throws InconsistencyException {
		if(this.isAssigned()) {
			throw new InconsistencyException("Removing value from assigned variable.");
		}
		
		if(!this.contains(value)) {
			throw new InconsistencyException("Removing incorrect value from variable.");
		}
		
		this.domain.remove(value);
	}
}
