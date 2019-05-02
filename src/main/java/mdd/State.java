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

    double rank(Node node);

    State copy();

}
