package core;

import java.util.LinkedList;
import java.util.Queue;

import dp.DP;
import dp.State;
import heuristics.DeleteSelector;
import heuristics.MergeSelector;
import heuristics.VariableSelector;

public class Solver {
	
	private Problem problem;
	private DP dp;
	private int width = 2; // should be greater than the domain size of the variables
	
	public Solver(Problem problem, MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector) {
		this.problem = problem;
		this.dp = new DP(problem, mergeSelector, deleteSelector, variableSelector);
	}
	
	public void solve() {
		State best = null;
		double lowerBound = Double.MIN_VALUE;
		
		Queue<State> q = new LinkedList<State>();
		q.add(this.problem.root());
		
		while(!q.isEmpty()) {
			State state = q.poll();		

			System.out.println("Solving restricted");
			this.dp.setInitialState(state);
			State result = this.dp.solveRestricted(this.width);

			if(best == null || result.value() > best.value()) {
				best = result.copy();
				System.out.println("Improved solution : " + best.value());
				for(Variable var : best.variables()) {
					System.out.print(var.value() + " ");
				}
				System.out.println();
				System.out.println("Improved solution : " + result.value());
				for(Variable var : result.variables()) {
					System.out.print(var.value() + " ");
				}
				System.out.println();
			}

			if(!this.dp.isExact()) {
				System.out.println("Solving relaxed");
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
			for(Variable var : best.variables()) {
				System.out.print(var.value() + " ");
			}
			System.out.println();
		}
	}
	
}
