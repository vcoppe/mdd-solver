package dp;

/*
 * State representation in order to merge equivalent states in the DP recursion
 * Should be adapted to each problem
 */
public interface StateRepresentation {
	
	int hashCode();
	
	boolean equals(Object obj);
	
}
