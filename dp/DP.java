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
	
	private Layer root;
	private Layer lastExactLayer;
	private boolean exact;
	private ArrayList<Layer> layers;
	private Problem problem;
	private MergeSelector mergeSelector;
	private DeleteSelector deleteSelector;
	private VariableSelector variableSelector;
	
	public DP(Problem problem, MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector) {
		this(problem, mergeSelector, deleteSelector, variableSelector, problem.root());
	}
	
	public DP(Problem problem, MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector, State initialState) {
		this.problem = problem;
		this.layers = new ArrayList<Layer>();
		this.root = new Layer(problem, variableSelector, initialState, initialState.layerNumber());
		this.exact = true;
		this.lastExactLayer = null;
		this.mergeSelector = mergeSelector;
		this.deleteSelector = deleteSelector;
		this.variableSelector = variableSelector;
	}
	
	public void setInitialState(State initialState) {
		this.root = new Layer(this.problem, this.variableSelector, initialState, initialState.layerNumber());
		this.lastExactLayer = null;
		this.exact = true;
	}
	
	public State solveRestricted(int width) {
		this.layers.clear();
		this.layers.add(root);
		Layer lastLayer = root;
		while(!lastLayer.isFinal()) {
			lastLayer = lastLayer.nextLayer();

			while(lastLayer.width() > width) {
				Set<State> toRemove = this.deleteSelector.select(lastLayer);
				lastLayer.removeStates(toRemove);
			}
			
			this.layers.add(lastLayer);
			
			if(lastLayer.isExact()) {
				this.lastExactLayer = lastLayer;
			} else {
				this.exact = false;
			}
		}
		
		return lastLayer.best();
	}
	
	public State solveRelaxed(int width) {
		this.layers.clear();
		this.layers.add(root);
		Layer lastLayer = root;
		
		while(!lastLayer.isFinal()) {
			lastLayer = lastLayer.nextLayer();
			
			while(lastLayer.width() > width) {
				Set<State> toMerge = this.mergeSelector.select(lastLayer);
				lastLayer.removeStates(toMerge);
				State mergedState = this.problem.merge(toMerge);
				mergedState.setExact(false);
				lastLayer.addState(mergedState);
			}
			
			this.layers.add(lastLayer);
			
			if(lastLayer.isExact()) {
				this.lastExactLayer = lastLayer;
			} else {
				this.exact = false;
			}
		}
		
		return lastLayer.best();
	}
	
	public Layer lastExactLayer() {
		return this.lastExactLayer;
	}
	
	public boolean isExact() {
		return this.exact;
	}
	
	public State solveExact() {
		return this.solveRelaxed(Integer.MAX_VALUE);
	}
}
