package mdd;

/**
 * State representation in order to merge equivalent nodes in the MDD recursion.
 * Should be adapted to each problem.
 * Equivalent states should have the same hashCode and return {@code true} when compared
 * with {@code equals} in order to induce collision in the {@code HashMap}.
 */
public interface State {

    int hashCode();

    boolean equals(Object obj);

    /**
     * Function assigning a rank to each node of a layer used to determine which nodes to
     * delete/merge in restricted/relaxed decision diagrams.
     *
     * @param node the node containing this state
     * @return the rank
     */
    double rank(Node node);

    /**
     * Should return a deep copy of the state.
     *
     * @return another {@code State} object with the same properties
     */
    State copy();

}
