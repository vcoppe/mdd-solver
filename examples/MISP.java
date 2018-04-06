package examples;

import core.Problem;
import core.Solver;
import core.Variable;
import dp.State;
import dp.StateRepresentation;
import utils.InconsistencyException;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.BitSet;

/*
 * Implementation of the Maximum Independent Set Problem
 */
public class MISP implements Problem {
	
	int [] weights;
	LinkedList<Integer> [] g;
	
	int nVariables;
	State root;
	
	public MISP(int [] weights, LinkedList<Integer> [] g) {
		this.nVariables = weights.length;
		this.weights = weights;
		this.g = g;
		
		Variable [] variables = new Variable[this.nVariables];
		for(int i = 0; i < this.nVariables; i++) {
			variables[i] = new Variable(i, 2);
		}

		this.root = new State(new MISPState(this.nVariables), variables, 0);
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
		double maxValue = Integer.MIN_VALUE;
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

	public State root() {
		return this.root;
	}

	public int nVariables() {
		return this.nVariables;
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
		int [] weights = {3, 4, 2, 2, 7};
		
		@SuppressWarnings("unchecked")
		LinkedList<Integer> [] g = new LinkedList[weights.length];
		for (int i = 0; i < weights.length; i++) {
			g[i] = new LinkedList<Integer>();
		}
		
		g[0].add(1); g[1].add(0);
		g[0].add(2); g[2].add(0);
		g[1].add(2); g[2].add(1);
		g[1].add(3); g[3].add(1);
		g[2].add(3); g[3].add(2);
		g[3].add(4); g[4].add(3);
		
		Problem p = new MISP(weights, g);
		
		Solver solver = new Solver(p, new SimpleMergeSelector(), new SimpleDeleteSelector(), new SimpleVariableSelector());
		solver.solve();
	}
}
