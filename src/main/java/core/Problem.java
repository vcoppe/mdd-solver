package core;

import mdd.Node;

import java.util.List;

/**
 * Enables solving new problems by implementing the successors and merge functions.
 * The interface {@code State} should also be implemented.
 *
 * @author Vianney Coppé
 */
public interface Problem {

    /**
     * Returns the initial node of the problem i. e. the empty assignment.
     *
     * @return an object {@code Node} representing the root.
     */
    Node root();

    /**
     * Returns the number of variables of the problem.
     *
     * @return an integer equal to the number of variables
     */
    int nVariables();

    /**
     * Given a ndoe and a variable, returns the list of nodes reached after having assigned
     * the variable to every possible value.
     * Should transmit the cost, the exact property, the variables and assign a valid State
     * to the successors.
     *
     * @param s   a node
     * @param var a variable belonging to the node's variables and not assigned yet
     * @return an array of nodes resulting from a valid value assigned to the variable based on the given node
     */
    List<Node> successors(Node s, Variable var);

    /**
     * Given a set of nodes, returns a new node with a {@code State}
     * and a value leading to a relaxed MDD.
     *
     * @param nodes a set of nodes
     * @return the resulting merged node,
     * should have consistent {@code variables} and {@code indexes} arrays
     */
    Node merge(Node[] nodes);

}
