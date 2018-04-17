package examples;

import core.Problem;
import core.Solver;
import core.Variable;
import dp.State;
import dp.StateRepresentation;
import heuristics.MinLPDeleteSelector;
import heuristics.MinLPMergeSelector;
import heuristics.SimpleVariableSelector;
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
		
		try {
			MISPState newMispState = mispState.copy();
			newMispState.bs.clear(u);
			State dontTake = new State(newMispState, s.variables(), s.value());
			dontTake.assign(u, 0);
			ret.add(dontTake);
		} catch(InconsistencyException e) {
			System.err.println(e.getMessage());
		}
		
		if(!mispState.bs.get(u)) { 
			return ret;
		}
		
		MISPState newMispState = mispState.copy();
		newMispState.bs.clear(u);
		
		for (int v : g[u]) {
			newMispState.bs.clear(v);
		}

		try {
			State take = new State(newMispState, s.variables(), s.value() + this.weights[u]);
			take.assign(u, 1);
			ret.add(take);
		} catch(InconsistencyException e) {
			System.err.println(e.getMessage());
		}
		
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
	
	private static LinkedList<Integer> [] toGraph(int n, Edge [] edges) {
		@SuppressWarnings("unchecked")
		LinkedList<Integer> [] g = new LinkedList[n];
		for (int i = 0; i < g.length; i++) {
			g[i] = new LinkedList<Integer>();
		}
		
		for(Edge e : edges) {
			g[e.u].add(e.v);
			g[e.v].add(e.u);
		}
		
		return g;
	}
	
	class MISPState implements StateRepresentation {
		
		int size;
		BitSet bs;
		
		public MISPState(int size) {
			this.size = size;
			this.bs = new BitSet(size);
			this.bs.flip(0, size);
		}
		
		public int hashCode() {
			return this.bs.hashCode();
		}
		
		public boolean equals(Object o) {
			return Integer.compare(this.hashCode(), o.hashCode()) == 0;
		}
		
		public MISPState copy() {
			MISPState next = new MISPState(this.size);
			next.bs.and(this.bs);
			return next;
		}
	}
	
	public static void main(String[] args) {
		double [] weights = {3, 4, 2, 2, 7};
		Edge [] edges = {new Edge(0, 1), new Edge(0, 2), new Edge(1, 2), new Edge(1, 3), new Edge(2, 3), new Edge(3, 4)};
		
		Problem p = new MISP(5, weights, edges);
		
		Solver solver = new Solver(p, new MinLPMergeSelector(), new MinLPDeleteSelector(), new SimpleVariableSelector());
		solver.solve();
	}
}