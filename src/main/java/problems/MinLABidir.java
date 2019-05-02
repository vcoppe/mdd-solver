package problems;

import core.Problem;
import core.Variable;
import heuristics.VariableSelector;
import mdd.Layer;
import mdd.Node;
import mdd.State;

import java.io.File;
import java.util.*;

import static problems.Edge.toWeightedGraph;

/**
 * Implementation of the Minimum Linear Arrangement Problem.
 *
 * @author Vianney Coppé
 */
public class MinLABidir implements Problem {

    public double opt;
    private Map<Integer, Integer>[] g;
    private int nVariables;
    private Node root;

    public MinLABidir(int n, Edge[] edges) {
        this(toWeightedGraph(n, edges));
    }

    private MinLABidir(Map<Integer, Integer>[] g) {
        this.nVariables = g.length;
        this.g = g;

        Variable[] variables = new Variable[this.nVariables];
        for (int i = 0; i < this.nVariables; i++) {
            variables[i] = new Variable(i);
        }

        this.root = new Node(new MinLAState(this.nVariables), variables, 0);
    }

    /**
     * Instances can be found on <a href=https://www.cs.upc.edu/~jpetit/MinLA/Experiments/</a>.
     *
     * @param path path to a .gra file
     * @return a MinLA object encoding the problem
     */
    public static MinLABidir readGra(String path) {
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

        MinLABidir p = new MinLABidir(n, edges);
        p.opt = opt;
        return p;
    }

    public Node root() {
        return this.root;
    }

    public int nVariables() {
        return this.nVariables;
    }

    public List<Node> successors(Node s, Variable var) {
        int pos = var.id;
        int layerNumber = s.layerNumber();
        MinLAState minLAState = (MinLAState) s.state;
        int[] side = minLAState.side;
        List<Node> succs = new LinkedList<>();

        int endR = side.length / 2 + side.length % 2;
        int endL = endR - 1;
        double value;

        if (layerNumber == 1 && s.getVariable(0).value() == side.length - 1) { // break symmetry
            return succs;
        }

        for (int i = 0; i < side.length; i++) {
            if (layerNumber == 0 && i == side.length - 1) break; // break symmetry
            if (layerNumber == 1 && i < s.getVariable(0).value()) continue; // break symmetry
            if (side[i] == 0) {
                MinLAState succMinLAState = minLAState.copy();
                succMinLAState.side[i] = (pos < endR) ? 1 : 2;

                value = s.value();

                Integer w;
                for (int j = 0; j < succMinLAState.side.length; j++) {
                    if (succMinLAState.side[j] == 0) {
                        w = g[i].get(j);
                        if (w != null) value += w;

                        if (pos < endR) { // left
                            for (int k = 0; k < pos; k++) {
                                int u = s.getVariable(k).value();
                                w = g[u].get(j);
                                if (w != null) value += w;
                            }
                        } else {
                            for (int k = side.length - 1; k > pos; k--) {
                                int u = s.getVariable(k).value();
                                w = g[u].get(j);
                                if (w != null) value += w;
                            }
                        }
                    } else if (succMinLAState.side[i] != succMinLAState.side[j] && succMinLAState.side[j] > 0) {
                        if (pos < endR) { // left
                            w = g[i].get(j);
                            if (w != null) value += w * ((side.length - pos - 1) - pos);
                        } else {
                            w = g[i].get(j);
                            if (w != null) value += w * (pos - (side.length - pos));
                        }
                    }
                }

                succs.add(s.getSuccessor(succMinLAState, value, pos, i));
            }
        }

        if (succs.isEmpty()) {
            Node succ = s.getSuccessor(s.state.copy(), s.value(), pos, -1);
            succ.setExact(false);
            succs.add(succ);
        }

        return succs;
    }

    public Node merge(Node[] nodes) {
        Variable[] variables = null;
        int[] indexes = null;
        double maxValue = -Double.MAX_VALUE;
        MinLAState minLAState = null;

        for (Node node : nodes) {
            if (minLAState == null) {
                minLAState = (MinLAState) node.state;
            } else {
                int[] side = ((MinLAState) node.state).side;
                for (int i = 0; i < side.length; i++) {
                    if (minLAState.side[i] != side[i]) {
                        minLAState.side[i] = -1;
                    }
                }
            }

            if (node.value() > maxValue) {
                maxValue = node.value();
                variables = node.variables;
                indexes = node.indexes;
            }
        }

        return new Node(minLAState, variables, indexes, maxValue, false);
    }

    public static class MinLABidirVariableSelector implements VariableSelector {

        public Variable select(Variable[] vars, Layer layer) {
            if (vars.length == 0) return null;

            boolean left = (layer.number % 2 == 0);
            int bestId = left ? Integer.MAX_VALUE : -1;
            int index = 0;

            for (int i = 0; i < vars.length; i++) {
                if (left && vars[i].id < bestId) {
                    bestId = vars[i].id;
                    index = i;
                } else if (!left && vars[i].id > bestId) {
                    bestId = vars[i].id;
                    index = i;
                }
            }

            return vars[index];
        }
    }

    class MinLAState implements State {

        int size;
        int[] side;

        public MinLAState(int size) {
            this.size = size;
            this.side = new int[size];
        }

        public MinLAState(int[] side) {
            this.size = side.length;
            this.side = side;
        }

        public int hashCode() {
            return Arrays.hashCode(this.side);
        }

        public boolean equals(Object o) {
            return o instanceof MinLAState && Arrays.equals(this.side, ((MinLAState) o).side);
        }

        public boolean isFree(int u) {
            return this.side[u] == 0;
        }

        public MinLAState copy() {
            return new MinLAState(this.side.clone());
        }

        public double rank(Node node) {
            return node.value();
        }
    }
}
