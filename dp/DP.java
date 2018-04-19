package dp;

import heuristics.DeleteSelector;
import heuristics.MergeSelector;
import heuristics.VariableSelector;

import java.util.ArrayList;
import java.util.Set;

import core.Problem;

/**
 * Represents the DP graph.
 * Gives lower/upper bounds (or the exact solution)
 * by solving the MDD representation with the given width.
 * Solves a maximization problem by default.
 * 
 * @author Vianney Coppé
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
	
	/**
	 * Returns the DP representation of the problem.
	 * @param problem the implementation of a problem
	 * @param mergeSelector heuristic to select nodes to merge (to build relaxed MDDs)
	 * @param deleteSelector heuristic to select nodes to delete (to build restricted MDDs)
	 * @param variableSelector heuristic to select the next variable to be assigned
	 */
	public DP(Problem problem, MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector) {
		this(problem, mergeSelector, deleteSelector, variableSelector, problem.root());
	}
	
	/**
	 * Returns the DP representation of the problem.
	 * @param problem the implementation of a problem
	 * @param mergeSelector heuristic to select nodes to merge (to build relaxed MDDs)
	 * @param deleteSelector heuristic to select nodes to delete (to build restricted MDDs)
	 * @param variableSelector heuristic to select the next variable to be assigned
	 * @param initialState the state where to start the layers
	 */
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
	
	/**
	 * Sets the initial state of the DP representation.
	 * @param initialState the state where to start the layers
	 */
	public void setInitialState(State initialState) {
		this.root = new Layer(this.problem, this.variableSelector, initialState, initialState.layerNumber());
		this.lastExactLayer = null;
		this.exact = true;
	}
	
	/**
	 * Solves the given problem starting from the given node with layers of at most {@code width} 
	 * states by deleting some states and thus providing a feasible solution.
	 * @param width the maximum width of the layers
	 * @return the {@code State} object representing the best solution found
	 */
	public State solveRestricted(int width, long startTime, int timeOut) {
		this.layers.clear();
		this.layers.add(root);
		this.lastExactLayer = null;
		Layer lastLayer = root;
		
		while(!lastLayer.isFinal()) {
			if(System.currentTimeMillis()-startTime > timeOut * 1000) {
				return lastLayer.best();
			}
			
			lastLayer = lastLayer.nextLayer();

			while(lastLayer.width() > width) {
				Set<State> toRemove = this.deleteSelector.select(lastLayer, lastLayer.width()-width);
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
	
	/**
	 * Solves the given problem starting from the given node with layers of at most {@code width} 
	 * states by merging some states and thus providing a solution not always feasible.
	 * @param width the maximum width of the layers
	 * @return the {@code State} object representing the best solution found
	 */
	public State solveRelaxed(int width, long startTime, int timeOut) {
		this.layers.clear();
		this.layers.add(root);
		this.lastExactLayer = null;
		Layer lastLayer = root;
		
		while(!lastLayer.isFinal()) {
			if(System.currentTimeMillis()-startTime > timeOut * 1000) {
				return lastLayer.best();
			}
			
			lastLayer = lastLayer.nextLayer();
			
			while(lastLayer.width() > width) {
				Set<State> toMerge = this.mergeSelector.select(lastLayer, lastLayer.width()-width+1);
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
	
	/**
	 * Returns the deepest layer equal to the corresponding complete MDD layer.
	 * @return a {@code Layer} object representing the last exact layer
	 */
	public Layer lastExactLayer() {
		return this.lastExactLayer;
	}
	
	/**
	 * Returns a {@code boolean} telling if this DP resolution was exact.
	 * @return {@code true} <==> all the layers are exact
	 */
	public boolean isExact() {
		return this.exact;
	}
	
	/**
	 * Solves the given problem starting from the given node.
	 * @return the {@code State} object representing the best solution found
	 */
	public State solveExact() {
		return this.solveRelaxed(Integer.MAX_VALUE, Integer.MIN_VALUE, 0);
	}
}
