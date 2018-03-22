package dp;

import heuristics.DeleteSelector;
import heuristics.MergeSelector;
import heuristics.VariableSelector;

import java.util.Set;

import core.Problem;

/*
 * Represents the DP graph
 * Gives lower/upper bounds (or the exact solution) 
 * by solving the MDD representation with the given width
 * 
 * Solves a maximization problem by default
 */
public class DP {
	
	int nVariables;
	Layer [] layers;
	
	Problem problem;
	MergeSelector mergeSelector;
	DeleteSelector deleteSelector;
	
	public DP(Problem problem, MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector) {
		this.problem = problem;
		this.nVariables = problem.nVariables();
		this.layers = new Layer[this.nVariables+1];
		this.layers[0] = new Layer(problem.root(), variableSelector, problem);
		this.mergeSelector = mergeSelector;
		this.deleteSelector = deleteSelector;
	}
	
	public double solveRestricted(int width) {
		for(int i = 0; i < nVariables; i++) {
			while(this.layers[i].width() > width) {
				Set<State> toRemove = this.deleteSelector.select(this.layers[i]);
				this.layers[i].removeStates(toRemove);
			}
			// System.out.println("Layer " + i + " : size = " + this.layers[i].width());
			
			this.layers[i+1] = this.layers[i].nextLayer();
			// System.out.println("Layer " + (i+1) + " : size = " + this.layers[i+1].width());
		}
		return this.layers[nVariables].value();
	}
	
	public double solveRelaxed(int width) {
		for(int i = 0; i < nVariables; i++) {
			while(this.layers[i].width() > width) {
				Set<State> toMerge = this.mergeSelector.select(this.layers[i]);
				this.layers[i].removeStates(toMerge);
				this.layers[i].addState(this.problem.merge(toMerge));
			}
			// System.out.println("Layer " + i + " : size = " + this.layers[i].width());
			
			this.layers[i+1] = this.layers[i].nextLayer();
			// System.out.println("Layer " + (i+1) + " : size = " + this.layers[i+1].width());
		}
		return this.layers[nVariables].value();
	}
	
	public double solveExact() {
		return this.solveRelaxed(Integer.MAX_VALUE);
	}
}
