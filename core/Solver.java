package core;

import java.util.LinkedList;
import java.util.Queue;

import dp.DP;
import dp.State;
import heuristics.DeleteSelector;
import heuristics.MergeSelector;
import heuristics.VariableSelector;

/**
 * Implementation of the branch and bound algorithm for MDDs.
 * 
 * @author Vianney Coppé
 */
public class Solver {
	
	private Problem problem;
	private DP dp;
	private int width = 2; // should be greater than the domain size of the variables
	
	/**
	 * Constructor of the solver : allows the user to choose heuristics. 
	 * @param problem the implementation of a problem
	 * @param mergeSelector heuristic to select nodes to merge (to build relaxed MDDs)
	 * @param deleteSelector heuristic to select nodes to delete (to build restricted MDDs)
	 * @param variableSelector heuristic to select the next variable to be assigned
	 * @return the {@code Solver} object ready to solve the problem
	 */
	public Solver(Problem problem, MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector) {
		this.problem = problem;
		this.dp = new DP(problem, mergeSelector, deleteSelector, variableSelector);
	}
	
	/**
	 * Solves the given problem with the given heuristics and returns the optimal solution if it exists.
	 * @return an object {@code State} containing the optimal value and assignment
	 */
	public State solve() {
		State best = null;
		double lowerBound = Double.MIN_VALUE;
		
		Queue<State> q = new LinkedList<State>();
		q.add(this.problem.root());
		
		while(!q.isEmpty()) {
			State state = q.poll();		

			this.dp.setInitialState(state);
			State result = this.dp.solveRestricted(this.width);

			if(best == null || result.value() > best.value()) {
				best = result.copy();
				System.out.println("Improved solution : " + best.value());
			}

			if(!this.dp.isExact()) {
				this.dp.setInitialState(state);
				result = this.dp.solveRelaxed(this.width);

				if(result.value() > lowerBound) {
					q.addAll(this.dp.lastExactLayer().states());
				}
			}
		}
		
		if(best == null) {
			System.out.println("No solution found");
		} else {
			System.out.println("Optimal solution : " + best.value());
			System.out.print("Assignment       : ");
			for(Variable var : best.variables()) {
				System.out.print(var.value() + " ");
			}
			System.out.println();
		}
		
		return best;
	}
	
}
