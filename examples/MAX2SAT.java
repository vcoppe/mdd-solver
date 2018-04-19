package examples;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javafx.util.Pair;

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

public class MAX2SAT implements Problem {
	
	static Map<Integer, double[]>[] g;
	
	int nVariables;
	State root;
	
	/**
	 * Returns the representation of the MAX2SAT problem.
	 * @param n the number of boolean variables
	 * @param clauses an array of {@code Clause} objects with variables indexes in [0,n-1]
	 */
	public MAX2SAT(int n, Clause [] clauses) {
		this(toGraph(n, clauses));
	}
	
	/**
	 * Returns the representation of the MAX2SAT problem.
	 * @param g an array of maps where {@code g[i].get(j)} contains an array with the weight
	 * of the clauses with the variables {@code i} and {@code j} with
	 * 0 = 00 -> FF
	 * 1 = 01 -> FT
	 * 2 = 10 -> TF
	 * 3 = 11 -> TT
	 * where the first bit corresponds to the smallest variable id.
	 */
	public MAX2SAT(Map<Integer, double[]>[] g) {
		this.nVariables = g.length;
		MAX2SAT.g = g;
		
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
			if(!variables[i].isAssigned()) {
				if(u != i) benefits0[i] = max2satState.benefits[i];
				if(g[u].containsKey(i)) {
					int numTT = 3, numTF = 2, numFT = 1, numFF = 0;
					if(u > i) {
						numTF = 1;
						numFT = 2;
					}

					if(u != i) {
						value0 += g[u].get(i)[numFF] + g[u].get(i)[numFT] 
								+ Math.min(
										Math.max(0,  max2satState.benefits[i]) + g[u].get(i)[numTT],
										Math.max(0, -max2satState.benefits[i]) + g[u].get(i)[numTF]
										);
						benefits0[i] += g[u].get(i)[numTT] - g[u].get(i)[numTF];
					} else {
						value0 += g[u].get(i)[numFF];
					}
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
			if(!variables[i].isAssigned()) {
				if(u != i) benefits1[i] = max2satState.benefits[i];
				if(g[u].containsKey(i)) {
					int numTT = 3, numTF = 2, numFT = 1, numFF = 0;
					if(u > i) {
						numTF = 1;
						numFT = 2;
					}

					if(u != i) {
						value1 += g[u].get(i)[numTF] + g[u].get(i)[numTT] 
								+ Math.min(
										Math.max(0,  max2satState.benefits[i]) + g[u].get(i)[numFT],
										Math.max(0, -max2satState.benefits[i]) + g[u].get(i)[numFF]
										);
						benefits1[i] += g[u].get(i)[numFT] - g[u].get(i)[numFF];
					} else {
						value1 += g[u].get(i)[numTT];
					}
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
			newValues[i] = state.value();
			statesRep[i++] = (MAX2SATState) state.stateRepresentation();
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
		
		for(int j = 0; j < newValues.length; j++) {
			maxValue = Math.max(maxValue, newValues[j]);
		}
		
		return new State(new MAX2SATState(benefits), variables, maxValue);
	}
	
	private static Map<Integer, double[]>[] toGraph(int n, Clause [] clauses) {
		@SuppressWarnings("unchecked")
		Map<Integer, double[]> [] g = new Map[n];
		for(int i = 0; i < g.length; i++) {
			g[i] = new HashMap<Integer, double[]>();
		}
		
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
		
		return g;
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

		public double rank(State state) {
			double rank = state.value();
			
			for(int i = 0; i < benefits.length; i++) {
				rank += Math.abs(benefits[i]);
			}
			
			return rank;
		}

		public MAX2SATState copy() {
			return new MAX2SATState(this.benefits.clone());
		}
	}
	
	public static class MAX2SATVariableSelector implements VariableSelector {

		boolean done = false;
		int [] order;
		
		public Variable select(Variable[] vars, Layer layer) {
			if(!done) {
				order = new int[vars.length];
				@SuppressWarnings("unchecked")
				Pair<Double, Integer>[] l = new Pair[vars.length];
				
				for(int i = 0; i < vars.length; i++) {
					double sum = 0;
					for(double [] weights : g[i].values()) {
						for(double w : weights) {
							sum += w;
						}
					}
					l[i] = new Pair<Double, Integer>(sum, i);
				}
				
				Arrays.sort(l, (a, b) -> b.getKey().compareTo(a.getKey()));
				for(int i = 0; i < vars.length; i++) {
					order[i] = l[i].getValue();
				}
				
				done = true;
			}
			
			for(int index : order) {
				if(!vars[index].isAssigned()) {
					return vars[index];
				}
			}
			
			return null;
		}
		
	}
	
	public static void main(String[] args) {
		Clause [] clauses = {
				new Clause(0, 2, 1, 1, 3), new Clause(0, 2, 0, 0, 5),
				new Clause(0, 2, 0, 1, 4), new Clause(1, 2, 1, 0, 2), 
				new Clause(1, 2, 0, 0, 1), new Clause(1, 2, 1, 1, 5)
				};
		
		Problem p = new MAX2SAT(3, clauses);
		
		Solver solver = new Solver(p, new MinLPMergeSelector(), new MinLPDeleteSelector(), new MAX2SAT.MAX2SATVariableSelector());
		solver.solve();
	}

}