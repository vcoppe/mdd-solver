package examples;

import core.Problem;
import core.Variable;
import dp.DP;
import dp.State;
import utils.InconsistencyException;

import java.util.LinkedList;
import java.util.List;

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
		
		this.root = new State(variables, 0);
	}

	public State root() {
		return this.root;
	}

	public int nVariables() {
		return this.nVariables;
	}

	public List<State> successors(State s, Variable var) {
		int u = var.id();
		List<State> ret = new LinkedList<State>();
		
		try {
			State dontTake = new State(s.variables(), s.value());
			dontTake.assign(u, 0);
			ret.add(dontTake);
		} catch(InconsistencyException e) {
			System.err.println(e.getMessage());
		}
		
		for (int v : g[u]) {
			if(s.variables()[v].isAssigned() && s.variables()[v].contains(1)) {
				return ret;
			}
		}

		try {
			State take = new State(s.variables(), s.value() + this.weights[u]);
			take.assign(u, 1);
			ret.add(take);
		} catch(InconsistencyException e) {
			System.err.println(e.getMessage());
		}
		
		return ret;
	}

	public State merge(State s1, State s2) {
		return null;
	}
	
	public static void main(String[] args) {
		int [] weights = {1, 2, 3, 5};
		
		@SuppressWarnings("unchecked")
		LinkedList<Integer> [] g = new LinkedList[weights.length];
		for (int i = 0; i < weights.length; i++) {
			g[i] = new LinkedList<Integer>();
		}
		
		g[0].add(1); g[1].add(0);
		g[0].add(3); g[3].add(0);
		g[1].add(2); g[2].add(1);
		g[2].add(3); g[3].add(2);
		
		Problem p = new MISP(weights, g);
		DP dp = new DP(p, null, new SimpleVariableSelector());
		
		System.out.println(dp.solveExact());
	}
}
