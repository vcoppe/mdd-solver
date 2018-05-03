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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the Maximum Cut Problem.
 * 
 * @author Vianney Coppé
 */
public class MCP implements Problem {

    private Map<Integer, Double>[] g;

    private int nVariables;
    private State root;
	
	/**
	 * Returns the representation of the MCP problem.
	 * @param n the number of vertices
	 * @param edges a list of {@code Edge} objects with vertices indexes in [0,n-1]
	 */
	public MCP(int n, Edge [] edges) {
		this(toGraph(n, edges));
	}
	
	/**
	 * Returns the representation of the MCP problem.
	 * @param g the adjacency lists in the format of a map, where 
	 * {@code g[i]} contains the key {@code j} if the vertices{@code i} and {@code j} 
	 * are connected with an edge and {@code g[i].get(j)} is the weight of this edge
	 */
	private MCP(Map<Integer, Double>[] g) {
		this.nVariables = g.length;
		this.g = g;
		
		Variable [] variables = new Variable[this.nVariables];
		for(int i = 0; i < this.nVariables; i++) {
			try {
				variables[i] = new Variable(i, 2);
			} catch (InconsistencyException e) {
				e.printStackTrace();
			}
		}
		
		try {
			variables[0].assign(0); // arbitrarily assign first vertex to one side
		} catch (InconsistencyException e) {
			System.out.println("Should not happen");
		}
		
		double rootValue = 0;
		for(Map<Integer, Double> adj : g) {
			for(double e : adj.values()) {
				rootValue += Math.min(0, e);
			}
		}
		rootValue /= 2; // edges were counted twice
		
		double [] benefits0 = new double[this.nVariables];
		for(int i = 1; i < this.nVariables; i++) {
			if(g[0].containsKey(i)) {
				benefits0[i] += g[0].get(i);
			}
		}
		
		this.root = new State(new MCPState(benefits0), variables, rootValue);
		this.root.setLayerNumber(1); // already 1 variable assigned
	}

	public State root() {
		return this.root;
	}

	public int nVariables() {
		return this.nVariables;
	}

    private static Map<Integer, Double>[] toGraph(int n, Edge[] edges) {
        @SuppressWarnings("unchecked")
        Map<Integer, Double>[] g = new Map[n];

        for (int i = 0; i < g.length; i++) {
            g[i] = new HashMap<>();
        }

        for (Edge e : edges) {
            g[e.u].put(e.v, e.w);
            g[e.v].put(e.u, e.w);
        }

        return g;
    }

	public State merge(State[] states) {
        Variable[] variables = null;
        double maxValue = Double.MIN_VALUE;
        double[] benefits = new double[nVariables];
		double[] newValues = new double[states.length];
		MCPState[] statesRep = new MCPState[states.length];

        int i = 0;
        for (State state : states) {
            if (variables == null) {
                variables = state.variables();
            }
            statesRep[i++] = (MCPState) state.stateRepresentation();
            maxValue = Math.max(maxValue, state.value());
        }

        for (i = 0; i < nVariables; i++) {
            double sign = 0;
            double minValue = Double.MAX_VALUE;
            boolean same = true;

            for (MCPState state : statesRep) {
                minValue = Math.min(minValue, Math.abs(state.benefits[i]));
                if (sign == 0 && state.benefits[i] != 0) {
                    sign = state.benefits[i] / state.benefits[i]; // +/- 1
                } else if (sign * state.benefits[i] < 0) {
                    same = false;
                    break;
                }
            }

            if (same) {
                benefits[i] = sign * minValue;
            }

            for (int j = 0; j < newValues.length; j++) {
                newValues[j] += Math.abs(statesRep[j].benefits[i]) - Math.abs(benefits[i]);
            }
        }

        return new State(new MCPState(benefits), variables, maxValue);
    }

    public State[] successors(State s, Variable var) {
		int u = var.id();

		Variable [] variables = s.variables();
		MCPState mcpState = ((MCPState) s.stateRepresentation());

		// assigning var to 0
		double [] benefits0 = new double[this.nVariables];
		double value0 = s.value() + Math.max(0, -mcpState.benefits[u]);

		for(int i = 0; i < this.nVariables; i++) {
			if (i != u && !variables[i].isBound()) {
				benefits0[i] = mcpState.benefits[i];
				if(g[u].containsKey(i)) {
					if(mcpState.benefits[i] * g[u].get(i) <= 0) {
						value0 += Math.min(Math.abs(mcpState.benefits[i]), Math.abs(g[u].get(i)));
					}
					benefits0[i] += g[u].get(i);
				}
			}
		}

        State state0 = new State(new MCPState(benefits0), variables, value0);

        try {
			state0.assign(u, 0);
		} catch (InconsistencyException e) {
			System.err.println(e.getMessage());
		}

        // assigning var to 1
		double [] benefits1 = new double[this.nVariables];
		double value1 = s.value() + Math.max(0, mcpState.benefits[u]);

        for(int i = 0; i < this.nVariables; i++) {
			if (i != u && !variables[i].isBound()) {
				benefits1[i] = mcpState.benefits[i];
				if(g[u].containsKey(i)) {
					if(mcpState.benefits[i] * g[u].get(i) >= 0) {
						value1 += Math.min(Math.abs(mcpState.benefits[i]), Math.abs(g[u].get(i)));
					}
					benefits1[i] -= g[u].get(i);
				}
			}
		}

        State state1 = new State(new MCPState(benefits1), variables, value1);

        try {
			state1.assign(u, 1);
		} catch (InconsistencyException e) {
			System.err.println(e.getMessage());
		}

        State[] ret = new State[2];
        ret[0] = state0;
        ret[1] = state1;

		return ret;
	}
	
	class MCPState implements StateRepresentation {
		
		double [] benefits ;
		
		public MCPState(int size) {
			this.benefits = new double[size];
		}
		
		public MCPState(double [] benefits) {
			this.benefits = benefits;
		}
		
		public int hashCode() {
			return Arrays.hashCode(benefits);
		}
		
		public boolean equals(Object o) {
			if(!(o instanceof MCPState)) {
				return false;
			}
			
			MCPState other = (MCPState) o;
			return Arrays.equals(benefits, other.benefits);
		}

		public double rank(State state) {
			double rank = state.value();

            for (double benefit : benefits) {
                rank += Math.abs(benefit);
			}
			
			return rank;
		}

		public MCPState copy() {
			return new MCPState(this.benefits.clone());
		}
	}
	
	public static void main(String[] args) {
		Edge [] edges = {new Edge(0, 1, 1), new Edge(0, 2, 2), new Edge(0, 3, -2),
				new Edge(1, 2, 3), new Edge(1, 3, -1), new Edge(2, 3, -1)};
		
		Problem p = new MCP(4, edges);
		
		Solver solver = new Solver(p, new MinLPMergeSelector(), new MinLPDeleteSelector(), new SimpleVariableSelector());
		solver.solve();
	}

}
