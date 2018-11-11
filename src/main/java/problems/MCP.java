package problems;

import core.Problem;
import core.Variable;
import mdd.State;
import mdd.StateRepresentation;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static problems.Edge.toWeightedGraph;

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
     *
     * @param n     the number of vertices
     * @param edges a list of {@code Edge} objects with vertices indexes in [0,n-1]
     */
    public MCP(int n, Edge[] edges) {
        this(toWeightedGraph(n, edges));
    }

    /**
     * Returns the representation of the MCP problem.
     *
     * @param g the adjacency lists in the format of a map, where
     *          {@code g[i]} contains the key {@code j} if the vertices{@code i} and {@code j}
     *          are connected with an edge and {@code g[i].get(j)} is the weight of this edge
     */
    private MCP(Map<Integer, Double>[] g) {
        this.nVariables = g.length;
        this.g = g;

        Variable[] variables = new Variable[this.nVariables];
        for (int i = 0; i < this.nVariables; i++) {
            variables[i] = new Variable(i);
        }

        variables[0].assign(0); // arbitrarily assign first vertex to one side

        double rootValue = 0;
        for (Map<Integer, Double> adj : g) {
            for (double e : adj.values()) {
                rootValue += Math.min(0, e);
            }
        }
        rootValue /= 2; // edges were counted twice

        double[] benefits0 = new double[this.nVariables];
        for (int i = 1; i < this.nVariables; i++) {
            if (g[0].containsKey(i)) {
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

    public State merge(State[] states) {
        Variable[] variables = null;
        int[] indexes = null;
        double maxValue = -Double.MAX_VALUE;
        double[] benefits = new double[nVariables];
        double[] newValues = new double[states.length];
        MCPState[] statesRep = new MCPState[states.length];

        int i = 0;
        for (State state : states) {
            newValues[i] = state.value();
            statesRep[i++] = (MCPState) state.stateRepresentation;
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

        for (i = 0; i < newValues.length; i++) {
            if (newValues[i] > maxValue) {
                maxValue = newValues[i];
                variables = states[i].variables;
                indexes = states[i].indexes;
            }
        }

        return new State(new MCPState(benefits), variables, indexes, maxValue, false);
    }

    public List<State> successors(State s, Variable var) {
        int u = var.id;
        List<State> succs = new LinkedList<>();

        Variable[] variables = s.variables;
        MCPState mcpState = ((MCPState) s.stateRepresentation);

        // assigning var to 0
        double[] benefits0 = new double[this.nVariables];
        double value0 = s.value() + Math.max(0, -mcpState.benefits[u]);

        for (int i = 0; i < this.nVariables; i++) {
            if (i != u && !s.isBound(i)) {
                benefits0[i] = mcpState.benefits[i];
                if (g[u].containsKey(i)) {
                    if (mcpState.benefits[i] * g[u].get(i) <= 0) {
                        value0 += Math.min(Math.abs(mcpState.benefits[i]), Math.abs(g[u].get(i)));
                    }
                    benefits0[i] += g[u].get(i);
                }
            }
        }

        succs.add(s.getSuccessor(new MCPState(benefits0), value0, u, 0));

        // assigning var to 1
        double[] benefits1 = new double[this.nVariables];
        double value1 = s.value() + Math.max(0, mcpState.benefits[u]);

        for (int i = 0; i < this.nVariables; i++) {
            if (i != u && !s.isBound(i)) {
                benefits1[i] = mcpState.benefits[i];
                if (g[u].containsKey(i)) {
                    if (mcpState.benefits[i] * g[u].get(i) >= 0) {
                        value1 += Math.min(Math.abs(mcpState.benefits[i]), Math.abs(g[u].get(i)));
                    }
                    benefits1[i] -= g[u].get(i);
                }
            }
        }

        succs.add(s.getSuccessor(new MCPState(benefits1), value1, u, 1));

        return succs;
    }

    class MCPState implements StateRepresentation {

        double[] benefits;

        public MCPState(int size) {
            this.benefits = new double[size];
        }

        public MCPState(double[] benefits) {
            this.benefits = benefits;
        }

        public int hashCode() {
            return Arrays.hashCode(benefits);
        }

        public boolean equals(Object o) {
            if (!(o instanceof MCPState)) {
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
}
