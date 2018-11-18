package problems;

import core.Problem;
import core.Variable;
import mdd.State;
import mdd.StateRepresentation;

import java.io.File;
import java.util.*;

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

    public double opt;

    public MinLA(int n, Edge[] edges) {
        this(toWeightedGraph(n, edges));
    }

    private MinLA(Map<Integer, Double>[] g) {
        this.nVariables = g.length;
        this.g = g;

        Variable[] variables = new Variable[this.nVariables];
        for (int i = 0; i < this.nVariables; i++) {
            variables[i] = new Variable(i);
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
        double opt = 0;

        try {
            Scanner scan = new Scanner(new File(path));

            String line = scan.nextLine();
            String[] tokens = line.split("\\s+");

            if (tokens[0].equals("opt")) {
                opt = Integer.valueOf(tokens[1]);

                n = scan.nextInt();
                m = scan.nextInt();
            } else {
                n = Integer.valueOf(tokens[0]);
                m = scan.nextInt();
            }

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

        MinLA p = new MinLA(n, edges);
        p.opt = opt;
        return p;
    }

    public List<State> successors(State s, Variable var) {
        int pos = var.id;
        MinLAState minLAState = (MinLAState) s.stateRepresentation;
        List<State> succs = new LinkedList<>();

        double value;

        for (int i = minLAState.bs.nextSetBit(0); i >= 0; i = minLAState.bs.nextSetBit(i + 1)) {
            MinLAState succMinLAState = minLAState.copy();
            succMinLAState.bs.clear(i);

            value = s.value();
            Double w, w1, w2;
            for (int j = succMinLAState.bs.nextSetBit(0); j >= 0; j = succMinLAState.bs.nextSetBit(j + 1)) {
                w = g[i].get(j);
                if (w != null) value += w;
                for (int k = 0; k < pos; k++) {
                    int u = s.getVariable(k).value();
                    if (succMinLAState.dummyVertices[u] != null) w = succMinLAState.dummyVertices[u].get(j);
                    else w = g[u].get(j);
                    if (w != null) value += w;
                }
            }

            for (int j = 0; j < this.nVariables; j++)
                if (succMinLAState.dummyVertices[j] != null && !succMinLAState.used[j]) {
                    w = succMinLAState.dummyVertices[j].get(i);
                    if (w != null) value += w;
                    for (int k = 0; k < pos; k++) {
                        int u = s.getVariable(k).value();
                        if (succMinLAState.dummyVertices[u] != null) {
                            w1 = succMinLAState.dummyVertices[u].get(j);
                            w2 = succMinLAState.dummyVertices[j].get(u);
                            if (w1 != null && w2 != null) w = Math.min(w1, w2);
                            else w = 0.0;
                        } else w = g[u].get(j);
                        if (w != null) value += w;
                    }
                }

            succs.add(s.getSuccessor(succMinLAState, value, pos, i));
        }

        for (int i = 0; i < this.nVariables; i++)
            if (minLAState.dummyVertices[i] != null && !minLAState.used[i]) {
                MinLAState succMinLAState = minLAState.copy();
                succMinLAState.removeDummy(i);

                value = s.value();
                Double w, w1, w2;

                for (int j = succMinLAState.bs.nextSetBit(0); j >= 0; j = succMinLAState.bs.nextSetBit(j + 1)) {
                    w = minLAState.dummyVertices[i].get(j);
                    if (w != null) value += w;
                    for (int k = 0; k < pos; k++) {
                        int u = s.getVariable(k).value();
                        if (succMinLAState.dummyVertices[u] != null) w = succMinLAState.dummyVertices[u].get(j);
                        else w = g[u].get(j);
                        if (w != null) value += w;
                    }
                }

                for (int j = 0; j < this.nVariables; j++)
                    if (succMinLAState.dummyVertices[j] != null && !succMinLAState.used[j]) {
                        w1 = succMinLAState.dummyVertices[j].get(i);
                        w2 = succMinLAState.dummyVertices[i].get(j);
                        if (w1 != null && w2 != null) value += Math.min(w1, w2);
                        for (int k = 0; k < pos; k++) {
                            int u = s.getVariable(k).value();
                            if (succMinLAState.dummyVertices[u] != null) {
                                w1 = succMinLAState.dummyVertices[u].get(j);
                                w2 = succMinLAState.dummyVertices[j].get(u);
                                if (w1 != null && w2 != null) w = Math.min(w1, w2);
                                else w = 0.0;
                            } else w = g[u].get(j);
                            if (w != null) value += w;
                        }
                    }

                succs.add(s.getSuccessor(succMinLAState, value, pos, i));
        }

        return succs;
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
                merge(minLAState, (MinLAState) state.stateRepresentation);
            }

            if (state.value() > maxValue) {
                maxValue = state.value();
                variables = state.variables;
                indexes = state.indexes;
            }
        }

        return new State(minLAState, variables, indexes, maxValue, false);
    }

    private void merge(MinLAState state1, MinLAState state2) {
        for (int i = 0; i < this.nVariables; i++) {
            if (state1.bs.get(i) && !state2.bs.get(i)) {
                state1.bs.clear(i);
                state1.addDummy(i);
            } else if (!state1.bs.get(i) && state2.bs.get(i)) {
                state1.bs.clear(i);
                state2.addDummy(i);
            }
        }

        int i = 0, j = 0;
        while (i < this.nVariables) {
            while (i < this.nVariables && (state1.dummyVertices[i] == null || state1.used[i])) i++;
            if (i == this.nVariables) break;
            while (j < this.nVariables && (state2.dummyVertices[j] == null || state2.used[j])) j++;
            if (j == this.nVariables) break;

            Map<Integer, Double> m1 = state1.dummyVertices[i];
            Map<Integer, Double> m2 = state2.dummyVertices[j];

            for (int k = 0; k < this.nVariables; k++) {
                Double w1 = m1.get(k);
                Double w2 = m2.get(k);
                if (w1 != null) {
                    if (w2 == null) m1.remove(k);
                    else if (w1 != w2) m1.replace(k, Math.min(w1, w2));
                }
            }

            i++;
            j++;
        }
    }

    class MinLAState implements StateRepresentation {

        int size;
        BitSet bs;
        Map<Integer, Double>[] dummyVertices;
        boolean[] used;

        public MinLAState(int size) {
            this.size = size;
            this.bs = new BitSet(size);
            this.bs.flip(0, size);
            this.dummyVertices = new Map[size];
            this.used = new boolean[size];
        }

        public MinLAState(BitSet bitSet, Map<Integer, Double>[] dummyVertices, boolean[] used) {
            this.size = dummyVertices.length;
            this.bs = (BitSet) bitSet.clone();
            this.dummyVertices = new Map[size];
            for (int i = 0; i < size; i++)
                if (dummyVertices[i] != null) {
                    Map<Integer, Double> dummyVertex = new HashMap<>();
                    dummyVertex.putAll(dummyVertices[i]);
                    this.dummyVertices[i] = dummyVertex;
                }
            this.used = used.clone();
        }

        public int hashCode() {
            return this.bs.hashCode();
        }

        public boolean equals(Object o) {
            return o instanceof MinLAState && this.bs.equals(((MinLAState) o).bs);
        }

        public MinLAState copy() {
            return new MinLAState(this.bs, this.dummyVertices, this.used);
        }

        public double rank(State state) {
            return state.value();
        }

        public String toString() {
            return this.bs.toString();
        }

        public void addDummy(int i) {
            this.dummyVertices[i] = new HashMap<>();
            this.dummyVertices[i].putAll(g[i]);
            this.used[i] = false;
        }

        public void removeDummy(int i) {
            this.used[i] = true;
        }
    }
}
