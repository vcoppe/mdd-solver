package problems;

import core.Problem;
import core.Solver;
import core.Variable;
import heuristics.MinLPDeleteSelector;
import heuristics.MinLPMergeSelector;
import heuristics.SimpleVariableSelector;
import mdd.State;
import mdd.StateRepresentation;

import java.io.File;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

import static problems.Edge.toWeightedGraph;

/**
 * Implementation of the Minimum Linear Arrangement Problem.
 *
 * @author Vianney Coppé
 */
public class MinLA implements Problem {

    private Map<Integer, Double>[] g;

    private int nVariables;
    private State root;

    public MinLA(int n, Edge[] edges) {
        this(toWeightedGraph(n, edges));
    }

    private MinLA(Map<Integer, Double>[] g) {
        this.nVariables = g.length;
        this.g = g;

        Variable[] variables = new Variable[this.nVariables];
        for (int i = 0; i < this.nVariables; i++) {
            variables[i] = new Variable(i, this.nVariables);
        }

        this.root = new State(new MinLAState(this.nVariables), variables, 0);
    }

    public State root() {
        return this.root;
    }

    public int nVariables() {
        return this.nVariables;
    }

    /**
     * Instances can be found on <a href=https://www.cs.upc.edu/~jpetit/MinLA/Experiments/</a>.
     *
     * @param path path to a .gra file
     * @return a MinLA object encoding the problem
     */
    public static MinLA readGra(String path) {
        int n = 0, m, deg[];
        Edge[] edges = null;

        try {
            Scanner scan = new Scanner(new File(path));

            n = scan.nextInt();
            m = scan.nextInt();

            deg = new int[n];
            edges = new Edge[m * 2];

            for (int i = 0; i < n; i++) {
                deg[i] = scan.nextInt();
            }

            int cumul = 0, j;
            for (int i = 0; i < n; i++) {
                for (int k = cumul; k < cumul + deg[i]; k++) {
                    j = scan.nextInt();
                    edges[k] = new Edge(i, j, -1);
                }
                cumul += deg[i];
            }

            scan.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new MinLA(n, edges);
    }

    public static void main(String[] args) {
        Edge[] edges = {new Edge(0, 1, -1), new Edge(0, 2, -2), new Edge(0, 3, -2),
                new Edge(1, 2, -3), new Edge(1, 3, -1), new Edge(2, 3, -1)};

        Problem p = new MinLA(4, edges);
        Solver solver = new Solver(p, new MinLPMergeSelector(), new MinLPDeleteSelector(), new SimpleVariableSelector());
        solver.solve();
    }

    public State[] successors(State s, Variable var) {
        int pos = var.id, j;
        MinLAState minLAState = (MinLAState) s.stateRepresentation;
        LinkedList<State> succs = new LinkedList<>();

        double value;
        Double w;

        for (int i = minLAState.bs.nextSetBit(0); i != -1; i = minLAState.bs.nextSetBit(i + 1)) {
            if (var.id == 0 && i > this.nVariables / 2) break; // breaking the symmetry

            MinLAState succMinLAState = minLAState.copy();
            succMinLAState.bs.clear(i);

            value = s.value();
            for (int k = 0; k < pos; k++) {
                j = s.variables[k].value();
                w = g[i].get(j);
                if (w != null) value += w * (pos - k);
            }

            succs.add(s.getSuccessor(succMinLAState, value, pos, i));
        }

        return succs.toArray(new State[0]);
    }

    public State merge(State[] states) {
        Variable[] variables = null;
        int[] indexes = null;
        double maxValue = -Double.MAX_VALUE;
        MinLAState minLAState = null;

        for (State state : states) {
            if (minLAState == null) {
                minLAState = (MinLAState) state.stateRepresentation;
            } else {
                minLAState.bs.or(((MinLAState) state.stateRepresentation).bs);
            }

            if (state.value() > maxValue) {
                maxValue = state.value();
                variables = state.variables;
                indexes = state.indexes;
            }
        }

        return new State(minLAState, variables, indexes, maxValue, false);
    }

    class MinLAState implements StateRepresentation {

        int size;
        BitSet bs;

        public MinLAState(int size) {
            this.size = size;
            this.bs = new BitSet(size);
            this.bs.flip(0, size);
        }

        public MinLAState(BitSet bitSet) {
            this.size = bitSet.size();
            this.bs = (BitSet) bitSet.clone();
        }

        public int hashCode() {
            return this.bs.hashCode();
        }

        public boolean equals(Object o) {
            return o instanceof MinLAState && this.bs.equals(((MinLAState) o).bs);
        }

        public boolean isFree(int u) {
            return this.bs.get(u);
        }

        public MinLAState copy() {
            return new MinLAState(this.bs);
        }

        public double rank(State state) {
            return state.value();
        }

        public String toString() {
            return this.bs.toString();
        }
    }
}
