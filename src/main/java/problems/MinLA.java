package problems;

import core.Problem;
import core.Variable;
import mdd.Node;
import mdd.State;

import java.io.File;
import java.util.*;

import static problems.Edge.toWeightedGraphArray;

/**
 * Implementation of the Minimum Linear Arrangement Problem.
 *
 * @author Vianney Coppé
 */
public class MinLA implements Problem {

    private int[][] g;

    private int nVariables;
    private Node root;

    public double opt;

    public MinLA(int n, Edge[] edges) {
        this(toWeightedGraphArray(n, edges));
    }

    private MinLA(int[][] g) {
        this.nVariables = g.length;
        this.g = g;

        this.root = new Node(new MinLAState(this.nVariables), Variable.newArray(nVariables), 0);
    }

    public Node root() {
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

    public List<Node> successors(Node s, Variable var) {
        int pos = var.id;
        MinLAState minLAState = (MinLAState) s.state;
        List<Node> succs = new LinkedList<>();

        double value;

        for (int i = 0; i < nVariables; i++) {
            if (s.layerNumber() == 0 && i == nVariables - 1) break;
            if (((minLAState.mask[i / 64] >> (i % 64)) & 1L) == 1L) {
                MinLAState succMinLAState = minLAState.copy();
                succMinLAState.mask[i / 64] &= ~(1L << (i % 64));

                value = s.value();

                // add weight of edges between fixed vertices and free vertices
                for (int j = 0; j < nVariables; j++)
                    if (((succMinLAState.mask[j / 64] >> (j % 64)) & 1L) == 1L) {
                        int edgeWeight = succMinLAState.edgeWeight(i, j);
                        succMinLAState.removeEdge(i, j);
                        int toFixedWeight = succMinLAState.edgeWeight(j, nVariables);
                        succMinLAState.replaceEdge(j, nVariables, toFixedWeight + edgeWeight);
                        value += toFixedWeight + edgeWeight;
                    }

                succs.add(s.getSuccessor(succMinLAState, value, pos, i));
            }
        }

        return succs;
    }

    public Node merge(Node[] nodes) {
        Variable[] variables = null;
        int[] indexes = null;
        double maxValue = -Double.MAX_VALUE;

        MinLAState[] minLAStates = new MinLAState[nodes.length];

        for (int i = 0; i < nodes.length; i++) {
            minLAStates[i] = (MinLAState) nodes[i].state;

            if (nodes[i].value() > maxValue) {
                maxValue = nodes[i].value();
                variables = nodes[i].variables;
                indexes = nodes[i].indexes;
            }
        }

        merge(minLAStates);

        return new Node(minLAStates[0], variables, indexes, maxValue, false);
    }

    private void merge(MinLAState[] states) {
        LinkedList<Vertex>[] l = new LinkedList[states.length];
        int[][] match = new int[states.length][nVariables];

        for (int k = 0; k < states.length; k++) {
            l[k] = new LinkedList<>();

            for (int i = 0; i < nVariables; i++)
                if (((states[k].mask[i / 64] >> (i % 64)) & 1L) == 1L) {
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
            for (int j = 0; j < nVariables; j++)
                if (((states[0].mask[j / 64] >> (j % 64)) & 1L) == 1L) {
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

    class MinLAState implements State {

        long[] mask;
        int[][] gMod;

        MinLAState(int size) {
            this.mask = new long[size / 64 + 1];
            Arrays.fill(mask, ~0L);
            this.gMod = new int[size + 1][];
            this.gMod[size] = new int[size];
        }

        MinLAState(long[] mask, int[][] gMod) {
            this.mask = mask.clone();
            this.gMod = new int[gMod.length][];
            for (int i = 0; i < gMod.length; i++)
                if (gMod[i] != null) this.gMod[i] = gMod[i].clone();
        }

        public int hashCode() {
            return Arrays.hashCode(mask);
        }

        public boolean equals(Object o) {
            return o instanceof MinLAState && Arrays.equals(this.mask, ((MinLAState) o).mask);
        }

        public MinLAState copy() {
            return new MinLAState(this.mask, this.gMod);
        }

        public double rank(Node node) {
            return node.value();
        }

        public String toString() {
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < nVariables; i++)
                if (((mask[i / 64] >> (i % 64)) & 1L) == 1L)
                s.append(i + 1);
            return s.toString();
        }

        int edgeWeight(int i, int j) {
            if (i == nVariables) return gMod[i][j];
            if (j == nVariables) return gMod[j][i];
            int ans = g[i][j];
            if (gMod[i] != null) ans = Math.max(ans, gMod[i][j]);
            if (gMod[j] != null) ans = Math.max(ans, gMod[j][i]);
            return ans;
        }

        void removeEdge(int i, int j) {
            if (i == nVariables) gMod[i][j] = 0;
            else if (j == nVariables) gMod[j][i] = 0;
            else {
                if (gMod[i] == null && gMod[j] == null) gMod[i] = g[i].clone();
                if (gMod[i] != null) gMod[i][j] = 0;
                if (gMod[j] != null) gMod[j][i] = 0;
            }
        }

        void replaceEdge(int i, int j, int w) {
            if (i == nVariables) gMod[i][j] = w;
            else if (j == nVariables) gMod[j][i] = w;
            else {
                if (gMod[i] == null && gMod[j] == null) gMod[i] = g[i].clone();
                if (gMod[i] != null) gMod[i][j] = w;
                if (gMod[j] != null) gMod[j][i] = w;
            }
        }
    }
}
