package dp;

import core.Variable;
import utils.InconsistencyException;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a particular state of the MDD.
 * 
 * @author Vianney Coppé
 */
public class State implements Comparable<State> {
	
	private double value;
	private double relaxedValue;
	private boolean exact;
	private int layerNumber;
	private StateRepresentation stateRepresentation;
	private Set<State> parents;
	
	private int nVariables;
	private Variable [] variables;
	
	/**
	 * @param stateRepresentation the state representation in the dynamic programming approach
	 * @param variables the variables of this state
	 * @param value the value of the objective function at this point
	 */
	public State(StateRepresentation stateRepresentation, Variable [] variables, double value) {
		this(stateRepresentation, variables, value, true);
	}
	
	/**
	 * @param stateRepresentation the state representation in the dynamic programming approach
	 * @param variables the variables of this state
	 * @param value the value of the objective function at this point
	 * @param exact a boolean telling if the state is exact or not
	 */
	public State(StateRepresentation stateRepresentation, Variable [] variables, double value, boolean exact) {
		this.stateRepresentation = stateRepresentation.copy();
		this.value = value;
		this.exact = exact;
		this.layerNumber = 0;
		this.nVariables = variables.length;
		this.variables = new Variable[this.nVariables];
		this.relaxedValue = Double.MAX_VALUE;
		this.parents = new HashSet<State>();
		
		for (int i = 0; i < this.nVariables; i++) {
			this.variables[i] = variables[i].copy();
		}
	}
	
	/**
	 * Returns a copy of the state.
	 * @return a different {@code State} object with the same properties
	 */
	public State copy() {
		return new State(this.stateRepresentation, this.variables, this.value, this.exact);
	}
	
	/**
	 * Help function to get the variables of the state.
	 * @return an array with the variables of the state
	 */
	public Variable [] variables() {
		return this.variables;
	}
	
	/**
	 * Assigns a variable of the problem to the given value.
	 * @param id the identifier of the variable to be assigned
	 * @param value the value to be assigned
	 * @throws InconsistencyException if the value can not be assigned to the given variable
	 */
	public void assign(int id, int value) throws InconsistencyException {
		this.variables[id].assign(value);
	}
	
	/**
	 * Updates the state given another state with the same {@code StateRepresentation}.
	 * @param other another state with the same {@code StateRepresentation}
	 */
	public void update(State other) {
		if(this.value < other.value()) {
			for (int i = 0; i < this.nVariables; i++) {
				this.variables[i] = other.variables[i].copy();
			}
			this.value = other.value();
		}
		this.exact &= other.exact;
		this.parents.addAll(other.parents);
	}
	
	/**
	 * Help function to get the value of the objective function in the state.
	 * @return the value of the objective function in the state
	 */
	public double value() {
		return this.value;
	}
	
	/**
	 * Help function to get the number of the layer this state belongs to.
	 * @return the number of the layer the state is in
	 */
	public int layerNumber() {
		return this.layerNumber;
	}
	
	/**
	 * Help function to set the exact property of the state.
	 * @param exact a {@code boolean} telling if the state is exact or not
	 */
	public void setExact(boolean exact) {
		this.exact = exact;
	}
	
	/**
	 * Help function to set the number of the layer the state belongs to.
	 * @param layerNumber the number of the layer the state is in
	 */
	public void setLayerNumber(int layerNumber) {
		this.layerNumber = layerNumber;
	}
	
	/**
	 * Returns a {@code boolean} telling if the state is exact.
	 * @return {@code true} <==> the state is exact
	 */
	public boolean isExact() {
		return this.exact;
	}
	
	/**
	 * Returns a {@code boolean} telling if the state belongs to the final layer i. e. all the variables are assigned.
	 * @return {@code true} <==> the state is a final one
	 */
	public boolean isFinal() {
		return this.layerNumber == this.nVariables;
	}
	
	/**
	 * Help function to get the {@code StateRepresentation} of the state.
	 * @return the {@code StateRepresentation} object corresponding to the state
	 */
	public StateRepresentation stateRepresentation() {
		return this.stateRepresentation;
	}

	/**
	 * Help function to get the number of variables of the state.
	 * @return the number of variables of the state
	 */
	public int nVariables() {
		return this.nVariables;
	}

    /**
     * @return the best value of the relaxed DD
     */
	public double relaxedValue() {
		return relaxedValue;
	}

    /**
     * @param relaxedValue the best value of the relaxed DD
     */
    public void setRelaxedValue(double relaxedValue) {
        this.relaxedValue = relaxedValue;
    }

    /**
     * Adds an exact parent to the state
     *
     * @param s an exact parent of this state
     */
    public void addParent(State s) {
        this.parents.add(s);
    }

    /**
     * @return the set of all exact parents of this state
     */
    public Set<State> exactParents() {
        return this.parents;
    }

	public int hashCode() {
		return this.stateRepresentation.hashCode();
    }

    public boolean equals(Object o) {
        if (!(o instanceof State)) {
            return false;
        }

        State other = (State) o;

		if(this.layerNumber != other.layerNumber) {
			return false;
		}
		if(this.hashCode() != other.hashCode()) {
			return false;
        }

        return this.stateRepresentation.equals(other.stateRepresentation);
    }

    /**
     * Comparison based on the {@code StateRepresentation} rank.
     *
     * @param o an other state
     * @return the same comparison as the corresponding state representations
     */
    public int compareTo(State o) {
        return Double.compare(this.stateRepresentation.rank(this), o.stateRepresentation.rank(o));
    }
}
