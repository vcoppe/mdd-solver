package problems;

import core.Problem;
import core.Variable;
import heuristics.MergeSelector;
import mdd.Layer;
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

    private Map<Integer, Integer>[] g;

    private int nVariables;
    private State root;

    public double opt;

    public MinLA(int n, Edge[] edges) {
        this(toWeightedGraph(n, edges));
    }

    private MinLA(Map<Integer, Integer>[] g) {
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

            // add weight of edges between fixed vertices and free vertices
            for (int j = succMinLAState.bs.nextSetBit(0); j >= 0; j = succMinLAState.bs.nextSetBit(j + 1)) {
                int edgeWeight = succMinLAState.edgeWeight(i, j);
                succMinLAState.removeEdge(i, j);
                int toFixedWeight = succMinLAState.edgeWeight(j, nVariables);
                succMinLAState.replaceEdge(j, nVariables, toFixedWeight + edgeWeight);
                value += toFixedWeight + edgeWeight;
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
        LinkedList<Vertex> l1 = new LinkedList<>(), l2 = new LinkedList<>();

        for (int i = state1.bs.nextSetBit(0); i >= 0; i = state1.bs.nextSetBit(i + 1)) {
            int deg = 0, w = 0;
            for (int j = 0; j <= nVariables; j++) {
                if (state1.edgeWeight(i, j) != 0) {
                    deg++;
                    w += state1.edgeWeight(i, j);
                }
            }
            l1.add(new Vertex(deg, w, i));
        }

        for (int i = state2.bs.nextSetBit(0); i >= 0; i = state2.bs.nextSetBit(i + 1)) {
            int deg = 0, w = 0;
            for (int j = 0; j <= nVariables; j++) {
                if (state2.edgeWeight(i, j) != 0) {
                    deg++;
                    w += state2.edgeWeight(i, j);
                }
            }
            l2.add(new Vertex(deg, w, i));
        }

        Collections.sort(l1);
        Collections.sort(l2);

        int[] match = new int[nVariables];
        for (int k = 0; k < l1.size(); k++) match[l1.get(k).index] = l2.get(k).index;

        int w1, w2;
        for (int z = 0; z < l1.size(); z++) {
            int i = l1.get(z).index;
            int j = match[i];

            // common edges to other free vertices
            for (int k = state1.bs.nextSetBit(0); k >= 0; k = state1.bs.nextSetBit(k + 1)) {
                w1 = state1.edgeWeight(i, k);
                w2 = state2.edgeWeight(j, match[k]);

                if (w1 != 0) {
                    if (w2 == 0) state1.removeEdge(i, k);
                    else if (w2 > w1) state1.replaceEdge(i, k, w2);
                }
            }

            // weight to fixed vertices
            w1 = state1.edgeWeight(i, nVariables);
            w2 = state2.edgeWeight(j, nVariables);

            if (w2 > w1) state1.replaceEdge(i, nVariables, w2);
        }
    }

    static class MinLAMergeSelector implements MergeSelector {

        public State[] select(Layer layer, int number) {
            State[] states = new State[layer.width()];
            layer.states().toArray(states);

            Arrays.sort(states);

            int index1 = number - 1, index2 = 0;

            BitSet bs1 = ((MinLAState) states[index1].stateRepresentation).bs;
            BitSet bs2 = (BitSet) ((MinLAState) states[0].stateRepresentation).bs.clone();

            bs2.and(bs1);
            int bestMatch = bs2.cardinality();

            for (int i = 1; i < states.length; i++) {
                if (i != index1) {
                    bs2 = (BitSet) ((MinLAState) states[i].stateRepresentation).bs.clone();
                    bs2.and(bs1);
                    int match = bs2.cardinality();
                    if (match > bestMatch) {
                        bestMatch = match;
                        index2 = i;
                    }
                }
            }

            return new State[]{states[index1], states[index2]};
        }
    }

    class MinLAState implements StateRepresentation {

        int size;
        BitSet bs;
        Map<Integer, Integer>[] gMod;

        public MinLAState(int size) {
            this.size = size;
            this.bs = new BitSet(size);
            this.bs.flip(0, size);
            this.gMod = new HashMap[size + 1];
            this.gMod[size] = new HashMap<>();
            for (int i = 0; i < size; i++) this.gMod[size].put(i, 0);
        }

        public MinLAState(BitSet bitSet, Map<Integer, Integer>[] gMod) {
            this.size = gMod.length;
            this.bs = (BitSet) bitSet.clone();
            this.gMod = new HashMap[size];
            for (int i = 0; i < size; i++)
                if (gMod[i] != null) this.gMod[i] = new HashMap<>(gMod[i]);
        }

        public int hashCode() {
            return this.bs.hashCode();
        }

        public boolean equals(Object o) {
            return o instanceof MinLAState && this.bs.equals(((MinLAState) o).bs);
        }

        public MinLAState copy() {
            return new MinLAState(this.bs, this.gMod);
        }

        public double rank(State state) {
            return state.value();
        }

        public String toString() {
            StringBuilder s = new StringBuilder();
            for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1))
                s.append(i + 1);
            return s.toString();
        }

        public int edgeWeight(int i, int j) {
            Integer w1, w2;

            if (gMod[i] != null) w1 = gMod[i].get(j);
            else w1 = g[i].get(j);
            if (w1 == null) w1 = 0;

            if (gMod[j] != null) w2 = gMod[j].get(i);
            else w2 = g[j].get(i);
            if (w2 == null) w2 = 0;

            if (i == nVariables) return w1;
            if (j == nVariables) return w2;

            return Math.max(w1, w2);
        }

        public void removeEdge(int i, int j) {
            if (gMod[i] != null) gMod[i].remove(j);
            else if (gMod[j] != null) gMod[j].remove(i);
            else {
                gMod[i] = new HashMap<>(g[i]);
                gMod[i].remove(j);
            }
        }

        public void replaceEdge(int i, int j, int w) {
            if (i == nVariables) gMod[i].replace(j, w);
            else if (j == nVariables) gMod[j].replace(i, w);
            else if (gMod[i] == null && gMod[j] == null) {
                gMod[i] = new HashMap<>(g[i]);
                gMod[i].replace(j, w);
            } else if (gMod[i] != null) gMod[i].replace(j, w);
            else if (gMod[j] != null) gMod[j].replace(i, w);
        }

        public int mergeWeight(MinLAState other, int i, int j) {
            int w1, w2, weight = 0;

            if (this.bs.get(i) && other.bs.get(j)) {
                weight = Math.max(this.edgeWeight(i, nVariables), other.edgeWeight(j, nVariables));

                for (int k = 0; k < nVariables; k++) {
                    if (k != i && k != j && this.bs.get(k) && other.bs.get(k)) {
                        w1 = this.edgeWeight(i, k);
                        w2 = other.edgeWeight(j, k);

                        weight += Math.max(w1, w2); // weights are negative
                    }
                }
            }

            return weight;
        }
    }
}
