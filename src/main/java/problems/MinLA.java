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

        MinLAState[] minLAStates = new MinLAState[states.length];

        for (int i = 0; i < states.length; i++) {
            minLAStates[i] = (MinLAState) states[i].stateRepresentation;

            if (states[i].value() > maxValue) {
                maxValue = states[i].value();
                variables = states[i].variables;
                indexes = states[i].indexes;
            }
        }

        merge(minLAStates);

        return new State(minLAStates[0], variables, indexes, maxValue, false);
    }

    private void merge(MinLAState[] states) {
        LinkedList<Vertex>[] l = new LinkedList[states.length];
        int[][] match = new int[states.length][nVariables];

        for (int k = 0; k < states.length; k++) {
            l[k] = new LinkedList<>();

            for (int i = states[k].bs.nextSetBit(0); i >= 0; i = states[k].bs.nextSetBit(i + 1)) {
                int deg = 0, w = 0;
                for (int j = 0; j <= nVariables; j++) {
                    if (states[k].edgeWeight(i, j) != 0) {
                        deg++;
                        w += states[k].edgeWeight(i, j);
                    }
                }
                l[k].add(new Vertex(deg, w, i));
            }

            Collections.sort(l[k]);

            if (k == 0) for (int i = 0; i < l[k].size(); i++) match[k][i] = l[k].get(i).index;
            else for (int i = 0; i < l[k].size(); i++) match[k][match[0][i]] = l[k].get(i).index;
        }

        int w1, w2;
        for (Vertex v : l[0]) {
            int i = v.index;

            // common edges to other free vertices
            for (int j = states[0].bs.nextSetBit(0); j >= 0; j = states[0].bs.nextSetBit(j + 1)) {
                w1 = states[0].edgeWeight(i, j);
                w2 = Integer.MIN_VALUE;
                for (int k = 1; k < states.length; k++)
                    w2 = Math.max(w2, states[k].edgeWeight(match[k][i], match[k][j]));

                if (w1 != 0) {
                    if (w2 == 0) states[0].removeEdge(i, j);
                    else if (w2 > w1) states[0].replaceEdge(i, j, w2);
                }
            }

            // weight to fixed vertices
            w1 = states[0].edgeWeight(i, nVariables);
            w2 = Integer.MIN_VALUE;
            for (int k = 1; k < states.length; k++)
                w2 = Math.max(w2, states[k].edgeWeight(match[k][i], nVariables));

            if (w2 > w1) states[0].replaceEdge(i, nVariables, w2);
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
    }
}
