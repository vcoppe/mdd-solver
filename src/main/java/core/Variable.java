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
    private boolean bound;
    private int value;
	
	/**
	 * Returns a variable with the domain [min, max].
	 * @param id the id of the variable in the problem
	 * @param min the minimum value of the domain
	 * @param max the maximum value of the domain
	 */
    public Variable(int id, int min, int max) throws InconsistencyException {
        if (min > max) {
            throw new InconsistencyException("Variable with empty domain");
        }

		this.id = id;
        this.bound = false;
        this.value = -1;
        this.domain = new HashSet<>();
		for(int i = min; i <= max; i++) {
			this.domain.add(i);
		}
	}
	
	/**
	 * Returns a variable with the domain [0, n-1].
	 * @param id the id of the variable in the problem
	 * @param n the size of the domain
	 */
    public Variable(int id, int n) throws InconsistencyException {
		this(id, 0, n-1);
	}
	
	/**
	 * Returns a variable with the given domain.
	 * @param id the id of the variable in the problem
	 * @param domain the possible values of the variable
	 */
    public Variable(int id, Set<Integer> domain) throws InconsistencyException {
		this.id = id;
        this.bound = false;
        this.value = -1;
        this.domain = domain;
        if (this.domain.size() == 0) {
            throw new InconsistencyException("Variable with empty domain");
        }
    }

    /**
     * Returns a variable with the given domain.
     *
     * @param id     the id of the variable in the problem
     * @param domain the possible values of the variable
     */
    public Variable(int id, Set<Integer> domain, boolean bound, int value) {
        this.id = id;
        this.domain = domain;
        this.bound = bound;
        this.value = value;
    }
	
	/**
	 * Returns a copy of the variable.
	 * @return an different object {@code Variable} with the same domain
	 */
	public Variable copy() {
        return new Variable(this.id, this.domain, this.bound, this.value);
	}
	
	/**
	 * Returns {@code true} <==> the variable is assigned.
	 * @return a {@code boolean} with the status of the variable 
     */
    public boolean isBound() {
        return this.bound;
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
        return this.value;
	}
	
	/**
	 * Assigns the variable to a value.
	 * @param value the value to be assigned
	 * @throws InconsistencyException if the variable is already assigned or if the value is
	 * not in the domain
	 */
    public void assign(int value) throws InconsistencyException {
        if (this.bound) {
            throw new InconsistencyException("Variable already bound");
        }

        if(!this.contains(value)) {
            throw new InconsistencyException("Assigning incorrect value to variable : " + value + ".");
        }

        this.bound = true;
        this.value = value;
    }
	
	/**
	 * Returns a boolean telling if a value is in the domain of the variable.
	 * @param value an integer
	 * @return a {@code boolean}, {@code true} <==> value is a possible value of the variable
	 */
	public boolean contains(int value) {
        if (this.bound) {
            return this.value == value;
        } else {
            return this.domain.contains(value);
        }
	}
}
