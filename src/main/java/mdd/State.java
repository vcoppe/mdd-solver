package mdd;

import core.Variable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a particular state of the MDD.
 *
 * @author Vianney Coppé
 */
public class State<R extends StateRepresentation> implements Comparable<State> {

    public R stateRepresentation;
    public final Variable[] variables;
    public int[] indexes;
    private double value;
    private double relaxedValue;
    private boolean exact;
    private int layerNumber;
    private Set<State> parents;
    private final int nVariables;

    /**
     * @param stateRepresentation the state representation in the dynamic programming approach
     * @param variables           the variables of this state
     * @param value               the value of the objective function at this point
     */
    public State(R stateRepresentation, Variable[] variables, double value) {
        this(stateRepresentation, variables, value, true);
    }

    /**
     * @param stateRepresentation the state representation in the dynamic programming approach
     * @param variables           the variables of this state
     * @param value               the value of the objective function at this point
     * @param exact               a boolean telling if the state is exact or not
     */
    public State(R stateRepresentation, Variable[] variables, double value, boolean exact) {
        this(stateRepresentation, variables, new int[variables.length], value, exact);
        for (int i = 0; i < nVariables; i++) {
            this.indexes[i] = i;
        }
    }

    /**
     * @param stateRepresentation the state representation in the dynamic programming approach
     * @param variables           the variables of this state
     * @param indexes             the index of each variable indexed by their id
     * @param value               the value of the objective function at this point
     * @param exact               a boolean telling if the state is exact or not
     */
    public State(R stateRepresentation, Variable[] variables, int[] indexes, double value, boolean exact) {
        this.stateRepresentation = stateRepresentation;
        this.value = value;
        this.exact = exact;
        this.layerNumber = 0;
        this.nVariables = variables.length;
        this.relaxedValue = Double.MAX_VALUE;
        this.parents = new HashSet<>();

        this.variables = new Variable[this.nVariables];
        this.indexes = new int[this.nVariables];
        for (int i = 0; i < nVariables; i++) {
            this.variables[i] = variables[i];
            this.indexes[i] = indexes[i];
        }
    }

    /**
     * Returns a copy of the state.
     *
     * @return a different {@code State} object with the same properties
     */
    public State copy() {
        return new State(this.stateRepresentation.copy(), this.variables, this.indexes, this.value, this.exact);
    }

    /**
     * Assigns a variable of the problem to the given value.
     *
     * @param id    the identifier of the variable to be assigned
     * @param value the value to be assigned
     */
    public void assign(int id, int value) {
        this.variables[indexes[id]] = this.variables[indexes[id]].copy();
        this.variables[indexes[id]].assign(value);

        int i1 = indexes[id];
        int i2 = this.layerNumber - 1;

        Variable v1 = this.variables[i1];
        Variable v2 = this.variables[i2];

        this.variables[i1] = v2;
        this.variables[i2] = v1;

        this.indexes[v1.id] = i2;
        this.indexes[v2.id] = i1;
    }

    /**
     * Updates the state given another state with the same {@code StateRepresentation}.
     *
     * @param other another state with the same {@code StateRepresentation}
     */
    public void update(State other) {
        if (this.value < other.value()) {
            System.arraycopy(other.variables, 0, this.variables, 0, this.nVariables);
            System.arraycopy(other.indexes, 0, this.indexes, 0, this.nVariables);
            this.value = other.value;
            this.stateRepresentation = (R) other.stateRepresentation;
        }
        this.exact &= other.exact;
        this.parents.addAll(other.parents);
    }

    /**
     * Utility function to build a new state from this one, with one more variable bound.
     *
     * @param stateRepresentation the {@code StateRepresentation} for the successor state
     * @param value               the value for the successor state
     * @param id                  the id of the variable to bind
     * @param val                 the value to bind to the varible chosen
     * @return a new state with the internal properties required to be the successor of this state
     */
    public State getSuccessor(StateRepresentation stateRepresentation, double value, int id, int val) {
        State succ = new State(stateRepresentation, variables, indexes, value, exact);
        succ.setLayerNumber(this.layerNumber + 1);
        succ.assign(id, val);
        return succ;
    }

    /**
     * Help function to get the free variables of the state.
     *
     * @return an array with the free variables of the state
     */
    public Variable[] freeVariables() {
        return Arrays.copyOfRange(this.variables, this.layerNumber, this.nVariables);
    }

    /**
     * Help function to get the variable with id i.
     *
     * @param i the if of a variable
     * @return the variable with id i
     */
    public Variable getVariable(int i) {
        return this.variables[this.indexes[i]];
    }

    /**
     * Returns a {@code boolean} telling if the variable {@code i} is bound.
     *
     * @param i the id of a variable
     * @return {@code true} <==> the variable {@code i} is bound
     */
    public boolean isBound(int i) {
        return this.indexes[i] < this.layerNumber;
    }

    /**
     * Help function to get the value of the objective function in the state.
     *
     * @return the value of the objective function in the state
     */
    public double value() {
        return this.value;
    }

    /**
     * Help function to get the number of the layer this state belongs to.
     *
     * @return the number of the layer the state is in
     */
    public int layerNumber() {
        return this.layerNumber;
    }

    /**
     * Help function to set the number of the layer the state belongs to.
     *
     * @param layerNumber the number of the layer the state is in
     */
    public void setLayerNumber(int layerNumber) {
        this.layerNumber = layerNumber;
    }

    /**
     * Returns a {@code boolean} telling if the state is exact.
     *
     * @return {@code true} <==> the state is exact
     */
    public boolean isExact() {
        return this.exact;
    }

    /**
     * Help function to set the exact property of the state.
     *
     * @param exact a {@code boolean} telling if the state is exact or not
     */
    public void setExact(boolean exact) {
        this.exact = exact;
    }

    /**
     * Returns a {@code boolean} telling if the state belongs to the final layer i. e. all the variables are assigned.
     *
     * @return {@code true} <==> the state is a final one
     */
    public boolean isFinal() {
        return this.layerNumber == this.nVariables;
    }

    /**
     * Help function to get the number of variables of the state.
     *
     * @return the number of variables of the state
     */
    public int nVariables() {
        return this.nVariables;
    }

    /**
     * @param value the value to be set
     */
    public void setValue(double value) {
        this.value = value;
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

        if (this.hashCode() != other.hashCode()) {
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
