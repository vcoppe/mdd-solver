package mdd;

import core.Variable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a particular node of the MDD.
 *
 * @author Vianney Coppé
 */
public class Node<R extends State> implements Comparable<Node> {

    public final Variable[] variables;
    private final int nVariables;
    public R state;
    public int[] indexes;
    private double value;
    private double relaxedValue;
    private boolean exact;
    private int layerNumber;
    private Set<Node> parents;

    /**
     * @param state     the state representation in the dynamic programming approach
     * @param variables the variables of this node
     * @param value     the value of the objective function at this point
     */
    public Node(R state, Variable[] variables, double value) {
        this(state, variables, value, true);
    }

    /**
     * @param state     the state representation in the dynamic programming approach
     * @param variables the variables of this node
     * @param value     the value of the objective function at this point
     * @param exact     a boolean telling if the node is exact or not
     */
    public Node(R state, Variable[] variables, double value, boolean exact) {
        this(state, variables, new int[variables.length], value, exact);
        for (int i = 0; i < nVariables; i++) {
            this.indexes[i] = i;
        }
    }

    /**
     * @param state     the state representation in the dynamic programming approach
     * @param variables the variables of this node
     * @param indexes   the index of each variable indexed by their id
     * @param value     the value of the objective function at this point
     * @param exact     a boolean telling if the node is exact or not
     */
    public Node(R state, Variable[] variables, int[] indexes, double value, boolean exact) {
        this.state = state;
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
     * Returns a copy of the node.
     *
     * @return a different {@code Node} object with the same properties
     */
    public Node copy() {
        return new Node(this.state.copy(), this.variables, this.indexes, this.value, this.exact);
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
     * Updates the node given another node with the same {@code State}.
     *
     * @param other another node with the same {@code State}
     */
    public void update(Node other) {
        if (this.value < other.value()) {
            System.arraycopy(other.variables, 0, this.variables, 0, this.nVariables);
            System.arraycopy(other.indexes, 0, this.indexes, 0, this.nVariables);
            this.value = other.value;
            this.state = (R) other.state;
        }
        this.exact &= other.exact;
        this.parents.addAll(other.parents);
    }

    /**
     * Utility function to build a new node from this one, with one more variable bound.
     *
     * @param state the {@code State} for the successor node
     * @param value the value for the successor node
     * @param id    the id of the variable to bind
     * @param val   the value to bind to the variable chosen
     * @return a new node with the internal properties required to be the successor of this node
     */
    public Node getSuccessor(State state, double value, int id, int val) {
        Node succ = new Node(state, variables, indexes, value, exact);
        succ.setLayerNumber(this.layerNumber + 1);
        succ.assign(id, val);
        return succ;
    }

    /**
     * Help function to get the free variables of the node.
     *
     * @return an array with the free variables of the node
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
     * Help function to get the value of the objective function in the node.
     *
     * @return the value of the objective function in the node
     */
    public double value() {
        return this.value;
    }

    /**
     * Help function to get the number of the layer this node belongs to.
     *
     * @return the number of the layer the node is in
     */
    public int layerNumber() {
        return this.layerNumber;
    }

    /**
     * Help function to set the number of the layer the node belongs to.
     *
     * @param layerNumber the number of the layer the node is in
     */
    public void setLayerNumber(int layerNumber) {
        this.layerNumber = layerNumber;
    }

    /**
     * Returns a {@code boolean} telling if the node is exact.
     *
     * @return {@code true} <==> the node is exact
     */
    public boolean isExact() {
        return this.exact;
    }

    /**
     * Help function to set the exact property of the node.
     *
     * @param exact a {@code boolean} telling if the node is exact or not
     */
    public void setExact(boolean exact) {
        this.exact = exact;
    }

    /**
     * Returns a {@code boolean} telling if the node belongs to the final layer i. e. all the variables are assigned.
     *
     * @return {@code true} <==> the node is a final one
     */
    public boolean isFinal() {
        return this.layerNumber == this.nVariables;
    }

    /**
     * Help function to get the number of variables of the node.
     *
     * @return the number of variables of the node
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
     * Adds an exact parent to the node
     *
     * @param node an exact parent of this node
     */
    public void addParent(Node node) {
        this.parents.add(node);
    }

    /**
     * @return the set of all exact parents of this node
     */
    public Set<Node> exactParents() {
        return this.parents;
    }

    public int hashCode() {
        return this.state.hashCode();
    }

    public boolean equals(Object o) {
        if (!(o instanceof Node)) {
            return false;
        }

        Node other = (Node) o;

        if (this.hashCode() != other.hashCode()) {
            return false;
        }

        return this.state.equals(other.state);
    }

    /**
     * Comparison based on the {@code State} rank.
     *
     * @param o an other node
     * @return the same comparison as the corresponding state representations
     */
    public int compareTo(Node o) {
        return Double.compare(this.state.rank(this), o.state.rank(o));
    }
}
