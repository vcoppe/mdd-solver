package problems;

import core.Problem;
import core.Solver;
import core.Variable;
import dp.State;
import dp.StateRepresentation;
import heuristics.MinLPDeleteSelector;
import heuristics.MinLPMergeSelector;
import heuristics.SimpleVariableSelector;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.Map;

import static problems.Edge.toWeightedGraph;

/**
 * Implementation of the Minimun Linear Arrangement Problem.
 *
 * @author Vianney Coppé
 */
public class MLAP implements Problem {

    private Map<Integer, Double>[] g;

    private int nVariables;
    private State root;

    public MLAP(int n, Edge[] edges) {
        this(toWeightedGraph(n, edges));
    }

    private MLAP(Map<Integer, Double>[] g) {
        this.nVariables = g.length;
        this.g = g;

        Variable[] variables = new Variable[this.nVariables];
        for (int i = 0; i < this.nVariables; i++) {
            variables[i] = new Variable(i, this.nVariables);
        }

        this.root = new State(new MLAPState(this.nVariables), variables, 0);
    }

    public State root() {
        return this.root;
    }

    public int nVariables() {
        return this.nVariables;
    }

    public State[] successors(State s, Variable var) {
        int pos = var.id, j;
        MLAPState mlapState = (MLAPState) s.stateRepresentation;
        LinkedList<State> succs = new LinkedList<>();

        double value;
        Double w;

        for (int i = mlapState.bs.nextSetBit(0); i != -1; i = mlapState.bs.nextSetBit(i + 1)) {
            MLAPState succMlapState = mlapState.copy();
            succMlapState.bs.clear(i);

            value = s.value();
            for (int k = 0; k < pos; k++) {
                j = s.variables[k].value();
                w = g[i].get(j);
                if (w != null) value += w * (pos - k);
            }

            succs.add(s.getSuccessor(succMlapState, value, pos, i));
        }

        return succs.toArray(new State[0]);
    }

    public State merge(State[] states) {
        Variable[] variables = null;
        int[] indexes = null;
        double maxValue = -Double.MAX_VALUE;
        MLAPState mlapState = null;

        for (State state : states) {
            if (mlapState == null) {
                mlapState = (MLAPState) state.stateRepresentation;
            } else {
                mlapState.bs.or(((MLAPState) state.stateRepresentation).bs);
            }

            if (state.value() > maxValue) {
                maxValue = state.value();
                variables = state.variables;
                indexes = state.indexes;
            }
        }

        return new State(mlapState, variables, indexes, maxValue, false);
    }

    class MLAPState implements StateRepresentation {

        int size;
        BitSet bs;

        public MLAPState(int size) {
            this.size = size;
            this.bs = new BitSet(size);
            this.bs.flip(0, size);
        }

        public MLAPState(BitSet bitSet) {
            this.size = bitSet.size();
            this.bs = (BitSet) bitSet.clone();
        }

        public int hashCode() {
            return this.bs.hashCode();
        }

        public boolean equals(Object o) {
            return o instanceof MLAPState && this.bs.equals(((MLAPState) o).bs);
        }

        public boolean isFree(int u) {
            return this.bs.get(u);
        }

        public MLAPState copy() {
            return new MLAPState(this.bs);
        }

        public double rank(State state) {
            return state.value();
        }

        public String toString() {
            return this.bs.toString();
        }
    }

    public static void main(String[] args) {
        Edge[] edges = {new Edge(0, 1, -1), new Edge(0, 2, -2), new Edge(0, 3, -2),
                new Edge(1, 2, -3), new Edge(1, 3, -1), new Edge(2, 3, -1)};

        Problem p = new MLAP(4, edges);
        Solver solver = new Solver(p, new MinLPMergeSelector(), new MinLPDeleteSelector(), new SimpleVariableSelector());
        solver.solve();
    }
}
