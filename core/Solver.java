package core;

import dp.DP;
import heuristics.DeleteSelector;
import heuristics.MergeSelector;
import heuristics.VariableSelector;

public class Solver {
	
	Problem problem;
	DP dp;
	
	public Solver(Problem problem, MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector) {
		this.problem = problem;
		this.dp = new DP(problem, mergeSelector, deleteSelector, variableSelector);
	}
	
	public void solve() {
		
	}
	
}
