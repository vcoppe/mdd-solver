package dp;

import heuristics.DeleteSelector;
import heuristics.MergeSelector;
import heuristics.VariableSelector;

import java.util.ArrayList;
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
	ArrayList<Layer> layers;
	
	Layer root;
	Problem problem;
	MergeSelector mergeSelector;
	DeleteSelector deleteSelector;
	
	public DP(Problem problem, MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector) {
		this.problem = problem;
		this.nVariables = problem.nVariables();
		this.layers = new ArrayList<Layer>();
		this.root = new Layer(problem, variableSelector, problem.root(), 0);
		this.mergeSelector = mergeSelector;
		this.deleteSelector = deleteSelector;
	}
	
	public double solveRestricted(int width) {
		this.layers.clear();
		this.layers.add(root);
		Layer lastLayer = root;
		do {
			while(lastLayer.width() > width) {
				Set<State> toRemove = this.deleteSelector.select(lastLayer);
				lastLayer.removeStates(toRemove);
			}
			lastLayer = lastLayer.nextLayer();
			this.layers.add(lastLayer);
		} while(!lastLayer.isLast());
		
		return lastLayer.value();
	}
	
	public double solveRelaxed(int width) {
		this.layers.clear();
		this.layers.add(root);
		Layer lastLayer = root;
		do {
			while(lastLayer.width() > width) {
				Set<State> toMerge = this.mergeSelector.select(lastLayer);
				lastLayer.removeStates(toMerge);
				lastLayer.addState(this.problem.merge(toMerge));
			}
			lastLayer = lastLayer.nextLayer();
			this.layers.add(lastLayer);
		} while(!lastLayer.isLast());
		
		return lastLayer.value();
	}
	
	public double solveExact() {
		return this.solveRelaxed(Integer.MAX_VALUE);
	}
}
