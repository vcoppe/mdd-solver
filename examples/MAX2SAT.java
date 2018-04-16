package examples;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import core.Problem;
import core.Solver;
import core.Variable;
import dp.State;
import dp.StateRepresentation;
import heuristics.SimpleDeleteSelector;
import heuristics.SimpleMergeSelector;
import heuristics.SimpleVariableSelector;
import utils.InconsistencyException;

public class MAX2SAT implements Problem {
	
	Map<Integer, double[]>[] g;
	
	int nVariables;
	State root;
	
	public MAX2SAT(Map<Integer, double[]>[] g) {
		this.nVariables = g.length;
		this.g = g;
		
		Variable [] variables = new Variable[this.nVariables];
		for(int i = 0; i < this.nVariables; i++) {
			variables[i] = new Variable(i, 2);
		}
		
		this.root = new State(new MAX2SATState(this.nVariables), variables, 0);
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
		
		Variable [] variables = s.variables();
		MAX2SATState max2satState = ((MAX2SATState) s.stateRepresentation());
		
		// assigning var to 0
		double [] benefits0 = new double[this.nVariables];
		double value0 = s.value() + Math.max(0, -max2satState.benefits[u]);
		
		for(int i = 0; i < this.nVariables; i++) {
			if(i != u && !variables[i].isAssigned()) {
				benefits0[i] = max2satState.benefits[i];
				if(g[u].containsKey(i)) {
					int numTT = 3, numTF = 2, numFT = 1, numFF = 0;
					if(u > i) {
						numTF = 1;
						numFT = 2;
					}
					
					value0 += g[u].get(i)[numFF] + g[u].get(i)[numFT] 
							+ Math.min(
									Math.max(0,  max2satState.benefits[i]) + g[u].get(i)[numTT],
									Math.max(0, -max2satState.benefits[i]) + g[u].get(i)[numTF]
									);
					benefits0[i] += g[u].get(i)[numTT] - g[u].get(i)[numTF];
				}
			}
		}
		
		State state0 = new State(new MAX2SATState(benefits0), variables, value0);
		
		try {
			state0.assign(u, 0);
		} catch (InconsistencyException e) {
			System.err.println(e.getMessage());
		}
		
		ret.add(state0);
		
		// assigning var to 1
		double [] benefits1 = new double[this.nVariables];
		double value1 = s.value() + Math.max(0, max2satState.benefits[u]);
		
		for(int i = 0; i < this.nVariables; i++) {
			if(i != u && !variables[i].isAssigned()) {
				benefits1[i] = max2satState.benefits[i];
				if(g[u].containsKey(i)) {
					int numTT = 3, numTF = 2, numFT = 1, numFF = 0;
					if(u > i) {
						numTF = 1;
						numFT = 2;
					}

					value1 += g[u].get(i)[numTF] + g[u].get(i)[numTT] 
							+ Math.min(
									Math.max(0,  max2satState.benefits[i]) + g[u].get(i)[numFT],
									Math.max(0, -max2satState.benefits[i]) + g[u].get(i)[numFF]
									);
					benefits1[i] += g[u].get(i)[numFT] - g[u].get(i)[numFF];
				}
			}
		}
		
		State state1 = new State(new MAX2SATState(benefits1), variables, value1);
		
		try {
			state1.assign(u, 1);
		} catch (InconsistencyException e) {
			System.err.println(e.getMessage());
		}
		
		ret.add(state1);
		
		return ret;
	}

	public State merge(Set<State> states) {
		Variable [] variables = null;
		double maxValue = Double.MIN_VALUE;
		double [] benefits = new double[nVariables];
		double [] newValues = new double[states.size()];
		MAX2SATState [] statesRep = new MAX2SATState[states.size()];
		
		int i = 0;
		for(State state : states) {
			if(variables == null) {
				variables = state.variables();
			}
			statesRep[i++] = (MAX2SATState) state.stateRepresentation();
			maxValue = Math.max(maxValue, state.value());
		}
		
		for(i = 0; i < nVariables; i++) {
			double sign = 0;
			double minValue = Double.MAX_VALUE;
			boolean same = true;
			
			for(MAX2SATState state : statesRep) {
				minValue = Math.min(minValue, Math.abs(state.benefits[i]));
				if(sign == 0 && state.benefits[i] != 0) {
					sign = state.benefits[i]/state.benefits[i]; // +/- 1
				} else if(sign * state.benefits[i] < 0) {
					same = false;
					break;
				}
			}
			
			if(same) {
				benefits[i] = sign * minValue;
			}
			
			for(int j = 0; j < newValues.length; j++) {
				newValues[j] += Math.abs(statesRep[j].benefits[i]) - Math.abs(benefits[i]);
			}
		}
		
		return new State(new MAX2SATState(benefits), variables, maxValue);
	}
	
	class MAX2SATState implements StateRepresentation {
		
		double [] benefits ;
		
		public MAX2SATState(int size) {
			this.benefits = new double[size];
		}
		
		public MAX2SATState(double [] benefits) {
			this.benefits = benefits;
		}
		
		public int hashCode() {
			return Arrays.hashCode(benefits);
		}
		
		public boolean equals(Object o) {
			if(!(o instanceof MAX2SATState)) {
				return false;
			}
			
			MAX2SATState other = (MAX2SATState) o;
			return Arrays.equals(benefits, other.benefits);
		}
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("unchecked")
		Map<Integer, double[]> [] g = new Map[3];
		for(int i = 0; i < g.length; i++) {
			g[i] = new HashMap<Integer, double[]>();
		}
		
		Clause [] clauses = {
				new Clause(0, 2, 1, 1, 3), new Clause(0, 2, 0, 0, 5),
				new Clause(0, 2, 0, 1, 4), new Clause(1, 2, 1, 0, 2), 
				new Clause(1, 2, 0, 0, 1), new Clause(1, 2, 1, 1, 5)
				};
		
		for(Clause clause : clauses) {
			if(!g[clause.u].containsKey(clause.v)) {
				g[clause.u].put(clause.v, new double[4]);
			}
			g[clause.u].get(clause.v)[clause.num] = clause.w;

			if(!g[clause.v].containsKey(clause.u)) {
				g[clause.v].put(clause.u, new double[4]);
			}
			g[clause.v].get(clause.u)[clause.num] = clause.w;
		}
		
		Problem p = new MAX2SAT(g);
		
		Solver solver = new Solver(p, new SimpleMergeSelector(), new SimpleDeleteSelector(), new SimpleVariableSelector());
		solver.solve();
	}

}

class Clause {
	
	int u, v;
	int num; 	// 0 = 00 -> FF
				// 1 = 01 -> FT
				// 2 = 10 -> TF
				// 3 = 11 -> TT
	double w;
	
	// two variables u, v and tu, tv their truth values
	// true <==> ((u == tu) || (v == tv))
	public Clause(int u, int v, int tu, int tv, double w) {
		if(u > v) {
			int tmp = u;
			u = v;
			v = tmp;
			
			tmp = tu;
			tu = tv;
			tv = tmp;
		}
		
		this.u = u;
		this.v = v;
		this.w = w;
		
		this.num = (tu << 1)|tv;
	}
}
