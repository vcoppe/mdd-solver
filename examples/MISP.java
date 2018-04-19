package examples;

import core.Problem;
import core.Solver;
import core.Variable;
import dp.Layer;
import dp.State;
import dp.StateRepresentation;
import heuristics.MinLPDeleteSelector;
import heuristics.MinLPMergeSelector;
import heuristics.VariableSelector;
import utils.InconsistencyException;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.BitSet;

/**
 * Implementation of the Maximum Independent Set Problem.
 * 
 * @author Vianney Coppé
 */
public class MISP implements Problem {
	
	double [] weights;
	LinkedList<Integer> [] g;
	
	int nVariables;
	State root;
	
	/**
	 * Creates the representation of the MISP problem.
	 * @param n the number of vertices
	 * @param weights the weights of the vertices
	 * @param edges an array of {@code Edge} objects with vertices in [0,n-1]
	 */
	public MISP(int n, double [] weights, Edge [] edges) {
		this(weights, toGraph(n, edges));
	}
	
	/**
	 * Creates the representation of the MISP problem.
	 * @param weights the weights of the vertices
	 * @param g the adjacency lists
	 */
	public MISP(double [] weights, LinkedList<Integer> [] g) {
		this.nVariables = weights.length;
		this.weights = weights;
		this.g = g;
		
		Variable [] variables = new Variable[this.nVariables];
		for(int i = 0; i < this.nVariables; i++) {
			variables[i] = new Variable(i, 2);
		}

		this.root = new State(new MISPState(this.nVariables), variables, 0);
	}

	public State root() {
		return this.root;
	}

	public int nVariables() {
		return this.nVariables;
	}

	public Set<State> successors(State s, Variable var) {
		int u = var.id();
		Set<State> ret = new HashSet<State>();
		MISPState mispState = ((MISPState) s.stateRepresentation());
		Variable [] variables = s.variables();

		// assign 0
		MISPState mispState0 = mispState.copy();
		mispState0.bs.clear(u);
		State dontTake = new State(mispState0, variables, s.value());

		try {
			dontTake.assign(u, 0);
		} catch(InconsistencyException e) {
			e.printStackTrace();
		}
		
		ret.add(dontTake);
		
		if(!mispState.isFree(u)) { 
			return ret;
		}
		
		// assign 1
		MISPState mispState1 = mispState.copy();
		mispState1.bs.clear(u);
		
		for(int v : g[u]) {
			mispState1.bs.clear(v);
		}

		State take = new State(mispState1, variables, s.value() + this.weights[u]);

		try {
			take.assign(u, 1);
		} catch(InconsistencyException e) {
			e.printStackTrace();
		}

		ret.add(take);
		
		return ret;
	}

	public State merge(Set<State> states) {
		Variable [] variables = null;
		double maxValue = Double.MIN_VALUE;
		MISPState mispState = null;
		
		for(State state : states) {
			if(mispState == null) {
				variables = state.variables();
				mispState = ((MISPState) state.stateRepresentation()).copy();
			}
			
			mispState.bs.or(((MISPState) state.stateRepresentation()).bs);
			maxValue = Math.max(maxValue, state.value());
		}
		
		return new State(mispState, variables, maxValue);
	}
	
	public static LinkedList<Integer> [] toGraph(int n, Edge [] edges) {
		@SuppressWarnings("unchecked")
		LinkedList<Integer> [] adj = new LinkedList[n];
		for (int i = 0; i < n; i++) {
			adj[i] = new LinkedList<Integer>();
		}
		
		for(Edge e : edges) {
			adj[e.u].add(e.v);
			adj[e.v].add(e.u);
		}
		
		return adj;
	}
	
	class MISPState implements StateRepresentation {
		
		int size;
		BitSet bs;
		
		public MISPState(int size) {
			this.size = size;
			this.bs = new BitSet(size);
			this.bs.flip(0, size);
		}
		
		public MISPState(BitSet bitSet) {
			this.size = bitSet.size();
			this.bs = (BitSet) bitSet.clone();
		}
		
		public int hashCode() {
			return this.bs.hashCode();
		}
		
		public boolean equals(Object o) {
			if(!(o instanceof MISPState)) {
				return false;
			}
			
			MISPState other = (MISPState) o;
			if(this.bs.cardinality() != other.bs.cardinality()) {
				return false;
			}
			
			int j = -1;
			for(int i = this.bs.nextSetBit(0); i >= 0; i = this.bs.nextSetBit(i+1)) {
				j = other.bs.nextSetBit(j+1);
				if(i != j) {
					return false;
				}
			}
			
			return true;
		}
		
		public boolean isFree(int u) {
			return this.bs.get(u);
		}
		
		public MISPState copy() {
			return new MISPState(this.bs);
		}

		public double rank(State state) {
			return state.value();
		}
		
		public String toString() {
			return this.bs.toString();
		}
	}
	
	public static class MISPVariableSelector implements VariableSelector {

		public Variable select(Variable[] vars, Layer layer) {
			int minCount = Integer.MAX_VALUE, index = -1;
			int [] count = new int[vars.length];
			
			for(State state : layer.states()) {
				for(int i = 0; i < vars.length; i++) if(!vars[i].isAssigned()) {
					if(((MISPState) state.stateRepresentation()).isFree(i)) {
						count[i]++;
					}
					
					if(count[i] < minCount) {
						minCount = count[i];
						index = i;
					}
				}
			}
			
			return vars[index];
		}
		
	}
	
	public static void main(String[] args) {
		double [] weights = {3, 4, 2, 2, 7};
		Edge [] edges = {new Edge(0, 1), new Edge(0, 2), new Edge(1, 2), new Edge(1, 3), new Edge(2, 3), new Edge(3, 4)};
		
		Problem p = new MISP(5, weights, edges);
		
		Solver solver = new Solver(p, new MinLPMergeSelector(), new MinLPDeleteSelector(), new MISP.MISPVariableSelector());
		solver.solve();
	}
}